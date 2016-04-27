package org.ovirt.engine.core.sso.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.ovirt.engine.api.extensions.Base;
import org.ovirt.engine.api.extensions.ExtMap;
import org.ovirt.engine.api.extensions.aaa.Authn;
import org.ovirt.engine.api.extensions.aaa.Authz;
import org.ovirt.engine.api.extensions.aaa.Mapping;
import org.ovirt.engine.core.extensions.mgr.ExtensionProxy;
import org.ovirt.engine.core.sso.search.AuthzUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthenticationUtils {
    private static Logger log = LoggerFactory.getLogger(AuthenticationUtils.class);

    public static void loginOnBehalf(SsoContext ssoContext, HttpServletRequest request, String username)
            throws Exception {
        log.debug("Entered AuthenticationUtils.loginOnBehalf");
        int index = username.lastIndexOf("@");
        String profile = null;
        if (index != -1) {
            profile = username.substring(index + 1);
            username = username.substring(0, index);
        }
        if (StringUtils.isEmpty(username) || StringUtils.isEmpty(profile)) {
            throw new AuthenticationException(
                    ssoContext.getLocalizationUtils().localize(
                            SsoConstants.APP_ERROR_PROVIDE_USERNAME_AND_PROFILE,
                            (Locale) request.getAttribute(SsoConstants.LOCALE)));
        }

        ObjectMapper mapper = new ObjectMapper()
                .configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .enableDefaultTyping(ObjectMapper.DefaultTyping.OBJECT_AND_NON_CONCRETE);
        mapper.getDeserializationConfig().addMixInAnnotations(ExtMap.class, JsonExtMapMixIn.class);
        String authRecordJson = SsoUtils.getRequestParameter(request, SsoConstants.HTTP_PARAM_AUTH_RECORD, "");
        ExtMap authRecord;
        if (StringUtils.isNotEmpty(authRecordJson)) {
            authRecord = mapper.readValue(authRecordJson, ExtMap.class);
        } else {
            authRecord = new ExtMap().mput(Authn.AuthRecord.PRINCIPAL, username);
        }
        SsoSession ssoSession = login(ssoContext,
                request,
                new Credentials(username,
                        null,
                        profile,
                        SsoUtils.getSsoContext(request).getSsoProfiles().contains(profile)),
                authRecord);
        log.info("User {}@{} successfully logged in using login-on-behalf with client id : {} and scopes : {}",
                username,
                profile,
                ssoSession.getClientId(),
                ssoSession.getScope());
    }

    public static void handleCredentials(
            SsoContext ssoContext,
            HttpServletRequest request,
            Credentials credentials) throws Exception {
        log.debug("Entered AuthenticationUtils.handleCredentials");
        if (StringUtils.isEmpty(credentials.getUsername()) || StringUtils.isEmpty(credentials.getProfile())) {
            throw new AuthenticationException(
                    ssoContext.getLocalizationUtils().localize(
                            SsoConstants.APP_ERROR_PROVIDE_USERNAME_PASSWORD_AND_PROFILE,
                            (Locale) request.getAttribute(SsoConstants.LOCALE)));
        }
        SsoSession ssoSession = login(ssoContext, request, credentials, null);
        log.info("User {}@{} successfully logged in with scopes: {}",
                credentials.getUsername(),
                credentials.getProfile(),
                ssoSession.getScope());
    }

    private static SsoSession login(
            SsoContext ssoContext,
            HttpServletRequest request,
            Credentials credentials,
            ExtMap authRecord) throws Exception {
        ExtensionProfile profile = getExtensionProfile(ssoContext, credentials.getProfile());
        String user = mapUser(profile, credentials);
        if (authRecord == null) {
            log.debug("AuthenticationUtils.handleCredentials invoking AUTHENTICATE_CREDENTIALS on authn");
            ExtMap outputMap = profile.authn.invoke(new ExtMap().mput(
                            Base.InvokeKeys.COMMAND,
                            Authn.InvokeCommands.AUTHENTICATE_CREDENTIALS
                    ).mput(
                            Authn.InvokeKeys.USER,
                            user
                    ).mput(
                            Authn.InvokeKeys.CREDENTIALS,
                            credentials.getPassword()
                    )
            );
            if (outputMap.<Integer>get(Base.InvokeKeys.RESULT) != Base.InvokeResult.SUCCESS ||
                    outputMap.<Integer>get(Authn.InvokeKeys.RESULT) != Authn.AuthResult.SUCCESS) {
                SsoUtils.getSsoSession(request).setChangePasswdCredentials(credentials);
                log.debug("AuthenticationUtils.handleCredentials AUTHENTICATE_CREDENTIALS on authn failed");
                throw new AuthenticationException(
                        AuthnMessageMapper.mapMessageErrorCode(
                                ssoContext,
                                request,
                                credentials.getProfile(),
                                outputMap));
            }
            log.debug("AuthenticationUtils.handleCredentials AUTHENTICATE_CREDENTIALS on authn succeeded");
            authRecord = outputMap.get(Authn.InvokeKeys.AUTH_RECORD);
        }

        if (profile.mapper != null) {
            log.debug("AuthenticationUtils.handleCredentials invoking MAP_AUTH_RECORD on mapper");
            authRecord = profile.mapper.invoke(new ExtMap()
                    .mput(
                            Base.InvokeKeys.COMMAND,
                            Mapping.InvokeCommands.MAP_AUTH_RECORD
                    ).mput(
                            Authn.InvokeKeys.AUTH_RECORD,
                            authRecord
                    ),
                    true
            ).get(
                    Authn.InvokeKeys.AUTH_RECORD,
                    authRecord
            );
        }

        log.debug("AuthenticationUtils.handleCredentials invoking FETCH_PRINCIPAL_RECORD on authz");
        ExtMap output = profile.authz.invoke(new ExtMap().mput(
                Base.InvokeKeys.COMMAND,
                Authz.InvokeCommands.FETCH_PRINCIPAL_RECORD
        ).mput(
                Authn.InvokeKeys.AUTH_RECORD,
                authRecord
        ).mput(
                Authz.InvokeKeys.QUERY_FLAGS,
                Authz.QueryFlags.RESOLVE_GROUPS | Authz.QueryFlags.RESOLVE_GROUPS_RECURSIVE
        ));
        log.debug("AuthenticationUtils.handleCredentials saving data in session data");
        return SsoUtils.persistAuthInfoInContextWithToken(request,
                credentials.getPassword(),
                credentials.getProfile(),
                authRecord,
                output.get(Authz.InvokeKeys.PRINCIPAL_RECORD));
    }

    public static void changePassword(SsoContext context, HttpServletRequest request, Credentials credentials)
            throws AuthenticationException {
        ExtensionProfile profile = getExtensionProfile(context, credentials.getProfile());
        String user = mapUser(profile, credentials);
        log.debug("AuthenticationUtils.changePassword invoking CREDENTIALS_CHANGE on authn");
        ExtMap outputMap = profile.authn.invoke(new ExtMap()
                .mput(
                        Base.InvokeKeys.COMMAND,
                        Authn.InvokeCommands.CREDENTIALS_CHANGE
                ).mput(
                        Authn.InvokeKeys.USER,
                        user
                ).mput(
                        Authn.InvokeKeys.CREDENTIALS,
                        credentials.getCredentials()
                ).mput(
                        Authn.InvokeKeys.CREDENTIALS_NEW,
                        credentials.getNewCredentials()
                )
        );
        if (outputMap.<Integer>get(Base.InvokeKeys.RESULT) != Base.InvokeResult.SUCCESS ||
                outputMap.<Integer>get(Authn.InvokeKeys.RESULT) != Authn.AuthResult.SUCCESS) {
            SsoUtils.getSsoSession(request).setChangePasswdCredentials(credentials);
            log.debug("AuthenticationUtils.changePassword CREDENTIALS_CHANGE on authn failed");
            throw new AuthenticationException(
                    AuthnMessageMapper.mapMessageErrorCode(
                            context,
                            request,
                            credentials.getProfile(),
                            outputMap));
        }
        log.debug("AuthenticationUtils.changePassword CREDENTIALS_CHANGE on authn succeeded");
    }

    public static Map<String, List<String>> getAvailableNamesSpaces(SsoExtensionsManager extensionsManager) {
        Map<String, List<String>> namespacesMap = new HashMap<>();
        extensionsManager.getExtensionsByService(Authz.class.getName())
                .forEach(authz -> {
                    String authzName = authz.getContext().<String> get(Base.ContextKeys.INSTANCE_NAME);
                    authz.getContext().<Collection<String>>get(Authz.ContextKeys.AVAILABLE_NAMESPACES,
                            Collections.<String>emptyList())
                            .forEach(namespace -> {
                                if (!namespacesMap.containsKey(authzName)) {
                                    namespacesMap.put(authzName, new ArrayList<>());
                                }
                                namespacesMap.get(authzName).add(namespace);
                            });
                });

        namespacesMap.values().forEach(Collections::sort);
        return namespacesMap;
    }

    public static List<Map<String, Object>> getProfileList(SsoExtensionsManager extensionsManager) {
        return extensionsManager.getExtensionsByService(Authn.class.getName()).stream()
                .map((authn) -> AuthenticationUtils.getProfileEntry(extensionsManager, authn))
                .collect(Collectors.toList());
    }

    public static String getDefaultProfile(SsoExtensionsManager extensionsManager) {
        Optional<ExtensionProxy> defaultExtension =  extensionsManager.getExtensionsByService(Authn.class.getName())
                .stream()
                .filter(a -> Boolean.valueOf(a.getContext().<Properties>get(Base.ContextKeys.CONFIGURATION)
                                .getProperty(Authn.ConfigKeys.DEFAULT_PROFILE)))
                .findFirst();
        return defaultExtension.isPresent() ? getProfileName(defaultExtension.get()) : null;
    }

    public static List<String> getAvailableProfiles(SsoExtensionsManager extensionsManager) {
        return extensionsManager.getExtensionsByService(Authn.class.getName()).stream()
                .map(AuthenticationUtils::getProfileName)
                .collect(Collectors.toList());
    }

    public static List<String> getAvailableProfilesSupportingPasswd(SsoExtensionsManager extensionsManager) {
        return getAvailableProfilesImpl(extensionsManager, Authn.Capabilities.AUTHENTICATE_PASSWORD);
    }

    public static List<String> getAvailableProfilesSupportingPasswdChange(SsoExtensionsManager extensionsManager) {
        return getAvailableProfilesImpl(extensionsManager, Authn.Capabilities.CREDENTIALS_CHANGE);
    }

    public static ExtensionProfile getExtensionProfile(SsoContext ssoContext, String profileName) {
        Optional<ExtensionProfile> profile = getExtensionProfileImpl(ssoContext, profileName, null);
        if (!profile.isPresent()) {
            log.debug("AuthenticationUtils.getExtensionProfile authn and authz NOT found for profile {}", profileName);
            throw new RuntimeException(String.format("Error in obtaining profile %s", profileName));
        }
        log.debug("AuthenticationUtils.getExtensionProfile authn and authz found for profile %s", profileName);
        return profile.get();
    }

    public static ExtensionProfile getExtensionProfileByAuthzName(SsoContext ssoContext, String authzName) {
        Optional<ExtensionProfile> profile = getExtensionProfileImpl(ssoContext, null, authzName);
        if (!profile.isPresent()) {
            log.debug("AuthenticationUtils.getExtensionProfile authn and authz NOT found for authz {}", authzName);
            throw new RuntimeException(String.format("Error in obtaining profile for authz %s", authzName));
        }
        log.debug("AuthenticationUtils.getExtensionProfile authn and authz found for authz %s", authzName);
        return profile.get();
    }

    private static List<String> getAvailableProfilesImpl(SsoExtensionsManager extensionsManager,
                                                         long capability) {
        return extensionsManager.getExtensionsByService(Authn.class.getName()).stream()
                .filter(a -> (a.getContext().<Long>get(Authn.ContextKeys.CAPABILITIES, 0L)
                        & capability) != 0)
                .map(AuthenticationUtils::getProfileName)
                .collect(Collectors.toList());
    }

    private static String getProfileName(ExtensionProxy proxy) {
        return proxy.getContext().<Properties>get(Base.ContextKeys.CONFIGURATION)
                .getProperty(Authn.ConfigKeys.PROFILE_NAME);
    }

    private static Map<String, Object> getProfileEntry(SsoExtensionsManager extensionsManager, ExtensionProxy authn) {
        Map<String, Object> profileEntry = new HashMap<>();
        profileEntry.put("authn_name", getProfileName(authn));
        ExtensionProxy authz = extensionsManager.getExtensionByName(getAuthzName(authn));
        profileEntry.put("authz_name", AuthzUtils.getName(authz));
        profileEntry.put("capability_password_auth", AuthzUtils.supportsPasswordAuthentication(authz));
        return profileEntry;
    }

    private static String getAuthzName(ExtensionProxy proxy) {
        return proxy.getContext().<Properties>get(Base.ContextKeys.CONFIGURATION)
                .getProperty(Authn.ConfigKeys.AUTHZ_PLUGIN);
    }

    private static Optional<ExtensionProfile> getExtensionProfileImpl(SsoContext ssoContext,
                                                                     final String searchProfileName,
                                                                     final String searchAuthzName) {
        return ssoContext.getSsoExtensionsManager().getExtensionsByService(Authn.class.getName()).stream()
                .filter(a -> matchesSearchName(a, searchProfileName, searchAuthzName))
                .map(a -> mapToExtensionProfile(ssoContext, a))
                .findFirst();
    }

    private static boolean matchesSearchName(ExtensionProxy authn,
                                             String searchProfileName,
                                             String searchAuthzName) {
        return (StringUtils.isNotEmpty(searchProfileName) && searchProfileName.equals(getProfileName(authn))) ||
                (StringUtils.isNotEmpty(searchAuthzName) && searchAuthzName.equals(getAuthzName(authn)));
    }

    private static ExtensionProfile mapToExtensionProfile(SsoContext ssoContext, ExtensionProxy authn) {
        ExtensionProfile profile = new ExtensionProfile();
        String mapperName = authn.getContext().<Properties>get(Base.ContextKeys.CONFIGURATION)
                .getProperty(Authn.ConfigKeys.MAPPING_PLUGIN);
        profile.mapper = mapperName != null ?
                ssoContext.getSsoExtensionsManager().getExtensionByName(mapperName) : null;
        profile.authn = authn;
        profile.authz = ssoContext.getSsoExtensionsManager().getExtensionByName(getAuthzName(authn));
        return profile;
    }

    private static String mapUser(ExtensionProfile profile, Credentials credentials) {
        String user = credentials.getUsername();
        if (profile.mapper != null) {
            log.debug("AuthenticationUtils.handleCredentials invoking MAP_USER on mapper");
            user = profile.mapper.invoke(new ExtMap()
                            .mput(
                                    Base.InvokeKeys.COMMAND,
                                    Mapping.InvokeCommands.MAP_USER
                            )
                            .mput(
                                    Mapping.InvokeKeys.USER,
                                    user
                            ),
                    true).get(Mapping.InvokeKeys.USER, user);
        }
        return user;
    }

    public static class ExtensionProfile {
        private ExtensionProxy authn;
        private ExtensionProxy authz;
        private ExtensionProxy mapper;

        public ExtensionProxy getAuthn() {
            return authn;
        }

        public ExtensionProxy getAuthz() {
            return authz;
        }

        public ExtensionProxy getMapper() {
            return mapper;
        }
    }
}
