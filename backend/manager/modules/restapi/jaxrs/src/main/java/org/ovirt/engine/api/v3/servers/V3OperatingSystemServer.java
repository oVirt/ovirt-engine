/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.servers;

import javax.ws.rs.GET;
import javax.ws.rs.Produces;

import org.ovirt.engine.api.resource.OperatingSystemResource;
import org.ovirt.engine.api.v3.V3Server;
import org.ovirt.engine.api.v3.types.V3OperatingSystemInfo;

@Produces({"application/xml", "application/json"})
public class V3OperatingSystemServer extends V3Server<OperatingSystemResource> {
    public V3OperatingSystemServer(OperatingSystemResource delegate) {
        super(delegate);
    }

    @GET
    public V3OperatingSystemInfo get() {
        return adaptGet(getDelegate()::get);
    }
}
