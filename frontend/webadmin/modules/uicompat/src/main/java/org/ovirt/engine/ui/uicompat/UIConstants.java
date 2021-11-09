package org.ovirt.engine.ui.uicompat;

import com.google.gwt.i18n.client.Constants;

public interface UIConstants extends Constants {
    String ok();

    String cancel();

    String yes();

    String no();

    String close();

    String notAvailableLabel();

    @DefaultStringValue("") // Use annotation and not a properties key to leave it out of translations
    String emptyString();

    String bracketedNotAvailableLabel();

    String notSpecifiedLabel();

    String action();

    String showAdvancedOptions();

    String hideAdvancedOptions();

    @DefaultStringValue(" ") // Use annotation and not a properties key to leave it out of translations
    String space();

    String errorTitle();

    String configurePowerManagement();

    String createNewBondTitle();

    String joinBondsTitle();

    String addNewLabelTitle();

    String assignDetachNetworksTitle();

    String assignDetachNetworkTitle();

    String clustersTitle();

    String networkProfilesTitle();

    String newClusterGuideMeTitle();

    String editClusterTitle();

    String removeClusterTitle();

    String resetClusterEmulatedMachineTitle();

    String resetClusterEmulatedMachineMessage();

    String changeClusterCompatibilityVersionTitle();

    String changeDCQuotaEnforcementModeTitle();

    String setUnlimitedSpecificQuotaModeTitle();

    String changeCpuLevel();

    String changeCpuLevelWhileRunningMessage();

    String changeCpuLevelCustomVmCpusMessage();

    String changeCpuLevelWarningMessage();

    String terminateSessionConfirmation();

    String generalTitle();

    String servicesTitle();

    String glusterHooksTitle();

    String glusterSwiftTitle();

    String storageDRTitle();

    String newDRSetup();

    String clusterPolicyTitle();

    String editPolicyTitle();

    String copytoClipboardTitle();

    String newVmTitle();

    String editVmTitle();

    String cloneVmTitle();

    String cloneVmLunsWontBeCloned();

    String rootTag();

    String removeBookmarksTitle();

    String editBookmarkTitle();

    String newBookmarkTitle();

    String addLocalStorageTitle();

    String newLocalDomainTitle();

    String newClusterTitle();

    String selectHostTitle();

    String newHostTitle();

    String powerManagementConfigurationTitle();

    String editNextRunConfigurationTitle();

    String confirmTpmDataRemovalTitle();

    String confirmNvramDataRemovalTitle();

    String confirmTpmDataRemovalMessage();

    String confirmNvramDataRemovalMessage();

    String configurationChangesForHighPerformanceVmTitle();

    String configurationChangesForHighPerformancePoolTitle();

    String highPerformancePopupRecommendationMsgForHugePages();

    String highPerformancePopupRecommendationMsgForNumaSetAndPinned();

    String highPerformancePopupRecommendationMsgForNumaPinned();

    String highPerformancePopupRecommendationMsgForCpuPin();

    String highPerformancePopupRecommendationMsgForCpuSpecificHostPin();

    String vmUnsupportedCpuTitle();

    String vmUnsupportedCpuMessage();

    String vmCpuPinningClearMessage();

    String vmCpuPinningClearTitle();

    String dataCentersTitle();

    String newDataCenterGuideMeTitle();

    String configureLaterTitle();

    String newDataCenterTitle();

    String editDataCenterTitle();

    String removeDataCenterTitle();

    String addMacPoolButton();

    String forceRemoveDataCenterTitle();

    String dataCenterReInitializeTitle();

    String changeDataCenterCompatibilityVersionTitle();

    String newSharedMacPoolTitle();

    String editSharedMacPoolTitle();

    String removeSharedMacPoolsTitle();

    String logicalNetworksTitle();

    String removeLogicalNetworkTitle();

    String editLogicalNetworkTitle();

    String newLogicalNetworkTitle();

    String newNetworkQosButton();

    String updateManagementNetworkWarning();

    String attachDetachNetworkToFromClustersTitle();

    String saveNetworkConfigurationTitle();

    String storageTitle();

    String noStoragesToImport();

    String detachStorageTitle();

    String eventNotifierTitle();

    String addEventNotificationTitle();

    String usersTitle();

    String assignTagsTitle();

    String addUsersAndGroupsTitle();

    String add();

    String addAndClose();

    String removeUsersTitle();

    String directoryGroupsTitle();

    String quotaTitle();

    String networkQoSTitle();

    String storageQosTitle();

    String cpuQosTitle();

    String hostNetworkQosTitle();

    String hostNoExternalNetworkProvider();

    String volumesTitle();

    String newVolumeTitle();

    String addBricksTitle();

    String parameterTitle();

    String bricksTitle();

    String geoReplicationTitle();

    String geoReplicationSessionDetailsTitle();

    String geoRepBrickDetailsFetchFailed();

    String geoRepSessionStatusDetailFetchFailed();

    String geoReplicationStartTitle();

    String geoReplicationStopTitle();

    String geoReplicationPauseTitle();

    String geoReplicationResumeTitle();

    String geoReplicationRemoveTitle();

    String startGeoRepProgressText();

    String stopGeoRepProgressText();

    String pauseGeoRepProgressText();

    String resumeGeoRepProgressText();

    String removeGeoRepProgressText();

    String startGeoRep();

    String stopGeoRep();

    String pauseGeoRep();

    String resumeGeoRep();

    String removeGeoRep();

    String geoReplicationOptions();

    String setGeorepConfig();

    String resetAllConfigsTitle();

    String volumeRebalanceStatusTitle();

    String virtualMachinesTitle();

    String addDesktopsToUserADGroupTitle();

    String detachVirtualMachinesTitle();

    String detachAllVmsWarning();

    String permissionsTitle();

    String disksTitle();

    String removePermissionTitle();

    String removeTemplateDisksTitle();

    String copyDisksTitle();

    String networkInterfacesTitle();

    String newNetworkInterfaceTitle();

    String editNetworkInterfaceTitle();

    String removeNetworkInterfacesTitle();

    String removeVnicProfileTitle();

    String removeExternalSubnetTitle();

    String copyTemplateTitle();

    String backupTemplateTitle();

    String editTemplateTitle();

    String removeTemplatesTitle();

    String removeUnregisteredTemplatesTitle();

    String guestAgentNotResponsiveTitle();

    String dataCenterTitle();

    String attachToDataCenterTitle();

    String imagesTitle();

    String templateImportTitle();

    String disksImportTitle();

    String invalidImportTitle();

    String removeBackedUpTemplatesTitle();

    String removeBackedUpTemplatesWithDependentsVMTitle();

    String theFollowingTemplatesHaveDependentVmsBackupOnExportDomainMsg();

    String templatesTitle();

    String newDomainTitle();

    String editDomainTitle();

    String importPreConfiguredDomainTitle();

    String removeStoragesTitle();

    String destroyStorageDomainTitle();

    String vmImportTitle();

    String removeBackedUpVMsTitle();

    String importVirtualMachinesTitle();

    String configureTitle();

    String eventsTitle();

    String newQuotaTitle();

    String editQuotaTitle();

    String cloneQuotaTitle();

    String removeQuotasTitle();

    String assignUsersAndGroupsToQuotaTitle();

    String removeQuotaAssignmentFromUsersTitle();

    String defineClusterQuotaOnDataCenterTitle();

    String defineStorageQuotaOnDataCenterTitle();

    String hostHooksTitle();

    String hostBricksTitle();

    String interfaceListTitle();

    String confirmTitle();

    String bondNetworkInterfacesTitle();

    String detachNetworkInterfacesTitle();

    String editManagementNetworkInterfaceTitle();

    String newLabelPanelText();

    String newLabel();

    String installHostTitle();

    String upgradeHostTitle();

    String areYouSureTitle();

    String removeConfirmationPopupMessage();

    String editHostTitle();

    String removeHostsTitle();

    String maintenanceHostsTitle();

    String editAndApproveHostTitle();

    String restartHostsTitle();

    String addMultipleHostsTitle();

    String stopHostsTitle();

    String configureLocalStorageTitle();

    String systemTitle();

    String hostsTitle();

    String networksTitle();

    String providersTitle();

    String vnicProfilesTitle();

    String newExternalSubnetTitle();

    String vmsTitle();

    String hardDiskTitle();

    String cdromTitle();

    String networkPXETitle();

    String thinTitle();

    String cloneTitle();

    String lowTitle();

    String mediumTitle();

    String highTitle();

    String neverTitle();

    String normalTitle();

    String noneTitle();

    String removeVirtualMachineTitle();

    String removeUnregisteredVirtualMachineTitle();

    String removeTagsTitle();

    String editTagTitle();

    String newTagTitle();

    String rolesTitle();

    String removeRolesTitle();

    String resetTitle();

    String rolesPermissionsTitle();

    String systemPermissionTitle();

    String addSystemPermissionToUserTitle();

    String removeSystemPermissionsTitle();

    String addPermissionToUserTitle();

    String automaticTitle();

    String manualTitle();

    String poolsTitle();

    String newPoolTitle();

    String editPoolTitle();

    String removePoolsTitle();

    String attachVirtualDiskTitle();

    String newVirtualDiskTitle();

    String editVirtualDiskTitle();

    String removeDisksTitle();

    String removeUnregisteredDisksTitle();

    String sparsifyDisksTitle();

    String sparsifyConfirmationPopupMessage();

    String moveDisksTitle();

    String importImagesTitle();

    String exportImagesTitle();

    String uploadImageTitle();

    String uploadImageResumeTitle();

    String uploadImageCancelTitle();

    String uploadImageCancelConfirmationMessage();

    String uploadImageLeaveWindowPopupWarning();

    String uploadImageBackingFileUnsupported();

    String uploadImageCannotBeOpened();

    String snapshotsTitle();

    String deleteSnapshotTitle();

    String createSnapshotTitle();

    String previewSnapshotTitle();

    String customPreviewSnapshotTitle();

    String previewPartialSnapshotTitle();

    String commitSnapshotTitle();

    String preserveActiveDisks();

    String excludeActiveDisks();

    String openCustomPreviewDialog();

    String applicationsTitle();

    String containersTitle();

    String monitorTitle();

    String guestInformationTitle();

    String activeUserSessionsTitle();

    String userSessionsTitle();

    String rdpTitle();

    String rdpIsNotSupportedInYourBrowser();

    String retrievingCDsTitle();

    String newVirtualMachineGuideMeTitle();

    String removeVirtualMachinesTitle();

    String moveVirtualMachineTitle();

    String exportVirtualMachineTitle();

    String exportVirtualMachineAsOvaTitle();

    String exportTemplateTitle();

    String exportTemplateAsOvaTitle();

    String templatesNotFoundOnExportDomainTitle();

    String baseTemplatesNotFoundOnExportDomainTitle();

    String runVirtualMachinesTitle();

    String vncTitle();

    String spiceTitle();

    String newTemplateTitle();

    String shutdownVirtualMachinesTitle();

    String stopVirtualMachinesTitle();

    String rebootVirtualMachinesTitle();

    String resetVirtualMachinesTitle();

    String suspendVirtualMachinesTitle();

    String changeCDTitle();

    String consoleDisconnectedTitle();

    String importTemplatesTitle();

    String importDisksTitle();

    String cloneVmFromSnapshotTitle();

    String youAreAboutChangeClusterCompatibilityVersionMsg();

    String youAreAboutChangeClusterCompatibilityVersionNonResponsiveHostsMsg();

    String youAreAboutChangeDCQuotaEnforcementMsg();

    String youAreAboutToCreateUnlimitedSpecificQuotaMsg();

    String asciiNameValidationMsg();

    String asciiNameAndDomainValidationMsg();

    String noSpecialCharactersWithDotMsg();

    String vmInitNetworkNameValidationMsg();

    String asciiOrNoneValidationMsg();

    String specialAsciiI18NOrNoneValidationMsg();

    String i18NNameValidationMsg();

    String poolNameValidationMsg();

    String i18NExtraNameOrNoneValidation();

    String cpuNameValidation();

    String nonUtfValidationMsg();

    String youHavntConfigPmMsg();

    String editFenceAgent();

    String selectFenceProxy();

    String concurrentFenceAgent();

    String duplicateFenceAgentManagementIp();

    String nameMustContainAlphanumericMaxLenMsg();

    String networkNameStartMsg();

    String thereAreNoCompatibleStorageDomainsAttachThisDcMsg();

    String youAreAboutChangeDcCompatibilityVersionMsg();

    String youAreAboutToAttachDetachNetworkToFromTheClustersMsg();

    String areYouSureYouWantToMakeTheChangesPersistentMsg();

    String areYouSureYouWantDetachFollowingStoragesMsg();

    String areYouSureYouWantDetachFromUserFollowingVmsMsg();

    String disksAlreadyExistMsg();

    String templatesResideOnSeveralDcsMakeSureExportedTemplatesResideOnSameDcMsg();

    String thereIsNoExportDomainToExportTheTemplateIntoMsg();

    String thereAreNoDataCenterStorageDomainAttachedMsg();

    String areYouSureYouWantDetachStorageFromDcsMsg();

    String thereIsNoDataStorageDomainToImportTemplateIntoMsg();

    String invalidImportMsg();

    String theExportDomainIsInactiveMsg();

    String exportDomainIsNotAttachedToAnyDcMsg();

    String importProviderCertificateTitle();

    String thereAreNoNetworksAvailablePleaseAddAdditionalNetworksMsg();

    String thereAreNoISOversionsVompatibleWithHostCurrentVerMsg();

    String areYouSureYouWantToPlaceFollowingHostsIntoMaintenanceModeMsg();

    String areYouSureYouWantToPlaceFollowingHostsIntoMaintenanceModeDueToPinnedVmsMsg();

    String areYouSureYouWantToRestartTheFollowingHostsMsg();

    String areYouSureYouWantToStopTheFollowingHostsMsg();

    String hostDoesntSupportLocalStorageConfigurationMsg();

    String hostMustBeInstalledBeforeUpgrade();

    String areYouSureYouWantToCheckTheFollowingHostForUpgradesMsg();

    String areYouSureYouWantToUpgradeTheFollowingHostWithRunningVmsMsg();

    String areYouSureYouWantToUpgradeTheFollowingHostMsg();

    String testingInProgressItWillTakeFewSecondsPleaseWaitMsg();

    String nameMustBeUpToAndStartWithMsg();

    String couldNotConnectToOvirtEngineServiceMsg();

    String areYouSurYouWantToDetachSelectedVirtualMachinesMsg();

    String snapshotCannotBeCreatedLockedSnapshotMsg();

    String snapshotCannotBeCreatedPreviewSnapshotMsg();

    String snapshotCannotBeCreatedStatelessSnapshotMsg();

    String liveSnapshotWithNoGuestAgentMsg();

    String atLeastOneDiskMustBeMarkedMsg();

    String vmsResideOnSeveralDCsMakeSureTheExportedVMResideOnSameDcMsg();

    String thereIsNoExportDomainBackupVmAttachExportDomainToVmsDcMsg();

    String theRelevantExportDomainIsNotActivePleaseActivateItMsg();

    String areYouSureYouWantToShutDownTheFollowingVirtualMachinesMsg();

    String areYouSureYouWantToStopTheFollowingVirtualMachinesMsg();

    String areYouSureYouWantToRebootTheFollowingVirtualMachinesMsg();

    String areYouSureYouWantToResetTheFollowingVirtualMachinesMsg();

    String areYouSureYouWantToSuspendTheFollowingVirtualMachinesMsg();

    String thisFieldMustContainIpv4AddressInFormatMsg();

    String thisFieldMustContainIpv6AddressMsg();

    String thisFieldMustContainIpv4OrIpv6AddressMsg();

    String emptyOrValidIpv4AddressInFormatMsg();

    String emptyOrValidIpv6AddressInFormatMsg();

    String emptyOrValidIpAddressInFormatMsg();

    String thisFieldMustContainValidPrefixOrNetmask();

    String thisFieldMustContainValidNetmask();

    String inValidNetmask();

    String thisFieldMustContainCidrInFormatMsg();

    String cidrNotNetworkAddress();

    String thisFieldCantConatainSpacesMsg();

    String invalidUnicastMacAddressMsg();

    String invalidMacAddressMsg();

    String invalidMacRangeRightBound();

    String tooBigMacRange();

    String tpmDeviceRequired();

    String labelAlreadyExists();

    String noteLocalStorageAlreadyConfiguredForThisHostMsg();

    String withLocalStorageDomainMsg();

    String localfsMountPashIsIllegalMsg();

    String nfsMountPashIsIllegalMsg();

    String noNewDevicesWereFoundMsg();

    String noStorageDomainAvailableMsg();

    String noExternalImageProviderHasBeenConfiguredMsg();

    String sourceStorageDomainIsNotActiveMsg();

    String noActiveTargetStorageDomainAvailableMsg();

    String noActiveSourceStorageDomainAvailableMsg();

    String diskExistsOnAllActiveStorageDomainsMsg();

    String noActiveStorageDomainWithTemplateMsg();

    String fieldValueShouldFollowMsg();

    String importSparseDiskToBlockDeviceMustCollapseSnapshots();

    String importVMWithTemplateNotInSystemMustCollapseSnapshots();

    String importVMThatExistsInSystemMustClone();

    String importTemplateThatExistsInSystemMustClone();

    String importTemplateWithoutBaseMustClone();

    String importCloneVMMustCollapseSnapshots();

    String osVersionAbout();

    String vdsmVersionAbout();

    String oVirtEnterpriseVirtualizationEngineHypervisorHostsAbout();

    String noHostsAbout();

    String oVirtEngineForServersAndDesktopsAbout();

    String theFieldContainsSpecialCharactersInvalidReason();

    String configureHostClusterGuide();

    String guidePopupNoActionsLabel();

    String addDataCenter();

    String addAnotherHostClusterGuide();

    String selectHostsClusterGuide();

    String theClusterIsntAttachedToADcClusterGuide();

    String thisClusterBelongsToALocalDcWhichAlreadyContainHostClusterGuide();

    String attachLocalStorageDomainToFullyConfigure();

    String nameMustBeUniqueInvalidReason();

    String youMustApproveTheActionByClickingOnThisCheckboxInvalidReason();

    String dataCenterConfigureClustersAction();

    String dataCenterAddAnotherClusterAction();

    String dataCenterConfigureHostsAction();

    String dataCenterAddAnotherHostAction();

    String dataCenterSelectHostsAction();

    String dataCenterConfigureStorageAction();

    String dataCenterAddMoreStorageAction();

    String dataCenterAttachStorageAction();

    String dataCenterAttachMoreStorageAction();

    String dataCenterConfigureISOLibraryAction();

    String dataCenterAttachISOLibraryAction();

    String noUpHostReason();

    String noDataDomainAttachedReason();

    String localDataCenterAlreadyContainsAHostDcGuide();

    String dataCenterWasAlreadyInitializedDcGuide();

    String newISOLibraryTitle();

    String newStorageTitle();

    String attachISOLibraryTitle();

    String attachStorageTitle();

    String attachExportDomainTitle();

    String invalidEmailAddressInvalidReason();

    String addressIsNotValidHostNameOrIpAddressInvalidReason();

    String portHostnameOrIpPort();

    String uriInvalidFormat();

    String switchToMaintenanceModeToEnableUpgradeReason();

    String configuringLocalStorageHost();

    String configuringLocalStoragePermittedOnlyAdministratorsWithSystemLevelPermissionsReason();

    String testFailedUnknownErrorMsg();

    String powerOn();

    String powerOff();

    String errorWhileRetrievingListOfDomains();

    String pleaseSelectStorageDomainToImportImportSanStorage();

    String errorWhileRetrievingListOfDomainsImportSanStorage();

    String dataCenterIsNotAccessibleMsg();

    String thisFieldCantBeEmptyInvalidReason();

    String emptyFieldsInvalidReason();

    String leadingOrTrailingSpacesInField();

    String quotaMustBeSelectedInvalidReason();

    String newRoleTitle();

    String editRoleTitle();

    String copyRoleTitle();

    String thisFieldMustContainIntegerNumberInvalidReason();

    String thisFieldMustContainNumberInvalidReason();

    String thisFieldMustContainNonNegativeIntegerNumberInvalidReason();

    String bondNameInvalid();

    String storageRemovePopupFormatLabel();

    String allowToAddRemoveUsersFromTheSystemRoleTreeTooltip();

    String allowToAddRemovePermissionsForUsersOnObjectsInTheSystemRoleTreeTooltip();

    String allowToAddUsersAndGroupsFromDirectoryOnObjectsInTheSystemRoleTreeTooltip();

    String allowToLoginToTheSystemRoleTreeTooltip();

    String allowToManageTags();

    String allowToManageBookmarks();

    String allowToManageEventNotifications();

    String allowToManageAuditLogs();

    String allowToDefineConfigureRolesInTheSystemRoleTreeTooltip();

    String allowToGetOrSetSystemConfigurationRoleTreeTooltip();

    String allowToCreateDataCenterRoleTreeTooltip();

    String allowToRemoveDataCenterRoleTreeTooltip();

    String allowToModifyDataCenterPropertiesRoleTreeTooltip();

    String allowToEditLogicalNetworkRoleTreeTooltip();

    String allowToCreateLogicalNetworkPerDataCenterRoleTreeTooltip();

    String allowToDeleteLogicalNetworkRoleTreeTooltip();

    String allowToCreateVnicProfileRoleTreeTooltip();

    String allowToEditVnicProfileRoleTreeTooltip();

    String allowToDeleteVnicProfileRoleTreeTooltip();

    String allowToCreateStorageDomainRoleTreeTooltip();

    String allowToDeleteStorageDomainRoleTreeTooltip();

    String allowToModifyStorageDomainPropertiesRoleTreeTooltip();

    String allowToChangeStorageDomainStatusRoleTreeTooltip();

    String allowToCreateNewClusterRoleTreeTooltip();

    String allowToRemoveClusterRoleTreeTooltip();

    String allowToEditClusterPropertiesRoleTreeTooltip();

    String allowToAddRemoveLogicalNetworksForTheClusterRoleTreeTooltip();

    String allowToEditLogicalNetworksForTheClusterRoleTreeTooltip();

    String allowToManipulateAffinityGroupsForClusterRoleTreeTooltip();

    String allowToAddNewHostToTheClusterRoleTreeTooltip();

    String allowToRemoveExistingHostFromTheClusterRoleTreeTooltip();

    String allowToEditHostPropertiesRoleTreeTooltip();

    String allowToChangeHostStatusRoleTreeTooltip();

    String allowToConfigureHostsNetworkPhysicalInterfacesRoleTreeTooltip();

    String allowToChangeTemplatePropertiesRoleTreeTooltip();

    String allowToConfigureTemlateNetworkRoleTreeTooltip();

    String notePermissionsContainigTheseOperationsShuoldAssociatSdOrAboveRoleTreeTooltip();

    String allowToCreateNewTemplateRoleTreeTooltip();

    String allowToRemoveExistingTemplateRoleTreeTooltip();

    String allowImportExportOperationsRoleTreeTooltip();

    String allowToCopyTemplateBetweenStorageDomainsRoleTreeTooltip();

    String allowBasicVmOperationsRoleTreeTooltip();

    String allowToAttachCdToTheVmRoleTreeTooltip();

    String allowViewingTheVmConsoleScreenRoleTreeTooltip();

    String allowConnectingToVmSerialConsoleRoleTreeTooltip();

    String allowVmNetworkPortMirroringRoleTreeTooltip();

    String allowChangeVmPropertiesRoleTreeTooltip();

    String allowToChangeVmCustomPropertiesRoleTreeTooltip();

    String allowChangingVmAdminPropertiesRoleTreeTooltip();

    String allowChangingTemplateAdminPropertiesRoleTreeTooltip();

    String allowReconnectToVmRoleTreeTooltip();

    String allowToCreateNewVmsRoleTreeTooltip();

    String allowToCreateNewInstnaceRoleTreeTooltip();

    String allowToRemoveVmsFromTheSystemRoleTreeTooltip();

    String allowToConfigureVMsNetworkRoleTreeTooltip();

    String allowToAddRemoveDiskToTheVmRoleTreeTooltip();

    String allowToCreateDeleteSnapshotsOfTheVmRoleTreeTooltip();

    String notePermissionsContainigTheseOperationsShuoldAssociatDcOrEqualRoleTreeTooltip();

    String notePermissionsContainingTheseOperationsShouldAssociateNetworkOrEqualRoleTreeTooltip();

    String allowToMoveVmImageToAnotherStorageDomainRoleTreeTooltip();

    String allowMigratingVmBetweenHostsInClusterRoleTreeTooltip();

    String allowToRunPauseStopVmFromVmPoolRoleTreeTooltip();

    String allowToCreateVmPoolRoleTreeTooltip();

    String allowToDeleteVmPoolRoleTreeTooltip();

    String allowToChangePropertiesOfTheVmPoolRoleTreeTooltip();

    String cpuProfileRoleTree();

    String diskRoleTree();

    String allowToCreateCpuRoleTreeTooltip();

    String allowToDeleteCpuRoleTreeTooltip();

    String allowToUpdateCpuProfileRoleTreeTooltip();

    String allowToAssignCpuRoleTreeToolTip();

    String notePermissionsContainingCpuProfileProvisioningOperationsRoleTreeTooltip();

    String notePermissionsContainingCpuProfileAdministrationOperationsRoleTreeTooltip();

    String notePermissionsContainingDiskOperationsRoleTreeTooltip();

    String allowToCreateDiskRoleTreeTooltip();

    String allowToDeleteDiskRoleTreeTooltip();

    String allowToMoveDiskToAnotherStorageDomainRoleTreeTooltip();

    String allowToLiveMigrateDiskToAnotherStorageDomainRoleTreeTooltip();

    String allowToBackupDiskRoleTreeTooltip();

    String allowToAttachDiskToVmRoleTreeTooltip();

    String allowToSparsifyDiskToVmRoleTreeTooltip();

    String allowToChangePropertiesOfTheDiskRoleTreeTooltip();

    String allowToChangeSGIORoleTreeTooltip();

    String allowAccessImageDomainRoleTreeTooltip();

    String allowToAttachDiskProfile();

    String noAlerts();

    String maxMemoryHasToBeLargerThanMemorySize();

    String storageDomainMustBeSpecifiedInvalidReason();

    String atLeastOneDnsServerHasToBeConfigured();

    String theFieldMustContainTimeValueInvalidReason();

    String noteThisActionWillRemoveTemplatePermanentlyFromStorageDomains();

    String blankTemplateCannotBeEdited();

    String blankTemplateCannotBeRemoved();

    String blankTemplateCannotBeExported();

    String blankTemplateCannotBeCopied();

    String noteTheDeletedItemsMightStillAppearOntheSubTab();

    String noteRemovingTheTagWillAlsoRemoveAllItsDescendants();

    String failedToRetrieveExistingStorageDomainInformationMsg();

    String thereIsNoStorageDomainUnderTheSpecifiedPathMsg();

    String importingStorageDomainProgress();

    String playSpiceConsole();

    String suspendSpiceConsole();

    String stopSpiceConsole();

    String noLUNsSelectedInvalidReason();

    String noStorageDomainsSelectedInvalidReason();

    String noStorageDomainsFound();

    String couldNotRetrieveLUNsLunsFailure();

    String kernelParamsInvalid();

    String initrdPathInvalid();

    String allocCanBeModifiedOnlyWhenImportSingleVm();

    String vmCreateVirtualDiskAction();

    String vmAttachVirtualDisksAction();

    String thereIsNoActiveStorageDomainCreateDiskInMsg();

    String errorRetrievingRelevantStorageDomainMsg();

    String couldNotReadTemplatesFromExportDomainMsg();

    String theFollowingTemplatesAreMissingOnTargetExportDomainMsg();

    String theFollowingTemplatesAreMissingOnTargetExportDomainForTemplateVersionsMsg();

    String noActiveDataCenters();

    String noActiveStorageDomainsInDC();

    String noManagedBlockDomainsInDC();

    String relevantDCnotActive();

    String hostNameValidationMsg();

    String rootRoleTree();

    String systemRoleTree();

    String configureSystemRoleTree();

    String dataCenterRoleTree();

    String configureDataCenterRoleTree();

    String networkRoleTree();

    String configureNetworkRoleTree();

    String configureVnicProfileRoleTree();

    String storageDomainRoleTree();

    String configureStorageDomainRoleTree();

    String configureDiskProfileRoleTree();

    String allowToCreateDiskProfileRoleTreeTooltip();

    String allowToDeleteDiskProfileRoleTreeTooltip();

    String allowToUpdateDiskProfileRoleTreeTooltip();

    String clusterRoleTree();

    String configureClusterRoleTree();

    String hostRoleTree();

    String volumeRoleTree();

    String configureVolumesRoleTree();

    String allowToCreateGlusterVolumesRoleTree();

    String allowToDeleteGlusterVolumesRoleTree();

    String allowToManipulateGlusterVolumesRoleTree();

    String configureHostRoleTree();

    String templateRoleTree();

    String basicOperationsRoleTree();

    String provisioningOperationsRoleTree();

    String vmRoleTree();

    String administrationOperationsRoleTree();

    String vmPoolRoleTree();

    String attachDiskProfileRoleTree();

    String notePermissionsContainingDiskProfileOperationsRoleTreeTooltip();

    String allowToAttachDiskProfileToDiskRoleTreeTooltip();

    String thisNetworkDoesNotExistInTheClusterErr();

    String subnetMaskIsNotValid();

    String duplicateLabel();

    String requestToServerFailedWithCode();

    String requestToServerFailed();

    String noValidateMessage();

    String addBricksVolume();

    String addBricksButtonLabel();

    String removeBricksButtonLabel();

    String clearBricksButtonLabel();

    String removeAllBricksButtonLabel();

    String moveBricksUpButtonLabel();

    String moveBricksDownButtonLabel();

    String addBricksReplicateConfirmationTitle();

    String addVolume();

    String editVolume();

    String edit();

    String resetVolume();

    String resetAllVolume();

    String addOptionVolume();

    String editOptionVolume();

    String editOptionsTitle();

    String errorInFetchingVolumeOptionList();

    String resetOptionVolumeTitle();

    String resetOptionVolumeMsg();

    String resetAllOptionsTitle();

    String resetAllOptionsMsg();

    String confirmStopVolume();

    String stopVolumeMessage();

    String startForceVolumeMessage();

    String confirmStartVolume();

    String startForceLabel();

    String stopVolumeWarning();

    String stopMetaVolumeWarning();

    String removeVolumesTitle();

    String removeVolumesWarning();

    String removeMetaVolumeWarning();

    String removeBricksTitle();

    String removeBricksStatusTitle();

    String replaceBrickTitle();

    String cannotAddBricksNoUpServerFound();

    String advancedDetailsBrickTitle();

    String errorInFetchingBrickAdvancedDetails();

    String removeBricksMessage();

    String removeMetaVolumeBricksMessage();

    String removeMetaVolumeBricksWarning();

    String removeBricksWarning();

    String stopRemoveBricksTitle();

    String stopRemoveBricksMessage();

    String stopRemoveBricksButton();

    String commitRemoveBricksButton();

    String retainBricksButton();

    String commitRemoveBricksTitle();

    String commitRemoveBricksMessage();

    String retainBricksTitle();

    String retainBricksMessage();

    String duplicateBrickMsg();

    String invalidBrickDirectoryMsg();

    String invalidBrickDirectoryStartWithSlashMsg();

    String invalidBrickDirectoryContainsSpaceMsg();

    String invalidBrickDirectoryAtleastTwoCharacterseMsg();

    String invalidKey();

    String emptyBrickDirectoryMsg();

    String emptyServerBrickMsg();

    String distriputedVolumeAddBricksMsg();

    String replicateVolumeAddBricksMsg();

    String distriputedReplicateVolumeAddBricksMsg();

    String stripeVolumeAddBricksMsg();

    String distriputedStripeVolumeAddBricksMsg();

    String stripedReplicateVolumeAddBricksMsg();

    String distriputedStripedReplicateVolumeAddBricksMsg();

    String emptyAddBricksMsg();

    String addBricksToReplicateVolumeFromSameServerMsg();

    String confirmStopVolumeRebalanceTitle();

    String confirmDisableGlusterHooks();

    String disableGlusterHooksMessage();

    String viewContentGlusterHookTitle();

    String viewContentErrorGlusterHook();

    String viewContentEmptyGlusterHook();

    String resolveConflictsGlusterHookTitle();

    String vmAlreadyExistsMsg();

    String templateAlreadyExistsMsg();

    String vmNoExistsMsg();

    String templateNoExistsMsg();

    String defaultQuotaPrefix();

    String quotaIsEmptyValidation();

    String clusterServiceValidationMsg();

    String pleaseSelectKey();

    String noKeyAvailable();

    String volumeTransportTypesValidationMsg();

    String volumeEmptyClusterValidationMsg();

    String changeCd();

    String noCds();

    String send();

    String toggleFullScreen();

    String specialKeys();

    String nfsVersionAutoNegotiate();

    String nfsVersion3();

    String nfsVersion4();

    String nfsVersion40();

    String nfsVersion41();

    String nfsVersion42();

    String shareableDiskNotSupported();

    String shareableDiskNotSupportedOnRunningVM();

    String shareableDiskNotSupportedByConfiguration();

    String cannotRemoveBricksReplicateVolume();

    String cannotRemoveBricksDistributedReplicateVolume();

    String cannotRemoveBricksDistributedStripeVolume();

    String cannotRemoveBricksStripedReplicateVolume();

    String cannotRemoveBricksDistributedStripedReplicateVolume();

    String addBricksReplicaCountIncreaseValidationMsg();

    String addBricksStripeCountIncreaseValidationMsg();

    String consoleOptions();

    String consolePublicKeyTitle();

    String mgmtNotAttachedToolTip();

    String lunUnusable();

    String forceStorageDomainCreation();

    String lunsAlreadyInUse();

    String lunsAlreadyPartOfSD();

    String syncAllHostNetworkConfirmationDialogTitle();

    String syncAllClusterNetworkConfirmationDialogTitle();

    String areYouSureYouWantToSyncAllHostNetworksMsg();

    String areYouSureYouWantToSyncAllClusterNetworksMsg();

    String nullOperationTooManyNonVlans();

    String nullOperationDuplicateVlanIds();

    String nullOperationInvalidBondMode();

    String assignQuotaForDisk();

    String importMissingStorages();

    String errorTemplateCannotBeFoundMessage();

    String loadingPublicKey();

    String errorLoadingHostSshPublicKey();

    String errorLoadingPublicKey();

    String successLoadingPublicKey();

    String sshPublicKeyNotVerified();

    String publicKeyAddressError();

    String manageGlusterSwiftTitle();

    String emptyGlusterHosts();

    String missingQuotaStorageEnforceMode();

    String missingVirtioDriversForWindows();

    String missingQuotaClusterEnforceMode();

    String importVmConflictTitle();

    String importTemplateConflictTitle();

    String attachedHost();

    String unattachedHost();

    String runningVm();

    String notRunningVm();

    String removeVmDisksTemplateMsg();

    String removeVmDisksNoDisksMsg();

    String removeVmDisksSnapshotsMsg();

    String removeVmDisksAllSharedMsg();

    String emptyNewGlusterHosts();

    String detachGlusterHostsTitle();

    String hotTypeUpdateNotPossible();

    String hotMacUpdateNotPossible();

    String hotNetworkUpdateNotSupportedExternalNetworks();

    String hotLinkStateUpdateNotSupportedExternalNetworks();

    String linkStateUpdateNotSupportedForPassthroughVnic();

    String confirmConsoleConnect();

    String confirmConsoleConnectMessage();

    String userCantReconnectToVm();

    String noDisksSelected();

    String alertsTitle();

    String onlyOneBootableDisk();

    String posixVfsTypeHint();

    String mountOptionsHint();

    String driverOptionsHint();

    String driverSensitiveOptionsHint();

    String eventDetailsTitle();

    String hosCPUUnavailable();

    String cpuAutoDetect();

    String cpuPinningUnavailable();

    String cpuChangesConflictWithAutoPin();

    String portMirroringNotSupportedExternalNetworks();

    String passthroughNotSupportedExternalNetworks();

    String portMirroringNotChangedIfUsedByVms();

    String portMirroringNotChangedIfPassthrough();

    String networkQosNotChangedIfPassthrough();

    String networkFilterNotChangedIfPassthrough();

    String passthroughNotChangedIfUsedByVms();

    String failoverNotChangedIfUsedByVms();

    String vmLowPriority();

    String vmMediumPriority();

    String vmHighPriority();

    String vmUnknownPriority();

    String anyHostInCluster();

    String anyHostInDataCenter();

    String anyDataCenter();

    String unsupported();

    String unknown();

    String smtDisabled();

    String smtEnabled();

    String primaryPmVariant();

    String secondaryPmVariant();

    String externalHostsDiscovered();

    String externalHostsProvisioned();

    String eject();

    String providerNetworksTitle();

    String addProviderTitle();

    String editProviderTitle();

    String removeProviderTitle();

    String terminateSessionTitle();

    String providerUrlWarningTitle();

    String importNetworksTitle();

    String importNetworksButton();

    String importDuplicateName();

    String removeNetworkFromProvider();

    String noResolveActionSelectedGlusterHook();

    String noActionSelectedManageGlusterSwift();

    String hostActivationTimeOut();

    String hostChangeClusterTimeOut();

    String noHaWhenHostedEngineUsed();

    String trustedServiceDisabled();

    String removeNetworkQoSTitle();

    String removeStorageQoSTitle();

    String removeCpuQoSTitle();

    String editNetworkQoSTitle();

    String editStorageQoSTitle();

    String editCpuQoSTitle();

    String newNetworkQoSTitle();

    String newHostNetworkQosTitle();

    String editHostNetworkQosTitle();

    String removeHostNetworkQosTitle();

    String newStorageQoSTitle();

    String newCpuQoSTitle();

    String removeNetworkQoSMessage();

    String newClusterPolicyTitle();

    String editClusterPolicyTitle();

    String copyClusterPolicyTitle();

    String removeClusterPolicyTitle();

    String shareKsmAcrossNumaNodes();

    String shareKsmInsideEachNumaNode();

    String cloudInitRootPasswordMatchMessage();

    String cloudInitDnsServerListMessage();

    String cloudInitNewAttachmentItem();

    String cloudInitAttachmentTypePlainText();

    String cloudInitAttachmentTypeBase64();

    String cloudInitBase64Message();

    String vnicProfileTitle();

    String cannotEditNameInTreeContext();

    String cannotChangeDCInTreeContext();

    String cannotChangeClusterInTreeContext();

    String cannotChangeHostInTreeContext();

    String cannotUndeployHeFromLastHostWithHeDeployed();

    String dcCanOnlyBeChangedWhenHostInMaintMode();

    String clusterCanOnlyBeChangedWhenHostInMaintMode();

    String clusterNoDefaultNetworkProvider();

    String timeZoneCannotBeChangedAfterVMInit();

    String qosNotSupportedDcVersion();

    String unlimitedQoSTitle();

    String rebalanceStatusTitle();

    String fetchingDataMessage();

    String stopRebalance();

    String managePolicyUnits();

    String glusterDomainConfigurationMessage();

    String notAvailableWithNoUpDC();

    String suspendedVMsWhenClusterChange();

    String notAvailableWithNoUpDCWithClusterWithPermissions();

    String runningVmsWereFilteredOnImportVm();

    String nonRetrievedVmsWereFilteredOnImportVm();

    String nonRetrievedAndRunningVmsWereFilteredOnImportVm();

    String runningVmsWereAllFilteredOnImportVm();

    String nonRetrievedVmsWereAllFilteredOnImportVm();

    String nonRetrievedAndRunningVmsWereAllFilteredOnImportVm();

    String notAvailableWithNoActiveExportDomain();

    String notAvailableWithNoTemplates();

    String connectToPoolNotSupported();

    String affinityLabelsTitle();

    String newAffinityLabelTitle();

    String editAffinityLabelTitle();

    String removeAffinityLabelsTitle();

    String affinityGroupsTitle();

    String newAffinityGroupsTitle();

    String editAffinityGroupsTitle();

    String removeAffinityGroupsTitle();

    String selectHost();

    String selectVm();

    String noAvailableVms();

    String noAffinityGroupsSelected();

    String noAffinityLabelsSelected();

    String selectedAffinityGroups();

    String selectedAffinityLabels();

    String noAvailableHosts();

    String iscsiBondsTitle();

    String addIscsiBondTitle();

    String editIscsiBondTitle();

    String removeIscsiBondTitle();

    String noNetworksSelected();

    String latestTemplateVersionName();

    String latestTemplateVersionDescription();

    String cannotEnableVirtioScsiForOs();

    String maintenanceStorageDomainsTitle();

    String areYouSureYouWantToPlaceFollowingStorageDomainsIntoMaintenanceModeMsg();

    String deactivateVmDisksTitle();

    String areYouSureYouWantDeactivateVMDisksMsg();

    String cannotEnableIdeInterfaceForReadOnlyDisk();

    String cannotEnableReadonlyWhenScsiPassthroughEnabled();

    String cannotEnableSgioWhenScsiPassthroughDisabled();

    String cannotEnableScsiPassthroughForLunReadOnlyDisk();

    String haGlobalMaintenance();

    String haLocalMaintenance();

    String haNotActive();

    String cannotHotPlugDiskWithIdeInterface();

    String ideDisksWillBeAttachedButNotActivated();

    String cannotPlugDiskIncorrectVmStatus();

    String unplugVnicTitle();

    String areYouSureYouWantUnplugVnicMsg();

    String newInstanceTypeTitle();

    String editInstanceTypeTitle();

    String removeInstanceTypeTitle();

    String customInstanceTypeName();

    String customInstanceTypeDescription();

    String vmsAttachedToInstanceTypeNote();

    String vmsAttachedToInstanceTypeWarningMessage();

    String storageDomainsAttachedToDataCenterWarningTitle();

    String storageDomainsAttachedToDataCenterWarningMessage();

    String rngRateInvalid();

    String rngNotSupportedByClusterCV();

    String rngNotSupported();

    String loginAllButtonLabel();

    String loginButtonLabel();

    String defaultMtu();

    String customMtu();

    String macPoolTree();

    String macPoolUser();

    String allowToCreateMacPoolTooltip();

    String allowToEditMacPoolTooltip();

    String allowToDeleteMacPoolTooltip();

    String allowToUseMacPoolTooltip();

    String optimiseForVirtStoreTitle();

    String optimiseForVirtStoreWarning();

    String optimiseForVirtStoreContinueMessage();

    String continueOptimiseForVirtStore();

    String doNotOptimiseForVirtStore();

    String diskProfilesTitle();

    String diskProfileTitle();

    String removeDiskProfileTitle();

    String removeCpuProfileTitle();

    String cpuProfileTitle();

    String testFailedInsufficientParams();

    String numaDisabledInfoMessage();

    String numaInfoMessage();

    String eitherTotalOrReadWriteCanHaveValues();

    String detachWarningNote();

    String removeDataCenterWarningNote();

    String back();

    String next();

    String invalidGuidMsg();

    String geoRepForceHelp();

    String createScheduleVolumeSnapshotTitle();

    String editVolumeSnapshotScheduleTitle();

    String endDateOptionText();

    String noEndDateOptionText();

    String configureClusterSnapshotOptionsTitle();

    String configureVolumeSnapshotOptionsTitle();

    String snapshotConfigUpdateButtonLabel();

    String updateSnapshotConfigurationConfirmationTitle();

    String removeGlusterVolumeSnapshotScheduleConfirmationTitle();

    String youAreAboutChangeSnapshotConfigurationMsg();

    String youAreAboutToRemoveSnapshotScheduleMsg();

    String existingDisk();

    String creatingDisk();

    String attachingDisk();

    String bootDisk();

    String storageDevices();

    String confirmVolumeSnapshotRestoreWithStopMessage();

    String confirmVolumeSnapshotDeleteAllMessage();

    String confirmVolumeSnapshotActivateMessage();

    String confirmVolumeSnapshotDeactivateMessage();

    String geoReplicationConfigSetTitle();

    String geoRepSessionConfigSetFailed();

    String geoReplicationConfigResetTitle();

    String geoRepSessionConfigResetFailed();

    String lastDay();

    String noWeekDaysSelectedMessage();

    String noMonthDaysSelectedMessage();

    String lastDayMonthCanBeSelectedAlone();

    String endDateBeforeStartDate();

    String unableToFetchVolumeSnapshotSchedule();

    String createBrick();

    String prohibitManagementNetworkChangeInEditClusterInfoMessage();

    String newGeoRepSessionTitle();

    String rootUser();

    String geoReplicationRecommendedConfigViolation();

    String notSupportedForManagedBlockDisks();

    String registerDisksTitle();

    String createSecretTitle();

    String editSecretTitle();

    String removeSecretTitle();

    String iconIsNotParsable();

    String iconNotValidatedYet();

    String secretValueMustBeInBase64();

    String selectStorageDevice();

    String invalidMountPointMsg();

    String invalidName();

    String allNetworksAllowed();

    String specificNetworksAllowed();

    String hostDevicesTitle();

    String removeHostDevices();

    String addVmHostDevicesTitle();

    String repinHostTitle();

    String someNonDefaultTemplateHasToExistFirst();

    String vmDevicesTitle();

    String providerNone();

    String errata();

    String katelloProblemRetrievingErrata();

    String confirmClusterWarnings();

    String configured();

    String notConfigured();

    String soundDeviceUnavailable();

    String unableToRemoveTitle();

    String unableToRemove();

    String stopGlusterServices();

    String ignoreGlusterQuorumChecks();

    String noClusterSupportingArchitectureInDC();

    String sameArchitectureRequired();

    String availableOnlyWithLegacyPolicy();

    String xenUriExample();

    String kernelCmdlineNotAvailableInClusterWithIbmCpu();

    String kernelCmdlineCheckboxesAndDirectCustomizationNotAllowed();

    String currentKernelCmdLine();

    String cannotClusterVersionChangeWithActiveVm();

    String openingNewConsoleWindowFailed();

    String emptyImagePath();

    String kvmBlockDomainWraning();

    String arbiterVolumeShouldBeReplica3();

    String arbiter();

    String headlessMode();

    String vnicProfilesMapping();

    String heHostRemovalWarning();

    String vmLeasesSupported();

    String vmLeasesNotSupportedWithoutHA();

    String storageDomainDRTitle();

    String storageDomainLeaseTitle();

    String noManagedGlusterVolumeMessage();

    String memoryHotUnplug();

    String incrementalBackupNotSupportedForRawDisks();

    String discardIsNotSupportedByUnderlyingStorage();

    String theUnderlyingStorageDoesNotSupportDiscardWhenWipeAfterDeleteIsEnabled();

    String theUnderlyingStorageDoesNotSupportWipeAfterDeleteWhenDiscardIsEnabled();

    String noGeoRepSessionForGlusterVolume();

    String forceRemoveProvider();

    String both();

    String runningOnCurrentHost();

    String pinnedToCurrentHost();

    String replaceBrickWarning();

    String ignoreOVFUpdateFailure();

    String cannotEditNotActiveLeaseDomain();

    String selectCacheDevicePath();

    String selectCacheMode();

    String resetBrickTitle();

    String resetBrickMessage();

    String clusterSwitchChangeDisabled();

    String externalNetworkInfo();

    String failoverVnicProfile();

    String consolidatedTitle();

    String separatedTitle();

    String vgpuPlacementCompatibilityInfo();

    String portSecurityDisabled();

    String portSecurityEnabled();

    String portSecurityUndefined();

    String diskMaxSizeReached();

    String largeNumberOfDevicesWarning();

    String pendingVMChanges();

    String cpuPassthrough();

    String selectUserOrGroup();

    String selectRoleToAssign();

    String customSerialNumberDisabledReason();

    String systemDefaultCustomSerialNumberDisabledReason();

    String clusterDefaultCustomSerialNumberDisabledReason();

    String updatingStorageDomainTitle();

    String chipsetDependentVmDeviceChangesTitle();

    String chipsetDependentVmDeviceChangesMessage();

    String mixedTargetDomains();

    String uefiRequired();

    String guestOsVersionNotSupported();

    String sealWindowsUnavailable();

    String numaTuneModeDisabledReasonNodeUnpinned();

    String numaTuneModeDisabledReasonNotCurrentlyEditedVM();

    String consoleDisconnectActionDelayDisabledReason();

    String adjustToHost();

    String cpuPinningNoneDescription();

    String cpuPinningManualDescription();

    String cpuPinningManualDisabled();

    String cpuPinningResizeAndPinDescription();
}
