package org.ovirt.engine.core.bll.aaa;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.enterprise.concurrent.ManagedScheduledExecutorService;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang.time.DateUtils;
import org.ovirt.engine.api.extensions.aaa.Acct;
import org.ovirt.engine.core.aaa.AcctUtils;
import org.ovirt.engine.core.aaa.AuthenticationProfile;
import org.ovirt.engine.core.aaa.AuthenticationProfileRepository;
import org.ovirt.engine.core.aaa.SsoOAuthServiceUtils;
import org.ovirt.engine.core.common.businessentities.EngineSession;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.dao.EngineSessionDao;
import org.ovirt.engine.core.utils.threadpool.ThreadPools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class SessionDataContainer {

    SsoSessionValidator ssoSessionValidator = new SsoSessionValidator();

    @Inject
    SsoSessionUtils ssoSessionUtils;

    @Inject
    @ThreadPools(ThreadPools.ThreadPoolType.EngineScheduledThreadPool)
    private ManagedScheduledExecutorService scheduledExecutorService;

    private static class SessionInfo {
        private ConcurrentMap<String, Object> contentOfSession = new ConcurrentHashMap<>();

    }

    protected Logger log = LoggerFactory.getLogger(getClass());

    private ConcurrentMap<String, SessionInfo> sessionInfoMap = new ConcurrentHashMap<>();

    private static final String USER_PARAMETER_NAME = "user";
    private static final String SOURCE_IP = "source_ip";
    private static final String PROFILE_PARAMETER_NAME = "profile";
    private static final String HARD_LIMIT_PARAMETER_NAME = "hard_limit";
    private static final String SOFT_LIMIT_PARAMETER_NAME = "soft_limit";
    private static final String ENGINE_SESSION_SEQ_ID = "engine_session_seq_id";
    private static final String ENGINE_SESSION_ID = "engine_session_id";
    private static final String PRINCIPAL_PARAMETER_NAME = "username";
    private static final String SSO_ACCESS_TOKEN_PARAMETER_NAME = "sso_access_token";
    private static final String SSO_IS_OVIRT_APP_API_SCOPE_PARAMETER_NAME = "sso_is_ovirt_app_api_scope";
    private static final String SESSION_VALID_PARAMETER_NAME = "session_valid";
    private static final String SOFT_LIMIT_INTERVAL_PARAMETER_NAME = "soft_limit_interval";
    private static final String SESSION_START_TIME = "session_start_time";
    private static final String SESSION_LAST_ACTIVE_TIME = "session_last_active_time";
    private static final String OVIRT_APP_API_SCOPE = "ovirt-app-api";
    private static final String OVIRT_APP_ADMIN_SCOPE = "ovirt-app-admin";
    private static final String OVIRT_APP_PORTAL_SCOPE = "ovirt-app-portal";

    @Inject
    private EngineSessionDao engineSessionDao;

    @PostConstruct
    private void init() {
        scheduledExecutorService.scheduleAtFixedRate(this::cleanExpiredUsersSessions,
                1,
                1, TimeUnit.MINUTES);

    }

    public String generateEngineSessionId() {
        String engineSessionId;
        byte[] s = new byte[64];
        new SecureRandom().nextBytes(s);
        engineSessionId = new Base64(0).encodeToString(s);
        return engineSessionId;
    }

    /**
     * Get data by session and internal key
     *
     * @param sessionId
     *            - id of session
     * @param key
     *            - the internal key
     * @param refresh
     *            - if perform refresh of session
     */
    public final Object getData(String sessionId, String key, boolean refresh) {
        if (sessionId == null) {
            return null;
        }
        SessionInfo sessionInfo = getSessionInfo(sessionId);
        Object value = null;
        if (sessionInfo != null) {
            if (refresh) {
                refresh(sessionInfo);

            }
            value = sessionInfo.contentOfSession.get(key);
        }
        return value;
    }

    public final void setData(String sessionId, String key, Object value) {
        SessionInfo sessionInfo = getSessionInfo(sessionId);
        if (sessionInfo == null) {
            sessionInfo = new SessionInfo();
            sessionInfo.contentOfSession.put(ENGINE_SESSION_ID, sessionId);
            // Add default soft-limit interval for new sessions
            sessionInfo.contentOfSession.put(SOFT_LIMIT_INTERVAL_PARAMETER_NAME,
                    Config.<Integer> getValue(ConfigValues.UserSessionTimeOutInterval));
            SessionInfo oldSessionInfo = sessionInfoMap.putIfAbsent(sessionId, sessionInfo);
            if (oldSessionInfo != null) {
                sessionInfo = oldSessionInfo;
            }
        }
        sessionInfo.contentOfSession.put(key, value);
    }

    private SessionInfo getSessionInfo(String sessionId) {
        return sessionInfoMap.get(sessionId);
    }

    private void persistEngineSession(String sessionId) {
        SessionInfo sessionInfo = getSessionInfo(sessionId);
        if (sessionInfo != null) {
            sessionInfo.contentOfSession.put(ENGINE_SESSION_SEQ_ID,
                    engineSessionDao.save(new EngineSession(getUser(sessionId, false), sessionId, getSourceIp(sessionId))));
            setSessionStartTime(sessionId);
        }
    }

    public long getEngineSessionSeqId(String sessionId) {
        if (!sessionInfoMap.containsKey(sessionId)) {
            throw new RuntimeException("Session not found for sessionId " + sessionId);
        }
        return (Long) sessionInfoMap.get(sessionId).contentOfSession.get(ENGINE_SESSION_SEQ_ID);
    }

    public String getSessionIdBySeqId(long sessionSequenceId) {
        String sessionId = null;
        for (SessionInfo sessionInfo : sessionInfoMap.values()) {
            if (Long.valueOf(sessionSequenceId).equals(sessionInfo.contentOfSession.get(ENGINE_SESSION_SEQ_ID))) {
                sessionId = (String) sessionInfo.contentOfSession.get(ENGINE_SESSION_ID);
                break;
            }
        }
        return sessionId;
    }

    public String getSessionIdBySsoAccessToken(String ssoToken) {
        String sessionId = null;
        if (StringUtils.isNotEmpty(ssoToken)) {
            for (SessionInfo sessionInfo : sessionInfoMap.values()) {
                if (ssoToken.equals(sessionInfo.contentOfSession.get(SSO_ACCESS_TOKEN_PARAMETER_NAME))) {
                    sessionId = (String) sessionInfo.contentOfSession.get(ENGINE_SESSION_ID);
                    break;
                }
            }
        }
        return sessionId;
    }

    public void cleanupEngineSessionsOnStartup() {
        engineSessionDao.removeAll();
    }

    public void cleanupEngineSessionsForSsoAccessToken(String ssoAccessToken) {
        if (StringUtils.isNotEmpty(ssoAccessToken)) {
            Iterator<Entry<String, SessionInfo>> iter = sessionInfoMap.entrySet().iterator();
            while (iter.hasNext()) {
                Entry<String, SessionInfo> entry = iter.next();
                ConcurrentMap<String, Object> sessionMap = entry.getValue().contentOfSession;
                if (ssoAccessToken.equals(sessionMap.get(SSO_ACCESS_TOKEN_PARAMETER_NAME))) {
                    removeSessionImpl(entry.getKey(),
                            Acct.ReportReason.PRINCIPAL_SESSION_EXPIRED,
                            "Session has expired for principal %1$s",
                            getUserName(entry.getKey()));
                }
            }
        }
    }

    /**
     * Remove the cached data of current session
     *
     * @param sessionId
     *            - id of current session
     */
    public final void removeSessionOnLogout(String sessionId) {
        removeSessionImpl(sessionId,
                Acct.ReportReason.PRINCIPAL_LOGOUT,
                "Prinicial %1$s has performed logout",
                getUserName(sessionId));
    }

    /**
     * Will run the process of cleaning expired sessions.
     */
    public final void cleanExpiredUsersSessions() {
        try {
            cleanExpiredUsersSessionsImpl();
        } catch (Throwable t) {
            log.error("Exception in cleanExpiredUsersSessions: {}", ExceptionUtils.getRootCauseMessage(t));
            log.debug("Exception", t);
        }
    }

    public final void cleanExpiredUsersSessionsImpl() {
        Date now = new Date();
        Iterator<Entry<String, SessionInfo>>  iter = sessionInfoMap.entrySet().iterator();
        Set<String> tokens = sessionInfoMap.values().stream()
                .map(sessionInfo -> (String) sessionInfo.contentOfSession.get(SSO_ACCESS_TOKEN_PARAMETER_NAME))
                .collect(Collectors.toSet());
        // retrieve session statues from SSO
        Map<String, Boolean> sessionStatuses = ssoSessionValidator.getSessionStatuses(tokens);

        while (iter.hasNext()) {
            Entry<String, SessionInfo> entry = iter.next();
            ConcurrentMap<String, Object> sessionMap = entry.getValue().contentOfSession;
            Date hardLimit = (Date) sessionMap.get(HARD_LIMIT_PARAMETER_NAME);
            Date softLimit = (Date) sessionMap.get(SOFT_LIMIT_PARAMETER_NAME);
            String token = (String) sessionMap.get(SSO_ACCESS_TOKEN_PARAMETER_NAME);
            // if the session was created after the tokens statuses were retrieved from the server, the token will not
            // have a session status in the sessionStatuses map. The session for the token will be checked and cleaned
            // in the next iteration.
            if (!sessionStatuses.containsKey(token)) {
                continue;
            }
            boolean sessionValid = StringUtils.isEmpty(token) ? false : sessionStatuses.get(token);
            if (((hardLimit != null && hardLimit.before(now)) || (softLimit != null && softLimit.before(now))) ||
                    !(boolean) sessionMap.get(SESSION_VALID_PARAMETER_NAME) ||
                    !sessionValid) {
                removeSessionImpl(entry.getKey(),
                        Acct.ReportReason.PRINCIPAL_SESSION_EXPIRED,
                        "Session has expired for principal %1$s",
                        getUserName(entry.getKey()));
                if (sessionValid) {
                   SsoOAuthServiceUtils.revoke((String) sessionMap.get(SSO_ACCESS_TOKEN_PARAMETER_NAME), "");
                }
            }
        }
    }

    /**
     * Sets the user for the given session Id
     * @param sessionId The session to set
     * @param user The user to set
     */
    public final void setUser(String sessionId, DbUser user) {
        setData(sessionId, USER_PARAMETER_NAME, user);
        setSessionValid(sessionId, true);
        persistEngineSession(sessionId);
    }

    public final void setSessionValid(String sessionId, boolean valid) {
        setData(sessionId, SESSION_VALID_PARAMETER_NAME, valid);
    }

    public final void setSessionStartTime(String sessionId) {
        setData(sessionId, SESSION_START_TIME, new Date());
    }

    public final Date getSessionStartTime(String sessionId) {
        return (Date) getData(sessionId, SESSION_START_TIME, false);
    }

    public final void updateSessionLastActiveTime(String sessionId) {
        if (isSessionExists(sessionId)) {
            setData(sessionId, SESSION_LAST_ACTIVE_TIME, new Date());
            refresh(sessionId);
        }
    }

    public final Date getSessionLastActiveTime(String sessionId) {
        return (Date) getData(sessionId, SESSION_LAST_ACTIVE_TIME, false);
    }

    public boolean getSessionValid(String sessionId, boolean refresh) {
        Object obj = getData(sessionId, SESSION_VALID_PARAMETER_NAME, refresh);
        return obj == null ? false : (boolean) obj;
    }

    public final void setHardLimit(String sessionId, Date hardLimit) {
        setData(sessionId, HARD_LIMIT_PARAMETER_NAME, hardLimit);
    }

    public final void setSoftLimit(String sessionId, Date softLimit) {
        setData(sessionId, SOFT_LIMIT_PARAMETER_NAME, softLimit);
    }

    public final void setSoftLimitInterval(String sessionId, int softLimitInterval) {
        setData(sessionId, SOFT_LIMIT_INTERVAL_PARAMETER_NAME, softLimitInterval);
    }

    /**
     * @param sessionId The session to get the user for
     * @param refresh Whether refreshing the session is needed
     * @return The user set for the given {@link #session}
     */
    public DbUser getUser(String sessionId, boolean refresh) {
        return (DbUser) getData(sessionId, USER_PARAMETER_NAME, refresh);
    }

    public long getNumUserSessions(DbUser user) {
        return sessionInfoMap.keySet()
                .stream()
                .filter(sessionId -> getUser(sessionId, false).getId().equals(user.getId()))
                .filter(sessionId -> getSessionValid(sessionId, false))
                .count();
    }

    public void refresh(String sessionId) {
        refresh(getSessionInfo(sessionId));
    }

    public void setProfile(String sessionId, AuthenticationProfile profile) {
        setData(sessionId, PROFILE_PARAMETER_NAME, profile);
    }

    public AuthenticationProfile getProfile(String sessionId) {
        AuthenticationProfile profile = (AuthenticationProfile) getData(sessionId, PROFILE_PARAMETER_NAME, false);
        if (profile == null) {
            profile = getProfileFromUser(getUser(sessionId, false));
        }
        return profile;
    }

    private AuthenticationProfile getProfileFromUser(DbUser user) {
        AuthenticationProfile retVal = null;
        if (user != null) {
            for (AuthenticationProfile profile : AuthenticationProfileRepository.getInstance().getProfiles()) {
                if (profile.getAuthzName().equals(user.getDomain())) {
                    retVal = profile;
                    break;
                }
            }
        }
        return retVal;
    }

    public String getPrincipalName(String sessionId) {
        return (String) getData(sessionId, PRINCIPAL_PARAMETER_NAME, false);
    }

    public String getUserName(String sessionId) {
        return String.format(
                "%s@%s",
                getPrincipalName(sessionId),
                getProfile(sessionId) != null ? getProfile(sessionId).getAuthzName() : "N/A");
    }

    public void setPrincipalName(String engineSessionId, String name) {
        setData(engineSessionId, PRINCIPAL_PARAMETER_NAME, name);
    }

    public void setSsoAccessToken(String engineSessionId, String ssoToken) {
        setData(engineSessionId, SSO_ACCESS_TOKEN_PARAMETER_NAME, ssoToken);
    }

    public String getSsoAccessToken(String engineSessionId) {
        return (String) getData(engineSessionId, SSO_ACCESS_TOKEN_PARAMETER_NAME, false);
    }

    public String getSsoAccessToken(String engineSessionId, boolean mustBeValid) {
        String ssoToken = getSsoAccessToken(engineSessionId);
        if (StringUtils.isNotEmpty(ssoToken) && mustBeValid) {
            ssoToken = getSessionValid(engineSessionId, false) ? ssoToken : null;
        }
        return ssoToken;
    }

    public void setSsoOvirtAppApiScope(String engineSessionId, String scope) {
        List<String> scopes = StringUtils.isEmpty(scope) ?
                Collections.emptyList() :
                Arrays.asList(scope.trim().split("\\s *"));
        setData(engineSessionId, SSO_IS_OVIRT_APP_API_SCOPE_PARAMETER_NAME,
                scopes.contains(OVIRT_APP_API_SCOPE) &&
                        !scopes.contains(OVIRT_APP_ADMIN_SCOPE) &&
                        !scopes.contains(OVIRT_APP_PORTAL_SCOPE));
    }

    public boolean isSsoOvirtAppApiScope(String engineSessionId) {
        return isSessionExists(engineSessionId) ?
                (boolean) getData(engineSessionId, SSO_IS_OVIRT_APP_API_SCOPE_PARAMETER_NAME, false):
                false;
    }

    public void setSourceIp(String engineSessionId, String sourceIp) {
        setData(engineSessionId, SOURCE_IP, sourceIp);
    }

    public String getSourceIp(String engineSessionId) {
        return (String) getData(engineSessionId, SOURCE_IP, false);
    }

    private void refresh(SessionInfo sessionInfo) {
        int softLimitValue = (Integer) sessionInfo.contentOfSession.get(SOFT_LIMIT_INTERVAL_PARAMETER_NAME);
        if (softLimitValue > 0) {
            sessionInfo.contentOfSession.put(SOFT_LIMIT_PARAMETER_NAME,
                    DateUtils.addMinutes(new Date(), softLimitValue));
        }
    }

    public boolean isSessionExists(String sessionId) {
        return StringUtils.isEmpty(sessionId) ? false : sessionInfoMap.containsKey(sessionId);
    }

    private void removeSessionImpl(String sessionId, int reason, String message, Object... msgArgs) {

        // Only remove session if there are no running commands for this session
        if (ssoSessionUtils.isSessionInUse(getEngineSessionSeqId(sessionId))) {
            DbUser dbUser = getUser(sessionId, false);
            log.info("Not removing session '{}', session has running commands{}",
                    sessionId,
                    dbUser == null ? "." : String.format(" for user '%s@%s'.", dbUser.getLoginName(), dbUser.getDomain()));
            return;
        }

        /*
         * So we won't need to add profile to tests
         */
        String authzName = null;
        if (getProfile(sessionId) != null) {
            authzName = getProfile(sessionId).getAuthzName();
        }

        AcctUtils.reportRecords(reason,
                authzName,
                getPrincipalName(sessionId),
                message,
                msgArgs
                );
        engineSessionDao.remove(getEngineSessionSeqId(sessionId));
        sessionInfoMap.remove(sessionId);
    }

    class SsoSessionValidator {
        public Map<String, Boolean> getSessionStatuses(Set<String> tokens) {
            Map<String, Boolean> sessionStatuses = Collections.emptyMap();
            if (!tokens.isEmpty()) {
                try {
                    Map<String, Object> response = SsoOAuthServiceUtils.getSessionStatues(tokens);
                    if (response.get("error") == null) {
                        sessionStatuses = (Map<String, Boolean>) response.get("result");
                    }
                } catch (Exception e) {
                    log.error("Unable to retrieve session statuses." + e.getMessage());
                }
            }
            return sessionStatuses;
        }
    }
}
