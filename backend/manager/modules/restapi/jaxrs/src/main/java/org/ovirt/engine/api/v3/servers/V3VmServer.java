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

import java.util.Map;
import java.util.Set;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.ovirt.engine.api.common.util.DetailHelper;
import org.ovirt.engine.api.model.Actionable;
import org.ovirt.engine.api.resource.VmResource;
import org.ovirt.engine.api.restapi.invocation.CurrentManager;
import org.ovirt.engine.api.restapi.resource.BackendVmResource;
import org.ovirt.engine.api.v3.V3Server;
import org.ovirt.engine.api.v3.helpers.V3VmHelper;
import org.ovirt.engine.api.v3.types.V3Action;
import org.ovirt.engine.api.v3.types.V3Disks;
import org.ovirt.engine.api.v3.types.V3VM;

@Produces({"application/xml", "application/json"})
public class V3VmServer extends V3Server<VmResource> {
    private String id;

    public V3VmServer(String id, VmResource delegate) {
        super(delegate);
        this.id = id;
    }

    @POST
    @Consumes({"application/xml", "application/json"})
    @Actionable
    @Path("cancelmigration")
    public Response cancelMigration(V3Action action) {
        return adaptAction(getDelegate()::cancelMigration, action);
    }

    @POST
    @Consumes({"application/xml", "application/json"})
    @Actionable
    @Path("commit_snapshot")
    public Response commitSnapshot(V3Action action) {
        return adaptAction(getDelegate()::commitSnapshot, action);
    }

    @POST
    @Consumes({"application/xml", "application/json"})
    @Actionable
    @Path("clone")
    public Response doClone(V3Action action) {
        return adaptAction(getDelegate()::doClone, action);
    }

    @POST
    @Consumes({"application/xml", "application/json"})
    @Actionable
    @Path("detach")
    public Response detach(V3Action action) {
        return adaptAction(getDelegate()::detach, action);
    }

    @POST
    @Consumes({"application/xml", "application/json"})
    @Actionable
    @Path("export")
    public Response export(V3Action action) {
        return adaptAction(getDelegate()::export, action);
    }

    @POST
    @Consumes({"application/xml", "application/json"})
    @Actionable
    @Path("freezefilesystems")
    public Response freezeFilesystems(V3Action action) {
        return adaptAction(getDelegate()::freezeFilesystems, action);
    }

    @GET
    public V3VM get(@Context HttpHeaders headers, @Context UriInfo ui) {
        // Transfor the object:
        V3VM vm = adaptGet(getDelegate()::get);

        // Add the disks links:
        V3VmHelper.addDisksLink(vm);

        // Add the required inline details:
        Set<String> details = DetailHelper.getDetails(headers, ui);
        if (details != null && !details.isEmpty()) {
            V3VmHelper.addInlineDetails(vm, this, details);
        }
        return vm;
    }

    @POST
    @Consumes({"application/xml", "application/json"})
    @Actionable
    @Path("logon")
    public Response logon(V3Action action) {
        return adaptAction(getDelegate()::logon, action);
    }

    @POST
    @Consumes({"application/xml", "application/json"})
    @Actionable
    @Path("maintenance")
    public Response maintenance(V3Action action) {
        return adaptAction(getDelegate()::maintenance, action);
    }

    @POST
    @Consumes({"application/xml", "application/json"})
    @Actionable
    @Path("migrate")
    public Response migrate(V3Action action) {
        return adaptAction(getDelegate()::migrate, action);
    }

    @POST
    @Consumes({"application/xml", "application/json"})
    @Actionable
    @Path("preview_snapshot")
    public Response previewSnapshot(V3Action action) {
        return adaptAction(getDelegate()::previewSnapshot, action);
    }

    @PUT
    @Consumes({"application/xml", "application/json"})
    public V3VM update(V3VM vm) {
        vm = adaptUpdate(getDelegate()::update, vm);
        V3VmHelper.addDisksLink(vm);
        return vm;
    }

    @POST
    @Consumes({"application/xml", "application/json"})
    @Actionable
    @Path("reboot")
    public Response reboot(V3Action action) {
        return adaptAction(getDelegate()::reboot, action);
    }

    @DELETE
    public Response remove() {
        return adaptRemove(getDelegate()::remove);
    }

    @DELETE
    @Consumes({"application/xml", "application/json"})
    public Response remove(V3Action action) {
        // V3 version of the API used the action "force" element and the disks "detach_disks" elements as parameters,
        // but in V4 this has been replaced with equivalent parameters:
        Map<String, String> parameters = CurrentManager.get().getParameters();
        if (action.isSetForce() && action.isForce()) {
            parameters.put("force", String.valueOf(true));
        }
        if (action.isSetVm() && action.getVm().isSetDisks()) {
            V3Disks disks = action.getVm().getDisks();
            if (disks.isSetDetachOnly() && disks.isDetachOnly()) {
                parameters.put("detach_only", String.valueOf(true));
            }
        }
        return adaptRemove(getDelegate()::remove);
    }

    @POST
    @Consumes({"application/xml", "application/json"})
    @Actionable
    @Path("reordermacaddresses")
    public Response reorderMacAddresses(V3Action action) {
        return adaptAction(getDelegate()::reorderMacAddresses, action);
    }

    @POST
    @Consumes({"application/xml", "application/json"})
    @Actionable
    @Path("shutdown")
    public Response shutdown(V3Action action) {
        return adaptAction(getDelegate()::shutdown, action);
    }

    @POST
    @Consumes({"application/xml", "application/json"})
    @Actionable
    @Path("start")
    public Response start(V3Action action) {
        return adaptAction(getDelegate()::start, action);
    }

    @POST
    @Consumes({"application/xml", "application/json"})
    @Actionable
    @Path("stop")
    public Response stop(V3Action action) {
        return adaptAction(getDelegate()::stop, action);
    }

    @POST
    @Consumes({"application/xml", "application/json"})
    @Actionable
    @Path("suspend")
    public Response suspend(V3Action action) {
        return adaptAction(getDelegate()::suspend, action);
    }

    @POST
    @Consumes({"application/xml", "application/json"})
    @Actionable
    @Path("thawfilesystems")
    public Response thawFilesystems(V3Action action) {
        return adaptAction(getDelegate()::thawFilesystems, action);
    }

    @POST
    @Consumes({"application/xml", "application/json"})
    @Actionable
    @Path("ticket")
    public Response ticket(V3Action action) {
        return adaptAction(getDelegate()::ticket, action);
    }

    @POST
    @Consumes({"application/xml", "application/json"})
    @Actionable
    @Path("undo_snapshot")
    public Response undoSnapshot(V3Action action) {
        return adaptAction(getDelegate()::undoSnapshot, action);
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
    public V3VmGraphicsConsolesServer getGraphicsConsolesResource() {
        return new V3VmGraphicsConsolesServer(getDelegate().getGraphicsConsolesResource());
    }

    @Path("katelloerrata")
    public V3KatelloErrataServer getKatelloErrataResource() {
        return new V3KatelloErrataServer(getDelegate().getKatelloErrataResource());
    }

    @Path("snapshots")
    public V3SnapshotsServer getSnapshotsResource() {
        return new V3SnapshotsServer(getDelegate().getSnapshotsResource());
    }

    @Path("applications")
    public V3VmApplicationsServer getApplicationsResource() {
        return new V3VmApplicationsServer(getDelegate().getApplicationsResource());
    }

    @Path("cdroms")
    public V3VmCdromsServer getCdromsResource() {
        return new V3VmCdromsServer(getDelegate().getCdromsResource());
    }

    @Path("disks")
    public V3VmDisksServer getDisksResource() {
        return new V3VmDisksServer(id, ((BackendVmResource) getDelegate()).getDisksResource());
    }

    @Path("hostdevices")
    public V3VmHostDevicesServer getHostDevicesResource() {
        return new V3VmHostDevicesServer(getDelegate().getHostDevicesResource());
    }

    @Path("nics")
    public V3VmNicsServer getNicsResource() {
        return new V3VmNicsServer(id, getDelegate().getNicsResource());
    }

    @Path("numanodes")
    public V3VmNumaNodesServer getNumaNodesResource() {
        return new V3VmNumaNodesServer(getDelegate().getNumaNodesResource());
    }

    @Path("reporteddevices")
    public V3VmReportedDevicesServer getReportedDevicesResource() {
        return new V3VmReportedDevicesServer(getDelegate().getReportedDevicesResource());
    }

    @Path("sessions")
    public V3VmSessionsServer getSessionsResource() {
        return new V3VmSessionsServer(getDelegate().getSessionsResource());
    }

    @Path("watchdogs")
    public V3VmWatchdogsServer getWatchdogsResource() {
        return new V3VmWatchdogsServer(getDelegate().getWatchdogsResource());
    }

    @Path("statistics")
    public V3StatisticsServer getStatisticsResource() {
        return new V3StatisticsServer(getDelegate().getStatisticsResource());
    }

    @Path("{action: (cancelmigration|clone|commit_snapshot|detach|export|freezefilesystems|logon|maintenance|migrate|move|preview_snapshot|reboot|reordermacaddresses|shutdown|start|stop|suspend|thawfilesystems|ticket|undo_snapshot)}/{oid}")
    public V3ActionServer getActionResource(@PathParam("action") String action, @PathParam("oid") String oid) {
        return new V3ActionServer(getDelegate().getActionResource(action, oid));
    }
}
