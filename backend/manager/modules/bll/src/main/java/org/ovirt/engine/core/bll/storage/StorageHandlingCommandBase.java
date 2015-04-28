package org.ovirt.engine.core.bll.storage;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.RetrieveImageDataParameters;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.bll.snapshots.SnapshotsValidator;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.RegisterDiskParameters;
import org.ovirt.engine.core.common.action.StoragePoolParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.OvfEntityData;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainOvfInfo;
import org.ovirt.engine.core.common.businessentities.StorageDomainOvfInfoStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StorageFormatType;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmEntityType;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.queries.GetUnregisteredDisksQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSParametersBase;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.TransactionScopeOption;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.dao.DiskImageDAO;
import org.ovirt.engine.core.dao.StoragePoolIsoMapDAO;
import org.ovirt.engine.core.dao.UnregisteredOVFDataDAO;
import org.ovirt.engine.core.utils.JsonHelper;
import org.ovirt.engine.core.utils.OvfUtils;
import org.ovirt.engine.core.utils.SyncronizeNumberOfAsyncOperations;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.linq.Predicate;
import org.ovirt.engine.core.utils.ovf.OvfInfoFileConstants;
import org.ovirt.engine.core.utils.ovf.OvfParser;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

public abstract class StorageHandlingCommandBase<T extends StoragePoolParametersBase> extends CommandBase<T> {

    private CinderBroker cinderBroker;
    protected List<DiskImage> ovfDisks;

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

    protected List<VDS> getAllRunningVdssInPool() {
        return getVdsDAO().getAllForStoragePoolAndStatus(getStoragePool().getId(), VDSStatus.Up);
    }

    protected void updateStoragePoolMasterDomainVersionInDiffTransaction() {
        executeInScope(TransactionScopeOption.Suppress, new TransactionMethod<Void>() {
            @Override
            public Void runInTransaction() {
                int master_domain_version = getStoragePoolDAO().increaseStoragePoolMasterVersion(getStoragePool().getId());
                getStoragePool().setMasterDomainVersion(master_domain_version);
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
            return hosts.get(new Random().nextInt(hosts.size()));
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
        List<OvfEntityData> ovfEntityList =
                getUnregisteredOVFDataDao().getByEntityIdAndStorageDomain(entityId, storageDomainId);
        if (!ovfEntityList.isEmpty()) {
            log.info("Entity '{}' with id '{}', already exists as unregistered entity. override it with the new entity from the engine",
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
                                getVdsGroupDAO().get(vmTemplate.getVdsGroupId()).getCompatibilityVersion(),
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
            if ((getStoragePool().getSpmVdsId() == null && poolFromDb.getSpmVdsId() != null)
                    || (getStoragePool().getSpmVdsId() != null && !getStoragePool().getSpmVdsId().equals(
                            poolFromDb.getSpmVdsId()))) {
                getStoragePool().setSpmVdsId(poolFromDb.getSpmVdsId());
            }
            if (getStoragePool().getStatus() == StoragePoolStatus.Uninitialized) {
                getStoragePool().setSpmVdsId(null);
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

    protected void registerAllOvfDisks(List<DiskImage> ovfStoreDiskImages, Guid storageDomainId) {
        for (DiskImage ovfStoreDiskImage : ovfStoreDiskImages) {
            ovfStoreDiskImage.setDiskAlias(OvfInfoFileConstants.OvfStoreDescriptionLabel);
            ovfStoreDiskImage.setDiskDescription(OvfInfoFileConstants.OvfStoreDescriptionLabel);
            ovfStoreDiskImage.setShareable(true);
            RegisterDiskParameters registerDiskParams =
                    new RegisterDiskParameters(ovfStoreDiskImage, storageDomainId);

            boolean registerDiskResult = runInternalAction(VdcActionType.RegisterDisk, registerDiskParams,
                    cloneContext()).getSucceeded();

            log.info("Register new floating OVF_STORE disk with disk id '{}' for storage domain '{}' has {}",
                    ovfStoreDiskImage.getId(),
                    storageDomainId,
                    registerDiskResult ? "succeeded" : "failed");

            if (registerDiskResult) {
                addOvfStoreDiskToDomain(ovfStoreDiskImage);
            }
        }
    }

    /**
     * Register all the OVF_STORE disks as floating disks in the engine.
     */
    private void addOvfStoreDiskToDomain(DiskImage ovfDisk) {
        // Setting OVF_STORE disk to be outdated so it will be updated.
        StorageDomainOvfInfo storageDomainOvfInfo =
                new StorageDomainOvfInfo(getStorageDomainId(),
                        null,
                        ovfDisk.getId(),
                        StorageDomainOvfInfoStatus.OUTDATED,
                        null);
        getDbFacade().getStorageDomainOvfInfoDao().save(storageDomainOvfInfo);
    }

    protected void updateStorageDomainFormatIfNeeded(StorageDomain domain) {
        final StorageDomainType sdType = domain.getStorageDomainType();

        if (!sdType.isDataDomain()) {
            log.debug("Skipping format update for domain '{}' (type '{}')",
                    getStorageDomain().getId(), sdType);
            return;
        }

        final StorageDomainStatic storageStaticData = domain.getStorageStaticData();
        final StorageFormatType targetFormat = getStoragePool().getStoragePoolFormatType();

        if (storageStaticData.getStorageFormat() != targetFormat) {
            log.info("Updating storage domain '{}' (type '{}') to format '{}'",
                    getStorageDomain().getId(), sdType, targetFormat);
            storageStaticData.setStorageFormat(targetFormat);
            getStorageDomainStaticDAO().update(storageStaticData);
        } else {
            log.debug("Skipping format update for domain '{}' format is '{}'",
                    getStorageDomain().getId(), storageStaticData.getStorageFormat());
        }
    }

    protected List<DiskImage> getAllOVFDisks(Guid storageDomainId, Guid storagePoolId) {
        if (ovfDisks == null) {
            ovfDisks = new ArrayList<>();

            // Get all unregistered disks.
            List<Disk> unregisteredDisks = getBackend().runInternalQuery(VdcQueryType.GetUnregisteredDisks,
                    new GetUnregisteredDisksQueryParameters(storageDomainId,
                            storagePoolId)).getReturnValue();
            if (unregisteredDisks == null) {
                log.error("An error occurred while fetching unregistered disks from Storage Domain id '{}'",
                        storageDomainId);
                return ovfDisks;
            }
            for (Disk disk : unregisteredDisks) {
                DiskImage ovfStoreDisk = (DiskImage) disk;
                String diskDecription = ovfStoreDisk.getDescription();
                if (diskDecription.contains(OvfInfoFileConstants.OvfStoreDescriptionLabel)) {
                    Map<String, Object> diskDescriptionMap;
                    try {
                        diskDescriptionMap = JsonHelper.jsonToMap(diskDecription);
                    } catch (IOException e) {
                        log.warn("Exception while generating json containing ovf store info: {}", e.getMessage());
                        log.debug("Exception", e);
                        continue;
                    }

                    // The purpose of this check is to verify that it's an OVF store with data related to the Storage
                    // Domain.
                    if (!isDomainExistsInDiskDescription(diskDescriptionMap, storageDomainId)) {
                        log.warn("The disk description does not contain the storage domain id '{}'", storageDomainId);
                        continue;
                    }
                    ovfDisks.add(ovfStoreDisk);
                }
            }
        }
        return ovfDisks;
    }

    /**
     * Returns the best match for OVF disk from all the disks. If no OVF disk was found, it returns null for disk and
     * size 0. If there are OVF disks, we first match the updated ones, and from them we retrieve the one which was last
     * updated.
     *
     * @param ovfStoreDiskImages
     *            - A list of OVF_STORE disks
     * @return A Pair which contains the best OVF disk to retrieve data from and its size.
     */
    private Pair<DiskImage, Long> getLatestOVFDisk(List<DiskImage> ovfStoreDiskImages) {
        Date foundOvfDiskUpdateDate = new Date();
        boolean isFoundOvfDiskUpdated = false;
        Long size = 0L;
        Disk ovfDisk = null;
        for (DiskImage ovfStoreDisk : ovfStoreDiskImages) {
            boolean isBetterOvfDiskFound = false;
            Map<String, Object> diskDescriptionMap;
            try {
                diskDescriptionMap = JsonHelper.jsonToMap(ovfStoreDisk.getDescription());
            } catch (IOException e) {
                log.warn("Exception while generating json containing ovf store info: {}", e.getMessage());
                log.debug("Exception", e);
                continue;
            }

            boolean isUpdated = Boolean.valueOf(diskDescriptionMap.get(OvfInfoFileConstants.IsUpdated).toString());
            Date date = getDateFromDiskDescription(diskDescriptionMap);
            if (date == null) {
                continue;
            }
            if (isFoundOvfDiskUpdated && !isUpdated) {
                continue;
            }
            if ((isUpdated && !isFoundOvfDiskUpdated) || date.after(foundOvfDiskUpdateDate)) {
                isBetterOvfDiskFound = true;
            }
            if (isBetterOvfDiskFound) {
                isFoundOvfDiskUpdated = isUpdated;
                foundOvfDiskUpdateDate = date;
                ovfDisk = ovfStoreDisk;
                size = new Long(diskDescriptionMap.get(OvfInfoFileConstants.Size).toString());
            }
        }
        return new Pair<>((DiskImage)ovfDisk, size);
    }

    protected List<OvfEntityData> getEntitiesFromStorageOvfDisk(Guid storageDomainId, Guid storagePoolId) {
        // Initialize a new ArrayList with all the ovfDisks in the specified Storage Domain,
        // so the entities can be removed from the list every time we register the latest OVF disk and we can keep the
        // ovfDisks cache list updated.
        List<DiskImage> ovfStoreDiskImages = new ArrayList(getAllOVFDisks(storageDomainId, storagePoolId));
        if (!ovfStoreDiskImages.isEmpty()) {
            if (!FeatureSupported.ovfStoreOnAnyDomain(getStoragePool().getCompatibilityVersion())) {
                auditLogDirector.log(this, AuditLogType.RETRIEVE_UNREGISTERED_ENTITIES_NOT_SUPPORTED_IN_DC_VERSION);
                return Collections.emptyList();
            }
            while (!ovfStoreDiskImages.isEmpty()) {
                Pair<DiskImage, Long> ovfDiskAndSize = getLatestOVFDisk(ovfStoreDiskImages);
                DiskImage ovfDisk = ovfDiskAndSize.getFirst();
                if (ovfDisk != null) {
                    try {
                        VdcReturnValueBase vdcReturnValue = runInternalAction(VdcActionType.RetrieveImageData,
                                new RetrieveImageDataParameters(getParameters().getStoragePoolId(),
                                        storageDomainId,
                                        ovfDisk.getId(),
                                        ovfDisk.getImage().getId(),
                                        ovfDiskAndSize.getSecond()), cloneContextAndDetachFromParent());

                        getReturnValue().getVdsmTaskIdList().addAll(vdcReturnValue.getInternalVdsmTaskIdList());
                        if (vdcReturnValue.getSucceeded()) {
                            return OvfUtils.getOvfEntities((byte[]) vdcReturnValue.getActionReturnValue(),
                                    storageDomainId);
                        } else {
                            log.error("Image data could not be retrieved for disk id '{}' in storage domain id '{}'",
                                    ovfDisk.getId(),
                                    storageDomainId);
                        }
                    } catch (RuntimeException e) {
                        // We are catching RuntimeException, since the call for OvfUtils.getOvfEntities will throw
                        // a RuntimeException if there is a problem to untar the file.
                        log.error("Image data could not be retrieved for disk id '{}' in storage domain id '{}': {}",
                                ovfDisk.getId(),
                                storageDomainId,
                                e.getMessage());
                        log.debug("Exception", e);
                    }
                    ovfStoreDiskImages.remove(ovfDisk);
                }
            }
            AuditLogableBase logable = new AuditLogableBase();
            logable.setStorageDomainId(storageDomainId);
            auditLogDirector.log(logable, AuditLogType.RETRIEVE_OVF_STORE_FAILED);
        } else {
            log.warn("There are no OVF_STORE disks on storage domain id {}", storageDomainId);
            auditLogDirector.log(this, AuditLogType.OVF_STORE_DOES_NOT_EXISTS);
        }
        return Collections.emptyList();
    }

    protected boolean checkStoragePoolNameLengthValid() {
        boolean result = true;
        if (getStoragePool().getName().length() > getStoragePoolNameSizeLimit()) {
            result = false;
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_NAME_LENGTH_IS_TOO_LONG);
        }
        return result;
    }

    private Date getDateFromDiskDescription(Map<String, Object> map) {
        try {
            Object lastUpdate = map.get(OvfInfoFileConstants.LastUpdated);
            if (lastUpdate != null) {
                return new SimpleDateFormat(OvfParser.formatStrFromDiskDescription).parse(lastUpdate.toString());
            } else {
                log.info("LastUpdate Date is not initialized in the OVF_STORE disk.");
            }
        } catch (java.text.ParseException e) {
            log.error("LastUpdate Date could not be parsed from disk description: {}", e.getMessage());
            log.debug("Exception", e);
        }
        return null;
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

    private boolean isDomainExistsInDiskDescription(Map<String, Object> map, Guid storageDomainId) {
        if (map.get(OvfInfoFileConstants.Domains) == null) {
            return false;
        }
        return map.get(OvfInfoFileConstants.Domains).toString().contains(storageDomainId.toString());
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

    /* Overidden DAO access methods, for easier testing */

    @Override
    public BackendInternal getBackend() {
        return super.getBackend();
    }

    protected StoragePoolIsoMapDAO getStoragePoolIsoMapDAO() {
        return getDbFacade().getStoragePoolIsoMapDao();
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

    public CinderBroker getCinderBroker() {
        if (cinderBroker == null) {
            cinderBroker = new CinderBroker(getStorageDomainId(), getReturnValue().getExecuteFailedMessages());
        }
        return cinderBroker;
    }
}
