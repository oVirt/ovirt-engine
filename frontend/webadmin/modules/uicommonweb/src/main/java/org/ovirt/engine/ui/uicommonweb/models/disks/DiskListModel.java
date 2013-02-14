package org.ovirt.engine.ui.uicommonweb.models.disks;

import java.util.ArrayList;

import org.ovirt.engine.core.common.action.AddDiskParameters;
import org.ovirt.engine.core.common.action.AttachDettachVmDiskParameters;
import org.ovirt.engine.core.common.action.ChangeQuotaParameters;
import org.ovirt.engine.core.common.action.RemoveDiskParameters;
import org.ovirt.engine.core.common.action.UpdateVmDiskParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VmDiskOperationParameterBase;
import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.Disk.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.DiskInterface;
import org.ovirt.engine.core.common.businessentities.ImageStatus;
import org.ovirt.engine.core.common.businessentities.LUNs;
import org.ovirt.engine.core.common.businessentities.LunDisk;
import org.ovirt.engine.core.common.businessentities.PropagateErrors;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.QuotaEnforcementTypeEnum;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmEntityType;
import org.ovirt.engine.core.common.businessentities.VolumeType;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.core.common.queries.SearchParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.searchbackend.SearchObjects;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ISupportSystemTreeContext;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.ListWithDetailsModel;
import org.ovirt.engine.ui.uicommonweb.models.SystemTreeItemModel;
import org.ovirt.engine.ui.uicommonweb.models.SystemTreeItemType;
import org.ovirt.engine.ui.uicommonweb.models.configure.PermissionListModel;
import org.ovirt.engine.ui.uicommonweb.models.quota.ChangeQuotaItemModel;
import org.ovirt.engine.ui.uicommonweb.models.quota.ChangeQuotaModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.SanStorageModel;
import org.ovirt.engine.ui.uicommonweb.models.templates.CopyDiskModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.DiskModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.MoveDiskModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.RemoveDiskModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.FrontendMultipleActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendMultipleActionAsyncCallback;
import org.ovirt.engine.ui.uicompat.ObservableCollection;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

@SuppressWarnings("unused")
public class DiskListModel extends ListWithDetailsModel implements ISupportSystemTreeContext
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

    private UICommand privateMoveCommand;

    public UICommand getMoveCommand()
    {
        return privateMoveCommand;
    }

    private void setMoveCommand(UICommand value)
    {
        privateMoveCommand = value;
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

    private UICommand privateCopyCommand;

    public UICommand getCopyCommand()
    {
        return privateCopyCommand;
    }

    private void setCopyCommand(UICommand value)
    {
        privateCopyCommand = value;
    }

    private EntityModel diskViewType;

    public EntityModel getDiskViewType() {
        return diskViewType;
    }

    public void setDiskViewType(EntityModel diskViewType) {
        this.diskViewType = diskViewType;
    }

    private SystemTreeItemModel systemTreeSelectedItem;

    @Override
    public SystemTreeItemModel getSystemTreeSelectedItem()
    {
        return systemTreeSelectedItem;
    }

    @Override
    public void setSystemTreeSelectedItem(SystemTreeItemModel value)
    {
        systemTreeSelectedItem = value;
        OnPropertyChanged(new PropertyChangedEventArgs("SystemTreeSelectedItem")); //$NON-NLS-1$
    }

    private ListModel diskVmListModel;
    private ListModel diskTemplateListModel;
    private ListModel diskStorageListModel;

    public DiskListModel()
    {
        setTitle(ConstantsManager.getInstance().getConstants().disksTitle());

        setDefaultSearchString("Disks: disk_type = image"); //$NON-NLS-1$
        setSearchString(getDefaultSearchString());
        setSearchObjects(new String[] { SearchObjects.DISK_OBJ_NAME, SearchObjects.DISK_PLU_OBJ_NAME });
        setAvailableInModes(ApplicationMode.VirtOnly);

        setNewCommand(new UICommand("New", this)); //$NON-NLS-1$
        setEditCommand(new UICommand("Edit", this)); //$NON-NLS-1$
        setRemoveCommand(new UICommand("Remove", this)); //$NON-NLS-1$
        setMoveCommand(new UICommand("Move", this)); //$NON-NLS-1$
        setChangeQuotaCommand(new UICommand("changeQuota", this)); //$NON-NLS-1$
        setCopyCommand(new UICommand("Copy", this)); //$NON-NLS-1$

        UpdateActionAvailability();

        getSearchNextPageCommand().setIsAvailable(true);
        getSearchPreviousPageCommand().setIsAvailable(true);

        setDiskViewType(new EntityModel());
    }

    @Override
    protected void SyncSearch()
    {
        SearchParameters tempVar = new SearchParameters(getSearchString(), SearchType.Disk);
        tempVar.setMaxCount(getSearchPageSize());
        super.SyncSearch(VdcQueryType.Search, tempVar);
    }

    @Override
    public void setItems(Iterable value)
    {
        if (value == null) {
            super.setItems(null);
            return;
        }

        ArrayList<Disk> disks = Linq.<Disk> Cast(value);
        super.setItems(disks);
    }

    @Override
    protected void InitDetailModels()
    {
        super.InitDetailModels();

        diskVmListModel = new DiskVmListModel();
        diskVmListModel.setIsAvailable(false);

        diskTemplateListModel = new DiskTemplateListModel();
        diskTemplateListModel.setIsAvailable(false);

        diskStorageListModel = new DiskStorageListModel();
        diskStorageListModel.setIsAvailable(false);

        ObservableCollection<EntityModel> list = new ObservableCollection<EntityModel>();
        list.add(new DiskGeneralModel());
        list.add(diskVmListModel);
        list.add(diskTemplateListModel);
        list.add(diskStorageListModel);
        list.add(new PermissionListModel());
        setDetailModels(list);
    }

    @Override
    protected void UpdateDetailsAvailability()
    {
        if (getSelectedItem() != null)
        {
            Disk disk = (Disk) getSelectedItem();

            diskVmListModel.setIsAvailable(disk.getVmEntityType() != VmEntityType.TEMPLATE);
            diskTemplateListModel.setIsAvailable(disk.getVmEntityType() == VmEntityType.TEMPLATE);
            diskStorageListModel.setIsAvailable(disk.getDiskStorageType() == DiskStorageType.IMAGE);
        }
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

    private void New()
    {
        DiskModel model = new DiskModel(getSystemTreeSelectedItem());
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().addVirtualDiskTitle());
        model.setHashName("new_virtual_disk"); //$NON-NLS-1$
        model.setIsNew(true);
        model.getIsInVm().setEntity(false);
        model.getIsInternal().setEntity(true);

        ArrayList<UICommand> commands = new ArrayList<UICommand>();
        UICommand tempVar2 = new UICommand("OnSave", this); //$NON-NLS-1$
        tempVar2.setTitle(ConstantsManager.getInstance().getConstants().ok());
        tempVar2.setIsDefault(true);
        model.getCommands().add(tempVar2);
        UICommand tempVar3 = new UICommand("Cancel", this); //$NON-NLS-1$
        tempVar3.setTitle(ConstantsManager.getInstance().getConstants().cancel());
        tempVar3.setIsCancel(true);
        model.getCommands().add(tempVar3);
    }

    private void Edit()
    {

    }

    private void OnSave()
    {
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

        StorageDomain storageDomain = (StorageDomain) model.getStorageDomain().getSelectedItem();
        Disk disk;
        boolean isInternal = (Boolean) model.getIsInternal().getEntity();

        if (isInternal) {
            DiskImage diskImage = model.getIsNew() ? new DiskImage() : (DiskImage) getSelectedItem();
            diskImage.setSizeInGigabytes(Integer.parseInt(model.getSize().getEntity().toString()));
            diskImage.setVolumeType((VolumeType) model.getVolumeType().getSelectedItem());
            diskImage.setvolumeFormat(model.getVolumeFormat());
            if (model.getQuota().getIsAvailable()) {
                diskImage.setQuotaId(((Quota) model.getQuota().getSelectedItem()).getId());
            }

            disk = diskImage;
        }
        else {
            LunDisk lunDisk;
            SanStorageModel sanStorageModel = model.getSanStorageModel();
            ArrayList<String> partOfSdLunsMessages = sanStorageModel.getPartOfSdLunsMessages();

            if (model.getIsNew()) {
                if (partOfSdLunsMessages.isEmpty() || sanStorageModel.isForce()) {
                    LUNs luns = (LUNs) model.getSanStorageModel().getAddedLuns().get(0).getEntity();
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

        VdcActionType actionType;
        VmDiskOperationParameterBase parameters;
        if (model.getIsNew())
        {
            actionType = VdcActionType.AddDisk;
            parameters = new AddDiskParameters(Guid.Empty, disk);
            if (isInternal) {
                ((AddDiskParameters) parameters).setStorageDomainId(storageDomain.getId());
            }
        }
        else
        {
            actionType = VdcActionType.UpdateVmDisk;
            parameters = new UpdateVmDiskParameters(Guid.Empty, disk.getId(), disk);
        }

        model.StartProgress(null);

        ArrayList<VdcActionParametersBase> paramerterList = new ArrayList<VdcActionParametersBase>();
        paramerterList.add(parameters);

        Frontend.RunMultipleAction(actionType, paramerterList,
                new IFrontendMultipleActionAsyncCallback() {
                    @Override
                    public void Executed(FrontendMultipleActionAsyncResult result) {
                        DiskListModel localModel = (DiskListModel) result.getState();
                        localModel.getWindow().StopProgress();
                        Cancel();
                    }
                },
                this);
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
        VM vm = (VM) getEntity();
        DiskModel model = (DiskModel) getWindow();
        ArrayList<VdcActionParametersBase> paramerterList = new ArrayList<VdcActionParametersBase>();

        ArrayList<EntityModel> disksToAttach = (Boolean) model.getIsInternal().getEntity() ?
                (ArrayList<EntityModel>) model.getInternalAttachableDisks().getSelectedItems() :
                (ArrayList<EntityModel>) model.getExternalAttachableDisks().getSelectedItems();

        for (EntityModel item : disksToAttach)
        {
            DiskModel disk = (DiskModel) item.getEntity();
            AttachDettachVmDiskParameters parameters = new AttachDettachVmDiskParameters(
                    vm.getId(), disk.getDisk().getId(), (Boolean) model.getIsPlugged().getEntity());
            paramerterList.add(parameters);
        }

        model.StartProgress(null);

        Frontend.RunMultipleAction(VdcActionType.AttachDiskToVm, paramerterList,
                new IFrontendMultipleActionAsyncCallback() {
                    @Override
                    public void Executed(FrontendMultipleActionAsyncResult result) {
                        DiskListModel localModel = (DiskListModel) result.getState();
                        localModel.getWindow().StopProgress();
                        Cancel();
                    }
                },
                this);
    }

    private void Move()
    {
        ArrayList<DiskImage> disks = (ArrayList<DiskImage>) getSelectedItems();

        if (disks == null || getWindow() != null)
        {
            return;
        }

        MoveDiskModel model = new MoveDiskModel();
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().moveDisksTitle());
        model.setHashName("move_disks"); //$NON-NLS-1$
        model.setIsSourceStorageDomainNameAvailable(true);
        model.setEntity(this);
        model.init(disks);
        model.StartProgress(null);
    }

    private void changeQuota()
    {
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
                            disk.getStorageIds().get(0),
                            disk.getStoragePoolId().getValue());
            paramerterList.add(parameters);
        }

        model.StartProgress(null);

        Frontend.RunMultipleAction(VdcActionType.ChangeQuotaForDisk, paramerterList,
                new IFrontendMultipleActionAsyncCallback() {
                    @Override
                    public void Executed(FrontendMultipleActionAsyncResult result) {
                        DiskListModel localModel = (DiskListModel) result.getState();
                        localModel.StopProgress();
                        Cancel();
                    }
                },
                this);
    }

    private void Copy()
    {
        ArrayList<DiskImage> disks = (ArrayList<DiskImage>) getSelectedItems();

        if (disks == null || getWindow() != null)
        {
            return;
        }

        CopyDiskModel model = new CopyDiskModel();
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().copyDisksTitle());
        model.setHashName("copy_disks"); //$NON-NLS-1$
        model.setEntity(this);
        model.init(disks);
        model.StartProgress(null);
    }

    private void Remove()
    {
        if (getWindow() != null)
        {
            return;
        }

        RemoveDiskModel model = new RemoveDiskModel();
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().removeDisksTitle());
        model.setHashName("remove_disk"); //$NON-NLS-1$
        model.setMessage(ConstantsManager.getInstance().getConstants().disksMsg());

        model.getLatch().setIsAvailable(false);

        ArrayList<DiskModel> items = new ArrayList<DiskModel>();
        for (Object item : getSelectedItems())
        {
            Disk disk = (Disk) item;

            DiskModel diskModel = new DiskModel();
            diskModel.setDisk(disk);
            diskModel.getIsInVm().setEntity(false);

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

    private void OnRemove()
    {
        VM vm = (VM) getEntity();
        RemoveDiskModel model = (RemoveDiskModel) getWindow();
        ArrayList<VdcActionParametersBase> paramerterList = new ArrayList<VdcActionParametersBase>();

        for (Object item : getSelectedItems())
        {
            Disk disk = (Disk) item;
            VdcActionParametersBase parameters = new RemoveDiskParameters(disk.getId());
            paramerterList.add(parameters);
        }

        model.StartProgress(null);

        Frontend.RunMultipleAction(VdcActionType.RemoveDisk, paramerterList,
                new IFrontendMultipleActionAsyncCallback() {
                    @Override
                    public void Executed(FrontendMultipleActionAsyncResult result) {
                        DiskListModel localModel = (DiskListModel) result.getState();
                        localModel.StopProgress();
                        Cancel();
                    }
                },
                this);
    }

    private void UpdateActionAvailability()
    {
        VM vm = (VM) getEntity();
        Disk disk = (Disk) getSelectedItem();
        ArrayList<Disk> disks = getSelectedItems() != null ? (ArrayList<Disk>) getSelectedItems() : null;
        boolean isDiskLocked = disk != null && disk.getDiskStorageType() == DiskStorageType.IMAGE &&
                ((DiskImage) disk).getImageStatus() == ImageStatus.LOCKED;

        getNewCommand().setIsExecutionAllowed(true);
        getEditCommand().setIsExecutionAllowed(disk != null && disks != null && disks.size() == 1 && !isDiskLocked);
        getRemoveCommand().setIsExecutionAllowed(disks != null && disks.size() > 0 && isRemoveCommandAvailable());
        getMoveCommand().setIsExecutionAllowed(disks != null && disks.size() > 0 && isMoveCommandAvailable());
        getCopyCommand().setIsExecutionAllowed(disks != null && disks.size() > 0 && isCopyCommandAvailable());
        getChangeQuotaCommand().setIsAvailable(false);
        if (getSystemTreeSelectedItem() != null
                && getSystemTreeSelectedItem().getType() == SystemTreeItemType.DataCenter
                &&
                ((storage_pool) getSystemTreeSelectedItem().getEntity()).getQuotaEnforcementType() != QuotaEnforcementTypeEnum.DISABLED) {
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

    private boolean isMoveCommandAvailable() {
        ArrayList<Disk> disks =
                getSelectedItems() != null ? Linq.<Disk> Cast(getSelectedItems()) : new ArrayList<Disk>();

        Disk firstDisk = disks.get(0);
        if (firstDisk.getDiskStorageType() != DiskStorageType.IMAGE) {
            return false;
        }

        NGuid datacenterId = ((DiskImage) firstDisk).getStoragePoolId();

        for (Disk disk : disks)
        {
            if (disk.getDiskStorageType() != DiskStorageType.IMAGE) {
                return false;
            }

            DiskImage diskImage = (DiskImage) disk;
            if (disk.getDiskStorageType() != DiskStorageType.IMAGE ||
                    diskImage.getImageStatus() != ImageStatus.OK ||
                    disk.getVmEntityType() == VmEntityType.TEMPLATE ||
                    !datacenterId.equals(diskImage.getStoragePoolId()))
            {
                return false;
            }
        }

        return true;
    }

    private boolean isCopyCommandAvailable() {
        ArrayList<Disk> disks =
                getSelectedItems() != null ? Linq.<Disk> Cast(getSelectedItems()) : new ArrayList<Disk>();

        Disk firstDisk = disks.get(0);
        if (firstDisk.getDiskStorageType() != DiskStorageType.IMAGE) {
            return false;
        }

        NGuid datacenterId = ((DiskImage) firstDisk).getStoragePoolId();

        for (Disk disk : disks)
        {
            DiskImage diskImage = (DiskImage) disk;
            if (diskImage.getImageStatus() != ImageStatus.OK || disk.getVmEntityType() != VmEntityType.TEMPLATE ||
                    !datacenterId.equals(diskImage.getStoragePoolId()))
            {
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
            boolean isTemplateDisk = disk.getVmEntityType() == VmEntityType.TEMPLATE;
            boolean isImageLocked = disk.getDiskStorageType() == DiskStorageType.IMAGE
                    && ((DiskImage) disk).getImageStatus() == ImageStatus.LOCKED;

            if (isTemplateDisk || isImageLocked)
            {
                return false;
            }
        }

        return true;
    }

    private void CancelConfirm()
    {
        DiskModel model = (DiskModel) getWindow();
        SanStorageModel sanStorageModel = model.getSanStorageModel();
        sanStorageModel.setForce(false);
        setConfirmWindow(null);
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
            Remove();
        }
        else if (command == getMoveCommand())
        {
            Move();
        }
        else if (command == getCopyCommand())
        {
            Copy();
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
        } else if (command == getChangeQuotaCommand()) {
            changeQuota();
        } else if (command.getName().equals("onChangeQuota")) { //$NON-NLS-1$
            onChangeQuota();
        }
    }

    @Override
    public boolean IsSearchStringMatch(String searchString)
    {
        return searchString.trim().toLowerCase().startsWith("disk"); //$NON-NLS-1$
    }

    @Override
    protected String getListName() {
        return "DiskListModel"; //$NON-NLS-1$
    }
}
