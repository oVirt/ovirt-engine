package org.ovirt.engine.ui.uicompat;

import java.util.List;

import com.google.gwt.i18n.client.Messages;

public interface UIMessages extends Messages {
    String customPropertyOneOfTheParamsIsntSupported(String parameters);

    String customPropertiesValuesShouldBeInFormatReason(String format);

    String keyValueFormat();

    String emptyOrValidKeyValueFormatMessage(String format);

    String customPropertyValueShouldBeInFormatReason(String parameter, String format);

    String createOperationFailedDcGuideMsg(String storageName);

    String nameCanContainOnlyMsg(int maxNameLength);

    String detachNote(String localStoragesFormattedString);

    String youAreAboutToDisconnectHostInterfaceMsg(String nicName);

    String connectingToGuestWithNotResponsiveAgentMsg();

    String hostNameMsg(int hostNameMaxLength);

    String naturalNumber();

    String realNumber();

    String thisFieldMustContainTypeNumberInvalidReason(String type);

    String numberValidationNumberBetweenInvalidReason(String prefixMsg, String min, String max);

    String numberValidationNumberGreaterInvalidReason(String prefixMsg, String min);

    String numberValidationNumberLessInvalidReason(String prefixMsg, String max);

    String integerValidationNumberBetweenInvalidReason(String prefixMsg, int min, int max);

    String integerValidationNumberGreaterInvalidReason(String prefixMsg, int min);

    String integerValidationNumberLessInvalidReason(String prefixMsg, int max);

    String longValidationNumberBetweenInvalidReason(String prefixMsg, long min, long max);

    String longValidationNumberGreaterInvalidReason(String prefixMsg, long min);

    String longValidationNumberLessInvalidReason(String prefixMsg, long max);

    String lenValidationFieldMusnotExceed(int maxLength);

    String vmStorageDomainIsNotAccessible();

    String noActiveStorageDomain();

    String alreadyAssignedClonedVmName();

    String suffixCauseToClonedVmNameCollision(String vmName);

    String alreadyAssignedClonedTemplateName();

    String suffixCauseToClonedTemplateNameCollision(String templateName);

    String createFailedDomainAlreadyExistStorageMsg(String storageName);

    String importFailedDomainAlreadyExistStorageMsg(String storageName);

    String memSizeBetween(int minMemSize, int maxMemSize);

    String maxMemSizeIs(int maxMemSize);

    String minMemSizeIs(int minMemSize);

    String maxMaxMemoryForSelectedOsIs(int maxMaxMemorySize);

    String maxMaxMemoryIs(int maxMaxMemorySize);

    String memSizeMultipleOf(String architectureName, int multiplier);

    String nameMustConataionOnlyAlphanumericChars(int maxLen);

    String newNameWithSuffixCannotContainBlankOrSpecialChars(int maxLen);

    String importProcessHasBegunForVms(String importedVms);

    String importProcessHasBegunForImages(String images);

    String storageDomainIsNotActive(String storageName);

    String importProcessHasBegunForTemplates(String importedTemplates);

    String templatesAlreadyExistonTargetExportDomain(String existingTemplates);

    String vmsAlreadyExistOnTargetExportDomain(String existingVMs);

    String templatesWithDependentVMs(String template, String vms);

    String sharedDisksWillNotBePartOfTheExport(String diskList);

    String directLUNDisksWillNotBePartOfTheExport(String diskList);

    String snapshotDisksWillNotBePartOfTheExport(String diskList);

    String noExportableDisksFoundForTheExport();

    String sharedDisksWillNotBePartOfTheSnapshot(String diskList);

    String directLUNDisksWillNotBePartOfTheSnapshot(String diskList);

    String snapshotDisksWillNotBePartOfTheSnapshot(String diskList);

    String noExportableDisksFoundForTheSnapshot();

    String sharedDisksWillNotBePartOfTheTemplate(String diskList);

    String directLUNDisksWillNotBePartOfTheTemplate(String diskList);

    String snapshotDisksWillNotBePartOfTheTemplate(String diskList);

    String noExportableDisksFoundForTheTemplate();

    String templateVersionNameAndNumber(String versionName, int versionNumber);

    String errConnectingVmUsingSpiceMsg(Object errCode);

    String errConnectingVmUsingRdpMsg(Object errCode);

    String areYouSureYouWantToDeleteSanpshot(String from, Object description);

    String incrementalBackupEnableWillRemovedForDisks(String diskList);

    String areYouSureYouWantToCommitSnapshot(String from, Object description);

    String editBondInterfaceTitle(String name);

    String editHostNicVfsConfigTitle(String name);

    String editManagementNetworkTitle(String networkName);

    String editNetworkTitle(String name);

    String setupHostNetworksTitle(String hostName);

    String noOfBricksSelected(int brickCount);

    String vncInfoMessage(String hostIp, int port, String password, int seconds);

    String lunAlreadyPartOfStorageDomainWarning(String storageDomainName);

    String lunIsMetadataDevice(String storageDomainName);

    String lunUsedByDiskWarning(String diskAlias);

    String lunUsedByVG(String vgID);

    String usedLunIdReason(String id, String reason);

    String removeBricksReplicateVolumeMessage(int oldReplicaCount, int newReplicaCount);

    String breakBond(String bondName);

    String detachNetwork(String networkName);

    String removeNetwork(String networkName);

    String attachTo(String name);

    String bondWith(String name);

    String addToBond(String name);

    String extendBond(String name);

    String removeFromBond(String name);

    String label(String label);

    String unlabel(String label);

    String suggestDetachNetwork(String networkName);

    String labelInUse(String label, String ifaceName);

    String incorrectVCPUNumber();

    String poolNameLengthInvalid(int maxLength, int vmsInPool);

    String poolNameWithQuestionMarksLengthInvalid(int maxLength, int vmsInPool, int numberOfQuestionMarks);

    String numOfVmsInPoolInvalid(int maxNumOfVms, int poolNameLength);

    String numOfVmsInPoolInvalidWithQuestionMarks(int maxNumOfVms, int poolNameLength, int numberOfQuestionMarks);

    String refreshInterval(int intervalSec);

    String importClusterHostNameEmpty(String address);

    String importClusterHostPasswordEmpty(String address);

    String importClusterHostSshPublicKeyEmpty(String address);

    String unreachableGlusterHosts(List<String> hosts);

    String networkDcDescription(String networkName, String dcName, String description);

    String networkDc(String networkName, String dcName);

    String vnicFromVm(String vnic, String vm);

    String vnicProfileFromNetwork(String vnicProfile, String network);

    String vnicFromTemplate(String vnic, String template);

    String bridlessNetworkNotSupported(String version);

    String numberOfVmsForHostsLoad(int numberOfVms);

    String cpuInfoLabel(int numberOfCpus, int numberOfSockets, int numberOfCpusPerSocket, int numberOfThreadsPerCore);

    String templateDiskDescription(String diskAlias, String storageDomainName);

    String interfaceIsRequiredToBootFromNetwork();

    String bootableDiskIsRequiredToBootFromDisk();

    String disklessVmCannotRunAsStateless();

    String urlSchemeMustBeEmpty(String passedScheme);

    String urlSchemeMustNotBeEmpty(String allowedSchemes);

    String urlSchemeInvalidScheme(String passedScheme, String allowedSchemes);

    String providerUrlWarningText(String providedEntities);

    String nicHotPlugNotSupported(String clusterVersion);

    String customSpmPriority(int priority);

    String brickDetailsNotSupportedInClusterCompatibilityVersion(String version);

    String hostNumberOfRunningVms(String hostName, int runningVms);

    String commonMessageWithBrackets(String subject, String inBrackets);

    String removeNetworkQoSMessage(int numOfProfiles);

    String removeStorageQoSMessage(int numOfProfiles);

    String removeStorageQoSItem(String qosName, String diskProfileNames);

    String removeCpuQoSMessage(int numOfProfiles);

    String removeHostNetworkQosMessage(int numOfNetworks);

    String cpuInfoMessage(int numOfCpus, int sockets, int coresPerSocket, int threadsPerSocket);

    String numaTopologyTitle(String hostName);

    String rebalanceStatusFailed(String name);

    String volumeProfileStatisticsFailed(String volName);

    String removeBrickStatusFailed(String name);

    String confirmStopVolumeRebalance(String name);

    String cannotMoveDisks(String disks);

    String cannotCopyDisks(String disks);

    String moveDisksConvertToCowWarning(String disks);

    String moveDisksWhileVmRunning(String disks);

    String errorConnectingToConsole(String name, String s);

    String errorConnectingToConsoleNoProtocol(String name);

    String cannotConnectToTheConsole(String vmName);

    String schedulerOptimizationInfo(int numOfRequests);

    String schedulerAllowOverbookingInfo(int numOfRequests);

    String vmTemplateWithCloneProvisioning(String templateName);

    String vmTemplateWithThinProvisioning(String templateName);

    String youAreAboutChangeDcCompatibilityVersionWithUpgradeMsg(String version);

    String haActive(int score);

    String hugePages(String size, String free, String total);

    String volumeProfilingStatsTitle(String volumeName);

    String networkLabelConflict(String nicName, String labelName);

    String labeledNetworkNotAttached(String nicName, String labelName);

    String bootMenuNotSupported(String clusterVersion);

    String diskSnapshotLabel(String diskAlias, String snapshotDescription);

    String optionNotSupportedClusterVersionTooOld(String clusterVersion);

    String optionRequiresSpiceEnabled();

    String rngSourceNotSupportedByCluster(String source);

    String glusterVolumeCurrentProfileRunTime(int currentRunTime, String currentRunTimeUnit, int totalRunTime, String totalRunTimeUnit);

    String bytesReadInCurrentProfileInterval(String currentBytesRead, String currentBytesReadUnit, String totalBytes, String totalBytesUnit);

    String bytesWrittenInCurrentProfileInterval(String currentBytesWritten, String currentBytesWrittenUnit, String totalBytes, String totalBytesUnit);

    String defaultMtu(int mtu);

    String threadsAsCoresPerSocket(int cores, int threads);

    String approveCertificateTrust(String subject, String issuer, String shaFingerprint, String shaName);

    String approveRootCertificateTrust(String subject, String shaFingerprint, String shaName);

    String geoRepForceTitle(String action);

    String geoRepActionConfirmationMessage(String action);

    String iconDimensionsTooLarge(int width, int height, int maxWidht, int maxHeight);

    String iconFileTooLarge(int maxSize);

    String invalidIconFormat(String s);

    String clusterSnapshotOptionValueEmpty(String option);

    String volumeSnapshotOptionValueEmpty(String option);

    String vmDialogDisk(String name, String sizeInGb, String type, String boot);

    String confirmRestoreSnapshot(String volumeName);

    String confirmRemoveSnapshot(String volumeName);

    String confirmRemoveAllSnapshots(String volumeName);

    String confirmActivateSnapshot(String volumeName);

    String confirmDeactivateSnapshot(String volumeName);

    String confirmVolumeSnapshotDeleteMessage(String snapshotNames);

    String sizeUnitString(String size, String sizeUnit);

    String userSessionRow(long sessionId, String UserName);

    String testSuccessfulWithPowerStatus(String powerStatus);

    String testFailedWithErrorMsg(String errorMessage);

    String uiCommonRunActionPartitialyFailed(String reason);

    String vnicTypeDoesntMatchPassthroughProfile(String type);

    String vnicTypeDoesntMatchNonPassthroughProfile(String type);

    String guestOSVersionOptional(String optional);

    String guestOSVersionLinux(String distribution, String version, String codeName);

    String guestOSVersionWindows(String version, String build);

    String guestOSVersionWindowsServer(String version, String build);

    String positiveTimezoneOffset(String name, String hours, String minutes);

    String negativeTimezoneOffset(String name, String hours, String minutes);

    String bracketsWithGB(int value);

    String criticalSpaceActionBlockerBiggerThanStorageDomain(int value);

    String confirmDeleteFenceAgent(String agentDisplayString);

    String confirmDeleteAgentGroup(String agents);

    String failedToLoadOva(String ovaPath);

    String failedToListExternalNetworks(String detailedErrorMessage);

    String errataForHost(String hostName);

    String errataForVm(String vmName);

    String uploadImageFailedToStartMessage(String reason);

    String uploadImageFailedToResumeMessage(String reason);

    String uploadImageFailedToResumeSizeMessage(long priorFileBytes, long newFileBytes);

    String uploadImageFailedToResumeUploadOriginatedInAPI();

    String providerFailure();

    String providerImportFailure();

    String userName(String firstName, String lastName);

    String thereAreActiveVMsRequiringRestart(int count);

    String uploadImageQemuCompatUnsupported(String compat, String storageFormatType);

    String memoryHotUnplugConfirmation(int memorySizeMb, String vmName);

    String nullOperationUnmanagedNetwork(String networkName);

    String nullOperationOutOfSyncNetwork(String networkName);

    String storageDomainOfDiskCannotBeAccessed(String diskName);

    String highPerformancePopupRecommendationMsgForKsm(String clusterName);

    String biosTypeSupportedForX86Only();

    String glusterPeerNotMatchingHostSshPublicKey(String hostAddress, String glusterPeerAddress);

    String noTemplateNameDuplicatesAllowed();

    String noVmNameDuplicatesAllowed();

    String vmName(String vmName);

    String hostName(String hostName);

    String labelName(String labelName);

    String availableInVersionOrHigher(String version);

    String compareStorageFormatToDataCenterWarningMessage(String dataCenterName, String storageDomainName,
            String storageDomainFormat, String dcVersion);

    String compareMultipleStorageFormatsToDataCenterWarningMessage(String dataCenterName, String storageDomainNames);

    String clusterSnapshotOptionNotExist();

    String detachStorageDomainContainsEntitiesWithDisksOnMultipleSDsFromDC();

    String detachStorageDomainsContainEntitiesWithDisksOnMultipleSDs();

    String removeStorageDomainWithMemoryVolumesOnMultipleSDs(String diskIds);

    String removeStorageDomainFromDataCenterWithMemoryVolumesOnMultipleSDs(String storageDomainName, String diskIds);

    String virtioScsiRequired();

    String creatingIsoDomainDeprecatedMessage();

    String detachStorageDomainContainsEntitiesWithLeaseOfVmsOnOtherSDsFromDC(String storageDomainName, String entitiesIds);

    String detachStorageDomainContainsEntitiesWithLeaseOfVmsOnOtherSDs(String entitiesIds);
}
