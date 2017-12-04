package org.ovirt.engine.core.bll.validator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.Backend;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.storage.disk.image.ImagesHandler;
import org.ovirt.engine.core.bll.validator.storage.DiskImagesValidator;
import org.ovirt.engine.core.bll.validator.storage.MultipleStorageDomainsValidator;
import org.ovirt.engine.core.bll.validator.storage.StorageDomainValidator;
import org.ovirt.engine.core.common.action.ImportParameters;
import org.ovirt.engine.core.common.businessentities.OvfEntityData;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSParametersBase;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.DiskImageDao;
import org.ovirt.engine.core.dao.StorageDomainDao;
import org.ovirt.engine.core.dao.StoragePoolDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImportValidator {

    private ImportParameters params;

    private StoragePool cachedStoragePool;
    private StorageDomain cachedStorageDomain;

    public ImportValidator(ImportParameters params) {
        this.params = params;
    }

    protected Logger log = LoggerFactory.getLogger(getClass());

    /**
     * Validate the existence of the VM's disks. If a disk already exist in the engine,
     * the engine should fail the validation, unless the allowPartial flag is true. Once the
     * allowPartial flag is true the operation should not fail and the disk will be removed from the VM's disk list and
     * also from the imageToDestinationDomainMap, so the operation will pass the execute phase and import the VM
     * partially without the invalid disks.
     *
     * @param images
     *            - The images list to validate their storage domains. This list might be filtered if the allowPartial
     *            flag is true
     * @param allowPartial
     *            - Flag which determine if the VM can be imported partially.
     * @param imageToDestinationDomainMap
     *            - Map from src storage to dst storage which might be filtered if the allowPartial flag is true.
     * @return - The validation result.
     */
    public ValidationResult validateDiskNotAlreadyExistOnDB(List<DiskImage> images,
            boolean allowPartial,
            Map<Guid, Guid> imageToDestinationDomainMap,
            Map<Guid, String> failedDisksToImport) {
        // Creating new ArrayList in order to manipulate the original List and remove the existing disks
        for (DiskImage image : new ArrayList<>(images)) {
            DiskImage diskImage = getDiskImageDao().get(image.getImageId());
            if (diskImage != null) {
                log.info("Disk '{}' with id '{}', already exist on storage domain '{}'",
                        diskImage.getDiskAlias(),
                        diskImage.getImageId(),
                        diskImage.getStoragesNames().get(0));
                if (!allowPartial) {
                    return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_IMPORT_DISKS_ALREADY_EXIST,
                            String.format("$diskAliases %s", diskImage.getDiskAlias()));
                }
                failedDisksToImport.putIfAbsent(image.getId(), image.getDiskAlias());
                imageToDestinationDomainMap.remove(image.getId());
                images.remove(image);
            }
        }
        return ValidationResult.VALID;
    }

    /**
     * Validate the storage domains' existence of the VM's disks. If a storage domain will fail to be active or does not
     * exist in the engine, the engine should fail the validation, unless the allowPartial flag is true. Once the
     * allowPartial flag is true the operation should not fail and the disk will be removed from the VM's disk list and
     * also from the imageToDestinationDomainMap, so the operation will pass the execute phase and import the VM
     * partially without the invalid disks.
     *
     * @param images
     *            - The images list to validate their storage domains. This list might be filtered if the allowPartial
     *            flag is true
     * @param allowPartial
     *            - Flag which determine if the VM can be imported partially.
     * @param imageToDestinationDomainMap
     *            - Map from src storage to dst storage which might be filtered if the allowPartial flag is true.
     * @return - The validation result.
     */
    public ValidationResult validateStorageExistForUnregisteredEntity(List<DiskImage> images,
            boolean allowPartial,
            Map<Guid, Guid> imageToDestinationDomainMap,
            Map<Guid, String> failedDisksToImport) {
        for (DiskImage image : new ArrayList<>(images)) {
            StorageDomain sd = getStorageDomainDao().getForStoragePool(
                    image.getStorageIds().get(0), getStoragePool().getId());
            ValidationResult result = new StorageDomainValidator(sd).isDomainExistAndActive();
            if (!result.isValid()) {
                log.error("Storage Domain '{}' with id '{}', could not be found for disk alias '{}' with image id '{}'",
                        sd == null ? null : sd.getStorageName(),
                        image.getStorageIds().get(0),
                        image.getDiskAlias(),
                        image.getId());
                if (!allowPartial) {
                    return result;
                }
                failedDisksToImport.putIfAbsent(image.getId(), image.getDiskAlias());
                imageToDestinationDomainMap.remove(image.getId());
                images.remove(image);
            }
        }
        return ValidationResult.VALID;
    }

    public ValidationResult validateStorageExistsForMemoryDisks(List<Snapshot> snapshots,
            boolean allowPartial,
            Map<Guid, String> failedDisksToImport) {
        for (Snapshot snap : snapshots) {
            if (StringUtils.isNotEmpty(snap.getMemoryVolume())) {
                List<Guid> guids = Guid.createGuidListFromString(snap.getMemoryVolume());
                StorageDomain sd = getStorageDomainDao().getForStoragePool(guids.get(0), params.getStoragePoolId());
                ValidationResult result = new StorageDomainValidator(sd).isDomainExistAndActive();
                if (!result.isValid()) {
                    log.error("Storage Domain '{}', could not be found for memory disks: '{}','{}' in snapshot '{}' (id: '{}')",
                            guids.get(0),
                            guids.get(2),
                            guids.get(4),
                            snap.getDescription(),
                            snap.getId());
                    if (!allowPartial) {
                        return result;
                    }
                    failedDisksToImport.putIfAbsent(guids.get(2), snap.getDescription() + "(memory disk)");
                }
            }
        }
        return ValidationResult.VALID;
    }

    public ValidationResult validateUnregisteredEntity(OvfEntityData ovfEntityData) {
        if (ovfEntityData == null && !params.isImportAsNewEntity()) {
            return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_UNSUPPORTED_OVF);
        }

        if (!getStorageDomain().getStorageDomainType().isDataDomain()) {
            return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_TYPE_UNSUPPORTED,
                    String.format("$domainId %1$s", params.getStorageDomainId()),
                    String.format("$domainType %1$s", getStorageDomain().getStorageDomainType()));
        }

        return ValidationResult.VALID;
    }

    public ValidationResult verifyDisks(Iterable<DiskImage> imageList, Map<Guid, Guid> imageToDestinationDomainMap) {
        if (!params.isImportAsNewEntity() && !params.isImagesExistOnTargetStorageDomain()) {
            return new DiskImagesValidator(imageList).diskImagesOnStorage(imageToDestinationDomainMap, params.getStoragePoolId());
        }

        return ValidationResult.VALID;
    }

    public ValidationResult validateSpaceRequirements(Collection<DiskImage> diskImages) {
        MultipleStorageDomainsValidator sdValidator = createMultipleStorageDomainsValidator(diskImages);
        ValidationResult result = sdValidator.allDomainsExistAndActive();
        if (!result.isValid()) {
            return result;
        }

        result = sdValidator.allDomainsWithinThresholds();
        if (!result.isValid()) {
            return result;
        }

        if (params.getCopyCollapse()) {
            return sdValidator.allDomainsHaveSpaceForClonedDisks(diskImages);
        }

        return sdValidator.allDomainsHaveSpaceForDisksWithSnapshots(diskImages);
    }

    public MultipleStorageDomainsValidator createMultipleStorageDomainsValidator(Collection<DiskImage> diskImages) {
        return new MultipleStorageDomainsValidator(params.getStoragePoolId(),
                ImagesHandler.getAllStorageIdsForImageIds(diskImages));
    }

    protected VDSReturnValue runVdsCommand(VDSCommandType commandType, VDSParametersBase parameters)
            throws EngineException {
        return Backend.getInstance().getResourceManager().runVdsCommand(commandType, parameters);
    }

    protected StorageDomain getStorageDomain(Guid domainId) {
        return getStorageDomainDao().getForStoragePool(domainId, getStoragePool().getId());
    }

    public StorageDomainDao getStorageDomainDao() {
        return DbFacade.getInstance().getStorageDomainDao();
    }

    public DiskImageDao getDiskImageDao() {
        return DbFacade.getInstance().getDiskImageDao();
    }

    protected StoragePoolDao getStoragePoolDao() {
        return DbFacade.getInstance().getStoragePoolDao();
    }

    public StoragePool getStoragePool() {
        if (cachedStoragePool == null) {
            cachedStoragePool = getStoragePoolDao().get(params.getStoragePoolId());
        }
        return cachedStoragePool;
    }

    public StorageDomain getStorageDomain() {
        if (cachedStorageDomain == null) {
            cachedStorageDomain = getStorageDomainDao().get(params.getStorageDomainId());
        }
        return cachedStorageDomain;
    }
}
