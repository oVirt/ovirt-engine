package org.ovirt.engine.ui.common.uicommon;

import org.ovirt.engine.ui.common.CommonApplicationMessages;
import org.ovirt.engine.ui.frontend.FrontendFailureEventArgs;
import org.ovirt.engine.ui.uicommonweb.ErrorPopupManager;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;

import com.google.inject.Inject;

public class FrontendFailureEventListener implements IEventListener {

    private final ErrorPopupManager errorPopupManager;
    private final CommonApplicationMessages messages;

    @Inject
    public FrontendFailureEventListener(ErrorPopupManager errorPopupManager,
            CommonApplicationMessages messages) {
        this.errorPopupManager = errorPopupManager;
        this.messages = messages;
    }

    @Override
    public void eventRaised(Event ev, Object sender, EventArgs args) {
        FrontendFailureEventArgs failureArgs = (FrontendFailureEventArgs) args;

        if (failureArgs.getMessage() != null) {
            errorPopupManager.show(messages.uiCommonRunActionFailed(ErrorMessageFormatter.formatMessage(failureArgs.getMessage())));
        } else if (failureArgs.getMessages() != null) {
            errorPopupManager.show(messages.uiCommonRunActionFailed(ErrorMessageFormatter.formatMessages(failureArgs.getMessages())));
        }
    }

}
