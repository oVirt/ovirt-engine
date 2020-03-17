package org.ovirt.engine.core.bll.storage.pool;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.LockMessagesMatchUtil;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.network.ExternalNetworkManagerFactory;
import org.ovirt.engine.core.bll.storage.StorageHandlingCommandBase;
import org.ovirt.engine.core.bll.validator.storage.StoragePoolValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.DetachStorageDomainFromPoolParameters;
import org.ovirt.engine.core.common.action.RemoveStorageDomainParameters;
import org.ovirt.engine.core.common.action.StoragePoolParametersBase;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.VmNic;
import org.ovirt.engine.core.common.businessentities.network.VnicProfile;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.FormatStorageDomainVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.StorageDomainDao;
import org.ovirt.engine.core.dao.StoragePoolDao;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.dao.network.NetworkDao;
import org.ovirt.engine.core.dao.network.VmNicDao;
import org.ovirt.engine.core.dao.network.VnicProfileDao;
import org.ovirt.engine.core.di.Injector;
import org.ovirt.engine.core.utils.ISingleAsyncOperation;
import org.ovirt.engine.core.utils.SynchronizeNumberOfAsyncOperations;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@NonTransactiveCommandAttribute(forceCompensation = true)
public class RemoveStoragePoolCommand<T extends StoragePoolParametersBase> extends StorageHandlingCommandBase<T> {

    @Inject
    private NetworkDao networkDao;
    @Inject
    private VnicProfileDao vnicProfileDao;
    @Inject
    private StorageDomainDao storageDomainDao;
    @Inject
    private StoragePoolDao storagePoolDao;
    @Inject
    private VmNicDao vmNicDao;
    @Inject
    private VdsDao vdsDao;

    private Map<String, Pair<String, String>> sharedLocks;

    @Inject
    private ExternalNetworkManagerFactory externalNetworkManagerFactory;

    public RemoveStoragePoolCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    public RemoveStoragePoolCommand(Guid commandId) {
        super(commandId);
    }

    @Override
    protected void executeCommand() {
        removeNetworks();

        // Detach master storage domain last.
        List<StorageDomain> storageDomains = storageDomainDao.getAllForStoragePool(getStoragePool().getId());
        storageDomains.sort(Comparator.comparing(StorageDomain::getStorageDomainType));

        if (!storageDomains.isEmpty()) {
            if (!getParameters().isForceDelete() && !getAllRunningVdssInPool().isEmpty()) {
                if(!regularRemoveStorageDomains(storageDomains)) {
                    setSucceeded(false);
                    return;
                }
            } else if (getParameters().isForceDelete()) {
                forceRemoveStorageDomains(storageDomains);
            } else {
                return;
            }
        }

        getQuotaManager().removeStoragePoolFromCache(getStoragePool().getId());

        removeDataCenter();

        setSucceeded(true);
    }

    private void removeDataCenter() {
        TransactionSupport.executeInNewTransaction(() -> {
            getCompensationContext().snapshotEntity(getStoragePool());
            storagePoolDao.remove(getStoragePool().getId());
            getCompensationContext().stateChanged();

            return null;
        });
    }

    private void removeNetworks() {
        final List<Network> networks = networkDao.getAllForDataCenter(getStoragePoolId());
        for (Network network : networks) {
            if (network.isExternal()) {
                for (VmNic nic : vmNicDao.getAllForNetwork(network.getId())) {
                    externalNetworkManagerFactory.create(nic, network).deallocateIfExternal();
                }
            }
        }

        TransactionSupport.executeInNewTransaction(() -> {
            for (final Network net : networks) {
                List<VnicProfile> profiles = vnicProfileDao.getAllForNetwork(net.getId());
                for (VnicProfile vnicProfile : profiles) {
                    getCompensationContext().snapshotEntity(vnicProfile);
                    vnicProfileDao.remove(vnicProfile.getId());
                }
                getCompensationContext().snapshotEntity(net);
                networkDao.remove(net.getId());
            }
            getCompensationContext().stateChanged();
            return null;
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
        final StorageDomain masterDomain =
                storageDomains.stream().filter(s ->  s.getStorageDomainType() == StorageDomainType.Master).findFirst().orElse(null);
        lockStorageDomain(masterDomain);
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
                    log.error("Unable to detach storage domain '{}' '{}'",
                            storageDomain.getStorageName(),
                            storageDomain.getId());
                    retVal = false;
                }
            }
        }

        masterDomainDetachWithDestroyPool(masterDomain);
        runSynchronizeOperation(new DisconnectStoragePoolAsyncOperationFactory());

        setSucceeded(true);

        if (!getStoragePool().isLocal() || !masterDomain.isLocal()) {
            for (VDS vds : vdss) {
                storageHelperDirector.getItem(masterDomain.getStorageType())
                        .disconnectStorageFromDomainByVdsId(masterDomain, vds.getId());
            }
        } else {
            try {
                runVdsCommand(VDSCommandType.FormatStorageDomain,
                                new FormatStorageDomainVDSCommandParameters(vdss.get(0).getId(),
                                        masterDomain.getId()));
            } catch (EngineException e) {
                // Do nothing, exception already printed at logs
            }
            storageHelperDirector.getItem(masterDomain.getStorageType())
                    .disconnectStorageFromDomainByVdsId(masterDomain, vdss.get(0).getId());
            removeDomainFromDb(masterDomain);
        }

        return retVal;
    }

    private void removeDomainFromDb(final StorageDomain domain) {
        TransactionSupport.executeInNewTransaction(() -> {
            // Not compensation for remove domain as we don't want
            // to rollback a deleted domain - it will only cause more
            // problems if a domain got deleted in VDSM and not in backend
            // as it will be impossible to remove it.
            storageHelperDirector.getItem(domain.getStorageType())
                    .storageDomainRemoved(domain.getStorageStaticData());
            storageDomainDao.remove(domain.getId());
            return null;
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
            if (!backend
                    .runInternalAction(ActionType.DetachStorageDomainFromPool,
                            tempVar,
                            cloneContext().withoutCompensationContext().withoutExecutionContext())
                    .getSucceeded()) {
                return false;
            }
        } else {
            RemoveStorageDomainParameters tempVar = new RemoveStorageDomainParameters(storageDomain.getId());
            tempVar.setDestroyingPool(true);
            tempVar.setDoFormat(true);
            tempVar.setVdsId(vds.getId());
            if (!runInternalAction(ActionType.RemoveStorageDomain, tempVar, cloneContext().withoutLock().withoutExecutionContext())
                    .getSucceeded()) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected boolean validate() {
        if (!super.validate()) {
            return false;
        }

        StoragePoolValidator validator = createStoragePoolValidator();

        if (!validate(validator.exists())) {
            return false;
        }

        if (!getStoragePool().isManaged()) {
            return true;
        }

        if (!validator.isNotInStatus(StoragePoolStatus.Up).isValid()) {
            return failValidation(EngineMessage.ERROR_CANNOT_REMOVE_ACTIVE_STORAGE_POOL);
        }

        if (!validator.isInStatus(StoragePoolStatus.Uninitialized).isValid() && !getParameters().isForceDelete()
                && !initializeVds()) {
            return false;
        }

        final List<StorageDomain> poolDomains =
                storageDomainDao.getAllForStoragePool(getStoragePool().getId());

        if (!validateDomainsInMaintenance(poolDomains)) {
            return false;
        }
        if (!getParameters().isForceDelete()) {
            if(poolDomains.size() > 1) {
                return failValidation(EngineMessage.ERROR_CANNOT_REMOVE_STORAGE_POOL_WITH_NONMASTER_DOMAINS);
            }
            if (!poolDomains.isEmpty() && !canDetachStorageDomainWithVmsAndDisks(poolDomains.get(0))) {
                return false;
            }
        } else {
            List<VDS> poolHosts = vdsDao.getAllForStoragePool(getParameters().getStoragePoolId());

            sharedLocks = new HashMap<>();
            for (VDS host : poolHosts) {
                sharedLocks.put(host.getId().toString(),
                        LockMessagesMatchUtil.makeLockingPair(LockingGroup.VDS, EngineMessage.ACTION_TYPE_FAILED_OBJECT_LOCKED));
            }

            if (!poolHosts.isEmpty() && acquireLockInternal()) {
                for (VDS host : poolHosts) {
                    if (host.getStatus() != VDSStatus.Maintenance) {
                        return failValidation(EngineMessage.ERROR_CANNOT_FORCE_REMOVE_STORAGE_POOL_WITH_VDS_NOT_IN_MAINTENANCE);
                    }
                }
            }
        }
        return true;
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__TYPE__STORAGE__POOL);
        addValidationMessage(EngineMessage.VAR__ACTION__REMOVE);
    }

    protected boolean validateDomainsInMaintenance(List<StorageDomain> domainsList) {
        if (domainsList.stream().anyMatch(d -> d.getStatus() != StorageDomainStatus.Maintenance)) {
            return failValidation(EngineMessage.ERROR_CANNOT_REMOVE_POOL_WITH_ACTIVE_DOMAINS);
        }

        return true;
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        if (getParameters().isForceDelete()){
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
        SynchronizeNumberOfAsyncOperations sync = new SynchronizeNumberOfAsyncOperations(vdsList.size(),
                null, new ActivateDeactivateSingleAsyncOperationFactory() {

                    @Override
                    public ISingleAsyncOperation createSingleAsyncOperation() {
                        return Injector.injectMembers(
                                new ConnectVDSToPoolAndDomains(vdsList, masterDomain, storagePool));
                    }

                    @Override
                    public void initialize(List parameters) {
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
