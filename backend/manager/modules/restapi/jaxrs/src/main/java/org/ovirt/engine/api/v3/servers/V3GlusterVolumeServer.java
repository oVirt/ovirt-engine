/*
Copyright (c) 2016 Red Hat, Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package org.ovirt.engine.api.v3.servers;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Actionable;
import org.ovirt.engine.api.resource.gluster.GlusterVolumeResource;
import org.ovirt.engine.api.v3.V3Server;
import org.ovirt.engine.api.v3.types.V3Action;
import org.ovirt.engine.api.v3.types.V3GlusterVolume;

@Produces({"application/xml", "application/json"})
public class V3GlusterVolumeServer extends V3Server<GlusterVolumeResource> {
    public V3GlusterVolumeServer(GlusterVolumeResource delegate) {
        super(delegate);
    }

    @GET
    public V3GlusterVolume get() {
        return adaptGet(delegate::get);
    }

    @POST
    @Consumes({"application/xml", "application/json"})
    @Actionable
    @Path("getprofilestatistics")
    public Response getProfileStatistics(V3Action action) {
        return adaptAction(delegate::getProfileStatistics, action);
    }

    @POST
    @Consumes({"application/xml", "application/json"})
    @Actionable
    @Path("rebalance")
    public Response rebalance(V3Action action) {
        return adaptAction(delegate::rebalance, action);
    }

    @DELETE
    public Response remove() {
        return adaptRemove(delegate::remove);
    }

    @POST
    @Consumes({"application/xml", "application/json"})
    @Actionable
    @Path("resetalloptions")
    public Response resetAllOptions(V3Action action) {
        return adaptAction(delegate::resetAllOptions, action);
    }

    @POST
    @Consumes({"application/xml", "application/json"})
    @Actionable
    @Path("resetoption")
    public Response resetOption(V3Action action) {
        return adaptAction(delegate::resetOption, action);
    }

    @POST
    @Consumes({"application/xml", "application/json"})
    @Actionable
    @Path("setoption")
    public Response setOption(V3Action action) {
        return adaptAction(delegate::setOption, action);
    }

    @POST
    @Consumes({"application/xml", "application/json"})
    @Actionable
    @Path("start")
    public Response start(V3Action action) {
        return adaptAction(delegate::start, action);
    }

    @POST
    @Consumes({"application/xml", "application/json"})
    @Actionable
    @Path("startprofile")
    public Response startProfile(V3Action action) {
        return adaptAction(delegate::startProfile, action);
    }

    @POST
    @Consumes({"application/xml", "application/json"})
    @Actionable
    @Path("stop")
    public Response stop(V3Action action) {
        return adaptAction(delegate::stop, action);
    }

    @POST
    @Consumes({"application/xml", "application/json"})
    @Actionable
    @Path("stopprofile")
    public Response stopProfile(V3Action action) {
        return adaptAction(delegate::stopProfile, action);
    }

    @POST
    @Consumes({"application/xml", "application/json"})
    @Actionable
    @Path("stoprebalance")
    public Response stopRebalance(V3Action action) {
        return adaptAction(delegate::stopRebalance, action);
    }

    @Path("glusterbricks")
    public V3GlusterBricksServer getGlusterBricksResource() {
        return new V3GlusterBricksServer(delegate.getGlusterBricksResource());
    }

    @Path("{action: (getprofilestatistics|rebalance|resetalloptions|resetoption|setoption|start|startprofile|stop|stopprofile|stoprebalance)}/{oid}")
    public V3ActionServer getActionResource(@PathParam("action") String action, @PathParam("oid") String oid) {
        return new V3ActionServer(delegate.getActionResource(action, oid));
    }
}
