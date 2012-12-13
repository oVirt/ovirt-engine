package org.ovirt.engine.core.bll.storage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.Backend;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
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
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.vdscommands.FormatStorageDomainVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.IrsBaseVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.ISingleAsyncOperation;
import org.ovirt.engine.core.utils.SyncronizeNumberOfAsyncOperations;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.linq.Predicate;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@NonTransactiveCommandAttribute(forceCompensation = true)
public class RemoveStoragePoolCommand<T extends StoragePoolParametersBase> extends StorageHandlingCommandBase<T> {

    private Map<String, String> sharedLocks;

    public RemoveStoragePoolCommand(T parameters) {
        super(parameters);
    }

    protected RemoveStoragePoolCommand(Guid commandId) {
        super(commandId);
    }

    @Override
    protected void executeCommand() {
        removeNetworks();
        /**
         * Detach master storage domain last.
         */
        List<storage_domains> storageDomains = DbFacade.getInstance().getStorageDomainDao().getAllForStoragePool(
                    getStoragePool().getId());
        Collections.sort(storageDomains, new Comparator<storage_domains>() {
            @Override
            public int compare(storage_domains o1, storage_domains o2) {
                return o1.getstorage_domain_type().compareTo(o2.getstorage_domain_type());
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

    private void forceRemoveStorageDomains(List<storage_domains> storageDomains) {
        storage_domains masterDomain = null;
        for (storage_domains storageDomain : storageDomains) {
            if (storageDomain.getstorage_domain_type() != StorageDomainType.Master) {
                if (storageDomain.getstorage_domain_type() != StorageDomainType.ISO) {
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

    private boolean regularRemoveStorageDomains(List<storage_domains> storageDomains) {
        boolean retVal = true;
        List<storage_domains> temp = LinqUtils.filter(storageDomains, new Predicate<storage_domains>() {
            @Override
            public boolean eval(storage_domains storage_domain) {
                return storage_domain.getstorage_domain_type() == StorageDomainType.Master;
            }
        });
        final storage_domains masterDomain = LinqUtils.first(temp);

        TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {

            @Override
            public Void runInTransaction() {
                getCompensationContext().snapshotEntity(masterDomain.getStoragePoolIsoMapData());
                masterDomain.setstatus(StorageDomainStatus.Locked);
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
        for (storage_domains storageDomain : storageDomains) {
            if (storageDomain.getstorage_domain_type() != StorageDomainType.Master) {
                if (!removeDomainFromPool(storageDomain, vdss.get(0))) {
                    log.errorFormat("Unable to detach storage domain {0} {1}",
                            storageDomain.getstorage_name(),
                            storageDomain.getId());
                    retVal = false;
                }
            }
        }

        TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {

            @Override
            public Void runInTransaction() {
                getCompensationContext().snapshotEntity(masterDomain.getStorageStaticData());
                masterDomain.setstorage_domain_type(StorageDomainType.Data);
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

    private void removeDomainFromDb(final storage_domains domain) {
        TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {
            @Override
            public Void runInTransaction() {
                // Not compensation for remove domain as we don't want
                // to rollback a deleted domain - it will only cause more
                // problems if a domain got deleted in VDSM and not in backend
                // as it will be impossible to remove it.
                StorageHelperDirector.getInstance().getItem(domain.getstorage_type())
                        .storageDomainRemoved(domain.getStorageStaticData());
                DbFacade.getInstance().getStorageDomainDao().remove(domain.getId());
                return null;
            }
        });

    }

    protected boolean removeDomainFromPool(storage_domains storageDomain, VDS vds) {
        if (storageDomain.getstorage_type() != StorageType.LOCALFS) {
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
        boolean returnValue = super.canDoAction() && checkStoragePool()
                && CheckStoragePoolStatusNotEqual(StoragePoolStatus.Up,
                                                  VdcBllMessages.ERROR_CANNOT_REMOVE_ACTIVE_STORAGE_POOL);
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__STORAGE__POOL);
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__REMOVE);
        if (!returnValue) {
            return false;
        }
        if (getStoragePool().getstatus() != StoragePoolStatus.Uninitialized && !getParameters().getForceDelete()
                && !InitializeVds()) {
            return false;
        }
        final List<storage_domains> poolDomains =
                DbFacade.getInstance().getStorageDomainDao().getAllForStoragePool(
                        getStoragePool().getId());
        final List<storage_domains> activeOrLockedDomains = getActiveOrLockedDomainList(poolDomains);

        if (!activeOrLockedDomains.isEmpty()) {
            return failCanDoAction(VdcBllMessages.ERROR_CANNOT_REMOVE_POOL_WITH_ACTIVE_DOMAINS);
        }
        if (!getParameters().getForceDelete()) {
            if(poolDomains.size() > 1) {
                return failCanDoAction(VdcBllMessages.ERROR_CANNOT_REMOVE_STORAGE_POOL_WITH_NONMASTER_DOMAINS);
            }
            for (storage_domains domain : poolDomains) {
                // check that there are no images on data domains
                if ((domain.getstorage_domain_type() == StorageDomainType.Data || domain.getstorage_domain_type() == StorageDomainType.Master)
                        && DbFacade.getInstance()
                                .getDiskImageDao()
                                .getAllSnapshotsForStorageDomain(domain.getId())
                                .size() != 0) {
                    return failCanDoAction(VdcBllMessages.ERROR_CANNOT_REMOVE_STORAGE_POOL_WITH_IMAGES);
                }
            }
            final List<VmStatic> vms =
                    DbFacade.getInstance().getVmStaticDao().getAllByStoragePoolId(getStoragePool().getId());
            if (vms.size() > 0) {
                return failCanDoAction(VdcBllMessages.ERROR_CANNOT_REMOVE_STORAGE_POOL_WITH_VMS);
            }
        }
        else {
            List<VDS> poolHosts = getVdsDAO().getAllForStoragePool(getParameters().getStoragePoolId());

            sharedLocks = new HashMap<String, String>();
            for (VDS host : poolHosts) {
                sharedLocks.put(host.getId().toString(), LockingGroup.VDS.name());
            }

            if (poolHosts != null && !poolHosts.isEmpty() && acquireLockInternal()) {
                for (VDS host : poolHosts) {
                    if (host.getstatus() != VDSStatus.Maintenance) {
                        return failCanDoAction(VdcBllMessages.ERROR_CANNOT_FORCE_REMOVE_STORAGE_POOL_WITH_VDS_NOT_IN_MAINTENANCE);
                    }
                }
            }
        }

        return true;
    }

    protected List<storage_domains> getActiveOrLockedDomainList(List<storage_domains> domainsList) {
        domainsList = LinqUtils.filter(domainsList, new Predicate<storage_domains>() {
            @Override
            public boolean eval(storage_domains dom) {
                return (dom.getstatus() == StorageDomainStatus.Active || dom.getstatus() == StorageDomainStatus.Locked);
            }
        });
        return domainsList;
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_REMOVE_STORAGE_POOL : AuditLogType.USER_REMOVE_STORAGE_POOL_FAILED;
    }

    /**
     * @param masterDomain
     *            Connect all hosts to the pool and to the domains
     */
    protected void connectAllHostToPoolAndDomain(final storage_domains masterDomain) {
        final List<VDS> vdsList = getAllRunningVdssInPool();
        final storage_pool storagePool = getStoragePool();
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
    protected Map<String, String> getSharedLocks() {
        return sharedLocks;
    }
}
