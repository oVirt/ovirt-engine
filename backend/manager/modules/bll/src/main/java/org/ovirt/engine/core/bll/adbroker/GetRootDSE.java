package org.ovirt.engine.core.bll.adbroker;

import java.net.URI;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.ldap.LdapProviderType;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;

/**
 * Anonymous query an LDAP server to get the rootDSE object. rootDSE provides data on the directory server. This query
 * is needed in order to fetch the base DN and the domain functionality level and does not require authentication
 */
public class GetRootDSE {
    private final URI ldapURI;

    private Attributes attributes;

    private final static Log log = LogFactory.getLog(GetRootDSE.class);

    public GetRootDSE(URI ldapURI) {
        this.ldapURI = ldapURI;
    }

    /**
     * Perform an LDAP query to the inner LDAP server to fetch the rootDSE table.
     */
    private void execute(LdapProviderType ldapProviderType, String domain) {
        Hashtable<String, String> env = new Hashtable<String, String>();
        LdapBrokerUtils.addLdapConfigValues(env);
        initContextVariables(env);

        Attributes results = null;

        DirContext ctx = null;
        try {
            ctx = createContext(env);
            LdapQueryData ldapQueryData = new LdapQueryDataImpl();
            ldapQueryData.setLdapQueryType(LdapQueryType.rootDSE);
            ldapQueryData.setDomain(domain);

            LdapQueryExecution queryExecution =
                    LdapQueryExecutionBuilderImpl.getInstance().build(ldapProviderType, ldapQueryData);

            SearchControls searchControls = new SearchControls();
            searchControls.setReturningAttributes(queryExecution.getReturningAttributes());
            searchControls.setSearchScope(queryExecution.getSearchScope());
            // Added this in order to prevent a warning saying: "the returning obj flag wasn't set, setting it to true"
            searchControls.setReturningObjFlag(true);
            searchControls.setTimeLimit(Config.<Integer> GetValue(ConfigValues.LDAPOperationTimeout) * 1000);
            NamingEnumeration<SearchResult> search =
                    ctx.search(queryExecution.getBaseDN(), queryExecution.getFilter(), searchControls);

            try {
                // build a map of attributes and their string values
                results = search.next().getAttributes();
            } finally {
                // make sure we close this search, otherwise the ldap connection will stick until GC kills it
                search.close();
            }

        } catch (NamingException e) {
            String message = LdapBrokerUtils.getFriendlyExceptionMessage(e);

            log.errorFormat("Failed to query rootDSE for LDAP server {0} due to {1}", ldapURI, message);

            throw new DirectoryServiceException(e);
        } finally {
            closeContext(ctx);
        }
        attributes = results;
    }

    /**
     * Bullet proof close of a director context. Possible exceptions are caught and logged.
     *
     * @param ctx
     *            a directory context, can be null
     */
    private void closeContext(final DirContext ctx) {
        log.trace("closing directory context");
        try {
            if (ctx != null) {
                ctx.close();
            }
        } catch (NamingException e) {
            // ignore, but log
            log.warn("Could not close directory context", e);
        }
    }

    protected DirContext createContext(Hashtable<String, String> env) throws NamingException {
        return new InitialDirContext(env);
    }

    /**
     * Try to detect which LDAP server type this domain is working with. Since the rootDSE attributes are not standard
     * nor compulsory the result is somewhat fragile, therefore deprecated.
     * @param domain
     * @return {@link LdapProviderType} of this domain.
     * @throws NamingException
     */
    @Deprecated
    public LdapProviderType autoDetectLdapProviderType(String domain) throws NamingException {
        log.infoFormat("Trying to auto-detect the LDAP provider type for domain {0}", domain);
        LdapProviderType retVal = LdapProviderType.general;
        Attributes attributes = getDomainAttributes(LdapProviderType.general, domain);
        if (attributes != null) {
            if (attributes.get(ADRootDSEAttributes.domainControllerFunctionality.name()) != null) {
                retVal = LdapProviderType.activeDirectory;
            } else if (attributes.get(RHDSRootDSEAttributes.vendorName.name()) != null) {
                String vendorName = (String) attributes.get(RHDSRootDSEAttributes.vendorName.name()).get(0);
                if (vendorName.equals(LdapProviderType.ipa.getLdapVendorName())) {
                    retVal = LdapProviderType.ipa;
                } else if (vendorName.equals(LdapProviderType.rhds.getLdapVendorName())) {
                    retVal = LdapProviderType.rhds;
                } else if (vendorName.equals(LdapProviderType.itds.getLdapVendorName())) {
                    retVal = LdapProviderType.itds;
                }
            }
        }
        log.infoFormat("Provider type is {0}", retVal.name());
        updateProviderTypeInConfig(domain, retVal.name());
        return retVal;
    }

    private void updateProviderTypeInConfig(String domain, String type) {
        String[] types = Config.<String> GetValue(ConfigValues.LDAPProviderTypes).split(",");
        for (int x = 0; x < types.length; x++) {
            if (types[x].startsWith(domain)) {
                types[x] = domain + ":" + type;
                break;
            }
        }
        Config.getConfigUtils().setStringValue(ConfigValues.LDAPProviderTypes.name(), StringUtils.join(types, ","));
    }

    public Attributes getDomainAttributes(LdapProviderType general, String domain) {
        if (attributes == null) {
            execute(general, domain);
        }
        return attributes;
    }

    protected URI getLdapURI() {
        return ldapURI;
    }

    private void initContextVariables(Hashtable<String, String> env) {
        env.put(Context.SECURITY_AUTHENTICATION, "SIMPLE");
        env.put(Context.SECURITY_PRINCIPAL, "");
        env.put(Context.SECURITY_CREDENTIALS, "");
        env.put(Context.REFERRAL, "follow");

        env.put(Context.INITIAL_CONTEXT_FACTORY,
                "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, getLdapURI().toString());
    }
}
