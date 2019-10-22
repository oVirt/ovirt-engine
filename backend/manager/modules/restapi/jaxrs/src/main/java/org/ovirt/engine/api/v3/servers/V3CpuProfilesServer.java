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

import org.ovirt.engine.api.resource.CpuProfilesResource;
import org.ovirt.engine.api.v3.V3Server;
import org.ovirt.engine.api.v3.types.V3CpuProfile;
import org.ovirt.engine.api.v3.types.V3CpuProfiles;

@Produces({"application/xml", "application/json"})
public class V3CpuProfilesServer extends V3Server<CpuProfilesResource> {
    public V3CpuProfilesServer(CpuProfilesResource delegate) {
        super(delegate);
    }

    @POST
    @Consumes({"application/xml", "application/json"})
    public Response add(V3CpuProfile profile) {
        return adaptAdd(getDelegate()::add, profile);
    }

    @GET
    public V3CpuProfiles list() {
        return adaptList(getDelegate()::list);
    }

    @Path("{id}")
    public V3CpuProfileServer getProfileResource(@PathParam("id") String id) {
        return new V3CpuProfileServer(getDelegate().getProfileResource(id));
    }
}
