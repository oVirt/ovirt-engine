package org.ovirt.engine.core.bll.profiles;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.action.ProfileParametersBase;
import org.ovirt.engine.core.common.businessentities.profiles.ProfileBase;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.profiles.ProfilesDao;

public abstract class ProfileCommandBase<T extends ProfileParametersBase<P>, P extends ProfileBase> extends CommandBase<T> {
    private P profile;
    private Guid profileId;

    public ProfileCommandBase(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    public P getProfile() {
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

    protected abstract ProfilesDao<P> getProfileDao();

    /**
     * used for auditLog's ${ProfileName} placeholder
     */
    public String getProfileName() {
        return getProfile().getName();
    }
}
