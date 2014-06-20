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
import org.ovirt.engine.api.model.VM;

@Produces({ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON, ApiMediaType.APPLICATION_X_YAML})
public interface VmResource extends UpdatableResource<VM>, AsynchronouslyCreatedResource, MeasurableResource {

    @Path("{action: (start|stop|shutdown|reboot|suspend|detach|migrate|export|move|ticket|cancelmigration|preview_snapshot|commit_snapshot|undo_snapshot|clone|maintenance)}/{oid}")
    public ActionResource getActionSubresource(@PathParam("action")String action, @PathParam("oid")String oid);

    @POST
    @Consumes({ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON, ApiMediaType.APPLICATION_X_YAML})
    @Actionable
    @Path("start")
    public Response start(Action action);

    @POST
    @Consumes({ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON, ApiMediaType.APPLICATION_X_YAML})
    @Actionable
    @Path("stop")
    public Response stop(Action action);

    @POST
    @Consumes({ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON, ApiMediaType.APPLICATION_X_YAML})
    @Actionable
    @Path("shutdown")
    public Response shutdown(Action action);

    @POST
    @Consumes({ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON, ApiMediaType.APPLICATION_X_YAML})
    @Actionable
    @Path("reboot")
    public Response reboot(Action action);

    @POST
    @Consumes({ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON, ApiMediaType.APPLICATION_X_YAML})
    @Actionable
    @Path("suspend")
    public Response suspend(Action action);

    @POST
    @Consumes({ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON, ApiMediaType.APPLICATION_X_YAML})
    @Actionable
    @Path("detach")
    public Response detach(Action action);

    @POST
    @Consumes({ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON, ApiMediaType.APPLICATION_X_YAML})
    @Actionable
    @Path("migrate")
    public Response migrate(Action action);

    @POST
    @Consumes({ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON, ApiMediaType.APPLICATION_X_YAML})
    @Actionable
    @Path("export")
    public Response export(Action action);

    @Deprecated
    @POST
    @Consumes({ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON, ApiMediaType.APPLICATION_X_YAML})
    @Actionable
    @Path("move")
    public Response move(Action action);

    @POST
    @Consumes({ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON, ApiMediaType.APPLICATION_X_YAML})
    @Actionable
    @Path("ticket")
    public Response ticket(Action action);

    @POST
    @Consumes({ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON, ApiMediaType.APPLICATION_X_YAML})
    @Actionable
    @Path("preview_snapshot")
    public Response previewSnapshot(Action action);

    @POST
    @Consumes({ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON, ApiMediaType.APPLICATION_X_YAML})
    @Actionable
    @Path("commit_snapshot")
    public Response commitSnapshot(Action action);

    @POST
    @Consumes({ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON, ApiMediaType.APPLICATION_X_YAML})
    @Actionable
    @Path("undo_snapshot")
    public Response undoSnapshot(Action action);

    @POST
    @Consumes({ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON, ApiMediaType.APPLICATION_X_YAML})
    @Actionable
    @Path("clone")
    public Response cloneVm(Action action);

    @POST
    @Consumes({ ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON, ApiMediaType.APPLICATION_X_YAML })
    @Actionable
    @Path("cancelmigration")
    public Response cancelMigration(Action action);

    @POST
    @Consumes({ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON, ApiMediaType.APPLICATION_X_YAML})
    @Actionable
    @Path("maintenance")
    public Response maintenance(Action action);

    @POST
    @Consumes({ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON, ApiMediaType.APPLICATION_X_YAML})
    @Actionable
    @Path("logon")
    public Response logon(Action action);

    @Path("applications")
    public VmApplicationsResource getApplicationsResource();

    @Path("cdroms")
    public DevicesResource<CdRom, CdRoms> getCdRomsResource();

    @Path("watchdogs")
    public WatchdogsResource getWatchdogsResource();

    @Path("disks")
    public VmDisksResource getDisksResource();

    @Path("numanodes")
    public VmNumaNodesResource getVirtualNumaNodesResource();

    @Path("nics")
    public VmNicsResource getNicsResource();

    @Path("snapshots")
    public SnapshotsResource getSnapshotsResource();

    @Path("tags")
    public AssignedTagsResource getTagsResource();

    @Path("permissions")
    public AssignedPermissionsResource getPermissionsResource();

    @Path("reporteddevices")
    public VmReportedDevicesResource getVmReportedDevicesResource();

    @Path("sessions")
    public VmSessionsResource getVmSessionsResource();

}
