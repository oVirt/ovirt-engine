package org.ovirt.engine.core.bll.adbroker;

import java.net.URI;
import java.util.Hashtable;

import javax.naming.directory.SearchControls;

import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.ldap.LdapProviderType;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;
import org.springframework.ldap.control.PagedResultsCookie;
import org.springframework.ldap.control.PagedResultsDirContextProcessor;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.NameClassPairCallbackHandler;
import org.springframework.ldap.core.support.DirContextAuthenticationStrategy;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.ldap.core.support.SingleContextSource;

public abstract class LDAPTemplateWrapper {

    private static Log log = LogFactory.getLog(LDAPTemplateWrapper.class);

    protected LdapTemplate ldapTemplate;
    protected LdapContextSource contextSource;
    protected String password;
    protected String userName;
    protected DirContextAuthenticationStrategy authStrategy;
    protected String baseDN;
    protected String domain;
    protected boolean explicitAuth;

    public abstract void search(String baseDN, String filter, String displayFilter, SearchControls searchControls,
            NameClassPairCallbackHandler handler);

    public LDAPTemplateWrapper(LdapContextSource contextSource, String userName, String password, String domain) {
        this.contextSource = contextSource;
        ldapTemplate = new LdapTemplate(this.contextSource);
        this.userName = userName;
        this.password = password;
        this.domain = domain;
        this.baseDN = "";

    }

    public void init(URI ldapURI,
            boolean setBaseDN,
            boolean explicitAuth,
            String explicitBaseDN,
            LdapProviderType ldapProviderType, long timeout) {
        this.explicitAuth = explicitAuth;
        if (explicitBaseDN != null) {
            this.baseDN = explicitBaseDN;
        } else if (!domain.isEmpty() && setBaseDN) {
            this.baseDN = getBaseDNForDomain(domain);
        }

        adjustUserName(ldapProviderType);

        this.contextSource.setUrl(ldapURI.toString());

        setCredentialsOnContext();
        contextSource.setBase(baseDN);
        contextSource.setContextFactory(com.sun.jndi.ldap.LdapCtxFactory.class);

        // binary properties
        Hashtable<String, String> baseEnvironmentProperties =  new Hashtable<String, String>();
        // objectGUID
        baseEnvironmentProperties.put("java.naming.ldap.attributes.binary", "objectGUID");
        LdapBrokerUtils.addLdapConfigValues(baseEnvironmentProperties);
        contextSource.setBaseEnvironmentProperties(baseEnvironmentProperties);
    }

    protected abstract void setCredentialsOnContext();

    public abstract void adjustUserName(LdapProviderType ldapProviderType);

    public void setIgnorePartialResultException(boolean value) {
        ldapTemplate.setIgnorePartialResultException(value);
    }

    public void useAuthenticationStrategy() throws AuthenticationResultException {
        authStrategy = buildContextAuthenticationStategy();
        contextSource.setAuthenticationStrategy(authStrategy);
    }

    protected abstract DirContextAuthenticationStrategy buildContextAuthenticationStategy();

    private String getBaseDNForDomain(String domainName) {

        if (domainName == null) {
            return null;
        }

        Domain domain = UsersDomainsCacheManagerService.getInstance().getDomain(domainName);
        if (domain == null) {
            log.errorFormat("The domain {0} does not exist in the configuration. A base DN cannot be configured for it",
                    domainName);
            return null;
        }
        RootDSE rootDSE = domain.getRootDSE();
        if (rootDSE != null) {
            return rootDSE.getDefaultNamingContext();
        }
        return null;
    }

    public void setExplicitAuth(boolean explicitAuth) {
        this.explicitAuth = explicitAuth;

    }

    protected NameClassPairCallbackHandler pagedSearch(String baseDN,
            String filter,
            String displayFilter,
            SearchControls searchControls,
            NameClassPairCallbackHandler handler) {

        /*
         * once a SingleContextSource is constructed, it must be destroyed (see below), otherwise only the garbage
         * collector will close its connection.
         */
        final SingleContextSource singleContextSource =
                new SingleContextSource(contextSource.getContext(userName, password));
        try {
            ldapTemplate.setContextSource(singleContextSource);

            if (log.isDebugEnabled()) {
                log.debugFormat("LDAP query is {0}", displayFilter);
            }
            int ldapPageSize = Config.<Integer> GetValue(ConfigValues.LdapQueryPageSize);
            PagedResultsDirContextProcessor requestControl = new PagedResultsDirContextProcessor(ldapPageSize);
            ldapTemplate.search(baseDN, filter, searchControls, handler, requestControl);
            PagedResultsCookie cookie = requestControl.getCookie();
            while (cookie != null) {
                byte[] cookieBytes = cookie.getCookie();
                if (cookieBytes == null) {
                    break;
                }
                requestControl = new PagedResultsDirContextProcessor(ldapPageSize, cookie);
                ldapTemplate.search(baseDN, filter, searchControls, handler, requestControl);
                cookie = requestControl.getCookie();
            }

        } catch (Exception ex) {
            log.errorFormat("Error in running LDAP query. BaseDN is {0}, filter is {1}. Exception message is: {2}",
                    baseDN,
                    displayFilter,
                    ex.getMessage());
            if (log.isDebugEnabled()) {
                log.debug("Exception stacktrace: ", ex);
            }
            handleException(ex);
        } finally {
            singleContextSource.destroy();
        }
        return handler;
    }

    /**
     * @param ex
     */
    private Throwable handleException(Exception e) {
        if (e instanceof RuntimeException) {
            throw (RuntimeException) e;
        }
        return e;
    }
}
