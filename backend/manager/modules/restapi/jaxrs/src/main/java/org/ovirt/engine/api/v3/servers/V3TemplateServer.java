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
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Actionable;
import org.ovirt.engine.api.resource.TemplateResource;
import org.ovirt.engine.api.restapi.resource.BackendTemplateResource;
import org.ovirt.engine.api.v3.V3Server;
import org.ovirt.engine.api.v3.helpers.V3TemplateHelper;
import org.ovirt.engine.api.v3.types.V3Action;
import org.ovirt.engine.api.v3.types.V3Template;

@Produces({"application/xml", "application/json"})
public class V3TemplateServer extends V3Server<TemplateResource> {
    private String templateId;

    public V3TemplateServer(String templateId, TemplateResource delegate) {
        super(delegate);
        this.templateId = templateId;
    }

    @POST
    @Consumes({"application/xml", "application/json"})
    @Actionable
    @Path("export")
    public Response export(V3Action action) {
        return adaptAction(getDelegate()::export, action);
    }

    @GET
    public V3Template get() {
        V3Template template = adaptGet(getDelegate()::get);
        V3TemplateHelper.addDisksLink(template);
        return template;
    }

    @PUT
    @Consumes({"application/xml", "application/json"})
    public V3Template update(V3Template template) {
        template = adaptUpdate(getDelegate()::update, template);
        V3TemplateHelper.addDisksLink(template);
        return template;
    }

    @DELETE
    public Response remove() {
        return adaptRemove(getDelegate()::remove);
    }

    @Path("permissions")
    public V3AssignedPermissionsServer getPermissionsResource() {
        return new V3AssignedPermissionsServer(getDelegate().getPermissionsResource());
    }

    @Path("tags")
    public V3AssignedTagsServer getTagsResource() {
        return new V3AssignedTagsServer(getDelegate().getTagsResource());
    }

    @Path("graphicsconsoles")
    public V3GraphicsConsolesServer getGraphicsConsolesResource() {
        return new V3GraphicsConsolesServer(getDelegate().getGraphicsConsolesResource());
    }

    @Path("cdroms")
    public V3TemplateCdromsServer getCdromsResource() {
        return new V3TemplateCdromsServer(getDelegate().getCdromsResource());
    }

    @Path("disks")
    public V3TemplateDisksServer getDisksResource() {
        return new V3TemplateDisksServer(templateId, ((BackendTemplateResource) getDelegate()).getDisksResource());
    }

    @Path("nics")
    public V3TemplateNicsServer getNicsResource() {
        return new V3TemplateNicsServer(getDelegate().getNicsResource());
    }

    @Path("watchdogs")
    public V3TemplateWatchdogsServer getWatchdogsResource() {
        return new V3TemplateWatchdogsServer(getDelegate().getWatchdogsResource());
    }

    @Path("{action: (export)}/{oid}")
    public V3ActionServer getActionResource(@PathParam("action") String action, @PathParam("oid") String oid) {
        return new V3ActionServer(getDelegate().getActionResource(action, oid));
    }
}
