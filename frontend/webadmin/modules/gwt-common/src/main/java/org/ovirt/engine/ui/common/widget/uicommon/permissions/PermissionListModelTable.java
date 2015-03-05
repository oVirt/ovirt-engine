package org.ovirt.engine.ui.common.widget.uicommon.permissions;

import com.google.gwt.event.shared.EventBus;
import org.ovirt.engine.core.common.businessentities.Permission;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.gin.AssetProvider;
import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.uicommon.model.SearchableTableModelProvider;
import org.ovirt.engine.ui.common.widget.action.UiCommandButtonDefinition;
import org.ovirt.engine.ui.common.widget.renderer.FullDateTimeRenderer;
import org.ovirt.engine.ui.common.widget.table.column.AbstractRenderedTextColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractTextColumn;
import org.ovirt.engine.ui.common.widget.table.column.PermissionTypeColumn;
import org.ovirt.engine.ui.common.widget.uicommon.AbstractModelBoundTableWidget;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.configure.PermissionListModel;

import java.util.Date;

public class PermissionListModelTable<P extends PermissionListModel<?>> extends AbstractModelBoundTableWidget<Permission, P> {

    private final static CommonApplicationConstants constants = AssetProvider.getConstants();

    public PermissionListModelTable(
            SearchableTableModelProvider<Permission, P> modelProvider,
            EventBus eventBus, ClientStorage clientStorage) {
        super(modelProvider, eventBus, clientStorage, false);
    }

    @Override
    public void initTable() {
        getTable().enableColumnResizing();
        //add columns
        addTypeColumn(constants);
        addUserColumn(constants);
        addAuthzColumn(constants);
        addNameSpaceColumn(constants);
        addRoleColumn(constants);
        addCreationDateColum(constants);
        // add buttons
        addAddPermissionsButton(constants);
        addRemovePermissionsButton(constants);
    }

    private void addTypeColumn(CommonApplicationConstants constants) {
        getTable().addColumn(new PermissionTypeColumn(), constants.empty(), "30px"); //$NON-NLS-1$
    }

    private void addUserColumn(CommonApplicationConstants constants) {
        AbstractTextColumn<Permission> userColumn = new AbstractTextColumn<Permission>() {
            @Override
            public String getValue(Permission object) {
                return object.getOwnerName();
            }
        };
        userColumn.makeSortable();
        getTable().addColumn(userColumn, constants.userPermission(), "300px"); //$NON-NLS-1$
    }

    private void addAuthzColumn(CommonApplicationConstants constants) {
        AbstractTextColumn<Permission> authzColumn = new AbstractTextColumn<Permission>() {
            @Override
            public String getValue(Permission object) {
                return object.getAuthz();
            }
        };
        authzColumn.makeSortable();
        getTable().addColumn(authzColumn, constants.authz(), "150px"); //$NON-NLS-1$
    }

    private void addNameSpaceColumn(CommonApplicationConstants constants) {
        AbstractTextColumn<Permission> namespaceColumn = new AbstractTextColumn<Permission>() {
            @Override
            public String getValue(Permission object) {
                return object.getNamespace();
            }
        };
        namespaceColumn.makeSortable();
        getTable().addColumn(namespaceColumn, constants.namespace(), "150px"); //$NON-NLS-1$
    }

        private void addRoleColumn(CommonApplicationConstants constants) {
            AbstractTextColumn<Permission> roleColumn = new AbstractTextColumn<Permission>() {
            @Override
            public String getValue(Permission object) {
                return object.getRoleName();
            }
        };
        roleColumn.makeSortable();
        getTable().addColumn(roleColumn, constants.rolePermission(), "300px"); //$NON-NLS-1$
    }

    private void addCreationDateColum(CommonApplicationConstants constants) {
            AbstractTextColumn<Permission> creationDateColumn = new AbstractRenderedTextColumn<Permission, Date>(new FullDateTimeRenderer()) {
                @Override
                public Date getRawValue(Permission object) {
                    return new Date(object.getCreationDate() * 1000); // GWT doesn't have TimeUnit.java
                }
        };
        getTable().addColumn(creationDateColumn, constants.permissionsCreationDate(), "300px"); //$NON-NLS-1$
        creationDateColumn.makeSortable();
    }

    private void addAddPermissionsButton(final CommonApplicationConstants constants) {
        getTable().addActionButton(new UiCommandButtonDefinition<Permission>(getEventBus(), constants.addPermission()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getAddCommand();
            }
        });
    }

        private void addRemovePermissionsButton(final CommonApplicationConstants constants) {
            getTable().addActionButton(new UiCommandButtonDefinition<Permission>(getEventBus(), constants.removePermission()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getRemoveCommand();
            }
        });
    }
}
