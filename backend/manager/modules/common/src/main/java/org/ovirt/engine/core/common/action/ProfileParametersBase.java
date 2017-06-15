package org.ovirt.engine.core.common.action;

import javax.validation.Valid;

import org.ovirt.engine.core.common.businessentities.profiles.ProfileBase;
import org.ovirt.engine.core.compat.Guid;

public abstract class ProfileParametersBase<T extends ProfileBase> extends ActionParametersBase {

    private static final long serialVersionUID = 1303387921254823324L;

    private boolean addPermissions;

    public ProfileParametersBase(boolean addPermissions) {
        this.addPermissions = addPermissions;
    }

    public ProfileParametersBase(T profile, boolean addPermissions) {
        this.profile = profile;
        this.addPermissions = addPermissions;
    }

    public ProfileParametersBase(T profile) {
        this.profile = profile;
    }

    @Valid
    private T profile;

    public ProfileParametersBase() {}

    public T getProfile() {
        return profile;
    }

    public void setProfile(T profile) {
        this.profile = profile;
    }

    public Guid getProfileId() {
        return profile.getId();
    }

    public void setProfileId(Guid profileId) {
        this.profile.setId(profileId);
    }

    public boolean isAddPermissions() {
        return addPermissions;
    }

    public void setAddPermissions(boolean addPermissions) {
        this.addPermissions = addPermissions;
    }

}
