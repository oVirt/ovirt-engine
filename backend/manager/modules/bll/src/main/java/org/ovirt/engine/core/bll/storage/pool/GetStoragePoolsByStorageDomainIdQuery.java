package org.ovirt.engine.core.bll.storage.pool;

import org.ovirt.engine.core.common.queries.IdQueryParameters;
public class GetStoragePoolsByStorageDomainIdQuery<P extends IdQueryParameters>
        extends StoragePoolQueryBase<P> {
    public GetStoragePoolsByStorageDomainIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected Object queryDataCenter() {
        return storagePoolDao.getAllForStorageDomain(getParameters().getId());
    }
}
