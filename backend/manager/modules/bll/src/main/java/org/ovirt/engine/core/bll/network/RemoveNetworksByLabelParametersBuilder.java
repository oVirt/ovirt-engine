package org.ovirt.engine.core.bll.network;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.action.SetupNetworksParameters;
import org.ovirt.engine.core.common.businessentities.Entities;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.NetworkUtils;

public class RemoveNetworksByLabelParametersBuilder extends NetworkParametersBuilder {

    public SetupNetworksParameters buildParameters(VdsNetworkInterface nic, String label, Guid clusterId) {
        SetupNetworksParameters parameters = createSetupNetworksParameters(nic.getVdsId());
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
                } else if (StringUtils.equals(NetworkUtils.stripVlan(nic.getName()), underlyingNic.getName())) {
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
