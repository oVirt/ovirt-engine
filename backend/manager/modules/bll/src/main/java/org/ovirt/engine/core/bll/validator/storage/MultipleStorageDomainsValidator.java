package org.ovirt.engine.core.bll.validator.storage;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.StorageDomainDao;
import org.ovirt.engine.core.utils.collections.MultiValueMapUtils;

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
                MultiValueMapUtils.addToMap(domainId, disk, domainsDisksMap);
            }
        }
        return domainsDisksMap;
    }

    /**
     * Validates that all the domains exist and are active.
     * @return {@link ValidationResult#VALID} if all the domains are OK, or a {@link ValidationResult} with the first non-active domain encountered.
     */
    public ValidationResult allDomainsExistAndActive() {
        return validOrFirstFailure(new ValidatorPredicate() {
            @Override
            public ValidationResult evaluate(Map.Entry<Guid, StorageDomainValidator> entry) {
                return getStorageDomainValidator(entry).isDomainExistAndActive();
            }
        });
    }

    /**
     * Validates that all the domains are within free disk space threshold.
     * @return {@link ValidationResult#VALID} if all the domains are OK, or a {@link ValidationResult} with the first low space domain encountered.
     */
    public ValidationResult allDomainsWithinThresholds() {
        return validOrFirstFailure(new ValidatorPredicate() {
            @Override
            public ValidationResult evaluate(Map.Entry<Guid, StorageDomainValidator> entry) {
                return getStorageDomainValidator(entry).isDomainWithinThresholds();
            }
        });
    }

    /**
     * Validates that all the domains have enough space for the request
     * @return {@link ValidationResult#VALID} if all the domains have enough free space, or a {@link ValidationResult} with the first low-on-space domain encountered.
     */
    public ValidationResult allDomainsHaveSpaceForNewDisks(Collection<DiskImage> disksList) {
        final Map<Guid, List<DiskImage>> disksMap = getDomainsDisksMap(disksList);

        return validOrFirstFailure(new ValidatorPredicate() {
            @Override
            public ValidationResult evaluate(Map.Entry<Guid, StorageDomainValidator> entry) {
                Guid sdId = entry.getKey();
                List<DiskImage> disksList = disksMap.get(sdId);
                return getStorageDomainValidator(entry).hasSpaceForNewDisks(disksList);
            }
        });
    }

    /**
     * Validates that all the domains have enough space for the request
     * @return {@link ValidationResult#VALID} if all the domains have enough free space, or a {@link ValidationResult} with the first low-on-space domain encountered.
     */
    public ValidationResult allDomainsHaveSpaceForClonedDisks(Collection<DiskImage> diskImages) {
        final Map<Guid, List<DiskImage>> disksMap = getDomainsDisksMap(diskImages);

        return validOrFirstFailure(new ValidatorPredicate() {
            @Override
            public ValidationResult evaluate(Map.Entry<Guid, StorageDomainValidator> entry) {
                Guid sdId = entry.getKey();
                List<DiskImage> disksList = disksMap.get(sdId);
                return getStorageDomainValidator(entry).hasSpaceForClonedDisks(disksList);
            }
        });
    }

    /**
     * Validates that all the domains have enough space for the request
     * @return {@link ValidationResult#VALID} if all the domains have enough free space, or a {@link ValidationResult} with the first low-on-space domain encountered.
     */
    public ValidationResult allDomainsHaveSpaceForAllDisks(List<DiskImage> newDiskImages, List<DiskImage> clonedDiskImages) {
        final Map<Guid, List<DiskImage>> domainsNewDisksMap = getDomainsDisksMap(newDiskImages);
        final Map<Guid, List<DiskImage>> domainsClonedDisksMap = getDomainsDisksMap(clonedDiskImages);

        return validOrFirstFailure(new ValidatorPredicate() {
            @Override
            public ValidationResult evaluate(Map.Entry<Guid, StorageDomainValidator> entry) {
                Guid sdId = entry.getKey();
                List<DiskImage> newDisksForDomain = domainsNewDisksMap.get(sdId);
                List<DiskImage> clonedDisksForDomain = domainsClonedDisksMap.get(sdId);
                return getStorageDomainValidator(entry).hasSpaceForAllDisks(newDisksForDomain, clonedDisksForDomain);
            }
        });
    }

    /**
     * Validates that all the domains have enough space for the request
     * @return {@link ValidationResult#VALID} if all the domains have enough free space, or a {@link ValidationResult} with the first low-on-space domain encountered.
     */
    public ValidationResult allDomainsHaveSpaceForDisksWithSnapshots(Collection<DiskImage> diskImages) {
        final Map<Guid, List<DiskImage>> disksMap = getDomainsDisksMap(diskImages);

        return validOrFirstFailure(new ValidatorPredicate() {
            @Override
            public ValidationResult evaluate(Map.Entry<Guid, StorageDomainValidator> entry) {
                Guid sdId = entry.getKey();
                List<DiskImage> diskImages = disksMap.get(sdId);
                return getStorageDomainValidator(entry).hasSpaceForDisksWithSnapshots(diskImages);
            }
        });
    }

    /** @return The lazy-loaded validator for the given map entry */
    protected StorageDomainValidator getStorageDomainValidator(Map.Entry<Guid, StorageDomainValidator> entry) {
        if (entry.getValue() == null) {
            entry.setValue(new StorageDomainValidator(getStorageDomainDao().getForStoragePool(entry.getKey(), storagePoolId)));
        }

        return entry.getValue();
    }

    /** @return The Dao object used to retrieve storage domains */
    public StorageDomainDao getStorageDomainDao() {
        return DbFacade.getInstance().getStorageDomainDao();
    }

    /**
     * Validates all the storage domains by a given predicate.
     *
     * @return {@link ValidationResult#VALID} if all the domains are OK, or the
     * first validation error if they aren't.
     */
    private ValidationResult validOrFirstFailure(ValidatorPredicate predicate) {
        for (Map.Entry<Guid, StorageDomainValidator> entry : domainValidators.entrySet()) {
            ValidationResult currResult = predicate.evaluate(entry);
            if (!currResult.isValid()) {
                return currResult;
            }
        }
        return ValidationResult.VALID;
    }

    /** A predicate for evaluating storage domains */
    private static interface ValidatorPredicate {
        public ValidationResult evaluate(Map.Entry<Guid, StorageDomainValidator> entry);
    }
}
