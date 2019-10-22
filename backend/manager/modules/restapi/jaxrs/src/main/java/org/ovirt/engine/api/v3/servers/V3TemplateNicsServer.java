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

import org.ovirt.engine.api.resource.TemplateNicsResource;
import org.ovirt.engine.api.v3.V3Server;
import org.ovirt.engine.api.v3.types.V3NIC;
import org.ovirt.engine.api.v3.types.V3Nics;

@Produces({"application/xml", "application/json"})
public class V3TemplateNicsServer extends V3Server<TemplateNicsResource> {
    public V3TemplateNicsServer(TemplateNicsResource delegate) {
        super(delegate);
    }

    @POST
    @Consumes({"application/xml", "application/json"})
    public Response add(V3NIC nic) {
        return adaptAdd(getDelegate()::add, nic);
    }

    @GET
    public V3Nics list() {
        return adaptList(getDelegate()::list);
    }

    @Path("{id}")
    public V3TemplateNicServer getNicResource(@PathParam("id") String id) {
        return new V3TemplateNicServer(getDelegate().getNicResource(id));
    }
}
