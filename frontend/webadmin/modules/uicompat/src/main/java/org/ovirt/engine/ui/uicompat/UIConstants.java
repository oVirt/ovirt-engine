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

    @DefaultStringValue("")
    String emptyString();

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

    @DefaultStringValue("Add new Label")
    String addNewLabelTitle();

    @DefaultStringValue("Manage Networks")
    String assignDetachNetworksTitle();

    @DefaultStringValue("Manage Network")
    String assignDetachNetworkTitle();

    @DefaultStringValue("Clusters")
    String clustersTitle();

    @DefaultStringValue("Profiles")
    String networkProfilesTitle();

    @DefaultStringValue("Cluster - Guide Me")
    String newClusterGuideMeTitle();

    @DefaultStringValue("Edit Cluster")
    String editClusterTitle();

    @DefaultStringValue("Remove Cluster(s)")
    String removeClusterTitle();

    @DefaultStringValue("Reset Cluster(s) Emulated Machine")
    String resetClusterEmulatedMachineTitle();

    @DefaultStringValue("Are you sure you want to reset the Emulated-Machine of the following clusters?")
    String resetClusterEmulatedMachineMessage();

    @DefaultStringValue("Change Cluster Compatibility Version")
    String changeClusterCompatibilityVersionTitle();

    @DefaultStringValue("There are running VMs in the cluster. All of them have to be rebooted if the Cluster Compatibility Version is changed.\n")
    String thereAreActiveVMsRequiringRestart();

    @DefaultStringValue("Change Data Center Quota Enforcement Mode")
    String changeDCQuotaEnforcementModeTitle();

    @DefaultStringValue("Notice")
    String setUnlimitedSpecificQuotaModeTitle();

    @DefaultStringValue("Disable CPU Thread Support")
    String disableClusterCpuThreadSupportTitle();

    @DefaultStringValue("Change Cluster CPU level")
    String changeCpuLevel();

    @DefaultStringValue("\n - The cluster contains VMs which are currently running. \n")
    String changeCpuLevelWhileRunningMessage();

    @DefaultStringValue(" - The following VMs have a custom CPU level which is not supported by the new cluster CPU level:\n")
    String changeCpuLevelCustomVmCpusMessage();

    @DefaultStringValue("\nLowering the Cluster CPU level might prevent migration of these VMs to some of the hosts in the Cluster. Are you sure you want to continue?")
    String changeCpuLevelWarningMessage();

    @DefaultStringValue("The following user session(s) will be terminated.\nAre you sure you want to continue?")
    String terminateSessionConfirmation();

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

    @DefaultStringValue("VM's LUN(s) will not be cloned.")
    String cloneVmLunsWontBeCloned();

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

    @DefaultStringValue("Pending Virtual Machine changes")
    String editNextRunConfigurationTitle();

    @DefaultStringValue("Unsupported CPU type for current cluster")
    String vmUnsupportedCpuTitle();

    @DefaultStringValue("The selected CPU type is not supported by the VM cluster, this may cause scheduling limitations and prevent this vm from running on certain hosts. Are you sure you want to proceed?")
    String vmUnsupportedCpuMessage();

    @DefaultStringValue("Data Centers")
    String dataCentersTitle();

    @DefaultStringValue("Data Center - Guide Me")
    String newDataCenterGuideMeTitle();

    @DefaultStringValue("Configure Later")
    String configureLaterTitle();

    @DefaultStringValue("New Data Center")
    String newDataCenterTitle();

    @DefaultStringValue("Edit Data Center")
    String editDataCenterTitle();

    @DefaultStringValue("Remove Data Center(s)")
    String removeDataCenterTitle();

    @DefaultStringValue("New")
    String addMacPoolButton();

    @DefaultStringValue("Force Remove Data Center")
    String forceRemoveDataCenterTitle();

    @DefaultStringValue("Data Center Re-Initialize")
    String dataCenterReInitializeTitle();

    @DefaultStringValue("Change Data Center Compatibility Version")
    String changeDataCenterCompatibilityVersionTitle();

    @DefaultStringValue("New MAC Address Pool")
    String newSharedMacPoolTitle();

    @DefaultStringValue("Edit MAC Address Pool")
    String editSharedMacPoolTitle();

    @DefaultStringValue("Remove MAC Address Pool(s)")
    String removeSharedMacPoolsTitle();

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

    @DefaultStringValue("The selected Data Center compatibility version does not support importing a data domain with its entities (VM's and Templates). The domain will be imported without them.")
    String dataCenterDoesntSupportImportDataDomainAlert();

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

    @DefaultStringValue("Storage QoS")
    String storageQosTitle();

    @DefaultStringValue("CPU QoS")
    String cpuQosTitle();

    @DefaultStringValue("Host Network QoS")
    String hostNetworkQosTitle();

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

    @DefaultStringValue("Geo-Replication")
    String geoReplicationTitle();

    @DefaultStringValue("Geo-Replication Session Details")
    String geoReplicationSessionDetailsTitle();

    @DefaultStringValue("Could not fetch brick details")
    String geoRepBrickDetailsFetchFailed();

    @DefaultStringValue("Could not fetch Geo-Replication status details")
    String geoRepSessionStatusDetailFetchFailed();

    @DefaultStringValue("Start Geo-Replication")
    String geoReplicationStartTitle();

    @DefaultStringValue("Stop Geo-Replication")
    String geoReplicationStopTitle();

    @DefaultStringValue("Pause Geo-Replication")
    String geoReplicationPauseTitle();

    @DefaultStringValue("Resume Geo-Replication")
    String geoReplicationResumeTitle();

    @DefaultStringValue("Remove Geo-Replication")
    String geoReplicationRemoveTitle();

    @DefaultStringValue("Start")
    String startGeoRepProgressText();

    @DefaultStringValue("Stop")
    String stopGeoRepProgressText();

    @DefaultStringValue("Pause")
    String pauseGeoRepProgressText();

    @DefaultStringValue("Resume")
    String resumeGeoRepProgressText();

    @DefaultStringValue("Remove")
    String removeGeoRepProgressText();

    @DefaultStringValue("start")
    String startGeoRep();

    @DefaultStringValue("stop")
    String stopGeoRep();

    @DefaultStringValue("pause")
    String pauseGeoRep();

    @DefaultStringValue("resume")
    String resumeGeoRep();

    @DefaultStringValue("remove")
    String removeGeoRep();

    @DefaultStringValue("Geo-Replication Options")
    String geoReplicationOptions();

    @DefaultStringValue("Set")
    String setGeorepConfig();

    @DefaultStringValue("Reset All")
    String resetAllConfigsTitle();

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

    @DefaultStringValue("Template(s) has dependent VM(s) on Export Domain")
    String removeBackedUpTemplatesWithDependentsVMTitle();

    @DefaultStringValue("The following templates have dependent VM(s) on the export domain and are required for importing these VM(s).\n"
            +
            "If you proceed you will not be able to import these virtual machines unless you already have the relevant templates on the target domains.\n"
            +
            "Do you wish to continue anyway?")
    String theFollowingTemplatesHaveDependentVmsBackupOnExportDomainMsg();

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

    @DefaultStringValue("[New Label]")
    String newLabelPanelText();

    @DefaultStringValue("Add new Label to")
    String newLabel();

    @DefaultStringValue("Install Host")
    String installHostTitle();

    @DefaultStringValue("Upgrade Host")
    String upgradeHostTitle();

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

    @DefaultStringValue("You must select a provider")
    String validateSelectExternalProvider();

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

    @DefaultStringValue("Attach Virtual Disks")
    String attachVirtualDiskTitle();

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

    @DefaultStringValue("Preview Partial Snapshot")
    String previewPartialSnapshotTitle();

    @DefaultStringValue("Include in the previewed VM all disks (keep the disks that are not included in the snapshot as-is).")
    String preserveActiveDisks();

    @DefaultStringValue("Include in the previewed VM only the disks that are included in the snapshot.")
    String excludeActiveDisks();

    @DefaultStringValue("None of the above, take me to the 'Custom Preview Snapshot' dialog.")
    String openCustomPreviewDialog();

    @DefaultStringValue("Applications")
    String applicationsTitle();

    @DefaultStringValue("Monitor")
    String monitorTitle();

    @DefaultStringValue("Guest Information")
    String guestInformationTitle();

    @DefaultStringValue("Sessions")
    String sessionsTitle();

    @DefaultStringValue("User Sessions")
    String userSessionsTitle();

    @DefaultStringValue("RDP")
    String RDPTitle();

    @DefaultStringValue("RDP is not supported in your browser")
    String rdpIsNotSupportedInYourBrowser();

    @DefaultStringValue("Retrieving CDs...")
    String retrievingCDsTitle();

    @DefaultStringValue("Virtual Machine - Guide Me")
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

    @DefaultStringValue("Moving Quota to Enforce Mode\n"
            +
            "All the templates, virtual machines, and disks must be assigned into specific quota allocations otherwise will be unusable.\nUsers should be added as quota consumers.\n\n"
            +
            "Please consider using Audit mode until you define Quotas for the users.\n\n" +
            "Are you sure you want to continue?")
    String youAreAboutChangeDCQuotaEnforcementMsg();

    @DefaultStringValue("You are about to disable CPU thread support for this cluster. Disabling this can affect the ability to run VMs with certain CPU configurations.\n\n"
            +
            "Please ensure there are no VMs in this cluster making use of specific CPU settings such as CPU-pinning which may be affected by this change.\n\n"
            +
            "Are you sure you want to continue?")
    String youAreAboutChangeClusterCpuThreadSupportMsg();

    @DefaultStringValue("You are about to set an Unlimited quota on specific resource. This is inadvisable.\nThe aggregated value of this quota will be Unlimited.\nDo you wish to proceed?")
    String youAreAboutToCreateUnlimitedSpecificQuotaMsg();

    @DefaultStringValue("Name can contain only 'A-Z', 'a-z', '0-9', '_' or '-' characters.")
    String asciiNameValidationMsg();

    @DefaultStringValue("Name can contain only 'A-Z', 'a-z', '0-9', '_' or '-' characters, and might be followed by '@' and domain name")
    String asciiNameAndDomainValidationMsg();

    @DefaultStringValue("Name can contain only 'A-Z', 'a-z', '0-9', '_', '-' or '.' characters.")
    String noSpecialCharactersWithDotMsg();

    @DefaultStringValue("Name can contain only 'A-Z', 'a-z', '0-9', '_', '-' or ':' characters.")
    String vmInitNetworkNameValidationMsg();

    @DefaultStringValue("Only alphanumeric and some special characters that conform to the standard ASCII character set are allowed.")
    String asciiOrNoneValidationMsg();

    @DefaultStringValue("Only alphanumeric and some special characters that conform to the standard ASCII character set and UTF letters are allowed.")
    String specialAsciiI18NOrNoneValidationMsg();

    @DefaultStringValue("Name can contain only alphanumeric, '.', '_' or '-' characters.")
    String i18NNameValidationMsg();

    @DefaultStringValue("Name can contain only alphanumeric, '.', '_' or '-' characters, and optionally one sequence of '"
            + VmPool.MASK_CHARACTER + "' to specify mask for the VM indexes")
    String poolNameValidationMsg();

    @DefaultStringValue("Name can contain only alphanumeric, '.', '_', '+' or '-' characters.")
    String I18NExtraNameOrNoneValidation();

    @DefaultStringValue("UTF characters are not allowed.")
    String nonUtfValidationMsg();

    @DefaultStringValue("You haven't configured Power Management for this Host. Are you sure you want to continue?")
    String youHavntConfigPmMsg();

    @DefaultStringValue("Edit fence agent")
    String editFenceAgent();

    @DefaultStringValue("Select fence proxy preference type to add")
    String selectFenceProxy();

    @DefaultStringValue("Concurrent with:")
    String concurrentFenceAgent();

    @DefaultStringValue("Duplicate fence agent found.")
    String duplicateFenceAgentManagementIp();

    @DefaultStringValue("Name must contain alphanumeric characters, '-' or '_' (maximum length 15 characters).")
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

    @DefaultStringValue("Are you sure you want to detach the following storage(s)?")
    String areYouSureYouWantDetachFollowingStoragesMsg();

    @DefaultStringValue("Are you sure you want to detach from the user the following Virtual Machine(s)")
    String areYouSureYouWantDetachFromUserFollowingVmsMsg();

    @DefaultStringValue("Disks already exist on all available Storage Domains.")
    String disksAlreadyExistMsg();

    @DefaultStringValue("Templates reside on several Data Centers. Make sure the exported Templates reside on the same Data Center.")
    String templatesResideOnSeveralDcsMakeSureExportedTemplatesResideOnSameDcMsg();

    @DefaultStringValue("There is no Export Domain to export the Template into. Please attach an Export Domain to the Template's Data Center.")
    String thereIsNoExportDomainToExportTheTemplateIntoMsg();

    @DefaultStringValue("There are No Data Centers to which the Storage Domain can be attached")
    String thereAreNoDataCenterStorageDomainAttachedMsg();

    @DefaultStringValue("Are you sure you want to detach Storage from the following Data Center(s)?")
    String areYouSureYouWantDetachStorageFromDcsMsg();

    @DefaultStringValue("There is no Data Storage Domain to import the Template into. Please attach a Data Storage Domain to the Template's Data Center.")
    String thereIsNoDataStorageDomainToImportTemplateIntoMsg();

    @DefaultStringValue("There are selected items with non-matching architectures. Please select only items with the same architecture to proceed the import process.")
    String invalidImportMsg();

    @DefaultStringValue("The Export Domain is inactive. Data can be retrieved only when the Domain is activated")
    String theExportDomainIsInactiveMsg();

    @DefaultStringValue("Export Domain is not attached to any Data Center. Data can be retrieved only when the Domain is attached to a Data Center and is active")
    String ExportDomainIsNotAttachedToAnyDcMsg();

    @DefaultStringValue("Import provider certificate")
    String importProviderCertificateTitle();

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

    @DefaultStringValue("There are running VMs on this host. Are you sure you want to continue the upgrade process? Running VM(s) will be migrated to another host.")
    String areYouSureYouWantToUpgradeTheFollowingHostWithRunningVmsMsg();

    @DefaultStringValue("Are you sure you want to Upgrade this Host?")
    String areYouSureYouWantToUpgradeTheFollowingHostMsg();

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

    @DefaultStringValue("Could not detect Guest Agent on the VM. Note that without a Guest Agent the data on the created snapshot may be inconsistent.")
    String liveSnapshotWithNoGuestAgentMsg();

    @DefaultStringValue("At least one disk must be marked.")
    String atLeastOneDiskMustBeMarkedMsg();

    @DefaultStringValue("Virtual Machines reside on several Data Centers. Make sure the exported Virtual Machines reside on the same Data Center.")
    String vmsResideOnSeveralDCsMakeSureTheExportedVMResideOnSameDcMsg();

    @DefaultStringValue("There is no Export Domain to Backup the Virtual Machine into. Attach an Export Domain to the Virtual Machine's Data Center.")
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

    @DefaultStringValue("This field must contain a subnet in either of the following formats:\n\txxx.xxx.xxx.xxx where xxx is between 0 and 255.\n\txx where xx is between 0-32")
    String thisFieldMustContainValidPrefixOrNetmask();

    @DefaultStringValue("This field must contain a subnet of the following format:\n\txxx.xxx.xxx.xxx where xxx is between 0 and 255")
    String thisFieldMustContainValidNetmask();

    @DefaultStringValue("Invalid mask value")
    String inValidNetmask();

    @DefaultStringValue("This field must contain a CIDR in format xxx.xxx.xxx.xxx/yy, where xxx is between 0 and 255 and yy is between 0 and 32.")
    String thisFieldMustContainCidrInFormatMsg();

    @DefaultStringValue("This field must contain CIDR notation of an IP subnet, please ensure that the IP address matches the prefix.\nexample:\n\tvalid network address: 2.2.0.0/16\n\tinvalid: 2.2.0.1/16")
    String cidrNotNetworkAddress();

    @DefaultStringValue("This field can't contain spaces.")
    String thisFieldCantConatainSpacesMsg();

    @DefaultStringValue("This field must contain a valid unicast MAC address.")
    String invalidUnicastMacAddressMsg();

    @DefaultStringValue("Invalid MAC address")
    String invalidMacAddressMsg();

    @DefaultStringValue("The right bound of the MAC address range must not be smaller than its left bound.")
    String invalidMacRangeRightBound();

    @DefaultStringValue("Label already exists in the Data Center.")
    String labelAlreadyExists();

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

    @DefaultStringValue("No active external image provider has been configured.")
    String noExternalImageProviderHasBeenConfiguredMsg();

    @DefaultStringValue("Source Storage Domain is not active")
    String sourceStorageDomainIsNotActiveMsg();

    @DefaultStringValue("No active target Storage Domain is available")
    String noActiveTargetStorageDomainAvailableMsg();

    @DefaultStringValue("No active source Storage Domain is available")
    String noActiveSourceStorageDomainAvailableMsg();

    @DefaultStringValue("Disk exists on all active Storage Domains")
    String diskExistsOnAllActiveStorageDomainsMsg();

    @DefaultStringValue("The Template that the VM is based on does not exist on any active Storage Domain")
    String noActiveStorageDomainWithTemplateMsg();

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

    @DefaultStringValue("No actions available on cluster.")
    String guidePopupNoActionsLabel();

    @DefaultStringValue("Attach Cluster to Data Center")
    String addDataCenter();

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

    @DefaultStringValue("Name is already used in the environment, create new unique name.")
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

    @DefaultStringValue("power on")
    String powerOn();

    @DefaultStringValue("power off")
    String powerOff();

    @DefaultStringValue("Error while retrieving list of domains. Please consult your Storage Administrator.")
    String errorWhileRetrievingListOfDomains();

    @DefaultStringValue("Please select a Storage Domain to import")
    String pleaseSelectStorageDomainToImportImportSanStorage();

    @DefaultStringValue("Error while retrieving list of domains. Please consult your Storage Administrator.")
    String errorWhileRetrievingListOfDomainsImportSanStorage();

    @DefaultStringValue("Value does not match pattern: key=value,key=value...")
    String valueDoesntNotMatchPatternKeyValueKeyValueInvalidReason();

    @DefaultStringValue("Value does not match pattern: key=value,key,key=value...")
    String valueDoesntNotNatchPatternKeyValueKeyKeyValueInvalidReason();

    @DefaultStringValue("Data Center is not accessible.")
    String dataCenterIsNotAccessibleMsg();

    @DefaultStringValue("This field can't be empty.")
    String thisFieldCantBeEmptyInvalidReason();

    @DefaultStringValue("Please fill in all fields.")
    String emptyFieldsInvalidReason();

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

    @DefaultStringValue("This field must contain an integer number")
    String thisFieldMustContainIntegerNumberInvalidReason();

    @DefaultStringValue("This field must contain a number")
    String thisFieldMustContainNumberInvalidReason();

    @DefaultStringValue("This field must contain a positive integer number")
    String thisFieldMustContainNonNegativeIntegerNumberInvalidReason();

    @DefaultStringValue("A bond name must begin with the prefix 'bond' followed by a number.")
    String bondNameInvalid();

    @DefaultStringValue("Format Domain, i.e. Storage Content will be lost!")
    String storageRemovePopupFormatLabel();

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

    @DefaultStringValue("Allow connecting to the VM graphical console screen")
    String allowViewingTheVmConsoleScreenRoleTreeTooltip();

    @DefaultStringValue("Allow connecting to VM serial console")
    String allowConnectingToVmSerialConsoleRoleTreeTooltip();

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

    @DefaultStringValue("Cpu Profile")
    String cpuProfileRoleTree();

    @DefaultStringValue("Disk")
    String diskRoleTree();

    @DefaultStringValue("Allow to create CPU Profile")
    String allowToCreateCpuRoleTreeTooltip();

    @DefaultStringValue("Allow to delete CPU Profile")
    String allowToDeleteCpuRoleTreeTooltip();

    @DefaultStringValue("Allow to update CPU Profile")
    String allowToUpdateCpuProfileRoleTreeTooltip();

    @DefaultStringValue("Allow to assign CPU Profile to Vm")
    String allowToAssignCpuRoleTreeToolTip();

    @DefaultStringValue("note: Permissions containing these operations should be associated with Cpu Profile provisioning operations")
    String notePermissionsContainingCpuProfileProvisioningOperationsRoleTreeTooltip();

    @DefaultStringValue("note: Permissions containing these operations should be associated with Cpu Profile administration operations")
    String notePermissionsContainingCpuProfileAdministrationOperationsRoleTreeTooltip();


    @DefaultStringValue("note: Permissions containing these operations should be associated with Disk or Storage Domain Object (or above)")
    String notePermissionsContainingDiskOperationsRoleTreeTooltip();

    @DefaultStringValue("Allow to create Disk")
    String allowToCreateDiskRoleTreeTooltip();

    @DefaultStringValue("Allow to delete Disk")
    String allowToDeleteDiskRoleTreeTooltip();

    @DefaultStringValue("Allow to move Disk to another Storage Domain")
    String allowToMoveDiskToAnotherStorageDomainRoleTreeTooltip();

    @DefaultStringValue("Allow to live migrate a Disk to another Storage Domain")
    String allowToLiveMigrateDiskToAnotherStorageDomainRoleTreeTooltip();

    @DefaultStringValue("Allow to attach Disk to a VM")
    String allowToAttachDiskToVmRoleTreeTooltip();

    @DefaultStringValue("Allow to change properties of the Disk")
    String allowToChangePropertiesOfTheDiskRoleTreeTooltip();

    @DefaultStringValue("Allow to change SCSI I/O privileges")
    String allowToChangeSGIORoleTreeTooltip();

    @DefaultStringValue("Allow to access image domain")
    String allowAccessImageDomainRoleTreeTooltip();

    @DefaultStringValue("Allow to attach Disk Profile to a Disk")
    String allowToAttachDiskProfile();

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

    @DefaultStringValue("There is no storage domain under the specified path. Check event pane for more details.")
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

    @DefaultStringValue("Create a Virtual Disk")
    String vmCreateVirtualDiskAction();

    @DefaultStringValue("Attach Virtual Disks")
    String vmAttachVirtualDisksAction();

    @DefaultStringValue("There is no active Storage Domain to create the Disk in. Please activate a Storage Domain.")
    String thereIsNoActiveStorageDomainCreateDiskInMsg();

    @DefaultStringValue("Error in retrieving the relevant Storage Domain.")
    String errorRetrievingRelevantStorageDomainMsg();

    @DefaultStringValue("Could not read templates from Export Domain")
    String couldNotReadTemplatesFromExportDomainMsg();

    @DefaultStringValue("The following virtual machines are based on templates which do not exist on the export domain and are required for the virtual machines to function.\n"
            +
            "If you proceed you will not be able to import these virtual machines unless you already have the relevant templates on the target domains.\n"
            +
            "Do you wish to continue anyway?")
    String theFollowingTemplatesAreMissingOnTargetExportDomainMsg();

    @DefaultStringValue("The following template versions are based on templates which do not exist on the export domain and are required for the template version to function.\n"
            +
            "If you proceed you will not be able to import these template versions unless you already have the relevant templates on the target domains, or by using Clone.\n"
            +
            "Do you wish to continue anyway?")
    String theFollowingTemplatesAreMissingOnTargetExportDomainForTemplateVersionsMsg();

    @DefaultStringValue("There are no active Data Centers in the system.")
    String noActiveDataCenters();

    @DefaultStringValue("There are no active Storage Domains that you have permissions to create a disk on in the relevant Data Center.")
    String noActiveStorageDomainsInDC();

    @DefaultStringValue("There are no available OpenStack Volume storage domains that you have permissions to create a disk on in the relevant Data Center.")
    String noCinderStorageDomainsInDC();

    @DefaultStringValue("The relevant Data Center is not active.")
    String relevantDCnotActive();

    @DefaultStringValue("Host name must be at least one character long and may contain only 'a-z', '0-9', '_', '.' or '-' characters.")
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

    @DefaultStringValue("Configure Disk Profile")
    String configureDiskProfileRoleTree();

    @DefaultStringValue("Allow to create Disk Profile")
    String allowToCreateDiskProfileRoleTreeTooltip();

    @DefaultStringValue("Allow to delete Disk Profile")
    String allowToDeleteDiskProfileRoleTreeTooltip();

    @DefaultStringValue("Allow to update Disk Profile")
    String allowToUpdateDiskProfileRoleTreeTooltip();

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

    @DefaultStringValue("Disk Profile")
    String attachDiskProfileRoleTree();

    @DefaultStringValue("Note: Permissions containing these operations should be associated with Disk Profile or Storage Domain Object (or above)")
    String notePermissionsContainingDiskProfileOperationsRoleTreeTooltip();

    @DefaultStringValue("Allow to attach Disk Profile to a Disk")
    String allowToAttachDiskProfileToDiskRoleTreeTooltip();

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

    @DefaultStringValue("Edit")
    String edit();

    @DefaultStringValue("Edit Options")
    String editOptionsTitle();

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

    @DefaultStringValue("Following volumes are already started but some/all bricks are down. Do you want to force start them ?")
    String startForceVolumeMessage();

    @DefaultStringValue("Start Volume")
    String confirmStartVolume();

    @DefaultStringValue("Force start")
    String startForceLabel();

    @DefaultStringValue("NOTE:\n -Stopping volume will make its data inaccessible.")
    String stopVolumeWarning();

    @DefaultStringValue("NOTE:\n -Stopping meta volume will impact normal working of features like volume snapshot, geo-replication etc.")
    String stopMetaVolumeWarning();

    @DefaultStringValue("Remove Volume")
    String removeVolumesTitle();

    @DefaultStringValue("NOTE:\n -Removing volume will erase all information about the volume.")
    String removeVolumesWarning();

    @DefaultStringValue("NOTE:\n - Removing meta volume will impact normal working of features like volume snapshot, geo-replication etc.")
    String removeMetaVolumeWarning();

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

    @DefaultStringValue("Are you sure you want to remove the following Brick(s) from meta volume?")
    String removeMetaVolumeBricksMessage();

    @DefaultStringValue("NOTE:\n -Removing bricks from meta volume will impact normal working of features like volume snapshot, geo-replication etc.")
    String removeMetaVolumeBricksWarning();

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

    @DefaultStringValue("V4.1")
    String nfsVersion41();

    @DefaultStringValue("DirectLUN disk is not supported by the Data Center Compatibility Version")
    String directLUNDiskNotSupported();

    @DefaultStringValue("Shareable Disk is not supported by the Data Center Compatibility Version")
    String shareableDiskNotSupported();

    @DefaultStringValue("Shareable Disk is not supported by the selected configuration")
    String shareableDiskNotSupportedByConfiguration();

    @DefaultStringValue("Moving disk(s) while the VM is running")
    String liveStorageMigrationWarning();

    @DefaultStringValue("Note: Some target domains are filtered by the source domain type (file/block)")
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

    @DefaultStringValue("Set public key for Serial Console proxy access")
    String consolePublicKeyTitle();

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

    @DefaultStringValue("Invalid operation, an unmanaged network can only be detached.")
    String nullOperationUnmanagedNetwork();

    @DefaultStringValue("Invalid operation, an out-of-sync network can only be detached.")
    String nullOperationOutOfSyncNetwork();

    @DefaultStringValue("Synchronize all host networks")
    String syncAllHostNetworkConfirmationDialogTitle();

    @DefaultStringValue("Are you sure you want to synchronize all host's networks?")
    String areYouSureYouWantToSyncAllHostNetworksMsg();

    @DefaultStringValue("Cannot have more than one non-VLAN network on one interface.")
    String nullOperationTooManyNonVlans();

    @DefaultStringValue("Cannot mix non-VLAN VM network and VLAN-tagged networks on same interface.")
    String nullOperationVmWithVlans();

    @DefaultStringValue("Cannot add vm network to bond in bond mode 0, 5 or 6.")
    String nullOperationInvalidBondMode();

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

    @DefaultStringValue("You have selected windows OS and have not selected VirtIO drivers. This may cause the system not to boot up.")
    String missingVirtioDriversForWindows();

    @DefaultStringValue("Missing Quota for the selected Cluster, Please define proper Quota")
    String missingQuotaClusterEnforceMode();

    @DefaultStringValue("Import Virtual Machine Conflict")
    String importVmConflictTitle();

    @DefaultStringValue("Import Template Conflict")
    String importTemplateConflictTitle();

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

    @DefaultStringValue("Updating 'Link State' on a 'passthrough' vm network interface is not supported")
    String linkStateUpdateNotSupportedForPassthroughVnic();

    @DefaultStringValue("Console connect")
    String confirmConsoleConnect();

    @DefaultStringValue("There may be users connected to the console who will not be able to reconnect. Do you want to proceed?")
    String confirmConsoleConnectMessage();

    @DefaultStringValue("Console connection denied. Another user has already accessed the console of this VM. The VM should either be rebooted to allow another user to access it, or changed by an admin to not enforce a reboot between users accessing its console.")
    String userCantReconnectToVm();

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

    @DefaultStringValue("'Use Host CPU' is only available for non-PPC-based clusters compatible with version 3.2 or higher with 'Do not allow migration' selected.")
    String hosCPUUnavailable();

    @DefaultStringValue("'CPU Pinning topology' is only available for cluster compatible with ver 3.1 or higher, when 'Do not allow migration' is selected and host is specified")
    String cpuPinningUnavailable();

    @DefaultStringValue("'CPU Pinning topology' is only available for cluster compatible with ver 3.1 or higher")
    String cpuPinningUnavailableLocalStorage();

    @DefaultStringValue("'Port Mirroring' is not supported for externally-provided networks")
    String portMirroringNotSupportedExternalNetworks();

    @DefaultStringValue("'Port Mirroring' cannot be changed if the vNIC Profile is used by a VM")
    String portMirroringNotChangedIfUsedByVms();

    @DefaultStringValue("'Port Mirroring' cannot be set if the vNIC Profile is 'Passthrough'")
    String portMirroringNotChangedIfPassthrough();

    @DefaultStringValue("'Network QoS' cannot be set if the vNIC Profile is 'Passthrough'")
    String networkQosNotChangedIfPassthrough();

    @DefaultStringValue("'Passthrough' cannot be changed if the vNIC Profile is used by a VM")
    String passthroughNotChangedIfUsedByVms();

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

    @DefaultStringValue("Any Host in Data Center")
    String anyHostInDataCenter();

    @DefaultStringValue("Any Data Center")
    String anyDataCenter();

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

    @DefaultStringValue("Terminate Session(s)")
    String terminateSessionTitle();

    @DefaultStringValue("Change Provider URL")
    String providerUrlWarningTitle();

    @DefaultStringValue("Import Networks")
    String importNetworksTitle();

    @DefaultStringValue("Import")
    String importNetworksButton();

    @DefaultStringValue("Cannot import network, another network with the same name has been marked for import.")
    String importDuplicateName();

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

    @DefaultStringValue("Activating the Hosts may take a while, please close the 'Guide Me' window and track the Hosts activation via the Events tab or the Hosts tab")
    String hostActivationTimeOut();

    @DefaultStringValue("Changing Host Clusters is taking longer than expected, please close the 'Guide Me' window and activate hosts via the Hosts tab")
    String hostChangeClusterTimeOut();

    @DefaultStringValue("Host cannot be set highly available when 'Do not allow migration' is selected and pinning to a single hosts is requested.")
    String hostNonMigratable();

    @DefaultStringValue("VM cannot be set highly available when hosted engine is used.")
    String noHaWhenHostedEngineUsed();

    @DefaultStringValue("Host must be migratable or pinned to multiple hosts when high availability is requested.")
    String hostIsHa();

    @DefaultStringValue("Cannot change Cluster's trust support state while host/s existed in the cluster")
    String trustedServiceDisabled();

    @DefaultStringValue("22")
    String defaultHostSSHPort();

    @DefaultStringValue("Remove Network QoS")
    String removeNetworkQoSTitle();

    @DefaultStringValue("Remove Storage QoS")
    String removeStorageQoSTitle();

    @DefaultStringValue("Remove CPU QoS")
    String removeCpuQoSTitle();

    @DefaultStringValue("Edit Network QoS")
    String editNetworkQoSTitle();

    @DefaultStringValue("Edit Storage QoS")
    String editStorageQoSTitle();

    @DefaultStringValue("Edit CPU QoS")
    String editCpuQoSTitle();

    @DefaultStringValue("New Network QoS")
    String newNetworkQoSTitle();

    @DefaultStringValue("New Host Network QoS")
    String newHostNetworkQosTitle();

    @DefaultStringValue("Edit Host Network QoS")
    String editHostNetworkQosTitle();

    @DefaultStringValue("Remove Host Network QoS")
    String removeHostNetworkQosTitle();

    @DefaultStringValue("New Storage QoS")
    String newStorageQoSTitle();

    @DefaultStringValue("New CPU QoS")
    String newCpuQoSTitle();

    @DefaultStringValue("Are you sure you want to remove this Network QoS")
    String removeNetworkQoSMessage();

    @DefaultStringValue("New Scheduling Policy")
    String newClusterPolicyTitle();

    @DefaultStringValue("Edit Scheduling Policy")
    String editClusterPolicyTitle();

    @DefaultStringValue("Clone Scheduling Policy")
    String copyClusterPolicyTitle();

    @DefaultStringValue("Remove Scheduling Policy")
    String removeClusterPolicyTitle();

    // MoM
    @DefaultStringValue("KSM control is only available for Cluster compatibility version 3.4 and higher")
    String ksmNotAvailable();

    @DefaultStringValue("KSM with NUMA optimization control is only available for Cluster compatibility version 3.6 and higher")
    String ksmWithNumaAwarnessNotAvailable();

    @DefaultStringValue("Share memory pages across all available memory (best KSM effectivness)")
    String shareKsmAcrossNumaNodes();

    @DefaultStringValue("Share memory pages inside NUMA nodes (best NUMA performance)")
    String shareKsmInsideEachNumaNode();

    @DefaultStringValue("Ballooning is only available for Cluster compatibility version 3.3 and higher")
    String ballooningNotAvailable();

    // Cloud-Init
    @DefaultStringValue("Passwords do not match")
    String cloudInitRootPasswordMatchMessage();

    @DefaultStringValue("List can contain zero or more IP addresses separated by spaces, each in the form xxx.xxx.xxx.xxx")
    String cloudInitDnsServerListMessage();

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

    @DefaultStringValue("The VM is suspended, it might not operate normally after resume on different cluster. It's suggested to power it off before cluster change.")
    String suspendedVMsWhenClusterChange();

    @DefaultStringValue("There is no Cluster on which you can create a VM. Please check Data Center status and Cluster permissions.")
    String notAvailableWithNoUpDCWithClusterWithPermissions();

    @DefaultStringValue("Some VMs are running in the external system and therefore have been filtered")
    String runningVmsWereFilteredOnImportVm();

    @DefaultStringValue("Not available when no Export Domain is active.")
    String notAvailableWithNoActiveExportDomain();

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

    @DefaultStringValue("Storage Domain maintenance")
    String maintenanceStorageDomainsTitle();

    @DefaultStringValue("Are you sure you want to place the following Storage Domain into maintenance mode?")
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

    @DefaultStringValue("The selected IDE disk(s) will be attached, but will not be activated. In order to activate them, you should shut the VM down and manually activate them.")
    String ideDisksWillBeAttachedButNotActivated();

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

    @DefaultStringValue("Are you sure?")
    String storageDomainsAttachedToDataCenterWarningTitle();

    @DefaultStringValue("Storage Domain(s) are already attached to a Data Center. Approving this operation might cause data corruption if both Data Centers are active.")
    String storageDomainsAttachedToDataCenterWarningMessage();

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

    @DefaultStringValue("MAC Pools")
    String macPoolTree();

    @DefaultStringValue("MAC Pool User")
    String macPoolUser();

    @DefaultStringValue("Create")
    String allowToCreateMacPoolTooltip();

    @DefaultStringValue("Edit")
    String allowToEditMacPoolTooltip();

    @DefaultStringValue("Delete")
    String allowToDeleteMacPoolTooltip();

    @DefaultStringValue("Configure")
    String allowToUseMacPoolTooltip();

    @DefaultStringValue("Optimize for Virt Store")
    String optimiseForVirtStoreTitle();

    @DefaultStringValue("The following volumes were found not to be of the suggested replica-3 type : \n")
    String optimiseForVirtStoreWarning();

    @DefaultStringValue("Are you sure you want to continue?")
    String optimiseForVirtStoreContinueMessage();

    @DefaultStringValue("Yes")
    String continueOptimiseForVirtStore();

    @DefaultStringValue("No")
    String doNotOptimiseForVirtStore();

    @DefaultStringValue("Disk Profiles")
    String diskProfilesTitle();

    @DefaultStringValue("Disk Profile")
    String diskProfileTitle();

    @DefaultStringValue("Remove Disk Profile(s)")
    String removeDiskProfileTitle();

    @DefaultStringValue("Remove CPU Profile(s)")
    String removeCpuProfileTitle();

    @DefaultStringValue("CPU Profile")
    String cpuProfileTitle();

    @DefaultStringValue("Insufficient parameters to test connectivity")
    String testFailedInsufficientParams();

    @DefaultStringValue("To enable NUMA set migration mode to non-migratable, and pin VM to host that supports NUMA topology")
    String numaDisabledInfoMessage();

    @DefaultStringValue("Click on Numa Pinning to configure VM's virtual node span on top of physical host NUMA nodes")
    String numaInfoMessage();

    @DefaultStringValue("Either Total or Read/Write can have values.")
    String eitherTotalOrReadWriteCanHaveValues();

    @DefaultStringValue("The detach operation will move the entities residing on the storage domain(s) to an unregistered state. For further information please consult documentation.")
    String detachWarnningNote();

    @DefaultStringValue("Remove the Data Center(s) will move the entities residing on the storage domain(s) to an unregistered state. For further information please consult documentation.")
    String removeDataCenterWarnningNote();

    @DefaultStringValue("Back")
    String back();

    @DefaultStringValue("Next")
    String next();

    @DefaultStringValue("This field is not a valid Guid (use 0-9,A-F format: 00000000-0000-0000-0000-000000000000)")
    String invalidGuidMsg();

    @DefaultStringValue("This will force the operation on geo-replication sessions on the nodes that are part of the master volume \n."
            + "If it is unable to successfully perform the operation on any node which is online and part of the master volume,\n "
            + "the command will still perform the operation on as many nodes as it can.\n"
            + "This command can also be used to re-perform the operation on the nodes where the session has died, or the operation has not be executed.")
    String geoRepForceHelp();

    // Gluster Volume Snapshots
    @DefaultStringValue("Create/Schedule Snapshot")
    String createScheduleVolumeSnapshotTitle();

    @DefaultStringValue("Edit Snapshot Schedule")
    String editVolumeSnapshotScheduleTitle();

    @DefaultStringValue("Date")
    String endDateOptionText();

    @DefaultStringValue("No End Date")
    String noEndDateOptionText();

    @DefaultStringValue("Volume Snapshot - Cluster Options")
    String configureClusterSnapshotOptionsTitle();

    @DefaultStringValue("Volume Snapshot - Volume Options")
    String configureVolumeSnapshotOptionsTitle();

    @DefaultStringValue("Update")
    String snapshotConfigUpdateButtonLabel();

    @DefaultStringValue("Update snapshot configuration options")
    String updateSnapshotConfigurationConfirmationTitle();

    @DefaultStringValue("Remove Gluster Volume snapshot schedule")
    String removeGlusterVolumeSnapshotScheduleConfirmationTitle();

    @DefaultStringValue("Configuring volume snapshot options\n\n"
            + "Changing configuration parameters will limit the creation of new snapshots if they exceed the new limit.\n\n"
            + "Do you want to continue?")
    String youAreAboutChangeSnapshotConfigurationMsg();

    @DefaultStringValue("The snapshot schedule would be deleted. Do you want to continue?")
    String youAreAboutToRemoveSnapshotScheduleMsg();

    @DefaultStringValue("existing")
    String existingDisk();

    @DefaultStringValue("creating")
    String creatingDisk();

    @DefaultStringValue("attaching")
    String attachingDisk();

    @DefaultStringValue("boot")
    String bootDisk();

    @DefaultStringValue("Storage Devices")
    String storageDevices();

    @DefaultStringValue("The volume will be brought down and restored to the state of the selected snapshot.\nDo you want to continue?")
    String confirmVolumeSnapshotRestoreWithStopMessage();

    @DefaultStringValue("All snapshots will be removed. Do you want to continue?")
    String confirmVolumeSnapshotDeleteAllMessage();

    @DefaultStringValue("The selected snapshot will be activated. Do you want to continue?")
    String confirmVolumeSnapshotActivateMessage();

    @DefaultStringValue("The selected snapshot will be deactivated.\n Do you want to continue?")
    String confirmVolumeSnapshotDeactivateMessage();

    @DefaultStringValue("Geo-replication set config")
    String geoReplicationConfigSetTitle();

    @DefaultStringValue("Geo-replication session configuration set failed")
    String geoRepSessionConfigSetFailed();

    @DefaultStringValue("Geo-replication reset config")
    String geoReplicationConfigResetTitle();

    @DefaultStringValue("Geo-replication session configuration reset failed")
    String geoRepSessionConfigResetFailed();

    @DefaultStringValue("Last Day")
    String lastDay();

    @DefaultStringValue("No week days selected")
    String noWeekDaysSelectedMessage();

    @DefaultStringValue("No month days selected")
    String noMonthDaysSelectedMessage();

    @DefaultStringValue("Last day of month cannot be selected with other month days")
    String lastDayMonthCanBeSelectedAlone();

    @DefaultStringValue("End by date cannot be equal to or before start date")
    String endDateBeforeStartDate();

    @DefaultStringValue("Unable to fetch gluster volume snapshot schedule")
    String unableToFetchVolumeSnapshotSchedule();

    @DefaultStringValue("Create Brick")
    String createBrick();

    @DefaultStringValue("Changing management network is only permitted via the 'Manage Cluster Networks' dialog.")
    String prohibitManagementNetworkChangeInEditClusterInfoMessage();

    @DefaultStringValue("New Geo-Replication Session")
    String newGeoRepSessionTitle();

    @DefaultStringValue("root")
    String rootUser();

    @DefaultStringValue("Warning : Recommendations for geo-replication not met -")
    String geoReplicationRecommendedConfigViolation();

    @DefaultStringValue("Not supported for Cinder disks")
    String notSupportedForCinderDisks();

    @DefaultStringValue("Register Disks")
    String registerDisksTitle();

    @DefaultStringValue("Create Authentication Key")
    String createSecretTitle();

    @DefaultStringValue("Edit Authentication Key")
    String editSecretTitle();

    @DefaultStringValue("Remove Authentication Key(s)")
    String removeSecretTitle();

    @DefaultStringValue("Icon file is not parsable.")
    String iconIsNotParsable();

    @DefaultStringValue("Icon is not validated yet. Please try it again later.")
    String iconNotValidatedYet();

    @DefaultStringValue("Authentication Key value must be encoded in Base64.")
    String secretValueMustBeInBase64();

    @DefaultStringValue("Please select a storage device to create brick.")
    String selectStorageDevice();

    @DefaultStringValue("Invalid Mount Point.")
    String invalidMountPointMsg();

    @DefaultStringValue("Invalid name")
    String invalidName();

    @DefaultStringValue("All networks")
    String allNetworksAllowed();

    @DefaultStringValue("Specific networks")
    String specificNetworksAllowed();

    @DefaultStringValue("Host Devices")
    String hostDevicesTitle();

    @DefaultStringValue("Remove Host Device(s)")
    String removeHostDevices();

    @DefaultStringValue("Add Host Devices")
    String addVmHostDevicesTitle();

    @DefaultStringValue("Pin VM to Host")
    String repinHostTitle();

    @DefaultStringValue("Some non-default base template has to exist first.")
    String someNonDefaultTemplateHasToExistFirst();

    @DefaultStringValue("VM Devices")
    String vmDevicesTitle();

    @DefaultStringValue("No provider")
    String providerNone();

    @DefaultStringValue("Errata")
    String errata();

    @DefaultStringValue("Problem retrieving errata:")
    String katelloProblemRetrievingErrata();

    @DefaultStringValue("Confirm Cluster Edit")
    String confirmClusterWarnings();

    @DefaultStringValue("Configured")
    String configured();

    @DefaultStringValue("Not Configured")
    String notConfigured();

    @DefaultStringValue("Soundcard is not available in selected cluster")
    String soundDeviceUnavailable();

    @DefaultStringValue("Unable to remove proxy preference")
    String unableToRemoveTitle();

    @DefaultStringValue("You must have at least 1 proxy preference")
    String unableToRemove();

    @DefaultStringValue("Stop Gluster service")
    String stopGlusterServices();

    @DefaultStringValue("There is no cluster supporting selected VM(s) architecture in selected Data Center")
    String noClusterSupportingArchitectureInDC();

    @DefaultStringValue("Selected Virtual Machines have to have same architecture.")
    String sameArchitectureRequired();

    @DefaultStringValue("Cannot change Cluster Compatibility Version when a VM is active. Please shutdown all VMs in the Cluster.")
    String cannotClusterVersionChangeWithActiveVm();
}
