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

import org.ovirt.engine.api.resource.IscsiBondsResource;
import org.ovirt.engine.api.v3.V3Server;
import org.ovirt.engine.api.v3.types.V3IscsiBond;
import org.ovirt.engine.api.v3.types.V3IscsiBonds;

@Produces({"application/xml", "application/json"})
public class V3IscsiBondsServer extends V3Server<IscsiBondsResource> {
    public V3IscsiBondsServer(IscsiBondsResource delegate) {
        super(delegate);
    }

    @POST
    @Consumes({"application/xml", "application/json"})
    public Response add(V3IscsiBond bond) {
        return adaptAdd(getDelegate()::add, bond);
    }

    @GET
    public V3IscsiBonds list() {
        return adaptList(getDelegate()::list);
    }

    @Path("{id}")
    public V3IscsiBondServer getIscsiBondResource(@PathParam("id") String id) {
        return new V3IscsiBondServer(getDelegate().getIscsiBondResource(id));
    }
}
