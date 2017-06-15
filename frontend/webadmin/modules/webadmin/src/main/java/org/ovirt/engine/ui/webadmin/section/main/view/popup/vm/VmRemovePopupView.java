package org.ovirt.engine.ui.webadmin.section.main.view.popup.vm;

import org.ovirt.engine.ui.common.view.popup.AbstractVmRemoveConfimationPopup;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class VmRemovePopupView extends AbstractVmRemoveConfimationPopup implements VmRemovePopupPresenterWidget.ViewDef {

    @Inject
    public VmRemovePopupView(EventBus eventBus) {
        super(eventBus);
    }
}
