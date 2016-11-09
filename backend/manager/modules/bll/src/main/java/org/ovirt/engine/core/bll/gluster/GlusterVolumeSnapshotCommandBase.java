package org.ovirt.engine.core.bll.gluster;

import java.util.Collections;
import java.util.Map;

import org.ovirt.engine.core.bll.LockMessagesMatchUtil;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeSnapshotActionParameters;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeSnapshotEntity;
import org.ovirt.engine.core.common.constants.gluster.GlusterConstants;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.lock.EngineLock;

public abstract class GlusterVolumeSnapshotCommandBase<T extends GlusterVolumeSnapshotActionParameters> extends GlusterSnapshotCommandBase<T> {
    private GlusterVolumeSnapshotEntity snapshot;

    public GlusterVolumeSnapshotCommandBase(T params, CommandContext commandContext) {
        super(params, commandContext);
    }

    @Override
    protected void init() {
        super.init();
        snapshot = glusterVolumeSnapshotDao.getByName(getGlusterVolumeId(), getParameters().getSnapshotName());
    }

    @Override
    public Map<String, String> getCustomValues() {
        addCustomValue(GlusterConstants.VOLUME_SNAPSHOT_NAME, getParameters().getSnapshotName());
        return super.getCustomValues();
    }

    @Override
    protected boolean validate() {
        if (!super.validate()) {
            return false;
        }

        if (getSnapshot() == null) {
            failValidation(EngineMessage.ACTION_TYPE_FAILED_GLUSTER_VOLUME_SNAPSHOT_DOES_NOT_EXIST,
                    getParameters().getSnapshotName());
        }

        return true;
    }

    protected GlusterVolumeSnapshotEntity getSnapshot() {
        return this.snapshot;
    }

    protected EngineLock acquireGeoRepSessionLock(Guid id) {
        EngineLock lock = new EngineLock(Collections.singletonMap(id.toString(),
                LockMessagesMatchUtil.makeLockingPair(LockingGroup.GLUSTER_GEOREP,
                        EngineMessage.ACTION_TYPE_FAILED_GEOREP_SESSION_LOCKED)), null);
        lockManager.acquireLockWait(lock);
        return lock;
    }

    protected EngineLock acquireVolumeSnapshotLock(Guid id) {
        EngineLock lock = new EngineLock(Collections.singletonMap(id.toString(),
                LockMessagesMatchUtil.makeLockingPair(LockingGroup.GLUSTER_SNAPSHOT,
                        EngineMessage.ACTION_TYPE_FAILED_VOLUME_SNAPSHOT_LOCKED)), null);
        lockManager.acquireLockWait(lock);
        return lock;
    }
}
