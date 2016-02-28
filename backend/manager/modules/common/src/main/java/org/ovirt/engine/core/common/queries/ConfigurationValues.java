package org.ovirt.engine.core.common.queries;

public enum ConfigurationValues {
    MaxNumOfVmCpus(ConfigAuthType.User),
    MaxNumOfVmSockets(ConfigAuthType.User),
    MaxNumOfCpuPerSocket(ConfigAuthType.User),
    MaxNumOfThreadsPerCpu(ConfigAuthType.User),
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
    ValidNumOfMonitors(ConfigAuthType.User),
    EnableUSBAsDefault(ConfigAuthType.User),
    SpiceSecureChannels(ConfigAuthType.User),
    ConsoleReleaseCursorKeys(ConfigAuthType.User),
    ConsoleToggleFullScreenKeys(ConfigAuthType.User),
    SpiceProxyDefault(ConfigAuthType.User),
    RemoteViewerSupportedVersions(ConfigAuthType.User),
    RemoteViewerNewerVersionUrl(ConfigAuthType.User),
    RemapCtrlAltDelDefault(ConfigAuthType.User),
    ClientModeSpiceDefault(ConfigAuthType.User),
    ClientModeVncDefault(ConfigAuthType.User),
    ClientModeRdpDefault(ConfigAuthType.User),
    UseFqdnForRdpIfAvailable(ConfigAuthType.User),
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
    FenceProxyDefaultPreferences,
    VcpuConsumptionPercentage(ConfigAuthType.User),
    SearchResultsLimit(ConfigAuthType.User),
    MaxBlockDiskSize(ConfigAuthType.User),
    EnableSpiceRootCertificateValidation(ConfigAuthType.User),
    VMMinMemorySizeInMB(ConfigAuthType.User),
    VM32BitMaxMemorySizeInMB(ConfigAuthType.User),
    VM64BitMaxMemorySizeInMB(ConfigAuthType.User),
    VmPriorityMaxValue(ConfigAuthType.User),
    WarningLowSpaceIndicator(ConfigAuthType.User),
    CriticalSpaceActionBlocker(ConfigAuthType.User),
    StorageDomainNameSizeLimit(ConfigAuthType.User),
    HostedEngineStorageDomainName,
    StoragePoolNameSizeLimit(ConfigAuthType.User),
    SANWipeAfterDelete(ConfigAuthType.User),
    UserDefinedVMProperties(ConfigAuthType.User),
    PredefinedVMProperties(ConfigAuthType.User),
    VdsFenceOptionTypes,
    FenceAgentMapping,
    FenceAgentDefaultParams,
    FenceAgentDefaultParamsForPPC,
    VdsFenceOptionMapping,
    VdsFenceType,
    SupportedClusterLevels(ConfigAuthType.User),
    OvfUpdateIntervalInMinutes,
    OvfItemsCountPerUpdate,
    ProductRPMVersion(ConfigAuthType.User),
    RhevhLocalFSPath,
    HotPlugCpuSupported(ConfigAuthType.User),
    IoThreadsSupported(ConfigAuthType.User),
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
    CustomDeviceProperties(ConfigAuthType.User),
    PreDefinedNetworkCustomProperties,
    UserDefinedNetworkCustomProperties,
    SshSoftFencingCommand,
    HostNetworkQosSupported,
    MaxAverageNetworkQoSValue,
    MaxPeakNetworkQoSValue,
    MaxBurstNetworkQoSValue,
    MaxHostNetworkQosShares,
    UserMessageOfTheDay(ConfigAuthType.User),
    QoSInboundAverageDefaultValue,
    QoSInboundPeakDefaultValue,
    QoSInboundBurstDefaultValue,
    QoSOutboundAverageDefaultValue,
    QoSOutboundPeakDefaultValue,
    QoSOutboundBurstDefaultValue,
    MaxVmNameLength(ConfigAuthType.User),
    MaxVmNameLengthSysprep(ConfigAuthType.User),
    AttestationServer,
    DefaultGeneralTimeZone,
    DefaultWindowsTimeZone,
    SpeedOptimizationSchedulingThreshold,
    SchedulerAllowOverBooking,
    SchedulerOverBookingThreshold,
    UserSessionTimeOutInterval(ConfigAuthType.User),
    DefaultMaximumMigrationDowntime,
    IsMigrationSupported(ConfigAuthType.User),
    IsMemorySnapshotSupported(ConfigAuthType.User),
    IsSuspendSupported(ConfigAuthType.User),
    ClusterRequiredRngSourcesDefault(ConfigAuthType.User),
    SpiceFileTransferToggleSupported(ConfigAuthType.User),
    DefaultMTU,
    MaxThroughputUpperBoundQosValue,
    MaxReadThroughputUpperBoundQosValue,
    MaxWriteThroughputUpperBoundQosValue,
    MaxIopsUpperBoundQosValue,
    MaxReadIopsUpperBoundQosValue,
    MaxWriteIopsUpperBoundQosValue,
    MaxCpuLimitQosValue,
    AutoConvergenceSupported(ConfigAuthType.User),
    MigrationCompressionSupported(ConfigAuthType.User),
    DefaultAutoConvergence,
    DefaultMigrationCompression,
    CORSSupport,
    CORSAllowedOrigins,
    CinderProviderSupported,
    NetworkSriovSupported(ConfigAuthType.User),
    NetworkExclusivenessPermissiveValidation,
    HostDevicePassthroughCapabilities,
    LiveStorageMigrationBetweenDifferentStorageTypes,
    MaxIoThreadsPerVm(ConfigAuthType.User),
    MultipleGraphicsSupported(ConfigAuthType.User),
    DisplayUncaughtUIExceptions,
    DisplaySupportedBrowserWarning,
    RefreshLunSupported,
    UploadImageUiInactivityTimeoutInSeconds(ConfigAuthType.User),
    UploadImageChunkSizeKB,
    UploadImageXhrTimeoutInSeconds,
    UploadImageXhrRetryIntervalInSeconds,
    UploadImageXhrMaxRetries;

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
