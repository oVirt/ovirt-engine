package org.ovirt.engine.core.bll.validator;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.StorageDomainDAO;

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
    private NGuid storagePoolId;

    /** A map from the ids of each domain being validated to its validator */
    private Map<Guid, StorageDomainValidator> domainValidators;

    /**
     * Constructor from Guids
     * @param sdIds A {@link Collection} of storage domain IDs to be validated
     */
    public MultipleStorageDomainsValidator(NGuid storagePoolId, Collection<Guid> sdIds) {
        this.storagePoolId = storagePoolId;
        domainValidators = new HashMap<Guid, StorageDomainValidator>();
        for (Guid id : sdIds) {
            domainValidators.put(id, null);
        }
    }

    /**
     * Validates that all the domains exist and are active.
     * @return {@link ValidationResult#VALID} if all the domains are OK, or a {@link ValidationResult} with the first non-active domain encountered.
     */
    public ValidationResult allDomainsExistAndActive() {
        return validOrFirstFailure(new ValidatorPredicate() {
            @Override
            public ValidationResult evaluate(StorageDomainValidator validator) {
                return validator.isDomainExistAndActive();
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
            public ValidationResult evaluate(StorageDomainValidator validator) {
                return validator.isDomainWithinThresholds();
            }
        });
    }

    /** @return The lazy-loaded validator for the given map entry */
    private StorageDomainValidator getStorageDomainValidator(Map.Entry<Guid, StorageDomainValidator> entry) {
        if (entry.getValue() == null) {
            entry.setValue(new StorageDomainValidator(getStorageDomainDAO().getForStoragePool(entry.getKey(), storagePoolId)));
        }

        return entry.getValue();
    }

    /** @return The DAO object used to retrieve storage domains */
    protected StorageDomainDAO getStorageDomainDAO() {
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
            ValidationResult currResult = predicate.evaluate(getStorageDomainValidator(entry));
            if (!currResult.isValid()) {
                return currResult;
            }
        }
        return ValidationResult.VALID;
    }

    /** A predicate for evaluating storage domains */
    private static interface ValidatorPredicate {
        public ValidationResult evaluate(StorageDomainValidator validator);
    }
}
