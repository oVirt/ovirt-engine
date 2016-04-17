package org.ovirt.engine.core.common.queries;

public enum ConfigurationValues {
    MaxNumOfVmCpus(ConfigAuthType.User),
    MaxNumOfVmSockets(ConfigAuthType.User),
    MaxNumOfCpuPerSocket(ConfigAuthType.User),
    MaxNumOfThreadsPerCpu(ConfigAuthType.User),
    VdcVersion(ConfigAuthType.User),
    MaxVmsInPool(ConfigAuthType.User),
    MaxVdsMemOverCommit(ConfigAuthType.User),
    MaxVdsMemOverCommitForServers(ConfigAuthType.User),
    ValidNumOfMonitors(ConfigAuthType.User),
    SpiceProxyDefault(ConfigAuthType.User),
    RemapCtrlAltDelDefault(ConfigAuthType.User),
    EnableDeprecatedClientModeSpicePlugin(ConfigAuthType.User),
    ClientModeSpiceDefault(ConfigAuthType.User),
    ClientModeVncDefault(ConfigAuthType.User),
    ClientModeRdpDefault(ConfigAuthType.User),
    UseFqdnForRdpIfAvailable(ConfigAuthType.User),
    WebSocketProxy(ConfigAuthType.User),
    SpiceUsbAutoShare(ConfigAuthType.User),
    FullScreenWebadminDefault(ConfigAuthType.User),
    FullScreenUserportalBasicDefault(ConfigAuthType.User),
    FullScreenUserportalExtendedDefault(ConfigAuthType.User),
    FenceProxyDefaultPreferences,
    SearchResultsLimit(ConfigAuthType.User),
    MaxBlockDiskSize(ConfigAuthType.User),
    VmPriorityMaxValue(ConfigAuthType.User),
    WarningLowSpaceIndicator(ConfigAuthType.User),
    CriticalSpaceActionBlocker(ConfigAuthType.User),
    StorageDomainNameSizeLimit(ConfigAuthType.User),
    StoragePoolNameSizeLimit(ConfigAuthType.User),
    UserDefinedVMProperties(ConfigAuthType.User),
    PredefinedVMProperties(ConfigAuthType.User),
    VdsFenceOptionTypes,
    VdsFenceOptionMapping,
    VdsFenceType,
    SupportedClusterLevels(ConfigAuthType.User),
    ProductRPMVersion(ConfigAuthType.User),
    RhevhLocalFSPath,
    HotPlugCpuSupported(ConfigAuthType.User),
    ApplicationMode(ConfigAuthType.User),
    PopulateDirectLUNDiskDescriptionWithLUNId,
    WANDisableEffects(ConfigAuthType.User),
    WANColorDepth(ConfigAuthType.User),
    NetworkConnectivityCheckTimeoutInSeconds,
    AllowClusterWithVirtGlusterEnabled,
    GlusterVolumeOptionGroupVirtValue,
    GlusterVolumeOptionOwnerUserVirtValue,
    GlusterVolumeOptionOwnerGroupVirtValue,
    GlusterDefaultBrickMountPoint,
    GlusterMetaVolumeName,
    CpuPinMigrationEnabled,
    VncKeyboardLayout(ConfigAuthType.User),
    VncKeyboardLayoutValidValues(ConfigAuthType.User),
    PreDefinedNetworkCustomProperties,
    UserDefinedNetworkCustomProperties,
    MaxAverageNetworkQoSValue,
    MaxPeakNetworkQoSValue,
    MaxBurstNetworkQoSValue,
    MaxHostNetworkQosShares,
    QoSInboundAverageDefaultValue,
    QoSInboundPeakDefaultValue,
    QoSInboundBurstDefaultValue,
    QoSOutboundAverageDefaultValue,
    QoSOutboundPeakDefaultValue,
    QoSOutboundBurstDefaultValue,
    MaxVmNameLength(ConfigAuthType.User),
    MaxVmNameLengthSysprep(ConfigAuthType.User),
    DefaultGeneralTimeZone,
    DefaultWindowsTimeZone,
    SpeedOptimizationSchedulingThreshold,
    SchedulerAllowOverBooking,
    SchedulerOverBookingThreshold,
    UserSessionTimeOutInterval(ConfigAuthType.User),
    DefaultMaximumMigrationDowntime,
    ClusterRequiredRngSourcesDefault(ConfigAuthType.User),
    DefaultMTU,
    MaxThroughputUpperBoundQosValue,
    MaxReadThroughputUpperBoundQosValue,
    MaxWriteThroughputUpperBoundQosValue,
    MaxIopsUpperBoundQosValue,
    MaxReadIopsUpperBoundQosValue,
    MaxWriteIopsUpperBoundQosValue,
    MaxCpuLimitQosValue,
    CORSSupport,
    CORSAllowedOrigins,
    HostDevicePassthroughCapabilities,
    MaxIoThreadsPerVm(ConfigAuthType.User),
    DisplayUncaughtUIExceptions,
    MigrationPoliciesSupported(ConfigAuthType.User),
    UploadImageUiInactivityTimeoutInSeconds(ConfigAuthType.User),
    UploadImageChunkSizeKB,
    UploadImageXhrTimeoutInSeconds,
    UploadImageXhrRetryIntervalInSeconds,
    UploadImageXhrMaxRetries,
    MigrationPolicies,
    AdPartnerMacSupported,
    OvsSupported;

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
