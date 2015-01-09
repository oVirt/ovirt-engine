package org.ovirt.engine.ui.common.widget.uicommon.permissions;

import org.ovirt.engine.core.common.businessentities.Permissions;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.uicommon.model.SearchableTableModelProvider;
import org.ovirt.engine.ui.common.widget.action.UiCommandButtonDefinition;
import org.ovirt.engine.ui.common.widget.table.column.PermissionTypeColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractTextColumnWithTooltip;
import org.ovirt.engine.ui.common.widget.uicommon.AbstractModelBoundTableWidget;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.configure.PermissionListModel;

import com.google.gwt.event.shared.EventBus;

public class PermissionListModelTable<P extends PermissionListModel> extends AbstractModelBoundTableWidget<Permissions, P> {

    public PermissionListModelTable(
            SearchableTableModelProvider<Permissions, P> modelProvider,
            EventBus eventBus, ClientStorage clientStorage) {
        super(modelProvider, eventBus, clientStorage, false);
    }

    @Override
    public void initTable(CommonApplicationConstants constants) {
        getTable().enableColumnResizing();

        getTable().addColumn(new PermissionTypeColumn(), constants.empty(), "30px"); //$NON-NLS-1$

        AbstractTextColumnWithTooltip<Permissions> userColumn = new AbstractTextColumnWithTooltip<Permissions>() {
            @Override
            public String getValue(Permissions object) {
                return object.getOwnerName();
            }
        };
        userColumn.makeSortable();
        getTable().addColumn(userColumn, constants.userPermission(), "300px"); //$NON-NLS-1$


        AbstractTextColumnWithTooltip<Permissions> authzColumn = new AbstractTextColumnWithTooltip<Permissions>() {
            @Override
            public String getValue(Permissions object) {
                return object.getAuthz();
            }
        };
        authzColumn.makeSortable();
        getTable().addColumn(authzColumn, constants.authz(), "300px"); //$NON-NLS-1$

        AbstractTextColumnWithTooltip<Permissions> namespaceColumn = new AbstractTextColumnWithTooltip<Permissions>() {
            @Override
            public String getValue(Permissions object) {
                return object.getNamespace();
            }
        };
        namespaceColumn.makeSortable();
        getTable().addColumn(namespaceColumn, constants.namespace(), "300px"); //$NON-NLS-1$

        AbstractTextColumnWithTooltip<Permissions> roleColumn = new AbstractTextColumnWithTooltip<Permissions>() {
            @Override
            public String getValue(Permissions object) {
                return object.getRoleName();
            }
        };
        roleColumn.makeSortable();
        getTable().addColumn(roleColumn, constants.rolePermission(), "300px"); //$NON-NLS-1$

        getTable().addActionButton(new UiCommandButtonDefinition<Permissions>(getEventBus(), constants.addPermission()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getAddCommand();
            }
        });
        getTable().addActionButton(new UiCommandButtonDefinition<Permissions>(getEventBus(), constants.removePermission()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getRemoveCommand();
            }
        });
    }

}
