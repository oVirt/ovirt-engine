package org.ovirt.engine.core.bll.validator.storage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.SubchainInfo;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.StorageDomainDao;
import org.ovirt.engine.core.di.Injector;

/**
 * A validator for multiple storage domains.
 *
 * This class offers several validations similar to those offered in
 * {@link StorageDomainValidator} which can be performed on several domains.
 *
 * The guideline of this class is performance and short circuit logic, so any
 * of these validations will fail on the first storage domain which fails
 * validation, and the others will not be inspected.
 */
public class MultipleStorageDomainsValidator {

    /** The ID of the storage pool all the domains belong to */
    private Guid storagePoolId;

    /** A map from the ids of each domain being validated to its validator */
    private Map<Guid, StorageDomainValidator> domainValidators;

    /**
     * Constructor from Guids
     * @param sdIds A {@link Collection} of storage domain IDs to be validated
     */
    public MultipleStorageDomainsValidator(Guid storagePoolId, Collection<Guid> sdIds) {
        this.storagePoolId = storagePoolId;
        domainValidators = new HashMap<>();
        for (Guid id : sdIds) {
            domainValidators.put(id, null);
        }
    }

    private Map<Guid, List<DiskImage>> getDomainsDisksMap(Collection<DiskImage> diskImages) {
        Map<Guid, List<DiskImage>> domainsDisksMap = new HashMap<>();
        for (DiskImage disk : diskImages) {
            List<Guid> domainIds = disk.getStorageIds();
            for (Guid domainId : domainIds) {
                domainsDisksMap.computeIfAbsent(domainId, k -> new ArrayList<>()).add(disk);
            }
        }
        return domainsDisksMap;
    }

    /**
     * Validates that all the domains exist and are active.
     * @return {@link ValidationResult#VALID} if all the domains are OK, or a {@link ValidationResult} with the first non-active domain encountered.
     */
    public ValidationResult allDomainsExistAndActive() {
        return validOrFirstFailure(entry -> getStorageDomainValidator(entry).isDomainExistAndActive());
    }

    /**
     * Validates that all the domains are not in backup mode
     * @return {@link ValidationResult#VALID} if all the domains are OK, or a {@link ValidationResult} with the first non-backup domain encountered.
     */
    public ValidationResult allDomainsNotBackupDomains() {
        return validOrFirstFailure(entry -> getStorageDomainValidator(entry).isNotBackupDomain());
    }

    /**
     * Validates that all the domains are within free disk space threshold.
     * @return {@link ValidationResult#VALID} if all the domains are OK, or a {@link ValidationResult} with the first low space domain encountered.
     */
    public ValidationResult allDomainsWithinThresholds() {
        return validOrFirstFailure(entry -> getStorageDomainValidator(entry).isDomainWithinThresholds());
    }

    /**
     * Validates that all the domains have enough space for the request
     * @return {@link ValidationResult#VALID} if all the domains have enough free space, or a {@link ValidationResult} with the first low-on-space domain encountered.
     */
    public ValidationResult allDomainsHaveSpaceForNewDisks(Collection<DiskImage> disksImages) {
        final Map<Guid, List<DiskImage>> disksMap = getDomainsDisksMap(disksImages);

        return validOrFirstFailure(entry -> {
            Guid sdId = entry.getKey();
            List<DiskImage> disksList = disksMap.get(sdId);
            return getStorageDomainValidator(entry).hasSpaceForNewDisks(disksList);
        });
    }

    /**
     * Validates that all the domains have enough space for the request
     * @return {@link ValidationResult#VALID} if all the domains have enough free space, or a {@link ValidationResult} with the first low-on-space domain encountered.
     */
    public ValidationResult allDomainsHaveSpaceForClonedDisks(Collection<DiskImage> diskImages) {
        final Map<Guid, List<DiskImage>> disksMap = getDomainsDisksMap(diskImages);

        return validOrFirstFailure(entry -> {
            Guid sdId = entry.getKey();
            List<DiskImage> disksList = disksMap.get(sdId);
            return getStorageDomainValidator(entry).hasSpaceForClonedDisks(disksList);
        });
    }

    /**
     * Validates that all the domains have enough space for the request
     * @return {@link ValidationResult#VALID} if all the domains have enough free space, or a {@link ValidationResult} with the first low-on-space domain encountered.
     */
    public ValidationResult allDomainsHaveSpaceForMerge(List<SubchainInfo> snapshots, ActionType snapshotActionType) {
        // Make sure the subchain was built correctly, in case we have orphaned volumes in the chain
        // it could fail with NPE
        if (snapshots.isEmpty()) {
            return new ValidationResult(EngineMessage.ERROR_CANNOT_REMOVE_SNAPSHOT_ILLEGAL_IMAGE);
        }

        final Map<Guid, List<SubchainInfo>> storageToSnapshots = getDomainsToSnapshotsMap(snapshots);

        return validOrFirstFailure(entry -> {
            Guid sdId = entry.getKey();
            List<SubchainInfo> subchain = storageToSnapshots.get(sdId);
            return getStorageDomainValidator(entry).hasSpaceForMerge(subchain, snapshotActionType);
        });
    }

    /**
     * Validates that all the domains have enough space for the request
     * @return {@link ValidationResult#VALID} if all the domains have enough free space, or a {@link ValidationResult} with the first low-on-space domain encountered.
     */
    public ValidationResult allDomainsHaveSpaceForAllDisks(List<DiskImage> newDiskImages, List<DiskImage> clonedDiskImages) {
        final Map<Guid, List<DiskImage>> domainsNewDisksMap = getDomainsDisksMap(newDiskImages);
        final Map<Guid, List<DiskImage>> domainsClonedDisksMap = getDomainsDisksMap(clonedDiskImages);

        return validOrFirstFailure(entry -> {
            Guid sdId = entry.getKey();
            List<DiskImage> newDisksForDomain = domainsNewDisksMap.get(sdId);
            List<DiskImage> clonedDisksForDomain = domainsClonedDisksMap.get(sdId);
            return getStorageDomainValidator(entry).hasSpaceForAllDisks(newDisksForDomain, clonedDisksForDomain);
        });
    }

    /**
     * Validates that all the domains have enough space for the request
     * @return {@link ValidationResult#VALID} if all the domains have enough free space, or a {@link ValidationResult} with the first low-on-space domain encountered.
     */
    public ValidationResult allDomainsHaveSpaceForDisksWithSnapshots(Collection<DiskImage> diskImages) {
        final Map<Guid, List<DiskImage>> disksMap = getDomainsDisksMap(diskImages);

        return validOrFirstFailure(entry -> {
            Guid sdId = entry.getKey();
            List<DiskImage> diskList = disksMap.get(sdId);
            return getStorageDomainValidator(entry).hasSpaceForDisksWithSnapshots(diskList);
        });
    }

    /**
     * Validates if there is a managed block storage domain and if it supports the operation.
     * @return {@link ValidationResult#VALID} if there are managed block storage domains that support the operation,
     * or a {@link ValidationResult} in case there is a managed block storage domain that doesn't support the operation.
     */
    public ValidationResult isSupportedByManagedBlockStorageDomains(ActionType actionType) {
        return validOrFirstFailure(entry -> {
            StorageDomainValidator storageDomainValidator = getStorageDomainValidator(entry);
            return storageDomainValidator.isManagedBlockStorage() ?
                    ManagedBlockStorageDomainValidator.isOperationSupportedByManagedBlockStorage(actionType) :
                    ValidationResult.VALID;
        });
    }

    /** @return The lazy-loaded validator for the given map entry */
    protected StorageDomainValidator getStorageDomainValidator(Map.Entry<Guid, StorageDomainValidator> entry) {
        if (entry.getValue() == null) {
            StorageDomain storageDomain = getStorageDomainDao().getForStoragePool(entry.getKey(), storagePoolId);
            entry.setValue(new StorageDomainValidator(storageDomain));
        }

        return entry.getValue();
    }

    /** @return The Dao object used to retrieve storage domains */
    public StorageDomainDao getStorageDomainDao() {
        return Injector.get(StorageDomainDao.class);
    }

    /**
     * Validates all the storage domains by a given predicate.
     *
     * @return {@link ValidationResult#VALID} if all the domains are OK, or the
     * first validation error if they aren't.
     */
    private ValidationResult validOrFirstFailure
        (Function<Map.Entry<Guid, StorageDomainValidator>, ValidationResult> predicate) {
        return domainValidators.entrySet()
                .stream()
                .map(predicate)
                .filter(v -> !v.isValid())
                .findFirst()
                .orElse(ValidationResult.VALID);
    }

    private Map<Guid, List<SubchainInfo>> getDomainsToSnapshotsMap(List<SubchainInfo> snapshots) {
        return snapshots.stream().collect(Collectors.groupingBy(SubchainInfo::getStorageDomainId));
    }
}
