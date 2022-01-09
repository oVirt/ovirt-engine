package org.ovirt.engine.core.common;

import java.util.Map;

import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.Version;

/**
 * Convenience class to check if a feature is supported or not in any given version.<br>
 * Methods should be named by feature and accept version to check against.
 */
public class FeatureSupported {

    public static boolean supportedInConfig(ConfigValues feature, Version version) {
        Boolean value = Config.<Boolean> getValue(feature, version.getValue());
        if (value == null) {
            throw new IllegalArgumentException(feature.toString() + " has no value for version: " + version);
        }
        return value;
    }

    public static boolean supportedInConfig(ConfigValues feature, Version version, ArchitectureType arch) {
        Map<String, String> archOptions = Config.getValue(feature, version.getValue());
        String value = archOptions.get(arch.name());
        if (value == null) {
            value = archOptions.get(arch.getFamily().name());
        }
        return Boolean.parseBoolean(value);
    }

    public static boolean hotPlugCpu(Version version, ArchitectureType arch) {
        return supportedInConfig(ConfigValues.HotPlugCpuSupported, version, arch);
    }

    public static boolean hotUnplugCpu(Version version, ArchitectureType arch) {
        return supportedInConfig(ConfigValues.HotUnplugCpuSupported, version, arch);
    }

    public static boolean hotPlugMemory(Version version, ArchitectureType arch) {
        return supportedInConfig(ConfigValues.HotPlugMemorySupported, version, arch);
    }

    public static boolean hotUnplugMemory(Version version, ArchitectureType arch) {
        return supportedInConfig(ConfigValues.HotUnplugMemorySupported, version, arch);
    }

   /**
     * Checks if migration is supported by the given CPU architecture
     *
     * @param architecture
     *            The CPU architecture
     * @param version
     *            Compatibility version to check for.
     */
    public static boolean isMigrationSupported(ArchitectureType architecture, Version version) {
        return supportedInConfig(ConfigValues.IsMigrationSupported, version, architecture);
    }

    /**
     * Check if the migrate encrypted is supported for the given version
     * @param version Compatibility version to check for.
     * @return
     */
    public static boolean isMigrateEncryptedSupported(Version version) {
        return version.greaterOrEquals(Version.v4_4);
    }

    /**
     * Check if the asynchronous live snapshot is supported for the given version
     * @param version Compatibility version to check for.
     * @param vds The VDS the snapshot is going to be performed on.
     * @return {@code true} if asynchronous live snapshot is supported for this version.
     */
    public static boolean isAsyncLiveSnapshotSupported(Version version, VDS vds) {
        final Version minVersion = Version.v4_4;
        return version.greaterOrEquals(minVersion) || vds.getSupportedClusterVersionsSet().contains(minVersion);
    }

    /**
     * Checks if SCSI reservations are supported by the cluster version
     *
     * @param version
     *            Compatibility version to check for.
     */
    public static boolean isScsiReservationSupported(Version version) {
        return supportedInConfig(ConfigValues.ScsiReservationSupported, version);
    }

    /**
     * Checks if memory snapshot is supported by architecture
     *
     * @param architecture
     *            The CPU architecture
     * @param version
     *            Compatibility version to check for.
     */
    public static boolean isMemorySnapshotSupportedByArchitecture(ArchitectureType architecture, Version version) {
        return supportedInConfig(ConfigValues.IsMemorySnapshotSupported, version, architecture);
    }

    /**
     * Checks if suspend is supported by architecture
     *
     * @param architecture
     *            The CPU architecture
     * @param version
     *            Compatibility version to check for.
     */
    public static boolean isSuspendSupportedByArchitecture(ArchitectureType architecture, Version version) {
        return supportedInConfig(ConfigValues.IsSuspendSupported, version, architecture);
    }

    public static boolean ipv6IscsiSupported(Version version) {
        return supportedInConfig(ConfigValues.ipv6IscsiSupported, version);
    }

    /**
     * @param version
     *            Compatibility version to check for.
     * @return <code>true</code> if gluster libgfapi access is supported for the given version
     */
    public static boolean libgfApiSupported(Version version) {
        return supportedInConfig(ConfigValues.LibgfApiSupported, version);
    }

    /**
     * @param version Compatibility version to check for.
     * @return {@code true} if Managed block domain storage domain is supported for this version.
     */
    public static boolean isManagedBlockDomainSupported(Version version) {
        return supportedInConfig(ConfigValues.ManagedBlockDomainSupported, version);
    }

    /**
     * Firewalld is supported for host if it supports cluster version 4.2.
     *
     * @param vds the host we are insterested in
     * @return true if host support firewalld
     */
    public static boolean isFirewalldSupported(VDS vds) {
        return vds.getSupportedClusterVersionsSet().contains(Version.v4_2);
    }

    /**
     * @param version Compatibility version to check for.
     * @return {@code true} if getting an custom bond name is supported for this version.
     */
    public static boolean isCustomBondNameSupported(Version version) {
        return supportedInConfig(ConfigValues.CustomBondNameSupported, version);
    }

    /**
     * Checks if BIOS Type configuration supported
     *
     * @param version Compatibility version to check for.
     */
    public static boolean isBiosTypeSupported(Version version) {
        return supportedInConfig(ConfigValues.BiosTypeSupported, version);
    }

    /**
     * Check if aio=native should be used for Gluster storage
     * instead of threads
     *
     * @param version Compatibility version to check for.
     */
    public static boolean useNativeIOForGluster(Version version) {
        return supportedInConfig(ConfigValues.UseNativeIOForGluster, version);
    }

    /**
     * @param version
     *            Check if the Hyper-V KVM enlightenments are supported.
     * @return <code>true</code> if Hyper-V KVM enlightenments are supported for the given version.
     */
    public static boolean hyperVSynicStimerSupported(Version version) {
        return supportedInConfig(ConfigValues.HyperVSynicStimerSupported, version);
    }

    /**
     * Check if vGPU placement is supported
     *
     * @param version Compatibility version to check for.
     */
    public static boolean isVgpuPlacementSupported(Version version) {
        return supportedInConfig(ConfigValues.VgpuPlacementSupported, version);
    }

    /**
     * Check if vGPU framebuffer is supported
     *
     * @param version Compatibility version to check for.
     */
    public static boolean isVgpuFramebufferSupported(Version version) {
        return supportedInConfig(ConfigValues.VgpuFramebufferSupported, version);
    }

    /**
     * Skip commit network changes is supported for
     * - host supporting commitOnSuccess (>= 4.3)
     * - engine has at least version 4.4
     *
     * @param vds the host
     * @return true if skipping the commit is allowed
     */
    public static boolean isSkipCommitNetworkChangesSupported(VDS vds) {
        return vds != null && Version.v4_3.lessOrEquals(vds.getSupportedClusterVersionsSet());
    }

    /**
     * Do not measure volume size with MeasureVolume if datacenter is < 4.4
     * The API is exposed only in vdsm >= 4.4
     * @param vds the host
     * @return true if measure volume can be used
     */
    public static boolean isMeasureVolumeSupported(VDS vds) {
        return vds != null && Version.v4_4.lessOrEquals(vds.getClusterCompatibilityVersion());
    }

    /**
     * Check if incremental backup is supported.
     *
     * @param version Compatibility version to check for.
     * @return true if incremental backup is supported.
     */
    public static boolean isIncrementalBackupSupported(Version version) {
        return supportedInConfig(ConfigValues.IsIncrementalBackupSupported, version);
    }

    /**
     * Check if isolated port is supported.
     *
     * @param version Compatibility version to check for.
     * @return true if isolated port is supported.
     */
    public static boolean isPortIsolationSupported(Version version) {
        return supportedInConfig(ConfigValues.IsPortIsolationSupported, version);
    }

    /**
     * Check if using backup mode and bitmaps operations are supported.
     *
     * @param version Compatibility version to check for.
     * @return true if backup mode supported and bitmaps operations are supported.
     */
    public static boolean isBackupModeAndBitmapsOperationsSupported(Version version) {
        return Version.v4_5.lessOrEquals(version);
    }

    /**
     * Check if backup using a single checkpoint is supported.
     *
     * @param version Compatibility version to check for.
     * @return true if backup using a single checkpoint is supported.
     */
    public static boolean isBackupSingleCheckpointSupported(Version version) {
        return Version.v4_6.lessOrEquals(version);
    }

    /**
     * Check if v2 of OpenStack Image Service API is supported.
     * The API v2 is supported only in vdsm of oVirt >= 4.4
     *
     * @param version Compatibility version to check for.
     * @return true if Image Service API v2 is supported.
     */
    public static boolean isOpenStackImageServiceApiV2Supported(Version version) {
        return Version.v4_4.lessOrEquals(version);
    }

    /**
     * Check if the implicit affinity group is supported
     *
     * @param version Compatibility version to check for.
     * @return true if the implicit affinity group is supported.
     */
    public static boolean isImplicitAffinityGroupSupported(Version version) {
        return Version.v4_4.greater(version);
    }

    /**
     * Check if TSC frequency is supported
     *
     * @param version Compatibility version to check for.
     * @return true if TSC frequency is supported.
     */
    public static boolean isTscFrequencySupported(Version version) {
        return Version.v4_4.lessOrEquals(version);
    }

    /**
     * Check if Windows Guest Tools ISO is supported.
     * The RPM is fully supported only until oVirt < 4.4
     *
     * @param version Compatibility version to check for.
     * @return true if WGT is supported.
     */
    public static boolean isWindowsGuestToolsSupported(Version version) {
        return version.less(Version.v4_4);
    }

    /**
     * Check if switching the master storage domain operation is supported.
     *
     * @param version Compatibility version to check for.
     * @return true if switching the master storage domain operation is supported.
     */
    public static boolean isSwitchMasterStorageDomainOperationSupported(Version version) {
        return Version.v4_5.lessOrEquals(version);
    }

    /**
     * Check if cluster FIPS mode is supported
     *
     * @param version Compatibility version to check for.
     * @return true if cluster FIPS mode is supported.
     */
    public static boolean isFipsModeSupported(Version version) {
        return Version.v4_4.lessOrEquals(version);
    }

    /**
     * Check if TPM device is supported.
     *
     * @param version Compatibility version to check for.
     * @return true if TPM device is supported.
     */
    public static boolean isTpmDeviceSupported(Version version, ArchitectureType arch) {
        return supportedInConfig(ConfigValues.TpmDeviceSupported, version, arch);
    }

    /**
     * Check if NVRAM data persistence is supported.
     *
     * @param version Compatibility version to check for.
     * @return true if TPM device is supported.
     */
    public static boolean isNvramPersistenceSupported(Version version) {
        return supportedInConfig(ConfigValues.NvramPersistenceSupported, version);
    }

    /**
     * Checks if bochs display support enabled for the cluster version
     *
     * @param version
     *            Compatibility version to check for.
     */
    public static boolean isBochsDisplayEnabled(Version version) {
        return supportedInConfig(ConfigValues.EnableBochsDisplay, version);
    }

    /**
     * Checks if freeze in engine for live snapshot support enabled for the cluster version
     *
     * @param version
     *            Compatibility version to check for.
     */
    public static boolean isFreezeInEngineEnabled(Version version) {
        return supportedInConfig(ConfigValues.LiveSnapshotPerformFreezeInEngine, version);
    }

    /**
     * Checks if Reset-VM is supported
     *
     * @param version Compatibility version to check for.
     * @return true if Reset-VM is supported.
     */
    public static boolean isVMResetSupported(Version version) {
        return Version.v4_6.lessOrEquals(version);
    }

    /**
     * Checks if host can perform copy on Managed Block Storage disks
     *
     * @param vds the host
     * @return true if the host can perform the copy
     */
    public static boolean isHostSupportsMBSCopy(VDS vds) {
        return vds != null && vds.getConnectorInfo() != null
                && Version.v4_6.lessOrEquals(vds.getClusterCompatibilityVersion());
    }

    /**
     * Checks if Screenshot-VM is supported
     *
     * @param version Compatibility version to check for.
     * @return true if Screenshot-VM is supported.
     */
    public static boolean isVMScreenshotSupported(Version version) {
        return Version.v4_7.lessOrEquals(version);
    }

}
