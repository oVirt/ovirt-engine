package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;
import java.util.Collections;

import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.ui.uicommonweb.Linq.DiskByAliasComparer;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;

public class VmImportDiskListModel extends SearchableListModel
{
    public VmImportDiskListModel() {
        setIsTimerDisabled(true);
    }

    @Override
    protected void onEntityChanged()
    {
        if (getEntity() != null) {
            VM vm = (VM) getEntity();
            if (vm != null && vm.getDiskMap() != null)
            {
                ArrayList<DiskImage> list = new ArrayList<DiskImage>();
                for (Disk img : vm.getDiskMap().values())
                {
                    list.add((DiskImage) img);
                }

                Collections.sort(list, new DiskByAliasComparer());
                setItems(list);
            }
        }
        else
        {
            setItems(null);
        }
    }

    @Override
    public void setEntity(Object value) {
        super.setEntity(value == null ? null : ((ImportVmData) value).getVm());
    }

    @Override
    protected String getListName() {
        return "VmImportDiskListModel"; //$NON-NLS-1$
    }
}
