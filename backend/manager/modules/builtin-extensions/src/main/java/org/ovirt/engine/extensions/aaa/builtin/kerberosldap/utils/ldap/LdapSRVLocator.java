package org.ovirt.engine.extensions.aaa.builtin.kerberosldap.utils.ldap;

import org.ovirt.engine.extensions.aaa.builtin.kerberosldap.utils.dns.DnsSRVLocator;

/**
 * This class is responsible to return SRV records for ldap servers in a domain
 */
public class LdapSRVLocator extends DnsSRVLocator {

    private static final String LDAP_PROTOCOL = "_ldap";

    /**
     *
     */
    public LdapSRVLocator() {
    }

    public DnsSRVResult getLdapServers(String domainName) throws Exception {
        return getService(LDAP_PROTOCOL, TCP, domainName);

    }

    public DnsSRVResult getLdapServers(String[] records, String domainName) {
        return getSRVResult(domainName, records);
    }

}
