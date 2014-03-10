package org.ovirt.engine.core.aaa.internal;

import org.apache.commons.lang.ObjectUtils;
import org.ovirt.engine.api.extensions.AAAExtensionException;
import org.ovirt.engine.core.aaa.Authenticator;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This authenticator authenticates the internal user as specified in the {@code AdminUser} and {@code AdminPassword}
 * configuration parameters stored in the database.
 */
public class InternalAuthenticator extends Authenticator {


    private static final Logger log = LoggerFactory.getLogger(InternalAuthenticator.class);

    @Override
    public void authenticate(String user, String password) {
        String adminName = Config.<String> getValue(ConfigValues.AdminUser);
        String adminPassword = Config.<String> getValue(ConfigValues.AdminPassword);
        if (!ObjectUtils.equals(user, adminName) || !ObjectUtils.equals(password, adminPassword)) {
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
