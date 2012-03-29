package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;
import java.util.HashMap;

import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VolumeType;
import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;

@SuppressWarnings("unused")
public class VmImportDiskListModel extends VmDiskListModel
{
    private boolean collapseSnapshots;

    public boolean getCollapseSnapshots()
    {
        return collapseSnapshots;
    }

    public void setCollapseSnapshots(boolean value)
    {
        if (collapseSnapshots != value)
        {
            collapseSnapshots = value;
            // OnCollapseSnapshotsChanged();
            OnPropertyChanged(new PropertyChangedEventArgs("CollapseSnapshots"));
        }
    }

    private int privateSelectedVMsCount;

    public int getSelectedVMsCount()
    {
        return privateSelectedVMsCount;
    }

    public void setSelectedVMsCount(int value)
    {
        privateSelectedVMsCount = value;
    }

    private void OnCollapseSnapshotsChanged()
    {
        SetDisksVolumeTypeAvailability();
    }

    private HashMap<Guid, ArrayList<Guid>> diskStorageMap;

    public HashMap<Guid, ArrayList<Guid>> getDiskStorageMap()
    {
        return diskStorageMap;
    }

    public void setDiskStorageMap(HashMap<Guid, ArrayList<Guid>> value)
    {
        diskStorageMap = value;
        OnPropertyChanged(new PropertyChangedEventArgs("DiskStorageMap"));
    }

    public VmImportDiskListModel() {
        setIsTimerDisabled(true);

        setDiskStorageMap(new HashMap<Guid, ArrayList<Guid>>());
    }

    @Override
    protected void OnEntityChanged()
    {
        super.OnEntityChanged();
        VM vm = (VM) getEntity();
        if (vm != null && vm.getDiskMap() != null)
        {
            java.util.ArrayList<DiskImage> list = new java.util.ArrayList<DiskImage>();
            for (DiskImage img : vm.getDiskMap().values())
            {
                list.add(img);
            }
            setItems(list);
        }
        else
        {
            setItems(null);
        }

        if (vm != null && !NGuid.Empty.equals(vm.getvmt_guid())) {
            AsyncDataProvider.GetTemplateDiskList(new AsyncQuery(this,
                    new INewAsyncCallback() {
                        @Override
                        public void OnSuccess(Object target, Object returnValue) {
                            VmImportDiskListModel vmImportDiskListModel = (VmImportDiskListModel) target;
                            ArrayList<DiskImage> disks = (ArrayList<DiskImage>) returnValue;

                            for (DiskImage diskImage : (ArrayList<DiskImage>) vmImportDiskListModel.getItems())
                            {
                                for (DiskImage disk : disks) {
                                    if (disk.getId().equals(diskImage.getParentId())) {
                                        ArrayList<Guid> storageIds = disk.getstorage_ids();
                                        diskStorageMap.put(diskImage.getId(), storageIds);
                                        break;
                                    }
                                }
                            }

                            setDiskStorageMap(diskStorageMap);
                        }
                    }), vm.getvmt_guid());
        }
    }

    @Override
    public void eventRaised(Event ev, Object sender, EventArgs args)
    {
        super.eventRaised(ev, sender, args);

        if (ev.equals(SelectedItemChangedEventDefinition) && sender instanceof ListModel)
        {
            // VolumeType_SelectedItemChanged(args);
        }
    }

    public void VolumeType_SelectedItemChanged(DiskImage disk, VolumeType selectedVolumeType)
    {
        VM vm = (VM) getEntity();
        if (vm != null)
        {
            for (Object item : getItems())
            {
                for (java.util.Map.Entry<String, DiskImage> kvp : vm.getDiskMap().entrySet())
                {
                    DiskImage innerDisk = kvp.getValue();
                    if (StringHelper.stringsEqual(innerDisk.getinternal_drive_mapping(),
                            disk.getinternal_drive_mapping()))
                    {
                        innerDisk.setvolume_type(selectedVolumeType);
                        break;
                    }
                }
            }
        }
    }

    private void SetDisksVolumeTypeAvailability()
    {
        java.util.ArrayList<DiskModel> list = (java.util.ArrayList<DiskModel>) getItems();
        if (list != null && list.size() > 0)
        {
            if (getSelectedVMsCount() == 1)
            {
                if (!getCollapseSnapshots())
                {
                    // list.Each(a =>
                    // {
                    // AvailabilityDecorator.GetChangeProhibitionReasons(a.VolumeType).Add("Allocation can be modified only when 'Collapse Snapshots' is check");
                    // AvailabilityDecorator.IsChangable = a.VolumeType, false);
                    // });
                    for (DiskModel a : list)
                    {
                        a.getVolumeType()
                                .getChangeProhibitionReasons()
                                .add("Allocation can be modified only when 'Collapse Snapshots' is check");
                        a.getVolumeType().setIsChangable(false);
                    }
                }
                else
                {
                    // list.Each(a => AvailabilityDecorator.IsChangable = a.VolumeType, true));
                    for (DiskModel a : list)
                    {
                        a.getVolumeType().setIsChangable(true);
                    }
                }
            }
            else
            {
                // list.Each(a =>
                // {
                // AvailabilityDecorator.GetChangeProhibitionReasons(a.VolumeType).Add("Allocation can be modified only when importing a single VM");
                // AvailabilityDecorator.IsChangable = a.VolumeType, false);
                // });
                for (DiskModel a : list)
                {
                    a.getVolumeType()
                            .getChangeProhibitionReasons()
                            .add("Allocation can be modified only when importing a single VM");
                    a.getVolumeType().setIsChangable(false);
                }
            }
        }
    }

    @Override
    public void Search()
    {
    }

    public ArrayList<Guid> getAvailableStorageDomainsByDiskId(Guid diskId) {
        return diskStorageMap.get(diskId);
    }
}
