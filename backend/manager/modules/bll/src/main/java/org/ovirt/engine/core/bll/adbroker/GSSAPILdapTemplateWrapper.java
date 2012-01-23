/**
 *
 */
package org.ovirt.engine.core.bll.adbroker;

import java.security.PrivilegedAction;

import javax.naming.directory.SearchControls;
import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;

import org.springframework.ldap.core.NameClassPairCallbackHandler;
import org.springframework.ldap.core.support.DirContextAuthenticationStrategy;
import org.springframework.ldap.core.support.LdapContextSource;

import org.ovirt.engine.core.compat.LogCompat;
import org.ovirt.engine.core.compat.LogFactoryCompat;

/**
 *
 */
public class GSSAPILdapTemplateWrapper extends LDAPTemplateWrapper {

    private static LogCompat log = LogFactoryCompat.getLog(GSSAPILdapTemplateWrapper.class);

    private LoginContext loginContext;

    public GSSAPILdapTemplateWrapper(LdapContextSource contextSource, String userName, String password, String path) {
        super(contextSource, userName, password, path);
    }

    /**
     *
     */

    /*
     * (non-Javadoc)
     *
     * @see
     * org.ovirt.engine.core.dal.adbroker.LDapTemplateWrapper#search(java.lang.String
     * , java.lang.String, javax.naming.directory.SearchControls,
     * org.springframework.ldap.core.NameClassPairCallbackHandler)
     */
    @Override
    public void search(String baseDN, String filter, String displayFilter, SearchControls searchControls, NameClassPairCallbackHandler handler) {
        Subject.doAs(loginContext.getSubject(), new SearchAction(baseDN, filter, displayFilter, searchControls, handler));

    }

    private class SearchAction implements PrivilegedAction<NameClassPairCallbackHandler> {

        private String baseDN;
        private String filter;
        private String displayFilter;
        private SearchControls searchControls;
        private NameClassPairCallbackHandler handler;

        public SearchAction(String baseDN, String filter, String displayFilter, SearchControls searchControls,
                            NameClassPairCallbackHandler handler) {
            this.baseDN = baseDN;
            this.filter = filter;
            this.displayFilter = displayFilter;
            this.searchControls = searchControls;
            this.handler = handler;
        }

        @Override
        public NameClassPairCallbackHandler run() {
            return pagedSearch(baseDN, filter, displayFilter, searchControls, handler);
        }


    }




    @Override
    protected DirContextAuthenticationStrategy buildContextAuthenticationStategy() {

        String realm = domain.toUpperCase();

        return new GSSAPIDirContextAuthenticationStrategy(userName, password, realm, explicitAuth);
    }

    @Override
    public void useAuthenticationStrategy() throws EngineDirectoryServiceException {
        super.useAuthenticationStrategy();
        GSSAPIDirContextAuthenticationStrategy strategy = (GSSAPIDirContextAuthenticationStrategy) authStrategy;
        strategy.authenticate();
        loginContext = strategy.getLoginContext();

    }

    @Override
    public void adjustUserName(LdapProviderType ldapProviderType) {
        // No manipulation on user name is required, in contrast to SIMPLE
        // authentication

    }

    @Override
    protected void setCredentialsOnContext() {
        // Does nothing - credentials are used by JAAS
    }

}
