package org.ovirt.engine.core.bll.storage.domain;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.queries.GetExistingStorageDomainListParameters;
import org.ovirt.engine.core.common.queries.StorageDomainsAndStoragePoolIdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;

public class GetFileStorageDomainsWithAttachedStoragePoolGuidQuery<P extends StorageDomainsAndStoragePoolIdQueryParameters> extends GetStorageDomainsWithAttachedStoragePoolGuidQuery<P> {
    public GetFileStorageDomainsWithAttachedStoragePoolGuidQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected List<StorageDomainStatic> filterAttachedStorageDomains() {
        List<StorageDomainStatic> storageDomainStaticList = new ArrayList<>();
        if (getParameters().getStorageDomainList() != null) {
            storageDomainStaticList = getAttachedStorageDomains(getParameters().getStorageDomainList());
        } else if (getParameters().getStorageServerConnection() != null) {
            storageDomainStaticList =
                    getStorageDomainsByStorageServerConnections(getParameters().getStorageServerConnection());
        }
        return storageDomainStaticList;
    }

    protected List<StorageDomainStatic> getStorageDomainsByStorageServerConnections(StorageServerConnections storageServerConnection) {
        List<StorageDomainStatic> storageDomainsWithAttachedStoragePoolId = new ArrayList<>();
        VdcQueryReturnValue returnValue = getExistingStorageDomainList(storageServerConnection);
        if (returnValue.getSucceeded()) {
            List<StorageDomain> existingStorageDomains = returnValue.getReturnValue();
            if (!existingStorageDomains.isEmpty()) {
                StorageDomain storageDomain = existingStorageDomains.get(0);
                if (storageDomain.getStoragePoolId() != null) {
                    storageDomainsWithAttachedStoragePoolId.add(storageDomain.getStorageStaticData());
                }
            }
        }
        return storageDomainsWithAttachedStoragePoolId;
    }

    protected VdcQueryReturnValue getExistingStorageDomainList(StorageServerConnections storageServerConnection) {
        VdcQueryReturnValue returnValue = getBackend().runInternalQuery(VdcQueryType.GetExistingStorageDomainList,
                new GetExistingStorageDomainListParameters(
                        getVdsId(),
                        storageServerConnection.getStorageType(),
                        StorageDomainType.Data,
                        storageServerConnection.getConnection()));
        return returnValue;
    }
}
