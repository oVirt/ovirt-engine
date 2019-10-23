package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.datacenter;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.qos.StorageQos;
import org.ovirt.engine.ui.common.presenter.DetailActionPanelPresenterWidget;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.DataCenterListModel;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.qos.DataCenterStorageQosListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminButtonDefinition;

import com.google.web.bindery.event.shared.EventBus;

public class DataCenterStorageQosActionPanelPresenterWidget extends
    DetailActionPanelPresenterWidget<StoragePool, StorageQos, DataCenterListModel, DataCenterStorageQosListModel> {

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public DataCenterStorageQosActionPanelPresenterWidget(EventBus eventBus,
            DetailActionPanelPresenterWidget.ViewDef<StoragePool, StorageQos> view,
            SearchableDetailModelProvider<StorageQos, DataCenterListModel, DataCenterStorageQosListModel> dataProvider) {
        super(eventBus, view, dataProvider);
    }

    @Override
    protected void initializeButtons() {
        addActionButton(new WebAdminButtonDefinition<StoragePool, StorageQos>(constants.newQos()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getNewCommand();
            }
        });

        addActionButton(new WebAdminButtonDefinition<StoragePool, StorageQos>(constants.editQos()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getEditCommand();
            }
        });

        addActionButton(new WebAdminButtonDefinition<StoragePool, StorageQos>(constants.removeQos()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getRemoveCommand();
            }
        });
    }

}
