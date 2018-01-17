package org.ovirt.engine.ui.webadmin.section.main.view;

import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.searchbackend.VdcUserConditionFieldAutoCompleter;
import org.ovirt.engine.core.searchbackend.VdcUserConditionFieldAutoCompleter.UserOrGroup;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.presenter.FragmentParams;
import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.common.widget.table.column.AbstractLinkColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractTextColumn;
import org.ovirt.engine.ui.common.widget.uicommon.users.UserTypeChangeHandler;
import org.ovirt.engine.ui.common.widget.uicommon.users.UsersTypeRadioGroup;
import org.ovirt.engine.ui.uicommonweb.models.users.UserListModel;
import org.ovirt.engine.ui.uicommonweb.place.WebAdminApplicationPlaces;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.MainUserPresenter;
import org.ovirt.engine.ui.webadmin.widget.table.column.UserStatusColumn;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;
import com.google.inject.Inject;

public class MainUserView extends AbstractMainWithDetailsTableView<DbUser, UserListModel>
    implements MainUserPresenter.ViewDef, UserTypeChangeHandler {

    interface ViewIdHandler extends ElementIdHandler<MainUserView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    private static final ApplicationConstants constants = AssetProvider.getConstants();

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
    public MainUserView(MainModelProvider<DbUser, UserListModel> modelProvider) {
        super(modelProvider);
        ViewIdHandler.idHandler.generateAndSetIds(this);
        initTable();
        initTableOverhead();
        initWidget(getTable());
        userTypeChanged(UserOrGroup.User);
    }

    private void initTableOverhead() {
        userTypes = new UsersTypeRadioGroup();
        userTypes.addChangeHandler(this);
        getTable().setTableOverhead(userTypes);
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

        groupNameColumn = new AbstractLinkColumn<DbUser>(new FieldUpdater<DbUser, String>() {
            @Override
            public void update(int index, DbUser user, String value) {
                Map<String, String> parameters = new HashMap<>();
                parameters.put(FragmentParams.NAME.getName(), user.getName());
                //The link was clicked, now fire an event to switch to details.
                getPlaceTransitionHandler().handlePlaceTransition(
                        WebAdminApplicationPlaces.userGeneralSubTabPlace, parameters);
            }
        }) {
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

        userNameColumn = new AbstractLinkColumn<DbUser>(new FieldUpdater<DbUser, String>() {
            @Override
            public void update(int index, DbUser user, String value) {
                Map<String, String> parameters = new HashMap<>();
                parameters.put(FragmentParams.NAME.getName(), user.getName());
                //The link was clicked, now fire an event to switch to details.
                getPlaceTransitionHandler().handlePlaceTransition(
                        WebAdminApplicationPlaces.userGeneralSubTabPlace, parameters);
            }
        }) {
            @Override
            public String getValue(DbUser object) {
                return object.getLoginName();
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
        final String usersSearchPrefix = "users:"; //$NON-NLS-1$

        String inputSearchString = getMainModel().getSearchString() != null
                ? getMainModel().getSearchString().trim() : "";
        String inputSearchStringPrefix = getMainModel().getDefaultSearchString() != null
                ? getMainModel().getDefaultSearchString().trim().toLowerCase() : "";
        String userSearchString = inputSearchString.substring(inputSearchStringPrefix.length());

        inputSearchStringPrefix = usersSearchPrefix + VdcUserConditionFieldAutoCompleter.TYPE.toLowerCase()
                + " = " + userType.name().toLowerCase() + " "; //$NON-NLS-1$ $NON-NLS-2$
        if (inputSearchString.equals(usersSearchPrefix.toLowerCase())) {
            inputSearchString = inputSearchStringPrefix;
        } else {
            inputSearchString = inputSearchStringPrefix + userSearchString;
        }
        getMainModel().setDefaultSearchString(inputSearchStringPrefix);
        getMainModel().setSearchString(inputSearchString);

        getTable().getSelectionModel().clear();
        getMainModel().setUserOrGroup(userType);
        getMainModel().setItems(null);
        getMainModel().search();
    }

}
