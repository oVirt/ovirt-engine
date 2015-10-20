/*
* Copyright (c) 2010 Red Hat, Inc.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*           http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.ovirt.engine.api.resource;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.DataCenter;

@Produces({ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON})
public interface DataCenterResource {
    @GET
    DataCenter get();

    @PUT
    @Consumes({ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON})
    DataCenter update(DataCenter dataCenter);

    @DELETE
    Response remove();

    @DELETE
    @Consumes({ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON})
    Response remove(Action action);

    @Path("storagedomains")
    AttachedStorageDomainsResource getAttachedStorageDomainsResource();

    @Path("clusters")
    ClustersResource getClustersResource();

    @Path("networks")
    NetworksResource getNetworksResource();

    @Path("permissions")
    AssignedPermissionsResource getPermissionsResource();

    @Path("quotas")
    QuotasResource getQuotasResource();

    @Path("qoss")
    QoSsResource getQossResource();

    @Path("iscsibonds")
    IscsiBondsResource getIscsiBondsResource();
}
