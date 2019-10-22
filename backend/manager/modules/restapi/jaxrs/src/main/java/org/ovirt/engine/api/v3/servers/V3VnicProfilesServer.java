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

import org.ovirt.engine.api.resource.VnicProfilesResource;
import org.ovirt.engine.api.v3.V3Server;
import org.ovirt.engine.api.v3.types.V3VnicProfile;
import org.ovirt.engine.api.v3.types.V3VnicProfiles;

@Produces({"application/xml", "application/json"})
public class V3VnicProfilesServer extends V3Server<VnicProfilesResource> {
    public V3VnicProfilesServer(VnicProfilesResource delegate) {
        super(delegate);
    }

    @POST
    @Consumes({"application/xml", "application/json"})
    public Response add(V3VnicProfile profile) {
        return adaptAdd(getDelegate()::add, profile);
    }

    @GET
    public V3VnicProfiles list() {
        return adaptList(getDelegate()::list);
    }

    @Path("{id}")
    public V3VnicProfileServer getProfileResource(@PathParam("id") String id) {
        return new V3VnicProfileServer(getDelegate().getProfileResource(id));
    }
}
