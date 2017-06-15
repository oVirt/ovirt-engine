package org.ovirt.engine.ui.webadmin.section.main.presenter.popup.vm;

import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.models.vms.CloneVmModel;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class CloneVmPopupPresenterWidget extends AbstractModelBoundPopupPresenterWidget<CloneVmModel, CloneVmPopupPresenterWidget.ViewDef> {

    public interface ViewDef extends AbstractModelBoundPopupPresenterWidget.ViewDef<CloneVmModel> {
    }

    @Inject
    public CloneVmPopupPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
    }

}
