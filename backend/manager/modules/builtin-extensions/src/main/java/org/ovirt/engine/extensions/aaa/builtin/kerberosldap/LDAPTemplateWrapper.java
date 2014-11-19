package org.ovirt.engine.extensions.aaa.builtin.kerberosldap;

import java.util.Hashtable;
import java.util.Properties;

import javax.naming.directory.SearchControls;

import com.sun.jndi.ldap.LdapCtxFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ldap.control.PagedResultsCookie;
import org.springframework.ldap.control.PagedResultsDirContextProcessor;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.NameClassPairCallbackHandler;
import org.springframework.ldap.core.support.DirContextAuthenticationStrategy;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.ldap.core.support.SingleContextSource;

import org.ovirt.engine.extensions.aaa.builtin.kerberosldap.utils.ldap.LdapProviderType;

public abstract class LDAPTemplateWrapper {

    private static final Logger log = LoggerFactory.getLogger(LDAPTemplateWrapper.class);

    protected LdapTemplate ldapTemplate;
    protected LdapContextSource contextSource;
    protected String password;
    protected String userName;
    protected DirContextAuthenticationStrategy authStrategy;
    protected String baseDN;
    protected String domain;
    protected Properties configuration;

    public abstract void search(String baseDN, String filter, String displayFilter, SearchControls searchControls,
            NameClassPairCallbackHandler handler);

    public LDAPTemplateWrapper(Properties configuration,
            LdapContextSource contextSource,
            String userName,
            String password,
            String domain) {
        this.contextSource = contextSource;
        ldapTemplate = new LdapTemplate(this.contextSource);
        this.userName = userName;
        this.password = password;
        this.domain = domain;
        this.baseDN = "";
        this.configuration = configuration;

    }

    public void init(String ldapURI,
            boolean setBaseDN,
            String explicitBaseDN,
            LdapProviderType ldapProviderType, long timeout) {
        if (explicitBaseDN != null) {
            this.baseDN = explicitBaseDN;
        } else if (!domain.isEmpty() && setBaseDN) {
            this.baseDN = getBaseDNForDomain();
        }

        adjustUserName(ldapProviderType);

        this.contextSource.setUrl(ldapURI.toString());

        setCredentialsOnContext();
        contextSource.setBase(baseDN);
        contextSource.setContextFactory(LdapCtxFactory.class);

        // binary properties
        Hashtable<String, String> baseEnvironmentProperties =  new Hashtable<String, String>();
        // objectGUID
        baseEnvironmentProperties.put("java.naming.ldap.attributes.binary", "objectGUID");
        LdapBrokerUtils.addLdapConfigValues(configuration, baseEnvironmentProperties);
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

    private String getBaseDNForDomain() {

        RootDSE rootDSE = (RootDSE) configuration.get("config.rootDSE");
        if (rootDSE != null) {
            return rootDSE.getDefaultNamingContext();
        }
        return null;
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

            log.debug("LDAP query is {}", displayFilter);
            int ldapPageSize = Integer.parseInt(configuration.getProperty("config.LdapQueryPageSize"));
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
            ex.printStackTrace();
            log.error("Error in running LDAP query. BaseDN is {}, filter is {}. Exception message is: {}",
                    baseDN,
                    displayFilter,
                    ex.getMessage());
            log.debug("Exception stacktrace: ", ex);
            handleException(ex);
        } finally {
            singleContextSource.destroy();
        }
        return handler;
    }

    /**
     * @param ex
     */
    private void handleException(Exception e) {
        if (e instanceof RuntimeException) {
            throw (RuntimeException) e;
        } else {
            throw new RuntimeException(e);
        }
    }
}
