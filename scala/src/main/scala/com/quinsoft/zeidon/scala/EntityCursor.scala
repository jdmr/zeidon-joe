/**
 *
 */
package com.quinsoft.zeidon.scala

import scala.language.dynamics
import scala.collection.Iterable
import com.quinsoft.zeidon._
import com.quinsoft.zeidon.objectdefinition._

/**
 * @author dgc
 *
 */
class EntityCursor( private[this] val view: View,
                     val jentityCursor: com.quinsoft.zeidon.EntityCursor )
            extends AbstractEntity( jentityCursor.getViewEntity() )
            with Iterable[EntityInstance]
{
    def getEntityInstance: com.quinsoft.zeidon.EntityInstance = jentityCursor.getEntityInstance()

    def create(): EntityCursor = {
        jentityCursor.createEntity()
        this
    }

    def create( position: CursorPosition = CursorPosition.NEXT ): EntityCursor = {
        jentityCursor.createEntity()
        this
    }

    def drop: CursorResult = jentityCursor.dropEntity()
    def drop( reposition: CursorPosition ): CursorResult = jentityCursor.dropEntity( reposition )
    def delete(): CursorResult = jentityCursor.deleteEntity()
    def delete( reposition: CursorPosition ): CursorResult = jentityCursor.deleteEntity( reposition )

    def include( source: AbstractEntity, position: CursorPosition = CursorPosition.NEXT ) = {
        jentityCursor.includeSubobject( source.getEntityInstance, position )
    }

    def count = jentityCursor.getEntityCount()
    def setFirst: CursorResult = jentityCursor.setFirst()
    def setNext: CursorResult = jentityCursor.setNext()

    def setFirst( predicate:  => Boolean, scopingEntity: String = null ): Boolean = {
        val iter = jentityCursor.eachEntity( scopingEntity )
        while ( iter.hasNext() )
        {
            iter.next()
            if ( predicate )
                return true
        }

        return false
    }

    def setFirst( predicate: (EntityInstance) => Boolean ): Boolean = {
        val iter = jentityCursor.eachEntity()
        while ( iter.hasNext() )
        {
            val ei = iter.next()
            if ( predicate( new EntityInstance( ei ) ) )
                return true
        }

        return false
    }

    def setFirst( attributeName: String, value: Any): Boolean = {
        if ( value.isInstanceOf[ AttributeInstance ] )
            // If the value is of type AttributeInstance then convert it to an internal value.
            jentityCursor.setFirst( attributeName, value.asInstanceOf[ AttributeInstance ].
                                                   jattributeInstance.getValue ) == CursorResult.SET
        else
            jentityCursor.setFirst( attributeName, value ) == CursorResult.SET
    }

    def iterator = {
        val iter = jentityCursor.eachEntity
        new Iterator[EntityInstance] {
            def hasNext = iter.hasNext()
            def next = new EntityInstance( iter.next() )
        }
    }

    def iterator( scopingEntity: String ) = {
        val iter = jentityCursor.eachEntity( scopingEntity )
        new Iterator[EntityInstance] {
            def hasNext = iter.hasNext()
            def next = new EntityInstance( iter.next() )
        }
    }
}