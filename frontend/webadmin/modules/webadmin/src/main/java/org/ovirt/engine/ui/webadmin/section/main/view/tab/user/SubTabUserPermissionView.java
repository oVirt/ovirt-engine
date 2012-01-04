package org.ovirt.engine.ui.webadmin.section.main.view.tab.user;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.DbUser;
import org.ovirt.engine.core.common.businessentities.permissions;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.users.UserListModel;
import org.ovirt.engine.ui.uicommonweb.models.users.UserPermissionListModel;
import org.ovirt.engine.ui.webadmin.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.user.SubTabUserPermissionPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTableView;
import org.ovirt.engine.ui.webadmin.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.webadmin.widget.action.UiCommandButtonDefinition;
import org.ovirt.engine.ui.webadmin.widget.table.column.GroupNameColumn;
import org.ovirt.engine.ui.webadmin.widget.table.column.ObjectNameColumn;
import org.ovirt.engine.ui.webadmin.widget.table.column.PermissionTypeColumn;
import org.ovirt.engine.ui.webadmin.widget.table.column.TextColumnWithTooltip;

import com.google.gwt.core.client.GWT;

public class SubTabUserPermissionView extends AbstractSubTabTableView<DbUser, permissions, UserListModel, UserPermissionListModel>
        implements SubTabUserPermissionPresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<SubTabUserPermissionView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @Inject
    public SubTabUserPermissionView(SearchableDetailModelProvider<permissions, UserListModel, UserPermissionListModel> modelProvider) {
        super(modelProvider);
        ViewIdHandler.idHandler.generateAndSetIds(this);
        initTable();
        initWidget(getTable());
    }

    void initTable() {
        getTable().addColumn(new PermissionTypeColumn(), "", "30px");

        TextColumnWithTooltip<permissions> roleColumn = new TextColumnWithTooltip<permissions>() {
            @Override
            public String getValue(permissions object) {
                return object.getRoleName();
            }
        };
        getTable().addColumn(roleColumn, "Role");

        TextColumnWithTooltip<permissions> permissionColumn = new ObjectNameColumn<permissions>() {
            @Override
            protected Object[] getRawValue(permissions object) {
                return new Object[] { object.getObjectType(), object.getObjectName() };
            }
        };
        getTable().addColumn(permissionColumn, "Object");

        TextColumnWithTooltip<permissions> groupColumn = new GroupNameColumn<permissions>() {
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
