package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.*;
import org.ovirt.engine.core.common.businessentities.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "MoveOrCopyImageGroupParameters")
public class MoveOrCopyImageGroupParameters extends ImagesContainterParametersBase implements java.io.Serializable {
    private static final long serialVersionUID = -2113154541748941225L;

    public MoveOrCopyImageGroupParameters(Guid containerId, Guid imageGroupId, Guid leafSnapshotID,
            Guid storageDomainId, ImageOperation operation) {
        super(leafSnapshotID, "", containerId);
        setStorageDomainId(storageDomainId);
        setImageGroupID(imageGroupId);
        setOperation(operation);
        setUseCopyCollapse(false);
        setVolumeFormat(VolumeFormat.Unassigned);
        setVolumeType(VolumeType.Unassigned);
        setPostZero(false);
        setForceOverride(false);
    }

    @XmlElement(name = "Operation")
    private ImageOperation privateOperation = ImageOperation.forValue(0);

    public ImageOperation getOperation() {
        return privateOperation;
    }

    private void setOperation(ImageOperation value) {
        privateOperation = value;
    }

    @XmlElement(name = "UseCopyCollapse")
    private boolean privateUseCopyCollapse;

    public boolean getUseCopyCollapse() {
        return privateUseCopyCollapse;
    }

    public void setUseCopyCollapse(boolean value) {
        privateUseCopyCollapse = value;
    }

    @XmlElement(name = "VolumeFormat")
    private VolumeFormat privateVolumeFormat = VolumeFormat.forValue(0);

    public VolumeFormat getVolumeFormat() {
        return privateVolumeFormat;
    }

    public void setVolumeFormat(VolumeFormat value) {
        privateVolumeFormat = value;
    }

    @XmlElement(name = "VolumeType")
    private VolumeType privateVolumeType = VolumeType.forValue(0);

    public VolumeType getVolumeType() {
        return privateVolumeType;
    }

    public void setVolumeType(VolumeType value) {
        privateVolumeType = value;
    }

    @XmlElement(name = "CopyVolumeType")
    private CopyVolumeType privateCopyVolumeType = CopyVolumeType.forValue(0);

    public CopyVolumeType getCopyVolumeType() {
        return privateCopyVolumeType;
    }

    public void setCopyVolumeType(CopyVolumeType value) {
        privateCopyVolumeType = value;
    }

    @XmlElement(name = "AddImageDomainMapping")
    private boolean privateAddImageDomainMapping;

    public boolean getAddImageDomainMapping() {
        return privateAddImageDomainMapping;
    }

    public void setAddImageDomainMapping(boolean value) {
        privateAddImageDomainMapping = value;
    }

    @XmlElement(name = "PostZero")
    private boolean privatePostZero;

    public boolean getPostZero() {
        return privatePostZero;
    }

    public void setPostZero(boolean value) {
        privatePostZero = value;
    }

    @XmlElement(name = "ForceOverride")
    private boolean privateForceOverride;

    public boolean getForceOverride() {
        return privateForceOverride;
    }

    public void setForceOverride(boolean value) {
        privateForceOverride = value;
    }

    private NGuid privateSourceDomainId;

    public NGuid getSourceDomainId() {
        return privateSourceDomainId;
    }

    public void setSourceDomainId(NGuid value) {
        privateSourceDomainId = value;
    }

    public MoveOrCopyImageGroupParameters() {
    }
}
