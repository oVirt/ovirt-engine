package org.ovirt.engine.extensions.aaa.builtin.kerberosldap;

import java.util.Properties;
import java.util.concurrent.Callable;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;

import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;
import org.ovirt.engine.extensions.aaa.builtin.kerberosldap.utils.kerberos.AuthenticationResult;
import org.ovirt.engine.extensions.aaa.builtin.kerberosldap.utils.ldap.LdapProviderType;

public class GetRootDSETask implements Callable<Boolean> {

    private final DirectorySearcher searcher;
    private final String domainName;
    private final String ldapURI;
    private final Properties configuration;

    private static final Log log = LogFactory.getLog(GetRootDSETask.class);

    public GetRootDSETask(Properties configuration, DirectorySearcher searcher, String domainName, String ldapURI) {
        super();
        this.configuration = configuration;
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
        // If no domain can be found in the cache - it means it was not set
        // during system initialization and we will not query for rootDSE for it
        LdapProviderType ldapProviderType =
                LdapProviderType.valueOf(configuration.getProperty("config.LDAPProviderTypes"));
        RootDSE rootDSE = (RootDSE) configuration.get("config.rootDSE");
        // If no rootDSE is set for domain - try to set it - if in
        // construct a rootDSE object and provide a baseDN that assumes
        // that all users will be under "cn=users"
        if (rootDSE == null) {
            synchronized (configuration) {
                try {
                    rootDSE = (RootDSE) configuration.get("config.rootDSE");
                    if (rootDSE == null) {
                        GetRootDSE query = createGetRootDSE(ldapURI);
                        Attributes rootDseRecords = query.getDomainAttributes(ldapProviderType, domainName);
                        if (rootDseRecords != null) {
                            setRootDSE(ldapProviderType, rootDseRecords);
                            baseDNExist = true;
                        } else {
                            log.errorFormat("Couldn't deduce provider type for domain {0}", domainName);
                            throw new AuthenticationResultException(AuthenticationResult.CONNECTION_ERROR,
                                    "Failed to get rootDSE record for server " + ldapURI);
                        }
                    } else {
                        baseDNExist = true;
                    }
                } catch (Exception ex) {
                    log.error("", ex);
                }
            }
        } else {
            baseDNExist = true;
        }
        searcher.setBaseDNExist(baseDNExist);
        return baseDNExist;
    }

    protected GetRootDSE createGetRootDSE(String uri) {
        return new GetRootDSE(configuration, uri);
    }

    private void setRootDSE(LdapProviderType ldapProviderType, Attributes rootDseRecords)
            throws NamingException {
        RootDSE rootDSE;
        rootDSE = RootDSEFactory.get(ldapProviderType, rootDseRecords);
        configuration.put("config.rootDSE", rootDSE);
        configuration.put("config.LDAPProviderTypes", ldapProviderType.toString());
    }

}
