package org.ovirt.engine.core.bll.network;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.Backend;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.bll.network.cluster.ManagementNetworkUtil;
import org.ovirt.engine.core.bll.network.host.HostSetupNetworkPoller;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.action.HostSetupNetworksParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.action.VdsActionParameters;
import org.ovirt.engine.core.common.businessentities.Entities;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.network.IPv4Address;
import org.ovirt.engine.core.common.businessentities.network.IpConfiguration;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkAttachment;
import org.ovirt.engine.core.common.businessentities.network.NetworkBootProtocol;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.vdscommands.CollectHostNetworkDataVdsCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.di.Injector;
import org.ovirt.engine.core.utils.NetworkUtils;
import org.ovirt.engine.core.utils.network.predicate.InterfaceByNetworkNamePredicate;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class NetworkConfigurator {

    private static final int VDSM_RESPONSIVENESS_PERIOD_IN_SECONDS = 120;
    private static final String MANAGEMENET_NETWORK_CONFIG_ERR = "Failed to configure management network";
    private static final String NETWORK_CONFIG_LOG_ERR = "Failed to configure management network: {0}";
    private static final Logger log = LoggerFactory.getLogger(NetworkConfigurator.class);
    private final VDS host;
    private final AuditLogDirector auditLogDirector = new AuditLogDirector();
    private final Network managementNetwork;
    private CommandContext commandContext;

    public NetworkConfigurator(VDS host, CommandContext commandContext) {
        this.host = host;
        this.commandContext = commandContext;
        this.managementNetwork = getManagementNetworkUtil().getManagementNetwork(host.getVdsGroupId());
    }

    public void createManagementNetworkIfRequired() {
        if (host == null) {
            return;
        }

        final String managementNetworkName = managementNetwork.getName();
        final String hostManagementNetworkAddress = resolveHostManagementNetworkAddress(managementNetworkName);
        final String hostIp = NetworkUtils.getHostByIp(host);
        if (hostManagementNetworkAddress != null && hostManagementNetworkAddress.equals(hostIp)) {
            log.info("The management network '{}' is already configured on host '{}'",
                    managementNetworkName,
                    host.getName());
            return;
        }

        if (!FeatureSupported.setupManagementNetwork(host.getVdsGroupCompatibilityVersion())) {
            log.warn("Cluster of host '{}' does not support normalize management network feature", host.getName());
            return;
        }

        VdsNetworkInterface nic = findNicToSetupManagementNetwork();
        if (nic == null) {
            return;
        }

        List<VdsNetworkInterface> interfaces = filterBondsWithoutSlaves(host.getInterfaces());
        if (interfaces.contains(nic)) {
            configureManagementNetwork(createSetupNetworkParams(nic));
        } else {
            final AuditLogableBase event = createEvent();
            event.addCustomValue("InterfaceName", nic.getName());
            auditLogDirector.log(event,
                    AuditLogType.INVALID_INTERFACE_FOR_MANAGEMENT_NETWORK_CONFIGURATION,
                    NETWORK_CONFIG_LOG_ERR);
            throw new NetworkConfiguratorException(MANAGEMENET_NETWORK_CONFIG_ERR);
        }
    }

    private String resolveHostManagementNetworkAddress(String managementNetworkName) {
        if (managementNetworkName == null) {
            return null;
        }
        return host.getInterfaces()
                .stream()
                .filter(new InterfaceByNetworkNamePredicate(managementNetworkName))
                .map(VdsNetworkInterface::getAddress)
                .findFirst()
                .orElse(null);
    }

    private ManagementNetworkUtil getManagementNetworkUtil() {
        return Injector.get(ManagementNetworkUtil.class);
    }

    public boolean awaitVdsmResponse() {
        final int checks =
                VDSM_RESPONSIVENESS_PERIOD_IN_SECONDS
                        / Config.<Integer> getValue(ConfigValues.SetupNetworksPollingTimeout);
        HostSetupNetworkPoller poller = new HostSetupNetworkPoller();
        for (int i = 0; i < checks; i++) {
            if (poller.poll(host.getId())) {
                log.info("Engine managed to communicate with VDSM agent on host '{}' ('{}')",
                        host.getName(),
                        host.getId());
                return true;
            }
        }

        return false;
    }

    public void refreshNetworkConfiguration() {
        TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {

            @Override
            public Void runInTransaction() {
                getBackend().getResourceManager().runVdsCommand(VDSCommandType.CollectVdsNetworkDataAfterInstallation,
                        new CollectHostNetworkDataVdsCommandParameters(host));
                return null;
            }
        });
    }

    public HostSetupNetworksParameters createSetupNetworkParams(VdsNetworkInterface nic) {
        HostSetupNetworksParameters parameters = new HostSetupNetworksParameters(host.getId());
        NetworkAttachment managementAttachment = new NetworkAttachment();
        managementAttachment.setNetworkId(managementNetwork.getId());

        Map<String, VdsNetworkInterface> nicNameToNic = Entities.entitiesByName(host.getInterfaces());
        Guid baseNicId = nicNameToNic.get(NetworkUtils.stripVlan(nic)).getId();
        managementAttachment.setNicId(baseNicId);
        IpConfiguration ipConfiguration = new IpConfiguration();
        ipConfiguration.getIPv4Addresses().add(createIPv4Address(nic));
        managementAttachment.setIpConfiguration(ipConfiguration);
        parameters.getNetworkAttachments().add(managementAttachment);
        return parameters;
    }

    public IPv4Address createIPv4Address(VdsNetworkInterface nic) {
        IPv4Address result = new IPv4Address();

        if (nic.getBootProtocol() == NetworkBootProtocol.STATIC_IP) {
            result.setAddress(nic.getAddress());
            result.setNetmask(nic.getSubnet());
            result.setGateway(nic.getGateway());
        }

        result.setBootProtocol(nic.getBootProtocol());
        return result;
    }

    private VdsNetworkInterface findNicToSetupManagementNetwork() {

        VdsNetworkInterface nic = Entities.entitiesByName(host.getInterfaces()).get(host.getActiveNic());

        if (nic == null) {
            log.warn("Failed to find a valid interface for the management network of host {}."
                            + " If the interface {} is a bridge, it should be torn-down manually.",
                    host.getName(),
                    host.getActiveNic());
            throw new NetworkConfiguratorException(
                    String.format("Interface %s is invalid for management network", host.getActiveNic()));
        }

        if (managementNetwork.getName().equals(nic.getNetworkName())) {
            return null;
        }

        if (!nicHasValidVlanId(managementNetwork, nic)) {
            final AuditLogableBase event = createEvent();
            event.addCustomValue("VlanId", resolveVlanId(nic.getVlanId()));
            event.addCustomValue("MgmtVlanId", resolveVlanId(managementNetwork.getVlanId()));
            event.addCustomValue("InterfaceName", nic.getName());
            auditLogDirector.log(event,
                    AuditLogType.VLAN_ID_MISMATCH_FOR_MANAGEMENT_NETWORK_CONFIGURATION,
                    NETWORK_CONFIG_LOG_ERR);
            throw new NetworkConfiguratorException(MANAGEMENET_NETWORK_CONFIG_ERR);
        }

        return nic;
    }

    private AuditLogableBase createEvent() {
        final AuditLogableBase event = new AuditLogableBase();
        event.setVds(host);
        return event;
    }

    private String resolveVlanId(Integer vlanId) {
        return vlanId == null ? "none" : vlanId.toString();
    }

    private boolean nicHasValidVlanId(Network network, VdsNetworkInterface nic) {
        int nicVlanId = nic.getVlanId() == null ? 0 : nic.getVlanId();
        int mgmtVlanId = network.getVlanId() == null ? 0 : network.getVlanId();
        return nicVlanId == mgmtVlanId;
    }

    /**
     * filters out bonds with less than two slaves.
     * @return all non-bonds and bonds with two or more slaves.
     */
    public List<VdsNetworkInterface> filterBondsWithoutSlaves(List<VdsNetworkInterface> interfaces) {
        List<VdsNetworkInterface> filteredList = new ArrayList<>();
        Map<String, Integer> bonds = new HashMap<>();

        for (VdsNetworkInterface iface : interfaces) {
            if (Boolean.TRUE.equals(iface.getBonded())) {
                bonds.put(iface.getName(), 0);
            }
        }

        for (VdsNetworkInterface iface : interfaces) {
            if (bonds.containsKey(iface.getBondName())) {
                bonds.put(iface.getBondName(), bonds.get(iface.getBondName()) + 1);
            }
        }

        for (VdsNetworkInterface iface : interfaces) {
            if (!bonds.containsKey(iface.getName()) || bonds.get(iface.getName()) >= 2) {
                filteredList.add(iface);
            }
        }

        return filteredList;
    }

    private void configureManagementNetwork(HostSetupNetworksParameters parameters) {
        VdcReturnValueBase retVal =
                getBackend().runInternalAction(VdcActionType.HostSetupNetworks,
                        parameters,
                        cloneContextAndDetachFromParent());
        if (retVal.getSucceeded()) {
            retVal =
                    getBackend().runInternalAction(VdcActionType.CommitNetworkChanges,
                            new VdsActionParameters(parameters.getVdsId()), cloneContextAndDetachFromParent());
            if (!retVal.getSucceeded()) {
                auditLogDirector.log(createEvent(),
                        AuditLogType.PERSIST_NETWORK_FAILED_FOR_MANAGEMENT_NETWORK,
                        NETWORK_CONFIG_LOG_ERR);
                throw new NetworkConfiguratorException(MANAGEMENET_NETWORK_CONFIG_ERR);
            }
        } else {
            auditLogDirector.log(createEvent(),
                    AuditLogType.SETUP_NETWORK_FAILED_FOR_MANAGEMENT_NETWORK_CONFIGURATION,
                    NETWORK_CONFIG_LOG_ERR);
            throw new NetworkConfiguratorException(MANAGEMENET_NETWORK_CONFIG_ERR);
        }
    }

    private BackendInternal getBackend() {
        return Backend.getInstance();
    }

    private CommandContext cloneContextAndDetachFromParent() {
        return commandContext.clone().withoutCompensationContext().withoutExecutionContext().withoutLock();
    }

    public static class NetworkConfiguratorException extends RuntimeException {
        private static final long serialVersionUID = 3526212482581207006L;

        public NetworkConfiguratorException(String message) {
            super(message);
        }
    }
}
