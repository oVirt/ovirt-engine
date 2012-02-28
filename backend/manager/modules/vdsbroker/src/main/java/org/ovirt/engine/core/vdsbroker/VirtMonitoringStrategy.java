package org.ovirt.engine.core.vdsbroker;

import org.ovirt.engine.core.common.businessentities.NonOperationalReason;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.compat.Guid;

/**
 * This class defines virt strategy entry points, which are needed in host monitoring phase
 */
public class VirtMonitoringStrategy implements MonitoringStrategy {

    @Override
    public boolean canMoveToMaintenance(VDS vds) {
        // We can only move to maintenance in case no VMs are running on the host
        return (vds.getvm_count() == 0);
    }

    @Override
    public boolean isMonitoringNeeded(VDS vds) {
        // No need to update the run-time info for hosts that don't run VMs
        return (vds.getstatus() != VDSStatus.NonOperational || vds.getvm_count() > 0);
    }

    @Override
    public void processSoftwareCapabilities(VDS vds) {
        boolean softwareCapabilitiesAreMet = true;

        // If we can't test for those capabilities, we don't say they don't exist
        if (vds.getkvm_enabled() != null && vds.getkvm_enabled().equals(false)) {
            softwareCapabilitiesAreMet = false;
        }

        if (!softwareCapabilitiesAreMet && vds.getstatus() != VDSStatus.NonOperational) {
            vdsNonOperational(vds);
            vds.setstatus(VDSStatus.NonOperational);
        }
    }

    @Override
    public void processHardwareCapabilities(VDS vds) {
        ResourceManager.getInstance().getEventListener().processOnCpuFlagsChange(vds.getId());
    }

    protected void vdsNonOperational(VDS vds) {
        ResourceManager.getInstance().getEventListener().vdsNonOperational(vds.getId(), NonOperationalReason.KVM_NOT_RUNNING, true, true, Guid.Empty);
    }
}

