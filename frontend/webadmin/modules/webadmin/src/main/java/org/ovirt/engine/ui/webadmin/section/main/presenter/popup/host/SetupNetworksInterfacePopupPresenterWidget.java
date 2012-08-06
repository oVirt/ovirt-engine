package org.ovirt.engine.ui.webadmin.section.main.presenter.popup.host;

import org.ovirt.engine.ui.uicommonweb.models.hosts.HostInterfaceModel;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class SetupNetworksInterfacePopupPresenterWidget extends HostInterfacePopupPresenterWidget{

    public interface ViewDef extends HostInterfacePopupPresenterWidget.ViewDef {
    }

    @Inject
    public SetupNetworksInterfacePopupPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
    }

    @Override
    public void init(final HostInterfaceModel model) {
        super.init(model);
    }

}
