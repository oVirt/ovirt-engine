package org.ovirt.engine.core.aaa;

import java.util.Map;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ovirt.engine.api.extensionsold.Extension;

/**
 * A authenticator is an object used to verify an identity.
 */
public abstract class Authenticator implements Extension {

    protected Map<Extension.ExtensionProperties, Object> context;

    /**
     * Returns the name of the profile the authenticator is associated with
     * @return profile name
     */
    public String getName() {
        return (String) context.get(ExtensionProperties.NAME);
    }

    public String getProfileName() {
        return ((Properties) context.get(ExtensionProperties.CONFIGURATION)).getProperty("ovirt.engine.aaa.authn.profile.name");
    }


    @Override
    public void setContext(Map<ExtensionProperties, Object> context) {
        this.context = context;
    }

    @Override
    public Map<ExtensionProperties, Object> getContext() {
        return context;
    }

    /**
     * Process the given request and return a new result object if the negotiation has finished or {@code null} if it
     * hasn't. If the process hasn't finished then the response must be populated by the authenticator and it will be
     * sent back to the client.
     *
     * @param request the HTTP request to be processed
     * @param response the HTTP response to be processed by the application or sent to back the browser if the
     *     authentication didn't finish yet
     * @return a result object if the authentication process has finished or {@code null} if it hasn't
     */
    public NegotiationResult negotiate(HttpServletRequest request, HttpServletResponse response) {
        // Override this in subclasses where needed
        throw new RuntimeException("negotiate method is not supported");
    }

    /**
     * Authenticates according to the given name and password. In case authentication fails, the
     * {@code AAAExtensionException will be thrown}
     *
     * @param name
     *            the name of user being authenticated
     *
     */
    public void authenticate(String name, String password) {
        // Override this in subclasses where needed
        throw new RuntimeException("authenticate method is not supported");

    }

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

    public boolean isNegotiationAuth() {
        Integer capabilities = (Integer) context.get(ExtensionProperties.AAA_AUTHENTICATION_CAPABILITIES);
        if (capabilities == null) {
            return false;
        } else {
            return (capabilities & AAA_AUTH_CAP_FLAGS_NEGOTIATING) != 0;
        }
    }

    public boolean isPasswordAuth() {
        Integer capabilities = (Integer) context.get(ExtensionProperties.AAA_AUTHENTICATION_CAPABILITIES);
        if (capabilities == null) {
            return false;
        } else {
            return (capabilities & AAA_AUTH_CAP_FLAGS_PASSWORD) != 0;
        }
    }

    protected Authenticator() {
    }

}
