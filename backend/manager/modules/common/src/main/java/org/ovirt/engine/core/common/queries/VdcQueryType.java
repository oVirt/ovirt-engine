package org.ovirt.engine.core.common.queries;

import java.io.Serializable;

public enum VdcQueryType implements Serializable {
    // VM queries
    IsVmWithSameNameExist(VdcQueryAuthType.User),
    GetVmByVmId(VdcQueryAuthType.User),
    GetVmByVmNameForDataCenter(VdcQueryAuthType.User),
    GetAllVms(VdcQueryAuthType.User),
    GetAllVmsForUser(VdcQueryAuthType.User),
    GetUnregisteredVms,
    GetUnregisteredVmTemplates,
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
    GetVmUpdatesOnNextRunExists(VdcQueryAuthType.User),

    // Vds queries
    GetVdsByVdsId,
    GetVdsByName,
    GetVdsFenceStatus,
    GetNewVdsFenceStatus,
    GetAgentFenceOptions,
    GetAllChildVlanInterfaces,
    GetAllSiblingVlanInterfaces,
    GetVlanParent,
    GetVdsHooksById,
    GetAllHosts,
    GetHostsByClusterId(VdcQueryAuthType.User),
    IsDisplayAddressConsistentInCluster,
    GetAllVdsByStoragePool(VdcQueryAuthType.User),
    GetHostListFromExternalProvider(),
    GetHostGroupsFromExternalProvider(),
    GetComputeResourceFromExternalProvider(),
    GetDiscoveredHostListFromExternalProvider(),
    GetProviderCertificateChain(),
    GetHostsForStorageOperation,
    GetServerSSHPublicKey,
    GetServerSSHKeyFingerprint,
    GetCpuStatisticsByVdsId,

    // VdsStatic Queries
    GetVdsStaticByName,

    // Vds Networks
    GetVdsInterfacesByVdsId(VdcQueryAuthType.User),
    GetVdsFreeBondsByVdsId,
    GetAllNetworks(VdcQueryAuthType.User),
    GetAllNetworksByClusterId(VdcQueryAuthType.User),
    GetNetworksByDataCenterId(VdcQueryAuthType.User),
    GetManagementInterfaceAddressByVmId(VdcQueryAuthType.User),
    GetInterfacesByLabelForNetwork,

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
    GetVdsGroupsAndNetworksByNetworkId,
    GetVdsAndNetworkInterfacesByNetworkId,
    GetVdsWithoutNetwork,
    GetVmsAndNetworkInterfacesByNetworkId,
    GetVmTemplatesAndNetworkInterfacesByNetworkId,
    GetNetworkById(VdcQueryAuthType.User),

    // External network providers
    GetAllExternalNetworksOnProvider,
    GetExternalSubnetsOnProviderByNetwork,

    // Network labels
    GetNetworkLabelsByNetworkId,
    GetNetworkLabelsByDataCenterId,
    GetNetworkLabelsByHostNicId,

    // NUMA
    GetVdsNumaNodesByVdsId(VdcQueryAuthType.User),
    GetVmNumaNodesByVmId(VdcQueryAuthType.User),
    GetAllVmsWithNumaByVdsGroupId(VdcQueryAuthType.User),

    // VdsGroups
    GetVdsCertificateSubjectByVdsId(VdcQueryAuthType.User),
    GetVdsCertificateSubjectByVmId(VdcQueryAuthType.User),
    GetAllVdsGroups(VdcQueryAuthType.User),
    GetVdsGroupByVdsGroupId(VdcQueryAuthType.User), // needed when updating VM
    GetVdsGroupById(VdcQueryAuthType.User),
    GetVdsGroupByName(VdcQueryAuthType.User),
    GetVdsGroupsByStoragePoolId(VdcQueryAuthType.User),
    GetNumberOfActiveVmsInVdsGroupByVdsGroupId,
    GetNumberOfVmsInVdsGroupByVdsGroupId,

    // Certificate
    GetCACertificate(VdcQueryAuthType.User),
    SignString(VdcQueryAuthType.User),

    // VM Template based entities queries
    IsVmTemlateWithSameNameExist(VdcQueryAuthType.User),
    GetVmTemplate(VdcQueryAuthType.User),
    GetAllVmTemplates(VdcQueryAuthType.User),
    GetAllInstanceTypes(VdcQueryAuthType.User),
    GetAllImageTypes(VdcQueryAuthType.User),
    GetVmTemplatesDisks(VdcQueryAuthType.User),
    GetVmTemplatesByStoragePoolId,
    GetVmTemplatesByImageGuid,
    GetSystemPermissions,

    // VM Snapshot queries
    GetAllVmSnapshotsByVmId(VdcQueryAuthType.User),
    GetAllVmSnapshotsFromConfigurationByVmId(VdcQueryAuthType.User),

    // Images queries
    GetImageById(VdcQueryAuthType.User),
    GetImagesList(VdcQueryAuthType.User),
    GetImagesListByStoragePoolId(VdcQueryAuthType.User),
    GetAllDisksByVmId(VdcQueryAuthType.User),
    GetAllAttachableDisks(VdcQueryAuthType.User),
    GetAllDisksByStorageDomainId,
    GetAllDisks(VdcQueryAuthType.User),
    GetAllDiskSnapshotsByStorageDomainId,
    GetUnregisteredDisks,
    GetUnregisteredDisk,
    GetDiskByDiskId(VdcQueryAuthType.User),
    GetDiskSnapshotByImageId,

    // Users queries
    GetUserVmsByUserIdAndGroups(VdcQueryAuthType.User),
    GetAllDbUsers(VdcQueryAuthType.User),
    GetDbUserByUserId(VdcQueryAuthType.User),
    GetDbUserByUserNameAndDomain(VdcQueryAuthType.User),
    GetUserBySessionId(VdcQueryAuthType.User),

    // Directory queries:
    GetDirectoryUserById(VdcQueryAuthType.User),
    GetDirectoryGroupById(VdcQueryAuthType.User),
    GetAvailableNamespaces(VdcQueryAuthType.User),

    // Groups queries:
    GetAllDbGroups(VdcQueryAuthType.User),
    GetDbGroupById,

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
    GetAvailableClusterVersionsByStoragePool,

    // AuditLog
    GetAllEventMessages(VdcQueryAuthType.User),
    GetAllAuditLogsByVMId(VdcQueryAuthType.User),
    GetAllAuditLogsByVMTemplateId(VdcQueryAuthType.User),
    GetAuditLogById,

    // Search queries
    Search,

    // Public services
    GetDomainList(VdcQueryAuthType.User),
    GetAAAProfileList(VdcQueryAuthType.User),
    RegisterVds(VdcQueryAuthType.User),
    CheckDBConnection(VdcQueryAuthType.User),
    ValidateSession(VdcQueryAuthType.User),
    GetValueBySession,

    // Auxiliary queries used by architecture compatibility
    IsClusterEmpty(VdcQueryAuthType.User),
    GetHostArchitecture(VdcQueryAuthType.User),

    // License queries
    GetAllServerCpuList,

    // Multi Level Administration queries
    GetAllRoles(VdcQueryAuthType.User),
    GetRoleById(VdcQueryAuthType.User),
    GetRoleByName,
    GetPermissionById(VdcQueryAuthType.User),
    GetPermissionByRoleId,
    GetPermissionsByAdElement,
    HasAdElementReconnectPermission(VdcQueryAuthType.User),
    GetPermissionsByAdElementId(VdcQueryAuthType.User),
    GetRoleActionGroupsByRoleId(VdcQueryAuthType.User),
    GetPermissionsForObject(VdcQueryAuthType.User),
    GetAllStoragePools(VdcQueryAuthType.User),
    GetDataCentersWithPermittedActionOnClusters(VdcQueryAuthType.User),
    GetClustersWithPermittedAction(VdcQueryAuthType.User),
    GetVmTemplatesWithPermittedAction(VdcQueryAuthType.User),

    // Storage
    GetStorageDomainById(VdcQueryAuthType.User),
    GetStorageDomainByName(VdcQueryAuthType.User),
    GetStorageServerConnectionById,
    GetAllStorageServerConnections,
    GetStorageServerConnectionsForDomain,
    GetStoragePoolById(VdcQueryAuthType.User),
    GetStoragePoolByDatacenterName(VdcQueryAuthType.User),
    GetStorageDomainsByConnection,
    GetConnectionsByDataCenterAndStorageType,
    GetStorageDomainsByStoragePoolId(VdcQueryAuthType.User),
    GetStorageDomainsByImageId,
    GetUnregisteredBlockStorageDomains,
    GetVgList,
    GetDeviceList,
    DiscoverSendTargets,
    GetStorageDomainsByVmTemplateId(VdcQueryAuthType.User),
    GetVmsFromExportDomain("org.ovirt.engine.core.bll.storage"),
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
    GetStorageTypesInPoolByPoolId,

    // Event Notification
    GetEventSubscribersBySubscriberIdGrouped,

    // oVirt
    GetoVirtISOs,

    // Async Tasks
    GetTasksStatusesByTasksIDs,

    // Quota
    GetQuotaByStoragePoolId,
    GetQuotaByQuotaId,
    GetQuotaVdsGroupByQuotaId,
    GetQuotaStorageByQuotaId,
    GetVmsRelatedToQuotaId,
    GetTemplatesRelatedToQuotaId,
    GetPermissionsToConsumeQuotaByQuotaId,
    GetQuotasByAdElementId,
    GetQuotasConsumptionForCurrentUser(VdcQueryAuthType.User),
    GetAllRelevantQuotasForStorage(VdcQueryAuthType.User),
    GetAllRelevantQuotasForVdsGroup(VdcQueryAuthType.User),

    // Jobs
    GetJobByJobId,
    GetJobsByCorrelationId,
    GetJobsByOffset,
    GetAllJobs,
    GetAllSteps,
    GetStepByStepId,
    GetStepsByJobId,

    // Commands
    GetCommandsCompatibilityVersions(VdcQueryAuthType.User),

    // Disks
    GetNextAvailableDiskAliasNameByVMId(VdcQueryAuthType.User),

    // Gluster
    GetGlusterVolumeById,
    GetGlusterVolumeOptionsInfo,
    GetGlusterVolumeBricks,
    GetGlusterVolumeBricksByServerId,
    GetGlusterVolumeBricksByTaskId,
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
    GetGlusterVolumeRemoveBricksStatus,
    GetGlusterVolumeByTaskId,

    GetDefaultConfigurationVersion(VdcQueryAuthType.User),
    OsRepository(VdcQueryAuthType.User),
    GetArchitectureCapabilities(VdcQueryAuthType.User),

    // Providers
    GetAllProviders,
    GetAllNetworksForProvider,

    //Network QoS
    GetAllNetworkQosByStoragePoolId,

    GetWatchdog(VdcQueryAuthType.User),
    GetConsoleDevices(VdcQueryAuthType.User),
    GetRngDevice(VdcQueryAuthType.User),

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

    private static final String DEFAULT_PACKAGE_NAME = "org.ovirt.engine.core.bll";

    private String packageName;

    private VdcQueryAuthType authType;

    private VdcQueryType() {
        packageName = DEFAULT_PACKAGE_NAME;
        authType = VdcQueryAuthType.Admin;
    }

    private VdcQueryType(String packageName) {
        this.packageName = packageName;
        authType = VdcQueryAuthType.Admin;
    }

    private VdcQueryType(VdcQueryAuthType authType) {
        packageName = DEFAULT_PACKAGE_NAME;
        this.authType = authType;
    }

    public int getValue() {
        return this.ordinal();
    }

    public static VdcQueryType forValue(int value) {
        return values()[value];
    }

    public String getPackageName() {
        return packageName;
    }

    public VdcQueryAuthType getAuthType() {
        return authType;
    }

    public boolean isAdmin() {
        return authType == VdcQueryAuthType.Admin;
    }
}
