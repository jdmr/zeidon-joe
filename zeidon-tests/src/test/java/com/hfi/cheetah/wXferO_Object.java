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

    Copyright 2009-2010 QuinSoft
**/

package com.hfi.cheetah;

import com.quinsoft.zeidon.ActivateFlags;
import com.quinsoft.zeidon.CursorPosition;
import com.quinsoft.zeidon.TaskQualification;
import com.quinsoft.zeidon.vml.VmlObjectOperations;
import com.quinsoft.zeidon.View;
import com.quinsoft.zeidon.ZeidonException;
import com.quinsoft.zeidon.vml.zVIEW;
import org.apache.commons.lang3.mutable.MutableDouble;
import org.apache.commons.lang3.mutable.MutableInt;

import com.hfi.cheetah.ZGLOBAL1_Operation;

/**
   @author QuinSoft
**/

public class wXferO_Object extends VmlObjectOperations
{
   public wXferO_Object( View view )
   {
      super( view );
   }


//:DERIVED ATTRIBUTE OPERATION
//:dCurrentDateTime( VIEW wXferO BASED ON LOD wXferO,
//:                  STRING ( 32 ) InternalEntityStructure,
//:                  STRING ( 32 ) InternalAttribStructure,
//:                  SHORT GetOrSetFlag )

//:   STRING (  18  ) szDateTime
public int 
owXferO_dCurrentDateTime( View     wXferO,
                          String InternalEntityStructure,
                          String InternalAttribStructure,
                          Integer   GetOrSetFlag )
{
   String   szDateTime = null;


   //:CASE GetOrSetFlag
   switch( GetOrSetFlag )
   { 
      //:OF zDERIVED_GET:
      case zDERIVED_GET :
         //:HFI_GetDateTime( szDateTime )
         {
          ZGLOBAL1_Operation m_ZGLOBAL1_Operation = new ZGLOBAL1_Operation( wXferO );
          {StringBuilder sb_szDateTime;
         if ( szDateTime == null )
            sb_szDateTime = new StringBuilder( 32 );
         else
            sb_szDateTime = new StringBuilder( szDateTime );
                   m_ZGLOBAL1_Operation.HFI_GetDateTime( sb_szDateTime );
         szDateTime = sb_szDateTime.toString( );}
          // m_ZGLOBAL1_Operation = null;  // permit gc  (unnecessary)
         }
         //:StoreStringInRecord ( wXferO,
         //:                      InternalEntityStructure, InternalAttribStructure, szDateTime )
         StoreStringInRecord( wXferO, InternalEntityStructure, InternalAttribStructure, szDateTime );
         break ;
      //:/* end zDERIVED_GET */
      //:OF zDERIVED_SET:
      case zDERIVED_SET :
         break ;
   } 


   //:   /* end zDERIVED_SET */
   //:END  /* case */
   return( 0 );
// END
} 


//:DERIVED ATTRIBUTE OPERATION
//:dCurrentTime( VIEW wXferO BASED ON LOD wXferO,
//:              STRING ( 32 ) InternalEntityStructure,
//:              STRING ( 32 ) InternalAttribStructure,
//:              SHORT GetOrSetFlag )
//:   STRING (  18  ) szTime
public int 
owXferO_dCurrentTime( View     wXferO,
                      String InternalEntityStructure,
                      String InternalAttribStructure,
                      Integer   GetOrSetFlag )
{
   String   szTime = null;


   //:CASE GetOrSetFlag
   switch( GetOrSetFlag )
   { 
      //:OF zDERIVED_GET:
      case zDERIVED_GET :
         //:// KJS 04/04/14 - Getting the derived attribute dCurrentDateTime from within the derived
         //:// attribute is not working. I am pretty sure that getting a version of zeidon-joe solves this
         //:// issue (because I had it in MyENC) but for now I am calling HFI_GetDateTime which also
         //:// solves the issue.
         //://szTime = wXferO.Root.dCurrentDateTime
         //:HFI_GetDateTime( szTime )
         {
          ZGLOBAL1_Operation m_ZGLOBAL1_Operation = new ZGLOBAL1_Operation( wXferO );
          {StringBuilder sb_szTime;
         if ( szTime == null )
            sb_szTime = new StringBuilder( 32 );
         else
            sb_szTime = new StringBuilder( szTime );
                   m_ZGLOBAL1_Operation.HFI_GetDateTime( sb_szTime );
         szTime = sb_szTime.toString( );}
          // m_ZGLOBAL1_Operation = null;  // permit gc  (unnecessary)
         }
         //:StoreStringInRecord ( wXferO,
         //:                      InternalEntityStructure, InternalAttribStructure, szTime )
         StoreStringInRecord( wXferO, InternalEntityStructure, InternalAttribStructure, szTime );
         break ;
      //:/* end zDERIVED_GET */
      //:OF zDERIVED_SET:
      case zDERIVED_SET :
         break ;
   } 


   //:   /* end zDERIVED_SET */
   //:END  /* case */
   return( 0 );
// END
} 


//:DERIVED ATTRIBUTE OPERATION
//:dCurrentDate( VIEW wXferO BASED ON LOD wXferO,
//:              STRING ( 32 ) InternalEntityStructure,
//:              STRING ( 32 ) InternalAttribStructure,
//:              SHORT GetOrSetFlag )
//:   STRING (  18  ) szDate
public int 
owXferO_dCurrentDate( View     wXferO,
                      String InternalEntityStructure,
                      String InternalAttribStructure,
                      Integer   GetOrSetFlag )
{
   String   szDate = null;


   //:CASE GetOrSetFlag
   switch( GetOrSetFlag )
   { 
      //:OF zDERIVED_GET:
      case zDERIVED_GET :
         //:HFI_GetDateTime( szDate )
         {
          ZGLOBAL1_Operation m_ZGLOBAL1_Operation = new ZGLOBAL1_Operation( wXferO );
          {StringBuilder sb_szDate;
         if ( szDate == null )
            sb_szDate = new StringBuilder( 32 );
         else
            sb_szDate = new StringBuilder( szDate );
                   m_ZGLOBAL1_Operation.HFI_GetDateTime( sb_szDate );
         szDate = sb_szDate.toString( );}
          // m_ZGLOBAL1_Operation = null;  // permit gc  (unnecessary)
         }
         //:StoreStringInRecord ( wXferO,
         //:                      InternalEntityStructure, InternalAttribStructure, szDate )
         StoreStringInRecord( wXferO, InternalEntityStructure, InternalAttribStructure, szDate );
         break ;
      //:/* end zDERIVED_GET */
      //:OF zDERIVED_SET:
      case zDERIVED_SET :
         break ;
   } 


   //:   /* end zDERIVED_SET */
   //:END  /* case */
   return( 0 );
// END
} 



}
