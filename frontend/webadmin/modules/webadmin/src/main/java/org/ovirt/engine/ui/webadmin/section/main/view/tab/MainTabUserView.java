package org.ovirt.engine.ui.webadmin.section.main.view.tab;

import org.ovirt.engine.core.common.businessentities.DbUser;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.common.widget.table.column.TextColumnWithTooltip;
import org.ovirt.engine.ui.frontend.utils.FormatUtils;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.users.UserListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.MainTabUserPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractMainTabWithDetailsTableView;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminButtonDefinition;
import org.ovirt.engine.ui.webadmin.widget.table.column.UserStatusColumn;

import com.google.gwt.core.client.GWT;
import com.google.inject.Inject;

public class MainTabUserView extends AbstractMainTabWithDetailsTableView<DbUser, UserListModel> implements MainTabUserPresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<MainTabUserView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @Inject
    public MainTabUserView(MainModelProvider<DbUser, UserListModel> modelProvider, ApplicationConstants constants) {
        super(modelProvider);
        ViewIdHandler.idHandler.generateAndSetIds(this);
        initTable(constants);
        initWidget(getTable());
    }

    void initTable(ApplicationConstants constants) {
        getTable().enableColumnResizing();

        getTable().addColumn(new UserStatusColumn(), constants.empty(), "30px"); //$NON-NLS-1$

        getTable().addColumn(new TextColumnWithTooltip<DbUser>() {
            @Override
            public String getValue(DbUser object) {
                return object.getFirstName();
            }
        }, constants.firstnameUser(), "150px"); //$NON-NLS-1$

        getTable().addColumn(new TextColumnWithTooltip<DbUser>() {
            @Override
            public String getValue(DbUser object) {
                return object.getLastName();
            }
        }, constants.lastNameUser(), "150px"); //$NON-NLS-1$

        getTable().addColumn(new TextColumnWithTooltip<DbUser>() {
            @Override
            public String getValue(DbUser object) {
                return FormatUtils.getFullLoginName(object);
            }
        }, constants.userNameUser(), "150px"); //$NON-NLS-1$

        getTable().addColumn(new TextColumnWithTooltip<DbUser>() {
            @Override
            public String getValue(DbUser object) {
                StringBuilder builder = new StringBuilder();
                for (String name : object.getGroupNames()) {
                    if (builder.length() > 0) {
                        builder.append(","); //$NON-NLS-1$
                    }
                    builder.append(name);
                }
                return builder.toString();
            }
        }, constants.groupUser(), "150px"); //$NON-NLS-1$

        getTable().addColumn(new TextColumnWithTooltip<DbUser>() {
            @Override
            public String getValue(DbUser object) {
                return object.getEmail();
            }
        }, constants.emailUser(), "150px"); //$NON-NLS-1$

        getTable().addActionButton(new WebAdminButtonDefinition<DbUser>(constants.addUser()) {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getAddCommand();
            }
        });
        getTable().addActionButton(new WebAdminButtonDefinition<DbUser>(constants.removeUser()) {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getRemoveCommand();
            }
        });
        getTable().addActionButton(new WebAdminButtonDefinition<DbUser>(constants.assignTagsUser()) {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getAssignTagsCommand();
            }
        });
    }

}
