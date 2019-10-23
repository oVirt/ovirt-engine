package org.ovirt.engine.ui.webadmin.section.main.view.tab.datacenter;

import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.network.HostNetworkQos;
import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.table.column.AbstractTextColumn;
import org.ovirt.engine.ui.common.widget.uicommon.AbstractModelBoundTableWidget;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.DataCenterListModel;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.qos.DataCenterHostNetworkQosListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.network.HostNetworkQosActionPanelPresenterWidget;

import com.google.gwt.event.shared.EventBus;

public class HostNetworkQosListModelTable extends AbstractModelBoundTableWidget<StoragePool, HostNetworkQos,
    DataCenterHostNetworkQosListModel> {

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    public HostNetworkQosListModelTable(SearchableDetailModelProvider<HostNetworkQos,
            DataCenterListModel, DataCenterHostNetworkQosListModel> modelProvider, EventBus eventBus,
            HostNetworkQosActionPanelPresenterWidget actionPanel,
            ClientStorage clientStorage) {
        super(modelProvider, eventBus, actionPanel, clientStorage, false);
        initTable();
    }

    @Override
    public void initTable() {
        getTable().enableColumnResizing();

        AbstractTextColumn<HostNetworkQos> nameColumn = new AbstractTextColumn<HostNetworkQos>() {
            @Override
            public String getValue(HostNetworkQos object) {
                assert object.getName() != null : "QoS entitites in a DC context must be named!"; //$NON-NLS-1$
                return object.getName(); //$NON-NLS-1$
            }
        };
        nameColumn.makeSortable();
        getTable().addColumn(nameColumn, constants.qosName(), "200px"); //$NON-NLS-1$

        AbstractTextColumn<HostNetworkQos> descColumn = new AbstractTextColumn<HostNetworkQos>() {
            @Override
            public String getValue(HostNetworkQos object) {
                return object.getDescription() == null ? "" : object.getDescription(); //$NON-NLS-1$
            }
        };
        descColumn.makeSortable();
        getTable().addColumn(descColumn, constants.qosDescription(), "150px"); //$NON-NLS-1$

        AbstractTextColumn<HostNetworkQos> outAverageLinkshareColumn = new AbstractTextColumn<HostNetworkQos>() {
            @Override
            public String getValue(HostNetworkQos object) {
                return object.getOutAverageLinkshare() == null ? constants.noneQos()
                        : object.getOutAverageLinkshare().toString();
            }
        };
        outAverageLinkshareColumn.makeSortable();
        getTable().addColumn(outAverageLinkshareColumn, constants.hostNetworkQosTabOutAverageLinkshare(), "105px"); //$NON-NLS-1$

        AbstractTextColumn<HostNetworkQos> outAverageUpperlimitColumn = new AbstractTextColumn<HostNetworkQos>() {
            @Override
            public String getValue(HostNetworkQos object) {
                return object.getOutAverageUpperlimit() == null ? constants.unlimitedQos()
                        : object.getOutAverageUpperlimit().toString();
            }
        };
        outAverageUpperlimitColumn.makeSortable();
        getTable().addColumn(outAverageUpperlimitColumn, constants.hostNetworkQosTabOutAverageUpperlimit(), "105px"); //$NON-NLS-1$

        AbstractTextColumn<HostNetworkQos> outAverageRealtimeColumn = new AbstractTextColumn<HostNetworkQos>() {
            @Override
            public String getValue(HostNetworkQos object) {
                return object.getOutAverageRealtime() == null ? constants.noneQos()
                        : object.getOutAverageRealtime().toString();
            }
        };
        outAverageRealtimeColumn.makeSortable();
        getTable().addColumn(outAverageRealtimeColumn, constants.hostNetworkQosTabOutAverageRealtime(), "105px"); //$NON-NLS-1$
    }

    @Override
    public void addModelListeners() {
        super.addModelListeners();
        getTable().getSelectionModel().addSelectionChangeHandler(event ->
            getModelProvider().setSelectedItems(getTable().getSelectedItems())
        );
    }
}
