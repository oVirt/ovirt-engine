package org.ovirt.engine.core.aaa;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.ovirt.engine.api.extensions.Base;
import org.ovirt.engine.api.extensions.aaa.Authn;
import org.ovirt.engine.core.extensions.mgr.ConfigurationException;
import org.ovirt.engine.core.extensions.mgr.ExtensionProxy;
import org.ovirt.engine.core.utils.extensionsmgr.EngineExtensionsManager;

public class AuthenticationProfileRepository {

    private static final String AUTHN_SERVICE = Authn.class.getName();
    private static final String AUTHN_AUTHZ_PLUGIN = "ovirt.engine.aaa.authn.authz.plugin";
    private static final String AUTHN_MAPPING_PLUGIN = "ovirt.engine.aaa.authn.mapping.plugin";


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

        for (ExtensionProxy authnExtension : EngineExtensionsManager.getInstance().getExtensionsByService(AUTHN_SERVICE)) {
            String mapperName = authnExtension.getContext().<Properties>get(Base.ContextKeys.CONFIGURATION).getProperty(AUTHN_MAPPING_PLUGIN);
            String authzName = authnExtension.getContext().<Properties>get(Base.ContextKeys.CONFIGURATION).getProperty(AUTHN_AUTHZ_PLUGIN);
            if (authzName == null) {
                throw new ConfigurationException(String.format("Authz plugin for %1$s does not exist",
                        authnExtension.getContext().<String> get(Base.ContextKeys.INSTANCE_NAME)));
            }

            registerProfile(
                new AuthenticationProfile(
                    authnExtension,
                    EngineExtensionsManager.getInstance().getExtensionByName(authzName),
                    mapperName != null ? EngineExtensionsManager.getInstance().getExtensionByName(mapperName) : null
                    )
                );
        }
    }

}
