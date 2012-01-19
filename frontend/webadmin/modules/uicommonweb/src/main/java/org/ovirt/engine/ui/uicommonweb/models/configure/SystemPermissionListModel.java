package org.ovirt.engine.ui.uicommonweb.models.configure;

import org.ovirt.engine.core.common.action.PermissionsOperationsParametes;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.DbUser;
import org.ovirt.engine.core.common.businessentities.ad_groups;
import org.ovirt.engine.core.common.businessentities.permissions;
import org.ovirt.engine.core.common.businessentities.roles;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.users.VdcUser;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicommonweb.models.users.AdElementListModel;
import org.ovirt.engine.ui.uicompat.FrontendMultipleActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendMultipleActionAsyncCallback;

@SuppressWarnings("unused")
public class SystemPermissionListModel extends SearchableListModel
{

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

    public SystemPermissionListModel()
    {
        setTitle("System Permission");

        setAddCommand(new UICommand("Add", this));
        setRemoveCommand(new UICommand("Remove", this));

        setSearchPageSize(1000);

        UpdateActionAvailability();
    }

    @Override
    protected void AsyncSearch()
    {
        super.AsyncSearch();

        setAsyncResult(Frontend.RegisterQuery(VdcQueryType.GetSystemPermissions, new VdcQueryParametersBase()));
        setItems(getAsyncResult().getData());
    }

    @Override
    protected void SyncSearch()
    {
        super.SyncSearch();

        AsyncQuery _asyncQuery = new AsyncQuery();
        _asyncQuery.setModel(this);
        _asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void OnSuccess(Object model, Object ReturnValue)
            {
                SystemPermissionListModel systemPermissionListModel = (SystemPermissionListModel) model;
                systemPermissionListModel.setItems((Iterable) ((VdcQueryReturnValue) ReturnValue).getReturnValue());
            }
        };
        VdcQueryParametersBase params = new VdcQueryParametersBase();
        params.setRefresh(false);

        Frontend.RunQuery(VdcQueryType.GetSystemPermissions, params, _asyncQuery);
    }

    private void UpdateActionAvailability()
    {
        getRemoveCommand().setIsExecutionAllowed(getSelectedItems() != null && getSelectedItems().size() > 0);
    }

    @Override
    protected void OnSelectedItemChanged()
    {
        super.OnSelectedItemChanged();
        UpdateActionAvailability();
    }

    @Override
    protected void SelectedItemsChanged()
    {
        super.SelectedItemsChanged();
        UpdateActionAvailability();
    }

    private void add()
    {
        if (getWindow() != null)
        {
            return;
        }

        AdElementListModel model = new AdElementListModel();
        model.getIsEveryoneSelectionHidden().setEntity(true);
        setWindow(model);
        model.setTitle("Add System Permission to User");
        model.setHashName("add_system_permission_to_user");
        // model.Role.IsAvailable = true;
        // model.ExcludeItems = Items;

        UICommand tempVar = new UICommand("OnAttach", this);
        tempVar.setTitle("OK");
        tempVar.setIsDefault(true);
        model.getCommands().add(tempVar);
        UICommand tempVar2 = new UICommand("Cancel", this);
        tempVar2.setTitle("Cancel");
        tempVar2.setIsCancel(true);
        model.getCommands().add(tempVar2);
    }

    private void OnAttach()
    {
        AdElementListModel model = (AdElementListModel) getWindow();

        if (model.getProgress() != null)
        {
            return;
        }

        if (model.getSelectedItems() == null)
        {
            Cancel();
            return;
        }

        java.util.ArrayList<DbUser> items = new java.util.ArrayList<DbUser>();
        for (Object item : model.getItems())
        {
            EntityModel entityModel = (EntityModel) item;
            if (entityModel.getIsSelected())
            {
                items.add((DbUser) entityModel.getEntity());
            }
        }

        roles role = (roles) model.getRole().getSelectedItem();

        java.util.ArrayList<VdcActionParametersBase> list = new java.util.ArrayList<VdcActionParametersBase>();
        for (DbUser user : items)
        {
            permissions tempVar = new permissions();
            tempVar.setad_element_id(user.getuser_id());
            tempVar.setrole_id(role.getId());
            permissions perm = tempVar;

            if (user.getIsGroup())
            {
                PermissionsOperationsParametes tempVar2 = new PermissionsOperationsParametes();
                tempVar2.setPermission(perm);
                tempVar2.setAdGroup(new ad_groups(user.getuser_id(), user.getname(), user.getdomain()));
                list.add(tempVar2);
            }
            else
            {
                PermissionsOperationsParametes tempVar3 = new PermissionsOperationsParametes();
                tempVar3.setPermission(perm);
                tempVar3.setVdcUser(new VdcUser(user.getuser_id(), user.getusername(), user.getdomain()));
                list.add(tempVar3);
            }
        }

        model.StartProgress(null);

        Frontend.RunMultipleAction(VdcActionType.AddSystemPermission, list,
                new IFrontendMultipleActionAsyncCallback() {
                    @Override
                    public void Executed(FrontendMultipleActionAsyncResult result) {

                        AdElementListModel localModel = (AdElementListModel) result.getState();
                        localModel.StopProgress();
                        Cancel();

                    }
                }, model);
    }

    private void OnSave()
    {
    }

    public void Cancel()
    {
        setWindow(null);
    }

    private void remove()
    {
        if (getWindow() != null)
        {
            return;
        }

        ConfirmationModel model = new ConfirmationModel();
        setWindow(model);
        model.setTitle("Remove System Permission(s)");
        model.setHashName("remove_system_permission");
        model.setMessage("System Permission(s):");

        java.util.ArrayList<String> list = new java.util.ArrayList<String>();
        for (Object item : getSelectedItems())
        {
            permissions permission = (permissions) item;
            list.add("User: " + permission.getOwnerName() + " with Role: " + permission.getRoleName());
        }
        model.setItems(list);

        UICommand tempVar = new UICommand("OnRemove", this);
        tempVar.setTitle("OK");
        tempVar.setIsDefault(true);
        model.getCommands().add(tempVar);
        UICommand tempVar2 = new UICommand("Cancel", this);
        tempVar2.setTitle("Cancel");
        tempVar2.setIsCancel(true);
        model.getCommands().add(tempVar2);
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

            java.util.ArrayList<VdcActionParametersBase> list = new java.util.ArrayList<VdcActionParametersBase>();
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
    }

    @Override
    public void ExecuteCommand(UICommand command)
    {
        super.ExecuteCommand(command);

        if (command == getAddCommand())
        {
            add();
        }
        else if (StringHelper.stringsEqual(command.getName(), "OnSave"))
        {
            OnSave();
        }
        else if (StringHelper.stringsEqual(command.getName(), "OnAttach"))
        {
            OnAttach();
        }
        else if (command == getRemoveCommand())
        {
            remove();
        }
        else if (StringHelper.stringsEqual(command.getName(), "OnRemove"))
        {
            OnRemove();
        }
        else if (StringHelper.stringsEqual(command.getName(), "Cancel"))
        {
            Cancel();
        }
    }

    @Override
    protected String getListName() {
        return "SystemPermissionListModel";
    }
}
