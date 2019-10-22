/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.servers;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.resource.externalhostproviders.ExternalHostProvidersResource;
import org.ovirt.engine.api.v3.V3Server;
import org.ovirt.engine.api.v3.types.V3ExternalHostProvider;
import org.ovirt.engine.api.v3.types.V3ExternalHostProviders;

@Produces({"application/xml", "application/json"})
public class V3ExternalHostProvidersServer extends V3Server<ExternalHostProvidersResource> {
    public V3ExternalHostProvidersServer(ExternalHostProvidersResource delegate) {
        super(delegate);
    }

    @POST
    @Consumes({"application/xml", "application/json"})
    public Response add(V3ExternalHostProvider provider) {
        return adaptAdd(getDelegate()::add, provider);
    }

    @GET
    public V3ExternalHostProviders list() {
        return adaptList(getDelegate()::list);
    }

    @Path("{id}")
    public V3ExternalHostProviderServer getProviderResource(@PathParam("id") String id) {
        return new V3ExternalHostProviderServer(getDelegate().getProviderResource(id));
    }
}
