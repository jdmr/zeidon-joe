package com.quinsoft.zeidon.zeidonoperations;

/*
 * This code was developed by Jeremy Mortis here is link to the original code
 * http://blogs.msdn.com/b/alextch/archive/2012/05/15/how-to-set-active-directory-password-from-java-application.aspx
 */

import javax.naming.*;
import javax.naming.directory.*;
import javax.naming.ldap.*;
import java.util.*;
import java.security.*;

public class ldapAdminBind 
{
	DirContext ldapContext;
	public Hashtable<String, String> ldapEnv = null;
	
	public ldapAdminBind(String ldapurl, String strAdminUser, String strAdminPassword) 
	{
		try 
		{
			ldapEnv = new Hashtable<String, String>();
			ldapEnv.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
			ldapEnv.put(Context.PROVIDER_URL, ldapurl);
			ldapEnv.put(Context.SECURITY_AUTHENTICATION, "simple");
			ldapEnv.put(Context.SECURITY_PRINCIPAL, strAdminUser);
			ldapEnv.put(Context.SECURITY_CREDENTIALS, strAdminPassword);
			//ldapEnv.put(Context.SECURITY_PRINCIPAL, "enc-ad\\zmail");
			//ldapEnv.put(Context.SECURITY_CREDENTIALS, "F82b7mk,9j");
			//ldapEnv.put(Context.SECURITY_PROTOCOL, "ssl");
			ldapContext = new InitialDirContext(ldapEnv);
		}
		catch (Exception e) 
		{
			System.out.println(" bind error: " + e);
			e.printStackTrace();
			System.exit(-1);
		}
	}
	public int updatePassword(String username, String password) 
	{
		try 
		{
			SearchControls ctls = new SearchControls();
			//ctls.setReturningAttributes(attrIDs);
			ctls.setSearchScope(SearchControls.SUBTREE_SCOPE);
			String filter = "(&(objectCategory=Person)(objectclass=User)(sAMAccountName=" + username + "))";

			// Search for the object using the username (sAMAccountName), under the following path.
			NamingEnumeration answer = ldapContext.search("OU=ENC,DC=enc-ad,DC=enc,DC=edu", filter, ctls);
			while (answer.hasMore()) 
			{  
				// Username was found. We need to get the user's "distinguishedName" in order to be
				// able to do a modifyAttribute for this user.
	            SearchResult sr = (SearchResult) answer.next();  
	            Attributes attrs = sr.getAttributes();
	            Attribute dn = attrs.get("distinguishedName");
	            String dnValue = (String) dn.get(0);
	            Attribute psw = attrs.get("userPassword");
	            psw.set(0, password);
	            //dn.set(0, "NewPassword");
	            // Loops through attributes to see what they are...
	            /*
	    	    NamingEnumeration ae = attrs.getAll(); 
	    		   
	    	    while (ae.hasMoreElements()) {
	    	        Attribute attr =(Attribute)ae.next();
	    	 
	    	        String id = attr.getID();
	    	        NamingEnumeration vals = attr.getAll();
	    	        while (vals.hasMoreElements())
	    	            System.out.println("   "+id + ": " + vals.nextElement());
	    	    }
	    	    */	              

	            // LOOK AT THE FOLLOWING LINK WHICH SAYS I CAN NOT UPDATE PASSWORD W/O SSL OR TLS OR SOMETHING
	            //https://forums.oracle.com/forums/thread.jspa?messageID=4693664&#4693664
				String quotedPassword = "\"" + password + "\"";
				/* Would I need the following code? Or the UTF-16LE works fine.
				char unicodePwd[] = quotedPassword.toCharArray();
				byte pwdArray[] = new byte[unicodePwd.length * 2];
				for (int i=0; i<unicodePwd.length; i++) 
				{
					pwdArray[i*2 + 1] = (byte) (unicodePwd[i] >>> 8);
					pwdArray[i*2 + 0] = (byte) (unicodePwd[i] & 0xff);
				}
				*/ 
				byte[] newUnicodePassword = quotedPassword.getBytes("UTF-16LE");
		
				ModificationItem[] mods = new ModificationItem[1];
				mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
				//new BasicAttribute("userPassword", "Test4444"));
				//new BasicAttribute("userPassword", "F82b7mk,9j"));
				//new BasicAttribute("userPassword", newUnicodePassword));
				new BasicAttribute("userPassword", password));
				//new BasicAttribute("sn", "TestLN"));
				//"CN=Test Z,OU=Infrastructure,OU=Users,OU=ENC,DC=enc-ad,DC=enc,DC=edu"
				ldapContext.modifyAttributes(dnValue, mods);
				return 0;
	        }  	

            // If we get here, then the username was not found in ActiveDirectory.
			// Return an error.
			return -1;
		}
		catch (Exception e) 
		{
			System.out.println("update password error: " + e);
			return -1;
		}
	}
	
	/*
	public static void main(String[] args) 
	{
		Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
		// the keystore that holds trusted root certificates
		System.setProperty("javax.net.ssl.trustStore", "c:\\myCaCerts.jks");
		System.setProperty("javax.net.debug", "all");
		//ADConnection adc = new ADConnection();
		//adc.updatePassword("Java User2", "pass@word3");
	}
	*/
}

/*      
        ModificationItem[] mods = new ModificationItem[2];

        mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute("unicodePwd", unicodePassword));

            mods[1] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute("userAccountControl", Integer.toString(Agent.UF_NORMAL_ACCOUNT + Agent.UF_DONT_EXPIRE_PASSWD)));

        modifySecurely(agent, mods);
        */
