package org.ovirt.engine.ui.webadmin.section.main.view.popup.vm;

import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.view.popup.AbstractModelBoundWidgetPopupView;
import org.ovirt.engine.ui.common.widget.uicommon.popup.vm.VmChangeCDPopupWidget;
import org.ovirt.engine.ui.uicommonweb.models.vms.AttachCdModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.vm.VmChangeCDPopupPresenterWidget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class VmChangeCDPopupView extends AbstractModelBoundWidgetPopupView<AttachCdModel> implements VmChangeCDPopupPresenterWidget.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<VmChangeCDPopupView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @Inject
    public VmChangeCDPopupView(EventBus eventBus) {
        super(eventBus, new VmChangeCDPopupWidget(), "400px", "170px"); //$NON-NLS-1$ //$NON-NLS-2$
        setNoScroll(true);
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }

}
