package org.ovirt.engine.core.bll.exportimport;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

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
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.FullEntityOvfData;
import org.ovirt.engine.core.common.businessentities.storage.ImageStorageDomainMap;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.utils.CompatibilityVersionUtils;
import org.ovirt.engine.core.common.vdscommands.GetImageInfoVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.GetImagesListVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.ImageStorageDomainMapDao;
import org.ovirt.engine.core.dao.StorageDomainDao;
import org.ovirt.engine.core.dao.UnregisteredDisksDao;
import org.ovirt.engine.core.dao.UnregisteredOVFDataDao;
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

    @Inject
    private AuditLogDirector auditLogDirector;

    @Inject
    private ClusterDao clusterDao;
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
    private ExternalVnicProfileMappingValidator externalVnicProfileMappingValidator;

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
        initVmTemplate();
        if (!super.validate()) {
            return false;
        }
        if (!validateExternalVnicProfileMapping()) {
            return false;
        }
        drMappingHelper.mapVnicProfiles(vmTemplateFromConfiguration.getInterfaces(),
                getParameters().getExternalVnicProfileMappings());
        ArrayList<DiskImage> disks = new ArrayList(getVmTemplate().getDiskTemplateMap().values());
        setImagesWithStoragePoolId(getStorageDomain().getStoragePoolId(), disks);
        getVmTemplate().setImages(disks);
        if (getParameters().isImagesExistOnTargetStorageDomain() &&
                !validateUnregisteredEntity(vmTemplateFromConfiguration, ovfEntityData)) {
            return false;
        }
        return true;
    }

    private boolean validateExternalVnicProfileMapping() {
        final ValidationResult validationResult =
                externalVnicProfileMappingValidator.validateExternalVnicProfileMapping(
                        getParameters().getExternalVnicProfileMappings(),
                        getParameters().getClusterId());
        return validate(validationResult);
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

        for (DiskImage image : new ArrayList<>(getImages())) {
            DiskImage fromIrs = null;
            Guid storageDomainId = image.getStorageIds().get(0);
            Guid imageGroupId = image.getId() != null ? image.getId() : Guid.Empty;
            try {
                fromIrs = (DiskImage) runVdsCommand(VDSCommandType.GetImageInfo,
                        new GetImageInfoVDSCommandParameters(getStoragePool().getId(),
                                storageDomainId,
                                imageGroupId,
                                image.getImageId())).getReturnValue();
            } catch (Exception e) {
                log.debug("Unable to get image info from storage", e);
            }
            if (fromIrs == null) {
                if (!getParameters().isAllowPartialImport()) {
                    return failValidation(EngineMessage.TEMPLATE_IMAGE_NOT_EXIST);
                }
                log.warn("Disk image '{}/{}' doesn't exist on storage domain '{}'. Ignoring since force flag in on",
                        imageGroupId,
                        image.getImageId(),
                        storageDomainId);
                getImages().remove(image);
                failedDisksToImportForAuditLog.putIfAbsent(image.getId(), image.getDiskAlias());
            }
        }
        for (DiskImage image : getImages()) {
            StorageDomain sd = storageDomainDao.getForStoragePool(
                    image.getStorageIds().get(0), getStoragePool().getId());
            if (!validate(new StorageDomainValidator(sd).isDomainExistAndActive())) {
                return false;
            }
        }
        if (!getStorageDomain().getStorageDomainType().isDataDomain()) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_TYPE_UNSUPPORTED,
                    String.format("$domainId %1$s", getParameters().getStorageDomainId()),
                    String.format("$domainType %1$s", getStorageDomain().getStorageDomainType()));
        }
        return true;
    }

    private void setImagesWithStoragePoolId(Guid storagePoolId, List<DiskImage> diskImages) {
        for (DiskImage diskImage : diskImages) {
            diskImage.setStoragePoolId(storagePoolId);
        }
    }

    private void initVmTemplate() {
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
                setEffectiveCompatibilityVersion(CompatibilityVersionUtils.getEffective(getVmTemplate(), this::getCluster));
                vmHandler.updateMaxMemorySize(getVmTemplate(), getEffectiveCompatibilityVersion());
                getParameters().setVmTemplate(vmTemplateFromConfiguration);
                getParameters().setDestDomainId(ovfEntityData.getStorageDomainId());
                getParameters().setSourceDomainId(ovfEntityData.getStorageDomainId());
                getParameters().setDbUsers(fullEntityOvfData.getDbUsers());
                getParameters().setUserToRoles(fullEntityOvfData.getUserToRoles());

                // For quota, update disks when required
                if (getParameters().getDiskTemplateMap() != null) {
                    ArrayList imageList = new ArrayList<>(getParameters().getDiskTemplateMap().values());
                    vmTemplateFromConfiguration.setDiskList(imageList);
                    ensureDomainMap(imageList, getParameters().getDestDomainId());
                }
            } catch (OvfReaderException e) {
                log.error("Failed to parse a given ovf configuration: {}:\n{}",
                        e.getMessage(),
                        ovfEntityData.getOvfData());
                log.debug("Exception", e);
            }
        }
        setClusterId(getParameters().getClusterId());
        if (getCluster() != null) {
            setStoragePoolId(getCluster().getStoragePoolId());
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
    protected void mapDbUsers() {
        drMappingHelper.mapDbUsers(getParameters().getDomainMap(),
                getParameters().getDbUsers(),
                getParameters().getUserToRoles(),
                getVmTemplateId(),
                VdcObjectType.VmTemplate,
                getParameters().getRoleMap());
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
        super.executeCommand();
        addAuditLogForPartialVMs();
        if (getParameters().isImagesExistOnTargetStorageDomain()) {
            if (!getImages().isEmpty()) {
                findAndSaveDiskCopies();
                getImages().stream().forEach(diskImage -> {
                    initQcowVersionForDisks(diskImage.getId());
                });
            }
            unregisteredOVFDataDao.removeEntity(ovfEntityData.getEntityId(), null);
            unregisteredDisksDao.removeUnregisteredDiskRelatedToVM(ovfEntityData.getEntityId(), null);
        }
        setActionReturnValue(getVmTemplate().getId());
        setSucceeded(true);
    }

    private void addAuditLogForPartialVMs() {
        if (getParameters().isAllowPartialImport() && !failedDisksToImportForAuditLog.isEmpty()) {
            addCustomValue("DiskAliases", StringUtils.join(failedDisksToImportForAuditLog.values(), ", "));
            auditLogDirector.log(this, AuditLogType.IMPORTEXPORT_PARTIAL_TEMPLATE_DISKS_NOT_EXISTS);
        }
    }

    private void findAndSaveDiskCopies() {
        List<OvfEntityData> ovfEntityDataList =
                unregisteredOVFDataDao.getByEntityIdAndStorageDomain(ovfEntityData.getEntityId(), null);
        List<ImageStorageDomainMap> copiedTemplateDisks = new LinkedList<>();
        for (OvfEntityData ovfEntityDataFetched : ovfEntityDataList) {
            populateDisksCopies(copiedTemplateDisks,
                    getImages(),
                    ovfEntityDataFetched.getStorageDomainId());
        }
        saveImageStorageDomainMapList(copiedTemplateDisks);
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
                for (ImageStorageDomainMap imageStorageDomainMap : copiedTemplateDisks) {
                    imageStorageDomainMapDao.save(imageStorageDomainMap);
                }
                return null;
            });
        }
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.TEMPLATE_IMPORT_FROM_CONFIGURATION_SUCCESS :
                AuditLogType.TEMPLATE_IMPORT_FROM_CONFIGURATION_FAILED;
    }
}
