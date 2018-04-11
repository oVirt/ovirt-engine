package org.ovirt.engine.core.bll.provider.storage;

import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.provider.ProviderValidator;
import org.ovirt.engine.core.bll.validator.storage.StoragePoolValidator;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.ProviderType;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.OpenStackVolumeProviderProperties;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DiskImageDao;
import org.ovirt.engine.core.dao.StorageDomainDao;
import org.ovirt.engine.core.dao.StoragePoolDao;
import org.ovirt.engine.core.dao.provider.ProviderDao;

public class CinderProviderValidator extends ProviderValidator {

    @Inject
    private StoragePoolDao storagePoolDao;

    @Inject
    private StorageDomainDao storageDomainDao;

    @Inject
    private DiskImageDao diskImageDao;

    @Inject
    private ProviderDao providerDao;

    StorageDomain cinderStorageDomain;

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
        return isCinderHasNoImages();
    }

    public ValidationResult isCinderHasNoImages() {
        List<DiskImage> cinderDisks = diskImageDao.getAllForStorageDomain(getStorageDomain().getId());
        if (!cinderDisks.isEmpty()) {
            return new ValidationResult(EngineMessage.ERROR_CANNOT_DETACH_CINDER_PROVIDER_WITH_IMAGES);
        }
        return ValidationResult.VALID;
    }

    private StorageDomain getStorageDomain() {
        if (cinderStorageDomain == null) {
            List<StorageDomain> providerStorageList = storageDomainDao.getAllByConnectionId(provider.getId());
            if (!providerStorageList.isEmpty()) {
                cinderStorageDomain = providerStorageList.get(0);
            }
        }
        return cinderStorageDomain;
    }

    private ValidationResult validateAttachStorageDomain() {
        StoragePoolValidator spValidator = new StoragePoolValidator(getStoragePool());
        ValidationResult result;

        result = spValidator.isAnyDomainInProcess();
        if (!result.isValid()) {
            return result;
        }
        result = spValidator.isInStatus(StoragePoolStatus.Up);
        if (!result.isValid()) {
            return result;
        }
        return ValidationResult.VALID;
    }


    private StoragePool getStoragePool() {
        Guid storagePoolId =
                ((OpenStackVolumeProviderProperties) provider.getAdditionalProperties()).getStoragePoolId();
        return storagePoolDao.get(storagePoolId);
    }

    public ValidationResult isCinderAlreadyExists() {
        List<Provider<?>> cinderProvidersFromDB = providerDao.getAllByTypes(ProviderType.OPENSTACK_VOLUME);
        for (Provider cinderProviderFromDB : cinderProvidersFromDB) {
            if (provider.getUrl().equals(cinderProviderFromDB.getUrl())) {
                return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_CINDER_ALREADY_EXISTS);
            }
        }
        return ValidationResult.VALID;
    }
}
