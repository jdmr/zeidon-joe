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
 * Copyright 2009-2014 QuinSoft
 */
package com.quinsoft.zeidon.zeidonoperations;

/*
 * This code was developed by Jeremy Mortis here is link to the original code
 * http://blogs.msdn.com/b/alextch/archive/2012/05/15/how-to-set-active-directory-password-from-java-application.aspx
 */
import javax.naming.*;
import javax.naming.directory.*;
import java.util.*;

public class ldapAdminBind {

    DirContext ldapContext;
    public Hashtable<String, String> ldapEnv = null;

    public ldapAdminBind(String ldapurl, String strAdminUser, String strAdminPassword) {
        try {
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
        } catch (Exception e) {
            System.out.println(" bind error: " + e);
            e.printStackTrace();
        }
    }

    public int updatePassword(String username, String password) {
        try {
            String quotedPassword = "\"" + password + "\"";
            char unicodePwd[] = quotedPassword.toCharArray();
            byte pwdArray[] = new byte[unicodePwd.length * 2];
            for (int i = 0; i < unicodePwd.length; i++) {
                pwdArray[i * 2 + 1] = (byte) (unicodePwd[i] >>> 8);
                pwdArray[i * 2 + 0] = (byte) (unicodePwd[i] & 0xff);
            }
            ModificationItem[] mods = new ModificationItem[1];
            mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
                    new BasicAttribute("UnicodePwd", pwdArray));
            SearchControls ctls = new SearchControls();
            ctls.setSearchScope(SearchControls.SUBTREE_SCOPE);
            String filter = "(&(objectCategory=Person)(objectclass=User)(sAMAccountName=" + username + "))";

            // Search for the object using the username (sAMAccountName), under the following path.
            NamingEnumeration answer = ldapContext.search("DC=AD,DC=SWAU,DC=EDU", filter, ctls);
            if (answer.hasMore()) {
                SearchResult sr = (SearchResult) answer.next();
                Attributes attrs = sr.getAttributes();
                Attribute dn = attrs.get("distinguishedName");
                String dnValue = (String) dn.get(0);
                System.out.println(dnValue);
                ldapContext.modifyAttributes(dnValue, mods);
                return 0;
            }

            // If we get here, then the username was not found in ActiveDirectory.
            // Return an error.
            return -1;
        } catch (Exception e) {
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
