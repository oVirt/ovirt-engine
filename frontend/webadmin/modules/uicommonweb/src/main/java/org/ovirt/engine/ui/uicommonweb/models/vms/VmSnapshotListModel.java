package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.Collections;

import org.ovirt.engine.core.common.action.CreateAllSnapshotsFromVmParameters;
import org.ovirt.engine.core.common.action.MergeSnapshotParamenters;
import org.ovirt.engine.core.common.action.RestoreAllSnapshotsParameters;
import org.ovirt.engine.core.common.action.TryBackToAllSnapshotsOfVmParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.vm_pools;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.core.compat.StringFormat;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicompat.FrontendActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;

@SuppressWarnings("unused")
public class VmSnapshotListModel extends SearchableListModel
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

    @Override
    public SnapshotModel getSelectedItem()
    {
        return (SnapshotModel) ((super.getSelectedItem() instanceof SnapshotModel) ? super.getSelectedItem() : null);
    }

    public void setSelectedItem(SnapshotModel value)
    {
        super.setSelectedItem(value);
    }

    @Override
    public Iterable getItems()
    {
        return items;
    }

    @Override
    public void setItems(Iterable value)
    {
        if (items != value)
        {
            SnapshotModel lastSelectedItem = getSelectedItem();

            ItemsChanging(value, items);
            items = value;
            getItemsChangedEvent().raise(this, EventArgs.Empty);
            OnPropertyChanged(new PropertyChangedEventArgs("Items"));

            selectedItem = null;

            if (lastSelectedItem != null)
            {
                SnapshotModel newSelectedItem = null;
                java.util.ArrayList<SnapshotModel> newItems = (java.util.ArrayList<SnapshotModel>) value;

                if (newItems != null)
                {
                    for (SnapshotModel newItem : newItems)
                    {
                        // Search for selected item
                        if (newItem.getSnapshotId().equals(lastSelectedItem.getSnapshotId()))
                        {
                            newSelectedItem = newItem;
                            break;
                        }
                    }
                }

                if (newSelectedItem != null)
                {
                    selectedItem = newSelectedItem;
                }
            }
            OnSelectedItemChanged();
        }
    }

    private Iterable apps;

    public Iterable getApps()
    {
        return apps;
    }

    public void setApps(Iterable value)
    {
        if (apps != value)
        {
            apps = value;
            OnPropertyChanged(new PropertyChangedEventArgs("Apps"));
        }
    }

    private boolean isSnapshotsAvailable;

    public boolean getIsSnapshotsAvailable()
    {
        return isSnapshotsAvailable;
    }

    public void setIsSnapshotsAvailable(boolean value)
    {
        if (isSnapshotsAvailable != value)
        {
            isSnapshotsAvailable = value;
            OnPropertyChanged(new PropertyChangedEventArgs("IsSnapshotsAvailable"));
        }
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

    public VmSnapshotListModel()
    {
        setTitle("Snapshots");

        setNewCommand(new UICommand("New", this));
        setPreviewCommand(new UICommand("Preview", this));
        setCommitCommand(new UICommand("Commit", this));
        setUndoCommand(new UICommand("Undo", this));
        setRemoveCommand(new UICommand("Remove", this));

        setCanSelectSnapshot(new EntityModel());
    }

    private void remove()
    {
        if (getEntity() != null)
        {
            if (getWindow() != null)
            {
                return;
            }

            ConfirmationModel model = new ConfirmationModel();
            setWindow(model);
            model.setTitle("Delete Snapshot");
            model.setHashName("delete_snapshot");
            model.setMessage(StringFormat.format("Are you sure you want to delete snapshot from %1$s with description '%2$s'?",
                    getSelectedItem().getDate(),
                    getSelectedItem().getDescription().getEntity()));

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
        if (getSelectedItem() == null)
        {
            Cancel();
            return;
        }

        VM vm = (VM) getEntity();
        if (vm != null)
        {
            for (DiskImage disk : data.keySet())
            {
                // var snapshots = data[disk].OrderBy(a => a.lastModified);

                java.util.ArrayList<DiskImage> list = new java.util.ArrayList<DiskImage>();
                for (DiskImage a : data.get(disk))
                {
                    list.add(a);
                }
                Collections.sort(list, new Linq.DiskImageByLastModifiedComparer());

                if (list.size() < 2)
                {
                    continue;
                }

                // var srcSnapshot = snapshots.FirstOrDefault(a => a.vm_snapshot_id == SelectedItem.SnapshotId);
                DiskImage srcSnapshot = null;
                for (DiskImage a : list)
                {
                    if (a.getvm_snapshot_id().equals(getSelectedItem().getSnapshotId()))
                    {
                        srcSnapshot = a;
                        break;
                    }
                }
                if (srcSnapshot == null)
                {
                    continue;
                }

                // var dstSnapshot = snapshots.FirstOrDefault(a => a.ParentId == srcSnapshot.image_guid);
                DiskImage dstSnapshot = null;
                for (DiskImage a : list)
                {
                    if (a.getParentId().equals(srcSnapshot.getId()))
                    {
                        dstSnapshot = a;
                        break;
                    }
                }
                if (dstSnapshot == null)
                {
                    continue;
                }

                Guid srcSnapshotId =
                        (srcSnapshot.getvm_snapshot_id() != null) ? srcSnapshot.getvm_snapshot_id().getValue()
                                : NGuid.Empty;
                Guid dstSnapshotId =
                        (dstSnapshot.getvm_snapshot_id() != null) ? dstSnapshot.getvm_snapshot_id().getValue()
                                : NGuid.Empty;

                Frontend.RunAction(VdcActionType.MergeSnapshot, new MergeSnapshotParamenters(srcSnapshotId,
                        dstSnapshotId,
                        vm.getvm_guid()),
                        new IFrontendActionAsyncCallback() {
                            @Override
                            public void Executed(FrontendActionAsyncResult result) {

                            }
                        }, null);

                getCanSelectSnapshot().setEntity(false);

                break;
            }
        }

        Cancel();
    }

    private void Undo()
    {
        VM vm = (VM) getEntity();
        if (vm != null)
        {
            Guid snapshotId = NGuid.Empty;

            for (DiskImage disk : data.keySet())
            {
                // Find the last snapshot to revert to it.
                // var snapshot = data[disk]
                // //.OrderByDescending(a => a.lastModified)
                // .FirstOrDefault();

                DiskImage snapshot = data.get(disk).get(0);
                if (snapshot != null)
                {
                    snapshotId =
                            (snapshot.getvm_snapshot_id() != null) ? snapshot.getvm_snapshot_id().getValue()
                                    : NGuid.Empty;
                    break;
                }
            }

            if (!snapshotId.equals(NGuid.Empty))
            {
                Frontend.RunAction(VdcActionType.RestoreAllSnapshots,
                        new RestoreAllSnapshotsParameters(vm.getvm_guid(), snapshotId),
                        new IFrontendActionAsyncCallback() {
                            @Override
                            public void Executed(FrontendActionAsyncResult result) {

                            }
                        },
                        null);
            }
        }
    }

    private void Commit()
    {
        VM vm = (VM) getEntity();
        if (vm != null)
        {
            Guid snapshotId = NGuid.Empty;

            for (DiskImage disk : data.keySet())
            {
                // Find a previwing snapshot by disk.
                // var snapshot = data[disk].FirstOrDefault(a => (Guid)a.image_guid ==
                // previewingImages[disk.image_guid]);
                DiskImage snapshot = null;
                for (DiskImage a : data.get(disk))
                {
                    if (a.getId().equals(previewingImages.get(disk.getId())))
                    {
                        snapshot = a;
                        break;
                    }
                }

                if (snapshot != null)
                {
                    snapshotId =
                            (snapshot.getvm_snapshot_id() != null) ? snapshot.getvm_snapshot_id().getValue()
                                    : NGuid.Empty;
                    break;
                }
            }

            if (!snapshotId.equals(NGuid.Empty))
            {
                Frontend.RunAction(VdcActionType.RestoreAllSnapshots,
                        new RestoreAllSnapshotsParameters(vm.getvm_guid(), snapshotId),
                        new IFrontendActionAsyncCallback() {
                            @Override
                            public void Executed(FrontendActionAsyncResult result) {

                            }
                        },
                        null);
            }
        }
    }

    private void Preview()
    {
        VM vm = (VM) getEntity();
        if (vm != null)
        {
            Frontend.RunAction(VdcActionType.TryBackToAllSnapshotsOfVm,
                    new TryBackToAllSnapshotsOfVmParameters(vm.getvm_guid(), getSelectedItem().getSnapshotId()),
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
        if (getEntity() != null)
        {
            if (getWindow() != null)
            {
                return;
            }

            SnapshotModel model = new SnapshotModel();
            setWindow(model);
            model.setTitle("Create Snapshot");
            model.setHashName("create_snapshot");

            // var disks = data.getKey()s
            // .Select(a => StringFormat.format("Disk {0}", a.internal_drive_mapping))
            // .OrderBy(a => a)
            // .Select(a => new EntityModel() { Entity = a })
            // .ToList();

            java.util.ArrayList<String> driveMappings = new java.util.ArrayList<String>();
            for (DiskImage a : data.keySet())
            {
                driveMappings.add(StringFormat.format("Disk %1$s", a.getinternal_drive_mapping()));
            }
            Collections.sort(driveMappings);

            java.util.ArrayList<EntityModel> disks = new java.util.ArrayList<EntityModel>();
            for (String a : driveMappings)
            {
                EntityModel m = new EntityModel();
                m.setEntity(StringFormat.format("Disk %1$s", a));
                disks.add(m);
            }
            model.setDisks(disks);

            // disks.Each(a => Selector.SetIsSelected(a, true));
            for (EntityModel a : disks)
            {
                a.setIsSelected(true);
            }

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

        java.util.ArrayList<String> disks = new java.util.ArrayList<String>();
        for (EntityModel a : model.getDisks())
        {
            if (a.getIsSelected())
            {
                disks.add(GetInternalDriveMapping((String) a.getEntity()));
            }
        }

        model.StartProgress(null);

        CreateAllSnapshotsFromVmParameters tempVar =
                new CreateAllSnapshotsFromVmParameters(vm.getvm_guid(), (String) model.getDescription().getEntity());
        tempVar.setDisksList(disks);
        Frontend.RunAction(VdcActionType.CreateAllSnapshotsFromVm, tempVar,
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

    /**
     * Getting a string in the format of "Disk x", should return "x". NOTE: When localizing, this function should be
     * smarter.
     *
     * @param diskDisplayName
     *            string in the format of "Disk x"
     * @return "x"
     */
    private String GetInternalDriveMapping(String diskDisplayName)
    {
        return diskDisplayName.substring(5);
    }

    private void Cancel()
    {
        setWindow(null);
    }

    @Override
    protected void OnEntityChanged()
    {
        super.OnEntityChanged();

        // Deal with pool as Entity without failing.
        if (getEntity() instanceof vm_pools)
        {
            setIsSnapshotsAvailable(false);
            setItems(null);
        }
        else
        {
            setIsSnapshotsAvailable(true);
            UpdateData();
        }

        if (getEntity() != null)
        {
            getSearchCommand().Execute();
        }
    }

    @Override
    protected void SyncSearch()
    {
        UpdateData();
        setIsQueryFirstTime(false);
    }

    private void UpdateData()
    {
        VM vm = (VM) getEntity();
        if (vm == null)
        {
            return;
        }

        new GetAllVmSnapshotsExecutor(vm.getvm_guid(), new AsyncQuery(this,
                new INewAsyncCallback() {
                    @Override
                    public void OnSuccess(Object target1, Object returnValue1) {

                        VmSnapshotListModel model = (VmSnapshotListModel) target1;
                        model.PostUpdateData(returnValue1);

                    }
                }), getIsQueryFirstTime()).Execute();
    }

    public void PostUpdateData(Object returnValue)
    {
        VM vm = (VM) getEntity();
        if (vm == null)
        {
            return;
        }

        // Check that we get a return value corresponding to a current VM.
        for (AsyncDataProvider.GetSnapshotListQueryResult result : (java.util.List<AsyncDataProvider.GetSnapshotListQueryResult>) returnValue)
        {
            if (!result.getVmId().equals(vm.getvm_guid()))
            {
                return;
            }
        }

        data = new java.util.HashMap<DiskImage, java.util.List<DiskImage>>();
        previewingImages = new java.util.HashMap<Guid, Guid>();

        for (AsyncDataProvider.GetSnapshotListQueryResult result : (java.util.List<AsyncDataProvider.GetSnapshotListQueryResult>) returnValue)
        {
            data.put(result.getDisk(), result.getSnapshots());
            previewingImages.put(result.getDisk().getId(), result.getPreviewingImage());
        }

        UpdateItems();
        UpdateActionAvailability();
    }

    @Override
    protected void OnSelectedItemChanged()
    {
        super.OnSelectedItemChanged();

        UpdateActionAvailability();

        java.util.ArrayList<String> list = new java.util.ArrayList<String>();
        if (getSelectedItem() != null && getSelectedItem().getApps() != null)
        {
            for (String item : getSelectedItem().getApps().split("[,]", -1))
            {
                list.add(item);
            }
        }

        setApps(list);
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
            UpdateData();
        }
    }

    private java.util.Map<DiskImage, java.util.List<DiskImage>> data;
    private java.util.Map<Guid, Guid> previewingImages;

    public void UpdateItems()
    {
        if (getEntity() == null)
        {
            setItems(null);
            return;
        }

        VM vm = (VM) getEntity();
        java.util.ArrayList<DiskImage> images = new java.util.ArrayList<DiskImage>();

        for (java.util.List<DiskImage> item : data.values())
        {
            java.util.ArrayList<DiskImage> temp = new java.util.ArrayList<DiskImage>();
            temp.addAll(item);

            if (item.size() > 0)
            {
                temp.remove(0);
            }

            images.addAll(temp);
        }

        // Combine snapshots by disk.
        // Items = l
        // .Where(a => a.lastModified.Date == Date.Date)
        // .OrderByDescending(a => a.lastModified.TimeOfDay)
        // .GroupBy(a => a.vm_snapshot_id)
        // .Select(a =>
        // new SnapshotModel
        // {
        // SnapshotId = a.getKey().GetValueOrDefault(),
        // Date = a.First().lastModified,
        // IsPreviewed = a.Any(b => previewingImages.Any(c => b.image_guid == c.Value)),
        // Description = a.Any(b => previewingImages.Any(c => b.image_guid == c.Value))
        // ? new EntityModel() { Value = a.First().description + " (Preview Mode)" }
        // : new EntityModel() { Value = a.First().description },
        // ParticipantDisks = a.Select(b => b.internal_drive_mapping)
        // .Distinct()
        // .OrderBy(b => b)
        // .Separate(',', b => b),
        // Apps = a.First().appList
        // }
        // )
        // .ToList();

        // C# TO JAVA CONVERTER TODO TASK: There is no Java equivalent to LINQ queries:
        Linq.OrderByDescending(images, new Linq.DiskImageByLastModifiedTimeOfDayComparer());

        java.util.HashMap<NGuid, java.util.ArrayList<DiskImage>> dict =
                new java.util.HashMap<NGuid, java.util.ArrayList<DiskImage>>();
        for (DiskImage a : images)
        {
            if (!dict.containsKey(a.getvm_snapshot_id()))
            {
                dict.put(a.getvm_snapshot_id(), new java.util.ArrayList<DiskImage>());
            }

            java.util.ArrayList<DiskImage> ls = dict.get(a.getvm_snapshot_id());
            ls.add(a);
        }

        java.util.ArrayList<SnapshotModel> items = new java.util.ArrayList<SnapshotModel>();

        // Add hard coded "Current" snapshot.
        SnapshotModel m = new SnapshotModel();
        m.setIsCurrent(true);
        m.setParticipantDisks(DriveNamesAsString(getDriveNames(data.keySet())));
        m.getDescription().setEntity("<" + vm.getvm_name() + ">");
        items.add(m);

        for (java.util.Map.Entry<NGuid, java.util.ArrayList<DiskImage>> pair : dict.entrySet())
        {
            NGuid key = pair.getKey();
            java.util.ArrayList<DiskImage> value = pair.getValue();

            DiskImage firstDiskImage = Linq.FirstOrDefault(value);

            m = new SnapshotModel();
            m.setSnapshotId(key.getValue());
            m.setDate(firstDiskImage.getlastModified());
            m.setApps(firstDiskImage.getappList());

            // IsPreviewed
            boolean isPreviewed = false;
            for (DiskImage b : value)
            {
                if (isPreviewed)
                {
                    break;
                }

                for (java.util.Map.Entry<Guid, Guid> c : previewingImages.entrySet())
                {
                    if (b.getId().equals(c.getValue()))
                    {
                        isPreviewed = true;
                        break;
                    }
                }
            }
            m.setIsPreviewed(isPreviewed);

            // Description
            String description = firstDiskImage.getdescription();
            if (isPreviewed)
            {
                description += " (Preview Mode)";
            }
            EntityModel tempVar = new EntityModel();
            tempVar.setEntity(description);
            m.setDescription(tempVar);

            m.setParticipantDisks(DriveNamesAsString(getDriveNames(value)));

            items.add(m);
        }

        // C# TO JAVA CONVERTER TODO TASK: There is no Java equivalent to LINQ queries:
        items = Linq.OrderByDescending(items, new Linq.SnapshotModelDateComparer());
        UpdateSnapshotModels(items);
    }

    private void UpdateSnapshotModels(java.util.ArrayList<SnapshotModel> items)
    {
        java.util.ArrayList<SnapshotModel> newItems = new java.util.ArrayList<SnapshotModel>();

        if (getItems() != null)
        {
            java.util.ArrayList<SnapshotModel> oldItems = Linq.ToList((Iterable<SnapshotModel>) getItems());

            for (SnapshotModel newItem : items)
            {
                boolean isItemUpdated = false;
                for (SnapshotModel item : oldItems)
                {
                    if (newItem.getSnapshotId().equals(item.getSnapshotId()))
                    {
                        item.setDate(newItem.getDate());
                        item.setApps(newItem.getApps());
                        item.setIsPreviewed(newItem.getIsPreviewed());
                        item.setDescription(newItem.getDescription());
                        item.setParticipantDisks(newItem.getParticipantDisks());
                        newItems.add(item);
                        isItemUpdated = true;
                        break;
                    }
                }

                if (!isItemUpdated)
                {
                    newItems.add(newItem);
                }
            }
        }

        setItems(newItems);
    }

    private String DriveNamesAsString(java.util.ArrayList<String> names)
    {
        String result = "";
        for (String a : names)
        {
            result += a;
            if (!StringHelper.stringsEqual(a, names.get(names.size() - 1)))
            {
                result += ", ";
            }
        }

        return result;
    }

    private java.util.ArrayList<String> getDriveNames(Iterable<DiskImage> images)
    {
        // ParticipantDisks
        java.util.ArrayList<String> list = new java.util.ArrayList<String>();
        for (DiskImage a : images)
        {
            if (!list.contains(a.getinternal_drive_mapping()))
            {
                list.add("Disk " + a.getinternal_drive_mapping());
            }
        }
        Collections.sort(list);
        return list;
    }

    public void UpdateActionAvailability()
    {
        if (getEntity() instanceof vm_pools)
        {
            return;
        }

        VM vm = (VM) getEntity();

        // bool isPreviewing = previewingImages.Any(a => a.Value != Guid.Empty);
        boolean isPreviewing = false;
        for (java.util.Map.Entry<Guid, Guid> a : previewingImages.entrySet())
        {
            if (!a.getValue().equals(NGuid.Empty))
            {
                isPreviewing = true;
                break;
            }
        }

        boolean isVmDown = vm == null || vm.getstatus() == VMStatus.Down;
        boolean isVmImageLocked = vm == null || vm.getstatus() == VMStatus.ImageLocked;

        getPreviewCommand().setIsExecutionAllowed(!isPreviewing && getSelectedItem() != null && isVmDown);

        getCanSelectSnapshot().setEntity(!isPreviewing && !isVmImageLocked);

        getNewCommand().setIsExecutionAllowed(!isPreviewing && isVmDown);

        getCommitCommand().setIsExecutionAllowed(isPreviewing && isVmDown);

        getUndoCommand().setIsExecutionAllowed(isPreviewing && isVmDown);

        getRemoveCommand().setIsExecutionAllowed(!isPreviewing && getSelectedItem() != null && isVmDown);
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
    }

    @Override
    protected String getListName() {
        return "VmSnapshotListModel";
    }
}
