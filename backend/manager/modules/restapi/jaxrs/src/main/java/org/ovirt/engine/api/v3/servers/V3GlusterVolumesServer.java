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

import org.ovirt.engine.api.resource.gluster.GlusterVolumesResource;
import org.ovirt.engine.api.v3.V3Server;
import org.ovirt.engine.api.v3.types.V3GlusterVolume;
import org.ovirt.engine.api.v3.types.V3GlusterVolumes;

@Produces({"application/xml", "application/json"})
public class V3GlusterVolumesServer extends V3Server<GlusterVolumesResource> {
    public V3GlusterVolumesServer(GlusterVolumesResource delegate) {
        super(delegate);
    }

    @POST
    @Consumes({"application/xml", "application/json"})
    public Response add(V3GlusterVolume volume) {
        return adaptAdd(getDelegate()::add, volume);
    }

    @GET
    public V3GlusterVolumes list() {
        return adaptList(getDelegate()::list);
    }

    @Path("{id}")
    public V3GlusterVolumeServer getVolumeResource(@PathParam("id") String id) {
        return new V3GlusterVolumeServer(getDelegate().getVolumeResource(id));
    }
}
