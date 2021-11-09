package org.ovirt.engine.ui.common;

import com.google.gwt.i18n.client.Constants;

public interface CommonApplicationConstants extends Constants {

    String resetGridSettings();

    String changeColumnsVisibilityOrder();

    @DefaultStringValue("") // Use annotation and not a properties key to leave it out of translations
    String empty();

    String emptyListBoxText();

    String vendorUrl();

    String errorPopupCaption();

    String closeButtonLabel();

    String unAvailablePropertyLabel();

    @DefaultStringValue("<br/>") // Use annotation and not a properties key to leave it out of translations
    String lineBreak();

    @DefaultStringValue("&nbsp;") // Use annotation and not a properties key to leave it out of translations
    String htmlNonBreakingSpace();

    @DefaultStringValue("&emsp;") // Use annotation and not a properties key to leave it out of translations
    String htmlTab();

    String andBreak();

    @DefaultStringValue(" ") // Use annotation and not a properties key to leave it out of translations
    String space();

    String missingColumnContextMenuTitle();

    String actionTableNextPageButtonLabel();

    String actionTablePrevPageButtonLabel();

    String diskHotPlugNotSupported();

    String disksAllocation();

    String templateName();

    String templateVersion();

    String templateDescription();

    String createAsSubTemplate();

    String rootTemplate();

    String templateVersionName();

    String template();

    String vm();

    String latest();

    String baseTemplate();

    String diskNamePrefix();

    String singleDestinationStorage();

    String defaultStorage();

    String singleQuota();

    String runOncePopupBootOptionsLabel();

    String bootSequenceUpButtonLabel();

    String bootSequenceDownButtonLabel();

    String runOncePopupCustomPropertiesLabel();

    String runOncePopupDisplayConsoleVncLabel();

    String runOncePopupDisplayConsoleSpiceLabel();

    String runOnceHeadlessModeExplanation();

    String runOncePopupRunAsStatelessLabel();

    String runOncePopupRunAndPauseLabel();

    String runOncePopupLinuxBootOptionsLabel();

    String runOncePopupKernelPathLabel();

    String runOncePopupInitrdPathLabel();

    String runOncePopupKernelParamsLabel();

    String runOncePopupAttachFloppyLabel();

    String runOncePopupAttachIsoLabel();

    String runOncePopupAttachWgtLabel();

    String runOncePopupSysprepLabel();

    String runOncePopupInitialRunLabel();

    String runOncePopupSysPrepDomainNameLabel();

    String runOnceUseAlternateCredentialsLabel();

    String runOncePopupSysPrepUserNameLabel();

    String runOncePopupSysPrepPasswordLabel();

    String runOncePopupSysPrepPasswordVerificationLabel();

    String runOncePopupBootSequenceLabel();

    String runOncePopupCloudInitLabel();

    String inputLocaleLabel();

    String uiLanguageLabel();

    String systemLocaleLabel();

    String userLocaleLabel();

    String customLocaleLabel();

    String sysprepLabel();

    String showVfLabel();

    String hideVfLabel();

    String activeDirectoryOU();

    String activeDirectoryOUToolTip();

    String cloudInitUserNameLabel();

    String cloudInitHostnameLabel();

    String sysprepOrgNameLabel();

    String cloudInitAuthenticationLabel();

    String cloudInitAuthorizedKeysLabel();

    String cloudInitRegenerateKeysLabel();

    String cloudInitConfigureTimeZoneLabel();

    String cloudInitTimeZoneLabel();

    String cloudInitRootPasswordLabel();

    String ignitionRootPasswordLabel();

    String cloudInitRootPasswordVerificationLabel();

    String sysprepAdminPasswordLabel();

    String sysprepAdminPasswordVerificationLabel();

    String cloudInitNetworskLabel();

    String cloudInitNetworkLabel();

    String cloudInitNetworkIpv4BootProtocolLabel();

    String cloudInitNetworkIpv6BootProtocolLabel();

    String cloudInitNetworkIpv4AddressLabel();

    String cloudInitNetworkIpv6AddressLabel();

    String cloudInitNetworkIpv4NetmaskLabel();

    String cloudInitNetworkIpv6PrefixLabel();

    String cloudInitNetworkIpv4GatewayLabel();

    String cloudInitNetworkIpv6GatewayLabel();

    String cloudInitNetworkStartOnBootLabel();

    String cloudInitDnsServersLabel();

    String cloudInitDnsSearchDomainsLabel();

    String customScriptLabel();

    String ignitionScriptLabel();

    String cloudInitAttachmentLabel();

    String cloudInitAttachmentSelectLabel();

    String cloudInitAttachmentTypeLabel();

    String cloudInitAttachmentContentLabel();

    String cloudInitObjectAddLabel();

    String cloudInitObjectRemoveLabel();

    String vmInitPasswordSetLabel();

    String vmInitPasswordSetToolTip();

    String vmInitPasswordNotSetToolTip();

    String cloudInitHostnameToolTip();

    String cloudInitAuthorizedKeysToolTip();

    String customScriptToolTip();

    String cloudInitRegenerateKeysToolTip();

    String cloudInitTimeZoneToolTip();

    String cloudInitRootPasswordToolTip();

    String cloudInitRootPasswordVerificationToolTip();

    String sysprepAdminPasswordToolTip();

    String sysprepAdminPasswordVerificationToolTip();

    String cloudInitNetworkToolTip();

    String cloudInitNetworkIpv4BootProtocolToolTip();

    String cloudInitNetworkIpv6BootProtocolToolTip();

    String cloudInitNetworkIpv4AddressToolTip();

    String cloudInitNetworkIpv6AddressToolTip();

    String cloudInitNetworkIpv4NetmaskToolTip();

    String cloudInitNetworkIpv6PrefixToolTip();

    String cloudInitNetworkIpv4GatewayToolTip();

    String cloudInitNetworkIpv6GatewayToolTip();

    String cloudInitNetworkStartOnBootToolTip();

    String cloudInitDnsServersToolTip();

    String cloudInitDnsSearchDomainsToolTip();

    String cloudInitAttachmentToolTip();

    String cloudInitAttachmentTypeToolTip();

    String cloudInitAttachmentContentTextToolTip();

    String cloudInit();

    String sysprep();

    String ignition();

    String cloudInitAttachmentContentBase64ToolTip();

    String makeTemplatePopupNameLabel();

    String makeTemplatePopupDescriptionLabel();

    String makeTemplateClusterLabel();

    String makeTemplateQuotaLabel();

    String makeTemplateStorageDomainLabel();

    String makeTemplateIsTemplatePublicEditorLabel();

    String virtualMachineSnapshotCreatePopupDescriptionLabel();

    String virtualMachineSnapshotCreatePopupMemoryLabel();

    String virtualMachineSnapshotPreviewPopupMemoryLabel();

    String previewSnapshotContainsMemory();

    String snapshotContainsMemory();

    String loadingLabel();

    String generalLabel();

    String loadLabel();

    String disksLabel();

    String networkFilterParametersLabel();

    String registerDisksLabel();

    String statistics();

    String nicsLabel();

    String applicationsLabel();

    String containersLabel();

    String readonlyLabel();

    String currentSnapshotLabel();

    String previousCurrentSnapshotLabel();

    String brokenVmConfiguration();

    String latchApproveOperationLabel();

    String latchApproveUnpinningLabel();

    String permanentlyRemoveLabel();

    String persistGridSettingsOnServer();

    String persistGridSettingsOnServerTooltip();

    String confirmSuspendingVm();

    String hideDisplayColumns();

    String swapColumns();

    String approveOperation();

    String forceRemove();

    String doNotShowAgain();

    String yes();

    String no();

    String newVm();

    String cloneVm();

    String powerOffVm();

    String shutDownVm();

    String rebootVm();

    String resetVm();

    String pluggedVm();

    String nameVm();

    String statusVm();

    String uptimeVm();

    String typeVm();

    String aliasVm();

    String descriptionVm();

    String templateVm();

    String osVm();

    String videoType();

    String priorityVm();

    String definedMemoryVm();

    String physMemGauranteedVm();

    String guestFreeCachedBufferedMemInfo();

    String guestFreeCachedBufferedCombinedMemInfo();

    String numOfCpuCoresVm();

    String highlyAvailableVm();

    String numOfMonitorsVm();

    String allowConsoleReconnect();

    String overriddenSpiceProxyAddress();

    String usbPolicyVm();

    String createdByUserVm();

    String clusterCompatibilityVersionVm();

    String quotaVm();

    String originVm();

    String runOnVm();

    String customPropertiesVm();

    String domainVm();

    String timeZoneVm();

    String installedAppsVm();

    String consoleConnectedUserVm();

    String consoleConnectedClientIp();

    String loggedInUserVm();

    String highAvailVmPopup();

    String resourceAllocVmPopup();

    String bootOptionsVmPopup();

    String GeneralVmPopup();

    String dcVmPopup();

    String hostClusterVmPopup();

    String quotaVmPopup();

    String nameVmPopup();

    String vmIdPopup();

    String descriptionVmPopup();

    String commentLabel();

    String biosTypeGeneral();

    String customBiosTypeLabel();

    String emulatedMachineLabel();

    String cpuModelLabel();

    String customCompatibilityVersionLabel();

    String clusterDefaultOption();

    String reasonLabel();

    String basedOnTemplateVmPopup();

    String instanceType();

    String instanceImages();

    String editInstanceImages();

    String addInstanceImages();

    String attachInstanceImages();

    String memSizeVmPopup();

    String maxMemorySizePopup();

    String totalCoresVmPopup();

    String cpuSocketsVmPopup();

    String numOfVCPUs();

    String coresPerSocket();

    String threadsPerCore();

    String numOfSockets();

    String osVmPopup();

    String statelessVmPopup();

    String runAndPauseVmPopup();

    String deleteProtectionPopup();

    String assignNics();

    String copyTemplatePermissions();

    String restoreMemoryPopup();

    String smartcardVmPopup();

    String consoleDeviceEnabled();

    String consolePublicKeyLabel();

    String poolVmPopup();

    String poolTypeVmPopup();

    String poolStatefulLabel();

    String initialRunVmPopup();

    String systemVmPopup();

    String prestartedVms();

    String domainVmPopup();

    String consoleVmPopup();

    String vncKeyboardLayoutVmPopup();

    String usbPolicyVmPopup();

    String consoleDisconnectActionVmPopup();

    String consoleDisconnectActionDelayVmPopup();

    String monitorsVmPopup();

    String hostVmPopup();

    String anyHostInClusterVmPopup();

    String runOnSelectedHostVmPopup();

    String passThroughHostCpu();

    String tscFrequency();

    String allowMigrationOnlyAdminVmPopup();

    String highlyAvailableVmPopup();

    String highPerformance();

    String highPerformanceChanges();

    String watchdogAction();

    String watchdogModel();

    String templateProvisVmPopup();

    String thinVmPopup();

    String cloneVmPopup();

    String physMemGuarVmPopup();

    String firstDeviceVmPopup();

    String secondDeviceVmPopup();

    String kernelPathVmPopup();

    String initrdPathVmPopup();

    String kernelParamsVmPopup();

    String customPropsVmPopup();

    String runOnVmPopup();

    String hostCpuVmPopup();

    String runMigrationOptionsVmPopup();

    String priorForRunMigrationQueueVmPopup();

    String watchdog();

    String memAllocVmPopup();

    String trustedPlatformModuleVmPopup();

    String multiQueuesVmPopup();

    String ioThreadsVmPopup();

    String cpuAllocVmPopup();

    String storageAllocVmPopup();

    String availOnlyTemplSelectedVmPopup();

    String bootSequenceVmPopup();

    String attachCdVmPopup();

    String attachVirtioDrivers();

    String linuxBootOptionsVmPopup();

    String specificVmPopup();

    String clonedVmName();

    String iconTabVmPopup();

    String newIconVmPopup();

    String currentIconVmPopup();

    String uploadIconVmPopup();

    String useDefaultIconVmPopup();

    String discardChangesIconVmPopup();

    String iconLimitationsIconVmPopup();

    String inheritedPermission();

    String typePermission();

    String userPermission();

    String rolePermission();

    String addPermission();

    String removePermission();

    String objectPermission();

    String addSystemPermission();

    String newInterface();

    String editInterface();

    String removeInterface();

    String activateInterface();

    String deactivateInterface();

    String nameInterface();

    String networkFilterParametersWidgetName();

    String nameNetworkFilterParameter();

    String valueNetworkFilterParameter();

    String networkNameInterface();

    String originalNetworkNameInterface();

    String profileNameInterface();

    String typeInterface();

    String macInterface();

    String speedInterface();

    String rxRate();

    String txRate();

    String rxTotal();

    String txTotal();

    String vmNetworkQosName();

    String dropsInterface();

    String portMirroring();

    String guestInterfaceName();

    String plugged();

    String unplugged();

    String portMirroringEnabled();

    String portMirroringDisabled();

    String mbps();

    String bytes();

    String pkts();

    String sizeMiB();

    String sizeGiB();

    String sizeMB();

    String sizeM();

    String sizeT();

    String sizeTB();

    String sizeG();

    String sizeGB();

    String sizeTiB();

    String severityEvent();

    String timeEvent();

    String messageEvent();

    String correltaionIdEvent();

    String originEvent();

    String customEventIdEvent();

    String idEvent();

    String untilEndTime();

    String until();

    String dateSnapshot();

    String descriptionSnapshot();

    String leaseSnapshot();

    String statusSnapshot();

    String memorySnapshot();

    String disksSnapshot();

    String createSnapshot();

    String previewSnapshot();

    String customPreviewSnapshot();

    String commitSnapshot();

    String undoSnapshot();

    String deleteSnapshot();

    String cloneSnapshot();

    String makeTemplateFromSnapshot();

    String installedAppsSnapshot();

    String nameSnapshot();

    String namesContainer();

    String idContainer();

    String imageContainer();

    String commandContainer();

    String statusContainer();

    String newDisk();

    String attachDisk();

    String editDisk();

    String removeDisk();

    String sparsifyDisk();

    String activateDisk();

    String deactivateDisk();

    String active();

    String inactive();

    String moveDisk();

    String exportDisk();

    String dataCenter();

    String sizeDisk();

    String diskSnapshotSize();

    String diskSnapshotAlias();

    String diskSnapshotDescription();

    String storageDomainDisk();

    String storageTypeDisk();

    String storageDomainsDisk();

    String storageDomainsLabelDisk();

    String typeDisk();

    String allocationDisk();

    String originalAllocationDisk();

    String interfaceDisk();

    String logicalNameDisk();

    String statusDisk();

    String contentDisk();

    String creationDateDisk();

    String modificationDateDisk();

    String diskSnapshotCreationDate();

    String diskSnapshotIDDisk();

    String descriptionDisk();

    String diskVirtualSize();

    String diskActualSize();

    String formatDisk();

    String copyDisk();

    String freeSpaceDisk();

    String aliasDisk();

    String provisionedSizeDisk();

    String sourceDisk();

    String targetDisk();

    String diskProfile();

    String quotaDisk();

    String destDisk();

    String attachedToDisk();

    String unattachedDisk();

    String containersIconDisk();

    String applyLater();

    String installedApp();

    String guestContainer();

    String nameTemplateGeneral();

    String descriptionTemplateGeneral();

    String hostClusterTemplateGeneral();

    String osTemplateGeneral();

    String definedMemTemplateGeneral();

    String numOfCpuCoresTemplateGeneral();

    String numOfMonitorsTemplateGeneral();

    String highlyAvailTemplateGeneral();

    String allowPartialVmImportWarning();

    String priorityTemplateGeneral();

    String usbPolicyTemplateGeneral();

    String originTemplateGeneral();

    String isStatelessTemplateGeneral();

    String domainTemplateGeneral();

    String tzTemplateGeneral();

    String quotaTemplateGeneral();

    String goPermissionsPopup();

    String nameNetworkInterfacePopup();

    String profileNetworkInterfacePopup();

    String typeNetworkInterfacePopup();

    String customMacNetworkInterfacePopup();

    String cardStatusNetworkInterface();

    String pluggedNetworkInterface();

    String unpluggedNetworkInterface();

    String linkStateNetworkInterface();

    String linkedNetworkInterface();

    String unlinkedNetworkInterface();

    String numOfVmsPoolPopup();

    String sizeVmDiskPopup();

    String extendImageSizeBy();

    String storageDomainVmDiskPopup();

    String hostVmDiskPopup();

    String hostToUseToolTip();

    String aliasVmDiskPopup();

    String dcVmDiskPopup();

    String diskProfileVmDiskPopup();

    String quotaVmDiskPopup();

    String descriptionVmDiskPopup();

    String attachDiskVmDiskPopup();

    String interfaceVmDiskPopup();

    String storageTypeVmDiskPopup();

    String wipeAfterDeleteVmDiskPopup();

    String enableDiscardVmDiskPopup();

    String isBootableVmDiskPopup();

    String isShareableVmDiskPopup();

    String isReadOnlyVmDiskPopup();

    String isIncrementalBackupVmDiskPopup();

    String isUsingScsiReservationEditor();

    String isScsiPassthroughEditor();

    String isSgIoUnfilteredEditor();

    String activateVmDiskPopup();

    String activateVmDiskPopupToolTip();

    String aliasVmDiskTable();

    String descriptionVmDiskTable();

    String idVmDiskTable();

    String provisionedSizeVmDiskTable();

    String sizeVmDiskTable();

    String storageDomainVmDiskTable();

    String everyonePermission();

    String myGroupsPermission();

    String roleToAssignPermissionsPopup();

    String exampleInterfacePopup();

    String firstNamePermissionsPopup();

    String groupNamePermissionsPopup();

    String lastNamePermissionsPopup();

    String userNamePermissionsPopup();

    String displayNamePermissionsPopup();

    String searchPermissionsPopup();

    String namespacePermissionsPopup();

    String namePoolGeneral();

    String descriptionPoolGeneral();

    String templatePoolGeneral();

    String osPoolGeneral();

    String definedMemPoolGeneral();

    String physMemGaurPoolGeneral();

    String numOfCpuCoresPoolGeneral();

    String numOfMonitorsPoolGeneral();

    String usbPolicyPoolGeneral();

    String originPoolGeneral();

    String isStatelessPoolGeneral();

    String runOnPoolGeneral();

    String domainPoolGeneral();

    String tzPoolGeneral();

    String selectedActionTable();

    String statusTask();

    String timeTask();

    String descriptionTask();

    String loadingTaskTree();

    String completedTask();

    String startedTask();

    String shareable();

    String readOnly();

    String bootable();

    String imageDisk();

    String directLunDisk();

    String managedBlockDisk();

    String storageIscsiPopupLunToTargetsTabLabel();

    String storageIscsiPopupTargetsToLunTabLabel();

    String storageIscsiPopupAddressLabel();

    String storageIscsiPopupPortLabel();

    String storageIscsiPopupUserAuthLabel();

    String storageIscsiPopupChapUserLabel();

    String storageIscsiPopupChapPassLabel();

    String storageIscsiPopupDiscoverButtonLabel();

    String storageIscsiDiscoverTargetsLabel();

    String storageIscsiPopupLoginButtonLabel();

    String storageIscsiSelectStorageLabel();

    String storageIscsiRemoveLUNsLabel();

    String storageIscsiAvailableActionsOnMaintenanceLabel();

    String storageIscsiActionsLabel();

    String storageIscsiAvailableActionsForActiveDomainsLabel();

    String targetNameSanStorage();

    String addressSanStorage();

    String portSanStorage();

    String lunIdSanStorage();

    String devSizeSanStorage();

    String devAdditionalSizeSanStorage();

    String pathSanStorage();

    String vendorIdSanStorage();

    String productIdSanStorage();

    String serialSanStorage();

    String removeSanStorage();

    String addSanStorage();

    String actionsSanStorage();

    String cannotExtendSanStorage();

    String nameSanImStorage();

    String formatSanImStorage();

    String imageTransferUnknown();

    String imageTransferInitializing();

    String imageTransferResuming();

    String imageUploadTransferring();

    String imageDownloadTransferring();

    String uploadingImageViaAPI();

    String downloadingImageViaAPI();

    String imageTransferPausedSystem();

    String imageTransferPausedUser();

    String imageTransferCancelledSystem();

    String imageTransferCancelledUser();

    String imageTransferFinalizingSuccess();

    String imageTransferFinalizingFailure();

    String imageTransferFinalizingCleanup();

    String imageTransferFinishedSuccess();

    String imageTransferFinishedFailure();

    String imageTransferFinishedCleanup();

    String imageTransferringViaBrowser();

    String imageTransferring();

    String notAvailableLabel();

    String illegalStatus();

    String cpuPinningLabel();

    String cpuPinningPolicyLabel();

    String multiQueuesLabel();

    String virtioScsiMultiQueuesEnabled();

    String isVirtioScsiMultiQueuesInfoIcon();

    String cpuPinningLabelExplanation();

    String diskFormatTypeMatrixInfo();

    String multiQueuesLabelExplanation();

    String monitors();

    String lunAlreadyUsedWarning();

    String advancedParameters();

    String initialRunGeneral();

    String initialRunWindows();

    String advancedOptionsLabel();

    String bootableDisk();

    String allDisksLabel();

    String imageDisksLabel();

    String lunDisksLabel();

    String managedBlockDisksLabel();

    String currentQuota();

    String elementName();

    String assignQuota();

    String refreshLUN();

    String increaseNumberOfVMsInPoolBy();

    String vms();

    String vmFilters();

    String maxAssignedVmsPerUser();

    String spmPriority();

    String refreshRate();

    String statusNetworkHost();

    String nicNetworkHost();

    String speedNetworkHost();

    String rxNetworkHost();

    String txNetworkHost();

    String vnicStatusNetworkVM();

    String vnicNetworkVM();

    String rxNetworkVM();

    String txNetworkVM();

    String vnicNetworkTemplate();

    String showQuotaDistribution();

    String hideQuotaDistribution();

    String nameVmGuestAgent();

    String ipv4VmGuestAgent();

    String ipv6VmGuestAgent();

    String macVmGuestAgent();

    String cloneVM();

    String collapseSnapshots();

    String highPriorityOnly();

    String virt();

    String gluster();

    String spice();

    String auto();

    String nativeClient();

    String browserPlugin();

    String noVnc();

    String userSettingsLabel();

    String usbAutoshare();

    String openInFullScreen();

    String enableSpiceProxy();

    String rdpPluginNotSupportedByBrowser();

    String spiceProxyCanBeEnabledOnlyWhenDefined();

    String enableWanOptions();

    String disableSmartcard();

    String rdpOptions();

    String useLocalDrives();

    String remoteDesktop();

    String vnc();

    String webBasedClientsUnsupported();

    String spiceOptions();

    String vncOptions();

    String spiceInvokeAuto();

    String consoleInvokeNative();

    String invokeNoVnc();

    String rdpInvokeAuto();

    String rdpInvokeNative();

    String rdpInvokePlugin();

    String consoleInvocation();

    String vncNotSupportedMsg();

    String spiceNotAvailable();

    String rdpNotAvailable();

    String vncNotAvailable();

    String browserNotSupportedMsg();

    String consoleOptions();

    String details();

    String notConfigured();

    String noItemsToDisplay();

    String removeConfirmationPopupMessage();

    String soundcardEnabled();

    String optimizedFor();

    String addItemButtonLabel();

    String selectItemLabel();

    String selectItemTooltip();

    String affinity();

    String affinityGroupsDropDownInstruction();

    String affinityLabelsDropDownInstruction();

    String copyVmPermissions();

    String sealed();

    String sealTemplate();

    String server();

    String desktop();

    String statelessServer();

    String statelessDesktop();

    String desktopInPreview();

    String serverInPreview();

    String serverChanges();

    String desktopChanges();

    String statelessServerChanges();

    String statelessDesktopChanges();

    String statelessHighPerformance();

    String statelessHighPerformanceChanges();

    String pooledDesktop();

    String pooledServer();

    String pooledHighPerformance();

    String runOnce();

    String up();

    String rebooting();

    String waitForLaunchStatus();

    String imageLocked();

    String migrating();

    String suspended();

    String paused();

    String unknown();

    String down();

    String newAgent();

    String volumeBricksDown();

    String volumeAllBricksDown();

    String poweringUp();

    String restoring();

    String vmStatusSaving();

    String poweringDown();

    String notResponding();

    String UnlimitedNetworkQoS();

    String unlimitedQos();

    String noneQos();

    String noneFailover();

    String hostPasswordLabel();

    String hostPublicKeyLable();

    String memoryBalloonDeviceEnabled();

    String ioThreadsEnabled();

    String ioThreadsExplanation();

    String vmId();

    String templateId();

    String cpuShares();

    String networkProfilePopup();

    String isVirtioScsiEnabled();

    String isVirtioScsiEnabledInfo();

    String customScriptInfo();

    String ignitionScriptInfo();

    String diskInterfaceInfo();

    String scsiReservationInfoIcon();

    String fqdn();

    String snapshotDescriptionActiveVm();

    String snapshotDescriptionActiveVmBeforePreview();

    String waitForGlusterTask();

    String ssoMethod();

    String none();

    String guestAgent();

    String defineSpiceProxyEnable();

    String snapshotDisks();

    String vmConfiguration();

    String customPreviewSnapshotTableTitle();

    String snapshotCreationWithMemoryAndPartialDisksWarning();

    String snapshotCreationWithMemoryNotLiveWarning();

    String snapshotPreviewWithMemoryFromDifferentClusterVersion();

    String snapshotPreviewWithMemoryAndPartialDisksWarning();

    String snapshotPreviewWithExcludedDisksWarning();

    String previewPartialSnapshotSubsetDisksLabel();

    String previewPartialSnapshotQuestionLabel();

    String importAsTemplate();

    String importTemplateName();

    String migrationMode();

    String migrationDowntimeLabel();

    String logicalNetworks();

    String storageTargets();

    String name();

    String description();

    String iqn();

    String storageName();

    String storageIdVgName();

    String overrideSerialNumberPolicy();

    String connect();

    String bootMenuEnabled();

    String timeZoneInfo();

    String windowsHostNameInfo();

    String nameInstanceTypeGeneral();

    String descriptionInstanceTypeGeneral();

    String attachedToInstanceType();

    String detachedFromInstanceType();

    String GuestCpuCount();

    String GuestCpuType();

    String rngDevEnabled();

    String rngPeriod();

    String rngBytes();

    String deviceSource();

    String rngSourceRandom();

    String rngSourceUrandom();

    String rngSourceHwrng();

    String rngSourceUrandomOrRandom();

    String rngDeviceTab();

    String rngDevExplanation();

    String spiceFileTransferEnabled();

    String spiceCopyPasteEnabled();

    String namespace();

    String authz();

    String profileNameLabel();

    String profileDescriptionLabel();

    String qosName();

    String cpuQosName();

    String newProfile();

    String editProfile();

    String removeProfile();

    String cpuProfileLabel();

    String numaUnassignedInstructions();

    String unassignedVNumaNodesLabel();

    String numaSummaryTotals();

    String pinToNumaNode();

    String unPinNode();

    String numaTunaModeLabel();

    String numaNodeCountLabel();

    String numaSectionLabel();

    String numaSupportButtonLabel();

    String autoConvergeLabel();

    String migrateCompressedLabel();

    String migrateEncryptedLabel();

    String autoConverge();

    String dontAutoConverge();

    String compress();

    String dontCompress();

    String encrypt();

    String dontEncrypt();

    String inheritFromCluster();

    String inheritFromGlobal();

    String graphicsProtocol();

    String user();

    String group();

    String admin();

    String unassigned();

    String maintenance();

    String nonResponsive();

    String error();

    String installing();

    String installFailed();

    String reboot();

    String preparingForMaintenance();

    String nonOperational();

    String pendingApproval();

    String initializing();

    String connecting();

    String installingOS();

    String kdumping();

    String clickForHelp();

    String permissionsCreationDate();

    String addVmHostDevice();

    String removeVmHostDevice();

    String repinVmHost();

    String updateAvailable();

    String noWatchdogLabel();

    String clusterLevelUpgradeNeeded();

    String deviceGeneralType();

    String deviceType();

    String deviceAddress();

    String deviceReadOnlyAlias();

    String devicePluggedAlias();

    String deviceManagedAlias();

    String deviceSpecParamsAlias();

    String diskDeviceGeneralType();

    String interfaceDeviceGeneralType();

    String videoDeviceGeneralType();

    String graphicsDeviceGeneralType();

    String soundDeviceGeneralType();

    String controllerDeviceGeneralType();

    String balloonDeviceGeneralType();

    String channelDeviceGeneralType();

    String redirDeviceGeneralType();

    String consoleDeviceGeneralType();

    String rngDeviceGeneralType();

    String smartcardDeviceGeneralType();

    String watchdogDeviceGeneralType();

    String hostdevDeviceGeneralType();

    String memoryDeviceGeneralType();

    String unknownDeviceGeneralType();

    String guestOsArchitecture();

    String guestOsType();

    String guestOperatingSystem();

    String guestOsKernelInfo();

    String guestOsTimezone();

    String guestOSDiffers();

    String guestTimezoneDiffers();

    String guestAgentNotAvailable();

    String isRunOnce();

    String ExternalStatus();

    String foremanLabel();

    String providerLabel();

    String groups();

    String checkUiLogs();

    String vnicProfileNoFilter();

    String hostKernelTabLabel();

    String kernelCmdlineBootParamsLabel();

    String modifyingkernelCmdlineWarning();

    String reinstallRequiredkernelCmdlineWarning();

    String hostKernelCmdlineBlacklistNouveau();

    String hostKernelCmdlineIommu();

    String hostKernelCmdlineKvmNested();

    String hostKernelCmdlineUnsafeInterrupts();

    String hostKernelCmdlinePciRealloc();

    String hostKernelCmdlineFips();

    String hostKernelCmdlineSmt();

    String hostKernelCmdlineReset();

    String kernelCmdlineLabel();

    String hostedEngineLabel();

    String hostedEngineDeploymentAction();

    String hostRestartAfterUpgrade();

    String tsxRemovalInsecureCpuWarning();

    String imageFormat();

    String imageBackingFile();

    String imageQcowCompat();

    String imageContent();

    String imageActualSize();

    String imageVirtualSize();

    String diskAllocationTargetEnabled();

    String automaticNetworkProviderInstallWarning();

    String manualNetworkProviderInstallRecomendation();

    String useClusterDefaultNetworkProvider();

    String leaseStorageDomain();

    String emptyLeaseStorageDomain();

    String newDRSetup();

    String editDRSetup();

    String enableUsbSupportNotAvailable();

    String graphicalConsoleOptionsVmPopup();

    String serialConsoleOptionsVmPopup();

    String hotUnplug();

    String unplugging();

    String vmHasToBeUp();

    String deviceCantBeHotUnplugged();

    String headlessModeVmPopup();

    String headlessModeExplanation();

    String consolePublicKeyMessage();

    String kernelCmdlineInfoIcon();

    String kernelCmdlineBlacklistNouveauInfoIcon();

    String kernelCmdlineIommuInfoIcon();

    String kernelCmdlineKvmNestedInfoIcon();

    String kernelCmdlineUnsafeInterruptsInfoIcon();

    String kernelCmdlinePciReallocInfoIcon();

    String kernelCmdlineFipsInfoIcon();

    String kernelCmdlineSmtInfoIcon();

    String physMemGuarInfoIcon();

    String maxMemoryInfoIcon();

    String vmUrandomInfoIcon();

    String numOfCpuCoresTooltip();

    String xenUriInfo();

    String kvmUriInfo();

    String volatileRunOnce();

    String volatileRunInfo();

    String runningVmHasIllegalImages();

    String shutdownVmHasIllegalImages();

    String expandAll();

    String collapseAll();

    String diskType();

    String diskContentType();

    String memoryDisk();

    String otherMemoryDiskWillbeRemoved();

    String resumeBehavior();

    String userNameAboutLabel();

    String versionAboutLabel();

    String fencingOptionsLabel();

    String ovaHost();

    String ovaDir();

    String cloudInitProtocolLabel();

    String cloudInitProtocolInfo();

    String ignitionPasswordInfo();

    String storageDomainStatus();

    String forceToMaintenance();

    String systemDefaultOption();

    String vmMigrationPolicyLabel();

    String migrationDowntime();

    String vmSerialNumberPolicy();

    String vmCustomSerialNumber();

    String k8s_namespace();

    String targetStorageDomain();

    String ppcChipset();

    String s390xChipset();

    String tpmDeviceLabel();

    String typeToSearchPlaceHolder();

    String configChangesPending();
}
