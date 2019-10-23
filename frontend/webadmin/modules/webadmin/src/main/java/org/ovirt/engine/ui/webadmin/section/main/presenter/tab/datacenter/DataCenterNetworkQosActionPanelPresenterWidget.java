package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.datacenter;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.network.NetworkQoS;
import org.ovirt.engine.ui.common.presenter.DetailActionPanelPresenterWidget;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.DataCenterListModel;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.DataCenterNetworkQoSListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminButtonDefinition;

import com.google.web.bindery.event.shared.EventBus;

public class DataCenterNetworkQosActionPanelPresenterWidget
    extends DetailActionPanelPresenterWidget<StoragePool, NetworkQoS, DataCenterListModel, DataCenterNetworkQoSListModel> {

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public DataCenterNetworkQosActionPanelPresenterWidget(EventBus eventBus,
            DetailActionPanelPresenterWidget.ViewDef<StoragePool, NetworkQoS> view,
            SearchableDetailModelProvider<NetworkQoS, DataCenterListModel, DataCenterNetworkQoSListModel> dataProvider) {
        super(eventBus, view, dataProvider);
    }

    @Override
    protected void initializeButtons() {
        addActionButton(new WebAdminButtonDefinition<StoragePool, NetworkQoS>(constants.newNetworkQoS()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getNewCommand();
            }
        });

        addActionButton(new WebAdminButtonDefinition<StoragePool, NetworkQoS>(constants.editNetworkQoS()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getEditCommand();
            }
        });

        addActionButton(new WebAdminButtonDefinition<StoragePool, NetworkQoS>(constants.removeNetworkQoS()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getRemoveCommand();
            }
        });
    }

}
