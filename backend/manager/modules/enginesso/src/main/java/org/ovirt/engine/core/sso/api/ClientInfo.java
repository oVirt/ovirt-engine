package org.ovirt.engine.core.sso.api;

import java.util.List;

public class ClientInfo {
    private String clientId;
    private String clientSecret;
    private String certificateLocation;
    private String callbackPrefix;
    private String clientNotificationCallback;
    private boolean encryptedUserInfo;
    private List<String> scope;
    private boolean trusted;
    private String notificationCallbackProtocol;
    private boolean notificationCallbackVerifyHost;
    private boolean notificationCallbackVerifyChain;

    public String getClientId() {
        return clientId;
    }

    public ClientInfo withClientId(String clientId) {
        this.clientId = clientId;
        return this;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public ClientInfo withClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
        return this;
    }

    public ClientInfo withCallbackPrefix(String callbackPrefix) {
        this.callbackPrefix = callbackPrefix;
        return this;
    }

    public String getCallbackPrefix() {
        return callbackPrefix;
    }

    public ClientInfo withCertificateLocation(String certificateLocation) {
        this.certificateLocation = certificateLocation;
        return this;
    }

    public String getCertificateLocation() {
        return certificateLocation;
    }

    public ClientInfo withClientNotificationCallback(String clientNotificationCallback) {
        this.clientNotificationCallback = clientNotificationCallback;
        return this;
    }

    public String getClientNotificationCallback() {
        return clientNotificationCallback;
    }

    public ClientInfo withScope(List<String> scope) {
        this.scope = scope;
        return this;
    }

    public List<String> getScope() {
        return scope;
    }

    public ClientInfo withIsTrusted(boolean trusted) {
        this.trusted = trusted;
        return this;
    }

    public boolean isTrusted() {
        return trusted;
    }

    public ClientInfo withNotificationCallbackProtocol(String notificationCallbackProtocol) {
        this.notificationCallbackProtocol = notificationCallbackProtocol;
        return this;
    }

    public String getNotificationCallbackProtocol() {
        return notificationCallbackProtocol;
    }

    public ClientInfo withNotificationCallbackVerifyHost(boolean notificationCallbackVerifyHost) {
        this.notificationCallbackVerifyHost = notificationCallbackVerifyHost;
        return this;
    }

    public boolean isNotificationCallbackVerifyHost() {
        return notificationCallbackVerifyHost;
    }

    public ClientInfo withNotificationCallVerifyChain(boolean notificationCallbackVerifyChain) {
        this.notificationCallbackVerifyChain = notificationCallbackVerifyChain;
        return this;
    }

    public boolean isNotificationCallbackVerifyChain() {
        return notificationCallbackVerifyChain;
    }

    public boolean isEncryptedUserInfo() {
        return encryptedUserInfo;
    }

    public ClientInfo withEncryptedUserInfo(boolean encryptedUserInfo) {
        this.encryptedUserInfo = encryptedUserInfo;
        return this;
    }
}
