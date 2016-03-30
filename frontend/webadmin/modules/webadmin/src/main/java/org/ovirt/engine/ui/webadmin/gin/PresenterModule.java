package org.ovirt.engine.ui.webadmin.gin;

import org.ovirt.engine.ui.common.gin.BasePresenterModule;
import org.ovirt.engine.ui.webadmin.section.main.presenter.AboutPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.HeaderPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.MainContentPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.MainSectionPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.MainTabPanelPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.ReportPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.SearchPanelPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.AssignTagsPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.CpuQosPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.HostErrataListWithDetailsPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.HostNetworkQosPopupPresenterWidget;
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
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.macpool.SharedMacPoolPopupPresenterWidget;
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
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.storage.StorageDestroyPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.storage.StorageForceCreatePopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.storage.StoragePopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.storage.StorageRemovePopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.storage.UploadImagePopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.storage.backup.ImportCloneDialogPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.storage.backup.ImportTemplatePopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.storage.backup.ImportVmFromExportDomainPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.tag.TagPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.template.TemplateEditPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.template.TemplateInterfacePopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.user.ManageEventsPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.vm.CloneVmPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.vm.ImportVmFromExternalProviderPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.vm.SingleSelectionVmDiskAttachPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.vm.VmChangeCDPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.vm.VmClonePopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.vm.VmDiskAttachPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.vm.VmDiskPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.vm.VmDiskRemovePopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.vm.VmExportPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.vm.VmInterfacePopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.vm.VmMakeTemplatePopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.vm.VmMigratePopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.vm.VmNextRunConfigurationPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.vm.VmPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.vm.VmRunOncePopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.vm.VmSnapshotCreatePopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.vm.VmSnapshotCustomPreviewPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.vm.VmSnapshotPreviewPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.vm.VncInfoPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.MainTabClusterPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.MainTabDataCenterPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.MainTabDiskPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.MainTabEngineErrataPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.MainTabEventPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.MainTabHostPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.MainTabNetworkPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.MainTabPoolPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.MainTabProviderPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.MainTabQuotaPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.MainTabReportsPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.MainTabSessionPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.MainTabStoragePresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.MainTabTemplatePresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.MainTabUserPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.MainTabVirtualMachinePresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.MainTabVnicProfilePresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.MainTabVolumePresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.cluster.ClusterSubTabPanelPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.cluster.SubTabClusterAffinityGroupPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.cluster.SubTabClusterCpuProfilePresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.cluster.SubTabClusterGeneralPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.cluster.SubTabClusterGlusterHookPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.cluster.SubTabClusterHostPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.cluster.SubTabClusterNetworkPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.cluster.SubTabClusterPermissionPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.cluster.SubTabClusterServicePresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.cluster.SubTabClusterVmPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.datacenter.DataCenterQosSubTabPanelPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.datacenter.DataCenterSubTabPanelPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.datacenter.SubTabDataCenterClusterPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.datacenter.SubTabDataCenterCpuQosPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.datacenter.SubTabDataCenterEventPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.datacenter.SubTabDataCenterHostNetworkQosPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.datacenter.SubTabDataCenterIscsiBondPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.datacenter.SubTabDataCenterNetworkPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.datacenter.SubTabDataCenterNetworkQoSPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.datacenter.SubTabDataCenterPermissionPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.datacenter.SubTabDataCenterQuotaPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.datacenter.SubTabDataCenterStoragePresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.datacenter.SubTabDataCenterStorageQosPresenter;
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
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.host.HostGeneralSubTabPanelPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.host.HostSubTabPanelPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.host.SubTabHostBrickPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.host.SubTabHostDevicePresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.host.SubTabHostEventPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.host.SubTabHostGeneralHardwarePresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.host.SubTabHostGeneralHostErrataPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.host.SubTabHostGeneralInfoPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.host.SubTabHostGeneralSoftwarePresenter;
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
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.storage.SubTabStorageDataCenterPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.storage.SubTabStorageDiskPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.storage.SubTabStorageDiskProfilePresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.storage.SubTabStorageEventPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.storage.SubTabStorageGeneralPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.storage.SubTabStorageIsoPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.storage.SubTabStoragePermissionPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.storage.SubTabStorageRegisterDiskImagePresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.storage.SubTabStorageRegisterDiskPresenter;
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
import org.ovirt.engine.ui.webadmin.section.main.view.MainContentView;
import org.ovirt.engine.ui.webadmin.section.main.view.MainSectionView;
import org.ovirt.engine.ui.webadmin.section.main.view.MainTabPanelView;
import org.ovirt.engine.ui.webadmin.section.main.view.ReportView;
import org.ovirt.engine.ui.webadmin.section.main.view.SearchPanelView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.AssignTagsPopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.HostErrataListWithDetailsPopupView;
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
import org.ovirt.engine.ui.webadmin.section.main.view.popup.macpool.SharedMacPoolPopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.networkQoS.NetworkQoSPopupView;
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
import org.ovirt.engine.ui.webadmin.section.main.view.popup.storage.StorageDestroyPopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.storage.StorageForceCreatePopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.storage.StoragePopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.storage.StorageRemovePopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.storage.UploadImagePopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.storage.backup.ImportCloneDialogPopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.storage.backup.ImportTemplatePopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.storage.backup.ImportVmFromExportDomainPopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.tag.TagPopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.template.TemplateEditPopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.template.TemplateInterfacePopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.user.ManageEventsPopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.vm.AddVmHostDevicePopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.vm.CloneVmPopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.vm.ImportVmFromExternalProviderPopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.vm.SingleSelectionVmDiskAttachPopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.vm.VmChangeCDPopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.vm.VmClonePopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.vm.VmDiskAttachPopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.vm.VmDiskPopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.vm.VmDiskRemovePopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.vm.VmExportPopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.vm.VmInterfacePopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.vm.VmMakeTemplatePopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.vm.VmMigratePopupView;
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
import org.ovirt.engine.ui.webadmin.section.main.view.tab.MainTabClusterView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.MainTabDataCenterView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.MainTabDiskView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.MainTabEngineErrataView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.MainTabEventView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.MainTabHostView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.MainTabNetworkView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.MainTabPoolView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.MainTabProviderView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.MainTabQuotaView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.MainTabReportsView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.MainTabSessionView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.MainTabStorageView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.MainTabTemplateView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.MainTabUserView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.MainTabVirtualMachineView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.MainTabVnicProfileView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.MainTabVolumeView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.cluster.ClusterSubTabPanelView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.cluster.SubTabClusterAffinityGroupView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.cluster.SubTabClusterCpuProfileView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.cluster.SubTabClusterGeneralView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.cluster.SubTabClusterGlusterHookView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.cluster.SubTabClusterHostView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.cluster.SubTabClusterNetworkView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.cluster.SubTabClusterPermissionView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.cluster.SubTabClusterServiceView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.cluster.SubTabClusterVmView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.datacenter.DataCenterQosSubTabPanelView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.datacenter.DataCenterSubTabPanelView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.datacenter.SubTabDataCenterClusterView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.datacenter.SubTabDataCenterCpuQosView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.datacenter.SubTabDataCenterEventView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.datacenter.SubTabDataCenterHostNetworkQosView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.datacenter.SubTabDataCenterIscsiBondView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.datacenter.SubTabDataCenterNetworkQoSView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.datacenter.SubTabDataCenterNetworkView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.datacenter.SubTabDataCenterPermissionView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.datacenter.SubTabDataCenterQuotaView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.datacenter.SubTabDataCenterStorageQosView;
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
import org.ovirt.engine.ui.webadmin.section.main.view.tab.host.HostGeneralSubTabPanelView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.host.HostSubTabPanelView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.host.SubTabHostBrickView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.host.SubTabHostDeviceView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.host.SubTabHostEventView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.host.SubTabHostGeneralHardwareView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.host.SubTabHostGeneralHostErrataView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.host.SubTabHostGeneralInfoView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.host.SubTabHostGeneralSoftwareView;
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
import org.ovirt.engine.ui.webadmin.section.main.view.tab.storage.SubTabStorageDataCenterView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.storage.SubTabStorageDiskProfileView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.storage.SubTabStorageDiskView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.storage.SubTabStorageEventView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.storage.SubTabStorageGeneralView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.storage.SubTabStorageIsoView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.storage.SubTabStoragePermissionView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.storage.SubTabStorageRegisterDiskImageView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.storage.SubTabStorageRegisterDiskView;
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

/**
 * GIN module containing WebAdmin GWTP presenter bindings.
 */
public class PresenterModule extends BasePresenterModule {

    @Override
    protected void configure() {
        // Common stuff
        bindCommonPresenters();

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
        bindSingletonPresenterWidget(SearchPanelPresenterWidget.class,
                SearchPanelPresenterWidget.ViewDef.class,
                SearchPanelView.class);
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

        // Main section: main tabs
        bindPresenter(MainTabPanelPresenter.class,
                MainTabPanelPresenter.ViewDef.class,
                MainTabPanelView.class,
                MainTabPanelPresenter.ProxyDef.class);
        bindPresenter(MainTabDataCenterPresenter.class,
                MainTabDataCenterPresenter.ViewDef.class,
                MainTabDataCenterView.class,
                MainTabDataCenterPresenter.ProxyDef.class);
        bindPresenter(MainTabClusterPresenter.class,
                MainTabClusterPresenter.ViewDef.class,
                MainTabClusterView.class,
                MainTabClusterPresenter.ProxyDef.class);
        bindPresenter(MainTabHostPresenter.class,
                MainTabHostPresenter.ViewDef.class,
                MainTabHostView.class,
                MainTabHostPresenter.ProxyDef.class);
        bindPresenter(MainTabNetworkPresenter.class,
                MainTabNetworkPresenter.ViewDef.class,
                MainTabNetworkView.class,
                MainTabNetworkPresenter.ProxyDef.class);
        bindPresenter(MainTabVnicProfilePresenter.class,
                MainTabVnicProfilePresenter.ViewDef.class,
                MainTabVnicProfileView.class,
                MainTabVnicProfilePresenter.ProxyDef.class);
        bindPresenter(MainTabProviderPresenter.class,
                MainTabProviderPresenter.ViewDef.class,
                MainTabProviderView.class,
                MainTabProviderPresenter.ProxyDef.class);
        bindPresenter(MainTabEngineErrataPresenter.class,
                MainTabEngineErrataPresenter.ViewDef.class,
                MainTabEngineErrataView.class,
                MainTabEngineErrataPresenter.ProxyDef.class);
        bindPresenter(MainTabSessionPresenter.class,
                MainTabSessionPresenter.ViewDef.class,
                MainTabSessionView.class,
                MainTabSessionPresenter.ProxyDef.class);
        bindPresenter(MainTabStoragePresenter.class,
                MainTabStoragePresenter.ViewDef.class,
                MainTabStorageView.class,
                MainTabStoragePresenter.ProxyDef.class);
        bindPresenter(MainTabVirtualMachinePresenter.class,
                MainTabVirtualMachinePresenter.ViewDef.class,
                MainTabVirtualMachineView.class,
                MainTabVirtualMachinePresenter.ProxyDef.class);
        bindPresenter(MainTabPoolPresenter.class,
                MainTabPoolPresenter.ViewDef.class,
                MainTabPoolView.class,
                MainTabPoolPresenter.ProxyDef.class);
        bindPresenter(MainTabTemplatePresenter.class,
                MainTabTemplatePresenter.ViewDef.class,
                MainTabTemplateView.class,
                MainTabTemplatePresenter.ProxyDef.class);
        bindPresenter(MainTabUserPresenter.class,
                MainTabUserPresenter.ViewDef.class,
                MainTabUserView.class,
                MainTabUserPresenter.ProxyDef.class);
        bindPresenter(MainTabEventPresenter.class,
                MainTabEventPresenter.ViewDef.class,
                MainTabEventView.class,
                MainTabEventPresenter.ProxyDef.class);
        bindPresenter(MainTabReportsPresenter.class,
                MainTabReportsPresenter.ViewDef.class,
                MainTabReportsView.class,
                MainTabReportsPresenter.ProxyDef.class);
        bindPresenter(MainTabQuotaPresenter.class,
                MainTabQuotaPresenter.ViewDef.class,
                MainTabQuotaView.class,
                MainTabQuotaPresenter.ProxyDef.class);
        bindPresenter(MainTabVolumePresenter.class,
                MainTabVolumePresenter.ViewDef.class,
                MainTabVolumeView.class,
                MainTabVolumePresenter.ProxyDef.class);
        bindPresenter(MainTabDiskPresenter.class,
                MainTabDiskPresenter.ViewDef.class,
                MainTabDiskView.class,
                MainTabDiskPresenter.ProxyDef.class);

        // Main section: sub tabs

        // DataCenter
        bindPresenter(DataCenterSubTabPanelPresenter.class,
                DataCenterSubTabPanelPresenter.ViewDef.class,
                DataCenterSubTabPanelView.class,
                DataCenterSubTabPanelPresenter.ProxyDef.class);
        bindPresenter(DataCenterQosSubTabPanelPresenter.class,
                DataCenterQosSubTabPanelPresenter.ViewDef.class,
                DataCenterQosSubTabPanelView.class,
                DataCenterQosSubTabPanelPresenter.ProxyDef.class);
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
        bindPresenter(SubTabDataCenterNetworkQoSPresenter.class,
                SubTabDataCenterNetworkQoSPresenter.ViewDef.class,
                SubTabDataCenterNetworkQoSView.class,
                SubTabDataCenterNetworkQoSPresenter.ProxyDef.class);
        bindPresenter(SubTabDataCenterStorageQosPresenter.class,
                SubTabDataCenterStorageQosPresenter.ViewDef.class,
                SubTabDataCenterStorageQosView.class,
                SubTabDataCenterStorageQosPresenter.ProxyDef.class);
        bindPresenter(SubTabDataCenterCpuQosPresenter.class,
                SubTabDataCenterCpuQosPresenter.ViewDef.class,
                SubTabDataCenterCpuQosView.class,
                SubTabDataCenterCpuQosPresenter.ProxyDef.class);
        bindPresenter(SubTabDataCenterHostNetworkQosPresenter.class,
                SubTabDataCenterHostNetworkQosPresenter.ViewDef.class,
                SubTabDataCenterHostNetworkQosView.class,
                SubTabDataCenterHostNetworkQosPresenter.ProxyDef.class);
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
        bindPresenter(SubTabStorageRegisterDiskPresenter.class,
                SubTabStorageRegisterDiskPresenter.ViewDef.class,
                SubTabStorageRegisterDiskView.class,
                SubTabStorageRegisterDiskPresenter.ProxyDef.class);
        bindPresenter(SubTabStorageRegisterDiskImagePresenter.class,
                SubTabStorageRegisterDiskImagePresenter.ViewDef.class,
                SubTabStorageRegisterDiskImageView.class,
                SubTabStorageRegisterDiskImagePresenter.ProxyDef.class);
        bindPresenter(SubTabStorageSnapshotPresenter.class,
                SubTabStorageSnapshotPresenter.ViewDef.class,
                SubTabStorageSnapshotView.class,
                SubTabStorageSnapshotPresenter.ProxyDef.class);
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

        // Host
        bindPresenter(HostSubTabPanelPresenter.class,
                HostSubTabPanelPresenter.ViewDef.class,
                HostSubTabPanelView.class,
                HostSubTabPanelPresenter.ProxyDef.class);
        bindPresenter(HostGeneralSubTabPanelPresenter.class,
                HostGeneralSubTabPanelPresenter.ViewDef.class,
                HostGeneralSubTabPanelView.class,
                HostGeneralSubTabPanelPresenter.ProxyDef.class);
        bindPresenter(SubTabHostGeneralSoftwarePresenter.class,
                SubTabHostGeneralSoftwarePresenter.ViewDef.class,
                SubTabHostGeneralSoftwareView.class,
                SubTabHostGeneralSoftwarePresenter.ProxyDef.class);
        bindPresenter(SubTabHostGeneralInfoPresenter.class,
                SubTabHostGeneralInfoPresenter.ViewDef.class,
                SubTabHostGeneralInfoView.class,
                SubTabHostGeneralInfoPresenter.ProxyDef.class);
        bindPresenter(SubTabHostGeneralHardwarePresenter.class,
                SubTabHostGeneralHardwarePresenter.ViewDef.class,
                SubTabHostGeneralHardwareView.class,
                SubTabHostGeneralHardwarePresenter.ProxyDef.class);
        bindPresenter(SubTabHostGeneralHostErrataPresenter.class,
                SubTabHostGeneralHostErrataPresenter.ViewDef.class,
                SubTabHostGeneralHostErrataView.class,
                SubTabHostGeneralHostErrataPresenter.ProxyDef.class);
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

        // Errata
        bindPresenter(ErrataSubTabPanelPresenter.class,
                ErrataSubTabPanelPresenter.ViewDef.class,
                ErrataSubTabPanelView.class,
                ErrataSubTabPanelPresenter.ProxyDef.class);
        bindPresenter(SubTabEngineErrataDetailsPresenter.class,
                SubTabEngineErrataDetailsPresenter.ViewDef.class,
                SubTabEngineErrataDetailsView.class,
                SubTabEngineErrataDetailsPresenter.ProxyDef.class);

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
        bindPresenterWidget(RegisterVmPopupPresenterWidget.class,
                RegisterVmPopupPresenterWidget.ViewDef.class,
                RegisterVmPopupView.class);
        bindPresenterWidget(RegisterTemplatePopupPresenterWidget.class,
                RegisterTemplatePopupPresenterWidget.ViewDef.class,
                RegisterTemplatePopupView.class);
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

        // VM Migrate
        bindPresenterWidget(VmMigratePopupPresenterWidget.class,
                VmMigratePopupPresenterWidget.ViewDef.class,
                VmMigratePopupView.class);

        // VM Export
        bindPresenterWidget(VmExportPopupPresenterWidget.class,
                VmExportPopupPresenterWidget.ViewDef.class,
                VmExportPopupView.class);

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

        // Edit Template
        bindPresenterWidget(TemplateEditPresenterWidget.class,
                TemplateEditPresenterWidget.ViewDef.class,
                TemplateEditPopupView.class);

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

        // Reports
        bindPresenterWidget(ReportPresenterWidget.class,
                ReportPresenterWidget.ViewDef.class,
                ReportView.class);
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

    }
}
