package org.ovirt.engine.extensions.aaa.builtin.kerberosldap.utils.kerberos;

import static org.ovirt.engine.extensions.aaa.builtin.kerberosldap.utils.kerberos.InstallerConstants.ERROR_PREFIX;

import java.io.IOException;
import java.util.List;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.apache.log4j.Logger;
import org.ovirt.engine.extensions.aaa.builtin.kerberosldap.utils.ldap.LdapProviderType;

/**
 * Utility to verify Kerberos installation
 *
 */
public class KerberosConfigCheck {
    private LoginContext lc;
    private final List<String> ldapServers;
    private String defaultLdapServerPort;
    private final static Logger log = Logger.getLogger(KerberosConfigCheck.class);

    public enum Arguments {
        domains,
        user,
        password,
        jaas_file,
        jboss_dir,
        krb5_conf_path,
        ldapProviderType;
    }

    public KerberosConfigCheck(List<String> ldapServers, String defaultLdapServerPort) {
        this.ldapServers = ldapServers;
        this.defaultLdapServerPort =  defaultLdapServerPort;
    }

    public KerberosConfigCheck() {
        this(null, null);
    }

    /**
     * JAAS callback handler. JAAS uses this class during login - it provides an array of callbacks (including the
     * NameCallback and PasswordCallback) It is the responsibility of the implementor of CallbackHandler to set the user
     * name and the password on the relevant call backs.
     */
    private static class KerberosUtilCallbackHandler implements CallbackHandler {
        private String username;
        private String password;

        public KerberosUtilCallbackHandler(String username, String password) {
            this.username = username;
            this.password = password;
        }

        @Override
        public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
            for (int i = 0; i < callbacks.length; i++) {
                if (callbacks[i] instanceof NameCallback) {
                    NameCallback cb = (NameCallback) callbacks[i];
                    cb.setName(username);

                } else if (callbacks[i] instanceof PasswordCallback) {
                    PasswordCallback cb = (PasswordCallback) callbacks[i];
                    cb.setPassword(password.toCharArray());
                } else {
                    throw new UnsupportedCallbackException(callbacks[i]);
                }
            }
        }
    }

    public void checkInstallation(String domains,
            String username,
            String password,
            String jaasFile,
            String krb5ConfFile,
            StringBuffer userGuid,
            LdapProviderType ldapProviderType)
            throws AuthenticationException {
        String[] domainsList = domains.split(",", -1);
        String domain = domainsList[0].trim();
        String realm = domain.toUpperCase();
        validateKerberosInstallation(realm, username, password, jaasFile, krb5ConfFile, userGuid, ldapProviderType);
    }

    public void validateKerberosInstallation(String realm,
            String username,
            String password,
            String pathToJAASFile,
            String pathToKrb5ConfFile,
            StringBuffer userGuid,
            LdapProviderType ldapProviderType) throws AuthenticationException {

        AuthenticationResult authResult = authenticate(realm, username, password, pathToJAASFile, pathToKrb5ConfFile);
        if (authResult == AuthenticationResult.OK) {
            // Successful authentication was acehived, no point in searching for
            // KDcs that use UDP
            AuthenticationResult actionResult =
                    promptSuccessfulAuthentication(realm, username, userGuid, ldapProviderType);

            if (actionResult != AuthenticationResult.OK) {
                throw new AuthenticationException(actionResult);
            }

            return;
        } else {
            throw new AuthenticationException(authResult);
        }
    }

    private AuthenticationResult promptSuccessfulAuthentication(String realm,
            String username,
            StringBuffer userGuid,
            LdapProviderType ldapProviderType) {

        AuthenticationResult authResult = AuthenticationResult.OTHER;

        try {
            // Executing the code that will perform the LDAP query to get the
            // user and print its GUID.
            // A Windows domain is lowercase string of Keberos realm.
            authResult =
                    (AuthenticationResult) Subject.doAs(lc.getSubject(), new JndiAction(username,
                            realm.toLowerCase(),
                            userGuid, ldapProviderType, ldapServers, defaultLdapServerPort));

        } finally {
            if (lc != null) {
                try {
                    lc.logout();
                } catch (LoginException e) {
                    String formattedMessage = ERROR_PREFIX + " logout failed " + e.getMessage();
                    System.out.println(formattedMessage);
                    log.error(formattedMessage);
                }
            }
        }

        return authResult;
    }

    private AuthenticationResult authenticate(String realm, String username, String password,
            String pathToJAASFile, String pathToKrb5File) {

        // Set the realm to authenticate to and the path to the JAAS file that
        // will define
        // that JAAS is using kerberos login module

        System.setProperty("java.security.krb5.conf", pathToKrb5File);
        // Get kdcs for the relevant protocol (tcp or udp) and for the given
        // realm

        System.setProperty("java.security.auth.login.config", pathToJAASFile);

        return checkAuthentication(username, password);
    }

    private AuthenticationResult checkAuthentication(String username, String password) {

        AuthenticationResult result = AuthenticationResult.OK;
        try {

            lc = new LoginContext("oVirtKerb", new KerberosUtilCallbackHandler(username, password));
            lc.login();
            log.debug("Check authentication finished successfully ");
        } catch (LoginException ex) {
            String resultMessage = ex.getMessage();
            String formattedMessage = ERROR_PREFIX + " exception message: " + resultMessage;
            log.error(formattedMessage);
            log.debug("", ex);

            KerberosReturnCodeParser parser = new KerberosReturnCodeParser();
            result = parser.parse(resultMessage);
            if (result != AuthenticationResult.OTHER) {
                return result;
            } else {
                System.out.println(formattedMessage);
            }
        }
        return result;
    }
}
