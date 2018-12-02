package org.ovirt.engine.core.bll.network;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.inject.Inject;

import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.PersistentHostSetupNetworksParameters;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.network.IPv4Address;
import org.ovirt.engine.core.common.businessentities.network.IpConfiguration;
import org.ovirt.engine.core.common.businessentities.network.Ipv4BootProtocol;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkAttachment;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.common.businessentities.network.NetworkClusterId;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.utils.MapNetworkAttachments;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VdsStaticDao;
import org.ovirt.engine.core.dao.network.InterfaceDao;
import org.ovirt.engine.core.dao.network.NetworkAttachmentDao;
import org.ovirt.engine.core.dao.network.NetworkClusterDao;
import org.ovirt.engine.core.utils.NetworkUtils;

public abstract class HostSetupNetworksParametersBuilder {

    protected InterfaceDao interfaceDao;
    protected VdsStaticDao vdsStaticDao;
    protected NetworkClusterDao networkClusterDao;
    protected NetworkAttachmentDao networkAttachmentDao;
    private Map<Guid, List<VdsNetworkInterface>> hostIdToNics = new HashMap<>();
    private Map<Guid, Map<Guid, NetworkAttachment>> networkToAttachmentByHostId = new HashMap<>();

    @Inject
    public HostSetupNetworksParametersBuilder(InterfaceDao interfaceDao,
            VdsStaticDao vdsStaticDao,
            NetworkClusterDao networkClusterDao,
            NetworkAttachmentDao networkAttachmentDao) {
        this.interfaceDao = interfaceDao;
        this.vdsStaticDao = vdsStaticDao;
        this.networkClusterDao = networkClusterDao;
        this.networkAttachmentDao = networkAttachmentDao;
    }

    protected PersistentHostSetupNetworksParameters createHostSetupNetworksParameters(Guid hostId) {
        PersistentHostSetupNetworksParameters parameters = new PersistentHostSetupNetworksParameters(hostId);
        parameters.setRollbackOnFailure(true);
        parameters.setShouldBeLogged(false);
        parameters.setCommitOnSuccess(true);
        return parameters;
    }

    protected VdsNetworkInterface getNicToConfigure(List<VdsNetworkInterface> nics, Guid id) {
        for (VdsNetworkInterface nic : nics) {
            if (nic.getId().equals(id)) {
                return nic;
            }
        }

        return null;
    }

    protected void addAttachmentToParameters(VdsNetworkInterface baseNic,
            Network network,
            PersistentHostSetupNetworksParameters params) {
        NetworkAttachment attachmentToConfigure = getNetworkIdToAttachmentMap(baseNic.getVdsId()).get(network.getId());

        if (attachmentToConfigure == null) {
            // The network is not attached to the host, attach it to the nic with the label
            VdsNetworkInterface nic = network.getVlanId() == null ? baseNic : getVlanDevice(baseNic, network.getVlanId());
            attachmentToConfigure =
                    new NetworkAttachment(baseNic,
                            network,
                            NetworkUtils.createIpConfigurationFromVdsNetworkInterface(nic));
        } else if (!attachmentToConfigure.getNicId().equals(baseNic.getId())) {
            // Move the attachment to the nic with the label
            attachmentToConfigure.setNicId(baseNic.getId());
            attachmentToConfigure.setNicName(baseNic.getName());
        }

        addBootProtocolForRoleNetworkAttachment(baseNic, network, attachmentToConfigure);

        params.getNetworkAttachments().add(attachmentToConfigure);
    }

    /**
     * Finds the vlan device among all interfaces
     *
     * @param baseNic
     *            the underlying interface of the vlan device
     * @return the vlan device if exists, else <code>null</code>
     */
    protected VdsNetworkInterface getVlanDevice(final VdsNetworkInterface baseNic,
            final Integer vlanId) {
        if (vlanId == null) {
            return null;
        }

        return getNics(baseNic.getVdsId()).stream()
                .filter(t -> baseNic.getName().equals(t.getBaseInterface()) && Objects.equals(t.getVlanId(), vlanId))
                .findFirst()
                .orElse(null);
    }

    /**
     * Update the given list of parameters to have sequencing according to their order, and have the total count. The
     * updated parameters also will be set to log the command.
     *
     * @param parameters
     *            A list of parameters to update.
     */
    public static void updateParametersSequencing(List<ActionParametersBase> parameters) {
        for (int i = 0; i < parameters.size(); i++) {
            PersistentHostSetupNetworksParameters setupNetworkParameters =
                    (PersistentHostSetupNetworksParameters) parameters.get(i);
            setupNetworkParameters.setSequence(i + 1);
            setupNetworkParameters.setTotal(parameters.size());
            setupNetworkParameters.setShouldBeLogged(true);
        }
    }

    protected void addBootProtocolForRoleNetworkAttachment(VdsNetworkInterface nic,
            Network network,
            NetworkAttachment attachment) {
        if (NetworkUtils.isRoleNetwork(getNetworkCluster(nic, network))) {
            if (attachment.getIpConfiguration() != null && attachment.getIpConfiguration().hasIpv4PrimaryAddressSet()) {
                IPv4Address primaryAddress = attachment.getIpConfiguration().getIpv4PrimaryAddress();

                if (primaryAddress.getBootProtocol() == null
                        || primaryAddress.getBootProtocol() == Ipv4BootProtocol.NONE) {
                    primaryAddress.setBootProtocol(Ipv4BootProtocol.DHCP);
                }
            } else {
                IpConfiguration ipConfiguration;
                if (attachment.getIpConfiguration() == null) {
                    ipConfiguration = new IpConfiguration();
                } else {
                    ipConfiguration = attachment.getIpConfiguration();
                }

                IPv4Address primaryAddress = new IPv4Address();
                primaryAddress.setBootProtocol(Ipv4BootProtocol.DHCP);
                ipConfiguration.setIPv4Addresses(Collections.singletonList(primaryAddress));
                attachment.setIpConfiguration(ipConfiguration);
            }
        }
    }

    protected NetworkCluster getNetworkCluster(VdsNetworkInterface nic, Network network) {
        Guid clusterId = vdsStaticDao.get(nic.getVdsId()).getClusterId();
        return networkClusterDao.get(new NetworkClusterId(clusterId, network.getId()));
    }

    public List<VdsNetworkInterface> getNics(Guid hostId) {
        if (!hostIdToNics.containsKey(hostId)) {
            VDS host = new VDS();
            host.setId(hostId);
            NetworkConfigurator configurator = new NetworkConfigurator(host, null);
            hostIdToNics.put(hostId, configurator.filterBondsWithoutSlaves(interfaceDao.getAllInterfacesForVds(hostId)));
        }

        return hostIdToNics.get(hostId);
    }

    public Map<Guid, NetworkAttachment> getNetworkIdToAttachmentMap(Guid hostId) {
        if (!networkToAttachmentByHostId.containsKey(hostId)) {
            networkToAttachmentByHostId.put(hostId,
                    new MapNetworkAttachments(networkAttachmentDao.getAllForHost(hostId)).byNetworkId());
        }

        return networkToAttachmentByHostId.get(hostId);
    }
}
