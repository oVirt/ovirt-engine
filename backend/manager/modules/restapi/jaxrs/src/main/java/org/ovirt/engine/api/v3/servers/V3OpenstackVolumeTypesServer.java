/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.servers;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.ovirt.engine.api.resource.openstack.OpenstackVolumeTypesResource;
import org.ovirt.engine.api.v3.V3Server;
import org.ovirt.engine.api.v3.types.V3OpenStackVolumeTypes;

@Produces({"application/xml", "application/json"})
public class V3OpenstackVolumeTypesServer extends V3Server<OpenstackVolumeTypesResource> {
    public V3OpenstackVolumeTypesServer(OpenstackVolumeTypesResource delegate) {
        super(delegate);
    }

    @GET
    public V3OpenStackVolumeTypes list() {
        return adaptList(getDelegate()::list);
    }

    @Path("{id}")
    public V3OpenstackVolumeTypeServer getTypeResource(@PathParam("id") String id) {
        return new V3OpenstackVolumeTypeServer(getDelegate().getTypeResource(id));
    }
}
