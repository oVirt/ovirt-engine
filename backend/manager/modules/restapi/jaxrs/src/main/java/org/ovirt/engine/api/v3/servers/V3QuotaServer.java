/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.servers;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.resource.QuotaResource;
import org.ovirt.engine.api.v3.V3Server;
import org.ovirt.engine.api.v3.types.V3Quota;

@Produces({"application/xml", "application/json"})
public class V3QuotaServer extends V3Server<QuotaResource> {
    public V3QuotaServer(QuotaResource delegate) {
        super(delegate);
    }

    @GET
    public V3Quota get() {
        return adaptGet(getDelegate()::get);
    }

    @PUT
    @Consumes({"application/xml", "application/json"})
    public V3Quota update(V3Quota quota) {
        return adaptUpdate(getDelegate()::update, quota);
    }

    @DELETE
    public Response remove() {
        return adaptRemove(getDelegate()::remove);
    }

    @Path("quotastoragelimits")
    public V3QuotaStorageLimitsServer getQuotaStorageLimitsResource() {
        return new V3QuotaStorageLimitsServer(getDelegate().getQuotaStorageLimitsResource());
    }

    @Path("quotaclusterlimits")
    public V3QuotaClusterLimitsServer getQuotaClusterLimitsResource() {
        return new V3QuotaClusterLimitsServer(getDelegate().getQuotaClusterLimitsResource());
    }

    @Path("permissions")
    public V3AssignedPermissionsServer getPermissionsResource() {
        return new V3AssignedPermissionsServer(getDelegate().getPermissionsResource());
    }
}
