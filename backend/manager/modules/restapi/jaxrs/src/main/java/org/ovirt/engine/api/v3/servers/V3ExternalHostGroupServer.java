/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.servers;

import javax.ws.rs.GET;
import javax.ws.rs.Produces;

import org.ovirt.engine.api.resource.externalhostproviders.ExternalHostGroupResource;
import org.ovirt.engine.api.v3.V3Server;
import org.ovirt.engine.api.v3.types.V3ExternalHostGroup;

@Produces({"application/xml", "application/json"})
public class V3ExternalHostGroupServer extends V3Server<ExternalHostGroupResource> {
    public V3ExternalHostGroupServer(ExternalHostGroupResource delegate) {
        super(delegate);
    }

    @GET
    public V3ExternalHostGroup get() {
        return adaptGet(getDelegate()::get);
    }
}
