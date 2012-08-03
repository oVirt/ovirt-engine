package org.ovirt.engine.ui.userportal.section.main.view.popup.vm;

import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.CommonApplicationMessages;
import org.ovirt.engine.ui.common.CommonApplicationResources;
import org.ovirt.engine.ui.common.view.popup.AbstractDiskRemoveConfirmationPopupView;
import org.ovirt.engine.ui.userportal.section.main.presenter.popup.vm.VmDiskRemovePopupPresenterWidget;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class VmDiskRemovePopupView extends AbstractDiskRemoveConfirmationPopupView implements VmDiskRemovePopupPresenterWidget.ViewDef {

    @Inject
    public VmDiskRemovePopupView(EventBus eventBus,
            CommonApplicationResources resources,
            CommonApplicationMessages messages,
            CommonApplicationConstants constants) {
        super(eventBus, resources, messages, constants);
    }

    @Override
    protected void localize(CommonApplicationConstants constants) {
        latch.setLabel(constants.permanentlyRemoveLabel());
    }

}
