package org.ovirt.engine.ui.webadmin;

import org.ovirt.engine.ui.common.CommonApplicationMessages;

public interface ApplicationMessages extends CommonApplicationMessages {

    // Host alert messages (not that the <a> and </a> tags are used to indicate the
    // place where the link to actions should be introduced:
    @DefaultMessage("A new version is available; an upgrade option will appear once the Host is moved to maintenance mode.")
    String hostHasUpgradeAlert();

    @DefaultMessage("This host is in non responding state. Try to Activate it; If the problem persists, switch Host to Maintenance mode and try to reinstall it.")
    String hostHasReinstallAlertNonResponsive();

    @DefaultMessage("Host installation failed. Fix installation issues and try to <a>Re-Install</a>")
    String hostHasReinstallAlertInstallFailed();

    @DefaultMessage("Host is in maintenance mode, you can Activate it by pressing the Activate button. If you wish to upgrade or reinstall it click <a>here</a>.")
    String hostHasReinstallAlertMaintenance();

    @DefaultMessage("The Host network configuration is not saved. <a>Save</a>")
    String hostHasNICsAlert();

    @DefaultMessage("This host is in non responding state and has no power management configured. Please reboot manually.")
    String hostHasManualFenceAlert();

    @DefaultMessage("Power Management is not configured for this Host. <a>Enable Power Management</a>")
    String hostHasNoPowerManagementAlert();

    @DefaultMessage("Host Destination is disabled since you have selected Virtual Machines in several Clusters.")
    String migrateHostDisabledVMsInServerClusters();

    @DefaultMessage("Note that some of the selected VMs are already running on this Host and will not be migrated.")
    String migrateSomeVmsAlreadyRunningOnHost();

    @DefaultMessage("No available Host to migrate to.")
    String migrateNoAvailableHost();

    @DefaultMessage("Please make sure the Host ''{0}'' has been manually shut down or rebooted.")
    String manaulFencePopupMessageLabel(String hostName);

    @DefaultMessage("All references to objects that reside on Storage Domain {0} in the database will be removed. You may need to manually clean the storage in order to reuse it.")
    String storageDestroyPopupMessageLabel(String storageName);

    @DefaultMessage("Memory Page Sharing Threshold set to {0}. Allow VMs to run on the Host up to the overcommit threshold")
    String clusterPopupOptimizationForServerExplainationLabel(String a);

    @DefaultMessage("Memory Page Sharing Threshold set to {0}. Allow VMs to run on the Host up to the overcommit threshold")
    String clusterPopupOptimizationForDesktopExplainationLabel( String a);

    @DefaultMessage("Memory Page Sharing Threshold set to {0} via API/CLI")
    String clusterPopupOptimizationCustomExplainationLabel(String a);

    @DefaultMessage("The Network will be added to the Data Center {0} as well.")
    String theNetworkWillBeAddedToTheDataCenterAsWell(String dcName);
}
