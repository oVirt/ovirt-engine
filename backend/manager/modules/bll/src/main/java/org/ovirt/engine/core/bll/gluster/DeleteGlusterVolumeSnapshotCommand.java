package org.ovirt.engine.core.bll.gluster;

import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.GlusterUtil;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeSnapshotActionParameters;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterGeoRepSession;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.gluster.GlusterVolumeSnapshotActionVDSParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.gluster.GlusterVolumeDao;
import org.ovirt.engine.core.utils.lock.EngineLock;

@NonTransactiveCommandAttribute
public class DeleteGlusterVolumeSnapshotCommand extends GlusterVolumeSnapshotCommandBase<GlusterVolumeSnapshotActionParameters> {

    @Inject
    private GlusterVolumeDao glusterVolumeDao;
    @Inject
    private GlusterUtil glusterUtil;

    private List<GlusterGeoRepSession> georepSessions;

    public DeleteGlusterVolumeSnapshotCommand(GlusterVolumeSnapshotActionParameters params,
            CommandContext commandContext) {
        super(params, commandContext);
    }

    @Override
    protected void init() {
        super.init();
        georepSessions = glusterGeoRepDao.getGeoRepSessions(getGlusterVolumeId());
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__ACTION__REMOVE);
        super.setActionMessageParameters();
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
            glusterVolumeSnapshotDao.remove(getSnapshot().getId());
        }

        return true;
    }

    @Override
    public void executeCommand() {
        if (georepSessions != null) {
            for (GlusterGeoRepSession session : georepSessions) {
                GlusterVolumeEntity slaveVolume = glusterVolumeDao.getById(session.getSlaveVolumeId());
                if (slaveVolume == null) {
                    // continue with other sessions and try to pause
                    continue;
                }

                VDS slaveUpServer = glusterUtil.getRandomUpServer(slaveVolume.getClusterId());
                if (slaveUpServer == null) {
                    handleVdsError(AuditLogType.GLUSTER_VOLUME_SNAPSHOT_DELETE_FAILED,
                            EngineError.NoUpServerFoundInRemoteCluster.name());
                    setSucceeded(false);
                    return;
                }

                try (EngineLock lock = acquireEngineLock(session.getSlaveVolumeId(), LockingGroup.GLUSTER_SNAPSHOT)) {
                    if (!deleteGlusterVolumeSnapshot(slaveUpServer.getId(),
                            slaveVolume.getName(),
                            getSnapshot().getSnapshotName())) {
                        return;
                    }
                    // Check and remove soft limit alert for the volume
                    glusterUtil.checkAndRemoveVolumeSnapshotLimitsAlert(slaveVolume);
                }
            }
        }

        deleteGlusterVolumeSnapshot(getUpServer().getId(), getGlusterVolumeName(), getSnapshot().getSnapshotName());
        // Check and remove soft limit alert for the volume
        glusterUtil.checkAndRemoveVolumeSnapshotLimitsAlert(getGlusterVolume());
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
