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

import org.ovirt.engine.api.resource.QuotaStorageLimitsResource;
import org.ovirt.engine.api.v3.V3Server;
import org.ovirt.engine.api.v3.types.V3QuotaStorageLimit;
import org.ovirt.engine.api.v3.types.V3QuotaStorageLimits;

@Produces({"application/xml", "application/json"})
public class V3QuotaStorageLimitsServer extends V3Server<QuotaStorageLimitsResource> {
    public V3QuotaStorageLimitsServer(QuotaStorageLimitsResource delegate) {
        super(delegate);
    }

    @POST
    @Consumes({"application/xml", "application/json"})
    public Response add(V3QuotaStorageLimit limit) {
        return adaptAdd(getDelegate()::add, limit);
    }

    @GET
    public V3QuotaStorageLimits list() {
        return adaptList(getDelegate()::list);
    }

    @Path("{id}")
    public V3QuotaStorageLimitServer getLimitResource(@PathParam("id") String id) {
        return new V3QuotaStorageLimitServer(getDelegate().getLimitResource(id));
    }
}
