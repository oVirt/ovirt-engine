package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "CreateSnapshotFromTemplateParameters")
public class CreateSnapshotFromTemplateParameters extends ImagesActionsParametersBase implements java.io.Serializable {
    private static final long serialVersionUID = -3841623510039174546L;
    @XmlElement(name = "VmId")
    private Guid privateVmId = new Guid();

    public Guid getVmId() {
        return privateVmId;
    }

    private void setVmId(Guid value) {
        privateVmId = value;
    }

    public CreateSnapshotFromTemplateParameters(Guid imageId, Guid vmId) {
        super(imageId);
        setVmId(vmId);
    }

    public CreateSnapshotFromTemplateParameters() {
    }
}
