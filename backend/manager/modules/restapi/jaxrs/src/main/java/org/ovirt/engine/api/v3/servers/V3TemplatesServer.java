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

import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.ovirt.engine.api.resource.TemplatesResource;
import org.ovirt.engine.api.v3.V3Server;
import org.ovirt.engine.api.v3.types.V3Permissions;
import org.ovirt.engine.api.v3.types.V3Template;
import org.ovirt.engine.api.v3.types.V3Templates;

@Produces({"application/xml", "application/json"})
public class V3TemplatesServer extends V3Server<TemplatesResource> {
    public V3TemplatesServer(TemplatesResource delegate) {
        super(delegate);
    }

    @POST
    @Consumes({"application/xml", "application/json"})
    public Response add(@Context UriInfo ui, V3Template template) {
        // V3 version of the API used the "template.permissions.clone" element to indicate if the permissions should be
        // cloned, but in V4 this element has been removed and replaced by a "clone_permissions" matrix parameter:
        List<PathSegment> segments = ui.getPathSegments();
        PathSegment segment = segments.get(segments.size() - 1);
        MultivaluedMap<String, String> matrix = segment.getMatrixParameters();
        if (template.isSetPermissions()) {
            V3Permissions permissions = template.getPermissions();
            if (permissions.isSetClone() && permissions.isClone()) {
                matrix.putSingle("clone_permissions", String.valueOf(true));
            }
        }
        return adaptAdd(getDelegate()::add, template);
    }

    @GET
    public V3Templates list() {
        return adaptList(getDelegate()::list);
    }

    @Path("{id}")
    public V3TemplateServer getTemplateResource(@PathParam("id") String id) {
        return new V3TemplateServer(getDelegate().getTemplateResource(id));
    }
}
