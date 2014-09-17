package org.ovirt.engine.core.bll.storage;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.context.CompensationContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.StorageDomainParametersBase;
import org.ovirt.engine.core.common.businessentities.LUN_storage_server_connection_map;
import org.ovirt.engine.core.common.businessentities.LUN_storage_server_connection_map_id;
import org.ovirt.engine.core.common.businessentities.LUNs;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMap;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMapId;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.eventqueue.Event;
import org.ovirt.engine.core.common.eventqueue.EventQueue;
import org.ovirt.engine.core.common.eventqueue.EventResult;
import org.ovirt.engine.core.common.eventqueue.EventType;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.BaseDiskDao;
import org.ovirt.engine.core.dao.DiskDao;
import org.ovirt.engine.core.dao.DiskImageDynamicDAO;
import org.ovirt.engine.core.dao.ImageDao;
import org.ovirt.engine.core.dao.ImageStorageDomainMapDao;
import org.ovirt.engine.core.dao.LunDAO;
import org.ovirt.engine.core.dao.SnapshotDao;
import org.ovirt.engine.core.dao.StorageServerConnectionDAO;
import org.ovirt.engine.core.utils.ejb.BeanProxyType;
import org.ovirt.engine.core.utils.ejb.BeanType;
import org.ovirt.engine.core.utils.ejb.EjbUtils;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.linq.Predicate;
import org.ovirt.engine.core.utils.threadpool.ThreadPoolUtil;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

public abstract class StorageDomainCommandBase<T extends StorageDomainParametersBase> extends
        StorageHandlingCommandBase<T> {

    protected StorageDomainCommandBase(T parameters) {
        this(parameters, null);
    }

    protected StorageDomainCommandBase(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }


    /**
     * Constructor for command creation when compensation is applied on startup
     *
     * @param commandId
     */
    protected StorageDomainCommandBase(Guid commandId) {
        super(commandId);
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
                && isDetachAllowed(isRemoveLast);
    }

    protected boolean isDetachAllowed(final boolean isRemoveLast) {
        boolean returnValue = true;
        if (getStoragePoolIsoMap() == null) {
            returnValue = false;
            addCanDoActionMessage(VdcBllMessages.STORAGE_DOMAIN_NOT_ATTACHED_TO_STORAGE_POOL);
        } else if (!isRemoveLast
                && isMaster()) {

            StorageDomain storage_domains =
                    LinqUtils.firstOrNull(getStorageDomainDAO().getAllForStoragePool
                            (getStorageDomain().getStoragePoolId()),
                            new Predicate<StorageDomain>() {
                                @Override
                                public boolean eval(StorageDomain a) {
                                    return a.getId().equals(getStorageDomain().getId())
                                            && a.getStatus() == StorageDomainStatus.Active;
                                }
                            });
            if (storage_domains == null) {
                returnValue = false;
                addCanDoActionMessage(VdcBllMessages.ERROR_CANNOT_DETACH_LAST_STORAGE_DOMAIN);
            }
        }
        return returnValue;
    }

    protected boolean isNotLocalData(final boolean isInternal) {
        boolean returnValue = true;
        if (this.getStoragePool().isLocal()
                && getStorageDomain().getStorageDomainType() == StorageDomainType.Data
                && !isInternal) {
            returnValue = false;
            addCanDoActionMessage(VdcBllMessages.VDS_GROUP_CANNOT_DETACH_DATA_DOMAIN_FROM_LOCAL_STORAGE);
        }
        return returnValue;
    }

    private StoragePoolIsoMap getStoragePoolIsoMap() {
        return getStoragePoolIsoMapDAO()
                .get(new StoragePoolIsoMapId(getStorageDomain().getId(),
                        getStoragePoolId()));
    }

    private boolean isMaster() {
        return getStorageDomain().getStorageDomainType() == StorageDomainType.Master;
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__STORAGE__DOMAIN);
    }

    protected boolean checkStorageDomainNameLengthValid() {
        boolean result = true;
        if (getStorageDomain().getStorageName().length() > Config
                .<Integer> getValue(ConfigValues.StorageDomainNameSizeLimit)) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_NAME_LENGTH_IS_TOO_LONG);
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
            if (status.isStorageDomainInProcess()) {
                return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_OBJECT_LOCKED);
            }
            addStorageDomainStatusIllegalMessage();
        }
        return valid;
    }

    protected boolean checkStorageDomainStatusNotEqual(StorageDomainStatus status) {
        boolean returnValue = false;
        if (getStorageDomainStatus() != null) {
            returnValue = (getStorageDomainStatus() != status);
            if (!returnValue) {
                addStorageDomainStatusIllegalMessage();
            }
        }
        return returnValue;
    }

    protected boolean checkStorageDomainNotInPool() {
        return isStorageDomainNotInPool(getStorageDomain());
    }


    protected boolean checkMasterDomainIsUp() {
        boolean returnValue = true;
        List<StorageDomain> storageDomains = getStorageDomainDAO().getAllForStoragePool(getStoragePool().getId());
        storageDomains = LinqUtils.filter(storageDomains, new Predicate<StorageDomain>() {
            @Override
            public boolean eval(StorageDomain a) {
                return a.getStorageDomainType() == StorageDomainType.Master
                        && a.getStatus() == StorageDomainStatus.Active;
            }
        });
        if (storageDomains.isEmpty()) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_MASTER_STORAGE_DOMAIN_NOT_ACTIVE);
            returnValue = false;
        }
        return returnValue;
    }

    protected void setStorageDomainStatus(StorageDomainStatus status, CompensationContext context) {
        if (getStorageDomain() != null && getStorageDomain().getStoragePoolId() != null) {
            StoragePoolIsoMap map = getStorageDomain().getStoragePoolIsoMapData();
            if(context != null) {
                context.snapshotEntityStatus(map);
            }
            getStorageDomain().setStatus(status);
            getStoragePoolIsoMapDAO().updateStatus(map.getId(), status);
        }
    }

    protected boolean isLunsAlreadyInUse(List<String> lunIds) {
        // Get LUNs from DB
        List<LUNs> lunsFromDb = getLunDao().getAll();
        Set<LUNs> lunsUsedBySDs = new HashSet<>();
        Set<LUNs> lunsUsedByDisks = new HashSet<>();

        for (LUNs lun : lunsFromDb) {
            if (lunIds.contains(lun.getLUN_id())) {
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
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_LUNS_ALREADY_PART_OF_STORAGE_DOMAINS);
            Set<String> formattedIds = new HashSet<>();
            for (LUNs lun : lunsUsedBySDs) {
                formattedIds.add(getFormattedLunId(lun, lun.getStorageDomainName()));
            }
            addCanDoActionMessageVariable("lunIds", StringUtils.join(formattedIds, ", "));
        }

        if (!lunsUsedByDisks.isEmpty()) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_LUNS_ALREADY_USED_BY_DISKS);
            Set<String> formattedIds = new HashSet<>();
            for (LUNs lun : lunsUsedByDisks) {
                formattedIds.add(getFormattedLunId(lun, lun.getDiskAlias()));
            }
            addCanDoActionMessageVariable("lunIds", StringUtils.join(formattedIds, ", "));
        }

       return !lunsUsedBySDs.isEmpty() || !lunsUsedByDisks.isEmpty();
    }

    protected String getFormattedLunId(LUNs lun, String usedByEntityName) {
        return String.format("%1$s (%2$s)", lun.getLUN_id(), usedByEntityName);
    }

    public static void proceedLUNInDb(final LUNs lun, StorageType storageType) {
        proceedLUNInDb(lun, storageType, "");
    }

    protected LunDAO getLunDao() {
        return DbFacade.getInstance().getLunDao();
    }

    public static void proceedLUNInDb(final LUNs lun, StorageType storageType, String volumeGroupId) {
        lun.setvolume_group_id(volumeGroupId);
        if (DbFacade.getInstance().getLunDao().get(lun.getLUN_id()) == null) {
            DbFacade.getInstance().getLunDao().save(lun);
        } else if (!volumeGroupId.isEmpty()) {
            DbFacade.getInstance().getLunDao().update(lun);
        }

        if (storageType == StorageType.FCP) {
            // No need to handle connections (FCP storage doesn't utilize connections).
            return;
        }

        for (StorageServerConnections connection : lun.getLunConnections()) {
            List<StorageServerConnections> connections = DbFacade.getInstance()
                    .getStorageServerConnectionDao().getAllForConnection(connection);
            if (connections.isEmpty()) {
                connection.setid(Guid.newGuid().toString());
                connection.setstorage_type(storageType);
                DbFacade.getInstance().getStorageServerConnectionDao().save(connection);

            } else {
                connection.setid(connections.get(0).getid());
            }
            if (DbFacade.getInstance()
                    .getStorageServerConnectionLunMapDao()
                    .get(new LUN_storage_server_connection_map_id(lun.getLUN_id(),
                            connection.getid())) == null) {
                DbFacade.getInstance().getStorageServerConnectionLunMapDao().save(
                        new LUN_storage_server_connection_map(lun.getLUN_id(), connection.getid()));
            }
        }
    }

    protected List<Pair<Guid, Boolean>> connectHostsInUpToDomainStorageServer() {
        List<VDS> hostsInStatusUp = getAllRunningVdssInPool();
        List<Callable<Pair<Guid, Boolean>>> callables = new LinkedList<>();
        for (final VDS vds : hostsInStatusUp) {
            callables.add(new Callable<Pair<Guid, Boolean>>() {
                @Override
                public Pair<Guid, Boolean> call() throws Exception {
                    Pair<Guid, Boolean> toReturn = new Pair<>(vds.getId(), Boolean.FALSE);
                    try {
                        boolean connectResult = StorageHelperDirector.getInstance().getItem(getStorageDomain().getStorageType())
                                .connectStorageToDomainByVdsId(getStorageDomain(), vds.getId());
                        toReturn.setSecond(connectResult);
                    } catch (RuntimeException e) {
                        log.errorFormat("Failed to connect host {0} to storage domain (name: {1}, id: {2}). Exception: {3}",
                                vds.getName(), getStorageDomain().getName(), getStorageDomain().getId(), e);
                    }
                    return toReturn;
                }
            });
        }

        return ThreadPoolUtil.invokeAll(callables);
    }

    protected void disconnectAllHostsInPool() {
        ((EventQueue) EjbUtils.findBean(BeanType.EVENTQUEUE_MANAGER, BeanProxyType.LOCAL)).submitEventSync(
                new Event(getParameters().getStoragePoolId(), getParameters().getStorageDomainId(), null, EventType.POOLREFRESH, ""),
                new Callable<EventResult>() {
                    @Override
                    public EventResult call() {
                        runSynchronizeOperation(new RefreshStoragePoolAndDisconnectAsyncOperationFactory());
                        return null;
                    }
        });
    }

    /**
     *  The new master is a data domain which is preferred to be in Active/Unknown status, if selectInactiveWhenNoActiveUnknownDomains
     * is set to True, an Inactive domain will be returned in case that no domain in Active/Unknown status was found.
     * @return an elected master domain or null
     */
    protected StorageDomain electNewMaster(boolean duringReconstruct, boolean selectInactiveWhenNoActiveUnknownDomains, boolean canChooseCurrentMasterAsNewMaster) {
        if (getStoragePool() == null) {
            log.warnFormat("Cannot elect new master: storage pool not found");
            return null;
        }

        List<StorageDomain> storageDomains = getStorageDomainDAO().getAllForStoragePool(getStoragePool().getId());

        if (storageDomains.isEmpty()) {
            log.warnFormat("Cannot elect new master, no storage domains found for pool {0}", getStoragePool().getName());
            return null;
        }

        Collections.sort(storageDomains, LastTimeUsedAsMasterComp.instance);

        StorageDomain newMaster = null;
        StorageDomain storageDomain = getStorageDomain();

        for (StorageDomain dbStorageDomain : storageDomains) {
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
    protected StorageDomain electNewMaster(boolean duringReconstruct) {
        return electNewMaster(duringReconstruct, false, false);
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return Collections.singletonList(new PermissionSubject(getParameters().getStorageDomainId(),
                VdcObjectType.Storage,
                getActionType().getActionGroup()));
    }

    protected void changeStorageDomainStatusInTransaction(final StoragePoolIsoMap map,
            final StorageDomainStatus status) {
        executeInNewTransaction(new TransactionMethod<StoragePoolIsoMap>() {
            @SuppressWarnings("synthetic-access")
            @Override
            public StoragePoolIsoMap runInTransaction() {
                CompensationContext context = getCompensationContext();
                context.snapshotEntityStatus(map);
                map.setStatus(status);
                getStoragePoolIsoMapDAO().updateStatus(map.getId(), map.getStatus());
                getCompensationContext().stateChanged();
                return null;
            }
        });
    }

    private StorageDomainStatus getStorageDomainStatus() {
        StorageDomainStatus status = null;
        if (getStorageDomain() != null) {
            status = getStorageDomain().getStatus();
        }
        return status;
    }

    protected void addStorageDomainStatusIllegalMessage() {
        addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_DOMAIN_STATUS_ILLEGAL2);
        addCanDoActionMessageVariable("status", getStorageDomainStatus());
    }

    protected BaseDiskDao getBaseDiskDao() {
        return getDbFacade().getBaseDiskDao();
    }

    protected DiskDao getDiskDao() {
        return getDbFacade().getDiskDao();
    }

    protected ImageDao getImageDao() {
        return getDbFacade().getImageDao();
    }

    protected DiskImageDynamicDAO getDiskImageDynamicDAO() {
        return getDbFacade().getDiskImageDynamicDao();
    }

    protected SnapshotDao getSnapshotDao() {
        return getDbFacade().getSnapshotDao();
    }

    protected ImageStorageDomainMapDao getImageStorageDomainMapDao() {
        return getDbFacade().getImageStorageDomainMapDao();
    }

    protected StorageServerConnectionDAO getStorageServerConnectionDAO() {
        return getDbFacade().getStorageServerConnectionDao();
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
}
