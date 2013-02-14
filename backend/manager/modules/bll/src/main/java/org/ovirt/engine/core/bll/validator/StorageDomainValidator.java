package org.ovirt.engine.core.bll.validator;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.StorageDomainDynamic;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class StorageDomainValidator {

    private StorageDomain storageDomain;

    public StorageDomainValidator(StorageDomain domain) {
        storageDomain = domain;
    }

    public ValidationResult isDomainExistAndActive() {
        if (storageDomain == null) {
            return new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_DOMAIN_NOT_EXIST);
        }
        if (storageDomain.getstatus() == null || storageDomain.getstatus() != StorageDomainStatus.Active) {
            return new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_DOMAIN_STATUS_ILLEGAL);
        }
        return ValidationResult.VALID;
    }

    public ValidationResult domainIsValidDestination() {
        if (storageDomain.getstorage_domain_type() == StorageDomainType.ISO
                || storageDomain.getstorage_domain_type() == StorageDomainType.ImportExport) {
            return new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_DOMAIN_TYPE_ILLEGAL);
        }
        return ValidationResult.VALID;
    }

    public ValidationResult isDomainWithinThresholds() {
        StorageDomainDynamic dynamic = storageDomain.getStorageDynamicData();
        if (dynamic != null && dynamic.getfreeDiskInGB() < getLowDiskSpaceThreshold()) {
            return new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_DISK_SPACE_LOW_ON_TARGET_STORAGE_DOMAIN,
                    storageName());
        }
        return ValidationResult.VALID;
    }

    private String storageName() {
        return String.format("$%1$s %2$s", "storageName", storageDomain.getstorage_name());
    }

    public ValidationResult isDomainHasSpaceForRequest(final long requestedSize) {
        if (storageDomain.getavailable_disk_size() != null &&
                storageDomain.getavailable_disk_size() - requestedSize < getLowDiskSpaceThreshold()) {
            return new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_DISK_SPACE_LOW_ON_TARGET_STORAGE_DOMAIN,
                    storageName());
        }
        return ValidationResult.VALID;
    }

    private static Integer getLowDiskSpaceThreshold() {
        return Config.<Integer> GetValue(ConfigValues.FreeSpaceCriticalLowInGB);
    }

    public static Map<StorageDomain, Integer> getSpaceRequirementsForStorageDomains(Collection<DiskImage> images,
            Map<Guid, StorageDomain> storageDomains, Map<Guid, DiskImage> imageToDestinationDomainMap) {
        Map<DiskImage, StorageDomain> spaceMap = new HashMap<DiskImage, StorageDomain>();
        for (DiskImage image : images) {
            Guid storageId = imageToDestinationDomainMap.get(image.getId()).getstorage_ids().get(0);
            StorageDomain domain = storageDomains.get(storageId);
            if (domain == null) {
                domain = DbFacade.getInstance().getStorageDomainDao().get(storageId);
            }
            spaceMap.put(image, domain);
        }
        return StorageDomainValidator.getSpaceRequirementsForStorageDomains(spaceMap);
    }

    public static Map<StorageDomain, Integer> getSpaceRequirementsForStorageDomains(Map<DiskImage, StorageDomain> imageToDomainMap) {
        Map<StorageDomain, Integer> map = new HashMap<StorageDomain, Integer>();
        if (!imageToDomainMap.isEmpty()) {
            for (Map.Entry<DiskImage, StorageDomain> entry : imageToDomainMap.entrySet()) {
                StorageDomain domain = entry.getValue();
                int size = (int) entry.getKey().getActualSize();
                if (map.containsKey(domain)) {
                    map.put(domain, map.get(domain) + size);
                } else {
                    map.put(domain, size);
                }
            }
        }
        return map;
    }
}
