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
import org.ovirt.engine.api.resource.DiskResource;
import org.ovirt.engine.api.resource.DisksResource;
import org.ovirt.engine.api.resource.VmDiskResource;
import org.ovirt.engine.api.restapi.resource.BackendApiResource;
import org.ovirt.engine.api.v3.V3Server;
import org.ovirt.engine.api.v3.helpers.V3VmHelper;
import org.ovirt.engine.api.v3.types.V3Action;
import org.ovirt.engine.api.v3.types.V3Disk;

@Produces({"application/xml", "application/json"})
public class V3VmDiskServer extends V3Server<VmDiskResource> {
    private String vmId;
    private String diskId;

    public V3VmDiskServer(String vmId, String diskId, VmDiskResource delegate) {
        super(delegate);
        this.vmId = vmId;
        this.diskId = diskId;
    }

    @POST
    @Consumes({"application/xml", "application/json"})
    @Actionable
    @Path("activate")
    public Response activate(V3Action action) {
        return adaptAction(getDelegate()::activate, action);
    }

    @POST
    @Consumes({"application/xml", "application/json"})
    @Actionable
    @Path("deactivate")
    public Response deactivate(V3Action action) {
        return adaptAction(getDelegate()::deactivate, action);
    }

    @POST
    @Consumes({"application/xml", "application/json"})
    @Actionable
    @Path("export")
    public Response export(V3Action action) {
        return adaptAction(getDelegate()::export, action);
    }

    @GET
    public V3Disk get() {
        V3Disk disk = adaptGet(getDelegate()::get);
        V3VmHelper.fixDiskLinks(vmId, disk);
        return disk;
    }

    @POST
    @Consumes({"application/xml", "application/json"})
    @Actionable
    @Path("move")
    public Response move(V3Action action) {
        return adaptAction(getDelegate()::move, action);
    }

    @PUT
    @Consumes({"application/xml", "application/json"})
    public V3Disk update(V3Disk disk) {
        disk = adaptUpdate(getDelegate()::update, disk);
        V3VmHelper.fixDiskLinks(vmId, disk);
        return disk;
    }

    @DELETE
    public Response remove() {
        return remove(new V3Action());
    }

    @DELETE
    @Consumes({"application/xml", "application/json"})
    public Response remove(V3Action action) {
        // Detach the disk from the VM:
        Response response = adaptRemove(getDelegate()::remove);

        // V3 version of the API used the action "detach" element as parameter, to indicate that the disk should only
        // be detached and not removed completely. In V4 this has been removed, so if the value isn't "true" then we
        // need to delete the disk using the top level disks collection.
        if (!action.isSetDetach() || !action.isDetach()) {
            DisksResource disksResource = BackendApiResource.getInstance().getDisksResource();
            DiskResource diskResource = disksResource.getDiskResource(diskId);
            response = adaptRemove(diskResource::remove);
        }

        // Return the response:
        return response;
    }

    @Path("permissions")
    public V3AssignedPermissionsServer getPermissionsResource() {
        return new V3AssignedPermissionsServer(getDelegate().getPermissionsResource());
    }

    @Path("statistics")
    public V3StatisticsServer getStatisticsResource() {
        return new V3StatisticsServer(getDelegate().getStatisticsResource());
    }

    @Path("{action: (activate|deactivate|export|move)}/{oid}")
    public V3ActionServer getActionResource(@PathParam("action") String action, @PathParam("oid") String oid) {
        return new V3ActionServer(getDelegate().getActionResource(action, oid));
    }
}
