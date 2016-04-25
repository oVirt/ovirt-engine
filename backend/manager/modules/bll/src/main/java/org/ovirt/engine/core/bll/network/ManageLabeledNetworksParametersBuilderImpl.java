package org.ovirt.engine.core.bll.network;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.ovirt.engine.core.common.action.PersistentHostSetupNetworksParameters;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VdsStaticDao;
import org.ovirt.engine.core.dao.network.InterfaceDao;
import org.ovirt.engine.core.dao.network.NetworkAttachmentDao;
import org.ovirt.engine.core.dao.network.NetworkClusterDao;

final class ManageLabeledNetworksParametersBuilderImpl extends HostSetupNetworksParametersBuilder
        implements ManageLabeledNetworksParametersBuilder {

    @Inject
    private AddNetworksByLabelParametersBuilder addNetworksByLabelParametersBuilder;

    @Inject
    private RemoveNetworksByLabelParametersBuilder removeNetworksByLabelParametersBuilder;

    @Inject
    ManageLabeledNetworksParametersBuilderImpl(InterfaceDao interfaceDao,
            VdsStaticDao vdsStaticDao,
            NetworkClusterDao networkClusterDao,
            NetworkAttachmentDao networkAttachmentDao) {
        super(interfaceDao, vdsStaticDao, networkClusterDao, networkAttachmentDao);
    }

    @Override
    public PersistentHostSetupNetworksParameters buildParameters(Guid vdsId,
            List<Network> labeledNetworksToBeAdded,
            List<Network> labeledNetworksToBeRemoved,
            Map<String, VdsNetworkInterface> nicsByLabel) {
        final PersistentHostSetupNetworksParameters addSetupNetworksParameters =
                addNetworksByLabelParametersBuilder.buildParameters(vdsId, labeledNetworksToBeAdded, nicsByLabel);
        final PersistentHostSetupNetworksParameters removeSetupNetworksParameters =
                removeNetworksByLabelParametersBuilder.buildParameters(vdsId,
                        labeledNetworksToBeRemoved);
        final PersistentHostSetupNetworksParameters combinedParams =
                combine(addSetupNetworksParameters, removeSetupNetworksParameters);
        final Collection<Network> affectedNetworks =
                Stream.concat(labeledNetworksToBeAdded.stream(), labeledNetworksToBeRemoved.stream())
                        .collect(Collectors.toList());
        combinedParams.setNetworkNames
                (affectedNetworks.stream().map(Network::getName).collect(Collectors.joining(", ")));
        return combinedParams;
    }

    private PersistentHostSetupNetworksParameters combine(PersistentHostSetupNetworksParameters addSetupNetworksParameters,
            PersistentHostSetupNetworksParameters removeSetupNetworksParameters) {
        Guid hostId = addSetupNetworksParameters.getVdsId();
        final PersistentHostSetupNetworksParameters resultParam = createHostSetupNetworksParameters(hostId);
        extendParameters(resultParam, addSetupNetworksParameters);
        extendParameters(resultParam, removeSetupNetworksParameters);
        return resultParam;
    }

    private void extendParameters(PersistentHostSetupNetworksParameters resultParam,
            PersistentHostSetupNetworksParameters setupNetworksParameters) {
        resultParam.getNetworkAttachments().addAll(setupNetworksParameters.getNetworkAttachments());
        resultParam.getRemovedNetworkAttachments().addAll(setupNetworksParameters.getRemovedNetworkAttachments());
        resultParam.getRemovedUnmanagedNetworks().addAll(setupNetworksParameters.getRemovedUnmanagedNetworks());
    }
}
