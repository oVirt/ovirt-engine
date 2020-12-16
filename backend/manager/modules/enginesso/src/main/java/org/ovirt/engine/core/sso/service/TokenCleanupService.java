package org.ovirt.engine.core.sso.service;

import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.api.extensions.Base;
import org.ovirt.engine.api.extensions.ExtMap;
import org.ovirt.engine.api.extensions.aaa.Authn;
import org.ovirt.engine.core.extensions.mgr.ExtensionProxy;
import org.ovirt.engine.core.sso.api.SsoContext;
import org.ovirt.engine.core.sso.api.SsoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TokenCleanupService {
    private static long lastCleanup = 0;
    private static Logger log = LoggerFactory.getLogger(TokenCleanupService.class);

    public static synchronized void cleanupExpiredTokens(ServletContext ctx) {
        SsoContext ssoContext = SsoService.getSsoContext(ctx);
        long currentTime = System.nanoTime();
        if (currentTime
                - lastCleanup < (ssoContext.getSsoLocalConfig().getLong("SSO_HOUSE_KEEPING_INTERVAL") * 1000000000)) {
            log.debug("Not cleaning up expired tokens");
            return;
        }
        lastCleanup = currentTime;
        log.debug("Cleaning up expired tokens");
        long tokenTimeout = ssoContext.getSsoLocalConfig().getLong("SSO_TOKEN_TIMEOUT") * 1000000000;

        for (Map.Entry<String, SsoSession> entry : ssoContext.getSsoSessions().entrySet()) {
            if ((currentTime - entry.getValue().getTokenLastAccess()) > tokenTimeout) {
                try {
                    cleanupSsoSession(ssoContext, entry.getValue(), entry.getValue().getAssociatedClientIds());
                } catch (Exception ex) {
                    log.error("Unable to cleanup expired session for token {} : {}", entry.getKey(), ex.getMessage());
                    log.debug("Exception", ex);
                }
            }
        }
        log.debug("Done cleaning up expired tokens");
    }

    public static void cleanupSsoSession(
            SsoContext ssoContext,
            SsoSession ssoSession,
            Set<String> associateClientIds) {
        try {
            ssoContext.removeSsoSession(ssoSession.getAccessToken());
            HttpSession existingSession = ssoSession.getHttpSession();
            if (existingSession == null) {
                log.debug("No existing Session found for token: {}, cannot invalidate session",
                        ssoSession.getAccessToken());
            } else {
                log.debug("Existing Session found for token: {}, invalidating session", ssoSession.getAccessToken());
                try {
                    existingSession.invalidate();
                } catch (IllegalStateException ex) {
                    log.debug("Unable to cleanup SsoSession: {}", ex.getMessage());
                }
            }
            if (ssoContext.getSsoLocalConfig().getBoolean("ENGINE_SSO_ENABLE_EXTERNAL_SSO")) {
                log.debug("Existing Session found for token: {}, invalidating session on external OP",
                        ssoSession.getAccessToken());
                ExternalOIDCService.logout(ssoContext, ssoSession.getRefreshToken());
            }
            invokeAuthnLogout(ssoContext, ssoSession);
            SsoService.notifyClientsOfLogoutEvent(ssoContext,
                    associateClientIds,
                    ssoSession.getAccessToken());
        } catch (Exception ex) {
            log.error("Unable to cleanup SsoSession: {}", ex.getMessage());
            log.debug("Exception", ex);
        }
    }

    private static void invokeAuthnLogout(SsoContext ssoContext, SsoSession ssoSession) throws Exception {
        String profileName = ssoSession.getProfile();
        String principalName = ssoSession.getUserId();
        ExtMap authRecord = null;
        ExtensionProxy authn = null;

        try {
            authRecord = ssoSession.getAuthRecord();
            if (StringUtils.isNotEmpty(profileName) && StringUtils.isNotEmpty(principalName)) {
                for (ExtensionProxy authnExtension : ssoContext.getSsoExtensionsManager()
                        .getExtensionsByService(Authn.class.getName())) {
                    Properties config = authnExtension.getContext().get(Base.ContextKeys.CONFIGURATION);
                    if (profileName.equals(config.getProperty(Authn.ConfigKeys.PROFILE_NAME))) {
                        authn = authnExtension;
                        break;
                    }
                }
            }
        } catch (Exception ex) {
            throw new RuntimeException(String.format("Unable to invalidate sessions for token: %s", ex.getMessage()));
        } finally {
            if (authn != null && authRecord != null &&
                    (authn.getContext().<Long> get(Authn.ContextKeys.CAPABILITIES) & Authn.Capabilities.LOGOUT) != 0) {
                authn.invoke(new ExtMap().mput(
                        Base.InvokeKeys.COMMAND,
                        Authn.InvokeCommands.LOGOUT)
                        .mput(
                                Authn.InvokeKeys.AUTH_RECORD,
                                authRecord));
            }
        }
    }
}
