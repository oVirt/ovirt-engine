package org.ovirt.engine.ui.webadmin.section.main.presenter.popup.host;

import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostBondInterfaceModel;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class HostBondPopupPresenterWidget extends AbstractModelBoundPopupPresenterWidget<HostBondInterfaceModel, HostBondPopupPresenterWidget.ViewDef> {

    public interface ViewDef extends AbstractModelBoundPopupPresenterWidget.ViewDef<HostBondInterfaceModel> {

    }

    @Inject
    public HostBondPopupPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
    }

}
