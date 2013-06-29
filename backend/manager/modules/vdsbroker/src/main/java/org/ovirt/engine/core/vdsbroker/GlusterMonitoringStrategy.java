package org.ovirt.engine.core.vdsbroker;

import org.ovirt.engine.core.common.businessentities.VDS;

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
