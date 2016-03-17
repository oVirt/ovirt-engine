package org.ovirt.engine.core.bll.validator.storage;

import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.utils.VersionStorageFormatUtil;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.StorageDomainDao;
import org.ovirt.engine.core.dao.StoragePoolDao;
import org.ovirt.engine.core.dao.StoragePoolIsoMapDao;

/**
 * Validate validation methods for attaching a storage domain to a DC (pool).
 */
public class StorageDomainToPoolRelationValidator {
    private final StorageDomainStatic storageDomainStatic;
    private final StoragePool storagePool;

    public StorageDomainToPoolRelationValidator(StorageDomainStatic domainStatic, StoragePool pool) {
        storageDomainStatic = domainStatic;
        storagePool = pool;
    }

    protected StorageDomainDao getStorageDomainDao() {
        return DbFacade.getInstance().getStorageDomainDao();
    }
    protected StoragePoolDao getStoragePoolDao() {
        return DbFacade.getInstance().getStoragePoolDao();
    }

    private boolean isStorageDomainOfTypeIsoOrExport() {
        return storageDomainStatic.getStorageDomainType().isIsoOrImportExportDomain();
    }

    /**
     * Check that we are not trying to attach more than one ISO or export
     * domain to the same data center.
     */
    public ValidationResult validateAmountOfIsoAndExportDomainsInDC() {
        // Nothing to check if the storage domain is not an ISO or export:
        if (!isStorageDomainOfTypeIsoOrExport()) {
            return ValidationResult.VALID;
        }

        final StorageDomainType type = storageDomainStatic.getStorageDomainType();

        // Check if such a domain type is already present in the pool
        boolean hasSuchType =
                getStorageDomainDao().getAllForStoragePool(storagePool.getId()).stream().
                        anyMatch(a -> a.getStorageDomainType() == type);

        // If it's the first domain of that type, we are okay, we can add a new one:
        if (!hasSuchType) {
            return ValidationResult.VALID;
        }

        // If we are here then we already have at least one storage type of the given type
        // so when have to prepare a friendly message for the user (see #713160) and fail:
        if (type == StorageDomainType.ISO) {
            return new ValidationResult(EngineMessage.ERROR_CANNOT_ATTACH_MORE_THAN_ONE_ISO_DOMAIN);
        } else {
            return new ValidationResult(EngineMessage.ERROR_CANNOT_ATTACH_MORE_THAN_ONE_EXPORT_DOMAIN);
        }
    }

    /**
     * The following method checks if the format of the storage domain allows it to be attached to the storage pool.
     */
    public ValidationResult isStorageDomainFormatCorrectForDC() {
        if (isStorageDomainOfTypeIsoOrExport()) {
            return ValidationResult.VALID;
        }

        if (storagePool != null) {
            if (VersionStorageFormatUtil.getForVersion(storagePool.getCompatibilityVersion())
                    .compareTo(storageDomainStatic.getStorageFormat()) < 0) {
                return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_FORMAT_ILLEGAL, String.format("$storageFormat %1$s", storageDomainStatic
                        .getStorageFormat().toString()));
            }
        }
        return ValidationResult.VALID;
    }

    public ValidationResult isStorageDomainLocalityFitsDC() {
        if (!isStorageDomainOfTypeIsoOrExport() && storageDomainStatic.getStorageType().isLocal() != storagePool.isLocal()) {
            return new ValidationResult(EngineMessage.ERROR_CANNOT_ATTACH_STORAGE_DOMAIN_STORAGE_TYPE_NOT_MATCH);
        }
        return ValidationResult.VALID;
    }

    public ValidationResult isStorageDomainNotInAnyPool() {
        if (storageDomainStatic != null) {
            // check if there is no pool-domain map
            if (!getStoragePoolIsoMapDao().getAllForStorage(storageDomainStatic.getId()).isEmpty()) {
                return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_STATUS_ILLEGAL);
            }
        }
        return ValidationResult.VALID;
    }

    protected StoragePoolIsoMapDao getStoragePoolIsoMapDao() {
        return DbFacade.getInstance().getStoragePoolIsoMapDao();
    }

    public ValidationResult validateDomainCanBeAttachedToPool() {
        ValidationResult valResult;
        if (!isStorageDomainOfTypeIsoOrExport()) {
            if (!(valResult = isStorageDomainFormatCorrectForDC()).isValid()) {
                return valResult;
            }
            if (!(valResult = isStorageDomainNotInAnyPool()).isValid()) {
                return valResult;
            }
            if (!(valResult = isStorageDomainLocalityFitsDC()).isValid()) {
                return valResult;
            }
        } else {
            if (!(valResult = validateAmountOfIsoAndExportDomainsInDC()).isValid()) {
                return valResult;
            }
        }
        return ValidationResult.VALID;
    }
}
