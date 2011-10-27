package org.ovirt.engine.ui.webadmin.section.main.view.tab;

import org.ovirt.engine.core.common.businessentities.VmPoolType;
import org.ovirt.engine.core.common.businessentities.vm_pools;
import org.ovirt.engine.ui.uicommonweb.models.pools.PoolListModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.MainTabPoolPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractMainTabWithDetailsTableView;
import org.ovirt.engine.ui.webadmin.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.webadmin.widget.table.UiCommandButtonDefinition;
import org.ovirt.engine.ui.webadmin.widget.table.column.EnumColumn;

import com.google.gwt.user.cellview.client.TextColumn;
import com.google.inject.Inject;

public class MainTabPoolView extends AbstractMainTabWithDetailsTableView<vm_pools, PoolListModel> implements MainTabPoolPresenter.ViewDef {

    @Inject
    public MainTabPoolView(MainModelProvider<vm_pools, PoolListModel> modelProvider) {
        super(modelProvider);
        initTable();
        initWidget(getTable());
    }

    void initTable() {
        TextColumn<vm_pools> nameColumn = new TextColumn<vm_pools>() {
            @Override
            public String getValue(vm_pools object) {
                return object.getvm_pool_name();
            }
        };
        getTable().addColumn(nameColumn, "Name");

        TextColumn<vm_pools> assignedColumn = new TextColumn<vm_pools>() {
            @Override
            public String getValue(vm_pools object) {
                return Integer.toString(object.getvm_assigned_count());
            }
        };
        getTable().addColumn(assignedColumn, "Assigned VMs");

        TextColumn<vm_pools> runningColumn = new TextColumn<vm_pools>() {
            @Override
            public String getValue(vm_pools object) {
                return Integer.toString(object.getvm_running_count());
            }
        };
        getTable().addColumn(runningColumn, "Running VMs");

        TextColumn<vm_pools> typeColumn = new EnumColumn<vm_pools, VmPoolType>() {
            @Override
            public VmPoolType getRawValue(vm_pools object) {
                return object.getvm_pool_type();
            }
        };
        getTable().addColumn(typeColumn, "Type");

        TextColumn<vm_pools> descColumn = new TextColumn<vm_pools>() {
            @Override
            public String getValue(vm_pools object) {
                return object.getvm_pool_description();
            }
        };
        getTable().addColumn(descColumn, "Description");

        getTable().addActionButton(new UiCommandButtonDefinition<vm_pools>(getMainModel().getNewCommand(),false,false));
        getTable().addActionButton(new UiCommandButtonDefinition<vm_pools>(getMainModel().getEditCommand(),false,false));
        getTable().addActionButton(new UiCommandButtonDefinition<vm_pools>(getMainModel().getRemoveCommand(),false,false));
    }

}
