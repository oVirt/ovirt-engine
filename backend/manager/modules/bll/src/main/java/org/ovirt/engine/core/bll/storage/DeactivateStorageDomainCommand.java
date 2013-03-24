package org.ovirt.engine.core.bll.storage;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.LockIdNameAttribute;
import org.ovirt.engine.core.bll.LockMessagesMatchUtil;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.StorageDomainPoolParametersBase;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMapId;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMap;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
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
    private StorageDomain _newMaster;
    protected boolean _isLastMaster;
    private VDS spm;

    protected StorageDomain getNewMaster(boolean duringReconstruct) {
        if (_newMaster == null && Guid.Empty.equals(_newMasterStorageDomainId)) {
            _newMaster = electNewMaster(duringReconstruct);
        } else if (_newMaster == null) {
            _newMaster = getStorageDomainDAO().get(_newMasterStorageDomainId);
        }
        return _newMaster;
    }

    protected void setNewMaster(StorageDomain value) {
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
        if (!(checkStorageDomain())) {
            return false;
        }

        // when the execution is internal, proceed also if the domain is in unknown status.
        if (!((getParameters().getIsInternal() && checkStorageDomainStatus(StorageDomainStatus.Active,
                StorageDomainStatus.Unknown)) || checkStorageDomainStatus(StorageDomainStatus.Active))) {
            return false;
        }

        if (!getParameters().getIsInternal()
                && getStorageDomain().getStorageDomainType() == StorageDomainType.Master) {
            List<StorageDomain> domains =
                    getStorageDomainDAO().getAllForStoragePool(getStorageDomain().getStoragePoolId().getValue());

            List<StorageDomain> activeDomains = filterDomainsByStatus(domains, StorageDomainStatus.Active);

            List<StorageDomain> dataDomains = LinqUtils.filter(activeDomains, new Predicate<StorageDomain>() {
                @Override
                public boolean eval(StorageDomain a) {
                    return a.getStorageDomainType() == StorageDomainType.Data;
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
            if (getStorageDomain().getStorageDomainType() == StorageDomainType.Master &&
            getAsyncTaskDao().getAsyncTaskIdsByStoragePoolId(getStorageDomain().getStoragePoolId().getValue()).size() > 0) {
                addCanDoActionMessage(VdcBllMessages.ERROR_CANNOT_DEACTIVATE_MASTER_DOMAIN_WITH_TASKS_ON_POOL);
                return false;
            } else if (getStorageDomain().getStorageDomainType() != StorageDomainType.ISO
                    && (getStorageDomain().getStorageDomainType() == StorageDomainType.ImportExport && !getParameters().getIsInternal())
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
    private List<StorageDomain> filterDomainsByStatus(List<StorageDomain> domains,
            final StorageDomainStatus domainStatus) {
        List<StorageDomain> activeDomains = LinqUtils.filter(domains, new Predicate<StorageDomain>() {
            @Override
            public boolean eval(StorageDomain a) {
                return a.getStatus() == domainStatus && !a.getId().equals(getStorageDomain().getId());
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
                    getStoragePool().setstatus(StoragePoolStatus.Maintenance);
                    getStoragePoolDAO().updateStatus(getStoragePool().getId(), getStoragePool().getstatus());
                    getCompensationContext().stateChanged();
                    return null;
                }
            });

            StoragePoolStatusHandler.PoolStatusChanged(getStoragePool().getId(), getStoragePool().getstatus());
            runSynchronizeOperation(new DisconnectStoragePoolAsyncOperationFactory());
            getStorageDomain().getStorageDynamicData().setAvailableDiskSize(null);
            getStorageDomain().getStorageDynamicData().setUsedDiskSize(null);
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
        if (getStorageDomain().getStorageDomainType() == StorageDomainType.Master) {
            final StorageDomain newMaster = getNewMaster(duringReconstruct);
            if (newMaster != null) {
                // increase master domain version
                executeInNewTransaction(new TransactionMethod<Object>() {

                    @Override
                    public Object runInTransaction() {
                        StoragePoolIsoMap newMasterMap = newMaster.getStoragePoolIsoMapData();
                        newMaster.getStorageStaticData().setLastTimeUsedAsMaster(System.currentTimeMillis());
                        getCompensationContext().snapshotEntity(newMaster.getStorageStaticData());
                        newMaster.setStorageDomainType(StorageDomainType.Master);
                        _newMasterStorageDomainId = newMaster.getId();
                        if (!duringReconstruct) {
                            getCompensationContext().snapshotEntityStatus(newMasterMap, newMasterMap.getstatus());
                            newMaster.setStatus(StorageDomainStatus.Locked);
                            getStoragePoolIsoMapDAO().updateStatus(newMasterMap.getId(), newMasterMap.getstatus());
                        }
                        DbFacade.getInstance()
                                .getStorageDomainStaticDao()
                                .update(newMaster.getStorageStaticData());
                        getCompensationContext().snapshotEntity(getStorageDomain().getStorageStaticData());
                        getStorageDomain().setStorageDomainType(StorageDomainType.Data);
                        DbFacade.getInstance()
                                .getStorageDomainStaticDao()
                                .update(getStorageDomain().getStorageStaticData());
                        getCompensationContext().stateChanged();
                        return null;
                    }
                });
                updateStoragePoolMasterDomainVersionInDiffTransaction();
            } else {
                _isLastMaster = true;
            }
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
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        StorageDomain storageDomain = getStorageDomain();
        if (storageDomain != null) {
            Map<String, Pair<String, String>> locks = new HashMap<String, Pair<String, String>>();
            locks.put(storageDomain.getId().toString(),
                    LockMessagesMatchUtil.makeLockingPair(LockingGroup.STORAGE, VdcBllMessages.ACTION_TYPE_FAILED_OBJECT_LOCKED));
            if (storageDomain.getStorageDomainType() == StorageDomainType.Master) {
                locks.put(storageDomain.getStoragePoolId().toString(),
                        LockMessagesMatchUtil.makeLockingPair(LockingGroup.POOL, VdcBllMessages.ACTION_TYPE_FAILED_OBJECT_LOCKED));
            }
            return locks;
        }
        return null;
    }

    @Override
    protected Map<String, Pair<String, String>> getSharedLocks() {
        StorageDomain storageDomain = getStorageDomain();
        if (storageDomain != null && storageDomain.getStorageDomainType() == StorageDomainType.Data
                && storageDomain.getStorageDomainType() != StorageDomainType.Master
                && storageDomain.getStoragePoolId() != null) {
            return Collections.singletonMap(storageDomain.getStoragePoolId().toString(),
                    LockMessagesMatchUtil.makeLockingPair(LockingGroup.POOL, VdcBllMessages.ACTION_TYPE_FAILED_OBJECT_LOCKED));
        }
        return null;
    }
}
