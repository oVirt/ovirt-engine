/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.servers;

import javax.ws.rs.GET;
import javax.ws.rs.Produces;

import org.ovirt.engine.api.resource.externalhostproviders.KatelloErratumResource;
import org.ovirt.engine.api.v3.V3Server;
import org.ovirt.engine.api.v3.types.V3KatelloErratum;

@Produces({"application/xml", "application/json"})
public class V3KatelloErratumServer extends V3Server<KatelloErratumResource> {
    public V3KatelloErratumServer(KatelloErratumResource delegate) {
        super(delegate);
    }

    @GET
    public V3KatelloErratum get() {
        return adaptGet(getDelegate()::get);
    }
}
