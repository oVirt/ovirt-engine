package org.ovirt.engine.ui.webadmin.section.main.view.tab.pool;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.vm_pools;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.pools.PoolListModel;
import org.ovirt.engine.ui.uicommonweb.models.pools.PoolVmListModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.pool.SubTabPoolVmPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTableView;
import org.ovirt.engine.ui.webadmin.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.webadmin.widget.table.UiCommandButtonDefinition;
import org.ovirt.engine.ui.webadmin.widget.table.column.EnumColumn;
import org.ovirt.engine.ui.webadmin.widget.table.column.UptimeColumn;
import org.ovirt.engine.ui.webadmin.widget.table.column.VmStatusColumn;
import org.ovirt.engine.ui.webadmin.widget.table.column.VmTypeColumn;

import com.google.gwt.user.cellview.client.TextColumn;

public class SubTabPoolVmView extends AbstractSubTabTableView<vm_pools, VM, PoolListModel, PoolVmListModel>
        implements SubTabPoolVmPresenter.ViewDef {

    @Inject
    public SubTabPoolVmView(SearchableDetailModelProvider<VM, PoolListModel, PoolVmListModel> modelProvider) {
        super(modelProvider);
        initTable();
        initWidget(getTable());
    }

    void initTable() {
        getTable().addColumn(new VmStatusColumn(), "", "30px");

        TextColumn<VM> nameColumn = new TextColumn<VM>() {
            @Override
            public String getValue(VM object) {
                return object.getvm_name();
            }
        };
        getTable().addColumn(nameColumn, "Name");

        getTable().addColumn(new VmTypeColumn(), "", "30px");

        TextColumn<VM> hostColumn = new TextColumn<VM>() {
            @Override
            public String getValue(VM object) {
                return object.getrun_on_vds_name();
            }
        };
        getTable().addColumn(hostColumn, "Host");

        TextColumn<VM> ipColumn = new TextColumn<VM>() {
            @Override
            public String getValue(VM object) {
                return object.getvm_ip();
            }
        };
        getTable().addColumn(ipColumn, "IP Address");

        TextColumn<VM> statusColumn = new EnumColumn<VM, VMStatus>() {
            @Override
            protected VMStatus getRawValue(VM object) {
                return object.getstatus();
            }
        };
        getTable().addColumn(statusColumn, "Status");

        TextColumn<VM> uptimeColumn = new UptimeColumn<VM>() {
            @Override
            protected Double getRawValue(VM object) {
                return object.getRoundedElapsedTime();
            }
        };
        getTable().addColumn(uptimeColumn, "Uptime");

        TextColumn<VM> loggedInUserColumn = new TextColumn<VM>() {
            @Override
            public String getValue(VM object) {
                return object.getguest_cur_user_name();
            }
        };
        getTable().addColumn(loggedInUserColumn, "Logged-in User");

        getTable().addActionButton(new UiCommandButtonDefinition<VM>("Detach") {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getDetachCommand();
            }
        });
    }

}
