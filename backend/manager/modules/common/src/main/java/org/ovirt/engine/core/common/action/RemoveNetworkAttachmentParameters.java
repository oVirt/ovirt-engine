package org.ovirt.engine.core.common.action;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.ovirt.engine.core.compat.Guid;

public class RemoveNetworkAttachmentParameters extends VdsActionParameters {

    private static final long serialVersionUID = -5132029941161321131L;

    @Valid
    @NotNull
    private Guid networkAttachmentId;

    public RemoveNetworkAttachmentParameters() {
    }

    public RemoveNetworkAttachmentParameters(Guid hostId, Guid networkAttachmentId) {
        super(hostId);
        this.networkAttachmentId = networkAttachmentId;
    }

    public Guid getNetworkAttachmentId() {
        return networkAttachmentId;
    }
}
