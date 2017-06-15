package org.ovirt.engine.ui.webadmin.section.main.view.popup.datacenter;

import org.ovirt.engine.ui.common.editor.UiCommonEditorDriver;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.EditNetworkModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.datacenter.EditNetworkPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.AbstractNetworkPopupView;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class EditNetworkPopupView extends AbstractNetworkPopupView<EditNetworkModel> implements EditNetworkPopupPresenterWidget.ViewDef {

    interface Driver extends UiCommonEditorDriver<EditNetworkModel, EditNetworkPopupView> {
    }

    private final Driver driver = GWT.create(Driver.class);

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public EditNetworkPopupView(EventBus eventBus) {
        super(eventBus);
        driver.initialize(this);
    }

    @Override
    protected void localize() {
        super.localize();
        mainLabel.setText(constants.dataCenterEditNetworkPopupLabel());
        messageLabel.setHTML(constants.dataCenterNetworkPopupSubLabel());
    }

    @Override
    public void edit(EditNetworkModel object) {
        super.edit(object);
        driver.edit(object);
    }

    @Override
    public void updateVisibility() {
        super.updateVisibility();
        attachContainer.setVisible(false);
        clusterTab.setVisible(false);
        toggleSubnetVisibility(false);
    }

    @Override
    public EditNetworkModel flush() {
        super.flush();
        return driver.flush();
    }

    @Override
    public void cleanup() {
        driver.cleanup();
    }

}
