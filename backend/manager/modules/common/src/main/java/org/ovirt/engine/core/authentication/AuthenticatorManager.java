package org.ovirt.engine.core.authentication;

import java.util.List;

/**
 * The authenticator manager is responsible for managing a collection of authenticator objects.
 */
public class AuthenticatorManager extends Manager<Authenticator> {
    /**
     * This is a singleton, and this is the instance.
     */
    private static AuthenticatorManager instance;

    static {
        instance = new AuthenticatorManager();
    }

    /**
     * Get the instance of the authenticator manager.
     */
    public static AuthenticatorManager getInstance() {
        return instance;
    }

    private AuthenticatorManager() {
        super(AuthenticatorFactory.class);
    }

    /**
     * Parses an authenticator configuration file and creates an instance with that configuration.
     *
     * @param config the properties already loaded from the configuration file
     * @return the reference to the loaded authenticator or {@code null} if something fails while parsing the
     *     configuration
     */
    public Authenticator parseAuthenticator(Configuration config) {
        return parseObject(config);
    }

    /**
     * Returns an unmodifiable list containing all the authenticators that have been previously loaded.
     */
    public List<Authenticator> getAuthenticators() {
        return getObjects();
    }

    /**
     * Gets the authenticator for the given name.
     *
     * @param id the identifier of the authenticator
     * @return the requested authenticator instance or {@code null} if no such authenticator can be found
     */
    public Authenticator getAuthenticator(String id) {
        return getObject(id);
    }

    /**
     * Register an authenticator.
     *
     * @param id the identifier of the authenticator
     * @param authenticator the authenticator to register
     */
    public void registerAuthenticator(String id, Authenticator authenticator) {
        registerObject(id, authenticator);
    }
}
