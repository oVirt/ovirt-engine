package org.ovirt.engine.ui.webadmin.section.main.view.popup.instancetypes;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import org.ovirt.engine.ui.common.CommonApplicationMessages;
import org.ovirt.engine.ui.common.CommonApplicationTemplates;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.view.popup.AbstractVmPopupView;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.instancetypes.InstanceTypesPopupPresenterWidget;
import org.ovirt.engine.ui.common.widget.uicommon.popup.instancetypes.InstanceTypesPopupWidget;

public class InstanceTypesPopupView extends AbstractVmPopupView implements InstanceTypesPopupPresenterWidget.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<InstanceTypesPopupView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @Inject
    public InstanceTypesPopupView(EventBus eventBus, ApplicationResources resources, ApplicationConstants constants, CommonApplicationMessages messages, CommonApplicationTemplates templates) {
        super(eventBus, resources, new InstanceTypesPopupWidget(constants, resources, messages, templates, eventBus), "710px", "570px"); //$NON-NLS-1$ //$NON-NLS-2$
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }
}
