/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.servers;

import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.resource.ActionResource;
import org.ovirt.engine.api.v3.V3Server;

@Produces({"application/xml", "application/json"})
public class V3ActionServer extends V3Server<ActionResource> {
    public V3ActionServer(ActionResource delegate) {
        super(delegate);
    }

    @GET
    public Response get() {
        return adaptResponse(getDelegate()::get);
    }
}
