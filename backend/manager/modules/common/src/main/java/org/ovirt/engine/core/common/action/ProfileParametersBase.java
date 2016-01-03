package org.ovirt.engine.core.common.action;

import javax.validation.Valid;

import org.ovirt.engine.core.common.businessentities.profiles.ProfileBase;
import org.ovirt.engine.core.compat.Guid;

public abstract class ProfileParametersBase<T extends ProfileBase> extends VdcActionParametersBase {

    private static final long serialVersionUID = 1303387921254823324L;

    private boolean addPermissions;

    public ProfileParametersBase(boolean addPermissions) {
        this.addPermissions = addPermissions;
    }

    public ProfileParametersBase(T profile, Guid profileId, boolean addPermissions) {
        this.profile = profile;
        this.profileId = profileId;
        this.addPermissions = addPermissions;
    }

    public ProfileParametersBase(T profile, Guid profileId) {
        this.profile = profile;
        this.profileId = profileId;
    }

    @Valid
    private T profile;
    private Guid profileId;

    public ProfileParametersBase() {}

    public T getProfile() {
        return profile;
    }

    public void setProfile(T profile) {
        this.profile = profile;
    }

    public Guid getProfileId() {
        return profileId;
    }

    public void setProfileId(Guid profileId) {
        this.profileId = profileId;
    }

    public boolean isAddPermissions() {
        return addPermissions;
    }

    public void setAddPermissions(boolean addPermissions) {
        this.addPermissions = addPermissions;
    }

}
