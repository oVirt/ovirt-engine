package org.ovirt.engine.core.common.action;

import java.util.HashMap;

import org.ovirt.engine.core.compat.*;
import org.ovirt.engine.core.common.businessentities.*;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import org.ovirt.engine.core.common.queries.*;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "AddVmFromTemplateParameters")
public class AddVmFromTemplateParameters extends VmManagementParametersBase implements java.io.Serializable {
    private static final long serialVersionUID = -3400982291165788716L;

    @XmlElement(name = "DiskInfoList")
    public ValueObjectMap getSerializedDiskInfoList() {
        return new ValueObjectMap(diskInfoList, false);
    }

    public void setSerializedDiskInfoList(ValueObjectMap serializedDiskInfoList) {
        diskInfoList = (serializedDiskInfoList == null) ? null : serializedDiskInfoList.asMap();
    }

    // This is used for the internal java implementation
    private java.util.Map<String, DiskImageBase> diskInfoList;

    public java.util.Map<String, DiskImageBase> getDiskInfoList() {
        return diskInfoList;
    }

    public void setDiskInfoList(java.util.HashMap<String, DiskImageBase> value) {
        diskInfoList = value;
    }

    @XmlElement
    public Guid OriginalTemplate = new Guid();

    public AddVmFromTemplateParameters(VmStatic vmStatic, java.util.HashMap<String, DiskImageBase> diskInfoList,
            Guid storageDomainId) {
        super(vmStatic);
        setStorageDomainId(storageDomainId);
        setDiskInfoList(diskInfoList);
    }

    public AddVmFromTemplateParameters() {
    }

    public AddVmFromTemplateParameters(VM vm, HashMap<String, DiskImageBase> diskInfoList, Guid storageDomainId) {
        this(vm.getStaticData(), diskInfoList, storageDomainId);
    }
}
