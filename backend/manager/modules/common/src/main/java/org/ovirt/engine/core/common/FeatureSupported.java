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
}
