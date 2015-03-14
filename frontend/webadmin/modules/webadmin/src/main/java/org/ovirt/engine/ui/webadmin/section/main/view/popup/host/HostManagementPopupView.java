package org.ovirt.engine.ui.webadmin.section.main.view.popup.host;

import org.ovirt.engine.ui.uicommonweb.models.hosts.HostInterfaceModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostManagementNetworkModel;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.host.HostManagementPopupPresenterWidget;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class HostManagementPopupView extends HostInterfacePopupView implements HostManagementPopupPresenterWidget.ViewDef {

    private final static ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public HostManagementPopupView(EventBus eventBus) {

        super(eventBus);
        asWidget().setHeight("600px"); //$NON-NLS-1$

        nameEditor.setLabel(constants.networkNameInterface() + ":"); //$NON-NLS-1$
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

    @Override
    public void focusInput() {
        interfaceEditor.setFocus(true);
    }

}
