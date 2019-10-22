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

import org.ovirt.engine.api.resource.openstack.OpenstackVolumeAuthenticationKeysResource;
import org.ovirt.engine.api.v3.V3Server;
import org.ovirt.engine.api.v3.types.V3OpenstackVolumeAuthenticationKey;
import org.ovirt.engine.api.v3.types.V3OpenstackVolumeAuthenticationKeys;

@Produces({"application/xml", "application/json"})
public class V3OpenstackVolumeAuthenticationKeysServer extends V3Server<OpenstackVolumeAuthenticationKeysResource> {
    public V3OpenstackVolumeAuthenticationKeysServer(OpenstackVolumeAuthenticationKeysResource delegate) {
        super(delegate);
    }

    @POST
    @Consumes({"application/xml", "application/json"})
    public Response add(V3OpenstackVolumeAuthenticationKey key) {
        return adaptAdd(getDelegate()::add, key);
    }

    @GET
    public V3OpenstackVolumeAuthenticationKeys list() {
        return adaptList(getDelegate()::list);
    }

    @Path("{id}")
    public V3OpenstackVolumeAuthenticationKeyServer getKeyResource(@PathParam("id") String id) {
        return new V3OpenstackVolumeAuthenticationKeyServer(getDelegate().getKeyResource(id));
    }
}
