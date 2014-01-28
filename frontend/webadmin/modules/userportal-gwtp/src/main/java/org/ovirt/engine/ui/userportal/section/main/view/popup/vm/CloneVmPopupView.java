package org.ovirt.engine.ui.userportal.section.main.view.popup.vm;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.view.popup.AbstractModelBoundWidgetPopupView;
import org.ovirt.engine.ui.common.widget.uicommon.popup.vm.CloneVmWidget;
import org.ovirt.engine.ui.uicommonweb.models.vms.CloneVmModel;
import org.ovirt.engine.ui.userportal.ApplicationResources;
import org.ovirt.engine.ui.userportal.section.main.presenter.popup.vm.CloneVmPopupPresenterWidget;

public class CloneVmPopupView extends AbstractModelBoundWidgetPopupView<CloneVmModel> implements CloneVmPopupPresenterWidget.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<CloneVmPopupView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @Inject
    public CloneVmPopupView(EventBus eventBus, ApplicationResources resources, CommonApplicationConstants constants) {
        super(eventBus, resources, new CloneVmWidget(constants), "400px", "170px"); //$NON-NLS-1$ //$NON-NLS-2$
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }

}
