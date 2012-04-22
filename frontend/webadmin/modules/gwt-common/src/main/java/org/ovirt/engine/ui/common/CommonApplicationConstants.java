package org.ovirt.engine.ui.common;

import com.google.gwt.i18n.client.Constants;

public interface CommonApplicationConstants extends Constants {

    @DefaultStringValue("")
    String empty();

    @DefaultStringValue("Oops!")
    String errorPopupCaption();

    @DefaultStringValue("Close")
    String closeButtonLabel();

    @DefaultStringValue("[N/A]")
    String unAvailablePropertyLabel();

    // Widgets

    @DefaultStringValue("Next >>")
    String actionTableNextPageButtonLabel();

    @DefaultStringValue("<< Prev")
    String actionTablePrevPageButtonLabel();

    // Table columns

    @DefaultStringValue("Disk Activate/Deactivate while VM is running, is supported only for Clusters of version 3.1 and above")
    String diskHotPlugNotSupported();

    @DefaultStringValue("Disks Allocation:")
    String disksAllocation();

    @DefaultStringValue("Disk ")
    String diskNamePrefix();

    @DefaultStringValue("Single Destination Storage")
    String singleDestinationStorage();

    @DefaultStringValue(" and Quota")
    String singleQuota();

    // Model-bound widgets

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

    @DefaultStringValue("Name")
    String makeTemplatePopupNameLabel();

    @DefaultStringValue("Description")
    String makeTemplatePopupDescriptionLabel();

    @DefaultStringValue("Host Cluster")
    String makeTemplateClusterLabel();

    @DefaultStringValue("Quota")
    String makeTemplateQuotaLabel();

    @DefaultStringValue("Storage Domain")
    String makeTemplateStorageDomainLabel();

    @DefaultStringValue("Make Private")
    String makeTemplateIsTemplatePrivateEditorLabel();

    @DefaultStringValue("Description")
    String virtualMachineSnapshotCreatePopupDescriptionLabel();

    @DefaultStringValue("Loading...")
    String loadingLabel();

    @DefaultStringValue("General")
    String generalLabel();

    @DefaultStringValue("Virtual Disks")
    String disksLabel();

    @DefaultStringValue("Network Interfaces")
    String nicsLabel();

    @DefaultStringValue("Installed Applications")
    String applicationsLabel();

    @DefaultStringValue("Preview Mode")
    String previewModelLabel();

    @DefaultStringValue("%1$s (%2$s Socket(s), %3$s Core(s) per Socket)")
    String cpuInfoLabel();

    @DefaultStringValue("Read-Only")
    String readonlyLabel();

    @DefaultStringValue("Current")
    String currentSnapshotLabel();

    @DefaultStringValue("Current (Previous)")
    String previousCurrentSnapshotLabel();

    @DefaultStringValue("Clone VM from Snapshot is supported only for Clusters of version %1$s and above")
    String cloneVmNotSupported();

    @DefaultStringValue("The VM contains disks in illegal status")
    String illegalDisksInVm();

    @DefaultStringValue("Approve operation")
    String latchApproveOperationLabel();

    @DefaultStringValue("Permanently remove from storage")
    String permanentlyRemoveLabel();

    @DefaultStringValue("Approve operation")
    String approveOperation();

    // General
    @DefaultStringValue("Yes")
    String yes();

    @DefaultStringValue("No")
    String no();

    // Vm
    @DefaultStringValue("Name")
    String nameVm();

    @DefaultStringValue("Alias")
    String aliasVm();

    @DefaultStringValue("Description")
    String descriptionVm();

    @DefaultStringValue("Template")
    String templateVm();

    @DefaultStringValue("Operating System")
    String osVm();

    @DefaultStringValue("Default Display Type")
    String defaultDisplayTypeVm();

    @DefaultStringValue("Priority")
    String priorityVm();

    @DefaultStringValue("Defined Memory")
    String definedMemoryVm();

    @DefaultStringValue("Physical Memory Guaranteed")
    String physMemGauranteedVm();

    @DefaultStringValue("Number of CPU Cores")
    String numOfCpuCoresVm();

    @DefaultStringValue("Highly Available")
    String highlyAvailableVm();

    @DefaultStringValue("Number of Monitors")
    String numOfMonitorsVm();

    @DefaultStringValue("USB Policy")
    String usbPolicyVm();

    @DefaultStringValue("Cluster Compatibility Version")
    String clusterCompatibilityVersionVm();

    @DefaultStringValue("Quota")
    String quotaVm();

    @DefaultStringValue("Origin")
    String originVm();

    @DefaultStringValue("Run On")
    String runOnVm();

    @DefaultStringValue("Custom Properties")
    String customPropertiesVm();

    @DefaultStringValue("Domain")
    String domainVm();

    @DefaultStringValue("Time Zone")
    String timeZoneVm();

    @DefaultStringValue("Installed Applications")
    String installedAppsVm();

    // VM popup
    @DefaultStringValue("High Availability")
    String highAvailVmPopup();

    @DefaultStringValue("Resource Allocation")
    String resourceAllocVmPopup();

    @DefaultStringValue("Boot Options")
    String bootOptionsVmPopup();

    @DefaultStringValue("General")
    String GeneralVmPopup();

    @DefaultStringValue("Data Center")
    String dcVmPopup();

    @DefaultStringValue("Host Cluster")
    String hostClusterVmPopup();

    @DefaultStringValue("Quota")
    String quotaVmPopup();

    @DefaultStringValue("Name")
    String nameVmPopup();

    @DefaultStringValue("Description")
    String descriptionVmPopup();

    @DefaultStringValue("Based on Template")
    String basedOnTemplateVmPopup();

    @DefaultStringValue("Memory Size")
    String memSizeVmPopup();

    @DefaultStringValue("Total Cores")
    String totalCoresVmPopup();

    @DefaultStringValue("CPU Sockets")
    String cpuSocketsVmPopup();

    @DefaultStringValue("Operating System")
    String osVmPopup();

    @DefaultStringValue("Stateless")
    String statelessVmPopup();

    @DefaultStringValue("Pool")
    String poolVmPopup();

    @DefaultStringValue("Pool Type")
    String poolTypeVmPopup();

    @DefaultStringValue("Assigned VMs")
    String assignedVmsVmPopup();

    @DefaultStringValue("Windows Sysprep")
    String windowsSysprepVmPopup();

    @DefaultStringValue("Domain")
    String domainVmPopup();

    @DefaultStringValue("Time Zone")
    String tzVmPopup();

    @DefaultStringValue("Console")
    String consoleVmPopup();

    @DefaultStringValue("Protocol")
    String protocolVmPopup();

    @DefaultStringValue("USB Policy")
    String usbPolicyVmPopup();

    @DefaultStringValue("Monitors")
    String monitorsVmPopup();

    @DefaultStringValue("Host")
    String hostVmPopup();

    @DefaultStringValue("Any Host in Cluster")
    String anyHostInClusterVmPopup();

    @DefaultStringValue("Run VM on the selected host (no migration allowed)")
    String runOnSelectedHostVmPopup();

    @DefaultStringValue("Allow VM migration only upon Administrator specific request (system will not trigger automatic migration of this VM)")
    String allowMigrationOnlyAdminVmPopup();

    @DefaultStringValue("Highly Available")
    String highlyAvailableVmPopup();

    @DefaultStringValue("Template Provisioning")
    String templateProvisVmPopup();

    @DefaultStringValue("Thin")
    String thinVmPopup();

    @DefaultStringValue("Clone")
    String cloneVmPopup();

    @DefaultStringValue("Physical Memory Guaranteed")
    String physMemGuarVmPopup();

    @DefaultStringValue("First Device")
    String firstDeviceVmPopup();

    @DefaultStringValue("Second Device")
    String secondDeviceVmPopup();

    @DefaultStringValue("kernel path")
    String kernelPathVmPopup();

    @DefaultStringValue("initrd path")
    String initrdPathVmPopup();

    @DefaultStringValue("kernel parameters")
    String kernelParamsVmPopup();

    @DefaultStringValue("Custom Properties")
    String customPropsVmPopup();

    @DefaultStringValue("Run On:")
    String runOnVmPopup();

    @DefaultStringValue("Run/Migration Options:")
    String runMigrationOptionsVmPopup();

    @DefaultStringValue("Priority for Run/Migration queue:")
    String priorForRunMigrationQueueVmPopup();

    @DefaultStringValue("Memory Allocation:")
    String memAllocVmPopup();

    @DefaultStringValue("Storage Allocation:")
    String storageAllocVmPopup();

    @DefaultStringValue("(Available only when a template is selected)")
    String availOnlyTemplSelectedVmPopup();

    @DefaultStringValue("Boot Sequence:")
    String bootSequenceVmPopup();

    @DefaultStringValue("Attach CD")
    String attachCdVmPopup();

    @DefaultStringValue("Linux Boot Options:")
    String linuxBootOptionsVmPopup();

    @DefaultStringValue("Specific")
    String specificVmPopup();

    // Permissions
    @DefaultStringValue("Inherited Permission")
    String inheritedPermission();

    @DefaultStringValue("User")
    String userPermission();

    @DefaultStringValue("Role")
    String rolePermission();

    @DefaultStringValue("Add")
    String addPermission();

    @DefaultStringValue("Remove")
    String removePermission();

    @DefaultStringValue("Object")
    String objectPermission();

    // Interface
    @DefaultStringValue("New")
    String newInterface();

    @DefaultStringValue("Edit")
    String editInterface();

    @DefaultStringValue("Remove")
    String removeInterface();

    @DefaultStringValue("Name")
    String nameInterface();

    @DefaultStringValue("Network Name")
    String networkNameInterface();

    @DefaultStringValue("Type")
    String typeInterface();

    @DefaultStringValue("MAC")
    String macInterface();

    @DefaultStringValue("Speed")
    String speedInterface();

    @DefaultStringValue("Rx")
    String rxInterface();

    @DefaultStringValue("Tx")
    String txInterface();

    @DefaultStringValue("Drops")
    String dropsInterface();

    @DefaultStringValue("(Mbps)")
    String mbps();

    @DefaultStringValue("(Pkts)")
    String pkts();

    // Event
    @DefaultStringValue("Time")
    String timeEvent();

    @DefaultStringValue("Message")
    String messageEvent();

    // Snapshot
    @DefaultStringValue("Date")
    String dateSnapshot();

    @DefaultStringValue("Description")
    String descriptionSnapshot();

    @DefaultStringValue("Status")
    String statusSnapshot();

    @DefaultStringValue("Disks")
    String disksSnapshot();

    @DefaultStringValue("Create")
    String createSnapshot();

    @DefaultStringValue("Preview")
    String previewSnapshot();

    @DefaultStringValue("Commit")
    String commitSnapshot();

    @DefaultStringValue("Undo")
    String undoSnapshot();

    @DefaultStringValue("Delete")
    String deleteSnapshot();

    @DefaultStringValue("Clone")
    String cloneSnapshot();

    @DefaultStringValue("Installed Applications")
    String installedAppsSnapshot();

    @DefaultStringValue("Name")
    String nameSnapshot();

    // Disk
    @DefaultStringValue("Add")
    String addDisk();

    @DefaultStringValue("Edit")
    String editDisk();

    @DefaultStringValue("Remove")
    String removeDisk();

    @DefaultStringValue("Activate")
    String activateDisk();

    @DefaultStringValue("Deactivate")
    String deactivateDisk();

    @DefaultStringValue("Move")
    String moveDisk();

    @DefaultStringValue("Name")
    String nameDisk();

    @DefaultStringValue("Size")
    String sizeDisk();

    @DefaultStringValue("Storage Domain")
    String storageDomainDisk();

    @DefaultStringValue("Type")
    String typeDisk();

    @DefaultStringValue("Allocation")
    String allocationDisk();

    @DefaultStringValue("Interface")
    String interfaceDisk();

    @DefaultStringValue("Status")
    String statusDisk();

    @DefaultStringValue("Creation Date")
    String creationDateDisk();

    @DefaultStringValue("Format")
    String formatDisk();

    @DefaultStringValue("Copy")
    String copyDisk();

    @DefaultStringValue("Free Space")
    String freeSpaceDisk();

    @DefaultStringValue("Alias")
    String aliasDisk();

    @DefaultStringValue("Provisioned Size")
    String provisionedSizeDisk();

    @DefaultStringValue("Source")
    String sourceDisk();

    @DefaultStringValue("Target")
    String targetDisk();

    @DefaultStringValue("Quota")
    String quotaDisk();

    @DefaultStringValue("Destination")
    String destDisk();

    // Application list
    @DefaultStringValue("Installed Applications")
    String installedApp();

    // Template- general
    @DefaultStringValue("Name")
    String nameTemplateGeneral();

    @DefaultStringValue("Description")
    String descriptionTemplateGeneral();

    @DefaultStringValue("Host Cluster")
    String hostClusterTemplateGeneral();

    @DefaultStringValue("Operating System")
    String osTemplateGeneral();

    @DefaultStringValue("Default Display Type")
    String defaultDisTypeTemplateGeneral();

    @DefaultStringValue("Defined Memory")
    String definedMemTemplateGeneral();

    @DefaultStringValue("Number of CPU Cores")
    String numOfCpuCoresTemplateGeneral();

    @DefaultStringValue("Number of Monitors")
    String numOfMonitorsTemplateGeneral();

    @DefaultStringValue("Highly Available")
    String highlyAvailTemplateGeneral();

    @DefaultStringValue("Priority")
    String priorityTemplateGeneral();

    @DefaultStringValue("USB Policy")
    String usbPolicyTemplateGeneral();

    @DefaultStringValue("Origin")
    String originTemplateGeneral();

    @DefaultStringValue("Is Stateless")
    String isStatelessTemplateGeneral();

    @DefaultStringValue("Domain")
    String domainTemplateGeneral();

    @DefaultStringValue("Time Zone")
    String tzTemplateGeneral();

    @DefaultStringValue("Quota")
    String quotaTemplateGeneral();

    // Permissions popup
    @DefaultStringValue("GO")
    String goPermissionsPopup();

    // Network interface popup
    @DefaultStringValue("Name")
    String nameNetworkIntefacePopup();

    @DefaultStringValue("Network")
    String networkNetworkIntefacePopup();

    @DefaultStringValue("Type")
    String typeNetworkIntefacePopup();

    @DefaultStringValue("Specify custom MAC address")
    String specipyCustMacNetworkIntefacePopup();

    // Pool popup
    @DefaultStringValue("Do not migrate VM")
    String dontMigrageVmPoolPopup();

    @DefaultStringValue("Number of VMs")
    String numOfVmsPoolPopup();

    @DefaultStringValue("Number of VMs to add")
    String numOfVmsToAddPoolPopup();

    @DefaultStringValue("Add VMs")
    String addVmsPoolPopup();

    // VM disk
    @DefaultStringValue("Size(GB)")
    String sizeVmDiskPopup();

    @DefaultStringValue("Storage Domain")
    String storageDomainVmDiskPopup();

    @DefaultStringValue("Alias")
    String aliasVmDiskPopup();

    @DefaultStringValue("Quota")
    String quotaVmDiskPopup();

    @DefaultStringValue("Attach Disk")
    String attachDiskVmDiskPopup();

    @DefaultStringValue("Interface")
    String interfaceVmDiskPopup();

    @DefaultStringValue("Format")
    String formatVmDiskPopup();

    @DefaultStringValue("Wipe after delete")
    String wipeAfterDeleteVmDiskPopup();

    @DefaultStringValue("Is bootable")
    String isBootableVmDiskPopup();

    @DefaultStringValue("Activate")
    String activateVmDiskPopup();

    @DefaultStringValue("Alias")
    String aliasVmDiskTable();

    @DefaultStringValue("Provisioned Size")
    String provisionedSizeVmDiskTable();

    @DefaultStringValue("Size")
    String sizeVmDiskTable();

    @DefaultStringValue("Storage Domain")
    String storageDomainVmDiskTable();

    // Permissions popup
    @DefaultStringValue("Role to Assign:")
    String roleToAssignPermissionsPopup();

    // Interface popup
    @DefaultStringValue("Example:")
    String exampleInterfacePopup();

    // Permissions popup
    @DefaultStringValue("First Name")
    String firsNamePermissionsPopup();

    @DefaultStringValue("Last Name")
    String lastNamePermissionsPopup();

    @DefaultStringValue("User Name")
    String userNamePermissionsPopup();

    @DefaultStringValue("Search:")
    String searchPermissionsPopup();

    // Pool General
    @DefaultStringValue("Name")
    String namePoolGeneral();

    @DefaultStringValue("Description")
    String descriptionPoolGeneral();

    @DefaultStringValue("Template")
    String templatePoolGeneral();

    @DefaultStringValue("Operating System")
    String osPoolGeneral();

    @DefaultStringValue("Default Display Type")
    String defaultDisplayTypePoolGeneral();

    @DefaultStringValue("Defined Memory")
    String definedMemPoolGeneral();

    @DefaultStringValue("Physical Memory Guaranteed")
    String physMemGaurPoolGeneral();

    @DefaultStringValue("Number of CPU Cores")
    String numOfCpuCoresPoolGeneral();

    @DefaultStringValue("Number of Monitors")
    String numOfMonitorsPoolGeneral();

    @DefaultStringValue("USB Policy")
    String usbPolicyPoolGeneral();

    @DefaultStringValue("Resides on Storage Domain")
    String residesOnSDPoolGeneral();

    @DefaultStringValue("Origin")
    String originPoolGeneral();

    @DefaultStringValue("Is Stateless")
    String isStatelessPoolGeneral();

    @DefaultStringValue("Run On")
    String runOnPoolGeneral();

    @DefaultStringValue("Domain")
    String domainPoolGeneral();

    @DefaultStringValue("Time Zone")
    String tzPoolGeneral();

    // Action table
    @DefaultStringValue("selected")
    String selectedActionTable();

    // Task
    @DefaultStringValue("Status")
    String statusTask();

    @DefaultStringValue("Time")
    String timeTask();

    @DefaultStringValue("Description")
    String descriptionTask();

    // Tasks Tree
    @DefaultStringValue("Loading...")
    String loadingTaskTree();
}
