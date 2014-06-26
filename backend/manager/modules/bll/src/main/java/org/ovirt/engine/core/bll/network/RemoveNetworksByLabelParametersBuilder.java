package org.ovirt.engine.core.bll.network;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.action.PersistentSetupNetworksParameters;
import org.ovirt.engine.core.common.businessentities.Entities;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.NetworkUtils;

public class RemoveNetworksByLabelParametersBuilder extends NetworkParametersBuilder {

    public RemoveNetworksByLabelParametersBuilder(CommandContext commandContext) {
        super(commandContext);
    }

    /**
     * Removes labeled networks from an interface by a given label
     */
    public PersistentSetupNetworksParameters buildParameters(VdsNetworkInterface nic, String label, Guid clusterId) {
        PersistentSetupNetworksParameters parameters = createSetupNetworksParameters(nic.getVdsId());
        List<Network> labeledNetworks =
                DbFacade.getInstance().getNetworkDao().getAllByLabelForCluster(label, clusterId);
        VdsNetworkInterface nicToConfigure = getNicToConfigure(parameters.getInterfaces(), nic.getId());
        if (nicToConfigure == null) {
            throw new VdcBLLException(VdcBllErrors.LABELED_NETWORK_INTERFACE_NOT_FOUND);
        }

        Set<VdsNetworkInterface> nicsToRemove =
                getNicsToRemove(parameters.getInterfaces(), labeledNetworks, nicToConfigure);

        // remove the label from the nic to be passed to setup-networks
        unlabelConfiguredNic(label, nicToConfigure);

        // remove the networks from all of the nics
        parameters.getInterfaces().removeAll(nicsToRemove);
        return parameters;
    }

    /**
     * Removes a given list of labeled networks from a host
     */
    public PersistentSetupNetworksParameters buildParameters(Guid hostId,
            List<Network> networksToRemove,
            List<VdsNetworkInterface> nics) {
        PersistentSetupNetworksParameters parameters = createSetupNetworksParameters(hostId);

        for (VdsNetworkInterface nic : nics) {
            VdsNetworkInterface nicToConfigure = getNicToConfigure(parameters.getInterfaces(), nic.getId());
            if (nicToConfigure == null) {
                throw new VdcBLLException(VdcBllErrors.LABELED_NETWORK_INTERFACE_NOT_FOUND);
            }

            Set<VdsNetworkInterface> nicsToRemove =
                    getNicsToRemove(parameters.getInterfaces(), networksToRemove, nicToConfigure);
            parameters.getInterfaces().removeAll(nicsToRemove);
        }

        parameters.setNetworkNames(StringUtils.join(Entities.objectNames(networksToRemove), ", "));

        return parameters;
    }

    private Set<VdsNetworkInterface> getNicsToRemove(List<VdsNetworkInterface> nics,
            List<Network> labeledNetworks,
            VdsNetworkInterface underlyingNic) {
        Map<String, VdsNetworkInterface> nicsByNetworkName = Entities.hostInterfacesByNetworkName(nics);
        Set<VdsNetworkInterface> nicsToRemove = new HashSet<>();

        for (Network network : labeledNetworks) {
            VdsNetworkInterface nic = nicsByNetworkName.get(network.getName());
            if (nic != null) {
                if (StringUtils.equals(nic.getName(), underlyingNic.getName())) {
                    underlyingNic.setNetworkName(null);
                } else if (NetworkUtils.interfaceBasedOn(nic, underlyingNic.getName())) {
                    nicsToRemove.add(nic);
                }
            }
        }

        return nicsToRemove;
    }

    private void unlabelConfiguredNic(String label, VdsNetworkInterface nicToConfigure) {
        if (NetworkUtils.isLabeled(nicToConfigure)) {
            nicToConfigure.getLabels().remove(label);
        }
    }
}
