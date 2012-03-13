package org.ovirt.engine.ui.webadmin.section.main.view.popup.vm;

import org.ovirt.engine.ui.common.CommonApplicationResources;
import org.ovirt.engine.ui.common.view.popup.AbstractModelBoundWidgetPopupView;
import org.ovirt.engine.ui.common.widget.uicommon.popup.networkinterface.NetworkInterfacePopupWidget;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmInterfaceModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.vm.VmInterfacePopupPresenterWidget;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class VmInterfacePopupView extends AbstractModelBoundWidgetPopupView<VmInterfaceModel> implements VmInterfacePopupPresenterWidget.ViewDef {

    @Inject
    public VmInterfacePopupView(EventBus eventBus, CommonApplicationResources resources,ApplicationConstants constants) {
        super(eventBus,
                resources,
                new NetworkInterfacePopupWidget(eventBus, constants),
                "400px",
                "320px");
    }

}
