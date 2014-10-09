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

package org.ovirt.engine.api.restapi.resource;

import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

/**
 * A collection of functions useful for dealing with external providers.
 */
public class BackendExternalProviderHelper {
    /**
     * Finds the provider that corresponds to the given identifier.
     *
     * @param resource the resource that will be used to perform the required queries
     * @param id the identifier of the provider
     * @return the reference to the provider or {@code null} if no such provider exists
     */
    public static Provider getProvider(BackendResource resource, String id) {
        Guid guid = Guid.createGuidFromString(id);
        IdQueryParameters parameters = new IdQueryParameters(guid);
        return resource.getEntity(Provider.class, VdcQueryType.GetProviderById, parameters, id, true);
    }
}
