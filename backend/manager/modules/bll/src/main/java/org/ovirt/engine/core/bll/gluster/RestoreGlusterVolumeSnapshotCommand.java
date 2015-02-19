package org.ovirt.engine.core.bll.gluster;

import java.util.List;

import org.ovirt.engine.core.bll.utils.ClusterUtils;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeActionParameters;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeSnapshotActionParameters;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterGeoRepSession;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeSnapshotEntity;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.gluster.GlusterVolumeSnapshotActionVDSParameters;

public class RestoreGlusterVolumeSnapshotCommand extends GlusterVolumeSnapshotCommandBase<GlusterVolumeSnapshotActionParameters> {
    private List<GlusterGeoRepSession> georepSessions;

    public RestoreGlusterVolumeSnapshotCommand(GlusterVolumeSnapshotActionParameters params) {
        super(params);
        georepSessions = getDbFacade().getGlusterGeoRepDao().getGeoRepSessions(getGlusterVolumeId());
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__RESTORE);
    }

    @Override
    public void executeCommand() {
        for (GlusterGeoRepSession session : georepSessions) {
            GlusterVolumeEntity slaveVolume = getDbFacade().getGlusterVolumeDao().getById(session.getSlaveVolumeId());
            if (slaveVolume == null) {
                // continue with other sessions and restore the volumes
                continue;
            }

            VDS slaveUpServer = ClusterUtils.getInstance().getRandomUpServer(slaveVolume.getClusterId());
            if (slaveUpServer == null) {
                handleVdsError(AuditLogType.GLUSTER_VOLUME_SNAPSHOT_RESTORE_FAILED,
                        "No up server found in slave cluster of geo-rep session");
                setSucceeded(false);
                return;
            }

            // Bring down the remote volume and restore snapshot
            if (slaveVolume.getStatus() == GlusterStatus.UP) {
                VdcReturnValueBase volumeDownRetVal =
                        runInternalAction(VdcActionType.StopGlusterVolume,
                                new GlusterVolumeActionParameters(slaveVolume.getId(), true));
                if (!volumeDownRetVal.getSucceeded()) {
                    handleVdsError(AuditLogType.GLUSTER_VOLUME_STOP_FAILED, slaveVolume.getName());
                    setSucceeded(false);
                    return;
                } else {
                    GlusterVolumeSnapshotEntity slaveVolumeSnapshot =
                            getGlusterVolumeSnapshotDao().getByName(slaveVolume.getId(),
                                    getParameters().getSnapshotName());
                    if (slaveVolumeSnapshot == null) {
                        handleVdsError(AuditLogType.GLUSTER_VOLUME_SNAPSHOT_RESTORE_FAILED,
                                "Unable to find snapshot for slave volume");
                        setSucceeded(false);
                        return;
                    }

                    VDSReturnValue slaveRestoreRetVal =
                            runVdsCommand(VDSCommandType.RestoreGlusterVolumeSnapshot,
                                    new GlusterVolumeSnapshotActionVDSParameters(slaveUpServer.getId(),
                                            slaveVolume.getName(),
                                            slaveVolumeSnapshot.getSnapshotName()));
                    if (!slaveRestoreRetVal.getSucceeded()) {
                        handleVdsError(AuditLogType.GLUSTER_VOLUME_SNAPSHOT_RESTORE_FAILED,
                                slaveRestoreRetVal.getVdsError().getMessage());
                        setSucceeded(false);
                        return;
                    } else {
                        getGlusterVolumeSnapshotDao().remove(slaveVolumeSnapshot.getId());
                    }
                }
            }
        }

        VDSReturnValue retVal =
                runVdsCommand(VDSCommandType.RestoreGlusterVolumeSnapshot,
                        new GlusterVolumeSnapshotActionVDSParameters(getUpServer().getId(),
                                getGlusterVolumeName(),
                                getParameters().getSnapshotName()));
        setSucceeded(retVal.getSucceeded());

        if (!getSucceeded()) {
            handleVdsError(AuditLogType.GLUSTER_VOLUME_SNAPSHOT_RESTORE_FAILED, retVal.getVdsError().getMessage());
        } else {
            getGlusterVolumeSnapshotDao().remove(getSnapshot().getId());
            // TODO: Was discussed to mark the snapshot as restored and still maintain in engine
        }
    }

    @Override
    protected boolean canDoAction() {
        if (!super.canDoAction()) {
            return false;
        }

        if (getGlusterVolume().getStatus() != GlusterStatus.DOWN) {
            failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_GLUSTER_VOLUME_IS_UP, getGlusterVolumeName());
        }

        for (GlusterGeoRepSession session : georepSessions) {
            if (session.getSlaveVolumeId() == null || session.getSlaveNodeUuid() == null) {
                failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_REMOTE_CLUSTER_NOT_MAINTAINED_BY_ENGINE);
            }
        }

        return true;
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        if (getSucceeded()) {
            return AuditLogType.GLUSTER_VOLUME_SNAPSHOT_RESTORED;
        } else {
            return AuditLogType.GLUSTER_VOLUME_SNAPSHOT_RESTORE_FAILED;
        }
    }
}
