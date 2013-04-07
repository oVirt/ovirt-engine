package org.ovirt.engine.ui.webadmin.section.main.view.popup.host;

import org.ovirt.engine.ui.uicommonweb.models.hosts.HostBondInterfaceModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.host.SetupNetworksBondPopupPresenterWidget;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class SetupNetworksBondPopupView extends HostBondPopupView implements SetupNetworksBondPopupPresenterWidget.ViewDef {

    @Inject
    public SetupNetworksBondPopupView(EventBus eventBus, ApplicationResources resources, ApplicationConstants constants) {
        super(eventBus, resources, constants);
    }

    @Override
    public void edit(final HostBondInterfaceModel object) {
        super.edit(object);

        // hide widgets
        info.setVisible(false);
        message.setVisible(false);
        // resize
        layoutPanel.remove(infoPanel);
        layoutPanel.setWidgetSize(mainPanel, 300);
        asPopupPanel().setPixelSize(400, 400);
    }

}
