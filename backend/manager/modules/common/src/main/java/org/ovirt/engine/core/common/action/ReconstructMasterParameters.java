package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "ReconstructMasterParameters")
public class ReconstructMasterParameters extends StorageDomainPoolParametersBase {
    private static final long serialVersionUID = -640521915810322901L;
    @XmlElement(name = "IsDeactivate")
    private boolean privateIsDeactivate;

    public boolean getIsDeactivate() {
        return privateIsDeactivate;
    }

    public void setIsDeactivate(boolean value) {
        privateIsDeactivate = value;
    }

    public ReconstructMasterParameters(Guid storagePoolId, Guid storageDomainId, boolean isDeactivate) {
        super(storageDomainId, storagePoolId);
        setIsDeactivate(isDeactivate);
    }

    public ReconstructMasterParameters() {
    }
}
