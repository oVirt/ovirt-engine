/*
Copyright (c) 2017 Red Hat, Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package org.ovirt.engine.api.restapi.resource;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendDataCenterHelper {
    public static Guid lookupByStorageDomainId(BackendResource resource, Guid storageDomainId) {
        // Retrieve the data centers for the storage domain:
        List<StoragePool> dataCenters = resource.getBackendCollection(
            StoragePool.class,
            VdcQueryType.GetStoragePoolsByStorageDomainId,
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
