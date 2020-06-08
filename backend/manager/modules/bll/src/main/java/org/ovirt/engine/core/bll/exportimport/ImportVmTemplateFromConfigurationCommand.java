package org.ovirt.engine.core.bll.exportimport;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.storage.ovfstore.DrMappingHelper;
import org.ovirt.engine.core.bll.storage.ovfstore.OvfHelper;
import org.ovirt.engine.core.bll.validator.ImportValidator;
import org.ovirt.engine.core.bll.validator.storage.StorageDomainValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ImportVmTemplateFromConfParameters;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.OvfEntityData;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskVmElement;
import org.ovirt.engine.core.common.businessentities.storage.FullEntityOvfData;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.common.businessentities.storage.ImageStorageDomainMap;
import org.ovirt.engine.core.common.businessentities.storage.QcowCompat;
import org.ovirt.engine.core.common.businessentities.storage.QemuImageInfo;
import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.common.vdscommands.GetImageInfoVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.GetImagesListVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dao.DiskImageDao;
import org.ovirt.engine.core.dao.DiskVmElementDao;
import org.ovirt.engine.core.dao.ImageStorageDomainMapDao;
import org.ovirt.engine.core.dao.RoleDao;
import org.ovirt.engine.core.dao.StorageDomainDao;
import org.ovirt.engine.core.dao.UnregisteredDisksDao;
import org.ovirt.engine.core.dao.UnregisteredOVFDataDao;
import org.ovirt.engine.core.dao.VmDeviceDao;
import org.ovirt.engine.core.utils.ovf.OvfReaderException;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NonTransactiveCommandAttribute
public class ImportVmTemplateFromConfigurationCommand<T extends ImportVmTemplateFromConfParameters> extends ImportVmTemplateCommand<T> {

    private static final Logger log = LoggerFactory.getLogger(ImportVmFromConfigurationCommand.class);
    private Map<Guid, String> failedDisksToImportForAuditLog = new HashMap<>();
    private OvfEntityData ovfEntityData;
    VmTemplate vmTemplateFromConfiguration;
    private ArrayList<DiskImage> imagesList;
    private List<String> missingUsers = new ArrayList<>();
    private List<String> missingRoles = new ArrayList<>();
    private List<String> missingVnicMappings = new ArrayList<>();
    private Collection<DiskImage> templateDisksToAttach;
    private Map<Guid, Guid> diskIdToStorageDomainId;
    private Guid invalidDiskIdToAttach;

    @Inject
    private AuditLogDirector auditLogDirector;

    @Inject
    private OvfHelper ovfHelper;
    @Inject
    private ImageStorageDomainMapDao imageStorageDomainMapDao;
    @Inject
    private StorageDomainDao storageDomainDao;
    @Inject
    private UnregisteredOVFDataDao unregisteredOVFDataDao;
    @Inject
    private UnregisteredDisksDao unregisteredDisksDao;
    @Inject
    private DrMappingHelper drMappingHelper;
    @Inject
    private RoleDao roleDao;
    @Inject
    private VmDeviceDao vmDeviceDao;
    @Inject
    private DiskVmElementDao diskVmElementDao;
    @Inject
    private DiskImageDao diskImageDao;

    public ImportVmTemplateFromConfigurationCommand(Guid commandId) {
        super(commandId);
    }

    public ImportVmTemplateFromConfigurationCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(Scope.Execution);
    }

    @Override
    public Guid getVmTemplateId() {
        if (getParameters().isImagesExistOnTargetStorageDomain()) {
            return getParameters().getContainerId();
        }
        return super.getVmTemplateId();
    }

    @Override
    protected boolean validate() {
        if (!super.validate()) {
            return false;
        }

        if (templateDisksToAttach == null) {
            ArrayList<DiskImage> disks = new ArrayList<>(getVmTemplate().getDiskTemplateMap().values());
            setImagesWithStoragePoolId(getStorageDomain().getStoragePoolId(), disks);
            getVmTemplate().setImages(disks);
            if (getParameters().isImagesExistOnTargetStorageDomain() &&
                    !validateUnregisteredEntity(vmTemplateFromConfiguration, ovfEntityData)) {
                return false;
            }
        }

        ImportValidator importValidator = new ImportValidator(getParameters());
        removeInvalidUsers(importValidator);
        removeInavlidRoles(importValidator);
        return true;
    }

    @Override
    protected boolean validateSourceStorageDomain() {
        if (templateDisksToAttach == null) {
            return super.validateSourceStorageDomain();
        }
        if (invalidDiskIdToAttach != null) {
            log.error("failed to find an image of disk {} in the database", invalidDiskIdToAttach);
            return failValidation(EngineMessage.TEMPLATE_IMAGE_NOT_EXIST);
        }
        diskIdToStorageDomainId = new HashMap<>();
        for (DiskImage disk : templateDisksToAttach) {
            // there should be a single volume for the disk as this is a template's disk
            if (disk.getImageStatus() != ImageStatus.OK) {
                log.error("found an image ({}) whose status is {}",
                        disk.getImageId(), disk.getImageStatus());
                return failValidation(EngineMessage.ACTION_TYPE_FAILED_TEMPLATE_DISK_STATUS_IS_NOT_VALID);
            }
            Iterator<Guid> storageIdsIterator = disk.getStorageIds().iterator();
            if (!storageIdsIterator.hasNext()) {
                log.error("found an image with no storage domain {}", disk.getImageId());
                return failValidation(EngineMessage.TEMPLATE_IMAGE_NOT_EXIST);
            }
            // theoretically, a template's disk may reside within several storage domains, however,
            // it is unlikely to be the case here and anyway, we can just take the first storage domain
            diskIdToStorageDomainId.put(disk.getId(), storageIdsIterator.next());
        }
        return true;
    }

    @Override
    protected boolean validateSpaceRequirements(Collection<DiskImage> diskImages) {
        return templateDisksToAttach == null ? super.validateSpaceRequirements(diskImages) : true;
    }

    private void updateVnicsFromMapping() {
        if (templateDisksToAttach == null) {
            missingVnicMappings = drMappingHelper.updateVnicsFromMappings(
                    getParameters().getClusterId(),
                    getParameters().getVmTemplate().getName(),
                    vmTemplateFromConfiguration.getInterfaces(),
                    getParameters().getExternalVnicProfileMappings());
        }
    }

    private boolean validateUnregisteredEntity(VmTemplate entityFromConfiguration, OvfEntityData ovfEntityData) {
        if (ovfEntityData == null && !getParameters().isImportAsNewEntity()) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_UNSUPPORTED_OVF);
        }
        if (entityFromConfiguration == null) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_OVF_CONFIGURATION_NOT_SUPPORTED);
        }

        ImportValidator importValidator = new ImportValidator(getParameters());

        if (!validate(importValidator.validateDiskNotAlreadyExistOnDB(
                getImages(),
                getParameters().isAllowPartialImport(),
                imageToDestinationDomainMap,
                failedDisksToImportForAuditLog))) {
            return false;
        }

        if (!validateSourceStorageDomainsAvailability()) {
            return false;
        }

        if (!validateImagesAvailability()) {
            return false;
        }

        if (!getStorageDomain().getStorageDomainType().isDataDomain()) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_TYPE_UNSUPPORTED,
                    String.format("$domainId %1$s", getParameters().getStorageDomainId()),
                    String.format("$domainType %1$s", getStorageDomain().getStorageDomainType()));
        }
        return true;
    }

    private boolean validateImagesAvailability() {
        // A new array is being initialized to avoid ConcurrentModificationException when
        // invalid images are being removed from the images list.
        for (DiskImage image : new ArrayList<>(getImages())) {
            Guid imageGroupId = image.getId() != null ? image.getId() : Guid.Empty;
            if (!validateImageAvailability(image, imageGroupId)) {
                if (!getParameters().isAllowPartialImport()) {
                    return failValidation(EngineMessage.TEMPLATE_IMAGE_NOT_EXIST);
                }
                log.warn("Disk image '{}/{}' doesn't exist on any of its storage domains. " +
                        "Ignoring since the 'Allow Partial' flag is on", imageGroupId, image.getImageId());
                getImages().remove(image);
                failedDisksToImportForAuditLog.putIfAbsent(image.getId(), image.getDiskAlias());
            }
        }
        return true;
    }

    /**
     * Returns true iff the image is contained in at least
     * one storage domain among image.getStorageIds().
     */
    private boolean validateImageAvailability(DiskImage image, Guid imageGroupId) {
        boolean imageAvailable = false;
        for (Guid storageDomainId : image.getStorageIds()) {
            DiskImage fromIrs = null;
            try {
                log.info("Validating that image '{}/{}' is available in storage domain {}.", imageGroupId,
                        image.getImageId(), storageDomainId);
                fromIrs = (DiskImage) runVdsCommand(VDSCommandType.GetImageInfo,
                        new GetImageInfoVDSCommandParameters(getStoragePoolId(), storageDomainId,
                                imageGroupId, image.getImageId())).getReturnValue();
            } catch (Exception e) {
                log.debug("Unable to get image info from storage", e);
            }
            if (fromIrs == null) {
                log.warn("Disk image '{}/{}' doesn't exist on storage domain '{}'", imageGroupId,
                        image.getImageId(), storageDomainId);
            } else if (!imageAvailable) {
                imageAvailable = true;
            }
        }
        return imageAvailable;
    }

    private boolean validateSourceStorageDomainsAvailability() {
        // A new array is being initialized to avoid ConcurrentModificationException when
        // invalid images are being removed from the images list.
        for (DiskImage image : new ArrayList<>(getImages())) {
            // True iff at least one SD among image.getStorageIds() exists and is active.
            boolean storageDomainAvailable = false;
            Iterator<Guid> iterator = image.getStorageIds().iterator();
            while (iterator.hasNext()) {
                Guid storageDomainId = iterator.next();
                StorageDomain sd = storageDomainDao.getForStoragePool(storageDomainId, getStoragePool().getId());
                ValidationResult result = new StorageDomainValidator(sd).isDomainExistAndActive();
                if (!result.isValid()) {
                    log.warn("Storage domain '{}' does not exist.", storageDomainId);
                    iterator.remove();
                } else if (!storageDomainAvailable) {
                    storageDomainAvailable = true;
                }
            }
            if (!storageDomainAvailable) {
                if (!getParameters().isAllowPartialImport()) {
                    return failValidation(EngineMessage.ACTION_TYPE_FAILED_CANNOT_FIND_DISK_IMAGE_IN_ACTIVE_DOMAINS,
                            String.format("$diskImageId %1$s", image.getId()),
                            String.format("$imageId %1$s", image.getImageId()));
                }
                log.warn("No active storage domain that contains the image {}/{} exists. " +
                        "Ignoring since the 'Allow Partial' flag is on.", image.getId(), image.getImageId());
                getImages().remove(image);
                failedDisksToImportForAuditLog.putIfAbsent(image.getId(), image.getDiskAlias());
            }
        }
        return true;
    }

    private void setImagesWithStoragePoolId(Guid storagePoolId, List<DiskImage> diskImages) {
        diskImages.forEach(diskImage -> diskImage.setStoragePoolId(storagePoolId));
    }

    @Override
    public void init() {
        VmTemplate templateFromConfiguration = getParameters().getVmTemplate();
        if (templateFromConfiguration != null) {
            templateFromConfiguration.setClusterId(getParameters().getClusterId());
            getParameters().setContainerId(templateFromConfiguration.getId());
            getParameters().setImagesExistOnTargetStorageDomain(true);
            setDisksToBeAttached(templateFromConfiguration);
        } else {
            initUnregisteredTemplate();
        }
        setClusterId(getParameters().getClusterId());
        if (getCluster() != null) {
            setStoragePoolId(getCluster().getStoragePoolId());
            getParameters().setStoragePoolId(getStoragePoolId());
        }
        super.init();
    }

    private void setDisksToBeAttached(VmTemplate templateFromConfiguration) {
        templateDisksToAttach = templateFromConfiguration.getDiskTemplateMap().values();
        clearVmDisks(templateFromConfiguration);
        getParameters().setCopyCollapse(true);
        List<DiskImage> volumesFromDB;
        for (DiskImage disk : templateDisksToAttach) {
            volumesFromDB = diskImageDao.getAllSnapshotsForImageGroup(disk.getId());
            if (volumesFromDB.isEmpty()) {
                invalidDiskIdToAttach = disk.getId();
                break;
            }
            DiskImage volumeFromDB = volumesFromDB.get(0);
            disk.setImageId(volumeFromDB.getImageId());
            disk.setStorageIds(volumeFromDB.getStorageIds());
            disk.setStoragePoolId(volumeFromDB.getStoragePoolId());
        }
    }

    private static void clearVmDisks(VmTemplate template) {
        template.setDiskImageMap(new HashMap<>());
        template.getImages().clear();
        template.getDiskList().clear();
    }

    private void initUnregisteredTemplate() {
        List<OvfEntityData> ovfEntityList =
                unregisteredOVFDataDao.getByEntityIdAndStorageDomain(getParameters().getContainerId(),
                        getParameters().getStorageDomainId());
        if (!ovfEntityList.isEmpty()) {
            try {
                // We should get only one entity, since we fetched the entity with a specific Storage Domain
                ovfEntityData = ovfEntityList.get(0);
                FullEntityOvfData fullEntityOvfData = ovfHelper.readVmTemplateFromOvf(ovfEntityData.getOvfData());
                vmTemplateFromConfiguration = fullEntityOvfData.getVmTemplate();
                if (Guid.isNullOrEmpty(getParameters().getClusterId())) {
                    mapCluster(fullEntityOvfData);
                }
                vmTemplateFromConfiguration.setClusterId(getParameters().getClusterId());
                setVmTemplate(vmTemplateFromConfiguration);
                vmHandler.updateMaxMemorySize(getVmTemplate(), getEffectiveCompatibilityVersion());
                getParameters().setVmTemplate(vmTemplateFromConfiguration);
                getParameters().setDestDomainId(ovfEntityData.getStorageDomainId());
                getParameters().setSourceDomainId(ovfEntityData.getStorageDomainId());
                getParameters().setUserToRoles(fullEntityOvfData.getUserToRoles());

                // For quota, update disks when required
                if (getParameters().getDiskTemplateMap() != null) {
                    ArrayList<DiskImage> imageList = new ArrayList<>(getParameters().getDiskTemplateMap().values());
                    vmTemplateFromConfiguration.setDiskList(imageList);
                    ensureDomainMap(imageList, getParameters().getDestDomainId());
                }
                if (getParameters().getDomainMap() != null) {
                    getParameters().setDbUsers(drMappingHelper.mapDbUsers(fullEntityOvfData.getDbUsers(),
                            getParameters().getDomainMap()));
                } else {
                    getParameters().setDbUsers(fullEntityOvfData.getDbUsers());
                }
                if (getParameters().getRoleMap() != null) {
                    getParameters().setUserToRoles(drMappingHelper.mapRoles(getParameters().getRoleMap(),
                            getParameters().getUserToRoles()));
                } else {
                    getParameters().setUserToRoles(fullEntityOvfData.getUserToRoles());
                }
            } catch (OvfReaderException e) {
                log.error("Failed to parse a given ovf configuration: {}:\n{}",
                        e.getMessage(),
                        ovfEntityData.getOvfData());
                log.debug("Exception", e);
            }
        }
    }

    private void mapCluster(FullEntityOvfData fullEntityOvfData) {
        Cluster cluster =
                drMappingHelper.getMappedCluster(fullEntityOvfData.getClusterName(),
                        vmTemplateFromConfiguration.getId(),
                        getParameters().getClusterMap());
        if (cluster != null) {
            getParameters().setClusterId(cluster.getId());
        }
    }

    @Override
    protected void addPermissionsToDB() {
        if (templateDisksToAttach == null) {
            drMappingHelper.addPermissions(getParameters().getDbUsers(),
                    getParameters().getUserToRoles(),
                    getVmTemplateId(),
                    VdcObjectType.VmTemplate,
                    getParameters().getRoleMap());
        }
    }

    @Override
    protected ArrayList<DiskImage> getImages() {
        if (imagesList == null) {
            imagesList = new ArrayList<>(getParameters().getDiskTemplateMap() != null ?
                    getParameters().getDiskTemplateMap().values() : getVmTemplate().getDiskTemplateMap().values());
        }
        return imagesList;
    }

    @Override
    public void executeCommand() {
        updateVnicsFromMapping();
        super.executeCommand();
        addAuditLogForPartialVMs();
        if (templateDisksToAttach == null) {
            if (!getImages().isEmpty()) {
                findAndSaveDiskCopies();
                getImages().stream().map(DiskImage::getId).forEach(this::initQcowVersionForDisks);
            }
            unregisteredOVFDataDao.removeEntity(ovfEntityData.getEntityId(), null);
            unregisteredDisksDao.removeUnregisteredDiskRelatedToVM(ovfEntityData.getEntityId(), null);
        }
        setActionReturnValue(getVmTemplate().getId());
        setSucceeded(true);
    }

    @Override
    protected void addDisksToDb() {
        if (templateDisksToAttach == null) {
            super.addDisksToDb();
        } else {
            // TODO: compensation
            templateDisksToAttach.stream().map(disk -> disk.getDiskVmElements().iterator().next()).forEach(dve -> {
                VmDevice device = createVmDevice(dve);
                vmDeviceDao.save(device);
                getCompensationContext().snapshotNewEntity(device);
                diskVmElementDao.save(dve);
                getCompensationContext().snapshotNewEntity(dve);
            });
        }
    }

    protected VmDevice createVmDevice(DiskVmElement dve) {
        return new VmDevice(new VmDeviceId(dve.getDiskId(), getVmTemplateId()),
                VmDeviceGeneralType.DISK,
                VmDeviceType.DISK.getName(),
                "",
                null,
                true,
                dve.isPlugged(),
                dve.isReadOnly(),
                "",
                null,
                null,
                null);
    }

    private void addAuditLogForPartialVMs() {
        StringBuilder missingEntities = new StringBuilder();
        if (getParameters().isAllowPartialImport() && !failedDisksToImportForAuditLog.isEmpty()) {
            missingEntities.append("Disks: ");
            missingEntities.append(StringUtils.join(failedDisksToImportForAuditLog.values(), ", ") + " ");
        }
        if (!missingUsers.isEmpty()) {
            missingEntities.append("Users: ");
            missingEntities.append(StringUtils.join(missingUsers, ", ") + " ");
        }
        if (!missingRoles.isEmpty()) {
            missingEntities.append("Roles: ");
            missingEntities.append(StringUtils.join(missingRoles, ", ") + " ");
        }
        if (!missingVnicMappings.isEmpty()) {
            missingEntities.append("Vnic Mappings: ");
            missingEntities.append(StringUtils.join(missingVnicMappings, ", ") + " ");
        }

        if (missingEntities.length() > 0) {
            addCustomValue("MissingEntities", missingEntities.toString());
            auditLogDirector.log(this, AuditLogType.IMPORTEXPORT_PARTIAL_TEMPLATE_MISSING_ENTITIES);
        }
    }

    private void findAndSaveDiskCopies() {
        List<OvfEntityData> ovfEntityDataList =
                unregisteredOVFDataDao.getByEntityIdAndStorageDomain(ovfEntityData.getEntityId(), null);
        removeIrrelevantOvfs(ovfEntityDataList, getImages());
        List<ImageStorageDomainMap> copiedTemplateDisks = new LinkedList<>();
        ovfEntityDataList.forEach(ovfEntityData -> populateDisksCopies(
                copiedTemplateDisks,
                getImages(),
                ovfEntityData.getStorageDomainId()));
        saveImageStorageDomainMapList(copiedTemplateDisks);
    }

    /**
     * Removes from ovfEntityDataList objects that belong to
     * storage domains that don't contain any image from diskImages.
     */
    private void removeIrrelevantOvfs(List<OvfEntityData> ovfEntityDataList, List<DiskImage> diskImages) {
        Set<Guid> relevantSDGuids = getImages().stream()
                .map(DiskImage::getStorageIds)
                .flatMap(List::stream)
                .collect(Collectors.toSet());
        ovfEntityDataList.removeIf(ovfEntityData -> !relevantSDGuids.contains(ovfEntityData.getStorageDomainId()));
    }

    private void populateDisksCopies(List<ImageStorageDomainMap> copiedTemplateDisks,
            List<DiskImage> originalTemplateImages,
            Guid storageDomainId) {
        List<Guid> imagesContainedInStorageDomain = getImagesGuidFromStorage(storageDomainId, getStoragePoolId());
        for (DiskImage templateDiskImage : originalTemplateImages) {
            if (storageDomainId.equals(templateDiskImage.getStorageIds().get(0))) {
                // The original Storage Domain was already saved. skipping it.
                continue;
            }
            if (imagesContainedInStorageDomain.contains(templateDiskImage.getId())) {
                log.info("Found a copied image of '{}' on Storage Domain id '{}'",
                        templateDiskImage.getId(),
                        storageDomainId);
                ImageStorageDomainMap imageStorageDomainMap =
                        new ImageStorageDomainMap(templateDiskImage.getImageId(),
                                storageDomainId,
                                templateDiskImage.getQuotaId(),
                                templateDiskImage.getDiskProfileId());
                copiedTemplateDisks.add(imageStorageDomainMap);
            }
        }
    }

    private List<Guid> getImagesGuidFromStorage(Guid storageDomainId, Guid storagePoolId) {
        List<Guid> imagesList = Collections.emptyList();
        try {
            VDSReturnValue imagesListResult = runVdsCommand(VDSCommandType.GetImagesList,
                    new GetImagesListVDSCommandParameters(storageDomainId, storagePoolId));
            if (imagesListResult.getSucceeded()) {
                imagesList = (List<Guid>) imagesListResult.getReturnValue();
            } else {
                log.error("Unable to get images list for storage domain, can not update copied template disks related to Storage Domain id '{}'",
                        storageDomainId);
            }
        } catch (Exception e) {
            log.error("Unable to get images list for storage domain, can not update copied template disks related to Storage Domain id '{}'. error is: {}",
                    storageDomainId,
                    e);
        }
        return imagesList;
    }

    private void saveImageStorageDomainMapList(final List<ImageStorageDomainMap> copiedTemplateDisks) {
        if (!copiedTemplateDisks.isEmpty()) {
            TransactionSupport.executeInNewTransaction(() -> {
                copiedTemplateDisks.forEach(imageStorageDomainMapDao::save);
                return null;
            });
        }
    }

    private void removeInvalidUsers(ImportValidator importValidator) {
        if (getParameters().getDbUsers() == null || getParameters().getDbUsers().isEmpty()) {
            return;
        }

        log.info("Checking for missing users");
        List<DbUser> dbMissingUsers = importValidator.findMissingUsers(getParameters().getDbUsers());
        getParameters().getDbUsers().removeAll(dbMissingUsers);
        missingUsers = dbMissingUsers
                .stream()
                .map(dbUser -> String.format("%s@%s", dbUser.getLoginName(), dbUser.getDomain()))
                .collect(Collectors.toList());
    }

    private void removeInavlidRoles(ImportValidator importValidator) {
        if (MapUtils.isEmpty(getParameters().getUserToRoles())) {
            return;
        }

        log.info("Checking for missing roles");
        Set<String> candidateRoles = getParameters().getUserToRoles().entrySet()
                .stream()
                .flatMap(userToRoles -> userToRoles.getValue().stream())
                .collect(Collectors.toSet());
        missingRoles = importValidator.findMissingEntities(candidateRoles, val -> roleDao.getByName(val));
        getParameters().getUserToRoles().forEach((k, v) -> v.removeAll(missingRoles));
    }

    private void initQcowVersionForDisks(Guid imageGroupId) {
        List<DiskImage> diskVolumes = diskImageDao.getAllSnapshotsForImageGroup(imageGroupId);
        diskVolumes.stream().filter(volume -> volume.getVolumeFormat() == VolumeFormat.COW).forEach(volume -> {
            try {
                setQcowCompat(volume);
            } catch (Exception e) {
                log.error("Could not set qcow compat version for disk '{} with id '{}/{}'",
                        volume.getDiskAlias(),
                        volume.getId(),
                        volume.getImageId());
            }
        });
    }

    protected void setQcowCompat(DiskImage diskImage) {
        diskImage.setQcowCompat(QcowCompat.QCOW2_V2);
        QemuImageInfo qemuImageInfo = getQemuImageInfo(diskImage, diskImage.getStorageIds().get(0));
        if (qemuImageInfo != null) {
            diskImage.setQcowCompat(qemuImageInfo.getQcowCompat());
        }
        imageDao.update(diskImage.getImage());
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.TEMPLATE_IMPORT_FROM_CONFIGURATION_SUCCESS :
            AuditLogType.TEMPLATE_IMPORT_FROM_CONFIGURATION_FAILED;
    }

    @Override
    protected void updateDiskSizeByQcowImageInfo(DiskImage diskImage, Guid storageId) {
        if (!Guid.isNullOrEmpty(storageId)) {
            super.updateDiskSizeByQcowImageInfo(diskImage, storageId);
            return;
        }
        // otherwise, we have an image whose storage domain(s) is unknown at this point
        // this may happen when getting here with a template that was read from an OVF
        // and since the image id in the database may be different than the one specified
        // within the OVF (e.g., when the image id is generate during upload image),
        // let's take the storage domain of the disk's image that was queried
        // from the database.
        super.updateDiskSizeByQcowImageInfo(diskImage, diskIdToStorageDomainId.get(diskImage.getId()));
    }
}
