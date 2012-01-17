package org.ovirt.engine.core.common.vdscommands;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.ovirt.engine.core.compat.Guid;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "ValidateStorageDomainVDSCommandParameters")
public class ValidateStorageDomainVDSCommandParameters extends VdsIdVDSCommandParametersBase {
    @XmlElement(name = "StorageDomainId")
    private Guid privateStorageDomainId = new Guid();

    public Guid getStorageDomainId() {
        return privateStorageDomainId;
    }

    private void setStorageDomainId(Guid value) {
        privateStorageDomainId = value;
    }

    public ValidateStorageDomainVDSCommandParameters(Guid vdsId, Guid storageDomainId) {
        super(vdsId);
        setStorageDomainId(storageDomainId);
    }

    public ValidateStorageDomainVDSCommandParameters() {
    }

    @Override
    public String toString() {
        return String.format("%s, storageDomainId=%s", super.toString(), getStorageDomainId());
    }
}
