package org.ovirt.engine.ui.userportal.section.main.view.popup.vm;

import com.google.gwt.event.shared.EventBus;
import org.ovirt.engine.ui.common.view.popup.AbstractModelBoundWidgetPopupView;
import org.ovirt.engine.ui.common.widget.uicommon.popup.vm.VmDiskAttachPopupWidget;
import org.ovirt.engine.ui.uicommonweb.models.vms.AttachDiskModel;
import org.ovirt.engine.ui.userportal.ApplicationConstants;
import org.ovirt.engine.ui.userportal.ApplicationResources;
import org.ovirt.engine.ui.userportal.ApplicationTemplates;

public class BaseVmDiskAttachPopupView extends AbstractModelBoundWidgetPopupView<AttachDiskModel> {

    public BaseVmDiskAttachPopupView(EventBus eventBus, ApplicationResources resources, ApplicationConstants constants, ApplicationTemplates templates, boolean allowMultipleSelection) {
        super(eventBus, resources, new VmDiskAttachPopupWidget(constants, resources, templates, false, allowMultipleSelection), "815px", "615px"); //$NON-NLS-1$ //$NON-NLS-2$
        asWidget().enableResizeSupport(true);
    }
}
