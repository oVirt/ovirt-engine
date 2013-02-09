package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;

import org.ovirt.engine.core.common.action.AddDiskParameters;
import org.ovirt.engine.core.common.action.AttachDettachVmDiskParameters;
import org.ovirt.engine.core.common.action.ChangeQuotaParameters;
import org.ovirt.engine.core.common.action.HotPlugDiskToVmParameters;
import org.ovirt.engine.core.common.action.RemoveDiskParameters;
import org.ovirt.engine.core.common.action.UpdateVmDiskParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VmDiskOperationParameterBase;
import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.Disk.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.DiskImageBase;
import org.ovirt.engine.core.common.businessentities.DiskInterface;
import org.ovirt.engine.core.common.businessentities.ImageStatus;
import org.ovirt.engine.core.common.businessentities.LUNs;
import org.ovirt.engine.core.common.businessentities.LunDisk;
import org.ovirt.engine.core.common.businessentities.PropagateErrors;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.QuotaEnforcementTypeEnum;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VolumeType;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.common.queries.GetAllDisksByVmIdParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.Linq.DiskByAliasComparer;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ISupportSystemTreeContext;
import org.ovirt.engine.ui.uicommonweb.models.SystemTreeItemType;
import org.ovirt.engine.ui.uicommonweb.models.quota.ChangeQuotaItemModel;
import org.ovirt.engine.ui.uicommonweb.models.quota.ChangeQuotaModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.SanStorageModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.FrontendActionAsyncResult;
import org.ovirt.engine.ui.uicompat.FrontendMultipleActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;
import org.ovirt.engine.ui.uicompat.IFrontendMultipleActionAsyncCallback;

@SuppressWarnings("unused")
public class VmDiskListModel extends VmDiskListModelBase
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

    ISupportSystemTreeContext systemTreeContext;

    public ISupportSystemTreeContext getSystemTreeContext() {
        return systemTreeContext;
    }

    public void setSystemTreeContext(ISupportSystemTreeContext systemTreeContext) {
        this.systemTreeContext = systemTreeContext;
    }

    private UICommand privateChangeQuotaCommand;

    public UICommand getChangeQuotaCommand()
    {
        return privateChangeQuotaCommand;
    }

    private void setChangeQuotaCommand(UICommand value)
    {
        privateChangeQuotaCommand = value;
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

    private boolean privateIsDiskHotPlugSupported;

    public boolean getIsDiskHotPlugSupported()
    {
        VM vm = getEntity();
        boolean isVmStatusApplicableForHotPlug =
                vm != null && (vm.getStatus() == VMStatus.Up || vm.getStatus() == VMStatus.Down |
                        vm.getStatus() == VMStatus.Paused || vm.getStatus() == VMStatus.Suspended);

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

    private boolean isLiveStorageMigrationEnabled;

    public boolean getIsLiveStorageMigrationEnabled()
    {
        return isLiveStorageMigrationEnabled;
    }

    private void setIsLiveStorageMigrationEnabled(boolean value)
    {
        if (isLiveStorageMigrationEnabled != value)
        {
            isLiveStorageMigrationEnabled = value;
            OnPropertyChanged(new PropertyChangedEventArgs("IsLiveStorageMigrationEnabled")); //$NON-NLS-1$
        }
    }

    private ArrayList<DiskImageBase> presets;
    private String nextAlias;
    private storage_pool datacenter;

    public VmDiskListModel()
    {
        setTitle(ConstantsManager.getInstance().getConstants().disksTitle());
        setHashName("disks"); //$NON-NLS-1$

        setNewCommand(new UICommand("New", this)); //$NON-NLS-1$
        setEditCommand(new UICommand("Edit", this)); //$NON-NLS-1$
        setRemoveCommand(new UICommand("Remove", this)); //$NON-NLS-1$
        setPlugCommand(new UICommand("Plug", this)); //$NON-NLS-1$
        setUnPlugCommand(new UICommand("Unplug", this)); //$NON-NLS-1$
        setMoveCommand(new UICommand("Move", this)); //$NON-NLS-1$
        setChangeQuotaCommand(new UICommand("changeQuota", this)); //$NON-NLS-1$
        getChangeQuotaCommand().setIsAvailable(false);

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
    }

    @Override
    protected void OnEntityChanged()
    {
        super.OnEntityChanged();

        if (getEntity() != null)
        {
            getSearchCommand().Execute();
            UpdateIsDiskHotPlugAvailable();
            UpdateLiveStorageMigrationEnabled();
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
        VM vm = getEntity();

        super.SyncSearch(VdcQueryType.GetAllDisksByVmId, new GetAllDisksByVmIdParameters(vm.getId()));
    }

    @Override
    protected void AsyncSearch()
    {
        super.AsyncSearch();

        VM vm = getEntity();

        setAsyncResult(Frontend.RegisterQuery(VdcQueryType.GetAllDisksByVmId,
                new GetAllDisksByVmIdParameters(vm.getId())));
        setItems(getAsyncResult().getData());
    }

    @Override
    public void setItems(Iterable value)
    {
        ArrayList<Disk> disks =
                value != null ? Linq.<Disk> Cast(value) : new ArrayList<Disk>();

        Linq.Sort(disks, new DiskByAliasComparer());
        super.setItems(disks);

        UpdateActionAvailability();
    }

    private void New()
    {
        final VM vm = getEntity();

        if (getWindow() != null)
        {
            return;
        }

        DiskModel model = new DiskModel();
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().addVirtualDiskTitle());
        model.setHashName("new_virtual_disk"); //$NON-NLS-1$
        model.setIsNew(true);
        model.setDatacenterId(vm.getStoragePoolId());
        model.getIsInVm().setEntity(true);
        model.getIsInternal().setEntity(true);
        model.setVmId(getEntity().getId());
        model.StartProgress(null);

        AddDiskUpdateData();
    }

    private void AddDiskUpdateData() {
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
    }

    private void AddDiskPostData() {
        if (nextAlias == null) {
            return;
        }

        DiskModel diskModel = (DiskModel) getWindow();
        storage_domains storage = (storage_domains) diskModel.getStorageDomain().getSelectedItem();
        ArrayList<Disk> disks =
                getItems() != null ? Linq.<Disk> Cast(getItems()) : new ArrayList<Disk>();
        boolean hasDisks = disks.size() > 0;

        boolean hasBootableDisk = false;
        for (Disk a : disks)
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

    private void changeQuota() {
        ArrayList<DiskImage> disks = (ArrayList<DiskImage>) getSelectedItems();

        if (disks == null || getWindow() != null)
        {
            return;
        }

        ChangeQuotaModel model = new ChangeQuotaModel();
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().assignQuotaForDisk());
        model.setHashName("change_quota_disks"); //$NON-NLS-1$
        model.StartProgress(null);
        model.init(disks);

        UICommand command = new UICommand("onChangeQuota", this); //$NON-NLS-1$
        command.setTitle(ConstantsManager.getInstance().getConstants().ok());
        command.setIsDefault(true);
        model.getCommands().add(command);
        command = new UICommand("Cancel", this); //$NON-NLS-1$
        command.setTitle(ConstantsManager.getInstance().getConstants().cancel());
        command.setIsCancel(true);
        model.getCommands().add(command);
    }

    private void onChangeQuota() {
        ChangeQuotaModel model = (ChangeQuotaModel) getWindow();
        ArrayList<VdcActionParametersBase> paramerterList = new ArrayList<VdcActionParametersBase>();

        for (Object item : model.getItems())
        {
            ChangeQuotaItemModel itemModel = (ChangeQuotaItemModel) item;
            DiskImage disk = (DiskImage) itemModel.getEntity();
            VdcActionParametersBase parameters =
                    new ChangeQuotaParameters(((Quota) itemModel.getQuota().getSelectedItem()).getId(),
                            disk.getId(),
                            disk.getstorage_ids().get(0),
                            disk.getstorage_pool_id().getValue());
            paramerterList.add(parameters);
        }

        model.StartProgress(null);

        Frontend.RunMultipleAction(VdcActionType.ChangeQuotaForDisk, paramerterList,
                new IFrontendMultipleActionAsyncCallback() {
                    @Override
                    public void Executed(FrontendMultipleActionAsyncResult result) {
                        Cancel();
                    }
                },
                this);
    }

    private void Edit()
    {
        final Disk disk = (Disk) getSelectedItem();

        if (getWindow() != null)
        {
            return;
        }

        DiskModel model = new DiskModel();
        model.setDisk(disk);
        model.setIsNew(false);
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().editVirtualDiskTitle());
        model.setHashName("edit_virtual_disk"); //$NON-NLS-1$
        model.getAttachDisk().setIsAvailable(false);
        model.getIsInVm().setEntity(true);
        model.getIsInternal().setEntity(disk.getDiskStorageType() == DiskStorageType.IMAGE);
        model.getStorageDomain().setIsChangable(false);
        model.getHost().setIsChangable(false);
        model.getStorageType().setIsChangable(false);
        model.getDataCenter().setIsChangable(false);
        model.getSize().setIsChangable(false);
        model.getSize()
                .setEntity(disk.getDiskStorageType() == DiskStorageType.IMAGE ?
                        ((DiskImage) disk).getSizeInGigabytes() :
                        ((LunDisk) disk).getLun().getDeviceSize());

        Guid storageDomainId = disk.getDiskStorageType() == DiskStorageType.IMAGE ?
                ((DiskImage) disk).getstorage_ids().get(0) : Guid.Empty;

        model.StartProgress(null);

        AsyncQuery _asyncQuery1 = new AsyncQuery();
        _asyncQuery1.setModel(this);
        _asyncQuery1.asyncCallback = new INewAsyncCallback() {
            @Override
            public void OnSuccess(Object model, Object result)
            {
                VmDiskListModel vmDiskListModel = (VmDiskListModel) model;
                DiskModel diskModel = (DiskModel) vmDiskListModel.getWindow();
                VM vm = vmDiskListModel.getEntity();
                storage_domains storageDomain = (storage_domains) result;
                Disk disk = (Disk) vmDiskListModel.getSelectedItem();

                diskModel.getStorageDomain().setSelectedItem(storageDomain);

                DiskImageBase preset = new DiskImage();
                diskModel.getPreset().setSelectedItem(preset);
                diskModel.getPreset().setIsChangable(false);

                if (disk.getDiskStorageType() == DiskStorageType.IMAGE) {
                    diskModel.getVolumeType().setSelectedItem(((DiskImage) disk).getvolume_type());
                    diskModel.getVolumeType().setIsChangable(false);

                    diskModel.setVolumeFormat(((DiskImage) disk).getvolume_format());
                }

                ArrayList<DiskInterface> interfaces =
                        AsyncDataProvider.GetDiskInterfaceList(vm.getVmOs(), vm.getVdsGroupCompatibilityVersion());
                if (!interfaces.contains(disk.getDiskInterface()))
                {
                    interfaces.add(disk.getDiskInterface());
                }
                diskModel.getInterface().setItems(interfaces);
                diskModel.getInterface().setSelectedItem(disk.getDiskInterface());
                // Allow interface type to be edited only if the disk is not sharable
                diskModel.getInterface().setIsChangable(!disk.isShareable());

                storage_domains storage = (storage_domains) diskModel.getStorageDomain().getSelectedItem();

                if (diskModel.getStorageDomain() != null && diskModel.getStorageDomain().getSelectedItem() != null)
                {
                    StorageType storageType = storage.getstorage_type();
                    boolean isFileDomain =
                            storageType == StorageType.NFS || storageType == StorageType.LOCALFS
                                    || storageType == StorageType.POSIXFS;
                    diskModel.getWipeAfterDelete().setIsChangable(!isFileDomain);
                }

                diskModel.getWipeAfterDelete().setEntity(disk.isWipeAfterDelete());

                ArrayList<Disk> disks =
                        vmDiskListModel.getItems() != null ? Linq.<Disk> Cast(vmDiskListModel.getItems())
                                : new ArrayList<Disk>();

                Disk bootableDisk = null;
                for (Disk a : disks)
                {
                    if (a.isBoot())
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
                            .add("There can be only one bootable disk defined."); //$NON-NLS-1$
                }
                diskModel.getIsBootable().setEntity(disk.isBoot());
                diskModel.getIsShareable().setEntity(disk.isShareable());

                diskModel.getAlias().setEntity(disk.getDiskAlias());
                diskModel.getDescription().setEntity(disk.getDiskDescription());

                diskModel.StopProgress();

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

        AsyncDataProvider.GetStorageDomainById(_asyncQuery1, storageDomainId);

        if (disk.getDiskStorageType() == DiskStorageType.IMAGE) {
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
                        dModel.quota_storageSelectedItemChanged(((DiskImage) disk).getQuotaId());
                    }
                }
            }), getEntity().getStoragePoolId());
        }
        else {
            ((DiskModel) getWindow()).getQuota().setIsAvailable(false);
        }
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

        model.getLatch().setEntity(false);

        ArrayList<DiskModel> items = new ArrayList<DiskModel>();
        for (Object item : getSelectedItems())
        {
            Disk disk = (Disk) item;

            DiskModel diskModel = new DiskModel();
            diskModel.setDisk(disk);
            diskModel.getIsInVm().setEntity(true);

            items.add(diskModel);

            // A shared disk can only be detached
            if (disk.getNumberOfVms() > 1) {
                model.getLatch().setIsChangable(false);
            }
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
        VM vm = getEntity();
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
        VM vm = getEntity();
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

        storage_domains storageDomain = (storage_domains) model.getStorageDomain().getSelectedItem();
        Disk disk = (Disk) getSelectedItem();
        boolean isInternal = (Boolean) model.getIsInternal().getEntity();

        if (!model.getIsNew()) {
            model.getIsInternal().setEntity(disk.getDiskStorageType() == DiskStorageType.IMAGE);
        }

        if (isInternal) {
            DiskImage diskImage = model.getIsNew() ? new DiskImage() : (DiskImage) getSelectedItem();
            diskImage.setSizeInGigabytes(Integer.parseInt(model.getSize().getEntity().toString()));
            diskImage.setvolume_type((VolumeType) model.getVolumeType().getSelectedItem());
            diskImage.setvolume_format(model.getVolumeFormat());
            if (model.getQuota().getIsAvailable() && model.getQuota().getSelectedItem() != null) {
                diskImage.setQuotaId(((Quota) model.getQuota().getSelectedItem()).getId());
            }

            disk = diskImage;
        }
        else {
            LunDisk lunDisk;

            if (model.getIsNew()) {
                SanStorageModel sanStorageModel = model.getSanStorageModel();
                ArrayList<String> partOfSdLunsMessages = sanStorageModel.getPartOfSdLunsMessages();

                if (partOfSdLunsMessages.isEmpty() || sanStorageModel.isForce()) {
                    LUNs luns = (LUNs) sanStorageModel.getAddedLuns().get(0).getEntity();
                    luns.setLunType((StorageType) model.getStorageType().getSelectedItem());

                    lunDisk = new LunDisk();
                    lunDisk.setLun(luns);
                }
                else {
                    ForceCreationWarning(partOfSdLunsMessages);
                    return;
                }

            }
            else {
                lunDisk = (LunDisk) getSelectedItem();
            }
            disk = lunDisk;
        }

        disk.setDiskAlias((String) model.getAlias().getEntity());
        disk.setDiskDescription((String) model.getDescription().getEntity());
        disk.setDiskInterface((DiskInterface) model.getInterface().getSelectedItem());
        disk.setWipeAfterDelete((Boolean) model.getWipeAfterDelete().getEntity());
        disk.setBoot((Boolean) model.getIsBootable().getEntity());
        disk.setShareable((Boolean) model.getIsShareable().getEntity());
        disk.setPlugged((Boolean) model.getIsPlugged().getEntity());
        disk.setPropagateErrors(PropagateErrors.Off);

        model.StartProgress(null);

        VdcActionType actionType;
        VmDiskOperationParameterBase parameters;

        if (model.getIsNew())
        {
            actionType = VdcActionType.AddDisk;
            parameters = new AddDiskParameters(vm.getId(), disk);
            if (isInternal) {
                ((AddDiskParameters) parameters).setStorageDomainId(storageDomain.getId());
            }
        }
        else
        {
            actionType = VdcActionType.UpdateVmDisk;
            parameters = new UpdateVmDiskParameters(vm.getId(), disk.getId(), disk);
        }

        Frontend.RunAction(actionType, parameters, new IFrontendActionAsyncCallback() {
            @Override
            public void Executed(FrontendActionAsyncResult result) {
                VmDiskListModel localModel = (VmDiskListModel) result.getState();
                localModel.getWindow().StopProgress();
                Cancel();
            }
        }, this);
    }

    private void ForceCreationWarning(ArrayList<String> usedLunsMessages) {
        ConfirmationModel confirmationModel = new ConfirmationModel();
        setConfirmWindow(confirmationModel);

        confirmationModel.setTitle(ConstantsManager.getInstance().getConstants().forceStorageDomainCreation());
        confirmationModel.setMessage(ConstantsManager.getInstance().getConstants().lunsAlreadyPartOfSD());
        confirmationModel.setHashName("force_lun_disk_creation"); //$NON-NLS-1$
        confirmationModel.setItems(usedLunsMessages);

        UICommand command;
        command = new UICommand("OnForceSave", this); //$NON-NLS-1$
        command.setTitle(ConstantsManager.getInstance().getConstants().ok());
        command.setIsDefault(true);
        confirmationModel.getCommands().add(command);

        command = new UICommand("CancelConfirm", this); //$NON-NLS-1$
        command.setTitle(ConstantsManager.getInstance().getConstants().cancel());
        command.setIsCancel(true);
        confirmationModel.getCommands().add(command);
    }

    private void OnForceSave()
    {
        ConfirmationModel confirmationModel = (ConfirmationModel) getConfirmWindow();
        if (confirmationModel != null && !confirmationModel.Validate())
        {
            return;
        }

        CancelConfirm();

        DiskModel model = (DiskModel) getWindow();
        SanStorageModel sanStorageModel = model.getSanStorageModel();
        sanStorageModel.setForce(true);

        OnSave();
    }

    private void OnAttachDisks()
    {
        VM vm = getEntity();
        DiskModel model = (DiskModel) getWindow();
        ArrayList<VdcActionType> actionTypes = new ArrayList<VdcActionType>();
        ArrayList<VdcActionParametersBase> paramerterList = new ArrayList<VdcActionParametersBase>();
        ArrayList<IFrontendActionAsyncCallback> callbacks = new ArrayList<IFrontendActionAsyncCallback>();

        IFrontendActionAsyncCallback onFinishCallback = new IFrontendActionAsyncCallback() {
            @Override
            public void Executed(FrontendActionAsyncResult result) {
                VmDiskListModel localModel = (VmDiskListModel) result.getState();
                localModel.getWindow().StopProgress();
                Cancel();
            }
        };

        ArrayList<EntityModel> disksToAttach = (Boolean) model.getIsInternal().getEntity() ?
                (ArrayList<EntityModel>) model.getInternalAttachableDisks().getSelectedItems() :
                (ArrayList<EntityModel>) model.getExternalAttachableDisks().getSelectedItems();

        for (int i = 0; i < disksToAttach.size(); i++) {
            DiskModel disk = (DiskModel) disksToAttach.get(i).getEntity();
            AttachDettachVmDiskParameters parameters = new AttachDettachVmDiskParameters(
                    vm.getId(), disk.getDisk().getId(), (Boolean) model.getIsPlugged().getEntity());

            actionTypes.add(VdcActionType.AttachDiskToVm);
            paramerterList.add(parameters);
            callbacks.add(i == disksToAttach.size() - 1 ? onFinishCallback : null);
        }

        model.StartProgress(null);

        Frontend.RunMultipleActions(actionTypes, paramerterList, callbacks, null, this);
    }

    private void Plug(boolean plug) {
        VM vm = getEntity();

        ArrayList<VdcActionParametersBase> paramerterList = new ArrayList<VdcActionParametersBase>();
        for (Object item : getSelectedItems()) {
            Disk disk = (Disk) item;
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

        VM vm = getEntity();

        MoveDiskModel model = new MoveDiskModel();
        setWindow(model);
        model.setVmId(vm.getStatus() == VMStatus.Up ? vm.getId() : null);
        model.setWarningAvailable(vm.getStatus() == VMStatus.Up);
        model.setMessage(vm.getStatus() == VMStatus.Up ?
                ConstantsManager.getInstance().getConstants().liveStorageMigrationWarning() :
                null);
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

    private void CancelConfirm()
    {
        DiskModel model = (DiskModel) getWindow();
        SanStorageModel sanStorageModel = model.getSanStorageModel();
        sanStorageModel.setForce(false);
        setConfirmWindow(null);
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
        VM vm = getEntity();
        Disk disk = (Disk) getSelectedItem();
        boolean isDiskLocked = disk != null && disk.getDiskStorageType() == DiskStorageType.IMAGE &&
                ((DiskImage) disk).getImageStatus() == ImageStatus.LOCKED;

        getNewCommand().setIsExecutionAllowed(true);

        getEditCommand().setIsExecutionAllowed(getSelectedItem() != null && getSelectedItems() != null
                && getSelectedItems().size() == 1 && isVmDown() && !isDiskLocked);

        getRemoveCommand().setIsExecutionAllowed(getSelectedItems() != null && getSelectedItems().size() > 0
                && isRemoveCommandAvailable());

        getMoveCommand().setIsExecutionAllowed(getSelectedItems() != null && getSelectedItems().size() > 0
                && (isMoveCommandAvailable() || isLiveMoveCommandAvailable()));

        getPlugCommand().setIsExecutionAllowed(isPlugCommandAvailable(true));

        getUnPlugCommand().setIsExecutionAllowed(isPlugCommandAvailable(false));

        if (systemTreeContext != null
                && systemTreeContext.getSystemTreeSelectedItem() != null
                && systemTreeContext.getSystemTreeSelectedItem().getType() == SystemTreeItemType.DataCenter
                &&
                ((storage_pool) systemTreeContext.getSystemTreeSelectedItem().getEntity()).getQuotaEnforcementType() != QuotaEnforcementTypeEnum.DISABLED) {
            ArrayList<Disk> disks = getSelectedItems() != null ? (ArrayList<Disk>) getSelectedItems() : null;
            getChangeQuotaCommand().setIsAvailable(true);
            getChangeQuotaCommand().setIsExecutionAllowed(true);
            if (disks != null && !disks.isEmpty()) {
                for (Disk diskItem : disks) {
                    if (diskItem.getDiskStorageType() != DiskStorageType.IMAGE) {
                        getChangeQuotaCommand().setIsExecutionAllowed(false);
                        break;
                    }
                }
            } else {
                getChangeQuotaCommand().setIsExecutionAllowed(false);
            }
        }
    }

    public boolean isVmDown() {
        VM vm = getEntity();
        return vm != null && vm.getStatus() == VMStatus.Down;
    }

    public boolean isHotPlugAvailable() {
        VM vm = getEntity();
        return vm != null && (vm.getStatus() == VMStatus.Up ||
                vm.getStatus() == VMStatus.Paused || vm.getStatus() == VMStatus.Suspended);
    }

    private boolean isPlugCommandAvailable(boolean plug) {
        return getSelectedItems() != null && getSelectedItems().size() > 0
                && isPlugAvailableByDisks(plug) &&
                (isVmDown() || (isHotPlugAvailable() && getIsDiskHotPlugSupported()));
    }

    private boolean isPlugAvailableByDisks(boolean plug) {
        ArrayList<Disk> disks =
                getSelectedItems() != null ? Linq.<Disk> Cast(getSelectedItems()) : new ArrayList<Disk>();

        for (Disk disk : disks)
        {
            boolean isLocked =
                    disk.getDiskStorageType() == DiskStorageType.IMAGE
                            && ((DiskImage) disk).getImageStatus() == ImageStatus.LOCKED;

            if (disk.getPlugged() == plug || isLocked || (disk.getDiskInterface() == DiskInterface.IDE && !isVmDown()))
            {
                return false;
            }
        }

        return true;
    }

    private boolean isImageDiskOK(Disk disk) {
        return disk.getDiskStorageType() == DiskStorageType.IMAGE &&
                ((DiskImage) disk).getImageStatus() == ImageStatus.OK;
    }

    private boolean isMoveCommandAvailable() {
        ArrayList<Disk> disks =
                getSelectedItems() != null ? Linq.<Disk> Cast(getSelectedItems()) : new ArrayList<Disk>();

        for (Disk disk : disks) {
            if (!isImageDiskOK(disk) || (!isVmDown() && disk.getPlugged())) {
                return false;
            }
        }

        return true;
    }

    private boolean isLiveMoveCommandAvailable() {
        if (!getIsLiveStorageMigrationEnabled()) {
            return false;
        }

        VM vm = getEntity();
        if (vm == null || !vm.getStatus().isUpOrPaused()) {
            return false;
        }

        ArrayList<Disk> disks = getSelectedItems() != null ?
                Linq.<Disk> Cast(getSelectedItems()) : new ArrayList<Disk>();

        for (Disk disk : disks) {
            if (!isImageDiskOK(disk)) {
                return false;
            }
        }

        return true;
    }

    private boolean isRemoveCommandAvailable() {
        ArrayList<Disk> disks =
                getSelectedItems() != null ? Linq.<Disk> Cast(getSelectedItems()) : new ArrayList<Disk>();

        for (Disk disk : disks)
        {
            if (disk.getDiskStorageType() == DiskStorageType.IMAGE &&
                    ((DiskImage) disk).getImageStatus() == ImageStatus.LOCKED || (!isVmDown() && disk.getPlugged()))
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
        else if (StringHelper.stringsEqual(command.getName(), "OnForceSave")) //$NON-NLS-1$
        {
            OnForceSave();
        }
        else if (StringHelper.stringsEqual(command.getName(), "Cancel")) //$NON-NLS-1$
        {
            Cancel();
        }
        else if (StringHelper.stringsEqual(command.getName(), "CancelConfirm")) //$NON-NLS-1$
        {
            CancelConfirm();
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
        } else if (command == getChangeQuotaCommand()) {
            changeQuota();
        } else if (command.getName().equals("onChangeQuota")) { //$NON-NLS-1$
            onChangeQuota();
        }
    }

    protected void UpdateIsDiskHotPlugAvailable()
    {
        VM vm = getEntity();
        Version clusterCompatibilityVersion = vm.getVdsGroupCompatibilityVersion();
        if (clusterCompatibilityVersion == null) {
            setIsDiskHotPlugSupported(false);
        } else {
            AsyncDataProvider.IsHotPlugAvailable(new AsyncQuery(this,
                    new INewAsyncCallback() {
                        @Override
                        public void OnSuccess(Object target, Object returnValue) {
                            VmDiskListModel model = (VmDiskListModel) target;
                            model.setIsDiskHotPlugSupported((Boolean) returnValue);
                        }
                    }), clusterCompatibilityVersion.toString());
        }
    }

    protected void UpdateLiveStorageMigrationEnabled()
    {
        VM vm = getEntity();

        AsyncDataProvider.GetDataCenterById(new AsyncQuery(this, new INewAsyncCallback() {
            @Override
            public void OnSuccess(Object target, Object returnValue) {
                VmDiskListModel model = (VmDiskListModel) target;
                VM vm = model.getEntity();

                storage_pool dataCenter = (storage_pool) returnValue;
                Version dcCompatibilityVersion = dataCenter.getcompatibility_version() != null
                        ? dataCenter.getcompatibility_version() : new Version();

                AsyncDataProvider.IsLiveStorageMigrationEnabled(new AsyncQuery(model,
                        new INewAsyncCallback() {
                            @Override
                            public void OnSuccess(Object target, Object returnValue) {
                                VmDiskListModel model = (VmDiskListModel) target;
                                model.setIsLiveStorageMigrationEnabled((Boolean) returnValue);
                            }
                        }), dcCompatibilityVersion.toString());
            }
        }), vm.getStoragePoolId().getValue());

    }

    @Override
    protected String getListName() {
        return "VmDiskListModel"; //$NON-NLS-1$
    }
}
