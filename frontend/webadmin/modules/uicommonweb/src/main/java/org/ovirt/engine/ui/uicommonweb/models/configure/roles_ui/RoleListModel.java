package org.ovirt.engine.ui.uicommonweb.models.configure.roles_ui;

import java.util.ArrayList;

import org.ovirt.engine.core.common.action.ActionGroupsToRoleParameter;
import org.ovirt.engine.core.common.action.RoleWithActionGroupsParameters;
import org.ovirt.engine.core.common.action.RolesOperationsParameters;
import org.ovirt.engine.core.common.action.RolesParameterBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.RoleType;
import org.ovirt.engine.core.common.businessentities.roles;
import org.ovirt.engine.core.common.queries.MultilevelAdministrationByRoleIdParameters;
import org.ovirt.engine.core.common.queries.MultilevelAdministrationsQueriesParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.ObservableCollection;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListWithDetailsModel;
import org.ovirt.engine.ui.uicommonweb.models.common.SelectionTreeNodeModel;
import org.ovirt.engine.ui.uicompat.FrontendActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;

@SuppressWarnings("unused")
public class RoleListModel extends ListWithDetailsModel
{
    private static final String COPY_OF = "Copy_of_";

    private enum CommandType
    {
        New,
        Edit,
        Clone;

        public int getValue()
        {
            return this.ordinal();
        }

        public static CommandType forValue(int value)
        {
            return values()[value];
        }
    }

    @Override
    public void setSelectedItem(Object value) {
        // TODO Auto-generated method stub
        super.setSelectedItem(value);
    }

    private UICommand privateNewCommand;

    public UICommand getNewCommand()
    {
        return privateNewCommand;
    }

    private void setNewCommand(UICommand value)
    {
        privateNewCommand = value;
    }

    private UICommand privateEditCommand;

    public UICommand getEditCommand()
    {
        return privateEditCommand;
    }

    private void setEditCommand(UICommand value)
    {
        privateEditCommand = value;
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

    private UICommand privateCloneCommand;

    public UICommand getCloneCommand()
    {
        return privateCloneCommand;
    }

    private void setCloneCommand(UICommand value)
    {
        privateCloneCommand = value;
    }

    private UICommand privateSearchAllRolesCommand;

    public UICommand getSearchAllRolesCommand()
    {
        return privateSearchAllRolesCommand;
    }

    private void setSearchAllRolesCommand(UICommand value)
    {
        privateSearchAllRolesCommand = value;
    }

    private UICommand privateSearchAdminRolesCommand;

    public UICommand getSearchAdminRolesCommand()
    {
        return privateSearchAdminRolesCommand;
    }

    private void setSearchAdminRolesCommand(UICommand value)
    {
        privateSearchAdminRolesCommand = value;
    }

    private UICommand privateSearchUserRolesCommand;

    public UICommand getSearchUserRolesCommand()
    {
        return privateSearchUserRolesCommand;
    }

    private void setSearchUserRolesCommand(UICommand value)
    {
        privateSearchUserRolesCommand = value;
    }

    private CommandType commandType = CommandType.values()[0];
    public java.util.ArrayList<ActionGroup> publicAttachedActions;
    public java.util.ArrayList<ActionGroup> detachActionGroup;
    public java.util.ArrayList<ActionGroup> attachActionGroup;
    public roles role;
    private RoleType privateItemsFilter;

    public RoleType getItemsFilter()
    {
        return privateItemsFilter;
    }

    public void setItemsFilter(RoleType value)
    {
        privateItemsFilter = value;
    }

    public RoleListModel()
    {
        setTitle("Roles");

        setNewCommand(new UICommand("New", this));
        setEditCommand(new UICommand("Edit", this));
        setRemoveCommand(new UICommand("Remove", this));
        setCloneCommand(new UICommand("Clone", this));
        setSearchAllRolesCommand(new UICommand("SearchAllRoles", this));
        setSearchAdminRolesCommand(new UICommand("SearchAdminRoles", this));
        setSearchUserRolesCommand(new UICommand("SearchUserRoles", this));

        setSearchPageSize(1000);

        UpdateActionAvailability();
    }

    @Override
    protected void InitDetailModels()
    {
        super.InitDetailModels();

        ObservableCollection<EntityModel> list = new ObservableCollection<EntityModel>();
        list.add(new RolePermissionListModel());

        setDetailModels(list);
    }

    @Override
    protected void AsyncSearch()
    {
        super.AsyncSearch();

        AsyncQuery _asyncQuery = new AsyncQuery();
        _asyncQuery.setModel(this);
        _asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void OnSuccess(Object model, Object ReturnValue)
            {
                RoleListModel roleListModel = (RoleListModel) model;
                java.util.ArrayList<roles> filteredList = new java.util.ArrayList<roles>();
                for (roles item : (java.util.ArrayList<roles>) ((VdcQueryReturnValue) ReturnValue).getReturnValue())
                {
                    if (roleListModel.getItemsFilter() == null || roleListModel.getItemsFilter() == item.getType())
                    {
                        filteredList.add(item);
                    }
                }

                roleListModel.setItems(filteredList);
            }
        };

        Frontend.RunQuery(VdcQueryType.GetAllRoles, new MultilevelAdministrationsQueriesParameters(), _asyncQuery);
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
                RoleListModel roleListModel = (RoleListModel) model;
                java.util.ArrayList<roles> filteredList = new java.util.ArrayList<roles>();
                for (roles item : (java.util.ArrayList<roles>) ((VdcQueryReturnValue) ReturnValue).getReturnValue())
                {
                    if (roleListModel.getItemsFilter() == null || roleListModel.getItemsFilter() == item.getType())
                    {
                        filteredList.add(item);
                    }
                }

                roleListModel.setItems(filteredList);
            }
        };

        MultilevelAdministrationsQueriesParameters tempVar = new MultilevelAdministrationsQueriesParameters();
        tempVar.setRefresh(getIsQueryFirstTime());
        Frontend.RunQuery(VdcQueryType.GetAllRoles, tempVar, _asyncQuery);
    }

    private void SearchAllRoles()
    {
        setItemsFilter(null);
        getSearchCommand().Execute();
    }

    private void SearchUserRoles()
    {
        setItemsFilter(RoleType.USER);
        getSearchCommand().Execute();
    }

    private void SearchAdminRoles()
    {
        setItemsFilter(RoleType.ADMIN);
        getSearchCommand().Execute();
    }

    public void remove()
    {
        if (getWindow() != null)
        {
            return;
        }

        ConfirmationModel model = new ConfirmationModel();
        setWindow(model);
        model.setTitle("Remove Role(s)");
        model.setHashName("remove_role");
        model.setMessage("Role(s):");

        java.util.ArrayList<String> list = new java.util.ArrayList<String>();
        for (roles role : Linq.<roles> Cast(getSelectedItems()))
        {
            list.add(role.getname());
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

    public void OnRemove()
    {
        for (Object item : getSelectedItems())
        {
            roles role = (roles) item;
            Frontend.RunAction(VdcActionType.RemoveRole, new RolesParameterBase(role.getId()));
        }

        Cancel();

        // Execute search to keep list updated.
        getSearchCommand().Execute();
    }

    public void Edit()
    {
        commandType = CommandType.Edit;
        roles role = (roles) getSelectedItem();
        InitRoleDialog(role);
    }

    public void New()
    {
        commandType = CommandType.New;
        roles role = new roles();
        InitRoleDialog(role);
    }

    public void CloneRole()
    {
        commandType = CommandType.Clone;
        roles role = (roles) getSelectedItem();
        InitRoleDialog(role);
    }

    @Override
    public void eventRaised(Event ev, Object sender, EventArgs args)
    {
        super.eventRaised(ev, sender, args);

        if (getWindow() != null
                && sender == ((RoleModel) getWindow()).getIsAdminRole()) {
            if (commandType == CommandType.New) {
                setAttachedActionGroups(new java.util.ArrayList<ActionGroup>());
            } else {

                AsyncQuery _asyncQuery = new AsyncQuery();
                _asyncQuery.setModel(this);
                _asyncQuery.asyncCallback = new INewAsyncCallback() {
                    @Override
                    public void OnSuccess(Object model, Object ReturnValue) {
                        RoleListModel roleListModel = (RoleListModel) model;
                        roleListModel.publicAttachedActions =
                                (ArrayList<ActionGroup>) ((VdcQueryReturnValue) ReturnValue).getReturnValue();
                        roleListModel.setAttachedActionGroups(publicAttachedActions);
                    }
                };
                roles role = (roles) getSelectedItem();
                Frontend.RunQuery(
                        VdcQueryType.GetRoleActionGroupsByRoleId,
                        new MultilevelAdministrationByRoleIdParameters(role
                                .getId()), _asyncQuery);
            }

        }
    }

    void setAttachedActionGroups(ArrayList<ActionGroup> attachedActions) {
        roles role = (roles) getSelectedItem();
        RoleModel model = (RoleModel) getWindow();
        ArrayList<SelectionTreeNodeModel> selectionTree =
                RoleTreeView.GetRoleTreeView((model.getIsNew() ? false : role.getis_readonly()),
                        (Boolean) model.getIsAdminRole().getEntity());
        for (SelectionTreeNodeModel sm : selectionTree)
        {
            for (SelectionTreeNodeModel smChild : sm.getChildren())
            {
                smChild.setParent(sm);
                smChild.setIsSelectedNotificationPrevent(false);

                for (SelectionTreeNodeModel smGrandChild : smChild.getChildren())
                {
                    smGrandChild.setParent(smChild);
                    smGrandChild.setIsSelectedNotificationPrevent(false);

                    if (attachedActions.contains(ActionGroup.valueOf(smGrandChild.getTitle())))
                    {
                        smGrandChild.setIsSelectedNullable(true);
                        smGrandChild.UpdateParentSelection();
                    }

                    if (smChild.getChildren().get(0).equals(smGrandChild))
                    {
                        smGrandChild.UpdateParentSelection();
                    }
                }
            }
        }
        model.setPermissionGroupModels(selectionTree);
    }

    private void InitRoleDialog(roles role)
    {
        if (getWindow() != null)
        {
            return;
        }

        RoleModel model = new RoleModel();
        setWindow(model);
        model.setIsNew(commandType != CommandType.Edit);
        if (commandType == CommandType.New)
        {
            role.setType(RoleType.USER);
        }
        model.getIsAdminRole().getEntityChangedEvent().addListener(this);
        model.getIsAdminRole().setEntity(RoleType.ADMIN.equals(role.getType()));
        model.getName().setEntity(role.getname());
        if (commandType == CommandType.Clone)
        {
            model.getName().setEntity(COPY_OF + role.getname());
        }
        model.getDescription().setEntity(role.getdescription());
        if (commandType == CommandType.Edit)
        {
            model.getName().setIsChangable(!role.getis_readonly());
            model.getDescription().setIsChangable(!role.getis_readonly());
        }
        String title = null;
        String hashName = null;
        switch (commandType)
        {
        case New:
            title = "New Role";
            hashName = "new_role";
            break;
        case Edit:
            title = "Edit Role";
            hashName = "edit_role";
            model.getIsAdminRole().setIsChangable(false);
            break;
        case Clone:
            title = "Copy Role";
            hashName = "copy_role";
            model.getIsAdminRole().setIsChangable(false);
            break;

        }

        model.setTitle(title);
        model.setHashName(hashName);
        if (!role.getis_readonly() || commandType == CommandType.Clone)
        {
            UICommand tempVar = new UICommand("OnSave", this);
            tempVar.setTitle("OK");
            tempVar.setIsDefault(true);
            model.getCommands().add(tempVar);
            UICommand tempVar2 = new UICommand("OnReset", this);
            tempVar2.setTitle("Reset");
            model.getCommands().add(tempVar2);
        }

        UICommand tempVar3 = new UICommand("Cancel", this);
        tempVar3.setTitle(!role.getis_readonly() ? "Cancel" : "Close");
        tempVar3.setIsCancel(true);
        tempVar3.setIsDefault(role.getis_readonly());
        model.getCommands().add(tempVar3);
    }

    public void OnReset()
    {
        roles role = (roles) getSelectedItem();
        RoleModel model = (RoleModel) getWindow();

        java.util.ArrayList<ActionGroup> attachedActions =
                commandType == CommandType.New ? new java.util.ArrayList<ActionGroup>() : publicAttachedActions;

        for (SelectionTreeNodeModel sm : model.getPermissionGroupModels())
        {
            for (SelectionTreeNodeModel smChild : sm.getChildren())
            {
                for (SelectionTreeNodeModel smGrandChild : smChild.getChildren())
                {
                    smGrandChild.setIsSelectedNullable(attachedActions.contains(ActionGroup.valueOf(smGrandChild.getTitle())));
                }
            }
        }
    }

    public void OnSave()
    {
        RoleModel model = (RoleModel) getWindow();

        if (model.getProgress() != null)
        {
            return;
        }

        role = commandType != CommandType.Edit ? new roles() : (roles) getSelectedItem();
        role.setType(((Boolean) model.getIsAdminRole().getEntity() ? RoleType.ADMIN : RoleType.USER));

        if (!model.Validate())
        {
            return;
        }

        // Check name unicitate.
        String name = (String) model.getName().getEntity();

        // if (!DataProvider.IsRoleNameUnique(name) && name.compareToIgnoreCase(role.getname()) != 0)
        // {
        // model.getName().setIsValid(false);
        // model.getName().getInvalidityReasons().add("Name must be unique.");
        // return;
        // }

        role.setname((String) model.getName().getEntity());
        role.setdescription((String) model.getDescription().getEntity());

        java.util.ArrayList<ActionGroup> actions = new java.util.ArrayList<ActionGroup>();
        java.util.HashMap<ActionGroup, ActionGroup> actionDistinctSet =
                new java.util.HashMap<ActionGroup, ActionGroup>();
        for (SelectionTreeNodeModel sm : model.getPermissionGroupModels())
        {
            for (SelectionTreeNodeModel smChild : sm.getChildren())
            {
                if (smChild.getIsSelectedNullable() == null || smChild.getIsSelectedNullable())
                {
                    for (SelectionTreeNodeModel smGrandChild : smChild.getChildren())
                    {
                        if (smGrandChild.getIsSelectedNullable())
                        {
                            ActionGroup actionGroup = ActionGroup.valueOf(smGrandChild.getTitle());
                            if (actionDistinctSet.containsKey(actionGroup))
                            {
                                continue;
                            }
                            actionDistinctSet.put(actionGroup, actionGroup);
                            actions.add(actionGroup);
                        }
                    }
                }
            }
        }

        VdcReturnValueBase returnValue;

        model.StartProgress(null);

        if (commandType != CommandType.Edit)
        {
            // Add a new role.
            RoleWithActionGroupsParameters tempVar = new RoleWithActionGroupsParameters();
            tempVar.setRole(role);
            tempVar.setActionGroups(actions);
            Frontend.RunAction(VdcActionType.AddRoleWithActionGroups, tempVar,
                    new IFrontendActionAsyncCallback() {
                        @Override
                        public void Executed(FrontendActionAsyncResult result) {

                            RoleListModel localModel = (RoleListModel) result.getState();
                            localModel.PostOnSaveNew(result.getReturnValue());

                        }
                    }, this);
        }
        else
        {

            detachActionGroup = Linq.Except(publicAttachedActions, actions);
            attachActionGroup = Linq.Except(actions, publicAttachedActions);

            Frontend.RunAction(VdcActionType.UpdateRole, new RolesOperationsParameters(role),
                    new IFrontendActionAsyncCallback() {
                        @Override
                        public void Executed(FrontendActionAsyncResult result) {

                            RoleListModel roleListModel = (RoleListModel) result.getState();
                            VdcReturnValueBase retVal = result.getReturnValue();
                            if (retVal != null && retVal.getSucceeded())
                            {
                                if (roleListModel.detachActionGroup.size() > 0)
                                {
                                    ActionGroupsToRoleParameter tempVar2 = new ActionGroupsToRoleParameter();
                                    tempVar2.setActionGroups(roleListModel.detachActionGroup);
                                    tempVar2.setRoleId(roleListModel.role.getId());
                                    retVal = Frontend.RunAction(VdcActionType.DetachActionGroupsFromRole, tempVar2);
                                }
                                if (roleListModel.attachActionGroup.size() > 0)
                                {
                                    ActionGroupsToRoleParameter tempVar3 = new ActionGroupsToRoleParameter();
                                    tempVar3.setActionGroups(roleListModel.attachActionGroup);
                                    tempVar3.setRoleId(roleListModel.role.getId());
                                    retVal = Frontend.RunAction(VdcActionType.AttachActionGroupsToRole, tempVar3);
                                }
                                roleListModel.getWindow().StopProgress();
                                roleListModel.Cancel();
                            }
                            else
                            {
                                roleListModel.getWindow().StopProgress();
                            }

                        }
                    }, this);
        }
    }

    public void PostOnSaveNew(VdcReturnValueBase returnValue)
    {
        RoleModel model = (RoleModel) getWindow();

        model.StopProgress();

        if (returnValue != null && returnValue.getSucceeded())
        {
            Cancel();
            getSearchCommand().Execute();
        }
    }

    public void Cancel()
    {
        setWindow(null);
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

    private void UpdateActionAvailability()
    {
        boolean temp = getSelectedItems() != null && getSelectedItems().size() == 1;

        getCloneCommand().setIsExecutionAllowed(temp);
        getEditCommand().setIsExecutionAllowed(temp);
        getRemoveCommand().setIsExecutionAllowed(getSelectedItems() != null && getSelectedItems().size() > 0
                && !IsAnyRoleReadOnly(getSelectedItems()));
    }

    private boolean IsAnyRoleReadOnly(java.util.List roles)
    {
        for (Object item : roles)
        {
            roles r = (roles) item;
            if (r.getis_readonly())
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public void ExecuteCommand(UICommand command)
    {
        super.ExecuteCommand(command);

        if (command == getNewCommand())
        {
            New();
        }
        else if (command == getEditCommand())
        {
            Edit();
        }
        else if (command == getRemoveCommand())
        {
            remove();
        }
        else if (command == getSearchAllRolesCommand())
        {
            SearchAllRoles();
        }
        else if (command == getSearchAdminRolesCommand())
        {
            SearchAdminRoles();
        }
        else if (command == getSearchUserRolesCommand())
        {
            SearchUserRoles();
        }
        else if (StringHelper.stringsEqual(command.getName(), "OnSave"))
        {
            OnSave();
        }
        else if (StringHelper.stringsEqual(command.getName(), "Cancel"))
        {
            Cancel();
        }
        else if (StringHelper.stringsEqual(command.getName(), "OnRemove"))
        {
            OnRemove();
        }
        else if (StringHelper.stringsEqual(command.getName(), "OnReset"))
        {
            OnReset();
        }
        else if (StringHelper.stringsEqual(command.getName(), "Clone"))
        {
            CloneRole();
        }
    }

    @Override
    protected String getListName() {
        return "RoleListModel";
    }
}
