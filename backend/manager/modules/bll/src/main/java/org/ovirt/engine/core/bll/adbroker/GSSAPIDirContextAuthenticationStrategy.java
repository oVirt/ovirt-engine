/**
 *
 */
package org.ovirt.engine.core.bll.adbroker;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.security.auth.Subject;
import javax.security.auth.kerberos.KerberosTicket;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.utils.kerberos.AuthenticationResult;
import org.ovirt.engine.core.utils.kerberos.KerberosReturnCodeParser;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;
import org.springframework.ldap.core.support.DirContextAuthenticationStrategy;

/**
 *
 * Implements a GSSAPI directory context authentication strategy to be used with
 * LDAP
 *
 *
 */
public class GSSAPIDirContextAuthenticationStrategy implements DirContextAuthenticationStrategy {

    private static final String GSS_API_AUTHENTICATION = "GSSAPI";
    private static final String LOGIN_MODULE_POLICY_NAME = "EngineKerberosAuth";
    private static Log log = LogFactory.getLog(GSSAPIDirContextAuthenticationStrategy.class);
    private LoginContext loginContext;
    private String password;
    private String userName;
    private String realm;
    private boolean explicitAuth;

    public void setExplicitAuth(boolean explicitAuth) {
        this.explicitAuth = explicitAuth;
    }

    public LoginContext getLoginContext() {
        return loginContext;
    }

    /**
     * @param password
     * @param userName
     * @param explicitAuth
     *
     */
    public GSSAPIDirContextAuthenticationStrategy(String userName, String password, String realm, boolean explicitAuth) {
        this.userName = userName;
        this.password = password;
        this.realm = realm;
        this.explicitAuth = explicitAuth;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.springframework.ldap.core.support.DirContextAuthenticationStrategy
     * #setupEnvironment(java.util.Hashtable, java.lang.String,
     * java.lang.String)
     */
    @Override
    public void setupEnvironment(Hashtable env, String userDn, String password) throws NamingException {
        env.put(Context.SECURITY_AUTHENTICATION, GSS_API_AUTHENTICATION);
        String qopValue = Config.<String>GetValue(ConfigValues.SASL_QOP);
        env.put("javax.security.sasl.qop", qopValue);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.springframework.ldap.core.support.DirContextAuthenticationStrategy
     * #processContextAfterCreation(javax.naming.directory.DirContext,
     * java.lang.String, java.lang.String)
     */
    @Override
    public DirContext processContextAfterCreation(DirContext ctx, String userDn, String password)
            throws NamingException {

        return ctx;

    }

    public void authenticate() throws AuthenticationResultException {
        UsersDomainsCacheManager usersDomainsCacheManager = UsersDomainsCacheManagerService.getInstance();
        UserDomainInfo userDomainInfo = usersDomainsCacheManager.associateUserWithDomain(this.userName,
                this.realm.toLowerCase());
        loginContext = null;
        synchronized (userDomainInfo) {
            // In case authentication is performed in an implicit way (as a
            // result of internal command) try and get
            // login context from cache
            if (!explicitAuth) {
                loginContext = userDomainInfo.getLoginContext();
            }

            if (!validLoginContext()) {
                explicitAuth(userDomainInfo);
            }
        }
    }

    private void explicitAuth(UserDomainInfo userDomainInfo) throws AuthenticationResultException {

        GSSAPICallbackHandler callbackHandler = new GSSAPICallbackHandler(userName, password);
        authenticateToKDC(callbackHandler, userDomainInfo);

    }

    private  void authenticateToKDC(GSSAPICallbackHandler callbackHandler, UserDomainInfo userDomainInfo) throws AuthenticationResultException {

        try {
            loginContext = new LoginContext(LOGIN_MODULE_POLICY_NAME, callbackHandler);
            loginContext.login();
            userDomainInfo.setLoginContext(loginContext);
            if (log.isDebugEnabled()) {
                log.debug("Successful login for user " + userName);
            }
        } catch (LoginException ex) {

            // JAAS throws login exception due to various reasons.
            // We check if the login exception matches a case where the user
            // provided wrong authentication details, or
            // if there was another error - in case the user provided wrong
            // authentication details, we will abort the kdc search
            loginContext = null;
            KerberosReturnCodeParser parser = new KerberosReturnCodeParser();
            AuthenticationResult result = parser.parse(ex.getMessage());
            if (result.getAuditLogType() != null) {
                LdapBrokerUtils.logEventForUser(userName, result.getAuditLogType());
            }
            log.error("Kerberos error: " + ex.getMessage());
            if (log.isDebugEnabled()) {
                log.debug("Kerberos error stacktrace: ", ex);
            }
            if (result != AuthenticationResult.OTHER) {
                log.error(result.getDetailedMessage());
            }
            throw new AuthenticationResultException(result);
        }
    }

    private boolean validLoginContext() {

        if (loginContext == null)
            return false;

        Subject subject = loginContext.getSubject();
        if (subject == null)
            return false;
        Set<KerberosTicket> privateCreds = subject.getPrivateCredentials(KerberosTicket.class);
        if (privateCreds == null || privateCreds.size() == 0)
            return false;

        Iterator<KerberosTicket> iterator = privateCreds.iterator();
        KerberosTicket ticket = iterator.next();
        return ticket.isCurrent();
    }
}
