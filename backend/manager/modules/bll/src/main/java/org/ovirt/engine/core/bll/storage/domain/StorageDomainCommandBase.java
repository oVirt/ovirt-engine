package org.ovirt.engine.core.bll.storage.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.context.CompensationContext;
import org.ovirt.engine.core.bll.hostedengine.HostedEngineHelper;
import org.ovirt.engine.core.bll.profiles.DiskProfileHelper;
import org.ovirt.engine.core.bll.storage.StorageHandlingCommandBase;
import org.ovirt.engine.core.bll.storage.connection.CINDERStorageHelper;
import org.ovirt.engine.core.bll.storage.connection.ISCSIStorageHelper;
import org.ovirt.engine.core.bll.storage.connection.IStorageHelper;
import org.ovirt.engine.core.bll.storage.connection.StorageHelperDirector;
import org.ovirt.engine.core.bll.storage.pool.RefreshStoragePoolAndDisconnectAsyncOperationFactory;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.DiskProfileParameters;
import org.ovirt.engine.core.common.action.StorageDomainParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainSharedStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMap;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMapId;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.profiles.DiskProfile;
import org.ovirt.engine.core.common.businessentities.storage.LUNStorageServerConnectionMap;
import org.ovirt.engine.core.common.businessentities.storage.LUNStorageServerConnectionMapId;
import org.ovirt.engine.core.common.businessentities.storage.LUNs;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.eventqueue.Event;
import org.ovirt.engine.core.common.eventqueue.EventQueue;
import org.ovirt.engine.core.common.eventqueue.EventType;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.CommandEntityDao;
import org.ovirt.engine.core.dao.ImageStorageDomainMapDao;
import org.ovirt.engine.core.utils.threadpool.ThreadPoolUtil;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

public abstract class StorageDomainCommandBase<T extends StorageDomainParametersBase> extends
        StorageHandlingCommandBase<T> {

    @Inject
    private HostedEngineHelper hostedEngineHelper;

    @Inject
    protected EventQueue eventQueue;

    protected StorageDomainCommandBase(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }


    /**
     * Constructor for command creation when compensation is applied on startup
     */
    protected StorageDomainCommandBase(Guid commandId) {
        super(commandId);
    }

    @Override
    protected boolean checkStoragePool() {
        return super.checkStoragePool();
    }

    @Override
    public Guid getStorageDomainId() {
        return getParameters() != null ? !Guid.Empty.equals(getParameters().getStorageDomainId()) ? getParameters()
                .getStorageDomainId() : super.getStorageDomainId() : super.getStorageDomainId();
    }

    protected boolean canDetachDomain(boolean isDestroyStoragePool, boolean isRemoveLast, boolean isInternal) {
        return checkStoragePool()
                && checkStorageDomain()
                && checkStorageDomainStatus(StorageDomainStatus.Inactive, StorageDomainStatus.Maintenance)
                && (isMaster() || isDestroyStoragePool || checkMasterDomainIsUp())
                && isNotLocalData(isInternal)
                && isDetachAllowed(isRemoveLast)
                && isCinderStorageHasNoDisks();
    }

    protected boolean isDetachAllowed(final boolean isRemoveLast) {

        if (getStoragePoolIsoMap() == null) {
            return failValidation(EngineMessage.STORAGE_DOMAIN_NOT_ATTACHED_TO_STORAGE_POOL);
        }
        if (!isRemoveLast && isMaster()) {
            return failValidation(EngineMessage.ERROR_CANNOT_DETACH_LAST_STORAGE_DOMAIN);
        }
        return true;
    }

    protected boolean isNotLocalData(final boolean isInternal) {
        if (this.getStoragePool().isLocal()
                && getStorageDomain().getStorageDomainType() == StorageDomainType.Data
                && !isInternal) {
            return failValidation(EngineMessage.CLUSTER_CANNOT_DETACH_DATA_DOMAIN_FROM_LOCAL_STORAGE);
        }
        return true;
    }

    private StoragePoolIsoMap getStoragePoolIsoMap() {
        return getStoragePoolIsoMapDao()
                .get(new StoragePoolIsoMapId(getStorageDomain().getId(),
                        getStoragePoolId()));
    }

    protected boolean isCinderStorageHasNoDisks() {
        if (getStorageDomain().getStorageType() == StorageType.CINDER) {
            return validate(CINDERStorageHelper.isCinderHasNoImages(getStorageDomainId()));
        }
        return true;
    }

    private boolean isMaster() {
        return getStorageDomain().getStorageDomainType() == StorageDomainType.Master;
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__TYPE__STORAGE__DOMAIN);
    }

    protected boolean checkStorageDomainNameLengthValid() {
        boolean result = true;
        if (StringUtils.isNotEmpty(getStorageDomain().getStorageName()) &&
                getStorageDomain().getStorageName().length() > Config
                .<Integer> getValue(ConfigValues.StorageDomainNameSizeLimit)) {
            addValidationMessage(EngineMessage.ACTION_TYPE_FAILED_NAME_LENGTH_IS_TOO_LONG);
            result = false;
        }
        return result;
    }

    protected boolean checkStorageDomain() {
        return isStorageDomainNotNull(getStorageDomain());
    }

    protected boolean checkStorageDomainStatus(final StorageDomainStatus... statuses) {
        boolean valid = false;
        StorageDomainStatus status = getStorageDomainStatus();
        if (status != null) {
            valid = Arrays.asList(statuses).contains(status);
        }
        if (!valid) {
            if (status != null && status.isStorageDomainInProcess()) {
                return failValidation(EngineMessage.ACTION_TYPE_FAILED_OBJECT_LOCKED);
            }
            addStorageDomainStatusIllegalMessage();
        }
        return valid;
    }

    protected boolean checkStorageDomainStatusNotEqual(StorageDomainStatus status) {
        boolean returnValue = false;
        if (getStorageDomainStatus() != null) {
            returnValue = getStorageDomainStatus() != status;
            if (!returnValue) {
                addStorageDomainStatusIllegalMessage();
            }
        }
        return returnValue;
    }

    protected boolean checkMasterDomainIsUp() {
        boolean hasUpMaster =
                !getStorageDomainDao().getStorageDomains
                        (getStoragePool().getId(), StorageDomainType.Master, StorageDomainStatus.Active).isEmpty();

        if (!hasUpMaster) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_MASTER_STORAGE_DOMAIN_NOT_ACTIVE);
        }
        return true;
    }

    protected void setStorageDomainStatus(StorageDomainStatus status, CompensationContext context) {
        if (getStorageDomain() != null && getStorageDomain().getStoragePoolId() != null) {
            StoragePoolIsoMap map = getStorageDomain().getStoragePoolIsoMapData();
            if(context != null) {
                context.snapshotEntityStatus(map);
            }
            getStorageDomain().setStatus(status);
            getStoragePoolIsoMapDao().updateStatus(map.getId(), status);
        }
    }

    protected boolean isLunsAlreadyInUse(List<String> lunIds) {
        // Get LUNs from DB
        List<LUNs> lunsFromDb = getLunDao().getAll();
        Set<LUNs> lunsUsedBySDs = new HashSet<>();
        Set<LUNs> lunsUsedByDisks = new HashSet<>();

        for (LUNs lun : lunsFromDb) {
            if (lunIds.contains(lun.getLUNId())) {
                if (lun.getStorageDomainId() != null) {
                    // LUN is already part of a storage domain
                    lunsUsedBySDs.add(lun);
                }
                if (lun.getDiskId() != null) {
                    // LUN is already used by a disk
                    lunsUsedByDisks.add(lun);
                }
            }
        }

        if (!lunsUsedBySDs.isEmpty()) {
            addValidationMessage(EngineMessage.ACTION_TYPE_FAILED_LUNS_ALREADY_PART_OF_STORAGE_DOMAINS);
            Set<String> formattedIds = new HashSet<>();
            for (LUNs lun : lunsUsedBySDs) {
                formattedIds.add(getFormattedLunId(lun, lun.getStorageDomainName()));
            }
            addValidationMessageVariable("lunIds", StringUtils.join(formattedIds, ", "));
        }

        if (!lunsUsedByDisks.isEmpty()) {
            addValidationMessage(EngineMessage.ACTION_TYPE_FAILED_LUNS_ALREADY_USED_BY_DISKS);
            Set<String> formattedIds = new HashSet<>();
            for (LUNs lun : lunsUsedByDisks) {
                formattedIds.add(getFormattedLunId(lun, lun.getDiskAlias()));
            }
            addValidationMessageVariable("lunIds", StringUtils.join(formattedIds, ", "));
        }

       return !lunsUsedBySDs.isEmpty() || !lunsUsedByDisks.isEmpty();
    }

    protected String getFormattedLunId(LUNs lun, String usedByEntityName) {
        return String.format("%1$s (%2$s)", lun.getLUNId(), usedByEntityName);
    }

    public static void proceedLUNInDb(final LUNs lun, StorageType storageType) {
        proceedLUNInDb(lun, storageType, "");
    }

    public static void proceedLUNInDb(final LUNs lun, StorageType storageType, String volumeGroupId) {
        lun.setVolumeGroupId(volumeGroupId);
        if (DbFacade.getInstance().getLunDao().get(lun.getLUNId()) == null) {
            DbFacade.getInstance().getLunDao().save(lun);
        } else if (!volumeGroupId.isEmpty()) {
            DbFacade.getInstance().getLunDao().update(lun);
        }

        if (storageType == StorageType.FCP) {
            // No need to handle connections (FCP storage doesn't utilize connections).
            return;
        }

        for (StorageServerConnections connection : lun.getLunConnections()) {
            StorageServerConnections dbConnection = ISCSIStorageHelper.findConnectionWithSameDetails(connection);
            if (dbConnection == null) {
                connection.setId(Guid.newGuid().toString());
                connection.setStorageType(storageType);
                DbFacade.getInstance().getStorageServerConnectionDao().save(connection);

            } else {
                connection.setId(dbConnection.getId());
            }
            if (DbFacade.getInstance()
                    .getStorageServerConnectionLunMapDao()
                    .get(new LUNStorageServerConnectionMapId(lun.getLUNId(),
                            connection.getId())) == null) {
                DbFacade.getInstance().getStorageServerConnectionLunMapDao().save(
                        new LUNStorageServerConnectionMap(lun.getLUNId(), connection.getId()));
            }
        }
    }

    protected List<Pair<Guid, Boolean>> connectHostsInUpToDomainStorageServer() {
        List<VDS> hostsInStatusUp = getAllRunningVdssInPool();
        List<Callable<Pair<Guid, Boolean>>> callables = new LinkedList<>();
        for (final VDS vds : hostsInStatusUp) {
            callables.add(() -> {
                Pair<Guid, Boolean> toReturn = new Pair<>(vds.getId(), Boolean.FALSE);
                try {
                    boolean connectResult = StorageHelperDirector.getInstance().getItem(getStorageDomain().getStorageType())
                            .connectStorageToDomainByVdsId(getStorageDomain(), vds.getId());
                    toReturn.setSecond(connectResult);
                } catch (RuntimeException e) {
                    log.error("Failed to connect host '{}' to storage domain (name '{}', id '{}'): {}",
                            vds.getName(),
                            getStorageDomain().getName(),
                            getStorageDomain().getId(),
                            e.getMessage());
                    log.debug("Exception", e);
                }
                return toReturn;
            });
        }

        return ThreadPoolUtil.invokeAll(callables);
    }

    protected List<Pair<Guid, Boolean>> disconnectHostsInUpToDomainStorageServer() {
        List<VDS> hostsInStatusUp = getAllRunningVdssInPool();
        List<Callable<Pair<Guid, Boolean>>> callables = new LinkedList<>();
        for (final VDS vds : hostsInStatusUp) {
            callables.add(() -> {
                Pair<Guid, Boolean> toReturn = new Pair<>(vds.getId(), Boolean.FALSE);
                try {
                    boolean connectResult = StorageHelperDirector.getInstance().getItem(getStorageDomain().getStorageType())
                            .disconnectStorageFromDomainByVdsId(getStorageDomain(), vds.getId());
                    toReturn.setSecond(connectResult);
                } catch (RuntimeException e) {
                    log.error("Failed to disconnect host '{}' to storage domain (name '{}', id '{}'): {}",
                            vds.getName(),
                            getStorageDomain().getName(),
                            getStorageDomain().getId(),
                            e.getMessage());
                    log.debug("Exception", e);
                }
                return toReturn;
            });
        }

        return ThreadPoolUtil.invokeAll(callables);
    }

    protected void disconnectAllHostsInPool() {
        getEventQueue().submitEventSync(
                new Event(getParameters().getStoragePoolId(),
                        getParameters().getStorageDomainId(),
                        null,
                        EventType.POOLREFRESH,
                        ""),
                () -> {
                    runSynchronizeOperation(new RefreshStoragePoolAndDisconnectAsyncOperationFactory());
                    return null;
                });
    }

    /**
     *  The new master is a data domain which is preferred to be in Active/Unknown status, if selectInactiveWhenNoActiveUnknownDomains
     * is set to True, an Inactive domain will be returned in case that no domain in Active/Unknown status was found.
     * @return an elected master domain or null
     */
    protected StorageDomain electNewMaster(boolean duringReconstruct, boolean selectInactiveWhenNoActiveUnknownDomains, boolean canChooseCurrentMasterAsNewMaster) {
        if (getStoragePool() == null) {
            log.warn("Cannot elect new master: storage pool not found");
            return null;
        }

        List<StorageDomain> storageDomains = getStorageDomainDao().getAllForStoragePool(getStoragePool().getId());

        if (storageDomains.isEmpty()) {
            log.warn("Cannot elect new master, no storage domains found for pool {}", getStoragePool().getName());
            return null;
        }

        Collections.sort(storageDomains, LastTimeUsedAsMasterComp.instance);

        StorageDomain newMaster = null;
        StorageDomain storageDomain = getStorageDomain();

        for (StorageDomain dbStorageDomain : storageDomains) {
            if (hostedEngineHelper.isHostedEngineStorageDomain(dbStorageDomain)) {
                continue;
            }
            if ((storageDomain == null || (duringReconstruct || !dbStorageDomain.getId()
                    .equals(storageDomain.getId())))
                    && ((dbStorageDomain.getStorageDomainType() == StorageDomainType.Data)
                    ||
                    (canChooseCurrentMasterAsNewMaster && dbStorageDomain.getStorageDomainType() == StorageDomainType.Master))) {
                if (dbStorageDomain.getStatus() == StorageDomainStatus.Active
                        || dbStorageDomain.getStatus() == StorageDomainStatus.Unknown) {
                    newMaster = dbStorageDomain;
                    break;
                } else if (selectInactiveWhenNoActiveUnknownDomains && newMaster == null
                        && dbStorageDomain.getStatus() == StorageDomainStatus.Inactive) {
                    // if the found domain is inactive, we don't break to continue and look for
                    // active/unknown domain.
                    newMaster = dbStorageDomain;
                }
            }
        }

        return newMaster;
    }

    /**
     * returns new master domain which is in Active/Unknown status
     * @return an elected master domain or null
     */
    protected StorageDomain electNewMaster() {
        return electNewMaster(false, false, false);
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        List permissionCheckSubjects = new ArrayList<>();
        permissionCheckSubjects.add(new PermissionSubject(getParameters().getStorageDomainId(),
                VdcObjectType.Storage,
                getActionType().getActionGroup()));
        return permissionCheckSubjects;
    }

    protected void changeStorageDomainStatusInTransaction(final StoragePoolIsoMap map,
                                                          final StorageDomainStatus status) {
        changeStorageDomainStatusInTransaction(map, status, getCompensationContext());
    }

    protected void changeStorageDomainStatusInTransaction(final StoragePoolIsoMap map,
            final StorageDomainStatus status, final CompensationContext context) {
        executeInNewTransaction(() -> {
            context.snapshotEntityStatus(map);
            map.setStatus(status);
            getStoragePoolIsoMapDao().updateStatus(map.getId(), map.getStatus());
            context.stateChanged();
            return null;
        });
    }

    protected void changeDomainStatusWithCompensation(StoragePoolIsoMap map, StorageDomainStatus compensateStatus,
                                                      StorageDomainStatus newStatus, CompensationContext context) {
        map.setStatus(compensateStatus);
        changeStorageDomainStatusInTransaction(map, newStatus, context);
    }

    private StorageDomainStatus getStorageDomainStatus() {
        StorageDomainStatus status = null;
        if (getStorageDomain() != null) {
            status = getStorageDomain().getStatus();
        }
        return status;
    }

    private StorageDomainSharedStatus getStorageDomainSharedStatus() {
        return getStorageDomain() == null ? null : getStorageDomain().getStorageDomainSharedStatus();
    }

    protected void addStorageDomainStatusIllegalMessage() {
        addValidationMessage(EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_STATUS_ILLEGAL2);
        StorageDomainStatus status = getStorageDomainStatus();
        StorageDomainSharedStatus sharedStatus = getStorageDomainSharedStatus();
        Object messageParameter = status;
        if (status == StorageDomainStatus.Unknown && sharedStatus != null) {
            // We got more informative information than "Unknown".
            messageParameter = sharedStatus;
        }
        addValidationMessageVariable("status", messageParameter);
    }

    protected ImageStorageDomainMapDao getImageStorageDomainMapDao() {
        return getDbFacade().getImageStorageDomainMapDao();
    }

    protected IStorageHelper getStorageHelper(StorageDomain storageDomain) {
        return StorageHelperDirector.getInstance().getItem(storageDomain.getStorageType());
    }

    protected void executeInNewTransaction(TransactionMethod<?> method) {
        TransactionSupport.executeInNewTransaction(method);
    }

    private static final class LastTimeUsedAsMasterComp implements Comparator<StorageDomain>, Serializable {
        private static final long serialVersionUID = -7736904426129973519L;
        public static final LastTimeUsedAsMasterComp instance = new LastTimeUsedAsMasterComp();

        @Override
        public int compare(StorageDomain o1, StorageDomain o2) {
            return Long.compare(o1.getLastTimeUsedAsMaster(), o2.getLastTimeUsedAsMaster());
        }
    }

    protected CommandEntityDao getCommandEntityDao() {
        return getDbFacade().getCommandEntityDao();
    }

    protected boolean isCinderStorageDomain() {
        return getStorageDomain().getStorageType().isCinderDomain();
    }

    protected EventQueue getEventQueue() {
        return eventQueue;
    }

    /**
     * Creates default disk profile for existing storage domain.
     */
    protected void createDefaultDiskProfile() {
        executeInNewTransaction(() -> {
            final DiskProfile diskProfile =
                    DiskProfileHelper.createDiskProfile(getStorageDomain().getId(), getStorageDomainName());
            DiskProfileParameters diskProfileParameters = new DiskProfileParameters(diskProfile, true);
            runInternalAction(VdcActionType.AddDiskProfile, diskProfileParameters);
            getCompensationContext().snapshotNewEntity(diskProfile);
            getCompensationContext().stateChanged();
            return null;
        });
    }
}
