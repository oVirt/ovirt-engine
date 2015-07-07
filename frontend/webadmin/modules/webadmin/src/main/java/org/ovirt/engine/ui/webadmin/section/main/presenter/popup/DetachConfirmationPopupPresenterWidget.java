package org.ovirt.engine.ui.webadmin.section.main.presenter.popup;

import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostInterfaceModel;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class DetachConfirmationPopupPresenterWidget extends AbstractModelBoundPopupPresenterWidget<HostInterfaceModel, DetachConfirmationPopupPresenterWidget.ViewDef> {

    public interface ViewDef extends AbstractModelBoundPopupPresenterWidget.ViewDef<HostInterfaceModel> {
    }

    @Inject
    public DetachConfirmationPopupPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
    }

}
