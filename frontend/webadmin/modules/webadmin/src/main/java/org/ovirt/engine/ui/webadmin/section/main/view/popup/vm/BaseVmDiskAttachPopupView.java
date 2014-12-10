package org.ovirt.engine.ui.webadmin.section.main.view.popup.vm;

import com.google.gwt.event.shared.EventBus;
import org.ovirt.engine.ui.common.view.popup.AbstractModelBoundWidgetPopupView;
import org.ovirt.engine.ui.common.widget.uicommon.popup.vm.VmDiskAttachPopupWidget;
import org.ovirt.engine.ui.uicommonweb.models.vms.AttachDiskModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.ApplicationTemplates;

public class BaseVmDiskAttachPopupView extends AbstractModelBoundWidgetPopupView<AttachDiskModel> {

    public BaseVmDiskAttachPopupView(EventBus eventBus, ApplicationResources resources, ApplicationConstants constants, ApplicationTemplates templates, boolean allowMultipleSelection) {
        super(eventBus, resources, new VmDiskAttachPopupWidget(constants, resources, templates, true, allowMultipleSelection), "815px", "615px"); //$NON-NLS-1$ //$NON-NLS-2$
        asWidget().enableResizeSupport(true);
    }
}
