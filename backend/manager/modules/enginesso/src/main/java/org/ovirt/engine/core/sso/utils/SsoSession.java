package org.ovirt.engine.core.sso.utils;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;

import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.api.extensions.ExtMap;

public class SsoSession implements Serializable {
    private static final long serialVersionUID = 6904401523209679500L;

    public enum Status { unauthenticated, inprogress, authenticated}
    private Status status = Status.unauthenticated;
    private String clientId;
    private boolean active;
    private String authorizationCode;
    private String accessToken;
    private int validTo;
    private String userId;
    private String scope;
    private List<String> scopeAsList;
    private String state;
    private String password;
    private String redirectUri;
    private String profile;
    private long tokenLastAccess;
    private HttpSession httpSession;
    private ExtMap authRecord;
    private ExtMap principalRecord;
    private String loginMessage;
    private String changePasswdMessage;
    private boolean reauthenticate;
    private Credentials tempCredentials;
    private Credentials changePasswdCredentials;
    private Credentials autheticatedCredentials;
    private Set<String> associateClientIds = new TreeSet<>();
    private Stack<InteractiveAuth> authStack;

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

    public long getTokenLastAccess() {
        return tokenLastAccess;
    }

    public void touch() {
        this.tokenLastAccess = System.nanoTime();
    }

    public HttpSession getHttpSession() {
        return httpSession;
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

    public int getValidTo() {
        return validTo;
    }

    public void setValidTo(int validTo) {
        this.validTo = validTo;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        if (scopeAsList == null  && !SsoUtils.strippedScopeAsList(SsoUtils.scopeAsList(scope)).isEmpty()) {
            this.scope = scope;
            this.scopeAsList = StringUtils.isEmpty(scope) ? Collections.<String>emptyList() : SsoUtils.scopeAsList(scope);
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

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
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

    public void cleanup() {
        redirectUri = null;
        authStack = null;
    }
}
