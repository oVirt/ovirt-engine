/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.servers;

import static org.ovirt.engine.api.v3.adapters.V3InAdapters.adaptIn;
import static org.ovirt.engine.api.v3.helpers.V3ClusterHelper.assignCompatiblePolicy;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Cluster;
import org.ovirt.engine.api.resource.ClustersResource;
import org.ovirt.engine.api.v3.V3Server;
import org.ovirt.engine.api.v3.types.V3Cluster;
import org.ovirt.engine.api.v3.types.V3Clusters;

@Produces({"application/xml", "application/json"})
public class V3ClustersServer extends V3Server<ClustersResource> {
    public V3ClustersServer(ClustersResource delegate) {
        super(delegate);
    }

    @POST
    @Consumes({"application/xml", "application/json"})
    public Response add(V3Cluster v3Cluster) {
        Cluster v4Cluster = adaptIn(v3Cluster);
        assignCompatiblePolicy(v3Cluster, v4Cluster);
        try {
            return adaptResponse(getDelegate().add(v4Cluster));
        } catch(WebApplicationException exception) {
            throw adaptException(exception);
        }
    }

    @GET
    public V3Clusters list() {
        return adaptList(getDelegate()::list);
    }

    @Path("{id}")
    public V3ClusterServer getClusterResource(@PathParam("id") String id) {
        return new V3ClusterServer(getDelegate().getClusterResource(id));
    }
}
