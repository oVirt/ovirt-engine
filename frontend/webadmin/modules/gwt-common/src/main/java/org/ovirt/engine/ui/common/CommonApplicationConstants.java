package org.ovirt.engine.ui.common;

import com.google.gwt.i18n.client.Constants;

public interface CommonApplicationConstants extends Constants {
    String empty();

    String emptyListBoxText();

    String vendorUrl();

    String errorPopupCaption();

    String closeButtonLabel();

    String unAvailablePropertyLabel();

    String lineBreak();

    String htmlNonBreakingSpace();

    String htmlTab();

    String commaBreak();

    String andBreak();

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

    String runOncePopupInitialRunLabel();

    String runOnceSysPrepToEnableLabel();

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

    String cloudInitOrSysprep();

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

    String snapshotContainsMemory();

    String loadingLabel();

    String generalLabel();

    String loadLabel();

    String disksLabel();

    String networkFilterParametersLabel();

    String registerDisksLabel();

    String statistics();

    String guestAgentData();

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

    String approveOperation();

    String forceRemove();

    String yes();

    String no();

    String newVm();

    String cloneVm();

    String powerOffVm();

    String shutDownVm();

    String rebootVm();

    String pluggedVm();

    String nameVm();

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

    String monitorsVmPopup();

    String hostVmPopup();

    String anyHostInClusterVmPopup();

    String runOnSelectedHostVmPopup();

    String passThroughHostCpu();

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

    String runMigrationOptionsVmPopup();

    String priorForRunMigrationQueueVmPopup();

    String watchdog();

    String memAllocVmPopup();

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

    String plugged();

    String unplugged();

    String portMirroringEnabled();

    String portMirroringDisabled();

    String mbps();

    String bytes();

    String pkts();

    String mb();

    String gb();

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

    String getDiskAlignment();

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

    String cinderVolumeTypeDisk();

    String interfaceDisk();

    String statusDisk();

    String creationDateDisk();

    String diskSnapshotIDDisk();

    String descriptionDisk();

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

    String diskAlignment();

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

    String prestartedPoolPopup();

    String sizeVmDiskPopup();

    String extendImageSizeBy();

    String storageDomainVmDiskPopup();

    String hostVmDiskPopup();

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

    String cinderDisk();

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

    String imageTransferCancelled();

    String imageTransferFinalizingSuccess();

    String imageTransferFinalizingFailure();

    String imageTransferFinishedSuccess();

    String imageTransferFinishedFailure();

    String imageTransferringViaBrowser();

    String notAvailableLabel();

    String illegalStatus();

    String cpuPinningLabel();

    String cpuPinningLabelExplanation();

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

    String cinderDisksLabel();

    String currentQuota();

    String elementName();

    String assignQuota();

    String increaseNumberOfVMsInPoolBy();

    String vms();

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

    String singleQxlEnabled();

    String optimizedFor();

    String addItemButtonLabel();

    String selectItemLabel();

    String selectItemTooltip();

    String affinityLabels();

    String affinityLabelsDropDownInstruction();

    String copyVmPermissions();

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

    String newtools();

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

    String hostPasswordLabel();

    String hostPublicKeyLable();

    String memoryBalloonDeviceEnabled();

    String ioThreadsEnabled();

    String vmId();

    String templateId();

    String cpuShares();

    String networkProfilePopup();

    String isVirtioScsiEnabled();

    String isVirtioScsiEnabledInfo();

    String customScriptInfo();

    String diskInterfaceInfo();

    String cinderVolumeTypeInfoIcon();

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

    String overrideMigrationDowntimeLabel();

    String overrideMigrationPolicyLabel();

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

    String autoConverge();

    String dontAutoConverge();

    String compress();

    String dontCompress();

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

    String hostKernelCmdlineIommu();

    String hostKernelCmdlineKvmNested();

    String hostKernelCmdlineUnsafeInterrupts();

    String hostKernelCmdlinePciRealloc();

    String hostKernelCmdlineReset();

    String kernelCmdlineLabel();

    String hostedEngineLabel();

    String hostedEngineDeploymentCompatibilityWarning();

    String hostedEngineDeploymentAction();

    String hostRestartAfterUpgrade();

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

    String kernelCmdlineIommuInfoIcon();

    String kernelCmdlineKvmNestedInfoIcon();

    String kernelCmdlineUnsafeInterruptsInfoIcon();

    String kernelCmdlinePciReallocInfoIcon();

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

    String resumeBehavior();

    String userNameAboutLabel();

    String versionAboutLabel();

    String fencingOptionsLabel();

    String ovaHost();

    String ovaDir();
}
