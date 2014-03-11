package org.ovirt.engine.extensions.aaa.builtin.kerberosldap;

public class LdapQueryDataImpl implements LdapQueryData {

    private LdapQueryType ldapQueryType;
    private Object[] filterParameters;
    private Object[] baseDNParameters;
    private String domain;

    @Override
    public LdapQueryType getLdapQueryType() {
        return ldapQueryType;
    }

    @Override
    public void setLdapQueryType(LdapQueryType ldapQueryType) {
        this.ldapQueryType = ldapQueryType;
    }

    @Override
    public void setFilterParameters(Object[] filterParameters) {
        this.filterParameters = filterParameters;
    }

    @Override
    public Object[] getFilterParameters() {
        return filterParameters;
    }

    @Override
    public void setBaseDNParameters(Object[] baseDNParameters) {
        this.baseDNParameters = baseDNParameters;
    }

    @Override
    public Object[] getBaseDNParameters() {
        return baseDNParameters;
    }

    @Override
    public String getDomain() {
        return domain;
    }

    @Override
    public void setDomain(String domain) {
        this.domain = domain;
    }

}
