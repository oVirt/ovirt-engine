package org.ovirt.engine.ui.webadmin.section.main.view.tab.user;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.DbUser;
import org.ovirt.engine.core.common.businessentities.permissions;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.users.UserListModel;
import org.ovirt.engine.ui.uicommonweb.models.users.UserPermissionListModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.user.SubTabUserPermissionPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTableView;
import org.ovirt.engine.ui.webadmin.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.webadmin.widget.action.UiCommandButtonDefinition;
import org.ovirt.engine.ui.webadmin.widget.table.column.GroupNameColumn;
import org.ovirt.engine.ui.webadmin.widget.table.column.ObjectNameColumn;
import org.ovirt.engine.ui.webadmin.widget.table.column.PermissionTypeColumn;

import com.google.gwt.user.cellview.client.TextColumn;

public class SubTabUserPermissionView extends AbstractSubTabTableView<DbUser, permissions, UserListModel, UserPermissionListModel>
        implements SubTabUserPermissionPresenter.ViewDef {

    @Inject
    public SubTabUserPermissionView(SearchableDetailModelProvider<permissions, UserListModel, UserPermissionListModel> modelProvider) {
        super(modelProvider);
        initTable();
        initWidget(getTable());
    }

    void initTable() {
        getTable().addColumn(new PermissionTypeColumn(), "", "30px");

        TextColumn<permissions> roleColumn = new TextColumn<permissions>() {
            @Override
            public String getValue(permissions object) {
                return object.getRoleName();
            }
        };
        getTable().addColumn(roleColumn, "Role");

        TextColumn<permissions> permissionColumn = new ObjectNameColumn<permissions>() {

            @Override
            protected Object[] getRawValue(permissions object) {
                return new Object[] { object.getObjectType(), object.getObjectName() };
            }
        };
        getTable().addColumn(permissionColumn, "Object");

        TextColumn<permissions> groupColumn = new GroupNameColumn<permissions>() {

            @Override
            protected Object[] getRawValue(permissions object) {
                return new Object[] { getDetailModel().getEntity(), object.getad_element_id(), object.getOwnerName() };
            }
        };
        getTable().addColumn(groupColumn, "Inherited Permission");

        getTable().addActionButton(new UiCommandButtonDefinition<permissions>("Remove") {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getRemoveCommand();
            }
        });
    }

}
