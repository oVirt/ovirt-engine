package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.compat.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "SetStoragePoolDescriptionVDSCommandParameters")
public class SetStoragePoolDescriptionVDSCommandParameters extends IrsBaseVDSCommandParameters {
    @XmlElement(name = "Description")
    private String privateDescription;

    public String getDescription() {
        return privateDescription;
    }

    private void setDescription(String value) {
        privateDescription = value;
    }

    public SetStoragePoolDescriptionVDSCommandParameters(Guid storagePoolId, String description) {
        super(storagePoolId);
        setDescription(description);
    }

    public SetStoragePoolDescriptionVDSCommandParameters() {
    }

    @Override
    public String toString() {
        return String.format("%s, description = %s", super.toString(), getDescription());
    }
}
