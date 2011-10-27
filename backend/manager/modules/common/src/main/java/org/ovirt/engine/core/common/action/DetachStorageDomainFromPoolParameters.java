package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "DetachStorageDomainFromPoolParameters")
public class DetachStorageDomainFromPoolParameters extends StorageDomainPoolParametersBase {
    private static final long serialVersionUID = 375203524805933936L;
    @XmlElement(name = "RemoveLast")
    private boolean privateRemoveLast;

    public boolean getRemoveLast() {
        return privateRemoveLast;
    }

    public void setRemoveLast(boolean value) {
        privateRemoveLast = value;
    }

    @XmlElement(name = "DestroyingPool")
    private boolean privateDestroyingPool;

    public boolean getDestroyingPool() {
        return privateDestroyingPool;
    }

    public void setDestroyingPool(boolean value) {
        privateDestroyingPool = value;
    }

    public DetachStorageDomainFromPoolParameters(Guid storageId, Guid storagePoolId) {
        super(storageId, storagePoolId);
    }

    public DetachStorageDomainFromPoolParameters() {
    }
}
