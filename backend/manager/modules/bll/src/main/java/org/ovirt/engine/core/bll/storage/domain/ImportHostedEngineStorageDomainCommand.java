package org.ovirt.engine.core.bll.storage.domain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.LockMessagesMatchUtil;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.hostedengine.HostedEngineHelper;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.AttachStorageDomainToPoolParameters;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.RemoveDiskParameters;
import org.ovirt.engine.core.common.action.StorageDomainManagementParameter;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.storage.BaseDisk;
import org.ovirt.engine.core.common.businessentities.storage.LUNs;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.constants.StorageConstants;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.queries.GetExistingStorageDomainListParameters;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.GetDeviceListVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.BaseDiskDao;
import org.ovirt.engine.core.dao.StoragePoolDao;
import org.ovirt.engine.core.dao.StorageServerConnectionDao;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

/**
 * <pre>
 * Try to import the hosted engine storage domain which is already connected to the host by the hosted engine broker.
 * We use 1) the storage domain id that is fetched from the hosted engine vm and then passed as a parameter to the command
 * 2) The connection details are fetched from the deviceList
 * {@link org.ovirt.engine.core.common.vdscommands.VDSCommandType#GetDeviceList} connected in vdsm
 * (as the domain already connected) and crossed the storage domain info.
 * With that in hand we are able to get the connection user/pass (in case of block device)
 * </pre>
 */
public class ImportHostedEngineStorageDomainCommand<T extends StorageDomainManagementParameter> extends CommandBase<T> {

    @Inject
    private HostedEngineHelper hostedEngineHelper;
    @Inject
    private StoragePoolDao storagePoolDao;
    @Inject
    private StorageServerConnectionDao storageServerConnectionDao;
    @Inject
    private BaseDiskDao baseDiskDao;

    private StorageDomain heStorageDomain;

    static final StorageType[] SUPPORTED_DOMAIN_TYPES =
            { StorageType.NFS, StorageType.FCP, StorageType.GLUSTERFS, StorageType.ISCSI };

    public ImportHostedEngineStorageDomainCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    // This is needed for command resume infrastructure
    public ImportHostedEngineStorageDomainCommand(Guid commandId) {
        super(commandId);
    }

    @Override
    protected void init() {
        setVdsId(getParameters().getVdsId());
        fetchStorageDomainInfo();
    }

    @Override
    protected boolean validate() {
        // no point in importing this domain without an active DC. The hosted
        // engine domain should never be the first, or master domain of a DC
        if (storagePoolDao.get(getVds().getStoragePoolId()).getStatus() != StoragePoolStatus.Up) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_MASTER_STORAGE_DOMAIN_NOT_ACTIVE);
        }
        // if sd imported already, fail
        if (hostedEngineHelper.getStorageDomain() != null) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_ALREADY_EXIST);
        }

        // fetch info on storage domain from VDSM, sets #heStorageDomain
        if (heStorageDomain == null) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_NOT_EXIST);
        }

        if (!Arrays.asList(SUPPORTED_DOMAIN_TYPES).contains(heStorageDomain.getStorageType())) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_TYPE_UNSUPPORTED);
        }
        return true;
    }

    @Override
    protected void executeCommand() {
        StorageDomainManagementParameter addSdParams =
                new StorageDomainManagementParameter(heStorageDomain.getStorageStaticData());
        addSdParams.setVdsId(getParameters().getVdsId());
        addSdParams.setStoragePoolId(getVds().getStoragePoolId());

        ActionType actionType = null;
        switch (heStorageDomain.getStorageType()) {
        case NFS:
        case GLUSTERFS:
            actionType = ActionType.AddExistingFileStorageDomain;
            addStorageServerConnection();
            break;
        case ISCSI:
            discoverBlockConnectionDetails();
        case FCP:
            actionType = ActionType.AddExistingBlockStorageDomain;
            removeHostedEngineLunDisk();
            break;
        }

        if (getSucceeded()) {
            setSucceeded(backend.runInternalAction(
                    actionType,
                    addSdParams,
                    getContext()).getSucceeded());
        }

        if (getSucceeded()) {
            AttachStorageDomainToPoolParameters attachSdParams =
                    new AttachStorageDomainToPoolParameters(
                            addSdParams.getStorageDomainId(),
                            addSdParams.getStoragePoolId());
            setSucceeded(backend.runInternalAction(
                    ActionType.AttachStorageDomainToPool,
                    attachSdParams,
                    getContext()).getSucceeded());
        }

        setActionReturnValue(heStorageDomain);
    }

    private void discoverBlockConnectionDetails() {
        // get device list
        VDSReturnValue getDeviceList = runVdsCommand(
                VDSCommandType.GetDeviceList,
                new GetDeviceListVDSCommandParameters(
                        getParameters().getVdsId(),
                        heStorageDomain.getStorageType()));

        if (getDeviceList.getSucceeded() && getDeviceList.getReturnValue() != null
                && heStorageDomain.getStorageStaticData().getStorage() != null) {
            for (LUNs lun : (ArrayList<LUNs>) getDeviceList.getReturnValue()) {
                // match a lun vgid to the domain vgid.
                if (heStorageDomain.getStorage().equals(lun.getVolumeGroupId())) {
                    // found a lun. Use its connection details
                    heStorageDomain.getStorageStaticData()
                            .setConnection(lun.getLunConnections().get(0));
                    setSucceeded(true);
                    break;
                }
            }
            if (!getSucceeded()) {
                log.error("There are no luns with VG that match the SD VG '{}'."
                        + " Connections details are missing.  completing this automatic import",
                        heStorageDomain.getStorage());
            }
        }
    }

    /**
     * For File based storage only, we need to save the connection in DB. It is implicitly called for SAN domains.
     */
    private void addStorageServerConnection() {
        TransactionSupport.executeInNewTransaction(() -> {
            StorageServerConnections connection = heStorageDomain.getStorageStaticData().getConnection();
            connection.setId(Guid.newGuid().toString());
            if (heStorageDomain.getStorageType() == StorageType.GLUSTERFS) {
                // The use of the vfs type is mainly used for posix Storage Domains, usually,
                // adding a posix SD will have a defined vfs type configured by the user,
                // for this specific Gluster Storage Domain the user does not indicate the vfs type,
                // and that is the reason why it is done only for Gluster
                connection.setVfsType(StorageType.GLUSTERFS.name().toLowerCase());
            }
            storageServerConnectionDao.save(connection);
            // make sure the storage domain object is full for the rest of the flow
            heStorageDomain.setStorage(connection.getId());
            setSucceeded(true);
            getCompensationContext().snapshotEntity(connection);
            getCompensationContext().stateChanged();
            return null;
        });
    }

    private void removeHostedEngineLunDisk() {
        List<BaseDisk> disks = baseDiskDao.getDisksByAlias(StorageConstants.HOSTED_ENGINE_LUN_DISK_ALIAS);
        if (disks != null && !disks.isEmpty()) {
            BaseDisk heDirectLun = disks.get(0);
            ActionReturnValue removeDisk = backend.runInternalAction(
                    ActionType.RemoveDisk,
                    new RemoveDiskParameters(heDirectLun.getId()));
            if (!removeDisk.getSucceeded()) {
                setSucceeded(false);
                log.error("Failed to remove the hosted engine direct lun disk");
                return;
            }
        }
        setSucceeded(true);
    }

    /**
     * This command should run internal only. No permission is needed.
     * @return empty collection. The subjects shouldn't be checked for permission.
     */
    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return Collections.emptyList();
    }

    private boolean fetchStorageDomainInfo() {
        QueryReturnValue allDomainsQuery = backend.runInternalQuery(
                QueryType.GetExistingStorageDomainList,
                new GetExistingStorageDomainListParameters(
                        getParameters().getVdsId(),
                        null,
                        StorageDomainType.Data,
                        null));
        if (allDomainsQuery.getSucceeded()) {
            for (StorageDomain sd : (List<StorageDomain>) allDomainsQuery.getReturnValue()) {
                if(sd.getId().equals(getParameters().getStorageDomainId())){
                    heStorageDomain = sd;
                    return true;
                }
            }
        } else {
            log.error("Failed query for all Storage Domains."
                    + " The import command can not proceed without this info");
        }
        return false;
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded()
                ? AuditLogType.HOSTED_ENGINE_DOMAIN_IMPORT_SUCCEEDED
                : AuditLogType.HOSTED_ENGINE_DOMAIN_IMPORT_FAILED;
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(LockProperties.Scope.Command);
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        if (heStorageDomain != null && heStorageDomain.getId() != null) {
            return Collections.singletonMap(
                    heStorageDomain.getId().toString(),
                    LockMessagesMatchUtil.makeLockingPair(
                            LockingGroup.STORAGE,
                            EngineMessage.ACTION_TYPE_FAILED_STORAGE_DEVICE_LOCKED));
        }
        return Collections.emptyMap();
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__ACTION__ADD);
        addValidationMessage(EngineMessage.VAR__TYPE__STORAGE__DOMAIN);
    }

}
