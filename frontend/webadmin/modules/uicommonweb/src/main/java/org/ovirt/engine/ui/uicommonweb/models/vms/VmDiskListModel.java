package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;

import org.ovirt.engine.core.common.action.AddDiskParameters;
import org.ovirt.engine.core.common.action.AttachDettachVmDiskParameters;
import org.ovirt.engine.core.common.action.HotPlugDiskToVmParameters;
import org.ovirt.engine.core.common.action.RemoveDiskParameters;
import org.ovirt.engine.core.common.action.UpdateVmDiskParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.action.VmDiskOperatinParameterBase;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.DiskImageBase;
import org.ovirt.engine.core.common.businessentities.DiskInterface;
import org.ovirt.engine.core.common.businessentities.ImageStatus;
import org.ovirt.engine.core.common.businessentities.PropagateErrors;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.QuotaEnforcementTypeEnum;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VolumeType;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.common.queries.GetAllDisksByVmIdParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.DataProvider;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.Linq.DiskByInternalDriveMappingComparer;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.FrontendActionAsyncResult;
import org.ovirt.engine.ui.uicompat.FrontendMultipleActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;
import org.ovirt.engine.ui.uicompat.IFrontendMultipleActionAsyncCallback;

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

    private UICommand privatePlugCommand;

    public UICommand getPlugCommand()
    {
        return privatePlugCommand;
    }

    private void setPlugCommand(UICommand value)
    {
        privatePlugCommand = value;
    }

    private UICommand privateUnPlugCommand;

    public UICommand getUnPlugCommand()
    {
        return privateUnPlugCommand;
    }

    private void setUnPlugCommand(UICommand value)
    {
        privateUnPlugCommand = value;
    }

    private boolean privateIsDiskHotPlugSupported;

    public boolean getIsDiskHotPlugSupported()
    {
        VM vm = (VM) getEntity();
        boolean isVmStatusApplicableForHotPlug =
                vm != null && (vm.getstatus() == VMStatus.Up || vm.getstatus() == VMStatus.Down |
                        vm.getstatus() == VMStatus.Paused || vm.getstatus() == VMStatus.Suspended);

        return privateIsDiskHotPlugSupported && isVmStatusApplicableForHotPlug;
    }

    private void setIsDiskHotPlugSupported(boolean value)
    {
        if (privateIsDiskHotPlugSupported != value)
        {
            privateIsDiskHotPlugSupported = value;
            OnPropertyChanged(new PropertyChangedEventArgs("IsDiskHotPlugSupported")); //$NON-NLS-1$
        }
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

    private ArrayList<DiskImageBase> presets;
    private String nextAlias;
    private storage_pool datacenter;

    public VmDiskListModel()
    {
        setTitle(ConstantsManager.getInstance().getConstants().virtualDisksTitle());
        setHashName("virtual_disks"); //$NON-NLS-1$

        setNewCommand(new UICommand("New", this)); //$NON-NLS-1$
        setEditCommand(new UICommand("Edit", this)); //$NON-NLS-1$
        setRemoveCommand(new UICommand("Remove", this)); //$NON-NLS-1$
        setPlugCommand(new UICommand("Plug", this)); //$NON-NLS-1$
        setUnPlugCommand(new UICommand("Unplug", this)); //$NON-NLS-1$
        setMoveCommand(new UICommand("Move", this)); //$NON-NLS-1$

        UpdateActionAvailability();
    }

    @Override
    public VM getEntity()
    {
        return (VM) super.getEntity();
    }

    public void setEntity(VM value)
    {
        super.setEntity(value);

        UpdateIsDiskHotPlugAvailable();
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

        super.SyncSearch(VdcQueryType.GetAllDisksByVmId, new GetAllDisksByVmIdParameters(vm.getId()));
    }

    @Override
    protected void AsyncSearch()
    {
        super.AsyncSearch();

        VM vm = (VM) getEntity();

        setAsyncResult(Frontend.RegisterQuery(VdcQueryType.GetAllDisksByVmId,
                new GetAllDisksByVmIdParameters(vm.getId())));
        setItems(getAsyncResult().getData());
    }

    @Override
    public void setItems(Iterable value)
    {
        ArrayList<DiskImage> disks =
                value != null ? Linq.<DiskImage> Cast(value) : new ArrayList<DiskImage>();

        Linq.Sort(disks, new DiskByInternalDriveMappingComparer());
        super.setItems(disks);

        UpdateActionAvailability();
    }

    private void New()
    {
        final VM vm = (VM) getEntity();

        if (getWindow() != null)
        {
            return;
        }

        DiskModel model = new DiskModel();
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().addVirtualDiskTitle());
        model.setHashName("new_virtual_disk"); //$NON-NLS-1$
        model.setIsNew(true);
        model.setDatacenterId(vm.getstorage_pool_id());
        model.StartProgress(null);

        AddDiskUpdateData();
    }

    private void AddDiskUpdateData() {
        AsyncDataProvider.GetPermittedStorageDomainsByStoragePoolId(new AsyncQuery(this, new INewAsyncCallback() {
            @Override
            public void OnSuccess(Object target, Object returnValue) {
                VmDiskListModel vmDiskListModel = (VmDiskListModel) target;
                ArrayList<storage_domains> storageDomains = (ArrayList<storage_domains>) returnValue;
                DiskModel diskModel = (DiskModel) vmDiskListModel.getWindow();
                ArrayList<DiskImage> disks =
                        getItems() != null ? Linq.<DiskImage> Cast(getItems()) : new ArrayList<DiskImage>();

                ArrayList<storage_domains> filteredStorageDomains = new ArrayList<storage_domains>();
                for (storage_domains a : (ArrayList<storage_domains>) storageDomains)
                {
                    if (a.getstorage_domain_type() != StorageDomainType.ISO
                            && a.getstorage_domain_type() != StorageDomainType.ImportExport
                            && a.getstatus() == StorageDomainStatus.Active)
                    {
                        filteredStorageDomains.add(a);
                    }
                }
                Linq.Sort(filteredStorageDomains, new Linq.StorageDomainByNameComparer());
                storage_domains selectedStorage = Linq.FirstOrDefault(filteredStorageDomains);
                StorageType storageType =
                        selectedStorage == null ? StorageType.UNKNOWN : selectedStorage.getstorage_type();

                diskModel.getStorageDomain().setItems(filteredStorageDomains);
                diskModel.getStorageDomain().setSelectedItem(selectedStorage);

                AsyncDataProvider.GetDiskPresetList(new AsyncQuery(vmDiskListModel, new INewAsyncCallback() {
                    @Override
                    public void OnSuccess(Object target, Object returnValue) {
                        VmDiskListModel vmDiskListModel = (VmDiskListModel) target;
                        DiskModel diskModel1 = (DiskModel) vmDiskListModel.getWindow();
                        ArrayList<DiskImageBase> presets = (ArrayList<DiskImageBase>) returnValue;

                        diskModel1.getPreset().setItems(presets);
                        vmDiskListModel.presets = presets;
                        vmDiskListModel.AddDiskPostData();
                    }
                }), getEntity().getvm_type(), storageType);
            }
        }), getEntity().getstorage_pool_id(), ActionGroup.CREATE_VM);

        AsyncDataProvider.GetNextAvailableDiskAliasNameByVMId(new AsyncQuery(this, new INewAsyncCallback() {
            @Override
            public void OnSuccess(Object model, Object returnValue) {
                String suggestedDiskName = (String) returnValue;
                VmDiskListModel vmDiskListModel = (VmDiskListModel) model;
                DiskModel diskModel = (DiskModel) vmDiskListModel.getWindow();
                diskModel.getAlias().setEntity(suggestedDiskName);

                vmDiskListModel.nextAlias = suggestedDiskName;
                vmDiskListModel.AddDiskPostData();
            }
        }), getEntity().getId());

        AsyncDataProvider.GetDataCenterById(new AsyncQuery(this, new INewAsyncCallback() {
            @Override
            public void OnSuccess(Object model, Object returnValue) {
                storage_pool datacenter = (storage_pool) returnValue;
                VmDiskListModel vmDiskListModel = (VmDiskListModel) model;
                DiskModel dModel = (DiskModel) vmDiskListModel.getWindow();
                if (datacenter.getQuotaEnforcementType().equals(QuotaEnforcementTypeEnum.DISABLED)) {
                    dModel.getQuota().setIsAvailable(false);
                } else {
                    dModel.getQuota().setIsAvailable(true);
                    dModel.quota_storageSelectedItemChanged(getEntity().getQuotaId());
                }

                vmDiskListModel.datacenter = datacenter;
                vmDiskListModel.AddDiskPostData();
            }
        }), getEntity().getstorage_pool_id());
    }

    private void AddDiskPostData() {
        if (presets == null || nextAlias == null || datacenter == null) {
            return;
        }

        DiskModel diskModel = (DiskModel) getWindow();
        storage_domains storage = (storage_domains) diskModel.getStorageDomain().getSelectedItem();
        ArrayList<DiskImage> disks =
                getItems() != null ? Linq.<DiskImage> Cast(getItems()) : new ArrayList<DiskImage>();
        boolean hasDisks = disks.size() > 0;

        diskModel.getInterface().setItems(DataProvider.GetDiskInterfaceList(
                getEntity().getvm_os(), getEntity().getvds_group_compatibility_version()));
        diskModel.getInterface().setSelectedItem(DataProvider.GetDefaultDiskInterface(
                getEntity().getvm_os(), disks));

        if (storage != null)
        {
            UpdateWipeAfterDelete(storage.getstorage_type(), diskModel.getWipeAfterDelete(), true);
        }
        else
        {
            String cantCreateMessage =
                    hasDisks ? ConstantsManager.getInstance().getMessages().errorRetrievingStorageDomains()
                            : ConstantsManager.getInstance().getMessages().noActiveStorageDomains();

            diskModel.setMessage(cantCreateMessage);
        }

        diskModel.getPreset().setItems(presets);
        for (DiskImageBase a : presets)
        {
            if ((hasDisks && !a.isBoot()) || (!hasDisks && a.isBoot()))
            {
                diskModel.getPreset().setSelectedItem(a);
                break;
            }
        }

        boolean hasBootableDisk = false;
        for (DiskImage a : disks)
        {
            if (a.isBoot())
            {
                hasBootableDisk = true;
                break;
            }
        }

        diskModel.getIsBootable().setEntity(!hasBootableDisk);
        if (hasBootableDisk)
        {
            diskModel.getIsBootable().setIsChangable(false);
            diskModel.getIsBootable()
                    .getChangeProhibitionReasons()
                    .add("There can be only one bootable disk defined."); //$NON-NLS-1$
        }

        ArrayList<UICommand> commands = new ArrayList<UICommand>();
        UICommand tempVar2 = new UICommand("OnSave", this); //$NON-NLS-1$
        tempVar2.setTitle(ConstantsManager.getInstance().getConstants().ok());
        tempVar2.setIsDefault(true);
        diskModel.getCommands().add(tempVar2);
        UICommand tempVar3 = new UICommand("Cancel", this); //$NON-NLS-1$
        tempVar3.setTitle(ConstantsManager.getInstance().getConstants().cancel());
        tempVar3.setIsCancel(true);
        diskModel.getCommands().add(tempVar3);

        diskModel.StopProgress();
    }

    private void Edit()
    {
        final DiskImage disk = (DiskImage) getSelectedItem();

        if (getWindow() != null)
        {
            return;
        }

        DiskModel model = new DiskModel();
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().editVirtualDiskTitle());
        model.setHashName("edit_virtual_disk"); //$NON-NLS-1$
        model.getStorageDomain().setIsChangable(false);
        model.getSize().setEntity(disk.getSizeInGigabytes());
        model.getSize().setIsChangable(false);
        model.getAttachDisk().setIsChangable(false);

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
                diskModel.getPreset().setSelectedItem(preset);
                diskModel.getPreset().setIsChangable(false);

                diskModel.getVolumeType().setSelectedItem(disk.getvolume_type());
                diskModel.getVolumeType().setIsChangable(false);

                diskModel.setVolumeFormat(disk.getvolume_format());

                ArrayList<DiskInterface> interfaces =
                        DataProvider.GetDiskInterfaceList(vm.getvm_os(), vm.getvds_group_compatibility_version());
                if (!interfaces.contains(disk.getDiskInterface()))
                {
                    interfaces.add(disk.getDiskInterface());
                }
                diskModel.getInterface().setItems(interfaces);
                diskModel.getInterface().setSelectedItem(disk.getDiskInterface());
                diskModel.getInterface().setIsChangable(false);

                storage_domains storage = (storage_domains) diskModel.getStorageDomain().getSelectedItem();

                diskModel.getWipeAfterDelete().setEntity(disk.isWipeAfterDelete());
                if (diskModel.getStorageDomain() != null && diskModel.getStorageDomain().getSelectedItem() != null)
                {
                    vmDiskListModel.UpdateWipeAfterDelete(storage.getstorage_type(),
                            diskModel.getWipeAfterDelete(),
                            false);
                }

                ArrayList<DiskImage> disks =
                        vmDiskListModel.getItems() != null ? Linq.<DiskImage> Cast(vmDiskListModel.getItems())
                                : new ArrayList<DiskImage>();

                DiskImage bootableDisk = null;
                for (DiskImage a : disks)
                {
                    if (a.isBoot())
                    {
                        bootableDisk = a;
                        break;
                    }
                }
                if (bootableDisk != null && !bootableDisk.getImageId().equals(disk.getImageId()))
                {
                    diskModel.getIsBootable().setIsChangable(false);
                    diskModel.getIsBootable()
                            .getChangeProhibitionReasons()
                            .add("There can be only one bootable disk defined."); //$NON-NLS-1$
                }
                diskModel.getIsBootable().setEntity(disk.isBoot());

                diskModel.getAlias().setEntity(disk.getDiskAlias());
                diskModel.getDescription().setEntity(disk.getDiskDescription());

                UICommand tempVar = new UICommand("OnSave", vmDiskListModel); //$NON-NLS-1$
                tempVar.setTitle(ConstantsManager.getInstance().getConstants().ok());
                tempVar.setIsDefault(true);
                diskModel.getCommands().add(tempVar);
                UICommand tempVar2 = new UICommand("Cancel", vmDiskListModel); //$NON-NLS-1$
                tempVar2.setTitle(ConstantsManager.getInstance().getConstants().cancel());
                tempVar2.setIsCancel(true);
                diskModel.getCommands().add(tempVar2);
            }
        };

        AsyncDataProvider.GetStorageDomainById(_asyncQuery1, disk.getstorage_ids().get(0));

        AsyncDataProvider.GetDataCenterById(new AsyncQuery(this, new INewAsyncCallback() {

            @Override
            public void OnSuccess(Object model, Object returnValue) {
                storage_pool dataCenter = (storage_pool) returnValue;
                VmDiskListModel vmDiskListModel1 = (VmDiskListModel) model;
                DiskModel dModel = (DiskModel) vmDiskListModel1.getWindow();
                if (dataCenter.getQuotaEnforcementType().equals(QuotaEnforcementTypeEnum.DISABLED)) {
                    dModel.getQuota().setIsAvailable(false);
                } else {
                    dModel.getQuota().setIsAvailable(true);
                    dModel.quota_storageSelectedItemChanged(disk.getQuotaId());
                }
            }
        }), ((VM) getEntity()).getstorage_pool_id());
    }

    private void remove()
    {
        if (getWindow() != null)
        {
            return;
        }

        boolean hasSystemDiskWarning = false;
        RemoveDiskModel model = new RemoveDiskModel();
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().removeDisksTitle());
        model.setHashName("remove_disk"); //$NON-NLS-1$
        model.setMessage(ConstantsManager.getInstance().getConstants().disksMsg());

        model.getLatch().setEntity(true);

        ArrayList<String> items = new ArrayList<String>();
        for (Object item : getSelectedItems())
        {
            DiskImage a = (DiskImage) item;
            items.add(a.getDiskAlias());
        }
        model.setItems(items);

        UICommand tempVar = new UICommand("OnRemove", this); //$NON-NLS-1$
        tempVar.setTitle(ConstantsManager.getInstance().getConstants().ok());
        tempVar.setIsDefault(true);
        model.getCommands().add(tempVar);
        UICommand tempVar2 = new UICommand("Cancel", this); //$NON-NLS-1$
        tempVar2.setTitle(ConstantsManager.getInstance().getConstants().cancel());
        tempVar2.setIsCancel(true);
        model.getCommands().add(tempVar2);
    }

    private void OnRemove() {
        VM vm = (VM) getEntity();
        RemoveDiskModel model = (RemoveDiskModel) getWindow();
        boolean removeDisk = (Boolean) model.getLatch().getEntity();
        VdcActionType actionType = removeDisk ? VdcActionType.RemoveDisk : VdcActionType.DetachDiskFromVm;
        ArrayList<VdcActionParametersBase> paramerterList = new ArrayList<VdcActionParametersBase>();

        for (Object item : getSelectedItems()) {
            Disk disk = (Disk) item;
            VdcActionParametersBase parameters = removeDisk ?
                    new RemoveDiskParameters(disk.getId()) :
                    new AttachDettachVmDiskParameters(vm.getId(), disk.getId(), true);

            paramerterList.add(parameters);
        }

        model.StartProgress(null);

        Frontend.RunMultipleAction(actionType, paramerterList,
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
        VM vm = (VM) getEntity();
        DiskModel model = (DiskModel) getWindow();

        if (model.getProgress() != null || !model.Validate())
        {
            return;
        }

        if ((Boolean) model.getAttachDisk().getEntity())
        {
            OnAttachDisks();
            return;
        }

        // Save changes.
        storage_domains storageDomain = (storage_domains) model.getStorageDomain().getSelectedItem();

        DiskImage disk = model.getIsNew() ? new DiskImage() : (DiskImage) getSelectedItem();
        disk.setSizeInGigabytes(Integer.parseInt(model.getSize().getEntity().toString()));
        disk.setDiskAlias((String) model.getAlias().getEntity());
        disk.setDiskDescription((String) model.getDescription().getEntity());
        disk.setDiskInterface((DiskInterface) model.getInterface().getSelectedItem());
        disk.setvolume_type((VolumeType) model.getVolumeType().getSelectedItem());
        disk.setvolume_format(model.getVolumeFormat());
        disk.setWipeAfterDelete((Boolean) model.getWipeAfterDelete().getEntity());
        disk.setBoot((Boolean) model.getIsBootable().getEntity());
        disk.setPlugged((Boolean) model.getIsPlugged().getEntity());
        if (model.getQuota().getIsAvailable()) {
            disk.setQuotaId(((Quota) model.getQuota().getSelectedItem()).getId());
        }

        // NOTE: Since we doesn't support partial snapshots in GUI, propagate errors flag always must be set false.
        // disk.propagate_errors = model.PropagateErrors.ValueAsBoolean() ? PropagateErrors.On : PropagateErrors.Off;
        disk.setPropagateErrors(PropagateErrors.Off);

        model.StartProgress(null);

        VdcActionType actionType;
        VmDiskOperatinParameterBase parameters;

        if (model.getIsNew())
        {
            parameters = new AddDiskParameters(vm.getId(), disk);
            ((AddDiskParameters) parameters).setStorageDomainId(storageDomain.getId());
            actionType = VdcActionType.AddDisk;
        }
        else
        {
            parameters = new UpdateVmDiskParameters(vm.getId(), disk.getId(), disk);
            actionType = VdcActionType.UpdateVmDisk;
        }

        Frontend.RunAction(actionType, parameters,
                new IFrontendActionAsyncCallback() {
                    @Override
                    public void Executed(FrontendActionAsyncResult result) {

                        VmDiskListModel localModel = (VmDiskListModel) result.getState();
                        localModel.PostOnSaveInternal(result.getReturnValue());

                    }
                }, this);
    }

    private void OnAttachDisks()
    {
        VM vm = (VM) getEntity();
        DiskModel model = (DiskModel) getWindow();
        ArrayList<VdcActionParametersBase> paramerterList = new ArrayList<VdcActionParametersBase>();

        for (EntityModel item : (ArrayList<EntityModel>) model.getAttachableDisks().getSelectedItems())
        {
            DiskModel disk = (DiskModel) item.getEntity();
            disk.getDiskImage().setPlugged((Boolean) model.getIsPlugged().getEntity());
            UpdateVmDiskParameters parameters =
                    new UpdateVmDiskParameters(vm.getId(), disk.getDiskImage().getId(), disk.getDiskImage());
            paramerterList.add(parameters);
        }

        model.StartProgress(null);

        Frontend.RunMultipleAction(VdcActionType.AttachDiskToVm, paramerterList,
                new IFrontendMultipleActionAsyncCallback() {
                    @Override
                    public void Executed(FrontendMultipleActionAsyncResult result) {
                        VmDiskListModel localModel = (VmDiskListModel) result.getState();
                        localModel.getWindow().StopProgress();
                        Cancel();
                    }
                },
                this);
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

    private void Plug(boolean plug) {
        VM vm = (VM) getEntity();

        ArrayList<VdcActionParametersBase> paramerterList = new ArrayList<VdcActionParametersBase>();
        for (Object item : getSelectedItems()) {
            DiskImage disk = (DiskImage) item;
            disk.setPlugged(plug);

            paramerterList.add(new HotPlugDiskToVmParameters(vm.getId(), disk.getId()));
        }

        VdcActionType plugAction = VdcActionType.HotPlugDiskToVm;
        if (!plug) {
            plugAction = VdcActionType.HotUnPlugDiskFromVm;
        }

        Frontend.RunMultipleAction(plugAction, paramerterList,
                new IFrontendMultipleActionAsyncCallback() {
                    @Override
                    public void Executed(FrontendMultipleActionAsyncResult result) {
                    }
                },
                this);
    }

    private void Move()
    {
        ArrayList<DiskImage> disks = (ArrayList<DiskImage>) getSelectedItems();

        if (disks == null)
        {
            return;
        }

        if (getWindow() != null)
        {
            return;
        }

        VM vm = (VM) getEntity();

        MoveDiskModel model = new MoveDiskModel();
        model.setIsSingleDiskMove(disks.size() == 1);
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().moveDisksTitle());
        model.setHashName("move_disk"); //$NON-NLS-1$
        model.setIsSourceStorageDomainNameAvailable(true);
        model.setEntity(this);
        model.init(disks);
        model.StartProgress(null);
    }

    private void ResetData() {
        presets = null;
        nextAlias = null;
        datacenter = null;
    }

    private void Cancel()
    {
        setWindow(null);
        ResetData();
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

        if (e.PropertyName.equals("status")) //$NON-NLS-1$
        {
            UpdateActionAvailability();
        }
    }

    private void UpdateActionAvailability()
    {
        VM vm = (VM) getEntity();
        DiskImage disk = (DiskImage) getSelectedItem();
        boolean isDiskLocked = disk != null && disk.getimageStatus() == ImageStatus.LOCKED;

        getNewCommand().setIsExecutionAllowed(isVmDown());

        getEditCommand().setIsExecutionAllowed(getSelectedItem() != null && getSelectedItems() != null
                && getSelectedItems().size() == 1 && isVmDown() && !isDiskLocked);

        getRemoveCommand().setIsExecutionAllowed(getSelectedItems() != null && getSelectedItems().size() > 0
                && isRemoveCommandAvailable());

        getMoveCommand().setIsExecutionAllowed(getSelectedItems() != null && getSelectedItems().size() > 0
                && isMoveCommandAvailable());

        getPlugCommand().setIsExecutionAllowed(isPlugCommandAvailable(true));

        getUnPlugCommand().setIsExecutionAllowed(isPlugCommandAvailable(false));
    }

    public boolean isVmDown() {
        VM vm = (VM) getEntity();
        return vm != null && vm.getstatus() == VMStatus.Down;
    }

    public boolean isHotPlugAvailable() {
        VM vm = (VM) getEntity();
        return vm != null && (vm.getstatus() == VMStatus.Up ||
                vm.getstatus() == VMStatus.Paused || vm.getstatus() == VMStatus.Suspended);
    }

    private boolean isPlugCommandAvailable(boolean plug) {
        return getSelectedItems() != null && getSelectedItems().size() > 0
                && isPlugAvailableByDisks(plug) &&
                (isVmDown() || (isHotPlugAvailable() && getIsDiskHotPlugSupported()));
    }

    private boolean isPlugAvailableByDisks(boolean plug) {
        ArrayList<DiskImage> disks =
                getSelectedItems() != null ? Linq.<DiskImage> Cast(getSelectedItems()) : new ArrayList<DiskImage>();

        for (DiskImage disk : disks)
        {
            if (disk.getPlugged() == plug || disk.getDiskInterface() == DiskInterface.IDE ||
                    disk.getimageStatus() == ImageStatus.LOCKED)
            {
                return false;
            }
        }

        return true;
    }

    private boolean isMoveCommandAvailable() {
        ArrayList<DiskImage> disks =
                getSelectedItems() != null ? Linq.<DiskImage> Cast(getSelectedItems()) : new ArrayList<DiskImage>();

        for (DiskImage disk : disks)
        {
            if (disk.getimageStatus() != ImageStatus.OK || (!isVmDown() && disk.getPlugged()))
            {
                return false;
            }
        }

        return true;
    }

    private boolean isRemoveCommandAvailable() {
        ArrayList<DiskImage> disks =
                getSelectedItems() != null ? Linq.<DiskImage> Cast(getSelectedItems()) : new ArrayList<DiskImage>();

        for (DiskImage disk : disks)
        {
            if (disk.getimageStatus() == ImageStatus.LOCKED || (!isVmDown() && disk.getPlugged()))
            {
                return false;
            }
        }

        return true;
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
        else if (command == getMoveCommand())
        {
            Move();
        }
        else if (StringHelper.stringsEqual(command.getName(), "OnSave")) //$NON-NLS-1$
        {
            OnSave();
        }
        else if (StringHelper.stringsEqual(command.getName(), "Cancel")) //$NON-NLS-1$
        {
            Cancel();
        }
        else if (StringHelper.stringsEqual(command.getName(), "OnRemove")) //$NON-NLS-1$
        {
            OnRemove();
        }
        else if (command == getPlugCommand())
        {
            Plug(true);
        }
        else if (command == getUnPlugCommand())
        {
            Plug(false);
        }
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

    protected void UpdateIsDiskHotPlugAvailable()
    {
        if (getEntity() == null)
        {
            return;
        }
        VM vm = (VM) getEntity();
        Version clusterCompatibilityVersion = vm.getvds_group_compatibility_version() != null
                ? vm.getvds_group_compatibility_version() : new Version();

        AsyncDataProvider.IsDiskHotPlugAvailable(new AsyncQuery(this,
                new INewAsyncCallback() {
                    @Override
                    public void OnSuccess(Object target, Object returnValue) {
                        VmDiskListModel model = (VmDiskListModel) target;
                        model.setIsDiskHotPlugSupported((Boolean) returnValue);
                    }
                }), clusterCompatibilityVersion.toString());
    }

    @Override
    protected String getListName() {
        return "VmDiskListModel"; //$NON-NLS-1$
    }
}
