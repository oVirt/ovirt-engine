package org.ovirt.engine.core.bll.profiles;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.common.action.DiskProfileParameters;
import org.ovirt.engine.core.common.businessentities.profiles.DiskProfile;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.profiles.ProfilesDao;

public abstract class DiskProfileCommandBase extends CommandBase<DiskProfileParameters> {
    private DiskProfile profile;
    private Guid profileId;

    public DiskProfileCommandBase(DiskProfileParameters parameters) {
        super(parameters);
    }

    public DiskProfile getProfile() {
        if (profile == null) {
            if (getParameters().getProfile() != null) {
                profile = getParameters().getProfile();
            } else if (getParameters().getProfileId() != null) {
                profile = getProfileDao().get(getParameters().getProfileId());
            }
        }
        return profile;
    }

    public Guid getProfileId() {
        if (profileId == null) {
            if (getParameters().getProfileId() != null) {
                profileId = getParameters().getProfileId();
            } else if (getParameters().getProfile() != null) {
                profileId = getParameters().getProfile().getId();
            }
        }
        return profileId;
    }

    protected ProfilesDao<DiskProfile> getProfileDao() {
        return getDiskProfileDao();
    }
}
