package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.*;
import org.ovirt.engine.core.common.businessentities.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "AddImageFromScratchParameters")
public class AddImageFromScratchParameters extends ImagesActionsParametersBase {
    private static final long serialVersionUID = 8249273209551108387L;
    @XmlElement(name = "MasterVmId")
    private Guid privateMasterVmId = new Guid();

    public Guid getMasterVmId() {
        return privateMasterVmId;
    }

    private void setMasterVmId(Guid value) {
        privateMasterVmId = value;
    }

    @XmlElement(name = "DiskInfo")
    private DiskImageBase privateDiskInfo;

    public DiskImageBase getDiskInfo() {
        return privateDiskInfo;
    }

    private void setDiskInfo(DiskImageBase value) {
        privateDiskInfo = value;
    }

    public AddImageFromScratchParameters(Guid imageId, Guid vmTemplateId, DiskImageBase diskInfo) {
        super(imageId);
        setMasterVmId(vmTemplateId);
        setDiskInfo(diskInfo);
    }

    public AddImageFromScratchParameters() {
    }
}
