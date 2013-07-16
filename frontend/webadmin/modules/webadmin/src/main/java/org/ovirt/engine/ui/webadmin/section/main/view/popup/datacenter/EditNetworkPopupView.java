package org.ovirt.engine.ui.webadmin.section.main.view.popup.datacenter;

import org.ovirt.engine.ui.uicommonweb.models.datacenters.EditNetworkModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.ApplicationTemplates;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.datacenter.EditNetworkPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.AbstractNetworkPopupView;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class EditNetworkPopupView extends AbstractNetworkPopupView<EditNetworkModel> implements EditNetworkPopupPresenterWidget.ViewDef {

    interface Driver extends SimpleBeanEditorDriver<EditNetworkModel, EditNetworkPopupView> {
    }

    private final Driver driver = GWT.create(Driver.class);

    @Inject
    public EditNetworkPopupView(EventBus eventBus,
            ApplicationResources resources,
            ApplicationConstants constants,
            ApplicationTemplates templates) {
        super(eventBus, resources, constants, templates);
        driver.initialize(this);
    }

    @Override
    protected void localize(ApplicationConstants constants) {
        super.localize(constants);
        mainLabel.setText(constants.dataCenterEditNetworkPopupLabel());
        messageLabel.setHTML(constants.dataCenterNetworkPopupSubLabel());
    }

    @Override
    public void edit(EditNetworkModel object) {
        driver.edit(object);
    }

    @Override
    public void updateVisibility() {
        super.updateVisibility();
        attachPanel.setVisible(false);
        clusterTab.setVisible(false);
    }

    @Override
    public EditNetworkModel flush() {
        return driver.flush();
    }

}
