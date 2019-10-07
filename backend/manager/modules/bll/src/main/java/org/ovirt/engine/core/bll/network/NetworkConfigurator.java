package org.ovirt.engine.core.bll.network;

import static org.ovirt.engine.core.common.FeatureSupported.isSkipCommitNetworkChangesSupported;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.host.HostConnectivityChecker;
import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.bll.network.cluster.ManagementNetworkUtil;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.HostSetupNetworksParameters;
import org.ovirt.engine.core.common.action.VdsActionParameters;
import org.ovirt.engine.core.common.businessentities.Entities;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.network.IpConfiguration;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkAttachment;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.interfaces.VDSBrokerFrontend;
import org.ovirt.engine.core.common.utils.NetworkCommonUtils;
import org.ovirt.engine.core.common.vdscommands.CollectHostNetworkDataVdsCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogable;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableImpl;
import org.ovirt.engine.core.di.Injector;
import org.ovirt.engine.core.utils.NetworkUtils;
import org.ovirt.engine.core.utils.network.function.NicToIpv4AddressFunction;
import org.ovirt.engine.core.utils.network.function.NicToIpv6AddressFunction;
import org.ovirt.engine.core.utils.network.predicate.InterfaceByNetworkNamePredicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class NetworkConfigurator {

    private static final Logger log = LoggerFactory.getLogger(NetworkConfigurator.class);
    private static final String MANAGEMENT_NETWORK_CONFIG_ERR = "Failed to configure management network";

    private final VDS host;
    private final AuditLogDirector auditLogDirector;
    private final Network managementNetwork;
    private CommandContext commandContext;

    NetworkConfigurator(VDS host, CommandContext commandContext, AuditLogDirector auditLogDirector) {
        this.host = host;
        this.commandContext = commandContext;
        this.auditLogDirector = auditLogDirector;
        this.managementNetwork = getManagementNetworkUtil().getManagementNetwork(host.getClusterId());
    }

    public NetworkConfigurator(VDS host, CommandContext commandContext) {
        this(host, commandContext, Injector.get(AuditLogDirector.class));
    }

    public void createManagementNetworkIfRequired() {
        if (host == null) {
            return;
        }

        final String hostIp = NetworkUtils.getHostIp(host);
        final String managementNetworkName = managementNetwork.getName();

        final String hostManagementNetworkIpv4Address = getIpv4AddressOfNetwork(managementNetworkName);
        final String hostManagementNetworkIpv6Address = getIpv6AddressOfNetwork(managementNetworkName);
        if ((hostManagementNetworkIpv4Address != null && hostManagementNetworkIpv4Address.equals(hostIp)) ||
                (hostManagementNetworkIpv6Address != null && hostManagementNetworkIpv6Address.equals(hostIp))) {
            log.info("The management network '{}' is already configured on host '{}'",
                    managementNetworkName,
                    host.getName());
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
            final AuditLogable event = createEvent();
            event.addCustomValue("InterfaceName", nic.getName());
            auditLogDirector.log(event, AuditLogType.INVALID_BOND_INTERFACE_FOR_MANAGEMENT_NETWORK_CONFIGURATION);
            throw new NetworkConfiguratorException(MANAGEMENT_NETWORK_CONFIG_ERR);
        }
    }

    String getIpv4AddressOfNetwork(String networkName) {
        return resolveHostNetworkAddress(networkName, VdsNetworkInterface::getIpv4Address);
    }

    String getIpv6AddressOfNetwork(String networkName) {
        return resolveHostNetworkAddress(networkName, VdsNetworkInterface::getIpv6Address);
    }

    private String resolveHostNetworkAddress(String networkName,
            Function<VdsNetworkInterface, String> ipFunction) {
        if (networkName == null) {
            return null;
        }
        return host.getInterfaces()
                .stream()
                .filter(new InterfaceByNetworkNamePredicate(networkName))
                .map(ipFunction)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    private ManagementNetworkUtil getManagementNetworkUtil() {
        return Injector.get(ManagementNetworkUtil.class);
    }

    public boolean awaitVdsmResponse() {
        return new HostConnectivityChecker().check(host);
    }

    public void refreshNetworkConfiguration() {
        Injector.get(VDSBrokerFrontend.class).runVdsCommand(VDSCommandType.CollectVdsNetworkDataAfterInstallation,
                new CollectHostNetworkDataVdsCommandParameters(host));
    }

    public HostSetupNetworksParameters createSetupNetworkParams(VdsNetworkInterface nic) {
        HostSetupNetworksParameters parameters = new HostSetupNetworksParameters(host.getId());
        NetworkAttachment managementAttachment = new NetworkAttachment();
        managementAttachment.setNetworkId(managementNetwork.getId());

        Map<String, VdsNetworkInterface> nicNameToNic = Entities.entitiesByName(host.getInterfaces());
        Guid baseNicId = nicNameToNic.get(NetworkCommonUtils.stripVlan(nic)).getId();
        managementAttachment.setNicId(baseNicId);
        IpConfiguration ipConfiguration = new IpConfiguration();
        ipConfiguration.setIPv4Addresses(Collections.singletonList(new NicToIpv4AddressFunction().apply(nic)));
        ipConfiguration.setIpV6Addresses(Collections.singletonList(new NicToIpv6AddressFunction().apply(nic)));
        managementAttachment.setIpConfiguration(ipConfiguration);
        parameters.getNetworkAttachments().add(managementAttachment);
        parameters.setCommitOnSuccess(true);
        return parameters;
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
            final AuditLogable event = createEvent();
            event.addCustomValue("VlanId", resolveVlanId(nic.getVlanId()));
            event.addCustomValue("MgmtVlanId", resolveVlanId(managementNetwork.getVlanId()));
            event.addCustomValue("InterfaceName", nic.getName());
            auditLogDirector.log(event, AuditLogType.VLAN_ID_MISMATCH_FOR_MANAGEMENT_NETWORK_CONFIGURATION);
            throw new NetworkConfiguratorException(MANAGEMENT_NETWORK_CONFIG_ERR);
        }

        return nic;
    }

    private AuditLogable createEvent() {
        final AuditLogable event = new AuditLogableImpl();
        event.setVdsName(host.getName());
        event.setVdsId(host.getId());
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
        ActionReturnValue retVal =
                getBackend().runInternalAction(ActionType.HostSetupNetworks,
                        parameters,
                        cloneContextAndDetachFromParent());
        boolean skipCommit = parameters.isCommitOnSuccess() && isSkipCommitNetworkChangesSupported(host);
        if (retVal.getSucceeded() && !skipCommit) {
            retVal =
                    getBackend().runInternalAction(ActionType.CommitNetworkChanges,
                            new VdsActionParameters(parameters.getVdsId()), cloneContextAndDetachFromParent());
            if (!retVal.getSucceeded()) {
                auditLogDirector.log(createEvent(), AuditLogType.PERSIST_NETWORK_FAILED_FOR_MANAGEMENT_NETWORK);
                throw new NetworkConfiguratorException(MANAGEMENT_NETWORK_CONFIG_ERR);
            }
        }

        if (!retVal.getSucceeded()) {
            auditLogDirector.log(createEvent(), AuditLogType.SETUP_NETWORK_FAILED_FOR_MANAGEMENT_NETWORK_CONFIGURATION);
            throw new NetworkConfiguratorException(MANAGEMENT_NETWORK_CONFIG_ERR);
        }
    }

    BackendInternal getBackend() {
        return Injector.get(BackendInternal.class);
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
