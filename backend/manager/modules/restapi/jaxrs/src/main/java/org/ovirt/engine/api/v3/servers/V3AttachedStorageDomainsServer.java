/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.servers;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.resource.AttachedStorageDomainsResource;
import org.ovirt.engine.api.v3.V3Server;
import org.ovirt.engine.api.v3.types.V3StorageDomain;
import org.ovirt.engine.api.v3.types.V3StorageDomains;

@Produces({"application/xml", "application/json"})
public class V3AttachedStorageDomainsServer extends V3Server<AttachedStorageDomainsResource> {
    public V3AttachedStorageDomainsServer(AttachedStorageDomainsResource delegate) {
        super(delegate);
    }

    @POST
    @Consumes({"application/xml", "application/json"})
    public Response add(V3StorageDomain storageDomain) {
        return adaptAdd(getDelegate()::add, storageDomain);
    }

    @GET
    public V3StorageDomains list() {
        return adaptList(getDelegate()::list);
    }

    @Path("{id}")
    public V3AttachedStorageDomainServer getStorageDomainResource(@PathParam("id") String id) {
        return new V3AttachedStorageDomainServer(getDelegate().getStorageDomainResource(id));
    }
}
