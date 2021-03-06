
package com.quinsoft.zeidon.zeidonoperations;

import com.quinsoft.zeidon.zeidonoperations.ldapAdminBind;
import com.quinsoft.zeidon.zeidonoperations.ldapFastBind;
import com.quinsoft.zeidon.zeidonoperations.ldapBind;

/**
 * This file belongs to the c-to-java conversion system.  These files convert functionality
 * defined in older .c files into Java.  They use non-Java naming conventions to hopefully
 * make it easier to generate the correct .java files using the VML generator.
 *
 * This is the list of Zeidon global operations that are defined in ZDRVROPR.h.
 *
 * @author DG
 *
 */
public class ActiveDirectory 
{
    
    public ActiveDirectory( )
    //public ZDRVROPR( View view )
    {
        super( );
    }
     	
    public int ActiveDirectorySetPassword( String stringADPathname, String stringADAdminUserName, String stringADAdminPassword,
	            String stringADUserName, String stringADPassword )
	{
    	int nRC;
    	try
    	{
	    	ldapAdminBind ctx = new ldapAdminBind(stringADPathname, stringADAdminUserName, stringADAdminPassword );
	    	nRC = ctx.updatePassword(stringADUserName, stringADPassword);
	    	if ( nRC < 0 )
	    		return -1;
    	}
    	catch (Exception e)
    	{
               System.out.println(e);
               return -1;
     	}    
    	return 0;
	}
	
    public int ActiveDirectoryChangePassword( String stringADPathname, String stringADAdminUserName, String stringADAdminPassword,
	               String stringADUserName, String stringADOldPassword, String stringADNewPassword )
	{
    	int nRC;
    	try
    	{
	    	ldapAdminBind ctx = new ldapAdminBind(stringADPathname, stringADAdminUserName, stringADAdminPassword );
	    	nRC = ctx.updatePassword(stringADUserName, stringADNewPassword);
	    	if ( nRC < 0 )
	    		return -1;
    	}
    	catch (Exception e)
    	{
               System.out.println(e);
               return -1;
     	}    
    	return 0;
	}
	
    public int ActiveDirectoryRemoveUser( String stringServerName, String stringServerPort, String stringOrganization,
	           String stringUserName )
	{
		// TODO So far, we are not using this operation.
		return 0;
	}
	
    public int ActiveDirectoryLoginAuthentication( String stringADPathname, String stringADUserName, String stringADPassword )
	{

    	boolean IsAuthenticated = false;
    	try
    	{
	    	ldapFastBind ctx = new ldapFastBind(stringADPathname);
	    	IsAuthenticated = ctx.Authenticate(stringADUserName, stringADPassword);
	    	ctx.finito();
	    	if ( IsAuthenticated )
	    		return 0;
	    	else
	    		return -1;
    	}
    	catch (Exception e)
    	{
            System.out.println(e);
           	return -1;	
     	}        	
    }
	
    // Trying to bind without fastbind.
    public int ActiveDirectoryLoginAuthenticationNF( String stringADPathname, String stringADUserName, String stringADPassword )
	{
    	boolean IsAuthenticated = false;
    	try
    	{
	    	ldapBind ctx = new ldapBind(stringADPathname);
	    	IsAuthenticated = ctx.Authenticate(stringADUserName, stringADPassword);
	    	if ( !IsAuthenticated )
	    		return -1;
	    	ctx.finito();
    	}
    	catch (Exception e)
    	{
           return -1;
     	}    
    	return 0;
    }
    public int ActiveDirectorySetProperty( String stringADPathname, String stringADAdminName, String stringADAdminPassword,
            String stringADUserName, String stringADPropertyName,
            String stringADPropertyValue )
	{
		// TODO So far, we are not using this operation.
		return 0;
	}
	
    public int ActiveDirectoryGetProperty( String stringADPathname, String stringADUserName, String stringADPassword,
	            String stringADProperty, StringBuilder sbReturnProperty )
	{
		// TODO So far, we are not using this operation.
		return 0;
	}
	
    public int ActiveDirectoryAddUser( String stringADPathname, String stringADLoginUserName, String stringADLoginPassword,
	        String stringADNewUserName, String stringADNewUserPassword )
	{
		// TODO So far, we are not using this operation.
		return 0;
	}
}
