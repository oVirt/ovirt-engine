package org.ovirt.engine.ui.common;

import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.compat.Version;

import com.google.gwt.i18n.client.Messages;
import com.google.gwt.safehtml.shared.SafeHtml;

public interface CommonApplicationMessages extends Messages {
    String fromIndexToIndex(int from, int to);

    String fromIndexToIndexOfTotalCount(int from, int to, int totalCount);

    String asyncCallFailure(String reason);

    String uiCommonRunActionFailed(String reason);

    String uiCommonRunActionExecutionFailed(String action, String reason);

    String uiCommonRunQueryFailed(String reason);

    String uiCommonPublicConnectionClosed(String reason);

    String lunAlreadyPartOfStorageDomainWarning(String storageDomainName);

    String lunUsedByDiskWarning(String diskAlias);

    String diskAttachedToOtherVMs(int numberOfVms, String vmName);

    String diskAttachedToVMs(int numberOfVms);

    String diskNote();

    String shareable();

    String bootable();

    String outOfXVMsInPool(String numOfVms);

    String prestartedHelp();

    String poolNameHelp();

    String maxAssignedVmsPerUserHelp();

    String quotaFreeCpus(int numOfVCPU);

    String addressesVmGuestAgent(int numOfAddresses);

    String disksStatusWarning(String status, String disksAliases);

    String cpuInfoLabel(int numberOfCpus, int numberOfSockets, int numberOfCpusPerSocket, int numOfThreadsPerCore);

    String storageDomainFreeSpace(String name, int free, int total);

    String additionalAvailableSizeInGB(int size);

    String selectConsoleFor(String name);

    String refreshRateSeconds(Integer seconds);

    String globalVncKeyboardLayoutCaption(String currentDefault);

    String defaultTimeZoneCaption(String currentDefault);

    SafeHtml emptyProfile();

    SafeHtml emptyProfileDescription();

    String profileAndNetwork(String profileName, String networkName);

    String profileAndNetworkSelected(String profileName, String networkName);

    SafeHtml userWithRole(String userName, String roleName);

    SafeHtml roleOnUser(String roleName, String userName);

    String remapCtrlAltDelete(String hotkey);

    String consoleOverrideSpiceProxyMessage(String parentConfiguration, String parentSpiceProxy);

    String consoleOverrideDefinedOnCluster();

    String consoleOverrideDefinedInGlobalConfig();

    String noSpiceProxyDefined();

    String or(String a, String b);

    String migrationDowntimeInfo(Integer milliseconds);

    String migrationSelectInfo();

    String leaseInfoIcon();

    String resumeBehaviorInfoIcon();

    String migrationPolicyInfo();

    String hostCpuInfo();

    String tscFrequencyInfo();

    String hotPlugUnplugCpuWarning();

    String threadsPerCoreInfo();

    String serialNumberInfo();

    String nextRunConfigurationExists();

    String nextRunConfigurationCanBeAppliedImmediately();

    String nextRunConfigurationCpuValue();

    String nextRunConfigurationMemoryValue();

    String nextRunConfigurationMinAllocatedMemoryValue();

    String nextRunConfigurationVmLeaseValue();

    String highPerformanceConfigurationManualChange();

    String unpinnedRunningVmWarningTitle();

    String warningSectionTitle();

    String unpinnedRunningVmWarningIncompatability();

    String unpinnedRunningVmWarningSecurity();

    String snapshotPreviewing(String snapshotDescription, String diskAliases);

    String vmDisksLabel(int numOfDisks, String diskAliases);

    String snapshotDisksLabel(int numOfDisks, String diskAliases);

    String imageUploadProgress(int mbSent);

    String imageDownloadProgress(int mbSent);

    String imageUploadProgressWithTotal(int mbSent, int mbTotal);

    String imageDownloadProgressWithTotal(int mbSent, int mbTotal);

    String imageTransferringViaAPI();

    String vNumaName(String name, int index);

    String numaTotalCpus(int totalCpu);

    String numaPercentUsed(int percentage);

    String numaMemory(long totalMemory);

    String numaMemoryUsed(long totalMemoryUsed);

    SafeHtml numaSocketNumber(int nodeIndex);

    String numaNode(int index);

    String migratingProgress(String status, String progress);

    String hostDataCenter(String name);

    String uncaughtExceptionAlertMessage(String reloadLink);

    String uncaughtExceptionAlertMessageDetails(String details);

    String templateVersionName(String name);

    String principalLoginName(String name);

    String principalName(String firstName, String lastName);

    String principalEmail(String email);

    String principalDepartment(String department);

    String principalNote(String note);

    String principalNamespace(String namespace);

    String snapshotContainsMemoryIncompatibleCluster(String clusterVersion);

    String bytes(String num);

    String kilobytes(String num);

    String megabytes(String num);

    String mebibytes(String num);

    String gibibytes(String num);

    String memoryHotUnplugNotSupportedForCompatibilityVersionAndArchitecture(Version compatibilityVersion,
            ArchitectureType clusterArch);

    String ovaPathInfo();

    String vmGuestCpuTypeWarning(String cpuType);

    String biosTypeWarning(String bioType);
}

