package org.ovirt.engine.ui.uicommon.models.storage;
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

import org.ovirt.engine.core.common.interfaces.*;
import org.ovirt.engine.core.common.queries.*;

import org.ovirt.engine.core.common.businessentities.*;
import org.ovirt.engine.core.common.*;
import org.ovirt.engine.ui.uicommon.*;
import org.ovirt.engine.ui.uicommon.models.*;

@SuppressWarnings("unused")
public class StorageDataCenterListModel extends SearchableListModel
{

	private UICommand privateAttachCommand;
	public UICommand getAttachCommand()
	{
		return privateAttachCommand;
	}
	private void setAttachCommand(UICommand value)
	{
		privateAttachCommand = value;
	}
	private UICommand privateDetachCommand;
	public UICommand getDetachCommand()
	{
		return privateDetachCommand;
	}
	private void setDetachCommand(UICommand value)
	{
		privateDetachCommand = value;
	}
	private UICommand privateActivateCommand;
	public UICommand getActivateCommand()
	{
		return privateActivateCommand;
	}
	private void setActivateCommand(UICommand value)
	{
		privateActivateCommand = value;
	}
	private UICommand privateMaintenanceCommand;
	public UICommand getMaintenanceCommand()
	{
		return privateMaintenanceCommand;
	}
	private void setMaintenanceCommand(UICommand value)
	{
		privateMaintenanceCommand = value;
	}



	public storage_domains getEntity()
	{
		return (storage_domains)super.getEntity();
	}
	public void setEntity(storage_domains value)
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

	/**
	 Gets the value indicating whether multiple data centers
	 can be selected to attach storage to.
	*/
	private boolean privateAttachMultiple;
	public boolean getAttachMultiple()
	{
		return privateAttachMultiple;
	}
	private void setAttachMultiple(boolean value)
	{
		privateAttachMultiple = value;
	}


	public StorageDataCenterListModel()
	{
		setTitle("Data Center");

		setAttachCommand(new UICommand("Attach", this));
		setDetachCommand(new UICommand("Detach", this));
		setActivateCommand(new UICommand("Activate", this));
		setMaintenanceCommand(new UICommand("Maintenance", this));

		UpdateActionAvailability();
	}

	@Override
	protected void OnEntityChanged()
	{
		super.OnEntityChanged();

		getSearchCommand().Execute();
		UpdateActionAvailability();
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
		super.SyncSearch();

		AsyncQuery _asyncQuery = new AsyncQuery();
		_asyncQuery.setModel(this);
		_asyncQuery.asyncCallback = new INewAsyncCallback() { public void OnSuccess(Object model, Object ReturnValue)
			{
				SearchableListModel searchableListModel = (SearchableListModel)model;
				searchableListModel.setItems((java.util.ArrayList<storage_domains>)((VdcQueryReturnValue)ReturnValue).getReturnValue());
				setIsEmpty(((java.util.List)searchableListModel.getItems()).size() == 0);
			}};

		Frontend.RunQuery(VdcQueryType.GetStorageDomainListById, new StorageDomainQueryParametersBase(getEntity().getid()), _asyncQuery);
	}

	@Override
	protected void AsyncSearch()
	{
		super.AsyncSearch();

		setAsyncResult(Frontend.RegisterQuery(VdcQueryType.GetStorageDomainListById, new StorageDomainQueryParametersBase(getEntity().getid())));
		setItems(getAsyncResult().getData());
	}

	private void Attach()
	{
		if (getWindow() != null)
		{
			return;
		}

		setAttachMultiple(getEntity().getstorage_domain_type() == StorageDomainType.ISO);

		ListModel model = new ListModel();
		setWindow(model);
		model.setTitle("Attach to Data Center");

		java.util.ArrayList<EntityModel> items = new java.util.ArrayList<EntityModel>();

		for (storage_pool dataCenter : DataProvider.GetDataCenterList())
		{
			boolean add = false;

			switch (getEntity().getstorage_domain_type())
			{
				case Master:
				case Data:
					{
						if ((dataCenter.getstatus() == StoragePoolStatus.Uninitialized || dataCenter.getstatus() == StoragePoolStatus.Up) && (dataCenter.getStoragePoolFormatType() == null || dataCenter.getStoragePoolFormatType() == getEntity().getStorageStaticData().getStorageFormat()) && dataCenter.getstorage_pool_type() == getEntity().getstorage_type())
						{
							add = true;
						}
					}
					break;
				case ISO:
					{
						if (dataCenter.getstatus() == StoragePoolStatus.Up)
						{
							storage_domains isoStorage = DataProvider.GetIsoDomainByDataCenterId(dataCenter.getId());
							if (isoStorage == null)
							{
								add = true;
							}
						}
					}
					break;
				case ImportExport:
					{
						if (dataCenter.getstatus() == StoragePoolStatus.Up)
						{
							storage_domains exportStorage = DataProvider.GetExportDomainByDataCenterId(dataCenter.getId());
							if (exportStorage == null)
							{
								add = true;
							}
						}
					}
					break;
			}

			if (add)
			{
				EntityModel tempVar = new EntityModel();
				tempVar.setEntity(dataCenter);
				items.add(tempVar);
			}
		}

		model.setItems(items);


		if (items.isEmpty())
		{
			model.setMessage("There are No Data Centers to which the Storage Domain can be attached");

			UICommand tempVar2 = new UICommand("Cancel", this);
			tempVar2.setTitle("Close");
			tempVar2.setIsDefault(true);
			tempVar2.setIsCancel(true);
			model.getCommands().add(tempVar2);
		}
		else
		{
			UICommand tempVar3 = new UICommand("OnAttach", this);
			tempVar3.setTitle("OK");
			tempVar3.setIsDefault(true);
			model.getCommands().add(tempVar3);
			UICommand tempVar4 = new UICommand("Cancel", this);
			tempVar4.setTitle("Cancel");
			tempVar4.setIsCancel(true);
			model.getCommands().add(tempVar4);
		}
	}

	private void OnAttach()
	{
		ListModel model = (ListModel)getWindow();

		if (model.getProgress() != null)
		{
			return;
		}

		if (getEntity() == null)
		{
			Cancel();
			return;
		}


		java.util.ArrayList<storage_pool> items = new java.util.ArrayList<storage_pool>();
		for (EntityModel a : Linq.<EntityModel>Cast(model.getItems()))
		{
			if (a.getIsSelected())
			{
				items.add((storage_pool)a.getEntity());
			}
		}


		if (items.size() > 0)
		{
			model.StartProgress(null);

			java.util.ArrayList<VdcActionParametersBase> parameters = new java.util.ArrayList<VdcActionParametersBase>();
			for (storage_pool dataCenter : items)
			{
				parameters.add(new StorageDomainPoolParametersBase(getEntity().getid(), dataCenter.getId()));
			}

			Frontend.RunMultipleAction(VdcActionType.AttachStorageDomainToPool, parameters,
		new IFrontendMultipleActionAsyncCallback() {
			@Override
			public void Executed(FrontendMultipleActionAsyncResult  result) {

				ListModel localModel = (ListModel)result.getState();
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

	private void Detach()
	{
		if (getWindow() != null)
		{
			return;
		}

		ConfirmationModel model = new ConfirmationModel();
		setWindow(model);
		model.setTitle("Detach Storage");
		model.setHashName("detach_storage");
		model.setMessage("Are you sure you want to Detach storage from the following Data Center(s)?");

		java.util.ArrayList<String> items = new java.util.ArrayList<String>();
		for (Object item : getSelectedItems())
		{
			storage_domains a = (storage_domains)item;
			items.add(a.getstorage_pool_name());
		}
		model.setItems(items);

		if (ContainsLocalStorage(model))
		{
			model.getLatch().setIsAvailable(true);
			model.getLatch().setIsChangable(true);

			model.setNote("Note: " + GetLocalStoragesFormattedString() + " will be removed!");
		}

		UICommand tempVar = new UICommand("OnDetach", this);
		tempVar.setTitle("OK");
		tempVar.setIsDefault(true);
		model.getCommands().add(tempVar);
		UICommand tempVar2 = new UICommand("Cancel", this);
		tempVar2.setTitle("Cancel");
		tempVar2.setIsCancel(true);
		model.getCommands().add(tempVar2);
	}

	private String GetLocalStoragesFormattedString()
	{
		String localStorages = "";
		for (storage_domains a : Linq.<storage_domains>Cast(getSelectedItems()))
		{
			if (a.getstorage_type() == StorageType.LOCALFS)
			{
				localStorages += a.getstorage_name() + ", ";
			}
		}
		return localStorages.substring(0, localStorages.length() - 2);
	}

	private boolean ContainsLocalStorage(ConfirmationModel model)
	{
		for (storage_domains a : Linq.<storage_domains>Cast(getSelectedItems()))
		{
			if (a.getstorage_type() == StorageType.LOCALFS)
			{
				return true;
			}
		}
		return false;
	}

	private void OnDetach()
	{
		ConfirmationModel model = (ConfirmationModel)getWindow();

		if (!model.Validate())
		{
			return;
		}

		java.util.ArrayList<VdcActionParametersBase> detachPrms = new java.util.ArrayList<VdcActionParametersBase>();
		java.util.ArrayList<VdcActionParametersBase> removePrms = new java.util.ArrayList<VdcActionParametersBase>();

		for (Object item : getSelectedItems())
		{
			storage_domains a = (storage_domains)item;
			if (a.getstorage_type() != StorageType.LOCALFS)
			{
				DetachStorageDomainFromPoolParameters param = new DetachStorageDomainFromPoolParameters();
				param.setStorageDomainId(getEntity().getid());
				if (a.getstorage_pool_id() != null)
				{
					param.setStoragePoolId(a.getstorage_pool_id().getValue());
				}

				detachPrms.add(param);
			}
			else
			{
				VDS locaVds = DataProvider.GetLocalStorageHost(a.getstorage_pool_name());
				RemoveStorageDomainParameters tempVar = new RemoveStorageDomainParameters(a.getid());
				tempVar.setVdsId((locaVds != null ? locaVds.getvds_id() : null));
				tempVar.setDoFormat(true);
				RemoveStorageDomainParameters removeStorageDomainParameters = tempVar;
				removePrms.add(removeStorageDomainParameters);
			}
		}

		Frontend.RunMultipleAction(VdcActionType.DetachStorageDomainFromPool, detachPrms);
		Frontend.RunMultipleAction(VdcActionType.RemoveStorageDomain, removePrms);

		Cancel();
	}

	private void Maintenance()
	{
		java.util.ArrayList<VdcActionParametersBase> list = new java.util.ArrayList<VdcActionParametersBase>();
		for (Object item : getSelectedItems())
		{
			storage_domains a = (storage_domains)item;

			StorageDomainPoolParametersBase parameters = new StorageDomainPoolParametersBase();
			parameters.setStorageDomainId(getEntity().getid());
			if (a.getstorage_pool_id() != null)
			{
				parameters.setStoragePoolId(a.getstorage_pool_id().getValue());
			}

			list.add(parameters);
		}

		Frontend.RunMultipleAction(VdcActionType.DeactivateStorageDomain, list,
		new IFrontendMultipleActionAsyncCallback() {
			@Override
			public void Executed(FrontendMultipleActionAsyncResult  result) {


			}
		}, null);
	}

	private void Activate()
	{
		java.util.ArrayList<VdcActionParametersBase> list = new java.util.ArrayList<VdcActionParametersBase>();
		for (Object item : getSelectedItems())
		{
			storage_domains a = (storage_domains)item;

			StorageDomainPoolParametersBase parameters = new StorageDomainPoolParametersBase();
			parameters.setStorageDomainId(getEntity().getid());
			if (a.getstorage_pool_id() != null)
			{
				parameters.setStoragePoolId(a.getstorage_pool_id().getValue());
			}

			list.add(parameters);
		}

		Frontend.RunMultipleAction(VdcActionType.ActivateStorageDomain, list,
		new IFrontendMultipleActionAsyncCallback() {
			@Override
			public void Executed(FrontendMultipleActionAsyncResult  result) {


			}
		}, null);
	}

	private void Cancel()
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

	@Override
	protected void SelectedItemPropertyChanged(Object sender, PropertyChangedEventArgs e)
	{
		super.SelectedItemPropertyChanged(sender, e);

		if (e.PropertyName.equals("status"))
		{
			UpdateActionAvailability();
		}
	}

	private void UpdateActionAvailability()
	{
		java.util.ArrayList<storage_domains> items = getSelectedItems() != null ? Linq.<storage_domains>Cast(getSelectedItems()) : new java.util.ArrayList<storage_domains>();

		getActivateCommand().setIsExecutionAllowed(items.size() == 1 && VdcActionUtils.CanExecute(items, storage_domains.class, VdcActionType.ActivateStorageDomain));

		getMaintenanceCommand().setIsExecutionAllowed(items.size() == 1 && VdcActionUtils.CanExecute(items, storage_domains.class, VdcActionType.DeactivateStorageDomain));

		getAttachCommand().setIsExecutionAllowed(getEntity() != null && (getEntity().getstorage_domain_shared_status() == StorageDomainSharedStatus.Unattached || getEntity().getstorage_domain_type() == StorageDomainType.ISO));

		getDetachCommand().setIsExecutionAllowed(items.size() > 0 && VdcActionUtils.CanExecute(items, storage_domains.class, VdcActionType.DetachStorageDomainFromPool));
	}


	@Override
	public void ExecuteCommand(UICommand command)
	{
		super.ExecuteCommand(command);

		if (command == getAttachCommand())
		{
			Attach();
		}
		else if (command == getDetachCommand())
		{
			Detach();
		}
		else if (command == getActivateCommand())
		{
			Activate();
		}
		else if (command == getMaintenanceCommand())
		{
			Maintenance();
		}
		else if (StringHelper.stringsEqual(command.getName(), "OnAttach"))
		{
			OnAttach();
		}
		else if (StringHelper.stringsEqual(command.getName(), "OnDetach"))
		{
			OnDetach();
		}
		else if (StringHelper.stringsEqual(command.getName(), "Cancel"))
		{
			Cancel();
		}
	}
}