package org.ovirt.engine.core.bll.storage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.LockMessagesMatchUtil;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.action.StorageServerConnectionParametersBase;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.storage.LUNs;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.StorageServerConnectionDAO;

@NonTransactiveCommandAttribute
public class RemoveStorageServerConnectionCommand<T extends StorageServerConnectionParametersBase> extends DisconnectStorageServerConnectionCommand<T> {

    public RemoveStorageServerConnectionCommand(T parameters) {
        super(parameters, null);
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(Scope.Execution);
    }

    @Override
    protected boolean canDoAction() {
        String connectionId = getConnection().getid();
        List<StorageDomain> domains = null;
        if (StringUtils.isEmpty(connectionId) ) {
           return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_CONNECTION_ID_EMPTY);
        }
        StorageServerConnections connection = getStorageServerConnectionDao().get(connectionId);
        if(connection == null) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_CONNECTION_NOT_EXIST);
        }
        // if user passed only the connection id for removal, vdsm still needs few more details in order to disconnect, so
        // bringing them from db and repopulating them in the connection object received in input parameters
        populateMissingFields(connection);
        StorageType storageType = connection.getstorage_type();
        if (storageType.isFileDomain()) {
            // go to storage domain static, get all storage domains where storage field  = storage connection id
           domains = getStorageDomainsByConnId(connectionId);
           if(domains.size() > 0) {
               String domainNames = createDomainNamesListFromStorageDomains(domains);
               return prepareFailureMessageForDomains(domainNames);
           }
        }
        else if (storageType.equals(StorageType.ISCSI)) {
           List<String> domainNames = new ArrayList<>();
           List<String> diskNames = new ArrayList<>();
           // go to luns to storage connections map table, get it from there
           List<LUNs> luns = getLunDao().getAllForStorageServerConnection(connectionId);
           if (!luns.isEmpty()) {
                String volumeGroupId = null;
                for(LUNs lun : luns) {
                    volumeGroupId = lun.getvolume_group_id();
                    if (StringUtils.isNotEmpty(volumeGroupId)) {
                        // non empty vg id indicates there's a storage domain using the lun
                        String domainName = lun.getStorageDomainName();
                        domainNames.add(domainName);
                    }
                    else {
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
                     }
                     else {
                        String diskNamesForMessage = prepareEntityNamesForMessage(diskNames);
                        return prepareFailureMessageForDomainsAndDisks(domainNamesForMessage, diskNamesForMessage);
                    }
                }
                else if (!diskNames.isEmpty()) {
                    String diskNamesForMessage = prepareEntityNamesForMessage(diskNames);
                    return prepareFailureMessageForDisks(diskNamesForMessage);
                }

              }
           }

           return true;
        }

    protected StorageServerConnectionDAO getStorageServerConnectionDao() {
        return getDbFacade().getStorageServerConnectionDao();
    }

    private String prepareEntityNamesForMessage(List<String> entityNames) {
        return StringUtils.join(entityNames, ",");
    }

    @Override
    protected void executeCommand() {
        String connectionId = getConnection().getid();
        getStorageServerConnectionDao().remove(connectionId);
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
        if(connectionFromParams.getstorage_type() == null || connectionFromParams.getstorage_type().equals(StorageType.UNKNOWN)) {
            connectionFromParams.setstorage_type(connectionFromDb.getstorage_type());
        }
        if(StringUtils.isEmpty(connectionFromParams.getconnection())) {
            connectionFromParams.setconnection(connectionFromDb.getconnection());
        }
        if(connectionFromParams.getstorage_type().equals(StorageType.ISCSI)){
            if(StringUtils.isEmpty(connectionFromParams.getiqn())) {
                connectionFromParams.setiqn(connectionFromDb.getiqn());
            }
            if(StringUtils.isEmpty(connectionFromParams.getuser_name())) {
                connectionFromParams.setuser_name(connectionFromDb.getuser_name());
            }
            if(StringUtils.isEmpty(connectionFromParams.getpassword())) {
                connectionFromParams.setpassword(connectionFromDb.getpassword());
            }
            if(StringUtils.isEmpty(connectionFromParams.getport())) {
                connectionFromParams.setport(connectionFromDb.getport());
            }
        }

    }

    protected boolean prepareFailureMessageForDomains(String domainNames) {
        addCanDoActionMessageVariable("domainNames", domainNames);
        return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_CONNECTION_BELONGS_TO_SEVERAL_STORAGE_DOMAINS);
    }

    protected boolean prepareFailureMessageForDisks(String diskNames) {
        addCanDoActionMessageVariable("diskNames", diskNames);
        return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_CONNECTION_BELONGS_TO_SEVERAL_DISKS);
    }

    protected boolean prepareFailureMessageForDomainsAndDisks(String domainNames, String diskNames) {
        addCanDoActionMessageVariable("domainNames", domainNames);
        addCanDoActionMessageVariable("diskNames", diskNames);
        return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_CONNECTION_BELONGS_TO_SEVERAL_STORAGE_DOMAINS_AND_DISKS);
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
        Map<String, Pair<String, String>> locks = new HashMap<String, Pair<String, String>>();
        locks.put(getConnection().getconnection(),
                LockMessagesMatchUtil.makeLockingPair(LockingGroup.STORAGE_CONNECTION,
                        VdcBllMessages.ACTION_TYPE_FAILED_OBJECT_LOCKED));
        // lock connection's id to avoid editing or removing this connection at the same time
        // by another user
        locks.put(getConnection().getid(),
                LockMessagesMatchUtil.makeLockingPair(LockingGroup.STORAGE_CONNECTION,
                        VdcBllMessages.ACTION_TYPE_FAILED_OBJECT_LOCKED));
        return locks;
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__REMOVE);
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__STORAGE__CONNECTION);
    }
}
