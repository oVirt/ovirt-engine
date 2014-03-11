package org.ovirt.engine.extensions.aaa.builtin.kerberosldap;

public class LdapSearchByQueryParameters extends LdapBrokerBaseParameters {
    private LdapQueryData ldapQueryData;

    public LdapQueryData getLdapQueryData() {
        return ldapQueryData;
    }

    public void setLdapQueryData(LdapQueryData ldapQueryData) {
        this.ldapQueryData = ldapQueryData;
    }

    public LdapSearchByQueryParameters(String domain, LdapQueryData ldapQueryData) {
        super(domain);
        setLdapQueryData(ldapQueryData);
    }

    public LdapSearchByQueryParameters(String sessionId, String domain, LdapQueryData ldapQueryData) {
        super(sessionId, domain);
        setLdapQueryData(ldapQueryData);
    }

}
