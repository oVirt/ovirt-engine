package org.ovirt.engine.ui.webadmin.section.main.view.popup.vm;

import org.ovirt.engine.ui.common.view.popup.AbstractModelBoundWidgetPopupView;
import org.ovirt.engine.ui.common.widget.uicommon.popup.vm.VmDiskAttachPopupWidget;
import org.ovirt.engine.ui.uicommonweb.models.vms.AttachDiskModel;

import com.google.gwt.event.shared.EventBus;

public class BaseVmDiskAttachPopupView extends AbstractModelBoundWidgetPopupView<AttachDiskModel> {

    public BaseVmDiskAttachPopupView(EventBus eventBus, boolean allowMultipleSelection) {
        super(eventBus, new VmDiskAttachPopupWidget(true, allowMultipleSelection), "865px", "630px"); //$NON-NLS-1$ //$NON-NLS-2$
    }
}
