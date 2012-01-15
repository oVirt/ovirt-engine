package org.ovirt.engine.ui.uicommonweb;

import java.util.Collections;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.EventNotificationEntity;
import org.ovirt.engine.core.common.VdcEventNotificationUtils;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.DbUser;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.DiskImageBase;
import org.ovirt.engine.core.common.businessentities.DiskInterface;
import org.ovirt.engine.core.common.businessentities.DiskType;
import org.ovirt.engine.core.common.businessentities.IVdcQueryable;
import org.ovirt.engine.core.common.businessentities.RepoFileMetaData;
import org.ovirt.engine.core.common.businessentities.RoleType;
import org.ovirt.engine.core.common.businessentities.ServerCpu;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.TagsType;
import org.ovirt.engine.core.common.businessentities.UsbPolicy;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VDSType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VdsNetworkInterface;
import org.ovirt.engine.core.common.businessentities.VdsSelectionAlgorithm;
import org.ovirt.engine.core.common.businessentities.VdsSpmStatus;
import org.ovirt.engine.core.common.businessentities.VmInterfaceType;
import org.ovirt.engine.core.common.businessentities.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.VmOsType;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.VmTemplateStatus;
import org.ovirt.engine.core.common.businessentities.VmType;
import org.ovirt.engine.core.common.businessentities.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.VolumeType;
import org.ovirt.engine.core.common.businessentities.bookmarks;
import org.ovirt.engine.core.common.businessentities.event_subscriber;
import org.ovirt.engine.core.common.businessentities.network;
import org.ovirt.engine.core.common.businessentities.permissions;
import org.ovirt.engine.core.common.businessentities.roles;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.common.businessentities.storage_server_connections;
import org.ovirt.engine.core.common.businessentities.tags;
import org.ovirt.engine.core.common.businessentities.vm_pools;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.ConfigurationValues;
import org.ovirt.engine.core.common.queries.GetAllChildVlanInterfacesQueryParameters;
import org.ovirt.engine.core.common.queries.GetAllDisksByVmIdParameters;
import org.ovirt.engine.core.common.queries.GetAllIsoImagesListParameters;
import org.ovirt.engine.core.common.queries.GetAllNetworkQueryParamenters;
import org.ovirt.engine.core.common.queries.GetAllServerCpuListParameters;
import org.ovirt.engine.core.common.queries.GetAllVmSnapshotsByDriveParameters;
import org.ovirt.engine.core.common.queries.GetAllVmSnapshotsByDriveQueryReturnValue;
import org.ovirt.engine.core.common.queries.GetAvailableClusterVersionsByStoragePoolParameters;
import org.ovirt.engine.core.common.queries.GetAvailableClusterVersionsParameters;
import org.ovirt.engine.core.common.queries.GetAvailableStoragePoolVersionsParameters;
import org.ovirt.engine.core.common.queries.GetConfigurationValueParameters;
import org.ovirt.engine.core.common.queries.GetDomainListParameters;
import org.ovirt.engine.core.common.queries.GetEventSubscribersBySubscriberIdParameters;
import org.ovirt.engine.core.common.queries.GetStorageDomainsByConnectionParameters;
import org.ovirt.engine.core.common.queries.GetStorageDomainsByVmTemplateIdQueryParameters;
import org.ovirt.engine.core.common.queries.GetSystemStatisticsQueryParameters;
import org.ovirt.engine.core.common.queries.GetTagsByUserGroupIdParameters;
import org.ovirt.engine.core.common.queries.GetTagsByUserIdParameters;
import org.ovirt.engine.core.common.queries.GetTagsByVdsIdParameters;
import org.ovirt.engine.core.common.queries.GetTagsByVmIdParameters;
import org.ovirt.engine.core.common.queries.GetUserVmsByUserIdAndGroupsParameters;
import org.ovirt.engine.core.common.queries.GetVdsByVdsIdParameters;
import org.ovirt.engine.core.common.queries.GetVdsGroupByIdParameters;
import org.ovirt.engine.core.common.queries.GetVmByVmIdParameters;
import org.ovirt.engine.core.common.queries.GetVmTemplateParameters;
import org.ovirt.engine.core.common.queries.GetVmTemplatesByStoragePoolIdParameters;
import org.ovirt.engine.core.common.queries.GetVmTemplatesDisksParameters;
import org.ovirt.engine.core.common.queries.IsStoragePoolWithSameNameExistParameters;
import org.ovirt.engine.core.common.queries.IsVdsGroupWithSameNameExistParameters;
import org.ovirt.engine.core.common.queries.IsVdsWithSameNameExistParameters;
import org.ovirt.engine.core.common.queries.IsVmPoolWithSameNameExistsParameters;
import org.ovirt.engine.core.common.queries.IsVmTemlateWithSameNameExistParameters;
import org.ovirt.engine.core.common.queries.IsVmWithSameNameExistParameters;
import org.ovirt.engine.core.common.queries.MultilevelAdministrationByAdElementIdParameters;
import org.ovirt.engine.core.common.queries.MultilevelAdministrationByRoleIdParameters;
import org.ovirt.engine.core.common.queries.MultilevelAdministrationsQueriesParameters;
import org.ovirt.engine.core.common.queries.SearchParameters;
import org.ovirt.engine.core.common.queries.StorageDomainQueryParametersBase;
import org.ovirt.engine.core.common.queries.StoragePoolQueryParametersBase;
import org.ovirt.engine.core.common.queries.StorageServerConnectionQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.queries.VdsGroupQueryParamenters;
import org.ovirt.engine.core.common.users.VdcUser;
import org.ovirt.engine.core.compat.CultureInfo;
import org.ovirt.engine.core.compat.EnumCompat;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.IntegerCompat;
import org.ovirt.engine.core.compat.KeyValuePairCompat;
import org.ovirt.engine.core.compat.NGuid;
import org.ovirt.engine.core.compat.RefObject;
import org.ovirt.engine.core.compat.StringFormat;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.genericapi.parameters.UIQueryParametersBase;
import org.ovirt.engine.ui.genericapi.returnvalues.UIQueryReturnValue;
import org.ovirt.engine.ui.genericapi.uiqueries.UIQueryType;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.tags.TagModel;
import org.ovirt.engine.ui.uicommonweb.models.users.UserPermissionModel;
import org.ovirt.engine.ui.uicompat.Assembly;
import org.ovirt.engine.ui.uicompat.ResourceManager;

/**
 * Contains method for retrieving common data (mostly via frontend).
 *
 *
 * All method returning list of objects must avoid returning a null value, but an empty list.
 */
@SuppressWarnings("unused")
public final class DataProvider
{
    public static final int SearchLimit = 9999;

    public static String GetLocalFSPath()
    {
        VdcQueryReturnValue returnValue =
                GetConfigFromCache(new GetConfigurationValueParameters(ConfigurationValues.RhevhLocalFSPath));

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            return (String) returnValue.getReturnValue();
        }

        return "";
    }

    public static String GetLinuxMountPointRegex()
    {
        // 32-bit IPv4 Internet Protocol (IP): RFC 1918
        // ^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$

        // FQDN: RFC's 952/1123
        // ^([a-zA-Z0-9]([a-zA-Z0-9\-]{0,61}[a-zA-Z0-9])?\.)+[a-zA-Z]{2,}$

        // Linux path
        // (.*?/|.*?\\)?([^\./|^\.\\]+)(?:\.([^\\]*)|)$

        // [IP:/path or FQDN:/path]
        return "^((?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)|(([a-zA-Z0-9]([a-zA-Z0-9\\-]{0,61}[a-zA-Z0-9])?\\.)+[a-zA-Z]{2,}))\\:/(.*?/|.*?\\\\)?([^\\./|^\\.\\\\]+)(?:\\.([^\\\\]*)|)$";
    }

    public static java.util.ArrayList<UserPermissionModel> GetUserPermissionMatrix(Guid userId)
    {
        // var roles = GetRoleList().ToDictionary(a => a.id);
        java.util.HashMap<Guid, roles> roles = new java.util.HashMap<Guid, roles>();
        for (roles role : GetRoleList())
        {
            roles.put(role.getId(), role);
        }

        VdcQueryReturnValue returnValue =
                Frontend.RunQuery(VdcQueryType.GetPermissionsByAdElementId,
                        new MultilevelAdministrationByAdElementIdParameters(userId));

        if (returnValue == null || !returnValue.getSucceeded())
        {
            return null;
        }
        java.util.ArrayList<permissions> permissions = (java.util.ArrayList<permissions>) returnValue.getReturnValue();

        java.util.ArrayList<UserPermissionModel> userPermissions = new java.util.ArrayList<UserPermissionModel>();

        for (permissions permission : permissions)
        {
            UserPermissionModel userPermission = new UserPermissionModel();
            userPermission.setId(permission.getId());
            ListModel tempVar = new ListModel();
            tempVar.setSelectedItem(roles.get(permission.getrole_id()).getname());
            userPermission.setRole(tempVar);
            java.util.ArrayList<TagModel> tags = new java.util.ArrayList<TagModel>();
            for (tags tag : permission.getTags())
            {
                TagModel tagModel = new TagModel();
                EntityModel entityModel = new EntityModel();
                entityModel.setEntity(tag.gettag_name());
                tagModel.setName(entityModel);
                tags.add(tagModel);
            }
            userPermission.setTags(tags);
            userPermissions.add(userPermission);
        }
        // return permissions
        // .Select(a =>
        // new UserPermissionModel
        // {
        // Id = a.id,
        // Role = public ListModel(container) { Value = roles[a.role_id].name },
        // Tags = a.Tags.Select(b => new TagModel { Name = new EntityModel(Container) { Value = b.tag_name } }).ToList()
        // }
        // )
        // .ToList();
        return userPermissions;
    }

    public static java.util.ArrayList<event_subscriber> GetEventNotificationList(Guid userId)
    {
        VdcQueryReturnValue returnValue =
                Frontend.RunQuery(VdcQueryType.GetEventSubscribersBySubscriberId,
                        new GetEventSubscribersBySubscriberIdParameters(userId));
        if (returnValue == null || !returnValue.getSucceeded())
        {
            return new java.util.ArrayList<event_subscriber>();
        }

        return (java.util.ArrayList<event_subscriber>) returnValue.getReturnValue();
    }

    public static java.util.ArrayList<network> GetNetworkList(Guid storagePoolId)
    {
        VdcQueryReturnValue returnValue =
                Frontend.RunQuery(VdcQueryType.GetAllNetworks, new GetAllNetworkQueryParamenters(storagePoolId));

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            return (java.util.ArrayList<network>) returnValue.getReturnValue();
        }

        return new java.util.ArrayList<network>();
    }

    public static java.util.ArrayList<network> GetClusterNetworkList(Guid clusterId)
    {
        VdcQueryReturnValue returnValue =
                Frontend.RunQuery(VdcQueryType.GetAllNetworksByClusterId, new VdsGroupQueryParamenters(clusterId));

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            return (java.util.ArrayList<network>) returnValue.getReturnValue();
        }

        return new java.util.ArrayList<network>();
    }

    public static java.util.ArrayList<roles> GetRoleList()
    {
        VdcQueryReturnValue returnValue =
                Frontend.RunQuery(VdcQueryType.GetAllRoles, new MultilevelAdministrationsQueriesParameters());

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            return (java.util.ArrayList<roles>) returnValue.getReturnValue();
        }

        return new java.util.ArrayList<roles>();
    }

    private static String cachedDefaultTimeZone;

    public static String GetDefaultTimeZone()
    {
        if (cachedDefaultTimeZone == null)
        {
            VdcQueryReturnValue returnValue =
                    Frontend.RunQuery(VdcQueryType.GetDefualtTimeZone, new VdcQueryParametersBase());

            if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
            {
                cachedDefaultTimeZone = ((java.util.Map.Entry<String, String>) returnValue.getReturnValue()).getKey();
            }
            else
            {
                cachedDefaultTimeZone = "";
            }
        }

        return cachedDefaultTimeZone;
    }

    private static java.util.HashMap<String, String> cachedTimeZones;

    public static java.util.HashMap<String, String> GetTimeZoneList()
    {
        if (cachedTimeZones == null)
        {
            VdcQueryReturnValue returnValue =
                    Frontend.RunQuery(VdcQueryType.GetTimeZones, new VdcQueryParametersBase());

            if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
            {
                cachedTimeZones = (java.util.HashMap<String, String>) returnValue.getReturnValue();
            }
            else
            {
                cachedTimeZones = new java.util.HashMap<String, String>();
            }
        }

        return cachedTimeZones;
    }

    public static java.util.ArrayList<VDSGroup> GetClusterList()
    {
        VdcQueryReturnValue returnValue = Frontend.RunQuery(VdcQueryType.GetAllVdsGroups, new VdcQueryParametersBase());
        if (returnValue != null && returnValue.getSucceeded())
        {
            java.util.ArrayList<VDSGroup> list = (java.util.ArrayList<VDSGroup>) returnValue.getReturnValue();
            if (list != null)
            {
                // return Linq.OrderBy<VDSGroup>( groups.OrderBy(a => a.name).ToList();
                Collections.sort(list, new Linq.VdsGroupByNameComparer());
                return list;
            }
        }

        return new java.util.ArrayList<VDSGroup>();
    }

    public static java.util.ArrayList<VDSGroup> GetClusterList(Guid storagePoolID)
    {
        VdcQueryReturnValue returnValue =
                Frontend.RunQuery(VdcQueryType.GetVdsGroupsByStoragePoolId,
                        new StoragePoolQueryParametersBase(storagePoolID));

        if (returnValue != null && returnValue.getSucceeded())
        {
            java.util.ArrayList<VDSGroup> list = (java.util.ArrayList<VDSGroup>) returnValue.getReturnValue();
            if (list != null)
            {
                // return groups.OrderBy(a => a.name).ToList();
                Collections.sort(list, new Linq.VdsGroupByNameComparer());
                return list;
            }
        }

        return new java.util.ArrayList<VDSGroup>();
    }

    public static java.util.ArrayList<String> GetDomainList(boolean filterInternalDomain)
    {
        GetDomainListParameters tempVar = new GetDomainListParameters();
        tempVar.setFilterInternalDomain(filterInternalDomain);
        VdcQueryReturnValue returnValue = Frontend.RunQuery(VdcQueryType.GetDomainList, tempVar);

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            return (java.util.ArrayList<String>) returnValue.getReturnValue();
        }

        return new java.util.ArrayList<String>();
    }

    // NOTE: Moved to AsyncDataProvider.
    // public static List<string> GetDomainListViaPublic()
    // {
    // VdcQueryReturnValue returnValue = Frontend.RunPublicQuery(VdcQueryType.GetDomainList,
    // new VdcQueryParametersBase());

    // if (returnValue != null && returnValue.Succeeded && returnValue.ReturnValue != null)
    // {
    // return (List<string>)returnValue.ReturnValue;
    // }

    // return new List<string>();
    // }

    // NOTE: Moved to AsyncDataProvider.
    // public static bool IsBackendAvailable()
    // {
    // return Frontend.RunPublicQuery(VdcQueryType.GetDomainList, new VdcQueryParametersBase()) != null ? true : false;
    // }

    public static int GetMinimalVmMemSize()
    {
        VdcQueryReturnValue returnValue =
                GetConfigFromCache(new GetConfigurationValueParameters(ConfigurationValues.VMMinMemorySizeInMB));

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            return (Integer) returnValue.getReturnValue();
        }

        return 1;
    }

    public static int GetMaximalVmMemSize32OS()
    {
        VdcQueryReturnValue returnValue =
                GetConfigFromCache(new GetConfigurationValueParameters(ConfigurationValues.VM32BitMaxMemorySizeInMB));

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            return (Integer) returnValue.getReturnValue();
        }

        return 262144;
    }

    public static int GetMaximalVmMemSize64OS()
    {
        VdcQueryReturnValue returnValue =
                GetConfigFromCache(new GetConfigurationValueParameters(ConfigurationValues.VM64BitMaxMemorySizeInMB));

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            return (Integer) returnValue.getReturnValue();
        }

        return 262144;
    }

    public static java.util.ArrayList<VmTemplate> GetTemplateList(Guid storagePoolId)
    {
        VdcQueryReturnValue returnValue =
                Frontend.RunQuery(VdcQueryType.GetVmTemplatesByStoragePoolId,
                        new GetVmTemplatesByStoragePoolIdParameters(storagePoolId));
        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            // var list = ((List<VmTemplate>)returnValue.ReturnValue)
            // .Where(a => a.status == VmTemplateStatus.OK)
            // .OrderBy(a => a.name)
            // .ToList();
            VmTemplate blankTemplate = new VmTemplate();
            java.util.ArrayList<VmTemplate> list = new java.util.ArrayList<VmTemplate>();
            for (VmTemplate template : (java.util.ArrayList<VmTemplate>) returnValue.getReturnValue())
            {
                if (template.getId().equals(NGuid.Empty))
                {
                    blankTemplate = template;
                }
                else if (template.getstatus() == VmTemplateStatus.OK)
                {
                    list.add(template);
                }
            }

            Collections.sort(list, new Linq.VmTemplateByNameComparer());
            list.add(0, blankTemplate);
            // VmTemplate blankTemplate = list.First(a => (Guid)a.vmt_guid == Guid.Empty);
            // list.Remove(blankTemplate);
            // list.Insert(0, blankTemplate);

            return list;
        }

        return new java.util.ArrayList<VmTemplate>();
    }

    public static VmTemplate GetTemplateByID(Guid templateGUID)
    {
        VdcQueryReturnValue returnValue =
                Frontend.RunQuery(VdcQueryType.GetVmTemplate, new GetVmTemplateParameters(templateGUID));

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            return (VmTemplate) returnValue.getReturnValue();
        }

        return null;
    }

    public static java.util.ArrayList<storage_domains> GetStorageDomainListByTemplate(Guid templateId)
    {
        VdcQueryReturnValue returnValue =
                Frontend.RunQuery(VdcQueryType.GetStorageDomainsByVmTemplateId,
                        new GetStorageDomainsByVmTemplateIdQueryParameters(templateId));

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            return (java.util.ArrayList<storage_domains>) returnValue.getReturnValue();
        }

        return new java.util.ArrayList<storage_domains>();
    }

    public static storage_domains GetIsoDomainByDataCenterId(Guid dataCenterId)
    {
        StoragePoolQueryParametersBase getIsoParams = new StoragePoolQueryParametersBase(dataCenterId);
        VdcQueryReturnValue returnValue =
                Frontend.RunQuery(VdcQueryType.GetStorageDomainsByStoragePoolId, getIsoParams);

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            java.util.ArrayList<storage_domains> storageDomains =
                    (java.util.ArrayList<storage_domains>) returnValue.getReturnValue();

            for (storage_domains domain : storageDomains)
            {
                if (domain.getstorage_domain_type() == StorageDomainType.ISO)
                {
                    return domain;
                }
            }
        }

        return null;
    }

    public static storage_domains GetExportDomainByDataCenterId(Guid dataCenterId)
    {
        VdcQueryReturnValue returnValue =
                Frontend.RunQuery(VdcQueryType.GetStorageDomainsByStoragePoolId,
                        new StoragePoolQueryParametersBase(dataCenterId));

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            java.util.ArrayList<storage_domains> storageDomains =
                    (java.util.ArrayList<storage_domains>) returnValue.getReturnValue();
            for (storage_domains domain : storageDomains)
            {
                if (domain.getstorage_domain_type() == StorageDomainType.ImportExport)
                {
                    return domain;
                }
            }
        }

        return null;
    }

    public static java.util.ArrayList<String> GetIrsImageList(Guid dataCenterId, boolean forceRefresh)
    {
        storage_domains isoDomain = GetIsoDomainByDataCenterId(dataCenterId);
        if (isoDomain != null)
        {
            return GetIsoListByIsoDomainId(isoDomain.getid(), forceRefresh);
        }

        return new java.util.ArrayList<String>();
    }

    public static java.util.ArrayList<String> GetIsoListByIsoDomainId(Guid isoDomainId, boolean forceRefresh)
    {
        GetAllIsoImagesListParameters param = new GetAllIsoImagesListParameters();
        param.setStorageDomainId(isoDomainId);
        param.setForceRefresh(forceRefresh);

        VdcQueryReturnValue returnValue = Frontend.RunQuery(VdcQueryType.GetAllIsoImagesList, param);
        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            java.util.ArrayList<RepoFileMetaData> listRepoFileList =
                    (java.util.ArrayList<RepoFileMetaData>) returnValue.getReturnValue();
            java.util.ArrayList<String> fileNameList = new java.util.ArrayList<String>();
            for (RepoFileMetaData RepoFileMetaData : listRepoFileList)
            {
                fileNameList.add(RepoFileMetaData.getRepoFileName());
            }

            Collections.sort(fileNameList);
            return fileNameList;
        }

        return new java.util.ArrayList<String>();
    }

    public static String GetDefaultExportPath()
    {
        VdcQueryReturnValue returnValue =
                GetConfigFromCache(new GetConfigurationValueParameters(ConfigurationValues.ExportDefaultPath));

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            return (String) returnValue.getReturnValue();
        }

        return "";
    }

    public static String GetDefaultImportPath()
    {
        VdcQueryReturnValue returnValue =
                GetConfigFromCache(new GetConfigurationValueParameters(ConfigurationValues.ImportDefaultPath));

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            return (String) returnValue.getReturnValue();
        }

        return "";
    }

    public static java.util.ArrayList<tags> GetAllTagsList()
    {
        VdcQueryReturnValue returnValue = Frontend.RunQuery(VdcQueryType.GetAllTags, new VdcQueryParametersBase());

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            return ((java.util.ArrayList<tags>) returnValue.getReturnValue());
        }

        return new java.util.ArrayList<tags>();
    }

    public static tags GetRootTag()
    {
        VdcQueryReturnValue returnValue = Frontend.RunQuery(VdcQueryType.GetRootTag, new VdcQueryParametersBase());
        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            tags tag = (tags) returnValue.getReturnValue();

            tags root =
                    new tags(tag.getdescription(),
                            tag.getparent_id(),
                            tag.getIsReadonly(),
                            tag.gettag_id(),
                            tag.gettag_name());
            if (tag.getChildren() != null)
            {
                fillTagsRecursive(root, tag.getChildren());
            }

            return root;
        }

        return new tags();
    }

    public static void fillTagsRecursive(tags tagToFill, java.util.List<tags> children)
    {
        java.util.ArrayList<tags> list = new java.util.ArrayList<tags>();

        for (tags tag : children)
        {
            // tags child = new tags(tag.description, tag.parent_id, tag.IsReadonly, tag.tag_id, tag.tag_name);
            if (tag.gettype() == TagsType.GeneralTag)
            {
                list.add(tag);
                if (tag.getChildren() != null)
                {
                    fillTagsRecursive(tag, tag.getChildren());
                }
            }

        }

        tagToFill.setChildren(list);
    }

    public static java.util.ArrayList<tags> GetAttachedTagsToVm(Guid id)
    {
        VdcQueryReturnValue returnValue =
                Frontend.RunQuery(VdcQueryType.GetTagsByVmId, new GetTagsByVmIdParameters(id.toString()));

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            // return ((List<tags>)returnValue.ReturnValue)
            // .Where(a => a.type == TagsType.GeneralTag)
            // .ToList();
            java.util.ArrayList<tags> ret = new java.util.ArrayList<tags>();
            for (tags tags : (java.util.ArrayList<tags>) returnValue.getReturnValue())
            {
                if (tags.gettype() == TagsType.GeneralTag)
                {
                    ret.add(tags);
                }
            }
            return ret;
        }

        return new java.util.ArrayList<tags>();
    }

    public static java.util.ArrayList<tags> GetAttachedTagsToHost(Guid id)
    {
        VdcQueryReturnValue returnValue =
                Frontend.RunQuery(VdcQueryType.GetTagsByVdsId, new GetTagsByVdsIdParameters(String.valueOf(id)));

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            // return ((List<tags>)returnValue.ReturnValue)
            // .Where(a => a.type == TagsType.GeneralTag)
            // .ToList();
            java.util.ArrayList<tags> ret = new java.util.ArrayList<tags>();
            for (tags tags : (java.util.ArrayList<tags>) returnValue.getReturnValue())
            {
                if (tags.gettype() == TagsType.GeneralTag)
                {
                    ret.add(tags);
                }
            }
            return ret;
        }

        return new java.util.ArrayList<tags>();
    }

    public static java.util.ArrayList<tags> GetAttachedTagsToUser(Guid id)
    {
        VdcQueryReturnValue returnValue =
                Frontend.RunQuery(VdcQueryType.GetTagsByUserId, new GetTagsByUserIdParameters(id.toString()));

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            // return ((List<tags>)returnValue.ReturnValue)
            // .Where(a => a.type == TagsType.GeneralTag)
            // .ToList();
            java.util.ArrayList<tags> ret = new java.util.ArrayList<tags>();
            for (tags tags : (java.util.ArrayList<tags>) returnValue.getReturnValue())
            {
                if (tags.gettype() == TagsType.GeneralTag)
                {
                    ret.add(tags);
                }
            }
            return ret;
        }

        return new java.util.ArrayList<tags>();
    }

    public static java.util.ArrayList<tags> GetAttachedTagsToUserGroup(Guid id)
    {
        VdcQueryReturnValue returnValue =
                Frontend.RunQuery(VdcQueryType.GetTagsByUserGroupId, new GetTagsByUserGroupIdParameters(id.toString()));

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            // return ((List<tags>)returnValue.ReturnValue)
            // .Where(a => a.type == TagsType.GeneralTag)
            // .ToList();
            java.util.ArrayList<tags> ret = new java.util.ArrayList<tags>();
            for (tags tags : (java.util.ArrayList<tags>) returnValue.getReturnValue())
            {
                if (tags.gettype() == TagsType.GeneralTag)
                {
                    ret.add(tags);
                }
            }
            return ret;
        }

        return new java.util.ArrayList<tags>();
    }

    public static java.util.ArrayList<tags> GetAttachedTagsBySubscriberId(Guid id, String event_name)
    {
        // return ((List<event_subscriber>)GetEventNotificationList(id)).Where(a => a.event_up_name == event_name &&
        // !string.IsNullOrEmpty(a.tag_name)).Select(b => new tags(b.event_up_name, null, false, i++,
        // b.tag_name)).ToList();
        java.util.ArrayList<tags> tags = new java.util.ArrayList<tags>();
        for (event_subscriber event_subscriber : GetEventNotificationList(id))
        {
            if (StringHelper.stringsEqual(event_subscriber.getevent_up_name(), event_name)
                    && !StringHelper.isNullOrEmpty(event_subscriber.gettag_name()))
            {
                tags.add(new tags(event_subscriber.getevent_up_name(),
                        null,
                        false,
                        new Guid(),
                        event_subscriber.gettag_name()));
            }
        }
        return tags;
    }

    public static java.util.ArrayList<String> GetHostTypeList(boolean showPowerclient)
    {
        // TODO: We can translate it here too
        java.util.ArrayList<String> ret = new java.util.ArrayList<String>();
        for (String s : EnumCompat.GetNames(VDSType.class))
        {
            if (StringHelper.stringsEqual(s, VDSType.PowerClient.toString()))
            {
                if (showPowerclient)
                {
                    ret.add(s);
                }
            }
            else
            {
                ret.add(s);
            }
        }
        return ret;
    }

    public static java.util.ArrayList<EventNotificationEntity> GetEventNotificationTypeList()
    {
        java.util.ArrayList<EventNotificationEntity> ret = new java.util.ArrayList<EventNotificationEntity>();
        // TODO: We can translate it here too
        for (EventNotificationEntity entity : EnumCompat.GetValues(EventNotificationEntity.class))
        {
            if (entity != EventNotificationEntity.UNKNOWN)
            {
                ret.add(entity);
            }
        }
        return ret;
    }

    public static java.util.Map<EventNotificationEntity, java.util.HashSet<AuditLogType>> GetAvailableNotificationEvents()
    {
        return VdcEventNotificationUtils.GetNotificationEvents();
    }

    public static java.util.ArrayList<bookmarks> GetBookmarkList()
    {
        VdcQueryReturnValue returnValue = Frontend.RunQuery(VdcQueryType.GetAllBookmarks, new VdcQueryParametersBase());

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            return (java.util.ArrayList<bookmarks>) returnValue.getReturnValue();
        }

        return new java.util.ArrayList<bookmarks>();
    }

    public static java.util.ArrayList<VmInterfaceType> GetNicTypeList(VmOsType osType, boolean hasDualmode)
    {
        java.util.ArrayList<VmInterfaceType> list = new java.util.ArrayList<VmInterfaceType>();
        for (VmInterfaceType item : EnumCompat.GetValues(VmInterfaceType.class))
        {
            list.add(item);
        }

        list.remove(VmInterfaceType.rtl8139_pv); // Dual mode NIC should be available only for existing NICs that have
                                                 // that type already
        if (IsWindowsOsType(osType))
        {
            list.remove(VmInterfaceType.e1000);
            if (osType == VmOsType.WindowsXP && hasDualmode)
            {
                list.add(VmInterfaceType.rtl8139_pv);
            }
        }

        return list;
    }

    public static VmInterfaceType GetDefaultNicType(VmOsType osType)
    {
        return VmInterfaceType.pv;
    }

    private static java.util.ArrayList<Integer> cachedNumOfMonitors;

    public static java.util.ArrayList<Integer> GetNumOfMonitorList()
    {
        if (cachedNumOfMonitors == null)
        {
            VdcQueryReturnValue returnValue =
                    GetConfigFromCache(new GetConfigurationValueParameters(ConfigurationValues.ValidNumOfMonitors));

            if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
            {
                // cachedNumOfMonitors = ((List<string>)returnValue.ReturnValue).Select(a =>
                // Convert.ToInt32(a)).ToList();
                java.util.ArrayList<Integer> nums = new java.util.ArrayList<Integer>();
                for (String num : (java.util.ArrayList<String>) returnValue.getReturnValue())
                {
                    nums.add(Integer.parseInt(num));
                }
                return nums;
            }
            else
            {
                cachedNumOfMonitors = new java.util.ArrayList<Integer>();
            }
        }

        return cachedNumOfMonitors;
    }

    public static boolean IsSupportCustomProperties(String version)
    {
        GetConfigurationValueParameters tempVar =
                new GetConfigurationValueParameters(ConfigurationValues.SupportCustomProperties);
        tempVar.setVersion(version);
        VdcQueryReturnValue returnValue = GetConfigFromCache(tempVar);

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            return (Boolean) returnValue.getReturnValue();
        }

        return true;
    }

    public static int GetMaxNumOfVmCpus(String version)
    {
        GetConfigurationValueParameters tempVar =
                new GetConfigurationValueParameters(ConfigurationValues.MaxNumOfVmCpus);
        tempVar.setVersion(version);
        VdcQueryReturnValue returnValue = GetConfigFromCache(tempVar);

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            return (Integer) returnValue.getReturnValue();
        }

        return 1;
    }

    public static String GetAuthenticationMethod()
    {
        VdcQueryReturnValue returnValue =
                GetConfigFromCache(new GetConfigurationValueParameters(ConfigurationValues.AuthenticationMethod));

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            return (String) returnValue.getReturnValue();
        }

        return "";
    }

    public static int GetMaxNumOfVmSockets(String version)
    {
        GetConfigurationValueParameters tempVar =
                new GetConfigurationValueParameters(ConfigurationValues.MaxNumOfVmSockets);
        tempVar.setVersion(version);
        VdcQueryReturnValue returnValue = GetConfigFromCache(tempVar);

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            return (Integer) returnValue.getReturnValue();
        }

        return 1;
    }

    public static int GetMaxNumOfCPUsPerSocket(String version)
    {
        GetConfigurationValueParameters tempVar =
                new GetConfigurationValueParameters(ConfigurationValues.MaxNumOfCpuPerSocket);
        tempVar.setVersion(version);
        VdcQueryReturnValue returnValue = GetConfigFromCache(tempVar);

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            return (Integer) returnValue.getReturnValue();
        }

        return 1;
    }

    private static java.util.HashMap<Version, java.util.ArrayList<ServerCpu>> cachedCPUs;

    public static java.util.ArrayList<ServerCpu> GetCPUList(Version Version)
    {
        if (Version != null
                && (cachedCPUs == null ? InitCachedCPUsDict() : true)
                && ((!cachedCPUs.containsKey(Version)) || ((cachedCPUs.containsKey(Version)) && cachedCPUs.get(Version) == null)))
        {
            VdcQueryReturnValue returnValue =
                    Frontend.RunQuery(VdcQueryType.GetAllServerCpuList, new GetAllServerCpuListParameters(Version));

            if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
            {
                cachedCPUs.put(Version, (java.util.ArrayList<ServerCpu>) returnValue.getReturnValue());
            }
        }

        return Version != null && cachedCPUs.containsKey(Version) ? ((cachedCPUs.get(Version)) != null) ? cachedCPUs.get(Version)
                : new java.util.ArrayList<ServerCpu>()
                : new java.util.ArrayList<ServerCpu>();
    }

    private static boolean InitCachedCPUsDict()
    {
        cachedCPUs = new java.util.HashMap<Version, java.util.ArrayList<ServerCpu>>();
        return cachedCPUs != null;
    }

    // /#537775, #622235
    public static void GetWipeAfterDeleteDefaultsByStorageType(StorageType storageType,
            EntityModel WipeAfterDeleteEntityModel,
            boolean IsThisNewCommand)
    {
        if (storageType == StorageType.NFS || storageType == StorageType.LOCALFS)
        {
            WipeAfterDeleteEntityModel.setIsChangable(false);
        }
        else
        {
            WipeAfterDeleteEntityModel.setIsChangable(true);
            if (IsThisNewCommand)
            {
                WipeAfterDeleteEntityModel.setEntity(GetSANWipeAfterDelete());
            }
        }
    }

    public static String[] GetADPathList()
    {
        VdcQueryReturnValue returnValue =
                GetConfigFromCache(new GetConfigurationValueParameters(ConfigurationValues.ComputerADPaths));

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            return ((String) returnValue.getReturnValue()).split("[;]", -1);
        }

        return new String[] {};
    }

    public static java.util.ArrayList<StorageType> GetStoragePoolTypeList()
    {
        return new java.util.ArrayList<StorageType>(java.util.Arrays.asList(new StorageType[] { StorageType.NFS,
                StorageType.ISCSI, StorageType.FCP, StorageType.LOCALFS }));
    }

    public static boolean IsVersionMatchStorageType(Version version, StorageType type)
    {
        return !(type == StorageType.LOCALFS && version.compareTo(new Version(2, 2)) <= 0);
    }

    public static String GetRpmVersionViaPublic()
    {
        VdcQueryReturnValue returnValue =
                Frontend.RunPublicQuery(VdcQueryType.GetConfigurationValue,
                        new GetConfigurationValueParameters(ConfigurationValues.ProductRPMVersion));

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            return (String) returnValue.getReturnValue();
        }

        return "";
    }

    /**
     * Used to retrieve versions for editing cluster
     *
     * @param vdsGroupId
     * @return
     */
    public static java.util.ArrayList<Version> GetClusterVersions(Guid vdsGroupId)
    {
        GetAvailableClusterVersionsParameters tempVar = new GetAvailableClusterVersionsParameters();
        tempVar.setVdsGroupId(vdsGroupId);
        VdcQueryReturnValue returnValue = Frontend.RunQuery(VdcQueryType.GetAvailableClusterVersions, tempVar);

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            return (java.util.ArrayList<Version>) returnValue.getReturnValue();
        }
        else
        {
            return new java.util.ArrayList<Version>();
        }
    }

    /**
     * Used to retrieve versions for creating cluster
     *
     * @param storagePoolId
     * @return
     */
    public static java.util.ArrayList<Version> GetDataCenterClusterVersions(NGuid storagePoolId)
    {
        GetAvailableClusterVersionsByStoragePoolParameters tempVar =
                new GetAvailableClusterVersionsByStoragePoolParameters();
        tempVar.setStoragePoolId(storagePoolId);
        VdcQueryReturnValue returnValue =
                Frontend.RunQuery(VdcQueryType.GetAvailableClusterVersionsByStoragePool, tempVar);

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            java.util.ArrayList<Version> list = (java.util.ArrayList<Version>) returnValue.getReturnValue();
            Collections.sort(list);
            return list;
        }

        return new java.util.ArrayList<Version>();
    }

    public static java.util.ArrayList<Version> GetDataCenterVersions(NGuid storagePoolId)
    {
        GetAvailableStoragePoolVersionsParameters tempVar = new GetAvailableStoragePoolVersionsParameters();
        tempVar.setStoragePoolId(storagePoolId);
        VdcQueryReturnValue returnValue = Frontend.RunQuery(VdcQueryType.GetAvailableStoragePoolVersions, tempVar);

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            // C# TO JAVA CONVERTER TODO TASK: There is no Java equivalent to LINQ queries:
            return Linq.OrderByDescending((java.util.ArrayList<Version>) returnValue.getReturnValue());
        }

        return new java.util.ArrayList<Version>();
    }

    public static int GetClusterServerMemoryOverCommit()
    {
        VdcQueryReturnValue returnValue =
                GetConfigFromCache(new GetConfigurationValueParameters(ConfigurationValues.MaxVdsMemOverCommitForServers));

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            return (Integer) returnValue.getReturnValue();
        }

        return 0;
    }

    public static int GetClusterDesktopMemoryOverCommit()
    {
        VdcQueryReturnValue returnValue =
                GetConfigFromCache(new GetConfigurationValueParameters(ConfigurationValues.MaxVdsMemOverCommit));

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            return (Integer) returnValue.getReturnValue();
        }

        return 0;
    }

    public static int GetClusterDefaultMemoryOverCommit()
    {
        return 100;
    }

    public static java.util.ArrayList<storage_pool> GetDataCenterList()
    {
        SearchParameters tempVar = new SearchParameters("DataCenter: sortby name", SearchType.StoragePool);
        tempVar.setMaxCount(SearchLimit);
        VdcQueryReturnValue returnValue = Frontend.RunQuery(VdcQueryType.Search, tempVar);

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            return Linq.<storage_pool> Cast((java.util.ArrayList<IVdcQueryable>) returnValue.getReturnValue());
            // return ((List<IVdcQueryable>)returnValue.ReturnValue)
            // .Cast<storage_pool>()
            // .ToList();
        }

        return new java.util.ArrayList<storage_pool>();
    }

    public static java.util.ArrayList<VmOsType> GetOSList()
    {
        return new java.util.ArrayList<VmOsType>(java.util.Arrays.asList(new VmOsType[] { VmOsType.Unassigned,
                VmOsType.RHEL6, VmOsType.RHEL6x64, VmOsType.RHEL5, VmOsType.RHEL5x64, VmOsType.RHEL4,
                VmOsType.RHEL4x64, VmOsType.RHEL3, VmOsType.RHEL3x64, VmOsType.OtherLinux, VmOsType.WindowsXP,
                VmOsType.Windows2003, VmOsType.Windows2003x64, VmOsType.Windows7, VmOsType.Windows7x64,
                VmOsType.Windows2008, VmOsType.Windows2008x64, VmOsType.Windows2008R2x64, VmOsType.Other }));
    }

    public static Iterable GetUsbPolicyList()
    {
        return EnumCompat.GetValues(UsbPolicy.class);
    }

    public static java.util.ArrayList<VDS> GetUpHostList()
    {
        VdcQueryReturnValue returnValue =
                Frontend.RunQuery(VdcQueryType.Search, new SearchParameters("Host: status=up", SearchType.VDS));

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            // return ((List<IVdcQueryable>)returnValue.ReturnValue).Cast<VDS>().ToList();
            return Linq.<VDS> Cast((java.util.ArrayList<IVdcQueryable>) returnValue.getReturnValue());
        }

        return new java.util.ArrayList<VDS>();
    }

    public static java.util.ArrayList<VDS> GetUpSpmsByStorage(Guid storageDomainId)
    {
        java.util.ArrayList<storage_domains> storageDomainWithPools = null;

        // get the storage domain instance - each one with a different storage pool:
        VdcQueryReturnValue returnValue =
                Frontend.RunQuery(VdcQueryType.GetStorageDomainListById,
                        new StorageDomainQueryParametersBase(storageDomainId));

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            storageDomainWithPools = (java.util.ArrayList<storage_domains>) returnValue.getReturnValue();
        }

        // filter only the storage domain instances that are active:
        // storageDomainWithPools =
        // storageDomainWithPools == null ?
        // null :
        // storageDomainWithPools.Where(a => a.status.HasValue && a.status.Value ==
        // StorageDomainStatus.Active).ToList();
        if (storageDomainWithPools != null)
        {
            java.util.ArrayList<storage_domains> list = new java.util.ArrayList<storage_domains>();

            for (storage_domains domain : storageDomainWithPools)
            {
                if (domain.getstatus() != null && (domain.getstatus() == StorageDomainStatus.Active))
                {
                    list.add(domain);
                }
            }
            storageDomainWithPools = list;
        }

        if (storageDomainWithPools != null && storageDomainWithPools.size() > 0)
        {
            // build search query according to storage pool names:
            StringBuilder sbSearch = new StringBuilder();
            sbSearch.append(StringFormat.format("Host: status=up and datacenter=%1$s", storageDomainWithPools.get(0)
                    .getstorage_pool_name()));

            for (int i = 1; i < storageDomainWithPools.size(); i++)
            {
                sbSearch.append(StringFormat.format(" or datacenter=%1$s", storageDomainWithPools.get(i)
                        .getstorage_pool_name()));
            }

            returnValue =
                    Frontend.RunQuery(VdcQueryType.Search, new SearchParameters(sbSearch.toString(), SearchType.VDS));

            if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
            {

                // return ((List<IVdcQueryable>)returnValue.ReturnValue).Cast<VDS>().Where(a => a.spm_status ==
                // VdsSpmStatus.SPM).ToList();
                java.util.ArrayList<VDS> list = new java.util.ArrayList<VDS>();
                for (IVdcQueryable a : (java.util.ArrayList<IVdcQueryable>) returnValue.getReturnValue())
                {
                    VDS vds = (VDS) a;
                    if (vds.getspm_status() == VdsSpmStatus.SPM)
                    {
                        list.add(vds);
                    }
                }
                return list;
            }
        }

        return new java.util.ArrayList<VDS>();
    }

    public static java.util.ArrayList<VDS> GetUpHostListByCluster(String cluster)
    {
        VdcQueryReturnValue returnValue =
                Frontend.RunQuery(VdcQueryType.Search, new SearchParameters("Host: cluster = " + cluster
                        + " and status = up", SearchType.VDS));

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            return Linq.<VDS> Cast((java.util.ArrayList<IVdcQueryable>) returnValue.getReturnValue());
        }

        return new java.util.ArrayList<VDS>();
    }

    public static java.util.ArrayList<VDS> GetHostListByDataCenter(String dataCenterName)
    {
        VdcQueryReturnValue returnValue =
                Frontend.RunQuery(VdcQueryType.Search, new SearchParameters("Host: datacenter = " + dataCenterName
                        + " sortby name", SearchType.VDS));

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            return Linq.<VDS> Cast((java.util.ArrayList<IVdcQueryable>) returnValue.getReturnValue());
        }

        return new java.util.ArrayList<VDS>();
    }

    public static java.util.ArrayList<VDS> GetHostListByCluster(String cluster)
    {
        VdcQueryReturnValue returnValue =
                Frontend.RunQuery(VdcQueryType.Search, new SearchParameters("Host: cluster = " + cluster
                        + " sortby name", SearchType.VDS));

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            // var list = ((List<IVdcQueryable>)returnValue.ReturnValue)
            // .Cast<VDS>()
            // .ToList();
            java.util.ArrayList<VDS> list =
                    Linq.<VDS> Cast((java.util.ArrayList<IVdcQueryable>) returnValue.getReturnValue());
            return list;
        }

        return new java.util.ArrayList<VDS>();
    }

    public static VDS GetHostById(Guid hostId)
    {
        VdcQueryReturnValue returnValue =
                Frontend.RunQuery(VdcQueryType.GetVdsByVdsId, new GetVdsByVdsIdParameters(hostId));

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            return (VDS) returnValue.getReturnValue();
        }

        return null;
    }

    public static java.util.ArrayList<VDS> GetHostList()
    {
        SearchParameters tempVar = new SearchParameters("Host:", SearchType.VDS);
        tempVar.setMaxCount(SearchLimit);
        VdcQueryReturnValue returnValue = Frontend.RunQuery(VdcQueryType.Search, tempVar);

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            // return ((List<IVdcQueryable>)returnValue.ReturnValue).Cast<VDS>().ToList();
            return Linq.<VDS> Cast((java.util.ArrayList<IVdcQueryable>) returnValue.getReturnValue());
        }

        return new java.util.ArrayList<VDS>();
    }

    public static java.util.ArrayList<VM> GetServerList()
    {
        VdcQueryReturnValue returnValue =
                Frontend.RunQuery(VdcQueryType.Search, new SearchParameters("Vms: status=up sortby cpu_usage desc",
                        SearchType.VM));

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            // return ((List<IVdcQueryable>)returnValue.ReturnValue).Cast<VM>().ToList();
            return Linq.<VM> Cast((java.util.ArrayList<IVdcQueryable>) returnValue.getReturnValue());
        }

        return new java.util.ArrayList<VM>();
    }

    // Assumption that we have only 1 data/export storage domain per pool otherwise review the code
    public static java.util.ArrayList<storage_domains> GetStorageDomainList(Guid storagePoolId)
    {
        VdcQueryReturnValue returnValue =
                Frontend.RunQuery(VdcQueryType.GetStorageDomainsByStoragePoolId,
                        new StoragePoolQueryParametersBase(storagePoolId));

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            return (java.util.ArrayList<storage_domains>) returnValue.getReturnValue();
        }

        return new java.util.ArrayList<storage_domains>();
    }

    public static java.util.ArrayList<storage_pool> GetDataCentersByStorageDomain(Guid storageDomainId)
    {
        VdcQueryReturnValue returnValue =
                Frontend.RunQuery(VdcQueryType.GetStoragePoolsByStorageDomainId,
                        new StorageDomainQueryParametersBase(storageDomainId));

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null
                && ((java.util.ArrayList<storage_pool>) returnValue.getReturnValue()).size() > 0)
        {
            return (java.util.ArrayList<storage_pool>) (returnValue.getReturnValue());
        }

        return new java.util.ArrayList<storage_pool>();
    }

    public static storage_pool GetFirstStoragePoolByStorageDomain(Guid storageDomainId)
    {
        java.util.ArrayList<storage_pool> storagePools = GetDataCentersByStorageDomain(storageDomainId);
        return storagePools.size() > 0 ? storagePools.get(0) : null;
    }

    public static java.util.ArrayList<storage_domains> GetDataDomainsListByDomain(Guid storageDomainId)
    {
        storage_pool pool = GetFirstStoragePoolByStorageDomain(storageDomainId);
        if (pool != null)
        {
            // return GetStorageDomainList(pool.id).Where(a => a.id != storageDomainId).ToList();
            java.util.ArrayList<storage_domains> list = new java.util.ArrayList<storage_domains>();
            for (storage_domains domain : GetStorageDomainList(pool.getId()))
            {
                if (!domain.getid().equals(storageDomainId))
                {
                    list.add(domain);
                }
            }
            return list;
        }

        return new java.util.ArrayList<storage_domains>();
    }

    public static java.util.ArrayList<VDSGroup> GetClusterListByStorageDomain(Guid storage_domain_id)
    {

        storage_pool pool = GetFirstStoragePoolByStorageDomain(storage_domain_id);
        if (pool != null)
        {
            return GetClusterList(pool.getId());
        }

        return new java.util.ArrayList<VDSGroup>();
    }

    public static storage_domains GetStorageDomainById(Guid storageDomainId)
    {
        VdcQueryReturnValue returnValue =
                Frontend.RunQuery(VdcQueryType.GetStorageDomainById,
                        new StorageDomainQueryParametersBase(storageDomainId));

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            return (storage_domains) (returnValue.getReturnValue());
        }

        return null;
    }

    public static storage_domains GetStorageDomainByDiskList(Iterable disksList)
    {
        java.util.ArrayList<DiskImage> diskImageList =
                disksList == null ? new java.util.ArrayList<DiskImage>() : Linq.<DiskImage> Cast(disksList);

        if (diskImageList.size() > 0)
        {
            DiskImage firstDisk = diskImageList.get(0);
            return GetStorageDomainByDisk(firstDisk);
        }

        return null;
    }

    public static storage_domains GetStorageDomainByDisk(DiskImage disk)
    {
        if (disk != null)
        {
            Guid selectedStorageDomainId =
                    (disk.getstorage_id() != null) ? disk.getstorage_id().getValue() : NGuid.Empty;
            if (!selectedStorageDomainId.equals(NGuid.Empty))
            {
                storage_domains selectedStorageDomain = GetStorageDomainById(selectedStorageDomainId);

                if (selectedStorageDomain != null)
                {
                    return selectedStorageDomain;
                }
            }
        }

        return null;
    }

    public static java.util.ArrayList<storage_domains> GetStorageDomainList()
    {
        SearchParameters searchParameters = new SearchParameters("Storage:", SearchType.StorageDomain);
        searchParameters.setMaxCount(SearchLimit);
        VdcQueryReturnValue returnValue = Frontend.RunQuery(VdcQueryType.Search, searchParameters);

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            // return ((List<IVdcQueryable>)returnValue.ReturnValue).Cast<storage_domains>().ToList();
            return Linq.<storage_domains> Cast((java.util.ArrayList<IVdcQueryable>) returnValue.getReturnValue());
        }

        return new java.util.ArrayList<storage_domains>();
    }

    public static java.util.ArrayList<storage_domains> GetISOStorageDomainList()
    {
        SearchParameters searchParams = new SearchParameters("Storage:", SearchType.StorageDomain);
        searchParams.setMaxCount(9999);

        VdcQueryReturnValue returnValue = Frontend.RunQuery(VdcQueryType.Search, searchParams);

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            // filter only the ISO storage domains into the return value:
            java.util.ArrayList<storage_domains> allStorageDomains =
                    (java.util.ArrayList<storage_domains>) returnValue.getReturnValue();
            java.util.ArrayList<storage_domains> isoStorageDomains = new java.util.ArrayList<storage_domains>();
            for (storage_domains storageDomain : allStorageDomains)
            {
                if (storageDomain.getstorage_domain_type() == StorageDomainType.ISO)
                {
                    isoStorageDomains.add(storageDomain);
                }
            }

            return isoStorageDomains;
        }

        return new java.util.ArrayList<storage_domains>();
    }

    public static boolean IsSSLEnabled()
    {
        VdcQueryReturnValue returnValue =
                GetConfigFromCache(new GetConfigurationValueParameters(ConfigurationValues.SSLEnabled));

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            return (Boolean) returnValue.getReturnValue();
        }

        return false;
    }

    public static boolean EnableSpiceRootCertificateValidation()
    {
        VdcQueryReturnValue returnValue =
                GetConfigFromCache(new GetConfigurationValueParameters(ConfigurationValues.EnableSpiceRootCertificateValidation));

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            return (Boolean) returnValue.getReturnValue();
        }

        return false;
    }

    public static String GetHostCertSubjectByHostID(Guid hostID)
    {
        VdcQueryReturnValue returnValue =
                Frontend.RunQuery(VdcQueryType.GetVdsCertificateSubjectByVdsId, new GetVdsByVdsIdParameters(hostID));

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            return (String) returnValue.getReturnValue();
        }

        return null;
    }

    public static String GetCACertificate()
    {
        VdcQueryReturnValue returnValue =
                Frontend.RunQuery(VdcQueryType.GetCACertificate, new VdcQueryParametersBase());

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            return (String) returnValue.getReturnValue();
        }

        return null;
    }

    public static String GetSpiceSecureChannelList()
    {
        VdcQueryReturnValue returnValue =
                GetConfigFromCache(new GetConfigurationValueParameters(ConfigurationValues.SpiceSecureChannels));

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            return (String) returnValue.getReturnValue();
        }

        return "";
    }

    public static String GetCipherSuite()
    {
        VdcQueryReturnValue returnValue =
                GetConfigFromCache(new GetConfigurationValueParameters(ConfigurationValues.CipherSuite));

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            return (String) returnValue.getReturnValue();
        }

        return "";
    }

    public static boolean IsUSBEnabledByDefault()
    {
        VdcQueryReturnValue returnValue =
                GetConfigFromCache(new GetConfigurationValueParameters(ConfigurationValues.EnableUSBAsDefault));

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            return (Boolean) returnValue.getReturnValue();
        }

        return false;
    }

    public static String GetSpiceToggleFullScreenKeys()
    {
        VdcQueryReturnValue returnValue =
                GetConfigFromCache(new GetConfigurationValueParameters(ConfigurationValues.SpiceToggleFullScreenKeys));

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            return (String) returnValue.getReturnValue();
        }

        return "shift+f11";
    }

    public static String GetSpiceReleaseCursorKeys()
    {
        VdcQueryReturnValue returnValue =
                DataProvider.GetConfigFromCache(new GetConfigurationValueParameters(ConfigurationValues.SpiceReleaseCursorKeys));

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            return (String) returnValue.getReturnValue();
        }

        return "shift+f12";
    }

    public static java.util.ArrayList<ActionGroup> GetRoleActionGroupsByRoleId(Guid roleId)
    {
        // TODO getRoleActionGroupsByRoleId instead
        VdcQueryReturnValue returnValue =
                Frontend.RunQuery(VdcQueryType.GetRoleActionGroupsByRoleId,
                        new MultilevelAdministrationByRoleIdParameters(roleId));

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            return (java.util.ArrayList<ActionGroup>) returnValue.getReturnValue();
        }
        return new java.util.ArrayList<ActionGroup>();
    }

    public static boolean IsLicenseValid(RefObject<java.util.List<String>> reasons)
    {
        // LicenseReturnValue returnValue = null;
        // try
        // {
        // returnValue = (LicenseReturnValue)Frontend.RunPublicQuery(VdcQueryType.IsLicenseValid,
        // new VdcQueryParametersBase());
        // }
        // catch
        // {
        // }

        // if (returnValue != null && returnValue.Succeeded && returnValue.Messages.Count == 0)
        // {
        // reasons = new List<string>();
        // return true;
        // }

        reasons.argvalue = null; // returnValue.Messages;
        return true;
    }

    // public static IDictionary<string, string> GetLicenseProperties()
    // {
    // var returnValue = Frontend.RunPublicQuery(VdcQueryType.GetLicenseProperties,
    // new VdcQueryParametersBase());

    // if (returnValue != null && returnValue.Succeeded && returnValue.ReturnValue != null)
    // {
    // return (IDictionary<string, string>)returnValue.ReturnValue;
    // }

    // return new Dictionary<string, string>();
    // }

    public static boolean IsLicenseHasDesktops()
    {
        // var dict = GetLicenseProperties();
        // return dict.ContainsKey("ProductTypeProperty") && dict["ProductTypeProperty"].Contains("Desktop");
        return true;
    }

    public static int GetClusterDefaultCpuOverCommit()
    {
        VdcQueryReturnValue returnValue =
                DataProvider.GetConfigFromCache(new GetConfigurationValueParameters(ConfigurationValues.CpuOverCommitDurationMinutes));

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            return (Integer) returnValue.getReturnValue();
        }

        return 0;
    }

    public static VdsSelectionAlgorithm GetDefaultVdsSelectionAlgorithm()
    {
        VdcQueryReturnValue returnValue =
                GetConfigFromCache(new GetConfigurationValueParameters(ConfigurationValues.VdsSelectionAlgorithm));

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            return VdsSelectionAlgorithm.valueOf((String) returnValue.getReturnValue());
        }

        return VdsSelectionAlgorithm.None;
    }

    public static int GetHighUtilizationForEvenDistribution()
    {
        VdcQueryReturnValue returnValue =
                GetConfigFromCache(new GetConfigurationValueParameters(ConfigurationValues.HighUtilizationForEvenlyDistribute));

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            return (Integer) returnValue.getReturnValue();
        }

        return 0;
    }

    public static int GetHighUtilizationForPowerSave()
    {
        VdcQueryReturnValue returnValue =
                GetConfigFromCache(new GetConfigurationValueParameters(ConfigurationValues.HighUtilizationForPowerSave));

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            return (Integer) returnValue.getReturnValue();
        }

        return 0;
    }

    public static int GetLowUtilizationForPowerSave()
    {
        VdcQueryReturnValue returnValue =
                GetConfigFromCache(new GetConfigurationValueParameters(ConfigurationValues.LowUtilizationForPowerSave));

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            return (Integer) returnValue.getReturnValue();
        }

        return 0;
    }

    public static int GetVcpuConsumptionPercentage()
    {
        VdcQueryReturnValue returnValue =
                GetConfigFromCache(new GetConfigurationValueParameters(ConfigurationValues.VcpuConsumptionPercentage));

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            return (Integer) returnValue.getReturnValue();
        }

        return 10;
    }

    public static java.util.ArrayList<DiskImageBase> GetDiskPresetList(VmType vmType, StorageType storageType)
    {
        // Get basic preset list from backend:
        VdcQueryReturnValue returnValue =
                Frontend.RunQuery(VdcQueryType.GetDiskConfigurationList, new VdcQueryParametersBase());

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            // List<DiskImageBase> presetList =
            // ((List<DiskImageBase>)returnValue.ReturnValue)
            // .Where(a => a.disk_type == DiskType.System || a.disk_type == DiskType.Data)
            // .ToList();
            java.util.ArrayList<DiskImageBase> list = new java.util.ArrayList<DiskImageBase>();
            DiskImageBase presetData = null;
            DiskImageBase presetSystem = null;
            for (DiskImageBase disk : (java.util.ArrayList<DiskImageBase>) returnValue.getReturnValue())
            {
                if (disk.getdisk_type() == DiskType.System || disk.getdisk_type() == DiskType.Data)
                {
                    list.add(disk);
                }
                if (disk.getdisk_type() == DiskType.System && presetSystem == null)
                {
                    presetSystem = disk;
                }
                else if (disk.getdisk_type() == DiskType.Data && presetData == null)
                {
                    presetData = disk;
                }
            }
            java.util.ArrayList<DiskImageBase> presetList = list;

            // DiskImageBase presetData = presetList.FirstOrDefault(a => a.disk_type == DiskType.Data);
            // DiskImageBase presetSystem = presetList.FirstOrDefault(a => a.disk_type == DiskType.System);

            // Set default volume type by vm type:
            // Set volume format by storage type and volume type:
            if (presetData != null)
            {
                presetData.setvolume_type(VolumeType.Preallocated);
                presetData.setvolume_format(GetDiskVolumeFormat(presetData.getvolume_type(), storageType));
            }
            if (presetSystem != null)
            {
                presetSystem.setvolume_type(vmType == VmType.Server ? VolumeType.Preallocated : VolumeType.Sparse);
                presetSystem.setvolume_format(GetDiskVolumeFormat(presetSystem.getvolume_type(), storageType));
            }

            return presetList;
        }

        return new java.util.ArrayList<DiskImageBase>();
    }

    public static VolumeFormat GetDiskVolumeFormat(VolumeType volumeType, StorageType storageType)
    {
        switch (storageType)
        {
        case NFS:
            return VolumeFormat.RAW;

        case ISCSI:
        case FCP:
            switch (volumeType)
            {
            case Sparse:
                return VolumeFormat.COW;

            case Preallocated:
                return VolumeFormat.RAW;

            default:
                return VolumeFormat.Unassigned;
            }

        case LOCALFS:
            return VolumeFormat.RAW;

        default:
            return VolumeFormat.Unassigned;
        }
    }

    public static java.util.ArrayList<VolumeType> GetVolumeTypeList()
    {
        return new java.util.ArrayList<VolumeType>(java.util.Arrays.asList(new VolumeType[] { VolumeType.Preallocated,
                VolumeType.Sparse }));
    }

    public static java.util.ArrayList<DiskInterface> GetDiskInterfaceList(VmOsType osType, Version Version)
    {
        return osType == VmOsType.WindowsXP && (Version == null || Version.compareTo(new Version("2.2")) < 0) ? new java.util.ArrayList<DiskInterface>(java.util.Arrays.asList(new DiskInterface[] { DiskInterface.IDE }))
                : new java.util.ArrayList<DiskInterface>(java.util.Arrays.asList(new DiskInterface[] {
                        DiskInterface.IDE, DiskInterface.VirtIO }));
    }

    public static DiskInterface GetDefaultDiskInterface(VmOsType osType, java.util.List<DiskImage> disks)
    {
        return osType == VmOsType.WindowsXP ? DiskInterface.IDE : disks != null && disks.size() > 0 ? disks.get(0)
                .getdisk_interface() : DiskInterface.VirtIO;
    }

    public static VolumeFormat[] GetVolumeFormatList()
    {
        return new VolumeFormat[] { VolumeFormat.COW, VolumeFormat.RAW };
    }

    public static java.util.ArrayList<VdsNetworkInterface> GetFreeBondList(Guid hostId)
    {
        VdcQueryReturnValue returnValue =
                Frontend.RunQuery(VdcQueryType.GetVdsFreeBondsByVdsId, new GetVdsByVdsIdParameters(hostId));

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            return (java.util.ArrayList<VdsNetworkInterface>) returnValue.getReturnValue();
        }

        return new java.util.ArrayList<VdsNetworkInterface>();
    }

    public static java.util.ArrayList<String> GetoVirtISOsList()
    {
        VdcQueryReturnValue returnValue = Frontend.RunQuery(VdcQueryType.GetoVirtISOs, new VdcQueryParametersBase());

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            return (java.util.ArrayList<String>) returnValue.getReturnValue();
        }

        return new java.util.ArrayList<String>();
    }

    public static boolean IsDataCenterNameUnique(String name)
    {
        VdcQueryReturnValue returnValue =
                Frontend.RunQuery(VdcQueryType.IsStoragePoolWithSameNameExist,
                        new IsStoragePoolWithSameNameExistParameters(name));

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            return !(Boolean) returnValue.getReturnValue();
        }

        return true;
    }

    public static boolean IsStorageDomainNameUnique(String name)
    {
        VdcQueryReturnValue returnValue =
                Frontend.RunQuery(VdcQueryType.Search,
                        new SearchParameters(StringFormat.format("Storage: name=%1$s", name), SearchType.StorageDomain));

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            java.util.ArrayList<storage_domains> list =
                    (java.util.ArrayList<storage_domains>) returnValue.getReturnValue();
            return list.isEmpty();
        }
        return true;
    }

    public static boolean IsClusterNameUnique(String name)
    {
        VdcQueryReturnValue returnValue =
                Frontend.RunQuery(VdcQueryType.IsVdsGroupWithSameNameExist,
                        new IsVdsGroupWithSameNameExistParameters(name));

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            return !(Boolean) returnValue.getReturnValue();
        }

        return true;
    }

    public static boolean IsHostNameUnique(String name)
    {
        VdcQueryReturnValue returnValue =
                Frontend.RunQuery(VdcQueryType.IsVdsWithSameNameExist, new IsVdsWithSameNameExistParameters(name));

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            return !(Boolean) returnValue.getReturnValue();
        }

        return true;
    }

    public static boolean IsVmNameUnique(String name)
    {
        VdcQueryReturnValue returnValue =
                Frontend.RunQuery(VdcQueryType.IsVmWithSameNameExist, new IsVmWithSameNameExistParameters(name));

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            return !(Boolean) returnValue.getReturnValue();
        }

        return true;
    }

    public static boolean IsTemplateNameUnique(String name)
    {
        VdcQueryReturnValue returnValue =
                Frontend.RunQuery(VdcQueryType.IsVmTemlateWithSameNameExist,
                        new IsVmTemlateWithSameNameExistParameters(name));

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            return !(Boolean) returnValue.getReturnValue();
        }

        return false;
    }

    public static boolean IsPoolNameUnique(String name)
    {
        VdcQueryReturnValue returnValue =
                Frontend.RunQuery(VdcQueryType.IsVmPoolWithSameNameExists,
                        new IsVmPoolWithSameNameExistsParameters(name));

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            return !(Boolean) returnValue.getReturnValue();
        }

        return false;
    }

    public static boolean IsRoleNameUnique(String name)
    {
        VdcQueryReturnValue returnValue =
                Frontend.RunQuery(VdcQueryType.GetAllRoles, new MultilevelAdministrationsQueriesParameters());

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            // return ((List<roles>)returnValue.ReturnValue).FirstOrDefault(a => String.Compare(a.name, name, true) ==
            // 0) == null;
            for (roles role : (java.util.ArrayList<roles>) returnValue.getReturnValue())
            {
                if (role.getname().compareToIgnoreCase(name) == 0)
                {
                    return false;
                }
            }
            return true;
        }

        return false;
    }

    public static java.util.ArrayList<String> GetPmTypeList(Version ClusterVersion)
    {
        java.util.ArrayList<String> list = new java.util.ArrayList<String>();

        GetConfigurationValueParameters tempVar = new GetConfigurationValueParameters(ConfigurationValues.VdsFenceType);
        tempVar.setVersion(ClusterVersion != null ? ClusterVersion.toString() : null);
        VdcQueryReturnValue returnValue = GetConfigFromCache(tempVar);

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            String[] array = ((String) returnValue.getReturnValue()).split("[,]", -1);
            for (String item : array)
            {
                list.add(item);
            }
        }

        return list;
    }

    private static java.util.HashMap<String, java.util.ArrayList<String>> cachedPmMap;

    public static java.util.ArrayList<String> GetPmOptions(String pmType)
    {
        if (cachedPmMap == null)
        {
            VdcQueryReturnValue returnValue =
                    Frontend.RunQuery(VdcQueryType.GetAgentFenceOptions, new VdcQueryParametersBase());

            if (returnValue != null && returnValue.getSucceeded())
            {
                cachedPmMap = new java.util.HashMap<String, java.util.ArrayList<String>>();

                java.util.HashMap<String, java.util.HashMap<String, Object>> dict =
                        (java.util.HashMap<String, java.util.HashMap<String, Object>>) returnValue.getReturnValue();
                for (java.util.Map.Entry<String, java.util.HashMap<String, Object>> pair : dict.entrySet())
                {
                    java.util.ArrayList<String> list = new java.util.ArrayList<String>();
                    for (java.util.Map.Entry<String, Object> p : pair.getValue().entrySet())
                    {
                        list.add(p.getKey());
                    }

                    cachedPmMap.put(pair.getKey(), list);
                }
            }
        }

        return cachedPmMap.get(pmType);
    }

    public static java.util.ArrayList<DiskImage> GetVmDiskList(Guid id)
    {
        VdcQueryReturnValue returnValue =
                Frontend.RunQuery(VdcQueryType.GetAllDisksByVmId, new GetAllDisksByVmIdParameters(id));

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            return (java.util.ArrayList<DiskImage>) returnValue.getReturnValue();
        }

        return new java.util.ArrayList<DiskImage>();
    }

    public static java.util.ArrayList<DiskImage> GetTemplateDiskList(Guid templateId)
    {
        VdcQueryReturnValue returnValue =
                Frontend.RunQuery(VdcQueryType.GetVmTemplatesDisks, new GetVmTemplatesDisksParameters(templateId));

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            return (java.util.ArrayList<DiskImage>) returnValue.getReturnValue();
        }

        return new java.util.ArrayList<DiskImage>();
    }

    public static java.util.ArrayList<VmNetworkInterface> GetVmNicList(Guid id)
    {
        VdcQueryReturnValue returnValue =
                Frontend.RunQuery(VdcQueryType.GetVmInterfacesByVmId, new GetVmByVmIdParameters(id));

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            return (java.util.ArrayList<VmNetworkInterface>) returnValue.getReturnValue();
        }

        return new java.util.ArrayList<VmNetworkInterface>();
    }

    public static java.util.ArrayList<DiskImage> GetSnapshotList(Guid id,
            String drive,
            RefObject<Guid> previewingImageId)
    {
        GetAllVmSnapshotsByDriveQueryReturnValue returnValue = null;
        try
        {
            returnValue =
                    (GetAllVmSnapshotsByDriveQueryReturnValue) Frontend.RunQuery(VdcQueryType.GetAllVmSnapshotsByDrive,
                            new GetAllVmSnapshotsByDriveParameters(id, drive));
        } catch (java.lang.Exception e)
        {
        }

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            previewingImageId.argvalue = returnValue.getTryingImage();
            return (java.util.ArrayList<DiskImage>) returnValue.getReturnValue();
        }

        previewingImageId.argvalue = NGuid.Empty;
        return new java.util.ArrayList<DiskImage>();
    }

    public static java.util.ArrayList<String> GetFloppyImageList(Guid dataCenterId, boolean forceRefresh)
    {
        storage_domains isoDomain = GetIsoDomainByDataCenterId(dataCenterId);

        if (isoDomain != null)
        {
            GetAllIsoImagesListParameters parameters = new GetAllIsoImagesListParameters();
            parameters.setStorageDomainId(isoDomain.getid());
            parameters.setForceRefresh(forceRefresh);

            VdcQueryReturnValue returnValue = Frontend.RunQuery(VdcQueryType.GetAllFloppyImagesList, parameters);

            if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
            {
                java.util.ArrayList<RepoFileMetaData> listRepoFileList =
                        (java.util.ArrayList<RepoFileMetaData>) returnValue.getReturnValue();
                java.util.ArrayList<String> fileNameList = new java.util.ArrayList<String>();
                for (RepoFileMetaData RepoFileMetaData : listRepoFileList)
                {
                    fileNameList.add(RepoFileMetaData.getRepoFileName());
                }

                Collections.sort(fileNameList);
                return fileNameList;
            }
        }

        return new java.util.ArrayList<String>();
    }

    public static storage_server_connections GetStorageConnectionById(String id)
    {
        VdcQueryReturnValue returnValue =
                Frontend.RunQuery(VdcQueryType.GetStorageServerConnectionById,
                        new StorageServerConnectionQueryParametersBase(id));

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            return (storage_server_connections) returnValue.getReturnValue();
        }

        return null;
    }

    public static int GetDefaultPoolLeasePeriod()
    {
        VdcQueryReturnValue returnValue =
                GetConfigFromCache(new GetConfigurationValueParameters(ConfigurationValues.VmPoolLeaseDays));

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            return (Integer) returnValue.getReturnValue();
        }

        return 0;
    }

    public static java.util.Date GetDefaultPoolLeaseStartTime()
    {
        VdcQueryReturnValue returnValue =
                GetConfigFromCache(new GetConfigurationValueParameters(ConfigurationValues.VmPoolLeaseStartTime));

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            return new java.util.Date(java.util.Date.parse((String) returnValue.getReturnValue()));
        }

        return new java.util.Date();
    }

    public static java.util.Date GetDefaultPoolLeaseEndTime()
    {
        VdcQueryReturnValue returnValue =
                GetConfigFromCache(new GetConfigurationValueParameters(ConfigurationValues.VmPoolLeaseEndTime));

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            return new java.util.Date(java.util.Date.parse((String) returnValue.getReturnValue()));
        }

        return new java.util.Date();
    }

    public static java.util.ArrayList<DbUser> GetUserList()
    {
        VdcQueryReturnValue returnValue =
                Frontend.RunQuery(VdcQueryType.Search, new SearchParameters("User:", SearchType.DBUser));

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            // return ((List<IVdcQueryable>)returnValue.ReturnValue).Cast<DbUser>().ToList();
            return Linq.<DbUser> Cast((java.util.ArrayList<IVdcQueryable>) returnValue.getReturnValue());
        }

        return new java.util.ArrayList<DbUser>();
    }

    public static VM GetVmById(Guid id)
    {
        VdcQueryReturnValue returnValue = Frontend.RunQuery(VdcQueryType.GetVmByVmId, new GetVmByVmIdParameters(id));

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            return (VM) returnValue.getReturnValue();
        }

        return null;
    }

    // NOTE: Moved to AsyncDataProvider.
    // public static vm_pools GetPoolById(Guid poolId)
    // {
    // VdcQueryReturnValue returnValue = Frontend.RunQuery(VdcQueryType.GetVmPoolById,
    // new GetVmPoolByIdParameters(poolId));

    // if (returnValue != null && returnValue.Succeeded && returnValue.ReturnValue != null)
    // {
    // return (vm_pools)returnValue.ReturnValue;
    // }

    // return null;
    // }

    public static VDSGroup GetClusterById(Guid id)
    {
        VdcQueryReturnValue returnValue =
                Frontend.RunQuery(VdcQueryType.GetVdsGroupById, new GetVdsGroupByIdParameters(id));

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            return (VDSGroup) returnValue.getReturnValue();
        }

        return null;
    }

    public static VDSGroup GetClusterByName(String name)
    {
        VdcQueryReturnValue returnValue =
                Frontend.RunQuery(VdcQueryType.Search,
                        new SearchParameters(StringFormat.format("Cluster: name=%1$s", name), SearchType.Cluster));

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            return Linq.FirstOrDefault(Linq.<VDSGroup> Cast((java.util.ArrayList<IVdcQueryable>) returnValue.getReturnValue()));
        }

        return null;
    }

    public static storage_pool GetDataCenterById(Guid id)
    {
        VdcQueryReturnValue returnValue =
                Frontend.RunQuery(VdcQueryType.GetStoragePoolById, new StoragePoolQueryParametersBase(id));

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            return (storage_pool) returnValue.getReturnValue();
        }

        return null;
    }

    public static storage_pool GetDataCenterByName(String name)
    {
        VdcQueryReturnValue returnValue =
                Frontend.RunQuery(VdcQueryType.Search,
                        new SearchParameters(StringFormat.format("DataCenter: name=%1$s", name), SearchType.StoragePool));

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            return Linq.FirstOrDefault(Linq.<storage_pool> Cast((java.util.ArrayList<IVdcQueryable>) returnValue.getReturnValue()));
        }

        return null;
    }

    public static java.util.ArrayList<VM> GetVmList(String poolName)
    {
        VdcQueryReturnValue returnValue =
                Frontend.RunQuery(VdcQueryType.Search, new SearchParameters("Vms: pool=" + poolName, SearchType.VM));

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            // return ((List<IVdcQueryable>)returnValue.ReturnValue).Cast<VM>().ToList();
            return Linq.<VM> Cast((java.util.ArrayList<IVdcQueryable>) returnValue.getReturnValue());
        }

        return new java.util.ArrayList<VM>();
    }

    public static VM GetAnyVm(String poolName)
    {
        java.util.ArrayList<VM> vms = GetVmList(poolName);
        return vms.size() > 0 ? vms.get(0) : null;
    }

    private static int _cachedSearchResultsLimit = -1;

    public static int GetSearchResultsLimit()
    {
        if (_cachedSearchResultsLimit == -1)
        {
            VdcQueryReturnValue returnValue =
                    GetConfigFromCache(new GetConfigurationValueParameters(ConfigurationValues.SearchResultsLimit));

            if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
            {
                _cachedSearchResultsLimit = (Integer) returnValue.getReturnValue();
            }
            else
            {
                _cachedSearchResultsLimit = 0;
            }
        }

        return _cachedSearchResultsLimit;
    }

    public static java.util.ArrayList<VdsNetworkInterface> GetInterfaceOptionsForEditNetwork(java.util.ArrayList<VdsNetworkInterface> interfaceList,
            VdsNetworkInterface originalInterface,
            network networkToEdit,
            Guid vdsID,
            RefObject<String> defaultInterfaceName)
    {
        java.util.ArrayList<VdsNetworkInterface> ifacesOptions = new java.util.ArrayList<VdsNetworkInterface>();
        for (VdsNetworkInterface i : interfaceList)
        {
            if (StringHelper.isNullOrEmpty(i.getNetworkName()) && StringHelper.isNullOrEmpty(i.getBondName()))
            {
                ifacesOptions.add(i);
            }
        }

        // List<Interface> ifacesOptions =

        // interfaceList
        // Filter only Interfaces that are not connected to a network:
        // .Where(a => string.IsNullOrEmpty(a.network_name))

        // Filter only Interfaces that are not slaves of a bond:
        // .Where(a => string.IsNullOrEmpty(a.bond_name))

        // .ToList();

        if (originalInterface.getVlanId() == null) // no vlan:
        {
            // Filter out the Interfaces that have child vlan Interfaces:
            // ifacesOptions.RemoveAll(
            // delegate(Interface i)
            // {
            // return InterfaceHasChildVlanInterfaces(vdsID, i);
            // });
            java.util.ArrayList<VdsNetworkInterface> ifacesOptionsTemp = new java.util.ArrayList<VdsNetworkInterface>();
            for (VdsNetworkInterface i : ifacesOptions)
            {
                if (!InterfaceHasChildVlanInterfaces(vdsID, i))
                {
                    ifacesOptionsTemp.add(i);
                }
            }
            ifacesOptions = ifacesOptionsTemp;

            if (originalInterface.getBonded() != null && originalInterface.getBonded())
            {
                // eth0 -- \
                // |---> bond0 -> <networkToEdit>
                // eth1 -- /
                // ---------------------------------------
                // - originalInterface: 'bond0'
                // --> We want to add 'eth0' and and 'eth1' as optional Interfaces
                // (note that choosing one of them will break the bond):
                // ifacesOptions.AddRange(interfaceList.Where(a => a.bond_name == originalInterface.name).ToList());
                for (VdsNetworkInterface i : interfaceList)
                {
                    if (StringHelper.stringsEqual(i.getBondName(), originalInterface.getName()))
                    {
                        ifacesOptions.add(i);
                    }
                }
            }

            // add the original interface as an option and set it as the default option:
            ifacesOptions.add(originalInterface);
            defaultInterfaceName.argvalue = originalInterface.getName();
        }

        else // vlan:
        {
            VdsNetworkInterface vlanParent = GetVlanParentInterface(vdsID, originalInterface);
            if (vlanParent != null && vlanParent.getBonded() != null && vlanParent.getBonded()
                    && !InterfaceHasSiblingVlanInterfaces(vdsID, originalInterface))
            {
                // eth0 -- \
                // |--- bond0 ---> bond0.3 -> <networkToEdit>
                // eth1 -- /
                // ---------------------------------------------------
                // - originalInterface: 'bond0.3'
                // - vlanParent: 'bond0'
                // - 'bond0.3' has no vlan siblings
                // --> We want to add 'eth0' and and 'eth1' as optional Interfaces.
                // (note that choosing one of them will break the bond):
                // ifacesOptions.AddRange(interfaceList.Where(a => a.bond_name == vlanParent.name).ToList());
                for (VdsNetworkInterface i : interfaceList)
                {
                    if (StringHelper.stringsEqual(i.getBondName(), vlanParent.getName()))
                    {
                        ifacesOptions.add(i);
                    }
                }
            }

            // the vlanParent should already be in ifacesOptions
            // (since it has no network_name or bond_name).

            defaultInterfaceName.argvalue = vlanParent.getName();
        }

        return ifacesOptions;
    }

    private static java.util.ArrayList<VdsNetworkInterface> GetAllHostInterfaces(Guid vdsID)
    {
        java.util.ArrayList<VdsNetworkInterface> interfaceList = new java.util.ArrayList<VdsNetworkInterface>();

        VdcQueryReturnValue retValue =
                Frontend.RunQuery(VdcQueryType.GetVdsInterfacesByVdsId, new GetVdsByVdsIdParameters(vdsID));

        if (retValue != null && retValue.getSucceeded())
        {
            interfaceList = (java.util.ArrayList<VdsNetworkInterface>) retValue.getReturnValue();
        }

        return interfaceList;
    }

    private static VdsNetworkInterface GetVlanParentInterface(Guid vdsID, VdsNetworkInterface iface)
    {
        VdcQueryReturnValue returnValue =
                Frontend.RunQuery(VdcQueryType.GetVlanParanet, new GetAllChildVlanInterfacesQueryParameters(vdsID,
                        iface));

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            return (VdsNetworkInterface) returnValue.getReturnValue();
        }

        return null;
    }

    private static boolean InterfaceHasChildVlanInterfaces(Guid vdsID, VdsNetworkInterface iface)
    {
        java.util.ArrayList<VdsNetworkInterface> childVlanInterfaces = new java.util.ArrayList<VdsNetworkInterface>();
        VdcQueryReturnValue returnValue =
                Frontend.RunQuery(VdcQueryType.GetAllChildVlanInterfaces,
                        new GetAllChildVlanInterfacesQueryParameters(vdsID, iface));

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            childVlanInterfaces = (java.util.ArrayList<VdsNetworkInterface>) (returnValue.getReturnValue());
        }

        if (childVlanInterfaces.size() > 0)
        {
            return true;
        }

        return false;
    }

    private static boolean InterfaceHasSiblingVlanInterfaces(Guid vdsID, VdsNetworkInterface iface)
    {
        java.util.ArrayList<VdsNetworkInterface> siblingVlanInterfaces = new java.util.ArrayList<VdsNetworkInterface>();
        VdcQueryReturnValue returnValue =
                Frontend.RunQuery(VdcQueryType.GetAllSiblingVlanInterfaces,
                        new GetAllChildVlanInterfacesQueryParameters(vdsID, iface));

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            siblingVlanInterfaces = (java.util.ArrayList<VdsNetworkInterface>) returnValue.getReturnValue();
        }

        if (siblingVlanInterfaces.size() > 0)
        {
            return true;
        }

        return false;
    }

    public static java.util.HashMap<String, Integer> GetSystemStatistics()
    {
        VdcQueryReturnValue returnValue =
                Frontend.RunQuery(VdcQueryType.GetSystemStatistics, new GetSystemStatisticsQueryParameters(-1));

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            return (java.util.HashMap<String, Integer>) returnValue.getReturnValue();
        }

        return new java.util.HashMap<String, Integer>();
    }

    public static int GetDiskMaxSize()
    {
        VdcQueryReturnValue returnValue =
                GetConfigFromCache(new GetConfigurationValueParameters(ConfigurationValues.MaxDiskSize));

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            return (Integer) returnValue.getReturnValue();
        }

        return 2047;
    }

    public static int GetMaxVmsInPool()
    {
        VdcQueryReturnValue returnValue =
                GetConfigFromCache(new GetConfigurationValueParameters(ConfigurationValues.MaxVmsInPool));

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            return (Integer) returnValue.getReturnValue();
        }

        return 1000;
    }

    public static java.util.ArrayList<VM> GetUserVmList(Guid userId, String groupNames)
    {
        VdcQueryReturnValue returnValue =
                Frontend.RunQuery(VdcQueryType.GetUserVmsByUserIdAndGroups, new GetUserVmsByUserIdAndGroupsParameters()); // user.UserId,
                                                                                                                          // user.GroupNames

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            return (java.util.ArrayList<VM>) returnValue.getReturnValue();
        }

        return new java.util.ArrayList<VM>();
    }

    public static String GetNewNicName(java.util.ArrayList<VmNetworkInterface> existingInterfaces)
    {
        int maxIfaceNumber = 0;
        if (existingInterfaces != null)
        {
            for (VmNetworkInterface iface : existingInterfaces)
            {
                int ifaceNumber = 0;
                // name of Interface is "eth<n>" (<n>: integer).
                RefObject<Integer> tempRef_ifaceNumber = new RefObject<Integer>(ifaceNumber);
                boolean tempVar =
                        iface.getName().length() > 3
                                && IntegerCompat.TryParse(iface.getName().substring(3), tempRef_ifaceNumber);
                ifaceNumber = tempRef_ifaceNumber.argvalue;
                if (tempVar)
                {
                    if (ifaceNumber > maxIfaceNumber)
                    {
                        maxIfaceNumber = ifaceNumber;
                    }
                }
            }
        }

        return "nic" + (maxIfaceNumber + 1);
    }

    private static java.util.HashMap<String, ResourceManager> _resourcesCache =
            new java.util.HashMap<String, ResourceManager>();

    public static String GetValueFromResource(String resourcePath, String key)
    {
        if (StringHelper.isNullOrEmpty(resourcePath) || StringHelper.isNullOrEmpty(key))
        {
            return "";
        }

        synchronized (_resourcesCache)
        {
            if (!_resourcesCache.containsKey(resourcePath))
            {
                _resourcesCache.put(resourcePath, new ResourceManager(resourcePath, Assembly.GetExecutingAssembly()));
            }
        }

        if (_resourcesCache.containsKey(resourcePath))
        {
            try
            {
                return _resourcesCache.get(resourcePath).GetString(key, CultureInfo.CurrentCulture);
            } catch (java.lang.Exception e)
            {
            }
        }

        return key;
    }

    public static String GetValueFromSpiceRedKeysResource(String key)
    {
        return GetValueFromResource("UICommon.Resources.SpiceRedKeys.SpiceRedKeys", key);
    }

    /**
     * Gets a value composed of "[string1]+[string2]+..." and returns "[string1Translated]+[string2Translated]+..."
     *
     * @param complexValue
     *            string in the form of "[string1]+[string2]+..."
     * @return string in the form of "[string1Translated]+[string2Translated]+..."
     */
    public static String GetComplexValueFromSpiceRedKeysResource(String complexValue)
    {
        if (StringHelper.isNullOrEmpty(complexValue))
        {
            return "";
        }
        java.util.ArrayList<String> values = new java.util.ArrayList<String>();

        for (String s : complexValue.split("[+]", -1))
        {
            values.add(GetValueFromSpiceRedKeysResource(s));
        }
        return StringHelper.join("+", values.toArray(new String[] {}));
        // return string.Join("+", complexValue.Split(new char[] { '+' }).Select(a =>
        // GetValueFromSpiceRedKeysResource(a)).ToArray());
    }

    public static int GetMaxVmPriority()
    {
        VdcQueryReturnValue returnValue =
                GetConfigFromCache(new GetConfigurationValueParameters(ConfigurationValues.VmPriorityMaxValue));

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            return (Integer) returnValue.getReturnValue();
        }

        return 100;
    }

    public static int RoundPriority(int priority)
    {
        int max = GetMaxVmPriority();
        int medium = max / 2;

        int[] levels = new int[] { 1, medium, max };

        for (int i = 0; i < levels.length; i++)
        {
            int lengthToLess = levels[i] - priority;
            int lengthToMore = levels[i + 1] - priority;

            if (lengthToMore < 0)
            {
                continue;
            }

            return Math.abs(lengthToLess) < lengthToMore ? levels[i] : levels[i + 1];
        }

        return 0;
    }

    private static int cacheDataCenterMaxNameLength = -1;

    public static int GetDataCenterMaxNameLength()
    {
        if (cacheDataCenterMaxNameLength == -1) // cache not initialized -> call Backend:
        {
            VdcQueryReturnValue returnValue =
                    GetConfigFromCache(new GetConfigurationValueParameters(ConfigurationValues.StoragePoolNameSizeLimit));

            if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
            {
                cacheDataCenterMaxNameLength = (Integer) returnValue.getReturnValue();
            }
            else
            {
                cacheDataCenterMaxNameLength = 1;
            }
        }

        return cacheDataCenterMaxNameLength;
    }

    private static int cacheStorageDomainMaxNameLength = -1;

    public static int GetStorageDomainMaxNameLength()
    {
        if (cacheStorageDomainMaxNameLength == -1) // cache not initialized -> call Backend:
        {
            VdcQueryReturnValue returnValue =
                    DataProvider.GetConfigFromCache(new GetConfigurationValueParameters(ConfigurationValues.StorageDomainNameSizeLimit));

            if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
            {
                cacheStorageDomainMaxNameLength = (Integer) returnValue.getReturnValue();
            }
            else
            {
                cacheStorageDomainMaxNameLength = 1;
            }
        }

        return cacheStorageDomainMaxNameLength;
    }

    // dictionary to hold cache of all config values (per version) queried by client, if the request for them succeeded.
    private static java.util.HashMap<java.util.Map.Entry<ConfigurationValues, String>, VdcQueryReturnValue> CachedConfigValues =
            new java.util.HashMap<java.util.Map.Entry<ConfigurationValues, String>, VdcQueryReturnValue>();

    // helper method to clear the config cache (currently used on each login)
    public static void ClearConfigCache()
    {
        if (CachedConfigValues != null)
        {
            CachedConfigValues.clear();
        }

        userActionGroups = null;
        cacheCustomProperties = null;
        windowsOsTypes = null;
        linuxOsTypes = null;
        x64OsTypes = null;
        hasAdminSystemPermission = null;
    }

    // method to get an item from config while caching it (config is not supposed to change during a session)
    public static VdcQueryReturnValue GetConfigFromCache(GetConfigurationValueParameters parameters)
    {
        java.util.Map.Entry<ConfigurationValues, String> config_key =
                new KeyValuePairCompat<ConfigurationValues, String>(parameters.getConfigValue(),
                        parameters.getVersion());

        // populate cache if not in cache already
        if (!CachedConfigValues.containsKey(config_key))
        {

            VdcQueryReturnValue returnValue = Frontend.RunQuery(VdcQueryType.GetConfigurationValue, parameters);

            // only put result in cache if query succeeded
            if (returnValue != null && returnValue.getSucceeded())
            {
                CachedConfigValues.put(config_key, returnValue);
            }
            // return actual return value on error
            else
            {
                return returnValue;
            }
        }
        // return value from cache (either it was in, or the query succeeded, and it is now in the cache
        return CachedConfigValues.get(config_key);
    }

    private static boolean GetSANWipeAfterDelete()
    {
        VdcQueryReturnValue returnValue =
                GetConfigFromCache(new GetConfigurationValueParameters(ConfigurationValues.SANWipeAfterDelete));

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            // use bool? to force conversion to Boolean in java (ReturnValue is Object in java code)b
            return (Boolean) returnValue.getReturnValue();
        }
        return false;
    }

    public static Guid GetEntityGuid(Object entity)
    {
        if (entity instanceof VM)
        {
            return ((VM) entity).getvm_guid();
        }
        else if (entity instanceof storage_pool)
        {
            return ((storage_pool) entity).getId();
        }
        else if (entity instanceof VDSGroup)
        {
            return ((VDSGroup) entity).getID();
        }
        else if (entity instanceof VDS)
        {
            return ((VDS) entity).getvds_id();
        }
        else if (entity instanceof storage_domains)
        {
            return ((storage_domains) entity).getid();
        }
        else if (entity instanceof VmTemplate)
        {
            return ((VmTemplate) entity).getId();
        }
        else if (entity instanceof vm_pools)
        {
            return ((vm_pools) entity).getvm_pool_id();
        }
        else if (entity instanceof DbUser)
        {
            return ((DbUser) entity).getuser_id();
        }
        return new Guid();
    }

    private static java.util.ArrayList<ActionGroup> userActionGroups = null;

    public static java.util.ArrayList<ActionGroup> GetUserActionGroups()
    {
        if (userActionGroups != null)
        {
            return userActionGroups;
        }
        UIQueryReturnValue ret = Frontend.RunUIQuery(UIQueryType.GetUserActionGroups, new UIQueryParametersBase());
        if (ret.getSucceeded() && ret.getReturnValue() != null)
        {
            userActionGroups = (java.util.ArrayList<ActionGroup>) ret.getReturnValue();
            return userActionGroups;
        }

        return new java.util.ArrayList<ActionGroup>();
    }

    private static java.util.HashMap<String, String> cacheCustomProperties;

    /**
     * Gets a dictionary in which the keys are the valid custom property keys allowed and the values are the valid
     * RegExp to validate the custom property values with.
     *
     * @return dictionary of valid keys and valid values' RegExps.
     */
    public static java.util.HashMap<String, String> GetCustomPropertiesList()
    {
        if (cacheCustomProperties != null)
        {
            return cacheCustomProperties;
        }
        VdcQueryReturnValue returnValue =
                Frontend.RunQuery(VdcQueryType.GetVmCustomProperties, new VdcQueryParametersBase());
        if (returnValue.getSucceeded() && returnValue.getReturnValue() != null
                && ((String) returnValue.getReturnValue()).equals("") == false)
        {
            String temp = (String) returnValue.getReturnValue();
            String[] tempArray = temp.split("[;]", -1);
            cacheCustomProperties = new java.util.HashMap<String, String>();
            for (String keyRegexpPair : tempArray)
            {
                String[] keyAndRegexp = keyRegexpPair.split("[=]", -1);
                String key = keyAndRegexp[0];
                String regexp = null;
                // if there is no "=", it means that there is no RegExp to
                // validate with, which means that all strings are valid.
                if (keyAndRegexp.length > 1)
                {
                    regexp = keyAndRegexp[1];
                }

                if (!cacheCustomProperties.containsKey(key))
                {
                    cacheCustomProperties.put(key, regexp);
                }
            }
            return cacheCustomProperties;
        }

        return new java.util.HashMap<String, String>();
    }

    private static java.util.ArrayList<VmOsType> windowsOsTypes;
    private static java.util.ArrayList<VmOsType> linuxOsTypes;
    private static java.util.ArrayList<VmOsType> x64OsTypes;

    public static java.util.ArrayList<VmOsType> GetWindowsOsTypes()
    {
        if (windowsOsTypes != null)
        {
            return windowsOsTypes;
        }

        // TODO: Uncomment once the gwt is using generic api instead of backend!

        // UIQueryReturnValue ret = Frontend.RunUIQuery(UIQueryType.GetWindowsOsTypes, new UIQueryParametersBase());
        // if (ret.Succeeded && ret.ReturnValue != null)
        // {
        // windowsOsTypes = (List<VmOsType>)ret.ReturnValue;
        // return windowsOsTypes;
        // }

        // return new List<VmOsType>();

        /***** TODO: remove once the gwt is using generic api instead of backend! *****/
        windowsOsTypes =
                new java.util.ArrayList<VmOsType>(java.util.Arrays.asList(new VmOsType[] { VmOsType.Windows2003,
                        VmOsType.Windows2003x64, VmOsType.Windows2008, VmOsType.Windows2008R2x64,
                        VmOsType.Windows2008x64, VmOsType.Windows7, VmOsType.Windows7x64, VmOsType.WindowsXP }));

        return windowsOsTypes;
        /*******************************************************************************/
    }

    public static boolean IsWindowsOsType(VmOsType osType)
    {
        if (GetWindowsOsTypes().contains(osType))
        {
            return true;
        }

        return false;
    }

    public static java.util.ArrayList<VmOsType> GetLinuxOsTypes()
    {
        if (linuxOsTypes != null)
        {
            return linuxOsTypes;
        }

        // TODO: Uncomment once the gwt is using generic api instead of backend!

        // UIQueryReturnValue ret = Frontend.RunUIQuery(UIQueryType.GetLinuxOsTypes, new UIQueryParametersBase());
        // if (ret.Succeeded && ret.ReturnValue != null)
        // {
        // linuxOsTypes = (List<VmOsType>)ret.ReturnValue;
        // return linuxOsTypes;
        // }

        // return new List<VmOsType>();

        /***** TODO: remove once the gwt is using generic api instead of backend! *****/
        linuxOsTypes =
                new java.util.ArrayList<VmOsType>(java.util.Arrays.asList(new VmOsType[] { VmOsType.OtherLinux,
                        VmOsType.RHEL3, VmOsType.RHEL3x64, VmOsType.RHEL4, VmOsType.RHEL4x64, VmOsType.RHEL5,
                        VmOsType.RHEL5x64, VmOsType.RHEL6, VmOsType.RHEL6x64 }));

        return linuxOsTypes;
        /*******************************************************************************/
    }

    public static boolean IsLinuxOsType(VmOsType osType)
    {
        if (GetLinuxOsTypes().contains(osType))
        {
            return true;
        }

        return false;
    }

    public static java.util.ArrayList<VmOsType> Get64bitOsTypes()
    {
        if (x64OsTypes != null)
        {
            return x64OsTypes;
        }

        // TODO: Uncomment once the gwt is using generic api instead of backend!

        // UIQueryReturnValue ret = Frontend.RunUIQuery(UIQueryType.Get64bitOsTypes, new UIQueryParametersBase());
        // if (ret.Succeeded && ret.ReturnValue != null)
        // {
        // x64OsTypes = (List<VmOsType>)ret.ReturnValue;
        // return x64OsTypes;
        // }

        // return new List<VmOsType>();

        /***** TODO: remove once the gwt is using generic api instead of backend! *****/
        x64OsTypes =
                new java.util.ArrayList<VmOsType>(java.util.Arrays.asList(new VmOsType[] { VmOsType.RHEL3x64,
                        VmOsType.RHEL4x64, VmOsType.RHEL5x64, VmOsType.RHEL6x64, VmOsType.Windows2003x64,
                        VmOsType.Windows2008R2x64, VmOsType.Windows2008x64, VmOsType.Windows7x64 }));

        return x64OsTypes;
        /*******************************************************************************/
    }

    public static boolean Is64bitOsType(VmOsType osType)
    {
        if (Get64bitOsTypes().contains(osType))
        {
            return true;
        }

        return false;
    }

    private static Boolean hasAdminSystemPermission = null;

    public static boolean HasAdminSystemPermission()
    {
        if (hasAdminSystemPermission == null)
        {
            VdcUser vdcUser = Frontend.getLoggedInUser();
            if (vdcUser == null)
            {
                hasAdminSystemPermission = false;
                return false;
            }
            VdcQueryReturnValue returnValue =
                    Frontend.RunQuery(VdcQueryType.GetPermissionsByAdElementId,
                            new MultilevelAdministrationByAdElementIdParameters(vdcUser.getUserId()));
            if (returnValue == null || !returnValue.getSucceeded())
            {
                hasAdminSystemPermission = false;
                return false;
            }
            java.util.ArrayList<permissions> permissions =
                    (java.util.ArrayList<permissions>) returnValue.getReturnValue();

            for (permissions permission : permissions)
            {
                if (permission.getObjectType() == VdcObjectType.System && permission.getRoleType() == RoleType.ADMIN)
                {
                    hasAdminSystemPermission = true;
                    return true;
                }
            }
            if (hasAdminSystemPermission == null)
            {
                hasAdminSystemPermission = false;
            }
        }
        return hasAdminSystemPermission;
    }

    public static VDS GetLocalStorageHost(String storagePoolName)
    {
        SearchParameters sp =
                new SearchParameters(StringFormat.format("hosts: datacenter=%1$s", storagePoolName), SearchType.VDS);

        VdcQueryReturnValue result = Frontend.RunQuery(VdcQueryType.Search, sp);
        if ((result != null) && result.getSucceeded() && (result.getReturnValue() != null))
        {
            for (IVdcQueryable res : (java.util.ArrayList<IVdcQueryable>) (result.getReturnValue()))
            {
                return (VDS) res;
            }
        }
        return null;
    }

    public static java.util.ArrayList<java.util.Map.Entry<String, EntityModel>> GetBondingOptionList(RefObject<java.util.Map.Entry<String, EntityModel>> defaultItem)
    {
        java.util.ArrayList<java.util.Map.Entry<String, EntityModel>> list =
                new java.util.ArrayList<java.util.Map.Entry<String, EntityModel>>();
        EntityModel entityModel = new EntityModel();
        entityModel.setEntity("(Mode 1) Active-Backup");
        list.add(new KeyValuePairCompat<String, EntityModel>("mode=1 miimon=100", entityModel));
        entityModel = new EntityModel();
        entityModel.setEntity("(Mode 2) Load balance (balance-xor)");
        list.add(new KeyValuePairCompat<String, EntityModel>("mode=2", entityModel));
        entityModel = new EntityModel();
        entityModel.setEntity("(Mode 4) Dynamic link aggregation (802.3ad)");
        defaultItem.argvalue = new KeyValuePairCompat<String, EntityModel>("mode=4", entityModel);
        list.add(defaultItem.argvalue);
        entityModel = new EntityModel();
        entityModel.setEntity("(Mode 5) Adaptive transmit load balancing (balance-tlb)");
        list.add(new KeyValuePairCompat<String, EntityModel>("mode=5", entityModel));
        entityModel = new EntityModel();
        entityModel.setEntity("");
        list.add(new KeyValuePairCompat<String, EntityModel>("custom", entityModel));
        return list;
    }

    public static String GetDefaultBondingOption()
    {
        return "mode=802.3ad miimon=150";
    }

    public static boolean IsDomainAlreadyExist(NGuid storagePoolId, String path, RefObject<String> storageName)
    {
        GetStorageDomainsByConnectionParameters param = new GetStorageDomainsByConnectionParameters();
        param.setConnection(path);
        if (storagePoolId != null)
        {
            param.setStoragePoolId(storagePoolId.getValue());
        }

        VdcQueryReturnValue returnValue = Frontend.RunQuery(VdcQueryType.GetStorageDomainsByConnection, param);

        if (returnValue != null && returnValue.getSucceeded())
        {
            java.util.List<storage_domains> storages =
                    (java.util.ArrayList<storage_domains>) returnValue.getReturnValue();
            if (storages.size() > 0)
            {
                storageName.argvalue = storages.get(0).getstorage_name();
                return true;
            }
        }

        storageName.argvalue = null;
        return false;
    }

    public static boolean IsDomainAlreadyExist(String path, RefObject<String> storageName)
    {
        return IsDomainAlreadyExist(null, path, storageName);
    }

}
