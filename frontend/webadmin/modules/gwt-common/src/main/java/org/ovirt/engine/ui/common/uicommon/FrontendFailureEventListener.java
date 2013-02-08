package org.ovirt.engine.ui.common.uicommon;

import org.ovirt.engine.ui.common.CommonApplicationMessages;
import org.ovirt.engine.ui.common.system.ErrorPopupManager;
import org.ovirt.engine.ui.frontend.FrontendFailureEventArgs;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;

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
            errorPopupManager.show(MESSAGES.uiCommonRunActionFailed(ErrorMessageFormatter.formatMessage(failureArgs.getMessage())));
        } else if (failureArgs.getMessages() != null) {
            errorPopupManager.show(MESSAGES.uiCommonRunActionFailed(ErrorMessageFormatter.formatMessages(failureArgs.getMessages())));
        }
    }

    public void hide() {
        errorPopupManager.hide();
    }
}
