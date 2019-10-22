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

import org.ovirt.engine.api.resource.FiltersResource;
import org.ovirt.engine.api.v3.V3Server;
import org.ovirt.engine.api.v3.types.V3Filter;
import org.ovirt.engine.api.v3.types.V3Filters;

@Produces({"application/xml", "application/json"})
public class V3FiltersServer extends V3Server<FiltersResource> {
    public V3FiltersServer(FiltersResource delegate) {
        super(delegate);
    }

    @POST
    @Consumes({"application/xml", "application/json"})
    public Response add(V3Filter filter) {
        return adaptAdd(getDelegate()::add, filter);
    }

    @GET
    public V3Filters list() {
        return adaptList(getDelegate()::list);
    }

    @Path("{id}")
    public V3FilterServer getFilterResource(@PathParam("id") String id) {
        return new V3FilterServer(getDelegate().getFilterResource(id));
    }
}
