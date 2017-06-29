package org.ovirt.engine.core.common.action;

import javax.validation.Valid;

import org.ovirt.engine.core.common.businessentities.UserProfile;
import org.ovirt.engine.core.compat.Guid;


public class UserProfileParameters extends ActionParametersBase {
    private static final long serialVersionUID = -4168302609852555079L;

    @Valid
    private UserProfile profile;

    public UserProfileParameters() {
        this(Guid.Empty, Guid.Empty, "");
    }

    public UserProfileParameters(Guid userId) {
        this(userId, Guid.Empty, "");
    }

    public UserProfileParameters(String sshPublicKey) {
        this(Guid.Empty, Guid.newGuid(), sshPublicKey);
    }

    public UserProfileParameters(Guid sshPublicKeyId, String sshPublicKey) {
        this(Guid.Empty, sshPublicKeyId, sshPublicKey);
    }

    public UserProfileParameters(Guid userId, Guid sshPublicKeyId, String sshPublicKey) {
        profile = new UserProfile();
        profile.setUserId(userId);
        profile.setSshPublicKeyId(sshPublicKeyId);
        profile.setSshPublicKey(sshPublicKey);
    }

    public UserProfile getUserProfile() {
        return profile;
    }

    public void setUserProfile(UserProfile value) {
        profile = value;
    }
}
