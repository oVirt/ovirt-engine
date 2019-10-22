/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.servers;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.ovirt.engine.api.resource.HostNicsResource;
import org.ovirt.engine.api.v3.V3Server;
import org.ovirt.engine.api.v3.types.V3HostNics;

@Produces({"application/xml", "application/json"})
public class V3HostNicsServer extends V3Server<HostNicsResource> {
    public V3HostNicsServer(HostNicsResource delegate) {
        super(delegate);
    }

    @GET
    public V3HostNics list() {
        return adaptList(getDelegate()::list);
    }

    @Path("{id}")
    public V3HostNicServer getNicResource(@PathParam("id") String id) {
        return new V3HostNicServer(getDelegate().getNicResource(id));
    }
}
