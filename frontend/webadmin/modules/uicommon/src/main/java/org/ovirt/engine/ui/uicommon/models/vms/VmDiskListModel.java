package org.ovirt.engine.ui.uicommon.models.vms;

import java.util.ArrayList;
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

import org.ovirt.engine.ui.uicommon.dataprovider.*;
import org.ovirt.engine.ui.uicompat.*;
import org.ovirt.engine.core.common.interfaces.*;
import org.ovirt.engine.core.common.queries.*;
import org.ovirt.engine.core.common.businessentities.*;

import org.ovirt.engine.ui.uicommon.*;
import org.ovirt.engine.ui.uicommon.models.*;

@SuppressWarnings("unused")
public class VmDiskListModel extends SearchableListModel
{

	private UICommand privateNewCommand;
	public UICommand getNewCommand()
	{
		return privateNewCommand;
	}
	private void setNewCommand(UICommand value)
	{
		privateNewCommand = value;
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


	public VmDiskListModel()
	{
		setTitle("Virtual Disks");

		setNewCommand(new UICommand("New", this));
		setEditCommand(new UICommand("Edit", this));
		setRemoveCommand(new UICommand("Remove", this));

		UpdateActionAvailability();
	}

	@Override
	protected void OnEntityChanged()
	{
		super.OnEntityChanged();

		if (getEntity() != null)
		{
			getSearchCommand().Execute();
		}

		UpdateActionAvailability();
	}

	@Override
	protected void SyncSearch()
	{
		VM vm = (VM)getEntity();

		super.SyncSearch(VdcQueryType.GetAllDisksByVmId, new GetAllDisksByVmIdParameters(vm.getId()));
	}

	@Override
	protected void AsyncSearch()
	{
		super.AsyncSearch();

		VM vm = (VM)getEntity();

		setAsyncResult(Frontend.RegisterQuery(VdcQueryType.GetAllDisksByVmId, new GetAllDisksByVmIdParameters(vm.getId())));
		setItems(getAsyncResult().getData());
	}

	private void New()
	{
		VM vm = (VM)getEntity();

		if (getWindow() != null)
		{
			return;
		}

		DiskModel model = new DiskModel();
		setWindow(model);
		model.setTitle("New Virtual Disk");
		model.setHashName("new_virtual_disk");
		model.setIsNew(true);
		AsyncQuery _asyncQuery1 = new AsyncQuery();
		_asyncQuery1.setModel(this);
		_asyncQuery1.asyncCallback = new INewAsyncCallback() { public void OnSuccess(Object model1, Object result1)
										{
											VmDiskListModel vmDiskListModel = (VmDiskListModel)model1;
											DiskModel diskModel = (DiskModel)vmDiskListModel.getWindow();
											java.util.ArrayList<DiskImage> disks = getItems() != null ? Linq.<DiskImage>Cast(getItems()) : new java.util.ArrayList<DiskImage>();
											boolean hasDisks = disks.size() > 0;
											java.util.ArrayList<storage_domains> storageDomains = new java.util.ArrayList<storage_domains>();
											for (storage_domains a : (java.util.ArrayList<storage_domains>)result1)
											{
												if (a.getstorage_domain_type() != StorageDomainType.ISO && a.getstorage_domain_type() != StorageDomainType.ImportExport && a.getstatus() == StorageDomainStatus.Active)
												{
													storageDomains.add(a);
												}
											}

											diskModel.getStorageDomain().setItems(storageDomains);
											diskModel.getStorageDomain().setIsAvailable(!hasDisks);

											if (hasDisks)
											{
												// the StorageDomain value should be the one that all other Disks are on
												// (although this field is not-available, we use its value in the 'OnSave' method):
												AsyncQuery _asyncQuery2 = new AsyncQuery();
												_asyncQuery2.setModel(model1);
												_asyncQuery2.asyncCallback = new INewAsyncCallback() { public void OnSuccess(Object model2, Object result2)
												{
													VmDiskListModel vmDiskListModel2 = (VmDiskListModel)model2;
													DiskModel diskModel2 = (DiskModel)vmDiskListModel2.getWindow();
													java.util.ArrayList<storage_domains> storageDomains2 = (java.util.ArrayList<storage_domains>)diskModel2.getStorageDomain().getItems();
													storage_domains storage2 = (storage_domains)result2;
													vmDiskListModel2.StepA(storage2 != null && Linq.IsSDItemExistInList(storageDomains2, storage2.getId()) ? storage2 : null);
												}};
												AsyncDataProvider.GetStorageDomainById(_asyncQuery2, disks.get(0).getstorage_ids().get(0));
											}
											else // first disk -> just choose the first from the list of available storage-domains:
											{
												vmDiskListModel.StepA(Linq.FirstOrDefault(storageDomains));
											}
										}};
		AsyncDataProvider.GetStorageDomainList(_asyncQuery1, vm.getstorage_pool_id());
	}

	private void Edit()
	{
		DiskImage disk = (DiskImage)getSelectedItem();

		if (getWindow() != null)
		{
			return;
		}

		DiskModel model = new DiskModel();
		setWindow(model);
		model.setTitle("Edit Virtual Disk");
		model.setHashName("edit_virtual_disk");
		model.getStorageDomain().setIsAvailable(false);
		model.getSize().setEntity(disk.getSizeInGigabytes());
		model.getSize().setIsChangable(false);
		AsyncQuery _asyncQuery1 = new AsyncQuery();
		_asyncQuery1.setModel(this);
		_asyncQuery1.asyncCallback = new INewAsyncCallback() { public void OnSuccess(Object model1, Object result1)
					  {
						  VmDiskListModel vmDiskListModel1 = (VmDiskListModel)model1;
						  DiskModel diskModel1 = (DiskModel)vmDiskListModel1.getWindow();
						  storage_domains storageDomain1 = (storage_domains)result1;
						  diskModel1.getStorageDomain().setSelectedItem(storageDomain1);
						  AsyncQuery _asyncQuery2 = new AsyncQuery();
						  _asyncQuery2.setModel(model1);
						  _asyncQuery2.asyncCallback = new INewAsyncCallback() { public void OnSuccess(Object model2, Object result2)
														{
															VmDiskListModel vmDiskListModel2 = (VmDiskListModel)model2;
															DiskModel vmModel2 = (DiskModel)vmDiskListModel2.getWindow();
															VM vm2 = (VM)vmDiskListModel2.getEntity();

															DiskImage disk2 = (DiskImage)vmDiskListModel2.getSelectedItem();
															java.util.ArrayList<DiskImage> disks = vmDiskListModel2.getItems() != null ? Linq.<DiskImage>Cast(vmDiskListModel2.getItems()) : new java.util.ArrayList<DiskImage>();
															java.util.ArrayList<DiskImageBase> presets = (java.util.ArrayList<DiskImageBase>)result2;
															
											                DiskImageBase preset = new DiskImage();
											                vmModel2.getPreset().setSelectedItem(preset);
															vmModel2.getPreset().setIsChangable(false);

															vmModel2.getVolumeType().setSelectedItem(disk2.getvolume_type());
															vmModel2.getVolumeType().setIsChangable(false);

															vmModel2.setVolumeFormat(disk2.getvolume_format());

															java.util.ArrayList<DiskInterface> interfaces = DataProvider.GetDiskInterfaceList(vm2.getvm_os(), vm2.getvds_group_compatibility_version());
															if (!interfaces.contains(disk2.getdisk_interface()))
															{
																interfaces.add(disk2.getdisk_interface());
															}
															vmModel2.getInterface().setItems(interfaces);
															vmModel2.getInterface().setSelectedItem(disk2.getdisk_interface());
															vmModel2.getInterface().setIsChangable(false);

															storage_domains storage = (storage_domains)vmModel2.getStorageDomain().getSelectedItem();

															vmModel2.getWipeAfterDelete().setEntity(disk2.getwipe_after_delete());
															if (vmModel2.getStorageDomain() != null && vmModel2.getStorageDomain().getSelectedItem() != null)
															{
																vmDiskListModel2.UpdateWipeAfterDelete(storage.getstorage_type(), vmModel2.getWipeAfterDelete(), false);
															}

															DiskImage bootableDisk = null;
															for (DiskImage a : disks)
															{
																if (a.getboot())
																{
																	bootableDisk = a;
																	break;
																}
															}
															if (bootableDisk != null && !bootableDisk.getId().equals(disk2.getId()))
															{
																vmModel2.getIsBootable().setIsChangable(false);
																vmModel2.getIsBootable().getChangeProhibitionReasons().add("There can be only one bootable disk defined.");
															}
															vmModel2.getIsBootable().setEntity(disk2.getboot());


															UICommand tempVar = new UICommand("OnSave", vmDiskListModel2);
															tempVar.setTitle("OK");
															tempVar.setIsDefault(true);
															vmModel2.getCommands().add(tempVar);
															UICommand tempVar2 = new UICommand("Cancel", vmDiskListModel2);
															tempVar2.setTitle("Cancel");
															tempVar2.setIsCancel(true);
															vmModel2.getCommands().add(tempVar2);

														}};
						  AsyncDataProvider.GetDiskPresetList(_asyncQuery2, ((VM)vmDiskListModel1.getEntity()).getvm_type(), diskModel1.getStorageDomain().getSelectedItem() == null ? StorageType.UNKNOWN : storageDomain1.getstorage_type());


					  }};
		AsyncDataProvider.GetStorageDomainById(_asyncQuery1, disk.getstorage_ids().get(0));
	}

	private void remove()
	{
		if (getWindow() != null)
		{
			return;
		}

		boolean hasSystemDiskWarning = false;
		ConfirmationModel model = new ConfirmationModel();
		setWindow(model);
		model.setTitle("Remove Disk(s)");
		model.setHashName("remove_disk");
		model.setMessage("Disk(s)");

		java.util.ArrayList<String> items = new java.util.ArrayList<String>();
		for (Object item : getSelectedItems())
		{
			DiskImage a = (DiskImage)item;
            items.add(StringFormat.format("Disk %1$s", a.getinternal_drive_mapping()));
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
        VM vm = (VM) getEntity();
        ConfirmationModel model = (ConfirmationModel) getWindow();

        ArrayList<Guid> images = new ArrayList<Guid>();
        ArrayList<VdcActionParametersBase> paramerterList = new ArrayList<VdcActionParametersBase>();

        for (Object item : getSelectedItems())
        {
            DiskImage a = (DiskImage) item;
            RemoveDiskParameters parameters = new RemoveDiskParameters(a.getId());
            paramerterList.add(parameters);
        }

        model.StartProgress(null);

        Frontend.RunMultipleAction(VdcActionType.RemoveDisk, paramerterList,
                new IFrontendMultipleActionAsyncCallback() {
                    @Override
                    public void Executed(FrontendMultipleActionAsyncResult result) {
                        VmDiskListModel localModel = (VmDiskListModel) result.getState();
                        localModel.StopProgress();
                        Cancel();
                    }
                },
                this);
	}

	private void OnSave()
	{
		VM vm = (VM)getEntity();
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

		DiskImage disk = model.getIsNew() ? new DiskImage() : (DiskImage)getSelectedItem();
		disk.setSizeInGigabytes(Integer.parseInt(model.getSize().getEntity().toString()));
		disk.setdisk_interface((DiskInterface)model.getInterface().getSelectedItem());
		disk.setvolume_type((VolumeType)model.getVolumeType().getSelectedItem());
		disk.setvolume_format(model.getVolumeFormat());
		disk.setwipe_after_delete((Boolean)model.getWipeAfterDelete().getEntity());
		disk.setboot((Boolean)model.getIsBootable().getEntity());

		//NOTE: Since we doesn't support partial snapshots in GUI, propagate errors flag always must be set false.
		//disk.propagate_errors = model.PropagateErrors.ValueAsBoolean() ? PropagateErrors.On : PropagateErrors.Off;
		disk.setpropagate_errors(PropagateErrors.Off);


		model.StartProgress(null);

		if (model.getIsNew())
		{
			AddDiskToVmParameters tempVar = new AddDiskToVmParameters(vm.getId(), disk);
			tempVar.setStorageDomainId(storageDomain.getId());
			Frontend.RunAction(VdcActionType.AddDiskToVm, tempVar,
		new IFrontendActionAsyncCallback() {
			@Override
			public void Executed(FrontendActionAsyncResult  result) {

				VmDiskListModel localModel = (VmDiskListModel)result.getState();
				localModel.PostOnSaveInternal(result.getReturnValue());

			}
		}, this);
		}
		else
		{
			Frontend.RunAction(VdcActionType.UpdateVmDisk, new UpdateVmDiskParameters(vm.getId(), disk.getId(), disk),
		new IFrontendActionAsyncCallback() {
			@Override
			public void Executed(FrontendActionAsyncResult  result) {

				VmDiskListModel localModel = (VmDiskListModel)result.getState();
				localModel.PostOnSaveInternal(result.getReturnValue());

			}
		}, this);
		}
	}

	public void PostOnSaveInternal(VdcReturnValueBase returnValue)
	{
		DiskModel model = (DiskModel)getWindow();

		model.StopProgress();

		if (returnValue != null && returnValue.getSucceeded())
		{
			Cancel();
		}
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
	protected void EntityPropertyChanged(Object sender, PropertyChangedEventArgs e)
	{
		super.EntityPropertyChanged(sender, e);

		if (e.PropertyName.equals("status"))
		{
			UpdateActionAvailability();
		}
	}

	private void UpdateActionAvailability()
	{
		VM vm = (VM)getEntity();
		boolean isDown = vm != null && vm.getstatus() == VMStatus.Down;

		getNewCommand().setIsExecutionAllowed(isDown);

		getEditCommand().setIsExecutionAllowed(getSelectedItem() != null && getSelectedItems() != null && getSelectedItems().size() == 1 && isDown);

		getRemoveCommand().setIsExecutionAllowed(getSelectedItems() != null && getSelectedItems().size() > 0 && isDown);
	}

	@Override
	public void ExecuteCommand(UICommand command)
	{
		super.ExecuteCommand(command);

		if (command == getNewCommand())
		{
			New();
		}
		else if (command == getEditCommand())
		{
			Edit();
		}
		else if (command == getRemoveCommand())
		{
			remove();
		}
		else if (StringHelper.stringsEqual(command.getName(), "OnSave"))
		{
			OnSave();
		}
		else if (StringHelper.stringsEqual(command.getName(), "Cancel"))
		{
			Cancel();
		}
		else if (StringHelper.stringsEqual(command.getName(), "OnRemove"))
		{
			OnRemove();
		}
	}

	public void StepA(storage_domains storage)
	{
		DiskModel model = (DiskModel)getWindow();
		VM vm = (VM)getEntity();

		model.getStorageDomain().setSelectedItem(storage);

		if (storage != null)
		{
			UpdateWipeAfterDelete(storage.getstorage_type(), model.getWipeAfterDelete(), true);
		}

		AsyncQuery _asyncQuery1 = new AsyncQuery();
		_asyncQuery1.setModel(this);
		_asyncQuery1.asyncCallback = new INewAsyncCallback() { public void OnSuccess(Object model1, Object result1)
										{
											VmDiskListModel vmDiskListModel1 = (VmDiskListModel)model1;
											DiskModel vmModel = (DiskModel)vmDiskListModel1.getWindow();
											VM vm1 = (VM)vmDiskListModel1.getEntity();

											java.util.ArrayList<DiskImage> disks = vmDiskListModel1.getItems() != null ? Linq.<DiskImage>Cast(vmDiskListModel1.getItems()) : new java.util.ArrayList<DiskImage>();
											boolean hasDisks = disks.size() > 0;
											storage_domains storage1 = (storage_domains)vmModel.getStorageDomain().getSelectedItem();

											java.util.ArrayList<DiskImageBase> presets = (java.util.ArrayList<DiskImageBase>)result1;
											vmModel.getPreset().setItems(presets);
											vmModel.getPreset().setSelectedItem(null);
											
							                for (DiskImageBase a : presets)
							                {
							                    if ((hasDisks && !a.getboot()) || (!hasDisks && a.getboot()))
							                    {
							                        vmModel.getPreset().setSelectedItem(a);
							                        break;
							                    }
							                }
											
											vmModel.getInterface().setItems(DataProvider.GetDiskInterfaceList(vm1.getvm_os(), vm1.getvds_group_compatibility_version()));
											vmModel.getInterface().setSelectedItem(DataProvider.GetDefaultDiskInterface(vm1.getvm_os(), disks));


											boolean hasBootableDisk = false;
											for (DiskImage a : disks)
											{
												if (a.getboot())
												{
													hasBootableDisk = true;
													break;
												}
											}

											vmModel.getIsBootable().setEntity(!hasBootableDisk);
											if (hasBootableDisk)
											{
												vmModel.getIsBootable().setIsChangable(false);
												vmModel.getIsBootable().getChangeProhibitionReasons().add("There can be only one bootable disk defined.");
											}

											java.util.ArrayList<UICommand> commands = new java.util.ArrayList<UICommand>();

											if (storage1 == null)
											{
												String cantCreateMessage = "There is no active Storage Domain to create the Disk in. Please activate a Storage Domain.";
												if (hasDisks)
												{
													cantCreateMessage = "Error in retrieving the relevant Storage Domain.";
													//if (storage.storage_name != null)
													//{
													//    cantCreateMessage = StringFormat.format("'{0}' Storage Domain is not active. Please activate it.", storage.storage_name);
													//}
												}

												vmModel.setMessage(cantCreateMessage);

												UICommand tempVar = new UICommand("Cancel", vmDiskListModel1);
												tempVar.setTitle("Close");
												tempVar.setIsDefault(true);
												tempVar.setIsCancel(true);
												vmModel.getCommands().add(tempVar);
											}
											else
											{
												UICommand tempVar2 = new UICommand("OnSave", vmDiskListModel1);
												tempVar2.setTitle("OK");
												tempVar2.setIsDefault(true);
												vmModel.getCommands().add(tempVar2);

												UICommand tempVar3 = new UICommand("Cancel", vmDiskListModel1);
												tempVar3.setTitle("Cancel");
												tempVar3.setIsCancel(true);
												vmModel.getCommands().add(tempVar3);
											}

										}};
		AsyncDataProvider.GetDiskPresetList(_asyncQuery1, vm.getvm_type(), model.getStorageDomain().getSelectedItem() == null ? StorageType.UNKNOWN : storage.getstorage_type());
	}

	private void UpdateWipeAfterDelete(StorageType storageType, EntityModel wipeAfterDeleteModel, boolean isNew)
	{
		if (storageType == StorageType.NFS || storageType == StorageType.LOCALFS)
		{
			wipeAfterDeleteModel.setIsChangable(false);
		}
		else
		{
			wipeAfterDeleteModel.setIsChangable(true);
			if (isNew)
			{
				AsyncQuery _asyncQuery = new AsyncQuery();
				_asyncQuery.setModel(getWindow());
				_asyncQuery.asyncCallback = new INewAsyncCallback() { public void OnSuccess(Object model, Object result)
											  {
												  DiskModel diskModel = (DiskModel)model;
												  diskModel.getWipeAfterDelete().setEntity(result);
											  }};
				AsyncDataProvider.GetSANWipeAfterDelete(_asyncQuery);
			}
		}
	}
}