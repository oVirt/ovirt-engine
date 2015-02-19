package org.ovirt.engine.core.bll.gluster;

import org.ovirt.engine.core.common.action.gluster.GlusterVolumeSnapshotActionParameters;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeSnapshotEntity;
import org.ovirt.engine.core.common.constants.gluster.GlusterConstants;
import org.ovirt.engine.core.common.errors.VdcBllMessages;

public abstract class GlusterVolumeSnapshotCommandBase<T extends GlusterVolumeSnapshotActionParameters> extends GlusterSnapshotCommandBase<T> {
    private GlusterVolumeSnapshotEntity snapshot;

    public GlusterVolumeSnapshotCommandBase(T params) {
        super(params);
        snapshot = getGlusterVolumeSnapshotDao().getByName(getGlusterVolumeId(), getParameters().getSnapshotName());
    }

    @Override
    protected void setActionMessageParameters() {
        addCustomValue(GlusterConstants.VOLUME_SNAPSHOT_NAME, getParameters().getSnapshotName());
    }

    @Override
    protected boolean canDoAction() {
        if (!super.canDoAction()) {
            return false;
        }

        if (getSnapshot() == null) {
            failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_GLUSTER_VOLUME_SNAPSHOT_DOES_NOT_EXIST,
                    getParameters().getSnapshotName());
        }

        return true;
    }

    protected GlusterVolumeSnapshotEntity getSnapshot() {
        return this.snapshot;
    }
}
