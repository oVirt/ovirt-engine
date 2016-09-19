package org.ovirt.engine.core.bll.gluster;

import java.util.Collections;
import java.util.Map;

import org.ovirt.engine.core.bll.LockMessagesMatchUtil;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeGeoRepSessionParameters;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterGeoRepSession;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;

public abstract class GeoRepSessionCommandBase<T extends GlusterVolumeGeoRepSessionParameters> extends GlusterVolumeCommandBase<T> {
    private GlusterGeoRepSession geoRepSession = null;

    public GeoRepSessionCommandBase(T params, CommandContext context) {
        super(params, context);
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__TYPE__GLUSTER_GEOREP_SESSION);
        addValidationMessageVariable("volumeName", getGlusterVolumeName());
        addValidationMessageVariable("cluster", getClusterName());
    }

    @Override
    protected boolean validate() {
        if (!super.validate()) {
            return false;
        }

        if (getGeoRepSession() == null) {
            addValidationMessage(EngineMessage.ACTION_TYPE_FAILED_GEOREP_SESSION_INVALID);
            return false;
        }

        GlusterVolumeEntity volume = getGlusterVolume();
        if (!volume.isOnline()) {
            addValidationMessage(EngineMessage.ACTION_TYPE_FAILED_GLUSTER_VOLUME_IS_DOWN);
            addValidationMessageVariable("volumeName", volume.getName());
            return false;
        }

        return true;
    }

    protected GlusterGeoRepSession getGeoRepSession() {
        if (geoRepSession == null) {
            if(getParameters().getGeoRepSessionId() != null) {
                geoRepSession = glusterGeoRepDao.getById(getParameters().getGeoRepSessionId());
            } else {
                geoRepSession =
                        glusterGeoRepDao.getGeoRepSession(getGlusterVolumeId(),
                                getParameters().getSlaveHostId(),
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
                            EngineMessage.ACTION_TYPE_FAILED_GEOREP_SESSION_LOCKED));
        }
        return null;
    }
}
