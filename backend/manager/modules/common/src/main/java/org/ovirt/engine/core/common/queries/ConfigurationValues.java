package org.ovirt.engine.core.common.queries;

public enum ConfigurationValues {
    MaxNumOfVmCpus,
    MaxNumOfVmSockets,
    MaxNumOfCpuPerSocket,
    VirtualMachineDomainName,
    VdcVersion,
    // GetAllAdDomains,
    SSLEnabled,
    CipherSuite,
    VmPoolLeaseDays,
    VmPoolLeaseStartTime,
    VmPoolLeaseEndTime,
    MaxVmsInPool,
    MaxVdsMemOverCommit,
    MaxVdsMemOverCommitForServers,
    AdUserName,
    // TODO remove remarks and AdUserPassword completely in version 3.1.
    // AdUserPassword field format has been changed.
    // AdUserPassword,
    LocalAdminPassword,
    ValidNumOfMonitors,
    EnableUSBAsDefault,
    SpiceSecureChannels,
    SpiceReleaseCursorKeys,
    SpiceToggleFullScreenKeys,
    HighUtilizationForEvenlyDistribute,
    RDPLoginWithFQN,
    SpiceUsbAutoShare,
    ExportDefaultPath,
    ImportDefaultPath,
    ComputerADPaths,
    PowerClientGUI,
    VdsSelectionAlgorithm,
    LowUtilizationForEvenlyDistribute,
    LowUtilizationForPowerSave,
    HighUtilizationForPowerSave,
    CpuOverCommitDurationMinutes,
    InstallVds,
    IrsClusterStatusRefreshRate,
    AsyncTaskPollingRate,
    VdsFenceType,
    VdsFenceOptions,
    VdsFenceOptionMapping,
    VcpuConsumptionPercentage,
    CertificateFingerPrint,
    SearchResultsLimit,
    MaxDiskSize,
    RedirectServletReportsPage,
    RedirectServletReportsPageError,
    EnableSpiceRootCertificateValidation,
    VMMinMemorySizeInMB,
    VM32BitMaxMemorySizeInMB,
    VM64BitMaxMemorySizeInMB,
    VmPriorityMaxValue,
    StorageDomainNameSizeLimit,
    StoragePoolNameSizeLimit,
    SANWipeAfterDelete,
    AuthenticationMethod,
    LocalStorageEnabled,
    UserDefinedVMProperties,
    PredefinedVMProperties,
    SupportCustomProperties,
    VdsFenceOptionTypes,
    ServerCPUList,
    SupportedClusterLevels,
    ProductRPMVersion,
    RhevhLocalFSPath,
    CustomPublicConfig_AppsWebSite,
    DocsURL,
    HotPlugEnabled;

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
