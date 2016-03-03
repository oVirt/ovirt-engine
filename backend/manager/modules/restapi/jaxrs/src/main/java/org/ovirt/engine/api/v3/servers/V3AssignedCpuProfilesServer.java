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

import org.ovirt.engine.api.resource.AssignedCpuProfilesResource;
import org.ovirt.engine.api.v3.V3Server;
import org.ovirt.engine.api.v3.types.V3CpuProfile;
import org.ovirt.engine.api.v3.types.V3CpuProfiles;

@Produces({"application/xml", "application/json"})
public class V3AssignedCpuProfilesServer extends V3Server<AssignedCpuProfilesResource> {
    public V3AssignedCpuProfilesServer(AssignedCpuProfilesResource delegate) {
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
    public V3AssignedCpuProfileServer getProfileResource(@PathParam("id") String id) {
        return new V3AssignedCpuProfileServer(getDelegate().getProfileResource(id));
    }
}
