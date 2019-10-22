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

import org.ovirt.engine.api.resource.BalancesResource;
import org.ovirt.engine.api.v3.V3Server;
import org.ovirt.engine.api.v3.types.V3Balance;
import org.ovirt.engine.api.v3.types.V3Balances;

@Produces({"application/xml", "application/json"})
public class V3BalancesServer extends V3Server<BalancesResource> {
    public V3BalancesServer(BalancesResource delegate) {
        super(delegate);
    }

    @POST
    @Consumes({"application/xml", "application/json"})
    public Response add(V3Balance balance) {
        return adaptAdd(getDelegate()::add, balance);
    }

    @GET
    public V3Balances list() {
        return adaptList(getDelegate()::list);
    }

    @Path("{id}")
    public V3BalanceServer getBalanceResource(@PathParam("id") String id) {
        return new V3BalanceServer(getDelegate().getBalanceResource(id));
    }
}
