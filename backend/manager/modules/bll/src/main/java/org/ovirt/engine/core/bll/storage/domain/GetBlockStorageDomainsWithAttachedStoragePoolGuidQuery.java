package org.ovirt.engine.core.bll.storage.domain;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.queries.StorageDomainsAndStoragePoolIdQueryParameters;

public class GetBlockStorageDomainsWithAttachedStoragePoolGuidQuery<P extends StorageDomainsAndStoragePoolIdQueryParameters> extends GetStorageDomainsWithAttachedStoragePoolGuidQuery<P> {
    public GetBlockStorageDomainsWithAttachedStoragePoolGuidQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected List<StorageDomainStatic> filterAttachedStorageDomains() {
        List<StorageDomainStatic> storageDomainStaticList = new ArrayList<>();
        if (getParameters().getStorageDomainList() != null) {
            storageDomainStaticList = getAttachedStorageDomains(getParameters().getStorageDomainList());
        }
        return storageDomainStaticList;
    }
}
