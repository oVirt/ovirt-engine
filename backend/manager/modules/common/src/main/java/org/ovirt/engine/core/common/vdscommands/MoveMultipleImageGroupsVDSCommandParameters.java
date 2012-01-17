package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.compat.*;
import org.ovirt.engine.core.common.businessentities.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "MoveMultipleImageGroupsVDSCommandParameters")
public class MoveMultipleImageGroupsVDSCommandParameters extends StoragePoolDomainAndGroupIdBaseVDSCommandParameters {
    @XmlElement
    private Guid privateDstDomainId = new Guid();

    public Guid getDstDomainId() {
        return privateDstDomainId;
    }

    private void setDstDomainId(Guid value) {
        privateDstDomainId = value;
    }

    @XmlElement
    private Guid privateContainerId = new Guid();

    public Guid getContainerId() {
        return privateContainerId;
    }

    private void setContainerId(Guid value) {
        privateContainerId = value;
    }

    @XmlElement
    private java.util.ArrayList<DiskImage> privateImagesList;

    public java.util.ArrayList<DiskImage> getImagesList() {
        return privateImagesList;
    }

    private void setImagesList(java.util.ArrayList<DiskImage> value) {
        privateImagesList = value;
    }

    public MoveMultipleImageGroupsVDSCommandParameters(Guid storagePoolId, Guid storageDomainId,
            java.util.ArrayList<DiskImage> imagesList, Guid dstStorageDomainId, Guid containerId) {
        super(storagePoolId, storageDomainId, Guid.Empty);
        setDstDomainId(dstStorageDomainId);
        setContainerId(containerId);
        setImagesList(imagesList);
    }

    public MoveMultipleImageGroupsVDSCommandParameters() {
    }

    @Override
    public String toString() {
        return String.format("%s, dstDomainId = %s, containerId = %s, imageList = %s",
                super.toString(),
                getDstDomainId(),
                getContainerId(),
                getImagesList());
    }
}
