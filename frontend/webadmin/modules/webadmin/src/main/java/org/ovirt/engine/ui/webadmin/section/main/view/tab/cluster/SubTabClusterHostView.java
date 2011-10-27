package org.ovirt.engine.ui.webadmin.section.main.view.tab.cluster;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterHostListModel;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterListModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.cluster.SubTabClusterHostPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTableView;
import org.ovirt.engine.ui.webadmin.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.webadmin.widget.table.column.EnumColumn;
import org.ovirt.engine.ui.webadmin.widget.table.column.HostStatusColumn;

import com.google.gwt.user.cellview.client.TextColumn;

public class SubTabClusterHostView extends AbstractSubTabTableView<VDSGroup, VDS, ClusterListModel, ClusterHostListModel>
        implements SubTabClusterHostPresenter.ViewDef {

    @Inject
    public SubTabClusterHostView(SearchableDetailModelProvider<VDS, ClusterListModel, ClusterHostListModel> modelProvider) {
        super(modelProvider);
        initTable();
        initWidget(getTable());
    }

    void initTable() {
        getTable().addColumn(new HostStatusColumn(), "", "30px");

        TextColumn<VDS> nameColumn = new TextColumn<VDS>() {
            @Override
            public String getValue(VDS object) {
                return object.getvds_name();
            }
        };
        getTable().addColumn(nameColumn, "Name");

        TextColumn<VDS> hostColumn = new TextColumn<VDS>() {
            @Override
            public String getValue(VDS object) {
                return object.gethost_name();
            }
        };
        getTable().addColumn(hostColumn, "Host/IP");

        TextColumn<VDS> statusColumn = new EnumColumn<VDS, VDSStatus>() {
            @Override
            public VDSStatus getRawValue(VDS object) {
                return object.getstatus();
            }
        };
        getTable().addColumn(statusColumn, "Status");

        TextColumn<VDS> loadColumn = new TextColumn<VDS>() {
            @Override
            public String getValue(VDS object) {
                return object.getvm_active() + " VMs";
            }
        };
        getTable().addColumn(loadColumn, "Load");
    }

}
