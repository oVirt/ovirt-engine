/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.servers;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.resource.openstack.OpenstackSubnetResource;
import org.ovirt.engine.api.v3.V3Server;
import org.ovirt.engine.api.v3.types.V3OpenStackSubnet;

@Produces({"application/xml", "application/json"})
public class V3OpenstackSubnetServer extends V3Server<OpenstackSubnetResource> {
    public V3OpenstackSubnetServer(OpenstackSubnetResource delegate) {
        super(delegate);
    }

    @GET
    public V3OpenStackSubnet get() {
        return adaptGet(getDelegate()::get);
    }

    @DELETE
    public Response remove() {
        return adaptRemove(getDelegate()::remove);
    }
}
