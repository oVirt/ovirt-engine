package org.ovirt.engine.core.sso.utils;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.ovirt.engine.api.extensions.Base;
import org.ovirt.engine.api.extensions.aaa.Authn;
import org.ovirt.engine.core.extensions.mgr.ConfigurationException;
import org.ovirt.engine.core.extensions.mgr.ExtensionProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SSOContext implements Serializable{
    private static final long serialVersionUID = 2059075681091705372L;

    private SSOLocalConfig ssoLocalConfig;
    private SSOExtensionsManager ssoExtensionsManager;
    private NegotiateAuthUtils negotiateAuthUtils;
    private List<String> ssoProfiles;
    private List<String> ssoProfilesSupportingPasswd;
    private Map<String, ClientInfo> ssoClientRegistry;
    private Map<String, SSOSession> ssoSessions = new ConcurrentHashMap<>();
    private Map<String, AuthenticationProfile> profiles = null;
    private Map<String, List<String>> scopeDependenciesMap = new HashMap<>();
    private String engineUrl;
    private static final Logger log = LoggerFactory.getLogger(SSOContext.class);

    public void init(SSOLocalConfig ssoLocalConfig) {
        this.ssoLocalConfig = ssoLocalConfig;
        engineUrl = ssoLocalConfig.getProperty("SSO_ENGINE_URL");
        createProfiles();
    }

    private void createProfiles() {

        // Get the extensions that correspond to authn (authentication) service.
        // For each extension - get the relevant authn extension.

        Map<String, AuthenticationProfile> results = new HashMap<>();
        for (ExtensionProxy authnExtension : ssoExtensionsManager.getExtensionsByService(Authn.class.getName())) {
            try {
                String mapperName = authnExtension.getContext().<Properties>get(Base.ContextKeys.CONFIGURATION).getProperty(Authn.ConfigKeys.MAPPING_PLUGIN);
                String authzName = authnExtension.getContext().<Properties>get(Base.ContextKeys.CONFIGURATION).getProperty(Authn.ConfigKeys.AUTHZ_PLUGIN);
                AuthenticationProfile profile = new AuthenticationProfile(
                        authnExtension,
                        ssoExtensionsManager.getExtensionByName(authzName),
                        mapperName != null ? ssoExtensionsManager.getExtensionByName(mapperName) : null
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
    }

    /**
     * Returns an unmodifiable list containing all the authentication profiles that have been previously loaded.
     */
    public Collection<AuthenticationProfile> getProfiles() {
        return Collections.unmodifiableCollection(profiles.values());
    }

    public SSOLocalConfig getSsoLocalConfig() {
        return ssoLocalConfig;
    }

    public SSOExtensionsManager getSsoExtensionsManager() {
        return ssoExtensionsManager;
    }

    public void setSsoExtensionsManager(SSOExtensionsManager ssoExtensionsManager) {
        this.ssoExtensionsManager = ssoExtensionsManager;
    }

    public List<String> getSsoProfiles() {
        return ssoProfiles;
    }

    public void setSsoProfiles(List<String> ssoProfiles) {
        this.ssoProfiles = ssoProfiles;
    }

    public List<String> getSsoProfilesSupportingPasswd() {
        return ssoProfilesSupportingPasswd;
    }

    public void setSsoProfilesSupportingPasswd(List<String> ssoProfiles) {
        this.ssoProfilesSupportingPasswd = ssoProfiles;
    }

    public void setSsoClientRegistry(Map<String, ClientInfo> ssoClientRegistry) {
        this.ssoClientRegistry = ssoClientRegistry;
    }

    public NegotiateAuthUtils getNegotiateAuthUtils() {
        return negotiateAuthUtils;
    }

    public void setNegotiateAuthUtils(NegotiateAuthUtils negotiateAuthUtils) {
        this.negotiateAuthUtils = negotiateAuthUtils;
    }

    public SSOSession getSsoSession(String token) {
        return ssoSessions.get(token);
    }

    public void registerSsoSession(SSOSession ssoSession) {
        ssoSessions.put(ssoSession.getAccessToken(), ssoSession);
    }

    public void removeSsoSession(String token) {
        ssoSessions.remove(token);
    }

    public ClientInfo getClienInfo(String clientId) {
        return ssoClientRegistry.get(clientId);
    }

    public String getTokenForAuthCode(String authCode) {
        String token = null;
        for (Map.Entry<String, SSOSession> entry : ssoSessions.entrySet()) {
            if (entry.getValue().getAuthorizationCode().equals(authCode)) {
                token = entry.getKey();
                break;
            }
        }
        return token;
    }

    public Map<String, SSOSession> getSsoSessions() {
        return ssoSessions;
    }

    public String getEngineUrl() {
        return engineUrl;
    }

    public void setScopeDependencies(Map<String, List<String>> scopeDependenciesMap) {
        this.scopeDependenciesMap = scopeDependenciesMap;
    }

    public List<String> getScopeDependencies(String scope) {
        if (!scopeDependenciesMap.containsKey(scope)) {
            return Collections.emptyList();
        }
        return scopeDependenciesMap.get(scope);
    }
}
