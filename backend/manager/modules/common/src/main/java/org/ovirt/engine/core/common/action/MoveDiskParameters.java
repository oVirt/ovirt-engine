package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.businessentities.storage.ImageOperation;
import org.ovirt.engine.core.compat.Guid;

public class MoveDiskParameters extends MoveOrCopyImageGroupParameters {
    private static final long serialVersionUID = 6007874805077449968L;

    public MoveDiskParameters() {
    }

    public MoveDiskParameters(Guid imageId,
            Guid sourceDomainId,
            Guid destDomainId) {
        super(imageId, sourceDomainId, destDomainId, ImageOperation.Move);
    }
}
