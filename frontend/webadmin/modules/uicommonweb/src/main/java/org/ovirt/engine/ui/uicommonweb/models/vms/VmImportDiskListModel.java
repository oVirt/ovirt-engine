package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;
import java.util.HashMap;

import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.Linq.DiskByAliasComparer;

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
            OnPropertyChanged(new PropertyChangedEventArgs("CollapseSnapshots")); //$NON-NLS-1$
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

    private HashMap<Guid, ArrayList<Guid>> diskStorageMap;

    public HashMap<Guid, ArrayList<Guid>> getDiskStorageMap()
    {
        return diskStorageMap;
    }

    public void setDiskStorageMap(HashMap<Guid, ArrayList<Guid>> value)
    {
        diskStorageMap = value;
    }

    public VmImportDiskListModel() {
        setIsTimerDisabled(true);

        setDiskStorageMap(new HashMap<Guid, ArrayList<Guid>>());
    }

    @Override
    protected void OnEntityChanged()
    {
        super.OnEntityChanged();
        VM vm = getEntity();
        if (vm != null && vm.getDiskMap() != null)
        {
            ArrayList<DiskImage> list = new ArrayList<DiskImage>();
            for (Disk img : vm.getDiskMap().values())
            {
                list.add((DiskImage) img);
            }

            Linq.Sort(list, new DiskByAliasComparer());
            setItems(list);
        }
        else
        {
            setItems(null);
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
