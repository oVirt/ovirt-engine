package org.ovirt.engine.core.bll.network;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.PersistentSetupNetworksParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.utils.NetworkUtils;

public class AddNetworkParametersBuilder extends NetworkParametersBuilder {

    private Network network;

    public AddNetworkParametersBuilder(Network network, CommandContext commandContext) {
        super(commandContext);
        this.network = network;
    }

    public ArrayList<VdcActionParametersBase> buildParameters(List<VdsNetworkInterface> nics) {
        Set<Guid> nonUpdateableHosts = new HashSet<>();
        ArrayList<VdcActionParametersBase> parameters = new ArrayList<>();
        boolean vlanNetwork = NetworkUtils.isVlan(network);

        for (VdsNetworkInterface nic : nics) {
            PersistentSetupNetworksParameters setupNetworkParams = createSetupNetworksParameters(nic.getVdsId());
            setupNetworkParams.setNetworkNames(network.getName());
            VdsNetworkInterface nicToConfigure = getNicToConfigure(setupNetworkParams.getInterfaces(), nic.getId());
            if (nicToConfigure == null) {
                throw new VdcBLLException(VdcBllErrors.LABELED_NETWORK_INTERFACE_NOT_FOUND);
            }

            NetworkCluster networkCluster = getNetworkCluster(nicToConfigure, network);
            if (vlanNetwork) {
                VdsNetworkInterface vlan = createVlanDevice(nic, network);
                addBootProtocolForRoleNetwork(networkCluster, vlan);
                setupNetworkParams.getInterfaces().add(vlan);
            } else if (nicToConfigure.getNetworkName() == null) {
                nicToConfigure.setNetworkName(network.getName());
                addBootProtocolForRoleNetwork(networkCluster, nicToConfigure);
            } else {
                // if a network is already assigned to that nic, it cannot be configured
                nonUpdateableHosts.add(nic.getVdsId());
                continue;
            }

            parameters.add(setupNetworkParams);
        }

        reportNonUpdateableHosts(AuditLogType.ADD_NETWORK_BY_LABEL_FAILED, nonUpdateableHosts);
        return parameters;
    }

    @Override
    protected void addValuesToLog(AuditLogableBase logable) {
        logable.setStoragePoolId(network.getDataCenterId());
        logable.addCustomValue("Network", network.getName());
        logable.addCustomValue("Label", network.getLabel());
    }
}
