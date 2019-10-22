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

    @Path("{action: (?:attach|detach|updatevirtualfunctionsconfiguration)}/{oid}")
    public V3ActionServer getActionResource(@PathParam("action") String action, @PathParam("oid") String oid) {
        return new V3ActionServer(getDelegate().getActionResource(action, oid));
    }
}
