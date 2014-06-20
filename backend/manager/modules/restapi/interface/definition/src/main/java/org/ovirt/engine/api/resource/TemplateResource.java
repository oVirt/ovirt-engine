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
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.Actionable;
import org.ovirt.engine.api.model.CdRom;
import org.ovirt.engine.api.model.CdRoms;
import org.ovirt.engine.api.model.NIC;
import org.ovirt.engine.api.model.Nics;
import org.ovirt.engine.api.model.Template;

@Produces({ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON, ApiMediaType.APPLICATION_X_YAML})
public interface TemplateResource extends UpdatableResource<Template>, AsynchronouslyCreatedResource {

    @Path("{action: (export)}/{oid}")
    public ActionResource getActionSubresource(@PathParam("action") String action, @PathParam("oid") String oid);

    @POST
    @Consumes({ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON, ApiMediaType.APPLICATION_X_YAML})
    @Actionable
    @Path("export")
    public Response export(Action action);

    @Path("cdroms")
    public ReadOnlyDevicesResource<CdRom, CdRoms> getCdRomsResource();

    @Path("disks")
    public TemplateDisksResource getDisksResource();

    @Path("nics")
    public DevicesResource<NIC, Nics> getNicsResource();

    @Path("tags")
    public AssignedTagsResource getTagsResource();

    @Path("permissions")
    public AssignedPermissionsResource getPermissionsResource();

    @Path("watchdogs")
    public WatchdogsResource getWatchdogsResource();

}
