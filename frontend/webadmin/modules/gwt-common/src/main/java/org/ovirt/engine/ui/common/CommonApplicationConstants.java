package org.ovirt.engine.ui.common;

import com.google.gwt.i18n.client.Constants;

public interface CommonApplicationConstants extends Constants {

    @DefaultStringValue("")
    String empty();

    @DefaultStringValue("Please select an item...")
    String emptyListBoxText();

    @DefaultStringValue("http://www.ovirt.org")
    String vendorUrl();

    @DefaultStringValue("Operation Canceled")
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

    @DefaultStringValue("Create as a Template Sub Version")
    String createAsSubTemplate();

    @DefaultStringValue("Root Template")
    String rootTemplate();

    @DefaultStringValue("Sub Version Name")
    String templateVersionName();

    @DefaultStringValue("Template Sub Version")
    String templateSubVersion();

    @DefaultStringValue("latest")
    String latest();

    @DefaultStringValue("base template")
    String baseTemplate();

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

    @DefaultStringValue("To enable Sysprep, attach the \"[sysprep]\" Floppy to this VM.")
    String runOnceSysPrepToEnableLabel();

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

    @DefaultStringValue("Use Cloud-Init")
    String runOncePopupCloudInitLabel();

    @DefaultStringValue("Input Locale")
    String inputLocaleLabel();

    @DefaultStringValue("UI Language")
    String uiLanguageLabel();

    @DefaultStringValue("System Locale")
    String systemLocaleLabel();

    @DefaultStringValue("User Locale")
    String userLocaleLabel();

    @DefaultStringValue("Custom Locale")
    String customLocaleLabel();

    @DefaultStringValue("Sysprep")
    String sysprepLabel();

    @DefaultStringValue("Active Directory OU")
    String activeDirectoryOU();

    @DefaultStringValue("This field will map to MachineObjectOU within Sysprep")
    String activeDirectoryOUToolTip();

    @DefaultStringValue("User Name")
    String cloudInitUserNameLabel();

    @DefaultStringValue("VM Hostname")
    String cloudInitHostnameLabel();

    @DefaultStringValue("Organization Name")
    String sysprepOrgNameLabel();

    @DefaultStringValue("Authentication")
    String cloudInitAuthenticationLabel();

    @DefaultStringValue("SSH Authorized Keys")
    String cloudInitAuthorizedKeysLabel();

    @DefaultStringValue("Regenerate SSH Keys")
    String cloudInitRegenerateKeysLabel();

    @DefaultStringValue("Configure Time Zone")
    String cloudInitConfigureTimeZoneLabel();

    @DefaultStringValue("Time Zone")
    String cloudInitTimeZoneLabel();

    @DefaultStringValue("Password")
    String cloudInitRootPasswordLabel();

    @DefaultStringValue("Verify Password")
    String cloudInitRootPasswordVerificationLabel();

    @DefaultStringValue("Admin Password")
    String sysprepAdminPasswordLabel();

    @DefaultStringValue("Verify Admin Password")
    String sysprepAdminPasswordVerificationLabel();

    @DefaultStringValue("Networks")
    String cloudInitNetworskLabel();

    @DefaultStringValue("Network")
    String cloudInitNetworkLabel();

    @DefaultStringValue("Select network above")
    String cloudInitNetworkSelectLabel();

    @DefaultStringValue("Boot Protocol")
    String cloudInitNetworkBootProtocolLabel();

    @DefaultStringValue("IP Address")
    String cloudInitNetworkIpAddressLabel();

    @DefaultStringValue("Netmask")
    String cloudInitNetworkNetmaskLabel();

    @DefaultStringValue("Gateway")
    String cloudInitNetworkGatewayLabel();

    @DefaultStringValue("Start on Boot")
    String cloudInitNetworkStartOnBootLabel();

    @DefaultStringValue("DNS Servers")
    String cloudInitDnsServersLabel();

    @DefaultStringValue("DNS Search Domains")
    String cloudInitDnsSearchDomainsLabel();

    @DefaultStringValue("Custom Script")
    String customScriptLabel();

    @DefaultStringValue("File Attachment")
    String cloudInitAttachmentLabel();

    @DefaultStringValue("Select pathname above")
    String cloudInitAttachmentSelectLabel();

    @DefaultStringValue("Content Type")
    String cloudInitAttachmentTypeLabel();

    @DefaultStringValue("Content")
    String cloudInitAttachmentContentLabel();

    @DefaultStringValue("Add new")
    String cloudInitObjectAddLabel();

    @DefaultStringValue("Remove selected")
    String cloudInitObjectRemoveLabel();

    @DefaultStringValue("Use already configured password")
    String vmInitPasswordSetLabel();

    @DefaultStringValue("Password is set, uncheck to change password")
    String vmInitPasswordSetToolTip();

    @DefaultStringValue("Password is not set")
    String vmInitPasswordNotSetToolTip();

    @DefaultStringValue("Enter the hostname to be assigned to the guest")
    String cloudInitHostnameToolTip();

    @DefaultStringValue("Add SSH keys (separated by newlines) to be added to the root user's authorized_keys file")
    String cloudInitAuthorizedKeysToolTip();

    @DefaultStringValue("Add Script that will run at startup")
    String customScriptToolTip();

    @DefaultStringValue("Regenerate system SSH keys (typically in /etc/ssh)")
    String cloudInitRegenerateKeysToolTip();

    @DefaultStringValue("Select the guest's time zone")
    String cloudInitTimeZoneToolTip();

    @DefaultStringValue("Choose a root password for the guest")
    String cloudInitRootPasswordToolTip();

    @DefaultStringValue("Verify the root password for the guest")
    String cloudInitRootPasswordVerificationToolTip();

    @DefaultStringValue("Choose a password for the admin login")
    String sysprepAdminPasswordToolTip();

    @DefaultStringValue("Verify the  password")
    String sysprepAdminPasswordVerificationToolTip();

    @DefaultStringValue("Enter the name of a network interface, e.g. \"eth0\"")
    String cloudInitNetworkToolTip();

    @DefaultStringValue("Choose boot protocol for the selected network interface")
    String cloudInitNetworkBootProtocolToolTip();

    @DefaultStringValue("Enter the IP address for the selected network interface")
    String cloudInitNetworkIpAddressToolTip();

    @DefaultStringValue("Enter the netmask (dotted-quad format) for the selected network interface")
    String cloudInitNetworkNetmaskToolTip();

    @DefaultStringValue("Enter the gateway for the selected network interface")
    String cloudInitNetworkGatewayToolTip();

    @DefaultStringValue("Enable to start the selected network interface when the guest boots")
    String cloudInitNetworkStartOnBootToolTip();

    @DefaultStringValue("Enter a space-separated list of DNS server IP addresses")
    String cloudInitDnsServersToolTip();

    @DefaultStringValue("Enter a space-separated list of DNS search domains")
    String cloudInitDnsSearchDomainsToolTip();

    @DefaultStringValue("Enter the full pathname where this file should be saved on the guest")
    String cloudInitAttachmentToolTip();

    @DefaultStringValue("Choose the input encoding type for the selected attachment")
    String cloudInitAttachmentTypeToolTip();

    @DefaultStringValue("Enter the attachment content")
    String cloudInitAttachmentContentTextToolTip();

    @DefaultStringValue("Use Cloud-Init/Sysprep")
    String cloudInitOrSysprep();

    @DefaultStringValue("Enter the attachment content encoded in base-64")
    String cloudInitAttachmentContentBase64ToolTip();

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

    @DefaultStringValue("Save Memory")
    String virtualMachineSnapshotCreatePopupMemoryLabel();

    @DefaultStringValue("Restore Memory")
    String virtualMachineSnapshotPreviewPopupMemoryLabel();

    @DefaultStringValue("The selected snapshot to be previewed contains memory")
    String snapshotContainsMemory();

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

    @DefaultStringValue("Clone VM")
    String cloneVm();

    @DefaultStringValue("Power Off")
    String powerOffVm();

    @DefaultStringValue("Shutdown")
    String shutDownVm();

    @DefaultStringValue("Reboot")
    String rebootVm();

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

    @DefaultStringValue("Overridden SPICE proxy address")
    String overriddenSpiceProxyAddress();

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

    @DefaultStringValue("Console Client IP")
    String consoleConnectedClientIp();

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

    @DefaultStringValue("Comment")
    String commentLabel();

    @DefaultStringValue("Reason")
    String reasonLabel();

    @DefaultStringValue("Based on Template")
    String basedOnTemplateVmPopup();

    @DefaultStringValue("Instance Type")
    String instanceType();

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

    @DefaultStringValue("Delete Protection")
    String deleteProtectionPopup();

    @DefaultStringValue("Instantiate VM network interfaces by picking a vNIC profile.")
    String assignNics();

    @DefaultStringValue("Copy Template Permissions")
    String copyTemplatePermissions();

    @DefaultStringValue("Restore saved memory")
    String restoreMemoryPopup();

    @DefaultStringValue("Smartcard Enabled")
    String smartcardVmPopup();

    @DefaultStringValue("VirtIO Console Device Enabled")
    String consoleDeviceEnabled();

    @DefaultStringValue("Type")
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

    @DefaultStringValue("Pass-Through Host CPU")
    String passThroughHostCpu();

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

    @DefaultStringValue("Start Running On:")
    String runOnVmPopup();

    @DefaultStringValue("Migration Options:")
    String runMigrationOptionsVmPopup();

    @DefaultStringValue("Priority for Run/Migration queue:")
    String priorForRunMigrationQueueVmPopup();

    @DefaultStringValue("Choosing different cluster may lead to unexpected results. Please consult documentation.")
    String migrationToDifferentClusterWarning();

    @DefaultStringValue("Watchdog")
    String watchdog();

    @DefaultStringValue("Memory Allocation:")
    String memAllocVmPopup();

    @DefaultStringValue("CPU Allocation:")
    String cpuAllocVmPopup();

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

    @DefaultStringValue("Clone Name")
    String clonedVmName();

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

    @DefaultStringValue("Profile Name")
    String profileNameInterface();

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

    @DefaultStringValue("QoS Name")
    String vmNetworkQosName();

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

    @DefaultStringValue("Memory")
    String memorySnapshot();

    @DefaultStringValue("Disks")
    String disksSnapshot();

    @DefaultStringValue("Create")
    String createSnapshot();

    @DefaultStringValue("Preview")
    String previewSnapshot();

    @DefaultStringValue("Custom...")
    String customPreviewSnapshot();

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
    @DefaultStringValue("New")
    String newDisk();

    @DefaultStringValue("Attach")
    String attachDisk();

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

    @DefaultStringValue("Export")
    String exportDisk();

    @DefaultStringValue("Data Center")
    String dataCenter();

    @DefaultStringValue("Actual Size")
    String sizeDisk();

    @DefaultStringValue("Size")
    String diskSnapshotSize();

    @DefaultStringValue("Disk Alias")
    String diskSnapshotAlias();

    @DefaultStringValue("Snapshot Description")
    String diskSnapshotDescription();

    @DefaultStringValue("Storage Domain")
    String storageDomainDisk();

    @DefaultStringValue("Storage Type")
    String storageTypeDisk();

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

    @DefaultStringValue("Disk Profile")
    String diskProfile();

    @DefaultStringValue("Quota")
    String quotaDisk();

    @DefaultStringValue("Destination")
    String destDisk();

    @DefaultStringValue("Alignment")
    String diskAlignment();

    @DefaultStringValue("Attached To")
    String attachedToDisk();

    @DefaultStringValue("Unattached")
    String unattachedDisk();

    @DefaultStringValue("Apply later")
    String applyLater();

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
    String nameNetworkInterfacePopup();

    @DefaultStringValue("Profile")
    String profileNetworkInterfacePopup();

    @DefaultStringValue("Type")
    String typeNetworkInterfacePopup();

    @DefaultStringValue("Custom MAC address")
    String customMacNetworkInterfacePopup();

    @DefaultStringValue("Card Status")
    String cardStatusNetworkInterface();

    @DefaultStringValue("Plugged")
    String pluggedNetworkInterface();

    @DefaultStringValue("Unplugged")
    String unpluggedNetworkInterface();

    @DefaultStringValue("Link State")
    String linkStateNetworkInterface();

    @DefaultStringValue("Up")
    String linkedNetworkInterface();

    @DefaultStringValue("Down")
    String unlinkedNetworkInterface();

    @DefaultStringValue("Number of VMs")
    String numOfVmsPoolPopup();

    @DefaultStringValue("Prestarted")
    String prestartedPoolPopup();

    // VM disk
    @DefaultStringValue("Size(GB)")
    String sizeVmDiskPopup();

    @DefaultStringValue("Extend size by(GB)")
    String extendImageSizeBy();

    @DefaultStringValue("Storage Domain")
    String storageDomainVmDiskPopup();

    @DefaultStringValue("Use Host")
    String hostVmDiskPopup();

    @DefaultStringValue("Alias")
    String aliasVmDiskPopup();

    @DefaultStringValue("Data Center")
    String dcVmDiskPopup();

    @DefaultStringValue("Disk Profile")
    String diskProfileVmDiskPopup();

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

    @DefaultStringValue("Bootable")
    String isBootableVmDiskPopup();

    @DefaultStringValue("Shareable")
    String isShareableVmDiskPopup();

    @DefaultStringValue("Read Only")
    String isReadOnlyVmDiskPopup();

    @DefaultStringValue("Enable SCSI Pass-Through")
    String isScsiPassthroughEditor();

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

    @DefaultStringValue("Namespace:")
    String namespacePermissionsPopup();

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

    @DefaultStringValue("Read Only")
    String readOnly();

    @DefaultStringValue("Bootable")
    String bootable();

    @DefaultStringValue("Internal")
    String internalDisk();

    @DefaultStringValue("External (Direct LUN)")
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

    @DefaultStringValue("Disk in status Illegal")
    String illegalStatus();

    @DefaultStringValue("CPU Pinning topology")
    String cpuPinningLabel();

    @DefaultStringValue("Format: v#p[_v#p]\n" +
            "Examples:\n" +
            "0#0 => pin vCPU 0 to pCPU 0\n" +
            "0#0_1#3 => pin vCPU 0 to pCPU 0 and pin vCPU 1 to pCPU 3\n" +
            "1#1-4,^2 => pin vCPU 1 to pCPU set 1 to 4, excluding 2")
    String cpuPinningLabelExplanation();

    @DefaultStringValue("Migration is NOT currently supported using SPICE Native USB redirection on cluster version lower than 3.2")
    String nativeUsbSupportWarning();

    @DefaultStringValue("Monitors")
    String monitors();

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

    @DefaultStringValue("Speed")
    String speedNetworkHost();

    @DefaultStringValue("Rx")
    String rxNetworkHost();

    @DefaultStringValue("Tx")
    String txNetworkHost();

    // Network VM
    @DefaultStringValue("vNIC Status")
    String vnicStatusNetworkVM();

    @DefaultStringValue("vNIC")
    String vnicNetworkVM();

    @DefaultStringValue("vNIC Rx")
    String rxNetworkVM();

    @DefaultStringValue("vNIC Tx")
    String txNetworkVM();

    // Network Template
    @DefaultStringValue("vNIC")
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

    @DefaultStringValue("SPICE HTML5 browser client (Tech preview)")
    String spiceHtml5();

    @DefaultStringValue("Enable USB Auto-Share")
    String usbAutoshare();

    @DefaultStringValue("Open in Full Screen")
    String openInFullScreen();

    @DefaultStringValue("Enable SPICE Proxy")
    String enableSpiceProxy();

    @DefaultStringValue("Your browser doesn't support SPICE plugin")
    String spicePluginNotSupportedByBrowser();

    @DefaultStringValue("Your browser doesn't support RDP plugin")
    String rdpPluginNotSupportedByBrowser();

    @DefaultStringValue("No SPICE proxy defined on system level")
    String spiceProxyCanBeEnabledOnlyWhenDefined();

    @DefaultStringValue("SPICE HTML5 client can be used only if websocket proxy is configured in the engine.")
    String spiceHtml5OnlyWhenWebsocketProxySet();

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

    @DefaultStringValue("VNC Options")
    String vncOptions();

    @DefaultStringValue("If there is a SPICE plugin installed in your browser, it is used for invoking the console " +
            "session.Otherwise SPICE configuration file is downloaded which will automatically launch locally " +
            "installed Remote Viewer (found under \"Console Client Resources\" page.).")
    String spiceInvokeAuto();

    @DefaultStringValue("Downloads a console configuration file to be opened by Remote Viewer application installed on " +
            "your system (found under \"Console Client Resources\" page.).")
    String consoleInvokeNative();

    @DefaultStringValue("Uses SPICE browser plugin for invoking console session. For this you must have SPICE console plugin installed in your browser.")
    String spiceInvokePlugin();

    @DefaultStringValue("Uses SPICE HTML5 client that runs inside your browser. This client is a Technology preview and it's possible some functions (e.g. keyboard layouts) will not work as expected.")
    String spiceInvokeHtml5();

    @DefaultStringValue("Uses noVnc client that runs inside your browser. ")
    String invokeNoVnc();

    @DefaultStringValue("Uses RDP browser plugin if supported. Otherwise switches to \"Native client\" invocation.")
    String rdpInvokeAuto();

    @DefaultStringValue("Downloads a console configuration file to be opened by Remote Desktop client application on " +
            "your system. Required for Network Level Authentication.")
    String rdpInvokeNative();

    @DefaultStringValue("Uses browser plugin to invoke the RDP session (MS Internet Explorer only). Required for Single Sign On.")
    String rdpInvokePlugin();

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

    @DefaultStringValue("Single PCI")
    String singleQxlEnabled();

    @DefaultStringValue("Optimized for")
    String optimizedFor();

    @DefaultStringValue("Copy VM permissions")
    String copyVmPermissions();

    @DefaultStringValue("Server")
    String server();

    @DefaultStringValue("Desktop")
    String desktop();

    @DefaultStringValue("Stateless Server")
    String statelessServer();

    @DefaultStringValue("Stateless Desktop")
    String statelessDesktop();

    @DefaultStringValue("Desktop in Preview")
    String desktopInPreview();

    @DefaultStringValue("Server in Preview")
    String serverInPreview();

    @DefaultStringValue("Server with newer configuration for next run")
    String serverChanges();

    @DefaultStringValue("Desktop with newer configuration for next run")
    String desktopChanges();

    @DefaultStringValue("Stateless Server with newer configuration for next run")
    String statelessServerChanges();

    @DefaultStringValue("Stateless Desktop with newer configuration for next run")
    String statelessDesktopChanges();

    @DefaultStringValue("Run Once")
    String runOnce();

    @DefaultStringValue("Up")
    String up();

    @DefaultStringValue("Rebooting")
    String rebooting();

    @DefaultStringValue("Wait For Launch")
    String waitForLaunchStatus();

    @DefaultStringValue("Image Locked")
    String imageLocked();

    @DefaultStringValue("Migrating")
    String migrating();

    @DefaultStringValue("Suspended")
    String suspended();

    @DefaultStringValue("Paused")
    String paused();

    @DefaultStringValue("Unknown")
    String unknown();

    @DefaultStringValue("Down")
    String down();

    @DefaultStringValue("Up but one or more bricks are down")
    String volumeBricksDown();

    @DefaultStringValue("Up but all bricks are down")
    String volumeAllBricksDown();

    @DefaultStringValue("Powering Up")
    String poweringUp();

    @DefaultStringValue("Restoring")
    String restoring();

    @DefaultStringValue("Saving")
    String vmStatusSaving();

    @DefaultStringValue("Powering Down")
    String poweringDown();

    @DefaultStringValue("Not Responding")
    String notResponding();

    @DefaultStringValue("Unlimited")
    String UnlimitedNetworkQoS();

    @DefaultStringValue("Unlimited")
    String unlimitedQos();

    @DefaultStringValue("Password")
    String hostPasswordLabel();

    @DefaultStringValue("SSH PublicKey")
    String hostPublicKeyLable();

    @DefaultStringValue("Memory Balloon Device Enabled")
    String memoryBalloonDeviceEnabled();

    @DefaultStringValue("VM Id")
    String vmId();

    @DefaultStringValue("CPU Shares")
    String cpuShares();

    @DefaultStringValue("Network")
    String networkProfilePopup();

    @DefaultStringValue("VirtIO-SCSI Enabled")
    String isVirtioScsiEnabled();

    @DefaultStringValue("Attach a VirtIO-SCSI controller when running the VM")
    String isVirtioScsiEnabledInfo();

    @DefaultStringValue("Cloud-Init YAML sections, please refer to: http://www.ovirt.org/Features/vm-init-persistent#Custom_Script")
    String customScriptInfo();

    @DefaultStringValue("VirtIO-SCSI can be enabled from Resource Allocation tab on VM dialog")
    String diskInterfaceInfo();

    @DefaultStringValue("FQDN")
    String fqdn();

    @DefaultStringValue("The fields under 'Start Running On' and 'Migration Options' aren't editable while the VM isn't down")
    String nonEditableMigrationFieldsWhileVmNotDownInfo();

    @DefaultStringValue("Active VM")
    String snapshotDescriptionActiveVm();

    @DefaultStringValue("Active VM before the preview")
    String snapshotDescriptionActiveVmBeforePreview();

    @DefaultStringValue("Waiting ...")
    String waitForGlusterTask();

    @DefaultStringValue("Single Sign On method")
    String ssoMethod();

    @DefaultStringValue("Disable Single Sign On")
    String none();

    @DefaultStringValue("Use Guest Agent")
    String guestAgent();

    @DefaultStringValue("Override SPICE proxy")
    String defineSpiceProxyEnable();

    @DefaultStringValue("Disks to include:")
    String snapshotDisks();

    @DefaultStringValue("VM Configuration")
    String vmConfiguration();

    @DefaultStringValue("Double-click to select an entire row")
    String customPreviewSnapshotTableTitle();

    @DefaultStringValue("Saving memory may cause data loss when excluding disks!")
    String snapshotCreationWithMemoryAndPartialDisksWarning();

    @DefaultStringValue("Previewing memory may cause data loss when excluding disks!")
    String snapshotPreviewWithMemoryAndPartialDisksWarning();

    @DefaultStringValue("Excluded disks will be deleted when the snapshot is committed.")
    String snapshotPreviewWithExcludedDisksWarning();

    @DefaultStringValue("You chose to preview a snapshot that contains only a subset of these disks")
    String previewPartialSnapshotSubsetDisksLabel();

    @DefaultStringValue("What would you like to do?")
    String previewPartialSnapshotQuestionLabel();

    @DefaultStringValue("Import as Template")
    String importAsTemplate();

    @DefaultStringValue("Use custom migration downtime")
    String overrideMigrationDowntimeLabel();

    @DefaultStringValue("Migration downtime (ms)")
    String migrationDowntimeLabel();

    @DefaultStringValue("Logical Networks")
    String logicalNetworks();

    @DefaultStringValue("Storage Targets")
    String storageTargets();

    @DefaultStringValue("Name")
    String name();

    @DefaultStringValue("Description")
    String description();

    @DefaultStringValue("IQN")
    String iqn();

    @DefaultStringValue("Storage Name")
    String storageName();

    @DefaultStringValue("Storage ID (VG Name)")
    String storageIdVgName();

    @DefaultStringValue("Provide custom serial number policy")
    String overrideSerialNumberPolicy();

    @DefaultStringValue("Connect")
    String connect();

    @DefaultStringValue("Enable boot menu")
    String bootMenuEnabled();


    @DefaultStringValue("This option sets the time zone offset of the guest hardware clock. " +
                        "For Windows OS this should correspond to the time zone set in the guest (during installation or afterwards). " +
                        "Most default Linux installations expect hardware clock to be GMT+00:00.")
    String timeZoneInfo();

    // Instance Types
    @DefaultStringValue("Name")
    String nameInstanceTypeGeneral();

    @DefaultStringValue("Description")
    String descriptionInstanceTypeGeneral();

    @DefaultStringValue("The fields is attached to the currently selected instance type")
    String attachedToInstanceType();

    @DefaultStringValue("The field is not attached to any instance type")
    String detachedFromInstanceType();

    @DefaultStringValue("Guest CPU Count")
    String GuestCpuCount();

    @DefaultStringValue("Random Generator enabled")
    String rngDevEnabled();

    @DefaultStringValue("Period duration (ms)")
    String rngPeriod();

    @DefaultStringValue("Bytes per period")
    String rngBytes();

    @DefaultStringValue("Device source")
    String deviceSource();

    @DefaultStringValue("/dev/random source")
    String rngSourceRandom();

    @DefaultStringValue("/dev/hwrng source")
    String rngSourceHwrng();

    @DefaultStringValue("Random Generator")
    String rngDeviceTab();

    @DefaultStringValue("Enables/disables Random Number Generator device. If 'period' and 'bytes' are empty, libvirt default is used. If you specify 'period' you need to specify 'bytes' as well.")
    String rngDevExplanation();

    @DefaultStringValue("Enable SPICE file transfer")
    String spiceFileTransferEnabled();

    @DefaultStringValue("Enable SPICE clipboard copy and paste")
    String spiceCopyPasteEnabled();

    @DefaultStringValue("Required Random Number Generator sources:")
    String requiredRngSources();

    @DefaultStringValue("Namespace")
    String namespace();

    @DefaultStringValue("Authorization provider")
    String authz();

    @DefaultStringValue("Name")
    String profileNameLabel();

    @DefaultStringValue("Description")
    String profileDescriptionLabel();

    @DefaultStringValue("QoS Name")
    String qosName();

    @DefaultStringValue("QoS Name")
    String cpuQosName();

    @DefaultStringValue("New")
    String newProfile();

    @DefaultStringValue("Edit")
    String editProfile();

    @DefaultStringValue("Remove")
    String removeProfile();

    @DefaultStringValue("CPU Profile")
    String cpuProfileLabel();

    @DefaultStringValue("SPICE-HTML5 does not support SPICE Proxy.")
    String spiceHtml5DoesntSupportSpiceProxy();

    //Numa
    @DefaultStringValue("Drag a vNUMA onto a NUMA node to pin the vNUMA to that node.")
    String numaUnassignedInstructions();

    @DefaultStringValue("Unassigned virtual nodes")
    String unassignedVNumaNodesLabel();

    @DefaultStringValue("Totals:")
    String numaSummaryTotals();

    @DefaultStringValue("Pin to NUMA node")
    String pinToNumaNode();

    @DefaultStringValue("Un pin virtual node")
    String unPinNode();

    @DefaultStringValue("Tune Mode")
    String numaTunaModeLabel();

    @DefaultStringValue("NUMA Node Count")
    String numaNodeCountLabel();

    @DefaultStringValue("Configure NUMA:")
    String numaSectionLabel();

    @DefaultStringValue("NUMA Pinning")
    String numaSupportButtonLabel();

    @DefaultStringValue("Changed fields list")
    String changedFieldsList();

    @DefaultStringValue("Auto Converge migrations")
    String autoConvergeLabel();

    @DefaultStringValue("Enable migration compression")
    String migrateCompressedLabel();

    @DefaultStringValue("Auto Converge")
    String autoConverge();

    @DefaultStringValue("Don't Auto Converge")
    String dontAutoConverge();

    @DefaultStringValue("Compress")
    String compress();

    @DefaultStringValue("Don't compress")
    String dontCompress();

    @DefaultStringValue("Inherit from cluster setting")
    String inheritFromCluster();

    @DefaultStringValue("Inherit from global setting")
    String inheritFromGlobal();
}
