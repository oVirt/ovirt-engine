package org.ovirt.engine.core.vdsbroker;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.businessentities.NonOperationalReason;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
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
        // No need to update the run-time info for hosts that don't run VMs
        return (vds.getStatus() != VDSStatus.NonOperational || vds.getVmCount() > 0);
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
            customLogValues.put("clusterEmulatedMachines", StringUtils.isEmpty(vdsGroup.getEmulatedMachine()) ?
                    Config.<String>GetValue(ConfigValues.ClusterEmulatedMachines, vds.getVdsGroupCompatibilityVersion().getValue()) :
                    vdsGroup.getEmulatedMachine());

            vdsNonOperational(vds, NonOperationalReason.EMULATED_MACHINES_INCOMPATIBLE_WITH_CLUSTER, customLogValues);
            vds.setStatus(VDSStatus.NonOperational);
        }

    }

    @Override
    public void processHardwareCapabilities(VDS vds) {
        ResourceManager.getInstance().getEventListener().processOnCpuFlagsChange(vds.getId());
    }

    protected void vdsNonOperational(VDS vds, NonOperationalReason reason, Map<String, String> customLogValues) {
        ResourceManager.getInstance().getEventListener().vdsNonOperational(vds.getId(), reason, true, true, Guid.Empty, customLogValues);
    }

    @Override
    public boolean processHardwareCapabilitiesNeeded(VDS oldVds, VDS newVds) {
        return !StringUtils.equals(oldVds.getCpuFlags(), newVds.getCpuFlags());
    }

    private boolean hostCompliesWithClusterEmulationMode(VDS vds, VDSGroup vdsGroup) {

        String clusterEmulatedMachine = vdsGroup.getEmulatedMachine();
        String[] hostSupportedEmulatedMachines =
                vds.getSupportedEmulatedMachines() != null ? vds.getSupportedEmulatedMachines().split(",") : new String[]{""};

        // the initial cluster emulated machine value is set by the first host that complies.
        if (clusterEmulatedMachine == null || clusterEmulatedMachine.isEmpty()) {
            return hostEmulationModeMatchesTheConfigValues(vds, hostSupportedEmulatedMachines);
        } else {
            // the cluster has the emulated machine flag set. match the host on it.
            return Arrays.asList(hostSupportedEmulatedMachines).contains(clusterEmulatedMachine);
        }
    }

    private boolean hostEmulationModeMatchesTheConfigValues(VDS vds, String[] hostSupportedEmulatedMachines) {
        // match this host against the config flags by order
        String matchedEmulatedMachine =
                ListUtils.firstMatch(
                        Arrays.asList(Config.<String> GetValue(ConfigValues.ClusterEmulatedMachines,
                                vds.getVdsGroupCompatibilityVersion().getValue()).split(",")),
                        hostSupportedEmulatedMachines);

        if (matchedEmulatedMachine != null && !matchedEmulatedMachine.isEmpty()) {
            setClusterEmulatedMachine(vds, matchedEmulatedMachine);
            return true;
        }
        return false;
    }

    private void setClusterEmulatedMachine(VDS vds, String matchedEmulatedMachine) {
        // host matches and its value will set the cluster emulated machine
        DbFacade.getInstance().getVdsGroupDao().setEmulatedMachine(vds.getVdsGroupId(), matchedEmulatedMachine);
    }
}
