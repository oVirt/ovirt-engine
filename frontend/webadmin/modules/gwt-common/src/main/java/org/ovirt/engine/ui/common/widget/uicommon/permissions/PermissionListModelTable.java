package org.ovirt.engine.ui.common.widget.uicommon.permissions;

import org.ovirt.engine.core.common.businessentities.permissions;
import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.uicommon.model.SearchableTableModelProvider;
import org.ovirt.engine.ui.common.widget.action.UiCommandButtonDefinition;
import org.ovirt.engine.ui.common.widget.table.column.PermissionTypeColumn;
import org.ovirt.engine.ui.common.widget.table.column.TextColumnWithTooltip;
import org.ovirt.engine.ui.common.widget.uicommon.AbstractModelBoundTableWidget;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.configure.PermissionListModel;

import com.google.gwt.event.shared.EventBus;

public class PermissionListModelTable<P extends PermissionListModel> extends AbstractModelBoundTableWidget<permissions, P> {

    public PermissionListModelTable(
            SearchableTableModelProvider<permissions, P> modelProvider,
            EventBus eventBus, ClientStorage clientStorage) {
        super(modelProvider, eventBus, clientStorage, false);
    }

    @Override
    public void initTable() {
        getTable().addColumn(new PermissionTypeColumn(), "", "30px");

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
