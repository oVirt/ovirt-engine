/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.servers;

import javax.ws.rs.GET;
import javax.ws.rs.Produces;

import org.ovirt.engine.api.resource.CreationResource;
import org.ovirt.engine.api.v3.V3Server;
import org.ovirt.engine.api.v3.types.V3Creation;

@Produces({"application/xml", "application/json"})
public class V3CreationServer extends V3Server<CreationResource> {
    public V3CreationServer(CreationResource delegate) {
        super(delegate);
    }

    @GET
    public V3Creation get() {
        return adaptGet(getDelegate()::get);
    }
}
