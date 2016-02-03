package org.ovirt.engine.core.vdsbroker.monitoring;

import org.ovirt.engine.core.common.businessentities.NonOperationalReason;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.VdsIdVDSCommandParametersBase;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.vdsbroker.ResourceManager;

/**
 * This class defines gluster strategy entry points, which are needed in host monitoring phase
 */
public class GlusterMonitoringStrategy implements MonitoringStrategy {

    @Override
    public boolean canMoveToMaintenance(VDS vds) {
        return true;
    }

    @Override
    public boolean isMonitoringNeeded(VDS vds) {
        return true;
    }

    @Override
    public void processSoftwareCapabilities(VDS vds) {
        // check if gluster is running
        VDSReturnValue returnValue = ResourceManager.getInstance().runVdsCommand(VDSCommandType.GlusterServersList,
                new VdsIdVDSCommandParametersBase(vds.getId()));
        if (!returnValue.getSucceeded()) {
            vds.setStatus(VDSStatus.NonOperational);
            vds.setNonOperationalReason(NonOperationalReason.GLUSTER_COMMAND_FAILED);
            vdsNonOperational(vds, NonOperationalReason.GLUSTER_COMMAND_FAILED);
        }
    }

    private void vdsNonOperational(VDS vds, NonOperationalReason reason) {
        ResourceManager.getInstance().getEventListener().vdsNonOperational(vds.getId(), reason, true, Guid.Empty, null);
    }

    @Override
    public void processHardwareCapabilities(VDS vds) {
    }

    @Override
    public boolean processHardwareCapabilitiesNeeded(VDS oldVds, VDS newVds) {
        return false;
    }

    @Override
    public boolean isPowerManagementSupported() {
        return false;
    }
}
