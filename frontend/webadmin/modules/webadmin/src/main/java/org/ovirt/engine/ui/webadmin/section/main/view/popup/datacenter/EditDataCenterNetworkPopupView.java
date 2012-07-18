package org.ovirt.engine.ui.webadmin.section.main.view.popup.datacenter;

import org.ovirt.engine.ui.uicommonweb.models.datacenters.DataCenterNetworkModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.ApplicationTemplates;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.datacenter.EditDataCenterNetworkPopupPresenterWidget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class EditDataCenterNetworkPopupView extends DataCenterNetworkPopupView implements EditDataCenterNetworkPopupPresenterWidget.ViewDef{

    interface Driver extends SimpleBeanEditorDriver<DataCenterNetworkModel, DataCenterNetworkPopupView> {
        Driver driver = GWT.create(Driver.class);
    }

    @Inject
    public EditDataCenterNetworkPopupView(EventBus eventBus,
            ApplicationResources resources,
            ApplicationConstants constants, ApplicationTemplates templates) {
        super(eventBus, resources, constants, templates);
        Driver.driver.initialize(this);
    }

    @Override
    protected void localize(ApplicationConstants constants) {
        super.localize(constants);
        mainLabel.setText(constants.dataCenterEditNetworkPopupLabel());
        messageLabel.setHTML(constants.dataCenterNetworkPopupSubLabel());
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
