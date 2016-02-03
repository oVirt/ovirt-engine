package org.ovirt.engine.core.vdsbroker.monitoring;

import org.ovirt.engine.core.common.businessentities.VDS;

/**
 * This interface defines service strategy entry points, which are needed in host monitoring phase
 */
public interface MonitoringStrategy {
    /**
     * Check VDS hardware capabilities, and update VDS accordingly
     */
    public void processHardwareCapabilities(VDS vds);

    /**
     * Check if VDS hardware capabilities processing is needed
     */
    public boolean processHardwareCapabilitiesNeeded(VDS oldVds, VDS newVds);

    /**
     * Checking for the existence of special software capabilities, and update VDS accordingly
     */
    public void processSoftwareCapabilities(VDS vds);

    /**
     * Can this VDS go to maintenance now?
     */
    public boolean canMoveToMaintenance(VDS vds);

    /**
     * Do we need to monitor this VDS?
     */
    public boolean isMonitoringNeeded(VDS vds);

    /**
     * Check if power supported for the VDS
     */
    public boolean isPowerManagementSupported();

}
