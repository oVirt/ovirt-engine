package org.ovirt.engine.ui.webadmin.section.main.presenter.popup.provider;

import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.models.networks.ImportNetworksModel;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class ImportNetworksPopupPresenterWidget extends AbstractModelBoundPopupPresenterWidget<ImportNetworksModel, ImportNetworksPopupPresenterWidget.ViewDef> {

    public interface ViewDef extends AbstractModelBoundPopupPresenterWidget.ViewDef<ImportNetworksModel> {
    }

    @Inject
    public ImportNetworksPopupPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
    }

}
