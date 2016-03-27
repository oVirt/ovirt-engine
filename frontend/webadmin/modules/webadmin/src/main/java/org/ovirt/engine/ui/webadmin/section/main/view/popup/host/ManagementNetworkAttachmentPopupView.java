package org.ovirt.engine.ui.webadmin.section.main.view.popup.host;

import org.ovirt.engine.ui.uicommonweb.models.hosts.ManagementNetworkAttachmentModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.NetworkAttachmentModel;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.host.ManagementNetworkAttachmentPopupPresenterWidget;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class ManagementNetworkAttachmentPopupView extends NetworkAttachmentPopupView implements ManagementNetworkAttachmentPopupPresenterWidget.ViewDef {

    @Inject
    public ManagementNetworkAttachmentPopupView(EventBus eventBus) {
        super(eventBus);
    }

    @Override
    public void edit(NetworkAttachmentModel model) {
        final ManagementNetworkAttachmentModel object = (ManagementNetworkAttachmentModel) model;

        super.edit(object);

        object.getPropertyChangedEvent().addListener(new IEventListener<PropertyChangedEventArgs>() {
            @Override
            public void eventRaised(Event<? extends PropertyChangedEventArgs> ev, Object sender, PropertyChangedEventArgs args) {
                if ("Entity".equals(args.propertyName)) { //$NON-NLS-1$
                    nameEditor.asEditor().getSubEditor().setValue(object.getNetwork().getName());
                }
            }
        });

        if (object.getNetwork() != null) {
            nameEditor.asValueBox().setValue(object.getNetwork().getName());
        }
    }
}
