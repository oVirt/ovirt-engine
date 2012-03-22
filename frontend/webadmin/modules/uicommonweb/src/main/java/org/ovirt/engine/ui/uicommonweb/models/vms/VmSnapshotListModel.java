package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.common.action.AddVmFromSnapshotParameters;
import org.ovirt.engine.core.common.action.CreateAllSnapshotsFromVmParameters;
import org.ovirt.engine.core.common.action.RemoveSnapshotParameters;
import org.ovirt.engine.core.common.action.RestoreAllSnapshotsParameters;
import org.ovirt.engine.core.common.action.TryBackToAllSnapshotsOfVmParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.MigrationSupport;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotStatus;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotType;
import org.ovirt.engine.core.common.businessentities.UsbPolicy;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmOsType;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.VolumeType;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.queries.GetAllVmSnapshotsByVmIdParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.core.compat.StringFormat;
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
import org.ovirt.engine.ui.uicompat.FrontendActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;

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
        OnPropertyChanged(new PropertyChangedEventArgs("SystemTreeSelectedItem"));
    }

    private ArrayList<String> privateCustomPropertiesKeysList;

    private ArrayList<String> getCustomPropertiesKeysList()
    {
        return privateCustomPropertiesKeysList;
    }

    private void setCustomPropertiesKeysList(ArrayList<String> value)
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
        OnPropertyChanged(new PropertyChangedEventArgs("SnapshotsMap"));
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
            OnPropertyChanged(new PropertyChangedEventArgs("IsCloneVmSupported"));
        }
    }

    public VmSnapshotListModel()
    {
        setTitle("Snapshots");

        setNewCommand(new UICommand("New", this));
        setPreviewCommand(new UICommand("Preview", this));
        setCommitCommand(new UICommand("Commit", this));
        setUndoCommand(new UICommand("Undo", this));
        setRemoveCommand(new UICommand("Remove", this));
        setCloneVmCommand(new UICommand("CloneVM", this));

        setCanSelectSnapshot(new EntityModel());
        getCanSelectSnapshot().setEntity(true);

        setSnapshotsMap(new HashMap<Guid, SnapshotModel>());

        AsyncDataProvider.GetCustomPropertiesList(new AsyncQuery(this,
                new INewAsyncCallback() {
                    @Override
                    public void OnSuccess(Object target, Object returnValue) {
                        VmSnapshotListModel model = (VmSnapshotListModel) target;
                        if (returnValue != null)
                        {
                            String[] array = ((String) returnValue).split("[;]", -1);
                            model.setCustomPropertiesKeysList(new ArrayList<String>());
                            for (String s : array)
                            {
                                model.getCustomPropertiesKeysList().add(s);
                            }
                        }
                    }
                }));
    }

    @Override
    public void setItems(Iterable value)
    {
        ArrayList<Snapshot> snapshots =
                value != null ? Linq.<Snapshot> Cast(value) : new ArrayList<Snapshot>();

        snapshots = Linq.OrderByDescending(snapshots, new SnapshotByCreationDateCommparer());

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

        UpdateActionAvailability();
    }

    @Override
    public void setEntity(Object value)
    {
        super.setEntity(value);

        UpdateIsCloneVmSupported();
    }

    @Override
    protected void OnEntityChanged()
    {
        super.OnEntityChanged();

        if (getEntity() != null)
        {
            getSearchCommand().Execute();
        }
    }

    @Override
    protected void SyncSearch()
    {
        VM vm = (VM) getEntity();
        if (vm == null)
        {
            return;
        }

        super.SyncSearch(VdcQueryType.GetAllVmSnapshotsByVmId, new GetAllVmSnapshotsByVmIdParameters(vm.getId()));
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
            model.setTitle("Delete Snapshot");
            model.setHashName("delete_snapshot");
            model.setMessage(StringFormat.format("Are you sure you want to delete snapshot from %1$s with description '%2$s'?",
                    snapshot.getCreationDate(),
                    snapshot.getDescription()));

            UICommand tempVar = new UICommand("OnRemove", this);
            tempVar.setTitle("OK");
            tempVar.setIsDefault(true);
            model.getCommands().add(tempVar);
            UICommand tempVar2 = new UICommand("Cancel", this);
            tempVar2.setTitle("Cancel");
            tempVar2.setIsCancel(true);
            model.getCommands().add(tempVar2);
        }
    }

    private void OnRemove()
    {
        Snapshot snapshot = (Snapshot) getSelectedItem();
        if (snapshot == null)
        {
            Cancel();
            return;
        }

        VM vm = (VM) getEntity();
        if (vm != null)
        {
            Frontend.RunAction(VdcActionType.RemoveSnapshot, new RemoveSnapshotParameters(snapshot.getId(),
                    vm.getId()),
                    new IFrontendActionAsyncCallback() {
                        @Override
                        public void Executed(FrontendActionAsyncResult result) {

                        }
                    }, null);
        }

        getCanSelectSnapshot().setEntity(false);

        Cancel();
    }

    private void Undo()
    {
        VM vm = (VM) getEntity();
        if (vm != null)
        {
            Snapshot snapshot = getPreview();

            Frontend.RunAction(VdcActionType.RestoreAllSnapshots,
                    new RestoreAllSnapshotsParameters(vm.getId(), snapshot.getId()),
                    new IFrontendActionAsyncCallback() {
                        @Override
                        public void Executed(FrontendActionAsyncResult result) {

                        }
                    },
                    null);
        }
    }

    private void Commit()
    {
        VM vm = (VM) getEntity();
        if (vm != null)
        {
            Snapshot snapshot = getInPreview();

            Frontend.RunAction(VdcActionType.RestoreAllSnapshots,
                    new RestoreAllSnapshotsParameters(vm.getId(), snapshot.getId()),
                    new IFrontendActionAsyncCallback() {
                        @Override
                        public void Executed(FrontendActionAsyncResult result) {

                        }
                    },
                    null);
        }
    }

    private void Preview()
    {
        VM vm = (VM) getEntity();
        if (vm != null)
        {
            Snapshot snapshot = (Snapshot) getSelectedItem();

            Frontend.RunAction(VdcActionType.TryBackToAllSnapshotsOfVm,
                    new TryBackToAllSnapshotsOfVmParameters(vm.getId(), snapshot.getId()),
                    new IFrontendActionAsyncCallback() {
                        @Override
                        public void Executed(FrontendActionAsyncResult result) {

                        }
                    },
                    null);
        }
    }

    private void New()
    {
        VM vm = (VM) getEntity();
        if (vm == null)
        {
            return;
        }

        if (getWindow() != null)
        {
            return;
        }

        SnapshotModel model = new SnapshotModel();
        setWindow(model);
        model.setTitle("Create Snapshot");
        model.setHashName("create_snapshot");

        model.StartProgress(null);
        AsyncDataProvider.GetVmDiskList(new AsyncQuery(this,
                new INewAsyncCallback() {
                    @Override
                    public void OnSuccess(Object target, Object returnValue) {
                        VmSnapshotListModel vmSnapshotListModel = (VmSnapshotListModel) target;
                        SnapshotModel snapshotModel = (SnapshotModel) vmSnapshotListModel.getWindow();
                        ArrayList<DiskImage> disks = (ArrayList<DiskImage>) returnValue;

                        vmSnapshotListModel.PostNew(disks);
                        snapshotModel.StopProgress();
                    }
                }),
                vm.getId());
    }

    public void PostNew(ArrayList<DiskImage> disks) {
        SnapshotModel model = (SnapshotModel) getWindow();

        if (disks.isEmpty())
        {
            model.setMessage("Snapshot cannot be created since the VM has no Disks");

            UICommand tempVar = new UICommand("Cancel", this);
            tempVar.setTitle("Close");
            tempVar.setIsDefault(true);
            tempVar.setIsCancel(true);
            model.getCommands().add(tempVar);
        }
        else
        {
            UICommand tempVar2 = new UICommand("OnNew", this);
            tempVar2.setTitle("OK");
            tempVar2.setIsDefault(true);
            model.getCommands().add(tempVar2);
            UICommand tempVar3 = new UICommand("Cancel", this);
            tempVar3.setTitle("Cancel");
            tempVar3.setIsCancel(true);
            model.getCommands().add(tempVar3);
        }
    }

    private void OnNew()
    {
        VM vm = (VM) getEntity();
        if (vm == null)
        {
            return;
        }

        SnapshotModel model = (SnapshotModel) getWindow();

        if (model.getProgress() != null)
        {
            return;
        }

        if (!model.Validate())
        {
            return;
        }

        model.StartProgress(null);

        CreateAllSnapshotsFromVmParameters params =
                new CreateAllSnapshotsFromVmParameters(vm.getId(), (String) model.getDescription().getEntity());

        Frontend.RunAction(VdcActionType.CreateAllSnapshotsFromVm, params,
                new IFrontendActionAsyncCallback() {
                    @Override
                    public void Executed(FrontendActionAsyncResult result) {

                        VmSnapshotListModel localModel = (VmSnapshotListModel) result.getState();
                        localModel.PostOnNew(result.getReturnValue());

                    }
                }, this);
    }

    public void PostOnNew(VdcReturnValueBase returnValue)
    {
        SnapshotModel model = (SnapshotModel) getWindow();

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

    private void CloneVM()
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

        UnitVmModel model = new UnitVmModel(new CloneVmFromSnapshotModelBehavior());
        setWindow(model);
        model.StartProgress(null);

        AsyncDataProvider.GetVmConfigurationBySnapshot(new AsyncQuery(this, new INewAsyncCallback() {
            @Override
            public void OnSuccess(Object target, Object returnValue) {
                VmSnapshotListModel vmSnapshotListModel = (VmSnapshotListModel) target;
                UnitVmModel model = (UnitVmModel) vmSnapshotListModel.getWindow();
                VM selectedVm = (VM) getEntity();

                CloneVmFromSnapshotModelBehavior behavior = (CloneVmFromSnapshotModelBehavior) model.getBehavior();
                VM vm = (VM) returnValue;
                behavior.setVm(vm);

                model.setTitle("Clone VM from Snapshot");
                model.setHashName("clone_vm_from_snapshot");
                model.setCustomPropertiesKeysList(getCustomPropertiesKeysList());
                model.Initialize(vmSnapshotListModel.getSystemTreeSelectedItem());

                UICommand tempVar = new UICommand("OnCloneVM", vmSnapshotListModel);
                tempVar.setTitle("OK");
                tempVar.setIsDefault(true);
                model.getCommands().add(tempVar);
                UICommand tempVar2 = new UICommand("Cancel", vmSnapshotListModel);
                tempVar2.setTitle("Cancel");
                tempVar2.setIsCancel(true);
                model.getCommands().add(tempVar2);

                vmSnapshotListModel.StopProgress();
            }
        }), snapshot.getId());
    }

    private void OnCloneVM()
    {
        UnitVmModel model = (UnitVmModel) getWindow();
        CloneVmFromSnapshotModelBehavior behavior = (CloneVmFromSnapshotModelBehavior) model.getBehavior();
        Snapshot snapshot = (Snapshot) getSelectedItem();
        if (snapshot == null)
        {
            Cancel();
            return;
        }

        if (!model.Validate())
        {
            return;
        }

        setcurrentVm(behavior.getVm());

        String name = (String) model.getName().getEntity();

        // Save changes.
        VmTemplate template = (VmTemplate) model.getTemplate().getSelectedItem();

        getcurrentVm().setvm_type(model.getVmType());
        getcurrentVm().setvmt_guid(template.getId());
        getcurrentVm().setvm_name(name);
        getcurrentVm().setvm_os((VmOsType) model.getOSType().getSelectedItem());
        getcurrentVm().setnum_of_monitors((Integer) model.getNumOfMonitors().getSelectedItem());
        getcurrentVm().setvm_description((String) model.getDescription().getEntity());
        getcurrentVm().setvm_domain(model.getDomain().getIsAvailable() ?
                (String) model.getDomain().getSelectedItem() : "");
        getcurrentVm().setvm_mem_size_mb((Integer) model.getMemSize().getEntity());
        getcurrentVm().setMinAllocatedMem((Integer) model.getMinAllocatedMemory().getEntity());
        Guid newClusterID = ((VDSGroup) model.getCluster().getSelectedItem()).getId();
        getcurrentVm().setvds_group_id(newClusterID);
        getcurrentVm().settime_zone(
                (model.getTimeZone().getIsAvailable() && model.getTimeZone().getSelectedItem() != null) ?
                        ((Map.Entry<String, String>) model.getTimeZone().getSelectedItem()).getKey() : "");
        getcurrentVm().setnum_of_sockets((Integer) model.getNumOfSockets().getEntity());
        getcurrentVm().setcpu_per_socket((Integer) model.getTotalCPUCores().getEntity() /
                (Integer) model.getNumOfSockets().getEntity());
        getcurrentVm().setusb_policy((UsbPolicy) model.getUsbPolicy().getSelectedItem());
        getcurrentVm().setis_auto_suspend(false);
        getcurrentVm().setis_stateless((Boolean) model.getIsStateless().getEntity());
        getcurrentVm().setdefault_boot_sequence(model.getBootSequence());
        getcurrentVm().setiso_path(model.getCdImage().getIsChangable() ?
                (String) model.getCdImage().getSelectedItem() : "");
        getcurrentVm().setauto_startup((Boolean) model.getIsHighlyAvailable().getEntity());
        getcurrentVm().setinitrd_url((String) model.getInitrd_path().getEntity());
        getcurrentVm().setkernel_url((String) model.getKernel_path().getEntity());
        getcurrentVm().setkernel_params((String) model.getKernel_parameters().getEntity());
        getcurrentVm().setCustomProperties((String) model.getCustomProperties().getEntity());

        EntityModel displayProtocolSelectedItem = (EntityModel) model.getDisplayProtocol().getSelectedItem();
        getcurrentVm().setdefault_display_type((DisplayType) displayProtocolSelectedItem.getEntity());

        EntityModel prioritySelectedItem = (EntityModel) model.getPriority().getSelectedItem();
        getcurrentVm().setpriority((Integer) prioritySelectedItem.getEntity());

        VDS defaultHost = (VDS) model.getDefaultHost().getSelectedItem();
        getcurrentVm().setdedicated_vm_for_vds(
                (Boolean) model.getIsAutoAssign().getEntity() ? null : defaultHost.getId());

        getcurrentVm().setMigrationSupport(MigrationSupport.MIGRATABLE);
        if ((Boolean) model.getRunVMOnSpecificHost().getEntity())
        {
            getcurrentVm().setMigrationSupport(MigrationSupport.PINNED_TO_HOST);
        }
        else if ((Boolean) model.getDontMigrateVM().getEntity())
        {
            getcurrentVm().setMigrationSupport(MigrationSupport.IMPLICITLY_NON_MIGRATABLE);
        }
        getcurrentVm().setDiskMap(behavior.getVm().getDiskMap());

        HashMap<Guid, Guid> imageToDestinationDomainMap =
                model.getDisksAllocationModel().getImageToDestinationDomainMap();
        storage_domains storageDomain =
                ((storage_domains) model.getDisksAllocationModel().getStorageDomain().getSelectedItem());
        ArrayList<DiskImage> diskInfoList = CreateDiskInfoList();

        if ((Boolean) model.getDisksAllocationModel().getIsSingleStorageDomain().getEntity()) {
            for (Guid key : imageToDestinationDomainMap.keySet()) {
                imageToDestinationDomainMap.put(key, storageDomain.getId());
            }
        }

        AddVmFromSnapshotParameters parameters =
                new AddVmFromSnapshotParameters(getcurrentVm().getStaticData(), diskInfoList, snapshot.getId());
        parameters.setImageToDestinationDomainMap(imageToDestinationDomainMap);

        model.StartProgress(null);

        Frontend.RunAction(VdcActionType.AddVmFromSnapshot, parameters,
                new IFrontendActionAsyncCallback() {
                    @Override
                    public void Executed(FrontendActionAsyncResult result) {

                        VmSnapshotListModel vmSnapshotListModel = (VmSnapshotListModel) result.getState();
                        vmSnapshotListModel.getWindow().StopProgress();
                        VdcReturnValueBase returnValueBase = result.getReturnValue();
                        if (returnValueBase != null && returnValueBase.getSucceeded())
                        {
                            vmSnapshotListModel.Cancel();
                            vmSnapshotListModel.UpdateActionAvailability();
                        }
                    }
                }, this);
    }

    private ArrayList<DiskImage> CreateDiskInfoList()
    {
        UnitVmModel model = (UnitVmModel) getWindow();
        ArrayList<DiskImage> diskInfoList = new ArrayList<DiskImage>();

        for (DiskImage diskImage : getcurrentVm().getDiskList()) {
            for (DiskModel diskModel : model.getDisks()) {
                if (diskImage.getId().equals(diskModel.getDiskImage().getId())) {
                    if (!diskImage.getvolume_type().equals(diskModel.getVolumeType().getSelectedItem())) {
                        diskModel.getDiskImage().setvolume_type(
                                (VolumeType) diskModel.getVolumeType().getSelectedItem());

                        diskInfoList.add(diskModel.getDiskImage());
                    }
                }
            }
        }

        return diskInfoList;
    }

    public void UpdateActionAvailability()
    {
        VM vm = (VM) getEntity();
        Snapshot snapshot = (Snapshot) getSelectedItem();

        boolean isVmDown = vm != null && vm.getstatus() == VMStatus.Down;
        boolean isVmImageLocked = vm != null && vm.getstatus() == VMStatus.ImageLocked;
        boolean isPreviewing = getIsPreviewing();
        boolean isLocked = getIsLocked();
        boolean isSelected = snapshot != null && snapshot.getType() != SnapshotType.ACTIVE;
        boolean isStateless = getIsStateless();
        boolean isCloneVmSupported = getIsCloneVmSupported();

        getCanSelectSnapshot().setEntity(!isPreviewing && !isLocked && !isVmImageLocked && !isStateless);
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

    public void UpdateVmConfigurationBySnapshot(Guid snapshotId)
    {
        SnapshotModel snapshotModel = snapshotsMap.get(snapshotId);
        snapshotModel.UpdateVmConfiguration();
    }

    protected void UpdateIsCloneVmSupported()
    {
        if (getEntity() == null)
        {
            return;
        }
        VM vm = (VM) getEntity();
        Version version = vm.getvds_group_compatibility_version() != null
                ? vm.getvds_group_compatibility_version() : new Version();

        // TODO: replace with configuration value
        setIsCloneVmSupported(version.compareTo(new Version("3.1")) >= 0);
    }

    @Override
    public void ExecuteCommand(UICommand command)
    {
        super.ExecuteCommand(command);

        if (command == getNewCommand())
        {
            New();
        }
        else if (command == getPreviewCommand())
        {
            Preview();
        }
        else if (command == getCommitCommand())
        {
            Commit();
        }
        else if (command == getUndoCommand())
        {
            Undo();
        }
        else if (command == getRemoveCommand())
        {
            remove();
        }
        else if (command == getCloneVmCommand())
        {
            CloneVM();
        }
        else if (StringHelper.stringsEqual(command.getName(), "OnRemove"))
        {
            OnRemove();
        }
        else if (StringHelper.stringsEqual(command.getName(), "Cancel"))
        {
            Cancel();
        }
        else if (StringHelper.stringsEqual(command.getName(), "OnNew"))
        {
            OnNew();
        }
        else if (StringHelper.stringsEqual(command.getName(), "OnCloneVM"))
        {
            OnCloneVM();
        }
    }

    @Override
    protected String getListName() {
        return "VmSnapshotListModel";
    }
}
