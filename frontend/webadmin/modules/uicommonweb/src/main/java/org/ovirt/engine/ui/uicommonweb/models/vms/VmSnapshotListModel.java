package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.ovirt.engine.core.common.VdcActionUtils;
import org.ovirt.engine.core.common.action.AddVmFromSnapshotParameters;
import org.ovirt.engine.core.common.action.RemoveSnapshotParameters;
import org.ovirt.engine.core.common.action.RestoreAllSnapshotsParameters;
import org.ovirt.engine.core.common.action.TryBackToAllSnapshotsOfVmParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.MigrationSupport;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotStatus;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotType;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.UsbPolicy;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.VmType;
import org.ovirt.engine.core.common.businessentities.VolumeType;
import org.ovirt.engine.core.common.queries.ConfigurationValues;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.Linq.SnapshotByCreationDateCommparer;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicommonweb.models.SystemTreeItemModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.FrontendActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

@SuppressWarnings("unused")
public class VmSnapshotListModel extends SearchableListModel
{

    private UICommand privateNewCommand;

    public UICommand getNewCommand() {
        return privateNewCommand;
    }

    private void setNewCommand(UICommand value)
    {
        privateNewCommand = value;
    }

    private UICommand privatePreviewCommand;

    public UICommand getPreviewCommand()
    {
        return privatePreviewCommand;
    }

    private void setPreviewCommand(UICommand value)
    {
        privatePreviewCommand = value;
    }

    private UICommand privateCommitCommand;

    public UICommand getCommitCommand()
    {
        return privateCommitCommand;
    }

    private void setCommitCommand(UICommand value)
    {
        privateCommitCommand = value;
    }

    private UICommand privateUndoCommand;

    public UICommand getUndoCommand()
    {
        return privateUndoCommand;
    }

    private void setUndoCommand(UICommand value)
    {
        privateUndoCommand = value;
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

    private UICommand privateCloneVmCommand;

    public UICommand getCloneVmCommand()
    {
        return privateCloneVmCommand;
    }

    private void setCloneVmCommand(UICommand value)
    {
        privateCloneVmCommand = value;
    }

    private EntityModel privateCanSelectSnapshot;

    public EntityModel getCanSelectSnapshot()
    {
        return privateCanSelectSnapshot;
    }

    private void setCanSelectSnapshot(EntityModel value)
    {
        privateCanSelectSnapshot = value;
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

    private SystemTreeItemModel systemTreeSelectedItem;

    public SystemTreeItemModel getSystemTreeSelectedItem()
    {
        return systemTreeSelectedItem;
    }

    public void setSystemTreeSelectedItem(SystemTreeItemModel value)
    {
        systemTreeSelectedItem = value;
        onPropertyChanged(new PropertyChangedEventArgs("SystemTreeSelectedItem")); //$NON-NLS-1$
    }

    private HashMap<Version, ArrayList<String>> privateCustomPropertiesKeysList;

    private HashMap<Version, ArrayList<String>> getCustomPropertiesKeysList()
    {
        return privateCustomPropertiesKeysList;
    }

    private void setCustomPropertiesKeysList(HashMap<Version, ArrayList<String>> value)
    {
        privateCustomPropertiesKeysList = value;
    }

    private HashMap<Guid, SnapshotModel> snapshotsMap;

    public HashMap<Guid, SnapshotModel> getSnapshotsMap()
    {
        return snapshotsMap;
    }

    public void setSnapshotsMap(HashMap<Guid, SnapshotModel> value)
    {
        snapshotsMap = value;
        onPropertyChanged(new PropertyChangedEventArgs("SnapshotsMap")); //$NON-NLS-1$
    }

    private boolean isCloneVmSupported;

    public boolean getIsCloneVmSupported()
    {
        return isCloneVmSupported;
    }

    private void setIsCloneVmSupported(boolean value)
    {
        if (isCloneVmSupported != value)
        {
            isCloneVmSupported = value;
            onPropertyChanged(new PropertyChangedEventArgs("IsCloneVmSupported")); //$NON-NLS-1$
        }
    }

    private boolean isEntityChanged;

    public VmSnapshotListModel()
    {
        setTitle(ConstantsManager.getInstance().getConstants().snapshotsTitle());
        setHashName("snapshots"); //$NON-NLS-1$

        setNewCommand(new UICommand("New", this)); //$NON-NLS-1$
        setPreviewCommand(new UICommand("Preview", this)); //$NON-NLS-1$
        setCommitCommand(new UICommand("Commit", this)); //$NON-NLS-1$
        setUndoCommand(new UICommand("Undo", this)); //$NON-NLS-1$
        setRemoveCommand(new UICommand("Remove", this)); //$NON-NLS-1$
        setCloneVmCommand(new UICommand("CloneVM", this)); //$NON-NLS-1$

        setCanSelectSnapshot(new EntityModel());
        getCanSelectSnapshot().setEntity(true);

        setSnapshotsMap(new HashMap<Guid, SnapshotModel>());
        getSnapshotsMap().put(null, new SnapshotModel());

        if (getCustomPropertiesKeysList() == null) {
            AsyncDataProvider.getCustomPropertiesList(new AsyncQuery(this,
                    new INewAsyncCallback() {
                        @Override
                        public void onSuccess(Object target, Object returnValue) {
                            VmSnapshotListModel model = (VmSnapshotListModel) target;
                            if (returnValue != null) {
                                model.setCustomPropertiesKeysList((HashMap<Version, ArrayList<String>>) returnValue);
                            }
                        }
                    }));
        }
    }

    @Override
    public void setItems(Iterable value)
    {
        ArrayList<Snapshot> snapshots =
                value != null ? Linq.<Snapshot> cast(value) : new ArrayList<Snapshot>();

        Collections.sort(snapshots, Collections.reverseOrder(new SnapshotByCreationDateCommparer()));
        ArrayList<Snapshot> sortedSnapshots = new ArrayList<Snapshot>();

        for (Snapshot snapshot : snapshots) {
            SnapshotModel snapshotModel = snapshotsMap.get(snapshot.getId());
            if (snapshotModel == null) {
                snapshotModel = new SnapshotModel();
                snapshotsMap.put(snapshot.getId(), snapshotModel);
            }
            snapshotModel.setEntity(snapshot);

            if ((snapshot.getType() == SnapshotType.ACTIVE && getInType(SnapshotType.PREVIEW, snapshots) == null)
                    || snapshot.getType() == SnapshotType.PREVIEW) {
                sortedSnapshots.add(0, snapshot);
            }
            else if (snapshot.getType() == SnapshotType.REGULAR || snapshot.getType() == SnapshotType.STATELESS) {
                sortedSnapshots.add(snapshot);
            }
        }

        super.setItems(sortedSnapshots);

        if (isEntityChanged && sortedSnapshots.size() > 1) {
            setSelectedItem(sortedSnapshots.get(1));
        }
        isEntityChanged = false;

        updateActionAvailability();
    }

    @Override
    public void setEntity(Object value)
    {
        super.setEntity(value);

        updateIsCloneVmSupported();
    }

    @Override
    protected void onEntityChanged()
    {
        super.onEntityChanged();

        if (getEntity() != null)
        {
            isEntityChanged = true;
            getSearchCommand().execute();
        }
    }

    @Override
    protected void syncSearch()
    {
        VM vm = (VM) getEntity();
        if (vm == null)
        {
            return;
        }

        super.syncSearch(VdcQueryType.GetAllVmSnapshotsByVmId, new IdQueryParameters(vm.getId()));
    }

    @Override
    protected void onSelectedItemChanged()
    {
        super.onSelectedItemChanged();
        updateActionAvailability();

        if (getSelectedItem() != null) {
            Snapshot snapshot = ((Snapshot) getSelectedItem());
            updateVmConfigurationBySnapshot(snapshot.getId());
        }
    }

    @Override
    protected void selectedItemsChanged()
    {
        super.selectedItemsChanged();
        updateActionAvailability();
    }

    private void remove()
    {
        if (getEntity() != null)
        {
            if (getWindow() != null)
            {
                return;
            }

            Snapshot snapshot = (Snapshot) getSelectedItem();
            ConfirmationModel model = new ConfirmationModel();
            setWindow(model);
            model.setTitle(ConstantsManager.getInstance().getConstants().deleteSnapshotTitle());
            model.setHashName("delete_snapshot"); //$NON-NLS-1$
            model.setMessage(ConstantsManager.getInstance()
                    .getMessages()
                    .areYouSureYouWantToDeleteSanpshot(snapshot.getCreationDate(),
                            snapshot.getDescription()));

            UICommand tempVar = new UICommand("OnRemove", this); //$NON-NLS-1$
            tempVar.setTitle(ConstantsManager.getInstance().getConstants().ok());
            tempVar.setIsDefault(true);
            model.getCommands().add(tempVar);
            UICommand tempVar2 = new UICommand("Cancel", this); //$NON-NLS-1$
            tempVar2.setTitle(ConstantsManager.getInstance().getConstants().cancel());
            tempVar2.setIsCancel(true);
            model.getCommands().add(tempVar2);
        }
    }

    private void onRemove()
    {
        Snapshot snapshot = (Snapshot) getSelectedItem();
        if (snapshot == null)
        {
            cancel();
            return;
        }

        VM vm = (VM) getEntity();
        if (vm != null)
        {
            Frontend.RunAction(VdcActionType.RemoveSnapshot, new RemoveSnapshotParameters(snapshot.getId(),
                    vm.getId()), null, null);
        }

        getCanSelectSnapshot().setEntity(false);

        cancel();
    }

    private void undo()
    {
        VM vm = (VM) getEntity();
        if (vm != null)
        {
            Snapshot snapshot = getPreview();

            Frontend.RunAction(VdcActionType.RestoreAllSnapshots,
                    new RestoreAllSnapshotsParameters(vm.getId(), snapshot.getId()),
                    null,
                    null);
        }
    }

    private void commit()
    {
        VM vm = (VM) getEntity();
        if (vm != null)
        {
            Snapshot snapshot = getInPreview();

            Frontend.RunAction(VdcActionType.RestoreAllSnapshots,
                    new RestoreAllSnapshotsParameters(vm.getId(), snapshot.getId()),
                    null,
                    null);
        }
    }

    private void preview()
    {
        VM vm = (VM) getEntity();
        if (vm == null) {
            return;
        }

        Snapshot snapshot = (Snapshot) getSelectedItem();
        // if snapshot doesn't have memory, just trigger preview without showing popup
        if (!isMemorySnapshotSupported() || snapshot.getMemoryVolume().isEmpty()) {
            Frontend.RunAction(VdcActionType.TryBackToAllSnapshotsOfVm,
                    new TryBackToAllSnapshotsOfVmParameters(vm.getId(), snapshot.getId()),
                    null);
        }
        // otherwise, show a popup asking whether to use the memory or not
        else {
            SnapshotModel model = new SnapshotModel();
            setWindow(model);

            model.setTitle(ConstantsManager.getInstance().getConstants().previewSnapshotTitle());
            model.setHashName("preview_snapshot"); //$NON-NLS-1$

            model.getCommands().add(new UICommand("OnPreview", this) //$NON-NLS-1$
               .setTitle(ConstantsManager.getInstance().getConstants().ok())
               .setIsDefault(true));
            UICommand cancelCommand = new UICommand("Cancel", this) //$NON-NLS-1$
               .setTitle(ConstantsManager.getInstance().getConstants().cancel())
               .setIsCancel(true);
            model.getCommands().add(cancelCommand);
            model.setCancelCommand(cancelCommand);
            model.setCloseCommand(new UICommand("Cancel", this) //$NON-NLS-1$
               .setTitle(ConstantsManager.getInstance().getConstants().close())
               .setIsCancel(true));
        }
    }

    private void OnPreview() {
        Snapshot snapshot = (Snapshot) getSelectedItem();

        if (snapshot == null) {
            cancel();
            return;
        }

        VM vm = (VM) getEntity();
        final SnapshotModel model = (SnapshotModel) getWindow();

        model.startProgress(null);

        Frontend.RunAction(VdcActionType.TryBackToAllSnapshotsOfVm,
                new TryBackToAllSnapshotsOfVmParameters(vm.getId(), snapshot.getId(),
                        (Boolean) model.getMemory().getEntity()),
                new IFrontendActionAsyncCallback() {

                    @Override
                    public void executed(FrontendActionAsyncResult result) {
                        model.stopProgress();
                        cancel();
                    }
                });
    }

    private void newEntity()
    {
        VM vm = (VM) getEntity();
        if (vm == null || getWindow() != null) {
            return;
        }

        SnapshotModel model = new SnapshotModel();
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().createSnapshotTitle());
        model.setHashName("create_snapshot"); //$NON-NLS-1$

        model.setVm(vm);
        model.initialize();

        UICommand cancelCommand = new UICommand("Cancel", this); //$NON-NLS-1$
        cancelCommand.setTitle(ConstantsManager.getInstance().getConstants().cancel());
        cancelCommand.setIsCancel(true);
        UICommand closeCommand = new UICommand("Cancel", this); //$NON-NLS-1$
        closeCommand.setTitle(ConstantsManager.getInstance().getConstants().close());
        closeCommand.setIsCancel(true);

        model.setCancelCommand(cancelCommand);
        model.setCloseCommand(closeCommand);
    }

    public void postOnNew(List<VdcReturnValueBase> returnValues) {

        SnapshotModel model = (SnapshotModel) getWindow();

        model.stopProgress();

        if (returnValues != null && Linq.all(returnValues, new Linq.CanDoActionSucceedPredicate())) {
            cancel();
        }
    }

    private void cancel()
    {
        setWindow(null);
    }

    private void cloneVM()
    {
        Snapshot snapshot = (Snapshot) getSelectedItem();
        if (snapshot == null)
        {
            return;
        }

        if (getWindow() != null)
        {
            return;
        }

        VM selectedVm = (VM) getEntity();

        UnitVmModel model = new UnitVmModel(new CloneVmFromSnapshotModelBehavior());
        model.getVmType().setSelectedItem(selectedVm.getVmType());
        setWindow(model);

        model.startProgress(null);

        AsyncDataProvider.getVmConfigurationBySnapshot(new AsyncQuery(this, new INewAsyncCallback() {
            @Override
            public void onSuccess(Object target, Object returnValue) {
                VmSnapshotListModel vmSnapshotListModel = (VmSnapshotListModel) target;
                UnitVmModel model = (UnitVmModel) vmSnapshotListModel.getWindow();

                CloneVmFromSnapshotModelBehavior behavior = (CloneVmFromSnapshotModelBehavior) model.getBehavior();
                VM vm = (VM) returnValue;
                behavior.setVm(vm);

                model.setTitle(ConstantsManager.getInstance().getConstants().cloneVmFromSnapshotTitle());
                model.setHashName("clone_vm_from_snapshot"); //$NON-NLS-1$
                model.setCustomPropertiesKeysList(getCustomPropertiesKeysList());
                model.initialize(vmSnapshotListModel.getSystemTreeSelectedItem());

                VmBasedWidgetSwitchModeCommand switchModeCommand = new VmBasedWidgetSwitchModeCommand();
                switchModeCommand.init(model);
                model.getCommands().add(switchModeCommand);

                UICommand tempVar = new UICommand("OnCloneVM", vmSnapshotListModel); //$NON-NLS-1$
                tempVar.setTitle(ConstantsManager.getInstance().getConstants().ok());
                tempVar.setIsDefault(true);
                model.getCommands().add(tempVar);
                UICommand tempVar2 = new UICommand("Cancel", vmSnapshotListModel); //$NON-NLS-1$
                tempVar2.setTitle(ConstantsManager.getInstance().getConstants().cancel());
                tempVar2.setIsCancel(true);
                model.getCommands().add(tempVar2);

                vmSnapshotListModel.stopProgress();
            }
        }), snapshot.getId());
    }

    private void onCloneVM()
    {
        UnitVmModel model = (UnitVmModel) getWindow();
        CloneVmFromSnapshotModelBehavior behavior = (CloneVmFromSnapshotModelBehavior) model.getBehavior();
        Snapshot snapshot = (Snapshot) getSelectedItem();
        if (snapshot == null)
        {
            cancel();
            return;
        }

        if (!model.validate())
        {
            return;
        }

        setcurrentVm(behavior.getVm());

        String name = (String) model.getName().getEntity();

        // Save changes.
        VmTemplate template = (VmTemplate) model.getTemplate().getSelectedItem();

        getcurrentVm().setVmType((VmType) model.getVmType().getSelectedItem());
        getcurrentVm().setVmtGuid(template.getId());
        getcurrentVm().setName(name);
        getcurrentVm().setVmOs((Integer) model.getOSType().getSelectedItem());
        getcurrentVm().setNumOfMonitors((Integer) model.getNumOfMonitors().getSelectedItem());
        getcurrentVm().setVmDescription((String) model.getDescription().getEntity());
        getcurrentVm().setComment((String) model.getComment().getEntity());
        getcurrentVm().setVmDomain(model.getDomain().getIsAvailable() ?
                (String) model.getDomain().getSelectedItem() : ""); //$NON-NLS-1$
        getcurrentVm().setVmMemSizeMb((Integer) model.getMemSize().getEntity());
        getcurrentVm().setMinAllocatedMem((Integer) model.getMinAllocatedMemory().getEntity());
        Guid newClusterID = model.getSelectedCluster().getId();
        getcurrentVm().setVdsGroupId(newClusterID);
        getcurrentVm().setTimeZone(
                (model.getTimeZone().getIsAvailable() && model.getTimeZone().getSelectedItem() != null) ?
                        ((TimeZoneModel) model.getTimeZone().getSelectedItem()).getTimeZoneKey() : ""); //$NON-NLS-1$
        getcurrentVm().setNumOfSockets((Integer) model.getNumOfSockets().getSelectedItem());
        getcurrentVm().setCpuPerSocket(Integer.parseInt(model.getTotalCPUCores().getEntity().toString()) /
                (Integer) model.getNumOfSockets().getSelectedItem());
        getcurrentVm().setUsbPolicy((UsbPolicy) model.getUsbPolicy().getSelectedItem());
        getcurrentVm().setStateless((Boolean) model.getIsStateless().getEntity());
        getcurrentVm().setSmartcardEnabled((Boolean) model.getIsSmartcardEnabled().getEntity());
        getcurrentVm().setDeleteProtected((Boolean) model.getIsDeleteProtected().getEntity());
        getcurrentVm().setDefaultBootSequence(model.getBootSequence());
        getcurrentVm().setIsoPath(model.getCdImage().getIsChangable() ?
                (String) model.getCdImage().getSelectedItem() : ""); //$NON-NLS-1$
        getcurrentVm().setAutoStartup((Boolean) model.getIsHighlyAvailable().getEntity());
        getcurrentVm().setInitrdUrl((String) model.getInitrd_path().getEntity());
        getcurrentVm().setKernelUrl((String) model.getKernel_path().getEntity());
        getcurrentVm().setKernelParams((String) model.getKernel_parameters().getEntity());
        getcurrentVm().setCustomProperties((String) model.getCustomProperties().getEntity());
        if (model.getQuota().getIsAvailable() && model.getQuota().getSelectedItem() != null) {
            getcurrentVm().setQuotaId(((Quota) model.getQuota().getSelectedItem()).getId());
        }

        getcurrentVm().setVncKeyboardLayout((String) model.getVncKeyboardLayout().getSelectedItem());

        EntityModel displayProtocolSelectedItem = (EntityModel) model.getDisplayProtocol().getSelectedItem();
        getcurrentVm().setDefaultDisplayType((DisplayType) displayProtocolSelectedItem.getEntity());

        EntityModel prioritySelectedItem = (EntityModel) model.getPriority().getSelectedItem();
        getcurrentVm().setPriority((Integer) prioritySelectedItem.getEntity());

        VDS defaultHost = (VDS) model.getDefaultHost().getSelectedItem();
        getcurrentVm().setDedicatedVmForVds(
                (Boolean) model.getIsAutoAssign().getEntity() ? null : defaultHost.getId());

        getcurrentVm().setMigrationSupport((MigrationSupport) model.getMigrationMode().getSelectedItem());
        getcurrentVm().setUseHostCpuFlags((Boolean) model.getHostCpu().getEntity());
        getcurrentVm().setDiskMap(behavior.getVm().getDiskMap());

        HashMap<Guid, DiskImage> imageToDestinationDomainMap =
                model.getDisksAllocationModel().getImageToDestinationDomainMap();

        AddVmFromSnapshotParameters parameters =
                new AddVmFromSnapshotParameters(getcurrentVm().getStaticData(), snapshot.getId());
        parameters.setDiskInfoDestinationMap(imageToDestinationDomainMap);
        setupAddVmFromSnapshotParameters(parameters);
        parameters.setConsoleEnabled((Boolean) model.getIsConsoleDeviceEnabled().getEntity());

        model.startProgress(null);

        Frontend.RunAction(VdcActionType.AddVmFromSnapshot, parameters,
                new IFrontendActionAsyncCallback() {
                    @Override
                    public void executed(FrontendActionAsyncResult result) {

                        VmSnapshotListModel vmSnapshotListModel = (VmSnapshotListModel) result.getState();
                        vmSnapshotListModel.getWindow().stopProgress();
                        VdcReturnValueBase returnValueBase = result.getReturnValue();
                        if (returnValueBase != null && returnValueBase.getSucceeded())
                        {
                            vmSnapshotListModel.cancel();
                            vmSnapshotListModel.updateActionAvailability();
                        }
                    }
                }, this);
    }

    protected void setupAddVmFromSnapshotParameters(AddVmFromSnapshotParameters parameters) {
        // do nothing - no additional setup needed
    }

    private ArrayList<DiskImage> createDiskInfoList()
    {
        UnitVmModel model = (UnitVmModel) getWindow();
        ArrayList<DiskImage> diskInfoList = new ArrayList<DiskImage>();

        for (DiskImage diskImage : getcurrentVm().getDiskList()) {
            for (DiskModel diskModel : model.getDisks()) {
                DiskImage image = (DiskImage) diskModel.getDisk();
                if (diskImage.getImageId().equals(image.getImageId())) {
                    if (!diskImage.getVolumeType().equals(diskModel.getVolumeType().getSelectedItem())) {
                        image.setVolumeType(
                                (VolumeType) diskModel.getVolumeType().getSelectedItem());

                        diskInfoList.add(image);
                    }
                }
            }
        }

        return diskInfoList;
    }

    public void updateActionAvailability()
    {
        VM vm = (VM) getEntity();
        Snapshot snapshot = (Snapshot) getSelectedItem();

        boolean isVmDown = vm != null && vm.getStatus() == VMStatus.Down;
        boolean isVmImageLocked = vm != null && vm.getStatus() == VMStatus.ImageLocked;
        boolean isPreviewing = getIsPreviewing();
        boolean isLocked = getIsLocked();
        boolean isSelected = snapshot != null && snapshot.getType() != SnapshotType.ACTIVE;
        boolean isStateless = getIsStateless();
        boolean isCloneVmSupported = getIsCloneVmSupported();

        getCanSelectSnapshot().setEntity(!isPreviewing && !isLocked && !isStateless
                && VdcActionUtils.CanExecute(Arrays.asList(vm), VM.class, VdcActionType.CreateAllSnapshotsFromVm));
        getNewCommand().setIsExecutionAllowed(!isPreviewing && !isLocked && !isVmImageLocked && !isStateless);
        getPreviewCommand().setIsExecutionAllowed(isSelected && !isLocked && !isPreviewing && isVmDown && !isStateless);
        getCommitCommand().setIsExecutionAllowed(isPreviewing && isVmDown && !isStateless);
        getUndoCommand().setIsExecutionAllowed(isPreviewing && isVmDown && !isStateless);
        getRemoveCommand().setIsExecutionAllowed(isSelected && !isLocked && !isPreviewing && isVmDown && !isStateless);
        getCloneVmCommand().setIsExecutionAllowed(isSelected && !isLocked && !isPreviewing
                && !isVmImageLocked && !isStateless && isCloneVmSupported);
    }

    public boolean getIsPreviewing() {
        return getInPreview() != null;
    }

    public boolean getIsLocked() {
        return getLocked() != null;
    }

    public boolean getIsStateless() {
        return getInType(SnapshotType.STATELESS, (ArrayList<Snapshot>) getItems()) != null;
    }

    public Snapshot getLocked() {
        for (Snapshot snapshot : (ArrayList<Snapshot>) getItems()) {
            if (snapshot.getStatus() == SnapshotStatus.LOCKED) {
                return snapshot;
            }
        }
        return null;
    }

    public Snapshot getInPreview() {
        for (Snapshot snapshot : (ArrayList<Snapshot>) getItems()) {
            if (snapshot.getStatus() == SnapshotStatus.IN_PREVIEW) {
                return snapshot;
            }
        }
        return null;
    }

    public Snapshot getPreview() {
        for (Snapshot snapshot : (ArrayList<Snapshot>) getItems()) {
            if (snapshot.getType() == SnapshotType.PREVIEW) {
                return snapshot;
            }
        }
        return null;
    }

    public Snapshot getInStatus(SnapshotStatus snapshotStatus, ArrayList<Snapshot> snapshots) {
        for (Snapshot snapshot : snapshots) {
            if (snapshot.getStatus() == snapshotStatus) {
                return snapshot;
            }
        }
        return null;
    }

    public Snapshot getInType(SnapshotType snapshotType, ArrayList<Snapshot> snapshots) {
        for (Snapshot snapshot : snapshots) {
            if (snapshot.getType() == snapshotType) {
                return snapshot;
            }
        }
        return null;
    }

    public void updateVmConfigurationBySnapshot(Guid snapshotId)
    {
        SnapshotModel snapshotModel = snapshotsMap.get(snapshotId);
        snapshotModel.updateVmConfiguration();
    }

    protected void updateIsCloneVmSupported()
    {
        if (getEntity() == null)
        {
            return;
        }

        VM vm = (VM) getEntity();

        AsyncDataProvider.getDataCenterById(new AsyncQuery(this, new INewAsyncCallback() {
            @Override
            public void onSuccess(Object target, Object returnValue) {
                VmSnapshotListModel model = (VmSnapshotListModel) target;
                StoragePool dataCenter = (StoragePool) returnValue;
                VM vm = (VM) model.getEntity();

                Version minClusterVersion = vm.getVdsGroupCompatibilityVersion();
                Version minDcVersion = dataCenter.getcompatibility_version();

                AsyncDataProvider.isCommandCompatible(new AsyncQuery(model, new INewAsyncCallback() {
                    @Override
                    public void onSuccess(Object target, Object returnValue) {
                        VmSnapshotListModel model = (VmSnapshotListModel) target;
                        model.setIsCloneVmSupported((Boolean) returnValue);
                    }
                }), VdcActionType.AddVmFromSnapshot, minClusterVersion, minDcVersion);
            }
        }), vm.getStoragePoolId());
    }

    protected boolean isMemorySnapshotSupported() {
        if (getEntity() == null) {
            return false;
        }

        VM vm = (VM) getEntity();

        return  (Boolean) AsyncDataProvider.getConfigValuePreConverted(
                ConfigurationValues.MemorySnapshotSupported,
                vm.getVdsGroupCompatibilityVersion().toString());
    }

    @Override
    public void executeCommand(UICommand command)
    {
        super.executeCommand(command);

        if (command == getNewCommand())
        {
            newEntity();
        }
        else if (command == getPreviewCommand())
        {
            preview();
        }
        else if (command == getCommitCommand())
        {
            commit();
        }
        else if (command == getUndoCommand())
        {
            undo();
        }
        else if (command == getRemoveCommand())
        {
            remove();
        }
        else if (command == getCloneVmCommand())
        {
            cloneVM();
        }
        else if (StringHelper.stringsEqual(command.getName(), "OnRemove")) //$NON-NLS-1$
        {
            onRemove();
        }
        else if (StringHelper.stringsEqual(command.getName(), "Cancel")) //$NON-NLS-1$
        {
            cancel();
        }
        else if (StringHelper.stringsEqual(command.getName(), "OnCloneVM")) //$NON-NLS-1$
        {
            onCloneVM();
        }
        else if (StringHelper.stringsEqual(command.getName(), "OnPreview")) //$NON-NLS-1$
        {
            OnPreview();
        }
    }

    @Override
    protected String getListName() {
        return "VmSnapshotListModel"; //$NON-NLS-1$
    }
}
