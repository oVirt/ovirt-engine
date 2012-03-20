package org.ovirt.engine.core.common.config;

import java.util.HashMap;
import java.util.Map;

public enum ConfigValues {
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("9b9002d1-ec33-4083-8a7b-31f6b8931648")
    AdUserId(0),
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("SQLServer")
    DBEngine(1),
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("5")
    PSAsyncActionTimeOutInMinutes(2),
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("120")
    SelectCommandTimeout(3),
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("VdcDbConnection")
    DefaultDataBaseName(4),
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("administrator")
    AdUserName(5),
    @TypeConverterAttribute(Map.class)
    @DefaultValueAttribute("REDHAT.COM:123456")
    @OptionBehaviourAttribute(behaviour = OptionBehaviour.DomainsPasswordMap)
    AdUserPassword(8),
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("123456")
    @OptionBehaviourAttribute(behaviour = OptionBehaviour.Password)
    LocalAdminPassword(9),
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("REDHAT.COM")
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
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("3000")
    AsyncTaskZombieTaskLifeInMinutes(17),
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("3600")
    UserRefreshRate(18),
    @TypeConverterAttribute(java.util.Date.class)
    @DefaultValueAttribute("03:35:35")
    AuditLogCleanupTime(19),
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("30")
    AuditLogAgingThreashold(20),
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("00:1A:4A:16:72:XX")
    MigrationMinPort(21),
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("100")
    MigrationPortRange(22),
    @TypeConverterAttribute(Boolean.class)
    @DefaultValueAttribute("false")
    LogXmlRpcData(23),
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("drac5,ilo,ipmilan,rsa,bladecenter,alom,apc,eps,wti,rsb")
    VdsFenceType(24),
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("drac5:secure=yes,ilo:ssl=yes,ipmilan:,rsa:secure=yes,bladecenter:secure=yes,alom:secure=yes,apc:secure=yes,eps:,wti:secure=yes,rsb:")
    VdsFenceOptions(25),
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("alom:secure=secure,port=ipport;apc:secure=secure,port=ipport,slot=port;bladecenter:secure=secure,port=ipport,slot=port;drac5:secure=secure,port=ipport;eps:slot=port;ilo:secure=ssl,port=ipport;ipmilan:;rsa:secure=secure,port=ipport;rsb:;wti:secure=secure,port=ipport,slot=port")
    VdsFenceOptionMapping(26),
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("secure=bool,port=int,slot=int")
    VdsFenceOptionTypes(27),
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("3")
    FenceStopStatusRetries(28),
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("60")
    FenceStopStatusDelayBetweenRetriesInSec(29),
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("180")
    FenceQuietTimeBetweenOperationsInSec(30),
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("/data/updates/ovirt-node-image.iso")
    oVirtUploadPath(32),
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("/usr/share/rhev-hypervisor")
    oVirtISOsRepositoryPath(33),
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("/usr/share/vdsm-reg/vdsm-upgrade")
    oVirtUpgradeScriptName(34),
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("Scripts\\vds_installer.py")
    BootstrapInstallerFileName(35),
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("/usr/share/vdsm-reg/ovirt-vdsm-gen-cert.py")
    CBCCertificateScriptName(36),
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("/usr/share/vdsm-reg/ovirt-vdsm-complete.py")
    CBCCloseCertificateScriptName(37),
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("5")
    VdsCertificateValidityInYears(38),
    @TypeConverterAttribute(Boolean.class)
    @DefaultValueAttribute("false")
    RemoteBackend(39),
    @TypeConverterAttribute(Boolean.class)
    @DefaultValueAttribute("true")
    RemoteUserBackend(40),
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("Remoting")
    RemoteInterface(41),
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("AutoBackend_tcp")
    AutoRemoteInterface(42),
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("")
    RemoteUri(43),
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("USERID")
    UserId(44),
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("PASSW0RD")
    @OptionBehaviourAttribute(behaviour = OptionBehaviour.Password)
    Password(45),
    @TypeConverterAttribute(Boolean.class)
    @DefaultValueAttribute("true")
    IncludeDesktop(47),
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("100")
    SearchResultsLimit(48),
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("2")
    VDSAttemptsToResetCount(49),
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("30")
    TimeoutToResetVdsInSeconds(50),
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("VirtualMachineDomainName")
    VirtualMachineDomainName(51),
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("")
    ProductKey2003(52),
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("")
    ProductKey2003x64(53),
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("")
    ProductKey2008(54),
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("")
    ProductKey2008x64(55),
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("")
    ProductKey2008R2(56),
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("")
    ProductKeyWindow7(57),
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("")
    ProductKeyWindow7x64(58),
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("")
    ProductKey(59),
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("10")
    FreeSpaceLow(60),
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("5")
    FreeSpaceCriticalLowInGB(61),
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("")
    MacPoolRanges(62),
    @TypeConverterAttribute(Boolean.class)
    @DefaultValueAttribute("false")
    HasCluster(63),
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("1.0.0.0")
    VdcVersion(64),
    @TypeConverterAttribute(Boolean.class)
    @DefaultValueAttribute("true")
    SSLEnabled(65),
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("DEFAULT")
    CipherSuite(66),
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("10")
    MinVmDiskSize(67),
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("250")
    MaxVmDiskSize(68),
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("40")
    StoragePoolNameSizeLimit(69),
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("50")
    StorageDomainNameSizeLimit(70),
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("30")
    ImageCheckFailureMessageTimout(71),
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("30")
    SlowStorageResponseMessageTimout(72),
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("3")
    NumberOfFailedRunsOnVds(73),
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("30")
    TimeToReduceFailedRunOnVdsInMinutes(74),
    /**
     * In default rerun Vm on all Available desktops
     */
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("3")
    MaxRerunVmOnVdsCount(75),
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("PerServer")
    AutoMode(78),
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("")
    SysPrepXPPath(79),
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("")
    SysPrep2K3Path(80),
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("")
    SysPrep2K8Path(81),
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("")
    SysPrep2K8x64Path(82),
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("")
    SysPrep2K8R2Path(83),
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("")
    SysPrepWindows7Path(84),
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("")
    SysPrepWindows7x64Path(85),
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("7200")
    AutoPostbackDelay(86),
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
    @DefaultValueAttribute("WORKGROUP")
    DefaultWorkgroup(91),
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("LDAP")
    AuthenticationMethod(92),
    @TypeConverterAttribute(java.util.List.class)
    @DefaultValueAttribute("1,2,4")
    @OptionBehaviourAttribute(behaviour = OptionBehaviour.CommaSeparatedStringArray)
    ValidNumOfMonitors(93),
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("16")
    MaxNumOfVmCpus(94),
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("16")
    MaxNumOfVmSockets(95),
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
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("certs/engine.cer")
    CertificateFileName(102),
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("")
    @OptionBehaviourAttribute(behaviour = OptionBehaviour.Password)
    CertificatePassword(103),
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("")
    CertificateFingerPrint(104),
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("")
    LicenseCertificateFingerPrint(105),
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("")
    VdcBootStrapUrl(106),
    @TypeConverterAttribute(Boolean.class)
    @DefaultValueAttribute("true")
    InstallVds(108),
    @TypeConverterAttribute(Boolean.class)
    @DefaultValueAttribute("true")
    EnableUSBAsDefault(110),
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("600")
    SSHInactivityTimoutSeconds(111),
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("120")
    ServerRebootTimeout(112),
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("40")
    VmGracefulShutdownTimeout(113),
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("100")
    VmPriorityMaxValue(114),
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("Shutting Down")
    VmGracefulShutdownMessage(115),
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("3")
    SearchesRefreshRateInSeconds(116),
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("smain,sinputs")
    SpiceSecureChannels(117),
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("30")
    AutoSuspendTimeInMinutes(118),
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("shift+f12")
    SpiceReleaseCursorKeys(119),
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("shift+f11")
    SpiceToggleFullScreenKeys(120),
    @TypeConverterAttribute(Boolean.class)
    @DefaultValueAttribute("true")
    SpiceUsbAutoShare(121),
    @TypeConverterAttribute(Boolean.class)
    @DefaultValueAttribute("true")
    RDPLoginWithFQN(122),
    @TypeConverterAttribute(Boolean.class)
    @DefaultValueAttribute("true")
    UseSecureConnectionWithServers(123),
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("500")
    MaxResultsPageSize(124),
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("RedHat")
    OrganizationName(125),
    @TypeConverterAttribute(Boolean.class)
    @DefaultValueAttribute("true")
    IsMultilevelAdministrationOn(127),
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("3")
    VdsRecoveryTimeoutInMintues(128),
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("2047")
    MaxDiskSize(129),
    // the order is- {level}:{name}:{flags}:{vdsm};
    // {level}:{name}:{flags}:{vdsm};1:cpu_name:cpu_flags,..,:vdsm_exec,+..,-..;..
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("1:pentium3:vmx:pentium3;2:intel-qemu64-nx:vmx,sse2:qemu64,-nx,+sse2;3:intel-qemu64:vmx,sse2,nx:qemu64,+sse2;2:amd-qemu64-nx:svm,sse2:qemu64,-nx,+sse2;3:amd-qemu64:svm,sse2,nx:qemu64,+sse2")
    ServerCPUList(130),
    @TypeConverterAttribute(Boolean.class)
    @DefaultValueAttribute("true")
    UseVdsBrokerInProc(131),
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("RHEV-Agent")
    AgentAppName(132),
    @TypeConverterAttribute(Map.class)
    @DefaultValueAttribute("{\"windows\":\"RHEV-Spice\",\"linux\":\"xorg-x11-drv-qxl\"}")
    SpiceDriverNameInGuest(133),
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("RHEV-toolsSetup_")
    GuestToolsSetupIsoPrefix(134),
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("10")
    VcpuConsumptionPercentage(135),
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("OvfMetaFiles\\")
    OvfDirectoryPath(136),
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("/images/export/")
    ExportVmDefaultPath(137),
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("/images/import/")
    ImportDefaultPath(138),
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("None")
    VdsSelectionAlgorithm(139),
    @TypeConverterAttribute(Boolean.class)
    @DefaultValueAttribute("true")
    EnableVdsLoadBalancing(140),
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("1")
    VdsLoadBalancingeIntervalInMinutes(141),
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("0")
    LowUtilizationForEvenlyDistribute(142),
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("6")
    LowUtilizationForPowerSave(143),
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("10")
    HighUtilizationForEvenlyDistribute(144),
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("10")
    HighUtilizationForPowerSave(145),
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("80")
    UtilizationThresholdInPercent(146),
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("2")
    CpuOverCommitDurationMinutes(147),
    // a default of 120% memory over commit.
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("120")
    MaxVdsMemOverCommit(148),
    // a default of 120% memory over commit.
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("120")
    MaxVdsMemOverCommitForServers(149),
    @TypeConverterAttribute(Boolean.class)
    @DefaultValueAttribute("false")
    PowerClientGUI(150),
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("1")
    PowerClientMaxNumberOfConcurrentVMs(151),
    @TypeConverterAttribute(Boolean.class)
    @DefaultValueAttribute("false")
    PowerClientAllowUsingAsIRS(152),
    @TypeConverterAttribute(Boolean.class)
    @DefaultValueAttribute("false")
    PowerClientAutoMigrateToPowerClientOnConnect(153),
    @TypeConverterAttribute(Boolean.class)
    @DefaultValueAttribute("false")
    PowerClientAutoMigrateFromPowerClientToVdsWhenConnectingFromRegularClient(154),
    @TypeConverterAttribute(Boolean.class)
    @DefaultValueAttribute("false")
    PowerClientAutoMigrateCheckOnRDP(155),
    @TypeConverterAttribute(Boolean.class)
    @DefaultValueAttribute("true")
    PowerClientSpiceDynamicCompressionManagement(156),
    @TypeConverterAttribute(Boolean.class)
    @DefaultValueAttribute("true")
    PowerClientAutoAdjustMemory(157),
    @TypeConverterAttribute(Boolean.class)
    @DefaultValueAttribute("true")
    PowerClientAutoAdjustMemoryBaseOnAvailableMemory(158),
    @TypeConverterAttribute(Boolean.class)
    @DefaultValueAttribute("true")
    PowerClientAutoAdjustMemoryLog(159),
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("256")
    PowerClientAutoAdjustMemoryGeneralReserve(160),
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("100")
    PowerClientAutoAdjustMemorySpicePerSessionReserve(161),
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("50")
    PowerClientAutoAdjustMemorySpicePerMonitorReserve(162),
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("3712")
    PowerClientAutoAdjustMemoryMaxMemory(163),
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("64")
    PowerClientAutoAdjustMemoryModulus(164),
    @TypeConverterAttribute(Boolean.class)
    @DefaultValueAttribute("true")
    PowerClientAutoInstallCertificateOnApprove(165),
    @TypeConverterAttribute(Boolean.class)
    @DefaultValueAttribute("false")
    PowerClientAllowRunningGuestsWithoutTools(166),
    @TypeConverterAttribute(Boolean.class)
    @DefaultValueAttribute("false")
    PowerClientLogDetection(167),
    @TypeConverterAttribute(Boolean.class)
    @DefaultValueAttribute("false")
    LogVdsRegistration(168),
    @TypeConverterAttribute(Boolean.class)
    @DefaultValueAttribute("true")
    DebugTimerLogging(169),
    @TypeConverterAttribute(Boolean.class)
    @DefaultValueAttribute("true")
    DebugSearchLogging(170),
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("")
    PowerClientAutoApprovePatterns(171),
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("99408929-82CF-4DC7-A532-9D998063FA95")
    PowerClientAutoRegistrationDefaultVdsGroupID(172),
    @TypeConverterAttribute(Boolean.class)
    @DefaultValueAttribute("false")
    PowerClientRunVmShouldVerifyPendingVMsAsWell(173),
    @TypeConverterAttribute(Boolean.class)
    @DefaultValueAttribute("false")
    PowerClientDedicatedVmLaunchOnVdsWhilePowerClientStarts(174),
    @TypeConverterAttribute(Boolean.class)
    @DefaultValueAttribute("false")
    RenewGuestIpOnVdsSubnetChange(176),
    @TypeConverterAttribute(Boolean.class)
    @DefaultValueAttribute("false")
    RenewGuestIpOnVdsSubnetChangeOnParseError(177),
    @TypeConverterAttribute(Boolean.class)
    @DefaultValueAttribute("true")
    PowerClientUserPortalVdcManagedSpiceState(178),
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("3")
    StoragePoolRefreshTimeInSeconds(179),
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("3")
    StoragePoolNonOperationalResetTimeoutInMin(180),
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("1")
    StorageDomainFalureTimeoutInMinutes(181),
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("")
    ComputerADPaths(182),
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("")
    ENMailHost(183),
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("-1")
    ENMailPort(184),
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("")
    ENMailUser(185),
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("")
    @OptionBehaviourAttribute(behaviour = OptionBehaviour.Password)
    ENMailPassword(186),
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("")
    ENMailDomain(187),
    @TypeConverterAttribute(Boolean.class)
    @DefaultValueAttribute("true")
    ENMailIsBodyHtml(188),
    @TypeConverterAttribute(Boolean.class)
    @DefaultValueAttribute("true")
    ENMailEnableSsl(189),
    @TypeConverterAttribute(Boolean.class)
    @DefaultValueAttribute("false")
    ENMailUseDefaultCredentials(190),
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("System,Sparse,COW,true;Data,Preallocated,RAW,false;Shared,Preallocated,RAW,false;Swap,Preallocated,RAW,false;Temp,Sparse,COW,false")
    DiskConfigurationList(191),
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("3")
    SPMFailOverAttempts(192),
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("ON")
    LockPolicy(193),
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("5")
    LockRenewalIntervalSec(194),
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("30")
    LeaseTimeSec(195),
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("10")
    IoOpTimeoutSec(196),
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("3")
    LeaseRetries(197),
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("en-us")
    VncKeyboardLayout(203),
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("3")
    SpmCommandFailOverRetries(204),
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("1")
    SpmVCpuConsumption(205),
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("")
    RedirectServletReportsPage(251),
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("Reports not installed, please contact your administrator")
    RedirectServletReportsPageError(252),
    @TypeConverterAttribute(Boolean.class)
    @DefaultValueAttribute("true")
    EnableSpiceRootCertificateValidation(206),
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("100000")
    MaxMacsCountInPool(207),
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("10")
    NumberOfVmsForTopSizeVms(208),
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("256")
    VMMinMemorySizeInMB(210),
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("20480")
    VM32BitMaxMemorySizeInMB(211),
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("262144")
    VM64BitMaxMemorySizeInMB(212),
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("0")
    BlockMigrationOnSwapUsagePercentage(213),
    @TypeConverterAttribute(Boolean.class)
    @DefaultValueAttribute("true")
    EnableSwapCheck(214),
    @TypeConverterAttribute(Boolean.class)
    @DefaultValueAttribute("true")
    SendSMPOnRunVm(215),
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
    // Do not use those (used internally)
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute(" WHERE RowNum BETWEEN {0} AND {1}")
    SQLServerPagingSyntax(220),
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("Range")
    SQLServerPagingType(221),
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("SELECT * FROM (SELECT *, ROW_NUMBER() OVER({0}) as RowNum FROM ( {1})) as T1 ) as T2 {2}")
    SQLServerSearchTemplate(222),
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute(" OFFSET {0} LIMIT {1}")
    PostgresPagingSyntax(223),
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("Offset")
    PostgresPagingType(224),
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("SELECT * FROM ( {1}) as T1 {2}")
    PostgresSearchTemplate(225),
    @TypeConverterAttribute(Boolean.class)
    @DefaultValueAttribute("false")
    LogDBCommands(226),
    @TypeConverterAttribute(java.util.HashSet.class)
    @DefaultValueAttribute("4.4,4.5")
    @OptionBehaviourAttribute(behaviour = OptionBehaviour.CommaSeparatedVersionArray)
    SupportedVDSMVersions(227),
    @TypeConverterAttribute(java.util.HashSet.class)
    @DefaultValueAttribute("2.2,3.0")
    @OptionBehaviourAttribute(behaviour = OptionBehaviour.CommaSeparatedVersionArray)
    SupportedClusterLevels(228),
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("ENGINE")
    OvfVirtualSystemType(229),

    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("60")
    WaitForVdsInitInSec(230),

    // JTODO - temporarily using values from 256 for Java specific options
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute(".keystore")
    keystoreUrl(256),

    // TODO: handle password behavior
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("NoSoup4U")
    // @OptionBehaviourAttribute(behaviour = OptionBehaviour.Password)
    keystorePass(257),

    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute(".keystore")
    TruststoreUrl(258),

    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("NoSoup4U")
    @OptionBehaviourAttribute(behaviour = OptionBehaviour.Password)
    TruststorePass(259),

    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("(GMT) GMT Standard Time")
    DefaultTimeZone(260),

    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("30")
    AsyncPollingCyclesBeforeRefreshSuspend(261),

    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("120")
    AsyncPollingCyclesBeforeCallbackCleanup(262),

    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("389")
    LDAPServerPort(263),

    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("SignReq.bat")
    SignScriptName(264),

    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("engine")
    CertAlias(265),

    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("8080")
    PublicURLPort(266),

    @TypeConverterAttribute(Boolean.class)
    @DefaultValueAttribute("false")
    SANWipeAfterDelete(267),

    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("c:\\Program Files\\RedHat\\oVirt Engine")
    ConfigDir(267),

    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("c:\\Program Files\\RedHat\\oVirt Engine\\Service")
    DataDir(268),

    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("30")
    UserSessionTimeOutInterval(269),

    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("/data/images/rhev")
    RhevhLocalFSPath(290),

    @TypeConverterAttribute(Boolean.class)
    @DefaultValueAttribute("false")
    SupportCustomProperties(289),

    @TypeConverterAttribute(Boolean.class)
    @DefaultValueAttribute("false")
    LocalStorageEnabled(270),

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

    @TypeConverterAttribute(Boolean.class)
    @DefaultValueAttribute("true")
    LimitNumberOfNetworkInterfaces(275),

    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("15")
    MaxVmNameLengthWindows(276),

    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("64")
    MaxVmNameLengthNonWindows(277),

    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("255")
    MaxVdsNameLength(278),

    @TypeConverterAttribute(Double.class)
    @DefaultValueAttribute("30")
    MaxStorageVdsTimeoutCheckSec(279),

    @TypeConverterAttribute(Double.class)
    @DefaultValueAttribute("5")
    MaxStorageVdsDelayCheckSec(280),

    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("300")
    DisableFenceAtStartupInSec(281),

    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("60")
    NicDHCPDelayGraceInMS(282),

    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("3")
    FindFenceProxyRetries(283),

    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("30")
    FindFenceProxyDelayBetweenRetriesInSec(284),

    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("1024")
    LogPhysicalMemoryThresholdInMB(285),

    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("auth-conf")
    SASL_QOP(286),

    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("%JBOSS_HOME%\\standalone\\deployments\\engine.ear")
    ENGINEEarLib(287),

    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("1000")
    LdapQueryPageSize(288),

    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("100")
    MaxLDAPQueryPartsNumber(289),

    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("3")
    FenceStartStatusRetries(290),

    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("60")
    FenceStartStatusDelayBetweenRetriesInSec(291),

    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("30")
    LDAPQueryTimeout(292),

    @TypeConverterAttribute(Boolean.class)
    @DefaultValueAttribute("false")
    SupportGetDevicesVisibility(293),

    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("default,ich6")
    DesktopAudioDeviceType(294),

    @TypeConverterAttribute(Boolean.class)
    @DefaultValueAttribute("false")
    SupportStorageFormat(295),

    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("0")
    SupportedStorageFormats(296),

    @TypeConverterAttribute(Boolean.class)
    @DefaultValueAttribute("false")
    UseRtl8139_pv(297),

    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("ILIKE")
    @OptionBehaviourAttribute(behaviour = OptionBehaviour.ValueDependent, dependentOn = ConfigValues.DBEngine,
            realValue = "LikeSyntax")
    DBLikeSyntax(298),

    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("LIKE")
    SQLServerLikeSyntax(299),

    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("ILIKE")
    PostgresLikeSyntax(300),

    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("")
    @OptionBehaviourAttribute(behaviour = OptionBehaviour.ValueDependent, dependentOn = ConfigValues.DBEngine,
            realValue = "I18NPrefix")
    DBI18NPrefix(301),

    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("N")
    SQLI18NPrefix(302),
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("")
    PostgresI18NPrefix(303),

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

    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("10000")
    MaxAuditLogMessageLength(308),

    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("")
    SysPrepDefaultUser(309),

    @TypeConverterAttribute(String.class)
    @OptionBehaviourAttribute(behaviour = OptionBehaviour.Password)
    @DefaultValueAttribute("")
    SysPrepDefaultPassword(310),

    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("ilo3=ipmilan")
    FenceAgentMapping(311),

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

    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("30")
    SignCertTimeoutInSeconds(316),

    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("600")
    OtpExpirationInSeconds(317),

    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("20")
    ConnectToServerTimeoutInSeconds(318),

    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("")
    IPTablesConfig(319),

    @TypeConverterAttribute(String.class)

    @DefaultValueAttribute("")
    CustomPublicConfig_AppsWebSite(320),

    /**
     * Lower threshold for disk space on host to be considered low, in MB.
     */
    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("1000")
    VdsLocalDisksLowFreeSpace(321),

    /**
     * Lower threshold for disk space on host to be considered critically low (almost out of space), in MB.
     */
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

    @TypeConverterAttribute(Boolean.class)
    @DefaultValueAttribute("false")
    IsNeedSupportForOldVgAPI(325),

    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("1")
    InitStorageSparseSizeInGB(326),

    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("ovirtmgmt")
    ManagementNetwork(328),

    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("5.8")
    OvirtInitialSupportedIsoVersion(329),

    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("rhevh")
    OvirtIsoPrefix(330),

    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("80")
    QuotaThresholdVdsGroup(331),

    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("80")
    QuotaThresholdStorage(332),

    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("20")
    QuotaGraceVdsGroup(333),

    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("20")
    QuotaGraceStorage(334),

    /**
     * The base URL for the documentation web-site
     */
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("")
    DocsURL(335),

    // This value indicates devices that although are given to us by VDSM
    // are still treated as managed devices
    // This should be a [device=<device> type=<type>[,]]* string
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("")
    ManagedDevicesWhiteList(336),

    /**
     * The origin type to be used for VM and VM template creation
     */
    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("OVIRT")
    OriginType(336),

    @TypeConverterAttribute(Boolean.class)
    @DefaultValueAttribute("false")
    HotPlugEnabled(337),

    @TypeConverterAttribute(String.class)
    @DefaultValueAttribute("Windows2008,Windows2008x64,Windows2008R2x64,RHEL5,RHEL5x64,RHEL6,RHEL6x64")
    HotPlugSupportedOsList(338),

    @TypeConverterAttribute(Boolean.class)
    @DefaultValueAttribute("false")
    AllowDuplicateMacAddresses(339),

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

    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("100")
    JobPageSize(344),

    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("50")
    MaxCorrelationIdLength(345),

    @TypeConverterAttribute(Integer.class)
    @DefaultValueAttribute("5")
    VmPoolMonitorIntervalInMinutes(344),

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
