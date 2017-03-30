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

import java.util.Set;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.ovirt.engine.api.common.util.DetailHelper;
import org.ovirt.engine.api.resource.HostsResource;
import org.ovirt.engine.api.v3.V3Server;
import org.ovirt.engine.api.v3.helpers.V3HostHelper;
import org.ovirt.engine.api.v3.types.V3Host;
import org.ovirt.engine.api.v3.types.V3Hosts;

@Produces({"application/xml", "application/json"})
public class V3HostsServer extends V3Server<HostsResource> {
    public V3HostsServer(HostsResource delegate) {
        super(delegate);
    }

    @POST
    @Consumes({"application/xml", "application/json"})
    public Response add(V3Host host) {
        return adaptAdd(getDelegate()::add, host);
    }

    @GET
    public V3Hosts list(@Context HttpHeaders headers, @Context UriInfo ui) {
        V3Hosts hosts = adaptList(getDelegate()::list);

        // In V3 the collection of hosts used to have the statistics inline, but only when the 'statistics' detail
        // was explicitly provided:
        Set<String> details = DetailHelper.getDetails(headers, ui);
        if (details.contains("statistics")) {
            hosts.getHosts().forEach(V3HostHelper::addStatistics);
        }

        return hosts;
    }

    @Path("{id}")
    public V3HostServer getHostResource(@PathParam("id") String id) {
        return new V3HostServer(getDelegate().getHostResource(id));
    }
}
