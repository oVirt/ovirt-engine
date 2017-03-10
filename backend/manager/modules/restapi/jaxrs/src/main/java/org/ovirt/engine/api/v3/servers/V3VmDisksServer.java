/*
Copyright (c) 2016-2017 Red Hat, Inc.

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

import static org.ovirt.engine.api.v3.adapters.V3OutAdapters.adaptOut;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Disk;
import org.ovirt.engine.api.model.DiskAttachment;
import org.ovirt.engine.api.model.DiskAttachments;
import org.ovirt.engine.api.resource.DiskAttachmentsResource;
import org.ovirt.engine.api.resource.DiskResource;
import org.ovirt.engine.api.resource.DisksResource;
import org.ovirt.engine.api.resource.SystemResource;
import org.ovirt.engine.api.resource.VmDisksResource;
import org.ovirt.engine.api.resource.VmResource;
import org.ovirt.engine.api.resource.VmsResource;
import org.ovirt.engine.api.restapi.resource.BackendApiResource;
import org.ovirt.engine.api.v3.V3Server;
import org.ovirt.engine.api.v3.helpers.V3VmHelper;
import org.ovirt.engine.api.v3.types.V3Disk;
import org.ovirt.engine.api.v3.types.V3Disks;

@Produces({"application/xml", "application/json"})
public class V3VmDisksServer extends V3Server<VmDisksResource> {
    private String vmId;

    public V3VmDisksServer(String vmId, VmDisksResource delegate) {
        super(delegate);
        this.vmId = vmId;
    }

    @POST
    @Consumes({"application/xml", "application/json"})
    public Response add(V3Disk disk) {
        Response response = adaptAdd(getDelegate()::add, disk);
        Object entity = response.getEntity();
        if (entity instanceof V3Disk) {
            disk = (V3Disk) entity;
            V3VmHelper.fixDiskLinks(vmId, disk);
            V3VmHelper.addDiskAttachmentDetails(vmId, disk);
        }
        return response;
    }

    @GET
    public V3Disks list() {
        SystemResource systemResource = BackendApiResource.getInstance();

        // In version 4 of the API the collection of disks of a virtual machine has been replaced by the collection of
        // disk attachments, so we need to fetch the disk attachments:
        VmsResource vmsResource = systemResource.getVmsResource();
        VmResource vmResource = vmsResource.getVmResource(vmId);
        DiskAttachmentsResource attachmentsResource = vmResource.getDiskAttachmentsResource();
        DiskAttachments attachments = attachmentsResource.list();

        // For each disk, we need now to fetch it from the top level disks collection, and add the information from
        // the corresponding attachment:
        DisksResource disksResource = systemResource.getDisksResource();
        V3Disks disks = new V3Disks();
        for (DiskAttachment attachment : attachments.getDiskAttachments()) {
            DiskResource diskResource = disksResource.getDiskResource(attachment.getDisk().getId());
            Disk v4Disk = diskResource.get();
            V3Disk v3Disk = adaptOut(v4Disk);
            V3VmHelper.addDiskAttachmentDetails(attachment, v3Disk);
            V3VmHelper.fixDiskLinks(vmId, v3Disk);
            disks.getDisks().add(v3Disk);
        }
        return disks;
    }

    @Path("{id}")
    public V3VmDiskServer getDiskResource(@PathParam("id") String id) {
        return new V3VmDiskServer(vmId, id, getDelegate().getDiskResource(id));
    }
}
