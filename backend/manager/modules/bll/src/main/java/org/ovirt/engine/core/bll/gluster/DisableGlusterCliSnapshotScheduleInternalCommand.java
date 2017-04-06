package org.ovirt.engine.core.bll.gluster;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeActionParameters;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.gluster.GlusterVolumeActionVDSParameters;
import org.ovirt.engine.core.dao.ClusterDao;

@InternalCommandAttribute
public class DisableGlusterCliSnapshotScheduleInternalCommand<T extends GlusterVolumeActionParameters> extends GlusterVolumeCommandBase<T> {

    @Inject
    private ClusterDao clusterDao;

    public DisableGlusterCliSnapshotScheduleInternalCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected void executeCommand() {
        Cluster cluster = getCluster();

        VDSReturnValue retValue =
                runVdsCommand(VDSCommandType.OverrideGlusterVolumeSnapshotSchedule,
                        new GlusterVolumeActionVDSParameters(getUpServer().getId(),
                                getGlusterVolumeName(),
                                getParameters().isForceAction()));

        setSucceeded(retValue.getSucceeded());

        if (!retValue.getSucceeded()) {
            handleVdsError(AuditLogType.GLUSTER_CLI_SNAPSHOT_SCHEDULE_DISABLE_FAILED, retValue.getVdsError()
                    .getMessage());
        } else {
            // If force is passed as true, then VDSM verb disables the CLI scheduling as well
            // else it just sets the scheduler type as ovirt in. If actual schedule disabling
            // is of snapshot schedule happens in gluster side, then only we set the flag
            // cliBasedSnapshotSchedulingOn=false and persist in engine side
            if (getParameters().isForceAction()) {
                cluster.setGlusterCliBasedSchedulingOn(false);
                clusterDao.update(cluster);
            }
        }
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        if (getSucceeded()) {
            return AuditLogType.GLUSTER_CLI_SNAPSHOT_SCHEDULE_DISABLED;
        } else {
            return errorType == null ? AuditLogType.GLUSTER_CLI_SNAPSHOT_SCHEDULE_DISABLE_FAILED : errorType;
        }
    }
}
