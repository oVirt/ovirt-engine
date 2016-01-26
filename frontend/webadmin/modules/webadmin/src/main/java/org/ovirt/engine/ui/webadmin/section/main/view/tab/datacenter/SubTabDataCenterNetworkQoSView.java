package org.ovirt.engine.ui.webadmin.section.main.view.tab.datacenter;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.network.NetworkQoS;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.table.column.AbstractTextColumn;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.DataCenterListModel;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.DataCenterNetworkQoSListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.datacenter.SubTabDataCenterNetworkQoSPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTableView;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminButtonDefinition;
import com.google.gwt.core.client.GWT;

public class SubTabDataCenterNetworkQoSView extends AbstractSubTabTableView<StoragePool,
        NetworkQoS, DataCenterListModel, DataCenterNetworkQoSListModel>
        implements SubTabDataCenterNetworkQoSPresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<SubTabDataCenterNetworkQoSView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public SubTabDataCenterNetworkQoSView(SearchableDetailModelProvider<NetworkQoS,
            DataCenterListModel, DataCenterNetworkQoSListModel> modelProvider) {
        super(modelProvider);
        initTable();
        initWidget(getTable());
    }

    @Override
    protected void generateIds() {
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }

    void initTable() {
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

        getTable().addActionButton(new WebAdminButtonDefinition<NetworkQoS>(constants.newNetworkQoS()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getNewCommand();
            }
        });

        getTable().addActionButton(new WebAdminButtonDefinition<NetworkQoS>(constants.editNetworkQoS()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getEditCommand();
            }
        });

        getTable().addActionButton(new WebAdminButtonDefinition<NetworkQoS>(constants.removeNetworkQoS()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getRemoveCommand();
            }
        });
    }
}
