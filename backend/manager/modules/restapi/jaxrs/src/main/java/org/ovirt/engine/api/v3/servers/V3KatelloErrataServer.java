/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.servers;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.ovirt.engine.api.resource.externalhostproviders.KatelloErrataResource;
import org.ovirt.engine.api.v3.V3Server;
import org.ovirt.engine.api.v3.types.V3KatelloErrata;

@Produces({"application/xml", "application/json"})
public class V3KatelloErrataServer extends V3Server<KatelloErrataResource> {
    public V3KatelloErrataServer(KatelloErrataResource delegate) {
        super(delegate);
    }

    @GET
    public V3KatelloErrata list() {
        return adaptList(getDelegate()::list);
    }

    @Path("{id}")
    public V3KatelloErratumServer getKatelloErratumResource(@PathParam("id") String id) {
        return new V3KatelloErratumServer(getDelegate().getKatelloErratumResource(id));
    }
}
