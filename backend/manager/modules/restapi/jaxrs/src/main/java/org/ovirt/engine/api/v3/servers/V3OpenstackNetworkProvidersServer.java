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

import org.ovirt.engine.api.resource.openstack.OpenstackNetworkProvidersResource;
import org.ovirt.engine.api.v3.V3Server;
import org.ovirt.engine.api.v3.types.V3OpenStackNetworkProvider;
import org.ovirt.engine.api.v3.types.V3OpenStackNetworkProviders;

@Produces({"application/xml", "application/json"})
public class V3OpenstackNetworkProvidersServer extends V3Server<OpenstackNetworkProvidersResource> {
    public V3OpenstackNetworkProvidersServer(OpenstackNetworkProvidersResource delegate) {
        super(delegate);
    }

    @POST
    @Consumes({"application/xml", "application/json"})
    public Response add(V3OpenStackNetworkProvider provider) {
        return adaptAdd(getDelegate()::add, provider);
    }

    @GET
    public V3OpenStackNetworkProviders list() {
        return adaptList(getDelegate()::list);
    }

    @Path("{id}")
    public V3OpenstackNetworkProviderServer getProviderResource(@PathParam("id") String id) {
        return new V3OpenstackNetworkProviderServer(getDelegate().getProviderResource(id));
    }
}
