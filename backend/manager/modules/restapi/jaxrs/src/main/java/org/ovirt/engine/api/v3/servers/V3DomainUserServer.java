/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.servers;

import javax.ws.rs.GET;
import javax.ws.rs.Produces;

import org.ovirt.engine.api.resource.aaa.DomainUserResource;
import org.ovirt.engine.api.v3.V3Server;
import org.ovirt.engine.api.v3.types.V3User;

@Produces({"application/xml", "application/json"})
public class V3DomainUserServer extends V3Server<DomainUserResource> {
    public V3DomainUserServer(DomainUserResource delegate) {
        super(delegate);
    }

    @GET
    public V3User get() {
        return adaptGet(getDelegate()::get);
    }
}
