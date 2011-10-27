package org.ovirt.engine.ui.webadmin;

import com.google.gwt.i18n.client.Messages;

public interface ApplicationMessages extends Messages {

    // Common stuff

    @DefaultMessage("Error while loading data from server: {0}")
    String asyncCallFailure(String reason);

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

    // Main section

    @DefaultMessage("Are you sure you want to remove the following {0}?")
    String removeConfirmationPopupMessage(String what);

    @DefaultMessage("Browser {0} version {1} is currently not supported.")
    String browserNotSupportedVersion(String browser, String version);

    // Host alert messages (not that the <a> and </a> tags are used to indicate the
    // place where the link to actions should be introduced:
    @DefaultMessage("A new RHEV-Hypervisor version is available, an upgrade option will appear once the host is moved to maintenance mode.")
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
}
