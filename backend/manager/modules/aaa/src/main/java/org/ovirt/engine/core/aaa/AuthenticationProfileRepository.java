package org.ovirt.engine.core.aaa;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Properties;

import org.ovirt.engine.api.extensions.Base;
import org.ovirt.engine.api.extensions.aaa.Authn;
import org.ovirt.engine.core.extensions.mgr.ConfigurationException;
import org.ovirt.engine.core.extensions.mgr.ExtensionProxy;
import org.ovirt.engine.core.utils.extensionsmgr.EngineExtensionsManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthenticationProfileRepository extends Observable {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationProfileRepository.class);

    private static volatile AuthenticationProfileRepository instance = null;
    private volatile Map<String, AuthenticationProfile> profiles = null;


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

    public void registerProfile(AuthenticationProfile profile) {
        profiles.put(profile.getName(), profile);
    }

    private AuthenticationProfileRepository() {
        EngineExtensionsManager.getInstance().addObserver((o, arg) -> createProfiles());
        createProfiles();
    }

    private void createProfiles() {

        // Get the extensions that correspond to authn (authentication) service.
        // For each extension - get the relevant authn extension.

        Map<String, AuthenticationProfile> results = new HashMap<>();
        for (ExtensionProxy authnExtension : EngineExtensionsManager.getInstance().getExtensionsByService(Authn.class.getName())) {
            try {
                String mapperName = authnExtension.getContext().<Properties>get(Base.ContextKeys.CONFIGURATION).getProperty(Authn.ConfigKeys.MAPPING_PLUGIN);
                String authzName = authnExtension.getContext().<Properties>get(Base.ContextKeys.CONFIGURATION).getProperty(Authn.ConfigKeys.AUTHZ_PLUGIN);
                AuthenticationProfile profile = new AuthenticationProfile(
                        authnExtension,
                        authzName,
                        mapperName != null ? EngineExtensionsManager.getInstance().getExtensionByName(mapperName) : null
                        );

                if (results.containsKey(profile.getName())) {
                    log.warn(
                            "Profile name '{}' already registered for '{}', ignoring for '{}'",
                            profile.getName(),
                            results.get(profile.getName()).getAuthnName(),
                            profile.getAuthnName()
                    );
                } else {
                    results.put(profile.getName(), profile);
                }
            } catch (ConfigurationException e) {
                log.debug("Ignoring", e);
            }
        }
        profiles = results;
        setChanged();
        notifyObservers();
    }

}
