package org.ovirt.engine.core.bll.gluster;

import java.util.List;

import org.ovirt.engine.core.bll.Backend;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeBricksActionParameters;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.gluster.GlusterVolumeBricksActionVDSParameters;
import org.ovirt.engine.core.dal.VdcBllMessages;

public class AddBricksToGlusterVolumeCommand extends GlusterVolumeCommandBase<GlusterVolumeBricksActionParameters> {

    private static final long serialVersionUID = 1798863209150948961L;

    public AddBricksToGlusterVolumeCommand(GlusterVolumeBricksActionParameters params) {
        super(params);
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__ADD);
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__GLUSTER_BRICK);
    }

    @Override
    protected boolean canDoAction() {
        if (!super.canDoAction()) {
            return false;
        }

        if (getParameters().getBricks() == null || getParameters().getBricks().size() == 0) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_BRICKS_REQUIRED);
            return false;
        }
        return true;
    }

    @Override
    protected void executeCommand() {
        VDSReturnValue returnValue =
                Backend
                        .getInstance()
                        .getResourceManager()
                        .RunVdsCommand(
                                VDSCommandType.AddBricksToGlusterVolume,
                                new GlusterVolumeBricksActionVDSParameters(getUpServer().getId(),
                                        getGlusterVolumeName(),
                                        getParameters().getBricks(),
                                        getParameters().getReplicaCount(),
                                        getParameters().getStripeCount()));
        setSucceeded(returnValue.getSucceeded());

        if (getSucceeded()) {
            addGlusterVolumeBricksInDb(getParameters().getBricks(),
                    getParameters().getReplicaCount(),
                    getParameters().getStripeCount());
        }
    }

    private void addGlusterVolumeBricksInDb(List<GlusterBrickEntity> bricks, int replicaCount, int stripeCount) {
        for (GlusterBrickEntity brick : bricks) {
            getGlusterVolumeDao().addBrickToVolume(brick);
        }
        if (replicaCount != 0) {
            getGlusterVolumeDao().updateReplicaCount(bricks.get(0).getVolumeId(), replicaCount);
        }
        if (stripeCount != 0) {
            getGlusterVolumeDao().updateStripeCount(bricks.get(0).getVolumeId(), stripeCount);
        }
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        if (getSucceeded()) {
            return AuditLogType.GLUSTER_VOLUME_ADD_BRICK;
        } else {
            return AuditLogType.GLUSTER_VOLUME_ADD_BRICK_FAILED;
        }
    }
}
