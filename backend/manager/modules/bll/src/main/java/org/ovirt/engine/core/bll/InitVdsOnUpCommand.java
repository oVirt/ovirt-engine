package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.storage.StorageHandlingCommandBase;
import org.ovirt.engine.core.bll.storage.StoragePoolStatusHandler;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.HostStoragePoolParametersBase;
import org.ovirt.engine.core.common.action.ReconstructMasterParameters;
import org.ovirt.engine.core.common.action.SetNonOperationalVdsParameters;
import org.ovirt.engine.core.common.action.StoragePoolParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.FenceActionType;
import org.ovirt.engine.core.common.businessentities.FenceStatusReturnValue;
import org.ovirt.engine.core.common.businessentities.NonOperationalReason;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VdsSpmStatus;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterServerInfo;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.common.eventqueue.Event;
import org.ovirt.engine.core.common.eventqueue.EventQueue;
import org.ovirt.engine.core.common.eventqueue.EventResult;
import org.ovirt.engine.core.common.eventqueue.EventType;
import org.ovirt.engine.core.common.vdscommands.ConnectStoragePoolVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.VdsIdAndVdsVDSCommandParametersBase;
import org.ovirt.engine.core.common.vdscommands.VdsIdVDSCommandParametersBase;
import org.ovirt.engine.core.common.vdscommands.gluster.AddGlusterServerVDSParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AlertDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.dao.network.InterfaceDao;
import org.ovirt.engine.core.utils.ejb.BeanProxyType;
import org.ovirt.engine.core.utils.ejb.BeanType;
import org.ovirt.engine.core.utils.ejb.EjbUtils;
import org.ovirt.engine.core.vdsbroker.irsbroker.IrsBrokerCommand;

/**
 * Initialize Vds on its loading. For storages: First connect all storage
 * servers to VDS. Second connect Vds to storage Pool.
 *
 * After server initialized - its will be moved to Up status.
 */
@SuppressWarnings("serial")
@NonTransactiveCommandAttribute
public class InitVdsOnUpCommand<T extends StoragePoolParametersBase> extends StorageHandlingCommandBase<T> {
    private boolean _fenceSucceeded = true;
    private boolean _vdsProxyFound;
    private boolean _connectStorageSucceeded, _connectPoolSucceeded;
    private boolean _glusterPeerListSucceeded, _glusterPeerProbeSucceeded;
    private FenceStatusReturnValue _fenceStatusReturnValue;

    public InitVdsOnUpCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected void executeCommand() {
        VDSGroup vdsGroup = getVdsGroup();

        if (vdsGroup.supportsVirtService()) {
            setSucceeded(initVirtResources());
        }

        if (vdsGroup.supportsGlusterService()) {
            setSucceeded(initGlusterPeerProcess());
        }
    }

    private boolean initVirtResources() {
        if (InitializeStorage()) {
            processFence();
            processStoragePoolStatus();
        } else {
            Map<String, String> customLogValues = Collections.singletonMap("StoragePoolName", getStoragePoolName());
            setNonOperational(NonOperationalReason.STORAGE_DOMAIN_UNREACHABLE, customLogValues);
            return false;
        }
        return true;
    }

    private void processFence() {
        FenceExecutor executor = new FenceExecutor(getVds(), FenceActionType.Status);
        // check first if we have any VDS to act as the proxy for fence
        // actions.
        if (getVds().getpm_enabled() && executor.FindVdsToFence()) {
            VDSReturnValue returnValue = executor.Fence();
            _fenceSucceeded = returnValue.getSucceeded();
            _fenceStatusReturnValue = (FenceStatusReturnValue) returnValue.getReturnValue();
            _vdsProxyFound = true;
        }
    }

    private void processStoragePoolStatus() {
        if (getVds().getspm_status() != VdsSpmStatus.None) {
            storage_pool pool = DbFacade.getInstance().getStoragePoolDao().get(getVds().getStoragePoolId());
            if (pool != null && pool.getstatus() == StoragePoolStatus.NotOperational) {
                pool.setstatus(StoragePoolStatus.Problematic);
                DbFacade.getInstance().getStoragePoolDao().updateStatus(pool.getId(), pool.getstatus());
                StoragePoolStatusHandler.PoolStatusChanged(pool.getId(), pool.getstatus());
            }
        }
    }

    private void setNonOperational(NonOperationalReason reason, Map<String, String> customLogValues) {
        SetNonOperationalVdsParameters tempVar =
                new SetNonOperationalVdsParameters(getVds().getId(), reason, customLogValues);
        tempVar.setSaveToDb(true);
        Backend.getInstance().runInternalAction(VdcActionType.SetNonOperationalVds, tempVar,  ExecutionHandler.createInternalJobContext());
    }

    private boolean InitializeStorage() {
        boolean returnValue = false;
        setStoragePoolId(getVds().getStoragePoolId());

        // if no pool or pool is uninitialized or in maintenance mode no need to
        // connect any storage
        if (getStoragePool() == null || StoragePoolStatus.Uninitialized == getStoragePool().getstatus()
                || StoragePoolStatus.Maintanance == getStoragePool().getstatus()) {
            returnValue = true;
            _connectStorageSucceeded = true;
            _connectPoolSucceeded = true;
        } else {
            HostStoragePoolParametersBase params = new HostStoragePoolParametersBase(getStoragePool(), getVds());
            if (Backend.getInstance()
                    .runInternalAction(VdcActionType.ConnectHostToStoragePoolServers, params)
                    .getSucceeded()) {
                _connectStorageSucceeded = true;
                returnValue = connectHostToPool();
                _connectPoolSucceeded = returnValue;
            }
        }
        return returnValue;
    }

    /**
     * The following method should connect host to pool
     * The method will perform a connect storage pool operation,
     * if operation will wail on StoragePoolWrongMaster or StoragePoolMasterNotFound errors
     * we will try to run reconstruct
     * @return
     */
    private boolean connectHostToPool() {
        final VDS vds = getVds();
        EventResult result =
                ((EventQueue) EjbUtils.findBean(BeanType.EVENTQUEUE_MANAGER, BeanProxyType.LOCAL)).submitEventSync(new Event(getStoragePool().getId(),
                        null, vds.getId(), EventType.VDSCONNECTTOPOOL),
                        new Callable<EventResult>() {
                            @Override
                            public EventResult call() {
                                return runConnectHostToPoolEvent(getStoragePool().getId(), vds);
                            }
                        });
        if (result != null) {
            return result.isSuccess();
        }
        return false;
    }

    private EventResult runConnectHostToPoolEvent(final Guid storagePoolId, final VDS vds) {
        EventResult result = new EventResult(true, EventType.VDSCONNECTTOPOOL);
        storage_pool storagePool = getStoragePoolDAO().get(storagePoolId);
        Guid masterDomainIdFromDb = getStorageDomainDAO().getMasterStorageDomainIdForPool(storagePoolId);
        try {
            runVdsCommand(VDSCommandType.ConnectStoragePool,
                    new ConnectStoragePoolVDSCommandParameters(vds.getId(), storagePoolId,
                            vds.getvds_spm_id(), masterDomainIdFromDb,
                            storagePool.getmaster_domain_version())).getSucceeded();
        } catch (VdcBLLException e) {
            if (e.getErrorCode() == VdcBllErrors.StoragePoolWrongMaster
                    || e.getErrorCode() == VdcBllErrors.StoragePoolMasterNotFound) {
                boolean returnValue =
                        Backend.getInstance()
                                .runInternalAction(VdcActionType.ReconstructMasterDomain,
                                        new ReconstructMasterParameters(vds.getStoragePoolId(),
                                                masterDomainIdFromDb, vds.getId(), true)).getSucceeded();
                result = new EventResult(returnValue, EventType.RECONSTRUCT);
            } else {
                log.errorFormat("Could not connect host {0} to pool {1}", vds.getvds_name(), storagePool
                        .getname());
                result.setSuccess(false);
            }
        } catch (RuntimeException exp) {
            log.errorFormat("Could not connect host {0} to pool {1}", vds.getvds_name(), storagePool
                    .getname());
            result.setSuccess(false);
        }
        if (result.isSuccess() && result.getEventType() != EventType.RECONSTRUCT) {
            result.setSuccess(proceedVdsStats());
            if (!result.isSuccess()) {
                AuditLogDirector.log(new AuditLogableBase(getVdsId()),
                        AuditLogType.VDS_STORAGE_VDS_STATS_FAILED);
            }
        }
        return result;
    }

    protected boolean proceedVdsStats() {
        boolean returnValue = true;
        try {
            runVdsCommand(VDSCommandType.GetStats, new VdsIdAndVdsVDSCommandParametersBase(getVds()));
            if (IrsBrokerCommand.isDomainsReportedAsProblematic(getVds().getStoragePoolId(), getVds().getDomains())) {
                log.errorFormat("One of the Storage Domains of host {0} in pool {1} is problematic",
                        getVds().getvds_name(),
                        getStoragePool()
                                .getname());
                returnValue = false;
            }
        } catch (VdcBLLException e) {
            log.errorFormat("Could not get Host statistics for Host {0}, Error is {1}",
                    getVds().getvds_name(),
                    e);
            returnValue = false;
        }
        return returnValue;
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        AuditLogType type = AuditLogType.UNASSIGNED;

        if(!getVdsGroup().supportsVirtService()) {
            if (getVdsGroup().supportsGlusterService()) {
                if (!_glusterPeerListSucceeded) {
                    type = AuditLogType.GLUSTER_SERVERS_LIST_FAILED;
                } else if (!_glusterPeerProbeSucceeded) {
                    type = AuditLogType.GLUSTER_SERVER_ADD_FAILED;
                }
            }
            return type;
        }

        if (!_connectStorageSucceeded) {
            type = AuditLogType.CONNECT_STORAGE_SERVERS_FAILED;
        } else if (!_connectPoolSucceeded) {
            type = AuditLogType.CONNECT_STORAGE_POOL_FAILED;
        } else if (getVds().getpm_enabled() && _fenceSucceeded) {
            type = AuditLogType.VDS_FENCE_STATUS;
        } else if (getVds().getpm_enabled() && !_fenceSucceeded) {
            type = AuditLogType.VDS_FENCE_STATUS_FAILED;
        }

        // PM alerts
        AuditLogableBase logable = new AuditLogableBase(getVds().getId());
        if (getVds().getpm_enabled()) {
            if (!_vdsProxyFound) {
                logable.AddCustomValue("Reason",
                        AuditLogDirector.GetMessage(AuditLogType.VDS_ALERT_FENCE_NO_PROXY_HOST));
                AlertDirector.Alert(logable, AuditLogType.VDS_ALERT_FENCE_TEST_FAILED);
            } else if (!_fenceStatusReturnValue.getIsSucceeded()) {
                logable.AddCustomValue("Reason", _fenceStatusReturnValue.getMessage());
                AlertDirector.Alert(logable, AuditLogType.VDS_ALERT_FENCE_TEST_FAILED);
            }
        } else {
            AlertDirector.Alert(logable, AuditLogType.VDS_ALERT_FENCE_IS_NOT_CONFIGURED);
        }
        return type;
    }

    private boolean initGlusterPeerProcess() {
        _glusterPeerListSucceeded = true;
        _glusterPeerProbeSucceeded = true;
        List<VDS> vdsList = getVdsDAO().getAllForVdsGroupWithStatus(getVdsGroupId(), VDSStatus.Up);
        // If the cluster already having Gluster servers, get an up server
        if (vdsList != null && vdsList.size() > 0) {
            VDS upServer = null;
            for (VDS vds : vdsList) {
                if (!getVdsId().equals(vds.getId())) {
                    upServer = vds;
                    break;
                }
            }

            // If new server is not part of the existing gluster peers, add into peer group
            if (upServer != null) {
                List<GlusterServerInfo> glusterServers = getGlusterPeers(upServer.getId());
                Map<String, String> customLogValues = new HashMap<String, String>();
                customLogValues.put("Server", upServer.gethost_name());
                if (glusterServers.size() == 0) {
                    customLogValues.put("Command", "gluster peer status");
                    setNonOperational(NonOperationalReason.GLUSTER_COMMAND_FAILED, customLogValues);
                    return false;
                } else if (!hostExists(glusterServers, getVds())) {
                    if (!glusterPeerProbe(upServer.getId(), getVds().gethost_name())) {
                        customLogValues.put("Command", "gluster peer probe " + getVds().gethost_name());
                        setNonOperational(NonOperationalReason.GLUSTER_COMMAND_FAILED, customLogValues);
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private boolean hostExists(List<GlusterServerInfo> glusterServers, VDS server) {
        for (GlusterServerInfo glusterServer : glusterServers) {
            if (glusterServer.getHostnameOrIp().equals(server.gethost_name())) {
                return true;
            }
            for (VdsNetworkInterface vdsNwInterface : getVdsInterfaces(server.getId())) {
                if (glusterServer.getHostnameOrIp().equals(vdsNwInterface.getAddress())) {
                    return true;
                }
            }
        }
        return false;
    }

    public InterfaceDao getInterfaceDAO() {
        return getDbFacade().getInterfaceDao();
    }

    private List<VdsNetworkInterface> getVdsInterfaces(Guid vdsId) {
        List<VdsNetworkInterface> interfaces = getInterfaceDAO().getAllInterfacesForVds(vdsId);
        return (interfaces == null) ? new ArrayList<VdsNetworkInterface>() : interfaces;
    }

    @SuppressWarnings("unchecked")
    private List<GlusterServerInfo> getGlusterPeers(Guid upServerId) {
        List<GlusterServerInfo> glusterServers = new ArrayList<GlusterServerInfo>();
        VDSReturnValue returnValue = runVdsCommand(VDSCommandType.GlusterServersList,
                        new VdsIdVDSCommandParametersBase(upServerId));
        if (!returnValue.getSucceeded()) {
            getReturnValue().getFault().setError(returnValue.getVdsError().getCode());
            getReturnValue().getFault().setMessage(returnValue.getVdsError().getMessage());
            AuditLogDirector.log(new AuditLogableBase(upServerId), AuditLogType.GLUSTER_SERVERS_LIST_FAILED);
            _glusterPeerListSucceeded = false;
        } else {
            glusterServers = (List<GlusterServerInfo>) returnValue.getReturnValue();
        }
        return glusterServers;
    }

    private boolean glusterPeerProbe(Guid upServerId, String newServerName) {
        try {
            VDSReturnValue returnValue = runVdsCommand(VDSCommandType.AddGlusterServer,
                    new AddGlusterServerVDSParameters(upServerId, newServerName));
            if (!returnValue.getSucceeded()) {
                getReturnValue().getFault().setError(returnValue.getVdsError().getCode());
                getReturnValue().getFault().setMessage(returnValue.getVdsError().getMessage());
                AuditLogDirector.log(new AuditLogableBase(getVdsId()), AuditLogType.GLUSTER_SERVER_ADD_FAILED);
                _glusterPeerProbeSucceeded = false;
            }
            return returnValue.getSucceeded();
        } catch (Exception e) {
            log.errorFormat("Could not peer probe the gluster server {0}. Error: {1}",
                    getVds().gethost_name(),
                    e.getMessage());
            _glusterPeerProbeSucceeded = false;
            return false;
        }
    }

}
