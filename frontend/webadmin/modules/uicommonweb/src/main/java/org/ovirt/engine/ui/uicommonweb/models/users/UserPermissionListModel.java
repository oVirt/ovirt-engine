package org.ovirt.engine.ui.uicommonweb.models.users;
import java.util.Collections;
import org.ovirt.engine.core.compat.*;
import org.ovirt.engine.ui.uicompat.*;
import org.ovirt.engine.core.common.businessentities.*;
import org.ovirt.engine.core.common.vdscommands.*;
import org.ovirt.engine.core.common.queries.*;
import org.ovirt.engine.core.common.action.*;
import org.ovirt.engine.ui.frontend.*;
import org.ovirt.engine.ui.uicommonweb.*;
import org.ovirt.engine.ui.uicommonweb.models.*;
import org.ovirt.engine.core.common.*;

import org.ovirt.engine.core.common.businessentities.*;

import org.ovirt.engine.ui.uicommonweb.*;
import org.ovirt.engine.ui.uicommonweb.models.*;

@SuppressWarnings("unused")
public class UserPermissionListModel extends SearchableListModel
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



	public DbUser getEntity()
	{
		return (DbUser)((super.getEntity() instanceof DbUser) ? super.getEntity() : null);
	}
	public void setEntity(DbUser value)
	{
		super.setEntity(value);
	}


	public UserPermissionListModel()
	{
		setTitle("Permissions");

		setRemoveCommand(new UICommand("Remove", this));

		UpdateActionAvailability();
	}

	@Override
	protected void OnEntityChanged()
	{
		super.OnEntityChanged();
		getSearchCommand().Execute();
	}

	@Override
	public void Search()
	{
		if (getEntity() != null)
		{
			super.Search();
		}
	}

	@Override
	protected void SyncSearch()
	{
		if (getEntity() == null)
		{
			return;
		}

		super.SyncSearch(VdcQueryType.GetPermissionsByAdElementId, new MultilevelAdministrationByAdElementIdParameters(getEntity().getuser_id()));
	}

	@Override
	protected void AsyncSearch()
	{
		super.AsyncSearch();

		setAsyncResult(Frontend.RegisterQuery(VdcQueryType.GetPermissionsByAdElementId, new MultilevelAdministrationByAdElementIdParameters(getEntity().getuser_id())));
		setItems(getAsyncResult().getData());
	}

	public void remove()
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

		java.util.ArrayList<String> list = new java.util.ArrayList<String>();
		for (permissions a : Linq.<permissions>Cast(getSelectedItems()))
		{
			list.add("Role " + a.getRoleName() + " on User " + a.getOwnerName());
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
		else
		{
			Cancel();
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
		getRemoveCommand().setIsExecutionAllowed(getSelectedItem() != null || (getSelectedItems() != null && getSelectedItems().size() > 0));
	}

	@Override
	public void ExecuteCommand(UICommand command)
	{
		super.ExecuteCommand(command);

		if (command == getRemoveCommand())
		{
			remove();
		}
		if (StringHelper.stringsEqual(command.getName(), "OnRemove"))
		{
			OnRemove();
		}
		if (StringHelper.stringsEqual(command.getName(), "Cancel"))
		{
			Cancel();
		}
	}
}