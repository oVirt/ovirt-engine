package org.ovirt.engine.ui.webadmin.section.main.presenter.popup.instancetypes;

import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.widget.popup.AbstractVmBasedPopupPresenterWidget;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class InstanceTypesPopupPresenterWidget extends AbstractVmBasedPopupPresenterWidget<InstanceTypesPopupPresenterWidget.ViewDef> {

    public interface ViewDef extends AbstractVmBasedPopupPresenterWidget.ViewDef {

    }

    @Inject
    public InstanceTypesPopupPresenterWidget(EventBus eventBus, ViewDef view, ClientStorage clientStorage) {
        super(eventBus, view, clientStorage);
    }
}
