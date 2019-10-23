package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.datacenter;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.qos.CpuQos;
import org.ovirt.engine.ui.common.presenter.DetailActionPanelPresenterWidget;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.DataCenterListModel;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.qos.DataCenterCpuQosListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminButtonDefinition;

import com.google.web.bindery.event.shared.EventBus;

public class CpuQosActionPanelPresenterWidget extends DetailActionPanelPresenterWidget<StoragePool, CpuQos,
    DataCenterListModel, DataCenterCpuQosListModel> {

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public CpuQosActionPanelPresenterWidget(EventBus eventBus,
            DetailActionPanelPresenterWidget.ViewDef<StoragePool, CpuQos> view,
            SearchableDetailModelProvider<CpuQos, DataCenterListModel, DataCenterCpuQosListModel> dataProvider) {
        super(eventBus, view, dataProvider);
    }

    @Override
    protected void initializeButtons() {
        addActionButton(new WebAdminButtonDefinition<StoragePool, CpuQos>(constants.newCpuQos()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getNewCommand();
            }
        });

        addActionButton(new WebAdminButtonDefinition<StoragePool, CpuQos>(constants.editCpuQos()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getEditCommand();
            }
        });

        addActionButton(new WebAdminButtonDefinition<StoragePool, CpuQos>(constants.removeQos()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getRemoveCommand();
            }
        });
    }

}
