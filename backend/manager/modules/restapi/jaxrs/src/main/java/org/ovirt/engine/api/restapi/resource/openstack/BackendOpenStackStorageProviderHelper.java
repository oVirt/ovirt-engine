/*
* Copyright (c) 2014 Red Hat, Inc.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*   http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.ovirt.engine.api.restapi.resource.openstack;

import java.util.List;

import org.ovirt.engine.api.restapi.resource.BackendResource;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

/**
 * A set of utility functions for dealing with OpenStack storage (image/volume) providers.
 */
public class BackendOpenStackStorageProviderHelper {
    /**
     * Finds the identifier of the storage domain corresponding to the given provider.
     *
     * @param resource the resource that will be used to perform the operation
     * @param providerId identifier of the provider
     * @return the identifier of the corresponding storage domain or {@code null} if no such storage domain exists
     */
    public static Guid getStorageDomainId(BackendResource resource, String providerId) {
        // The backend doesn't have any mechanism to obtain the images other than listing the images provided by the
        // storage domain that is created for the provider, and the only way to find that provider is to iterate the
        // complete list. This is potentially very slow, so it should be improved in the future.
        Guid storageDomainId = null;
        List<StorageDomain> storageDomains =
                resource.runQuery(VdcQueryType.GetAllStorageDomains, new VdcQueryParametersBase()).getReturnValue();
        for (StorageDomain storageDomain : storageDomains) {
            String storageId = storageDomain.getStorage();
            if (providerId.equals(storageId)) {
                storageDomainId = storageDomain.getId();
                break;
            }
        }
        return storageDomainId;
    }
}
