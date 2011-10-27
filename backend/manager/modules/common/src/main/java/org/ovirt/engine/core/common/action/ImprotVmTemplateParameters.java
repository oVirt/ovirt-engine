package org.ovirt.engine.core.common.action;

import java.util.List;

import org.ovirt.engine.core.compat.*;
import org.ovirt.engine.core.common.businessentities.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "ImprotVmTemplateParameters")
public class ImprotVmTemplateParameters extends MoveOrCopyParameters implements java.io.Serializable {
    private static final long serialVersionUID = -6796905699865416157L;

    public ImprotVmTemplateParameters(Guid storagePoolId, Guid sourceDomainId, Guid destDomainId, Guid vdsGroupId,
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

    @XmlElement(name = "SourceDomainId")
    private Guid privateSourceDomainId = new Guid();

    public Guid getSourceDomainId() {
        return privateSourceDomainId;
    }

    public void setSourceDomainId(Guid value) {
        privateSourceDomainId = value;
    }

    @XmlElement(name = "DestDomainId")
    private Guid privateDestDomainId = new Guid();

    public Guid getDestDomainId() {
        return privateDestDomainId;
    }

    public void setDestDomainId(Guid value) {
        privateDestDomainId = value;
    }

    @XmlElement(name = "VmTemplate")
    private VmTemplate privateVmTemplate;

    public VmTemplate getVmTemplate() {
        return privateVmTemplate;
    }

    public void setVmTemplate(VmTemplate value) {
        privateVmTemplate = value;
    }

    @XmlElement
    List<DiskImage> privateImages;

    public List<DiskImage> getImages() {
        return privateImages;
    }

    public void setImages(List<DiskImage> value) {
        privateImages = value;
    }

    @XmlElement(name = "VdsGroupId")
    private Guid privateVdsGroupId;

    public Guid getVdsGroupId() {
        return privateVdsGroupId;
    }

    public void setVdsGroupId(Guid value) {
        privateVdsGroupId = value;
    }

    public ImprotVmTemplateParameters() {
    }
}
