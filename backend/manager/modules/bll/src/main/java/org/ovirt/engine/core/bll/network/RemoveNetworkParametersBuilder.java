package org.ovirt.engine.core.bll.network;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.SetupNetworksParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.utils.NetworkUtils;

public class RemoveNetworkParametersBuilder extends NetworkParametersBuilder {

    private Network network;

    public RemoveNetworkParametersBuilder(Network network) {
        this.network = network;
    }

    public ArrayList<VdcActionParametersBase> buildParameters(List<VdsNetworkInterface> nics) {
        Set<Guid> nonUpdateableHosts = new HashSet<>();
        ArrayList<VdcActionParametersBase> parameters = new ArrayList<>();

        if (NetworkUtils.isManagementNetwork(network)) {
            return parameters;
        }

        boolean vlanNetwork = NetworkUtils.isVlan(network);

        for (VdsNetworkInterface nic : nics) {
            SetupNetworksParameters setupNetworkParams = createSetupNetworksParameters(nic.getVdsId());
            VdsNetworkInterface nicToConfigure = getNicToConfigure(setupNetworkParams.getInterfaces(), nic.getId());
            if (nicToConfigure == null) {
                throw new VdcBLLException(VdcBllErrors.LABELED_NETWORK_INTERFACE_NOT_FOUND);
            }

            if (network.getName().equals(nicToConfigure.getNetworkName())) {
                nicToConfigure.setNetworkName(null);
            } else if (vlanNetwork) {
                VdsNetworkInterface vlan = getVlanDevice(setupNetworkParams.getInterfaces(), nicToConfigure);

                if (vlan == null) {
                    nonUpdateableHosts.add(nic.getVdsId());
                } else {
                    setupNetworkParams.getInterfaces().remove(vlan);
                }
            } else {
                // if a network is assigned to nic other than the labeled one
                nonUpdateableHosts.add(nic.getVdsId());
                continue;
            }

            parameters.add(setupNetworkParams);
        }

        reportNonUpdateableHosts(AuditLogType.REMOVE_NETWORK_BY_LABEL_FAILED, nonUpdateableHosts);
        return parameters;
    }

    @Override
    protected void addValuesToLog(AuditLogableBase logable) {
        logable.setStoragePoolId(network.getDataCenterId());
        logable.addCustomValue("Network", network.getName());
    }

    /**
     * Finds the vlan device among all interfaces, either by the network name or by vlan-id
     *
     * @param nics
     *            the host interfaces
     * @param baseNic
     *            the underlying interface of the vlan device
     * @return the vlan device if exists, else <code>null</code>
     */
    private VdsNetworkInterface getVlanDevice(List<VdsNetworkInterface> nics, VdsNetworkInterface baseNic) {
        for (VdsNetworkInterface n : nics) {
            if (StringUtils.equals(n.getName(), NetworkUtils.getVlanDeviceName(baseNic, network))
                    || StringUtils.equals(n.getNetworkName(), network.getName())) {
                return n;
            }
        }

        return null;
    }
}
