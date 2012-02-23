package org.ovirt.engine.ui.common.widget.uicommon.permissions;

import org.ovirt.engine.core.common.businessentities.permissions;
import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.uicommon.model.SearchableTableModelProvider;
import org.ovirt.engine.ui.common.widget.action.UiCommandButtonDefinition;
import org.ovirt.engine.ui.common.widget.table.column.BasePermissionTypeColumn;
import org.ovirt.engine.ui.common.widget.table.column.TextColumnWithTooltip;
import org.ovirt.engine.ui.common.widget.uicommon.AbstractModelBoundTableWidget;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.configure.PermissionListModel;

import com.google.gwt.event.shared.EventBus;

public class PermissionListModelTable extends AbstractModelBoundTableWidget<permissions, PermissionListModel> {

    private final BasePermissionTypeColumn permissionTypeColumn;

    public PermissionListModelTable(
            SearchableTableModelProvider<permissions, PermissionListModel> modelProvider,
            EventBus eventBus, ClientStorage clientStorage,
            BasePermissionTypeColumn permissionTypeColumn) {
        super(modelProvider, eventBus, clientStorage, false);
        this.permissionTypeColumn = permissionTypeColumn;
    }

    @Override
    public void initTable() {
        getTable().addColumn(permissionTypeColumn, "", "30px");

        TextColumnWithTooltip<permissions> userColumn = new TextColumnWithTooltip<permissions>() {
            @Override
            public String getValue(permissions object) {
                return object.getOwnerName();
            }
        };
        getTable().addColumn(userColumn, "User");

        TextColumnWithTooltip<permissions> roleColumn = new TextColumnWithTooltip<permissions>() {
            @Override
            public String getValue(permissions object) {
                return object.getRoleName();
            }
        };
        getTable().addColumn(roleColumn, "Role");

        getTable().addActionButton(new UiCommandButtonDefinition<permissions>(getEventBus(), "Add") {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getAddCommand();
            }
        });
        getTable().addActionButton(new UiCommandButtonDefinition<permissions>(getEventBus(), "Remove") {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getRemoveCommand();
            }
        });
    }

}
