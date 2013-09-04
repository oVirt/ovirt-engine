package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.Guid;

public class ExportRepoImageParameters extends ImagesActionsParametersBase {

    private static final long serialVersionUID = 5665917269570010693L;

    private Guid destinationDomainId;

    public ExportRepoImageParameters() {
    }

    public ExportRepoImageParameters(Guid sourceImageGroupId, Guid destinationDomainId) {
        super();
        setImageGroupID(sourceImageGroupId);
        setDestinationDomainId(destinationDomainId);
    }

    public Guid getDestinationDomainId() {
        return destinationDomainId;
    }

    public void setDestinationDomainId(Guid destinationDomainId) {
        this.destinationDomainId = destinationDomainId;
    }

}
