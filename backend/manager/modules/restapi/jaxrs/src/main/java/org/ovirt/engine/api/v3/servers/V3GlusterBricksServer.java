/*
Copyright (c) 2016 Red Hat, Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package org.ovirt.engine.api.v3.servers;

import static org.ovirt.engine.api.v3.adapters.V3InAdapters.adaptIn;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Actionable;
import org.ovirt.engine.api.resource.gluster.GlusterBricksResource;
import org.ovirt.engine.api.v3.V3Server;
import org.ovirt.engine.api.v3.types.V3Action;
import org.ovirt.engine.api.v3.types.V3GlusterBricks;

@Produces({"application/xml", "application/json"})
public class V3GlusterBricksServer extends V3Server<GlusterBricksResource> {
    public V3GlusterBricksServer(GlusterBricksResource delegate) {
        super(delegate);
    }

    @POST
    @Consumes({"application/xml", "application/json"})
    @Actionable
    @Path("activate")
    public Response activate(V3Action action) {
        return adaptAction(getDelegate()::activate, action);
    }

    @POST
    @Consumes({"application/xml", "application/json"})
    public Response add(V3GlusterBricks bricks) {
        return adaptAdd(getDelegate()::add, bricks);
    }

    @GET
    public V3GlusterBricks list() {
        return adaptList(getDelegate()::list);
    }

    @POST
    @Consumes({"application/xml", "application/json"})
    @Actionable
    @Path("migrate")
    public Response migrate(V3Action action) {
        return adaptAction(getDelegate()::migrate, action);
    }

    @DELETE
    @Consumes({"application/xml", "application/json"})
    public Response remove(V3Action action) {
        try {
            return adaptResponse(getDelegate().remove(adaptIn(action)));
        }
        catch (WebApplicationException exception) {
            throw adaptException(exception);
        }
    }

    @POST
    @Consumes({"application/xml", "application/json"})
    @Actionable
    @Path("stopmigrate")
    public Response stopMigrate(V3Action action) {
        return adaptAction(getDelegate()::stopMigrate, action);
    }

    @Path("{id}")
    public V3GlusterBrickServer getBrickResource(@PathParam("id") String id) {
        return new V3GlusterBrickServer(getDelegate().getBrickResource(id));
    }

    @Path("{action: (activate|migrate|stopmigrate)}/{oid}")
    public V3ActionServer getActionResource(@PathParam("action") String action, @PathParam("oid") String oid) {
        return new V3ActionServer(getDelegate().getActionResource(action, oid));
    }
}
