package org.ovirt.engine.ui.uicompat;





public interface Constants extends com.google.gwt.i18n.client.Constants {

    @DefaultStringValue("OK")
    String ok();

    @DefaultStringValue("Cancel")
    String cancel();

    @DefaultStringValue("Close")
    String close();

    @DefaultStringValue("N/A")
    String notAvailableLabel();

    @DefaultStringValue("Not Specified")
    String notSpecifiedLabel();

    // Titles
    @DefaultStringValue("")
    String empty();

    @DefaultStringValue(" ")
    String space();

    @DefaultStringValue("Error")
    String errorTitle();

    @DefaultStringValue("Configure Power Management")
    String configurePowerManagement();

    @DefaultStringValue("Create New Bond")
    String createNewBondTitle();

    @DefaultStringValue("Setup Host Networks")
    String setupHostNetworksTitle();

    @DefaultStringValue("Assign/Unassign Networks")
    String assignDetachNetworksTitle();

    @DefaultStringValue("Assign/Unassign Network")
    String assignDetachNetworkTitle();

    @DefaultStringValue("Clusters")
    String clustersTitle();

    @DefaultStringValue("New Cluster - Guide Me")
    String newClusterGuideMeTitle();

    @DefaultStringValue("Edit Cluster")
    String editClusterTitle();

    @DefaultStringValue("Remove Cluster(s)")
    String removeClusterTitle();

    @DefaultStringValue("Change Cluster Compatibility Version")
    String changeClusterCompatibilityVersionTitle();

    @DefaultStringValue("Disable CPU Thread Support")
    String disableClusterCpuThreadSupportTitle();

    @DefaultStringValue("General")
    String generalTitle();

    @DefaultStringValue("Services")
    String servicesTitle();

    @DefaultStringValue("Edit Policy")
    String editPolicyTitle();

    @DefaultStringValue("Copy to Clipboard")
    String copytoClipboardTitle();

    // Tag
    @DefaultStringValue("Root")
    String rootTag();

    @DefaultStringValue("Remove Bookmark(s)")
    String removeBookmarksTitle();

    @DefaultStringValue("Edit Bookmark")
    String editBookmarkTitle();

    @DefaultStringValue("New Bookmark")
    String newBookmarkTitle();

    @DefaultStringValue("Add Local Storage")
    String addLocalStorageTitle();

    @DefaultStringValue("New Local Domain")
    String newLocalDomainTitle();

    @DefaultStringValue("New Cluster")
    String newClusterTitle();

    @DefaultStringValue("Select Host")
    String selectHostTitle();

    @DefaultStringValue("New Host")
    String newHostTitle();

    @DefaultStringValue("Power Management Configuration")
    String powerManagementConfigurationTitle();

    @DefaultStringValue("Data Centers")
    String dataCentersTitle();

    @DefaultStringValue("New Data Center - Guide Me")
    String newDataCenterGuideMeTitle();

    @DefaultStringValue("Configure Later")
    String configureLaterTitle();

    @DefaultStringValue("New Data Center")
    String newDataCenterTitle();

    @DefaultStringValue("Edit Data Center")
    String editDataCenterTitle();

    @DefaultStringValue("Remove Data Center(s)")
    String removeDataCenterTitle();

    @DefaultStringValue("Force Remove Data Center")
    String forceRemoveDataCenterTitle();

    @DefaultStringValue("Data Center Re-Initialize")
    String dataCenterReInitializeTitle();

    @DefaultStringValue("Change Data Center Compatibility Version")
    String changeDataCenterCompatibilityVersionTitle();

    @DefaultStringValue("Logical Networks")
    String logicalNetworksTitle();

    @DefaultStringValue("Remove Logical Network(s)")
    String removeLogicalNetworkTitle();

    @DefaultStringValue("Edit Logical Network")
    String editLogicalNetworkTitle();

    @DefaultStringValue("New Logical Network")
    String newLogicalNetworkTitle();

    @DefaultStringValue("Attach/Detach Network to/from Clusters")
    String attachDetachNetworkToFromClustersTitle();

    @DefaultStringValue("Save network configuration")
    String saveNetworkConfigurationTitle();

    @DefaultStringValue("Storage")
    String storageTitle();

    @DefaultStringValue("Detach Storage")
    String detachStorageTitle();

    @DefaultStringValue("Event Notifier")
    String eventNotifierTitle();

    @DefaultStringValue("Add Event Notification")
    String addEventNotificationTitle();

    @DefaultStringValue("Users")
    String usersTitle();

    @DefaultStringValue("Assign Tags")
    String assignTagsTitle();

    @DefaultStringValue("Add Users and Groups")
    String addUsersAndGroupsTitle();

    @DefaultStringValue("Remove User(s)")
    String removeUsersTitle();

    @DefaultStringValue("Directory Groups")
    String directoryGroupsTitle();

    @DefaultStringValue("Quota")
    String quotaTitle();

    @DefaultStringValue("Volumes")
    String volumesTitle();

    @DefaultStringValue("Create Volume")
    String createVolumeTitle();

    @DefaultStringValue("Parameters")
    String parameterTitle();

    @DefaultStringValue("Bricks")
    String bricksTitle();

    @DefaultStringValue("Virtual Machines")
    String virtualMachinesTitle();

    @DefaultStringValue("Add Desktop(s) to User/AD Group")
    String addDesktopsToUserADGroupTitle();

    @DefaultStringValue("Detach Virtual Machine(s)")
    String detachVirtualMachinesTitle();

    @DefaultStringValue("Note: You chose to detach all VMs from the pool - this will remove the pool itself.")
    String detachAllVmsWarning();

    @DefaultStringValue("Permissions")
    String permissionsTitle();

    @DefaultStringValue("Disks")
    String disksTitle();

    @DefaultStringValue("Remove Permission")
    String removePermissionTitle();

    @DefaultStringValue("Remove Template Disk(s)")
    String removeTemplateDisksTitle();

    @DefaultStringValue("Copy Disk(s)")
    String copyDisksTitle();

    @DefaultStringValue("Network Interfaces")
    String networkInterfacesTitle();

    @DefaultStringValue("New Network Interface")
    String newNetworkInterfaceTitle();

    @DefaultStringValue("Edit Network Interface")
    String editNetworkInterfaceTitle();

    @DefaultStringValue("Remove Network Interface(s)")
    String removeNetworkInterfacesTitle();

    @DefaultStringValue("Copy Template")
    String copyTemplateTitle();

    @DefaultStringValue("Backup Template")
    String backupTemplateTitle();

    @DefaultStringValue("Edit Template")
    String editTemplateTitle();

    @DefaultStringValue("Remove Template(s)")
    String removeTemplatesTitle();

    @DefaultStringValue("Guest Agent is not responsive")
    String guestAgentNotResponsiveTitle();

    @DefaultStringValue("Data Center")
    String dataCenterTitle();

    @DefaultStringValue("Attach to Data Center")
    String attachToDataCenterTitle();

    @DefaultStringValue("Images")
    String imagesTitle();

    @DefaultStringValue("Template Import")
    String templateImportTitle();

    @DefaultStringValue("Remove Backed up Template(s)")
    String removeBackedUpTemplatesTitle();

    @DefaultStringValue("Templates")
    String templatesTitle();

    @DefaultStringValue("New Domain")
    String newDomainTitle();

    @DefaultStringValue("Edit Domain")
    String editDomainTitle();

    @DefaultStringValue("Import Pre-Configured Domain")
    String importPreConfiguredDomainTitle();

    @DefaultStringValue("Remove Storage(s)")
    String removeStoragesTitle();

    @DefaultStringValue("Destroy Storage Domain")
    String destroyStorageDomainTitle();

    @DefaultStringValue("VM Import")
    String vmImportTitle();

    @DefaultStringValue("Remove Backed up VM(s)")
    String removeBackedUpVMsTitle();

    @DefaultStringValue("Import Virtual Machine(s)")
    String importVirtualMachinesTitle();

    @DefaultStringValue("About oVirt Engine")
    String aboutOVirtEngineTitle();

    @DefaultStringValue("Configure")
    String ConfigureTitle();

    @DefaultStringValue("Events")
    String eventsTitle();

    @DefaultStringValue("New Quota")
    String newQuotaTitle();

    @DefaultStringValue("Edit Quota")
    String editQuotaTitle();

    @DefaultStringValue("Copy Quota")
    String cloneQuotaTitle();

    @DefaultStringValue("Remove Quota(s)")
    String removeQuotasTitle();

    @DefaultStringValue("Assign Users and Groups to Quota")
    String assignUsersAndGroupsToQuotaTitle();

    @DefaultStringValue("Remove Quota Assignment from User(s)")
    String removeQuotaAssignmentFromUsersTitle();

    @DefaultStringValue("Edit Quota")
    String defineClusterQuotaOnDataCenterTitle();

    @DefaultStringValue("Edit Quota")
    String defineStorageQuotaOnDataCenterTitle();

    @DefaultStringValue("Host Hooks")
    String hostHooksTitle();

    @DefaultStringValue("Edit Management Network")
    String editManagementNetworkTitle();

    @DefaultStringValue("InterfaceList")
    String interfaceListTitle();

    @DefaultStringValue("Confirm")
    String confirmTitle();

    @DefaultStringValue("Bond Network Interfaces")
    String bondNetworkInterfacesTitle();

    @DefaultStringValue("Detach Network Interfaces")
    String detachNetworkInterfacesTitle();

    @DefaultStringValue("Edit Management Network Interface")
    String editManagementNetworkInterfaceTitle();

    @DefaultStringValue("Install Host")
    String installHostTitle();

    @DefaultStringValue("Are you sure?")
    String areYouSureTitle();

    @DefaultStringValue("Edit Host")
    String editHostTitle();

    @DefaultStringValue("Remove Host(s)")
    String removeHostsTitle();

    @DefaultStringValue("Maintenance Host(s)")
    String maintenanceHostsTitle();

    @DefaultStringValue("Edit and Approve Host")
    String editAndApproveHostTitle();

    @DefaultStringValue("Restart Host(s)")
    String restartHostsTitle();

    @DefaultStringValue("Add Hosts")
    String addMultipleHostsTitle();

    @DefaultStringValue("Stop Host(s)")
    String stopHostsTitle();

    @DefaultStringValue("Configure Local Storage")
    String configureLocalStorageTitle();

    @DefaultStringValue("System")
    String systemTitle();

    @DefaultStringValue("Hosts")
    String hostsTitle();

    @DefaultStringValue("Networks")
    String networksTitle();

    @DefaultStringValue("VMs")
    String vmsTitle();

    @DefaultStringValue("Hard Disk")
    String hardDiskTitle();

    @DefaultStringValue("CD-ROM")
    String cdromTitle();

    @DefaultStringValue("Network (PXE)")
    String networkPXETitle();

    @DefaultStringValue("Thin")
    String thinTitle();

    @DefaultStringValue("Clone")
    String cloneTitle();

    @DefaultStringValue("Low")
    String lowTitle();

    @DefaultStringValue("Medium")
    String mediumTitle();

    @DefaultStringValue("High")
    String highTitle();

    @DefaultStringValue("Never")
    String neverTitle();

    @DefaultStringValue("Normal")
    String normalTitle();

    @DefaultStringValue("[None]")
    String noneTitle();

    @DefaultStringValue("Remove Virtual Machine")
    String removeVirtualMachineTitle();

    @DefaultStringValue("Remove Tag(s)")
    String removeTagsTitle();

    @DefaultStringValue("Edit Tag")
    String editTagTitle();

    @DefaultStringValue("New Tag")
    String newTagTitle();

    @DefaultStringValue("Roles")
    String rolesTitle();

    @DefaultStringValue("Remove Role(s)")
    String removeRolesTitle();

    @DefaultStringValue("Reset")
    String resetTitle();

    @DefaultStringValue("Role's Permissions")
    String rolesPermissionsTitle();

    @DefaultStringValue("System Permission")
    String systemPermissionTitle();

    @DefaultStringValue("Add System Permission to User")
    String addSystemPermissionToUserTitle();

    @DefaultStringValue("Remove System Permission(s)")
    String removeSystemPermissionsTitle();

    @DefaultStringValue("Add Permission to User")
    String addPermissionToUserTitle();

    @DefaultStringValue("Automatic")
    String automaticTitle();

    @DefaultStringValue("Manual")
    String manualTitle();

    @DefaultStringValue("Pools")
    String poolsTitle();

    @DefaultStringValue("New Pool")
    String newPoolTitle();

    @DefaultStringValue("Edit Pool")
    String editPoolTitle();

    @DefaultStringValue("Remove Pool(s)")
    String removePoolsTitle();

    @DefaultStringValue("Add Virtual Disk")
    String addVirtualDiskTitle();

    @DefaultStringValue("New Virtual Disk")
    String newVirtualDiskTitle();

    @DefaultStringValue("Edit Virtual Disk")
    String editVirtualDiskTitle();

    @DefaultStringValue("Remove Disk(s)")
    String removeDisksTitle();

    @DefaultStringValue("Move Disk(s)")
    String moveDisksTitle();

    @DefaultStringValue("Snapshots")
    String snapshotsTitle();

    @DefaultStringValue("Delete Snapshot")
    String deleteSnapshotTitle();

    @DefaultStringValue("Create Snapshot")
    String createSnapshotTitle();

    @DefaultStringValue("Applications")
    String applicationsTitle();

    @DefaultStringValue("Monitor")
    String monitorTitle();

    @DefaultStringValue("Sessions")
    String sessionsTitle();

    @DefaultStringValue("RDP")
    String RDPTitle();

    @DefaultStringValue("Retrieving CDs...")
    String retrievingCDsTitle();

    @DefaultStringValue("New Virtual Machine - Guide Me")
    String newVirtualMachineGuideMeTitle();

    @DefaultStringValue("Remove Virtual Machine(s)")
    String removeVirtualMachinesTitle();

    @DefaultStringValue("Move Virtual Machine")
    String moveVirtualMachineTitle();

    @DefaultStringValue("Export Virtual Machine")
    String exportVirtualMachineTitle();

    @DefaultStringValue("Export Template")
    String exportTemplateTitle();

    @DefaultStringValue("Template(s) not Found on Export Domain")
    String templatesNotFoundOnExportDomainTitle();

    @DefaultStringValue("Run Virtual Machine(s)")
    String runVirtualMachinesTitle();

    @DefaultStringValue("VNC")
    String VNCTitle();

    @DefaultStringValue("Spice")
    String spiceTitle();

    @DefaultStringValue("New Template")
    String newTemplateTitle();

    @DefaultStringValue("Migrate Virtual Machine(s)")
    String migrateVirtualMachinesTitle();

    @DefaultStringValue("Shut down Virtual Machine(s)")
    String shutdownVirtualMachinesTitle();

    @DefaultStringValue("Power Off Virtual Machine(s)")
    String stopVirtualMachinesTitle();

    @DefaultStringValue("Change CD")
    String changeCDTitle();

    @DefaultStringValue("No CDs")
    String noCDsTitle();

    @DefaultStringValue("Console Disconnected")
    String consoleDisconnectedTitle();

    @DefaultStringValue("Import Template(s)")
    String importTemplatesTitle();

    @DefaultStringValue("Clone VM from Snapshot")
    String cloneVmFromSnapshotTitle();

    // Messages
    @DefaultStringValue("There are no available Bonds")
    String thereAreNoAvailableBondsMsg();

    @DefaultStringValue("Bookmark(s):")
    String bookmarsMsg();

    @DefaultStringValue("Cluster(s)")
    String clustersMsg();

    @DefaultStringValue("You are about to change the Cluster Compatibility Version. Are you sure you want to continue?")
    String youAreAboutChangeClusterCompatibilityVersionMsg();

    @DefaultStringValue("You are about to disable CPU thread support for this cluster. Disabling this can affect the ability to run VMs with certain CPU configurations.\n\n" +
            "Please ensure there are no VMs in this cluster making use of specific CPU settings such as CPU-pinning which may be affected by this change.\n\n" +
            "Are you sure you want to continue?")
    String youAreAboutChangeClusterCpuThreadSupportMsg();

    @DefaultStringValue("Name can contain only 'A-Z', 'a-z', '0-9', '_' or '-' characters.")
    String asciiNameValidationMsg();

    @DefaultStringValue("Only alphanumeric and some special characters that conform to the standard ASCII character set are allowed.")
    String asciiOrNoneValidationMsg();

    @DefaultStringValue("Only alphanumeric and some special characters that conform to the standard ASCII character set and UTF letters are allowed.")
    String specialAsciiOrNoneValidationMsg();

    @DefaultStringValue("Name can contain only alphanumeric characters, '0-9', '_' or '-' characters.")
    String i18NNameValidationMsg();

    @DefaultStringValue("UTF characters are not allowed.")
    String nonUtfValidationMsg();

    @DefaultStringValue("You haven't configured Power Management for this Host. Are you sure you want to continue?")
    String youHavntConfigPmMsg();

    @DefaultStringValue("Name must contain alphanumeric characters or '_' (maximum length 15 characters).")
    String nameMustContainAlphanumericMaxLenMsg();

    @DefaultStringValue("Network name shouldn't start with 'bond'.")
    String networkNameStartMsg();

    @DefaultStringValue("Data Center(s)")
    String dataCentersMsg();

    @DefaultStringValue("There are no compatible Storage Domains to attach to this Data Center. Please add new Storage from the Storage tab.")
    String thereAreNoCompatibleStorageDomainsAttachThisDcMsg();

    @DefaultStringValue("You are about to change the Data Center Compatibility Version. Are you sure you want to continue?")
    String youAreAboutChangeDcCompatibilityVersionMsg();

    @DefaultStringValue("Logical Network(s)")
    String logicalNetworksMsg();

    @DefaultStringValue("Cannot edit Management Network when there are Clusters in the Data Center")
    String cannotDetachManagementNetworkFromClustersMsg();

    @DefaultStringValue("You are about to attach the network to all the selected clusters and to detach the network from all the unselected clusters.\n\nAre you sure you want to continue?")
    String youAreAboutToAttachDetachNetworkToFromTheClustersMsg();

    @DefaultStringValue("Changes done to the Networking configuration are temporary until explicitly saved.\n\nAre you sure you want to make the changes persistent?")
    String areYouSureYouWantToMakeTheChangesPersistentMsg();

    @DefaultStringValue("Are you sure you want to Detach the following storage(s)?")
    String areYouSureYouWantDetachFollowingStoragesMsg();

    @DefaultStringValue("User(s)")
    String usersMsg();

    @DefaultStringValue("Are you sure you want to Detach from the user the following Virtual Machine(s)")
    String areYouSureYouWantDetachFromUserFollowingVmsMsg();

    @DefaultStringValue("Permission")
    String permissionMsg();

    @DefaultStringValue("No Storage Domain is available - check Storage Domains and Hosts status.")
    String noSDAvailableMsg();

    @DefaultStringValue("Disks already exist on all available Storage Domains.")
    String disksAlreadyExistMsg();

    @DefaultStringValue("Templates reside on several Data Centers. Make sure the exported Templates reside on the same Data Center.")
    String templatesResideOnSeveralDcsMakeSureExportedTemplatesResideOnSameDcMsg();

    @DefaultStringValue("There is no Export Domain to export the Template into. Please attach an Export Domain to the Template's Data Center.")
    String thereIsNoExportDomainToExportTheTemplateIntoMsg();

    @DefaultStringValue("Template(s)")
    String templatesMsg();

    @DefaultStringValue("There are No Data Centers to which the Storage Domain can be attached")
    String thereAreNoDataCenterStorageDomainAttachedMsg();

    @DefaultStringValue("Are you sure you want to Detach storage from the following Data Center(s)?")
    String areYouSureYouWantDetachStorageFromDcsMsg();

    @DefaultStringValue("There is no Data Storage Domain to import the Template into. Please attach a Data Storage Domain to the Template's Data Center.")
    String thereIsNoDataStorageDomainToImportTemplateIntoMsg();

    @DefaultStringValue("The Export Domain is inactive. Data can be retrieved only when the Domain is activated")
    String theExportDomainIsInactiveMsg();

    @DefaultStringValue("Export Domain is not attached to any Data Center. Data can be retrieved only when the Domain is attached to a Data Center and is active")
    String ExportDomainIsNotAttachedToAnyDcMsg();

    @DefaultStringValue("Are you sure you want to Remove the Storage Domain?")
    String areYouSureYouWantToRemoveTheStorageDomainMsg();

    @DefaultStringValue("VM(s)")
    String vmsMsg();

    @DefaultStringValue("Quota")
    String quotasMsg();

    @DefaultStringValue("Assignment(s)")
    String assignmentsMsg();

    @DefaultStringValue("There are no networks available. Please add additional networks.")
    String thereAreNoNetworksAvailablePleaseAddAdditionalNetworksMsg();

    @DefaultStringValue("There are no ISO versions that are compatible with the Host's current version.")
    String thereAreNoISOversionsVompatibleWithHostCurrentVerMsg();

    @DefaultStringValue("Host(s)")
    String hostsMsg();

    @DefaultStringValue("Are you sure you want to place the following host(s) into maintenance mode?")
    String areYouSureYouWantToPlaceFollowingHostsIntoMaintenanceModeMsg();

    @DefaultStringValue("Are you sure you want to Restart the following Host(s)?")
    String areYouSureYouWantToRestartTheFollowingHostsMsg();

    @DefaultStringValue("Are you sure you want to Stop the following Host(s)?")
    String areYouSureYouWantToStopTheFollowingHostsMsg();

    @DefaultStringValue("Host doesn't support Local Storage configuration")
    String hostDoesntSupportLocalStorageConfigurationMsg();

    @DefaultStringValue("Testing in progress. It will take a few seconds. Please wait...")
    String testingInProgressItWillTakeFewSecondsPleaseWaitMsg();

    @DefaultStringValue("Virtual Machine")
    String virtualMachineMsg();

    @DefaultStringValue("Name must be up to 126 characters and start with any word character.")
    String nameMustBeUpToAndStartWithMsg();

    @DefaultStringValue("Could not connect to the Engine Service, please try to refresh the page. If the problem persists contact your System Administrator.")
    String couldNotConnectToOvirtEngineServiceMsg();

    @DefaultStringValue("Are you sure you want to detach selected Virtual Machine(s)?")
    String areYouSurYouWantToDetachSelectedVirtualMachinesMsg();

    @DefaultStringValue("Pool(s)")
    String poolsMsg();

    @DefaultStringValue("Disk(s)")
    String disksMsg();

    @DefaultStringValue("Snapshot cannot be created since the VM has no Virtual Disks")
    String snapshotCannotBeCreatedSinceTheVMHasNoDisksMsg();

    @DefaultStringValue("At least one disk must be marked.")
    String atLeastOneDiskMustBeMarkedMsg();

    @DefaultStringValue("Network Interface(s)")
    String networkInterfacesMsg();

    @DefaultStringValue("Name must contain alphanumeric characters only.")
    String nameMustContainAlphanumericCharactersOnlyMsg();

    @DefaultStringValue("Virtual Machine(s)")
    String virtualMachinesMsg();

    @DefaultStringValue("Virtual Machines reside on several Data Centers. Make sure the exported Virtual Machines reside on the same Data Center.")
    String vmsResideOnSeveralDCsMakeSureTheExportedVMResideOnSameDcMsg();

    @DefaultStringValue("There is no Export Domain to Backup the Virtual Machine into. Attach an Export Domain to the Virtual Machine(s) Data Center.")
    String thereIsNoExportDomainBackupVmAttachExportDomainToVmsDcMsg();

    @DefaultStringValue("The relevant Export Domain is not active. Please activate it.")
    String theRelevantExportDomainIsNotActivePleaseActivateItMsg();

    @DefaultStringValue("Are you sure you want to Shut down the following Virtual Machines?")
    String areYouSureYouWantToShutDownTheFollowingVirtualMachinesMsg();

    @DefaultStringValue("Are you sure you want to Power Off the following Virtual Machines?")
    String areYouSureYouWantToStopTheFollowingVirtualMachinesMsg();

    @DefaultStringValue("This field must contain an IP address in format xxx.xxx.xxx.xxx")
    String thisFieldMustContainIPaddressInFormatMsg();

    @DefaultStringValue("This field can't contain spaces.")
    String thisFieldCantConatainSpacesMsg();

    @DefaultStringValue("Invalid MAC address")
    String invalidMacAddressMsg();

    @DefaultStringValue("Role(s):")
    String rolesMsg();

    @DefaultStringValue("System Permission(s):")
    String systemPermissionsMsg();

    @DefaultStringValue("Note: Local Storage is already configured for this Host. The Host belongs to")
    String noteLocalStorageAlreadyConfiguredForThisHostMsg();

    @DefaultStringValue("with local Storage Domain. If OK is clicked - this Host will be moved to a new Data Center, and a new Local Storage Domain will be created. Hit Cancel to abort the operation.")
    String withLocalStorageDomainMsg();

    @DefaultStringValue("Mount path is illegal, please use [/path] convention.")
    String localfsMountPashIsIllegalMsg();

    @DefaultStringValue("NFS mount path is illegal, please use [IP:/path or FQDN:/path] convention.")
    String nfsMountPashIsIllegalMsg();

    @DefaultStringValue("No new devices were found. This may be due to either: incorrect multipath configuration on the Host or wrong address of the iscsi target or a failure to authenticate on the target device. Please consult your Storage Administrator.")
    String noNewDevicesWereFoundMsg();

    @DefaultStringValue("Tag(s)")
    String tagsMsg();

    @DefaultStringValue("The system could not find available target Storage Domain.\nPossible reasons:\n  - No active Storage Domain available\n  - The Template that the VM is based on does not exist on active Storage Domain")
    String theSystemCouldNotFindAvailableTargetStorageDomainMsg();

    @DefaultStringValue("Field value should follow: <parameter=value;parameter=value;...>")
    String fieldValueShouldFollowMsg();

    @DefaultStringValue("Note that all snapshots will be collapsed due to different storage types")
    String noteThatAllSnapshotsCollapsedDueDifferentStorageTypesMsg();

    @DefaultStringValue("Use a separate import operation for the marked VMs or\nApply \"Collapse Snapshots\" for all VMs")
    String useSeparateImportOperationForMarkedVMsMsg();

    @DefaultStringValue("Template Disk(s)")
    String templateDisksMsg();

    @DefaultStringValue("OS Version -")
    String osVersionAbout();

    @DefaultStringValue("VDSM Version -")
    String VDSMVersionAbout();

    @DefaultStringValue("oVirt Engine Hypervisor Hosts:")
    String oVirtEnterpriseVirtualizationEngineHypervisorHostsAbout();

    @DefaultStringValue("[No Hosts]")
    String noHostsAbout();

    @DefaultStringValue("oVirt Engine for Servers and Desktops:")
    String oVirtEngineForServersAndDesktopsAbout();

    @DefaultStringValue("The field contains special characters. Only numbers, letters, '-' and '_' are allowed.")
    String theFieldContainsSpecialCharactersInvalidReason();

    @DefaultStringValue("Configure Host")
    String configureHostClusterGuide();

    @DefaultStringValue("Add another Host")
    String addAnotherHostClusterGuide();

    @DefaultStringValue("Select Hosts")
    String selectHostsClusterGuide();

    @DefaultStringValue("The Cluster isn't attached to a Data Center")
    String theClusterIsntAttachedToADcClusterGuide();

    @DefaultStringValue("This Cluster belongs to a Local Data Center which already contain a Host")
    String thisClusterBelongsToALocalDcWhichAlreadyContainHostClusterGuide();

    @DefaultStringValue("Name must be unique.")
    String nameMustBeUniqueInvalidReason();

    @DefaultStringValue("You must approve the action by clicking on this checkbox.")
    String youMustApproveTheActionByClickingOnThisCheckboxInvalidReason();

    @DefaultStringValue("Configure Cluster")
    String dataCenterConfigureClustersAction();

    @DefaultStringValue("Add another Cluster")
    String dataCenterAddAnotherClusterAction();

    @DefaultStringValue("Configure Host")
    String dataCenterConfigureHostsAction();

    @DefaultStringValue("Select Hosts")
    String dataCenterSelectHostsAction();

    @DefaultStringValue("Configure Storage")
    String dataCenterConfigureStorageAction();

    @DefaultStringValue("Add more Storage")
    String dataCenterAddMoreStorageAction();

    @DefaultStringValue("Attach Storage")
    String dataCenterAttachStorageAction();

    @DefaultStringValue("Attach more Storage")
    String dataCenterAttachMoreStorageAction();

    @DefaultStringValue("Configure ISO Library")
    String dataCenterConfigureISOLibraryAction();

    @DefaultStringValue("Attach ISO Library")
    String dataCenterAttachISOLibraryAction();

    @DefaultStringValue("There should be at least one active Host in the Data Center")
    String noUpHostReason();

    @DefaultStringValue("Cannot create an ISO domain in a non-active Data Center")
    String noDataDomainAttachedReason();

    @DefaultStringValue("Local Data Center already contains a Host")
    String localDataCenterAlreadyContainsAHostDcGuide();

    @DefaultStringValue("Data Center was already initialized")
    String dataCenterWasAlreadyInitializedDcGuide();

    @DefaultStringValue("New ISO Library")
    String newISOLibraryTitle();

    @DefaultStringValue("New Storage")
    String newStorageTitle();

    @DefaultStringValue("Attach ISO Library")
    String attachISOLibraryTitle();

    @DefaultStringValue("Attach Storage")
    String attachStorageTitle();

    @DefaultStringValue("Attach Export Domain")
    String attachExportDomainTitle();

    @DefaultStringValue("Invalid E-Mail address")
    String invalidEmailAddressInvalidReason();

    @DefaultStringValue("Address is not a valid host name or IP address.")
    String addressIsNotValidHostNameOrIpAddressInvalidReason();

    @DefaultStringValue("Switch to maintenance mode to enable Upgrade.")
    String switchToMaintenanceModeToEnableUpgradeReason();

    @DefaultStringValue("Configuring Local Storage...")
    String configuringLocalStorageHost();

    @DefaultStringValue("Configuring local Storage is permitted only to Administrators with System-level permissions")
    String configuringLocalStoragePermittedOnlyAdministratorsWithSystemLevelPermissionsReason();

    @DefaultStringValue("Test Failed (unknown error).")
    String testFailedUnknownErrorMsg();

    @DefaultStringValue("Error while retrieving list of domains. Please consult your Storage Administrator.")
    String errorWhileRetrievingListOfDomains();

    @DefaultStringValue("Please select a Storage Domain to import")
    String pleaseSelectStorageDomainToImportImportSanStorage();

    @DefaultStringValue("Error while retrieving list of domains. Please consult your Storage Administrator.")
    String errorWhileRetrievingListOfDomainsImportSanStorage();

    @DefaultStringValue("Value doesn't not match pattern: key=value,key=value...")
    String valueDoesntNotMatchPatternKeyValueKeyValueInvalidReason();

    @DefaultStringValue("Value doesn't not match pattern: key=value,key,key=value...")
    String valueDoesntNotNatchPatternKeyValueKeyKeyValueInvalidReason();

    @DefaultStringValue("Data Center is not accessible.")
    String dataCenterIsNotAccessibleMsg();

    @DefaultStringValue("This field can't be empty.")
    String thisFieldCantBeEmptyInvalidReason();

    @DefaultStringValue("Quota enforcement activated. Quota must be defined for the selected storage domain")
    String quotaMustBeSelectedInvalidReason();

    @DefaultStringValue("New Role")
    String newRoleTitle();

    @DefaultStringValue("Edit Role")
    String editRoleTitle();

    @DefaultStringValue("Copy Role")
    String copyRoleTitle();

    @DefaultStringValue("This field must contain integer number")
    String thisFieldMustContainIntegerNumberInvalidReason();

    // Role tree tooltip
    @DefaultStringValue("Allow to Add/Remove Users from the System")
    String allowToAddRemoveUsersFromTheSystemRoleTreeTooltip();

    @DefaultStringValue("Allow to add/remove permissions for Users on objects in the system")
    String allowToAddRemovePermissionsForUsersOnObjectsInTheSystemRoleTreeTooltip();

    @DefaultStringValue("Allow to login to the system")
    String allowToLoginToTheSystemRoleTreeTooltip();

    @DefaultStringValue("Allow to define/configure roles in the System")
    String allowToDefineConfigureRolesInTheSystemRoleTreeTooltip();

    @DefaultStringValue("Allow to get or set System Configuration")
    String allowToGetOrSetSystemConfigurationRoleTreeTooltip();

    @DefaultStringValue("Allow to create Data Center")
    String allowToCreateDataCenterRoleTreeTooltip();

    @DefaultStringValue("Allow to remove Data Center")
    String allowToRemoveDataCenterRoleTreeTooltip();

    @DefaultStringValue("Allow to modify Data Center properties")
    String allowToModifyDataCenterPropertiesRoleTreeTooltip();

    @DefaultStringValue("Allow to edit Logical Network's properties")
    String allowToEditLogicalNetworkRoleTreeTooltip();

    @DefaultStringValue("Allow to create Logical Network per Data Center")
    String allowToCreateLogicalNetworkPerDataCenterRoleTreeTooltip();

    @DefaultStringValue("Allow to delete Logical Network")
    String allowToDeleteLogicalNetworkRoleTreeTooltip();

    @DefaultStringValue("Allow to create Storage Domain")
    String allowToCreateStorageDomainRoleTreeTooltip();

    @DefaultStringValue("Allow to delete Storage Domain")
    String allowToDeleteStorageDomainRoleTreeTooltip();

    @DefaultStringValue("Allow to modify Storage Domain properties")
    String allowToModifyStorageDomainPropertiesRoleTreeTooltip();

    @DefaultStringValue("Allow to change Storage Domain status:  maintenance/activate; attach/detach")
    String allowToChangeStorageDomainStatusRoleTreeTooltip();

    @DefaultStringValue("Allow to create new Cluster")
    String allowToCreateNewClusterRoleTreeTooltip();

    @DefaultStringValue("Allow to remove Cluster")
    String allowToRemoveClusterRoleTreeTooltip();

    @DefaultStringValue("Allow to Edit Cluster properties")
    String allowToEditClusterPropertiesRoleTreeTooltip();

    @DefaultStringValue("Allow to add/remove Logical Networks for the Cluster (from the list of Networks defined by the Data Center)")
    String allowToAddRemoveLogicalNetworksForTheClusterRoleTreeTooltip();

    @DefaultStringValue("Allow to edit Logical Networks properties within the Cluster (e.g. non-required or display network)")
    String allowToEditLogicalNetworksForTheClusterRoleTreeTooltip();

    @DefaultStringValue("Allow to add new Host to the Cluster")
    String allowToAddNewHostToTheClusterRoleTreeTooltip();

    @DefaultStringValue("Allow to remove existing Host from the Cluster")
    String allowToRemoveExistingHostFromTheClusterRoleTreeTooltip();

    @DefaultStringValue("Allow to Edit Host properties; upgrade/install")
    String allowToEditHostPropertiesRoleTreeTooltip();

    @DefaultStringValue("Allow to change Host status: activate/maintenance")
    String allowToChangeHostStatusRoleTreeTooltip();

    @DefaultStringValue("Allow to configure Host's Network physical interfaces (Nics)")
    String allowToConfigureHostsNetworkPhysicalInterfacesRoleTreeTooltip();

    @DefaultStringValue("Allow to change Template properties")
    String allowToChangeTemplatePropertiesRoleTreeTooltip();

    @DefaultStringValue("Allow to configure Temlate Network")
    String allowToConfigureTemlateNetworkRoleTreeTooltip();

    @DefaultStringValue("note: Permissions containig these operations should be associated with Storage Domain Object (or above)")
    String notePermissionsContainigTheseOperationsShuoldAssociatSdOrAboveRoleTreeTooltip();

    @DefaultStringValue("Allow to create new Template")
    String allowToCreateNewTemplateRoleTreeTooltip();

    @DefaultStringValue("Allow to remove existing Template")
    String allowToRemoveExistingTemplateRoleTreeTooltip();

    @DefaultStringValue("Allow import/export operations")
    String allowImportExportOperationsRoleTreeTooltip();

    @DefaultStringValue("Allow to copy Template between Storage Domains")
    String allowToCopyTemplateBetweenStorageDomainsRoleTreeTooltip();

    @DefaultStringValue("Allow basic VM operations - Run/Stop/Pause")
    String allowBasicVmOperationsRoleTreeTooltip();

    @DefaultStringValue("Allow to attach CD to the VM")
    String allowToAttachCdToTheVmRoleTreeTooltip();

    @DefaultStringValue("Allow viewing the VM Console Screen")
    String allowViewingTheVmConsoleScreenRoleTreeTooltip();

    @DefaultStringValue("Allow setting port mirroring to VMs networks")
    String allowVmNetworkPortMirroringRoleTreeTooltip();

    @DefaultStringValue("Allow Change VM properties")
    String allowChangeVmPropertiesRoleTreeTooltip();

    @DefaultStringValue("Allow changing VM custom properties")
    String allowToChangeVmCustomPropertiesRoleTreeTooltip();

    @DefaultStringValue("Allow to override the currently opened remote console session")
    String allowReconnectToVmRoleTreeTooltip();

    @DefaultStringValue("Allow to create new Vms")
    String allowToCreateNewVmsRoleTreeTooltip();

    @DefaultStringValue("Allow to remove Vms from the system")
    String allowToRemoveVmsFromTheSystemRoleTreeTooltip();

    @DefaultStringValue("Allow to configure VMs network")
    String allowToConfigureVMsNetworkRoleTreeTooltip();

    @DefaultStringValue("Allow to add/remove disk to the VM")
    String allowToAddRemoveDiskToTheVmRoleTreeTooltip();

    @DefaultStringValue("Allow to create/delete snapshots of the VM")
    String allowToCreateDeleteSnapshotsOfTheVmRoleTreeTooltip();

    @DefaultStringValue("note: Permissions containing these operations should be associated with Data Center Object (or equivalent)")
    String notePermissionsContainigTheseOperationsShuoldAssociatDcOrEqualRoleTreeTooltip();

    @DefaultStringValue("note: Permissions containing these operations should be associated with Network Object (or equivalent)")
    String notePermissionsContainingTheseOperationsShouldAssociateNetworkOrEqualRoleTreeTooltip();

    @DefaultStringValue("Allow to move VM image to another Storage Domain")
    String allowToMoveVmImageToAnotherStorageDomainRoleTreeTooltip();

    @DefaultStringValue("Allow migrating VM between Hosts in a Cluster")
    String allowMigratingVmBetweenHostsInClusterRoleTreeTooltip();

    @DefaultStringValue("Allow to Run/Pause/Stop a VM from VM-Pool")
    String allowToRunPauseStopVmFromVmPoolRoleTreeTooltip();

    @DefaultStringValue("Allow to create VM-Pool")
    String allowToCreateVmPoolRoleTreeTooltip();

    @DefaultStringValue("Allow to delete VM-Pool")
    String allowToDeleteVmPoolRoleTreeTooltip();

    @DefaultStringValue("Allow to change properties of the VM-Pool")
    String allowToChangePropertiesOfTheVmPoolRoleTreeTooltip();

    @DefaultStringValue("Disk")
    String diskRoleTree();

    @DefaultStringValue("note: Permissions containing these operations should be associated with Disk or Storage Domain Object (or above)")
    String notePermissionsContainingOperationsRoleTreeTooltip();

    @DefaultStringValue("Allow to create Disk")
    String allowToCreateDiskRoleTreeTooltip();

    @DefaultStringValue("Allow to delete Disk")
    String allowToDeleteDiskRoleTreeTooltip();

    @DefaultStringValue("Allow to move Disk to another Storage Domain")
    String allowToMoveDiskToAnotherStorageDomainRoleTreeTooltip();

    @DefaultStringValue("Allow to attach Disk to a VM")
    String allowToAttachDiskToVmRoleTreeTooltip();

    @DefaultStringValue("Allow to change properties of the Disk")
    String allowToChangePropertiesOfTheDiskRoleTreeTooltip();

    @DefaultStringValue("No")
    String noAlerts();

    @DefaultStringValue("Storage Domain must be specified.")
    String storageDomainMustBeSpecifiedInvalidReason();

    @DefaultStringValue("The field must contain a time value")
    String theFieldMustContainTimeValueInvalidReason();

    @DefaultStringValue("Note: This action will remove the Template permanently from all Storage Domains.")
    String noteThisActionWillRemoveTemplatePermanentlyFromStorageDomains();

    @DefaultStringValue("Blank Template cannot be edited")
    String blankTemplateCannotBeEdited();

    @DefaultStringValue("Blank Template cannot be removed")
    String blankTemplateCannotBeRemoved();

    @DefaultStringValue("Blank Template cannot be exported")
    String blankTemplateCannotBeExported();

    @DefaultStringValue("Blank Template cannot be copied")
    String blankTemplateCannotBeCopied();

    @DefaultStringValue("Note: The deleted items might still appear on the sub-tab, since the remove operation might be long. Use the Refresh button, to get the updated status.")
    String noteTheDeletedItemsMightStillAppearOntheSubTab();

    @DefaultStringValue("NOTE:\n  - Removing the tag will also remove all of its descendants.\n  - Tag and descendants will be erased from all objects that are attached to them.")
    String noteRemovingTheTagWillAlsoRemoveAllItsDescendants();

    @DefaultStringValue("Failed to retrieve existing storage domain information.")
    String failedToRetrieveExistingStorageDomainInformationMsg();

    @DefaultStringValue("There is no storage domain under the specified path. Check event log for more details.")
    String thereIsNoStorageDomainUnderTheSpecifiedPathMsg();

    @DefaultStringValue("Importing Storage Domain...")
    String importingStorageDomainProgress();

    @DefaultStringValue("Play")
    String playSpiceConsole();

    @DefaultStringValue("Suspend")
    String suspendSpiceConsole();

    @DefaultStringValue("Stop")
    String stopSpiceConsole();

    @DefaultStringValue("No LUNs selected. Please select LUNs.")
    String noLUNsSelectedInvalidReason();

    @DefaultStringValue("Could not retrieve LUNs, please check your storage.")
    String couldNotRetrieveLUNsLunsFailure();

    @DefaultStringValue("a kernel parameter argument")
    String kernelInvalid();

    @DefaultStringValue("or")
    String or();

    @DefaultStringValue("an initrd path")
    String inetdInvalid();

    @DefaultStringValue("Allocation can be modified only when importing a single VM")
    String allocCanBeModifiedOnlyWhenImportSingleVm();

    @DefaultStringValue("Configure Network Interfaces")
    String vmConfigureNetworkInterfacesAction();

    @DefaultStringValue("Add another Network Interface")
    String vmAddAnotherNetworkInterfaceAction();

    @DefaultStringValue("Configure Virtual Disks")
    String vmConfigureVirtualDisksAction();

    @DefaultStringValue("Add another Virtual Disk")
    String vmAddAnotherVirtualDiskAction();

    @DefaultStringValue("There is no active Storage Domain to create the Disk in. Please activate a Storage Domain.")
    String thereIsNoActiveStorageDomainCreateDiskInMsg();

    @DefaultStringValue("Error in retrieving the relevant Storage Domain.")
    String errorRetrievingRelevantStorageDomainMsg();

    @DefaultStringValue("Server")
    String serverVmType();

    @DefaultStringValue("Desktop")
    String desktopVmType();

    @DefaultStringValue("Could not read templates from Export Domain")
    String couldNotReadTemplatesFromExportDomainMsg();

    @DefaultStringValue("The following templates are missing on the target Export Domain:")
    String theFollowingTemplatesAreMissingOnTargetExportDomainMsg();

    @DefaultStringValue("There are no active Data-Centers in the system.")
    String noActiveDataCenters();

    @DefaultStringValue("There are no active Storage Domains in the relevant Data-Center.")
    String noActiveStorageDomainsInDC();

    @DefaultStringValue("Host name can't contain blanks or special characters, must be at least one character long, and contain 'a-z', '0-9', '_' or '.' characters.")
    String hostNameValidationMsg();

    // Role tree

    @DefaultStringValue("root")
    String rootRoleTree();

    @DefaultStringValue("System")
    String systemRoleTree();

    @DefaultStringValue("Configure System")
    String configureSystemRoleTree();

    @DefaultStringValue("Data Center")
    String dataCenterRoleTree();

    @DefaultStringValue("Configure Data Center")
    String configureDataCenterRoleTree();

    @DefaultStringValue("Network")
    String networkRoleTree();

    @DefaultStringValue("Configure Network")
    String configureNetworkRoleTree();

    @DefaultStringValue("Storage Domain")
    String storageDomainRoleTree();

    @DefaultStringValue("Configure Storage Domain")
    String configureStorageDomainRoleTree();

    @DefaultStringValue("Cluster")
    String clusterRoleTree();

    @DefaultStringValue("Configure Cluster")
    String configureClusterRoleTree();

    @DefaultStringValue("Host")
    String hostRoleTree();

    @DefaultStringValue("Gluster")
    String glusterRoleTree();

    @DefaultStringValue("Configure Volumes")
    String configureVolumesRoleTree();

    @DefaultStringValue("Allow to create Gluster Volumes")
    String allowToCreateGlusterVolumesRoleTree();

    @DefaultStringValue("Allow to manipulate Gluster Volumes")
    String allowToManipulateGlusterVolumesRoleTree();

    @DefaultStringValue("Configure Host")
    String configureHostRoleTree();

    @DefaultStringValue("Template")
    String templateRoleTree();

    @DefaultStringValue("Basic Operations")
    String basicOperationsRoleTree();

    @DefaultStringValue("Provisioning Operations")
    String provisioningOperationsRoleTree();

    @DefaultStringValue("VM")
    String vmRoleTree();

    @DefaultStringValue("Administration Operations")
    String administrationOperationsRoleTree();

    @DefaultStringValue("VM Pool")
    String vmPoolRoleTree();

    // Error
    @DefaultStringValue("This Network does not exist in the Cluster")
    String thisNetworkDoesNotExistInTheClusterErr();

    @DefaultStringValue("Subnet Mask is not Valid")
    String subnetMaskIsNotValid();

    // Fronted
    @DefaultStringValue("A Request to the Server failed with the following Status Code")
    String requestToServerFailedWithCode();

    @DefaultStringValue("A Request to the Server failed")
    String requestToServerFailed();

    // Volume
    @DefaultStringValue("Add Bricks")
    String addBricksVolume();

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

    @DefaultStringValue("Add")
    String AddVolume();

    @DefaultStringValue("Edit")
    String editVolume();

    @DefaultStringValue("Reset")
    String resetVolume();

    @DefaultStringValue("Reset All")
    String resetAllVolume();

    @DefaultStringValue("Add Option")
    String addOptionVolume();

    @DefaultStringValue("Edit Option")
    String editOptionVolume();

    @DefaultStringValue("Error in fetching volume option list, please try again.")
    String errorInFetchingVolumeOptionList();

    @DefaultStringValue("Reset Option")
    String resetOptionVolumeTitle();

    @DefaultStringValue("Are you sure want to reset the following option?")
    String resetOptionVolumeMsg();

    @DefaultStringValue("Reset All Options")
    String resetAllOptionsTitle();

    @DefaultStringValue("Are you sure want to reset all the options?")
    String resetAllOptionsMsg();

    @DefaultStringValue("Stop Volume")
    String confirmStopVolume();

    @DefaultStringValue("Are you sure want to stop the following Volume(s)?")
    String stopVolumeMessage();

    @DefaultStringValue("NOTE:\n -Stopping volume will make its data inaccessible.")
    String stopVolumeWarning();

    @DefaultStringValue("Remove Volume")
    String removeVolumesTitle();

    @DefaultStringValue("Volume(s)?")
    String removeVolumesMessage();

    @DefaultStringValue("NOTE:\n -Removing volume will erase all information about the volume.")
    String removeVolumesWarning();

    @DefaultStringValue("Remove Bricks")
    String removeBricksTitle();

    @DefaultStringValue("Replace Brick")
    String replaceBrickTitle();

    @DefaultStringValue("Brick Details")
    String advancedDetailsBrickTitle();

    @DefaultStringValue("Error in fetching the brick details, please try again.")
    String errorInFetchingBrickAdvancedDetails();

    @DefaultStringValue("Are you sure want to remove the following Brick(s)?")
    String removeBricksMessage();

    @DefaultStringValue("Brick with the same details already exist")
    String duplicateBrickMsg();

    @DefaultStringValue("Invalid Brick Directory")
    String invalidBrickDirectoryMsg();

    @DefaultStringValue("Brick Directory should start with '/'")
    String invalidBrickDirectoryStartWithSlashMsg();

    @DefaultStringValue("Brick Directory should not contain any space")
    String invalidBrickDirectoryContainsSpaceMsg();

    @DefaultStringValue("Brick Directory should be at least 2 characters")
    String invalidBrickDirectoryAtleastTwoCharacterseMsg();

    @DefaultStringValue("Brick Directory field is mandatory")
    String emptyBrickDirectoryMsg();

    @DefaultStringValue("Server field is mandatory")
    String emptyServerBrickMsg();

    @DefaultStringValue("At least 1 brick should be present")
    String distriputedVolumeAddBricksMsg();

    @DefaultStringValue("Number of bricks should be equal to Replica Count")
    String replicateVolumeAddBricksMsg();

    @DefaultStringValue("Number of bricks should be a mutiple of Replica Count")
    String distriputedReplicateVolumeAddBricksMsg();

    @DefaultStringValue("Number of bricks should be equal to Stripe Count")
    String stripeVolumeAddBricksMsg();

    @DefaultStringValue("Number of bricks should be a mutiple of Stripe Count")
    String distriputedStripeVolumeAddBricksMsg();

    @DefaultStringValue("Bricks cannot be empty")
    String emptyAddBricksMsg();

    @DefaultStringValue("Cannot choose Volume's Data Center in tree context")
    String cannotChooseVolumesDataCenterinTreeContect();

    @DefaultStringValue("Cannot choose Volume's Cluster in tree context")
    String cannotChooseVolumesClusterinTreeContect();

    @DefaultStringValue("VMs already exist")
    String vmAlreadyExistsMsg();

    @DefaultStringValue("Templates already exist")
    String templateAlreadyExistsMsg();

    @DefaultStringValue("No VMs exist")
    String vmNoExistsMsg();

    @DefaultStringValue("No Templates exist")
    String templateNoExistsMsg();

    @DefaultStringValue("DefaultQuota")
    String defaultQuotaPrefix();

    @DefaultStringValue("Quota must contain limitations")
    String quotaIsEmptyValidation();

    @DefaultStringValue("At least one service should be selected")
    String clusterServiceValidationMsg();

    @DefaultStringValue("Please select a key...")
    String pleaseSelectKey();

    @DefaultStringValue("No available keys")
    String noKeyAvailable();

    @DefaultStringValue("At least one transport type should be selected.")
    String volumeTransportTypesValidationMsg();

    @DefaultStringValue("USB")
    String usb();

    @DefaultStringValue("USB Devices,No USB devices,Client's SPICE USB Redirector is not installed")
    String usbDevicesNoUsbdevicesClientSpiceUsbRedirectorNotInstalled();

    @DefaultStringValue("Change CD")
    String changeCd();

    @DefaultStringValue("No CDs")
    String noCds();

    @DefaultStringValue("Send")
    String send();

    @DefaultStringValue("Toggle Full Screen")
    String toggleFullScreen();

    @DefaultStringValue("Special Keys")
    String specialKeys();

    @DefaultStringValue("Auto Negotiate")
    String nfsVersionAutoNegotiate();

    @DefaultStringValue("V3 (default)")
    String nfsVersion3();

    @DefaultStringValue("V4")
    String nfsVersion4();

    @DefaultStringValue("DirectLUN disk is not supported by the Data Center Compatibility Version")
    String directLUNDiskNotSupported();

    @DefaultStringValue("Shareable Disk is not supported by the Data Center Compatibility Version")
    String shareableDiskNotSupported();

    @DefaultStringValue("Shareable Disk is not supported by the selected configuration")
    String shareableDiskNotSupportedByConfiguration();

    @DefaultStringValue("Note: Moving the disk(s) while the VM is running")
    String liveStorageMigrationWarning();

    @DefaultStringValue("Cannot remove more than one brick from a Replicate volume at a time")
    String cannotRemoveBricksReplicateVolume();

    @DefaultStringValue("Incorrect bricks selected for the removel in Distributed Replicate volume. Either all the bricks should be from the same sub volume or one brick each for every sub volume!")
    String cannotRemoveBricksDistributedReplicateVolume();

    @DefaultStringValue("Incorrect bricks selected for the removel in Distributed Stripe volume. Selected bricks should be from the same sub volume!")
    String cannotRemoveBricksDistributedStripeVolume();

    @DefaultStringValue("Replica count can be increased by only one")
    String addBricksReplicaCountIncreaseValidationMsg();

    @DefaultStringValue("Stripe count can be increased by only one")
    String addBricksStripeCountIncreaseValidationMsg();

    @DefaultStringValue("Console Options")
    String consoleOptions();

    @DefaultStringValue("Can't save configuration - Management Network must be attached")
    String mgmtNotAttachedToolTip();

    @DefaultStringValue("LUN is not usable")
    String lunUnusable();

    @DefaultStringValue("Are you sure?")
    String forceStorageDomainCreation();

    @DefaultStringValue("The following LUNs are already in use:")
    String lunsAlreadyInUse();

    @DefaultStringValue("The following LUNs are already part of Storage Domains:")
    String lunsAlreadyPartOfSD();

    @DefaultStringValue("Networks on both interfaces : Detach all the networks from one of the interfaces/bond, if required re-attach to the bond")
    String networksOnBothInterfaces();

    @DefaultStringValue("Invalid operation with unmanaged network: unmanaged network can only be removed")
    String invalidOperationWithUnmanagedNetwork();

    @DefaultStringValue("Cannot add nic with unmanaged network/s to a bond")
    String cannotAddNicWithUnmanagedNetworkToBond();

    @DefaultStringValue("Cannot create a bond if one of the nics contains unmanaged network/s")
    String cannotCreateBondIfNicsContainsUnmanagedNetwork();

    @DefaultStringValue("Cannot add nic with unsync network/s to a bond")
    String cannotAddNicWithUnsyncNetworkToBond();

    @DefaultStringValue("Invalid operation with unsync network: unsync network can only be detached")
    String invalidOperationWithUnsyncNetwork();

    @DefaultStringValue("Cannot create a bond if one of the nics contains unsync network/s")
    String cannotCreateBondIfNicsContainsUnsyncNetwork();

    @DefaultStringValue("Unassigned Logical Networks panel")
    String unassignedLogicalNetworksPanel();

    @DefaultStringValue("disk_type=")
    String diskTypeSearchPrefix();

    @DefaultStringValue("Assign Disk Quota")
    String assignQuotaForDisk();

    @DefaultStringValue("Some imported VMs depend on one or more templates which are " +
    "not available in the system. Therefore you must Import VMs with 'collapse snapshots', "+
    "another option is to Import missing templates first and then try import the VMs again")
    String importMissingStorages();

    @DefaultStringValue("The selected default Storage Domain is not applicable for all disks " +
            "(i.e. some disks will be imported to a different Storage Domain).")
    String importNotApplicableForDefaultStorage();

    @DefaultStringValue("Template cannot be found in the system, VM(s) cannot be imported")
    String errorTemplateCannotBeFoundMessage();

    // Import Gluster Cluster
    @DefaultStringValue("fetching fingerprint...")
    String loadingFingerprint();

    @DefaultStringValue("Error in fetching fingerprint")
    String errorLoadingFingerprint();

    @DefaultStringValue("Fingerprint needs to be verified before importing the gluster configuration")
    String fingerprintNotVerified();

    @DefaultStringValue("Unable to fetch the gluster hosts. Please try again")
    String emptyGlusterHosts();

    @DefaultStringValue("Missing Quota for the selected Storage Domain(s), Please define proper Quota")
    String missingQuotaStorageEnforceMode();

    @DefaultStringValue("Missing Quota for the selected Cluster, Please define proper Quota")
    String missingQuotaClusterEnforceMode();

    @DefaultStringValue("Import Conflict")
    String importConflictTitle();

    // Network- Host
    @DefaultStringValue("Attached")
    String attachedHost();

    @DefaultStringValue("Unattached")
    String unattachedHost();

    // Network - Vm
    @DefaultStringValue("Running")
    String runningVm();

    @DefaultStringValue("Not Running")
    String notRunningVm();

    @DefaultStringValue("Remove VM Disk(s)")
    String removeVmDisksMsg();

    @DefaultStringValue("VM is created from template thus disks must be removed")
    String removeVmDisksTemplateMsg();

    @DefaultStringValue("VM has no disks")
    String removeVmDisksNoDisksMsg();

    @DefaultStringValue("Cannot remove VM with detaching disks - VM has snapshots")
    String removeVmDisksSnapshotsMsg();

    @DefaultStringValue("All VM disks are sharable and cannot be removed")
    String removeVmDisksAllSharedMsg();

    // Network
    @DefaultStringValue("VM")
    String vmNetworkRole();

    @DefaultStringValue("Management")
    String mgmgtNetworkRole();

    @DefaultStringValue("There are no new hosts in the cluster")
    String emptyNewGlusterHosts();

    @DefaultStringValue("Detach Gluster Hosts")
    String detachGlusterHostsTitle();

    // Vnic

    @DefaultStringValue("In order to change 'Type' please Unplug and then Plug again")
    String hotTypeUpdateNotPossible();

    @DefaultStringValue("In order to change 'MAC' please Unplug and then Plug again")
    String hotMacUpdateNotPossible();

    @DefaultStringValue("Updating 'Network' on a running virtual machine while the NIC is plugged is not supported when 'Port Mirroring' is set on the virtual machine interface")
    String hotNetworkUpdateNotSupportedWithPortMirroring();

    @DefaultStringValue("Updating 'Link State' on a running virtual machine while the NIC is plugged is not supported when 'Port Mirroring' is set on the virtual machine interface")
    String hotLinkStateUpdateNotSupportedWithPortMirroring();

    @DefaultStringValue("Updating 'Port Mirroring' on a running virtual machine while the NIC is plugged is not supported")
    String hotPortMirroringUpdateNotSupported();
}
