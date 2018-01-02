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
     * Checks if High Performance VM type is supported by cluster version
     *
     * @param version
     *            Compatibility version to check for.
     */
    public static boolean isHighPerformanceTypeSupported(Version version) {
        return supportedInConfig(ConfigValues.IsHighPerformanceTypeSupported, version);
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
     *            Compatibility version to check for.
     * @return <code>true</code> if data operations by HSM are supported for the given version
     */
    public static boolean dataOperationsByHSM(Version version) {
        return supportedInConfig(ConfigValues.DataOperationsByHSM, version);
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

    public static boolean adPartenerMacSupported(Version version) {
        return supportedInConfig(ConfigValues.AdPartnerMacSupported, version);
    }

    public static boolean ipv6Supported(Version version) {
        return supportedInConfig(ConfigValues.Ipv6Supported, version);
    }

    public static boolean ovsSupported(Version version) {
        return supportedInConfig(ConfigValues.OvsSupported, version);
    }

    /**
     *
     * @param version Compatibility version to check for.
     * @return <code>true</code> iff get external VMS names only in 1st phase from external server VMS (v2v) is supported in <code>version</code>
     */
    public static boolean isGetNamesOfVmsFromExternalProviderSupported(Version version) {
        return supportedInConfig(ConfigValues.GetNamesOfVmsFromExternalProviderSupported, version);
    }

    public static boolean virtioScsiIoThread(Version version) {
        return supportedInConfig(ConfigValues.VirtIOScsiIOThread, version);
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
     * @return {@code true} if Pass Discard is supported for this version.
     */
    public static boolean passDiscardSupported(Version version) {
        return supportedInConfig(ConfigValues.PassDiscardSupported, version);
    }

    /**
     * @param version Compatibility version to check for.
     * @return {@code true} if Discard After Delete is supported for this version.
     */
    public static boolean discardAfterDeleteSupported(Version version) {
        return supportedInConfig(ConfigValues.DiscardAfterDeleteSupported, version);
    }

    /**
     * @param version Compatibility version to check for.
     * @return {@code true} if qcow compat is supported for this version.
     */
    public static boolean qcowCompatSupported(Version version) {
        return supportedInConfig(ConfigValues.QcowCompatSupported, version);
    }

    /**
     * @param version Compatibility version to check for.
     * @return {@code true} if reduce device from domain is supported for this version.
     */
    public static boolean reduceDeviceFromStorageDomain(Version version) {
        return supportedInConfig(ConfigValues.ReduceDeviceFromStorageDomain, version);
    }

    public static boolean isQemuimgCommitSupported(Version version) {
        return supportedInConfig(ConfigValues.QemuimgCommitSupported, version);
    }

    public static boolean isIpv6MigrationProperlyHandled(Version version) {
        return supportedInConfig(ConfigValues.Ipv6MigrationProperlyHandled, version);
    }

    /**
     * @param version Compatibility version to check for.
     * @return {@code true} if VM leases are supported for this version.
     */
    public static boolean isVmLeasesSupported(Version version) {
        return supportedInConfig(ConfigValues.VmLeasesSupported, version);
    }

    public static boolean isAgentChannelNamingSupported(Version version) {
        return supportedInConfig(ConfigValues.AgentChannelNamingSupported, version);
    }

    public static boolean isLegacyDisplaySupported(Version version) {
        return supportedInConfig(ConfigValues.LegacyGraphicsDisplay, version);
    }

    /**
     * @param version Compatibility version to check for.
     * @return {@code true} if VDSM trapping of guest reboot is supported.
     */
    public static boolean isDestroyOnRebootSupported(Version version) {
        return supportedInConfig(ConfigValues.DestroyOnRebootSupported, version);
    }

    /**
     * @param version Compatibility version to check for.
     * @return {@code true} if configuration of the resume behavior is supported.
     */
    public static boolean isResumeBehaviorSupported(Version version) {
        return supportedInConfig(ConfigValues.ResumeBehaviorSupported, version);
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

    public static boolean isReduceVolumeSupported(Version version) {
        return supportedInConfig(ConfigValues.ReduceVolumeSupported, version);
    }

    public static boolean isContentTypeSupported(Version version) {
        return supportedInConfig(ConfigValues.ContentType, version);
    }

    public static boolean isIsoOnDataDomainSupported(Version version) {
        return supportedInConfig(ConfigValues.IsoOnDataDomain, version);
    }

    public static boolean isDefaultRouteReportedByVdsm(Version version) {
        return supportedInConfig(ConfigValues.DefaultRouteReportedByVdsm, version);
    }

    /**
     * The use of libvirt's domain XML on the engine side.
     * Important: this is determined by the compatibility level of the cluster!
     * (rather than that of the VM)
     *
     * @param version Compatibility version <B>of the cluster</B> to check for.
     * @return {@code true} if the use of libvirt's domain XML is supported.
     */
    public static boolean isDomainXMLSupported(Version version) {
        return supportedInConfig(ConfigValues.DomainXML, version);
    }

    /**
     * @param version Compatibility version to check for.
     * @return {@code true} if getting an image ticket from vdsm is supported for this version.
     */
    public static boolean getImageTicketSupported(Version version) {
        return supportedInConfig(ConfigValues.GetImageTicketSupported, version);
    }

    /**
     * @param version Compatibility version to check for.
     * @return {@code true} if getting an LLDP information from vdsm is supported for this version.
     */
    public static boolean isLlldpInformationSupported(Version version) {
        return supportedInConfig(ConfigValues.LldpInformationSupported, version);
    }
}
