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
package com.quinsoft.zeidon.standardoe;

import com.quinsoft.zeidon.EntityCursor;
import com.quinsoft.zeidon.ZeidonException;
import com.quinsoft.zeidon.objectdefinition.ViewEntity;
import com.quinsoft.zeidon.objectdefinition.ViewOd;

/**
 * @author DG
 *
 */
class ViewCursor
{
    private final ViewImpl         view;
    private final ViewOd           viewOd;
    private final EntityCursorImpl cursorList[];
    private final ObjectInstance   objectInstance;
    
    /**
     * If this view was set to a recursive suboject this points to the root instance
     * of the subobject.
     */
    private EntityInstanceImpl recursiveRoot;
    
    /**
     * When there is a subobject cursor defined, this represents the difference in levels
     * between recursiveRoot and its view entity level.  When there's no subobject this is 0.
     */
    private int recursiveDiff;
    private int firstValidCursorIndex;
    private int lastValidCursorIndex;
    
    ViewCursor( TaskImpl task, ViewImpl view, ViewOd viewOd )
    {
        this( task, view, viewOd, null, null );
    }

    ViewCursor( ViewImpl view, ViewCursor sourceCursor )
    {
        this( sourceCursor.getTask(), view, sourceCursor.getViewOD(), sourceCursor, null );
    }

    ViewCursor( ViewImpl view, ObjectInstance oi )
    {
        this( oi.getTask(), view, oi.getViewOd(), null, oi );
    }

    /**
     * Create and initialize the entity cursors from a source cursor.
     * 
     * @param task
     * @param viewOd
     * @param sourceCursor - If not null, then initialize the cursors in the new ViewCursor to point to the same entities.
     */
    private ViewCursor(TaskImpl task, ViewImpl view, ViewOd viewOd, ViewCursor sourceCursor, ObjectInstance oi )
    {
        super();
        this.viewOd = viewOd;
        this.view = view;
        cursorList = new EntityCursorImpl[ viewOd.getEntityCount() ];
        for ( ViewEntity viewEntity : viewOd.getViewEntitiesHier() )
        {
            int idx = viewEntity.getHierIndex();

            EntityCursorImpl parentCsr = null;
            ViewEntity parent = viewEntity.getParent();
            if ( parent != null )
            {
                int parentIdx = parent.getHierIndex();
                parentCsr = cursorList[ parentIdx ];
            }

            if ( sourceCursor != null )
                cursorList[ idx ] = new EntityCursorImpl( this, sourceCursor.cursorList[ idx ], parentCsr );
            else
                cursorList[ idx ] = new EntityCursorImpl( this, viewEntity, parentCsr );
        }
        
        // Set the next/prev pointers.
        for ( int i = 0; i < cursorList.length; i++ )
        {
            if ( i > 0 )
                cursorList[i].setPrevHier( cursorList[i - 1] );
            
            if ( i < cursorList.length - 1 )
                cursorList[i].setNextHierCursor( cursorList[i + 1] );
        }
        
        if ( oi != null )
        {
            objectInstance = oi;
            setRecursiveParent( null );
        }
        else
        if ( sourceCursor != null )
        {
            objectInstance = sourceCursor.getObjectInstance();
            recursiveRoot = sourceCursor.getRecursiveRoot();
            setRecursiveDiff( sourceCursor.getRecursiveDiff() );
            firstValidCursorIndex = sourceCursor.firstValidCursorIndex;
            lastValidCursorIndex = sourceCursor.lastValidCursorIndex;
        }
        else
        {
            objectInstance = new ObjectInstance(task, viewOd);
            setRecursiveParent( null );
        }
    }
    
    TaskImpl getTask()
    {
        return objectInstance.getTask();
    }
    
    protected EntityCursorImpl getEntityCursor( String entityName )
    {
        return getEntityCursor( viewOd.getViewEntity( entityName ) );
    }

    protected EntityCursorImpl getEntityCursor( ViewEntity viewEntity )
    {
        return cursorList[ viewEntity.getHierIndex() ];
    }

    protected ObjectInstance getObjectInstance()
    {
        return objectInstance;
    }

    protected ViewOd getViewOD()
    {
        return viewOd;
    }
    
    void copyEntityCursors( ViewCursor source )
    {
        for ( int i = 0; i < cursorList.length; i++ )
            cursorList[ i ] = source.cursorList[ i ];
    }

    void setRecursiveParent(EntityInstanceImpl recursiveParent)
    {
        this.recursiveRoot = recursiveParent;
        if ( recursiveRoot == null )
        {
            recursiveDiff = 0;
            return;
        }
        
        // Calculate the recursiveDiff.
        ViewEntity viewEntity = recursiveParent.getViewEntity();
        if ( viewEntity.getRecursiveParentViewEntity() != null )
            viewEntity = viewEntity.getRecursiveParentViewEntity();
        recursiveDiff = recursiveParent.getLevel() - viewEntity.getLevel();
        if ( recursiveDiff == 0 )
        {
            // We've reset the cursor back to its "normal" state so there is no recursiveRoot.
            recursiveRoot = null;
            return;
        }
        
        // Calculate which cursors are now valid with the new scope.
        firstValidCursorIndex = viewEntity.getHierIndex();
        lastValidCursorIndex = viewEntity.getLastChildHier().getHierIndex();
    }
    
    void resetSubobjectToParent()
    {
        EntityInstanceImpl currentRoot = getRecursiveRoot(); 
        if ( currentRoot == null )
            return;
//            throw new ZeidonException("View %s has no subobject cursors", getViewOd() );
 
        // We need to find the ancestor of currentRoot that has the same ER entity token
        // as current root.
        ViewEntity viewEntity = currentRoot.getViewEntity();
        ViewEntity recursiveParent = viewEntity.getRecursiveParentViewEntity();
        EntityInstanceImpl ancestor = currentRoot.findMatchingParent( recursiveParent );
        if ( ancestor == null )
            throw new ZeidonException( "Current subobject root has no valid ancestor" );
        
        setRecursiveParent( ancestor );
        view.cursor( recursiveParent ).setCursor( ancestor );  // Set the cursor for the parent entity.
        view.cursor( viewEntity ).setCursor( currentRoot );    // Set the cursor for the recursive child.
    }

    EntityInstanceImpl getRecursiveRoot()
    {
        return recursiveRoot;
    }

    private void setRecursiveDiff(int recursiveDiff)
    {
        this.recursiveDiff = recursiveDiff;
    }

    int getRecursiveDiff()
    {
        return recursiveDiff;
    }

    /**
     * Verifies that the supplied entity cursor is in scope.
     * 
     * @param cursor
     * @return
     */
    boolean isCursorInScope( EntityCursor cursor )
    {
        if ( recursiveRoot == null )
            return true;  // All cursors are in scope if there is no recursive subobject defined.
        
        ViewEntity viewEntity = cursor.getViewEntity();
        if ( viewEntity.getHierIndex() < firstValidCursorIndex )
            return false;
        
        if ( viewEntity.getHierIndex() > lastValidCursorIndex )
            return false;
        
        return true;
    }

    ViewImpl getView()
    {
        return view;
    }

    ViewOd getViewOd()
    {
        return viewOd;
    }
    
    /**
     * Sets the root cursor to point to the first entity and all child cursors to NOT-SET.
     */
    void reset()
    {
        if ( objectInstance.getRootEntityInstance() == null )
            return;
        
        cursorList[0].setCursor( objectInstance.getRootEntityInstance() );
        setRecursiveParent( null );
//        cursorList[0].resetChildCursors( objectInstance.getRootEntityInstance() );
    }
}
