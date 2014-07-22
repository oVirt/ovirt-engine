package org.ovirt.engine.extensions.aaa.builtin.kerberosldap.utils.kerberos;

import org.ovirt.engine.extensions.aaa.builtin.kerberosldap.utils.dns.DnsSRVLocator;

/**
 * KDCs locator helper class. This class is used in order to locate KDCs in the DNS (based on a given realm). For each
 * KDC there are SRV records in DNS , providing information on the UDP and TCP ports it is using RFC 2782 defines an
 * algorithm that is used to order the KDCs for a given realm.
 **/

public class KDCLocator extends DnsSRVLocator {

    public DnsSRVResult getKdc(String protocol, String realmName) throws Exception {
        return getService("_kerberos", protocol, realmName);

    }

    public DnsSRVResult getKdc(String[] records, String realmName) {
        return getSRVResult(realmName, records);
    }
}
