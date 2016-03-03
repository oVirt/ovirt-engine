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

import static org.ovirt.engine.api.v3.adapters.V3InAdapters.adaptIn;
import static org.ovirt.engine.api.v3.adapters.V3OutAdapters.adaptOut;
import static org.ovirt.engine.api.v3.helpers.V3ClusterHelper.assignCompatiblePolicy;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Actionable;
import org.ovirt.engine.api.model.Cluster;
import org.ovirt.engine.api.resource.ClusterResource;
import org.ovirt.engine.api.v3.V3Server;
import org.ovirt.engine.api.v3.types.V3Action;
import org.ovirt.engine.api.v3.types.V3Cluster;

@Produces({"application/xml", "application/json"})
public class V3ClusterServer extends V3Server<ClusterResource> {
    public V3ClusterServer(ClusterResource delegate) {
        super(delegate);
    }

    @GET
    public V3Cluster get() {
        return adaptGet(getDelegate()::get);
    }

    @PUT
    @Consumes({"application/xml", "application/json"})
    public V3Cluster update(V3Cluster v3Cluster) {
        Cluster v4Cluster = adaptIn(v3Cluster);
        assignCompatiblePolicy(v3Cluster, v4Cluster);
        try {
            return adaptOut(getDelegate().update(v4Cluster));
        }
        catch (WebApplicationException exception) {
            throw adaptException(exception);
        }
    }

    @DELETE
    public Response remove() {
        return adaptRemove(getDelegate()::remove);
    }

    @POST
    @Consumes({"application/xml", "application/json"})
    @Actionable
    @Path("resetemulatedmachine")
    public Response resetEmulatedMachine(V3Action action) {
        return adaptAction(getDelegate()::resetEmulatedMachine, action);
    }

    @Path("affinitygroups")
    public V3AffinityGroupsServer getAffinityGroupsResource() {
        return new V3AffinityGroupsServer(getDelegate().getAffinityGroupsResource());
    }

    @Path("cpuprofiles")
    public V3AssignedCpuProfilesServer getCpuProfilesResource() {
        return new V3AssignedCpuProfilesServer(getDelegate().getCpuProfilesResource());
    }

    @Path("networks")
    public V3AssignedNetworksServer getNetworksResource() {
        return new V3AssignedNetworksServer(getDelegate().getNetworksResource());
    }

    @Path("permissions")
    public V3AssignedPermissionsServer getPermissionsResource() {
        return new V3AssignedPermissionsServer(getDelegate().getPermissionsResource());
    }

    @Path("glusterhooks")
    public V3GlusterHooksServer getGlusterHooksResource() {
        return new V3GlusterHooksServer(getDelegate().getGlusterHooksResource());
    }

    @Path("glustervolumes")
    public V3GlusterVolumesServer getGlusterVolumesResource() {
        return new V3GlusterVolumesServer(getDelegate().getGlusterVolumesResource());
    }

    @Path("{action: (resetemulatedmachine)}/{oid}")
    public V3ActionServer getActionResource(@PathParam("action") String action, @PathParam("oid") String oid) {
        return new V3ActionServer(getDelegate().getActionResource(action, oid));
    }
}
