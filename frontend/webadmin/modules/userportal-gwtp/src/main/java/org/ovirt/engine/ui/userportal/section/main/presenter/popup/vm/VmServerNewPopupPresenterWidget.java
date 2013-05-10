package org.ovirt.engine.ui.userportal.section.main.presenter.popup.vm;

import org.ovirt.engine.ui.common.widget.popup.AbstractVmBasedPopupPresenterWidget;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class VmServerNewPopupPresenterWidget extends AbstractVmBasedPopupPresenterWidget<VmServerNewPopupPresenterWidget.ViewDef> {

    public interface ViewDef extends AbstractVmBasedPopupPresenterWidget.ViewDef {
    }

    @Inject
    public VmServerNewPopupPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
    }

}
