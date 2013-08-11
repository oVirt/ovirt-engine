package org.ovirt.engine.ui.webadmin.section.main.presenter.popup.host;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class SetupNetworksManagementPopupPresenterWidget extends SetupNetworksInterfacePopupPresenterWidget {

    public interface ViewDef extends SetupNetworksInterfacePopupPresenterWidget.ViewDef {
    }

    @Inject
    public SetupNetworksManagementPopupPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
    }

}
