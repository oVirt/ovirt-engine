package org.ovirt.engine.ui.webadmin.section.main.presenter.popup.datacenter;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class EditDataCenterNetworkPopupPresenterWidget extends EditNetworkPopupPresenterWidget{

    public interface ViewDef extends EditNetworkPopupPresenterWidget.ViewDef {
    }

    @Inject
    public EditDataCenterNetworkPopupPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
    }

}
