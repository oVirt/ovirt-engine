package org.ovirt.engine.core.common.action;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;

public class ManageNetworkClustersParameters extends ActionParametersBase {

    private Collection<NetworkCluster> attachments;
    private Collection<NetworkCluster> detachments;
    private Collection<NetworkCluster> updates;

    public ManageNetworkClustersParameters(Collection<NetworkCluster> attachments) {
        this(attachments, Collections.emptyList());
    }

    public ManageNetworkClustersParameters(
            Collection<NetworkCluster> attachments,
            Collection<NetworkCluster> detachments) {
        this(attachments, detachments, Collections.emptyList());
    }

    public ManageNetworkClustersParameters(
            Collection<NetworkCluster> attachments,
            Collection<NetworkCluster> detachments,
            Collection<NetworkCluster> updates) {
        super();

        Objects.requireNonNull(attachments, "attachments cannot be null");
        Objects.requireNonNull(detachments, "detachments cannot be null");
        Objects.requireNonNull(updates, "updates cannot be null");

        this.attachments = attachments;
        this.detachments = detachments;
        this.updates = updates;
    }

    ManageNetworkClustersParameters() {
    }

    public Collection<NetworkCluster> getAttachments() {
        return attachments;
    }

    public Collection<NetworkCluster> getDetachments() {
        return detachments;
    }

    public Collection<NetworkCluster> getUpdates() {
        return updates;
    }
}
