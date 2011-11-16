package org.ovirt.engine.core.bll.adbroker;

import java.net.URI;
import java.util.concurrent.Callable;

import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.LogCompat;
import org.ovirt.engine.core.compat.LogFactoryCompat;
import org.ovirt.engine.core.utils.kerberos.AuthenticationResult;
import org.springframework.ldap.core.support.LdapContextSource;

public class PrepareLdapConnectionTask implements Callable<LDAPTemplateWrapper> {

    public PrepareLdapConnectionTask(
            DirectorySearcher searcher,
            LdapCredentials ldapCredentials,
            String domain,
            URI ldapURI) {
        this.searcher = searcher;
        this.ldapCredentials = ldapCredentials;
        this.domain = domain;
        this.ldapURI = ldapURI;
    }

    private final LdapCredentials ldapCredentials;
    private final DirectorySearcher searcher;
    private final String domain;
    private final URI ldapURI;

    private static final LogCompat log = LogFactoryCompat.getLog(PrepareLdapConnectionTask.class);

    @Override
    public LDAPTemplateWrapper call() throws Exception {

        String userName = ldapCredentials.getUserName();
        String password = ldapCredentials.getPassword();
        LdapContextSource ldapctx = new LdapContextSource();

        LDAPTemplateWrapper wrapper = LDAPTemplateWrapperFactory.getLDAPTemplateWrapper(ldapctx, userName, password,
                domain);

        try {
            wrapper.init(ldapURI,
                    searcher.isBaseDNExist(),
                    searcher.getExplicitAuth(),
                    searcher.getExplicitBaseDN(),
                    searcher.getDomainObject(domain).getLdapProviderType(),
                    Config.<Integer> GetValue(ConfigValues.LDAPQueryTimeout) * 1000);
            ldapctx.afterPropertiesSet();
        } catch (Exception e) {
            log.error("Error connecting to directory server", e);
            throw new EngineDirectoryServiceException(AuthenticationResult.OTHER);
        }

        wrapper.useAuthenticationStrategy();
        wrapper.setIgnorePartialResultException(true);
        return wrapper;
    }
}
