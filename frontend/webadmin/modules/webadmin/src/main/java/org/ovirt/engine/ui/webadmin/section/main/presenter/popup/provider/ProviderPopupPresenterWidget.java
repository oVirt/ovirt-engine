package org.ovirt.engine.ui.webadmin.section.main.presenter.popup.provider;

import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.models.providers.ProviderModel;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class ProviderPopupPresenterWidget extends AbstractModelBoundPopupPresenterWidget<ProviderModel, ProviderPopupPresenterWidget.ViewDef> {

    public interface ViewDef extends AbstractModelBoundPopupPresenterWidget.ViewDef<ProviderModel> {
    }

    @Inject
    public ProviderPopupPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
    }

}
