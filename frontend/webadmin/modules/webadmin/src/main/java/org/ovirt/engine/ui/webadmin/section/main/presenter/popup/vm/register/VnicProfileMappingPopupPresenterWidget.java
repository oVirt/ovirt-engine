package org.ovirt.engine.ui.webadmin.section.main.presenter.popup.vm.register;

import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.models.vms.register.VnicProfileMappingModel;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class VnicProfileMappingPopupPresenterWidget
        extends AbstractModelBoundPopupPresenterWidget<VnicProfileMappingModel, VnicProfileMappingPopupPresenterWidget.ViewDef> {

    public interface ViewDef extends AbstractModelBoundPopupPresenterWidget.ViewDef<VnicProfileMappingModel> {
    }

    @Inject
    public VnicProfileMappingPopupPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
    }

}
