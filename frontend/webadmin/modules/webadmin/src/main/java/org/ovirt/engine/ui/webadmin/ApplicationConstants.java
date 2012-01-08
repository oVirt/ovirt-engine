package org.ovirt.engine.ui.webadmin;

import com.google.gwt.i18n.client.Constants;

public interface ApplicationConstants extends Constants {

    // Common stuff

    @DefaultStringValue("Oops!")
    String errorPopupCaption();

    @DefaultStringValue("About")
    String aboutPopupCaption();

    @DefaultStringValue("Close")
    String closeButtonLabel();

    // Widgets

    @DefaultStringValue("Next >>")
    String actionTableNextPageButtonLabel();

    @DefaultStringValue("<< Prev")
    String actionTablePrevPageButtonLabel();

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

    @DefaultStringValue("Storage")
    String dataCenterStorageSubTabLabel();

    @DefaultStringValue("Logical Networks")
    String dataCenterNetworkSubTabLabel();

    @DefaultStringValue("Name")
    String clusterNewNetworkNameLabel();

    @DefaultStringValue("Description")
    String clusterNewNetworkDescriptionLabel();

    @DefaultStringValue("STP support")
    String clusterNewNetworkPopupStpEnabledLabel();

    @DefaultStringValue("Enable VLAN tagging")
    String clusterNewNetworkPopupVlanEnabledLabel();

    @DefaultStringValue("VLAN tag")
    String clusterNewNetworkPopupVlanIdLabel();

    @DefaultStringValue("Networks")
    String clusterManageNetworkPopupLabel();

    @DefaultStringValue("Clusters")
    String dataCenterClusterSubTabLabel();

    @DefaultStringValue("Permissions")
    String dataCenterPermissionSubTabLabel();

    @DefaultStringValue("Events")
    String dataCenterEventSubTabLabel();

    @DefaultStringValue("Name")
    String dataCenterPopupNameLabel();

    @DefaultStringValue("Description")
    String dataCenterPopupDescriptionLabel();

    @DefaultStringValue("Type")
    String dataCenterPopupStorageTypeLabel();

    @DefaultStringValue("Compatibility Version")
    String dataCenterPopupVersionLabel();

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
    String storagePopupLocalPathLabel();

    @DefaultStringValue("Please use 'FQDN:/path' or 'IP:/path' Example 'server.example.com:/export/VMs'")
    String storagePopupNfsMessageLabel();

    @DefaultStringValue("Select Host to be used")
    String storageRemovePopupHostLabel();

    @DefaultStringValue("Format Domain, i.e. Storage Content will be lost!")
    String storageRemovePopupFormatLabel();

    @DefaultStringValue("The following operation is unrecoverable and destructive!")
    String storageDestroyPopupWarningLabel();

    @DefaultStringValue("All references to objects that reside on Storage Domain %1$s in the database will be removed. You may need to manually clean the storage in order to reuse it.")
    String storageDestroyPopupMessageLabel();

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

    @DefaultStringValue("Description")
    String virtualMachineSnapshotCreatePopupDescriptionLabel();

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

    @DefaultStringValue("Root Password")
    String hostInstallPasswordLabel();

    @DefaultStringValue("RHEV-H ISO Name")
    String hostInstallIsoLabel();

    @DefaultStringValue("Override IP tables")
    String hostInstallOverrideIpTablesLabel();

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

    @DefaultStringValue("Directory Groups")
    String userGroupsSubTabLabel();

    @DefaultStringValue("Event Notifier")
    String userEventNotifierSubTabLabel();

    @DefaultStringValue("Events")
    String userEventSubTabLabel();

    @DefaultStringValue("Events")
    String eventMainTabLabel();

    @DefaultStringValue("Basic View")
    String eventBasicViewLabel();

    @DefaultStringValue("Advanced View")
    String eventAdvancedViewLabel();

    @DefaultStringValue("[N/A]")
    String unAvailablePropertyLabel();

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

    @DefaultStringValue("This feature is not implemented in this version.")
    String featureNotImplementedMessage();

    @DefaultStringValue("This feature is not implemented but available in UserPortal for users assigned with PowerUser role.")
    String featureNotImplementedButAvailInUserPortalMessage();

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

    @DefaultStringValue("Policy:")
    String clusterPolicyPolicyLabel();

    @DefaultStringValue("Name")
    String makeTemplatePopupNameLabel();

    @DefaultStringValue("Description")
    String makeTemplatePopupDescriptionLabel();

    @DefaultStringValue("Host Cluster")
    String makeTemplateClusterLabel();

    @DefaultStringValue("Storage Domain")
    String makeTemplateStorageDomainLabel();

    @DefaultStringValue("Make Private")
    String makeTemplateIsTemplatePrivateEditorLabel();

    @DefaultStringValue("Boot Options:")
    String runOncePopupBootOptionsLabel();

    @DefaultStringValue("Display Protocol:")
    String runOncePopupDisplayProtocolLabel();

    @DefaultStringValue("Custom Properties")
    String runOncePopupCustomPropertiesLabel();

    @DefaultStringValue("Vnc")
    String runOncePopupDisplayConsoleVncLabel();

    @DefaultStringValue("Spice")
    String runOncePopupDisplayConsoleSpiceLabel();

    @DefaultStringValue("Run Stateless")
    String runOncePopupRunAsStatelessLabel();

    @DefaultStringValue("Start in Pause Mode")
    String runOncePopupRunAndPauseLabel();

    @DefaultStringValue("Linux Boot Options:")
    String runOncePopupLinuxBootOptionsLabel();

    @DefaultStringValue("kernel path")
    String runOncePopupKernelPathLabel();

    @DefaultStringValue("initrd path")
    String runOncePopupInitrdPathLabel();

    @DefaultStringValue("kernel params")
    String runOncePopupKernelParamsLabel();

    @DefaultStringValue("Attach Floppy")
    String runOncePopupAttachFloppyLabel();

    @DefaultStringValue("Attach CD")
    String runOncePopupAttachIsoLabel();

    @DefaultStringValue("Windows Sysprep:")
    String runOncePopupWindowsSysprepLabel();

    @DefaultStringValue("Domain")
    String runOncePopupSysPrepDomainNameLabel();

    @DefaultStringValue("Alternate Credentials")
    String runOnceUseAlternateCredentialsLabel();

    @DefaultStringValue("User Name")
    String runOncePopupSysPrepUserNameLabel();

    @DefaultStringValue("Password")
    String runOncePopupSysPrepPasswordLabel();

    @DefaultStringValue("Boot Sequence:")
    String runOncePopupBootSequenceLabel();

    @DefaultStringValue("")
    String copyRightNotice();

    @DefaultStringValue("LUNs > Targets")
    String storageIscsiPopupLunToTargetsTabLabel();

    @DefaultStringValue("Targets > LUNs")
    String storageIscsiPopupTargetsToLunTabLabel();

    @DefaultStringValue("Address")
    String storageIscsiPopupAddressLabel();

    @DefaultStringValue("Port")
    String storageIscsiPopupPortLabel();

    @DefaultStringValue("User Authentication:")
    String storageIscsiPopupUserAuthLabel();

    @DefaultStringValue("CHAP username")
    String storageIscsiPopupChapUserLabel();

    @DefaultStringValue("CHAP password")
    String storageIscsiPopupChapPassLabel();

    @DefaultStringValue("Discover")
    String storageIscsiPopupDiscoverButtonLabel();

    @DefaultStringValue("Discover Targets")
    String storageIscsiDiscoverTargetsLabel();

    @DefaultStringValue("Login All")
    String storageIscsiPopupLoginAllButtonLabel();

    @DefaultStringValue("Login")
    String storageIscsiPopupLoginButtonLabel();

    @DefaultStringValue("Select Storage Domain")
    String storageIscsiSelectStorageLabel();

    @DefaultStringValue("Configure")
    String configurePopupTitle();

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

    @DefaultStringValue("Destination Storage")
    String importVm_destStorage();

    @DefaultStringValue("Collapse All Snapshots")
    String importVm_collapseSnapshots();

    @DefaultStringValue("Expand All")
    String treeExpandAll();

    @DefaultStringValue("Collapse All")
    String treeCollapseAll();
}
