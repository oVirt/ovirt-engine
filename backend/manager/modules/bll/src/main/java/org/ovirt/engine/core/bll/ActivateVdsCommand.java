package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.network.cluster.NetworkClusterHelper;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.SetupNetworksParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.action.VdsActionParameters;
import org.ovirt.engine.core.common.businessentities.Entities;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.ActivateVdsVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.SetVdsStatusVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.utils.lock.EngineLock;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@SuppressWarnings("serial")
@LockIdNameAttribute
@NonTransactiveCommandAttribute
public class ActivateVdsCommand<T extends VdsActionParameters> extends VdsCommand<T> {

    public ActivateVdsCommand(T parameters) {
        super(parameters);
    }

    /**
     * Constructor for command creation when compensation is applied on startup
     *
     * @param commandId
     */
    protected ActivateVdsCommand(Guid commandId) {
        super(commandId);
    }

    @Override
    protected void executeCommand() {

        final VDS vds = getVds();
        EngineLock monitoringLock =
                new EngineLock(Collections.singletonMap(getParameters().getVdsId().toString(),
                        new Pair<String, String>(LockingGroup.VDS_INIT.name(), "")), null);
        log.infoFormat("Before acquiring lock in order to prevent monitoring for host {0} from data-center {1}",
                vds.getName(),
                vds.getStoragePoolName());
        getLockManager().acquireLockWait(monitoringLock);
        log.infoFormat("Lock acquired, from now a monitoring of host will be skipped for host {0} from data-center {1}",
                vds.getName(),
                vds.getStoragePoolName());
        try {
            ExecutionHandler.updateSpecificActionJobCompleted(vds.getId(), VdcActionType.MaintenanceVds, false);
            runVdsCommand(VDSCommandType.SetVdsStatus,
                    new SetVdsStatusVDSCommandParameters(getVdsId(), VDSStatus.Unassigned));

            VDSReturnValue returnValue =
                    runVdsCommand(VDSCommandType.ActivateVds, new ActivateVdsVDSCommandParameters(getVdsId()));
            setSucceeded(returnValue.getSucceeded());

            if (getSucceeded()) {
                createManagementNetworkIfRequired((VDS) returnValue.getReturnValue());
                TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {

                    @Override
                    public Void runInTransaction() {
                        // set network to operational / non-operational
                        List<Network> networks = getNetworkDAO().getAllForCluster(vds.getVdsGroupId());
                        for (Network net : networks) {
                            NetworkClusterHelper.setStatus(vds.getVdsGroupId(), net);
                        }
                        return null;
                    }
                });
            }
        } finally {
            getLockManager().releaseLock(monitoringLock);
            log.infoFormat("Activate finished. Lock released. Monitoring can run now for host {0} from data-center {1}",
                    vds.getName(),
                    vds.getStoragePoolName());
        }
    }

    private void createManagementNetworkIfRequired(VDS host) {
        if (host == null) {
            return;
        }

        VdsNetworkInterface nic = findNicToSetupManagementNetwork(host);
        if (nic == null) {
            return;
        }

        List<VdsNetworkInterface> interfaces = filterBondsWithoutSlaves(host.getInterfaces());
        if (interfaces.contains(nic)) {
            nic.setNetworkName(Config.<String> GetValue(ConfigValues.ManagementNetwork));
            SetupNetworksParameters parameters = new SetupNetworksParameters();
            parameters.setVdsId(host.getId());
            parameters.setInterfaces(interfaces);
            parameters.setCheckConnectivity(true);
            createAndPersistManagementNetwork(parameters);
        } else {
            addCustomValue("InterfaceName", nic.getName());
            AuditLogDirector.log(this, AuditLogType.INVALID_INTERFACE_FOR_MANAGEMENT_NETWORK_CONFIGURATION);
        }
    }

    private VdsNetworkInterface findNicToSetupManagementNetwork(final VDS host) {
        Network managementNetwork =
                getNetworkDAO().getByNameAndDataCenter(Config.<String> GetValue(ConfigValues.ManagementNetwork),
                        host.getStoragePoolId());

        if (StringUtils.isEmpty(host.getActiveNic())) {
            return null;
        }

        Map<String, VdsNetworkInterface> nameToIface = Entities.entitiesByName(host.getInterfaces());
        VdsNetworkInterface nic = nameToIface.get(host.getActiveNic());

        if (nic == null) {
            return null;
        }

        if (managementNetwork.getName().equals(nic.getNetworkName())) {
            return null;
        }

        if (!nicHasValidVlanId(managementNetwork, nic)) {
            addCustomValue("VlanId", resolveVlanId(nic.getVlanId()));
            addCustomValue("MgmtVlanId", resolveVlanId(managementNetwork.getVlanId()));
            addCustomValue("InterfaceName", nic.getName());
            AuditLogDirector.log(this, AuditLogType.VLAN_ID_MISMATCH_FOR_MANAGEMENT_NETWORK_CONFIGURATION);
            return null;
        }

        return nic;
    }

    private String resolveVlanId(Integer vlanId) {
        return vlanId == null ? "none" : vlanId.toString();
    }

    private boolean nicHasValidVlanId(Network network, VdsNetworkInterface nic) {
        int nicVlanId = nic.getVlanId() == null ? 0 : nic.getVlanId();
        int mgmtVlanId = network.getVlanId() == null ? 0 : network.getVlanId();
        return nicVlanId == mgmtVlanId;
    }

    private List<VdsNetworkInterface> filterBondsWithoutSlaves(ArrayList<VdsNetworkInterface> interfaces) {
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

    public void createAndPersistManagementNetwork(SetupNetworksParameters parameters) {
        VdcReturnValueBase retVal = getBackend().runInternalAction(VdcActionType.SetupNetworks, parameters);
        if (retVal.getSucceeded()) {
            retVal = getBackend().runInternalAction(VdcActionType.CommitNetworkChanges,
                            new VdsActionParameters(parameters.getVdsId()));
            if (!retVal.getSucceeded()) {
                AuditLogDirector.log(this, AuditLogType.PERSIST_NETWORK_FAILED_FOR_MANAGEMENT_NETWORK);
            }
        } else {
            AuditLogDirector.log(this, AuditLogType.SETUP_NETWORK_FAILED_FOR_MANAGEMENT_NETWORK_CONFIGURATION);
        }
    }

    @Override
    protected boolean canDoAction() {
        if (getVds() == null) {
            return failCanDoAction(VdcBllMessages.VDS_CANNOT_ACTIVATE_VDS_NOT_EXIST);
        }
        if (getVds().getStatus() == VDSStatus.Up) {
            return failCanDoAction(VdcBllMessages.VDS_CANNOT_ACTIVATE_VDS_ALREADY_UP);
        }
        return true;
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        return Collections.singletonMap(getParameters().getVdsId().toString(),
                LockMessagesMatchUtil.makeLockingPair(LockingGroup.VDS, VdcBllMessages.ACTION_TYPE_FAILED_OBJECT_LOCKED));
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__ACTIVATE);
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__HOST);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        if (getParameters().isRunSilent()) {
            return getSucceeded() ? AuditLogType.VDS_ACTIVATE_ASYNC : AuditLogType.VDS_ACTIVATE_FAILED_ASYNC;
        } else {
            return getSucceeded() ? AuditLogType.VDS_ACTIVATE : AuditLogType.VDS_ACTIVATE_FAILED;
        }
    }
}
