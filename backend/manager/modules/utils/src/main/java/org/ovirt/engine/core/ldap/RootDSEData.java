package org.ovirt.engine.core.ldap;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

public class RootDSEData {
    private String domainDN = null;
    private boolean isIPA = false;

    public RootDSEData(DirContext dirContext) throws NamingException {
        // Queries the rootDSE and get the "defaultNamingContext" attribute value -
        // this attribute will be a part of the LDAP URL to perform users queries (i.e - search for a user)
        SearchControls controls = RootDSEQueryInfo.createSearchControls();
        String query = RootDSEQueryInfo.ROOT_DSE_LDAP_QUERY;
        NamingEnumeration<SearchResult> searchResults = dirContext.search("", query, controls);

        // The information on base DN is located in the attribute "defaultNamingContext"
        while (searchResults.hasMoreElements()) {
            SearchResult searchResult = searchResults.nextElement();
            Attributes attributes = searchResult.getAttributes();
            Attribute attribute = attributes.get(RootDSEQueryInfo.DEFAULT_NAMING_CONTEXT_RESULT_ATTRIBUTE);
            if (attribute != null) {
                domainDN = (String) attribute.get();
            } else {
                Attribute ipaAttribute = attributes.get(RootDSEQueryInfo.NAMING_CONTEXTS_RESULT_ATTRIBUTE);
                if (ipaAttribute != null) {
                    isIPA = true;
                    domainDN = (String) ipaAttribute.get(0);
                }
            }
        }
    }

    public String getDomainDN() {
        return domainDN;
    }

    public void setDomainDN(String domainDN) {
        this.domainDN = domainDN;
    }

    public boolean isIPA() {
        return isIPA;
    }

    public void setIPA(boolean isIPA) {
        this.isIPA = isIPA;
    }

}
