/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.servers;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.resource.openstack.OpenstackVolumeAuthenticationKeyResource;
import org.ovirt.engine.api.v3.V3Server;
import org.ovirt.engine.api.v3.types.V3OpenstackVolumeAuthenticationKey;

@Produces({"application/xml", "application/json"})
public class V3OpenstackVolumeAuthenticationKeyServer extends V3Server<OpenstackVolumeAuthenticationKeyResource> {
    public V3OpenstackVolumeAuthenticationKeyServer(OpenstackVolumeAuthenticationKeyResource delegate) {
        super(delegate);
    }

    @GET
    public V3OpenstackVolumeAuthenticationKey get() {
        return adaptGet(getDelegate()::get);
    }

    @PUT
    @Consumes({"application/xml", "application/json"})
    public V3OpenstackVolumeAuthenticationKey update(V3OpenstackVolumeAuthenticationKey key) {
        return adaptUpdate(getDelegate()::update, key);
    }

    @DELETE
    public Response remove() {
        return adaptRemove(getDelegate()::remove);
    }
}
