package org.ovirt.engine.ui.webadmin.gin;

import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.Erratum;
import org.ovirt.engine.core.common.businessentities.EventSubscriber;
import org.ovirt.engine.core.common.businessentities.HostDeviceView;
import org.ovirt.engine.core.common.businessentities.InstanceType;
import org.ovirt.engine.core.common.businessentities.IscsiBond;
import org.ovirt.engine.core.common.businessentities.Label;
import org.ovirt.engine.core.common.businessentities.MacPool;
import org.ovirt.engine.core.common.businessentities.Permission;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.Role;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainDR;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.UserSession;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmPool;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
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
import org.ovirt.engine.core.common.businessentities.storage.LibvirtSecret;
import org.ovirt.engine.core.common.businessentities.storage.RepoImage;
import org.ovirt.engine.core.common.scheduling.AffinityGroup;
import org.ovirt.engine.core.common.scheduling.ClusterPolicy;
import org.ovirt.engine.core.common.utils.PairQueryable;
import org.ovirt.engine.ui.common.gin.BasePresenterModule;
import org.ovirt.engine.ui.common.presenter.ActionPanelPresenterWidget;
import org.ovirt.engine.ui.common.presenter.DetailActionPanelPresenterWidget;
import org.ovirt.engine.ui.common.presenter.DisksBreadCrumbsPresenterWidget;
import org.ovirt.engine.ui.common.presenter.ExpandAllButtonPresenterWidget;
import org.ovirt.engine.ui.common.presenter.NetworkBreadCrumbsPresenterWidget;
import org.ovirt.engine.ui.common.presenter.OvirtBreadCrumbsPresenterWidget;
import org.ovirt.engine.ui.common.presenter.QuotaBreadCrumbsPresenterWidget;
import org.ovirt.engine.ui.common.presenter.ShowHideVfPresenterWidget;
import org.ovirt.engine.ui.common.presenter.TemplateBreadCrumbsPresenterWidget;
import org.ovirt.engine.ui.common.presenter.VnicProfileBreadCrumbsPresenterWidget;
import org.ovirt.engine.ui.common.presenter.popup.HostMaintenanceConfirmationPopupPresenterWidget;
import org.ovirt.engine.ui.common.presenter.popup.HostRestartConfirmationPopupPresenterWidget;
import org.ovirt.engine.ui.common.presenter.popup.HostUpgradePopupPresenterWidget;
import org.ovirt.engine.ui.common.view.ActionPanelView;
import org.ovirt.engine.ui.common.view.DetailActionPanelView;
import org.ovirt.engine.ui.common.view.DisksBreadCrumbsView;
import org.ovirt.engine.ui.common.view.ExpandAllButtonView;
import org.ovirt.engine.ui.common.view.NetworkBreadCrumbsView;
import org.ovirt.engine.ui.common.view.OvirtBreadCrumbsView;
import org.ovirt.engine.ui.common.view.QuotaBreadCrumbsView;
import org.ovirt.engine.ui.common.view.ShowHideVfButtonView;
import org.ovirt.engine.ui.common.view.TemplateBreadCrumbsView;
import org.ovirt.engine.ui.common.view.VnicProfileBreadCrumbsView;
import org.ovirt.engine.ui.common.view.popup.HostMaintenanceConfirmationPopupView;
import org.ovirt.engine.ui.common.view.popup.HostRestartConfirmationPopupView;
import org.ovirt.engine.ui.common.view.popup.HostUpgradePopupView;
import org.ovirt.engine.ui.common.widget.MenuDetailsProvider;
import org.ovirt.engine.ui.uicommonweb.models.EngineErrataListModel;
import org.ovirt.engine.ui.uicommonweb.models.SessionListModel;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterListModel;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.DataCenterListModel;
import org.ovirt.engine.ui.uicommonweb.models.disks.DiskListModel;
import org.ovirt.engine.ui.uicommonweb.models.events.EventListModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostInterfaceLineModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostListModel;
import org.ovirt.engine.ui.uicommonweb.models.networks.NetworkListModel;
import org.ovirt.engine.ui.uicommonweb.models.pools.PoolListModel;
import org.ovirt.engine.ui.uicommonweb.models.profiles.VnicProfileListModel;
import org.ovirt.engine.ui.uicommonweb.models.providers.ProviderListModel;
import org.ovirt.engine.ui.uicommonweb.models.quota.QuotaListModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageListModel;
import org.ovirt.engine.ui.uicommonweb.models.templates.TemplateListModel;
import org.ovirt.engine.ui.uicommonweb.models.users.UserListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.DiskModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmListModel;
import org.ovirt.engine.ui.uicommonweb.models.volumes.VolumeListModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.AboutPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.HeaderPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.MainClusterPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.MainContentPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.MainDataCenterPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.MainDiskPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.MainEngineErrataPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.MainEventPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.MainHostPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.MainNetworkPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.MainPoolPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.MainProviderPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.MainQuotaPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.MainSectionPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.MainSessionPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.MainStoragePresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.MainTemplatePresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.MainUserPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.MainVirtualMachinePresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.MainVnicProfilePresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.MainVolumePresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.MenuPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.NotificationPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.SearchPanelPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.overlay.BookmarkPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.overlay.TagsPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.overlay.TasksPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.AssignTagsPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.CpuQosPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.HostErrataListWithDetailsPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.HostNetworkQosPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.ImportTemplatesPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.ImportVmsPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.NetworkQoSPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.PermissionsPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.StorageQosPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.StorageQosRemovePopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.SystemPermissionsRemoveConfirmationPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.VmErrataListWithDetailsPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.bookmark.BookmarkPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.cluster.AddDataCenterClusterPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.cluster.ClusterManageNetworkPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.cluster.ClusterPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.cluster.ClusterWarningsPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.cluster.GlusterHookContentPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.cluster.GlusterHookResolveConflictsPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.cluster.ManageGlusterSwiftPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.cluster.NewClusterNetworkPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.configure.ConfigurePopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.configure.RolePopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.datacenter.DataCenterForceRemovePopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.datacenter.DataCenterPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.datacenter.EditDataCenterNetworkPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.datacenter.EditNetworkPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.datacenter.FindMultiStoragePopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.datacenter.FindSingleStoragePopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.datacenter.IscsiBondPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.datacenter.NewDataCenterNetworkPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.datacenter.NewNetworkPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.datacenter.RecoveryStoragePopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.event.EventPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.gluster.AddBrickPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.gluster.BrickAdvancedDetailsPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.gluster.CreateBrickPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.gluster.DetachGlusterHostsPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.gluster.GlusterClusterSnapshotConfigureOptionsPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.gluster.GlusterVolumeGeoRepActionConfirmPopUpViewPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.gluster.GlusterVolumeGeoRepCreateSessionPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.gluster.GlusterVolumeGeoReplicationSessionConfigPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.gluster.GlusterVolumeSnapshotConfigureOptionsPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.gluster.GlusterVolumeSnapshotCreatePopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.gluster.RemoveBrickPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.gluster.RemoveBrickStatusPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.gluster.ReplaceBrickPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.gluster.ResetBrickPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.gluster.VolumeGeoRepSessionDetailsPopUpPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.gluster.VolumeParameterPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.gluster.VolumePopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.gluster.VolumeProfileStatisticsPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.gluster.VolumeRebalanceStatusPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.guide.GuidePopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.guide.MoveHostPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.host.ConfigureLocalStoragePopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.host.HostFenceAgentPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.host.HostFenceProxyPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.host.HostInstallPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.host.HostPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.host.HostSetupNetworksPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.host.ManualFencePopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.host.MultipleHostsPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.host.NetworkAttachmentPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.host.SetupNetworksBondPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.host.VfsConfigPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.hostdev.AddVmHostDevicePopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.hostdev.VmRepinHostPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.instancetypes.InstanceTypesPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.label.AffinityLabelPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.macpool.SharedMacPoolPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.ova.ExportOvaPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.pool.PoolEditPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.pool.PoolNewPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.profile.CpuProfilePopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.profile.DiskProfilePopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.profile.VnicProfilePopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.provider.ExternalSubnetPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.provider.ImportNetworksPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.provider.ProviderPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.provider.ProviderSecretPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.quota.ChangeQuotaPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.quota.EditQuotaClusterPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.quota.EditQuotaStoragePopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.quota.QuotaPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.scheduling.AffinityGroupPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.scheduling.ClusterPolicyPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.scheduling.ManagePolicyUnitPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.storage.DisksAllocationPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.storage.FindMultiDcPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.storage.FindSingleDcPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.storage.ImportExportImagePopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.storage.RegisterTemplatePopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.storage.RegisterVmPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.storage.StorageDRPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.storage.StorageDestroyPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.storage.StorageForceCreatePopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.storage.StoragePopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.storage.StorageRemovePopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.storage.UploadImagePopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.storage.backup.ImportCloneDialogPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.storage.backup.ImportTemplatePopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.storage.backup.ImportVmFromExportDomainPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.tag.TagPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.template.ImportTemplateFromOvaPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.template.TemplateEditPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.template.TemplateInterfacePopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.user.ManageEventsPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.user.UserRolesPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.vm.CloneVmPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.vm.ImportVmFromExternalProviderPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.vm.SingleSelectionVmDiskAttachPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.vm.VmChangeCDPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.vm.VmClonePopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.vm.VmDiskAttachPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.vm.VmDiskPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.vm.VmDiskRemovePopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.vm.VmDiskSparsifyPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.vm.VmExportPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.vm.VmHighPerformanceConfigurationPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.vm.VmInterfacePopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.vm.VmMakeTemplatePopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.vm.VmNextRunConfigurationPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.vm.VmPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.vm.VmRunOncePopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.vm.VmSnapshotCreatePopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.vm.VmSnapshotCustomPreviewPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.vm.VmSnapshotPreviewPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.vm.VncInfoPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.vm.register.VnicProfileMappingPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.cluster.ClusterSubTabPanelPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.cluster.SubTabClusterAffinityGroupPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.cluster.SubTabClusterAffinityLabelPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.cluster.SubTabClusterCpuProfilePresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.cluster.SubTabClusterEventPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.cluster.SubTabClusterGeneralPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.cluster.SubTabClusterGlusterHookPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.cluster.SubTabClusterHostPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.cluster.SubTabClusterNetworkPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.cluster.SubTabClusterPermissionPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.cluster.SubTabClusterServicePresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.cluster.SubTabClusterVmPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.datacenter.DataCenterSubTabPanelPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.datacenter.SubTabDataCenterClusterPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.datacenter.SubTabDataCenterEventPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.datacenter.SubTabDataCenterIscsiBondPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.datacenter.SubTabDataCenterNetworkPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.datacenter.SubTabDataCenterPermissionPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.datacenter.SubTabDataCenterQosPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.datacenter.SubTabDataCenterQuotaPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.datacenter.SubTabDataCenterStoragePresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.disk.DiskSubTabPanelPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.disk.SubTabDiskGeneralPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.disk.SubTabDiskPermissionPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.disk.SubTabDiskStoragePresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.disk.SubTabDiskTemplatePresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.disk.SubTabDiskVmPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.errata.ErrataSubTabPanelPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.errata.SubTabEngineErrataDetailsPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.gluster.SubTabGlusterVolumeSnapshotPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.gluster.SubTabVolumeBrickPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.gluster.SubTabVolumeEventPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.gluster.SubTabVolumeGeneralPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.gluster.SubTabVolumeGeoRepPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.gluster.SubTabVolumeParameterPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.gluster.SubTabVolumePermissionPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.gluster.VolumeSubTabPanelPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.host.HostGeneralSubTabPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.host.HostSubTabPanelPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.host.SubTabHostAffinityLabelPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.host.SubTabHostBrickPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.host.SubTabHostDevicePresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.host.SubTabHostErrataPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.host.SubTabHostEventPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.host.SubTabHostGlusterStorageDevicesPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.host.SubTabHostGlusterSwiftPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.host.SubTabHostHookPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.host.SubTabHostInterfacePresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.host.SubTabHostPermissionPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.host.SubTabHostVmPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.network.NetworkSubTabPanelPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.network.SubTabNetworkClusterPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.network.SubTabNetworkExternalSubnetPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.network.SubTabNetworkGeneralPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.network.SubTabNetworkHostPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.network.SubTabNetworkPermissionPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.network.SubTabNetworkProfilePresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.network.SubTabNetworkTemplatePresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.network.SubTabNetworkVmPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.pool.PoolSubTabPanelPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.pool.SubTabPoolGeneralPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.pool.SubTabPoolPermissionPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.pool.SubTabPoolVmPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.profile.SubTabVnicProfilePermissionPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.profile.SubTabVnicProfileTemplatePresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.profile.SubTabVnicProfileVmPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.profile.VnicProfileSubTabPanelPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.provider.ProviderSubTabPanelPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.provider.SubTabProviderGeneralPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.provider.SubTabProviderNetworkPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.provider.SubTabProviderSecretPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.quota.QuotaSubTabPanelPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.quota.SubTabQuotaClusterPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.quota.SubTabQuotaEventPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.quota.SubTabQuotaPermissionPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.quota.SubTabQuotaStoragePresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.quota.SubTabQuotaTemplatePresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.quota.SubTabQuotaUserPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.quota.SubTabQuotaVmPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.storage.StorageSubTabPanelPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.storage.SubTabStorageDRPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.storage.SubTabStorageDataCenterPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.storage.SubTabStorageDiskPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.storage.SubTabStorageDiskProfilePresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.storage.SubTabStorageEventPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.storage.SubTabStorageGeneralPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.storage.SubTabStorageIsoPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.storage.SubTabStorageLeasePresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.storage.SubTabStoragePermissionPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.storage.SubTabStorageRegisterDiskImagePresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.storage.SubTabStorageRegisterTemplatePresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.storage.SubTabStorageRegisterVmPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.storage.SubTabStorageSnapshotPresenter;
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
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.virtualMachine.SubTabVirtualMachineAffinityGroupPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.virtualMachine.SubTabVirtualMachineAffinityLabelPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.virtualMachine.SubTabVirtualMachineApplicationPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.virtualMachine.SubTabVirtualMachineErrataPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.virtualMachine.SubTabVirtualMachineEventPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.virtualMachine.SubTabVirtualMachineGeneralPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.virtualMachine.SubTabVirtualMachineGuestContainerPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.virtualMachine.SubTabVirtualMachineGuestInfoPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.virtualMachine.SubTabVirtualMachineHostDevicePresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.virtualMachine.SubTabVirtualMachineNetworkInterfacePresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.virtualMachine.SubTabVirtualMachinePermissionPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.virtualMachine.SubTabVirtualMachineSnapshotPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.virtualMachine.SubTabVirtualMachineVirtualDiskPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.virtualMachine.SubTabVirtualMachineVmDevicePresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.virtualMachine.VirtualMachineSubTabPanelPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AboutPopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.HeaderView;
import org.ovirt.engine.ui.webadmin.section.main.view.MainClusterView;
import org.ovirt.engine.ui.webadmin.section.main.view.MainContentView;
import org.ovirt.engine.ui.webadmin.section.main.view.MainDataCenterView;
import org.ovirt.engine.ui.webadmin.section.main.view.MainDiskView;
import org.ovirt.engine.ui.webadmin.section.main.view.MainEngineErrataView;
import org.ovirt.engine.ui.webadmin.section.main.view.MainEventView;
import org.ovirt.engine.ui.webadmin.section.main.view.MainHostView;
import org.ovirt.engine.ui.webadmin.section.main.view.MainNetworkView;
import org.ovirt.engine.ui.webadmin.section.main.view.MainPoolView;
import org.ovirt.engine.ui.webadmin.section.main.view.MainProviderView;
import org.ovirt.engine.ui.webadmin.section.main.view.MainQuotaView;
import org.ovirt.engine.ui.webadmin.section.main.view.MainSectionView;
import org.ovirt.engine.ui.webadmin.section.main.view.MainSessionView;
import org.ovirt.engine.ui.webadmin.section.main.view.MainStorageView;
import org.ovirt.engine.ui.webadmin.section.main.view.MainTemplateView;
import org.ovirt.engine.ui.webadmin.section.main.view.MainUserView;
import org.ovirt.engine.ui.webadmin.section.main.view.MainVirtualMachineView;
import org.ovirt.engine.ui.webadmin.section.main.view.MainVnicProfileView;
import org.ovirt.engine.ui.webadmin.section.main.view.MainVolumeView;
import org.ovirt.engine.ui.webadmin.section.main.view.MenuView;
import org.ovirt.engine.ui.webadmin.section.main.view.NotificationView;
import org.ovirt.engine.ui.webadmin.section.main.view.SearchPanelView;
import org.ovirt.engine.ui.webadmin.section.main.view.overlay.BookmarkView;
import org.ovirt.engine.ui.webadmin.section.main.view.overlay.TagsView;
import org.ovirt.engine.ui.webadmin.section.main.view.overlay.TasksView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.AssignTagsPopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.HostErrataListWithDetailsPopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.ImportTemplatesPopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.ImportVmsPopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.NewNetworkPopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.PermissionsPopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.StorageQosRemovePopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.SystemPermissionsRemoveConfirmationPopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.VmErrataListWithDetailsPopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.bookmark.BookmarkPopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.cluster.AddDataCenterClusterPopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.cluster.ClusterManageNetworkPopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.cluster.ClusterPopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.cluster.ClusterWarningsPopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.cluster.GlusterHookContentPopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.cluster.GlusterHookResolveConflictsPopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.cluster.ManageGlusterSwiftPopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.cluster.NewClusterNetworkPopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.configure.ConfigurePopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.configure.RolePopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.datacenter.DataCenterForceRemovePopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.datacenter.DataCenterPopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.datacenter.EditDataCenterNetworkPopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.datacenter.EditNetworkPopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.datacenter.FindMultiStoragePopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.datacenter.FindSingleStoragePopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.datacenter.IscsiBondPopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.datacenter.NewDataCenterNetworkPopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.datacenter.RecoveryStorageConfirmationPopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.event.EventPopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.gluster.AddBrickPopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.gluster.BrickAdvancedDetailsPopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.gluster.CreateBrickPopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.gluster.DetachGlusterHostsPopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.gluster.GeoRepActionConfirmPopUpView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.gluster.GlusterClusterSnapshotConfigureOptionsPopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.gluster.GlusterVolumeGeoRepCreateSessionPopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.gluster.GlusterVolumeGeoReplicationSessionConfigPopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.gluster.GlusterVolumeSnapshotConfigureOptionsPopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.gluster.GlusterVolumeSnapshotCreatePopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.gluster.RemoveBrickPopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.gluster.RemoveBrickStatusPopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.gluster.ReplaceBrickPopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.gluster.ResetBrickPopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.gluster.VolumeGeoRepSessionDetailsPopUpView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.gluster.VolumeParameterPopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.gluster.VolumePopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.gluster.VolumeProfileStatisticsPopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.gluster.VolumeRebalanceStatusPopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.guide.GuidePopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.guide.MoveHostPopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.host.HostConfigureLocalStoragePopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.host.HostFenceAgentPopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.host.HostFenceProxyPopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.host.HostInstallPopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.host.HostPopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.host.HostSetupNetworksPopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.host.ManualFenceConfirmationPopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.host.MultipleHostsPopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.host.NetworkAttachmentPopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.host.SetupNetworksBondPopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.host.SetupNetworksLabelPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.host.SetupNetworksLabelPopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.host.VfsConfigPopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.instancetypes.InstanceTypesPopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.label.AffinityLabelPopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.macpool.SharedMacPoolPopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.networkQoS.NetworkQoSPopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.ova.ExportOvaPopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.pool.PoolEditPopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.pool.PoolNewPopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.profile.CpuProfilePopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.profile.DiskProfilePopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.profile.VnicProfilePopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.provider.ExternalSubnetPopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.provider.ImportNetworksPopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.provider.ProviderPopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.provider.ProviderSecretPopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.qos.CpuQosPopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.qos.HostNetworkQosPopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.qos.StorageQosPopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.quota.ChangeQuotaPopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.quota.EditQuotaClusterPopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.quota.EditQuotaStoragePopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.quota.QuotaPopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.scheduling.AffinityGroupPopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.scheduling.ClusterPolicyPopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.scheduling.ManagePolicyUnitPopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.storage.DisksAllocationPopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.storage.FindMultiDcPopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.storage.FindSingleDcPopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.storage.ImportExportImagePopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.storage.RegisterTemplatePopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.storage.RegisterVmPopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.storage.StorageDRPopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.storage.StorageDestroyPopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.storage.StorageForceCreatePopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.storage.StoragePopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.storage.StorageRemovePopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.storage.UploadImagePopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.storage.backup.ImportCloneDialogPopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.storage.backup.ImportTemplatePopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.storage.backup.ImportVmFromExportDomainPopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.tag.TagPopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.template.ImportTemplateFromOvaPopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.template.TemplateEditPopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.template.TemplateInterfacePopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.user.ManageEventsPopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.user.UserRolesPopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.vm.AddVmHostDevicePopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.vm.CloneVmPopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.vm.ImportVmFromExternalProviderPopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.vm.SingleSelectionVmDiskAttachPopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.vm.VmChangeCDPopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.vm.VmClonePopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.vm.VmDiskAttachPopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.vm.VmDiskPopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.vm.VmDiskRemovePopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.vm.VmDiskSparsifyPopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.vm.VmExportPopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.vm.VmHighPerformanceConfigurationPopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.vm.VmInterfacePopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.vm.VmMakeTemplatePopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.vm.VmNextRunConfigurationPopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.vm.VmPopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.vm.VmRemovePopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.vm.VmRemovePopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.vm.VmRepinHostPopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.vm.VmRunOncePopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.vm.VmSnapshotCreatePopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.vm.VmSnapshotCustomPreviewPopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.vm.VmSnapshotPreviewPopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.vm.VncInfoPopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.vm.register.VnicProfileMappingPopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.cluster.ClusterSubTabPanelView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.cluster.SubTabClusterAffinityGroupView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.cluster.SubTabClusterAffinityLabelView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.cluster.SubTabClusterCpuProfileView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.cluster.SubTabClusterEventView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.cluster.SubTabClusterGeneralView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.cluster.SubTabClusterGlusterHookView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.cluster.SubTabClusterHostView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.cluster.SubTabClusterNetworkView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.cluster.SubTabClusterPermissionView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.cluster.SubTabClusterServiceView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.cluster.SubTabClusterVmView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.datacenter.DataCenterSubTabPanelView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.datacenter.SubTabDataCenterClusterView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.datacenter.SubTabDataCenterEventView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.datacenter.SubTabDataCenterIscsiBondView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.datacenter.SubTabDataCenterNetworkView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.datacenter.SubTabDataCenterPermissionView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.datacenter.SubTabDataCenterQosView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.datacenter.SubTabDataCenterQuotaView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.datacenter.SubTabDataCenterStorageView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.disk.DiskSubTabPanelView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.disk.SubTabDiskGeneralView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.disk.SubTabDiskPermissionView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.disk.SubTabDiskStorageView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.disk.SubTabDiskTemplateView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.disk.SubTabDiskVmView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.errata.ErrataSubTabPanelView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.errata.SubTabEngineErrataDetailsView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.gluster.SubTabGlusterVolumeSnapshotView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.gluster.SubTabVolumeBrickView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.gluster.SubTabVolumeEventView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.gluster.SubTabVolumeGeneralView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.gluster.SubTabVolumeGeoRepView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.gluster.SubTabVolumeParameterView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.gluster.SubTabVolumePermissionView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.gluster.VolumeSubTabPanelView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.host.HostGeneralSubTabView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.host.HostSubTabPanelView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.host.SubTabHostAffinityLabelView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.host.SubTabHostBrickView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.host.SubTabHostDeviceView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.host.SubTabHostErrataView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.host.SubTabHostEventView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.host.SubTabHostGlusterStorageDevicesView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.host.SubTabHostGlusterSwiftView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.host.SubTabHostHookView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.host.SubTabHostInterfaceView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.host.SubTabHostPermissionView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.host.SubTabHostVmView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.network.NetworkSubTabPanelView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.network.SubTabNetworkClusterView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.network.SubTabNetworkExternalSubnetView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.network.SubTabNetworkGeneralView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.network.SubTabNetworkHostView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.network.SubTabNetworkPermissionView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.network.SubTabNetworkProfileView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.network.SubTabNetworkTemplateView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.network.SubTabNetworkVmView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.pool.PoolSubTabPanelView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.pool.SubTabPoolGeneralView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.pool.SubTabPoolPermissionView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.pool.SubTabPoolVmView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.profile.SubTabVnicProfilePermissionView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.profile.SubTabVnicProfileTemplateView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.profile.SubTabVnicProfileVmView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.profile.VnicProfileSubTabPanelView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.provider.ProviderSubTabPanelView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.provider.SubTabProviderGeneralView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.provider.SubTabProviderNetworkView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.provider.SubTabProviderSecretView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.quota.QuotaSubTabPanelView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.quota.SubTabQuotaClusterView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.quota.SubTabQuotaEventView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.quota.SubTabQuotaPermissionView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.quota.SubTabQuotaStorageView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.quota.SubTabQuotaTemplateView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.quota.SubTabQuotaUserView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.quota.SubTabQuotaVmView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.storage.StorageSubTabPanelView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.storage.SubTabStorageDRView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.storage.SubTabStorageDataCenterView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.storage.SubTabStorageDiskProfileView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.storage.SubTabStorageDiskView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.storage.SubTabStorageEventView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.storage.SubTabStorageGeneralView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.storage.SubTabStorageIsoView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.storage.SubTabStorageLeaseView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.storage.SubTabStoragePermissionView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.storage.SubTabStorageRegisterDiskImageView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.storage.SubTabStorageRegisterTemplateView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.storage.SubTabStorageRegisterVmView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.storage.SubTabStorageSnapshotView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.storage.SubTabStorageTemplateBackupView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.storage.SubTabStorageTemplateView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.storage.SubTabStorageVmBackupView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.storage.SubTabStorageVmView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.template.SubTabTemplateDiskView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.template.SubTabTemplateEventView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.template.SubTabTemplateGeneralView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.template.SubTabTemplateInterfaceView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.template.SubTabTemplatePermissionView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.template.SubTabTemplateStorageView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.template.SubTabTemplateVmView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.template.TemplateSubTabPanelView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.user.SubTabUserEventNotifierView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.user.SubTabUserEventView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.user.SubTabUserGeneralView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.user.SubTabUserGroupView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.user.SubTabUserPermissionView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.user.SubTabUserQuotaView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.user.UserSubTabPanelView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.virtualMachine.SubTabVirtualMachineAffinityGroupView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.virtualMachine.SubTabVirtualMachineAffinityLabelView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.virtualMachine.SubTabVirtualMachineApplicationView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.virtualMachine.SubTabVirtualMachineErrataView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.virtualMachine.SubTabVirtualMachineEventView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.virtualMachine.SubTabVirtualMachineGeneralView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.virtualMachine.SubTabVirtualMachineGuestContainerView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.virtualMachine.SubTabVirtualMachineGuestInfoView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.virtualMachine.SubTabVirtualMachineHostDeviceView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.virtualMachine.SubTabVirtualMachineNetworkInterfaceView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.virtualMachine.SubTabVirtualMachinePermissionView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.virtualMachine.SubTabVirtualMachineSnapshotView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.virtualMachine.SubTabVirtualMachineVirtualDiskView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.virtualMachine.SubTabVirtualMachineVmDevicesView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.virtualMachine.VirtualMachineSubTabPanelView;

import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

/**
 * GIN module containing WebAdmin GWTP presenter bindings.
 */
public class PresenterModule extends BasePresenterModule {

    @Override
    protected void configure() {
        // Common stuff
        bindCommonPresenters();

        // Menu
        bindSingletonPresenterWidget(MenuPresenterWidget.class,
                MenuPresenterWidget.ViewDef.class,
                MenuView.class);
        bind(MenuDetailsProvider.class).to(MenuPresenterWidget.class).in(Singleton.class);

        // Main section: common stuff
        bindPresenter(MainSectionPresenter.class,
                MainSectionPresenter.ViewDef.class,
                MainSectionView.class,
                MainSectionPresenter.ProxyDef.class);
        bindPresenter(MainContentPresenter.class,
                MainContentPresenter.ViewDef.class,
                MainContentView.class,
                MainContentPresenter.ProxyDef.class);
        bindSingletonPresenterWidget(HeaderPresenterWidget.class,
                HeaderPresenterWidget.ViewDef.class,
                HeaderView.class);
        bindSingletonPresenterWidget(NotificationPresenterWidget.class,
                NotificationPresenterWidget.ViewDef.class,
                NotificationView.class);
        bindPresenterWidget(AboutPopupPresenterWidget.class,
                AboutPopupPresenterWidget.ViewDef.class,
                AboutPopupView.class);
        bindPresenterWidget(ConfigurePopupPresenterWidget.class,
                ConfigurePopupPresenterWidget.ViewDef.class,
                ConfigurePopupView.class);
        bindPresenterWidget(RolePopupPresenterWidget.class,
                RolePopupPresenterWidget.ViewDef.class,
                RolePopupView.class);
        bindPresenterWidget(ClusterPolicyPopupPresenterWidget.class,
                ClusterPolicyPopupPresenterWidget.ViewDef.class,
                ClusterPolicyPopupView.class);
        bindPresenterWidget(ManagePolicyUnitPopupPresenterWidget.class,
                ManagePolicyUnitPopupPresenterWidget.ViewDef.class,
                ManagePolicyUnitPopupView.class);
        bindPresenterWidget(SharedMacPoolPopupPresenterWidget.class,
                SharedMacPoolPopupPresenterWidget.ViewDef.class,
                SharedMacPoolPopupView.class);
        bindPresenterWidget(AffinityGroupPopupPresenterWidget.class,
                AffinityGroupPopupPresenterWidget.ViewDef.class,
                AffinityGroupPopupView.class);
        bindPresenterWidget(AffinityLabelPopupPresenterWidget.class,
                AffinityLabelPopupPresenterWidget.ViewDef.class,
                AffinityLabelPopupView.class);

        // Main section: main tabs
        bindPresenter(MainDataCenterPresenter.class,
                MainDataCenterPresenter.ViewDef.class,
                MainDataCenterView.class,
                MainDataCenterPresenter.ProxyDef.class);
        bindPresenter(MainClusterPresenter.class,
                MainClusterPresenter.ViewDef.class,
                MainClusterView.class,
                MainClusterPresenter.ProxyDef.class);
        bindPresenter(MainHostPresenter.class,
                MainHostPresenter.ViewDef.class,
                MainHostView.class,
                MainHostPresenter.ProxyDef.class);
        bindPresenter(MainNetworkPresenter.class,
                MainNetworkPresenter.ViewDef.class,
                MainNetworkView.class,
                MainNetworkPresenter.ProxyDef.class);
        bindPresenter(MainVnicProfilePresenter.class,
                MainVnicProfilePresenter.ViewDef.class,
                MainVnicProfileView.class,
                MainVnicProfilePresenter.ProxyDef.class);
        bindPresenter(MainProviderPresenter.class,
                MainProviderPresenter.ViewDef.class,
                MainProviderView.class,
                MainProviderPresenter.ProxyDef.class);
        bindPresenter(MainEngineErrataPresenter.class,
                MainEngineErrataPresenter.ViewDef.class,
                MainEngineErrataView.class,
                MainEngineErrataPresenter.ProxyDef.class);
        bindPresenter(MainSessionPresenter.class,
                MainSessionPresenter.ViewDef.class,
                MainSessionView.class,
                MainSessionPresenter.ProxyDef.class);
        bindPresenter(MainStoragePresenter.class,
                MainStoragePresenter.ViewDef.class,
                MainStorageView.class,
                MainStoragePresenter.ProxyDef.class);
        bindPresenter(MainVirtualMachinePresenter.class,
                MainVirtualMachinePresenter.ViewDef.class,
                MainVirtualMachineView.class,
                MainVirtualMachinePresenter.ProxyDef.class);
        bindPresenter(MainPoolPresenter.class,
                MainPoolPresenter.ViewDef.class,
                MainPoolView.class,
                MainPoolPresenter.ProxyDef.class);
        bindPresenter(MainTemplatePresenter.class,
                MainTemplatePresenter.ViewDef.class,
                MainTemplateView.class,
                MainTemplatePresenter.ProxyDef.class);
        bindPresenter(MainUserPresenter.class,
                MainUserPresenter.ViewDef.class,
                MainUserView.class,
                MainUserPresenter.ProxyDef.class);
        bindPresenter(MainEventPresenter.class,
                MainEventPresenter.ViewDef.class,
                MainEventView.class,
                MainEventPresenter.ProxyDef.class);
        bindPresenter(MainQuotaPresenter.class,
                MainQuotaPresenter.ViewDef.class,
                MainQuotaView.class,
                MainQuotaPresenter.ProxyDef.class);
        bindPresenter(MainVolumePresenter.class,
                MainVolumePresenter.ViewDef.class,
                MainVolumeView.class,
                MainVolumePresenter.ProxyDef.class);
        bindPresenter(MainDiskPresenter.class,
                MainDiskPresenter.ViewDef.class,
                MainDiskView.class,
                MainDiskPresenter.ProxyDef.class);

        // Main section: sub tabs

        // DataCenter
        bindPresenter(DataCenterSubTabPanelPresenter.class,
                DataCenterSubTabPanelPresenter.ViewDef.class,
                DataCenterSubTabPanelView.class,
                DataCenterSubTabPanelPresenter.ProxyDef.class);
        bindPresenter(SubTabDataCenterStoragePresenter.class,
                SubTabDataCenterStoragePresenter.ViewDef.class,
                SubTabDataCenterStorageView.class,
                SubTabDataCenterStoragePresenter.ProxyDef.class);
        bindPresenter(SubTabDataCenterIscsiBondPresenter.class,
                SubTabDataCenterIscsiBondPresenter.ViewDef.class,
                SubTabDataCenterIscsiBondView.class,
                SubTabDataCenterIscsiBondPresenter.ProxyDef.class);
        bindPresenter(SubTabDataCenterNetworkPresenter.class,
                SubTabDataCenterNetworkPresenter.ViewDef.class,
                SubTabDataCenterNetworkView.class,
                SubTabDataCenterNetworkPresenter.ProxyDef.class);
        bindPresenter(SubTabDataCenterQosPresenter.class,
                SubTabDataCenterQosPresenter.ViewDef.class,
                SubTabDataCenterQosView.class,
                SubTabDataCenterQosPresenter.ProxyDef.class);
        bindPresenter(SubTabDataCenterClusterPresenter.class,
                SubTabDataCenterClusterPresenter.ViewDef.class,
                SubTabDataCenterClusterView.class,
                SubTabDataCenterClusterPresenter.ProxyDef.class);
        bindPresenter(SubTabDataCenterQuotaPresenter.class,
                SubTabDataCenterQuotaPresenter.ViewDef.class,
                SubTabDataCenterQuotaView.class,
                SubTabDataCenterQuotaPresenter.ProxyDef.class);
        bindPresenter(SubTabDataCenterPermissionPresenter.class,
                SubTabDataCenterPermissionPresenter.ViewDef.class,
                SubTabDataCenterPermissionView.class,
                SubTabDataCenterPermissionPresenter.ProxyDef.class);
        bindPresenter(SubTabDataCenterEventPresenter.class,
                SubTabDataCenterEventPresenter.ViewDef.class,
                SubTabDataCenterEventView.class,
                SubTabDataCenterEventPresenter.ProxyDef.class);
        bindPresenterWidget(RecoveryStoragePopupPresenterWidget.class,
                RecoveryStoragePopupPresenterWidget.ViewDef.class,
                RecoveryStorageConfirmationPopupView.class);
        bindSingletonPresenterWidget(
                new TypeLiteral<SearchPanelPresenterWidget<StoragePool, DataCenterListModel>>(){},
                new TypeLiteral<SearchPanelPresenterWidget.ViewDef<DataCenterListModel>>(){},
                new TypeLiteral<SearchPanelView<DataCenterListModel>>(){});
        bindPresenterWidget(
                new TypeLiteral<OvirtBreadCrumbsPresenterWidget<StoragePool, DataCenterListModel>>(){},
                new TypeLiteral<OvirtBreadCrumbsPresenterWidget.ViewDef<StoragePool>>(){},
                new TypeLiteral<OvirtBreadCrumbsView<StoragePool, DataCenterListModel>>(){});
        bindActionPanel(new TypeLiteral<ActionPanelPresenterWidget.ViewDef<Void, StoragePool>>(){},
            new TypeLiteral<ActionPanelView<Void, StoragePool>>(){});
        bindActionPanel(new TypeLiteral<DetailActionPanelPresenterWidget.ViewDef<Void, StoragePool>>(){},
                new TypeLiteral<DetailActionPanelView<Void, StoragePool>>(){});

        // Storage
        bindPresenter(StorageSubTabPanelPresenter.class,
                StorageSubTabPanelPresenter.ViewDef.class,
                StorageSubTabPanelView.class,
                StorageSubTabPanelPresenter.ProxyDef.class);
        bindPresenter(SubTabStorageGeneralPresenter.class,
                SubTabStorageGeneralPresenter.ViewDef.class,
                SubTabStorageGeneralView.class,
                SubTabStorageGeneralPresenter.ProxyDef.class);
        bindPresenter(SubTabStorageDataCenterPresenter.class,
                SubTabStorageDataCenterPresenter.ViewDef.class,
                SubTabStorageDataCenterView.class,
                SubTabStorageDataCenterPresenter.ProxyDef.class);
        bindPresenter(SubTabStorageVmBackupPresenter.class,
                SubTabStorageVmBackupPresenter.ViewDef.class,
                SubTabStorageVmBackupView.class,
                SubTabStorageVmBackupPresenter.ProxyDef.class);
        bindPresenter(SubTabStorageTemplateBackupPresenter.class,
                SubTabStorageTemplateBackupPresenter.ViewDef.class,
                SubTabStorageTemplateBackupView.class,
                SubTabStorageTemplateBackupPresenter.ProxyDef.class);
        bindPresenter(SubTabStorageRegisterVmPresenter.class,
                SubTabStorageRegisterVmPresenter.ViewDef.class,
                SubTabStorageRegisterVmView.class,
                SubTabStorageRegisterVmPresenter.ProxyDef.class);
        bindPresenter(SubTabStorageRegisterTemplatePresenter.class,
                SubTabStorageRegisterTemplatePresenter.ViewDef.class,
                SubTabStorageRegisterTemplateView.class,
                SubTabStorageRegisterTemplatePresenter.ProxyDef.class);
        bindPresenter(SubTabStorageVmPresenter.class,
                SubTabStorageVmPresenter.ViewDef.class,
                SubTabStorageVmView.class,
                SubTabStorageVmPresenter.ProxyDef.class);
        bindPresenter(SubTabStorageTemplatePresenter.class,
                SubTabStorageTemplatePresenter.ViewDef.class,
                SubTabStorageTemplateView.class,
                SubTabStorageTemplatePresenter.ProxyDef.class);
        bindPresenter(SubTabStorageIsoPresenter.class,
                SubTabStorageIsoPresenter.ViewDef.class,
                SubTabStorageIsoView.class,
                SubTabStorageIsoPresenter.ProxyDef.class);
        bindPresenter(SubTabStorageDiskPresenter.class,
                SubTabStorageDiskPresenter.ViewDef.class,
                SubTabStorageDiskView.class,
                SubTabStorageDiskPresenter.ProxyDef.class);
        bindPresenter(SubTabStorageRegisterDiskImagePresenter.class,
                SubTabStorageRegisterDiskImagePresenter.ViewDef.class,
                SubTabStorageRegisterDiskImageView.class,
                SubTabStorageRegisterDiskImagePresenter.ProxyDef.class);
        bindPresenter(SubTabStorageSnapshotPresenter.class,
                SubTabStorageSnapshotPresenter.ViewDef.class,
                SubTabStorageSnapshotView.class,
                SubTabStorageSnapshotPresenter.ProxyDef.class);
        bindPresenter(SubTabStorageDRPresenter.class,
                SubTabStorageDRPresenter.ViewDef.class,
                SubTabStorageDRView.class,
                SubTabStorageDRPresenter.ProxyDef.class);
        bindPresenter(SubTabStorageLeasePresenter.class,
                SubTabStorageLeasePresenter.ViewDef.class,
                SubTabStorageLeaseView.class,
                SubTabStorageLeasePresenter.ProxyDef.class);
        bindPresenter(SubTabStoragePermissionPresenter.class,
                SubTabStoragePermissionPresenter.ViewDef.class,
                SubTabStoragePermissionView.class,
                SubTabStoragePermissionPresenter.ProxyDef.class);
        bindPresenter(SubTabStorageEventPresenter.class,
                SubTabStorageEventPresenter.ViewDef.class,
                SubTabStorageEventView.class,
                SubTabStorageEventPresenter.ProxyDef.class);
        bindPresenter(SubTabStorageDiskProfilePresenter.class,
                SubTabStorageDiskProfilePresenter.ViewDef.class,
                SubTabStorageDiskProfileView.class,
                SubTabStorageDiskProfilePresenter.ProxyDef.class);
        bindSingletonPresenterWidget(
                new TypeLiteral<SearchPanelPresenterWidget<StorageDomain, StorageListModel>>(){},
                new TypeLiteral<SearchPanelPresenterWidget.ViewDef<StorageListModel>>(){},
                new TypeLiteral<SearchPanelView<StorageListModel>>(){});
        bindPresenterWidget(
                new TypeLiteral<OvirtBreadCrumbsPresenterWidget<StorageDomain, StorageListModel>>(){},
                new TypeLiteral<OvirtBreadCrumbsPresenterWidget.ViewDef<StorageDomain>>(){},
                new TypeLiteral<OvirtBreadCrumbsView<StorageDomain, StorageListModel>>(){});
        bindActionPanel(new TypeLiteral<ActionPanelPresenterWidget.ViewDef<Void, StorageDomain>>(){},
                new TypeLiteral<ActionPanelView<Void, StorageDomain>>(){});
        bindActionPanel(new TypeLiteral<ActionPanelPresenterWidget.ViewDef<StorageDomain, StorageDomain>>(){},
            new TypeLiteral<ActionPanelView<StorageDomain, StorageDomain>>(){});
        bindActionPanel(new TypeLiteral<DetailActionPanelPresenterWidget.ViewDef<StorageDomain, StorageDomain>>(){},
                new TypeLiteral<DetailActionPanelView<StorageDomain, StorageDomain>>(){});

        // Cluster
        bindPresenter(ClusterSubTabPanelPresenter.class,
                ClusterSubTabPanelPresenter.ViewDef.class,
                ClusterSubTabPanelView.class,
                ClusterSubTabPanelPresenter.ProxyDef.class);
        bindPresenter(SubTabClusterGeneralPresenter.class,
                SubTabClusterGeneralPresenter.ViewDef.class,
                SubTabClusterGeneralView.class,
                SubTabClusterGeneralPresenter.ProxyDef.class);
        bindPresenter(SubTabClusterHostPresenter.class,
                SubTabClusterHostPresenter.ViewDef.class,
                SubTabClusterHostView.class,
                SubTabClusterHostPresenter.ProxyDef.class);
        bindPresenter(SubTabClusterVmPresenter.class,
                SubTabClusterVmPresenter.ViewDef.class,
                SubTabClusterVmView.class,
                SubTabClusterVmPresenter.ProxyDef.class);
        bindPresenter(SubTabClusterNetworkPresenter.class,
                SubTabClusterNetworkPresenter.ViewDef.class,
                SubTabClusterNetworkView.class,
                SubTabClusterNetworkPresenter.ProxyDef.class);
        bindPresenter(SubTabClusterServicePresenter.class,
                SubTabClusterServicePresenter.ViewDef.class,
                SubTabClusterServiceView.class,
                SubTabClusterServicePresenter.ProxyDef.class);
        bindPresenter(SubTabClusterGlusterHookPresenter.class,
                SubTabClusterGlusterHookPresenter.ViewDef.class,
                SubTabClusterGlusterHookView.class,
                SubTabClusterGlusterHookPresenter.ProxyDef.class);
        bindPresenter(SubTabClusterAffinityGroupPresenter.class,
                SubTabClusterAffinityGroupPresenter.ViewDef.class,
                SubTabClusterAffinityGroupView.class,
                SubTabClusterAffinityGroupPresenter.ProxyDef.class);
        bindPresenter(SubTabClusterPermissionPresenter.class,
                SubTabClusterPermissionPresenter.ViewDef.class,
                SubTabClusterPermissionView.class,
                SubTabClusterPermissionPresenter.ProxyDef.class);
        bindPresenter(SubTabClusterCpuProfilePresenter.class,
                SubTabClusterCpuProfilePresenter.ViewDef.class,
                SubTabClusterCpuProfileView.class,
                SubTabClusterCpuProfilePresenter.ProxyDef.class);
        bindPresenterWidget(GlusterVolumeSnapshotConfigureOptionsPopupPresenterWidget.class,
                GlusterVolumeSnapshotConfigureOptionsPopupPresenterWidget.ViewDef.class,
                GlusterVolumeSnapshotConfigureOptionsPopupView.class);
        bindPresenterWidget(GlusterClusterSnapshotConfigureOptionsPopupPresenterWidget.class,
                GlusterClusterSnapshotConfigureOptionsPopupPresenterWidget.ViewDef.class,
                GlusterClusterSnapshotConfigureOptionsPopupView.class);
        bindPresenterWidget(GlusterVolumeSnapshotCreatePopupPresenterWidget.class,
                GlusterVolumeSnapshotCreatePopupPresenterWidget.ViewDef.class,
                GlusterVolumeSnapshotCreatePopupView.class);
        bindPresenter(SubTabClusterAffinityLabelPresenter.class,
                SubTabClusterAffinityLabelPresenter.ViewDef.class,
                SubTabClusterAffinityLabelView.class,
                SubTabClusterAffinityLabelPresenter.ProxyDef.class);
        bindPresenter(SubTabClusterEventPresenter.class,
                SubTabClusterEventPresenter.ViewDef.class,
                SubTabClusterEventView.class,
                SubTabClusterEventPresenter.ProxyDef.class);
        bindSingletonPresenterWidget(
                new TypeLiteral<SearchPanelPresenterWidget<Cluster, ClusterListModel<Void>>>(){},
                new TypeLiteral<SearchPanelPresenterWidget.ViewDef<ClusterListModel<Void>>>(){},
                new TypeLiteral<SearchPanelView<ClusterListModel<Void>>>(){});
        bindPresenterWidget(
                new TypeLiteral<OvirtBreadCrumbsPresenterWidget<Cluster, ClusterListModel<Void>>>(){},
                new TypeLiteral<OvirtBreadCrumbsPresenterWidget.ViewDef<Cluster>>(){},
                new TypeLiteral<OvirtBreadCrumbsView<Cluster, ClusterListModel<Void>>>(){});
        bindActionPanel(new TypeLiteral<ActionPanelPresenterWidget.ViewDef<Void, Cluster>>(){},
            new TypeLiteral<ActionPanelView<Void, Cluster>>(){});
        bindActionPanel(new TypeLiteral<DetailActionPanelPresenterWidget.ViewDef<Void, Cluster>>(){},
                new TypeLiteral<DetailActionPanelView<Void, Cluster>>(){});

        // Host
        bindPresenter(HostSubTabPanelPresenter.class,
                HostSubTabPanelPresenter.ViewDef.class,
                HostSubTabPanelView.class,
                HostSubTabPanelPresenter.ProxyDef.class);
        bindPresenter(HostGeneralSubTabPresenter.class,
                HostGeneralSubTabPresenter.ViewDef.class,
                HostGeneralSubTabView.class,
                HostGeneralSubTabPresenter.ProxyDef.class);
        bindPresenter(SubTabHostErrataPresenter.class,
                SubTabHostErrataPresenter.ViewDef.class,
                SubTabHostErrataView.class,
                SubTabHostErrataPresenter.ProxyDef.class);
        bindPresenter(SubTabHostVmPresenter.class,
                SubTabHostVmPresenter.ViewDef.class,
                SubTabHostVmView.class,
                SubTabHostVmPresenter.ProxyDef.class);
        bindPresenter(SubTabHostInterfacePresenter.class,
                SubTabHostInterfacePresenter.ViewDef.class,
                SubTabHostInterfaceView.class,
                SubTabHostInterfacePresenter.ProxyDef.class);
        bindPresenter(SubTabHostDevicePresenter.class,
                SubTabHostDevicePresenter.ViewDef.class,
                SubTabHostDeviceView.class,
                SubTabHostDevicePresenter.ProxyDef.class);
        bindPresenter(SubTabHostHookPresenter.class,
                SubTabHostHookPresenter.ViewDef.class,
                SubTabHostHookView.class,
                SubTabHostHookPresenter.ProxyDef.class);
        bindPresenter(SubTabHostGlusterSwiftPresenter.class,
                SubTabHostGlusterSwiftPresenter.ViewDef.class,
                SubTabHostGlusterSwiftView.class,
                SubTabHostGlusterSwiftPresenter.ProxyDef.class);
        bindPresenter(SubTabHostPermissionPresenter.class,
                SubTabHostPermissionPresenter.ViewDef.class,
                SubTabHostPermissionView.class,
                SubTabHostPermissionPresenter.ProxyDef.class);
        bindPresenter(SubTabHostEventPresenter.class,
                SubTabHostEventPresenter.ViewDef.class,
                SubTabHostEventView.class,
                SubTabHostEventPresenter.ProxyDef.class);
        bindPresenter(SubTabHostBrickPresenter.class,
                SubTabHostBrickPresenter.ViewDef.class,
                SubTabHostBrickView.class,
                SubTabHostBrickPresenter.ProxyDef.class);
        bindPresenter(SubTabHostGlusterStorageDevicesPresenter.class,
                SubTabHostGlusterStorageDevicesPresenter.ViewDef.class,
                SubTabHostGlusterStorageDevicesView.class,
                SubTabHostGlusterStorageDevicesPresenter.ProxyDef.class);
        bindSingletonPresenterWidget(
                new TypeLiteral<SearchPanelPresenterWidget<VDS, HostListModel<Void>>>(){},
                new TypeLiteral<SearchPanelPresenterWidget.ViewDef<HostListModel<Void>>>(){},
                new TypeLiteral<SearchPanelView<HostListModel<Void>>>(){});
        bindPresenterWidget(
                new TypeLiteral<OvirtBreadCrumbsPresenterWidget<VDS, HostListModel<Void>>>(){},
                new TypeLiteral<OvirtBreadCrumbsPresenterWidget.ViewDef<VDS>>(){},
                new TypeLiteral<OvirtBreadCrumbsView<VDS, HostListModel<Void>>>(){});
        bindPresenter(SubTabHostAffinityLabelPresenter.class,
                SubTabHostAffinityLabelPresenter.ViewDef.class,
                SubTabHostAffinityLabelView.class,
                SubTabHostAffinityLabelPresenter.ProxyDef.class);
        bindActionPanel(new TypeLiteral<ActionPanelPresenterWidget.ViewDef<Void, VDS>>(){},
            new TypeLiteral<ActionPanelView<Void, VDS>>(){});

        // VirtualMachine
        bindPresenter(VirtualMachineSubTabPanelPresenter.class,
                VirtualMachineSubTabPanelPresenter.ViewDef.class,
                VirtualMachineSubTabPanelView.class,
                VirtualMachineSubTabPanelPresenter.ProxyDef.class);
        bindPresenter(SubTabVirtualMachineGeneralPresenter.class,
                SubTabVirtualMachineGeneralPresenter.ViewDef.class,
                SubTabVirtualMachineGeneralView.class,
                SubTabVirtualMachineGeneralPresenter.ProxyDef.class);
        bindPresenter(SubTabVirtualMachineNetworkInterfacePresenter.class,
                SubTabVirtualMachineNetworkInterfacePresenter.ViewDef.class,
                SubTabVirtualMachineNetworkInterfaceView.class,
                SubTabVirtualMachineNetworkInterfacePresenter.ProxyDef.class);
        bindPresenter(SubTabVirtualMachineVirtualDiskPresenter.class,
                SubTabVirtualMachineVirtualDiskPresenter.ViewDef.class,
                SubTabVirtualMachineVirtualDiskView.class,
                SubTabVirtualMachineVirtualDiskPresenter.ProxyDef.class);
        bindPresenter(SubTabVirtualMachineSnapshotPresenter.class,
                SubTabVirtualMachineSnapshotPresenter.ViewDef.class,
                SubTabVirtualMachineSnapshotView.class,
                SubTabVirtualMachineSnapshotPresenter.ProxyDef.class);
        bindPresenter(SubTabVirtualMachineApplicationPresenter.class,
                SubTabVirtualMachineApplicationPresenter.ViewDef.class,
                SubTabVirtualMachineApplicationView.class,
                SubTabVirtualMachineApplicationPresenter.ProxyDef.class);
        bindPresenter(SubTabVirtualMachineGuestContainerPresenter.class,
                SubTabVirtualMachineGuestContainerPresenter.ViewDef.class,
                SubTabVirtualMachineGuestContainerView.class,
                SubTabVirtualMachineGuestContainerPresenter.ProxyDef.class);
        bindPresenter(SubTabVirtualMachineVmDevicePresenter.class,
                SubTabVirtualMachineVmDevicePresenter.ViewDef.class,
                SubTabVirtualMachineVmDevicesView.class,
                SubTabVirtualMachineVmDevicePresenter.ProxyDef.class);
        bindPresenter(SubTabVirtualMachineAffinityGroupPresenter.class,
                SubTabVirtualMachineAffinityGroupPresenter.ViewDef.class,
                SubTabVirtualMachineAffinityGroupView.class,
                SubTabVirtualMachineAffinityGroupPresenter.ProxyDef.class);
        bindPresenter(SubTabVirtualMachinePermissionPresenter.class,
                SubTabVirtualMachinePermissionPresenter.ViewDef.class,
                SubTabVirtualMachinePermissionView.class,
                SubTabVirtualMachinePermissionPresenter.ProxyDef.class);
        bindPresenter(SubTabVirtualMachineGuestInfoPresenter.class,
                SubTabVirtualMachineGuestInfoPresenter.ViewDef.class,
                SubTabVirtualMachineGuestInfoView.class,
                SubTabVirtualMachineGuestInfoPresenter.ProxyDef.class);
        bindPresenter(SubTabVirtualMachineEventPresenter.class,
                SubTabVirtualMachineEventPresenter.ViewDef.class,
                SubTabVirtualMachineEventView.class,
                SubTabVirtualMachineEventPresenter.ProxyDef.class);
        bindPresenter(SubTabVirtualMachineHostDevicePresenter.class,
                SubTabVirtualMachineHostDevicePresenter.ViewDef.class,
                SubTabVirtualMachineHostDeviceView.class,
                SubTabVirtualMachineHostDevicePresenter.ProxyDef.class);
        bindPresenter(SubTabVirtualMachineErrataPresenter.class,
                SubTabVirtualMachineErrataPresenter.ViewDef.class,
                SubTabVirtualMachineErrataView.class,
                SubTabVirtualMachineErrataPresenter.ProxyDef.class);
        bindSingletonPresenterWidget(
                new TypeLiteral<SearchPanelPresenterWidget<VM, VmListModel<Void>>>(){},
                new TypeLiteral<SearchPanelPresenterWidget.ViewDef<VmListModel<Void>>>(){},
                new TypeLiteral<SearchPanelView<VmListModel<Void>>>(){});
        bindPresenterWidget(
                new TypeLiteral<OvirtBreadCrumbsPresenterWidget<VM, VmListModel<Void>>>(){},
                new TypeLiteral<OvirtBreadCrumbsPresenterWidget.ViewDef<VM>>(){},
                new TypeLiteral<OvirtBreadCrumbsView<VM, VmListModel<Void>>>(){});
        bindPresenter(SubTabVirtualMachineAffinityLabelPresenter.class,
                SubTabVirtualMachineAffinityLabelPresenter.ViewDef.class,
                SubTabVirtualMachineAffinityLabelView.class,
                SubTabVirtualMachineAffinityLabelPresenter.ProxyDef.class);
        bindActionPanel(new TypeLiteral<ActionPanelPresenterWidget.ViewDef<Void, VM>>(){},
            new TypeLiteral<ActionPanelView<Void, VM>>(){});

        // Pool
        bindPresenter(PoolSubTabPanelPresenter.class,
                PoolSubTabPanelPresenter.ViewDef.class,
                PoolSubTabPanelView.class,
                PoolSubTabPanelPresenter.ProxyDef.class);
        bindPresenter(SubTabPoolGeneralPresenter.class,
                SubTabPoolGeneralPresenter.ViewDef.class,
                SubTabPoolGeneralView.class,
                SubTabPoolGeneralPresenter.ProxyDef.class);
        bindPresenter(SubTabPoolVmPresenter.class,
                SubTabPoolVmPresenter.ViewDef.class,
                SubTabPoolVmView.class,
                SubTabPoolVmPresenter.ProxyDef.class);
        bindPresenter(SubTabPoolPermissionPresenter.class,
                SubTabPoolPermissionPresenter.ViewDef.class,
                SubTabPoolPermissionView.class,
                SubTabPoolPermissionPresenter.ProxyDef.class);
        bindPresenterWidget(PoolNewPopupPresenterWidget.class,
                PoolNewPopupPresenterWidget.ViewDef.class,
                PoolNewPopupView.class);
        bindPresenterWidget(PoolEditPopupPresenterWidget.class,
                PoolEditPopupPresenterWidget.ViewDef.class,
                PoolEditPopupView.class);
        bindSingletonPresenterWidget(
                new TypeLiteral<SearchPanelPresenterWidget<VmPool, PoolListModel>>(){},
                new TypeLiteral<SearchPanelPresenterWidget.ViewDef<PoolListModel>>(){},
                new TypeLiteral<SearchPanelView<PoolListModel>>(){});
        bindPresenterWidget(
                new TypeLiteral<OvirtBreadCrumbsPresenterWidget<VmPool, PoolListModel>>(){},
                new TypeLiteral<OvirtBreadCrumbsPresenterWidget.ViewDef<VmPool>>(){},
                new TypeLiteral<OvirtBreadCrumbsView<VmPool, PoolListModel>>(){});
        bindActionPanel(new TypeLiteral<ActionPanelPresenterWidget.ViewDef<Void, VmPool>>(){},
            new TypeLiteral<ActionPanelView<Void, VmPool>>(){});

        // Template
        bindPresenter(TemplateSubTabPanelPresenter.class,
                TemplateSubTabPanelPresenter.ViewDef.class,
                TemplateSubTabPanelView.class,
                TemplateSubTabPanelPresenter.ProxyDef.class);
        bindPresenter(SubTabTemplateGeneralPresenter.class,
                SubTabTemplateGeneralPresenter.ViewDef.class,
                SubTabTemplateGeneralView.class,
                SubTabTemplateGeneralPresenter.ProxyDef.class);
        bindPresenter(SubTabTemplateVmPresenter.class,
                SubTabTemplateVmPresenter.ViewDef.class,
                SubTabTemplateVmView.class,
                SubTabTemplateVmPresenter.ProxyDef.class);
        bindPresenter(SubTabTemplateInterfacePresenter.class,
                SubTabTemplateInterfacePresenter.ViewDef.class,
                SubTabTemplateInterfaceView.class,
                SubTabTemplateInterfacePresenter.ProxyDef.class);
        bindPresenter(SubTabTemplateDiskPresenter.class,
                SubTabTemplateDiskPresenter.ViewDef.class,
                SubTabTemplateDiskView.class,
                SubTabTemplateDiskPresenter.ProxyDef.class);
        bindPresenter(SubTabTemplateStoragePresenter.class,
                SubTabTemplateStoragePresenter.ViewDef.class,
                SubTabTemplateStorageView.class,
                SubTabTemplateStoragePresenter.ProxyDef.class);
        bindPresenter(SubTabTemplatePermissionPresenter.class,
                SubTabTemplatePermissionPresenter.ViewDef.class,
                SubTabTemplatePermissionView.class,
                SubTabTemplatePermissionPresenter.ProxyDef.class);
        bindPresenter(SubTabTemplateEventPresenter.class,
                SubTabTemplateEventPresenter.ViewDef.class,
                SubTabTemplateEventView.class,
                SubTabTemplateEventPresenter.ProxyDef.class);
        bindSingletonPresenterWidget(
                new TypeLiteral<SearchPanelPresenterWidget<VmTemplate, TemplateListModel>>(){},
                new TypeLiteral<SearchPanelPresenterWidget.ViewDef<TemplateListModel>>(){},
                new TypeLiteral<SearchPanelView<TemplateListModel>>(){});
        bindPresenterWidget(TemplateBreadCrumbsPresenterWidget.class,
                TemplateBreadCrumbsPresenterWidget.TemplateBreadCrumbsViewDef.class,
                TemplateBreadCrumbsView.class);
        bindActionPanel(new TypeLiteral<ActionPanelPresenterWidget.ViewDef<Void, VmTemplate>>(){},
            new TypeLiteral<ActionPanelView<Void, VmTemplate>>(){});
        bindActionPanel(new TypeLiteral<DetailActionPanelPresenterWidget.ViewDef<Void, VmTemplate>>(){},
                new TypeLiteral<DetailActionPanelView<Void, VmTemplate>>(){});

        // User
        bindPresenter(UserSubTabPanelPresenter.class,
                UserSubTabPanelPresenter.ViewDef.class,
                UserSubTabPanelView.class,
                UserSubTabPanelPresenter.ProxyDef.class);
        bindPresenter(SubTabUserGeneralPresenter.class,
                SubTabUserGeneralPresenter.ViewDef.class,
                SubTabUserGeneralView.class,
                SubTabUserGeneralPresenter.ProxyDef.class);
        bindPresenter(SubTabUserPermissionPresenter.class,
                SubTabUserPermissionPresenter.ViewDef.class,
                SubTabUserPermissionView.class,
                SubTabUserPermissionPresenter.ProxyDef.class);
        bindPresenter(SubTabUserEventNotifierPresenter.class,
                SubTabUserEventNotifierPresenter.ViewDef.class,
                SubTabUserEventNotifierView.class,
                SubTabUserEventNotifierPresenter.ProxyDef.class);
        bindPresenter(SubTabUserEventPresenter.class,
                SubTabUserEventPresenter.ViewDef.class,
                SubTabUserEventView.class,
                SubTabUserEventPresenter.ProxyDef.class);
        bindPresenter(SubTabUserGroupPresenter.class,
                SubTabUserGroupPresenter.ViewDef.class,
                SubTabUserGroupView.class,
                SubTabUserGroupPresenter.ProxyDef.class);
        bindPresenter(SubTabUserQuotaPresenter.class,
                SubTabUserQuotaPresenter.ViewDef.class,
                SubTabUserQuotaView.class,
                SubTabUserQuotaPresenter.ProxyDef.class);
        bindPresenterWidget(UserRolesPopupPresenterWidget.class, UserRolesPopupPresenterWidget.ViewDef.class,
                UserRolesPopupView.class);
        bindSingletonPresenterWidget(
                new TypeLiteral<SearchPanelPresenterWidget<DbUser, UserListModel>>(){},
                new TypeLiteral<SearchPanelPresenterWidget.ViewDef<UserListModel>>(){},
                new TypeLiteral<SearchPanelView<UserListModel>>(){});
        bindPresenterWidget(
                new TypeLiteral<OvirtBreadCrumbsPresenterWidget<DbUser, UserListModel>>(){},
                new TypeLiteral<OvirtBreadCrumbsPresenterWidget.ViewDef<DbUser>>(){},
                new TypeLiteral<OvirtBreadCrumbsView<DbUser, UserListModel>>(){});
        bindActionPanel(new TypeLiteral<ActionPanelPresenterWidget.ViewDef<Void, DbUser>>(){},
            new TypeLiteral<ActionPanelView<Void, DbUser>>(){});

        // Quota
        bindPresenter(QuotaSubTabPanelPresenter.class,
                QuotaSubTabPanelPresenter.ViewDef.class,
                QuotaSubTabPanelView.class,
                QuotaSubTabPanelPresenter.ProxyDef.class);
        bindPresenter(SubTabQuotaClusterPresenter.class,
                SubTabQuotaClusterPresenter.ViewDef.class,
                SubTabQuotaClusterView.class,
                SubTabQuotaClusterPresenter.ProxyDef.class);
        bindPresenter(SubTabQuotaStoragePresenter.class,
                SubTabQuotaStoragePresenter.ViewDef.class,
                SubTabQuotaStorageView.class,
                SubTabQuotaStoragePresenter.ProxyDef.class);
        bindPresenter(SubTabQuotaVmPresenter.class,
                SubTabQuotaVmPresenter.ViewDef.class,
                SubTabQuotaVmView.class,
                SubTabQuotaVmPresenter.ProxyDef.class);
        bindPresenter(SubTabQuotaTemplatePresenter.class,
                SubTabQuotaTemplatePresenter.ViewDef.class,
                SubTabQuotaTemplateView.class,
                SubTabQuotaTemplatePresenter.ProxyDef.class);
        bindPresenter(SubTabQuotaUserPresenter.class,
                SubTabQuotaUserPresenter.ViewDef.class,
                SubTabQuotaUserView.class,
                SubTabQuotaUserPresenter.ProxyDef.class);
        bindPresenter(SubTabQuotaPermissionPresenter.class,
                SubTabQuotaPermissionPresenter.ViewDef.class,
                SubTabQuotaPermissionView.class,
                SubTabQuotaPermissionPresenter.ProxyDef.class);
        bindPresenter(SubTabQuotaEventPresenter.class,
                SubTabQuotaEventPresenter.ViewDef.class,
                SubTabQuotaEventView.class,
                SubTabQuotaEventPresenter.ProxyDef.class);
        bindSingletonPresenterWidget(
                new TypeLiteral<SearchPanelPresenterWidget<Quota, QuotaListModel>>(){},
                new TypeLiteral<SearchPanelPresenterWidget.ViewDef<QuotaListModel>>(){},
                new TypeLiteral<SearchPanelView<QuotaListModel>>(){});
        bindPresenterWidget(QuotaBreadCrumbsPresenterWidget.class,
                QuotaBreadCrumbsPresenterWidget.QuotaBreadCrumbsViewDef.class,
                QuotaBreadCrumbsView.class);
        bindActionPanel(new TypeLiteral<ActionPanelPresenterWidget.ViewDef<Void, Quota>>(){},
            new TypeLiteral<ActionPanelView<Void, Quota>>(){});

        // Disk
        bindPresenter(DiskSubTabPanelPresenter.class,
                DiskSubTabPanelPresenter.ViewDef.class,
                DiskSubTabPanelView.class,
                DiskSubTabPanelPresenter.ProxyDef.class);
        bindPresenter(SubTabDiskGeneralPresenter.class,
                SubTabDiskGeneralPresenter.ViewDef.class,
                SubTabDiskGeneralView.class,
                SubTabDiskGeneralPresenter.ProxyDef.class);
        bindPresenter(SubTabDiskVmPresenter.class,
                SubTabDiskVmPresenter.ViewDef.class,
                SubTabDiskVmView.class,
                SubTabDiskVmPresenter.ProxyDef.class);
        bindPresenter(SubTabDiskTemplatePresenter.class,
                SubTabDiskTemplatePresenter.ViewDef.class,
                SubTabDiskTemplateView.class,
                SubTabDiskTemplatePresenter.ProxyDef.class);
        bindPresenter(SubTabDiskStoragePresenter.class,
                SubTabDiskStoragePresenter.ViewDef.class,
                SubTabDiskStorageView.class,
                SubTabDiskStoragePresenter.ProxyDef.class);
        bindPresenter(SubTabDiskPermissionPresenter.class,
                SubTabDiskPermissionPresenter.ViewDef.class,
                SubTabDiskPermissionView.class,
                SubTabDiskPermissionPresenter.ProxyDef.class);
        bindSingletonPresenterWidget(
                new TypeLiteral<SearchPanelPresenterWidget<Disk, DiskListModel>>(){},
                new TypeLiteral<SearchPanelPresenterWidget.ViewDef<DiskListModel>>(){},
                new TypeLiteral<SearchPanelView<DiskListModel>>(){});
        bindPresenterWidget(DisksBreadCrumbsPresenterWidget.class,
                DisksBreadCrumbsPresenterWidget.DiskBreadCrumbsViewDef.class,
                DisksBreadCrumbsView.class);

        bindActionPanel(new TypeLiteral<ActionPanelPresenterWidget.ViewDef<Void, Disk>>(){},
            new TypeLiteral<ActionPanelView<Void, Disk>>(){});

        // Network
        bindPresenter(NetworkSubTabPanelPresenter.class,
                NetworkSubTabPanelPresenter.ViewDef.class,
                NetworkSubTabPanelView.class,
                NetworkSubTabPanelPresenter.ProxyDef.class);
        bindPresenter(SubTabNetworkGeneralPresenter.class,
                SubTabNetworkGeneralPresenter.ViewDef.class,
                SubTabNetworkGeneralView.class,
                SubTabNetworkGeneralPresenter.ProxyDef.class);
        bindPresenter(SubTabNetworkProfilePresenter.class,
                SubTabNetworkProfilePresenter.ViewDef.class,
                SubTabNetworkProfileView.class,
                SubTabNetworkProfilePresenter.ProxyDef.class);
        bindPresenter(SubTabNetworkExternalSubnetPresenter.class,
                SubTabNetworkExternalSubnetPresenter.ViewDef.class,
                SubTabNetworkExternalSubnetView.class,
                SubTabNetworkExternalSubnetPresenter.ProxyDef.class);
        bindPresenter(SubTabNetworkClusterPresenter.class,
                SubTabNetworkClusterPresenter.ViewDef.class,
                SubTabNetworkClusterView.class,
                SubTabNetworkClusterPresenter.ProxyDef.class);
        bindPresenter(SubTabNetworkHostPresenter.class,
                SubTabNetworkHostPresenter.ViewDef.class,
                SubTabNetworkHostView.class,
                SubTabNetworkHostPresenter.ProxyDef.class);
        bindPresenter(SubTabNetworkVmPresenter.class,
                SubTabNetworkVmPresenter.ViewDef.class,
                SubTabNetworkVmView.class,
                SubTabNetworkVmPresenter.ProxyDef.class);
        bindPresenter(SubTabNetworkTemplatePresenter.class,
                SubTabNetworkTemplatePresenter.ViewDef.class,
                SubTabNetworkTemplateView.class,
                SubTabNetworkTemplatePresenter.ProxyDef.class);
        bindPresenter(SubTabNetworkPermissionPresenter.class,
                SubTabNetworkPermissionPresenter.ViewDef.class,
                SubTabNetworkPermissionView.class,
                SubTabNetworkPermissionPresenter.ProxyDef.class);
        bindSingletonPresenterWidget(
                new TypeLiteral<SearchPanelPresenterWidget<NetworkView, NetworkListModel>>(){},
                new TypeLiteral<SearchPanelPresenterWidget.ViewDef<NetworkListModel>>(){},
                new TypeLiteral<SearchPanelView<NetworkListModel>>(){});
        bindPresenterWidget(NetworkBreadCrumbsPresenterWidget.class,
                NetworkBreadCrumbsPresenterWidget.NetworkBreadCrumbsViewDef.class,
                NetworkBreadCrumbsView.class);
        bindActionPanel(new TypeLiteral<ActionPanelPresenterWidget.ViewDef<NetworkView, NetworkView>>(){},
            new TypeLiteral<ActionPanelView<NetworkView, NetworkView>>(){});

        // Provider
        bindPresenter(ProviderSubTabPanelPresenter.class,
                ProviderSubTabPanelPresenter.ViewDef.class,
                ProviderSubTabPanelView.class,
                ProviderSubTabPanelPresenter.ProxyDef.class);
        bindPresenter(SubTabProviderGeneralPresenter.class,
                SubTabProviderGeneralPresenter.ViewDef.class,
                SubTabProviderGeneralView.class,
                SubTabProviderGeneralPresenter.ProxyDef.class);
        bindPresenter(SubTabProviderNetworkPresenter.class,
                SubTabProviderNetworkPresenter.ViewDef.class,
                SubTabProviderNetworkView.class,
                SubTabProviderNetworkPresenter.ProxyDef.class);
        bindPresenter(SubTabProviderSecretPresenter.class,
                SubTabProviderSecretPresenter.ViewDef.class,
                SubTabProviderSecretView.class,
                SubTabProviderSecretPresenter.ProxyDef.class);
        bindSingletonPresenterWidget(
                new TypeLiteral<SearchPanelPresenterWidget<Provider, ProviderListModel>>(){},
                new TypeLiteral<SearchPanelPresenterWidget.ViewDef<ProviderListModel>>(){},
                new TypeLiteral<SearchPanelView<ProviderListModel>>(){});
        bindPresenterWidget(
                new TypeLiteral<OvirtBreadCrumbsPresenterWidget<Provider, ProviderListModel>>(){},
                new TypeLiteral<OvirtBreadCrumbsPresenterWidget.ViewDef<Provider>>(){},
                new TypeLiteral<OvirtBreadCrumbsView<Provider, ProviderListModel>>(){});
        bindActionPanel(new TypeLiteral<ActionPanelPresenterWidget.ViewDef<Void, Provider>>(){},
            new TypeLiteral<ActionPanelView<Void, Provider>>(){});

        // Errata
        bindPresenter(ErrataSubTabPanelPresenter.class,
                ErrataSubTabPanelPresenter.ViewDef.class,
                ErrataSubTabPanelView.class,
                ErrataSubTabPanelPresenter.ProxyDef.class);
        bindPresenter(SubTabEngineErrataDetailsPresenter.class,
                SubTabEngineErrataDetailsPresenter.ViewDef.class,
                SubTabEngineErrataDetailsView.class,
                SubTabEngineErrataDetailsPresenter.ProxyDef.class);
        bindPresenterWidget(
                new TypeLiteral<OvirtBreadCrumbsPresenterWidget<Erratum, EngineErrataListModel>>(){},
                new TypeLiteral<OvirtBreadCrumbsPresenterWidget.ViewDef<Erratum>>(){},
                new TypeLiteral<OvirtBreadCrumbsView<Erratum, EngineErrataListModel>>(){});

        // Profile
        bindPresenter(VnicProfileSubTabPanelPresenter.class,
                VnicProfileSubTabPanelPresenter.ViewDef.class,
                VnicProfileSubTabPanelView.class,
                VnicProfileSubTabPanelPresenter.ProxyDef.class);
        bindPresenter(SubTabVnicProfilePermissionPresenter.class,
                SubTabVnicProfilePermissionPresenter.ViewDef.class,
                SubTabVnicProfilePermissionView.class,
                SubTabVnicProfilePermissionPresenter.ProxyDef.class);
        bindPresenter(SubTabVnicProfileVmPresenter.class,
                SubTabVnicProfileVmPresenter.ViewDef.class,
                SubTabVnicProfileVmView.class,
                SubTabVnicProfileVmPresenter.ProxyDef.class);
        bindPresenter(SubTabVnicProfileTemplatePresenter.class,
                SubTabVnicProfileTemplatePresenter.ViewDef.class,
                SubTabVnicProfileTemplateView.class,
                SubTabVnicProfileTemplatePresenter.ProxyDef.class);
        bindPresenterWidget(VnicProfileBreadCrumbsPresenterWidget.class,
                VnicProfileBreadCrumbsPresenterWidget.VnicProfileBreadCrumbsViewDef.class,
                VnicProfileBreadCrumbsView.class);
        bindSingletonPresenterWidget(
                new TypeLiteral<SearchPanelPresenterWidget<VnicProfileView, VnicProfileListModel>>(){},
                new TypeLiteral<SearchPanelPresenterWidget.ViewDef<VnicProfileListModel>>(){},
                new TypeLiteral<SearchPanelView<VnicProfileListModel>>(){});
        bindActionPanel(new TypeLiteral<ActionPanelPresenterWidget.ViewDef<VnicProfileView, VnicProfileView>>(){},
            new TypeLiteral<ActionPanelView<VnicProfileView, VnicProfileView>>(){});

        // User Sessions
        bindSingletonPresenterWidget(
                new TypeLiteral<SearchPanelPresenterWidget<UserSession, SessionListModel>>(){},
                new TypeLiteral<SearchPanelPresenterWidget.ViewDef<SessionListModel>>(){},
                new TypeLiteral<SearchPanelView<SessionListModel>>(){});
        bindPresenterWidget(
                new TypeLiteral<OvirtBreadCrumbsPresenterWidget<UserSession, SessionListModel>>(){},
                new TypeLiteral<OvirtBreadCrumbsPresenterWidget.ViewDef<UserSession>>(){},
                new TypeLiteral<OvirtBreadCrumbsView<UserSession, SessionListModel>>(){});
        bindActionPanel(new TypeLiteral<ActionPanelPresenterWidget.ViewDef<UserSession, UserSession>>(){},
            new TypeLiteral<ActionPanelView<UserSession, UserSession>>(){});

        // Volume
        bindSingletonPresenterWidget(
                new TypeLiteral<SearchPanelPresenterWidget<GlusterVolumeEntity, VolumeListModel>>(){},
                new TypeLiteral<SearchPanelPresenterWidget.ViewDef<VolumeListModel>>(){},
                new TypeLiteral<SearchPanelView<VolumeListModel>>(){});
        bindPresenterWidget(
                new TypeLiteral<OvirtBreadCrumbsPresenterWidget<GlusterVolumeEntity, VolumeListModel>>(){},
                new TypeLiteral<OvirtBreadCrumbsPresenterWidget.ViewDef<GlusterVolumeEntity>>(){},
                new TypeLiteral<OvirtBreadCrumbsView<GlusterVolumeEntity, VolumeListModel>>(){});
        bindActionPanel(new TypeLiteral<ActionPanelPresenterWidget.ViewDef<Void, GlusterVolumeEntity>>(){},
            new TypeLiteral<ActionPanelView<Void, GlusterVolumeEntity>>(){});

        // Events
        bindActionPanel(new TypeLiteral<ActionPanelPresenterWidget.ViewDef<Void, AuditLog>>(){},
                new TypeLiteral<ActionPanelView<Void, AuditLog>>(){});

        // Main section: popups

        // Permissions
        bindPresenterWidget(PermissionsPopupPresenterWidget.class,
                PermissionsPopupPresenterWidget.ViewDef.class,
                PermissionsPopupView.class);

        bindPresenterWidget(SystemPermissionsRemoveConfirmationPopupPresenterWidget.class,
                SystemPermissionsRemoveConfirmationPopupPresenterWidget.ViewDef.class,
                SystemPermissionsRemoveConfirmationPopupView.class);

        bindPresenterWidget(VmNextRunConfigurationPresenterWidget.class,
                VmNextRunConfigurationPresenterWidget.ViewDef.class,
                VmNextRunConfigurationPopupView.class);

        bindPresenterWidget(VmHighPerformanceConfigurationPresenterWidget.class,
                VmHighPerformanceConfigurationPresenterWidget.ViewDef.class,
                VmHighPerformanceConfigurationPopupView.class);

        // Bookmarks
        bindPresenterWidget(BookmarkPopupPresenterWidget.class,
                BookmarkPopupPresenterWidget.ViewDef.class,
                BookmarkPopupView.class);

        // Tags
        bindPresenterWidget(TagPopupPresenterWidget.class,
                TagPopupPresenterWidget.ViewDef.class,
                TagPopupView.class);

        // Guide
        bindPresenterWidget(GuidePopupPresenterWidget.class,
                GuidePopupPresenterWidget.ViewDef.class,
                GuidePopupView.class);
        bindPresenterWidget(MoveHostPopupPresenterWidget.class,
                MoveHostPopupPresenterWidget.ViewDef.class,
                MoveHostPopupView.class);

        // DataCenter
        bindPresenterWidget(DataCenterPopupPresenterWidget.class,
                DataCenterPopupPresenterWidget.ViewDef.class,
                DataCenterPopupView.class);
        bindPresenterWidget(FindMultiStoragePopupPresenterWidget.class,
                FindMultiStoragePopupPresenterWidget.ViewDef.class,
                FindMultiStoragePopupView.class);
        bindPresenterWidget(FindSingleStoragePopupPresenterWidget.class,
                FindSingleStoragePopupPresenterWidget.ViewDef.class,
                FindSingleStoragePopupView.class);
        bindPresenterWidget(NewDataCenterNetworkPopupPresenterWidget.class,
                NewDataCenterNetworkPopupPresenterWidget.ViewDef.class,
                NewDataCenterNetworkPopupView.class);
        bindPresenterWidget(EditDataCenterNetworkPopupPresenterWidget.class,
                EditDataCenterNetworkPopupPresenterWidget.ViewDef.class,
                EditDataCenterNetworkPopupView.class);
        bindPresenterWidget(DataCenterForceRemovePopupPresenterWidget.class,
                DataCenterForceRemovePopupPresenterWidget.ViewDef.class,
                DataCenterForceRemovePopupView.class);

        // Cluster
        bindPresenterWidget(NewClusterNetworkPopupPresenterWidget.class,
                NewClusterNetworkPopupPresenterWidget.ViewDef.class,
                NewClusterNetworkPopupView.class);

        bindPresenterWidget(ClusterManageNetworkPopupPresenterWidget.class,
                ClusterManageNetworkPopupPresenterWidget.ViewDef.class,
                ClusterManageNetworkPopupView.class);

        bindPresenterWidget(ClusterPopupPresenterWidget.class,
                ClusterPopupPresenterWidget.ViewDef.class,
                ClusterPopupView.class);

        bindPresenterWidget(ClusterWarningsPopupPresenterWidget.class,
                ClusterWarningsPopupPresenterWidget.ViewDef.class,
                ClusterWarningsPopupView.class);

        bindPresenterWidget(VolumePopupPresenterWidget.class,
                VolumePopupPresenterWidget.ViewDef.class,
                VolumePopupView.class);

        bindPresenterWidget(DetachGlusterHostsPopupPresenterWidget.class,
                DetachGlusterHostsPopupPresenterWidget.ViewDef.class,
                DetachGlusterHostsPopupView.class);

        bindPresenterWidget(GlusterHookContentPopupPresenterWidget.class,
                GlusterHookContentPopupPresenterWidget.ViewDef.class,
                GlusterHookContentPopupView.class);

        bindPresenterWidget(GlusterHookResolveConflictsPopupPresenterWidget.class,
                GlusterHookResolveConflictsPopupPresenterWidget.ViewDef.class,
                GlusterHookResolveConflictsPopupView.class);

        bindPresenterWidget(VolumeRebalanceStatusPopupPresenterWidget.class,
                VolumeRebalanceStatusPopupPresenterWidget.ViewDef.class,
                VolumeRebalanceStatusPopupView.class);

        bindPresenterWidget(GlusterVolumeGeoRepActionConfirmPopUpViewPresenterWidget.class,
                GlusterVolumeGeoRepActionConfirmPopUpViewPresenterWidget.ViewDef.class,
                GeoRepActionConfirmPopUpView.class);

        bindPresenterWidget(GlusterVolumeGeoReplicationSessionConfigPopupPresenterWidget.class,
                GlusterVolumeGeoReplicationSessionConfigPopupPresenterWidget.ViewDef.class,
                GlusterVolumeGeoReplicationSessionConfigPopupView.class);

        bindPresenterWidget(VolumeGeoRepSessionDetailsPopUpPresenterWidget.class,
                VolumeGeoRepSessionDetailsPopUpPresenterWidget.ViewDef.class,
                VolumeGeoRepSessionDetailsPopUpView.class);

        bindPresenterWidget(GlusterVolumeGeoRepCreateSessionPopupPresenterWidget.class,
                GlusterVolumeGeoRepCreateSessionPopupPresenterWidget.ViewDef.class,
                GlusterVolumeGeoRepCreateSessionPopupView.class);

        bindPresenterWidget(RemoveBrickStatusPopupPresenterWidget.class,
                RemoveBrickStatusPopupPresenterWidget.ViewDef.class,
                RemoveBrickStatusPopupView.class);

        bindPresenterWidget(ManageGlusterSwiftPopupPresenterWidget.class,
                ManageGlusterSwiftPopupPresenterWidget.ViewDef.class,
                ManageGlusterSwiftPopupView.class);

        bindPresenterWidget(VolumeProfileStatisticsPopupPresenterWidget.class,
                VolumeProfileStatisticsPopupPresenterWidget.ViewDef.class,
                VolumeProfileStatisticsPopupView.class);

        bindPresenterWidget(AddDataCenterClusterPopupPresenterWidget.class,
                AddDataCenterClusterPopupPresenterWidget.ViewDef.class,
                AddDataCenterClusterPopupView.class);

        // Host
        bindPresenterWidget(HostPopupPresenterWidget.class,
                HostPopupPresenterWidget.ViewDef.class,
                HostPopupView.class);
        bindPresenterWidget(HostInstallPopupPresenterWidget.class,
                HostInstallPopupPresenterWidget.ViewDef.class,
                HostInstallPopupView.class);
        bindPresenterWidget(NetworkAttachmentPopupPresenterWidget.class,
                NetworkAttachmentPopupPresenterWidget.ViewDef.class,
                NetworkAttachmentPopupView.class);
        bindPresenterWidget(SetupNetworksBondPopupPresenterWidget.class,
                SetupNetworksBondPopupPresenterWidget.ViewDef.class,
                SetupNetworksBondPopupView.class);
        bindPresenterWidget(VfsConfigPopupPresenterWidget.class,
                VfsConfigPopupPresenterWidget.ViewDef.class,
                VfsConfigPopupView.class);
        bindPresenterWidget(SetupNetworksLabelPopupPresenterWidget.class,
                SetupNetworksLabelPopupPresenterWidget.ViewDef.class,
                SetupNetworksLabelPopupView.class);
        bindPresenterWidget(HostSetupNetworksPopupPresenterWidget.class,
                HostSetupNetworksPopupPresenterWidget.ViewDef.class,
                HostSetupNetworksPopupView.class);
        bindPresenterWidget(ManualFencePopupPresenterWidget.class,
                ManualFencePopupPresenterWidget.ViewDef.class,
                ManualFenceConfirmationPopupView.class);
        bindPresenterWidget(ConfigureLocalStoragePopupPresenterWidget.class,
                ConfigureLocalStoragePopupPresenterWidget.ViewDef.class,
                HostConfigureLocalStoragePopupView.class);
        bindPresenterWidget(MultipleHostsPopupPresenterWidget.class,
                MultipleHostsPopupPresenterWidget.ViewDef.class,
                MultipleHostsPopupView.class);
        bindPresenterWidget(HostFenceAgentPopupPresenterWidget.class,
                HostFenceAgentPopupPresenterWidget.ViewDef.class,
                HostFenceAgentPopupView.class);
        bindPresenterWidget(HostFenceProxyPopupPresenterWidget.class,
                HostFenceProxyPopupPresenterWidget.ViewDef.class,
                HostFenceProxyPopupView.class);
        bindPresenterWidget(HostMaintenanceConfirmationPopupPresenterWidget.class,
                HostMaintenanceConfirmationPopupPresenterWidget.ViewDef.class,
                HostMaintenanceConfirmationPopupView.class);
        bindPresenterWidget(HostRestartConfirmationPopupPresenterWidget.class,
                HostRestartConfirmationPopupPresenterWidget.ViewDef.class,
                HostRestartConfirmationPopupView.class);
        bindPresenterWidget(HostUpgradePopupPresenterWidget.class,
                HostUpgradePopupPresenterWidget.ViewDef.class,
                HostUpgradePopupView.class);

        // Storage
        bindPresenterWidget(StoragePopupPresenterWidget.class,
                StoragePopupPresenterWidget.ViewDef.class,
                StoragePopupView.class);
        bindPresenterWidget(FindMultiDcPopupPresenterWidget.class,
                FindMultiDcPopupPresenterWidget.ViewDef.class,
                FindMultiDcPopupView.class);
        bindPresenterWidget(FindSingleDcPopupPresenterWidget.class,
                FindSingleDcPopupPresenterWidget.ViewDef.class,
                FindSingleDcPopupView.class);
        bindPresenterWidget(ImportVmFromExportDomainPopupPresenterWidget.class,
                ImportVmFromExportDomainPopupPresenterWidget.ViewDef.class,
                ImportVmFromExportDomainPopupView.class);
        bindPresenterWidget(ImportVmFromExternalProviderPopupPresenterWidget.class,
                ImportVmFromExternalProviderPopupPresenterWidget.ViewDef.class,
                ImportVmFromExternalProviderPopupView.class);
        bindPresenterWidget(ImportTemplatePopupPresenterWidget.class,
                ImportTemplatePopupPresenterWidget.ViewDef.class,
                ImportTemplatePopupView.class);
        bindPresenterWidget(ImportTemplateFromOvaPopupPresenterWidget.class,
                ImportTemplateFromOvaPopupPresenterWidget.ViewDef.class,
                ImportTemplateFromOvaPopupView.class);
        bindPresenterWidget(RegisterVmPopupPresenterWidget.class,
                RegisterVmPopupPresenterWidget.ViewDef.class,
                RegisterVmPopupView.class);
        bindPresenterWidget(RegisterTemplatePopupPresenterWidget.class,
                RegisterTemplatePopupPresenterWidget.ViewDef.class,
                RegisterTemplatePopupView.class);
        bindPresenterWidget(VnicProfileMappingPopupPresenterWidget.class,
                VnicProfileMappingPopupPresenterWidget.ViewDef.class,
                VnicProfileMappingPopupView.class);
        bindPresenterWidget(ImportCloneDialogPresenterWidget.class,
                ImportCloneDialogPresenterWidget.ViewDef.class,
                ImportCloneDialogPopupView.class);
        bindPresenterWidget(DisksAllocationPopupPresenterWidget.class,
                DisksAllocationPopupPresenterWidget.ViewDef.class,
                DisksAllocationPopupView.class);
        bindPresenterWidget(ChangeQuotaPopupPresenterWidget.class,
                ChangeQuotaPopupPresenterWidget.ViewDef.class,
                ChangeQuotaPopupView.class);
        bindPresenterWidget(ImportExportImagePopupPresenterWidget.class,
                ImportExportImagePopupPresenterWidget.ViewDef.class,
                ImportExportImagePopupView.class);
        bindPresenterWidget(UploadImagePopupPresenterWidget.class,
                UploadImagePopupPresenterWidget.ViewDef.class,
                UploadImagePopupView.class);
        bindPresenterWidget(StorageDRPopupPresenterWidget.class,
                StorageDRPopupPresenterWidget.ViewDef.class,
                StorageDRPopupView.class);

        // Storage Remove
        bindPresenterWidget(StorageRemovePopupPresenterWidget.class,
                StorageRemovePopupPresenterWidget.ViewDef.class,
                StorageRemovePopupView.class);

        // Storage Destroy
        bindPresenterWidget(StorageDestroyPopupPresenterWidget.class,
                StorageDestroyPopupPresenterWidget.ViewDef.class,
                StorageDestroyPopupView.class);

        bindPresenterWidget(StorageForceCreatePopupPresenterWidget.class,
                StorageForceCreatePopupPresenterWidget.ViewDef.class,
                StorageForceCreatePopupView.class);

        bindPresenterWidget(VmPopupPresenterWidget.class,
                VmPopupPresenterWidget.ViewDef.class,
                VmPopupView.class);

        // VM Snapshot Create
        bindPresenterWidget(VmSnapshotCreatePopupPresenterWidget.class,
                VmSnapshotCreatePopupPresenterWidget.ViewDef.class,
                VmSnapshotCreatePopupView.class);

        // VM Snapshot Preview
        bindPresenterWidget(VmSnapshotPreviewPopupPresenterWidget.class,
                VmSnapshotPreviewPopupPresenterWidget.ViewDef.class,
                VmSnapshotPreviewPopupView.class);
        bindPresenterWidget(VmSnapshotCustomPreviewPopupPresenterWidget.class,
                VmSnapshotCustomPreviewPopupPresenterWidget.ViewDef.class,
                VmSnapshotCustomPreviewPopupView.class);

        // VM Clone from Snapshot
        bindPresenterWidget(VmClonePopupPresenterWidget.class,
                VmClonePopupPresenterWidget.ViewDef.class,
                VmClonePopupView.class);

        // VM Assign Tags
        bindPresenterWidget(AssignTagsPopupPresenterWidget.class,
                AssignTagsPopupPresenterWidget.ViewDef.class,
                AssignTagsPopupView.class);

        // VM RunOnce
        bindPresenterWidget(VmRunOncePopupPresenterWidget.class,
                VmRunOncePopupPresenterWidget.ViewDef.class,
                VmRunOncePopupView.class);

        // VM Make Template
        bindPresenterWidget(VmMakeTemplatePopupPresenterWidget.class,
                VmMakeTemplatePopupPresenterWidget.ViewDef.class,
                VmMakeTemplatePopupView.class);

        // VM Change CD
        bindPresenterWidget(VmChangeCDPopupPresenterWidget.class,
                VmChangeCDPopupPresenterWidget.ViewDef.class,
                VmChangeCDPopupView.class);

        // Clone VM
        bindPresenterWidget(CloneVmPopupPresenterWidget.class,
                CloneVmPopupPresenterWidget.ViewDef.class,
                CloneVmPopupView.class);

        // VM Export
        bindPresenterWidget(VmExportPopupPresenterWidget.class,
                VmExportPopupPresenterWidget.ViewDef.class,
                VmExportPopupView.class);

        // OVA Export
        bindPresenterWidget(ExportOvaPopupPresenterWidget.class,
                ExportOvaPopupPresenterWidget.ViewDef.class,
                ExportOvaPopupView.class);

        // VM Remove
        bindPresenterWidget(VmRemovePopupPresenterWidget.class,
                VmRemovePopupPresenterWidget.ViewDef.class,
                VmRemovePopupView.class);

        // VM VNC info
        bindPresenterWidget(VncInfoPopupPresenterWidget.class,
                VncInfoPopupPresenterWidget.ViewDef.class,
                VncInfoPopupView.class);

        // VM Add/Edit Interface
        bindPresenterWidget(VmInterfacePopupPresenterWidget.class,
                VmInterfacePopupPresenterWidget.ViewDef.class,
                VmInterfacePopupView.class);

        // VM Add/Edit Disk
        bindPresenterWidget(VmDiskPopupPresenterWidget.class,
                VmDiskPopupPresenterWidget.ViewDef.class,
                VmDiskPopupView.class);

        // VM Attach Disk
        bindPresenterWidget(VmDiskAttachPopupPresenterWidget.class,
                VmDiskAttachPopupPresenterWidget.ViewDef.class,
                VmDiskAttachPopupView.class);
        bindPresenterWidget(SingleSelectionVmDiskAttachPopupPresenterWidget.class,
                SingleSelectionVmDiskAttachPopupPresenterWidget.ViewDef.class,
                SingleSelectionVmDiskAttachPopupView.class);

        // VM Detach/Remove Disk
        bindPresenterWidget(VmDiskRemovePopupPresenterWidget.class,
                VmDiskRemovePopupPresenterWidget.ViewDef.class,
                VmDiskRemovePopupView.class);

        // VM Sparsify Disk
        bindPresenterWidget(VmDiskSparsifyPopupPresenterWidget.class,
                VmDiskSparsifyPopupPresenterWidget.ViewDef.class,
                VmDiskSparsifyPopupView.class);

        // Edit Template
        bindPresenterWidget(TemplateEditPresenterWidget.class,
                TemplateEditPresenterWidget.ViewDef.class,
                TemplateEditPopupView.class);

        // Import Template
        bindPresenterWidget(ImportTemplatesPopupPresenterWidget.class,
                ImportTemplatesPopupPresenterWidget.ViewDef.class,
                ImportTemplatesPopupView.class);

        // Instance Types
        bindPresenterWidget(InstanceTypesPopupPresenterWidget.class,
                InstanceTypesPopupPresenterWidget.ViewDef.class,
                InstanceTypesPopupView.class);

        // Add/Edit Template's NIC
        bindPresenterWidget(TemplateInterfacePopupPresenterWidget.class,
                TemplateInterfacePopupPresenterWidget.ViewDef.class,
                TemplateInterfacePopupView.class);

        // Users Manage Events
        bindPresenterWidget(ManageEventsPopupPresenterWidget.class,
                ManageEventsPopupPresenterWidget.ViewDef.class,
                ManageEventsPopupView.class);

        // Quota
        bindPresenterWidget(QuotaPopupPresenterWidget.class,
                QuotaPopupPresenterWidget.ViewDef.class,
                QuotaPopupView.class);

        // Network QoS
        bindPresenterWidget(NetworkQoSPopupPresenterWidget.class,
                NetworkQoSPopupPresenterWidget.ViewDef.class,
                NetworkQoSPopupView.class);

        // Storage QoS
        bindPresenterWidget(StorageQosPopupPresenterWidget.class,
                StorageQosPopupPresenterWidget.ViewDef.class,
                StorageQosPopupView.class);

        bindPresenterWidget(StorageQosRemovePopupPresenterWidget.class,
                StorageQosRemovePopupPresenterWidget.ViewDef.class,
                StorageQosRemovePopupView.class);

        // Cpu QoS
        bindPresenterWidget(CpuQosPopupPresenterWidget.class,
                CpuQosPopupPresenterWidget.ViewDef.class,
                CpuQosPopupView.class);

        // Host Network QoS
        bindPresenterWidget(HostNetworkQosPopupPresenterWidget.class,
                HostNetworkQosPopupPresenterWidget.ViewDef.class,
                HostNetworkQosPopupView.class);

        bindPresenterWidget(EditQuotaClusterPopupPresenterWidget.class,
                EditQuotaClusterPopupPresenterWidget.ViewDef.class,
                EditQuotaClusterPopupView.class);

        bindPresenterWidget(EditQuotaStoragePopupPresenterWidget.class,
                EditQuotaStoragePopupPresenterWidget.ViewDef.class,
                EditQuotaStoragePopupView.class);

        bindPresenterWidget(ImportVmsPopupPresenterWidget.class,
                ImportVmsPopupPresenterWidget.ViewDef.class,
                ImportVmsPopupView.class);

        // Host Devices
        bindPresenterWidget(AddVmHostDevicePopupPresenterWidget.class,
                AddVmHostDevicePopupPresenterWidget.ViewDef.class,
                AddVmHostDevicePopupView.class);

        bindPresenterWidget(VmRepinHostPopupPresenterWidget.class,
                VmRepinHostPopupPresenterWidget.ViewDef.class,
                VmRepinHostPopupView.class);

        // Volume
        bindPresenter(VolumeSubTabPanelPresenter.class,
                VolumeSubTabPanelPresenter.ViewDef.class,
                VolumeSubTabPanelView.class,
                VolumeSubTabPanelPresenter.ProxyDef.class);
        bindPresenter(SubTabVolumeGeneralPresenter.class,
                SubTabVolumeGeneralPresenter.ViewDef.class,
                SubTabVolumeGeneralView.class,
                SubTabVolumeGeneralPresenter.ProxyDef.class);

        bindPresenter(SubTabVolumeBrickPresenter.class,
                SubTabVolumeBrickPresenter.ViewDef.class,
                SubTabVolumeBrickView.class,
                SubTabVolumeBrickPresenter.ProxyDef.class);

        bindPresenter(SubTabVolumeGeoRepPresenter.class,
                SubTabVolumeGeoRepPresenter.ViewDef.class,
                SubTabVolumeGeoRepView.class,
                SubTabVolumeGeoRepPresenter.ProxyDef.class);

        bindPresenter(SubTabVolumeParameterPresenter.class,
                SubTabVolumeParameterPresenter.ViewDef.class,
                SubTabVolumeParameterView.class,
                SubTabVolumeParameterPresenter.ProxyDef.class);

        bindPresenter(SubTabVolumePermissionPresenter.class,
                SubTabVolumePermissionPresenter.ViewDef.class,
                SubTabVolumePermissionView.class,
                SubTabVolumePermissionPresenter.ProxyDef.class);

        bindPresenter(SubTabVolumeEventPresenter.class,
                SubTabVolumeEventPresenter.ViewDef.class,
                SubTabVolumeEventView.class,
                SubTabVolumeEventPresenter.ProxyDef.class);

        bindPresenter(SubTabGlusterVolumeSnapshotPresenter.class,
                SubTabGlusterVolumeSnapshotPresenter.ViewDef.class,
                SubTabGlusterVolumeSnapshotView.class,
                SubTabGlusterVolumeSnapshotPresenter.ProxyDef.class);

        bindPresenterWidget(AddBrickPopupPresenterWidget.class,
                AddBrickPopupPresenterWidget.ViewDef.class,
                AddBrickPopupView.class);

        bindPresenterWidget(CreateBrickPopupPresenterWidget.class,
                CreateBrickPopupPresenterWidget.ViewDef.class,
                CreateBrickPopupView.class);

        bindPresenterWidget(RemoveBrickPopupPresenterWidget.class,
                RemoveBrickPopupPresenterWidget.ViewDef.class,
                RemoveBrickPopupView.class);

        bindPresenterWidget(ReplaceBrickPopupPresenterWidget.class,
                ReplaceBrickPopupPresenterWidget.ViewDef.class,
                ReplaceBrickPopupView.class);

        bindPresenterWidget(BrickAdvancedDetailsPopupPresenterWidget.class,
                BrickAdvancedDetailsPopupPresenterWidget.ViewDef.class,
                BrickAdvancedDetailsPopupView.class);

        bindPresenterWidget(VolumeParameterPopupPresenterWidget.class,
                VolumeParameterPopupPresenterWidget.ViewDef.class,
                VolumeParameterPopupView.class);

        bindPresenterWidget(ResetBrickPopupPresenterWidget.class,
                ResetBrickPopupPresenterWidget.ViewDef.class,
                ResetBrickPopupView.class);

        // Network
        bindPresenterWidget(NewNetworkPopupPresenterWidget.class,
                NewNetworkPopupPresenterWidget.ViewDef.class,
                NewNetworkPopupView.class);
        bindPresenterWidget(EditNetworkPopupPresenterWidget.class,
                EditNetworkPopupPresenterWidget.ViewDef.class,
                EditNetworkPopupView.class);

        // Event
        bindPresenterWidget(EventPopupPresenterWidget.class,
                EventPopupPresenterWidget.ViewDef.class,
                EventPopupView.class);
        bindSingletonPresenterWidget(
                new TypeLiteral<SearchPanelPresenterWidget<AuditLog, EventListModel<Void>>>(){},
                new TypeLiteral<SearchPanelPresenterWidget.ViewDef<EventListModel<Void>>>(){},
                new TypeLiteral<SearchPanelView<EventListModel<Void>>>(){});
        bindPresenterWidget(
                new TypeLiteral<OvirtBreadCrumbsPresenterWidget<AuditLog, EventListModel<Void>>>(){},
                new TypeLiteral<OvirtBreadCrumbsPresenterWidget.ViewDef<AuditLog>>(){},
                new TypeLiteral<OvirtBreadCrumbsView<AuditLog, EventListModel<Void>>>(){});

        // Provider
        bindPresenterWidget(ProviderPopupPresenterWidget.class,
                ProviderPopupPresenterWidget.ViewDef.class,
                ProviderPopupView.class);

        bindPresenterWidget(ImportNetworksPopupPresenterWidget.class,
                ImportNetworksPopupPresenterWidget.ViewDef.class,
                ImportNetworksPopupView.class);

        bindPresenterWidget(ProviderSecretPopupPresenterWidget.class,
                ProviderSecretPopupPresenterWidget.ViewDef.class,
                ProviderSecretPopupView.class);

        // Profile
        bindPresenterWidget(VnicProfilePopupPresenterWidget.class,
                VnicProfilePopupPresenterWidget.ViewDef.class,
                VnicProfilePopupView.class);
        bindPresenterWidget(DiskProfilePopupPresenterWidget.class,
                DiskProfilePopupPresenterWidget.ViewDef.class,
                DiskProfilePopupView.class);
        bindPresenterWidget(CpuProfilePopupPresenterWidget.class,
                CpuProfilePopupPresenterWidget.ViewDef.class,
                CpuProfilePopupView.class);

        // External Subnet
        bindPresenterWidget(ExternalSubnetPopupPresenterWidget.class,
                ExternalSubnetPopupPresenterWidget.ViewDef.class,
                ExternalSubnetPopupView.class);

        // ISCSI Bond
        bindPresenterWidget(IscsiBondPopupPresenterWidget.class,
                IscsiBondPopupPresenterWidget.ViewDef.class,
                IscsiBondPopupView.class);

        // Errata details
        bindPresenterWidget(HostErrataListWithDetailsPopupPresenterWidget.class,
                HostErrataListWithDetailsPopupPresenterWidget.ViewDef.class,
                HostErrataListWithDetailsPopupView.class);
        bindPresenterWidget(VmErrataListWithDetailsPopupPresenterWidget.class,
                VmErrataListWithDetailsPopupPresenterWidget.ViewDef.class,
                VmErrataListWithDetailsPopupView.class);

        bindPresenterWidget(ExpandAllButtonPresenterWidget.class, ExpandAllButtonPresenterWidget.ViewDef.class,
                ExpandAllButtonView.class);

        bindPresenterWidget(ShowHideVfPresenterWidget.class, ShowHideVfPresenterWidget.ViewDef.class,
                ShowHideVfButtonView.class);

        // Overlays
        // Tasks
        bindPresenterWidget(TasksPresenterWidget.class,
                TasksPresenterWidget.ViewDef.class,
                TasksView.class);
        // Bookmarks
        bindPresenterWidget(BookmarkPresenterWidget.class,
                BookmarkPresenterWidget.ViewDef.class,
                BookmarkView.class);
        // Tags
        bindPresenterWidget(TagsPresenterWidget.class,
                TagsPresenterWidget.ViewDef.class,
                TagsView.class);

        // Popup/detail Action panels
        bindActionPanel(new TypeLiteral<ActionPanelPresenterWidget.ViewDef<Role, Permission>>(){},
            new TypeLiteral<ActionPanelView<Role, Permission>>(){});
        bindActionPanel(new TypeLiteral<ActionPanelPresenterWidget.ViewDef<Void, Permission>>(){},
                new TypeLiteral<ActionPanelView<Void, Permission>>(){});
        bindActionPanel(new TypeLiteral<ActionPanelPresenterWidget.ViewDef<Void, MacPool>>(){},
            new TypeLiteral<ActionPanelView<Void, MacPool>>(){});
        bindActionPanel(new TypeLiteral<ActionPanelPresenterWidget.ViewDef<Void, Role>>(){},
            new TypeLiteral<ActionPanelView<Void, Role>>(){});
        bindActionPanel(new TypeLiteral<ActionPanelPresenterWidget.ViewDef<Object, ClusterPolicy>>(){},
            new TypeLiteral<ActionPanelView<Object, ClusterPolicy>>(){});
        bindActionPanel(new TypeLiteral<ActionPanelPresenterWidget.ViewDef<Void, InstanceType>>(){},
            new TypeLiteral<ActionPanelView<Void, InstanceType>>(){});
        bindActionPanel(new TypeLiteral<ActionPanelPresenterWidget.ViewDef<StoragePool, CpuQos>>(){},
            new TypeLiteral<ActionPanelView<StoragePool, CpuQos>>(){});
        bindActionPanel(new TypeLiteral<ActionPanelPresenterWidget.ViewDef<StoragePool, NetworkQoS>>(){},
            new TypeLiteral<ActionPanelView<StoragePool, NetworkQoS>>(){});
        bindActionPanel(new TypeLiteral<ActionPanelPresenterWidget.ViewDef<StoragePool, HostNetworkQos>>(){},
            new TypeLiteral<ActionPanelView<StoragePool, HostNetworkQos>>(){});
        bindActionPanel(new TypeLiteral<ActionPanelPresenterWidget.ViewDef<?, DiskProfile>>(){},
            new TypeLiteral<ActionPanelView<?, DiskProfile>>(){});
        bindActionPanel(new TypeLiteral<ActionPanelPresenterWidget.ViewDef<?, CpuProfile>>(){},
            new TypeLiteral<ActionPanelView<?, CpuProfile>>(){});
        bindActionPanel(new TypeLiteral<ActionPanelPresenterWidget.ViewDef<VmTemplate, VmNetworkInterface>>(){},
            new TypeLiteral<ActionPanelView<VmTemplate, VmNetworkInterface>>(){});
        bindActionPanel(new TypeLiteral<ActionPanelPresenterWidget.ViewDef<VM, VmNetworkInterface>>(){},
                new TypeLiteral<ActionPanelView<VM, VmNetworkInterface>>(){});
        bindActionPanel(new TypeLiteral<ActionPanelPresenterWidget.ViewDef<?, Snapshot>>(){},
            new TypeLiteral<ActionPanelView<?, Snapshot>>(){});
        bindActionPanel(new TypeLiteral<ActionPanelPresenterWidget.ViewDef<?, HostDeviceView>>(){},
            new TypeLiteral<ActionPanelView<?, HostDeviceView>>(){});

        // Label action panels
        bindActionPanel(new TypeLiteral<DetailActionPanelPresenterWidget.ViewDef<VDS, Label>>(){},
            new TypeLiteral<DetailActionPanelView<VDS, Label>>(){});
        bindActionPanel(new TypeLiteral<DetailActionPanelPresenterWidget.ViewDef<Cluster, Label>>(){},
                new TypeLiteral<DetailActionPanelView<Cluster, Label>>(){});
        bindActionPanel(new TypeLiteral<DetailActionPanelPresenterWidget.ViewDef<VM, Label>>(){},
                new TypeLiteral<DetailActionPanelView<VM, Label>>(){});

        bindActionPanel(new TypeLiteral<DetailActionPanelPresenterWidget.ViewDef<Cluster, GlusterHookEntity>>(){},
            new TypeLiteral<DetailActionPanelView<Cluster, GlusterHookEntity>>(){});
        bindActionPanel(new TypeLiteral<DetailActionPanelPresenterWidget.ViewDef<Cluster, Network>>(){},
            new TypeLiteral<DetailActionPanelView<Cluster, Network>>(){});
        bindActionPanel(new TypeLiteral<DetailActionPanelPresenterWidget.ViewDef<StoragePool, Network>>(){},
                new TypeLiteral<DetailActionPanelView<StoragePool, Network>>(){});
        bindActionPanel(new TypeLiteral<DetailActionPanelPresenterWidget.ViewDef<StoragePool, IscsiBond>>(){},
            new TypeLiteral<DetailActionPanelView<StoragePool, IscsiBond>>(){});
        bindActionPanel(new TypeLiteral<DetailActionPanelPresenterWidget.ViewDef<GlusterVolumeEntity, GlusterBrickEntity>>(){},
            new TypeLiteral<DetailActionPanelView<GlusterVolumeEntity, GlusterBrickEntity>>(){});
        bindActionPanel(new TypeLiteral<DetailActionPanelPresenterWidget.ViewDef<GlusterVolumeEntity, GlusterVolumeOptionEntity>>(){},
            new TypeLiteral<DetailActionPanelView<GlusterVolumeEntity, GlusterVolumeOptionEntity>>(){});
        bindActionPanel(new TypeLiteral<DetailActionPanelPresenterWidget.ViewDef<VDS, StorageDevice>>(){},
            new TypeLiteral<DetailActionPanelView<VDS, StorageDevice>>(){});
        bindActionPanel(new TypeLiteral<DetailActionPanelPresenterWidget.ViewDef<GlusterVolumeEntity, GlusterVolumeSnapshotEntity>>(){},
            new TypeLiteral<DetailActionPanelView<GlusterVolumeEntity, GlusterVolumeSnapshotEntity>>(){});
        bindActionPanel(new TypeLiteral<DetailActionPanelPresenterWidget.ViewDef<VDS, GlusterServerService>>(){},
            new TypeLiteral<DetailActionPanelView<VDS, GlusterServerService>>(){});
        bindActionPanel(new TypeLiteral<DetailActionPanelPresenterWidget.ViewDef<?, NetworkCluster>>(){},
            new TypeLiteral<DetailActionPanelView<?, NetworkCluster>>(){});
        bindActionPanel(new TypeLiteral<DetailActionPanelPresenterWidget.ViewDef<NetworkView, ExternalSubnet>>(){},
            new TypeLiteral<DetailActionPanelView<NetworkView, ExternalSubnet>>(){});
        bindActionPanel(new TypeLiteral<DetailActionPanelPresenterWidget.ViewDef<Provider, LibvirtSecret>>(){},
            new TypeLiteral<DetailActionPanelView<Provider, LibvirtSecret>>(){});

        // Permission action panels
        bindActionPanel(new TypeLiteral<DetailActionPanelPresenterWidget.ViewDef<Void, Permission>>(){},
                new TypeLiteral<DetailActionPanelView<Void, Permission>>(){});
        bindActionPanel(new TypeLiteral<DetailActionPanelPresenterWidget.ViewDef<Quota, Permission>>(){},
                new TypeLiteral<DetailActionPanelView<Quota, Permission>>(){});
        bindActionPanel(new TypeLiteral<DetailActionPanelPresenterWidget.ViewDef<Role, Permission>>(){},
            new TypeLiteral<DetailActionPanelView<Role, Permission>>(){});
        bindActionPanel(new TypeLiteral<DetailActionPanelPresenterWidget.ViewDef<DbUser, Permission>>(){},
                new TypeLiteral<DetailActionPanelView<DbUser, Permission>>(){});
        bindActionPanel(new TypeLiteral<DetailActionPanelPresenterWidget.ViewDef<DiskProfile, Permission>>(){},
                new TypeLiteral<DetailActionPanelView<DiskProfile, Permission>>(){});
        bindActionPanel(new TypeLiteral<DetailActionPanelPresenterWidget.ViewDef<StoragePool, Permission>>(){},
                new TypeLiteral<DetailActionPanelView<StoragePool, Permission>>(){});
        bindActionPanel(new TypeLiteral<DetailActionPanelPresenterWidget.ViewDef<StorageDomain, Permission>>(){},
                new TypeLiteral<DetailActionPanelView<StorageDomain, Permission>>(){});
        bindActionPanel(new TypeLiteral<DetailActionPanelPresenterWidget.ViewDef<Cluster, Permission>>(){},
                new TypeLiteral<DetailActionPanelView<Cluster, Permission>>(){});
        bindActionPanel(new TypeLiteral<DetailActionPanelPresenterWidget.ViewDef<Disk, Permission>>(){},
                new TypeLiteral<DetailActionPanelView<Disk, Permission>>(){});
        bindActionPanel(new TypeLiteral<DetailActionPanelPresenterWidget.ViewDef<VDS, Permission>>(){},
                new TypeLiteral<DetailActionPanelView<VDS, Permission>>(){});
        bindActionPanel(new TypeLiteral<DetailActionPanelPresenterWidget.ViewDef<VM, Permission>>(){},
                new TypeLiteral<DetailActionPanelView<VM, Permission>>(){});
        bindActionPanel(new TypeLiteral<DetailActionPanelPresenterWidget.ViewDef<VmPool, Permission>>(){},
                new TypeLiteral<DetailActionPanelView<VmPool, Permission>>(){});
        bindActionPanel(new TypeLiteral<DetailActionPanelPresenterWidget.ViewDef<VnicProfileView, Permission>>(){},
                new TypeLiteral<DetailActionPanelView<VnicProfileView, Permission>>(){});
        bindActionPanel(new TypeLiteral<DetailActionPanelPresenterWidget.ViewDef<NetworkView, Permission>>(){},
                new TypeLiteral<DetailActionPanelView<NetworkView, Permission>>(){});
        bindActionPanel(new TypeLiteral<DetailActionPanelPresenterWidget.ViewDef<VmTemplate, Permission>>(){},
                new TypeLiteral<DetailActionPanelView<VmTemplate, Permission>>(){});
        bindActionPanel(new TypeLiteral<DetailActionPanelPresenterWidget.ViewDef<GlusterVolumeEntity, Permission>>(){},
                new TypeLiteral<DetailActionPanelView<GlusterVolumeEntity, Permission>>(){});


        bindActionPanel(new TypeLiteral<DetailActionPanelPresenterWidget.ViewDef<GlusterVolumeEntity, GlusterGeoRepSession>>(){},
            new TypeLiteral<DetailActionPanelView<GlusterVolumeEntity, GlusterGeoRepSession>>(){});
        bindActionPanel(new TypeLiteral<DetailActionPanelPresenterWidget.ViewDef<NetworkView, PairQueryable<Cluster, NetworkCluster>>>(){},
            new TypeLiteral<DetailActionPanelView<NetworkView, PairQueryable<Cluster, NetworkCluster>>>(){});
        bindActionPanel(new TypeLiteral<DetailActionPanelPresenterWidget.ViewDef<NetworkView, PairQueryable<VdsNetworkInterface, VDS>>>(){},
            new TypeLiteral<DetailActionPanelView<NetworkView, PairQueryable<VdsNetworkInterface, VDS>>>(){});
        bindActionPanel(new TypeLiteral<DetailActionPanelPresenterWidget.ViewDef<NetworkView, PairQueryable<VmNetworkInterface, VM>>>(){},
            new TypeLiteral<DetailActionPanelView<NetworkView, PairQueryable<VmNetworkInterface, VM>>>(){});
        bindActionPanel(new TypeLiteral<DetailActionPanelPresenterWidget.ViewDef<NetworkView, PairQueryable<VmNetworkInterface, VmTemplate>>>(){},
            new TypeLiteral<DetailActionPanelView<NetworkView, PairQueryable<VmNetworkInterface, VmTemplate>>>(){});
        bindActionPanel(new TypeLiteral<DetailActionPanelPresenterWidget.ViewDef<StoragePool, StorageDomainDR>>(){},
            new TypeLiteral<DetailActionPanelView<StoragePool, StorageDomainDR>>(){});
        bindActionPanel(new TypeLiteral<DetailActionPanelPresenterWidget.ViewDef<StorageDomain, StorageDomainDR>>(){},
                new TypeLiteral<DetailActionPanelView<StorageDomain, StorageDomainDR>>(){});
        bindActionPanel(new TypeLiteral<DetailActionPanelPresenterWidget.ViewDef<StorageDomain, RepoImage>>(){},
            new TypeLiteral<DetailActionPanelView<StorageDomain, RepoImage>>(){});
        bindActionPanel(new TypeLiteral<DetailActionPanelPresenterWidget.ViewDef<VmTemplate, DiskModel>>(){},
            new TypeLiteral<DetailActionPanelView<VmTemplate, DiskModel>>(){});
        bindActionPanel(new TypeLiteral<DetailActionPanelPresenterWidget.ViewDef<DbUser, EventSubscriber>>(){},
            new TypeLiteral<DetailActionPanelView<DbUser, EventSubscriber>>(){});
        bindActionPanel(new TypeLiteral<DetailActionPanelPresenterWidget.ViewDef<Cluster, AffinityGroup>>(){},
            new TypeLiteral<DetailActionPanelView<Cluster, AffinityGroup>>(){});
        bindActionPanel(new TypeLiteral<DetailActionPanelPresenterWidget.ViewDef<VM, AffinityGroup>>(){},
                new TypeLiteral<DetailActionPanelView<VM, AffinityGroup>>(){});
        bindActionPanel(new TypeLiteral<DetailActionPanelPresenterWidget.ViewDef<?, MacPool>>(){},
            new TypeLiteral<DetailActionPanelView<?, MacPool>>(){});
        bindActionPanel(new TypeLiteral<DetailActionPanelPresenterWidget.ViewDef<?, Role>>(){},
            new TypeLiteral<DetailActionPanelView<?, Role>>(){});
        bindActionPanel(new TypeLiteral<DetailActionPanelPresenterWidget.ViewDef<?, ClusterPolicy>>(){},
            new TypeLiteral<DetailActionPanelView<?, ClusterPolicy>>(){});
        bindActionPanel(new TypeLiteral<DetailActionPanelPresenterWidget.ViewDef<Void, InstanceType>>(){},
            new TypeLiteral<DetailActionPanelView<Void, InstanceType>>(){});
        bindActionPanel(new TypeLiteral<DetailActionPanelPresenterWidget.ViewDef<StoragePool, CpuQos>>(){},
            new TypeLiteral<DetailActionPanelView<StoragePool, CpuQos>>(){});
        bindActionPanel(new TypeLiteral<DetailActionPanelPresenterWidget.ViewDef<StoragePool, NetworkQoS>>(){},
            new TypeLiteral<DetailActionPanelView<StoragePool, NetworkQoS>>(){});
        bindActionPanel(new TypeLiteral<DetailActionPanelPresenterWidget.ViewDef<StoragePool, HostNetworkQos>>(){},
            new TypeLiteral<DetailActionPanelView<StoragePool, HostNetworkQos>>(){});
        bindActionPanel(new TypeLiteral<DetailActionPanelPresenterWidget.ViewDef<StorageDomain, DiskProfile>>(){},
            new TypeLiteral<DetailActionPanelView<StorageDomain, DiskProfile>>(){});
        bindActionPanel(new TypeLiteral<DetailActionPanelPresenterWidget.ViewDef<Cluster, CpuProfile>>(){},
            new TypeLiteral<DetailActionPanelView<Cluster, CpuProfile>>(){});
        bindActionPanel(new TypeLiteral<DetailActionPanelPresenterWidget.ViewDef<VmTemplate, VmNetworkInterface>>(){},
            new TypeLiteral<DetailActionPanelView<VmTemplate, VmNetworkInterface>>(){});
        bindActionPanel(new TypeLiteral<DetailActionPanelPresenterWidget.ViewDef<VM, VmNetworkInterface>>(){},
                new TypeLiteral<DetailActionPanelView<VM, VmNetworkInterface>>(){});
        bindActionPanel(new TypeLiteral<DetailActionPanelPresenterWidget.ViewDef<VM, Snapshot>>(){},
            new TypeLiteral<DetailActionPanelView<VM, Snapshot>>(){});
        bindActionPanel(new TypeLiteral<DetailActionPanelPresenterWidget.ViewDef<VM, HostDeviceView>>(){},
            new TypeLiteral<DetailActionPanelView<VM, HostDeviceView>>(){});
        bindActionPanel(new TypeLiteral<DetailActionPanelPresenterWidget.ViewDef<VDS, HostInterfaceLineModel>>(){},
            new TypeLiteral<DetailActionPanelView<VDS, HostInterfaceLineModel>>(){});
        bindActionPanel(new TypeLiteral<DetailActionPanelPresenterWidget.ViewDef<StoragePool, StorageQos>>(){},
            new TypeLiteral<DetailActionPanelView<StoragePool, StorageQos>>(){});
        bindActionPanel(new TypeLiteral<DetailActionPanelPresenterWidget.ViewDef<Cluster, VDS>>(){},
                new TypeLiteral<DetailActionPanelView<Cluster, VDS>>(){});
        bindActionPanel(new TypeLiteral<DetailActionPanelPresenterWidget.ViewDef<StoragePool, Quota>>(){},
                new TypeLiteral<DetailActionPanelView<StoragePool, Quota>>(){});
        bindActionPanel(new TypeLiteral<DetailActionPanelPresenterWidget.ViewDef<VDS, VM>>(){},
                new TypeLiteral<DetailActionPanelView<VDS, VM>>(){});
        bindActionPanel(new TypeLiteral<DetailActionPanelPresenterWidget.ViewDef<VmPool, VM>>(){},
                new TypeLiteral<DetailActionPanelView<VmPool, VM>>(){});
        bindActionPanel(new TypeLiteral<DetailActionPanelPresenterWidget.ViewDef<StorageDomain, VM>>(){},
                new TypeLiteral<DetailActionPanelView<StorageDomain, VM>>(){});
        bindActionPanel(new TypeLiteral<DetailActionPanelPresenterWidget.ViewDef<NetworkView, VnicProfileView>>(){},
                new TypeLiteral<DetailActionPanelView<NetworkView, VnicProfileView>>(){});
        bindActionPanel(new TypeLiteral<DetailActionPanelPresenterWidget.ViewDef<Provider, NetworkView>>(){},
                new TypeLiteral<DetailActionPanelView<Provider, NetworkView>>(){});
        bindActionPanel(new TypeLiteral<DetailActionPanelPresenterWidget.ViewDef<StorageDomain, Disk>>(){},
                new TypeLiteral<DetailActionPanelView<StorageDomain, Disk>>(){});
        bindActionPanel(new TypeLiteral<DetailActionPanelPresenterWidget.ViewDef<VM, Disk>>(){},
                new TypeLiteral<DetailActionPanelView<VM, Disk>>(){});
        bindActionPanel(new TypeLiteral<DetailActionPanelPresenterWidget.ViewDef<StorageDomain, VmTemplate>>(){},
                new TypeLiteral<DetailActionPanelView<StorageDomain, VmTemplate>>(){});
        bindActionPanel(new TypeLiteral<DetailActionPanelPresenterWidget.ViewDef<VmTemplate, StorageDomain>>(){},
                new TypeLiteral<DetailActionPanelView<VmTemplate, StorageDomain>>(){});
        bindActionPanel(new TypeLiteral<DetailActionPanelPresenterWidget.ViewDef<StoragePool, StorageDomain>>(){},
                new TypeLiteral<DetailActionPanelView<StoragePool, StorageDomain>>(){});
    }

    <P extends PresenterWidget<?>, V extends View> void bindSingletonPresenterWidget(
            TypeLiteral<P> presenterImpl, TypeLiteral<V> view, TypeLiteral<? extends V> viewImpl) {
        bind(presenterImpl).in(Singleton.class);
        bind(view).to(viewImpl).in(Singleton.class);
    }

    <P extends PresenterWidget<?>, V extends View> void bindPresenterWidget(
            TypeLiteral<P> presenterImpl, TypeLiteral<V> view, TypeLiteral<? extends V> viewImpl) {
        bind(presenterImpl);
        bind(view).to(viewImpl);
    }

    <V extends ActionPanelPresenterWidget.ViewDef<?, ?>> void bindActionPanel(
            TypeLiteral<V> view, TypeLiteral<? extends V> viewImpl) {
        bind(view).to(viewImpl);
    }

}
