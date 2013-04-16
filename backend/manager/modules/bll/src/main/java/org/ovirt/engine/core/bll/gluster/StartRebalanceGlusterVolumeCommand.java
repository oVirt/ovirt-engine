package org.ovirt.engine.core.bll.gluster;

import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeRebalanceParameters;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeType;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.gluster.GlusterVolumeRebalanceVDSParameters;
import org.ovirt.engine.core.dal.VdcBllMessages;

/**
 * BLL command to Start Rebalance Gluster volume
 */
@NonTransactiveCommandAttribute
public class StartRebalanceGlusterVolumeCommand extends GlusterVolumeCommandBase<GlusterVolumeRebalanceParameters> {

    public StartRebalanceGlusterVolumeCommand(GlusterVolumeRebalanceParameters params) {
        super(params);
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__REBALANCE_START);
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__GLUSTER_VOLUME);
    }

    @Override
    protected boolean canDoAction() {
        GlusterVolumeEntity glusterVolume = getGlusterVolume();
        if (!super.canDoAction()) {
            return false;
        }

        if (!glusterVolume.isOnline()) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_GLUSTER_VOLUME_SHOULD_BE_STARTED);
            return false;
        }

        if ((glusterVolume.getVolumeType() == GlusterVolumeType.REPLICATE && glusterVolume.getBricks().size() <= glusterVolume.getReplicaCount())
                || (glusterVolume.getVolumeType() == GlusterVolumeType.STRIPE && glusterVolume.getBricks().size() <= glusterVolume.getStripeCount())
                || (glusterVolume.getBricks().size() == 1)) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_GLUSTER_VOLUME_BRICKS_ARE_NOT_DISTRIBUTED);
            return false;
        }
        return true;
    }

    @Override
    protected void executeCommand() {
        VDSReturnValue returnValue =
                                runVdsCommand(
                                        VDSCommandType.StartRebalanceGlusterVolume,
                                        new GlusterVolumeRebalanceVDSParameters(upServer.getId(),
                                                getGlusterVolumeName(), getParameters().isFixLayoutOnly(), getParameters().isForceAction()));
        if (getSucceeded()) {
            setSucceeded(returnValue.getSucceeded());
        } else {
            handleVdsError(AuditLogType.GLUSTER_VOLUME_REBALANCE_START_FAILED, returnValue.getVdsError().getMessage());
            return;
        }
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        if (getSucceeded()) {
            return AuditLogType.GLUSTER_VOLUME_REBALANCE_START;
        } else {
            return errorType == null ? AuditLogType.GLUSTER_VOLUME_REBALANCE_START_FAILED : errorType;
        }
    }
}
