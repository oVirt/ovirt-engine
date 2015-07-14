package org.ovirt.engine.core.bll.provider.storage;

import java.util.List;

import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.provider.ProviderValidator;
import org.ovirt.engine.core.bll.validator.storage.StoragePoolValidator;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.ProviderType;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.storage.OpenStackVolumeProviderProperties;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.StoragePoolDao;

public class CinderProviderValidator extends ProviderValidator {

    public CinderProviderValidator(Provider<?> provider) {
        super(provider);
    }

    public ValidationResult validateAddProvider() {
        ValidationResult cinderValidation = isCinderAlreadyExists();
        if (!cinderValidation.isValid()) {
            return cinderValidation;
        }
        if (getStoragePool() != null) {
            return validateAttachStorageDomain();
        }
        return ValidationResult.VALID;
    }

    private ValidationResult validateAttachStorageDomain() {
        StoragePoolValidator spValidator = new StoragePoolValidator(getStoragePool());
        ValidationResult result;

        result = spValidator.isAnyDomainInProcess();
        if (!result.isValid()) {
            return result;
        }
        result = isCinderSupportedInDC();
        if (!result.isValid()) {
            return result;
        }
        result = spValidator.checkStoragePoolNotInStatus(StoragePoolStatus.Up);
        if (!result.isValid()) {
            return result;
        }
        return ValidationResult.VALID;
    }


    private StoragePool getStoragePool() {
        Guid storagePoolId =
                ((OpenStackVolumeProviderProperties) provider.getAdditionalProperties()).getStoragePoolId();
        return getStoragePoolDao().get(storagePoolId);
    }

    protected StoragePoolDao getStoragePoolDao() {
        return DbFacade.getInstance().getStoragePoolDao();
    }

    /**
     * Checks that the DC compatibility version supports Cinder domains and it is not local.
     * In case there is a mismatch, a proper canDoAction message will be added
     *
     * @return The result of the validation
     */
    public ValidationResult isCinderSupportedInDC() {
        if (!getStoragePool().isLocal()
                && !FeatureSupported.cinderProviderSupported(getStoragePool().getCompatibilityVersion())) {
            return new ValidationResult(EngineMessage.DATA_CENTER_CINDER_STORAGE_NOT_SUPPORTED_IN_CURRENT_VERSION);
        }
        return ValidationResult.VALID;
    }

    public ValidationResult isCinderAlreadyExists() {
        List<Provider<?>> cinderProvidersFromDB = getProviderDao().getAllByType(ProviderType.OPENSTACK_VOLUME);
        for (Provider cinderProviderFromDB : cinderProvidersFromDB) {
            if (provider.getUrl().equals(cinderProviderFromDB.getUrl())) {
                return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_CINDER_ALREADY_EXISTS);
            }
        }
        return ValidationResult.VALID;
    }
}
