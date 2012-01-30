package org.ovirt.engine.ui.webadmin.section.main.presenter.popup.qouta;

import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.models.qouta.EditQuotaStorageModel;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class EditQuotaStoragePopupPresenterWidget extends AbstractModelBoundPopupPresenterWidget<EditQuotaStorageModel, EditQuotaStoragePopupPresenterWidget.ViewDef> {

    public interface ViewDef extends AbstractModelBoundPopupPresenterWidget.ViewDef<EditQuotaStorageModel> {
    }

    @Inject
    public EditQuotaStoragePopupPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
    }

}
