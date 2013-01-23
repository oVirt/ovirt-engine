package org.ovirt.engine.core.bll.adbroker;

import java.net.URI;
import java.util.concurrent.Callable;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;

import org.ovirt.engine.core.ldap.LdapProviderType;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;
import org.ovirt.engine.core.utils.kerberos.AuthenticationResult;

public class GetRootDSETask implements Callable<Boolean> {

    private final DirectorySearcher searcher;
    private final String domainName;
    private final URI ldapURI;

    private static final Log log = LogFactory.getLog(GetRootDSETask.class);

    public GetRootDSETask(DirectorySearcher searcher, String domainName, URI ldapURI) {
        super();
        this.searcher = searcher;
        this.domainName = domainName;
        this.ldapURI = ldapURI;
    }


    /**
     * Sets a base DN for the domain parameter of directory searcher and also sets a flag in directory searcher for
     * later usage to determine if baseDN can be set (rootDSE is needed for baseDN calculation).
     *
     * @param ldapUri
     * @throws Exception
     * @throws NumberFormatException
     * @return true when base DN was found and set for this domain based on the root DSE response
     */
    @Override
    public Boolean call() throws Exception {
        boolean baseDNExist = false;
        Domain domainObject = searcher.getDomainObject(domainName);
        // If no domain can be found in the cache - it means it was not set
        // during system initialization and we will not query for rootDSE for it
        if (domainObject == null) {
            log.errorFormat("No domain object was obtained for domain {0} - this domain is probably not configured in the database",
                    domainName);
            baseDNExist = false;
            throw new DomainNotConfiguredException(domainName);
        } else {
            LdapProviderType ldapProviderType = domainObject.getLdapProviderType();
            RootDSE rootDSE = domainObject.getRootDSE();
            // If no rootDSE is set for domain - try to set it - if in
            // construct a rootDSE object and provide a baseDN that assumes
            // that all users will be under "cn=users"
            if (rootDSE == null) {
                domainObject.getLock().writeLock().lock();
                try {
                    rootDSE = domainObject.getRootDSE();
                    if (rootDSE == null) {
                        GetRootDSE query = createGetRootDSE(ldapURI);
                        ldapProviderType =
                                (ldapProviderType == LdapProviderType.general ? query.autoDetectLdapProviderType(domainName)
                                        : ldapProviderType);
                        if (ldapProviderType != LdapProviderType.general) {
                            Attributes rootDseRecords = query.getDomainAttributes(ldapProviderType, domainName);
                            if (rootDseRecords != null) {
                                setRootDSE(domainObject, ldapProviderType, rootDseRecords);
                                baseDNExist = true;
                            }
                        } else {
                            log.errorFormat("Couldn't deduce provider type for domain {0}", domainName);
                            throw new AuthenticationResultException(AuthenticationResult.CONNECTION_ERROR,
                                    "Failed to get rootDSE record for server " + ldapURI);
                        }
                    } else {
                        baseDNExist = true;
                    }
                } finally {
                    domainObject.getLock().writeLock().unlock();
                }
            } else {
                baseDNExist = true;
            }
        }
        searcher.setBaseDNExist(baseDNExist);
        return baseDNExist;
    }

    protected GetRootDSE createGetRootDSE(URI uri) {
        return new GetRootDSE(uri);
    }

    private void setRootDSE(Domain domainObject, LdapProviderType ldapProviderType, Attributes rootDseRecords)
            throws NamingException {
        RootDSE rootDSE;
        rootDSE = RootDSEFactory.get(ldapProviderType, rootDseRecords);
        domainObject.setRootDSE(rootDSE);
        domainObject.setLdapProviderType(ldapProviderType);
    }

}
