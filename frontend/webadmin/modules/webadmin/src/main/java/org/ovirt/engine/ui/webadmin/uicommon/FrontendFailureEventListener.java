package org.ovirt.engine.ui.webadmin.uicommon;

import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.IEventListener;
import org.ovirt.engine.ui.frontend.FrontendFailureEventArgs;
import org.ovirt.engine.ui.webadmin.ApplicationMessages;
import org.ovirt.engine.ui.webadmin.system.ErrorPopupManager;

import com.google.inject.Inject;

public class FrontendFailureEventListener implements IEventListener {

    private final ErrorPopupManager errorPopupManager;
    private final ApplicationMessages messages;

    @Inject
    public FrontendFailureEventListener(ErrorPopupManager errorPopupManager, ApplicationMessages messages) {
        this.errorPopupManager = errorPopupManager;
        this.messages = messages;
    }

    @Override
    public void eventRaised(Event ev, Object sender, EventArgs args) {
        FrontendFailureEventArgs failureArgs = (FrontendFailureEventArgs) args;

        if (failureArgs.getMessage() != null) {
            errorPopupManager.show(
                    messages.uiCommonFrontendFailure(ErrorMessageFormatter.formatMessage(failureArgs.getMessage())));
        } else if (failureArgs.getMessages() != null) {
            errorPopupManager.show(
                    messages.uiCommonFrontendFailure(ErrorMessageFormatter.formatMessages(failureArgs.getMessages())));
        }
    }

}
