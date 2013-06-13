package org.ovirt.engine.core.common.queries;

public enum ConfigurationValues {
    MaxNumOfVmCpus(ConfigAuthType.User),
    MaxNumOfVmSockets(ConfigAuthType.User),
    MaxNumOfCpuPerSocket(ConfigAuthType.User),
    VdcVersion(ConfigAuthType.User),
    // GetAllAdDomains,
    SSLEnabled(ConfigAuthType.User),
    CipherSuite(ConfigAuthType.User),
    VmPoolLeaseDays(ConfigAuthType.User),
    VmPoolLeaseStartTime(ConfigAuthType.User),
    VmPoolLeaseEndTime(ConfigAuthType.User),
    MaxVmsInPool(ConfigAuthType.User),
    MaxVdsMemOverCommit(ConfigAuthType.User),
    MaxVdsMemOverCommitForServers(ConfigAuthType.User),
    AdUserName,
    // TODO remove remarks and AdUserPassword completely in version 3.1.
    // AdUserPassword field format has been changed.
    // AdUserPassword,
    LocalAdminPassword,
    ValidNumOfMonitors(ConfigAuthType.User),
    EnableUSBAsDefault(ConfigAuthType.User),
    SpiceSecureChannels(ConfigAuthType.User),
    SpiceReleaseCursorKeys(ConfigAuthType.User),
    SpiceToggleFullScreenKeys(ConfigAuthType.User),
    SpiceProxyDefault(ConfigAuthType.User),
    ClientModeSpiceDefault(ConfigAuthType.User),
    ClientModeRdpDefault(ConfigAuthType.User),
    WebSocketProxy(ConfigAuthType.User),
    WebSocketProxyTicketValiditySeconds(ConfigAuthType.User),
    HighUtilizationForEvenlyDistribute(ConfigAuthType.User),
    SpiceUsbAutoShare(ConfigAuthType.User),
    ImportDefaultPath,
    ComputerADPaths(ConfigAuthType.User),
    VdsSelectionAlgorithm,
    LowUtilizationForEvenlyDistribute,
    LowUtilizationForPowerSave,
    HighUtilizationForPowerSave,
    CpuOverCommitDurationMinutes,
    InstallVds,
    AsyncTaskPollingRate,
    VdsFenceType,
    VdsFenceOptionMapping,
    FenceProxyDefaultPreferences,
    VcpuConsumptionPercentage(ConfigAuthType.User),
    SearchResultsLimit(ConfigAuthType.User),
    MaxBlockDiskSize(ConfigAuthType.User),
    RedirectServletReportsPage(ConfigAuthType.User),
    EnableSpiceRootCertificateValidation(ConfigAuthType.User),
    VMMinMemorySizeInMB(ConfigAuthType.User),
    VM32BitMaxMemorySizeInMB(ConfigAuthType.User),
    VM64BitMaxMemorySizeInMB(ConfigAuthType.User),
    VmPriorityMaxValue(ConfigAuthType.User),
    StorageDomainNameSizeLimit(ConfigAuthType.User),
    StoragePoolNameSizeLimit(ConfigAuthType.User),
    SANWipeAfterDelete(ConfigAuthType.User),
    AuthenticationMethod(ConfigAuthType.User),
    UserDefinedVMProperties(ConfigAuthType.User),
    PredefinedVMProperties(ConfigAuthType.User),
    VdsFenceOptionTypes,
    ServerCPUList,
    SupportedClusterLevels(ConfigAuthType.User),
    OvfUpdateIntervalInMinutes,
    OvfItemsCountPerUpdate,
    ProductRPMVersion(ConfigAuthType.User),
    RhevhLocalFSPath,
    DocsURL(ConfigAuthType.User),
    HotPlugEnabled(ConfigAuthType.User),
    NetworkLinkingSupported(ConfigAuthType.User),
    SupportBridgesReportByVDSM(ConfigAuthType.User),
    ManagementNetwork,
    ApplicationMode(ConfigAuthType.User),
    ShareableDiskEnabled(ConfigAuthType.User),
    DirectLUNDiskEnabled(ConfigAuthType.User),
    WANDisableEffects(ConfigAuthType.User),
    WANColorDepth(ConfigAuthType.User),
    SupportForceCreateVG,
    NetworkConnectivityCheckTimeoutInSeconds,
    AllowClusterWithVirtGlusterEnabled,
    MTUOverrideSupported(ConfigAuthType.User),
    GlusterVolumeOptionGroupVirtValue,
    GlusterVolumeOptionOwnerUserVirtValue,
    GlusterVolumeOptionOwnerGroupVirtValue,
    CpuPinningEnabled,
    CpuPinMigrationEnabled,
    MigrationSupportForNativeUsb(ConfigAuthType.User),
    MigrationNetworkEnabled,
    VncKeyboardLayout(ConfigAuthType.User),
    VncKeyboardLayoutValidValues(ConfigAuthType.User),
    SupportCustomDeviceProperties(ConfigAuthType.User),
    CustomDeviceProperties(ConfigAuthType.User),
    MultipleGatewaysSupported,
    VirtIoScsiEnabled(ConfigAuthType.User),
    SshSoftFencingCommand
    ;

    public static enum ConfigAuthType {
        Admin,
        User
    }

    private ConfigAuthType authType;

    private ConfigurationValues(ConfigAuthType authType) {
        this.authType = authType;
    }

    private ConfigurationValues() {
        this(ConfigAuthType.Admin);
    }

    public ConfigAuthType getConfigAuthType() {
        return authType;
    }

    public boolean isAdmin() {
        return ConfigAuthType.Admin == authType;
    }

    public int getValue() {
        return ordinal();
    }

    public static ConfigurationValues forValue(int value) {
        return values()[value];
    }
}
