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
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.Actionable;
import org.ovirt.engine.api.resource.aaa.DomainsResource;
import org.ovirt.engine.api.resource.aaa.GroupsResource;
import org.ovirt.engine.api.resource.aaa.UsersResource;
import org.ovirt.engine.api.resource.externalhostproviders.ExternalHostProvidersResource;
import org.ovirt.engine.api.resource.externalhostproviders.SystemKatelloErrataResource;
import org.ovirt.engine.api.resource.openstack.OpenStackImageProvidersResource;
import org.ovirt.engine.api.resource.openstack.OpenStackNetworkProvidersResource;
import org.ovirt.engine.api.resource.openstack.OpenStackVolumeProvidersResource;

@Path("/")
@Produces({ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON})
public interface ApiResource {
    @HEAD
    Response head();

    @GET
    Response get();

    @POST
    @Consumes({ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON})
    @Actionable
    @Path("reloadconfigurations")
    Response reloadConfigurations(Action action);

    @Path("bookmarks")
    BookmarksResource getBookmarksResource();

    @Path("capabilities")
    CapabilitiesResource getCapabilitiesResource();

    @Path("clusters")
    ClustersResource getClustersResource();

    @Path("cpuprofiles")
    CpuProfilesResource getCpuProfilesResource();

    @Path("datacenters")
    DataCentersResource getDataCentersResource();

    @Path("diskprofiles")
    DiskProfilesResource getDiskProfilesResource();

    @Path("disks")
    DisksResource getDisksResource();

    @Path("domains")
    DomainsResource getDomainsResource();

    @Path("events")
    EventsResource getEventsResource();

    @Path("externalhostproviders")
    ExternalHostProvidersResource getExternalHostProvidersResource();

    @Path("groups")
    GroupsResource getGroupsResource();

    @Path("hosts")
    HostsResource getHostsResource();

    @Path("icons")
    IconsResource getIconsResource();

    @Path("instancetypes")
    InstanceTypesResource getInstanceTypesResource();

    @Path("jobs")
    JobsResource getJobsResource();

    @Path("macpools")
    MacPoolsResource getMacPoolsResource();

    @Path("networks")
    NetworksResource getNetworksResource();

    @Path("openstackimageproviders")
    OpenStackImageProvidersResource getOpenStackImageProviersResource();

    @Path("openstacknetworkproviders")
    OpenStackNetworkProvidersResource getOpenStackNetworkProvidersResource();

    @Path("openstackvolumeproviders")
    OpenStackVolumeProvidersResource getOpenStackVolumeProvidersResource();

    @Path("operatingsystems")
    OperatingSystemsResource getOperatingSystemsResource();

    @Path("roles")
    RolesResource getRolesResource();

    @Path("schedulingpolicies")
    SchedulingPoliciesResource getSchedulingPoliciesResource();

    @Path("schedulingpolicyunits")
    SchedulingPolicyUnitsResource getSchedulingPolicyUnitsResource();

    @Path("storagedomains")
    StorageDomainsResource getStorageDomainsResource();

    @Path("storageconnections")
    StorageServerConnectionsResource getStorageConnectionsResource();

    @Path("katelloerrata")
    SystemKatelloErrataResource getKatelloErrataResource();

    @Path("permissions")
    SystemPermissionsResource getPermissionsResource();

    @Path("tags")
    TagsResource getTagsResource();

    @Path("templates")
    TemplatesResource getTemplatesResource();

    @Path("users")
    UsersResource getUsersResource();

    @Path("vmpools")
    VmPoolsResource getVmPoolsResource();

    @Path("vms")
    VmsResource getVmsResource();

    @Path("vnicprofiles")
    VnicProfilesResource getVnicProfilesResource();
}
