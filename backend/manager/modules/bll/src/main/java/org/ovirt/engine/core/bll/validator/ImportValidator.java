package org.ovirt.engine.core.bll.validator;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.Backend;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.storage.disk.image.ImagesHandler;
import org.ovirt.engine.core.bll.validator.storage.DiskImagesValidator;
import org.ovirt.engine.core.bll.validator.storage.MultipleStorageDomainsValidator;
import org.ovirt.engine.core.bll.validator.storage.StorageDomainValidator;
import org.ovirt.engine.core.common.action.ImportParameters;
import org.ovirt.engine.core.common.businessentities.IVdcQueryable;
import org.ovirt.engine.core.common.businessentities.OvfEntityData;
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

    public ValidationResult validateUnregisteredEntity(IVdcQueryable entityFromConfiguration, OvfEntityData ovfEntityData, List<DiskImage> images) {
        if (ovfEntityData == null && !params.isImportAsNewEntity()) {
            return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_UNSUPPORTED_OVF);
        }

        if (entityFromConfiguration == null) {
            return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_OVF_CONFIGURATION_NOT_SUPPORTED);
        }

        for (DiskImage image : images) {
            StorageDomain sd = getStorageDomainDao().getForStoragePool(
                    image.getStorageIds().get(0), getStoragePool().getId());
            ValidationResult result = new StorageDomainValidator(sd).isDomainExistAndActive();
            if (!result.isValid()) {
                log.error("Storage Domain '{}' with id '{}', could not be found for disk alias '{}' with image id '{}'",
                        sd == null ? null : sd.getStorageName(),
                        image.getStorageIds().get(0),
                        image.getDiskAlias(),
                        image.getId());
                return result;
            }
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
