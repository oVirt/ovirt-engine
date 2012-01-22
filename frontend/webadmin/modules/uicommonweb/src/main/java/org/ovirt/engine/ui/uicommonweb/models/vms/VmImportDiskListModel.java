package org.ovirt.engine.ui.uicommonweb.models.vms;

import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VolumeType;
import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.core.compat.StringHelper;
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
            OnCollapseSnapshotsChanged();
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

    public VmImportDiskListModel() {
        setIsTimerDisabled(true);
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
            // for (DiskImage img : vm.getDiskMap().values())
            // {
            // DiskModel model = new DiskModel();
            // model.setName(img.getinternal_drive_mapping());
            // EntityModel tempVar = new EntityModel();
            // tempVar.setEntity(img.getSizeInGigabytes());
            // model.setSize(tempVar);
            // model.getVolumeType().setSelectedItem(img.getvolume_type());
            // //model.VolumeType.SelectedItemChanged += new EventHandler(VolumeType_SelectedItemChanged);
            // model.getVolumeType().getSelectedItemChangedEvent().addListener(this);
            // model.setDiskType(img.getdisk_type());
            // model.setVolumeFormat(img.getvolume_format());
            // model.setCreationDate(img.getcreation_date());
            // //NOTE: The following code won't pass conversion to Java.
            // //model.ActualSize = Convert.ToInt32(img.ActualDiskWithSnapshotsSize);
            // list.add(model);
            // }
            setItems(list);
        }
        else
        {
            setItems(null);
        }
        // SetDisksVolumeTypeAvailability();
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

}
