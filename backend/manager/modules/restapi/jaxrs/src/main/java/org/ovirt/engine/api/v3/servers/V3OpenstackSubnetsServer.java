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

import org.ovirt.engine.api.resource.openstack.OpenstackSubnetsResource;
import org.ovirt.engine.api.v3.V3Server;
import org.ovirt.engine.api.v3.types.V3OpenStackSubnet;
import org.ovirt.engine.api.v3.types.V3OpenStackSubnets;

@Produces({"application/xml", "application/json"})
public class V3OpenstackSubnetsServer extends V3Server<OpenstackSubnetsResource> {
    public V3OpenstackSubnetsServer(OpenstackSubnetsResource delegate) {
        super(delegate);
    }

    @POST
    @Consumes({"application/xml", "application/json"})
    public Response add(V3OpenStackSubnet subnet) {
        return adaptAdd(getDelegate()::add, subnet);
    }

    @GET
    public V3OpenStackSubnets list() {
        return adaptList(getDelegate()::list);
    }

    @Path("{id}")
    public V3OpenstackSubnetServer getSubnetResource(@PathParam("id") String id) {
        return new V3OpenstackSubnetServer(getDelegate().getSubnetResource(id));
    }
}
