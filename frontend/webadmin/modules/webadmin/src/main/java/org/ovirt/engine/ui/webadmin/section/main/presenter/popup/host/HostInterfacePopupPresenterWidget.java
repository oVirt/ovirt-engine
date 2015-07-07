package org.ovirt.engine.ui.webadmin.section.main.presenter.popup.host;

import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostInterfaceModel;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class HostInterfacePopupPresenterWidget extends AbstractModelBoundPopupPresenterWidget<HostInterfaceModel, HostInterfacePopupPresenterWidget.ViewDef> {

    public interface ViewDef extends AbstractModelBoundPopupPresenterWidget.ViewDef<HostInterfaceModel> {
    }

    @Inject
    public HostInterfacePopupPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
    }

}
