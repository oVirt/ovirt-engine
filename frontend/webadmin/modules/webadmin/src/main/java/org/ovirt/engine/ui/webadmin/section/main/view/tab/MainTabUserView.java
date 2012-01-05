package org.ovirt.engine.ui.webadmin.section.main.view.tab;

import org.ovirt.engine.core.common.businessentities.DbUser;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.users.UserListModel;
import org.ovirt.engine.ui.webadmin.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.MainTabUserPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractMainTabWithDetailsTableView;
import org.ovirt.engine.ui.webadmin.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.webadmin.widget.action.UiCommandButtonDefinition;
import org.ovirt.engine.ui.webadmin.widget.table.column.TextColumnWithTooltip;
import org.ovirt.engine.ui.webadmin.widget.table.column.UserStatusColumn;

import com.google.gwt.core.client.GWT;
import com.google.inject.Inject;

public class MainTabUserView extends AbstractMainTabWithDetailsTableView<DbUser, UserListModel> implements MainTabUserPresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<MainTabUserView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @Inject
    public MainTabUserView(MainModelProvider<DbUser, UserListModel> modelProvider) {
        super(modelProvider);
        ViewIdHandler.idHandler.generateAndSetIds(this);
        initTable();
        initWidget(getTable());
    }

    void initTable() {
        getTable().addColumn(new UserStatusColumn(), "", "30px");

        getTable().addColumn(new TextColumnWithTooltip<DbUser>() {
            @Override
            public String getValue(DbUser object) {
                return object.getname();
            }
        }, "First Name");

        getTable().addColumn(new TextColumnWithTooltip<DbUser>() {
            @Override
            public String getValue(DbUser object) {
                return object.getsurname();
            }
        }, "Last Name");

        getTable().addColumn(new TextColumnWithTooltip<DbUser>(40) {
            @Override
            public String getValue(DbUser object) {
                return object.getusername();
            }
        }, "User Name");

        getTable().addColumn(new TextColumnWithTooltip<DbUser>(40) {
            @Override
            public String getValue(DbUser object) {
                return object.getgroups();
            }
        }, "Group");

        getTable().addColumn(new TextColumnWithTooltip<DbUser>() {
            @Override
            public String getValue(DbUser object) {
                return object.getemail();
            }
        }, "e-mail");

        getTable().addActionButton(new UiCommandButtonDefinition<DbUser>("Add") {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getAddCommand();
            }
        });
        getTable().addActionButton(new UiCommandButtonDefinition<DbUser>("Remove") {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getRemoveCommand();
            }
        });
        getTable().addActionButton(new UiCommandButtonDefinition<DbUser>("Assign Tags") {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getAssignTagsCommand();
            }
        });
    }

}
