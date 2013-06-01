package org.ovirt.engine.ui.webadmin.section.main.presenter.popup.provider;

import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.models.providers.DiscoverNetworksModel;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class DiscoverNetworkPopupPresenterWidget extends AbstractModelBoundPopupPresenterWidget<DiscoverNetworksModel, DiscoverNetworkPopupPresenterWidget.ViewDef> {

    public interface ViewDef extends AbstractModelBoundPopupPresenterWidget.ViewDef<DiscoverNetworksModel> {
    }

    @Inject
    public DiscoverNetworkPopupPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
    }

}
