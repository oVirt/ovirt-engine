package org.ovirt.engine.ui.webadmin.section.main.view.popup.vm;

import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.view.popup.AbstractModelBoundWidgetPopupView;
import org.ovirt.engine.ui.common.widget.uicommon.popup.vm.CloneVmWidget;
import org.ovirt.engine.ui.uicommonweb.models.vms.CloneVmModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.vm.CloneVmPopupPresenterWidget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class CloneVmPopupView extends AbstractModelBoundWidgetPopupView<CloneVmModel> implements CloneVmPopupPresenterWidget.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<CloneVmPopupView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @Inject
    public CloneVmPopupView(EventBus eventBus) {
        super(eventBus, new CloneVmWidget(), "400px", "270px"); //$NON-NLS-1$ //$NON-NLS-2$
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }

}
