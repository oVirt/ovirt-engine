package org.ovirt.engine.core.common.config;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.EngineWorkingMode;
import org.ovirt.engine.core.common.businessentities.SerialNumberPolicy;
import org.ovirt.engine.core.compat.Version;

public enum ConfigValues {
    @TypeConverterAttribute(String.class)
    DBEngine,

    /**
     * Timeout in seconds for the completion of calls to VDSM. It should
     * be quite large as some host operations can take more than 3
     * minutes to complete.
     */
    @TypeConverterAttribute(Integer.class)
    vdsTimeout,

    /**
     * The number of times to retry host operations when there are IO errors.
     */
    @TypeConverterAttribute(Integer.class)
    vdsRetries,

    /**
     * Timeout in seconds how often we should receive heart-beat.
     */
    @TypeConverterAttribute(Integer.class)
    vdsHeartbeatInSeconds,

    /**
     * Timeout for establishment of connections with hosts. This should be quite
     * small, a few seconds at most, as it the TCP handshake with hosts should
     * be very quick in most networks.
     */
    @TypeConverterAttribute(Integer.class)
    vdsConnectionTimeout,

    /**
     * Maximum concurrent http(s) connections to hosts. A small number of connections should suffice for most
     * environments. When a lot of storage actions are performed, this value can be increased for more VDS command
     * throughput.
     */
    @TypeConverterAttribute(Integer.class)
    VdsMaxConnectionsPerHost,

    /**
     * Maximum number of connections allowed.
     */
    @TypeConverterAttribute(Integer.class)
    MaxTotalConnections,

    @TypeConverterAttribute(Long.class)
    VdsRefreshRate,
    @TypeConverterAttribute(Long.class)
    AsyncTaskPollingRate,

    @TypeConverterAttribute(Long.class)
    AsyncCommandPollingLoopInSeconds,

    @TypeConverterAttribute(Long.class)
    AsyncCommandPollingRateInSeconds,

    /**
     * The rate (in seconds) to refresh the cache that holds the asynchronous tasks' statuses.
     */
    @TypeConverterAttribute(Long.class)
    AsyncTaskStatusCacheRefreshRateInSeconds,

    /**
     * The period of time (in minutes) to hold the asynchronous tasks' statuses in the asynchronous tasks cache.
     */
    @TypeConverterAttribute(Long.class)
    AsyncTaskStatusCachingTimeInMinutes,
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    AsyncTaskZombieTaskLifeInMinutes,
    @TypeConverterAttribute(Date.class)
    AuditLogCleanupTime,
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    AuditLogAgingThreshold,
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    CoCoLifeInMinutes,
    @TypeConverterAttribute(Integer.class)
    CoCoWaitForEventInMinutes,
    @TypeConverterAttribute(Date.class)
    CommandEntityCleanupTime,
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    CommandEntityAgingThreshold,
    @Reloadable
    @TypeConverterAttribute(String.class)
    VdsFenceType(ClientAccessLevel.Admin),
    @Reloadable
    @TypeConverterAttribute(String.class)
    CustomVdsFenceType,
    @Reloadable
    @TypeConverterAttribute(String.class)
    VdsFenceOptionMapping,
    @Reloadable
    @TypeConverterAttribute(String.class)
    CustomVdsFenceOptionMapping,
    @Reloadable
    @TypeConverterAttribute(String.class)
    VdsFenceOptionTypes,
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    FenceStopStatusRetries,
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    FenceStopStatusDelayBetweenRetriesInSec,
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    FenceQuietTimeBetweenOperationsInSec,
    @Reloadable
    @TypeConverterAttribute(String.class)
    FenceProxyDefaultPreferences(ClientAccessLevel.Admin),
    @Reloadable
    @TypeConverterAttribute(String.class)
    oVirtUploadPath,
    @Reloadable
    @TypeConverterAttribute(String.class)
    oVirtISOsRepositoryPath,
    @Reloadable
    @TypeConverterAttribute(String.class)
    oVirtUpgradeScriptName,
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    VdsCertificateValidityInYears,
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    SearchResultsLimit(ClientAccessLevel.User),
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    VDSAttemptsToResetCount,
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    TimeoutToResetVdsInSeconds,
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    //This value is in percents
    WarningLowSpaceIndicator(ClientAccessLevel.User),
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    //This value is in GB
    CriticalSpaceActionBlocker(ClientAccessLevel.User),
    @Reloadable
    @TypeConverterAttribute(String.class)
    VdcVersion(ClientAccessLevel.User),
    @Reloadable
    @TypeConverterAttribute(Boolean.class)
    SSLEnabled,
    @Reloadable
    @TypeConverterAttribute(String.class)
    CipherSuite,
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    StoragePoolNameSizeLimit(ClientAccessLevel.User),
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    StorageDomainNameSizeLimit(ClientAccessLevel.User),
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    NumberOfFailedRunsOnVds,
    @TypeConverterAttribute(Long.class)
    TimeToReduceFailedRunOnVdsInMinutes,
    /**
     * In default rerun Vm on all Available desktops
     */
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    MaxRerunVmOnVdsCount,
    @TypeConverterAttribute(Integer.class)
    MaxVmsInPool(ClientAccessLevel.User),
    @Reloadable
    @TypeConverterAttribute(List.class)
    @OptionBehaviourAttribute(behaviour = OptionBehaviour.CommaSeparatedStringArray)
    ValidNumOfMonitors(ClientAccessLevel.User),
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    MaxNumOfVmCpus(ClientAccessLevel.User),
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    MaxNumOfVmSockets(ClientAccessLevel.User),
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    MaxNumOfCpuPerSocket(ClientAccessLevel.User),
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    MaxNumOfThreadsPerCpu(ClientAccessLevel.User),
    @TypeConverterAttribute(Integer.class)
    NumberVmRefreshesBeforeSave,
    @TypeConverterAttribute(Integer.class)
    NumberVdsRefreshesBeforeTryToStartUnknownVms,
    @TypeConverterAttribute(Integer.class)
    NumberVdsRefreshesBeforeRetryToStartUnknownVms,
    @TypeConverterAttribute(Integer.class)
    RepoDomainInvalidateCacheTimeInMinutes,
    /**
     * When retrieving a file list from a domain without explicitly stating whether should we force refresh or not
     * then whether a force refresh will be done is decided according to this value
     */
    @TypeConverterAttribute(Boolean.class)
    ForceRefreshDomainFilesListByDefault,
    @TypeConverterAttribute(Boolean.class)
    InstallVds,
    @Reloadable
    @TypeConverterAttribute(Boolean.class)
    EnableUSBAsDefault,
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    SSHInactivityTimeoutSeconds,
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    ServerRebootTimeout,
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    VmGracefulShutdownTimeout,
    @TypeConverterAttribute(Integer.class)
    VmPriorityMaxValue(ClientAccessLevel.User),
    @Reloadable
    @TypeConverterAttribute(String.class)
    VmGracefulShutdownMessage,
    @Reloadable
    @TypeConverterAttribute(String.class)
    SpiceSecureChannels,
    @Reloadable
    @TypeConverterAttribute(String.class)
    ConsoleReleaseCursorKeys,
    @Reloadable
    @TypeConverterAttribute(String.class)
    ConsoleToggleFullScreenKeys,
    @Reloadable
    @TypeConverterAttribute(Boolean.class)
    SpiceUsbAutoShare(ClientAccessLevel.User),
    @Reloadable
    @TypeConverterAttribute(Boolean.class)
    FullScreenWebadminDefault,
    @TypeConverterAttribute(Boolean.class)
    EncryptHostCommunication,
    @TypeConverterAttribute(String.class)
    VdsmSSLProtocol,
    @TypeConverterAttribute(String.class)
    ExternalCommunicationProtocol,
    @TypeConverterAttribute(String.class)
    VdsRequestQueueName,
    @TypeConverterAttribute(String.class)
    VdsResponseQueueName,
    @TypeConverterAttribute(String.class)
    IrsRequestQueueName,
    @TypeConverterAttribute(String.class)
    IrsResponseQueueName,
    @TypeConverterAttribute(String.class)
    EventQueueName,
    @TypeConverterAttribute(Integer.class)
    EventProcessingPoolSize,
    @Reloadable
    @TypeConverterAttribute(String.class)
    OrganizationName,
    @Reloadable
    @TypeConverterAttribute(Boolean.class)
    IsMultilevelAdministrationOn,
    @Reloadable
    @TypeConverterAttribute(Long.class)
    VdsRecoveryTimeoutInMinutes,
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    MaxBlockDiskSize(ClientAccessLevel.User),
    // the order is- {level}:{name}:{flags}:{vdsm};
    // {level}:{name}:{flags}:{vdsm};1:cpu_name:cpu_flags,..,:vdsm_exec,+..,-..;..
    @TypeConverterAttribute(String.class)
    ServerCPUList,
    @Reloadable
    @TypeConverterAttribute(List.class)
    @OptionBehaviourAttribute(behaviour = OptionBehaviour.CommaSeparatedStringArray)
    AgentAppName,
    @Reloadable
    @TypeConverterAttribute(Map.class)
    SpiceDriverNameInGuest,
    @Reloadable
    @TypeConverterAttribute(String.class)
    GuestToolsSetupIsoPrefix,
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    VcpuConsumptionPercentage,
    @TypeConverterAttribute(Boolean.class)
    EnableVdsLoadBalancing,
    @TypeConverterAttribute(Long.class)
    VdsLoadBalancingIntervalInMinutes,

    //AffinityRulesEnforcementManager
    @TypeConverterAttribute(Long.class)
    AffinityRulesEnforcementManagerRegularInterval,
    @TypeConverterAttribute(Long.class)
    AffinityRulesEnforcementManagerInitialDelay,
    @TypeConverterAttribute(Boolean.class)
    AffinityRulesEnforcementManagerEnabled,

    @TypeConverterAttribute(Long.class)
    VdsHaReservationIntervalInMinutes,
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    LowUtilizationForEvenlyDistribute,
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    LowUtilizationForPowerSave,
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    HostsInReserve,
    @Reloadable
    @TypeConverterAttribute(Boolean.class)
    EnableAutomaticHostPowerManagement,
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    HighUtilizationForEvenlyDistribute,

    // The percentage represents a minimum load for all cores
    // to consider a host over utilized for scheduling.
    // To clarify: all cores have to be loaded above this level
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    HighUtilizationForScheduling,
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    OverUtilizationForHaReservation,
    @TypeConverterAttribute(Integer.class)
    ScaleDownForHaReservation,
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    HighUtilizationForPowerSave,
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    UtilizationThresholdInPercent,
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    CpuOverCommitDurationMinutes,
    // a default of 120% memory over commit.
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    MaxVdsMemOverCommit(ClientAccessLevel.User),
    // a default of 120% memory over commit.
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    MaxVdsMemOverCommitForServers(ClientAccessLevel.User),
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    HighVmCountForEvenGuestDistribute,
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    SpmVmGraceForEvenGuestDistribute,
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    MigrationThresholdForEvenGuestDistribute,
    @Reloadable
    @TypeConverterAttribute(Boolean.class)
    AutoInstallCertificateOnApprove,
    @Reloadable
    @TypeConverterAttribute(Boolean.class)
    DebugTimerLogging,
    @TypeConverterAttribute(String.class)
    AutoApprovePatterns,
    @Reloadable
    @TypeConverterAttribute(String.class)
    AutoRegistrationDefaultClusterID,
    @TypeConverterAttribute(Long.class)
    StoragePoolRefreshTimeInSeconds,
    @TypeConverterAttribute(Long.class)
    HostStorageConnectionAndPoolRefreshTimeInSeconds,
    @Reloadable
    @TypeConverterAttribute(Long.class)
    StoragePoolNonOperationalResetTimeoutInMin,
    @TypeConverterAttribute(Long.class)
    StorageDomainFailureTimeoutInMinutes,
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    SPMFailOverAttempts,
    @Reloadable
    @TypeConverterAttribute(String.class)
    LockPolicy,
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    LockRenewalIntervalSec,
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    LeaseTimeSec,
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    IoOpTimeoutSec,
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    LeaseRetries,
    @Reloadable
    @TypeConverterAttribute(String.class)
    VncKeyboardLayout(ClientAccessLevel.User),
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    SpmCommandFailOverRetries,
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    HsmCommandFailOverRetries,
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    SpmVCpuConsumption,
    @Reloadable
    @TypeConverterAttribute(Boolean.class)
    EnableSpiceRootCertificateValidation,
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    VM32BitMaxMemorySizeInMB(ClientAccessLevel.User),
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    VM64BitMaxMemorySizeInMB(ClientAccessLevel.User),
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    BlockMigrationOnSwapUsagePercentage,
    @Reloadable
    @TypeConverterAttribute(Boolean.class)
    EnableSwapCheck,
    @Reloadable
    @TypeConverterAttribute(Boolean.class)
    SendSMPOnRunVm,
    @TypeConverterAttribute(String.class)
    @OptionBehaviourAttribute(behaviour = OptionBehaviour.ValueDependent, dependentOn = DBEngine,
    realValue = "PagingSyntax")
    DBPagingSyntax,
    @TypeConverterAttribute(String.class)
    @OptionBehaviourAttribute(behaviour = OptionBehaviour.ValueDependent, dependentOn = DBEngine,
    realValue = "PagingType")
    DBPagingType,
    @TypeConverterAttribute(String.class)
    @OptionBehaviourAttribute(behaviour = OptionBehaviour.ValueDependent, dependentOn = DBEngine,
    realValue = "SearchTemplate")
    DBSearchTemplate,
    @TypeConverterAttribute(String.class)
    PostgresPagingSyntax,      // used by behaviour DBPagingSyntax
    @TypeConverterAttribute(String.class)
    PostgresPagingType,        // used by behaviour DBPagingType
    @TypeConverterAttribute(String.class)
    PostgresSearchTemplate,    // used by behaviour DBSearchTemplate
    @Reloadable
    @TypeConverterAttribute(HashSet.class)
    @OptionBehaviourAttribute(behaviour = OptionBehaviour.CommaSeparatedVersionArray)
    SupportedVDSMVersions,
    @TypeConverterAttribute(HashSet.class)
    @OptionBehaviourAttribute(behaviour = OptionBehaviour.CommaSeparatedVersionArray)
    SupportedClusterLevels(ClientAccessLevel.User),
    @Reloadable
    @TypeConverterAttribute(String.class)
    OvfVirtualSystemType,

    @Reloadable
    @TypeConverterAttribute(Integer.class)
    WaitForVdsInitInSec,

    @Reloadable
    @TypeConverterAttribute(Long.class)
    OvfUpdateIntervalInMinutes,

    @Reloadable
    @TypeConverterAttribute(Integer.class)
    OvfItemsCountPerUpdate,

    @TypeConverterAttribute(String.class)
    DefaultWindowsTimeZone(ClientAccessLevel.Admin),

    @TypeConverterAttribute(String.class)
    DefaultGeneralTimeZone(ClientAccessLevel.Admin),

    @Reloadable
    @TypeConverterAttribute(Boolean.class)
    SANWipeAfterDelete,

    @TypeConverterAttribute(String.class)
    DataDir,

    @TypeConverterAttribute(Integer.class)
    UserSessionTimeOutInterval(ClientAccessLevel.User),

    @Reloadable
    @TypeConverterAttribute(String.class)
    RhevhLocalFSPath(ClientAccessLevel.Admin),

    @TypeConverterAttribute(String.class)
    UserDefinedVMProperties(ClientAccessLevel.User),

    @TypeConverterAttribute(String.class)
    PredefinedVMProperties(ClientAccessLevel.User),

    @TypeConverterAttribute(Integer.class)
    MaxNumberOfHostsInStoragePool,

    @Reloadable
    @TypeConverterAttribute(Integer.class)
    MaxVmNameLengthSysprep(ClientAccessLevel.User),

    @Reloadable
    @TypeConverterAttribute(Integer.class)
    MaxVmNameLength(ClientAccessLevel.User),

    @Reloadable
    @TypeConverterAttribute(Integer.class)
    MaxVdsNameLength,

    @Reloadable
    @TypeConverterAttribute(Double.class)
    MaxStorageVdsTimeoutCheckSec,

    @Reloadable
    @TypeConverterAttribute(Double.class)
    MaxStorageVdsDelayCheckSec,

    @Reloadable
    @TypeConverterAttribute(Integer.class)
    DisableFenceAtStartupInSec,

    @Reloadable
    @TypeConverterAttribute(Integer.class)
    NicDHCPDelayGraceInMS,

    @Reloadable
    @TypeConverterAttribute(Integer.class)
    FindFenceProxyRetries,

    @Reloadable
    @TypeConverterAttribute(Integer.class)
    FindFenceProxyDelayBetweenRetriesInSec,

    @Reloadable
    @TypeConverterAttribute(Integer.class)
    LogPhysicalMemoryThresholdInMB,

    @Reloadable
    @TypeConverterAttribute(Integer.class)
    LogSwapMemoryThresholdInMB,

    @Reloadable
    @TypeConverterAttribute(Integer.class)
    FenceStartStatusRetries,

    @Reloadable
    @TypeConverterAttribute(Integer.class)
    FenceStartStatusDelayBetweenRetriesInSec,

    @TypeConverterAttribute(String.class)
    @OptionBehaviourAttribute(behaviour = OptionBehaviour.ValueDependent, dependentOn = DBEngine,
    realValue = "LikeSyntax")
    DBLikeSyntax,
    @TypeConverterAttribute(String.class)
    PostgresLikeSyntax,    // used by behaviour DBLikeSyntax

    @TypeConverterAttribute(String.class)
    @OptionBehaviourAttribute(behaviour = OptionBehaviour.ValueDependent, dependentOn = DBEngine,
    realValue = "I18NPrefix")
    DBI18NPrefix,
    @TypeConverterAttribute(String.class)
    PostgresI18NPrefix,    // used by behaviour DBI18NPrefix

    @Reloadable
    @TypeConverterAttribute(Integer.class)
    UnknownTaskPrePollingLapse,

    @TypeConverterAttribute(String.class)
    ProductRPMVersion(ClientAccessLevel.User),

    @Reloadable
    @TypeConverterAttribute(Integer.class)
    MaxAuditLogMessageLength,

    @Reloadable
    @TypeConverterAttribute(String.class)
    SysPrepDefaultUser,

    @Reloadable
    @TypeConverterAttribute(String.class)
    @OptionBehaviourAttribute(behaviour = OptionBehaviour.Password)
    SysPrepDefaultPassword,

    @Reloadable
    @TypeConverterAttribute(String.class)
    FenceAgentMapping,

    @Reloadable
    @TypeConverterAttribute(String.class)
    CustomFenceAgentMapping,

    @Reloadable
    @TypeConverterAttribute(String.class)
    FenceAgentDefaultParams,

    @Reloadable
    @TypeConverterAttribute(String.class)
    CustomFenceAgentDefaultParams,

    @Reloadable
    @TypeConverterAttribute(String.class)
    FenceAgentDefaultParamsForPPC,

    @Reloadable
    @TypeConverterAttribute(String.class)
    CustomFenceAgentDefaultParamsForPPC,

    @Reloadable
    @TypeConverterAttribute(Integer.class)
    SignCertTimeoutInSeconds,

    @Reloadable
    @TypeConverterAttribute(Integer.class)
    ConnectToServerTimeoutInSeconds,

    @Reloadable
    @TypeConverterAttribute(String.class)
    IPTablesConfig,

    /**
     * Lower threshold for disk space on host to be considered low, in MB.
     */
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    VdsLocalDisksLowFreeSpace,

    /**
     * Lower threshold for disk space on host to be considered critically low (almost out of space), in MB.
     */
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    VdsLocalDisksCriticallyLowFreeSpace,

    @Reloadable
    @TypeConverterAttribute(String.class)
    DefaultManagementNetwork,

    @Reloadable
    @TypeConverterAttribute(String.class)
    OvirtInitialSupportedIsoVersion,

    @TypeConverterAttribute(String.class)
    OvirtIsoPrefix,

    @Reloadable
    @TypeConverterAttribute(Integer.class)
    QuotaThresholdCluster,

    @Reloadable
    @TypeConverterAttribute(Integer.class)
    QuotaThresholdStorage,

    @Reloadable
    @TypeConverterAttribute(Integer.class)
    QuotaGraceCluster,

    @Reloadable
    @TypeConverterAttribute(Integer.class)
    QuotaGraceStorage,

    // This value indicates devices that although are given to us by VDSM
    // are still treated as managed devices
    // This should be a [device=<device> type=<type>[,]]* string
    @Reloadable
    @TypeConverterAttribute(String.class)
    ManagedDevicesWhiteList,

    /**
     * The origin type to be used for VM and VM template creation
     */
    @TypeConverterAttribute(String.class)
    OriginType,

    @TypeConverterAttribute(String.class)
    ImageProxyAddress,

    @TypeConverterAttribute(Boolean.class)
    ImageProxySSLEnabled,

    @TypeConverterAttribute(String.class)
    ImageDaemonPort,


    @TypeConverterAttribute(Integer.class)
    ImageTransferClientTicketValidityInSeconds,

    @TypeConverterAttribute(Integer.class)
    ImageTransferHostTicketValidityInSeconds,

    @TypeConverterAttribute(Integer.class)
    ImageTransferHostTicketRefreshAllowanceInSeconds,

    @TypeConverterAttribute(Integer.class)
    ImageTransferPausedLogIntervalInSeconds,

    @TypeConverterAttribute(Integer.class)
    UploadImageUiInactivityTimeoutInSeconds(ClientAccessLevel.User),

    @TypeConverterAttribute(Integer.class)
    UploadImageChunkSizeKB(ClientAccessLevel.Admin),

    @TypeConverterAttribute(Integer.class)
    UploadImageXhrTimeoutInSeconds(ClientAccessLevel.Admin),

    @TypeConverterAttribute(Integer.class)
    UploadImageXhrRetryIntervalInSeconds(ClientAccessLevel.Admin),

    @TypeConverterAttribute(Integer.class)
    UploadImageXhrMaxRetries(ClientAccessLevel.Admin),

    @TypeConverterAttribute(Boolean.class)
    GetImageTicketSupported,

    @TypeConverterAttribute(Boolean.class)
    TestImageIOProxyConnectionSupported,

    @Reloadable
    @TypeConverterAttribute(Long.class)
    SetupNetworksPollingTimeout,

    @TypeConverterAttribute(Long.class)
    JobCleanupRateInMinutes,

    @TypeConverterAttribute(Integer.class)
    SucceededJobCleanupTimeInMinutes,

    @TypeConverterAttribute(Integer.class)
    FailedJobCleanupTimeInMinutes,

    @Reloadable
    @TypeConverterAttribute(Integer.class)
    JobPageSize,

    @TypeConverterAttribute(Long.class)
    VmPoolMonitorIntervalInMinutes,

    @Reloadable
    @TypeConverterAttribute(Integer.class)
    VmPoolMonitorBatchSize,

    @TypeConverterAttribute(String.class)
    AutoRecoverySchedule,

    @TypeConverterAttribute(Integer.class)
    VmPoolMonitorMaxAttempts,

    @TypeConverterAttribute(Integer.class)
    PayloadSize,

    @TypeConverterAttribute(Integer.class)
    ApplicationMode(ClientAccessLevel.User),

    @TypeConverterAttribute(Integer.class)
    NumberOfUSBSlots,

    @TypeConverterAttribute(Integer.class)
    PopulateDirectLUNDiskDescriptionWithLUNId(ClientAccessLevel.Admin),

    @TypeConverterAttribute(String.class)
    WANDisableEffects(ClientAccessLevel.User),

    @TypeConverterAttribute(Integer.class)
    WANColorDepth(ClientAccessLevel.User),

    @TypeConverterAttribute(Integer.class)
    VmPoolMaxSubsequentFailures,

    @TypeConverterAttribute(Boolean.class)
    CpuPinMigrationEnabled(ClientAccessLevel.Admin),

    @TypeConverterAttribute(Integer.class)
    NetworkConnectivityCheckTimeoutInSeconds(ClientAccessLevel.Admin),

    @Reloadable
    @TypeConverterAttribute(Map.class)
    AutoRecoveryAllowedTypes,

    /*
     * umask is required to allow only self access
     * tar is missing from vanilla fedora-18 so we use python
     */
    @Reloadable
    @TypeConverterAttribute(String.class)
    BootstrapCommand,

    @TypeConverterAttribute(Integer.class)
    BootstrapCacheRefreshInterval,
    @TypeConverterAttribute(String.class)
    BootstrapPackageDirectory,
    @TypeConverterAttribute(String.class)
    BootstrapPackageName,
    @Reloadable
    @TypeConverterAttribute(String.class)
    SSHKeyAlias,
    @Reloadable
    @TypeConverterAttribute(String.class)
    SSHDefaultKeyDigest,

    /*
     * Whether to allow a cluster with both Virt and Gluster services enabled
     */
    @TypeConverterAttribute(Boolean.class)
    AllowClusterWithVirtGlusterEnabled(ClientAccessLevel.Admin),

    @TypeConverterAttribute(Boolean.class)
    EnableMACAntiSpoofingFilterRules,

    // Gluster peer status command
    @TypeConverterAttribute(String.class)
    GlusterPeerStatusCommand,

    @Reloadable
    @TypeConverterAttribute(Integer.class)
    SSHInactivityHardTimeoutSeconds,

    @TypeConverterAttribute(String.class)
    GlusterVolumeOptionGroupVirtValue(ClientAccessLevel.Admin),

    @TypeConverterAttribute(String.class)
    GlusterVolumeOptionOwnerUserVirtValue(ClientAccessLevel.Admin),

    @TypeConverterAttribute(String.class)
    GlusterVolumeOptionOwnerGroupVirtValue(ClientAccessLevel.Admin),

    @TypeConverterAttribute(String.class)
    GlusterDefaultBrickMountPoint(ClientAccessLevel.Admin),

    @Reloadable
    @TypeConverterAttribute(String.class)
    IPTablesConfigForVirt,

    @Reloadable
    @TypeConverterAttribute(String.class)
    IPTablesConfigForGluster,

    @Reloadable
    @TypeConverterAttribute(String.class)
    IPTablesConfigSiteCustom,

    // Host time drift
    @Reloadable
    @TypeConverterAttribute(Boolean.class)
    EnableHostTimeDrift,

    @Reloadable
    @TypeConverterAttribute(Integer.class)
    HostTimeDriftInSec,

    @TypeConverterAttribute(Integer.class)
    ThrottlerMaxWaitForVdsUpdateInMillis,

    @TypeConverterAttribute(Boolean.class)
    OnlyRequiredNetworksMandatoryForVdsSelection,

    @Reloadable
    @TypeConverterAttribute(EngineWorkingMode.class)
    EngineMode,

    /**
     * Refresh rate (in seconds) for light-weight gluster data i.e. data that can be fetched without much of an overhead
     * on the GlusterFS processes
     */
    @TypeConverterAttribute(Integer.class)
    GlusterRefreshRateLight,

    /**
     * Refresh rate (in seconds) for Storage Devices.
     */
    @TypeConverterAttribute(Integer.class)
    GlusterRefreshRateStorageDevices,

    /**
     * Refresh rate (in seconds) for heavy-weight gluster data i.e. commands to fetch such data adds a considerable
     * overhead on the GlusterFS processes.
     */
    @TypeConverterAttribute(Integer.class)
    GlusterRefreshRateHeavy,

    /**
     * Refresh rate (in seconds) for gluster self heal info . 'gluster self heal info' command will be used to fetch
     * heal info, and it adds a considerable overhead on the GlusterFS processes.
     */
    @TypeConverterAttribute(Integer.class)
    GlusterRefreshRateHealInfo,

    /**
     * Defines the number of history values storable by the engine for unsynced entries in gluster brick
     */
    @TypeConverterAttribute(Integer.class)
    GlusterUnSyncedEntriesHistoryLimit,

    @TypeConverterAttribute(Boolean.class)
    GlusterSelfHealMonitoringSupported,

    @Reloadable
    @TypeConverterAttribute(String.class)
    BootstrapMinimalVdsmVersion,

    @TypeConverterAttribute(String.class)
    MinimalETLVersion,

    @TypeConverterAttribute(String.class)
    OvirtNodeOS,

    @TypeConverterAttribute(Long.class)
    QuotaCacheIntervalInMinutes,

    @TypeConverterAttribute(Integer.class)
    MinimumPercentageToUpdateQuotaCache,

    @Reloadable
    @TypeConverterAttribute(Integer.class)
    LogMaxPhysicalMemoryUsedThresholdInPercentage,

    @Reloadable
    @TypeConverterAttribute(Integer.class)
    LogMaxSwapMemoryUsedThresholdInPercentage,

    @Reloadable
    @TypeConverterAttribute(Integer.class)
    LogMaxCpuUsedThresholdInPercentage,

    @Reloadable
    @TypeConverterAttribute(Integer.class)
    LogMaxNetworkUsedThresholdInPercentage,

    @TypeConverterAttribute(Integer.class)
    PgMajorRelease,

    @TypeConverterAttribute(List.class)
    @OptionBehaviourAttribute(behaviour = OptionBehaviour.CommaSeparatedStringArray)
    VncKeyboardLayoutValidValues(ClientAccessLevel.User),

    @Reloadable
    @TypeConverterAttribute(String.class)
    SpiceProxyDefault(ClientAccessLevel.User),

    @Reloadable
    @TypeConverterAttribute(String.class)
    RemoteViewerSupportedVersions,

    @Reloadable
    @TypeConverterAttribute(String.class)
    RemoteViewerNewerVersionUrl,

    @Reloadable
    @TypeConverterAttribute(Boolean.class)
    RemapCtrlAltDelDefault(ClientAccessLevel.User),

    @TypeConverterAttribute(Integer.class)
    GlusterRefreshRateHooks,

    @TypeConverterAttribute(Boolean.class)
    GlusterServicesEnabled,

    @TypeConverterAttribute(Integer.class)
    GlusterTaskMinWaitForCleanupInMins,

    @TypeConverterAttribute(String.class)
    GlusterTunedProfile,

    @TypeConverterAttribute(String.class)
    GlusterStorageDeviceListMountPointsToIgnore,

    @TypeConverterAttribute(String.class)
    GlusterStorageDeviceListFileSystemTypesToIgnore,

    @TypeConverterAttribute(Integer.class)
    GlusterRefreshRateGeoRepDiscoveryInSecs,

    @TypeConverterAttribute(Integer.class)
    GlusterRefreshRateGeoRepStatusInSecs,

    @TypeConverterAttribute(Integer.class)
    GlusterRefreshRateSnapshotDiscovery,

    @TypeConverterAttribute(String.class)
    GlusterMetaVolumeName(ClientAccessLevel.Admin),

    @TypeConverterAttribute(String.class)
    PollUri,

    @TypeConverterAttribute(String.class)
    AttestationTruststore,

    @TypeConverterAttribute(Integer.class)
    AttestationPort,

    @TypeConverterAttribute(String.class)
    @OptionBehaviourAttribute(behaviour = OptionBehaviour.Password)
    AttestationTruststorePass,

    @TypeConverterAttribute(String.class)
    AttestationServer,

    @TypeConverterAttribute(Integer.class)
    AttestationFirstStageSize,

    @TypeConverterAttribute(Boolean.class)
    SecureConnectionWithOATServers,

    @TypeConverterAttribute(Integer.class)
    AttestationSecondStageSize,

    @TypeConverterAttribute(Integer.class)
    GlusterRefreshRateTasks,

    @TypeConverterAttribute(String.class)
    ClientModeSpiceDefault(ClientAccessLevel.User),

    @TypeConverterAttribute(String.class)
    ClientModeVncDefault(ClientAccessLevel.User),

    @Reloadable
    @TypeConverterAttribute(Double.class)
    DelayResetForSpmInSeconds,

    @Reloadable
    @TypeConverterAttribute(Double.class)
    DelayResetPerVmInSeconds,

    @Reloadable
    @TypeConverterAttribute(String.class)
    GetVdsmIdByVdsmToolCommand,

    @TypeConverterAttribute(String.class)
    ClientModeRdpDefault(ClientAccessLevel.User),

    @Reloadable
    @TypeConverterAttribute(String.class)
    WebSocketProxy(ClientAccessLevel.User),

    @TypeConverterAttribute(Integer.class)
    WebSocketProxyTicketValiditySeconds,

    @TypeConverterAttribute(String.class)
    CustomDeviceProperties,

    @TypeConverterAttribute(String.class)
    PreDefinedNetworkCustomProperties(ClientAccessLevel.Admin),

    @TypeConverterAttribute(String.class)
    UserDefinedNetworkCustomProperties(ClientAccessLevel.Admin),

    @TypeConverterAttribute(String.class)
    OsRepositoryConfDir,

    @TypeConverterAttribute(String.class)
    SshSoftFencingCommand,

    @TypeConverterAttribute(String.class)
    SshHostRebootCommand,

    @TypeConverterAttribute(String.class)
    SshVdsPowerdownCommand,

    @TypeConverterAttribute(List.class)
    @OptionBehaviourAttribute(behaviour = OptionBehaviour.CommaSeparatedStringArray)
    ClusterEmulatedMachines,

    @TypeConverterAttribute(Integer.class)
    MaxAverageNetworkQoSValue(ClientAccessLevel.Admin),

    @TypeConverterAttribute(Integer.class)
    MaxPeakNetworkQoSValue(ClientAccessLevel.Admin),

    @TypeConverterAttribute(Integer.class)
    MaxBurstNetworkQoSValue(ClientAccessLevel.Admin),

    @TypeConverterAttribute(Integer.class)
    MaxHostNetworkQosShares(ClientAccessLevel.Admin),

    @TypeConverterAttribute(Integer.class)
    QoSInboundAverageDefaultValue(ClientAccessLevel.Admin),

    @TypeConverterAttribute(Integer.class)
    QoSInboundPeakDefaultValue(ClientAccessLevel.Admin),

    @TypeConverterAttribute(Integer.class)
    QoSInboundBurstDefaultValue(ClientAccessLevel.Admin),

    @TypeConverterAttribute(Integer.class)
    IterationsWithBalloonProblem,

    @TypeConverterAttribute(Integer.class)
    QoSOutboundAverageDefaultValue,

    @TypeConverterAttribute(Integer.class)
    QoSOutboundPeakDefaultValue,

    @TypeConverterAttribute(Integer.class)
    QoSOutboundBurstDefaultValue,

    @TypeConverterAttribute(String.class)
    ExternalSchedulerServiceURL,

    @TypeConverterAttribute(Integer.class)
    ExternalSchedulerConnectionTimeout,

    @TypeConverterAttribute(Integer.class)
    ExternalSchedulerResponseTimeout,

    @TypeConverterAttribute(Boolean.class)
    ExternalSchedulerEnabled,

    @TypeConverterAttribute(Integer.class)
    MaxSchedulerWeight,

    @TypeConverterAttribute(Boolean.class)
    UseFqdnForRdpIfAvailable(ClientAccessLevel.User),

    @TypeConverterAttribute(Long.class)
    DwhHeartBeatInterval,

    @TypeConverterAttribute(Integer.class)
    DisconnectDwh,

    @TypeConverterAttribute(Integer.class)
    GlanceImageListSize,

    @TypeConverterAttribute(Integer.class)
    GlanceImageTotalListSize,

    @TypeConverterAttribute(Integer.class)
    HostPreparingForMaintenanceIdleTime,

    @TypeConverterAttribute(Integer.class)
    SpeedOptimizationSchedulingThreshold(ClientAccessLevel.Admin),

    @TypeConverterAttribute(Boolean.class)
    SchedulerAllowOverBooking(ClientAccessLevel.Admin),

    @TypeConverterAttribute(Integer.class)
    SchedulerOverBookingThreshold(ClientAccessLevel.Admin),

    @TypeConverterAttribute(Boolean.class)
    GlusterSupportArbiterVolume,

    @Reloadable
    @TypeConverterAttribute(Integer.class)
    GlusterPeerStatusRetries,

    @TypeConverterAttribute(Long.class)
    AutoStartVmsRunnerIntervalInSeconds,

    @TypeConverterAttribute(Integer.class)
    RetryToRunAutoStartVmIntervalInSeconds,

    @TypeConverterAttribute(Integer.class)
    DelayToRunAutoStartVmIntervalInSeconds,

    @TypeConverterAttribute(Integer.class)
    MaxNumOfTriesToRunFailedAutoStartVm,

    @TypeConverterAttribute(Integer.class)
    MaxNumOfSkipsBeforeAutoStartVm,

    /**
     * Value representing maximum number of milliseconds a VM can be down during live migration.
     * Default value of 0 means this value will not be sent to VDSM at all and the currently configured value on
     * the VDSM will be used.
     */
    @TypeConverterAttribute(Integer.class)
    DefaultMaximumMigrationDowntime(ClientAccessLevel.Admin),

    @TypeConverterAttribute(Map.class)
    HotPlugCpuSupported(ClientAccessLevel.User),

    @TypeConverterAttribute(Map.class)
    HotUnplugCpuSupported,

    @TypeConverterAttribute(Map.class)
    HotPlugMemorySupported,

    @TypeConverterAttribute(Map.class)
    HotUnplugMemorySupported,

    @TypeConverterAttribute(Map.class)
    IsMigrationSupported,

    @TypeConverterAttribute(Boolean.class)
    IsHighPerformanceTypeSupported,

    @TypeConverterAttribute(Map.class)
    IsMemorySnapshotSupported,

    @TypeConverterAttribute(Map.class)
    IsSuspendSupported,

    @TypeConverterAttribute(SerialNumberPolicy.class)
    DefaultSerialNumberPolicy,

    @TypeConverterAttribute(String.class)
    DefaultCustomSerialNumber,

    @TypeConverterAttribute(Integer.class)
    UploadFileMaxTimeInMinutes,

    @TypeConverterAttribute(Integer.class)
    RetrieveDataMaxTimeInMinutes,

    @TypeConverterAttribute(Integer.class)
    StorageDomainOvfStoreCount,

    @TypeConverterAttribute(String.class)
    DefaultSysprepLocale,

    @TypeConverterAttribute(Boolean.class)
    PMHealthCheckEnabled,

    @TypeConverterAttribute(Long.class)
    PMHealthCheckIntervalInSec,

    @TypeConverterAttribute(String.class)
    ClusterRequiredRngSourcesDefault(ClientAccessLevel.User),

    @TypeConverterAttribute(Integer.class)
    DefaultMTU(ClientAccessLevel.Admin),

    /**
     * Defines the hostname(s) or IP address(es) to send fence_kdump messages to. If empty, engine FQDN is used.
     */
    @TypeConverterAttribute(String.class)
    FenceKdumpDestinationAddress,

    /**
     * Defines the port to send fence_kdump messages to.
     */
    @TypeConverterAttribute(Integer.class)
    FenceKdumpDestinationPort,

    /**
     * Defines the interval in seconds between messages sent by fence_kdump_send.
     */
    @TypeConverterAttribute(Integer.class)
    FenceKdumpMessageInterval,

    /**
     * Defines max timeout in seconds since last heartbeat to consider fence_kdump listener alive.
     */
    @TypeConverterAttribute(Integer.class)
    FenceKdumpListenerTimeout,

    /**
     * Defines maximum timeout in seconds to wait until 1st message from kdumping host is received (host kdump flow
     * started).
     */
    @TypeConverterAttribute(Integer.class)
    KdumpStartedTimeout,

    @TypeConverterAttribute(Integer.class)
    AlertOnNumberOfLVs,

    /**
     * Defines the parameter name used by the agent script to delay host on/off
     */
    @TypeConverterAttribute(String.class)
    FencePowerWaitParam,

    /**
     * Defines the parameter name used by the custom agent script to delay host on/off
     */
    @TypeConverterAttribute(String.class)
    CustomFencePowerWaitParam,

    /**
     * If the values is {@code true} then the RESTAPI will accept requests to create CSRF protected sessions, otherwise
     * all sessions will be unprotected, regardless of what the client requests.
     */
    @TypeConverterAttribute(Boolean.class)
    CSRFProtection,

    /**
     * If the values is {@code true} then the RESTAPI will support CORS (Cross Origin Resource Sharing).
     */
    @TypeConverterAttribute(Boolean.class)
    CORSSupport(ClientAccessLevel.Admin),

    /**
     * If CORS is enabled (with the {@code CORSSupport} parameter set to {@code true} then this indicates which are the
     * allowed origins.
     */
    @TypeConverterAttribute(String.class)
    CORSAllowedOrigins(ClientAccessLevel.Admin),

    /**
     * If CORS is enabled (with the {@code CORSSupport} parameter set to {@code true} then this indicates
     * whether all configured hosts shall be taken as allowed origins.
     */
    @TypeConverterAttribute(Boolean.class)
    CORSAllowDefaultOrigins(ClientAccessLevel.Admin),

    /**
     * If CORS is enabled (with the {@code CORSSupport} parameter set to {@code true}
     * same as the {@code CORSAllowDefaultOrigins} is set to {@code true},
     * then the (@code CORSDefaultOriginSuffixes) indicate what suffices will be added
     * to the by-default-allowed origins (means all hosts).
     *
     * A comma-separated list like ":9090,:1234"
     */
    @TypeConverterAttribute(String.class)
    CORSDefaultOriginSuffixes(ClientAccessLevel.Admin),

    /**
     * Port the Cockpit is listening on.
     *
     */
    @TypeConverterAttribute(String.class)
    CockpitPort(ClientAccessLevel.Admin),

    /**
     * Port the ovirt-cockpit-sso service is listening on.
     *
     */
    @TypeConverterAttribute(String.class)
    CockpitSSOPort(ClientAccessLevel.Admin),

    /**
     * Interval in seconds after which is safe to check host storage lease status when host stopped responding
     * to monitoring
     */
    @TypeConverterAttribute(Integer.class)
    HostStorageLeaseAliveCheckingInterval,

    @TypeConverterAttribute(Integer.class)
    MaxThroughputUpperBoundQosValue(ClientAccessLevel.Admin),

    @TypeConverterAttribute(Integer.class)
    MaxReadThroughputUpperBoundQosValue(ClientAccessLevel.Admin),

    @TypeConverterAttribute(Integer.class)
    MaxWriteThroughputUpperBoundQosValue(ClientAccessLevel.Admin),

    @TypeConverterAttribute(Integer.class)
    MaxIopsUpperBoundQosValue(ClientAccessLevel.Admin),

    @TypeConverterAttribute(Integer.class)
    MaxReadIopsUpperBoundQosValue(ClientAccessLevel.Admin),

    @TypeConverterAttribute(Integer.class)
    MaxWriteIopsUpperBoundQosValue(ClientAccessLevel.Admin),

    /**
     * UseHostNameIdentifier this will run external headers on STOMP connect frame, in order to identify hosts
     */
    @TypeConverterAttribute(Boolean.class)
    UseHostNameIdentifier,

    /**
    * Defines the number of history values storable by the engine for cpu/network/memory usage of a VM
    */
    @TypeConverterAttribute(Integer.class)
    UsageHistoryLimit,

    @TypeConverterAttribute(Integer.class)
    MaxCpuLimitQosValue(ClientAccessLevel.Admin),

    @Reloadable
    @TypeConverterAttribute(List.class)
    @OptionBehaviourAttribute(behaviour = OptionBehaviour.CommaSeparatedStringArray)
    UnsupportedLocalesFilterOverrides,

    /**
     * Defines the parameter name used by numa migration on/off
     */
    @TypeConverterAttribute(Boolean.class)
    SupportNUMAMigration,

    @TypeConverterAttribute(List.class)
    @OptionBehaviourAttribute(behaviour = OptionBehaviour.CommaSeparatedStringArray)
    UnsupportedLocalesFilter,

    @TypeConverterAttribute(Boolean.class)
    DefaultAutoConvergence,

    @TypeConverterAttribute(Boolean.class)
    DefaultMigrationCompression,

    @TypeConverterAttribute(Long.class)
    BackupCheckPeriodInHours,

    @TypeConverterAttribute(Integer.class)
    BackupAlertPeriodInDays,

    @TypeConverterAttribute(List.class)
    @OptionBehaviourAttribute(behaviour = OptionBehaviour.CommaSeparatedStringArray)
    HostDevicePassthroughCapabilities(ClientAccessLevel.Admin),

    /**
     * The interval in hours of checking for available updates on the host.
     */
    @TypeConverterAttribute(Double.class)
    HostPackagesUpdateTimeInHours,

    /**
     * The interval in hours of checking the validity of the host's certification
     */
    @TypeConverterAttribute(Double.class)
    CertificationValidityCheckTimeInHours,

    @TypeConverterAttribute(Integer.class)
    CertExpirationWarnPeriodInDays,

    @TypeConverterAttribute(Integer.class)
    CertExpirationAlertPeriodInDays,

    @TypeConverterAttribute(Integer.class)
    VMConsoleTicketTolerance,

    @TypeConverterAttribute(Boolean.class)
    DataOperationsByHSM,

    /**
     * In 4.1 gluster libgfapi is supported.
     */
    @TypeConverterAttribute(Boolean.class)
    LibgfApiSupported,

    @TypeConverterAttribute(Integer.class)
    MaxMemorySlots,

    /** User can only hot plug multiples of this value. */
    @TypeConverterAttribute(Integer.class)
    HotPlugMemoryBlockSizeMb,

    @TypeConverterAttribute(String.class)
    HostedEngineVmName,

    @TypeConverterAttribute(String.class)
    HostedEngineConfigurationImageGuid,

    @TypeConverterAttribute(Integer.class)
    MaxIoThreadsPerVm(ClientAccessLevel.User),

    @TypeConverterAttribute(Integer.class)
    VMPpc64BitMaxMemorySizeInMB(ClientAccessLevel.User),

    @TypeConverterAttribute(Boolean.class)
    AutoImportHostedEngine,

    @TypeConverterAttribute(Boolean.class)
    SriovHotPlugSupported,

    @TypeConverterAttribute(Boolean.class)
    AdPartnerMacSupported(ClientAccessLevel.Admin),

    @TypeConverterAttribute(Boolean.class)
    OvsSupported(ClientAccessLevel.Admin),

    @TypeConverterAttribute(Boolean.class)
    AllowEditingHostedEngine,

    @TypeConverterAttribute(Boolean.class)
    DisplayUncaughtUIExceptions(ClientAccessLevel.Admin),

    @TypeConverterAttribute(Boolean.class)
    MigrationPoliciesSupported(ClientAccessLevel.User),

    @TypeConverterAttribute(String.class)
    MigrationPolicies(ClientAccessLevel.Admin),

    @TypeConverterAttribute(Integer.class)
    HostedEngineConfigDiskSizeInBytes,

    @TypeConverterAttribute(Boolean.class)
    HystrixMonitoringEnabled,

    @TypeConverterAttribute(Boolean.class)
    Ipv6Supported,

    @TypeConverterAttribute(Boolean.class)
    GetNamesOfVmsFromExternalProviderSupported(ClientAccessLevel.Admin),

    @TypeConverterAttribute(Boolean.class)
    VirtIOScsiIOThread,

    @TypeConverterAttribute(Boolean.class)
    PassDiscardSupported(ClientAccessLevel.User),

    @TypeConverterAttribute(Boolean.class)
    DiscardAfterDeleteSupported(ClientAccessLevel.Admin),

    @TypeConverterAttribute(Boolean.class)
    QcowCompatSupported,

    @TypeConverterAttribute(Boolean.class)
    ReduceDeviceFromStorageDomain,

    @TypeConverterAttribute(Boolean.class)
    QemuimgCommitSupported,

    @TypeConverterAttribute(Boolean.class)
    Ipv6MigrationProperlyHandled,

    @TypeConverterAttribute(Boolean.class)
    VmLeasesSupported(ClientAccessLevel.User),

    @TypeConverterAttribute(Boolean.class)
    DomainXML,

    @TypeConverterAttribute(Boolean.class)
    AgentChannelNamingSupported,

    /**
     * Timeout in seconds for the completion of calls to external network providers.
     */
    @TypeConverterAttribute(Integer.class)
    ExternalNetworkProviderTimeout,

    @TypeConverterAttribute(Boolean.class)
    BackupSupported,

    @TypeConverterAttribute(Integer.class)
    RepeatEndMethodsOnFailMaxRetries,

    /**
     * Timeout in seconds for establishment of connections with external network providers. This
     * should be quite small, a few seconds at most, as it the TCP handshake with
     * network providers should be very quick in most networks.
     */
    @TypeConverterAttribute(Integer.class)
    ExternalNetworkProviderConnectionTimeout,

    @TypeConverterAttribute(Boolean.class)
    LegacyGraphicsDisplay,

    @TypeConverterAttribute(Boolean.class)
    DestroyOnRebootSupported(ClientAccessLevel.User),

    @TypeConverterAttribute(Version.class)
    MultiFirewallSupportSince,

    @TypeConverterAttribute(Boolean.class)
    ReduceVolumeSupported,

    /**
     * Value of 'Filter' header Web-ui is supposed to send for admin users
     *
     * <p>Web-ui assumes {@code false} if option is missing (in older engine versions).</p>
     */
    @TypeConverterAttribute(Boolean.class)
    AlwaysFilterResultsForWebUi(ClientAccessLevel.User),

    @TypeConverterAttribute(Boolean.class)
    ContentType,

    @TypeConverterAttribute(Boolean.class)
    IsoOnDataDomain,

    /**
     * Determines if the resume behavior can be configured in this compatibility level.
     *
     * If the storage on which the VM has it's disks gets unavailable, the VM gets paused.
     * The resume behavior determines what should happen in case the storage gets available again.
     */
    @TypeConverterAttribute(Boolean.class)
    ResumeBehaviorSupported,

    @TypeConverterAttribute(Boolean.class)
    DefaultRouteReportedByVdsm(),

    @TypeConverterAttribute(Boolean.class)
    GlusterEventingSupported,

    @TypeConverterAttribute(Boolean.class)
    LldpInformationSupported,

    Invalid;

    private ClientAccessLevel accessLevel;

    private ConfigValues(ClientAccessLevel accessLevel) {
        this.accessLevel = accessLevel;
    }

    private ConfigValues() {
        this(ClientAccessLevel.Internal);
    }

    public boolean nonAdminVisible() {
        return accessLevel == ClientAccessLevel.User;
    }

    public ClientAccessLevel getAccessLevel() {
        return accessLevel;
    }

    /**
     * @see org.ovirt.engine.core.bll.GetSystemOptionQuery#shouldReturnValue
     */
    public enum ClientAccessLevel {
        Internal,
        Admin,
        User
    }
}
