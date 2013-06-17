package org.ovirt.engine.core.bll.storage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.Backend;
import org.ovirt.engine.core.bll.LockMessagesMatchUtil;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.network.MacPoolManager;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.DetachStorageDomainFromPoolParameters;
import org.ovirt.engine.core.common.action.RemoveStorageDomainParameters;
import org.ovirt.engine.core.common.action.StoragePoolParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.FormatStorageDomainVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.IrsBaseVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.ISingleAsyncOperation;
import org.ovirt.engine.core.utils.SyncronizeNumberOfAsyncOperations;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.linq.Predicate;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@NonTransactiveCommandAttribute(forceCompensation = true)
public class RemoveStoragePoolCommand<T extends StoragePoolParametersBase> extends StorageHandlingCommandBase<T> {

    private Map<String, Pair<String, String>> sharedLocks;

    public RemoveStoragePoolCommand(T parameters) {
        super(parameters);
    }

    protected RemoveStoragePoolCommand(Guid commandId) {
        super(commandId);
    }

    @Override
    protected void executeCommand() {
        List<String> macsToRemove = getVmNetworkInterfaceDao().getAllMacsByDataCenter(getStoragePool().getId());
        removeNetworks();
        /**
         * Detach master storage domain last.
         */
        List<StorageDomain> storageDomains = DbFacade.getInstance().getStorageDomainDao().getAllForStoragePool(
                    getStoragePool().getId());
        Collections.sort(storageDomains, new Comparator<StorageDomain>() {
            @Override
            public int compare(StorageDomain o1, StorageDomain o2) {
                return o1.getStorageDomainType().compareTo(o2.getStorageDomainType());
            }
        });
        if (storageDomains.size() > 0) {
            if (!getParameters().getForceDelete() && getAllRunningVdssInPool().size() > 0) {
                if(!regularRemoveStorageDomains(storageDomains)) {
                    setSucceeded(false);
                    return;
                }
            } else if (getParameters().getForceDelete()) {
                forceRemoveStorageDomains(storageDomains);
            } else {
                return;
            }
        }

        getStoragePoolDAO().remove(getStoragePool().getId());
        MacPoolManager.getInstance().freeMacs(macsToRemove);
        setSucceeded(true);
    }

    private void removeNetworks() {
        TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {

            @Override
            public Void runInTransaction() {
                final List<Network> networks = DbFacade.getInstance().getNetworkDao()
                        .getAllForDataCenter(getStoragePoolId().getValue());
                for (final Network net : networks) {
                    getCompensationContext().snapshotEntity(net);
                    DbFacade.getInstance().getNetworkDao().remove(net.getId());
                }
                getCompensationContext().stateChanged();
                return null;
            }
        });
    }

    private void forceRemoveStorageDomains(List<StorageDomain> storageDomains) {
        StorageDomain masterDomain = null;
        for (StorageDomain storageDomain : storageDomains) {
            if (storageDomain.getStorageDomainType() != StorageDomainType.Master) {
                if (storageDomain.getStorageDomainType() != StorageDomainType.ISO) {
                    removeDomainFromDb(storageDomain);
                }
            } else {
                masterDomain = storageDomain;
            }
        }
        if (masterDomain != null) {
            removeDomainFromDb(masterDomain);
        }
    }

    private boolean regularRemoveStorageDomains(List<StorageDomain> storageDomains) {
        boolean retVal = true;
        List<StorageDomain> temp = LinqUtils.filter(storageDomains, new Predicate<StorageDomain>() {
            @Override
            public boolean eval(StorageDomain storage_domain) {
                return storage_domain.getStorageDomainType() == StorageDomainType.Master;
            }
        });
        final StorageDomain masterDomain = LinqUtils.first(temp);

        TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {

            @Override
            public Void runInTransaction() {
                getCompensationContext().snapshotEntity(masterDomain.getStoragePoolIsoMapData());
                masterDomain.setStatus(StorageDomainStatus.Locked);
                DbFacade.getInstance()
                        .getStoragePoolIsoMapDao()
                        .update(masterDomain.getStoragePoolIsoMapData());
                getCompensationContext().stateChanged();
                return null;
            }
        });
        // destroying a pool is an SPM action. We need to connect all hosts
        // to the pool. Later on, during spm election, one of the hosts will
        // lock the pool
        // and the spm status will be FREE. Only then we can invoke the
        // destroy verb.
        connectAllHostToPoolAndDomain(masterDomain);

        List<VDS> vdss = getAllRunningVdssInPool();
        for (StorageDomain storageDomain : storageDomains) {
            if (storageDomain.getStorageDomainType() != StorageDomainType.Master) {
                if (!removeDomainFromPool(storageDomain, vdss.get(0))) {
                    log.errorFormat("Unable to detach storage domain {0} {1}",
                            storageDomain.getStorageName(),
                            storageDomain.getId());
                    retVal = false;
                }
            }
        }

        TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {

            @Override
            public Void runInTransaction() {
                getCompensationContext().snapshotEntity(masterDomain.getStorageStaticData());
                masterDomain.setStorageDomainType(StorageDomainType.Data);
                DbFacade.getInstance().getStorageDomainStaticDao().update(masterDomain.getStorageStaticData());
                getCompensationContext().stateChanged();
                return null;
            }
        });

        TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {
            @Override
            public Void runInTransaction() {
                getCompensationContext().snapshotEntity(getStoragePool());
                handleDestroyStoragePoolCommand();
                getCompensationContext().stateChanged();
                return null;
            }
        });

        setSucceeded(true);
        if (getStoragePool().getstorage_pool_type() != StorageType.LOCALFS) {
            for (VDS vds : vdss) {
                StorageHelperDirector.getInstance().getItem(getStoragePool().getstorage_pool_type())
                        .disconnectStorageFromDomainByVdsId(masterDomain, vds.getId());
            }
        } else {
            try {
                Backend
                        .getInstance()
                        .getResourceManager()
                        .RunVdsCommand(VDSCommandType.FormatStorageDomain,
                                new FormatStorageDomainVDSCommandParameters(vdss.get(0).getId(),
                                        masterDomain.getId()));
            } catch (VdcBLLException e) {
                // Do nothing, exception already printed at logs
            }
            StorageHelperDirector.getInstance().getItem(getStoragePool().getstorage_pool_type())
                    .disconnectStorageFromDomainByVdsId(masterDomain, vdss.get(0).getId());
            removeDomainFromDb(masterDomain);
        }

        runSynchronizeOperation(new DisconnectStoragePoolAsyncOperationFactory());
        return retVal;
    }

    private void handleDestroyStoragePoolCommand() {
        try {
            Backend
                    .getInstance()
                    .getResourceManager()
                    .RunVdsCommand(VDSCommandType.DestroyStoragePool,
                            new IrsBaseVDSCommandParameters(getStoragePool().getId()));
        } catch (VdcBLLException e) {
            try {
                TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {
                    @Override
                    public Void runInTransaction() {
                        Backend.getInstance()
                                .getResourceManager()
                                .RunVdsCommand(VDSCommandType.SpmStopOnIrs,
                                        new IrsBaseVDSCommandParameters(getStoragePool().getId()));
                        return null;
                    }
                });
            } catch (Exception e1) {
                log.errorFormat("Failed destroy storage pool with id {0} and after that failed to stop spm because of {1}",
                        getStoragePoolId(),
                        e1);
            }
            throw e;
        }
    }

    private void removeDomainFromDb(final StorageDomain domain) {
        TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {
            @Override
            public Void runInTransaction() {
                // Not compensation for remove domain as we don't want
                // to rollback a deleted domain - it will only cause more
                // problems if a domain got deleted in VDSM and not in backend
                // as it will be impossible to remove it.
                StorageHelperDirector.getInstance().getItem(domain.getStorageType())
                        .storageDomainRemoved(domain.getStorageStaticData());
                DbFacade.getInstance().getStorageDomainDao().remove(domain.getId());
                return null;
            }
        });

    }

    protected boolean removeDomainFromPool(StorageDomain storageDomain, VDS vds) {
        if (storageDomain.getStorageType() != StorageType.LOCALFS
                || storageDomain.getStorageDomainType() == StorageDomainType.ISO) {
            DetachStorageDomainFromPoolParameters tempVar = new DetachStorageDomainFromPoolParameters(
                    storageDomain.getId(), getStoragePool().getId());
            tempVar.setRemoveLast(true);
            tempVar.setDestroyingPool(true);
            // Compensation context is not passed, as we do not want to compensate in case of failure
            // in detach of one of storage domains
            if (!Backend.getInstance()
                    .runInternalAction(VdcActionType.DetachStorageDomainFromPool, tempVar)
                    .getSucceeded()) {
                return false;
            }
        } else {
            RemoveStorageDomainParameters tempVar = new RemoveStorageDomainParameters(storageDomain.getId());
            tempVar.setDestroyingPool(true);
            tempVar.setDoFormat(true);
            tempVar.setVdsId(vds.getId());
            if (!Backend.getInstance()
                    .runInternalAction(VdcActionType.RemoveStorageDomain,
                            tempVar,
                            new CommandContext(getCompensationContext()))
                    .getSucceeded()) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected boolean canDoAction() {
        if (!super.canDoAction() ||
                !checkStoragePool() ||
                !CheckStoragePoolStatusNotEqual(StoragePoolStatus.Up,
                        VdcBllMessages.ERROR_CANNOT_REMOVE_ACTIVE_STORAGE_POOL)) {
            return false;
        }

        if (getStoragePool().getstatus() != StoragePoolStatus.Uninitialized && !getParameters().getForceDelete()
                && !initializeVds()) {
            return false;
        }

        final List<StorageDomain> poolDomains =
                getStorageDomainDAO().getAllForStoragePool(getStoragePool().getId());
        final List<StorageDomain> activeOrLockedDomains = getActiveOrLockedDomainList(poolDomains);

        if (!activeOrLockedDomains.isEmpty()) {
            return failCanDoAction(VdcBllMessages.ERROR_CANNOT_REMOVE_POOL_WITH_ACTIVE_DOMAINS);
        }
        if (!getParameters().getForceDelete()) {
            if(poolDomains.size() > 1) {
                return failCanDoAction(VdcBllMessages.ERROR_CANNOT_REMOVE_STORAGE_POOL_WITH_NONMASTER_DOMAINS);
            }
            for (StorageDomain domain : poolDomains) {
                // check that there are no images on data domains
                if ((domain.getStorageDomainType() == StorageDomainType.Data || domain.getStorageDomainType() == StorageDomainType.Master)
                        && DbFacade.getInstance()
                                .getDiskImageDao()
                                .getAllSnapshotsForStorageDomain(domain.getId())
                                .size() != 0) {
                    return failCanDoAction(VdcBllMessages.ERROR_CANNOT_REMOVE_STORAGE_POOL_WITH_IMAGES);
                }
            }
            final List<VmStatic> vms = getVmStaticDAO().getAllByStoragePoolId(getStoragePool().getId());
            if (!vms.isEmpty()) {
                return failCanDoAction(VdcBllMessages.ERROR_CANNOT_REMOVE_STORAGE_POOL_WITH_VMS);
            }
        } else {
            List<VDS> poolHosts = getVdsDAO().getAllForStoragePool(getParameters().getStoragePoolId());

            sharedLocks = new HashMap<String, Pair<String, String>>();
            for (VDS host : poolHosts) {
                sharedLocks.put(host.getId().toString(),
                        LockMessagesMatchUtil.makeLockingPair(LockingGroup.VDS, VdcBllMessages.ACTION_TYPE_FAILED_OBJECT_LOCKED));
            }

            if (!poolHosts.isEmpty() && acquireLockInternal()) {
                for (VDS host : poolHosts) {
                    if (host.getStatus() != VDSStatus.Maintenance) {
                        return failCanDoAction(VdcBllMessages.ERROR_CANNOT_FORCE_REMOVE_STORAGE_POOL_WITH_VDS_NOT_IN_MAINTENANCE);
                    }
                }
            }
        }

        return true;
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__STORAGE__POOL);
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__REMOVE);
    }

    protected List<StorageDomain> getActiveOrLockedDomainList(List<StorageDomain> domainsList) {
        domainsList = LinqUtils.filter(domainsList, new Predicate<StorageDomain>() {
            @Override
            public boolean eval(StorageDomain dom) {
                return (dom.getStatus() == StorageDomainStatus.Active || dom.getStatus() == StorageDomainStatus.Locked);
            }
        });
        return domainsList;
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        if (getParameters().getForceDelete()){
            return getSucceeded() ? AuditLogType.USER_FORCE_REMOVE_STORAGE_POOL : AuditLogType.USER_FORCE_REMOVE_STORAGE_POOL_FAILED;
        }
        return getSucceeded() ? AuditLogType.USER_REMOVE_STORAGE_POOL : AuditLogType.USER_REMOVE_STORAGE_POOL_FAILED;
    }

    /**
     * @param masterDomain
     *            Connect all hosts to the pool and to the domains
     */
    protected void connectAllHostToPoolAndDomain(final StorageDomain masterDomain) {
        final List<VDS> vdsList = getAllRunningVdssInPool();
        final StoragePool storagePool = getStoragePool();
        SyncronizeNumberOfAsyncOperations sync = new SyncronizeNumberOfAsyncOperations(vdsList.size(),
                null, new ActivateDeactivateSingleAsyncOperationFactory() {

                    @Override
                    public ISingleAsyncOperation createSingleAsyncOperation() {

                        return new ConntectVDSToPoolAndDomains((ArrayList<VDS>) vdsList, masterDomain, storagePool);
                    }

                    @Override
                    public void initialize(ArrayList parameters) {
                        // no need to initilalize params
                    }
                });
        sync.Execute();
    }

    @Override
    protected Map<String, Pair<String, String>> getSharedLocks() {
        return sharedLocks;
    }
}
