package org.ovirt.engine.core.common.action;

import java.util.Collection;
import java.util.Map;

public class CreateUserSessionParameters extends ActionParametersBase {

    private static final long serialVersionUID = 5238452182295928273L;
    private String ssoToken;
    private String ssoScope;
    private String appScope;
    private String profileName;
    private String principalName;
    private String principalId;
    private String email;
    private String firstName;
    private String lastName;
    private String namespace;
    private String sourceIp;
    private Collection<? extends Map> groupIds;
    private boolean adminRequired;

    public CreateUserSessionParameters() {
    }

    public CreateUserSessionParameters(String ssoToken,
                                       String ssoScope,
                                       String appScope,
                                       String profileName,
                                       String principalName,
                                       String principalId,
                                       String email,
                                       String firstName,
                                       String lastName,
                                       String namespace,
                                       String sourceIp,
                                       Collection<? extends Map> groupIds,
                                       boolean adminRequired) {
        setSsoToken(ssoToken);
        setSsoScope(ssoScope);
        setAppScope(appScope);
        setProfileName(profileName);
        setPrincipalName(principalName);
        setPrincipalId(principalId);
        setEmail(email);
        setFirstName(firstName);
        setLastName(lastName);
        setNamespace(namespace);
        setSourceIp(sourceIp);
        setGroupIds(groupIds);
        setAdminRequired(adminRequired);
    }

    public boolean isAdminRequired() {
        return adminRequired;
    }

    public void setAdminRequired(boolean adminRequired) {
        this.adminRequired = adminRequired;
    }

    public String getProfileName() {
        return profileName;
    }

    public void setProfileName(String profileName) {
        this.profileName = profileName;
    }

    public String getPrincipalName() {
        return principalName;
    }

    public void setPrincipalName(String principalName) {
        this.principalName = principalName;
    }

    public String getPrincipalId() {
        return principalId;
    }

    public void setPrincipalId(String principalId) {
        this.principalId = principalId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @ShouldNotBeLogged
    public Collection<? extends Map> getGroupIds() {
        return groupIds;
    }

    public void setGroupIds(Collection<? extends Map> groupIds) {
        this.groupIds = groupIds;
    }

    public String getSsoToken() {
        return ssoToken;
    }

    public void setSsoToken(String ssoToken) {
        this.ssoToken = ssoToken;
    }

    public String getSsoScope() {
        return ssoScope;
    }

    public void setSsoScope(String ssoScope) {
        this.ssoScope = ssoScope;
    }

    public String getSourceIp() {
        return sourceIp;
    }

    public void setSourceIp(String sourceIp) {
        this.sourceIp = sourceIp;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getAppScope() {
        return appScope;
    }

    public void setAppScope(String appScope) {
        this.appScope = appScope;
    }
}
