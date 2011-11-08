/**
 *
 */
package org.ovirt.engine.core.ldap;

import org.ovirt.engine.core.dns.DnsSRVLocator;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;

/**
 * This class is responsible for querying the AD global catalog
 *
 */
public class GlobalCatalogSrvLocator extends DnsSRVLocator {

    private static final String GLOBAL_CATALOG_DOMAIN_PREFIX = "gc._msdcs.";
    private static final String LDAP_PROTOCOL = "_ldap";

    public GlobalCatalogSrvLocator() {
    }

    public DnsSRVResult getGlobalCatalog(String domainName) {
        try {
            return getService(LDAP_PROTOCOL, TCP,  GLOBAL_CATALOG_DOMAIN_PREFIX + domainName);
        } catch (Exception e) {
            log.error("Error in getting global catalog for " + domainName);
            return null;
        }
    }

    private static Log log = LogFactory.getLog(GlobalCatalogSrvLocator.class);

}
