package org.ovirt.engine.core.common.action;

import java.util.Collection;
import java.util.Map;

public class CreateUserSessionParameters extends VdcActionParametersBase {

    private static final long serialVersionUID = 5238452182295928273L;
    private String ssoToken;
    private String profileName;
    private String principalName;
    private String principalId;
    private String email;
    private String sourceIp;
    private Collection<? extends Map> groupIds;
    private boolean adminRequired;

    public CreateUserSessionParameters() {
    }

    public CreateUserSessionParameters(String ssoToken,
                                       String profileName,
                                       String principalName,
                                       String principalId,
                                       String email,
                                       String sourceIp,
                                       Collection<? extends Map> groupIds,
                                       boolean adminRequired) {
        setSsoToken(ssoToken);
        setProfileName(profileName);
        setPrincipalName(principalName);
        setPrincipalId(principalId);
        setEmail(email);
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

    public String getSourceIp() {
        return sourceIp;
    }

    public void setSourceIp(String sourceIp) {
        this.sourceIp = sourceIp;
    }
}
