package org.ovirt.engine.core.bll.network.cluster.transformer;

import java.util.Collection;
import java.util.List;

import org.ovirt.engine.core.common.action.PersistentSetupNetworksParameters;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;

public interface NetworkClustersToSetupNetworksParametersTransformer {

    List<PersistentSetupNetworksParameters> transform(
            Collection<NetworkCluster> attachments,
            Collection<NetworkCluster> detachments);
}
