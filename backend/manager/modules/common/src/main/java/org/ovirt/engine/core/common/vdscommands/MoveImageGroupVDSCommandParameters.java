package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.compat.*;
import org.ovirt.engine.core.common.businessentities.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "MoveImageGroupVDSCommandParameters")
public class MoveImageGroupVDSCommandParameters extends StoragePoolDomainAndGroupIdBaseVDSCommandParameters {
    @XmlElement(name = "DstDomainId")
    private Guid privateDstDomainId = new Guid();

    public Guid getDstDomainId() {
        return privateDstDomainId;
    }

    private void setDstDomainId(Guid value) {
        privateDstDomainId = value;
    }

    @XmlElement(name = "VmId")
    private Guid privateVmId = new Guid();

    public Guid getVmId() {
        return privateVmId;
    }

    private void setVmId(Guid value) {
        privateVmId = value;
    }

    @XmlElement(name = "Op")
    private ImageOperation privateOp = ImageOperation.forValue(0);

    public ImageOperation getOp() {
        return privateOp;
    }

    private void setOp(ImageOperation value) {
        privateOp = value;
    }

    public MoveImageGroupVDSCommandParameters(Guid storagePoolId, Guid storageDomainId, Guid imageGroupId,
            Guid dstStorageDomainId, Guid vmId, ImageOperation op, boolean postZero, boolean force,
            String compatibilityVersion) {
        super(storagePoolId, storageDomainId, imageGroupId);
        setDstDomainId(dstStorageDomainId);
        setVmId(vmId);
        setOp(op);
        setPostZero(postZero);
        setForce(force);
        setCompatibilityVersion(compatibilityVersion);
    }

    @XmlElement
    private boolean privatePostZero;

    public boolean getPostZero() {
        return privatePostZero;
    }

    public void setPostZero(boolean value) {
        privatePostZero = value;
    }

    @XmlElement
    private boolean privateForce;

    public boolean getForce() {
        return privateForce;
    }

    public void setForce(boolean value) {
        privateForce = value;
    }

    public MoveImageGroupVDSCommandParameters() {
    }

    @Override
    public String toString() {
        return String.format("%s, dstDomainId = %s, vmId = %s, op = %s, postZero = %s, force = %s",
                super.toString(),
                getDstDomainId(),
                getVmId(),
                getOp(),
                getPostZero(),
                getForce());
    }
}
