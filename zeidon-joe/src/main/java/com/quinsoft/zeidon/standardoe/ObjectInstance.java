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
/**
 *
 */
package com.quinsoft.zeidon.standardoe;

import java.util.Iterator;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.quinsoft.zeidon.ActivateOptions;
import com.quinsoft.zeidon.Lockable;
import com.quinsoft.zeidon.dbhandler.PessimisticLockingHandler;
import com.quinsoft.zeidon.objectdefinition.ViewOd;
import com.quinsoft.zeidon.utils.LazyLoadLock;

/**
 * @author DG
 *
 */
class ObjectInstance implements Lockable
{
    private TaskImpl            task;
    private final ViewOd        viewOd;

    /**
     * A unique internal ID created for each OI.  Note this ID is unique only within
     * a single JOE instance.  Use uuid for IDs that are unique across instances.
     */
    private final long          id;

    private final UUID          uuid;

    private EntityInstanceImpl  rootEntityInstance;
    private boolean             isLocked;   // TODO: implement this
    private boolean             isReadOnly;
    private final AtomicInteger versionedInstances;
    private PessimisticLockingHandler pessimisticLockingHandler;

    /**
     * If true then this OI has been updated since it was last loaded from the DB.
     */
    private boolean         updated = false;

    /**
     * If true then this OI has been updated since it was last loaded from a file.
     */
    private boolean         updatedFile = false;

    /**
     * Stores the option used when activating the OI.  This is intended to be used by lazy load
     * processing.
     */
    private ActivateOptions activateOptions;

    // Following used for commit processing
    boolean dbhNeedsForeignKeys;
    boolean dbhNeedsGenKeys;

    private final LazyLoadLock lock;

    /**
     * This keeps track of attribute hash keys that are global to the OI.  Intended for use
     * by cursor.setFirst() processing.
     */
    private final AttributeHashKeyMap attributeHashkeyMap;

    ObjectInstance(TaskImpl task, ViewOd viewOd)
    {
        this.task = task;
        this.viewOd = viewOd;
        id = task.getObjectEngine().getNextObjectKey();
        uuid = task.getObjectEngine().generateUuid();
        lock = new LazyLoadLock();
        versionedInstances = new AtomicInteger( 0 );
        attributeHashkeyMap = new AttributeHashKeyMap( this );
    }

    ViewOd getViewOd()
    {
        return viewOd;
    }

    TaskImpl getTask()
    {
        return task;
    }

    /**
     * A unique internal ID created for each OI.  Note this ID is unique only within
     * a single JOE instance.  Use uuid for IDs that are unique across instances.
     */
    long getId()
    {
        return id;
    }

    EntityInstanceImpl getRootEntityInstance()
    {
        return rootEntityInstance;
    }

    void setRootEntityInstance(EntityInstanceImpl rootEntityInstance)
    {
        this.rootEntityInstance = rootEntityInstance;
    }

    boolean isUpdated()
    {
        return updated;
    }

    void setUpdated(boolean updated)
    {
        this.updated = updated;
        if ( updated )
            setUpdatedFile( true );
    }

    boolean isUpdatedFile()
    {
        return updatedFile;
    }

    void setUpdatedFile(boolean updatedFile)
    {
        this.updatedFile = updatedFile;
    }

    boolean isLocked()
    {
        return isLocked;
    }

    void setLocked(boolean isLocked)
    {
        this.isLocked = isLocked;
    }

    boolean isReadOnly()
    {
        return isReadOnly;
    }

    void setReadOnly(boolean isReadOnly)
    {
        this.isReadOnly = isReadOnly;
    }

    void incrementVersionedCount()
    {
        versionedInstances.incrementAndGet();
    }

    void decrementVersionedCount()
    {
        versionedInstances.decrementAndGet();
    }

    boolean isVersioned()
    {
        return versionedInstances.intValue() > 0;
    }

    EntityInstanceImpl findByHierIndex( long index )
    {
        return getRootEntityInstance().findByHierIndex( index );
    }

    /**
     * Iterable that loops through all entities, including hidden ones.
     *
     * @return
     */
    Iterable<EntityInstanceImpl> getEntities()
    {
        return getEntities( true );
    }

    Iterable<EntityInstanceImpl> getEntities( final boolean allowHidden )
    {
        return new Iterable<EntityInstanceImpl>()
        {
            @Override
            public Iterator<EntityInstanceImpl> iterator()
            {
                return new IteratorBuilder(ObjectInstance.this)
                                .withOiScoping( ObjectInstance.this )
                                .allowHidden( allowHidden )
                                .setLazyLoad( false )
                                .build();
            }
        };
    }

    @Override
    public String toString()
    {
        return viewOd.toString();
    }

    /* (non-Javadoc)
     * @see com.quinsoft.zeidon.Lockable#getLock()
     */
    @Override
    public ReentrantReadWriteLock getLock()
    {
        return lock.getLock();
    }

    /**
     * Goes through all the prev/next pointers and attempts to verify that they are correctly
     * set.
     *
     * @return
     */
    boolean validateChains()
    {
        EntityInstanceImpl root = getRootEntityInstance();
        if ( root == null )
            return true;

        root = root.getLatestVersion();
        for ( EntityInstanceImpl scan = root; scan != null; scan = scan.getNextHier() )
        {
            if ( scan.getPrevHier() != null )
            {
                if ( scan.getPrevHier().getNextHier() != scan )
                {
                    writeValidateError( scan, scan.getPrevHier(), "Prev/Next hier pointers don't match" );
                    return false;
                }
            }

            EntityInstanceImpl next = scan.getNextHier();
            if ( next != null )
            {
                if ( next.getPrevHier() != scan )
                {
                    writeValidateError( scan, next, "Next/prev hier pointers don't match" );
                    scan.logEntity( false );
                    next.logEntity( false );
                    return false;
                }
            }

            if ( scan.getNextTwin() != null )
            {
                if ( scan.getNextTwin().getPrevTwin() != scan )
                {
                    writeValidateError( scan, scan.getNextTwin(), "Next/prev Twin pointers don't match" );
                    return false;
                }

                if ( scan.getNextTwin().getViewEntity() != scan.getViewEntity() )
                {
                    writeValidateError( scan, scan.getNextTwin(), "ViewEntity next Twin pointers don't match" );
                    return false;
                }
            }

            if ( scan.getPrevTwin() != null )
            {
                if ( scan.getPrevTwin().getParent() != scan.getParent() )
                {
                    writeValidateError( scan, scan.getPrevTwin(), "Parent pointers don't match" );
                    return false;
                }

                if ( scan.getPrevTwin().getNextTwin() != scan )
                {
                    writeValidateError( scan, scan.getPrevTwin(), "Prev/Next twin pointers don't match" );
                    return false;
                }

                if ( scan.getPrevTwin().getViewEntity() != scan.getViewEntity() )
                {
                    writeValidateError( scan, scan.getPrevTwin(), "Prev twin ViewEntity don't match" );
                    return false;
                }
            }
        }

        return true;
    }

    private void writeValidateError( EntityInstanceImpl scan, EntityInstanceImpl other, String msg )
    {
        getTask().log().error( msg + "\nScan = %s (%d)\nEI2  = %s (%d)",
                               scan, scan.getEntityKey(), other, other.getEntityKey() );
    }

    /**
     * @param task2
     */
    void setTask( TaskImpl task )
    {
        this.task = task;
    }

    /**
     * @return the pessimisticLockingHandler
     */
    public PessimisticLockingHandler getPessimisticLockingHandler()
    {
        return pessimisticLockingHandler;
    }

    /**
     * @param pessimisticLockingHandler the pessimisticLockingHandler to set
     */
    void setPessimisticLockingHandler( PessimisticLockingHandler pessimisticLockingHandler )
    {
        this.pessimisticLockingHandler = pessimisticLockingHandler;
    }

    AttributeHashKeyMap getAttributeHashkeyMap()
    {
        return attributeHashkeyMap;
    }

    /**
     * @return the activateOptions
     */
    ActivateOptions getActivateOptions()
    {
        return activateOptions;
    }

    /**
     * @param activateOptions the activateOptions to set
     */
    void setActivateOptions( ActivateOptions activateOptions )
    {
        this.activateOptions = activateOptions;
    }

    public UUID getUuid()
    {
        return uuid;
    }
}
