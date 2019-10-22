/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.servers;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.ovirt.engine.api.resource.externalhostproviders.ExternalDiscoveredHostsResource;
import org.ovirt.engine.api.v3.V3Server;
import org.ovirt.engine.api.v3.types.V3ExternalDiscoveredHosts;

@Produces({"application/xml", "application/json"})
public class V3ExternalDiscoveredHostsServer extends V3Server<ExternalDiscoveredHostsResource> {
    public V3ExternalDiscoveredHostsServer(ExternalDiscoveredHostsResource delegate) {
        super(delegate);
    }

    @GET
    public V3ExternalDiscoveredHosts list() {
        return adaptList(getDelegate()::list);
    }

    @Path("{id}")
    public V3ExternalDiscoveredHostServer getHostResource(@PathParam("id") String id) {
        return new V3ExternalDiscoveredHostServer(getDelegate().getHostResource(id));
    }
}
