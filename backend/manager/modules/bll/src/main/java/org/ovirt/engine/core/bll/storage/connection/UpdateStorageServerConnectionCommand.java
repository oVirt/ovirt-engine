package org.ovirt.engine.core.bll.storage.connection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.LockMessagesMatchUtil;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
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
import org.ovirt.engine.core.dao.LunDao;
import org.ovirt.engine.core.dao.StorageDomainDao;
import org.ovirt.engine.core.dao.StorageDomainDynamicDao;
import org.ovirt.engine.core.dao.StoragePoolIsoMapDao;
import org.ovirt.engine.core.dao.StorageServerConnectionDao;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@NonTransactiveCommandAttribute(forceCompensation = true)
public class UpdateStorageServerConnectionCommand<T extends StorageServerConnectionParametersBase> extends ConnectStorageToVdsCommand<T> {

    @Inject
    private StorageServerConnectionDao storageServerConnectionDao;
    @Inject
    private LunDao lunDao;
    @Inject
    private StorageDomainDao storageDomainDao;
    @Inject
    private StoragePoolIsoMapDao storagePoolIsoMapDao;
    @Inject
    private StorageDomainDynamicDao storageDomainDynamicDao;
    @Inject
    private VmDao vmDao;

    private List<StorageDomain> domains = new ArrayList<>();
    private List<LUNs> luns = new ArrayList<>();

    public UpdateStorageServerConnectionCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    public UpdateStorageServerConnectionCommand(Guid commandId) {
        super(commandId);
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(Scope.Execution);
    }

    @Override
    protected boolean validate() {
        StorageServerConnections newConnectionDetails = getConnection();
        StorageType storageType = newConnectionDetails.getStorageType();
        if (!storageType.isFileDomain() && !storageType.equals(StorageType.ISCSI)) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_STORAGE_CONNECTION_UNSUPPORTED_ACTION_FOR_STORAGE_TYPE);
        }

        if (!isValidConnection(newConnectionDetails)) {
            return false;
        }

        // Check if connection exists by id, otherwise there's nothing to update
        String connectionId = newConnectionDetails.getId();

        StorageServerConnections oldConnection = storageServerConnectionDao.get(connectionId);

        if (oldConnection == null) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_STORAGE_CONNECTION_NOT_EXIST);
        }

        if (!newConnectionDetails.getStorageType().equals(oldConnection.getStorageType())) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_STORAGE_CONNECTION_UNSUPPORTED_CHANGE_STORAGE_TYPE);
        }

        Guid storagePoolId = getStoragePoolIdByFileConnectionId(oldConnection.getId());
        String duplicateConnectionId = isConnWithSameDetailsExists(newConnectionDetails, storagePoolId);
        if (!duplicateConnectionId.isEmpty() && !duplicateConnectionId.equalsIgnoreCase(newConnectionDetails.getId())) {
            String storageDomainName = getStorageNameByConnectionId(duplicateConnectionId);
            addValidationMessageVariable("connectionId", duplicateConnectionId);
            addValidationMessageVariable("storageDomainName", storageDomainName);
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_STORAGE_CONNECTION_ALREADY_EXISTS);
        }

        if (doDomainsUseConnection(newConnectionDetails) || doLunsUseConnection()) {
            if (storageType.isFileDomain() && domains.size() > 1) {
                String domainNames = createDomainNamesList(domains);
                addValidationMessageVariable("domainNames", domainNames);
                return failValidation(EngineMessage.ACTION_TYPE_FAILED_STORAGE_CONNECTION_BELONGS_TO_SEVERAL_STORAGE_DOMAINS);
            }
            // Check that the storage domain is in proper state to be edited
            if (!isConnectionEditable(newConnectionDetails)) {
                return false;
            }
        }
        return super.validate();
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
            luns = lunDao.getAllForStorageServerConnection(getConnection().getId());
        }
        return luns;
    }

    protected boolean isConnectionEditable(StorageServerConnections connection) {
        if (connection.getStorageType().isFileDomain()) {
            boolean isConnectionEditable = isFileDomainInEditState(domains.get(0)) || getParameters().isForce();
            if (!isConnectionEditable) {
                addValidationMessageVariable("domainNames", domains.get(0).getStorageName());
                addValidationMessage(EngineMessage.ACTION_TYPE_FAILED_UNSUPPORTED_ACTION_DOMAIN_MUST_BE_IN_MAINTENANCE_OR_UNATTACHED);
            }
            return isConnectionEditable;
        }
        if (!getLuns().isEmpty()) {
            List<String> problematicVMNames = new ArrayList<>();
            List<String> problematicDomainNames = new ArrayList<>();
            for (LUNs lun : getLuns()) {
                Guid diskId = lun.getDiskId();
                if (diskId != null) {
                    Map<Boolean, List<VM>> vmsMap = vmDao.getForDisk(diskId, true);
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
                    StorageDomain domain = storageDomainDao.get(storageDomainId);
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
                    } else { //unattached domain, edit allowed
                        domains.add(domain);
                    }

                }
            }
            if (!problematicVMNames.isEmpty()) {
                if (problematicDomainNames.isEmpty()) {
                    addValidationMessageVariable("vmNames", prepareEntityNamesForMessage(problematicVMNames));
                    addValidationMessage(EngineMessage.ACTION_TYPE_FAILED_STORAGE_CONNECTION_UNSUPPORTED_ACTION_FOR_RUNNING_VMS);
                } else {
                    addValidationMessageVariable("vmNames", prepareEntityNamesForMessage(problematicVMNames));
                    addValidationMessageVariable("domainNames", prepareEntityNamesForMessage(problematicDomainNames));
                    addValidationMessage(EngineMessage.ACTION_TYPE_FAILED_STORAGE_CONNECTION_UNSUPPORTED_ACTION_FOR_RUNNING_VMS_AND_DOMAINS_STATUS);
                }
                return false;
            }

            if (!problematicDomainNames.isEmpty()) {
                addValidationMessageVariable("domainNames", prepareEntityNamesForMessage(problematicDomainNames));
                addValidationMessage(EngineMessage.ACTION_TYPE_FAILED_UNSUPPORTED_ACTION_DOMAIN_MUST_BE_IN_MAINTENANCE_OR_UNATTACHED);
                return false;
            }
        }
        return true;
    }

    private String prepareEntityNamesForMessage(List<String> entityNames) {
        return StringUtils.join(entityNames, ",");
    }

    private boolean isFileDomainInEditState(StorageDomain storageDomain) {
        return storageDomain.getStatus() == StorageDomainStatus.Maintenance || storageDomain.getStorageDomainSharedStatus() == StorageDomainSharedStatus.Unattached;
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
        storageServerConnectionDao.update(getParameters().getStorageServerConnection());
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
        return storagePoolIsoMapDao.getAllForStorage(storageDomain.getId());
    }

    protected void changeStorageDomainStatusInTransaction(final StorageDomainStatus status) {
        executeInNewTransaction(() -> {
            CompensationContext context = getCompensationContext();
            for (StorageDomain domain : domains) {
                for (StoragePoolIsoMap map : getStoragePoolIsoMap(domain)) {
                    context.snapshotEntityStatus(map);
                    updateStatus(map, status);
                }
            }
            getCompensationContext().stateChanged();
            return null;
        });
    }

    protected void updateStorageDomain(final List<StorageDomain> storageDomainsToUpdate) {
        executeInNewTransaction(() -> {
            for (StorageDomain domainToUpdate : storageDomainsToUpdate) {
                    CompensationContext context = getCompensationContext();
                    context.snapshotEntity(domainToUpdate.getStorageDynamicData());
                    storageDomainDynamicDao.update(domainToUpdate.getStorageDynamicData());
                    getCompensationContext().stateChanged();
            }
            return null;
        });
    }

    protected void updateStatus(StoragePoolIsoMap map, StorageDomainStatus status) {
        log.info("Setting domain '{}' to status '{}'", map.getId(), status.name());
        map.setStatus(status);
        storagePoolIsoMapDao.updateStatus(map.getId(), map.getStatus());
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
                        getConnection().getStorageType(),
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
        return new StorageServerConnectionManagementVDSParameters(vdsmId, storagePoolId, storageType,
                new ArrayList<>(Arrays.asList(storageServerConnection)));
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
        if (getConnection().getStorageType().isFileDomain()) {
           // lock the path to avoid at the same time if some other user tries to
           // add new storage connection to same path or edit another storage server connection to point to same path
           locks.put(getConnection().getConnection(),
                    LockMessagesMatchUtil.makeLockingPair(LockingGroup.STORAGE_CONNECTION,
                            EngineMessage.ACTION_TYPE_FAILED_OBJECT_LOCKED));
        } else {
          // for block domains, locking the target details
          locks.put(getConnection().getConnection() + ";" + getConnection().getIqn() + ";" + getConnection().getPort() + ";" + getConnection().getUserName(),
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
        addValidationMessage(EngineMessage.VAR__ACTION__UPDATE);
        addValidationMessage(EngineMessage.VAR__TYPE__STORAGE__CONNECTION);
    }
}
