package org.ovirt.engine.core.bll.gluster;

import java.util.List;

import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.utils.ClusterUtils;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeSnapshotActionParameters;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterGeoRepSession;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.gluster.GlusterVolumeSnapshotActionVDSParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.lock.EngineLock;

@NonTransactiveCommandAttribute
public class DeleteGlusterVolumeSnapshotCommand extends GlusterVolumeSnapshotCommandBase<GlusterVolumeSnapshotActionParameters> {
    private List<GlusterGeoRepSession> georepSessions;

    public DeleteGlusterVolumeSnapshotCommand(GlusterVolumeSnapshotActionParameters params) {
        super(params);
        georepSessions = getDbFacade().getGlusterGeoRepDao().getGeoRepSessions(getGlusterVolumeId());
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__REMOVE);
    }

    private boolean deleteGlusterVolumeSnapshot(Guid serverId, String volumeName, String snapshotName) {
        VDSReturnValue retVal =
                runVdsCommand(VDSCommandType.DeleteGlusterVolumeSnapshot,
                        new GlusterVolumeSnapshotActionVDSParameters(serverId,
                                volumeName,
                                snapshotName));
        setSucceeded(retVal.getSucceeded());

        if (!getSucceeded()) {
            handleVdsError(AuditLogType.GLUSTER_VOLUME_SNAPSHOT_DELETE_FAILED, retVal.getVdsError().getMessage());
            return false;
        } else {
            getGlusterVolumeSnapshotDao().remove(getSnapshot().getId());
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

                VDS slaveUpServer = ClusterUtils.getInstance().getRandomUpServer(slaveVolume.getClusterId());
                if (slaveUpServer == null) {
                    handleVdsError(AuditLogType.GLUSTER_VOLUME_SNAPSHOT_DELETE_FAILED,
                            VdcBllErrors.NoUpServerFoundInRemoteCluster.name());
                    setSucceeded(false);
                    return;
                }

                try (EngineLock lock = acquireEngineLock(session.getSlaveVolumeId(), LockingGroup.GLUSTER_SNAPSHOT)) {
                    if (!deleteGlusterVolumeSnapshot(slaveUpServer.getId(),
                            slaveVolume.getName(),
                            getSnapshot().getSnapshotName())) {
                        return;
                    }
                }
            }
        }

        deleteGlusterVolumeSnapshot(getUpServer().getId(), getGlusterVolumeName(), getSnapshot().getSnapshotName());
    }

    @Override
    protected boolean canDoAction() {
        if (!super.canDoAction()) {
            return false;
        }

        for (GlusterGeoRepSession session : georepSessions) {
            if (session.getSlaveVolumeId() == null || session.getSlaveNodeUuid() == null) {
                return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_REMOTE_CLUSTER_NOT_MAINTAINED_BY_ENGINE);
            }
        }

        return true;
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        if (getSucceeded()) {
            return AuditLogType.GLUSTER_VOLUME_SNAPSHOT_DELETED;
        } else {
            return AuditLogType.GLUSTER_VOLUME_SNAPSHOT_DELETE_FAILED;
        }
    }
}
