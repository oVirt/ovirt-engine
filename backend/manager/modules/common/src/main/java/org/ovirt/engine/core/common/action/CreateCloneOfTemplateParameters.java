package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.*;
import org.ovirt.engine.core.common.businessentities.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "CreateCloneOfTemplateParameters")
public class CreateCloneOfTemplateParameters extends CreateSnapshotFromTemplateParameters implements
        java.io.Serializable {

    private static final long serialVersionUID = 3513412319261236990L;
    @XmlElement(name = "DiskImageBase")
    private DiskImageBase privateDiskImageBase;

    public DiskImageBase getDiskImageBase() {
        return privateDiskImageBase;
    }

    private void setDiskImageBase(DiskImageBase value) {
        privateDiskImageBase = value;
    }

    public CreateCloneOfTemplateParameters(Guid imageId, Guid vmId, DiskImageBase diskImageBase) {
        super(imageId, vmId);
        setDiskImageBase(diskImageBase);
    }

    public CreateCloneOfTemplateParameters() {
    }
}
