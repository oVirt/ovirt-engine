package org.ovirt.engine.core.authentication.internal;

import org.apache.commons.lang.ObjectUtils;
import org.ovirt.engine.core.authentication.AuthenticationResult;
import org.ovirt.engine.core.authentication.PasswordAuthenticator;
import org.ovirt.engine.core.authentication.result.BooleanAuthenticationResult;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This authenticator authenticates the internal user as specified in the {@code AdminUser} and {@code AdminPassword}
 * configuration parameters stored in the database.
 */
public class InternalAuthenticator implements PasswordAuthenticator {
    private static final Logger log = LoggerFactory.getLogger(InternalAuthenticator.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public AuthenticationResult<?> authenticate(String user, String password) {
        String adminName = Config.<String> getValue(ConfigValues.AdminUser);
        String adminPassword = Config.<String> getValue(ConfigValues.AdminPassword);
        return new BooleanAuthenticationResult(ObjectUtils.equals(user, adminName) &&
                ObjectUtils.equals(password, adminPassword));
    }
}
