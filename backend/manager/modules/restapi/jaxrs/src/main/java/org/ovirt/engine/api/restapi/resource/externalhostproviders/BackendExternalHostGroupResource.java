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

import org.ovirt.engine.api.model.ExternalHostGroup;
import org.ovirt.engine.api.model.ExternalHostProvider;
import org.ovirt.engine.api.resource.externalhostproviders.ExternalHostGroupResource;
import org.ovirt.engine.api.restapi.resource.AbstractBackendActionableResource;
import org.ovirt.engine.api.restapi.resource.BackendExternalProviderHelper;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.queries.ProviderQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendExternalHostGroupResource
        extends AbstractBackendActionableResource<ExternalHostGroup, org.ovirt.engine.core.common.businessentities.ExternalHostGroup>
        implements ExternalHostGroupResource {
    private String providerId;

    protected BackendExternalHostGroupResource(String id, String providerId) {
        super(id, ExternalHostGroup.class, org.ovirt.engine.core.common.businessentities.ExternalHostGroup.class);
        this.providerId = providerId;
    }

    @Override
    public ExternalHostGroup get() {
        // Convert the resource identifier to the host group name:
        String name = hex2string(id);

        // The backend query that retrieves the list of hosts groups needs a complete provider instance, the id isn't
        // enough:
        Provider provider = BackendExternalProviderHelper.getProvider(this, providerId);

        // The backend doesn't have a way to retrieve a host by ide, so we have to iterate them:
        ProviderQueryParameters parameters = new ProviderQueryParameters();
        parameters.setProvider(provider);
        VdcQueryReturnValue result = runQuery(VdcQueryType.GetHostGroupsFromExternalProvider, parameters);
        List<org.ovirt.engine.core.common.businessentities.ExternalHostGroup> entities =  result.getReturnValue();
        if (entities != null) {
            for (org.ovirt.engine.core.common.businessentities.ExternalHostGroup entity : entities) {
                if (name.equals(entity.getName())) {
                    return addLinks(populate(map(entity), entity));
                }
            }
        }

        // No luck:
        return notFound();
    }

    @Override
    protected ExternalHostGroup addParents(ExternalHostGroup image) {
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
