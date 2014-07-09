package org.ovirt.engine.ui.webadmin.section.main.view.popup.cluster;

import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationMessages;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.ApplicationTemplates;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.cluster.NewClusterNetworkPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.NewNetworkPopupView;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class NewClusterNetworkPopupView extends NewNetworkPopupView implements NewClusterNetworkPopupPresenterWidget.ViewDef {

    private final ApplicationMessages messages;

    @Inject
    public NewClusterNetworkPopupView(EventBus eventBus,
            ApplicationResources resources,
            ApplicationConstants constants,
            ApplicationTemplates templates,
            ApplicationMessages messages) {
        super(eventBus, resources, constants, templates, messages);
        this.messages = messages;
    }

    @Override
    public void setDataCenterName(String name) {
        messageLabel.setText(messages.theNetworkWillBeAddedToTheDataCenterAsWell(name));

    }

    @Override
    public void updateVisibility() {
        super.updateVisibility();
        messageLabel.setVisible(true);
        dataCenterEditor.setVisible(false);
    }

}
