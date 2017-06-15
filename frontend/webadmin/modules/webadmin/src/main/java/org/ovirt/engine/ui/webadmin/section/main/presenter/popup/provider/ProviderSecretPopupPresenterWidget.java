package org.ovirt.engine.ui.webadmin.section.main.presenter.popup.provider;

import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.models.storage.LibvirtSecretModel;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class ProviderSecretPopupPresenterWidget extends AbstractModelBoundPopupPresenterWidget<LibvirtSecretModel, ProviderSecretPopupPresenterWidget.ViewDef> {

    public interface ViewDef extends AbstractModelBoundPopupPresenterWidget.ViewDef<LibvirtSecretModel> {
    }

    @Inject
    public ProviderSecretPopupPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
    }

}
