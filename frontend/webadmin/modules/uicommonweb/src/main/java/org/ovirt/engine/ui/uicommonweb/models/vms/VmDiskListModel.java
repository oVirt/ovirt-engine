package org.ovirt.engine.ui.uicommonweb.models.vms;

import org.ovirt.engine.core.common.action.AddDiskToVmParameters;
import org.ovirt.engine.core.common.action.RemoveDisksFromVmParameters;
import org.ovirt.engine.core.common.action.UpdateVmDiskParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.DiskImageBase;
import org.ovirt.engine.core.common.businessentities.DiskInterface;
import org.ovirt.engine.core.common.businessentities.DiskType;
import org.ovirt.engine.core.common.businessentities.PropagateErrors;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VolumeType;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.queries.GetAllDisksByVmIdParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.core.compat.StringFormat;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.DataProvider;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicompat.FrontendActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;

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
        if (getEntity() == null)
        {
            return;
        }
        VM vm = (VM) getEntity();

        super.SyncSearch(VdcQueryType.GetAllDisksByVmId, new GetAllDisksByVmIdParameters(vm.getvm_guid()));
    }

    @Override
    protected void AsyncSearch()
    {
        super.AsyncSearch();

        VM vm = (VM) getEntity();

        setAsyncResult(Frontend.RegisterQuery(VdcQueryType.GetAllDisksByVmId,
                new GetAllDisksByVmIdParameters(vm.getvm_guid())));
        setItems(getAsyncResult().getData());
    }

    private void New()
    {
        VM vm = (VM) getEntity();

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
        _asyncQuery1.asyncCallback = new INewAsyncCallback() {
            @Override
            public void OnSuccess(Object model1, Object result1)
            {
                VmDiskListModel vmDiskListModel = (VmDiskListModel) model1;
                DiskModel diskModel = (DiskModel) vmDiskListModel.getWindow();
                java.util.ArrayList<DiskImage> disks =
                        getItems() != null ? Linq.<DiskImage> Cast(getItems()) : new java.util.ArrayList<DiskImage>();
                boolean hasDisks = disks.size() > 0;
                java.util.ArrayList<storage_domains> storageDomains = new java.util.ArrayList<storage_domains>();
                for (storage_domains a : (java.util.ArrayList<storage_domains>) result1)
                {
                    if (a.getstorage_domain_type() != StorageDomainType.ISO
                            && a.getstorage_domain_type() != StorageDomainType.ImportExport
                            && a.getstatus() == StorageDomainStatus.Active)
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
                    _asyncQuery2.asyncCallback = new INewAsyncCallback() {
                        @Override
                        public void OnSuccess(Object model2, Object result2)
                        {
                            VmDiskListModel vmDiskListModel2 = (VmDiskListModel) model2;
                            DiskModel diskModel2 = (DiskModel) vmDiskListModel2.getWindow();
                            java.util.ArrayList<storage_domains> storageDomains2 =
                                    (java.util.ArrayList<storage_domains>) diskModel2.getStorageDomain().getItems();
                            storage_domains storage2 = (storage_domains) result2;
                            vmDiskListModel2.StepA(storage2 != null
                                    && Linq.IsSDItemExistInList(storageDomains2, storage2.getid()) ? storage2 : null);
                        }
                    };
                    AsyncDataProvider.GetStorageDomainById(_asyncQuery2, disks.get(0).getstorage_id().getValue());
                }
                else // first disk -> just choose the first from the list of available storage-domains:
                {
                    vmDiskListModel.StepA(Linq.FirstOrDefault(storageDomains));
                }
            }
        };
        AsyncDataProvider.GetStorageDomainList(_asyncQuery1, vm.getstorage_pool_id());
    }

    private void Edit()
    {
        DiskImage disk = (DiskImage) getSelectedItem();

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
        _asyncQuery1.asyncCallback = new INewAsyncCallback() {
            @Override
            public void OnSuccess(Object model, Object result)
            {
                VmDiskListModel vmDiskListModel = (VmDiskListModel) model;
                DiskModel diskModel = (DiskModel) vmDiskListModel.getWindow();
                VM vm = (VM) vmDiskListModel.getEntity();
                storage_domains storageDomain = (storage_domains) result;
                DiskImage disk = (DiskImage) vmDiskListModel.getSelectedItem();

                diskModel.getStorageDomain().setSelectedItem(storageDomain);

                DiskImageBase preset = new DiskImage();
                preset.setdisk_type(disk.getdisk_type());
                diskModel.getPreset().setSelectedItem(preset);
                diskModel.getPreset().setIsChangable(false);

                diskModel.getVolumeType().setSelectedItem(disk.getvolume_type());
                diskModel.getVolumeType().setIsChangable(false);

                diskModel.setVolumeFormat(disk.getvolume_format());

                java.util.ArrayList<DiskInterface> interfaces =
                        DataProvider.GetDiskInterfaceList(vm.getvm_os(), vm.getvds_group_compatibility_version());
                if (!interfaces.contains(disk.getdisk_interface()))
                {
                    interfaces.add(disk.getdisk_interface());
                }
                diskModel.getInterface().setItems(interfaces);
                diskModel.getInterface().setSelectedItem(disk.getdisk_interface());
                diskModel.getInterface().setIsChangable(false);

                storage_domains storage = (storage_domains) diskModel.getStorageDomain().getSelectedItem();

                diskModel.getWipeAfterDelete().setEntity(disk.getwipe_after_delete());
                if (diskModel.getStorageDomain() != null && diskModel.getStorageDomain().getSelectedItem() != null)
                {
                    vmDiskListModel.UpdateWipeAfterDelete(storage.getstorage_type(),
                            diskModel.getWipeAfterDelete(),
                            false);
                }

                java.util.ArrayList<DiskImage> disks =
                        vmDiskListModel.getItems() != null ? Linq.<DiskImage> Cast(vmDiskListModel.getItems())
                                : new java.util.ArrayList<DiskImage>();

                DiskImage bootableDisk = null;
                for (DiskImage a : disks)
                {
                    if (a.getboot())
                    {
                        bootableDisk = a;
                        break;
                    }
                }
                if (bootableDisk != null && !bootableDisk.getId().equals(disk.getId()))
                {
                    diskModel.getIsBootable().setIsChangable(false);
                    diskModel.getIsBootable()
                            .getChangeProhibitionReasons()
                            .add("There can be only one bootable disk defined.");
                }
                diskModel.getIsBootable().setEntity(disk.getboot());

                UICommand tempVar = new UICommand("OnSave", vmDiskListModel);
                tempVar.setTitle("OK");
                tempVar.setIsDefault(true);
                diskModel.getCommands().add(tempVar);
                UICommand tempVar2 = new UICommand("Cancel", vmDiskListModel);
                tempVar2.setTitle("Cancel");
                tempVar2.setIsCancel(true);
                diskModel.getCommands().add(tempVar2);
            }
        };

        AsyncDataProvider.GetStorageDomainById(_asyncQuery1, disk.getstorage_id().getValue());
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
            DiskImage a = (DiskImage) item;
            if (a.getdisk_type() == DiskType.System)
            {
                items.add(StringFormat.format("Disk %1$s (System Disk)", a.getinternal_drive_mapping()));
                if (!hasSystemDiskWarning)
                {
                    model.setNote("Note that removing a system disk would make the VM unbootable.");
                    hasSystemDiskWarning = true;
                }
            }
            else
            {
                items.add(StringFormat.format("Disk %1$s", a.getinternal_drive_mapping()));
            }
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

    public void OnSystemRemoveConfirm()
    {
        // var systemDisks = SelectedItems.Cast<DiskImage>().Where(a => a.disk_type == DiskType.System);
        // if (systemDisks.Count() > 0)
        // {
        // SystemDiskRemoveConfirmModel = new ConfirmModel();
        // SystemDiskRemoveConfirmModel.View = new ConfirmationView();
        // SystemDiskRemoveConfirmModel.IsOpen = true;
        // SystemDiskRemoveConfirmModel.Header = "Remove System Disk(s)";
        // SystemDiskRemoveConfirmModel.ConfirmMsg = "Are you sure you want to remove the following System Disk(s)?";
        // SystemDiskRemoveConfirmModel.EntityNames = systemDisks.Select(a => StringFormat.format("Disk {0}",
        // a.internal_drive_mapping));
        // SystemDiskRemoveConfirmModel.Commands =
        // new ArrayList
        // {
        // new
        // {
        // Command = new DelegateCommand(OnRemove),
        // Text = "OK",
        // IsDefault = true
        // },
        // new
        // {
        // Command = new DelegateCommand(Cancel),
        // Text = "Cancel"
        // }
        // };
        // }
        // else
        // {
        // OnRemove();
        // }
    }

    private void OnRemove()
    {
        VM vm = (VM) getEntity();

        // TODO: Confirm system disk removal.

        // List<Guid> images = SelectedItems.Cast<DiskImage>().Select(a =>(Guid) a.image_guid).ToList();

        java.util.ArrayList<Guid> images = new java.util.ArrayList<Guid>();
        for (Object item : getSelectedItems())
        {
            DiskImage a = (DiskImage) item;
            images.add(a.getId());
        }

        Frontend.RunAction(VdcActionType.RemoveDisksFromVm, new RemoveDisksFromVmParameters(vm.getvm_guid(), images),
                new IFrontendActionAsyncCallback() {
                    @Override
                    public void Executed(FrontendActionAsyncResult result) {

                    }
                }, null);

        Cancel();
    }

    private void OnSave()
    {
        VM vm = (VM) getEntity();
        DiskModel model = (DiskModel) getWindow();

        if (model.getProgress() != null)
        {
            return;
        }

        if (!model.Validate())
        {
            return;
        }

        // Save changes.
        storage_domains storageDomain = (storage_domains) model.getStorageDomain().getSelectedItem();

        DiskImage disk = model.getIsNew() ? new DiskImage() : (DiskImage) getSelectedItem();
        disk.setSizeInGigabytes(Integer.parseInt(model.getSize().getEntity().toString()));

        DiskImageBase preset = (DiskImageBase) model.getPreset().getSelectedItem();
        disk.setdisk_type(preset.getdisk_type());

        disk.setdisk_interface((DiskInterface) model.getInterface().getSelectedItem());
        disk.setvolume_type((VolumeType) model.getVolumeType().getSelectedItem());
        disk.setvolume_format(model.getVolumeFormat());
        disk.setwipe_after_delete((Boolean) model.getWipeAfterDelete().getEntity());
        disk.setboot((Boolean) model.getIsBootable().getEntity());

        // NOTE: Since we doesn't support partial snapshots in GUI, propagate errors flag always must be set false.
        // disk.propagate_errors = model.PropagateErrors.ValueAsBoolean() ? PropagateErrors.On : PropagateErrors.Off;
        disk.setpropagate_errors(PropagateErrors.Off);

        model.StartProgress(null);

        if (model.getIsNew())
        {
            AddDiskToVmParameters tempVar = new AddDiskToVmParameters(vm.getvm_guid(), disk);
            tempVar.setStorageDomainId(storageDomain.getid());
            Frontend.RunAction(VdcActionType.AddDiskToVm, tempVar,
                    new IFrontendActionAsyncCallback() {
                        @Override
                        public void Executed(FrontendActionAsyncResult result) {

                            VmDiskListModel localModel = (VmDiskListModel) result.getState();
                            localModel.PostOnSaveInternal(result.getReturnValue());

                        }
                    }, this);
        }
        else
        {
            Frontend.RunAction(VdcActionType.UpdateVmDisk, new UpdateVmDiskParameters(vm.getvm_guid(),
                    disk.getId(),
                    disk),
                    new IFrontendActionAsyncCallback() {
                        @Override
                        public void Executed(FrontendActionAsyncResult result) {

                            VmDiskListModel localModel = (VmDiskListModel) result.getState();
                            localModel.PostOnSaveInternal(result.getReturnValue());

                        }
                    }, this);
        }
    }

    public void PostOnSaveInternal(VdcReturnValueBase returnValue)
    {
        DiskModel model = (DiskModel) getWindow();

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
        VM vm = (VM) getEntity();
        boolean isDown = vm != null && vm.getstatus() == VMStatus.Down;

        getNewCommand().setIsExecutionAllowed(isDown);

        getEditCommand().setIsExecutionAllowed(getSelectedItem() != null && getSelectedItems() != null
                && getSelectedItems().size() == 1 && isDown);

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
        DiskModel model = (DiskModel) getWindow();
        VM vm = (VM) getEntity();

        model.getStorageDomain().setSelectedItem(storage);

        if (storage != null)
        {
            UpdateWipeAfterDelete(storage.getstorage_type(), model.getWipeAfterDelete(), true);
        }

        AsyncQuery _asyncQuery1 = new AsyncQuery();
        _asyncQuery1.setModel(this);
        _asyncQuery1.asyncCallback = new INewAsyncCallback() {
            @Override
            public void OnSuccess(Object model1, Object result1)
            {
                VmDiskListModel vmDiskListModel1 = (VmDiskListModel) model1;
                DiskModel vmModel = (DiskModel) vmDiskListModel1.getWindow();
                VM vm1 = (VM) vmDiskListModel1.getEntity();

                java.util.ArrayList<DiskImage> disks =
                        vmDiskListModel1.getItems() != null ? Linq.<DiskImage> Cast(vmDiskListModel1.getItems())
                                : new java.util.ArrayList<DiskImage>();
                boolean hasDisks = disks.size() > 0;
                storage_domains storage1 = (storage_domains) vmModel.getStorageDomain().getSelectedItem();

                java.util.ArrayList<DiskImageBase> presets = (java.util.ArrayList<DiskImageBase>) result1;
                vmModel.getPreset().setItems(presets);

                for (DiskImageBase a : presets)
                {
                    if ((hasDisks && a.getdisk_type() == DiskType.Data)
                            || (!hasDisks && a.getdisk_type() == DiskType.System))
                    {
                        vmModel.getPreset().setSelectedItem(a);
                        break;
                    }
                }

                vmModel.getInterface().setItems(DataProvider.GetDiskInterfaceList(vm1.getvm_os(),
                        vm1.getvds_group_compatibility_version()));
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
                    vmModel.getIsBootable()
                            .getChangeProhibitionReasons()
                            .add("There can be only one bootable disk defined.");
                }

                java.util.ArrayList<UICommand> commands = new java.util.ArrayList<UICommand>();

                if (storage1 == null)
                {
                    String cantCreateMessage =
                            "There is no active Storage Domain to create the Disk in. Please activate a Storage Domain.";
                    if (hasDisks)
                    {
                        cantCreateMessage = "Error in retrieving the relevant Storage Domain.";
                        // if (storage.storage_name != null)
                        // {
                        // cantCreateMessage =
                        // StringFormat.format("'{0}' Storage Domain is not active. Please activate it.",
                        // storage.storage_name);
                        // }
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

            }
        };
        AsyncDataProvider.GetDiskPresetList(_asyncQuery1,
                vm.getvm_type(),
                model.getStorageDomain().getSelectedItem() == null ? StorageType.UNKNOWN : storage.getstorage_type());
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
                _asyncQuery.asyncCallback = new INewAsyncCallback() {
                    @Override
                    public void OnSuccess(Object model, Object result)
                    {
                        DiskModel diskModel = (DiskModel) model;
                        diskModel.getWipeAfterDelete().setEntity(result);
                    }
                };
                AsyncDataProvider.GetSANWipeAfterDelete(_asyncQuery);
            }
        }
    }

    @Override
    protected String getListName() {
        return "VmDiskListModel";
    }
}
