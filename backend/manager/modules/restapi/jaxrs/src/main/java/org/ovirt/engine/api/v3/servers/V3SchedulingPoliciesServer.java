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

import org.ovirt.engine.api.resource.SchedulingPoliciesResource;
import org.ovirt.engine.api.v3.V3Server;
import org.ovirt.engine.api.v3.types.V3SchedulingPolicies;
import org.ovirt.engine.api.v3.types.V3SchedulingPolicy;

@Produces({"application/xml", "application/json"})
public class V3SchedulingPoliciesServer extends V3Server<SchedulingPoliciesResource> {
    public V3SchedulingPoliciesServer(SchedulingPoliciesResource delegate) {
        super(delegate);
    }

    @POST
    @Consumes({"application/xml", "application/json"})
    public Response add(V3SchedulingPolicy policy) {
        return adaptAdd(getDelegate()::add, policy);
    }

    @GET
    public V3SchedulingPolicies list() {
        return adaptList(getDelegate()::list);
    }

    @Path("{id}")
    public V3SchedulingPolicyServer getPolicyResource(@PathParam("id") String id) {
        return new V3SchedulingPolicyServer(getDelegate().getPolicyResource(id));
    }
}
