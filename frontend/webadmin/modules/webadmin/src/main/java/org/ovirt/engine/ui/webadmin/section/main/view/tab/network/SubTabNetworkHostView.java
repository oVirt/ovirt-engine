package org.ovirt.engine.ui.webadmin.section.main.view.tab.network;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.Network;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.table.column.EnumColumn;
import org.ovirt.engine.ui.common.widget.table.column.TextColumnWithTooltip;
import org.ovirt.engine.ui.uicommonweb.models.networks.NetworkHostListModel;
import org.ovirt.engine.ui.uicommonweb.models.networks.NetworkListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.network.SubTabNetworkHostPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTableView;
import org.ovirt.engine.ui.webadmin.widget.table.column.HostStatusColumn;

public class SubTabNetworkHostView extends AbstractSubTabTableView<Network, VDS, NetworkListModel, NetworkHostListModel>
        implements SubTabNetworkHostPresenter.ViewDef {

    @Inject
    public SubTabNetworkHostView(SearchableDetailModelProvider<VDS, NetworkListModel, NetworkHostListModel> modelProvider, ApplicationConstants constants) {
        super(modelProvider);
        initTable(constants);
        initWidget(getTable());
    }

    void initTable(final ApplicationConstants constants) {
        getTable().addColumn(new HostStatusColumn(), constants.empty(), "30px"); //$NON-NLS-1$

        TextColumnWithTooltip<VDS> nameColumn = new TextColumnWithTooltip<VDS>() {
            @Override
            public String getValue(VDS object) {
                return object.getvds_name();
            }
        };
        getTable().addColumn(nameColumn, constants.nameHost());

        TextColumnWithTooltip<VDS> hostColumn = new TextColumnWithTooltip<VDS>() {
            @Override
            public String getValue(VDS object) {
                return object.gethost_name();
            }
        };
        getTable().addColumn(hostColumn, constants.ipHost());

        TextColumnWithTooltip<VDS> statusColumn = new EnumColumn<VDS, VDSStatus>() {
            @Override
            public VDSStatus getRawValue(VDS object) {
                return object.getstatus();
            }
        };
        getTable().addColumn(statusColumn, constants.statusHost());
    }

}

