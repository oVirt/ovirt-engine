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
import javax.ws.rs.HEAD;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Actionable;
import org.ovirt.engine.api.resource.SystemResource;
import org.ovirt.engine.api.v3.V3Server;
import org.ovirt.engine.api.v3.types.V3Action;

@Path("/")
@Produces({"application/xml", "application/json"})
public class V3SystemServer extends V3Server<SystemResource> {
    public V3SystemServer(SystemResource delegate) {
        super(delegate);
    }

    @GET
    public Response get() {
        return adaptRemove(delegate::get);
    }

    @HEAD
    public Response head() {
        return adaptResponse(delegate::get);
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
        return adaptAction(delegate::reloadConfigurations, action);
    }

    @Path("bookmarks")
    public V3BookmarksServer getBookmarksResource() {
        return new V3BookmarksServer(delegate.getBookmarksResource());
    }

    @Path("clusters")
    public V3ClustersServer getClustersResource() {
        return new V3ClustersServer(delegate.getClustersResource());
    }

    @Path("cpuprofiles")
    public V3CpuProfilesServer getCpuProfilesResource() {
        return new V3CpuProfilesServer(delegate.getCpuProfilesResource());
    }

    @Path("datacenters")
    public V3DataCentersServer getDataCentersResource() {
        return new V3DataCentersServer(delegate.getDataCentersResource());
    }

    @Path("diskprofiles")
    public V3DiskProfilesServer getDiskProfilesResource() {
        return new V3DiskProfilesServer(delegate.getDiskProfilesResource());
    }

    @Path("disks")
    public V3DisksServer getDisksResource() {
        return new V3DisksServer(delegate.getDisksResource());
    }

    @Path("domains")
    public V3DomainsServer getDomainsResource() {
        return new V3DomainsServer(delegate.getDomainsResource());
    }

    @Path("events")
    public V3EventsServer getEventsResource() {
        return new V3EventsServer(delegate.getEventsResource());
    }

    @Path("externalhostproviders")
    public V3ExternalHostProvidersServer getExternalHostProvidersResource() {
        return new V3ExternalHostProvidersServer(delegate.getExternalHostProvidersResource());
    }

    @Path("groups")
    public V3GroupsServer getGroupsResource() {
        return new V3GroupsServer(delegate.getGroupsResource());
    }

    @Path("hosts")
    public V3HostsServer getHostsResource() {
        return new V3HostsServer(delegate.getHostsResource());
    }

    @Path("icons")
    public V3IconsServer getIconsResource() {
        return new V3IconsServer(delegate.getIconsResource());
    }

    @Path("instancetypes")
    public V3InstanceTypesServer getInstanceTypesResource() {
        return new V3InstanceTypesServer(delegate.getInstanceTypesResource());
    }

    @Path("jobs")
    public V3JobsServer getJobsResource() {
        return new V3JobsServer(delegate.getJobsResource());
    }

    @Path("macpools")
    public V3MacPoolsServer getMacPoolsResource() {
        return new V3MacPoolsServer(delegate.getMacPoolsResource());
    }

    @Path("networks")
    public V3NetworksServer getNetworksResource() {
        return new V3NetworksServer(delegate.getNetworksResource());
    }

    @Path("openstackimageproviers")
    public V3OpenstackImageProvidersServer getOpenstackImageProviersResource() {
        return new V3OpenstackImageProvidersServer(delegate.getOpenstackImageProviersResource());
    }

    @Path("openstacknetworkproviders")
    public V3OpenstackNetworkProvidersServer getOpenstackNetworkProvidersResource() {
        return new V3OpenstackNetworkProvidersServer(delegate.getOpenstackNetworkProvidersResource());
    }

    @Path("openstackvolumeproviders")
    public V3OpenstackVolumeProvidersServer getOpenstackVolumeProvidersResource() {
        return new V3OpenstackVolumeProvidersServer(delegate.getOpenstackVolumeProvidersResource());
    }

    @Path("operatingsystems")
    public V3OperatingSystemsServer getOperatingSystemsResource() {
        return new V3OperatingSystemsServer(delegate.getOperatingSystemsResource());
    }

    @Path("roles")
    public V3RolesServer getRolesResource() {
        return new V3RolesServer(delegate.getRolesResource());
    }

    @Path("schedulingpolicies")
    public V3SchedulingPoliciesServer getSchedulingPoliciesResource() {
        return new V3SchedulingPoliciesServer(delegate.getSchedulingPoliciesResource());
    }

    @Path("schedulingpolicyunits")
    public V3SchedulingPolicyUnitsServer getSchedulingPolicyUnitsResource() {
        return new V3SchedulingPolicyUnitsServer(delegate.getSchedulingPolicyUnitsResource());
    }

    @Path("storagedomains")
    public V3StorageDomainsServer getStorageDomainsResource() {
        return new V3StorageDomainsServer(delegate.getStorageDomainsResource());
    }

    @Path("storageconnections")
    public V3StorageServerConnectionsServer getStorageConnectionsResource() {
        return new V3StorageServerConnectionsServer(delegate.getStorageConnectionsResource());
    }

    @Path("katelloerrata")
    public V3EngineKatelloErrataServer getKatelloErrataResource() {
        return new V3EngineKatelloErrataServer(delegate.getKatelloErrataResource());
    }

    @Path("permissions")
    public V3SystemPermissionsServer getPermissionsResource() {
        return new V3SystemPermissionsServer(delegate.getPermissionsResource());
    }

    @Path("tags")
    public V3TagsServer getTagsResource() {
        return new V3TagsServer(delegate.getTagsResource());
    }

    @Path("templates")
    public V3TemplatesServer getTemplatesResource() {
        return new V3TemplatesServer(delegate.getTemplatesResource());
    }

    @Path("users")
    public V3UsersServer getUsersResource() {
        return new V3UsersServer(delegate.getUsersResource());
    }

    @Path("vmpools")
    public V3VmPoolsServer getVmPoolsResource() {
        return new V3VmPoolsServer(delegate.getVmPoolsResource());
    }

    @Path("vms")
    public V3VmsServer getVmsResource() {
        return new V3VmsServer(delegate.getVmsResource());
    }

    @Path("vnicprofiles")
    public V3VnicProfilesServer getVnicProfilesResource() {
        return new V3VnicProfilesServer(delegate.getVnicProfilesResource());
    }

    @Path("{action: (reloadconfigurations)}/{oid}")
    public V3ActionServer getActionResource(@PathParam("action") String action, @PathParam("oid") String oid) {
        return new V3ActionServer(delegate.getActionResource(action, oid));
    }
}
