package org.ovirt.engine.core.bll.network;

import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.action.PersistentSetupNetworksParameters;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.compat.Guid;

public interface ManageLabeledNetworksParametersBuilder {
    PersistentSetupNetworksParameters buildParameters(Guid vdsId,
            List<Network> labeledNetworksToBeAdded,
            List<Network> labeledNetworksToBeRemoved,
            Map<String, VdsNetworkInterface> nicsByLabel,
            List<VdsNetworkInterface> labeledNicsToBeRemoved);
}
