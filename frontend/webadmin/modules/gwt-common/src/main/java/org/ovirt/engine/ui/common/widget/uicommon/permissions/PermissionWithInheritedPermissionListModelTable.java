package org.ovirt.engine.ui.common.widget.uicommon.permissions;

import org.ovirt.engine.core.common.businessentities.Permission;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.gin.AssetProvider;
import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.uicommon.model.SearchableTableModelProvider;
import org.ovirt.engine.ui.common.widget.table.column.AbstractObjectNameColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractTextColumn;
import org.ovirt.engine.ui.uicommonweb.models.configure.PermissionListModel;
import com.google.gwt.event.shared.EventBus;

public class PermissionWithInheritedPermissionListModelTable<P extends PermissionListModel<?>> extends PermissionListModelTable<P> {

    private static final CommonApplicationConstants constants = AssetProvider.getConstants();

    public PermissionWithInheritedPermissionListModelTable(
            SearchableTableModelProvider<Permission, P> modelProvider,
            EventBus eventBus, ClientStorage clientStorage) {
        super(modelProvider, eventBus, clientStorage);
    }

    @Override
    public void initTable() {
        super.initTable();

        AbstractTextColumn<Permission> permissionColumn = new AbstractObjectNameColumn<Permission>() {
            @Override
            protected Object[] getRawValue(Permission object) {
                return new Object[] { object.getObjectType(), object.getObjectName(), getModel().getEntity(),
                        object.getObjectId()
                };
            }
        };
        permissionColumn.makeSortable();
        getTable().addColumn(permissionColumn, constants.inheritedPermission(), "300px"); //$NON-NLS-1$
    }

}
