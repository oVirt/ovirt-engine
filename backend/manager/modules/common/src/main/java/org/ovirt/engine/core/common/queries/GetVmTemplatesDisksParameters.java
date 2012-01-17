package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "GetVmTemplatesDisksParameters")
public class GetVmTemplatesDisksParameters extends GetVmsByVmTemplateGuidParameters {
    private static final long serialVersionUID = -4340274643795202346L;

    public GetVmTemplatesDisksParameters(Guid vmTemplateId) {
        super(vmTemplateId);
    }

    public GetVmTemplatesDisksParameters() {
    }
}
