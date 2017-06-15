package org.ovirt.engine.ui.webadmin.section.main.presenter.popup.datacenter;

import org.ovirt.engine.ui.uicommonweb.models.datacenters.EditNetworkModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.AbstractNetworkPopupPresenterWidget;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class EditNetworkPopupPresenterWidget extends AbstractNetworkPopupPresenterWidget<EditNetworkModel, EditNetworkPopupPresenterWidget.ViewDef> {

    public interface ViewDef extends AbstractNetworkPopupPresenterWidget.ViewDef<EditNetworkModel> {
    }

    @Inject
    public EditNetworkPopupPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
    }
}
