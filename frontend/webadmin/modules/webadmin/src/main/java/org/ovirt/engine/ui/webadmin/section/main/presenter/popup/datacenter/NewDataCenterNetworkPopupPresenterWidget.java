package org.ovirt.engine.ui.webadmin.section.main.presenter.popup.datacenter;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class NewDataCenterNetworkPopupPresenterWidget extends NewNetworkPopupPresenterWidget{

    public interface ViewDef extends NewNetworkPopupPresenterWidget.ViewDef {
    }

    @Inject
    public NewDataCenterNetworkPopupPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
    }

}
