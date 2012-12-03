package org.ovirt.engine.core.bll.gluster;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeParameters;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterStatus;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.gluster.GlusterBrickDao;
import org.ovirt.engine.core.dao.gluster.GlusterOptionDao;

/**
 * Base class for all Gluster Volume related commands
 */
public abstract class GlusterVolumeCommandBase<T extends GlusterVolumeParameters> extends GlusterCommandBase<T> {
    private static final long serialVersionUID = -7394070330293300587L;

    public GlusterVolumeCommandBase(T params) {
        super(params);
        setGlusterVolumeId(getParameters().getVolumeId());
    }

    protected GlusterBrickDao getGlusterBrickDao() {
        return DbFacade.getInstance().getGlusterBrickDao();
    }

    protected GlusterOptionDao getGlusterOptionDao() {
        return DbFacade.getInstance().getGlusterOptionDao();
    }

    @Override
    protected VDSGroup getVdsGroup() {
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
    protected Map<String, String> getExclusiveLocks() {
        return Collections.singletonMap(getGlusterVolume().getClusterId().toString(), LockingGroup.GLUSTER.name());
    }

    protected void updateBrickStatus(GlusterStatus status) {
        for(GlusterBrickEntity brick : getGlusterVolume().getBricks()) {
            getGlusterBrickDao().updateBrickStatus(brick.getId(), status);
        }
    }
}
