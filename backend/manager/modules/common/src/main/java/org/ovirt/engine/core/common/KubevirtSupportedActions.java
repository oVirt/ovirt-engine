package org.ovirt.engine.core.common;

import java.util.EnumSet;
import java.util.Set;

import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.businessentities.Managed;

public final class KubevirtSupportedActions {

    private static Set<ActionType> SUPPORTED_ACTIONS = EnumSet.of(
            ActionType.AddVm,
//            ActionType.AddVmFromTemplate,
            ActionType.AddVmFromScratch,
            ActionType.AddUnmanagedVms,
            ActionType.RemoveVm,
            // ActionType.UpdateVm,
            ActionType.RebootVm,
            ActionType.StopVm,
            ActionType.ShutdownVm,
            // ActionType.HibernateVm,
            // ActionType.ChangeDisk,
            // ActionType.HibernateVm,
            ActionType.RunVm,
            // ActionType.RunVmOnce,
            ActionType.MigrateVm,
            // ActionType.MigrateVmToServer,
            ActionType.MigrateMultipleVms,
            // ActionType.ReorderVmNics,
            ActionType.VmLogon,
            // ActionType.SetVmTicket,
            // ActionType.ExportVm,
            // ActionType.ExportVmTemplate,
            // ActionType.RestoreStatelessVm,
            // ActionType.ExportVmToOva,
            // ActionType.CreateOva,
            // ActionType.AddVmInterface,
            // ActionType.RemoveVmInterface,
            // ActionType.UpdateVmInterface,
            // ActionType.AddDisk,
            // ActionType.RegisterDisk,
            // ActionType.ExtractOva,
            // ActionType.UpdateDisk,
            // ActionType.ExportVmTemplateToOva,
            // ActionType.AttachDiskToVm,
            // ActionType.DetachDiskFromVm,
            // ActionType.HotPlugDiskToVm,
            // ActionType.HotUnPlugDiskFromVm,
            // ActionType.HotSetNumberOfCpus,
            // ActionType.VmSlaPolicy,
            // ActionType.HotSetAmountOfMemory,
            // ActionType.ImportVm,
            // ActionType.RemoveVmFromImportExport,
            // ActionType.RemoveVmTemplateFromImportExport,
            // ActionType.ImportVmTemplate,
            // ActionType.ImportVmTemplateFromOva,
            // ActionType.AddDiskToTemplate,
            // ActionType.ChangeVMCluster,
            // ActionType.CancelMigrateVm,
            // ActionType.ActivateDeactivateVmNic,
            // ActionType.AddVmFromSnapshot,
            // ActionType.CloneVm,
            // ActionType.CloneVmNoCollapse,
            // ActionType.ImportVmFromConfiguration,
            // ActionType.UpdateVmVersion,
            // ActionType.ImportVmTemplateFromConfiguration,
            // ActionType.ProcessDownVm,
            // ActionType.ConvertVm,
            // ActionType.ImportVmFromExternalProvider,
            // ActionType.ImportVmFromOva,
            // ActionType.ConvertOva,
            // ActionType.CancelConvertVm,
            // ActionType.ImportVmFromExternalUrl,
            // ActionType.AddVmNicFilterParameter,
            // ActionType.UpdateVmNicFilterParameter,
            // ActionType.RemoveVmNicFilterParameter,
            // ActionType.UpdateConvertedVm,
            // ActionType.RemoveUnregisteredVmTemplate,
            // ActionType.RemoveUnregisteredVm,

            // VdsCommands
            // ActionType.AddVds,
            // ActionType.UpdateVds,
            // ActionType.RemoveVds,
            // ActionType.RestartVds,
            // ActionType.VdsNotRespondingTreatment,
            // ActionType.MaintenanceVds,
            // ActionType.MaintenanceNumberOfVdss,
            // ActionType.ActivateVds,
            // ActionType.InstallVdsInternal,
            // ActionType.ClearNonResponsiveVdsVms,
            // ActionType.SshHostReboot,
            // ActionType.ApproveVds,
            // ActionType.HandleVdsCpuFlagsOrClusterChanged,
            // ActionType.InitVdsOnUp,
            // ActionType.SetNonOperationalVds,
            // ActionType.AddVdsSpmId,
            // ActionType.ForceSelectSPM,

            // Fencing
            // ActionType.StartVds,
            // ActionType.StopVds,
            // ActionType.HandleVdsVersion,
            // ActionType.ChangeVDSCluster,
            // ActionType.RefreshHostCapabilities,
            // ActionType.SshSoftFencing,
            // ActionType.VdsPowerDown,
            // ActionType.InstallVds,
            // ActionType.VdsKdumpDetection,
            // ActionType.AddFenceAgent,
            // ActionType.RemoveFenceAgent,
            // ActionType.UpdateFenceAgent,
            // ActionType.RemoveFenceAgentsByVdsId,
            // ActionType.UpgradeHost,
            // ActionType.UpgradeHostInternal,
            // ActionType.HostEnrollCertificate,

            // ActionType.HostEnrollCertificateInternal,
            // ActionType.HostUpgradeCheck,
            // ActionType.HostUpgradeCheckInternal,

            // Network
            // ActionType.AddNetwork,
            // ActionType.RemoveNetwork,
            // ActionType.UpdateNetwork,
            // ActionType.CommitNetworkChanges,
            // ActionType.ImportExternalNetwork,
            // ActionType.InternalImportExternalNetwork,

            // VnicProfile Commands
            // ActionType.AddVnicProfile,
            // ActionType.UpdateVnicProfile,
            // ActionType.RemoveVnicProfile,

            // Network labels
            // ActionType.LabelNetwork,
            // ActionType.UnlabelNetwork,
            // ActionType.LabelNic,
            // ActionType.UnlabelNic,
            // ActionType.PropagateNetworksToClusterHosts,

            // SR-IOV
            // ActionType.UpdateHostNicVfsConfig,
            // ActionType.AddVfsConfigNetwork,
            // ActionType.RemoveVfsConfigNetwork,
            // ActionType.AddVfsConfigLabel,
            // ActionType.RemoveVfsConfigLabel,

            // NUMA
            // ActionType.AddVmNumaNodes,
            // ActionType.UpdateVmNumaNodes,
            // ActionType.RemoveVmNumaNodes,
            // ActionType.SetVmNumaNodes,

            // VmTemplatesCommand
            // ActionType.AddVmTemplate,
            // ActionType.UpdateVmTemplate,
            // ActionType.RemoveVmTemplate,
            // ActionType.AddVmTemplateInterface,
            // ActionType.RemoveVmTemplateInterface,
            // ActionType.UpdateVmTemplateInterface,
            // ActionType.AddVmTemplateFromSnapshot,
            // ActionType.SealVmTemplate,

            // ImagesCommands
            // ActionType.TryBackToSnapshot,
            // ActionType.RestoreFromSnapshot,
            // ActionType.CreateSnapshotForVm,
            // ActionType.CreateSnapshot,
            // ActionType.CreateSnapshotFromTemplate,
            // ActionType.CreateImageTemplate,
            // ActionType.RemoveSnapshot,
            // ActionType.RemoveImage,
            // ActionType.RemoveAllVmImages,
            // ActionType.AddImageFromScratch,
            // ActionType.RemoveTemplateSnapshot,
            // ActionType.RemoveAllVmTemplateImageTemplates,
            // ActionType.TryBackToAllSnapshotsOfVm,
            // ActionType.RestoreAllSnapshots,
            // ActionType.CopyImageGroup,
            // ActionType.MoveOrCopyDisk,
            // ActionType.CreateCloneOfTemplate,
            // ActionType.RemoveDisk,
            // ActionType.MoveImageGroup,
            // ActionType.AmendVolume,
            // ActionType.RemoveMemoryVolumes,
            // ActionType.RemoveDiskSnapshots,
            // ActionType.RemoveSnapshotSingleDiskLive,
            // ActionType.Merge,
            // ActionType.MergeStatus,
            // ActionType.DestroyImage,
            // ActionType.MergeExtend,
            // ActionType.SparsifyImage,
            // ActionType.AmendImageGroupVolumes,
            // ActionType.ColdMergeSnapshotSingleDisk,
            // ActionType.PrepareMerge,
            // ActionType.ColdMerge,
            // ActionType.FinalizeMerge,
            // ActionType.CreateAllTemplateDisks,
            // ActionType.CreateAllTemplateDisksFromSnapshot,
            // ActionType.UpdateVolume,
            // ActionType.UpdateAllTemplateDisks,
            // ActionType.CreateSnapshotDisk,

            // VmPoolCommands
            // ActionType.AddVmPool,
            // ActionType.UpdateVmPool,
            // ActionType.RemoveVmPool,
            // ActionType.DetachUserFromVmFromPool,
            // ActionType.AddVmToPool,
            // ActionType.RemoveVmFromPool,
            // ActionType.AttachUserToVmFromPoolAndRun,

            // UserAndGroupsCommands
            // ActionType.LogoutSession,
            // ActionType.RemoveUser,
            // ActionType.TerminateSession,
            // ActionType.TerminateSessionsForToken,
            // ActionType.RemoveGroup,
            // ActionType.AddUser,
            // ActionType.AddGroup,
            // ActionType.LoginOnBehalf,
            // ActionType.CreateUserSession,

            // UserProfile
            // ActionType.AddUserProfile,
            // ActionType.UpdateUserProfile,
            // ActionType.RemoveUserProfile,

            // Tags
            // ActionType.AddTag,
            // ActionType.RemoveTag,
            // ActionType.UpdateTag,
            // ActionType.MoveTag,
            // ActionType.AttachUserToTag,
            // ActionType.DetachUserFromTag,
            // ActionType.AttachUserGroupToTag,
            // ActionType.DetachUserGroupFromTag,
            // ActionType.AttachVmsToTag,
            // ActionType.DetachVmFromTag,
            // ActionType.AttachVdsToTag,
            // ActionType.DetachVdsFromTag,
            // ActionType.UpdateTagsVmMapDefaultDisplayType,
            // ActionType.AttachTemplatesToTag,
            // ActionType.DetachTemplateFromTag,

            // Quota
            // ActionType.AddQuota,
            // ActionType.UpdateQuota,
            // ActionType.RemoveQuota,
            // ActionType.ChangeQuotaForDisk,

            // bookmarks
            // ActionType.AddBookmark,
            // ActionType.RemoveBookmark,
            // ActionType.UpdateBookmark,

            // Cluster
            // ActionType.AddCluster,
            // ActionType.UpdateCluster,
            // ActionType.RemoveCluster,
            // ActionType.AttachNetworkToClusterInternal,
            // ActionType.AttachNetworkToCluster,
            // ActionType.DetachNetworkToCluster,
            // ActionType.DetachNetworkFromClusterInternal,
            // ActionType.UpdateNetworkOnCluster,
            // ActionType.ManageNetworkClusters,
            // ActionType.StartClusterUpgrade,
            // ActionType.FinishClusterUpgrade,

            // MultiLevelAdministration
            // ActionType.AddPermission,
            // ActionType.RemovePermission,
            // ActionType.UpdateRole,
            // ActionType.RemoveRole,
            // ActionType.AttachActionGroupsToRole,
            // ActionType.DetachActionGroupsFromRole,
            // ActionType.AddRoleWithActionGroups,
            // ActionType.AddSystemPermission,
            // ActionType.RemoveSystemPermission,

            // Storage handling
            // ActionType.AddLocalStorageDomain,
            // ActionType.AddNFSStorageDomain,
            // ActionType.UpdateStorageDomain,
            // ActionType.RemoveStorageDomain,
            // ActionType.ForceRemoveStorageDomain,
            // ActionType.AttachStorageDomainToPool,
            // ActionType.DetachStorageDomainFromPool,
            // ActionType.ActivateStorageDomain,
            // ActionType.ConnectDomainToStorage,
            // ActionType.DeactivateStorageDomain,
            // ActionType.AddSANStorageDomain,
            // ActionType.ExtendSANStorageDomain,
            // ActionType.ReconstructMasterDomain,
            // ActionType.ProcessOvfUpdateForStorageDomain,
            // ActionType.CreateOvfVolumeForStorageDomain,
            // ActionType.RecoveryStoragePool,
            // ActionType.RefreshLunsSize,
            // ActionType.MoveStorageDomainDevice,
            // ActionType.ReduceStorageDomain,
            // ActionType.RemoveDeviceFromSANStorageDomain,
            // ActionType.ReduceSANStorageDomainDevices,
            // ActionType.AddEmptyStoragePool,
            // ActionType.AddStoragePoolWithStorages,
            // ActionType.RemoveStoragePool,
            // ActionType.UpdateStoragePool,
            // ActionType.FenceVdsManualy,
            // ActionType.AddExistingFileStorageDomain,
            // ActionType.AddExistingBlockStorageDomain,
            // ActionType.AddStorageServerConnection,
            // ActionType.UpdateStorageServerConnection,
            // ActionType.DisconnectStorageServerConnection,
            // ActionType.RemoveStorageServerConnection,
            // ActionType.ConnectHostToStoragePoolServers,
            // ActionType.DisconnectHostFromStoragePoolServers,
            // ActionType.ConnectStorageToVds,
            // ActionType.SetStoragePoolStatus,
            // ActionType.ConnectAllHostsToLun,
            // ActionType.AddPosixFsStorageDomain,
            // ActionType.LiveMigrateDisk,
            // ActionType.MoveDisk,
            // ActionType.ExtendImageSize,
            // ActionType.ImportRepoImage,
            // ActionType.ExportRepoImage,
            // ActionType.AttachStorageConnectionToStorageDomain,
            // ActionType.DetachStorageConnectionFromStorageDomain,
            // ActionType.SyncLunsInfoForBlockStorageDomain,
            // ActionType.UpdateStorageServerConnectionExtension,
            // ActionType.RemoveStorageServerConnectionExtension,
            // ActionType.AddStorageServerConnectionExtension,
            // ActionType.RefreshVolume,
            // ActionType.TransferDiskImage,
            // ActionType.TransferImageStatus,
            // ActionType.ScanStorageForUnregisteredDisks,
            // ActionType.CreateImagePlaceholder,
            // ActionType.SyncImageGroupData,
            // ActionType.CreateVolumeContainer,
            // ActionType.DownloadImage,
            // ActionType.CloneImageGroupVolumesStructure,
            // ActionType.CopyData,
            // ActionType.CopyImageGroupVolumesData,
            // ActionType.CopyImageGroupWithData,
            // ActionType.GlusterStorageSync,
            // ActionType.GlusterStorageGeoRepSyncInternal,
            // ActionType.ScheduleGlusterStorageSync,
            // ActionType.FenceVolumeJob,
            // ActionType.ReduceImage,

            // Leases
            // ActionType.AddVmLease,
            // ActionType.RemoveVmLease,
            // ActionType.GetVmLeaseInfo,

            // Sync luns
            // ActionType.SyncAllStorageDomainsLuns,
            // ActionType.SyncDirectLuns,
            // ActionType.SyncAllUsedLuns,
            // ActionType.SyncStorageDomainsLuns,

            // Event Notification,
            ActionType.AddEventSubscription,
            ActionType.RemoveEventSubscription,

            // Config
            // ActionType.ReloadConfigurations,

            // Gluster
            // ActionType.CreateGlusterVolume
            // ActionType.SetGlusterVolumeOption
            // ActionType.StartGlusterVolume
            // ActionType.StopGlusterVolume
            // ActionType.ResetGlusterVolumeOptions
            // ActionType.DeleteGlusterVolume
            // ActionType.GlusterVolumeRemoveBricks
            // ActionType.StartRebalanceGlusterVolume
            // ActionType.ReplaceGlusterVolumeBrick
            // ActionType.AddBricksToGlusterVolume
            // ActionType.StartGlusterVolumeProfile
            // ActionType.StopGlusterVolumeProfile
            // ActionType.RemoveGlusterServer
            // ActionType.AddGlusterFsStorageDomain
            // ActionType.EnableGlusterHook
            // ActionType.DisableGlusterHook
            // ActionType.UpdateGlusterHook
            // ActionType.AddGlusterHook
            // ActionType.RemoveGlusterHook
            // ActionType.RefreshGlusterHooks
            // ActionType.ManageGlusterService
            // ActionType.StopRebalanceGlusterVolume
            // ActionType.StartRemoveGlusterVolumeBricks
            // ActionType.StopRemoveGlusterVolumeBricks
            // ActionType.CommitRemoveGlusterVolumeBricks
            // ActionType.RefreshGlusterVolumeDetails
            // ActionType.RefreshGeoRepSessions
            // ActionType.StopGeoRepSession
            // ActionType.DeleteGeoRepSession
            // ActionType.StartGlusterVolumeGeoRep
            // ActionType.ResumeGeoRepSession
            // ActionType.PauseGlusterVolumeGeoRepSession
            // ActionType.SetGeoRepConfig
            // ActionType.ResetDefaultGeoRepConfig
            // ActionType.DeleteGlusterVolumeSnapshot
            // ActionType.DeleteAllGlusterVolumeSnapshots
            // ActionType.ActivateGlusterVolumeSnapshot
            // ActionType.DeactivateGlusterVolumeSnapshot
            // ActionType.RestoreGlusterVolumeSnapshot
            // ActionType.UpdateGlusterVolumeSnapshotConfig
            // ActionType.SyncStorageDevices
            // ActionType.CreateGlusterVolumeSnapshot
            // ActionType.ScheduleGlusterVolumeSnapshot
            // ActionType.RescheduleGlusterVolumeSnapshot
            // ActionType.CreateBrick
            // ActionType.CreateGlusterVolumeGeoRepSession
            // ActionType.SetupGlusterGeoRepMountBrokerInternal
            // ActionType.UpdateGlusterHostPubKeyToSlaveInternal
            // ActionType.DisableGlusterCliSnapshotScheduleInternal
            // ActionType.SetUpPasswordLessSSHInternal
            // ActionType.ResetGlusterVolumeBrick
            // ActionType.AddGlusterWebhookInternal

            // Scheduling Policy
            // ActionType.AddClusterPolicy,
            // ActionType.EditClusterPolicy,
            // ActionType.RemoveClusterPolicy,
            // ActionType.RemoveExternalPolicyUnit,

            // External events,
            ActionType.AddExternalEvent,

            // Providers,
            ActionType.AddProvider,
            ActionType.UpdateProvider,
            ActionType.RemoveProvider,
            ActionType.TestProviderConnectivity
            // ActionType.ImportProviderCertificate,
            // ActionType.AddNetworkOnProvider,
            // ActionType.AddNetworkWithSubnetOnProvider,
            // ActionType.AddSubnetToProvider,
            // ActionType.RemoveSubnetFromProvider,
            // ActionType.SyncNetworkProvider,
            // ActionType.AutodefineExternalNetwork,
            // ActionType.AddWatchdog,
            // ActionType.UpdateWatchdog,
            // ActionType.RemoveWatchdog,
            // ActionType.AddNetworkQoS,
            // ActionType.UpdateNetworkQoS,
            // ActionType.RemoveNetworkQoS,

            // qos,
            // ActionType.AddStorageQos,
            // ActionType.UpdateStorageQos,
            // ActionType.RemoveStorageQos,
            // ActionType.AddCpuQos,
            // ActionType.UpdateCpuQos,
            // ActionType.RemoveCpuQos,
            // ActionType.AddHostNetworkQos,
            // ActionType.UpdateHostNetworkQos,
            // ActionType.RemoveHostNetworkQos,

            // disk profiles,
            // ActionType.AddDiskProfile,
            // ActionType.UpdateDiskProfile,
            // ActionType.RemoveDiskProfile,

            // cpu profiles,
            // ActionType.AddCpuProfile,
            // ActionType.UpdateCpuProfile,
            // ActionType.RemoveCpuProfile,

            // External Tasks,
            // ActionType.AddExternalJob,
            // ActionType.EndExternalJob,
            // ActionType.ClearExternalJob,
            // ActionType.AddExternalStep,
            // ActionType.EndExternalStep,

            //Internal Tasks,
            // ActionType.AddInternalJob,
            // ActionType.AddInternalStep,
            // ActionType.UpdateMomPolicy,
            // ActionType.UploadStream,
            // ActionType.RetrieveImageData,
            // ActionType.ProcessOvfUpdateForStoragePool,
            // ActionType.UpdateOvfStoreForStorageDomain,

            // Affinity Groups
            // ActionType.AddAffinityGroup,
            // ActionType.EditAffinityGroup,
            // ActionType.RemoveAffinityGroup,

            // ISCSI Bonds
            // ActionType.AddIscsiBond,
            // ActionType.EditIscsiBond,
            // ActionType.RemoveIscsiBond,
            // ActionType.SetHaMaintenance,

            // Rng crud
            // ActionType.AddRngDevice,
            // ActionType.UpdateRngDevice,
            // ActionType.RemoveRngDevice,

            // Graphics Device CRUD
            // ActionType.AddGraphicsDevice,
            // ActionType.UpdateGraphicsDevice,
            // ActionType.RemoveGraphicsDevice,
            // ActionType.AddGraphicsAndVideoDevices,
            // ActionType.RemoveGraphicsAndVideoDevices,

            // Vm Host Device CRUD
            // ActionType.AddVmHostDevices,
            // ActionType.RemoveVmHostDevices,

            // Vm devices
            // ActionType.HotUnplugMemory,
            // ActionType.HotUnplugMemoryWithoutVmUpdate,

            // Audit Log
            // ActionType.RemoveAuditLogById,
            // ActionType.ClearAllAuditLogEvents,
            // ActionType.DisplayAllAuditLogEvents,
            // ActionType.ClearAllAuditLogAlerts,
            // ActionType.DisplayAllAuditLogAlerts,

            // ActionType.SetSesssionSoftLimit,

            // Mac Pool
            // ActionType.AddMacPool,
            // ActionType.UpdateMacPool,
            // ActionType.RemoveMacPool,

            // Cinder
            // ActionType.AddCinderDisk,
            // ActionType.RemoveCinderDisk,
            // ActionType.ExtendCinderDisk,
            // ActionType.RemoveAllVmCinderDisks,
            // ActionType.CloneSingleCinderDisk,
            // ActionType.RegisterCinderDisk,
            // ActionType.CreateCinderSnapshot,
            // ActionType.RemoveCinderSnapshotDisk,
            //
            // ActionType.AddLibvirtSecret,
            // ActionType.UpdateLibvirtSecret,
            // ActionType.RemoveLibvirtSecret,
            // ActionType.TryBackToCinderSnapshot,
            // ActionType.RestoreFromCinderSnapshot,
            // ActionType.RestoreAllCinderSnapshots,
            // ActionType.RemoveAllCinderSnapshotDisks,
            // ActionType.FreezeVm,
            // ActionType.ThawVm,
            // ActionType.RemoveCinderDiskVolume,

            // Managed Block Storage
            // ActionType.AddManagedBlockStorageDomain,
            // ActionType.AddManagedBlockStorageDisk,
            // ActionType.RemoveManagedBlockStorageDisk,
            // ActionType.ConnectManagedBlockStorageDevice,
            // ActionType.SaveManagedBlockStorageDiskDevice,
            // ActionType.DisconnectManagedBlockStorageDevice,
            // ActionType.GetConnectionInfoForManagedBlockStorageDisk,
            // ActionType.ExtendManagedBlockStorageDiskSize,
            // ActionType.GetManagedBlockStorageStats,
            // ActionType.CloneSingleManagedBlockDisk,
            // ActionType.RemoveAllManagedBlockStorageDisks,
            // ActionType.CreateManagedBlockStorageDiskSnapshot,
            // ActionType.RemoveManagedBlockStorageSnapshot,
            // ActionType.TryBackToManagedBlockSnapshot,
            // ActionType.RestoreAllManagedBlockSnapshots,

            // Incremental Backup
            // ActionType.StartVmBackup,
            // ActionType.StopVmBackup,

            // Host Devices
            // ActionType.RefreshHostDevices,
            // ActionType.RefreshHost,

            // Network Attachments
            // ActionType.HostSetupNetworks,
            // ActionType.AddNetworkAttachment,
            // ActionType.UpdateNetworkAttachment,
            // ActionType.RemoveNetworkAttachment,
            // ActionType.PersistentHostSetupNetworks,
            // ActionType.SyncAllHostNetworks,
            // ActionType.SyncAllClusterNetworks,
            // ActionType.CopyHostNetworks,

            // Hosted Engine
            // ActionType.ImportHostedEngineStorageDomain,

            // ActionType.AddLabel,

            // ActionType.RemoveLabel,

            // ActionType.UpdateLabel,

            // Scheduling and balancing
            // ActionType.BalanceVm,

            // CoCo
            // ActionType.RunAsyncAction,

            // API:
            // ActionType.AddDeprecatedApiEvent,

            // ActionType.ExportVmToOva,
            // ActionType.ExportVmToOva
    );

    public static boolean isActionSupported(Managed entity, ActionType actionType) {
        if (entity.isManaged()) {
            return true;
        }

        return SUPPORTED_ACTIONS.contains(actionType);
    }
}
