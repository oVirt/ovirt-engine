package org.ovirt.engine.core.aaa;


/**
 * A password authenticator checks a user name and a password. Returns an AuthenticationResult object representing the
 * result of the authenticate call.
 */
public abstract class PasswordAuthenticator extends Authenticator {

    protected PasswordAuthenticator() {
    }

    /**
     * Authenticates according to the given name and password. In case authentication fails, the
     * {@code AAAExtensionException will be thrown}
     *
     * @param name
     *            the name of user being authenticated
     *
     */
    public abstract void authenticate(String name, String password);

    /**
     * Returns the URL to a management page the user can set its expired password at
     *
     * @return the URL
     */
    public String getChangeExpiredPasswordURL() {
        return (String) context.get(ExtensionProperties.AAA_CHANGE_EXPIRED_PASSWORD_URL);
    }

    /**
     * Returns a custom message that the user will get when its tries to login with expired password
     *
     * @return the custom message
     */
    public String getChangeExpiredPasswordMsg() {
        return (String) context.get(ExtensionProperties.AAA_CHANGE_EXPIRED_PASSWORD_MSG);
    }
}
