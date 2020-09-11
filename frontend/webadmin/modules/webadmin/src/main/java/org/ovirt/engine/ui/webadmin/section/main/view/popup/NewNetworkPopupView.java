package org.ovirt.engine.ui.webadmin.section.main.view.popup;

import org.ovirt.engine.ui.common.editor.UiCommonEditorDriver;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.NetworkClusterModel;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.NewNetworkModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.datacenter.NewNetworkPopupPresenterWidget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class NewNetworkPopupView extends AbstractNetworkPopupView<NewNetworkModel> implements NewNetworkPopupPresenterWidget.ViewDef {

    interface Driver extends UiCommonEditorDriver<NewNetworkModel, NewNetworkPopupView> {
    }

    private final Driver driver = GWT.create(Driver.class);

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public NewNetworkPopupView(EventBus eventBus) {
        super(eventBus);
        driver.initialize(this);
        portIsolationEditor.asCheckBox().addValueChangeHandler(event -> refreshClustersTable());
        isVmNetworkEditor.asCheckBox().addValueChangeHandler(event -> refreshClustersTable());
    }

    @Override
    public void updateVisibility() {
        super.updateVisibility();
        messageLabel.setVisible(false);
    }

    @Override
    protected void localize() {
        super.localize();
        mainLabel.setText(constants.dataCenterNewNetworkPopupLabel());
    }

    @Override
    public void edit(NewNetworkModel object) {
        super.edit(object);
        driver.edit(object);
    }

    @Override
    public NewNetworkModel flush() {
        super.flush();
        return driver.flush();
    }

    @Override
    public void cleanup() {
        driver.cleanup();
    }

    @Override
    public void setNetworkClusterList(ListModel<NetworkClusterModel> networkClusterList) {
        clustersTable.asEditor().edit(networkClusterList);
    }

    @Override
    public int setTabIndexes(int nextTabIndex) {
        nextTabIndex = createSubnetEditor.setTabIndexes(nextTabIndex);
        nextTabIndex = this.subnetWidget.setTabIndexes(nextTabIndex);
        return nextTabIndex;
    }

}
