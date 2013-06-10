package org.ovirt.engine.ui.webadmin.section.main.view.popup.host;

import org.ovirt.engine.core.common.businessentities.network.NetworkBootProtocol;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostInterfaceModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.ApplicationTemplates;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.host.SetupNetworksInterfacePopupPresenterWidget;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class SetupNetworksInterfacePopupView extends HostInterfacePopupView implements SetupNetworksInterfacePopupPresenterWidget.ViewDef {

    @Inject
    public SetupNetworksInterfacePopupView(EventBus eventBus,
            ApplicationResources resources,
            ApplicationConstants constants,
            ApplicationTemplates templates) {
        super(eventBus, resources, constants, templates);
    }

    @Override
    public void edit(HostInterfaceModel object) {
        super.edit(object);

        info.setVisible(false);
        message.setVisible(false);
        checkConnectivity.setVisible(false);
        bondingModeEditor.setVisible(false);
        commitChanges.setVisible(false);

        gateway.setVisible(true);

        isToSync.setVisible(true);
        if (object.getIsToSync().getIsChangable()){
            isToSyncInfo.setVisible(true);
        }

        // resize
        layoutPanel.remove(infoPanel);
        layoutPanel.setWidgetSize(mainPanel, 235);
        asPopupPanel().setPixelSize(400, 325);

        bootProtocolLabel.setEnabled(object.getBootProtocolsAvailable());
        bootProtocol.setEnabled(object.getBootProtocolsAvailable());
        bootProtocol.setEnabled(NetworkBootProtocol.NONE, object.getNoneBootProtocolAvailable());
    }
}
