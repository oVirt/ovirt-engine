package org.ovirt.engine.ui.uicommon.models.vms;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

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
import org.ovirt.engine.ui.uicommon.models.userportal.*;
import org.ovirt.engine.core.common.*;
import org.ovirt.engine.core.common.interfaces.*;
import org.ovirt.engine.core.common.businessentities.*;

import org.ovirt.engine.core.common.queries.*;
import org.ovirt.engine.ui.uicompat.*;
import org.ovirt.engine.ui.uicommon.dataprovider.*;
import org.ovirt.engine.ui.uicommon.*;
import org.ovirt.engine.ui.uicommon.models.*;

@SuppressWarnings("unused")
public class VmListModel extends ListWithDetailsModel implements ISupportSystemTreeContext
{

	private UICommand privateNewServerCommand;
	public UICommand getNewServerCommand()
	{
		return privateNewServerCommand;
	}
	private void setNewServerCommand(UICommand value)
	{
		privateNewServerCommand = value;
	}
	private UICommand privateNewDesktopCommand;
	public UICommand getNewDesktopCommand()
	{
		return privateNewDesktopCommand;
	}
	private void setNewDesktopCommand(UICommand value)
	{
		privateNewDesktopCommand = value;
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
	private UICommand privateRunCommand;
	public UICommand getRunCommand()
	{
		return privateRunCommand;
	}
	private void setRunCommand(UICommand value)
	{
		privateRunCommand = value;
	}
	private UICommand privatePauseCommand;
	public UICommand getPauseCommand()
	{
		return privatePauseCommand;
	}
	private void setPauseCommand(UICommand value)
	{
		privatePauseCommand = value;
	}
	private UICommand privateStopCommand;
	public UICommand getStopCommand()
	{
		return privateStopCommand;
	}
	private void setStopCommand(UICommand value)
	{
		privateStopCommand = value;
	}
	private UICommand privateShutdownCommand;
	public UICommand getShutdownCommand()
	{
		return privateShutdownCommand;
	}
	private void setShutdownCommand(UICommand value)
	{
		privateShutdownCommand = value;
	}
	private UICommand privateMigrateCommand;
	public UICommand getMigrateCommand()
	{
		return privateMigrateCommand;
	}
	private void setMigrateCommand(UICommand value)
	{
		privateMigrateCommand = value;
	}
	private UICommand privateNewTemplateCommand;
	public UICommand getNewTemplateCommand()
	{
		return privateNewTemplateCommand;
	}
	private void setNewTemplateCommand(UICommand value)
	{
		privateNewTemplateCommand = value;
	}
	private UICommand privateRunOnceCommand;
	public UICommand getRunOnceCommand()
	{
		return privateRunOnceCommand;
	}
	private void setRunOnceCommand(UICommand value)
	{
		privateRunOnceCommand = value;
	}
	private UICommand privateExportCommand;
	public UICommand getExportCommand()
	{
		return privateExportCommand;
	}
	private void setExportCommand(UICommand value)
	{
		privateExportCommand = value;
	}
	private UICommand privateMoveCommand;
	public UICommand getMoveCommand()
	{
		return privateMoveCommand;
	}
	private void setMoveCommand(UICommand value)
	{
		privateMoveCommand = value;
	}
	private UICommand privateRetrieveIsoImagesCommand;
	public UICommand getRetrieveIsoImagesCommand()
	{
		return privateRetrieveIsoImagesCommand;
	}
	private void setRetrieveIsoImagesCommand(UICommand value)
	{
		privateRetrieveIsoImagesCommand = value;
	}
	private UICommand privateGuideCommand;
	public UICommand getGuideCommand()
	{
		return privateGuideCommand;
	}
	private void setGuideCommand(UICommand value)
	{
		privateGuideCommand = value;
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

	private Model confirmWindow;
	public Model getConfirmWindow()
	{
		return confirmWindow;
	}
	public void setConfirmWindow(Model value)
	{
		if (confirmWindow != value)
		{
			confirmWindow = value;
			OnPropertyChanged(new PropertyChangedEventArgs("ConfirmWindow"));
		}
	}

	private Model errorWindow;
	public Model getErrorWindow()
	{
		return errorWindow;
	}
	public void setErrorWindow(Model value)
	{
		if (errorWindow != value)
		{
			errorWindow = value;
			OnPropertyChanged(new PropertyChangedEventArgs("ErrorWindow"));
		}
	}

	private ConsoleModel defaultConsoleModel;
	public ConsoleModel getDefaultConsoleModel()
	{
		return defaultConsoleModel;
	}
	public void setDefaultConsoleModel(ConsoleModel value)
	{
		if (defaultConsoleModel != value)
		{
			defaultConsoleModel = value;
			OnPropertyChanged(new PropertyChangedEventArgs("DefaultConsoleModel"));
		}
	}

	private ConsoleModel additionalConsoleModel;
	public ConsoleModel getAdditionalConsoleModel()
	{
		return additionalConsoleModel;
	}
	public void setAdditionalConsoleModel(ConsoleModel value)
	{
		if (additionalConsoleModel != value)
		{
			additionalConsoleModel = value;
			OnPropertyChanged(new PropertyChangedEventArgs("AdditionalConsoleModel"));
		}
	}

	private boolean hasAdditionalConsoleModel;
	public boolean getHasAdditionalConsoleModel()
	{
		return hasAdditionalConsoleModel;
	}
	public void setHasAdditionalConsoleModel(boolean value)
	{
		if (hasAdditionalConsoleModel != value)
		{
			hasAdditionalConsoleModel = value;
			OnPropertyChanged(new PropertyChangedEventArgs("HasAdditionalConsoleModel"));
		}
	}

	public ObservableCollection<ChangeCDModel> isoImages;
	public ObservableCollection<ChangeCDModel> getIsoImages()
	{
		return isoImages;
	}
	private void setIsoImages(ObservableCollection<ChangeCDModel> value)
	{
		if ((isoImages == null && value != null) || (isoImages != null && !isoImages.equals(value)))
		{
			isoImages = value;
			OnPropertyChanged(new PropertyChangedEventArgs("IsoImages"));
		}
	}

		//get { return SelectedItems == null ? new object[0] : SelectedItems.Cast<VM>().Select(a => a.vm_guid).Cast<object>().ToArray(); }
	protected Object[] getSelectedKeys()
	{
		if (getSelectedItems() == null)
		{
			return new Object[0];
		}

		Object[] keys = new Object[getSelectedItems().size()];
		for (int i = 0; i < getSelectedItems().size(); i++)
		{
			keys[i] = ((VM)getSelectedItems().get(i)).getId();
		}

		return keys;
	}

	private Object privateGuideContext;
	public Object getGuideContext()
	{
		return privateGuideContext;
	}
	public void setGuideContext(Object value)
	{
		privateGuideContext = value;
	}
	private VM privatecurrentVm;
	public VM getcurrentVm()
	{
		return privatecurrentVm;
	}
	public void setcurrentVm(VM value)
	{
		privatecurrentVm = value;
	}

	private java.util.HashMap<Guid, java.util.ArrayList<ConsoleModel>> cachedConsoleModels;

	private java.util.ArrayList<String> privateCustomPropertiesKeysList;
	private java.util.ArrayList<String> getCustomPropertiesKeysList()
	{
		return privateCustomPropertiesKeysList;
	}
	private void setCustomPropertiesKeysList(java.util.ArrayList<String> value)
	{
		privateCustomPropertiesKeysList = value;
	}


	public VmListModel()
	{
		setTitle("Virtual Machines");

		setDefaultSearchString("Vms:");
		setSearchString(getDefaultSearchString());

		cachedConsoleModels = new java.util.HashMap<Guid, java.util.ArrayList<ConsoleModel>>();

		setNewServerCommand(new UICommand("NewServer", this));
		setNewDesktopCommand(new UICommand("NewDesktop", this));
		setEditCommand(new UICommand("Edit", this));
		setRemoveCommand(new UICommand("Remove", this));
		setRunCommand(new UICommand("Run", this));
		setPauseCommand(new UICommand("Pause", this));
		setStopCommand(new UICommand("Stop", this));
		setShutdownCommand(new UICommand("Shutdown", this));
		setMigrateCommand(new UICommand("Migrate", this));
		setNewTemplateCommand(new UICommand("NewTemplate", this));
		setRunOnceCommand(new UICommand("RunOnce", this));
		setExportCommand(new UICommand("Export", this));
		setMoveCommand(new UICommand("Move", this));
		setGuideCommand(new UICommand("Guide", this));
		setRetrieveIsoImagesCommand(new UICommand("RetrieveIsoImages", this));
		setAssignTagsCommand(new UICommand("AssignTags", this));

		setIsoImages(new ObservableCollection<ChangeCDModel>());
		ChangeCDModel tempVar = new ChangeCDModel();
		tempVar.setTitle("Retrieving CDs...");
		getIsoImages().add(tempVar);

		UpdateActionAvailability();

		getSearchNextPageCommand().setIsAvailable(true);
		getSearchPreviousPageCommand().setIsAvailable(true);


		AsyncDataProvider.GetCustomPropertiesList(new AsyncQuery(this,
		new INewAsyncCallback() {
			@Override
			public void OnSuccess(Object target, Object returnValue) {

			VmListModel model = (VmListModel)target;
			if (returnValue != null)
			{
				String[] array = ((String)returnValue).split("[;]", -1);
				model.setCustomPropertiesKeysList(new java.util.ArrayList<String>());
				for (String s : array)
				{
					model.getCustomPropertiesKeysList().add(s);
				}
			}

			}
		}));
	}

	private void AssignTags()
	{
		if (getWindow() != null)
		{
			return;
		}

		TagListModel model = new TagListModel();
		setWindow(model);
		model.setTitle("Assign Tags");
		model.setHashName("assign_tags_vms");

		model.setAttachedTagsToEntities(GetAttachedTagsToSelectedVMs());
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

	private java.util.Map<Guid, Boolean> GetAttachedTagsToSelectedVMs()
	{
		java.util.HashMap<Guid, Boolean> tags = new java.util.HashMap<Guid, Boolean>();

		//var vmIds = SelectedItems
		//   .Cast<VM>()
		//   .Select(a => a.vm_guid)
		//   .ToList();
		java.util.ArrayList<Guid> vmIds = new java.util.ArrayList<Guid>();
		for (Object item : getSelectedItems())
		{
			VM vm = (VM)item;
			vmIds.add(vm.getId());
		}


		//var allAttachedTags = vmIds.SelectMany(a => DataProvider.GetAttachedTagsToVm(a)).ToList();
		java.util.ArrayList<org.ovirt.engine.core.common.businessentities.tags> allAttachedTags = new java.util.ArrayList<org.ovirt.engine.core.common.businessentities.tags>();
		for (Guid id : vmIds)
		{
			allAttachedTags.addAll(DataProvider.GetAttachedTagsToVm(id));
		}

		//var attachedTags = allAttachedTags
		//    .Distinct(new TagsEqualityComparer())
		//    .ToList();
//C# TO JAVA CONVERTER TODO TASK: There is no Java equivalent to LINQ queries:
		java.util.ArrayList<org.ovirt.engine.core.common.businessentities.tags> attachedTags = Linq.Distinct(allAttachedTags, new TagsEqualityComparer());

		//attachedTags.Each(a => { tags.Add(a.tag_id, allAttachedTags.Count(b => b.tag_id == a.tag_id) == vmIds.Count() ? true : false); });
		for (org.ovirt.engine.core.common.businessentities.tags tag : attachedTags)
		{
			int count = 0;
			for (org.ovirt.engine.core.common.businessentities.tags tag2 : allAttachedTags)
			{
				if (tag2.gettag_id().equals(tag.gettag_id()))
				{
					count++;
				}
			}
			tags.put(tag.gettag_id(), count == vmIds.size() ? true : false);
		}

		return tags;
	}

	private void OnAssignTags()
	{
		TagListModel model = (TagListModel)getWindow();

		//var vmIds = SelectedItems
		//    .Cast<VM>()
		//    .Select(a => a.vm_guid)
		//    .ToList();
		java.util.ArrayList<Guid> vmIds = new java.util.ArrayList<Guid>();
		for (Object item : getSelectedItems())
		{
			VM vm = (VM)item;
			vmIds.add(vm.getId());
		}

		java.util.Map<Guid, Boolean> attachedTags = GetAttachedTagsToSelectedVMs();

		//prepare attach/detach lists
		java.util.ArrayList<Guid> tagsToAttach = new java.util.ArrayList<Guid>();
		java.util.ArrayList<Guid> tagsToDetach = new java.util.ArrayList<Guid>();

		//model.Items
		//    .Cast<TagModel>()
		//    .First()
		//    .EachRecursive(a => a.Children, (a, b) =>
		//    {
		//        if (a.Selection == true && (!attachedTags.ContainsKey(a.Id) || attachedTags[a.Id] == false))
		//        {
		//            tagsToAttach.Add(a.Id);
		//        }
		//        else if (a.Selection == false && attachedTags.ContainsKey(a.Id))
		//        {
		//            tagsToDetach.Add(a.Id);
		//        }
		//    });
		if (model.getItems() != null && ((java.util.ArrayList<TagModel>)model.getItems()).size() > 0)
		{
			java.util.ArrayList<TagModel> tags = (java.util.ArrayList<TagModel>)model.getItems();
			TagModel rootTag = tags.get(0);
			TagModel.RecursiveEditAttachDetachLists(rootTag, attachedTags, tagsToAttach, tagsToDetach);
		}


		//Frontend.RunMultipleActions(VdcActionType.AttachVmsToTag,
		//    tagsToAttach.Select(a =>
		//        (VdcActionParametersBase)new AttachEntityToTagParameters(a, vmIds)
		//    )
		//    .ToList()
		//);

		java.util.ArrayList<VdcActionParametersBase> parameters = new java.util.ArrayList<VdcActionParametersBase>();
		for (Guid a : tagsToAttach)
		{
			parameters.add(new AttachEntityToTagParameters(a, vmIds));
		}
		Frontend.RunMultipleAction(VdcActionType.AttachVmsToTag, parameters);

		//Detach tags.
		//Frontend.RunMultipleActions(VdcActionType.DetachVmFromTag,
		//    tagsToDetach.Select(a =>
		//        (VdcActionParametersBase)new AttachEntityToTagParameters(a, vmIds)
		//    )
		//    .ToList()
		//);

		parameters = new java.util.ArrayList<VdcActionParametersBase>();
		for (Guid a : tagsToDetach)
		{
			parameters.add(new AttachEntityToTagParameters(a, vmIds));
		}
		Frontend.RunMultipleAction(VdcActionType.DetachVmFromTag, parameters);


		Cancel();
	}

	private void Guide()
	{
		VmGuideModel model = new VmGuideModel();
		setWindow(model);
		model.setTitle("New Virtual Machine - Guide Me");
		model.setHashName("new_virtual_machine_-_guide_me");

		model.setEntity(getGuideContext() != null ? DataProvider.GetVmById((Guid)getGuideContext()) : null);


		UICommand tempVar = new UICommand("Cancel", this);
		tempVar.setTitle("Configure Later");
		tempVar.setIsDefault(true);
		tempVar.setIsCancel(true);
		model.getCommands().add(tempVar);
	}

	@Override
	protected void InitDetailModels()
	{
		super.InitDetailModels();

		ObservableCollection<EntityModel> list = new ObservableCollection<EntityModel>();
		list.add(new VmGeneralModel());
		list.add(new VmInterfaceListModel());
		list.add(new VmDiskListModel());
		list.add(new VmSnapshotListModel());
		list.add(new VmEventListModel());
		list.add(new VmAppListModel());
		list.add(new PermissionListModel());
		setDetailModels(list);
	}

	@Override
	public boolean IsSearchStringMatch(String searchString)
	{
		return searchString.trim().toLowerCase().startsWith("vm");
	}

	@Override
	protected void SyncSearch()
	{
		SearchParameters tempVar = new SearchParameters(getSearchString(), SearchType.VM);
		tempVar.setMaxCount(getSearchPageSize());
		super.SyncSearch(VdcQueryType.Search, tempVar);
	}

	@Override
	protected void AsyncSearch()
	{
		super.AsyncSearch();

		setAsyncResult(Frontend.RegisterSearch(getSearchString(), SearchType.VM, getSearchPageSize()));
		setItems(getAsyncResult().getData());
	}

	private void UpdateConsoleModels()
	{
		java.util.List tempVar = getSelectedItems();
		java.util.List selectedItems = (tempVar != null) ? tempVar : new java.util.ArrayList();
		Object tempVar2 = getSelectedItem();
		VM vm = (VM)((tempVar2 instanceof VM) ? tempVar2 : null);

		if (vm == null || selectedItems.size() > 1)
		{
			setDefaultConsoleModel(null);
			setAdditionalConsoleModel(null);
			setHasAdditionalConsoleModel(false);
		}
		else
		{
			if (!cachedConsoleModels.containsKey(vm.getId()))
			{
				SpiceConsoleModel spiceConsoleModel = new SpiceConsoleModel();
				spiceConsoleModel.getErrorEvent().addListener(this);
				VncConsoleModel vncConsoleModel = new VncConsoleModel();
				RdpConsoleModel rdpConsoleModel = new RdpConsoleModel();

				cachedConsoleModels.put(vm.getId(), new java.util.ArrayList<ConsoleModel>(java.util.Arrays.asList(new ConsoleModel[] { spiceConsoleModel, vncConsoleModel, rdpConsoleModel })));
			}


			java.util.ArrayList<ConsoleModel> cachedModels = cachedConsoleModels.get(vm.getId());
			for (ConsoleModel a : cachedModels)
			{
				a.setEntity(null);
				a.setEntity(vm);
			}

			setDefaultConsoleModel(vm.getdisplay_type() == DisplayType.vnc ? cachedModels.get(1) : cachedModels.get(0));

			if (DataProvider.IsWindowsOsType(vm.getvm_os()))
			{
				for (ConsoleModel a : cachedModels)
				{
					if (a instanceof RdpConsoleModel)
					{
						setAdditionalConsoleModel(a);
						break;
					}
				}
				setHasAdditionalConsoleModel(true);
			}
			else
			{
				setAdditionalConsoleModel(null);
				setHasAdditionalConsoleModel(false);
			}
		}
	}

	public java.util.ArrayList<ConsoleModel> GetConsoleModelsByVmGuid(Guid vmGuid)
	{
		if (cachedConsoleModels != null && cachedConsoleModels.containsKey(vmGuid))
		{
			return cachedConsoleModels.get(vmGuid);
		}

		return null;
	}

	private void NewDesktop()
	{
		NewInternal(VmType.Desktop);
	}

	private void NewServer()
	{
		NewInternal(VmType.Server);
	}

	private void NewInternal(VmType vmType)
	{
		if (getWindow() != null)
		{
			return;
		}

		UnitVmModel model = new UnitVmModel(new NewVmModelBehavior());
		setWindow(model);
		model.setTitle(StringFormat.format("New %1$s Virtual Machine", vmType == VmType.Server ? "Server" : "Desktop"));
		model.setHashName("new_" + (vmType == VmType.Server ? "server" : "desktop"));
		model.setIsNew(true);
		model.setVmType(vmType);
		model.setCustomPropertiesKeysList(getCustomPropertiesKeysList());

		model.Initialize(getSystemTreeSelectedItem());

		// Ensures that the default provisioning is "Clone" for a new server and "Thin" for a new desktop.
		EntityModel selectedItem = null;
		boolean selectValue = model.getVmType() == VmType.Server;

		for (Object item : model.getProvisioning().getItems())
		{
			EntityModel a = (EntityModel)item;
			if ((Boolean)a.getEntity() == selectValue)
			{
				selectedItem = a;
				break;
		}
		}
		model.getProvisioning().setSelectedItem(selectedItem);


		UICommand tempVar = new UICommand("OnSave", this);
		tempVar.setTitle("OK");
		tempVar.setIsDefault(true);
		model.getCommands().add(tempVar);
		UICommand tempVar2 = new UICommand("Cancel", this);
		tempVar2.setTitle("Cancel");
		tempVar2.setIsCancel(true);
		model.getCommands().add(tempVar2);
	}

	private void Edit()
	{
		VM vm = (VM)getSelectedItem();
		if (vm == null)
		{
			return;
		}

		if (getWindow() != null)
		{
			return;
		}

		UnitVmModel model = new UnitVmModel(new ExistingVmModelBehavior(vm));
		model.setVmType(vm.getvm_type());
		setWindow(model);
		model.setTitle(StringFormat.format("Edit %1$s Virtual Machine", vm.getvm_type() == VmType.Server ? "Server" : "Desktop"));
		model.setHashName("edit_" + (vm.getvm_type() == VmType.Server ? "server" : "desktop"));
		model.setCustomPropertiesKeysList(getCustomPropertiesKeysList());

		model.Initialize(this.getSystemTreeSelectedItem());

		//TODO:
		//VDSGroup cluster = null;
		//if (model.Cluster.Items == null)
		//{
		//    model.Commands.Add(
		//        new UICommand("Cancel", this)
		//        {
		//            Title = "Cancel",
		//            IsCancel = true
		//        });
		//    return;
		//}


		UICommand tempVar = new UICommand("OnSave", this);
		tempVar.setTitle("OK");
		tempVar.setIsDefault(true);
		model.getCommands().add(tempVar);
		UICommand tempVar2 = new UICommand("Cancel", this);
		tempVar2.setTitle("Cancel");
		tempVar2.setIsCancel(true);
		model.getCommands().add(tempVar2);
	}

	private void remove()
	{
		if (getWindow() != null)
		{
			return;
		}

		ConfirmationModel model = new ConfirmationModel();
		setWindow(model);
		model.setTitle("Remove Virtual Machine(s)");
		model.setHashName("remove_virtual_machine");
		model.setMessage("Virtual Machine(s)");

		//model.Items = SelectedItems.Cast<VM>().Select(a => a.vm_name);
		java.util.ArrayList<String> list = new java.util.ArrayList<String>();
		for (Object selectedItem : getSelectedItems())
		{
			VM a = (VM)selectedItem;
			list.add(a.getvm_name());
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

	private void Move()
	{
		VM vm = (VM)getSelectedItem();
		if (vm == null)
		{
			return;
		}

		if (getWindow() != null)
		{
			return;
		}

		ListModel model = new ListModel();
		setWindow(model);
		model.setTitle("Move Virtual Machine");
		model.setHashName("move_virtual_machine");

		//var storageDomains = (vm.vmt_guid != Guid.Empty
		//    ? DataProvider.GetStorageDomainListByTemplate(vm.vmt_guid)
		//    : DataProvider.GetStorageDomainList(vm.storage_pool_id)
		//        .Where(a => a.storage_domain_type == StorageDomainType.Data || a.storage_domain_type == StorageDomainType.Master)
		//    );

		java.util.ArrayList<storage_domains> storageDomains;
		if (!vm.getvmt_guid().equals(Guid.Empty))
		{
			storageDomains = DataProvider.GetStorageDomainListByTemplate(vm.getvmt_guid());
		}
		else
		{
			storageDomains = new java.util.ArrayList<storage_domains>();
			for (storage_domains a : DataProvider.GetStorageDomainList(vm.getstorage_pool_id()))
			{
				if (a.getstorage_domain_type() == StorageDomainType.Data || a.getstorage_domain_type() == StorageDomainType.Master)
				{
					storageDomains.add(a);
				}
			}
		}


		// filter only the Active storage domains (Active regarding the relevant storage pool).
		//storageDomains = storageDomains.Where(a => a.status.HasValue && a.status.Value == StorageDomainStatus.Active);
		java.util.ArrayList<storage_domains> list = new java.util.ArrayList<storage_domains>();
		for (storage_domains a : storageDomains)
		{
			if (a.getstatus() != null && a.getstatus() == StorageDomainStatus.Active)
			{
				list.add(a);
			}
		}
		storageDomains = list;


		java.util.ArrayList<DiskImage> disks = DataProvider.GetVmDiskList(vm.getId());
		if (disks.size() > 0)
		{
			//storageDomains = storageDomains.Where(a => a.id != disks[0].storage_id);
			list = new java.util.ArrayList<storage_domains>();
			for (storage_domains a : storageDomains)
			{
				if (!a.getId().equals(disks.get(0).getstorage_ids().get(0)))
				{
					list.add(a);
				}
			}
			storageDomains = list;
		}

		Collections.sort(storageDomains, new Linq.StorageDomainByNameComparer());

		//var items = storageDomains.OrderBy(a => a.storage_name)
		//    .Select(a => new EntityModel() { Entity = a })
		//    .ToList();
		java.util.ArrayList<EntityModel> items = new java.util.ArrayList<EntityModel>();
		for (storage_domains a : storageDomains)
		{
			EntityModel m = new EntityModel();
			m.setEntity(a);
			items.add(m);
		}

		model.setItems(items);
		if (items.size() == 1)
		{
			items.get(0).setIsSelected(true);
		}


		if (items.isEmpty())
		{
			model.setMessage("The system could not find available target Storage Domain.\nPossible reasons:\n  - No active Storage Domain available\n  - The Template that the VM is based on does not exist on active Storage Domain");

			UICommand tempVar = new UICommand("Cancel", this);
			tempVar.setTitle("Close");
			tempVar.setIsDefault(true);
			tempVar.setIsCancel(true);
			model.getCommands().add(tempVar);
		}
		else
		{
			UICommand tempVar2 = new UICommand("OnMove", this);
			tempVar2.setTitle("OK");
			tempVar2.setIsDefault(true);
			model.getCommands().add(tempVar2);
			UICommand tempVar3 = new UICommand("Cancel", this);
			tempVar3.setTitle("Cancel");
			tempVar3.setIsCancel(true);
			model.getCommands().add(tempVar3);
		}
	}

	private void OnMove()
	{
		VM vm = (VM)getSelectedItem();

		if (vm == null)
		{
			Cancel();
			return;
		}

		ListModel model = (ListModel)getWindow();

		if (model.getProgress() != null)
		{
			return;
		}

		java.util.ArrayList<storage_domains> items = new java.util.ArrayList<storage_domains>();
		for (Object item : model.getItems())
		{
			EntityModel a = (EntityModel)item;
			if (a.getIsSelected())
			{
				items.add((storage_domains)a.getEntity());
			}
		}

		// should be only one:
		if (items.isEmpty())
		{
			return;
		}


		java.util.ArrayList<VdcActionParametersBase> parameters = new java.util.ArrayList<VdcActionParametersBase>();
		for (storage_domains a : items)
		{
			parameters.add(new MoveVmParameters(vm.getId(), a.getId()));
		}


		model.StartProgress(null);

		Frontend.RunMultipleAction(VdcActionType.MoveVm, parameters,
		new IFrontendMultipleActionAsyncCallback() {
			@Override
			public void Executed(FrontendMultipleActionAsyncResult  result) {

			ListModel localModel = (ListModel)result.getState();
			localModel.StopProgress();
			Cancel();

			}
		}, model);
	}

	private void Export()
	{
		VM vm = (VM)getSelectedItem();
		if (vm == null)
		{
			return;
		}

		if (getWindow() != null)
		{
			return;
		}

		ExportVmModel model = new ExportVmModel();
		setWindow(model);
		model.setTitle("Export Virtual Machine");
		model.setHashName("export_virtual_machine");

		//var storages = DataProvider.GetStorageDomainList(vm.storage_pool_id)
		//    .Where(a => a.storage_domain_type == StorageDomainType.ImportExport)
		//    .ToList();
		//model.Storage.Options = storages;
		//model.Storage.Value = storages.FirstOrDefault();
		java.util.ArrayList<storage_domains> storages = new java.util.ArrayList<storage_domains>();
		for (storage_domains a : DataProvider.GetStorageDomainList(vm.getstorage_pool_id()))
		{
			if (a.getstorage_domain_type() == StorageDomainType.ImportExport)
			{
				storages.add(a);
			}
		}
		model.getStorage().setItems(storages);
		model.getStorage().setSelectedItem(Linq.FirstOrDefault(storages));


		boolean noActiveStorage = true;
		for (storage_domains a : storages)
		{
			if (a.getstatus() == StorageDomainStatus.Active)
			{
				noActiveStorage = false;
				break;
			}
		}


		if (SelectedVmsOnDifferentDataCenters())
		{
			model.getCollapseSnapshots().setIsChangable(false);
			model.getForceOverride().setIsChangable(false);

			model.setMessage("Virtual Machines reside on several Data Centers. Make sure the exported Virtual Machines reside on the same Data Center.");


			UICommand tempVar = new UICommand("Cancel", this);
			tempVar.setTitle("Close");
			tempVar.setIsDefault(true);
			tempVar.setIsCancel(true);
			model.getCommands().add(tempVar);
		}
		else if (storages.isEmpty())
		{
			model.getCollapseSnapshots().setIsChangable(false);
			model.getForceOverride().setIsChangable(false);

			model.setMessage("There is no Export Domain to Backup the Virtual Machine into. Attach an Export Domain to the Virtual Machine(s) Data Center.");

			UICommand tempVar2 = new UICommand("Cancel", this);
			tempVar2.setTitle("Close");
			tempVar2.setIsDefault(true);
			tempVar2.setIsCancel(true);
			model.getCommands().add(tempVar2);

		}
		//else if (storages.All(a => a.status != StorageDomainStatus.Active))
		else if (noActiveStorage)
		{
			model.getCollapseSnapshots().setIsChangable(false);
			model.getForceOverride().setIsChangable(false);

			model.setMessage("The relevant Export Domain in not active. Please activate it.");

			UICommand tempVar3 = new UICommand("Cancel", this);
			tempVar3.setTitle("Close");
			tempVar3.setIsDefault(true);
			tempVar3.setIsCancel(true);
			model.getCommands().add(tempVar3);
		}
		else
		{
			showWarningOnExistingVms(model);

			UICommand tempVar4 = new UICommand("OnExport", this);
			tempVar4.setTitle("OK");
			tempVar4.setIsDefault(true);
			model.getCommands().add(tempVar4);
			UICommand tempVar5 = new UICommand("Cancel", this);
			tempVar5.setTitle("Cancel");
			tempVar5.setIsCancel(true);
			model.getCommands().add(tempVar5);
		}
	}

	private void showWarningOnExistingVms(ExportVmModel model)
	{
		Guid storageDomainId = ((storage_domains)model.getStorage().getSelectedItem()).getId();
		storage_pool storagePool = DataProvider.GetFirstStoragePoolByStorageDomain(storageDomainId);
		String existingVMs = "";
		if (storagePool != null)
		{
			GetAllFromExportDomainQueryParamenters tempVar = new GetAllFromExportDomainQueryParamenters(storagePool.getId(), storageDomainId);
			tempVar.setGetAll(true);
			VdcQueryReturnValue returnValue = Frontend.RunQuery(VdcQueryType.GetVmsFromExportDomain, tempVar);
			if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
			{
				for (Object selectedItem : getSelectedItems())
				{
					VM vm = (VM)selectedItem;

					//if (((List<VM>)returnValue.ReturnValue).SingleOrDefault(a => a.vm_guid == vm.vm_guid) != null)
					VM foundVm = null;
					for (VM a : (java.util.ArrayList<VM>)returnValue.getReturnValue())
					{
						if (a.getId().equals(vm.getId()))
						{
							foundVm = a;
							break;
						}
					}

					if (foundVm != null)
					{
						existingVMs += "\u2022  " + vm.getvm_name() + "\n";
					}
				}
			}
			if (!StringHelper.isNullOrEmpty(existingVMs))
			{
				model.setMessage(StringFormat.format("VM(s):\n%1$s already exist on the target Export Domain. If you want to override them, please check the 'Force Override' check-box.", existingVMs));
			}
		}
	}

	private boolean SelectedVmsOnDifferentDataCenters()
	{
		//List<VM> vms = SelectedItems.Cast<VM>().ToList();
		//return vms.GroupBy(a => a.storage_pool_id).Count() > 1 ? true : false;

		java.util.ArrayList<VM> vms = new java.util.ArrayList<VM>();
		for (Object selectedItem : getSelectedItems())
		{
			VM a = (VM)selectedItem;
			vms.add(a);
		}

		java.util.Map<NGuid, java.util.ArrayList<VM>> t = new java.util.HashMap<NGuid, java.util.ArrayList<VM>>();
		for (VM a : vms)
		{
			if (!t.containsKey(a.getstorage_pool_id()))
			{
				t.put(a.getstorage_pool_id(), new java.util.ArrayList<VM>());
			}

			java.util.ArrayList<VM> list = t.get(a.getstorage_pool_id());
			list.add(a);
		}

		return t.size() > 1;
	}

	private java.util.ArrayList<String> getTemplatesNotPresentOnExportDomain()
	{
		ExportVmModel model = (ExportVmModel)getWindow();
		Guid storageDomainId = ((storage_domains)model.getStorage().getSelectedItem()).getId();
		storage_pool storagePool = DataProvider.GetFirstStoragePoolByStorageDomain(storageDomainId);
		java.util.ArrayList<String> missingTemplates = new java.util.ArrayList<String>();

		if (storagePool != null)
		{
			GetAllFromExportDomainQueryParamenters tempVar = new GetAllFromExportDomainQueryParamenters(storagePool.getId(), storageDomainId);
			tempVar.setGetAll(true);
			VdcQueryReturnValue returnValue = Frontend.RunQuery(VdcQueryType.GetTemplatesFromExportDomain, tempVar);
			java.util.HashMap<String, java.util.ArrayList<String>> templateDic = new java.util.HashMap<String, java.util.ArrayList<String>>();
			//check if relevant templates are already there
			if (returnValue != null && returnValue.getSucceeded())
			{
				for (Object selectedItem : getSelectedItems())
				{
					VM vm = (VM)selectedItem;

					//if (vm.vmt_guid != Guid.Empty && ((Dictionary<VmTemplate, List<DiskImage>>)returnValue.ReturnValue).getKey()s.SingleOrDefault(a => vm.vmt_guid == a.vmt_guid) == null)

					boolean hasMatch = false;
					for (VmTemplate a : ((java.util.HashMap<VmTemplate, java.util.ArrayList<DiskImage>>)returnValue.getReturnValue()).keySet())
					{
						if (vm.getvmt_guid().equals(a.getId()))
						{
							hasMatch = true;
							break;
						}
					}

					if (!vm.getvmt_guid().equals(Guid.Empty) && !hasMatch)
					{
						if (!templateDic.containsKey(vm.getvmt_name()))
						{
							templateDic.put(vm.getvmt_name(), new java.util.ArrayList<String>());
						}
						templateDic.get(vm.getvmt_name()).add(vm.getvm_name());
						//missingTemplates.Add(StringFormat.format("Template '{0}' for VM '{1}'", vm.vmt_name, vm.vm_name));
					}
				}
				String tempStr;
				java.util.ArrayList<String> tempList;
				for (java.util.Map.Entry<String, java.util.ArrayList<String>> keyValuePair : templateDic.entrySet())
				{
					tempList = keyValuePair.getValue();
					tempStr = "Template " + keyValuePair.getKey() + " (for ";
					int i;
					for (i = 0; i < tempList.size() - 1; i++)
					{
						tempStr += tempList.get(i) + ", ";
					}
					tempStr += tempList.get(i) + ")";
					missingTemplates.add(tempStr);
				}
			}
			else
			{
				return null;
			}
		}
		return missingTemplates;
	}

	public void OnExport()
	{
		ExportVmModel model = (ExportVmModel)getWindow();
		Guid storageDomainId = ((storage_domains)model.getStorage().getSelectedItem()).getId();
		if (!model.Validate())
		{
			return;
		}

		java.util.ArrayList<String> missingTemplatesFromVms = getTemplatesNotPresentOnExportDomain();

		java.util.ArrayList<VdcActionParametersBase> parameters = new java.util.ArrayList<VdcActionParametersBase>();
		for (Object a : getSelectedItems())
		{
			VM vm = (VM)a;
			MoveVmParameters parameter = new MoveVmParameters(vm.getId(), storageDomainId);
			parameter.setForceOverride((Boolean)model.getForceOverride().getEntity());
			parameter.setCopyCollapse((Boolean)model.getCollapseSnapshots().getEntity());
			parameter.setTemplateMustExists(true);

			parameters.add(parameter);
		}


		if (!(Boolean)model.getCollapseSnapshots().getEntity())
		{
			if ((missingTemplatesFromVms == null || missingTemplatesFromVms.size() > 0))
			{
				ConfirmationModel confirmModel = new ConfirmationModel();
				setConfirmWindow(confirmModel);
				confirmModel.setTitle("Template(s) not Found on Export Domain");
				confirmModel.setHashName("template_not_found_on_export_domain");

				confirmModel.setMessage(missingTemplatesFromVms == null ? "Could not read templates from Export Domain" : "The following templates are missing on the target Export Domain:");
				confirmModel.setItems(missingTemplatesFromVms);

				UICommand tempVar = new UICommand("OnExportNoTemplates", this);
				tempVar.setTitle("OK");
				tempVar.setIsDefault(true);
				confirmModel.getCommands().add(tempVar);
				UICommand tempVar2 = new UICommand("CancelConfirmation", this);
				tempVar2.setTitle("Cancel");
				tempVar2.setIsCancel(true);
				confirmModel.getCommands().add(tempVar2);
			}
			else
			{
				if (model.getProgress() != null)
				{
					return;
				}

				model.StartProgress(null);

				Frontend.RunMultipleAction(VdcActionType.ExportVm, parameters,
		new IFrontendMultipleActionAsyncCallback() {
			@Override
			public void Executed(FrontendMultipleActionAsyncResult  result) {

					ExportVmModel localModel = (ExportVmModel)result.getState();
					localModel.StopProgress();
					Cancel();

			}
		}, model);
			}
		}
		else
		{
			if (model.getProgress() != null)
			{
				return;
			}

			for (VdcActionParametersBase item : parameters)
			{
				MoveVmParameters parameter = (MoveVmParameters)item;
				parameter.setTemplateMustExists(false);
			}


			model.StartProgress(null);

			Frontend.RunMultipleAction(VdcActionType.ExportVm, parameters,
		new IFrontendMultipleActionAsyncCallback() {
			@Override
			public void Executed(FrontendMultipleActionAsyncResult  result) {

				ExportVmModel localModel = (ExportVmModel)result.getState();
				localModel.StopProgress();
				Cancel();

			}
		}, model);
		}
	}

	private void OnExportNoTemplates()
	{
		ExportVmModel model = (ExportVmModel)getWindow();
		Guid storageDomainId = ((storage_domains)model.getStorage().getSelectedItem()).getId();

		if (model.getProgress() != null)
		{
			return;
		}

		java.util.ArrayList<VdcActionParametersBase> list = new java.util.ArrayList<VdcActionParametersBase>();
		for (Object item : getSelectedItems())
		{
			VM a = (VM)item;
			MoveVmParameters parameters = new MoveVmParameters(a.getId(), storageDomainId);
			parameters.setForceOverride((Boolean)model.getForceOverride().getEntity());
			parameters.setCopyCollapse((Boolean)model.getCollapseSnapshots().getEntity());
			parameters.setTemplateMustExists(false);

			list.add(parameters);
		}


		model.StartProgress(null);

		Frontend.RunMultipleAction(VdcActionType.ExportVm, list,
		new IFrontendMultipleActionAsyncCallback() {
			@Override
			public void Executed(FrontendMultipleActionAsyncResult  result) {

			ExportVmModel localModel = (ExportVmModel)result.getState();
			localModel.StopProgress();
			Cancel();

			}
		}, model);
	}

	private void RunOnce()
	{
		VM vm = (VM)getSelectedItem();
		if (vm == null)
		{
			return;
		}

		RunOnceModel model = new RunOnceModel();
		setWindow(model);
		model.setTitle("Run Virtual Machine(s)");
		model.setHashName("run_virtual_machine");
		model.getAttachIso().setEntity(false);

		java.util.ArrayList<String> images = DataProvider.GetIrsImageList(vm.getstorage_pool_id(), false);
		model.getIsoImage().setItems(images);
		//model.IsoImage.Value = images.FirstOrDefault();
		model.getIsoImage().setSelectedItem(Linq.FirstOrDefault(images));
		model.getAttachFloppy().setEntity(false);
		images = DataProvider.GetFloppyImageList(vm.getstorage_pool_id(), false);

		if (DataProvider.IsWindowsOsType(vm.getvm_os()))
		{
			// Add a pseudo floppy disk image used for Windows' sysprep.
			if (!vm.getis_initialized())
			{
				images.add(0, "[sysprep]");
				model.getAttachFloppy().setEntity(true);
			}
			else
			{
				images.add("[sysprep]");
			}
		}
		model.getFloppyImage().setItems(images);
		//model.FloppyImage.Value = images.FirstOrDefault();
		model.getFloppyImage().setSelectedItem(Linq.FirstOrDefault(images));
		model.getRunAsStateless().setEntity(vm.getis_stateless());
		model.setHwAcceleration(true);

		//Boot sequence.
		VdcQueryReturnValue returnValue = Frontend.RunQuery(VdcQueryType.GetVmInterfacesByVmId, new GetVmByVmIdParameters(vm.getId()));

		boolean hasNics = returnValue != null && returnValue.getSucceeded() && ((java.util.ArrayList<VmNetworkInterface>)returnValue.getReturnValue()).size() > 0;

		if (!hasNics)
		{
			BootSequenceModel bootSequenceModel = model.getBootSequence();
			bootSequenceModel.getNetworkOption().setIsChangable(false);
			bootSequenceModel.getNetworkOption().getChangeProhibitionReasons().add("Virtual Machine must have at least one network interface defined to boot from network.");
		}
		//passing Kernel parameters
		model.getKernel_parameters().setEntity(vm.getkernel_params());
		model.getKernel_path().setEntity(vm.getkernel_url());
		model.getInitrd_path().setEntity(vm.getinitrd_url());
		model.getCustomProperties().setEntity(vm.getCustomProperties());
		model.getCustomProperties().setIsChangable(DataProvider.IsSupportCustomProperties(vm.getvds_group_compatibility_version().toString()));
		model.setIsLinux_Unassign_UnknownOS(DataProvider.IsLinuxOsType(vm.getvm_os()) || vm.getvm_os() == VmOsType.Unassigned || vm.getvm_os() == VmOsType.Other);
		model.setIsWindowsOS(DataProvider.IsWindowsOsType(vm.getvm_os()));
		model.getIsVmFirstRun().setEntity(!vm.getis_initialized());
		model.getSysPrepDomainName().setSelectedItem(vm.getvm_domain());

		// Update Domain list
		AsyncDataProvider.GetDomainList(new AsyncQuery(model,
		new INewAsyncCallback() {
			@Override
			public void OnSuccess(Object target, Object returnValue1) {

			RunOnceModel runOnceModel = (RunOnceModel)target;
			java.util.List<String> domains = (java.util.List<String>)returnValue1;
			String oldDomain = (String)runOnceModel.getSysPrepDomainName().getSelectedItem();
			if (oldDomain != null && !oldDomain.equals("") && !domains.contains(oldDomain))
			{
				domains.add(0, oldDomain);
			}
			runOnceModel.getSysPrepDomainName().setItems(domains);
			runOnceModel.getSysPrepDomainName().setSelectedItem((oldDomain != null) ? oldDomain : Linq.FirstOrDefault(domains));

			}
		}), true);

		//Display protocols.
		EntityModel tempVar = new EntityModel();
		tempVar.setTitle("VNC");
		tempVar.setEntity(DisplayType.vnc);
		EntityModel vncProtocol = tempVar;

		EntityModel tempVar2 = new EntityModel();
		tempVar2.setTitle("Spice");
		tempVar2.setEntity(DisplayType.qxl);
		EntityModel qxlProtocol = tempVar2;

		java.util.ArrayList<EntityModel> items = new java.util.ArrayList<EntityModel>();
		items.add(vncProtocol);
		items.add(qxlProtocol);
		model.getDisplayProtocol().setItems(items);
		model.getDisplayProtocol().setSelectedItem(vm.getdefault_display_type() == DisplayType.vnc ? vncProtocol : qxlProtocol);

		model.setCustomPropertiesKeysList(this.getCustomPropertiesKeysList());

		UICommand tempVar3 = new UICommand("OnRunOnce", this);
		tempVar3.setTitle("OK");
		tempVar3.setIsDefault(true);
		model.getCommands().add(tempVar3);
		UICommand tempVar4 = new UICommand("Cancel", this);
		tempVar4.setTitle("Cancel");
		tempVar4.setIsCancel(true);
		model.getCommands().add(tempVar4);
	}

	private void OnRunOnce()
	{
		VM vm = (VM)getSelectedItem();
		if (vm == null)
		{
			Cancel();
			return;
		}

		RunOnceModel model = (RunOnceModel)getWindow();

		if (!model.Validate())
		{
			return;
		}

		BootSequenceModel bootSequenceModel = model.getBootSequence();

		RunVmOnceParams tempVar = new RunVmOnceParams();
		tempVar.setVmId(vm.getId());
		tempVar.setBootSequence(bootSequenceModel.getSequence());
		tempVar.setDiskPath((Boolean)model.getAttachIso().getEntity() ? (String)model.getIsoImage().getSelectedItem() : "");
		tempVar.setFloppyPath(model.getFloppyImagePath());
		tempVar.setKvmEnable(model.getHwAcceleration());
		tempVar.setRunAndPause((Boolean)model.getRunAndPause().getEntity());
		tempVar.setAcpiEnable(true);
		tempVar.setRunAsStateless((Boolean)model.getRunAsStateless().getEntity());
		tempVar.setReinitialize(model.getReinitialize());
		tempVar.setCustomProperties((String)model.getCustomProperties().getEntity());
		RunVmOnceParams param = tempVar;

		//kernel params
		if (model.getKernel_path().getEntity() != null)
		{
			param.setkernel_url((String)model.getKernel_path().getEntity());
		}
		if (model.getKernel_parameters().getEntity() != null)
		{
			param.setkernel_params((String)model.getKernel_parameters().getEntity());
		}
		if (model.getInitrd_path().getEntity() != null)
		{
			param.setinitrd_url((String)model.getInitrd_path().getEntity());
		}

		//Sysprep params
		if (model.getSysPrepDomainName().getSelectedItem() != null)
		{
			param.setSysPrepDomainName((String)model.getSysPrepDomainName().getSelectedItem());
		}
		if (model.getSysPrepUserName().getEntity() != null)
		{
			param.setSysPrepUserName((String)model.getSysPrepUserName().getEntity());
		}
		if (model.getSysPrepPassword().getEntity() != null)
		{
			param.setSysPrepPassword((String)model.getSysPrepPassword().getEntity());
		}

		EntityModel displayProtocolSelectedItem = (EntityModel)model.getDisplayProtocol().getSelectedItem();
		param.setUseVnc((DisplayType)displayProtocolSelectedItem.getEntity() == DisplayType.vnc);

		Frontend.RunActionAsyncroniousely(VdcActionType.RunVmOnce, param);

		Cancel();
	}

	private void NewTemplate()
	{
		VM vm = (VM)getSelectedItem();
		if (vm == null)
		{
			return;
		}

		if (getWindow() != null)
		{
			return;
		}

		UnitVmModel model = new UnitVmModel(new NewTemplateVmModelBehavior(vm));
		setWindow(model);
		model.setTitle("New Template");
		model.setHashName("new_template");
		model.setIsNew(true);
		model.setVmType(vm.getvm_type());

		model.Initialize(getSystemTreeSelectedItem());


		UICommand tempVar = new UICommand("OnNewTemplate", this);
		tempVar.setTitle("OK");
		tempVar.setIsDefault(true);
		model.getCommands().add(tempVar);
		UICommand tempVar2 = new UICommand("Cancel", this);
		tempVar2.setTitle("Cancel");
		tempVar2.setIsCancel(true);
		model.getCommands().add(tempVar2);
	}

	private void DisableNewTemplateModel(VmModel model, String errMessage)
	{
		model.setMessage(errMessage);

		model.getName().setIsChangable(false);
		model.getDescription().setIsChangable(false);
		model.getCluster().setIsChangable(false);
		model.getStorageDomain().setIsChangable(false);
		model.getIsTemplatePublic().setIsChangable(false);

		UICommand tempVar = new UICommand("Cancel", this);
		tempVar.setTitle("Close");
		tempVar.setIsCancel(true);
		model.getCommands().add(tempVar);
	}

	private void OnNewTemplate()
	{
		UnitVmModel model = (UnitVmModel)getWindow();
		VM vm = (VM)getSelectedItem();
		if (vm == null)
		{
			Cancel();
			return;
		}

		if (model.getProgress() != null)
		{
			return;
		}

		if (!model.Validate())
		{
			return;
		}

		String name = (String)model.getName().getEntity();

		//Check name unicitate.
		if (!DataProvider.IsTemplateNameUnique(name))
		{
			model.getName().setIsValid(false);
			model.getName().getInvalidityReasons().add("Name must be unique.");
			model.setIsGeneralTabValid(false);
			return;
		}

		VM tempVar = new VM();
		tempVar.setId(vm.getId());
		tempVar.setvm_type(model.getVmType());
		tempVar.setvm_os((VmOsType)model.getOSType().getSelectedItem());
		tempVar.setnum_of_monitors((Integer)model.getNumOfMonitors().getSelectedItem());
		tempVar.setvm_domain(model.getDomain().getIsAvailable() ? (String)model.getDomain().getSelectedItem() : "");
		tempVar.setvm_mem_size_mb((Integer)model.getMemSize().getEntity());
		tempVar.setMinAllocatedMem((Integer)model.getMinAllocatedMemory().getEntity());
		tempVar.setvds_group_id(((VDSGroup)model.getCluster().getSelectedItem()).getId());
		tempVar.settime_zone(model.getTimeZone().getIsAvailable() && model.getTimeZone().getSelectedItem() != null ? ((java.util.Map.Entry<String, String>)model.getTimeZone().getSelectedItem()).getKey() : "");
		tempVar.setnum_of_sockets((Integer)model.getNumOfSockets().getEntity());
		tempVar.setcpu_per_socket((Integer)model.getTotalCPUCores().getEntity() / (Integer)model.getNumOfSockets().getEntity());
		tempVar.setusb_policy((UsbPolicy)model.getUsbPolicy().getSelectedItem());
		tempVar.setis_auto_suspend(false);
		tempVar.setis_stateless((Boolean)model.getIsStateless().getEntity());
		tempVar.setdefault_boot_sequence(model.getBootSequence());
		tempVar.setauto_startup((Boolean)model.getIsHighlyAvailable().getEntity());
		tempVar.setiso_path(model.getCdImage().getIsChangable() ? (String)model.getCdImage().getSelectedItem() : "");
		tempVar.setinitrd_url(vm.getinitrd_url());
		tempVar.setkernel_url(vm.getkernel_url());
		tempVar.setkernel_params(vm.getkernel_params());
		VM newvm = tempVar;

		EntityModel displayProtocolSelectedItem = (EntityModel)model.getDisplayProtocol().getSelectedItem();
		newvm.setdefault_display_type((DisplayType)displayProtocolSelectedItem.getEntity());

		EntityModel prioritySelectedItem = (EntityModel)model.getPriority().getSelectedItem();
		newvm.setpriority((Integer)prioritySelectedItem.getEntity());

		AddVmTemplateParameters addVmTemplateParameters = new AddVmTemplateParameters(newvm, (String)model.getName().getEntity(), (String)model.getDescription().getEntity());
		addVmTemplateParameters.setDestinationStorageDomainId(((storage_domains)model.getStorageDomain().getSelectedItem()).getId());
		addVmTemplateParameters.setPublicUse((Boolean)model.getIsTemplatePublic().getEntity());


		model.StartProgress(null);

		Frontend.RunAction(VdcActionType.AddVmTemplate, addVmTemplateParameters,
		new IFrontendActionAsyncCallback() {
			@Override
			public void Executed(FrontendActionAsyncResult  result) {

			VmListModel vmListModel = (VmListModel)result.getState();
			vmListModel.getWindow().StopProgress();
			VdcReturnValueBase returnValueBase = result.getReturnValue();
			if (returnValueBase != null && returnValueBase.getSucceeded())
			{
				vmListModel.Cancel();
			}

			}
		}, this);
	}

	private void Migrate()
	{
		VM vm = (VM)getSelectedItem();
		if (vm == null)
		{
			return;
		}

		if (getWindow() != null)
		{
			return;
		}

		MigrateModel model = new MigrateModel();
		setWindow(model);
		model.setTitle("Migrate Virtual Machine(s)");
		model.setHashName("migrate_virtual_machine");
		model.setIsAutoSelect(true);
		model.setVmList(Linq.<VM>Cast(getSelectedItems()));
		java.util.ArrayList<VDS> hosts = DataProvider.GetUpHostListByCluster(vm.getvds_group_name());
		model.setVmsOnSameCluster(true);
		NGuid run_on_vds = null;
		boolean allRunOnSameVds = true;

		for (Object item : getSelectedItems())
		{
			VM a = (VM)item;
			if (!a.getvds_group_id().equals(((VM)getSelectedItems().get(0)).getvds_group_id()))
			{
				model.setVmsOnSameCluster(false);
			}
			if (run_on_vds == null)
			{
				run_on_vds = a.getrun_on_vds().getValue();
			}
			else if(allRunOnSameVds && !run_on_vds.equals(a.getrun_on_vds().getValue()))
			{
				allRunOnSameVds = false;
			}
		}

		model.setIsHostSelAvailable(model.getVmsOnSameCluster() && hosts.size() > 0);

		if (model.getVmsOnSameCluster() && allRunOnSameVds)
		{
			VDS runOnSameVDS = null;
			for (VDS host : hosts)
			{
				if(host.getId().equals(run_on_vds))
				{
					runOnSameVDS = host;
				}
			}
			hosts.remove(runOnSameVDS);
		}
		if (hosts.isEmpty())
		{
			model.setIsHostSelAvailable(false);

			if (allRunOnSameVds)
			{
				model.setNoSelAvailable(true);

				UICommand tempVar = new UICommand("Cancel", this);
				tempVar.setTitle("Close");
				tempVar.setIsDefault(true);
				tempVar.setIsCancel(true);
				model.getCommands().add(tempVar);
			}
		}
		else
		{
			model.getHosts().setItems(hosts);
			model.getHosts().setSelectedItem(Linq.FirstOrDefault(hosts));

			UICommand tempVar2 = new UICommand("OnMigrate", this);
			tempVar2.setTitle("OK");
			tempVar2.setIsDefault(true);
			model.getCommands().add(tempVar2);
			UICommand tempVar3 = new UICommand("Cancel", this);
			tempVar3.setTitle("Cancel");
			tempVar3.setIsCancel(true);
			model.getCommands().add(tempVar3);
		}
	}

	private void OnMigrate()
	{
		MigrateModel model = (MigrateModel)getWindow();

		if (model.getProgress() != null)
		{
			return;
		}

		model.StartProgress(null);

		if (model.getIsAutoSelect())
		{
			java.util.ArrayList<VdcActionParametersBase> list = new java.util.ArrayList<VdcActionParametersBase>();
			for (Object item : getSelectedItems())
			{
				VM a = (VM)item;
				list.add(new MigrateVmParameters(true, a.getId()));
			}

			Frontend.RunMultipleAction(VdcActionType.MigrateVm, list,
		new IFrontendMultipleActionAsyncCallback() {
			@Override
			public void Executed(FrontendMultipleActionAsyncResult  result) {

				MigrateModel localModel = (MigrateModel)result.getState();
				localModel.StopProgress();
				Cancel();

			}
		}, model);
		}
		else
		{
			java.util.ArrayList<VdcActionParametersBase> list = new java.util.ArrayList<VdcActionParametersBase>();
			for (Object item : getSelectedItems())
			{
				VM a = (VM)item;

				if (a.getrun_on_vds().getValue().equals(((VDS)model.getHosts().getSelectedItem()).getId()))
				{
					continue;
				}

				list.add(new MigrateVmToServerParameters(true, a.getId(), ((VDS)model.getHosts().getSelectedItem()).getId()));
			}

			Frontend.RunMultipleAction(VdcActionType.MigrateVmToServer, list,
		new IFrontendMultipleActionAsyncCallback() {
			@Override
			public void Executed(FrontendMultipleActionAsyncResult  result) {

				MigrateModel localModel = (MigrateModel)result.getState();
				localModel.StopProgress();
				Cancel();

			}
		}, model);
		}
	}

	private void Shutdown()
	{
		ConfirmationModel model = new ConfirmationModel();
		setWindow(model);
		model.setTitle("Shut down Virtual Machine(s)");
		model.setHashName("shut_down_virtual_machine");
		model.setMessage("Are you sure you want to Shut down the following Virtual Machines?");
		//model.Items = SelectedItems.Cast<VM>().Select(a => a.vm_name);
		java.util.ArrayList<String> items = new java.util.ArrayList<String>();
		for (Object item : getSelectedItems())
		{
			VM a = (VM)item;
			items.add(a.getvm_name());
		}
		model.setItems(items);


		UICommand tempVar = new UICommand("OnShutdown", this);
		tempVar.setTitle("OK");
		tempVar.setIsDefault(true);
		model.getCommands().add(tempVar);
		UICommand tempVar2 = new UICommand("Cancel", this);
		tempVar2.setTitle("Cancel");
		tempVar2.setIsCancel(true);
		model.getCommands().add(tempVar2);
	}

	private void OnShutdown()
	{
		ConfirmationModel model = (ConfirmationModel)getWindow();

		if (model.getProgress() != null)
		{
			return;
		}

		java.util.ArrayList<VdcActionParametersBase> list = new java.util.ArrayList<VdcActionParametersBase>();
		for (Object item : getSelectedItems())
		{
			VM a = (VM)item;
			list.add(new ShutdownVmParameters(a.getId(), true));
		}


		model.StartProgress(null);

		Frontend.RunMultipleAction(VdcActionType.ShutdownVm, list,
		new IFrontendMultipleActionAsyncCallback() {
			@Override
			public void Executed(FrontendMultipleActionAsyncResult  result) {

			ConfirmationModel localModel = (ConfirmationModel)result.getState();
			localModel.StopProgress();
			Cancel();

			}
		}, model);
	}

	private void stop()
	{
		ConfirmationModel model = new ConfirmationModel();
		setWindow(model);
		model.setTitle("Stop Virtual Machine(s)");
		model.setHashName("stop_virtual_machine");
		model.setMessage("Are you sure you want to Stop the following Virtual Machines?");
		//model.Items = SelectedItems.Cast<VM>().Select(a => a.vm_name);
		java.util.ArrayList<String> items = new java.util.ArrayList<String>();
		for (Object item : getSelectedItems())
		{
			VM a = (VM)item;
			items.add(a.getvm_name());
		}
		model.setItems(items);


		UICommand tempVar = new UICommand("OnStop", this);
		tempVar.setTitle("OK");
		tempVar.setIsDefault(true);
		model.getCommands().add(tempVar);
		UICommand tempVar2 = new UICommand("Cancel", this);
		tempVar2.setTitle("Cancel");
		tempVar2.setIsCancel(true);
		model.getCommands().add(tempVar2);
	}

	private void OnStop()
	{
		ConfirmationModel model = (ConfirmationModel)getWindow();

		if (model.getProgress() != null)
		{
			return;
		}

		java.util.ArrayList<VdcActionParametersBase> list = new java.util.ArrayList<VdcActionParametersBase>();
		for (Object item : getSelectedItems())
		{
			VM a = (VM)item;
			list.add(new StopVmParameters(a.getId(), StopVmTypeEnum.NORMAL));
		}


		model.StartProgress(null);

		Frontend.RunMultipleAction(VdcActionType.StopVm, list,
		new IFrontendMultipleActionAsyncCallback() {
			@Override
			public void Executed(FrontendMultipleActionAsyncResult  result) {

			ConfirmationModel localModel = (ConfirmationModel)result.getState();
			localModel.StopProgress();
			Cancel();

			}
		}, model);
	}

	private void Pause()
	{
		java.util.ArrayList<VdcActionParametersBase> list = new java.util.ArrayList<VdcActionParametersBase>();
		for (Object item : getSelectedItems())
		{
			VM a = (VM)item;
			list.add(new HibernateVmParameters(a.getId()));
		}

		Frontend.RunMultipleAction(VdcActionType.HibernateVm, list,
		new IFrontendMultipleActionAsyncCallback() {
			@Override
			public void Executed(FrontendMultipleActionAsyncResult  result) {


			}
		}, null);
	}

	private void Run()
	{
		java.util.ArrayList<VdcActionParametersBase> list = new java.util.ArrayList<VdcActionParametersBase>();
		for (Object item : getSelectedItems())
		{
			VM a = (VM)item;
			//use sysprep iff the vm is not initialized and vm has Win OS
			boolean reinitialize = !a.getis_initialized() && DataProvider.IsWindowsOsType(a.getvm_os());
			RunVmParams tempVar = new RunVmParams(a.getId());
			tempVar.setReinitialize(reinitialize);
			list.add(tempVar);
		}

		Frontend.RunMultipleAction(VdcActionType.RunVm, list,
		new IFrontendMultipleActionAsyncCallback() {
			@Override
			public void Executed(FrontendMultipleActionAsyncResult  result) {


			}
		}, null);
	}

	private void OnRemove()
	{
		ConfirmationModel model = (ConfirmationModel)getWindow();

		if (model.getProgress() != null)
		{
			return;
		}

		java.util.ArrayList<VdcActionParametersBase> list = new java.util.ArrayList<VdcActionParametersBase>();
		for (Object item : getSelectedItems())
		{
			VM a = (VM)item;
			list.add(new RemoveVmParameters(a.getId(), false));
		}


		model.StartProgress(null);

		Frontend.RunMultipleAction(VdcActionType.RemoveVm, list,
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
		UnitVmModel model = (UnitVmModel)getWindow();
		VM selectedItem = (VM)getSelectedItem();
		if (model.getIsNew() == false && selectedItem == null)
		{
			Cancel();
			return;
		}

		setcurrentVm(model.getIsNew() ? new VM() : (VM)Cloner.clone(selectedItem));

		if (!model.Validate())
		{
			return;
		}

		String name = (String)model.getName().getEntity();

		//Check name unicitate.
		if (!DataProvider.IsVmNameUnique(name) && name.compareToIgnoreCase(getcurrentVm().getvm_name()) != 0)
		{
			model.getName().setIsValid(false);
			model.getName().getInvalidityReasons().add("Name must be unique.");
			model.setIsGeneralTabValid(false);
			return;
		}

		//Save changes.
		VmTemplate template = (VmTemplate)model.getTemplate().getSelectedItem();

		getcurrentVm().setvm_type(model.getVmType());
		getcurrentVm().setvmt_guid(template.getId());
		getcurrentVm().setvm_name(name);
		getcurrentVm().setvm_os((VmOsType)model.getOSType().getSelectedItem());
		getcurrentVm().setnum_of_monitors((Integer)model.getNumOfMonitors().getSelectedItem());
		getcurrentVm().setvm_description((String)model.getDescription().getEntity());
		getcurrentVm().setvm_domain(model.getDomain().getIsAvailable() ? (String)model.getDomain().getSelectedItem() : "");
		getcurrentVm().setvm_mem_size_mb((Integer)model.getMemSize().getEntity());
		getcurrentVm().setMinAllocatedMem((Integer)model.getMinAllocatedMemory().getEntity());
		Guid newClusterID = ((VDSGroup)model.getCluster().getSelectedItem()).getId();
		getcurrentVm().setvds_group_id(newClusterID);
		getcurrentVm().settime_zone((model.getTimeZone().getIsAvailable() && model.getTimeZone().getSelectedItem() != null) ? ((java.util.Map.Entry<String, String>)model.getTimeZone().getSelectedItem()).getKey() : "");
		getcurrentVm().setnum_of_sockets((Integer)model.getNumOfSockets().getEntity());
		getcurrentVm().setcpu_per_socket((Integer)model.getTotalCPUCores().getEntity() / (Integer)model.getNumOfSockets().getEntity());
		getcurrentVm().setusb_policy((UsbPolicy)model.getUsbPolicy().getSelectedItem());
		getcurrentVm().setis_auto_suspend(false);
		getcurrentVm().setis_stateless((Boolean)model.getIsStateless().getEntity());
		getcurrentVm().setdefault_boot_sequence(model.getBootSequence());
		getcurrentVm().setiso_path(model.getCdImage().getIsChangable() ? (String)model.getCdImage().getSelectedItem() : "");
		getcurrentVm().setauto_startup((Boolean)model.getIsHighlyAvailable().getEntity());

		getcurrentVm().setinitrd_url((String)model.getInitrd_path().getEntity());
		getcurrentVm().setkernel_url((String)model.getKernel_path().getEntity());
		getcurrentVm().setkernel_params((String)model.getKernel_parameters().getEntity());

		getcurrentVm().setCustomProperties((String)model.getCustomProperties().getEntity());

		EntityModel displayProtocolSelectedItem = (EntityModel)model.getDisplayProtocol().getSelectedItem();
		getcurrentVm().setdefault_display_type((DisplayType)displayProtocolSelectedItem.getEntity());

		EntityModel prioritySelectedItem = (EntityModel)model.getPriority().getSelectedItem();
		getcurrentVm().setpriority((Integer)prioritySelectedItem.getEntity());


		VDS defaultHost = (VDS)model.getDefaultHost().getSelectedItem();
		if ((Boolean)model.getIsAutoAssign().getEntity())
		{
			getcurrentVm().setdedicated_vm_for_vds(null);
		}
		else
		{
			getcurrentVm().setdedicated_vm_for_vds(defaultHost.getId());
		}

		getcurrentVm().setMigrationSupport(MigrationSupport.MIGRATABLE);
		if ((Boolean)model.getRunVMOnSpecificHost().getEntity())
		{
			getcurrentVm().setMigrationSupport(MigrationSupport.PINNED_TO_HOST);
		}
		else if ((Boolean)model.getDontMigrateVM().getEntity())
		{
			getcurrentVm().setMigrationSupport(MigrationSupport.IMPLICITLY_NON_MIGRATABLE);
		}

		if (model.getIsNew())
		{
			if (getcurrentVm().getvmt_guid().equals(Guid.Empty))
			{
				if (model.getProgress() != null)
				{
					return;
				}

				model.StartProgress(null);

				Frontend.RunAction(VdcActionType.AddVmFromScratch, new AddVmFromScratchParameters(getcurrentVm(), new java.util.ArrayList<DiskImageBase>(), Guid.Empty),
		new IFrontendActionAsyncCallback() {
			@Override
			public void Executed(FrontendActionAsyncResult  result) {

					VmListModel vmListModel = (VmListModel)result.getState();
					vmListModel.getWindow().StopProgress();
					VdcReturnValueBase returnValueBase = result.getReturnValue();
					if (returnValueBase != null && returnValueBase.getSucceeded())
					{
						vmListModel.Cancel();
						vmListModel.setGuideContext(returnValueBase.getActionReturnValue());
						vmListModel.UpdateActionAvailability();
						vmListModel.getGuideCommand().Execute();
					}

			}
		}, this);
			}
			else
			{
				if (model.getProgress() != null)
				{
					return;
				}

				storage_domains storageDomain = (storage_domains)model.getStorageDomain().getSelectedItem();

				if ((Boolean)((EntityModel)model.getProvisioning().getSelectedItem()).getEntity())
				{
					java.util.ArrayList<DiskImage> templateDisks = DataProvider.GetTemplateDiskList(template.getId());
					for (DiskImage templateDisk : templateDisks)
					{
						//DiskModel disk = model.Disks.First(a => a.Name == templateDisk.internal_drive_mapping);
						DiskModel disk = null;
						for (DiskModel a : model.getDisks())
						{
							if (StringHelper.stringsEqual(a.getName(), templateDisk.getinternal_drive_mapping()))
							{
								disk = a;
								break;
							}
						}

						if (disk != null)
						{
							templateDisk.setvolume_type((VolumeType)disk.getVolumeType().getSelectedItem());
							templateDisk.setvolume_format(DataProvider.GetDiskVolumeFormat((VolumeType)disk.getVolumeType().getSelectedItem(), storageDomain.getstorage_type()));
						}
					}

					HashMap<Guid, DiskImage> dict = new HashMap<Guid, DiskImage>();
					for (DiskImage a : templateDisks)
					{
						dict.put(a.getImageId(), a);
					}

					model.StartProgress(null);

					Frontend.RunAction(VdcActionType.AddVmFromTemplate, new AddVmFromTemplateParameters(getcurrentVm(), dict, storageDomain.getId()),
		new IFrontendActionAsyncCallback() {
			@Override
			public void Executed(FrontendActionAsyncResult  result) {

						VmListModel vmListModel = (VmListModel)result.getState();
						vmListModel.getWindow().StopProgress();
						VdcReturnValueBase returnValueBase = result.getReturnValue();
						if (returnValueBase != null && returnValueBase.getSucceeded())
						{
							vmListModel.Cancel();
						}

			}
		}, this);
				}
				else
				{
					if (model.getProgress() != null)
					{
						return;
					}

					model.StartProgress(null);

					VmManagementParametersBase tempVar = new VmManagementParametersBase(getcurrentVm());
					tempVar.setStorageDomainId(storageDomain.getId());
					Frontend.RunAction(VdcActionType.AddVm, tempVar,
		new IFrontendActionAsyncCallback() {
			@Override
			public void Executed(FrontendActionAsyncResult  result) {

						VmListModel vmListModel = (VmListModel)result.getState();
						vmListModel.getWindow().StopProgress();
						VdcReturnValueBase returnValueBase = result.getReturnValue();
						if (returnValueBase != null && returnValueBase.getSucceeded())
						{
							vmListModel.Cancel();
						}

			}
		}, this);
				}
			}
		}
		else // Update existing VM -> consists of editing VM cluster, and if succeeds - editing VM:
		{
			if (model.getProgress() != null)
			{
				return;
			}

			// runEditVM: should be true if Cluster hasn't changed or if
			// Cluster has changed and Editing it in the Backend has succeeded:
			Guid oldClusterID = selectedItem.getvds_group_id();
			if (oldClusterID.equals(newClusterID) == false)
			{
				ChangeVMClusterParameters parameters = new ChangeVMClusterParameters(newClusterID, getcurrentVm().getId());

				model.StartProgress(null);

				Frontend.RunAction(VdcActionType.ChangeVMCluster, parameters,
		new IFrontendActionAsyncCallback() {
			@Override
			public void Executed(FrontendActionAsyncResult  result) {

					VmListModel vmListModel = (VmListModel)result.getState();
					VdcReturnValueBase returnValueBase = result.getReturnValue();
					if (returnValueBase != null && returnValueBase.getSucceeded())
					{
						Frontend.RunAction(VdcActionType.UpdateVm, new VmManagementParametersBase(vmListModel.getcurrentVm()),
		new IFrontendActionAsyncCallback() {
			@Override
			public void Executed(FrontendActionAsyncResult  result1) {

							VmListModel vmListModel1 = (VmListModel)result1.getState();
							vmListModel1.getWindow().StopProgress();
							VdcReturnValueBase retVal = result1.getReturnValue();
							boolean isSucceeded = retVal.getSucceeded();
							if (retVal != null && isSucceeded)
							{
								vmListModel1.Cancel();
							}

			}
		}, vmListModel);
					}
					else
					{
						vmListModel.getWindow().StopProgress();
					}

			}
		}, this);
			}
			else
			{
				if (model.getProgress() != null)
				{
					return;
				}

				model.StartProgress(null);

				Frontend.RunAction(VdcActionType.UpdateVm, new VmManagementParametersBase(getcurrentVm()),
		new IFrontendActionAsyncCallback() {
			@Override
			public void Executed(FrontendActionAsyncResult  result) {

					VmListModel vmListModel = (VmListModel)result.getState();
					vmListModel.getWindow().StopProgress();
					VdcReturnValueBase returnValueBase = result.getReturnValue();
					if (returnValueBase != null && returnValueBase.getSucceeded())
					{
						vmListModel.Cancel();
					}

			}
		}, this);
			}
		}
	}

	private void RetrieveIsoImages()
	{
		Object tempVar = getSelectedItem();
		VM vm = (VM)((tempVar instanceof VM) ? tempVar : null);
		if (vm == null)
		{
			return;
		}

		Guid storagePoolId = vm.getstorage_pool_id();

		getIsoImages().clear();

		ChangeCDModel tempVar2 = new ChangeCDModel();
		tempVar2.setTitle(ConsoleModel.EjectLabel);
		ChangeCDModel ejectModel = tempVar2;
		ejectModel.getExecutedEvent().addListener(this);
		getIsoImages().add(ejectModel);

		java.util.ArrayList<String> list = DataProvider.GetIrsImageList(storagePoolId, false);
		if (list.size() > 0)
		{
			for (String iso : list)
			{
				ChangeCDModel tempVar3 = new ChangeCDModel();
				tempVar3.setTitle(iso);
				ChangeCDModel model = tempVar3;
				//model.Executed += changeCD;
				model.getExecutedEvent().addListener(this);
				getIsoImages().add(model);
			}

		}
		else
		{
			ChangeCDModel tempVar4 = new ChangeCDModel();
			tempVar4.setTitle("No CDs");
			getIsoImages().add(tempVar4);
		}


	}

	private void changeCD(Object sender, EventArgs e)
	{
		ChangeCDModel model = (ChangeCDModel)sender;

		//TODO: Patch!
		String isoName = model.getTitle();
		if (StringHelper.stringsEqual(isoName, "No CDs"))
		{
			return;
		}

		Object tempVar = getSelectedItem();
		VM vm = (VM)((tempVar instanceof VM) ? tempVar : null);
		if (vm == null)
		{
			return;
		}


		Frontend.RunMultipleAction(VdcActionType.ChangeDisk, new java.util.ArrayList<VdcActionParametersBase>(java.util.Arrays.asList(new VdcActionParametersBase[] { new ChangeDiskCommandParameters(vm.getId(), StringHelper.stringsEqual(isoName, ConsoleModel.EjectLabel) ? "" : isoName) })),
		new IFrontendMultipleActionAsyncCallback() {
			@Override
			public void Executed(FrontendMultipleActionAsyncResult  result) {


			}
		}, null);
	}

	public void Cancel()
	{
		Frontend.Unsubscribe();

		CancelConfirmation();

		setGuideContext(null);
		setWindow(null);

		UpdateActionAvailability();
	}

	private void CancelConfirmation()
	{
		setConfirmWindow(null);
	}

	public void CancelError()
	{
		setErrorWindow(null);
	}

	@Override
	protected void OnSelectedItemChanged()
	{
		super.OnSelectedItemChanged();

		UpdateActionAvailability();
		UpdateConsoleModels();
	}

	@Override
	protected void SelectedItemsChanged()
	{
		super.SelectedItemsChanged();

		UpdateActionAvailability();
		UpdateConsoleModels();
	}

	@Override
	protected void SelectedItemPropertyChanged(Object sender, PropertyChangedEventArgs e)
	{
		super.SelectedItemPropertyChanged(sender, e);

//C# TO JAVA CONVERTER NOTE: The following 'switch' operated on a string member and was converted to Java 'if-else' logic:
//		switch (e.PropertyName)
//ORIGINAL LINE: case "status":
		if (e.PropertyName.equals("status"))
		{
				UpdateActionAvailability();

		}
//ORIGINAL LINE: case "display_type":
		else if (e.PropertyName.equals("display_type"))
		{
				UpdateConsoleModels();
		}
	}

	private void UpdateActionAvailability()
	{
		java.util.List items = getSelectedItems() != null && getSelectedItem() != null ? getSelectedItems() : new java.util.ArrayList();

		getEditCommand().setIsExecutionAllowed(items.size() == 1);
		getRemoveCommand().setIsExecutionAllowed(items.size() > 0 && VdcActionUtils.CanExecute(items, VM.class, VdcActionType.RemoveVm));
		getRunCommand().setIsExecutionAllowed(items.size() > 0 && VdcActionUtils.CanExecute(items, VM.class, VdcActionType.RunVm));
		getPauseCommand().setIsExecutionAllowed(items.size() > 0 && VdcActionUtils.CanExecute(items, VM.class, VdcActionType.HibernateVm));
		getShutdownCommand().setIsExecutionAllowed(items.size() > 0 && VdcActionUtils.CanExecute(items, VM.class, VdcActionType.ShutdownVm));
		getStopCommand().setIsExecutionAllowed(items.size() > 0 && VdcActionUtils.CanExecute(items, VM.class, VdcActionType.StopVm));
		getMigrateCommand().setIsExecutionAllowed(items.size() > 0 && VdcActionUtils.CanExecute(items, VM.class, VdcActionType.MigrateVm));
		getNewTemplateCommand().setIsExecutionAllowed(items.size() == 1 && VdcActionUtils.CanExecute(items, VM.class, VdcActionType.AddVmTemplate));
		getRunOnceCommand().setIsExecutionAllowed(items.size() == 1 && VdcActionUtils.CanExecute(items, VM.class, VdcActionType.RunVmOnce));
		getExportCommand().setIsExecutionAllowed(items.size() > 0 && VdcActionUtils.CanExecute(items, VM.class, VdcActionType.ExportVm));
		getMoveCommand().setIsExecutionAllowed(items.size() == 1 && VdcActionUtils.CanExecute(items, VM.class, VdcActionType.MoveVm));
		getRetrieveIsoImagesCommand().setIsExecutionAllowed(items.size() == 1 && VdcActionUtils.CanExecute(items, VM.class, VdcActionType.ChangeDisk));
		getAssignTagsCommand().setIsExecutionAllowed(items.size() > 0);

		getGuideCommand().setIsExecutionAllowed(getGuideContext() != null || (getSelectedItem() != null && getSelectedItems() != null && getSelectedItems().size() == 1));
	}

	@Override
	public void eventRaised(Event ev, Object sender, EventArgs args)
	{
		super.eventRaised(ev, sender, args);

		if (ev.equals(ChangeCDModel.ExecutedEventDefinition))
		{
			changeCD(sender, args);
		}
		else if (ev.equals(ConsoleModel.ErrorEventDefinition) && sender instanceof SpiceConsoleModel)
		{
			SpiceConsoleModel_Error(sender, (ErrorCodeEventArgs)args);
		}
	}

	private void SpiceConsoleModel_Error(Object sender, ErrorCodeEventArgs e)
	{
		ResourceManager rm = new ResourceManager("UICommon.Resources.RdpErrors.RdpErrors", Assembly.GetExecutingAssembly());

		ConfirmationModel model = new ConfirmationModel();
		if (getErrorWindow() == null)
		{
			setErrorWindow(model);
		}
		model.setTitle("Console Disconnected");
		model.setHashName("console_disconnected");
		model.setMessage(StringFormat.format("Error connecting to Virtual Machine using Spice:\n%1$s", rm.GetString("E" + e.getErrorCode())));

		rm.ReleaseAllResources();


		UICommand tempVar = new UICommand("CancelError", this);
		tempVar.setTitle("Close");
		tempVar.setIsDefault(true);
		tempVar.setIsCancel(true);
		model.getCommands().add(tempVar);
	}

	@Override
	public void ExecuteCommand(UICommand command)
	{
		super.ExecuteCommand(command);

		if (command == getNewServerCommand())
		{
			NewServer();
		}
		else if (command == getNewDesktopCommand())
		{
			NewDesktop();
		}
		else if (command == getEditCommand())
		{
			Edit();
		}
		else if (command == getRemoveCommand())
		{
			remove();
		}
		else if (command == getRunCommand())
		{
			Run();
		}
		else if (command == getPauseCommand())
		{
			Pause();
		}
		else if (command == getStopCommand())
		{
			stop();
		}
		else if (command == getShutdownCommand())
		{
			Shutdown();
		}
		else if (command == getMigrateCommand())
		{
			Migrate();
		}
		else if (command == getNewTemplateCommand())
		{
			NewTemplate();
		}
		else if (command == getRunOnceCommand())
		{
			RunOnce();
		}
		else if (command == getExportCommand())
		{
			Export();
		}
		else if (command == getMoveCommand())
		{
			Move();
		}
		else if (command == getGuideCommand())
		{
			Guide();
		}
		else if (command == getRetrieveIsoImagesCommand())
		{
			RetrieveIsoImages();
		}
		else if (command == getAssignTagsCommand())
		{
			AssignTags();
		}
		else if (StringHelper.stringsEqual(command.getName(), "OnAssignTags"))
		{
			OnAssignTags();
		}
		else if (StringHelper.stringsEqual(command.getName(), "Cancel"))
		{
			Cancel();
		}
		else if (StringHelper.stringsEqual(command.getName(), "OnSave"))
		{
			OnSave();
		}
		else if (StringHelper.stringsEqual(command.getName(), "OnRemove"))
		{
			OnRemove();
		}
		else if (StringHelper.stringsEqual(command.getName(), "OnMove"))
		{
			OnMove();
		}
		else if (StringHelper.stringsEqual(command.getName(), "OnExport"))
		{
			OnExport();
		}
		else if (StringHelper.stringsEqual(command.getName(), "OnExportNoTemplates"))
		{
			OnExportNoTemplates();
		}
		else if (StringHelper.stringsEqual(command.getName(), "CancelConfirmation"))
		{
			CancelConfirmation();
		}
		else if (StringHelper.stringsEqual(command.getName(), "CancelError"))
		{
			CancelError();
		}
		else if (StringHelper.stringsEqual(command.getName(), "OnRunOnce"))
		{
			OnRunOnce();
		}
		else if (StringHelper.stringsEqual(command.getName(), "OnNewTemplate"))
		{
			OnNewTemplate();
		}
		else if (StringHelper.stringsEqual(command.getName(), "OnMigrate"))
		{
			OnMigrate();
		}
		else if (StringHelper.stringsEqual(command.getName(), "OnShutdown"))
		{
			OnShutdown();
		}
		else if (StringHelper.stringsEqual(command.getName(), "OnStop"))
		{
			OnStop();
		}
	}


	private SystemTreeItemModel systemTreeSelectedItem;
	public SystemTreeItemModel getSystemTreeSelectedItem()
	{
		return systemTreeSelectedItem;
	}
	public void setSystemTreeSelectedItem(SystemTreeItemModel value)
	{
		systemTreeSelectedItem = value;
		OnPropertyChanged(new PropertyChangedEventArgs("SystemTreeSelectedItem"));
	}

}
