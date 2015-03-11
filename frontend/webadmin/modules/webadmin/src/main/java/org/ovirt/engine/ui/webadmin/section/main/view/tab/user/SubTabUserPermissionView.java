package org.ovirt.engine.ui.webadmin.section.main.view.tab.user;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.Permissions;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.table.column.AbstractObjectNameColumn;
import org.ovirt.engine.ui.common.widget.table.column.PermissionTypeColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractTextColumn;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.users.UserListModel;
import org.ovirt.engine.ui.uicommonweb.models.users.UserPermissionListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.user.SubTabUserPermissionPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTableView;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminButtonDefinition;
import org.ovirt.engine.ui.webadmin.widget.table.column.AbstractGroupNameColumn;

import com.google.gwt.core.client.GWT;

public class SubTabUserPermissionView extends AbstractSubTabTableView<DbUser, Permissions, UserListModel, UserPermissionListModel>
        implements SubTabUserPermissionPresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<SubTabUserPermissionView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @Inject
    public SubTabUserPermissionView(SearchableDetailModelProvider<Permissions, UserListModel, UserPermissionListModel> modelProvider, ApplicationConstants constants) {
        super(modelProvider);
        initTable(constants);
        initWidget(getTable());
    }

    @Override
    protected void generateIds() {
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }

    void initTable(ApplicationConstants constants) {
        getTable().addColumn(new PermissionTypeColumn(), constants.empty(), "30px"); //$NON-NLS-1$

        AbstractTextColumn<Permissions> roleColumn = new AbstractTextColumn<Permissions>() {
            @Override
            public String getValue(Permissions object) {
                return object.getRoleName();
            }
        };
        roleColumn.makeSortable();
        getTable().addColumn(roleColumn, constants.rolePermission());

        AbstractTextColumn<Permissions> permissionColumn = new AbstractObjectNameColumn<Permissions>() {
            @Override
            protected Object[] getRawValue(Permissions object) {
                return new Object[] { object.getObjectType(), object.getObjectName() };
            }
        };
        permissionColumn.makeSortable();
        getTable().addColumn(permissionColumn, constants.objectPermission());

        AbstractTextColumn<Permissions> inheritedColumn = new AbstractGroupNameColumn<Permissions>() {
            @Override
            protected Object[] getRawValue(Permissions object) {
                return new Object[] { getDetailModel().getEntity(), object.getAdElementId(), object.getOwnerName() };
            }
        };
        inheritedColumn.makeSortable();
        getTable().addColumn(inheritedColumn, constants.inheritedPermission());

        getTable().addActionButton(new WebAdminButtonDefinition<Permissions>(constants.removePermission()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getRemoveCommand();
            }
        });
    }

}
