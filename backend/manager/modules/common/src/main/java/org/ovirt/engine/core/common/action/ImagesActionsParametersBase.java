package org.ovirt.engine.core.common.action;

import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.ovirt.engine.core.compat.Guid;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "ImagesActionsParametersBase")
public class ImagesActionsParametersBase extends StorageDomainParametersBase implements java.io.Serializable {
    private static final long serialVersionUID = -8414109153288146048L;
    @XmlElement(name = "ImageId")
    private Guid _imageId = new Guid();

    public ImagesActionsParametersBase(Guid imageId) {
        super(Guid.Empty);
        _imageId = imageId;
    }

    public Guid getImageId() {
        return _imageId;
    }

    @XmlElement(name = "DestinationImageId")
    private Guid privateDestinationImageId = new Guid();

    public Guid getDestinationImageId() {
        return privateDestinationImageId;
    }

    public void setDestinationImageId(Guid value) {
        privateDestinationImageId = value;
    }

    @XmlElement(name = "Description")
    private String privateDescription;
    private String oldDescription;
    private Date oldLastModifiedValue;

    public String getDescription() {
        return privateDescription;
    }

    public void setDescription(String value) {
        privateDescription = value;
    }

    public String getOldDescription() {
        return oldDescription;
    }

    public void setOldDescription(String oldDescription) {
        this.oldDescription = oldDescription;
    }

    public void setOldLastModifiedValue(Date oldLastModifiedValue) {
        this.oldLastModifiedValue = oldLastModifiedValue;
    }

    public Date getOldLastModifiedValue() {
        return oldLastModifiedValue;
    }

    @XmlElement(name = "VmSnapshotId")
    private Guid privateVmSnapshotId = new Guid();

    public Guid getVmSnapshotId() {
        return privateVmSnapshotId;
    }

    public void setVmSnapshotId(Guid value) {
        privateVmSnapshotId = value;
    }

    @XmlElement(name = "ImageGroupID")
    private Guid privateImageGroupID = new Guid();

    public Guid getImageGroupID() {
        return privateImageGroupID;
    }

    public void setImageGroupID(Guid value) {
        privateImageGroupID = value;
    }

    public ImagesActionsParametersBase() {
    }
}
