package org.ovirt.engine.core.bll.storage.connection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.LockMessagesMatchUtil;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.action.StorageServerConnectionParametersBase;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.storage.LUNs;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.LunDao;
import org.ovirt.engine.core.dao.StorageServerConnectionDao;

@NonTransactiveCommandAttribute
public class RemoveStorageServerConnectionCommand<T extends StorageServerConnectionParametersBase> extends DisconnectStorageServerConnectionCommand<T> {

    @Inject
    private StorageServerConnectionDao storageServerConnectionDao;
    @Inject
    private LunDao lunDao;

    public RemoveStorageServerConnectionCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(Scope.Execution);
    }

    @Override
    protected boolean validate() {
        String connectionId = getConnection().getId();
        List<StorageDomain> domains = null;
        if (StringUtils.isEmpty(connectionId) ) {
           return failValidation(EngineMessage.ACTION_TYPE_FAILED_STORAGE_CONNECTION_ID_EMPTY);
        }
        StorageServerConnections connection = storageServerConnectionDao.get(connectionId);
        if(connection == null) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_STORAGE_CONNECTION_NOT_EXIST);
        }
        // if user passed only the connection id for removal, vdsm still needs few more details in order to disconnect, so
        // bringing them from db and repopulating them in the connection object received in input parameters
        populateMissingFields(connection);
        StorageType storageType = connection.getStorageType();
        if (storageType.isFileDomain()) {
            // go to storage domain static, get all storage domains where storage field  = storage connection id
           domains = getStorageDomainsByConnId(connectionId);
           if(domains.size() > 0) {
               String domainNames = createDomainNamesListFromStorageDomains(domains);
               return prepareFailureMessageForDomains(domainNames);
           }
        } else if (storageType.equals(StorageType.ISCSI)) {
           List<String> domainNames = new ArrayList<>();
           List<String> diskNames = new ArrayList<>();
           // go to luns to storage connections map table, get it from there
           List<LUNs> luns = lunDao.getAllForStorageServerConnection(connectionId);
           if (!luns.isEmpty()) {
                String volumeGroupId = null;
                for(LUNs lun : luns) {
                    volumeGroupId = lun.getVolumeGroupId();
                    if (StringUtils.isNotEmpty(volumeGroupId)) {
                        // non empty vg id indicates there's a storage domain using the lun
                        String domainName = lun.getStorageDomainName();
                        domainNames.add(domainName);
                    } else {
                        // empty vg id indicates there's a lun disk using the lun
                        String lunDiskName = lun.getDiskAlias();
                        diskNames.add(lunDiskName);
                    }
                }
                String domainNamesForMessage = null;
                if (!domainNames.isEmpty() ) {
                    // Build domain names list to display in the error
                    domainNamesForMessage = prepareEntityNamesForMessage(domainNames);
                    if (diskNames.isEmpty()) {
                        return prepareFailureMessageForDomains(domainNamesForMessage);
                    } else {
                        String diskNamesForMessage = prepareEntityNamesForMessage(diskNames);
                        return prepareFailureMessageForDomainsAndDisks(domainNamesForMessage, diskNamesForMessage);
                    }
                } else if (!diskNames.isEmpty()) {
                    String diskNamesForMessage = prepareEntityNamesForMessage(diskNames);
                    return prepareFailureMessageForDisks(diskNamesForMessage);
                }

              }
           }

           return true;
        }

    private String prepareEntityNamesForMessage(List<String> entityNames) {
        return StringUtils.join(entityNames, ",");
    }

    @Override
    protected void executeCommand() {
        String connectionId = getConnection().getId();
        storageServerConnectionDao.remove(connectionId);
        log.info("Removing connection '{}' from database ", connectionId);
        if (Guid.isNullOrEmpty(getParameters().getVdsId())) {
            log.info("No vdsId passed - hosts will not be disconnected.");
        } else {
            // disconnect the connection from vdsm
            disconnectStorage();
        }
        setSucceeded(true);
    }

    protected void populateMissingFields(StorageServerConnections connectionFromDb) {
        StorageServerConnections connectionFromParams = getConnection();
        if(connectionFromParams.getStorageType() == null || connectionFromParams.getStorageType().equals(StorageType.UNKNOWN)) {
            connectionFromParams.setStorageType(connectionFromDb.getStorageType());
        }
        if(StringUtils.isEmpty(connectionFromParams.getConnection())) {
            connectionFromParams.setConnection(connectionFromDb.getConnection());
        }
        if(connectionFromParams.getStorageType().equals(StorageType.ISCSI)){
            if(StringUtils.isEmpty(connectionFromParams.getIqn())) {
                connectionFromParams.setIqn(connectionFromDb.getIqn());
            }
            if(StringUtils.isEmpty(connectionFromParams.getUserName())) {
                connectionFromParams.setUserName(connectionFromDb.getUserName());
            }
            if(StringUtils.isEmpty(connectionFromParams.getPassword())) {
                connectionFromParams.setPassword(connectionFromDb.getPassword());
            }
            if(StringUtils.isEmpty(connectionFromParams.getPort())) {
                connectionFromParams.setPort(connectionFromDb.getPort());
            }
        }

    }

    protected boolean prepareFailureMessageForDomains(String domainNames) {
        addValidationMessageVariable("domainNames", domainNames);
        return failValidation(EngineMessage.ACTION_TYPE_FAILED_STORAGE_CONNECTION_BELONGS_TO_SEVERAL_STORAGE_DOMAINS);
    }

    protected boolean prepareFailureMessageForDisks(String diskNames) {
        addValidationMessageVariable("diskNames", diskNames);
        return failValidation(EngineMessage.ACTION_TYPE_FAILED_STORAGE_CONNECTION_BELONGS_TO_SEVERAL_DISKS);
    }

    protected boolean prepareFailureMessageForDomainsAndDisks(String domainNames, String diskNames) {
        addValidationMessageVariable("domainNames", domainNames);
        addValidationMessageVariable("diskNames", diskNames);
        return failValidation(EngineMessage.ACTION_TYPE_FAILED_STORAGE_CONNECTION_BELONGS_TO_SEVERAL_STORAGE_DOMAINS_AND_DISKS);
    }

    protected String createDomainNamesListFromStorageDomains(List<StorageDomain> domains) {
        // Build domain names list to display in the error
        StringBuilder domainNames = new StringBuilder();
        for (StorageDomain domain : domains) {
            domainNames.append(domain.getStorageName());
            domainNames.append(",");
        }
        // Remove the last "," after the last domain
        domainNames.deleteCharAt(domainNames.length() - 1);
        return domainNames.toString();
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        Map<String, Pair<String, String>> locks = new HashMap<>();
        locks.put(getConnection().getConnection(),
                LockMessagesMatchUtil.makeLockingPair(LockingGroup.STORAGE_CONNECTION,
                        EngineMessage.ACTION_TYPE_FAILED_OBJECT_LOCKED));
        // lock connection's id to avoid editing or removing this connection at the same time
        // by another user
        locks.put(getConnection().getId(),
                LockMessagesMatchUtil.makeLockingPair(LockingGroup.STORAGE_CONNECTION,
                        EngineMessage.ACTION_TYPE_FAILED_OBJECT_LOCKED));
        return locks;
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__ACTION__REMOVE);
        addValidationMessage(EngineMessage.VAR__TYPE__STORAGE__CONNECTION);
    }
}
