package org.ovirt.engine.ui.uicommonweb.models.configure.roles_ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.ovirt.engine.core.common.action.ActionGroupsToRoleParameter;
import org.ovirt.engine.core.common.action.RoleWithActionGroupsParameters;
import org.ovirt.engine.core.common.action.RolesOperationsParameters;
import org.ovirt.engine.core.common.action.RolesParameterBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.Role;
import org.ovirt.engine.core.common.businessentities.RoleType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.MultilevelAdministrationsQueriesParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.auth.ApplicationGuids;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListWithDetailsModel;
import org.ovirt.engine.ui.uicommonweb.models.common.SelectionTreeNodeModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.FrontendActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;

import com.google.inject.Inject;

@SuppressWarnings("unused")
public class RoleListModel extends ListWithDetailsModel
{
    public static final String COPY_OF = "Copy_of_"; //$NON-NLS-1$

    public enum CommandType
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

    @Override
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
    public ArrayList<ActionGroup> publicAttachedActions;
    public ArrayList<ActionGroup> detachActionGroup;
    public ArrayList<ActionGroup> attachActionGroup;
    public Role role;
    private RoleType privateItemsFilter;

    public RoleType getItemsFilter()
    {
        return privateItemsFilter;
    }

    public void setItemsFilter(RoleType value)
    {
        privateItemsFilter = value;
    }

    @Inject
    public RoleListModel(final RolePermissionListModel rolePermissionListModel) {
        setDetailList(rolePermissionListModel);
        setTitle(ConstantsManager.getInstance().getConstants().rolesTitle());

        setNewCommand(new UICommand("New", this)); //$NON-NLS-1$
        setEditCommand(new UICommand("Edit", this)); //$NON-NLS-1$
        setRemoveCommand(new UICommand("Remove", this)); //$NON-NLS-1$
        setCloneCommand(new UICommand("Clone", this)); //$NON-NLS-1$
        setSearchAllRolesCommand(new UICommand("SearchAllRoles", this)); //$NON-NLS-1$
        setSearchAdminRolesCommand(new UICommand("SearchAdminRoles", this)); //$NON-NLS-1$
        setSearchUserRolesCommand(new UICommand("SearchUserRoles", this)); //$NON-NLS-1$

        setSearchPageSize(1000);

        updateActionAvailability();
    }

    private void setDetailList(final RolePermissionListModel rolePermissionListModel) {
        List<EntityModel> list = new ArrayList<EntityModel>();
        list.add(rolePermissionListModel);

        setDetailModels(list);
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
                RoleListModel roleListModel = (RoleListModel) model;
                ArrayList<Role> filteredList = new ArrayList<Role>();
                for (Role item : (ArrayList<Role>) ((VdcQueryReturnValue) ReturnValue).getReturnValue())
                {
                    // ignore CONSUME_QUOTA_ROLE in UI
                    if (item.getId().equals(ApplicationGuids.quotaConsumer.asGuid())) {
                        continue;
                    }
                    if (roleListModel.getItemsFilter() == null || roleListModel.getItemsFilter() == item.getType())
                    {
                        filteredList.add(item);
                    }
                }
                Collections.sort(filteredList, new Linq.RoleNameComparer());
                roleListModel.setItems(filteredList);
            }
        };

        MultilevelAdministrationsQueriesParameters tempVar = new MultilevelAdministrationsQueriesParameters();
        tempVar.setRefresh(getIsQueryFirstTime());
        Frontend.getInstance().runQuery(VdcQueryType.GetAllRoles, tempVar, _asyncQuery);
        setIsQueryFirstTime(false);
    }

    private void searchAllRoles()
    {
        setItemsFilter(null);
        getSearchCommand().execute();
    }

    private void searchUserRoles()
    {
        setItemsFilter(RoleType.USER);
        getSearchCommand().execute();
    }

    private void searchAdminRoles()
    {
        setItemsFilter(RoleType.ADMIN);
        getSearchCommand().execute();
    }

    public void remove()
    {
        if (getWindow() != null)
        {
            return;
        }

        ConfirmationModel model = new ConfirmationModel();
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().removeRolesTitle());
        model.setHelpTag(HelpTag.remove_role);
        model.setHashName("remove_role"); //$NON-NLS-1$

        ArrayList<String> list = new ArrayList<String>();
        for (Role role : Linq.<Role> cast(getSelectedItems()))
        {
            list.add(role.getname());
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

    public void onRemove()
    {
        for (Object item : getSelectedItems())
        {
            Role role = (Role) item;
            Frontend.getInstance().runAction(VdcActionType.RemoveRole, new RolesParameterBase(role.getId()));
        }

        cancel();

        // Execute search to keep list updated.
        getSearchCommand().execute();
    }

    public void edit()
    {
        commandType = CommandType.Edit;
        Role role = (Role) getSelectedItem();
        initRoleDialog(role);
    }

    public void newEntity()
    {
        commandType = CommandType.New;
        Role role = new Role();
        initRoleDialog(role);
    }

    public void cloneRole()
    {
        commandType = CommandType.Clone;
        Role role = (Role) getSelectedItem();
        initRoleDialog(role);
    }

    @Override
    public void eventRaised(Event ev, Object sender, EventArgs args)
    {
        super.eventRaised(ev, sender, args);

        if (getWindow() != null
                && sender == ((RoleModel) getWindow()).getIsAdminRole()) {
            if (commandType == CommandType.New) {
                List<ActionGroup> selectedActionGroups = new ArrayList<ActionGroup>();
                selectedActionGroups.add(ActionGroup.LOGIN);
                setAttachedActionGroups(selectedActionGroups);
            } else {

                AsyncQuery _asyncQuery = new AsyncQuery();
                _asyncQuery.setModel(this);
                _asyncQuery.asyncCallback = new INewAsyncCallback() {
                    @Override
                    public void onSuccess(Object model, Object ReturnValue) {
                        RoleListModel roleListModel = (RoleListModel) model;
                        roleListModel.publicAttachedActions =
                                (ArrayList<ActionGroup>) ((VdcQueryReturnValue) ReturnValue).getReturnValue();
                        roleListModel.setAttachedActionGroups(publicAttachedActions);
                    }
                };
                Role role = (Role) getSelectedItem();
                Frontend.getInstance().runQuery(
                        VdcQueryType.GetRoleActionGroupsByRoleId,
                        new IdQueryParameters(role.getId()),
                        _asyncQuery);
            }

        }
    }

    void setAttachedActionGroups(List<ActionGroup> attachedActions) {
        Role role = (Role) getSelectedItem();
        RoleModel model = (RoleModel) getWindow();
        ArrayList<SelectionTreeNodeModel> selectionTree =
                RoleTreeView.getRoleTreeView((model.getIsNew() ? false : role.getis_readonly()),
                        model.getIsAdminRole().getEntity());
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
                        smGrandChild.updateParentSelection();
                    }

                    if (smChild.getChildren().get(0).equals(smGrandChild))
                    {
                        smGrandChild.updateParentSelection();
                    }
                }
            }
        }
        model.setPermissionGroupModels(selectionTree);
    }

    private void initRoleDialog(Role role)
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
        switch (commandType)
        {
            case New:
                model.setTitle(ConstantsManager.getInstance().getConstants().newRoleTitle());
                model.setHelpTag(HelpTag.new_role);
                model.setHashName("new_role"); //$NON-NLS-1$
                break;
            case Edit:
                model.setTitle(ConstantsManager.getInstance().getConstants().editRoleTitle());
                model.setHelpTag(HelpTag.edit_role);
                model.setHashName("edit_role"); //$NON-NLS-1$
                model.getIsAdminRole().setIsChangable(false);
                break;
            case Clone:
                model.setTitle(ConstantsManager.getInstance().getConstants().copyRoleTitle());
                model.setHelpTag(HelpTag.copy_role);
                model.setHashName("copy_role"); //$NON-NLS-1$
                model.getIsAdminRole().setIsChangable(false);
                break;
        }

        if (!role.getis_readonly() || commandType == CommandType.Clone)
        {
            UICommand tempVar = new UICommand("OnSave", this); //$NON-NLS-1$
            tempVar.setTitle(ConstantsManager.getInstance().getConstants().ok());
            tempVar.setIsDefault(true);
            model.getCommands().add(tempVar);
            UICommand tempVar2 = new UICommand("OnReset", this); //$NON-NLS-1$
            tempVar2.setTitle(ConstantsManager.getInstance().getConstants().resetTitle());
            model.getCommands().add(tempVar2);
        }

        UICommand tempVar3 = new UICommand("Cancel", this); //$NON-NLS-1$
        tempVar3.setTitle(!role.getis_readonly() ? ConstantsManager.getInstance().getConstants().cancel()
                : ConstantsManager.getInstance().getConstants().close());
        tempVar3.setIsCancel(true);
        tempVar3.setIsDefault(role.getis_readonly());
        model.getCommands().add(tempVar3);
    }

    public void onReset()
    {
        RoleModel model = (RoleModel) getWindow();

        ArrayList<ActionGroup> attachedActions =
                commandType == CommandType.New ? new ArrayList<ActionGroup>() : publicAttachedActions;

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

    public void onSave()
    {
        RoleModel model = (RoleModel) getWindow();

        if (model.getProgress() != null)
        {
            return;
        }

        role = commandType != CommandType.Edit ? new Role() : (Role) getSelectedItem();
        role.setType((model.getIsAdminRole().getEntity() ? RoleType.ADMIN : RoleType.USER));

        if (!model.validate())
        {
            return;
        }

        // if (!DataProvider.IsRoleNameUnique(name) && name.compareToIgnoreCase(role.getname()) != 0)
        // {
        // model.getName().setIsValid(false);
        // model.getName().getInvalidityReasons().add("Name must be unique.");
        // return;
        // }

        role.setname(model.getName().getEntity());
        role.setdescription(model.getDescription().getEntity());

        ArrayList<ActionGroup> actions = new ArrayList<ActionGroup>();
        HashMap<ActionGroup, ActionGroup> actionDistinctSet =
                new HashMap<ActionGroup, ActionGroup>();
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

        model.startProgress(null);

        if (commandType != CommandType.Edit)
        {
            // Add a new role.
            RoleWithActionGroupsParameters tempVar = new RoleWithActionGroupsParameters();
            tempVar.setRole(role);
            tempVar.setActionGroups(actions);
            Frontend.getInstance().runAction(VdcActionType.AddRoleWithActionGroups, tempVar,
                    new IFrontendActionAsyncCallback() {
                        @Override
                        public void executed(FrontendActionAsyncResult result) {

                            RoleListModel localModel = (RoleListModel) result.getState();
                            localModel.postOnSaveNew(result.getReturnValue());

                        }
                    }, this);
        }
        else
        {

            detachActionGroup = Linq.except(publicAttachedActions, actions);
            attachActionGroup = Linq.except(actions, publicAttachedActions);

            Frontend.getInstance().runAction(VdcActionType.UpdateRole, new RolesOperationsParameters(role),
                    new IFrontendActionAsyncCallback() {
                        @Override
                        public void executed(FrontendActionAsyncResult result) {

                            RoleListModel roleListModel = (RoleListModel) result.getState();
                            VdcReturnValueBase retVal = result.getReturnValue();
                            if (retVal != null && retVal.getSucceeded())
                            {
                                if (roleListModel.detachActionGroup.size() > 0)
                                {
                                    ActionGroupsToRoleParameter tempVar2 = new ActionGroupsToRoleParameter();
                                    tempVar2.setActionGroups(roleListModel.detachActionGroup);
                                    tempVar2.setRoleId(roleListModel.role.getId());
                                    Frontend.getInstance().runAction(VdcActionType.DetachActionGroupsFromRole, tempVar2);
                                }
                                if (roleListModel.attachActionGroup.size() > 0)
                                {
                                    ActionGroupsToRoleParameter tempVar3 = new ActionGroupsToRoleParameter();
                                    tempVar3.setActionGroups(roleListModel.attachActionGroup);
                                    tempVar3.setRoleId(roleListModel.role.getId());
                                    Frontend.getInstance().runAction(VdcActionType.AttachActionGroupsToRole, tempVar3);
                                }
                                roleListModel.getWindow().stopProgress();
                                roleListModel.cancel();
                            }
                            else
                            {
                                roleListModel.getWindow().stopProgress();
                            }

                        }
                    }, this);
        }
    }

    public void postOnSaveNew(VdcReturnValueBase returnValue)
    {
        RoleModel model = (RoleModel) getWindow();

        model.stopProgress();

        if (returnValue != null && returnValue.getSucceeded())
        {
            cancel();
            getSearchCommand().execute();
        }
    }

    public void cancel()
    {
        setWindow(null);
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

    private void updateActionAvailability()
    {
        boolean temp = getSelectedItems() != null && getSelectedItems().size() == 1;

        getCloneCommand().setIsExecutionAllowed(temp);
        getEditCommand().setIsExecutionAllowed(temp);
        getRemoveCommand().setIsExecutionAllowed(getSelectedItems() != null && getSelectedItems().size() > 0
                && !isAnyRoleReadOnly(getSelectedItems()));
    }

    private boolean isAnyRoleReadOnly(List roles)
    {
        for (Object item : roles)
        {
            Role r = (Role) item;
            if (r.getis_readonly())
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public void executeCommand(UICommand command)
    {
        super.executeCommand(command);

        if (command == getNewCommand())
        {
            newEntity();
        }
        else if (command == getEditCommand())
        {
            edit();
        }
        else if (command == getRemoveCommand())
        {
            remove();
        }
        else if (command == getSearchAllRolesCommand())
        {
            searchAllRoles();
        }
        else if (command == getSearchAdminRolesCommand())
        {
            searchAdminRoles();
        }
        else if (command == getSearchUserRolesCommand())
        {
            searchUserRoles();
        }
        else if ("OnSave".equals(command.getName())) //$NON-NLS-1$
        {
            onSave();
        }
        else if ("Cancel".equals(command.getName())) //$NON-NLS-1$
        {
            cancel();
        }
        else if ("OnRemove".equals(command.getName())) //$NON-NLS-1$
        {
            onRemove();
        }
        else if ("OnReset".equals(command.getName())) //$NON-NLS-1$
        {
            onReset();
        }
        else if ("Clone".equals(command.getName())) //$NON-NLS-1$
        {
            cloneRole();
        }
    }

    @Override
    protected String getListName() {
        return "RoleListModel"; //$NON-NLS-1$
    }
}
