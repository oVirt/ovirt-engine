package org.ovirt.engine.core.common.action;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.ovirt.engine.core.compat.Guid;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "RemoveStorageDomainParameters")
public class RemoveStorageDomainParameters extends StorageDomainParametersBase {
    private static final long serialVersionUID = -4687251701380479912L;

    transient private boolean destroyingPool = false;

    public RemoveStorageDomainParameters(Guid storageDomainId) {
        super(storageDomainId);
    }

    @XmlElement(name = "DoFormat")
    private boolean privateDoFormat;

    public boolean getDoFormat() {
        return privateDoFormat;
    }

    public void setDoFormat(boolean value) {
        privateDoFormat = value;
    }

    public RemoveStorageDomainParameters() {
    }

    public void setDestroyingPool(boolean destroyingPool) {
        this.destroyingPool = destroyingPool;
    }

    public boolean getDestroyingPool() {
        return destroyingPool;
    }
}
