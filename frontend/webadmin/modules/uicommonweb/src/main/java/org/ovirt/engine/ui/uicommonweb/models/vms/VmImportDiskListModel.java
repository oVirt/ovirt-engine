package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.comparators.DiskByDiskAliasComparator;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;

public class VmImportDiskListModel extends SearchableListModel {
    public VmImportDiskListModel() {
        setIsTimerDisabled(true);
    }

    @Override
    protected void onEntityChanged() {
        if (getEntity() != null) {
            VM vm = (VM) getEntity();
            if (vm != null && vm.getDiskMap() != null) {
                List<Disk> disks = new ArrayList<>(vm.getDiskMap().values());
                Collections.sort(disks, new DiskByDiskAliasComparator());
                setItems(disks);
            }
        } else {
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
