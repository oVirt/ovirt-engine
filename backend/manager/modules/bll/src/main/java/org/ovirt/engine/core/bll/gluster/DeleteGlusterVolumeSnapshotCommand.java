package org.ovirt.engine.core.bll.gluster;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeSnapshotActionParameters;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.gluster.GlusterVolumeSnapshotActionVDSParameters;

public class DeleteGlusterVolumeSnapshotCommand extends GlusterVolumeSnapshotCommandBase<GlusterVolumeSnapshotActionParameters> {
    public DeleteGlusterVolumeSnapshotCommand(GlusterVolumeSnapshotActionParameters params) {
        super(params);
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__REMOVE);
    }

    @Override
    public void executeCommand() {
        VDSReturnValue retVal =
                runVdsCommand(VDSCommandType.DeleteGlusterVolume,
                        new GlusterVolumeSnapshotActionVDSParameters(getUpServer().getId(),
                                getGlusterVolumeName(),
                                getParameters().getSnapshotName()));
        setSucceeded(retVal.getSucceeded());
        if (!getSucceeded()) {
            handleVdsError(AuditLogType.GLUSTER_VOLUME_SNAPSHOT_DELETE_FAILED, retVal.getVdsError().getMessage());
        } else {
            getGlusterVolumeSnapshotDao().remove(getSnapshot().getId());
        }
    }

    @Override
    protected boolean canDoAction() {
        if (!super.canDoAction()) {
            return false;
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
