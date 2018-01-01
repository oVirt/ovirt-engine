package org.ovirt.engine.core.bll.storage.domain;

import static java.util.stream.Collectors.toSet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.function.Function;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.context.CompensationContext;
import org.ovirt.engine.core.bll.profiles.DiskProfileHelper;
import org.ovirt.engine.core.bll.provider.ProviderProxyFactory;
import org.ovirt.engine.core.bll.provider.storage.OpenStackVolumeProviderProxy;
import org.ovirt.engine.core.bll.storage.StorageHandlingCommandBase;
import org.ovirt.engine.core.bll.storage.connection.IStorageHelper;
import org.ovirt.engine.core.bll.storage.disk.image.ImagesHandler;
import org.ovirt.engine.core.bll.storage.pool.RefreshStoragePoolAndDisconnectAsyncOperationFactory;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.utils.VmDeviceUtils;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.DiskProfileParameters;
import org.ovirt.engine.core.common.action.StorageDomainParametersBase;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainSharedStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMap;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMapId;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.profiles.DiskProfile;
import org.ovirt.engine.core.common.businessentities.storage.LUNs;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.eventqueue.Event;
import org.ovirt.engine.core.common.eventqueue.EventQueue;
import org.ovirt.engine.core.common.eventqueue.EventType;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.GetStorageDomainStatsVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.LunDao;
import org.ovirt.engine.core.dao.StorageDomainDao;
import org.ovirt.engine.core.dao.StorageDomainDynamicDao;
import org.ovirt.engine.core.dao.StoragePoolIsoMapDao;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.utils.threadpool.ThreadPoolUtil;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

public abstract class StorageDomainCommandBase<T extends StorageDomainParametersBase> extends
        StorageHandlingCommandBase<T> {

    @Inject
    private EventQueue eventQueue;
    @Inject
    private VmDeviceUtils vmDeviceUtils;
    @Inject
    private DiskProfileHelper diskProfileHelper;
    @Inject
    protected LunHelper lunHelper;
    @Inject
    private StoragePoolIsoMapDao storagePoolIsoMapDao;
    @Inject
    private LunDao lunDao;
    @Inject
    private StorageDomainDao storageDomainDao;
    @Inject
    private StorageDomainDynamicDao storageDomainDynamicDao;
    @Inject
    private VmDao vmDao;

    @Inject
    protected ImagesHandler imagesHandler;
    @Inject
    private ProviderProxyFactory providerProxyFactory;

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
    public Guid getStorageDomainId() {
        return getParameters() != null ? !Guid.Empty.equals(getParameters().getStorageDomainId()) ? getParameters()
                .getStorageDomainId() : super.getStorageDomainId() : super.getStorageDomainId();
    }

    protected boolean canDetachDomain(boolean isDestroyStoragePool) {
        return validate(createStoragePoolValidator().exists())
                && checkStorageDomain()
                && checkStorageDomainStatus(StorageDomainStatus.Maintenance)
                && (isMaster() || isDestroyStoragePool || checkMasterDomainIsUp())
                && isDetachAllowed()
                && isCinderStorageHasNoDisks();
    }

    protected boolean isDetachAllowed() {

        if (getStoragePoolIsoMap() == null) {
            return failValidation(EngineMessage.STORAGE_DOMAIN_NOT_ATTACHED_TO_STORAGE_POOL);
        }
        if (isMaster() && storageDomainDao.getAllForStoragePool(getStoragePoolId()).size() > 1) {
            return failValidation(EngineMessage.ERROR_CANNOT_DETACH_LAST_STORAGE_DOMAIN);
        }
        return true;
    }

    private StoragePoolIsoMap getStoragePoolIsoMap() {
        return storagePoolIsoMapDao.get(new StoragePoolIsoMapId(getStorageDomain().getId(), getStoragePoolId()));
    }

    protected boolean isCinderStorageHasNoDisks() {
        if (getStorageDomain().getStorageType() == StorageType.CINDER) {
            return validate(OpenStackVolumeProviderProxy.getFromStorageDomainId(getStorageDomainId(), providerProxyFactory)
                    .getProviderValidator()
                    .isCinderHasNoImages());
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
        return checkStorageDomainStatus(Arrays.stream(statuses).collect(toSet()));
    }

    protected boolean checkStorageDomainStatus(Set<StorageDomainStatus> statuses) {
        boolean valid = false;
        StorageDomainStatus status = getStorageDomainStatus();
        if (status != null) {
            valid = statuses.contains(status);
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
                !storageDomainDao.getStorageDomains
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
            storagePoolIsoMapDao.updateStatus(map.getId(), status);
        }
    }

    protected boolean isLunsAlreadyInUse(Set<String> lunIds) {
        // Get LUNs from DB
        List<LUNs> lunsFromDb = lunDao.getAll();
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
            Set<String> formattedIds = lunsUsedBySDs.stream()
                    .map(lun -> getFormattedLunId(lun, lun.getStorageDomainName()))
                    .collect(toSet());
            addValidationMessageVariable("lunIds", StringUtils.join(formattedIds, ", "));
        }

        if (!lunsUsedByDisks.isEmpty()) {
            addValidationMessage(EngineMessage.ACTION_TYPE_FAILED_LUNS_ALREADY_USED_BY_DISKS);
            Set<String> formattedIds = lunsUsedByDisks.stream()
                    .map(lun -> getFormattedLunId(lun, lun.getDiskAlias()))
                    .collect(toSet());
            addValidationMessageVariable("lunIds", StringUtils.join(formattedIds, ", "));
        }

       return !lunsUsedBySDs.isEmpty() || !lunsUsedByDisks.isEmpty();
    }

    protected String getFormattedLunId(LUNs lun, String usedByEntityName) {
        return String.format("%1$s (%2$s)", lun.getLUNId(), usedByEntityName);
    }

    protected List<Pair<Guid, Boolean>> connectHostsInUpToDomainStorageServer() {
        return performConnectionOpOnAllUpHosts(vds -> storageHelperDirector.getItem(getStorageDomain().getStorageType())
                .connectStorageToDomainByVdsId(getStorageDomain(), vds.getId()));
    }

    protected List<Pair<Guid, Boolean>> disconnectHostsInUpToDomainStorageServer() {
        return performConnectionOpOnAllUpHosts(vds -> storageHelperDirector.getItem(getStorageDomain().getStorageType())
                .disconnectStorageFromDomainByVdsId(getStorageDomain(), vds.getId()));
    }

    private List<Pair<Guid, Boolean>> performConnectionOpOnAllUpHosts(Function<VDS, Boolean> connectionMethod) {
        List<VDS> hostsInStatusUp = getAllRunningVdssInPool();
        List<Callable<Pair<Guid, Boolean>>> callables = new LinkedList<>();
        for (final VDS vds : hostsInStatusUp) {
            callables.add(() -> {
                Pair<Guid, Boolean> toReturn = new Pair<>(vds.getId(), Boolean.FALSE);
                try {
                    toReturn.setSecond(connectionMethod.apply(vds));
                } catch (RuntimeException e) {
                    log.error("Failed to connect/disconnect host '{}' to storage domain (name '{}', id '{}'): {}",
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

        List<StorageDomain> storageDomains = storageDomainDao.getAllForStoragePool(getStoragePool().getId());

        if (storageDomains.isEmpty()) {
            log.warn("Cannot elect new master, no storage domains found for pool {}", getStoragePool().getName());
            return null;
        }

        Collections.sort(storageDomains,
                Comparator.comparing(StorageDomain::getLastTimeUsedAsMaster)
                        .thenComparing(Comparator.comparing(StorageDomain::isLocal)));

        StorageDomain newMaster = null;
        StorageDomain storageDomain = getStorageDomain();

        for (StorageDomain dbStorageDomain : storageDomains) {
            if (dbStorageDomain.isHostedEngineStorage()) {
                continue;
            }
            if ((storageDomain == null || (duringReconstruct || !dbStorageDomain.getId()
                    .equals(storageDomain.getId())))
                    && ((dbStorageDomain.getStorageDomainType() == StorageDomainType.Data && !dbStorageDomain.isBackup())
                            || (canChooseCurrentMasterAsNewMaster
                                    && dbStorageDomain.getStorageDomainType() == StorageDomainType.Master))) {
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
        List<PermissionSubject> permissionCheckSubjects = new ArrayList<>();
        permissionCheckSubjects.add(new PermissionSubject(getParameters().getStorageDomainId(),
                VdcObjectType.Storage,
                getActionType().getActionGroup()));
        return permissionCheckSubjects;
    }

    protected void changeStorageDomainStatusInTransaction(final StoragePoolIsoMap map,
                                                          final StorageDomainStatus status) {
        changeStorageDomainStatusInTransaction(map, status, getCompensationContext());
    }

    private void changeStorageDomainStatusInTransaction(final StoragePoolIsoMap map,
            final StorageDomainStatus status, final CompensationContext context) {
        executeInNewTransaction(() -> {
            context.snapshotEntityStatus(map);
            map.setStatus(status);
            storagePoolIsoMapDao.updateStatus(map.getId(), map.getStatus());
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

    protected List<VM> getVmsOnlyOnStorageDomain() {
        List<VM> allVmsRelatedToSD = vmDao.getAllForStorageDomain(getStorageDomainId());
        List<VM> vmsWithDisksOnMultipleStorageDomain = vmDao.getAllVMsWithDisksOnOtherStorageDomain(getStorageDomainId());
        allVmsRelatedToSD.removeAll(vmsWithDisksOnMultipleStorageDomain);
        return allVmsRelatedToSD;
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

    protected IStorageHelper getStorageHelper(StorageDomain storageDomain) {
        return storageHelperDirector.getItem(storageDomain.getStorageType());
    }

    protected void executeInNewTransaction(TransactionMethod<?> method) {
        TransactionSupport.executeInNewTransaction(method);
    }

    protected VmDeviceUtils getVmDeviceUtils() {
        return vmDeviceUtils;
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
                    diskProfileHelper.createDiskProfile(getStorageDomain().getId(), getStorageDomainName());
            DiskProfileParameters diskProfileParameters = new DiskProfileParameters(diskProfile, true);
            runInternalActionWithTasksContext(ActionType.AddDiskProfile, diskProfileParameters);
            getCompensationContext().snapshotNewEntity(diskProfile);
            getCompensationContext().stateChanged();
            return null;
        });
    }

    protected void updateStorageDomainDynamicFromIrs() {
        final StorageDomain sd =
                (StorageDomain) runVdsCommand(VDSCommandType.GetStorageDomainStats,
                        new GetStorageDomainStatsVDSCommandParameters(getVds().getId(),
                                getStorageDomain().getId()))
                        .getReturnValue();
        TransactionSupport.executeInNewTransaction(() -> {
            getCompensationContext().snapshotEntity(getStorageDomain().getStorageDynamicData());
            storageDomainDynamicDao.update(sd.getStorageDynamicData());
            getCompensationContext().stateChanged();
            return null;
        });
    }
}
