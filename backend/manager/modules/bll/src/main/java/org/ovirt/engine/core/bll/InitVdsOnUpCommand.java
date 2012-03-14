package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.bll.storage.StorageHandlingCommandBase;
import org.ovirt.engine.core.bll.storage.StoragePoolStatusHandler;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.SetNonOperationalVdsParameters;
import org.ovirt.engine.core.common.action.StoragePoolParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.FenceActionType;
import org.ovirt.engine.core.common.businessentities.FenceStatusReturnValue;
import org.ovirt.engine.core.common.businessentities.NonOperationalReason;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VdsSpmStatus;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.common.vdscommands.ConnectStoragePoolVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.TransactionScopeOption;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AlertDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;

/**
 * Initialize Vds on its loading. For storages: First connect all storage
 * servers to VDS. Second connect Vds to storage Pool.
 *
 * After server initialized - its will be moved to Up status.
 */
public class InitVdsOnUpCommand<T extends StoragePoolParametersBase> extends StorageHandlingCommandBase<T> {
    private boolean _fencingSucceeded = true;
    private boolean _vdsProxyFound;
    private boolean _connectStorageSucceeded, _connectPoolSucceeded;
    private FenceStatusReturnValue _fenceStatusReturnValue;

    public InitVdsOnUpCommand(T parameters) {
        super(parameters);
        setVdsId(parameters.getVdsId());
    }

    @Override
    protected void executeCommand() {
        VDSGroup vdsGroup = getVdsGroup();

        if (vdsGroup.supportsVirtService()) {
            initVirtResources();
        }
        setSucceeded(true);
    }

    private void initVirtResources() {
        if (InitializeStorage()) {
            processFencing();
            processStoragePoolStatus();
        } else {
            setNonOperational(NonOperationalReason.STORAGE_DOMAIN_UNREACHABLE);
        }
    }

    private void processFencing() {
        FencingExecutor executor = new FencingExecutor(getVds(), FenceActionType.Status);
        // check first if we have any VDS to act as the proxy for fencing
        // actions.
        if (getVds().getpm_enabled() && executor.FindVdsToFence()) {
            VDSReturnValue returnValue = executor.Fence();
            _fencingSucceeded = returnValue.getSucceeded();
            _fenceStatusReturnValue = (FenceStatusReturnValue) returnValue.getReturnValue();
            _vdsProxyFound = true;
        }
    }

    private void processStoragePoolStatus() {
        if (getVds().getspm_status() != VdsSpmStatus.None) {
            storage_pool pool = DbFacade.getInstance().getStoragePoolDAO().get(getVds().getstorage_pool_id());
            if (pool != null && pool.getstatus() == StoragePoolStatus.NotOperational) {
                pool.setstatus(StoragePoolStatus.Problematic);
                DbFacade.getInstance().getStoragePoolDAO().updateStatus(pool.getId(), pool.getstatus());
                StoragePoolStatusHandler.PoolStatusChanged(pool.getId(), pool.getstatus());
            }
        }
    }

    private void setNonOperational(NonOperationalReason reason) {
        SetNonOperationalVdsParameters tempVar = new SetNonOperationalVdsParameters(getVds().getId(), reason);
        tempVar.setSaveToDb(true);
        Backend.getInstance().runInternalAction(VdcActionType.SetNonOperationalVds, tempVar);
    }

    private boolean InitializeStorage() {
        boolean returnValue = false;
        setStoragePoolId(getVds().getstorage_pool_id());

        // if no pool or pool is uninitialized or in maintenance mode no need to
        // connect any storage
        if (getStoragePool() == null || StoragePoolStatus.Uninitialized == getStoragePool().getstatus()
                || StoragePoolStatus.Maintanance == getStoragePool().getstatus()) {
            returnValue = true;
            _connectStorageSucceeded = true;
            _connectPoolSucceeded = true;
        } else {
            // boolean suppressCheck = false; //LINQ
            // AllRunningVdssInPool.Count() == 0;
            boolean suppressCheck = getAllRunningVdssInPool().size() == 0;
            StoragePoolParametersBase tempStorageBaseParams =
                    new StoragePoolParametersBase(getVds().getstorage_pool_id());
            tempStorageBaseParams.setVdsId(getVds().getId());
            tempStorageBaseParams.setSuppressCheck(suppressCheck);
            tempStorageBaseParams.setTransactionScopeOption(TransactionScopeOption.Suppress);
            if (Backend.getInstance()
                    .runInternalAction(VdcActionType.ConnectHostToStoragePoolServers, tempStorageBaseParams)
                    .getSucceeded()
                    || suppressCheck) {
                _connectStorageSucceeded = true;
                try {
                    setStoragePool(null);
                    returnValue = _connectPoolSucceeded = Backend
                            .getInstance()
                            .getResourceManager()
                            .RunVdsCommand(
                                    VDSCommandType.ConnectStoragePool,
                                    new ConnectStoragePoolVDSCommandParameters(getVds().getId(), getVds()
                                            .getstorage_pool_id(), getVds().getvds_spm_id(), getMasterDomainIdFromDb(),
                                            getStoragePool().getmaster_domain_version())).getSucceeded();
                } catch (RuntimeException exp) {
                    log.errorFormat("Could not connect host {0} to pool {1}", getVds().getvds_name(), getStoragePool()
                            .getname());
                    returnValue = false;
                }
                // if couldn't connect check if this is the only vds
                // return true if connect succeeded or it's the only vds
                if (!returnValue && suppressCheck) {
                    AuditLogDirector.log(new AuditLogableBase(getVdsId()),
                            AuditLogType.VDS_STORAGE_CONNECTION_FAILED_BUT_LAST_VDS);
                    returnValue = true;
                }
            }
        }
        return returnValue;
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        AuditLogType type = AuditLogType.UNASSIGNED;
        if (!_connectStorageSucceeded) {
            type = AuditLogType.CONNECT_STORAGE_SERVERS_FAILED;
        } else if (!_connectPoolSucceeded) {
            type = AuditLogType.CONNECT_STORAGE_POOL_FAILED;
        } else if (getVds().getpm_enabled() && _fencingSucceeded) {
            type = AuditLogType.VDS_FENCE_STATUS;
        } else if (getVds().getpm_enabled() && !_fencingSucceeded) {
            type = AuditLogType.VDS_FENCE_STATUS_FAILED;
        }

        // PM alerts
        AuditLogableBase logable = new AuditLogableBase(getVds().getId());
        if (getVds().getpm_enabled()) {
            if (!_vdsProxyFound) {
                logable.AddCustomValue("Reason",
                        AuditLogDirector.GetMessage(AuditLogType.VDS_ALERT_FENCING_NO_PROXY_HOST));
                AlertDirector.Alert(logable, AuditLogType.VDS_ALERT_FENCING_TEST_FAILED);
            } else if (!_fenceStatusReturnValue.getIsSucceeded()) {
                logable.AddCustomValue("Reason", _fenceStatusReturnValue.getMessage());
                AlertDirector.Alert(logable, AuditLogType.VDS_ALERT_FENCING_TEST_FAILED);
            }
        } else {
            AlertDirector.Alert(logable, AuditLogType.VDS_ALERT_FENCING_IS_NOT_CONFIGURED);
        }
        return type;
    }

    private static Log log = LogFactory.getLog(InitVdsOnUpCommand.class);
}
