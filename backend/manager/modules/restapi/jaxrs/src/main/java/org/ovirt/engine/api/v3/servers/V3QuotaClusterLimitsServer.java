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

import org.ovirt.engine.api.resource.QuotaClusterLimitsResource;
import org.ovirt.engine.api.v3.V3Server;
import org.ovirt.engine.api.v3.types.V3QuotaClusterLimit;
import org.ovirt.engine.api.v3.types.V3QuotaClusterLimits;

@Produces({"application/xml", "application/json"})
public class V3QuotaClusterLimitsServer extends V3Server<QuotaClusterLimitsResource> {
    public V3QuotaClusterLimitsServer(QuotaClusterLimitsResource delegate) {
        super(delegate);
    }

    @POST
    @Consumes({"application/xml", "application/json"})
    public Response add(V3QuotaClusterLimit limit) {
        return adaptAdd(getDelegate()::add, limit);
    }

    @GET
    public V3QuotaClusterLimits list() {
        return adaptList(getDelegate()::list);
    }

    @Path("{id}")
    public V3QuotaClusterLimitServer getLimitResource(@PathParam("id") String id) {
        return new V3QuotaClusterLimitServer(getDelegate().getLimitResource(id));
    }
}
