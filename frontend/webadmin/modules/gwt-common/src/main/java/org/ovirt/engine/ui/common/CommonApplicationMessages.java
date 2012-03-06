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

}
