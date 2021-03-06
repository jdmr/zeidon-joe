/**
 * This file is part of the Zeidon Java Object Engine (Zeidon JOE).
 *
 * Zeidon JOE is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * Zeidon JOE is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Zeidon JOE. If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2009-2012 QuinSoft
 */
/**
 *
 */
package com.quinsoft.zeidon.standardoe;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import com.google.common.collect.MapMaker;
import com.quinsoft.zeidon.Task;
import com.quinsoft.zeidon.ZeidonException;
import com.quinsoft.zeidon.objectdefinition.ViewAttribute;
import com.quinsoft.zeidon.objectdefinition.ViewEntity;

/**
 * This is the object that contains all the attribute values for an entity.
 * Linked entity instances will have different AttributeListInstances but the
 * lists will share the same persistentAttributes array.
 *
 * @author DG
 *
 */
class AttributeListInstance
{
    private final ViewEntity             viewEntity;

    /**
     * Map of persistent attributes. Linked entities will reference the same
     * persistentAttributes.
     *      Key = ER Attribute token
     */
    private Map<Long, AttributeValue> persistentAttributes;

    /**
     * Map of work attributes. Every entity, even linked ones, will have their
     * own set of workAttributes.
     *      Key = ER Attribute token
     */
    private Map<Long, AttributeValue> workAttributes;

    /**
     * List of instances linked with this one. This is a set of weak references;
     * when one of the entity instances is dropped the GC will remove it from
     * this list. The boolean value a dummy value required to make this a map.
     */
    private volatile ConcurrentMap<EntityInstanceImpl, Boolean> linkedInstances;

    /**
     * Create or update an attribute list that shares the persistent attributes with
     * the source list and has a its own version of work attributes. This is
     * used to link entity instances.
     *
     * @param targetInstance
     * @param sourceInstance
     * @param source
     * @return
     */
    static AttributeListInstance newSharedAttributeList( EntityInstanceImpl targetInstance,
                                                         AttributeListInstance targetAttributeList,
                                                         EntityInstanceImpl sourceInstance,
                                                         AttributeListInstance source )
    {
        ViewEntity targetViewEntity = targetInstance.getViewEntity();
        assert targetViewEntity.getErEntityToken() == sourceInstance.getViewEntity().getErEntityToken() :
                    "Attempting to link view entities that are not the same ER entity";

        if ( targetAttributeList == null )
        {
            targetAttributeList = new AttributeListInstance( targetInstance );
            targetAttributeList.workAttributes = new HashMap<Long, AttributeValue>( targetViewEntity.getWorkAttributeCount() );
        }

        source.addLinkedInstance( sourceInstance, targetInstance );
        targetAttributeList.persistentAttributes = source.persistentAttributes;
        targetAttributeList.linkedInstances = source.linkedInstances;
        return targetAttributeList;
    }

    /**
     * Updates the persistent shared attr list in target to match source.
     * Expected to be called by accept logic.
     *
     *
     * @param target
     * @param source
     */
    static void updateSharedAttributeList( AttributeListInstance target, AttributeListInstance source )
    {
        target.persistentAttributes = source.persistentAttributes;
    }

    /**
     * Creates a new AttributeListInstance for a temporal entity. The work
     * attributes are copied from prevInstanceAttributeList. Persistent
     * attributes are copied from prevInstanceAttributeList unless
     * linkedAttributeList is not null.
     *
     * If linkedAttributeList is not null then temporalInstance is linked to
     * another instance so we'll just use the linked instance's persistent
     * attribute.
     * @param task TODO
     * @param temporalInstance
     *            - The temporal entity instance.
     * @param sourceInstance
     *            - source EI for the temporal instance.
     * @param prevInstanceAttributeList
     *            - The previous entity instance.
     * @param linkedAttributeList
     *            - If non-null, then the temporal instance will be linked to
     *            this one.
     *
     * @return
     */
    static AttributeListInstance newTemporalAttributeList( TaskImpl task,
                                                           EntityInstanceImpl temporalInstance,
                                                           EntityInstanceImpl sourceInstance,
                                                           AttributeListInstance prevInstanceAttributeList, AttributeListInstance linkedAttributeList )
    {
        ViewEntity viewEntity = temporalInstance.getViewEntity();
        AttributeListInstance newList = new AttributeListInstance( temporalInstance );

        newList.workAttributes = new HashMap<Long, AttributeValue>( viewEntity.getWorkAttributeCount() );

        if ( linkedAttributeList == null )
        {
            newList.persistentAttributes = new HashMap<Long, AttributeValue>( viewEntity.getPersistentAttributeCount() );

            // Copy work and persistent attributes.
            newList.copyAttributes( task, prevInstanceAttributeList, true, true );

            newList.initializeLinkedInstances( temporalInstance );
        }
        else
        {
            // Share the persistent attributes.
            newList.persistentAttributes = linkedAttributeList.persistentAttributes;
            linkedAttributeList.addLinkedInstance( sourceInstance, temporalInstance );
            newList.linkedInstances = linkedAttributeList.linkedInstances;

            // Copy just work attributes.
            newList.copyAttributes( task, prevInstanceAttributeList, false, true );
        }

        return newList;
    }

    static AttributeListInstance newAttributeList( EntityInstanceImpl entityInstance )
    {
        ViewEntity viewEntity = entityInstance.getViewEntity();
        AttributeListInstance newList = new AttributeListInstance( entityInstance );
        newList.workAttributes = new HashMap<Long, AttributeValue>( viewEntity.getWorkAttributeCount() );
        newList.persistentAttributes = new HashMap<Long, AttributeValue>( viewEntity.getPersistentAttributeCount() );
        newList.initializeLinkedInstances( entityInstance );
        return newList;
    }

    private AttributeListInstance(EntityInstanceImpl entityInstance)
    {
        viewEntity = entityInstance.getViewEntity();
    }

    private void initializeLinkedInstances( EntityInstanceImpl first )
    {
        linkedInstances = null;
    }

    private Map<Long, AttributeValue> getInstanceMap( ViewAttribute viewAttribute )
    {
        if ( viewAttribute.isPersistent() )
            return persistentAttributes;

        return workAttributes;
    }

    /**
     * Validate that the entity instance for viewAttribute matches getViewEntity().  Check
     * to see if this is a recursive entity and if so return the
     * @param viewAttribute
     * @return
     */
    private ViewAttribute validateMatchingEntities( ViewAttribute viewAttribute )
    {
        if ( viewAttribute.getViewEntity() == getViewEntity() )
            return viewAttribute;

        // If viewAttribute points to a recursive child, find the parent attribute.
        // TODO: Do we have to do this for the reciprocal situation as well?
        if ( viewAttribute.getViewEntity() == getViewEntity().getRecursiveParentViewEntity() )
        {
            // Find the attribute in getViewEntity() that matches viewAttribute.
            for ( ViewAttribute va : getViewEntity().getAttributes() )
            {
                if ( va.getErAttributeToken().equals( viewAttribute.getErAttributeToken() ) )
                    return va;
            }
        }

        throw new ZeidonException( "Mismatching entities.  ViewAttribEntity: %s, ViewEntity: %s",
                                    viewAttribute.getViewEntity(), getViewEntity() );
    }

    AttributeValue getAttribute( ViewAttribute viewAttribute )
    {
        ViewAttribute va = validateMatchingEntities( viewAttribute );

        Map<Long, AttributeValue> attributes = getInstanceMap( va );
        if ( ! attributes.containsKey( va.getErAttributeToken() ) )
            attributes.put( va.getErAttributeToken(), new AttributeValue( va ) );

        return attributes.get( va.getErAttributeToken() );
    }

    /**
     * Copies attribute values and flags from source list to 'this'.
     * @param task TODO
     * @param sourceList
     * @param copyPersist
     * @param copyUpdateFlags
     *
     * @return
     */
    AttributeListInstance copyAttributes( TaskImpl task, AttributeListInstance sourceList,
                                          boolean copyPersist, boolean copyUpdateFlags )
    {
        for ( ViewAttribute viewAttrib : sourceList.getNonNullAttributeList(task) )
        {
            if ( viewAttrib.isPersistent() && !copyPersist )
                continue;

            AttributeValue source = sourceList.getAttribute( viewAttrib );
            AttributeValue target = getAttribute( viewAttrib );
            Object internalValue = sourceList.getAttribute( viewAttrib ).getInternalValue();
            target.setInternalValue( task, viewAttrib, internalValue, true );

            if ( copyUpdateFlags )
                target.copyUpdateFlags( source );
            else
                target.setUpdated( true );
        }

        return this;
    }

    private ViewEntity getViewEntity()
    {
        return viewEntity;
    }

    /**
     * Returns a list of attributes that are not null or have been updated.
     *
     * @param task TODO
     */
    Iterable<ViewAttribute> getNonNullAttributeList(final Task task)
    {
        return new Iterable<ViewAttribute>()
        {
            @Override
            public Iterator<ViewAttribute> iterator()
            {
                return new Iterator<ViewAttribute>()
                {
                    private int attributeNumber     = -1;
                    private int nextAttributeNumber = -1;

                    @Override
                    public boolean hasNext()
                    {
                        if ( nextAttributeNumber <= attributeNumber )
                        {
                            nextAttributeNumber = attributeNumber + 1;
                            while ( nextAttributeNumber < getViewEntity().getAttributeCount() )
                            {
                                ViewAttribute viewAttribute = getViewEntity().getAttribute( nextAttributeNumber );
                                AttributeValue attrib = getAttribute( viewAttribute );
                                if ( ! attrib.isNull(task, viewAttribute) || attrib.isUpdated() )
                                    break;

                                nextAttributeNumber++;
                            }
                        }

                        return nextAttributeNumber < getViewEntity().getAttributeCount();
                    }

                    @Override
                    public ViewAttribute next()
                    {
                        attributeNumber = nextAttributeNumber;
                        return getViewEntity().getAttribute( attributeNumber );
                    }

                    @Override
                    public void remove()
                    {
                        throw new UnsupportedOperationException();
                    }
                };
            }
        };
    }

    /**
     * Adds newInstance to the list of linked entities for firstInstance.
     *
     * @param firstInstance
     * @param newInstance
     */
    synchronized void addLinkedInstance( EntityInstanceImpl firstInstance, EntityInstanceImpl newInstance )
    {
        if ( linkedInstances == null )
        {
            // Create a concurrent map that uses weak references for the keys.
            // This will allow the GC to clean up if a linked instance goes
            // away.
            // Initialize it with firstInstance.
            linkedInstances = new MapMaker().concurrencyLevel( 2 ).weakKeys().makeMap();
            linkedInstances.put( firstInstance, Boolean.TRUE );
        }

        linkedInstances.putIfAbsent( newInstance, Boolean.TRUE );
    }

    /**
     * Returns the linked instances if there are any. If 'excludeSource' is true
     * then 'source' will be ignored when building the return list. This allows
     * the caller to get a list of all other linked instances.
     *
     * Note that entities that have been dropped but not reclaimed by the GC
     * will still show up in this list. We can skip most of those by ignoring
     * ones flagged as dropped.
     *
     * @param source
     *            Ignore this EI when creating the list.
     *
     * @return List of linked instances. If there are none, an empty list is
     *         returned.
     */
    Collection<EntityInstanceImpl> getLinkedInstances( boolean includeDropped, boolean excludeSource,
                                                       EntityInstanceImpl source )
    {
        // If linkedInstances == null then this EI has never been linked to
        // anything.
        if ( linkedInstances == null )
        {
            if ( excludeSource )
                return Collections.emptyList();

            List<EntityInstanceImpl> list = new ArrayList<EntityInstanceImpl>();
            list.add( source );
            return list;
        }

        // We'll use a hash set instead of a list because some callers will use
        // .contains(...)
        // and a hash set is faster.
        HashSet<EntityInstanceImpl> list = new HashSet<EntityInstanceImpl>( linkedInstances.size() );
        for ( EntityInstanceImpl ei : linkedInstances.keySet() )
        {
            if ( ei == null )
                continue;

            if ( excludeSource && ei == source )
                continue;

            if ( ei.isDropped() && !includeDropped )
                continue;

            list.add( ei );
        }

        assert list.size() > 0 || source != null : "getLinkedInstances returned empty list";
        return list;
    }

    /**
     * Add the linked instances from source to 'this'. Intended to be used by
     * Accept logic for merging linked instances from a new, accepted, entity.
     *
     * @param source
     */
    void mergeLinkedInstances( EntityInstanceImpl targetInstance, AttributeListInstance source )
    {
        // If source doesn't have any linkedInstances than there's nothing to
        // do.
        if ( source.linkedInstances == null )
            return;

        for ( EntityInstanceImpl linked : source.linkedInstances.keySet() )
            addLinkedInstance( targetInstance, linked );
    }

    /**
     * Returns true if 'this' has the same persistent attributes as
     * otherInstance.
     *
     * @param otherInstance
     * @return
     */
    boolean isLinkedWith( AttributeListInstance otherInstance )
    {
        return persistentAttributes == otherInstance.persistentAttributes;
    }
}