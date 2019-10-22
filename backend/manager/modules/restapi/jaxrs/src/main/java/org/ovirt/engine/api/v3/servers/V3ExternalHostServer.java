/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.servers;

import javax.ws.rs.GET;
import javax.ws.rs.Produces;

import org.ovirt.engine.api.resource.externalhostproviders.ExternalHostResource;
import org.ovirt.engine.api.v3.V3Server;
import org.ovirt.engine.api.v3.types.V3ExternalHost;

@Produces({"application/xml", "application/json"})
public class V3ExternalHostServer extends V3Server<ExternalHostResource> {
    public V3ExternalHostServer(ExternalHostResource delegate) {
        super(delegate);
    }

    @GET
    public V3ExternalHost get() {
        return adaptGet(getDelegate()::get);
    }
}
