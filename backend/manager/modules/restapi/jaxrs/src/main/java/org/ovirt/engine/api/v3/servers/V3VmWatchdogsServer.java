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

import org.ovirt.engine.api.resource.VmWatchdogsResource;
import org.ovirt.engine.api.v3.V3Server;
import org.ovirt.engine.api.v3.types.V3WatchDog;
import org.ovirt.engine.api.v3.types.V3WatchDogs;

@Produces({"application/xml", "application/json"})
public class V3VmWatchdogsServer extends V3Server<VmWatchdogsResource> {
    public V3VmWatchdogsServer(VmWatchdogsResource delegate) {
        super(delegate);
    }

    @POST
    @Consumes({"application/xml", "application/json"})
    public Response add(V3WatchDog watchdog) {
        return adaptAdd(getDelegate()::add, watchdog);
    }

    @GET
    public V3WatchDogs list() {
        return adaptList(getDelegate()::list);
    }

    @Path("{id}")
    public V3VmWatchdogServer getWatchdogResource(@PathParam("id") String id) {
        return new V3VmWatchdogServer(getDelegate().getWatchdogResource(id));
    }
}
