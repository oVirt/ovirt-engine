package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.compat.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "SetImageLegalityVDSCommandParameters")
public class SetImageLegalityVDSCommandParameters extends AllStorageAndImageIdVDSCommandParametersBase {
    @XmlElement
    private boolean _isLegal;

    public SetImageLegalityVDSCommandParameters(Guid storagePoolId, Guid storageDomainId, Guid imageGroupId,
            Guid imageId, boolean isLegal) {
        super(storagePoolId, storageDomainId, imageGroupId, imageId);
        _isLegal = isLegal;
    }

    public boolean getIsLegal() {
        return _isLegal;
    }

    public SetImageLegalityVDSCommandParameters() {
    }

    @Override
    public String toString() {
        return String.format("%s, isLegal = %s", super.toString(), getIsLegal());
    }
}
