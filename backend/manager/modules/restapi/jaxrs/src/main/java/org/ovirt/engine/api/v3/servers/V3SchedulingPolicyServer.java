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

import org.ovirt.engine.api.resource.SchedulingPolicyResource;
import org.ovirt.engine.api.v3.V3Server;
import org.ovirt.engine.api.v3.types.V3SchedulingPolicy;

@Produces({"application/xml", "application/json"})
public class V3SchedulingPolicyServer extends V3Server<SchedulingPolicyResource> {
    public V3SchedulingPolicyServer(SchedulingPolicyResource delegate) {
        super(delegate);
    }

    @GET
    public V3SchedulingPolicy get() {
        return adaptGet(getDelegate()::get);
    }

    @PUT
    @Consumes({"application/xml", "application/json"})
    public V3SchedulingPolicy update(V3SchedulingPolicy policy) {
        return adaptUpdate(getDelegate()::update, policy);
    }

    @DELETE
    public Response remove() {
        return adaptRemove(getDelegate()::remove);
    }

    @Path("filters")
    public V3FiltersServer getFiltersResource() {
        return new V3FiltersServer(getDelegate().getFiltersResource());
    }

    @Path("weights")
    public V3WeightsServer getWeightsResource() {
        return new V3WeightsServer(getDelegate().getWeightsResource());
    }

    @Path("balances")
    public V3BalancesServer getBalancesResource() {
        return new V3BalancesServer(getDelegate().getBalancesResource());
    }
}
