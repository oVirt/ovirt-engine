package org.ovirt.engine.core.common.queries;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlType;

@XmlType(name = "VdcQueryType")
public enum VdcQueryType implements Serializable {
    /**
     * Vm queries
     */
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
    /**
     * Vds queries
     */
    IsVdsWithSameNameExist,
    IsVdsWithSameHostExist,
    IsVdsWithSameIpExists,
    GetVdsByVdsId,
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
    /**
     * Vds Networks
     */
    GetVdsInterfacesByVdsId,
    GetVdsFreeBondsByVdsId,
    GetAllNetworks,
    GetAllNetworksByClusterId,
    GetNetworkDisplayByClusterId,
    GetNonOperationalVds,
    /**
     * Vm Network
     */
    GetVmInterfacesByVmId,
    /**
     * Template Network
     */
    GetTemplateInterfacesByTemplateId,
    /**
     * VdsGroups
     */
    GetVdsCertificateSubjectByVdsId,
    GetAllVdsGroups,
    GetVdsGroupByVdsGroupId,
    GetVdsGroupById,
    GetVdsGroupByName,
    IsVdsGroupWithSameNameExist,
    GetVdsGroupsByStoragePoolId,
    /**
     * certificate
     */
    GetCACertificate,
    /**
     * Vm Templates queries
     */
    IsVmTemlateWithSameNameExist,
    GetVmTemplate,
    GetAllVmTemplates,
    GetVmsByVmTemplateGuid,
    GetVmTemplatesDisks,
    GetVmTemplatesByStoragePoolId,
    GetSystemPermissions,
    /**
     * Images queries
     */
    GetAllVmSnapshotsByDrive,
    GetAllIsoImagesList,
    GetAllFloppyImagesList,
    GetAllDisksByVmId,
    GetImageByImageId,
    // Users queries
    GetUserVmsByUserIdAndGroups,
    GetTimeLeasedUsersByVmPoolId,
    GetDbUserByUserId,
    GetUsersByVmid,
    GetVmsByUserid,
    GetUserMessage,
    GetUserBySessionId,
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
    GetAllVmPoolsAttachedToUser,
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
    GetConfigurationValue,
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
    // Search queries
    Search,
    RegisterSearch,
    UnregisterSearch,
    // public services
    GetDomainList,
    IsLicenseValid,
    IsLicenseSupported,
    RegisterVds,
    CheckDBConnection,
    // license queries
    GetLicenseProperties,
    GetLicenseProductType,
    GetResourceUsage,
    GetPowerClient,
    AddPowerClient,
    GetDedicatedVm,
    GetMACAddress,
    GetAllServerCpuList,
    GetAvailableClustersByServerCpu,
    // multi level administration queries
    GetAllRoles,
    GetRolesByAdElement,
    GetRolesByAdElementIdAndNullTag,
    GetRoleById,
    GetRoleByName,
    GetPermissionById,
    GetPermissionByRoleId,
    GetPermissionsByAdElement,
    GetRolesByAdElementId,
    GetPermissionsByAdElementId,
    GetRoleActionGroupsByRoleId,
    IsUserPowerUserOrAbove,
    GetRolesForDelegationByUser,
    GetPermissionsForObject,
    GetDataCentersWithPermittedActionOnClusters,
    GetClustersWithPermittedAction,
    GetVmTemplatesWithPermittedAction,

    // Storage
    IsStoragePoolWithSameNameExist,
    GetStorageDomainById,
    GetStorageServerConnectionById,
    GetStoragePoolById,
    GetStorageDomainsByConnection,
    GetStorageDomainsByStoragePoolId,
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
    GetTasksStatusesByTasksIDs;

    private static final String DEFAULT_PACKAGE_NAME = "org.ovirt.engine.core.bll";

    private String packageName;

    private VdcQueryType() {
        packageName = DEFAULT_PACKAGE_NAME;
    }

    private VdcQueryType(String packageName) {
        this.packageName = packageName;
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
}
