package org.ovirt.engine.core.bll.gluster;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeParameters;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterGeoRepSession;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeSnapshotEntity;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.gluster.GlusterVolumeVDSParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.lock.EngineLock;

@NonTransactiveCommandAttribute
public class DeleteAllGlusterVolumeSnapshotsCommand extends GlusterSnapshotCommandBase<GlusterVolumeParameters> {
    private List<GlusterVolumeSnapshotEntity> snapshots;
    private List<GlusterGeoRepSession> georepSessions;

    public DeleteAllGlusterVolumeSnapshotsCommand(GlusterVolumeParameters params, CommandContext commandContext) {
        super(params, commandContext);
        snapshots = getGlusterVolumeSnapshotDao().getAllByVolumeId(getGlusterVolumeId());
        georepSessions = getDbFacade().getGlusterGeoRepDao().getGeoRepSessions(getGlusterVolumeId());
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__ACTION__REMOVE);
        super.setActionMessageParameters();
    }

    private boolean deleteAllGlusterVolumeSnapshots(Guid serverId,
            String volumeName,
            List<GlusterVolumeSnapshotEntity> snapshotsList) {
        VDSReturnValue retVal =
                runVdsCommand(VDSCommandType.DeleteAllGlusterVolumeSnapshots,
                        new GlusterVolumeVDSParameters(serverId, volumeName));
        setSucceeded(retVal.getSucceeded());

        if (!getSucceeded()) {
            handleVdsError(AuditLogType.GLUSTER_VOLUME_ALL_SNAPSHOTS_DELETE_FAILED, retVal.getVdsError()
                    .getMessage());
        } else {
            List<Guid> guids = new ArrayList<>();
            for (GlusterVolumeSnapshotEntity snapshot : snapshotsList) {
                guids.add(snapshot.getId());
            }
            getGlusterVolumeSnapshotDao().removeAll(guids);
        }

        return true;
    }

    @Override
    public void executeCommand() {
        if (georepSessions != null) {
            for (GlusterGeoRepSession session : georepSessions) {
                GlusterVolumeEntity slaveVolume =
                        getDbFacade().getGlusterVolumeDao().getById(session.getSlaveVolumeId());
                if (slaveVolume == null) {
                    // continue with other sessions and try to pause
                    continue;
                }

                VDS slaveUpServer = getGlusterUtils().getRandomUpServer(slaveVolume.getClusterId());
                if (slaveUpServer == null) {
                    handleVdsError(AuditLogType.GLUSTER_VOLUME_ALL_SNAPSHOTS_DELETE_FAILED,
                            EngineError.NoUpServerFoundInRemoteCluster.name());
                    setSucceeded(false);
                    return;
                }

                List<GlusterVolumeSnapshotEntity> slaveVolumeSnapshots =
                        getGlusterVolumeSnapshotDao().getAllByVolumeId(slaveVolume.getId());

                try (EngineLock lock = acquireEngineLock(session.getSlaveVolumeId(), LockingGroup.GLUSTER_SNAPSHOT)) {
                    if (!deleteAllGlusterVolumeSnapshots(slaveUpServer.getId(),
                            slaveVolume.getName(),
                            slaveVolumeSnapshots)) {
                        return;
                    }
                    // Check and remove soft limit alert for the volume
                    getGlusterUtils().checkAndRemoveVolumeSnapshotLimitsAlert(slaveVolume);
                }
            }
        }

        deleteAllGlusterVolumeSnapshots(getUpServer().getId(), getGlusterVolumeName(), snapshots);
        // Check and remove soft limit alert for the volume
        getGlusterUtils().checkAndRemoveVolumeSnapshotLimitsAlert(getGlusterVolume());
    }

    @Override
    protected boolean validate() {
        if (!super.validate()) {
            return false;
        }

        for (GlusterGeoRepSession session : georepSessions) {
            if (session.getSlaveVolumeId() == null || session.getSlaveNodeUuid() == null) {
                return failValidation(EngineMessage.ACTION_TYPE_FAILED_REMOTE_CLUSTER_NOT_MAINTAINED_BY_ENGINE);
            }
        }

        if (snapshots == null || snapshots.isEmpty()) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_GLUSTER_VOLUME_NO_SNAPSHOTS_EXIST,
                    getGlusterVolumeName());
        }

        return true;
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        if (getSucceeded()) {
            return AuditLogType.GLUSTER_VOLUME_ALL_SNAPSHOTS_DELETED;
        } else {
            return AuditLogType.GLUSTER_VOLUME_ALL_SNAPSHOTS_DELETE_FAILED;
        }
    }
}
