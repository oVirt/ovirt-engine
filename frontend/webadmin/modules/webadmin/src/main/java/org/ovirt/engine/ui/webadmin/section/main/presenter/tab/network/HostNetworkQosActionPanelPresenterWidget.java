package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.network;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.network.HostNetworkQos;
import org.ovirt.engine.ui.common.presenter.DetailActionPanelPresenterWidget;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.DataCenterListModel;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.qos.DataCenterHostNetworkQosListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminButtonDefinition;

import com.google.web.bindery.event.shared.EventBus;

public class HostNetworkQosActionPanelPresenterWidget extends
    DetailActionPanelPresenterWidget<StoragePool, HostNetworkQos, DataCenterListModel, DataCenterHostNetworkQosListModel> {

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public HostNetworkQosActionPanelPresenterWidget(EventBus eventBus,
            DetailActionPanelPresenterWidget.ViewDef<StoragePool, HostNetworkQos> view,
            SearchableDetailModelProvider<HostNetworkQos, DataCenterListModel,
                DataCenterHostNetworkQosListModel> dataProvider) {
        super(eventBus, view, dataProvider);
    }

    @Override
    protected void initializeButtons() {
        addActionButton(new WebAdminButtonDefinition<StoragePool, HostNetworkQos>(constants.newQos()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getNewCommand();
            }
        });

        addActionButton(new WebAdminButtonDefinition<StoragePool, HostNetworkQos>(constants.editQos()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getEditCommand();
            }
        });

        addActionButton(new WebAdminButtonDefinition<StoragePool, HostNetworkQos>(constants.removeQos()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getRemoveCommand();
            }
        });
    }

}
