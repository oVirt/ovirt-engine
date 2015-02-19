package org.ovirt.engine.core.bll.gluster;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeParameters;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeSnapshotEntity;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.gluster.GlusterVolumeVDSParameters;
import org.ovirt.engine.core.compat.Guid;

public class DeleteAllGlusterVolumeSnapshotsCommand extends GlusterSnapshotCommandBase<GlusterVolumeParameters> {
    List<GlusterVolumeSnapshotEntity> snapshots;

    public DeleteAllGlusterVolumeSnapshotsCommand(GlusterVolumeParameters params) {
        super(params);
        snapshots = getGlusterVolumeSnapshotDao().getAllByVolumeId(getGlusterVolumeId());
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__REMOVE);
    }

    @Override
    public void executeCommand() {
        VDSReturnValue retVal =
                runVdsCommand(VDSCommandType.DeleteAllGlusterVolumeSnapshots,
                        new GlusterVolumeVDSParameters(getUpServer().getId(), getGlusterVolumeName()));
        setSucceeded(retVal.getSucceeded());

        if (!getSucceeded()) {
            handleVdsError(AuditLogType.GLUSTER_VOLUME_ALL_SNAPSHOTS_DELETE_FAILED, retVal.getVdsError().getMessage());
        } else {
            List<Guid> guids = new ArrayList<>();
            for (GlusterVolumeSnapshotEntity snapshot : snapshots) {
                guids.add(snapshot.getId());
            }
            getGlusterVolumeSnapshotDao().removeAll(guids);
        }
    }

    @Override
    protected boolean canDoAction() {
        if (!super.canDoAction()) {
            return false;
        }

        if (snapshots == null || snapshots.isEmpty()) {
            failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_GLUSTER_VOLUME_NO_SNAPSHOTS_EXIST, getGlusterVolumeName());
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
