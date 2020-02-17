package org.ovirt.engine.ui.uicommonweb.models.quota;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.PermissionsOperationsParameters;
import org.ovirt.engine.core.common.businessentities.Permission;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.aaa.DbGroup;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.auth.ApplicationGuids;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicommonweb.models.users.AdElementListModel;
import org.ovirt.engine.ui.uicommonweb.models.users.AdElementListModel.AdSearchType;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class QuotaUserListModel extends SearchableListModel<Quota, Permission> {

    public QuotaUserListModel() {
        setTitle(ConstantsManager.getInstance().getConstants().usersTitle());
        setHelpTag(HelpTag.users);
        setHashName("users"); //$NON-NLS-1$

        setAddCommand(new UICommand("Add", this)); //$NON-NLS-1$
        setRemoveCommand(new UICommand("Remove", this)); //$NON-NLS-1$

        updateActionAvailability();
    }

    private UICommand privateAddCommand;

    public UICommand getAddCommand() {
        return privateAddCommand;
    }

    private void setAddCommand(UICommand value) {
        privateAddCommand = value;
    }

    private UICommand privateRemoveCommand;

    public UICommand getRemoveCommand() {
        return privateRemoveCommand;
    }

    private void setRemoveCommand(UICommand value) {
        privateRemoveCommand = value;
    }

    @Override
    protected void syncSearch() {
        super.syncSearch();
        IdQueryParameters param = new IdQueryParameters(getEntity().getId());
        param.setRefresh(getIsQueryFirstTime());

        param.setRefresh(getIsQueryFirstTime());

        Frontend.getInstance().runQuery(QueryType.GetPermissionsToConsumeQuotaByQuotaId, param, new AsyncQuery<QueryReturnValue>(returnValue -> {
            ArrayList<Permission> list = returnValue.getReturnValue();
            Map<Guid, Permission> map = new HashMap<>();
            for (Permission permission : list) {
                //filter out sys-admin and dc admin from consumers sub-tab
                if (permission.getRoleId().equals(ApplicationGuids.superUser.asGuid())
                        || permission.getRoleId().equals(ApplicationGuids.dataCenterAdmin.asGuid())) {
                    continue;
                }
                if (!map.containsKey(permission.getAdElementId())) {
                    map.put(permission.getAdElementId(), permission);
                } else {
                    if (map.get(permission.getAdElementId())
                            .getRoleId()
                            .equals(ApplicationGuids.quotaConsumer.asGuid())) {
                        map.put(permission.getAdElementId(), permission);
                    }
                }
            }
            list.clear();
            for (Permission permission : map.values()) {
                list.add(permission);
            }
            setItems(list);
        }));

        setIsQueryFirstTime(false);
    }

    @Override
    protected void onEntityChanged() {
        super.onEntityChanged();
        if (getEntity() == null) {
            return;
        }
        getSearchCommand().execute();
        updateActionAvailability();
    }

    @Override
    protected void selectedItemsChanged() {
        super.selectedItemsChanged();
        updateActionAvailability();
    }

    @Override
    protected String getListName() {
        return "QuotaUserListModel"; //$NON-NLS-1$
    }

    private void updateActionAvailability() {
        ArrayList<Permission> items =
                (getSelectedItems() != null) ? (ArrayList<Permission>) getSelectedItems()
                        : new ArrayList<Permission>();

        boolean removeExe = false;
        if (items.size() > 0) {
            removeExe = true;
        }
        for (Permission perm : items) {
            if (!perm.getRoleId().equals(ApplicationGuids.quotaConsumer.asGuid())) {
                removeExe = false;
                break;
            }
        }
        getRemoveCommand().setIsExecutionAllowed(removeExe);
    }

    public void add() {
        if (getWindow() != null) {
            return;
        }

        AdElementListModel model = new AdElementListModel();
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().assignUsersAndGroupsToQuotaTitle());
        model.setHelpTag(HelpTag.assign_users_and_groups_to_quota);
        model.setHashName("assign_users_and_groups_to_quota"); //$NON-NLS-1$
        model.setIsRoleListHidden(true);
        model.getIsEveryoneSelectionHidden().setEntity(false);

        model.addCommandOperatingOnSelectedItems(UICommand.createDefaultOkUiCommand("OnAdd", this)); //$NON-NLS-1$
        model.addCancelCommand(this);
    }

    public void remove() {
        if (getWindow() != null) {
            return;
        }

        ConfirmationModel model = new ConfirmationModel();
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().removeQuotaAssignmentFromUsersTitle());
        model.setHelpTag(HelpTag.remove_quota_assignment_from_user);
        model.setHashName("remove_quota_assignment_from_user"); //$NON-NLS-1$

        ArrayList<String> list = new ArrayList<>();
        for (Permission item : getSelectedItems()) {
            list.add(item.getOwnerName());
        }
        model.setItems(list);

        UICommand tempVar = UICommand.createDefaultOkUiCommand("OnRemove", this); //$NON-NLS-1$
        model.getCommands().add(tempVar);
        UICommand tempVar2 = UICommand.createCancelUiCommand("Cancel", this); //$NON-NLS-1$
        model.getCommands().add(tempVar2);
    }

    private void cancel() {
        setWindow(null);
    }

    public void onAdd() {
        AdElementListModel model = (AdElementListModel) getWindow();

        if (model.getProgress() != null) {
            return;
        }

        ArrayList<DbUser> items = new ArrayList<>();
        if (model.getSearchType() == AdSearchType.EVERYONE) {
            DbUser tempVar = new DbUser();
            tempVar.setId(ApplicationGuids.everyone.asGuid());
            items.add(tempVar);
        } else if (model.getItems() != null) {
            for (Object item : model.getItems()) {
                EntityModel entityModel = (EntityModel) item;
                if (entityModel.getIsSelected()) {
                    items.add((DbUser) entityModel.getEntity());
                }
            }
        }

        if(items.isEmpty()){
            model.setIsValid(false);
            model.setMessage(ConstantsManager.getInstance().getConstants().selectUserOrGroup());
            return;
        }

        model.startProgress();

        ArrayList<ActionParametersBase> list = new ArrayList<>();
        PermissionsOperationsParameters permissionParams;
        for (DbUser user : items) {
            Permission perm = new Permission(
                    user.getId(),
                    ApplicationGuids.quotaConsumer.asGuid(),
                    getEntity().getId(),
                    VdcObjectType.Quota);

            permissionParams = new PermissionsOperationsParameters();
            if (user.isGroup()) {
                DbGroup group = new DbGroup();
                group.setId(user.getId());
                group.setExternalId(user.getExternalId());
                group.setName(user.getFirstName());
                group.setDomain(user.getDomain());
                permissionParams.setGroup(group);
            } else {
                permissionParams.setUser(user);
            }
            permissionParams.setPermission(perm);
            list.add(permissionParams);
        }

        Frontend.getInstance()
                .runMultipleAction(ActionType.AddPermission,
                        list,
                        result -> {
                            model.stopProgress();
                            cancel();
                        });
        cancel();
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
        }

        cancel();
    }

    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);

        if (command == getAddCommand()) {
            add();
        }
        if (command == getRemoveCommand()) {
            remove();
        }

        if ("Cancel".equals(command.getName())) { //$NON-NLS-1$
            cancel();
        }
        if ("OnAdd".equals(command.getName())) { //$NON-NLS-1$
            onAdd();
        }
        if ("OnRemove".equals(command.getName())) { //$NON-NLS-1$
            onRemove();
        }
    }

}
