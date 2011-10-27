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

import org.ovirt.engine.ui.uicommon.models.common.*;
import org.ovirt.engine.ui.uicommon.models.configure.*;
import org.ovirt.engine.ui.uicompat.*;
import org.ovirt.engine.core.common.*;
import org.ovirt.engine.core.common.interfaces.*;
import org.ovirt.engine.core.common.businessentities.*;

import org.ovirt.engine.core.common.queries.*;
import org.ovirt.engine.ui.uicommon.*;
import org.ovirt.engine.ui.uicommon.models.*;

@SuppressWarnings("unused")
public class StorageListModel extends ListWithDetailsModel implements ITaskTarget, ISupportSystemTreeContext
{

	private UICommand privateNewDomainCommand;
	public UICommand getNewDomainCommand()
	{
		return privateNewDomainCommand;
	}
	private void setNewDomainCommand(UICommand value)
	{
		privateNewDomainCommand = value;
	}
	private UICommand privateImportDomainCommand;
	public UICommand getImportDomainCommand()
	{
		return privateImportDomainCommand;
	}
	private void setImportDomainCommand(UICommand value)
	{
		privateImportDomainCommand = value;
	}
	private UICommand privateEditCommand;
	public UICommand getEditCommand()
	{
		return privateEditCommand;
	}
	private void setEditCommand(UICommand value)
	{
		privateEditCommand = value;
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
	private UICommand privateDestroyCommand;
	public UICommand getDestroyCommand()
	{
		return privateDestroyCommand;
	}
	private void setDestroyCommand(UICommand value)
	{
		privateDestroyCommand = value;
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

		//			get { return SelectedItems == null ? new object[0] : SelectedItems.Cast<storage_domains>().Select(a => a.id).Cast<object>().ToArray(); }
	protected Object[] getSelectedKeys()
	{
		if (getSelectedItems() == null)
		{
			return new Object[0];
		}
		else
		{
			java.util.ArrayList<Object> items = new java.util.ArrayList<Object>();
			for (Object item : getSelectedItems())
			{
				storage_domains i = (storage_domains)item;
				items.add(i.getid());
			}
			return items.toArray(new Object[]{});
		}
	}



	public StorageListModel()
	{
		setTitle("Storage");

		setDefaultSearchString("Storage:");
		setSearchString(getDefaultSearchString());

		setNewDomainCommand(new UICommand("NewDomain", this));
		setImportDomainCommand(new UICommand("ImportDomain", this));
		setEditCommand(new UICommand("Edit", this));
		setRemoveCommand(new UICommand("Remove", this));
		setDestroyCommand(new UICommand("Destroy", this));

		UpdateActionAvailability();

		getSearchNextPageCommand().setIsAvailable(true);
		getSearchPreviousPageCommand().setIsAvailable(true);
	}

	private EntityModel vmBackupModel;
	private EntityModel templateBackupModel;
	private ListModel vmListModel;
	private ListModel templateListModel;
	private ListModel isoListModel;

	@Override
	protected void InitDetailModels()
	{
		super.InitDetailModels();


		vmBackupModel = new VmBackupModel();
		vmBackupModel.setIsAvailable(false);

		templateBackupModel = new TemplateBackupModel();
		templateBackupModel.setIsAvailable(false);

		vmListModel = new StorageVmListModel();
		vmListModel.setIsAvailable(false);

		templateListModel = new StorageTemplateListModel();
		templateListModel.setIsAvailable(false);

		isoListModel = new StorageIsoListModel();
		isoListModel.setIsAvailable(false);

		ObservableCollection<EntityModel> list = new ObservableCollection<EntityModel>();
		list.add(new StorageGeneralModel());
		list.add(new StorageDataCenterListModel());
		list.add(vmBackupModel);
		list.add(templateBackupModel);
		list.add(vmListModel);
		list.add(templateListModel);
		list.add(isoListModel);
		list.add(new StorageEventListModel());
		list.add(new PermissionListModel());
		setDetailModels(list);
	}

	@Override
	public boolean IsSearchStringMatch(String searchString)
	{
		return searchString.trim().toLowerCase().startsWith("storage");
	}

	@Override
	protected void SyncSearch()
	{
		SearchParameters tempVar = new SearchParameters(getSearchString(), SearchType.StorageDomain);
		tempVar.setMaxCount(getSearchPageSize());
		super.SyncSearch(VdcQueryType.Search, tempVar);
	}

	@Override
	protected void AsyncSearch()
	{
		super.AsyncSearch();

		setAsyncResult(Frontend.RegisterSearch(getSearchString(), SearchType.StorageDomain, getSearchPageSize()));
		setItems(getAsyncResult().getData());
	}

	private void NewDomain()
	{
		if (getWindow() != null)
		{
			return;
		}

		StorageModel model = new StorageModel(new NewEditStorageModelBehavior());
		setWindow(model);
		model.setTitle("New Domain");
		model.setHashName("new_domain");
		model.setSystemTreeSelectedItem(getSystemTreeSelectedItem());

		java.util.ArrayList<IStorageModel> items = new java.util.ArrayList<IStorageModel>();
		// putting all Data domains at the beginning on purpose (so when choosing the
		// first selectable storage type/function, it will be a Data one, if relevant).
		NfsStorageModel tempVar = new NfsStorageModel();
		tempVar.setRole(StorageDomainType.Data);
		items.add(tempVar);
		IscsiStorageModel tempVar2 = new IscsiStorageModel();
		tempVar2.setRole(StorageDomainType.Data);
		tempVar2.setIsGrouppedByTarget(true);
		items.add(tempVar2);
		FcpStorageModel tempVar3 = new FcpStorageModel();
		tempVar3.setRole(StorageDomainType.Data);
		items.add(tempVar3);
		LocalStorageModel tempVar4 = new LocalStorageModel();
		tempVar4.setRole(StorageDomainType.Data);
		items.add(tempVar4);

		NfsStorageModel tempVar5 = new NfsStorageModel();
		tempVar5.setRole(StorageDomainType.ISO);
		items.add(tempVar5);

		NfsStorageModel tempVar6 = new NfsStorageModel();
		tempVar6.setRole(StorageDomainType.ImportExport);
		items.add(tempVar6);
		IscsiStorageModel tempVar7 = new IscsiStorageModel();
		tempVar7.setRole(StorageDomainType.ImportExport);
		tempVar7.setIsGrouppedByTarget(true);
		items.add(tempVar7);
		FcpStorageModel tempVar8 = new FcpStorageModel();
		tempVar8.setRole(StorageDomainType.ImportExport);
		items.add(tempVar8);

		model.setItems(items);

		model.Initialize();


		UICommand tempVar9 = new UICommand("OnSave", this);
		tempVar9.setTitle("OK");
		tempVar9.setIsDefault(true);
		model.getCommands().add(tempVar9);
		UICommand tempVar10 = new UICommand("Cancel", this);
		tempVar10.setTitle("Cancel");
		tempVar10.setIsCancel(true);
		model.getCommands().add(tempVar10);
	}

	private void Edit()
	{
		storage_domains storage = (storage_domains)getSelectedItem();

		if (getWindow() != null)
		{
			return;
		}

		StorageModel model = new StorageModel(new NewEditStorageModelBehavior());
		setWindow(model);
		model.setTitle("Edit Domain");
		model.setHashName("edit_domain");
		model.setSystemTreeSelectedItem(getSystemTreeSelectedItem());
		model.setStorage(storage);
		model.getName().setEntity(storage.getstorage_name());
		model.setOriginalName(storage.getstorage_name());

		model.getDataCenter().setIsAvailable(false);
		model.getFormat().setIsAvailable(false);


		IStorageModel item = null;
		switch (storage.getstorage_type())
		{
			case NFS:
				item = PrepareNfsStorageForEdit(storage);
				break;

			case FCP:
				item = PrepareFcpStorageForEdit(storage);
				break;

			case ISCSI:
				item = PrepareIscsiStorageForEdit(storage);
				break;

			case LOCALFS:
				item = PrepareLocalStorageForEdit(storage);
				break;
		}

		model.setItems(new java.util.ArrayList<IStorageModel>(java.util.Arrays.asList(new IStorageModel[] { item })));
		model.setSelectedItem(item);

		model.Initialize();


		if (getSystemTreeSelectedItem() != null && getSystemTreeSelectedItem().getType() != SystemTreeItemType.System)
		{
			switch (getSystemTreeSelectedItem().getType())
			{
				case Storage:
					{
						model.getName().setIsChangable(false);
						model.getName().setInfo("Cannot edit Storage Domains's Name in this tree context");
					}
					break;
			}
		}




		UICommand tempVar = new UICommand("OnSave", this);
		tempVar.setTitle("OK");
		tempVar.setIsDefault(true);
		model.getCommands().add(tempVar);
		UICommand tempVar2 = new UICommand("Cancel", this);
		tempVar2.setTitle("Cancel");
		tempVar2.setIsCancel(true);
		model.getCommands().add(tempVar2);
	}

	private IStorageModel PrepareNfsStorageForEdit(storage_domains storage)
	{
		NfsStorageModel model = new NfsStorageModel();
		model.setRole(storage.getstorage_domain_type());

		storage_server_connections connection = DataProvider.GetStorageConnectionById(storage.getstorage());
		model.getPath().setEntity(connection.getconnection());
		model.getPath().setIsAvailable(false);

		return model;
	}

	private IStorageModel PrepareLocalStorageForEdit(storage_domains storage)
	{
		LocalStorageModel model = new LocalStorageModel();
		model.setRole(storage.getstorage_domain_type());

		storage_server_connections connection = DataProvider.GetStorageConnectionById(storage.getstorage());
		model.getPath().setEntity(connection.getconnection());
		model.getPath().setIsAvailable(false);

		return model;
	}

	private IStorageModel PrepareIscsiStorageForEdit(storage_domains storage)
	{
		IscsiStorageModel model = new IscsiStorageModel();
		model.setRole(storage.getstorage_domain_type());

		PrepareSanStorageForEdit(model);

		return model;
	}

	private IStorageModel PrepareFcpStorageForEdit(storage_domains storage)
	{
		FcpStorageModel model = new FcpStorageModel();
		model.setRole(storage.getstorage_domain_type());

		PrepareSanStorageForEdit(model);

		return model;
	}

	private void PrepareSanStorageForEdit(SanStorageModel model)
	{
		storage_domains storage = (storage_domains)getSelectedItem();

		GetLunsByVgIdParameters tempVar = new GetLunsByVgIdParameters();
		tempVar.setVgId(storage.getstorage());
		VdcQueryReturnValue returnValue = Frontend.RunQuery(VdcQueryType.GetLunsByVgId, tempVar);

		if (returnValue != null && returnValue.getSucceeded())
		{
			model.ApplyData((java.util.ArrayList<LUNs>)returnValue.getReturnValue(), true);
		}
	}

	private void ImportDomain()
	{
		if (getWindow() != null)
		{
			return;
		}

		StorageModel model = new StorageModel(new ImportStorageModelBehavior());
		setWindow(model);
		model.setTitle("Import Pre-Configured Domain");
		model.setHashName("import_pre-configured_domain");
		model.setSystemTreeSelectedItem(getSystemTreeSelectedItem());
		model.getName().setIsAvailable(false);
		model.getFormat().setIsAvailable(false);

		java.util.ArrayList<IStorageModel> items = new java.util.ArrayList<IStorageModel>();
		NfsStorageModel tempVar = new NfsStorageModel();
		tempVar.setRole(StorageDomainType.ISO);
		items.add(tempVar);
		NfsStorageModel tempVar2 = new NfsStorageModel();
		tempVar2.setRole(StorageDomainType.ImportExport);
		items.add(tempVar2);

		IscsiImportStorageModel tempVar3 = new IscsiImportStorageModel();
		tempVar3.setRole(StorageDomainType.ImportExport);
		items.add(tempVar3);

		FcpImportStorageModel tempVar4 = new FcpImportStorageModel();
		tempVar4.setRole(StorageDomainType.ImportExport);
		items.add(tempVar4);

		model.setItems(items);

		model.Initialize();


		UICommand tempVar5 = new UICommand("OnImport", this);
		tempVar5.setTitle("OK");
		tempVar5.setIsDefault(true);
		model.getCommands().add(tempVar5);
		UICommand tempVar6 = new UICommand("Cancel", this);
		tempVar6.setTitle("Cancel");
		tempVar6.setIsCancel(true);
		model.getCommands().add(tempVar6);
	}

	private void OnImport()
	{
		StorageModel model = (StorageModel)getWindow();

		if (model.getProgress() != null)
		{
			return;
		}

		if (!model.Validate())
		{
			return;
		}

		model.StartProgress("Importing Storage Domain...");

		VDS host = (VDS)model.getHost().getSelectedItem();

		//Save changes.
		if (model.getSelectedItem() instanceof NfsStorageModel)
		{
			NfsStorageModel nfsModel = (NfsStorageModel)model.getSelectedItem();
			nfsModel.setMessage(null);

			Task.Create(this, new java.util.ArrayList<Object>(java.util.Arrays.asList(new Object[] { "ImportNfs", host.getvds_id(), nfsModel.getPath().getEntity(), nfsModel.getRole() }))).Run();
		}
		else
		{
			Task.Create(this, new java.util.ArrayList<Object>(java.util.Arrays.asList(new Object[] { "ImportSan", host.getvds_id() }))).Run();
		}
	}

	private storage_server_connections nfsConnection;

	private void CleanNfsConnection(Guid hostId)
	{
		if (nfsConnection != null)
		{
			VdcReturnValueBase ret1 = Frontend.RunAction(VdcActionType.RemoveStorageServerConnection, new StorageServerConnectionParametersBase(nfsConnection, hostId));

			if (ret1 != null && ret1.getSucceeded())
			{
				nfsConnection = null;
			}
		}
	}

	private void remove()
	{
		storage_domains storage = (storage_domains)getSelectedItem();

		if (getWindow() != null)
		{
			return;
		}

		RemoveStorageModel model = new RemoveStorageModel();
		setWindow(model);
		model.setTitle("Remove Storage(s)");
		model.setHashName("remove_storage");

		java.util.ArrayList<VDS> hosts = DataProvider.GetUpHostList();

		model.getHostList().setItems(hosts);
		model.getHostList().setSelectedItem(Linq.FirstOrDefault(hosts));

		model.getFormat().setIsAvailable(storage.getstorage_domain_type() == StorageDomainType.ISO || storage.getstorage_domain_type() == StorageDomainType.ImportExport);


		if (hosts.isEmpty())
		{
			UICommand tempVar = new UICommand("Cancel", this);
			tempVar.setTitle("Close");
			tempVar.setIsDefault(true);
			tempVar.setIsCancel(true);
			model.getCommands().add(tempVar);
		}
		else
		{
			UICommand tempVar2 = new UICommand("OnRemove", this);
			tempVar2.setTitle("OK");
			tempVar2.setIsDefault(true);
			model.getCommands().add(tempVar2);
			UICommand tempVar3 = new UICommand("Cancel", this);
			tempVar3.setTitle("Cancel");
			tempVar3.setIsCancel(true);
			model.getCommands().add(tempVar3);
		}
	}

	private void OnRemove()
	{
		if (getSelectedItem() != null)
		{
			storage_domains storage = (storage_domains)getSelectedItem();
			RemoveStorageModel model = (RemoveStorageModel)getWindow();

			if (!model.Validate())
			{
				return;
			}

			VDS host = (VDS)model.getHostList().getSelectedItem();

			RemoveStorageDomainParameters tempVar = new RemoveStorageDomainParameters(storage.getid());
			tempVar.setVdsId(host.getvds_id());
			tempVar.setDoFormat((storage.getstorage_domain_type() == StorageDomainType.Data || storage.getstorage_domain_type() == StorageDomainType.Master) ? true : (Boolean)model.getFormat().getEntity());
			Frontend.RunActionAsyncroniousely(VdcActionType.RemoveStorageDomain, tempVar);
		}

		Cancel();
	}

	private void Destroy()
	{
		ConfirmationModel model = new ConfirmationModel();
		setWindow(model);
		model.setTitle("Destroy Storage Domain");
		model.setHashName("destroy_storage_domain");
		java.util.ArrayList<String> items = new java.util.ArrayList<String>();
		items.add(((storage_domains)getSelectedItem()).getstorage_name());
		model.setItems(items);

		model.getLatch().setIsAvailable(true);
		model.getLatch().setIsChangable(true);


		UICommand tempVar = new UICommand("OnDestroy", this);
		tempVar.setTitle("OK");
		tempVar.setIsDefault(true);
		model.getCommands().add(tempVar);
		UICommand tempVar2 = new UICommand("Cancel", this);
		tempVar2.setTitle("Cancel");
		tempVar2.setIsCancel(true);
		model.getCommands().add(tempVar2);
	}

	private void OnDestroy()
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

		storage_domains storageDomain = (storage_domains)getSelectedItem();


		model.StartProgress(null);

		Frontend.RunMultipleAction(VdcActionType.ForceRemoveStorageDomain, new java.util.ArrayList<VdcActionParametersBase>(java.util.Arrays.asList(new VdcActionParametersBase[] { new StorageDomainParametersBase(storageDomain.getid()) })),
		new IFrontendMultipleActionAsyncCallback() {
			@Override
			public void Executed(FrontendMultipleActionAsyncResult  result) {

			ConfirmationModel localModel = (ConfirmationModel)result.getState();
			localModel.StopProgress();
			Cancel();

			}
		}, model);
	}

	private void OnSave()
	{
		StorageModel model = (StorageModel)getWindow();

		if (!model.Validate())
		{
			return;
		}


		if (model.getSelectedItem() instanceof NfsStorageModel)
		{
			SaveNfsStorage();
		}
		else if (model.getSelectedItem() instanceof LocalStorageModel)
		{
			SaveLocalStorage();
		}
		else
		{
			SaveSanStorage();
		}
	}

	private void SaveLocalStorage()
	{
		if (getWindow().getProgress() != null)
		{
			return;
		}

		getWindow().StartProgress(null);

		Task.Create(this, new java.util.ArrayList<Object>(java.util.Arrays.asList(new Object[] { "SaveLocal" }))).Run();
	}

	private void SaveNfsStorage()
	{
		if (getWindow().getProgress() != null)
		{
			return;
		}

		getWindow().StartProgress(null);

		Task.Create(this, new java.util.ArrayList<Object>(java.util.Arrays.asList(new Object[] { "SaveNfs" }))).Run();
	}

	private void SaveSanStorage()
	{
		if (getWindow().getProgress() != null)
		{
			return;
		}

		getWindow().StartProgress(null);

		Task.Create(this, new java.util.ArrayList<Object>(java.util.Arrays.asList(new Object[] { "SaveSan" }))).Run();
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
	protected void ItemsCollectionChanged(Object sender, NotifyCollectionChangedEventArgs e)
	{
		super.ItemsCollectionChanged(sender, e);

		//Try to select an item corresponding to the system tree selection.
		if (getSystemTreeSelectedItem() != null && getSystemTreeSelectedItem().getType() == SystemTreeItemType.Storage)
		{
			storage_domains storage = (storage_domains)getSystemTreeSelectedItem().getEntity();

			setSelectedItem(Linq.FirstOrDefault(Linq.<storage_domains>Cast(getItems()), new Linq.StoragePredicate(storage.getid())));
		}
	}

	@Override
	protected void UpdateDetailsAvailability()
	{
		if (getSelectedItem() != null)
		{
			storage_domains storage = (storage_domains)getSelectedItem();
			boolean isBackupStorage = storage.getstorage_domain_type() == StorageDomainType.ImportExport;
			boolean isDataStorage = storage.getstorage_domain_type() == StorageDomainType.Data || storage.getstorage_domain_type() == StorageDomainType.Master;
			boolean isIsoStorage = storage.getstorage_domain_type() == StorageDomainType.ISO;

			vmBackupModel.setIsAvailable(isBackupStorage);
			templateBackupModel.setIsAvailable(isBackupStorage);

			vmListModel.setIsAvailable(isDataStorage);
			templateListModel.setIsAvailable(isDataStorage);

			isoListModel.setIsAvailable(isIsoStorage);
		}
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

		if (e.PropertyName.equals("storage_domain_shared_status"))
		{
			UpdateActionAvailability();
		}
	}

	private void UpdateActionAvailability()
	{
		java.util.ArrayList<storage_domains> items = getSelectedItems() != null ? Linq.<storage_domains>Cast(getSelectedItems()) : new java.util.ArrayList<storage_domains>();

		storage_domains item = (storage_domains)getSelectedItem();

		getNewDomainCommand().setIsAvailable(true);

		getEditCommand().setIsExecutionAllowed(item != null && items.size() == 1 && (item.getstorage_domain_shared_status() == StorageDomainSharedStatus.Active || item.getstorage_domain_shared_status() == StorageDomainSharedStatus.Mixed));

		getRemoveCommand().setIsExecutionAllowed(items.size() == 1 && Linq.FindAllStorageDomainsBySharedStatus(items, StorageDomainSharedStatus.Unattached).size() == items.size());

		getDestroyCommand().setIsExecutionAllowed(item != null && items.size() == 1);


		//System tree dependent actions.
		boolean isAvailable = !(getSystemTreeSelectedItem() != null && getSystemTreeSelectedItem().getType() == SystemTreeItemType.Storage);

		getNewDomainCommand().setIsAvailable(isAvailable);
		getRemoveCommand().setIsAvailable(isAvailable);
	}

	@Override
	public void ExecuteCommand(UICommand command)
	{
		super.ExecuteCommand(command);

		if (command == getNewDomainCommand())
		{
			NewDomain();
		}
		else if (command == getImportDomainCommand())
		{
			ImportDomain();
		}
		else if (command == getEditCommand())
		{
			Edit();
		}
		else if (command == getRemoveCommand())
		{
			remove();
		}
		else if (command == getDestroyCommand())
		{
			Destroy();
		}
		else if (StringHelper.stringsEqual(command.getName(), "OnSave"))
		{
			OnSave();
		}
		else if (StringHelper.stringsEqual(command.getName(), "Cancel"))
		{
			Cancel();
		}
		else if (StringHelper.stringsEqual(command.getName(), "OnImport"))
		{
			OnImport();
		}
		else if (StringHelper.stringsEqual(command.getName(), "OnRemove"))
		{
			OnRemove();
		}
		else if (StringHelper.stringsEqual(command.getName(), "OnDestroy"))
		{
			OnDestroy();
		}
	}

	private void SaveNfsStorage(TaskContext context)
	{
		storage_domains selectedItem = (storage_domains)getSelectedItem();
		StorageModel model = (StorageModel)getWindow();
		NfsStorageModel nfsModel = (NfsStorageModel)model.getSelectedItem();
		VDS host = (VDS)model.getHost().getSelectedItem();
		boolean isNew = model.getStorage() == null;


		String path = (String)nfsModel.getPath().getEntity();
		String storageName = null;
		RefObject<String> tempRef_storageName = new RefObject<String>(storageName);
		boolean tempVar = isNew && DataProvider.IsDomainAlreadyExist(path, tempRef_storageName);
			storageName = tempRef_storageName.argvalue;
		if (tempVar)
		{
			context.InvokeUIThread(this, new java.util.ArrayList<Object>(java.util.Arrays.asList(new Object[] { "Finish", false, nfsModel, "Create Operation failed. Domain " + storageName + " already exists in the system." })));

			return;
		}


		//Create storage connection.
		storage_server_connections tempVar2 = new storage_server_connections();
		tempVar2.setconnection(path);
		tempVar2.setstorage_type(nfsModel.getType());
		storage_server_connections connection = tempVar2;

		storage_domain_static storageDomain = isNew ? new storage_domain_static() : (storage_domain_static)Cloner.clone(selectedItem.getStorageStaticData());

		storageDomain.setstorage_type(isNew ? nfsModel.getType() : storageDomain.getstorage_type());

		storageDomain.setstorage_domain_type(isNew ? nfsModel.getRole() : storageDomain.getstorage_domain_type());

		storageDomain.setstorage_name((String)model.getName().getEntity());
		storageDomain.setStorageFormat((StorageFormatType)model.getFormat().getSelectedItem());


		VdcReturnValueBase returnValue;

		if (isNew)
		{
			boolean attach = false;

			returnValue = Frontend.RunAction(VdcActionType.AddStorageServerConnection, new StorageServerConnectionParametersBase(connection, host.getvds_id()));

			if (returnValue != null && returnValue.getSucceeded())
			{
				storageDomain.setstorage((String)returnValue.getActionReturnValue());

				//Add storage domain.
				StorageDomainManagementParameter tempVar3 = new StorageDomainManagementParameter(storageDomain);
				tempVar3.setVdsId(host.getvds_id());
				returnValue = Frontend.RunAction(VdcActionType.AddNFSStorageDomain, tempVar3);

				attach = returnValue != null && returnValue.getSucceeded();
			}

			//Clean up connection.
			Frontend.RunAction(VdcActionType.RemoveStorageServerConnection, new StorageServerConnectionParametersBase(connection, host.getvds_id()));

			if (attach)
			{
				//Attach storage to data center as neccessary.
				storage_pool dataCenter = (storage_pool)model.getDataCenter().getSelectedItem();
				if (!dataCenter.getId().equals(StorageModel.UnassignedDataCenterId))
				{
					NGuid storageId = (NGuid)returnValue.getActionReturnValue();
					AttachStorageToDataCenter((Guid)storageId, dataCenter.getId());
				}
			}
		}
		else
		{
			returnValue = Frontend.RunAction(VdcActionType.UpdateStorageDomain, new StorageDomainManagementParameter(storageDomain));
		}


		context.InvokeUIThread(this, new java.util.ArrayList<Object>(java.util.Arrays.asList(new Object[] { "Finish", returnValue != null && returnValue.getSucceeded(), nfsModel, null })));
	}

	private void SaveLocalStorage(TaskContext context)
	{
		storage_domains selectedItem = (storage_domains)getSelectedItem();
		StorageModel model = (StorageModel)getWindow();
		LocalStorageModel localModel = (LocalStorageModel)model.getSelectedItem();
		VDS host = (VDS)model.getHost().getSelectedItem();
		boolean isNew = model.getStorage() == null;


		String path = (String)localModel.getPath().getEntity();
		String storageName = null;
		RefObject<String> tempRef_storageName = new RefObject<String>(storageName);
		boolean tempVar = isNew && DataProvider.IsDomainAlreadyExist(host.getstorage_pool_id(), path, tempRef_storageName);
			storageName = tempRef_storageName.argvalue;
		if (tempVar)
		{
			context.InvokeUIThread(this, new java.util.ArrayList<Object>(java.util.Arrays.asList(new Object[] { "Finish", false, localModel, "Create Operation failed. Domain " + storageName + " already exists in the system." })));

			return;
		}


		//Create storage connection.
		storage_server_connections tempVar2 = new storage_server_connections();
		tempVar2.setconnection(path);
		tempVar2.setstorage_type(localModel.getType());
		storage_server_connections connection = tempVar2;

		storage_domain_static storageDomain = isNew ? new storage_domain_static() : (storage_domain_static)Cloner.clone(selectedItem.getStorageStaticData());

		storageDomain.setstorage_type(isNew ? localModel.getType() : storageDomain.getstorage_type());

		storageDomain.setstorage_domain_type(isNew ? localModel.getRole() : storageDomain.getstorage_domain_type());

		storageDomain.setstorage_name((String)model.getName().getEntity());


		VdcReturnValueBase returnValue;
		boolean removeConnection = false;

		if (isNew)
		{
			returnValue = Frontend.RunAction(VdcActionType.AddStorageServerConnection, new StorageServerConnectionParametersBase(connection, host.getvds_id()));

			if (returnValue != null && returnValue.getSucceeded())
			{
				storageDomain.setstorage((String)returnValue.getActionReturnValue());

				//Add storage domain.
				StorageDomainManagementParameter tempVar3 = new StorageDomainManagementParameter(storageDomain);
				tempVar3.setVdsId(host.getvds_id());
				returnValue = Frontend.RunAction(VdcActionType.AddLocalStorageDomain, tempVar3);

				if (returnValue == null || !returnValue.getSucceeded())
				{
					removeConnection = true;
				}
			}

			//Clean up connection in case of storage creation failure.
			if (removeConnection)
			{
				Frontend.RunAction(VdcActionType.RemoveStorageServerConnection, new StorageServerConnectionParametersBase(connection, host.getvds_id()));
			}
		}
		else
		{
			returnValue = Frontend.RunAction(VdcActionType.UpdateStorageDomain, new StorageDomainManagementParameter(storageDomain));
		}


		context.InvokeUIThread(this, new java.util.ArrayList<Object>(java.util.Arrays.asList(new Object[] { "Finish", returnValue != null && returnValue.getSucceeded(), localModel, null })));
	}

	private void SaveSanStorage(TaskContext context)
	{
		StorageModel model = (StorageModel)getWindow();
		SanStorageModel sanModel = (SanStorageModel)model.getSelectedItem();
		storage_domains storage = (storage_domains)getSelectedItem();
		boolean isNew = model.getStorage() == null;

		VDS host = (VDS)model.getHost().getSelectedItem();


		java.util.ArrayList<String> lunIds = new java.util.ArrayList<String>();
		for (LunModel lun : sanModel.getAddedLuns())
		{
			lunIds.add(lun.getLunId());
		}


		storage_domain_static storageDomain = isNew ? new storage_domain_static() : (storage_domain_static)Cloner.clone(storage.getStorageStaticData());

		storageDomain.setstorage_type(isNew ? sanModel.getType() : storageDomain.getstorage_type());

		storageDomain.setstorage_domain_type(isNew ? sanModel.getRole() : storageDomain.getstorage_domain_type());

		storageDomain.setStorageFormat(isNew ? (StorageFormatType)sanModel.getContainer().getFormat().getSelectedItem() : storageDomain.getStorageFormat());

		storageDomain.setstorage_name((String)model.getName().getEntity());

		VdcReturnValueBase returnValue;

		if (isNew)
		{
			AddSANStorageDomainParameters tempVar = new AddSANStorageDomainParameters(storageDomain);
			tempVar.setVdsId(host.getvds_id());
			tempVar.setLunIds(lunIds);
			returnValue = Frontend.RunAction(VdcActionType.AddSANStorageDomain, tempVar);

			if (returnValue != null && returnValue.getSucceeded())
			{
				//Attach storage to data center as neccessary.
				storage_pool dataCenter = (storage_pool)model.getDataCenter().getSelectedItem();
				if (!dataCenter.getId().equals(StorageModel.UnassignedDataCenterId))
				{
					NGuid storageId = (NGuid)returnValue.getActionReturnValue();
					AttachStorageToDataCenter((Guid)storageId, dataCenter.getId());
				}
			}
		}
		else
		{
			returnValue = Frontend.RunAction(VdcActionType.UpdateStorageDomain, new StorageDomainManagementParameter(storageDomain));


			//Extend storage as neccessary.
			if (returnValue != null && returnValue.getSucceeded() && lunIds.size() > 0)
			{
				returnValue = Frontend.RunAction(VdcActionType.ExtendSANStorageDomain, new ExtendSANStorageDomainParameters(storage.getid(), lunIds));
			}
		}


		context.InvokeUIThread(this, new java.util.ArrayList<Object>(java.util.Arrays.asList(new Object[] { "Finish", returnValue != null && returnValue.getSucceeded(), sanModel, null })));
	}

	private void AttachStorageToDataCenter(Guid storageId, Guid dataCenterId)
	{
		Frontend.RunActionAsyncroniousely(VdcActionType.AttachStorageDomainToPool, new StorageDomainPoolParametersBase(storageId, dataCenterId));
	}

	private void ImportNfsStorage(TaskContext context)
	{
		java.util.ArrayList<Object> data = (java.util.ArrayList<Object>)context.getState();

		StorageModel model = (StorageModel)getWindow();
		NfsStorageModel storageModel = (NfsStorageModel)model.getSelectedItem();

		Guid hostId = (Guid)data.get(1);
		String path = (String)data.get(2);
		StorageDomainType domainType = (StorageDomainType)data.get(3);

		boolean success = true;
		String message = null;

		CleanNfsConnection(hostId);

		try
		{
			String storageName = null;
			RefObject<String> tempRef_storageName = new RefObject<String>(storageName);
			boolean tempVar = DataProvider.IsDomainAlreadyExist(path, tempRef_storageName);
				storageName = tempRef_storageName.argvalue;
			if (tempVar)
			{
				throw new RuntimeException("Import Operation failed. Domain " + storageName + " already exists in the system.");
			}

			storage_server_connections tempVar2 = new storage_server_connections();
			tempVar2.setconnection(path);
			tempVar2.setstorage_type(StorageType.NFS);
			nfsConnection = tempVar2;

			VdcReturnValueBase ret2 = Frontend.RunAction(VdcActionType.ConnectStorageToVds, new StorageServerConnectionParametersBase(nfsConnection, hostId));

			if (ret2 == null || !ret2.getSucceeded())
			{
				throw new RuntimeException("Failed to retrieve existing storage domain information.");
			}

			VdcQueryReturnValue ret3 = Frontend.RunQuery(VdcQueryType.GetExistingStorageDomainList, new GetExistingStorageDomainListParameters(hostId, StorageType.NFS, domainType, path));

			if (ret3 == null || !ret3.getSucceeded())
			{
				throw new RuntimeException("Failed to retrieve existing storage domain information.");
			}

			java.util.ArrayList<storage_domains> domains = ((java.util.ArrayList<storage_domains>)ret3.getReturnValue());
			if (domains.isEmpty())
			{
				throw new RuntimeException("There is no storage domain under the specified path. Please check path.");
			}

			storage_domains sdToAdd = Linq.FirstOrDefault(domains);
			storage_domain_static sdsToAdd = sdToAdd == null ? null : sdToAdd.getStorageStaticData();

			StorageDomainManagementParameter tempVar3 = new StorageDomainManagementParameter(sdsToAdd);
			tempVar3.setVdsId(hostId);
			VdcReturnValueBase ret4 = Frontend.RunAction(VdcActionType.AddExistingNFSStorageDomain, tempVar3);

			if (ret4 == null || !ret4.getSucceeded())
			{
				throw new RuntimeException();
			}


			//Attach storage to data center as neccessary.
			storage_pool dataCenter = (storage_pool)model.getDataCenter().getSelectedItem();
			if (!dataCenter.getId().equals(StorageModel.UnassignedDataCenterId))
			{
				AttachStorageToDataCenter(sdToAdd.getid(), dataCenter.getId());
			}
		}
		catch (RuntimeException ex)
		{
			success = false;
			message = ex.getMessage();
		}
		finally
		{
			CleanNfsConnection(hostId);
		}

		context.InvokeUIThread(this, new java.util.ArrayList<Object>(java.util.Arrays.asList(new Object[] { "Finish", success, storageModel, message })));
	}

	private void ImportSanStorage(TaskContext context)
	{
		java.util.ArrayList<Object> data = (java.util.ArrayList<Object>)context.getState();

		StorageModel model = (StorageModel)getWindow();
		ImportSanStorageModel storageModel = (ImportSanStorageModel)model.getSelectedItem();
		storage_domains storage = (storage_domains)storageModel.getSelectedItem();

		Guid hostId = (Guid)data.get(1);

		AddSANStorageDomainParameters tempVar = new AddSANStorageDomainParameters(storage == null ? null : storage.getStorageStaticData());
		tempVar.setVdsId(hostId);
		VdcReturnValueBase returnValue = Frontend.RunAction(VdcActionType.AddExistingSANStorageDomain, tempVar);


		if (returnValue != null && returnValue.getSucceeded())
		{
			//Attach storage to data center as neccessary.
			storage_pool dataCenter = (storage_pool)model.getDataCenter().getSelectedItem();
			if (!dataCenter.getId().equals(StorageModel.UnassignedDataCenterId))
			{
				AttachStorageToDataCenter(storage.getid(), dataCenter.getId());
			}
		}


		context.InvokeUIThread(this, new java.util.ArrayList<Object>(java.util.Arrays.asList(new Object[] { "Finish", returnValue != null && returnValue.getSucceeded(), storageModel, null })));
	}

	public void run(TaskContext context)
	{
		java.util.ArrayList<Object> data = (java.util.ArrayList<Object>)context.getState();
		String key = (String)data.get(0);

//C# TO JAVA CONVERTER NOTE: The following 'switch' operated on a string member and was converted to Java 'if-else' logic:
//		switch (key)
//ORIGINAL LINE: case "SaveNfs":
		if (StringHelper.stringsEqual(key, "SaveNfs"))
		{
				SaveNfsStorage(context);

		}
//ORIGINAL LINE: case "SaveLocal":
		else if (StringHelper.stringsEqual(key, "SaveLocal"))
		{
				SaveLocalStorage(context);

		}
//ORIGINAL LINE: case "SaveSan":
		else if (StringHelper.stringsEqual(key, "SaveSan"))
		{
				SaveSanStorage(context);

		}
//ORIGINAL LINE: case "ImportNfs":
		else if (StringHelper.stringsEqual(key, "ImportNfs"))
		{
				ImportNfsStorage(context);

		}
//ORIGINAL LINE: case "ImportSan":
		else if (StringHelper.stringsEqual(key, "ImportSan"))
		{
				ImportSanStorage(context);

		}
//ORIGINAL LINE: case "Finish":
		else if (StringHelper.stringsEqual(key, "Finish"))
		{
				getWindow().StopProgress();

				if ((Boolean)data.get(1))
				{
					Cancel();
				}
				else
				{
					((Model)data.get(2)).setMessage((String)data.get(3));
				}
		}
	}


	private SystemTreeItemModel systemTreeSelectedItem;
	public SystemTreeItemModel getSystemTreeSelectedItem()
	{
		return systemTreeSelectedItem;
	}
	public void setSystemTreeSelectedItem(SystemTreeItemModel value)
	{
		if (systemTreeSelectedItem != value)
		{
			systemTreeSelectedItem = value;
			OnSystemTreeSelectedItemChanged();
		}
	}

	private void OnSystemTreeSelectedItemChanged()
	{
		UpdateActionAvailability();
	}
}