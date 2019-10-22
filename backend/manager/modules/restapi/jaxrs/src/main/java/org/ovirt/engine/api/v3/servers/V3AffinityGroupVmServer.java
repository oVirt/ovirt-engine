/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.servers;

import javax.ws.rs.DELETE;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.resource.AffinityGroupVmResource;
import org.ovirt.engine.api.v3.V3Server;

@Produces({"application/xml", "application/json"})
public class V3AffinityGroupVmServer extends V3Server<AffinityGroupVmResource> {
    public V3AffinityGroupVmServer(AffinityGroupVmResource delegate) {
        super(delegate);
    }

    @DELETE
    public Response remove() {
        return adaptRemove(getDelegate()::remove);
    }
}
