package org.ovirt.engine.ui.webadmin.section.main.presenter.popup.storage;

import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.models.storage.RegisterEntityModel;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class RegisterVmPopupPresenterWidget extends AbstractModelBoundPopupPresenterWidget<RegisterEntityModel, RegisterVmPopupPresenterWidget.ViewDef> {

    public interface ViewDef extends AbstractModelBoundPopupPresenterWidget.ViewDef<RegisterEntityModel> {
    }

    @Inject
    public RegisterVmPopupPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
    }

}
