package org.ovirt.engine.core.common.queries;

import java.io.Serializable;

public enum VdcQueryType implements Serializable {
    // VM queries
    IsVmWithSameNameExist(VdcQueryAuthType.User),
    GetVmByVmId(VdcQueryAuthType.User),
    GetVmByVmNameForDataCenter(VdcQueryAuthType.User),
    GetAllVms(VdcQueryAuthType.User),
    GetAllVmsForUser(VdcQueryAuthType.User),
    GetAllVmsForUserAndActionGroup(VdcQueryAuthType.User),
    GetAllPoolVms,
    GetUnregisteredVms,
    GetUnregisteredVmTemplates,
    GetUnregisteredDisksFromDB,
    GetVmsRunningOnOrMigratingToVds,
    GetVmsByStorageDomain,
    GetVmsByInstanceTypeId,
    GetVmCustomProperties(VdcQueryAuthType.User),
    GetVmConfigurationBySnapshot(VdcQueryAuthType.User),
    GetVmFromConfiguration(VdcQueryAuthType.User),
    GetVmOvfByVmId(VdcQueryAuthType.User),
    GetSnapshotBySnapshotId(VdcQueryAuthType.User),
    GetVmsByDiskGuid,
    GetVmPayload(VdcQueryAuthType.User),
    IsBalloonEnabled(VdcQueryAuthType.User),
    GetSoundDevices(VdcQueryAuthType.User),
    GetVmsByVnicProfileId,
    GetTemplatesByVnicProfileId,
    GetVirtioScsiControllers(VdcQueryAuthType.User),
    GetVmsInit(VdcQueryAuthType.User),
    GetVmNextRunConfiguration(VdcQueryAuthType.User),
    GetVmChangedFieldsForNextRun(VdcQueryAuthType.User),
    GetVmsFromExternalProvider,
    GetVmFromOva,
    GetVmIcon(VdcQueryAuthType.User),
    GetVmIcons(VdcQueryAuthType.User),
    GetAllVmIcons(VdcQueryAuthType.User),
    GetVmIconDefaults(VdcQueryAuthType.User),
    GetVmIconDefault(VdcQueryAuthType.User),
    GetVmDevicesForVm(VdcQueryAuthType.User),

    // Vds queries
    GetVdsByVdsId,
    GetVdsByName,
    GetVdsFenceStatus,
    GetFenceAgentStatus,
    GetAgentFenceOptions,
    GetAllChildVlanInterfaces,
    GetAllSiblingVlanInterfaces,
    GetVlanParent,
    GetVdsHooksById,
    GetAllHosts(VdcQueryAuthType.User),
    GetHostsByClusterId(VdcQueryAuthType.User),
    IsDisplayAddressConsistentInCluster,
    GetAllVdsByStoragePool(VdcQueryAuthType.User),
    GetHostListFromExternalProvider(),
    GetHostGroupsFromExternalProvider(),
    GetComputeResourceFromExternalProvider(),
    GetDiscoveredHostListFromExternalProvider(),
    GetProviderCertificateChain,
    GetHostsForStorageOperation,
    GetEngineSSHPublicKey,
    GetServerSSHKeyFingerprint,
    GetCpuStatisticsByVdsId,
    GetFenceAgentById,
    GetFenceAgentsByVdsId,
    GetHostDevicesByHostId,
    GetHostDeviceByHostIdAndDeviceName,
    GetExtendedHostDevicesByHostId,
    GetExtendedVmHostDevicesByVmId,
    GetErrataForHost,
    GetErratumByIdForHost,
    GetErrataForEngine,
    GetErratumByIdForEngine,
    GetErrataCountsForHost,
    GetErrataCountsForVm,
    GetErrataForVm,
    GetErratumByIdForVm,
    GetAllHostNamesPinnedToVmById,

    // VdsStatic Queries
    GetVdsStaticByName,

    // Vds Networks
    GetVdsInterfacesByVdsId(VdcQueryAuthType.User),
    GetHostBondsByHostId,
    GetVdsFreeBondsByVdsId,
    GetAllNetworks(VdcQueryAuthType.User),
    GetAllNetworksByClusterId(VdcQueryAuthType.User),
    GetAllUnmanagedNetworksByHostId,
    GetUnmanagedNetworkByHostIdAndName,
    GetNetworksByDataCenterId(VdcQueryAuthType.User),
    GetAllNetworksByQosId,
    GetManagementInterfaceAddressByVmId(VdcQueryAuthType.User),
    GetInterfacesByLabelForNetwork,
    GetAllVfsConfigByHostId,
    GetVfToPfMapByHostId,

    // Vm Network
    GetVmInterfacesByVmId(VdcQueryAuthType.User),
    GetVmGuestAgentInterfacesByVmId(VdcQueryAuthType.User),

    // Vnic Profiles
    GetAllVnicProfiles(VdcQueryAuthType.User),
    GetVnicProfileById(VdcQueryAuthType.User),
    GetVnicProfilesByNetworkId(VdcQueryAuthType.User),
    GetVnicProfilesByDataCenterId(VdcQueryAuthType.User),
    GetVnicProfilesByNetworkQosId,

    // Template Network
    GetTemplateInterfacesByTemplateId(VdcQueryAuthType.User),

    // Networks
    GetClustersAndNetworksByNetworkId,
    GetVdsAndNetworkInterfacesByNetworkId,
    GetVdsWithoutNetwork,
    GetVmsAndNetworkInterfacesByNetworkId,
    GetVmTemplatesAndNetworkInterfacesByNetworkId,
    GetNetworkById(VdcQueryAuthType.User),
    GetNetworkByNameAndDataCenter,
    IsManagementNetwork,
    GetManagementNetwork,
    GetDefaultManagementNetwork,
    GetManagementNetworkCandidates,

    // External network providers
    GetAllExternalNetworksOnProvider,
    GetExternalSubnetsOnProviderByNetwork,
    GetExternalSubnetsOnProviderByExternalNetwork,

    // Network labels
    GetNetworkLabelsByNetworkId,
    GetNetworkLabelsByDataCenterId,
    GetNetworkLabelsByHostNicId,

    // Network Attachments
    GetNetworkAttachmentById,
    GetNetworkAttachmentsByHostId,
    GetNetworkAttachmentsByHostNicId,

    // NUMA
    GetVdsNumaNodesByVdsId(VdcQueryAuthType.User),
    GetVmNumaNodesByVmId(VdcQueryAuthType.User),
    GetAllVmsWithNumaByClusterId(VdcQueryAuthType.User),

    // Cluster
    GetVdsCertificateSubjectByVmId(VdcQueryAuthType.User),
    GetAllClusters(VdcQueryAuthType.User),
    GetClusterByClusterId(VdcQueryAuthType.User), // needed when updating VM
    GetClusterById(VdcQueryAuthType.User),
    GetClusterByName(VdcQueryAuthType.User),
    GetClustersByStoragePoolId(VdcQueryAuthType.User),
    GetNumberOfActiveVmsInClusterByClusterId,
    GetNumberOfVmsInClusterByClusterId,
    GetClusterFeaturesByVersionAndCategory,
    GetClusterFeaturesByClusterId,
    GetClusterEditWarnings,
    GetAllNetworkFilters,
    GetAllSupportedNetworkFiltersByVersion,
    GetNetworkFilterById,

    // Certificate
    GetCACertificate(VdcQueryAuthType.User),
    SignString(VdcQueryAuthType.User),

    GetSignedWebsocketProxyTicket(VdcQueryAuthType.User),

    // VM Template based entities queries
    IsVmTemlateWithSameNameExist(VdcQueryAuthType.User),
    GetVmTemplate(VdcQueryAuthType.User),
    GetInstanceType(VdcQueryAuthType.User),
    GetAllVmTemplates(VdcQueryAuthType.User),
    GetAllInstanceTypes(VdcQueryAuthType.User),
    GetAllImageTypes(VdcQueryAuthType.User),
    GetVmTemplatesDisks(VdcQueryAuthType.User),
    GetVmTemplatesByStoragePoolId(VdcQueryAuthType.User),
    GetVmTemplatesByImageGuid,
    GetSystemPermissions,
    GetVmTemplatesByBaseTemplateId,
    GetLatestTemplateInChain,

    // VM Snapshot queries
    GetAllVmSnapshotsByVmId(VdcQueryAuthType.User),
    GetAllVmSnapshotsFromConfigurationByVmId(VdcQueryAuthType.User),

    // Images queries
    GetImageById(VdcQueryAuthType.User),
    GetImagesList(VdcQueryAuthType.User),
    GetImagesListByStoragePoolId(VdcQueryAuthType.User),
    GetAllDisksByVmId(VdcQueryAuthType.User),
    GetAllAttachableDisksForVm(VdcQueryAuthType.User),
    GetAllDisksByStorageDomainId,
    GetAllDisks(VdcQueryAuthType.User),
    GetAllDiskSnapshotsByStorageDomainId,
    GetUnregisteredDisks,
    GetUnregisteredDisk,
    GetDiskByDiskId(VdcQueryAuthType.User),
    GetDiskSnapshotByImageId,
    GetAncestorImagesByImagesIds(VdcQueryAuthType.User),

    GetDiskVmElementById(VdcQueryAuthType.Admin),
    GetDiskVmElementsByVmId(VdcQueryAuthType.Admin),

    // Users queries
    GetUserVmsByUserIdAndGroups(VdcQueryAuthType.User),
    GetAllDbUsers(VdcQueryAuthType.User),
    GetDbUserByUserId(VdcQueryAuthType.User),
    GetDbUserByUserNameAndDomain(VdcQueryAuthType.User),
    GetUserBySessionId(VdcQueryAuthType.User),
    GetEngineSessionIdToken(VdcQueryAuthType.User),
    GetEngineSessionIdForSsoToken(VdcQueryAuthType.User),
    GetUserProfile(VdcQueryAuthType.User),
    GetUserProfileAsList(VdcQueryAuthType.User),
    GetAllUserProfiles,
    IsPasswordDelegationPossible(VdcQueryAuthType.User),

    // Directory queries:
    GetDirectoryUserById(VdcQueryAuthType.User),
    GetDirectoryGroupById(VdcQueryAuthType.User),
    GetDirectoryGroupsForUser(VdcQueryAuthType.User),
    GetAvailableNamespaces(VdcQueryAuthType.User),
    GetDirectoryUserByPrincipal(VdcQueryAuthType.User),

    // Groups queries:
    GetAllDbGroups(VdcQueryAuthType.User),
    GetDbGroupById,
    GetAuthzGroupsByUserId,

    // VM pools queries
    GetVmPoolById(VdcQueryAuthType.User),
    GetAllVmPoolsAttachedToUser(VdcQueryAuthType.User),
    GetAllVmsAndVmPools(VdcQueryAuthType.User),
    IsVmPoolWithSameNameExists,
    GetVmDataByPoolId(VdcQueryAuthType.User),
    GetVmDataByPoolName(VdcQueryAuthType.User),

    // Tags queries
    GetAllTags,
    GetRootTag,
    GetTagByTagId,
    GetTagByTagName,
    GetTagsByUserGroupId,
    GetTagsByUserId,
    GetTagsByVmId,
    GetTagsByTemplateId,
    GetTagsByVdsId,

    // System
    GetSystemStatistics,

    // Bookmarks
    GetAllBookmarks,
    GetBookmarkByBookmarkId,
    GetBookmarkByBookmarkName,

    // Configuration values
    GetConfigurationValue(VdcQueryAuthType.User),
    GetConfigurationValues(VdcQueryAuthType.User),
    GetFenceConfigurationValue(VdcQueryAuthType.User),
    GetDefaultTimeZone(VdcQueryAuthType.User),
    GetAvailableStoragePoolVersions(VdcQueryAuthType.User),
    GetAvailableClusterVersionsByStoragePool(VdcQueryAuthType.User),

    // AuditLog
    GetAllEventMessages(VdcQueryAuthType.User),
    GetAllAuditLogsByVMId(VdcQueryAuthType.User),
    GetAllAuditLogsByVMTemplateId(VdcQueryAuthType.User),
    GetAuditLogById,

    // Search queries
    Search(VdcQueryAuthType.User),

    // Public services
    GetDomainList(VdcQueryAuthType.User),
    GetAAAProfileList(VdcQueryAuthType.User),
    RegisterVds(VdcQueryAuthType.User),
    CheckDBConnection(VdcQueryAuthType.User),
    ValidateSession(VdcQueryAuthType.User),
    GetDbUserBySession,

    // Auxiliary queries used by architecture compatibility
    IsClusterEmpty(VdcQueryAuthType.User),
    GetHostArchitecture(VdcQueryAuthType.User),

    // License queries
    GetAllServerCpuList(VdcQueryAuthType.User),
    GetSupportedCpuList(VdcQueryAuthType.User),

    // Multi Level Administration queries
    GetAllRoles(VdcQueryAuthType.User),
    GetRoleById(VdcQueryAuthType.User),
    GetRoleByName,
    GetPermissionById(VdcQueryAuthType.User),
    GetPermissionByRoleId,
    HasAdElementReconnectPermission(VdcQueryAuthType.User),
    GetPermissionsByAdElementId(VdcQueryAuthType.User),
    GetRoleActionGroupsByRoleId(VdcQueryAuthType.User),
    GetPermissionsForObject(VdcQueryAuthType.User),
    GetAllStoragePools(VdcQueryAuthType.User),
    GetDataCentersWithPermittedActionOnClusters(VdcQueryAuthType.User),
    GetClustersWithPermittedAction(VdcQueryAuthType.User),
    GetVmTemplatesWithPermittedAction(VdcQueryAuthType.User),
    GetAllClustersHavingHosts,

    // Storage
    GetStorageDomainById(VdcQueryAuthType.User),
    GetStorageDomainByName(VdcQueryAuthType.User),
    GetStorageServerConnectionById,
    GetAllStorageServerConnections,
    GetStorageServerConnectionsForDomain,
    GetStoragePoolById(VdcQueryAuthType.User),
    GetStorageServerConnectionExtensionsByHostId,
    GetStorageServerConnectionExtensionById,
    GetMacPoolById,
    GetAllMacPools,
    GetStoragePoolByDatacenterName(VdcQueryAuthType.User),
    GetStorageDomainsByConnection,
    GetConnectionsByDataCenterAndStorageType,
    GetStorageDomainsByStoragePoolId(VdcQueryAuthType.User),
    GetStorageDomainsByImageId,
    GetUnregisteredBlockStorageDomains,
    GetDeviceList,
    DiscoverSendTargets,
    GetStorageDomainsByVmTemplateId(VdcQueryAuthType.User),
    GetVmsFromExportDomain,
    GetTemplatesFromExportDomain,
    GetVmTemplatesFromStorageDomain(VdcQueryAuthType.User),
    GetAllStorageDomains(VdcQueryAuthType.User),
    GetExistingStorageDomainList,
    GetStorageDomainByIdAndStoragePoolId,
    GetStoragePoolsByStorageDomainId,
    GetStoragePoolsByClusterService(VdcQueryAuthType.User),
    GetStorageDomainListById,
    GetLunsByVgId,
    GetPermittedStorageDomainsByStoragePoolId(VdcQueryAuthType.User),
    GetIscsiBondsByStoragePoolId,
    GetIscsiBondById,
    GetStorageServerConnectionByIscsiBondId,
    GetNetworksByIscsiBondId,
    GetStorageDomainsWithAttachedStoragePoolGuid,
    GetFileStorageDomainsWithAttachedStoragePoolGuid,
    GetBlockStorageDomainsWithAttachedStoragePoolGuid,
    GetStorageDomainDefaultWipeAfterDelete,

    // Cinder
    GetCinderVolumeTypesByStorageDomainId(VdcQueryAuthType.User),
    GetUnregisteredCinderDisksByStorageDomainId,
    GetUnregisteredCinderDiskByIdAndStorageDomainId,
    GetAllLibvirtSecretsByProviderId,
    GetLibvirtSecretById,

    // Event Notification
    GetEventSubscribersBySubscriberIdGrouped,

    // oVirt
    GetoVirtISOs,

    // Async Tasks
    GetTasksStatusesByTasksIDs(VdcQueryAuthType.User),

    // Quota
    GetQuotaByStoragePoolId,
    GetQuotaByQuotaId(VdcQueryAuthType.User),
    GetQuotaClusterByQuotaId,
    GetQuotaStorageByQuotaId,
    GetVmsRelatedToQuotaId,
    GetTemplatesRelatedToQuotaId,
    GetPermissionsToConsumeQuotaByQuotaId,
    GetQuotasByAdElementId,
    GetQuotasConsumptionForCurrentUser(VdcQueryAuthType.User),
    GetAllRelevantQuotasForStorage(VdcQueryAuthType.User),
    GetAllRelevantQuotasForCluster(VdcQueryAuthType.User),

    // Jobs
    GetJobByJobId,
    GetJobsByCorrelationId,
    GetJobsByOffset,
    GetAllJobs,
    GetAllSteps,
    GetStepByStepId,
    GetStepsByJobId,

    // Disks
    GetNextAvailableDiskAliasNameByVMId(VdcQueryAuthType.User),

    // Gluster
    GetGlusterVolumeById,
    GetGlusterVolumeOptionsInfo,
    GetGlusterVolumeBricks,
    GetGlusterVolumeBricksByServerId,
    GetGlusterBrickById,
    GetGlusterServersForImport,
    GetAddedGlusterServers,
    GetGlusterVolumeAdvancedDetails,
    GetGlusterVolumeProfileInfo,
    GetGlusterHooks,
    GetGlusterHookContent,
    GetGlusterHookById,
    GetGlusterServerServicesByClusterId,
    GetGlusterServerServicesByServerId,
    GetGlusterClusterServiceByClusterId,
    GetGlusterVolumeRebalanceStatus,
    GetGlusterVolumeGeoRepSessions,
    GetGlusterHostPublicKeys,
    GetGlusterVolumeRemoveBricksStatus,
    GetGlusterVolumeByTaskId,
    GetNonEligibilityReasonsOfVolumeForGeoRepSession,
    GetGlusterGeoReplicationEligibleVolumes,
    GetGlusterVolumeSnapshotsByVolumeId,
    GetGlusterVolumeGeoRepConfigList,
    GetGlusterVolumeSnapshotConfig,
    GetGlusterStorageDevices,
    GetGlusterVolumeSnapshotScheduleByVolumeId,
    GetUnusedGlusterBricks,
    GetGlusterTunedProfiles,
    GetGlusterVolumeSnapshotCliScheduleFlag,

    GetDefaultConfigurationVersion(VdcQueryAuthType.User),
    GetProductVersion(VdcQueryAuthType.User),
    OsRepository(VdcQueryAuthType.User),
    GetArchitectureCapabilities(VdcQueryAuthType.User),

    // Providers
    GetAllProviders,
    GetProviderById,
    GetAllNetworksForProvider,
    GetEngineForemanProvider,

    //Network QoS
    GetAllNetworkQosByStoragePoolId,

    // QoS
    GetQosById,
    GetAllQosByStoragePoolId,
    GetAllQosByStoragePoolIdAndType,
    GetAllQosByType,

    GetWatchdog(VdcQueryAuthType.User),
    GetConsoleDevices(VdcQueryAuthType.User),
    GetRngDevice(VdcQueryAuthType.User),
    GetGraphicsDevices(VdcQueryAuthType.User),
    GetNextRunGraphicsDevices(VdcQueryAuthType.User),
    GetGraphicsDevicesMultiple(VdcQueryAuthType.User),

    GetVmHostDevices,
    GetVmHostDeviceByVmIdAndDeviceName,

    ConfigureConsoleOptions(VdcQueryAuthType.User),
    GetConsoleDescriptorFile(VdcQueryAuthType.User),

    GetDeviceCustomProperties(VdcQueryAuthType.User),

    // Scheduling
    GetClusterPolicies,
    GetClusterPolicyById,
    GetAllPolicyUnits,
    GetPolicyUnitById,
    GetAttachedClustersByClusterPolicyId,
    GetAffinityGroupById,
    GetAffinityGroupsByClusterId,
    GetAffinityGroupsByVmId,

    GetAllDisksPartialDataByVmId(VdcQueryAuthType.User),
    GetVmTemplateCount,

    //Disk Profiles
    GetDiskProfileById,
    GetAllDiskProfiles,
    GetDiskProfilesByStorageDomainId(VdcQueryAuthType.User),
    GetDiskProfilesByStorageQosId,

    // Cpu Profiles
    GetCpuProfileById,
    GetAllCpuProfiles,
    GetCpuProfilesByClusterId(VdcQueryAuthType.User),
    GetCpuProfilesByCpuQosId,

    IsUserApplicationContainerManager,

    // migration profiles
    GetAllMigrationPolicies(VdcQueryAuthType.User),

    // Labels
    GetAllLabels,
    GetLabelById,
    GetLabelsByIds,
    GetLabelByEntityId,

    // Default type instead of having to null check
    Unknown(VdcQueryAuthType.User);

    /**
     * What kind of authorization the query requires. Although this is essentially a <code>boolean</code>, it's
     * implemented as an enum for future extendability.
     */
    public static enum VdcQueryAuthType {
        Admin,
        User
    }

    private VdcQueryAuthType authType;

    private VdcQueryType() {
        authType = VdcQueryAuthType.Admin;
    }

    private VdcQueryType(VdcQueryAuthType authType) {
        this.authType = authType;
    }

    public int getValue() {
        return this.ordinal();
    }

    public static VdcQueryType forValue(int value) {
        return values()[value];
    }

    public VdcQueryAuthType getAuthType() {
        return authType;
    }

    public boolean isAdmin() {
        return authType == VdcQueryAuthType.Admin;
    }
}
