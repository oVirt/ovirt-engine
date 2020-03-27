package org.ovirt.engine.ui.uicommonweb.models.users;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.PermissionsOperationsParameters;
import org.ovirt.engine.core.common.businessentities.Permission;
import org.ovirt.engine.core.common.businessentities.Role;
import org.ovirt.engine.core.common.businessentities.aaa.DbGroup;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.ui.frontend.AsyncCallback;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.auth.ApplicationGuids;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.configure.PermissionListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

import com.google.inject.Provider;

public class UserPermissionListModel extends PermissionListModel<DbUser> {

    private UICommand addRoleToUserCommand;

    @Inject
    public UserPermissionListModel(Provider<AdElementListModel> adElementListModelProvider) {
        super(adElementListModelProvider);
        setTitle(ConstantsManager.getInstance().getConstants().permissionsTitle());
        setHelpTag(HelpTag.permissions);
        setHashName("permissions"); //$NON-NLS-1$

        setAddRoleToUserCommand(new UICommand("AddRoleToUser", this)); // $NON-NLS-1$
        getCommands().add(getAddRoleToUserCommand());
        updateActionAvailability();
    }

    @Override
    protected void onEntityChanged() {
        super.onEntityChanged();
        getSearchCommand().execute();
    }

    @Override
    public void search() {
        if (getEntity() != null) {
            super.search();
        }
    }

    @Override
    protected void syncSearch() {
        if (getEntity() == null) {
            return;
        }
        IdQueryParameters mlaParams = new IdQueryParameters(getEntity().getId());
        mlaParams.setRefresh(getIsQueryFirstTime());

        Frontend.getInstance().runQuery(QueryType.GetPermissionsOnBehalfByAdElementId, mlaParams, new AsyncQuery<>((AsyncCallback<QueryReturnValue>) returnValue -> {
            ArrayList<Permission> list = returnValue.getReturnValue();
            ArrayList<Permission> newList = new ArrayList<>();
            for (Permission permission : list) {
                if (!permission.getRoleId().equals(ApplicationGuids.quotaConsumer.asGuid())) {
                    newList.add(permission);
                }
            }
            setItems(newList);
        }));

        setIsQueryFirstTime(false);

    }

    public UICommand getAddRoleToUserCommand() {
        return addRoleToUserCommand;
    }

    private void setAddRoleToUserCommand(UICommand value) {
        addRoleToUserCommand = value;
    }

    public void remove() {
        if (getWindow() != null) {
            return;
        }

        ConfirmationModel model = new ConfirmationModel();
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().removePermissionTitle());
        model.setHelpTag(HelpTag.remove_permission);
        model.setHashName("remove_permission"); //$NON-NLS-1$
        model.setItems(new ArrayList<>(getSelectedItems()));

        UICommand tempVar = UICommand.createDefaultOkUiCommand("OnRemove", this); //$NON-NLS-1$
        model.getCommands().add(tempVar);
        UICommand tempVar2 = UICommand.createCancelUiCommand("Cancel", this); //$NON-NLS-1$
        model.getCommands().add(tempVar2);
    }

    private void onRemove() {
        if (getSelectedItems() != null && getSelectedItems().size() > 0) {
            ConfirmationModel model = (ConfirmationModel) getWindow();

            if (model.getProgress() != null) {
                return;
            }

            ArrayList<ActionParametersBase> list = new ArrayList<>();
            for (Object perm : getSelectedItems()) {
                PermissionsOperationsParameters tempVar = new PermissionsOperationsParameters();
                tempVar.setPermission((Permission) perm);
                list.add(tempVar);
            }

            model.startProgress();

            Frontend.getInstance().runMultipleAction(ActionType.RemovePermission, list,
                    result -> {

                        ConfirmationModel localModel = (ConfirmationModel) result.getState();
                        localModel.stopProgress();
                        cancel();

                    }, model);
        } else {
            cancel();
        }
    }

    public void cancel() {
        setWindow(null);
    }

    @Override
    protected void onSelectedItemChanged() {
        super.onSelectedItemChanged();
        updateActionAvailability();
    }

    @Override
    protected void selectedItemsChanged() {
        super.selectedItemsChanged();
        updateActionAvailability();
    }

    private void updateActionAvailability() {
        Permission p = getSelectedItem();
        if (p != null && ApplicationGuids.everyone.asGuid().equals(p.getAdElementId())) {
            getRemoveCommand().setIsExecutionAllowed(false);
        } else {
            boolean isInherited = p != null && getEntity() != null && !p.getAdElementId().equals(getEntity().getId());
            getRemoveCommand().setIsExecutionAllowed(!isInherited && (getSelectedItem() != null
                    || (getSelectedItems() != null && getSelectedItems().size() > 0)));
        }

        /**
         * User Permission uses the same action panel as all the permission models, but you can't add, so we need to
         * hide the add button.
         */
        getAddCommand().setIsAvailable(false);
    }

    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);

        if (command == getRemoveCommand()) {
            remove();
        } else if (command == getAddRoleToUserCommand()) {
            addRoleToUser();
        } else if ("OnRemove".equals(command.getName())) { //$NON-NLS-1$
            onRemove();
        } else if ("Cancel".equals(command.getName())) { //$NON-NLS-1$
            cancel();
        } else if ("OnAddRoleToUser".equals(command.getName())) { //$NON-NLS-1$
            onAddRoleToUser();
        }
    }

    private void onAddRoleToUser() {
        AdElementListModel model = (AdElementListModel) getWindow();

        if (model.getProgress() != null) {
            return;
        }


        List<Role> roles = model.getRole().getSelectedItems();
        if (roles == null || roles.isEmpty()) {
            model.setIsValid(false);
            model.setMessage(
                    ConstantsManager.getInstance().getConstants().selectRoleToAssign());
            return;
        }
        // adGroup/user

        DbUser user = getEntity();
        List<ActionParametersBase> permissionParamsList = new ArrayList<>();
        roles.forEach(role -> {
            PermissionsOperationsParameters permissionParams = new PermissionsOperationsParameters();
            Permission perm = new Permission(user.getId(), role.getId(), null, null);
            if (user.isGroup()) {
                DbGroup group = new DbGroup();
                group.setId(user.getId());
                group.setExternalId(user.getExternalId());
                group.setName(user.getFirstName());
                group.setDomain(user.getDomain());
                group.setNamespace(user.getNamespace());
                permissionParams.setPermission(perm);
                permissionParams.setGroup(group);
            } else {
                permissionParams.setPermission(perm);
                permissionParams.setUser(user);
            }
            permissionParamsList.add(permissionParams);
        });

        model.startProgress();

        Frontend.getInstance().runMultipleAction(ActionType.AddSystemPermission, permissionParamsList,
                result -> {

                    AdElementListModel localModel = (AdElementListModel) result.getState();
                    localModel.stopProgress();
                    cancel();

                }, model);
    }

    private void addRoleToUser() {
        if (getWindow() != null) {
            return;
        }

        AdElementListModel model = getAdElementListModelProvider().get();
        model.setSelectDefaultRole(false);
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().addSystemPermissionToUserTitle());
        model.setHelpTag(HelpTag.add_system_permission_to_user);
        model.setHashName("add_system_permission_to_user"); //$NON-NLS-1$

        model.getCommands().add(UICommand.createDefaultOkUiCommand("OnAddRoleToUser", this)); //$NON-NLS-1$
        model.addCancelCommand(this);
    }

    @Override
    protected String getListName() {
        return "UserPermissionListModel"; //$NON-NLS-1$
    }
}
