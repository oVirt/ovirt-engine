package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.Guid;

public class ExportRepoImageParameters extends ImagesActionsParametersBase {

    private static final long serialVersionUID = 8168949491104775480L;

    private Guid destinationDomainId;

    public ExportRepoImageParameters(Guid imageId) {
        super(imageId);
    }

    public Guid getDestinationDomainId() {
        return destinationDomainId;
    }

    public void setDestinationDomainId(Guid destinationDomainId) {
        this.destinationDomainId = destinationDomainId;
    }

}
