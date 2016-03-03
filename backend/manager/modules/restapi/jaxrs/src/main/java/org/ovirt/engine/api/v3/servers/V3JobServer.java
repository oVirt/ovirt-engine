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

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Actionable;
import org.ovirt.engine.api.resource.JobResource;
import org.ovirt.engine.api.v3.V3Server;
import org.ovirt.engine.api.v3.types.V3Action;
import org.ovirt.engine.api.v3.types.V3Job;

@Produces({"application/xml", "application/json"})
public class V3JobServer extends V3Server<JobResource> {
    public V3JobServer(JobResource delegate) {
        super(delegate);
    }

    @POST
    @Consumes({"application/xml", "application/json"})
    @Actionable
    @Path("clear")
    public Response clear(V3Action action) {
        return adaptAction(getDelegate()::clear, action);
    }

    @POST
    @Consumes({"application/xml", "application/json"})
    @Actionable
    @Path("end")
    public Response end(V3Action action) {
        return adaptAction(getDelegate()::end, action);
    }

    @GET
    public V3Job get() {
        return adaptGet(getDelegate()::get);
    }

    @Path("steps")
    public V3StepsServer getStepsResource() {
        return new V3StepsServer(getDelegate().getStepsResource());
    }

    @Path("{action: (clear|end)}/{oid}")
    public V3ActionServer getActionResource(@PathParam("action") String action, @PathParam("oid") String oid) {
        return new V3ActionServer(getDelegate().getActionResource(action, oid));
    }
}
