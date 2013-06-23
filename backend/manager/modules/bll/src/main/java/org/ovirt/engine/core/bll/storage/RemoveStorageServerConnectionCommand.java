package org.ovirt.engine.core.bll.storage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.LockIdNameAttribute;
import org.ovirt.engine.core.bll.LockMessagesMatchUtil;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.common.action.StorageServerConnectionParametersBase;
import org.ovirt.engine.core.common.businessentities.LUNs;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.dao.LunDAO;
import org.ovirt.engine.core.dao.StorageServerConnectionDAO;


@NonTransactiveCommandAttribute
@LockIdNameAttribute
public class RemoveStorageServerConnectionCommand<T extends StorageServerConnectionParametersBase> extends DisconnectStorageServerConnectionCommand {

    public RemoveStorageServerConnectionCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected boolean canDoAction() {
        String connectionId = getConnection().getid();
        List<StorageDomain> domains = new ArrayList<>();
        if (StringUtils.isEmpty(connectionId) ) {
           return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_CONNECTION_ID_EMPTY);
        }
        StorageServerConnections connection = getStorageServerConnectionDao().get(connectionId);
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
                        return prepareFailureMessageForDomains(domainNamesForMessage)  ;
                     }
                     else {
                        String diskNamesForMessage = prepareEntityNamesForMessage(diskNames);
                        return prepareFailureMessageForDomainsAndDisks(domainNamesForMessage,diskNamesForMessage);
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
        // disconnect the connection from vdsm
        disconnectStorage();
        setSucceeded(true);
    }

    protected boolean prepareFailureMessageForDomains(String domainNames) {
        addCanDoActionMessage(String.format("$domainNames %1$s", domainNames));
        return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_CONNECTION_BELONGS_TO_SEVERAL_STORAGE_DOMAINS);
    }

    protected boolean prepareFailureMessageForDisks(String diskNames) {
        addCanDoActionMessage(String.format("$diskNames %1$s", diskNames));
        return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_CONNECTION_BELONGS_TO_SEVERAL_DISKS);
    }

    protected boolean prepareFailureMessageForDomainsAndDisks(String domainNames, String diskNames) {
        addCanDoActionMessage(String.format("$domainNames %1$s", domainNames));
        addCanDoActionMessage(String.format("$diskNames %1$s", diskNames));
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

    protected LunDAO getLunDao() {
        return getDbFacade().getLunDao();
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        return Collections.singletonMap(getConnection().getconnection(),
                LockMessagesMatchUtil.makeLockingPair(LockingGroup.STORAGE_CONNECTION,
                        VdcBllMessages.ACTION_TYPE_FAILED_OBJECT_LOCKED));
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__REMOVE);
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__STORAGE__CONNECTION);
    }
}
