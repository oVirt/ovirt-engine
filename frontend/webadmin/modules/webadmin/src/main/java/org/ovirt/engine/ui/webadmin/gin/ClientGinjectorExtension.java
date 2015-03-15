package org.ovirt.engine.ui.webadmin.gin;

import java.util.Map;

import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.core.common.businessentities.EngineSession;
import org.ovirt.engine.core.common.businessentities.IscsiBond;
import org.ovirt.engine.core.common.businessentities.Permissions;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.QuotaStorage;
import org.ovirt.engine.core.common.businessentities.QuotaVdsGroup;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmPool;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.event_subscriber;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterGeoRepSession;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterHookEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterServerService;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeOptionEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeSnapshotEntity;
import org.ovirt.engine.core.common.businessentities.gluster.StorageDevice;
import org.ovirt.engine.core.common.businessentities.network.ExternalSubnet;
import org.ovirt.engine.core.common.businessentities.network.HostNetworkQos;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.common.businessentities.network.NetworkQoS;
import org.ovirt.engine.core.common.businessentities.network.NetworkView;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VnicProfileView;
import org.ovirt.engine.core.common.businessentities.profiles.CpuProfile;
import org.ovirt.engine.core.common.businessentities.profiles.DiskProfile;
import org.ovirt.engine.core.common.businessentities.qos.CpuQos;
import org.ovirt.engine.core.common.businessentities.qos.StorageQos;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.RepoImage;
import org.ovirt.engine.core.common.scheduling.AffinityGroup;
import org.ovirt.engine.core.common.utils.PairQueryable;
import org.ovirt.engine.ui.common.auth.LoggedInGatekeeper;
import org.ovirt.engine.ui.common.uicommon.model.DetailModelProvider;
import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.uicommonweb.models.SessionListModel;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterGeneralModel;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterGlusterHookListModel;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterHostListModel;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterListModel;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterNetworkListModel;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterServiceModel;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterVmListModel;
import org.ovirt.engine.ui.uicommonweb.models.configure.PermissionListModel;
import org.ovirt.engine.ui.uicommonweb.models.configure.scheduling.affinity_groups.list.ClusterAffinityGroupListModel;
import org.ovirt.engine.ui.uicommonweb.models.configure.scheduling.affinity_groups.list.VmAffinityGroupListModel;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.DataCenterClusterListModel;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.DataCenterEventListModel;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.DataCenterIscsiBondListModel;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.DataCenterListModel;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.DataCenterNetworkListModel;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.DataCenterNetworkQoSListModel;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.DataCenterQuotaListModel;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.DataCenterStorageListModel;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.qos.DataCenterCpuQosListModel;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.qos.DataCenterHostNetworkQosListModel;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.qos.DataCenterStorageQosListModel;
import org.ovirt.engine.ui.uicommonweb.models.disks.DiskGeneralModel;
import org.ovirt.engine.ui.uicommonweb.models.disks.DiskListModel;
import org.ovirt.engine.ui.uicommonweb.models.disks.DiskStorageListModel;
import org.ovirt.engine.ui.uicommonweb.models.disks.DiskTemplateListModel;
import org.ovirt.engine.ui.uicommonweb.models.disks.DiskVmListModel;
import org.ovirt.engine.ui.uicommonweb.models.events.EventListModel;
import org.ovirt.engine.ui.uicommonweb.models.gluster.HostGlusterSwiftListModel;
import org.ovirt.engine.ui.uicommonweb.models.gluster.HostGlusterStorageDevicesListModel;
import org.ovirt.engine.ui.uicommonweb.models.gluster.VolumeBrickListModel;
import org.ovirt.engine.ui.uicommonweb.models.gluster.VolumeEventListModel;
import org.ovirt.engine.ui.uicommonweb.models.gluster.VolumeGeneralModel;
import org.ovirt.engine.ui.uicommonweb.models.gluster.VolumeGeoRepListModel;
import org.ovirt.engine.ui.uicommonweb.models.gluster.VolumeParameterListModel;
import org.ovirt.engine.ui.uicommonweb.models.gluster.GlusterVolumeSnapshotListModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostBricksListModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostEventListModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostGeneralModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostHardwareGeneralModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostHooksListModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostInterfaceLineModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostInterfaceListModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostListModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostVmListModel;
import org.ovirt.engine.ui.uicommonweb.models.networks.NetworkClusterListModel;
import org.ovirt.engine.ui.uicommonweb.models.networks.NetworkExternalSubnetListModel;
import org.ovirt.engine.ui.uicommonweb.models.networks.NetworkGeneralModel;
import org.ovirt.engine.ui.uicommonweb.models.networks.NetworkHostListModel;
import org.ovirt.engine.ui.uicommonweb.models.networks.NetworkListModel;
import org.ovirt.engine.ui.uicommonweb.models.networks.NetworkProfileListModel;
import org.ovirt.engine.ui.uicommonweb.models.networks.NetworkTemplateListModel;
import org.ovirt.engine.ui.uicommonweb.models.networks.NetworkVmListModel;
import org.ovirt.engine.ui.uicommonweb.models.pools.PoolGeneralModel;
import org.ovirt.engine.ui.uicommonweb.models.pools.PoolListModel;
import org.ovirt.engine.ui.uicommonweb.models.pools.PoolVmListModel;
import org.ovirt.engine.ui.uicommonweb.models.profiles.CpuProfileListModel;
import org.ovirt.engine.ui.uicommonweb.models.profiles.DiskProfileListModel;
import org.ovirt.engine.ui.uicommonweb.models.profiles.VnicProfileListModel;
import org.ovirt.engine.ui.uicommonweb.models.profiles.VnicProfileTemplateListModel;
import org.ovirt.engine.ui.uicommonweb.models.profiles.VnicProfileVmListModel;
import org.ovirt.engine.ui.uicommonweb.models.providers.ProviderGeneralModel;
import org.ovirt.engine.ui.uicommonweb.models.providers.ProviderListModel;
import org.ovirt.engine.ui.uicommonweb.models.providers.ProviderNetworkListModel;
import org.ovirt.engine.ui.uicommonweb.models.quota.QuotaClusterListModel;
import org.ovirt.engine.ui.uicommonweb.models.quota.QuotaEventListModel;
import org.ovirt.engine.ui.uicommonweb.models.quota.QuotaListModel;
import org.ovirt.engine.ui.uicommonweb.models.quota.QuotaPermissionListModel;
import org.ovirt.engine.ui.uicommonweb.models.quota.QuotaStorageListModel;
import org.ovirt.engine.ui.uicommonweb.models.quota.QuotaTemplateListModel;
import org.ovirt.engine.ui.uicommonweb.models.quota.QuotaUserListModel;
import org.ovirt.engine.ui.uicommonweb.models.quota.QuotaVmListModel;
import org.ovirt.engine.ui.uicommonweb.models.reports.ReportsListModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageDataCenterListModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageDiskListModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageEventListModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageGeneralModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageIsoListModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageListModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageRegisterTemplateListModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageRegisterVmListModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageSnapshotListModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageTemplateListModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageVmListModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.TemplateBackupModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.VmBackupModel;
import org.ovirt.engine.ui.uicommonweb.models.templates.TemplateDiskListModel;
import org.ovirt.engine.ui.uicommonweb.models.templates.TemplateEventListModel;
import org.ovirt.engine.ui.uicommonweb.models.templates.TemplateGeneralModel;
import org.ovirt.engine.ui.uicommonweb.models.templates.TemplateInterfaceListModel;
import org.ovirt.engine.ui.uicommonweb.models.templates.TemplateListModel;
import org.ovirt.engine.ui.uicommonweb.models.templates.TemplateStorageListModel;
import org.ovirt.engine.ui.uicommonweb.models.templates.TemplateVmListModel;
import org.ovirt.engine.ui.uicommonweb.models.users.UserEventListModel;
import org.ovirt.engine.ui.uicommonweb.models.users.UserEventNotifierListModel;
import org.ovirt.engine.ui.uicommonweb.models.users.UserGeneralModel;
import org.ovirt.engine.ui.uicommonweb.models.users.UserGroup;
import org.ovirt.engine.ui.uicommonweb.models.users.UserGroupListModel;
import org.ovirt.engine.ui.uicommonweb.models.users.UserListModel;
import org.ovirt.engine.ui.uicommonweb.models.users.UserPermissionListModel;
import org.ovirt.engine.ui.uicommonweb.models.users.UserQuotaListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.DiskModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmAppListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmDiskListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmEventListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmGeneralModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmInterfaceListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmSessionsModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmSnapshotListModel;
import org.ovirt.engine.ui.uicommonweb.models.volumes.VolumeListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;

import com.gwtplatform.mvp.client.annotations.DefaultGatekeeper;

/**
 * WebAdmin {@code Ginjector} extension interface.
 */
public interface ClientGinjectorExtension extends WebAdminGinUiBinderWidgets {

    // Core GWTP components

    @DefaultGatekeeper
    LoggedInGatekeeper getDefaultGatekeeper();

    // Application-level components

    ApplicationConstants getApplicationConstants();

    // UiCommon model providers: Main tabs

    MainModelProvider<StoragePool, DataCenterListModel> getMainTabDataCenterModelProvider();

    MainModelProvider<VDSGroup, ClusterListModel<Void>> getMainTabClusterModelProvider();

    MainModelProvider<VDS, HostListModel<Void>> getMainTabHostModelProvider();

    MainModelProvider<NetworkView, NetworkListModel> getMainTabNetworkModelProvider();

    MainModelProvider<VnicProfileView, VnicProfileListModel> getMainTabVnicProfileModelProvider();

    MainModelProvider<org.ovirt.engine.core.common.businessentities.Provider, ProviderListModel> getMainTabProviderModelProvider();

    MainModelProvider<StorageDomain, StorageListModel> getMainTabStorageModelProvider();

    MainModelProvider<VM, VmListModel<Void>> getMainTabVirtualMachineModelProvider();

    MainModelProvider<VmPool, PoolListModel> getMainTabPoolModelProvider();

    MainModelProvider<VmTemplate, TemplateListModel> getMainTabTemplateModelProvider();

    MainModelProvider<DbUser, UserListModel> getMainTabUserModelProvider();

    MainModelProvider<AuditLog, EventListModel> getMainTabEventModelProvider();

    MainModelProvider<Void, ReportsListModel> getMainTabReportsModelProvider();

    MainModelProvider<Quota, QuotaListModel> getMainTabQuotaModelProvider();

    MainModelProvider<Disk, DiskListModel> getMainTabDiskModelProvider();

    MainModelProvider<GlusterVolumeEntity, VolumeListModel> getMainTabVolumeModelProvider();

    MainModelProvider<EngineSession, SessionListModel> getMainTabSessionModelProvider();

    // UiCommon model providers: Sub tabs

    // DataCenter

    SearchableDetailModelProvider<StorageDomain, DataCenterListModel, DataCenterStorageListModel> getSubTabDataCenterStorageModelProvider();

    SearchableDetailModelProvider<Network, DataCenterListModel, DataCenterNetworkListModel> getSubTabDataCenterNetworkModelProvider();

    SearchableDetailModelProvider<IscsiBond, DataCenterListModel, DataCenterIscsiBondListModel> getSubTabDataCenterIscsiBondModelProvider();

    SearchableDetailModelProvider<VDSGroup, DataCenterListModel, DataCenterClusterListModel> getSubTabDataCenterClusterModelProvider();

    SearchableDetailModelProvider<Quota, DataCenterListModel, DataCenterQuotaListModel> getSubTabDataCenterQuotaModelProvider();

    SearchableDetailModelProvider<NetworkQoS, DataCenterListModel, DataCenterNetworkQoSListModel> getSubTabDataCenterNetworkQoSModelProvider();

    SearchableDetailModelProvider<HostNetworkQos, DataCenterListModel, DataCenterHostNetworkQosListModel> getSubTabDataCenterHostNetworkQosModelProvider();

    SearchableDetailModelProvider<StorageQos, DataCenterListModel, DataCenterStorageQosListModel> getSubTabDataCenterStorageQosModelProvider();

    SearchableDetailModelProvider<CpuQos, DataCenterListModel, DataCenterCpuQosListModel> getSubTabDataCenterCpuQosModelProvider();

    SearchableDetailModelProvider<Permissions, DataCenterListModel, PermissionListModel<StoragePool>> getSubTabDataCenterPermissionModelProvider();

    SearchableDetailModelProvider<AuditLog, DataCenterListModel, DataCenterEventListModel> getSubTabDataCenterEventModelProvider();

    // Storage

    DetailModelProvider<StorageListModel, StorageGeneralModel> getSubTabStorageGeneralModelProvider();

    SearchableDetailModelProvider<StorageDomain, StorageListModel, StorageDataCenterListModel> getSubTabStorageDataCenterModelProvider();

    SearchableDetailModelProvider<VM, StorageListModel, VmBackupModel> getSubTabStorageVmBackupModelProvider();

    SearchableDetailModelProvider<VmTemplate, StorageListModel, TemplateBackupModel> getSubTabStorageTemplateBackupModelProvider();

    SearchableDetailModelProvider<VM, StorageListModel, StorageRegisterVmListModel> getSubTabStorageRegisterVmModelProvider();

    SearchableDetailModelProvider<VmTemplate, StorageListModel, StorageRegisterTemplateListModel> getSubTabStorageRegisterTemplateModelProvider();

    SearchableDetailModelProvider<VM, StorageListModel, StorageVmListModel> getSubTabStorageVmModelProvider();

    SearchableDetailModelProvider<VmTemplate, StorageListModel, StorageTemplateListModel> getSubTabStorageTemplateModelProvider();

    SearchableDetailModelProvider<RepoImage, StorageListModel, StorageIsoListModel> getSubTabStorageIsoModelProvider();

    SearchableDetailModelProvider<Disk, StorageListModel, StorageDiskListModel> getSubTabStorageDiskModelProvider();

    SearchableDetailModelProvider<Disk, StorageListModel, StorageSnapshotListModel> getSubTabStorageSnapshotModelProvider();

    SearchableDetailModelProvider<Permissions, StorageListModel, PermissionListModel<StorageDomain>> getSubTabStoragePermissionModelProvider();

    SearchableDetailModelProvider<AuditLog, StorageListModel, StorageEventListModel> getSubTabStorageEventModelProvider();

    SearchableDetailModelProvider<DiskProfile, StorageListModel, DiskProfileListModel> getSubTabStorageDiskProfileModelProvider();

    // Cluster

    DetailModelProvider<ClusterListModel<Void>, ClusterGeneralModel> getSubTabClusterGeneralModelProvider();

    SearchableDetailModelProvider<VDS, ClusterListModel<Void>, ClusterHostListModel> getSubTabClusterHostModelProvider();

    SearchableDetailModelProvider<VM, ClusterListModel<Void>, ClusterVmListModel> getSubTabClusterVmModelProvider();

    SearchableDetailModelProvider<Network, ClusterListModel<Void>, ClusterNetworkListModel> getSubTabClusterNetworkModelProvider();

    DetailModelProvider<ClusterListModel<Void>, ClusterServiceModel> getSubTabClusterServiceModelProvider();

    SearchableDetailModelProvider<AffinityGroup, ClusterListModel<Void>, ClusterAffinityGroupListModel> getSubTabClusterAffinityGroupModelProvider();

    SearchableDetailModelProvider<GlusterHookEntity, ClusterListModel<Void>, ClusterGlusterHookListModel> getSubTabClusterGlusterHookModelProvider();

    SearchableDetailModelProvider<Permissions, ClusterListModel<Void>, PermissionListModel<VDSGroup>> getSubTabClusterPermissionModelProvider();

    SearchableDetailModelProvider<CpuProfile, ClusterListModel<Void>, CpuProfileListModel> getSubTabClusterCpuProfileModelProvider();

    // VirtualMachine

    DetailModelProvider<VmListModel<Void>, VmGeneralModel> getSubTabVirtualMachineGeneralModelProvider();

    SearchableDetailModelProvider<VmNetworkInterface, VmListModel<Void>, VmInterfaceListModel> getSubTabVirtualMachineNetworkInterfaceModelProvider();

    SearchableDetailModelProvider<Disk, VmListModel<Void>, VmDiskListModel> getSubTabVirtualMachineVirtualDiskModelProvider();

    SearchableDetailModelProvider<Snapshot, VmListModel<Void>, VmSnapshotListModel> getSubTabVirtualMachineSnapshotModelProvider();

    SearchableDetailModelProvider<String, VmListModel<Void>, VmAppListModel<VM>> getSubTabVirtualMachineApplicationModelProvider();

    SearchableDetailModelProvider<AffinityGroup, VmListModel<Void>, VmAffinityGroupListModel> getSubTabVirtualMachineAffinityGroupModelProvider();

    SearchableDetailModelProvider<Permissions, VmListModel<Void>, PermissionListModel<VM>> getSubTabVirtualMachinePermissionModelProvider();

    DetailModelProvider<VmListModel<Void>, VmSessionsModel> getSubTabVirtualMachineSessionsModelProvider();

    SearchableDetailModelProvider<AuditLog, VmListModel<Void>, VmEventListModel> getSubTabVirtualMachineEventModelProvider();

    // Host

    DetailModelProvider<HostListModel<Void>, HostGeneralModel> getSubTabHostGeneralModelProvider();

    DetailModelProvider<HostListModel<Void>, HostHardwareGeneralModel> getSubTabHostHardwareModelProvider();

    SearchableDetailModelProvider<VM, HostListModel<Void>, HostVmListModel> getSubTabHostVmModelProvider();

    SearchableDetailModelProvider<HostInterfaceLineModel, HostListModel<Void>, HostInterfaceListModel> getSubTabHostInterfaceModelProvider();

    SearchableDetailModelProvider<Map<String, String>, HostListModel<Void>, HostHooksListModel> getSubTabHostHookModelProvider();

    SearchableDetailModelProvider<GlusterServerService, HostListModel<Void>, HostGlusterSwiftListModel> getSubTabHostGlusterSwiftModelProvider();

    SearchableDetailModelProvider<Permissions, HostListModel<Void>, PermissionListModel<VDS>> getSubTabHostPermissionModelProvider();

    SearchableDetailModelProvider<AuditLog, HostListModel<Void>, HostEventListModel> getSubTabHostEventModelProvider();

    SearchableDetailModelProvider<GlusterBrickEntity, HostListModel<Void>, HostBricksListModel> getSubTabHostBricksModelProvider();

    SearchableDetailModelProvider<StorageDevice, HostListModel<Void>, HostGlusterStorageDevicesListModel> getHostGlusterStorageDevicesListModelProvider();

    // Pool

    DetailModelProvider<PoolListModel, PoolGeneralModel> getSubTabPoolGeneralModelProvider();

    SearchableDetailModelProvider<VM, PoolListModel, PoolVmListModel> getSubTabPoolVmModelProvider();

    SearchableDetailModelProvider<Permissions, PoolListModel, PermissionListModel<VmPool>> getSubTabPoolPermissionModelProvider();

    // Template

    DetailModelProvider<TemplateListModel, TemplateGeneralModel> getSubTabTemplateGeneralModelProvider();

    SearchableDetailModelProvider<VM, TemplateListModel, TemplateVmListModel> getSubTabTemplateVmModelProvider();

    SearchableDetailModelProvider<VmNetworkInterface, TemplateListModel, TemplateInterfaceListModel> getSubTabTemplateInterfaceModelProvider();

    SearchableDetailModelProvider<DiskModel, TemplateListModel, TemplateDiskListModel> getSubTabTemplateDiskModelProvider();

    SearchableDetailModelProvider<StorageDomain, TemplateListModel, TemplateStorageListModel> getSubTabTemplateStorageModelProvider();

    SearchableDetailModelProvider<Permissions, TemplateListModel, PermissionListModel<VmTemplate>> getSubTabTemplatePermissionModelProvider();

    SearchableDetailModelProvider<AuditLog, TemplateListModel, TemplateEventListModel> getSubTabTemplateEventModelProvider();

    // User

    DetailModelProvider<UserListModel, UserGeneralModel> getSubTabUserGeneralModelProvider();

    SearchableDetailModelProvider<Permissions, UserListModel, UserPermissionListModel> getSubTabUserPermissionlModelProvider();

    SearchableDetailModelProvider<event_subscriber, UserListModel, UserEventNotifierListModel> getSubTabUserEventNotifierModelProvider();

    SearchableDetailModelProvider<AuditLog, UserListModel, UserEventListModel> getSubTabUserEventModelProvider();

    SearchableDetailModelProvider<UserGroup, UserListModel, UserGroupListModel> getSubTabUserGroupModelProvider();

    SearchableDetailModelProvider<Quota, UserListModel, UserQuotaListModel> getSubTabUserQuotaModelProvider();

    // Quota

    SearchableDetailModelProvider<QuotaVdsGroup, QuotaListModel, QuotaClusterListModel> getSubTabQuotaClusterModelProvider();

    SearchableDetailModelProvider<QuotaStorage, QuotaListModel, QuotaStorageListModel> getSubTabQuotaStorageModelProvider();

    SearchableDetailModelProvider<Permissions, QuotaListModel, QuotaUserListModel> getSubTabQuotaUserModelProvider();

    SearchableDetailModelProvider<Permissions, QuotaListModel, QuotaPermissionListModel> getSubTabQuotaPermissionModelProvider();

    SearchableDetailModelProvider<AuditLog, QuotaListModel, QuotaEventListModel> getSubTabQuotaEventModelProvider();

    SearchableDetailModelProvider<VM, QuotaListModel, QuotaVmListModel> getSubTabQuotaVmModelProvider();

    SearchableDetailModelProvider<VmTemplate, QuotaListModel, QuotaTemplateListModel> getSubTabQuotaTemplateModelProvider();

    // Volume

    DetailModelProvider<VolumeListModel, VolumeGeneralModel> getSubTabVolumeGeneralModelProvider();

    SearchableDetailModelProvider<GlusterBrickEntity, VolumeListModel, VolumeBrickListModel> getSubTabVolumeBrickModelProvider();

    SearchableDetailModelProvider<GlusterVolumeOptionEntity, VolumeListModel, VolumeParameterListModel> getSubTabVolumeParameterModelProvider();

    SearchableDetailModelProvider<Permissions, VolumeListModel, PermissionListModel<GlusterVolumeEntity>> getSubTabVolumePermissionModelProvider();

    SearchableDetailModelProvider<AuditLog, VolumeListModel, VolumeEventListModel> getSubTabVolumeEventModelProvider();

    SearchableDetailModelProvider<GlusterGeoRepSession, VolumeListModel, VolumeGeoRepListModel> getSubTabVolumeGeoRepModelProvider();

    SearchableDetailModelProvider<GlusterVolumeSnapshotEntity, VolumeListModel, GlusterVolumeSnapshotListModel> getSubTabVolumeSnapshotModelProvider();

    // Disk

    DetailModelProvider<DiskListModel, DiskGeneralModel> getSubTabDiskGeneralModelProvider();

    SearchableDetailModelProvider<VM, DiskListModel, DiskVmListModel> getSubTabDiskVmModelProvider();

    SearchableDetailModelProvider<VmTemplate, DiskListModel, DiskTemplateListModel> getSubTabDiskTemplateModelProvider();

    SearchableDetailModelProvider<StorageDomain, DiskListModel, DiskStorageListModel> getSubTabDiskStorageModelProvider();

    SearchableDetailModelProvider<Permissions, DiskListModel, PermissionListModel<Disk>> getSubTabDiskPermissionModelProvider();

    // Network

    DetailModelProvider<NetworkListModel, NetworkGeneralModel> getSubTabNetworkGeneralModelProvider();

    SearchableDetailModelProvider<VnicProfileView, NetworkListModel, NetworkProfileListModel> getSubTabNetworkProfileModelProvider();

    SearchableDetailModelProvider<ExternalSubnet, NetworkListModel, NetworkExternalSubnetListModel> getSubTabExternalSubnetModelProvider();

    SearchableDetailModelProvider<PairQueryable<VDSGroup, NetworkCluster>, NetworkListModel, NetworkClusterListModel> getSubTabNetworkClusterModelProvider();

    SearchableDetailModelProvider<PairQueryable<VdsNetworkInterface, VDS>, NetworkListModel, NetworkHostListModel> getSubTabNetworkHostModelProvider();

    SearchableDetailModelProvider<PairQueryable<VmNetworkInterface, VM>, NetworkListModel, NetworkVmListModel> getSubTabNetworkVmModelProvider();

    SearchableDetailModelProvider<PairQueryable<VmNetworkInterface, VmTemplate>, NetworkListModel, NetworkTemplateListModel> getSubTabNetworkTemplateModelProvider();

    SearchableDetailModelProvider<Permissions, NetworkListModel, PermissionListModel<NetworkView>> getSubTabNetworkPermissionModelProvider();

    // Provider

    DetailModelProvider<ProviderListModel, ProviderGeneralModel> getSubTabProviderGeneralModelProvider();

    SearchableDetailModelProvider<NetworkView, ProviderListModel, ProviderNetworkListModel> getSubTabProviderNetworkModelProvider();

    // Profile
    SearchableDetailModelProvider<VM, VnicProfileListModel, VnicProfileVmListModel> getSubTabVnicProfileVmModelProvider();

    SearchableDetailModelProvider<VmTemplate, VnicProfileListModel, VnicProfileTemplateListModel> getSubTabVnicProfileTemplateModelProvider();

    SearchableDetailModelProvider<Permissions, VnicProfileListModel, PermissionListModel<VnicProfileView>> getSubTabVnicProfilePermissionModelProvider();

}
