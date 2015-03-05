package org.ovirt.engine.core.bll.gluster;

import java.util.Collections;
import java.util.Map;

import org.ovirt.engine.core.bll.LockMessagesMatchUtil;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeGeoRepSessionParameters;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterGeoRepSession;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.dao.gluster.GlusterGeoRepDao;

public abstract class GeoRepSessionCommandBase<T extends GlusterVolumeGeoRepSessionParameters> extends GlusterVolumeCommandBase<T> {
    private GlusterGeoRepSession geoRepSession = null;

    public GeoRepSessionCommandBase(T params) {
        super(params);
    }

    public GeoRepSessionCommandBase(T params, CommandContext context) {
        super(params, context);
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__GLUSTER_GEOREP_SESSION);
        addCanDoActionMessageVariable("volumeName", getGlusterVolumeName());
        addCanDoActionMessageVariable("vdsGroup", getVdsGroupName());
    }

    @Override
    protected boolean canDoAction() {
        if (!super.canDoAction()) {
            return false;
        }

        if (getGeoRepSession() == null) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_GEOREP_SESSION_INVALID);
            return false;
        }

        GlusterVolumeEntity volume = getGlusterVolume();
        if (!volume.isOnline()) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_GLUSTER_VOLUME_IS_DOWN);
            addCanDoActionMessageVariable("volumeName", volume.getName());
            return false;
        }

        return true;
    }

    protected GlusterGeoRepSession getGeoRepSession() {
        if (geoRepSession == null) {
            if(getParameters().getGeoRepSessionId() != null) {
                geoRepSession = getGlusterGeoRepDao().getById(getParameters().getGeoRepSessionId());
            } else {
                geoRepSession =
                        getGlusterGeoRepDao().getGeoRepSession(getGlusterVolumeId(),
                                getParameters().getSlaveHost(),
                                getParameters().getSlaveVolumeName());
            }
        }
        return geoRepSession;
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        if (!isInternalExecution()) {
            return Collections.singletonMap(getGeoRepSession().getId().toString(),
                    LockMessagesMatchUtil.makeLockingPair(LockingGroup.GLUSTER_GEOREP,
                            VdcBllMessages.ACTION_TYPE_FAILED_GEOREP_SESSION_LOCKED));
        }
        return null;
    }

    protected GlusterGeoRepDao getGlusterGeoRepDao() {
        return getDbFacade().getGlusterGeoRepDao();
    }

}
