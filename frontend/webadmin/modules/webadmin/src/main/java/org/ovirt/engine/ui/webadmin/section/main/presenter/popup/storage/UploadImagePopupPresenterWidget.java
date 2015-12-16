package org.ovirt.engine.ui.webadmin.section.main.presenter.popup.storage;

import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.models.storage.UploadImageModel;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class UploadImagePopupPresenterWidget extends AbstractModelBoundPopupPresenterWidget<UploadImageModel, UploadImagePopupPresenterWidget.ViewDef> {

    public interface ViewDef extends AbstractModelBoundPopupPresenterWidget.ViewDef<UploadImageModel> {
    }

    @Inject
    public UploadImagePopupPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
    }
}
