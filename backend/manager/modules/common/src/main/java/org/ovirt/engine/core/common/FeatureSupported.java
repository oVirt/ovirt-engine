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
        return Boolean.parseBoolean(
                ((Map<String, String>) Config.<Map>getValue(feature, version.getValue())).get(arch.name()));
    }

    /**
     * @param version
     *            Compatibility version to check for.
     * @return <code>true</code> if network linking is supported for the version, <code>false</code> if it's not.
     */
    public static boolean networkLinking(Version version) {
        return supportedInConfig(ConfigValues.NetworkLinkingSupported, version);
    }

    /**
     * @param version
     *            Compatibility version to check for.
     * @return <code>true</code> if MTU specification is supported for the version, <code>false</code> if it's not.
     */
    public static boolean mtuSpecification(Version version) {
        return supportedInConfig(ConfigValues.MTUOverrideSupported, version);
    }

    /**
     * @param version
     *            Compatibility version to check for.
     * @return <code>true</code> if port mirroring is supported for the version, <code>false</code> if it's not.
     */
    public static boolean portMirroring(Version version) {
        return supportedInConfig(ConfigValues.PortMirroringSupported, version);
    }

    /**
     * @param version
     *            Compatibility version to check for.
     * @return <code>true</code> if non-VM network is supported for the version, <code>false</code> if it's not.
     */
    public static boolean nonVmNetwork(Version version) {
        return supportedInConfig(ConfigValues.NonVmNetworkSupported, version);
    }

    /**
     * @param version
     *            Compatibility version to check for.
     * @return <code>true</code> if VM SLA policy is supported for the version,
     *         <code>false</code> if it's not.
     */
    public static boolean vmSlaPolicy(Version version) {
        return supportedInConfig(ConfigValues.VmSlaPolicySupported, version);
    }

    /**
     * @param version
     *            Compatibility version to check for.
     * @return <code>true</code> if bridges element reported by VDSM is supported for the version, <code>false</code> if
     *         it's not.
     */
    public static boolean bridgesReportByVdsm(Version version) {
        return supportedInConfig(ConfigValues.SupportBridgesReportByVDSM, version);
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
     * @return <code>true</code> if anti MAC spoofing is supported for the version, <code>false</code> if it's not.
     */
    public static boolean antiMacSpoofing(Version version) {
        return supportedInConfig(ConfigValues.MacAntiSpoofingFilterRulesSupported, version);
    }

    /**
     * @param version
     *            Compatibility version to check for.
     * @return <code>true</code> if get hardware information is supported for the version, <code>false</code> if it's not.
     */
    public static boolean hardwareInfo(Version version) {
        return supportedInConfig(ConfigValues.HardwareInfoEnabled, version);
    }

    /**
     * @param version
     *            Compatibility version to check for.
     * @return <code>true</code> if tunnel migration is supported for the version, <code>false</code> if it's not.
     */
    public static boolean tunnelMigration(Version version) {
        return supportedInConfig(ConfigValues.TunnelMigrationEnabled, version);
    }

    /**
     * @param version
     *            Compatibility version to check for.
     * @return <code>true</code> if hot plug is supported for the version, <code>false</code> if it's not.
     */
    public static boolean hotPlug(Version version) {
        return supportedInConfig(ConfigValues.HotPlugEnabled, version);
    }

    /**
     * @param version
     *            Compatibility version to check for.
     * @return <code>true</code> if migration network is supported for the version, <code>false</code> if it's not.
     */
    public static boolean migrationNetwork(Version version) {
        return supportedInConfig(ConfigValues.MigrationNetworkEnabled, version);
    }

    /**
     * @param version
     *            Compatibility version to check for.
     * @return {@code true} if device custom properties are supported for the version, otherwise {@code false}.
     */
    public static boolean deviceCustomProperties(Version version) {
        return supportedInConfig(ConfigValues.SupportCustomDeviceProperties, version);
    }

    public static boolean networkCustomProperties(Version version) {
        return supportedInConfig(ConfigValues.NetworkCustomPropertiesSupported, version);
    }

    /**
     * @param version
     *            Compatibility version to check for.
     * @return <code>true</code> if multiple gateways is supported for the version, <code>false</code> if it's not.
     */
    public static boolean multipleGatewaysSupported(Version version) {
        return supportedInConfig(ConfigValues.MultipleGatewaysSupported, version);
    }

    /**
     * @param version
     *            Compatibility version to check for.
     * @return <code>true</code> if memory snapshot is supported for the version, <code>false</code> if it's not.
     */
    public static boolean memorySnapshot(Version version) {
        return supportedInConfig(ConfigValues.MemorySnapshotSupported, version);
    }

    /**
     * @param version
     *            Compatibility version to check for.
     * @return <code>true</code> if Virtio-SCSI is supported for the cluster version, <code>false</code> if it's not.
     */
    public static boolean virtIoScsi(Version version) {
        return supportedInConfig(ConfigValues.VirtIoScsiEnabled, version);
    }

    /**
     * @param version
     *            Compatibility version to check for.
     * @return <code>true</code> if Management Network normalization is supported for the cluster version.
     */
    public static boolean setupManagementNetwork(Version version) {
        return supportedInConfig(ConfigValues.NormalizedMgmtNetworkEnabled, version);
    }

    /**
     *
     * @param version
     *            Compatibility version to check for.
     * @return <code>true</code> if Single Qxl video display is supported for the cluster version.
     */
    public static boolean singleQxlPci(Version version) {
        return supportedInConfig(ConfigValues.SingleQxlPciEnabled, version);
    }

    /**
     * @param version
     *            Compatibility version to check for.
     * @return <code>true</code> iff MoM Policy on host is supported for the cluster version.
     */
    public static boolean momPolicyOnHost(Version version) {
        return supportedInConfig(ConfigValues.MomPoliciesOnHostSupported, version);
    }

    /**
     * @param version
     *            Compatibility version to check for.
     * @return <code>true</code> iff Network QoS is supported for the cluster version.
     */
    public static boolean networkQoS(Version version) {
        return supportedInConfig(ConfigValues.NetworkQosSupported, version);
    }

     /**
     * @param version
     *            Compatibility version to check for.
     * @return <code>true</code> iff Storage QoS is supported for the cluster version.
     */
    public static boolean storageQoS(Version version) {
        return supportedInConfig(ConfigValues.StorageQosSupported, version);
    }

    /**
     * @param version
     *            Compatibility version to check for.
     * @return <code>true</code> if Cpu QoS is supported for the cluster version.
     */
    public static boolean cpuQoS(Version version) {
        return supportedInConfig(ConfigValues.CpuQosSupported, version);
    }

    public static boolean hostNetworkQos(Version version) {
        return supportedInConfig(ConfigValues.HostNetworkQosSupported, version);
    }

    /**
     * @param version
     *            Compatibility version to check for.
     * @return <code>true</code> if cloud-init is supported for the cluster version.
     */
    public static boolean cloudInit(Version version) {
        return supportedInConfig(ConfigValues.CloudInitSupported, version);
    }

   /**
    * @param version
    *            Compatibility version to check for.
    * @return <code>true</code> if hot plug disk snapshot is supported for the given version.
    */
    public static boolean hotPlugDiskSnapshot(Version version) {
        return supportedInConfig(ConfigValues.HotPlugDiskSnapshotSupported, version);
    }

    public static boolean hotPlugCpu(Version version, ArchitectureType arch) {
        return supportedInConfig(ConfigValues.HotPlugCpuSupported, version, arch);
    }

    public static boolean hotUnplugCpu(Version version, ArchitectureType arch) {
        return supportedInConfig(ConfigValues.HotUnplugCpuSupported, version, arch);
    }

    /**
     * @param version
     *            Compatibility version to check for.
     * @return <code>true</code> if get file stats is supported for the given version.
     */
    public static boolean getFileStats(Version version) {
        return supportedInConfig(ConfigValues.GetFileStats, version);
    }

    /**
     * @param version
     *            Compatibility version to check for.
     * @return <code>true</code> if default route is supported for the given version.
     */
    public static boolean defaultRoute(Version version) {
        return supportedInConfig(ConfigValues.DefaultRouteSupported, version);
    }

    /**
     * @param version
     *            Compatibility version to check for.
     * @return <code>true</code> if importing a glance image as template is supported for the given version.
     */
    public static boolean importGlanceImageAsTemplate(Version version) {
        return supportedInConfig(ConfigValues.ImportGlanceImageAsTemplate, version);
    }

   /**
     * @param version
     * Checks if migration is supported by the given CPU architecture
     *
     * @param architecture
     *            The CPU architecture
     * @param version
     *            Compatibility version to check for.
     * @return
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
     * @return
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
     * @return
     */
    public static boolean isSuspendSupportedByArchitecture(ArchitectureType architecture, Version version) {
        return supportedInConfig(ConfigValues.IsSuspendSupported, version, architecture);
    }

    /**
     * @param version
     *            Compatibility version to check for.
     * @return <code>true</code> if get file stats is supported for the given version.
     */
    public static boolean serialNumberPolicy(Version version) {
        return supportedInConfig(ConfigValues.SerialNumberPolicySupported, version);
    }

    /**
     * @param version
     *            Compatibility version to check for.
     * @return <code>true</code> if hot plug disk snapshot is supported for the given version.
     */
    public static boolean ovfStoreOnAnyDomain(Version version) {
        return supportedInConfig(ConfigValues.OvfStoreOnAnyDomain, version);
    }

    /**
     * @param version
     *            Compatibility version to check for.
     * @return <code>true</code> if import of Data Storage Domain is supported.
     */
    public static boolean importDataStorageDomain(Version version) {
        return supportedInConfig(ConfigValues.ImportDataStorageDomain, version);
    }

    /**
     * @param version
     *          Compatibility version to check for.
     * @return  <code>true</code> if iSCSI multipathing is supported for the given version.
     */
    public static boolean isIscsiMultipathingSupported(Version version) {
        return supportedInConfig(ConfigValues.IscsiMultipathingSupported, version);
    }

    /**
     *            Compatibility version to check for.
     * @return <code>true</code> if mixed domain type is supported for the given version.
     */
    public static boolean mixedDomainTypesOnDataCenter(Version version) {
        return supportedInConfig(ConfigValues.MixedDomainTypesInDataCenter, version);
    }

    /**
     * @param version
     *            Compatibility version to check for.
     * @return <code>true</code> if get boot menu is supported for the given version.
     */
    public static boolean bootMenu(Version version) {
        return supportedInConfig(ConfigValues.BootMenuSupported, version);
    }

    /**
     * @param version
     *            Compatibility version to check for.
     * @return <code>true</code> if VirtIo RNG device is supported for the version, <code>false</code> if it's not.
     */
    public static boolean virtIoRngSupported(Version version) {
        return supportedInConfig(ConfigValues.VirtIoRngDeviceSupported, version);
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
     * @return  <code>true</code> if SPICE copy-paste toggle is supported for the given version.
     */
    public static boolean isSpiceCopyPasteToggleSupported(Version version) {
        return supportedInConfig(ConfigValues.SpiceCopyPasteToggleSupported, version);
    }

    /**
     * @param version
     *            Compatibility version to check for.
     * @return <code>true</code> if the pool memory backend is supported for the given version.
     */
    public static boolean storagePoolMemoryBackend(Version version) {
        return supportedInConfig(ConfigValues.StoragePoolMemoryBackend, version);
    }

    /**
     * @param version
     *            Compatibility version to check for.
     * @return <code>true</code> if get live merge is supported for the given version.
     */
    public static boolean liveMerge(Version version) {
        return supportedInConfig(ConfigValues.LiveMergeSupported, version);

    }

    /**
     * @param version
     *            Compatibility version to check for.
     * @return <code>true</code> if reported disk logical names is supported for the given version.
     */
    public static boolean reportedDisksLogicalNames(Version version) {
        return supportedInConfig(ConfigValues.ReportedDisksLogicalNames, version);

    }

    /**
     * @param version
     *            Compatibility version to check for.
     * @return <code>true</code> if skip fencing when host connected to SD feature is supported for the given version.
     */
    public static boolean isSkipFencingIfSDActiveSupported(Version version) {
        return supportedInConfig(ConfigValues.SkipFencingIfSDActiveSupported, version);
    }

    /**
     * @param version
     *            Compatibility version to check for.
     * @return <code>true</code> if json protocol is supported for the given version.
     */
    public static boolean jsonProtocol(Version version) {
        return supportedInConfig(ConfigValues.JsonProtocolSupported, version);
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
     * @param version
     *            Compatibility version to check for.
     * @return <code>true</code> if report whether domain monitoring result is supported for the given version.
     */
    public static boolean reportWhetherDomainMonitoringResultIsActual(Version version) {
        return supportedInConfig(ConfigValues.ReportWhetherDomainMonitoringResultIsActual, version);
    }

    /**
     * @param version - compatibility version to check for
     * @return <code>true</code> if the Cinder provider is supported for the specified datacenter
     */
    public static boolean cinderProviderSupported(Version version) {
        return supportedInConfig(ConfigValues.CinderProviderSupported, version);
    }

    /**
     * @param version
     *            Compatibility version to check for.
     * @return <code>true</code> if SR-IOV network interfaces are supported for the given version
     */
    public static boolean sriov(Version version) {
        return supportedInConfig(ConfigValues.NetworkSriovSupported, version);
    }
}
