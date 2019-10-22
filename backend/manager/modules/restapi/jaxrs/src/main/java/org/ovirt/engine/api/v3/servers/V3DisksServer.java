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

import org.ovirt.engine.api.resource.DisksResource;
import org.ovirt.engine.api.v3.V3Server;
import org.ovirt.engine.api.v3.types.V3Disk;
import org.ovirt.engine.api.v3.types.V3Disks;

@Produces({"application/xml", "application/json"})
public class V3DisksServer extends V3Server<DisksResource> {
    public V3DisksServer(DisksResource delegate) {
        super(delegate);
    }

    @POST
    @Consumes({"application/xml", "application/json"})
    public Response add(V3Disk disk) {
        return adaptAdd(getDelegate()::add, disk);
    }

    @GET
    public V3Disks list() {
        return adaptList(getDelegate()::list);
    }

    @Path("{id}")
    public V3DiskServer getDiskResource(@PathParam("id") String id) {
        return new V3DiskServer(getDelegate().getDiskResource(id));
    }
}
