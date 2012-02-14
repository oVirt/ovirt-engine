package org.ovirt.engine.ui.uicommon.models.users;
import java.util.Collections;
import org.ovirt.engine.core.compat.*;
import org.ovirt.engine.ui.uicompat.*;
import org.ovirt.engine.core.common.businessentities.*;
import org.ovirt.engine.core.common.vdscommands.*;
import org.ovirt.engine.core.common.queries.*;
import org.ovirt.engine.core.common.action.*;
import org.ovirt.engine.ui.frontend.*;
import org.ovirt.engine.ui.uicommon.*;
import org.ovirt.engine.ui.uicommon.models.*;
import org.ovirt.engine.core.common.*;

import org.ovirt.engine.ui.uicommon.models.configure.*;
import org.ovirt.engine.ui.uicommon.models.tags.*;
import org.ovirt.engine.core.common.*;
import org.ovirt.engine.core.common.users.*;
import org.ovirt.engine.core.common.queries.*;

import org.ovirt.engine.core.common.interfaces.*;
import org.ovirt.engine.core.common.businessentities.*;
import org.ovirt.engine.ui.uicommon.*;
import org.ovirt.engine.ui.uicommon.models.*;

@SuppressWarnings("unused")
public class UserListModel extends ListWithDetailsModel
{
	public static Guid EveryoneUserId = new Guid("eee00000-0000-0000-0000-123456789eee");


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



	private Model window;
	public Model getWindow()
	{
		return window;
	}
	public void setWindow(Model value)
	{
		if (window != value)
		{
			window = value;
			OnPropertyChanged(new PropertyChangedEventArgs("Window"));
		}
	}

		//			get { return SelectedItems == null ? new object[0] : SelectedItems.Cast<DbUser>().Select(a => a.user_id).Cast<object>().ToArray(); }
	protected Object[] getSelectedKeys()
	{
		if (getSelectedItems() == null)
		{
			return new Object[0];
		}
		else
		{
			java.util.ArrayList<Object> items = new java.util.ArrayList<Object>();
			for (Object i : getSelectedItems())
			{
				items.add(((VDSGroup)i).getId());
			}
			return items.toArray(new Object[]{});
		}
	}


	public UserListModel()
	{
		setTitle("Users");

		setDefaultSearchString("Users:");
		setSearchString(getDefaultSearchString());

		setAddCommand(new UICommand("Add", this));
		setRemoveCommand(new UICommand("Remove", this));
		setAssignTagsCommand(new UICommand("AssignTags", this));

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
		model.setTitle("Assign Tags");
		model.setHashName("assign_tags_users");

		model.setAttachedTagsToEntities(GetAttachedTagsToSelectedUsers());
		java.util.ArrayList<TagModel> tags = (java.util.ArrayList<TagModel>)model.getItems();

		UICommand tempVar = new UICommand("OnAssignTags", this);
		tempVar.setTitle("OK");
		tempVar.setIsDefault(true);
		model.getCommands().add(tempVar);
		UICommand tempVar2 = new UICommand("Cancel", this);
		tempVar2.setTitle("Cancel");
		tempVar2.setIsCancel(true);
		model.getCommands().add(tempVar2);
	}

	private java.util.HashMap<Guid, Boolean> GetAttachedTagsToSelectedUsers()
	{
		java.util.HashMap<Guid, Boolean> tags = new java.util.HashMap<Guid, Boolean>();

		//			List<Guid> userIds = SelectedItems
		//				.Cast<DbUser>()
		//				.Select(a => a.user_id)
		//				.ToList();

		java.util.ArrayList<Guid> userIds = new java.util.ArrayList<Guid>();
		java.util.ArrayList<Guid> grpIds = new java.util.ArrayList<Guid>();

		for (Object item : getSelectedItems())
		{
			DbUser user = (DbUser)item;
			if (!user.getIsGroup())
			{
				userIds.add(user.getuser_id());
			}
			else
			{
				grpIds.add(user.getuser_id());
			}
		}

		//			List<DbUser> allAttachedTags = userIds.SelectMany(a => DataProvider.GetAttachedTagsToUser(a)).ToList();

		//			List<DbUser> attachedTags = allAttachedTags
		//				.Distinct(new TagsEqualityComparer())
		//				.ToList();

		java.util.ArrayList<org.ovirt.engine.core.common.businessentities.tags> allAttachedTags = new java.util.ArrayList<org.ovirt.engine.core.common.businessentities.tags>();

		for (Guid userId : userIds)
		{
			allAttachedTags.addAll(DataProvider.GetAttachedTagsToUser(userId));
		}
		for (Guid grpId : grpIds)
		{
			allAttachedTags.addAll(DataProvider.GetAttachedTagsToUserGroup(grpId));
		}

//C# TO JAVA CONVERTER TODO TASK: There is no Java equivalent to LINQ queries:
		java.util.ArrayList<org.ovirt.engine.core.common.businessentities.tags> attachedTags = (java.util.ArrayList<org.ovirt.engine.core.common.businessentities.tags>)Linq.Distinct(allAttachedTags, new TagsEqualityComparer());

		//attachedTags.Each(a => { tags.Add(a.tag_id, allAttachedTags.Count(b => b.tag_id == a.tag_id) == userIds.Count() ? true : false); });
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

			tags.put(a.gettag_id(), count == userIds.size() + grpIds.size());
		}

		return tags;
	}

	private void OnAssignTags()
	{
		TagListModel model = (TagListModel)getWindow();

		//			List<DbUser> userIds = SelectedItems
		//				.Cast<DbUser>()
		//				.Select(a => a.user_id)
		//				.ToList();

		java.util.ArrayList<Guid> userIds = new java.util.ArrayList<Guid>();
		java.util.ArrayList<Guid> grpIds = new java.util.ArrayList<Guid>();

		for (Object item : getSelectedItems())
		{
			DbUser user = (DbUser)item;
			if (user.getIsGroup())
			{
				grpIds.add(user.getuser_id());
			}
			else
			{
				userIds.add(user.getuser_id());
			}
		}

		java.util.HashMap<Guid, Boolean> attachedTags = GetAttachedTagsToSelectedUsers();

		//prepare attach/detach lists
		java.util.ArrayList<Guid> tagsToAttach = new java.util.ArrayList<Guid>();
		java.util.ArrayList<Guid> tagsToDetach = new java.util.ArrayList<Guid>();

		//model.Items
		//     .Cast<TagModel>()
		//     .First()
		//     .EachRecursive(a => a.Children, (a, b) =>
		//     {
		//         if (a.Selection == true && (!attachedTags.ContainsKey(a.Id) || attachedTags[a.Id] == false))
		//         {
		//             tagsToAttach.Add(a.Id);
		//         }
		//         else if (a.Selection == false && attachedTags.ContainsKey(a.Id))
		//         {
		//             tagsToDetach.Add(a.Id);
		//         }
		//     });

		if (model.getItems() != null && ((java.util.ArrayList<TagModel>)model.getItems()).size() > 0)
		{
			java.util.ArrayList<TagModel> tags = (java.util.ArrayList<TagModel>)model.getItems();
			TagModel rootTag = tags.get(0);
			TagModel.RecursiveEditAttachDetachLists(rootTag, attachedTags, tagsToAttach, tagsToDetach);
		}

		java.util.ArrayList<VdcActionParametersBase> usersToAttach = new java.util.ArrayList<VdcActionParametersBase>();
		java.util.ArrayList<VdcActionParametersBase> grpsToAttach = new java.util.ArrayList<VdcActionParametersBase>();
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
		//			Frontend.RunMultipleActions(VdcActionType.AttachUserToTag,
		//				tagsToAttach.Select(a =>
		//                  (VdcActionParametersBase)new AttachEntityToTagParameters(a, userIds.Select(userId => (Guid)userId).ToList())
		//			)
		//			.ToList()
		//		);

		//			Frontend.RunMultipleActions(VdcActionType.DetachUserFromTag,
		//				tagsToDetach.Select(a =>
		//                  (VdcActionParametersBase)new AttachEntityToTagParameters(a, userIds.Select(userId => (Guid)userId).ToList())
		//			)
		//			.ToList()
		//		);

		java.util.ArrayList<VdcActionParametersBase> usersToDetach = new java.util.ArrayList<VdcActionParametersBase>();
		java.util.ArrayList<VdcActionParametersBase> grpsToDetach = new java.util.ArrayList<VdcActionParametersBase>();
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
		model.setTitle("Add Users and Groups");
		model.setHashName("add_users_and_groups");
		model.setExcludeItems(DataProvider.GetUserList());
		model.setIsRoleListHidden(true);


		UICommand tempVar = new UICommand("OnAdd", this);
		tempVar.setTitle("OK");
		tempVar.setIsDefault(true);
		model.getCommands().add(tempVar);
		UICommand tempVar2 = new UICommand("Cancel", this);
		tempVar2.setTitle("Cancel");
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
		model.setTitle("Remove User(s)");
		model.setHashName("remove_user");
		model.setMessage("User(s)");

		java.util.ArrayList<String> list = new java.util.ArrayList<String>();
		for (DbUser item : Linq.<DbUser>Cast(getSelectedItems()))
		{
			list.add(item.getname());
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

	@Override
	public boolean IsSearchStringMatch(String searchString)
	{
		return searchString.trim().toLowerCase().startsWith("user");
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
			DbUser adUser = (DbUser)getSelectedItem();
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
		AdElementListModel model = (AdElementListModel)getWindow();

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
			EntityModel entityModel = (EntityModel)item;
			if (entityModel.getIsSelected())
			{
				items.add((DbUser)entityModel.getEntity());
			}
		}

		java.util.ArrayList<VdcActionParametersBase> parameters = new java.util.ArrayList<VdcActionParametersBase>();
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
				tempVar2.setAdGroup(new ad_groups(item.getuser_id(), item.getname(), item.getdomain()));
				parameters.add(tempVar2);
			}
		}


		model.StartProgress(null);

		Frontend.RunMultipleAction(VdcActionType.AddUser, parameters,
		new IFrontendMultipleActionAsyncCallback() {
			@Override
			public void Executed(FrontendMultipleActionAsyncResult  result) {

			AdElementListModel localModel = (AdElementListModel)result.getState();
			localModel.StopProgress();
			Cancel();

			}
		}, model);
	}

	public void OnRemove()
	{
		java.util.ArrayList<DbUser> items = Linq.<DbUser>Cast(getSelectedItems());

		java.util.ArrayList<VdcActionParametersBase> userPrms = new java.util.ArrayList<VdcActionParametersBase>();
		java.util.ArrayList<VdcActionParametersBase> groupPrms = new java.util.ArrayList<VdcActionParametersBase>();
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

	//private void AssignTags()
	//{
	//    base.AssignTags();

	//    DbUser user = (DbUser)SelectedItem;
	//    AssignTagsModel.AttachedTags = DataProvider.GetAttachedTagsToUser(user.user_id);
	//}

	//public override void NotifyTagsAttached(IList<tags> tags)
	//{
	//    base.NotifyTagsAttached(tags);

	//    var dbUsers = SelectedItems.Cast<DbUser>();

	//    var userIds = dbUsers
	//        .Where(a => !a.IsGroup)
	//        .Select(a => a.user_id)
	//        .ToList();

	//    Frontend.RunMultipleActions(VdcActionType.AttachUserToTag,
	//        tags.Select(a =>
	//            (VdcActionParametersBase)new AttachEntityToTagParameters(a.tag_id, userIds)
	//        )
	//        .ToList()
	//    );

	//    var groupIds = dbUsers
	//        .Where(a => a.IsGroup)
	//        .Select(a => a.user_id)
	//        .ToList();

	//    Frontend.RunMultipleActions(VdcActionType.AttachUserGroupToTag,
	//        tags.Select(a =>
	//            (VdcActionParametersBase)new AttachEntityToTagParameters(a.tag_id, groupIds)
	//        )
	//        .ToList()
	//    );
	//}

	//public override void NotifyTagsDetached(IList<tags> tags)
	//{
	//    base.NotifyTagsDetached(tags);

	//    var dbUsers = SelectedItems.Cast<DbUser>();

	//    var userIds = dbUsers
	//        .Where(a => !a.IsGroup)
	//        .Select(a => a.user_id)
	//        .ToList();

	//    Frontend.RunMultipleActions(VdcActionType.DetachUserFromTag,
	//        tags.Select(a =>
	//            (VdcActionParametersBase)new AttachEntityToTagParameters(a.tag_id, userIds)
	//        )
	//        .ToList()
	//    );

	//    var groupIds = dbUsers
	//        .Where(a => a.IsGroup)
	//        .Select(a => a.user_id)
	//        .ToList();

	//    Frontend.RunMultipleActions(VdcActionType.DetachUserGroupFromTag,
	//        tags.Select(a =>
	//            (VdcActionParametersBase)new AttachEntityToTagParameters(a.tag_id, groupIds)
	//        )
	//        .ToList()
	//    );
	//}

	//public override void OnSelectionChanged(object item, IList items)
	//{
	//    base.OnSelectionChanged(item, items);

	//    UpdateActionAvailability();
	//}


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
		java.util.ArrayList items = (((java.util.ArrayList)getSelectedItems()) != null) ? (java.util.ArrayList)getSelectedItems() : new java.util.ArrayList();

		getRemoveCommand().setIsExecutionAllowed(items.size() > 0 && VdcActionUtils.CanExecute(items, DbUser.class, VdcActionType.RemoveUser));

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

		if (StringHelper.stringsEqual(command.getName(), "Cancel"))
		{
			Cancel();
		}
		if (StringHelper.stringsEqual(command.getName(), "OnAssignTags"))
		{
			OnAssignTags();
		}
		if (StringHelper.stringsEqual(command.getName(), "OnAdd"))
		{
			OnAdd();
		}

		if (StringHelper.stringsEqual(command.getName(), "OnRemove"))
		{
			OnRemove();
		}
	}
}