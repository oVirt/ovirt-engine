package org.ovirt.engine.extensions.aaa.builtin.kerberosldap;

import java.util.Properties;

public class LdapSearchByQueryParameters extends LdapBrokerBaseParameters {
    private LdapQueryData ldapQueryData;
    private boolean populateGroups;

    public LdapQueryData getLdapQueryData() {
        return ldapQueryData;
    }

    public void setLdapQueryData(LdapQueryData ldapQueryData) {
        this.ldapQueryData = ldapQueryData;
    }

    public LdapSearchByQueryParameters(Properties configuration, String domain, LdapQueryData ldapQueryData) {
        super(configuration, domain);
        setLdapQueryData(ldapQueryData);
    }

    public LdapSearchByQueryParameters(Properties configuration,
            String sessionId,
            String domain,
            LdapQueryData ldapQueryData) {
        this(configuration, sessionId, domain, ldapQueryData, false);
    }

    public LdapSearchByQueryParameters(Properties configuration,
            String sessionId,
            String domain,
            LdapQueryData ldapQueryData,
            boolean populateGroups) {
        super(configuration, sessionId, domain);
        setLdapQueryData(ldapQueryData);
        this.populateGroups = populateGroups;
    }

    boolean isPopulateGroups() {
        return populateGroups;
    }

}
