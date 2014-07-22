/**
 *
 */
package org.ovirt.engine.extensions.aaa.builtin.kerberosldap;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.security.auth.Subject;
import javax.security.auth.kerberos.KerberosTicket;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;
import org.ovirt.engine.extensions.aaa.builtin.kerberosldap.utils.kerberos.AuthenticationResult;
import org.ovirt.engine.extensions.aaa.builtin.kerberosldap.utils.kerberos.KerberosReturnCodeParser;
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
    private static final Log log = LogFactory.getLog(GSSAPIDirContextAuthenticationStrategy.class);
    private LoginContext loginContext;
    private String password;
    private String userName;
    private Properties configuration;

    public LoginContext getLoginContext() {
        return loginContext;
    }

    /**
     * @param password
     * @param userName
     * @param explicitAuth
     *
     */
    public GSSAPIDirContextAuthenticationStrategy(Properties configuration, String userName, String password) {
        this.userName = userName;
        this.password = password;
        this.configuration = configuration;
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
        String qopValue = configuration.getProperty("config.SASL_QOP");
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
        GSSAPICallbackHandler callbackHandler = new GSSAPICallbackHandler(userName, password);
        authenticateToKDC(callbackHandler);
    }


    private void authenticateToKDC(GSSAPICallbackHandler callbackHandler) throws AuthenticationResultException {

        try {
            loginContext = new LoginContext(configuration.getProperty("config.JAASLoginContext"), callbackHandler);
            loginContext.login();
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
