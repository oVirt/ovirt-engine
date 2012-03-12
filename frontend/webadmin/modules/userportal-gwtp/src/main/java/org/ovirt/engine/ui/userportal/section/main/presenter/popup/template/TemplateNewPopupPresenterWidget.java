package org.ovirt.engine.ui.userportal.section.main.presenter.popup.template;

import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.models.vms.UnitVmModel;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class TemplateNewPopupPresenterWidget extends AbstractModelBoundPopupPresenterWidget<UnitVmModel, TemplateNewPopupPresenterWidget.ViewDef> {

    public interface ViewDef extends AbstractModelBoundPopupPresenterWidget.ViewDef<UnitVmModel> {
    }

    @Inject
    public TemplateNewPopupPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
    }

}
