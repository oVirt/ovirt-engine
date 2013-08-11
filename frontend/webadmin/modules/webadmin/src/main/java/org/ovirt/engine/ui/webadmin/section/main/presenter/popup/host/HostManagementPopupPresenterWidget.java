package org.ovirt.engine.ui.webadmin.section.main.presenter.popup.host;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class HostManagementPopupPresenterWidget extends HostInterfacePopupPresenterWidget {

    public interface ViewDef extends HostInterfacePopupPresenterWidget.ViewDef {
    }

    @Inject
    public HostManagementPopupPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
    }

}
