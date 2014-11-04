package org.ovirt.engine.ui.userportal.section.main.view.popup.vm;

import org.ovirt.engine.ui.common.CommonApplicationTemplates;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.view.popup.AbstractModelBoundWidgetPopupView;
import org.ovirt.engine.ui.common.widget.uicommon.popup.vm.VmMakeTemplatePopupWidget;
import org.ovirt.engine.ui.uicommonweb.models.vms.UnitVmModel;
import org.ovirt.engine.ui.userportal.ApplicationConstants;
import org.ovirt.engine.ui.userportal.ApplicationResources;
import org.ovirt.engine.ui.userportal.section.main.presenter.popup.vm.VmMakeTemplatePopupPresenterWidget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class VmMakeTemplatePopupView extends AbstractModelBoundWidgetPopupView<UnitVmModel> implements VmMakeTemplatePopupPresenterWidget.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<VmMakeTemplatePopupView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @Inject
    public VmMakeTemplatePopupView(EventBus eventBus, ApplicationResources resources, ApplicationConstants constants, CommonApplicationTemplates applicationTemplates) {
        super(eventBus, resources, new VmMakeTemplatePopupWidget(constants, applicationTemplates), "490px", "580px"); //$NON-NLS-1$ //$NON-NLS-2$
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }

}
