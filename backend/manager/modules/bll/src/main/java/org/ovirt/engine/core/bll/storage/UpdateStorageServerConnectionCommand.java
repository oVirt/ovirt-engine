package org.ovirt.engine.core.bll.storage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.LockMessagesMatchUtil;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CompensationContext;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.action.StorageServerConnectionParametersBase;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainSharedStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMap;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.storage.LUNs;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.GetStorageDomainStatsVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.StorageServerConnectionManagementVDSParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.StorageDomainDynamicDao;
import org.ovirt.engine.core.dao.StoragePoolIsoMapDao;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@NonTransactiveCommandAttribute(forceCompensation = true)
public class UpdateStorageServerConnectionCommand<T extends StorageServerConnectionParametersBase> extends ConnectStorageToVdsCommand<T> {
    private List<StorageDomain> domains = new ArrayList<>();
    private List<LUNs> luns = new ArrayList<>();

    public UpdateStorageServerConnectionCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(Scope.Execution);
    }

    @Override
    protected boolean canDoAction() {
        StorageServerConnections newConnectionDetails = getConnection();
        StorageType storageType = newConnectionDetails.getstorage_type();
        if (!storageType.isFileDomain() && !storageType.equals(StorageType.ISCSI)) {
            return failCanDoAction(EngineMessage.ACTION_TYPE_FAILED_STORAGE_CONNECTION_UNSUPPORTED_ACTION_FOR_STORAGE_TYPE);
        }

        if (!isValidConnection(newConnectionDetails)) {
            return false;
        }

        // Check if connection exists by id, otherwise there's nothing to update
        String connectionId = newConnectionDetails.getId();

        StorageServerConnections oldConnection = getStorageConnDao().get(connectionId);

        if (oldConnection == null) {
            return failCanDoAction(EngineMessage.ACTION_TYPE_FAILED_STORAGE_CONNECTION_NOT_EXIST);
        }

        if (!newConnectionDetails.getstorage_type().equals(oldConnection.getstorage_type())) {
            return failCanDoAction(EngineMessage.ACTION_TYPE_FAILED_STORAGE_CONNECTION_UNSUPPORTED_CHANGE_STORAGE_TYPE);
        }

        Guid storagePoolId = getStoragePoolIdByFileConnectionId(oldConnection.getId());
        if (isConnWithSameDetailsExists(newConnectionDetails, storagePoolId)) {
            return failCanDoAction(EngineMessage.ACTION_TYPE_FAILED_STORAGE_CONNECTION_ALREADY_EXISTS);
        }

        if (doDomainsUseConnection(newConnectionDetails) || doLunsUseConnection()) {
            if (storageType.isFileDomain() && domains.size() > 1) {
                String domainNames = createDomainNamesList(domains);
                addCanDoActionMessageVariable("domainNames", domainNames);
                return failCanDoAction(EngineMessage.ACTION_TYPE_FAILED_STORAGE_CONNECTION_BELONGS_TO_SEVERAL_STORAGE_DOMAINS);
            }
            // Check that the storage domain is in proper state to be edited
            if (!isConnectionEditable(newConnectionDetails)) {
                return false;
            }
        }
        return super.canDoAction();
    }

    protected String createDomainNamesList(List<StorageDomain> domains) {
        // Build domain names list to display in the error
        StringBuilder domainNames = new StringBuilder();
        for (StorageDomain domain : domains) {
            domainNames.append(domain.getStorageName());
            domainNames.append(",");
        }
        // Remove the last comma after the last domain
        domainNames.deleteCharAt(domainNames.length() - 1);
        return domainNames.toString();
    }

    protected List<LUNs> getLuns() {
        if (luns.isEmpty()) {
            luns = getLunDao().getAllForStorageServerConnection(getConnection().getId());
        }
        return luns;
    }

    protected boolean isConnectionEditable(StorageServerConnections connection) {
        if (connection.getstorage_type().isFileDomain()) {
            boolean isConnectionEditable = isFileDomainInEditState(domains.get(0)) || getParameters().isForce();
            if (!isConnectionEditable) {
                addCanDoActionMessageVariable("domainNames", domains.get(0).getStorageName());
                addCanDoActionMessage(EngineMessage.ACTION_TYPE_FAILED_UNSUPPORTED_ACTION_DOMAIN_MUST_BE_IN_MAINTENANCE_OR_UNATTACHED);
            }
            return isConnectionEditable;
        }
        if (!getLuns().isEmpty()) {
            List<String> problematicVMNames = new ArrayList<>();
            List<String> problematicDomainNames = new ArrayList<>();
            for (LUNs lun : getLuns()) {
                Guid diskId = lun.getDiskId();
                if (diskId != null) {
                    Map<Boolean, List<VM>> vmsMap = getVmDao().getForDisk(diskId, true);
                    List<VM> pluggedVms = vmsMap.get(Boolean.TRUE);
                    if (pluggedVms != null && !pluggedVms.isEmpty()) {
                        for (VM vm : pluggedVms) {
                            if (!vm.getStatus().equals(VMStatus.Down)) {
                                problematicVMNames.add(vm.getName());
                            }
                        }
                    }
                }
                Guid storageDomainId = lun.getStorageDomainId();
                if (storageDomainId != null) {
                    StorageDomain domain = getStorageDomainDao().get(storageDomainId);
                    if (!domain.getStorageDomainSharedStatus().equals(StorageDomainSharedStatus.Unattached)
                            && !getParameters().isForce()) {
                        for (StoragePoolIsoMap map : getStoragePoolIsoMap(domain)) {
                            if (!map.getStatus().equals(StorageDomainStatus.Maintenance)) {
                                String domainName = domain.getStorageName();
                                problematicDomainNames.add(domainName);
                            } else {
                                domains.add(domain);
                            }
                        }
                    }
                    else { //unattached domain, edit allowed
                        domains.add(domain);
                    }

                }
            }
            if (!problematicVMNames.isEmpty()) {
                if (problematicDomainNames.isEmpty()) {
                    addCanDoActionMessageVariable("vmNames", prepareEntityNamesForMessage(problematicVMNames));
                    addCanDoActionMessage(EngineMessage.ACTION_TYPE_FAILED_STORAGE_CONNECTION_UNSUPPORTED_ACTION_FOR_RUNNING_VMS);
                } else {
                    addCanDoActionMessageVariable("vmNames", prepareEntityNamesForMessage(problematicVMNames));
                    addCanDoActionMessageVariable("domainNames", prepareEntityNamesForMessage(problematicDomainNames));
                    addCanDoActionMessage(EngineMessage.ACTION_TYPE_FAILED_STORAGE_CONNECTION_UNSUPPORTED_ACTION_FOR_RUNNING_VMS_AND_DOMAINS_STATUS);
                }
                return false;
            }

            if (!problematicDomainNames.isEmpty()) {
                addCanDoActionMessageVariable("domainNames", prepareEntityNamesForMessage(problematicDomainNames));
                addCanDoActionMessage(EngineMessage.ACTION_TYPE_FAILED_UNSUPPORTED_ACTION_DOMAIN_MUST_BE_IN_MAINTENANCE_OR_UNATTACHED);
                return false;
            }
        }
        return true;
    }

    private String prepareEntityNamesForMessage(List<String> entityNames) {
        return StringUtils.join(entityNames, ",");
    }

    private boolean isFileDomainInEditState(StorageDomain storageDomain) {
        return (storageDomain.getStatus() == StorageDomainStatus.Maintenance || storageDomain.getStorageDomainSharedStatus() == StorageDomainSharedStatus.Unattached);
    }

    @Override
    protected void executeCommand() {
        boolean isDomainUpdateRequired = !Guid.isNullOrEmpty(getVdsId()) && doDomainsUseConnection(getConnection());
        List<StorageDomain> updatedDomains = new ArrayList<>();
        boolean hasConnectStorageSucceeded = false;
        if (isDomainUpdateRequired) {
            hasConnectStorageSucceeded = connectToStorage();
            VDSReturnValue returnValueUpdatedStorageDomain = null;
            if (hasConnectStorageSucceeded) {
                changeStorageDomainStatusInTransaction(StorageDomainStatus.Locked);
                for (StorageDomain domain : domains) {
                    // update info such as free space - because we switched to a different server
                    returnValueUpdatedStorageDomain = getStatsForDomain(domain);
                    if (returnValueUpdatedStorageDomain.getSucceeded()) {
                        StorageDomain updatedStorageDomain =
                                (StorageDomain) returnValueUpdatedStorageDomain.getReturnValue();
                        updatedDomains.add(updatedStorageDomain);
                    }
                }
                if (!updatedDomains.isEmpty()) {
                   updateStorageDomain(updatedDomains);
                }
            }
        }
        getStorageConnDao().update(getParameters().getStorageServerConnection());
        if (isDomainUpdateRequired) {
            for (StorageDomain domain : domains) {
                for (StoragePoolIsoMap map : getStoragePoolIsoMap(domain)) {
                    restoreStateAfterUpdate(map);
                }
            }
            if (hasConnectStorageSucceeded) {
                disconnectFromStorage();
            }
        }
        setSucceeded(true);
    }

    protected void restoreStateAfterUpdate(StoragePoolIsoMap map) {
        updateStatus(map, StorageDomainStatus.Maintenance);
    }

    protected boolean doDomainsUseConnection(StorageServerConnections connection) {
        if (domains == null || domains.isEmpty()) {
            domains = getStorageDomainsByConnId(connection.getId());
        }
        return domains != null && !domains.isEmpty();
    }

    protected boolean doLunsUseConnection() {
        return !getLuns().isEmpty();
    }

    protected Collection<StoragePoolIsoMap> getStoragePoolIsoMap(StorageDomain storageDomain) {
        return getStoragePoolIsoMapDao().getAllForStorage(storageDomain.getId());
    }

    protected void changeStorageDomainStatusInTransaction(final StorageDomainStatus status) {
        executeInNewTransaction(new TransactionMethod<Void>() {
            @Override
            public Void runInTransaction() {
                CompensationContext context = getCompensationContext();
                for (StorageDomain domain : domains) {
                    for (StoragePoolIsoMap map : getStoragePoolIsoMap(domain)) {
                        context.snapshotEntityStatus(map);
                        updateStatus(map, status);
                    }
                }
                getCompensationContext().stateChanged();
                return null;
            }
        });
    }

    protected void updateStorageDomain(final List<StorageDomain> storageDomainsToUpdate) {
        executeInNewTransaction(new TransactionMethod<Void>() {
            @Override
            public Void runInTransaction() {
                for (StorageDomain domainToUpdate : storageDomainsToUpdate) {
                        CompensationContext context = getCompensationContext();
                        context.snapshotEntity(domainToUpdate.getStorageDynamicData());
                        getStorageDomainDynamicDao().update(domainToUpdate.getStorageDynamicData());
                        getCompensationContext().stateChanged();
                }
                return null;
            }
        });
    }

    protected void updateStatus(StoragePoolIsoMap map, StorageDomainStatus status) {
        log.info("Setting domain '{}' to status '{}'", map.getId(), status.name());
        map.setStatus(status);
        getStoragePoolIsoMapDao().updateStatus(map.getId(), map.getStatus());
    }

    protected void executeInNewTransaction(TransactionMethod<?> method) {
        TransactionSupport.executeInNewTransaction(method);
    }

    protected boolean connectToStorage() {
         Pair<Boolean, Integer> result = connectHostToStorage();
         return result.getFirst();
    }

    protected void disconnectFromStorage() {
        StorageServerConnectionManagementVDSParameters connectionParametersForVdsm =
                createParametersForVdsm(getParameters().getVdsId(),
                        Guid.Empty,
                        getConnection().getstorage_type(),
                        getConnection());
        boolean isDisconnectSucceeded =
                runVdsCommand(VDSCommandType.DisconnectStorageServer, connectionParametersForVdsm).getSucceeded();
        if (!isDisconnectSucceeded) {
            log.warn("Failed to disconnect storage connection {}", getConnection());
        }
    }

    protected VDSReturnValue getStatsForDomain(StorageDomain storageDomain) {
        return runVdsCommand(VDSCommandType.GetStorageDomainStats,
                new GetStorageDomainStatsVDSCommandParameters(getVds().getId(), storageDomain.getId()));
    }

    protected StorageServerConnectionManagementVDSParameters createParametersForVdsm(Guid vdsmId,
                                                                               Guid storagePoolId,
                                                                               StorageType storageType,
                                                                               StorageServerConnections storageServerConnection) {
        StorageServerConnectionManagementVDSParameters newConnectionParametersForVdsm =
                new StorageServerConnectionManagementVDSParameters(vdsmId, storagePoolId, storageType,
                        new ArrayList<>(Arrays.asList(storageServerConnection)));
        return newConnectionParametersForVdsm;
    }

    protected StorageDomainDynamicDao getStorageDomainDynamicDao() {
        return getDbFacade().getStorageDomainDynamicDao();
    }

    protected StoragePoolIsoMapDao getStoragePoolIsoMapDao() {
        return getDbFacade().getStoragePoolIsoMapDao();
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        Map<String, Pair<String, String>> locks = new HashMap<>();
        domains = getStorageDomainsByConnId(getConnection().getId());
        if (!domains.isEmpty()) {
            for (StorageDomain domain : domains) {
                locks.put(domain.getId().toString(),
                        LockMessagesMatchUtil.makeLockingPair(LockingGroup.STORAGE,
                                EngineMessage.ACTION_TYPE_FAILED_OBJECT_LOCKED));
            }
        }
        if (getConnection().getstorage_type().isFileDomain()) {
           // lock the path to avoid at the same time if some other user tries to
           // add new storage connection to same path or edit another storage server connection to point to same path
           locks.put(getConnection().getconnection(),
                    LockMessagesMatchUtil.makeLockingPair(LockingGroup.STORAGE_CONNECTION,
                            EngineMessage.ACTION_TYPE_FAILED_OBJECT_LOCKED));
        }
        else {
          // for block domains, locking the target details
          locks.put(getConnection().getconnection() + ";" + getConnection().getiqn() + ";" + getConnection().getport() + ";" + getConnection().getuser_name(),
          LockMessagesMatchUtil.makeLockingPair(LockingGroup.STORAGE_CONNECTION,
                    EngineMessage.ACTION_TYPE_FAILED_OBJECT_LOCKED));

          //lock lun disks and domains, not VMs , no need to load from db.
          if(getLuns()!=null) {
              for(LUNs lun : getLuns()) {
                Guid diskId = lun.getDiskId();
                Guid storageDomainId = lun.getStorageDomainId();
                if(diskId != null) {
                       locks.put(diskId.toString(), LockMessagesMatchUtil.makeLockingPair(LockingGroup.DISK,
                       EngineMessage.ACTION_TYPE_FAILED_OBJECT_LOCKED));
                }
                if(storageDomainId != null) {
                       locks.put(storageDomainId.toString(), LockMessagesMatchUtil.makeLockingPair(LockingGroup.STORAGE,
                       EngineMessage.ACTION_TYPE_FAILED_OBJECT_LOCKED));
                }

              }
          }

        }

        // lock connection's id to avoid editing or removing this connection at the same time
        // by another user
        locks.put(getConnection().getId(),
                LockMessagesMatchUtil.makeLockingPair(LockingGroup.STORAGE_CONNECTION,
                        EngineMessage.ACTION_TYPE_FAILED_OBJECT_LOCKED));
        return locks;
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(EngineMessage.VAR__ACTION__UPDATE);
        addCanDoActionMessage(EngineMessage.VAR__TYPE__STORAGE__CONNECTION);
    }
}
