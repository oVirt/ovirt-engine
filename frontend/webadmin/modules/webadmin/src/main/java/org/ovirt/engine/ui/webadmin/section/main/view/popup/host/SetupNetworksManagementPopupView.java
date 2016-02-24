package org.ovirt.engine.ui.webadmin.section.main.view.popup.host;

import org.ovirt.engine.ui.uicommonweb.models.hosts.HostInterfaceModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostManagementNetworkModel;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.host.SetupNetworksManagementPopupPresenterWidget;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class SetupNetworksManagementPopupView extends SetupNetworksInterfacePopupView implements SetupNetworksManagementPopupPresenterWidget.ViewDef {

    @Inject
    public SetupNetworksManagementPopupView(EventBus eventBus) {
        super(eventBus);
    }

    @Override
    public void edit(HostInterfaceModel model) {
        final HostManagementNetworkModel object = (HostManagementNetworkModel) model;

        super.edit(object);

        object.getPropertyChangedEvent().addListener(new IEventListener<PropertyChangedEventArgs>() {
            @Override
            public void eventRaised(Event<? extends PropertyChangedEventArgs> ev, Object sender, PropertyChangedEventArgs args) {
                if ("Entity".equals(args.propertyName)) { //$NON-NLS-1$
                    nameEditor.asEditor().getSubEditor().setValue(object.getEntity().getName());
                }
            }
        });

        if (object.getEntity() != null) {
            nameEditor.asValueBox().setValue(object.getEntity().getName());
        }
    }
}
