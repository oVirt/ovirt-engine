--Handling Authentication Method
select fn_db_add_config_value('AuthenticationMethod','LDAP','general');
--Handling LDAP Security Authentication Method
select fn_db_add_config_value('LDAPSecurityAuthentication','GSSAPI','general');
--Handling NetBIOS Domain Name
select fn_db_add_config_value('DomainName','example.com','general');
--Handling Active Directory User Name
select fn_db_add_config_value('AdUserName','SampleUser','general');
--Handling Active Directory User Password
select fn_db_add_config_value('AdUserPassword','SamplePassword','general');
--Handling Refresh Rate of Users Data from Active Directory (in seconds)
select fn_db_add_config_value('UserRefreshRate','3600','general');
--Handling Port on which LDAP server listens
select fn_db_add_config_value('LDAPServerPort','389','general');
--Handling LDAP query time-out
select fn_db_add_config_value('LDAPQueryTimeout','30','general');
--Handling SASL QOP
select fn_db_add_config_value('SASL_QOP','auth-conf','general');
--Handling LDAP query page size
select fn_db_add_config_value('LdapQueryPageSize','1000','general');
--Handling Max number of LDAP query parts
select fn_db_add_config_value('MaxLDAPQueryPartsNumber','100','general');
--Handling Local Administrator Password
select fn_db_add_config_value('LocalAdminPassword','123456','general');
--Handling Default Workgroup
select fn_db_add_config_value('DefaultWorkgroup','WORKGROUP','general');
--Handling Virtual Machine Domain Name
select fn_db_add_config_value('VirtualMachineDomainName','VirtualMachineDomainName','general');
--Handling Path to an XP machine Sys-Prep file.
select fn_db_add_config_value('SysPrepXPPath','backend/manager/conf/sysprep/sysprep.xp','general');
--Handling Path to an Windows 2003 machine Sys-Prep file.
select fn_db_add_config_value('SysPrep2K3Path','backend/manager/conf/sysprep/sysprep.2k3','general');
--Handling Path to an Windows 2008 machine Sys-Prep file.
select fn_db_add_config_value('SysPrep2K8Path','backend/manager/conf/sysprep/sysprep.2k8','general');
--Handling Path to an Windows 2008 x64 machine Sys-Prep file.
select fn_db_add_config_value('SysPrep2K8x64Path','backend/manager/conf/sysprep/sysprep.2k8x86','general');
--Handling Path to an Windows 2008 R2 machine Sys-Prep file.
select fn_db_add_config_value('SysPrep2K8R2Path','backend/manager/conf/sysprep/sysprep.2k8','general');
--Handling Path to an Windows 7 machine Sys-Prep file.
select fn_db_add_config_value('SysPrepWindows7Path','backend/manager/conf/sysprep/sysprep.w7','general');
--Handling Path to an Windows 7 x64 machine Sys-Prep file.
select fn_db_add_config_value('SysPrepWindows7x64Path','backend/manager/conf/sysprep/sysprep.w7x64','general');
--Handling Product Key (for Windows XP)
select fn_db_add_config_value('ProductKey','','general');
--Handling Product Key (for Windows 2003)
select fn_db_add_config_value('ProductKey2003','','general');
--Handling Product Key (for Windows 2003 x64)
select fn_db_add_config_value('ProductKey2003x64','','general');
--Handling Product Key (for Windows 2008)
select fn_db_add_config_value('ProductKey2008','','general');
--Handling Product Key (for Windows 2008 x64)
select fn_db_add_config_value('ProductKey2008x64','','general');
--Handling Product Key (for Windows 2008 R2)
select fn_db_add_config_value('ProductKey2008R2','','general');
--Handling Product Key (for Windows 7)
select fn_db_add_config_value('ProductKeyWindow7','','general');
--Handling Product Key (for Windows 7 x64)
select fn_db_add_config_value('ProductKeyWindow7x64','','general');
--Handling Limit of % free disk-space below which it is considered low
select fn_db_add_config_value('FreeSpaceLow','10','general');
--Handling Critical low disk space alert threshold (in GB)
select fn_db_add_config_value('FreeSpaceCriticalLowInGB','5','general');
--Handling Async Task Polling Rate (in seconds)
select fn_db_add_config_value('AsyncTaskPollingRate','10','general');
--Handling Storage Domain failure timeout
select fn_db_add_config_value('StorageDomainFalureTimeoutInMinutes','5','general');
--Handling Storage Pool Manager Polling Rate (in seconds)
select fn_db_add_config_value('StoragePoolRefreshTimeInSeconds','10','general');
--Handling Number of attempts to connect to the Storage Pool Manager before Failover
select fn_db_add_config_value('SPMFailOverAttempts','3','general');
--Handling Number of retries to failover the Storage Pool Manager on failed commands
select fn_db_add_config_value('SpmCommandFailOverRetries','3','general');
--Handling Initializing disk image is more secure but it is time consuming and I/O intensive (depends on the size of the image)
select fn_db_add_config_value('SANWipeAfterDelete','false','general');
--Handling Number of VMs with highest disk size to display
select fn_db_add_config_value('NumberOfVmsForTopSizeVms','10','general');
--Handling Number of VMs with highest disk size to display
select fn_db_add_config_value('StoragePoolNonOperationalResetTimeoutInMin','3','general');
--Handling Max number of hosts in Storage Pool
select fn_db_add_config_value('MaxNumberOfHostsInStoragePool','250','general');
--Handling Max timeout for last check of domain in seconds
select fn_db_add_config_value('MaxStorageVdsTimeoutCheckSec','30','general');
--Handling Max delay for check of domain in seconds
select fn_db_add_config_value('MaxStorageVdsDelayCheckSec','5','general');
--Handling Host Control Communication Timeout (in seconds)
select fn_db_add_config_value('vdsTimeout','180','general');
--Handling Host Reboot Timeout (in seconds)
select fn_db_add_config_value('ServerRebootTimeout','300','general');
--Handling Host Polling Rate (in seconds)
select fn_db_add_config_value('VdsRefreshRate','2','general');
--Handling Max Host Memory Over-Commit (%) for Virtual Desktops load
select fn_db_add_config_value('MaxVdsMemOverCommit','200','general');
--Handling Max Host Memory Over-Commit (%) for Virtual Servers load
select fn_db_add_config_value('MaxVdsMemOverCommitForServers','150','general');
--Handling Install virtualization software on Add Host
select fn_db_add_config_value('InstallVds','true','general');
--Handling Enable USB devices attachment to the VM by default
select fn_db_add_config_value('EnableUSBAsDefault','true','general');
--Handling Use Secure Connection with Hosts
select fn_db_add_config_value('UseSecureConnectionWithServers','true','general');
--Handling Organization Name
select fn_db_add_config_value('OrganizationName','Redhat','general');
--Handling Net Console Port
select fn_db_add_config_value('NetConsolePort','','general');
--Handling Host Timeout when Recovering (in minutes)
select fn_db_add_config_value('VdsRecoveryTimeoutInMintues','3','general');
--Handling Number of attempts to communicate with Host before trying to reset
select fn_db_add_config_value('VDSAttemptsToResetCount','2','general');
--Handling Communication timeout in seconds before trying to reset
select fn_db_add_config_value('TimeoutToResetVdsInSeconds','60','general');
--Handling The RHEV-H installation files path
select fn_db_add_config_value('oVirtISOsRepositoryPath','ovirt-isos','general');
--Handling Host Installation Bootstrap Script URL
select fn_db_add_config_value('VdcBootStrapUrl','http://example.com/engine/vds_scripts','general');
--Handling Host swap percentage threshold (for scheduling)
select fn_db_add_config_value('BlockMigrationOnSwapUsagePercentage','0','general');
--Handling Wait to a Host to complete init in SPM selection
select fn_db_add_config_value('WaitForVdsInitInSec','60','general');
--Handling Threshold for logging low host memory in MB
select fn_db_add_config_value('LogPhysicalMemoryThresholdInMB','1024','general');
--Handling Certificate File Name
select fn_db_add_config_value('CertificateFileName','vdc.pfx','general');
--Handling Certificate Password
select fn_db_add_config_value('CertificatePassword','jlOTIS0q5avsg1GaRjf/6/tnEM1pXcCRvNoeJ5MCgHF1kCzcqqhZvzR8Pn/5iBxaKVC7Y4OdA0joXVMLGasVGLnUkxExzNCMT+6QwyFM1L9/0g+1OgGTuMbvYTfEi0jLOFv0xwWDl5MWunPUjZriGEhkiu5d6QJ5ZeEhD4rRooA=','general');
--Handling CA certificate path
select fn_db_add_config_value('CACertificatePath','ca/certs.pem','general');
--Handling Certificate Finger Print
select fn_db_add_config_value('CertificateFingerPrint','73 18 22 44 5d 98 b0 5d c0 f7 36 7d f8 1d 85 da e1 3c f1 c6','general');
--Handling CA Base Directory
select fn_db_add_config_value('CABaseDirectory','ca','general');
--Handling Truststore password
select fn_db_add_config_value('TruststorePass','NoSoup4U','general');
--Handling Keystore password
select fn_db_add_config_value('keystorePass','NoSoup4U','general');
--Handling Script name for signing
select fn_db_add_config_value('SignScriptName','SignReq.sh','general');
--Handling Keystore URL
select fn_db_add_config_value('keystoreUrl','.keystore','general');
--Handling Certificate alias
select fn_db_add_config_value('CertAlias','engine','general');
--Handling Truststore URL
select fn_db_add_config_value('TruststoreUrl','.keystore','general');
--Handling oVirt Engine public HTTP port
select fn_db_add_config_value('PublicURLPort','8080','general');
--Handling PEM File Name
select fn_db_add_config_value('CAEngineKey','engine.pem','general');
--Handling SSH Inactivity Timeout (in seconds)
select fn_db_add_config_value('SSHInactivityTimoutSeconds','600','general');
--Handling SPICE SSL Enabled
select fn_db_add_config_value('SSLEnabled','true','general');
--Handling SPICE Secure Channels
select fn_db_add_config_value('SpiceSecureChannels','smain,sinputs','general');
--Handling Enables Host Load Balancing system.
select fn_db_add_config_value('EnableVdsLoadBalancing','true','general');
--Handling Host Load Balancing Interval (in minutes)
select fn_db_add_config_value('VdsLoadBalancingeIntervalInMinutes','1','general');
--Handling High Utilization Limit For Evenly Distribute selection algorithm
select fn_db_add_config_value('HighUtilizationForEvenlyDistribute','75','general');
--Handling Low Utilization Limit For Evenly Distribute selection algorithm
select fn_db_add_config_value('LowUtilizationForEvenlyDistribute','0','general');
--Handling High Utilization Limit For Power Save selection algorithm
select fn_db_add_config_value('HighUtilizationForPowerSave','75','general');
--Handling Low Utilization Limit For Power Save selection algorithm
select fn_db_add_config_value('LowUtilizationForPowerSave','20','general');
--Handling The Utilization Threshold (in percent)
select fn_db_add_config_value('UtilizationThresholdInPercent','80','general');
--Handling The duration in minutes of CPU consumption to activate selection algorithm
select fn_db_add_config_value('CpuOverCommitDurationMinutes','2','general');
--Handling The cpu consumption of SPM embodied as number of VCPUs on the Host
select fn_db_add_config_value('SpmVCpuConsumption','1','general');
--Handling Enable Spice Root Certification Validation
select fn_db_add_config_value('EnableSpiceRootCertificateValidation','true','general');
--Handling Configuration directory for ENGINE
select fn_db_add_config_value('ConfigDir','/etc/engine','general');
--Handling Data directory for ENGINE
select fn_db_add_config_value('DataDir','/usr/share/engine','general');
--Handling oVirt Enterprise Virtualization Engine Manager Version
select fn_db_add_config_value('VdcVersion','3.0.0.0','general');
--Handling Audit Log Cleanup Time
select fn_db_add_config_value('AuditLogCleanupTime','03:35:35','general');
--Handling Audit Log Aging Threashold (in days)
select fn_db_add_config_value('AuditLogAgingThreashold','30','general');
--Handling Max Quantity of Search Results
select fn_db_add_config_value('SearchResultsLimit','100','general');
--Handling Log XML-RPC Data
select fn_db_add_config_value('LogXmlRpcData','false','general');
--Handling MAC Addresses Pool Ranges
select fn_db_add_config_value('MacPoolRanges','00:1A:4A:16:01:51-00:1A:4A:16:01:e6','general');
--Handling Maximum MAC Addresses count in Pool
select fn_db_add_config_value('MaxMacsCountInPool','100000','general');
--Handling Max VM name length in Windows
select fn_db_add_config_value('MaxVmNameLengthWindows','15','general');
--Handling Max VM name length for non-Windows OS
select fn_db_add_config_value('MaxVmNameLengthNonWindows','64','general');
--Handling Max VDS name length
select fn_db_add_config_value('MaxVdsNameLength','255','general');
--Handling Number of Failed Runs on Host
select fn_db_add_config_value('NumberOfFailedRunsOnVds','3','general');
--Handling Time to Reduce Failed Run on Host (in minutes)
select fn_db_add_config_value('TimeToReduceFailedRunOnVdsInMinutes','30','general');
--Handling Max Virtual Machine Rerun Attemtps on a Host
select fn_db_add_config_value('MaxRerunVmOnVdsCount','3','general');
--Handling Valid Numbers of Monitors
select fn_db_add_config_value('ValidNumOfMonitors','1,2,4','general');
--Handling Number of Virtaul Machine Data Refreshes Before Saving to DataBase
select fn_db_add_config_value('NumberVmRefreshesBeforeSave','5','general');
--Handling Minimal memory size of virtual machine in MB
select fn_db_add_config_value('VMMinMemorySizeInMB','256','general');
--Handling Time to Auto Suspend VM (in minutes)
select fn_db_add_config_value('AutoSuspendTimeInMinutes','30','general');
--Handling Message displayed in Virtual Machine when Virtual Machine is being shutdown from oVirt Enterprise Virtualization Engine Manager
select fn_db_add_config_value('VmGracefulShutdownMessage','The oVirt Enterprise Virtualization Engine Manager is shutting down this Virtual Machine','general');
--Handling Keyboard keys combination that causes the mouse cursor to be released from its grab on SPICE
select fn_db_add_config_value('SpiceReleaseCursorKeys','shift+f12','general');
--Handling Keyboard keys combination that toggles the full-screen state of SPICE client window
select fn_db_add_config_value('SpiceToggleFullScreenKeys','shift+f11','general');
--Handling Enable USB devices sharing by default in SPICE
select fn_db_add_config_value('SpiceUsbAutoShare','true','general');
--Handling Connect to RDP console with Fully Qualified User-Name (user@domain)
select fn_db_add_config_value('RDPLoginWithFQN','true','general');
--Handling Keyboard Layout configuration for VNC
select fn_db_add_config_value('VncKeyboardLayout','en-us','general');
--Handling Max size of newly created disk (in GB)
select fn_db_add_config_value('MaxDiskSize','16384','general');
--Handling Zombie tasks life-time in minutes
select fn_db_add_config_value('AsyncTaskZombieTaskLifeInMinutes','300','general');
--Handling Disable Fence Operations At oVirt Startup In Seconds
select fn_db_add_config_value('DisableFenceAtStartupInSec','300','general');
--Handling Enable Power Client GUI
select fn_db_add_config_value('PowerClientGUI','false','general');
--Handling Auto Approve Patterns
select fn_db_add_config_value('PowerClientAutoApprovePatterns','','general');
--Handling Dedicated Vm Launch on Host while PowerClient Starts
select fn_db_add_config_value('PowerClientDedicatedVmLaunchOnVdsWhilePowerClientStarts','false','general');
--Handling AutoMigrate To PowerClient On Connect
select fn_db_add_config_value('PowerClientAutoMigrateToPowerClientOnConnect','false','general');
--Handling Auto Migrate From PowerClient To Host When Connecting From Regular Client
select fn_db_add_config_value('PowerClientAutoMigrateFromPowerClientToVdsWhenConnectingFromRegularClient','false','general');
--Handling Allow Running Guests Without Tools
select fn_db_add_config_value('PowerClientAllowRunningGuestsWithoutTools','false','general');
--Handling Auto Registration Default Host Group ID
select fn_db_add_config_value('PowerClientAutoRegistrationDefaultVdsGroupID','99408929-82CF-4DC7-A532-9D998063FA95','general');
--Handling Spice Dynamic Compression Management
select fn_db_add_config_value('PowerClientSpiceDynamicCompressionManagement','false','general');
--Handling Client Auto Adjust Memory
select fn_db_add_config_value('PowerClientAutoAdjustMemory','false','general');
--Handling Auto-Adjust Memory Reserve
select fn_db_add_config_value('PowerClientAutoAdjustMemoryGeneralReserve','768','general');
--Handling Auto-AdjustMemory Base On Available Memory
select fn_db_add_config_value('PowerClientAutoAdjustMemoryBaseOnAvailableMemory','false','general');
--Handling Auto-Adjust Memory Log
select fn_db_add_config_value('PowerClientAutoAdjustMemoryLog','false','general');
--Handling Mail Server
select fn_db_add_config_value('ENMailHost','','general');
--Handling Mail Port
select fn_db_add_config_value('ENMailPort','0','general');
--Handling Mail Sender
select fn_db_add_config_value('ENMailUser','','general');
--Handling Mail User Password
select fn_db_add_config_value('ENMailPassword','','general');
--Handling Mail User Domain
select fn_db_add_config_value('ENMailDomain','','general');
--Handling Use HTML in message body
select fn_db_add_config_value('ENMailIsBodyHtml','true','general');
--Handling Enable SSL
select fn_db_add_config_value('ENMailEnableSsl','true','general');
--Handling Use Default Credentials
select fn_db_add_config_value('ENMailUseDefaultCredentials','false','general');
select fn_db_add_config_value('VM32BitMaxMemorySizeInMB','20480','general');
select fn_db_add_config_value('VM64BitMaxMemorySizeInMB','262144','general');
select fn_db_add_config_value('EnableSwapCheck','true','general');
select fn_db_add_config_value('SendSMPOnRunVm','true','general');
select fn_db_add_config_value('NicDHCPDelayGraceInMS','60','general');
select fn_db_add_config_value('OvfVirtualSystemType','ENGINE','general');
--Handling Host Selection Algorithm default for cluster
select fn_db_add_config_value('VdsSelectionAlgorithm','None','general');
select fn_db_add_config_value('AdUserId','00000000-0000-0000-0000-000000000000','general');
select fn_db_add_config_value('SelectCommandTimeout','120','general');
select fn_db_add_config_value('CipherSuite','DEFAULT','general');
select fn_db_add_config_value('AutoMode','PerServer','general');
select fn_db_add_config_value('VmGracefulShutdownTimeout','30','general');
select fn_db_add_config_value('VmPriorityMaxValue','100','general');
select fn_db_add_config_value('ScriptsPath','/usr/share/engine','general');
select fn_db_add_config_value('BootstrapInstallerFileName','backend/manager/conf/vds_installer.py','general');
select fn_db_add_config_value('SearchesRefreshRateInSeconds','1','general');
select fn_db_add_config_value('UseVdsBrokerInProc','true','general');
select fn_db_add_config_value('RenewGuestIpOnVdsSubnetChangeOnParseError','false','general');
select fn_db_add_config_value('RenewGuestIpOnVdsSubnetChange','false','general');
select fn_db_add_config_value('DebugTimerLogging','true','general');
select fn_db_add_config_value('DebugSearchLogging','false','general');
select fn_db_add_config_value('AgentAppName','RHEV-Agent','general');
select fn_db_add_config_value('SpiceDriverNameInGuest','RHEV-Spice','general');
select fn_db_add_config_value('GuestToolsSetupIsoPrefix','RHEV-toolsSetup_','general');
select fn_db_add_config_value('VcpuConsumptionPercentage','10','general');
select fn_db_add_config_value('VdsCertificateValidityInYears','5','general');
select fn_db_add_config_value('DiskConfigurationList','System,Sparse,COW,true;Data,Preallocated,RAW,false;Shared,Preallocated,RAW,false;Swap,Preallocated,RAW,false;Temp,Sparse,COW,false','general');
select fn_db_add_config_value('CbcCheckOnVdsChange','false','general');
select fn_db_add_config_value('CBCCertificateScriptName','/usr/share/vdsm-reg/vdsm-gen-cert','general');
select fn_db_add_config_value('CBCCloseCertificateScriptName','/usr/share/vdsm-reg/vdsm-complete','general');
select fn_db_add_config_value('PowerClientAutoInstallCertificateOnApprove','true','general');
select fn_db_add_config_value('PowerClientMaxNumberOfConcurrentVMs','1','general');
select fn_db_add_config_value('PowerClientAllowUsingAsIRS','false','general');
select fn_db_add_config_value('PowerClientRunVmShouldVerifyPendingVMsAsWell','false','general');
select fn_db_add_config_value('PowerClientLogDetection','false','general');
select fn_db_add_config_value('PowerClientAutoAdjustMemorySpicePerSessionReserve','0','general');
select fn_db_add_config_value('PowerClientAutoAdjustMemorySpicePerMonitorReserve','0','general');
select fn_db_add_config_value('PowerClientAutoAdjustMemoryMaxMemory','2048','general');
select fn_db_add_config_value('PowerClientAutoAdjustMemoryModulus','64','general');
select fn_db_add_config_value('oVirtUploadPath','/data/updates/ovirt-node-image.iso','general');
select fn_db_add_config_value('oVirtUpgradeScriptName','/usr/share/vdsm-reg/vdsm-upgrade','general');
select fn_db_add_config_value('AsyncTaskStatusCacheRefreshRateInSeconds','30','general');
select fn_db_add_config_value('AsyncTaskStatusCachingTimeInMinutes','1','general');
select fn_db_add_config_value('VdsFenceOptions','','general');
select fn_db_add_config_value('VdsFenceOptionMapping','alom:secure=secure,port=ipport;apc:secure=secure,port=ipport,slot=port;bladecenter:secure=secure,port=ipport,slot=port;drac5:secure=secure,port=ipport;eps:slot=port;ilo:secure=ssl,port=ipport;ipmilan:;rsa:secure=secure,port=ipport;rsb:;wti:secure=secure,port=ipport,slot=port;cisco_ucs:secure=ssl,slot=port','general');
select fn_db_add_config_value('VdsFenceOptionTypes','secure=bool,port=int,slot=int','general');
select fn_db_add_config_value('FenceStopStatusRetries','3','general');
select fn_db_add_config_value('FenceStopStatusDelayBetweenRetriesInSec','60','general');
select fn_db_add_config_value('FenceStartStatusRetries','3','general');
select fn_db_add_config_value('FenceStartStatusDelayBetweenRetriesInSec','60','general');
select fn_db_add_config_value('FindFenceProxyRetries','3','general');
select fn_db_add_config_value('FindFenceProxyDelayBetweenRetriesInSec','30','general');
select fn_db_add_config_value('MaxVmsInPool','1000','general');
select fn_db_add_config_value('LogDBCommands','false','general');
--Handling Enable lock policy for Storage Pool Manager on activation
select fn_db_add_config_value('LockPolicy','ON','general');
--Handling How often to renew the lease in seconds
select fn_db_add_config_value('LockRenewalIntervalSec','5','general');
--Handling Time between renewals before loosing lease in seconds
select fn_db_add_config_value('LeaseTimeSec','60','general');
--Handling Minimum Number of minutes to refresh the the cached Iso domain file list
select fn_db_add_config_value('AutoRepoDomainRefreshTime','60','general');
--Handling IO operation timeout in seconds
select fn_db_add_config_value('IoOpTimeoutSec','10','general');
--Handling Number of renewal retries before fencing
select fn_db_add_config_value('LeaseRetries','3','general');
select fn_db_add_config_value('DBEngine','SQLServer','general');
select fn_db_add_config_value('SQLServerPagingSyntax',E' WHERE RowNum BETWEEN %1$s AND %2$s','general');
select fn_db_add_config_value('SQLServerPagingType','Range','general');
select fn_db_add_config_value('SQLServerSearchTemplate',E'SELECT * FROM (SELECT *, ROW_NUMBER() OVER(%1$s) as RowNum FROM (%2$s)) as T1) as T2 %3$s','general');
select fn_db_add_config_value('PostgresPagingSyntax',E' OFFSET (%1$s -1) LIMIT %2$s','general');
select fn_db_add_config_value('PostgresPagingType','Offset','general');
select fn_db_add_config_value('PostgresSearchTemplate',E'SELECT * FROM (%2$s) %1$s) as T1 %3$s','general');
select fn_db_add_config_value('SQLServerLikeSyntax','LIKE','general');
select fn_db_add_config_value('PostgresLikeSyntax','ILIKE','general');
select fn_db_add_config_value('SQLServerI18NPrefix','N','general');
select fn_db_add_config_value('PostgresI18NPrefix','','general');
select fn_db_add_config_value('SupportedVDSMVersions','4.5,4.9','general');
select fn_db_add_config_value('SupportedClusterLevels','2.2,3.0','general');
--Handling Storage-Database Validation Test Rate (in hours)
select fn_db_add_config_value('ImagesSyncronizationTimeout','0','general');
select fn_db_add_config_value('LicenseCertificateFingerPrint','5f 38 41 89 b1 33 49 0c 24 13 6b b3 e5 ba 9e c7 fd 83 80 3b','general');
select fn_db_add_config_value('StoragePoolNameSizeLimit','40','general');
select fn_db_add_config_value('StorageDomainNameSizeLimit','50','general');
select fn_db_add_config_value('IsMultilevelAdministrationOn','true','general');
select fn_db_add_config_value('DefaultTimeZone','(GMT) GMT Standard Time','general');
select fn_db_add_config_value('AsyncPollingCyclesBeforeRefreshSuspend','30','general');
select fn_db_add_config_value('AsyncPollingCyclesBeforeCallbackCleanup','120','general');
select fn_db_add_config_value('UserSessionTimeOutInterval','30','general');
select fn_db_add_config_value('ENGINEEARLib','%JBOSS_HOME%/server/engine-slimmed/deploy/engine.ear','general');
select fn_db_add_config_value('RhevhLocalFSPath','/data/images/','general');
select fn_db_add_config_value('UseRtl8139_pv','true','2.2');
select fn_db_add_config_value('EmulatedMachine','rhel5.5.0','2.2');
select fn_db_add_config_value('LimitNumberOfNetworkInterfaces','true','2.2');
select fn_db_add_config_value('DesktopAudioDeviceType','default,ac97','2.2');
--Handling CPU flags syntax: {id:name:flags,..,:vdsm-command};{id:...}
select fn_db_add_config_value('ServerCPUList','2:Intel Xeon w/o XD/NX:vmx,sse2:qemu64,-nx,+sse2; 3:Intel Xeon:vmx,sse2,nx:qemu64,+sse2; 4:Intel Conroe Family:vmx,sse2,nx,cx16,ssse3:qemu64,+sse2,+cx16,+ssse3; 5:Intel Penryn Family:vmx,sse2,nx,cx16,ssse3,sse4_1:qemu64,+sse2,+cx16,+ssse3,+sse4.1; 6:Intel Nehalem Family:vmx,sse2,nx,cx16,ssse3,sse4_1,sse4_2,popcnt:qemu64,+sse2,+cx16,+ssse3,+sse4.1,+sse4.2,+popcnt; 2:AMD Opteron G1 w/o NX:svm,sse2:qemu64,-nx,+sse2; 3:AMD Opteron G1:svm,sse2,nx:qemu64,+sse2; 4:AMD Opteron G2:svm,sse2,nx,cx16:qemu64,+sse2,+cx16; 5:AMD Opteron G3:svm,sse2,nx,cx16,sse4a,misalignsse,popcnt,abm:qemu64,+sse2,+cx16,+sse4a,+misalignsse,+popcnt,+abm;','2.2');
select fn_db_add_config_value('VdsFenceType','alom,apc,bladecenter,drac5,eps,ilo,ipmilan,rsa,rsb,wti,cisco_ucs','2.2');
--Handling Total Numbers of Virtual Machine CPUs
select fn_db_add_config_value('MaxNumOfVmCpus','16','2.2');
--Handling Max Number of Socket per Virtual Machine
select fn_db_add_config_value('MaxNumOfVmSockets','16','2.2');
--Handling Max Number of CPU per socket
select fn_db_add_config_value('MaxNumOfCpuPerSocket','16','2.2');
select fn_db_add_config_value('LocalStorageEnabled','false','2.2');
select fn_db_add_config_value('SupportCustomProperties','false','2.2');
select fn_db_add_config_value('SupportGetDevicesVisibility','false','2.2');
select fn_db_add_config_value('SupportStorageFormat','false','2.2');
select fn_db_add_config_value('SupportedStorageFormats','0','2.2');
--Handling User defined VM properties
select fn_db_add_config_value('UserDefinedVMProperties','','3.0');
--Handling Predefined VM properties
select fn_db_add_config_value('PredefinedVMProperties','sap_agent=^(true|false)$;sndbuf=^[0-9]+$;vhost=^(([a-zA-Z0-9_]*):(true|false))(,(([a-zA-Z0-9_]*):(true|false)))*$;viodiskcache=^(none|writeback|writethrough)$','3.0');
select fn_db_add_config_value('UseRtl8139_pv','false','3.0');
select fn_db_add_config_value('EmulatedMachine','rhel6.0.0','3.0');
select fn_db_add_config_value('LimitNumberOfNetworkInterfaces','false','3.0');
select fn_db_add_config_value('DesktopAudioDeviceType','WindowsXP,ac97,RHEL4,ac97,RHEL3,ac97,Windows2003x64,ac97,RHEL4x64,ac97,RHEL3x64,ac97,OtherLinux,ac97,Other,ac97,default,ich6','3.0');
--Handling CPU flags syntax: {id:name:flags,..,:vdsm-command};{id:...}
select fn_db_add_config_value('ServerCPUList','3:Intel Conroe Family:vmx,nx,model_Conroe:Conroe; 4:Intel Penryn Family:vmx,nx,model_Penryn:Penryn; 5:Intel Nehalem Family:vmx,nx,model_Nehalem:Nehalem; 6:Intel Westmere Family:aes,vmx,nx,model_Westmere:Westmere; 2:AMD Opteron G1:svm,nx,model_Opteron_G1:Opteron_G1; 3:AMD Opteron G2:svm,nx,model_Opteron_G2:Opteron_G2; 4:AMD Opteron G3:svm,nx,model_Opteron_G3:Opteron_G3;','3.0');
select fn_db_add_config_value('VdsFenceType','alom,apc,bladecenter,drac5,eps,ilo,ipmilan,rsa,rsb,wti,cisco_ucs','3.0');
--Handling Total Numbers of Virtual Machine CPUs
select fn_db_add_config_value('MaxNumOfVmCpus','64','3.0');
--Handling Max Number of Socket per Virtual Machine
select fn_db_add_config_value('MaxNumOfVmSockets','16','3.0');
--Handling Max Number of CPU per socket
select fn_db_add_config_value('MaxNumOfCpuPerSocket','16','3.0');
select fn_db_add_config_value('LocalStorageEnabled','true','3.0');
select fn_db_add_config_value('SupportCustomProperties','true','3.0');
select fn_db_add_config_value('SupportGetDevicesVisibility','true','3.0');
select fn_db_add_config_value('SupportStorageFormat','true','3.0');
select fn_db_add_config_value('SupportedStorageFormats','0,2','3.0');
select fn_db_add_config_value('UknownTaskPrePollingLapse','60000','general');
