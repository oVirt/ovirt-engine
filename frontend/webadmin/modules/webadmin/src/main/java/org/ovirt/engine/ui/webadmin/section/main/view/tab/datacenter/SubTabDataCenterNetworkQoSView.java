package org.ovirt.engine.ui.webadmin.section.main.view.tab.datacenter;

import com.google.gwt.core.client.GWT;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.network.NetworkQoS;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.table.column.TextColumnWithTooltip;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.DataCenterListModel;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.DataCenterNetworkQoSListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.datacenter.SubTabDataCenterNetworkQoSPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTableView;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminButtonDefinition;

import javax.inject.Inject;

public class SubTabDataCenterNetworkQoSView extends AbstractSubTabTableView<StoragePool,
        NetworkQoS, DataCenterListModel, DataCenterNetworkQoSListModel>
        implements SubTabDataCenterNetworkQoSPresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<SubTabDataCenterNetworkQoSView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @Inject
    public SubTabDataCenterNetworkQoSView(SearchableDetailModelProvider<NetworkQoS,
            DataCenterListModel, DataCenterNetworkQoSListModel> modelProvider,
            ApplicationConstants constants) {
        super(modelProvider);
        ViewIdHandler.idHandler.generateAndSetIds(this);
        initTable(constants);
        initWidget(getTable());
    }

    void initTable(final ApplicationConstants constants) {
        getTable().enableColumnResizing();

        getTable().addColumn(new TextColumnWithTooltip<NetworkQoS>() {
            @Override
            public String getValue(NetworkQoS object) {
                return object.getName() == null ? "" : object.getName(); //$NON-NLS-1$
            }
        }, constants.networkQoSName(), "200px"); //$NON-NLS-1$

        getTable().addColumn(new TextColumnWithTooltip<NetworkQoS>() {
            @Override
            public String getValue(NetworkQoS object) {
                return object.getInboundAverage() == null ? constants.UnlimitedNetworkQoS()
                        : object.getInboundAverage().toString();
            }
        }, constants.networkQoSInboundAverage(), "100px"); //$NON-NLS-1$

        getTable().addColumn(new TextColumnWithTooltip<NetworkQoS>() {
            @Override
            public String getValue(NetworkQoS object) {
                return object.getInboundPeak() == null ? constants.UnlimitedNetworkQoS()
                        : object.getInboundPeak().toString();
            }
        }, constants.networkQoSInboundPeak(), "100px"); //$NON-NLS-1$

        getTable().addColumn(new TextColumnWithTooltip<NetworkQoS>() {
            @Override
            public String getValue(NetworkQoS object) {
                return object.getInboundBurst() == null ? constants.UnlimitedNetworkQoS()
                        : object.getInboundBurst().toString();
            }
        }, constants.networkQoSInboundBurst(), "100px"); //$NON-NLS-1$

        getTable().addColumn(new TextColumnWithTooltip<NetworkQoS>() {
            @Override
            public String getValue(NetworkQoS object) {
                return object.getOutboundAverage() == null ? constants.UnlimitedNetworkQoS()
                        : object.getOutboundAverage().toString();
            }
        }, constants.networkQoSOutboundAverage(), "100px"); //$NON-NLS-1$

        getTable().addColumn(new TextColumnWithTooltip<NetworkQoS>() {
            @Override
            public String getValue(NetworkQoS object) {
                return object.getOutboundPeak() == null ? constants.UnlimitedNetworkQoS()
                        : object.getOutboundPeak().toString();
            }
        }, constants.networkQoSOutboundPeak(), "100px"); //$NON-NLS-1$

        getTable().addColumn(new TextColumnWithTooltip<NetworkQoS>() {
            @Override
            public String getValue(NetworkQoS object) {
                return object.getOutboundBurst() == null ? constants.UnlimitedNetworkQoS()
                        : object.getOutboundBurst().toString();
            }
        }, constants.networkQoSOutboundBurst(), "100px"); //$NON-NLS-1$

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
