/**
 *
 */
package org.ovirt.engine.extensions.aaa.builtin.kerberosldap.utils.ldap;

import javax.naming.directory.SearchControls;

/**
 * RootDSEQueryInfo is a helper class to provide necessary information to perform RootDSE ldap queries
 */
public class RootDSEQueryInfo {

    public static final String ROOT_DSE_LDAP_QUERY = "(objectclass=*)";
    public static final String DEFAULT_NAMING_CONTEXT_RESULT_ATTRIBUTE = "defaultNamingContext";
    public static final String NAMING_CONTEXTS_RESULT_ATTRIBUTE = "NamingContexts";
    // Property used to auto-identify RHDS ldap provider
    public static final String PROVIDER_TYPE_PROPERTY = "vendorName";

    /**
     * Creates search controls object for the purpose of ROOT DSE query
     * @return
     */
    public static SearchControls createSearchControls() {
        String[] returnAttributes = {NAMING_CONTEXTS_RESULT_ATTRIBUTE, DEFAULT_NAMING_CONTEXT_RESULT_ATTRIBUTE};
        SearchControls searchControls = new SearchControls();
        searchControls.setSearchScope(SearchControls.OBJECT_SCOPE);
        searchControls.setReturningAttributes(returnAttributes);
        // Added this in order to prevent a warning saying: "the returning obj flag wasn't set, setting it to true"
        searchControls.setReturningObjFlag(true);
        return searchControls;
    }

}
