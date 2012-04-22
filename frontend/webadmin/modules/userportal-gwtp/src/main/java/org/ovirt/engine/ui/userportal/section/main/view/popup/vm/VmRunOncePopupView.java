package org.ovirt.engine.ui.userportal.section.main.view.popup.vm;

import org.ovirt.engine.ui.common.view.popup.AbstractModelBoundWidgetPopupView;
import org.ovirt.engine.ui.common.widget.uicommon.popup.vm.VmRunOncePopupWidget;
import org.ovirt.engine.ui.uicommonweb.models.vms.RunOnceModel;
import org.ovirt.engine.ui.userportal.ApplicationConstants;
import org.ovirt.engine.ui.userportal.ApplicationResources;
import org.ovirt.engine.ui.userportal.section.main.presenter.popup.vm.VmRunOncePopupPresenterWidget;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class VmRunOncePopupView extends AbstractModelBoundWidgetPopupView<RunOnceModel> implements VmRunOncePopupPresenterWidget.ViewDef {

    @Inject
    public VmRunOncePopupView(EventBus eventBus, ApplicationResources resources, ApplicationConstants constants) {
        super(eventBus, resources, new VmRunOncePopupWidget(constants), "420px", "640px"); //$NON-NLS-1$ //$NON-NLS-2$
    }

}
