package org.ovirt.engine.core.bll.validator;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class StorageDomainValidator {

    private storage_domains storageDomain;

    public StorageDomainValidator(storage_domains domain) {
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

    public static Map<storage_domains, Integer> getSpaceRequirementsForStorageDomains(Collection<DiskImage> images,
            Map<Guid, storage_domains> storageDomains, Map<Guid, DiskImage> imageToDestinationDomainMap) {
        Map<DiskImage, storage_domains> spaceMap = new HashMap<DiskImage, storage_domains>();
        for (DiskImage image : images) {
            Guid storageId = imageToDestinationDomainMap.get(image.getId()).getstorage_ids().get(0);
            storage_domains domain = storageDomains.get(storageId);
            if (domain == null) {
                domain = DbFacade.getInstance().getStorageDomainDao().get(storageId);
            }
            spaceMap.put(image, domain);
        }
        return StorageDomainValidator.getSpaceRequirementsForStorageDomains(spaceMap);
    }

    public static Map<storage_domains, Integer> getSpaceRequirementsForStorageDomains(Map<DiskImage, storage_domains> imageToDomainMap) {
        Map<storage_domains, Integer> map = new HashMap<storage_domains, Integer>();
        if (!imageToDomainMap.isEmpty()) {
            for (Map.Entry<DiskImage, storage_domains> entry : imageToDomainMap.entrySet()) {
                storage_domains domain = entry.getValue();
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
