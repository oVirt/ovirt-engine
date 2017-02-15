package org.ovirt.engine.core.aaa;

import java.util.Properties;

import org.ovirt.engine.api.extensions.Base;
import org.ovirt.engine.api.extensions.aaa.Authn;
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

    private String authzName;

    private ExtensionProxy authn;

    private ExtensionProxy mapper;

    private int negotiationPriority;

    /**
     * Create a new authentication profile with the given name, authenticator and directory.
     *
     * @param authn the authenticator that will be used to check the credentials of the user
     * @param mapping the mappinng extension to map the post authn auth record
     *     authenticated
     */
    public AuthenticationProfile(ExtensionProxy authn, String authzName, ExtensionProxy mapper) {
        Properties config = authn.getContext().get(Base.ContextKeys.CONFIGURATION);
        this.name = config.getProperty(Authn.ConfigKeys.PROFILE_NAME);
        this.authzName = authzName;
        this.authn = authn;
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

    public ExtensionProxy getMapper() {
        return mapper;
    }

    public String getAuthnName() {
        return authn.getContext().get(Base.ContextKeys.INSTANCE_NAME);
    }

    public String getAuthzName() {
        return authzName;
    }

    public int getNegotiationPriority() {
        return negotiationPriority;
    }
}
