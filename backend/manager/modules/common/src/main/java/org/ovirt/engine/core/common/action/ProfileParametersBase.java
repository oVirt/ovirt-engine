package org.ovirt.engine.core.common.action;

import javax.validation.Valid;

import org.ovirt.engine.core.common.businessentities.profiles.ProfileBase;
import org.ovirt.engine.core.compat.Guid;

public class ProfileParametersBase<T extends ProfileBase> extends VdcActionParametersBase {

    private static final long serialVersionUID = 1303387921254823324L;

    public ProfileParametersBase() {

    }

    public ProfileParametersBase(T profile, Guid profileId) {
        this.profile = profile;
        this.profileId = profileId;
    }

    @Valid
    private T profile;
    private Guid profileId;

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

}
