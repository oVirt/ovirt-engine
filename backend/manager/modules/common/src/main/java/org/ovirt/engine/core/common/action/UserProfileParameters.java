package org.ovirt.engine.core.common.action;

import javax.validation.Valid;

import org.ovirt.engine.core.common.businessentities.UserProfile;
import org.ovirt.engine.core.compat.Guid;


public class UserProfileParameters extends VdcActionParametersBase {
    private static final long serialVersionUID = -4168302609852555079L;

    @Valid
    private UserProfile profile;

    public UserProfileParameters() {
        this(Guid.Empty, "");
    }

    public UserProfileParameters(Guid userId) {
        this(userId, "", true);
    }

    public UserProfileParameters(String sshPublicKey) {
        this(Guid.Empty, sshPublicKey, true);
    }

    public UserProfileParameters(Guid userId, String sshPublicKey) {
        this(Guid.Empty, sshPublicKey, true);
    }

    public UserProfileParameters(Guid userId, String sshPublicKey, boolean userPortalVmLoginAutomatically) {
        profile = new UserProfile();
        profile.setUserId(userId);
        profile.setSshPublicKey(sshPublicKey);
        profile.setUserPortalVmLoginAutomatically(userPortalVmLoginAutomatically);
    }

    public UserProfile getUserProfile() {
        return profile;
    }

    public void setUserProfile(UserProfile value) {
        profile = value;
    }
}
