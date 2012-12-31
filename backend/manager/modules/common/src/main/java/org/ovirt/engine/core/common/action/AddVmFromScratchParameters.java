package org.ovirt.engine.core.common.action;

import java.util.ArrayList;

import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.compat.Guid;

public class AddVmFromScratchParameters extends VmManagementParametersBase {
    private static final long serialVersionUID = 8641610721114989096L;

    private ArrayList<DiskImage> privateDiskInfoList;
    private ArrayList<VmNetworkInterface> _interfaces;

    public AddVmFromScratchParameters() {
    }

    public AddVmFromScratchParameters(VmStatic vmStatic, ArrayList<DiskImage> diskInfoList,
            Guid storageDomainId) {
        super(vmStatic);
        setDiskInfoList((diskInfoList != null) ? diskInfoList : new ArrayList<DiskImage>());
        setStorageDomainId(storageDomainId);
        _interfaces = new java.util.ArrayList<VmNetworkInterface>();
    }

    public AddVmFromScratchParameters(VM vm, ArrayList<DiskImage> diskInfoList, Guid storageDomainId) {
        this(vm.getStaticData(), diskInfoList, storageDomainId);
    }

    public ArrayList<DiskImage> getDiskInfoList() {
        return privateDiskInfoList == null ? new ArrayList<DiskImage>() : privateDiskInfoList;
    }

    public void setDiskInfoList(ArrayList<DiskImage> value) {
        privateDiskInfoList = value;
    }

    public ArrayList<VmNetworkInterface> getInterfaces() {
        return _interfaces == null ? new ArrayList<VmNetworkInterface>() : _interfaces;
    }

    public void setInterfaces(ArrayList<VmNetworkInterface> value) {
        _interfaces = value;
    }

}
