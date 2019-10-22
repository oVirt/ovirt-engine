/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.servers;

import javax.ws.rs.GET;
import javax.ws.rs.Produces;

import org.ovirt.engine.api.resource.openstack.OpenstackVolumeTypeResource;
import org.ovirt.engine.api.v3.V3Server;
import org.ovirt.engine.api.v3.types.V3OpenStackVolumeType;

@Produces({"application/xml", "application/json"})
public class V3OpenstackVolumeTypeServer extends V3Server<OpenstackVolumeTypeResource> {
    public V3OpenstackVolumeTypeServer(OpenstackVolumeTypeResource delegate) {
        super(delegate);
    }

    @GET
    public V3OpenStackVolumeType get() {
        return adaptGet(getDelegate()::get);
    }
}
