package org.ovirt.engine.extensions.aaa.builtin.kerberosldap;

import org.ovirt.engine.api.extensions.ExtMap;

public class LdapBrokerBaseParameters {
    private String privateDomain;
    private String authenticationDomain;
    private String sessionId;
    private String privatePassword;
    private ExtMap inputMap;
    private ExtMap outputMap;

    public LdapBrokerBaseParameters(String domain) {
        setDomain(domain);
    }

    /**
     * Create parameters with sessionId and domains
     *
     * @param sessionId
     *            - id of current working session
     * @param domain
     *            - domain name for search
     */
    public LdapBrokerBaseParameters(String sessionId, String domain) {
        setSessionId(sessionId);
        setDomain(domain);
    }

    public String getDomain() {
        return privateDomain;
    }

    public ExtMap getInputMap() {
        return inputMap;
    }

    public void setInputMap(ExtMap inputMap) {
        this.inputMap = inputMap;
    }

    public ExtMap getOutputMap() {
        return outputMap;
    }

    public void setOutputMap(ExtMap outputMap) {
        this.outputMap = outputMap;
    }

    public void setDomain(String value) {
        privateDomain = value;
    }

    private String privateLoginName;

    public String getLoginName() {
        return privateLoginName;
    }

    public void setLoginName(String value) {
        privateLoginName = value;
    }

    public String getPassword() {
        return privatePassword;
    }

    public void setPassword(String value) {
        privatePassword = value;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getAuthenticationDomain() {
        return authenticationDomain;
    }

    public void setAuthenticationDomain(String value) {
        authenticationDomain = value;
    }


}
