/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.servers;

import javax.ws.rs.GET;
import javax.ws.rs.Produces;

import org.ovirt.engine.api.resource.externalhostproviders.ExternalComputeResourceResource;
import org.ovirt.engine.api.v3.V3Server;
import org.ovirt.engine.api.v3.types.V3ExternalComputeResource;

@Produces({"application/xml", "application/json"})
public class V3ExternalComputeResourceServer extends V3Server<ExternalComputeResourceResource> {
    public V3ExternalComputeResourceServer(ExternalComputeResourceResource delegate) {
        super(delegate);
    }

    @GET
    public V3ExternalComputeResource get() {
        return adaptGet(getDelegate()::get);
    }
}
