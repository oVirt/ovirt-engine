package org.ovirt.engine.ui.webadmin.section.main.view.popup.host;

import org.ovirt.engine.core.common.businessentities.network.NetworkBootProtocol;
import org.ovirt.engine.ui.common.widget.HasEnabledForContainter;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostManagementNetworkModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.ApplicationTemplates;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.host.SetupNetworksManagementPopupPresenterWidget;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.HasEnabled;
import com.google.inject.Inject;

public class SetupNetworksManagementPopupView  extends HostManagementPopupView implements SetupNetworksManagementPopupPresenterWidget.ViewDef {

    @Inject
    public SetupNetworksManagementPopupView(EventBus eventBus,
            ApplicationResources resources,
            ApplicationConstants constants,
            ApplicationTemplates templates) {
        super(eventBus, resources, constants, templates);
    }

    @Override
    public void edit(HostManagementNetworkModel object) {
        super.edit(object);

        info.setVisible(false);
        message.setVisible(false);
        checkConnectivity.setVisible(false);
        bondingModeEditor.setVisible(false);
        commitChanges.setVisible(false);

        isToSync.setVisible(true);
        if (object.getIsToSync().getIsChangable()){
            isToSyncInfo.setVisible(true);
        }

        // resize
        layoutPanel.remove(infoPanel);
        layoutPanel.setWidgetSize(mainPanel, 270);
        asPopupPanel().setPixelSize(400, 350);

        bootProtocolLabel.setEnabled(object.getBootProtocolsAvailable());
        bootProtocol.setEnabled(object.getBootProtocolsAvailable());
        bootProtocol.setEnabled(NetworkBootProtocol.None, object.getNoneBootProtocolAvailable());
    }

    @Override
    public HasEnabledForContainter<NetworkBootProtocol> getBootProtocol() {
        return bootProtocol;
    }

    @Override
    public HasEnabled getBootProtocolLabel() {
        return bootProtocolLabel;
    }


}
