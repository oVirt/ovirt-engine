package org.ovirt.engine.core.bll.utils;

import java.util.Map;

import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.businessentities.BiosType;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.MigrationSupport;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.businessentities.VmRngDevice;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.migration.NoMigrationPolicy;
import org.ovirt.engine.core.common.utils.VmCommonUtils;
import org.ovirt.engine.core.common.utils.VmCpuCountHelper;
import org.ovirt.engine.core.common.utils.customprop.ValidationError;
import org.ovirt.engine.core.common.utils.customprop.VmPropertiesUtils;
import org.ovirt.engine.core.compat.Version;

public class VmVersionUpdater {
    public static final Version LOWEST_VERSION_FOR_UPDATE = new Version(3, 6);

    /**
     * Update VM fields so that it is valid for specified compatibility version.
     */
    public void updateVmToVersion(VM vm, Version version, Cluster cluster) {
        updateVmBaseToVersion(vm.getStaticData(), version, cluster);
        vm.setClusterCompatibilityVersion(cluster.getCompatibilityVersion());
    }

    /**
     * Update VmTemplate fields so that it is valid for specified compatibility version.
     */
    public void updateVmtemplateToVersion(VmTemplate template, Version version, Cluster cluster) {
        updateVmBaseToVersion(template, version, cluster);
        template.setClusterCompatibilityVersion(cluster.getCompatibilityVersion());
    }

    public void updateVmBaseToVersion(VmBase vmBase, Version version, Cluster cluster) {
        Version sourceVersion = getSourceVersion(vmBase);
        if (!isVersionUpdatePossible(sourceVersion)) {
            return;
        }

        if (version.equals(sourceVersion)) {
            return;
        }

        updateMemory(vmBase, version);

        updateCpuTopology(vmBase, version, cluster);

        updateProperties(vmBase, version);

        if (!FeatureSupported.isMigrationSupported(cluster.getArchitecture(), version)) {
            vmBase.setMigrationSupport(MigrationSupport.PINNED_TO_HOST);
        }

        if (NoMigrationPolicy.ID.equals(vmBase.getMigrationPolicyId()) &&
                version.greaterOrEquals(Version.v4_3)) {
            vmBase.setMigrationPolicyId(cluster.getMigrationPolicyId());
        }

        if (!FeatureSupported.isBiosTypeSupported(version) &&
                vmBase.getBiosType() != BiosType.CLUSTER_DEFAULT &&
                vmBase.getBiosType() != BiosType.I440FX_SEA_BIOS) {
            vmBase.setBiosType(BiosType.CLUSTER_DEFAULT);
        }

        if (vmBase.getDefaultDisplayType() == DisplayType.cirrus &&
                version.greaterOrEquals(Version.v4_3)) {
            vmBase.setDefaultDisplayType(DisplayType.vga);
        }

        updateRngDevice(vmBase, version);

        // Update custom compatibility only if needed
        if (vmBase.getCustomCompatibilityVersion() != null) {
            vmBase.setCustomCompatibilityVersion(version);
        }

        // Setting the Cluster version origin, because the updated VM appears
        // as if it originated in a cluster with that version
        vmBase.setClusterCompatibilityVersionOrigin(version);
    }

    private void updateMemory(VmBase vmBase, Version version) {
        int maxMemoryFromConfig = VmCommonUtils.maxMemorySizeWithHotplugInMb(vmBase.getOsId(), version);

        int maxMemorySizeMb = vmBase.getMaxMemorySizeMb() == 0 ?
                VmCommonUtils.getMaxMemorySizeDefault(vmBase.getMemSizeMb()) :
                vmBase.getMaxMemorySizeMb();

        vmBase.setMaxMemorySizeMb(Math.min(maxMemorySizeMb, maxMemoryFromConfig));
        vmBase.setMemSizeMb(Math.min(vmBase.getMemSizeMb(), maxMemoryFromConfig));
        vmBase.setMinAllocatedMem(Math.min(vmBase.getMinAllocatedMem(), maxMemoryFromConfig));
    }

    private void updateCpuTopology(VmBase vmBase, Version version, Cluster cluster) {
        int maxCpuSockets = Config.<Integer>getValue(ConfigValues.MaxNumOfVmSockets, version.getValue());
        int maxCpuCoresPerSocket = Config.<Integer>getValue(ConfigValues.MaxNumOfCpuPerSocket, version.getValue());
        int maxCpuThreadsPerCore = Config.<Integer>getValue(ConfigValues.MaxNumOfThreadsPerCpu, version.getValue());
        vmBase.setNumOfSockets(Math.min(vmBase.getNumOfSockets(), maxCpuSockets));
        vmBase.setCpuPerSocket(Math.min(vmBase.getCpuPerSocket(), maxCpuCoresPerSocket));
        vmBase.setThreadsPerCpu(Math.min(vmBase.getThreadsPerCpu(), maxCpuThreadsPerCore));

        int maxTotalCpus = VmCpuCountHelper.calcMaxVCpu(vmBase, version, cluster.getArchitecture());
        if (vmBase.getNumOfCpus() <= maxTotalCpus) {
            return;
        }

        // Remove excess sockets
        int cpusToRemove = vmBase.getNumOfCpus() - maxTotalCpus;
        if (vmBase.getNumOfSockets() > 1) {
            int cpusPerSocket = vmBase.getCpuPerSocket() * vmBase.getThreadsPerCpu();
            int socketsToRemove = (cpusToRemove + cpusPerSocket - 1) / cpusPerSocket;
            vmBase.setNumOfSockets(Math.max(vmBase.getNumOfSockets() - socketsToRemove, 1));
        }

        if (vmBase.getNumOfCpus() <= maxTotalCpus) {
            return;
        }

        // Remove excess cores
        cpusToRemove = vmBase.getNumOfCpus() - maxTotalCpus;
        if (vmBase.getCpuPerSocket() > 1) {
            int threadsPerCore = vmBase.getThreadsPerCpu();
            int coresToRemove = (cpusToRemove + threadsPerCore - 1) / threadsPerCore;
            vmBase.setCpuPerSocket(Math.max(vmBase.getCpuPerSocket() - coresToRemove, 1));
        }

        if (vmBase.getNumOfCpus() <= maxTotalCpus) {
            return;
        }

        // Remove excess threads
        cpusToRemove = vmBase.getNumOfCpus() - maxTotalCpus;
        vmBase.setThreadsPerCpu(Math.max(vmBase.getThreadsPerCpu() - cpusToRemove, 1));
    }

    private void updateProperties(VmBase vmBase, Version version) {
        var propertiesUtils = getVmPropertiesUtils();
        var properties = vmBase.getCustomProperties();

        if (propertiesUtils.syntaxErrorInProperties(properties)) {
            return;
        }

        Map<String, String> propertiesMap = propertiesUtils.convertProperties(properties);

        var errors = propertiesUtils.validateVmProperties(version, propertiesMap);
        for (ValidationError error : errors) {
            propertiesMap.remove(error.getKeyName());
        }

        vmBase.setCustomProperties(propertiesUtils.convertProperties(propertiesMap));
    }

    private void updateRngDevice(VmBase vmBase, Version version) {
        for (VmDevice device : vmBase.getManagedDeviceMap().values()) {
            if (device.getType() == VmDeviceGeneralType.RNG) {
                VmRngDevice rngDevice = new VmRngDevice(device);
                rngDevice.updateSourceByVersion(version);
                device.setSpecParams(rngDevice.getSpecParams());
            }
        }
    }

    // Mocked in unit test
    protected VmPropertiesUtils getVmPropertiesUtils() {
        return VmPropertiesUtils.getInstance();
    }

    public static boolean isVersionUpdatePossible(VmBase vmBase) {
        return isVersionUpdatePossible(getSourceVersion(vmBase));
    }

    private static boolean isVersionUpdatePossible(Version sourceVersion) {
        return sourceVersion != null && sourceVersion.greaterOrEquals(LOWEST_VERSION_FOR_UPDATE);
    }

    private static Version getSourceVersion(VmBase vmBase) {
        return vmBase.getCustomCompatibilityVersion() != null ?
                vmBase.getCustomCompatibilityVersion() :
                vmBase.getClusterCompatibilityVersionOrigin();
    }
}
