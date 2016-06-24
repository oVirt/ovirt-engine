package org.ovirt.engine.ui.uicommonweb.models.users;

import java.util.ArrayList;
import java.util.Collection;
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
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.Tags;
import org.ovirt.engine.core.common.businessentities.aaa.DbGroup;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.SearchParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.searchbackend.SearchObjects;
import org.ovirt.engine.core.searchbackend.VdcUserConditionFieldAutoCompleter.UserOrGroup;
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
import org.ovirt.engine.ui.uicommonweb.models.users.AdElementListModel.AdSearchType;
import org.ovirt.engine.ui.uicommonweb.place.WebAdminApplicationPlaces;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.FrontendActionAsyncResult;
import org.ovirt.engine.ui.uicompat.FrontendMultipleActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;
import org.ovirt.engine.ui.uicompat.IFrontendMultipleActionAsyncCallback;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.inject.Inject;

public class UserListModel extends ListWithSimpleDetailsModel<Void, DbUser> {
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

    private UICommand privateAssignTagsCommand;

    public UICommand getAssignTagsCommand() {
        return privateAssignTagsCommand;
    }

    private void setAssignTagsCommand(UICommand value) {
        privateAssignTagsCommand = value;
    }

    protected Object[] getSelectedKeys() {
        if (getSelectedItems() == null) {
            return new Object[0];
        }
        else {
            ArrayList<Object> items = new ArrayList<>();
            for (Object i : getSelectedItems()) {
                items.add(((Cluster) i).getId());
            }
            return items.toArray(new Object[] {});
        }
    }

    @Inject
    public UserListModel(final UserGroupListModel userGroupListModel,
            final UserEventNotifierListModel userEventNotifierListModel, final UserGeneralModel userGeneralModel,
            final UserQuotaListModel userQuotaListModel, final UserPermissionListModel userPermissionListModel,
            final UserEventListModel userEventListModel) {
        setIsTimerDisabled(true);
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

    public void assignTags() {
        if (getWindow() != null) {
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
    private UserOrGroup userOrGroup;

    private void getAttachedTagsToSelectedUsers(TagListModel model) {
        ArrayList<Guid> userIds = new ArrayList<>();
        ArrayList<Guid> grpIds = new ArrayList<>();

        attachedTagsToEntities = new HashMap<>();
        allAttachedTags = new ArrayList<>();
        selectedItemsCounter = 0;

        for (Object item : getSelectedItems()) {
            DbUser user = (DbUser) item;
            if (!user.isGroup()) {
                userIds.add(user.getId());
            }
            else {
                grpIds.add(user.getId());
            }
        }

        for (Guid userId : userIds) {
            AsyncDataProvider.getInstance().getAttachedTagsToUser(new AsyncQuery(new Object[] { this, model },
                    new INewAsyncCallback() {
                        @Override
                        public void onSuccess(Object target, Object returnValue) {

                            Object[] array = (Object[]) target;
                            UserListModel userListModel = (UserListModel) array[0];
                            TagListModel tagListModel = (TagListModel) array[1];
                            userListModel.allAttachedTags.addAll((ArrayList<Tags>) returnValue);
                            userListModel.selectedItemsCounter++;
                            if (userListModel.selectedItemsCounter == userListModel.getSelectedItems().size()) {
                                postGetAttachedTags(userListModel, tagListModel);
                            }

                        }
                    }),
                    userId);
        }
        for (Guid grpId : grpIds) {
            AsyncDataProvider.getInstance().getAttachedTagsToUserGroup(new AsyncQuery(new Object[] { this, model },
                    new INewAsyncCallback() {
                        @Override
                        public void onSuccess(Object target, Object returnValue) {

                            Object[] array = (Object[]) target;
                            UserListModel userListModel = (UserListModel) array[0];
                            TagListModel tagListModel = (TagListModel) array[1];
                            userListModel.allAttachedTags.addAll((ArrayList<Tags>) returnValue);
                            userListModel.selectedItemsCounter++;
                            if (userListModel.selectedItemsCounter == userListModel.getSelectedItems().size()) {
                                postGetAttachedTags(userListModel, tagListModel);
                            }

                        }
                    }),
                    grpId);
        }
    }

    private void postGetAttachedTags(UserListModel userListModel, TagListModel tagListModel) {
        if (userListModel.getLastExecutedCommand() == getAssignTagsCommand()) {
            ArrayList<Tags> attachedTags =
                    Linq.distinct(userListModel.allAttachedTags, new TagsEqualityComparer());
            for (Tags a : attachedTags) {
                int count = 0;
                for (Tags b : allAttachedTags) {
                    if (b.getTagId().equals(a.getTagId())) {
                        count++;
                    }
                }

                userListModel.attachedTagsToEntities.put(a.getTagId(), count == userListModel.getSelectedItems()
                        .size());
            }
            tagListModel.setAttachedTagsToEntities(userListModel.attachedTagsToEntities);
        }
        else if ("OnAssignTags".equals(userListModel.getLastExecutedCommand().getName())) { //$NON-NLS-1$
            userListModel.postOnAssignTags(tagListModel.getAttachedTagsToEntities());
        }
    }

    private void onAssignTags() {
        TagListModel model = (TagListModel) getWindow();

        getAttachedTagsToSelectedUsers(model);
    }

    public void postOnAssignTags(Map<Guid, Boolean> attachedTags) {
        TagListModel model = (TagListModel) getWindow();
        ArrayList<Guid> userIds = new ArrayList<>();
        ArrayList<Guid> grpIds = new ArrayList<>();

        for (Object item : getSelectedItems()) {
            DbUser user = (DbUser) item;
            if (user.isGroup()) {
                grpIds.add(user.getId());
            }
            else {
                userIds.add(user.getId());
            }
        }

        // prepare attach/detach lists
        ArrayList<Guid> tagsToAttach = new ArrayList<>();
        ArrayList<Guid> tagsToDetach = new ArrayList<>();

        if (model.getItems() != null && model.getItems().size() > 0) {
            ArrayList<TagModel> tags = (ArrayList<TagModel>) model.getItems();
            TagModel rootTag = tags.get(0);
            TagModel.recursiveEditAttachDetachLists(rootTag, attachedTags, tagsToAttach, tagsToDetach);
        }

        ArrayList<VdcActionParametersBase> usersToAttach = new ArrayList<>();
        ArrayList<VdcActionParametersBase> grpsToAttach = new ArrayList<>();
        for (Guid tag_id : tagsToAttach) {
            if (userIds.size() > 0) {
                usersToAttach.add(new AttachEntityToTagParameters(tag_id, userIds));
            }
            if (grpIds.size() > 0) {
                grpsToAttach.add(new AttachEntityToTagParameters(tag_id, grpIds));
            }
        }
        if (usersToAttach.size() > 0) {
            Frontend.getInstance().runMultipleAction(VdcActionType.AttachUserToTag, usersToAttach);
        }
        if (grpsToAttach.size() > 0) {
            Frontend.getInstance().runMultipleAction(VdcActionType.AttachUserGroupToTag, grpsToAttach);
        }

        ArrayList<VdcActionParametersBase> usersToDetach = new ArrayList<>();
        ArrayList<VdcActionParametersBase> grpsToDetach = new ArrayList<>();
        for (Guid tag_id : tagsToDetach) {
            if (userIds.size() > 0) {
                usersToDetach.add(new AttachEntityToTagParameters(tag_id, userIds));
            }
            if (grpIds.size() > 0) {
                grpsToDetach.add(new AttachEntityToTagParameters(tag_id, grpIds));
            }
        }
        if (usersToDetach.size() > 0) {
            Frontend.getInstance().runMultipleAction(VdcActionType.DetachUserFromTag, usersToDetach);
        }
        if (grpsToDetach.size() > 0) {
            Frontend.getInstance().runMultipleAction(VdcActionType.DetachUserGroupFromTag, grpsToDetach);
        }

        cancel();
    }

    public void add() {
        if (getWindow() != null) {
            return;
        }

        AdElementListModel model = new AdElementListModel();
        if (getUserOrGroup() == UserOrGroup.Group) {
            model.setSearchType(AdSearchType.GROUP);
        } else {
            model.setSearchType(AdSearchType.USER);
        }
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().addUsersAndGroupsTitle());
        model.setHelpTag(HelpTag.add_users_and_groups);
        model.setHashName("add_users_and_groups"); //$NON-NLS-1$
        model.setIsRoleListHidden(true);
        model.getIsEveryoneSelectionHidden().setEntity(true);

        UICommand addCommand = new UICommand("OnAdd", this); //$NON-NLS-1$
        addCommand.setTitle(ConstantsManager.getInstance().getConstants().add());
        model.getCommands().add(addCommand);

        UICommand addAndCloseCommand = new UICommand("OnAddAndClose", this); //$NON-NLS-1$
        addAndCloseCommand.setTitle(ConstantsManager.getInstance().getConstants().addAndClose());
        addAndCloseCommand.setIsDefault(true);
        model.getCommands().add(addAndCloseCommand);

        UICommand closeCommand = new UICommand("Cancel", this); //$NON-NLS-1$
        closeCommand.setTitle(ConstantsManager.getInstance().getConstants().close());
        closeCommand.setIsCancel(true);
        model.getCommands().add(closeCommand);
    }

    public UserOrGroup getUserOrGroup() {
        return this.userOrGroup;
    }

    public void setUserOrGroup(UserOrGroup value) {
        this.userOrGroup = value;
    }

    public void remove() {
        if (getWindow() != null) {
            return;
        }

        ConfirmationModel model = new ConfirmationModel();
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().removeUsersTitle());
        model.setHelpTag(HelpTag.remove_user);
        model.setHashName("remove_user"); //$NON-NLS-1$

        ArrayList<String> list = new ArrayList<>();
        for (DbUser item : Linq.<DbUser> cast(getSelectedItems())) {
            list.add(item.getFirstName());
        }
        model.setItems(list);

        UICommand tempVar = UICommand.createDefaultOkUiCommand("OnRemove", this); //$NON-NLS-1$
        model.getCommands().add(tempVar);
        UICommand tempVar2 = UICommand.createCancelUiCommand("Cancel", this); //$NON-NLS-1$
        model.getCommands().add(tempVar2);
    }

    @Override
    public boolean isSearchStringMatch(String searchString) {
        return searchString.trim().toLowerCase().startsWith("user"); //$NON-NLS-1$
    }

    @Override
    protected void syncSearch() {
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

    private IFrontendActionAsyncCallback nopCallback = new IFrontendActionAsyncCallback() {
        @Override
        public void executed(FrontendActionAsyncResult result) {
            // Nothing.
        }
    };

    @Override
    protected void updateDetailsAvailability() {
        if (getSelectedItem() != null) {
            DbUser adUser = getSelectedItem();
            userGroupListModel.setIsAvailable(!adUser.isGroup());
            userEventNotifierListModel.setIsAvailable(!adUser.isGroup());
        }
    }

    public void cancel() {
        setWindow(null);
    }

    public void onAdd(final boolean closeWindow) {
        AdElementListModel model = (AdElementListModel) getWindow();

        if (model.getProgress() != null) {
            return;
        }

        if (model.getSelectedItems() == null) {
            cancel();
            return;
        }

        List<EntityModel<DbUser>> selectedItems = new ArrayList<>();
        for (EntityModel<DbUser> dbUserEntity : model.getItems()) {
            if (dbUserEntity.getIsSelected()) {
                selectedItems.add(dbUserEntity);
            }
        }

        List<VdcActionType> actionsList = new ArrayList<>(selectedItems.size());
        List<VdcActionParametersBase> parametersList = new ArrayList<>(selectedItems.size());
        VdcActionParametersBase parameters = null;
        for (EntityModel<DbUser> item : selectedItems) {
            if (item.getEntity().isGroup()) {
                actionsList.add(VdcActionType.AddGroup);
                DbGroup grp = new DbGroup();
                grp.setExternalId(item.getEntity().getExternalId());
                grp.setName(item.getEntity().getFirstName());
                grp.setNamespace(item.getEntity().getNamespace());
                grp.setId(item.getEntity().getId());
                grp.setDomain(item.getEntity().getDomain());
                parameters = new AddGroupParameters(grp);
            }
            else {
                actionsList.add(VdcActionType.AddUser);
                parameters = new AddUserParameters(item.getEntity());
            }
            parametersList.add(parameters);
        }

        model.startProgress();

        IFrontendActionAsyncCallback lastCallback = new IFrontendActionAsyncCallback() {
            @Override
            public void executed(FrontendActionAsyncResult result) {
                AdElementListModel localModel = (AdElementListModel) result.getState();
                localModel.stopProgress();
                //Refresh user list.
                syncSearch();
                if (closeWindow) {
                    cancel();
                }
            }
        };

        Collection<EntityModel<DbUser>> currentItems = model.getItems();
        List<IFrontendActionAsyncCallback> callbacksList = new ArrayList<>(selectedItems.size());
        for (EntityModel<DbUser> user: selectedItems) {
            callbacksList.add(nopCallback);
            currentItems.remove(user);
        }
        callbacksList.remove(callbacksList.size() - 1);
        callbacksList.add(lastCallback);

        //Refresh display
        model.setItems(null);
        model.setItems(currentItems);
        Frontend.getInstance().runMultipleActions(actionsList, parametersList, callbacksList, lastCallback, model);
    }

    public void onRemove() {
        List<DbUser> selectedItems = Linq.cast(getSelectedItems());

        ArrayList<VdcActionParametersBase> userPrms = new ArrayList<>();
        ArrayList<VdcActionParametersBase> groupPrms = new ArrayList<>();
        for (DbUser item : selectedItems) {
            if (!item.isGroup()) {
                userPrms.add(new IdParameters(item.getId()));
            }
            else {
                groupPrms.add(new IdParameters(item.getId()));
            }
        }

        IFrontendMultipleActionAsyncCallback lastCallback = new IFrontendMultipleActionAsyncCallback() {
            @Override
            public void executed(FrontendMultipleActionAsyncResult result) {
                Scheduler.get().scheduleDeferred(new ScheduledCommand() {

                    @Override
                    public void execute() {
                        //Refresh user list.
                        syncSearch();
                        cancel();
                    }
                });
            }
        };
        if (getUserOrGroup() == UserOrGroup.User) {
            if (userPrms.size() > 0) {
                Frontend.getInstance().runMultipleAction(VdcActionType.RemoveUser, userPrms, lastCallback);
            }
        } else if (getUserOrGroup() == UserOrGroup.Group) {
            if (groupPrms.size() > 0) {
                Frontend.getInstance().runMultipleAction(VdcActionType.RemoveGroup, groupPrms, lastCallback);
            }
        }
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
        ArrayList items =
                ((ArrayList) getSelectedItems() != null) ? (ArrayList) getSelectedItems()
                        : new ArrayList();

        getRemoveCommand().setIsExecutionAllowed(items.size() > 0
                && VdcActionUtils.canExecute(items, DbUser.class, VdcActionType.RemoveUser));

        getAssignTagsCommand().setIsExecutionAllowed(items.size() > 0);
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
        if (command == getAssignTagsCommand()) {
            assignTags();
        }

        if ("Cancel".equals(command.getName())) { //$NON-NLS-1$
            cancel();
        }
        if ("OnAssignTags".equals(command.getName())) { //$NON-NLS-1$
            onAssignTags();
        }
        if ("OnAdd".equals(command.getName())) { //$NON-NLS-1$
            onAdd(false);
        }
        if ("OnAddAndClose".equals(command.getName())) { //$NON-NLS-1$
            onAdd(true);
        }
        if ("OnRemove".equals(command.getName())) { //$NON-NLS-1$
            onRemove();
        }
    }

    @Override
    protected String getListName() {
        return "UserListModel"; //$NON-NLS-1$
    }
}
