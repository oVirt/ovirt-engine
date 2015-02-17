package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.common.VdcActionUtils;
import org.ovirt.engine.core.common.action.AttachDetachVmDiskParameters;
import org.ovirt.engine.core.common.action.ChangeQuotaParameters;
import org.ovirt.engine.core.common.action.GetDiskAlignmentParameters;
import org.ovirt.engine.core.common.action.HotPlugDiskToVmParameters;
import org.ovirt.engine.core.common.action.RemoveDiskParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.Disk.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.ImageStatus;
import org.ovirt.engine.core.common.businessentities.LunDisk;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.Linq.DiskByAliasComparer;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.ISupportSystemTreeContext;
import org.ovirt.engine.ui.uicommonweb.models.SystemTreeItemModel;
import org.ovirt.engine.ui.uicommonweb.models.quota.ChangeQuotaItemModel;
import org.ovirt.engine.ui.uicommonweb.models.quota.ChangeQuotaModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.FrontendMultipleActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendMultipleActionAsyncCallback;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

public class VmDiskListModel extends VmDiskListModelBase<VM> {

    private UICommand privateNewCommand;

    public UICommand getNewCommand()
    {
        return privateNewCommand;
    }

    private void setNewCommand(UICommand value)
    {
        privateNewCommand = value;
    }

    private UICommand attachCommand;

    public UICommand getAttachCommand()
    {
        return attachCommand;
    }

    private void setAttachCommand(UICommand value)
    {
        attachCommand = value;
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

    private UICommand privateScanAlignmentCommand;

    public UICommand getScanAlignmentCommand()
    {
        return privateScanAlignmentCommand;
    }

    private void setScanAlignmentCommand(UICommand value)
    {
        privateScanAlignmentCommand = value;
    }

    private boolean privateIsDiskHotPlugSupported;

    public boolean getIsDiskHotPlugSupported()
    {
        VM vm = getEntity();
        boolean isVmStatusApplicableForHotPlug =
                vm != null && (vm.getStatus() == VMStatus.Up || vm.getStatus() == VMStatus.Down ||
                        vm.getStatus() == VMStatus.Paused || vm.getStatus() == VMStatus.Suspended);

        return privateIsDiskHotPlugSupported && isVmStatusApplicableForHotPlug;
    }

    private void setIsDiskHotPlugSupported(boolean value)
    {
        if (privateIsDiskHotPlugSupported != value)
        {
            privateIsDiskHotPlugSupported = value;
            onPropertyChanged(new PropertyChangedEventArgs("IsDiskHotPlugSupported")); //$NON-NLS-1$
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
            onPropertyChanged(new PropertyChangedEventArgs("IsLiveStorageMigrationEnabled")); //$NON-NLS-1$
        }
    }

    public boolean isExtendImageSizeEnabled() {
        return (getEntity() != null) ?
                VdcActionUtils.canExecute(Arrays.asList(getEntity()), VM.class, VdcActionType.ExtendImageSize) : false;
    }

    public VmDiskListModel()
    {
        setTitle(ConstantsManager.getInstance().getConstants().disksTitle());
        setHelpTag(HelpTag.disks);
        setHashName("disks"); //$NON-NLS-1$

        setNewCommand(new UICommand("New", this)); //$NON-NLS-1$
        setAttachCommand(new UICommand("Attach", this)); //$NON-NLS-1$
        setEditCommand(new UICommand("Edit", this)); //$NON-NLS-1$
        setRemoveCommand(new UICommand("Remove", this)); //$NON-NLS-1$
        setPlugCommand(new UICommand("Plug", this)); //$NON-NLS-1$
        setUnPlugCommand(new UICommand("Unplug", this)); //$NON-NLS-1$
        setMoveCommand(new UICommand("Move", this)); //$NON-NLS-1$
        setScanAlignmentCommand(new UICommand("Scan Alignment", this)); //$NON-NLS-1$
        setChangeQuotaCommand(new UICommand("changeQuota", this)); //$NON-NLS-1$
        getChangeQuotaCommand().setIsAvailable(false);

        updateActionAvailability();
    }

    @Override
    protected void onEntityChanged()
    {
        super.onEntityChanged();

        if (getEntity() != null)
        {
            updateDataCenterVersion();
            getSearchCommand().execute();
            updateIsDiskHotPlugAvailable();
            updateLiveStorageMigrationEnabled();
        }

        updateActionAvailability();
    }

    @Override
    protected void syncSearch()
    {
        if (getEntity() == null)
        {
            return;
        }
        VM vm = getEntity();

        super.syncSearch(VdcQueryType.GetAllDisksByVmId, new IdQueryParameters(vm.getId()));
    }

    @Override
    public void setItems(Collection value)
    {
        ArrayList<Disk> disks =
                value != null ? Linq.<Disk> cast(value) : new ArrayList<Disk>();

        Collections.sort(disks, new DiskByAliasComparer());
        super.setItems(disks);

        updateActionAvailability();
    }

    private void newEntity() {

        if (getWindow() != null) {
            return;
        }

        addDisk(new NewDiskModel(),
                ConstantsManager.getInstance().getConstants().newVirtualDiskTitle(),
                HelpTag.new_virtual_disk, "new_virtual_disk"); //$NON-NLS-1$
    }

    private void attach() {
        if (getWindow() != null) {
            return;
        }

        addDisk(new AttachDiskModel(),
                ConstantsManager.getInstance().getConstants().attachVirtualDiskTitle(),
                HelpTag.attach_virtual_disk, "attach_virtual_disk"); //$NON-NLS-1$
    }

    private void addDisk(AbstractDiskModel model, String title, HelpTag helpTag, String hashName) {
        model.setTitle(title);
        model.setHelpTag(helpTag);
        model.setHashName(hashName); //$NON-NLS-1$
        model.setVm(getEntity());
        setWindow(model);

        UICommand cancelCommand = UICommand.createCancelUiCommand("Cancel", this); //$NON-NLS-1$
        model.setCancelCommand(cancelCommand);

        model.initialize();
    }

    private void changeQuota() {
        ArrayList<DiskImage> disks = (ArrayList) getSelectedItems();

        if (disks == null || getWindow() != null)
        {
            return;
        }

        ChangeQuotaModel model = new ChangeQuotaModel();
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().assignQuotaForDisk());
        model.setHelpTag(HelpTag.change_quota_disks);
        model.setHashName("change_quota_disks"); //$NON-NLS-1$
        model.startProgress(null);
        model.init(disks);

        model.getCommands().add(UICommand.createDefaultOkUiCommand("onChangeQuota", this)); //$NON-NLS-1$
        model.getCommands().add(UICommand.createCancelUiCommand("Cancel", this)); //$NON-NLS-1$
    }

    private void onChangeQuota() {
        ChangeQuotaModel model = (ChangeQuotaModel) getWindow();
        ArrayList<VdcActionParametersBase> paramerterList = new ArrayList<VdcActionParametersBase>();

        for (Object item : model.getItems())
        {
            ChangeQuotaItemModel itemModel = (ChangeQuotaItemModel) item;
            DiskImage disk = itemModel.getEntity();
            VdcActionParametersBase parameters =
                    new ChangeQuotaParameters(itemModel.getQuota().getSelectedItem().getId(),
                            disk.getId(),
                            itemModel.getStorageDomainId(),
                            disk.getStoragePoolId());
            paramerterList.add(parameters);
        }

        model.startProgress(null);

        Frontend.getInstance().runMultipleAction(VdcActionType.ChangeQuotaForDisk, paramerterList,
                new IFrontendMultipleActionAsyncCallback() {
                    @Override
                    public void executed(FrontendMultipleActionAsyncResult result) {
                        cancel();
                    }
                },
                this);
    }

    private void edit()
    {
        final Disk disk = getSelectedItem();

        if (getWindow() != null)
        {
            return;
        }

        EditDiskModel model = new EditDiskModel();
        model.setTitle(ConstantsManager.getInstance().getConstants().editVirtualDiskTitle());
        model.setHelpTag(HelpTag.edit_virtual_disk);
        model.setHashName("edit_virtual_disk"); //$NON-NLS-1$
        model.setVm(getEntity());
        model.setDisk(disk);
        setWindow(model);

        UICommand cancelCommand = UICommand.createCancelUiCommand("Cancel", this); //$NON-NLS-1$
        model.setCancelCommand(cancelCommand);

        model.initialize();
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
        model.setHelpTag(HelpTag.remove_disk);
        model.setHashName("remove_disk"); //$NON-NLS-1$

        model.getLatch().setEntity(false);

        ArrayList<DiskModel> items = new ArrayList<DiskModel>();
        for (Disk disk : getSelectedItems()) {

            DiskModel diskModel = new DiskModel();
            diskModel.setDisk(disk);
            diskModel.setVm(getEntity());

            items.add(diskModel);

            // A shared disk or a disk snapshot can only be detached
            if (disk.getNumberOfVms() > 1) {
                model.getLatch().setIsChangable(false);
            }
        }
        model.setItems(items);

        UICommand tempVar = UICommand.createDefaultOkUiCommand("OnRemove", this); //$NON-NLS-1$
        model.getCommands().add(tempVar);
        UICommand tempVar2 = UICommand.createCancelUiCommand("Cancel", this); //$NON-NLS-1$
        model.getCommands().add(tempVar2);
    }

    private void onRemove() {
        VM vm = getEntity();
        RemoveDiskModel model = (RemoveDiskModel) getWindow();
        boolean removeDisk = model.getLatch().getEntity();
        VdcActionType actionType = removeDisk ? VdcActionType.RemoveDisk : VdcActionType.DetachDiskFromVm;
        ArrayList<VdcActionParametersBase> paramerterList = new ArrayList<VdcActionParametersBase>();

        for (Object item : getSelectedItems()) {
            Disk disk = (Disk) item;
            VdcActionParametersBase parameters = removeDisk ?
                    new RemoveDiskParameters(disk.getId()) :
                    new AttachDetachVmDiskParameters(vm.getId(), disk.getId());
            paramerterList.add(parameters);
        }

        model.startProgress(null);

        Frontend.getInstance().runMultipleAction(actionType, paramerterList,
                new IFrontendMultipleActionAsyncCallback() {
                    @Override
                    public void executed(FrontendMultipleActionAsyncResult result) {
                        VmDiskListModel localModel = (VmDiskListModel) result.getState();
                        localModel.stopProgress();
                        cancel();
                    }
                },
                this);
    }

    private void plug() {
        Frontend.getInstance().runMultipleAction(VdcActionType.HotPlugDiskToVm, createHotPlugDiskToVmParameters(true),
                new IFrontendMultipleActionAsyncCallback() {
                    @Override
                    public void executed(FrontendMultipleActionAsyncResult result) {
                    }
                },
                this);
    }

    private void unplug() {
        final ConfirmationModel model = (ConfirmationModel) getWindow();
        model.startProgress(null);

        Frontend.getInstance().runMultipleAction(VdcActionType.HotUnPlugDiskFromVm, createHotPlugDiskToVmParameters(false),
                new IFrontendMultipleActionAsyncCallback() {
                    @Override
                    public void executed(FrontendMultipleActionAsyncResult result) {
                        model.stopProgress();
                        setWindow(null);
                    }
                },
                this);
    }

    private ArrayList<VdcActionParametersBase> createHotPlugDiskToVmParameters(boolean plug) {
        ArrayList<VdcActionParametersBase> parametersList = new ArrayList<VdcActionParametersBase>();
        VM vm = getEntity();

        for (Object item : getSelectedItems()) {
            Disk disk = (Disk) item;
            disk.setPlugged(plug);

            parametersList.add(new HotPlugDiskToVmParameters(vm.getId(), disk.getId()));
        }

        return parametersList;
    }

    private void confirmUnplug() {
        ConfirmationModel model = new ConfirmationModel();
        model.setTitle(ConstantsManager.getInstance().getConstants().deactivateVmDisksTitle());
        model.setMessage(ConstantsManager.getInstance().getConstants().areYouSureYouWantDeactivateVMDisksMsg());
        model.setHashName("deactivate_vm_disk"); //$NON-NLS-1$
        setWindow(model);

        ArrayList<String> items = new ArrayList<String>();
        for (Disk selected : getSelectedItems()) {
            items.add(selected.getDiskAlias());
        }
        model.setItems(items);

        UICommand unPlug = UICommand.createDefaultOkUiCommand("OnUnplug", this); //$NON-NLS-1$
        model.getCommands().add(unPlug);

        UICommand cancel = UICommand.createCancelUiCommand("Cancel", this); //$NON-NLS-1$
        model.getCommands().add(cancel);
    }

    private void move()
    {
        ArrayList<DiskImage> disks = (ArrayList) getSelectedItems();

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
        if (vm.isRunningAndQualifyForDisksMigration()) {
            model.setWarningAvailable(true);
            model.setMessage(ConstantsManager.getInstance().getConstants().liveStorageMigrationWarning());
            model.setMessage(ConstantsManager.getInstance().getConstants().liveStorageMigrationStorageFilteringNote());
        }

        model.setTitle(ConstantsManager.getInstance().getConstants().moveDisksTitle());
        model.setHelpTag(HelpTag.move_disk);
        model.setHashName("move_disk"); //$NON-NLS-1$
        model.setIsSourceStorageDomainNameAvailable(true);
        model.setEntity(this);
        model.init(disks);
        model.startProgress(null);
    }

    private void scanAlignment()
    {
        ArrayList<VdcActionParametersBase> parameterList = new ArrayList<VdcActionParametersBase>();

        for (Disk disk : getSelectedItems())
        {
            parameterList.add(new GetDiskAlignmentParameters(disk.getId()));
        }

        Frontend.getInstance().runMultipleAction(VdcActionType.GetDiskAlignment, parameterList,
                new IFrontendMultipleActionAsyncCallback() {
                    @Override
                    public void executed(FrontendMultipleActionAsyncResult result) {
                    }
                },
                this);
    }

    private void cancel()
    {
        setWindow(null);
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

    @Override
    protected void entityPropertyChanged(Object sender, PropertyChangedEventArgs e)
    {
        super.entityPropertyChanged(sender, e);

        if (e.propertyName.equals("status")) //$NON-NLS-1$
        {
            updateActionAvailability();
        }
    }

    private void updateActionAvailability()
    {
        Disk disk = getSelectedItem();

        getNewCommand().setIsExecutionAllowed(true);

        getAttachCommand().setIsExecutionAllowed(true);

        getEditCommand().setIsExecutionAllowed(disk != null && isSingleDiskSelected() && !isDiskLocked(disk) &&
                (isVmDown() || !disk.getPlugged() || (isExtendImageSizeSupported() && isExtendImageSizeEnabled())));

        getRemoveCommand().setIsExecutionAllowed(atLeastOneDiskSelected() && isRemoveCommandAvailable());

        getMoveCommand().setIsExecutionAllowed(atLeastOneDiskSelected()
                && (isMoveCommandAvailable() || isLiveMoveCommandAvailable()));

        updateScanAlignmentCommandAvailability();

        getPlugCommand().setIsExecutionAllowed(isPlugCommandAvailable(true));

        getUnPlugCommand().setIsExecutionAllowed(isPlugCommandAvailable(false));

        ChangeQuotaModel.updateChangeQuotaActionAvailability(getItems() != null ? (List<Disk>) getItems() : null,
                getSelectedItems() != null ? getSelectedItems() : null,
                getSystemTreeSelectedItem(),
                getChangeQuotaCommand());
    }

    public boolean isVmDown() {
        VM vm = getEntity();
        return vm != null && vm.getStatus() == VMStatus.Down;
    }

    private boolean isDiskLocked(Disk disk) {
        return disk != null && disk.getDiskStorageType() == DiskStorageType.IMAGE &&
                ((DiskImage) disk).getImageStatus() == ImageStatus.LOCKED;
    }

    private boolean isSingleDiskSelected() {
        return getSelectedItems() != null && getSelectedItems().size() == 1;
    }

    private boolean atLeastOneDiskSelected() {
        return getSelectedItems() != null && getSelectedItems().size() > 0;
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
                getSelectedItems() != null ? Linq.<Disk> cast(getSelectedItems()) : new ArrayList<Disk>();

        for (Disk disk : disks)
        {
            boolean isLocked =
                    disk.getDiskStorageType() == DiskStorageType.IMAGE
                            && ((DiskImage) disk).getImageStatus() == ImageStatus.LOCKED;

            boolean isDiskHotpluggableInterface = false;
            if (getEntity() != null) {
                isDiskHotpluggableInterface = AsyncDataProvider.getInstance().getDiskHotpluggableInterfaces(getEntity().getOs(),
                        getEntity().getVdsGroupCompatibilityVersion()).contains(disk.getDiskInterface());
            }

            if (disk.getPlugged() == plug
                    || isLocked
                    || (!isDiskHotpluggableInterface && !isVmDown())) {
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
                getSelectedItems() != null ? Linq.<Disk> cast(getSelectedItems()) : new ArrayList<Disk>();

        for (Disk disk : disks) {
            if (!isImageDiskOK(disk) || (!isVmDown() && disk.getPlugged()) || disk.isDiskSnapshot()) {
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
        if (vm == null || !vm.getStatus().isUpOrPaused() || vm.isStateless()) {
            return false;
        }

        ArrayList<Disk> disks = getSelectedItems() != null ?
                Linq.<Disk> cast(getSelectedItems()) : new ArrayList<Disk>();

        for (Disk disk : disks) {
            if (!isImageDiskOK(disk) || disk.isDiskSnapshot()) {
                return false;
            }
        }

        return true;
    }

    private boolean isRemoveCommandAvailable() {
        ArrayList<Disk> disks =
                getSelectedItems() != null ? Linq.<Disk> cast(getSelectedItems()) : new ArrayList<Disk>();

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

    private void updateScanAlignmentCommandAvailability() {
        boolean isExecutionAllowed = true;
        if (getSelectedItems() != null && getEntity() != null) {
            ArrayList<Disk> disks = Linq.<Disk> cast(getSelectedItems());
            for (Disk disk : disks) {

                if (!(disk instanceof LunDisk) && !isDiskOnBlockDevice(disk)) {
                    isExecutionAllowed = false;
                    break;
                }
            }
        }
        else {
            isExecutionAllowed = false;
        }
        getScanAlignmentCommand().setIsExecutionAllowed(isExecutionAllowed);
        //onPropertyChanged(new PropertyChangedEventArgs("IsScanAlignmentEnabled")); //$NON-NLS-1$
    }

    @Override
    public void executeCommand(UICommand command)
    {
        super.executeCommand(command);

        if (command == getNewCommand())
        {
            newEntity();
        }
        else if (command == getAttachCommand())
        {
            attach();
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
        else if (command == getScanAlignmentCommand())
        {
            scanAlignment();
        }
        else if ("Cancel".equals(command.getName())) //$NON-NLS-1$
        {
            cancel();
        }
        else if ("OnRemove".equals(command.getName())) //$NON-NLS-1$
        {
            onRemove();
        }
        else if (command == getPlugCommand())
        {
            plug();
        }
        else if (command == getUnPlugCommand())
        {
            confirmUnplug();
        }
        else if ("OnUnplug".equals(command.getName())) //$NON-NLS-1$
        {
            unplug();
        }

        else if (command == getChangeQuotaCommand()) {
            changeQuota();
        } else if (command.getName().equals("onChangeQuota")) { //$NON-NLS-1$
            onChangeQuota();
        }
    }

    protected void updateIsDiskHotPlugAvailable()
    {
        VM vm = getEntity();
        Version clusterCompatibilityVersion = vm.getVdsGroupCompatibilityVersion();
        if (clusterCompatibilityVersion == null) {
            setIsDiskHotPlugSupported(false);
        } else {
            setIsDiskHotPlugSupported(!AsyncDataProvider.getInstance().getDiskHotpluggableInterfaces(
                    getEntity().getOs(), clusterCompatibilityVersion).isEmpty());
        }
    }

    protected void updateLiveStorageMigrationEnabled()
    {
        final VM vm = getEntity();

        AsyncDataProvider.getInstance().getDataCenterById(new AsyncQuery(this, new INewAsyncCallback() {
            @Override
            public void onSuccess(Object target, Object returnValue) {
                VmDiskListModel model = (VmDiskListModel) target;

                StoragePool dataCenter = (StoragePool) returnValue;
                Version dcCompatibilityVersion = dataCenter.getCompatibilityVersion() != null
                        ? dataCenter.getCompatibilityVersion() : new Version();

                AsyncDataProvider.getInstance().isCommandCompatible(new AsyncQuery(model,
                        new INewAsyncCallback() {
                            @Override
                            public void onSuccess(Object target, Object returnValue) {
                                VmDiskListModel model = (VmDiskListModel) target;
                                model.setIsLiveStorageMigrationEnabled((Boolean) returnValue);
                            }
                        }),
                        VdcActionType.LiveMigrateVmDisks,
                        vm.getVdsGroupCompatibilityVersion(),
                        dcCompatibilityVersion);
            }
        }), vm.getStoragePoolId());
    }

    private boolean isDiskOnBlockDevice(Disk disk) {
        if (disk instanceof DiskImage) {

            List<StorageType> diskStorageTypes = ((DiskImage) disk).getStorageTypes();
            for (StorageType type : diskStorageTypes) {
                if (!type.isBlockDomain()) {
                    return false;
                }
            }
            return true;
        }
        return false; // Should never happen but we might add other Disk types in the future
    }

    protected void updateDataCenterVersion()
    {
        AsyncQuery query = new AsyncQuery(this, new INewAsyncCallback() {
            @Override
            public void onSuccess(Object target, Object returnValue) {
                VmDiskListModel model = (VmDiskListModel) target;
                StoragePool storagePool = (StoragePool) returnValue;
                model.setDataCenterVersion(storagePool.getCompatibilityVersion());
            }
        });
        AsyncDataProvider.getInstance().getDataCenterById(query, getEntity().getStoragePoolId());
    }

    protected void updateExtendImageSizeSupported()
    {
        VM vm = getEntity();
        AsyncQuery query = new AsyncQuery(this, new INewAsyncCallback()
        {
            @Override
            public void onSuccess(Object target, Object returnValue)
            {
                VmDiskListModel model = (VmDiskListModel) target;
                model.setExtendImageSizeSupported((Boolean) returnValue);
            }
        });
        AsyncDataProvider.getInstance().isCommandCompatible(query, VdcActionType.ExtendImageSize,
                vm.getVdsGroupCompatibilityVersion(), dataCenterVersion);
    }


    private Version dataCenterVersion;

    public Version getDataCenterVersion()
    {
        return dataCenterVersion;
    }

    public void setDataCenterVersion(Version dataCenterVersion)
    {
        if (dataCenterVersion != null && !dataCenterVersion.equals(this.dataCenterVersion))
        {
            this.dataCenterVersion = dataCenterVersion;
            updateExtendImageSizeSupported();
        }
    }

    private boolean extendImageSizeSupported = true;

    public boolean isExtendImageSizeSupported()
    {
        return extendImageSizeSupported;
    }

    public void setExtendImageSizeSupported(boolean extendImageSizeSupported)
    {
        if (this.extendImageSizeSupported != extendImageSizeSupported) {
            this.extendImageSizeSupported = extendImageSizeSupported;
            updateActionAvailability();
        }
    }

    @Override
    protected String getListName() {
        return "VmDiskListModel"; //$NON-NLS-1$
    }

    public SystemTreeItemModel getSystemTreeSelectedItem() {
        if (getSystemTreeContext() == null) {
            return null;
        }
        return getSystemTreeContext().getSystemTreeSelectedItem();
    }
}
