package org.ovirt.engine.core.sso.api;

import java.util.Properties;

import org.ovirt.engine.api.extensions.Base;
import org.ovirt.engine.api.extensions.aaa.Authn;
import org.ovirt.engine.core.extensions.mgr.ExtensionProxy;

/**
 * An authentication profile is the combination of an authn and authz extensions. On login the user is authenticated by
 * the authn extension and then the details are looked up in the authz extension.
 */
public class AuthenticationProfile {
    /**
     * The name of the profile.
     */
    private String name;

    private ExtensionProxy authn;

    private ExtensionProxy authz;

    private ExtensionProxy mapper;

    private int negotiationPriority;

    /**
     * Create a new authentication profile with the given name, authenticator and directory.
     *
     * @param authn
     *            the authenticator that will be used to check the credentials of the user
     * @param authz
     *            the directory that will be used to lookup the details of the user once it is successfully
     * @param mapper
     *            the mapping extension to map the post authn auth record authenticated
     */
    public AuthenticationProfile(ExtensionProxy authn, ExtensionProxy authz, ExtensionProxy mapper) {
        Properties config = authn.getContext().get(Base.ContextKeys.CONFIGURATION);
        this.name = config.getProperty(Authn.ConfigKeys.PROFILE_NAME);
        this.authn = authn;
        this.authz = authz;
        this.mapper = mapper;
        this.negotiationPriority = Integer.parseInt(config.getProperty(Authn.ConfigKeys.NEGOTIATION_PRIORITY, "50"));
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

    public ExtensionProxy getMapper() {
        return mapper;
    }

    public String getAuthnName() {
        return authn.getContext().get(Base.ContextKeys.INSTANCE_NAME);
    }

    public String getAuthzName() {
        return authz.getContext().get(Base.ContextKeys.INSTANCE_NAME);
    }

    public int getNegotiationPriority() {
        return negotiationPriority;
    }
}
