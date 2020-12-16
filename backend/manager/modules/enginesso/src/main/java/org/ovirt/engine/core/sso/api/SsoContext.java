package org.ovirt.engine.core.sso.api;

import java.io.Serializable;
import java.security.cert.Certificate;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.api.extensions.Base;
import org.ovirt.engine.api.extensions.aaa.Authn;
import org.ovirt.engine.core.extensions.mgr.ConfigurationException;
import org.ovirt.engine.core.extensions.mgr.ExtensionProxy;
import org.ovirt.engine.core.sso.service.LocalizationService;
import org.ovirt.engine.core.sso.service.NegotiateAuthService;
import org.ovirt.engine.core.sso.service.SsoClientsRegistry;
import org.ovirt.engine.core.sso.service.SsoExtensionsManager;
import org.ovirt.engine.core.sso.utils.SsoLocalConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SsoContext implements Serializable {
    private static final long serialVersionUID = 2059075681091705372L;

    private SsoLocalConfig ssoLocalConfig;
    private SsoExtensionsManager ssoExtensionsManager;
    private NegotiateAuthService negotiateAuthService;
    private LocalizationService localizationService;
    private String ssoDefaultProfile;
    private List<String> ssoProfiles;
    private List<String> ssoProfilesSupportingPasswd;
    private List<String> ssoProfilesSupportingPasswdChange;
    private SsoClientsRegistry ssoClientRegistry;
    private Map<String, SsoSession> ssoSessions = new ConcurrentHashMap<>();
    private Map<String, SsoSession> ssoSessionsById = new ConcurrentHashMap<>();
    private Map<String, AuthenticationProfile> profiles = null;
    private Map<String, List<String>> scopeDependenciesMap = new HashMap<>();
    private String engineUrl;
    private Certificate engineCertificate;
    private static final Logger log = LoggerFactory.getLogger(SsoContext.class);

    public void init(SsoLocalConfig ssoLocalConfig) {
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
                String mapperName = authnExtension.getContext()
                        .<Properties> get(Base.ContextKeys.CONFIGURATION)
                        .getProperty(Authn.ConfigKeys.MAPPING_PLUGIN);
                String authzName = authnExtension.getContext()
                        .<Properties> get(Base.ContextKeys.CONFIGURATION)
                        .getProperty(Authn.ConfigKeys.AUTHZ_PLUGIN);
                AuthenticationProfile profile = new AuthenticationProfile(
                        authnExtension,
                        ssoExtensionsManager.getExtensionByName(authzName),
                        mapperName != null ? ssoExtensionsManager.getExtensionByName(mapperName) : null);

                if (results.containsKey(profile.getName())) {
                    log.warn(
                            "Profile name '{}' already registered for '{}', ignoring for '{}'",
                            profile.getName(),
                            results.get(profile.getName()).getAuthnName(),
                            profile.getAuthnName());
                } else {
                    results.put(profile.getName(), profile);
                }
            } catch (ConfigurationException e) {
                log.debug("Exception", e);
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

    public SsoLocalConfig getSsoLocalConfig() {
        return ssoLocalConfig;
    }

    public SsoExtensionsManager getSsoExtensionsManager() {
        return ssoExtensionsManager;
    }

    public void setSsoExtensionsManager(SsoExtensionsManager ssoExtensionsManager) {
        this.ssoExtensionsManager = ssoExtensionsManager;
    }

    public String getSsoDefaultProfile() {
        return ssoDefaultProfile;
    }

    public void setSsoDefaultProfile(String ssoDefaultProfile) {
        this.ssoDefaultProfile = ssoDefaultProfile;
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

    public List<String> getSsoProfilesSupportingPasswdChange() {
        return ssoProfilesSupportingPasswdChange;
    }

    public void setSsoProfilesSupportingPasswdChange(List<String> ssoProfiles) {
        this.ssoProfilesSupportingPasswdChange = ssoProfiles;
    }

    public void setSsoClientRegistry(SsoClientsRegistry ssoClientRegistry) {
        this.ssoClientRegistry = ssoClientRegistry;
    }

    public NegotiateAuthService getNegotiateAuthUtils() {
        return negotiateAuthService;
    }

    public void setNegotiateAuthUtils(NegotiateAuthService negotiateAuthService) {
        this.negotiateAuthService = negotiateAuthService;
    }

    public SsoSession getSsoSession(String token) {
        return ssoSessions.get(token);
    }

    public void registerSsoSession(SsoSession ssoSession) {
        ssoSessions.put(ssoSession.getAccessToken(), ssoSession);
    }

    public void removeSsoSession(String token) {
        ssoSessions.remove(token);
    }

    public SsoSession getSsoSessionById(String id) {
        return ssoSessionsById.get(id);
    }

    public void registerSsoSessionById(String ssoSessionId, SsoSession ssoSession) {
        ssoSession.setSessionIdToken(ssoSessionId);
        ssoSessionsById.put(ssoSessionId, ssoSession);
    }

    public void removeSsoSessionById(SsoSession ssoSession) {
        String id = ssoSession.getSessionIdToken();
        if (StringUtils.isNotEmpty(id)) {
            ssoSessionsById.remove(id);
            ssoSession.setSessionIdToken(null);
        }
    }

    public ClientInfo getClienInfo(String clientId) {
        return ssoClientRegistry.getClientInfo(clientId);
    }

    public String getTokenForAuthCode(String authCode) {
        String token = null;
        for (Map.Entry<String, SsoSession> entry : ssoSessions.entrySet()) {
            if (entry.getValue().getAuthorizationCode().equals(authCode)) {
                token = entry.getKey();
                break;
            }
        }
        return token;
    }

    public String getTokenForOpenIdAuthCode(String authCode) {
        String token = null;
        for (Map.Entry<String, SsoSession> entry : ssoSessions.entrySet()) {
            if (entry.getValue().getAuthorizationCode().equals(authCode)) {
                if (entry.getValue().isTokenIssued()) {
                    entry.getValue().setActive(false);
                } else {
                    token = entry.getKey();
                    // the auth code should be used to obtain token only once
                    entry.getValue().setTokenIssued(true);
                }
                break;
            }
        }
        return token;
    }

    public Map<String, SsoSession> getSsoSessions() {
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

    public LocalizationService getLocalizationUtils() {
        return localizationService;
    }

    public void setLocalizationUtils(LocalizationService localizationService) {
        this.localizationService = localizationService;
    }

    public void setEngineCertificate(Certificate engineCertificate) {
        this.engineCertificate = engineCertificate;
    }

    public Certificate getEngineCertificate() {
        return engineCertificate;
    }

    public String getUserAuthzName(SsoSession ssoSession) {
        return getProfiles()
                .stream()
                .filter(p -> p.getName().equals(ssoSession.getProfile()))
                .findAny()
                .map(AuthenticationProfile::getAuthzName)
                .orElse("");
    }
}
