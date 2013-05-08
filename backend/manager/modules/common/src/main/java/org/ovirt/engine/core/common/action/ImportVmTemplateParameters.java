package org.ovirt.engine.core.common.action;

import java.util.List;

import javax.validation.Valid;

import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.compat.Guid;

public class ImportVmTemplateParameters extends MoveOrCopyParameters implements java.io.Serializable {
    private static final long serialVersionUID = -6796905699865416157L;

    public ImportVmTemplateParameters(Guid storagePoolId, Guid sourceDomainId, Guid destDomainId, Guid vdsGroupId,
            VmTemplate template) {
        super(template.getId(), destDomainId);
        this.setVmTemplate(template);
        this.setDestDomainId(destDomainId);
        this.setSourceDomainId(sourceDomainId);
        this.setDestDomainId(destDomainId);
        this.setStorageDomainId(this.getDestDomainId());
        this.setStoragePoolId(storagePoolId);
        this.setVdsGroupId(vdsGroupId);
    }

    private Guid privateSourceDomainId = new Guid();

    public Guid getSourceDomainId() {
        return privateSourceDomainId;
    }

    public void setSourceDomainId(Guid value) {
        privateSourceDomainId = value;
    }

    private Guid privateDestDomainId = new Guid();

    public Guid getDestDomainId() {
        return privateDestDomainId;
    }

    public void setDestDomainId(Guid value) {
        privateDestDomainId = value;
    }

    @Valid
    private VmTemplate privateVmTemplate;

    public VmTemplate getVmTemplate() {
        return privateVmTemplate;
    }

    public void setVmTemplate(VmTemplate value) {
        privateVmTemplate = value;
    }

    List<DiskImage> privateImages;

    public List<DiskImage> getImages() {
        return privateImages;
    }

    public void setImages(List<DiskImage> value) {
        privateImages = value;
    }

    private Guid privateVdsGroupId;

    public Guid getVdsGroupId() {
        return privateVdsGroupId;
    }

    public void setVdsGroupId(Guid value) {
        privateVdsGroupId = value;
    }

    public ImportVmTemplateParameters() {
    }
}
