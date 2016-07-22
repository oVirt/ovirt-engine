package org.ovirt.engine.core.common.config;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.EngineWorkingMode;
import org.ovirt.engine.core.common.businessentities.SerialNumberPolicy;

public enum ConfigValues {
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("Postgres")
    DBEngine,

    /**
     * Timeout in seconds for the completion of calls to VDSM. It should
     * be quite large as some host operations can take more than 3
     * minutes to complete.
     */
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("180")
    vdsTimeout,

    /**
     * The number of times to retry host operations when there are IO errors.
     */
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("0")
    vdsRetries,

    /**
     * Timeout in seconds how often we should receive heart-beat.
     */
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("30")
    vdsHeartbeatInSeconds,

    /**
     * Timeout for establishment of connections with hosts. This should be quite
     * small, a few seconds at most, as it the TCP handshake with hosts should
     * be very quick in most networks.
     */
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("20")
    vdsConnectionTimeout,

    /**
     * Maximum concurrent http(s) connections to hosts. A small number of connections should suffice for most
     * environments. When a lot of storage actions are performed, this value can be increased for more VDS command
     * throughput.
     */
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("2")
    VdsMaxConnectionsPerHost,

    /**
     * Maximum concurrent http(s) connections to hosts. A small number of connections should suffice for most
     * environments. When a lot of storage actions are performed, this value can be increased for more SPM command
     * throughput.
     */
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("2")
    IrsMaxConnectionsPerHost,

    /**
     * Maximum number of connections allowed.
     */
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("20")
    MaxTotalConnections,

    /**
     * The number of time to retry connection during protocol fallback.
     */
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("25")
    ProtocolFallbackRetries,

    /**
     * Timeout in milliseconds to wait between connection attempt during protocol fallback.
     */
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("5000")
    ProtocolFallbackTimeoutInMilliSeconds,

    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("2")
    VdsRefreshRate,
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("10")
    AsyncTaskPollingRate,

    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("1")
    AsyncCommandPollingLoopInSeconds,

    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("10")
    AsyncCommandPollingRateInSeconds,

    /**
     * The rate (in seconds) to refresh the cache that holds the asynchronous tasks' statuses.
     */
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("60")
    AsyncTaskStatusCacheRefreshRateInSeconds,

    /**
     * The period of time (in minutes) to hold the asynchronous tasks' statuses in the asynchronous tasks cache.
     */
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("5")
    AsyncTaskStatusCachingTimeInMinutes,
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("3000")
    AsyncTaskZombieTaskLifeInMinutes,
    @TypeConverterAttribute(Date.class)
    @DefaultValueAttribute("03:35:35")
    AuditLogCleanupTime,
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("30")
    AuditLogAgingThreshold,
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("3000")
    CoCoLifeInMinutes,
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("300")
    CoCoWaitForEventInMinutes,
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("10")
    CommandCoordinatorThreadPoolSize,
    @TypeConverterAttribute(Date.class)
    @DefaultValueAttribute("03:35:35")
    CommandEntityCleanupTime,
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("30")
    CommandEntityAgingThreshold,
    @Reloadable
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("drac5,ilo,ipmilan,rsa,bladecenter,alom,apc,eps,wti,rsb")
    VdsFenceType,
    @Reloadable
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("")
    CustomVdsFenceType,
    @Reloadable
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("alom:secure=secure,port=ipport;apc:secure=secure,port=ipport,slot=port;bladecenter:secure=secure,port=ipport,slot=port;drac5:secure=secure,port=ipport;eps:slot=port;ilo:secure=ssl,port=ipport;ipmilan:;rsa:secure=secure,port=ipport;rsb:;wti:secure=secure,port=ipport,slot=port")
    VdsFenceOptionMapping,
    @Reloadable
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("")
    CustomVdsFenceOptionMapping,
    @Reloadable
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("secure=bool,port=int,slot=int")
    VdsFenceOptionTypes,
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("3")
    FenceStopStatusRetries,
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("60")
    FenceStopStatusDelayBetweenRetriesInSec,
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("180")
    FenceQuietTimeBetweenOperationsInSec,
    @Reloadable
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("cluster,dc")
    FenceProxyDefaultPreferences,
    @Reloadable
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("/data/updates/ovirt-node-image.iso")
    oVirtUploadPath,
    @Reloadable
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("/usr/share/rhev-hypervisor")
    oVirtISOsRepositoryPath,
    @Reloadable
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("/usr/share/vdsm-reg/vdsm-upgrade")
    oVirtUpgradeScriptName,
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("5")
    VdsCertificateValidityInYears,
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("100")
    SearchResultsLimit,
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("2")
    VDSAttemptsToResetCount,
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("30")
    TimeoutToResetVdsInSeconds,
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("10")
    //This value is in percents
    WarningLowSpaceIndicator,
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("5")
    //This value is in GB
    CriticalSpaceActionBlocker,
    @Reloadable
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("1.0.0.0")
    VdcVersion,
    @Reloadable
    @TypeConverterAttribute(Boolean.class)
    @DefaultValueAttribute("true")
    SSLEnabled,
    @Reloadable
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("DEFAULT")
    CipherSuite,
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("40")
    StoragePoolNameSizeLimit,
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("50")
    StorageDomainNameSizeLimit,
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("3")
    NumberOfFailedRunsOnVds,
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("30")
    TimeToReduceFailedRunOnVdsInMinutes,
    /**
     * In default rerun Vm on all Available desktops
     */
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("3")
    MaxRerunVmOnVdsCount,
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("1000")
    MaxVmsInPool,
    @Reloadable
    @TypeConverterAttribute(List.class)
    @DefaultValueAttribute("1,2,4")
    @OptionBehaviourAttribute(behaviour = OptionBehaviour.CommaSeparatedStringArray)
    ValidNumOfMonitors,
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("16")
    MaxNumOfVmCpus,
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("16")
    MaxNumOfVmSockets,
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("16")
    MaxNumOfCpuPerSocket,
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("8")
    MaxNumOfThreadsPerCpu,
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("1")
    NumberVmRefreshesBeforeSave,
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("60")
    AutoRepoDomainRefreshTime,
    @TypeConverterAttribute(Boolean.class)
    @DefaultValueAttribute("true")
    InstallVds,
    @Reloadable
    @TypeConverterAttribute(Boolean.class)
    @DefaultValueAttribute("true")
    EnableUSBAsDefault,
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("300")
    SSHInactivityTimeoutSeconds,
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("120")
    ServerRebootTimeout,
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("40")
    VmGracefulShutdownTimeout,
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("100")
    VmPriorityMaxValue,
    @Reloadable
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("Shutting Down")
    VmGracefulShutdownMessage,
    @Reloadable
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("smain,sinputs,scursor,splayback,srecord,sdisplay,ssmartcard,susbredir")
    SpiceSecureChannels,
    @Reloadable
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("shift+f12")
    ConsoleReleaseCursorKeys,
    @Reloadable
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("shift+f11")
    ConsoleToggleFullScreenKeys,
    @Reloadable
    @TypeConverterAttribute(Boolean.class)
    @DefaultValueAttribute("true")
    SpiceUsbAutoShare,
    @Reloadable
    @TypeConverterAttribute(Boolean.class)
    @DefaultValueAttribute("false")
    FullScreenWebadminDefault,
    @Reloadable
    @TypeConverterAttribute(Boolean.class)
    @DefaultValueAttribute("true")
    FullScreenUserportalBasicDefault,
    @Reloadable
    @TypeConverterAttribute(Boolean.class)
    @DefaultValueAttribute("false")
    FullScreenUserportalExtendedDefault,
    @TypeConverterAttribute(Boolean.class)
    @DefaultValueAttribute("true")
    EncryptHostCommunication,
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("TLSv1")
    VdsmSSLProtocol,
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("TLSv1")
    ExternalCommunicationProtocol,
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("jms.queue.requests")
    VdsRequestQueueName,
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("jms.queue.reponses")
    VdsResponseQueueName,
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("jms.queue.irsrequests")
    IrsRequestQueueName,
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("jms.queue.irsreponses")
    IrsResponseQueueName,
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("jms.queue.events")
    EventQueueName,
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("10")
    EventProcessingPoolSize,
    @Reloadable
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("oVirt")
    OrganizationName,
    @Reloadable
    @TypeConverterAttribute(Boolean.class)
    @DefaultValueAttribute("true")
    IsMultilevelAdministrationOn,
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("3")
    VdsRecoveryTimeoutInMinutes,
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("8192")
    MaxBlockDiskSize,
    // the order is- {level}:{name}:{flags}:{vdsm};
    // {level}:{name}:{flags}:{vdsm};1:cpu_name:cpu_flags,..,:vdsm_exec,+..,-..;..
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("1:pentium3:vmx:pentium3;2:intel-qemu64-nx:vmx,sse2:qemu64,-nx,+sse2;3:intel-qemu64:vmx,sse2,nx:qemu64,+sse2;2:amd-qemu64-nx:svm,sse2:qemu64,-nx,+sse2;3:amd-qemu64:svm,sse2,nx:qemu64,+sse2")
    ServerCPUList,
    @Reloadable
    @TypeConverterAttribute(List.class)
    @DefaultValueAttribute("ovirt-guest-agent-common,ovirt-guest-agent")
    @OptionBehaviourAttribute(behaviour = OptionBehaviour.CommaSeparatedStringArray)
    AgentAppName,
    @Reloadable
    @TypeConverterAttribute(Map.class)
    @DefaultValueAttribute("{\"windows\":\"RHEV-Spice\",\"linux\":\"xorg-x11-drv-qxl\"}")
    SpiceDriverNameInGuest,
    @Reloadable
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("ovirt-guest-tools-")
    GuestToolsSetupIsoPrefix,
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("10")
    VcpuConsumptionPercentage,
    @TypeConverterAttribute(Boolean.class)
    @DefaultValueAttribute("true")
    EnableVdsLoadBalancing,
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("1")
    VdsLoadBalancingIntervalInMinutes,

    //AffinityRulesEnforcementManager
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("1")
    AffinityRulesEnforcementManagerRegularInterval,
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("1")
    AffinityRulesEnforcementManagerInitialDelay,
    @TypeConverterAttribute(Boolean.class)
    @DefaultValueAttribute("true")
    AffinityRulesEnforcementManagerEnabled,

    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("5")
    VdsHaReservationIntervalInMinutes,
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("0")
    LowUtilizationForEvenlyDistribute,
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("6")
    LowUtilizationForPowerSave,
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("0")
    HostsInReserve,
    @Reloadable
    @TypeConverterAttribute(Boolean.class)
    @DefaultValueAttribute("false")
    EnableAutomaticHostPowerManagement,
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("10")
    HighUtilizationForEvenlyDistribute,
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("200")
    OverUtilizationForHaReservation,
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("1")
    ScaleDownForHaReservation,
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("10")
    HighUtilizationForPowerSave,
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("80")
    UtilizationThresholdInPercent,
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("2")
    CpuOverCommitDurationMinutes,
    // a default of 120% memory over commit.
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("120")
    MaxVdsMemOverCommit,
    // a default of 120% memory over commit.
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("120")
    MaxVdsMemOverCommitForServers,
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("10")
    HighVmCountForEvenGuestDistribute,
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("5")
    SpmVmGraceForEvenGuestDistribute,
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("5")
    MigrationThresholdForEvenGuestDistribute,
    @Reloadable
    @TypeConverterAttribute(Boolean.class)
    @DefaultValueAttribute("true")
    AutoInstallCertificateOnApprove,
    @Reloadable
    @TypeConverterAttribute(Boolean.class)
    @DefaultValueAttribute("true")
    DebugTimerLogging,
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("")
    AutoApprovePatterns,
    @Reloadable
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("99408929-82CF-4DC7-A532-9D998063FA95")
    AutoRegistrationDefaultClusterID,
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("3")
    StoragePoolRefreshTimeInSeconds,
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("30")
    HostStorageConnectionAndPoolRefreshTimeInSeconds,
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("3")
    StoragePoolNonOperationalResetTimeoutInMin,
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("1")
    StorageDomainFailureTimeoutInMinutes,
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("3")
    SPMFailOverAttempts,
    @Reloadable
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("ON")
    LockPolicy,
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("5")
    LockRenewalIntervalSec,
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("30")
    LeaseTimeSec,
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("10")
    IoOpTimeoutSec,
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("3")
    LeaseRetries,
    @Reloadable
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("en-us")
    VncKeyboardLayout,
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("3")
    SpmCommandFailOverRetries,
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("3")
    HsmCommandFailOverRetries,
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("1")
    SpmVCpuConsumption,
    @Reloadable
    @TypeConverterAttribute(Boolean.class)
    @DefaultValueAttribute("true")
    EnableSpiceRootCertificateValidation,
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("20480")
    VM32BitMaxMemorySizeInMB,
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("268435456")
    VM64BitMaxMemorySizeInMB,
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("0")
    BlockMigrationOnSwapUsagePercentage,
    @Reloadable
    @TypeConverterAttribute(Boolean.class)
    @DefaultValueAttribute("true")
    EnableSwapCheck,
    @Reloadable
    @TypeConverterAttribute(Boolean.class)
    @DefaultValueAttribute("true")
    SendSMPOnRunVm,
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute(" WHERE RowNum BETWEEN %1$s AND %2$s")
    @OptionBehaviourAttribute(behaviour = OptionBehaviour.ValueDependent, dependentOn = ConfigValues.DBEngine,
    realValue = "PagingSyntax")
    DBPagingSyntax,
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("Range")
    @OptionBehaviourAttribute(behaviour = OptionBehaviour.ValueDependent, dependentOn = ConfigValues.DBEngine,
    realValue = "PagingType")
    DBPagingType,
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("SELECT * FROM (SELECT *, ROW_NUMBER() OVER(%1$s) as RowNum FROM (%2$s)) as T1 ) as T2 %3$s")
    @OptionBehaviourAttribute(behaviour = OptionBehaviour.ValueDependent, dependentOn = ConfigValues.DBEngine,
    realValue = "SearchTemplate")
    DBSearchTemplate,
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute(" OFFSET {0} LIMIT {1}")
    PostgresPagingSyntax,      // used by behaviour DBPagingSyntax
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("Offset")
    PostgresPagingType,        // used by behaviour DBPagingType
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("SELECT * FROM ( {1}) as T1 {2}")
    PostgresSearchTemplate,    // used by behaviour DBSearchTemplate
    @Reloadable
    @TypeConverterAttribute(HashSet.class)
    @DefaultValueAttribute("4.4,4.5")
    @OptionBehaviourAttribute(behaviour = OptionBehaviour.CommaSeparatedVersionArray)
    SupportedVDSMVersions,
    @TypeConverterAttribute(HashSet.class)
    @DefaultValueAttribute("2.2,3.0")
    @OptionBehaviourAttribute(behaviour = OptionBehaviour.CommaSeparatedVersionArray)
    SupportedClusterLevels,
    @Reloadable
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("ENGINE")
    OvfVirtualSystemType,

    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("60")
    WaitForVdsInitInSec,

    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("60")
    OvfUpdateIntervalInMinutes,

    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("100")
    OvfItemsCountPerUpdate,

    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("GMT Standard Time")
    DefaultWindowsTimeZone,

    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("Etc/GMT")
    DefaultGeneralTimeZone,

    @Reloadable
    @TypeConverterAttribute(Boolean.class)
    @DefaultValueAttribute("false")
    SANWipeAfterDelete,

    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("/usr/share/ovirt-engine")
    DataDir,

    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("30")
    UserSessionTimeOutInterval,

    @Reloadable
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("/data/images/rhev")
    RhevhLocalFSPath,

    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("")
    UserDefinedVMProperties,

    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("")
    PredefinedVMProperties,

    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("250")
    MaxNumberOfHostsInStoragePool,

    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("15")
    MaxVmNameLengthSysprep,

    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("64")
    MaxVmNameLength,

    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("255")
    MaxVdsNameLength,

    @Reloadable
    @TypeConverterAttribute(Double.class)
    @DefaultValueAttribute("30")
    MaxStorageVdsTimeoutCheckSec,

    @Reloadable
    @TypeConverterAttribute(Double.class)
    @DefaultValueAttribute("5")
    MaxStorageVdsDelayCheckSec,

    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("300")
    DisableFenceAtStartupInSec,

    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("60")
    NicDHCPDelayGraceInMS,

    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("3")
    FindFenceProxyRetries,

    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("30")
    FindFenceProxyDelayBetweenRetriesInSec,

    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("1024")
    LogPhysicalMemoryThresholdInMB,

    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("1024")
    LogSwapMemoryThresholdInMB,

    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("3")
    FenceStartStatusRetries,

    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("60")
    FenceStartStatusDelayBetweenRetriesInSec,

    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("ILIKE")
    @OptionBehaviourAttribute(behaviour = OptionBehaviour.ValueDependent, dependentOn = ConfigValues.DBEngine,
    realValue = "LikeSyntax")
    DBLikeSyntax,
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("ILIKE")
    PostgresLikeSyntax,    // used by behaviour DBLikeSyntax

    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("")
    @OptionBehaviourAttribute(behaviour = OptionBehaviour.ValueDependent, dependentOn = ConfigValues.DBEngine,
    realValue = "I18NPrefix")
    DBI18NPrefix,
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("")
    PostgresI18NPrefix,    // used by behaviour DBI18NPrefix

    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("60000")
    UnknownTaskPrePollingLapse,

    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("3.0.0.0")
    ProductRPMVersion,

    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("10000")
    MaxAuditLogMessageLength,

    @Reloadable
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("")
    SysPrepDefaultUser,

    @Reloadable
    @TypeConverterAttribute(String.class)
    @OptionBehaviourAttribute(behaviour = OptionBehaviour.Password)
    @DefaultValueAttribute("")
    SysPrepDefaultPassword,

    @Reloadable
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("ilo3=ipmilan")
    FenceAgentMapping,

    @Reloadable
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("")
    CustomFenceAgentMapping,

    @Reloadable
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("ilo3:lanplus,power_wait=4")
    FenceAgentDefaultParams,

    @Reloadable
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("")
    CustomFenceAgentDefaultParams,

    @Reloadable
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("ilo3:lanplus=1,cipher=1,privlvl=administrator,power_wait=4;ilo4:lanplus=1,cipher=1,privlvl=administrator,power_wait=4;ipmilan:lanplus=1,cipher=1,privlvl=administrator,power_wait=4")
    FenceAgentDefaultParamsForPPC,

    @Reloadable
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("")
    CustomFenceAgentDefaultParamsForPPC,

    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("30")
    SignCertTimeoutInSeconds,

    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("20")
    ConnectToServerTimeoutInSeconds,

    @Reloadable
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("")
    IPTablesConfig,

    /**
     * Lower threshold for disk space on host to be considered low, in MB.
     */
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("1000")
    VdsLocalDisksLowFreeSpace,

    /**
     * Lower threshold for disk space on host to be considered critically low (almost out of space), in MB.
     */
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("500")
    VdsLocalDisksCriticallyLowFreeSpace,

    /**
     * The minimal size of the internal thread pool. Minimal number of threads in pool
     */
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("50")
    DefaultMinThreadPoolSize,

    /**
     * The size of the internal thread pool
     */
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("500")
    DefaultMaxThreadPoolSize,

    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("100")
    DefaultMaxThreadWaitQueueSize,

    @Reloadable
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("ovirtmgmt")
    DefaultManagementNetwork,

    @Reloadable
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("5.8")
    OvirtInitialSupportedIsoVersion,

    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("rhevh")
    OvirtIsoPrefix,

    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("80")
    QuotaThresholdCluster,

    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("80")
    QuotaThresholdStorage,

    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("20")
    QuotaGraceCluster,

    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("20")
    QuotaGraceStorage,

    // This value indicates devices that although are given to us by VDSM
    // are still treated as managed devices
    // This should be a [device=<device> type=<type>[,]]* string
    @Reloadable
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("")
    ManagedDevicesWhiteList,

    /**
     * The origin type to be used for VM and VM template creation
     */
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("OVIRT")
    OriginType,

    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("localhost:54323")
    ImageProxyAddress,

    @TypeConverterAttribute(Boolean.class)
    @DefaultValueAttribute("true")
    ImageProxySSLEnabled,

    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("54322")
    ImageDaemonPort,


    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("3600")
    ImageTransferClientTicketValidityInSeconds,

    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("300")
    ImageTransferHostTicketValidityInSeconds,

    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("60")
    ImageTransferHostTicketRefreshAllowanceInSeconds,

    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("1800")
    ImageTransferPausedLogIntervalInSeconds,

    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("60")
    UploadImageUiInactivityTimeoutInSeconds,

    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("8192")
    UploadImageChunkSizeKB,

    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("10")
    UploadImageXhrTimeoutInSeconds,

    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("3")
    UploadImageXhrRetryIntervalInSeconds,

    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("3")
    UploadImageXhrMaxRetries,

    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("3")
    SetupNetworksPollingTimeout,

    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("10")
    JobCleanupRateInMinutes,

    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("10")
    SucceededJobCleanupTimeInMinutes,

    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("60")
    FailedJobCleanupTimeInMinutes,

    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("100")
    JobPageSize,

    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("5")
    VmPoolMonitorIntervalInMinutes,

    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("5")
    VmPoolMonitorBatchSize,

    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("0 0/5 * * * ?")
    AutoRecoverySchedule,

    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("3")
    VmPoolMonitorMaxAttempts,

    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("8192")
    PayloadSize,

    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("255")
    ApplicationMode,

    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("4")
    NumberOfUSBSlots,

    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("0")
    PopulateDirectLUNDiskDescriptionWithLUNId,

    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("animation")
    WANDisableEffects,

    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("16")
    WANColorDepth,

    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("3")
    VmPoolMaxSubsequentFailures,

    @TypeConverterAttribute(Boolean.class)
    @DefaultValueAttribute("true")
    CpuPinMigrationEnabled,

    @TypeConverterAttribute(List.class)
    @DefaultValueAttribute("0,2")
    @OptionBehaviourAttribute(behaviour = OptionBehaviour.CommaSeparatedStringArray)
    DisconnectPoolOnReconstruct,

    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("120")
    NetworkConnectivityCheckTimeoutInSeconds,

    @Reloadable
    @TypeConverterAttribute(Map.class)
    @DefaultValueAttribute("{\"storage domains\":\"true\",\"hosts\":\"true\"}")
    AutoRecoveryAllowedTypes,

    /*
     * umask is required to allow only self access
     * tar is missing from vanilla fedora-18 so we use python
     */
    @Reloadable
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute(
        "umask 0077; " +
        "MYTMP=\"$(TMPDIR=\"${OVIRT_TMPDIR}\" mktemp -d -t ovirt-XXXXXXXXXX)\"; " +
        "trap \"chmod -R u+rwX \\\"${MYTMP}\\\" > /dev/null 2>&1; rm -fr \\\"${MYTMP}\\\" > /dev/null 2>&1\" 0; " +
        "tar --warning=no-timestamp -C \"${MYTMP}\" -x && " +
        "@ENVIRONMENT@ \"${MYTMP}\"/@ENTRY@ DIALOG/dialect=str:machine DIALOG/customization=bool:True"
    )
    BootstrapCommand,

    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("10000")
    BootstrapCacheRefreshInterval,
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("/usr/share/ovirt-host-deploy/interface-3")
    BootstrapPackageDirectory,
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("ovirt-host-deploy.tar")
    BootstrapPackageName,
    @Reloadable
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("ovirt-engine")
    SSHKeyAlias,
    @Reloadable
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("SHA-256")
    SSHDefaultKeyDigest,

    /*
     * Whether to allow a cluster with both Virt and Gluster services enabled
     */
    @TypeConverterAttribute(Boolean.class)
    @DefaultValueAttribute("true")
    AllowClusterWithVirtGlusterEnabled,

    @TypeConverterAttribute(Boolean.class)
    @DefaultValueAttribute("true")
    EnableMACAntiSpoofingFilterRules,

    // Gluster peer status command
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("gluster peer status --xml")
    GlusterPeerStatusCommand,

    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("1800")
    SSHInactivityHardTimeoutSeconds,

    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("virt")
    GlusterVolumeOptionGroupVirtValue,

    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("36")
    GlusterVolumeOptionOwnerUserVirtValue,

    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("36")
    GlusterVolumeOptionOwnerGroupVirtValue,

    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("/gluster-bricks")
    GlusterDefaultBrickMountPoint,

    @Reloadable
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("")
    IPTablesConfigForVirt,

    @Reloadable
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("")
    IPTablesConfigForGluster,

    @Reloadable
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("")
    IPTablesConfigSiteCustom,

    // Host time drift
    @Reloadable
    @TypeConverterAttribute(Boolean.class)
    @DefaultValueAttribute("false")
    EnableHostTimeDrift,

    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("300")
    HostTimeDriftInSec,

    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("10000")
    ThrottlerMaxWaitForVdsUpdateInMillis,

    @TypeConverterAttribute(Boolean.class)
    @DefaultValueAttribute("false")
    OnlyRequiredNetworksMandatoryForVdsSelection,

    @Reloadable
    @TypeConverterAttribute(EngineWorkingMode.class)
    @DefaultValueAttribute("Active")
    EngineMode,

    /**
     * Refresh rate (in seconds) for light-weight gluster data i.e. data that can be fetched without much of an overhead
     * on the GlusterFS processes
     */
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("5")
    GlusterRefreshRateLight,

    /**
     * Refresh rate (in seconds) for Storage Devices.
     */
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("7200")
    GlusterRefreshRateStorageDevices,

    /**
     * Refresh rate (in seconds) for heavy-weight gluster data i.e. commands to fetch such data adds a considerable
     * overhead on the GlusterFS processes.
     */
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("300")
    GlusterRefreshRateHeavy,

    /**
     * Refresh rate (in seconds) for gluster self heal info . 'gluster self heal info' command will be used to fetch
     * heal info, and it adds a considerable overhead on the GlusterFS processes.
     */
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("600")
    GlusterRefreshRateHealInfo,

    /**
     * Defines the number of history values storable by the engine for unsynced entries in gluster brick
     */
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("40")
    GlusterUnSyncedEntriesHistoryLimit,

    @TypeConverterAttribute(Boolean.class)
    @DefaultValueAttribute("true")
    GlusterSelfHealMonitoringSupported,

    @Reloadable
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("")
    BootstrapMinimalVdsmVersion,

    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("")
    MinimalETLVersion,

    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("^ovirt.*$")
    OvirtNodeOS,

    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("10")
    QuotaCacheIntervalInMinutes,

    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("60")
    MinimumPercentageToUpdateQuotaCache,

    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("95")
    LogMaxPhysicalMemoryUsedThresholdInPercentage,

    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("95")
    LogMaxSwapMemoryUsedThresholdInPercentage,

    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("95")
    LogMaxCpuUsedThresholdInPercentage,

    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("95")
    LogMaxNetworkUsedThresholdInPercentage,

    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("256")
    LogMinFreeSwapThresholdInMB,

    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("95")
    LogMaxSwapUsedThresholdInPercentage,

    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("9")
    PgMajorRelease,

    @TypeConverterAttribute(List.class)
    @OptionBehaviourAttribute(behaviour = OptionBehaviour.CommaSeparatedStringArray)
    @DefaultValueAttribute("ar,da,de,de-ch,en-gb,en-us,es,et,fi,fo,fr,fr-be,fr-ca,fr-ch,hr,hu,is,it,ja,lt,lv,mk,nl,nl-be,no,pl,pt,pt-br,ru,sl,sv,th,tr")
    VncKeyboardLayoutValidValues,

    @Reloadable
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("")
    SpiceProxyDefault,

    @Reloadable
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("")
    RemoteViewerSupportedVersions,

    @Reloadable
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("")
    RemoteViewerNewerVersionUrl,

    @Reloadable
    @TypeConverterAttribute(Boolean.class)
    @DefaultValueAttribute("true")
    RemapCtrlAltDelDefault,

    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("600")
    GlusterRefreshRateHooks,

    @TypeConverterAttribute(Boolean.class)
    @DefaultValueAttribute("false")
    GlusterServicesEnabled,

    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("10")
    GlusterTaskMinWaitForCleanupInMins,

    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("")
    GlusterTunedProfile,

    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("/,/home,/boot")
    GlusterStorageDeviceListMountPointsToIgnore,

    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("swap")
    GlusterStorageDeviceListFileSystemTypesToIgnore,

    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("3600")
    GlusterRefreshRateGeoRepDiscoveryInSecs,

    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("300")
    GlusterRefreshRateGeoRepStatusInSecs,

    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("300")
    GlusterRefreshRateSnapshotDiscovery,

    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("gluster_shared_storage")
    GlusterMetaVolumeName,

    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("AttestationService/resources/PollHosts")
    PollUri,

    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("TrustStore.jks")
    AttestationTruststore,

    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("8443")
    AttestationPort,

    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("password")
    @OptionBehaviourAttribute(behaviour = OptionBehaviour.Password)
    AttestationTruststorePass,

    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("")
    AttestationServer,

    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("10")
    AttestationFirstStageSize,

    @TypeConverterAttribute(Boolean.class)
    @DefaultValueAttribute("true")
    SecureConnectionWithOATServers,

    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("20")
    AttestationSecondStageSize,

    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("30")
    GlusterRefreshRateTasks,

    @TypeConverterAttribute(Boolean.class)
    @DefaultValueAttribute("false")
    EnableDeprecatedClientModeSpicePlugin,

    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("Auto")
    ClientModeSpiceDefault,

    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("Native")
    ClientModeVncDefault,

    @Reloadable
    @TypeConverterAttribute(Double.class)
    @DefaultValueAttribute("20")
    DelayResetForSpmInSeconds,

    @Reloadable
    @TypeConverterAttribute(Double.class)
    @DefaultValueAttribute("0.5")
    DelayResetPerVmInSeconds,

    @Reloadable
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("/usr/bin/vdsm-tool vdsm-id")
    GetVdsmIdByVdsmToolCommand,

    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("Auto")
    ClientModeRdpDefault,

    @Reloadable
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("Off")
    WebSocketProxy,

    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("120")
    WebSocketProxyTicketValiditySeconds,

    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("")
    CustomDeviceProperties,

    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("")
    PreDefinedNetworkCustomProperties,

    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("")
    UserDefinedNetworkCustomProperties,

    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("/osinfo.conf.d")
    OsRepositoryConfDir,

    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("/usr/bin/vdsm-tool service-restart vdsmd")
    SshSoftFencingCommand,

    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("systemctl reboot")
    SshHostRebootCommand,

    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("/sbin/poweroff")
    SshVdsPowerdownCommand,

    @TypeConverterAttribute(List.class)
    @DefaultValueAttribute("rhel6.2.0,pc-1.0")
    @OptionBehaviourAttribute(behaviour = OptionBehaviour.CommaSeparatedStringArray)
    ClusterEmulatedMachines,

    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("1024")
    MaxAverageNetworkQoSValue,

    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("2048")
    MaxPeakNetworkQoSValue,

    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("10240")
    MaxBurstNetworkQoSValue,

    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("100")
    MaxHostNetworkQosShares,

    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("10")
    QoSInboundAverageDefaultValue,

    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("10")
    QoSInboundPeakDefaultValue,

    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("100")
    QoSInboundBurstDefaultValue,

    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("3")
    IterationsWithBalloonProblem,

    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("10")
    QoSOutboundAverageDefaultValue,

    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("10")
    QoSOutboundPeakDefaultValue,

    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("100")
    QoSOutboundBurstDefaultValue,

    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("http://localhost:18781/")
    ExternalSchedulerServiceURL,

    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("100")
    ExternalSchedulerConnectionTimeout,

    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("120000")
    ExternalSchedulerResponseTimeout,

    @TypeConverterAttribute(Boolean.class)
    @DefaultValueAttribute("false")
    ExternalSchedulerEnabled,

    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("1000")
    MaxSchedulerWeight,

    @TypeConverterAttribute(Boolean.class)
    @DefaultValueAttribute("true")
    UseFqdnForRdpIfAvailable,

    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("15")
    DwhHeartBeatInterval,

    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("0")
    DisconnectDwh,

    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("20")
    GlanceImageListSize,

    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("500")
    GlanceImageTotalListSize,

    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("300")
    HostPreparingForMaintenanceIdleTime,

    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("10")
    SpeedOptimizationSchedulingThreshold,

    @TypeConverterAttribute(Boolean.class)
    @DefaultValueAttribute("false")
    SchedulerAllowOverBooking,

    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("10")
    SchedulerOverBookingThreshold,

    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("2")
    GlusterPeerStatusRetries,

    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("1")
    AutoStartVmsRunnerIntervalInSeconds,

    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("30")
    RetryToRunAutoStartVmIntervalInSeconds,

    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("10")
    MaxNumOfTriesToRunFailedAutoStartVm,

    /**
     * Value representing maximum number of milliseconds a VM can be down during live migration.
     * Default value of 0 means this value will not be sent to VDSM at all and the currently configured value on
     * the VDSM will be used.
     */
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("0")
    DefaultMaximumMigrationDowntime,

    @TypeConverterAttribute(Map.class)
    @DefaultValueAttribute("{\"x86\":\"true\",\"ppc\":\"false\"}")
    HotPlugCpuSupported,

    @TypeConverterAttribute(Map.class)
    @DefaultValueAttribute("{\"x86\":\"false\",\"ppc\":\"false\"}")
    HotUnplugCpuSupported,

    @TypeConverterAttribute(Map.class)
    @DefaultValueAttribute("{\"x86\":\"true\",\"ppc\":\"true\"}")
    HotPlugMemorySupported,

    @TypeConverterAttribute(Map.class)
    @DefaultValueAttribute("{\"x86\":\"false\",\"ppc\":\"false\"}")
    HotUnplugMemorySupported,

    @TypeConverterAttribute(Map.class)
    @DefaultValueAttribute("{\"undefined\":\"true\",\"x86\":\"true\",\"ppc\":\"true\"}")
    IsMigrationSupported,

    @TypeConverterAttribute(Map.class)
    @DefaultValueAttribute("{\"undefined\":\"true\",\"x86\":\"true\",\"ppc\":\"true\"}")
    IsMemorySnapshotSupported,

    @TypeConverterAttribute(Map.class)
    @DefaultValueAttribute("{\"undefined\":\"true\",\"x86\":\"true\",\"ppc\":\"true\"}")
    IsSuspendSupported,

    @TypeConverterAttribute(SerialNumberPolicy.class)
    @DefaultValueAttribute("HOST_ID")
    DefaultSerialNumberPolicy,

    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("Dummy serial number.")
    DefaultCustomSerialNumber,

    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("5")
    UploadFileMaxTimeInMinutes,

    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("5")
    RetrieveDataMaxTimeInMinutes,

    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("2")
    StorageDomainOvfStoreCount,

    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("en_US")
    DefaultSysprepLocale,

    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("0")
    UserSessionHardLimit,

    @TypeConverterAttribute(Boolean.class)
    @DefaultValueAttribute("false")
    PMHealthCheckEnabled,

    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("3600")
    PMHealthCheckIntervalInSec,

    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("RANDOM")
    ClusterRequiredRngSourcesDefault,

    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("1500")
    DefaultMTU,

    /**
     * Defines the hostname(s) or IP address(es) to send fence_kdump messages to. If empty, engine FQDN is used.
     */
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("")
    FenceKdumpDestinationAddress,

    /**
     * Defines the port to send fence_kdump messages to.
     */
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("7410")
    FenceKdumpDestinationPort,

    /**
     * Defines the interval in seconds between messages sent by fence_kdump_send.
     */
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("5")
    FenceKdumpMessageInterval,

    /**
     * Defines max timeout in seconds since last heartbeat to consider fence_kdump listener alive.
     */
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("90")
    FenceKdumpListenerTimeout,

    /**
     * Defines maximum timeout in seconds to wait until 1st message from kdumping host is received (host kdump flow
     * started).
     */
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("30")
    KdumpStartedTimeout,

    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("300")
    AlertOnNumberOfLVs,

    /**
     * Defines the parameter name used by the agent script to delay host on/off
     */
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("apc=power_wait,apc_snmp=power_wait,bladecenter=power_wait,cisco_ucs=power_wait,drac5=power_wait,drac7=power_wait,eps=delay,hpblade=power_wait,ilo=power_wait,ilo2=power_wait,ilo3=power_wait,ilo4=power_wait,ipmilan=power_wait,rsa=power_wait,rsb=power_wait,wti=power_wait")
    FencePowerWaitParam,

    /**
     * Defines the parameter name used by the custom agent script to delay host on/off
     */
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("")
    CustomFencePowerWaitParam,

    /**
     * If the values is {@code true} then the RESTAPI will accept requests to create CSRF protected sessions, otherwise
     * all sessions will be unprotected, regardless of what the client requests.
     */
    @TypeConverterAttribute(Boolean.class)
    @DefaultValueAttribute("false")
    CSRFProtection,

    /**
     * If the values is {@code true} then the RESTAPI will support CORS (Cross Origin Resource Sharing).
     */
    @TypeConverterAttribute(Boolean.class)
    @DefaultValueAttribute("false")
    CORSSupport,

    /**
     * If CORS is enabled (with the {@code CORSSupport} parameter set to {@code true} then this indicates which are the
     * allowed origins.
     */
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("")
    CORSAllowedOrigins,

    /**
     * Interval in seconds after which is safe to check host storage lease status when host stopped responding
     * to monitoring
     */
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("90")
    HostStorageLeaseAliveCheckingInterval,

    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("1000000")
    MaxThroughputUpperBoundQosValue,

    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("1000000")
    MaxReadThroughputUpperBoundQosValue,

    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("1000000")
    MaxWriteThroughputUpperBoundQosValue,

    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("1000000")
    MaxIopsUpperBoundQosValue,

    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("1000000")
    MaxReadIopsUpperBoundQosValue,

    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("1000000")
    MaxWriteIopsUpperBoundQosValue,

    /**
     * UseHostNameIdentifier this will run external headers on STOMP connect frame, in order to identify hosts
     */
    @TypeConverterAttribute(Boolean.class)
    @DefaultValueAttribute("false")
    UseHostNameIdentifier,

    /**
    * Defines the number of history values storable by the engine for cpu/network/memory usage of a VM
    */
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("40")
    UsageHistoryLimit,

    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("100")
    MaxCpuLimitQosValue,

    @Reloadable
    @TypeConverterAttribute(List.class)
    @DefaultValueAttribute("")
    @OptionBehaviourAttribute(behaviour = OptionBehaviour.CommaSeparatedStringArray)
    UnsupportedLocalesFilterOverrides,

    /**
     * Defines the parameter name used by numa migration on/off
     */
    @TypeConverterAttribute(Boolean.class)
    @DefaultValueAttribute("false")
    SupportNUMAMigration,

    @TypeConverterAttribute(List.class)
    @DefaultValueAttribute("")
    @OptionBehaviourAttribute(behaviour = OptionBehaviour.CommaSeparatedStringArray)
    UnsupportedLocalesFilter,

    @TypeConverterAttribute(Boolean.class)
    @DefaultValueAttribute("false")
    DefaultAutoConvergence,

    @TypeConverterAttribute(Boolean.class)
    @DefaultValueAttribute("false")
    DefaultMigrationCompression,

    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("6")
    BackupCheckPeriodInHours,

    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("1")
    BackupAlertPeriodInDays,

    @TypeConverterAttribute(List.class)
    @DefaultValueAttribute("pci,scsi,usb_device")
    @OptionBehaviourAttribute(behaviour = OptionBehaviour.CommaSeparatedStringArray)
    HostDevicePassthroughCapabilities,

    /**
     * A comma delimited list of package names for checking if updates are available for the host
     */
    @TypeConverterAttribute(List.class)
    @DefaultValueAttribute("ioprocess,mom,libvirt,lvm2,ovirt-imageio-common,ovirt-imageio-daemon,ovirt-vmconsole,ovirt-vmconsole-host,python-ioprocess,qemu-kvm,qemu-img,sanlock,vdsm,vdsm-cli")
    @OptionBehaviourAttribute(behaviour = OptionBehaviour.CommaSeparatedStringArray)
    PackageNamesForCheckUpdate,

    /**
     * A comma delimited list of package names provided by the user for checking if updates are available for the host,
     * and for updating in addition to {@code HostPackageNamesForCheckUpdate}
     */
    @TypeConverterAttribute(List.class)
    @DefaultValueAttribute("")
    @OptionBehaviourAttribute(behaviour = OptionBehaviour.CommaSeparatedStringArray)
    UserPackageNamesForCheckUpdate,

    /**
     * A comma delimited list of package names for checking if updates are available for ovirt-node
     */
    @TypeConverterAttribute(List.class)
    @DefaultValueAttribute("ovirt-node-ng-image-update")
    @OptionBehaviourAttribute(behaviour = OptionBehaviour.CommaSeparatedStringArray)
    OvirtNodePackageNamesForCheckUpdate,

    /**
     * The interval in hours of checking for available updates on the host.
     */
    @TypeConverterAttribute(Double.class)
    @DefaultValueAttribute("24")
    HostPackagesUpdateTimeInHours,

    /**
     * The interval in hours of checking the validity of the host's certification
     */
    @TypeConverterAttribute(Double.class)
    @DefaultValueAttribute("24")
    CertificationValidityCheckTimeInHours,

    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("30")
    CertExpirationWarnPeriodInDays,

    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("7")
    CertExpirationAlertPeriodInDays,

    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("10000")
    VMConsoleTicketTolerance,

    @TypeConverterAttribute(Boolean.class)
    @DefaultValueAttribute("false")
    DataCenterWithoutSpm,

    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("16")
    MaxMemorySlots,

    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("256")
    HotPlugMemoryMultiplicationSizeMb,

    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("HostedEngine")
    HostedEngineVmName,

    @TypeConverterAttribute(Boolean.class)
    @DefaultValueAttribute("true")
    ChangeNetworkUnderBridgeInUseSupported,

    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("127")
    MaxIoThreadsPerVm,

    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("1048576")
    VMPpc64BitMaxMemorySizeInMB,

    @TypeConverterAttribute(Boolean.class)
    @DefaultValueAttribute("true")
    AutoImportHostedEngine,

    @TypeConverterAttribute(Boolean.class)
    @DefaultValueAttribute("true")
    SriovHotPlugSupported,

    @TypeConverterAttribute(Boolean.class)
    @DefaultValueAttribute("true")
    AdPartnerMacSupported,

    @TypeConverterAttribute(Boolean.class)
    @DefaultValueAttribute("true")
    OvsSupported,

    @TypeConverterAttribute(Boolean.class)
    @DefaultValueAttribute("true")
    AllowEditingHostedEngine,

    @TypeConverterAttribute(Boolean.class)
    @DefaultValueAttribute("true")
    DisplayUncaughtUIExceptions,

    @TypeConverterAttribute(Boolean.class)
    @DefaultValueAttribute("true")
    MigrationPoliciesSupported,

    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("")
    MigrationPolicies,

    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("20480")
    HostedEngineConfigDiskSizeInBytes,

    @TypeConverterAttribute(Boolean.class)
    @DefaultValueAttribute("true")
    Ipv6Supported,

    Invalid
}
