/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.restapi.resource;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendDataCenterHelper {
    public static Guid lookupByStorageDomainId(BackendResource resource, Guid storageDomainId) {
        // Retrieve the data centers for the storage domain:
        List<StoragePool> dataCenters = resource.getBackendCollection(
            StoragePool.class,
            QueryType.GetStoragePoolsByStorageDomainId,
            new IdQueryParameters(storageDomainId)
        );

        // Take the first storage pool. We should only be running on NFS domains and thus should only have a single
        // storage pool to deal with.
        if (dataCenters != null && !dataCenters.isEmpty()) {
            return dataCenters.get(0).getId();
        }
        return Guid.Empty;
    }
}
