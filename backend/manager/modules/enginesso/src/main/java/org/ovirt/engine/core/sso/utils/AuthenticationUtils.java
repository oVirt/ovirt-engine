package org.ovirt.engine.core.sso.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

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

    public static void loginOnBehalf(HttpServletRequest request, String username)
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

        ObjectMapper mapper = new ObjectMapper().configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .enableDefaultTyping(ObjectMapper.DefaultTyping.OBJECT_AND_NON_CONCRETE);
        mapper.getDeserializationConfig().addMixInAnnotations(ExtMap.class, JsonExtMapMixIn.class);
        String authRecordJson = SSOUtils.getRequestParameter(request, SSOConstants.HTTP_PARAM_AUTH_RECORD, "");
        ExtMap authRecord;
        if (StringUtils.isNotEmpty(authRecordJson)) {
            authRecord = mapper.readValue(authRecordJson, ExtMap.class);
        } else {
            authRecord = new ExtMap().mput(Authn.AuthRecord.PRINCIPAL, username);
        }
        SSOSession ssoSession = login(request,
                new Credentials(username, null, profile, SSOUtils.getSsoContext(request).getSsoProfiles().contains(profile)),
                authRecord);
        log.info("User {}@{} successfully logged in using login-on-behalf with client id : {} and scopes : {}",
                username,
                profile,
                ssoSession.getClientId(),
                ssoSession.getScope());
    }

    public static void handleCredentials(HttpServletRequest request,
                                         Credentials credentials)
            throws Exception {
        log.debug("Entered AuthenticationUtils.handleCredentials");
        if (StringUtils.isEmpty(credentials.getUsername()) || StringUtils.isEmpty(credentials.getProfile())) {
            throw new AuthenticationException("Please provide username, password and profile.");
        }
        SSOSession ssoSession = login(request, credentials, null);
        log.info("User {}@{} successfully logged in with scopes: {}",
                credentials.getUsername(),
                credentials.getProfile(),
                ssoSession.getScope());
    }

    private static SSOSession login(HttpServletRequest request,
                              Credentials credentials,
                              ExtMap authRecord)
            throws Exception {
        ExtensionProxy authn = null;
        ExtensionProxy authz = null;
        ExtensionProxy mapper = null;
        SSOContext ssoContext = SSOUtils.getSsoContext(request);
        for (ExtensionProxy authnExtension : ssoContext.getSsoExtensionsManager().getExtensionsByService(Authn.class.getName())) {
            Properties config = authnExtension.getContext().get(Base.ContextKeys.CONFIGURATION);
            if (credentials.getProfile().equals(config.getProperty(Authn.ConfigKeys.PROFILE_NAME))) {
                String mapperName = authnExtension.getContext().<Properties>get(Base.ContextKeys.CONFIGURATION).getProperty(Authn.ConfigKeys.MAPPING_PLUGIN);
                String authzName = authnExtension.getContext().<Properties>get(Base.ContextKeys.CONFIGURATION).getProperty(Authn.ConfigKeys.AUTHZ_PLUGIN);
                mapper = mapperName != null ? ssoContext.getSsoExtensionsManager().getExtensionByName(mapperName) : null;
                authn = authnExtension;
                authz = ssoContext.getSsoExtensionsManager().getExtensionByName(authzName);
                break;
            }
        }
        if (authn == null || authz == null) {
            log.debug("AuthenticationUtils.handleCredentials authn and authz NOT found for profile {}", credentials.getProfile());
            throw new RuntimeException(String.format("Error in obtaining profile %s", credentials.getProfile()));
        }
        log.debug("AuthenticationUtils.handleCredentials authn and authz found for profile %s", credentials.getProfile());
        String user = credentials.getUsername();
        if (mapper != null) {
            log.debug("AuthenticationUtils.handleCredentials invoking MAP_USER on mapper");
            user = mapper.invoke(new ExtMap().mput(
                            Base.InvokeKeys.COMMAND,
                            Mapping.InvokeCommands.MAP_USER
                    ).mput(
                            Mapping.InvokeKeys.USER,
                            user),
                    true).get(Mapping.InvokeKeys.USER, user);
        }
        if (authRecord == null) {
            log.debug("AuthenticationUtils.handleCredentials invoking AUTHENTICATE_CREDENTIALS on authn");
            ExtMap outputMap = authn.invoke(new ExtMap().mput(
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
                log.debug("AuthenticationUtils.handleCredentials AUTHENTICATE_CREDENTIALS on authn failed");
                throw new AuthenticationException(AuthnMessageMapper.mapMessageErrorCode(outputMap));
            }
            log.debug("AuthenticationUtils.handleCredentials AUTHENTICATE_CREDENTIALS on authn succeeded");
            authRecord = outputMap.get(Authn.InvokeKeys.AUTH_RECORD);
        }

        if (mapper != null) {
            log.debug("AuthenticationUtils.handleCredentials invoking MAP_AUTH_RECORD on mapper");
            authRecord = mapper.invoke(
                    new ExtMap().mput(
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
        ExtMap output = authz.invoke(new ExtMap().mput(
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

    public static List<String> getAvailableProfiles(SSOExtensionsManager extensionsManager) {
        List<String> profiles = new ArrayList<>();
        for (ExtensionProxy authnExtension : extensionsManager.getExtensionsByService(Authn.class.getName())) {
            Properties config = authnExtension.getContext().get(Base.ContextKeys.CONFIGURATION);
            profiles.add(config.getProperty(Authn.ConfigKeys.PROFILE_NAME));
        }
        return profiles;
    }

    public static List<String> getAvailableProfilesSupportingPasswd(SSOExtensionsManager extensionsManager) {
        List<String> profiles = new ArrayList<>();
        for (ExtensionProxy authnExtension : extensionsManager.getExtensionsByService(Authn.class.getName())) {
            if ((authnExtension.getContext().<Long>get(Authn.ContextKeys.CAPABILITIES, 0L) & Authn.Capabilities.AUTHENTICATE_PASSWORD) != 0) {
                Properties config = authnExtension.getContext().get(Base.ContextKeys.CONFIGURATION);
                profiles.add(config.getProperty(Authn.ConfigKeys.PROFILE_NAME));
            }
        }
        return profiles;
    }
}
