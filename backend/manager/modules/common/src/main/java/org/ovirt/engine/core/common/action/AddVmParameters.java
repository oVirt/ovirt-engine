package org.ovirt.engine.core.common.action;

import java.util.ArrayList;

import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.compat.Guid;

public class AddVmParameters extends VmManagementParametersBase {
    private static final long serialVersionUID = 8641610721114989096L;

    private ArrayList<DiskImage> diskInfoList;

    public AddVmParameters() {
    }

    public AddVmParameters(VmStatic vmStatic, ArrayList<DiskImage> diskInfoList,
            Guid storageDomainId) {
        super(vmStatic);
        setDiskInfoList((diskInfoList != null) ? diskInfoList : new ArrayList<DiskImage>());
        setStorageDomainId(storageDomainId);
    }

    public AddVmParameters(VM vm, ArrayList<DiskImage> diskInfoList, Guid storageDomainId) {
        this(vm.getStaticData(), diskInfoList, storageDomainId);
    }

    public ArrayList<DiskImage> getDiskInfoList() {
        return diskInfoList != null ? diskInfoList : new ArrayList<DiskImage>();
    }

    public void setDiskInfoList(ArrayList<DiskImage> value) {
        diskInfoList = value;
    }
}
