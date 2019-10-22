/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.servers;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.ovirt.engine.api.resource.OperatingSystemsResource;
import org.ovirt.engine.api.v3.V3Server;
import org.ovirt.engine.api.v3.types.V3OperatingSystemInfos;

@Produces({"application/xml", "application/json"})
public class V3OperatingSystemsServer extends V3Server<OperatingSystemsResource> {
    public V3OperatingSystemsServer(OperatingSystemsResource delegate) {
        super(delegate);
    }

    @GET
    public V3OperatingSystemInfos list() {
        return adaptList(getDelegate()::list);
    }

    @Path("{id}")
    public V3OperatingSystemServer getOperatingSystemResource(@PathParam("id") String id) {
        return new V3OperatingSystemServer(getDelegate().getOperatingSystemResource(id));
    }
}
