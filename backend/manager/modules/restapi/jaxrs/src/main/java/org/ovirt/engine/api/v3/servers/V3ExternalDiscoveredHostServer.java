/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.servers;

import javax.ws.rs.GET;
import javax.ws.rs.Produces;

import org.ovirt.engine.api.resource.externalhostproviders.ExternalDiscoveredHostResource;
import org.ovirt.engine.api.v3.V3Server;
import org.ovirt.engine.api.v3.types.V3ExternalDiscoveredHost;

@Produces({"application/xml", "application/json"})
public class V3ExternalDiscoveredHostServer extends V3Server<ExternalDiscoveredHostResource> {
    public V3ExternalDiscoveredHostServer(ExternalDiscoveredHostResource delegate) {
        super(delegate);
    }

    @GET
    public V3ExternalDiscoveredHost get() {
        return adaptGet(getDelegate()::get);
    }
}
