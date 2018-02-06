package org.ovirt.engine.core.common.queries;

import java.io.Serializable;

public enum QueryType implements Serializable {
    // VM queries
    IsVmWithSameNameExist(QueryAuthType.User),
    GetVmByVmId(QueryAuthType.User),
    GetVmByVmNameForDataCenter(QueryAuthType.User),
    GetAllVms(QueryAuthType.User),
    GetAllVmsForUser(QueryAuthType.User),
    GetAllVmsForUserAndActionGroup(QueryAuthType.User),
    GetAllVmsFilteredAndSorted(QueryAuthType.User),
    GetAllVmPoolsFilteredAndSorted(QueryAuthType.User),
    GetAllPoolVms,
    GetUnregisteredVms,
    GetUnregisteredVmTemplates,
    GetUnregisteredDisksFromDB,
    GetVmsRunningOnOrMigratingToVds,
    GetVmsByStorageDomain,
    GetVmsByInstanceTypeId,
    GetVmCustomProperties(QueryAuthType.User),
    GetVmConfigurationBySnapshot(QueryAuthType.User),
    GetVmFromConfiguration(QueryAuthType.User),
    GetVmTemplateFromConfiguration(QueryAuthType.User),
    GetVmOvfByVmId(QueryAuthType.User),
    GetSnapshotBySnapshotId(QueryAuthType.User),
    GetVmsByDiskGuid,
    GetVmPayload(QueryAuthType.User),
    IsBalloonEnabled(QueryAuthType.User),
    GetSoundDevices(QueryAuthType.User),
    GetVmsByVnicProfileId,
    GetTemplatesByVnicProfileId,
    GetVirtioScsiControllers(QueryAuthType.User),
    GetVmsInit(QueryAuthType.User),
    GetVmNextRunConfiguration(QueryAuthType.User),
    GetVmChangedFieldsForNextRun(QueryAuthType.User),
    GetVmsFromExternalProvider,
    GetVmFromOva,
    GetVmIcon(QueryAuthType.User),
    GetVmIcons(QueryAuthType.User),
    GetAllVmIcons(QueryAuthType.User),
    GetVmIconDefaults(QueryAuthType.User),
    GetVmIconDefault(QueryAuthType.User),
    GetVmDevicesForVm(QueryAuthType.User),
    GetVmsPinnedToHost(QueryAuthType.User),

    // Vds queries
    GetVdsByVdsId,
    GetVdsByName,
    GetVdsFenceStatus,
    GetFenceAgentStatus,
    GetAgentFenceOptions,
    GetAllChildVlanInterfaces,
    GetVdsHooksById,
    GetAllHosts(QueryAuthType.User),
    GetHostsByClusterId(QueryAuthType.User),
    IsDisplayAddressConsistentInCluster(QueryAuthType.User),
    GetAllVdsByStoragePool(QueryAuthType.User),
    GetHostListFromExternalProvider(),
    GetHostGroupsFromExternalProvider(),
    GetComputeResourceFromExternalProvider(),
    GetDiscoveredHostListFromExternalProvider(),
    GetProviderCertificateChain,
    GetHostsForStorageOperation,
    GetEngineSSHPublicKey,
    GetServerSSHKeyFingerprint,
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
    GetValidHostsForVms,

    // VdsStatic Queries
    GetVdsStaticByName,

    // Vds Networks
    GetVdsInterfacesByVdsId(QueryAuthType.User),
    GetHostBondsByHostId,
    GetVdsFreeBondsByVdsId,
    GetAllNetworks(QueryAuthType.User),
    GetAllNetworksByClusterId(QueryAuthType.User),
    GetAllVmNetworksByClusterId(QueryAuthType.User),
    GetAllUnmanagedNetworksByHostId,
    GetUnmanagedNetworkByHostIdAndName,
    GetNetworksByDataCenterId(QueryAuthType.User),
    GetAllNetworksByQosId,
    GetManagementInterfaceAddressByVmId(QueryAuthType.User),
    GetInterfacesByLabelForNetwork,
    GetAllVfsConfigByHostId,
    GetVfToPfMapByHostId,

    // Vm Network
    GetVmInterfacesByVmId(QueryAuthType.User),
    GetVmGuestAgentInterfacesByVmId(QueryAuthType.User),
    GetVmInterfaceFilterParametersByVmInterfaceId(QueryAuthType.User),
    GetVmInterfaceFilterParameterById(QueryAuthType.User),
    ValidateVmMacs,

    // Vnic Profiles
    GetAllVnicProfiles(QueryAuthType.User),
    GetVnicProfileById(QueryAuthType.User),
    GetVnicProfilesByNetworkId(QueryAuthType.User),
    GetVnicProfilesByDataCenterId(QueryAuthType.User),
    GetVnicProfilesByClusterId(QueryAuthType.User),
    GetVnicProfilesByNetworkQosId,

    // Template Network
    GetTemplateInterfacesByTemplateId(QueryAuthType.User),

    // Networks
    GetClustersAndNetworksByNetworkId,
    GetVdsAndNetworkInterfacesByNetworkId,
    GetVdsWithoutNetwork,
    GetVmsAndNetworkInterfacesByNetworkId,
    GetVmTemplatesAndNetworkInterfacesByNetworkId,
    GetNetworkById(QueryAuthType.User),
    GetNetworkByNameAndDataCenter,
    IsManagementNetwork,
    GetManagementNetwork,
    GetDefaultManagementNetwork,
    GetManagementNetworkCandidates,
    GetTlvsByHostNicId,
    GetMultipleTlvsByHostId,

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
    GetVdsNumaNodesByVdsId(QueryAuthType.User),
    GetVmNumaNodesByVmId(QueryAuthType.User),
    GetAllVmsWithNumaByClusterId(QueryAuthType.User),

    // Cluster
    GetVdsCertificateSubjectByVmId(QueryAuthType.User),
    GetAllClusters(QueryAuthType.User),
    GetClusterById(QueryAuthType.User),
    GetClusterByName(QueryAuthType.User),
    GetClustersByStoragePoolId(QueryAuthType.User),
    GetNumberOfActiveVmsInClusterByClusterId,
    GetNumberOfVmsInClusterByClusterId,
    GetClusterFeaturesByVersionAndCategory,
    GetClusterFeaturesByClusterId,
    GetClusterEditWarnings,
    GetAllNetworkFilters,
    GetAllSupportedNetworkFiltersByVersion,
    GetNetworkFilterById,

    // Certificate
    GetCACertificate(QueryAuthType.User),
    SignString(QueryAuthType.User),

    GetSignedWebsocketProxyTicket(QueryAuthType.User),

    // VM Template based entities queries
    IsVmTemlateWithSameNameExist(QueryAuthType.User),
    GetVmTemplate(QueryAuthType.User),
    GetInstanceType(QueryAuthType.User),
    GetAllVmTemplates(QueryAuthType.User),
    GetAllInstanceTypes(QueryAuthType.User),
    GetVmTemplatesDisks(QueryAuthType.User),
    GetVmTemplatesByStoragePoolId(QueryAuthType.User),
    GetVmTemplatesByImageGuid,
    GetSystemPermissions,
    GetVmTemplatesByBaseTemplateId,
    GetLatestTemplateInChain,

    // VM Snapshot queries
    GetAllVmSnapshotsByVmId(QueryAuthType.User),
    GetAllVmSnapshotsWithLeasesFromConfigurationByVmId(QueryAuthType.User),

    // Images queries
    GetImageById(QueryAuthType.User),
    GetImagesList(QueryAuthType.User),
    GetImagesListByStoragePoolId(QueryAuthType.User),
    GetAllDisksByVmId(QueryAuthType.User),
    GetAllAttachableDisksForVm(QueryAuthType.User),
    GetAllDisksByStorageDomainId,
    GetAllDisksWithSnapshots(QueryAuthType.User),
    GetAllDiskSnapshotsByStorageDomainId,
    GetUnregisteredDisks,
    GetUnregisteredDisk,
    GetDiskByDiskId(QueryAuthType.User),
    GetDiskAndSnapshotsByDiskId(QueryAuthType.User),
    GetDiskSnapshotByImageId,
    GetAncestorImagesByImagesIds(QueryAuthType.User),
    GetImageTransferById(QueryAuthType.User),
    GetAllImageTransfers(QueryAuthType.Admin),
    GetDiskImageByDiskAndImageIds(QueryAuthType.User),

    GetDiskVmElementById(QueryAuthType.User),
    GetDiskVmElementsByVmId(QueryAuthType.User),

    // Users queries
    GetUserVmsByUserIdAndGroups(QueryAuthType.User),
    GetAllDbUsers(QueryAuthType.User),
    GetAnyDbUserByUserId(QueryAuthType.User),
    GetDbUserByUserId(QueryAuthType.User),
    GetDbUserByUserNameAndDomain(QueryAuthType.User),
    GetUserBySessionId(QueryAuthType.User),
    GetEngineSessionIdToken(QueryAuthType.User),
    GetEngineSessionIdForSsoToken(QueryAuthType.User),
    GetUserProfile(QueryAuthType.User),
    GetUserProfileAsList(QueryAuthType.User),
    GetAllUserProfiles,
    IsPasswordDelegationPossible(QueryAuthType.User),
    GetDefaultAllowedOrigins,

    // Directory queries:
    GetDirectoryUserById(QueryAuthType.User),
    GetDirectoryGroupById(QueryAuthType.User),
    GetDirectoryGroupsForUser(QueryAuthType.User),
    GetAvailableNamespaces(QueryAuthType.User),
    GetDirectoryUserByPrincipal(QueryAuthType.User),

    // Groups queries:
    GetAllDbGroups(QueryAuthType.User),
    GetDbGroupById,
    GetAuthzGroupsByUserId(QueryAuthType.User),

    // VM pools queries
    GetVmPoolById(QueryAuthType.User),
    GetAllVmPoolsAttachedToUser(QueryAuthType.User),
    GetAllVmsAndVmPools(QueryAuthType.User),
    IsVmPoolWithSameNameExists,
    GetVmDataByPoolId(QueryAuthType.User),
    GetVmDataByPoolName(QueryAuthType.User),

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
    IsOvirtCockpitSSOStarted,

    // Bookmarks
    GetAllBookmarks,
    GetBookmarkByBookmarkId,
    GetBookmarkByBookmarkName,

    // Configuration values
    GetConfigurationValue(QueryAuthType.User),
    GetConfigurationValues(QueryAuthType.User),
    GetFenceConfigurationValue(QueryAuthType.User),
    GetAvailableStoragePoolVersions(QueryAuthType.User),
    GetAvailableClusterVersionsByStoragePool(QueryAuthType.User),

    // AuditLog
    GetAllEventMessages(QueryAuthType.User),
    GetAllAuditLogsByVMId(QueryAuthType.User),
    GetAllAuditLogsByVMTemplateId(QueryAuthType.User),
    GetAuditLogById,

    // Search queries
    Search(QueryAuthType.User),

    // Public services
    GetDomainList(QueryAuthType.User),
    GetAAAProfileList(QueryAuthType.User),
    RegisterVds(QueryAuthType.User),
    CheckDBConnection(QueryAuthType.User),
    ValidateSession(QueryAuthType.User),
    GetDbUserBySession,

    // Auxiliary queries used by architecture compatibility
    IsClusterEmpty(QueryAuthType.User),
    GetHostArchitecture(QueryAuthType.User),

    // License queries
    GetAllServerCpuList(QueryAuthType.User),
    GetSupportedCpuList(QueryAuthType.User),

    // Multi Level Administration queries
    GetAllRoles(QueryAuthType.User),
    GetRoleById(QueryAuthType.User),
    GetPermissionById(QueryAuthType.User),
    GetPermissionByRoleId,
    HasAdElementReconnectPermission(QueryAuthType.User),
    GetPermissionsByAdElementId(QueryAuthType.User),
    GetPermissionsOnBehalfByAdElementId(QueryAuthType.User),
    GetRoleActionGroupsByRoleId(QueryAuthType.User),
    GetPermissionsForObject(QueryAuthType.User),
    GetAllStoragePools(QueryAuthType.User),
    GetDataCentersWithPermittedActionOnClusters(QueryAuthType.User),
    GetClustersWithPermittedAction(QueryAuthType.User),
    GetVmTemplatesWithPermittedAction(QueryAuthType.User),
    GetAllClustersHavingHosts,

    // Storage
    GetStorageDomainById(QueryAuthType.User),
    GetStorageDomainByName(QueryAuthType.User),
    GetStorageServerConnectionById,
    GetAllStorageServerConnections,
    GetStorageServerConnectionsForDomain,
    GetStoragePoolById(QueryAuthType.User),
    GetStorageServerConnectionExtensionsByHostId,
    GetStorageServerConnectionExtensionById,
    GetMacPoolById,
    GetAllMacPools,
    GetStoragePoolByDatacenterName(QueryAuthType.User),
    GetStorageDomainsByConnection,
    GetConnectionsByDataCenterAndStorageType,
    GetStorageDomainsByStoragePoolId(QueryAuthType.User),
    GetStorageDomainsByImageId,
    GetUnregisteredBlockStorageDomains,
    GetDeviceList,
    DiscoverSendTargets,
    GetStorageDomainsByVmTemplateId(QueryAuthType.User),
    GetVmsFromExportDomain,
    GetTemplatesFromExportDomain,
    GetVmTemplatesFromStorageDomain(QueryAuthType.User),
    GetAllStorageDomains(QueryAuthType.User),
    GetExistingStorageDomainList,
    GetStorageDomainByIdAndStoragePoolId,
    GetStoragePoolsByStorageDomainId,
    GetStoragePoolsByClusterService(QueryAuthType.User),
    GetStorageDomainListById,
    GetLunsByVgId,
    GetPermittedStorageDomainsByStoragePoolId(QueryAuthType.User),
    GetIscsiBondsByStoragePoolId,
    GetIscsiBondById,
    GetStorageServerConnectionByIscsiBondId,
    GetNetworksByIscsiBondId,
    GetStorageDomainsWithAttachedStoragePoolGuid,
    GetFileStorageDomainsWithAttachedStoragePoolGuid,
    GetBlockStorageDomainsWithAttachedStoragePoolGuid,
    GetStorageDomainDefaultWipeAfterDelete,
    GetStorageDomainDR,

    // Cinder
    GetCinderVolumeTypesByStorageDomainId(QueryAuthType.User),
    GetUnregisteredCinderDisksByStorageDomainId,
    GetUnregisteredCinderDiskByIdAndStorageDomainId,
    GetAllLibvirtSecretsByProviderId,
    GetLibvirtSecretById,

    // Event Notification
    GetEventSubscribersBySubscriberIdGrouped,

    // oVirt
    GetoVirtISOs,

    // Async Tasks
    GetTasksStatusesByTasksIDs(QueryAuthType.User),

    // Quota
    GetQuotaByStoragePoolId,
    GetQuotaByQuotaId(QueryAuthType.User),
    GetQuotaClusterByQuotaId,
    GetQuotaStorageByQuotaId,
    GetVmsRelatedToQuotaId,
    GetTemplatesRelatedToQuotaId,
    GetPermissionsToConsumeQuotaByQuotaId,
    GetQuotasByAdElementId,
    GetQuotasConsumptionForCurrentUser(QueryAuthType.User),
    GetAllRelevantQuotasForStorage(QueryAuthType.User),
    GetAllRelevantQuotasForCluster(QueryAuthType.User),

    // Jobs
    GetJobByJobId,
    GetJobsByCorrelationId,
    GetJobsByOffset,
    GetAllJobs,
    GetStepWithSubjectEntitiesByStepId,
    GetStepsWithSubjectEntitiesByJobId,

    // Disks
    GetNextAvailableDiskAliasNameByVMId(QueryAuthType.User),

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
    GetGlusterVolumeGeoRepSessionById,
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
    GetAllGlusterVolumesForStorageDomain,
    GetGeoRepSessionsForStorageDomain,

    GetDefaultConfigurationVersion(QueryAuthType.User),
    GetProductVersion(QueryAuthType.User),
    OsRepository(QueryAuthType.User),
    GetArchitectureCapabilities(QueryAuthType.User),

    // Providers
    GetAllProviders,
    GetProviderById,
    GetProviderByName,
    GetAllNetworksForProvider,

    //Network QoS
    GetAllNetworkQosByStoragePoolId,

    // QoS
    GetQosById,
    GetAllQosByStoragePoolId,
    GetAllQosByStoragePoolIdAndType,
    GetAllQosByType,

    GetWatchdog(QueryAuthType.User),
    GetConsoleDevices(QueryAuthType.User),
    GetRngDevice(QueryAuthType.User),
    GetGraphicsDevices(QueryAuthType.User),
    GetNextRunGraphicsDevices(QueryAuthType.User),
    GetGraphicsDevicesMultiple(QueryAuthType.User),

    GetVmHostDevices,

    ConfigureConsoleOptions(QueryAuthType.User),
    GetConsoleDescriptorFile(QueryAuthType.User),

    GetDeviceCustomProperties(QueryAuthType.User),

    // Scheduling
    GetClusterPolicies,
    GetClusterPolicyById,
    GetAllPolicyUnits,
    GetPolicyUnitById,
    GetAttachedClustersByClusterPolicyId,
    GetAffinityGroupById,
    GetAffinityGroupsByClusterId,
    GetAffinityGroupsByVmId,

    GetAllDisksPartialDataByVmId(QueryAuthType.User),
    GetVmTemplateCount,

    //Disk Profiles
    GetDiskProfileById,
    GetAllDiskProfiles,
    GetDiskProfilesByStorageDomainId(QueryAuthType.User),
    GetDiskProfilesByStorageQosId,

    // Cpu Profiles
    GetCpuProfileById,
    GetAllCpuProfiles,
    GetCpuProfilesByClusterId(QueryAuthType.User),
    GetCpuProfilesByCpuQosId,

    IsUserApplicationContainerManager,

    // migration profiles
    GetAllMigrationPolicies(QueryAuthType.User),

    // Labels
    GetAllLabels,
    GetLabelById,
    GetLabelByEntityId,
    GetEntitiesNameMap,

    GetEntitiesWithLeaseByStorageId,

    GetSystemOption(QueryAuthType.User),

    // Default type instead of having to null check
    Unknown(QueryAuthType.User);

    /**
     * What kind of authorization the query requires. Although this is essentially a <code>boolean</code>, it's
     * implemented as an enum for future extendability.
     */
    public enum QueryAuthType {
        Admin,
        User
    }

    private QueryAuthType authType;

    QueryType() {
        authType = QueryAuthType.Admin;
    }

    QueryType(QueryAuthType authType) {
        this.authType = authType;
    }

    public int getValue() {
        return this.ordinal();
    }

    public static QueryType forValue(int value) {
        return values()[value];
    }

    public QueryAuthType getAuthType() {
        return authType;
    }

    public boolean isAdmin() {
        return authType == QueryAuthType.Admin;
    }
}
