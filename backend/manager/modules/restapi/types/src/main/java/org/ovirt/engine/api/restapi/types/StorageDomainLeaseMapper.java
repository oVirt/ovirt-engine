package org.ovirt.engine.api.restapi.types;

import org.ovirt.engine.api.model.StorageDomain;
import org.ovirt.engine.api.model.StorageDomainLease;
import org.ovirt.engine.api.restapi.utils.GuidUtils;
import org.ovirt.engine.core.compat.Guid;

public class StorageDomainLeaseMapper {

    public static StorageDomainLease map(Guid storageDomainId) {
        if (storageDomainId != null) {
            StorageDomain storageDomain = new StorageDomain();
            storageDomain.setId(storageDomainId.toString());
            StorageDomainLease lease = new StorageDomainLease();
            lease.setStorageDomain(storageDomain);
            return lease;
        }
        return null;
    }

    public static Guid map(StorageDomainLease lease) {
        if (lease.isSetStorageDomain() && lease.getStorageDomain().isSetId()) {
            return GuidUtils.asGuid(lease.getStorageDomain().getId());
        }
        return null;
    }
}
