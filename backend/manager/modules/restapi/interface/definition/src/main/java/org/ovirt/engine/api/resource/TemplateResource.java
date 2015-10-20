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
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.Actionable;
import org.ovirt.engine.api.model.Cdrom;
import org.ovirt.engine.api.model.Cdroms;
import org.ovirt.engine.api.model.Nic;
import org.ovirt.engine.api.model.Nics;
import org.ovirt.engine.api.model.Template;

@Produces({ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON})
public interface TemplateResource extends AsynchronouslyCreatedResource {
    @GET
    Template get();

    @PUT
    @Consumes({ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON})
    Template update(Template template);

    @Path("{action: (export)}/{oid}")
    ActionResource getActionResource(@PathParam("action") String action, @PathParam("oid") String oid);

    @POST
    @Consumes({ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON})
    @Actionable
    @Path("export")
    Response export(Action action);

    @DELETE
    Response remove();

    @Path("cdroms")
    ReadOnlyDevicesResource<Cdrom, Cdroms> getCdRomsResource();

    @Path("disks")
    TemplateDisksResource getDisksResource();

    @Path("nics")
    DevicesResource<Nic, Nics> getNicsResource();

    @Path("tags")
    AssignedTagsResource getTagsResource();

    @Path("permissions")
    AssignedPermissionsResource getPermissionsResource();

    @Path("watchdogs")
    WatchdogsResource getWatchdogsResource();

    @Path("graphicsconsoles")
    GraphicsConsolesResource getGraphicsConsolesResource();
}
