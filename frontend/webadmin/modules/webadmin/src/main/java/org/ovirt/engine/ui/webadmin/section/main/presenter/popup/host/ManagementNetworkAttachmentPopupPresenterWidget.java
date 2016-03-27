package org.ovirt.engine.ui.webadmin.section.main.presenter.popup.host;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class ManagementNetworkAttachmentPopupPresenterWidget extends NetworkAttachmentPopupPresenterWidget {

    public interface ViewDef extends NetworkAttachmentPopupPresenterWidget.ViewDef {
    }

    @Inject
    public ManagementNetworkAttachmentPopupPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
    }

}
