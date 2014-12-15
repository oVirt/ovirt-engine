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
import org.ovirt.engine.core.dao.VdsDAO;
import org.ovirt.engine.core.dao.VdsGroupDAO;

/**
 * This class defines virt strategy entry points, which are needed in host monitoring phase
 */
public class VirtMonitoringStrategy implements MonitoringStrategy {

    private final VdsGroupDAO vdsGroupDao;

    private final VdsDAO vdsDao;

    protected VirtMonitoringStrategy(VdsGroupDAO vdsGroupDao, VdsDAO vdsDao) {
        this.vdsGroupDao = vdsGroupDao;
        this.vdsDao = vdsDao;
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

        if (vds.getStatus() != VDSStatus.NonOperational) {
            checkIfNotMixingRhels(vds, vdsGroup);
        }

        if (!hostCompliesWithRngDeviceSources(vds, vdsGroup) && vds.getStatus() != VDSStatus.NonOperational) {
            Map<String, String> customLogValues = new HashMap<>();
            customLogValues.put("hostSupportedRngSources", VmRngDevice.sourcesToCsv(vds.getSupportedRngSources()));
            customLogValues.put("clusterRequiredRngSources", VmRngDevice.sourcesToCsv(vdsGroup.getRequiredRngSources()));

            vdsNonOperational(vds, NonOperationalReason.RNG_SOURCES_INCOMPATIBLE_WITH_CLUSTER, customLogValues);
            vds.setStatus(VDSStatus.NonOperational);
        }
    }

    /**
     * Sets the new host to non-operational if adding a RHEL6 machine to a cluster with RHEL7s or RHEL7 to cluster with RHEL6s
     *
     * It tries to be as non-invasive as possible and only if the above is the case, turns the host into non-operational.
     */
    private void checkIfNotMixingRhels(VDS vds, VDSGroup vdsGroup) {
        if (vds.getHostOs() == null) {
            return;
        }

        String[] hostOsInfo = vds.getHostOs().split("-");

        if (hostOsInfo.length != 3) {
            return;
        }

        String newOsName = hostOsInfo[0].trim();
        String newRelease = hostOsInfo[2].trim();
        // both the CentOS and RHEL has osName RHEL
        if (newOsName.equals("RHEL") || newOsName.equals("oVirt Node") || newOsName.equals("RHEV Hypervisor")) {
            VDS beforeRhel = vdsDao.getFirstUpRhelForVdsGroup(vdsGroup.getId());
            boolean firstHostInCluster = beforeRhel == null;
            if (firstHostInCluster) {
                // no need to do any checks
                return;
            }

            // if not first host in cluster, need to check if the version is the same
            if (beforeRhel.getHostOs() == null) {
                return;
            }

            String[] prevOsInfo = beforeRhel.getHostOs().split("-");
            if (prevOsInfo.length != 3) {
                return;
            }

            String prevRelease = prevOsInfo[2].trim();
            boolean addingRhel6toRhel7 = newRelease.contains("el6") && prevRelease.contains("el7");
            boolean addingRhel7toRhel6 = newRelease.contains("el7") && prevRelease.contains("el6");
            if (addingRhel7toRhel6 || addingRhel6toRhel7) {
                Map<String, String> customLogValues = new HashMap<>();
                customLogValues.put("previousRhel", beforeRhel.getHostOs());
                customLogValues.put("addingRhel", vds.getHostOs());
                vdsNonOperational(vds, NonOperationalReason.MIXING_RHEL_VERSIONS_IN_CLUSTER, customLogValues);
                vds.setStatus(VDSStatus.NonOperational);
            }
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
