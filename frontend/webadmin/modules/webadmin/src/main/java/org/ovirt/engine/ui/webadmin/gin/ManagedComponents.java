package org.ovirt.engine.ui.webadmin.gin;

import java.util.Map;

import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.core.common.businessentities.DbUser;
import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.QuotaStorage;
import org.ovirt.engine.core.common.businessentities.QuotaVdsGroup;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmPool;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.event_subscriber;
import org.ovirt.engine.core.common.businessentities.permissions;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterHookEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeOptionEntity;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.common.businessentities.network.NetworkView;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.utils.PairQueryable;
import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.uicommon.model.DetailModelProvider;
import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterGeneralModel;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterGlusterHookListModel;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterHostListModel;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterListModel;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterNetworkListModel;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterPolicyModel;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterServiceModel;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterVmListModel;
import org.ovirt.engine.ui.uicommonweb.models.configure.PermissionListModel;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.DataCenterClusterListModel;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.DataCenterEventListModel;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.DataCenterListModel;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.DataCenterNetworkListModel;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.DataCenterQuotaListModel;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.DataCenterStorageListModel;
import org.ovirt.engine.ui.uicommonweb.models.disks.DiskGeneralModel;
import org.ovirt.engine.ui.uicommonweb.models.disks.DiskListModel;
import org.ovirt.engine.ui.uicommonweb.models.disks.DiskStorageListModel;
import org.ovirt.engine.ui.uicommonweb.models.disks.DiskTemplateListModel;
import org.ovirt.engine.ui.uicommonweb.models.disks.DiskVmListModel;
import org.ovirt.engine.ui.uicommonweb.models.events.EventListModel;
import org.ovirt.engine.ui.uicommonweb.models.gluster.VolumeBrickListModel;
import org.ovirt.engine.ui.uicommonweb.models.gluster.VolumeEventListModel;
import org.ovirt.engine.ui.uicommonweb.models.gluster.VolumeGeneralModel;
import org.ovirt.engine.ui.uicommonweb.models.gluster.VolumeParameterListModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostEventListModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostGeneralModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostHardwareGeneralModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostHooksListModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostInterfaceLineModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostInterfaceListModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostListModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostVmListModel;
import org.ovirt.engine.ui.uicommonweb.models.networks.NetworkClusterListModel;
import org.ovirt.engine.ui.uicommonweb.models.networks.NetworkGeneralModel;
import org.ovirt.engine.ui.uicommonweb.models.networks.NetworkHostListModel;
import org.ovirt.engine.ui.uicommonweb.models.networks.NetworkListModel;
import org.ovirt.engine.ui.uicommonweb.models.networks.NetworkTemplateListModel;
import org.ovirt.engine.ui.uicommonweb.models.networks.NetworkVmListModel;
import org.ovirt.engine.ui.uicommonweb.models.pools.PoolGeneralModel;
import org.ovirt.engine.ui.uicommonweb.models.pools.PoolListModel;
import org.ovirt.engine.ui.uicommonweb.models.pools.PoolVmListModel;
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
import org.ovirt.engine.ui.webadmin.ApplicationMessages;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.ApplicationTemplates;
import org.ovirt.engine.ui.webadmin.section.login.presenter.LoginSectionPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.MainContentPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.MainSectionPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.MainTabPanelPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.bookmark.BookmarkPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.tag.TagPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.MainTabClusterPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.MainTabDataCenterPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.MainTabDiskPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.MainTabEventPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.MainTabHostPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.MainTabNetworkPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.MainTabPoolPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.MainTabQuotaPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.MainTabReportsPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.MainTabStoragePresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.MainTabTemplatePresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.MainTabUserPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.MainTabVirtualMachinePresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.MainTabVolumePresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.cluster.ClusterSubTabPanelPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.cluster.SubTabClusterGeneralPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.cluster.SubTabClusterGlusterHookPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.cluster.SubTabClusterHostPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.cluster.SubTabClusterNetworkPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.cluster.SubTabClusterPermissionPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.cluster.SubTabClusterPolicyPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.cluster.SubTabClusterServicePresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.cluster.SubTabClusterVmPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.datacenter.DataCenterSubTabPanelPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.datacenter.SubTabDataCenterClusterPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.datacenter.SubTabDataCenterEventPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.datacenter.SubTabDataCenterNetworkPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.datacenter.SubTabDataCenterPermissionPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.datacenter.SubTabDataCenterQuotaPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.datacenter.SubTabDataCenterStoragePresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.disk.DiskSubTabPanelPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.disk.SubTabDiskGeneralPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.disk.SubTabDiskPermissionPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.disk.SubTabDiskStoragePresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.disk.SubTabDiskTemplatePresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.disk.SubTabDiskVmPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.gluster.SubTabVolumeBrickPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.gluster.SubTabVolumeEventPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.gluster.SubTabVolumeGeneralPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.gluster.SubTabVolumeParameterPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.gluster.SubTabVolumePermissionPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.gluster.VolumeSubTabPanelPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.host.HostSubTabPanelPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.host.SubTabHostEventPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.host.SubTabHostGeneralPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.host.SubTabHostHardwarePresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.host.SubTabHostHookPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.host.SubTabHostInterfacePresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.host.SubTabHostPermissionPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.host.SubTabHostVmPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.network.NetworkSubTabPanelPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.network.SubTabNetworkClusterPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.network.SubTabNetworkGeneralPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.network.SubTabNetworkHostPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.network.SubTabNetworkPermissionPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.network.SubTabNetworkTemplatePresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.network.SubTabNetworkVmPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.pool.PoolSubTabPanelPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.pool.SubTabPoolGeneralPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.pool.SubTabPoolPermissionPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.pool.SubTabPoolVmPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.quota.QuotaSubTabPanelPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.quota.SubTabQuotaClusterPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.quota.SubTabQuotaEventPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.quota.SubTabQuotaPermissionPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.quota.SubTabQuotaStoragePresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.quota.SubTabQuotaTemplatePresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.quota.SubTabQuotaUserPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.quota.SubTabQuotaVmPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.storage.StorageSubTabPanelPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.storage.SubTabStorageDataCenterPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.storage.SubTabStorageDiskPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.storage.SubTabStorageEventPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.storage.SubTabStorageGeneralPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.storage.SubTabStorageIsoPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.storage.SubTabStoragePermissionPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.storage.SubTabStorageTemplateBackupPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.storage.SubTabStorageTemplatePresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.storage.SubTabStorageVmBackupPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.storage.SubTabStorageVmPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.template.SubTabTemplateDiskPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.template.SubTabTemplateEventPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.template.SubTabTemplateGeneralPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.template.SubTabTemplateInterfacePresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.template.SubTabTemplatePermissionPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.template.SubTabTemplateStoragePresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.template.SubTabTemplateVmPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.template.TemplateSubTabPanelPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.user.SubTabUserEventNotifierPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.user.SubTabUserEventPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.user.SubTabUserGeneralPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.user.SubTabUserGroupPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.user.SubTabUserPermissionPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.user.SubTabUserQuotaPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.user.UserSubTabPanelPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.virtualMachine.SubTabVirtualMachineApplicationPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.virtualMachine.SubTabVirtualMachineEventPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.virtualMachine.SubTabVirtualMachineGeneralPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.virtualMachine.SubTabVirtualMachineNetworkInterfacePresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.virtualMachine.SubTabVirtualMachinePermissionPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.virtualMachine.SubTabVirtualMachineSessionsPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.virtualMachine.SubTabVirtualMachineSnapshotPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.virtualMachine.SubTabVirtualMachineVirtualDiskPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.virtualMachine.VirtualMachineSubTabPanelPresenter;

import com.google.gwt.inject.client.AsyncProvider;
import com.google.inject.Provider;

/**
 * Contains methods for accessing managed components that participate in dependency injection.
 * <p>
 * There should be a method for each {@link com.gwtplatform.mvp.client.Presenter Presenter} (excluding
 * {@link com.gwtplatform.mvp.clientcom.gwtplatform.mvp.client.PresenterWidget PresenterWidget} classes, unless they are
 * referenced through {@link ClientGinjector} directly). This is necessary due to the current limitation of GWTP-GIN
 * integration.
 */
public interface ManagedComponents {

    ApplicationConstants getApplicationConstants();

    ApplicationResources getApplicationResources();

    ApplicationTemplates getApplicationTemplates();

    ApplicationMessages getApplicationMessages();

    ClientStorage getClientStorage();

    // PresenterWidgets

    Provider<BookmarkPopupPresenterWidget> getBookmarkPopupPresenterWidgetProvider();

    Provider<TagPopupPresenterWidget> getTagPopupPresenterWidgetProvider();

    // Presenters: Login section

    Provider<LoginSectionPresenter> getLoginSectionPresenter();

    // Presenters: Main section: common stuff

    AsyncProvider<MainSectionPresenter> getMainSectionPresenter();

    AsyncProvider<MainContentPresenter> getMainContentPresenter();

    // Presenters & model providers: Main section: main tabs

    AsyncProvider<MainTabPanelPresenter> getMainTabPanelPresenter();

    AsyncProvider<MainTabDataCenterPresenter> getMainTabDataCenterPresenter();

    MainModelProvider<StoragePool, DataCenterListModel> getMainTabDataCenterModelProvider();

    AsyncProvider<MainTabClusterPresenter> getMainTabClusterPresenter();

    MainModelProvider<VDSGroup, ClusterListModel> getMainTabClusterModelProvider();

    AsyncProvider<MainTabHostPresenter> getMainTabHostPresenter();

    MainModelProvider<VDS, HostListModel> getMainTabHostModelProvider();

    AsyncProvider<MainTabNetworkPresenter> getMainTabNetworkPresenter();

    MainModelProvider<NetworkView, NetworkListModel> getMainTabNetworkModelProvider();

    AsyncProvider<MainTabStoragePresenter> getMainTabStoragePresenter();

    MainModelProvider<StorageDomain, StorageListModel> getMainTabStorageModelProvider();

    AsyncProvider<MainTabVirtualMachinePresenter> getMainTabVirtualMachinePresenter();

    MainModelProvider<VM, VmListModel> getMainTabVirtualMachineModelProvider();

    AsyncProvider<MainTabPoolPresenter> getMainTabPoolPresenter();

    MainModelProvider<VmPool, PoolListModel> getMainTabPoolModelProvider();

    AsyncProvider<MainTabTemplatePresenter> getMainTabTemplatePresenter();

    MainModelProvider<VmTemplate, TemplateListModel> getMainTabTemplateModelProvider();

    AsyncProvider<MainTabUserPresenter> getMainTabUserPresenter();

    MainModelProvider<DbUser, UserListModel> getMainTabUserModelProvider();

    AsyncProvider<MainTabEventPresenter> getMainTabEventPresenter();

    MainModelProvider<AuditLog, EventListModel> getMainTabEventModelProvider();

    AsyncProvider<MainTabReportsPresenter> getMainTabReportsPresenter();

    MainModelProvider<Void, ReportsListModel> getMainTabReportsModelProvider();

    AsyncProvider<MainTabQuotaPresenter> getMainTabQuotaPresenter();

    MainModelProvider<Quota, QuotaListModel> getMainTabQuotaModelProvider();

    AsyncProvider<MainTabDiskPresenter> getMainTabDiskPresenter();

    MainModelProvider<Disk, DiskListModel> getMainTabDiskModelProvider();

    // Presenters & model providers: Main section: sub tabs

    // DataCenter

    AsyncProvider<DataCenterSubTabPanelPresenter> getDataCenterSubTabPanelPresenter();

    AsyncProvider<SubTabDataCenterStoragePresenter> getSubTabDataCenterStoragePresenter();

    SearchableDetailModelProvider<StorageDomain, DataCenterListModel, DataCenterStorageListModel> getSubTabDataCenterStorageModelProvider();

    AsyncProvider<SubTabDataCenterNetworkPresenter> getSubTabDataCenterNetworkPresenter();

    SearchableDetailModelProvider<Network, DataCenterListModel, DataCenterNetworkListModel> getSubTabDataCenterNetworkModelProvider();

    AsyncProvider<SubTabDataCenterClusterPresenter> getSubTabDataCenterClusterPresenter();

    SearchableDetailModelProvider<VDSGroup, DataCenterListModel, DataCenterClusterListModel> getSubTabDataCenterClusterModelProvider();

    AsyncProvider<SubTabDataCenterQuotaPresenter> getSubTabDataCenterQuotaPresenter();

    SearchableDetailModelProvider<Quota, DataCenterListModel, DataCenterQuotaListModel> getSubTabDataCenterQuotaModelProvider();

    AsyncProvider<SubTabDataCenterPermissionPresenter> getSubTabDataCenterPermissionPresenter();

    SearchableDetailModelProvider<permissions, DataCenterListModel, PermissionListModel> getSubTabDataCenterPermissionModelProvider();

    AsyncProvider<SubTabDataCenterEventPresenter> getSubTabDataCenterEventPresenter();

    SearchableDetailModelProvider<AuditLog, DataCenterListModel, DataCenterEventListModel> getSubTabDataCenterEventModelProvider();

    // Storage

    AsyncProvider<StorageSubTabPanelPresenter> getStorageSubTabPanelPresenter();

    AsyncProvider<SubTabStorageGeneralPresenter> getSubTabStorageGeneralPresenter();

    DetailModelProvider<StorageListModel, StorageGeneralModel> getSubTabStorageGeneralModelProvider();

    AsyncProvider<SubTabStorageDataCenterPresenter> getSubTabStorageDataCenterPresenter();

    SearchableDetailModelProvider<StorageDomain, StorageListModel, StorageDataCenterListModel> getSubTabStorageDataCenterModelProvider();

    AsyncProvider<SubTabStorageVmBackupPresenter> getSubTabStorageVmBackupPresenter();

    SearchableDetailModelProvider<VM, StorageListModel, VmBackupModel> getSubTabStorageVmBackupModelProvider();

    AsyncProvider<SubTabStorageTemplateBackupPresenter> getSubTabStorageTemplateBackupPresenter();

    SearchableDetailModelProvider<VmTemplate, StorageListModel, TemplateBackupModel> getSubTabStorageTemplateBackupModelProvider();

    AsyncProvider<SubTabStorageVmPresenter> getSubTabStorageVmPresenter();

    SearchableDetailModelProvider<VM, StorageListModel, StorageVmListModel> getSubTabStorageVmModelProvider();

    AsyncProvider<SubTabStorageTemplatePresenter> getSubTabStorageTemplatePresenter();

    SearchableDetailModelProvider<VmTemplate, StorageListModel, StorageTemplateListModel> getSubTabStorageTemplateModelProvider();

    AsyncProvider<SubTabStorageIsoPresenter> getSubTabStorageIsoPresenter();

    SearchableDetailModelProvider<EntityModel, StorageListModel, StorageIsoListModel> getSubTabStorageIsoModelProvider();

    AsyncProvider<SubTabStorageDiskPresenter> getSubTabStorageDiskPresenter();

    SearchableDetailModelProvider<Disk, StorageListModel, StorageDiskListModel> getSubTabStorageDiskModelProvider();

    AsyncProvider<SubTabStoragePermissionPresenter> getSubTabStoragePermissionPresenter();

    SearchableDetailModelProvider<permissions, StorageListModel, PermissionListModel> getSubTabStoragePermissionModelProvider();

    AsyncProvider<SubTabStorageEventPresenter> getSubTabStorageEventPresenter();

    SearchableDetailModelProvider<AuditLog, StorageListModel, StorageEventListModel> getSubTabStorageEventModelProvider();

    // Cluster

    AsyncProvider<ClusterSubTabPanelPresenter> getClusterSubTabPanelPresenter();

    AsyncProvider<SubTabClusterGeneralPresenter> getSubTabClusterGeneralPresenter();

    DetailModelProvider<ClusterListModel, ClusterGeneralModel> getSubTabClusterGeneralModelProvider();

    AsyncProvider<SubTabClusterPolicyPresenter> getSubTabClusterPolicyPresenter();

    DetailModelProvider<ClusterListModel, ClusterPolicyModel> getSubTabClusterPolicyModelProvider();

    AsyncProvider<SubTabClusterHostPresenter> getSubTabClusterHostPresenter();

    SearchableDetailModelProvider<VDS, ClusterListModel, ClusterHostListModel> getSubTabClusterHostModelProvider();

    AsyncProvider<SubTabClusterVmPresenter> getSubTabClusterVmPresenter();

    SearchableDetailModelProvider<VM, ClusterListModel, ClusterVmListModel> getSubTabClusterVmModelProvider();

    AsyncProvider<SubTabClusterNetworkPresenter> getSubTabClusterNetworkPresenter();

    SearchableDetailModelProvider<Network, ClusterListModel, ClusterNetworkListModel> getSubTabClusterNetworkModelProvider();

    AsyncProvider<SubTabClusterServicePresenter> getSubTabClusterServicePresenter();

    DetailModelProvider<ClusterListModel, ClusterServiceModel> getSubTabClusterServiceModelProvider();

    AsyncProvider<SubTabClusterGlusterHookPresenter> getSubTabClusterGlusterHookPresenter();

    SearchableDetailModelProvider<GlusterHookEntity, ClusterListModel, ClusterGlusterHookListModel> getSubTabClusterGlusterHookModelProvider();

    AsyncProvider<SubTabClusterPermissionPresenter> getSubTabClusterPermissionPresenter();

    SearchableDetailModelProvider<permissions, ClusterListModel, PermissionListModel> getSubTabClusterPermissionModelProvider();

    // VirtualMachine

    AsyncProvider<VirtualMachineSubTabPanelPresenter> getVirtualMachineSubTabPanelPresenter();

    AsyncProvider<SubTabVirtualMachineGeneralPresenter> getSubTabVirtualMachineGeneralPresenter();

    DetailModelProvider<VmListModel, VmGeneralModel> getSubTabVirtualMachineGeneralModelProvider();

    AsyncProvider<SubTabVirtualMachineNetworkInterfacePresenter> getSubTabVirtualMachineNetworkInterfacePresenter();

    SearchableDetailModelProvider<VmNetworkInterface, VmListModel, VmInterfaceListModel> getSubTabVirtualMachineNetworkInterfaceModelProvider();

    AsyncProvider<SubTabVirtualMachineVirtualDiskPresenter> getSubTabVirtualMachineVirtualDiskPresenter();

    SearchableDetailModelProvider<Disk, VmListModel, VmDiskListModel> getSubTabVirtualMachineVirtualDiskModelProvider();

    AsyncProvider<SubTabVirtualMachineSnapshotPresenter> getSubTabVirtualMachineSnapshotPresenter();

    SearchableDetailModelProvider<Snapshot, VmListModel, VmSnapshotListModel> getSubTabVirtualMachineSnapshotModelProvider();

    AsyncProvider<SubTabVirtualMachineApplicationPresenter> getSubTabVirtualMachineApplicationPresenter();

    SearchableDetailModelProvider<String, VmListModel, VmAppListModel> getSubTabVirtualMachineApplicationModelProvider();

    AsyncProvider<SubTabVirtualMachinePermissionPresenter> getSubTabVirtualMachinePermissionPresenter();

    SearchableDetailModelProvider<permissions, VmListModel, PermissionListModel> getSubTabVirtualMachinePermissionModelProvider();

    AsyncProvider<SubTabVirtualMachineSessionsPresenter> getSubTabVirtualMachineSessionsPresenter();

    DetailModelProvider<VmListModel, VmSessionsModel> getSubTabVirtualMachineSessionsModelProvider();

    AsyncProvider<SubTabVirtualMachineEventPresenter> getSubTabVirtualMachineEventPresenter();

    SearchableDetailModelProvider<AuditLog, VmListModel, VmEventListModel> getSubTabVirtualMachineEventModelProvider();

    // Host

    AsyncProvider<HostSubTabPanelPresenter> getHostSubTabPanelPresenter();

    AsyncProvider<SubTabHostGeneralPresenter> getSubTabHostGeneralPresenter();

    DetailModelProvider<HostListModel, HostGeneralModel> getSubTabHostGeneralModelProvider();

    AsyncProvider<SubTabHostHardwarePresenter> getSubTabHostHardwarePresenter();

    DetailModelProvider<HostListModel, HostHardwareGeneralModel> getSubTabHostHardwareModelProvider();

    AsyncProvider<SubTabHostVmPresenter> getSubTabHostVmPresenter();

    SearchableDetailModelProvider<VM, HostListModel, HostVmListModel> getSubTabHostVmModelProvider();

    AsyncProvider<SubTabHostInterfacePresenter> getSubTabHostInterfacePresenter();

    SearchableDetailModelProvider<HostInterfaceLineModel, HostListModel, HostInterfaceListModel> getSubTabHostInterfaceModelProvider();

    AsyncProvider<SubTabHostHookPresenter> getSubTabHostHookPresenter();

    SearchableDetailModelProvider<Map<String, String>, HostListModel, HostHooksListModel> getSubTabHostHookModelProvider();

    AsyncProvider<SubTabHostPermissionPresenter> getSubTabHostPermissionPresenter();

    SearchableDetailModelProvider<permissions, HostListModel, PermissionListModel> getSubTabHostPermissionModelProvider();

    AsyncProvider<SubTabHostEventPresenter> getSubTabHostEventPresenter();

    SearchableDetailModelProvider<AuditLog, HostListModel, HostEventListModel> getSubTabHostEventModelProvider();

    // Pool

    AsyncProvider<PoolSubTabPanelPresenter> getPoolSubTabPanelPresenter();

    AsyncProvider<SubTabPoolGeneralPresenter> getSubTabPoolGeneralPresenter();

    DetailModelProvider<PoolListModel, PoolGeneralModel> getSubTabPoolGeneralModelProvider();

    AsyncProvider<SubTabPoolVmPresenter> getSubTabPoolVmPresenter();

    SearchableDetailModelProvider<VM, PoolListModel, PoolVmListModel> getSubTabPoolVmModelProvider();

    AsyncProvider<SubTabPoolPermissionPresenter> getSubTabPoolPermissionPresenter();

    SearchableDetailModelProvider<permissions, PoolListModel, PermissionListModel> getSubTabPoolPermissionModelProvider();

    // Template

    AsyncProvider<TemplateSubTabPanelPresenter> getTemplateSubTabPanelPresenter();

    AsyncProvider<SubTabTemplateGeneralPresenter> getSubTabTemplateGeneralPresenter();

    DetailModelProvider<TemplateListModel, TemplateGeneralModel> getSubTabTemplateGeneralModelProvider();

    AsyncProvider<SubTabTemplateVmPresenter> getSubTabTemplateVmPresenter();

    SearchableDetailModelProvider<VM, TemplateListModel, TemplateVmListModel> getSubTabTemplateVmModelProvider();

    AsyncProvider<SubTabTemplateInterfacePresenter> getSubTabTemplateInterfacePresenter();

    SearchableDetailModelProvider<VmNetworkInterface, TemplateListModel, TemplateInterfaceListModel> getSubTabTemplateInterfaceModelProvider();

    AsyncProvider<SubTabTemplateDiskPresenter> getSubTabTemplateDiskPresenter();

    SearchableDetailModelProvider<DiskModel, TemplateListModel, TemplateDiskListModel> getSubTabTemplateDiskModelProvider();

    AsyncProvider<SubTabTemplateStoragePresenter> getSubTabTemplateStoragePresenter();

    SearchableDetailModelProvider<StorageDomain, TemplateListModel, TemplateStorageListModel> getSubTabTemplateStorageModelProvider();

    AsyncProvider<SubTabTemplatePermissionPresenter> getSubTabTemplatePermissionPresenter();

    SearchableDetailModelProvider<permissions, TemplateListModel, PermissionListModel> getSubTabTemplatePermissionModelProvider();

    AsyncProvider<SubTabTemplateEventPresenter> getSubTabTemplateEventPresenter();

    SearchableDetailModelProvider<AuditLog, TemplateListModel, TemplateEventListModel> getSubTabTemplateEventModelProvider();

    // User

    AsyncProvider<UserSubTabPanelPresenter> getUserSubTabPanelPresenter();

    AsyncProvider<SubTabUserGeneralPresenter> getSubTabUserGeneralPresenter();

    DetailModelProvider<UserListModel, UserGeneralModel> getSubTabUserGeneralModelProvider();

    AsyncProvider<SubTabUserPermissionPresenter> getSubTabUserPermissionlPresenter();

    SearchableDetailModelProvider<permissions, UserListModel, UserPermissionListModel> getSubTabUserPermissionlModelProvider();

    AsyncProvider<SubTabUserEventNotifierPresenter> getSubTabUserEventNotifierPresenter();

    SearchableDetailModelProvider<event_subscriber, UserListModel, UserEventNotifierListModel> getSubTabUserEventNotifierModelProvider();

    AsyncProvider<SubTabUserEventPresenter> getSubTabUserEventPresenter();

    SearchableDetailModelProvider<AuditLog, UserListModel, UserEventListModel> getSubTabUserEventModelProvider();

    AsyncProvider<SubTabUserGroupPresenter> getSubTabUserGroupPresenter();

    SearchableDetailModelProvider<UserGroup, UserListModel, UserGroupListModel> getSubTabUserGroupModelProvider();

    AsyncProvider<SubTabUserQuotaPresenter> getSubTabUserQuotaPresenter();

    SearchableDetailModelProvider<Quota, UserListModel, UserQuotaListModel> getSubTabUserQuotaModelProvider();

    // Quota

    AsyncProvider<QuotaSubTabPanelPresenter> getQuotaSubTabPanelPresenter();

    AsyncProvider<SubTabQuotaClusterPresenter> getSubTabQuotaClusterPresenter();

    SearchableDetailModelProvider<QuotaVdsGroup, QuotaListModel, QuotaClusterListModel> getSubTabQuotaClusterModelProvider();

    AsyncProvider<SubTabQuotaStoragePresenter> getSubTabQuotaStoragePresenter();

    SearchableDetailModelProvider<QuotaStorage, QuotaListModel, QuotaStorageListModel> getSubTabQuotaStorageModelProvider();

    AsyncProvider<SubTabQuotaUserPresenter> getSubTabQuotaUserPresenter();

    SearchableDetailModelProvider<permissions, QuotaListModel, QuotaUserListModel> getSubTabQuotaUserModelProvider();

    AsyncProvider<SubTabQuotaPermissionPresenter> getSubTabQuotaPermissionPresenter();

    SearchableDetailModelProvider<permissions, QuotaListModel, QuotaPermissionListModel> getSubTabQuotaPermissionModelProvider();

    AsyncProvider<SubTabQuotaEventPresenter> getSubTabQuotaEventPresenter();

    SearchableDetailModelProvider<AuditLog, QuotaListModel, QuotaEventListModel> getSubTabQuotaEventModelProvider();

    AsyncProvider<SubTabQuotaVmPresenter> getSubTabQuotaVmPresenter();

    SearchableDetailModelProvider<VM, QuotaListModel, QuotaVmListModel> getSubTabQuotaVmModelProvider();

    AsyncProvider<SubTabQuotaTemplatePresenter> getSubTabQuotaTemplatePresenter();

    SearchableDetailModelProvider<VmTemplate, QuotaListModel, QuotaTemplateListModel> getSubTabQuotaTemplateModelProvider();

    // Volumes

    AsyncProvider<MainTabVolumePresenter> getMainTabVolumePresenter();

    MainModelProvider<GlusterVolumeEntity, VolumeListModel> getMainTabVolumeModelProvider();

    AsyncProvider<VolumeSubTabPanelPresenter> getVolumeSubTabPanelPresenter();

    // volume - general
    AsyncProvider<SubTabVolumeGeneralPresenter> getSubTabVolumeGeneralPresenter();

    DetailModelProvider<VolumeListModel, VolumeGeneralModel> getSubTabVolumeGeneralModelProvider();

    // volume - bricks
    AsyncProvider<SubTabVolumeBrickPresenter> getSubTabVolumeBrickPresenter();

    SearchableDetailModelProvider<GlusterBrickEntity, VolumeListModel, VolumeBrickListModel> getSubTabVolumeBrickModelProvider();

    // volume - parameters
    AsyncProvider<SubTabVolumeParameterPresenter> getSubTabVolumeParameterPresenter();

    SearchableDetailModelProvider<GlusterVolumeOptionEntity, VolumeListModel, VolumeParameterListModel> getSubTabVolumeParameterModelProvider();

    // volume - permission
    AsyncProvider<SubTabVolumePermissionPresenter> getSubTabVolumePermissionPresenter();

    SearchableDetailModelProvider<permissions, VolumeListModel, PermissionListModel> getSubTabVolumePermissionModelProvider();

    // volume - events
    AsyncProvider<SubTabVolumeEventPresenter> getSubTabVolumeEventPresenter();

    SearchableDetailModelProvider<AuditLog, VolumeListModel, VolumeEventListModel> getSubTabVolumeEventModelProvider();

    // Disk

    AsyncProvider<DiskSubTabPanelPresenter> getDiskSubTabPanelPresenter();

    AsyncProvider<SubTabDiskGeneralPresenter> getSubTabDiskGeneralPresenter();

    DetailModelProvider<DiskListModel, DiskGeneralModel> getSubTabDiskGeneralModelProvider();

    AsyncProvider<SubTabDiskVmPresenter> getSubTabDiskVmPresenter();

    SearchableDetailModelProvider<VM, DiskListModel, DiskVmListModel> getSubTabDiskVmModelProvider();

    AsyncProvider<SubTabDiskTemplatePresenter> getSubTabDiskTemplatePresenter();

    SearchableDetailModelProvider<VmTemplate, DiskListModel, DiskTemplateListModel> getSubTabDiskTemplateModelProvider();

    AsyncProvider<SubTabDiskStoragePresenter> getSubTabDiskStoragePresenter();

    SearchableDetailModelProvider<StorageDomain, DiskListModel, DiskStorageListModel> getSubTabDiskStorageModelProvider();

    AsyncProvider<SubTabDiskPermissionPresenter> getSubTabDiskPermissionPresenter();

    SearchableDetailModelProvider<permissions, DiskListModel, PermissionListModel> getSubTabDiskPermissionModelProvider();

    // Network

    AsyncProvider<NetworkSubTabPanelPresenter> getNetworkSubTabPanelPresenter();

    AsyncProvider<SubTabNetworkGeneralPresenter> getSubTabNetworkGeneralPresenter();

    DetailModelProvider<NetworkListModel, NetworkGeneralModel> getSubTabNetworkGeneralModelProvider();

    AsyncProvider<SubTabNetworkClusterPresenter> getSubTabNetworkClusterPresenter();

    SearchableDetailModelProvider<PairQueryable<VDSGroup, NetworkCluster>, NetworkListModel, NetworkClusterListModel> getSubTabNetworkClusterModelProvider();

    AsyncProvider<SubTabNetworkHostPresenter> getSubTabNetworkHostPresenter();

    SearchableDetailModelProvider<PairQueryable<VdsNetworkInterface, VDS>, NetworkListModel, NetworkHostListModel> getSubTabNetworkHostModelProvider();

    AsyncProvider<SubTabNetworkVmPresenter> getSubTabNetworkVmPresenter();

    SearchableDetailModelProvider<PairQueryable<VmNetworkInterface, VM>, NetworkListModel, NetworkVmListModel> getSubTabNetworkVmModelProvider();

    AsyncProvider<SubTabNetworkTemplatePresenter> getSubTabNetworkTemplatePresenter();

    SearchableDetailModelProvider<PairQueryable<VmNetworkInterface, VmTemplate>, NetworkListModel, NetworkTemplateListModel> getSubTabNetworkTemplateModelProvider();

    AsyncProvider<SubTabNetworkPermissionPresenter> getSubTabNetworkPermissionPresenter();

    SearchableDetailModelProvider<permissions, NetworkListModel, PermissionListModel> getSubTabNetworkPermissionModelProvider();
}
