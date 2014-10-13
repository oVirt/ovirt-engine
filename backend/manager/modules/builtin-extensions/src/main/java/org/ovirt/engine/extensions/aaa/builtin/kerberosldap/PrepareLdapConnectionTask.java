package org.ovirt.engine.extensions.aaa.builtin.kerberosldap;

import java.util.Properties;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ldap.core.support.LdapContextSource;

import org.ovirt.engine.extensions.aaa.builtin.kerberosldap.utils.kerberos.AuthenticationResult;
import org.ovirt.engine.extensions.aaa.builtin.kerberosldap.utils.ldap.LdapProviderType;

public class PrepareLdapConnectionTask implements Callable<LDAPTemplateWrapper> {

    private Properties configuration;

    public PrepareLdapConnectionTask(
            Properties configuration,
            DirectorySearcher searcher,
            LdapCredentials ldapCredentials,
            String domain,
            String ldapURI) {
        this.configuration = configuration;
        this.searcher = searcher;
        this.ldapCredentials = ldapCredentials;
        this.domain = domain;
        this.ldapURI = ldapURI;
    }

    private final LdapCredentials ldapCredentials;
    private final DirectorySearcher searcher;
    private final String domain;
    private final String ldapURI;

    private static final Logger log = LoggerFactory.getLogger(PrepareLdapConnectionTask.class);

    @Override
    public LDAPTemplateWrapper call() throws Exception {

        String userName = ldapCredentials.getUserName();
        String password = ldapCredentials.getPassword();
        LdapContextSource ldapctx = new LdapContextSource();

        LDAPTemplateWrapper wrapper =
                LDAPTemplateWrapperFactory.getLDAPTemplateWrapper(configuration, ldapctx, userName, password,
                domain);

        try {
            wrapper.init(ldapURI,
                    searcher.isBaseDNExist(),
                    searcher.getExplicitBaseDN(),
                    LdapProviderType.valueOf(configuration.getProperty("config.LDAPProviderTypes")),
                    Integer.parseInt(configuration.getProperty("config.LDAPQueryTimeout")) * 1000);
            ldapctx.afterPropertiesSet();
        } catch (Exception e) {
            log.error("Error connecting to directory server", e);
            throw new AuthenticationResultException(AuthenticationResult.OTHER);
        }

        wrapper.useAuthenticationStrategy();
        wrapper.setIgnorePartialResultException(true);
        return wrapper;
    }
}
