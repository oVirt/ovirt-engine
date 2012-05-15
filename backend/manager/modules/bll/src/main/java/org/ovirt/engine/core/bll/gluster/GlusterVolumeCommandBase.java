package org.ovirt.engine.core.bll.gluster;

import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.common.PermissionSubject;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeParameters;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.gluster.GlusterBrickDao;
import org.ovirt.engine.core.dao.gluster.GlusterOptionDao;
import org.ovirt.engine.core.dao.gluster.GlusterVolumeDao;

/**
 * Base class for all Gluster Volume related commands
 */
public abstract class GlusterVolumeCommandBase<T extends GlusterVolumeParameters> extends GlusterCommandBase<T> {
    private static final long serialVersionUID = -7394070330293300587L;

    public GlusterVolumeCommandBase(T params) {
        super(params);
        setGlusterVolumeId(getParameters().getVolumeId());
        if(getGlusterVolume() != null) {
            setVdsGroupId(getGlusterVolume().getClusterId());
        }
    }

    protected GlusterVolumeDao getGlusterVolumeDao() {
        return DbFacade.getInstance().getGlusterVolumeDao();
    }

    protected GlusterBrickDao getGlusterBrickDao() {
        return DbFacade.getInstance().getGlusterBrickDao();
    }

    protected GlusterOptionDao getGlusterOptionDao() {
        return DbFacade.getInstance().getGlusterOptionDao();
    }

    @Override
    protected boolean canDoAction() {
        if (!super.canDoAction()) {
            return false;
        }

        if (getGlusterVolume() == null) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_GLUSTER_VOLUME_INVALID);
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
}
