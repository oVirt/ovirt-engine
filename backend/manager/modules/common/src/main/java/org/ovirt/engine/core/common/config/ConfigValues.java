package org.ovirt.engine.core.common.config;

import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.common.EngineWorkingMode;

public enum ConfigValues {
    @Reloadable
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("9b9002d1-ec33-4083-8a7b-31f6b8931648")
    AdUserId(0),
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("SQLServer")
    DBEngine(1),
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("administrator")
    AdUserName(5),
    @TypeConverterAttribute(Map.class)
    @DefaultValueAttribute("EXAMPLE.COM:123456")
    @OptionBehaviourAttribute(behaviour = OptionBehaviour.DomainsPasswordMap)
    AdUserPassword(8),
    @Reloadable
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("123456")
    @OptionBehaviourAttribute(behaviour = OptionBehaviour.Password)
    LocalAdminPassword(9),
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("EXAMPLE.COM")
    DomainName(10),
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("180")
    vdsTimeout(11),
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("2")
    VdsRefreshRate(12),
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("10")
    AsyncTaskPollingRate(13),

    /**
     * The rate (in seconds) to refresh the cache that holds the asynchronous tasks' statuses.
     */
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("60")
    AsyncTaskStatusCacheRefreshRateInSeconds(15),

    /**
     * The period of time (in minutes) to hold the asynchronous tasks' statuses in the asynchronous tasks cache.
     */
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("5")
    AsyncTaskStatusCachingTimeInMinutes(16),
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("3000")
    AsyncTaskZombieTaskLifeInMinutes(17),
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("3600")
    UserRefreshRate(18),
    @TypeConverterAttribute(java.util.Date.class)
    @DefaultValueAttribute("03:35:35")
    AuditLogCleanupTime(19),
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("30")
    AuditLogAgingThreashold(20),
    @Reloadable
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("drac5,ilo,ipmilan,rsa,bladecenter,alom,apc,eps,wti,rsb")
    VdsFenceType(24),
    @Reloadable
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("alom:secure=secure,port=ipport;apc:secure=secure,port=ipport,slot=port;bladecenter:secure=secure,port=ipport,slot=port;drac5:secure=secure,port=ipport;eps:slot=port;ilo:secure=ssl,port=ipport;ipmilan:;rsa:secure=secure,port=ipport;rsb:;wti:secure=secure,port=ipport,slot=port")
    VdsFenceOptionMapping(26),
    @Reloadable
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("secure=bool,port=int,slot=int")
    VdsFenceOptionTypes(27),
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("3")
    FenceStopStatusRetries(28),
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("60")
    FenceStopStatusDelayBetweenRetriesInSec(29),
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("180")
    FenceQuietTimeBetweenOperationsInSec(30),
    @Reloadable
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("/data/updates/ovirt-node-image.iso")
    oVirtUploadPath(32),
    @Reloadable
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("/usr/share/rhev-hypervisor")
    oVirtISOsRepositoryPath(33),
    @Reloadable
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("/usr/share/vdsm-reg/vdsm-upgrade")
    oVirtUpgradeScriptName(34),
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("5")
    VdsCertificateValidityInYears(38),
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("USERID")
    UserId(44),
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("PASSW0RD")
    @OptionBehaviourAttribute(behaviour = OptionBehaviour.Password)
    Password(45),
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("100")
    SearchResultsLimit(48),
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("2")
    VDSAttemptsToResetCount(49),
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("30")
    TimeoutToResetVdsInSeconds(50),
    @Reloadable
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("")
    ProductKey2003(52),
    @Reloadable
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("")
    ProductKey2003x64(53),
    @Reloadable
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("")
    ProductKey2008(54),
    @Reloadable
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("")
    ProductKey2008x64(55),
    @Reloadable
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("")
    ProductKey2008R2(56),
    @Reloadable
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("")
    ProductKeyWindow7(57),
    @Reloadable
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("")
    ProductKeyWindow7x64(58),
    @Reloadable
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("")
    ProductKey(59),
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("10")
    FreeSpaceLow(60),
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("5")
    FreeSpaceCriticalLowInGB(61),
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("")
    MacPoolRanges(62),
    @TypeConverterAttribute(Boolean.class)
    @DefaultValueAttribute("false")
    HasCluster(63),
    @Reloadable
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("1.0.0.0")
    VdcVersion(64),
    @Reloadable
    @TypeConverterAttribute(Boolean.class)
    @DefaultValueAttribute("true")
    SSLEnabled(65),
    @Reloadable
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("DEFAULT")
    CipherSuite(66),
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("40")
    StoragePoolNameSizeLimit(69),
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("50")
    StorageDomainNameSizeLimit(70),
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("3")
    NumberOfFailedRunsOnVds(73),
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("30")
    TimeToReduceFailedRunOnVdsInMinutes(74),
    /**
     * In default rerun Vm on all Available desktops
     */
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("3")
    MaxRerunVmOnVdsCount(75),
    @Reloadable
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("")
    SysPrepXPPath(79),
    @Reloadable
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("")
    SysPrep2K3Path(80),
    @Reloadable
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("")
    SysPrep2K8Path(81),
    @Reloadable
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("")
    SysPrep2K8x64Path(82),
    @Reloadable
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("")
    SysPrep2K8R2Path(83),
    @Reloadable
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("")
    SysPrepWindows7Path(84),
    @Reloadable
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("")
    SysPrepWindows7x64Path(85),
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("1000")
    MaxVmsInPool(87),
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("5")
    VmPoolLeaseDays(88),
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("12:00")
    VmPoolLeaseStartTime(89),
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("12:00")
    VmPoolLeaseEndTime(90),
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("LDAP")
    AuthenticationMethod(92),
    @Reloadable
    @TypeConverterAttribute(java.util.List.class)
    @DefaultValueAttribute("1,2,4")
    @OptionBehaviourAttribute(behaviour = OptionBehaviour.CommaSeparatedStringArray)
    ValidNumOfMonitors(93),
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("16")
    MaxNumOfVmCpus(94),
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("16")
    MaxNumOfVmSockets(95),
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("16")
    MaxNumOfCpuPerSocket(96),
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("1")
    NumberVmRefreshesBeforeSave(97),
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("60")
    AutoRepoDomainRefreshTime(99),
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("certs/ca.pem")
    CACertificatePath(100),
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("ca")
    CABaseDirectory(101),
    @Reloadable
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("certs/engine.cer")
    CertificateFileName(102),
    @TypeConverterAttribute(Boolean.class)
    @DefaultValueAttribute("true")
    InstallVds(108),
    @Reloadable
    @TypeConverterAttribute(Boolean.class)
    @DefaultValueAttribute("true")
    EnableUSBAsDefault(110),
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("300")
    SSHInactivityTimoutSeconds(111),
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("120")
    ServerRebootTimeout(112),
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("40")
    VmGracefulShutdownTimeout(113),
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("100")
    VmPriorityMaxValue(114),
    @Reloadable
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("Shutting Down")
    VmGracefulShutdownMessage(115),
    @Reloadable
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("smain,sinputs,scursor,splayback,srecord,sdisplay,ssmartcard,susbredir")
    SpiceSecureChannels(117),
    @Reloadable
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("shift+f12")
    SpiceReleaseCursorKeys(119),
    @Reloadable
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("shift+f11")
    SpiceToggleFullScreenKeys(120),
    @Reloadable
    @TypeConverterAttribute(Boolean.class)
    @DefaultValueAttribute("true")
    SpiceUsbAutoShare(121),
    @TypeConverterAttribute(Boolean.class)
    @DefaultValueAttribute("true")
    UseSecureConnectionWithServers(123),
    @Reloadable
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("oVirt")
    OrganizationName(125),
    @Reloadable
    @TypeConverterAttribute(Boolean.class)
    @DefaultValueAttribute("true")
    IsMultilevelAdministrationOn(127),
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("3")
    VdsRecoveryTimeoutInMintues(128),
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("8192")
    MaxBlockDiskSize(129),
    // the order is- {level}:{name}:{flags}:{vdsm};
    // {level}:{name}:{flags}:{vdsm};1:cpu_name:cpu_flags,..,:vdsm_exec,+..,-..;..
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("1:pentium3:vmx:pentium3;2:intel-qemu64-nx:vmx,sse2:qemu64,-nx,+sse2;3:intel-qemu64:vmx,sse2,nx:qemu64,+sse2;2:amd-qemu64-nx:svm,sse2:qemu64,-nx,+sse2;3:amd-qemu64:svm,sse2,nx:qemu64,+sse2")
    ServerCPUList(130),
    @Reloadable
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("RHEV-Agent")
    AgentAppName(132),
    @Reloadable
    @TypeConverterAttribute(Map.class)
    @DefaultValueAttribute("{\"windows\":\"RHEV-Spice\",\"linux\":\"xorg-x11-drv-qxl\"}")
    SpiceDriverNameInGuest(133),
    @Reloadable
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("RHEV-toolsSetup_")
    GuestToolsSetupIsoPrefix(134),
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("10")
    VcpuConsumptionPercentage(135),
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("/images/import/")
    ImportDefaultPath(138),
    @Reloadable
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("None")
    VdsSelectionAlgorithm(139),
    @TypeConverterAttribute(Boolean.class)
    @DefaultValueAttribute("true")
    EnableVdsLoadBalancing(140),
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("1")
    VdsLoadBalancingeIntervalInMinutes(141),
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("0")
    LowUtilizationForEvenlyDistribute(142),
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("6")
    LowUtilizationForPowerSave(143),
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("10")
    HighUtilizationForEvenlyDistribute(144),
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("10")
    HighUtilizationForPowerSave(145),
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("80")
    UtilizationThresholdInPercent(146),
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("2")
    CpuOverCommitDurationMinutes(147),
    // a default of 120% memory over commit.
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("120")
    MaxVdsMemOverCommit(148),
    // a default of 120% memory over commit.
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("120")
    MaxVdsMemOverCommitForServers(149),
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("1")
    PowerClientMaxNumberOfConcurrentVMs(151),
    @Reloadable
    @TypeConverterAttribute(Boolean.class)
    @DefaultValueAttribute("false")
    PowerClientAutoMigrateToPowerClientOnConnect(153),
    @Reloadable
    @TypeConverterAttribute(Boolean.class)
    @DefaultValueAttribute("false")
    PowerClientAutoMigrateFromPowerClientToVdsWhenConnectingFromRegularClient(154),
    @Reloadable
    @TypeConverterAttribute(Boolean.class)
    @DefaultValueAttribute("true")
    PowerClientSpiceDynamicCompressionManagement(156),
    @TypeConverterAttribute(Boolean.class)
    @DefaultValueAttribute("true")
    PowerClientAutoAdjustMemory(157),
    @TypeConverterAttribute(Boolean.class)
    @DefaultValueAttribute("true")
    PowerClientAutoAdjustMemoryBaseOnAvailableMemory(158),
    @Reloadable
    @TypeConverterAttribute(Boolean.class)
    @DefaultValueAttribute("true")
    PowerClientAutoAdjustMemoryLog(159),
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("256")
    PowerClientAutoAdjustMemoryGeneralReserve(160),
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("100")
    PowerClientAutoAdjustMemorySpicePerSessionReserve(161),
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("50")
    PowerClientAutoAdjustMemorySpicePerMonitorReserve(162),
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("3712")
    PowerClientAutoAdjustMemoryMaxMemory(163),
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("64")
    PowerClientAutoAdjustMemoryModulus(164),
    @Reloadable
    @TypeConverterAttribute(Boolean.class)
    @DefaultValueAttribute("true")
    PowerClientAutoInstallCertificateOnApprove(165),
    @Reloadable
    @TypeConverterAttribute(Boolean.class)
    @DefaultValueAttribute("false")
    PowerClientAllowRunningGuestsWithoutTools(166),
    @Reloadable
    @TypeConverterAttribute(Boolean.class)
    @DefaultValueAttribute("false")
    PowerClientLogDetection(167),
    @Reloadable
    @TypeConverterAttribute(Boolean.class)
    @DefaultValueAttribute("true")
    DebugTimerLogging(169),
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("")
    PowerClientAutoApprovePatterns(171),
    @Reloadable
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("99408929-82CF-4DC7-A532-9D998063FA95")
    PowerClientAutoRegistrationDefaultVdsGroupID(172),
    @Reloadable
    @TypeConverterAttribute(Boolean.class)
    @DefaultValueAttribute("false")
    PowerClientRunVmShouldVerifyPendingVMsAsWell(173),
    @Reloadable
    @TypeConverterAttribute(Boolean.class)
    @DefaultValueAttribute("false")
    PowerClientDedicatedVmLaunchOnVdsWhilePowerClientStarts(174),
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("3")
    StoragePoolRefreshTimeInSeconds(179),
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("3")
    StoragePoolNonOperationalResetTimeoutInMin(180),
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("1")
    StorageDomainFalureTimeoutInMinutes(181),
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("")
    ComputerADPaths(182),
    @Reloadable
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("System,Sparse,COW,true;Data,Preallocated,RAW,false;Shared,Preallocated,RAW,false;Swap,Preallocated,RAW,false;Temp,Sparse,COW,false")
    DiskConfigurationList(191),
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("3")
    SPMFailOverAttempts(192),
    @Reloadable
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("ON")
    LockPolicy(193),
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("5")
    LockRenewalIntervalSec(194),
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("30")
    LeaseTimeSec(195),
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("10")
    IoOpTimeoutSec(196),
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("3")
    LeaseRetries(197),
    @Reloadable
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("en-us")
    VncKeyboardLayout(203),
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("3")
    SpmCommandFailOverRetries(204),
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("1")
    SpmVCpuConsumption(205),
    @Reloadable
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("")
    RedirectServletReportsPage(251),
    @Reloadable
    @TypeConverterAttribute(Boolean.class)
    @DefaultValueAttribute("true")
    EnableSpiceRootCertificateValidation(206),
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("100000")
    MaxMacsCountInPool(207),
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("10")
    NumberOfVmsForTopSizeVms(208),
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("256")
    VMMinMemorySizeInMB(210),
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("20480")
    VM32BitMaxMemorySizeInMB(211),
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("262144")
    VM64BitMaxMemorySizeInMB(212),
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("0")
    BlockMigrationOnSwapUsagePercentage(213),
    @Reloadable
    @TypeConverterAttribute(Boolean.class)
    @DefaultValueAttribute("true")
    EnableSwapCheck(214),
    @Reloadable
    @TypeConverterAttribute(Boolean.class)
    @DefaultValueAttribute("true")
    SendSMPOnRunVm(215),
    @Reloadable
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("pc")
    EmulatedMachine(216),
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute(" WHERE RowNum BETWEEN %1$s AND %2$s")
    @OptionBehaviourAttribute(behaviour = OptionBehaviour.ValueDependent, dependentOn = ConfigValues.DBEngine,
    realValue = "PagingSyntax")
    DBPagingSyntax(217),
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("Range")
    @OptionBehaviourAttribute(behaviour = OptionBehaviour.ValueDependent, dependentOn = ConfigValues.DBEngine,
    realValue = "PagingType")
    DBPagingType(218),
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("SELECT * FROM (SELECT *, ROW_NUMBER() OVER(%1$s) as RowNum FROM (%2$s)) as T1 ) as T2 %3$s")
    @OptionBehaviourAttribute(behaviour = OptionBehaviour.ValueDependent, dependentOn = ConfigValues.DBEngine,
    realValue = "SearchTemplate")
    DBSearchTemplate(219),
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute(" OFFSET {0} LIMIT {1}")
    PostgresPagingSyntax(223),      // used by behaviour DBPagingSyntax
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("Offset")
    PostgresPagingType(224),        // used by behaviour DBPagingType
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("SELECT * FROM ( {1}) as T1 {2}")
    PostgresSearchTemplate(225),    // used by behaviour DBSearchTemplate
    @Reloadable
    @TypeConverterAttribute(java.util.HashSet.class)
    @DefaultValueAttribute("4.4,4.5")
    @OptionBehaviourAttribute(behaviour = OptionBehaviour.CommaSeparatedVersionArray)
    SupportedVDSMVersions(227),
    @TypeConverterAttribute(java.util.HashSet.class)
    @DefaultValueAttribute("2.2,3.0")
    @OptionBehaviourAttribute(behaviour = OptionBehaviour.CommaSeparatedVersionArray)
    SupportedClusterLevels(228),
    @Reloadable
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("ENGINE")
    OvfVirtualSystemType(229),

    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("60")
    WaitForVdsInitInSec(230),

    // JTODO - temporarily using values from 256 for Java specific options
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("keys/engine.p12")
    keystoreUrl(256),

    // TODO: handle password behavior
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("NoSoup4U")
    // @OptionBehaviourAttribute(behaviour = OptionBehaviour.Password)
    keystorePass(257),

    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute(".truststore")
    TruststoreUrl(258),

    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("NoSoup4U")
    @OptionBehaviourAttribute(behaviour = OptionBehaviour.Password)
    TruststorePass(259),

    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("(GMT) GMT Standard Time")
    DefaultTimeZone(260),

    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("389")
    LDAPServerPort(263),

    @Reloadable
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("SignReq.bat")
    SignScriptName(264),

    // PKCS#12 store contains only one key
    // Alias is almost always "1"
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("1")
    CertAlias(265),

    @Reloadable
    @TypeConverterAttribute(Boolean.class)
    @DefaultValueAttribute("false")
    SANWipeAfterDelete(267),

    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("/etc/ovirt-engine")
    ConfigDir(267),

    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("/usr/share/ovirt-engine")
    DataDir(268),

    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("30")
    UserSessionTimeOutInterval(269),

    @Reloadable
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("/data/images/rhev")
    RhevhLocalFSPath(290),

    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("default:GSSAPI")
    LDAPSecurityAuthentication(271),

    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("")
    UserDefinedVMProperties(272),

    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("")
    PredefinedVMProperties(273),

    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("250")
    MaxNumberOfHostsInStoragePool(274),

    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("15")
    MaxVmNameLengthWindows(276),

    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("64")
    MaxVmNameLengthNonWindows(277),

    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("255")
    MaxVdsNameLength(278),

    @Reloadable
    @TypeConverterAttribute(Double.class)
    @DefaultValueAttribute("30")
    MaxStorageVdsTimeoutCheckSec(279),

    @Reloadable
    @TypeConverterAttribute(Double.class)
    @DefaultValueAttribute("5")
    MaxStorageVdsDelayCheckSec(280),

    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("300")
    DisableFenceAtStartupInSec(281),

    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("60")
    NicDHCPDelayGraceInMS(282),

    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("3")
    FindFenceProxyRetries(283),

    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("30")
    FindFenceProxyDelayBetweenRetriesInSec(284),

    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("1024")
    LogPhysicalMemoryThresholdInMB(285),

    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("auth-conf")
    SASL_QOP(286),

    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("%JBOSS_HOME%\\standalone\\deployments\\engine.ear")
    ENGINEEarLib(287),

    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("1000")
    LdapQueryPageSize(288),

    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("100")
    MaxLDAPQueryPartsNumber(289),

    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("3")
    FenceStartStatusRetries(290),

    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("60")
    FenceStartStatusDelayBetweenRetriesInSec(291),

    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("30")
    LDAPQueryTimeout(292),

    @Reloadable
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("default,ich6")
    DesktopAudioDeviceType(294),

    @Reloadable
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("0")
    SupportedStorageFormats(296),

    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("ILIKE")
    @OptionBehaviourAttribute(behaviour = OptionBehaviour.ValueDependent, dependentOn = ConfigValues.DBEngine,
    realValue = "LikeSyntax")
    DBLikeSyntax(298),
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("ILIKE")
    PostgresLikeSyntax(300),    // used by behaviour DBLikeSyntax

    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("")
    @OptionBehaviourAttribute(behaviour = OptionBehaviour.ValueDependent, dependentOn = ConfigValues.DBEngine,
    realValue = "I18NPrefix")
    DBI18NPrefix(301),
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("")
    PostgresI18NPrefix(303),    // used by behaviour DBI18NPrefix

    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("60000")
    UknownTaskPrePollingLapse(304),

    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("")
    LdapServers(305),

    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("/var/lock/ovirt-engine/.openssl.exclusivelock")
    SignLockFile(306),

    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("3.0.0.0")
    ProductRPMVersion(307),

    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("10000")
    MaxAuditLogMessageLength(308),

    @Reloadable
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("")
    SysPrepDefaultUser(309),

    @Reloadable
    @TypeConverterAttribute(String.class)
    @OptionBehaviourAttribute(behaviour = OptionBehaviour.Password)
    @DefaultValueAttribute("")
    SysPrepDefaultPassword(310),

    @Reloadable
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("ilo3=ipmilan")
    FenceAgentMapping(311),

    @Reloadable
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("ilo3:lanplus,power_wait=4")
    FenceAgentDefaultParams(312),

    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("admin")
    AdminUser(313),

    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("internal")
    AdminDomain(314),

    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("")
    @OptionBehaviourAttribute(behaviour = OptionBehaviour.Password)
    AdminPassword(315),

    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("30")
    SignCertTimeoutInSeconds(316),

    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("600")
    OtpExpirationInSeconds(317),

    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("20")
    ConnectToServerTimeoutInSeconds(318),

    @Reloadable
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("")
    IPTablesConfig(319),

    /**
     * Lower threshold for disk space on host to be considered low, in MB.
     */
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("1000")
    VdsLocalDisksLowFreeSpace(321),

    /**
     * Lower threshold for disk space on host to be considered critically low (almost out of space), in MB.
     */
    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("500")
    VdsLocalDisksCriticallyLowFreeSpace(322),

    /**
     * The minimal size of the internal thread pool. Minimal number of threads in pool
     */
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("50")
    DefaultMinThreadPoolSize(323),

    /**
     * The size of the internal thread pool
     */
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("500")
    DefaultMaxThreadPoolSize(324),

    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("1")
    InitStorageSparseSizeInGB(326),

    @Reloadable
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("ovirtmgmt")
    ManagementNetwork(328),

    @Reloadable
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("5.8")
    OvirtInitialSupportedIsoVersion(329),

    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("rhevh")
    OvirtIsoPrefix(330),

    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("80")
    QuotaThresholdVdsGroup(331),

    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("80")
    QuotaThresholdStorage(332),

    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("20")
    QuotaGraceVdsGroup(333),

    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("20")
    QuotaGraceStorage(334),

    /**
     * The base URL for the documentation web-site
     */
    @Reloadable
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("")
    DocsURL(335),

    // This value indicates devices that although are given to us by VDSM
    // are still treated as managed devices
    // This should be a [device=<device> type=<type>[,]]* string
    @Reloadable
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("")
    ManagedDevicesWhiteList(336),

    /**
     * The origin type to be used for VM and VM template creation
     */
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("OVIRT")
    OriginType(336),

    @Reloadable
    @TypeConverterAttribute(Boolean.class)
    @DefaultValueAttribute("false")
    HotPlugEnabled(337),

    @Reloadable
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("")
    HotPlugUnsupportedOsList(338),

    @TypeConverterAttribute(Boolean.class)
    @DefaultValueAttribute("false")
    AllowDuplicateMacAddresses(339),

    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("3")
    SetupNetworksPollingTimeout(340),

    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("10")
    JobCleanupRateInMinutes(341),

    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("10")
    SucceededJobCleanupTimeInMinutes(342),

    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("60")
    FailedJobCleanupTimeInMinutes(343),

    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("100")
    JobPageSize(344),

    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("5")
    VmPoolMonitorIntervalInMinutes(344),

    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("5")
    VmPoolMonitorBatchSize(345),

    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("0 0/5 * * * ?")
    AutoRecoverySchedule(346),

    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("3")
    VmPoolMonitorMaxAttempts(347),

    @TypeConverterAttribute(Boolean.class)
    @DefaultValueAttribute("false")
    LiveSnapshotEnabled(348),

    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("9000")
    MaxMTU(349),

    @TypeConverterAttribute(Boolean.class)
    @DefaultValueAttribute("false")
    PosixStorageEnabled(350),

    @TypeConverterAttribute(Boolean.class)
    @DefaultValueAttribute("false")
    SendVmTicketUID(351),

    @DefaultValueAttribute("")
    @TypeConverterAttribute(String.class)
    LDAPProviderTypes(352),

    @TypeConverterAttribute(Boolean.class)
    @DefaultValueAttribute("false")
    AdvancedNFSOptionsEnabled(353),

    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("8192")
    PayloadSize(354),

    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("255")
    ApplicationMode(355),

    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("4")
    NumberOfUSBSlots(356),

    @TypeConverterAttribute(Boolean.class)
    @DefaultValueAttribute("true")
    NativeUSBEnabled(357),

    @TypeConverterAttribute(Boolean.class)
    @DefaultValueAttribute("false")
    ShareableDiskEnabled(358),

    @TypeConverterAttribute(Boolean.class)
    @DefaultValueAttribute("false")
    CpuPinningEnabled(359),

    @TypeConverterAttribute(Boolean.class)
    @DefaultValueAttribute("false")
    DirectLUNDiskEnabled(360),

    @TypeConverterAttribute(Boolean.class)
    @DefaultValueAttribute("false")
    FilteringLUNsEnabled(361),

    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("animation")
    WANDisableEffects(362),

    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("16")
    WANColorDepth(363),

    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("3")
    VmPoolMaxSubsequentFailures(364),

    @TypeConverterAttribute(Boolean.class)
    @DefaultValueAttribute("true")
    CpuPinMigrationEnabled(365),

    @TypeConverterAttribute(Boolean.class)
    @DefaultValueAttribute("false")
    SupportForceCreateVG(366),

    @TypeConverterAttribute(Boolean.class)
    @DefaultValueAttribute("true")
    NonVmNetworkSupported(367),

    @TypeConverterAttribute(java.util.List.class)
    @DefaultValueAttribute("0,2")
    @OptionBehaviourAttribute(behaviour = OptionBehaviour.CommaSeparatedStringArray)
    DisconnectPoolOnReconstruct(368),

    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("120")
    NetworkConnectivityCheckTimeoutInSeconds(369),

    @TypeConverterAttribute(Boolean.class)
    @DefaultValueAttribute("true")
    SupportBridgesReportByVDSM(370),

    @Reloadable
    @TypeConverterAttribute(Map.class)
    @DefaultValueAttribute("{\"storage domains\":\"false\",\"hosts\":\"false\"}")
    AutoRecoveryAllowedTypes(371),

    /*
     * umask is required to allow only self access
     * tar is missing from vanilla fedora-18 so we use python
     */
    @Reloadable
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute(
        "umask 0077; " +
        "MYTMP=\"$(mktemp -t ovirt-XXXXXXXXXX)\"; " +
        "trap \"chmod -R u+rwX \\\"${MYTMP}\\\" > /dev/null 2>&1; rm -fr \\\"${MYTMP}\\\" > /dev/null 2>&1\" 0; " +
        "rm -fr \"${MYTMP}\" && " +
        "mkdir \"${MYTMP}\" && " +
        "python -c \"import sys, tarfile; tarfile.open(fileobj=sys.stdin, mode='r|').extractall(path='${MYTMP}')\" && " +
        "@ENVIRONMENT@ \"${MYTMP}\"/setup DIALOG/dialect=str:machine DIALOG/customization=bool:True"
    )
    BootstrapCommand(373),

    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("10000")
    BootstrapCacheRefreshInterval(374),
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("/usr/share/ovirt-host-deploy/interface-3")
    BootstrapPackageDirectory(375),
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("ovirt-host-deploy.tar")
    BootstrapPackageName(376),
    @Reloadable
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("ovirt-engine")
    SSHKeyAlias(377),

    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("30")
    LDAPOperationTimeout(378),

    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("30")
    LDAPConnectTimeout(379),

    /*
     * Whether to allow a cluster with both Virt and Gluster services enabled
     */
    @TypeConverterAttribute(Boolean.class)
    @DefaultValueAttribute("true")
    AllowClusterWithVirtGlusterEnabled(380),

    @TypeConverterAttribute(Boolean.class)
    @DefaultValueAttribute("true")
    EnableMACAntiSpoofingFilterRules(381),
    // Gluster peer status command
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("gluster peer status --xml")
    GlusterPeerStatusCommand(378),

    @TypeConverterAttribute(Boolean.class)
    @DefaultValueAttribute("true")
    MTUOverrideSupported(382),

    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("1800")
    SSHInactivityHardTimoutSeconds(383),

    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("virt")
    GlusterVolumeOptionGroupVirtValue(384),

    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("36")
    GlusterVolumeOptionOwnerUserVirtValue(385),

    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("36")
    GlusterVolumeOptionOwnerGroupVirtValue(386),

    @Reloadable
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("")
    IPTablesConfigForVirt(387),

    @Reloadable
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("")
    IPTablesConfigForGluster(388),

    // Host time drift
    @Reloadable
    @TypeConverterAttribute(Boolean.class)
    @DefaultValueAttribute("false")
    EnableHostTimeDrift(389),

    @Reloadable
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("300")
    HostTimeDriftInSec(390),

    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("10000")
    ThrottlerMaxWaitForVdsUpdateInMillis(391),

    @TypeConverterAttribute(Boolean.class)
    @DefaultValueAttribute("false")
    OnlyRequiredNetworksMandatoryForVdsSelection(392),

    @Reloadable
    @TypeConverterAttribute(EngineWorkingMode.class)
    @DefaultValueAttribute("Active")
    EngineMode(393),

    @TypeConverterAttribute(Boolean.class)
    @DefaultValueAttribute("false")
    LiveStorageMigrationEnabled(394),

    /**
     * Refresh rate (in seconds) for light-weight gluster data i.e. data that can be fetched without much of an overhead
     * on the GlusterFS processes
     */
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("5")
    GlusterRefreshRateLight(395),

    /**
     * Refresh rate (in seconds) for heavy-weight gluster data i.e. commands to fetch such data adds a considerable
     * overhead on the GlusterFS processes.
     */
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("300")
    GlusterRefreshRateHeavy(396),

    @Reloadable
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("")
    BootstrapMinimalVdsmVersion(397),

    @TypeConverterAttribute(Boolean.class)
    @DefaultValueAttribute("true")
    SupportForceExtendVG(398),

    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("")
    ENGINEEARLib(399),
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("")
    MinimalETLVersion(400),

    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("")
    SysPrepWindows8Path(401),

    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("")
    ProductKeyWindows8(402),

    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("")
    SysPrepWindows8x64Path(403),

    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("")
    ProductKeyWindows8x64(404),

    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("")
    SysPrepWindows2012x64Path(405),

    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("")
    ProductKeyWindows2012x64(406),

    Invalid(65535);

    private int intValue;
    private static Map<Integer, ConfigValues> mappings;

    static {
        mappings = new HashMap<Integer, ConfigValues>();
        for (ConfigValues action : values()) {
            mappings.put(action.getValue(), action);
        }
    }

    private ConfigValues(int value) {
        intValue = value;
    }

    public int getValue() {
        return intValue;
    }

    public static ConfigValues forValue(int value) {
        return mappings.get(value);
    }
}
