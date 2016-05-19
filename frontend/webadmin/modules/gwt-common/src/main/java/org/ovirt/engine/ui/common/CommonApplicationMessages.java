package org.ovirt.engine.ui.common;

import com.google.gwt.i18n.client.Messages;
import com.google.gwt.safehtml.shared.SafeHtml;

public interface CommonApplicationMessages extends Messages {
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

    String lastDiskAlignment(String lastScanDate);

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

    SafeHtml profileAndNetwork(String profileName, String networkName);

    SafeHtml profileAndNetworkSelected(String profileName, String networkName);

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

    String migrationPolicyInfo();

    String hotPlugUnplugCpuWarning();

    String threadsPerCoreInfo();

    String serialNumberInfo();

    String nextRunConfigurationExists();

    String nextRunConfigurationCanBeAppliedImmediately();

    String nextRunConfigurationCpuValue();

    String nextRunConfigurationMemoryValue();

    String unpinnedRunningVmWarningTitle();

    String warningSectionTitle();

    String unpinnedRunningVmWarningIncompatability();

    String unpinnedRunningVmWarningSecurity();

    String snapshotPreviewing(String snapshotDescription, String diskAliases);

    String vmDisksLabel(int numOfDisks, String diskAliases);

    String snapshotDisksLabel(int numOfDisks, String diskAliases);

    String imageUploadProgress(int mbSent);

    String imageUploadProgressWithTotal(int mbSent, int mbTotal);

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
}

