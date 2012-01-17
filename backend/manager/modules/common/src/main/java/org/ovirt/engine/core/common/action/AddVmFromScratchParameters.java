package org.ovirt.engine.core.common.action;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.ovirt.engine.core.common.businessentities.DiskImageBase;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.compat.Guid;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "AddVmFromScratchParameters")
public class AddVmFromScratchParameters extends VmManagementParametersBase implements java.io.Serializable {
    private static final long serialVersionUID = 8641610721114989096L;

    public AddVmFromScratchParameters() {
    }

    public AddVmFromScratchParameters(VmStatic vmStatic, java.util.ArrayList<DiskImageBase> diskInfoList,
            Guid storageDomainId) {
        super(vmStatic);
        setDiskInfoList((diskInfoList != null) ? diskInfoList : new java.util.ArrayList<DiskImageBase>());
        setStorageDomainId(storageDomainId);
        _interfaces = new java.util.ArrayList<VmNetworkInterface>();
    }

    public AddVmFromScratchParameters(VM vm, ArrayList<DiskImageBase> diskInfoList, Guid storageDomainId) {
        this(vm.getStaticData(), diskInfoList, storageDomainId);
    }

    @XmlElement(name = "DiskInfoList")
    private java.util.ArrayList<DiskImageBase> privateDiskInfoList;

    public java.util.ArrayList<DiskImageBase> getDiskInfoList() {
        return privateDiskInfoList == null ? new ArrayList<DiskImageBase>() : privateDiskInfoList;
    }

    public void setDiskInfoList(java.util.ArrayList<DiskImageBase> value) {
        privateDiskInfoList = value;
    }

    @XmlElement
    private java.util.ArrayList<VmNetworkInterface> _interfaces;

    public java.util.ArrayList<VmNetworkInterface> getInterfaces() {
        return _interfaces == null ? new ArrayList<VmNetworkInterface>() : _interfaces;
    }

    public void setInterfaces(java.util.ArrayList<VmNetworkInterface> value) {
        _interfaces = value;
    }

}
