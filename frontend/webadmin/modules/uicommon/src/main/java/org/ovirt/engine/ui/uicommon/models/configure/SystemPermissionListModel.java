package org.ovirt.engine.ui.uicommon.models.configure;
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

import org.ovirt.engine.ui.uicommon.models.users.*;
import org.ovirt.engine.core.common.businessentities.*;

import org.ovirt.engine.core.common.interfaces.*;
import org.ovirt.engine.core.common.users.*;
import org.ovirt.engine.ui.uicommon.*;
import org.ovirt.engine.ui.uicommon.models.*;

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


	public SystemPermissionListModel()
	{
		setTitle("System Permission");

		setAddCommand(new UICommand("Add", this));
		setRemoveCommand(new UICommand("Remove", this));

		getSearchCommand().Execute();

		UpdateActionAvailability();
	}

	@Override
	protected void AsyncSearch()
	{
		super.AsyncSearch();

		setAsyncResult(Frontend.RegisterQuery(VdcQueryType.GetSystemPermissions, new VdcQueryParametersBase()));
		setItems(getAsyncResult().getData());
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
		setWindow(model);
		model.setTitle("Add System Permission to User");
		model.setHashName("add_system_permission_to_user");
		//model.Role.IsAvailable = true;
		//model.ExcludeItems = Items;

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

		roles role = (roles)model.getRole().getSelectedItem();

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
			public void Executed(FrontendMultipleActionAsyncResult  result) {

			AdElementListModel localModel = (AdElementListModel)result.getState();
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
			permissions permission = (permissions)item;
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
			ConfirmationModel model = (ConfirmationModel)getWindow();

			if (model.getProgress() != null)
			{
				return;
			}

			java.util.ArrayList<VdcActionParametersBase> list = new java.util.ArrayList<VdcActionParametersBase>();
			for (Object perm : getSelectedItems())
			{
				PermissionsOperationsParametes tempVar = new PermissionsOperationsParametes();
				tempVar.setPermission((permissions)perm);
				list.add(tempVar);
			}


			model.StartProgress(null);

			Frontend.RunMultipleAction(VdcActionType.RemovePermission, list,
		new IFrontendMultipleActionAsyncCallback() {
			@Override
			public void Executed(FrontendMultipleActionAsyncResult  result) {

				ConfirmationModel localModel = (ConfirmationModel)result.getState();
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
}