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
import org.ovirt.engine.core.common.businessentities.network.NetworkBootProtocol;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.Vlan;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.utils.NetworkUtils;

public class AddNetworkParametersBuilder extends NetworkParametersBuilder {

    private Network network;

    public AddNetworkParametersBuilder(Network network) {
        this.network = network;
    }

    public ArrayList<VdcActionParametersBase> buildParameters(List<VdsNetworkInterface> nics) {
        Set<Guid> nonUpdateableHosts = new HashSet<>();
        ArrayList<VdcActionParametersBase> parameters = new ArrayList<>();
        boolean vlanNetwork = NetworkUtils.isVlan(network);

        for (VdsNetworkInterface nic : nics) {
            SetupNetworksParameters setupNetworkParams = createSetupNetworksParameters(nic.getVdsId());
            VdsNetworkInterface nicToConfigure = getNicToConfigure(setupNetworkParams.getInterfaces(), nic.getId());

            if (vlanNetwork) {
                VdsNetworkInterface vlan = createVlanDevice(nic, nicToConfigure.getVdsId(), network);
                setupNetworkParams.getInterfaces().add(vlan);
            } else if (nicToConfigure.getNetworkName() == null) {
                nicToConfigure.setNetworkName(network.getName());
            } else {
                // if a network is already assigned to that nic, it cannot be configured
                nonUpdateableHosts.add(nic.getVdsId());
                continue;
            }

            parameters.add(setupNetworkParams);
        }

        reportNonUpdateableHosts(nonUpdateableHosts);
        return parameters;
    }

    private void reportNonUpdateableHosts(Set<Guid> nonUpdateableHosts) {
        if (nonUpdateableHosts.isEmpty()) {
            return;
        }

        List<String> hostNames = new ArrayList<>(nonUpdateableHosts.size());
        for (Guid hostId : nonUpdateableHosts) {
            hostNames.add(getDbFacade().getVdsStaticDao().get(hostId).getName());
        }

        AuditLogableBase logable = new AuditLogableBase();
        logable.setStoragePoolId(network.getDataCenterId());
        logable.addCustomValue("Network", network.getName());
        logable.addCustomValue("HostNames", StringUtils.join(hostNames, ", "));
        logable.addCustomValue("Label", network.getLabel());
        AuditLogDirector.log(logable, AuditLogType.ADD_NETWORK_BY_LABEL_FAILED);
    }

    private VdsNetworkInterface createVlanDevice(VdsNetworkInterface nic, Guid hostId, Network network) {
        VdsNetworkInterface vlan = new Vlan();
        vlan.setNetworkName(network.getName());
        vlan.setVdsId(hostId);
        vlan.setName(NetworkUtils.getVlanDeviceName(nic, network));
        vlan.setBootProtocol(NetworkBootProtocol.NONE);
        return vlan;
    }

    private VdsNetworkInterface getNicToConfigure(List<VdsNetworkInterface> nics, Guid id) {
        for (VdsNetworkInterface nic : nics) {
            if (nic.getId().equals(id)) {
                return nic;
            }
        }

        return null;
    }

    private DbFacade getDbFacade() {
        return DbFacade.getInstance();
    }
}
