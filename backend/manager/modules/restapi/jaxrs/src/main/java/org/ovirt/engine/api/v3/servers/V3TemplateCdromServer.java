/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.servers;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.ovirt.engine.api.resource.TemplateCdromResource;
import org.ovirt.engine.api.v3.V3Server;
import org.ovirt.engine.api.v3.types.V3CdRom;

@Produces({"application/xml", "application/json"})
public class V3TemplateCdromServer extends V3Server<TemplateCdromResource> {
    public V3TemplateCdromServer(TemplateCdromResource delegate) {
        super(delegate);
    }

    @GET
    public V3CdRom get() {
        return adaptGet(getDelegate()::get);
    }

    @Path("creation_status/{oid}")
    public V3CreationServer getCreationResource(@PathParam("oid") String oid) {
        return new V3CreationServer(getDelegate().getCreationResource(oid));
    }
}
