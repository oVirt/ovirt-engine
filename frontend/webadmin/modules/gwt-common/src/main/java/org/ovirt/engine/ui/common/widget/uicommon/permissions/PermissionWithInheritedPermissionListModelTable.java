package org.ovirt.engine.ui.common.widget.uicommon.permissions;

import org.ovirt.engine.core.common.businessentities.permissions;
import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.uicommon.model.SearchableTableModelProvider;
import org.ovirt.engine.ui.common.widget.table.column.BasePermissionTypeColumn;
import org.ovirt.engine.ui.common.widget.table.column.ObjectNameColumn;
import org.ovirt.engine.ui.common.widget.table.column.TextColumnWithTooltip;
import org.ovirt.engine.ui.uicommonweb.models.configure.PermissionListModel;

import com.google.gwt.event.shared.EventBus;

public class PermissionWithInheritedPermissionListModelTable<P extends PermissionListModel> extends PermissionListModelTable {

    public PermissionWithInheritedPermissionListModelTable(
            SearchableTableModelProvider<permissions, PermissionListModel> modelProvider,
            EventBus eventBus, ClientStorage clientStorage,
            BasePermissionTypeColumn permissionTypeColumn) {
        super(modelProvider, eventBus, clientStorage, permissionTypeColumn);
    }

    @Override
    public void initTable() {
        super.initTable();

        TextColumnWithTooltip<permissions> permissionColumn = new ObjectNameColumn<permissions>() {
            @Override
            protected Object[] getRawValue(permissions object) {
                return new Object[] { object.getObjectType(), object.getObjectName(), getModel().getEntity(),
                        object.getObjectId()
                };
            }
        };
        getTable().addColumn(permissionColumn, "Inherited Permission");
    }

}
