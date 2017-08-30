package org.ovirt.engine.ui.webadmin.section.main.view.tab.network;

import org.ovirt.engine.core.common.businessentities.network.ExternalSubnet;
import org.ovirt.engine.core.common.businessentities.network.ExternalSubnet.IpVersion;
import org.ovirt.engine.core.common.businessentities.network.NetworkView;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.table.column.AbstractEnumColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractTextColumn;
import org.ovirt.engine.ui.uicommonweb.models.networks.NetworkExternalSubnetListModel;
import org.ovirt.engine.ui.uicommonweb.models.networks.NetworkListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.network.SubTabNetworkExternalSubnetPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTableView;

import com.google.gwt.core.client.GWT;
import com.google.inject.Inject;

public class SubTabNetworkExternalSubnetView extends AbstractSubTabTableView<NetworkView, ExternalSubnet, NetworkListModel, NetworkExternalSubnetListModel>
        implements SubTabNetworkExternalSubnetPresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<SubTabNetworkExternalSubnetView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public SubTabNetworkExternalSubnetView(SearchableDetailModelProvider<ExternalSubnet, NetworkListModel, NetworkExternalSubnetListModel> modelProvider) {
        super(modelProvider);
        initTable();
        initWidget(getTableContainer());
    }

    @Override
    protected void generateIds() {
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }

    void initTable() {
        getTable().enableColumnResizing();

        AbstractTextColumn<ExternalSubnet> nameColumn =
                new AbstractTextColumn<ExternalSubnet>() {
                    @Override
                    public String getValue(ExternalSubnet object) {
                        return object.getName();
                    }
                };
        nameColumn.makeSortable();
        getTable().addColumn(nameColumn, constants.nameExternalSubnet(), "200px"); //$NON-NLS-1$

        AbstractTextColumn<ExternalSubnet> cidrColumn =
                new AbstractTextColumn<ExternalSubnet>() {
            @Override
            public String getValue(ExternalSubnet object) {
                return object.getCidr();
            }
        };
        cidrColumn.makeSortable();
        getTable().addColumn(cidrColumn, constants.cidrExternalSubnet(), "100px"); //$NON-NLS-1$

        AbstractTextColumn<ExternalSubnet> ipVersionColumn =
                new AbstractEnumColumn<ExternalSubnet, IpVersion>() {
            @Override
            protected IpVersion getRawValue(ExternalSubnet object) {
                return object.getIpVersion();
            }
        };
        ipVersionColumn.makeSortable();
        getTable().addColumn(ipVersionColumn, constants.ipVersionExternalSubnet(), "80px"); //$NON-NLS-1$

        AbstractTextColumn<ExternalSubnet> gatewayColumn =
                new AbstractTextColumn<ExternalSubnet>() {
                    @Override
                    public String getValue(ExternalSubnet object) {
                        return object.getGateway();
                    }
                };
        gatewayColumn.makeSortable();
        getTable().addColumn(gatewayColumn, constants.gatewayExternalSubnet(), "100px"); //$NON-NLS-1$

        AbstractTextColumn<ExternalSubnet> dnsServersColumn =
                new AbstractTextColumn<ExternalSubnet>() {
                    @Override
                    public String getValue(ExternalSubnet object) {
                        if (object != null && object.getDnsServers() != null) {
                            return String.join(", ", object.getDnsServers()); //$NON-NLS-1$
                        } else {
                            return "";
                        }
                    }
                };
        dnsServersColumn.makeSortable();
        getTable().addColumn(dnsServersColumn, constants.dnsServersExternalSubnet(), "200px"); //$NON-NLS-1$

        AbstractTextColumn<ExternalSubnet> externalIdColumn =
                new AbstractTextColumn<ExternalSubnet>() {
            @Override
            public String getValue(ExternalSubnet object) {
                return object.getId();
            }
        };
        externalIdColumn.makeSortable();
        getTable().addColumn(externalIdColumn, constants.externalIdExternalSubnet(), "300px"); //$NON-NLS-1$
    }
}
