package org.ovirt.engine.ui.common.uicommon;

import org.ovirt.engine.ui.common.CommonApplicationMessages;
import org.ovirt.engine.ui.common.gin.AssetProvider;
import org.ovirt.engine.ui.frontend.FrontendFailureEventArgs;
import org.ovirt.engine.ui.uicommonweb.ErrorPopupManager;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.IEventListener;

import com.google.inject.Inject;

public class FrontendFailureEventListener implements IEventListener<FrontendFailureEventArgs> {

    private final ErrorPopupManager errorPopupManager;
    private static final CommonApplicationMessages messages = AssetProvider.getMessages();

    @Inject
    public FrontendFailureEventListener(ErrorPopupManager errorPopupManager) {
        this.errorPopupManager = errorPopupManager;
    }

    @Override
    public void eventRaised(Event<? extends FrontendFailureEventArgs> ev, Object sender, FrontendFailureEventArgs args) {
        errorPopupManager.show(messages.uiCommonRunActionFailed(ErrorMessageFormatter.formatMessages(args.getMessages())));
    }

}
