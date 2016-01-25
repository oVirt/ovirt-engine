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
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.OpenStackVolumeProvider;
import org.ovirt.engine.api.model.OpenstackVolumeAuthenticationKey;
import org.ovirt.engine.api.model.OpenstackVolumeAuthenticationKeys;
import org.ovirt.engine.api.resource.openstack.OpenstackVolumeAuthenticationKeyResource;
import org.ovirt.engine.api.resource.openstack.OpenstackVolumeAuthenticationKeysResource;
import org.ovirt.engine.api.restapi.resource.AbstractBackendCollectionResource;
import org.ovirt.engine.api.restapi.utils.GuidUtils;
import org.ovirt.engine.core.common.action.LibvirtSecretParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.storage.LibvirtSecret;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendOpenStackVolumeAuthenticationKeysResource
        extends AbstractBackendCollectionResource<OpenstackVolumeAuthenticationKey, LibvirtSecret>
        implements OpenstackVolumeAuthenticationKeysResource {
    private String providerId;

    public BackendOpenStackVolumeAuthenticationKeysResource(String providerId) {
        super(OpenstackVolumeAuthenticationKey.class, LibvirtSecret.class);
        this.providerId = providerId;
    }

    @Override
    public OpenstackVolumeAuthenticationKeys list() {
        IdQueryParameters parameters = new IdQueryParameters(GuidUtils.asGuid(providerId));
        return mapCollection(getBackendCollection(VdcQueryType.GetAllLibvirtSecretsByProviderId, parameters));
    }

    @Override
    public Response add(OpenstackVolumeAuthenticationKey authenticationKey) {
        validateParameters(authenticationKey, "uuid", "value", "usageType");
        return performCreate(
                VdcActionType.AddLibvirtSecret,
                new LibvirtSecretParameters(map(addProvider(authenticationKey))),
                new QueryIdResolver<Guid>(VdcQueryType.GetLibvirtSecretById, IdQueryParameters.class)
        );
    }

    protected OpenstackVolumeAuthenticationKeys mapCollection(List<LibvirtSecret> entities) {
        OpenstackVolumeAuthenticationKeys collection = new OpenstackVolumeAuthenticationKeys();
        for (LibvirtSecret libvirtSecret : entities) {
            collection.getOpenstackVolumeAuthenticationKeys().add(addLinks(populate(map(libvirtSecret), libvirtSecret)));
        }
        return collection;
    }

    @Override
    protected OpenstackVolumeAuthenticationKey addParents(OpenstackVolumeAuthenticationKey authenticationKey) {
        return super.addParents(addProvider(authenticationKey));
    }

    private OpenstackVolumeAuthenticationKey addProvider(OpenstackVolumeAuthenticationKey authenticationKey) {
        OpenStackVolumeProvider provider = new OpenStackVolumeProvider();
        provider.setId(providerId);
        authenticationKey.setOpenstackVolumeProvider(provider);
        return authenticationKey;
    }

    @Override
    public OpenstackVolumeAuthenticationKeyResource getKeyResource(String id) {
        return inject(new BackendOpenStackVolumeAuthenticationKeyResource(providerId, id));
    }
}
