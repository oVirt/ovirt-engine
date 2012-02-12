package org.ovirt.engine.core.ldap;

import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;
import org.ovirt.engine.core.dns.DnsSRVLocator;

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

    public DnsSRVResult getLdapServers(String[] records) {
        return getSRVResult(records);

    }

    private static Log log = LogFactory.getLog(LdapSRVLocator.class);

}
