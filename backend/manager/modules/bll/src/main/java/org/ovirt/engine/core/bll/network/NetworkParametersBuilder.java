package org.ovirt.engine.core.bll.network;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.SetupNetworksParameters;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkBootProtocol;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.Vlan;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.utils.NetworkUtils;

public abstract class NetworkParametersBuilder {

    public NetworkParametersBuilder() {
    }

    protected SetupNetworksParameters createSetupNetworksParameters(Guid hostId) {
        VDS host = new VDS();
        host.setId(hostId);
        NetworkConfigurator configurator = new NetworkConfigurator(host);
        List<VdsNetworkInterface> nics = configurator.filterBondsWithoutSlaves(getHostInterfaces(hostId));
        return configurator.createSetupNetworkParams(nics);
    }

    private List<VdsNetworkInterface> getHostInterfaces(Guid hostId) {
        return DbFacade.getInstance().getInterfaceDao().getAllInterfacesForVds(hostId);
    }

    protected VdsNetworkInterface createVlanDevice(VdsNetworkInterface nic, Network network) {
        VdsNetworkInterface vlan = new Vlan();
        vlan.setNetworkName(network.getName());
        vlan.setVdsId(nic.getVdsId());
        vlan.setName(NetworkUtils.getVlanDeviceName(nic, network));
        vlan.setBootProtocol(NetworkBootProtocol.NONE);
        return vlan;
    }

    protected VdsNetworkInterface getNicToConfigure(List<VdsNetworkInterface> nics, Guid id) {
        for (VdsNetworkInterface nic : nics) {
            if (nic.getId().equals(id)) {
                return nic;
            }
        }

        return null;
    }

    protected void reportNonUpdateableHosts(AuditLogType auditLogType, Set<Guid> nonUpdateableHosts) {
        if (nonUpdateableHosts.isEmpty()) {
            return;
        }

        List<String> hostNames = new ArrayList<>(nonUpdateableHosts.size());
        for (Guid hostId : nonUpdateableHosts) {
            hostNames.add(getDbFacade().getVdsStaticDao().get(hostId).getName());
        }

        AuditLogableBase logable = new AuditLogableBase();
        addValuesToLog(logable);
        logable.addCustomValue("HostNames", StringUtils.join(hostNames, ", "));
        AuditLogDirector.log(logable, auditLogType);
    }

    protected void addValuesToLog(AuditLogableBase logable) {
    }

    /**
     * Configure a network on a given network interface if the network is not a vlan or add a newly created vlan device
     * based on the given nic to the host interface
     *
     * @param nic
     *            the underlying nic (interface or bond)
     * @param nics
     *            the host nics to which a vlan should be added
     * @param network
     *            the network to attach
     */
    protected void configureNetwork(VdsNetworkInterface nic, List<VdsNetworkInterface> nics, Network network) {
        if (NetworkUtils.isVlan(network)) {
            nics.add(createVlanDevice(nic, network));
        } else if (StringUtils.isEmpty(nic.getNetworkName())) {
            nic.setNetworkName(network.getName());
        } else {
            throw new VdcBLLException(VdcBllErrors.NETWORK_LABEL_CONFLICT);
        }
    }

    private DbFacade getDbFacade() {
        return DbFacade.getInstance();
    }
}
