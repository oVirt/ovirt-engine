package org.ovirt.engine.ui.webadmin.section.main.presenter.popup;

import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.NetworkQoSModel;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;


public class NetworkQoSPopupPresenterWidget extends AbstractModelBoundPopupPresenterWidget<NetworkQoSModel, NetworkQoSPopupPresenterWidget.ViewDef> {

    public interface ViewDef extends AbstractModelBoundPopupPresenterWidget.ViewDef<NetworkQoSModel> {
    }

    @Inject
    public NetworkQoSPopupPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
    }

}
