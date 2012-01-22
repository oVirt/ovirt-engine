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

import org.ovirt.engine.core.compat.LogCompat;
import org.ovirt.engine.core.compat.LogFactoryCompat;

/**
 * Anonymous query an LDAP server to get the rootDSE object. rootDSE provides data on the directory server. This query
 * is needed in order to fetch the base DN and the domain functionality level and does not require authentication
 */
public class GetRootDSE {
    private URI ldapURI;

    private Attributes attributes;

    private final static LogCompat log = LogFactoryCompat.getLog(GetRootDSE.class);

    public GetRootDSE(URI ldapURI) {
        this.ldapURI = ldapURI;
    }

    /**
     * Perform an LDAP query to the inner LDAP server to fetch the rootDSE table.
     *
     * @return EnumMap with the attributes as key and their string values.
     */
    private void execute(LdapProviderType ldapProviderType, String domain) {
        Hashtable<String, String> env = new Hashtable<String, String>();
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
            log.errorFormat("Failed to query rootDSE for LDAP server {0} due to {1}", ldapURI, e.getMessage());
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

    public LdapProviderType retrieveLdapProviderType(String domain) throws NamingException {
        LdapProviderType retVal = LdapProviderType.general;
        Attributes attributes = getDomainAttributes(LdapProviderType.general, domain);
        if (attributes != null) {
            if (attributes.get(ADRootDSEAttributes.domainControllerFunctionality.name()) != null) {
                retVal = LdapProviderType.activeDirectory;
            } else if (attributes.get(RHDSRootDSEAttributes.vendorName.name()) != null) {
                String vendorName = (String) attributes.get(RHDSRootDSEAttributes.vendorName.name()).get(0);
                if (vendorName.equals(LdapVendorNameEnum.IPAVendorName.getName())) {
                    retVal = LdapProviderType.ipa;
                } else if (vendorName.equals(LdapVendorNameEnum.RHDSVendorName.getName())) {
                    retVal = LdapProviderType.rhds;
                }
            }
        }

        return retVal;
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
