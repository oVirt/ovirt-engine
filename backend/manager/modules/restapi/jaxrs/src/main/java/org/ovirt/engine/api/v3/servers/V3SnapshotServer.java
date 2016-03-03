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
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Actionable;
import org.ovirt.engine.api.resource.SnapshotResource;
import org.ovirt.engine.api.v3.V3Server;
import org.ovirt.engine.api.v3.types.V3Action;
import org.ovirt.engine.api.v3.types.V3Snapshot;

@Produces({"application/xml", "application/json"})
public class V3SnapshotServer extends V3Server<SnapshotResource> {
    public V3SnapshotServer(SnapshotResource delegate) {
        super(delegate);
    }

    @GET
    public V3Snapshot get() {
        return adaptGet(getDelegate()::get);
    }

    @DELETE
    public Response remove() {
        return adaptRemove(getDelegate()::remove);
    }

    @POST
    @Consumes({"application/xml", "application/json"})
    @Actionable
    @Path("restore")
    public Response restore(V3Action action) {
        return adaptAction(getDelegate()::restore, action);
    }

    @Path("cdroms")
    public V3SnapshotCdromsServer getCdromsResource() {
        return new V3SnapshotCdromsServer(getDelegate().getCdromsResource());
    }

    @Path("disks")
    public V3SnapshotDisksServer getDisksResource() {
        return new V3SnapshotDisksServer(getDelegate().getDisksResource());
    }

    @Path("nics")
    public V3SnapshotNicsServer getNicsResource() {
        return new V3SnapshotNicsServer(getDelegate().getNicsResource());
    }

    @Path("{action: (restore)}/{oid}")
    public V3ActionServer getActionResource(@PathParam("action") String action, @PathParam("oid") String oid) {
        return new V3ActionServer(getDelegate().getActionResource(action, oid));
    }
}
