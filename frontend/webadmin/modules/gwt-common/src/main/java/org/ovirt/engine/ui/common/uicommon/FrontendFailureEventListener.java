package org.ovirt.engine.ui.common.uicommon;

import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.IEventListener;
import org.ovirt.engine.ui.common.CommonApplicationMessages;
import org.ovirt.engine.ui.common.system.ErrorPopupManager;
import org.ovirt.engine.ui.frontend.FrontendFailureEventArgs;

import com.google.gwt.core.client.GWT;
import com.google.inject.Inject;

public class FrontendFailureEventListener implements IEventListener {
    private final ErrorPopupManager errorPopupManager;
    private static final CommonApplicationMessages MESSAGES = GWT.create(CommonApplicationMessages.class);

    @Inject
    public FrontendFailureEventListener(ErrorPopupManager errorPopupManager) {
        this.errorPopupManager = errorPopupManager;
    }

    @Override
    public void eventRaised(Event ev, Object sender, EventArgs args) {
        FrontendFailureEventArgs failureArgs = (FrontendFailureEventArgs) args;

        if (failureArgs.getMessage() != null) {
            errorPopupManager.show(MESSAGES.uiCommonFrontendFailure(ErrorMessageFormatter.formatMessage(failureArgs.getMessage())));
        } else if (failureArgs.getMessages() != null) {
            errorPopupManager.show(MESSAGES.uiCommonFrontendFailure(ErrorMessageFormatter.formatMessages(failureArgs.getMessages())));
        }
    }
}
