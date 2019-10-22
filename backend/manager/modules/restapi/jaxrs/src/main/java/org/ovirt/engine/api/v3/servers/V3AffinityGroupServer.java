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

import org.ovirt.engine.api.resource.AffinityGroupResource;
import org.ovirt.engine.api.v3.V3Server;
import org.ovirt.engine.api.v3.types.V3AffinityGroup;

@Produces({"application/xml", "application/json"})
public class V3AffinityGroupServer extends V3Server<AffinityGroupResource> {
    public V3AffinityGroupServer(AffinityGroupResource delegate) {
        super(delegate);
    }

    @GET
    public V3AffinityGroup get() {
        return adaptGet(getDelegate()::get);
    }

    @PUT
    @Consumes({"application/xml", "application/json"})
    public V3AffinityGroup update(V3AffinityGroup group) {
        return adaptUpdate(getDelegate()::update, group);
    }

    @DELETE
    public Response remove() {
        return adaptRemove(getDelegate()::remove);
    }

    @Path("vms")
    public V3AffinityGroupVmsServer getVmsResource() {
        return new V3AffinityGroupVmsServer(getDelegate().getVmsResource());
    }
}
