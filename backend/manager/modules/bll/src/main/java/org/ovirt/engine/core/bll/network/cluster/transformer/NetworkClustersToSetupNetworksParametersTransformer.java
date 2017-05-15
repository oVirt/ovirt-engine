package org.ovirt.engine.core.bll.network.cluster.transformer;

import java.util.Collection;
import java.util.List;

import org.ovirt.engine.core.common.action.PersistentHostSetupNetworksParameters;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;

public interface NetworkClustersToSetupNetworksParametersTransformer {

    List<PersistentHostSetupNetworksParameters> transform(
            Collection<NetworkCluster> attachments,
            Collection<NetworkCluster> detachments,
            Collection<NetworkCluster> updates);
}
