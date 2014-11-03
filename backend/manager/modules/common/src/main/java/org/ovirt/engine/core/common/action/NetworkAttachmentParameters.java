package org.ovirt.engine.core.common.action;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.ovirt.engine.core.common.businessentities.network.NetworkAttachment;
import org.ovirt.engine.core.compat.Guid;

public class NetworkAttachmentParameters extends VdsActionParameters {

    private static final long serialVersionUID = -5132029941161321131L;

    @Valid
    @NotNull
    private NetworkAttachment networkAttachment;

    public NetworkAttachmentParameters() {
    }

    public NetworkAttachmentParameters(Guid hostId, NetworkAttachment networkAttachment) {
        super(hostId);
        this.networkAttachment = networkAttachment;
    }

    public NetworkAttachment getNetworkAttachment() {
        return networkAttachment;
    }
}
