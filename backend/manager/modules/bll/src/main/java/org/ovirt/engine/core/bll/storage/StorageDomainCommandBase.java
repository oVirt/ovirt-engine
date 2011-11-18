package org.ovirt.engine.core.bll.storage;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.Backend;
import org.ovirt.engine.core.bll.context.CompensationContext;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.SetNonOperationalVdsParameters;
import org.ovirt.engine.core.common.action.StorageDomainParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.LUN_storage_server_connection_map;
import org.ovirt.engine.core.common.businessentities.LUN_storage_server_connection_map_id;
import org.ovirt.engine.core.common.businessentities.LUNs;
import org.ovirt.engine.core.common.businessentities.NonOperationalReason;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMapId;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.common.businessentities.storage_pool_iso_map;
import org.ovirt.engine.core.common.businessentities.storage_server_connections;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;
import org.ovirt.engine.core.compat.TransactionScopeOption;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.linq.Predicate;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;
import org.ovirt.engine.core.vdsbroker.ResourceManager;

public abstract class StorageDomainCommandBase<T extends StorageDomainParametersBase> extends
        StorageHandlingCommandBase<T> {

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
        return getParameters() != null ? !getParameters().getStorageDomainId().equals(Guid.Empty) ? getParameters()
                .getStorageDomainId() : super.getStorageDomainId() : super.getStorageDomainId();
    }

    public boolean IsDomainActive(Guid domainId, NGuid storagePoolId) {
        return IsDomainActive(domainId, storagePoolId, getReturnValue().getCanDoActionMessages());
    }

    public static boolean IsDomainActive(Guid domainId, NGuid storagePoolId, java.util.ArrayList<String> messages) {
        storage_domains storage =
                DbFacade.getInstance().getStorageDomainDAO().getForStoragePool(domainId, storagePoolId);
        if (storage == null) {
            messages.add(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_DOMAIN_NOT_EXIST.toString());
            return false;
        }
        if (storage.getstatus() == null || storage.getstatus() != StorageDomainStatus.Active) {
            messages.add(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_DOMAIN_STATUS_ILLEGAL.toString());
            return false;
        }
        return true;
    }

    protected boolean canDetachDomain(boolean isDestroyStoragePool, boolean isRemoveLast, boolean isInternal) {
        boolean returnValue = CheckStoragePool()
                && CheckStorageDomain()
                && checkStorageDomainStatus(StorageDomainStatus.InActive)
                && (getStorageDomain().getstorage_domain_type() == StorageDomainType.Master
                        || isDestroyStoragePool || CheckMasterDomainIsUp());
        if (returnValue) {
            if (this.getStoragePool().getstorage_pool_type() == StorageType.LOCALFS
                    && getStorageDomain().getstorage_domain_type() == StorageDomainType.Data
                    && !isInternal) {
                returnValue = false;
                addCanDoActionMessage(VdcBllMessages.VDS_GROUP_CANNOT_DETACH_DATA_DOMAIN_FROM_LOCAL_STORAGE);
            }
        }
        if (returnValue) {
            if (DbFacade.getInstance()
                    .getStoragePoolIsoMapDAO()
                    .get(new StoragePoolIsoMapId(getStorageDomain().getid(),
                            getStoragePoolId())) == null) {
                returnValue = false;
                addCanDoActionMessage(VdcBllMessages.STORAGE_DOMAIN_NOT_ATTACHED_TO_STORAGE_POOL);
            } else if (DbFacade.getInstance()
                    .getDiskImageDAO()
                    .getAllSnapshotsForStorageDomain(getStorageDomain().getid())
                    .size() != 0
                    || DbFacade.getInstance()
                            .getStorageDomainDAO()
                            .getAllImageGroupStorageDomainMapsForStorageDomain(getStorageDomain().getid())
                            .size() != 0) {
                returnValue = false;
                addCanDoActionMessage(VdcBllMessages.ERROR_CANNOT_DETACH_STORAGE_DOMAIN_WITH_IMAGES);
            } else if (!isRemoveLast
                    && getStorageDomain().getstorage_domain_type() == StorageDomainType.Master) {

                storage_domains storage_domains =
                        LinqUtils.firstOrNull(DbFacade.getInstance()
                                .getStorageDomainDAO()
                                .getAllForStoragePool(getStorageDomain().getstorage_pool_id().getValue()),
                                new Predicate<storage_domains>() {
                                    @Override
                                    public boolean eval(storage_domains a) {
                                        return a.getid().equals(getStorageDomain().getid())
                                                && a.getstatus() == StorageDomainStatus.Active;
                                    }
                                });
                if (storage_domains == null) {
                    returnValue = false;
                    addCanDoActionMessage(VdcBllMessages.ERROR_CANNOT_DETACH_LAST_STORAGE_DOMAIN);
                }
            }
        }
        return returnValue;
    }

    private storage_pool _storagePool;

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

    protected boolean CheckStorageDomainNameLengthValid() {
        boolean result = true;
        if (getStorageDomain().getstorage_name().length() > Config
                .<Integer> GetValue(ConfigValues.StorageDomainNameSizeLimit)) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_NAME_LENGTH_IS_TOO_LONG);
            result = false;
        }
        return result;
    }

    protected boolean CheckStorageDomain() {
        return isStorageDomainNotNull(getStorageDomain());
    }

    protected boolean checkStorageDomainInDb() {
        boolean returnValue;
        returnValue = DbFacade.getInstance().getStorageDomainStaticDAO().get(getStorageDomain().getid()) != null;
        return returnValue;
    }

    protected boolean checkStorageDomainStatus(final StorageDomainStatus... statuses) {
        boolean valid = false;
        if(getStorageDomainStatus() != null) {
            valid = Arrays.asList(statuses).contains(getStorageDomainStatus());
        }
        if(!valid) {
            addStorageDomainStatusIllegalMessage();
        }
        return valid;
    }

    protected boolean CheckStorageDomainStatusNotEqual(StorageDomainStatus status) {
        boolean returnValue = false;
        if (getStorageDomain() != null && getStorageDomain().getstatus() != null) {
            returnValue = (getStorageDomain().getstatus() != status);
            if (!returnValue) {
                getReturnValue().getCanDoActionMessages().add(
                        VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_DOMAIN_STATUS_ILLEGAL2.toString());
            }
        }
        return returnValue;
    }

    protected boolean CheckStorageDomainNotInPool() {
        return isStorageDomainNotInPool(getStorageDomain());
    }

    protected boolean CheckStorageConnection(String storageDomainConnection) {
        boolean returnValue = true;
        if (DbFacade.getInstance().getStorageServerConnectionDAO().get(storageDomainConnection) == null) {
            returnValue = false;
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_CONNECTION_NOT_EXIST);
        }
        return returnValue;
    }

    protected boolean CheckMasterDomainIsUp() {
        boolean returnValue = true;
        // LINQ 29456
        // IEnumerable<storage_domains> storageDomains =
        // DbFacade.Instance.GetStorageDomainsByStoragePoolId(StoragePool.id).
        // Where(a => a.storage_domain_type == StorageDomainType.Master &&
        // a.status == StorageDomainStatus.Active);
        List<storage_domains> storageDomains = DbFacade.getInstance().getStorageDomainDAO().getAllForStoragePool(
                getStoragePool().getId());
        storageDomains = LinqUtils.filter(storageDomains, new Predicate<storage_domains>() {
            @Override
            public boolean eval(storage_domains a) {
                return a.getstorage_domain_type() == StorageDomainType.Master
                        && a.getstatus() == StorageDomainStatus.Active;
            }
        });
        // LINQ 29456
        if (storageDomains.isEmpty()) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_MASTER_STORAGE_DOMAIN_NOT_ACTIVE);
            returnValue = false;
        }
        return returnValue;
    }

    protected void SetStorageDomainStatus(StorageDomainStatus status) {
        if (getStorageDomain() != null && getStorageDomain().getstorage_pool_id() != null) {
            storage_pool_iso_map map = getStorageDomain().getStoragePoolIsoMapData();
            getStorageDomain().setstatus(status);
            DbFacade.getInstance()
                    .getStoragePoolIsoMapDAO()
                    .updateStatus(map.getId(), map.getstatus());
        }
    }

    protected void RefreshAllVdssInPool(boolean connect) {
        java.util.ArrayList<Guid> vdsIdsToSetNonOperational = new java.util.ArrayList<Guid>();
        runSynchronizeOperation(new RefreshPoolSingleAsyncOperationFactory(), vdsIdsToSetNonOperational);
        for (Guid vdsId : vdsIdsToSetNonOperational) {
            SetNonOperationalVdsParameters tempVar = new SetNonOperationalVdsParameters(vdsId,
                    NonOperationalReason.STORAGE_DOMAIN_UNREACHABLE);
            tempVar.setSaveToDb(true);
            tempVar.setStorageDomainId(getStorageDomain().getid());
            tempVar.setTransactionScopeOption(TransactionScopeOption.RequiresNew);
            Backend.getInstance().runInternalAction(VdcActionType.SetNonOperationalVds, tempVar);
        }
    }

    protected void ProceedLUNInDb(final LUNs lun) {
        DbFacade.getInstance().getLunDAO().save(lun);
        for (storage_server_connections connection : lun.getLunConnections()) {
            List<storage_server_connections> connections = DbFacade.getInstance()
                            .getStorageServerConnectionDAO().getAllForConnection(connection);
            if (connections.isEmpty()) {
                connection.setid(Guid.NewGuid().toString());
                connection.setstorage_type(getStorageDomain().getstorage_type());
                DbFacade.getInstance().getStorageServerConnectionDAO().save(connection);

            } else {
                connection.setid(connections.get(0).getid());
            }
            if (DbFacade.getInstance()
                            .getStorageServerConnectionLunMapDAO()
                            .get(new LUN_storage_server_connection_map_id(lun.getLUN_id(),
                                    connection.getid())) == null) {
                DbFacade.getInstance().getStorageServerConnectionLunMapDAO().save(
                                new LUN_storage_server_connection_map(lun.getLUN_id(), connection.getid()));
            }
        }
    }

    protected void ConnectAllHostsToPool() {
        runSynchronizeOperation(new ConnectSingleAsyncOperationFactory());
    }

    protected void DiconnectAllHostsInPool() {
        runSynchronizeOperation(new RefreshStoragePoolAndDisconnectAsyncOperationFactory());
    }

    /**
     * The following method will perform election for the new master: data domain which is in Active status and not
     * reported by any vdsm as problematic. In case that all domains reported as problematic a first Active data domain
     * will be returned
     * @return an elected master domain or null
     */
    protected storage_domains electNewMaster() {
        storage_domains newMaster = null;
        if (getStoragePool() != null) {
            List<storage_domains> storageDomains = DbFacade.getInstance()
                    .getStorageDomainDAO().getAllForStoragePool(getStoragePool().getId());
            if (storageDomains.size() > 0) {
                storage_domains storageDomain = getStorageDomain();
                for (storage_domains dbStorageDomain : storageDomains) {
                    if ((storageDomain == null || !dbStorageDomain.getid().equals(storageDomain.getid()))
                            && (dbStorageDomain.getstatus() == StorageDomainStatus.Active || dbStorageDomain.getstatus() == StorageDomainStatus.Unknown)
                            && dbStorageDomain.getstorage_domain_type() == StorageDomainType.Data) {
                        if (!ResourceManager.getInstance().isDomainReportedInProblem(getStoragePool().getId(),
                                dbStorageDomain.getid())) {
                            newMaster = dbStorageDomain;
                            break;
                        } else if (newMaster == null) {
                            newMaster = dbStorageDomain;
                        }
                    }
                }
            }
        }
        return newMaster;
    }

    @Override
    public Map<Guid, VdcObjectType> getPermissionCheckSubjects() {
        return Collections.singletonMap(getParameters().getStorageDomainId(), VdcObjectType.Storage);
    }

    protected void changeStorageDomainStatusInTransaction(final storage_pool_iso_map map,
            final StorageDomainStatus status) {
        TransactionSupport.executeInNewTransaction(new TransactionMethod<storage_pool_iso_map>() {
            @Override
            public storage_pool_iso_map runInTransaction() {
                CompensationContext context = getCompensationContext();
                context.snapshotEntityStatus(map, map.getstatus());
                map.setstatus(status);
                DbFacade.getInstance().getStoragePoolIsoMapDAO().updateStatus(map.getId(), map.getstatus());
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
}
