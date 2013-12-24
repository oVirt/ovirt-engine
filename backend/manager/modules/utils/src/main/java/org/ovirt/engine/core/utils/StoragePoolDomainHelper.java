package org.ovirt.engine.core.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMap;


public class StoragePoolDomainHelper {

    public static Map<String, String> buildStoragePoolDomainsMap(List<StoragePoolIsoMap> storagePoolIsoMaps) {
        Map<String, String> storageDomains = new HashMap<String, String>();

        for (StoragePoolIsoMap domain : storagePoolIsoMaps) {
            if (domain.getStatus() == StorageDomainStatus.Maintenance) {
                storageDomains.put(domain.getstorage_id().toString(), "attached");
            } else {
                storageDomains.put(domain.getstorage_id().toString(),
                        StorageDomainStatus.Active.toString().toLowerCase());
            }
        }

        return storageDomains;
    }

}
