package org.ovirt.engine.core.bll.network;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.action.PersistentSetupNetworksParameters;
import org.ovirt.engine.core.common.businessentities.Entities;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.NetworkUtils;

import static org.ovirt.engine.core.utils.linq.LinqUtils.concat;

final class ManageLabeledNetworksParametersBuilderImpl extends NetworkParametersBuilder
        implements ManageLabeledNetworksParametersBuilder {

    private final AddNetworksByLabelParametersBuilder addNetworksByLabelParametersBuilder;
    private final RemoveNetworksByLabelParametersBuilder removeNetworksByLabelParametersBuilder;

    ManageLabeledNetworksParametersBuilderImpl(CommandContext commandContext) {
        super(commandContext);
        addNetworksByLabelParametersBuilder = new AddNetworksByLabelParametersBuilder(commandContext);
        removeNetworksByLabelParametersBuilder = new RemoveNetworksByLabelParametersBuilder(commandContext);
    }

    @Override
    public PersistentSetupNetworksParameters buildParameters(Guid vdsId,
            List<Network> labeledNetworksToBeAdded,
            List<Network> labeledNetworksToBeRemoved,
            Map<String, VdsNetworkInterface> nicsByLabel,
            List<VdsNetworkInterface> labeledNicsToBeRemoved) {
        final PersistentSetupNetworksParameters addSetupNetworksParameters =
                addNetworksByLabelParametersBuilder.buildParameters(vdsId, labeledNetworksToBeAdded, nicsByLabel);
        final PersistentSetupNetworksParameters removeSetupNetworksParameters =
                removeNetworksByLabelParametersBuilder.buildParameters(vdsId,
                        labeledNetworksToBeRemoved,
                        labeledNicsToBeRemoved);
        final PersistentSetupNetworksParameters combinedParams =
                combine(addSetupNetworksParameters, removeSetupNetworksParameters);
        final Collection<Network> affectedNetworks = concat(labeledNetworksToBeAdded, labeledNetworksToBeRemoved);
        combinedParams.setNetworkNames(StringUtils.join(Entities.objectNames(affectedNetworks), ", "));
        return combinedParams;
    }

    private PersistentSetupNetworksParameters combine(PersistentSetupNetworksParameters addSetupNetworksParameters,
            PersistentSetupNetworksParameters removeSetupNetworksParameters) {
        final PersistentSetupNetworksParameters resultParam =
                createSetupNetworksParameters(addSetupNetworksParameters.getVdsId());
        final Map<String, VdsNetworkInterface> originalInterfacesByName =
                Entities.entitiesByName(resultParam.getInterfaces());
        final Map<String, VdsNetworkInterface> interfacesRemainedAfterRemoveByName =
                Entities.entitiesByName(removeSetupNetworksParameters.getInterfaces());
        final List<VdsNetworkInterface> combinedInterfaces = new ArrayList<>();
        resultParam.setInterfaces(combinedInterfaces);
        for (VdsNetworkInterface nic : addSetupNetworksParameters.getInterfaces()) {
            final String nicName = nic.getName();
            if (NetworkUtils.isVlan(nic)) {
                if (interfacesRemainedAfterRemoveByName.containsKey(nicName) ||
                        !originalInterfacesByName.containsKey(nicName)) {
                    combinedInterfaces.add(nic);
                }
            } else {
                combinedInterfaces.add(nic);
                final VdsNetworkInterface nicRemainedAfterRemove = interfacesRemainedAfterRemoveByName.get(nicName);
                final VdsNetworkInterface originalNic = originalInterfacesByName.get(nicName);
                final String networkName = getNonVlanCombinedNetworkName(nicName,
                        nicRemainedAfterRemove.getNetworkName(),
                        originalNic == null ? null : originalNic.getNetworkName());
                nic.setNetworkName(networkName);
            }
        }
        return resultParam;
    }

    private String getNonVlanCombinedNetworkName(
            String networkNameAfterAdd,
            String networkNameAfterRemove,
            String originalNetworkName) {
        if (Objects.equals(networkNameAfterAdd, originalNetworkName)) {
            return networkNameAfterRemove;
        }
        return networkNameAfterAdd;
    }

}
