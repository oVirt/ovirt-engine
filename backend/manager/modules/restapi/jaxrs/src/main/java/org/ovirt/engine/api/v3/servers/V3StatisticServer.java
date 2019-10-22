/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.servers;

import javax.ws.rs.GET;
import javax.ws.rs.Produces;

import org.ovirt.engine.api.resource.StatisticResource;
import org.ovirt.engine.api.v3.V3Server;
import org.ovirt.engine.api.v3.types.V3Statistic;

@Produces({"application/xml", "application/json"})
public class V3StatisticServer extends V3Server<StatisticResource> {
    public V3StatisticServer(StatisticResource delegate) {
        super(delegate);
    }

    @GET
    public V3Statistic get() {
        return adaptGet(getDelegate()::get);
    }
}
