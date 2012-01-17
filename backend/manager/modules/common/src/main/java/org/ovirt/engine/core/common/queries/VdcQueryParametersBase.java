package org.ovirt.engine.core.common.queries;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VdsNetworkInterface;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.common.businessentities.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network;
import org.ovirt.engine.core.common.businessentities.permissions;
import org.ovirt.engine.core.common.businessentities.roles_actions;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.common.businessentities.storage_server_connections;
import org.ovirt.engine.core.compat.Version;

@XmlSeeAlso({
        VdcQueryParametersBase.class,
        IsVmWithSameNameExistParameters.class,
        GetTagByTagIdParameters.class,
        GetTagsByUserGroupIdParameters.class,
        GetTagsByUserIdParameters.class,
        GetTagsByVmIdParameters.class,
        GetTagsByVdsIdParameters.class,
        GetTagByTagNameParametersBase.class,
        GetTagUserMapByTagNameParameters.class,
        GetTagUserGroupMapByTagNameParameters.class,
        GetTagVmMapByTagNameParameters.class,
        GetTagVdsMapByTagNameParameters.class,
        GetTagByTagNameParameters.class,
        GetBookmarkByIdParameters.class,
        GetBookmarkByNameParameters.class,
        // CanUpdateFieldGenericParameters.class,
        GetConfigurationValueParameters.class,
        GetVmByVmIdParameters.class,
        GetVmsRunningOnVDSParameters.class,
        GetVmsRunningOnVDSCountParameters.class,
        GetAllVmSnapshotsParameters.class,
        IsVdsWithSameNameExistParameters.class,
        IsVdsWithSameHostExistParameters.class,
        IsStoragePoolWithSameNameExistParameters.class,
        IsVdsWithSameIpExistsParameters.class,
        GetVdsByVdsIdParameters.class,
        GetVdsGroupByIdParameters.class,
        GetVdsGroupByNameParameters.class,
        IsVdsGroupWithSameNameExistParameters.class,
        IsVmTemlateWithSameNameExistParameters.class,
        GetVmsByVmTemplateGuidParameters.class,
        GetAllVmSnapshotsByDriveParameters.class,
        GetAllUsersInVdcRoleParameters.class,
        GetUserVmsByUserIdAndGroupsParameters.class,
        GetUserMessageParameters.class,
        GetTimeLeasedUsersByVmPoolIdParameters.class,
        GetDbUserByUserIdParameters.class,
        GetDbUserByVmPoolIdParameters.class,
        GetUsersByVmidParameters.class,
        GetVmsByUseridParameters.class,
        GetAdGroupsAttachedToVmPoolParameters.class,
        GetAdGroupsAttachedToTimeLeasedVmPoolParameters.class,
        GetVmPoolsAttachedToAdGroupParameters.class,
        GetAdGroupByIdParameters.class,
        GetVmPoolByIdParametersBase.class,
        GetVmPoolByIdParameters.class,
        GetVmPoolsMapByVmPoolIdParameters.class,
        HasFreeVmsInPoolParameters.class,
        GetAllVmPoolsAttachedToUserParameters.class,
        IsVmPoolWithSameNameExistsParameters.class,
        GetMessagesByIdParametersBase.class,
        GetVdsMessagesParameters.class,
        GetVmsMessagesParameters.class,
        GetUserMessagesParameters.class,
        GetEventMessagesParameters.class,
        GetTemplateMessagesParameters.class,
        SearchParameters.class,
        RegisterQueryParameters.class,
        UnregisterQueryParameters.class,
        VM.class,
        VMStatus.class,
        VDS.class,
        VDSStatus.class,
        GetVmTemplateParameters.class,
        GetResourceUsageParameters.class,
        GetImportCandidatesQueryParameters.class,
        CandidateInfoParameters.class,
        GetAllImportCandidatesQueryParameters.class,
        GetPowerClientByClientInfoParameters.class,
        GetVdsByNameParameters.class,
        GetVdsByHostParameters.class,
        AddPowerClientParameters.class,
        GetDedicatedVmParameters.class,
        GetAvailableClustersByServerCpuParameters.class,
        MultilevelAdministrationsQueriesParameters.class,
        MultilevelAdministrationByAdElementIdParameters.class,
        MultilevelAdministrationByRoleIdParameters.class,
        MultilevelAdministrationByRoleNameParameters.class,
        MultilevelAdministrationByPermissionIdParameters.class,
        VdsNetworkInterface.class,
        VmNetworkInterface.class,
        // java.util.ArrayList<Interface>.class,
        network.class,
        // java.util.ArrayList<network>.class,
        VdsGroupQueryParamenters.class,
        NetworkNonOperationalQueryParamenters.class,
        VdsStatic.class,
        // java.util.ArrayList<VdsStatic>.class,
        StorageDomainQueryParametersBase.class,
        StorageDomainQueryTopSizeVmsParameters.class,
        StoragePoolQueryParametersBase.class,
        StorageServerConnectionQueryParametersBase.class,
        storage_pool.class,
        // java.util.ArrayList<storage_pool>.class,
        storage_domains.class,
        // java.util.ArrayList<storage_domains>.class,
        GetAllIsoImagesListParameters.class,
        GetAllDisksByVmIdParameters.class,
        GetVmTemplatesDisksParameters.class,
        GetVmTemplatesByStoragePoolIdParameters.class,
        roles_actions.class,
        // java.util.ArrayList<roles_actions>.class,
        permissions.class,
        // java.util.ArrayList<permissions>.class,
        GetEventNotificationMethodByTypeParameters.class,
        VdsIdParametersBase.class,
        VGQueryParametersBase.class,
        storage_server_connections.class,
        // java.util.ArrayList<storage_server_connections>.class,
        DiscoverSendTargetsQueryParameters.class,
        GetEventSubscribersBySubscriberIdParameters.class,
        GetAllNetworkQueryParamenters.class,
        GetTasksStatusesByTasksIDsParameters.class,
        GetImageByImageIdParameters.class,
        GetStorageDomainsByConnectionParameters.class,
        GetStorageDomainsByVmTemplateIdQueryParameters.class,
        GetNewVdsFenceStatusParameters.class,
        GetVdsGroupByVdsGroupIdParameters.class,
        GetAllFromExportDomainQueryParamenters.class,
        // java.util.ArrayList<Guid>.class,
        StorageType.class, GetDeviceListQueryParameters.class, GetSystemStatisticsQueryParameters.class,
        GetAllChildVlanInterfacesQueryParameters.class,
        StorageDomainAndPoolQueryParameters.class, RegisterableQueryReturnDataType.class,
        GetAvailableClusterVersionsParameters.class, GetAvailableStoragePoolVersionsParameters.class,
        GetAllServerCpuListParameters.class, Version.class, GetAvailableClusterVersionsByStoragePoolParameters.class,
        GetExistingStorageDomainListParameters.class, GetPermissionsForObjectParameters.class,
        GetByUserIdParameters.class, GetVdsHooksByIdParameters.class, ValueObjectMap.class,
        GetLunsByVgIdParameters.class, GetEntitiesWithPermittedActionParameters.class, GetDomainListParameters.class })
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "VdcQueryParametersBase", namespace = "http://service.engine.ovirt.org")
public class VdcQueryParametersBase implements Serializable {
    private static final long serialVersionUID = -6766170283465888549L;

    /**
     * The identifier of session which should be set by sender via Rest Api or by front end
     */
    private String sessionId;
    /**
     * The identifier of session which should be set by web client of front end
     */
    private String httpSessionId;

    /**
     * The boolean flag which provides if the session should be refreshed
     */
    private boolean refresh = true;

    public RegisterableQueryReturnDataType GetReturnedDataTypeByVdcQueryType(VdcQueryType queryType) {
        return RegisterableQueryReturnDataType.LIST_IQUERYABLE;
    }

    public VdcQueryParametersBase() {
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getHttpSessionId() {
        return httpSessionId;
    }

    public void setHttpSessionId(String httpSessionId) {
        this.httpSessionId = httpSessionId;
    }

    @XmlElement(name="Refresh", defaultValue="true")
    public boolean getRefresh() {
        return refresh;
    }

    public void setRefresh(boolean refresh) {
        this.refresh = refresh;
    }

}
