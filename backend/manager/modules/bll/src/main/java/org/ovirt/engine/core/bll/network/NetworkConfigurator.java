package org.ovirt.engine.core.bll.network;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.Backend;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.bll.network.cluster.ManagementNetworkUtil;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.action.SetupNetworksParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.action.VdsActionParameters;
import org.ovirt.engine.core.common.businessentities.Entities;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.interfaces.FutureVDSCall;
import org.ovirt.engine.core.common.vdscommands.CollectHostNetworkDataVdsCommandParameters;
import org.ovirt.engine.core.common.vdscommands.FutureVDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.VdsIdVDSCommandParametersBase;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.di.Injector;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NetworkConfigurator {

    private static final int VDSM_RESPONSIVENESS_PERIOD_IN_SECONDS = 120;
    private static final String MANAGEMENET_NETWORK_CONFIG_ERR = "Failed to configure management network";
    private static final String NETWORK_CONFIG_LOG_ERR = "Failed to configure management network: {0}";
    private static final long POLLING_BREAK_IN_MILLIS = 500;
    private static final Logger log = LoggerFactory.getLogger(NetworkConfigurator.class);
    private final VDS host;
    private CommandContext commandContext;

    public NetworkConfigurator(VDS host, CommandContext commandContext) {
        this.host = host;
        this.commandContext = commandContext;
    }

    public void createManagementNetworkIfRequired() {

        if (host == null) {
            return;
        }

        final ManagementNetworkUtil managementNetworkUtil = getManagementNetworkUtil();
        final Network managementNetwork = managementNetworkUtil.getManagementNetwork(host.getVdsGroupId());
        final String managementNetworkName = managementNetwork.getName();
        if (managementNetworkName.equals(host.getActiveNic())) {
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
            nic.setNetworkName(managementNetworkName);
            configureManagementNetwork(createSetupNetworkParams(interfaces));
        } else {
            final AuditLogableBase event = createEvent();
            event.addCustomValue("InterfaceName", nic.getName());
            AuditLogDirector.log(event,
                    AuditLogType.INVALID_INTERFACE_FOR_MANAGEMENT_NETWORK_CONFIGURATION,
                    NETWORK_CONFIG_LOG_ERR);
            throw new NetworkConfiguratorException(MANAGEMENET_NETWORK_CONFIG_ERR);
        }
    }

    private ManagementNetworkUtil getManagementNetworkUtil() {
        return Injector.get(ManagementNetworkUtil.class);
    }

    public boolean pollVds() {
        try {
            FutureVDSCall<VDSReturnValue> task =
                    Backend.getInstance().getResourceManager().runFutureVdsCommand(FutureVDSCommandType.Poll,
                            new VdsIdVDSCommandParametersBase(host.getId()));
            VDSReturnValue returnValue =
                    task.get(Config.<Integer> getValue(ConfigValues.SetupNetworksPollingTimeout), TimeUnit.SECONDS);

            if (returnValue.getSucceeded()) {
                return true;
            }
        } catch (Exception e) {
            // ignore failure until VDSM become responsive
        }
        return false;
    }

    public boolean awaitVdsmResponse() {
        final int checks =
                VDSM_RESPONSIVENESS_PERIOD_IN_SECONDS
                        / Config.<Integer> getValue(ConfigValues.SetupNetworksPollingTimeout);
        for (int i = 0; i < checks; i++) {
            if (pollVds()) {
                log.info("Engine managed to communicate with VDSM agent on host '{}' ('{}')",
                        host.getName(),
                        host.getId());
                return true;
            } else {
                delayPolling();
            }
        }
        return false;
    }

    private void delayPolling() {
        try {
            Thread.sleep(POLLING_BREAK_IN_MILLIS);
        } catch (InterruptedException e) {
            // ignore exception
        }
    }

    public void refreshNetworkConfiguration() {
        TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {

            @Override
            public Void runInTransaction() {
                getBackend().getResourceManager().RunVdsCommand(VDSCommandType.CollectVdsNetworkDataAfterInstallation,
                        new CollectHostNetworkDataVdsCommandParameters(host));
                return null;
            }
        });
    }

    public SetupNetworksParameters createSetupNetworkParams(List<VdsNetworkInterface> interfaces) {
        SetupNetworksParameters parameters = new SetupNetworksParameters();
        parameters.setVdsId(host.getId());
        parameters.setInterfaces(interfaces);
        parameters.setCheckConnectivity(true);
        return parameters;
    }

    private VdsNetworkInterface findNicToSetupManagementNetwork() {

        if (StringUtils.isEmpty(host.getActiveNic())) {
            log.warn("No interface was reported as lastClientInterface by host '{}' capabilities. "
                    + "There will be no attempt to create the management network on the host.", host.getName());
            return null;
        }

        VdsNetworkInterface nic = Entities.entitiesByName(host.getInterfaces()).get(host.getActiveNic());

        if (nic == null) {
            log.warn("The lastClientInterface '{}' of host '{}' is not a valid interface for the management network."
                    + " If the interface is a bridge, it should be torn-down manually.",
                    host.getActiveNic(),
                    host.getName());
            throw new NetworkConfiguratorException(
                    String.format("lastClientIface %s is not a valid interface for management network",
                    host.getActiveNic()));
        }

        final Network managementNetwork = getManagementNetworkUtil().getManagementNetwork(host.getVdsGroupId());

        if (managementNetwork.getName().equals(nic.getNetworkName())) {
            return null;
        }

        if (!nicHasValidVlanId(managementNetwork, nic)) {
            final AuditLogableBase event = createEvent();
            event.addCustomValue("VlanId", resolveVlanId(nic.getVlanId()));
            event.addCustomValue("MgmtVlanId", resolveVlanId(managementNetwork.getVlanId()));
            event.addCustomValue("InterfaceName", nic.getName());
            AuditLogDirector.log(event,
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

    public List<VdsNetworkInterface> filterBondsWithoutSlaves(List<VdsNetworkInterface> interfaces) {
        List<VdsNetworkInterface> filteredList = new ArrayList<VdsNetworkInterface>();
        Map<String, Integer> bonds = new HashMap<String, Integer>();

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

    private void configureManagementNetwork(SetupNetworksParameters parameters) {
        VdcReturnValueBase retVal =
                getBackend().runInternalAction(VdcActionType.SetupNetworks, parameters, cloneContextAndDetachFromParent());
        if (retVal.getSucceeded()) {
            retVal =
                    getBackend().runInternalAction(VdcActionType.CommitNetworkChanges,
                            new VdsActionParameters(parameters.getVdsId()), cloneContextAndDetachFromParent());
            if (!retVal.getSucceeded()) {
                AuditLogDirector.log(createEvent(),
                        AuditLogType.PERSIST_NETWORK_FAILED_FOR_MANAGEMENT_NETWORK,
                        NETWORK_CONFIG_LOG_ERR);
                throw new NetworkConfiguratorException(MANAGEMENET_NETWORK_CONFIG_ERR);
            }
        } else {
            AuditLogDirector.log(createEvent(),
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
