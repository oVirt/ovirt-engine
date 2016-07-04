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

package org.ovirt.engine.api.restapi.util;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.ws.rs.Path;

import org.ovirt.engine.api.model.ActionableResource;
import org.ovirt.engine.api.model.AffinityGroup;
import org.ovirt.engine.api.model.AffinityLabel;
import org.ovirt.engine.api.model.Agent;
import org.ovirt.engine.api.model.Application;
import org.ovirt.engine.api.model.Balance;
import org.ovirt.engine.api.model.BaseResource;
import org.ovirt.engine.api.model.Bookmark;
import org.ovirt.engine.api.model.Cdrom;
import org.ovirt.engine.api.model.Cluster;
import org.ovirt.engine.api.model.ClusterLevel;
import org.ovirt.engine.api.model.CpuProfile;
import org.ovirt.engine.api.model.DataCenter;
import org.ovirt.engine.api.model.Disk;
import org.ovirt.engine.api.model.DiskAttachment;
import org.ovirt.engine.api.model.DiskProfile;
import org.ovirt.engine.api.model.DiskSnapshot;
import org.ovirt.engine.api.model.Domain;
import org.ovirt.engine.api.model.Event;
import org.ovirt.engine.api.model.ExternalComputeResource;
import org.ovirt.engine.api.model.ExternalDiscoveredHost;
import org.ovirt.engine.api.model.ExternalHost;
import org.ovirt.engine.api.model.ExternalHostGroup;
import org.ovirt.engine.api.model.ExternalHostProvider;
import org.ovirt.engine.api.model.File;
import org.ovirt.engine.api.model.Filter;
import org.ovirt.engine.api.model.GlusterBrick;
import org.ovirt.engine.api.model.GlusterHook;
import org.ovirt.engine.api.model.GlusterVolume;
import org.ovirt.engine.api.model.GlusterVolumeProfileDetails;
import org.ovirt.engine.api.model.GraphicsConsole;
import org.ovirt.engine.api.model.Group;
import org.ovirt.engine.api.model.Hook;
import org.ovirt.engine.api.model.Host;
import org.ovirt.engine.api.model.HostDevice;
import org.ovirt.engine.api.model.HostDevices;
import org.ovirt.engine.api.model.HostNic;
import org.ovirt.engine.api.model.HostStorage;
import org.ovirt.engine.api.model.Icon;
import org.ovirt.engine.api.model.Image;
import org.ovirt.engine.api.model.InstanceType;
import org.ovirt.engine.api.model.IscsiBond;
import org.ovirt.engine.api.model.Job;
import org.ovirt.engine.api.model.KatelloErratum;
import org.ovirt.engine.api.model.MacPool;
import org.ovirt.engine.api.model.Network;
import org.ovirt.engine.api.model.NetworkAttachment;
import org.ovirt.engine.api.model.NetworkFilter;
import org.ovirt.engine.api.model.NetworkLabel;
import org.ovirt.engine.api.model.Nic;
import org.ovirt.engine.api.model.NumaNode;
import org.ovirt.engine.api.model.OpenStackImage;
import org.ovirt.engine.api.model.OpenStackImageProvider;
import org.ovirt.engine.api.model.OpenStackNetwork;
import org.ovirt.engine.api.model.OpenStackNetworkProvider;
import org.ovirt.engine.api.model.OpenStackSubnet;
import org.ovirt.engine.api.model.OpenStackVolumeProvider;
import org.ovirt.engine.api.model.OpenStackVolumeType;
import org.ovirt.engine.api.model.OpenstackVolumeAuthenticationKey;
import org.ovirt.engine.api.model.OperatingSystemInfo;
import org.ovirt.engine.api.model.Permission;
import org.ovirt.engine.api.model.Permit;
import org.ovirt.engine.api.model.Qos;
import org.ovirt.engine.api.model.Quota;
import org.ovirt.engine.api.model.QuotaClusterLimit;
import org.ovirt.engine.api.model.QuotaStorageLimit;
import org.ovirt.engine.api.model.ReportedDevice;
import org.ovirt.engine.api.model.Role;
import org.ovirt.engine.api.model.SchedulingPolicy;
import org.ovirt.engine.api.model.SchedulingPolicyUnit;
import org.ovirt.engine.api.model.Session;
import org.ovirt.engine.api.model.Snapshot;
import org.ovirt.engine.api.model.SshPublicKey;
import org.ovirt.engine.api.model.Statistic;
import org.ovirt.engine.api.model.Step;
import org.ovirt.engine.api.model.StorageConnection;
import org.ovirt.engine.api.model.StorageConnectionExtension;
import org.ovirt.engine.api.model.StorageDomain;
import org.ovirt.engine.api.model.Tag;
import org.ovirt.engine.api.model.Template;
import org.ovirt.engine.api.model.UnmanagedNetwork;
import org.ovirt.engine.api.model.User;
import org.ovirt.engine.api.model.VirtualNumaNode;
import org.ovirt.engine.api.model.Vm;
import org.ovirt.engine.api.model.VmPool;
import org.ovirt.engine.api.model.VnicProfile;
import org.ovirt.engine.api.model.Watchdog;
import org.ovirt.engine.api.model.Weight;
import org.ovirt.engine.api.resource.AffinityGroupResource;
import org.ovirt.engine.api.resource.AffinityGroupsResource;
import org.ovirt.engine.api.resource.AffinityLabelHostResource;
import org.ovirt.engine.api.resource.AffinityLabelHostsResource;
import org.ovirt.engine.api.resource.AffinityLabelResource;
import org.ovirt.engine.api.resource.AffinityLabelVmResource;
import org.ovirt.engine.api.resource.AffinityLabelVmsResource;
import org.ovirt.engine.api.resource.AffinityLabelsResource;
import org.ovirt.engine.api.resource.AssignedAffinityLabelResource;
import org.ovirt.engine.api.resource.AssignedAffinityLabelsResource;
import org.ovirt.engine.api.resource.AssignedNetworkResource;
import org.ovirt.engine.api.resource.AssignedNetworksResource;
import org.ovirt.engine.api.resource.AssignedPermissionsResource;
import org.ovirt.engine.api.resource.AssignedRolesResource;
import org.ovirt.engine.api.resource.AssignedTagResource;
import org.ovirt.engine.api.resource.AssignedTagsResource;
import org.ovirt.engine.api.resource.AttachedStorageDomainResource;
import org.ovirt.engine.api.resource.AttachedStorageDomainsResource;
import org.ovirt.engine.api.resource.BalanceResource;
import org.ovirt.engine.api.resource.BalancesResource;
import org.ovirt.engine.api.resource.BookmarkResource;
import org.ovirt.engine.api.resource.BookmarksResource;
import org.ovirt.engine.api.resource.ClusterLevelResource;
import org.ovirt.engine.api.resource.ClusterLevelsResource;
import org.ovirt.engine.api.resource.ClusterResource;
import org.ovirt.engine.api.resource.ClustersResource;
import org.ovirt.engine.api.resource.CpuProfileResource;
import org.ovirt.engine.api.resource.CpuProfilesResource;
import org.ovirt.engine.api.resource.DataCenterResource;
import org.ovirt.engine.api.resource.DataCentersResource;
import org.ovirt.engine.api.resource.DiskAttachmentResource;
import org.ovirt.engine.api.resource.DiskAttachmentsResource;
import org.ovirt.engine.api.resource.DiskProfileResource;
import org.ovirt.engine.api.resource.DiskProfilesResource;
import org.ovirt.engine.api.resource.DiskResource;
import org.ovirt.engine.api.resource.DiskSnapshotResource;
import org.ovirt.engine.api.resource.DiskSnapshotsResource;
import org.ovirt.engine.api.resource.DisksResource;
import org.ovirt.engine.api.resource.EventResource;
import org.ovirt.engine.api.resource.EventsResource;
import org.ovirt.engine.api.resource.FenceAgentResource;
import org.ovirt.engine.api.resource.FenceAgentsResource;
import org.ovirt.engine.api.resource.FileResource;
import org.ovirt.engine.api.resource.FilesResource;
import org.ovirt.engine.api.resource.FilterResource;
import org.ovirt.engine.api.resource.FiltersResource;
import org.ovirt.engine.api.resource.GraphicsConsoleResource;
import org.ovirt.engine.api.resource.GraphicsConsolesResource;
import org.ovirt.engine.api.resource.HostDeviceResource;
import org.ovirt.engine.api.resource.HostDevicesResource;
import org.ovirt.engine.api.resource.HostHookResource;
import org.ovirt.engine.api.resource.HostHooksResource;
import org.ovirt.engine.api.resource.HostNicResource;
import org.ovirt.engine.api.resource.HostNicsResource;
import org.ovirt.engine.api.resource.HostNumaNodeResource;
import org.ovirt.engine.api.resource.HostNumaNodesResource;
import org.ovirt.engine.api.resource.HostStorageResource;
import org.ovirt.engine.api.resource.IconResource;
import org.ovirt.engine.api.resource.IconsResource;
import org.ovirt.engine.api.resource.ImageResource;
import org.ovirt.engine.api.resource.ImagesResource;
import org.ovirt.engine.api.resource.InstanceTypeNicResource;
import org.ovirt.engine.api.resource.InstanceTypeNicsResource;
import org.ovirt.engine.api.resource.InstanceTypeResource;
import org.ovirt.engine.api.resource.InstanceTypeWatchdogResource;
import org.ovirt.engine.api.resource.InstanceTypeWatchdogsResource;
import org.ovirt.engine.api.resource.InstanceTypesResource;
import org.ovirt.engine.api.resource.IscsiBondResource;
import org.ovirt.engine.api.resource.IscsiBondsResource;
import org.ovirt.engine.api.resource.JobResource;
import org.ovirt.engine.api.resource.JobsResource;
import org.ovirt.engine.api.resource.MacPoolResource;
import org.ovirt.engine.api.resource.MacPoolsResource;
import org.ovirt.engine.api.resource.NetworkAttachmentResource;
import org.ovirt.engine.api.resource.NetworkAttachmentsResource;
import org.ovirt.engine.api.resource.NetworkFilterResource;
import org.ovirt.engine.api.resource.NetworkFiltersResource;
import org.ovirt.engine.api.resource.NetworkLabelResource;
import org.ovirt.engine.api.resource.NetworkLabelsResource;
import org.ovirt.engine.api.resource.NetworkResource;
import org.ovirt.engine.api.resource.NetworksResource;
import org.ovirt.engine.api.resource.OperatingSystemResource;
import org.ovirt.engine.api.resource.OperatingSystemsResource;
import org.ovirt.engine.api.resource.PermissionResource;
import org.ovirt.engine.api.resource.PermitResource;
import org.ovirt.engine.api.resource.PermitsResource;
import org.ovirt.engine.api.resource.QosResource;
import org.ovirt.engine.api.resource.QossResource;
import org.ovirt.engine.api.resource.QuotaClusterLimitResource;
import org.ovirt.engine.api.resource.QuotaClusterLimitsResource;
import org.ovirt.engine.api.resource.QuotaResource;
import org.ovirt.engine.api.resource.QuotaStorageLimitResource;
import org.ovirt.engine.api.resource.QuotaStorageLimitsResource;
import org.ovirt.engine.api.resource.QuotasResource;
import org.ovirt.engine.api.resource.RoleResource;
import org.ovirt.engine.api.resource.RolesResource;
import org.ovirt.engine.api.resource.SchedulingPoliciesResource;
import org.ovirt.engine.api.resource.SchedulingPolicyResource;
import org.ovirt.engine.api.resource.SchedulingPolicyUnitResource;
import org.ovirt.engine.api.resource.SchedulingPolicyUnitsResource;
import org.ovirt.engine.api.resource.SnapshotResource;
import org.ovirt.engine.api.resource.SnapshotsResource;
import org.ovirt.engine.api.resource.StatisticResource;
import org.ovirt.engine.api.resource.StatisticsResource;
import org.ovirt.engine.api.resource.StepResource;
import org.ovirt.engine.api.resource.StepsResource;
import org.ovirt.engine.api.resource.StorageDomainResource;
import org.ovirt.engine.api.resource.StorageDomainTemplateResource;
import org.ovirt.engine.api.resource.StorageDomainTemplatesResource;
import org.ovirt.engine.api.resource.StorageDomainVmResource;
import org.ovirt.engine.api.resource.StorageDomainVmsResource;
import org.ovirt.engine.api.resource.StorageDomainsResource;
import org.ovirt.engine.api.resource.StorageResource;
import org.ovirt.engine.api.resource.StorageServerConnectionExtensionResource;
import org.ovirt.engine.api.resource.StorageServerConnectionExtensionsResource;
import org.ovirt.engine.api.resource.StorageServerConnectionResource;
import org.ovirt.engine.api.resource.StorageServerConnectionsResource;
import org.ovirt.engine.api.resource.SystemPermissionsResource;
import org.ovirt.engine.api.resource.SystemResource;
import org.ovirt.engine.api.resource.TagResource;
import org.ovirt.engine.api.resource.TagsResource;
import org.ovirt.engine.api.resource.TemplateCdromResource;
import org.ovirt.engine.api.resource.TemplateCdromsResource;
import org.ovirt.engine.api.resource.TemplateDiskResource;
import org.ovirt.engine.api.resource.TemplateDisksResource;
import org.ovirt.engine.api.resource.TemplateNicResource;
import org.ovirt.engine.api.resource.TemplateNicsResource;
import org.ovirt.engine.api.resource.TemplateResource;
import org.ovirt.engine.api.resource.TemplateWatchdogResource;
import org.ovirt.engine.api.resource.TemplateWatchdogsResource;
import org.ovirt.engine.api.resource.TemplatesResource;
import org.ovirt.engine.api.resource.UnmanagedNetworkResource;
import org.ovirt.engine.api.resource.UnmanagedNetworksResource;
import org.ovirt.engine.api.resource.VirtualFunctionAllowedNetworkResource;
import org.ovirt.engine.api.resource.VirtualFunctionAllowedNetworksResource;
import org.ovirt.engine.api.resource.VmApplicationResource;
import org.ovirt.engine.api.resource.VmApplicationsResource;
import org.ovirt.engine.api.resource.VmCdromResource;
import org.ovirt.engine.api.resource.VmCdromsResource;
import org.ovirt.engine.api.resource.VmDiskResource;
import org.ovirt.engine.api.resource.VmDisksResource;
import org.ovirt.engine.api.resource.VmGraphicsConsoleResource;
import org.ovirt.engine.api.resource.VmHostDeviceResource;
import org.ovirt.engine.api.resource.VmHostDevicesResource;
import org.ovirt.engine.api.resource.VmNicResource;
import org.ovirt.engine.api.resource.VmNicsResource;
import org.ovirt.engine.api.resource.VmNumaNodeResource;
import org.ovirt.engine.api.resource.VmNumaNodesResource;
import org.ovirt.engine.api.resource.VmPoolResource;
import org.ovirt.engine.api.resource.VmPoolsResource;
import org.ovirt.engine.api.resource.VmReportedDeviceResource;
import org.ovirt.engine.api.resource.VmReportedDevicesResource;
import org.ovirt.engine.api.resource.VmResource;
import org.ovirt.engine.api.resource.VmSessionResource;
import org.ovirt.engine.api.resource.VmSessionsResource;
import org.ovirt.engine.api.resource.VmWatchdogResource;
import org.ovirt.engine.api.resource.VmWatchdogsResource;
import org.ovirt.engine.api.resource.VmsResource;
import org.ovirt.engine.api.resource.VnicProfileResource;
import org.ovirt.engine.api.resource.VnicProfilesResource;
import org.ovirt.engine.api.resource.WeightResource;
import org.ovirt.engine.api.resource.WeightsResource;
import org.ovirt.engine.api.resource.aaa.DomainGroupResource;
import org.ovirt.engine.api.resource.aaa.DomainGroupsResource;
import org.ovirt.engine.api.resource.aaa.DomainResource;
import org.ovirt.engine.api.resource.aaa.DomainUserResource;
import org.ovirt.engine.api.resource.aaa.DomainUsersResource;
import org.ovirt.engine.api.resource.aaa.DomainsResource;
import org.ovirt.engine.api.resource.aaa.GroupResource;
import org.ovirt.engine.api.resource.aaa.GroupsResource;
import org.ovirt.engine.api.resource.aaa.SshPublicKeyResource;
import org.ovirt.engine.api.resource.aaa.SshPublicKeysResource;
import org.ovirt.engine.api.resource.aaa.UserResource;
import org.ovirt.engine.api.resource.aaa.UsersResource;
import org.ovirt.engine.api.resource.externalhostproviders.EngineKatelloErrataResource;
import org.ovirt.engine.api.resource.externalhostproviders.ExternalComputeResourceResource;
import org.ovirt.engine.api.resource.externalhostproviders.ExternalComputeResourcesResource;
import org.ovirt.engine.api.resource.externalhostproviders.ExternalDiscoveredHostResource;
import org.ovirt.engine.api.resource.externalhostproviders.ExternalDiscoveredHostsResource;
import org.ovirt.engine.api.resource.externalhostproviders.ExternalHostGroupResource;
import org.ovirt.engine.api.resource.externalhostproviders.ExternalHostGroupsResource;
import org.ovirt.engine.api.resource.externalhostproviders.ExternalHostProviderResource;
import org.ovirt.engine.api.resource.externalhostproviders.ExternalHostProvidersResource;
import org.ovirt.engine.api.resource.externalhostproviders.ExternalHostResource;
import org.ovirt.engine.api.resource.externalhostproviders.ExternalHostsResource;
import org.ovirt.engine.api.resource.externalhostproviders.KatelloErrataResource;
import org.ovirt.engine.api.resource.externalhostproviders.KatelloErratumResource;
import org.ovirt.engine.api.resource.gluster.GlusterBrickResource;
import org.ovirt.engine.api.resource.gluster.GlusterBricksResource;
import org.ovirt.engine.api.resource.gluster.GlusterHookResource;
import org.ovirt.engine.api.resource.gluster.GlusterHooksResource;
import org.ovirt.engine.api.resource.gluster.GlusterVolumeResource;
import org.ovirt.engine.api.resource.gluster.GlusterVolumesResource;
import org.ovirt.engine.api.resource.openstack.OpenstackImageProviderResource;
import org.ovirt.engine.api.resource.openstack.OpenstackImageProvidersResource;
import org.ovirt.engine.api.resource.openstack.OpenstackImageResource;
import org.ovirt.engine.api.resource.openstack.OpenstackImagesResource;
import org.ovirt.engine.api.resource.openstack.OpenstackNetworkProviderResource;
import org.ovirt.engine.api.resource.openstack.OpenstackNetworkProvidersResource;
import org.ovirt.engine.api.resource.openstack.OpenstackNetworkResource;
import org.ovirt.engine.api.resource.openstack.OpenstackNetworksResource;
import org.ovirt.engine.api.resource.openstack.OpenstackSubnetResource;
import org.ovirt.engine.api.resource.openstack.OpenstackSubnetsResource;
import org.ovirt.engine.api.resource.openstack.OpenstackVolumeAuthenticationKeyResource;
import org.ovirt.engine.api.resource.openstack.OpenstackVolumeAuthenticationKeysResource;
import org.ovirt.engine.api.resource.openstack.OpenstackVolumeProviderResource;
import org.ovirt.engine.api.resource.openstack.OpenstackVolumeProvidersResource;
import org.ovirt.engine.api.resource.openstack.OpenstackVolumeTypeResource;
import org.ovirt.engine.api.resource.openstack.OpenstackVolumeTypesResource;
import org.ovirt.engine.api.restapi.invocation.Current;
import org.ovirt.engine.api.restapi.invocation.CurrentManager;
import org.ovirt.engine.api.restapi.invocation.VersionSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Contains a static addLinks() method which constructs any href attributes
 * and action links required by a representation.
 *
 * The information used to build links is obtained from the annotations on
 * the API definition interfaces.

 * For example, a link to a VM is the combination of the @Path attribute on
 * VmsResource and the VM id - i.e. '/restapi-definition/vms/{vm_id}'
 *
 * Resource collections which are a sub-resource of a parent collection
 * present a more difficult challenge. For example, the link to a VM tag
 * is the combination of the @Path attribute on VmsResource, the VM id,
 * the @Path attribute on VmResource.getTagsResource() and the tag id -
 * i.e. '/restapi-definition/vms/{vm_id}/tags/{tag_id}'
 * In most cases the parent type may be computed, but in exceptional
 * cases there are a number of equally valid candidates. Disambiguation
 * is achieved via an explicit suggestedParentType parameter.
 *
 * To be able to do this we need, for each collection, the collection type
 * (e.g. AssignedTagsResource), the resource type (e.g. AssignedTagResource)
 * and the parent model type (e.g. VM). The TYPES map below is populated
 * with this information for every resource type.
 */
public class LinkHelper {
    private static final Logger log = LoggerFactory.getLogger(LinkHelper.class);

    private static final String SEARCH_RELATION = "/search";
    private static final String SEARCH_TEMPLATE = "?search={query}";
    private static final String MATRIX_PARAMETER_TEMPLATE = ";%s={%s}";

    /**
     * A constant representing the pseudo-parent of a top-level collection
     */
    private static final Class<? extends BaseResource> NO_PARENT = BaseResource.class;

    /**
     * A map describing every possible collection
     */
    private static EntityLocationMap TYPES = new EntityLocationMap();

    /**
     * A map for caching relevant resource methods for each class
     */
    private static ConcurrentMap<Class<?>, List<Method>> methodCache = new ConcurrentHashMap<>();

    /**
     * This class serves as a key to a map which stores values of 'Path' annotations
     * found in Service interfaces. A 'collection' service (e.g: VmsService) along
     * with a single-entity Service (e.g: VmService) identify a location on the API
     * tree, which may be associated with a value of a 'Path' annotation.
     */
    private static class PathKey {
        private Class<?> service;
        private Class<?> parentService;
        public PathKey(Class<?> service, Class<?> parentService) {
            super();
            this.service = service;
            this.parentService = parentService;
        }
        @Override
        public boolean equals(Object obj) {
            if (obj instanceof PathKey) {
                PathKey key = (PathKey)obj;
                return equals(service, key.service) && equals(parentService, key.parentService);
            } else {
                return false;
            }
        }
        private boolean equals(Class<?> class1, Class<?> class2) {
            return Objects.equals(class1, class2);
        }

        @Override
        public int hashCode() {
            if (service==null && parentService==null) {
                return 0;
            }
            if (service==null) {
                return parentService.hashCode();
            }
            if (parentService==null) {
                return service.hashCode();
            }
            return 997 * (service.hashCode()) ^ 991 * (parentService.hashCode()); //large primes!
        }
    }

    private static ConcurrentMap<PathKey, String> pathCache = new ConcurrentHashMap<>();

    static {
        LocationByParentMap map;

        map = new LocationByParentMap(TemplateCdromResource.class, TemplateCdromsResource.class, Template.class);
        map.add(VmCdromResource.class, VmCdromsResource.class, Vm.class);
        TYPES.put(Cdrom.class, map);

        map = new LocationByParentMap(GraphicsConsoleResource.class, GraphicsConsolesResource.class);
        map.add(VmGraphicsConsoleResource.class, GraphicsConsolesResource.class, Vm.class);
        map.add(GraphicsConsoleResource.class, GraphicsConsolesResource.class, Template.class);
        map.add(GraphicsConsoleResource.class, GraphicsConsolesResource.class, InstanceType.class);
        TYPES.put(GraphicsConsole.class, map);

        map = new LocationByParentMap(VmApplicationResource.class, VmApplicationsResource.class, Vm.class);
        TYPES.put(Application.class, map);

        map = new LocationByParentMap(VmReportedDeviceResource.class, VmReportedDevicesResource.class, Vm.class);
        TYPES.put(ReportedDevice.class, map);

        map = new LocationByParentMap(ClusterResource.class, ClustersResource.class);
        TYPES.put(Cluster.class, map);

        map = new LocationByParentMap(DataCenterResource.class, DataCentersResource.class);
        TYPES.put(DataCenter.class, map);

        map = new LocationByParentMap(MacPoolResource.class, MacPoolsResource.class);
        TYPES.put(MacPool.class, map);

        map = new LocationByParentMap(NetworkFilterResource.class, NetworkFiltersResource.class);
        TYPES.put(NetworkFilter.class, map);

        map = new LocationByParentMap(DiskResource.class, DisksResource.class);
        map.add(VmDiskResource.class, VmDisksResource.class, Vm.class);
        map.add(TemplateDiskResource.class, TemplateDisksResource.class, Template.class);
        TYPES.put(Disk.class, map);

        map = new LocationByParentMap(DiskSnapshotResource.class, DiskSnapshotsResource.class, StorageDomain.class);
        TYPES.put(DiskSnapshot.class, map);

        map = new LocationByParentMap(StorageServerConnectionExtensionResource.class, StorageServerConnectionExtensionsResource.class, Host.class);
        TYPES.put(StorageConnectionExtension.class, map);

        map = new LocationByParentMap(org.ovirt.engine.api.resource.HostResource.class, org.ovirt.engine.api.resource.HostsResource.class);
        map.add(AffinityLabelHostResource.class, AffinityLabelHostsResource.class, AffinityLabel.class);
        TYPES.put(Host.class, map);

        map = new LocationByParentMap(HostNicResource.class, HostNicsResource.class, Host.class);
        TYPES.put(HostNic.class, map);

        map = new LocationByParentMap(HostNumaNodeResource.class, HostNumaNodesResource.class, Host.class);
        TYPES.put(NumaNode.class, map);

        map = new LocationByParentMap(HostHookResource.class, HostHooksResource.class, Host.class);
        TYPES.put(Hook.class, map);

        map = new LocationByParentMap(FileResource.class, FilesResource.class, StorageDomain.class);
        TYPES.put(File.class, map);

        map = new LocationByParentMap(ImageResource.class, ImagesResource.class);
        map.add(ImageResource.class, ImagesResource.class, StorageDomain.class);
        TYPES.put(Image.class, map);

        map = new LocationByParentMap(GroupResource.class, GroupsResource.class);
        map.add(DomainGroupResource.class, DomainGroupsResource.class, Domain.class);
        TYPES.put(Group.class, map);

        map = new LocationByParentMap(PermissionResource.class, AssignedPermissionsResource.class, User.class);
        map.add(PermissionResource.class, AssignedPermissionsResource.class, Group.class);
        map.add(PermissionResource.class, AssignedPermissionsResource.class, Role.class);
        map.add(PermissionResource.class, AssignedPermissionsResource.class, Vm.class);
        map.add(PermissionResource.class, AssignedPermissionsResource.class, Disk.class);
        map.add(PermissionResource.class, SystemPermissionsResource.class, NO_PARENT);
        TYPES.put(Permission.class, map);

        map = new LocationByParentMap(NetworkResource.class, NetworksResource.class);
        map.add(AssignedNetworkResource.class, AssignedNetworksResource.class, Cluster.class);
        map.add(NetworkResource.class, NetworksResource.class, Network.class);
        map.add(VirtualFunctionAllowedNetworkResource.class, VirtualFunctionAllowedNetworksResource.class, HostNic.class);
        TYPES.put(Network.class, map);

        map = new LocationByParentMap();
        map.add(InstanceTypeNicResource.class, InstanceTypeNicsResource.class, InstanceType.class);
        map.add(TemplateNicResource.class, TemplateNicsResource.class, Template.class);
        map.add(VmNicResource.class, VmNicsResource.class, Vm.class);
        TYPES.put(Nic.class, map);

        map = new LocationByParentMap(VmNumaNodeResource.class, VmNumaNodesResource.class, Vm.class);
        TYPES.put(VirtualNumaNode.class, map);

        map = new LocationByParentMap(PermitResource.class, PermitsResource.class, Role.class);
        TYPES.put(Permit.class, map);

        map = new LocationByParentMap(RoleResource.class, RolesResource.class);
        map.add(RoleResource.class, AssignedRolesResource.class, User.class);
        TYPES.put(Role.class, map);

        map = new LocationByParentMap(SnapshotResource.class, SnapshotsResource.class, Vm.class);
        TYPES.put(Snapshot.class, map);

        map = new LocationByParentMap(StorageResource.class, HostStorageResource.class, Host.class);
        TYPES.put(HostStorage.class, map);

        map = new LocationByParentMap(StorageServerConnectionResource.class, StorageServerConnectionsResource.class);
        TYPES.put(StorageConnection.class, map);

        map = new LocationByParentMap(StorageDomainResource.class, StorageDomainsResource.class);
        map.add(AttachedStorageDomainResource.class, AttachedStorageDomainsResource.class, DataCenter.class);
        TYPES.put(StorageDomain.class, map);

        map = new LocationByParentMap(TagResource.class, TagsResource.class);
        map.add(AssignedTagResource.class, AssignedTagsResource.class, Host.class);
        map.add(AssignedTagResource.class, AssignedTagsResource.class, User.class);
        map.add(AssignedTagResource.class, AssignedTagsResource.class, Vm.class);
        map.add(AssignedTagResource.class, AssignedTagsResource.class, Template.class);
        map.add(AssignedTagResource.class, AssignedTagsResource.class, Group.class);
        TYPES.put(Tag.class, map);

        map = new LocationByParentMap(BookmarkResource.class, BookmarksResource.class);
        TYPES.put(Bookmark.class, map);

        map = new LocationByParentMap(IconResource.class, IconsResource.class);
        TYPES.put(Icon.class, map);

        map = new LocationByParentMap(TemplateResource.class, TemplatesResource.class);
        map.add(StorageDomainTemplateResource.class, StorageDomainTemplatesResource.class, StorageDomain.class);
        TYPES.put(Template.class, map);

        map = new LocationByParentMap(InstanceTypeResource.class, InstanceTypesResource.class);
        TYPES.put(InstanceType.class, map);

        map = new LocationByParentMap(UserResource.class, UsersResource.class);
        map.add(DomainUserResource.class, DomainUsersResource.class, Domain.class);
        TYPES.put(User.class, map);

        map = new LocationByParentMap(VmResource.class, VmsResource.class);
        map.add(StorageDomainVmResource.class, StorageDomainVmsResource.class, StorageDomain.class);
        map.add(AffinityLabelVmResource.class, AffinityLabelVmsResource.class, AffinityLabel.class);
        //        map.add(SnapshotResource.class, SnapshotsResource.class, Snapshot.class);
        TYPES.put(Vm.class, map);

        map = new LocationByParentMap(VmPoolResource.class, VmPoolsResource.class);
        TYPES.put(VmPool.class, map);

        map = new LocationByParentMap(EventResource.class, EventsResource.class);
        TYPES.put(Event.class, map);

        map = new LocationByParentMap(DomainResource.class, DomainsResource.class);
        TYPES.put(Domain.class, map);

        map = new LocationByParentMap(StatisticResource.class, StatisticsResource.class, Disk.class);
        map.add(StatisticResource.class, StatisticsResource.class, Host.class);
        map.add(StatisticResource.class, StatisticsResource.class, HostNic.class);
        map.add(StatisticResource.class, StatisticsResource.class, NumaNode.class);
        map.add(StatisticResource.class, StatisticsResource.class, Nic.class);
        map.add(StatisticResource.class, StatisticsResource.class, Vm.class);
        map.add(StatisticResource.class, StatisticsResource.class, GlusterBrick.class);
        TYPES.put(Statistic.class, map);

        map = new LocationByParentMap(QuotaResource.class, QuotasResource.class, DataCenter.class);
        TYPES.put(Quota.class, map);

        map = new LocationByParentMap(QuotaStorageLimitResource.class, QuotaStorageLimitsResource.class, Quota.class);
        TYPES.put(QuotaStorageLimit.class, map);
        map = new LocationByParentMap(QuotaClusterLimitResource.class, QuotaClusterLimitsResource.class, Quota.class);
        TYPES.put(QuotaClusterLimit.class, map);

        map = new LocationByParentMap(GlusterVolumeResource.class, GlusterVolumesResource.class, Cluster.class);
        TYPES.put(GlusterVolume.class, map);
        TYPES.put(GlusterVolumeProfileDetails.class, map);

        map = new LocationByParentMap(GlusterBrickResource.class, GlusterBricksResource.class, GlusterVolume.class);
        TYPES.put(GlusterBrick.class, map);

        map = new LocationByParentMap(GlusterHookResource.class, GlusterHooksResource.class, Cluster.class);
        TYPES.put(GlusterHook.class, map);

        map = new LocationByParentMap();
        map.add(InstanceTypeWatchdogResource.class, InstanceTypeWatchdogsResource.class, InstanceType.class);
        map.add(TemplateWatchdogResource.class, TemplateWatchdogsResource.class, Template.class);
        map.add(VmWatchdogResource.class, VmWatchdogsResource.class, Vm.class);
        TYPES.put(Watchdog.class, map);

        map = new LocationByParentMap(JobResource.class, JobsResource.class);
        TYPES.put(Job.class, map);

        map = new LocationByParentMap(StepResource.class, StepsResource.class, Job.class);
        TYPES.put(Step.class, map);

        map = new LocationByParentMap(VnicProfileResource.class, VnicProfilesResource.class);
        TYPES.put(VnicProfile.class, map);

        map = new LocationByParentMap(NetworkLabelResource.class, NetworkLabelsResource.class);
        map.add(NetworkLabelResource.class, NetworkLabelsResource.class, Network.class);
        map.add(NetworkLabelResource.class, NetworkLabelsResource.class, HostNic.class);
        TYPES.put(NetworkLabel.class, map);

        map = new LocationByParentMap(NetworkAttachmentResource.class, NetworkAttachmentsResource.class, Host.class);
        map.add(NetworkAttachmentResource.class, NetworkAttachmentsResource.class, HostNic.class);
        TYPES.put(NetworkAttachment.class, map);

        map = new LocationByParentMap(AffinityLabelResource.class, AffinityLabelsResource.class);
        map.add(AssignedAffinityLabelResource.class, AssignedAffinityLabelsResource.class, Vm.class);
        map.add(AssignedAffinityLabelResource.class, AssignedAffinityLabelsResource.class, Host.class);
        map.add(AffinityLabelResource.class, AffinityLabelsResource.class, NO_PARENT);
        TYPES.put(AffinityLabel.class, map);

        map = new LocationByParentMap(UnmanagedNetworkResource.class, UnmanagedNetworksResource.class, Host.class);
        TYPES.put(UnmanagedNetwork.class, map);

        map = new LocationByParentMap(AffinityGroupResource.class, AffinityGroupsResource.class, Cluster.class);
        TYPES.put(AffinityGroup.class, map);

        map = new LocationByParentMap(VmSessionResource.class, VmSessionsResource.class, Vm.class);
        TYPES.put(Session.class, map);

        map = new LocationByParentMap(HostDevice.class, HostDevices.class);
        map.add(HostDeviceResource.class, HostDevicesResource.class, Host.class);
        map.add(VmHostDeviceResource.class, VmHostDevicesResource.class, Vm.class);
        TYPES.put(HostDevice.class, map);

        map = new LocationByParentMap(SchedulingPolicyUnitResource.class, SchedulingPolicyUnitsResource.class);
        TYPES.put(SchedulingPolicyUnit.class, map);

        map = new LocationByParentMap(SchedulingPolicyResource.class, SchedulingPoliciesResource.class);
        TYPES.put(SchedulingPolicy.class, map);

        map = new LocationByParentMap(FilterResource.class, FiltersResource.class, SchedulingPolicy.class);
        TYPES.put(Filter.class, map);

        map = new LocationByParentMap(WeightResource.class, WeightsResource.class, SchedulingPolicy.class);
        TYPES.put(Weight.class, map);

        map = new LocationByParentMap(BalanceResource.class, BalancesResource.class, SchedulingPolicy.class);
        TYPES.put(Balance.class, map);

        map = new LocationByParentMap(QosResource.class, QossResource.class, DataCenter.class);
        map.add(QosResource.class, QossResource.class, Network.class);
        TYPES.put(Qos.class, map);

        map = new LocationByParentMap(IscsiBondResource.class, IscsiBondsResource.class, DataCenter.class);
        TYPES.put(IscsiBond.class, map);

        map = new LocationByParentMap(DiskProfileResource.class, DiskProfilesResource.class);
        TYPES.put(DiskProfile.class, map);

        map = new LocationByParentMap(CpuProfileResource.class, CpuProfilesResource.class);
        TYPES.put(CpuProfile.class, map);

        // Operating systems:
        map = new LocationByParentMap(OperatingSystemResource.class, OperatingSystemsResource.class);
        TYPES.put(OperatingSystemInfo.class, map);

        // External host providers:
        map = new LocationByParentMap(ExternalHostProviderResource.class, ExternalHostProvidersResource.class);
        TYPES.put(ExternalHostProvider.class, map);

        map = new LocationByParentMap(ExternalHostResource.class, ExternalHostsResource.class);
        map.add(ExternalHostResource.class, ExternalHostsResource.class, ExternalHostProvider.class);
        TYPES.put(ExternalHost.class, map);

        map = new LocationByParentMap(ExternalDiscoveredHostResource.class, ExternalHostsResource.class);
        map.add(ExternalDiscoveredHostResource.class, ExternalDiscoveredHostsResource.class, ExternalHostProvider.class);
        TYPES.put(ExternalDiscoveredHost.class, map);

        map = new LocationByParentMap(ExternalHostGroupResource.class, ExternalHostGroupsResource.class);
        map.add(ExternalHostGroupResource.class, ExternalHostGroupsResource.class, ExternalHostProvider.class);
        TYPES.put(ExternalHostGroup.class, map);

        map = new LocationByParentMap(ExternalComputeResourceResource.class, ExternalComputeResourcesResource.class);
        map.add(ExternalComputeResourceResource.class, ExternalComputeResourcesResource.class, ExternalHostProvider.class);
        TYPES.put(ExternalComputeResource.class, map);

        // OpenStack image providers:
        map = new LocationByParentMap(OpenstackImageProviderResource.class, OpenstackImageProvidersResource.class);
        TYPES.put(OpenStackImageProvider.class, map);

        map = new LocationByParentMap(OpenstackImageResource.class, OpenstackImagesResource.class);
        map.add(OpenstackImageResource.class, OpenstackImagesResource.class, OpenStackImageProvider.class);
        TYPES.put(OpenStackImage.class, map);

        // OpenStack volume providers:
        map = new LocationByParentMap(OpenstackVolumeProviderResource.class, OpenstackVolumeProvidersResource.class);
        TYPES.put(OpenStackVolumeProvider.class, map);

        map = new LocationByParentMap(OpenstackVolumeTypeResource.class, OpenstackVolumeTypesResource.class);
        map.add(OpenstackVolumeTypeResource.class, OpenstackVolumeTypesResource.class, OpenStackVolumeProvider.class);
        TYPES.put(OpenStackVolumeType.class, map);

        map = new LocationByParentMap(OpenstackVolumeAuthenticationKeyResource.class, OpenstackVolumeAuthenticationKeysResource.class);
        map.add(OpenstackVolumeAuthenticationKeyResource.class, OpenstackVolumeAuthenticationKeysResource.class, OpenStackVolumeProvider.class);
        TYPES.put(OpenstackVolumeAuthenticationKey.class, map);

        // OpenStack network providers:
        map = new LocationByParentMap(OpenstackNetworkProviderResource.class, OpenstackNetworkProvidersResource.class);
        TYPES.put(OpenStackNetworkProvider.class, map);

        map = new LocationByParentMap(OpenstackNetworkResource.class, OpenstackNetworksResource.class);
        map.add(OpenstackNetworkResource.class, OpenstackNetworksResource.class, OpenStackNetworkProvider.class);
        TYPES.put(OpenStackNetwork.class, map);

        map = new LocationByParentMap(OpenstackSubnetResource.class, OpenstackSubnetsResource.class);
        map.add(OpenstackSubnetResource.class, OpenstackSubnetsResource.class, OpenStackNetwork.class);
        TYPES.put(OpenStackSubnet.class, map);

        map = new LocationByParentMap(FenceAgentResource.class, FenceAgentsResource.class, Host.class);
        TYPES.put(Agent.class, map);

        map = new LocationByParentMap(KatelloErratumResource.class, KatelloErrataResource.class, Host.class);
        map.add(KatelloErratumResource.class, KatelloErrataResource.class, Vm.class);
        map.add(KatelloErratumResource.class, EngineKatelloErrataResource.class, NO_PARENT);
        TYPES.put(KatelloErratum.class, map);

        map = new LocationByParentMap();
        map.add(SshPublicKeyResource.class, SshPublicKeysResource.class, User.class);
        TYPES.put(SshPublicKey.class, map);

        map = new LocationByParentMap();
        map.add(ClusterLevelResource.class, ClusterLevelsResource.class, NO_PARENT);
        TYPES.put(ClusterLevel.class, map);

        map = new LocationByParentMap();
        map.add(DiskAttachmentResource.class, DiskAttachmentsResource.class, Vm.class);
        TYPES.put(DiskAttachment.class, map);
    }

    /**
     * Obtain the relative path to a top-level collection
     *
     * The path is the value of the {@link Path} annotation on resource locator method of the root resource that
     * returns a reference to this class of resource. For example, if the class is {@link BookmarksResource} then
     * returned value should be the value of the {@link Path} annotation on the
     * {@link SystemResource#getBookmarksResource()} method.
     *
     * @param service the collection resource type
     * @return the relative path to the collection
     */
    private static String getRelativePath(Class<?> service) {
        return getRelativePath(service, SystemResource.class);
    }

    /**
     * Obtain the relative path to a sub-collection.
     *
     * The path is obtained from the @Path annotation on the method on @parent
     * which returns an instance of @clz.
     *
     * @param service    the collection resource type (e.g. AssignedTagsResource)
     * @param parentService the parent resource type (e.g. VmResource)
     * @return       the relative path to the collection
     */
    private static String getRelativePath(Class<?> service, Class<?> parentService) {
        PathKey key = new PathKey(service, parentService);
        String path = pathCache.get(key);
        if (path!=null) {
            return path;
        }
        else {
            for (Method method : parentService.getMethods()) {
                if (method.getName().startsWith("get") && method.getReturnType() == service) {
                    Path pathAnnotation = method.getAnnotation(Path.class);
                    if (pathAnnotation != null) {
                        pathCache.put(key, pathAnnotation.value());
                        return pathAnnotation.value();
                    }
                }
            }
        }
        log.error("Can't find relative path for class \"" + service.getName() + "\", will return null");
        return null;
    }

    /**
     * Obtain a set of inline BaseResource objects from @obj
     *
     * i.e. return the value of any properties on @obj which are a
     * sub-type of BaseResource
     *
     * @param obj the object to check
     * @return    a list of any inline BaseResource objects
     */
    private static List<BaseResource> getInlineResources(Object obj) {
        ArrayList<BaseResource> ret = new ArrayList<>();

        for (Method method : getRelevantMethods(obj.getClass())) {
            // We need to recursively scan everything that is in the model package, as there may be references
            // to resources deeply nested:
            Object inline = null;
            try {
                inline = method.invoke(obj);
            } catch (Exception e) {
                // invocation target exception should not occur on simple getter
            }
            if (inline != null) {
                if (inline instanceof BaseResource) {
                    ret.add((BaseResource) inline);
                }
                else {
                    ret.addAll(getInlineResources(inline));
                }
            }
        }
        return ret;
    }

    /**
     * Gets all the relevant possible inline resources methods of a class. Data is cached for future use.
     * @param clz
     *            The class to examine
     * @return The list of relevant methods.
     */
    private static List<Method> getRelevantMethods(Class<?> clz) {
        List<Method> methods = methodCache.get(clz);
        if (methods == null) {
            methods = new ArrayList<>();
            for (Method method : clz.getMethods()) {
                if (method.getName().startsWith("get")) {
                    if (method.getReturnType().getPackage() == BaseResource.class.getPackage()) {
                        methods.add(method);
                    }
                }
            }
            methodCache.put(clz, methods);
        }

        return methods;
    }
    /**
     * Unset the property on @model of type @type
     *
     * @param model the object with the property to unset
     * @param type  the type of the property
     */
    private static void unsetInlineResource(BaseResource model, Class<?> type) {
        for (Method method : model.getClass().getMethods()) {
            if (method.getName().startsWith("set")) {
                try {
                    if (type.isAssignableFrom(method.getParameterTypes()[0])) {
                        method.invoke(model, new Object[]{null});
                        return;
                    }
                } catch (Exception e) {
                    // invocation target exception should not occur on simple setter
                }
            }
        }
    }

    /**
     * Return any parent object set on @model
     *
     * i.e. return the value of any bean property whose type matches @parentType
     *
     * @param model      object to check
     * @param parentType the type of the parent
     * @return           the parent object, or null if not set
     */
    private static <R extends BaseResource> BaseResource getParent(R model, Class<?> parentType) {
        for (Method method : getRelevantMethods(model.getClass())) {
            try {
                Object potentialParent = method.invoke(model);
                if (potentialParent != null && parentType.isAssignableFrom(potentialParent.getClass())) {
                    return (BaseResource)potentialParent;
                }
            } catch (Exception e) {
                log.error("Error invoking method when adding links to an API entity", e);
                continue;
            }
        }
        return null;
    }

    /**
     * Lookup the #Collection instance which represents this object
     *
     * i.e. for a VM tag (i.e. a Tag object which its VM property set)
     * return the #Collection instance which encapsulates AssignedTagResource,
     * AssignedTagsResource and VM.
     *
     * @param model the object to query for
     * @return      the #Collection instance representing the object's collection
     */
    private static ApiLocationMetadata getCollection(BaseResource model) {
        return getLocationMetadata(model, null);
    }

    /**
     * Lookup the #Collection instance which represents this object
     *
     * i.e. for a VM tag (i.e. a Tag object which its VM property set)
     * return the #Collection instance which encapsulates AssignedTagResource,
     * AssignedTagsResource and VM.
     *
     * @param model                the object to query for
     * @param suggestedParentType  the suggested parent type
     * @return                     the #Collection instance representing the object's collection
     */
    private static ApiLocationMetadata getLocationMetadata(BaseResource model, Class<? extends BaseResource> suggestedParentType) {
        LocationByParentMap locationByParentMap = TYPES.get(model.getClass());

        if (locationByParentMap == null) {
            return null;
        }

        if (suggestedParentType != null && locationByParentMap.containsKey(suggestedParentType)) {
            return locationByParentMap.get(suggestedParentType);
        }

        for (Entry<Class<? extends BaseResource>, ApiLocationMetadata> entry : locationByParentMap.entrySet()) {
            if (entry.getKey() != NO_PARENT &&
                getParent(model, entry.getKey()) != null) {
                return entry.getValue();
            }
        }

        return locationByParentMap.get(NO_PARENT);
    }

    private static ApiLocationMetadata getLocationMetadata(BaseResource model) {
        return getLocationMetadata(model, null);
    }
    /**
     * Computes the path for the given object. For example, for a tag of a virtual machine returns the path
     * {@code /ovirt-engine/api/vms/{vm:id}/tags/{tag:id}}.
     *
     * @param object the object
     * @return the path for the object, or {@code null} if the path can't be determined
     */
    public static String getPath(BaseResource object) {
        return getPath(object, null);
    }

    /**
     * Computes the path for the given object, using the given type to find out what is the type of the parent.
     *
     * @param entity the object
     * @param suggestedParentType the suggested parent type
     * @return the path for the object, or {@code null} if the path can't be determined
     */
    public static String getPath(BaseResource entity, Class<? extends BaseResource> suggestedParentType) {
        ApiLocationMetadata locationMetadata = getLocationMetadata(entity, suggestedParentType);
        if (locationMetadata != null) {
            if (locationMetadata.getParentType() != NO_PARENT) {
                return getPathConsideringParent(entity, locationMetadata);
            } else {
                return getPathWithoutParent(entity, locationMetadata);
            }
        } else {
            return null;
        }
    }

    private static String getPathWithoutParent(BaseResource entity, ApiLocationMetadata locationMetadata) {
        Current current = CurrentManager.get();
        StringBuilder buffer = new StringBuilder();
        buffer.append(current.getPrefix());
        if (current.getVersionSource() == VersionSource.URL) {
            buffer.append("/v");
            buffer.append(current.getVersion());
        }
        buffer.append("/");
        buffer.append(getRelativePath(locationMetadata.getCollectionServiceClass()));
        buffer.append("/");
        buffer.append(entity.getId());
        return buffer.toString();
    }

    private static String getPathConsideringParent(BaseResource entity, ApiLocationMetadata locationMetadata) {
        BaseResource parent = getParent(entity, locationMetadata.getParentType());
        if (parent == null) {
            return null;
        }
        ApiLocationMetadata parentLocationMetadata = getLocationMetadata(parent);
        if (parentLocationMetadata == null) {
            return null;
        }
        String parentPath = getPath(parent);
        if (parentPath == null) {
            return null;
        }
        String relativePath = getRelativePath(locationMetadata.getCollectionServiceClass(), parentLocationMetadata.getEntityServiceClass());
        return String.join("/", parentPath, relativePath, entity.getId());
    }

    /**
     * Set the href attribute on the supplied object
     *
     * e.g. set href = '/restapi-definition/vms/{vm_id}/tags/{tag_id}' on a VM tag
     *
     * @param model the object
     * @param suggestedParentType  the suggested parent type
     */
    private static void setHref(BaseResource model, String path) {
        if (path != null) {
            model.setHref(path);
        }
    }

    /**
     * Construct the set of action links for an object
     *
     * @param model   the object
     * @param suggestedParentType  the suggested parent type
     */
    private static void setActions(BaseResource model, String path) {
        ApiLocationMetadata collection = getCollection(model);
        if (collection != null) {
            ActionsBuilder actionsBuilder = new ActionsBuilder(path, collection.getEntityServiceClass());
            model.setActions(actionsBuilder.build());
        }
    }

    /**
     * Adds the set of action links for an object
     *
     * @param model the object to add actions to
     * @param collection the object to get implemented methods from
     */
    public static <R extends ActionableResource> void addActions(R model, Object collection) {
        Current current = CurrentManager.get();
        String base = current.getPrefix() + current.getPath();
        if (base != null) {
            ActionsBuilder actionsBuilder = new ActionsBuilder(base, model.getClass(), collection.getClass());
            model.setActions(actionsBuilder.build());
        }
    }

    /**
     * Set the href attribute on the object (and its inline objects)
     * and construct its set of action links
     *
     * @param model the object
     * @return the object, with href attributes and action links
     */
    public static <R extends BaseResource> R addLinks(R model) {
        return addLinks(model, null);
    }

    public static <R extends BaseResource> R addLinks(R model, Class<? extends BaseResource> suggestedParentType) {
        return addLinks(model, suggestedParentType, true);
    }

    public static <R extends BaseResource> R addLinks(R model, Class<? extends BaseResource> suggestedParentType, boolean addActions) {
        String path = getPath(model, suggestedParentType);
        if (path != null) {
            model.setHref(path);
            if (addActions) {
                setActions(model, path);
            }
        }
        for (BaseResource inline : getInlineResources(model)) {
            if (inline.getId() != null) {
                path = getPath(inline, null);
                if (path!=null) {
                    inline.setHref(path);
                }
            }
            for (BaseResource grandParent : getInlineResources(inline)) {
                unsetInlineResource(inline, grandParent.getClass());
            }
        }
        return model;
    }

    /**
     * A #Map sub-class which holds location meta-data by API entity.
     * For efficient access each entity contains its metadata objects in a map,
     * with parent-type as key. For example, the following is an entry in
     * EntityLocationMap for the entity 'Group':
     *
     * -------------------------------------------------
     * Group:
     *   NO_PARENT:
     *      parent: NO_PARENT
     *      resource_single    : GroupResource
     *      resource_collection: GroupsResource
     *   Domain:
     *      parent: Domain
     *      resource_single    : DomainGroupResource
     *      resource_collection: DomainGroupsResource
     * -------------------------------------------------
     *
     * Out of which the following are entries in LocationByParentMap:
     *
     *--------------------------------------------------
     * NO_PARENT:
     *    parent: NO_PARENT
     *    resource_single    : GroupResource
     *    resource_collection: GroupsResource
     *--------------------------------------------------
     *--------------------------------------------------
     * Domain:
     *    parent: Domain
     *    resource_single    : DomainGroupResource
     *    resource_collection: DomainGroupsResource
     *--------------------------------------------------
     *
     * Out of which the following are instances of ApiLocationMetadata:
     *
     *--------------------------------------------------
     * parent: NO_PARENT
     * resource_single    : GroupResource
     * resource_collection: GroupsResource
     *--------------------------------------------------
     *--------------------------------------------------
     * parent: Domain
     * resource_single    : DomainGroupResource
     * resource_collection: DomainGroupsResource
     *--------------------------------------------------
     */
    private static class EntityLocationMap extends HashMap<Class<? extends BaseResource>, LocationByParentMap> {}


    /**
     * A map which holds entity location meta-data according to parent-type.
     * This is a utility map, which exists only for performance reasons, and is always
     * used in a broader context. An instance of this map represents location
     * metadata for a specific entity, but the entity-type is not saved within the map itself,
     * meaning that looking at an instance of LocationByParentMap without the context in which
     * it was created, one could not tell which entity the map describes.
     */
    private static class LocationByParentMap extends LinkedHashMap<Class<? extends BaseResource>, ApiLocationMetadata> {
        public LocationByParentMap() {
            super();
        }

        public LocationByParentMap(Class<?> serviceClass,
                                     Class<?> collectionClass,
                                     Class<? extends BaseResource> parentType) {
            super();
            add(serviceClass, collectionClass, parentType);
        }

        public LocationByParentMap(Class<?> resourceType,
                                     Class<?> collectionType) {
            this(resourceType, collectionType, NO_PARENT);
        }

        public void add(Class<?> resourceType,
                        Class<?> collectionType,
                        Class<? extends BaseResource> parentType) {
            put(parentType, new ApiLocationMetadata(resourceType, collectionType, parentType));
        }
    }

    /**
     * A container of meta-data for a location in the API tree:
     * 1) the Service class which handles single entities in this location.
     * 2) the Service class which handles the collection of entities in this location.
     * 3) the parent-type of entities in this location (if any).
     * e.g: for VMs in root: VmResource, VmsResource, parentType=null.
     *      for VM-tags: AssignedTagResource, AssignedTagsResource, parentType=VM.
     */
    private static class ApiLocationMetadata {
        private final Class<?> entityServiceClass;
        private final Class<?> collectionServiceClass;
        private final Class<?> parentType;

        public ApiLocationMetadata(Class<?> entityServiceClass, Class<?> collectionServiceClass, Class<?> parentType) {
            this.entityServiceClass = entityServiceClass;
            this.collectionServiceClass = collectionServiceClass;
            this.parentType = parentType;
        }

        public Class<?> getEntityServiceClass() {
            return entityServiceClass;
        }

        public Class<?> getCollectionServiceClass() {
            return collectionServiceClass;
        }

        public Class<?> getParentType() {
            return parentType;
        }
    }
}
