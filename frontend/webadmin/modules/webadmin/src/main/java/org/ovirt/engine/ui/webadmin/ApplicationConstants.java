package org.ovirt.engine.ui.webadmin;

import org.ovirt.engine.ui.common.CommonApplicationConstants;

public interface ApplicationConstants extends CommonApplicationConstants {

    @DefaultStringValue("oVirt Engine Web Administration")
    String applicationTitle();

    @DefaultStringValue("About")
    String aboutPopupCaption();

    @DefaultStringValue("This Browser version isn't optimal for displaying the application graphics (refer to Documentation for details)")
    String browserNotSupported();

    @DefaultStringValue("oVirt Engine Version:")
    String ovirtVersionAbout();

    // Widgets

    @DefaultStringValue("Refresh")
    String actionTableRefreshPageButtonLabel();

    // Login section

    @DefaultStringValue("User Name")
    String loginFormUserNameLabel();

    @DefaultStringValue("Password")
    String loginFormPasswordLabel();

    @DefaultStringValue("Profile")
    String loginFormProfileLabel();

    @DefaultStringValue("Login")
    String loginButtonLabel();

    // Main section

    @DefaultStringValue("Configure")
    String configureLinkLabel();

    @DefaultStringValue("Sign Out")
    String logoutLinkLabel();

    @DefaultStringValue("About")
    String aboutLinkLabel();

    @DefaultStringValue("Guide")
    String guideLinkLabel();

    @DefaultStringValue("Search")
    String searchLabel();

    @DefaultStringValue("GO")
    String searchButtonLabel();

    @DefaultStringValue("Data Centers")
    String dataCenterMainTabLabel();

    @DefaultStringValue("Clusters")
    String clusterMainTabLabel();

    @DefaultStringValue("Hosts")
    String hostMainTabLabel();

    @DefaultStringValue("Networks")
    String networkMainTabLabel();

    @DefaultStringValue("vNIC Profiles")
    String vnicProfilesMainTabLabel();

    @DefaultStringValue("Storage")
    String storageMainTabLabel();

    @DefaultStringValue("Virtual Machines")
    String virtualMachineMainTabLabel();

    @DefaultStringValue("Pools")
    String poolMainTabLabel();

    @DefaultStringValue("Templates")
    String templateMainTabLabel();

    @DefaultStringValue("Users")
    String userMainTabLabel();

    @DefaultStringValue("Quota")
    String quotaMainTabLabel();

    @DefaultStringValue("Volumes")
    String volumeMainTabLabel();

    @DefaultStringValue("Providers")
    String providerMainTabLabel();

    @DefaultStringValue("Sessions")
    String sessionMainTabLabel();

    @DefaultStringValue("General")
    String volumeGeneralSubTabLabel();

    @DefaultStringValue("Volume Options")
    String volumeParameterSubTabLabel();

    @DefaultStringValue("Bricks")
    String volumeBrickSubTabLabel();

    @DefaultStringValue("Geo-Replication")
    String volumeGeoRepSubTabLabel();

    @DefaultStringValue("Snapshots")
    String volumeSnapshotSubTabLabel();

    @DefaultStringValue("Destination Host")
    String volumeSubTabGeoRepSlaveClusterHostColumn();

    @DefaultStringValue("Destination Volume")
    String volumeSubTabGeoRepSlaveVolumeColumn();

    @DefaultStringValue("User Name")
    String volumeSubTabGeoRepSlaveUserColumn();

    @DefaultStringValue("Status")
    String volumeSubTabGeoRepStatusColumn();

    @DefaultStringValue("Up Time")
    String volumeSubTabGeoRepUptime();

    @DefaultStringValue("New")
    String newGeoRepSession();

    @DefaultStringValue("Remove")
    String removeGeoRepSession();

    @DefaultStringValue("Start")
    String startGeoRepSession();

    @DefaultStringValue("Stop")
    String stopGeoRepSession();

    @DefaultStringValue("Pause")
    String pauseGeoRepSession();

    @DefaultStringValue("Resume")
    String resumeGeoRepSession();

    @DefaultStringValue("Options")
    String geoRepSessionsOptions();

    @DefaultStringValue("View Details")
    String geoRepSessionDetails();

    @DefaultStringValue("Sync")
    String geoRepSessionSync();

    @DefaultStringValue("Permissions")
    String volumePermissionSubTabLabel();

    @DefaultStringValue("Events")
    String volumeEventSubTabLabel();

    @DefaultStringValue("Storage")
    String dataCenterStorageSubTabLabel();

    @DefaultStringValue("Logical Networks")
    String dataCenterNetworkSubTabLabel();

    @DefaultStringValue("This operation will replace the current master domain with the selected domain.<br/> After the operation is finished you will be able to remove the replaced domain if desired.")
    String dataCenterRecoveryStoragePopupMessageLabel();

    @DefaultStringValue("Select new Data Storage Domain(Master):")
    String dataCenterRecoveryStoragePopupSelectNewDSDLabel();

    @DefaultStringValue("The following operation is unrecoverable and destructive!")
    String dataCenterForceRemovePopupWarningLabel();

    @DefaultStringValue("Name")
    String clusterNewNetworkNameLabel();

    @DefaultStringValue("Description")
    String clusterNewNetworkDescriptionLabel();

    @DefaultStringValue("Scheduling Policy")
    String clusterPolicySchedulePolicyPanelTitle();

    @DefaultStringValue("Additional Properties")
    String clusterPolicyAdditionalPropsPanelTitle();

    @DefaultStringValue("Enable Trusted Service")
    String clusterPolicyEnableTrustedServiceLabel();

    @DefaultStringValue("Enable HA Reservation")
    String clusterPolicyEnableHaReservationLabel();

    @DefaultStringValue("Enable to set VM maintenance reason")
    String clusterPolicyEnableReasonLabel();

    @DefaultStringValue("Enable to set Host maintenance reason")
    String clusterPolicyEnableHostMaintenanceReasonLabel();

    @DefaultStringValue("KSM control")
    String ksmLabelTitle();

    @DefaultStringValue("Enable KSM")
    String enableKsmLabel();

    @DefaultStringValue("Enable Memory Balloon Optimization")
    String enableBallooningLabel();

    @DefaultStringValue("Memory Balloon")
    String ballooningLabelTitle();

    @DefaultStringValue("VM network")
    String clusterNewNetworkPopupVmNetworkLabel();

    @DefaultStringValue("Enable VLAN tagging")
    String clusterNewNetworkPopupVlanEnabledLabel();

    @DefaultStringValue("Override MTU")
    String clusterNewNetworkPopupMtuEnabledLabel();

    @DefaultStringValue("MTU")
    String clusterNewNetworkPopupMtuLabel();

    @DefaultStringValue("Select boxes to attach networks")
    String clusterManageNetworkPopupLabel();

    @DefaultStringValue("Clusters")
    String dataCenterClusterSubTabLabel();

    @DefaultStringValue("Quota")
    String dataCenterQuotaSubTabLabel();

    @DefaultStringValue("Network")
    String dataCenterNetworkQoSSubTabLabel();

    @DefaultStringValue("Host Network")
    String dataCenterHostNetworkQosSubTabLabel();

    @DefaultStringValue("Storage")
    String dataCenterStorageQosSubTabLabel();

    @DefaultStringValue("CPU")
    String dataCenterCpuQosSubTabLabel();

    @DefaultStringValue("QoS")
    String dataCenterQosSubTabLabel();

    @DefaultStringValue("Permissions")
    String dataCenterPermissionSubTabLabel();

    @DefaultStringValue("Events")
    String dataCenterEventSubTabLabel();

    @DefaultStringValue("Name")
    String nameLabel();

    @DefaultStringValue("Description")
    String descriptionLabel();

    @DefaultStringValue("Export")
    String exportLabel();

    @DefaultStringValue("vNIC Profiles")
    String profilesLabel();

    @DefaultStringValue("Create on external provider")
    String exportCheckboxLabel();

    @DefaultStringValue("External Provider")
    String externalProviderLabel();

    @DefaultStringValue("Physical Network")
    String neutronPhysicalNetwork();

    @DefaultStringValue("Host Provider")
    String hostProviderTabLabel();

    @DefaultStringValue("Discovered Hosts")
    String discoveredHostsLabel();

    @DefaultStringValue("Provisioned Hosts")
    String provisionedHostsLabel();

    @DefaultStringValue("Hosts Type")
    String hostProviderType();

    @DefaultStringValue("Host Groups")
    String hostGroupsLabel();

    @DefaultStringValue("Compute Resources")
    String computeResourceLabel();

    @DefaultStringValue("If you would like this host's networks to be provisioned by an external provider, please choose it from the list.")
    String externalProviderExplanation();

    @DefaultStringValue("Network Label")
    String networkLabel();

    @DefaultStringValue("Label")
    String networkLabelNetworksTab();

    @Override
    @DefaultStringValue("Comment")
    String commentLabel();

    @DefaultStringValue("Management Network")
    String managementNetworkLabel();

    @DefaultStringValue("VM network")
    String vmNetworkLabel();

    @DefaultStringValue("Enable VLAN tagging")
    String enableVlanTagLabel();

    @DefaultStringValue("MTU")
    String mtuLabel();

    @DefaultStringValue("Create subnet")
    String createSubnetLabel();

    @DefaultStringValue("Host Network QoS")
    String hostNetworkQos();

    @DefaultStringValue("Name")
    String nameClusterHeader();

    @DefaultStringValue("Storage Type")
    String dataCenterPopupStorageTypeLabel();

    @DefaultStringValue("Shared")
    String storageTypeShared();

    @DefaultStringValue("Local")
    String storageTypeLocal();

    @DefaultStringValue("Compatibility Version")
    String dataCenterPopupVersionLabel();

    @DefaultStringValue("Quota Mode")
    String dataCenterPopupQuotaEnforceTypeLabel();

    @DefaultStringValue("MAC Address Pool")
    String dataCenterPopupMacPoolLabel();

    @DefaultStringValue("Edit Network Parameters")
    String dataCenterEditNetworkPopupLabel();

    @DefaultStringValue("Network Parameters")
    String dataCenterNewNetworkPopupLabel();

    @DefaultStringValue("To allow editing the network parameters, <b>detach all Clusters</b> and <b>click Apply</b>")
    String dataCenterNetworkPopupSubLabel();

    @DefaultStringValue("Attach/Detach Network to/from Cluster(s)")
    String networkPopupAssignLabel();

    @DefaultStringValue("Attach All")
    String attachAll();

    @DefaultStringValue("Attach")
    String attach();

    @DefaultStringValue("Assign All")
    String assignAll();

    @DefaultStringValue("Assign")
    String assign();

    @DefaultStringValue("Required All")
    String requiredAll();

    @DefaultStringValue("Required")
    String required();

    @DefaultStringValue("Name")
    String storagePopupNameLabel();

    @DefaultStringValue("Description")
    String storagePopupDescriptionLabel();

    @DefaultStringValue("Data Center")
    String storagePopupDataCenterLabel();

    @DefaultStringValue("Domain Function / Storage Type")
    String storagePopupStorageTypeLabel();

    @DefaultStringValue("Format")
    String storagePopupFormatTypeLabel();

    @DefaultStringValue("Use Host")
    String storagePopupHostLabel();

    @DefaultStringValue("Activate Domain in Data Center")
    String activateDomainLabel();

    @DefaultStringValue("Wipe After Delete")
    String wipeAfterDelete();

    @DefaultStringValue("Export Path")
    String storagePopupNfsPathLabel();

    @DefaultStringValue("Custom Connection Parameters")
    String storagePopupConnectionLabel();

    @DefaultStringValue("Override Default Options")
    String storagePopupNfsOverrideLabel();

    @DefaultStringValue("NFS Version")
    String storagePopupNfsVersionLabel();

    @DefaultStringValue("Retransmissions (#)")
    String storagePopupNfsRetransmissionsLabel();

    @DefaultStringValue("Timeout (deciseconds)")
    String storagePopupNfsTimeoutLabel();

    @DefaultStringValue("Path")
    String storagePopupPosixPathLabel();

    @DefaultStringValue("VFS Type")
    String storagePopupVfsTypeLabel();

    @DefaultStringValue("Mount Options")
    String storagePopupMountOptionsLabel();

    @DefaultStringValue("Additional mount options")
    String storagePopupAdditionalMountOptionsLabel();

    @DefaultStringValue("Path")
    String storagePopupLocalPathLabel();

    @DefaultStringValue("Remote path to NFS export, takes either the form: FQDN:/path or IP:/path e.g. server.example.com:/export/VMs")
    String storagePopupNfsPathHintLabel();

    @DefaultStringValue("Path to gluster volume to mount")
    String storagePopupGlusterPathHintLabel();

    @DefaultStringValue("Path to device to mount / remote export")
    String storagePopupPosixPathHintLabel();

    @DefaultStringValue("Adding an NFS domain as POSIX is highly discouraged, please create an NFS domain for best optimizations")
    String storagePopupPosixNfsWarningLabel();

    @DefaultStringValue("Select Host to be used")
    String storageRemovePopupHostLabel();

    @DefaultStringValue("Format Domain, i.e. Storage Content will be lost!")
    String storageRemovePopupFormatLabel();

    @DefaultStringValue("The following operation is unrecoverable and destructive!")
    String storageDestroyPopupWarningLabel();

    @DefaultStringValue("General")
    String storageGeneralSubTabLabel();

    @DefaultStringValue("Data Center")
    String storageDataCenterSubTabLabel();

    @DefaultStringValue("VM Import")
    String storageVmBackupSubTabLabel();

    @DefaultStringValue("Template Import")
    String storageTemplateBackupSubTabLabel();

    @DefaultStringValue("Virtual Machines")
    String storageVmSubTabLabel();

    @DefaultStringValue("Templates")
    String storageTemplateSubTabLabel();

    @DefaultStringValue("Images")
    String storageImagesSubTabLabel();

    @DefaultStringValue("Permissions")
    String storagePermissionSubTabLabel();

    @DefaultStringValue("Events")
    String storageEventSubTabLabel();

    @DefaultStringValue("General")
    String clusterGeneralSubTabLabel();

    @DefaultStringValue("Policy")
    String clusterPolicySubTabLabel();

    @DefaultStringValue("Hosts")
    String clusterHostSubTabLabel();

    @DefaultStringValue("Virtual Machines")
    String clusterVmSubTabLabel();

    @DefaultStringValue("Logical Networks")
    String clusterNetworkSubTabLabel();

    @DefaultStringValue("Services")
    String clusterServiceSubTabLabel();

    @DefaultStringValue("Gluster Hooks")
    String clusterGlusterHooksSubTabLabel();

    @DefaultStringValue("Affinity Groups")
    String affinityGroupSubTabLabel();

    @DefaultStringValue("Permissions")
    String clusterPermissionSubTabLabel();

    @DefaultStringValue("General")
    String virtualMachineGeneralSubTabLabel();

    @DefaultStringValue("Network Interfaces")
    String virtualMachineNetworkInterfaceSubTabLabel();

    @DefaultStringValue("Disks")
    String virtualMachineVirtualDiskSubTabLabel();

    @DefaultStringValue("Snapshots")
    String virtualMachineSnapshotSubTabLabel();

    @DefaultStringValue("Applications")
    String virtualMachineApplicationSubTabLabel();

    @DefaultStringValue("Permissions")
    String virtualMachinePermissionSubTabLabel();

    @DefaultStringValue("Sessions")
    String virtualMachineSessionsSubTabLabel();

    @DefaultStringValue("Events")
    String virtualMachineEventSubTabLabel();

    @DefaultStringValue("General")
    String hostGeneralSubTabLabel();

    @DefaultStringValue("Info")
    String hostGeneralInfoSubTabLabel();

    @DefaultStringValue("Software")
    String hostGeneralSoftwareSubTabLabel();

    @DefaultStringValue("Hardware")
    String hostGeneralHardwareSubTabLabel();

    @DefaultStringValue("Virtual Machines")
    String hostVmSubTabLabel();

    @DefaultStringValue("Network Interfaces")
    String hostIfaceSubTabLabel();

    @DefaultStringValue("Host Hooks")
    String hostHookSubTabLabel();

    @DefaultStringValue("Gluster Swift")
    String hostGlusterSwiftSubTabLabel();

    @DefaultStringValue("Bricks")
    String hostBricksSubTabLabel();

    @DefaultStringValue("Permissions")
    String hostPermissionSubTabLabel();

    @DefaultStringValue("Events")
    String hostEventSubTabLabel();

    @DefaultStringValue("General")
    String hostPopupGeneralTabLabel();

    @DefaultStringValue("Power Management")
    String hostPopupPowerManagementTabLabel();

    @DefaultStringValue("Memory Optimization")
    String hostPopupMemoryOptimizationTabLabel();

    @DefaultStringValue("Data Center")
    String hostPopupDataCenterLabel();

    @DefaultStringValue("Host Cluster")
    String hostPopupClusterLabel();

    @DefaultStringValue("Name")
    String hostPopupNameLabel();

    @DefaultStringValue("SSH Port")
    String hostPopupPortLabel();

    @DefaultStringValue("Address")
    String hostPopupHostAddressLabel();

    @DefaultStringValue("SSH Fingerprint")
    String hostPopupHostFingerprintLabel();

    @DefaultStringValue("User Name")
    String hostPopupUsernameLabel();

    @DefaultStringValue("root")
    String hostPopupDefaultUsername();

    @DefaultStringValue("SSH Public Key")
    String hostPopupPublicKeyLable();

    @DefaultStringValue("Password")
    String hostPopupPasswordLabel();

    @DefaultStringValue("Authentication")
    String hostPopupAuthLabel();

    @DefaultStringValue("Set Root Password")
    String hostPopupAuthLabelForExternalHost();

    @DefaultStringValue("Automatically configure host firewall")
    String hostPopupOverrideIpTablesLabel();

    @DefaultStringValue("Use JSON protocol")
    String hostPopupProtocolLabel();

    @DefaultStringValue("Use Foreman Hosts Providers")
    String hostPopupEnableExternalHostProvider();

    @DefaultStringValue("Providers Hosts")
    String hostPopupExternalHostName();

    @DefaultStringValue("Update Hosts")
    String hostPopupUpdateHosts();

    @DefaultStringValue("Provider search filter")
    String hostPopupProviderSearchFilter();

    @DefaultStringValue("Enable Power Management")
    String hostPopupPmEnabledLabel();

    @DefaultStringValue("Concurrent")
    String hostPopupPmConcurrent();

    @DefaultStringValue("Address")
    String hostPopupPmAddressLabel();

    @DefaultStringValue("User Name")
    String hostPopupPmUserNameLabel();

    @DefaultStringValue("Password")
    String hostPopupPmPasswordLabel();

    @DefaultStringValue("Type")
    String hostPopupPmTypeLabel();

    @DefaultStringValue("SSH Port")
    String hostPopupPmPortLabel();

    @DefaultStringValue("Slot")
    String hostPopupPmSlotLabel();

    @DefaultStringValue("Service Profile")
    String hostPopupPmCiscoUcsSlotLabel();

    @DefaultStringValue("Options")
    String hostPopupPmOptionsLabel();

    @DefaultStringValue("Please use a comma-separated list of 'key=value' or 'key'")
    String hostPopupPmOptionsExplanationLabel();

    @DefaultStringValue("Secure")
    String hostPopupPmSecureLabel();

    @DefaultStringValue("Encrypt options")
    String hostPopupPmEncryptOptionsLabel();

    @DefaultStringValue("Disable policy control of power management")
    String hostPopupPmDisableAutoPM();

    @DefaultStringValue("Kdump integration")
    String hostPopupPmKdumpDetection();

    @DefaultStringValue("Test")
    String hostPopupTestButtonLabel();

    @DefaultStringValue("Up")
    String hostPopupUpButtonLabel();

    @DefaultStringValue("Down")
    String hostPopupDownButtonLabel();

    @DefaultStringValue("Fetch")
    String hostPopupFetchButtonLabel();

    @DefaultStringValue("Source")
    String hostPopupSourceText();

    @DefaultStringValue("SPM")
    String spmTestButtonLabel();

    @DefaultStringValue("Console")
    String consoleButtonLabel();

    @DefaultStringValue("Network Provider")
    String networkProviderButtonLabel();

    @DefaultStringValue("External Network Provider")
    String externalNetworkProviderLabel();

    @DefaultStringValue("Override display address")
    String enableConsoleAddressOverride();

    @DefaultStringValue("Overrides the display address of all VMs on this host by the specified address")
    String enableConsoleAddressOverrideHelpMessage();

    @DefaultStringValue("Display address")
    String consoleAddress();

    @DefaultStringValue("Never")
    String spmNeverText();

    @DefaultStringValue("Low")
    String spmLowText();

    @DefaultStringValue("Normal")
    String spmNormalText();

    @DefaultStringValue("High")
    String spmHighText();

    @DefaultStringValue("Custom")
    String spmCustomText();

    @DefaultStringValue("Root Password")
    String hostInstallPasswordLabel();

    @DefaultStringValue("Current version")
    String hostInstallHostVersionLabel();

    @DefaultStringValue("ISO Name")
    String hostInstallIsoLabel();

    @DefaultStringValue("Automatically configure host firewall")
    String hostInstallOverrideIpTablesLabel();

    @DefaultStringValue("Activate host after install")
    String activateHostAfterInstallLabel();

    @DefaultStringValue("General")
    String importVmGeneralSubTabLabel();

    @DefaultStringValue("Network Interfaces")
    String importVmNetworkIntefacesSubTabLabel();

    @DefaultStringValue("Disks")
    String importVmDisksSubTabLabel();

    @DefaultStringValue("Applications")
    String importVmApplicationslSubTabLabel();

    @DefaultStringValue("Set the path to your local storage:")
    String configureLocalStoragePopupPathLabel();

    @DefaultStringValue("Executing this operation on a Host that was not properly manually rebooted could lead to a condition where VMs start on multiple hosts and lead to VM corruption!")
    String manaulFencePopupNoneSpmWarningLabel();

    @DefaultStringValue("This Host is the SPM. Executing this operation on a Host that was not properly manually rebooted could lead to Storage corruption condition!")
    String manaulFencePopupSpmWarningLabel();

    @DefaultStringValue("This Host is Contending to be SPM. Executing this operation on a Host that was not properly manually rebooted could lead to Storage corruption condition!")
    String manaulFencePopupContendingSpmWarningLabel();

    @DefaultStringValue("If the host has not been manually rebooted hit 'Cancel'.")
    String manaulFencePopupWarningLabel();

    @DefaultStringValue("General")
    String poolGeneralSubTabLabel();

    @DefaultStringValue("Virtual Machines")
    String poolVmSubTabLabel();

    @DefaultStringValue("Permissions")
    String poolPermissionSubTabLabel();

    @DefaultStringValue("General")
    String templateGeneralSubTabLabel();

    @DefaultStringValue("Virtual Machines")
    String templateVmSubTabLabel();

    @DefaultStringValue("Network Interfaces")
    String templateInterfaceSubTabLabel();

    @DefaultStringValue("Disks")
    String templateDiskSubTabLabel();

    @DefaultStringValue("Storage")
    String templateStorageSubTabLabel();

    @DefaultStringValue("Permissions")
    String templatePermissionSubTabLabel();

    @DefaultStringValue("Events")
    String templateEventSubTabLabel();

    @DefaultStringValue("General")
    String userGeneralSubTabLabel();

    @DefaultStringValue("Permissions")
    String userPermissionSubTabLabel();

    @DefaultStringValue("Quota")
    String userQuotaSubTabLabel();

    @DefaultStringValue("Directory Groups")
    String userGroupsSubTabLabel();

    @DefaultStringValue("Event Notifier")
    String userEventNotifierSubTabLabel();

    @DefaultStringValue("Events")
    String userEventSubTabLabel();

    @DefaultStringValue("Events")
    String eventMainTabLabel();

    @DefaultStringValue("Dashboard")
    String reportsMainTabLabel();

    @DefaultStringValue("Basic View")
    String eventBasicViewLabel();

    @DefaultStringValue("Advanced View")
    String eventAdvancedViewLabel();

    @DefaultStringValue("General")
    String clusterPopupGeneralTabLabel();

    @DefaultStringValue("Data Center")
    String clusterPopupDataCenterLabel();

    @DefaultStringValue("Name")
    String clusterPopupNameLabel();

    @DefaultStringValue("Description")
    String clusterPopupDescriptionLabel();

    @DefaultStringValue("CPU Type")
    String clusterPopupCPUTypeLabel();

    @DefaultStringValue("CPU Architecture")
    String clusterPopupArchitectureLabel();

    @DefaultStringValue("Compatibility Version")
    String clusterPopupVersionLabel();

    @DefaultStringValue("Optimization")
    String clusterPopupOptimizationTabLabel();

    @DefaultStringValue("Memory Optimization")
    String clusterPopupMemoryOptimizationPanelTitle();

    @DefaultStringValue("Allow VMs to run on the hosts up to the specified overcommit threshold." +
            " Higher values conserve memory at the expense of greater host CPU usage.")
    String clusterPopupMemoryOptimizationInfo();

    @DefaultStringValue("None - Disable memory overcommit")
    String clusterPopupOptimizationNoneLabel();

    @DefaultStringValue("CPU Threads")
    String clusterPopupCpuThreadsPanelTitle();

    @DefaultStringValue("Allow guests to use host threads as virtual CPU cores, utilizing AMD Clustered MultiThreading or Intel"
            +
            " Hyper-Threading technology on the virtualization host. Enabling this option may be useful for less" +
            " CPU-intensive workloads, or to run guests with CPU configurations that would otherwise be restricted.")
    String clusterPopupCpuThreadsInfo();

    @DefaultStringValue("Count Threads As Cores")
    String clusterPopupCountThreadsAsCoresLabel();

    @DefaultStringValue("Resilience Policy")
    String clusterPopupResiliencePolicyTabLabel();

    @DefaultStringValue("Scheduling Policy")
    String clusterPopupClusterPolicyTabLabel();

    @DefaultStringValue("Migrate Virtual Machines")
    String clusterPopupMigrateOnError_YesLabel();

    @DefaultStringValue("Migrate Only Highly Available Virtual Machines")
    String clusterPopupMigrateOnError_HaLabel();

    @DefaultStringValue("Do Not Migrate Virtual Machines")
    String clusterPopupMigrateOnError_NoLabel();

    @DefaultStringValue("Override the default SPICE proxy value")
    String clusterSpiceProxyInfo();

    @DefaultStringValue("Define SPICE proxy for Cluster")
    String clusterSpiceProxyEnable();

    @DefaultStringValue("Enable fencing")
    String fencingEnabled();

    @DefaultStringValue("Skip fencing if host has live lease on storage")
    String skipFencingIfSDActive();

    @DefaultStringValue("Skip fencing on cluster connectivity issues")
    String skipFencingWhenConnectivityBroken();

    @DefaultStringValue("Threshold")
    String hostsWithBrokenConnectivityThresholdLabel();

    @DefaultStringValue("Name")
    String bookmarkPopupNameLabel();

    @DefaultStringValue("Search string")
    String bookmarkPopupSearchStringLabel();

    @DefaultStringValue("Name")
    String tagPopupNameLabel();

    @DefaultStringValue("Description")
    String tagPopupDescriptionLabel();

    @DefaultStringValue("None")
    String clusterPolicyNoneLabel();

    @DefaultStringValue("Even Distribution")
    String clusterPolicyEvenDistLabel();

    @DefaultStringValue("Power Saving")
    String clusterPolicyPowSaveLabel();

    @DefaultStringValue("Maximum Service Level")
    String clusterPolicyMaxServiceLevelLabel();

    @DefaultStringValue("Minimum Service Level")
    String clusterPolicyMinServiceLevelLabel();

    @DefaultStringValue("for")
    String clusterPolicyForTimeLabel();

    @DefaultStringValue("min.")
    String clusterPolicyMinTimeLabel();

    @DefaultStringValue("Edit Policy")
    String clusterPolicyEditPolicyButtonLabel();

    @DefaultStringValue("Volume Details")
    String clusterVolumesLabel();

    @DefaultStringValue("Total No. Of Volumes")
    String clusterVolumesTotalLabel();

    @DefaultStringValue("No. Of Volumes Up")
    String clusterVolumesUpLabel();

    @DefaultStringValue("No. Of Volumes Down")
    String clusterVolumesDownLabel();

    @DefaultStringValue("Gluster Swift Status: ")
    String clusterGlusterSwiftLabel();

    @DefaultStringValue("Manage")
    String clusterGlusterSwiftManageLabel();

    @DefaultStringValue("Start")
    String startGlusterSwift();

    @DefaultStringValue("Stop")
    String stopGlusterSwift();

    @DefaultStringValue("Restart")
    String restartGlusterSwift();

    @DefaultStringValue("Manage swift on individual servers")
    String manageServerLevelGlusterSwift();

    @DefaultStringValue("Server")
    String hostGlusterSwift();

    @DefaultStringValue("Service")
    String serviceNameGlusterSwift();

    @DefaultStringValue("Status")
    String serviceStatusGlusterSwift();

    @DefaultStringValue("Policy:")
    String clusterPolicyPolicyLabel();

    @DefaultStringValue("")
    String copyRightNotice();

    @DefaultStringValue("Configure")
    String configurePopupTitle();

    // Role view
    @DefaultStringValue("All Roles")
    String allRolesLabel();

    @DefaultStringValue("Administrator Roles")
    String adminRolesLabel();

    @DefaultStringValue("User Roles")
    String userRolesLabel();

    @DefaultStringValue("Show")
    String showRolesLabel();

    @DefaultStringValue("Name")
    String RoleNameLabel();

    @DefaultStringValue("Description")
    String RoleDescriptionLabel();

    @DefaultStringValue("Account Type:")
    String RoleAccount_TypeLabel();

    @DefaultStringValue("User")
    String RoleUserLabel();

    @DefaultStringValue("Admin")
    String RoleAdminLabel();

    @DefaultStringValue("Check Boxes to Allow Action")
    String RoleCheckBoxes();

    @DefaultStringValue("Expand All")
    String RoleExpand_AllLabel();

    @DefaultStringValue("Collapse All")
    String RoleCollapse_AllLabel();

    @DefaultStringValue("Roles")
    String configureRoleTabLabel();

    @DefaultStringValue("Name")
    String nameRole();

    @DefaultStringValue("Description")
    String descriptionRole();

    @DefaultStringValue("New")
    String newRole();

    @DefaultStringValue("Edit")
    String editRole();

    @DefaultStringValue("Copy")
    String copyRole();

    @DefaultStringValue("Remove")
    String removeRole();

    @DefaultStringValue("System Permissions")
    String configureSystemPermissionTabLabel();

    @DefaultStringValue("Force Override")
    String vmExportPopupForceOverrideLabel();

    @DefaultStringValue("Collapse Snapshots")
    String vmExportPopupCollapseSnapshotsLabel();

    @DefaultStringValue("Select Host Automatically")
    String vmMigratePopupSelectHostAutomaticallyLabel();

    @DefaultStringValue("Select Destination Host")
    String vmMigratePopupSelectDestinationHostLabel();

    @DefaultStringValue("Host:")
    String vmMigratePopupHostsListLabel();

    @DefaultStringValue("Cluster")
    String importVm_destCluster();

    @DefaultStringValue("Cluster Quota")
    String importVm_destClusterQuota();

    @DefaultStringValue("Quota exceeded")
    String quotaExceeded();

    @DefaultStringValue("Expand All")
    String treeExpandAll();

    @DefaultStringValue("Collapse All")
    String treeCollapseAll();

    @DefaultStringValue("Mail Recipient:")
    String manageEventsPopupEmailLabel();

    @DefaultStringValue("Select the Events for Notification:")
    String manageEventsPopupTitleLabel();

    @DefaultStringValue("Note: To receive email notifications, ensure that the mail server is configured and the ovirt-event-notifier service is running.")
    String manageEventsPopupInfoLabel();

    @DefaultStringValue("Devices to configure:")
    String guidePopupRequiredActionsLabel();

    @DefaultStringValue("Optional actions:")
    String guidePopupOptionalActionsLabel();

    @DefaultStringValue("There are still unconfigured entities:")
    String guidePopupUnconfiguredLabel();

    @DefaultStringValue("Configuration completed.")
    String guidePopupConfigurationCompletedLabel();

    @DefaultStringValue("Data Center created.")
    String guidePopupDataCenterCreatedLabel();

    @DefaultStringValue("The Data Center is fully configured and ready for use.")
    String guidePopupConfiguredDataCenterLabel();

    @DefaultStringValue("Cluster created.")
    String guidePopupClusterCreatedLabel();

    @DefaultStringValue("The Cluster is fully configured and ready for use.")
    String guidePopupConfiguredClusterLabel();

    @DefaultStringValue("Virtual Machine created.")
    String guidePopupVMCreatedLabel();

    @DefaultStringValue("The Virtual Machine is fully configured and ready for use.")
    String guidePopupConfiguredVmLabel();

    @DefaultStringValue("Cluster:")
    String moveHostPopupClusterLabel();

    @DefaultStringValue("Please select entities from the same data center")
    String entitiesFromDifferentDCsError();

    @DefaultStringValue("Resides on a different storage domain")
    String differentStorageDomainWarning();

    @DefaultStringValue("Edit")
    String editText();

    @DefaultStringValue("Close")
    String closeText();

    @DefaultStringValue("Allocation can be modified only when importing a single VM")
    String importAllocationModifiedSingleVM();

    @DefaultStringValue("Allocation can be modified only when 'Collapse Snapshots' is checked")
    String importAllocationModifiedCollapse();

    @DefaultStringValue("Preallocated")
    String preallocatedAllocation();

    @DefaultStringValue("Thin Provision")
    String thinAllocation();

    @DefaultStringValue("Clusters")
    String quotaClusterSubTabLabel();

    @DefaultStringValue("Enable Virt Service")
    String clusterEnableOvirtServiceLabel();

    @DefaultStringValue("Enable Gluster Service")
    String clusterEnableGlusterServiceLabel();

    @DefaultStringValue("Import existing gluster configuration")
    String clusterImportGlusterConfigurationLabel();

    @DefaultStringValue("Enter the details of any server in the cluster")
    String clusterImportGlusterConfigurationExplanationLabel();

    @DefaultStringValue("Please verify the fingerprint of the host before proceeding")
    String clusterImportGlusterFingerprintInfoLabel();

    @DefaultStringValue("Storage")
    String quotaStorageSubTabLabel();

    @DefaultStringValue("Consumers")
    String quotaUserSubTabLabel();

    @DefaultStringValue("Permissions")
    String quotaPermissionSubTabLabel();

    @DefaultStringValue("Events")
    String quotaEventSubTabLabel();

    @DefaultStringValue("VMs")
    String quotaVmSubTabLabel();

    @DefaultStringValue("Templates")
    String quotaTemplateSubTabLabel();

    @DefaultStringValue("Disks")
    String diskMainTabLabel();

    @DefaultStringValue("General")
    String diskGeneralSubTabLabel();

    @DefaultStringValue("Virtual Machines")
    String diskVmSubTabLabel();

    @DefaultStringValue("Permissions")
    String diskPermissionSubTabLabel();

    @DefaultStringValue("Templates")
    String diskTemplateSubTabLabel();

    @DefaultStringValue("Storage")
    String diskStorageSubTabLabel();

    // DC
    @DefaultStringValue("New")
    String newDC();

    @DefaultStringValue("Edit")
    String editDC();

    @DefaultStringValue("Remove")
    String removeDC();

    @DefaultStringValue("Force Remove")
    String forceRemoveDC();

    @DefaultStringValue("Show Report")
    String showReportDC();

    @DefaultStringValue("Guide Me")
    String guideMeDc();

    @DefaultStringValue("Re-Initialize Data Center")
    String reinitializeDC();

    @DefaultStringValue("Name")
    String nameDc();

    @DefaultStringValue("Storage Type")
    String storgeTypeDc();

    @DefaultStringValue("Status")
    String statusDc();

    @DefaultStringValue("Compatibility Version")
    String comptVersDc();

    @DefaultStringValue("Description")
    String descriptionDc();

    @DefaultStringValue("MAC Address Pools")
    String configureMacPoolsTabLabel();

    @DefaultStringValue("Name")
    String configureMacPoolNameColumn();

    @DefaultStringValue("Description")
    String configureMacPoolDescriptionColumn();

    @DefaultStringValue("Add")
    String configureMacPoolAddButton();

    @DefaultStringValue("Edit")
    String configureMacPoolEditButton();

    @DefaultStringValue("Remove")
    String configureMacPoolRemoveButton();

    @DefaultStringValue("Name")
    String macPoolPopupName();

    @DefaultStringValue("Description")
    String macPoolPopupDescription();

    @DefaultStringValue("General")
    String dataCenterGeneralTab();

    @DefaultStringValue("MAC Address Pool")
    String dataCenterMacPoolTab();

    // Storage DC
    @DefaultStringValue("Domain status in Data Center")
    String domainStatusInDcStorageDc();

    @DefaultStringValue("Attach")
    String attachStorageDc();

    @DefaultStringValue("Detach")
    String detachStorageDc();

    @DefaultStringValue("Activate")
    String activateStorageDc();

    @DefaultStringValue("Maintenance")
    String maintenanceStorageDc();

    // Network
    @DefaultStringValue("General")
    String generalTabNetworkPopup();

    @DefaultStringValue("Cluster")
    String clusterTabNetworkPopup();

    @DefaultStringValue("vNIC Profiles")
    String profilesTabNetworkPopup();

    @DefaultStringValue("Subnet")
    String subnetTabNetworkPopup();

    @DefaultStringValue("General")
    String networkGeneralSubTabLabel();

    @DefaultStringValue("Subnets")
    String networkExternalSubnetSubTabLabel();

    @DefaultStringValue("Permissions")
    String profilePermissions();

    @DefaultStringValue("Clusters")
    String networkClusterSubTabLabel();

    @DefaultStringValue("Hosts")
    String networkHostSubTabLabel();

    @DefaultStringValue("Virtual Machines")
    String networkVmSubTabLabel();

    @DefaultStringValue("Templates")
    String networkTemplateSubTabLabel();

    @DefaultStringValue("Permissions")
    String networkPermissionSubTabLabel();

    @DefaultStringValue("Data Center")
    String networkPopupDataCenterLabel();

    // Quota Storage
    @DefaultStringValue("Name")
    String nameQuotaStorage();

    @DefaultStringValue("Used Storage/Total")
    String usedStorageTotalQuotaStorage();

    // vNIC Profile
    @DefaultStringValue("Network")
    String networkVnicProfile();

    @DefaultStringValue("Name")
    String nameVnicProfile();

    @DefaultStringValue("Data Center")
    String dcVnicProfile();

    @DefaultStringValue("Compatibility Version")
    String compatibilityVersionVnicProfile();

    @DefaultStringValue("Description")
    String descriptionVnicProfile();

    @DefaultStringValue("QoS Name")
    String qosNameVnicProfile();

    @DefaultStringValue("Port Mirroring")
    String portMirroringVnicProfile();

    @DefaultStringValue("Allow all users to use this Profile")
    String publicUseVnicProfile();

    @DefaultStringValue("Virtual Machines")
    String vnicProfileVmSubTabLabel();

    @DefaultStringValue("Templates")
    String vnicProfileTemplateSubTabLabel();

    @DefaultStringValue("Permissions")
    String vnicProfilePermissionSubTabLabel();

    @DefaultStringValue("New")
    String newVnicProfile();

    @DefaultStringValue("Edit")
    String editVnicProfile();

    @DefaultStringValue("Remove")
    String removeVnicProfile();

    // External Subnet
    @DefaultStringValue("External ID")
    String externalIdExternalSubnet();

    @DefaultStringValue("Network")
    String networkExternalSubnet();

    @DefaultStringValue("Name")
    String nameExternalSubnet();

    @DefaultStringValue("CIDR")
    String cidrExternalSubnet();

    @DefaultStringValue("IP Version")
    String ipVersionExternalSubnet();

    @DefaultStringValue("Gateway")
    String gatewayExternalSubnet();

    @DefaultStringValue("DNS Servers")
    String dnsServersExternalSubnet();

    // Cluster
    @DefaultStringValue("Name")
    String nameCluster();

    @DefaultStringValue("Data Center")
    String dcCluster();

    @DefaultStringValue("Attached Network")
    String attachedNetworkCluster();

    @DefaultStringValue("Compatibility Version")
    String comptVersCluster();

    @DefaultStringValue("Required Network")
    String requiredNetCluster();

    @DefaultStringValue("Network Role")
    String roleNetCluster();

    @DefaultStringValue("Description")
    String descriptionCluster();

    @DefaultStringValue("Host Count")
    String hostCount();

    @DefaultStringValue("VM Count")
    String vmCount();

    @DefaultStringValue("Cluster CPU Type")
    String cpuTypeCluster();

    @DefaultStringValue("New")
    String newCluster();

    @DefaultStringValue("Edit")
    String editCluster();

    @DefaultStringValue("Remove")
    String removeCluster();

    @DefaultStringValue("Show Report")
    String showReportCluster();

    @DefaultStringValue("Guide Me")
    String guideMeCluster();

    @DefaultStringValue("Reset Emulated Machine")
    String resetClusterEmulatedMachine();

    @DefaultStringValue("Used Memory/Total")
    String usedMemoryTotalCluster();

    @DefaultStringValue("Running CPU/Total")
    String runningCpuTotalCluster();

    // Host
    @DefaultStringValue("Name")
    String nameHost();

    @DefaultStringValue("Hostname/IP")
    String ipHost();

    @DefaultStringValue("Cluster")
    String clusterHost();

    @DefaultStringValue("Data Center")
    String dcHost();

    @DefaultStringValue("Status")
    String statusHost();

    @DefaultStringValue("Virtual Machines")
    String vmsCount();

    @DefaultStringValue("Memory")
    String memoryHost();

    @DefaultStringValue("CPU")
    String cpuHost();

    @DefaultStringValue("Network")
    String networkHost();

    @DefaultStringValue("SPM")
    String spmPriorityHost();

    @DefaultStringValue("New")
    String newHost();

    @DefaultStringValue("Edit")
    String editHost();

    @DefaultStringValue("Remove")
    String removeHost();

    @DefaultStringValue("Activate")
    String activateHost();

    @DefaultStringValue("Maintenance")
    String maintenanceHost();

    @DefaultStringValue("Select as SPM")
    String selectHostAsSPM();

    @DefaultStringValue("Confirm 'Host has been Rebooted'")
    String confirmRebootedHost();

    @DefaultStringValue("Approve")
    String approveHost();

    @DefaultStringValue("Reinstall")
    String reinstallHost();

    @DefaultStringValue("Upgrade")
    String upgradeOVirtNode();

    @DefaultStringValue("Configure Local Storage")
    String configureLocalStorageHost();

    @DefaultStringValue("Restart")
    String restartHost();

    @DefaultStringValue("Start")
    String startHost();

    @DefaultStringValue("Stop")
    String stopHost();

    @DefaultStringValue("Power Management")
    String pmHost();

    @DefaultStringValue("Assign Tags")
    String assignTagsHost();

    @DefaultStringValue("Show Report")
    String showReportHost();

    @DefaultStringValue("Refresh Capabilities")
    String refreshHostCapabilities();

    @DefaultStringValue("Host HA Maintenance")
    String hostHaMaintenance();

    // host- general
    @DefaultStringValue("OS Version")
    String osVersionHostGeneral();

    @DefaultStringValue("Manufacturer")
    String hardwareManufacturerGeneral();

    @DefaultStringValue("Product Name")
    String hardwareProductNameGeneral();

    @DefaultStringValue("Version")
    String hardwareVersionGeneral();

    @DefaultStringValue("Serial Number")
    String hardwareSerialNumberGeneral();

    @DefaultStringValue("UUID")
    String hardwareUUIDGeneral();

    @DefaultStringValue("Family")
    String hardwareFamilyGeneral();

    @DefaultStringValue("HBA Inventory")
    String hardwareHBAInventory();

    @DefaultStringValue("Model name")
    String hbaModelName();

    @DefaultStringValue("Device type")
    String hbaDeviceType();

    @DefaultStringValue("WWNN")
    String hbaWWNN();

    @DefaultStringValue("WWPNs")
    String hbaWWPNs();

    @DefaultStringValue("Kernel Version")
    String kernelVersionHostGeneral();

    @DefaultStringValue("KVM Version")
    String kvmVersionHostGeneral();

    @DefaultStringValue("LIBVIRT Version")
    String libvirtVersionHostGeneral();

    @DefaultStringValue("VDSM Version")
    String vdsmVersionHostGeneral();

    @DefaultStringValue("SPICE Version")
    String spiceVersionHostGeneral();

    @DefaultStringValue("GlusterFS Version")
    String glusterVersionHostGeneral();

    @DefaultStringValue("iSCSI Initiator Name")
    String isciInitNameHostGeneral();

    @DefaultStringValue("Active VMs")
    String activeVmsHostGeneral();

    @DefaultStringValue("Logical CPU Cores")
    String logicalCores();

    @DefaultStringValue("Online Logical CPU Cores")
    String onlineCores();

    @DefaultStringValue("CPU Model")
    String cpuModelHostGeneral();

    @DefaultStringValue("CPU Type")
    String cpuTypeHostGeneral();

    @DefaultStringValue("CPU Sockets")
    String numOfSocketsHostGeneral();

    @DefaultStringValue("CPU Cores per Socket")
    String numOfCoresPerSocketHostGeneral();

    @DefaultStringValue("CPU Threads per Core")
    String numOfThreadsPerCoreHostGeneral();

    @DefaultStringValue("Physical Memory")
    String physMemHostGeneral();

    @DefaultStringValue("Capacity")
    String volumeCapacityStatistics();

    @DefaultStringValue("Swap Size")
    String swapSizeHostGeneral();

    @DefaultStringValue("Max free Memory for scheduling new VMs")
    String maxSchedulingMemory();

    @DefaultStringValue("Memory Page Sharing")
    String memPageSharingHostGeneral();

    @DefaultStringValue("Automatic Large Pages")
    String autoLargePagesHostGeneral();

    @DefaultStringValue("Shared Memory")
    String sharedMemHostGeneral();

    @DefaultStringValue("Hosted Engine HA")
    String hostedEngineHaHostGeneral();

    @DefaultStringValue("Action Items")
    String actionItemsHostGeneral();

    @DefaultStringValue("Boot Time")
    String bootTimeHostGeneral();

    @DefaultStringValue("Kdump Status")
    String kdumpStatus();

    @DefaultStringValue("SELinux mode")
    String selinuxModeGeneral();

    @DefaultStringValue("Live Snapshot Support")
    String liveSnapshotSupportHostGeneral();

    // Storage
    @DefaultStringValue("Domain Name")
    String domainNameStorage();

    @DefaultStringValue("Description")
    String domainDescriptionStorage();

    @DefaultStringValue("Domain Type")
    String domainTypeStorage();

    @DefaultStringValue("Storage Type")
    String storageTypeStorage();

    @DefaultStringValue("Format")
    String formatStorage();

    @DefaultStringValue("Cross Data Center Status")
    String crossDcStatusStorage();

    @DefaultStringValue("Free Space")
    String freeSpaceStorage();

    @DefaultStringValue("New Domain")
    String newDomainStorage();

    @DefaultStringValue("Import Domain")
    String importDomainStorage();

    @DefaultStringValue("Edit")
    String editStorage();

    @DefaultStringValue("Remove")
    String removeStorage();

    @DefaultStringValue("Destroy")
    String destroyStorage();

    @DefaultStringValue("Show Report")
    String showReportStorage();

    @DefaultStringValue("Status")
    String statusStorage();

    @DefaultStringValue("Used Space")
    String usedSpaceStorage();

    @DefaultStringValue("Total Space")
    String totalSpaceStorage();

    @DefaultStringValue("Attach Data")
    String attachDataStorage();

    @DefaultStringValue("Attach ISO")
    String attachIsoStorage();

    @DefaultStringValue("Attach Export")
    String attachExportStorage();

    @DefaultStringValue("Detach")
    String detachStorage();

    @DefaultStringValue("Activate")
    String activateStorage();

    @DefaultStringValue("Maintenance")
    String maintenanceStorage();

    @DefaultStringValue("Name")
    String nameStorage();

    @DefaultStringValue("Type")
    String typeStorage();

    // Storage General
    @DefaultStringValue("Size")
    String sizeStorageGeneral();

    @DefaultStringValue("Available")
    String availableStorageGeneral();

    @DefaultStringValue("Used")
    String usedStorageGeneral();

    @DefaultStringValue("Allocated")
    String allocatedStorageGeneral();

    @DefaultStringValue("Over Allocation Ratio")
    String overAllocRatioStorageGeneral();

    @DefaultStringValue("Path")
    String pathStorageGeneral();

    @DefaultStringValue("VFS Type")
    String vfsTypeStorageGeneral();

    @DefaultStringValue("Mount Options")
    String mountOptionsGeneral();

    @DefaultStringValue("NFS Version")
    String nfsVersionGeneral();

    @DefaultStringValue("Retransmissions (#)")
    String nfsRetransmissionsGeneral();

    @DefaultStringValue("Timeout (deciseconds)")
    String nfsTimeoutGeneral();

    // VM
    @DefaultStringValue("Cluster")
    String clusterVm();

    @DefaultStringValue("Data Center")
    String dcVm();

    @DefaultStringValue("Host")
    String hostVm();

    @DefaultStringValue("IP Address")
    String ipVm();

    @DefaultStringValue("Memory")
    String memoryVm();

    @DefaultStringValue("CPU")
    String cpuVm();

    @DefaultStringValue("CPUs")
    String cpusVm();

    @DefaultStringValue("Architecture")
    String architectureVm();

    @DefaultStringValue("Network")
    String networkVm();

    @DefaultStringValue("Migration")
    String migrationProgress();

    @DefaultStringValue("Display")
    String displayVm();

    @DefaultStringValue("Status")
    String statusVm();

    @DefaultStringValue("Uptime")
    String uptimeVm();

    @DefaultStringValue("Edit")
    String editVm();

    @DefaultStringValue("Remove")
    String removeVm();

    @DefaultStringValue("Run Once")
    String runOnceVm();

    @DefaultStringValue("Run")
    String runVm();

    @DefaultStringValue("Suspend")
    String suspendVm();

    @DefaultStringValue("Console")
    String consoleVm();

    @DefaultStringValue("Migrate")
    String migrateVm();

    @DefaultStringValue("Cancel Migration")
    String cancelMigrationVm();

    @DefaultStringValue("Make Template")
    String makeTemplateVm();

    @DefaultStringValue("Export")
    String exportVm();

    @DefaultStringValue("Create Snapshot")
    String createSnapshotVM();

    @DefaultStringValue("Move")
    String moveVm();

    @DefaultStringValue("Change CD")
    String changeCdVm();

    @DefaultStringValue("Assign Tags")
    String assignTagsVm();

    @DefaultStringValue("Enable Global HA Maintenance")
    String enableGlobalHaMaintenanceVm();

    @DefaultStringValue("Disable Global HA Maintenance")
    String disableGlobalHaMaintenanceVm();

    @DefaultStringValue("Show Report")
    String showReportVm();

    @DefaultStringValue("Guide Me")
    String guideMeVm();

    @DefaultStringValue("Disks")
    String disksVm();

    @DefaultStringValue("Virtual Size")
    String vSizeVm();

    @DefaultStringValue("Actual Size")
    String actualSizeVm();

    @DefaultStringValue("Creation Date")
    String creationDateVm();

    @DefaultStringValue("Export Date")
    String exportDateVm();

    @DefaultStringValue("Detach")
    String detachVm();

    @DefaultStringValue("Import")
    String restoreVm();

    // Pool
    @DefaultStringValue("Name")
    String namePool();

    @DefaultStringValue("Assigned VMs")
    String assignVmsPool();

    @DefaultStringValue("Running VMs")
    String runningVmsPool();

    @DefaultStringValue("Type")
    String typePool();

    @DefaultStringValue("Description")
    String descriptionPool();

    @DefaultStringValue("New")
    String newPool();

    @DefaultStringValue("Edit")
    String editPool();

    @DefaultStringValue("Remove")
    String removePool();

    // Template
    @DefaultStringValue("Name")
    String nameTemplate();

    @DefaultStringValue("Alias")
    String aliasTemplate();

    @DefaultStringValue("Domain")
    String domainTemplate();

    @DefaultStringValue("Creation Date")
    String creationDateTemplate();

    @DefaultStringValue("Export Date")
    String exportDateTemplate();

    @DefaultStringValue("Status")
    String statusTemplate();

    @DefaultStringValue("Cluster")
    String clusterTemplate();

    @DefaultStringValue("Data Center")
    String dcTemplate();

    @DefaultStringValue("Description")
    String descriptionTemplate();

    @DefaultStringValue("Version")
    String versionTemplate();

    @DefaultStringValue("Edit")
    String editTemplate();

    @DefaultStringValue("Remove")
    String removeTemplate();

    @DefaultStringValue("Export")
    String exportTemplate();

    @DefaultStringValue("Copy")
    String copyTemplate();

    @DefaultStringValue("New VM")
    String createVmFromTemplate();

    @DefaultStringValue("Disks")
    String disksTemplate();

    @DefaultStringValue("Virtual Size")
    String provisionedSizeTemplate();

    @DefaultStringValue("Actual Size")
    String actualSizeTemplate();

    @DefaultStringValue("Origin")
    String originTemplate();

    @DefaultStringValue("Memory")
    String memoryTemplate();

    @DefaultStringValue("CPUs")
    String cpusTemplate();

    @DefaultStringValue("Architecture")
    String architectureTemplate();

    @DefaultStringValue("Import")
    String restoreTemplate();

    // User
    @DefaultStringValue("First Name")
    String firstnameUser();

    @DefaultStringValue("Last Name")
    String lastNameUser();

    @DefaultStringValue("User Name")
    String userNameUser();

    @DefaultStringValue("User Id")
    String userId();

    @DefaultStringValue("Session DB Id")
    String sessionDbId();

    @DefaultStringValue("Group")
    String groupUser();

    @DefaultStringValue("E-mail")
    String emailUser();

    @DefaultStringValue("Add")
    String addUser();

    @DefaultStringValue("Remove")
    String removeUser();

    @DefaultStringValue("Assign Tags")
    String assignTagsUser();

    @DefaultStringValue("User")
    String userUser();

    @DefaultStringValue("Inherited From")
    String inheritedFromUser();

    @DefaultStringValue("E-mail")
    String emailUserGeneral();

    // Quota
    @DefaultStringValue("Name")
    String nameQuota();

    @DefaultStringValue("Description")
    String descriptionQuota();

    @DefaultStringValue("Data Center")
    String dcQuota();

    @DefaultStringValue("Free Memory")
    String freeMemory();

    @DefaultStringValue("Free vCPU")
    String freeVcpu();

    @DefaultStringValue("Free Storage")
    String freeStorage();

    @DefaultStringValue("Memory Consumption")
    String usedMemoryQuota();

    @DefaultStringValue("VCPU Consumption")
    String runningCpuQuota();

    @DefaultStringValue("Storage Consumption")
    String usedStorageQuota();

    @DefaultStringValue("Unlimited")
    String unlimited();

    @DefaultStringValue("Exceeded")
    String exceeded();

    @DefaultStringValue("Add")
    String addQuota();

    @DefaultStringValue("Edit")
    String editQuota();

    @DefaultStringValue("Copy")
    String copyQuota();

    @DefaultStringValue("Remove")
    String removeQuota();

    @DefaultStringValue("Storage Name")
    String storageNameQuota();

    @DefaultStringValue("Cluster Name")
    String clusterNameQuota();

    @DefaultStringValue("Memory")
    String quotaOfMemQuota();

    @DefaultStringValue("vCPU")
    String quotaOfVcpuQuota();

    @DefaultStringValue("vCPUs")
    String vcpus();

    @DefaultStringValue("Quota")
    String quota();

    @DefaultStringValue("Edit")
    String editCellQuota();

    // Network
    @DefaultStringValue("Attached")
    String attachedNetwork();

    @DefaultStringValue("Name")
    String nameNetwork();

    @DefaultStringValue("Id")
    String idNetwork();

    @DefaultStringValue("Data Center")
    String dcNetwork();

    @DefaultStringValue("VLAN tag")
    String vlanNetwork();

    @DefaultStringValue("Provider")
    String providerNetwork();

    @DefaultStringValue("MTU")
    String mtuNetwork();

    @DefaultStringValue("default")
    String mtuDefault();

    @DefaultStringValue("Required")
    String requiredNetwork();

    @DefaultStringValue("Non Required")
    String nonRequiredNetwork();

    @DefaultStringValue("VM Network")
    String vmNetwork();

    @DefaultStringValue("true")
    String trueVmNetwork();

    @DefaultStringValue("false")
    String falseVmNetwork();

    @DefaultStringValue("Status")
    String statusNetwork();

    @DefaultStringValue("Display Network")
    String displayNetwork();

    @DefaultStringValue("Migration Network")
    String migrationNetwork();

    @DefaultStringValue("Gluster Network")
    String glusterNetwork();

    @DefaultStringValue("Role")
    String roleNetwork();

    @DefaultStringValue("Description")
    String descriptionNetwork();

    @DefaultStringValue("Add Network")
    String addNetworkNetwork();

    @DefaultStringValue("Manage Networks")
    String assignDetatchNetworksNework();

    @DefaultStringValue("Manage Network")
    String assignUnassignNetwork();

    @DefaultStringValue("Set as Display")
    String setAsDisplayNetwork();

    @DefaultStringValue("New")
    String newNetwork();

    @DefaultStringValue("Import")
    String importNetwork();

    @DefaultStringValue("Edit")
    String editNetwork();

    @DefaultStringValue("Remove")
    String removeNetwork();

    @DefaultStringValue("none")
    String noneVlan();

    @DefaultStringValue("New")
    String newNetworkProfile();

    @DefaultStringValue("Edit")
    String editNetworkProfile();

    @DefaultStringValue("Remove")
    String removeNetworkProfile();

    @DefaultStringValue("New")
    String newNetworkExternalSubnet();

    @DefaultStringValue("Remove")
    String removeNetworkExternalSubnet();

    // Cluster host
    @DefaultStringValue("Name")
    String nameClusterHost();

    @DefaultStringValue("Hostname/IP")
    String hostIpClusterHost();

    @DefaultStringValue("Status")
    String statusClusterHost();

    @DefaultStringValue("VMs")
    String vmsClusterHost();

    @DefaultStringValue("Load")
    String loadClusterHost();

    @DefaultStringValue("Sync MoM Policy")
    String updateMomPolicyClusterHost();

    // Cluster service
    @DefaultStringValue("Host")
    String hostService();

    @DefaultStringValue("Service")
    String nameService();

    @DefaultStringValue("Status")
    String statusService();

    @DefaultStringValue("Port")
    String portService();

    @DefaultStringValue("Process Id")
    String pidService();

    @DefaultStringValue("Filter")
    String filterService();

    @DefaultStringValue("Show All")
    String showAllService();

    @DefaultStringValue("Display Address Overridden")
    String overriddenConsoleAddress();

    // Cluster Gluster Hooks
    @DefaultStringValue("Name")
    String nameHook();

    @DefaultStringValue("Volume Event")
    String glusterVolumeEventHook();

    @DefaultStringValue("Stage")
    String stageHook();

    @DefaultStringValue("Status")
    String statusHook();

    @DefaultStringValue("Content Type")
    String contentTypeHook();

    @DefaultStringValue("Enable")
    String enableHook();

    @DefaultStringValue("Disable")
    String disableHook();

    @DefaultStringValue("View Content")
    String viewHookContent();

    @DefaultStringValue("Resolve Conflicts")
    String resolveConflictsGlusterHook();

    @DefaultStringValue("Sync")
    String syncWithServersGlusterHook();

    @DefaultStringValue("Conflicts Reasons")
    String conflictReasonsGlusterHook();

    @DefaultStringValue("Hook content is different in some servers compared to Master copy(Engine)")
    String conflictReasonContentGlusterHook();

    @DefaultStringValue("Hook status is inconsistent (Enabled/Disabled) across servers")
    String conflictReasonStatusGlusterHook();

    @DefaultStringValue("Hook is missing in some of the servers")
    String conflictReasonMissingGlusterHook();

    @DefaultStringValue("Select the source to view the content")
    String contentSourcesGlusterHook();

    @DefaultStringValue("Source")
    String sourceGlusterHook();

    @DefaultStringValue("Content")
    String contentGlusterHook();

    @DefaultStringValue("MD5 Checksum")
    String checksumGlusterHook();

    @DefaultStringValue("Status")
    String statusGlusterHook();

    @DefaultStringValue("Resolve Actions")
    String resolveActionsGlusterHook();

    @DefaultStringValue("Resolve Content Conflict")
    String resolveContentConflictGlusterHook();

    @DefaultStringValue("Use content from")
    String useContentSourceGlusterHook();

    @DefaultStringValue("NOTE: The hook content will be overwritten in all the servers and in Engine")
    String useContentSourceWarningGlusterHook();

    @DefaultStringValue("Resolve Status Conflict")
    String resolveStatusConflictGlusterHook();

    @DefaultStringValue("Enable")
    String statusEnableGlusterHook();

    @DefaultStringValue("Disable")
    String statusDisableGlusterHook();

    @DefaultStringValue("Resolve Missing Hook Conflict")
    String resolveMissingConflictGlusterHook();

    @DefaultStringValue("Copy the hook to all the servers")
    String resolveMissingConflictCopyGlusterHook();

    @DefaultStringValue("Remove the missing hook")
    String resolveMissingConflictRemoveGlusterHook();

    @DefaultStringValue("NOTE: Master copy of the hook will be removed from Engine, this cannot be restored later")
    String removeMissingWarningGlusterHook();

    // Interface
    @DefaultStringValue("Empty")
    String emptyInterface();

    @DefaultStringValue("Address")
    String addressInterface();

    @DefaultStringValue("Bond")
    String bondInterface();

    @DefaultStringValue("VLAN")
    String vlanInterface();

    @DefaultStringValue("Add / Edit")
    String addEditInterface();

    @DefaultStringValue("Edit Management Network")
    String editManageNetInterface();

    @DefaultStringValue("Detach")
    String detachInterface();

    @DefaultStringValue("Save Network Configuration")
    String saveNetConfigInterface();

    @DefaultStringValue("Setup Host Networks")
    String setupHostNetworksInterface();

    @DefaultStringValue("Date Created")
    String dateCreatedInterface();

    // Hook
    @DefaultStringValue("Event Name")
    String eventNameHook();

    @DefaultStringValue("Script Name")
    String scriptNameHook();

    @DefaultStringValue("Property Name")
    String propertyNameHook();

    @DefaultStringValue("Property Value")
    String propertyValueHook();

    // Host Gluster Swift
    @DefaultStringValue("Service")
    String serviceGlusterSwift();

    @DefaultStringValue("Status")
    String statusGlusterSwift();

    @DefaultStringValue("Start Swift")
    String startGlusterSwiftInHost();

    @DefaultStringValue("Stop Swift")
    String stopGlusterSwiftInHost();

    @DefaultStringValue("Restart Swift")
    String restartGlusterSwiftInHost();

    // Host Bricks
    @DefaultStringValue("Volume")
    String volumeName();

    // Disk
    @DefaultStringValue("Name")
    String deviceName();

    @DefaultStringValue("Type")
    String deviceType();

    @DefaultStringValue("File System Type")
    String fileSystemType();

    @DefaultStringValue("Size")
    String size();

    @DefaultStringValue("Storage Devices")
    String storageDevices();

    @DefaultStringValue("Brick Directory")
    String brickDirectory();

    // Group
    @DefaultStringValue("Group Name")
    String groupNameGroup();

    @DefaultStringValue("Namespace")
    String namespaceGroup();

    // Event notifier
    @DefaultStringValue("Event Name")
    String eventNameEventNotifier();

    @DefaultStringValue("Manage Events")
    String manageEventsEventNotifier();

    // Permissions
    @DefaultStringValue("Inherited From")
    String inheretedFromPermission();

    // Quota popup
    @DefaultStringValue("Unlimited")
    String ultQuotaPopup();

    @DefaultStringValue("limit to")
    String useQuotaPopup();

    @DefaultStringValue("Memory:")
    String memQuotaPopup();

    @DefaultStringValue("CPU:")
    String cpuQuotaPopup();

    @DefaultStringValue("Storage Quota:")
    String storageQuotaQuotaPopup();

    @DefaultStringValue("Name")
    String nameQuotaPopup();

    @DefaultStringValue("Description")
    String descriptionQuotaPopup();

    @DefaultStringValue("Data Center")
    String dataCenterQuotaPopup();

    @DefaultStringValue("Copy consumers and permissions")
    String copyQuotaPermissionsQuotaPopup();

    @DefaultStringValue("Memory & CPU")
    String memAndCpuQuotaPopup();

    @DefaultStringValue("Storage")
    String storageQuotaPopup();

    @DefaultStringValue("All Clusters")
    String ultQuotaForAllClustersQuotaPopup();

    @DefaultStringValue("Specific Clusters")
    String useQuotaSpecificClusterQuotaPopup();

    @DefaultStringValue("All Storage Domains")
    String utlQuotaAllStoragesQuotaPopup();

    @DefaultStringValue("Specific Storage Domains")
    String usedQuotaSpecStoragesQuotaPopup();

    // Event
    @DefaultStringValue("Event ID")
    String eventIdEvent();

    @DefaultStringValue("User")
    String userEvent();

    @DefaultStringValue("Host")
    String hostEvent();

    @DefaultStringValue("Virtual Machine")
    String vmEvent();

    @DefaultStringValue("Template")
    String templateEvent();

    @DefaultStringValue("Data Center")
    String dcEvent();

    @DefaultStringValue("Storage")
    String storageEvent();

    @DefaultStringValue("Cluster")
    String clusterEvent();

    @DefaultStringValue("Gluster Volume")
    String volumeEvent();

    @DefaultStringValue("Correlation Id")
    String eventCorrelationId();

    @DefaultStringValue("Origin")
    String eventOrigin();

    @DefaultStringValue("Custom Event Id")
    String eventCustomEventId();

    // Host configure local storage
    @DefaultStringValue("Data Center")
    String dcLocalStorage();

    @DefaultStringValue("Cluster")
    String clusterLocalStorage();

    @DefaultStringValue("Storage")
    String storageLocalStorage();

    // Confiramtion popup
    @DefaultStringValue("Confirm Operation")
    String confirmOperation();

    // Disks tree
    @DefaultStringValue("Domain Name")
    String domainNameDisksTree();

    @DefaultStringValue("Domain Type")
    String domainTypeDisksTree();

    @DefaultStringValue("Status")
    String statusDisksTree();

    @DefaultStringValue("Free Space")
    String freeSpaceDisksTree();

    @DefaultStringValue("Used Space")
    String usedSpaceDisksTree();

    @DefaultStringValue("Total Space")
    String totalSpaceDisksTree();

    @DefaultStringValue("Disk")
    String diskDisksTree();

    // Bookmark
    @DefaultStringValue("New")
    String newBookmark();

    @DefaultStringValue("Edit")
    String editBookmark();

    @DefaultStringValue("Remove")
    String removeBookmark();

    // About
    @DefaultStringValue("Copy to Clipboard")
    String copy2ClipAbout();

    @DefaultStringValue("OS Version -")
    String osVerAbout();

    @DefaultStringValue("VDSM Version -")
    String vdsmVerAbout();

    @DefaultStringValue("[No Hosts]")
    String noHostsAbout();

    // Event footer
    @DefaultStringValue("Last Message:")
    String lastMsgEventFooter();

    @DefaultStringValue("Alerts")
    String alertsEventFooter();

    @DefaultStringValue("Tasks")
    String tasksEventFooter();

    @DefaultStringValue("Events")
    String eventsEventFooter();

    @DefaultStringValue("Last Task:")
    String lastTaskEventFooter();

    @DefaultStringValue("Clear All")
    String clearAllDismissedAlerts();

    @DefaultStringValue("Dismiss Alert")
    String dismissAlert();

    // Network popup

    // Header
    @DefaultStringValue("Logged in user")
    String loggedInUser();

    @DefaultStringValue("ENGINE Web Admin Documentation")
    String engineWebAdminDoc();

    // Detach confirmation popup
    @DefaultStringValue("Are you sure you want to Detach the following Network Interface?")
    String areYouSureDetachConfirmPopup();

    @DefaultStringValue("<I>Changes done to the Networking configuration are temporary until explicitly saved.<BR>" +
            "Check the check-box below to make the changes persistent.</I>")
    String changesTempWarningDetachConfirmPopup();

    @DefaultStringValue("Save network configuration")
    String saveNetCongDetachConfirmPopup();

    // Main Section
    @DefaultStringValue("System")
    String systemMainSection();

    @DefaultStringValue("Bookmarks")
    String bookmarksMainSection();

    @DefaultStringValue("Tags")
    String tagsMainSection();

    // Host popup
    @DefaultStringValue("Custom")
    String customHostPopup();

    @DefaultStringValue("Bond Name")
    String bondNameHostPopup();

    @DefaultStringValue("Network")
    String networkHostPopup();

    @DefaultStringValue("Bonding Mode")
    String bondingModeHostPopup();

    @DefaultStringValue("Custom mode")
    String customModeHostPopup();

    @DefaultStringValue("Labels")
    String labelsHostPopup();

    @DefaultStringValue("Boot Protocol")
    String bootProtocolHostPopup();

    @DefaultStringValue("IP")
    String ipHostPopup();

    @DefaultStringValue("Netmask / Routing Prefix")
    String subnetMaskHostPopup();

    @DefaultStringValue("Gateway")
    String gwHostPopup();

    @DefaultStringValue("Custom Properties")
    String customPropertiesHostPopup();

    @DefaultStringValue("Verify connectivity between Host and Engine")
    String checkConHostPopup();

    @DefaultStringValue("Sync network")
    String syncNetwork();

    @DefaultStringValue("<I>Changes done to the Networking configuration are temporary until explicitly saved.<BR>" +
            "Check the check-box below to make the changes persistent.</I>")
    String changesTempHostPopup();

    @DefaultStringValue("Save network configuration")
    String saveNetConfigHostPopup();

    @DefaultStringValue("Name")
    String nameHostPopup();

    @DefaultStringValue("Interface")
    String intefaceHostPopup();

    // Host management confirmation popup
    @DefaultStringValue("Check Connectivity")
    String checkConnectivityManageConfirmPopup();

    @DefaultStringValue("You are about to change Management Network Configuration.")
    String youAreAboutManageConfirmPopup();

    @DefaultStringValue("This might cause the Host to lose connectivity.")
    String thisMightCauseManageConfirmPopup();

    @DefaultStringValue("It is")
    String itIsManageConfirmPopup();

    @DefaultStringValue("Highly recommended")
    String highlyRecommendedManageConfirmPopup();

    @DefaultStringValue("to proceeed with connectivity check.")
    String toProceeedWithConnectivityCheckManageConfirmPopup();

    // Import Cluster Hosts popup
    @DefaultStringValue("Use a common password")
    String hostsPopupUseCommonPassword();

    @DefaultStringValue("Automatically configure firewall for the hosts of this cluster")
    String configureFirewallForAllHostsOfThisCluster();

    @DefaultStringValue("Root Password")
    String hostsPopupRootPassword();

    @DefaultStringValue("Apply")
    String hostsPopupApply();

    @DefaultStringValue("SSH Fingerprint")
    String hostsPopupFingerprint();

    // Tag
    @DefaultStringValue("New")
    String newTag();

    @DefaultStringValue("Edit")
    String editTag();

    @DefaultStringValue("Remove")
    String removeTag();

    // Iso
    @DefaultStringValue("File Name")
    String fileNameIso();

    @DefaultStringValue("Type")
    String typeIso();

    @DefaultStringValue("Import")
    String importImage();

    // Storage tree
    @DefaultStringValue("Name")
    String nameStorageTree();

    @DefaultStringValue("Size")
    String sizeStorageTree();

    @DefaultStringValue("Status")
    String statusStorageTree();

    @DefaultStringValue("Allocation")
    String allocationStorageTree();

    @DefaultStringValue("Interface")
    String interfaceStorageTree();

    @DefaultStringValue("Creation Date")
    String creationDateStorageTree();

    // Import template
    @DefaultStringValue("General")
    String generalImpTempTab();

    @DefaultStringValue("Network Interfaces")
    String networkIntImpTempTab();

    @DefaultStringValue("Disks")
    String disksImpTempTab();

    // Volume Brick
    @DefaultStringValue("Server")
    String serverVolumeBrick();

    @DefaultStringValue("Brick Directory")
    String brickDirectoryVolumeBrick();

    @DefaultStringValue("Free Space (GB)")
    String freeSpaceGBVolumeBrick();

    @DefaultStringValue("Total Space (GB)")
    String totalSpaceGBVolumeBrick();

    @DefaultStringValue("Status")
    String statusVolumeBrick();

    // Network
    @DefaultStringValue("no network assigned")
    String noNetworkAssigned();

    // Item info
    @DefaultStringValue("Not synchronized")
    String networkNotInSync();

    @DefaultStringValue("Name")
    String nameItemInfo();

    @DefaultStringValue("Usage")
    String usageItemInfo();

    @DefaultStringValue("VM")
    String vmItemInfo();

    @DefaultStringValue("Display")
    String displayItemInfo();

    @DefaultStringValue("Migration")
    String migrationItemInfo();

    @DefaultStringValue("Gluster")
    String glusterNwItemInfo();

    @DefaultStringValue("Unmanaged Network")
    String unmanagedNetworkItemInfo();

    @DefaultStringValue("Doesn't exist in the Cluster")
    String unmanagedNetworkDescriptionItemInfo();

    @DefaultStringValue("Management")
    String managementItemInfo();

    @DefaultStringValue("MTU")
    String mtuItemInfo();

    @DefaultStringValue("Boot Protocol")
    String bootProtocolItemInfo();

    @DefaultStringValue("Address")
    String addressItemInfo();

    @DefaultStringValue("Subnet")
    String subnetItemInfo();

    @DefaultStringValue("Gateway")
    String gatewayItemInfo();

    @DefaultStringValue("Bond Options")
    String bondOptionsItemInfo();

    // Volume
    @DefaultStringValue("Data Center")
    String dataCenterVolume();

    @DefaultStringValue("Volume Cluster")
    String volumeClusterVolume();

    @DefaultStringValue("Stripe Count")
    String stripeCountVolume();

    @DefaultStringValue("Transport Type")
    String transportTypeVolume();

    @DefaultStringValue("TCP")
    String tcpVolume();

    @DefaultStringValue("RDMA")
    String rdmaVolume();

    @DefaultStringValue("Add Bricks")
    String addBricksVolume();

    @DefaultStringValue("Type")
    String typeVolume();

    @DefaultStringValue("Bricks")
    String bricksVolume();

    @DefaultStringValue("Access Protocols")
    String accessProtocolsVolume();

    @DefaultStringValue("Gluster")
    String glusterVolume();

    @DefaultStringValue("NFS")
    String nfsVolume();

    @DefaultStringValue("CIFS")
    String cifsVolume();

    @DefaultStringValue("Allow Access From")
    String allowAccessFromVolume();

    @DefaultStringValue("(Comma separated list of IP addresses/hostnames)")
    String allowAccessFromLabelVolume();

    @DefaultStringValue("Optimize for Virt Store")
    String optimizeForVirtStoreVolume();

    @DefaultStringValue("Name")
    String NameVolume();

    @DefaultStringValue("Cluster")
    String clusterVolume();

    @DefaultStringValue("Volume ID")
    String volumeIdVolume();

    @DefaultStringValue("Volume Type")
    String volumeTypeVolume();

    @DefaultStringValue("Bricks")
    String bricksStatusVolume();

    @DefaultStringValue("Number of Bricks")
    String numberOfBricksVolume();

    @DefaultStringValue("Replica Count")
    String replicaCountVolume();

    @DefaultStringValue("Transport Types")
    String transportTypesVolume();

    @DefaultStringValue("Maximum no of snapshots")
    String maxNumberOfSnapshotsVolume();

    @DefaultStringValue("Disperse Count")
    String disperseCount();

    @DefaultStringValue("Redundancy Count")
    String redundancyCount();

    @DefaultStringValue("Activities")
    String activitiesOnVolume();

    @DefaultStringValue("Status")
    String statusVolume();

    @DefaultStringValue("New")
    String newVolume();

    @DefaultStringValue("Remove")
    String removeVolume();

    @DefaultStringValue("Space Used")
    String volumeCapacity();

    @DefaultStringValue("Start")
    String startVolume();

    @DefaultStringValue("Stop")
    String stopVolume();

    @DefaultStringValue("Rebalance")
    String rebalanceVolume();

    @DefaultStringValue("Started At :")
    String rebalanceStartTime();

    @DefaultStringValue("Stopped At :")
    String rebalanceStopTime();

    @DefaultStringValue("Optimize for Virt Store")
    String optimizeForVirtStore();

    // Inteface editor
    @DefaultStringValue("Address:")
    String addressInterfaceEditor();

    @DefaultStringValue("Subnet:")
    String subnetInterfaceEditor();

    @DefaultStringValue("Gateway:")
    String gatewayInterfaceEditor();

    @DefaultStringValue("Protocol:")
    String protocolInterfaceEditor();

    // Disk
    @DefaultStringValue("ID")
    String idDisk();

    @Override
    @DefaultStringValue("Quota")
    String quotaDisk();

    @DefaultStringValue("Volume Format")
    String volumeFormatDisk();

    // Setup network
    @DefaultStringValue("Drag to make changes")
    String dragToMakeChangesSetupNetwork();

    @DefaultStringValue("No Valid Action")
    String noValidActionSetupNetwork();

    @DefaultStringValue("External networks can't be attached statically; they will be attached dynamically as they are needed by VMs.")
    String externalNetworksInfo();

    @DefaultStringValue("Check this checkbox to ensure you won't lose connectivity to the engine.")
    String checkConnectivityInfoPart1();

    @DefaultStringValue("If after changing the networks configuration the connectivity from the Host to the Engine is lost, changes are rolled back .")
    String checkConnectivityInfoPart2();

    @DefaultStringValue("Provider specific search. Check provider documentation for more information. Empty filter returns all hosts.")
    String providerSearchInfo();

    @DefaultStringValue("List of provisioned hosts.")
    String provisionedHostInfo();

    @DefaultStringValue("List of servers that require OS provisioning. After provision the host will be added to the selected cluster.")
    String discoveredHostInfoIcon();

    @DefaultStringValue("Changes done to the Networking configuration are temporary until explicitly saved.")
    String commitChangesInfoPart1();

    @DefaultStringValue("Check the check-box to make the changes persistent")
    String commitChangesInfoPart2();

    @DefaultStringValue("The logical network definition is not synchronized with the network configuration on the host,")
    String syncNetworkInfoPart1();

    @DefaultStringValue("To edit this network you need to synchronize it.")
    String syncNetworkInfoPart2();

    // Volume parameter
    @DefaultStringValue("Option Key")
    String optionKeyVolumeParameter();

    @DefaultStringValue("Description")
    String descriptionVolumeParameter();

    @DefaultStringValue("Option Value")
    String optionValueVolumeParameter();

    @DefaultStringValue("Add")
    String addVolumeParameter();

    @DefaultStringValue("Edit")
    String editVolumeParameter();

    @DefaultStringValue("Reset")
    String resetVolumeParameter();

    @DefaultStringValue("Reset All")
    String resetAllVolumeParameter();

    @DefaultStringValue("Interfaces")
    String interfaces();

    @DefaultStringValue("Assigned Logical Networks")
    String assignedLogicalNetworks();

    @DefaultStringValue("Unassigned Logical Networks")
    String unassignedLogicalNetworks();

    @DefaultStringValue("External Logical Networks")
    String externalLogicalNetworks();

    // Brick
    @DefaultStringValue("Status")
    String statusBrick();

    @DefaultStringValue("Add")
    String addBricksBrick();

    @DefaultStringValue("Remove")
    String removeBricksBrick();

    @DefaultStringValue("Stop")
    String removeBricksStop();

    @DefaultStringValue("Commit")
    String removeBricksCommit();

    @DefaultStringValue("Status")
    String removeBricksStatus();

    @DefaultStringValue("Retain")
    String retainBricks();

    @DefaultStringValue("Replace Brick")
    String replaceBrickBrick();

    @DefaultStringValue("Advanced Details")
    String advancedDetailsBrick();

    @DefaultStringValue("Host")
    String serverBricks();

    @DefaultStringValue("Brick Directory")
    String brickDirectoryBricks();

    @DefaultStringValue("Show available bricks from host")
    String addBricksShowBricksFromHost();

    @DefaultStringValue("Bricks")
    String bricksHeaderLabel();

    @DefaultStringValue("Add")
    String addBricksButtonLabel();

    @DefaultStringValue("Remove")
    String removeBricksButtonLabel();

    @DefaultStringValue("Clear")
    String clearBricksButtonLabel();

    @DefaultStringValue("Remove All")
    String removeAllBricksButtonLabel();

    @DefaultStringValue("Move Up")
    String moveBricksUpButtonLabel();

    @DefaultStringValue("Move Down")
    String moveBricksDownButtonLabel();

    @DefaultStringValue("NOTE: Replicate groups will be created based on the order of the bricks.")
    String distributedReplicateVolumeBrickInfoLabel();

    @DefaultStringValue("NOTE: Stripe groups will be created based on the order of the bricks.")
    String distributedStripeVolumeBrickInfoLabel();

    @DefaultStringValue("Allow bricks in root partition and re-use the bricks by clearing xattrs")
    String allowBricksInRootPartition();

    @DefaultStringValue("WARNING: This might cause the root partition to be filled up and non-operational")
    String allowBricksInRootPartitionWarning();

    @DefaultStringValue("Migrate Data from the bricks")
    String removeBricksMigrateData();

    @DefaultStringValue("Ensure safe data migration before removing the bricks")
    String removeBricksMigrateDataInfo();

    @DefaultStringValue("Please note that data will be lost if you choose not to migrate")
    String removeBricksWarning();

    // Volume Brick Details
    @DefaultStringValue("General")
    String generalBrickAdvancedPopupLabel();

    @DefaultStringValue("Brick")
    String brickAdvancedLabel();

    @DefaultStringValue("Status")
    String statusBrickAdvancedLabel();

    @DefaultStringValue("Port")
    String portBrickAdvancedLabel();

    @DefaultStringValue("Process ID")
    String pidBrickAdvancedLabel();

    @DefaultStringValue("Total Size (MB)")
    String totalSizeBrickAdvancedLabel();

    @DefaultStringValue("Free Size (MB)")
    String freeSizeBrickAdvancedLabel();

    @DefaultStringValue("Device")
    String deviceBrickAdvancedLabel();

    @DefaultStringValue("Block Size (Bytes)")
    String blockSizeBrickAdvancedLabel();

    @DefaultStringValue("Mount Options")
    String mountOptionsBrickAdvancedLabel();

    @DefaultStringValue("File System")
    String fileSystemBrickAdvancedLabel();

    @DefaultStringValue("Clients")
    String clientsBrickAdvancedPopupLabel();

    @DefaultStringValue("Client")
    String clientBrickAdvancedLabel();

    @DefaultStringValue("Port")
    String clientPortBrickAdvancedLabel();

    @DefaultStringValue("Bytes Read")
    String bytesReadBrickAdvancedLabel();

    @DefaultStringValue("Bytes Written")
    String bytesWrittenBrickAdvancedLabel();

    @DefaultStringValue("Memory Statistics")
    String memoryStatsBrickAdvancedPopupLabel();

    @DefaultStringValue("Total allocated - Non-mmapped (bytes)")
    String totalAllocatedBrickAdvancedLabel();

    @DefaultStringValue("No. of ordinary free blocks")
    String freeBlocksBrickAdvancedLabel();

    @DefaultStringValue("No. of free fastbin blocks")
    String freeFastbinBlocksBrickAdvancedLabel();

    @DefaultStringValue("No. of mmapped blocks allocated")
    String mmappedBlocksBrickAdvancedLabel();

    @DefaultStringValue("Space allocated in mmapped block (bytes)")
    String allocatedInMmappedBlocksBrickAdvancedLabel();

    @DefaultStringValue("Maximum total allocated space (bytes)")
    String maxTotalAllocatedSpaceBrickAdvancedLabel();

    @DefaultStringValue("Space in free fastbin blocks (bytes)")
    String spaceInFreedFasbinBlocksBrickAdvancedLabel();

    @DefaultStringValue("Total allocated space (bytes)")
    String totalAllocatedSpaceBrickAdvancedLabel();

    @DefaultStringValue("Total free space (bytes)")
    String totalFreeSpaceBrickAdvancedLabel();

    @DefaultStringValue("Releasable free space (bytes)")
    String releasableFreeSpaceBrickAdvancedLabel();

    @DefaultStringValue("Memory Pools")
    String memoryPoolsBrickAdvancedPopupLabel();

    @DefaultStringValue("Name")
    String nameBrickAdvancedLabel();

    @DefaultStringValue("Hot Count")
    String hotCountBrickAdvancedLabel();

    @DefaultStringValue("Cold Count")
    String coldCountBrickAdvancedLabel();

    @DefaultStringValue("Padded Size")
    String paddedSizeBrickAdvancedLabel();

    @DefaultStringValue("Allocated Count")
    String allocatedCountBrickAdvancedLabel();

    @DefaultStringValue("Max Allocated")
    String maxAllocatedBrickAdvancedLabel();

    @DefaultStringValue("Pool Misses")
    String poolMissesBrickAdvancedLabel();

    @DefaultStringValue("Max Std Allocated")
    String maxStdAllocatedBrickAdvancedLabel();

    @DefaultStringValue("Cluster Threshold")
    String quotaClusterThreshold();

    @DefaultStringValue("Cluster Grace")
    String quotaClusterGrace();

    @DefaultStringValue("Storage Threshold")
    String quotaStorageThreshold();

    @DefaultStringValue("Storage Grace")
    String quotaStorageGrace();

    @DefaultStringValue("Clone All VMs")
    String importVm_cloneAllVMs();

    @DefaultStringValue("Clone All Templates")
    String importTemplate_cloneAllTemplates();

    @DefaultStringValue("Clone Only Duplicated Templates")
    String importTemplate_cloneOnlyDuplicateTemplates();

    @DefaultStringValue("New Name:")
    String import_newName();

    @DefaultStringValue("VM in System")
    String vmInSetup();

    @DefaultStringValue("Template in System")
    String templateInSetup();

    @DefaultStringValue("* Note that cloned vm will be 'Collapsed Snapshot'")
    String noteClone_CollapsedSnapshotMsg();

    @DefaultStringValue("This operation might be unrecoverable and destructive!")
    String storageForceCreatePopupWarningLabel();

    @DefaultStringValue("Cluster Quota")
    String quotaCluster();

    @DefaultStringValue("Storage Quota")
    String quotaStorage();

    @DefaultStringValue("Extended")
    String extendedPanelLabel();

    @DefaultStringValue("select:")
    String cloneSelect();

    @DefaultStringValue("Apply to all")
    String cloneApplyToAll();

    @DefaultStringValue("Don't import")
    String cloneDontImport();

    @DefaultStringValue("Import as cloned (Changing name, MAC addresses and cloning all disks removing all snapshots)")
    String cloneImportVmDetails();

    @DefaultStringValue("Clone")
    String cloneImportTemplate();

    @DefaultStringValue("Suffix to add to the cloned VMs:")
    String cloneImportSuffixVm();

    @DefaultStringValue("Suffix to add to the cloned Templates:")
    String cloneImportSuffixTemplate();

    @DefaultStringValue("Please select a name for the cloned Template(s)")
    String sameTemplateNameExists();

    @DefaultStringValue("These calculations represents the max growth potential and may differ from the actual consumption. Please refer documentation for further explanations.")
    String quotaCalculationsMessage();

    // Network cluster
    @DefaultStringValue("Network Status")
    String networkStatus();

    @DefaultStringValue("Host IP/Name")
    String detachGlusterHostsHostAddress();

    @DefaultStringValue("Force Detach")
    String detachGlusterHostsForcefully();

    @DefaultStringValue("Allow all users to use this Network")
    String networkPublicUseLabel();

    @DefaultStringValue("Allow all users to use this Profile")
    String profilePublicUseLabel();

    @DefaultStringValue("Public")
    String profilePublicUseInstanceTypeLabel();

    @DefaultStringValue("QoS")
    String profileQoSInstanceTypeLabel();

    @DefaultStringValue("Some hosts in this cluster have the console address overridden and some not. For details please see the Hosts subtab")
    String consolePartiallyOverridden();

    @DefaultStringValue("Use Threads as CPU")
    String cpuThreadsCluster();

    @DefaultStringValue("Max Memory Over Commitment")
    String memoryOptimizationCluster();

    @DefaultStringValue("Resilience Policy")
    String resiliencePolicyCluster();

    @DefaultStringValue("Number of VMs")
    String numberOfVmsCluster();

    @DefaultStringValue("Emulated Machine")
    String emulatedMachine();

    @Override
    @DefaultStringValue("High Priority Only")
    String highPriorityOnly();

    @DefaultStringValue("Compatibility Version")
    String compatibilityVersionCluster();

    @DefaultStringValue("Cluster Node Type")
    String clusterType();

    @DefaultStringValue("used")
    String used();

    @DefaultStringValue("total")
    String total();

    @DefaultStringValue("free")
    String free();

    @Override
    @DefaultStringValue("Unknown")
    String unknown();

    // Provider
    @DefaultStringValue("Agent Configuration")
    String providerPopupAgentConfigurationTabLabel();

    @DefaultStringValue("General")
    String providerPopupGeneralTabLabel();

    @DefaultStringValue("Name")
    String nameProvider();

    @DefaultStringValue("Type")
    String typeProvider();

    @DefaultStringValue("Description")
    String descriptionProvider();

    @DefaultStringValue("Provider URL")
    String urlProvider();

    @DefaultStringValue("Test")
    String testProvider();

    @DefaultStringValue("Test succeeded, managed to access provider.")
    String testSuccessMessage();

    @DefaultStringValue("Requires Authentication")
    String requiresAuthenticationProvider();

    @DefaultStringValue("Username")
    String usernameProvider();

    @DefaultStringValue("Authentication URL")
    String authUrlProvider();

    @DefaultStringValue("Password")
    String passwordProvider();

    @DefaultStringValue("Tenant Name")
    String tenantName();

    @DefaultStringValue("Networking Plugin")
    String pluginType();

    @DefaultStringValue("Add")
    String addProvider();

    @DefaultStringValue("Edit")
    String editProvider();

    @DefaultStringValue("Remove")
    String removeProvider();

    @DefaultStringValue("General")
    String providerGeneralSubTabLabel();

    @DefaultStringValue("Networks")
    String providerNetworksSubTabLabel();

    @DefaultStringValue("Messaging Broker Configuration")
    String messagingConfiguration();

    @DefaultStringValue("Broker Type")
    String messagingBrokerType();

    @DefaultStringValue("Host")
    String messagingServer();

    @DefaultStringValue("Port")
    String messagingServerPort();

    @DefaultStringValue("Username")
    String messagingServerUsername();

    @DefaultStringValue("Password")
    String messagingServerPassword();

    // Provider Network
    @DefaultStringValue("External ID")
    String externalIdProviderNetwork();

    @DefaultStringValue("Data Center")
    String dataCenterProviderNetwork();

    @DefaultStringValue("Name")
    String nameNetworkHeader();

    @DefaultStringValue("Provider Network ID")
    String idNetworkHeader();

    @DefaultStringValue("Data Center")
    String dcNetworkHeader();

    @DefaultStringValue("Allow All")
    String publicNetwork();

    @DefaultStringValue("Network Provider")
    String networkProvider();

    @DefaultStringValue("Virtual Machines on Source")
    String externalVms();

    @DefaultStringValue("Virtual Machines to Import")
    String importedVms();

    @DefaultStringValue("Provider Networks")
    String providerNetworks();

    @DefaultStringValue("Networks to Import")
    String importedNetworks();

    @DefaultStringValue("Enter host fingerprint or <a>fetch</a> manually from host")
    String fetchingHostFingerprint();

    @DefaultStringValue("Name")
    String networkQoSName();

    @DefaultStringValue("In Average")
    String networkQoSInboundAverage();

    @DefaultStringValue("In Peak")
    String networkQoSInboundPeak();

    @DefaultStringValue("In Burst")
    String networkQoSInboundBurst();

    @DefaultStringValue("Out Average")
    String networkQoSOutboundAverage();

    @DefaultStringValue("Out Peak")
    String networkQoSOutboundPeak();

    @DefaultStringValue("Out Burst")
    String networkQoSOutboundBurst();

    @DefaultStringValue("New")
    String newNetworkQoS();

    @DefaultStringValue("Edit")
    String editNetworkQoS();

    @DefaultStringValue("Remove")
    String removeNetworkQoS();

    @DefaultStringValue("Data Center")
    String dataCenterNetworkQoSPopup();

    @DefaultStringValue("Average")
    String averageNetworkQoSPopup();

    @DefaultStringValue("Peak")
    String peakNetworkQoSPopup();

    @DefaultStringValue("Burst")
    String burstNetworkQoSPopup();

    @DefaultStringValue(" (Megabits per second)")
    String inMegabitsNetworkQoSPopup();

    @DefaultStringValue(" (Megabytes)")
    String inMegabytesNetworkQoSPopup();

    @DefaultStringValue("Inbound")
    String inboundLabelQoSPopup();

    @DefaultStringValue("Outbound")
    String outboundLabelQoSPopup();

    @DefaultStringValue("Mbps")
    String mbpsLabelQoSPopup();

    @DefaultStringValue("MiB")
    String mbLabelQoSPopup();

    @DefaultStringValue("Override QoS")
    String qosOverrideLabel();

    @DefaultStringValue("Outbound")
    String hostNetworkQosOutLabel();

    @DefaultStringValue("Weighted Share")
    String hostNetworkQosPopupOutAverageLinkshare();

    @DefaultStringValue("Rate Limit [Mbps]")
    String hostNetworkQosPopupOutAverageUpperlimit();

    @DefaultStringValue("Committed Rate [Mbps]")
    String hostNetworkQosPopupOutAverageRealtime();

    @DefaultStringValue("Out Share")
    String hostNetworkQosTabOutAverageLinkshare();

    @DefaultStringValue("Out Limit")
    String hostNetworkQosTabOutAverageUpperlimit();

    @DefaultStringValue("Out Committed")
    String hostNetworkQosTabOutAverageRealtime();

    // MAC pool widget
    @DefaultStringValue("From")
    String macPoolWidgetLeftBound();

    @DefaultStringValue("To")
    String macPoolWidgetRightBound();

    @DefaultStringValue("Allow Duplicates")
    String macPoolWidgetAllowDuplicates();

    @DefaultStringValue("MAC Address Ranges")
    String macPoolWidgetRangesLabel();

    @DefaultStringValue("Scheduling Policies")
    String configureClusterPolicyTabLabel();

    @DefaultStringValue("Name")
    String clusterPolicyNameLabel();

    @DefaultStringValue("Description")
    String clusterPolicyDescriptionLabel();

    @DefaultStringValue("New")
    String newClusterPolicy();

    @DefaultStringValue("Edit")
    String editClusterPolicy();

    @DefaultStringValue("Copy")
    String copyClusterPolicy();

    @DefaultStringValue("Remove")
    String removeClusterPolicy();

    @DefaultStringValue("Filter Modules")
    String clusterPolicyFilterLabel();

    @DefaultStringValue("Weights Modules")
    String clusterPolicyFunctionLabel();

    @DefaultStringValue("Load Balancer")
    String clusterPolicyLoadBalancerLabel();

    @DefaultStringValue("Clusters on this policy")
    String clusterPolicyAttachedCluster();

    @DefaultStringValue("Properties")
    String clusterPolicyPropertiesLabel();

    @DefaultStringValue("Select Policy")
    String clusterPolicySelectPolicyLabel();

    @DefaultStringValue("Enabled Filters")
    String enabledFilters();

    @DefaultStringValue("Disabled Filters")
    String disabledFilters();

    @DefaultStringValue("Enabled Weights & Factors")
    String enabledFunctions();

    @DefaultStringValue("Disabled Weights")
    String disabledFunctions();

    @DefaultStringValue("Drag or use context menu to make changes")
    String clusterPolicyExplanationMessage();

    @DefaultStringValue("First")
    String firstFilter();

    @DefaultStringValue("Last")
    String lastFilter();

    @DefaultStringValue("No Position")
    String noPositionFilter();

    @DefaultStringValue("Feedback")
    String feedbackMessage();

    @DefaultStringValue("Clicking this link will open the feedback in a new browser window/tab."
            + " If nothing appears to happen make sure you have your e-mail client configured.")
    String feedbackTooltip();

    @DefaultStringValue("Remove Filter")
    String removeFilter();

    @DefaultStringValue("Add Filter")
    String addFilter();

    @DefaultStringValue("Position")
    String position();

    @DefaultStringValue("Action Items")
    String actionItems();

    @DefaultStringValue("For allowing PK authentication, copy the following PK to host under /root/.ssh/authorized_keys")
    String publicKeyUsage();

    @Override
    @DefaultStringValue("Network")
    String networkProfilePopup();

    @DefaultStringValue("Name")
    String nameProfilePopup();

    @DefaultStringValue("Description")
    String descriptionProfilePopup();

    @DefaultStringValue("Port Mirroring")
    String portMirroringProfilePopup();

    @DefaultStringValue("Allow all users to use this Profile")
    String publicUseProfilePopup();

    @DefaultStringValue("Filters represent hard constraints for running a VM. " +
            "Each filter implements logic which validates a minimum set of requirements in order to run a VM. " +
            "For example, minimum RAM, CPU, designated host, etc. Hosts which fail this validation " +
            "are filtered out from the current request. For basic optimization, it is possible to set one " +
            "filter as the first one, and/or another as the last one. Other than that filter processing order " +
            "is not guaranteed.")
    String clusterPolicyFilterInfo();

    @DefaultStringValue("Weights represent soft constraints for running a VM. " +
            "Note: in a weighting system, lower score is considered better. So a host with the " +
            "lowest score (weight) is the one the scheduler will choose. " +
            "Each weight module scores any given host based on an optimization logic the module " +
            "implements. For example, if we want to optimize for CPU load, the module will score " +
            "each host based on its known CPU load. " +
            "Weight modules scores are being summed, so it is possible to have more than one " +
            "weight module. The way to prioritize modules is by increasing / decreasing a factor.")
    String clusterPolicyWeightFunctionInfo();

    @DefaultStringValue("Load balancing is a logic that determines which hosts are over-utilized and which " +
            "are under-utilized. Then, the balancing mechanism calls the scheduler trying to " +
            "migrate a VM from an over-utilized to an under-utilized host. " +
            "Note that it is important to choose a balancing module that does not conflict with " +
            "the weight module. Such a policy may destabilize this cluster. " +
            "Only a single load-balancing module is supported.")
    String clusterPolicyLoadBalancingInfo();

    @DefaultStringValue("These properties are needed for one of the above modules, so they will appear when " +
            "needed. Setting it when creating a policy generates the default values, which may " +
            "be overridden in each specific cluster using this policy.")
    String clusterPolicyPropertiesInfo();

    @DefaultStringValue("(EXT)")
    String externalPolicyUnitLabel();

    @DefaultStringValue("(Disabled)")
    String disabledPolicyUnit();

    // Volume Rebalance
    @DefaultStringValue("Stop")
    String stopRebalance();

    @DefaultStringValue("Status")
    String statusRebalance();

    @DefaultStringValue("Rebalance in progress")
    String rebalanceInProgress();

    @DefaultStringValue("Rebalance stopped")
    String rebalanceStopped();

    @DefaultStringValue("Rebalance failed")
    String rebalanceFailed();

    @DefaultStringValue("Rebalance completed")
    String rebalanceCompleted();

    @DefaultStringValue("Status At :")
    String rebalanceStatusTime();

    @DefaultStringValue("Volume :")
    String rebalanceVolumeName();

    @DefaultStringValue("Cluster :")
    String rebalanceClusterVolume();

    @DefaultStringValue("Rebalance in progress but status unknown. Unable to fetch the status at the moment.")
    String rebalanceStatusUnknown();

    @DefaultStringValue("Brick removal in progress")
    String removeBrickInProgress();

    @DefaultStringValue("Brick removal stopped")
    String removeBrickStopped();

    @DefaultStringValue("Brick removal failed")
    String removeBrickFailed();

    @DefaultStringValue("Migration completed, pending commit")
    String removeBrickCommitRequired();

    @DefaultStringValue("Remove brick in progress but status unknown. Unable to fetch the status at the moment.")
    String removeBrickStatusUnknown();

    // Volume Rebalance Status Table Columns
    @DefaultStringValue("Host")
    String rebalanceSessionHost();

    @DefaultStringValue("Files Rebalanced")
    String rebalanceFileCount();

    @DefaultStringValue("Files Migrated")
    String filesMigrated();

    @DefaultStringValue("Size")
    String rebalanceSize();

    @DefaultStringValue("Files Failed")
    String rebalanceFailedFileCount();

    @DefaultStringValue("Files Skipped")
    String rebalanceSkippedFileCount();

    @DefaultStringValue("Files Scanned")
    String rebalanceScannedFileCount();

    @DefaultStringValue("Run Time")
    String rebalanceRunTime();

    @DefaultStringValue("Status")
    String rebalanceStatus();

    @DefaultStringValue("(Completed)")
    String rebalanceComplete();

    @DefaultStringValue("Rebalance NOT STARTED")
    String rebalanceNotStarted();

    @DefaultStringValue("Manage Policy Units")
    String managePolicyUnits();

    @DefaultStringValue("Name")
    String policyUnitName();

    @DefaultStringValue("Type")
    String policyUnitType();

    @DefaultStringValue("Remove")
    String removePolicyUnit();

    @DefaultStringValue("Internal Policy Unit")
    String internalPolicyUnit();

    @DefaultStringValue("External Policy Unit")
    String externalPolicyUnit();

    @DefaultStringValue("Optimize for Utilization")
    String optimizeForUtilizationLabel();

    @DefaultStringValue("Optimize for Speed")
    String optimizeForSpeedLabel();

    @DefaultStringValue("Scheduler Optimization")
    String schedulerOptimizationPanelLabel();

    @DefaultStringValue("Allow Overbooking")
    String allowOverbookingLabel();

    @DefaultStringValue("Guaranty Resources")
    String guarantyResourcesLabel();

    @DefaultStringValue("Error getting status of Reports Webapp")
    String reportsWebAppErrorMsg();

    @DefaultStringValue("Reports Webapp deployment is in progress, please try again in a few minutes.")
    String reportsWebAppNotDeployedMsg();

    @DefaultStringValue("Console")
    String consoleTabLabel();

    @DefaultStringValue("Fencing Policy")
    String fencingPolicyTabLabel();

    @DefaultStringValue("Name")
    String nameAffinityGroup();

    @DefaultStringValue("Description")
    String descriptionAffinityGroup();

    @DefaultStringValue("Polarity")
    String polarityAffinityGroup();

    @DefaultStringValue("Enforcing")
    String enforceAffinityGroup();

    @DefaultStringValue("Members")
    String membersAffinityGroup();

    @DefaultStringValue("No Members")
    String noMembersAffinityGroup();

    @DefaultStringValue("New")
    String newAffinityGroupLabel();

    @DefaultStringValue("Edit")
    String editAffinityGroupLabel();

    @DefaultStringValue("Remove")
    String removeAffinityGroupLabel();

    @DefaultStringValue("Name")
    String affinityGroupNameLabel();

    @DefaultStringValue("Positive")
    String affinityGroupPolarityLabel();

    @DefaultStringValue("Enforcing")
    String affinityGroupEnforceTypeLabel();

    @DefaultStringValue("Description")
    String affinityDescriptionLabel();

    @DefaultStringValue("Positive")
    String positiveAffinity();

    @DefaultStringValue("Negative")
    String negativeAffinity();

    @DefaultStringValue("Hard")
    String hardEnforcingAffinity();

    @DefaultStringValue("Soft")
    String softEnforcingAffinity();

    @DefaultStringValue("Add")
    String addIscsiBond();

    @DefaultStringValue("Edit")
    String editIscsiBond();

    @DefaultStringValue("Remove")
    String removeIscsiBond();

    @DefaultStringValue("iSCSI Multipathing")
    String dataCenterIscsiMultipathingSubTabLabel();

    @DefaultStringValue("Changing the display network will cause any running VMs in the cluster to lose display console connectivity until they are restarted.")
    String changeDisplayNetworkWarning();

    // Instance Types
    @DefaultStringValue("Instance Types")
    String instanceTypes();

    @DefaultStringValue("Name")
    String instanceTypeName();

    @DefaultStringValue("New")
    String newInstanceType();

    @DefaultStringValue("Edit")
    String editInstanceType();

    @DefaultStringValue("Remove")
    String removeInstanceType();

    @DefaultStringValue("Disk Snapshots")
    String snapshotsLabel();

    @DefaultStringValue("Attached by label")
    String attachedByLabel();

    // Gluster Volume Profiling
    @DefaultStringValue("File operation")
    String fileOperation();

    @DefaultStringValue("No. of Invocations")
    String fOpInvocationCount();

    @DefaultStringValue("Latency contribution")
    String fOpLatency();

    @DefaultStringValue("Max-Latency")
    String fOpMaxLatency();

    @DefaultStringValue("Min-Latency")
    String fOpMinLatency();

    @DefaultStringValue("Avg-Latency")
    String fOpAvgLatency();

    @DefaultStringValue("Select Brick")
    String selectBrickToViewFopStats();

    @DefaultStringValue("Select Server")
    String selectServerToViewFopStats();

    @DefaultStringValue("BRICKS")
    String volumeProfileBricksTab();

    @DefaultStringValue("NFS SERVER")
    String volumeProfileNfsTab();

    @DefaultStringValue("Start")
    String startVolumeProfiling();

    @DefaultStringValue("Details")
    String volumeProfileDetails();

    @DefaultStringValue("Stop")
    String stopVolumeProfiling();

    @DefaultStringValue("Profiling")
    String volumeProfilingAction();

    @DefaultStringValue("Could not fetch nfs profile stats")
    String nfsProfileErrorMessage();

    @DefaultStringValue("Could not fetch brick profile stats")
    String brickProfileErrorMessage();

    @DefaultStringValue("Save As Pdf")
    String exportToPdf();

    @DefaultStringValue("Enables fencing operations in this cluster. Note that if fencing is disabled, HA VMs running on a non-responsive host will not be restarted elsewhere.")
    String fencingEnabledInfo();

    @DefaultStringValue("This will skip fencing for a Host that has live lease on Storage Domains")
    String skipFencingIfSDActiveInfo();

    @DefaultStringValue("This will skip fencing if the percentage of Cluster Hosts with connectivity issues is greater than or equal to the defined threshold")
    String skipFencingWhenConnectivityBrokenInfo();

    @DefaultStringValue("It is suggested to optimise a Volume for virt store if the volume is of replica-3 type")
    String newVolumeOptimiseForVirtStoreWarning();

    @DefaultStringValue("Total Throughput")
    String storageQosThroughputTotal();

    @DefaultStringValue("Read Throughput")
    String storageQosThroughputRead();

    @DefaultStringValue("Write Throughput")
    String storageQosThroughputWrite();

    @DefaultStringValue("Total IOps")
    String storageQosIopsTotal();

    @DefaultStringValue("Read IOps")
    String storageQosIopsRead();

    @DefaultStringValue("Write IOps")
    String storageQosIopsWrite();

    @DefaultStringValue("New")
    String newQos();

    @DefaultStringValue("Edit")
    String editQos();

    @DefaultStringValue("Remove")
    String removeQos();

    @DefaultStringValue("New")
    String newCpuQos();

    @DefaultStringValue("Edit")
    String editCpuQos();

    @DefaultStringValue("Remove")
    String removeCpuQos();

    @DefaultStringValue("Data Center")
    String dataCenterQosPopup();

    @DefaultStringValue("Description")
    String qosDescription();

    @DefaultStringValue("Throughput")
    String throughputLabelQosPopup();

    @DefaultStringValue("IOps")
    String iopsLabelQosPopup();

    @DefaultStringValue("Read")
    String readStorageQosPopup();

    @DefaultStringValue("None")
    String noneStorageQosPopup();

    @DefaultStringValue("Read / Write")
    String readWriteStorageQosPopup();

    @DefaultStringValue("Total")
    String totalStorageQosPopup();

    @DefaultStringValue("Write")
    String writeStorageQosPopup();

    @DefaultStringValue("MB/s")
    String mbpsLabelStorageQosPopup();

    @DefaultStringValue("IOps")
    String iopsCountLabelQosPopup();

    @DefaultStringValue("QoS")
    String diskProfileQosLabel();

    @DefaultStringValue("Disk Profiles")
    String diskProfilesSubTabLabel();

    @DefaultStringValue("Storage Domain")
    String diskProfileStorageDomainLabel();

    @DefaultStringValue("Limit")
    String cpuQosCpuLimit();

    @DefaultStringValue("Description")
    String cpuQosDescription();

    @DefaultStringValue("CPU Profiles")
    String cpuProfilesSubTabLabel();

    @DefaultStringValue("Cluster")
    String cpuProfileClusterLabel();

    @DefaultStringValue("QoS")
    String cpuProfileQosLabel();

    @DefaultStringValue("NUMA Support")
    String numaSupport();

    // Volume snapshots
    @DefaultStringValue("New")
    String newVolumeSnapshot();

    @DefaultStringValue("Edit Schedule")
    String editVolumeSnapshotSchedule();

    @DefaultStringValue("Schedule")
    String scheduleLabel();

    @DefaultStringValue("Recurrence")
    String recurrenceLabel();

    @DefaultStringValue("Interval (mins)")
    String intervalLabel();

    @DefaultStringValue("End By")
    String endByLabel();

    @DefaultStringValue("End By Date")
    String endByDateLabel();

    @DefaultStringValue("Time Zone")
    String timeZoneLabel();

    @DefaultStringValue("Days Of month")
    String daysOfMonthLabel();

    @DefaultStringValue("Days Of Week")
    String daysOfWeekLabel();

    @DefaultStringValue("Start At")
    String startAtLabel();

    @DefaultStringValue("Execution Time")
    String executionTimeLabel();

    @DefaultStringValue("No End Date")
    String noEndDateOption();

    @DefaultStringValue("Date")
    String endDateOption();

    @DefaultStringValue("Snapshot Name Prefix")
    String volumeSnapshotNamePrefixLabel();

    @DefaultStringValue("Snapshot name would be formed in the format <prefix><timestamp>")
    String snapshotNameInfo();

    @DefaultStringValue("Description")
    String volumeSnapshotDescriptionLabel();

    @DefaultStringValue("Name")
    String volumeSnapshotName();

    @DefaultStringValue("Description")
    String volumeSnapshotDescription();

    @DefaultStringValue("Creation Time")
    String volumeSnapshotCreationTime();

    @DefaultStringValue("No of snapshots")
    String noOfSnapshotsLabel();

    @DefaultStringValue("Cluster")
    String volumeClusterLabel();

    @DefaultStringValue("Volume")
    String volumeNameLabel();

    @DefaultStringValue("Snapshot")
    String volumeSnapshotMainTabTitle();

    @DefaultStringValue("Snapshot Scheduled")
    String snapshotScheduledLabel();

    @DefaultStringValue("Options - Cluster")
    String configureClusterSnapshotOptions();

    @DefaultStringValue("Options - Volume")
    String configureVolumeSnapshotOptions();

    @DefaultStringValue("Option")
    String volumeSnapshotConfigName();

    @DefaultStringValue("Cluster Value")
    String clusterSnapshotConfigValue();

    @DefaultStringValue("Value")
    String volumeSnapshotConfigValue();

    @DefaultStringValue("Snapshot Options")
    String snapshotConfigHeaderLabel();

    @DefaultStringValue("Mount Point")
    String mountPoint();

    @DefaultStringValue("Device is already in use")
    String deviceIsAlreadyUsed();

    @DefaultStringValue("Sync")
    String syncStorageDevices();

    @DefaultStringValue("Terminate Session")
    String terminateSession();

    @DefaultStringValue("Restore")
    String restoreVolumeSnapshot();

    @DefaultStringValue("Delete")
    String deleteVolumeSnapshot();

    @DefaultStringValue("Delete All")
    String deleteAllVolumeSnapshots();

    @DefaultStringValue("Activate")
    String activateVolumeSnapshot();

    @DefaultStringValue("Deactivate")
    String deactivateVolumeSnapshot();

    @DefaultStringValue("Reset")
    String resetGeoRepSessionConfig();

    @DefaultStringValue("Frequent creation of snapshots would overload the cluster")
    String criticalSnapshotIntervalNote();

    @DefaultStringValue("Brick Name")
    String logicalVolume();

    @DefaultStringValue("Size")
    String lvSize();

    @DefaultStringValue("Create Brick")
    String createBrick();

    @DefaultStringValue("Extend Brick")
    String extendBrick();

    @DefaultStringValue("RAID Type")
    String raidType();

    @DefaultStringValue("No. of Physical Disks in RAID Volume")
    String noOfPhysicalDisksInRaidVolume();

    @DefaultStringValue("Stripe Size")
    String stripeSize();

    @DefaultStringValue("Choose storage devices of RAID type: ")
    String getStorageDeviceSelectionInfo();

    @DefaultStringValue("Auto-start geo-replication session after creation")
    String geoRepSessionCreateAndStart();

    @DefaultStringValue("Master Volume")
    String geoRepMasterVolume();

    @DefaultStringValue("Destination host")
    String geoRepSlaveHostIp();

    @DefaultStringValue("Slave User")
    String geoRepSlaveUserName();

    @DefaultStringValue("User")
    String geoRepUserSessionCreate();

    @DefaultStringValue("Show volumes eligible for geo-replication")
    String geoRepShowEligibleVolumes();

    @DefaultStringValue("User Name")
    String geoRepSessionUserName();

    @DefaultStringValue("User Group")
    String slaveUserGroupName();

    @DefaultStringValue("Password")
    String geoRepSlaveNodePassword();

    @DefaultStringValue("Destination cluster")
    String geoRepSessionSlaveCluster();

    @DefaultStringValue("Destination volume")
    String geoRepSlaveVolume();

    @DefaultStringValue("Geo-replication")
    String geoReplicationMainTabTitle();

    @DefaultStringValue("Show Volumes")
    String selectGeoRepSlaveVolumeButtonLabel();

    @DefaultStringValue("Fetching Data")
    String fetchingDataMessage();

    @DefaultStringValue("Warning : Recommendations for geo-replication not met -")
    String geoReplicationRecommendedConfigViolation();
}
