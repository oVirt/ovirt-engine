package org.ovirt.engine.core.aaa;

/**
 * A password authenticator checks a user name and a password. Returns an AuthenticationResult object representing the
 * result of the authenticate call.
 */
public abstract class PasswordAuthenticator extends Authenticator {

    protected PasswordAuthenticator(String profileName) {
        super(profileName);
        // TODO Auto-generated constructor stub
    }

    /**
     * Authenticates according to the given name and password
     *
     * @param name
     *            the name of user being authenticated
     * @param password
     * @return AuthenticationResult object that holds the authentication result
     */
    public abstract AuthenticationResult authenticate(String name, String password);
}
