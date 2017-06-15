package org.ovirt.engine.ui.webadmin.section.main.presenter.popup.host;

import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostSetupNetworksModel;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class HostSetupNetworksPopupPresenterWidget extends AbstractModelBoundPopupPresenterWidget<HostSetupNetworksModel, HostSetupNetworksPopupPresenterWidget.ViewDef> {

    public interface ViewDef extends AbstractModelBoundPopupPresenterWidget.ViewDef<HostSetupNetworksModel> {

    }

    @Inject
    public HostSetupNetworksPopupPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
    }

}
