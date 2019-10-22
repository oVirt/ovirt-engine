/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.servers;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.ovirt.engine.api.resource.openstack.OpenstackNetworksResource;
import org.ovirt.engine.api.v3.V3Server;
import org.ovirt.engine.api.v3.types.V3OpenStackNetworks;

@Produces({"application/xml", "application/json"})
public class V3OpenstackNetworksServer extends V3Server<OpenstackNetworksResource> {
    public V3OpenstackNetworksServer(OpenstackNetworksResource delegate) {
        super(delegate);
    }

    @GET
    public V3OpenStackNetworks list() {
        return adaptList(getDelegate()::list);
    }

    @Path("{id}")
    public V3OpenstackNetworkServer getNetworkResource(@PathParam("id") String id) {
        return new V3OpenstackNetworkServer(getDelegate().getNetworkResource(id));
    }
}
