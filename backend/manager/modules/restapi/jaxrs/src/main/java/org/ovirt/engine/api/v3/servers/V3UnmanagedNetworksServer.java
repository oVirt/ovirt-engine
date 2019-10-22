/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.servers;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.ovirt.engine.api.resource.UnmanagedNetworksResource;
import org.ovirt.engine.api.v3.V3Server;
import org.ovirt.engine.api.v3.types.V3UnmanagedNetworks;

@Produces({"application/xml", "application/json"})
public class V3UnmanagedNetworksServer extends V3Server<UnmanagedNetworksResource> {
    public V3UnmanagedNetworksServer(UnmanagedNetworksResource delegate) {
        super(delegate);
    }

    @GET
    public V3UnmanagedNetworks list() {
        return adaptList(getDelegate()::list);
    }

    @Path("{id}")
    public V3UnmanagedNetworkServer getUnmanagedNetworkResource(@PathParam("id") String id) {
        return new V3UnmanagedNetworkServer(getDelegate().getUnmanagedNetworkResource(id));
    }
}
