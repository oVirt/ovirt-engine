/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.servers;

import javax.ws.rs.GET;
import javax.ws.rs.Produces;

import org.ovirt.engine.api.resource.IconResource;
import org.ovirt.engine.api.v3.V3Server;
import org.ovirt.engine.api.v3.types.V3Icon;

@Produces({"application/xml", "application/json"})
public class V3IconServer extends V3Server<IconResource> {
    public V3IconServer(IconResource delegate) {
        super(delegate);
    }

    @GET
    public V3Icon get() {
        return adaptGet(getDelegate()::get);
    }
}
