package org.ovirt.engine.core.bll.adbroker;

import org.springframework.ldap.core.ContextMapper;

public class LdapQueryMetadataImpl implements LdapQueryMetadata {

    private String filter;
    private String baseDN;
    private ContextMapper contextMapper;
    private int searchScope;
    private String[] returningAttributes;
    private LdapGuidEncoder ldapGuidEncoder;
    private LdapQueryFormatter<LdapQueryExecution> formatter;
    private LdapQueryData queryData;

    public LdapQueryMetadataImpl(String filter,
            String baseDN,
            ContextMapper contextMapper,
            int searchScope,
            String[] returningAttributes,
            LdapQueryFormatter<LdapQueryExecution> formatter,
            LdapGuidEncoder ldapGuidEncoder ) {

        this.filter = filter;
        this.baseDN = baseDN;
        this.contextMapper = contextMapper;
        this.searchScope = searchScope;
        this.returningAttributes = returningAttributes;
        this.formatter = formatter;
        this.ldapGuidEncoder = ldapGuidEncoder;
    }

    @Override
    public String getFilter() {
        return filter;
    }

    @Override
    public void setFilter(String filter) {
        this.filter = filter;
    }

    @Override
    public String getBaseDN() {
        return baseDN;
    }

    @Override
    public void setBaseDN(String baseDN) {
        this.baseDN = baseDN;
    }

    @Override
    public ContextMapper getContextMapper() {
        return contextMapper;
    }

    @Override
    public void setContextMapper(ContextMapper contextMapper) {
        this.contextMapper = contextMapper;
    }

    @Override
    public int getSearchScope() {
        return searchScope;
    }

    @Override
    public void setSearchScope(int searchScope) {
        this.searchScope = searchScope;
    }

    @Override
    public String[] getReturningAttributes() {
        return returningAttributes;
    }

    @Override
    public void setReturningAttributes(String[] returningAttributes) {
        this.returningAttributes = returningAttributes;
    }

    @Override
    public LdapGuidEncoder getLdapGuidEncoder() {
        return ldapGuidEncoder;
    }

    @Override
    public void setLdapGuidEncoder(LdapGuidEncoder ldapGuidEncoder) {
        this.ldapGuidEncoder = ldapGuidEncoder;
    }

    @Override
    public LdapQueryFormatter<LdapQueryExecution> getFormatter() {
        return this.formatter;
    }

    public void setFormatter(LdapQueryFormatter<LdapQueryExecution> formatter) {
        this.formatter = formatter;
    }

    @Override
    public LdapQueryData getQueryData() {
        return this.queryData;
    }

    @Override
    public void setQueryData(LdapQueryData queryData) {
        this.queryData = queryData;
    }
}
