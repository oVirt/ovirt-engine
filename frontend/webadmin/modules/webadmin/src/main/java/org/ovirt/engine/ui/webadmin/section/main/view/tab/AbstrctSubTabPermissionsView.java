package org.ovirt.engine.ui.webadmin.section.main.view.tab;

import org.ovirt.engine.core.common.businessentities.permissions;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.ListWithDetailsModel;
import org.ovirt.engine.ui.uicommonweb.models.configure.PermissionListModel;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTableView;
import org.ovirt.engine.ui.webadmin.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.webadmin.widget.table.UiCommandButtonDefinition;
import org.ovirt.engine.ui.webadmin.widget.table.column.ObjectNameColumn;
import org.ovirt.engine.ui.webadmin.widget.table.column.PermissionTypeColumn;

import com.google.gwt.user.cellview.client.TextColumn;

public abstract class AbstrctSubTabPermissionsView<I, M extends ListWithDetailsModel> extends AbstractSubTabTableView<I, permissions, M, PermissionListModel> {

    public AbstrctSubTabPermissionsView(SearchableDetailModelProvider<permissions, M, PermissionListModel> modelProvider) {
        super(modelProvider);
        initTable();
    }

    protected void initTable() {
        getTable().addColumn(new PermissionTypeColumn(), "", "30px");

        TextColumn<permissions> userColumn = new TextColumn<permissions>() {
            @Override
            public String getValue(permissions object) {
                return object.getOwnerName();
            }
        };
        getTable().addColumn(userColumn, "User");

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
                return new Object[] { object.getObjectType(), object.getObjectName(), getDetailModel().getEntity(),
                        object.getObjectId()
                };
            }
        };
        getTable().addColumn(permissionColumn, "Inherited Permission");

        getTable().addActionButton(new UiCommandButtonDefinition<permissions>("Add") {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getAddCommand();
            }
        });
        getTable().addActionButton(new UiCommandButtonDefinition<permissions>("Remove") {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getRemoveCommand();
            }
        });
    }

}
