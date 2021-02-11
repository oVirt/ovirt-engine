package org.ovirt.engine.core.bll.utils;

import java.util.EnumSet;
import java.util.Map;
import java.util.Objects;

import org.ovirt.engine.core.common.FeatureSupported;
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

public class CompatibilityVersionUpdater {

    /**
     * Update VM fields so that it is valid for specified compatibility version.
     */
    public EnumSet<VmUpdateType> updateVmCompatibilityVersion(VM vm, Version newVersion, Cluster cluster) {
        vm.setClusterCompatibilityVersion(cluster.getCompatibilityVersion());
        return updateVmBaseCompatibilityVersion(vm.getStaticData(), newVersion, cluster);
    }

    /**
     * Update VmTemplate fields so that it is valid for specified compatibility version.
     */
    public EnumSet<VmUpdateType> updateTemplateCompatibilityVersion(VmTemplate template, Version newVersion,
                                                                    Cluster cluster) {
        template.setClusterCompatibilityVersion(cluster.getCompatibilityVersion());
        return updateVmBaseCompatibilityVersion(template, newVersion, cluster);
    }

    public EnumSet<VmUpdateType> updateVmBaseCompatibilityVersion(VmBase vmBase, Version newVersion, Cluster cluster) {
        if (newVersion.equals(getSourceVersion(vmBase))) {
            return EnumSet.noneOf(VmUpdateType.class);
        }

        EnumSet<VmUpdateType> updates = EnumSet.noneOf(VmUpdateType.class);

        if (updateMemory(vmBase, newVersion)) {
            updates.add(VmUpdateType.MEMORY);
        }
        if (updateCpuTopology(vmBase, newVersion, cluster)) {
            updates.add(VmUpdateType.CPU_TOPOLOGY);
        }
        if (updateProperties(vmBase, newVersion)) {
            updates.add(VmUpdateType.PROPERTIES);
        }
        if (updateMigrationSupport(vmBase, newVersion, cluster)) {
            updates.add(VmUpdateType.MIGRATION_SUPPORT);
        }
        if (updateMigrationPolicy(vmBase, newVersion, cluster)) {
            updates.add(VmUpdateType.MIGRATION_POLICY);
        }
        if (updateDefaultDisplayType(vmBase, newVersion)) {
            updates.add(VmUpdateType.DEFAULT_DISPLAY_TYPE);
        }
        if (updateRngDevice(vmBase, newVersion)) {
            updates.add(VmUpdateType.RNG_DEVICE);
        }

        // Update custom compatibility only if needed
        if (vmBase.getCustomCompatibilityVersion() != null) {
            vmBase.setCustomCompatibilityVersion(newVersion);
        }

        // Setting the Cluster version origin, because the updated VM appears
        // as if it originated in a cluster with that version
        vmBase.setClusterCompatibilityVersionOrigin(newVersion);
        return updates;
    }

    private boolean updateMemory(VmBase vmBase, Version newVersion) {
        int oldMaxMem = vmBase.getMaxMemorySizeMb();
        int oldMemSize = vmBase.getMemSizeMb();
        int oldMinMem = vmBase.getMinAllocatedMem();

        int maxMemoryFromConfig = VmCommonUtils.maxMemorySizeWithHotplugInMb(vmBase.getOsId(), newVersion);

        int maxMemorySizeMb = vmBase.getMaxMemorySizeMb() == 0 ?
                VmCommonUtils.getMaxMemorySizeDefault(vmBase.getMemSizeMb()) :
                vmBase.getMaxMemorySizeMb();

        vmBase.setMaxMemorySizeMb(Math.min(maxMemorySizeMb, maxMemoryFromConfig));
        vmBase.setMemSizeMb(Math.min(vmBase.getMemSizeMb(), maxMemoryFromConfig));
        vmBase.setMinAllocatedMem(Math.min(vmBase.getMinAllocatedMem(), maxMemoryFromConfig));

        return vmBase.getMaxMemorySizeMb() != oldMaxMem && oldMaxMem != 0 ||
                vmBase.getMemSizeMb() != oldMemSize ||
                vmBase.getMinAllocatedMem() != oldMinMem && oldMinMem != 0;
    }

    private boolean updateCpuTopology(VmBase vmBase, Version newVersion, Cluster cluster) {
        int oldSockets = vmBase.getNumOfSockets();
        int oldCores = vmBase.getCpuPerSocket();
        int oldThreads = vmBase.getThreadsPerCpu();

        int maxCpuSockets = Config.<Integer>getValue(ConfigValues.MaxNumOfVmSockets, newVersion.getValue());
        int maxCpuCoresPerSocket = Config.<Integer>getValue(ConfigValues.MaxNumOfCpuPerSocket, newVersion.getValue());
        int maxCpuThreadsPerCore = Config.<Integer>getValue(ConfigValues.MaxNumOfThreadsPerCpu, newVersion.getValue());
        vmBase.setNumOfSockets(Math.min(vmBase.getNumOfSockets(), maxCpuSockets));
        vmBase.setCpuPerSocket(Math.min(vmBase.getCpuPerSocket(), maxCpuCoresPerSocket));
        vmBase.setThreadsPerCpu(Math.min(vmBase.getThreadsPerCpu(), maxCpuThreadsPerCore));

        boolean updated = vmBase.getNumOfSockets() != oldSockets ||
                vmBase.getCpuPerSocket() != oldCores ||
                vmBase.getThreadsPerCpu() != oldThreads;

        int maxTotalCpus = VmCpuCountHelper.calcMaxVCpu(vmBase, newVersion, cluster.getArchitecture());
        if (vmBase.getNumOfCpus() <= maxTotalCpus) {
            return updated;
        }

        // Remove excess sockets
        int cpusToRemove = vmBase.getNumOfCpus() - maxTotalCpus;
        if (vmBase.getNumOfSockets() > 1) {
            int cpusPerSocket = vmBase.getCpuPerSocket() * vmBase.getThreadsPerCpu();
            int socketsToRemove = (cpusToRemove + cpusPerSocket - 1) / cpusPerSocket;
            vmBase.setNumOfSockets(Math.max(vmBase.getNumOfSockets() - socketsToRemove, 1));
        }

        if (vmBase.getNumOfCpus() <= maxTotalCpus) {
            return true;
        }

        // Remove excess cores
        cpusToRemove = vmBase.getNumOfCpus() - maxTotalCpus;
        if (vmBase.getCpuPerSocket() > 1) {
            int threadsPerCore = vmBase.getThreadsPerCpu();
            int coresToRemove = (cpusToRemove + threadsPerCore - 1) / threadsPerCore;
            vmBase.setCpuPerSocket(Math.max(vmBase.getCpuPerSocket() - coresToRemove, 1));
        }

        if (vmBase.getNumOfCpus() <= maxTotalCpus) {
            return true;
        }

        // Remove excess threads
        cpusToRemove = vmBase.getNumOfCpus() - maxTotalCpus;
        vmBase.setThreadsPerCpu(Math.max(vmBase.getThreadsPerCpu() - cpusToRemove, 1));
        return true;
    }

    private boolean updateProperties(VmBase vmBase, Version newVersion) {
        var propertiesUtils = getVmPropertiesUtils();
        var properties = vmBase.getCustomProperties();

        if (propertiesUtils.syntaxErrorInProperties(properties)) {
            return false;
        }

        Map<String, String> propertiesMap = propertiesUtils.convertProperties(properties);

        var errors = propertiesUtils.validateVmProperties(newVersion, propertiesMap);
        for (ValidationError error : errors) {
            propertiesMap.remove(error.getKeyName());
        }

        vmBase.setCustomProperties(propertiesUtils.convertProperties(propertiesMap));
        return !errors.isEmpty();
    }

    private boolean updateMigrationSupport(VmBase vmBase, Version newVersion, Cluster cluster) {
        if (!FeatureSupported.isMigrationSupported(cluster.getArchitecture(), newVersion)) {
            var oldMigrationSupport = vmBase.getMigrationSupport();
            vmBase.setMigrationSupport(MigrationSupport.PINNED_TO_HOST);
            return oldMigrationSupport != MigrationSupport.PINNED_TO_HOST;
        }
        return false;
    }


    private boolean updateMigrationPolicy(VmBase vmBase, Version newVersion, Cluster cluster) {
        if (NoMigrationPolicy.ID.equals(vmBase.getMigrationPolicyId()) &&
                newVersion.greaterOrEquals(Version.v4_3)) {
            vmBase.setMigrationPolicyId(cluster.getMigrationPolicyId());
            return !Objects.equals(NoMigrationPolicy.ID, vmBase.getMigrationPolicyId());
        }
        return false;
    }

    private boolean updateDefaultDisplayType(VmBase vmBase, Version newVersion) {
        if (vmBase.getDefaultDisplayType() == DisplayType.cirrus &&
                newVersion.greaterOrEquals(Version.v4_3)) {
            vmBase.setDefaultDisplayType(DisplayType.vga);
            return true;
        }
        return false;
    }

    private boolean updateRngDevice(VmBase vmBase, Version newVersion) {
        boolean updated = false;
        for (VmDevice device : vmBase.getManagedDeviceMap().values()) {
            if (device.getType() == VmDeviceGeneralType.RNG) {
                VmRngDevice rngDevice = new VmRngDevice(device);
                rngDevice.updateSourceByVersion(newVersion);
                updated = updated || !Objects.equals(device.getSpecParams(),
                        rngDevice.getSpecParams());

                device.setSpecParams(rngDevice.getSpecParams());
            }
        }
        return updated;
    }

    // Mocked in unit test
    protected VmPropertiesUtils getVmPropertiesUtils() {
        return VmPropertiesUtils.getInstance();
    }

    private static Version getSourceVersion(VmBase vmBase) {
        return vmBase.getCustomCompatibilityVersion() != null ?
                vmBase.getCustomCompatibilityVersion() :
                vmBase.getClusterCompatibilityVersionOrigin();
    }
}
