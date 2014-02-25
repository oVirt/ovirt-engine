package org.ovirt.engine.core.aaa.internal;

import org.apache.commons.lang.ObjectUtils;
import org.ovirt.engine.core.aaa.AuthenticationResult;
import org.ovirt.engine.core.aaa.PasswordAuthenticator;
import org.ovirt.engine.core.aaa.result.BooleanAuthenticationResult;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This authenticator authenticates the internal user as specified in the {@code AdminUser} and {@code AdminPassword}
 * configuration parameters stored in the database.
 */
public class InternalAuthenticator extends PasswordAuthenticator {


    private static final Logger log = LoggerFactory.getLogger(InternalAuthenticator.class);

    @Override
    public AuthenticationResult authenticate(String user, String password) {
        String adminName = Config.<String> getValue(ConfigValues.AdminUser);
        String adminPassword = Config.<String> getValue(ConfigValues.AdminPassword);
        return new BooleanAuthenticationResult(ObjectUtils.equals(user, adminName) &&
                ObjectUtils.equals(password, adminPassword));
    }

    @Override
    public void init() {
        context.put(ExtensionProperties.AUTHOR, "The oVirt Project");
        context.put(ExtensionProperties.EXTENSION_NAME, "Internal Authentication (Built-in)");
        context.put(ExtensionProperties.LICENSE, "ASL 2.0");
        context.put(ExtensionProperties.HOME, "http://www.ovirt.org");
        context.put(ExtensionProperties.VERSION, "N/A");
    }
}
