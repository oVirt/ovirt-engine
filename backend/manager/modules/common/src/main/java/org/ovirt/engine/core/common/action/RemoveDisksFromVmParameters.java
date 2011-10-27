package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "RemoveDisksFromVmParameters")
public class RemoveDisksFromVmParameters extends VmOperationParameterBase implements java.io.Serializable {
    private static final long serialVersionUID = 2612165108112157615L;

    public RemoveDisksFromVmParameters(Guid vmId, java.util.ArrayList<Guid> imageIds) {
        super(vmId);
        setImageIds(imageIds);
    }

    @XmlElement(name = "ImageIds")
    private java.util.ArrayList<Guid> privateImageIds;

    public java.util.ArrayList<Guid> getImageIds() {
        return privateImageIds;
    }

    public void setImageIds(java.util.ArrayList<Guid> value) {
        privateImageIds = value;
    }

    public RemoveDisksFromVmParameters() {
    }
}
