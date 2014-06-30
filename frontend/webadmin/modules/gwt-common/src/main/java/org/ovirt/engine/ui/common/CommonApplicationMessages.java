package org.ovirt.engine.ui.common;

import org.ovirt.engine.core.common.businessentities.VmPool;

import com.google.gwt.i18n.client.Messages;
import com.google.gwt.safehtml.shared.SafeHtml;

public interface CommonApplicationMessages extends Messages {

    // Common error messages

    @DefaultMessage("Error while loading data from server: {0}")
    String asyncCallFailure(String reason);

    // UiCommon related error messages

    @DefaultMessage("Error while executing action: {0}")
    String uiCommonRunActionFailed(String reason);

    @DefaultMessage("Error while executing action {0}: {1}")
    String uiCommonRunActionExecutionFailed(String action, String reason);

    @DefaultMessage("Error while executing query: {0}")
    String uiCommonRunQueryFailed(String reason);

    @DefaultMessage("Connection closed: {0}")
    String uiCommonPublicConnectionClosed(String reason);

    @DefaultMessage("LUN is already part of a Storage Domain: {0}")
    String lunAlreadyPartOfStorageDomainWarning(String storageDomainName);

    @DefaultMessage("LUN is already used by a Disk: {0}")
    String lunUsedByDiskWarning(String diskAlias);

    @DefaultMessage("Attached to {0} VM(s) other than {1}")
    String diskAttachedToOtherVMs(int numberOfVms, String vmName);

    @DefaultMessage("Attached to {0} VM(s)")
    String diskAttachedToVMs(int numberOfVms);

    @DefaultMessage("Note that the disk is:")
    String diskNote();

    @DefaultMessage("Shareable")
    String shareable();

    @DefaultMessage("Bootable")
    String bootable();

    @DefaultMessage("Last scan: {0}")
    String lastDiskAlignment(String lastScanDate);

    @DefaultMessage("out of {0} VMs in pool")
    String outOfXVMsInPool(String numOfVms);

    @DefaultMessage("Number of Prestarted VMs defines the number of VMs in Run state , that are waiting to be attached to Users. Accepted values: 0 to the Number of VMs that already exists in the Pool.")
    String prestartedHelp();

    @DefaultMessage("It is possible to specify mask for the VM indexes, for example: for pool ''my"
            + VmPool.MASK_CHARACTER
            + VmPool.MASK_CHARACTER
            + "pool'' the generated names will be: my01pool,my02pool,...my99pool")
    String poolNameHelp();

    @DefaultMessage("Maximum number of VMs a single user can attach to from this pool. This field must be between 1 and 32,767.")
    String maxAssignedVmsPerUserHelp();

    @DefaultMessage("Free: {0} vCPU")
    String quotaFreeCpus(int numOfVCPU);

    @DefaultMessage("{0} addresses")
    String addressesVmGuestAgent(int numOfAddresses);

    @DefaultMessage("The following disks are in status {0}: {1}")
    String disksStatusWarning(String status, String disksAliases);

    // Model-bound widgets

    @DefaultMessage("{0} ({1} Socket(s), {2} Core(s) per Socket)")
    String cpuInfoLabel(int numberOfCpus, int numberOfSockets, int numberOfCpusPerSocket);

    @DefaultMessage("Clone VM from Snapshot is supported only for Clusters of version {0} and above")
    String cloneVmNotSupported(String minimalClusterVersion);

    @DefaultMessage("{0} ({1} GB free of {2} GB)")
    String storageDomainFreeSpace(String name, int free, int total);

    // Console

    @DefaultMessage("Select Console for ''{0}''")
    String selectConsoleFor(String name);

    @DefaultMessage("{0} sec")
    String refreshRateSeconds(Integer seconds);

    @DefaultMessage("default [{0}]")
    String globalVncKeyboardLayoutCaption(String currentDefault);

    @DefaultMessage("default: {0}")
    String defaultTimeZoneCaption(String currentDefault);

    @DefaultMessage("VM has {0} network interfaces. Assign profiles to them.")
    String assignNicsToProfilesPlural(int numOfNics);

    @DefaultMessage("VM has 1 network interface. Assign a profile to it.")
    String assignNicsToProfilesSingular();

    @DefaultMessage("VM has no network interfaces. To add one, assign a profile.")
    String assignNicsNothingToAssign();

    @DefaultMessage("<Empty>")
    SafeHtml emptyProfile();

    @DefaultMessage("Do not assign any profile to this virtual network interface")
    SafeHtml emptyProfileDescription();

    @DefaultMessage("{0} ({1})")
    SafeHtml profileAndNetwork(String profileName, String networkName);

    @DefaultMessage("{0}/{1}")
    SafeHtml profileAndNetworkSelected(String profileName, String networkName);

    @DefaultMessage("User: {0} with Role: {1}")
    SafeHtml userWithRole(String userName, String roleName);

    @DefaultMessage("Role: {0} on User {1}")
    SafeHtml roleOnUser(String roleName, String userName);

    @DefaultMessage("Map control-alt-del shortcut to {0}")
    String remapCtrlAltDelete(String hotkey);

    @DefaultMessage("This will override the SPICE proxy defined in {0}. Current configuration: {1}.")
    String consoleOverrideSpiceProxyMessage(String parentConfiguration, String parentSpiceProxy);

    @DefaultMessage("cluster")
    String consoleOverrideDefinedOnCluster();

    @DefaultMessage("global configuration")
    String consoleOverrideDefinedInGlobalConfig();

    @DefaultMessage("No SPICE proxy defined")
    String noSpiceProxyDefined();

    @DefaultMessage("{0} or {1}")
    String or(String a, String b);

    @DefaultMessage("Represents maximum number of milliseconds the VM can be down during live migration. Value of 0 means that VDSM default will be used. (Current engine-wide default is {0}ms)")
    String migrationDowntimeInfo(Integer milliseconds);

    @DefaultMessage("Hot add CPUs by changing the number of sockets." +
            " Please consult documentation for your guest operating system to ensure it has proper support for CPU Hot Add")
    String hotPlugUnplugCpuWarning();
}
