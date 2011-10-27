package org.ovirt.engine.ui.uicommon.models.datacenters;
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
import org.ovirt.engine.core.common.businessentities.*;

import org.ovirt.engine.core.common.queries.*;
import org.ovirt.engine.core.common.*;
import org.ovirt.engine.ui.uicommon.*;
import org.ovirt.engine.ui.uicommon.models.*;

@SuppressWarnings("unused")
public class DataCenterStorageListModel extends SearchableListModel
{

	private UICommand privateAttachStorageCommand;
	public UICommand getAttachStorageCommand()
	{
		return privateAttachStorageCommand;
	}
	private void setAttachStorageCommand(UICommand value)
	{
		privateAttachStorageCommand = value;
	}
	private UICommand privateAttachISOCommand;
	public UICommand getAttachISOCommand()
	{
		return privateAttachISOCommand;
	}
	private void setAttachISOCommand(UICommand value)
	{
		privateAttachISOCommand = value;
	}
	private UICommand privateAttachBackupCommand;
	public UICommand getAttachBackupCommand()
	{
		return privateAttachBackupCommand;
	}
	private void setAttachBackupCommand(UICommand value)
	{
		privateAttachBackupCommand = value;
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



	public storage_pool getEntity()
	{
		return (storage_pool)super.getEntity();
	}
	public void setEntity(storage_pool value)
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


	public DataCenterStorageListModel()
	{
		setTitle("Storage");

		setAttachStorageCommand(new UICommand("AttachStorage", this));
		setAttachISOCommand(new UICommand("AttachISO", this));
		setAttachBackupCommand(new UICommand("AttachBackup", this));
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
	}

	@Override
	public void Search()
	{
		if (getEntity() != null)
		{
			// omer - overriding AsyncSearch - using query instead of search
			//SearchString = StringFormat.format("storage: datacenter={0}", Entity.name);
			super.Search();
		}
	}

	@Override
	protected void SyncSearch()
	{
		AsyncQuery _asyncQuery = new AsyncQuery();
		_asyncQuery.setModel(this);
		_asyncQuery.asyncCallback = new INewAsyncCallback() { public void OnSuccess(Object model, Object ReturnValue)
		{
			SearchableListModel searchableListModel = (SearchableListModel)model;
			searchableListModel.setItems((java.util.ArrayList<storage_domains>)((VdcQueryReturnValue)ReturnValue).getReturnValue());
		}};

		Frontend.RunQuery(VdcQueryType.GetStorageDomainsByStoragePoolId, new StoragePoolQueryParametersBase(getEntity().getId()), _asyncQuery);
	}

	@Override
	protected void AsyncSearch()
	{
		super.AsyncSearch();

		setAsyncResult(Frontend.RegisterQuery(VdcQueryType.GetStorageDomainsByStoragePoolId, new StoragePoolQueryParametersBase(getEntity().getId())));
		setItems(getAsyncResult().getData());
	}

	public void Maintenance()
	{
		//Frontend.RunMultipleActions(VdcActionType.DeactivateStorageDomain,
		//    SelectedItems.Cast<storage_domains>()
		//    .Select(a => (VdcActionParametersBase)new StorageDomainPoolParametersBase(a.id, Entity.id))
		//    .ToList()
		//);
		java.util.ArrayList<VdcActionParametersBase> pb = new java.util.ArrayList<VdcActionParametersBase>();
		for (storage_domains a : Linq.<storage_domains>Cast(getSelectedItems()))
		{
			pb.add(new StorageDomainPoolParametersBase(a.getid(), getEntity().getId()));
		}

		Frontend.RunMultipleAction(VdcActionType.DeactivateStorageDomain, pb);
	}

	public void Activate()
	{
		//Frontend.RunMultipleActions(VdcActionType.ActivateStorageDomain,
		//    SelectedItems.Cast<storage_domains>()
		//    .Select(a => (VdcActionParametersBase)new StorageDomainPoolParametersBase(a.id, Entity.id))
		//    .ToList()
		//);
		java.util.ArrayList<VdcActionParametersBase> pb = new java.util.ArrayList<VdcActionParametersBase>();
		for (storage_domains a : Linq.<storage_domains>Cast(getSelectedItems()))
		{
			pb.add(new StorageDomainPoolParametersBase(a.getid(), getEntity().getId()));
		}


		Frontend.RunMultipleAction(VdcActionType.ActivateStorageDomain, pb);
	}

	public void AttachBackup()
	{
		AttachInternal(StorageDomainType.ImportExport, "Attach Export Domain", "attach_export_domain");
	}

	public void AttachISO()
	{
		AttachInternal(StorageDomainType.ISO, "Attach ISO Library", "attach_iso_library");
	}

	public void AttachStorage()
	{
		AttachInternal(StorageDomainType.Data, "Attach Storage", "attach_storage");
	}

	private void AttachInternal(StorageDomainType storageType, String title, String hashName)
	{
		if (getWindow() != null)
		{
			return;
		}

		//var items = Items != null ? Items.Cast<storage_domains>().ToList() : new List<storage_domains>();
		java.util.ArrayList<storage_domains> items = getItems() != null ? new java.util.ArrayList<storage_domains>(Linq.<storage_domains>Cast(getItems())) : new java.util.ArrayList<storage_domains>();

		ListModel model = new ListModel();
		setWindow(model);
		model.setTitle(title);
		model.setHashName(hashName);
		java.util.ArrayList<EntityModel> models;
		if (storageType == StorageDomainType.ISO)
		{
			java.util.ArrayList<storage_domains> list = DataProvider.GetISOStorageDomainList();
			//models = list.Where(a=>items.All(b=>b.id != a.id)).Select(a=>new EntityModel(){Entity = a}).ToList();
			models = new java.util.ArrayList<EntityModel>();
			for (storage_domains a : list)
			{
				//if (Linq.All<storage_domains>(items, delegate(storage_domains b) { return b.id != a.id; }))
				if (!Linq.IsSDItemExistInList(items, a.getid()))
				{
					EntityModel tempVar = new EntityModel();
					tempVar.setEntity(a);
					models.add(tempVar);
				}
			}

		}
		else
		{
			java.util.ArrayList<storage_domains> list = DataProvider.GetStorageDomainList();

			models = new java.util.ArrayList<EntityModel>();
			boolean addToList;
			Version version3_0 = new Version(3,0);
			for (storage_domains a : list)
			{
				addToList = false;

				if(!Linq.IsSDItemExistInList(items, a.getid()) && a.getstorage_domain_type() == storageType)
				{
					if(storageType == StorageDomainType.Data && a.getstorage_type() == getEntity().getstorage_pool_type() && a.getstorage_domain_shared_status() == StorageDomainSharedStatus.Unattached)
					{
						if(getEntity().getStoragePoolFormatType() == null)
						{
							//compat logic: in case its not v1 and the version is less than 3.0 - continue.
							if (a.getStorageStaticData().getStorageFormat() != StorageFormatType.V1 && getEntity().getcompatibility_version().compareTo(version3_0) < 0)
							{
								continue;
							}
							addToList = true;
						}
						else if ((StorageFormatType)getEntity().getStoragePoolFormatType() == a.getStorageStaticData().getStorageFormat())
						{
							addToList = true;
						}
					}
					else if(storageType == StorageDomainType.ImportExport && a.getstorage_domain_shared_status() == StorageDomainSharedStatus.Unattached)
					{
						addToList = true;
					}

					if(addToList)
					{
						EntityModel tempVar2 = new EntityModel();
						tempVar2.setEntity(a);
						models.add(tempVar2);
					}
				}
			}
		}
		model.setItems(models);

		if (models.isEmpty())
		{
			model.setMessage("There are no compatible Storage Domains to attach to this Data Center. Please add new Storage from the Storage tab.");

			UICommand tempVar3 = new UICommand("Cancel", this);
			tempVar3.setTitle("Close");
			tempVar3.setIsDefault(true);
			tempVar3.setIsCancel(true);
			model.getCommands().add(tempVar3);
		}
		else
		{
			UICommand tempVar4 = new UICommand("OnAttach", this);
			tempVar4.setTitle("OK");
			tempVar4.setIsDefault(true);
			model.getCommands().add(tempVar4);
			UICommand tempVar5 = new UICommand("Cancel", this);
			tempVar5.setTitle("Cancel");
			tempVar5.setIsCancel(true);
			model.getCommands().add(tempVar5);
		}
	}

	public void OnAttach()
	{
		ListModel model = (ListModel)getWindow();

		if (getEntity() == null)
		{
			Cancel();
			return;
		}

		//var items = model.Items
		//    .Cast<EntityModel>()
		//    .Where(Selector.GetIsSelected)
		//    .Select(a => (storage_domains)a.Entity)
		//    .ToList();
		java.util.ArrayList<storage_domains> items = new java.util.ArrayList<storage_domains>();
		for (EntityModel a : Linq.<EntityModel>Cast(model.getItems()))
		{
			if (a.getIsSelected())
			{
				items.add((storage_domains)a.getEntity());
			}
		}


		if (items.size() > 0)
		{
			//Frontend.RunMultipleActions(VdcActionType.AttachStorageDomainToPool,
			//    items
			//    .Select(a => (VdcActionParametersBase)new StorageDomainPoolParametersBase(a.id, Entity.id))
			//    .ToList()
			//);
			java.util.ArrayList<VdcActionParametersBase> pb = new java.util.ArrayList<VdcActionParametersBase>();
			for (storage_domains a : items)
			{
				pb.add(new StorageDomainPoolParametersBase(a.getid(), getEntity().getId()));
			}


			Frontend.RunMultipleAction(VdcActionType.AttachStorageDomainToPool, pb);
		}

		Cancel();
	}

	public void Detach()
	{
		if (getWindow() != null)
		{
			return;
		}

		ConfirmationModel model = new ConfirmationModel();
		setWindow(model);
		model.setTitle("Detach Storage");
		model.setHashName("detach_storage");
		model.setMessage("Are you sure you want to Detach the following storage(s)?");

		//model.Items = SelectedItems.Cast<storage_domains>().Select(a => a.storage_name);
		model.setItems(new java.util.ArrayList<String>());

		if (ContainsLocalStorage(model))
		{
			model.getLatch().setIsAvailable(true);
			model.getLatch().setIsChangable(true);

			model.setNote("Note: " + GetLocalStoragesFormattedString() + " will be removed!");
		}

		for (storage_domains a : Linq.<storage_domains>Cast(getSelectedItems()))
		{
			((java.util.ArrayList<String>)model.getItems()).add(a.getstorage_name());
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

	public void OnDetach()
	{
		ConfirmationModel model = (ConfirmationModel)getWindow();

		if (model.getProgress() != null)
		{
			return;
		}

		if (!model.Validate())
		{
			return;
		}

		// A list of 'detach' action parameters
		java.util.ArrayList<VdcActionParametersBase> pb_detach = new java.util.ArrayList<VdcActionParametersBase>();
		// A list of 'remove' action parameters
		java.util.ArrayList<VdcActionParametersBase> pb_remove = new java.util.ArrayList<VdcActionParametersBase>();

		for (storage_domains a : Linq.<storage_domains>Cast(getSelectedItems()))
		{
			// For local storage - remove; otherwise - detach
			if (a.getstorage_type() == StorageType.LOCALFS)
			{
				VDS locaVds = DataProvider.GetLocalStorageHost(a.getstorage_pool_name());
				RemoveStorageDomainParameters tempVar = new RemoveStorageDomainParameters(a.getid());
				tempVar.setVdsId((locaVds != null ? locaVds.getvds_id() : null));
				tempVar.setDoFormat(true);
				pb_remove.add(tempVar);
			}
			else
			{
				pb_detach.add(new DetachStorageDomainFromPoolParameters(a.getid(), getEntity().getId()));
			}
		}


		model.StartProgress(null);

		Frontend.RunMultipleAction(VdcActionType.RemoveStorageDomain, pb_remove,
		new IFrontendMultipleActionAsyncCallback() {
			@Override
			public void Executed(FrontendMultipleActionAsyncResult  result1) {

			Object[] array = (Object[])result1.getState();
			ConfirmationModel localModel1 = (ConfirmationModel)array[0];
			java.util.ArrayList<VdcActionParametersBase> parameters = (java.util.ArrayList<VdcActionParametersBase>)array[1];
			Frontend.RunMultipleAction(VdcActionType.DetachStorageDomainFromPool, parameters,
		new IFrontendMultipleActionAsyncCallback() {
			@Override
			public void Executed(FrontendMultipleActionAsyncResult  result2) {

				ConfirmationModel localModel2 = (ConfirmationModel)result2.getState();
				localModel2.StopProgress();
				Cancel();

			}
		}, localModel1);

			}
		}, new Object[] { model, pb_detach });
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

	@Override
	protected void ItemsCollectionChanged(Object sender, NotifyCollectionChangedEventArgs e)
	{
		super.ItemsCollectionChanged(sender, e);
		UpdateActionAvailability();
	}

	@Override
	protected void ItemsChanged()
	{
		super.ItemsChanged();
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

	@Override
	protected void ItemPropertyChanged(Object sender, PropertyChangedEventArgs e)
	{
		super.ItemPropertyChanged(sender, e);

		if (e.PropertyName.equals("status"))
		{
			UpdateActionAvailability();
		}
	}

	@Override
	protected boolean getNotifyPropertyChangeForAnyItem()
	{
		return true;
	}

	private void UpdateActionAvailability()
	{
		java.util.ArrayList<storage_domains> items = getItems() != null ? Linq.<storage_domains>Cast(getItems()) : new java.util.ArrayList<storage_domains>();
		java.util.ArrayList<storage_domains> selectedItems = getSelectedItems() != null ? Linq.<storage_domains>Cast(getSelectedItems()) : new java.util.ArrayList<storage_domains>();

		if (getEntity() != null)
		{
			getAttachStorageCommand().setIsExecutionAllowed(getEntity().getstorage_pool_type() != StorageType.LOCALFS);
		}

		boolean isMasterPresents = false;
		for (storage_domains a : items)
		{
			if (a.getstorage_domain_type() == StorageDomainType.Master && a.getstatus() != null && a.getstatus() == StorageDomainStatus.Active)
			{
				isMasterPresents = true;
				break;
			}
		}

		boolean isISOPresents = false;
		for (storage_domains a : items)
		{
			if (a.getstorage_domain_type() == StorageDomainType.ISO)
			{
				isISOPresents = true;
				break;
			}
		}
		getAttachISOCommand().setIsExecutionAllowed(false);
		getAttachISOCommand().setIsExecutionAllowed(items.size() > 0 && isMasterPresents && !isISOPresents);


		boolean isBackupPresents = false;
		for (storage_domains a : items)
		{
			if (a.getstorage_domain_type() == StorageDomainType.ImportExport)
			{
				isBackupPresents = true;
				break;
			}
		}
		getAttachBackupCommand().setIsExecutionAllowed(items.size() > 0 && isMasterPresents && !isBackupPresents);


		getDetachCommand().setIsExecutionAllowed(selectedItems.size() > 0 && VdcActionUtils.CanExecute(selectedItems, storage_domains.class, VdcActionType.DetachStorageDomainFromPool));

		getActivateCommand().setIsExecutionAllowed(selectedItems.size() == 1 && VdcActionUtils.CanExecute(selectedItems, storage_domains.class, VdcActionType.ActivateStorageDomain));

		getMaintenanceCommand().setIsExecutionAllowed(selectedItems.size() == 1 && VdcActionUtils.CanExecute(selectedItems, storage_domains.class, VdcActionType.DeactivateStorageDomain));
	}

	@Override
	public void ExecuteCommand(UICommand command)
	{
		super.ExecuteCommand(command);

		if (command == getAttachStorageCommand())
		{
			AttachStorage();
		}
		else if (command == getAttachISOCommand())
		{
			AttachISO();
		}
		else if (command == getAttachBackupCommand())
		{
			AttachBackup();
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