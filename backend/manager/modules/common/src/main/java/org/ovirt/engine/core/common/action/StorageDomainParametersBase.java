package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "StorageDomainParametersBase")
public class StorageDomainParametersBase extends StoragePoolParametersBase implements java.io.Serializable {
    private static final long serialVersionUID = 544938640760365469L;
    @XmlElement(name = "StorageDomainId")
    private Guid privateStorageDomainId = new Guid();

    public Guid getStorageDomainId() {
        return privateStorageDomainId;
    }

    public void setStorageDomainId(Guid value) {
        privateStorageDomainId = value;
    }

    @XmlElement(name = "IsInternal")
    private boolean privateIsInternal;

    public boolean getIsInternal() {
        return privateIsInternal;
    }

    public void setIsInternal(boolean value) {
        privateIsInternal = value;
    }

    public StorageDomainParametersBase(Guid storageDomainId) {
        super(Guid.Empty);
        setStorageDomainId(storageDomainId);
    }

    public StorageDomainParametersBase() {
    }
}
