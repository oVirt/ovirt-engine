package org.ovirt.engine.core.bll.network;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.PersistentHostSetupNetworksParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.network.IPv4Address;
import org.ovirt.engine.core.common.businessentities.network.IpConfiguration;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkAttachment;
import org.ovirt.engine.core.common.businessentities.network.NetworkBootProtocol;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.common.businessentities.network.NetworkClusterId;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.utils.MapNetworkAttachments;
import org.ovirt.engine.core.common.utils.ObjectUtils;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.dao.VdsStaticDao;
import org.ovirt.engine.core.dao.network.InterfaceDao;
import org.ovirt.engine.core.dao.network.NetworkAttachmentDao;
import org.ovirt.engine.core.dao.network.NetworkClusterDao;
import org.ovirt.engine.core.utils.NetworkUtils;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.linq.Predicate;

public abstract class HostSetupNetworksParametersBuilder {

    private final AuditLogDirector auditLogDirector = new AuditLogDirector();
    private CommandContext commandContext;
    private List<VdsNetworkInterface> nics;
    private InterfaceDao interfaceDao;
    private VdsStaticDao vdsStaticDao;
    private NetworkClusterDao networkClusterDao;
    private NetworkAttachmentDao networkAttachmentDao;
    private Map<Guid, NetworkAttachment> networkToAttachment;

    public HostSetupNetworksParametersBuilder(CommandContext commandContext,
            InterfaceDao interfaceDao,
            VdsStaticDao vdsStaticDao,
            NetworkClusterDao networkClusterDao,
            NetworkAttachmentDao networkAttachmentDao) {
        this.commandContext = commandContext;
        this.interfaceDao = interfaceDao;
        this.vdsStaticDao = vdsStaticDao;
        this.networkClusterDao = networkClusterDao;
        this.networkAttachmentDao = networkAttachmentDao;
    }

    protected PersistentHostSetupNetworksParameters createHostSetupNetworksParameters(Guid hostId) {
        VDS host = new VDS();
        host.setId(hostId);
        NetworkConfigurator configurator = new NetworkConfigurator(host, commandContext);
        nics = configurator.filterBondsWithoutSlaves(interfaceDao.getAllInterfacesForVds(hostId));

        networkToAttachment = new MapNetworkAttachments(networkAttachmentDao.getAllForHost(hostId)).byNetworkId();

        PersistentHostSetupNetworksParameters parameters = new PersistentHostSetupNetworksParameters(hostId);
        parameters.setRollbackOnFailure(true);
        parameters.setShouldBeLogged(false);
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

    protected void reportNonUpdateableHosts(AuditLogType auditLogType, Set<Guid> nonUpdateableHosts) {
        if (nonUpdateableHosts.isEmpty()) {
            return;
        }

        List<String> hostNames = new ArrayList<>(nonUpdateableHosts.size());
        for (Guid hostId : nonUpdateableHosts) {
            hostNames.add(vdsStaticDao.get(hostId).getName());
        }

        AuditLogableBase logable = new AuditLogableBase();
        addValuesToLog(logable);
        logable.addCustomValue("HostNames", StringUtils.join(hostNames, ", "));
        auditLogDirector.log(logable, auditLogType);
    }

    protected void addValuesToLog(AuditLogableBase logable) {
    }

    protected void addAttachmentToParameters(VdsNetworkInterface baseNic,
            Network network,
            PersistentHostSetupNetworksParameters params) {
        NetworkAttachment attachmentToConfigure = getNetworkToAttachment().get(network.getId());

        if (attachmentToConfigure == null) {
            // The network is not attached to the host, attach it to the nic with the label
            attachmentToConfigure =
                    new NetworkAttachment(baseNic,
                            network,
                            NetworkUtils.createIpConfigurationFromVdsNetworkInterface(getVlanDevice(baseNic,
                                    network.getVlanId())));
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
    private VdsNetworkInterface getVlanDevice(final VdsNetworkInterface baseNic,
            final Integer vlanId) {
        if (vlanId == null) {
            return null;
        }

        return LinqUtils.firstOrNull(getNics(), new Predicate<VdsNetworkInterface>() {

            @Override
            public boolean eval(VdsNetworkInterface t) {
                return (baseNic.getName().equals(t.getBaseInterface()) && Objects.equals(t.getVlanId(), vlanId));
            }
        });
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
            if (attachment.getIpConfiguration() != null && attachment.getIpConfiguration().hasPrimaryAddressSet()) {
                IPv4Address primaryAddress = attachment.getIpConfiguration().getPrimaryAddress();

                if (primaryAddress.getBootProtocol() == null
                        || primaryAddress.getBootProtocol() == NetworkBootProtocol.NONE) {
                    primaryAddress.setBootProtocol(NetworkBootProtocol.DHCP);
                }
            } else {
                IpConfiguration ipConfiguration;
                if (attachment.getIpConfiguration() == null) {
                    ipConfiguration = new IpConfiguration();
                } else {
                    ipConfiguration = attachment.getIpConfiguration();
                }

                IPv4Address primaryAddress = new IPv4Address();
                primaryAddress.setBootProtocol(NetworkBootProtocol.DHCP);
                ipConfiguration.setIPv4Addresses(Collections.singletonList(primaryAddress));
                attachment.setIpConfiguration(ipConfiguration);
            }
        }
    }

    protected NetworkCluster getNetworkCluster(VdsNetworkInterface nic, Network network) {
        Guid clusterId = vdsStaticDao.get(nic.getVdsId()).getVdsGroupId();
        return networkClusterDao.get(new NetworkClusterId(clusterId, network.getId()));
    }

    public List<VdsNetworkInterface> getNics() {
        return nics;
    }

    public Map<Guid, NetworkAttachment> getNetworkToAttachment() {
        return networkToAttachment;
    }
}
