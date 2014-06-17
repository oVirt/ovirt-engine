package org.ovirt.engine.core.bll.gluster;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeParameters;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.constants.gluster.GlusterConstants;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.dao.gluster.GlusterOptionDao;

/**
 * Base class for all Gluster Volume related commands
 */
public abstract class GlusterVolumeCommandBase<T extends GlusterVolumeParameters> extends GlusterCommandBase<T> {

    public GlusterVolumeCommandBase(T params) {
        this(params, null);
    }

    public GlusterVolumeCommandBase(T params, CommandContext commandContext) {
        super(params, commandContext);
        setGlusterVolumeId(getParameters().getVolumeId());

    }

    protected GlusterOptionDao getGlusterOptionDao() {
        return getDbFacade().getGlusterOptionDao();
    }

    @Override
    public VDSGroup getVdsGroup() {
        if (getGlusterVolume() != null) {
            setVdsGroupId(getGlusterVolume().getClusterId());
        }
        return super.getVdsGroup();
    }

    @Override
    protected boolean canDoAction() {
        if (getGlusterVolume() == null) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_GLUSTER_VOLUME_INVALID);
            return false;
        }
        // super class canDoAction expects cluster id (VdsGroupId).
        if (!super.canDoAction()) {
            return false;
        }

        return true;
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        // Any command that inherits from this class will check
        // permissions at volume level by default
        return Collections.singletonList(
                new PermissionSubject(
                        getParameters().getVolumeId(),
                        VdcObjectType.GlusterVolume,
                        getActionType().getActionGroup()));
    }

    @Override
    public Map<String, String> getJobMessageProperties() {
        if (jobProperties == null) {
            jobProperties = super.getJobMessageProperties();
            jobProperties.put(GlusterConstants.VOLUME, getGlusterVolumeName());
        }

        return jobProperties;
    }
}
