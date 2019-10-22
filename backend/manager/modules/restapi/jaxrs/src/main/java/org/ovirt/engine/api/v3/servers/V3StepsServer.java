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

import org.ovirt.engine.api.resource.StepsResource;
import org.ovirt.engine.api.v3.V3Server;
import org.ovirt.engine.api.v3.types.V3Step;
import org.ovirt.engine.api.v3.types.V3Steps;

@Produces({"application/xml", "application/json"})
public class V3StepsServer extends V3Server<StepsResource> {
    public V3StepsServer(StepsResource delegate) {
        super(delegate);
    }

    @POST
    @Consumes({"application/xml", "application/json"})
    public Response add(V3Step step) {
        return adaptAdd(getDelegate()::add, step);
    }

    @GET
    public V3Steps list() {
        return adaptList(getDelegate()::list);
    }

    @Path("{id}")
    public V3StepServer getStepResource(@PathParam("id") String id) {
        return new V3StepServer(getDelegate().getStepResource(id));
    }
}
