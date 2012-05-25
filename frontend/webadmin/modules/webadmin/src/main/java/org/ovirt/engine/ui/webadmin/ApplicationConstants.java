package org.ovirt.engine.ui.webadmin;

import org.ovirt.engine.ui.common.CommonApplicationConstants;

public interface ApplicationConstants extends CommonApplicationConstants {

    @DefaultStringValue("oVirt Enterprise Virtualization Engine Web Administration")
    String applicationTitle();

    @DefaultStringValue("About")
    String aboutPopupCaption();

    @DefaultStringValue("This Browser version isn't optimal for displaying the application graphics (refer to Documentation for details)")
    String browserNotSupported();

    @DefaultStringValue("oVirt Enterprise Virtualization Engine Manager Version:")
    String ovirtVersionAbout();

    // Widgets

    @DefaultStringValue("Refresh")
    String actionTableRefreshPageButtonLabel();

    // Login section

    @DefaultStringValue("User Name")
    String loginFormUserNameLabel();

    @DefaultStringValue("Password")
    String loginFormPasswordLabel();

    @DefaultStringValue("Domain")
    String loginFormDomainLabel();

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

    @DefaultStringValue("Summary")
    String volumeGeneralSubTabLabel();

    @DefaultStringValue("Volume Options")
    String volumeParameterSubTabLabel();

    @DefaultStringValue("Bricks")
    String volumeBrickSubTabLabel();

    @DefaultStringValue("Permissions")
    String volumePermissionSubTabLabel();

    @DefaultStringValue("Events")
    String volumeEventSubTabLabel();

    @DefaultStringValue("Storage")
    String dataCenterStorageSubTabLabel();

    @DefaultStringValue("Logical Networks")
    String dataCenterNetworkSubTabLabel();

    @DefaultStringValue("This operation is destructive and unrecoverable!")
    String dataCenterRecoveryStoragePopupWarningLabel();

    @DefaultStringValue("This operation will replace the current master domain with the selected domain.<br/> After the operation is finished you will be able to remove the replaced domain if desired.")
    String dataCenterRecoveryStoragePopupMessageLabel();

    @DefaultStringValue("Select new Data Storage Domain(Master):")
    String dataCenterRecoveryStoragePopupSelectNewDSDLabel();

    @DefaultStringValue("Name")
    String clusterNewNetworkNameLabel();

    @DefaultStringValue("Description")
    String clusterNewNetworkDescriptionLabel();

    @DefaultStringValue("VM network")
    String clusterNewNetworkPopupVmNetworkLabel();

    @DefaultStringValue("Enable VLAN tagging")
    String clusterNewNetworkPopupVlanEnabledLabel();

    @DefaultStringValue("VLAN tag")
    String clusterNewNetworkPopupVlanTagLabel();

    @DefaultStringValue("Override MTU")
    String clusterNewNetworkPopupMtuEnabledLabel();

    @DefaultStringValue("MTU")
    String clusterNewNetworkPopupMtuLabel();

    @DefaultStringValue("Networks")
    String clusterManageNetworkPopupLabel();

    @DefaultStringValue("Clusters")
    String dataCenterClusterSubTabLabel();

    @DefaultStringValue("Quota")
    String dataCenterQuotaSubTabLabel();

    @DefaultStringValue("Permissions")
    String dataCenterPermissionSubTabLabel();

    @DefaultStringValue("Events")
    String dataCenterEventSubTabLabel();

    @DefaultStringValue("Name")
    String dataCenterPopupNameLabel();

    @DefaultStringValue("Description")
    String dataCenterPopupDescriptionLabel();

    @DefaultStringValue("VM network")
    String dataCenterPopupVmNetworkLabel();

    @DefaultStringValue("Enable VLAN tagging")
    String dataCenterPopupEnableVlanTagLabel();

    @DefaultStringValue("Override MTU")
    String dataCenterPopupEnableMtuLabel();

    @DefaultStringValue("Type")
    String dataCenterPopupStorageTypeLabel();

    @DefaultStringValue("Compatibility Version")
    String dataCenterPopupVersionLabel();

    @DefaultStringValue("Quota Mode")
    String dataCenterPopupQuotaEnforceTypeLabel();

    @DefaultStringValue("Edit Network Parameters")
    String dataCenterNetworkPopupLabel();

    @DefaultStringValue("(To allow this option, detach all clusters from network)")
    String dataCenterNetworkPopupSubLabel();

    @DefaultStringValue("Assign Networks to Cluster(s)")
    String dataCenterNetworkPopupAssignLabel();

    @DefaultStringValue("Name")
    String storagePopupNameLabel();

    @DefaultStringValue("Data Center")
    String storagePopupDataCenterLabel();

    @DefaultStringValue("Domain Function / Storage Type")
    String storagePopupStorageTypeLabel();

    @DefaultStringValue("Format")
    String storagePopupFormatTypeLabel();

    @DefaultStringValue("Use Host")
    String storagePopupHostLabel();

    @DefaultStringValue("Export Path")
    String storagePopupNfsPathLabel();

    @DefaultStringValue("Path")
    String storagePopupPosixPathLabel();

    @DefaultStringValue("VFS Type")
    String storagePopupVfsTypeLabel();

    @DefaultStringValue("Mount Options")
    String storagePopupMountOptionsLabel();

    @DefaultStringValue("Path")
    String storagePopupLocalPathLabel();

    @DefaultStringValue("Please use 'FQDN:/path' or 'IP:/path' Example 'server.example.com:/export/VMs'")
    String storagePopupNfsMessageLabel();

    @DefaultStringValue("Path to device to mount / remote export")
    String storagePopupPosixPathHintLabel();

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
    String storageIsoSubTabLabel();

    @DefaultStringValue("Permissions")
    String storagePermissionSubTabLabel();

    @DefaultStringValue("Events")
    String storageEventSubTabLabel();

    @DefaultStringValue("General")
    String clusterGeneralSubTabLabel();

    @DefaultStringValue("Hosts")
    String clusterHostSubTabLabel();

    @DefaultStringValue("Virtual Machines")
    String clusterVmSubTabLabel();

    @DefaultStringValue("Logical Networks")
    String clusterNetworkSubTabLabel();

    @DefaultStringValue("Permissions")
    String clusterPermissionSubTabLabel();

    @DefaultStringValue("General")
    String virtualMachineGeneralSubTabLabel();

    @DefaultStringValue("Network Interfaces")
    String virtualMachineNetworkInterfaceSubTabLabel();

    @DefaultStringValue("Virtual Disks")
    String virtualMachineVirtualDiskSubTabLabel();

    @DefaultStringValue("Snapshots")
    String virtualMachineSnapshotSubTabLabel();

    @DefaultStringValue("Applications")
    String virtualMachineApplicationSubTabLabel();

    @DefaultStringValue("Permissions")
    String virtualMachinePermissionSubTabLabel();

    @DefaultStringValue("Events")
    String virtualMachineEventSubTabLabel();

    @DefaultStringValue("General")
    String hostGeneralSubTabLabel();

    @DefaultStringValue("Virtual Machines")
    String hostVmSubTabLabel();

    @DefaultStringValue("Network Interfaces")
    String hostIfaceSubTabLabel();

    @DefaultStringValue("Host Hooks")
    String hostHookSubTabLabel();

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

    @DefaultStringValue("Address")
    String hostPopupHostAddressLabel();

    @DefaultStringValue("Root Password")
    String hostPopupRootPasswordLabel();

    @DefaultStringValue("Override IP tables")
    String hostPopupOverrideIpTablesLabel();

    @DefaultStringValue("Enable Power Management")
    String hostPopupPmEnabledLabel();

    @DefaultStringValue("Address")
    String hostPopupPmAddressLabel();

    @DefaultStringValue("User Name")
    String hostPopupPmUserNameLabel();

    @DefaultStringValue("Password")
    String hostPopupPmPasswordLabel();

    @DefaultStringValue("Type")
    String hostPopupPmTypeLabel();

    @DefaultStringValue("Port")
    String hostPopupPmPortLabel();

    @DefaultStringValue("Slot")
    String hostPopupPmSlotLabel();

    @DefaultStringValue("Options")
    String hostPopupPmOptionsLabel();

    @DefaultStringValue("Please use a comma-separated list of 'key=value' or 'key'")
    String hostPopupPmOptionsExplanationLabel();

    @DefaultStringValue("Secure")
    String hostPopupPmSecureLabel();

    @DefaultStringValue("Test")
    String hostPopupTestButtonLabel();

    @DefaultStringValue("SPM")
    String spmTestButtonLabel();

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

    @DefaultStringValue("Override IP tables")
    String hostInstallOverrideIpTablesLabel();

    @DefaultStringValue("General")
    String importVmGeneralSubTabLabel();

    @DefaultStringValue("Network Interfaces")
    String importVmNetworkIntefacesSubTabLabel();

    @DefaultStringValue("Virtual Disks")
    String importVmVirualDisksSubTabLabel();

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

    @DefaultStringValue("Virtual Disks")
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

    @DefaultStringValue("CPU Name")
    String clusterPopupCPULabel();

    @DefaultStringValue("Compatibility Version")
    String clusterPopupVersionLabel();

    @DefaultStringValue("Memory Optimization")
    String clusterPopupMemoryOptimizationTabLabel();

    @DefaultStringValue("None")
    String clusterPopupOptimizationNoneLabel();

    @DefaultStringValue("For Server Load")
    String clusterPopupOptimizationForServerLabel();

    @DefaultStringValue("For Desktop Load")
    String clusterPopupOptimizationForDesktopLabel();

    @DefaultStringValue("Custom Value")
    String clusterPopupOptimizationCustomLabel();

    @DefaultStringValue("Memory Page Sharing is Disabled")
    String clusterPopupOptimizationNoneExplainationLabel();

    @DefaultStringValue("Memory Page Sharing Threshold set to %1$s. Allow VMs to run on the Host up to the overcommit threshold")
    String clusterPopupOptimizationForServerExplainationLabel();

    @DefaultStringValue("Memory Page Sharing Threshold set to %1$s. Allow VMs to run on the Host up to the overcommit threshold")
    String clusterPopupOptimizationForDesktopExplainationLabel();

    @DefaultStringValue("Memory Page Sharing Threshold set to %1$s via API/CLI")
    String clusterPopupOptimizationCustomExplainationLabel();

    @DefaultStringValue("Resilience Policy")
    String clusterPopupResiliencePolicyTabLabel();

    @DefaultStringValue("Migrate Virtual Machines")
    String clusterPopupMigrateOnError_YesLabel();

    @DefaultStringValue("Migrate only Highly Available Virtual Machines")
    String clusterPopupMigrateOnError_HaLabel();

    @DefaultStringValue("Do Not Migrate Virtual Machines")
    String clusterPopupMigrateOnError_NoLabel();

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

    @DefaultStringValue("No. Of Volumes")
    String clusterVolumesTotalLabel();

    @DefaultStringValue("Up")
    String clusterVolumesUpLabel();

    @DefaultStringValue("Down")
    String clusterVolumesDownLabel();

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

    @DefaultStringValue("Destination Cluster")
    String importVm_destCluster();

    @DefaultStringValue("Collapse All Snapshots")
    String importVm_collapseSnapshots();

    @DefaultStringValue("Expand All")
    String treeExpandAll();

    @DefaultStringValue("Collapse All")
    String treeCollapseAll();

    @DefaultStringValue("Mail Recipient:")
    String manageEventsPopupEmailLabel();

    @DefaultStringValue("Select the Events for Notification:")
    String manageEventsPopupTitleLabel();

    @DefaultStringValue("Required actions:")
    String guidePopupRequiredActionsLabel();

    @DefaultStringValue("Optional actions:")
    String guidePopupOptionalActionsLabel();

    @DefaultStringValue("There are still unconfigured entities:")
    String guidePopupUnconfiguredLabel();

    @DefaultStringValue("Configuration completed.")
    String guidePopupConfigurationCompletedLabel();

    @DefaultStringValue("Data Center created.")
    String guidePopupDataCenterCreatedLabel();

    @DefaultStringValue("Cluster created.")
    String guidePopupClusterCreatedLabel();

    @DefaultStringValue("Virtual Machine created.")
    String guidePopupVMCreatedLabel();

    @DefaultStringValue("Cluster:")
    String moveHostPopupClusterLabel();

    @DefaultStringValue("Please select reports from the same data center")
    String reportFromDifferentDCsError();

    @DefaultStringValue("Resides on a different storage domain")
    String differentStorageDomainWarning();

    @DefaultStringValue("Edit")
    String editText();

    @DefaultStringValue("Close")
    String closeText();

    @DefaultStringValue("Storage domain can be modified only when 'Single Destination Domain' is unchecked")
    String importVmTemplateSingleStorageCheckedLabel();

    @DefaultStringValue("Some Templates' disks which the selected VMs are based on are missing from the Data-Center.<br/>"
            + "Suggested solutions: "
            + "1. Preserving Template-Based structure: "
            + "a. Make sure the relevant Templates (on which the VMs are based) exist on the relevant Data-Center. "
            + "b. Import the VMs one by one. "
            + "2. Using non-Template-Based structure (less optimal storage-wise): "
            + "a. Export the VMs again using the Collapse Snapshots option. "
            + "b. Import the VMs.")
    String importMissingStorages();

    @DefaultStringValue("Allocation can be modified only when importing a single VM")
    String importAllocationModifiedSingleVM();

    @DefaultStringValue("Allocation can be modified only when 'Collapse All Snapshots' is checked")
    String importAllocationModifiedCollapse();

    @DefaultStringValue("Preallocated")
    String preallocatedAllocation();

    @DefaultStringValue("Thin Provision")
    String thisAllocation();

    @DefaultStringValue("Clusters")
    String quotaClusterSubTabLabel();

    @DefaultStringValue("Enable Ovirt Service")
    String clusterEnableOvirtServiceLabel();

    @DefaultStringValue("Enable Gluster Service")
    String clusterEnableGlusterServiceLabel();

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

    @DefaultStringValue("Source")
    String sourceStorage();

    @DefaultStringValue("Destination")
    String destinationStorage();

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

    // Storage DC
    @DefaultStringValue("Domain Status in Data-Center")
    String domainStatusInDcStorageDc();

    @DefaultStringValue("Attach")
    String attachStorageDc();

    @DefaultStringValue("Detach")
    String detachStorageDc();

    @DefaultStringValue("Activate")
    String activateStorageDc();

    @DefaultStringValue("Maintenance")
    String maintenanceStorageDc();

    // Quota Storage
    @DefaultStringValue("Name")
    String nameQuotaStorage();

    @DefaultStringValue("Used Storage/Total")
    String usedStorageTotalQuotaStorage();

    // Cluster
    @DefaultStringValue("Name")
    String nameCluster();

    @DefaultStringValue("Compatiblity Version")
    String comptVersCluster();

    @DefaultStringValue("Description")
    String descriptionCluster();

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

    @DefaultStringValue("Used Memory/Total")
    String usedMemoryTotalCluster();

    @DefaultStringValue("Running CPU/Total")
    String runningCpuTotalCluster();

    // Host
    @DefaultStringValue("Name")
    String nameHost();

    @DefaultStringValue("Host/IP")
    String ipHost();

    @DefaultStringValue("Cluster")
    String clusterHost();

    @DefaultStringValue("Status")
    String statusHost();

    @DefaultStringValue("Load")
    String loadHost();

    @DefaultStringValue("Memory")
    String memoryHost();

    @DefaultStringValue("CPU")
    String cpuHost();

    @DefaultStringValue("Network")
    String networkHost();

    @DefaultStringValue("SPM priority")
    String spmPriorityHost();

    @DefaultStringValue("SpmStatus")
    String spmStatusHost();

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

    @DefaultStringValue("Confirm 'Host has been Rebooted'")
    String confirmRebootedHost();

    @DefaultStringValue("Approve")
    String approveHost();

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

    // host- general
    @DefaultStringValue("OS Version")
    String osVersionHostGeneral();

    @DefaultStringValue("Kernel Version")
    String kernelVersionHostGeneral();

    @DefaultStringValue("KVM Version")
    String kvmVersionHostGeneral();

    @DefaultStringValue("VDSM Version")
    String vdsmVersionHostGeneral();

    @DefaultStringValue("SPICE Version")
    String spiceVersionHostGeneral();

    @DefaultStringValue("iSCSI Initiator Name")
    String isciInitNameHostGeneral();

    @DefaultStringValue("Active VMs")
    String activeVmsHostGeneral();

    @DefaultStringValue("Memory Page Sharing")
    String memPageSharingHostGeneral();

    @DefaultStringValue("Automatic Large Pages")
    String autoLargePagesHostGeneral();

    @DefaultStringValue("Number of CPUs")
    String numOfCpusHostGeneral();

    @DefaultStringValue("CPU Name")
    String cpuNameHostGeneral();

    @DefaultStringValue("CPU Type")
    String cpuTypeHostGeneral();

    @DefaultStringValue("Physical Memory")
    String physMemHostGeneral();

    @DefaultStringValue("Swap Size")
    String swapSizeHostGeneral();

    @DefaultStringValue("Shared Memory")
    String sharedMemHostGeneral();

    @DefaultStringValue("Action Items")
    String actionItemsHostGeneral();

    // Storage
    @DefaultStringValue("Domain Name")
    String domainNameStorage();

    @DefaultStringValue("Domain Type")
    String domainTypeStorage();

    @DefaultStringValue("Storage Type")
    String storageTypeStorage();

    @DefaultStringValue("Format")
    String formatStorage();

    @DefaultStringValue("Cross Data-Center Status")
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

    @DefaultStringValue("Over Allocation Ratio")
    String overAllocRatioStorageGeneral();

    @DefaultStringValue("NFS Export Path")
    String nfsExportPathStorageGeneral();

    @DefaultStringValue("Local Path on Host")
    String localPathOnHostStorageGeneral();

    // VM
    @DefaultStringValue("Cluster")
    String clusterVm();

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

    @DefaultStringValue("Network")
    String networkVm();

    @DefaultStringValue("Display")
    String displayVm();

    @DefaultStringValue("Status")
    String statusVm();

    @DefaultStringValue("Uptime")
    String uptimeVm();

    @DefaultStringValue("Logged-in User")
    String loggedInUserVm();

    @DefaultStringValue("New Server")
    String newServerVm();

    @DefaultStringValue("New Desktop")
    String newDesktopVm();

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

    @DefaultStringValue("Shut down")
    String shutDownVm();

    @DefaultStringValue("Stop")
    String stopVm();

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

    @DefaultStringValue("Move")
    String moveVm();

    @DefaultStringValue("Change CD")
    String cheangeCdVm();

    @DefaultStringValue("Assign Tags")
    String assignTagsVm();

    @DefaultStringValue("Show Report")
    String showReportVm();

    @DefaultStringValue("Guide Me")
    String guideMeVm();

    @DefaultStringValue("Disks")
    String disksVm();

    @DefaultStringValue("V-Size")
    String vSizeVm();

    @DefaultStringValue("Actual Size")
    String actualSizeVm();

    @DefaultStringValue("Creation Date")
    String creationDateVm();

    @DefaultStringValue("Detach")
    String detachVm();

    @DefaultStringValue("Restore")
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

    @DefaultStringValue("Status")
    String statusTemplate();

    @DefaultStringValue("Cluster")
    String clusterTemplate();

    @DefaultStringValue("Description")
    String descriptionTemplate();

    @DefaultStringValue("Edit")
    String editTemplate();

    @DefaultStringValue("Remove")
    String removeTemplate();

    @DefaultStringValue("Export")
    String exportTemplate();

    @DefaultStringValue("Copy")
    String copyTemplate();

    @DefaultStringValue("Disks")
    String disksTemplate();

    @DefaultStringValue("Actual Size")
    String actualSizeTemplate();

    @DefaultStringValue("Origin")
    String originTemplate();

    @DefaultStringValue("Memory")
    String memoryTemplate();

    @DefaultStringValue("CPUs")
    String cpusTemplate();

    @DefaultStringValue("Restore")
    String restoreTemplate();

    // User
    @DefaultStringValue("First Name")
    String firstnameUser();

    @DefaultStringValue("Last Name")
    String lastNameUser();

    @DefaultStringValue("User Name")
    String userNameUser();

    @DefaultStringValue("Group")
    String groupUser();

    @DefaultStringValue("e-mail")
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

    // User- general
    @DefaultStringValue("Domain")
    String domainUserGeneral();

    @DefaultStringValue("Status")
    String statusUserGeneral();

    @DefaultStringValue("E-mail")
    String emailUserGeneral();

    // Quota
    @DefaultStringValue("Name")
    String nameQuota();

    @DefaultStringValue("Description")
    String descriptionQuota();

    @DefaultStringValue("Data Center")
    String dcQuota();

    @DefaultStringValue("Used Memory/Total")
    String usedMemoryQuota();

    @DefaultStringValue("Running CPU/Total")
    String runningCpuQuota();

    @DefaultStringValue("Used Storage/Total")
    String usedStorageQuota();

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

    @DefaultStringValue("Quota")
    String quota();

    @DefaultStringValue("Edit")
    String editCellQuota();

    // Network
    @DefaultStringValue("Attached")
    String attachedNetwork();

    @DefaultStringValue("Name")
    String nameNetwork();

    @DefaultStringValue("Required")
    String requiredNetwork();

    @DefaultStringValue("VM")
    String vmNetwork();

    @DefaultStringValue("Status")
    String statusNetwork();

    @DefaultStringValue("Display")
    String displayNetwork();

    @DefaultStringValue("Role")
    String roleNetwork();

    @DefaultStringValue("Description")
    String descriptionNetwork();

    @DefaultStringValue("Add Network")
    String addNetworkNetwork();

    @DefaultStringValue("Assign/Detach Networks")
    String assignDetatchNetworksNework();

    @DefaultStringValue("Set as Display")
    String setAsDisplayNetwork();

    @DefaultStringValue("New")
    String newNetwork();

    @DefaultStringValue("Edit")
    String editNetwork();

    @DefaultStringValue("Remove")
    String removeNetwork();

    // Cluster host
    @DefaultStringValue("Name")
    String nameClusterHost();

    @DefaultStringValue("Host/IP")
    String hostIpClusterHost();

    @DefaultStringValue("Status")
    String statusClusterHost();

    @DefaultStringValue("VMs")
    String vmsClusterHost();

    @DefaultStringValue("Load")
    String loadClusterHost();

    // Interface
    @DefaultStringValue("Empty")
    String emptyInterface();

    @DefaultStringValue("Address")
    String addressInterface();

    @DefaultStringValue("Bond")
    String bondInterface();

    @DefaultStringValue("Vlan")
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

    // Group
    @DefaultStringValue("Group Name")
    String groupNameGroup();

    @DefaultStringValue("Organizational Unit")
    String orgUnitGroup();

    @DefaultStringValue("Domain")
    String domainGroup();

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

    @DefaultStringValue("Correlation Id")
    String eventCorrelationId();

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

    @DefaultStringValue("oVirt Enterprise Virtualization Engine Hypervisor Hosts:")
    String ovirtHypHostAbout();

    @DefaultStringValue("[No Hosts]")
    String noHostsAbout();

    @DefaultStringValue("oVirt Engine for Servers and Desktops:")
    String ovirtServersAndDesktopsAbout();

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

    // Network popup
    @DefaultStringValue("Detach ALL Clusters")
    String detachAllClustersNetworkPopup();

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
    @DefaultStringValue("Tree")
    String treeMainSection();

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

    @DefaultStringValue("Boot Protocol")
    String bootProtocolHostPopup();

    @DefaultStringValue("IP")
    String ipHostPopup();

    @DefaultStringValue("Subnet Mask")
    String subnetMaskHostPopup();

    @DefaultStringValue("Default Gateway:")
    String defaultGwHostPopup();

    @DefaultStringValue("Check Connectivity:")
    String checkConHostPopup();

    @DefaultStringValue("<I>Changes done to the Networking configuration are temporary until explicitly saved.<BR>" +
            "Check the check-box below to make the changes persistent.</I>")
    String changesTempHostPopup();

    @DefaultStringValue("Save network configuration")
    String saveNetConfigHostPopup();

    @DefaultStringValue("Name")
    String nameHostPopup();

    @DefaultStringValue("Interface")
    String intefaceHostPopup();

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

    // Storage tree
    @DefaultStringValue("Name")
    String nameStorageTree();

    @DefaultStringValue("Size")
    String sizeStorageTree();

    @DefaultStringValue("Type")
    String typeStorageTree();

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

    @DefaultStringValue("Virtual Disks")
    String virtualDisksImpTempTab();

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
    @DefaultStringValue("no netowrk assigned")
    String noNetowrkAssigned();

    // Item info
    @DefaultStringValue("Name")
    String nameItemInfo();

    @DefaultStringValue("Description")
    String descriptionItemInfo();

    @DefaultStringValue("VLan")
    String VLanItemInfo();

    @DefaultStringValue("Display")
    String displayItemInfo();

    @DefaultStringValue("Management")
    String managementItemInfo();

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

    @DefaultStringValue("Gluster")
    String glusterVolume();

    @DefaultStringValue("NFS")
    String nfsVolume();

    @DefaultStringValue("Allow Access From")
    String allowAccessFromVolume();

    @DefaultStringValue("Name")
    String NameVolume();

    @DefaultStringValue("Volume Type")
    String volumeTypeVolume();

    @DefaultStringValue("Number of Bricks")
    String numberOfBricksVolume();

    @DefaultStringValue("Replica Count")
    String replicaCountVolume();

    @DefaultStringValue("Transport Type")
    String transportTypeVolume();

    @DefaultStringValue("Status")
    String statusVolume();

    @DefaultStringValue("Create Volume")
    String createVolumeVolume();

    @DefaultStringValue("Remove")
    String removeVolume();

    @DefaultStringValue("Start")
    String startVolume();

    @DefaultStringValue("Stop")
    String stopVolume();

    @DefaultStringValue("Rebalance")
    String rebalanceVolume();

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

    @DefaultStringValue("Volume Format")
    String volumeFormatDisk();

    @DefaultStringValue("Bootable")
    String bootableDisk();

    @DefaultStringValue("Description")
    String descriptionDisk();

    // Setup network
    @DefaultStringValue("Drag to make changes")
    String dragToMakeChangesSetupNetwork();

    @DefaultStringValue("No Valid Action")
    String noValidActionSetupNetwork();

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

    // Brick
    @DefaultStringValue("Status")
    String statusBrick();

    @DefaultStringValue("Add Bricks")
    String addBricksBrick();

    @DefaultStringValue("Remove Bricks")
    String removeBricksBrick();

    @DefaultStringValue("Server")
    String serverBricks();

    @DefaultStringValue("Brick Directory")
    String brickDirectoryBricks();

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

    @DefaultStringValue("Cluster Threshold")
    String quotaClusterThreshold();

    @DefaultStringValue("Cluster Grace")
    String quotaClusterGrace();

    @DefaultStringValue("Storage Threshold")
    String quotaStorageThreshold();

    @DefaultStringValue("Storage Grace")
    String quotaStorageGrace();

    @DefaultStringValue("Unlimited")
    String unlimitedQuota();

    @DefaultStringValue(" out of ")
    String outOfQuota();

    @DefaultStringValue("Clone All VMs")
    String importVm_cloneAllVMs();

    @DefaultStringValue("Clone All Templates")
    String importTemplate_cloneAllTemplates();

    @DefaultStringValue("Clone Only Duplicated VMs")
    String importVm_cloneOnlyDuplicateVMs();

    @DefaultStringValue("Clone Only Duplicated Templates")
    String importTemplate_cloneOnlyDuplicateTemplates();

    @DefaultStringValue("Suffix:")
    String import_cloneSuffix();

    @DefaultStringValue("VM in System")
    String vmInSetup();

    @DefaultStringValue("Template in System")
    String templateInSetup();

    @DefaultStringValue("* Note that cloned vm will be 'Collapsed Snapshot'")
    String noteClone_CollapsedSnapshotMsg();
}
