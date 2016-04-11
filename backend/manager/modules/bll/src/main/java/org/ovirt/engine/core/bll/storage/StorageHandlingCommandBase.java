package org.ovirt.engine.core.bll.storage;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.RetrieveImageDataParameters;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.bll.snapshots.SnapshotsValidator;
import org.ovirt.engine.core.bll.storage.disk.cinder.CinderBroker;
import org.ovirt.engine.core.bll.storage.disk.image.MetadataDiskDescriptionHandler;
import org.ovirt.engine.core.bll.storage.pool.ActivateDeactivateSingleAsyncOperationFactory;
import org.ovirt.engine.core.bll.storage.pool.StoragePoolStatusHandler;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.AuditLogType;
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
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.businessentities.VmEntityType;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.UnregisteredDisk;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.errors.EngineMessage;
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
import org.ovirt.engine.core.dao.UnregisteredDisksDao;
import org.ovirt.engine.core.utils.JsonHelper;
import org.ovirt.engine.core.utils.OvfUtils;
import org.ovirt.engine.core.utils.SyncronizeNumberOfAsyncOperations;
import org.ovirt.engine.core.utils.ovf.OvfInfoFileConstants;
import org.ovirt.engine.core.utils.ovf.OvfParser;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

public abstract class StorageHandlingCommandBase<T extends StoragePoolParametersBase> extends CommandBase<T> {

    private CinderBroker cinderBroker;
    protected List<DiskImage> ovfDisks;
    protected List<UnregisteredDisk> unregisteredDisks = new ArrayList<>();

    @Inject
    protected UnregisteredDisksDao unregisteredDisksDao;

    protected StorageHandlingCommandBase(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
        init(parameters);

    }

    protected void init(T parameters) {
        setVdsId(parameters.getVdsId());
        if (getParameters() != null && !getParameters().getStoragePoolId().equals(Guid.Empty)) {
            setStoragePoolId(getParameters().getStoragePoolId());
        }
    }

    /**
     * Constructor for command creation when compensation is applied on startup
     */
    protected StorageHandlingCommandBase(Guid commandId) {
        super(commandId);
    }

    public static List<VDS> getAllRunningVdssInPool(StoragePool pool) {
        return DbFacade.getInstance().getVdsDao().getAllForStoragePoolAndStatus(pool.getId(), VDSStatus.Up);
    }

    protected List<VDS> getAllRunningVdssInPool() {
        return getVdsDao().getAllForStoragePoolAndStatus(getStoragePool().getId(), VDSStatus.Up);
    }

    protected void updateStoragePoolMasterDomainVersionInDiffTransaction() {
        executeInScope(TransactionScopeOption.Suppress, () -> {
            int master_domain_version = getStoragePoolDao().increaseStoragePoolMasterVersion(getStoragePool().getId());
            getStoragePool().setMasterDomainVersion(master_domain_version);
            return null;
        });
    }

    protected Guid getMasterDomainIdFromDb() {
        Guid ret = Guid.Empty;
        if (getStoragePool() != null) {
            ret = getStorageDomainDao()
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
        List<VDS> hosts = getVdsDao().getAllForStoragePoolAndStatus(getStoragePool().getId(),
                VDSStatus.Up);
        if (!hosts.isEmpty()) {
            return hosts.get(new Random().nextInt(hosts.size()));
        }
        addValidationMessage(EngineMessage.ACTION_TYPE_FAILED_NO_VDS_IN_POOL);
        return null;
    }

    protected boolean checkStoragePool() {
        if (getStoragePool() == null) {
            addValidationMessage(EngineMessage.ACTION_TYPE_FAILED_STORAGE_POOL_NOT_EXIST);
            return false;
        }
        return true;
    }

    protected boolean canDetachStorageDomainWithVmsAndDisks(StorageDomain storageDomain) {
        if (!storageDomain.getStorageDomainType().isDataDomain()) {
            return true;
        }

        SnapshotsValidator snapshotsValidator = new SnapshotsValidator();
        List<VM> vmRelatedToDomain = getVmDao().getAllForStorageDomain(storageDomain.getId());
        List<String> vmsInPreview = vmRelatedToDomain.stream().filter(vm -> !snapshotsValidator.vmNotInPreview(vm.getId()).isValid()).map(VM::getName).collect(Collectors.toList());

        List<VM> vmsWithDisksOnMultipleStorageDomain = getVmDao().getAllVMsWithDisksOnOtherStorageDomain(storageDomain.getId());
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

        List<VmTemplate> templatesRelatedToDomain = getVmTemplateDao().getAllForStorageDomain(storageDomain.getId());
        List<VmTemplate> vmTemplatesWithDisksOnMultipleStorageDomain =
                getVmTemplateDao().getAllTemplatesWithDisksOnOtherStorageDomain(storageDomain.getId());
        templatesRelatedToDomain.removeAll(vmTemplatesWithDisksOnMultipleStorageDomain);

        entitiesDeleteProtected.addAll(templatesRelatedToDomain.stream().filter(VmBase::isDeleteProtected).map(VmTemplate::getName).collect(Collectors.toList()));

        boolean succeeded = true;
        if (!entitiesDeleteProtected.isEmpty()) {
            addValidationMessage(EngineMessage.ACTION_TYPE_FAILED_STORAGE_DELETE_PROTECTED);
            addValidationMessageVariable("vms", StringUtils.join(entitiesDeleteProtected, ","));
            succeeded = false;
        }
        if (!vmsInPool.isEmpty()) {
            addValidationMessage(EngineMessage.ACTION_TYPE_FAILED_STORAGE_VMS_IN_POOL);
            addValidationMessageVariable("vms", StringUtils.join(vmsInPool, ","));
            succeeded = false;
        }
        if (!vmsInPreview.isEmpty()) {
            addValidationMessage(EngineMessage.ACTION_TYPE_FAILED_STORAGE_DELETE_VMS_IN_PREVIEW);
            addValidationMessageVariable("vms", StringUtils.join(vmsInPreview, ","));
            succeeded = false;
        }
        return succeeded;
    }

    protected void detachStorageDomainWithEntities(StorageDomain storageDomain) {
        // Check if we have entities related to the Storage Domain.
        List<VM> vmsForStorageDomain = getVmDao().getAllForStorageDomain(storageDomain.getId());
        List<VmTemplate> vmTemplatesForStorageDomain = getVmTemplateDao().getAllForStorageDomain(storageDomain.getId());
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
            TransactionSupport.executeInNewTransaction(() -> {
                for (VM vm : vmsForStorageDomain) {
                    removeEntityLeftOver(vm.getId(), vm.getName(), storageDomainId);
                    getUnregisteredOVFDataDao().saveOVFData(new OvfEntityData(
                            vm.getId(),
                            vm.getName(),
                            VmEntityType.VM,
                            vm.getClusterArch(),
                            vm.getCompatibilityVersion(),
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
                            getClusterDao().get(vmTemplate.getClusterId()).getCompatibilityVersion(),
                            storageDomainId,
                            null,
                            null));
                }
                getStorageDomainDao().removeEntitesFromStorageDomain(storageDomainId);
                return null;
            });
        }
    }

    protected boolean checkStoragePoolStatus(StoragePoolStatus status) {
        boolean returnValue = false;
        StoragePool storagePool = getStoragePool();
        if (storagePool != null) {
            returnValue = storagePool.getStatus() == status;
            if (!returnValue
                    && !getReturnValue().getValidationMessages().contains(
                            EngineMessage.ACTION_TYPE_FAILED_STORAGE_POOL_STATUS_ILLEGAL.toString())) {
                addValidationMessage(EngineMessage.ACTION_TYPE_FAILED_STORAGE_POOL_STATUS_ILLEGAL);
            }
        }
        return returnValue;
    }

    protected boolean checkStoragePoolStatusNotEqual(StoragePoolStatus status, EngineMessage onFailMessage) {
        boolean returnValue = false;
        StoragePool storagePool = getStoragePool();
        if (storagePool != null) {
            returnValue = storagePool.getStatus() != status;
            if (!returnValue
                    && !getReturnValue().getValidationMessages().contains(onFailMessage.name())) {
                addValidationMessage(onFailMessage);
            }
        }
        return returnValue;
    }

    protected boolean isStorageDomainNotNull(StorageDomain domain) {
        if (domain == null) {
            addValidationMessage(EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_NOT_EXIST);
            return false;
        }

        return true;
    }

    protected void calcStoragePoolStatusByDomainsStatus() {
        StorageDomain masterDomain =
                getStorageDomainDao().getStorageDomains(getStoragePool().getId(), StorageDomainType.Master)
                        .stream().findFirst().orElse(null);

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
            StoragePool poolFromDb = getStoragePoolDao().get(getStoragePool().getId());
            if ((getStoragePool().getSpmVdsId() == null && poolFromDb.getSpmVdsId() != null)
                    || (getStoragePool().getSpmVdsId() != null && !getStoragePool().getSpmVdsId().equals(
                            poolFromDb.getSpmVdsId()))) {
                getStoragePool().setSpmVdsId(poolFromDb.getSpmVdsId());
            }
            if (getStoragePool().getStatus() == StoragePoolStatus.Uninitialized) {
                getStoragePool().setSpmVdsId(null);
            }

            executeInScope(TransactionScopeOption.Required, () -> {
                getStoragePoolDao().update(getStoragePool());
                return null;
            });
            StoragePoolStatusHandler.poolStatusChanged(getStoragePool().getId(), getStoragePool().getStatus());
        }
    }

    protected void castDiskImagesToUnregisteredDisks(List<DiskImage> disksFromStorage, Guid storageDomainId) {
        for (DiskImage disk : disksFromStorage) {
            disk.getStorageIds().set(0, storageDomainId);
            UnregisteredDisk unregisteredDisk = new UnregisteredDisk(disk);
            unregisteredDisks.add(unregisteredDisk);
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
        getStorageDomainOvfInfoDao().save(storageDomainOvfInfo);
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
            getStorageDomainStaticDao().update(storageStaticData);
        } else {
            log.debug("Skipping format update for domain '{}' format is '{}'",
                    getStorageDomain().getId(), storageStaticData.getStorageFormat());
        }
    }

    protected List<DiskImage> getAllOVFDisks(Guid storageDomainId, Guid storagePoolId) {
        // Null ovfDisks indicating that the ovfDisks list was not set and also that the unregisteredDisks were not
        // fetched yet.
        if (ovfDisks == null) {
            ovfDisks = new ArrayList<>();

            // Get all unregistered disks.
            List<DiskImage> disksFromStorage = getBackend().runInternalQuery(VdcQueryType.GetUnregisteredDisks,
                    new GetUnregisteredDisksQueryParameters(storageDomainId,
                            storagePoolId)).getReturnValue();
            if (disksFromStorage == null) {
                log.error("An error occurred while fetching unregistered disks from Storage Domain id '{}'",
                        storageDomainId);
                return ovfDisks;
            } else {
                castDiskImagesToUnregisteredDisks(disksFromStorage, storageDomainId);
            }
            for (Disk disk : disksFromStorage) {
                DiskImage ovfStoreDisk = (DiskImage) disk;
                String diskDescription = ovfStoreDisk.getDescription();
                if (diskDescription.contains(OvfInfoFileConstants.OvfStoreDescriptionLabel)) {
                    Map<String, Object> diskDescriptionMap;
                    try {
                        diskDescriptionMap = JsonHelper.jsonToMap(diskDescription);
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
                size = Long.valueOf(diskDescriptionMap.get(OvfInfoFileConstants.Size).toString());
            }
        }
        return new Pair<>((DiskImage)ovfDisk, size);
    }

    protected List<OvfEntityData> getEntitiesFromStorageOvfDisk(Guid storageDomainId, Guid storagePoolId) {
        // Initialize a new ArrayList with all the ovfDisks in the specified Storage Domain,
        // so the entities can be removed from the list every time we register the latest OVF disk and we can keep the
        // ovfDisks cache list updated.
        List<DiskImage> ovfStoreDiskImages = new ArrayList<>(getAllOVFDisks(storageDomainId, storagePoolId));
        if (!ovfStoreDiskImages.isEmpty()) {
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
                            List<OvfEntityData> returnedMap =
                                    OvfUtils.getOvfEntities(vdcReturnValue.getActionReturnValue(),
                                            unregisteredDisks,
                                            storageDomainId);
                            return returnedMap;
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
        return new ArrayList<>();
    }

    protected void initUnregisteredDisksToDB(Guid storageDomainId) {
        List<DiskImage> existingDisks = getDiskImageDao().getAllForStorageDomain(storageDomainId);
        for (Object unregisteredDiskObj : unregisteredDisks) {
            UnregisteredDisk unregisteredDisk = (UnregisteredDisk) unregisteredDiskObj;
            if (existingDisks.stream().anyMatch(diskImage -> diskImage.getId().equals(unregisteredDisk.getId()))) {
                log.info("Disk {} with id '{}' already exists in the engine, therefore will not be " +
                        "part of the unregistered disks.",
                        unregisteredDisk.getDiskAlias(),
                        unregisteredDisk.getId());
                continue;
            }
            unregisteredDisksDao.removeUnregisteredDisk(unregisteredDisk.getId(), storageDomainId);
            unregisteredDisksDao.saveUnregisteredDisk(unregisteredDisk);
            log.info("Adding unregistered disk of disk id '{}' and disk alias '{}'",
                    unregisteredDisk.getId(),
                    unregisteredDisk.getDiskAlias());
        }
    }

    protected boolean checkStoragePoolNameLengthValid() {
        boolean result = true;
        if (getStoragePool().getName().length() > Config.<Integer>getValue(ConfigValues.StoragePoolNameSizeLimit)) {
            result = false;
            addValidationMessage(EngineMessage.ACTION_TYPE_FAILED_NAME_LENGTH_IS_TOO_LONG);
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
        ArrayList<Object> parameters = new ArrayList<>();
        parameters.add(allRunningVdsInPool);
        parameters.add(getStorageDomain());
        parameters.add(getStoragePool());
        return parameters;
    }

    private boolean isDomainExistsInDiskDescription(Map<String, Object> map, Guid storageDomainId) {
        return Objects.toString(map.get(OvfInfoFileConstants.Domains), "").contains(storageDomainId.toString());
    }

    protected String getJsonDiskDescription(Disk disk) {
        try {
            return MetadataDiskDescriptionHandler.getInstance().generateJsonDiskDescription(disk);
        } catch (IOException e) {
            log.error("Exception while generating json for disk. ERROR: '{}'", e);
            return StringUtils.EMPTY;
        }
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

    /* Overidden Dao access methods, for easier testing */

    @Override
    public BackendInternal getBackend() {
        return super.getBackend();
    }

    /* Transaction methods */

    protected void executeInScope(TransactionScopeOption scope, TransactionMethod<?> code) {
        TransactionSupport.executeInScope(scope, code);
    }

    @Override
    public VDSReturnValue runVdsCommand(VDSCommandType commandType, VDSParametersBase parameters) throws
            EngineException {
        return super.runVdsCommand(commandType, parameters);
    }

    public CinderBroker getCinderBroker() {
        if (cinderBroker == null) {
            cinderBroker = new CinderBroker(getStorageDomainId(), getReturnValue().getExecuteFailedMessages());
        }
        return cinderBroker;
    }

    protected void resetOvfStoreAndUnregisteredDisks() {
        ovfDisks = null;
        unregisteredDisks = new ArrayList<>();
    }
}
