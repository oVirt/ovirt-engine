/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.servers;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.ovirt.engine.api.resource.externalhostproviders.ExternalHostsResource;
import org.ovirt.engine.api.v3.V3Server;
import org.ovirt.engine.api.v3.types.V3ExternalHosts;

@Produces({"application/xml", "application/json"})
public class V3ExternalHostsServer extends V3Server<ExternalHostsResource> {
    public V3ExternalHostsServer(ExternalHostsResource delegate) {
        super(delegate);
    }

    @GET
    public V3ExternalHosts list() {
        return adaptList(getDelegate()::list);
    }

    @Path("{id}")
    public V3ExternalHostServer getHostResource(@PathParam("id") String id) {
        return new V3ExternalHostServer(getDelegate().getHostResource(id));
    }
}
