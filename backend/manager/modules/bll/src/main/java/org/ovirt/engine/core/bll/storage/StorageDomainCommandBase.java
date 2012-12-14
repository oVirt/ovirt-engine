package org.ovirt.engine.core.bll.storage;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.ovirt.engine.core.bll.context.CompensationContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.StorageDomainParametersBase;
import org.ovirt.engine.core.common.businessentities.LUN_storage_server_connection_map;
import org.ovirt.engine.core.common.businessentities.LUN_storage_server_connection_map_id;
import org.ovirt.engine.core.common.businessentities.LUNs;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMapId;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMap;
import org.ovirt.engine.core.common.businessentities.storage_server_connections;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.BaseDiskDao;
import org.ovirt.engine.core.dao.DiskImageDAO;
import org.ovirt.engine.core.dao.DiskImageDynamicDAO;
import org.ovirt.engine.core.dao.ImageDao;
import org.ovirt.engine.core.dao.ImageStorageDomainMapDao;
import org.ovirt.engine.core.dao.SnapshotDao;
import org.ovirt.engine.core.dao.LunDAO;
import org.ovirt.engine.core.dao.StorageDomainStaticDAO;
import org.ovirt.engine.core.dao.StoragePoolIsoMapDAO;
import org.ovirt.engine.core.dao.StorageServerConnectionDAO;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.linq.Predicate;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@SuppressWarnings("serial")
public abstract class StorageDomainCommandBase<T extends StorageDomainParametersBase> extends
        StorageHandlingCommandBase<T> {

    private storage_pool _storagePool;

    public StorageDomainCommandBase(T parameters) {
        super(parameters);
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
    public NGuid getStorageDomainId() {
        return getParameters() != null ? !Guid.Empty.equals(getParameters().getStorageDomainId()) ? getParameters()
                .getStorageDomainId() : super.getStorageDomainId() : super.getStorageDomainId();
    }

    protected boolean canDetachDomain(boolean isDestroyStoragePool, boolean isRemoveLast, boolean isInternal) {
        return checkStoragePool()
                && checkStorageDomain()
                && checkStorageDomainStatus(StorageDomainStatus.InActive, StorageDomainStatus.Maintenance)
                && (isMaster() || isDestroyStoragePool || checkMasterDomainIsUp())
                && isNotLocalData(isInternal)
                && isDetachAllowed(isRemoveLast);
    }

    protected boolean isDetachAllowed(final boolean isRemoveLast) {
        boolean returnValue = true;
        if (getStoragePoolIsoMap() == null) {
            returnValue = false;
            addCanDoActionMessage(VdcBllMessages.STORAGE_DOMAIN_NOT_ATTACHED_TO_STORAGE_POOL);
        } else if (hasImages()) {
            returnValue = false;
            addCanDoActionMessage(VdcBllMessages.ERROR_CANNOT_DETACH_STORAGE_DOMAIN_WITH_IMAGES);
        } else if (!isRemoveLast
                && isMaster()) {

            storage_domains storage_domains =
                    LinqUtils.firstOrNull(getStorageDomainDAO().getAllForStoragePool
                            (getStorageDomain().getstorage_pool_id().getValue()),
                            new Predicate<storage_domains>() {
                                @Override
                                public boolean eval(storage_domains a) {
                                    return a.getId().equals(getStorageDomain().getId())
                                            && a.getstatus() == StorageDomainStatus.Active;
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
        if (this.getStoragePool().getstorage_pool_type() == StorageType.LOCALFS
                && getStorageDomain().getstorage_domain_type() == StorageDomainType.Data
                && !isInternal) {
            returnValue = false;
            addCanDoActionMessage(VdcBllMessages.VDS_GROUP_CANNOT_DETACH_DATA_DOMAIN_FROM_LOCAL_STORAGE);
        }
        return returnValue;
    }

    private boolean hasImages() {
        return getDiskImageDao()
                .getAllSnapshotsForStorageDomain(getStorageDomain().getId())
                .size() != 0
                || getImageStorageDomainMapDao().getAllByStorageDomainId(getStorageDomain().getId()).size() != 0;
    }

    private StoragePoolIsoMap getStoragePoolIsoMap() {
        return getStoragePoolIsoMapDAO()
                .get(new StoragePoolIsoMapId(getStorageDomain().getId(),
                        getStoragePoolId()));
    }

    private boolean isMaster() {
        return getStorageDomain().getstorage_domain_type() == StorageDomainType.Master;
    }

    @Override
    public storage_pool getStoragePool() {
        if (_storagePool == null) {
            if (getStoragePoolId() != null && !getStoragePoolId().equals(Guid.Empty)) {
                _storagePool = getStoragePoolDAO().get(getStoragePoolId().getValue());
            }
        }
        return _storagePool;
    }

    @Override
    protected boolean canDoAction() {
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__STORAGE__DOMAIN);
        return super.canDoAction();
    }

    protected boolean checkStorageDomainNameLengthValid() {
        boolean result = true;
        if (getStorageDomain().getstorage_name().length() > Config
                .<Integer> GetValue(ConfigValues.StorageDomainNameSizeLimit)) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_NAME_LENGTH_IS_TOO_LONG);
            result = false;
        }
        return result;
    }

    protected boolean checkStorageDomain() {
        return isStorageDomainNotNull(getStorageDomain());
    }

    protected boolean checkStorageDomainInDb() {
        return getStorageDomainStaticDAO().get(getStorageDomain().getId()) != null;
    }

    protected boolean checkStorageDomainStatus(final StorageDomainStatus... statuses) {
        boolean valid = false;
        if (getStorageDomainStatus() != null) {
            valid = Arrays.asList(statuses).contains(getStorageDomainStatus());
        }
        if (!valid) {
            addStorageDomainStatusIllegalMessage();
        }
        return valid;
    }

    protected boolean checkStorageDomainStatusNotEqual(StorageDomainStatus status) {
        boolean returnValue = false;
        if (getStorageDomain() != null && getStorageDomain().getstatus() != null) {
            returnValue = (getStorageDomain().getstatus() != status);
            if (!returnValue) {
                addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_DOMAIN_STATUS_ILLEGAL2.toString());
                addCanDoActionMessage(String.format("$status %1$s", getStorageDomain().getstatus()));
            }
        }
        return returnValue;
    }

    protected boolean checkStorageDomainNotInPool() {
        return isStorageDomainNotInPool(getStorageDomain());
    }

    protected boolean checkStorageConnection(String storageDomainConnection) {
        if (getStorageServerConnectionDAO().get(storageDomainConnection) == null) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_CONNECTION_NOT_EXIST);
        }
        return true;
    }

    protected boolean checkMasterDomainIsUp() {
        boolean returnValue = true;
        List<storage_domains> storageDomains = getStorageDomainDAO().getAllForStoragePool(getStoragePool().getId());
        storageDomains = LinqUtils.filter(storageDomains, new Predicate<storage_domains>() {
            @Override
            public boolean eval(storage_domains a) {
                return a.getstorage_domain_type() == StorageDomainType.Master
                        && a.getstatus() == StorageDomainStatus.Active;
            }
        });
        if (storageDomains.isEmpty()) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_MASTER_STORAGE_DOMAIN_NOT_ACTIVE);
            returnValue = false;
        }
        return returnValue;
    }

    protected void setStorageDomainStatus(StorageDomainStatus status, CompensationContext context) {
        if (getStorageDomain() != null && getStorageDomain().getstorage_pool_id() != null) {
            StoragePoolIsoMap map = getStorageDomain().getStoragePoolIsoMapData();
            if(context != null) {
                context.snapshotEntityStatus(map, map.getstatus());
            }
            getStorageDomain().setstatus(status);
            getStoragePoolIsoMapDAO().updateStatus(map.getId(), status);
        }
    }

    public static void proceedLUNInDb(final LUNs lun, StorageType storageType) {
        proceedLUNInDb(lun,storageType,"");
    }

    protected static LunDAO getLunDao() {
        return DbFacade.getInstance().getLunDao();
    }

    public static void proceedLUNInDb(final LUNs lun, StorageType storageType, String volumeGroupId) {
        if (getLunDao().get(lun.getLUN_id()) == null) {
            getLunDao().save(lun);
        } else if (!volumeGroupId.isEmpty()){
            getLunDao().updateLUNsVolumeGroupId(lun.getLUN_id(), volumeGroupId);
        }

        for (storage_server_connections connection : lun.getLunConnections()) {
            List<storage_server_connections> connections = DbFacade.getInstance()
                    .getStorageServerConnectionDao().getAllForConnection(connection);
            if (connections.isEmpty()) {
                connection.setid(Guid.NewGuid().toString());
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

    protected void connectAllHostsToPool() {
        runSynchronizeOperation(new ConnectSingleAsyncOperationFactory());
    }

    protected void diconnectAllHostsInPool() {
        runSynchronizeOperation(new RefreshStoragePoolAndDisconnectAsyncOperationFactory());
    }

    /**
     *  The new master must  be a data domain which is in Active status and not
     * reported by any vdsm as problematic. In case that all domains reported as problematic a first Active data domain
     * will be returned
     * @return an elected master domain or null
     */
    protected storage_domains electNewMaster(boolean duringReconstruct) {
        storage_domains newMaster = null;
        if (getStoragePool() != null) {
            List<storage_domains> storageDomains = getStorageDomainDAO().getAllForStoragePool(getStoragePool().getId());
            Collections.sort(storageDomains, LastTimeUsedAsMasterComp.instance);
            if (storageDomains.size() > 0) {
                storage_domains storageDomain = getStorageDomain();
                for (storage_domains dbStorageDomain : storageDomains) {
                    if ((storageDomain == null || (duringReconstruct || !dbStorageDomain.getId()
                            .equals(storageDomain.getId())))
                            && (dbStorageDomain.getstatus() == StorageDomainStatus.Active || dbStorageDomain.getstatus() == StorageDomainStatus.Unknown)
                            && dbStorageDomain.getstorage_domain_type() == StorageDomainType.Data) {
                        newMaster = dbStorageDomain;
                        break;
                    }
                }
            }
        }
        return newMaster;
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
                context.snapshotEntityStatus(map, map.getstatus());
                map.setstatus(status);
                getStoragePoolIsoMapDAO().updateStatus(map.getId(), map.getstatus());
                getCompensationContext().stateChanged();
                return null;
            }
        });
    }

    private StorageDomainStatus getStorageDomainStatus() {
        StorageDomainStatus status = null;
        if (getStorageDomain() != null) {
            status = getStorageDomain().getstatus();
        }
        return status;
    }

    private void addStorageDomainStatusIllegalMessage() {
        addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_DOMAIN_STATUS_ILLEGAL);
    }

    protected StoragePoolIsoMapDAO getStoragePoolIsoMapDAO() {
        return getDbFacade().getStoragePoolIsoMapDao();
    }

    protected BaseDiskDao getBaseDiskDao() {
        return getDbFacade().getBaseDiskDao();
    }

    protected ImageDao getImageDao() {
        return getDbFacade().getImageDao();
    }

    protected DiskImageDAO getDiskImageDao() {
        return getDbFacade().getDiskImageDao();
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

    protected StorageDomainStaticDAO getStorageDomainStaticDAO() {
        return getDbFacade().getStorageDomainStaticDao();
    }

    protected StorageServerConnectionDAO getStorageServerConnectionDAO() {
        return getDbFacade().getStorageServerConnectionDao();
    }

    protected IStorageHelper getStorageHelper(storage_domains storageDomain) {
        return StorageHelperDirector.getInstance().getItem(storageDomain.getstorage_type());
    }

    protected void executeInNewTransaction(TransactionMethod<?> method) {
        TransactionSupport.executeInNewTransaction(method);
    }

    private static final class LastTimeUsedAsMasterComp implements Comparator<storage_domains> {
        public static final LastTimeUsedAsMasterComp instance = new LastTimeUsedAsMasterComp();

        @Override
        public int compare(storage_domains o1, storage_domains o2) {
            // TODO: When moving to JDK7 - this can be replaced with
            // Long.compare(o1.getLastTimeUsedAsMaster(),o2.getLastTimeUsedAsMaster());
            return (o1.getLastTimeUsedAsMaster() < o2.getLastTimeUsedAsMaster()) ? -1
                    : ((o1.getLastTimeUsedAsMaster() == o2.getLastTimeUsedAsMaster()) ? 0 : 1);
        }
    }
}
