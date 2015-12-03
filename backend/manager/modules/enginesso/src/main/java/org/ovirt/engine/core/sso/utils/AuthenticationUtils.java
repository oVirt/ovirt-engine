package org.ovirt.engine.core.sso.utils;

import java.util.List;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthenticationUtils {
    private static Logger log = LoggerFactory.getLogger(AuthenticationUtils.class);

    public static void loginOnBehalf(SSOContext ssoContext, HttpServletRequest request, String username)
            throws Exception {
        log.debug("Entered AuthenticationUtils.loginOnBehalf");
        int index = username.lastIndexOf("@");
        String profile = null;
        if (index != -1) {
            profile = username.substring(index + 1);
            username = username.substring(0, index);
        }
        if (StringUtils.isEmpty(username) || StringUtils.isEmpty(profile)) {
            throw new AuthenticationException("Please provide username and profile.");
        }

        ObjectMapper mapper = new ObjectMapper()
                .configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .enableDefaultTyping(ObjectMapper.DefaultTyping.OBJECT_AND_NON_CONCRETE);
        mapper.getDeserializationConfig().addMixInAnnotations(ExtMap.class, JsonExtMapMixIn.class);
        String authRecordJson = SSOUtils.getRequestParameter(request, SSOConstants.HTTP_PARAM_AUTH_RECORD, "");
        ExtMap authRecord;
        if (StringUtils.isNotEmpty(authRecordJson)) {
            authRecord = mapper.readValue(authRecordJson, ExtMap.class);
        } else {
            authRecord = new ExtMap().mput(Authn.AuthRecord.PRINCIPAL, username);
        }
        SSOSession ssoSession = login(ssoContext,
                request,
                new Credentials(username,
                        null,
                        profile,
                        SSOUtils.getSsoContext(request).getSsoProfiles().contains(profile)),
                authRecord);
        log.info("User {}@{} successfully logged in using login-on-behalf with client id : {} and scopes : {}",
                username,
                profile,
                ssoSession.getClientId(),
                ssoSession.getScope());
    }

    public static void handleCredentials(
            SSOContext ssoContext,
            HttpServletRequest request,
            Credentials credentials) throws Exception {
        log.debug("Entered AuthenticationUtils.handleCredentials");
        if (StringUtils.isEmpty(credentials.getUsername()) || StringUtils.isEmpty(credentials.getProfile())) {
            throw new AuthenticationException("Please provide username, password and profile.");
        }
        SSOSession ssoSession = login(ssoContext, request, credentials, null);
        log.info("User {}@{} successfully logged in with scopes: {}",
                credentials.getUsername(),
                credentials.getProfile(),
                ssoSession.getScope());
    }

    private static SSOSession login(
            SSOContext ssoContext,
            HttpServletRequest request,
            Credentials credentials,
            ExtMap authRecord) throws Exception {
        ExtensionProfile profile = getExtensionProfile(ssoContext, credentials);
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
                SSOUtils.getSsoSession(request).setChangePasswdCredentials(credentials);
                log.debug("AuthenticationUtils.handleCredentials AUTHENTICATE_CREDENTIALS on authn failed");
                throw new AuthenticationException(AuthnMessageMapper.mapMessageErrorCode(request,
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
        return SSOUtils.persistAuthInfoInContextWithToken(request,
                credentials.getPassword(),
                credentials.getProfile(),
                authRecord,
                output.get(Authz.InvokeKeys.PRINCIPAL_RECORD));
    }

    public static void changePassword(SSOContext context, HttpServletRequest request, Credentials credentials)
            throws AuthenticationException {
        ExtensionProfile profile = getExtensionProfile(context, credentials);
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
            SSOUtils.getSsoSession(request).setChangePasswdCredentials(credentials);
            log.debug("AuthenticationUtils.changePassword CREDENTIALS_CHANGE on authn failed");
            throw new AuthenticationException(AuthnMessageMapper.mapMessageErrorCode(request,
                    credentials.getProfile(),
                    outputMap));
        }
        log.debug("AuthenticationUtils.changePassword CREDENTIALS_CHANGE on authn succeeded");
    }

    public static List<String> getAvailableProfiles(SSOExtensionsManager extensionsManager) {
        return extensionsManager.getExtensionsByService(Authn.class.getName()).stream()
                .map(AuthenticationUtils::getProfileName)
                .collect(Collectors.toList());
    }

    public static List<String> getAvailableProfilesSupportingPasswd(SSOExtensionsManager extensionsManager) {
        return getAvailableProfilesImpl(extensionsManager, Authn.Capabilities.AUTHENTICATE_PASSWORD);
    }

    public static List<String> getAvailableProfilesSupportingPasswdChange(SSOExtensionsManager extensionsManager) {
        return getAvailableProfilesImpl(extensionsManager, Authn.Capabilities.CREDENTIALS_CHANGE);
    }

    private static List<String> getAvailableProfilesImpl(SSOExtensionsManager extensionsManager,
                                                         long capability) {
        return extensionsManager.getExtensionsByService(Authn.class.getName()).stream()
                .filter(a -> (a.getContext().<Long>get(Authn.ContextKeys.CAPABILITIES, 0L)
                        & capability) != 0)
                .map(AuthenticationUtils::getProfileName)
                .collect(Collectors.toList());
    }

    private static String getProfileName(ExtensionProxy proxy) {
        return ((Properties) proxy.getContext().get(Base.ContextKeys.CONFIGURATION))
                .getProperty(Authn.ConfigKeys.PROFILE_NAME);
    }

    private static ExtensionProfile getExtensionProfile(SSOContext ssoContext, Credentials credentials) {
        ExtensionProfile profile = new ExtensionProfile();
        for (ExtensionProxy authn : ssoContext.getSsoExtensionsManager().getExtensionsByService(Authn.class.getName())) {
            Properties config = authn.getContext().get(Base.ContextKeys.CONFIGURATION);
            if (credentials.getProfile().equals(config.getProperty(Authn.ConfigKeys.PROFILE_NAME))) {
                String mapperName = authn.getContext().<Properties>get(Base.ContextKeys.CONFIGURATION)
                        .getProperty(Authn.ConfigKeys.MAPPING_PLUGIN);
                String authzName = authn.getContext().<Properties>get(Base.ContextKeys.CONFIGURATION)
                        .getProperty(Authn.ConfigKeys.AUTHZ_PLUGIN);
                profile.mapper = mapperName != null ?
                        ssoContext.getSsoExtensionsManager().getExtensionByName(mapperName) : null;
                profile.authn = authn;
                profile.authz = ssoContext.getSsoExtensionsManager().getExtensionByName(authzName);
                break;
            }
        }
        if (profile.authn == null || profile.authz == null) {
            log.debug("AuthenticationUtils.getExtensionProfile authn and authz NOT found for profile {}",
                    credentials.getProfile());
            throw new RuntimeException(String.format("Error in obtaining profile %s", credentials.getProfile()));
        }
        log.debug("AuthenticationUtils.getExtensionProfile authn and authz found for profile %s",
                credentials.getProfile());
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

    static class ExtensionProfile {
        ExtensionProxy authn;
        ExtensionProxy authz;
        ExtensionProxy mapper;
    }
}
