package org.ovirt.engine.core.bll.gluster;

import java.util.Collections;
import java.util.Map;

import org.ovirt.engine.core.bll.LockMessagesMatchUtil;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeParameters;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.gluster.GlusterFeatureSupported;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.dao.gluster.GlusterVolumeSnapshotDao;

public abstract class GlusterSnapshotCommandBase<T extends GlusterVolumeParameters> extends GlusterVolumeCommandBase<T> {
    public GlusterSnapshotCommandBase(T params) {
        super(params);
        setGlusterVolumeId(params.getVolumeId());
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(Scope.Execution).withWait(false);
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__GLUSTER_VOLUME_SNAPSHOT);
        addCanDoActionMessageVariable("volumeName", getGlusterVolumeName());
        addCanDoActionMessageVariable("vdsGroup", getVdsGroupName());
    }

    @Override
    protected boolean canDoAction() {
        if (!super.canDoAction()) {
            return false;
        }

        if (!GlusterFeatureSupported.glusterSnapshot(getVdsGroup().getcompatibility_version())) {
            failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_VOLUME_SNAPSHOT_NOT_SUPPORTED);
        }

        return true;
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        if (!isInternalExecution()) {
            return Collections.singletonMap(getGlusterVolumeId().toString(),
                    LockMessagesMatchUtil.makeLockingPair(LockingGroup.GLUSTER_SNAPSHOT,
                            VdcBllMessages.ACTION_TYPE_FAILED_VOLUME_SNAPSHOT_LOCKED));
        }
        return null;
    }

    protected GlusterVolumeSnapshotDao getGlusterVolumeSnapshotDao() {
        return getDbFacade().getGlusterVolumeSnapshotDao();
    }
}
