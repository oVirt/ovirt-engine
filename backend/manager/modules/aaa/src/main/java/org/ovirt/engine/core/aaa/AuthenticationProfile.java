package org.ovirt.engine.core.aaa;

import java.util.Properties;

import org.ovirt.engine.api.extensions.Base;
import org.ovirt.engine.core.extensions.mgr.ExtensionProxy;

/**
 * An authentication profile is the combination of an authn and authz extensions. An user wishing to login to the system
 * is authenticated by the authn extension and then the details are looked up in the authz extension.
 */
public class AuthenticationProfile {
    /**
     * The name of the profile.
     */
    private String name;

    private ExtensionProxy authn;

    private ExtensionProxy authz;

    /**
     * Create a new authentication profile with the given name, authenticator and directory.
     *
     * @param authn the authenticator that will be used to check the credentials of the user
     * @param authz the directory that will be used to lookup the details of the user once it is successfully
     *     authenticated
     */
    public AuthenticationProfile(ExtensionProxy authn, ExtensionProxy authz) {
        this.name = authn.getContext().<Properties> get(Base.ContextKeys.CONFIGURATION)
                .getProperty("ovirt.engine.aaa.authn.profile.name");
        this.authn = authn;
        this.authz = authz;
    }

    /**
     * Get the name of the profile.
     */
    public String getName() {
        return name;
    }

    /**
     * Get a reference to the authenticator.
     */
    public ExtensionProxy getAuthn() {
        return authn;
    }

    /**
     * Get a reference to the directory.
     */

    public ExtensionProxy getAuthz() {
        return authz;
    }
}
