/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.servers;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.resource.VmCdromsResource;
import org.ovirt.engine.api.restapi.resource.BackendVmCdromsResource;
import org.ovirt.engine.api.v3.V3Server;
import org.ovirt.engine.api.v3.types.V3CdRom;
import org.ovirt.engine.api.v3.types.V3CdRoms;

@Produces({"application/xml", "application/json"})
public class V3VmCdromsServer extends V3Server<VmCdromsResource> {
    public V3VmCdromsServer(VmCdromsResource delegate) {
        super(delegate);
    }

    @POST
    @Consumes({"application/xml", "application/json"})
    public Response add(V3CdRom cdrom) {
        return adaptAdd(((BackendVmCdromsResource) getDelegate())::add, cdrom);
    }

    @GET
    public V3CdRoms list() {
        return adaptList(getDelegate()::list);
    }

    @Path("{id}")
    public V3VmCdromServer getCdromResource(@PathParam("id") String id) {
        return new V3VmCdromServer(getDelegate().getCdromResource(id));
    }
}
