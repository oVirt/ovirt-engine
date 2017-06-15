package org.ovirt.engine.ui.webadmin.section.main.presenter.popup.vm;

import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.models.vms.VncInfoModel;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class VncInfoPopupPresenterWidget extends AbstractModelBoundPopupPresenterWidget<VncInfoModel, VncInfoPopupPresenterWidget.ViewDef> {
    public interface ViewDef extends AbstractModelBoundPopupPresenterWidget.ViewDef<VncInfoModel> {

    }

    @Inject
    public VncInfoPopupPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
    }
}
