package org.ovirt.engine.ui.webadmin.section.main.view.popup.datacenter;

import org.ovirt.engine.ui.uicommonweb.models.datacenters.DataCenterNetworkModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.ApplicationTemplates;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.datacenter.NewDataCenterNetworkPopupPresenterWidget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class NewDataCenterNetworkPopupView extends DataCenterNetworkPopupView implements NewDataCenterNetworkPopupPresenterWidget.ViewDef {

    interface Driver extends SimpleBeanEditorDriver<DataCenterNetworkModel, NewDataCenterNetworkPopupView> {
        Driver driver = GWT.create(Driver.class);
    }

    @Inject
    public NewDataCenterNetworkPopupView(EventBus eventBus,
            ApplicationResources resources,
            ApplicationConstants constants, ApplicationTemplates templates) {
        super(eventBus, resources, constants, templates);
        Driver.driver.initialize(this);
    }

    @Override
    public void updateVisibility() {
        super.updateVisibility();
        messageLabel.setVisible(false);
        apply.setVisible(false);
    }

    @Override
    protected void localize(ApplicationConstants constants) {
        super.localize(constants);
        mainLabel.setText(constants.dataCenterNewNetworkPopupLabel());
    }

    @Override
    public void edit(DataCenterNetworkModel object) {
        Driver.driver.edit(object);
    }

    @Override
    public DataCenterNetworkModel flush() {
       return Driver.driver.flush();
    }

}
