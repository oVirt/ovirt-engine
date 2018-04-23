package org.ovirt.engine.core.common.action;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.compat.Guid;

public class ImportVmTemplateParameters extends MoveOrCopyParameters implements Serializable, ImportParameters {
    private static final long serialVersionUID = -6796905699865416157L;

    public ImportVmTemplateParameters(Guid storagePoolId, Guid sourceDomainId, Guid destDomainId, Guid clusterId,
            VmTemplate template) {
        super(template.getId(), destDomainId);
        this.setVmTemplate(template);
        this.setDestDomainId(destDomainId);
        this.setSourceDomainId(sourceDomainId);
        this.setDestDomainId(destDomainId);
        this.setStorageDomainId(this.getDestDomainId());
        this.setStoragePoolId(storagePoolId);
        this.setClusterId(clusterId);
    }

    private Guid privateSourceDomainId;

    public Guid getSourceDomainId() {
        return privateSourceDomainId;
    }

    public void setSourceDomainId(Guid value) {
        privateSourceDomainId = value;
    }

    private Guid privateDestDomainId;

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

    private Guid privateClusterId;

    @Override
    public Guid getClusterId() {
        return privateClusterId;
    }

    public void setClusterId(Guid value) {
        privateClusterId = value;
    }

    private Map<Guid, DiskImage> diskTemplateMap;

    public Map<Guid, DiskImage> getDiskTemplateMap() {
        return diskTemplateMap;
    }

    public void setDiskTemplateMap(Map<Guid, DiskImage> diskTemplateMap) {
        this.diskTemplateMap = diskTemplateMap;
    }

    public ImportVmTemplateParameters() {
        privateSourceDomainId = Guid.Empty;
        privateDestDomainId = Guid.Empty;
    }
}
