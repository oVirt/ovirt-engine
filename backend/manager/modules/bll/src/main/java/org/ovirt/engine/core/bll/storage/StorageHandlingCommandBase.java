package org.ovirt.engine.core.bll.storage;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ovirt.engine.core.bll.Backend;
import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.StoragePoolParametersBase;
import org.ovirt.engine.core.common.businessentities.IVdcQueryable;
import org.ovirt.engine.core.common.businessentities.StorageDomainSharedStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StorageFormatType;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.SearchParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.TransactionScopeOption;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.SyncronizeNumberOfAsyncOperations;
import org.ovirt.engine.core.utils.linq.All;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.linq.Predicate;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

public abstract class StorageHandlingCommandBase<T extends StoragePoolParametersBase> extends CommandBase<T> {
    public StorageHandlingCommandBase(T parameters) {
        super(parameters);
        if (getParameters() != null && !getParameters().getStoragePoolId().equals(Guid.Empty)) {
            setStoragePoolId(getParameters().getStoragePoolId());
        }
    }

    /**
     * Constructor for command creation when compensation is applied on startup
     *
     * @param commandId
     */

    protected StorageHandlingCommandBase(Guid commandId) {
        super(commandId);
    }

    public static final String UpVdssInStoragePoolQuery = "HOST: status = UP and DATACENTER = {0}";
    public static final String UpVdssInCluster = "HOST: status = UP and CLUSTER = {0}";
    public static final String DesktopsInStoragePoolQuery = "VMS: DATACENTER = {0}";

    public static List<VDS> GetAllRunningVdssInPool(storage_pool pool) {
        java.util.ArrayList<VDS> returnValue = new java.util.ArrayList<VDS>();

        SearchParameters p = new SearchParameters(MessageFormat.format(UpVdssInStoragePoolQuery, pool.getname()),
                SearchType.VDS);
        p.setMaxCount(Integer.MAX_VALUE);

        Iterable<IVdcQueryable> fromVds = (Iterable<IVdcQueryable>) (Backend.getInstance().runInternalQuery(
                VdcQueryType.Search, p).getReturnValue());
        if (fromVds != null) {
            for (IVdcQueryable vds : fromVds) {
                if (vds instanceof VDS) {
                    returnValue.add((VDS) vds);
                }
            }
        }
        return returnValue;
    }

    protected void updateStoragePoolInDiffTransaction() {
        TransactionSupport.executeInScope(TransactionScopeOption.Suppress, new TransactionMethod<Object>() {
            @Override
            public Object runInTransaction() {
                DbFacade.getInstance().getStoragePoolDAO().update(getStoragePool());
                return null;
            }
        });
    }


    protected List<VDS> getAllRunningVdssInPool() {
        return GetAllRunningVdssInPool(getStoragePool());
    }

    protected Guid getMasterDomainIdFromDb() {
        if (getStoragePool() != null) {
            return DbFacade.getInstance()
                    .getStorageDomainDAO()
                    .getMasterStorageDomainIdForPool(getStoragePool().getId());
        } else {
            return Guid.Empty;
        }
    }

    protected boolean InitializeVds() {
        boolean returnValue = true;
        if (getVds() == null) {
            SearchParameters p = new SearchParameters(MessageFormat.format(UpVdssInStoragePoolQuery, getStoragePool()
                    .getname()), SearchType.VDS);
            p.setMaxCount(Integer.MAX_VALUE);
            Object tempVar = LinqUtils.firstOrNull((List) Backend.getInstance()
                    .runInternalQuery(VdcQueryType.Search, p).getReturnValue(), new All());
            setVds(((VDS) ((tempVar instanceof VDS) ? tempVar : null)));
            if (getVds() == null) {
                returnValue = false;
                addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_NO_VDS_IN_POOL);
            }
        }
        return returnValue;
    }

    protected boolean CheckStoragePool() {
        boolean returnValue = false;
        if (getStoragePool() != null) {
            returnValue = DbFacade.getInstance().getStoragePoolDAO().get(getStoragePool().getId()) != null;
        }
        if (!returnValue) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_POOL_NOT_EXIST);
        }
        return returnValue;
    }

    protected boolean CheckStoragePoolStatus(StoragePoolStatus status) {
        boolean returnValue = false;
        if (getStoragePool() != null) {
            storage_pool storagePool = DbFacade.getInstance().getStoragePoolDAO().get(getStoragePool().getId());
            returnValue = (storagePool.getstatus() == status);
            if (!returnValue
                    && !getReturnValue().getCanDoActionMessages().contains(
                            VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_POOL_STATUS_ILLEGAL.toString())) {
                addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_POOL_STATUS_ILLEGAL);
            }
        }
        return returnValue;
    }

    protected boolean CheckStoragePoolStatusNotEqual(StoragePoolStatus status, VdcBllMessages onFailMessage) {
        boolean returnValue = false;
        if (getStoragePool() != null) {
            storage_pool storagePool = DbFacade.getInstance().getStoragePoolDAO().get(getStoragePool().getId());
            returnValue = (storagePool.getstatus() != status);
            if (!returnValue
                    && !getReturnValue().getCanDoActionMessages().contains(onFailMessage.name())) {
                addCanDoActionMessage(onFailMessage);
            }
        }
        return returnValue;
    }

    protected boolean isStorageDomainTypeCorrect(storage_domains storageDomain) {
        if (storageDomain.getstorage_domain_type() != StorageDomainType.ISO
                && storageDomain.getstorage_domain_type() != StorageDomainType.ImportExport
                && getStoragePool().getstorage_pool_type() != storageDomain.getstorage_type()) {
            addCanDoActionMessage(VdcBllMessages.ERROR_CANNOT_ATTACH_STORAGE_DOMAIN_STORAGE_TYPE_NOT_MATCH);
            return false;
        }
        return true;
    }

    protected boolean isStorageDomainNotInPool(storage_domains storageDomain) {
        boolean returnValue = false;
        if (storageDomain != null) {
            // check if there is no pool-domain map
            returnValue =
                    DbFacade.getInstance()
                            .getStoragePoolIsoMapDAO()
                            .getAllForStorage(storageDomain.getid())
                            .size() == 0;
            if (!returnValue) {
                addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_DOMAIN_STATUS_ILLEGAL);
            }
        }
        return returnValue;
    }

    protected boolean checkDomainCanBeAttached(storage_domains storageDomain) {
        return checkStorageDomainType(storageDomain)
                && isStorageDomainFormatCorrectForPool(storageDomain, getStoragePool())
                && checkStorageDomainSharedStatusNotLocked(storageDomain)
                && ((storageDomain.getstorage_domain_type() == StorageDomainType.ISO || storageDomain.getstorage_domain_type() ==
                StorageDomainType.ImportExport) || isStorageDomainNotInPool(storageDomain))
                && isStorageDomainTypeCorrect(storageDomain);
    }

    /**
     * Check that we are not trying to attach more than one ISO or export
     * domain to the same data center.
     */
    protected boolean checkStorageDomainType(final storage_domains storageDomain) {
        // Nothing to check if the storage domain is not an ISO or export:
        final StorageDomainType type = storageDomain.getstorage_domain_type();
        if (type != StorageDomainType.ISO && type != StorageDomainType.ImportExport) {
          return true;
        }

        // Get the number of storage domains of the given type currently attached
        // to the pool:
        int count = LinqUtils.filter(
            DbFacade.getInstance().getStorageDomainDAO().getAllForStoragePool(getStoragePool().getId()),
            new Predicate<storage_domains>() {
                @Override
                public boolean eval(storage_domains a) {
                    return a.getstorage_domain_type() == type;
                }
            }
        ).size();

        // If the count is zero we are okay, we can add a new one:
        if (count == 0) {
            return true;
        }

        // If we are here then we already have at least one storage type of the given type
        // so whe have to prepare a friendy message for the user (see #713160) and fail:
        if (type == StorageDomainType.ISO) {
            addCanDoActionMessage(VdcBllMessages.ERROR_CANNOT_ATTACH_MORE_THAN_ONE_ISO_DOMAIN);
        }
        else if (type == StorageDomainType.ImportExport) {
            addCanDoActionMessage(VdcBllMessages.ERROR_CANNOT_ATTACH_MORE_THAN_ONE_EXPORT_DOMAIN);
        }
        return false;
    }

    protected boolean checkStorageDomainSharedStatusNotLocked(storage_domains storageDomain) {
        boolean returnValue = storageDomain != null
                && storageDomain.getstorage_domain_shared_status() != StorageDomainSharedStatus.Locked;
        if (!returnValue) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_DOMAIN_STATUS_ILLEGAL);
        }
        return returnValue;
    }

    protected boolean isStorageDomainNotNull(storage_domains domain) {
        if (domain == null) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_DOMAIN_NOT_EXIST);
            return false;
        }

        return true;
    }

    protected void CalcStoragePoolStatusByDomainsStatus() {
        List<storage_domains> domains = DbFacade.getInstance().getStorageDomainDAO().getAllForStoragePool(
                getStoragePool().getId());
        // storage_domains masterDomain = null; //LINQ 31899 domains.Where(a =>
        // a.storage_domain_type == StorageDomainType.Master).FirstOrDefault();
        storage_domains masterDomain = LinqUtils.firstOrNull(domains, new Predicate<storage_domains>() {
            @Override
            public boolean eval(storage_domains a) {
                return a.getstorage_domain_type() == StorageDomainType.Master;
            }
        });

        // if no master then Uninitialized
        // if master not active maintenance
        StoragePoolStatus newStatus =
                (masterDomain == null) ? StoragePoolStatus.Uninitialized
                        : (masterDomain.getstatus() != null && masterDomain.getstatus() == StorageDomainStatus.Maintenance) ? StoragePoolStatus.Maintanance
                                : (masterDomain.getstatus() != null && masterDomain.getstatus() == StorageDomainStatus.Active) ? StoragePoolStatus.Up
                                        : StoragePoolStatus.Problematic;
        if (newStatus != getStoragePool().getstatus()) {
            getCompensationContext().snapshotEntity(getStoragePool());
            getStoragePool().setstatus(newStatus);
            storage_pool poolFromDb = DbFacade.getInstance().getStoragePoolDAO().get(getStoragePool().getId());
            if ((getStoragePool().getspm_vds_id() == null && poolFromDb.getspm_vds_id() != null)
                    || (getStoragePool().getspm_vds_id() != null && !getStoragePool().getspm_vds_id().equals(
                            poolFromDb.getspm_vds_id()))) {
                getStoragePool().setspm_vds_id(poolFromDb.getspm_vds_id());
            }
            if (getStoragePool().getstatus() == StoragePoolStatus.Uninitialized) {
                getStoragePool().setspm_vds_id(null);
            }

            TransactionSupport.executeInScope(TransactionScopeOption.Required, new TransactionMethod<storage_pool>() {
                @Override
                public storage_pool runInTransaction() {
                    DbFacade.getInstance().getStoragePoolDAO().update(getStoragePool());
                    return null;
                }
            });
            StoragePoolStatusHandler.PoolStatusChanged(getStoragePool().getId(), getStoragePool().getstatus());
        }
    }

    protected boolean CheckStoragePoolNameLengthValid() {
        boolean result = true;
        if (getStoragePool().getname().length() > Config.<Integer> GetValue(ConfigValues.StoragePoolNameSizeLimit)) {
            result = false;
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_NAME_LENGTH_IS_TOO_LONG);
        }
        return result;
    }

    /**
     * The following method should check if the format of the storage domain allows to it to be attached to the storage
     * pool. At case of failure the false value will be return and appropriate error message will be added to
     * canDoActionMessages
     * @param storageDomain
     *            -the domain object
     * @param storagePool
     *            - the pool object
     * @return
     */
    protected boolean isStorageDomainFormatCorrectForPool(storage_domains storageDomain, storage_pool storagePool) {
        if (storageDomain.getstorage_domain_type() == StorageDomainType.ISO
                || storageDomain.getstorage_domain_type() == StorageDomainType.ImportExport) {
            return true;
        }
        Set<StorageFormatType> supportedFormatsSet = getSupportedStorageFormatSet(storagePool.getcompatibility_version());
        if (supportedFormatsSet.contains(storageDomain.getStorageFormat())) {
            if (storagePool.getStoragePoolFormatType() == null
                    || storagePool.getStoragePoolFormatType() == storageDomain.getStorageFormat()) {
                return true;
            }

        }
        addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_DOMAIN_FORMAT_ILLEGAL);
        getReturnValue().getCanDoActionMessages().add(
                String.format("$storageFormat %1$s", storageDomain.getStorageFormat().toString()));
        return false;
    }

    protected Set<StorageFormatType> getSupportedStorageFormatSet(Version version) {
        String[] supportedFormats =
                Config.<String> GetValue(ConfigValues.SupportedStorageFormats,
                        version.toString()).split("[,]");
        Set<StorageFormatType> supportedFormatsSet = new HashSet<StorageFormatType>();
        for (String supportedFormat : supportedFormats) {
            supportedFormatsSet.add(StorageFormatType.forValue(supportedFormat));
        }
        return supportedFormatsSet;
    }

    protected void runSynchronizeOperation(ActivateDeactivateSingleAsyncOperationFactory factory,
            Object... addionalParams) {
        List<VDS> allRunningVdsInPool = getAllRunningVdssInPool();
        ArrayList parameters = InitAsyncOperationParameters(allRunningVdsInPool);
        if (addionalParams.length > 0) {
            parameters.addAll(Arrays.asList(addionalParams));
        }
        SyncronizeNumberOfAsyncOperations sync = new SyncronizeNumberOfAsyncOperations(allRunningVdsInPool.size(),
                parameters, factory);
        sync.Execute();
    }

    private java.util.ArrayList InitAsyncOperationParameters(List<VDS> allRunningVdsInPool) {
        java.util.ArrayList parameters = new java.util.ArrayList();
        parameters.add(allRunningVdsInPool);
        parameters.add(getStorageDomain());
        parameters.add(getStoragePool());
        return parameters;
    }

    @Override
    public Map<Guid, VdcObjectType> getPermissionCheckSubjects() {
        return Collections.singletonMap(getStoragePoolId() == null ? null : getStoragePoolId().getValue(),
                VdcObjectType.StoragePool);
    }
}
