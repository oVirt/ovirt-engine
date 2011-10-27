package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.*;
import org.ovirt.engine.core.common.businessentities.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "RemoveAllVmImagesParameters")
public class RemoveAllVmImagesParameters extends VmOperationParameterBase implements java.io.Serializable {
    private static final long serialVersionUID = 7211692656127711421L;
    @XmlElement
    public java.util.List<DiskImage> Images;

    public RemoveAllVmImagesParameters(Guid vmId, java.util.List<DiskImage> images) {
        super(vmId);
        this.Images = images;
        setForceDelete(false);
    }

    @XmlElement
    private boolean privateForceDelete;

    public boolean getForceDelete() {
        return privateForceDelete;
    }

    public void setForceDelete(boolean value) {
        privateForceDelete = value;
    }

    public RemoveAllVmImagesParameters() {
    }
}
