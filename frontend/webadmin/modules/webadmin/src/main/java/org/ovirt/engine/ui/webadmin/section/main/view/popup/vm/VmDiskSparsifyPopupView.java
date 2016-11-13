package org.ovirt.engine.ui.webadmin.section.main.view.popup.vm;

import org.ovirt.engine.ui.common.view.popup.AbstractDiskRemoveConfirmationPopupView;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.vm.VmDiskSparsifyPopupPresenterWidget;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class VmDiskSparsifyPopupView extends AbstractDiskRemoveConfirmationPopupView implements VmDiskSparsifyPopupPresenterWidget.ViewDef {

    @Inject
    public VmDiskSparsifyPopupView(EventBus eventBus) {
        super(eventBus);
    }

}
