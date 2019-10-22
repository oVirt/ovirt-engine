/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.servers;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.ovirt.engine.api.resource.externalhostproviders.ExternalHostGroupsResource;
import org.ovirt.engine.api.v3.V3Server;
import org.ovirt.engine.api.v3.types.V3ExternalHostGroups;

@Produces({"application/xml", "application/json"})
public class V3ExternalHostGroupsServer extends V3Server<ExternalHostGroupsResource> {
    public V3ExternalHostGroupsServer(ExternalHostGroupsResource delegate) {
        super(delegate);
    }

    @GET
    public V3ExternalHostGroups list() {
        return adaptList(getDelegate()::list);
    }

    @Path("{id}")
    public V3ExternalHostGroupServer getGroupResource(@PathParam("id") String id) {
        return new V3ExternalHostGroupServer(getDelegate().getGroupResource(id));
    }
}
