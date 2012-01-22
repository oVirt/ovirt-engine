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
    private LdapProviderType ldapProviderType = null;
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
            ldapProviderType = LdapProviderType.activeDirectory;
            retVal = true;
        } else {
            Attribute namingContextAttribute = attributes.get(RootDSEQueryInfo.NAMING_CONTEXTS_RESULT_ATTRIBUTE);
            Attribute vendorNameAttribute = attributes.get(RootDSEQueryInfo.PROVIDER_TYPE_PROPERTY);
            if (namingContextAttribute != null) {
                domainDN = getDefaultNamingContextFromNamingContexts(namingContextAttribute);
                String vendorName = (String) vendorNameAttribute.get(0);
                if (vendorName.equals(LdapVendorNameEnum.IPAVendorName.getName())) {
                    ldapProviderType = LdapProviderType.ipa;
                } else if (vendorName.equals(LdapVendorNameEnum.RHDSVendorName.getName())) {
                    ldapProviderType = LdapProviderType.rhds;
                }
                retVal = true;
            }
        }

        return retVal;
    }
    public RootDSEData(DirContext dirContext) throws NamingException {
        // Queries the rootDSE and get the "defaultNamingContext" attribute value -
        // this attribute will be a part of the LDAP URL to perform users queries (i.e - search for a user)
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
    }

    public String getDomainDN() {
        return domainDN;
    }

    public void setDomainDN(String domainDN) {
        this.domainDN = domainDN;
    }

    public LdapProviderType getLdapProviderType() {
        return ldapProviderType;
    }

    public void setLdapProviderType(LdapProviderType ldapProviderType) {
        this.ldapProviderType = ldapProviderType;
    }

}
