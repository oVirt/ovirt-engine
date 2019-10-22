/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.servers;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.ovirt.engine.api.resource.HostNumaNodeResource;
import org.ovirt.engine.api.v3.V3Server;
import org.ovirt.engine.api.v3.types.V3NumaNode;

@Produces({"application/xml", "application/json"})
public class V3HostNumaNodeServer extends V3Server<HostNumaNodeResource> {
    public V3HostNumaNodeServer(HostNumaNodeResource delegate) {
        super(delegate);
    }

    @GET
    public V3NumaNode get() {
        return adaptGet(getDelegate()::get);
    }

    @Path("statistics")
    public V3StatisticsServer getStatisticsResource() {
        return new V3StatisticsServer(getDelegate().getStatisticsResource());
    }
}
