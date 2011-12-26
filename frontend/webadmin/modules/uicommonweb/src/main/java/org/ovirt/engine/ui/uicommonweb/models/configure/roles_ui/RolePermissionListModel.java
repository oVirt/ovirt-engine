package org.ovirt.engine.ui.uicommonweb.models.configure.roles_ui;
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
import org.ovirt.engine.ui.uicommonweb.models.bookmarks.BookmarkListModel;
import org.ovirt.engine.ui.uicommonweb.models.configure.*;

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


	public RolePermissionListModel()
	{
		setTitle("Role's Permissions");

		setRemoveCommand(new UICommand("Remove", this));

		setSearchPageSize(1000);
		
		UpdateActionAvailability();
	}

	@Override
	protected void SyncSearch()
	{
		super.SyncSearch();

		AsyncQuery _asyncQuery = new AsyncQuery();
		_asyncQuery.setModel(this);
		_asyncQuery.asyncCallback = new INewAsyncCallback() { public void OnSuccess(Object model, Object ReturnValue)
		{
			RolePermissionListModel permissionListModel = (RolePermissionListModel)model;
			permissionListModel.setItems((Iterable)((VdcQueryReturnValue)ReturnValue).getReturnValue());
		}};

		MultilevelAdministrationByRoleIdParameters tempVar = new MultilevelAdministrationByRoleIdParameters(getEntity().getId());
		tempVar.setRefresh(getIsQueryFirstTime());
		Frontend.RunQuery(VdcQueryType.GetPermissionByRoleId, tempVar, _asyncQuery);
	}

	@Override
	protected void OnEntityChanged()
	{
		super.OnEntityChanged();
		Search();
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
    @Override
    protected String getListName() {
        return "RolePermissionListModel";
    }
}