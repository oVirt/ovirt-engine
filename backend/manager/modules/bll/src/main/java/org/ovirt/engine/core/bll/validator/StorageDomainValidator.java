package org.ovirt.engine.core.bll.validator;

import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class StorageDomainValidator {

    private storage_domains storageDomain;

    public StorageDomainValidator(Guid storageDomainId) {
        storageDomain = getStorageDomain(storageDomainId);
    }

    public StorageDomainValidator(storage_domains domain) {
        storageDomain = domain;
    }

    private storage_domains getStorageDomain(Guid storageDomainId) {
        return DbFacade.getInstance().getStorageDomainDAO().get(storageDomainId);
    }

    public boolean domainIsValidDestination() {
        return !(storageDomain.getstorage_domain_type() == StorageDomainType.ISO
                    || storageDomain.getstorage_domain_type() == StorageDomainType.ImportExport);
    }

    public static Map<storage_domains, Integer> getSpaceRequirementsForStorageDomains(Map<DiskImage, storage_domains> imageToDomainMap) {
            Map<storage_domains, Integer> map = new HashMap<storage_domains, Integer>();
            if (!imageToDomainMap.isEmpty()) {
                for(Map.Entry<DiskImage, storage_domains> entry : imageToDomainMap.entrySet()) {
                    storage_domains domain = entry.getValue();
                    int size = (int) entry.getKey().getActualDiskWithSnapshotsSize();
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
