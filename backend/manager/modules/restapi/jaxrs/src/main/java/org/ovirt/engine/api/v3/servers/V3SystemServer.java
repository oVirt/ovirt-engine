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

import static java.util.stream.Collectors.joining;

import java.io.IOException;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.ovirt.engine.api.model.Actionable;
import org.ovirt.engine.api.resource.SystemResource;
import org.ovirt.engine.api.restapi.invocation.CurrentManager;
import org.ovirt.engine.api.restapi.rsdl.RsdlLoader;
import org.ovirt.engine.api.restapi.util.ParametersHelper;
import org.ovirt.engine.api.v3.V3Server;
import org.ovirt.engine.api.v3.types.V3API;
import org.ovirt.engine.api.v3.types.V3Action;
import org.ovirt.engine.api.v3.types.V3Link;
import org.ovirt.engine.api.v3.types.V3RSDL;

@Path("/")
@Produces({"application/xml", "application/json"})
public class V3SystemServer extends V3Server<SystemResource> {
    public V3SystemServer(SystemResource delegate) {
        super(delegate);
    }

    @GET
    public Response get(@Context HttpHeaders headers, @Context UriInfo ui) {
        // Check if the RSDL was requested, and return it:
        if (ParametersHelper.getParameter(headers, ui, "rsdl") != null) {
            try {
                V3RSDL rsdl = getRSDL();
                return Response.ok().entity(rsdl).build();
            }
            catch (Exception exception) {
                throw new WebApplicationException(
                    exception,
                    Response.status(Response.Status.INTERNAL_SERVER_ERROR).build()
                );
            }
        }

        // Adapt the V4 response:
        Response response = adaptResponse(getDelegate()::get);

        // Replace the "Link" header with links calculated from the response body:
        response = replaceLinkHeader(response);

        return response;
    }

    private V3RSDL getRSDL() throws IOException {
        return RsdlLoader.loadRsdl(V3RSDL.class);
    }

    @HEAD
    public Response head() {
        // We need the V4 response for the GET method, as the response for the HEAD response doesn't contain an entity
        // and thus doesn't contain the links:
        Response response = adaptResponse(getDelegate()::get);

        // Replace the "Link" header with links calculated from the response body:
        response = replaceLinkHeader(response);

        // Clear the response body:
        response = Response.fromResponse(response)
            .entity(null)
            .build();

        return response;
    }

    private Response replaceLinkHeader(Response response) {
        Object entity = response.getEntity();
        if (entity != null && entity instanceof V3API) {
            V3API api = (V3API) entity;
            List<V3Link> links = api.getLinks();
            if (links != null) {
                String root = CurrentManager.get().getRoot();
                String header = links.stream()
                    .map(link -> String.format("<%s%s>; rel=%s", root, link.getHref(), link.getRel()))
                    .collect(joining(","));
                response = Response.fromResponse(response)
                    .header("Link", null)
                    .header("Link", header)
                    .build();
            }
        }
        return response;
    }

    @Path("capabilities")
    public V3CapabilitiesServer getCapabilitiesResource() {
        return new V3CapabilitiesServer();
    }

    @POST
    @Consumes({"application/xml", "application/json"})
    @Actionable
    @Path("reloadconfigurations")
    public Response reloadConfigurations(V3Action action) {
        return adaptAction(getDelegate()::reloadConfigurations, action);
    }

    @Path("bookmarks")
    public V3BookmarksServer getBookmarksResource() {
        return new V3BookmarksServer(getDelegate().getBookmarksResource());
    }

    @Path("clusters")
    public V3ClustersServer getClustersResource() {
        return new V3ClustersServer(getDelegate().getClustersResource());
    }

    @Path("cpuprofiles")
    public V3CpuProfilesServer getCpuProfilesResource() {
        return new V3CpuProfilesServer(getDelegate().getCpuProfilesResource());
    }

    @Path("datacenters")
    public V3DataCentersServer getDataCentersResource() {
        return new V3DataCentersServer(getDelegate().getDataCentersResource());
    }

    @Path("diskprofiles")
    public V3DiskProfilesServer getDiskProfilesResource() {
        return new V3DiskProfilesServer(getDelegate().getDiskProfilesResource());
    }

    @Path("disks")
    public V3DisksServer getDisksResource() {
        return new V3DisksServer(getDelegate().getDisksResource());
    }

    @Path("domains")
    public V3DomainsServer getDomainsResource() {
        return new V3DomainsServer(getDelegate().getDomainsResource());
    }

    @Path("events")
    public V3EventsServer getEventsResource() {
        return new V3EventsServer(getDelegate().getEventsResource());
    }

    @Path("externalhostproviders")
    public V3ExternalHostProvidersServer getExternalHostProvidersResource() {
        return new V3ExternalHostProvidersServer(getDelegate().getExternalHostProvidersResource());
    }

    @Path("groups")
    public V3GroupsServer getGroupsResource() {
        return new V3GroupsServer(getDelegate().getGroupsResource());
    }

    @Path("hosts")
    public V3HostsServer getHostsResource() {
        return new V3HostsServer(getDelegate().getHostsResource());
    }

    @Path("icons")
    public V3IconsServer getIconsResource() {
        return new V3IconsServer(getDelegate().getIconsResource());
    }

    @Path("instancetypes")
    public V3InstanceTypesServer getInstanceTypesResource() {
        return new V3InstanceTypesServer(getDelegate().getInstanceTypesResource());
    }

    @Path("jobs")
    public V3JobsServer getJobsResource() {
        return new V3JobsServer(getDelegate().getJobsResource());
    }

    @Path("macpools")
    public V3MacPoolsServer getMacPoolsResource() {
        return new V3MacPoolsServer(getDelegate().getMacPoolsResource());
    }

    @Path("networks")
    public V3NetworksServer getNetworksResource() {
        return new V3NetworksServer(getDelegate().getNetworksResource());
    }

    @Path("openstackimageproviders")
    public V3OpenstackImageProvidersServer getOpenstackImageProviersResource() {
        return new V3OpenstackImageProvidersServer(getDelegate().getOpenstackImageProvidersResource());
    }

    @Path("openstacknetworkproviders")
    public V3OpenstackNetworkProvidersServer getOpenstackNetworkProvidersResource() {
        return new V3OpenstackNetworkProvidersServer(getDelegate().getOpenstackNetworkProvidersResource());
    }

    @Path("openstackvolumeproviders")
    public V3OpenstackVolumeProvidersServer getOpenstackVolumeProvidersResource() {
        return new V3OpenstackVolumeProvidersServer(getDelegate().getOpenstackVolumeProvidersResource());
    }

    @Path("operatingsystems")
    public V3OperatingSystemsServer getOperatingSystemsResource() {
        return new V3OperatingSystemsServer(getDelegate().getOperatingSystemsResource());
    }

    @Path("roles")
    public V3RolesServer getRolesResource() {
        return new V3RolesServer(getDelegate().getRolesResource());
    }

    @Path("schedulingpolicies")
    public V3SchedulingPoliciesServer getSchedulingPoliciesResource() {
        return new V3SchedulingPoliciesServer(getDelegate().getSchedulingPoliciesResource());
    }

    @Path("schedulingpolicyunits")
    public V3SchedulingPolicyUnitsServer getSchedulingPolicyUnitsResource() {
        return new V3SchedulingPolicyUnitsServer(getDelegate().getSchedulingPolicyUnitsResource());
    }

    @Path("storagedomains")
    public V3StorageDomainsServer getStorageDomainsResource() {
        return new V3StorageDomainsServer(getDelegate().getStorageDomainsResource());
    }

    @Path("storageconnections")
    public V3StorageServerConnectionsServer getStorageConnectionsResource() {
        return new V3StorageServerConnectionsServer(getDelegate().getStorageConnectionsResource());
    }

    @Path("katelloerrata")
    public V3KatelloErrataServer getKatelloErrataResource() {
        return new V3KatelloErrataServer(getDelegate().getKatelloErrataResource());
    }

    @Path("permissions")
    public V3AssignedPermissionsServer getPermissionsResource() {
        return new V3AssignedPermissionsServer(getDelegate().getPermissionsResource());
    }

    @Path("tags")
    public V3TagsServer getTagsResource() {
        return new V3TagsServer(getDelegate().getTagsResource());
    }

    @Path("templates")
    public V3TemplatesServer getTemplatesResource() {
        return new V3TemplatesServer(getDelegate().getTemplatesResource());
    }

    @Path("users")
    public V3UsersServer getUsersResource() {
        return new V3UsersServer(getDelegate().getUsersResource());
    }

    @Path("vmpools")
    public V3VmPoolsServer getVmPoolsResource() {
        return new V3VmPoolsServer(getDelegate().getVmPoolsResource());
    }

    @Path("vms")
    public V3VmsServer getVmsResource() {
        return new V3VmsServer(getDelegate().getVmsResource());
    }

    @Path("vnicprofiles")
    public V3VnicProfilesServer getVnicProfilesResource() {
        return new V3VnicProfilesServer(getDelegate().getVnicProfilesResource());
    }

    @Path("{action: (reloadconfigurations)}/{oid}")
    public V3ActionServer getActionResource(@PathParam("action") String action, @PathParam("oid") String oid) {
        return new V3ActionServer(getDelegate().getActionResource(action, oid));
    }
}
