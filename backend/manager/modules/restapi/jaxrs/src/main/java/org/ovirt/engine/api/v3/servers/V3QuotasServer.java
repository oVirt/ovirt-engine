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

import org.ovirt.engine.api.resource.QuotasResource;
import org.ovirt.engine.api.v3.V3Server;
import org.ovirt.engine.api.v3.types.V3Quota;
import org.ovirt.engine.api.v3.types.V3Quotas;

@Produces({"application/xml", "application/json"})
public class V3QuotasServer extends V3Server<QuotasResource> {
    public V3QuotasServer(QuotasResource delegate) {
        super(delegate);
    }

    @POST
    @Consumes({"application/xml", "application/json"})
    public Response add(V3Quota quota) {
        return adaptAdd(getDelegate()::add, quota);
    }

    @GET
    public V3Quotas list() {
        return adaptList(getDelegate()::list);
    }

    @Path("{id}")
    public V3QuotaServer getQuotaResource(@PathParam("id") String id) {
        return new V3QuotaServer(getDelegate().getQuotaResource(id));
    }
}
