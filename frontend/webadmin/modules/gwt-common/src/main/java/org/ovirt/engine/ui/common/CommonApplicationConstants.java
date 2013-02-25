package org.ovirt.engine.ui.common;

import com.google.gwt.i18n.client.Constants;

public interface CommonApplicationConstants extends Constants {

    @DefaultStringValue("")
    String empty();

    @DefaultStringValue("")
    String errorPopupCaption();

    @DefaultStringValue("Close")
    String closeButtonLabel();

    @DefaultStringValue("[N/A]")
    String unAvailablePropertyLabel();

    @DefaultStringValue("<br/>")
    String lineBreak();

    @DefaultStringValue("&nbsp;")
    String htmlNonBreakingSpace();

    @DefaultStringValue("&emsp;")
    String htmlTab();

    @DefaultStringValue(", ")
    String commaBreak();

    @DefaultStringValue(" and ")
    String andBreak();

    @DefaultStringValue(" ")
    String space();

    // Widgets

    @DefaultStringValue("Next >>")
    String actionTableNextPageButtonLabel();

    @DefaultStringValue("<< Prev")
    String actionTablePrevPageButtonLabel();

    @DefaultStringValue("This feature is not implemented in this version.")
    String featureNotImplementedMessage();

    @DefaultStringValue("This feature is not implemented but available in UserPortal for users assigned with PowerUser role.")
    String featureNotImplementedButAvailInUserPortalMessage();

    // Table columns

    @DefaultStringValue("Disk Activate/Deactivate while VM is running, is supported only for Clusters of version 3.1 and above")
    String diskHotPlugNotSupported();

    @DefaultStringValue("Disks Allocation:")
    String disksAllocation();

    @DefaultStringValue("Disk ")
    String diskNamePrefix();

    @DefaultStringValue("Storage")
    String singleDestinationStorage();

    @DefaultStringValue("Default Storage Domain")
    String defaultStorage();

    @DefaultStringValue(" and Quota")
    String singleQuota();

    // Model-bound widgets

    @DefaultStringValue("Boot Options")
    String runOncePopupBootOptionsLabel();

    @DefaultStringValue("Up")
    String bootSequenceUpButtonLabel();

    @DefaultStringValue("Down")
    String bootSequenceDownButtonLabel();

    @DefaultStringValue("Display Protocol")
    String runOncePopupDisplayProtocolLabel();

    @DefaultStringValue("Custom Properties")
    String runOncePopupCustomPropertiesLabel();

    @DefaultStringValue("VNC")
    String runOncePopupDisplayConsoleVncLabel();

    @DefaultStringValue("SPICE")
    String runOncePopupDisplayConsoleSpiceLabel();

    @DefaultStringValue("Run Stateless")
    String runOncePopupRunAsStatelessLabel();

    @DefaultStringValue("Start in Pause Mode")
    String runOncePopupRunAndPauseLabel();

    @DefaultStringValue("Linux Boot Options")
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

    @DefaultStringValue("Initial Run")
    String runOncePopupInitialRunLabel();

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

    @DefaultStringValue("Cluster")
    String makeTemplateClusterLabel();

    @DefaultStringValue("Quota")
    String makeTemplateQuotaLabel();

    @DefaultStringValue("Storage Domain")
    String makeTemplateStorageDomainLabel();

    @DefaultStringValue("Allow all users to access this Template")
    String makeTemplateIsTemplatePublicEditorLabel();

    @DefaultStringValue("Description")
    String virtualMachineSnapshotCreatePopupDescriptionLabel();

    @DefaultStringValue("Loading...")
    String loadingLabel();

    @DefaultStringValue("General")
    String generalLabel();

    @DefaultStringValue("Disks")
    String disksLabel();

    @DefaultStringValue("Statistics")
    String statistics();

    @DefaultStringValue("Guest Agent Data")
    String guestAgentData();

    @DefaultStringValue("Network Interfaces")
    String nicsLabel();

    @DefaultStringValue("Installed Applications")
    String applicationsLabel();

    @DefaultStringValue("Preview Mode")
    String previewModelLabel();

    @DefaultStringValue("Read-Only")
    String readonlyLabel();

    @DefaultStringValue("Current")
    String currentSnapshotLabel();

    @DefaultStringValue("Current (Previous)")
    String previousCurrentSnapshotLabel();

    @DefaultStringValue("Approve operation")
    String latchApproveOperationLabel();

    @DefaultStringValue("Remove permanently")
    String permanentlyRemoveLabel();

    @DefaultStringValue("Approve operation")
    String approveOperation();

    @DefaultStringValue("Force Remove")
    String forceRemove();

    // General
    @DefaultStringValue("Yes")
    String yes();

    @DefaultStringValue("No")
    String no();

    // Vm
    @DefaultStringValue("New VM")
    String newVm();

    @DefaultStringValue("Power Off")
    String powerOffVm();

    @DefaultStringValue("Shutdown")
    String shutDownVm();

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

    @DefaultStringValue("Disable strict user checking")
    String allowConsoleReconnect();

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

    @DefaultStringValue("Directory Domain")
    String domainVm();

    @DefaultStringValue("Time Zone")
    String timeZoneVm();

    @DefaultStringValue("Installed Applications")
    String installedAppsVm();

    @DefaultStringValue("Console User")
    String consoleConnectedUserVm();

    @DefaultStringValue("Logged-in User")
    String loggedInUserVm();

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

    @DefaultStringValue("Cluster")
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

    @DefaultStringValue("Total Virtual CPUs")
    String numOfVCPUs();

    @DefaultStringValue("Cores per Virtual Socket")
    String coresPerSocket();

    @DefaultStringValue("Virtual Sockets")
    String numOfSockets();

    @DefaultStringValue("Operating System")
    String osVmPopup();

    @DefaultStringValue("Stateless")
    String statelessVmPopup();

    @DefaultStringValue("Start in Pause Mode")
    String runAndPauseVmPopup();

    @DefaultStringValue("Delete protection")
    String deleteProtectionPopup();

    @DefaultStringValue("Smartcard enabled")
    String smartcardVmPopup();

    @DefaultStringValue("Pool")
    String poolVmPopup();

    @DefaultStringValue("Pool Type")
    String poolTypeVmPopup();

    @DefaultStringValue("Initial Run")
    String initialRunVmPopup();

    @DefaultStringValue("System")
    String systemVmPopup();

    @DefaultStringValue("Prestarted VMs")
    String prestartedVms();

    @DefaultStringValue("Domain")
    String domainVmPopup();

    @DefaultStringValue("Time Zone")
    String tzVmPopup();

    @DefaultStringValue("Console")
    String consoleVmPopup();

    @DefaultStringValue("Protocol")
    String protocolVmPopup();

    @DefaultStringValue("VNC Keyboard Layout")
    String vncKeyboardLayoutVmPopup();

    @DefaultStringValue("USB Support")
    String usbPolicyVmPopup();

    @DefaultStringValue("Monitors")
    String monitorsVmPopup();

    @DefaultStringValue("Host")
    String hostVmPopup();

    @DefaultStringValue("Any Host in Cluster")
    String anyHostInClusterVmPopup();

    @DefaultStringValue("Run VM on the selected host (no migration allowed)")
    String runOnSelectedHostVmPopup();

    @DefaultStringValue("Use Host CPU")
    String useHostCpu();

    @DefaultStringValue("Allow VM migration only upon Administrator specific request (system will not trigger automatic migration of this VM)")
    String allowMigrationOnlyAdminVmPopup();

    @DefaultStringValue("Highly Available")
    String highlyAvailableVmPopup();

    @DefaultStringValue("Watchdog Action")
    String watchdogAction();

    @DefaultStringValue("Watchdog Model")
    String watchdogModel();

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

    @DefaultStringValue("Migration Options:")
    String runMigrationOptionsVmPopup();

    @DefaultStringValue("Priority for Run/Migration queue:")
    String priorForRunMigrationQueueVmPopup();

    @DefaultStringValue("Watchdog")
    String watchdog();

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

    @DefaultStringValue("Activate")
    String activateInterface();

    @DefaultStringValue("Deactivate")
    String deactivateInterface();

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

    @DefaultStringValue("Port Mirroring")
    String portMirroring();

    @DefaultStringValue("Plugged")
    String plugged();

    @DefaultStringValue("Unplugged")
    String unplugged();

    @DefaultStringValue("Enabled")
    String portMirroringEnabled();

    @DefaultStringValue("(Mbps)")
    String mbps();

    @DefaultStringValue("(Pkts)")
    String pkts();

    @DefaultStringValue("MB")
    String mb();

    // Event
    @DefaultStringValue("Time")
    String timeEvent();

    @DefaultStringValue("Message")
    String messageEvent();

    @DefaultStringValue("Correlation Id")
    String correltaionIdEvent();

    @DefaultStringValue("Origin")
    String originEvent();

    @DefaultStringValue("Custom Event Id")
    String customEventIdEvent();

    @DefaultStringValue("ID")
    String idEvent();

    @DefaultStringValue("Until")
    String untilEndTime();

    @DefaultStringValue("until")
    String until();

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

    @DefaultStringValue("Active")
    String active();

    @DefaultStringValue("Inactive")
    String inactive();

    @DefaultStringValue("Move")
    String moveDisk();

    @DefaultStringValue("Scan Alignment")
    String getDiskAlignment();

    @DefaultStringValue("Name")
    String nameDisk();

    @DefaultStringValue("Actual Size")
    String sizeDisk();

    @DefaultStringValue("Storage Domain")
    String storageDomainDisk();

    @DefaultStringValue("Storage Domain(s)")
    String storageDomainsDisk();

    @DefaultStringValue("Domains")
    String storageDomainsLabelDisk();

    @DefaultStringValue("Type")
    String typeDisk();

    @DefaultStringValue("Allocation Policy")
    String allocationDisk();

    @DefaultStringValue("Interface")
    String interfaceDisk();

    @DefaultStringValue("Status")
    String statusDisk();

    @DefaultStringValue("Creation Date")
    String creationDateDisk();

    @DefaultStringValue("Description")
    String descriptionDisk();

    @DefaultStringValue("Format")
    String formatDisk();

    @DefaultStringValue("Copy")
    String copyDisk();

    @DefaultStringValue("Free Space")
    String freeSpaceDisk();

    @DefaultStringValue("Alias")
    String aliasDisk();

    @DefaultStringValue("Virtual Size")
    String provisionedSizeDisk();

    @DefaultStringValue("Source")
    String sourceDisk();

    @DefaultStringValue("Target")
    String targetDisk();

    @DefaultStringValue("Quota")
    String quotaDisk();

    @DefaultStringValue("Destination")
    String destDisk();

    @DefaultStringValue("Alignment")
    String diskAlignment();

    @DefaultStringValue("Attached To")
    String attachedToDisk();

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

    @DefaultStringValue("Directory Domain")
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

    @DefaultStringValue("Port Mirroring")
    String portMirroringNetworkIntefacePopup();

    @DefaultStringValue("Specify custom MAC address")
    String specipyCustMacNetworkIntefacePopup();

    @DefaultStringValue("Card Status")
    String cardStatusNetworkInteface();

    @DefaultStringValue("Plugged")
    String pluggedNetworkInteface();

    @DefaultStringValue("Unplugged")
    String unpluggedNetworkInteface();

    @DefaultStringValue("Link State")
    String linkStateNetworkInteface();

    @DefaultStringValue("Up")
    String linkedNetworkInteface();

    @DefaultStringValue("Down")
    String unlinkedNetworkInteface();

    @DefaultStringValue("Number of VMs")
    String numOfVmsPoolPopup();

    @DefaultStringValue("Prestarted")
    String prestartedPoolPopup();

    // VM disk
    @DefaultStringValue("Size(GB)")
    String sizeVmDiskPopup();

    @DefaultStringValue("Storage Domain")
    String storageDomainVmDiskPopup();

    @DefaultStringValue("Use Host")
    String hostVmDiskPopup();

    @DefaultStringValue("Alias")
    String aliasVmDiskPopup();

    @DefaultStringValue("Data Center")
    String dcVmDiskPopup();

    @DefaultStringValue("Quota")
    String quotaVmDiskPopup();

    @DefaultStringValue("Description")
    String descriptionVmDiskPopup();

    @DefaultStringValue("Attach Disk")
    String attachDiskVmDiskPopup();

    @DefaultStringValue("Interface")
    String interfaceVmDiskPopup();

    @DefaultStringValue("Storage Type")
    String storageTypeVmDiskPopup();

    @DefaultStringValue("Wipe After Delete")
    String wipeAfterDeleteVmDiskPopup();

    @DefaultStringValue("Is Bootable")
    String isBootableVmDiskPopup();

    @DefaultStringValue("Is Shareable")
    String isShareableVmDiskPopup();

    @DefaultStringValue("Allow Privileged SCSI I/O")
    String isSgIoUnfilteredEditor();

    @DefaultStringValue("Activate")
    String activateVmDiskPopup();

    @DefaultStringValue("Alias")
    String aliasVmDiskTable();

    @DefaultStringValue("Description")
    String descriptionVmDiskTable();

    @DefaultStringValue("ID")
    String idVmDiskTable();

    @DefaultStringValue("Virtual Size")
    String provisionedSizeVmDiskTable();

    @DefaultStringValue("Actual Size")
    String sizeVmDiskTable();

    @DefaultStringValue("Storage Domain")
    String storageDomainVmDiskTable();

    @DefaultStringValue("Specific User/Group")
    String specificUserGroupPermission();

    @DefaultStringValue("Everyone")
    String everyonePermission();

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

    @DefaultStringValue("Shareable")
    String shareable();

    @DefaultStringValue("Bootable")
    String bootable();

    @DefaultStringValue("Internal")
    String internalDisk();

    @DefaultStringValue("External (Direct Lun)")
    String externalDisk();

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

    // San Storage
    @DefaultStringValue("Target Name")
    String targetNameSanStorage();

    @DefaultStringValue("Address")
    String addressSanStorage();

    @DefaultStringValue("Port")
    String portSanStorage();

    @DefaultStringValue("LUN ID")
    String lunIdSanStorage();

    @DefaultStringValue("Dev. Size")
    String devSizeSanStorage();

    @DefaultStringValue("#path")
    String pathSanStorage();

    @DefaultStringValue("Vendor ID")
    String vendorIdSanStorage();

    @DefaultStringValue("Product ID")
    String productIdSanStorage();

    @DefaultStringValue("Serial")
    String serialSanStorage();

    @DefaultStringValue("Name")
    String nameSanImStorage();

    @DefaultStringValue("Format")
    String formatSanImStorage();

    @DefaultStringValue("N/A")
    String notAvailableLabel();

    @DefaultStringValue("CPU Pinning topology")
    String cpuPinningLabel();

    @DefaultStringValue("Format: v#p[_v#p]</br>Examples:</br>&#149;0#0 => pin vCPU 0 to pCPU 0</br>&#149;0#0_1#3 => pin vCPU 0 to pCPU 0 and pin vCPU 1 to pCPU 3</br>&#149;1#1-4,^2 => pin vCPU 1 to pCPU set 1 to 4, excluding 2</br>")
    String cpuPinningLabelExplanation();

    @DefaultStringValue("Migration is NOT currently supported using SPICE Native USB redirection on cluster version lower than 3.2")
    String nativeUsbSupportWarning();

    @DefaultStringValue("LUN is already in use - attaching it could cause data corruption.")
    String lunAlreadyUsedWarning();

    @DefaultStringValue("Advanced Parameters")
    String advancedParameters();

    @DefaultStringValue("General")
    String initialRunGeneral();

    @DefaultStringValue("Windows")
    String initialRunWindows();

    @DefaultStringValue("* It is recommended to keep the default values in the fields below unchanged.")
    String advancedOptionsLabel();

    @DefaultStringValue("Bootable")
    String bootableDisk();

    @DefaultStringValue("All")
    String allDisksLabel();

    @DefaultStringValue("Images")
    String imageDisksLabel();

    @DefaultStringValue("Direct LUN")
    String lunDisksLabel();

    @DefaultStringValue("Current")
    String currentQuota();

    @DefaultStringValue("Name")
    String elementName();

    @DefaultStringValue("Assign Quota")
    String assignQuota();

    @DefaultStringValue("Increase number of VMs in pool by")
    String increaseNumberOfVMsInPoolBy();

    @DefaultStringValue("VMs")
    String vms();

    @DefaultStringValue("Maximum number of VMs per user")
    String maxAssignedVmsPerUser();

    @DefaultStringValue("SPM Priority")
    String spmPriority();

    @DefaultStringValue("Refresh Rate")
    String refreshRate();

    // Network Host
    @DefaultStringValue("Network Device Status")
    String statusNetworkHost();

    @DefaultStringValue("Network Device")
    String nicNetworkHost();

    @DefaultStringValue("Network Device Speed")
    String speedNetworkHost();

    @DefaultStringValue("Network Device Rx")
    String rxNetworkHost();

    @DefaultStringValue("Network Device Tx")
    String txNetworkHost();

    // Network VM
    @DefaultStringValue("Vnic Status")
    String vnicStatusNetworkVM();

    @DefaultStringValue("Vnic")
    String vnicNetworkVM();

    @DefaultStringValue("Nic Rx")
    String rxNetworkVM();

    @DefaultStringValue("Nic Tx")
    String txNetworkVM();

    // Network Template
    @DefaultStringValue("Vnic")
    String vnicNetworkTemplate();

    @DefaultStringValue("Show Quota Distribution")
    String showQuotaDistribution();

    @DefaultStringValue("Hide Quota Distribution")
    String hideQuotaDistribution();

    // Vm Guest Agent
    @DefaultStringValue("Name")
    String nameVmGuestAgent();

    @DefaultStringValue("IPv4")
    String ipv4VmGuestAgent();

    @DefaultStringValue("IPv6")
    String ipv6VmGuestAgent();

    @DefaultStringValue("MAC")
    String macVmGuestAgent();

    @DefaultStringValue("Clone")
    String cloneVM();

    @DefaultStringValue("Collapse Snapshots")
    String collapseSnapshots();

    @DefaultStringValue("High Priority Only")
    String highPriorityOnly();

    @DefaultStringValue("Virt")
    String virt();

    @DefaultStringValue("Gluster")
    String gluster();

    // Console popup view

    @DefaultStringValue("SPICE")
    String spice();

    @DefaultStringValue("Auto")
    String auto();

    @DefaultStringValue("Native client")
    String nativeClient();

    @DefaultStringValue("Browser plugin")
    String browserPlugin();

    @DefaultStringValue("noVNC")
    String noVnc();

    @DefaultStringValue("Pass Ctrl-Alt-Del to virtual machine")
    String ctrlAltDel();

    @DefaultStringValue("Enable USB Auto-Share")
    String usbAutoshare();

    @DefaultStringValue("Open in Full Screen")
    String openInFullScreen();

    @DefaultStringValue("Enable SPICE Proxy")
    String enableSpiceProxy();

    @DefaultStringValue("Ctrl-Alt-Del is always passed for this client OS")
    String ctrlAltDeletIsNotSupportedOnWindows();

    @DefaultStringValue("Your browser doesn't support SPICE plugin")
    String spicePluginNotSupportedByBrowser();

    @DefaultStringValue("Your browser doesn't support RDP plugin")
    String rdpPluginNotSupportedByBrowser();

    @DefaultStringValue("No SPICE proxy defined on system level")
    String spiceProxyCanBeEnabledOnlyWhenDefined();

    @DefaultStringValue("Enable WAN Options")
    String enableWanOptions();

    @DefaultStringValue("Disable smartcard")
    String disableSmartcard();

    @DefaultStringValue("RDP Options")
    String rdpOptions();

    @DefaultStringValue("Use Local Drives")
    String useLocalDrives();

    @DefaultStringValue("Remote Desktop")
    String remoteDesktop();

    @DefaultStringValue("VNC")
    String vnc();

    @DefaultStringValue("Websockets Proxy must be configured in the engine.")
    String webSocketProxyNotSet();

    @DefaultStringValue("SPICE Options")
    String spiceOptions();

    @DefaultStringValue("If there is a SPICE plugin installed in your browser, it is used for invoking the console session. Otherwise SPICE configuration file is downloaded.")
    String spiceInvokeAuto();

    @DefaultStringValue("Downloads a SPICE configuration file to be opened by a SPICE client installed on your system.")
    String spiceInvokeNative();

    @DefaultStringValue("Uses SPICE browser plugin for invoking console session. For this you must have SPICE console plugin installed in your browser.")
    String spiceInvokePlugin();

    @DefaultStringValue("Console Invocation")
    String consoleInvocation();

    @DefaultStringValue("VNC console access is not supported from the user portal.<br/>" +
            "Please ask the administrator to configure this " +
            "virtual machine to use SPICE for console access.")
    String vncNotSupportedMsg();

    @DefaultStringValue("SPICE isn't available for this machine. For enabling it, change the VM console protocol.")
    String spiceNotAvailable();

    @DefaultStringValue("RDP isn't available for this machine.")
    String rdpNotAvailable();

    @DefaultStringValue("VNC isn't available for this machine. For enabling it, change the VM console protocol.")
    String vncNotAvailable();

    @DefaultStringValue("Your browser/platform does not support console opening")
    String browserNotSupportedMsg();

    @DefaultStringValue("Console Options")
    String consoleOptions();

    @DefaultStringValue("Details")
    String details();

    @DefaultStringValue("Not Configured")
    String notConfigured();

    @DefaultStringValue("No items to display")
    String noItemsToDisplay();

    @DefaultStringValue("Are you sure you want to remove the following items?")
    String removeConfirmationPopupMessage();

    @DefaultStringValue("Soundcard enabled")
    String soundcardEnabled();

    @DefaultStringValue("Optimized for")
    String optimizedFor();
}
