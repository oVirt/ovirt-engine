package org.ovirt.engine.core.login;

import java.security.AccessController;
import java.security.Principal;
import java.security.PrivilegedAction;
import java.security.acl.Group;
import java.util.Map;

import javax.resource.spi.security.PasswordCredential;
import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.resource.security.AbstractPasswordCredentialLoginModule;
import org.jboss.security.SimplePrincipal;
import org.ovirt.engine.core.engineencryptutils.EncryptionUtils;

/**
 * An example of how one could use this custom login class other than the JBoss default class
 * {@code org.jboss.resource.security.SecureIdentityLoginModule} which this class is based on:<br>
 * <li>Add full path to the jar which contains this class to <i>$JBOSS_HOME/$PROFILE/conf/bootstrap/security.xml</i> as
 * an entry of element &lt;classloader name="security-classloader"...&gt;<br>
 * e.g.:
 * <pre>
 * &lt;root&gt;${jboss.server.home.url}deploy/engine.ear/lib/engine-login-3.1.0-0001.jar&lt;/root&gt;
 * </pre>
 * <li>Modify <i>$JBOSS_HOME/$PROFILE/conf/login-config.xml</i> to use the custom login class in element
 * &lt;application-policy name="EncryptDBPassword"&gt; (a complete element is shown below) e.g.:
 * <pre>
 * <code>
 * &lt;!--login-module code="org.jboss.resource.security.SecureIdentityLoginModule" flag="required"--&gt;
 * &lt;login-module code="org.ovirt.engine.core.login.EngineSecureIdentityLoginModule" flag="required"&gt;
 * </pre>
 * </code> <li>Encrypt the database password for a jca connection factory and set it in <i>EncryptDBPassword</i>
 * application-policy element:
 * <code>
 * <pre>
 * &lt;application-policy name="EncryptDBPassword"&gt;
 *      &lt;authentication&gt;
 *          &lt;!--login-module code="org.jboss.resource.security.SecureIdentityLoginModule" flag="required"--&gt;
 *          &lt;login-module code="org.ovirt.engine.core.security.login.EngineSecureIdentityLoginModule" flag="required"&gt;
 *              &lt;module-option name="username"&gt;sa&lt;/module-option&gt;
 *              &lt;module-option name="password"&gt;-1ef77a3433f8ba8aa370e115b1e73a8b&lt;/module-option&gt;
 *              &lt;module-option name="managedConnectionFactoryName"&gt;jboss.jca:name=ENGINEDataSource,service=LocalTxCM&lt;/module-option&gt;
 *          &lt;/login-module&gt;
 *       &lt;/authentication&gt;
 *   &lt;/application-policy&gt;
 * </pre>
 * </code>
 * <li>{@link #decode()} responsible for decoding the password by a customized algorithm as provided by {@link
 * EncryptionUtils.#decode(String, String, String)}<br>
 * or using the default if not specified any.
 * <li>The default value for cipher algorithm is Blowfish, and key derived from the phrase 'jaas is the way'.<br>
 * However, it is designed to support additional algorithms and keys. The full list of supported algorithms is <br>
 * specified in the following link.
 * @link<a href="http://download.oracle.com/javase/6/docs/technotes/guides/security/StandardNames.html">Java
 *         Cryptography Algorithms</a>
 */
public class EngineSecureIdentityLoginModule extends AbstractPasswordCredentialLoginModule {

    private static final Log log = LogFactory.getLog(EngineSecureIdentityLoginModule.class);

    private String username;
    private String password;

    public void initialize(Subject subject, CallbackHandler handler, Map sharedState, Map options) {
        super.initialize(subject, handler, sharedState, options);
        username = (String) options.get("username");
        if (username == null) {
            username = (String) options.get("userName");
            if (username == null) {
                throw new IllegalArgumentException("The user name is a required option");
            }
        }
        password = (String) options.get("password");
        if (password == null) {
            throw new IllegalArgumentException("The password is a required option");
        }
    }

    public boolean login() throws LoginException {
        log.trace("login called");
        if (super.login() == true)
            return true;

        super.loginOk = true;
        return true;
    }

    public boolean commit() throws LoginException {
        Principal principal = new SimplePrincipal(username);
        AccessController.doPrivileged(new AddPrincipalsAction(subject, principal));
        sharedState.put("javax.security.auth.login.name", username);
        // Decode the encrypted password
        try {
            char[] decodedPassword = decode(password);
            PasswordCredential cred = new PasswordCredential(username, decodedPassword);
            cred.setManagedConnectionFactory(getMcf());
            AccessController.doPrivileged(new AddCredentialsAction(subject, cred));
        } catch (Exception e) {
            log.debug("Failed to decode password", e);
            throw new LoginException("Failed to decode password: " + e.getMessage());
        }
        return true;
    }

    public boolean abort() {
        username = null;
        password = null;
        return true;
    }

    protected Principal getIdentity() {
        log.trace("getIdentity called, username=" + username);
        Principal principal = new SimplePrincipal(username);
        return principal;
    }

    protected Group[] getRoleSets() throws LoginException {
        Group[] empty = new Group[0];
        return empty;
    }

    private static String encode(String secret, String keyMaterial, String algorithm) {
        return EncryptionUtils.encode(secret, keyMaterial, algorithm);
    }

    /**
     * responsible for decoding the password by a customized algorithm as provided by {@link
     * EncryptionUtils.decode(password, key, algorithm)}. Customizing algorithm and key material could be achieved by
     * supplying other than null values to {@link EncryptionUtils.decode(password, key, algorithm)}
     */
    private static char[] decode(String secret) {
        String decode = EncryptionUtils.decode(secret, null, null);
        return decode != null ? decode.toCharArray() : null;
    }

    static class AddPrincipalsAction implements PrivilegedAction<Object> {
        Subject subject;
        Principal p;

        AddPrincipalsAction(Subject subject, Principal p) {
            this.subject = subject;
            this.p = p;
        }

        public Object run() {
            subject.getPrincipals().add(p);
            return null;
        }
    }

    static class AddCredentialsAction implements PrivilegedAction<Object> {
        Subject subject;
        PasswordCredential cred;

        AddCredentialsAction(Subject subject, PasswordCredential cred) {
            this.subject = subject;
            this.cred = cred;
        }

        public Object run() {
            subject.getPrivateCredentials().add(cred);
            return null;
        }
    }

    /**
     * Main entry point to encrypt a password using the hard-coded pass phrase
     * @param args
     * <br>
     *            - [0] = the password to encode<br>
     *            - [1] = the key material for encoding<br>
     *            - [2] = the algorithm for encoding
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        String encode = null;

        switch (args.length) {
        case 1:
            encode = encode(args[0], null, null);
            System.out.println("Encoded password: " + encode);
            break;
        case 3:
            encode = encode(args[0], args[1], args[2]);
            System.out.println("Encoded password: " + encode);
            break;
        default:
            System.out.println("Usage: <password> [<key material> <algorithm>]");
            break;
        }
    }
}

