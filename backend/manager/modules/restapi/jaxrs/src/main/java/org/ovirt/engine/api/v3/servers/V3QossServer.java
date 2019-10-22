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

import org.ovirt.engine.api.resource.QossResource;
import org.ovirt.engine.api.v3.V3Server;
import org.ovirt.engine.api.v3.types.V3QoS;
import org.ovirt.engine.api.v3.types.V3QoSs;

@Produces({"application/xml", "application/json"})
public class V3QossServer extends V3Server<QossResource> {
    public V3QossServer(QossResource delegate) {
        super(delegate);
    }

    @POST
    @Consumes({"application/xml", "application/json"})
    public Response add(V3QoS qos) {
        return adaptAdd(getDelegate()::add, qos);
    }

    @GET
    public V3QoSs list() {
        return adaptList(getDelegate()::list);
    }

    @Path("{id}")
    public V3QosServer getQosResource(@PathParam("id") String id) {
        return new V3QosServer(getDelegate().getQosResource(id));
    }
}
