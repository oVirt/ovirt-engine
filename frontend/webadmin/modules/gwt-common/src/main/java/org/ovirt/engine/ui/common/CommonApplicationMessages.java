package org.ovirt.engine.ui.common;

import com.google.gwt.i18n.client.Messages;

public interface CommonApplicationMessages extends Messages {

    // Confirmation messages

    @DefaultMessage("Are you sure you want to remove the following {0}?")
    String removeConfirmationPopupMessage(String what);

    // Common error messages

    @DefaultMessage("Error while loading data from server: {0}")
    String asyncCallFailure(String reason);

    // UiCommon related error messages

    @DefaultMessage("Error: {0}")
    String uiCommonFrontendFailure(String reason);

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

    @DefaultMessage("out of {0} VMs in pool")
    String outOfXVMsInPool(String numOfVms);

    @DefaultMessage("Number of Prestarted VMs defines the number of VMs in Run state , that are waiting to be attached to Users. Accepted values: 0 to the Number of VMs that already exists in the Pool.")
    String prestartedHelp();

    @DefaultMessage("Free: {0} vCPU")
    String quotaFreeCpus(int numOfVCPU);

    @DefaultMessage("{0} addresses")
    String addressesVmGuestAgent(int numOfAddresses);
}
