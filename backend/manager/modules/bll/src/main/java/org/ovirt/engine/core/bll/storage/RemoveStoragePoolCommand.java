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
import org.ovirt.engine.core.bll.network.ExternalNetworkManager;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.DetachStorageDomainFromPoolParameters;
import org.ovirt.engine.core.common.action.RemoveStorageDomainParameters;
import org.ovirt.engine.core.common.action.StoragePoolParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.VmNic;
import org.ovirt.engine.core.common.businessentities.network.VnicProfile;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.FormatStorageDomainVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.IrsBaseVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.ISingleAsyncOperation;
import org.ovirt.engine.core.utils.SyncronizeNumberOfAsyncOperations;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.linq.Predicate;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;
import org.ovirt.engine.core.vdsbroker.irsbroker.SpmStopOnIrsVDSCommandParameters;

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
        List<String> macsToRemove = getVmNicDao().getAllMacsByDataCenter(getStoragePool().getId());
        removeNetworks();
        /**
         * Detach master storage domain last.
         */
        List<StorageDomain> storageDomains = getStorageDomainDAO().getAllForStoragePool(getStoragePool().getId());
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

        getMacPool().freeMacs(macsToRemove);
        removeDataCenter();

        setSucceeded(true);
    }

    private void removeDataCenter() {
        TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {

            @Override
            public Void runInTransaction() {
                getCompensationContext().snapshotEntity(getStoragePool());
                getStoragePoolDAO().remove(getStoragePool().getId());
                getCompensationContext().stateChanged();

                return null;
            }
        });
    }

    private void removeNetworks() {
        final List<Network> networks = getNetworkDAO().getAllForDataCenter(getStoragePoolId());
        for (Network network : networks) {
            if (network.isExternal()) {
                for (VmNic nic : getVmNicDao().getAllForNetwork(network.getId())) {
                    new ExternalNetworkManager(nic, network).deallocateIfExternal();
                }
            }
        }

        TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {

            @Override
            public Void runInTransaction() {
                for (final Network net : networks) {
                    List<VnicProfile> profiles = getDbFacade().getVnicProfileDao().getAllForNetwork(net.getId());
                    for (VnicProfile vnicProfile : profiles) {
                        getCompensationContext().snapshotEntity(vnicProfile);
                        getDbFacade().getVnicProfileDao().remove(vnicProfile.getId());
                    }
                    getCompensationContext().snapshotEntity(net);
                    getNetworkDAO().remove(net.getId());
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
                getDbFacade().getStoragePoolIsoMapDao().update(masterDomain.getStoragePoolIsoMapData());
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
                detachStorageDomainWithEntities(masterDomain);
                getCompensationContext().snapshotEntity(masterDomain.getStorageStaticData());
                masterDomain.setStorageDomainType(StorageDomainType.Data);
                getDbFacade().getStorageDomainStaticDao().update(masterDomain.getStorageStaticData());
                getCompensationContext().stateChanged();
                return null;
            }
        });

        handleDestroyStoragePoolCommand();

        setSucceeded(true);

        if (!getStoragePool().isLocal()) {
            for (VDS vds : vdss) {
                StorageHelperDirector.getInstance().getItem(masterDomain.getStorageType())
                        .disconnectStorageFromDomainByVdsId(masterDomain, vds.getId());
            }
        } else {
            try {
                runVdsCommand(VDSCommandType.FormatStorageDomain,
                                new FormatStorageDomainVDSCommandParameters(vdss.get(0).getId(),
                                        masterDomain.getId()));
            } catch (VdcBLLException e) {
                // Do nothing, exception already printed at logs
            }
            StorageHelperDirector.getInstance().getItem(masterDomain.getStorageType())
                    .disconnectStorageFromDomainByVdsId(masterDomain, vdss.get(0).getId());
            removeDomainFromDb(masterDomain);
        }

        runSynchronizeOperation(new DisconnectStoragePoolAsyncOperationFactory());
        return retVal;
    }

    private void handleDestroyStoragePoolCommand() {
        try {
            runVdsCommand(VDSCommandType.DestroyStoragePool,
                            new IrsBaseVDSCommandParameters(getStoragePool().getId()));
        } catch (VdcBLLException e) {
            try {
                TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {
                    @Override
                    public Void runInTransaction() {
                        runVdsCommand(VDSCommandType.SpmStopOnIrs,
                                        new SpmStopOnIrsVDSCommandParameters(getStoragePool().getId()));
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
                getStorageDomainDAO().remove(domain.getId());
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
            if (!runInternalAction(VdcActionType.RemoveStorageDomain, tempVar, cloneContext().withoutLock().withoutExecutionContext())
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
                !checkStoragePoolStatusNotEqual(StoragePoolStatus.Up,
                        VdcBllMessages.ERROR_CANNOT_REMOVE_ACTIVE_STORAGE_POOL)) {
            return false;
        }

        if (getStoragePool().getStatus() != StoragePoolStatus.Uninitialized && !getParameters().getForceDelete()
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
            if (!poolDomains.isEmpty() && !canDetachStorageDomainWithVmsAndDisks(poolDomains.get(0))) {
                return false;
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
                return (dom.getStatus() == StorageDomainStatus.Active || dom.getStatus().isStorageDomainInProcess());
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
        sync.execute();
    }

    @Override
    protected Map<String, Pair<String, String>> getSharedLocks() {
        return sharedLocks;
    }
}
