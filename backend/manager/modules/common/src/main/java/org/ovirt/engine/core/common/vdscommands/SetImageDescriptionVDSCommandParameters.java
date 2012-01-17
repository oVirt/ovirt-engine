package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.compat.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "SetImageDescriptionVDSCommandParameters")
public class SetImageDescriptionVDSCommandParameters extends AllStorageAndImageIdVDSCommandParametersBase {
    @XmlElement
    private String _description;

    public SetImageDescriptionVDSCommandParameters(Guid storagePoolId, Guid storageDomainId, Guid imageGroupId,
            Guid imageId, String description) {
        super(storagePoolId, storageDomainId, imageGroupId, imageId);
        _description = description;
    }

    public String getDescription() {
        return _description;
    }

    public SetImageDescriptionVDSCommandParameters() {
    }

    @Override
    public String toString() {
        return String.format("%s, description = %s", super.toString(), getDescription());
    }
}
