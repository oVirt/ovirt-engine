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

package org.ovirt.engine.api.restapi.resource.externalhostproviders;

import static org.ovirt.engine.api.restapi.utils.HexUtils.hex2string;

import java.util.List;

import org.ovirt.engine.api.model.ExternalHost;
import org.ovirt.engine.api.model.ExternalHostProvider;
import org.ovirt.engine.api.resource.externalhostproviders.ExternalHostResource;
import org.ovirt.engine.api.restapi.resource.AbstractBackendActionableResource;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.queries.GetHostListFromExternalProviderParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendExternalHostResource
        extends AbstractBackendActionableResource<ExternalHost, VDS>
        implements ExternalHostResource {
    private String providerId;

    protected BackendExternalHostResource(String id, String providerId) {
        super(id, ExternalHost.class, VDS.class);
        this.providerId = providerId;
    }

    @Override
    public ExternalHost get() {
        // Convert the resource identifier to the host group name:
        String name = hex2string(id);

        // The backend doesn't have a way to retrieve a host by ide, so we have to iterate them:
        GetHostListFromExternalProviderParameters parameters = new GetHostListFromExternalProviderParameters();
        parameters.setFilterOutExistingHosts(true);
        parameters.setProviderId(asGuid(providerId));
        VdcQueryReturnValue result = runQuery(VdcQueryType.GetHostListFromExternalProvider, parameters);
        List<VDS> entities = result.getReturnValue();
        if (entities != null) {
            for (VDS entity : entities) {
                if (name.equals(entity.getName())) {
                    return addLinks(populate(map(entity), entity));
                }
            }
        }

        // No luck:
        return notFound();
    }

    @Override
    protected ExternalHost addParents(ExternalHost image) {
        ExternalHostProvider provider = new ExternalHostProvider();
        provider.setId(providerId);
        image.setExternalHostProvider(provider);
        return super.addParents(image);
    }

    @Override
    protected Guid asGuidOr404(String id) {
        // The identifier isn't a UUID.
        return null;
    }
}
