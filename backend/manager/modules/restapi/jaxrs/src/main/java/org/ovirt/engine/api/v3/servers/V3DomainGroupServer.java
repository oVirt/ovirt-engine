/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.servers;

import javax.ws.rs.GET;
import javax.ws.rs.Produces;

import org.ovirt.engine.api.resource.aaa.DomainGroupResource;
import org.ovirt.engine.api.v3.V3Server;
import org.ovirt.engine.api.v3.types.V3Group;

@Produces({"application/xml", "application/json"})
public class V3DomainGroupServer extends V3Server<DomainGroupResource> {
    public V3DomainGroupServer(DomainGroupResource delegate) {
        super(delegate);
    }

    @GET
    public V3Group get() {
        return adaptGet(getDelegate()::get);
    }
}
