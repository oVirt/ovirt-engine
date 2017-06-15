package org.ovirt.engine.ui.webadmin.section.main.presenter.popup.template;

import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmInterfaceModel;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class TemplateInterfacePopupPresenterWidget extends AbstractModelBoundPopupPresenterWidget<VmInterfaceModel, TemplateInterfacePopupPresenterWidget.ViewDef> {

    public interface ViewDef extends AbstractModelBoundPopupPresenterWidget.ViewDef<VmInterfaceModel> {
    }

    @Inject
    public TemplateInterfacePopupPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
    }
}
