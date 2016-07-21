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
import org.ovirt.engine.api.resource.StorageDomainResource;
import org.ovirt.engine.api.restapi.invocation.CurrentManager;
import org.ovirt.engine.api.v3.V3Server;
import org.ovirt.engine.api.v3.types.V3Action;
import org.ovirt.engine.api.v3.types.V3Host;
import org.ovirt.engine.api.v3.types.V3StorageDomain;

@Produces({"application/xml", "application/json"})
public class V3StorageDomainServer extends V3Server<StorageDomainResource> {
    public V3StorageDomainServer(StorageDomainResource delegate) {
        super(delegate);
    }

    @GET
    public V3StorageDomain get() {
        return adaptGet(getDelegate()::get);
    }

    @POST
    @Consumes({"application/xml", "application/json"})
    @Actionable
    @Path("isattached")
    public Response isAttached(V3Action action) {
        return adaptAction(getDelegate()::isAttached, action);
    }

    @PUT
    @Consumes({"application/xml", "application/json"})
    public V3StorageDomain update(V3StorageDomain storageDomain) {
        return adaptUpdate(getDelegate()::update, storageDomain);
    }

    @POST
    @Consumes({"application/xml", "application/json"})
    @Actionable
    @Path("refreshluns")
    public Response refreshLuns(V3Action action) {
        return adaptAction(getDelegate()::refreshLuns, action);
    }

    @DELETE
    @Consumes({"application/xml", "application/json"})
    public Response remove(V3StorageDomain storageDomain) {
        // In version 3 of the API the "format", "destroy" and "host" parameters where part of the representation of the
        // storage domain, but in version 4 of the API they have been replaced by parameters. This means that we need
        // to check if they are present in the body of the request and convert them into request parameters.
        if (storageDomain != null) {
            Map<String, String> parameters = CurrentManager.get().getParameters();
            if (storageDomain.isSetFormat()) {
                parameters.put("format", storageDomain.isFormat().toString());
            }
            if (storageDomain.isSetDestroy()) {
                parameters.put("destroy", storageDomain.isDestroy().toString());
            }
            V3Host host = storageDomain.getHost();
            if (host != null) {
                if (host.isSetId()) {
                    parameters.put("host", host.getId());
                }
                else if (host.isSetName()) {
                    parameters.put("host", host.getName());
                }
            }
        }
        return adaptRemove(getDelegate()::remove);
    }

    @DELETE
    public Response remove() {
        return adaptRemove(getDelegate()::remove);
    }

    @Path("diskprofiles")
    public V3AssignedDiskProfilesServer getDiskProfilesResource() {
        return new V3AssignedDiskProfilesServer(getDelegate().getDiskProfilesResource());
    }

    @Path("permissions")
    public V3AssignedPermissionsServer getPermissionsResource() {
        return new V3AssignedPermissionsServer(getDelegate().getPermissionsResource());
    }

    @Path("disksnapshots")
    public V3DiskSnapshotsServer getDiskSnapshotsResource() {
        return new V3DiskSnapshotsServer(getDelegate().getDiskSnapshotsResource());
    }

    @Path("disks")
    public V3DisksServer getDisksResource() {
        return new V3DisksServer(getDelegate().getDisksResource());
    }

    @Path("files")
    public V3FilesServer getFilesResource() {
        return new V3FilesServer(getDelegate().getFilesResource());
    }

    @Path("images")
    public V3ImagesServer getImagesResource() {
        return new V3ImagesServer(getDelegate().getImagesResource());
    }

    @Path("storageconnections")
    public V3StorageDomainServerConnectionsServer getStorageConnectionsResource() {
        return new V3StorageDomainServerConnectionsServer(getDelegate().getStorageConnectionsResource());
    }

    @Path("templates")
    public V3StorageDomainTemplatesServer getTemplatesResource() {
        return new V3StorageDomainTemplatesServer(getDelegate().getTemplatesResource());
    }

    @Path("vms")
    public V3StorageDomainVmsServer getVmsResource() {
        return new V3StorageDomainVmsServer(getDelegate().getVmsResource());
    }

    @Path("{action: (isattached|refreshluns)}/{oid}")
    public V3ActionServer getActionResource(@PathParam("action") String action, @PathParam("oid") String oid) {
        return new V3ActionServer(getDelegate().getActionResource(action, oid));
    }
}
