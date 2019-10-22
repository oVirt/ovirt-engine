/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.servers;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.ovirt.engine.api.resource.StorageDomainTemplatesResource;
import org.ovirt.engine.api.v3.V3Server;
import org.ovirt.engine.api.v3.types.V3Templates;

@Produces({"application/xml", "application/json"})
public class V3StorageDomainTemplatesServer extends V3Server<StorageDomainTemplatesResource> {
    public V3StorageDomainTemplatesServer(StorageDomainTemplatesResource delegate) {
        super(delegate);
    }

    @GET
    public V3Templates list() {
        return adaptList(getDelegate()::list);
    }

    @Path("{id}")
    public V3StorageDomainTemplateServer getTemplateResource(@PathParam("id") String id) {
        return new V3StorageDomainTemplateServer(getDelegate().getTemplateResource(id));
    }
}
