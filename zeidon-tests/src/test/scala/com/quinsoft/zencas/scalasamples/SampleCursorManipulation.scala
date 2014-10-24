/**
 * This file is part of the Zeidon Java Object Engine (Zeidon JOE).
 *
 * Zeidon JOE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Zeidon JOE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Zeidon JOE.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2009-2014 QuinSoft
 */
package com.quinsoft.zencas.scalasamples

import com.quinsoft.zeidon.scala.ZeidonOperations
import com.quinsoft.zeidon.scala.Task
import com.quinsoft.zeidon.scala.ObjectEngine
import com.quinsoft.zeidon.scala.View
import com.quinsoft.zeidon.scala.basedOn
import com.quinsoft.zencas.scalasamples.SampleActivates;
import com.quinsoft.zencas.scalasamples.SampleCursorManipulation;

/**
 *  This gives examples of how to manipulate cursors.  Usually there are two
 *  ways to manipulate cursors in Scala, the "VML" way and the "Scala" way.
 *  When appropriate both ways will be shown.
 */
class SampleCursorManipulation( val task: Task ) extends ZeidonOperations {

    /**
     * Simple SET CURSOR FIRST
     */
    def setCursorFirst( mUser: View @basedOn( "mUser") ) = {
        /* VML:
         *          SET CURSOR FIRST mUser.User
         *          IF RESULT >= zCURSOR_SET THEN
         *              ...
         *          END
         */

        // VML way 1
        SETFIRST( mUser.User )
        if ( RESULT >= zCURSOR_SET ) {
            println( "Cursor was set" )
        }

        // VML way 2
        if ( SETFIRST( mUser.User ) ) {
            println( "Cursor was set" )
        }

        // Scala way
        if ( mUser.User.setFirst ) {
            println( "Cursor was set" )
        }
    }

    /**
     * Set cursor with a WHERE predicate
     */
    def setCursorFirstWhere( mUser: View @basedOn( "mUser") ) = {
        /* VML:
         *          SET CURSOR FIRST mUser.User WHERE mUser.User.ID = 490
         *          IF RESULT >= zCURSOR_SET THEN
         *              ...
         *          END
         */

        // VML way
        if ( SETFIRST( mUser.User ) WHERE( mUser.User.ID == 490 ) ) {
            println( "Cursor was set" )
        }

        // Scala way
        if ( mUser.User.setFirst( mUser.User.ID == 490 ) ) {
            println( "Cursor was set" )
        }
    }

    /**
     * Set cursor with a multipart WHERE predicate
     */
    def setCursorFirstCompoundWhere( mUser: View @basedOn( "mUser") ) = {
        /* VML:
         *          SET CURSOR FIRST mUser.User WHERE mUser.User.ID = 490 OR mUser.User.ID = 491
         *          IF RESULT >= zCURSOR_SET THEN
         *              ...
         *          END
         */

        // VML way
        if ( SETFIRST( mUser.User ) WHERE( mUser.User.ID == 490 || mUser.User.ID == 491) ) {
            println( "Cursor was set" )
        }

        // Scala way
        if ( mUser.User.setFirst( mUser.User.ID == 490  || mUser.User.ID == 491 ) ) {
            println( "Cursor was set" )
        }
    }

    /**
     * Set cursor with a multipart WHERE predicate
     */
    def setCursorFirstWithScoping( mUser: View @basedOn( "mUser") ) = {
        /* VML:
         *          SET CURSOR FIRST mUser.User WHERE mUser.User.ID = 490 OR mUser.User.ID = 491
         *          IF RESULT >= zCURSOR_SET THEN
         *              ...
         *          END
         */

        // VML way
        if ( SETFIRST( mUser.User ) WHERE( mUser.User.ID == 490 || mUser.User.ID == 491) ) {
            println( "Cursor was set" )
        }

        // Scala way
        if ( mUser.User.setFirst( mUser.User.ID == 490  || mUser.User.ID == 491 ) ) {
            println( "Cursor was set" )
        }
    }
}

object SampleCursorManipulation {

   def main(args: Array[String]): Unit = {

        // Load the object engine and create a task.
        val oe = ObjectEngine.getInstance
        val task = oe.createTask("ZENCAs")

        val activator = new SampleActivates( task )
        var mUser = activator.activateSimple

        val sampler = new SampleCursorManipulation( task )
        sampler.setCursorFirst(mUser)
        sampler.setCursorFirstWhere(mUser)

//        mUser.logObjectInstance
    }

}