package org.ovirt.engine.ui.uicommonweb.models.users;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.common.VdcActionUtils;
import org.ovirt.engine.core.common.action.AdElementParametersBase;
import org.ovirt.engine.core.common.action.AddUserParameters;
import org.ovirt.engine.core.common.action.AttachEntityToTagParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.DbUser;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.LdapGroup;
import org.ovirt.engine.core.common.businessentities.tags;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.SearchParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.users.VdcUser;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.ObservableCollection;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.searchbackend.SearchObjects;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.TagsEqualityComparer;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListWithDetailsModel;
import org.ovirt.engine.ui.uicommonweb.models.tags.TagListModel;
import org.ovirt.engine.ui.uicommonweb.models.tags.TagModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.FrontendMultipleActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendMultipleActionAsyncCallback;

public class UserListModel extends ListWithDetailsModel
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

    private UICommand privateAssignTagsCommand;

    public UICommand getAssignTagsCommand()
    {
        return privateAssignTagsCommand;
    }

    private void setAssignTagsCommand(UICommand value)
    {
        privateAssignTagsCommand = value;
    }

    // get { return SelectedItems == null ? new object[0] : SelectedItems.Cast<DbUser>().Select(a =>
    // a.user_id).Cast<object>().ToArray(); }
    protected Object[] getSelectedKeys()
    {
        if (getSelectedItems() == null)
        {
            return new Object[0];
        }
        else
        {
            ArrayList<Object> items = new ArrayList<Object>();
            for (Object i : getSelectedItems())
            {
                items.add(((VDSGroup) i).getId());
            }
            return items.toArray(new Object[] {});
        }
    }

    public UserListModel()
    {
        setTitle(ConstantsManager.getInstance().getConstants().usersTitle());

        setDefaultSearchString("Users:"); //$NON-NLS-1$
        setSearchString(getDefaultSearchString());
        setSearchObjects(new String[] { SearchObjects.VDC_USER_OBJ_NAME, SearchObjects.VDC_USER_PLU_OBJ_NAME });

        setAddCommand(new UICommand("Add", this)); //$NON-NLS-1$
        setRemoveCommand(new UICommand("Remove", this)); //$NON-NLS-1$
        setAssignTagsCommand(new UICommand("AssignTags", this)); //$NON-NLS-1$

        UpdateActionAvailability();

        getSearchNextPageCommand().setIsAvailable(true);
        getSearchPreviousPageCommand().setIsAvailable(true);
    }

    public void AssignTags()
    {
        if (getWindow() != null)
        {
            return;
        }

        TagListModel model = new TagListModel();
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().assignTagsTitle());
        model.setHashName("assign_tags_users"); //$NON-NLS-1$

        GetAttachedTagsToSelectedUsers(model);

        UICommand tempVar = new UICommand("OnAssignTags", this); //$NON-NLS-1$
        tempVar.setTitle(ConstantsManager.getInstance().getConstants().ok());
        tempVar.setIsDefault(true);
        model.getCommands().add(tempVar);
        UICommand tempVar2 = new UICommand("Cancel", this); //$NON-NLS-1$
        tempVar2.setTitle(ConstantsManager.getInstance().getConstants().cancel());
        tempVar2.setIsCancel(true);
        model.getCommands().add(tempVar2);
    }

    public Map<Guid, Boolean> attachedTagsToEntities;
    public ArrayList<tags> allAttachedTags;
    public int selectedItemsCounter;

    private void GetAttachedTagsToSelectedUsers(TagListModel model)
    {
        HashMap<Guid, Boolean> tags = new HashMap<Guid, Boolean>();

        ArrayList<Guid> userIds = new ArrayList<Guid>();
        ArrayList<Guid> grpIds = new ArrayList<Guid>();

        attachedTagsToEntities = new HashMap<Guid, Boolean>();
        allAttachedTags = new ArrayList<org.ovirt.engine.core.common.businessentities.tags>();
        selectedItemsCounter = 0;

        for (Object item : getSelectedItems())
        {
            DbUser user = (DbUser) item;
            if (!user.getIsGroup())
            {
                userIds.add(user.getuser_id());
            }
            else
            {
                grpIds.add(user.getuser_id());
            }
        }

        for (Guid userId : userIds)
        {
            AsyncDataProvider.GetAttachedTagsToUser(new AsyncQuery(new Object[] { this, model },
                    new INewAsyncCallback() {
                        @Override
                        public void OnSuccess(Object target, Object returnValue) {

                            Object[] array = (Object[]) target;
                            UserListModel userListModel = (UserListModel) array[0];
                            TagListModel tagListModel = (TagListModel) array[1];
                            userListModel.allAttachedTags.addAll((ArrayList<org.ovirt.engine.core.common.businessentities.tags>) returnValue);
                            userListModel.selectedItemsCounter++;
                            if (userListModel.selectedItemsCounter == userListModel.getSelectedItems().size())
                            {
                                PostGetAttachedTags(userListModel, tagListModel);
                            }

                        }
                    }),
                    userId);
        }
        for (Guid grpId : grpIds)
        {
            AsyncDataProvider.GetAttachedTagsToUserGroup(new AsyncQuery(new Object[] { this, model },
                    new INewAsyncCallback() {
                        @Override
                        public void OnSuccess(Object target, Object returnValue) {

                            Object[] array = (Object[]) target;
                            UserListModel userListModel = (UserListModel) array[0];
                            TagListModel tagListModel = (TagListModel) array[1];
                            userListModel.allAttachedTags.addAll((ArrayList<org.ovirt.engine.core.common.businessentities.tags>) returnValue);
                            userListModel.selectedItemsCounter++;
                            if (userListModel.selectedItemsCounter == userListModel.getSelectedItems().size())
                            {
                                PostGetAttachedTags(userListModel, tagListModel);
                            }

                        }
                    }),
                    grpId);
        }
    }

    private void PostGetAttachedTags(UserListModel userListModel, TagListModel tagListModel)
    {
        if (userListModel.getLastExecutedCommand() == getAssignTagsCommand())
        {
            ArrayList<org.ovirt.engine.core.common.businessentities.tags> attachedTags =
                    Linq.Distinct(userListModel.allAttachedTags, new TagsEqualityComparer());
            for (org.ovirt.engine.core.common.businessentities.tags a : attachedTags)
            {
                int count = 0;
                for (org.ovirt.engine.core.common.businessentities.tags b : allAttachedTags)
                {
                    if (b.gettag_id().equals(a.gettag_id()))
                    {
                        count++;
                    }
                }

                userListModel.attachedTagsToEntities.put(a.gettag_id(), count == userListModel.getSelectedItems()
                        .size());
            }
            tagListModel.setAttachedTagsToEntities(userListModel.attachedTagsToEntities);
        }
        else if (StringHelper.stringsEqual(userListModel.getLastExecutedCommand().getName(), "OnAssignTags")) //$NON-NLS-1$
        {
            userListModel.PostOnAssignTags(tagListModel.getAttachedTagsToEntities());
        }
    }

    private void OnAssignTags()
    {
        TagListModel model = (TagListModel) getWindow();

        GetAttachedTagsToSelectedUsers(model);
    }

    public void PostOnAssignTags(Map<Guid, Boolean> attachedTags)
    {
        TagListModel model = (TagListModel) getWindow();
        ArrayList<Guid> userIds = new ArrayList<Guid>();
        ArrayList<Guid> grpIds = new ArrayList<Guid>();

        for (Object item : getSelectedItems())
        {
            DbUser user = (DbUser) item;
            if (user.getIsGroup())
            {
                grpIds.add(user.getuser_id());
            }
            else
            {
                userIds.add(user.getuser_id());
            }
        }

        // prepare attach/detach lists
        ArrayList<Guid> tagsToAttach = new ArrayList<Guid>();
        ArrayList<Guid> tagsToDetach = new ArrayList<Guid>();

        if (model.getItems() != null && ((ArrayList<TagModel>) model.getItems()).size() > 0)
        {
            ArrayList<TagModel> tags = (ArrayList<TagModel>) model.getItems();
            TagModel rootTag = tags.get(0);
            TagModel.RecursiveEditAttachDetachLists(rootTag, attachedTags, tagsToAttach, tagsToDetach);
        }

        ArrayList<VdcActionParametersBase> usersToAttach = new ArrayList<VdcActionParametersBase>();
        ArrayList<VdcActionParametersBase> grpsToAttach = new ArrayList<VdcActionParametersBase>();
        for (Guid tag_id : tagsToAttach)
        {
            if (userIds.size() > 0)
            {
                usersToAttach.add(new AttachEntityToTagParameters(tag_id, userIds));
            }
            if (grpIds.size() > 0)
            {
                grpsToAttach.add(new AttachEntityToTagParameters(tag_id, grpIds));
            }
        }
        if (usersToAttach.size() > 0)
        {
            Frontend.RunMultipleAction(VdcActionType.AttachUserToTag, usersToAttach);
        }
        if (grpsToAttach.size() > 0)
        {
            Frontend.RunMultipleAction(VdcActionType.AttachUserGroupToTag, grpsToAttach);
        }

        ArrayList<VdcActionParametersBase> usersToDetach = new ArrayList<VdcActionParametersBase>();
        ArrayList<VdcActionParametersBase> grpsToDetach = new ArrayList<VdcActionParametersBase>();
        for (Guid tag_id : tagsToDetach)
        {
            if (userIds.size() > 0)
            {
                usersToDetach.add(new AttachEntityToTagParameters(tag_id, userIds));
            }
            if (grpIds.size() > 0)
            {
                grpsToDetach.add(new AttachEntityToTagParameters(tag_id, grpIds));
            }
        }
        if (usersToDetach.size() > 0)
        {
            Frontend.RunMultipleAction(VdcActionType.DetachUserFromTag, usersToDetach);
        }
        if (grpsToDetach.size() > 0)
        {
            Frontend.RunMultipleAction(VdcActionType.DetachUserGroupFromTag, grpsToDetach);
        }

        Cancel();
    }

    public void add()
    {
        if (getWindow() != null)
        {
            return;
        }

        AdElementListModel model = new AdElementListModel();
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().addUsersAndGroupsTitle());
        model.setHashName("add_users_and_groups"); //$NON-NLS-1$
        model.setIsRoleListHidden(true);
        model.getIsEveryoneSelectionHidden().setEntity(true);

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
        model.setTitle(ConstantsManager.getInstance().getConstants().removeUsersTitle());
        model.setHashName("remove_user"); //$NON-NLS-1$
        model.setMessage(ConstantsManager.getInstance().getConstants().usersMsg());

        ArrayList<String> list = new ArrayList<String>();
        for (DbUser item : Linq.<DbUser> Cast(getSelectedItems()))
        {
            list.add(item.getname());
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

    @Override
    public boolean IsSearchStringMatch(String searchString)
    {
        return searchString.trim().toLowerCase().startsWith("user"); //$NON-NLS-1$
    }

    @Override
    protected void SyncSearch()
    {
        SearchParameters tempVar = new SearchParameters(getSearchString(), SearchType.DBUser);
        tempVar.setMaxCount(getSearchPageSize());
        super.SyncSearch(VdcQueryType.Search, tempVar);
    }

    private EntityModel userGroupListModel;
    private EntityModel userEventNotifierListModel;

    @Override
    protected void InitDetailModels()
    {
        super.InitDetailModels();

        ObservableCollection<EntityModel> list = new ObservableCollection<EntityModel>();
        list.add(new UserGeneralModel());
        list.add(new UserQuotaListModel());
        list.add(new UserPermissionListModel());
        list.add(new UserEventListModel());
        userGroupListModel = new UserGroupListModel();
        userGroupListModel.setIsAvailable(false);
        list.add(userGroupListModel);
        userEventNotifierListModel = new UserEventNotifierListModel();
        list.add(userEventNotifierListModel);
        setDetailModels(list);
    }

    @Override
    protected void UpdateDetailsAvailability()
    {
        if (getSelectedItem() != null)
        {
            DbUser adUser = (DbUser) getSelectedItem();
            userGroupListModel.setIsAvailable(!adUser.getIsGroup());
            userEventNotifierListModel.setIsAvailable(!adUser.getIsGroup());
        }
    }

    @Override
    protected void AsyncSearch()
    {
        super.AsyncSearch();

        setAsyncResult(Frontend.RegisterSearch(getSearchString(), SearchType.DBUser, getSearchPageSize()));
        setItems(getAsyncResult().getData());
    }

    public void Cancel()
    {
        setWindow(null);
    }

    public void OnAdd()
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

        ArrayList<DbUser> items = new ArrayList<DbUser>();
        for (Object item : model.getItems())
        {
            EntityModel entityModel = (EntityModel) item;
            if (entityModel.getIsSelected())
            {
                items.add((DbUser) entityModel.getEntity());
            }
        }

        ArrayList<VdcActionParametersBase> parameters = new ArrayList<VdcActionParametersBase>();
        for (DbUser item : items)
        {
            if (!item.getIsGroup())
            {
                AddUserParameters tempVar = new AddUserParameters();
                tempVar.setVdcUser(new VdcUser(item.getuser_id(), item.getusername(), item.getdomain()));
                parameters.add(tempVar);
            }
            else
            {
                AddUserParameters tempVar2 = new AddUserParameters();
                tempVar2.setAdGroup(new LdapGroup(item.getuser_id(), item.getname(), item.getdomain()));
                parameters.add(tempVar2);
            }
        }

        model.StartProgress(null);

        Frontend.RunMultipleAction(VdcActionType.AddUser, parameters,
                new IFrontendMultipleActionAsyncCallback() {
                    @Override
                    public void Executed(FrontendMultipleActionAsyncResult result) {

                        AdElementListModel localModel = (AdElementListModel) result.getState();
                        localModel.StopProgress();
                        Cancel();

                    }
                }, model);
    }

    public void OnRemove()
    {
        ArrayList<DbUser> items = Linq.<DbUser> Cast(getSelectedItems());

        ArrayList<VdcActionParametersBase> userPrms = new ArrayList<VdcActionParametersBase>();
        ArrayList<VdcActionParametersBase> groupPrms = new ArrayList<VdcActionParametersBase>();
        for (DbUser item : items)
        {
            if (!item.getIsGroup())
            {
                userPrms.add(new AdElementParametersBase(item.getuser_id()));
            }
            else
            {
                groupPrms.add(new AdElementParametersBase(item.getuser_id()));
            }
        }

        if (userPrms.size() > 0)
        {
            Frontend.RunMultipleAction(VdcActionType.RemoveUser, userPrms);

        }

        if (groupPrms.size() > 0)
        {
            Frontend.RunMultipleAction(VdcActionType.RemoveAdGroup, groupPrms);
        }

        Cancel();
    }

    // private void AssignTags()
    // {
    // base.AssignTags();

    // DbUser user = (DbUser)SelectedItem;
    // AssignTagsModel.AttachedTags = DataProvider.GetAttachedTagsToUser(user.user_id);
    // }

    // public override void NotifyTagsAttached(IList<tags> tags)
    // {
    // base.NotifyTagsAttached(tags);

    // var dbUsers = SelectedItems.Cast<DbUser>();

    // var userIds = dbUsers
    // .Where(a => !a.IsGroup)
    // .Select(a => a.user_id)
    // .ToList();

    // Frontend.RunMultipleActions(VdcActionType.AttachUserToTag,
    // tags.Select(a =>
    // (VdcActionParametersBase)new AttachEntityToTagParameters(a.tag_id, userIds)
    // )
    // .ToList()
    // );

    // var groupIds = dbUsers
    // .Where(a => a.IsGroup)
    // .Select(a => a.user_id)
    // .ToList();

    // Frontend.RunMultipleActions(VdcActionType.AttachUserGroupToTag,
    // tags.Select(a =>
    // (VdcActionParametersBase)new AttachEntityToTagParameters(a.tag_id, groupIds)
    // )
    // .ToList()
    // );
    // }

    // public override void NotifyTagsDetached(IList<tags> tags)
    // {
    // base.NotifyTagsDetached(tags);

    // var dbUsers = SelectedItems.Cast<DbUser>();

    // var userIds = dbUsers
    // .Where(a => !a.IsGroup)
    // .Select(a => a.user_id)
    // .ToList();

    // Frontend.RunMultipleActions(VdcActionType.DetachUserFromTag,
    // tags.Select(a =>
    // (VdcActionParametersBase)new AttachEntityToTagParameters(a.tag_id, userIds)
    // )
    // .ToList()
    // );

    // var groupIds = dbUsers
    // .Where(a => a.IsGroup)
    // .Select(a => a.user_id)
    // .ToList();

    // Frontend.RunMultipleActions(VdcActionType.DetachUserGroupFromTag,
    // tags.Select(a =>
    // (VdcActionParametersBase)new AttachEntityToTagParameters(a.tag_id, groupIds)
    // )
    // .ToList()
    // );
    // }

    // public override void OnSelectionChanged(object item, IList items)
    // {
    // base.OnSelectionChanged(item, items);

    // UpdateActionAvailability();
    // }

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
        ArrayList items =
                (((ArrayList) getSelectedItems()) != null) ? (ArrayList) getSelectedItems()
                        : new ArrayList();

        getRemoveCommand().setIsExecutionAllowed(items.size() > 0
                && VdcActionUtils.CanExecute(items, DbUser.class, VdcActionType.RemoveUser));

        getAssignTagsCommand().setIsExecutionAllowed(items.size() > 0);
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
        if (command == getAssignTagsCommand())
        {
            AssignTags();
        }

        if (StringHelper.stringsEqual(command.getName(), "Cancel")) //$NON-NLS-1$
        {
            Cancel();
        }
        if (StringHelper.stringsEqual(command.getName(), "OnAssignTags")) //$NON-NLS-1$
        {
            OnAssignTags();
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

    @Override
    protected String getListName() {
        return "UserListModel"; //$NON-NLS-1$
    }
}
