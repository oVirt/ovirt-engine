package org.ovirt.engine.core.bll.validator;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.Backend;
import org.ovirt.engine.core.bll.ImagesHandler;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.network.macpoolmanager.MacPoolManagerStrategy;
import org.ovirt.engine.core.bll.network.macpoolmanager.MacPoolPerDcSingleton;
import org.ovirt.engine.core.common.action.ImportVmParameters;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.IVdcQueryable;
import org.ovirt.engine.core.common.businessentities.OvfEntityData;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.network.VmNic;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.utils.MacAddressValidationPatterns;
import org.ovirt.engine.core.common.vdscommands.GetImagesListVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSParametersBase;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.StorageDomainDAO;
import org.ovirt.engine.core.dao.StoragePoolDAO;

public class ImportValidator {

    private ImportVmParameters params;

    private StoragePool cachedStoragePool;
    private StorageDomain cachedStorageDomain;

    private static final Pattern VALIDATE_MAC_ADDRESS =
            Pattern.compile(MacAddressValidationPatterns.UNICAST_MAC_ADDRESS_FORMAT);

    public ImportValidator(ImportVmParameters params) {
        this.params = params;
    }

    /**
     * Used for testings
     */
    public ImportValidator() {
    }

    public ValidationResult validateUnregisteredEntity(IVdcQueryable entityFromConfiguration, OvfEntityData ovfEntityData, List<DiskImage> images) {
        if (ovfEntityData == null && !params.isImportAsNewEntity()) {
            return new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_UNSUPPORTED_OVF);
        }

        if (entityFromConfiguration == null) {
            return new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_OVF_CONFIGURATION_NOT_SUPPORTED);
        }

        for (DiskImage image : images) {
            StorageDomain sd = getStorageDomainDAO().getForStoragePool(
                    image.getStorageIds().get(0), getStoragePool().getId());
            ValidationResult result = new StorageDomainValidator(sd).isDomainExistAndActive();
            if (!result.isValid()) {
                return result;
            }
        }

        if (!getStorageDomain().getStorageDomainType().isDataDomain()) {
            return new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_DOMAIN_TYPE_UNSUPPORTED,
                    String.format("$domainId %1$s", params.getStorageDomainId()),
                    String.format("$domainType %1$s", getStorageDomain().getStorageDomainType()));
        }

        return ValidationResult.VALID;
    }

    public ValidationResult validateMacAddress(List<VmNic> ifaces) {
        int freeMacs = 0;
        for (VmNic iface : ifaces) {
            if (!StringUtils.isEmpty(iface.getMacAddress())) {
                if(!VALIDATE_MAC_ADDRESS.matcher(iface.getMacAddress()).matches()) {
                    return new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_NETWORK_INTERFACE_MAC_INVALID,
                            String.format("$IfaceName %1$s", iface.getName()),
                            String.format("$MacAddress %1$s", iface.getMacAddress()));
                }
            }
            else {
                freeMacs++;
            }
        }
        if (freeMacs > 0 && !(getMacPool().getAvailableMacsCount() >= freeMacs)) {
            return new ValidationResult(VdcBllMessages.MAC_POOL_NOT_ENOUGH_MAC_ADDRESSES);
        }

        return ValidationResult.VALID;
    }

    protected ValidationResult checkIfDisksExist(Iterable<DiskImage> disksList, Map<Guid, Guid> imageToDestinationDomainMap) {
        Map<Guid, List<Guid>> alreadyRetrieved = new HashMap<>();
        for (DiskImage disk : disksList) {
            Guid targetStorageDomainId = imageToDestinationDomainMap.get(disk.getId());
            List<Guid> imagesOnStorageDomain = alreadyRetrieved.get(targetStorageDomainId);

            if (imagesOnStorageDomain == null) {
                VDSReturnValue returnValue = runVdsCommand(
                        VDSCommandType.GetImagesList,
                        new GetImagesListVDSCommandParameters(targetStorageDomainId, params.getStoragePoolId())
                );

                if (returnValue.getSucceeded()) {
                    imagesOnStorageDomain = (List<Guid>) returnValue.getReturnValue();
                    alreadyRetrieved.put(targetStorageDomainId, imagesOnStorageDomain);
                } else {
                    return new ValidationResult(VdcBllMessages.ERROR_GET_IMAGE_LIST,
                            String.format("$sdName %1$s", getStorageDomain(targetStorageDomainId).getName()));
                }
            }

            if (imagesOnStorageDomain.contains(disk.getId())) {
                return new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_DOMAIN_ALREADY_CONTAINS_DISK);
            }
        }

        return ValidationResult.VALID;
    }

    public ValidationResult verifyDisks(Iterable<DiskImage> imageList, Map<Guid, Guid> imageToDestinationDomainMap) {
        if (!params.isImportAsNewEntity() && !params.isImagesExistOnTargetStorageDomain()) {
            return checkIfDisksExist(imageList, imageToDestinationDomainMap);
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
            throws VdcBLLException {
        return Backend.getInstance().getResourceManager().RunVdsCommand(commandType, parameters);
    }

    protected StorageDomain getStorageDomain(Guid domainId) {
        return getStorageDomainDAO().getForStoragePool(domainId, getStoragePool().getId());
    }

    protected MacPoolManagerStrategy getMacPool() {
        return MacPoolPerDcSingleton.getInstance().poolForDataCenter(params.getStoragePoolId());
    }

    public StorageDomainDAO getStorageDomainDAO() {
        return DbFacade.getInstance().getStorageDomainDao();
    }

    protected StoragePoolDAO getStoragePoolDAO() {
        return DbFacade.getInstance().getStoragePoolDao();
    }

    public StoragePool getStoragePool() {
        if (cachedStoragePool == null) {
            cachedStoragePool = getStoragePoolDAO().get(params.getStoragePoolId());
        }
        return cachedStoragePool;
    }

    public StorageDomain getStorageDomain() {
        if (cachedStorageDomain == null) {
            cachedStorageDomain = getStorageDomainDAO().get(params.getStorageDomainId());
        }
        return cachedStorageDomain;
    }
}
