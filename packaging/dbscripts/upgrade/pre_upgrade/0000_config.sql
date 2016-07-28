/************************************************************************************
                DATABASE APPLICATION CONFIGURATION FILE

This file is used to update the vdc_options configuration table.
The following sections are available:
    Add Section
    Update section (w/o overriding current value)
    Delete section - Deprecated, all config key removals should be done in an upgrade script!
    Split config section
    Simple upgrades not available using a fn_db* function call
    Complex upgrades using temporary functions

In each section (except simple/function sections), entries are ordered by key,
please keep this when modifying this file.

PLEASE NOTE THAT THIS SCRIPT MUST REMAIN RE-ENTRANT!

************************************************************************************/

------------------------------------------------------------------------------------
-- Rename existing configuration key names, values modifications are preserved
------------------------------------------------------------------------------------
select fn_db_rename_config_key('AuditLogAgingThreashold', 'AuditLogAgingThreshold', 'general');
select fn_db_rename_config_key('ClientConsoleModeDefault', 'ClientModeSpiceDefault', 'general');
select fn_db_rename_config_key('PowerClientAutoApprovePatterns','AutoApprovePatterns','general');
select fn_db_rename_config_key('PowerClientAutoRegistrationDefaultClusterID','AutoRegistrationDefaultClusterID','general');
select fn_db_rename_config_key('PowerClientAutoInstallCertificateOnApprove','AutoInstallCertificateOnApprove','general');
select fn_db_rename_config_key('UseSecureConnectionWithServers', 'EncryptHostCommunication', 'general');
select fn_db_rename_config_key('SpiceReleaseCursorKeys', 'ConsoleReleaseCursorKeys', 'general');
select fn_db_rename_config_key('SpiceToggleFullScreenKeys', 'ConsoleToggleFullScreenKeys', 'general');
select fn_db_rename_config_key('SSHInactivityTimoutSeconds', 'SSHInactivityTimeoutSeconds', 'general');
select fn_db_rename_config_key('SSHInactivityHardTimoutSeconds', 'SSHInactivityHardTimeoutSeconds', 'general');
select fn_db_rename_config_key('StorageDomainFalureTimeoutInMinutes', 'StorageDomainFailureTimeoutInMinutes', 'general');
select fn_db_rename_config_key('VdsLoadBalancingeIntervalInMinutes', 'VdsLoadBalancingIntervalInMinutes', 'general');
select fn_db_rename_config_key('VdsRecoveryTimeoutInMintues', 'VdsRecoveryTimeoutInMinutes', 'general');
select fn_db_rename_config_key('UknownTaskPrePollingLapse', 'UnknownTaskPrePollingLapse', 'general');
select fn_db_rename_config_key('DefaultMtu', 'DefaultMTU', 'general');
select fn_db_rename_config_key('ManagementNetwork', 'DefaultManagementNetwork', 'general');
select fn_db_rename_config_key('FreeSpaceCriticalLowInGB','CriticalSpaceActionBlocker', 'general');
select fn_db_rename_config_key('FreeSpaceLow', 'WarningLowSpaceIndicator', 'general');

------------------------------------------------------------------------------------
--                  Add configuration values section
------------------------------------------------------------------------------------
select fn_db_add_config_value('BootstrapMinimalVdsmVersion','4.9','general');
select fn_db_add_config_value('CpuPinMigrationEnabled','true','general');
select fn_db_add_config_value('AdminDomain','internal','general');
select fn_db_add_config_value('AffinityRulesEnforcementManagerEnabled', 'true', 'general');
select fn_db_add_config_value('AffinityRulesEnforcementManagerRegularInterval', '1', 'general');

select fn_db_add_config_value('AgentAppName','ovirt-guest-agent-common,ovirt-guest-agent','general');
select fn_db_add_config_value('AllowClusterWithVirtGlusterEnabled','true','general');
select fn_db_add_config_value('AllowDuplicateMacAddresses','false','general');
select fn_db_add_config_value('ApplicationMode','255','general');
select fn_db_add_config_value('AsyncCommandPollingLoopInSeconds','1','general');
select fn_db_add_config_value('AsyncCommandPollingRateInSeconds','10','general');
select fn_db_add_config_value('AsyncTaskPollingRate','10','general');
select fn_db_add_config_value('AsyncTaskStatusCacheRefreshRateInSeconds','30','general');
select fn_db_add_config_value('AsyncTaskStatusCachingTimeInMinutes','1','general');
select fn_db_add_config_value('AsyncTaskZombieTaskLifeInMinutes','300','general');
select fn_db_add_config_value('AuditLogAgingThreshold','30','general');
select fn_db_add_config_value('AuditLogCleanupTime','03:35:35','general');
select fn_db_add_config_value('CoCoLifeInMinutes','3000','general');
select fn_db_add_config_value('CoCoWaitForEventInMinutes','300','general');
select fn_db_add_config_value('CommandCoordinatorThreadPoolSize','10','general');
select fn_db_add_config_value('CommandEntityAgingThreshold','30','general');
select fn_db_add_config_value('CommandEntityCleanupTime','03:35:35','general');
select fn_db_add_config_value('OnlyRequiredNetworksMandatoryForVdsSelection','false','general');
--Handling Auto Approve Patterns
select fn_db_add_config_value('AutoApprovePatterns','','general');
select fn_db_add_config_value('AutoInstallCertificateOnApprove','true','general');
select fn_db_add_config_value('AutoRecoverySchedule','0 0/5 * * * ?','general');
select fn_db_add_config_value('AutoRegistrationDefaultClusterID','99408929-82CF-4DC7-A532-9D998063FA95','general');
select fn_db_add_config_value('AutoRepoDomainRefreshTime','60','general');
select fn_db_add_config_value('BlockMigrationOnSwapUsagePercentage','0','general');
select fn_db_add_config_value('CipherSuite','DEFAULT','general');
--Handling Configuration directory for ENGINE
select fn_db_add_config_value('ConnectToServerTimeoutInSeconds','20','general');
select fn_db_add_config_value_for_versions_up_to('ClusterEmulatedMachines','pc-i440fx-rhel7.2.0,pc-i440fx-2.1,pseries-rhel7.2.0','4.0');
select fn_db_add_config_value('CpuOverCommitDurationMinutes','2','general');
--Handling Data directory for ENGINE
select fn_db_add_config_value('DataDir','/usr/share/engine','general');
select fn_db_add_config_value('DBEngine','Postgres','general');
select fn_db_add_config_value('DebugTimerLogging','true','general');
select fn_db_add_config_value('DefaultMaxThreadPoolSize','500','general');
select fn_db_add_config_value('DefaultMinThreadPoolSize','50','general');
select fn_db_add_config_value('DefaultMaxThreadWaitQueueSize','100','general');

select fn_db_add_config_value('DefaultWindowsTimeZone','GMT Standard Time','general');
select fn_db_add_config_value('DefaultGeneralTimeZone','Etc/GMT','general');
--Handling Default Workgroup
select fn_db_add_config_value('DefaultWorkgroup','WORKGROUP','general');
select fn_db_add_config_value('DisableFenceAtStartupInSec','300','general');
select fn_db_add_config_value('PopulateDirectLUNDiskDescriptionWithLUNId','4','general');
-- Host time drift
select fn_db_add_config_value('EnableHostTimeDrift','false','general');
-- list of os/remote-viewer minimal version supported by ovirt. Format: "linux:3.0;windows:2.5"
select fn_db_add_config_value('RemoteViewerSupportedVersions','rhev-win64:2.0-128;rhev-win32:2.0-128;rhel7:2.0-6;rhel6:2.0-14','general');
-- url which should be shown when the version check does not pass
select fn_db_add_config_value('RemoteViewerNewerVersionUrl','${console_client_resources_url}','general');
--Handling Enable Spice Root Certification Validation
select fn_db_add_config_value('EnableSpiceRootCertificateValidation','true','general');
select fn_db_add_config_value('EnableSwapCheck','true','general');
--Handling Enable USB devices attachment to the VM by default
select fn_db_add_config_value('EnableUSBAsDefault','true','general');
--Handling Enables Host Load Balancing system.
select fn_db_add_config_value('EnableVdsLoadBalancing','true','general');
--Handling Backup Awareness.
select fn_db_add_config_value('BackupCheckPeriodInHours','6','general');
select fn_db_add_config_value('BackupAlertPeriodInDays','1','general');
--Handling Engine working mode
select fn_db_add_config_value('EngineMode','Active','general');
--Handling Use Default Credentials
select fn_db_add_config_value('FailedJobCleanupTimeInMinutes','60','general');
select fn_db_add_config_value('FenceAgentDefaultParams','drac7:privlvl=OPERATOR,lanplus,delay=10;ilo3:lanplus,power_wait=4;ilo4:lanplus,power_wait=4','general');
select fn_db_add_config_value('CustomFenceAgentDefaultParams','','general');
select fn_db_add_config_value('FenceAgentDefaultParamsForPPC','ilo3:lanplus=1,cipher=1,privlvl=administrator,power_wait=4;ilo4:ilanplus=1,cipher=1,privlvl=administrator,power_wait=4;ipmilan:lanplus=1,cipher=1,privlvl=administrator,power_wait=4','general');
select fn_db_add_config_value('CustomFenceAgentDefaultParamsForPPC','','general');
select fn_db_add_config_value('FenceAgentMapping','drac7=ipmilan,ilo2=ilo,ilo3=ipmilan,ilo4=ipmilan','general');
select fn_db_add_config_value('CustomFenceAgentMapping','','general');
select fn_db_add_config_value('CustomFencePowerWaitParam','','general');
select fn_db_add_config_value('FenceProxyDefaultPreferences','cluster,dc','general');
select fn_db_add_config_value('FenceQuietTimeBetweenOperationsInSec','180','general');
select fn_db_add_config_value('FenceStartStatusDelayBetweenRetriesInSec','60','general');
select fn_db_add_config_value('FenceStartStatusRetries','3','general');
select fn_db_add_config_value('FenceStopStatusDelayBetweenRetriesInSec','60','general');
select fn_db_add_config_value('FenceStopStatusRetries','3','general');
select fn_db_add_config_value('FencePowerWaitParam','apc=power_wait,apc_snmp=power_wait,bladecenter=power_wait,cisco_ucs=power_wait,drac5=power_wait,drac7=power_wait,eps=delay,hpblade=power_wait,ilo=power_wait,ilo2=power_wait,ilo3=power_wait,ilo4=power_wait,ipmilan=power_wait,rsa=power_wait,rsb=power_wait,wti=power_wait','general');
select fn_db_add_config_value('FindFenceProxyDelayBetweenRetriesInSec','30','general');
select fn_db_add_config_value('FindFenceProxyRetries','3','general');
select fn_db_add_config_value('CriticalSpaceActionBlocker','5','general');
select fn_db_add_config_value('WarningLowSpaceIndicator','10','general');

-- Gluster related
select fn_db_add_config_value('GlusterRefreshRateHooks', '7200', 'general');
select fn_db_add_config_value('GlusterRefreshRateLight', '5', 'general');
select fn_db_add_config_value('GlusterRefreshRateHeavy', '300', 'general');
select fn_db_add_config_value('GlusterRefreshRateStorageDevices', '7200', 'general');
select fn_db_add_config_value('GlusterVolumeOptionGroupVirtValue','virt','general');
select fn_db_add_config_value('GlusterVolumeOptionOwnerUserVirtValue','36','general');
select fn_db_add_config_value('GlusterVolumeOptionOwnerGroupVirtValue','36','general');
select fn_db_add_config_value('GlusterRefreshRateTasks', '60', 'general');
select fn_db_add_config_value('GlusterPeerStatusRetries', '2', 'general');
select fn_db_add_config_value('GlusterTaskMinWaitForCleanupInMins', '10', 'general');
select fn_db_add_config_value('GlusterRefreshRateHealInfo', '600', 'general');
select fn_db_add_config_value('GlusterUnSyncedEntriesHistoryLimit', '40', 'general');
select fn_db_add_config_value_for_versions_up_to('GlusterSelfHealMonitoringSupported', 'false', '3.6');
-- Gluster Geo-replication --
select fn_db_add_config_value('GlusterRefreshRateGeoRepDiscoveryInSecs', '3600', 'general');
select fn_db_add_config_value('GlusterRefreshRateGeoRepStatusInSecs', '300', 'general');

-- Gluster Volume Snapshots --
select fn_db_add_config_value('GlusterRefreshRateSnapshotDiscovery', '300', 'general');
select fn_db_add_config_value('GlusterMetaVolumeName', 'gluster_shared_storage', 'general');

-- Gluster Disk Provisioning --
select fn_db_add_config_value('GlusterStorageDeviceListMountPointsToIgnore','/,/home,/boot,/run/gluster/snaps/.*','general');
select fn_db_add_config_value('GlusterStorageDeviceListFileSystemTypesToIgnore','swap','general');
select fn_db_add_config_value('GlusterDefaultBrickMountPoint','/gluster-bricks','general');

-- OpenStack related
select fn_db_add_config_value('KeystoneAuthUrl', '', 'general');

select fn_db_add_config_value('GuestToolsSetupIsoPrefix','ovirt-guest-tools-','general');
select fn_db_add_config_value('HighUtilizationForEvenlyDistribute','75','general');
select fn_db_add_config_value('HighUtilizationForPowerSave','75','general');
select fn_db_add_config_value('HostPreparingForMaintenanceIdleTime', '300', 'general');
select fn_db_add_config_value('HostTimeDriftInSec','300','general');
select fn_db_add_config_value_for_versions_up_to('HotPlugCpuSupported', '{"x86_64":"false","ppc64":"false"}', '3.6');
select fn_db_add_config_value_for_versions_up_to('HotPlugCpuSupported', '{"x86_64":"true","ppc64":"false"}', '4.0');
select fn_db_add_config_value_for_versions_up_to('HotUnplugCpuSupported', '{"x86_64":"false","ppc64":"false"}', '4.0');
select fn_db_add_config_value('HotPlugMemorySupported', '{"x86_64":"true","ppc64":"false"}', '3.6');
select fn_db_add_config_value_for_versions_up_to('HotUnplugMemorySupported', '{"x86_64":"false","ppc64":"false"}', '4.0');
select fn_db_add_config_value('MaxMemorySlots','16','general');
select fn_db_add_config_value('HotPlugMemoryMultiplicationSizeMb','256','general');

-- migration support per architecture
select fn_db_add_config_value('IsMigrationSupported','{"undefined": "true", "x86_64": "true", "ppc64" : "true" }','general');
-- snapshot support per architecture
select fn_db_add_config_value('IsMemorySnapshotSupported','{"undefined": "true", "x86_64": "true", "ppc64" : "true" }','general');
-- suspend support per architecture
select fn_db_add_config_value('IsSuspendSupported','{"undefined": "true", "x86_64": "true", "ppc64" : "true" }','general');
select fn_db_add_config_value('DisconnectPoolOnReconstruct','0,2','general');
select fn_db_add_config_value('OsRepositoryConfDir','/osinfo.conf.d','general');
select fn_db_add_config_value('IterationsWithBalloonProblem','3','general');
select fn_db_add_config_value('DefaultSysprepLocale','en_US','general');

select fn_db_add_config_value_for_versions_up_to('DataCenterWithoutSpm', 'false', '3.6'); --is that true in 4.0 ?

-- default requirement for rng sources (comma-separated string of 'RANDOM' and 'HWRNG')
select fn_db_add_config_value_for_versions_up_to('ClusterRequiredRngSourcesDefault', 'RANDOM', '4.0');

select fn_db_add_config_value('HostDevicePassthroughCapabilities', 'pci,scsi,usb_device', 'general');

-- A list of system required packages to be checked and updated on the host
select fn_db_add_config_value('PackageNamesForCheckUpdate','vdsm,vdsm-cli','general');

-- A comma delimited list of package names provided by the user for availability check and update for the host
select fn_db_add_config_value('UserPackageNamesForCheckUpdate','','general');

-- The internal between checking for new updates availability for the host
select fn_db_add_config_value('HostPackagesUpdateTimeInHours','24','general');

-- A list of system required packages to be checked and updated on a ovirt-node
select fn_db_add_config_value('OvirtNodePackageNamesForCheckUpdate','ovirt-node-ng-image-update','general');

-- Refresh rate (in hours) for available certification check
select fn_db_add_config_value('CertificationValidityCheckTimeInHours','24','general');

select fn_db_add_config_value('MaxIoThreadsPerVm','127','general');

select fn_db_add_config_value('DisplayUncaughtUIExceptions', 'true', 'general');

-- by default use no proxy
select fn_db_add_config_value('SpiceProxyDefault','','general');

select fn_db_add_config_value('RemapCtrlAltDelDefault','true','general');

select fn_db_add_config_value_for_versions_up_to('MigrationPoliciesSupported', 'false', '3.6');

--Handling Install virtualization software on Add Host

select fn_db_add_config_value('InstallVds','true','general');
select fn_db_add_config_value('IoOpTimeoutSec','10','general');
select fn_db_add_config_value('IPTablesConfig',
'# oVirt default firewall configuration. Automatically generated by vdsm bootstrap script.
*filter
:INPUT ACCEPT [0:0]
:FORWARD ACCEPT [0:0]
:OUTPUT ACCEPT [0:0]
-A INPUT -m state --state ESTABLISHED,RELATED -j ACCEPT
-A INPUT -p icmp -j ACCEPT
-A INPUT -i lo -j ACCEPT
# vdsm
-A INPUT -p tcp --dport @VDSM_PORT@ -j ACCEPT
# ovirt-imageio-daemon
-A INPUT -p tcp --dport 54322 -j ACCEPT
# libvirt tls
-A INPUT -p tcp --dport 16514 -j ACCEPT
# SSH
-A INPUT -p tcp --dport @SSH_PORT@ -j ACCEPT
# guest consoles
-A INPUT -p tcp -m multiport --dports 5900:6923 -j ACCEPT
# migration
-A INPUT -p tcp -m multiport --dports 49152:49216 -j ACCEPT
# snmp
-A INPUT -p udp --dport 161 -j ACCEPT
# Reject any other input traffic
-A INPUT -j REJECT --reject-with icmp-host-prohibited
-A FORWARD -m physdev ! --physdev-is-bridged -j REJECT --reject-with icmp-host-prohibited
COMMIT
','general');
select fn_db_add_config_value('IPTablesConfigForGluster',
'
# glusterd
-A INPUT -p tcp -m tcp --dport 24007 -j ACCEPT

# gluster swift
-A INPUT -p tcp -m tcp --dport 8080  -j ACCEPT

# portmapper
-A INPUT -p udp -m udp --dport 111   -j ACCEPT
-A INPUT -p tcp -m tcp --dport 38465 -j ACCEPT
-A INPUT -p tcp -m tcp --dport 38466 -j ACCEPT

# nfs
-A INPUT -p tcp -m tcp --dport 38467 -j ACCEPT
-A INPUT -p tcp -m tcp --dport 2049  -j ACCEPT
-A INPUT -p tcp -m tcp --dport 38469 -j ACCEPT

# nrpe
-A INPUT -p tcp --dport 5666 -j ACCEPT

# status
-A INPUT -p tcp -m tcp --dport 39543 -j ACCEPT
-A INPUT -p tcp -m tcp --dport 55863 -j ACCEPT

# nlockmgr
-A INPUT -p tcp -m tcp --dport 38468 -j ACCEPT
-A INPUT -p udp -m udp --dport 963   -j ACCEPT
-A INPUT -p tcp -m tcp --dport 965   -j ACCEPT

# ctdbd
-A INPUT -p tcp -m tcp --dport 4379  -j ACCEPT

# smbd
-A INPUT -p tcp -m tcp --dport 139   -j ACCEPT
-A INPUT -p tcp -m tcp --dport 445   -j ACCEPT

# Ports for gluster volume bricks (default 100 ports)
# Needed for gluster < 3.4.0 that may be still handled by the engine
-A INPUT -p tcp -m tcp --dport 24009:24108 -j ACCEPT

# Ports for gluster volume bricks in Hyper Converged setup(default 100 ports)
-A INPUT -p tcp -m tcp --dport 49217:49316 -j ACCEPT
','general');
select fn_db_add_config_value('IPTablesConfigForVirt',
'
# libvirt tls
-A INPUT -p tcp --dport 16514 -j ACCEPT

# serial consoles
-A INPUT -p tcp -m multiport --dports 2223 -j ACCEPT

# guest consoles
-A INPUT -p tcp -m multiport --dports 5900:6923 -j ACCEPT

# migration
-A INPUT -p tcp -m multiport --dports 49152:49216 -j ACCEPT
', 'general');


select fn_db_add_config_value_for_versions_up_to('Ipv6Supported', 'false', '4.0');
select fn_db_add_config_value('IPTablesConfigSiteCustom','','general');
select fn_db_add_config_value('IsMultilevelAdministrationOn','true','general');
select fn_db_add_config_value('JobCleanupRateInMinutes','10','general');
select fn_db_add_config_value('JobPageSize','100','general');
select fn_db_add_config_value('LeaseRetries','3','general');
select fn_db_add_config_value('LeaseTimeSec','60','general');
select fn_db_add_config_value('LicenseCertificateFingerPrint','5f 38 41 89 b1 33 49 0c 24 13 6b b3 e5 ba 9e c7 fd 83 80 3b','general');
--Handling Enable lock policy for Storage Pool Manager on activation
select fn_db_add_config_value('LockPolicy','ON','general');
select fn_db_add_config_value('LockRenewalIntervalSec','5','general');
select fn_db_add_config_value('LogPhysicalMemoryThresholdInMB','1024','general');
select fn_db_add_config_value('LogSwapMemoryThresholdInMB','1024','general');
--Handling Log XML-RPC Data
select fn_db_add_config_value('LowUtilizationForEvenlyDistribute','0','general');
select fn_db_add_config_value('LowUtilizationForPowerSave','20','general');
select fn_db_add_config_value('MacPoolRanges','00:1A:4A:16:01:51-00:1A:4A:16:01:e6','general');
select fn_db_add_config_value('ManagedDevicesWhiteList','','general');
select fn_db_add_config_value('DefaultManagementNetwork','ovirtmgmt','general');
select fn_db_add_config_value('MaxAuditLogMessageLength','10000','general');
select fn_db_add_config_value('MaxBlockDiskSize','8192','general');
select fn_db_add_config_value('MaxMacsCountInPool','100000','general');
select fn_db_add_config_value('MaxNumberOfHostsInStoragePool','250','general');
select fn_db_add_config_value_for_versions_up_to('MaxNumOfCpuPerSocket', '16', '4.0');
select fn_db_add_config_value_for_versions_up_to('MaxNumOfThreadsPerCpu', '8', '4.0');
select fn_db_add_config_value_for_versions_up_to('MaxNumOfVmCpus', '240', '4.0');
select fn_db_add_config_value_for_versions_up_to('MaxNumOfVmSockets', '16', '4.0');
select fn_db_add_config_value('MaxRerunVmOnVdsCount','3','general');
select fn_db_add_config_value('MaxStorageVdsDelayCheckSec','5','general');
select fn_db_add_config_value('MaxStorageVdsTimeoutCheckSec','30','general');
select fn_db_add_config_value('MaxVdsMemOverCommit','200','general');
select fn_db_add_config_value('MaxVdsMemOverCommitForServers','150','general');
select fn_db_add_config_value('MaxVdsNameLength','255','general');
select fn_db_add_config_value('MaxVmNameLengthNonWindows','64','general');
select fn_db_add_config_value('MaxVmNameLengthWindows','15','general');
select fn_db_add_config_value('MaxVmsInPool','1000','general');
select fn_db_add_config_value('MinimalETLVersion','3.0.0','general');
select fn_db_add_config_value('NicDHCPDelayGraceInMS','60','general');
select fn_db_add_config_value('NumberOfFailedRunsOnVds','3','general');
select fn_db_add_config_value('NumberOfUSBSlots','4','general');
select fn_db_add_config_value('NumberVmRefreshesBeforeSave','5','general');
select fn_db_add_config_value('EnableMACAntiSpoofingFilterRules','true', 'general');
select fn_db_add_config_value('MaxSchedulerWeight','1000','general');
select fn_db_add_config_value('SpeedOptimizationSchedulingThreshold','10','general');
select fn_db_add_config_value('SchedulerAllowOverBooking','false','general');
select fn_db_add_config_value('SchedulerOverBookingThreshold','10','general');
select fn_db_add_config_value('UploadFileMaxTimeInMinutes','5','general');
select fn_db_add_config_value('RetrieveDataMaxTimeInMinutes','5','general');
select fn_db_add_config_value('StorageDomainOvfStoreCount','2','general');
--Handling Organization Name
select fn_db_add_config_value('OrganizationName','oVirt','general');
select fn_db_add_config_value('OriginType','OVIRT','general');
select fn_db_add_config_value('OvfVirtualSystemType','ENGINE','general');
--Handling The ovirt-node installation files path
select fn_db_add_config_value('OvirtInitialSupportedIsoVersion','2.5.5:5.8','general');
select fn_db_add_config_value('OvirtIsoPrefix','^ovirt-node-iso-([0-9].*)\.iso$:^rhevh-([0-9].*)\.iso$','general');
select fn_db_add_config_value('OvirtNodeOS','^ovirt.*$:^rhev.*$','general');
select fn_db_add_config_value('oVirtISOsRepositoryPath','/usr/share/ovirt-node-iso:/usr/share/rhev-hypervisor','general');
select fn_db_add_config_value('oVirtUpgradeScriptName','/usr/share/vdsm-reg/vdsm-upgrade','general');
select fn_db_add_config_value('oVirtUploadPath','/data/updates/ovirt-node-image.iso','general');
select fn_db_add_config_value('OvfUpdateIntervalInMinutes','60','general');
select fn_db_add_config_value('OvfItemsCountPerUpdate','100','general');
select fn_db_add_config_value('PayloadSize','8192','general');
-- Power management health check
select fn_db_add_config_value('PMHealthCheckEnabled','false','general');
select fn_db_add_config_value('PMHealthCheckIntervalInSec','3600','general');
select fn_db_add_config_value('PostgresI18NPrefix','','general');
select fn_db_add_config_value('PostgresLikeSyntax','ILIKE','general');
select fn_db_add_config_value('PostgresPagingSyntax',E' OFFSET (%1$s -1) LIMIT %2$s','general');
select fn_db_add_config_value('PostgresPagingType','Offset','general');
select fn_db_add_config_value('PostgresSearchTemplate',E'SELECT * FROM (%2$s) %1$s) as T1 %3$s','general');
select fn_db_add_config_value_for_versions_up_to('PredefinedVMProperties', 'sap_agent=^(true|false)$;sndbuf=^[0-9]+$;vhost=^(([a-zA-Z0-9_]*):(true|false))(,(([a-zA-Z0-9_]*):(true|false)))*$;viodiskcache=^(none|writeback|writethrough)$', '4.0');
select fn_db_add_config_value('ProductRPMVersion','3.0.0.0','general');
select fn_db_add_config_value('QuotaGraceStorage','20','general');
select fn_db_add_config_value('QuotaGraceCluster','20','general');
select fn_db_add_config_value('QuotaThresholdStorage','80','general');
select fn_db_add_config_value('QuotaThresholdCluster','80','general');
--Handling Connect to RDP console with Fully Qualified User-Name (user@domain)
select fn_db_add_config_value('RhevhLocalFSPath','/data/images/','general');
select fn_db_add_config_value('SANWipeAfterDelete','false','general');
--Handling SASL QOP
select fn_db_add_config_value('SearchResultsLimit','100','general');
select fn_db_add_config_value('SendSMPOnRunVm','true','general');
select fn_db_add_config_value_for_versions_up_to('ServerCPUList','3:Intel Conroe Family:vmx,nx,model_Conroe:Conroe:x86_64; 4:Intel Penryn Family:vmx,nx,model_Penryn:Penryn:x86_64; 5:Intel Nehalem Family:vmx,nx,model_Nehalem:Nehalem:x86_64; 6:Intel Westmere Family:aes,vmx,nx,model_Westmere:Westmere:x86_64; 7:Intel SandyBridge Family:vmx,nx,model_SandyBridge:SandyBridge:x86_64; 8:Intel Haswell Family:vmx,nx,model_Haswell:Haswell:x86_64; 2:AMD Opteron G1:svm,nx,model_Opteron_G1:Opteron_G1:x86_64; 3:AMD Opteron G2:svm,nx,model_Opteron_G2:Opteron_G2:x86_64; 4:AMD Opteron G3:svm,nx,model_Opteron_G3:Opteron_G3:x86_64; 5:AMD Opteron G4:svm,nx,model_Opteron_G4:Opteron_G4:x86_64; 6:AMD Opteron G5:svm,nx,model_Opteron_G5:Opteron_G5:x86_64; 3:IBM POWER8:powernv,model_power8:power8:ppc64;','3.6');
select fn_db_add_config_value_for_versions_up_to('ServerCPUList', '3:Intel Conroe Family:vmx,nx,model_Conroe:Conroe:x86_64; 4:Intel Penryn Family:vmx,nx,model_Penryn:Penryn:x86_64; 5:Intel Nehalem Family:vmx,nx,model_Nehalem:Nehalem:x86_64; 6:Intel Westmere Family:aes,vmx,nx,model_Westmere:Westmere:x86_64; 7:Intel SandyBridge Family:vmx,nx,model_SandyBridge:SandyBridge:x86_64; 8:Intel Haswell-noTSX Family:vmx,nx,model_Haswell-noTSX:Haswell-noTSX:x86_64; 9:Intel Haswell Family:vmx,nx,model_Haswell:Haswell:x86_64; 10:Intel Broadwell-noTSX Family:vmx,nx,model_Broadwell-noTSX:Broadwell-noTSX:x86_64; 11:Intel Broadwell Family:vmx,nx,model_Broadwell:Broadwell:x86_64; 2:AMD Opteron G1:svm,nx,model_Opteron_G1:Opteron_G1:x86_64; 3:AMD Opteron G2:svm,nx,model_Opteron_G2:Opteron_G2:x86_64; 4:AMD Opteron G3:svm,nx,model_Opteron_G3:Opteron_G3:x86_64; 5:AMD Opteron G4:svm,nx,model_Opteron_G4:Opteron_G4:x86_64; 6:AMD Opteron G5:svm,nx,model_Opteron_G5:Opteron_G5:x86_64; 3:IBM POWER8:powernv,model_POWER8:POWER8:ppc64;', '4.0');

select fn_db_add_config_value('ServerRebootTimeout','300','general');
select fn_db_add_config_value('SetupNetworksPollingTimeout','3','general');
select fn_db_add_config_value('SignCertTimeoutInSeconds','30','general');
--Handling Script name for signing
select fn_db_add_config_value('SpiceDriverNameInGuest','RHEV-Spice','general');
select fn_db_add_config_value('ConsoleReleaseCursorKeys','shift+f12','general');
select fn_db_add_config_value('ConsoleToggleFullScreenKeys','shift+f11','general');
--Handling Enable USB devices sharing by default in SPICE
select fn_db_add_config_value('SpiceUsbAutoShare','true','general');
select fn_db_add_config_value('FullScreenWebadminDefault','false','general');
select fn_db_add_config_value('FullScreenUserportalBasicDefault','true','general');
select fn_db_add_config_value('FullScreenUserportalExtendedDefault','false','general');
select fn_db_add_config_value('WANDisableEffects','animation','general');
select fn_db_add_config_value('WANColorDepth','16','general');
select fn_db_add_config_value('SpmCommandFailOverRetries','3','general');
select fn_db_add_config_value('SPMFailOverAttempts','3','general');
select fn_db_add_config_value('HsmCommandFailOverRetries','3','general');
select fn_db_add_config_value('SpmVCpuConsumption','1','general');
select fn_db_add_config_value('SSHInactivityTimeoutSeconds','300','general');
select fn_db_add_config_value('SSHInactivityHardTimeoutSeconds','1800','general');
--Handling SPICE SSL Enabled
select fn_db_add_config_value('SSLEnabled','true','general');
select fn_db_add_config_value('StorageDomainFailureTimeoutInMinutes','5','general');
select fn_db_add_config_value('StorageDomainNameSizeLimit','50','general');
select fn_db_add_config_value('StoragePoolNameSizeLimit','40','general');
select fn_db_add_config_value('StoragePoolNonOperationalResetTimeoutInMin','3','general');
select fn_db_add_config_value('StoragePoolRefreshTimeInSeconds','10','general');
select fn_db_add_config_value('HostStorageConnectionAndPoolRefreshTimeInSeconds','30','general');
select fn_db_add_config_value('SucceededJobCleanupTimeInMinutes','10','general');
select fn_db_add_config_value('SupportedClusterLevels','3.0','general');
select fn_db_add_config_value('SupportedVDSMVersions','4.9,4.10,4.11,4.12,4.13,4.14','general');

select fn_db_add_config_value('SysPrepDefaultPassword','','general');
select fn_db_add_config_value('SysPrepDefaultUser','','general');
select fn_db_add_config_value('ThrottlerMaxWaitForVdsUpdateInMillis','10000','general');
select fn_db_add_config_value('TimeoutToResetVdsInSeconds','60','general');
select fn_db_add_config_value('DelayResetForSpmInSeconds','20','general');
select fn_db_add_config_value('DelayResetPerVmInSeconds','0.5','general');
--Handling Use Secure Connection with Hosts
select fn_db_add_config_value('EncryptHostCommunication','true','general');
select fn_db_add_config_value('VdsmSSLProtocol','TLSv1','general');
select fn_db_add_config_value('ExternalCommunicationProtocol','TLSv1','general');
select fn_db_add_config_value('VdsRequestQueueName','jms.topic.vdsm_requests','general');
select fn_db_add_config_value('VdsResponseQueueName','jms.topic.vdsm_responses','general');
select fn_db_add_config_value('IrsRequestQueueName','jms.topic.vdsm_irs_requests','general');
select fn_db_add_config_value('IrsResponseQueueName','jms.topic.vdsm_irs_responses','general');
select fn_db_add_config_value('EventQueueName','jms.queue.events','general');
select fn_db_add_config_value('EventProcessingPoolSize','10','general');
select fn_db_add_config_value('TimeToReduceFailedRunOnVdsInMinutes','30','general');
select fn_db_add_config_value('UnknownTaskPrePollingLapse','60000','general');
select fn_db_add_config_value('UserSessionHardLimit','600','general');
select fn_db_add_config_value_for_versions_up_to('UserDefinedVMProperties', '','4.0');
select fn_db_add_config_value('UserSessionTimeOutInterval','30','general');
select fn_db_add_config_value('UtilizationThresholdInPercent','80','general');
select fn_db_add_config_value('ValidNumOfMonitors','1,2,4','general');
select fn_db_add_config_value('VcpuConsumptionPercentage','10','general');
--Handling Host Installation Bootstrap Script URL
select fn_db_add_config_value('VdcVersion','3.0.0.0','general');
select fn_db_add_config_value('VDSAttemptsToResetCount','2','general');
select fn_db_add_config_value('VdsCertificateValidityInYears','5','general');
select fn_db_add_config_value('vdsConnectionTimeout','20','general');
select fn_db_add_config_value('VdsMaxConnectionsPerHost','2','general');
select fn_db_add_config_value('IrsMaxConnectionsPerHost','2','general');
select fn_db_add_config_value('MaxTotalConnections','20','general');
select fn_db_add_config_value_for_versions_up_to('VdsFenceOptionMapping','apc:secure=secure,port=ipport,slot=port;apc_snmp:port=port,encrypt_options=encrypt_options;bladecenter:secure=secure,port=ipport,slot=port;cisco_ucs:secure=ssl,slot=port;drac5:secure=secure,slot=port;drac7:;eps:slot=port;hpblade:port=port;ilo:secure=ssl,port=ipport;ipmilan:;ilo2:secure=ssl,port=ipport;ilo3:;ilo4:;rsa:secure=secure,port=ipport;rsb:;wti:secure=secure,port=ipport,slot=port','4.0');
select fn_db_add_config_value('CustomVdsFenceOptionMapping','','general');
select fn_db_add_config_value('VdsFenceOptions','','general');
select fn_db_add_config_value('VdsFenceOptionTypes','encrypt_options=bool,secure=bool,port=int,slot=int','general');
select fn_db_add_config_value_for_versions_up_to('VdsFenceType','apc,apc_snmp,bladecenter,cisco_ucs,drac5,drac7,eps,hpblade,ilo,ilo2,ilo3,ilo4,ipmilan,rsa,rsb,wti','4.0');
select fn_db_add_config_value('CustomVdsFenceType','','general');
select fn_db_add_config_value('vdsHeartbeatInSeconds','30','general');
select fn_db_add_config_value('VdsLoadBalancingIntervalInMinutes','1','general');
select fn_db_add_config_value('VdsLocalDisksCriticallyLowFreeSpace','100','general');
select fn_db_add_config_value('VdsLocalDisksLowFreeSpace','500','general');
select fn_db_add_config_value('VdsRecoveryTimeoutInMinutes','3','general');
select fn_db_add_config_value('VdsRefreshRate','2','general');
select fn_db_add_config_value('vdsRetries','0','general');
select fn_db_add_config_value('ProtocolFallbackRetries','3','general');
select fn_db_add_config_value('ProtocolFallbackTimeoutInMilliSeconds','5000','general');
select fn_db_add_config_value('vdsTimeout','180','general');
--Handling Virtual Machine Domain Name
select fn_db_add_config_value('VM32BitMaxMemorySizeInMB','20480','general');
select fn_db_add_config_value_for_versions_up_to('VM64BitMaxMemorySizeInMB','4194304','4.0');
select fn_db_add_config_value_for_versions_up_to('VMPpc64BitMaxMemorySizeInMB','1048576','4.0');
select fn_db_add_config_value('VmGracefulShutdownMessage','System Administrator has initiated shutdown of this Virtual Machine. Virtual Machine is shutting down.','general');
select fn_db_add_config_value('VmGracefulShutdownTimeout','30','general');
--Number of subsequent failures in VM creation in a pool before giving up and stop creating new VMs
select fn_db_add_config_value('VmPoolMaxSubsequentFailures','3','general');
select fn_db_add_config_value('VmPoolMonitorBatchSize','5','general');
select fn_db_add_config_value('VmPoolMonitorIntervalInMinutes','5','general');
select fn_db_add_config_value('VmPoolMonitorMaxAttempts','3','general');
select fn_db_add_config_value('VmPriorityMaxValue','100','general');
--How often we'll go over the HA VMs that went down and try to restart them
select fn_db_add_config_value('AutoStartVmsRunnerIntervalInSeconds','1','general');
--How often we'll try to run HA VM that we couldn't run before
select fn_db_add_config_value('RetryToRunAutoStartVmIntervalInSeconds','30','general');
--How many times we'll try to automatically restart HA VM that went down
select fn_db_add_config_value('MaxNumOfTriesToRunFailedAutoStartVm','10','general');
--Handling Keyboard Layout configuration for VNC
select fn_db_add_config_value('VncKeyboardLayout','en-us','general');
select fn_db_add_config_value('VncKeyboardLayoutValidValues','ar,da,de,de-ch,en-gb,en-us,es,et,fi,fo,fr,fr-be,fr-ca,fr-ch,hr,hu,is,it,ja,lt,lv,mk,nl,nl-be,no,pl,pt,pt-br,ru,sl,sv,th,tr','general');
select fn_db_add_config_value('WaitForVdsInitInSec','60','general');
--The default network connectivity check timeout
select fn_db_add_config_value('NetworkConnectivityCheckTimeoutInSeconds','120','general');
-- AutoRecoveryConfiguration
select fn_db_add_config_value('AutoRecoveryAllowedTypes','{"storage domains":"true","hosts":"true"}','general');
-- SPICE client mode default settings (Auto, Native, Html5), the 'Plugin' is not supported since 4.0
select fn_db_add_config_value('ClientModeSpiceDefault','Native','general');
-- Workaround to reenable the Spice Plugin to fix potential production issues: set EnableDeprecatedClientModeSpicePlugin to 'true'. Will be removed in 4.1
select fn_db_add_config_value('EnableDeprecatedClientModeSpicePlugin','false','general');
-- VNC client mode default settings (Native, NoVnc)
select fn_db_add_config_value('ClientModeVncDefault','Native','general');
-- RDP client mode default settings (Auto, Native, Plugin)
select fn_db_add_config_value('ClientModeRdpDefault','Auto','general');
-- Rdp client - Use FQDN reported by guest agent if available over IP and or generated hostname (VM Name + Domain)
select fn_db_add_config_value('UseFqdnForRdpIfAvailable','true','general');
-- Websocket proxy configuration (Off, Engine:port, Host:port or specific ip/hostname:port of websockets proxy)
select fn_db_add_config_value('WebSocketProxy','Off','general');
-- Websocket ticket validity in seconds
select fn_db_add_config_value('WebSocketProxyTicketValiditySeconds','120','general');

select fn_db_add_config_value('LogMaxPhysicalMemoryUsedThresholdInPercentage', '95', 'general');
select fn_db_add_config_value('LogMaxSwapMemoryUsedThresholdInPercentage', '95', 'general');
select fn_db_add_config_value('LogMaxCpuUsedThresholdInPercentage', '95', 'general');
select fn_db_add_config_value('LogMaxNetworkUsedThresholdInPercentage', '95', 'general');
select fn_db_add_config_value('LogMinFreeSwapThresholdInMB', '256', 'general');
select fn_db_add_config_value('LogMaxSwapUsedThresholdInPercentage', '95', 'general');

-- Allow to specify SecurityGroups property for vNICs, containing either an empty string or a list of one or more comma seperated UUIDs.
select fn_db_add_config_value_for_versions_up_to('CustomDeviceProperties', '{type=interface;prop={SecurityGroups=^(?:(?:[0-9a-fA-F]{8}-(?:[0-9a-fA-F]{4}-){3}[0-9a-fA-F]{12}, *)*[0-9a-fA-F]{8}-(?:[0-9a-fA-F]{4}-){3}[0-9a-fA-F]{12}|)$}}', '4.0');

-- Network Custom Properties
select fn_db_add_config_value_for_versions_up_to('PreDefinedNetworkCustomProperties', $q$bridge_opts=^[^\s=]+=[^\s=]+(\s+[^\s=]+=[^\s=]+)*$$q$, '4.0'); -- tag prevents psql from escaping backslashes ('q' is arbitrary)
select fn_db_add_config_value_for_versions_up_to('UserDefinedNetworkCustomProperties', '', '4.0');

select fn_db_add_config_value_for_versions_up_to('SshSoftFencingCommand', '/usr/bin/vdsm-tool service-restart vdsmd', '4.0');

--attestation configuration
select fn_db_add_config_value('SecureConnectionWithOATServers','true','general');
select fn_db_add_config_value('PollUri','AttestationService/resources/PollHosts','general');
select fn_db_add_config_value('AttestationTruststore','TrustStore.jks','general');
select fn_db_add_config_value('AttestationPort','8443','general');
select fn_db_add_config_value('AttestationTruststorePass','','general');
select fn_db_add_config_value('AttestationServer','','general');
select fn_db_add_config_value('AttestationFirstStageSize','10','general');

select fn_db_add_config_value('MaxAverageNetworkQoSValue','1024','general');
select fn_db_add_config_value('MaxPeakNetworkQoSValue','2048','general');
select fn_db_add_config_value('MaxBurstNetworkQoSValue','10240','general');
select fn_db_add_config_value('MaxHostNetworkQosShares', '100', 'general');
select fn_db_add_config_value('QoSInboundAverageDefaultValue','10','general');
select fn_db_add_config_value('QoSInboundPeakDefaultValue','10','general');
select fn_db_add_config_value('QoSInboundBurstDefaultValue','100','general');
select fn_db_add_config_value('QoSOutboundAverageDefaultValue','10','general');
select fn_db_add_config_value('QoSOutboundPeakDefaultValue','10','general');
select fn_db_add_config_value('QoSOutboundBurstDefaultValue','100','general');

--external scheduler
select fn_db_add_config_value('ExternalSchedulerServiceURL','http://localhost:18781/','general');
select fn_db_add_config_value('ExternalSchedulerConnectionTimeout','100','general');
select fn_db_add_config_value('ExternalSchedulerEnabled','false','general');
select fn_db_add_config_value('ExternalSchedulerResponseTimeout','120000','general');

select fn_db_add_config_value('DwhHeartBeatInterval', '30', 'general');
select fn_db_add_config_value('DisconnectDwh', '0', 'general');

-- OpenStack Glance
select fn_db_add_config_value('GlanceImageListSize','20','general');
select fn_db_add_config_value('GlanceImageTotalListSize','500','general');

-- Cluster HA Reservation
select fn_db_add_config_value('OverUtilizationForHaReservation','200','general');
select fn_db_add_config_value('ScaleDownForHaReservation','1','general');
select fn_db_add_config_value('VdsHaReservationIntervalInMinutes','5','general');

select fn_db_add_config_value('DefaultMaximumMigrationDowntime','0','general');

select fn_db_add_config_value('DefaultSerialNumberPolicy','HOST_ID','general');
select fn_db_add_config_value('DefaultCustomSerialNumber','Dummy serial number.','general');

select fn_db_add_config_value('DefaultMTU', '1500', 'general');

select fn_db_add_config_value('FenceKdumpDestinationAddress','','general');
select fn_db_add_config_value('FenceKdumpDestinationPort','7410','general');
select fn_db_add_config_value('FenceKdumpMessageInterval','5','general');
select fn_db_add_config_value('FenceKdumpListenerTimeout','90','general');
select fn_db_add_config_value('KdumpStartedTimeout','30','general');

select fn_db_add_config_value('AlertOnNumberOfLVs','300','general');

select fn_db_add_config_value('CSRFProtection','false','general');
select fn_db_add_config_value('CORSSupport','false','general');
select fn_db_add_config_value('CORSAllowedOrigins','','general');
select fn_db_add_config_value('UsageHistoryLimit','40', 'general');

select fn_db_add_config_value('HostStorageLeaseAliveCheckingInterval', '90', 'general');

select fn_db_add_config_value('SupportNUMAMigration','false','general');

select fn_db_add_config_value('UnsupportedLocalesFilter','','general');

select fn_db_add_config_value('DefaultAutoConvergence','false','general');
select fn_db_add_config_value('DefaultMigrationCompression','false','general');

select fn_db_add_config_value('MigrationPolicies','[{"id":{"uuid":"80554327-0569-496b-bdeb-fcbbf52b827b"},"maxMigrations":2,"autoConvergence":true,"migrationCompression":true,"enableGuestEvents":true,"name":"Safe but not may not converge","description":"A safe policy which in typical situations lets the VM converge. The user of the VM should not notice any significant slowdown of the VM. If the VM is not converging for a longer time, the migration will be aborted.","config":{"convergenceItems":[{"stallingLimit":1,"convergenceItem":{"action":"setDowntime","params":["150"]}},{"stallingLimit":2,"convergenceItem":{"action":"setDowntime","params":["200"]}},{"stallingLimit":3,"convergenceItem":{"action":"setDowntime","params":["300"]}},{"stallingLimit":4,"convergenceItem":{"action":"setDowntime","params":["400"]}},{"stallingLimit":6,"convergenceItem":{"action":"setDowntime","params":["500"]}}],"initialItems":[{"action":"setDowntime","params":["100"]}],"lastItems":[{"action":"abort","params":[]}]}},{"id":{"uuid":"80554327-0569-496b-bdeb-fcbbf52b827c"},"maxMigrations":1,"autoConvergence":true,"migrationCompression":true,"enableGuestEvents":true,"name":"Should converge but guest may notice a pause","description":"A safe policy which makes also a highly loaded VM converge in most situations. On the other hand, the user may notice a slowdown. If the VM is still not converging, the migration is aborted.","config":{"convergenceItems":[{"stallingLimit":1,"convergenceItem":{"action":"setDowntime","params":["150"]}},{"stallingLimit":2,"convergenceItem":{"action":"setDowntime","params":["200"]}},{"stallingLimit":3,"convergenceItem":{"action":"setDowntime","params":["300"]}},{"stallingLimit":4,"convergenceItem":{"action":"setDowntime","params":["400"]}},{"stallingLimit":6,"convergenceItem":{"action":"setDowntime","params":["500"]}}],"initialItems":[{"action":"setDowntime","params":["100"]}],"lastItems":[{"action":"setDowntime","params":["5000"]},{"action":"abort","params":[]}]}}]','general');

select fn_db_add_config_value_for_versions_up_to('SriovHotPlugSupported','false','3.6');

-- Hosted Engine
select fn_db_add_config_value('HostedEngineVmName','HostedEngine','general');
select fn_db_add_config_value('AutoImportHostedEngine','true','general');
select fn_db_add_config_value('AllowEditingHostedEngine','true','general');
select fn_db_add_config_value('HostedEngineConfigDiskSizeInBytes','20480','general');

select fn_db_add_config_value('ImageProxyAddress','localhost:54323','general');
select fn_db_add_config_value('ImageProxySSLEnabled','true','general');
select fn_db_add_config_value('ImageDaemonPort','54322','general');
select fn_db_add_config_value('ImageTransferClientTicketValidityInSeconds','3600','general');
select fn_db_add_config_value('ImageTransferHostTicketValidityInSeconds','300','general');
select fn_db_add_config_value('ImageTransferHostTicketRefreshAllowanceInSeconds','60','general');
select fn_db_add_config_value('ImageTransferPausedLogIntervalInSeconds','1800','general');
select fn_db_add_config_value('UploadImageUiInactivityTimeoutInSeconds','60','general');
select fn_db_add_config_value('UploadImageChunkSizeKB','8192','general');
select fn_db_add_config_value('UploadImageXhrTimeoutInSeconds','10','general');
select fn_db_add_config_value('UploadImageXhrRetryIntervalInSeconds','3','general');
select fn_db_add_config_value('UploadImageXhrMaxRetries','3','general');

select fn_db_add_config_value_for_versions_up_to('AdPartnerMacSupported','false','3.6');
select fn_db_add_config_value_for_versions_up_to('OvsSupported','false','3.6');

------------------------------------------------------------------------------------
--                  Update with override section
------------------------------------------------------------------------------------

select fn_db_update_config_value('AutoRecoveryAllowedTypes','{"storage domains":"true","hosts":"true"}','general');
select fn_db_update_config_value('BootstrapMinimalVdsmVersion','4.9','general');
select fn_db_update_config_value('DBEngine','Postgres','general');
select fn_db_update_config_value('DefaultTimeZone','(GMT) GMT Standard Time','general');
select fn_db_update_config_value('FenceAgentDefaultParams','drac7:privlvl=OPERATOR,lanplus=1,delay=10;ilo3:lanplus=1,power_wait=4;ilo4:lanplus=1,power_wait=4;ilo_ssh:secure=1','general');
select fn_db_update_config_value('FenceAgentDefaultParamsForPPC','ilo3:lanplus=1,cipher=1,privlvl=administrator,power_wait=4,retry_on=2;ilo4:ilanplus=1,cipher=1,privlvl=administrator,power_wait=4,retry_on=2;ipmilan:lanplus=1,cipher=1,privlvl=administrator,power_wait=4,retry_on=2;ilo_ssh:secure=1','general');
select fn_db_update_config_value('FenceAgentMapping','drac7=ipmilan,ilo2=ilo,ilo3=ipmilan,ilo4=ipmilan','general');
select fn_db_update_config_value('FenceStartStatusDelayBetweenRetriesInSec','10','general');
select fn_db_update_config_value('FenceStartStatusRetries','18','general');
select fn_db_update_config_value('FenceStopStatusDelayBetweenRetriesInSec','10','general');
select fn_db_update_config_value('FenceStopStatusRetries','18','general');
select fn_db_update_config_value('FencePowerWaitParam','apc=power_wait,apc_snmp=power_wait,bladecenter=power_wait,cisco_ucs=power_wait,drac5=power_wait,drac7=power_wait,eps=delay,hpblade=power_wait,ilo=power_wait,ilo2=power_wait,ilo3=power_wait,ilo4=power_wait,ilo_ssh=power_wait,ipmilan=power_wait,rsa=power_wait,rsb=power_wait,wti=power_wait','general');
select fn_db_update_config_value('MigrationPolicies','[{"id":{"uuid":"80554327-0569-496b-bdeb-fcbbf52b827b"},"maxMigrations":2,"autoConvergence":true,"migrationCompression":true,"enableGuestEvents":true,"name":"Minimal downtime","description":"A policy that lets the VM migrate in typical situations. The VM should not experience any significant downtime. If the VM migration is not converging for a long time, the migration will be aborted. The guest agent hook mechanism is enabled.","config":{"convergenceItems":[{"stallingLimit":1,"convergenceItem":{"action":"setDowntime","params":["150"]}},{"stallingLimit":2,"convergenceItem":{"action":"setDowntime","params":["200"]}},{"stallingLimit":3,"convergenceItem":{"action":"setDowntime","params":["300"]}},{"stallingLimit":4,"convergenceItem":{"action":"setDowntime","params":["400"]}},{"stallingLimit":6,"convergenceItem":{"action":"setDowntime","params":["500"]}}],"initialItems":[{"action":"setDowntime","params":["100"]}],"lastItems":[{"action":"abort","params":[]}]}},{"id":{"uuid":"80554327-0569-496b-bdeb-fcbbf52b827c"},"maxMigrations":1,"autoConvergence":true,"migrationCompression":true,"enableGuestEvents":true,"name":"Suspend workload if needed","description":"A policy that lets the VM migrate in most situations, including VMs running heavy workloads. On the other hand, the VM may experience a more significant downtime. The migration may still be aborted for extreme workloads. The guest agent hook mechanism is enabled.","config":{"convergenceItems":[{"stallingLimit":1,"convergenceItem":{"action":"setDowntime","params":["150"]}},{"stallingLimit":2,"convergenceItem":{"action":"setDowntime","params":["200"]}},{"stallingLimit":3,"convergenceItem":{"action":"setDowntime","params":["300"]}},{"stallingLimit":4,"convergenceItem":{"action":"setDowntime","params":["400"]}},{"stallingLimit":6,"convergenceItem":{"action":"setDowntime","params":["500"]}}],"initialItems":[{"action":"setDowntime","params":["100"]}],"lastItems":[{"action":"setDowntime","params":["5000"]},{"action":"abort","params":[]}]}}]','general');

select fn_db_update_config_value('IPTablesConfig','
# oVirt default firewall configuration. Automatically generated by vdsm bootstrap script.
*filter
:INPUT ACCEPT [0:0]
:FORWARD ACCEPT [0:0]
:OUTPUT ACCEPT [0:0]
-A INPUT -m state --state ESTABLISHED,RELATED -j ACCEPT
-A INPUT -p icmp -j ACCEPT
-A INPUT -i lo -j ACCEPT
# vdsm
-A INPUT -p tcp --dport @VDSM_PORT@ -j ACCEPT
# ovirt-imageio-daemon
-A INPUT -p tcp --dport 54322 -j ACCEPT
# rpc.statd
-A INPUT -p tcp --dport 111 -j ACCEPT
-A INPUT -p udp --dport 111 -j ACCEPT
# SSH
-A INPUT -p tcp --dport @SSH_PORT@ -j ACCEPT
# snmp
-A INPUT -p udp --dport 161 -j ACCEPT
# Cockpit
-A INPUT -p tcp --dport 9090 -j ACCEPT

@CUSTOM_RULES@

# Reject any other input traffic
-A INPUT -j REJECT --reject-with icmp-host-prohibited
-A FORWARD -m physdev ! --physdev-is-bridged -j REJECT --reject-with icmp-host-prohibited
COMMIT
','general');
select fn_db_update_config_value('IPTablesConfigForGluster',
'
# glusterd
-A INPUT -p tcp -m tcp --dport 24007 -j ACCEPT

# gluster swift
-A INPUT -p tcp -m tcp --dport 8080  -j ACCEPT

# portmapper
-A INPUT -p tcp -m tcp --dport 38465 -j ACCEPT
-A INPUT -p tcp -m tcp --dport 38466 -j ACCEPT

# nfs
-A INPUT -p tcp -m tcp --dport 38467 -j ACCEPT
-A INPUT -p tcp -m tcp --dport 2049  -j ACCEPT
-A INPUT -p tcp -m tcp --dport 38469 -j ACCEPT

# nrpe
-A INPUT -p tcp --dport 5666 -j ACCEPT

# status
-A INPUT -p tcp -m tcp --dport 39543 -j ACCEPT
-A INPUT -p tcp -m tcp --dport 55863 -j ACCEPT

# nlockmgr
-A INPUT -p tcp -m tcp --dport 38468 -j ACCEPT
-A INPUT -p udp -m udp --dport 963   -j ACCEPT
-A INPUT -p tcp -m tcp --dport 965   -j ACCEPT

# ctdbd
-A INPUT -p tcp -m tcp --dport 4379  -j ACCEPT

# smbd
-A INPUT -p tcp -m tcp --dport 139   -j ACCEPT
-A INPUT -p tcp -m tcp --dport 445   -j ACCEPT

# Ports for gluster volume bricks (default 100 ports)
# Needed for Gluster < 3.4.0 compatibility
-A INPUT -p tcp -m tcp --dport 24009:24108 -j ACCEPT

# Ports required for GlusterFS brick processes have changed in
# glusterfs 3.4.0 from 24009 onwards to 49152 onwards.
-A INPUT -p tcp -m tcp --dport 49152:49251 -j ACCEPT

# Ports for gluster volume bricks in Hyper Converged setup(default 100 ports)
-A INPUT -p tcp -m tcp --dport 49217:49316 -j ACCEPT

','general');
select fn_db_update_config_value('IPTablesConfigForVirt',
'
# libvirt tls
-A INPUT -p tcp --dport 16514 -j ACCEPT

# serial consoles
-A INPUT -p tcp -m multiport --dports 2223 -j ACCEPT

# guest consoles
-A INPUT -p tcp -m multiport --dports 5900:6923 -j ACCEPT

# migration
-A INPUT -p tcp -m multiport --dports 49152:49216 -j ACCEPT
', 'general');
select fn_db_update_config_value('IsMultilevelAdministrationOn','true','general');
select fn_db_update_config_value('MinimalETLVersion','4.0.0','general');
select fn_db_update_config_value('OvirtInitialSupportedIsoVersion','2.5.5:5.8','general');
select fn_db_update_config_value('OvirtIsoPrefix','^ovirt-node-iso-([0-9].*)\.iso$:^rhevh-([0-9].*)\.iso$','general');
select fn_db_update_config_value('OvirtNodeOS','^ovirt.*$:^rhev.*$','general');
select fn_db_update_config_value('oVirtISOsRepositoryPath','/usr/share/ovirt-node-iso:/usr/share/rhev-hypervisor','general');
select fn_db_update_config_value('PackageNamesForCheckUpdate','ioprocess,mom,libvirt,lvm2,ovirt-imageio-common,ovirt-imageio-daemon,ovirt-vmconsole,ovirt-vmconsole-host,python-ioprocess,qemu-kvm,qemu-img,sanlock,vdsm,vdsm-cli','general');
select fn_db_update_config_value('PostgresPagingSyntax','OFFSET (%1$s -1) LIMIT %2$s','general');
select fn_db_update_config_value('PostgresSearchTemplate','SELECT * FROM (%2$s) %1$s) as T1 %3$s','general');
select fn_db_update_config_value('RhevhLocalFSPath','/data/images/rhev','general');
select fn_db_update_config_value('ClusterEmulatedMachines','pc-i440fx-rhel7.2.0,pc-i440fx-2.1,pseries-rhel7.2.0','3.6');
select fn_db_update_config_value('SpiceDriverNameInGuest','{"windows": "RHEV-Spice", "linux" : "xorg-x11-drv-qxl" }','general');
select fn_db_update_config_value('SupportedClusterLevels','3.6,4.0','general');
select fn_db_update_config_value('SupportedStorageFormats','0,2,3','3.6,4.0');
select fn_db_update_config_value('SupportedVDSMVersions','4.17','general');
select fn_db_update_config_value('VdcVersion','4.0.0.0','general');
select fn_db_update_config_value('ProductRPMVersion','4.0.0.0','general');
select fn_db_update_config_value('VdsFenceOptionMapping','apc:secure=secure,port=ipport,slot=port;apc_snmp:port=port,encrypt_options=encrypt_options;bladecenter:secure=secure,port=ipport,slot=port;cisco_ucs:secure=ssl,slot=port;drac5:secure=secure,slot=port;drac7:;eps:slot=port;hpblade:port=port;ilo:secure=ssl,port=ipport;ipmilan:;ilo2:secure=ssl,port=ipport;ilo3:;ilo4:;ilo_ssh:port=port;rsa:secure=secure,port=ipport;rsb:;wti:secure=secure,port=ipport,slot=port','3.6');
select fn_db_update_config_value('VdsFenceOptionMapping','apc:secure=secure,port=ipport,slot=port;apc_snmp:port=port,encrypt_options=encrypt_options;bladecenter:secure=secure,port=ipport,slot=port;cisco_ucs:secure=ssl,slot=port;drac5:secure=secure,slot=port;drac7:;eps:slot=port;hpblade:port=port;ilo:secure=ssl,port=ipport;ipmilan:;ilo2:secure=ssl,port=ipport;ilo3:;ilo4:;ilo_ssh:port=port;rsa:secure=secure,port=ipport;rsb:;wti:secure=secure,port=ipport,slot=port','4.0');
select fn_db_update_config_value('VdsFenceType','apc,apc_snmp,bladecenter,cisco_ucs,drac5,drac7,eps,hpblade,ilo,ilo2,ilo3,ilo4,ilo_ssh,ipmilan,rsa,rsb,wti','3.6');
select fn_db_update_config_value('VdsFenceType','apc,apc_snmp,bladecenter,cisco_ucs,drac5,drac7,eps,hpblade,ilo,ilo2,ilo3,ilo4,ilo_ssh,ipmilan,rsa,rsb,wti','4.0');
select fn_db_update_config_value('VdsRefreshRate','3','general');
select fn_db_update_config_value('VmGracefulShutdownMessage','System Administrator has initiated shutdown of this Virtual Machine. Virtual Machine is shutting down.','general');
select fn_db_update_config_value('DisconnectPoolOnReconstruct','0,2,3','general');
select fn_db_update_config_value('AgentAppName','ovirt-guest-agent-common,ovirt-guest-agent','general');
select fn_db_update_config_value('VM64BitMaxMemorySizeInMB','4194304','3.6');
select fn_db_update_config_value('VM64BitMaxMemorySizeInMB','4194304','4.0');
select fn_db_update_config_value('AutoStartVmsRunnerIntervalInSeconds','1','general');
select fn_db_update_config_value('AllowEditingHostedEngine','true','general');

-- enable migration, memory snapshot and suspend in the ppc64 architecture
select fn_db_update_config_value('IsMigrationSupported','{"undefined": "true", "x86_64": "true", "ppc64" : "true" }','general');
select fn_db_update_config_value('IsMemorySnapshotSupported','{"undefined": "true", "x86_64": "true", "ppc64" : "true" }','general');
select fn_db_update_config_value('IsSuspendSupported','{"undefined": "true", "x86_64": "true", "ppc64" : "true" }','general');

------------------------------------------------------------------------------------
--   Update only if default not changed section
------------------------------------------------------------------------------------

-- Increase AsyncTaskZombieTaskLifeInMinutes to 50 hours if it's the default 5 hours.
select fn_db_update_default_config_value('AsyncTaskZombieTaskLifeInMinutes','300','3000','general',false);
select fn_db_update_default_config_value('VdsLocalDisksCriticallyLowFreeSpace','100','500','general',false);
select fn_db_update_default_config_value('VdsLocalDisksLowFreeSpace','1000', '100','general',false);
select fn_db_update_default_config_value('GuestToolsSetupIsoPrefix','RHEV-toolsSetup_', 'ovirt-guest-tools-','general', false);

-- Reduce the host connection timeout from 180 seconds to 2 seconds and
-- disable retries for more predictable HA timing:
select fn_db_update_default_config_value('vdsConnectionTimeout', '180', '2', 'general', false);
select fn_db_update_default_config_value('vdsRetries', '3', '0', 'general', false);

-- Override existing configuration to TLSv1 if it is SSLv3
select fn_db_update_default_config_value('VdsmSSLProtocol','SSLv3','TLSv1','general', false);
select fn_db_update_default_config_value('ExternalCommunicationProtocol','SSLv3','TLSv1','general', false);

-- Adding no-TSX Intel processors in 3.6
select fn_db_update_default_config_value('ServerCPUList', '3:Intel Conroe Family:vmx,nx,model_Conroe:Conroe:x86_64; 4:Intel Penryn Family:vmx,nx,model_Penryn:Penryn:x86_64; 5:Intel Nehalem Family:vmx,nx,model_Nehalem:Nehalem:x86_64; 6:Intel Westmere Family:aes,vmx,nx,model_Westmere:Westmere:x86_64; 7:Intel SandyBridge Family:vmx,nx,model_SandyBridge:SandyBridge:x86_64; 8:Intel Haswell-noTSX Family:vmx,nx,model_Haswell-noTSX:Haswell-noTSX:x86_64; 9:Intel Haswell Family:vmx,nx,model_Haswell:Haswell:x86_64; 10:Intel Broadwell-noTSX Family:vmx,nx,model_Broadwell-noTSX:Broadwell-noTSX:x86_64; 11:Intel Broadwell Family:vmx,nx,model_Broadwell:Broadwell:x86_64; 2:AMD Opteron G1:svm,nx,model_Opteron_G1:Opteron_G1:x86_64; 3:AMD Opteron G2:svm,nx,model_Opteron_G2:Opteron_G2:x86_64; 4:AMD Opteron G3:svm,nx,model_Opteron_G3:Opteron_G3:x86_64; 5:AMD Opteron G4:svm,nx,model_Opteron_G4:Opteron_G4:x86_64; 6:AMD Opteron G5:svm,nx,model_Opteron_G5:Opteron_G5:x86_64; 3:IBM POWER 8:powernv,model_power8:power8:ppc64; 4:IBM POWER 8E:powernv,model_power8e:power8e:ppc64;', '3:Intel Conroe Family:vmx,nx,model_Conroe:Conroe:x86_64; 4:Intel Penryn Family:vmx,nx,model_Penryn:Penryn:x86_64; 5:Intel Nehalem Family:vmx,nx,model_Nehalem:Nehalem:x86_64; 6:Intel Westmere Family:aes,vmx,nx,model_Westmere:Westmere:x86_64; 7:Intel SandyBridge Family:vmx,nx,model_SandyBridge:SandyBridge:x86_64; 8:Intel Haswell-noTSX Family:vmx,nx,model_Haswell-noTSX:Haswell-noTSX:x86_64; 9:Intel Haswell Family:vmx,nx,model_Haswell:Haswell:x86_64; 10:Intel Broadwell-noTSX Family:vmx,nx,model_Broadwell-noTSX:Broadwell-noTSX:x86_64; 11:Intel Broadwell Family:vmx,nx,model_Broadwell:Broadwell:x86_64; 2:AMD Opteron G1:svm,nx,model_Opteron_G1:Opteron_G1:x86_64; 3:AMD Opteron G2:svm,nx,model_Opteron_G2:Opteron_G2:x86_64; 4:AMD Opteron G3:svm,nx,model_Opteron_G3:Opteron_G3:x86_64; 5:AMD Opteron G4:svm,nx,model_Opteron_G4:Opteron_G4:x86_64; 6:AMD Opteron G5:svm,nx,model_Opteron_G5:Opteron_G5:x86_64; 3:IBM POWER8:powernv,model_POWER8:POWER8:ppc64;', '3.6', FALSE);

-- Update mount points filter for storage device list
select fn_db_update_default_config_value('GlusterStorageDeviceListMountPointsToIgnore','/,/home,/boot,/run/gluster/snaps/.*', '/,/home,/boot,/run/gluster/snaps/.*,/var/run/gluster/snaps/.*','general', false);

-- Increase heartbeat interval from 10 to 30 seconds
select fn_db_update_default_config_value('vdsHeartbeatInSeconds','10','30','general',false);

-- Update VM name length
select fn_db_rename_config_key('MaxVmNameLengthNonWindows', 'MaxVmNameLength', 'general');
select fn_db_rename_config_key('MaxVmNameLengthWindows', 'MaxVmNameLengthSysprep', 'general');

-- automatically switch SPICE Plugin to Native
select fn_db_update_default_config_value('ClientModeSpiceDefault','Plugin','Native','general',false);

-- Minimal version of remote-viewer supporting "sso-token" in vv files, there is no build of remote-viewer supporting sso-token for rhel6
select fn_db_update_config_value('RemoteViewerSupportedVersions','rhev-win64:2.0-160;rhev-win32:2.0-160;rhel7:2.0-6;rhel6:99.0-1','general');

-- Increase connection timeout from 2 to 20 seconds
select fn_db_update_default_config_value('vdsConnectionTimeout','2','20','general',false);

-- Increase number of attempts during protocol detection
select fn_db_update_default_config_value('ProtocolFallbackRetries','3','25','general',false);

-- Lower default interval of DWH heartbeat from 30 to 15 seconds
select fn_db_update_default_config_value('DwhHeartBeatInterval', '30', '15', 'general', false);

------------------------------------------------------------------------------------
--                  SCALE
------------------------------------------------------------------------------------
-- Using host identifier as header when using ovirt-vdsmfake (drive by rhev-scale team)
select fn_db_add_config_value('UseHostNameIdentifier', 'false', 'general');

------------------------------------------------------------------------------------
--                  Split config section
-- The purpose of this section is to treat config option that was once
-- general, and should now be version-specific.
-- To ease this the fn_db_split_config_value can be used, input is the
-- option_name, the old value and the new value. Result is creating one row for each old
-- cluster level with the original value if exists, or the input old value
-- and from the update version and beyond, the input value.
------------------------------------------------------------------------------------
-- Gluster Tuned profile --
select fn_db_split_config_value('GlusterTunedProfile', 'rhs-high-throughput,rhs-virtualization', 'rhgs-sequential-io,rhgs-random-io', '3.6');

------------------------------------------------------------------------------------
--                  Simple direct updates section
------------------------------------------------------------------------------------

------------------------------------------------------------------------------------
--                 complex updates using a temporary function section
--                 each temporary function name should start with __temp
------------------------------------------------------------------------------------

create or replace function __temp_set_pg_major_release()
RETURNS void
AS $procedure$
DECLARE
    v_pg_major_release char(1);
BEGIN
    -- the folowing evaluates currently to 8 on PG 8.x and to 9 on PG 9.x
    v_pg_major_release:=substring ((string_to_array(version(),' '))[2],1,1);
    perform fn_db_add_config_value('PgMajorRelease',v_pg_major_release,'general');
    -- ensure that if PG was upgraded we will get the right value
    perform fn_db_update_config_value('PgMajorRelease',v_pg_major_release,'general');
END; $procedure$
LANGUAGE plpgsql;
SELECT  __temp_set_pg_major_release();
DROP FUNCTION __temp_set_pg_major_release();
