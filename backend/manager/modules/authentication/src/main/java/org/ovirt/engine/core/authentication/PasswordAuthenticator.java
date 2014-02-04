package org.ovirt.engine.core.authentication;

/**
 * A password authenticator checks a user name and a password. Returns an AuthenticationResult object representing the
 * result of the authenticate call.
 */
public interface PasswordAuthenticator extends Authenticator {
    /**
     * Authenticates according to the given name and password
     *
     * @param name
     *            the name of user being authenticated
     * @param password
     * @return AuthenticationResult object that holds the authentication result
     */
    AuthenticationResult authenticate(String name, String password);
}
