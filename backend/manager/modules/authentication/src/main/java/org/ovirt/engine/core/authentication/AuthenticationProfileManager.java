package org.ovirt.engine.core.authentication;

import java.util.List;

import org.ovirt.engine.core.extensions.mgr.Configuration;
import org.ovirt.engine.core.extensions.mgr.Factory;
import org.ovirt.engine.core.extensions.mgr.Manager;

/**
 * The authentication profile manager is responsible for managing a set of authentication profiles.
 */
public class AuthenticationProfileManager extends Manager<AuthenticationProfile> {
    /**
     * This is a singleton and this is the instance.
     */
    private static AuthenticationProfileManager manager;

    static {
        manager = new AuthenticationProfileManager();
    }

    /**
     * Get an instance of the directory manager.
     */
    public static AuthenticationProfileManager getInstance() {
        return manager;
    }


    /**
     * There is only one authenticator profile factory, and it isn't configurable.
     */
    private AuthenticationProfileFactory factory;

    private AuthenticationProfileManager() {
        super(AuthenticationProfileFactory.class);
        factory = new AuthenticationProfileFactory();
    }

    /**
     * This method is overridden because this manager doesn't need to find the factory class, it is always the same.
     */
    @Override
    protected Factory<AuthenticationProfile> findFactory(Configuration config) {
        return factory;
    }

    /**
     * Returns an unmodifiable list containing all the authentication profiles that have been previously loaded.
     */
    public List<AuthenticationProfile> getProfiles() {
        return getObjects();
    }

    /**
     * Gets the authentication profile for the given name.
     *
     * @param name the name of the profile
     * @return the requested profile or {@code null} if no such profile can be found
     */
    public AuthenticationProfile getProfile(String name) {
        return getObject(name);
    }

    /**
     * Register an authentication profile.
     *
     * @param name the name of the profile
     * @param profile the profile to register
     */
    public void registerProfile(String name, AuthenticationProfile profile) {
        registerObject(name, profile);
    }
}
