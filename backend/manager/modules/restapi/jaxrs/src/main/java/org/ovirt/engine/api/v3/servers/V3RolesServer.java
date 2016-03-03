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

import org.ovirt.engine.api.resource.RolesResource;
import org.ovirt.engine.api.v3.V3Server;
import org.ovirt.engine.api.v3.types.V3Role;
import org.ovirt.engine.api.v3.types.V3Roles;

@Produces({"application/xml", "application/json"})
public class V3RolesServer extends V3Server<RolesResource> {
    public V3RolesServer(RolesResource delegate) {
        super(delegate);
    }

    @POST
    @Consumes({"application/xml", "application/json"})
    public Response add(V3Role role) {
        return adaptAdd(getDelegate()::add, role);
    }

    @GET
    public V3Roles list() {
        return adaptList(getDelegate()::list);
    }

    @Path("{id}")
    public V3RoleServer getRoleResource(@PathParam("id") String id) {
        return new V3RoleServer(getDelegate().getRoleResource(id));
    }
}
