package org.ovirt.engine.extensions.aaa.builtin.kerberosldap;

import org.springframework.ldap.core.ContextMapper;

public class LdapQueryExecution {

    private String filter;
    private String displayFilter; // The display filter is used for logging purposes, in cases where the filter is
                                  // unreadable
    private String baseDN;
    private ContextMapper contextMapper;
    private int searchScope;
    private String[] returningAttributes;
    private String domain;

    public LdapQueryExecution(String filter,
            String displayFilter,
            String baseDN,
            ContextMapper contextMapper,
            int searchScope,
            String[] returningAttributes,
            String domain) {

        this.filter = filter;
        this.displayFilter = displayFilter;
        this.baseDN = baseDN;
        this.contextMapper = contextMapper;
        this.searchScope = searchScope;
        this.returningAttributes = returningAttributes;
        this.domain = domain;
    }

    public String getFilter() {
        return filter;
    }

    public String getDisplayFilter() {
        return displayFilter;
    }

    public String getBaseDN() {
        return baseDN;
    }

    public ContextMapper getContextMapper() {
        return contextMapper;
    }

    public int getSearchScope() {
        return searchScope;
    }

    public String[] getReturningAttributes() {
        return returningAttributes;
    }

    public String getDomain() {
        return domain;
    }
}
