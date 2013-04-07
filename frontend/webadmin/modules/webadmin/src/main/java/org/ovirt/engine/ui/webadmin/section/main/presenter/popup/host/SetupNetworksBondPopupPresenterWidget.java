package org.ovirt.engine.ui.webadmin.section.main.presenter.popup.host;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class SetupNetworksBondPopupPresenterWidget extends HostBondPopupPresenterWidget {

    public interface ViewDef extends HostBondPopupPresenterWidget.ViewDef {

    }

    @Inject
    public SetupNetworksBondPopupPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
    }

}
