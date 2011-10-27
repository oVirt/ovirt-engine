package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "CreateImageTemplateParameters")
public class CreateImageTemplateParameters extends ImagesActionsParametersBase implements java.io.Serializable {
    private static final long serialVersionUID = 1528721415797299722L;

    @XmlElement(name = "DestinationStorageDomainId")
    private Guid destinationStorageDomainId;
    @XmlElement(name = "VmTemplateId")
    private Guid privateVmTemplateId = new Guid();

    public Guid getVmTemplateId() {
        return privateVmTemplateId;
    }

    private void setVmTemplateId(Guid value) {
        privateVmTemplateId = value;
    }

    @XmlElement(name = "VmId")
    private Guid privateVmId = new Guid();

    public Guid getVmId() {
        return privateVmId;
    }

    private void setVmId(Guid value) {
        privateVmId = value;
    }

    @XmlElement(name = "VmTemplateName")
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
    }

    public void setDestinationStorageDomainId(Guid destinationStorageDomainId) {
        this.destinationStorageDomainId = destinationStorageDomainId;
    }

    public Guid getDestinationStorageDomainId() {
        return destinationStorageDomainId;
    }
}
