package org.ovirt.engine.ui.webadmin.section.main.view.popup.cluster;

import org.ovirt.engine.ui.webadmin.ApplicationMessages;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.cluster.NewClusterNetworkPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.NewNetworkPopupView;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class NewClusterNetworkPopupView extends NewNetworkPopupView implements NewClusterNetworkPopupPresenterWidget.ViewDef {

    private static final ApplicationMessages messages = AssetProvider.getMessages();

    @Inject
    public NewClusterNetworkPopupView(EventBus eventBus) {
        super(eventBus);
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
