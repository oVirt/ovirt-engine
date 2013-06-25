package org.ovirt.engine.ui.uicommonweb.models.configure;

import java.util.ArrayList;

import org.ovirt.engine.core.common.action.PermissionsOperationsParametes;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.DbUser;
import org.ovirt.engine.core.common.businessentities.LdapGroup;
import org.ovirt.engine.core.common.businessentities.Role;
import org.ovirt.engine.core.common.businessentities.permissions;
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
import org.ovirt.engine.ui.uicompat.ConstantsManager;
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
        setTitle(ConstantsManager.getInstance().getConstants().systemPermissionTitle());

        setAddCommand(new UICommand("Add", this)); //$NON-NLS-1$
        setRemoveCommand(new UICommand("Remove", this)); //$NON-NLS-1$

        setSearchPageSize(1000);

        updateActionAvailability();
    }

    @Override
    protected void syncSearch()
    {
        super.syncSearch();

        AsyncQuery _asyncQuery = new AsyncQuery();
        _asyncQuery.setModel(this);
        _asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object ReturnValue)
            {
                SystemPermissionListModel systemPermissionListModel = (SystemPermissionListModel) model;
                systemPermissionListModel.setItems((Iterable) ((VdcQueryReturnValue) ReturnValue).getReturnValue());
            }
        };
        VdcQueryParametersBase params = new VdcQueryParametersBase();
        params.setRefresh(false);

        Frontend.RunQuery(VdcQueryType.GetSystemPermissions, params, _asyncQuery);
    }

    private void updateActionAvailability()
    {
        getRemoveCommand().setIsExecutionAllowed(getSelectedItems() != null && getSelectedItems().size() > 0);
    }

    @Override
    protected void onSelectedItemChanged()
    {
        super.onSelectedItemChanged();
        updateActionAvailability();
    }

    @Override
    protected void selectedItemsChanged()
    {
        super.selectedItemsChanged();
        updateActionAvailability();
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
        model.setTitle(ConstantsManager.getInstance().getConstants().addSystemPermissionToUserTitle());
        model.setHashName("add_system_permission_to_user"); //$NON-NLS-1$
        // model.Role.IsAvailable = true;
        // model.ExcludeItems = Items;

        UICommand tempVar = new UICommand("OnAttach", this); //$NON-NLS-1$
        tempVar.setTitle(ConstantsManager.getInstance().getConstants().ok());
        tempVar.setIsDefault(true);
        model.getCommands().add(tempVar);
        UICommand tempVar2 = new UICommand("Cancel", this); //$NON-NLS-1$
        tempVar2.setTitle(ConstantsManager.getInstance().getConstants().cancel());
        tempVar2.setIsCancel(true);
        model.getCommands().add(tempVar2);
    }

    private void onAttach()
    {
        AdElementListModel model = (AdElementListModel) getWindow();

        if (model.getProgress() != null)
        {
            return;
        }

        if (model.getSelectedItems() == null)
        {
            cancel();
            return;
        }

        ArrayList<DbUser> items = new ArrayList<DbUser>();
        for (Object item : model.getItems())
        {
            EntityModel entityModel = (EntityModel) item;
            if (entityModel.getIsSelected())
            {
                items.add((DbUser) entityModel.getEntity());
            }
        }

        Role role = (Role) model.getRole().getSelectedItem();

        ArrayList<VdcActionParametersBase> list = new ArrayList<VdcActionParametersBase>();
        for (DbUser user : items)
        {
            permissions tempVar = new permissions();
            tempVar.setad_element_id(user.getId());
            tempVar.setrole_id(role.getId());
            permissions perm = tempVar;

            if (user.isGroup())
            {
                PermissionsOperationsParametes tempVar2 = new PermissionsOperationsParametes();
                tempVar2.setPermission(perm);
                tempVar2.setAdGroup(new LdapGroup(user.getId(), user.getFirstName(), user.getDomain()));
                list.add(tempVar2);
            }
            else
            {
                PermissionsOperationsParametes tempVar3 = new PermissionsOperationsParametes();
                tempVar3.setPermission(perm);
                tempVar3.setVdcUser(new VdcUser(user.getId(), user.getLoginName(), user.getDomain()));
                list.add(tempVar3);
            }
        }

        model.startProgress(null);

        Frontend.RunMultipleAction(VdcActionType.AddSystemPermission, list,
                new IFrontendMultipleActionAsyncCallback() {
                    @Override
                    public void executed(FrontendMultipleActionAsyncResult result) {

                        AdElementListModel localModel = (AdElementListModel) result.getState();
                        localModel.stopProgress();
                        cancel();

                    }
                }, model);
    }

    private void onSave()
    {
    }

    public void cancel()
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
        model.setTitle(ConstantsManager.getInstance().getConstants().removeSystemPermissionsTitle());
        model.setHashName("remove_system_permission"); //$NON-NLS-1$
        model.setMessage(ConstantsManager.getInstance().getConstants().systemPermissionsMsg());

        ArrayList<String> list = new ArrayList<String>();
        for (Object item : getSelectedItems())
        {
            permissions permission = (permissions) item;
            list.add("User: " + permission.getOwnerName() + " with Role: " + permission.getRoleName()); //$NON-NLS-1$ //$NON-NLS-2$
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
                PermissionsOperationsParametes tempVar = new PermissionsOperationsParametes();
                tempVar.setPermission((permissions) perm);
                list.add(tempVar);
            }

            model.startProgress(null);

            Frontend.RunMultipleAction(VdcActionType.RemovePermission, list,
                    new IFrontendMultipleActionAsyncCallback() {
                        @Override
                        public void executed(FrontendMultipleActionAsyncResult result) {

                            ConfirmationModel localModel = (ConfirmationModel) result.getState();
                            localModel.stopProgress();
                            cancel();

                        }
                    }, model);
        }
    }

    @Override
    public void executeCommand(UICommand command)
    {
        super.executeCommand(command);

        if (command == getAddCommand())
        {
            add();
        }
        else if (StringHelper.stringsEqual(command.getName(), "OnSave")) //$NON-NLS-1$
        {
            onSave();
        }
        else if (StringHelper.stringsEqual(command.getName(), "OnAttach")) //$NON-NLS-1$
        {
            onAttach();
            getForceRefreshCommand().execute();
        }
        else if (command == getRemoveCommand())
        {
            remove();
        }
        else if (StringHelper.stringsEqual(command.getName(), "OnRemove")) //$NON-NLS-1$
        {
            onRemove();
        }
        else if (StringHelper.stringsEqual(command.getName(), "Cancel")) //$NON-NLS-1$
        {
            cancel();
        }
    }

    @Override
    protected String getListName() {
        return "SystemPermissionListModel"; //$NON-NLS-1$
    }
}
