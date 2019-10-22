/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.servers;

import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.ovirt.engine.api.resource.MeasurableResource;
import org.ovirt.engine.api.v3.V3Server;

@Produces({"application/xml", "application/json"})
public class V3MeasurableServer extends V3Server<MeasurableResource> {
    public V3MeasurableServer(MeasurableResource delegate) {
        super(delegate);
    }

    @Path("statistics")
    public V3StatisticsServer getStatisticsResource() {
        return new V3StatisticsServer(getDelegate().getStatisticsResource());
    }
}
