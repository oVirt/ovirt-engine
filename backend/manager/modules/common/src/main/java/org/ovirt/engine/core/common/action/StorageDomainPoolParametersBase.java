package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "StorageDomainPoolParametersBase")
public class StorageDomainPoolParametersBase extends StorageDomainParametersBase {
    private static final long serialVersionUID = 6248101174739394633L;

    public StorageDomainPoolParametersBase(Guid storageId, Guid storagePoolId) {
        super(storageId);
        setStoragePoolId(storagePoolId);
    }

    public StorageDomainPoolParametersBase() {
    }
}
