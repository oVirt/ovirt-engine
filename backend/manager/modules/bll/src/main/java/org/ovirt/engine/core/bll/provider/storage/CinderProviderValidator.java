package org.ovirt.engine.core.bll.provider.storage;

import java.util.List;

import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.provider.ProviderValidator;
import org.ovirt.engine.core.bll.storage.connection.CINDERStorageHelper;
import org.ovirt.engine.core.bll.validator.storage.StoragePoolValidator;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.ProviderType;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.storage.OpenStackVolumeProviderProperties;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.StorageDomainDao;
import org.ovirt.engine.core.dao.StoragePoolDao;

public class CinderProviderValidator extends ProviderValidator {

    StorageDomain CinderStorageDomain;

    public CinderProviderValidator(Provider<?> provider) {
        super(provider);
    }

    @Override
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

    @Override
    public ValidationResult validateRemoveProvider() {
        if (getStorageDomain() == null) {
            return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_NOT_EXIST);
        }
        StorageDomainStatus status = getStorageDomain().getStatus();
        if (status != StorageDomainStatus.Inactive && status != StorageDomainStatus.Maintenance
                && status != StorageDomainStatus.Unknown) {
            if (status.isStorageDomainInProcess()) {
                return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_OBJECT_LOCKED);
            }
            return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_STATUS_ILLEGAL2,
                    String.format("$%s %s", "status", status));
        }
        return CINDERStorageHelper.isCinderHasNoImages(getStorageDomain().getId());
    }

    private StorageDomain getStorageDomain() {
        if (CinderStorageDomain == null) {
            List<StorageDomain> providerStorageList = getStorageDomainDao().getAllByConnectionId(provider.getId());
            if (!providerStorageList.isEmpty()) {
                CinderStorageDomain = providerStorageList.get(0);
            }
        }
        return CinderStorageDomain;
    }

    private ValidationResult validateAttachStorageDomain() {
        StoragePoolValidator spValidator = new StoragePoolValidator(getStoragePool());
        ValidationResult result;

        result = spValidator.isAnyDomainInProcess();
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

    protected StorageDomainDao getStorageDomainDao() {
        return DbFacade.getInstance().getStorageDomainDao();
    }

    public ValidationResult isCinderAlreadyExists() {
        List<Provider<?>> cinderProvidersFromDB = getProviderDao().getAllByTypes(ProviderType.OPENSTACK_VOLUME);
        for (Provider cinderProviderFromDB : cinderProvidersFromDB) {
            if (provider.getUrl().equals(cinderProviderFromDB.getUrl())) {
                return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_CINDER_ALREADY_EXISTS);
            }
        }
        return ValidationResult.VALID;
    }
}
