/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.servers;

import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.resource.TemplatesResource;
import org.ovirt.engine.api.restapi.invocation.CurrentManager;
import org.ovirt.engine.api.v3.V3Server;
import org.ovirt.engine.api.v3.helpers.V3TemplateHelper;
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
    public Response add(V3Template template) {
        // V3 version of the API used the "template.permissions.clone" element to indicate if the permissions should be
        // cloned, but in V4 this element has been removed and replaced by a "clone_permissions" parameter:
        if (template.isSetPermissions()) {
            V3Permissions permissions = template.getPermissions();
            if (permissions.isSetClone() && permissions.isClone()) {
                Map<String, String> parameters = CurrentManager.get().getParameters();
                parameters.put("clone_permissions", String.valueOf(true));
            }
        }
        Response response = adaptAdd(getDelegate()::add, template);
        V3TemplateHelper.addDisksLink(response);
        return response;
    }

    @GET
    public V3Templates list() {
        V3Templates templates = adaptList(getDelegate()::list);
        templates.getTemplates().forEach(V3TemplateHelper::addDisksLink);
        return templates;
    }

    @Path("{id}")
    public V3TemplateServer getTemplateResource(@PathParam("id") String id) {
        return new V3TemplateServer(id, getDelegate().getTemplateResource(id));
    }
}
