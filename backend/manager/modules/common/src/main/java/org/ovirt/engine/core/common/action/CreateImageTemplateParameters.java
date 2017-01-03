package org.ovirt.engine.core.common.action;

import java.io.Serializable;

import org.ovirt.engine.core.common.businessentities.storage.CopyVolumeType;
import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.storage.VolumeType;
import org.ovirt.engine.core.compat.Guid;

public class CreateImageTemplateParameters extends ImagesActionsParametersBase implements Serializable {
    private static final long serialVersionUID = 1528721415797299722L;

    private Guid destinationStorageDomainId;
    private Guid privateVmTemplateId;
    private VolumeType volumeType;
    private VolumeFormat volumeFormat;
    private CopyVolumeType copyVolumeType = CopyVolumeType.SharedVol;

    public Guid getVmTemplateId() {
        return privateVmTemplateId;
    }

    private void setVmTemplateId(Guid value) {
        privateVmTemplateId = value;
    }

    private Guid privateVmId;

    public Guid getVmId() {
        return privateVmId;
    }

    private void setVmId(Guid value) {
        privateVmId = value;
    }

    private String privateVmTemplateName;

    public String getVmTemplateName() {
        return privateVmTemplateName;
    }

    private void setVmTemplateName(String value) {
        privateVmTemplateName = value;
    }

    public CreateImageTemplateParameters(Guid imageId, Guid vmTemplateId, String vmTemplateName, Guid vmId) {
        super(imageId);
        setVmTemplateId(vmTemplateId);
        setVmTemplateName(vmTemplateName);
        setVmId(vmId);
    }

    public CreateImageTemplateParameters() {
        privateVmTemplateId = Guid.Empty;
        privateVmId = Guid.Empty;
    }

    public void setDestinationStorageDomainId(Guid destinationStorageDomainId) {
        this.destinationStorageDomainId = destinationStorageDomainId;
    }

    public Guid getDestinationStorageDomainId() {
        return destinationStorageDomainId;
    }

    public VolumeFormat getVolumeFormat() {
        return volumeFormat;
    }

    public void setVolumeFormat(VolumeFormat volumeFormat) {
        this.volumeFormat = volumeFormat;
    }

    public VolumeType getVolumeType() {
        return volumeType;
    }

    public void setVolumeType(VolumeType volumeType) {
        this.volumeType = volumeType;
    }

    public CopyVolumeType getCopyVolumeType() {
        return copyVolumeType;
    }

    public void setCopyVolumeType(CopyVolumeType copyVolumeType) {
        this.copyVolumeType = copyVolumeType;
    }
}
