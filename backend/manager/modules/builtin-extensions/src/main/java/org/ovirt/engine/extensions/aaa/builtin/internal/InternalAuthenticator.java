package org.ovirt.engine.extensions.aaa.builtin.internal;

import java.util.Properties;

import org.ovirt.engine.api.extensions.AAAExtensionException;
import org.ovirt.engine.core.aaa.Authenticator;

/**
 * This authenticator authenticates the internal user as specified in the {@code AdminUser} and {@code AdminPassword}
 * configuration parameters stored in the database.
 */
public class InternalAuthenticator extends Authenticator {

    @Override
    public void authenticate(String user, String password) {
        String adminUser = ((Properties)context.get(ExtensionProperties.CONFIGURATION)).getProperty("config.authn.user.name");
        String adminPassword =  ((Properties)context.get(ExtensionProperties.CONFIGURATION)).getProperty("config.authn.user.password");
        if (!(user.equals(adminUser) && password.equals(adminPassword))) {
            throw new AAAExtensionException(AAAExtensionException.AAAExtensionError.INCORRECT_CREDENTIALS, "");
        }
    }

    @Override
    public void init() {
        context.put(ExtensionProperties.AUTHOR, "The oVirt Project");
        context.put(ExtensionProperties.EXTENSION_NAME, "Internal Authentication (Built-in)");
        context.put(ExtensionProperties.LICENSE, "ASL 2.0");
        context.put(ExtensionProperties.HOME, "http://www.ovirt.org");
        context.put(ExtensionProperties.VERSION, "N/A");
        context.put(ExtensionProperties.AAA_AUTHENTICATION_CAPABILITIES, AAA_AUTH_CAP_FLAGS_PASSWORD);
    }
}
