package org.ovirt.engine.ui.uicommonweb.models.quota;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.PermissionsOperationsParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.Permissions;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.aaa.DbGroup;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.auth.ApplicationGuids;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicommonweb.models.users.AdElementListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.FrontendMultipleActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendMultipleActionAsyncCallback;

public class QuotaUserListModel extends SearchableListModel<Quota, Permissions> {

    public QuotaUserListModel() {
        setTitle(ConstantsManager.getInstance().getConstants().usersTitle());
        setHelpTag(HelpTag.users);
        setHashName("users"); //$NON-NLS-1$

        setAddCommand(new UICommand("Add", this)); //$NON-NLS-1$
        setRemoveCommand(new UICommand("Remove", this)); //$NON-NLS-1$

        updateActionAvailability();
    }

    private UICommand privateAddCommand;

    public UICommand getAddCommand()
    {
        return privateAddCommand;
    }

    private void setAddCommand(UICommand value)
    {
        privateAddCommand = value;
    }

    private UICommand privateRemoveCommand;

    public UICommand getRemoveCommand()
    {
        return privateRemoveCommand;
    }

    private void setRemoveCommand(UICommand value)
    {
        privateRemoveCommand = value;
    }

    @Override
    protected void syncSearch() {
        super.syncSearch();
        IdQueryParameters param = new IdQueryParameters(getEntity().getId());
        param.setRefresh(getIsQueryFirstTime());

        AsyncQuery _asyncQuery = new AsyncQuery();
        _asyncQuery.setModel(this);
        _asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object ReturnValue)
            {
                ArrayList<Permissions> list = ((VdcQueryReturnValue) ReturnValue).getReturnValue();
                Map<Guid, Permissions> map = new HashMap<Guid, Permissions>();
                for (Permissions permission : list) {
                    //filter out sys-admin and dc admin from consumers sub-tab
                    if (permission.getrole_id().equals(ApplicationGuids.superUser.asGuid())
                            || permission.getrole_id().equals(ApplicationGuids.dataCenterAdmin.asGuid())) {
                        continue;
                    }
                    if (!map.containsKey(permission.getad_element_id())) {
                        map.put(permission.getad_element_id(), permission);
                    } else {
                        if (map.get(permission.getad_element_id())
                                .getrole_id()
                                .equals(ApplicationGuids.quotaConsumer.asGuid())) {
                            map.put(permission.getad_element_id(), permission);
                        }
                    }
                }
                list.clear();
                for (Permissions permission : map.values()) {
                    list.add(permission);
                }
                setItems(list);
            }
        };

        param.setRefresh(getIsQueryFirstTime());

        Frontend.getInstance().runQuery(VdcQueryType.GetPermissionsToConsumeQuotaByQuotaId, param, _asyncQuery);

        setIsQueryFirstTime(false);
    }

    @Override
    protected void onEntityChanged()
    {
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
        ArrayList<Permissions> items =
                (getSelectedItems() != null) ? (ArrayList<Permissions>) getSelectedItems()
                        : new ArrayList<Permissions>();

        boolean removeExe = false;
        if (items.size() > 0) {
            removeExe = true;
        }
        for (Permissions perm : items) {
            if (!perm.getrole_id().equals(ApplicationGuids.quotaConsumer.asGuid())) {
                removeExe = false;
                break;
            }
        }
        getRemoveCommand().setIsExecutionAllowed(removeExe);
    }

    public void add()
    {
        if (getWindow() != null)
        {
            return;
        }

        AdElementListModel model = new AdElementListModel();
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().assignUsersAndGroupsToQuotaTitle());
        model.setHelpTag(HelpTag.assign_users_and_groups_to_quota);
        model.setHashName("assign_users_and_groups_to_quota"); //$NON-NLS-1$
        model.setIsRoleListHidden(true);
        model.getIsEveryoneSelectionHidden().setEntity(false);

        UICommand tempVar = UICommand.createDefaultOkUiCommand("OnAdd", this); //$NON-NLS-1$
        model.getCommands().add(tempVar);
        UICommand tempVar2 = UICommand.createCancelUiCommand("Cancel", this); //$NON-NLS-1$
        model.getCommands().add(tempVar2);
    }

    public void remove()
    {
        if (getWindow() != null)
        {
            return;
        }

        ConfirmationModel model = new ConfirmationModel();
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().removeQuotaAssignmentFromUsersTitle());
        model.setHelpTag(HelpTag.remove_quota_assignment_from_user);
        model.setHashName("remove_quota_assignment_from_user"); //$NON-NLS-1$

        ArrayList<String> list = new ArrayList<String>();
        for (Permissions item : Linq.<Permissions> cast(getSelectedItems()))
        {
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

    public void onAdd()
    {
        AdElementListModel model = (AdElementListModel) getWindow();

        if (model.getProgress() != null)
        {
            return;
        }

        if (model.getSelectedItems() == null && !model.getIsEveryoneSelected())
        {
            cancel();
            return;
        }
        ArrayList<DbUser> items = new ArrayList<DbUser>();
        if (model.getIsEveryoneSelected())
        {
            DbUser tempVar = new DbUser();
            tempVar.setId(ApplicationGuids.everyone.asGuid());
            items.add(tempVar);
        }
        else {
            for (Object item : model.getItems())
            {
                EntityModel entityModel = (EntityModel) item;
                if (entityModel.getIsSelected())
                {
                    items.add((DbUser) entityModel.getEntity());
                }
            }
        }

        model.startProgress(null);

        ArrayList<VdcActionParametersBase> list = new ArrayList<VdcActionParametersBase>();
        PermissionsOperationsParameters permissionParams;
        for (DbUser user : items)
        {
            Permissions tempVar2 = new Permissions();
            tempVar2.setad_element_id(user.getId());
            tempVar2.setrole_id(ApplicationGuids.quotaConsumer.asGuid());
            Permissions perm = tempVar2;
            perm.setObjectId(getEntity().getId());
            perm.setObjectType(VdcObjectType.Quota);

            permissionParams = new PermissionsOperationsParameters();
            if (user.isGroup())
            {
                DbGroup group = new DbGroup();
                group.setId(user.getId());
                group.setExternalId(user.getExternalId());
                group.setName(user.getFirstName());
                group.setDomain(user.getDomain());
                permissionParams.setGroup(group);
            }
            else
            {
                permissionParams.setUser(user);
            }
            permissionParams.setPermission(perm);
            list.add(permissionParams);
        }

        Frontend.getInstance().runMultipleAction(VdcActionType.AddPermission, list,
                new IFrontendMultipleActionAsyncCallback() {
                    @Override
                    public void executed(FrontendMultipleActionAsyncResult result) {

                        QuotaUserListModel localModel = (QuotaUserListModel) result.getState();
                        localModel.stopProgress();
                        cancel();

                    }
                }, model);
        cancel();
    }

    private void onRemove()
    {
        if (getSelectedItems() != null && getSelectedItems().size() > 0)
        {
            ConfirmationModel model = (ConfirmationModel) getWindow();

            if (model.getProgress() != null)
            {
                return;
            }

            ArrayList<VdcActionParametersBase> list = new ArrayList<VdcActionParametersBase>();
            for (Object perm : getSelectedItems())
            {
                PermissionsOperationsParameters tempVar = new PermissionsOperationsParameters();
                tempVar.setPermission((Permissions) perm);
                list.add(tempVar);
            }

            model.startProgress(null);

            Frontend.getInstance().runMultipleAction(VdcActionType.RemovePermission, list,
                    new IFrontendMultipleActionAsyncCallback() {
                        @Override
                        public void executed(FrontendMultipleActionAsyncResult result) {

                            ConfirmationModel localModel = (ConfirmationModel) result.getState();
                            localModel.stopProgress();
                            cancel();

                        }
                    }, model);
        }

        cancel();
    }

    @Override
    public void executeCommand(UICommand command)
    {
        super.executeCommand(command);

        if (command == getAddCommand())
        {
            add();
        }
        if (command == getRemoveCommand())
        {
            remove();
        }

        if ("Cancel".equals(command.getName())) //$NON-NLS-1$
        {
            cancel();
        }
        if ("OnAdd".equals(command.getName())) //$NON-NLS-1$
        {
            onAdd();
        }
        if ("OnRemove".equals(command.getName())) //$NON-NLS-1$
        {
            onRemove();
        }
    }

}
