package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "GetStorageDomainsByVmTemplateIdQueryParameters")
public class GetStorageDomainsByVmTemplateIdQueryParameters extends GetVmTemplatesDisksParameters {
    private static final long serialVersionUID = -375667038186026530L;

    public GetStorageDomainsByVmTemplateIdQueryParameters(Guid vmTemplateId) {
        super(vmTemplateId);
    }

    public GetStorageDomainsByVmTemplateIdQueryParameters() {
    }
}
