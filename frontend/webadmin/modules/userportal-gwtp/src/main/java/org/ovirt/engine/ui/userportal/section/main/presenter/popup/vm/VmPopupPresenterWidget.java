package org.ovirt.engine.ui.userportal.section.main.presenter.popup.vm;

import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.widget.popup.AbstractVmBasedPopupPresenterWidget;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class VmPopupPresenterWidget extends AbstractVmBasedPopupPresenterWidget<VmPopupPresenterWidget.ViewDef> {

    public interface ViewDef extends AbstractVmBasedPopupPresenterWidget.ViewDef {
    }

    @Inject
    public VmPopupPresenterWidget(EventBus eventBus, ViewDef view, ClientStorage clientStorage) {
        super(eventBus, view, clientStorage);
    }

}
