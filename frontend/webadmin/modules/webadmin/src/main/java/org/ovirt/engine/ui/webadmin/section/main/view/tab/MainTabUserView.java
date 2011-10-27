package org.ovirt.engine.ui.webadmin.section.main.view.tab;

import org.ovirt.engine.core.common.businessentities.DbUser;
import org.ovirt.engine.ui.uicommonweb.models.users.UserListModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.MainTabUserPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractMainTabWithDetailsTableView;
import org.ovirt.engine.ui.webadmin.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.webadmin.widget.table.UiCommandButtonDefinition;
import org.ovirt.engine.ui.webadmin.widget.table.column.PopupTextColumn;
import org.ovirt.engine.ui.webadmin.widget.table.column.UserStatusColumn;

import com.google.gwt.user.cellview.client.TextColumn;
import com.google.inject.Inject;

public class MainTabUserView extends AbstractMainTabWithDetailsTableView<DbUser, UserListModel> implements MainTabUserPresenter.ViewDef {

    @Inject
    public MainTabUserView(MainModelProvider<DbUser, UserListModel> modelProvider) {
        super(modelProvider);
        initTable();
        initWidget(getTable());
    }

    void initTable() {
        getTable().addColumn(new UserStatusColumn(), "", "30px");

        getTable().addColumn(new TextColumn<DbUser>() {
            @Override
            public String getValue(DbUser object) {
                return object.getname();
            }
        }, "First Name");

        getTable().addColumn(new TextColumn<DbUser>() {
            @Override
            public String getValue(DbUser object) {
                return object.getsurname();
            }
        }, "Last Name");

        getTable().addColumn(new PopupTextColumn<DbUser>(40) {
            @Override
            public String getRawValue(DbUser object) {
                return object.getusername();
            }
        }, "User Name");

        getTable().addColumn(new PopupTextColumn<DbUser>(40) {
            @Override
            public String getRawValue(DbUser object) {
                return object.getgroups();
            }
        }, "Group");

        getTable().addColumn(new TextColumn<DbUser>() {
            @Override
            public String getValue(DbUser object) {
                return object.getemail();
            }
        }, "e-mail");

        getTable().addActionButton(new UiCommandButtonDefinition<DbUser>(getMainModel().getAddCommand(), "Add"));
        getTable().addActionButton(new UiCommandButtonDefinition<DbUser>(getMainModel().getRemoveCommand(), "Remove"));
        getTable().addActionButton(new UiCommandButtonDefinition<DbUser>(getMainModel().getAssignTagsCommand(),
                "Assign Tags"));
    }
}
