package org.ovirt.engine.ui.webadmin.section.main.presenter.popup.host;

import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.models.hosts.NetworkAttachmentModel;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class NetworkAttachmentPopupPresenterWidget extends AbstractModelBoundPopupPresenterWidget<NetworkAttachmentModel, NetworkAttachmentPopupPresenterWidget.ViewDef> {

    public interface ViewDef extends AbstractModelBoundPopupPresenterWidget.ViewDef<NetworkAttachmentModel> {
    }

    @Inject
    public NetworkAttachmentPopupPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
    }

}
