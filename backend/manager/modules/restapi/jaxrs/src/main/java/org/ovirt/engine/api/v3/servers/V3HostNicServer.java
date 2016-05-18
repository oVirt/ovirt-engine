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
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Actionable;
import org.ovirt.engine.api.resource.HostNicResource;
import org.ovirt.engine.api.v3.V3Server;
import org.ovirt.engine.api.v3.types.V3Action;
import org.ovirt.engine.api.v3.types.V3HostNIC;

@Produces({"application/xml", "application/json"})
public class V3HostNicServer extends V3Server<HostNicResource> {
    public V3HostNicServer(HostNicResource delegate) {
        super(delegate);
    }

    @GET
    public V3HostNIC get() {
        return adaptGet(getDelegate()::get);
    }

    @POST
    @Consumes({"application/xml", "application/json"})
    @Actionable
    @Path("updatevirtualfunctionsconfiguration")
    public Response updateVirtualFunctionsConfiguration(V3Action action) {
        return adaptAction(getDelegate()::updateVirtualFunctionsConfiguration, action);
    }

    @Path("labels")
    public V3LabelsServer getLabelsResource() {
        return new V3LabelsServer(getDelegate().getNetworkLabelsResource());
    }

    @Path("networkattachments")
    public V3NetworkAttachmentsServer getNetworkAttachmentsResource() {
        return new V3NetworkAttachmentsServer(getDelegate().getNetworkAttachmentsResource());
    }

    @Path("virtualfunctionallowedlabels")
    public V3LabelsServer getVirtualFunctionAllowedLabelsResource() {
        return new V3LabelsServer(getDelegate().getVirtualFunctionAllowedLabelsResource());
    }

    @Path("virtualfunctionallowednetworks")
    public V3VirtualFunctionAllowedNetworksServer getVirtualFunctionAllowedNetworksResource() {
        return new V3VirtualFunctionAllowedNetworksServer(getDelegate().getVirtualFunctionAllowedNetworksResource());
    }

    @Path("statistics")
    public V3StatisticsServer getStatisticsResource() {
        return new V3StatisticsServer(getDelegate().getStatisticsResource());
    }

    @Path("{action: (attach|detach|updatevirtualfunctionsconfiguration)}/{oid}")
    public V3ActionServer getActionResource(@PathParam("action") String action, @PathParam("oid") String oid) {
        return new V3ActionServer(getDelegate().getActionResource(action, oid));
    }
}
