package org.ovirt.engine.extensions.aaa.builtin.kerberosldap.utils.ldap;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

public class RootDSEData {
    private String domainDN = null;
    private final static String RHDS_NAMING_CONTEXT = "o=netscaperoot";

    public static String getDefaultNamingContextFromNamingContexts(Attribute namingContexts) {
        for (int index = 0; index < namingContexts.size(); ++index) {
            String namingContext;
            try {
                namingContext = (String) namingContexts.get(index);
            } catch (NamingException e) {
                return null;
            }
            if (!RHDS_NAMING_CONTEXT.equalsIgnoreCase(namingContext)) {
                return namingContext;
            }
        }
        return null;
    }

    public boolean applyDomainAttributes(Attributes attributes) throws NamingException {
        boolean retVal = false;
        Attribute attribute = attributes.get(RootDSEQueryInfo.DEFAULT_NAMING_CONTEXT_RESULT_ATTRIBUTE);

        if (attribute != null) {
            domainDN = (String) attribute.get();
            retVal = true;
        } else {
            Attribute namingContextAttribute = attributes.get(RootDSEQueryInfo.NAMING_CONTEXTS_RESULT_ATTRIBUTE);
            if (namingContextAttribute != null) {
                domainDN = getDefaultNamingContextFromNamingContexts(namingContextAttribute);
                retVal = true;
            }
        }

        return retVal;
    }

    public RootDSEData(String url) throws NamingException {
        // Queries the rootDSE and get the "defaultNamingContext" attribute value -
        // this attribute will be a part of the LDAP URL to perform users queries (i.e - search for a user)
        DirContext dirContext = null;
        Hashtable<String, String> env = new Hashtable<>();
        env.put(Context.SECURITY_AUTHENTICATION, "SIMPLE");
        env.put(Context.SECURITY_PRINCIPAL, "");
        env.put(Context.SECURITY_CREDENTIALS, "");
        env.put(Context.REFERRAL, "follow");
        env.put(Context.PROVIDER_URL, url);
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        try {
            dirContext = new InitialDirContext(env);
            SearchControls controls = RootDSEQueryInfo.createSearchControls();
            String query = RootDSEQueryInfo.ROOT_DSE_LDAP_QUERY;
            NamingEnumeration<SearchResult> searchResults = dirContext.search("", query, controls);

            boolean succeeded = false;
            // The information on base DN is located in the attribute "defaultNamingContext"
            while (searchResults.hasMoreElements() && !succeeded) {
                SearchResult searchResult = searchResults.nextElement();
                Attributes attributes = searchResult.getAttributes();
                succeeded = applyDomainAttributes(attributes);
            }

        } finally {
            if (dirContext != null) {
                dirContext.close();
            }
        }
    }

    public String getDomainDN() {
        return domainDN;
    }

    public void setDomainDN(String domainDN) {
        this.domainDN = domainDN;
    }

}
