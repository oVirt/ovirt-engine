package org.ovirt.engine.core.bll.validator.storage;

import java.util.List;

import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.utils.VersionStorageFormatUtil;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.StorageDomainDAO;
import org.ovirt.engine.core.dao.StoragePoolDAO;
import org.ovirt.engine.core.dao.StoragePoolIsoMapDAO;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.linq.Predicate;

/**
 * CanDoAction validation methods for attaching a storage domain to a DC (pool).
 */
public class StorageDomainToPoolRelationValidator {
    private final StorageDomainStatic storageDomainStatic;
    private final StoragePool storagePool;

    public StorageDomainToPoolRelationValidator(StorageDomainStatic domainStatic, StoragePool pool) {
        storageDomainStatic = domainStatic;
        storagePool = pool;
    }

    protected StorageDomainDAO getStorageDomainDao() {
        return DbFacade.getInstance().getStorageDomainDao();
    }
    protected StoragePoolDAO getStoragePoolDao() {
        return DbFacade.getInstance().getStoragePoolDao();
    }

    private boolean isStorageDomainOfTypeIsoOrExport() {
        return storageDomainStatic.getStorageDomainType().isIsoOrImportExportDomain();
    }

    public ValidationResult isStorageDomainCompatibleWithDC() {
        if (storageDomainStatic.getStorageType() == StorageType.GLUSTERFS) {
            return isGlusterSupportedInDC();
        }

        if (storageDomainStatic.getStorageType() == StorageType.POSIXFS) {
            return isPosixSupportedInDC();
        }

        return ValidationResult.VALID;
    }

    /**
     * Checks that the DC compatibility version supports Posix domains.
     * In case there is mismatch, a proper canDoAction message will be added
     *
     * @return The result of the validation
     */
    public ValidationResult isPosixSupportedInDC() {
        if (!storagePool.isLocal() &&
                !Config.<Boolean> getValue(ConfigValues.PosixStorageEnabled, storagePool.getcompatibility_version().toString())) {
            return new ValidationResult(VdcBllMessages.DATA_CENTER_POSIX_STORAGE_NOT_SUPPORTED_IN_CURRENT_VERSION);
        }
        return ValidationResult.VALID;
    }

    /**
     * Checks that the DC compatibility version supports Gluster domains.
     * In case there is mismatch, a proper canDoAction message will be added
     *
     * @return true if the version matches
     */
    public ValidationResult isGlusterSupportedInDC() {
        if (!storagePool.isLocal() &&
                !Config.<Boolean> getValue(ConfigValues.GlusterFsStorageEnabled,
                        storagePool.getcompatibility_version().toString())) {
            return new ValidationResult(VdcBllMessages.DATA_CENTER_GLUSTER_STORAGE_NOT_SUPPORTED_IN_CURRENT_VERSION);
        }
        return ValidationResult.VALID;
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

        // Get the number of storage domains of the given type currently attached
        // to the pool:
        int count = LinqUtils.filter(
                getStorageDomainDao().getAllForStoragePool(storagePool.getId()),
                new Predicate<StorageDomain>() {
                    @Override
                    public boolean eval(StorageDomain a) {
                        return a.getStorageDomainType() == type;
                    }
                }
        ).size();

        // If the count is zero we are okay, we can add a new one:
        if (count == 0) {
            return ValidationResult.VALID;
        }

        // If we are here then we already have at least one storage type of the given type
        // so when have to prepare a friendly message for the user (see #713160) and fail:
        if (type == StorageDomainType.ISO) {
            return new ValidationResult(VdcBllMessages.ERROR_CANNOT_ATTACH_MORE_THAN_ONE_ISO_DOMAIN);
        } else {
            return new ValidationResult(VdcBllMessages.ERROR_CANNOT_ATTACH_MORE_THAN_ONE_EXPORT_DOMAIN);
        }
    }

    /**
     * The following method should check if the format of the storage domain allows to it to be attached to the storage
     * pool. At case of failure the false value will be return and appropriate error message will be added to
     * canDoActionMessages
     */
    public ValidationResult isStorageDomainFormatCorrectForDC() {
        if (isStorageDomainOfTypeIsoOrExport()) {
            return ValidationResult.VALID;
        }

        if (storagePool != null) {
            if (VersionStorageFormatUtil.getPreferredForVersion(storagePool.getcompatibility_version(),
                    storageDomainStatic.getStorageType()).compareTo(storageDomainStatic.getStorageFormat()) < 0) {

                return new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_DOMAIN_FORMAT_ILLEGAL, String.format("$storageFormat %1$s", storageDomainStatic
                        .getStorageFormat().toString()));
            }
        }
        return ValidationResult.VALID;
    }

    public ValidationResult isStorageDomainLocalityFitsDC() {
        if (!isStorageDomainOfTypeIsoOrExport() && storageDomainStatic.getStorageType().isLocal() != storagePool.isLocal()) {
            return new ValidationResult(VdcBllMessages.ERROR_CANNOT_ATTACH_STORAGE_DOMAIN_STORAGE_TYPE_NOT_MATCH);
        }
        return ValidationResult.VALID;
    }

    public ValidationResult isStorageDomainTypeFitsDC() {
        boolean isBlockDomain = storageDomainStatic.getStorageType().isBlockDomain();

        if (!isMixedTypesAllowedInDC()) {
            List<StorageType> storageTypesOnPool =
                    getStoragePoolDao().getStorageTypesInPool(storagePool.getId());
            for (StorageType storageType : storageTypesOnPool) {
                if (storageType.isBlockDomain() != isBlockDomain) {
                    return new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_MIXED_STORAGE_TYPES_NOT_ALLOWED);
                }
            }
        }
        return ValidationResult.VALID;
    }


    // TODO: Should be removed when 3.0 compatibility will not be supported, for now we are blocking the possibility
    // to mix NFS domains with block domains on 3.0 pools since block domains on 3.0 pools can be in V2 format while NFS
    // domains on 3.0 can only be in V1 format
    public boolean isMixedTypesAllowedInDC() {
        return FeatureSupported.mixedDomainTypesOnDataCenter(storagePool.getcompatibility_version());
    }

    public ValidationResult isStorageDomainNotInAnyPool() {
        if (storageDomainStatic != null) {
            // check if there is no pool-domain map
            if (!getStoragePoolIsoMapDao().getAllForStorage(storageDomainStatic.getId()).isEmpty()) {
                return new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_DOMAIN_STATUS_ILLEGAL);
            }
        }
        return ValidationResult.VALID;
    }

    protected StoragePoolIsoMapDAO getStoragePoolIsoMapDao() {
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
            if (!(valResult = isStorageDomainTypeFitsDC()).isValid()) {
                return valResult;
            }
        } else {
            if (!(valResult = validateAmountOfIsoAndExportDomainsInDC()).isValid()) {
                return valResult;
            }
        }
        if (!(valResult = isStorageDomainCompatibleWithDC()).isValid()) {
            return valResult;
        }
        return ValidationResult.VALID;
    }
}
