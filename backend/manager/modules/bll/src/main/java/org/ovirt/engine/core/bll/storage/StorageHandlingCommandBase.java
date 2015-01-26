package org.ovirt.engine.core.bll.storage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.bll.snapshots.SnapshotsValidator;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.StoragePoolParametersBase;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.OvfEntityData;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainSharedStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StorageFormatType;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmEntityType;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.utils.VersionStorageFormatUtil;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSParametersBase;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.TransactionScopeOption;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.DiskImageDAO;
import org.ovirt.engine.core.dao.StorageDomainDynamicDAO;
import org.ovirt.engine.core.dao.StoragePoolIsoMapDAO;
import org.ovirt.engine.core.dao.UnregisteredOVFDataDAO;
import org.ovirt.engine.core.utils.RandomUtils;
import org.ovirt.engine.core.utils.SyncronizeNumberOfAsyncOperations;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.linq.Predicate;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

public abstract class StorageHandlingCommandBase<T extends StoragePoolParametersBase> extends CommandBase<T> {
    private List<DiskImage> diskImagesForStorageDomain;

    protected StorageHandlingCommandBase(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
        init(parameters);

    }

    protected StorageHandlingCommandBase(T parameters) {
        this(parameters, null);
    }


    protected void init(T parameters) {
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

    public static List<VDS> getAllRunningVdssInPool(StoragePool pool) {
        return DbFacade.getInstance().getVdsDao().getAllForStoragePoolAndStatus(pool.getId(), VDSStatus.Up);
    }

    protected int getAmountOfVdssInPool() {
        return getVdsDAO().getAllForStoragePool(getStoragePool().getId()).size();
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

    protected List<DiskImage> getDiskImagesForStorageDomain(Guid storageDomainId) {
        if (diskImagesForStorageDomain == null) {
            diskImagesForStorageDomain = getDiskImageDao().getAllForStorageDomain(storageDomainId);
        }
        return diskImagesForStorageDomain;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected boolean initializeVds() {
        boolean returnValue = true;
        if (getVds() == null) {
            // select random host to avoid executing almost every time through the same one
            // (as the db query will return the hosts in the same order on most times).
            setVds(checkForActiveVds());
            if (getVds() == null) {
                returnValue = false;
            }
        }
        return returnValue;
    }

    protected VDS checkForActiveVds() {
        List<VDS> hosts = getVdsDAO().getAllForStoragePoolAndStatus(getStoragePool().getId(),
                VDSStatus.Up);
        if (!hosts.isEmpty()) {
            return RandomUtils.instance().pickRandom(hosts);
        }
        addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_NO_VDS_IN_POOL);
        return null;
    }

    protected boolean checkStoragePool() {
        if (getStoragePool() == null) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_POOL_NOT_EXIST);
            return false;
        }
        return true;
    }

    protected boolean canDetachStorageDomainWithVmsAndDisks(StorageDomain storageDomain) {
        if (!storageDomain.getStorageDomainType().isDataDomain()) {
            return true;
        }

        SnapshotsValidator snapshotsValidator = new SnapshotsValidator();
        List<String> vmsInPreview = new ArrayList<>();
        List<VM> vmRelatedToDomain = getVmDAO().getAllForStorageDomain(storageDomain.getId());
        for (VM vm : vmRelatedToDomain) {
            if (!snapshotsValidator.vmNotInPreview(vm.getId()).isValid()) {
                vmsInPreview.add(vm.getName());
            }
        }

        List<VM> vmsWithDisksOnMultipleStorageDomain = getDbFacade().getVmDao().getAllVMsWithDisksOnOtherStorageDomain(storageDomain.getId());
        vmRelatedToDomain.removeAll(vmsWithDisksOnMultipleStorageDomain);
        List<String> entitiesDeleteProtected = new ArrayList<>();
        List<String> vmsInPool = new ArrayList<>();
        for (VM vm : vmRelatedToDomain) {
            if (vm.isDeleteProtected()) {
                entitiesDeleteProtected.add(vm.getName());
            }
            if (vm.getVmPoolId() != null) {
                vmsInPool.add(vm.getName());
            }
        }

        List<VmTemplate> templatesRelatedToDomain = getVmTemplateDAO().getAllForStorageDomain(storageDomain.getId());
        List<VmTemplate> vmTemplatesWithDisksOnMultipleStorageDomain =
                getVmTemplateDAO().getAllTemplatesWithDisksOnOtherStorageDomain(storageDomain.getId());
        templatesRelatedToDomain.removeAll(vmTemplatesWithDisksOnMultipleStorageDomain);

        for (VmTemplate vmTemplate : templatesRelatedToDomain) {
            if (vmTemplate.isDeleteProtected()) {
                entitiesDeleteProtected.add(vmTemplate.getName());
            }
        }

        boolean succeeded = true;
        if (!entitiesDeleteProtected.isEmpty()) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_DELETE_PROTECTED);
            addCanDoActionMessageVariable("vms", StringUtils.join(entitiesDeleteProtected, ","));
            succeeded = false;
        }
        if (!vmsInPool.isEmpty()) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_VMS_IN_POOL);
            addCanDoActionMessageVariable("vms", StringUtils.join(vmsInPool, ","));
            succeeded = false;
        }
        if (!vmsInPreview.isEmpty()) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_DELETE_VMS_IN_PREVIEW);
            addCanDoActionMessageVariable("vms", StringUtils.join(vmsInPreview, ","));
            succeeded = false;
        }
        return succeeded;
    }

    protected void detachStorageDomainWithEntities(StorageDomain storageDomain) {
        // Check if we have entities related to the Storage Domain.
        List<VM> vmsForStorageDomain = getVmDAO().getAllForStorageDomain(storageDomain.getId());
        List<VmTemplate> vmTemplatesForStorageDomain = getVmTemplateDAO().getAllForStorageDomain(storageDomain.getId());
        List<DiskImage> disksForStorageDomain = getDiskImageDao().getAllForStorageDomain(storageDomain.getId());
        removeEntitiesFromStorageDomain(vmsForStorageDomain, vmTemplatesForStorageDomain, disksForStorageDomain, storageDomain.getId());
    }

    private void removeEntityLeftOver(Guid entityId, String entityName, Guid storageDomainId) {
        List<OvfEntityData> ovfEntityList = getUnregisteredOVFDataDao().getByEntityIdAndStorageDomain(entityId, storageDomainId);
        if (!ovfEntityList.isEmpty()) {
            log.infoFormat("Entity {0} with id {1}, already exists as unregistered entity. override it with the new entity from the engine",
                    entityName,
                    entityId);
            getUnregisteredOVFDataDao().removeEntity(entityId, storageDomainId);
        }
    }

    /**
     * Remove all related entities of the Storage Domain from the DB.
     */
    private void removeEntitiesFromStorageDomain(final List<VM> vmsForStorageDomain,
            final List<VmTemplate> vmTemplatesForStorageDomain,
            final List<DiskImage> disksForStorageDomain,
            final Guid storageDomainId) {
        if (!vmsForStorageDomain.isEmpty() || !vmTemplatesForStorageDomain.isEmpty() || !disksForStorageDomain.isEmpty()) {
            TransactionSupport.executeInNewTransaction(new TransactionMethod<Object>() {
                @Override
                public Object runInTransaction() {
                    for (VM vm : vmsForStorageDomain) {
                        removeEntityLeftOver(vm.getId(), vm.getName(), storageDomainId);
                        getUnregisteredOVFDataDao().saveOVFData(new OvfEntityData(
                                vm.getId(),
                                vm.getName(),
                                VmEntityType.VM,
                                vm.getClusterArch(),
                                vm.getVdsGroupCompatibilityVersion(),
                                storageDomainId,
                                null,
                                null));
                    }

                    for (VmTemplate vmTemplate : vmTemplatesForStorageDomain) {
                        removeEntityLeftOver(vmTemplate.getId(), vmTemplate.getName(), storageDomainId);
                        getUnregisteredOVFDataDao().saveOVFData(new OvfEntityData(
                                vmTemplate.getId(),
                                vmTemplate.getName(),
                                VmEntityType.TEMPLATE,
                                vmTemplate.getClusterArch(),
                                getVdsGroupDAO().get(vmTemplate.getVdsGroupId()).getcompatibility_version(),
                                storageDomainId,
                                null,
                                null));
                    }
                    getStorageDomainDAO().removeEntitesFromStorageDomain(storageDomainId);
                    return null;
                }
            });
        }
    }

    protected UnregisteredOVFDataDAO getUnregisteredOVFDataDao() {
        return getDbFacade().getUnregisteredOVFDataDao();
    }

    protected boolean checkStoragePoolStatus(StoragePoolStatus status) {
        boolean returnValue = false;
        StoragePool storagePool = getStoragePool();
        if (storagePool != null) {
            returnValue = (storagePool.getStatus() == status);
            if (!returnValue
                    && !getReturnValue().getCanDoActionMessages().contains(
                            VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_POOL_STATUS_ILLEGAL.toString())) {
                addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_POOL_STATUS_ILLEGAL);
            }
        }
        return returnValue;
    }

    protected boolean checkStoragePoolStatusNotEqual(StoragePoolStatus status, VdcBllMessages onFailMessage) {
        boolean returnValue = false;
        StoragePool storagePool = getStoragePool();
        if (storagePool != null) {
            returnValue = (storagePool.getStatus() != status);
            if (!returnValue
                    && !getReturnValue().getCanDoActionMessages().contains(onFailMessage.name())) {
                addCanDoActionMessage(onFailMessage);
            }
        }
        return returnValue;
    }

    protected boolean isStorageDomainTypeCorrect(StorageDomain storageDomain) {
        if (!isStorageDomainOfTypeIsoOrExport(storageDomain) && storageDomain.isLocal() != getStoragePool().isLocal()) {
            addCanDoActionMessage(VdcBllMessages.ERROR_CANNOT_ATTACH_STORAGE_DOMAIN_STORAGE_TYPE_NOT_MATCH);
            return false;
        }
        return true;
    }

    protected boolean isStorageDomainNotInPool(StorageDomain storageDomain) {
        boolean returnValue = false;
        if (storageDomain != null) {
            // check if there is no pool-domain map
            returnValue = getStoragePoolIsoMapDAO().getAllForStorage(storageDomain.getId()).isEmpty();
            if (!returnValue) {
                addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_DOMAIN_STATUS_ILLEGAL);
            }
        }
        return returnValue;
    }

    protected ValidationResult isStorageDomainCompatibleWithDC(StorageDomainStatic domainStatic) {
        StoragePoolValidator spv = new StoragePoolValidator(getStoragePool());
        if (domainStatic.getStorageType() == StorageType.GLUSTERFS) {
            return spv.isGlusterSupportedInDC();
        }

        if (domainStatic.getStorageType() == StorageType.POSIXFS) {
            return spv.isPosixSupportedInDC();
        }

        return ValidationResult.VALID;
    }

    protected boolean checkDomainCanBeAttached(StorageDomain storageDomain) {
        if (!validateAmountOfIsoAndExportDomainsInDC(storageDomain)) {
            return false;
        }
        if (!isStorageDomainFormatCorrectForDC(storageDomain.getStorageStaticData(), getStoragePool())) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_DOMAIN_FORMAT_ILLEGAL);
            getReturnValue().getCanDoActionMessages().add(String.format("$storageFormat %1$s", storageDomain.getStorageFormat().toString()));
            return false;
        }
        if (!checkStorageDomainSharedStatusNotLocked(storageDomain)) {
            return false;
        }
        if (!(isStorageDomainOfTypeIsoOrExport(storageDomain) || isStorageDomainNotInPool(storageDomain))) {
            return false;
        }
        if (!isStorageDomainTypeCorrect(storageDomain)) {
            return false;
        }
        if (!validate(isStorageDomainCompatibleWithDC(storageDomain.getStorageStaticData()))) {
            return false;
        }
        if (!isStorageDomainOfTypeIsoOrExport(storageDomain ) && !isMixedTypesAllowedInDC(getStoragePool().getcompatibility_version())
                && isMixedTypeDC(storageDomain.getStorageStaticData())) {
            return false;
        }

        return true;
    }


    // TODO: Should be removed when 3.0 compatibility will not be supported, for now we are blocking the possibility
    // to mix NFS domains with block domains on 3.0 pools since block domains on 3.0 pools can be in V2 format while NFS
    // domains on 3.0 can only be in V1 format
    protected boolean isMixedTypesAllowedInDC(Version version) {
        return FeatureSupported.mixedDomainTypesOnDataCenter(version);
    }

    public boolean isMixedTypeDC(StorageDomainStatic domainStatic) {
        boolean isBlockDomain = domainStatic.getStorageType().isBlockDomain();

        List<StorageType> storageTypesOnPool = getStoragePoolDAO().getStorageTypesInPool(getStoragePoolId());
        for (StorageType storageType : storageTypesOnPool) {
            if (storageType.isBlockDomain() != isBlockDomain) {
                addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_MIXED_STORAGE_TYPES_NOT_ALLOWED);
                return true;
            }
        }
        return false;
    }


    private boolean isStorageDomainOfTypeIsoOrExport(StorageDomain storageDomain) {
        return storageDomain.getStorageDomainType().isIsoOrImportExportDomain();
    }

    /**
     * Check that we are not trying to attach more than one ISO or export
     * domain to the same data center.
     */
    protected boolean validateAmountOfIsoAndExportDomainsInDC(StorageDomain storageDomain) {
        // Nothing to check if the storage domain is not an ISO or export:
        if (!isStorageDomainOfTypeIsoOrExport(storageDomain)) {
            return true;
        }

        final StorageDomainType type = storageDomain.getStorageDomainType();

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
        if (newStatus != getStoragePool().getStatus()) {
            getCompensationContext().snapshotEntity(getStoragePool());
            getStoragePool().setStatus(newStatus);
            StoragePool poolFromDb = getStoragePoolDAO().get(getStoragePool().getId());
            if ((getStoragePool().getspm_vds_id() == null && poolFromDb.getspm_vds_id() != null)
                    || (getStoragePool().getspm_vds_id() != null && !getStoragePool().getspm_vds_id().equals(
                            poolFromDb.getspm_vds_id()))) {
                getStoragePool().setspm_vds_id(poolFromDb.getspm_vds_id());
            }
            if (getStoragePool().getStatus() == StoragePoolStatus.Uninitialized) {
                getStoragePool().setspm_vds_id(null);
            }

            executeInScope(TransactionScopeOption.Required, new TransactionMethod<StoragePool>() {
                @Override
                public StoragePool runInTransaction() {
                    getStoragePoolDAO().update(getStoragePool());
                    return null;
                }
            });
            StoragePoolStatusHandler.poolStatusChanged(getStoragePool().getId(), getStoragePool().getStatus());
        }
    }

    protected void updateStorageDomainFormat(StorageDomain domain) {
        StorageDomainType sdType = domain.getStorageDomainType();
        if (sdType == StorageDomainType.Data || sdType == StorageDomainType.Master) {
            final StorageDomainStatic storageStaticData = domain.getStorageStaticData();
            final StorageFormatType targetFormat = getStoragePool().getStoragePoolFormatType();

            if (storageStaticData.getStorageFormat() != targetFormat) {
                log.infoFormat("Updating storage domain {0} (type {1}) to format {2}",
                        getStorageDomain().getId(), sdType, targetFormat);
                storageStaticData.setStorageFormat(targetFormat);
                getStorageDomainStaticDAO().update(storageStaticData);
            }
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
     * @param domainStatic
     *            -the domain object
     * @param storagePool
     *            - the pool object
     * @return
     */
    protected boolean isStorageDomainFormatCorrectForDC(StorageDomainStatic domainStatic, StoragePool storagePool) {
        if (domainStatic.getStorageDomainType().isIsoOrImportExportDomain()) {
            return true;
        }

        if (storagePool != null) {
            if (VersionStorageFormatUtil.getPreferredForVersion(storagePool.getcompatibility_version(),
                    domainStatic.getStorageType()).compareTo(domainStatic.getStorageFormat()) < 0) {
                return false;
            }
        }
        return true;
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
        sync.execute();
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
        return Config.<Integer> getValue(ConfigValues.StoragePoolNameSizeLimit);
    }

    /** @return The supported storage domain formats, delimited by commas (","). */
    protected String getSupportedStorageFormats(Version version) {
        return Config.<String> getValue(ConfigValues.SupportedStorageFormats, version.toString());
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

    protected StoragePoolIsoMapDAO getStoragePoolIsoMapDAO() {
        return getDbFacade().getStoragePoolIsoMapDao();
    }

    protected StorageDomainDynamicDAO getStorageDomainDynamicDao() {
        return getDbFacade().getStorageDomainDynamicDao();
    }

    protected DiskImageDAO getDiskImageDao() {
        return getDbFacade().getDiskImageDao();
    }

    /* Transaction methods */

    protected void executeInScope(TransactionScopeOption scope, TransactionMethod<?> code) {
        TransactionSupport.executeInScope(scope, code);
    }

    @Override
    public VDSReturnValue runVdsCommand(VDSCommandType commandType, VDSParametersBase parameters) throws VdcBLLException {
        return super.runVdsCommand(commandType, parameters);
    }
}
