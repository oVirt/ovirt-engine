package org.ovirt.engine.ui.webadmin.section.main.presenter.popup.datacenter;

import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class RecoveryStoragePopupPresenterWidget extends AbstractModelBoundPopupPresenterWidget<ConfirmationModel, RecoveryStoragePopupPresenterWidget.ViewDef> {

    public interface ViewDef extends AbstractModelBoundPopupPresenterWidget.ViewDef<ConfirmationModel> {
    }

    @Inject
    public RecoveryStoragePopupPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
    }

}
