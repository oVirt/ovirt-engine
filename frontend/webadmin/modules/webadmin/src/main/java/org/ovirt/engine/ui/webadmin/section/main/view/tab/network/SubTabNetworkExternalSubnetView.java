package org.ovirt.engine.ui.webadmin.section.main.view.tab.network;

import org.ovirt.engine.core.common.businessentities.network.NetworkView;
import org.ovirt.engine.core.common.businessentities.network.ExternalSubnet;
import org.ovirt.engine.core.common.businessentities.network.ExternalSubnet.IpVersion;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.table.column.EnumColumn;
import org.ovirt.engine.ui.common.widget.table.column.TextColumnWithTooltip;
import org.ovirt.engine.ui.uicommonweb.models.networks.NetworkListModel;
import org.ovirt.engine.ui.uicommonweb.models.networks.NetworkExternalSubnetListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.network.SubTabNetworkExternalSubnetPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTableView;

import com.google.inject.Inject;

public class SubTabNetworkExternalSubnetView extends AbstractSubTabTableView<NetworkView, ExternalSubnet, NetworkListModel, NetworkExternalSubnetListModel>
        implements SubTabNetworkExternalSubnetPresenter.ViewDef {

    @Inject
    public SubTabNetworkExternalSubnetView(SearchableDetailModelProvider<ExternalSubnet, NetworkListModel, NetworkExternalSubnetListModel> modelProvider,
            ApplicationConstants constants) {
        super(modelProvider);
        initTable(constants);
        initWidget(getTable());
    }

    void initTable(ApplicationConstants constants) {
        getTable().enableColumnResizing();

        TextColumnWithTooltip<ExternalSubnet> nameColumn =
                new TextColumnWithTooltip<ExternalSubnet>() {
                    @Override
                    public String getValue(ExternalSubnet object) {
                        return object.getName();
                    }
                };
        getTable().addColumn(nameColumn, constants.nameExternalSubnet(), "300px"); //$NON-NLS-1$

        TextColumnWithTooltip<ExternalSubnet> cidrColumn =
                new TextColumnWithTooltip<ExternalSubnet>() {
            @Override
            public String getValue(ExternalSubnet object) {
                return object.getCidr();
            }
        };
        getTable().addColumn(cidrColumn, constants.cidrExternalSubnet(), "200px"); //$NON-NLS-1$

        TextColumnWithTooltip<ExternalSubnet> ipVersionColumn =
                new EnumColumn<ExternalSubnet, IpVersion>() {
            @Override
            protected IpVersion getRawValue(ExternalSubnet object) {
                return object.getIpVersion();
            }
        };
        getTable().addColumn(ipVersionColumn, constants.ipVersionExternalSubnet(), "100px"); //$NON-NLS-1$

        TextColumnWithTooltip<ExternalSubnet> externalIdColumn =
                new TextColumnWithTooltip<ExternalSubnet>() {
            @Override
            public String getValue(ExternalSubnet object) {
                return object.getId();
            }
        };

        getTable().addColumn(externalIdColumn, constants.externalIdExternalSubnet(), "300px"); //$NON-NLS-1$
    }
}
