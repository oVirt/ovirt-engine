/*
* Copyright (c) 2010 Red Hat, Inc.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*           http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.ovirt.engine.api.resource;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Role;
import org.ovirt.engine.api.model.Roles;

/**
 * Provides read-only access to the global set of roles
 */
@Path("/roles")
@Produces({ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON, ApiMediaType.APPLICATION_X_YAML})
public interface RolesResource {

    @GET
    public Roles list();

    @POST
    @Consumes({ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON, ApiMediaType.APPLICATION_X_YAML})
    public Response add(Role role);

    @DELETE
    @Path("{id}")
    public Response remove(@PathParam("id") String id);

    /**
     * Sub-resource locator method, returns individual RoleResource on which the
     * remainder of the URI is dispatched.
     *
     * @param id  the Role ID
     * @return    matching subresource if found
     */
    @Path("{id}")
    public UpdatableRoleResource getRoleSubResource(@PathParam("id") String id);
}
