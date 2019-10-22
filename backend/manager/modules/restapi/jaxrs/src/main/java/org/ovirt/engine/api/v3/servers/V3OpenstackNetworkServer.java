/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.servers;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.ovirt.engine.api.resource.openstack.OpenstackNetworkResource;
import org.ovirt.engine.api.v3.V3Server;
import org.ovirt.engine.api.v3.types.V3OpenStackNetwork;

@Produces({"application/xml", "application/json"})
public class V3OpenstackNetworkServer extends V3Server<OpenstackNetworkResource> {
    public V3OpenstackNetworkServer(OpenstackNetworkResource delegate) {
        super(delegate);
    }

    @GET
    public V3OpenStackNetwork get() {
        return adaptGet(getDelegate()::get);
    }

    @Path("subnets")
    public V3OpenstackSubnetsServer getSubnetsResource() {
        return new V3OpenstackSubnetsServer(getDelegate().getSubnetsResource());
    }
}
