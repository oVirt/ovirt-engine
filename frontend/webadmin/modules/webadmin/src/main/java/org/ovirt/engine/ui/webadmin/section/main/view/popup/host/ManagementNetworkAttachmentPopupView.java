package org.ovirt.engine.ui.webadmin.section.main.view.popup.host;

import org.ovirt.engine.ui.uicommonweb.models.hosts.NetworkAttachmentModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.host.ManagementNetworkAttachmentPopupPresenterWidget;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class ManagementNetworkAttachmentPopupView extends NetworkAttachmentPopupView implements ManagementNetworkAttachmentPopupPresenterWidget.ViewDef {

    @Inject
    public ManagementNetworkAttachmentPopupView(EventBus eventBus) {
        super(eventBus);
    }

    @Override
    public void edit(NetworkAttachmentModel model) {
        super.edit(model);
    }
}
