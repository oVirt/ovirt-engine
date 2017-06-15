package org.ovirt.engine.ui.webadmin.section.main.presenter.popup.provider;

import java.util.List;

import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.models.networks.BaseImportNetworksModel;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class ImportNetworksPopupPresenterWidget extends AbstractModelBoundPopupPresenterWidget<BaseImportNetworksModel, ImportNetworksPopupPresenterWidget.ViewDef> {

    public interface ViewDef extends AbstractModelBoundPopupPresenterWidget.ViewDef<BaseImportNetworksModel> {
        void validateImportedNetworks(List<String> errors);
    }

    @Inject
    public ImportNetworksPopupPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
    }

    @Override
    public void init(final BaseImportNetworksModel model) {
        super.init(model);
        model.getErrors().getItemsChangedEvent().addListener((ev, sender, args) ->
                getView().validateImportedNetworks((List<String>) model.getErrors().getItems()));
    }

}
