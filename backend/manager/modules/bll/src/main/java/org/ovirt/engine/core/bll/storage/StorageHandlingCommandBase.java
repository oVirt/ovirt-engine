package org.ovirt.engine.core.bll.storage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.StoragePoolParametersBase;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainSharedStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StorageFormatType;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.TransactionScopeOption;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.RandomUtils;
import org.ovirt.engine.core.utils.SyncronizeNumberOfAsyncOperations;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.linq.Predicate;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

public abstract class StorageHandlingCommandBase<T extends StoragePoolParametersBase> extends CommandBase<T> {
    public StorageHandlingCommandBase(T parameters) {
        super(parameters);
        setVdsId(parameters.getVdsId());
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

    public static List<VDS> GetAllRunningVdssInPool(StoragePool pool) {
        return DbFacade.getInstance().getVdsDao().getAllForStoragePoolAndStatus(pool.getId(), VDSStatus.Up);
    }

    protected List<VDS> getAllRunningVdssInPool() {
        return getVdsDAO().getAllForStoragePoolAndStatus(getStoragePool().getId(), VDSStatus.Up);
    }

    protected void updateStoragePoolMasterDomainVersionInDiffTransaction() {
        executeInScope(TransactionScopeOption.Suppress, new TransactionMethod<Void>() {
            @Override
            public Void runInTransaction() {
                int master_domain_version = getStoragePoolDAO().increaseStoragePoolMasterVersion(getStoragePool().getId());
                getStoragePool().setmaster_domain_version(master_domain_version);
                return null;
            }
        });
    }

    protected Guid getMasterDomainIdFromDb() {
        Guid ret = Guid.Empty;
        if (getStoragePool() != null) {
            ret = DbFacade.getInstance()
                    .getStorageDomainDao()
                    .getMasterStorageDomainIdForPool(getStoragePool().getId());
        }

        return ret;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected boolean initializeVds() {
        boolean returnValue = true;
        if (getVds() == null) {
            List<VDS> hosts = getVdsDAO().getAllForStoragePoolAndStatus(getStoragePool().getId(),
                    VDSStatus.Up);
            if (!hosts.isEmpty()) {
                // select random host to avoid executing almost every time through the same one
                // (as the db query will return the hosts in the same order on most times).
                setVds(RandomUtils.instance().pickRandom(hosts));
            }
            if (getVds() == null) {
                returnValue = false;
                addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_NO_VDS_IN_POOL);
            }
        }
        return returnValue;
    }

    protected boolean checkStoragePool() {
        if (getStoragePool() == null) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_POOL_NOT_EXIST);
            return false;
        }
        return true;
    }

    protected boolean CheckStoragePoolStatus(StoragePoolStatus status) {
        boolean returnValue = false;
        StoragePool storagePool = getStoragePool();
        if (storagePool != null) {
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
        StoragePool storagePool = getStoragePool();
        if (storagePool != null) {
            returnValue = (storagePool.getstatus() != status);
            if (!returnValue
                    && !getReturnValue().getCanDoActionMessages().contains(onFailMessage.name())) {
                addCanDoActionMessage(onFailMessage);
            }
        }
        return returnValue;
    }

    protected boolean isStorageDomainTypeCorrect(StorageDomain storageDomain) {
        if (storageDomain.getStorageDomainType() != StorageDomainType.ISO
                && storageDomain.getStorageDomainType() != StorageDomainType.ImportExport
                && getStoragePool().getStorageType() != storageDomain.getStorageType()) {
            addCanDoActionMessage(VdcBllMessages.ERROR_CANNOT_ATTACH_STORAGE_DOMAIN_STORAGE_TYPE_NOT_MATCH);
            return false;
        }
        return true;
    }

    protected boolean isStorageDomainNotInPool(StorageDomain storageDomain) {
        boolean returnValue = false;
        if (storageDomain != null) {
            // check if there is no pool-domain map
            returnValue = getDbFacade().getStoragePoolIsoMapDao().getAllForStorage(storageDomain.getId()).isEmpty();
            if (!returnValue) {
                addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_DOMAIN_STATUS_ILLEGAL);
            }
        }
        return returnValue;
    }

    protected boolean checkDomainCanBeAttached(StorageDomain storageDomain) {
        return checkStorageDomainType(storageDomain)
                && isStorageDomainFormatCorrectForPool(storageDomain, getStoragePool())
                && checkStorageDomainSharedStatusNotLocked(storageDomain)
                && ((storageDomain.getStorageDomainType() == StorageDomainType.ISO || storageDomain.getStorageDomainType() ==
                StorageDomainType.ImportExport) || isStorageDomainNotInPool(storageDomain))
                && isStorageDomainTypeCorrect(storageDomain);
    }

    /**
     * Check that we are not trying to attach more than one ISO or export
     * domain to the same data center.
     */
    protected boolean checkStorageDomainType(final StorageDomain storageDomain) {
        // Nothing to check if the storage domain is not an ISO or export:
        final StorageDomainType type = storageDomain.getStorageDomainType();
        if (type != StorageDomainType.ISO && type != StorageDomainType.ImportExport) {
            return true;
        }

        // Get the number of storage domains of the given type currently attached
        // to the pool:
        int count = LinqUtils.filter(
                getStorageDomainDAO().getAllForStoragePool(getStoragePool().getId()),
                new Predicate<StorageDomain>() {
                    @Override
                    public boolean eval(StorageDomain a) {
                        return a.getStorageDomainType() == type;
                    }
                }
                ).size();

        // If the count is zero we are okay, we can add a new one:
        if (count == 0) {
            return true;
        }

        // If we are here then we already have at least one storage type of the given type
        // so when have to prepare a friendly message for the user (see #713160) and fail:
        if (type == StorageDomainType.ISO) {
            addCanDoActionMessage(VdcBllMessages.ERROR_CANNOT_ATTACH_MORE_THAN_ONE_ISO_DOMAIN);
        }
        else if (type == StorageDomainType.ImportExport) {
            addCanDoActionMessage(VdcBllMessages.ERROR_CANNOT_ATTACH_MORE_THAN_ONE_EXPORT_DOMAIN);
        }
        return false;
    }

    protected boolean checkStorageDomainSharedStatusNotLocked(StorageDomain storageDomain) {
        boolean returnValue = storageDomain != null
                && storageDomain.getStorageDomainSharedStatus() != StorageDomainSharedStatus.Locked;
        if (!returnValue) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_DOMAIN_STATUS_ILLEGAL);
        }
        return returnValue;
    }

    protected boolean isStorageDomainNotNull(StorageDomain domain) {
        if (domain == null) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_DOMAIN_NOT_EXIST);
            return false;
        }

        return true;
    }

    protected void calcStoragePoolStatusByDomainsStatus() {
        List<StorageDomain> domains = getStorageDomainDAO().getAllForStoragePool(getStoragePool().getId());

        // set masterDomain to the first element of domains with type=master, or null if non have this type.
        StorageDomain masterDomain = LinqUtils.firstOrNull(domains, new Predicate<StorageDomain>() {
            @Override
            public boolean eval(StorageDomain a) {
                return a.getStorageDomainType() == StorageDomainType.Master;
            }
        });

        // if no master then Uninitialized
        // if master not active maintenance
        StoragePoolStatus newStatus =
                (masterDomain == null) ? StoragePoolStatus.Uninitialized
                        : (masterDomain.getStatus() != null && masterDomain.getStatus() == StorageDomainStatus.Maintenance) ? StoragePoolStatus.Maintenance
                                : (masterDomain.getStatus() != null && masterDomain.getStatus() == StorageDomainStatus.Active) ? StoragePoolStatus.Up
                                        : StoragePoolStatus.NonResponsive;
        if (newStatus != getStoragePool().getstatus()) {
            getCompensationContext().snapshotEntity(getStoragePool());
            getStoragePool().setstatus(newStatus);
            StoragePool poolFromDb = getStoragePoolDAO().get(getStoragePool().getId());
            if ((getStoragePool().getspm_vds_id() == null && poolFromDb.getspm_vds_id() != null)
                    || (getStoragePool().getspm_vds_id() != null && !getStoragePool().getspm_vds_id().equals(
                            poolFromDb.getspm_vds_id()))) {
                getStoragePool().setspm_vds_id(poolFromDb.getspm_vds_id());
            }
            if (getStoragePool().getstatus() == StoragePoolStatus.Uninitialized) {
                getStoragePool().setspm_vds_id(null);
            }

            executeInScope(TransactionScopeOption.Required, new TransactionMethod<StoragePool>() {
                @Override
                public StoragePool runInTransaction() {
                    getStoragePoolDAO().update(getStoragePool());
                    return null;
                }
            });
            StoragePoolStatusHandler.PoolStatusChanged(getStoragePool().getId(), getStoragePool().getstatus());
        }
    }

    protected boolean checkStoragePoolNameLengthValid() {
        boolean result = true;
        if (getStoragePool().getName().length() > getStoragePoolNameSizeLimit()) {
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
    protected boolean isStorageDomainFormatCorrectForPool(StorageDomain storageDomain, StoragePool storagePool) {
        if (storageDomain.getStorageDomainType() == StorageDomainType.ISO
                || storageDomain.getStorageDomainType() == StorageDomainType.ImportExport) {
            return true;
        }
        Set<StorageFormatType> supportedFormatsSet =
                getSupportedStorageFormatSet(storagePool.getcompatibility_version());
        if (supportedFormatsSet.contains(storageDomain.getStorageFormat())) {
            return true;
        }
        addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_DOMAIN_FORMAT_ILLEGAL);
        getReturnValue().getCanDoActionMessages().add(
                String.format("$storageFormat %1$s", storageDomain.getStorageFormat().toString()));
        return false;
    }

    protected Set<StorageFormatType> getSupportedStorageFormatSet(Version version) {
        String[] supportedFormats = getSupportedStorageFormats(version).split("[,]");
        Set<StorageFormatType> supportedFormatsSet = new HashSet<StorageFormatType>();
        for (String supportedFormat : supportedFormats) {
            supportedFormatsSet.add(StorageFormatType.forValue(supportedFormat));
        }
        return supportedFormatsSet;
    }

    protected void runSynchronizeOperation(ActivateDeactivateSingleAsyncOperationFactory factory,
            Object... addionalParams) {
        List<VDS> allRunningVdsInPool = getAllRunningVdssInPool();
        ArrayList<Object> parameters = initAsyncOperationParameters(allRunningVdsInPool);
        if (addionalParams.length > 0) {
            parameters.addAll(Arrays.asList(addionalParams));
        }
        SyncronizeNumberOfAsyncOperations sync = new SyncronizeNumberOfAsyncOperations(allRunningVdsInPool.size(),
                parameters, factory);
        sync.Execute();
    }

    private ArrayList<Object> initAsyncOperationParameters(List<VDS> allRunningVdsInPool) {
        ArrayList<Object> parameters = new ArrayList<Object>();
        parameters.add(allRunningVdsInPool);
        parameters.add(getStorageDomain());
        parameters.add(getStoragePool());
        return parameters;
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return Collections.singletonList(new PermissionSubject(getStoragePoolId(),
                VdcObjectType.StoragePool, getActionType().getActionGroup()));
    }

    @Override
    public Map<String, String> getJobMessageProperties() {
        if (jobProperties == null) {
            jobProperties = super.getJobMessageProperties();
            jobProperties.put(VdcObjectType.StoragePool.name().toLowerCase(), getStoragePoolName());
            jobProperties.put(VdcObjectType.Storage.name().toLowerCase(), getStorageDomainName());
            jobProperties.put(VdcObjectType.VDS.name().toLowerCase(), getVdsName());
        }
        return jobProperties;
    }

    /* Config methods - for easier testing */

    /** @return The maximum length for a storage pool's name, from the configuration. */
    protected Integer getStoragePoolNameSizeLimit() {
        return Config.<Integer> GetValue(ConfigValues.StoragePoolNameSizeLimit);
    }

    /** @return The supported storage domain formats, delimited by commas (","). */
    protected String getSupportedStorageFormats(Version version) {
        return Config.<String> GetValue(ConfigValues.SupportedStorageFormats, version.toString());
    }

    /* Overidden DAO access methods, for easier testing */

    @Override
    public BackendInternal getBackend() {
        return super.getBackend();
    }

    @Override
    protected DbFacade getDbFacade() {
        return super.getDbFacade();
    }

    /* Transaction methods */

    protected void executeInScope(TransactionScopeOption scope, TransactionMethod<?> code) {
        TransactionSupport.executeInScope(scope, code);
    }
}
