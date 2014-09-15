package org.ovirt.engine.ui.webadmin.section.main.view.popup.vm;

import org.ovirt.engine.ui.common.CommonApplicationResources;
import org.ovirt.engine.ui.common.CommonApplicationTemplates;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.view.popup.AbstractModelBoundWidgetPopupView;
import org.ovirt.engine.ui.common.widget.uicommon.popup.vm.VmNextRunConfigurationWidget;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmNextRunConfigurationModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationMessages;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.vm.VmNextRunConfigurationPresenterWidget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class VmNextRunConfigurationPopupView extends AbstractModelBoundWidgetPopupView<VmNextRunConfigurationModel>
        implements VmNextRunConfigurationPresenterWidget.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<VmNextRunConfigurationPopupView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @Inject
    public VmNextRunConfigurationPopupView(EventBus eventBus, CommonApplicationResources resources,
            ApplicationConstants constants, ApplicationMessages messages, CommonApplicationTemplates templates) {
        super(eventBus, resources, new VmNextRunConfigurationWidget(constants, messages, templates), "400px", "300px"); //$NON-NLS-1$ //$NON-NLS-2$
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }
}
