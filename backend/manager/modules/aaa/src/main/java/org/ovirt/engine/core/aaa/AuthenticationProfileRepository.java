package org.ovirt.engine.core.aaa;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.extensions.mgr.ConfigurationException;
import org.ovirt.engine.core.extensions.mgr.ExtensionsManager;
import org.ovirt.engine.core.extensions.mgr.ExtensionsManager.ExtensionEntry;

public class AuthenticationProfileRepository {

    private static final String AUTHN_SERVICE = "org.ovirt.engine.authentication";
    private static final String AUTHN_AUTHZ_PLUGIN = "ovirt.engine.aaa.authn.authz.plugin";


    private static volatile AuthenticationProfileRepository instance = null;
    private Map<String, AuthenticationProfile> profiles = new HashMap<String, AuthenticationProfile>();


    public static AuthenticationProfileRepository getInstance() {
        if (instance == null) {
            synchronized (AuthenticationProfileRepository.class) {
                if (instance == null) {
                    instance = new AuthenticationProfileRepository();
                }
            }
        }
        return instance;
    }

    /**
     * Returns an unmodifiable list containing all the authentication profiles that have been previously loaded.
     */
    public List<AuthenticationProfile> getProfiles() {
        return new ArrayList<>(profiles.values());
    }

    /**
     * Gets the authentication profile for the given name.
     *
     * @param name
     *            the name of the profile
     * @return the requested profile or {@code null} if no such profile can be found
     */
    public AuthenticationProfile getProfile(String name) {
        return profiles.get(name);
    }

    public Directory getDirectory(String name) {
        return getProfile(name).getDirectory();
    }

    /**
     * Register an authentication profile.
     * @param profile
     *            the profile to register
     */
    public void registerProfile(AuthenticationProfile profile) {
        profiles.put(profile.getName(), profile);
    }

    public void clear() {
        profiles.clear();
    }

    private AuthenticationProfileRepository() {
        createProfiles();
    }

    private void createProfiles() throws ConfigurationException {

        // Get the extensions that correspond to authn (authentication) service.
        // For each extension - get the relevant authn extension.

        for (ExtensionEntry authnExtension : ExtensionsManager.getInstance().getProvidedExtensions(AUTHN_SERVICE)) {
            registerProfile(
                    new AuthenticationProfile(
                    (Authenticator) authnExtension.getExtension(),
                    (Directory) ExtensionsManager.getInstance().getExtensionByName(
                            authnExtension.getConfig().getProperty(AUTHN_AUTHZ_PLUGIN)
                            ).getExtension()
                    )
            );
        }
    }

}
