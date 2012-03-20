package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.Guid;

public class GetVmTemplatesByImageGuidParameters extends VdcQueryParametersBase {
    private static final long serialVersionUID = 903841576014422593L;
    private final Guid imageGuid;

    public GetVmTemplatesByImageGuidParameters() {
        this(new Guid());
    }

    public GetVmTemplatesByImageGuidParameters(Guid imageGuid) {
        this.imageGuid = imageGuid;
    }

    public Guid getImageGuid() {
        return imageGuid;
    }
}
