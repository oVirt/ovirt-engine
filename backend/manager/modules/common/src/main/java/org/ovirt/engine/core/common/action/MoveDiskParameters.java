package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.businessentities.ImageOperation;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;

public class MoveDiskParameters extends MoveOrCopyImageGroupParameters {
    private static final long serialVersionUID = 6007874805077449968L;

    public MoveDiskParameters() {
    }

    public MoveDiskParameters(Guid imageId,
            NGuid sourceDomainId,
            Guid destDomainId) {
        super(imageId, sourceDomainId, destDomainId, ImageOperation.Move);
    }
}
