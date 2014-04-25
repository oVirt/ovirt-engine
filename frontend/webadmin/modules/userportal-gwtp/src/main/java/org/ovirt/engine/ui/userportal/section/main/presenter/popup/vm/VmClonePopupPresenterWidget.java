package org.ovirt.engine.ui.userportal.section.main.presenter.popup.vm;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.widget.popup.AbstractVmBasedPopupPresenterWidget;

public class VmClonePopupPresenterWidget extends AbstractVmBasedPopupPresenterWidget<VmClonePopupPresenterWidget.ViewDef> {
    public interface ViewDef extends AbstractVmBasedPopupPresenterWidget.ViewDef {

    }

    @Inject
    public VmClonePopupPresenterWidget(EventBus eventBus, ViewDef view, ClientStorage clientStorage) {
        super(eventBus, view, clientStorage);
    }
}
