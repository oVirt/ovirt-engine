package org.ovirt.engine.core.bll.network;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.ovirt.engine.core.common.action.PersistentHostSetupNetworksParameters;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkAttachment;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VdsStaticDao;
import org.ovirt.engine.core.dao.network.InterfaceDao;
import org.ovirt.engine.core.dao.network.NetworkAttachmentDao;
import org.ovirt.engine.core.dao.network.NetworkClusterDao;

final class ManageNetworksParametersBuilderImpl extends HostSetupNetworksParametersBuilder
        implements ManageNetworksParametersBuilder {

    @Inject
    private AddNetworksByLabelParametersBuilder addNetworksByLabelParametersBuilder;

    @Inject
    private RemoveNetworksByLabelParametersBuilder removeNetworksByLabelParametersBuilder;

    @Inject
    ManageNetworksParametersBuilderImpl(InterfaceDao interfaceDao,
            VdsStaticDao vdsStaticDao,
            NetworkClusterDao networkClusterDao,
            NetworkAttachmentDao networkAttachmentDao) {
        super(interfaceDao, vdsStaticDao, networkClusterDao, networkAttachmentDao);
    }

    @Override
    public PersistentHostSetupNetworksParameters buildParameters(Guid vdsId,
            List<Network> labeledNetworksToBeAdded,
            List<Network> labeledNetworksToBeRemoved,
            Map<String, VdsNetworkInterface> nicsByLabel,
            List<Network> updatedNetworks) {

        final PersistentHostSetupNetworksParameters addSetupNetworksParameters =
                addNetworksByLabelParametersBuilder.buildParameters(vdsId, labeledNetworksToBeAdded, nicsByLabel);
        final PersistentHostSetupNetworksParameters removeSetupNetworksParameters =
                removeNetworksByLabelParametersBuilder.buildParameters(vdsId, labeledNetworksToBeRemoved);

        PersistentHostSetupNetworksParameters updatedNetworksParams = createHostSetupNetworksParameters(vdsId);

        Map<Guid, NetworkAttachment> networkIdToAttachmentMap = getNetworkIdToAttachmentMap(vdsId);

        List<NetworkAttachment> updatedNetworkAttachments =
                updatedNetworks.stream()
                        .map(Network::getId)
                        .map(networkIdToAttachmentMap::get)
                        .collect(Collectors.toList());

        updatedNetworkAttachments.forEach(networkAttachment->networkAttachment.setOverrideConfiguration(true));
        updatedNetworksParams.getNetworkAttachments().addAll(updatedNetworkAttachments);

        final PersistentHostSetupNetworksParameters combinedParams =
                combine(
                    combine(addSetupNetworksParameters, removeSetupNetworksParameters),
                    updatedNetworksParams);

        combinedParams.setNetworkNames(commaSeparateNetworkNames(Arrays.asList(
                labeledNetworksToBeAdded.stream(),
                labeledNetworksToBeRemoved.stream(),
                updatedNetworks.stream())));
        combinedParams.setCommitOnSuccess(true);
        return combinedParams;
    }

    private String commaSeparateNetworkNames(List<Stream<Network>> networks) {
        return commaSeparateNetworkNames(networks.stream().flatMap(Function.identity()));
    }

    private String commaSeparateNetworkNames(Stream<Network> networks) {
        return networks.map(Network::getName)
                .distinct()
                .collect(Collectors.joining(", "));
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
