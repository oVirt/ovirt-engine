package org.ovirt.engine.ui.webadmin.section.main.presenter.popup.vm;

import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.models.vms.RunOnceModel;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class VmRunOncePopupPresenterWidget extends AbstractModelBoundPopupPresenterWidget<RunOnceModel, VmRunOncePopupPresenterWidget.ViewDef> {

    public interface ViewDef extends AbstractModelBoundPopupPresenterWidget.ViewDef<RunOnceModel> {
    }

    @Inject
    public VmRunOncePopupPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
    }

}
