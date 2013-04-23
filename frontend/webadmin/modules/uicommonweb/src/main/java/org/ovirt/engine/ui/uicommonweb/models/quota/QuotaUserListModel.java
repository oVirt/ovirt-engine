package org.ovirt.engine.ui.uicommonweb.models.quota;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.PermissionsOperationsParametes;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.DbUser;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.LdapGroup;
import org.ovirt.engine.core.common.businessentities.permissions;
import org.ovirt.engine.core.common.queries.GetEntitiesRelatedToQuotaIdParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.users.VdcUser;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.auth.ApplicationGuids;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicommonweb.models.users.AdElementListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.FrontendMultipleActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendMultipleActionAsyncCallback;

public class QuotaUserListModel extends SearchableListModel {

    public QuotaUserListModel() {
        setTitle(ConstantsManager.getInstance().getConstants().usersTitle());
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
    protected void SyncSearch() {
        super.SyncSearch();
        GetEntitiesRelatedToQuotaIdParameters param = new GetEntitiesRelatedToQuotaIdParameters();
        param.setQuotaId(((Quota) getEntity()).getId());
        param.setRefresh(getIsQueryFirstTime());

        AsyncQuery _asyncQuery = new AsyncQuery();
        _asyncQuery.setModel(this);
        _asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object ReturnValue)
            {
                SearchableListModel searchableListModel = (SearchableListModel) model;
                ArrayList<permissions> list =
                        (ArrayList<permissions>) ((VdcQueryReturnValue) ReturnValue).getReturnValue();
                Map<Guid, permissions> map = new HashMap<Guid, permissions>();
                for (permissions permission : list) {
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
                for (permissions permission : map.values()) {
                    list.add(permission);
                }
                searchableListModel.setItems(list);
            }
        };

        param.setRefresh(getIsQueryFirstTime());

        Frontend.RunQuery(VdcQueryType.GetPermissionsToConsumeQuotaByQuotaId, param, _asyncQuery);

        setIsQueryFirstTime(false);
    }

    @Override
    protected void onEntityChanged()
    {
        super.onEntityChanged();
        if (getEntity() == null) {
            return;
        }
        getSearchCommand().Execute();
        updateActionAvailability();
    }

    @Override
    protected void onSelectedItemChanged() {
        super.onSelectedItemChanged();
        updateActionAvailability();
    }

    @Override
    protected String getListName() {
        return "QuotaUserListModel"; //$NON-NLS-1$
    }

    private void updateActionAvailability() {
        ArrayList<permissions> items =
                (((ArrayList<permissions>) getSelectedItems()) != null) ? (ArrayList<permissions>) getSelectedItems()
                        : new ArrayList<permissions>();

        boolean removeExe = false;
        if (items.size() > 0) {
            removeExe = true;
        }
        for (permissions perm : items) {
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
        model.setHashName("assign_users_and_groups_to_quota"); //$NON-NLS-1$
        model.setIsRoleListHidden(true);
        model.getIsEveryoneSelectionHidden().setEntity(false);

        UICommand tempVar = new UICommand("OnAdd", this); //$NON-NLS-1$
        tempVar.setTitle(ConstantsManager.getInstance().getConstants().ok());
        tempVar.setIsDefault(true);
        model.getCommands().add(tempVar);
        UICommand tempVar2 = new UICommand("Cancel", this); //$NON-NLS-1$
        tempVar2.setTitle(ConstantsManager.getInstance().getConstants().cancel());
        tempVar2.setIsCancel(true);
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
        model.setHashName("remove_quota_assignment_from_user"); //$NON-NLS-1$
        model.setMessage(ConstantsManager.getInstance().getConstants().assignmentsMsg());

        ArrayList<String> list = new ArrayList<String>();
        for (permissions item : Linq.<permissions> Cast(getSelectedItems()))
        {
            list.add(item.getOwnerName());
        }
        model.setItems(list);

        UICommand tempVar = new UICommand("OnRemove", this); //$NON-NLS-1$
        tempVar.setTitle(ConstantsManager.getInstance().getConstants().ok());
        tempVar.setIsDefault(true);
        model.getCommands().add(tempVar);
        UICommand tempVar2 = new UICommand("Cancel", this); //$NON-NLS-1$
        tempVar2.setTitle(ConstantsManager.getInstance().getConstants().cancel());
        tempVar2.setIsCancel(true);
        model.getCommands().add(tempVar2);
    }

    private void Cancel() {
        setWindow(null);
    }

    public void OnAdd()
    {
        AdElementListModel model = (AdElementListModel) getWindow();

        if (model.getProgress() != null)
        {
            return;
        }

        if (model.getSelectedItems() == null && !model.getIsEveryoneSelected())
        {
            Cancel();
            return;
        }
        ArrayList<DbUser> items = new ArrayList<DbUser>();
        if (model.getIsEveryoneSelected())
        {
            DbUser tempVar = new DbUser();
            tempVar.setuser_id(ApplicationGuids.everyone.asGuid());
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

        model.StartProgress(null);

        ArrayList<VdcActionParametersBase> list = new ArrayList<VdcActionParametersBase>();
        PermissionsOperationsParametes permissionParams;
        for (DbUser user : items)
        {
            permissions tempVar2 = new permissions();
            tempVar2.setad_element_id(user.getuser_id());
            tempVar2.setrole_id(ApplicationGuids.quotaConsumer.asGuid());
            permissions perm = tempVar2;
            perm.setObjectId(((Quota) getEntity()).getId());
            perm.setObjectType(VdcObjectType.Quota);

            permissionParams = new PermissionsOperationsParametes();
            if (user.getIsGroup())
            {
                permissionParams.setAdGroup(new LdapGroup(user.getuser_id(), user.getname(), user.getdomain()));
            }
            else
            {
                permissionParams.setVdcUser(new VdcUser(user.getuser_id(), user.getusername(), user.getdomain()));
            }
            permissionParams.setPermission(perm);
            list.add(permissionParams);
        }

        Frontend.RunMultipleAction(VdcActionType.AddPermission, list,
                new IFrontendMultipleActionAsyncCallback() {
                    @Override
                    public void Executed(FrontendMultipleActionAsyncResult result) {

                        QuotaUserListModel localModel = (QuotaUserListModel) result.getState();
                        localModel.StopProgress();
                        Cancel();

                    }
                }, model);
        Cancel();
    }

    private void OnRemove()
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
                PermissionsOperationsParametes tempVar = new PermissionsOperationsParametes();
                tempVar.setPermission((permissions) perm);
                list.add(tempVar);
            }

            model.StartProgress(null);

            Frontend.RunMultipleAction(VdcActionType.RemovePermission, list,
                    new IFrontendMultipleActionAsyncCallback() {
                        @Override
                        public void Executed(FrontendMultipleActionAsyncResult result) {

                            ConfirmationModel localModel = (ConfirmationModel) result.getState();
                            localModel.StopProgress();
                            Cancel();

                        }
                    }, model);
        }

        Cancel();
    }

    @Override
    public void ExecuteCommand(UICommand command)
    {
        super.ExecuteCommand(command);

        if (command == getAddCommand())
        {
            add();
        }
        if (command == getRemoveCommand())
        {
            remove();
        }

        if (StringHelper.stringsEqual(command.getName(), "Cancel")) //$NON-NLS-1$
        {
            Cancel();
        }
        if (StringHelper.stringsEqual(command.getName(), "OnAdd")) //$NON-NLS-1$
        {
            OnAdd();
        }
        if (StringHelper.stringsEqual(command.getName(), "OnRemove")) //$NON-NLS-1$
        {
            OnRemove();
        }
    }

}
