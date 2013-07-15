package org.ovirt.engine.core.bll.network;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.Backend;
import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.bll.utils.VersionSupport;
import org.ovirt.engine.core.common.AuditLogType;
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
import org.ovirt.engine.core.common.vdscommands.FutureVDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.VdsIdAndVdsVDSCommandParametersBase;
import org.ovirt.engine.core.common.vdscommands.VdsIdVDSCommandParametersBase;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.utils.NetworkUtils;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

public class NetworkConfigurator {

    private static final int VDSM_RESPONSIVENESS_PERIOD_IN_SECONDS = 120;
    private static final String MANAGEMENET_NETWORK_CONFIG_ERR = "Failed to configure management network";
    private static final String NETWORK_CONFIG_LOG_ERR = "Failed to configure management network: {0}";
    private static final long POLLING_BREAK_IN_MILLIS = 500;
    private static final Log log = LogFactory.getLog(NetworkConfigurator.class);
    private final VDS host;

    public NetworkConfigurator(VDS host) {
        this.host = host;
    }

    public void createManagementNetworkIfRequired() {
        final String managementNetwork = NetworkUtils.getEngineNetwork();

        if (host == null
                || managementNetwork.equals(host.getActiveNic())
                || !VersionSupport.isActionSupported(VdcActionType.SetupNetworks,
                        host.getVdsGroupCompatibilityVersion())) {
            return;
        }

        VdsNetworkInterface nic = findNicToSetupManagementNetwork();
        if (nic == null) {
            return;
        }

        List<VdsNetworkInterface> interfaces = filterBondsWithoutSlaves(host.getInterfaces());
        if (interfaces.contains(nic)) {
            nic.setNetworkName(managementNetwork);
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

    public boolean pollVds() {
        try {
            FutureVDSCall<VDSReturnValue> task =
                    Backend.getInstance().getResourceManager().runFutureVdsCommand(FutureVDSCommandType.Poll,
                            new VdsIdVDSCommandParametersBase(host.getId()));
            VDSReturnValue returnValue =
                    task.get(Config.<Integer> GetValue(ConfigValues.SetupNetworksPollingTimeout), TimeUnit.SECONDS);

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
                        / Config.<Integer> GetValue(ConfigValues.SetupNetworksPollingTimeout);
        for (int i = 0; i < checks; i++) {
            if (pollVds()) {
                log.infoFormat("Engine managed to communicate with VDSM agent on host {0}",
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
                        new VdsIdAndVdsVDSCommandParametersBase(host));
                return null;
            }
        });
    }

    private SetupNetworksParameters createSetupNetworkParams(List<VdsNetworkInterface> interfaces) {
        SetupNetworksParameters parameters = new SetupNetworksParameters();
        parameters.setVdsId(host.getId());
        parameters.setInterfaces(interfaces);
        parameters.setCheckConnectivity(true);
        return parameters;
    }

    private VdsNetworkInterface findNicToSetupManagementNetwork() {

        if (StringUtils.isEmpty(host.getActiveNic())) {
            log.warnFormat("No interface was reported as lastClientInterface by host {0} capabilities. "
                    + "There will be no attempt to create the management network on the host.", host.getName());
            return null;
        }

        VdsNetworkInterface nic = Entities.entitiesByName(host.getInterfaces()).get(host.getActiveNic());

        if (nic == null) {
            log.warnFormat("The lastClientInterface {0} of host {1} is not a valid interface for the mangement network."
                    + " If the interface is a bridge, it should be torn-down manually.",
                    host.getActiveNic(),
                    host.getName());
            throw new NetworkConfiguratorException(
                    String.format("lastClientIface %s is not a valid interface for management network",
                    host.getActiveNic()));
        }

        Network managementNetwork =
                getDbFacade().getNetworkDao()
                        .getByNameAndDataCenter(NetworkUtils.getEngineNetwork(), host.getStoragePoolId());

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

    private List<VdsNetworkInterface> filterBondsWithoutSlaves(List<VdsNetworkInterface> interfaces) {
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
        VdcReturnValueBase retVal = getBackend().runInternalAction(VdcActionType.SetupNetworks, parameters);
        if (retVal.getSucceeded()) {
            retVal =
                    getBackend().runInternalAction(VdcActionType.CommitNetworkChanges,
                            new VdsActionParameters(parameters.getVdsId()));
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

    private DbFacade getDbFacade() {
        return DbFacade.getInstance();
    }

    @SuppressWarnings("serial")
    public static class NetworkConfiguratorException extends RuntimeException {
        public NetworkConfiguratorException(String message) {
            super(message);
        }
    }
}
