package org.ovirt.engine.ui.webadmin.section.main.view.tab;

import org.ovirt.engine.core.common.businessentities.VmPool;
import org.ovirt.engine.core.common.businessentities.VmPoolType;
import org.ovirt.engine.core.searchbackend.PoolConditionFieldAutoCompleter;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.common.widget.table.column.AbstractEnumColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractTextColumn;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.pools.PoolListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.MainTabPoolPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractMainTabWithDetailsTableView;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminButtonDefinition;
import org.ovirt.engine.ui.webadmin.widget.table.column.CommentColumn;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.inject.Inject;

public class MainTabPoolView extends AbstractMainTabWithDetailsTableView<VmPool, PoolListModel> implements MainTabPoolPresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<MainTabPoolView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public MainTabPoolView(MainModelProvider<VmPool, PoolListModel> modelProvider) {
        super(modelProvider);
        ViewIdHandler.idHandler.generateAndSetIds(this);
        initTable();
        initWidget(getTable());
    }

    void initTable() {
        getTable().enableColumnResizing();

        AbstractTextColumn<VmPool> nameColumn = new AbstractTextColumn<VmPool>() {
            @Override
            public String getValue(VmPool object) {
                return object.getName();
            }
        };
        nameColumn.makeSortable(PoolConditionFieldAutoCompleter.NAME);
        getTable().addColumn(nameColumn, constants.namePool(), "150px"); //$NON-NLS-1$

        CommentColumn<VmPool> commentColumn = new CommentColumn<>();
        getTable().addColumnWithHtmlHeader(commentColumn,
                SafeHtmlUtils.fromSafeConstant(constants.commentLabel()),
                "75px"); //$NON-NLS-1$

        AbstractTextColumn<VmPool> assignedColumn = new AbstractTextColumn<VmPool>() {
            @Override
            public String getValue(VmPool object) {
                return Integer.toString(object.getAssignedVmsCount());
            }
        };
        assignedColumn.makeSortable(PoolConditionFieldAutoCompleter.ASSIGNED_VM_COUNT);
        getTable().addColumn(assignedColumn, constants.assignVmsPool(), "150px"); //$NON-NLS-1$

        AbstractTextColumn<VmPool> runningColumn = new AbstractTextColumn<VmPool>() {
            @Override
            public String getValue(VmPool object) {
                return Integer.toString(object.getRunningVmsCount());
            }
        };
        runningColumn.makeSortable(PoolConditionFieldAutoCompleter.RUNNING_VM_COUNT);
        getTable().addColumn(runningColumn, constants.runningVmsPool(), "150px"); //$NON-NLS-1$

        AbstractTextColumn<VmPool> typeColumn = new AbstractEnumColumn<VmPool, VmPoolType>() {
            @Override
            public VmPoolType getRawValue(VmPool object) {
                return object.getVmPoolType();
            }
        };
        typeColumn.makeSortable(PoolConditionFieldAutoCompleter.TYPE);
        getTable().addColumn(typeColumn, constants.typePool(), "150px"); //$NON-NLS-1$

        AbstractTextColumn<VmPool> descColumn = new AbstractTextColumn<VmPool>() {
            @Override
            public String getValue(VmPool object) {
                return object.getVmPoolDescription();
            }
        };
        descColumn.makeSortable(PoolConditionFieldAutoCompleter.DESCRIPTION);
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
