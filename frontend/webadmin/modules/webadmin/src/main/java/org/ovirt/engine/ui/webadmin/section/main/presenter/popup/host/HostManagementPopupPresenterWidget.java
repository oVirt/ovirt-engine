package org.ovirt.engine.ui.webadmin.section.main.presenter.popup.host;

import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostManagementNetworkModel;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class HostManagementPopupPresenterWidget extends AbstractModelBoundPopupPresenterWidget<HostManagementNetworkModel, HostManagementPopupPresenterWidget.ViewDef> {

    public interface ViewDef extends AbstractModelBoundPopupPresenterWidget.ViewDef<HostManagementNetworkModel> {

    }

    @Inject
    public HostManagementPopupPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
    }

}
