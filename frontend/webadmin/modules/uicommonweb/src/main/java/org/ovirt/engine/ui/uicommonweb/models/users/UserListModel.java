package org.ovirt.engine.ui.uicommonweb.models.users;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.VdcActionUtils;
import org.ovirt.engine.core.common.action.AddGroupParameters;
import org.ovirt.engine.core.common.action.AddUserParameters;
import org.ovirt.engine.core.common.action.AttachEntityToTagParameters;
import org.ovirt.engine.core.common.action.IdParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.Tags;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.aaa.DbGroup;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.SearchParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.searchbackend.SearchObjects;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.TagsEqualityComparer;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.HasEntity;
import org.ovirt.engine.ui.uicommonweb.models.ListWithSimpleDetailsModel;
import org.ovirt.engine.ui.uicommonweb.models.tags.TagListModel;
import org.ovirt.engine.ui.uicommonweb.models.tags.TagModel;
import org.ovirt.engine.ui.uicommonweb.place.WebAdminApplicationPlaces;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.FrontendActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;

import com.google.inject.Inject;

public class UserListModel extends ListWithSimpleDetailsModel<Void, DbUser> {
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

    @Inject
    public UserListModel(final UserGroupListModel userGroupListModel,
            final UserEventNotifierListModel userEventNotifierListModel, final UserGeneralModel userGeneralModel,
            final UserQuotaListModel userQuotaListModel, final UserPermissionListModel userPermissionListModel,
            final UserEventListModel userEventListModel) {
        this.userGroupListModel = userGroupListModel;
        this.userEventNotifierListModel = userEventNotifierListModel;
        setDetailList(userGeneralModel, userQuotaListModel, userPermissionListModel, userEventListModel);
        setTitle(ConstantsManager.getInstance().getConstants().usersTitle());
        setApplicationPlace(WebAdminApplicationPlaces.userMainTabPlace);

        setDefaultSearchString("Users:"); //$NON-NLS-1$
        setSearchString(getDefaultSearchString());
        setSearchObjects(new String[] { SearchObjects.VDC_USER_OBJ_NAME, SearchObjects.VDC_USER_PLU_OBJ_NAME });

        setAddCommand(new UICommand("Add", this)); //$NON-NLS-1$
        setRemoveCommand(new UICommand("Remove", this)); //$NON-NLS-1$
        setAssignTagsCommand(new UICommand("AssignTags", this)); //$NON-NLS-1$

        updateActionAvailability();

        getSearchNextPageCommand().setIsAvailable(true);
        getSearchPreviousPageCommand().setIsAvailable(true);
    }

    private void setDetailList(final UserGeneralModel userGeneralModel, final UserQuotaListModel userQuotaListModel,
            final UserPermissionListModel userPermissionListModel, final UserEventListModel userEventListModel) {

        List<HasEntity<DbUser>> list = new ArrayList<>();
        list.add(userGeneralModel);
        list.add(userQuotaListModel);
        list.add(userPermissionListModel);
        list.add(userEventListModel);
        userGroupListModel.setIsAvailable(false);
        list.add(userGroupListModel);
        list.add(userEventNotifierListModel);
        setDetailModels(list);
    }

    public void assignTags()
    {
        if (getWindow() != null)
        {
            return;
        }

        TagListModel model = new TagListModel();
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().assignTagsTitle());
        model.setHelpTag(HelpTag.assign_tags_users);
        model.setHashName("assign_tags_users"); //$NON-NLS-1$

        getAttachedTagsToSelectedUsers(model);

        UICommand tempVar = UICommand.createDefaultOkUiCommand("OnAssignTags", this); //$NON-NLS-1$
        model.getCommands().add(tempVar);
        UICommand tempVar2 = UICommand.createCancelUiCommand("Cancel", this); //$NON-NLS-1$
        model.getCommands().add(tempVar2);
    }

    public Map<Guid, Boolean> attachedTagsToEntities;
    public ArrayList<Tags> allAttachedTags;
    public int selectedItemsCounter;

    private void getAttachedTagsToSelectedUsers(TagListModel model)
    {
        ArrayList<Guid> userIds = new ArrayList<Guid>();
        ArrayList<Guid> grpIds = new ArrayList<Guid>();

        attachedTagsToEntities = new HashMap<Guid, Boolean>();
        allAttachedTags = new ArrayList<Tags>();
        selectedItemsCounter = 0;

        for (Object item : getSelectedItems())
        {
            DbUser user = (DbUser) item;
            if (!user.isGroup())
            {
                userIds.add(user.getId());
            }
            else
            {
                grpIds.add(user.getId());
            }
        }

        for (Guid userId : userIds)
        {
            AsyncDataProvider.getInstance().getAttachedTagsToUser(new AsyncQuery(new Object[] { this, model },
                    new INewAsyncCallback() {
                        @Override
                        public void onSuccess(Object target, Object returnValue) {

                            Object[] array = (Object[]) target;
                            UserListModel userListModel = (UserListModel) array[0];
                            TagListModel tagListModel = (TagListModel) array[1];
                            userListModel.allAttachedTags.addAll((ArrayList<Tags>) returnValue);
                            userListModel.selectedItemsCounter++;
                            if (userListModel.selectedItemsCounter == userListModel.getSelectedItems().size())
                            {
                                postGetAttachedTags(userListModel, tagListModel);
                            }

                        }
                    }),
                    userId);
        }
        for (Guid grpId : grpIds)
        {
            AsyncDataProvider.getInstance().getAttachedTagsToUserGroup(new AsyncQuery(new Object[] { this, model },
                    new INewAsyncCallback() {
                        @Override
                        public void onSuccess(Object target, Object returnValue) {

                            Object[] array = (Object[]) target;
                            UserListModel userListModel = (UserListModel) array[0];
                            TagListModel tagListModel = (TagListModel) array[1];
                            userListModel.allAttachedTags.addAll((ArrayList<Tags>) returnValue);
                            userListModel.selectedItemsCounter++;
                            if (userListModel.selectedItemsCounter == userListModel.getSelectedItems().size())
                            {
                                postGetAttachedTags(userListModel, tagListModel);
                            }

                        }
                    }),
                    grpId);
        }
    }

    private void postGetAttachedTags(UserListModel userListModel, TagListModel tagListModel)
    {
        if (userListModel.getLastExecutedCommand() == getAssignTagsCommand())
        {
            ArrayList<Tags> attachedTags =
                    Linq.distinct(userListModel.allAttachedTags, new TagsEqualityComparer());
            for (Tags a : attachedTags)
            {
                int count = 0;
                for (Tags b : allAttachedTags)
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
        else if ("OnAssignTags".equals(userListModel.getLastExecutedCommand().getName())) //$NON-NLS-1$
        {
            userListModel.postOnAssignTags(tagListModel.getAttachedTagsToEntities());
        }
    }

    private void onAssignTags()
    {
        TagListModel model = (TagListModel) getWindow();

        getAttachedTagsToSelectedUsers(model);
    }

    public void postOnAssignTags(Map<Guid, Boolean> attachedTags)
    {
        TagListModel model = (TagListModel) getWindow();
        ArrayList<Guid> userIds = new ArrayList<Guid>();
        ArrayList<Guid> grpIds = new ArrayList<Guid>();

        for (Object item : getSelectedItems())
        {
            DbUser user = (DbUser) item;
            if (user.isGroup())
            {
                grpIds.add(user.getId());
            }
            else
            {
                userIds.add(user.getId());
            }
        }

        // prepare attach/detach lists
        ArrayList<Guid> tagsToAttach = new ArrayList<Guid>();
        ArrayList<Guid> tagsToDetach = new ArrayList<Guid>();

        if (model.getItems() != null && ((ArrayList<TagModel>) model.getItems()).size() > 0)
        {
            ArrayList<TagModel> tags = (ArrayList<TagModel>) model.getItems();
            TagModel rootTag = tags.get(0);
            TagModel.recursiveEditAttachDetachLists(rootTag, attachedTags, tagsToAttach, tagsToDetach);
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
            Frontend.getInstance().runMultipleAction(VdcActionType.AttachUserToTag, usersToAttach);
        }
        if (grpsToAttach.size() > 0)
        {
            Frontend.getInstance().runMultipleAction(VdcActionType.AttachUserGroupToTag, grpsToAttach);
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
            Frontend.getInstance().runMultipleAction(VdcActionType.DetachUserFromTag, usersToDetach);
        }
        if (grpsToDetach.size() > 0)
        {
            Frontend.getInstance().runMultipleAction(VdcActionType.DetachUserGroupFromTag, grpsToDetach);
        }

        cancel();
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
        model.setHelpTag(HelpTag.add_users_and_groups);
        model.setHashName("add_users_and_groups"); //$NON-NLS-1$
        model.setIsRoleListHidden(true);
        model.getIsEveryoneSelectionHidden().setEntity(true);

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
        model.setTitle(ConstantsManager.getInstance().getConstants().removeUsersTitle());
        model.setHelpTag(HelpTag.remove_user);
        model.setHashName("remove_user"); //$NON-NLS-1$

        ArrayList<String> list = new ArrayList<String>();
        for (DbUser item : Linq.<DbUser> cast(getSelectedItems()))
        {
            list.add(item.getFirstName());
        }
        model.setItems(list);

        UICommand tempVar = UICommand.createDefaultOkUiCommand("OnRemove", this); //$NON-NLS-1$
        model.getCommands().add(tempVar);
        UICommand tempVar2 = UICommand.createCancelUiCommand("Cancel", this); //$NON-NLS-1$
        model.getCommands().add(tempVar2);
    }

    @Override
    public boolean isSearchStringMatch(String searchString)
    {
        return searchString.trim().toLowerCase().startsWith("user"); //$NON-NLS-1$
    }

    @Override
    protected void syncSearch()
    {
        SearchParameters tempVar = new SearchParameters(applySortOptions(getSearchString()), SearchType.DBUser,
                isCaseSensitiveSearch());
        tempVar.setMaxCount(getSearchPageSize());
        super.syncSearch(VdcQueryType.Search, tempVar);
    }

    @Override
    public boolean supportsServerSideSorting() {
        return true;
    }

    private final UserGroupListModel userGroupListModel;
    private final UserEventNotifierListModel userEventNotifierListModel;

    @Override
    protected void updateDetailsAvailability()
    {
        if (getSelectedItem() != null)
        {
            DbUser adUser = (DbUser) getSelectedItem();
            userGroupListModel.setIsAvailable(!adUser.isGroup());
            userEventNotifierListModel.setIsAvailable(!adUser.isGroup());
        }
    }

    public void cancel()
    {
        setWindow(null);
    }

    public void onAdd()
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

        ArrayList<VdcActionType> actionsList = new ArrayList<VdcActionType>(items.size());
        ArrayList<VdcActionParametersBase> parametersList = new ArrayList<VdcActionParametersBase>(items.size());
        VdcActionParametersBase parameters = null;
        for (DbUser item : items)
        {
            if (item.isGroup())
            {
                actionsList.add(VdcActionType.AddGroup);
                DbGroup grp = new DbGroup();
                grp.setExternalId(item.getExternalId());
                grp.setName(item.getFirstName());
                grp.setNamespace(item.getNamespace());
                grp.setId(item.getId());
                grp.setDomain(item.getDomain());
                parameters = new AddGroupParameters(grp);
            }
            else
            {
                actionsList.add(VdcActionType.AddUser);
                parameters = new AddUserParameters(item);
            }
            parametersList.add(parameters);
        }

        model.startProgress(null);

        IFrontendActionAsyncCallback nopCallback = new IFrontendActionAsyncCallback() {
            @Override
            public void executed(FrontendActionAsyncResult result) {
                // Nothing.
            }
        };

        IFrontendActionAsyncCallback lastCallback = new IFrontendActionAsyncCallback() {
            @Override
            public void executed(FrontendActionAsyncResult result) {
                AdElementListModel localModel = (AdElementListModel) result.getState();
                localModel.stopProgress();
                cancel();
            }
        };

        ArrayList<IFrontendActionAsyncCallback> callbacksList = new ArrayList<IFrontendActionAsyncCallback>(items.size());
        for (int i = 1; i < items.size(); i++) {
            callbacksList.add(nopCallback);
        }
        callbacksList.add(lastCallback);

        Frontend.getInstance().runMultipleActions(actionsList, parametersList, callbacksList, lastCallback, model);
    }

    public void onRemove()
    {
        ArrayList<DbUser> items = Linq.<DbUser> cast(getSelectedItems());

        ArrayList<VdcActionParametersBase> userPrms = new ArrayList<VdcActionParametersBase>();
        ArrayList<VdcActionParametersBase> groupPrms = new ArrayList<VdcActionParametersBase>();
        for (DbUser item : items)
        {
            if (!item.isGroup())
            {
                userPrms.add(new IdParameters(item.getId()));
            }
            else
            {
                groupPrms.add(new IdParameters(item.getId()));
            }
        }

        if (userPrms.size() > 0)
        {
            Frontend.getInstance().runMultipleAction(VdcActionType.RemoveUser, userPrms);

        }

        if (groupPrms.size() > 0)
        {
            Frontend.getInstance().runMultipleAction(VdcActionType.RemoveGroup, groupPrms);
        }

        cancel();
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
        ArrayList items =
                (((ArrayList) getSelectedItems()) != null) ? (ArrayList) getSelectedItems()
                        : new ArrayList();

        getRemoveCommand().setIsExecutionAllowed(items.size() > 0
                && VdcActionUtils.canExecute(items, DbUser.class, VdcActionType.RemoveUser));

        getAssignTagsCommand().setIsExecutionAllowed(items.size() > 0);
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
        if (command == getAssignTagsCommand())
        {
            assignTags();
        }

        if ("Cancel".equals(command.getName())) //$NON-NLS-1$
        {
            cancel();
        }
        if ("OnAssignTags".equals(command.getName())) //$NON-NLS-1$
        {
            onAssignTags();
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

    @Override
    protected String getListName() {
        return "UserListModel"; //$NON-NLS-1$
    }
}
