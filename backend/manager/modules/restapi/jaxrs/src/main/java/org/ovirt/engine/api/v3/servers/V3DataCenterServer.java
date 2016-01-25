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
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.resource.DataCenterResource;
import org.ovirt.engine.api.v3.V3Server;
import org.ovirt.engine.api.v3.types.V3DataCenter;

@Produces({"application/xml", "application/json"})
public class V3DataCenterServer extends V3Server<DataCenterResource> {
    public V3DataCenterServer(DataCenterResource delegate) {
        super(delegate);
    }

    @GET
    public V3DataCenter get() {
        return adaptGet(delegate::get);
    }

    @PUT
    @Consumes({"application/xml", "application/json"})
    public V3DataCenter update(V3DataCenter dataCenter) {
        return adaptUpdate(delegate::update, dataCenter);
    }

    @DELETE
    public Response remove() {
        return adaptRemove(delegate::remove);
    }

    @Path("storagedomains")
    public V3AttachedStorageDomainsServer getStorageDomainsResource() {
        return new V3AttachedStorageDomainsServer(delegate.getStorageDomainsResource());
    }

    @Path("clusters")
    public V3ClustersServer getClustersResource() {
        return new V3ClustersServer(delegate.getClustersResource());
    }

    @Path("networks")
    public V3NetworksServer getNetworksResource() {
        return new V3NetworksServer(delegate.getNetworksResource());
    }

    @Path("permissions")
    public V3AssignedPermissionsServer getPermissionsResource() {
        return new V3AssignedPermissionsServer(delegate.getPermissionsResource());
    }

    @Path("quotas")
    public V3QuotasServer getQuotasResource() {
        return new V3QuotasServer(delegate.getQuotasResource());
    }

    @Path("qoss")
    public V3QossServer getQossResource() {
        return new V3QossServer(delegate.getQossResource());
    }

    @Path("iscsibonds")
    public V3IscsiBondsServer getIscsiBondsResource() {
        return new V3IscsiBondsServer(delegate.getIscsiBondsResource());
    }
}
