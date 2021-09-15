package org.ovirt.engine.ui.webadmin;

import org.ovirt.engine.ui.common.CommonApplicationConstants;

public interface ApplicationConstants extends CommonApplicationConstants {

    String andMore();

    String applicationTitle();

    String aboutPopupCaption();

    String ovirtVersionAbout();

    String actionTableRefreshPageButtonLabel();

    String loginFormUserNameLabel();

    String loginFormPasswordLabel();

    String loginFormProfileLabel();

    String loginButtonLabel();

    String logMaxMemoryUsedThresholdLabel();

    String logMaxMemoryUsedThresholdLabelHelpMessage();

    String configureLinkLabel();

    String logoutLinkLabel();

    String optionsLinkLabel();

    String aboutLinkLabel();

    String guideLinkLabel();

    String searchLabel();

    String clearSearch();

    String applySearch();

    String bookmarkSearch();

    String existingBookmarks();

    String searchButtonLabel();

    String dataCenterMainViewLabel();

    String clusterMainViewLabel();

    String hostMainViewLabel();

    String networkMainViewLabel();

    String vnicProfilesMainViewLabel();

    String storageMainViewLabel();

    String virtualMachineMainViewLabel();

    String poolMainViewLabel();

    String templateMainViewLabel();

    String userMainViewLabel();

    String quotaMainViewLabel();

    String volumeMainViewLabel();

    String providerMainViewLabel();

    String errataMainViewLabel();

    String errataDetailsSubTabLabel();

    String activeUserSessionMainViewLabel();

    String volumeGeneralSubTabLabel();

    String volumeParameterSubTabLabel();

    String volumeBrickSubTabLabel();

    String volumeGeoRepSubTabLabel();

    String volumeSnapshotSubTabLabel();

    String volumeSubTabGeoRepSlaveClusterHostColumn();

    String volumeSubTabGeoRepSlaveVolumeColumn();

    String volumeSubTabGeoRepSlaveUserColumn();

    String volumeSubTabGeoRepStatusColumn();

    String volumeSubTabGeoRepUptime();

    String newGeoRepSession();

    String removeGeoRepSession();

    String startGeoRepSession();

    String stopGeoRepSession();

    String pauseGeoRepSession();

    String resumeGeoRepSession();

    String geoRepSessionsOptions();

    String geoRepSessionDetails();

    String geoRepSessionSync();

    String geoRepMasterVolumeToolTip();

    String geoRepSlaveVolumeToolTip();

    String volumePermissionSubTabLabel();

    String volumeEventSubTabLabel();

    String dataCenterStorageSubTabLabel();

    String dataCenterNetworkSubTabLabel();

    String dataCenterRecoveryStoragePopupMessageLabel();

    String dataCenterRecoveryStoragePopupSelectNewDSDLabel();

    String dataCenterForceRemovePopupWarningLabel();

    String clusterNewNetworkNameLabel();

    String clusterNewNetworkDescriptionLabel();

    String clusterPolicySchedulePolicyPanelTitle();

    String clusterPolicyAdditionalPropsPanelTitle();

    String clusterPolicyEnableTrustedServiceLabel();

    String clusterPolicyEnableHaReservationLabel();

    String clusterPolicyEnableReasonLabel();

    String clusterPolicyEnableHostMaintenanceReasonLabel();

    String additionalRngSource();

    String hostOptionChangeInfo();

    String ksmLabelTitle();

    String enableKsmLabel();

    String enableBallooningLabel();

    String ballooningLabelTitle();

    String clusterNewNetworkPopupVmNetworkLabel();

    String clusterNewNetworkPopupVlanEnabledLabel();

    String clusterNewNetworkPopupMtuEnabledLabel();

    String clusterNewNetworkPopupMtuLabel();

    String clusterManageNetworkPopupLabel();

    String dataCenterClusterSubTabLabel();

    String dataCenterQuotaSubTabLabel();

    String dataCenterNetworkQoSSubTabLabel();

    String dataCenterHostNetworkQosSubTabLabel();

    String dataCenterStorageQosSubTabLabel();

    String dataCenterCpuQosSubTabLabel();

    String dataCenterQosSubTabLabel();

    String dataCenterPermissionSubTabLabel();

    String dataCenterEventSubTabLabel();

    String nameLabel();

    String descriptionLabel();

    String externalLabel();

    String profilesLabel();

    String externalCheckboxLabel();

    String physicalNetworkCheckboxLabel();

    String physicalNetworkLabel();

    String physicalNetworkDatacenterLabel();

    String physicalNetworkCustomLabel();

    String externalProviderLabel();

    String portIsolationLabel();

    String hostProviderTabLabel();

    String hostProviderType();

    String computeLabel();

    String networkLabel();

    String networkLabelNetworksTab();

    @Override
    String commentLabel();

    String managementNetworkLabel();

    String vmNetworkLabel();

    String enableVlanTagLabel();

    String mtuLabel();

    String createSubnetLabel();

    String hostNetworkQos();

    String nameClusterHeader();

    String dataCenterPopupStorageTypeLabel();

    String uploadImage();

    String uploadImageStart();

    String uploadImageCancel();

    String uploadImagePause();

    String uploadImageResume();

    String uploadImageChooseFile();

    String uploadImageNoFileChosen();

    String downloadImage();

    String testImageIOProxyConnection();

    String testImageIOProxyConnectionSuccess();

    String storageTypeShared();

    String storageTypeLocal();

    String dataCenterPopupVersionLabel();

    String dataCenterPopupQuotaEnforceTypeLabel();

    String clusterPopupMacPoolLabel();

    String dataCenterEditNetworkPopupLabel();

    String dataCenterNewNetworkPopupLabel();

    String dataCenterNetworkPopupSubLabel();

    String networkPopupAssignLabel();

    String attachAll();

    String attach();

    String assignAll();

    String assign();

    String requireAll();

    String require();

    String storagePopupNameLabel();

    String storagePopupDescriptionLabel();

    String storagePopupDataCenterLabel();

    String storagePopupStorageTypeLabel();

    String storagePopupDomainFunctionLabel();

    String storagePopupFormatTypeLabel();

    String storagePopupHostLabel();

    String hostToUseToolTip();

    String activateDomainLabel();

    String wipeAfterDelete();

    String discardAfterDelete();

    String storagePopupNfsPathLabel();

    String storagePopupConnectionLabel();

    String storagePopupNfsOverrideLabel();

    String storagePopupNfsVersionLabel();

    String storagePopupNfsRetransmissionsLabel();

    String storagePopupNfsTimeoutLabel();

    String storagePopupPosixPathLabel();

    String storagePopupVfsTypeLabel();

    String StoragePopupDriverOptionsLabel();

    String StoragePopupDriverSensitiveOptionsLabel();

    String storagePopupMountOptionsLabel();

    String storagePopupAdditionalMountOptionsLabel();

    String storagePopupLocalPathLabel();

    String storagePopupNfsPathExampleLabel();

    String storagePopupGlusterPathExampleLabel();

    String storagePopupPosixPathExampleLabel();

    String storagePopupPosixNfsWarningLabel();

    String storagePopupLinkGlusterVolumeLabel();

    String storagePopupGlusterVolume();

    String storageRemovePopupHostLabel();

    String storageRemovePopupFormatLabel();

    String storageDestroyPopupWarningLabel();

    String storageGeneralSubTabLabel();

    String storageDataCenterSubTabLabel();

    String storageVmBackupSubTabLabel();

    String storageTemplateBackupSubTabLabel();

    String storageDiskBackupSubTabLabel();

    String storageVmSubTabLabel();

    String storageTemplateSubTabLabel();

    String storageImagesSubTabLabel();

    String storageDRSubTabLabel();

    String storageLeaseSubTabLabel();

    String storagePermissionSubTabLabel();

    String storageEventSubTabLabel();

    String clusterGeneralSubTabLabel();

    String clusterPolicySubTabLabel();

    String clusterHostSubTabLabel();

    String clusterVmSubTabLabel();

    String clusterNetworkSubTabLabel();

    String clusterServiceSubTabLabel();

    String clusterGlusterHooksSubTabLabel();

    String clusterEventSubTabLabel();

    String affinityGroupSubTabLabel();

    String affinityLabelsImplicitGroupInfo();

    String affinityLabelsImplicitGroupLabel();

    String affinityLabelsNameLabel();

    String affinityLabelsSubTabLabel();

    String affinityLabelsNameColumnLabel();

    String affinityLabelsNoMembers();

    String affinityLabelsVmsColumnLabel();

    String affinityLabelsHostsColumnLabel();

    String affinityLabelsSubTabNewButton();

    String affinityLabelsSubTabEditButton();

    String affinityLabelsSubTabDeleteButton();

    String clusterPermissionSubTabLabel();

    String virtualMachineGeneralSubTabLabel();

    String virtualMachineNetworkInterfaceSubTabLabel();

    String virtualMachineVirtualDiskSubTabLabel();

    String virtualMachineHostDeviceSubTabLabel();

    String virtualMachineSnapshotSubTabLabel();

    String virtualMachineApplicationSubTabLabel();

    String virtualMachineContainerSubTabLabel();

    String virtualMachinePermissionSubTabLabel();

    String virtualMachineGuestInfoSubTabLabel();

    String virtualMachineEventSubTabLabel();

    String virtualMachineErrataSubTabLabel();

    String hostGeneralSubTabLabel();

    String hostGeneralInfoSubTabLabel();

    String hostGeneralSoftwareSubTabLabel();

    String hostGeneralHardwareSubTabLabel();

    String hostGeneralErrataSubTabLabel();

    String hostVmSubTabLabel();

    String hostIfaceSubTabLabel();

    String hostDeviceSubTabLabel();

    String hostHookSubTabLabel();

    String hostHugePages();

    String hostGlusterSwiftSubTabLabel();

    String hostBricksSubTabLabel();

    String hostPermissionSubTabLabel();

    String hostEventSubTabLabel();

    String hostPopupGeneralTabLabel();

    String hostPopupPowerManagementTabLabel();

    String detachFenceAgentFromGroup();

    String concurrentAgentGroupLabel();

    String agentsBySequentialOrder();

    String addNewFenceAgent();

    String hostPopupMemoryOptimizationTabLabel();

    String hostPopupDataCenterLabel();

    String hostPopupClusterLabel();

    String hostPopupNameLabel();

    String hostPopupPortLabel();

    String hostPopupHostAddressLabel();

    String hostPopupHostAddressLabelHelpMessage();

    String hostPopupHostPublicKeyLabel();

    String hostPopupUsernameLabel();

    String hostPopupFqdnLabel();

    String hostPopupDefaultUsername();

    String hostPopupPublicKeyLabel();

    String hostPopupPasswordLabel();

    String hostPopupAuthLabel();

    String hostPopupAuthLabelForExternalHost();

    String hostPopupOverrideIpTablesLabel();

    String hostPopupProtocolLabel();

    String hostPopupEnableExternalHostProvider();

    String hostPopupEnableExternalHostProviderHelpMessage();

    String hostPopupUpdateHosts();

    String hostPopupPmEnabledLabel();

    String hostPopupPmConcurrent();

    String hostPopupPmAddressLabel();

    String hostPopupPmUserNameLabel();

    String hostPopupPmPasswordLabel();

    String hostPopupPmTypeLabel();

    String hostPopupPmPortLabel();

    String hostPopupPmSlotLabel();

    String hostPopupPmCiscoUcsSlotLabel();

    String hostPopupPmOptionsLabel();

    String hostPopupPmOptionsExplanationLabel();

    String hostPopupPmSecureLabel();

    String hostPopupPmEncryptOptionsLabel();

    String hostPopupPmDisableAutoPM();

    String hostPopupPmKdumpDetection();

    String hostPopupTestButtonLabel();

    String hostPopupUpButtonLabel();

    String hostPopupDownButtonLabel();

    String hostPopupFetchButtonLabel();

    String hostPopupSourceText();

    String hostProxyPreferenceTypeLabel();

    String noHostProxyPrefenceTypeAvailableLabel();

    String hostPopupAddProxyPreferenceType();

    String spmTestButtonLabel();

    String consoleButtonLabel();

    String vgpuPlacementLabel();

    String vgpuPlacementInfoIcon();

    String vgpuConsolidatedPlacementLabel();

    String vgpuSeparatedPlacementLabel();

    String enableConsoleAddressOverride();

    String enableConsoleAddressOverrideHelpMessage();

    String consoleAddress();

    String spmNeverText();

    String spmLowText();

    String spmNormalText();

    String spmHighText();

    String spmCustomText();

    String hostInstallPasswordLabel();

    String hostInstallHostVersionLabel();

    String hostInstallIsoLabel();

    String hostInstallOverrideIpTablesLabel();

    String activateHostAfterInstallLabel();

    String rebootHostAfterInstallLabel();

    String rebootHostAfterInstallLabelHelpMessage();

    String reconfigureGlusterLabel();

    String importVmGeneralSubTabLabel();

    String importVmNetworkIntefacesSubTabLabel();

    String importVmDisksSubTabLabel();

    String importVmApplicationslSubTabLabel();

    String configureLocalStoragePopupPathLabel();

    String manaulFencePopupNoneSpmWarningLabel();

    String manaulFencePopupSpmWarningLabel();

    String manaulFencePopupContendingSpmWarningLabel();

    String manaulFencePopupWarningLabel();

    String poolGeneralSubTabLabel();

    String poolVmSubTabLabel();

    String poolPermissionSubTabLabel();

    String templateGeneralSubTabLabel();

    String templateVmSubTabLabel();

    String templateInterfaceSubTabLabel();

    String templateDiskSubTabLabel();

    String templateStorageSubTabLabel();

    String templatePermissionSubTabLabel();

    String templateEventSubTabLabel();

    String userGeneralSubTabLabel();

    String userPermissionSubTabLabel();

    String userQuotaSubTabLabel();

    String userGroupsSubTabLabel();

    String userEventNotifierSubTabLabel();

    String userEventSubTabLabel();

    String eventMainViewLabel();

    String eventBasicViewLabel();

    String eventAdvancedViewLabel();

    String clusterPopupGeneralTabLabel();

    String glusterTunedProfileLabel();

    String clusterPopupDataCenterLabel();

    String clusterPopupNameLabel();

    String clusterPopupDescriptionLabel();

    String clusterPopupCPUTypeLabel();

    String clusterPopupBiosTypeLabel();

    String clusterPopupBiosTypeInfoIcon();

    String clusterPopupArchitectureLabel();

    String clusterPopupFipsModeLabel();

    String clusterPopupVersionLabel();

    String clusterSwitchTypeLabel();

    String clusterFirewallTypeLabel();

    String clusterDefaultNetworkProviderLabel();

    String clusterPopupOptimizationTabLabel();

    String clusterPopupMemoryOptimizationPanelTitle();

    String clusterPopupMemoryOptimizationInfo();

    String clusterPopupOptimizationNoneLabel();

    String clusterPopupCpuThreadsPanelTitle();

    String clusterPopupCpuThreadsInfo();

    String clusterPopupCpuSmtLabel();

    String clusterPopupCpuSmtTitle();

    String clusterCpuSmtInfo();

    String clusterPopupCountThreadsAsCoresLabel();

    String clusterPopupMigrationTabLabel();

    String clusterMigrationPolicyLabel();

    String clusterPopupResiliencePolicyLabel();

    String clusterAdditionalMigrationProperties();

    String clusterPopupBandwidthLabel();

    String clusterPopupClusterPolicyTabLabel();

    String clusterPopupMigrateOnError_YesLabel();

    String clusterPopupMigrateOnError_HaLabel();

    String clusterPopupMigrateOnError_NoLabel();

    String clusterPopupMigrationSpeed();

    String clusterPopupNumberOfConcurrentMigrationsPerHost();

    String clusterPopupMigrationBandwidthLimit();

    String clusterSpiceProxyInfo();

    String clusterSpiceProxyEnable();

    String fencingEnabled();

    String skipFencingIfSDActive();

    String skipFencingWhenConnectivityBroken();

    String hostsWithBrokenConnectivityThresholdLabel();

    String bookmarkPopupNameLabel();

    String bookmarkPopupSearchStringLabel();

    String bookmarkOverlayNoBookmarksFound();

    String tagsOverlayNoTagsFound();

    String tagPopupNameLabel();

    String tagPopupDescriptionLabel();

    String clusterPolicyNoneLabel();

    String clusterPolicyEvenDistLabel();

    String clusterPolicyPowSaveLabel();

    String clusterPolicyMaxServiceLevelLabel();

    String clusterPolicyMinServiceLevelLabel();

    String clusterPolicyForTimeLabel();

    String clusterPolicyMinTimeLabel();

    String clusterPolicyEditPolicyButtonLabel();

    String clusterVolumesLabel();

    String clusterVolumesTotalLabel();

    String clusterVolumesUpLabel();

    String clusterVolumesDownLabel();

    String clusterGlusterSwiftLabel();

    String clusterGlusterSwiftManageLabel();

    String startGlusterSwift();

    String stopGlusterSwift();

    String restartGlusterSwift();

    String manageServerLevelGlusterSwift();

    String hostGlusterSwift();

    String serviceNameGlusterSwift();

    String serviceStatusGlusterSwift();

    String clusterPolicyPolicyLabel();

    @DefaultStringValue("") // Use annotation and not a properties key to leave it out of translations
    String copyRightNotice();

    String configurePopupTitle();

    String allRolesLabel();

    String adminRolesLabel();

    String userRolesLabel();

    String showRolesLabel();

    String RoleNameLabel();

    String RoleDescriptionLabel();

    String RoleAccount_TypeLabel();

    String RoleUserLabel();

    String RoleAdminLabel();

    String RoleCheckBoxes();

    String RoleExpand_AllLabel();

    String RoleCollapse_AllLabel();

    String configureRoleTabLabel();

    String nameRole();

    String descriptionRole();

    String newRole();

    String editRole();

    String copyRole();

    String removeRole();

    String configureSystemPermissionTabLabel();

    String vmExportPopupForceOverrideLabel();

    String vmExportPopupCollapseSnapshotsLabel();

    String importVm_destCluster();

    String importVm_destClusterQuota();

    String quotaExceeded();

    String treeExpandAll();

    String treeCollapseAll();

    String manageEventsPopupEmailLabel();

    String manageEventsPopupTitleLabel();

    String manageEventsPopupInfoLabel();

    String guidePopupRequiredActionsLabel();

    String guidePopupOptionalActionsLabel();

    String guidePopupUnconfiguredLabel();

    String guidePopupConfigurationCompletedLabel();

    String guidePopupDataCenterCreatedLabel();

    String guidePopupConfiguredDataCenterLabel();

    String guidePopupClusterCreatedLabel();

    String guidePopupConfiguredClusterLabel();

    String guidePopupVMCreatedLabel();

    String guidePopupConfiguredVmLabel();

    String moveHostPopupClusterLabel();

    String entitiesFromDifferentDCsError();

    String differentStorageDomainWarning();

    String editText();

    String closeText();

    String importAllocationModifiedSingleVM();

    String importAllocationModifiedCollapse();

    String preallocatedAllocation();

    String thinAllocation();

    String quotaClusterSubTabLabel();

    String clusterChangeToQ35();

    String clusterEnableOvirtServiceLabel();

    String clusterEnableGlusterServiceLabel();

    String clusterImportGlusterConfigurationLabel();

    String clusterImportGlusterConfigurationExplanationLabel();

    String quotaStorageSubTabLabel();

    String quotaUserSubTabLabel();

    String quotaPermissionSubTabLabel();

    String quotaEventSubTabLabel();

    String quotaVmSubTabLabel();

    String quotaTemplateSubTabLabel();

    String diskMainViewLabel();

    String diskGeneralSubTabLabel();

    String diskVmSubTabLabel();

    String diskPermissionSubTabLabel();

    String diskTemplateSubTabLabel();

    String diskStorageSubTabLabel();

    String newDC();

    String editDC();

    String removeDC();

    String forceRemoveDC();

    String guideMeDc();

    String reinitializeDC();

    String nameDc();

    String storgeTypeDc();

    String statusDc();

    String statusIconDc();

    String comptVersDc();

    String descriptionDc();

    String configureMacPoolsTabLabel();

    String configureMacPoolNameColumn();

    String configureMacPoolDescriptionColumn();

    String configureMacPoolAddButton();

    String configureMacPoolEditButton();

    String configureMacPoolRemoveButton();

    String macPoolPopupName();

    String macPoolPopupDescription();

    String macCountLabel();

    String dataCenterGeneralTab();

    String clusterMacPoolTab();

    String statusStorageDc();

    String domainStatusInDcStorageDc();

    String attachStorageDc();

    String detachStorageDc();

    String activateStorageDc();

    String maintenanceStorageDc();

    String generalTabNetworkPopup();

    String clusterTabNetworkPopup();

    String profilesTabNetworkPopup();

    String subnetTabNetworkPopup();

    String networkGeneralSubTabLabel();

    String networkExternalSubnetSubTabLabel();

    String profilePermissions();

    String networkClusterSubTabLabel();

    String networkHostSubTabLabel();

    String networkVmSubTabLabel();

    String networkTemplateSubTabLabel();

    String networkPermissionSubTabLabel();

    String networkPopupDataCenterLabel();

    String hostOutOfSync();

    String hostsOutOfSyncWarning();

    String hostForOutOfSyncSentence();

    String dcForOutOfSyncSentence();

    String hostOutOfSyncPreviewSentence();

    String PropertyOutOfSyncPopUp();

    String hostOutOfSyncPopUp();

    String dcOutOfSyncPopUp();

    String mtuOutOfSyncPopUp();

    String bridgedOutOfSyncPopUp();

    String vlanOutOfSyncPopUp();

    String ipv4ItemInfo();

    String ipv4BootProtocolOutOfSyncPopUp();

    String ipv4AddressOutOfSyncPopUp();

    String ipv4NetmaskOutOfSyncPopUp();

    String ipv4GatewayOutOfSyncPopUp();

    String ipv6ItemInfo();

    String ipv6BootProtocolOutOfSyncPopUp();

    String ipv6AddressOutOfSyncPopUp();

    String ipv6PrefixOutOfSyncPopUp();

    String ipv6GatewayOutOfSyncPopUp();

    String linkLayerInfo();

    String noImportantLLDP();

    String lldpInfoDisabled();

    String fetchingLldpInfo();

    String noLldpInfoAvailable();

    String outAverageLinkShareOutOfSyncPopUp();

    String outAverageUpperLimitOutOfSyncPopUp();

    String switchTypeOutOfSyncPopUp();

    String dnsConfigurationOutOfSyncPopUp();

    String defaultRouteOutOfSyncPopUp();

    String outAverageRealTimeOutOfSyncPopUp();

    String unknownPropertyOutOfSyncPopUp();

    String nameQuotaStorage();

    String usedStorageTotalQuotaStorage();

    String networkVnicProfile();

    String nameVnicProfile();

    String dcVnicProfile();

    String compatibilityVersionVnicProfile();

    String descriptionVnicProfile();

    String qosNameVnicProfile();

    String networkFilterNameVnicProfile();

    String passthroughVnicProfile();

    String migratableVnicProfile();

    String failoverVnicProfile();

    String portMirroringVnicProfile();

    String customPropertiesVnicProfile();

    String publicUseVnicProfile();

    String vnicProfileVmSubTabLabel();

    String vnicProfileTemplateSubTabLabel();

    String vnicProfilePermissionSubTabLabel();

    String newVnicProfile();

    String editVnicProfile();

    String removeVnicProfile();

    String externalIdExternalSubnet();

    String networkExternalSubnet();

    String nameExternalSubnet();

    String cidrExternalSubnet();

    String ipVersionExternalSubnet();

    String gatewayExternalSubnet();

    String dnsServersExternalSubnet();

    String nameCluster();

    String idCluster();

    String dcCluster();

    String attachedNetworkCluster();

    String comptVersCluster();

    String requiredNetCluster();

    String roleNetCluster();

    String descriptionCluster();

    String hostCount();

    String vmCount();

    String cpuTypeCluster();

    String newCluster();

    String editCluster();

    String removeCluster();

    String guideMeCluster();

    String resetClusterEmulatedMachine();

    String usedMemoryTotalCluster();

    String runningCpuTotalCluster();

    String nameHost();

    String ipHost();

    String clusterHost();

    String dcHost();

    String statusCluster();

    String statusClusterUpgrade();

    String statusHost();

    String statusIconHost();

    String additionalStatusHost();

    String additionalStatusCluster();

    String additionalStatusDataCenter();

    String vmsCount();

    String memoryHost();

    String cpuHost();

    String networkHost();

    String spmPriorityHost();

    String newHost();

    String editHost();

    String removeHost();

    String hostConsole();

    String activateHost();

    String maintenanceHost();

    String selectHostAsSPM();

    String confirmRebootedHost();

    String approveHost();

    String reinstallHost();

    String checkForHostUpgrade();

    String upgradeHost();

    String management();

    String sshManagement();

    String installation();

    String configureLocalStorageHost();

    String restartHost();

    String startHost();

    String stopHost();

    String pmHost();

    String assignTagsHost();

    String refreshHostCapabilities();

    String enrollCertificate();

    String hostHaMaintenance();

    String osVersionHostGeneral();

    String osPrettyName();

    String hardwareManufacturerGeneral();

    String hardwareProductNameGeneral();

    String hardwareVersionGeneral();

    String hardwareSerialNumberGeneral();

    String hardwareUUIDGeneral();

    String hardwareFamilyGeneral();

    String hardwareHBAInventory();

    String hbaModelName();

    String hbaDeviceType();

    String hbaWWNN();

    String hbaWWPNs();

    String kernelVersionHostGeneral();

    String kvmVersionHostGeneral();

    String libvirtVersionHostGeneral();

    String vdsmVersionHostGeneral();

    String spiceVersionHostGeneral();

    String glusterVersionHostGeneral();

    String cephVersionHostGeneral();

    String kernelFeatures();

    String isciInitNameHostGeneral();

    String activeVmsHostGeneral();

    String logicalCores();

    String onlineCores();

    String cpuModelHostGeneral();

    String cpuTypeHostGeneral();

    String numOfSocketsHostGeneral();

    String numOfCoresPerSocketHostGeneral();

    String numOfThreadsPerCoreHostGeneral();

    String physMemHostGeneral();

    String volumeCapacityStatistics();

    String swapSizeHostGeneral();

    String maxSchedulingMemory();

    String memPageSharingHostGeneral();

    String autoLargePagesHostGeneral();

    String sharedMemHostGeneral();

    String hostedEngineHaHostGeneral();

    String actionItemsHostGeneral();

    String bootTimeHostGeneral();

    String kdumpStatus();

    String selinuxModeGeneral();

    String clusterCompatibilityVersion();

    String sharedStatusStorage();

    String additionalStatusStorage();

    String additionalStatusTemplate();

    String domainNameStorage();

    String domainDescriptionStorage();

    String domainTypeStorage();

    String storageTypeStorage();

    String formatStorage();

    String crossDcStatusStorage();

    String freeSpaceStorage();

    String confirmedFreeSpaceStorage();

    String confirmedFreeSpaceStorageNonThinTooltip();

    String confirmedFreeSpaceStorageThinTooltip();

    String newDomainStorage();

    String importDomainStorage();

    String editStorage();

    String removeStorage();

    String updateOvfsForStorage();

    String destroyStorage();

    String scanDisksStorage();

    String selectStorageDomainAsMaster();

    String statusStorage();

    String statusIconStorage();

    String usedSpaceStorage();

    String totalSpaceStorage();

    String attachDataStorage();

    String attachIsoStorage();

    String attachExportStorage();

    String detachStorage();

    String activateStorage();

    String maintenanceStorage();

    String nameStorage();

    String typeStorage();

    String clickToEdit();

    String storageDomainIdGeneral();

    String sizeStorageGeneral();

    String availableStorageGeneral();

    String usedStorageGeneral();

    String allocatedStorageGeneral();

    String overAllocRatioStorageGeneral();

    String numberOfImagesStorageGeneral();

    String pathStorageGeneral();

    String vfsTypeStorageGeneral();

    String mountOptionsGeneral();

    String nfsVersionGeneral();

    String nfsRetransmissionsGeneral();

    String nfsTimeoutGeneral();

    String uploadImageSourceLabel();

    String uploadImageSourceLocal();

    String uploadImageSourceRemote();

    String uploadImageFileLabel();

    String uploadImageUploadNotSupportedMessage();

    String uploadImageDiskOptionsLabel();

    String uploadImageDiskOptionsInfoOnlyLabel();

    String warningLowSpaceIndicatorUnits();

    String warningLowConfirmedSpaceIndicatorUnits();

    String criticalSpaceActionBlockerUnits();

    String warningLowSpaceIndicator();

    String criticalSpaceActionBlocker();

    String changeCriticalSpaceActionBlockerWarning();

    String clusterVm();

    String attachmentToCurHost();

    String runningOnCurHost();

    String pinnedToCurHost();

    String runningAndPinnedOnCurHost();

    String allowPartial();

    String dcVm();

    String hostVm();

    String ipVm();

    String memoryVm();

    String memoryVmMB();

    String cpuVm();

    String cpusVm();

    String architectureVm();

    String networkVm();

    String migrationProgress();

    String networksUpdating();

    String networkUpdating();

    String graphicsVm();

    String statusVm();

    String statusIconVm();

    String uptimeVm();

    String editVm();

    String removeVm();

    String runOnceVm();

    String runVm();

    String suspendVm();

    String consoleVm();

    String cancelMigrationVm();

    String cancelConvertVm();

    String makeTemplateVm();

    String exportVm();

    String exportToExportDomain();

    String exportToOva();

    String createSnapshotVM();

    String changeCdVm();

    String assignTagsVm();

    String enableGlobalHaMaintenanceVm();

    String disableGlobalHaMaintenanceVm();

    String setConsoleKey();

    String guideMeVm();

    String disksVm();

    String vSizeVm();

    String actualSizeVm();

    String creationDateVm();

    String exportDateVm();

    String detachVm();

    String restoreVm();

    String namePool();

    String assignVmsPool();

    String runningVmsPool();

    String typePool();

    String descriptionPool();

    String newPool();

    String editPool();

    String removePool();

    String nameTemplate();

    String aliasTemplate();

    String domainTemplate();

    String creationDateTemplate();

    String exportDateTemplate();

    String statusTemplate();

    String sealedTemplate();

    String sealedTemplateTrueValue();

    String clusterTemplate();

    String dcTemplate();

    String descriptionTemplate();

    String versionTemplate();

    String editTemplate();

    String removeTemplate();

    String exportTemplate();

    String copyTemplate();

    String createVmFromTemplate();

    String disksTemplate();

    String provisionedSizeTemplate();

    String actualSizeTemplate();

    String originTemplate();

    String memoryTemplate();

    String cpusTemplate();

    String architectureTemplate();

    String restoreTemplate();

    String statusUser();

    String firstnameUser();

    String lastNameUser();

    String userNameUser();

    String userId();

    String authorizationProvider();

    String sessionDbId();

    String sessionStartTime();

    String sessionLastActiveTime();

    String sourceIp();

    String groupUser();

    String groupNameUser();

    String emailUser();

    String addUser();

    String removeUser();

    String assignTagsUser();

    String userUser();

    String inheritedFromUser();

    String emailUserGeneral();

    String dcStatusQuota();

    String nameQuota();

    String descriptionQuota();

    String dcQuota();

    String freeMemory();

    String freeVcpu();

    String freeStorage();

    String usedMemoryQuota();

    String runningCpuQuota();

    String usedStorageQuota();

    String unlimited();

    String exceeded();

    String addQuota();

    String editQuota();

    String copyQuota();

    String removeQuota();

    String storageNameQuota();

    String clusterNameQuota();

    String quotaOfMemQuota();

    String quotaOfVcpuQuota();

    String vcpus();

    String quota();

    String editCellQuota();

    String attachedNetwork();

    String nameNetwork();

    String idNetwork();

    String dcNetwork();

    String vlanNetwork();

    String providerNetwork();

    String mtuNetwork();

    String portIsolationNetwork();

    String mtuDefault();

    String requiredNetwork();

    String nonRequiredNetwork();

    String vmNetwork();

    String trueVmNetwork();

    String falseVmNetwork();

    String statusNetwork();

    String statusIconNetwork();

    String displayNetwork();

    String migrationNetwork();

    String glusterNetwork();

    String defaultRouteNetwork();

    String roleNetwork();

    String descriptionNetwork();

    String addNetworkNetwork();

    String assignDetatchNetworksNework();

    String assignUnassignNetwork();

    String setAsDisplayNetwork();

    String newNetwork();

    String importNetwork();

    String editNetwork();

    String removeNetwork();

    String noneVlan();

    String newNetworkProfile();

    String editNetworkProfile();

    String removeNetworkProfile();

    String newNetworkExternalSubnet();

    String removeNetworkExternalSubnet();

    String nameClusterHost();

    String hostIpClusterHost();

    String statusClusterHost();

    String statusIconClusterHost();

    String vmsClusterHost();

    String loadClusterHost();

    String updateMomPolicyClusterHost();

    String hostService();

    String nameService();

    String statusService();

    String portService();

    String rdmaPortService();

    String pidService();

    String filterService();

    String showAllService();

    String overriddenConsoleAddress();

    String nameHook();

    String glusterVolumeEventHook();

    String stageHook();

    String statusHook();

    String contentTypeHook();

    String enableHook();

    String disableHook();

    String viewHookContent();

    String resolveConflictsGlusterHook();

    String syncWithServersGlusterHook();

    String conflictReasonsGlusterHook();

    String conflictReasonContentGlusterHook();

    String conflictReasonStatusGlusterHook();

    String conflictReasonMissingGlusterHook();

    String contentSourcesGlusterHook();

    String sourceGlusterHook();

    String contentGlusterHook();

    String checksumGlusterHook();

    String statusGlusterHook();

    String resolveActionsGlusterHook();

    String resolveContentConflictGlusterHook();

    String useContentSourceGlusterHook();

    String useContentSourceWarningGlusterHook();

    String resolveStatusConflictGlusterHook();

    String statusEnableGlusterHook();

    String statusDisableGlusterHook();

    String resolveMissingConflictGlusterHook();

    String resolveMissingConflictCopyGlusterHook();

    String resolveMissingConflictRemoveGlusterHook();

    String removeMissingWarningGlusterHook();

    String emptyInterface();

    String ipv4AddressInterface();

    String ipv6AddressInterface();

    String bondInterface();

    String vlanInterface();

    String saveNetConfigInterface();

    String setupHostNetworksInterface();

    String syncAllHostNetworks();

    String syncAllClusterNetworks();

    String dateCreatedInterface();

    String eventNameHook();

    String scriptNameHook();

    String propertyNameHook();

    String propertyValueHook();

    String serviceGlusterSwift();

    String statusGlusterSwift();

    String startGlusterSwiftInHost();

    String stopGlusterSwiftInHost();

    String restartGlusterSwiftInHost();

    String volumeName();

    String deviceName();

    String mdevTypes();

    @Override
    String deviceType();

    String fileSystemType();

    String size();

    String storageDevices();

    String brickDirectory();

    String groupNameGroup();

    String namespaceGroup();

    String eventNameEventNotifier();

    String manageEventsEventNotifier();

    String inheretedFromPermission();

    String ultQuotaPopup();

    String useQuotaPopup();

    String memQuotaPopup();

    String cpuQuotaPopup();

    String storageQuotaQuotaPopup();

    String nameQuotaPopup();

    String descriptionQuotaPopup();

    String dataCenterQuotaPopup();

    String copyQuotaPermissionsQuotaPopup();

    String memAndCpuQuotaPopup();

    String storageQuotaPopup();

    String ultQuotaForAllClustersQuotaPopup();

    String useQuotaSpecificClusterQuotaPopup();

    String utlQuotaAllStoragesQuotaPopup();

    String usedQuotaSpecStoragesQuotaPopup();

    String eventIdEvent();

    String userEvent();

    String hostEvent();

    String vmEvent();

    String templateEvent();

    String dcEvent();

    String storageEvent();

    String clusterEvent();

    String volumeEvent();

    String eventCorrelationId();

    String eventOrigin();

    String eventCustomEventId();

    String dcLocalStorage();

    String clusterLocalStorage();

    String storageLocalStorage();

    String confirmOperation();

    String domainNameDisksTree();

    String domainTypeDisksTree();

    String statusDisksTree();

    String freeSpaceDisksTree();

    String usedSpaceDisksTree();

    String totalSpaceDisksTree();

    String diskDisksTree();

    String newBookmark();

    String editBookmark();

    String removeBookmark();

    String copy2ClipAbout();

    String osVerAbout();

    String vdsmVerAbout();

    String noHostsAbout();

    String lastMsgEventFooter();

    String alertsEventFooter();

    String tasksEventFooter();

    String eventsEventFooter();

    String lastTaskEventFooter();

    String clearAllDismissedAlerts();

    String clearAllDismissedEvents();

    String displayAllDismissedAlerts();

    String displayAllDismissedEvents();

    String dismissAlert();

    String dismissEvent();

    String loggedInUser();

    String engineWebAdminDoc();

    String saveNetCongDetachConfirmPopup();

    String systemMainSection();

    String bookmarksMainSection();

    String tagsMainSection();

    String customHostPopup();

    String bondNameHostPopup();

    String bondingModeHostPopup();

    String customModeHostPopup();

    String bootProtocolHostPopup();

    String ipHostPopup();

    String subnetMaskHostPopup();

    String subnetPrefixHostPopup();

    String gwHostPopup();

    String customPropertiesHostPopup();

    String checkConHostPopup();

    String labelsVfsConfigPopup();

    String syncNetwork();

    String saveNetConfigHostPopup();

    String nameHostPopup();

    String intefaceHostPopup();

    String checkConnectivityManageConfirmPopup();

    String youAreAboutManageConfirmPopup();

    String thisMightCauseManageConfirmPopup();

    String itIsManageConfirmPopup();

    String highlyRecommendedManageConfirmPopup();

    String toProceeedWithConnectivityCheckManageConfirmPopup();

    String hostsPopupUseCommonPassword();

    String configureFirewallForAllHostsOfThisCluster();

    String hostsPopupRootPassword();

    String hostsPopupApply();

    String hostsPopupSshPublicKey();

    String addTag();

    String newTag();

    String editTag();

    String removeTag();

    String fileNameIso();

    String typeIso();

    String importImage();

    String sizeStorageTree();

    String statusStorageTree();

    String allocationStorageTree();

    String interfaceStorageTree();

    String creationDateStorageTree();

    String generalImpTempTab();

    String networkIntImpTempTab();

    String disksImpTempTab();

    String serverVolumeBrick();

    String brickDirectoryVolumeBrick();

    String freeSpaceGBVolumeBrick();

    String totalSpaceGBVolumeBrick();

    String statusVolumeBrick();

    String noNetworkAssigned();

    String nameItemInfo();

    String usageItemInfo();

    String vmItemInfo();

    String displayItemInfo();

    String migrationItemInfo();

    String defaultRouteItemInfo();

    String glusterNwItemInfo();

    String unmanagedNetworkItemInfo();

    String unmanagedNetworkDescriptionItemInfo();

    String unmanagedNetworkItemTitle();

    String managementItemInfo();

    String mtuItemInfo();

    String bootProtocolItemInfo();

    String addressItemInfo();

    String subnetItemInfo();

    String gatewayItemInfo();

    String prefixItemInfo();

    String bondOptionsItemInfo();

    String dataCenterVolume();

    String volumeClusterVolume();

    String stripeCountVolume();

    String transportTypeVolume();

    String tcpVolume();

    String rdmaVolume();

    String addBricksVolume();

    String typeVolume();

    String bricksVolume();

    String accessProtocolsVolume();

    String glusterVolume();

    String nfsVolume();

    String cifsVolume();

    String allowAccessFromVolume();

    String allowAccessFromLabelVolume();

    String optimizeForVirtStoreVolume();

    String nameVolume();

    String clusterVolume();

    String volumeIdVolume();

    String volumeTypeVolume();

    String bricksStatusVolume();

    String volumeInfoVolume();

    String numberOfBricksVolume();

    String replicaCountVolume();

    String transportTypesVolume();

    String maxNumberOfSnapshotsVolume();

    String disperseCount();

    String redundancyCount();

    String activitiesOnVolume();

    String healInfo();

    String statusVolume();

    String newVolume();

    String removeVolume();

    String volumeCapacity();

    String startVolume();

    String stopVolume();

    String rebalanceVolume();

    String rebalanceStartTime();

    String rebalanceStopTime();

    String optimizeForVirtStore();

    String addressInterfaceEditor();

    String subnetInterfaceEditor();

    String gatewayInterfaceEditor();

    String protocolInterfaceEditor();

    String idDisk();

    @Override
    String quotaDisk();

    String volumeFormatDisk();

    String registerDisk();

    String registerDiskImage();

    String newLibvirtSecret();

    String editLibvirtSecret();

    String removeLibvirtSecret();

    String idLibvirtSecret();

    String idLibvirtSecretHint();

    String valueLibvirtSecret();

    String usageTypeLibvirtSecret();

    String descriptionLibvirtSecret();

    String creationDateLibvirtSecret();

    String dragToMakeChangesSetupNetwork();

    String noValidActionSetupNetwork();

    String externalNetworksInfo();

    String checkConnectivityInfoPart1();

    String checkConnectivityInfoPart2();

    String commitChangesInfoPart1();

    String commitChangesInfoPart2();

    String syncNetworkInfoPart1();

    String syncNetworkInfoPart2();

    String optionKeyVolumeParameter();

    String descriptionVolumeParameter();

    String optionValueVolumeParameter();

    String addVolumeParameter();

    String editVolumeParameter();

    String resetVolumeParameter();

    String resetAllVolumeParameter();

    String interfaces();

    String assignedLogicalNetworks();

    String unassignedLogicalNetworks();

    String networksPanel();

    String labelsPanel();

    String setupNetworksNewLabel();

    String externalLogicalNetworks();

    String statusBrick();

    String addBricksBrick();

    String removeBricksBrick();

    String removeBricksStop();

    String removeBricksCommit();

    String removeBricksStatus();

    String retainBricks();

    String replaceBrickBrick();

    String replaceHostAction();

    String replaceHostLabel();

    String advancedDetailsBrick();

    String serverBricks();

    String brickDirectoryBricks();

    String addBricksShowBricksFromHost();

    String bricksHeaderLabel();

    String addBricksButtonLabel();

    String removeBricksButtonLabel();

    String clearBricksButtonLabel();

    String removeAllBricksButtonLabel();

    String moveBricksUpButtonLabel();

    String moveBricksDownButtonLabel();

    String distributedReplicateVolumeBrickInfoLabel();

    String distributedStripeVolumeBrickInfoLabel();

    String allowBricksInRootPartition();

    String allowBricksInRootPartitionWarning();

    String removeBricksMigrateData();

    String removeBricksMigrateDataInfo();

    String removeBricksWarning();

    String generalBrickAdvancedPopupLabel();

    String brickAdvancedLabel();

    String statusBrickAdvancedLabel();

    String portBrickAdvancedLabel();

    String rdmaPortBrickAdvancedLabel();

    String pidBrickAdvancedLabel();

    String totalSizeBrickAdvancedLabel();

    String freeSizeBrickAdvancedLabel();

    String confirmedFreeSizeBrickAdvancedLabel();

    String vdoSavingsBrickAdvancedLabel();

    String deviceBrickAdvancedLabel();

    String blockSizeBrickAdvancedLabel();

    String mountOptionsBrickAdvancedLabel();

    String fileSystemBrickAdvancedLabel();

    String clientsBrickAdvancedPopupLabel();

    String clientBrickAdvancedLabel();

    String clientPortBrickAdvancedLabel();

    String bytesReadBrickAdvancedLabel();

    String bytesWrittenBrickAdvancedLabel();

    String memoryStatsBrickAdvancedPopupLabel();

    String totalAllocatedBrickAdvancedLabel();

    String freeBlocksBrickAdvancedLabel();

    String freeFastbinBlocksBrickAdvancedLabel();

    String mmappedBlocksBrickAdvancedLabel();

    String allocatedInMmappedBlocksBrickAdvancedLabel();

    String maxTotalAllocatedSpaceBrickAdvancedLabel();

    String spaceInFreedFasbinBlocksBrickAdvancedLabel();

    String totalAllocatedSpaceBrickAdvancedLabel();

    String totalFreeSpaceBrickAdvancedLabel();

    String releasableFreeSpaceBrickAdvancedLabel();

    String memoryPoolsBrickAdvancedPopupLabel();

    String nameBrickAdvancedLabel();

    String hotCountBrickAdvancedLabel();

    String coldCountBrickAdvancedLabel();

    String paddedSizeBrickAdvancedLabel();

    String allocatedCountBrickAdvancedLabel();

    String maxAllocatedBrickAdvancedLabel();

    String poolMissesBrickAdvancedLabel();

    String maxStdAllocatedBrickAdvancedLabel();

    String quotaClusterThreshold();

    String quotaClusterGrace();

    String quotaStorageThreshold();

    String quotaStorageGrace();

    String importVm_cloneAllVMs();

    String importTemplate_cloneAllTemplates();

    String importTemplate_cloneOnlyDuplicateTemplates();

    String import_newName();

    String vmInSetup();

    String templateInSetup();

    String noteClone_CollapsedSnapshotMsg();

    String storageForceCreatePopupWarningLabel();

    String quotaCluster();

    String quotaStorage();

    String extendedPanelLabel();

    String cloneSelect();

    String cloneApplyToAll();

    String cloneDontImport();

    String cloneImportVmDetails();

    String cloneImportTemplate();

    String cloneImportSuffixVm();

    String cloneImportSuffixTemplate();

    String sameTemplateNameExists();

    String quotaCalculationsMessage();

    String networkStatus();

    String detachGlusterHostsHostAddress();

    String detachGlusterHostsForcefully();

    String networkPublicUseLabel();

    String profilePublicUseLabel();

    String profilePublicUseInstanceTypeLabel();

    String profileQoSInstanceTypeLabel();

    String profileNetworkFilterLabel();

    String consolePartiallyOverridden();

    String cpuThreadsCluster();

    String memoryOptimizationCluster();

    String resiliencePolicyCluster();

    String numberOfVmsCluster();

    String emulatedMachine();

    @Override
    String highPriorityOnly();

    String compatibilityVersionCluster();

    String clusterType();

    String used();

    String total();

    String free();

    String confirmedFree();

    @Override
    String unknown();

    String providerPopupGeneralTabLabel();

    String nameProvider();

    String typeProvider();

    String descriptionProvider();

    String urlProvider();

    String importSource();

    String customExternalProvider();

    String proxyHost();

    String vCenter();

    String dataCenterInfo();

    String esxi();

    String vmwareDataCenter();

    String vmwareCluster();

    String vmwareVerifyServerSslCert();

    String ovaHost();

    String ovaPath();

    String xenUri();

    String kvmUri();

    String testProvider();

    String testSuccessMessage();

    String requiresAuthenticationProvider();

    String usernameProvider();

    String passwordProvider();

    String tenantName();

    String authHostName();

    String authPort();

    String authApiVersion();

    String authUserDomainName();

    String authProjectName();

    String authProjectDomainName();

    String pluginType();

    String autoSync();

    String addProvider();

    String editProvider();

    String removeProvider();

    String forceRemoveProvider();

    String providerGeneralSubTabLabel();

    String providerNetworksSubTabLabel();

    String providerSecretsSubTabLabel();

    String externalIdProviderNetwork();

    String dataCenterProviderNetwork();

    String nameNetworkHeader();

    String idNetworkHeader();

    String dcNetworkHeader();

    String publicNetwork();

    String publicNetworkTooltip();

    String networkProvider();

    String externalVms();

    String importedVms();

    String providerNetworks();

    String importedNetworks();

    String fetchingHostPublicKey();

    String networkQoSName();

    String networkQoSInboundAverage();

    String networkQoSInboundPeak();

    String networkQoSInboundBurst();

    String networkQoSOutboundAverage();

    String networkQoSOutboundPeak();

    String networkQoSOutboundBurst();

    String newNetworkQoS();

    String editNetworkQoS();

    String removeNetworkQoS();

    String dataCenterNetworkQoSPopup();

    String averageNetworkQoSPopup();

    String peakNetworkQoSPopup();

    String burstNetworkQoSPopup();

    String inMegabitsNetworkQoSPopup();

    String inMegabytesNetworkQoSPopup();

    String inboundLabelQoSPopup();

    String outboundLabelQoSPopup();

    String mbpsLabelQoSPopup();

    String mbLabelQoSPopup();

    String qosOverrideLabel();

    String hostNetworkQosOutLabel();

    String hostNetworkQosPopupOutAverageLinkshare();

    String hostNetworkQosPopupOutAverageUpperlimit();

    String hostNetworkQosPopupOutAverageRealtime();

    String hostNetworkQosTabOutAverageLinkshare();

    String hostNetworkQosTabOutAverageUpperlimit();

    String hostNetworkQosTabOutAverageRealtime();

    String macPoolWidgetLeftBound();

    String macPoolWidgetRightBound();

    String macPoolWidgetAllowDuplicates();

    String macPoolWidgetRangesLabel();

    String configureClusterPolicyTabLabel();

    String clusterPolicyNameLabel();

    String clusterPolicyDescriptionLabel();

    String newClusterPolicy();

    String editClusterPolicy();

    String copyClusterPolicy();

    String removeClusterPolicy();

    String clusterPolicyFilterLabel();

    String clusterPolicyFunctionLabel();

    String clusterPolicyLoadBalancerLabel();

    String clusterPolicyAttachedCluster();

    String clusterPolicyPropertiesLabel();

    String clusterPolicySelectPolicyLabel();

    String enabledFilters();

    String disabledFilters();

    String enabledFunctions();

    String disabledFunctions();

    String clusterPolicyExplanationMessage();

    String firstFilter();

    String lastFilter();

    String noPositionFilter();

    String feedbackMessage();

    String feedbackTooltip();

    String removeFilter();

    String addFilter();

    String position();

    String actionItems();

    String publicKeyUsage();

    @Override
    String networkProfilePopup();

    String nameProfilePopup();

    String descriptionProfilePopup();

    String portMirroringProfilePopup();

    String publicUseProfilePopup();

    String clusterPolicyFilterInfo();

    String clusterPolicyWeightFunctionInfo();

    String clusterPolicyLoadBalancingInfo();

    String clusterPolicyPropertiesInfo();

    String externalPolicyUnitLabel();

    String disabledPolicyUnit();

    String enabledPolicyUnit();

    String stopRebalance();

    String statusRebalance();

    String rebalanceInProgress();

    String rebalanceStopped();

    String rebalanceFailed();

    String rebalanceCompleted();

    String rebalanceStatusTime();

    String rebalanceVolumeName();

    String rebalanceClusterVolume();

    String rebalanceStatusUnknown();

    String removeBrickInProgress();

    String removeBrickStopped();

    String removeBrickFailed();

    String removeBrickCommitRequired();

    String removeBrickStatusUnknown();

    String rebalanceSessionHost();

    String rebalanceFileCount();

    String filesMigrated();

    String rebalanceSize();

    String rebalanceFailedFileCount();

    String rebalanceSkippedFileCount();

    String rebalanceScannedFileCount();

    String rebalanceRunTime();

    String rebalanceStatus();

    String rebalanceComplete();

    String rebalanceNotStarted();

    String geoRepSessionHostName();

    String geoRepSessionStatus();

    String geoRepLastSyncedAt();

    String geoRepMasterBrick();

    String geoRepSlaveHostName();

    String geoRepPairStatus();

    String geoRepSessionDetailHeader();

    String geoRepCheckPointStatus();

    String georepCrawlStatus();

    String geoRepDataOpsPending();

    String geoRepMetaOpsPending();

    String geoRepEntryOpsPending();

    String geoRepFailures();

    String geoRepCheckPointTime();

    String geoRepCheckPointCompletedAt();

    String managePolicyUnits();

    String policyUnitName();

    String policyUnitType();

    String policyUnitEnabledStatus();

    String removePolicyUnit();

    String internalPolicyUnit();

    String externalPolicyUnit();

    String optimizeForUtilizationLabel();

    String optimizeForSpeedLabel();

    String schedulerOptimizationPanelLabel();

    String allowOverbookingLabel();

    String guarantyResourcesLabel();

    String consoleTabLabel();

    String fencingPolicyTabLabel();

    String statusAffinityGroup();

    String nameAffinityGroup();

    String descriptionAffinityGroup();

    String priorityAffinityGroup();

    String polarityAffinityGroup();

    String hostPolarityAffinityGroup();

    String enforceAffinityGroup();

    String hostEnforceAffinityGroup();

    String hostLabelsAffinityGroup();

    String hostMembersAffinityGroup();

    String vmLabelsAffinityGroup();

    String vmMembersAffinityGroup();

    String noLabelsAffinityGroup();

    String noMembersAffinityGroup();

    String newAffinityGroupLabel();

    String editAffinityGroupLabel();

    String removeAffinityGroupLabel();

    String affinityGroupStatusOk();

    String affinityGroupStatusBroken();

    String affinityGroupNameLabel();

    String affinityGroupPriorityLabel();

    String affinityGroupPriorityInfo();

    String affinityGroupPolarityLabel();

    String affinityGroupEnforceInfo();

    String affinityGroupEnforceTypeLabel();

    String affinityGroupHostPolarityInfo();

    String affinityGroupVmPolarityInfo();

    String affinityDescriptionLabel();

    String positiveAffinity();

    String negativeAffinity();

    String vmAffinityRuleLabel();

    String hostAffinityRuleLabel();

    String vmsAffinitySelectionLabel();

    String hostsAffinitySelectionLabel();

    String hardEnforcingAffinity();

    String softEnforcingAffinity();

    String addIscsiBond();

    String editIscsiBond();

    String removeIscsiBond();

    String dataCenterIscsiMultipathingSubTabLabel();

    String changeDisplayNetworkWarning();

    String instanceTypes();

    String instanceTypeName();

    String newInstanceType();

    String editInstanceType();

    String removeInstanceType();

    String snapshotsLabel();

    String attachedByLabel();

    String fileOperation();

    String fOpInvocationCount();

    String fOpLatency();

    String fOpMaxLatency();

    String fOpMinLatency();

    String fOpAvgLatency();

    String selectBrickToViewFopStats();

    String selectServerToViewFopStats();

    String volumeProfileBricksTab();

    String volumeProfileNfsTab();

    String startVolumeProfiling();

    String volumeProfileDetails();

    String stopVolumeProfiling();

    String volumeProfilingAction();

    String nfsProfileErrorMessage();

    String brickProfileErrorMessage();

    String exportToText();

    String fencingEnabledInfo();

    String skipFencingIfSDActiveInfo();

    String skipFencingWhenConnectivityBrokenInfo();

    String skipFencingIfGlusterBricksUp();

    String skipFencingIfGlusterBricksUpInfo();

    String skipFencingIfGlusterQuorumNotMet();

    String skipFencingIfGlusterQuorumNotMetInfo();

    String newVolumeOptimiseForVirtStoreWarning();

    String storageQosThroughputTotal();

    String storageQosThroughputRead();

    String storageQosThroughputWrite();

    String storageQosIopsTotal();

    String storageQosIopsRead();

    String storageQosIopsWrite();

    String newQos();

    String editQos();

    String removeQos();

    String newCpuQos();

    String editCpuQos();

    String removeCpuQos();

    String dataCenterQosPopup();

    String qosDescription();

    String throughputLabelQosPopup();

    String iopsLabelQosPopup();

    String readStorageQosPopup();

    String noneStorageQosPopup();

    String readWriteStorageQosPopup();

    String totalStorageQosPopup();

    String writeStorageQosPopup();

    String mbpsLabelStorageQosPopup();

    String iopsCountLabelQosPopup();

    String diskProfileQosLabel();

    String diskProfilesSubTabLabel();

    String diskProfileStorageDomainLabel();

    String cpuQosCpuLimit();

    String cpuQosDescription();

    String cpuProfilesSubTabLabel();

    String cpuProfileClusterLabel();

    String cpuProfileQosLabel();

    String numaSupport();

    String newVolumeSnapshot();

    String editVolumeSnapshotSchedule();

    String scheduleLabel();

    String recurrenceLabel();

    String intervalLabel();

    String endByLabel();

    String endByDateLabel();

    String timeZoneLabel();

    String daysOfMonthLabel();

    String daysOfWeekLabel();

    String startAtLabel();

    String executionTimeLabel();

    String noEndDateOption();

    String endDateOption();

    String volumeSnapshotNamePrefixLabel();

    String snapshotNameInfo();

    String volumeSnapshotDescriptionLabel();

    String volumeSnapshotName();

    String volumeSnapshotDescription();

    String volumeSnapshotCreationTime();

    String noOfSnapshotsLabel();

    String volumeClusterLabel();

    String volumeNameLabel();

    String volumeSnapshotMainViewTitle();

    String configureClusterSnapshotOptions();

    String configureVolumeSnapshotOptions();

    String volumeSnapshotConfigName();

    String clusterSnapshotConfigValue();

    String volumeSnapshotConfigValue();

    String snapshotConfigHeaderLabel();

    String mountPoint();

    String deviceIsAlreadyUsed();

    String syncStorageDevices();

    String terminateSession();

    String restoreVolumeSnapshot();

    String deleteVolumeSnapshot();

    String deleteAllVolumeSnapshots();

    String activateVolumeSnapshot();

    String deactivateVolumeSnapshot();

    String resetGeoRepSessionConfig();

    String criticalSnapshotIntervalNote();

    String logicalVolume();

    String lvSize();

    String createBrick();

    String extendBrick();

    String raidParameters();

    String raidType();

    String noOfPhysicalDisksInRaidVolume();

    String stripeSize();

    String raidConfigurationWarning();

    String geoRepSessionCreateAndStart();

    String geoRepMasterVolume();

    String geoRepSlaveHostIp();

    String geoRepSlaveUserName();

    String geoRepUserSessionCreate();

    String geoRepShowEligibleVolumes();

    String geoRepSessionUserName();

    String slaveUserGroupName();

    String geoRepSlaveNodePassword();

    String geoRepSessionSlaveCluster();

    String geoRepSlaveVolume();

    String geoReplicationMainViewTitle();

    String selectGeoRepSlaveVolumeButtonLabel();

    String fetchingDataMessage();

    String geoReplicationRecommendedConfigViolation();

    String glusterVolumeSnapshotsScheduledToolTip();

    String numOfVfsSetting();

    String numOfVfs();

    String allowedNetworks();

    String selectNetworks();

    String vfsConfigNetworkName();

    String vfsConfigViaLabel();

    String iommuGroup();

    String capability();

    String product();

    String driver();

    String productName();

    String productId();

    String vendor();

    String vendorName();

    String vendorId();

    String availableHostDevices();

    String selectedHostDevices();

    String pinnedHost();

    String currentlyUsedByVm();

    String attachedToVms();

    String addtionalClusterFeaturesTitle();

    String virtualMachineVmDevicesSubTabLabel();

    String glusterCliSchedulingEnabled();

    String enabled();

    String disabled();

    String hostDevicePassthroughHostGeneral();

    String critical();

    String important();

    String moderate();

    String bug();

    String bugs();

    String enhancement();

    String enhancements();

    String security();

    String errataType();

    String errataSeverity();

    String errataDateIssued();

    String errataId();

    String errataTitle();

    String solution();

    String summary();

    String errataPackages();

    String totalSecurity();

    String totalBugFix();

    String totalEnhancement();

    String katelloProblemRetrievingErrata();

    String clusterEditHostTitle();

    String clusterEditVmtTitle();

    String areYouSureYouWantToContinue();

    String physicalFunction();

    String ipv4TabLabel();

    String ipv6TabLabel();

    String qosTabLabel();

    String customPropertiesTabLabel();

    String dnsConfigurationTabLabel();

    String shouldSetDnsConfigurationLabel();

    String nameServerAddressLabel();

    String bondInMode4HasNoPartnerMac();

    String bondInMode4HasInvalidAggregatorId();

    String GlusterSelfHealOk();

    String bondProperties();

    String kvmBlockDomainWraning();

    String hostedEngineVmTooltip();

    String hostedEngineStorageTooltip();

    String backupStorageTooltip();

    String isUnmanaged();

    String haActiveTooltip();

    String haActiveZeroHaScoreTooltip();

    String isHostedEngineVmTooltip();

    String isRunningInContainer();

    String isRunninVmsInContainer();

    String providedByContainerPlatform();

    String integratedWithContainerPlatform();

    String supportsContainerPlatform();

    String arbiterVolume();

    String externalLogicalNetwork();

    String externalVnicProfile();

    String externalVnicProfilesMapping();

    String reassignAllBadMacs();

    String targetVnicProfile();

    String reassignBadMacs();

    String arbiter();

    String storageDRHoursLabel();

    String storageDRMinuteLabel();

    String storageDRGeoRepSessionLabel();

    String unsetInitiallySetNetworkRoleIsNotAllowed();

    String migrationBandwidthLimit();

    String clusterPopupResiliencePolicyInfo();

    String administration();

    String backup();

    String activeTags();

    String deactivateTag();

    String activateTag();

    String notificationDrawer();

    String eventsAndAlerts();

    String help();

    String storageDomainsMenuLabel();

    String tasksOverlayNoTasksFound();

    String networkInSync();

    String logicalNetwork();

    String slave();

    String hostHasDisabledPowerManagment();

    String hostNetConfigurationDirty();

    String hostGlusterIssues();

    String hostReinstallRequired();

    String hostHasNoDefaultRoute();

    String hostSmtAlert();

    String autoDetect();

    String cacheDevice();

    String cacheDevicePath();

    String cacheMode();

    String cacheSize();

    String resetBrickBrick();

    String showVfInfo();

    String ovsVersionGeneral();

    String nmstateVersionGeneral();

    String enabledVirtualFunctions();

    String freeVirtualFunctions();

    String notifDismissAll();

    String notifDoNotDisturb();

    String notifDoNotDisturb10Minutes();

    String notifDoNotDisturb1Hour();

    String notifDoNotDisturb1Day();

    String notifDoNotDisturbNextLogin();

    String ipHostImportCluster();

    String glusterPeerAddress();

    String vncEncryptionEnabled();

    String vncEncryptionEnabledHelpMessage();

    String vncEncryptionLabel();

    String fipsEnabledLabel();

    String ovnConfiguredLabel();

    String vdsmName();

    String portSecurityEnabledLabel();

    String networkNameInfo();

    String bondLengthNameWarning();

    String physicalNetworkCustomInfo();

    String tscFrequency();

    String tscScalingOn();

    String tscScalingOff();

    String clusterHasHostWithMissingCpuFlagsWarning();

    String clusterCpuConfigurationOutdatedWarning();

    String clusterHasUpgradableHosts();

    String clusterUpgradeInProgress();

    String vmCpuTypeDoesNotMatchClusterCpuType();

    String supportedCpusInfo();

    String noSupportedCpusInfo();

    String clusterSerialNumberPolicyPanelLabel();

    String clusterSerialNumberPolicy();

    String clusterCustomSerialNumber();

    String kubevirtToken();

    String kubevirtTokenHelpMessage();

    String kubevirtCertificateAuthority();

    String kubevirtCertificateAuthorityHelpMessage();

    String prometheusUrl();

    String prometheusUrlHelpMessage();

    String prometheusCertificateAuthority();

    String prometheusCertificateAuthorityHelpMessage();

    String ipv6AutoconfAvailabilityInfo();

    String cleanupFinishedTasks();
}
