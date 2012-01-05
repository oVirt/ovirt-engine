package org.ovirt.engine.ui.webadmin.section.main.view.tab;

import org.ovirt.engine.core.common.businessentities.VmPoolType;
import org.ovirt.engine.core.common.businessentities.vm_pools;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.pools.PoolListModel;
import org.ovirt.engine.ui.webadmin.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.MainTabPoolPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractMainTabWithDetailsTableView;
import org.ovirt.engine.ui.webadmin.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.webadmin.widget.action.UiCommandButtonDefinition;
import org.ovirt.engine.ui.webadmin.widget.table.column.EnumColumn;
import org.ovirt.engine.ui.webadmin.widget.table.column.TextColumnWithTooltip;

import com.google.gwt.core.client.GWT;
import com.google.inject.Inject;

public class MainTabPoolView extends AbstractMainTabWithDetailsTableView<vm_pools, PoolListModel> implements MainTabPoolPresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<MainTabPoolView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @Inject
    public MainTabPoolView(MainModelProvider<vm_pools, PoolListModel> modelProvider) {
        super(modelProvider);
        ViewIdHandler.idHandler.generateAndSetIds(this);
        initTable();
        initWidget(getTable());
    }

    void initTable() {
        TextColumnWithTooltip<vm_pools> nameColumn = new TextColumnWithTooltip<vm_pools>() {
            @Override
            public String getValue(vm_pools object) {
                return object.getvm_pool_name();
            }
        };
        getTable().addColumn(nameColumn, "Name");

        TextColumnWithTooltip<vm_pools> assignedColumn = new TextColumnWithTooltip<vm_pools>() {
            @Override
            public String getValue(vm_pools object) {
                return Integer.toString(object.getvm_assigned_count());
            }
        };
        getTable().addColumn(assignedColumn, "Assigned VMs");

        TextColumnWithTooltip<vm_pools> runningColumn = new TextColumnWithTooltip<vm_pools>() {
            @Override
            public String getValue(vm_pools object) {
                return Integer.toString(object.getvm_running_count());
            }
        };
        getTable().addColumn(runningColumn, "Running VMs");

        TextColumnWithTooltip<vm_pools> typeColumn = new EnumColumn<vm_pools, VmPoolType>() {
            @Override
            public VmPoolType getRawValue(vm_pools object) {
                return object.getvm_pool_type();
            }
        };
        getTable().addColumn(typeColumn, "Type");

        TextColumnWithTooltip<vm_pools> descColumn = new TextColumnWithTooltip<vm_pools>() {
            @Override
            public String getValue(vm_pools object) {
                return object.getvm_pool_description();
            }
        };
        getTable().addColumn(descColumn, "Description");

        getTable().addActionButton(new UiCommandButtonDefinition<vm_pools>("New", false, false) {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getNewCommand();
            }
        });
        getTable().addActionButton(new UiCommandButtonDefinition<vm_pools>("Edit", false, false) {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getEditCommand();
            }
        });
        getTable().addActionButton(new UiCommandButtonDefinition<vm_pools>("Remove", false, false) {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getRemoveCommand();
            }
        });
    }

}
