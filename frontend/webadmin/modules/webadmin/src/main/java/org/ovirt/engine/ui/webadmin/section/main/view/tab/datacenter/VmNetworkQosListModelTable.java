package org.ovirt.engine.ui.webadmin.section.main.view.tab.datacenter;

import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.network.NetworkQoS;
import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.table.column.AbstractTextColumn;
import org.ovirt.engine.ui.common.widget.uicommon.AbstractModelBoundTableWidget;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.DataCenterListModel;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.DataCenterNetworkQoSListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.datacenter.DataCenterNetworkQosActionPanelPresenterWidget;

import com.google.gwt.event.shared.EventBus;

public class VmNetworkQosListModelTable extends AbstractModelBoundTableWidget<StoragePool, NetworkQoS, DataCenterNetworkQoSListModel> {

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    public VmNetworkQosListModelTable(SearchableDetailModelProvider<NetworkQoS,
            DataCenterListModel, DataCenterNetworkQoSListModel> modelProvider, EventBus eventBus,
            DataCenterNetworkQosActionPanelPresenterWidget actionPanel,
            ClientStorage clientStorage) {
        super(modelProvider, eventBus, actionPanel, clientStorage, false);
        initTable();
    }

    @Override
    public void initTable() {
        getTable().enableColumnResizing();

        AbstractTextColumn<NetworkQoS> nameColumn = new AbstractTextColumn<NetworkQoS>() {
            @Override
            public String getValue(NetworkQoS object) {
                return object.getName() == null ? "" : object.getName(); //$NON-NLS-1$
            }
        };
        nameColumn.makeSortable();
        getTable().addColumn(nameColumn, constants.networkQoSName(), "200px"); //$NON-NLS-1$

        AbstractTextColumn<NetworkQoS> inAverageColumn = new AbstractTextColumn<NetworkQoS>() {
            @Override
            public String getValue(NetworkQoS object) {
                return object.getInboundAverage() == null ? constants.UnlimitedNetworkQoS()
                        : object.getInboundAverage().toString();
            }
        };
        inAverageColumn.makeSortable();
        getTable().addColumn(inAverageColumn, constants.networkQoSInboundAverage(), "100px"); //$NON-NLS-1$

        AbstractTextColumn<NetworkQoS> inPeakColumn = new AbstractTextColumn<NetworkQoS>() {
            @Override
            public String getValue(NetworkQoS object) {
                return object.getInboundPeak() == null ? constants.UnlimitedNetworkQoS()
                        : object.getInboundPeak().toString();
            }
        };
        inPeakColumn.makeSortable();
        getTable().addColumn(inPeakColumn, constants.networkQoSInboundPeak(), "100px"); //$NON-NLS-1$

        AbstractTextColumn<NetworkQoS> inBurstColumn = new AbstractTextColumn<NetworkQoS>() {
            @Override
            public String getValue(NetworkQoS object) {
                return object.getInboundBurst() == null ? constants.UnlimitedNetworkQoS()
                        : object.getInboundBurst().toString();
            }
        };
        inBurstColumn.makeSortable();
        getTable().addColumn(inBurstColumn, constants.networkQoSInboundBurst(), "100px"); //$NON-NLS-1$

        AbstractTextColumn<NetworkQoS> outAverageColumn = new AbstractTextColumn<NetworkQoS>() {
            @Override
            public String getValue(NetworkQoS object) {
                return object.getOutboundAverage() == null ? constants.UnlimitedNetworkQoS()
                        : object.getOutboundAverage().toString();
            }
        };
        outAverageColumn.makeSortable();
        getTable().addColumn(outAverageColumn, constants.networkQoSOutboundAverage(), "100px"); //$NON-NLS-1$

        AbstractTextColumn<NetworkQoS> outPeakColumn = new AbstractTextColumn<NetworkQoS>() {
            @Override
            public String getValue(NetworkQoS object) {
                return object.getOutboundPeak() == null ? constants.UnlimitedNetworkQoS()
                        : object.getOutboundPeak().toString();
            }
        };
        outPeakColumn.makeSortable();
        getTable().addColumn(outPeakColumn, constants.networkQoSOutboundPeak(), "100px"); //$NON-NLS-1$

        AbstractTextColumn<NetworkQoS> outBurstColumn = new AbstractTextColumn<NetworkQoS>() {
            @Override
            public String getValue(NetworkQoS object) {
                return object.getOutboundBurst() == null ? constants.UnlimitedNetworkQoS()
                        : object.getOutboundBurst().toString();
            }
        };
        outBurstColumn.makeSortable();
        getTable().addColumn(outBurstColumn, constants.networkQoSOutboundBurst(), "100px"); //$NON-NLS-1$
    }

    @Override
    public void addModelListeners() {
        super.addModelListeners();
        getTable().getSelectionModel().addSelectionChangeHandler(event ->
            getModelProvider().setSelectedItems(getTable().getSelectedItems())
        );
    }
}
