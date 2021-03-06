/**
    This file is part of the Zeidon Java Object Engine (Zeidon JOE).

    Zeidon JOE is free software: you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Zeidon JOE is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with Zeidon JOE.  If not, see <http://www.gnu.org/licenses/>.

    Copyright 2009-2012 QuinSoft
 */

package com.quinsoft.zeidon.utils;

import org.apache.commons.lang3.StringUtils;

import com.quinsoft.zeidon.ActivateFlags;
import com.quinsoft.zeidon.ActivateOptions;
import com.quinsoft.zeidon.Application;
import com.quinsoft.zeidon.CursorResult;
import com.quinsoft.zeidon.EntityCursor;
import com.quinsoft.zeidon.EntityInstance;
import com.quinsoft.zeidon.Task;
import com.quinsoft.zeidon.TaskQualification;
import com.quinsoft.zeidon.View;
import com.quinsoft.zeidon.ZeidonException;
import com.quinsoft.zeidon.dbhandler.DbHandler;
import com.quinsoft.zeidon.objectdefinition.LockingLevel;
import com.quinsoft.zeidon.objectdefinition.ViewAttribute;
import com.quinsoft.zeidon.objectdefinition.ViewEntity;
import com.quinsoft.zeidon.objectdefinition.ViewOd;

/**
 * A builder for creating and manipulating Qualification objects.
 *
 * @author DG
 *
 */
public class QualificationBuilder
{
    public static final String QUAL_XOD_NAME = "kzdbhqua";
    public static final String ENTITYSPEC    = "EntitySpec";
    public static final String ENTITYNAME    = "EntityName";
    public static final String QUALATTRIB    = "QualAttrib";
    public static final String ATTRIBUTENAME = "AttributeName";
    public static final String OPER          = "Oper";
    public static final String VALUE         = "Value";
    public static final String KEYLIST       = "KeyList";

    private final Task        task;
    private       Task        cacheTask;
    private       Application application;
    private       View        qualView;
    private       int         entitySpecCount;
    private       String      cacheViewName;
    private final ActivateOptions activateOptions;

    /**
     * When doing the activate we will synchronize using this object.  May be changed
     * by cachedAs().  Defaults to the QualificationBuilder.
     */
    private       Object      synch;

    /**
     * If non-null, then attempt to relink this instance with the root after the activate.
     */
    private EntityInstance sourceEntityInstance;

    /**
     * Creates an empty qualification object.
     * @param task
     */
    public QualificationBuilder( TaskQualification taskQual )
    {
        this.task = taskQual.getTask();
        cacheTask = task;
        application = task.getApplication();
        qualView = task.activateEmptyObjectInstance( QUAL_XOD_NAME, task.getSystemTask().getApplication() );
        activateOptions = new ActivateOptions( taskQual.getTask() );
        activateOptions.setQualificationObject( qualView );
        entitySpecCount = 0;
        synch = this;
    }

    /**
     * @deprecated  Use QualificationBuilder( task ).setViewOd(...) instead.
     */
    @Deprecated
    public QualificationBuilder( TaskQualification taskQual, String viewOdName )
    {
        this( taskQual );
        setViewOd( viewOdName );
    }

    public QualificationBuilder setApplication( Application application )
    {
        this.application = application;
        return this;
    }

    public QualificationBuilder setApplication( String application )
    {
        this.application = task.getApplication( application );
        return this;
    }

    public QualificationBuilder setViewOd( ViewOd viewOd )
    {
        activateOptions.setViewOd( viewOd );
        return this;
    }

    public QualificationBuilder setViewOd( String viewOdName )
    {
        return setViewOd( application.getViewOd( task, viewOdName ) );
    }

    public QualificationBuilder setViewOd( String applicationName, String viewOdName )
    {
        setApplication( applicationName );
        return setViewOd( application.getViewOd( task, viewOdName ) );
    }

    public ViewOd getViewOd()
    {
        return activateOptions.getViewOd();
    }

    public QualificationBuilder loadFile( String filename )
    {
        ViewOd qualViewOd = task.getSystemTask().getApplication().getViewOd( task, QUAL_XOD_NAME );
        qualView = task.activateOiFromFile( qualViewOd, filename, null );
        return this;
    }

    public QualificationBuilder singleRoot()
    {
        return setFlag( ActivateFlags.fSINGLE );
    }

    public QualificationBuilder rootOnly()
    {
        return setFlag( ActivateFlags.fROOT_ONLY );
    }

    public QualificationBuilder readOnly()
    {
        return setFlag( ActivateFlags.fREAD_ONLY );
    }

    public QualificationBuilder multipleRoots()
    {
        return setFlag( ActivateFlags.fMULTIPLE );
    }

    public QualificationBuilder setAsynchronous()
    {
        return asynchronous();
    }

    public QualificationBuilder asynchronous()
    {
        if ( sourceEntityInstance != null )
            throw new ZeidonException( "The qualification source entity is configured to be relinked after " +
            		                   "the activate is finished.  This is incompatible with asynchronous config." );

        return setFlag( ActivateFlags.fASYNCHRONOUS );
    }

    public QualificationBuilder setFlag( ActivateFlags flag )
    {
        activateOptions.addActivateFlag( flag );
        return this;
    }

    /**
     * Gives this qualification a name.  This name may be used by the underlying dbhandler
     * for performing the activate.
     *
     * @param qualName
     * @return
     */
    public QualificationBuilder setQualificationName( String qualName )
    {
        activateOptions.setQualificationName( qualName );
        return this;
    }

    public QualificationBuilder setOiServerUrl( String url )
    {
        activateOptions.setOiServerUrl( url );
        return this;
    }

    /**
     * Cache this object in the System task using viewName to name the view.
     *
     * @param viewName
     * @return
     */
    public QualificationBuilder cachedAs( String viewName )
    {
        return cachedAs( viewName, task.getSystemTask(), viewName );
    }

    /**
     * Cache this object in the specified task using viewName to name the view.
     *
     * @param viewName
     * @param taskQual
     * @return
     */
    public QualificationBuilder cachedAs( String viewName, TaskQualification taskQual )
    {
        return cachedAs( viewName, taskQual.getTask(), viewName ); // We'll default sync object to view name.
    }

    public QualificationBuilder cachedAs( String viewName, TaskQualification taskQual, Object syncObject )
    {
        this.cacheViewName = viewName;
        this.synch = syncObject;
        cacheTask = taskQual.getTask();
        return this;
    }

    /**
     * @deprecated  Use forEntity() instead.
     */
    @Deprecated
    public QualificationBuilder entitySpec( String entityName )
    {
        return forEntity( entityName );
    }

    /**
     * Adds qualification to a non-root entity.  Mirrors the RESTRICTING clause
     * in VML.  Synonym for forEntity().
     *
     * @param entityName
     * @return
     */
    public QualificationBuilder restricting( String entityName )
    {
        return forEntity( entityName );
    }

    public QualificationBuilder forEntity( String entityName )
    {
        if ( getViewOd() == null )
            throw new ZeidonException( "Must specify ViewOD before setting qualification" );

        if ( qualView.cursor( ENTITYSPEC ).setFirst( "EntityName", entityName ) != CursorResult.SET )
        {
            entitySpecCount++;
            qualView.cursor( ENTITYSPEC ).createEntity().setAttribute( "EntityName", entityName );
        }

        return this;
    }

    private void validateEntity()
    {
        // If we haven't created an entity spec for this qual OI then we'll automatically create
        // one for the root entity.
        if ( entitySpecCount == 0 )
            forEntity( getViewOd().getRoot().getName() );

    }

    /**
     * @deprecated  Use addAttribQual() instead.
     */
    @Deprecated
    public QualificationBuilder newAttribSpec( String oper )
    {
        return addAttribQual( oper );
    }

    public QualificationBuilder addAttribQual( String oper )
    {
        validateEntity();
        qualView.cursor( QUALATTRIB ).createEntity().setAttribute( OPER, oper );
        return this;
    }

    public QualificationBuilder addAttribQual( String attribName, Object attribValue )
    {
        validateEntity();
        return addAttribQual( qualView.cursor( ENTITYSPEC ).getStringFromAttribute( ENTITYNAME ), attribName, "=", attribValue );
    }

    @Deprecated // use addAttribQual instead.
    public QualificationBuilder newAttribSpec( String attribName, String attribValue )
    {
        return addAttribQual( qualView.cursor( ENTITYSPEC ).getStringFromAttribute( ENTITYNAME ), attribName, "=", attribValue );
    }

    /**
     * @deprecated  Use forEntity() instead.
     */
    @Deprecated
    public QualificationBuilder newAttribSpec( String attribName, String oper, String attribValue )
    {
        return addAttribQual( qualView.cursor( ENTITYSPEC ).getStringFromAttribute( ENTITYNAME ), attribName, oper, attribValue );
    }

    public QualificationBuilder newEntityKey( Integer key )
    {
        qualView.cursor( KEYLIST ).createEntity()
                                  .setAttribute( "IntegerValue", key );
        return this;
    }

    /**
     * @deprecated  Use addAttribQual() instead.
     */
    @Deprecated
    public QualificationBuilder newAttribSpec( String entityName, String attribName, String oper, String attribValue )
    {
        return addAttribQual( entityName, attribName, oper, attribValue );
    }

    public QualificationBuilder addAttribQual( String entityName, String attribName, String oper, Object attribValue )
    {
        validateEntity();
        qualView.cursor( QUALATTRIB ).createEntity()
                                     .setAttribute( ENTITYNAME, entityName )
                                     .setAttribute( ATTRIBUTENAME, attribName )
                                     .setAttribute( OPER, oper )
                                     .setAttribute( VALUE, attribValue == null ? null : attribValue.toString() );

        return this;
    }

    /**
     * Adds qualification to activate the source entity instance.
     *
     * @param source
     * @return
     */
    public QualificationBuilder fromEntityKeys( EntityInstance source )
    {
        return fromEntityKeys( source, true );
    }

    /**
     * Use the keys from 'source' to qualify the entity.
     *
     * @param source
     * @param linkEntities if true and the target entity is the root then the entities will
     * be relinked after the activity.
     *
     * @return
     */
    public QualificationBuilder fromEntityKeys( EntityInstance source, boolean linkEntities )
    {
        ViewEntity viewEntity = source.getViewEntity();

        if ( viewEntity.getKeys().size() > 1 )
            addAttribQual( "(" );

        boolean firstKey = true;
        for ( ViewAttribute key : viewEntity.getKeys() )
        {
            if ( firstKey )
                firstKey = false;
            else
                addAttribQual( " AND " );

            addAttribQual( key.getName(), source.getStringFromAttribute( key ) );
        }

        if ( viewEntity.getKeys().size() > 1 )
            addAttribQual( ")" );

        if ( linkEntities == true )
        {
            if ( activateOptions.getActivateFlags().contains( ActivateFlags.fASYNCHRONOUS ) )
                throw new ZeidonException( "This activate is configured to be asynchronous.  This is incompatable " +
                		                   "with a linked source instance.  Use fromEntityKeys(..., false)." );

            sourceEntityInstance = source;
        }

        return this;
    }

    /**
     * Activate using the specified locking level.
     *
     * @param lockingLevel
     * @return
     */
    public QualificationBuilder withLocking( LockingLevel lockingLevel )
    {
        activateOptions.setLockingLevel( lockingLevel );
        return this;
    }

    /**
     * Activate with the default locking level as specified in the LOD.
     *
     * @return
     */
    public QualificationBuilder withLocking()
    {
        return withLocking( getViewOd().getLockingLevel() );
    }

    public View getView()
    {
        return qualView;
    }

    public View activate()
    {
        View view = null;

        // In case we're loading a cached object, synchronize using 'synch'.  If we're not
        // loading a cached object then sync = this so it's basically a no-op.  If we're loading
        // a cached object then synch is required to be an object that will prevent multiple
        // threads from loading the same cached object at the same time.  Usually the viewName
        // is good enough.
        synchronized ( synch )
        {
            // Are we dealing with a cached view?
            if ( StringUtils.isBlank( cacheViewName ) )
                // No.  Make sure cacheTask and task are the same.
                assert cacheTask == task : "Unequal tasks for non-cached activate";
            else
                // Yes.  Attempt to get the view by name in the cacheTask.
                view = cacheTask.getViewByName( cacheViewName );

            if ( view == null )
            {
                view = cacheTask.activateObjectInstance( activateOptions );

                // Are we dealing with a cached view?
                if ( ! StringUtils.isBlank( cacheViewName ) )
                    // Yes.  Name the view for later.
                    cacheTask.setNameForView( cacheViewName, view );
            }

            // If the source instance is specified and is the same type as the root, then relink.
            if ( sourceEntityInstance != null )
            {
                ViewEntity root = getViewOd().getRoot();
                if ( root == sourceEntityInstance.getViewEntity() && view.cursor( root ).getEntityCount() == 1 )
                    view.cursor( root ).linkInstances( sourceEntityInstance );
            }

            return view;
        }
    }

    /**
     * @deprecated  Use setViewOd(...).activate() instead.
     */
    @Deprecated
    public View activate( String viewOdName )
    {
        setViewOd( viewOdName );
        return activate();
    }

    public static View generateQualFromSingleRootValue( TaskQualification task,
                                                        String            viewOdName,
                                                        String            attributeName,
                                                        Object            value )
    {
        Application app = task.getApplication();
        ViewOd viewOd = app.getViewOd( task, viewOdName );

        QualificationBuilder qual = new QualificationBuilder( task ).setViewOd( viewOdName );
        qual.forEntity( viewOd.getRoot().getName() ).addAttribQual( attributeName, value.toString() );

        return qual.getView();
    }

    public static View generateQualFromEntityList( View sourceView, String entityName, String scopingEntity )
    {
        EntityCursor cursor = sourceView.cursor( entityName );
        ViewEntity viewEntity = cursor.getViewEntity();

        // Find the key.
        ViewAttribute key = null;
        for ( ViewAttribute attrib : viewEntity.getKeys() )
        {
            key = attrib;
        }

        QualificationBuilder qual = new QualificationBuilder( sourceView )
                                            .setViewOd( sourceView.getViewOd() )
                                            .forEntity( DbHandler.ROOT_ENTITY )
                                            .addAttribQual( key.getName(), null );
        for ( EntityInstance ei : cursor.eachEntity() )
            qual.newEntityKey( ei.getIntegerFromAttribute( key ) );

        return qual.getView();
    }

    public static View activateFromFile( Task task, String filename )
    {
        ViewOd qua = task.getSystemTask().getApplication().getViewOd( task, "kzdbhqua" );
        return task.activateOiFromFile( qua, filename, null );
    }
}