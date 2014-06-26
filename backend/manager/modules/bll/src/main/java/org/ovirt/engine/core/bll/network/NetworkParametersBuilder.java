package org.ovirt.engine.core.bll.network;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.PersistentSetupNetworksParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkBootProtocol;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.common.businessentities.network.NetworkClusterId;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.Vlan;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.common.utils.ObjectUtils;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.utils.NetworkUtils;

public abstract class NetworkParametersBuilder {

    private CommandContext commandContext;

    public NetworkParametersBuilder(CommandContext commandContext) {
        this.commandContext = commandContext;
    }

    protected PersistentSetupNetworksParameters createSetupNetworksParameters(Guid hostId) {
        VDS host = new VDS();
        host.setId(hostId);
        NetworkConfigurator configurator = new NetworkConfigurator(host, commandContext);
        List<VdsNetworkInterface> nics = configurator.filterBondsWithoutSlaves(getHostInterfaces(hostId));

        PersistentSetupNetworksParameters parameters = new PersistentSetupNetworksParameters();
        parameters.setVdsId(host.getId());
        parameters.setInterfaces(nics);
        parameters.setCheckConnectivity(true);
        parameters.setShouldBeLogged(false);
        return parameters;
    }

    private List<VdsNetworkInterface> getHostInterfaces(Guid hostId) {
        return DbFacade.getInstance().getInterfaceDao().getAllInterfacesForVds(hostId);
    }

    protected VdsNetworkInterface createVlanDevice(VdsNetworkInterface nic, Network network) {
        VdsNetworkInterface vlan = new Vlan();
        vlan.setNetworkName(network.getName());
        vlan.setVdsId(nic.getVdsId());
        vlan.setName(NetworkUtils.constructVlanDeviceName(nic, network));
        vlan.setVlanId(network.getVlanId());
        vlan.setBaseInterface(nic.getName());
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
        NetworkCluster networkCluster = getNetworkCluster(nic, network);
        if (NetworkUtils.isVlan(network)) {
            VdsNetworkInterface vlan = createVlanDevice(nic, network);
            addBootProtocolForRoleNetwork(networkCluster, vlan);
            nics.add(vlan);
        } else if (StringUtils.isEmpty(nic.getNetworkName())) {
            nic.setNetworkName(network.getName());
            addBootProtocolForRoleNetwork(networkCluster, nic);
        } else {
            throw new VdcBLLException(VdcBllErrors.NETWORK_LABEL_CONFLICT);
        }
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
    protected VdsNetworkInterface getVlanDevice(List<VdsNetworkInterface> nics,
            VdsNetworkInterface baseNic,
            Network network) {
        for (VdsNetworkInterface n : nics) {
            if ((baseNic.getName().equals(n.getBaseInterface()) && ObjectUtils.objectsEqual(n.getVlanId(),
                    network.getVlanId()))
                    || StringUtils.equals(n.getNetworkName(), network.getName())) {
                return n;
            }
        }

        return null;
    }

    private DbFacade getDbFacade() {
        return DbFacade.getInstance();
    }

    /**
     * Update the given list of parameters to have sequencing according to their order, and have the total count. The
     * updated parameters also will be set to log the command.
     *
     * @param parameters
     *            A list of parameters to update.
     */
    public static void updateParametersSequencing(List<VdcActionParametersBase> parameters) {
        for (int i = 0; i < parameters.size(); i++) {
            PersistentSetupNetworksParameters setupNetworkParameters =
                    (PersistentSetupNetworksParameters) parameters.get(i);
            setupNetworkParameters.setSequence(i + 1);
            setupNetworkParameters.setTotal(parameters.size());
            setupNetworkParameters.setShouldBeLogged(true);
        }
    }

    protected void addBootProtocolForRoleNetwork(NetworkCluster networkCluster, VdsNetworkInterface nic) {
        if ((networkCluster.isDisplay() || networkCluster.isMigration())
                && (nic.getBootProtocol() == null || nic.getBootProtocol() == NetworkBootProtocol.NONE)) {
            nic.setBootProtocol(NetworkBootProtocol.DHCP);
        }
    }

    protected NetworkCluster getNetworkCluster(VdsNetworkInterface nic, Network network) {
        Guid clusterId = getDbFacade().getVdsStaticDao().get(nic.getVdsId()).getVdsGroupId();
        return getDbFacade().getNetworkClusterDao().get(new NetworkClusterId(clusterId, network.getId()));
    }
}
