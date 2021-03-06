/**
 *
 */
package com.quinsoft.zeidon.scala

import com.quinsoft.zeidon._
import com.quinsoft.zeidon.standardoe.JavaObjectEngine

/**
 * @author dgc
 *
 */
class TestOperations ( task: Task ) extends ZeidonOperations( task ) {

  /**
   * Following constructor allows this to be called from Zeidon JOE.
   */
  def this( jtask: com.quinsoft.zeidon.Task ) = this( new Task( jtask ) )

    def CompileOper() {
        var view = VIEW basedOnLod "mPerson"
        view.activateWhere( _.Person.PersonID = 1 )
                       .orAll( _.Person.PersonID > 2,
                               _.Person.PersonID < 5 )
                       .and( q => q.Person.PersonID = q.Person.LastName )
                       .activate


    }

	def TestOper() {
		var view = VIEW BASEDONLOD "lStudDpt"
		view.buildQual( _.Student.Status = "A" )
		          .and( _.MajorDepartment.ID = 3 ).activate

		view.jview.logObjectInstance()
        val b = view.Student.eMailAddress.isNull

		FOREACH ( view.Student ) WHERE ( view.AdministrativeDivision.ID == 1 ) DO {
		    println( view.AdministrativeDivision.Name )
		}

        if ( SETFIRST( view.Student ) WHERE ( view.Person.MiddleName == "M"  ) ) {
            println( view.Person.FirstName )
            println( view.Person.LastName )
            println( view.Person.MiddleName )
        }

        
        if ( SETFIRST( view.Student ) WHERE ( _.eMailAddress == null  ) ) {
            println( view.Person.FirstName )
            println( view.Person.LastName )
            println( view.Person.MiddleName )
        }

        println("----------------------------")
        FOREACH ( view.Student ) WHERE ( view.Person.MiddleName == "M" || view.Person.MiddleName == "S" ) DO {
            println( view.Person.FirstName )
            println( view.Person.MiddleName )
            println( view.Person.LastName )
        }

        println("----------------------------")
        val result = SETFIRST( view.Student ) WHERE ( view.Person.MiddleName == "M" || view.Person.MiddleName == "S" )
        while ( result ) {
            println( view.Person.FirstName )
            println( view.Person.MiddleName )
            println( view.Person.LastName )
            result.SETNEXT
        }

		view = VIEW basedOnLod "mPerson"
		view activateEmpty

        view.Person create NEXT
		view.Person.ID = 1

		view.Person.create.ID = 2

		view.Person include view.Person
		view.Person.setFirst
		var personId: String = view.Person.ID

		val v = VIEW from view

		//
		// Different ways to call setFirst on a cursor.
		//

		// No arguments.  Set's cursor to first EI.
		view.Person.setFirst

		// Function argument with no parameters.  This can execute any code that
		// returns a boolean value.
		view.Person.setFirst( view.Person.ID == 2 || view.Person.ID == 3 )

		// Function argument with EntityInstance parameter.
        view.Person.setFirst( _.ID == 2 )
        view.Person.setFirst( (ei: EntityInstance) => ei.ID == 2 )  // Exactly equivalent to line above.

        // Function argument passing attribute name and value.  This has the advantage
        // of using Attribute HashKeys if they are specified for the LOD.
        view.Person.setFirst( "ID", 2 )

        // Method call from parent ZeidonOperations.scala
        SETFIRST( view.Person ) WHERE ( view.Person.ID == 2 )

		var id = view.Person.ID
		personId = view.Person.ID

		view.Person.foreach(ei => println(view.Person.ID) )
        view.Person.foreach(ei => println( ei.ID) )
        view.Person.iterator.withFilter(ei => ei.ID == 2 ).foreach( ei => println( ei ) )
        view.Person.iterator.withFilter( _.ID == 2 ).foreach( println( _ ) )

		FOREACH ( view.Person ) UNDER view.Person DO {
		  view.Person.Id = 0
		}

        FOREACH ( view.Person ) UNDER view.Person WHERE { view.Person.Idxxx > 5 } DO {
          view.Person.Id = 0
        }

		println( view )
	}
}

/*
import com.quinsoft.zeidon._
import com.quinsoft.zeidon.standardoe.JavaObjectEngine
import com.quinsoft.zeidon.scala._
val oe = JavaObjectEngine.getInstance()
val task = oe.createTask("Cheetah")
 */
object TestOperations {

    def main(args: Array[String]): Unit = {
        val oe = JavaObjectEngine.getInstance()
        val task = oe.createTask("ZENCAs")
        val stask = Task(task)
        val to = new TestOperations( stask )
        to.TestOper
    }
}