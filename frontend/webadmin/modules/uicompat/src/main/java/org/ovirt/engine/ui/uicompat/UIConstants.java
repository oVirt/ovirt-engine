package org.ovirt.engine.ui.uicompat;


import org.ovirt.engine.core.common.businessentities.VmPool;

import com.google.gwt.i18n.client.Constants;

public interface UIConstants extends com.google.gwt.i18n.client.Constants {

    @DefaultStringValue("OK")
    String ok();

    @DefaultStringValue("Cancel")
    String cancel();

    @DefaultStringValue("Yes")
    String yes();

    @DefaultStringValue("No")
    String no();

    @DefaultStringValue("Close")
    String close();

    @DefaultStringValue("N/A")
    String notAvailableLabel();

    @DefaultStringValue("[N/A]")
    String bracketedNotAvailableLabel();

    @DefaultStringValue("Not Specified")
    String notSpecifiedLabel();

    @DefaultStringValue("Action")
    String action();

    @DefaultStringValue("Show Advanced Options")
    String showAdvancedOptions();

    @DefaultStringValue("Hide Advanced Options")
    String hideAdvancedOptions();

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

    @DefaultStringValue("Join Bonds")
    String joinBondsTitle();

    @DefaultStringValue("Manage Networks")
    String assignDetachNetworksTitle();

    @DefaultStringValue("Manage Network")
    String assignDetachNetworkTitle();

    @DefaultStringValue("Clusters")
    String clustersTitle();

    @DefaultStringValue("Profiles")
    String networkProfilesTitle();

    @DefaultStringValue("New Cluster - Guide Me")
    String newClusterGuideMeTitle();

    @DefaultStringValue("Edit Cluster")
    String editClusterTitle();

    @DefaultStringValue("Remove Cluster(s)")
    String removeClusterTitle();

    @DefaultStringValue("Change Cluster Compatibility Version")
    String changeClusterCompatibilityVersionTitle();

    @DefaultStringValue("Change Data Center Quota Enforcement Mode")
    String changeDCQuotaEnforcementModeTitle();

    @DefaultStringValue("Notice")
    String setUnlimitedSpecificQuotaModeTitle();

    @DefaultStringValue("Disable CPU Thread Support")
    String disableClusterCpuThreadSupportTitle();

    @DefaultStringValue("Change Cluster CPU level")
    String changeCpuLevel();

    @DefaultStringValue("There are running VMs. Lowering the Cluster CPU level might prevent migration of these VMs to some of the Hosts in the Cluster. Are you sure you want to continue?")
    String changeCpuLevelConfirmation();

    @DefaultStringValue("General")
    String generalTitle();

    @DefaultStringValue("Services")
    String servicesTitle();

    @DefaultStringValue("Gluster Hooks")
    String glusterHooksTitle();

    @DefaultStringValue("Gluster Swift")
    String glusterSwiftTitle();

    @DefaultStringValue("Policy")
    String clusterPolicyTitle();

    @DefaultStringValue("Edit Policy")
    String editPolicyTitle();

    @DefaultStringValue("Copy to Clipboard")
    String copytoClipboardTitle();

    @DefaultStringValue("New Virtual Machine")
    String newVmTitle();

    @DefaultStringValue("Edit Virtual Machine")
    String editVmTitle();

    @DefaultStringValue("Clone Virtual Machine")
    String cloneVmTitle();

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

    @DefaultStringValue("Next Restart Configuration")
    String editNextRunConfigurationTitle();

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

    @DefaultStringValue("New")
    String newNetworkQosButton();

    @DefaultStringValue("Changing certain properties (e.g. VLAN, MTU) of the management network could lead to loss of connectivity to hosts in the data center, if its underlying network infrastructure isn't configured to accommodate the changes. Are you sure you want to proceed?")
    String updateManagementNetworkWarning();

    @DefaultStringValue("Attach/Detach Network to/from Clusters")
    String attachDetachNetworkToFromClustersTitle();

    @DefaultStringValue("Save network configuration")
    String saveNetworkConfigurationTitle();

    @DefaultStringValue("Storage")
    String storageTitle();

    @DefaultStringValue("Data Center is uninitialized, in order to initialize add a data domain")
    String dataCenterUninitializedAlert();

    @DefaultStringValue("The selected domain already has an ISO domain and an export domains attached")
    String noStoragesToImport();

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

    @DefaultStringValue("Network QoS")
    String networkQoSTitle();

    @DefaultStringValue("Volumes")
    String volumesTitle();

    @DefaultStringValue("New Volume")
    String newVolumeTitle();

    @DefaultStringValue("Add Bricks")
    String addBricksTitle();

    @DefaultStringValue("Parameters")
    String parameterTitle();

    @DefaultStringValue("Bricks")
    String bricksTitle();

    @DefaultStringValue("Rebalance Status")
    String volumeRebalanceStatusTitle();

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

    @DefaultStringValue("Remove VM Interface Profile(s)")
    String removeVnicProfileTitle();

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

    @DefaultStringValue("Invalid Import Configuration")
    String invalidImportTitle();

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

    @DefaultStringValue("Bricks")
    String hostBricksTitle();

    @DefaultStringValue("Edit Management Network")
    String editManagementNetworkTitle();

    @DefaultStringValue("The network's IP address cannot be statically modified if it's the same address supplied as the hostname.")
    String staticIpAddressSameAsHostname();

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

    @DefaultStringValue("External Providers")
    String externalProvidersTitle();

    @DefaultStringValue("Providers")
    String providersTitle();

    @DefaultStringValue("VNIC Profiles")
    String vnicProfilesTitle();

    @DefaultStringValue("New External Subnet")
    String newExternalSubnetTitle();

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

    @DefaultStringValue("Import Image(s)")
    String importImagesTitle();

    @DefaultStringValue("Export Image(s)")
    String exportImagesTitle();

    @DefaultStringValue("Snapshots")
    String snapshotsTitle();

    @DefaultStringValue("Delete Snapshot")
    String deleteSnapshotTitle();

    @DefaultStringValue("Create Snapshot")
    String createSnapshotTitle();

    @DefaultStringValue("Preview Snapshot")
    String previewSnapshotTitle();

    @DefaultStringValue("Custom Preview Snapshot")
    String customPreviewSnapshotTitle();

    @DefaultStringValue("Applications")
    String applicationsTitle();

    @DefaultStringValue("Monitor")
    String monitorTitle();

    @DefaultStringValue("Sessions")
    String sessionsTitle();

    @DefaultStringValue("RDP")
    String RDPTitle();

    @DefaultStringValue("RDP is not supported in your browser")
    String rdpIsNotSupportedInYourBrowser();

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

    @DefaultStringValue("Base Template(s) not Found on Export Domain")
    String baseTemplatesNotFoundOnExportDomainTitle();

    @DefaultStringValue("Run Virtual Machine(s)")
    String runVirtualMachinesTitle();

    @DefaultStringValue("VNC")
    String VNCTitle();

    @DefaultStringValue("SPICE")
    String spiceTitle();

    @DefaultStringValue("New Template")
    String newTemplateTitle();

    @DefaultStringValue("Migrate Virtual Machine(s)")
    String migrateVirtualMachinesTitle();

    @DefaultStringValue("Shut down Virtual Machine(s)")
    String shutdownVirtualMachinesTitle();

    @DefaultStringValue("Power Off Virtual Machine(s)")
    String stopVirtualMachinesTitle();

    @DefaultStringValue("Reboot Virtual Machine(s)")
    String rebootVirtualMachinesTitle();

    @DefaultStringValue("Change CD")
    String changeCDTitle();

    @DefaultStringValue("Console Disconnected")
    String consoleDisconnectedTitle();

    @DefaultStringValue("Import Template(s)")
    String importTemplatesTitle();

    @DefaultStringValue("Clone VM from Snapshot")
    String cloneVmFromSnapshotTitle();

    // Messages
    @DefaultStringValue("You are about to change the Cluster Compatibility Version. Are you sure you want to continue?")
    String youAreAboutChangeClusterCompatibilityVersionMsg();

    @DefaultStringValue("You are about to change the Cluster Compatibility Version and the cluster contains some non"
            + " responsive hosts. If these hosts do not support the selected compatibility level they may move to non"
            + " operational after connectivity is restored.\n\nAre you sure you want to continue?")
    String youAreAboutChangeClusterCompatibilityVersionNonResponsiveHostsMsg();

    @DefaultStringValue("Moving Quota to Enforce Mode\n" +
            "All the templates, virtual machines, and disks must be assigned into specific quota allocations otherwise will be unusable.\nUsers should be added as quota consumers.\n\n" +
            "Please consider using Audit mode until you define Quotas for the users.\n\n" +
            "Are you sure you want to continue?")
    String youAreAboutChangeDCQuotaEnforcementMsg();

    @DefaultStringValue("You are about to disable CPU thread support for this cluster. Disabling this can affect the ability to run VMs with certain CPU configurations.\n\n" +
            "Please ensure there are no VMs in this cluster making use of specific CPU settings such as CPU-pinning which may be affected by this change.\n\n" +
            "Are you sure you want to continue?")
    String youAreAboutChangeClusterCpuThreadSupportMsg();

    @DefaultStringValue("You are about to set an Unlimited quota on specific resource. This is inadvisable.\nThe aggregated value of this quota will be Unlimited.\nDo you wish to proceed?")
    String youAreAboutToCreateUnlimitedSpecificQuotaMsg();

    @DefaultStringValue("Name can contain only 'A-Z', 'a-z', '0-9', '_' or '-' characters.")
    String asciiNameValidationMsg();

    @DefaultStringValue("Name can contain only 'A-Z', 'a-z', '0-9', '_', '-' or '.' characters.")
    String noSpecialCharactersWithDotMsg();

    @DefaultStringValue("Only alphanumeric and some special characters that conform to the standard ASCII character set are allowed.")
    String asciiOrNoneValidationMsg();

    @DefaultStringValue("Only alphanumeric and some special characters that conform to the standard ASCII character set and UTF letters are allowed.")
    String specialAsciiI18NOrNoneValidationMsg();

    @DefaultStringValue("Name can contain only alphanumeric, '.', '_' or '-' characters.")
    String i18NNameValidationMsg();

    @DefaultStringValue("Name can contain only alphanumeric, '.', '_' or '-' characters, and optionally one sequence of '" + VmPool.MASK_CHARACTER + "' to specify mask for the VM indexes")
    String poolNameValidationMsg();

    @DefaultStringValue("UTF characters are not allowed.")
    String nonUtfValidationMsg();

    @DefaultStringValue("You haven't configured Power Management for this Host. Are you sure you want to continue?")
    String youHavntConfigPmMsg();

    @DefaultStringValue("Name must contain alphanumeric characters or '_' (maximum length 15 characters).")
    String nameMustContainAlphanumericMaxLenMsg();

    @DefaultStringValue("Network name shouldn't start with 'bond'.")
    String networkNameStartMsg();

    @DefaultStringValue("There are no compatible Storage Domains to attach to this Data Center. Please add new Storage from the Storage tab.")
    String thereAreNoCompatibleStorageDomainsAttachThisDcMsg();

    @DefaultStringValue("You are about to change the Data Center Compatibility Version. Are you sure you want to continue?")
    String youAreAboutChangeDcCompatibilityVersionMsg();

    @DefaultStringValue("You are about to attach the network to all the selected clusters and to detach the network from all the unselected clusters.\n\nAre you sure you want to continue?")
    String youAreAboutToAttachDetachNetworkToFromTheClustersMsg();

    @DefaultStringValue("Changes done to the Networking configuration are temporary until explicitly saved.\n\nAre you sure you want to make the changes persistent?")
    String areYouSureYouWantToMakeTheChangesPersistentMsg();

    @DefaultStringValue("Are you sure you want to Detach the following storage(s)?")
    String areYouSureYouWantDetachFollowingStoragesMsg();

    @DefaultStringValue("Are you sure you want to Detach from the user the following Virtual Machine(s)")
    String areYouSureYouWantDetachFromUserFollowingVmsMsg();

    @DefaultStringValue("Disks already exist on all available Storage Domains.")
    String disksAlreadyExistMsg();

    @DefaultStringValue("Templates reside on several Data Centers. Make sure the exported Templates reside on the same Data Center.")
    String templatesResideOnSeveralDcsMakeSureExportedTemplatesResideOnSameDcMsg();

    @DefaultStringValue("There is no Export Domain to export the Template into. Please attach an Export Domain to the Template's Data Center.")
    String thereIsNoExportDomainToExportTheTemplateIntoMsg();

    @DefaultStringValue("There are No Data Centers to which the Storage Domain can be attached")
    String thereAreNoDataCenterStorageDomainAttachedMsg();

    @DefaultStringValue("Are you sure you want to Detach storage from the following Data Center(s)?")
    String areYouSureYouWantDetachStorageFromDcsMsg();

    @DefaultStringValue("There is no Data Storage Domain to import the Template into. Please attach a Data Storage Domain to the Template's Data Center.")
    String thereIsNoDataStorageDomainToImportTemplateIntoMsg();

    @DefaultStringValue("There are selected items with non-matching architectures. Please select only items with the same architecture to proceed the import process.")
    String invalidImportMsg();

    @DefaultStringValue("The Export Domain is inactive. Data can be retrieved only when the Domain is activated")
    String theExportDomainIsInactiveMsg();

    @DefaultStringValue("Export Domain is not attached to any Data Center. Data can be retrieved only when the Domain is attached to a Data Center and is active")
    String ExportDomainIsNotAttachedToAnyDcMsg();

    @DefaultStringValue("The provider has the following certificates:\n")
    String theProviderHasTheFollowingCertificates();

    @DefaultStringValue("Do you approve importing this chain as trusted? (In case the chain consists of only an end certificate, it will be imported as trusted. Otherwise, all certificates will be trusted except the end certificate).")
    String doYouApproveImportingTheseCertificates();

    @DefaultStringValue("Import provider certificates")
    String importProviderCertificatesTitle();

    @DefaultStringValue("There are no networks available. Please add additional networks.")
    String thereAreNoNetworksAvailablePleaseAddAdditionalNetworksMsg();

    @DefaultStringValue("There are no ISO versions that are compatible with the Host's current version.")
    String thereAreNoISOversionsVompatibleWithHostCurrentVerMsg();

    @DefaultStringValue("Are you sure you want to place the following host(s) into maintenance mode?")
    String areYouSureYouWantToPlaceFollowingHostsIntoMaintenanceModeMsg();

    @DefaultStringValue("Are you sure you want to Restart the following Host(s), ungracefully stopping running VM(s)?")
    String areYouSureYouWantToRestartTheFollowingHostsMsg();

    @DefaultStringValue("Are you sure you want to Stop the following Host(s)?")
    String areYouSureYouWantToStopTheFollowingHostsMsg();

    @DefaultStringValue("Host doesn't support Local Storage configuration")
    String hostDoesntSupportLocalStorageConfigurationMsg();

    @DefaultStringValue("Host must be installed before upgrade.")
    String hostMustBeInstalledBeforeUpgrade();

    @DefaultStringValue("Testing in progress. It will take a few seconds. Please wait...")
    String testingInProgressItWillTakeFewSecondsPleaseWaitMsg();

    @DefaultStringValue("Name must be up to 126 characters and start with any word character.")
    String nameMustBeUpToAndStartWithMsg();

    @DefaultStringValue("Could not connect to the Engine Service, please try to refresh the page. If the problem persists contact your System Administrator.")
    String couldNotConnectToOvirtEngineServiceMsg();

    @DefaultStringValue("Are you sure you want to detach selected Virtual Machine(s)?")
    String areYouSurYouWantToDetachSelectedVirtualMachinesMsg();

    @DefaultStringValue("Snapshot cannot be created since the VM is performing an operation on a Snapshot.")
    String snapshotCannotBeCreatedLockedSnapshotMsg();

    @DefaultStringValue("Snapshot cannot be created since the VM is previewing a Snapshot.")
    String snapshotCannotBeCreatedPreviewSnapshotMsg();

    @DefaultStringValue("Snapshot cannot be created since the VM contains a stateless Snapshot.")
    String snapshotCannotBeCreatedStatelessSnapshotMsg();

    @DefaultStringValue("At least one disk must be marked.")
    String atLeastOneDiskMustBeMarkedMsg();

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

    @DefaultStringValue("Are you sure you want to Reboot the following Virtual Machines?")
    String areYouSureYouWantToRebootTheFollowingVirtualMachinesMsg();

    @DefaultStringValue("This field must contain an IP address in format xxx.xxx.xxx.xxx")
    String thisFieldMustContainIPaddressInFormatMsg();

    @DefaultStringValue("This field can be empty or contain an IP address in format xxx.xxx.xxx.xxx")
    String emptyOrValidIPaddressInFormatMsg();

    @DefaultStringValue("This field must contain a subnet in format xxx.xxx.xxx.xxx")
    String thisFieldMustContainSubnetInFormatMsg();

    @DefaultStringValue("This field must contain a CIDR in format xxx.xxx.xxx.xxx/yy, where xxx is between 0 and 255 and yy is between 0 and 32.")
    String thisFieldMustContainCidrInFormatMsg();

    @DefaultStringValue("This field can't contain spaces.")
    String thisFieldCantConatainSpacesMsg();

    @DefaultStringValue("Invalid MAC address")
    String invalidMacAddressMsg();

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

    @DefaultStringValue("No active Storage Domain is available - check Storage Domains and Hosts status.")
    String noStorageDomainAvailableMsg();

    @DefaultStringValue("Source Storage Domain is not active")
    String sourceStorageDomainIsNotActiveMsg();

    @DefaultStringValue("No active target Storage Domain is available")
    String noActiveTargetStorageDomainAvailableMsg();

    @DefaultStringValue("No active source Storage Domain is available")
    String noActiveSourceStorageDomainAvailableMsg();

    @DefaultStringValue("Disk exists on all active Storage Domains")
    String diskExistsOnAllActiveStorageDomainsMsg();

    @DefaultStringValue("The Template that the VM is based on does not exist on any active Storage Domain")
    String noActiveStorageDomainWithTemplateMsg();;

    @DefaultStringValue("Field value should follow: <parameter=value;parameter=value;...>")
    String fieldValueShouldFollowMsg();

    @DefaultStringValue("Import of a thin provisioned raw disk from a block device must be with collapse snapshot")
    String importSparseDiskToBlockDeviceMustCollapseSnapshots();

    @DefaultStringValue("Import of a VM based on template that does not exist in the system must be with collapse snapshot")
    String importVMWithTemplateNotInSystemMustCollapseSnapshots();

    @DefaultStringValue("Import of a VM that exists in the system has to be imported in clone mode")
    String importVMThatExistsInSystemMustClone();

    @DefaultStringValue("Import of a Template that exists in the system has to be imported in clone mode")
    String importTemplateThatExistsInSystemMustClone();

    @DefaultStringValue("Import of a VM with the clone option must be with collapse snapshot")
    String importCloneVMMustCollapseSnapshots();

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

    @DefaultStringValue("Note: to fully configure the Data-Center, at least one local Storage Domain should be attached and activated.")
    String attachLocalStorageDomainToFullyConfigure();

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

    @DefaultStringValue("Add another Host")
    String dataCenterAddAnotherHostAction();

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

    @DefaultStringValue("The given host address is neither a valid host name nor a valid IP address.")
    String addressIsNotValidHostNameOrIpAddressInvalidReason();

    @DefaultStringValue("The address has to be in form: [port://]hostnameOrIp[:port]")
    String portHostnameOrIpPort();

    @DefaultStringValue("Given URI is of an invalid format.")
    String uriInvalidFormat();

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

    @DefaultStringValue("This field can't contain trimming whitespace characters.")
    String trimmingSpacesInField();

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

    @DefaultStringValue("This field must contain positive integer number")
    String thisFieldMustContainNonNegativeIntegerNumberInvalidReason();

    @DefaultStringValue("A bond name must begin with the prefix 'bond' followed by a number.")
    String bondNameInvalid();

    // Role tree tooltip
    @DefaultStringValue("Allow to Add/Remove Users from the System")
    String allowToAddRemoveUsersFromTheSystemRoleTreeTooltip();

    @DefaultStringValue("Allow to add/remove permissions for Users on objects in the system")
    String allowToAddRemovePermissionsForUsersOnObjectsInTheSystemRoleTreeTooltip();

    @DefaultStringValue("Add users and groups from directory while adding permissions")
    String allowToAddUsersAndGroupsFromDirectoryOnObjectsInTheSystemRoleTreeTooltip();

    @DefaultStringValue("Allow to login to the system")
    String allowToLoginToTheSystemRoleTreeTooltip();

    @DefaultStringValue("Allow to manage Tags")
    String allowToManageTags();

    @DefaultStringValue("Allow to manage Bookmarks")
    String allowToManageBookmarks();

    @DefaultStringValue("Allow to manage Event Notifications")
    String allowToManageEventNotifications();

    @DefaultStringValue("Allow to manage Audit Logs")
    String allowToManageAuditLogs();

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

    @DefaultStringValue("Allow to create vNIC Profile")
    String allowToCreateVnicProfileRoleTreeTooltip();

    @DefaultStringValue("Allow to edit vNIC Profile")
    String allowToEditVnicProfileRoleTreeTooltip();

    @DefaultStringValue("Allow to delete vNIC Profile")
    String allowToDeleteVnicProfileRoleTreeTooltip();

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

    @DefaultStringValue("Allow to add, edit or remove Affinity Groups within the Cluster")
    String allowToManipulateAffinityGroupsForClusterRoleTreeTooltip();

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

    @DefaultStringValue("Allow changing VM administrative properties")
    String allowChangingVmAdminPropertiesRoleTreeTooltip();

    @DefaultStringValue("Allow changing Template administrative properties")
    String allowChangingTemplateAdminPropertiesRoleTreeTooltip();

    @DefaultStringValue("Allow to override the currently opened remote console session")
    String allowReconnectToVmRoleTreeTooltip();

    @DefaultStringValue("Allow to create new Vms")
    String allowToCreateNewVmsRoleTreeTooltip();

    @DefaultStringValue("Allow to create new Vms from Instance Type")
    String allowToCreateNewInstnaceRoleTreeTooltip();

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

    @DefaultStringValue("Allow to change SCSI I/O privileges")
    String allowToChangeSGIORoleTreeTooltip();

    @DefaultStringValue("Allow to access image domain")
    String allowAccessImageDomainRoleTreeTooltip();

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

    @DefaultStringValue("No storage domains selected. Please select storage domains to import.")
    String noStorageDomainsSelectedInvalidReason();

    @DefaultStringValue("No storage domains to import have been found.")
    String noStorageDomainsFound();

    @DefaultStringValue("Could not retrieve LUNs, please check your storage.")
    String couldNotRetrieveLUNsLunsFailure();

    @DefaultStringValue("If kernel parameters are specified, kernel path must be specified as well.")
    String kernelParamsInvalid();

    @DefaultStringValue("If initrd path is specified, kernel path must be specified as well.")
    String initrdPathInvalid();

    @DefaultStringValue("Allocation can be modified only when importing a single VM")
    String allocCanBeModifiedOnlyWhenImportSingleVm();

    @DefaultStringValue("Configure Virtual Disks")
    String vmConfigureVirtualDisksAction();

    @DefaultStringValue("Add another Virtual Disk")
    String vmAddAnotherVirtualDiskAction();

    @DefaultStringValue("There is no active Storage Domain to create the Disk in. Please activate a Storage Domain.")
    String thereIsNoActiveStorageDomainCreateDiskInMsg();

    @DefaultStringValue("Error in retrieving the relevant Storage Domain.")
    String errorRetrievingRelevantStorageDomainMsg();

    @DefaultStringValue("Could not read templates from Export Domain")
    String couldNotReadTemplatesFromExportDomainMsg();

    @DefaultStringValue("The following virtual machines are based on templates which do not exist on the export domain and are required for the virtual machines to function.\n" +
            "If you proceed you will not be able to import these virtual machines unless you already have the relevant templates on the target domains.\n" +
            "Do you wish to continue anyway?")
    String theFollowingTemplatesAreMissingOnTargetExportDomainMsg();

    @DefaultStringValue("The following template versions are based on templates which do not exist on the export domain and are required for the template version to function.\n" +
            "If you proceed you will not be able to import these template versions unless you already have the relevant templates on the target domains, or by using Clone.\n" +
            "Do you wish to continue anyway?")
    String theFollowingTemplatesAreMissingOnTargetExportDomainForTemplateVersionsMsg();

    @DefaultStringValue("There are no active Data-Centers in the system.")
    String noActiveDataCenters();

    @DefaultStringValue("There are no active Storage Domains that you have permissions to create a disk on in the relevant Data-Center.")
    String noActiveStorageDomainsInDC();

    @DefaultStringValue("The relevant Data-Center is not active.")
    String relevantDCnotActive();

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

    @DefaultStringValue("Configure vNIC Profile")
    String configureVnicProfileRoleTree();

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

    @DefaultStringValue("Volume")
    String volumeRoleTree();

    @DefaultStringValue("Configure Volumes")
    String configureVolumesRoleTree();

    @DefaultStringValue("Allow to create Gluster Volumes")
    String allowToCreateGlusterVolumesRoleTree();

    @DefaultStringValue("Allow to delete Gluster Volumes")
    String allowToDeleteGlusterVolumesRoleTree();

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

    @DefaultStringValue("This label has already been specified in another entry.")
    String duplicateLabel();

    // Fronted
    @DefaultStringValue("A Request to the Server failed with the following Status Code")
    String requestToServerFailedWithCode();

    @DefaultStringValue("A Request to the Server failed")
    String requestToServerFailed();

    @DefaultStringValue("No Message")
    String noCanDoActionMessage();

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

    @DefaultStringValue("Replicate Confirmation")
    String addBricksReplicateConfirmationTitle();

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

    @DefaultStringValue("Are you sure you want to reset the following option?")
    String resetOptionVolumeMsg();

    @DefaultStringValue("Reset All Options")
    String resetAllOptionsTitle();

    @DefaultStringValue("Are you sure you want to reset all the options?")
    String resetAllOptionsMsg();

    @DefaultStringValue("Stop Volume")
    String confirmStopVolume();

    @DefaultStringValue("Are you sure you want to stop the following Volume(s)?")
    String stopVolumeMessage();

    @DefaultStringValue("NOTE:\n -Stopping volume will make its data inaccessible.")
    String stopVolumeWarning();

    @DefaultStringValue("Remove Volume")
    String removeVolumesTitle();

    @DefaultStringValue("NOTE:\n -Removing volume will erase all information about the volume.")
    String removeVolumesWarning();

    @DefaultStringValue("Remove Bricks")
    String removeBricksTitle();

    @DefaultStringValue("Remove Bricks Status")
    String removeBricksStatusTitle();

    @DefaultStringValue("Replace Brick")
    String replaceBrickTitle();

    @DefaultStringValue("Could not find any host in Up status in the cluster. Please try again later.")
    String cannotAddBricksNoUpServerFound();

    @DefaultStringValue("Brick Advanced Details")
    String advancedDetailsBrickTitle();

    @DefaultStringValue("Error in fetching the brick details, please try again.")
    String errorInFetchingBrickAdvancedDetails();

    @DefaultStringValue("Are you sure you want to remove the following Brick(s)?")
    String removeBricksMessage();

    @DefaultStringValue("NOTE:\n -Removing brick(s) can result in data loss.")
    String removeBricksWarning();

    @DefaultStringValue("Stop Brick Removal")
    String stopRemoveBricksTitle();

    @DefaultStringValue("Are you sure you want to stop the removal operation of the following bricks?")
    String stopRemoveBricksMessage();

    @DefaultStringValue("Stop")
    String stopRemoveBricksButton();

    @DefaultStringValue("Commit")
    String commitRemoveBricksButton();

    @DefaultStringValue("Retain")
    String retainBricksButton();

    @DefaultStringValue("Commit Brick Removal")
    String commitRemoveBricksTitle();

    @DefaultStringValue("Are you sure you want to commit the removal of the following brick(s)?")
    String commitRemoveBricksMessage();

    @DefaultStringValue("Retain Bricks")
    String retainBricksTitle();

    @DefaultStringValue("Are you sure you want to retain the following brick(s)?")
    String retainBricksMessage();

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

    @DefaultStringValue("Number of bricks should be equal to Stripe Count * Replica count")
    String stripedReplicateVolumeAddBricksMsg();

    @DefaultStringValue("Number of bricks should be a mutiple of Stripe Count and Replica count")
    String distriputedStripedReplicateVolumeAddBricksMsg();

    @DefaultStringValue("Bricks cannot be empty")
    String emptyAddBricksMsg();

    @DefaultStringValue("Multiple bricks of a Replicate volume are present on the same server. This setup is not optimal. \nDo you still want to continue?")
    String addBricksToReplicateVolumeFromSameServerMsg();

    @DefaultStringValue("Stop Rebalance")
    String confirmStopVolumeRebalanceTitle();

    @DefaultStringValue("Disable Gluster Hooks")
    String confirmDisableGlusterHooks();

    @DefaultStringValue("Are you sure you want to disable the following Hook(s)?")
    String disableGlusterHooksMessage();

    @DefaultStringValue("Hook Content")
    String viewContentGlusterHookTitle();

    @DefaultStringValue("Unable to fetch the hook content, please try again later.")
    String viewContentErrorGlusterHook();

    @DefaultStringValue("Hook content is empty or binary")
    String viewContentEmptyGlusterHook();

    @DefaultStringValue("Resolve Conflicts")
    String resolveConflictsGlusterHookTitle();

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

    @DefaultStringValue("No host found in 'UP' state in the cluster, please select another cluster.")
    String volumeEmptyClusterValidationMsg();

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

    @DefaultStringValue("Wipe after delete is not supported for file domains")
    String wipeAfterDeleteNotSupportedForFileDomains();

    @DefaultStringValue("Moving disk(s) while the VM is running")
    String liveStorageMigrationWarning();

    @DefaultStringValue("Note: Target domains are filtered by the source domain type (file/block)")
    String liveStorageMigrationStorageFilteringNote();

    @DefaultStringValue("Cannot remove more than one brick from a Replicate volume at a time")
    String cannotRemoveBricksReplicateVolume();

    @DefaultStringValue("Incorrect bricks selected for removal in Distributed Replicate volume.\nEither all the selected bricks should be from the same sub volume or one brick each for every sub volume!")
    String cannotRemoveBricksDistributedReplicateVolume();

    @DefaultStringValue("Incorrect bricks selected for removal in Distributed Stripe volume.\nSelected bricks should be from the same sub volume!")
    String cannotRemoveBricksDistributedStripeVolume();

    @DefaultStringValue("Incorrect bricks selected for removal in Striped Replicate volume.\nSelected bricks should be a multiple of stripe and replica count")
    String cannotRemoveBricksStripedReplicateVolume();

    @DefaultStringValue("Incorrect bricks selected for removal in Distributed Striped Replicate volume.\nSelected bricks should be from the same sub volume!")
    String cannotRemoveBricksDistributedStripedReplicateVolume();

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

    @DefaultStringValue("Invalid operation with unmanaged network: unmanaged network can only be removed")
    String nullOperationUnmanagedNetwork();

    @DefaultStringValue("Cannot perform bond operation with an unmanaged network.")
    String nullOperationBondUnmanaged();

    @DefaultStringValue("Invalid operation with unsync network: unsync network can only be detached")
    String nullOperationOutOfSyncNetwork();

    @DefaultStringValue("Cannot perform bond operation with an out-of-sync network.")
    String nullOperationBondOutOfSync();

    @DefaultStringValue("Cannot have more than one non-VLAN network on one interface.")
    String nullOperationTooManyNonVlans();

    @DefaultStringValue("Cannot have a non-VLAN VM network and VLAN-tagged networks on one interface.")
    String nullOperationVmWithVlans();

    @DefaultStringValue("disk_type=")
    String diskTypeSearchPrefix();

    @DefaultStringValue("Assign Disk Quota")
    String assignQuotaForDisk();

    @DefaultStringValue("Some imported VMs depend on one or more templates which are " +
            "not available in the system. Therefore you must Import those VMs with 'collapse snapshots', " +
    "another option is to Import missing templates first and then try import the VMs again")
    String importMissingStorages();

    @DefaultStringValue("The selected default Storage Domain is not applicable for all disks " +
            "(i.e. some disks will be imported to a different Storage Domain).")
    String importNotApplicableForDefaultStorage();

    @DefaultStringValue("VM(s) cannot be imported: One of the templates cannot be found. Please verify that the template exists in both setup and export domain.")
    String errorTemplateCannotBeFoundMessage();

    // Import Gluster Cluster
    @DefaultStringValue("fetching fingerprint...")
    String loadingFingerprint();

    @DefaultStringValue("Error in fetching fingerprint")
    String errorLoadingFingerprint();

    @DefaultStringValue("Fetched fingerprint successfully")
    String successLoadingFingerprint();

    @DefaultStringValue("Fingerprint needs to be verified before importing the gluster configuration")
    String fingerprintNotVerified();

    @DefaultStringValue("Address Error: ")
    String fingerprintAddressError();

    // Gluster Swift
    @DefaultStringValue("Manage Gluster Swift")
    String manageGlusterSwiftTitle();

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

    @DefaultStringValue("Detach Hosts")
    String detachGlusterHostsTitle();

    // Vnic
    @DefaultStringValue("In order to change 'Type' please Unplug and then Plug again")
    String hotTypeUpdateNotPossible();

    @DefaultStringValue("In order to change 'MAC' please Unplug and then Plug again")
    String hotMacUpdateNotPossible();

    @DefaultStringValue("Detaching an externally-provided network from a plugged NIC on a running virtual machine is not supported")
    String hotNetworkUpdateNotSupportedExternalNetworks();

    @DefaultStringValue("Updating 'Link State' on a running virtual machine while the NIC is plugged is not supported for externally-provided networks")
    String hotLinkStateUpdateNotSupportedExternalNetworks();

    @DefaultStringValue("Console connect")
    String confirmConsoleConnect();

    @DefaultStringValue("There may be users connected to the console who will not be able to reconnect. Do you want to proceed?")
    String confirmConsoleConnectMessage();

    @DefaultStringValue("No disks selected")
    String noDisksSelected();

    @DefaultStringValue("Alerts")
    String alertsTitle();

    @DefaultStringValue("There can be only one bootable disk defined")
    String onlyOneBootableDisk();

    @DefaultStringValue("Enter a valid FS type (e.g. nfs/glusterfs/cifs/smbfs etc.")
    String posixVfsTypeHint();

    @DefaultStringValue("Enter additional Mount Options, as you would normally provide them to the mount command using the -o argument.\nThe mount options should be provided in a comma-separated list. See 'man mount' for a list of valid mount options.")
    String mountOptionsHint();

    @DefaultStringValue("Event Details")
    String eventDetailsTitle();

    @DefaultStringValue("'Use Host CPU' is only available for cluster compatible with ver 3.2 or higher, when 'Do not allow migration' is selected or 'Allow manual migration' is selected and no host is specified) ")
    String hosCPUUnavailable();

    @DefaultStringValue("'CPU Pinning topology' is only available for cluster compatible with ver 3.1 or higher, when 'Do not allow migration' is selected and host is specified")
    String cpuPinningUnavailable();

    @DefaultStringValue("'CPU Pinning topology' is only available for cluster compatible with ver 3.1 or higher")
    String cpuPinningUnavailableLocalStorage();

    @DefaultStringValue("'Port Mirroring' is not supported for externally-provided networks")
    String portMirroringNotSupportedExternalNetworks();

    @DefaultStringValue("'Port Mirroring' cannot be changed if the vNIC Profile is used by a VM")
    String portMirroringNotChangedIfUsedByVms();

    @DefaultStringValue("Low")
    String vmLowPriority();

    @DefaultStringValue("Medium")
    String vmMediumPriority();

    @DefaultStringValue("High")
    String vmHighPriority();

    @DefaultStringValue("Unknown")
    String vmUnknownPriority();

    @DefaultStringValue("Any Host in Cluster")
    String anyHostInCluster();

    @DefaultStringValue("Unsupported")
    String unsupported();

    @DefaultStringValue("Unknown")
    String unknown();

    @DefaultStringValue("SMT Disabled")
    String smtDisabled();

    @DefaultStringValue("SMT Enabled")
    String smtEnabled();

    @DefaultStringValue("Primary")
    String primaryPmVariant();

    @DefaultStringValue("Secondary")
    String secondaryPmVariant();

    @DefaultStringValue("Discovered Hosts")
    String externalHostsDiscovered();

    @DefaultStringValue("Provisioned Hosts")
    String externalHostsProvisioned();

    @DefaultStringValue("Eject")
    String eject();

    // Provider
    @DefaultStringValue("Networks")
    String providerNetworksTitle();

    @DefaultStringValue("Add Provider")
    String addProviderTitle();

    @DefaultStringValue("Edit Provider")
    String editProviderTitle();

    @DefaultStringValue("Remove Provider(s)")
    String removeProviderTitle();

    @DefaultStringValue("Change Provider URL")
    String providerUrlWarningTitle();

    @DefaultStringValue("No authentication URL found; please configure one using the 'engine-config' utility, then restart the engine service.")
    String noAuthUrl();

    @DefaultStringValue("Import Networks")
    String importNetworksTitle();

    @DefaultStringValue("Import")
    String importNetworksButton();

    @DefaultStringValue("Interface Mappings")
    String interfaceMappings();

    @DefaultStringValue("Bridge Mappings")
    String bridgeMappings();

    @DefaultStringValue("Please use a comma-separated list of 'label:interface'")
    String interfaceMappingsExplanation();

    @DefaultStringValue("Please use a comma-separated list of 'label:bridge'")
    String bridgeMappingsExplanation();

    @DefaultStringValue("The given mappings are of an invalid format.")
    String interfaceMappingsInvalid();

    @DefaultStringValue("Remove network(s) from the external provider(s) as well.")
    String removeNetworkFromProvider();

    // Gluster Hook
    @DefaultStringValue("Please select a resolve action to continue")
    String noResolveActionSelectedGlusterHook();

    @DefaultStringValue("Please select an action to continue")
    String noActionSelectedManageGlusterSwift();

    @DefaultStringValue("Host cannot be set highly available when 'Do not allow migration' or 'Allow manual migration' is selected")
    String hostNonMigratable();

    @DefaultStringValue("Host must be migratable when highly available is selected")
    String hostIsHa();

    @DefaultStringValue("Cannot change Cluster's trust support state while host/s existed in the cluster")
    String trustedServiceDisabled();

    @DefaultStringValue("22")
    String defaultHostSSHPort();

    @DefaultStringValue("Remove Network QoS")
    String removeNetworkQoSTitle();

    @DefaultStringValue("Edit Network QoS")
    String editNetworkQoSTitle();

    @DefaultStringValue("New Network QoS")
    String newNetworkQoSTitle();

    @DefaultStringValue("Are you sure you want to remove this Network QoS")
    String removeNetworkQoSMessage();

    @DefaultStringValue("New Cluster Policy")
    String newClusterPolicyTitle();

    @DefaultStringValue("Edit Cluster Policy")
    String editClusterPolicyTitle();

    @DefaultStringValue("Clone Cluster Policy")
    String copyClusterPolicyTitle();

    @DefaultStringValue("Remove Cluster Policy")
    String removeClusterPolicyTitle();

    @DefaultStringValue("KSM control is only available for Cluster compatibility version 3.4 and higher")
    String ksmNotAvailable();

    @DefaultStringValue("Ballooning is only available for Cluster compatibility version 3.3 and higher")
    String ballooningNotAvailable();

    // Cloud-Init
    @DefaultStringValue("Passwords do not match")
    String cloudInitRootPasswordMatchMessage();

    @DefaultStringValue("List can contain zero or more IP addresses separated by spaces, each in the form xxx.xxx.xxx.xxx")
    String cloudInitDnsServerListMessage();

    @DefaultStringValue("ethX [Click to Change]")
    String cloudInitNewNetworkItem();

    @DefaultStringValue("/path [Click to Change]")
    String cloudInitNewAttachmentItem();

    @DefaultStringValue("Plain Text")
    String cloudInitAttachmentTypePlainText();

    @DefaultStringValue("Base64")
    String cloudInitAttachmentTypeBase64();

    @DefaultStringValue("Content must be Base64")
    String cloudInitBase64Message();

    @DefaultStringValue("VM Interface Profile")
    String vnicProfileTitle();

    @DefaultStringValue("Cannot edit name in tree context")
    String cannotEditNameInTreeContext();

    @DefaultStringValue("Cannot change Repository type with Storage Domains attached to it")
    String cannotChangeRepositoryTypeWithSDAttached();

    @DefaultStringValue("Cannot change Data Center in tree context")
    String cannotChangeDCInTreeContext();

    @DefaultStringValue("Cannot change Cluster in tree context")
    String cannotChangeClusterInTreeContext();

    @DefaultStringValue("Cannot change Host in tree context")
    String cannotChangeHostInTreeContext();

    @DefaultStringValue("Data Center can be changed only when the Host is in Maintenance mode.")
    String dcCanOnlyBeChangedWhenHostInMaintMode();

    @DefaultStringValue("Cluster can be changed only when the Host is in Maintenance mode.")
    String clusterCanOnlyBeChangedWhenHostInMaintMode();

    @DefaultStringValue("Time Zone cannot be changed after the Virtual Machine is initialized.")
    String timeZoneCannotBeChangedAfterVMInit();

    @DefaultStringValue("Network QoS is supported only for Data Center version 3.3 or higher")
    String qosNotSupportedDcVersion();

    @DefaultStringValue("[Unlimited]")
    String unlimitedQoSTitle();

    @DefaultStringValue("Rebalance Status")
    String rebalanceStatusTitle();

    @DefaultStringValue("Fetching Data")
    String fetchingDataMessage();

    @DefaultStringValue("Stop")
    String stopRebalance();

    @DefaultStringValue("Manage Policy Units")
    String managePolicyUnits();

    @Constants.DefaultStringValue("For data integrity make sure that the server is configured with Quorum (both client and server Quorum)")
    String glusterDomainConfigurationMessage();

    @DefaultStringValue("Not available when no Data Center is up.")
    String notAvailableWithNoUpDC();

    @DefaultStringValue("Not available when Templates are not configured.")
    String notAvailableWithNoTemplates();
    @DefaultStringValue("Connecting to pool not supported.")
    String connectToPoolNotSupported();

    @DefaultStringValue("Affinity Groups")
    String affinityGroupsTitle();

    @DefaultStringValue("New Affinity Group")
    String newAffinityGroupsTitle();

    @DefaultStringValue("Edit Affinity Group")
    String editAffinityGroupsTitle();

    @DefaultStringValue("Remove Affinity Group(s)")
    String removeAffinityGroupsTitle();

    @DefaultStringValue("Select a VM")
    String selectVm();

    @DefaultStringValue("No available VMs")
    String noAvailableVms();

    @DefaultStringValue("iSCSI Bonds")
    String iscsiBondsTitle();

    @DefaultStringValue("Add iSCSI Bond")
    String addIscsiBondTitle();

    @DefaultStringValue("Edit iSCSI Bond")
    String editIscsiBondTitle();

    @DefaultStringValue("Remove iSCSI Bond(s)")
    String removeIscsiBondTitle();

    @DefaultStringValue("No networks selected")
    String noNetworksSelected();

    @DefaultStringValue("<latest>")
    String latestTemplateVersionName();

    @DefaultStringValue("In case the vm is stateless, vm will be re-created with the LATEST template version")
    String latestTemplateVersionDescription();

    @DefaultStringValue("VirtIO-SCSI is not supported for the selected OS")
    String cannotEnableVirtioScsiForOs();

    @DefaultStringValue("Maintenance Storage Domain(s)")
    String maintenanceStorageDomainsTitle();

    @DefaultStringValue("Are you sure you want to place the following storage domain(s) into maintenance mode?")
    String areYouSureYouWantToPlaceFollowingStorageDomainsIntoMaintenanceModeMsg();

    @DefaultStringValue("Deactivate VM Disk(s)")
    String deactivateVmDisksTitle();

    @DefaultStringValue("Are you sure you want to deactivate the following VM disk(s)?")
    String areYouSureYouWantDeactivateVMDisksMsg();

    @DefaultStringValue("An IDE disk can't be read-only.")
    String cannotEnableIdeInterfaceForReadOnlyDisk();

    @DefaultStringValue("A VirtIO-SCSI DirectLUN disk can't be set as read-only when SCSI pass-through is enabled.")
    String cannotEnableReadonlyWhenScsiPassthroughEnabled();

    @DefaultStringValue("Privileged SCSI I/O can be set only when SCSI pass-through is enabled.")
    String cannotEnableSgioWhenScsiPassthroughDisabled();

    @DefaultStringValue("SCSI pass-through is not supported for read-only disks.")
    String cannotEnableScsiPassthroughForLunReadOnlyDisk();

    @DefaultStringValue("Global Maintenance Enabled")
    String haGlobalMaintenance();

    @DefaultStringValue("Local Maintenance Enabled")
    String haLocalMaintenance();

    @DefaultStringValue("Not Active")
    String haNotActive();

    @DefaultStringValue("When the VM is running, cannot activate a disk attached with IDE interface.")
    String cannotHotPlugDiskWithIdeInterface();

    @DefaultStringValue("Cannot activate disk, VM should be in Down, Paused or Up status.")
    String cannotPlugDiskIncorrectVmStatus();

    @DefaultStringValue("Unplug VM Network Interface")
    String unplugVnicTitle();

    @DefaultStringValue("Are you sure you want to unplug the VM Network Interface?")
    String areYouSureYouWantUnplugVnicMsg();

    @DefaultStringValue("New Instance Type")
    String newInstanceTypeTitle();

    @DefaultStringValue("Edit Instance Type")
    String editInstanceTypeTitle();

    @DefaultStringValue("Remove Instance Type")
    String removeInstanceTypeTitle();

    @DefaultStringValue("Custom")
    String customInstanceTypeName();

    @DefaultStringValue("No instance type - if selected the instance will be attached to no instance type")
    String customInstanceTypeDescription();

    @DefaultStringValue("There are VMs attached to this instance type-this will make them attached to 'Custom'")
    String vmsAttachedToInstanceTypeNote();

    @DefaultStringValue("The following VMs are attached to the instance type")
    String vmsAttachedToInstanceTypeWarningMessage();

    @DefaultStringValue("If period is specified, bytes per period must be specified as well.")
    String rngRateInvalid();

    @DefaultStringValue("Random Number Generator requirements must be set in Cluster.")
    String rngNotSupportedByCluster();

    @DefaultStringValue("Random Number Generator is not supported by your cluster compatibility version.")
    String rngNotSupportedByClusterCV();

    @DefaultStringValue("Random Number Generator not supported for this cluster level or is disabled in the engine config.")
    String rngNotSupported();

    @DefaultStringValue("Login All")
    String loginAllButtonLabel();

    @DefaultStringValue("Login")
    String loginButtonLabel();

    @DefaultStringValue("Default")
    String defaultMtu();

    @DefaultStringValue("Custom")
    String customMtu();
}

