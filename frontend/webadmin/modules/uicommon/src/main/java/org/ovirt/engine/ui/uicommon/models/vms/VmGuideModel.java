package org.ovirt.engine.ui.uicommon.models.vms;
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

import org.ovirt.engine.ui.uicompat.*;
import org.ovirt.engine.ui.uicommon.*;
import org.ovirt.engine.ui.uicommon.models.*;

@SuppressWarnings("unused")
public class VmGuideModel extends GuideModel
{
	public final String VmConfigureNetworkInterfacesAction = "Configure Network Interfaces";
	public final String VmAddAnotherNetworkInterfaceAction = "Add another Network Interface";
	public final String VmConfigureVirtualDisksAction = "Configure Virtual Disks";
	public final String VmAddAnotherVirtualDiskAction = "Add another Virtual Disk";


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

	public VM getEntity()
	{
		return (VM)super.getEntity();
	}
	public void setEntity(VM value)
	{
		super.setEntity(value);
	}


	@Override
	protected void OnEntityChanged()
	{
		super.OnEntityChanged();
		UpdateOptions();
	}

	private void UpdateOptions()
	{
		getCompulsoryActions().clear();
		getOptionalActions().clear();

		if (getEntity() != null)
		{
			//Add NIC action.
			UICommand addNicAction = new UICommand("AddNetwork", this);

			java.util.ArrayList<VmNetworkInterface> nics = DataProvider.GetVmNicList(getEntity().getId());
			if (nics.isEmpty())
			{
				addNicAction.setTitle(VmConfigureNetworkInterfacesAction);
				getCompulsoryActions().add(addNicAction);
			}
			else
			{
				addNicAction.setTitle(VmAddAnotherNetworkInterfaceAction);
				getOptionalActions().add(addNicAction);
			}


			//Add disk action.
			UICommand addDiskAction = new UICommand("AddDisk", this);

			java.util.ArrayList<DiskImage> disks = DataProvider.GetVmDiskList(getEntity().getId());
			if (disks.isEmpty())
			{
				addDiskAction.setTitle(VmConfigureVirtualDisksAction);
				getCompulsoryActions().add(addDiskAction);
			}
			else
			{
				//                    if (!(Entity.vm_os == VmOsType.WindowsXP && disks.Count(a => a.disk_interface == DiskInterface.IDE) > 2))
				int ideDiskCount = 0;
				for (DiskImage a : disks)
				{
					if (a.getdisk_interface() == DiskInterface.IDE)
					{
						ideDiskCount++;
					}

				}
				if (!(getEntity().getvm_os() == VmOsType.WindowsXP && ideDiskCount > 2))
				{
					addDiskAction.setTitle(VmAddAnotherVirtualDiskAction);
					getOptionalActions().add(addDiskAction);
				}
			}
		}
	}

	public void AddNetwork()
	{
		if (getEntity() != null)
		{
			java.util.ArrayList<VmNetworkInterface> nics = DataProvider.GetVmNicList(getEntity().getId());
			int nicCount = nics.size();
			String newNicName = DataProvider.GetNewNicName(nics);

			//var networks = DataProvider.GetNetworkList(Entity.vds_group_id)
			//    .Where(a => a.Status == NetworkStatus.Operational)
			//    .ToList();
			java.util.ArrayList<network> networks = new java.util.ArrayList<network>();
			for (network a : DataProvider.GetClusterNetworkList(getEntity().getvds_group_id()))
			{
				if (a.getStatus() == NetworkStatus.Operational)
				{
					networks.add(a);
				}
			}

			VmInterfaceModel model = new VmInterfaceModel();
			setWindow(model);
			model.setTitle("New Network Interface");
			model.setHashName("new_network_interface_vms_guide");
			model.setIsNew(true);
			model.getNetwork().setItems(networks);
			model.getNetwork().setSelectedItem(networks.size() > 0 ? networks.get(0) : null);
			model.getNicType().setItems(DataProvider.GetNicTypeList(getEntity().getvm_os(), false));
			model.getNicType().setSelectedItem(DataProvider.GetDefaultNicType(getEntity().getvm_os()));
			model.getName().setEntity(newNicName);
			model.getMAC().setIsChangable(false);


			UICommand tempVar = new UICommand("OnAddNetwork", this);
			tempVar.setTitle("OK");
			tempVar.setIsDefault(true);
			model.getCommands().add(tempVar);
			UICommand tempVar2 = new UICommand("Cancel", this);
			tempVar2.setTitle("Cancel");
			tempVar2.setIsCancel(true);
			model.getCommands().add(tempVar2);
		}
	}

	private void OnAddNetwork()
	{
		if (getEntity() != null)
		{
			VmInterfaceModel model = (VmInterfaceModel)getWindow();

			if (model.getProgress() != null)
			{
				return;
			}

			if (!model.Validate())
			{
				return;
			}

			//Save changes.
			Integer _type;
			if(model.getNicType().getSelectedItem() == null)
			{
				_type = null;
			}
			else
			{
				_type = ((VmInterfaceType) model.getNicType().getSelectedItem()).getValue();
			}

			VmNetworkInterface vmNetworkInterface = new VmNetworkInterface();
			vmNetworkInterface.setName((String)model.getName().getEntity());
			vmNetworkInterface.setNetworkName(((network)model.getNetwork().getSelectedItem()).getname());
			vmNetworkInterface.setType(_type);
			vmNetworkInterface.setMacAddress(model.getMAC().getIsChangable() ? (model.getMAC().getEntity() == null ? null : ((String)(model.getMAC().getEntity())).toLowerCase()) : "");

			AddVmInterfaceParameters parameters = new AddVmInterfaceParameters(getEntity().getId(), vmNetworkInterface);

			model.StartProgress(null);

			Frontend.RunAction(VdcActionType.AddVmInterface, parameters,
		new IFrontendActionAsyncCallback() {
			@Override
			public void Executed(FrontendActionAsyncResult  result) {

				VmGuideModel vmGuideModel = (VmGuideModel)result.getState();
				vmGuideModel.getWindow().StopProgress();
				VdcReturnValueBase returnValueBase = result.getReturnValue();
				if (returnValueBase != null && returnValueBase.getSucceeded())
				{
					vmGuideModel.Cancel();
					vmGuideModel.PostAction();
				}

			}
		}, this);
		}
		else
		{
			Cancel();
		}
	}

	public void AddDisk()
	{
		if (getEntity() != null)
		{
			java.util.ArrayList<DiskImage> disks = DataProvider.GetVmDiskList(getEntity().getId());
			boolean hasDisks = disks.size() > 0;

			DiskModel model = new DiskModel();
			setWindow(model);
			model.setTitle("New Virtual Disk");
			model.setHashName("new_virtual_disk");
			model.setIsNew(true);

			//var storageDomains = DataProvider.GetStorageDomainList(Entity.storage_pool_id)
			//    .Where(a => a.storage_domain_type != StorageDomainType.ISO && a.storage_domain_type != StorageDomainType.ImportExport
			//    && a.status == StorageDomainStatus.Active);

			java.util.ArrayList<storage_domains> storageDomains = new java.util.ArrayList<storage_domains>();
			for (storage_domains a : DataProvider.GetStorageDomainList(getEntity().getstorage_pool_id()))
			{
				if (a.getstorage_domain_type() != StorageDomainType.ISO && a.getstorage_domain_type() != StorageDomainType.ImportExport && a.getstatus() == StorageDomainStatus.Active)
				{
					storageDomains.add(a);
				}
			}
			model.getStorageDomain().setItems(storageDomains);

			storage_domains storage = null;
			boolean storage_available = false;

			if (hasDisks)
			{
				// the StorageDomain value should be the one that all other Disks are on
				// (although this field is not-available, we use its value in the 'OnSave' method):
				storage = DataProvider.GetStorageDomainByDiskList(disks);
				if (storage != null && Linq.IsSDItemExistInList(storageDomains, storage.getId()))
				{
					storage_available = true;
				}
			}
			else // first disk -> just choose the first from the list of available storage-domains:
			{
				storage = Linq.<storage_domains>FirstOrDefault(storageDomains);
				storage_available = true;
			}

			model.getStorageDomain().setSelectedItem(storage);
			model.getStorageDomain().setIsAvailable(!hasDisks);

			if (model.getStorageDomain() != null && model.getStorageDomain().getSelectedItem() != null)
			{
				DataProvider.GetWipeAfterDeleteDefaultsByStorageType(((storage_domains)model.getStorageDomain().getSelectedItem()).getstorage_type(), model.getWipeAfterDelete(), true);
			}

			java.util.ArrayList<DiskImageBase> presets = DataProvider.GetDiskPresetList(getEntity().getvm_type(), model.getStorageDomain().getSelectedItem() == null ? StorageType.UNKNOWN : storage.getstorage_type());

			model.getPreset().setItems(presets);
			//model.Preset.SelectedItem = hasDisks
			//    ? presets.FirstOrDefault(a => a.disk_type == DiskType.Data)
			//    : presets.FirstOrDefault(a => a.disk_type == DiskType.System);

			model.getPreset().setSelectedItem(null);
			model.getInterface().setItems(DataProvider.GetDiskInterfaceList(getEntity().getvm_os(), DataProvider.GetClusterById(getEntity().getvds_group_id()).getcompatibility_version()));
			model.getInterface().setSelectedItem(DataProvider.GetDefaultDiskInterface(getEntity().getvm_os(), disks));

			//			bool hasBootableDisk = disks.Any(a => a.boot);
			boolean hasBootableDisk = false;
			for (DiskImage a : disks)
			{
				if (a.getboot())
				{
					hasBootableDisk = true;
					break;
				}
			}
			model.getIsBootable().setEntity(!hasBootableDisk);
			if (hasBootableDisk)
			{
				model.getIsBootable().setIsChangable(false);
				model.getIsBootable().getChangeProhibitionReasons().add("There can be only one bootable disk defined.");
			}


			if (storage == null || storage_available == false)
			{
				String cantCreateMessage = "There is no active Storage Domain to create the Disk in. Please activate a Storage Domain.";
				if (hasDisks)
				{
					cantCreateMessage = "Error in retrieving the relevant Storage Domain.";
					if (storage != null && storage.getstorage_name() != null)
					{
						cantCreateMessage = StringFormat.format("'%1$s' Storage Domain is not active. Please activate it.", storage.getstorage_name());
					}
				}

				model.setMessage(cantCreateMessage);

				UICommand tempVar = new UICommand("Cancel", this);
				tempVar.setTitle("Close");
				tempVar.setIsDefault(true);
				tempVar.setIsCancel(true);
				model.getCommands().add(tempVar);
			}
			else
			{
				UICommand tempVar2 = new UICommand("OnAddDisk", this);
				tempVar2.setTitle("OK");
				tempVar2.setIsDefault(true);
				model.getCommands().add(tempVar2);

				UICommand tempVar3 = new UICommand("Cancel", this);
				tempVar3.setTitle("Cancel");
				tempVar3.setIsCancel(true);
				model.getCommands().add(tempVar3);
			}
		}
	}

	public void OnAddDisk()
	{
		if (getEntity() != null)
		{
			DiskModel model = (DiskModel)getWindow();

			if (model.getProgress() != null)
			{
				return;
			}

			if (!model.Validate())
			{
				return;
			}

			//Save changes.
			storage_domains storageDomain = (storage_domains)model.getStorageDomain().getSelectedItem();

			DiskImage tempVar = new DiskImage();
			tempVar.setSizeInGigabytes(Integer.parseInt(model.getSize().getEntity().toString()));
			tempVar.setdisk_interface((DiskInterface)model.getInterface().getSelectedItem());
			tempVar.setvolume_type((VolumeType)model.getVolumeType().getSelectedItem());
			tempVar.setvolume_format(model.getVolumeFormat());
			tempVar.setwipe_after_delete((Boolean)model.getWipeAfterDelete().getEntity());
			tempVar.setboot((Boolean)model.getIsBootable().getEntity());
			tempVar.setpropagate_errors(PropagateErrors.Off);
			DiskImage disk = tempVar;

			model.StartProgress(null);

			AddDiskToVmParameters tempVar2 = new AddDiskToVmParameters(getEntity().getId(), disk);
			tempVar2.setStorageDomainId(storageDomain.getId());
			Frontend.RunAction(VdcActionType.AddDiskToVm, tempVar2,
		new IFrontendActionAsyncCallback() {
			@Override
			public void Executed(FrontendActionAsyncResult  result) {

				VmGuideModel vmGuideModel = (VmGuideModel)result.getState();
				vmGuideModel.getWindow().StopProgress();
				VdcReturnValueBase returnValueBase = result.getReturnValue();
				if (returnValueBase != null && returnValueBase.getSucceeded())
				{
					vmGuideModel.Cancel();
					vmGuideModel.PostAction();
				}

			}
		}, this);
		}
		else
		{
			Cancel();
		}
	}

	public void PostAction()
	{
		UpdateOptions();
	}

	public void Cancel()
	{
		setWindow(null);
	}

	@Override
	public void ExecuteCommand(UICommand command)
	{
		super.ExecuteCommand(command);

		if (StringHelper.stringsEqual(command.getName(), "AddNetwork"))
		{
			AddNetwork();
		}
		if (StringHelper.stringsEqual(command.getName(), "AddDisk"))
		{
			AddDisk();
		}
		if (StringHelper.stringsEqual(command.getName(), "OnAddNetwork"))
		{
			OnAddNetwork();
		}
		if (StringHelper.stringsEqual(command.getName(), "OnAddDisk"))
		{
			OnAddDisk();
		}
		if (StringHelper.stringsEqual(command.getName(), "Cancel"))
		{
			Cancel();
		}
	}
}