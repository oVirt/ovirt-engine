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
import org.ovirt.engine.api.model.StorageDomain;
import org.ovirt.engine.api.model.Template;
import org.ovirt.engine.api.model.Templates;
import org.ovirt.engine.api.model.VM;
import org.ovirt.engine.api.model.VMs;

@Produces({ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON, ApiMediaType.APPLICATION_X_YAML})
public interface StorageDomainResource extends UpdatableResource<StorageDomain> {

    @Path("{action: (isattached)}/{oid}")
    public ActionResource getActionSubresource(@PathParam("action")String action, @PathParam("oid")String oid);

    @Path("permissions")
    public AssignedPermissionsResource getPermissionsResource();

    @Path("vms")
    public RemovableStorageDomainContentsResource<VMs, VM> getStorageDomainVmsResource();

    @Path("templates")
    public RemovableStorageDomainContentsResource<Templates, Template> getStorageDomainTemplatesResource();

    @Path("files")
    public FilesResource getFilesResource();

    @Path("disks")
    public DisksResource getDisksResource();

    @Path("storageconnections")
    public StorageDomainServerConnectionsResource getStorageConnectionsResource();

    @Path("images")
    public ImagesResource getImagesResource();

    @Path("disksnapshots")
    public DiskSnapshotsResource getDiskSnapshotsResource();

    @Path("diskprofiles")
    public AssignedDiskProfilesResource getDiskProfilesResource();

    @POST
    @Consumes({ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON, ApiMediaType.APPLICATION_X_YAML})
    @Actionable
    @Path("isattached")
    public Response getIsAttached(Action action);
}
