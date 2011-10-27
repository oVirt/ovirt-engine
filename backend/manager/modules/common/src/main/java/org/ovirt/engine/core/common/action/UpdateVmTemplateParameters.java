package org.ovirt.engine.core.common.action;

import javax.validation.Valid;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.ovirt.engine.core.common.businessentities.VmTemplate;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "UpdateVmTemplateParameters")
public class UpdateVmTemplateParameters extends VmTemplateParametersBase {
    private static final long serialVersionUID = 7250355162926369307L;
    @XmlElement
    @Valid
    private VmTemplate _vmTemplate;

    public UpdateVmTemplateParameters(VmTemplate vmTemplate) {
        _vmTemplate = vmTemplate;
    }

    public VmTemplate getVmTemplateData() {
        return _vmTemplate;
    }

    public UpdateVmTemplateParameters() {
    }
}
