/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.servers;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.ovirt.engine.api.resource.TemplateCdromsResource;
import org.ovirt.engine.api.v3.V3Server;
import org.ovirt.engine.api.v3.types.V3CdRoms;

@Produces({"application/xml", "application/json"})
public class V3TemplateCdromsServer extends V3Server<TemplateCdromsResource> {
    public V3TemplateCdromsServer(TemplateCdromsResource delegate) {
        super(delegate);
    }

    @GET
    public V3CdRoms list() {
        return adaptList(getDelegate()::list);
    }

    @Path("{id}")
    public V3TemplateCdromServer getCdromResource(@PathParam("id") String id) {
        return new V3TemplateCdromServer(getDelegate().getCdromResource(id));
    }
}
