package org.ovirt.engine.ui.uicommonweb.models.disks;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.action.ChangeQuotaParameters;
import org.ovirt.engine.core.common.action.RemoveDiskParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.Disk.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.ImageStatus;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.VmEntityType;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.core.common.queries.SearchParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.searchbackend.SearchObjects;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ISupportSystemTreeContext;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.ListWithDetailsModel;
import org.ovirt.engine.ui.uicommonweb.models.SystemTreeItemModel;
import org.ovirt.engine.ui.uicommonweb.models.configure.PermissionListModel;
import org.ovirt.engine.ui.uicommonweb.models.quota.ChangeQuotaItemModel;
import org.ovirt.engine.ui.uicommonweb.models.quota.ChangeQuotaModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.SanStorageModel;
import org.ovirt.engine.ui.uicommonweb.models.templates.CopyDiskModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.AbstractDiskModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.DiskModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.MoveDiskModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.NewDiskModel;
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

    @Override
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
        onPropertyChanged(new PropertyChangedEventArgs("SystemTreeSelectedItem")); //$NON-NLS-1$
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

        updateActionAvailability();

        getSearchNextPageCommand().setIsAvailable(true);
        getSearchPreviousPageCommand().setIsAvailable(true);

        setDiskViewType(new EntityModel());
    }

    @Override
    protected void syncSearch()
    {
        SearchParameters tempVar = new SearchParameters(getSearchString(), SearchType.Disk);
        tempVar.setMaxCount(getSearchPageSize());
        super.syncSearch(VdcQueryType.Search, tempVar);
    }

    @Override
    public void setItems(Iterable value)
    {
        if (value == null) {
            super.setItems(null);
            return;
        }

        ArrayList<Disk> disks = Linq.<Disk> cast(value);
        super.setItems(disks);
    }

    @Override
    protected void initDetailModels()
    {
        super.initDetailModels();

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
    protected void updateDetailsAvailability()
    {
        if (getSelectedItem() != null)
        {
            Disk disk = (Disk) getSelectedItem();

            diskVmListModel.setIsAvailable(disk.getVmEntityType() != VmEntityType.TEMPLATE);
            diskTemplateListModel.setIsAvailable(disk.getVmEntityType() == VmEntityType.TEMPLATE);
            diskStorageListModel.setIsAvailable(disk.getDiskStorageType() == DiskStorageType.IMAGE);
        }
    }

    public void cancel()
    {
        setWindow(null);
        Frontend.Unsubscribe();
    }

    @Override
    protected void onSelectedItemChanged()
    {
        super.onSelectedItemChanged();
        updateActionAvailability();
    }

    @Override
    protected void selectedItemsChanged()
    {
        super.selectedItemsChanged();
        updateActionAvailability();
    }

    private void newEntity()
    {
        NewDiskModel model = new NewDiskModel(getSystemTreeSelectedItem());
        model.setTitle(ConstantsManager.getInstance().getConstants().addVirtualDiskTitle());
        model.setHashName("new_virtual_disk"); //$NON-NLS-1$
        setWindow(model);

        UICommand cancelCommand = new UICommand("Cancel", this); //$NON-NLS-1$
        cancelCommand.setTitle(ConstantsManager.getInstance().getConstants().cancel());
        cancelCommand.setIsCancel(true);
        model.setCancelCommand(cancelCommand);

        model.initialize();
    }

    private void edit()
    {

    }

    private void move()
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
        model.startProgress(null);
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
        model.startProgress(null);
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
                            disk.getStoragePoolId());
            paramerterList.add(parameters);
        }

        model.startProgress(null);

        Frontend.RunMultipleAction(VdcActionType.ChangeQuotaForDisk, paramerterList,
                new IFrontendMultipleActionAsyncCallback() {
                    @Override
                    public void executed(FrontendMultipleActionAsyncResult result) {
                        DiskListModel localModel = (DiskListModel) result.getState();
                        localModel.stopProgress();
                        cancel();
                    }
                },
                this);
    }

    private void copy()
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
        model.startProgress(null);
    }

    private void remove()
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

    private void onRemove()
    {
        RemoveDiskModel model = (RemoveDiskModel) getWindow();
        ArrayList<VdcActionParametersBase> paramerterList = new ArrayList<VdcActionParametersBase>();

        for (Object item : getSelectedItems())
        {
            Disk disk = (Disk) item;
            VdcActionParametersBase parameters = new RemoveDiskParameters(disk.getId());
            paramerterList.add(parameters);
        }

        model.startProgress(null);

        Frontend.RunMultipleAction(VdcActionType.RemoveDisk, paramerterList,
                new IFrontendMultipleActionAsyncCallback() {
                    @Override
                    public void executed(FrontendMultipleActionAsyncResult result) {
                        DiskListModel localModel = (DiskListModel) result.getState();
                        localModel.stopProgress();
                        cancel();
                    }
                },
                this);
    }

    private void updateActionAvailability()
    {
        Disk disk = (Disk) getSelectedItem();
        ArrayList<Disk> disks = getSelectedItems() != null ? (ArrayList<Disk>) getSelectedItems() : null;
        boolean isDiskLocked = disk != null && disk.getDiskStorageType() == DiskStorageType.IMAGE &&
                ((DiskImage) disk).getImageStatus() == ImageStatus.LOCKED;

        getNewCommand().setIsExecutionAllowed(true);
        getEditCommand().setIsExecutionAllowed(disk != null && disks != null && disks.size() == 1 && !isDiskLocked);
        getRemoveCommand().setIsExecutionAllowed(disks != null && disks.size() > 0 && isRemoveCommandAvailable());
        updateCopyAndMoveCommandAvailability(disks);

        ChangeQuotaModel.updateChangeQuotaActionAvailability(getItems() != null ? (List<Disk>) getItems() : null,
                getSelectedItems() != null ? (List<Disk>) getSelectedItems() : null,
                getSystemTreeSelectedItem(),
                getChangeQuotaCommand());
    }

    private void updateCopyAndMoveCommandAvailability(List<Disk> disks) {
        boolean isCopyAllowed = true;
        boolean isMoveAllowed = true;

        if (disks == null || disks.isEmpty() || disks.get(0).getDiskStorageType() != DiskStorageType.IMAGE) {
            disableMoveAndCopyCommands();
            return;
        }

        Guid datacenterId = ((DiskImage) disks.get(0)).getStoragePoolId();

        for (Disk disk : disks) {
            if ((!isCopyAllowed && !isMoveAllowed) || disk.getDiskStorageType() != DiskStorageType.IMAGE) {
                disableMoveAndCopyCommands();
                return;
            }

            DiskImage diskImage = (DiskImage) disk;
            if (diskImage.getImageStatus() != ImageStatus.OK || !datacenterId.equals(diskImage.getStoragePoolId())) {
                disableMoveAndCopyCommands();
                return;
            }

            if (disk.getVmEntityType() == VmEntityType.TEMPLATE) {
                isMoveAllowed = false;
            }
            else {
                isCopyAllowed = false;
            }
        }

        getCopyCommand().setIsExecutionAllowed(isCopyAllowed);
        getMoveCommand().setIsExecutionAllowed(isMoveAllowed);
    }

    private void disableMoveAndCopyCommands() {
        getCopyCommand().setIsExecutionAllowed(false);
        getMoveCommand().setIsExecutionAllowed(false);
    }

    private boolean isRemoveCommandAvailable() {
        ArrayList<Disk> disks =
                getSelectedItems() != null ? Linq.<Disk> cast(getSelectedItems()) : new ArrayList<Disk>();

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

    private void cancelConfirm()
    {
        AbstractDiskModel model = (AbstractDiskModel) getWindow();
        SanStorageModel sanStorageModel = model.getSanStorageModel();
        sanStorageModel.setForce(false);
        setConfirmWindow(null);
    }

    @Override
    public void executeCommand(UICommand command)
    {
        super.executeCommand(command);

        if (command == getNewCommand())
        {
            newEntity();
        }
        else if (command == getEditCommand())
        {
            edit();
        }
        else if (command == getRemoveCommand())
        {
            remove();
        }
        else if (command == getMoveCommand())
        {
            move();
        }
        else if (command == getCopyCommand())
        {
            copy();
        }
        else if (StringHelper.stringsEqual(command.getName(), "Cancel")) //$NON-NLS-1$
        {
            cancel();
        }
        else if (StringHelper.stringsEqual(command.getName(), "CancelConfirm")) //$NON-NLS-1$
        {
            cancelConfirm();
        }
        else if (StringHelper.stringsEqual(command.getName(), "OnRemove")) //$NON-NLS-1$
        {
            onRemove();
        } else if (command == getChangeQuotaCommand()) {
            changeQuota();
        } else if (command.getName().equals("onChangeQuota")) { //$NON-NLS-1$
            onChangeQuota();
        }
    }

    @Override
    public boolean isSearchStringMatch(String searchString)
    {
        return searchString.trim().toLowerCase().startsWith("disk"); //$NON-NLS-1$
    }

    @Override
    protected String getListName() {
        return "DiskListModel"; //$NON-NLS-1$
    }
}
