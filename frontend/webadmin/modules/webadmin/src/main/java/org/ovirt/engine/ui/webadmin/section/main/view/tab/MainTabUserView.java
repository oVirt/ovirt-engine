package org.ovirt.engine.ui.webadmin.section.main.view.tab;

import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.searchbackend.VdcUserConditionFieldAutoCompleter;
import org.ovirt.engine.core.searchbackend.VdcUserConditionFieldAutoCompleter.UserOrGroup;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.common.widget.table.column.AbstractTextColumn;
import org.ovirt.engine.ui.common.widget.uicommon.users.UserTypeChangeHandler;
import org.ovirt.engine.ui.common.widget.uicommon.users.UsersTypeRadioGroup;
import org.ovirt.engine.ui.frontend.utils.FormatUtils;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.CommonModel;
import org.ovirt.engine.ui.uicommonweb.models.users.UserListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.MainTabUserPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractMainTabWithDetailsTableView;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminButtonDefinition;
import org.ovirt.engine.ui.webadmin.widget.table.column.UserStatusColumn;
import com.google.gwt.core.client.GWT;
import com.google.gwt.regexp.shared.RegExp;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class MainTabUserView extends AbstractMainTabWithDetailsTableView<DbUser, UserListModel>
    implements MainTabUserPresenter.ViewDef, UserTypeChangeHandler {

    interface ViewIdHandler extends ElementIdHandler<MainTabUserView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    private static final ApplicationConstants constants = AssetProvider.getConstants();
    private static final String searchRegexTypeClause = "\\s*((and|or)\\s+)?type\\s*=\\s*\\S+"; //$NON-NLS-1$
    private static final String searchRegexFlags = "ig"; //$NON-NLS-1$

    private static final RegExp searchPatternDiskTypeClause = RegExp.compile(searchRegexTypeClause, searchRegexFlags);

    private AbstractTextColumn<DbUser> firstNameColumn;
    private AbstractTextColumn<DbUser> groupNameColumn;
    private AbstractTextColumn<DbUser> lastNameColumn;
    private AbstractTextColumn<DbUser> userNameColumn;
    private AbstractTextColumn<DbUser> authzColumn;
    private AbstractTextColumn<DbUser> namespaceColumn;
    private UserStatusColumn userStatusColumn;
    private AbstractTextColumn<DbUser> emailColumn;
    private UsersTypeRadioGroup userTypes;

    @Inject
    Provider<CommonModel> commonModelProvider;

    @Inject
    public MainTabUserView(MainModelProvider<DbUser, UserListModel> modelProvider) {
        super(modelProvider);
        ViewIdHandler.idHandler.generateAndSetIds(this);
        initTable();
        initTableOverhead();
        initWidget(getTable());
    }

    private void initTableOverhead() {
        userTypes = new UsersTypeRadioGroup();
        userTypes.addChangeHandler(this);
        getTable().setTableOverhead(userTypes);
        //Needs this to not hide the overhead behind the buttons.
        getTable().setTableTopMargin(20);
    }

    void initTable() {
        getTable().enableColumnResizing();

        userStatusColumn = new UserStatusColumn();
        userStatusColumn.setContextMenuTitle(constants.statusUser());
        getTable().addColumn(userStatusColumn, constants.empty(), "30px"); //$NON-NLS-1$

        firstNameColumn = new AbstractTextColumn<DbUser>() {
            @Override
            public String getValue(DbUser object) {
                return object.getFirstName();
            }
        };
        firstNameColumn.makeSortable(VdcUserConditionFieldAutoCompleter.FIRST_NAME);
        getTable().addColumn(firstNameColumn, constants.firstnameUser(), "150px"); //$NON-NLS-1$

        groupNameColumn = new AbstractTextColumn<DbUser>() {
            @Override
            public String getValue(DbUser object) {
                return object.getFirstName();
            }
        };
        groupNameColumn.makeSortable(VdcUserConditionFieldAutoCompleter.FIRST_NAME);
        getTable().addColumn(groupNameColumn, constants.groupNameUser(), "150px"); //$NON-NLS-1$

        lastNameColumn = new AbstractTextColumn<DbUser>() {
            @Override
            public String getValue(DbUser object) {
                return object.getLastName();
            }
        };
        lastNameColumn.makeSortable(VdcUserConditionFieldAutoCompleter.LAST_NAME);
        getTable().addColumn(lastNameColumn, constants.lastNameUser(), "150px"); //$NON-NLS-1$

        userNameColumn = new AbstractTextColumn<DbUser>() {
            @Override
            public String getValue(DbUser object) {
                return FormatUtils.getFullLoginName(object);
            }
        };
        userNameColumn.makeSortable(VdcUserConditionFieldAutoCompleter.USER_NAME);
        getTable().addColumn(userNameColumn, constants.userNameUser(), "150px"); //$NON-NLS-1$

        authzColumn = new AbstractTextColumn<DbUser>() {
            @Override
            public String getValue(DbUser object) {
                return object.getDomain();
            }
        };
        authzColumn.makeSortable(VdcUserConditionFieldAutoCompleter.DIRECTORY);
        getTable().addColumn(authzColumn, constants.authz(), "150px"); //$NON-NLS-1$

        namespaceColumn = new AbstractTextColumn<DbUser>() {
            @Override
            public String getValue(DbUser object) {
                return object.getNamespace();
            }
        };
        namespaceColumn.makeSortable();
        getTable().addColumn(namespaceColumn, constants.namespace(), "150px"); //$NON-NLS-1$

        emailColumn = new AbstractTextColumn<DbUser>() {
            @Override
            public String getValue(DbUser object) {
                return object.getEmail();
            }
        };
        getTable().addColumn(emailColumn, constants.emailUser());

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

    @Override
    public void userTypeChanged(UserOrGroup newType) {
        boolean isUser = newType == UserOrGroup.User;
        getTable().ensureColumnVisible(firstNameColumn, constants.firstnameUser(), isUser, "150px"); //$NON-NLS-1$
        getTable().ensureColumnVisible(groupNameColumn, constants.groupNameUser(), !isUser, "150px"); //$NON-NLS-1$
        getTable().ensureColumnVisible(lastNameColumn, constants.lastNameUser(), isUser, "150px"); //$NON-NLS-1$
        getTable().ensureColumnVisible(userNameColumn, constants.userNameUser(), isUser, "150px"); //$NON-NLS-1$
        getTable().ensureColumnVisible(authzColumn, constants.authz(), isUser, "150px"); //$NON-NLS-1$
        getTable().ensureColumnVisible(namespaceColumn, constants.namespace(), isUser, "150px"); //$NON-NLS-1$
        getTable().ensureColumnVisible(emailColumn, constants.emailUser(), isUser);
        updateSearchString(newType);
        userTypes.updateSelectedValue(newType, false);
    }

    private void updateSearchString(UserOrGroup userType) {
        final String usersSearchPrefix = "Users:"; //$NON-NLS-1$
        final String space = " "; //$NON-NLS-1$
        final String empty = ""; //$NON-NLS-1$
        final String colon = ":"; //$NON-NLS-1$

        String inputSearchString = commonModelProvider.get().getSearchString() != null
                ? commonModelProvider.get().getSearchString().trim().toLowerCase() : "";
        String inputSearchStringPrefix = commonModelProvider.get().getSearchStringPrefix() != null
                ? commonModelProvider.get().getSearchStringPrefix().trim().toLowerCase() : "";

        if (!inputSearchString.isEmpty() && inputSearchStringPrefix.isEmpty()) {
            int indexOfColon = inputSearchString.indexOf(colon);
            inputSearchStringPrefix = inputSearchString.substring(0, indexOfColon + 1).trim();
            inputSearchString = inputSearchString.substring(indexOfColon + 1).trim();
        }
        if (inputSearchStringPrefix.isEmpty()) {
            inputSearchStringPrefix = usersSearchPrefix;
            inputSearchString = empty;
        }

        //Strip out all '(and|or) type=X' patterns.
        inputSearchString = searchPatternDiskTypeClause
                .replace(inputSearchString, empty).trim();
        inputSearchStringPrefix = searchPatternDiskTypeClause
                .replace(inputSearchStringPrefix, empty).trim();

        inputSearchStringPrefix += VdcUserConditionFieldAutoCompleter.TYPE.toLowerCase()
                + " = " + userType.name().toLowerCase() + space; //$NON-NLS-1$
        commonModelProvider.get().setSearchStringPrefix(inputSearchStringPrefix);
        commonModelProvider.get().setSearchString(inputSearchString);

        getTable().getSelectionModel().clear();
        getMainModel().setUserOrGroup(userType);
        getMainModel().setItems(null);
        getMainModel().setSearchString(commonModelProvider.get().getEffectiveSearchString());
        getMainModel().search();
    }

}
