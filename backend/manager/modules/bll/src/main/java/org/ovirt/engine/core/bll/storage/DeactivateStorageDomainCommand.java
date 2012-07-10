package org.ovirt.engine.core.bll.storage;

import java.util.List;

import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.StorageDomainPoolParametersBase;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMapId;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.businessentities.storage_pool_iso_map;
import org.ovirt.engine.core.common.vdscommands.DeactivateStorageDomainVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.DisconnectStoragePoolVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.IrsBaseVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.AsyncTaskDAO;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.linq.Predicate;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;

@NonTransactiveCommandAttribute(forceCompensation = true)
public class DeactivateStorageDomainCommand<T extends StorageDomainPoolParametersBase> extends
        StorageDomainCommandBase<T> {
    protected Guid _newMasterStorageDomainId = new Guid();
    private storage_domains _newMaster;

    protected storage_domains getNewMaster() {
        if (_newMaster == null && _newMasterStorageDomainId.equals(Guid.Empty)) {
            _newMaster = electNewMaster();
        } else if (_newMaster == null) {
            _newMaster = getStorageDomainDAO().get(_newMasterStorageDomainId);
        }
        return _newMaster;
    }

    protected void setNewMaster(storage_domains value) {
        _newMaster = value;
    }

    protected boolean _isLastMaster;
    private VDS spm;

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
    protected boolean canDoAction() {
        super.canDoAction();
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__DEACTIVATE);
        if (!(CheckStorageDomain() && checkStorageDomainStatus(StorageDomainStatus.Active))) {
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
                        .getAllRunningForStorageDomain(getStorageDomain().getId())
                        .isEmpty()) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_DETECTED_RUNNING_VMS);
            return false;
        }
        if (getStoragePool().getspm_vds_id() != null
                    && getStorageDomain().getstorage_domain_type() != StorageDomainType.ISO
                    && getAsyncTaskDao().getAsyncTaskIdsByEntity(getParameters().getStorageDomainId()).size() > 0) {
                addCanDoActionMessage(VdcBllMessages.ERROR_CANNOT_DEACTIVATE_DOMAIN_WITH_TASKS);
                return false;
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
        final storage_pool_iso_map map =
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
        boolean succeeded = getBackend().getResourceManager()
                .RunVdsCommand(
                        VDSCommandType.DeactivateStorageDomain,
                        new DeactivateStorageDomainVDSCommandParameters(getStoragePool().getId(),
                                getStorageDomain()
                                        .getId(),
                                _newMasterStorageDomainId,
                                getStoragePool().getmaster_domain_version()))
                .getSucceeded();
        if (succeeded) {
            runSynchronizeOperation(new AfterDeactivateSingleAsyncOperationFactory(),
                    _isLastMaster,
                    _newMasterStorageDomainId);
            if (_isLastMaster && spm != null) {
                final VDSReturnValue stopSpmReturnValue = getBackend()
                        .getResourceManager()
                        .RunVdsCommand(VDSCommandType.SpmStopOnIrs,
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
                getBackend().getResourceManager()
                        .RunVdsCommand(
                                VDSCommandType.DisconnectStoragePool,
                                new DisconnectStoragePoolVDSCommandParameters(spm.getId(),
                                        getStoragePool().getId(), spm.getvds_spm_id()));
            }

            getStorageHelper(getStorageDomain()).DisconnectStorageFromDomainByVdsId(getStorageDomain(), spm.getId());

            executeInNewTransaction(new TransactionMethod<Object>() {
                @Override
                public Object runInTransaction() {
                    getCompensationContext().snapshotEntityStatus(map, map.getstatus());
                    if (getParameters().isInactive()) {
                        map.setstatus(StorageDomainStatus.InActive);
                    } else {
                        map.setstatus(StorageDomainStatus.Maintenance);
                    }
                    getStoragePoolIsoMapDAO().updateStatus(map.getId(), map.getstatus());
                    if (!Guid.Empty.equals(_newMasterStorageDomainId)) {
                        storage_pool_iso_map mapOfNewMaster = getNewMaster().getStoragePoolIsoMapData();
                        getCompensationContext().snapshotEntityStatus(mapOfNewMaster, mapOfNewMaster.getstatus());
                        mapOfNewMaster.setstatus(StorageDomainStatus.Active);
                        getStoragePoolIsoMapDAO().updateStatus(mapOfNewMaster.getId(), mapOfNewMaster.getstatus());
                    }
                    getCompensationContext().stateChanged();
                    return null;
                }
            });
            setSucceeded(true);
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
            if (getNewMaster() != null) {
                // increase master domain version
                executeInNewTransaction(new TransactionMethod<Object>() {

                    @Override
                    public Object runInTransaction() {
                        storage_pool_iso_map newMasterMap = getNewMaster().getStoragePoolIsoMapData();
                        // We do not need to compensate storage pool, it will be committed if run during reconstruct
                        getStoragePool().setmaster_domain_version(getStoragePool().getmaster_domain_version() + 1);
                        getCompensationContext().snapshotEntity(getNewMaster().getStorageStaticData());
                        getNewMaster().setstorage_domain_type(StorageDomainType.Master);
                        _newMasterStorageDomainId = getNewMaster().getId();
                        if (!duringReconstruct) {
                            getCompensationContext().snapshotEntityStatus(newMasterMap, newMasterMap.getstatus());
                            getNewMaster().setstatus(StorageDomainStatus.Locked);
                            getStoragePoolIsoMapDAO().updateStatus(newMasterMap.getId(), newMasterMap.getstatus());
                        }
                        DbFacade.getInstance()
                                .getStorageDomainStaticDAO()
                                .update(getNewMaster().getStorageStaticData());
                        getCompensationContext().snapshotEntity(getStorageDomain().getStorageStaticData());
                        getStorageDomain().setstorage_domain_type(StorageDomainType.Data);
                        DbFacade.getInstance()
                                .getStorageDomainStaticDAO()
                                .update(getStorageDomain().getStorageStaticData());
                        getCompensationContext().stateChanged();
                        return null;
                    }
                });
            } else {
                _isLastMaster = true;
            }
            updateStoragePoolInDiffTransaction();
        }
    }

    AsyncTaskDAO getAsyncTaskDao() {
        return DbFacade.getInstance().getAsyncTaskDAO();
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getParameters().getIsInternal() ? getSucceeded() ? AuditLogType.SYSTEM_DEACTIVATED_STORAGE_DOMAIN
                : AuditLogType.SYSTEM_DEACTIVATE_STORAGE_DOMAIN_FAILED
                : getSucceeded() ? AuditLogType.USER_DEACTIVATED_STORAGE_DOMAIN
                        : AuditLogType.USER_DEACTIVATE_STORAGE_DOMAIN_FAILED;
    }
}
