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
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.resource.externalhostproviders.ExternalHostProviderResource;
import org.ovirt.engine.api.v3.V3Server;
import org.ovirt.engine.api.v3.types.V3ExternalHostProvider;

@Produces({"application/xml", "application/json"})
public class V3ExternalHostProviderServer extends V3Server<ExternalHostProviderResource> {
    public V3ExternalHostProviderServer(ExternalHostProviderResource delegate) {
        super(delegate);
    }

    @GET
    public V3ExternalHostProvider get() {
        return adaptGet(delegate::get);
    }

    @PUT
    @Consumes({"application/xml", "application/json"})
    public V3ExternalHostProvider update(V3ExternalHostProvider provider) {
        return adaptUpdate(delegate::update, provider);
    }

    @DELETE
    public Response remove() {
        return adaptRemove(delegate::remove);
    }

    @Path("computeresources")
    public V3ExternalComputeResourcesServer getComputeResourcesResource() {
        return new V3ExternalComputeResourcesServer(delegate.getComputeResourcesResource());
    }

    @Path("discoveredhosts")
    public V3ExternalDiscoveredHostsServer getDiscoveredHostsResource() {
        return new V3ExternalDiscoveredHostsServer(delegate.getDiscoveredHostsResource());
    }

    @Path("hostgroups")
    public V3ExternalHostGroupsServer getHostGroupsResource() {
        return new V3ExternalHostGroupsServer(delegate.getHostGroupsResource());
    }

    @Path("hosts")
    public V3ExternalHostsServer getHostsResource() {
        return new V3ExternalHostsServer(delegate.getHostsResource());
    }

    @Path("{action: (importcertificates|testconnectivity)}/{oid}")
    public V3ActionServer getActionResource(@PathParam("action") String action, @PathParam("oid") String oid) {
        return new V3ActionServer(delegate.getActionResource(action, oid));
    }
}
