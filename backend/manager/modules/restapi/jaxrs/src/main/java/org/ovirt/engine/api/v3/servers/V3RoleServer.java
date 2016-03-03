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
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.resource.RoleResource;
import org.ovirt.engine.api.v3.V3Server;
import org.ovirt.engine.api.v3.types.V3Role;

@Produces({"application/xml", "application/json"})
public class V3RoleServer extends V3Server<RoleResource> {
    public V3RoleServer(RoleResource delegate) {
        super(delegate);
    }

    @GET
    public V3Role get() {
        return adaptGet(getDelegate()::get);
    }

    @DELETE
    public Response remove() {
        return adaptRemove(getDelegate()::remove);
    }

    @PUT
    @Consumes({"application/xml", "application/json"})
    public V3Role update(V3Role role) {
        return adaptUpdate(getDelegate()::update, role);
    }

    @Path("permits")
    public V3PermitsServer getPermitsResource() {
        return new V3PermitsServer(getDelegate().getPermitsResource());
    }
}
