package org.ovirt.engine.ui.webadmin.section.main.presenter.popup.host;

import org.ovirt.engine.core.common.businessentities.network.NetworkBootProtocol;
import org.ovirt.engine.ui.common.widget.HasEnabledForContainter;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostManagementNetworkModel;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.HasEnabled;
import com.google.inject.Inject;

public class SetupNetworksManagementPopupPresenterWidget extends HostManagementPopupPresenterWidget {

    public interface ViewDef extends HostManagementPopupPresenterWidget.ViewDef {
        HasEnabledForContainter<NetworkBootProtocol> getBootProtocol();

        HasEnabled getBootProtocolLabel();
    }

    @Inject
    public SetupNetworksManagementPopupPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
    }

    @Override
    public void init(final HostManagementNetworkModel model) {
        super.init(model);

        model.getPropertyChangedEvent().addListener(new IEventListener() {

            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                if ("BootProtocolsAvailable".equals(((PropertyChangedEventArgs) args).PropertyName)) { //$NON-NLS-1$
                    boolean bootProtocolsAvailable = model.getBootProtocolsAvailable();
                    ((ViewDef) getView()).getBootProtocolLabel().setEnabled(bootProtocolsAvailable);
                    ((ViewDef) getView()).getBootProtocol().setEnabled(bootProtocolsAvailable);
                    ((ViewDef) getView()).getBootProtocol().setEnabled(NetworkBootProtocol.NONE,
                            model.getNoneBootProtocolAvailable());
                }
            }
        });
    }

}
