package org.ovirt.engine.ui.common;

import com.google.gwt.i18n.client.Messages.DefaultMessage;
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

    @DefaultMessage("Error Connecting to {0}. This browser does not support {1} protocol")
    String errorConnectingToConsole(String name, String protocol);

    @DefaultMessage("Connecting to VM pool is not supported")
    String connectingToPoolIsNotSupported();

    @DefaultMessage("Select Console for ''{0}''")
    String selectConsoleFor(String name);

    @DefaultMessage("{0} sec")
    String refreshRateSeconds(Integer seconds);

    @DefaultMessage("default [{0}]")
    String globalVncKeyboardLayoutCaption(String currentDefault);

    @DefaultMessage("default: {0}")
    String defaultTimeZoneCaption(String currentDefault);

    @DefaultMessage("VM has {0} Nics. Assign them to Profiles.")
    String assignNicsToProfilesPlural(int numOfNics);

    @DefaultMessage("VM has 1 Nic. Assign it to a Profile.")
    String assignNicsToProfilesSingular();

    @DefaultMessage("VM has no network interfaces and there are no available profiles to choose from.")
    String assignNicsNothingToAssign();

    @DefaultMessage("{0} ({1})")
    SafeHtml profileAndNetwork(String profileName, String networkName);

    @DefaultMessage("{0}/{1}")
    SafeHtml profileAndNetworkSelected(String profileName, String networkName);
}
