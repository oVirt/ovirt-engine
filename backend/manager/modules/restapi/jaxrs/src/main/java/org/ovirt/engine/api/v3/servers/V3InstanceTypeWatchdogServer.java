/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.servers;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.resource.InstanceTypeWatchdogResource;
import org.ovirt.engine.api.v3.V3Server;
import org.ovirt.engine.api.v3.types.V3WatchDog;

@Produces({"application/xml", "application/json"})
public class V3InstanceTypeWatchdogServer extends V3Server<InstanceTypeWatchdogResource> {
    public V3InstanceTypeWatchdogServer(InstanceTypeWatchdogResource delegate) {
        super(delegate);
    }

    @GET
    public V3WatchDog get() {
        return adaptGet(getDelegate()::get);
    }

    @PUT
    @Consumes({"application/xml", "application/json"})
    public V3WatchDog update(V3WatchDog watchdog) {
        return adaptUpdate(getDelegate()::update, watchdog);
    }

    @DELETE
    public Response remove() {
        return adaptRemove(getDelegate()::remove);
    }

    @Path("creation_status/{oid}")
    public V3CreationServer getCreationResource(@PathParam("oid") String oid) {
        return new V3CreationServer(getDelegate().getCreationResource(oid));
    }
}
