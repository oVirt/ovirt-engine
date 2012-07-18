package org.ovirt.engine.ui.webadmin.section.main.view.popup.cluster;

import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterNetworkModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationMessages;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.ApplicationTemplates;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.cluster.ClusterNetworkPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.AbstractNetworkPopupView;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class ClusterNetworkPopupView extends AbstractNetworkPopupView<ClusterNetworkModel> implements ClusterNetworkPopupPresenterWidget.ViewDef {

    interface Driver extends SimpleBeanEditorDriver<ClusterNetworkModel, ClusterNetworkPopupView> {
        Driver driver = GWT.create(Driver.class);
    }

    private final ApplicationMessages messages;

    @Inject
    public ClusterNetworkPopupView(EventBus eventBus,
            ApplicationResources resources,
            ApplicationConstants constants,
            ApplicationTemplates templates,
            ApplicationMessages messages) {
        super(eventBus, resources, constants, templates);
        Driver.driver.initialize(this);
        this.messages = messages;
    }

    @Override
    public void setDataCenterName(String name) {
        mainLabel.setText(messages.theNetworkWillBeAddedToTheDataCenterAsWell(name));

    }

    @Override
    public void updateVisibility() {
        super.updateVisibility();
        messageLabel.setVisible(false);
        apply.setVisible(false);
    }

    @Override
    public void edit(ClusterNetworkModel object) {
        Driver.driver.edit(object);

    }

    @Override
    public ClusterNetworkModel flush() {
        return Driver.driver.flush();
    }

}
