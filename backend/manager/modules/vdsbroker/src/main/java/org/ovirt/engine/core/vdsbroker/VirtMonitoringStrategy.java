package org.ovirt.engine.core.vdsbroker;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.businessentities.NonOperationalReason;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VmRngDevice;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.utils.ListUtils;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.VdsGroupDAO;

/**
 * This class defines virt strategy entry points, which are needed in host monitoring phase
 */
public class VirtMonitoringStrategy implements MonitoringStrategy {

    private final VdsGroupDAO vdsGroupDao;

    protected VirtMonitoringStrategy(VdsGroupDAO vdsGroupDao) {
        this.vdsGroupDao = vdsGroupDao;
    }

    @Override
    public boolean canMoveToMaintenance(VDS vds) {
        // We can only move to maintenance in case no VMs are running on the host
        return (vds.getVmCount() == 0);
    }

    @Override
    public boolean isMonitoringNeeded(VDS vds) {
        // No need to update the run-time info for hosts that don't run VMs, depends on the non-operational reason
        return vds.getStatus() != VDSStatus.NonOperational || vds.getVmCount() > 0;
    }

    @Override
    public void processSoftwareCapabilities(VDS vds) {

        // If we can't test for those capabilities, we don't say they don't exist
        if (vds.getKvmEnabled() != null && vds.getKvmEnabled().equals(false) && vds.getStatus() != VDSStatus.NonOperational) {
            vdsNonOperational(vds, NonOperationalReason.KVM_NOT_RUNNING, null);
            vds.setStatus(VDSStatus.NonOperational);
        }


        VDSGroup vdsGroup = vdsGroupDao.get(vds.getVdsGroupId());
        if (!hostCompliesWithClusterEmulationMode(vds, vdsGroup) && vds.getStatus() != VDSStatus.NonOperational) {

            Map<String, String> customLogValues = new HashMap<>();
            customLogValues.put("hostSupportedEmulatedMachines", vds.getSupportedEmulatedMachines());
            if (vdsGroup.isDetectEmulatedMachine()) {
                customLogValues.put("clusterEmulatedMachines", Config.<List<String>>getValue(ConfigValues.ClusterEmulatedMachines, vds.getVdsGroupCompatibilityVersion().getValue()).toString());
                vdsNonOperational(vds, NonOperationalReason.EMULATED_MACHINES_INCOMPATIBLE_WITH_CLUSTER_LEVEL, customLogValues);
            } else {
                customLogValues.put("clusterEmulatedMachines", vdsGroup.getEmulatedMachine());
                vdsNonOperational(vds, NonOperationalReason.EMULATED_MACHINES_INCOMPATIBLE_WITH_CLUSTER, customLogValues);
            }


            vds.setStatus(VDSStatus.NonOperational);
        }

        if (!hostCompliesWithRngDeviceSources(vds, vdsGroup) && vds.getStatus() != VDSStatus.NonOperational) {
            Map<String, String> customLogValues = new HashMap<>();
            customLogValues.put("hostSupportedRngSources", VmRngDevice.sourcesToCsv(vds.getSupportedRngSources()));
            customLogValues.put("clusterRequiredRngSources", VmRngDevice.sourcesToCsv(vdsGroup.getRequiredRngSources()));

            vdsNonOperational(vds, NonOperationalReason.RNG_SOURCES_INCOMPATIBLE_WITH_CLUSTER, customLogValues);
            vds.setStatus(VDSStatus.NonOperational);
        }
    }

    private boolean hostCompliesWithRngDeviceSources(VDS vds, VDSGroup vdsGroup) {
        return vds.getSupportedRngSources().containsAll(vdsGroup.getRequiredRngSources());
    }

    @Override
    public void processHardwareCapabilities(VDS vds) {
        ResourceManager.getInstance().getEventListener().processOnCpuFlagsChange(vds.getId());
    }

    protected void vdsNonOperational(VDS vds, NonOperationalReason reason, Map<String, String> customLogValues) {
        ResourceManager.getInstance()
                .getEventListener()
                .vdsNonOperational(vds.getId(), reason, true, Guid.Empty, customLogValues);
    }

    @Override
    public boolean processHardwareCapabilitiesNeeded(VDS oldVds, VDS newVds) {
        return !StringUtils.equals(oldVds.getCpuFlags(), newVds.getCpuFlags());
    }

    @Override
    public boolean isPowerManagementSupported() {
        return true;
    }

    private boolean hostCompliesWithClusterEmulationMode(VDS vds, VDSGroup vdsGroup) {

        // the initial cluster emulated machine value is set by the first host that complies.
        if (vdsGroup.isDetectEmulatedMachine()) {
            return hostEmulationModeMatchesTheConfigValues(vds);
        } else {
            // the cluster has the emulated machine flag set. match the host against it.
            return vds.getSupportedEmulatedMachines() != null ? Arrays.asList(vds.getSupportedEmulatedMachines().split(",")).contains(vdsGroup.getEmulatedMachine()) : false;
        }
    }

    private boolean hostEmulationModeMatchesTheConfigValues(VDS vds) {
        // match this host against the config flags by order
        String matchedEmulatedMachine =
                ListUtils.firstMatch(
                        Config.<List<String>> getValue(ConfigValues.ClusterEmulatedMachines,
                                vds.getVdsGroupCompatibilityVersion().getValue()),
                        vds.getSupportedEmulatedMachines().split(","));

        if (matchedEmulatedMachine != null && !matchedEmulatedMachine.isEmpty()) {
            setClusterEmulatedMachine(vds, matchedEmulatedMachine);
            return true;
        }
        return false;
    }

    private void setClusterEmulatedMachine(VDS vds, String matchedEmulatedMachine) {
        // host matches and its value will set the cluster emulated machine
        DbFacade.getInstance().getVdsGroupDao().setEmulatedMachine(vds.getVdsGroupId(), matchedEmulatedMachine, false);
    }
}
