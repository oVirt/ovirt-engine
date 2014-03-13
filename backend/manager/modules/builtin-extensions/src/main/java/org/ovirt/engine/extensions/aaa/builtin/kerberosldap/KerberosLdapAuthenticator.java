package org.ovirt.engine.extensions.aaa.builtin.kerberosldap;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.api.extensions.AAAExtensionException;
import org.ovirt.engine.api.extensions.AAAExtensionException.AAAExtensionError;
import org.ovirt.engine.core.aaa.Authenticator;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;

/**
 * This authenticator implementation is a bridge between the new directory interface and the existing LDAP
 * infrastructure. It will exist only while the engine is migrated to use the new authentication interfaces, then it
 * will be removed.
 */
public class KerberosLdapAuthenticator extends Authenticator {

    /**
     * The reference to the LDAP broker that implements the authentication.
     */
    private LdapBroker broker;
    private static volatile Map<String, String> passwordChangeMsgPerDomain = null;



    public KerberosLdapAuthenticator() {
    }

    @Override
    public void init() {
        broker = LdapFactory.getInstance(getProfileName());
        context.put(ExtensionProperties.AUTHOR, "The oVirt Project");
        context.put(ExtensionProperties.EXTENSION_NAME, "Internal Kerberos/LDAP authentication (Built-in)");
        context.put(ExtensionProperties.LICENSE, "ASL 2.0");
        context.put(ExtensionProperties.HOME, "http://www.ovirt.org");
        context.put(ExtensionProperties.VERSION, "N/A");
        context.put(ExtensionProperties.AAA_AUTHENTICATION_CAPABILITIES, AAA_AUTH_CAP_FLAGS_PASSWORD);

        if (passwordChangeMsgPerDomain == null) {
            synchronized (KerberosLdapAuthenticator.class) {
                if (passwordChangeMsgPerDomain == null) {
                    passwordChangeMsgPerDomain = new HashMap<>();
                    String changePasswordUrl = Config.<String> getValue(ConfigValues.ChangePasswordMsg);
                    String[] pairs = changePasswordUrl.split(",");
                    for (String pair : pairs) {
                        // Split the pair in such a way that if the URL contains :, it will not be split to strings
                        String[] pairParts = pair.split(":", 2);
                        if (pairParts.length >= 2) {
                            try {
                                passwordChangeMsgPerDomain.put(pairParts[0], URLDecoder.decode(pairParts[1], "UTF-8"));
                            } catch (UnsupportedEncodingException e) {
                                throw new AAAExtensionException(AAAExtensionError.INVALID_CONFIGURATION,
                                        "error in obtaining the password change message or url for " + pairParts[0]);
                            }
                        }
                    }

                }
            }
        }

        String changePasswordMsgOrUrl = passwordChangeMsgPerDomain.get(getProfileName());
        if (changePasswordMsgOrUrl != null) {
            ExtensionProperties key =
                    containsURL(changePasswordMsgOrUrl) ? ExtensionProperties.AAA_CHANGE_EXPIRED_PASSWORD_URL
                            : ExtensionProperties.AAA_CHANGE_EXPIRED_PASSWORD_MSG;
            context.put(key, changePasswordMsgOrUrl);
        }

        KerberosManager.getInstance();
        UsersDomainsCacheManagerService.getInstance().init();
    }



    /**
     * {@inheritDoc}
     */
    @Override
    public void authenticate(String name, String password) {
        broker.runAdAction(
            AdActionType.AuthenticateUser,
                new LdapUserPasswordBaseParameters(getProfileName(), name, password)
        );
    }

    private boolean containsURL(String text) {
        return text.indexOf("http") == 0 || text.indexOf("https") == 0;
    }


}
