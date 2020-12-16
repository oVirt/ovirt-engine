package org.ovirt.engine.core.sso.api;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;

import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.api.extensions.ExtMap;
import org.ovirt.engine.core.sso.service.SsoService;

public class SsoSession implements Serializable, Cloneable {
    private static final long serialVersionUID = 6904401523209679500L;

    public enum Status {
        unauthenticated,
        inprogress,
        authenticated
    }

    private Status status = Status.unauthenticated;
    private String clientId;
    private String sourceAddr;
    private boolean active;
    private String appUrl;
    private String authorizationCode;
    private String accessToken;
    private String refreshToken;
    private long validTo;
    private String userId;
    private String scope;
    private List<String> scopeAsList;
    private String password;
    private String redirectUri;
    private String profile;
    private long tokenLastAccess;
    private HttpSession httpSession;
    private ExtMap authRecord;
    private ExtMap principalRecord;
    private String loginMessage;
    private String changePasswdMessage;
    private String state;
    private boolean reauthenticate;
    private Credentials tempCredentials;
    private Credentials changePasswdCredentials;
    private Credentials autheticatedCredentials;
    private Set<String> associateClientIds = new TreeSet<>();
    private Stack<InteractiveAuth> authStack;
    private String sessionIdToken;
    private String openIdDisplay;
    private boolean openIdScope;
    private boolean restApiScope;
    private String openIdNonce;
    private String openIdPrompt;
    private Date authTime;
    private boolean tokenIssued;

    public SsoSession() {
        this(null);
    }

    public SsoSession(HttpSession httpSession) {
        this.httpSession = httpSession;
        cleanup();
    }

    public String getAuthorizationCode() {
        return authorizationCode;
    }

    public void setAuthorizationCode(String authorizationCode) {
        this.authorizationCode = authorizationCode;
    }

    public boolean isTokenIssued() {
        return this.tokenIssued;
    }

    public void setTokenIssued(boolean tokenIssued) {
        this.tokenIssued = tokenIssued;
    }

    public long getTokenLastAccess() {
        return tokenLastAccess;
    }

    public void touch() {
        this.tokenLastAccess = System.nanoTime();
    }

    public HttpSession getHttpSession() {
        return httpSession;
    }

    public void setHttpSession(HttpSession httpSession) {
        this.httpSession = httpSession;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
        if (StringUtils.isNotEmpty(clientId)) {
            associateClientIds.add(clientId);
        }
    }

    public String getSourceAddr() {
        return sourceAddr;
    }

    public void setSourceAddr(String sourceAddr) {
        this.sourceAddr = sourceAddr;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getAppUrl() {
        return appUrl;
    }

    public void setAppUrl(String appUrl) {
        this.appUrl = appUrl;
    }

    public Long getValidTo() {
        return validTo;
    }

    public void setValidTo(long validTo) {
        this.validTo = validTo;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserIdWithProfile() {
        return String.format("%s@%s",
                Objects.toString(getUserId(), ""),
                Objects.toString(getProfile(), ""));
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        if (scopeAsList == null && !SsoService.strippedScopeAsList(SsoService.scopeAsList(scope)).isEmpty()) {
            this.scope = scope;
            this.scopeAsList = SsoService.scopeAsList(scope);
            restApiScope = SsoService.isRestApiScope(scopeAsList);
        }
    }

    public List<String> getScopeAsList() {
        return scopeAsList;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    public void setRedirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public Set<String> getAssociatedClientIds() {
        return associateClientIds;
    }

    public ExtMap getAuthRecord() {
        return authRecord;
    }

    public void setAuthRecord(ExtMap authRecord) {
        this.authRecord = authRecord;
    }

    public ExtMap getPrincipalRecord() {
        return principalRecord;
    }

    public void setPrincipalRecord(ExtMap principalRecord) {
        this.principalRecord = principalRecord;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
        if (Status.authenticated == status) {
            authTime = new Date(System.currentTimeMillis());
        } else {
            authTime = null;
        }
    }

    public Date getAuthTime() {
        return authTime;
    }

    public String getLoginMessage() {
        return loginMessage;
    }

    public void setLoginMessage(String loginMessage) {
        this.loginMessage = loginMessage;
    }

    public String getChangePasswdMessage() {
        return changePasswdMessage;
    }

    public void setChangePasswdMessage(String changePasswdMessage) {
        this.changePasswdMessage = changePasswdMessage;
    }

    public boolean isReauthenticate() {
        return reauthenticate;
    }

    public void setReauthenticate(boolean reauthenticate) {
        this.reauthenticate = reauthenticate;
    }

    public Credentials getTempCredentials() {
        return tempCredentials;
    }

    public void setTempCredentials(Credentials tempCredentials) {
        this.tempCredentials = tempCredentials;
    }

    public Credentials getChangePasswdCredentials() {
        return changePasswdCredentials;
    }

    public void setChangePasswdCredentials(Credentials changePasswdCredentials) {
        this.changePasswdCredentials = changePasswdCredentials;
    }

    public Credentials getAutheticatedCredentials() {
        return autheticatedCredentials;
    }

    public void setAutheticatedCredentials(Credentials autheticatedCredentials) {
        this.autheticatedCredentials = autheticatedCredentials;
    }

    public Stack<InteractiveAuth> getAuthStack() {
        return authStack;
    }

    public void setAuthStack(Stack<InteractiveAuth> authStack) {
        this.authStack = authStack;
    }

    public String getSessionIdToken() {
        return sessionIdToken;
    }

    public void setSessionIdToken(String sessionIdToken) {
        this.sessionIdToken = sessionIdToken;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getOpenIdDisplay() {
        return openIdDisplay;
    }

    public void setOpenIdDisplay(String openIdDisplay) {
        this.openIdDisplay = openIdDisplay;
    }

    public boolean isOpenIdScope() {
        return openIdScope;
    }

    public boolean isRestApiScope() {
        return restApiScope;
    }

    public void setOpenIdScope(boolean openIdScope) {
        this.openIdScope = openIdScope;
    }

    public String getOpenIdNonce() {
        return openIdNonce;
    }

    public void setOpenIdNonce(String openIdNonce) {
        this.openIdNonce = openIdNonce;
    }

    public String getOpenIdPrompt() {
        return openIdPrompt;
    }

    public void setOpenIdPrompt(String openIdPrompt) {
        this.openIdPrompt = openIdPrompt;
    }

    public void cleanup() {
        redirectUri = null;
        authStack = null;
        state = null;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        SsoSession ssoSession = (SsoSession) super.clone();
        ssoSession.setAuthorizationCode(null);
        ssoSession.setAccessToken(null);
        ssoSession.setTokenIssued(false);
        return ssoSession;
    }
}
