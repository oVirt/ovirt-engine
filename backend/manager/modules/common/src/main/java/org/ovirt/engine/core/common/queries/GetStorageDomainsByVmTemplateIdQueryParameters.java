package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.Guid;

public class GetStorageDomainsByVmTemplateIdQueryParameters extends GetVmTemplatesDisksParameters {
    private static final long serialVersionUID = -375667038186026530L;

    public GetStorageDomainsByVmTemplateIdQueryParameters(Guid vmTemplateId) {
        super(vmTemplateId);
    }

    public GetStorageDomainsByVmTemplateIdQueryParameters() {
    }
}
