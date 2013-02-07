package org.ovirt.engine.core.bll.storage;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.LockIdNameAttribute;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.StorageDomainPoolParametersBase;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMapId;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMap;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.vdscommands.DeactivateStorageDomainVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.DisconnectStoragePoolVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.IrsBaseVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.dao.AsyncTaskDAO;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.linq.Predicate;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;

@LockIdNameAttribute
@NonTransactiveCommandAttribute(forceCompensation = true)
public class DeactivateStorageDomainCommand<T extends StorageDomainPoolParametersBase> extends
        StorageDomainCommandBase<T> {
    protected Guid _newMasterStorageDomainId = Guid.Empty;
    private storage_domains _newMaster;
    protected boolean _isLastMaster;
    private VDS spm;

    protected storage_domains getNewMaster(boolean duringReconstruct) {
        if (_newMaster == null && Guid.Empty.equals(_newMasterStorageDomainId)) {
            _newMaster = electNewMaster(duringReconstruct);
        } else if (_newMaster == null) {
            _newMaster = getStorageDomainDAO().get(_newMasterStorageDomainId);
        }
        return _newMaster;
    }

    protected void setNewMaster(storage_domains value) {
        _newMaster = value;
    }

    public DeactivateStorageDomainCommand(T parameters) {
        super(parameters);
    }

    /**
     * Constructor for command creation when compensation is applied on startup
     *
     * @param commandId
     */

    protected DeactivateStorageDomainCommand(Guid commandId) {
        super(commandId);
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__STORAGE__DOMAIN);
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__DEACTIVATE);
    }

    @Override
    protected boolean canDoAction() {
        if (!(checkStorageDomain() && checkStorageDomainStatus(StorageDomainStatus.Active))) {
            return false;
        }

        if (!getParameters().getIsInternal()
                && getStorageDomain().getstorage_domain_type() == StorageDomainType.Master) {
            List<storage_domains> domains =
                    getStorageDomainDAO().getAllForStoragePool(getStorageDomain().getstorage_pool_id().getValue());

            List<storage_domains> activeDomains = filterDomainsByStatus(domains, StorageDomainStatus.Active);

            List<storage_domains> dataDomains = LinqUtils.filter(activeDomains, new Predicate<storage_domains>() {
                @Override
                public boolean eval(storage_domains a) {
                    return a.getstorage_domain_type() == StorageDomainType.Data;
                }
            });

            if (!activeDomains.isEmpty() && dataDomains.isEmpty()) {
                addCanDoActionMessage(VdcBllMessages.ERROR_CANNOT_DEACTIVATE_MASTER_WITH_NON_DATA_DOMAINS);
                return false;
            }

            if (!filterDomainsByStatus(domains, StorageDomainStatus.Locked).isEmpty()) {
                addCanDoActionMessage(VdcBllMessages.ERROR_CANNOT_DEACTIVATE_MASTER_WITH_LOCKED_DOMAINS);
                return false;
            }
        }
        if (!getParameters().getIsInternal()
                && !getVmDAO()
                        .getAllActiveForStorageDomain(getStorageDomain().getId())
                        .isEmpty()) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_DETECTED_ACTIVE_VMS);
            return false;
        }
        if (getStoragePool().getspm_vds_id() != null) {
            // In case there are running tasks in the pool, it is impossible to deactivate the master storage domain
            if (getStorageDomain().getstorage_domain_type() == StorageDomainType.Master &&
            getAsyncTaskDao().getAsyncTaskIdsByStoragePoolId(getStorageDomain().getstorage_pool_id().getValue()).size() > 0) {
                addCanDoActionMessage(VdcBllMessages.ERROR_CANNOT_DEACTIVATE_MASTER_DOMAIN_WITH_TASKS_ON_POOL);
                return false;
            } else if (getStorageDomain().getstorage_domain_type() != StorageDomainType.ISO
                    && (getStorageDomain().getstorage_domain_type() == StorageDomainType.ImportExport && !getParameters().getIsInternal())
            && getAsyncTaskDao().getAsyncTaskIdsByEntity(getParameters().getStorageDomainId()).size() > 0) {
               addCanDoActionMessage(VdcBllMessages.ERROR_CANNOT_DEACTIVATE_DOMAIN_WITH_TASKS);
               return false;
            }
        }
        return true;
    }

    /**
     * Filter out the domains with the requested status from the given domains list, excluding the domain which the
     * command is run for.
     *
     * @param domains
     *            The domains to filter.
     * @param domainStatus
     *            The status to filter by.
     * @return Just the domains that match the given status, excluding the current domain of the command.
     */
    private List<storage_domains> filterDomainsByStatus(List<storage_domains> domains,
            final StorageDomainStatus domainStatus) {
        List<storage_domains> activeDomains = LinqUtils.filter(domains, new Predicate<storage_domains>() {
            @Override
            public boolean eval(storage_domains a) {
                return a.getstatus() == domainStatus && !a.getId().equals(getStorageDomain().getId());
            }
        });
        return activeDomains;
    }

    @Override
    protected void executeCommand() {
        spm = null;
        if (getStoragePool().getspm_vds_id() != null) {
            spm = getVdsDAO().get(getStoragePool().getspm_vds_id());
        }
        final StoragePoolIsoMap map =
                getStoragePoolIsoMapDAO().get
                        (new StoragePoolIsoMapId(getParameters().getStorageDomainId(),
                                getParameters().getStoragePoolId()));
        changeStorageDomainStatusInTransaction(map, StorageDomainStatus.Locked);
        ProceedStorageDomainTreatmentByDomainType(false);

        if (_isLastMaster) {
            executeInNewTransaction(new TransactionMethod<Object>() {
                @Override
                public Object runInTransaction() {
                    getCompensationContext().snapshotEntityStatus(getStoragePool(), getStoragePool().getstatus());
                    getStoragePool().setstatus(StoragePoolStatus.Maintanance);
                    getStoragePoolDAO().updateStatus(getStoragePool().getId(), getStoragePool().getstatus());
                    getCompensationContext().stateChanged();
                    return null;
                }
            });

            StoragePoolStatusHandler.PoolStatusChanged(getStoragePool().getId(), getStoragePool().getstatus());
            runSynchronizeOperation(new DisconnectStoragePoolAsyncOperationFactory());
            getStorageDomain().getStorageDynamicData().setavailable_disk_size(null);
            getStorageDomain().getStorageDynamicData().setused_disk_size(null);
        }
        if (!getParameters().isInactive()) {
            runVdsCommand(VDSCommandType.DeactivateStorageDomain,
                    new DeactivateStorageDomainVDSCommandParameters(getStoragePool().getId(),
                            getStorageDomain()
                                    .getId(),
                            _newMasterStorageDomainId,
                            getStoragePool().getmaster_domain_version()));
        }
        freeLock();
        if (!getParameters().isInactive()) {
            runSynchronizeOperation(new AfterDeactivateSingleAsyncOperationFactory(),
                    _isLastMaster,
                    _newMasterStorageDomainId);
        }
        if (_isLastMaster && spm != null) {
            final VDSReturnValue stopSpmReturnValue = runVdsCommand(VDSCommandType.SpmStopOnIrs,
                    new IrsBaseVDSCommandParameters(getStoragePool().getId()));
            if (!stopSpmReturnValue.getSucceeded()) {
                // no need to continue because DisconnectStoragePool will
                // fail if host is SPM
                log.error("Aborting execution due to failure stopping SPM." +
                        " Stop SPM failed due to "
                        + stopSpmReturnValue.getExceptionString());
                setSucceeded(false);
                return;
            }
            runVdsCommand(VDSCommandType.DisconnectStoragePool,
                    new DisconnectStoragePoolVDSCommandParameters(spm.getId(),
                            getStoragePool().getId(), spm.getVdsSpmId()));
        }

        if (!getParameters().isInactive() && spm != null) {
            getStorageHelper(getStorageDomain()).disconnectStorageFromDomainByVdsId(getStorageDomain(), spm.getId());
        }

        executeInNewTransaction(new TransactionMethod<Object>() {
            @Override
            public Object runInTransaction() {
                if (getParameters().isInactive()) {
                    map.setstatus(StorageDomainStatus.InActive);
                } else {
                    map.setstatus(StorageDomainStatus.Maintenance);
                }
                getStoragePoolIsoMapDAO().updateStatus(map.getId(), map.getstatus());
                if (!Guid.Empty.equals(_newMasterStorageDomainId)) {
                    StoragePoolIsoMap mapOfNewMaster = getNewMaster(false).getStoragePoolIsoMapData();
                    mapOfNewMaster.setstatus(StorageDomainStatus.Active);
                    getStoragePoolIsoMapDAO().updateStatus(mapOfNewMaster.getId(), mapOfNewMaster.getstatus());
                }
                return null;
            }
        });
        notifyAsyncTasks();
        setSucceeded(true);
    }

    /**
     * Send notification to user about tasks still running at the moment when the storage got deactivated.
     */
    private void notifyAsyncTasks() {
        final List<Guid> asyncTasks =
                getDbFacade().getAsyncTaskDao()
                .getAsyncTaskIdsByEntity(getParameters().getStorageDomainId());

        if (!asyncTasks.isEmpty()) {
            AuditLogableBase auditLogableBase = new AuditLogableBase();
            auditLogableBase.setStorageDomain(getStorageDomain());
            AuditLogDirector.log(auditLogableBase, AuditLogType.STORAGE_DOMAIN_TASKS_ERROR);
        }
    }

    /**
     * In case of master domain this method decide if to move master to other domain or move pool to maintenance (since
     * there is no master domain)
     *
     * @param duringReconstruct If true storagePool will be saved to DB outside of the transaction and master domain
     * will not be locked
     */
    protected void ProceedStorageDomainTreatmentByDomainType(final boolean duringReconstruct) {
        if (getStorageDomain().getstorage_domain_type() == StorageDomainType.Master) {
            final storage_domains newMaster = getNewMaster(duringReconstruct);
            if (newMaster != null) {
                // increase master domain version
                executeInNewTransaction(new TransactionMethod<Object>() {

                    @Override
                    public Object runInTransaction() {
                        StoragePoolIsoMap newMasterMap = newMaster.getStoragePoolIsoMapData();
                        newMaster.getStorageStaticData().setLastTimeUsedAsMaster(System.currentTimeMillis());
                        getCompensationContext().snapshotEntity(newMaster.getStorageStaticData());
                        newMaster.setstorage_domain_type(StorageDomainType.Master);
                        _newMasterStorageDomainId = newMaster.getId();
                        if (!duringReconstruct) {
                            getCompensationContext().snapshotEntityStatus(newMasterMap, newMasterMap.getstatus());
                            newMaster.setstatus(StorageDomainStatus.Locked);
                            getStoragePoolIsoMapDAO().updateStatus(newMasterMap.getId(), newMasterMap.getstatus());
                        }
                        DbFacade.getInstance()
                                .getStorageDomainStaticDao()
                                .update(newMaster.getStorageStaticData());
                        getCompensationContext().snapshotEntity(getStorageDomain().getStorageStaticData());
                        getStorageDomain().setstorage_domain_type(StorageDomainType.Data);
                        DbFacade.getInstance()
                                .getStorageDomainStaticDao()
                                .update(getStorageDomain().getStorageStaticData());
                        getCompensationContext().stateChanged();
                        return null;
                    }
                });
            } else {
                _isLastMaster = true;
            }
            updateStoragePoolMasterDomainVersionInDiffTransaction();
        }
    }

    protected AsyncTaskDAO getAsyncTaskDao() {
        return DbFacade.getInstance().getAsyncTaskDao();
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getParameters().getIsInternal() ? getSucceeded() ? AuditLogType.SYSTEM_DEACTIVATED_STORAGE_DOMAIN
                : AuditLogType.SYSTEM_DEACTIVATE_STORAGE_DOMAIN_FAILED
                : getSucceeded() ? AuditLogType.USER_DEACTIVATED_STORAGE_DOMAIN
                        : AuditLogType.USER_DEACTIVATE_STORAGE_DOMAIN_FAILED;
    }

    @Override
    protected Map<String, String> getExclusiveLocks() {
        storage_domains storageDomain = getStorageDomain();
        if (storageDomain != null) {
            Map<String, String> locks = new HashMap<String, String>();
            locks.put(storageDomain.getId().toString(), LockingGroup.STORAGE.name());
            if (storageDomain.getstorage_domain_type() == StorageDomainType.Master) {
                locks.put(storageDomain.getstorage_pool_id().toString(), LockingGroup.POOL.name());
            }
            return locks;
        }
        return null;
    }

    @Override
    protected Map<String, String> getSharedLocks() {
        storage_domains storageDomain = getStorageDomain();
        if (storageDomain != null && storageDomain.getstorage_domain_type() == StorageDomainType.Data
                && storageDomain.getstorage_domain_type() != StorageDomainType.Master
                && storageDomain.getstorage_pool_id() != null) {
            return Collections.singletonMap(storageDomain.getstorage_pool_id().toString(), LockingGroup.POOL.name());
        }
        return null;
    }
}
