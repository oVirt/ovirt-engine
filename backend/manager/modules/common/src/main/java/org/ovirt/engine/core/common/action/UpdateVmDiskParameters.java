package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.*;
import org.ovirt.engine.core.common.businessentities.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "UpdateVmDiskParameters")
public class UpdateVmDiskParameters extends AddDiskToVmParameters {
    private static final long serialVersionUID = -272509502118714937L;

    public UpdateVmDiskParameters(Guid vmId, Guid imageId, DiskImageBase diskInfo) {
        super(vmId, diskInfo);
        setImageId(imageId);
    }

    @XmlElement(name = "ImageId")
    private Guid privateImageId = new Guid();

    public Guid getImageId() {
        return privateImageId;
    }

    public void setImageId(Guid value) {
        privateImageId = value;
    }

    public UpdateVmDiskParameters() {
    }
}
