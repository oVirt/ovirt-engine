package org.ovirt.engine.ui.webadmin.section.main.view.tab;

import org.ovirt.engine.core.common.businessentities.VmPoolType;
import org.ovirt.engine.core.common.businessentities.VmPool;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.common.widget.table.column.EnumColumn;
import org.ovirt.engine.ui.common.widget.table.column.TextColumnWithTooltip;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.pools.PoolListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.MainTabPoolPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractMainTabWithDetailsTableView;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminButtonDefinition;

import com.google.gwt.core.client.GWT;
import com.google.inject.Inject;

public class MainTabPoolView extends AbstractMainTabWithDetailsTableView<VmPool, PoolListModel> implements MainTabPoolPresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<MainTabPoolView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @Inject
    public MainTabPoolView(MainModelProvider<VmPool, PoolListModel> modelProvider, ApplicationConstants constants) {
        super(modelProvider);
        ViewIdHandler.idHandler.generateAndSetIds(this);
        initTable(constants);
        initWidget(getTable());
    }

    void initTable(ApplicationConstants constants) {
        getTable().enableColumnResizing();

        TextColumnWithTooltip<VmPool> nameColumn = new TextColumnWithTooltip<VmPool>() {
            @Override
            public String getValue(VmPool object) {
                return object.getVmPoolName();
            }
        };
        getTable().addColumn(nameColumn, constants.namePool(), "150px"); //$NON-NLS-1$

        TextColumnWithTooltip<VmPool> assignedColumn = new TextColumnWithTooltip<VmPool>() {
            @Override
            public String getValue(VmPool object) {
                return Integer.toString(object.getAssignedVmsCount());
            }
        };
        getTable().addColumn(assignedColumn, constants.assignVmsPool(), "150px"); //$NON-NLS-1$

        TextColumnWithTooltip<VmPool> runningColumn = new TextColumnWithTooltip<VmPool>() {
            @Override
            public String getValue(VmPool object) {
                return Integer.toString(object.getRunningVmsCount());
            }
        };
        getTable().addColumn(runningColumn, constants.runningVmsPool(), "150px"); //$NON-NLS-1$

        TextColumnWithTooltip<VmPool> typeColumn = new EnumColumn<VmPool, VmPoolType>() {
            @Override
            public VmPoolType getRawValue(VmPool object) {
                return object.getVmPoolType();
            }
        };
        getTable().addColumn(typeColumn, constants.typePool(), "150px"); //$NON-NLS-1$

        TextColumnWithTooltip<VmPool> descColumn = new TextColumnWithTooltip<VmPool>() {
            @Override
            public String getValue(VmPool object) {
                return object.getVmPoolDescription();
            }
        };
        getTable().addColumn(descColumn, constants.descriptionPool(), "300px"); //$NON-NLS-1$

        getTable().addActionButton(new WebAdminButtonDefinition<VmPool>(constants.newPool()) {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getNewCommand();
            }
        });
        getTable().addActionButton(new WebAdminButtonDefinition<VmPool>(constants.editPool()) {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getEditCommand();
            }
        });
        getTable().addActionButton(new WebAdminButtonDefinition<VmPool>(constants.removePool()) {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getRemoveCommand();
            }
        });
    }

}
