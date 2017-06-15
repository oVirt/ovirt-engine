package org.ovirt.engine.ui.webadmin.section.main.view.popup.datacenter;

import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.datacenter.NewDataCenterNetworkPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.NewNetworkPopupView;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class NewDataCenterNetworkPopupView extends NewNetworkPopupView implements NewDataCenterNetworkPopupPresenterWidget.ViewDef {

    @Inject
    public NewDataCenterNetworkPopupView(EventBus eventBus) {
        super(eventBus);
    }

    @Override
    public void updateVisibility() {
        super.updateVisibility();
        dataCenterEditor.setVisible(false);
    }
}
