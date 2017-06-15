package org.ovirt.engine.ui.webadmin.section.main.view.popup.vm;

import org.ovirt.engine.ui.common.view.popup.AbstractDiskRemoveConfirmationPopupView;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.vm.VmDiskRemovePopupPresenterWidget;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class VmDiskRemovePopupView extends AbstractDiskRemoveConfirmationPopupView implements VmDiskRemovePopupPresenterWidget.ViewDef {

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public VmDiskRemovePopupView(EventBus eventBus) {
        super(eventBus);
    }

    @Override
    protected void localize() {
        latch.setLabel(constants.permanentlyRemoveLabel());
    }

}
