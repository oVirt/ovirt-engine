package org.ovirt.engine.ui.common.widget.uicommon.permissions;

import org.ovirt.engine.core.common.businessentities.Permissions;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.uicommon.model.SearchableTableModelProvider;
import org.ovirt.engine.ui.common.widget.table.column.AbstractObjectNameColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractTextColumnWithTooltip;
import org.ovirt.engine.ui.uicommonweb.models.configure.PermissionListModel;

import com.google.gwt.event.shared.EventBus;

public class PermissionWithInheritedPermissionListModelTable<P extends PermissionListModel<?>> extends PermissionListModelTable<P> {

    public PermissionWithInheritedPermissionListModelTable(
            SearchableTableModelProvider<Permissions, P> modelProvider,
            EventBus eventBus, ClientStorage clientStorage) {
        super(modelProvider, eventBus, clientStorage);
    }

    @Override
    public void initTable(CommonApplicationConstants constants) {
        super.initTable(constants);

        AbstractTextColumnWithTooltip<Permissions> permissionColumn = new AbstractObjectNameColumn<Permissions>() {
            @Override
            protected Object[] getRawValue(Permissions object) {
                return new Object[] { object.getObjectType(), object.getObjectName(), getModel().getEntity(),
                        object.getObjectId()
                };
            }
        };
        permissionColumn.makeSortable();
        getTable().addColumn(permissionColumn, constants.inheritedPermission(), "300px"); //$NON-NLS-1$
    }

}
