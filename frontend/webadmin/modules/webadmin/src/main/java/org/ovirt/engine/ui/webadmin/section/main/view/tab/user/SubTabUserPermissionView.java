package org.ovirt.engine.ui.webadmin.section.main.view.tab.user;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.Permission;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.table.column.AbstractObjectNameColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractTextColumn;
import org.ovirt.engine.ui.common.widget.table.column.PermissionTypeColumn;
import org.ovirt.engine.ui.uicommonweb.models.users.UserListModel;
import org.ovirt.engine.ui.uicommonweb.models.users.UserPermissionListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.user.SubTabUserPermissionPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTableView;
import org.ovirt.engine.ui.webadmin.widget.table.column.AbstractGroupNameColumn;

import com.google.gwt.core.client.GWT;

public class SubTabUserPermissionView extends AbstractSubTabTableView<DbUser, Permission, UserListModel, UserPermissionListModel>
        implements SubTabUserPermissionPresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<SubTabUserPermissionView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public SubTabUserPermissionView(SearchableDetailModelProvider<Permission, UserListModel, UserPermissionListModel> modelProvider) {
        super(modelProvider);
        initTable();
        initWidget(getTableContainer());
    }

    @Override
    protected void generateIds() {
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }

    void initTable() {
        getTable().enableColumnResizing();

        PermissionTypeColumn typeColumn = new PermissionTypeColumn();
        typeColumn.setContextMenuTitle(constants.typePermission());
        getTable().addColumn(typeColumn, constants.empty(), "30px"); //$NON-NLS-1$

        AbstractTextColumn<Permission> roleColumn = new AbstractTextColumn<Permission>() {
            @Override
            public String getValue(Permission object) {
                return object.getRoleName();
            }
        };
        roleColumn.makeSortable();
        getTable().addColumn(roleColumn, constants.rolePermission(), "560px"); //$NON-NLS-1$

        AbstractTextColumn<Permission> permissionColumn = new AbstractObjectNameColumn<Permission>() {
            @Override
            protected Object[] getRawValue(Permission object) {
                return new Object[] { object.getObjectType(), object.getObjectName() };
            }
        };
        permissionColumn.makeSortable();
        getTable().addColumn(permissionColumn, constants.objectPermission(), "560px");  //$NON-NLS-1$

        AbstractTextColumn<Permission> inheritedColumn = new AbstractGroupNameColumn<Permission>() {
            @Override
            protected Object[] getRawValue(Permission object) {
                return new Object[] { getDetailModel().getEntity(), object.getAdElementId(), object.getOwnerName() };
            }
        };
        inheritedColumn.makeSortable();
        getTable().addColumn(inheritedColumn, constants.inheritedPermission(), "560px");  //$NON-NLS-1$
    }
}
