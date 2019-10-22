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

import org.ovirt.engine.api.resource.StorageDomainsResource;
import org.ovirt.engine.api.v3.V3Server;
import org.ovirt.engine.api.v3.types.V3StorageDomain;
import org.ovirt.engine.api.v3.types.V3StorageDomains;

@Produces({"application/xml", "application/json"})
public class V3StorageDomainsServer extends V3Server<StorageDomainsResource> {
    public V3StorageDomainsServer(StorageDomainsResource delegate) {
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
    public V3StorageDomainServer getStorageDomainResource(@PathParam("id") String id) {
        return new V3StorageDomainServer(getDelegate().getStorageDomainResource(id));
    }
}
