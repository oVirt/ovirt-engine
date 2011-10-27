package org.ovirt.engine.ui.uicommon.models.configure.roles_ui;
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

import org.ovirt.engine.core.common.businessentities.*;

import org.ovirt.engine.ui.uicommon.*;
import org.ovirt.engine.ui.uicommon.models.*;
import org.ovirt.engine.ui.uicommon.models.configure.*;

@SuppressWarnings("unused")
public class RolePermissionListModel extends SearchableListModel
{

	private UICommand privateRemoveCommand;
	public UICommand getRemoveCommand()
	{
		return privateRemoveCommand;
	}
	private void setRemoveCommand(UICommand value)
	{
		privateRemoveCommand = value;
	}



	public roles getEntity()
	{
		return (roles)super.getEntity();
	}
	public void setEntity(roles value)
	{
		super.setEntity(value);
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


	public RolePermissionListModel()
	{
		setTitle("Role's Permissions");

		setRemoveCommand(new UICommand("Remove", this));

		getSearchCommand().Execute();

		UpdateActionAvailability();
	}

	@Override
	protected void SyncSearch()
	{
		super.SyncSearch();

		VdcQueryReturnValue retValue = Frontend.RunQuery(VdcQueryType.GetPermissionByRoleId, new MultilevelAdministrationByRoleIdParameters(getEntity().getId()));

		if (retValue != null && retValue.getSucceeded())
		{
			//Items = ((List<IVdcQueryable>)retValue.ReturnValue).Cast<roles>().ToList();
			setItems(Linq.<permissions>Cast((java.util.ArrayList<permissions>)retValue.getReturnValue()));
		}

		else
		{
			setItems(new java.util.ArrayList<roles>());
		}
	}

	@Override
	protected void OnEntityChanged()
	{
		super.OnEntityChanged();
		AsyncSearch();
	}

	@Override
	protected void AsyncSearch()
	{
		super.AsyncSearch();

		if (getEntity() == null)
		{
			return;
		}

		setAsyncResult(Frontend.RegisterQuery(VdcQueryType.GetPermissionByRoleId, new MultilevelAdministrationByRoleIdParameters(getEntity().getId())));
		setItems(getAsyncResult().getData());
	}

	private void UpdateActionAvailability()
	{
		getRemoveCommand().setIsExecutionAllowed(getSelectedItem() != null || (getSelectedItems() != null && getSelectedItems().size() > 0));

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
		model.setTitle("Remove Permission");
		model.setHashName("remove_permission");
		model.setMessage("Permission");

		java.util.ArrayList<String> items = new java.util.ArrayList<String>();
		for (Object a : getSelectedItems())
		{
			items.add("Role " + ((permissions)a).getRoleName() + " on User " + ((permissions)a).getOwnerName());
		}
		model.setItems(items);

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

		if (command == getRemoveCommand())
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