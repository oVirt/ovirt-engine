package org.ovirt.engine.core.bll.validator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

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
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.scheduling.AffinityGroup;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DbUserDao;
import org.ovirt.engine.core.dao.DiskDao;
import org.ovirt.engine.core.dao.DiskImageDao;
import org.ovirt.engine.core.dao.StorageDomainDao;
import org.ovirt.engine.core.dao.StoragePoolDao;
import org.ovirt.engine.core.di.Injector;
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
                    image.getStorageIds().get(0), params.getStoragePoolId());
            ValidationResult result = new StorageDomainValidator(sd).isDomainExistAndActive();
            if (!result.isValid()) {
                log.error("Storage Domain '{}' with id '{}', could not be found for disk alias '{}' with image id '{}'",
                        sd == null ? "unknown" : sd.getStorageName(),
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
            if (snap.containsMemory()) {
                DiskImage memoryDump = (DiskImage) getDiskDao().get(snap.getMemoryDiskId());
                // If a memory disk is not found in the DB there will be an attempt to import it from the domain
                if (memoryDump == null) {
                    return ValidationResult.VALID;
                }
                StorageDomain dumpSd = getStorageDomainDao().getForStoragePool(memoryDump.getStorageIds().get(0), params.getStoragePoolId());
                ValidationResult dumpSdResult = new StorageDomainValidator(dumpSd).isDomainExistAndActive();
                if (!handleStorageValidationResult(dumpSdResult, memoryDump, snap, failedDisksToImport) && !allowPartial) {
                    return dumpSdResult;
                }

                DiskImage memoryConf = (DiskImage) getDiskDao().get(snap.getMetadataDiskId());
                // If a memory disk is not found in the DB there will be an attempt to import it from the domain
                if (memoryConf == null) {
                    return ValidationResult.VALID;
                }
                StorageDomain confSd = getStorageDomainDao().getForStoragePool(memoryConf.getStorageIds().get(0), params.getStoragePoolId());
                ValidationResult confSdResult = new StorageDomainValidator(confSd).isDomainExistAndActive();
                if (!handleStorageValidationResult(confSdResult, memoryConf, snap, failedDisksToImport) && !allowPartial) {
                    return confSdResult;
                }
            }
        }
        return ValidationResult.VALID;
    }

    private boolean handleStorageValidationResult(ValidationResult result, DiskImage disk, Snapshot snapshot,
            Map<Guid, String> failedDisksToImport) {
        if (!result.isValid()) {
            log.error("Storage Domain '{}' (id: '{}'), could not be found for memory disk: '{}' (id: '{}') in snapshot '{}' (id: '{}')",
                    disk.getStoragesNames().get(0),
                    disk.getStorageIds().get(0),
                    disk.getName(),
                    disk.getId(),
                    snapshot.getDescription(),
                    snapshot.getId());
            failedDisksToImport.putIfAbsent(disk.getId(), snapshot.getDescription() + "(memory disk)");
            return false;
        }
        return true;
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

    public ValidationResult verifyDisks(Collection<DiskImage> imageList, Map<Guid, Guid> imageToDestinationDomainMap) {
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

    protected StorageDomain getStorageDomain(Guid domainId) {
        return getStorageDomainDao().getForStoragePool(domainId, getStoragePool().getId());
    }

    public StorageDomainDao getStorageDomainDao() {
        return Injector.get(StorageDomainDao.class);
    }

    public DiskImageDao getDiskImageDao() {
        return Injector.get(DiskImageDao.class);
    }

    public DiskDao getDiskDao() {
        return Injector.get(DiskDao.class);
    }

    protected StoragePoolDao getStoragePoolDao() {
        return Injector.get(StoragePoolDao.class);
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

    public <T extends String, R> List<T> findMissingEntities(Collection<T> collection, Function<T, R> getterFunction) {
        List<T> missingEntities = new ArrayList<>();

        collection.forEach(entity -> {
            if (getterFunction.apply(entity) == null) {
                missingEntities.add(entity);
            }
        });

        if (!missingEntities.isEmpty()) {
            log.warn("Missing entities found: {}", missingEntities
                    .stream()
                    .collect(Collectors.joining(", ")));
        }

        return missingEntities;
    }

    public List<AffinityGroup> findFaultyAffinityGroups(List<AffinityGroup> affinityGroups, Guid clusterId) {
        List<AffinityGroup> faultyAffinityGroups = new ArrayList<>(affinityGroups.size());
        for (AffinityGroup affinityGroup : affinityGroups) {
            if (!clusterId.equals(affinityGroup.getClusterId())) {
                log.warn("Affinity group {} for cluster id {} does not match VM cluster id {}",
                        affinityGroup.getName(),
                        affinityGroup.getClusterId(),
                        clusterId);
                faultyAffinityGroups.add(affinityGroup);
            }
        }

        return faultyAffinityGroups;
    }

    public List<DbUser> findMissingUsers(Set<DbUser> usersToAdd) {
        List<DbUser> missingUsers = new ArrayList<>();

        usersToAdd.forEach(dbUser -> {
            DbUser userToAdd = getDbUserDao().getByUsernameAndDomain(dbUser.getLoginName(), dbUser.getDomain());
            if (userToAdd == null) {
                missingUsers.add(dbUser);
            }
        });

        if (!missingUsers.isEmpty()) {
            log.warn("Missing users: {}", missingUsers
                    .stream()
                    .map(dbUser -> String.format("%s@%s", dbUser.getName(), dbUser.getDomain()))
                    .collect(Collectors.joining(", ")));
        }

        return missingUsers;
    }

    private DbUserDao getDbUserDao() {
        return Injector.get(DbUserDao.class);
    }

}
