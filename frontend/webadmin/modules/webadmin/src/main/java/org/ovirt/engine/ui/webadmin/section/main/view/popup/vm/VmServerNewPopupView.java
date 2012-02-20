package org.ovirt.engine.ui.webadmin.section.main.view.popup.vm;

import org.ovirt.engine.ui.common.widget.uicommon.popup.vm.VmServerNewPopupWidget;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.vm.VmServerNewPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.AbstractVmPopupView;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class VmServerNewPopupView extends AbstractVmPopupView implements VmServerNewPopupPresenterWidget.ViewDef {

    @Inject
    public VmServerNewPopupView(EventBus eventBus, ApplicationResources resources, ApplicationConstants constants) {
        super(eventBus, resources, new VmServerNewPopupWidget(constants));
    }

}
