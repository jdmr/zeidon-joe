/**
 *
 */
package com.quinsoft.zeidon.standardoe;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.quinsoft.zeidon.CommitOptions;
import com.quinsoft.zeidon.Committer;
import com.quinsoft.zeidon.EntityCursor;
import com.quinsoft.zeidon.GenKeyHandler;
import com.quinsoft.zeidon.Task;
import com.quinsoft.zeidon.View;
import com.quinsoft.zeidon.ZeidonException;
import com.quinsoft.zeidon.dbhandler.DbHandler;
import com.quinsoft.zeidon.dbhandler.JdbcHandlerUtils;
import com.quinsoft.zeidon.objectdefinition.DataRecord;
import com.quinsoft.zeidon.objectdefinition.LazyLoadConfig;
import com.quinsoft.zeidon.objectdefinition.RelField;
import com.quinsoft.zeidon.objectdefinition.RelRecord;
import com.quinsoft.zeidon.objectdefinition.ViewAttribute;
import com.quinsoft.zeidon.objectdefinition.ViewEntity;
import com.quinsoft.zeidon.objectdefinition.ViewOd;

/**
 * Commits a list of OIs to a DB using a DB handler.  It will use a genkey handler to generate
 * the keys ahead of time.
 *
 * @author dgc
 *
 */
class CommitToDbUsingGenkeyHandler implements Committer
{
    private List<ViewImpl>       viewList;
    private Task                 task;
    private View                 genKeyObj;
    private DbHandler            dbHandler;
    private GenKeyHandler        genkeyHandler;
    private CommitOptions        options;

    /* (non-Javadoc)
     * @see com.quinsoft.zeidon.standardoe.Committer#init(com.quinsoft.zeidon.standardoe.TaskImpl, java.util.List)
     */
    @Override
    public void init( Task task, List<? extends View> list, CommitOptions options )
    {
        this.task = task;
        this.viewList = new ArrayList<ViewImpl>();
        for ( View v : list )
            viewList.add( ((InternalView) v).getViewImpl() );
        this.options = options;
        // Grab the first view to use as a task qualifier and for getting a dbhandler.
        // TODO: We're using the first view in the list but maybe we should do something different?
        //       We'd like to some day support commits across multiple DBs.
        ViewImpl firstView = viewList.get( 0 );
        JdbcHandlerUtils helper = new JdbcHandlerUtils( this.options, firstView.getViewOd().getDatabase() );
        dbHandler = helper.getDbHandler();
        genkeyHandler = helper.getGenKeyHandler();
    }

    @Override
    public List<ViewImpl> commit( )
    {
        try
        {
            // If there are no views to commit, return.
            if ( viewList.isEmpty() )
                return viewList;

            genKeyObj = task.activateEmptyObjectInstance( "kzgkhwob", task.getSystemTask().getApplication() );
            genKeyObj.cursor( "KZGKHWOB" ).createEntity();

            // Reset flags needed for commit processing.
            for ( ViewImpl view : viewList )
            {
                view.getObjectInstance().dbhNeedsForeignKeys = false;
                view.getObjectInstance().dbhNeedsGenKeys = false;

                for ( EntityInstanceImpl ei : view.getObjectInstance().getEntities() )
                {
                    ei.dbhCreated = false;
                    ei.dbhDeleted = false;
                    ei.dbhExcluded = false;
                    ei.dbhIncluded = false;
                    ei.dbhUpdated = false;
                    ei.dbhNeedsInclude = false;
                    ei.dbhSeqUpdated = false;
                    ei.dbhGenKeyNeeded = false;
                    ei.dbhNoGenKey = false;
                    ei.dbhForeignKey = false;
                }
            }

            // Initialize the genkey handler and create the genkeys.  This will also
            // determine if any EI's need to have foreign keys set.
            initializeGenKeys();
            setForeignKeys();

            /**
             * Determines if we should commit or rollback the current transaction.
             */
            boolean commit = false;

            dbHandler.beginTransaction();

            try
            {
                // Commit each view separately.
                for ( ViewImpl view : viewList )
                {
                    if ( ! view.getObjectInstance().isUpdated() )
                        continue;

                    commitView( view.newView() );
                }
                commit = true;
            }
            catch ( Exception e )
            {
                ZeidonException ze = ZeidonException.wrapException( e );
                throw ze;
            }
            finally
            {
                dbHandler.endTransaction( commit );
            }

            // If we get here then everything worked!  Turn off update flags for all the
            // entities/attributes and remove deleted/excluded entities from the OI chain.
            for ( ViewImpl view : viewList )
                cleanupOI( view.getObjectInstance() );

            // Drop any pessimistic locks we might have.
            for ( ViewImpl view : viewList )
                view.dropDbLocks();

            return viewList;
        }
        finally
        {
        }
    }


    /**
     * Called after a commit. Reset the entity/attribute update flags to false,
     * remove deleted entities from the OI.
     * @param oi
     */
    private void cleanupOI(ObjectInstance oi)
    {
        // We can't use an iterator because we are potentially removing entities.
        for ( EntityInstanceImpl ei = oi.getRootEntityInstance();
              ei != null;
              ei = ei.getNextHier() )
        {
            for ( EntityInstanceImpl linked : ei.getAllLinkedInstances() )
            {
                if ( linked.dbhDeleted || linked.dbhExcluded )
                {
                    linked.dropEntity();  // Remove the ei from the chain.
                    continue;
                }

                if ( linked.dbhCreated )
                    linked.setCreated( false );
                if ( linked.dbhIncluded )
                    linked.setIncluded( false );
                if ( linked.dbhUpdated )
                    linked.setUpdated( false );

                ViewEntity viewEntity = linked.getViewEntity();

                // For created/included entities we need to indicate that they don't have
                // any children that need to be lazy-loaded.
                if ( linked.dbhCreated || linked.dbhIncluded )
                {
                    LazyLoadConfig config = viewEntity.getLazyLoadConfig();
                    if ( config.hasLazyLoadChild() )
                    {
                        Set<ViewEntity> lazySet = linked.getEntitiesLoadedLazily();
                        for ( ViewEntity child : viewEntity.getChildren() )
                        {
                            if ( child.getLazyLoadConfig().isLazyLoad() )
                                lazySet.add( child );
                        }
                    }
                }

                for ( ViewAttribute viewAttrib : viewEntity.getAttributes() )
                {
                    if ( viewAttrib.isPersistent() )
                        linked.getInternalAttribute( viewAttrib ).setUpdated( false );
                }
            }
        }
    }

    private void commitView(ViewImpl view)
    {
        ObjectInstance oi = view.getObjectInstance();

        // TODO: implement optimistic locking check.

        // Keep track of the last ei in the OI while setting autoseq fields.
        EntityInstanceImpl lastEntityInstance = setAutoSeq( oi );

        commitExcludes( view, oi, lastEntityInstance );
        commitDeletes( view, oi, lastEntityInstance );
        commitCreates( view, oi );
        commitIncludes( view, oi );
        commitUpdates( view, oi );
    }

    private void commitExcludes(View view, ObjectInstance oi, EntityInstanceImpl lastEntityInstance)
    {
        // Start from the back so that we don't exclude the parent of another exclude.
        for ( EntityInstanceImpl ei = lastEntityInstance;
              ei != null;
              ei = ei.getPrevHier() )
        {
            ViewEntity viewEntity = ei.getViewEntity();

            // EIs down a derived path don't get committed to the database.
            // Since all children of a derived EI are also derived we can skip
            // the twins of the current EI.
            if ( viewEntity.isDerivedPath() )
            {
                while ( ei.getPrevTwin() != null )
                    ei = ei.getPrevTwin();

                continue;
            }

            // If preprocessing didn't think this EI needed to be commmited, skip it.
            if ( ! ei.dbhNeedsCommit )
                continue;

            if ( !ei.isExcluded() )
                continue;

            // Skip it if the entity was already excluded via a linked instance.
            if ( ei.dbhExcluded )
                continue;

            view.cursor( viewEntity ).setCursor( ei );
            dbHandler.deleteRelationship( view, ei );
            markDuplicateRelationships( ei );
        }
    }

    private void commitDeletes(ViewImpl view, ObjectInstance oi, EntityInstanceImpl lastEntityInstance)
    {
        for ( EntityInstanceImpl ei = lastEntityInstance;
              ei != null;
              ei = ei.getPrevHier() )
        {
            ViewEntity viewEntity = ei.getViewEntity();

            // EIs down a derived path don't get committed to the database.
            // Since all children of a derived EI are also derived we can skip
            // the twins of the current EI.
            if ( viewEntity.isDerivedPath() )
            {
                while ( ei.getPrevTwin() != null )
                    ei = ei.getPrevTwin();

                continue;
            }

            // If preprocessing didn't think this EI needed to be commmited, skip it.
            if ( ! ei.dbhNeedsCommit )
                continue;

            // Can't exclude an entity that wasn't deleted...
            if ( !ei.isDeleted() )
                continue;

            // If the EI was also created then there's nothing to persist.
            if ( ei.isCreated() )
                continue;

            // Skip it if the entity was already deleted via a linked instance.
            if ( ei.dbhDeleted )
                continue;

            // TODO: add code to delete all children?

            view.cursor( viewEntity ).setCursor( ei );
            dbHandler.deleteEntity( view, ei );

            // Flag all linked entities (including 'ei') as having been deleted.
            for ( EntityInstanceImpl linked : ei.getAllLinkedInstances() )
                linked.dbhDeleted = true;
        }
    }

    /**
     * Determines if ei needs to be created.
     *
     * @param ei
     * @return
     */
    private boolean requiresCreate( EntityInstanceImpl ei )
    {
        // If preprocessing didn't think this EI needed to be commmited, skip it.
        if ( ! ei.dbhNeedsCommit )
            return false;

        // Skip deleted entities; they've been created then deleted so no need to save them.
        if ( ei.isDeleted() )
            return false;

        // Can't create an entity that wasn't created...
        if ( !ei.isCreated() )
            return false;

        // Skip it if the entity was already created via a linked instance.
        if ( ei.dbhCreated )
            return false;

        return true;
    }

    private void commitCreates(ViewImpl view, ObjectInstance oi)
    {
        for ( EntityInstanceImpl ei : oi.getEntities() )
        {
            if ( ! requiresCreate( ei ) )
                continue;

            // EI needs to be created.  Let's check all of its twins to see
            // if we can create them all at once.  We can't (easily) create all
            // the instances because we have to deal with linked instances.
            List<EntityInstanceImpl> instances = new ArrayList<EntityInstanceImpl>();
            instances.add( ei );
            for ( EntityInstanceImpl twin = ei.getNextTwin(); twin != null; twin = twin.getNextTwin() )
            {
                if ( requiresCreate( twin ) )
                    instances.add( twin );
            }

            try
            {
                dbHandler.insertEntity( view, instances );
            }
            catch ( Exception e )
            {
                throw ZeidonException.wrapException( e )
                                     .prependEntityInstance( ei );
            }

            // Flag all linked entities (including 'ei') as having been created.
            for ( EntityInstanceImpl twin : instances )
            {
                for ( EntityInstanceImpl linked : twin.getAllLinkedInstances() )
                {
                    linked.dbhCreated = true;

                    // If the linked instance is flagged as created then we need
                    // to set its included flag on so that the *relationship*
                    // is still created.
                    if ( linked.isCreated() )
                        linked.dbhNeedsInclude = true;
                }
            }
        }
    }

    private void commitIncludes(ViewImpl view, ObjectInstance oi)
    {
        for ( EntityInstanceImpl ei : oi.getEntities() )
        {
            // If preprocessing didn't think this EI needed to be commmited, skip it.
            if ( ! ei.dbhNeedsCommit )
                continue;

            // Donn't include an entity that wasn't included...
            if ( ! ei.isIncluded() && ! ei.dbhNeedsInclude )
                continue;

            ViewEntity viewEntity = ei.getViewEntity();

            // Skip it if the entity was already included via a linked instance.
            if ( ei.dbhIncluded )
                continue;

            ei.dbhIncluded = true;

            // Nothing to include if this is the root.
            if ( ei.getParent() == null )
                continue;

            view.cursor( viewEntity ).setCursor( ei );
            dbHandler.insertRelationship( view, ei );
            markDuplicateRelationships( ei );
        }
    }

    private void commitUpdates(View view, ObjectInstance oi)
    {
        for ( EntityInstanceImpl ei : oi.getEntities() )
        {
            ViewEntity viewEntity = ei.getViewEntity();

            // If preprocessing didn't think this EI needed to be commmited, skip it.
            if ( ! ei.dbhNeedsCommit )
                continue;

            // Can't update an entity that wasn't updated...
            if ( !ei.isUpdated() )
                continue;

            // Skip it if the entity was already updated via a linked instance.
            if ( ei.dbhUpdated )
                continue;

            // Skip if the entity was created or deleted.
            if ( ei.dbhCreated || ei.dbhDeleted )
                continue;

            view.cursor( viewEntity ).setCursor( ei );
            dbHandler.updateEntity( view, ei );

            // Flag all linked entities (including 'ei') as having been updated.
            for ( EntityInstanceImpl linked : ei.getAllLinkedInstances() )
                linked.dbhUpdated = true;
        }
    }

    /**
      This function is called after an EI has been included into the DB.  This
      function sets the bDBHIncluded/bDBHExcluded flag for all linked EIs in the
      same OI that have the same relationship.
     */
    private void markDuplicateRelationships(EntityInstanceImpl ei)
    {
        EntityInstanceImpl parent = ei.getParent();
        ViewEntity         viewEntity = ei.getViewEntity();

        // Duplicate relationship searching phase I, see if a linked instance to
        // the target instance in the same object instance represents the
        // same relationship type AND has the same parent.
        for ( EntityInstanceImpl linked : ei.getLinkedInstances() )
        {
            // Check to make sure linked EI has a parent--it is possible for a root
            // to be flagged as included and we don't care about roots.
            if ( linked.isDeleted() || linked.getParent() == null )
                continue;

            if ( ei.isExcluded() )
            {
                if ( ! linked.isExcluded() )
                    continue;
            }
            else
            {
                if ( ! linked.isIncluded() )
                    continue;
            }

            ViewEntity linkedViewEntity = linked.getViewEntity();

            // Linked EI must have the same relationship and it can't be derived.
            if ( linkedViewEntity.getErRelToken() == viewEntity.getErRelToken() ||
                 linkedViewEntity.isDerivedPath() )
            {
                continue;
            }

            // Now check to see if the parents are linked.
            EntityInstanceImpl linkParent = linked.getParent();
            for ( EntityInstanceImpl parentLinked : linkParent.getLinkedInstances() )
            {
                if ( parentLinked == parent )
                {
                    if ( ei.isExcluded() )
                        linked.dbhExcluded = true;
                    else
                        linked.dbhIncluded = true;

                    break;
                }
            }
        } // for each linked instance...

        // Duplicate relationship searching, phase II, see if the parent of
        // the instance has a linked instance representing the same relationship
        // type which is also a child of one of the targets linked instances.

        // If the parent isn't linked then there are no duplicate relationships.
        if ( parent.getLinkedInstances().size() == 0 )
            return;

        for ( EntityInstanceImpl linked : parent.getLinkedInstances() )
        {
            // Check for appropriate include/exclude flag.
            if ( ei.isExcluded() )
            {
                if ( ! linked.isExcluded() )
                    continue;
            }
            else
            {
                if ( ! linked.isIncluded() )
                    continue;
            }

            ViewEntity linkedViewEntity = linked.getViewEntity();

            // Check to see if the relationship for the EI linked to the parent is
            // the same as the relationship of the original EI.
            if ( linkedViewEntity.getErRelToken() != viewEntity.getErRelToken() )
                continue; // Nope.

            // OK, we have an EI ('linked') that has the same relationship as
            // ei.  Check to see if the parent of 'linked' (grandParent)
            // is linked with ei.  If they are linked then 'linked'
            // has the same physical relationship as ei.
            EntityInstanceImpl grandParent = linked.getParent();
            for ( EntityInstanceImpl gp : ei.getLinkedInstances() )
            {
                if ( gp == grandParent )
                {
                    if ( ei.isExcluded() )
                        linked.dbhExcluded = true;
                    else
                        linked.dbhIncluded = true;

                    break;
                }
            }
        } // for each linked instance of parent...
    } // markDuplicateRelationships()

    /**
     *
     * @param view
     * @return the last EI in the OI.
     */
    private EntityInstanceImpl setAutoSeq( final ObjectInstance oi  )
    {
        EntityInstanceImpl lastEntityInstance = null;

        // Set any autoseq attributes and find the last EI in the OI.
        for ( final EntityInstanceImpl ei : oi.getEntities( true ) )
        {
            lastEntityInstance = ei;

            final ViewEntity viewEntity = ei.getViewEntity();
            if ( viewEntity.isDerivedPath() )
                continue;

            final ViewAttribute autoSeq = viewEntity.getAutoSeq();
            if ( autoSeq != null && ei.getPrevTwin() == null && // Must be first twin
                                    ei.getNextTwin() != null )  // Don't bother if only one twin.
            {
                int seq = 1;
                for ( EntityInstanceImpl twin = ei; twin != null; twin = twin.getNextTwin() )
                {
                    if ( twin.isHidden() )
                        continue;

                    twin.setInternalAttributeValue( autoSeq, seq++, true );

                    // Turn off the bDBHUpdated flag (if it's on) so that we
                    // make sure the entity is updated.  If the entity instance
                    // is linked with someone else it's possible that the
                    // entity was updated through the other link.
                    twin.dbhUpdated = false;
                }
            }
        }

        return lastEntityInstance;
    }

    private void setForeignKeys()
    {
        // Copy FKs.  We have to loop possibly many times to set the FKs.  It's
        // possible that the source for a FK is a FK from yet another EI.  We don't
        // want to copy a FK until we know that the source for a FK has been
        // properly set.  We also want to make sure we set the FK's for the EIs that
        // have been excluded/deleted before we copy FKs for the included/created.
        boolean hiddenOnly = true;  // We'll start by setting FKs for hidden EIs only.
        int     debugCnt   = 0;     // We'll keep a counter in case we get an infinite loop.
        boolean settingFKs = true;
        while ( settingFKs )
        {
            // We'll hope that we're done setting FKs after this iteration.  If we
            // find we need to set more FKs then we'll turn it back on.
            settingFKs = false;

            if ( debugCnt++ > 100 )
                throw new ZeidonException("Internal error: too many FKs");

            for ( ViewImpl view : viewList )
            {
                ObjectInstance oi = view.getObjectInstance();
                if ( ! oi.dbhNeedsForeignKeys )
                    continue;

                // Let's hope that this OI doesn't need FKs set after this
                // iteration.  If it does then we'll turn the flag back on.
                oi.dbhNeedsForeignKeys = false;

                for ( final EntityInstanceImpl ei : oi.getEntities() )
                {
                    if ( ! ei.dbhForeignKey )
                        continue;

                    final ViewEntity viewEntity = ei.getViewEntity();

                    assert ! viewEntity.isDerivedPath();

                    // If the EI is not hidden and we're setting FKs for hidden EIs
                    // only then set flags for another try.
                    if ( ! ei.isHidden() && hiddenOnly )
                    {
                        settingFKs = true;
                        oi.dbhNeedsForeignKeys = true;
                        continue;
                    }

                    if ( setForeignKeysForInstance( ei ) )
                    {
                        settingFKs = true;
                        oi.dbhNeedsForeignKeys = true;
                    }
                }
            }

            // After one iteration we can set non-hidden FKs.
            hiddenOnly = false;

        } // while settingFKs...
    }

    /**
    // Copies all foreign keys to/from lpEntityInstance.
    //
    // Returns false if no more FKs need to be set.
    //         true  if the FK couldn't be set because the source hasn't been set.
    //
    **/
    private boolean setForeignKeysForInstance( final EntityInstanceImpl ei )
    {
        ei.dbhForeignKey = false;

        final ViewEntity viewEntity = ei.getViewEntity();
        if ( viewEntity.getParent() == null )
            return false;

        final DataRecord dataRecord = viewEntity.getDataRecord();
        final RelRecord relRecord = dataRecord.getRelRecord();
        if ( relRecord == null )
            return false;

        for ( final RelField relField : relRecord.getRelFields() )
        {
            // If there is not rel data field then there's nothing to set.
            if ( relField.getRelDataField() == null )
                continue;

            final ViewAttribute srcViewAttrib = relField.getSrcDataField().getViewAttribute();
            final ViewEntity    srcViewEntity = srcViewAttrib.getViewEntity();
            final ViewAttribute relViewAttrib = relField.getRelDataField().getViewAttribute();
            final ViewEntity    relViewEntity = relViewAttrib.getViewEntity();

            // We now have the attributes--the source and relationship (i.e. target)
            // attributes.  One is part of the current entity (lpViewEntity) and
            // the other is a parent of the current entity.  Find the entity
            // instance of the parent entity.

            final EntityInstanceImpl relInstance;
            final EntityInstanceImpl srcInstance;
            if ( relViewEntity == viewEntity )
            {
                relInstance = ei;
                srcInstance = ei.findMatchingParent( srcViewEntity );
            }
            else
            {
                srcInstance = ei;
                relInstance = ei.findMatchingParent( relViewEntity );
            }

            // If the instance we are about to update with FKs is being deleted then
            // don't worry about setting the key because it's unnecessary.
            if ( relInstance.isDeleted() )
                continue;

            if ( ei.isCreated() || ei.isIncluded() )
            {
                // If the source instance hasn't had *IT'S* FKs set then skip this
                // one and try again later.
                if ( srcInstance.dbhForeignKey )
                {
                    // Turn the flag back on for this instance.
                    ei.dbhForeignKey = true;
                    return true;
                }

                relInstance.setInternalAttributeValue( relViewAttrib, srcInstance.getInternalAttribute( srcViewAttrib ).getInternalValue(), true );
            }
            else
            {
                // If the foreign key is a key to the target entity, then
                // we cannot null the key because we would lose the
                // capability of updating the entity (in this case it
                // better be deleted!!!)
                assert ! relViewAttrib.isKey();
                assert ei.isExcluded(); // EI is hidden and we've ignored deleted EIs above.

                // If the EI is excluded and the min cardinality is 1, we'll get an error
                // if we set the FK to null.  We will assume that a different entity is being
                // included which will set the FK to a non-null value.  We can assume this because
                // the OI has passed cardinality validation and if no EI was being included it
                // would have thrown a validation exception.
                if ( viewEntity.getMinCardinality() == 0)
                    relInstance.setInternalAttributeValue( relViewAttrib, null, true );
            }

            // Turn off the dbh flag to make sure that the DBHandler updates
            // the instance.
            relInstance.dbhUpdated = false;

        } // for each rel field...

        return false;
    }

    private void initializeGenKeys()
    {
        ViewEntity currentGenKeyEntity = null;
        final EntityCursor genKeyCursor = genKeyObj.cursor( "Genkey" );

        for ( final ViewImpl view : viewList )
        {
            final ViewOd viewOd = view.getViewOd();

            // Skip this if the view OD doesn't have genkeys.
            // TODO: What about foreign keys?  They have to be processed?
//            if ( ! viewOd.hasGenKey() )
//                continue;

            // TODO: Get genkey handler from ViewOd.

            final ObjectInstance oi = view.getObjectInstance();

            // Go through all the EIs and look for entities that need genkeys or FKs.
            for ( final EntityInstanceImpl ei : oi.getEntities() )
            {
                final ViewEntity viewEntity = ei.getViewEntity();
                if ( viewEntity.isDerivedPath() )
                    continue;

                // Check to see if the EI needs foreign keys.
                if ( ei.isCreated() && viewEntity.isCreate() )
                {
                    ei.dbhForeignKey = true;
                    oi.dbhNeedsForeignKeys = true;
                }
                else
                if ( ei.isIncluded() && viewEntity.isInclude() &&  ei.getParent() != null &&
                     ( ! ei.isCreated() || ! viewEntity.isCreate() ) )
                {
                    ei.dbhForeignKey = true;
                    oi.dbhNeedsForeignKeys = true;
                }
                else
                if ( ei.isExcluded() && viewEntity.isExclude() )
                {
                    ei.dbhForeignKey = true;
                    oi.dbhNeedsForeignKeys = true;
                }
                else
                if (ei.isDeleted() && viewEntity.isDelete() )
                {
                    ei.dbhForeignKey = true;
                    oi.dbhNeedsForeignKeys = true;
                }
                else
                    continue;  // Nothing was done to this entity so skip it.

                // We've determined if an entity needs FKs.  Now check for Genkeys.

                final ViewAttribute genKey = viewEntity.getGenKey();
                if ( genKey == null )
                    continue;

                // If NoGenKey flag is set then we already created a genkey through
                // another linked instance.
                if ( ei.dbhNoGenKey )
                    continue;

                // We only create genkeys for entities that have create authority.
                if ( ! viewEntity.isCreate() )
                    continue;

                if ( ! ei.getInternalAttribute( genKey ).isNull(task, genKey) )
                    continue;

                // genKeyObj is a work object that contains an entity for each
                // entity instance in the OI being committed and the number of
                // entity instances that need genkeys.
                //
                // If the viewEntity of the current entity instance is the same
                // as the previous entity instance, then increment the count in
                // genKeyObj by 1.  If they are different, then we need to find
                // the entity instance in genKeyObj that corresponds with the
                // current lpEntityInstance.  If one is not found, then it needs
                // to be created.
                if ( viewEntity != currentGenKeyEntity )
                {
                    if ( ! genKeyCursor.setFirst( "EntityID", viewEntity.getErEntityToken() ).isSet() )
                    {
                        DataRecord dataRecord = viewEntity.getDataRecord();
                        genKeyCursor.createEntity().setAttribute( "EntityID", viewEntity.getErEntityToken() )
                                                   .setAttribute( "EntityCount", 0 )
                                                   .setAttribute( "TableName", dataRecord.getRecordName() )
                                                   .setAttribute( "EntityName", viewEntity.getName() );
                    }
                    currentGenKeyEntity = viewEntity;
                }

                Integer count = genKeyCursor.getIntegerFromAttribute( "EntityCount" );
                count = ( count == null ) ? 1 : count + 1;
                genKeyCursor.setAttribute( "EntityCount", count );
                oi.dbhNeedsGenKeys = true;
                ei.dbhGenKeyNeeded = true;

                // We've created a genkey for ei; loop through linked instances and set a flag
                // indicating that they don't need a genkey.
                for ( final EntityInstanceImpl linked : ei.getLinkedInstances() )
                {
                    linked.dbhNoGenKey = true;
                }
            } // for each entity instance...
        } // for each view...

        if ( currentGenKeyEntity == null )
            return;

        // If/when the day comes that we support different genkey handlers we'll probably need to
        // rewrite this section.  The current genkey handler returns a map of the new genkey values.
        // The handler doesn't attempt to set the genkey attributes because it doesn't have access
        // to the dbh... flags in EntityInstanceImpl.
        //
        //TODO: genkey flag is now available via method on cursor.  Should we change this logic?
        final Map<Integer, Integer> genkeys = genkeyHandler.setGenKeys( genKeyObj, viewList );
        for ( final ViewImpl view : viewList )
        {
            if ( ! view.getObjectInstance().dbhNeedsGenKeys )
                continue;

            for ( final EntityInstanceImpl ei : view.getObjectInstance().getEntities() )
            {
                if ( ! ei.dbhGenKeyNeeded )
                    continue;

                final ViewEntity viewEntity = ei.getViewEntity();
                final ViewAttribute genkeyAttr = viewEntity.getGenKey();
                final Integer genkey = genkeys.get( viewEntity.getErEntityToken() );
                assert genkey != null;

                ei.setInternalAttributeValue( genkeyAttr, genkey, true );

                // Increment the genkey value in the genkey map.
                genkeys.put( viewEntity.getErEntityToken(), genkey + 1 );
            }
        }
    } // initializeGenKeys()
}
