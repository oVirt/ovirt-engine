package org.ovirt.engine.core.common;

import java.util.Map;

import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.Version;

/**
 * Convenience class to check if a feature is supported or not in any given version.<br>
 * Methods should be named by feature and accept version to check against.
 */
public class FeatureSupported {

    public static boolean supportedInConfig(ConfigValues feature, Version version) {
        return Config.<Boolean> getValue(feature, version.getValue());
    }

    public static boolean supportedInConfig(ConfigValues feature, Version version, ArchitectureType arch) {
        Map<String, String> archOptions = Config.<Map>getValue(feature, version.getValue());
        String value = archOptions.get(arch.name());
        if (value == null) {
            value = archOptions.get(arch.getFamily().name());
        }
        return Boolean.parseBoolean(value);
    }

    /**
     * @param version
     *            Compatibility version to check for.
     * @return <code>true</code> iff VDSM reports total RX/TX interface statistics.
     */
    public static boolean totalNetworkStatisticsReported(Version version) {
        return supportedInConfig(ConfigValues.TotalNetworkStatisticsReported, version);
    }

    /**
     * @param version
     *            Compatibility version to check for.
     * @return <code>true</code> iff VDSM doesn't depend on ifcfg files for reporting.
     */
    public static boolean cfgEntriesDeprecated(Version version) {
        return supportedInConfig(ConfigValues.CfgEntriesDeprecated, version);
    }

    public static boolean hostNetworkQos(Version version) {
        return supportedInConfig(ConfigValues.HostNetworkQosSupported, version);
    }

    public static boolean vmStatsEvents(Version version) {
        return supportedInConfig(ConfigValues.VmStatsEventsSupported, version);
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

    /**
     * @param version
     *          Compatibility version to check for.
     * @return  <code>true</code> if SPICE file transfer toggle is supported for the given version.
     */
    public static boolean isSpiceFileTransferToggleSupported(Version version) {
        return supportedInConfig(ConfigValues.SpiceFileTransferToggleSupported, version);
    }

    /**
     * @param version
     *          Compatibility version to check for.
     * @return  <code>true</code> if IO Threads are supported for the given version.
     */
    public static boolean isIoThreadsSupported(Version version) {
        return supportedInConfig(ConfigValues.IoThreadsSupported, version);
    }

    /**
     * @param version
     *          Compatibility version to check for.
     * @return  <code>true</code> if Virtio Serial Console is supported for the given version.
     */
    public static boolean virtioSerialConsole(Version version) {
        return supportedInConfig(ConfigValues.VirtioSerialConsoleSupported, version);
    }

    /**
     * @param version
     *            Compatibility version to check for.
     * @return <code>true</code> if events are supported for the given version.
     */
    public static boolean events(Version version) {
        return supportedInConfig(ConfigValues.EventsSupported, version);
    }

    /**
     * @param version
     *            Compatibility version to check for.
     * @return <code>true</code> if forcing convergence on migration is supported for the given version
     */
    public static boolean autoConvergence(Version version) {
        return supportedInConfig(ConfigValues.AutoConvergenceSupported, version);
    }

    /**
     * @param version
     *            Compatibility version to check for.
     * @return <code>true</code> if compressing memory pages on migration is supported for the given version
     */
    public static boolean migrationCompression(Version version) {
        return supportedInConfig(ConfigValues.MigrationCompressionSupported, version);
    }

    /**
     * Determines whether engine should represent graphics framebuffer as a generic device when sending VM to VDSM.
     * @param version - compatibility version to check for
     * @return
     *   <code>true</code> - graphics will be send to VDSM as a part of devices structure,
     *   <code>false</code> - grapihcs will be send to VDSM the old way (top level VM structure).
     */
    public static boolean graphicsDeviceEnabled(Version version) {
        return supportedInConfig(ConfigValues.GraphicsDeviceEnabled, version);
    }

    /**
     * @param version
     *            Compatibility version to check for.
     * @return <code>true</code> if pass through of host devices is supported for the given version
     */
    public static boolean hostDevicePassthrough(Version version) {
        return supportedInConfig(ConfigValues.HostDevicePassthroughSupported, version);
    }

    /**
     * @param version - compatibility version to check for
     * @return <code>true</code> if the Cinder provider is supported for the specified datacenter
     */
    public static boolean cinderProviderSupported(Version version) {
        return supportedInConfig(ConfigValues.CinderProviderSupported, version);
    }

    /**
     * @param version - compatibility version to check for
     * @return <code>true</code> if the Cinder provider is supported for the specified datacenter
     */
    public static boolean glusterVolumeInfoSupported(Version version) {
        return supportedInConfig(ConfigValues.GlusterVolumeInfoSupported, version);
    }

    /**
     * @param version
     *            Compatibility version to check for.
     * @return <code>true</code> if SR-IOV network interfaces are supported for the given version
     */
    public static boolean sriov(Version version) {
        return supportedInConfig(ConfigValues.NetworkSriovSupported, version);
    }

    /**
     * @param version
     *            Compatibility version to check for.
     * @return <code>true</code> if migraton downtime supported
     */
    public static boolean migrateDowntime(Version version) {
        return supportedInConfig(ConfigValues.MigrateDowntimeSupported, version);
    }

    /**
     * @param version
     *            Compatibility version to check for.
     * @return <code>true</code> if live storage migration between different storage types is supported for given version.
     */
    public static boolean liveStorageMigrationBetweenDifferentStorageTypesSupported(Version version) {
        return supportedInConfig(ConfigValues.LiveStorageMigrationBetweenDifferentStorageTypes, version);
    }

    /**
     * @param version
     *            Compatibility version to check for.
     * @return <code>true</code> if data center without spm is supported for the given version
     */
    public static boolean dataCenterWithoutSpm(Version version) {
        return supportedInConfig(ConfigValues.DataCenterWithoutSpm, version);
    }

    /**
     * @param version
     *            Compatibility version to check for.
     * @return <code>true</code> if Refresh LUN is supported for the given version
     */
    public static boolean refreshLunSupported(Version version) {
        return supportedInConfig(ConfigValues.RefreshLunSupported, version);
    }

    /**
     * @param version Compatibility version to check for.
     * @return <code>true</code> if a host of the given version supports changing network under a bridge in use.
     */
    public static boolean changeNetworkUsedByVmSupported(Version version) {
        return supportedInConfig(ConfigValues.ChangeNetworkUnderBridgeInUseSupported, version);
    }

    /**
     * @param version
     *            Compatibility version to check for.
     * @return <code>true</code> if trunk and VLAN tagged VM network is supported on a single NIC.
     */
    public static boolean networkExclusivenessPermissiveValidation(Version version) {
        return supportedInConfig(ConfigValues.NetworkExclusivenessPermissiveValidation, version);
    }

    /**
     * @param version
     *            Compatibility version to check for.
     * @return <code>true</code> if trunk and VLAN tagged VM network is supported on a single NIC.
     */
    public static boolean getDeviceListWithoutStatusSupported(Version version) {
        return supportedInConfig(ConfigValues.GetDeviceListWithoutStatusSupported, version);
    }

    /**
     * @param version
     *            Compatibility version to check for.
     * @return <code>true</code> if initial size for sparse disk is supported.
     */
    public static boolean initialSizeSparseDiskSupported(Version version) {
        return supportedInConfig(ConfigValues.InitialSizeSparseDiskSupported, version);
    }

    /**
     * @param version
     *            Compatibility version to check for.
     * @return <code>true</code>  if import VM from VMware is supported for the given version
     */
    public static boolean importVmFromExternalProvider(Version version) {
        return supportedInConfig(ConfigValues.ImportVmFromExternalProviderSupported, version);
    }

    /**
     * @param version
     *            Compatibility version to check for.
     * @return <code>true</code> the VM can have more than one graphics (e.g. SPICE and VNC) in this version
     */
    public static boolean multipleGraphicsSupported(Version version) {
        return supportedInConfig(ConfigValues.MultipleGraphicsSupported, version);
    }

    /**
     * @param version
     *            Compatibility version to check for.
     * @return <code>true</code> hotplug/unplug of VFs is supported in this version
     */
    public static boolean sriovHotPlugSupported(Version version) {
        return supportedInConfig(ConfigValues.SriovHotPlugSupported, version);
    }

    /**
     *
     * @param version Compatibility version to check for.
     * @return <code>true</code> iff migration policies are supported in <code>version</code>
     */
    public static boolean migrationPoliciesSupported(Version version) {
        return supportedInConfig(ConfigValues.MigrationPoliciesSupported, version);
    }
}
