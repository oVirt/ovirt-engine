package org.ovirt.engine.ui.webadmin.section.main.presenter.popup.storage;

import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.models.storage.RegisterTemplateModel;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class RegisterTemplatePopupPresenterWidget extends AbstractModelBoundPopupPresenterWidget<RegisterTemplateModel, RegisterTemplatePopupPresenterWidget.ViewDef> {

    public interface ViewDef extends AbstractModelBoundPopupPresenterWidget.ViewDef<RegisterTemplateModel> {
    }

    @Inject
    public RegisterTemplatePopupPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
    }

}
