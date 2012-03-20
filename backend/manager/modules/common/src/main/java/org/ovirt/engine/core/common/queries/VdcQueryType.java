package org.ovirt.engine.core.common.queries;

import java.io.Serializable;

@SuppressWarnings("unused")
public enum VdcQueryType implements Serializable {
    // VM queries
    IsVmWithSameNameExist,
    GetImportCandidates,
    GetImportCandidatesInfo,
    GetAllImportCandidatesInfo,
    GetCandidateInfo,
    GetVmByVmId,
    GetVmsRunningOnVDS,
    GetVmsRunningOnVDSCount,
    GetTopSizeVmsFromStorageDomain,
    GetVmCustomProperties,
    GetVmConfigurationBySnapshot(VdcQueryAuthType.User),
    GetVmsByImageGuid,

    // Vds queries
    IsVdsWithSameNameExist,
    IsVdsWithSameHostExist,
    IsVdsWithSameIpExists,
    GetVdsByVdsId(VdcQueryAuthType.User),
    GetVdsByHost,
    GetVdsByName,
    GetVdsByType,
    GetVdsFenceStatus,
    GetNewVdsFenceStatus,
    CanFenceVds,
    GetAgentFenceOptions,
    GetAgentFenceOptions2,
    GetAllChildVlanInterfaces,
    GetAllSiblingVlanInterfaces,
    GetVlanParanet,
    GetVdsHooksById,
    GetVdsHooksById2,

    // Vds Networks
    GetVdsInterfacesByVdsId,
    GetVdsFreeBondsByVdsId,
    GetAllNetworks,
    GetAllNetworksByClusterId(VdcQueryAuthType.User),
    GetNetworkDisplayByClusterId,
    GetNonOperationalVds,

    // Vm Network
    GetVmInterfacesByVmId(VdcQueryAuthType.User),

    // Template Network
    GetTemplateInterfacesByTemplateId,

    // VdsGroups
    GetVdsCertificateSubjectByVdsId,
    GetAllVdsGroups,
    GetVdsGroupByVdsGroupId,
    GetVdsGroupById(VdcQueryAuthType.User),
    GetVdsGroupByName,
    IsVdsGroupWithSameNameExist,
    GetVdsGroupsByStoragePoolId,

    // Certificate
    GetCACertificate,

    // VM Templates queries
    IsVmTemlateWithSameNameExist,
    GetVmTemplate,
    GetAllVmTemplates,
    GetVmsByVmTemplateGuid,
    GetVmTemplatesDisks,
    GetVmTemplatesByStoragePoolId,
    GetVmTemplatesByImageGuid,
    GetSystemPermissions,

    // VM Snapshot queries
    @Deprecated
    GetAllVmSnapshotsByDrive(VdcQueryAuthType.User),
    GetAllVmSnapshotsByVmId(VdcQueryAuthType.User),

    // Images queries
    GetAllIsoImagesList,
    GetAllFloppyImagesList,
    GetAllDisksByVmId(VdcQueryAuthType.User),
    GetImageByImageId,
    GetImagesByStorageDomainAndTemplate,

    // Users queries
    GetUserVmsByUserIdAndGroups(VdcQueryAuthType.User),
    GetTimeLeasedUsersByVmPoolId,
    GetDbUserByUserId,
    GetUsersByVmid,
    GetVmsByUserid,
    GetUserMessage,
    GetUserBySessionId(VdcQueryAuthType.User),

    // AdGroups queries
    GetAllAdGroups,
    GetAdGroupsAttachedToTimeLeasedVmPool,
    GetVmPoolsAttachedToAdGroup,
    GetAdGroupById,

    // VM pools queries
    GetVmPoolById,
    GetVmPoolsMapByVmPoolId,
    GetAllVmPools,
    HasFreeVmsInPool,
    GetAllVmPoolsAttachedToUser(VdcQueryAuthType.User),
    IsVmPoolWithSameNameExists,

    // Tags queries
    GetAllTags,
    GetAllNotReadonlyTags,
    GetRootTag,
    GetTagByTagId,
    GetTagByTagName,
    GetTagsByUserGroupId,
    GetTagsByUserId,
    GetTagsByVmId,
    GetTagsByVdsId,
    GetTagUserMapByTagName,
    GetTagUserGroupMapByTagName,
    GetTagVmMapByTagName,
    GetTagVdsMapByTagName,
    GetTagIdsAndChildrenIdsByRegExp,
    GetTagIdAndChildrenIds,

    // System
    GetSystemStatistics,
    GetStorageStatistics,

    // Bookmarks
    GetBookmarkById,
    GetBookmarkByName,
    GetAllBookmarks,

    // FieldsUpdating
    CanUpdateFieldGeneric,

    // Configuration values
    GetConfigurationValue(VdcQueryAuthType.User),
    GetTimeZones,
    GetDefualtTimeZone,
    GetDiskConfigurationList,
    GetAvailableClusterVersions,
    GetAvailableStoragePoolVersions,
    GetAvailableClusterVersionsByStoragePool,

    // AuditLog
    GetVdsMessages,
    GetVmsMessages,
    GetUserMessages,
    GetEventMessages,
    GetTemplateMessages,
    GetAllAuditLogsByVMName(VdcQueryAuthType.User),
    GetAllAuditLogsByVMTemplateName(VdcQueryAuthType.User),

    // Search queries
    Search,
    RegisterSearch,
    UnregisterSearch,

    // Public services
    GetDomainList,
    IsLicenseValid,
    IsLicenseSupported,
    RegisterVds,
    CheckDBConnection,

    // License queries
    GetLicenseProperties,
    GetLicenseProductType,
    GetResourceUsage,
    GetPowerClient,
    AddPowerClient,
    GetDedicatedVm,
    GetMACAddress,
    GetAllServerCpuList,
    GetAvailableClustersByServerCpu,

    // Multi Level Administration queries
    GetAllRoles,
    GetRolesByAdElement,
    GetRolesByAdElementIdAndNullTag,
    GetRoleById,
    GetRoleByName,
    GetPermissionById,
    GetPermissionByRoleId,
    GetPermissionsByAdElement,
    GetRolesByAdElementId,
    GetPermissionsByAdElementId(VdcQueryAuthType.User),
    GetRoleActionGroupsByRoleId,
    IsUserPowerUserOrAbove,
    GetRolesForDelegationByUser,
    GetPermissionsForObject,
    GetDataCentersWithPermittedActionOnClusters(VdcQueryAuthType.User),
    GetClustersWithPermittedAction(VdcQueryAuthType.User),
    GetVmTemplatesWithPermittedAction(VdcQueryAuthType.User),

    // Storage
    IsStoragePoolWithSameNameExist,
    GetStorageDomainById(VdcQueryAuthType.User),
    GetStorageServerConnectionById,
    GetStoragePoolById(VdcQueryAuthType.User),
    GetStorageDomainsByConnection,
    GetStorageDomainsByStoragePoolId(VdcQueryAuthType.User),
    GetStorageServerConnections,
    GetVgList,
    GetVGInfo,
    GetDeviceList,
    DiscoverSendTargets,
    GetStorageSessionsList,
    GetStorageDomainsByVmTemplateId,
    GetVmsFromExportDomain("org.ovirt.engine.core.bll.storage"),
    GetTemplatesFromExportDomain,
    GetVmTemplatesFromStorageDomain,
    GetAllIdsFromExportDomain,
    GetExistingStorageDomainList,
    GetStorageDomainByIdAndStoragePoolId,
    GetStoragePoolsByStorageDomainId,
    GetStorageDomainListById,
    GetLunsByVgId,

    // Event Notification
    GetEventNotificationMethods,
    GetEventNotificationMethodByType,
    GetNotificationEventMap,
    GetAllEventSubscribers,
    GetEventSubscribersBySubscriberId,
    GetEventSubscribersBySubscriberIdGrouped,

    // Query registration
    RegisterQuery,
    UnregisterQuery,

    // oVirt
    GetoVirtISOs,

    // Async Tasks
    GetTasksStatusesByTasksIDs,

    // Quota
    GetQuotaByStoragePoolId,
    GetQuotaByQuotaId,
    GetQuotaVdsGroupByQuotaId,
    GetQuotaStorageByQuotaId,
    GetDisksForQuotaId,
    GetVmsRelatedToQuotaId,
    GetTemplatesRelatedToQuotaId,
    GetPermissionsToConsumeQuotaByQuotaId,
    GetQuotasByAdElementId,

    // Jobs
    GetJobByJobId,
    GetJobsByCorrelationId,
    GetJobsByOffset,

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
