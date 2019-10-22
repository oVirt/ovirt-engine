/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.servers;

import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Actionable;
import org.ovirt.engine.api.resource.StorageDomainVmResource;
import org.ovirt.engine.api.restapi.invocation.CurrentManager;
import org.ovirt.engine.api.v3.V3Server;
import org.ovirt.engine.api.v3.types.V3Action;
import org.ovirt.engine.api.v3.types.V3Snapshots;
import org.ovirt.engine.api.v3.types.V3VM;

@Produces({"application/xml", "application/json"})
public class V3StorageDomainVmServer extends V3Server<StorageDomainVmResource> {
    public V3StorageDomainVmServer(StorageDomainVmResource delegate) {
        super(delegate);
    }

    @GET
    public V3VM get() {
        return adaptGet(getDelegate()::get);
    }

    @POST
    @Consumes({"application/xml", "application/json"})
    @Actionable
    @Path("import")
    public Response doImport(V3Action action) {
        // V3 supports using the "action.vm.snapshots.collapse_snapshots" element to indicate if the snapshots have
        // to be collapsed during import, but in V4 this has been replaced by a "collapse_snapshots" boolean parameter:
        if (action.isSetVm() && action.getVm().isSetSnapshots()) {
            V3Snapshots snapshots = action.getVm().getSnapshots();
            if (snapshots.isSetCollapseSnapshots() && snapshots.isCollapseSnapshots()) {
                Map<String, String> parameters = CurrentManager.get().getParameters();
                parameters.put("collapse_snapshots", String.valueOf(true));
            }
        }
        return adaptAction(getDelegate()::doImport, action);
    }

    @POST
    @Consumes({"application/xml", "application/json"})
    @Actionable
    @Path("register")
    public Response register(V3Action action) {
        return adaptAction(getDelegate()::register, action);
    }

    @DELETE
    public Response remove() {
        return adaptRemove(getDelegate()::remove);
    }

    @Path("disks")
    public V3StorageDomainContentDisksServer getDisksResource() {
        return new V3StorageDomainContentDisksServer(getDelegate().getDisksResource());
    }

    @Path("{action: (?:import|register)}/{oid}")
    public V3ActionServer getActionResource(@PathParam("action") String action, @PathParam("oid") String oid) {
        return new V3ActionServer(getDelegate().getActionResource(action, oid));
    }
}
