package org.ovirt.engine.core.bll.gluster;

import java.util.Collections;
import java.util.Map;

import org.ovirt.engine.core.bll.LockMessagesMatchUtil;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeParameters;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.gluster.GlusterVolumeSnapshotDao;
import org.ovirt.engine.core.utils.lock.EngineLock;
import org.ovirt.engine.core.utils.lock.LockManagerFactory;

public abstract class GlusterSnapshotCommandBase<T extends GlusterVolumeParameters> extends GlusterVolumeCommandBase<T> {

    public GlusterSnapshotCommandBase(T params, CommandContext commandContext) {
        super(params, commandContext);
        setGlusterVolumeId(params.getVolumeId());
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(Scope.Execution).withWait(true);
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__TYPE__GLUSTER_VOLUME_SNAPSHOT);
        addValidationMessageVariable("volumeName", getGlusterVolumeName());
        addValidationMessageVariable("cluster", getClusterName());
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        if (!isInternalExecution()) {
            return Collections.singletonMap(getGlusterVolumeId().toString(),
                    LockMessagesMatchUtil.makeLockingPair(LockingGroup.GLUSTER_SNAPSHOT,
                            EngineMessage.ACTION_TYPE_FAILED_VOLUME_SNAPSHOT_LOCKED));
        }
        return null;
    }

    protected GlusterVolumeSnapshotDao getGlusterVolumeSnapshotDao() {
        return getDbFacade().getGlusterVolumeSnapshotDao();
    }

    protected EngineLock acquireEngineLock(Guid id, LockingGroup group) {
        EngineLock lock = new EngineLock(Collections.singletonMap(id.toString(),
                LockMessagesMatchUtil.makeLockingPair(group,
                        EngineMessage.ACTION_TYPE_FAILED_VOLUME_OPERATION_IN_PROGRESS)), null);
        LockManagerFactory.getLockManager().acquireLockWait(lock);
        return lock;
    }
}
