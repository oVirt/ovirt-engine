/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.servers;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.resource.DataCenterNetworkResource;
import org.ovirt.engine.api.resource.NetworkResource;
import org.ovirt.engine.api.restapi.resource.BackendApiResource;
import org.ovirt.engine.api.v3.V3Server;
import org.ovirt.engine.api.v3.types.V3Network;

@Produces({"application/xml", "application/json"})
public class V3DataCenterNetworkServer extends V3Server<DataCenterNetworkResource> {
    private String networkId;

    public V3DataCenterNetworkServer(String networkId, DataCenterNetworkResource delegate) {
        super(delegate);
        this.networkId = networkId;
    }

    @GET
    public V3Network get() {
        return adaptGet(getDelegate()::get);
    }

    @PUT
    @Consumes({"application/xml", "application/json"})
    public V3Network update(V3Network network) {
        return adaptUpdate(getDelegate()::update, network);
    }

    @DELETE
    public Response remove() {
        return adaptRemove(getDelegate()::remove);
    }

    @Path("permissions")
    public V3AssignedPermissionsServer getPermissionsResource() {
        return new V3AssignedPermissionsServer(getNetworkResource().getPermissionsResource());
    }

    @Path("vnicprofiles")
    public V3AssignedVnicProfilesServer getVnicProfilesResource() {
        return new V3AssignedVnicProfilesServer(getNetworkResource().getVnicProfilesResource());
    }

    @Path("labels")
    public V3LabelsServer getLabelsResource() {
        return new V3LabelsServer(getNetworkResource().getNetworkLabelsResource());
    }

    private NetworkResource getNetworkResource() {
        return BackendApiResource.getInstance()
            .getNetworksResource()
            .getNetworkResource(networkId);
    }
}
