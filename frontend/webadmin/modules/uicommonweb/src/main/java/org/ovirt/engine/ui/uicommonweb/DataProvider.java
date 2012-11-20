package org.ovirt.engine.ui.uicommonweb;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.EventNotificationEntity;
import org.ovirt.engine.core.common.VdcEventNotificationUtils;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.DbUser;
import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.DiskImageBase;
import org.ovirt.engine.core.common.businessentities.DiskInterface;
import org.ovirt.engine.core.common.businessentities.IVdcQueryable;
import org.ovirt.engine.core.common.businessentities.Network;
import org.ovirt.engine.core.common.businessentities.NetworkView;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.Role;
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
import org.ovirt.engine.core.common.businessentities.VmType;
import org.ovirt.engine.core.common.businessentities.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.VolumeType;
import org.ovirt.engine.core.common.businessentities.bookmarks;
import org.ovirt.engine.core.common.businessentities.event_subscriber;
import org.ovirt.engine.core.common.businessentities.permissions;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.common.businessentities.storage_server_connections;
import org.ovirt.engine.core.common.businessentities.tags;
import org.ovirt.engine.core.common.businessentities.vm_pools;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.ConfigurationValues;
import org.ovirt.engine.core.common.queries.GetAllDisksByVmIdParameters;
import org.ovirt.engine.core.common.queries.GetAllNetworkQueryParamenters;
import org.ovirt.engine.core.common.queries.GetAllServerCpuListParameters;
import org.ovirt.engine.core.common.queries.GetAvailableClusterVersionsByStoragePoolParameters;
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
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.IntegerCompat;
import org.ovirt.engine.core.compat.KeyValuePairCompat;
import org.ovirt.engine.core.compat.NGuid;
import org.ovirt.engine.core.compat.RefObject;
import org.ovirt.engine.core.compat.StringFormat;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.tags.TagModel;
import org.ovirt.engine.ui.uicommonweb.models.users.UserPermissionModel;
import org.ovirt.engine.ui.uicompat.Assembly;
import org.ovirt.engine.ui.uicompat.ResourceManager;
import org.ovirt.engine.ui.uicompat.SpiceConstantsManager;

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
                GetConfigFromCache(new GetConfigurationValueParameters(ConfigurationValues.RhevhLocalFSPath, Config.DefaultConfigurationVersion));

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            return (String) returnValue.getReturnValue();
        }

        return ""; //$NON-NLS-1$
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
        return "^((?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)|(([a-zA-Z0-9]([a-zA-Z0-9\\-]{0,61}[a-zA-Z0-9])?\\.)+[a-zA-Z]{2,}))\\:/(.*?/|.*?\\\\)?([^\\./|^\\.\\\\]+)(?:\\.([^\\\\]*)|)$"; //$NON-NLS-1$
    }

    public static ArrayList<UserPermissionModel> GetUserPermissionMatrix(Guid userId)
    {
        // var roles = GetRoleList().ToDictionary(a => a.id);
        HashMap<Guid, Role> roles = new HashMap<Guid, Role>();
        for (Role role : GetRoleList())
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
        ArrayList<permissions> permissions = (ArrayList<permissions>) returnValue.getReturnValue();

        ArrayList<UserPermissionModel> userPermissions = new ArrayList<UserPermissionModel>();

        for (permissions permission : permissions)
        {
            UserPermissionModel userPermission = new UserPermissionModel();
            userPermission.setId(permission.getId());
            ListModel tempVar = new ListModel();
            tempVar.setSelectedItem(roles.get(permission.getrole_id()).getname());
            userPermission.setRole(tempVar);
            ArrayList<TagModel> tags = new ArrayList<TagModel>();
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

    public static ArrayList<event_subscriber> GetEventNotificationList(Guid userId)
    {
        VdcQueryReturnValue returnValue =
                Frontend.RunQuery(VdcQueryType.GetEventSubscribersBySubscriberId,
                        new GetEventSubscribersBySubscriberIdParameters(userId));
        if (returnValue == null || !returnValue.getSucceeded())
        {
            return new ArrayList<event_subscriber>();
        }

        return (ArrayList<event_subscriber>) returnValue.getReturnValue();
    }

    public static ArrayList<Network> GetNetworkList(Guid storagePoolId)
    {
        VdcQueryReturnValue returnValue =
                Frontend.RunQuery(VdcQueryType.GetAllNetworks, new GetAllNetworkQueryParamenters(storagePoolId));

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            return (ArrayList<Network>) returnValue.getReturnValue();
        }

        return new ArrayList<Network>();
    }

    public static ArrayList<Network> GetClusterNetworkList(Guid clusterId)
    {
        VdcQueryReturnValue returnValue =
                Frontend.RunQuery(VdcQueryType.GetAllNetworksByClusterId, new VdsGroupQueryParamenters(clusterId));

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            return (ArrayList<Network>) returnValue.getReturnValue();
        }

        return new ArrayList<Network>();
    }

    public static ArrayList<Role> GetRoleList()
    {
        VdcQueryReturnValue returnValue =
                Frontend.RunQuery(VdcQueryType.GetAllRoles, new MultilevelAdministrationsQueriesParameters());

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            return (ArrayList<Role>) returnValue.getReturnValue();
        }

        return new ArrayList<Role>();
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
                cachedDefaultTimeZone = ((Map.Entry<String, String>) returnValue.getReturnValue()).getKey();
            }
            else
            {
                cachedDefaultTimeZone = ""; //$NON-NLS-1$
            }
        }

        return cachedDefaultTimeZone;
    }

    private static HashMap<String, String> cachedTimeZones;

    public static HashMap<String, String> GetTimeZoneList()
    {
        if (cachedTimeZones == null)
        {
            VdcQueryReturnValue returnValue =
                    Frontend.RunQuery(VdcQueryType.GetTimeZones, new VdcQueryParametersBase());

            if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
            {
                cachedTimeZones = (HashMap<String, String>) returnValue.getReturnValue();
            }
            else
            {
                cachedTimeZones = new HashMap<String, String>();
            }
        }

        return cachedTimeZones;
    }

    public static ArrayList<VDSGroup> GetClusterList()
    {
        VdcQueryReturnValue returnValue = Frontend.RunQuery(VdcQueryType.GetAllVdsGroups, new VdcQueryParametersBase());
        if (returnValue != null && returnValue.getSucceeded())
        {
            ArrayList<VDSGroup> list = (ArrayList<VDSGroup>) returnValue.getReturnValue();
            if (list != null)
            {
                // return Linq.OrderBy<VDSGroup>( groups.OrderBy(a => a.name).ToList();
                Collections.sort(list, new Linq.VdsGroupByNameComparer());
                return list;
            }
        }

        return new ArrayList<VDSGroup>();
    }

    public static ArrayList<VDSGroup> GetClusterList(Guid storagePoolID)
    {
        VdcQueryReturnValue returnValue =
                Frontend.RunQuery(VdcQueryType.GetVdsGroupsByStoragePoolId,
                        new StoragePoolQueryParametersBase(storagePoolID));

        if (returnValue != null && returnValue.getSucceeded())
        {
            ArrayList<VDSGroup> list = (ArrayList<VDSGroup>) returnValue.getReturnValue();
            if (list != null)
            {
                // return groups.OrderBy(a => a.name).ToList();
                Collections.sort(list, new Linq.VdsGroupByNameComparer());
                return list;
            }
        }

        return new ArrayList<VDSGroup>();
    }

    public static ArrayList<String> GetDomainList(boolean filterInternalDomain)
    {
        GetDomainListParameters tempVar = new GetDomainListParameters();
        tempVar.setFilterInternalDomain(filterInternalDomain);
        VdcQueryReturnValue returnValue = Frontend.RunPublicQuery(VdcQueryType.GetDomainList, tempVar);

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            return (ArrayList<String>) returnValue.getReturnValue();
        }

        return new ArrayList<String>();
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
                GetConfigFromCache(new GetConfigurationValueParameters(ConfigurationValues.VMMinMemorySizeInMB, Config.DefaultConfigurationVersion));

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            return (Integer) returnValue.getReturnValue();
        }

        return 1;
    }

    public static int GetMaximalVmMemSize32OS()
    {
        VdcQueryReturnValue returnValue =
                GetConfigFromCache(new GetConfigurationValueParameters(ConfigurationValues.VM32BitMaxMemorySizeInMB, Config.DefaultConfigurationVersion));

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            return (Integer) returnValue.getReturnValue();
        }

        return 262144;
    }

    public static int GetMaximalVmMemSize64OS()
    {
        VdcQueryReturnValue returnValue =
                GetConfigFromCache(new GetConfigurationValueParameters(ConfigurationValues.VM64BitMaxMemorySizeInMB, Config.DefaultConfigurationVersion));

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            return (Integer) returnValue.getReturnValue();
        }

        return 262144;
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

    public static ArrayList<storage_domains> GetStorageDomainListByTemplate(Guid templateId)
    {
        VdcQueryReturnValue returnValue =
                Frontend.RunQuery(VdcQueryType.GetStorageDomainsByVmTemplateId,
                        new GetStorageDomainsByVmTemplateIdQueryParameters(templateId));

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            return (ArrayList<storage_domains>) returnValue.getReturnValue();
        }

        return new ArrayList<storage_domains>();
    }

    public static storage_domains GetIsoDomainByDataCenterId(Guid dataCenterId)
    {
        StoragePoolQueryParametersBase getIsoParams = new StoragePoolQueryParametersBase(dataCenterId);
        VdcQueryReturnValue returnValue =
                Frontend.RunQuery(VdcQueryType.GetStorageDomainsByStoragePoolId, getIsoParams);

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            ArrayList<storage_domains> storageDomains =
                    (ArrayList<storage_domains>) returnValue.getReturnValue();

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
            ArrayList<storage_domains> storageDomains =
                    (ArrayList<storage_domains>) returnValue.getReturnValue();
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

    public static String GetDefaultImportPath()
    {
        VdcQueryReturnValue returnValue =
                GetConfigFromCache(new GetConfigurationValueParameters(ConfigurationValues.ImportDefaultPath, Config.DefaultConfigurationVersion));

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            return (String) returnValue.getReturnValue();
        }

        return ""; //$NON-NLS-1$
    }

    public static ArrayList<tags> GetAllTagsList()
    {
        VdcQueryReturnValue returnValue = Frontend.RunQuery(VdcQueryType.GetAllTags, new VdcQueryParametersBase());

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            return ((ArrayList<tags>) returnValue.getReturnValue());
        }

        return new ArrayList<tags>();
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

    public static void fillTagsRecursive(tags tagToFill, List<tags> children)
    {
        ArrayList<tags> list = new ArrayList<tags>();

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

    public static ArrayList<tags> GetAttachedTagsToVm(Guid id)
    {
        VdcQueryReturnValue returnValue =
                Frontend.RunQuery(VdcQueryType.GetTagsByVmId, new GetTagsByVmIdParameters(id.toString()));

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            // return ((List<tags>)returnValue.ReturnValue)
            // .Where(a => a.type == TagsType.GeneralTag)
            // .ToList();
            ArrayList<tags> ret = new ArrayList<tags>();
            for (tags tags : (ArrayList<tags>) returnValue.getReturnValue())
            {
                if (tags.gettype() == TagsType.GeneralTag)
                {
                    ret.add(tags);
                }
            }
            return ret;
        }

        return new ArrayList<tags>();
    }

    public static ArrayList<tags> GetAttachedTagsToHost(Guid id)
    {
        VdcQueryReturnValue returnValue =
                Frontend.RunQuery(VdcQueryType.GetTagsByVdsId, new GetTagsByVdsIdParameters(String.valueOf(id)));

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            // return ((List<tags>)returnValue.ReturnValue)
            // .Where(a => a.type == TagsType.GeneralTag)
            // .ToList();
            ArrayList<tags> ret = new ArrayList<tags>();
            for (tags tags : (ArrayList<tags>) returnValue.getReturnValue())
            {
                if (tags.gettype() == TagsType.GeneralTag)
                {
                    ret.add(tags);
                }
            }
            return ret;
        }

        return new ArrayList<tags>();
    }

    public static ArrayList<tags> GetAttachedTagsToUser(Guid id)
    {
        VdcQueryReturnValue returnValue =
                Frontend.RunQuery(VdcQueryType.GetTagsByUserId, new GetTagsByUserIdParameters(id.toString()));

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            // return ((List<tags>)returnValue.ReturnValue)
            // .Where(a => a.type == TagsType.GeneralTag)
            // .ToList();
            ArrayList<tags> ret = new ArrayList<tags>();
            for (tags tags : (ArrayList<tags>) returnValue.getReturnValue())
            {
                if (tags.gettype() == TagsType.GeneralTag)
                {
                    ret.add(tags);
                }
            }
            return ret;
        }

        return new ArrayList<tags>();
    }

    public static ArrayList<tags> GetAttachedTagsToUserGroup(Guid id)
    {
        VdcQueryReturnValue returnValue =
                Frontend.RunQuery(VdcQueryType.GetTagsByUserGroupId, new GetTagsByUserGroupIdParameters(id.toString()));

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            // return ((List<tags>)returnValue.ReturnValue)
            // .Where(a => a.type == TagsType.GeneralTag)
            // .ToList();
            ArrayList<tags> ret = new ArrayList<tags>();
            for (tags tags : (ArrayList<tags>) returnValue.getReturnValue())
            {
                if (tags.gettype() == TagsType.GeneralTag)
                {
                    ret.add(tags);
                }
            }
            return ret;
        }

        return new ArrayList<tags>();
    }

    public static ArrayList<tags> GetAttachedTagsBySubscriberId(Guid id, String event_name)
    {
        // return ((List<event_subscriber>)GetEventNotificationList(id)).Where(a => a.event_up_name == event_name &&
        // !string.IsNullOrEmpty(a.tag_name)).Select(b => new tags(b.event_up_name, null, false, i++,
        // b.tag_name)).ToList();
        ArrayList<tags> tags = new ArrayList<tags>();
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

    public static ArrayList<String> GetHostTypeList(boolean showPowerclient)
    {
        // TODO: We can translate it here too
        ArrayList<String> ret = new ArrayList<String>();
        for (VDSType vdsType : VDSType.values())
        {
            if (VDSType.PowerClient == vdsType)
            {
                if (showPowerclient)
                {
                    ret.add(vdsType.name());
                }
            }
            else
            {
                ret.add(vdsType.name());
            }
        }
        return ret;
    }

    public static ArrayList<EventNotificationEntity> GetEventNotificationTypeList()
    {
        ArrayList<EventNotificationEntity> ret = new ArrayList<EventNotificationEntity>();
        for (EventNotificationEntity entity : EventNotificationEntity.values())
        {
            if (entity != EventNotificationEntity.UNKNOWN)
            {
                ret.add(entity);
            }
        }
        return ret;
    }

    public static Map<EventNotificationEntity, HashSet<AuditLogType>> GetAvailableNotificationEvents()
    {
        return VdcEventNotificationUtils.GetNotificationEvents();
    }

    public static ArrayList<bookmarks> GetBookmarkList()
    {
        VdcQueryReturnValue returnValue = Frontend.RunQuery(VdcQueryType.GetAllBookmarks, new VdcQueryParametersBase());

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            return (ArrayList<bookmarks>) returnValue.getReturnValue();
        }

        return new ArrayList<bookmarks>();
    }

    public static ArrayList<VmInterfaceType> GetNicTypeList(VmOsType osType, boolean hasDualmode)
    {
        ArrayList<VmInterfaceType> list = new ArrayList<VmInterfaceType>(Arrays.asList(VmInterfaceType.values()));

        list.remove(VmInterfaceType.rtl8139_pv); // Dual mode NIC should be available only for existing NICs that have
                                                 // that type already
        if (IsWindowsOsType(osType))
        {
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

    private static ArrayList<Integer> cachedNumOfMonitors;

    public static ArrayList<Integer> GetNumOfMonitorList()
    {
        if (cachedNumOfMonitors == null)
        {
            VdcQueryReturnValue returnValue =
                    GetConfigFromCache(new GetConfigurationValueParameters(ConfigurationValues.ValidNumOfMonitors, Config.DefaultConfigurationVersion));

            if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
            {
                // cachedNumOfMonitors = ((List<string>)returnValue.ReturnValue).Select(a =>
                // Convert.ToInt32(a)).ToList();
                ArrayList<Integer> nums = new ArrayList<Integer>();
                for (String num : (ArrayList<String>) returnValue.getReturnValue())
                {
                    nums.add(Integer.parseInt(num));
                }
                return nums;
            }
            else
            {
                cachedNumOfMonitors = new ArrayList<Integer>();
            }
        }

        return cachedNumOfMonitors;
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
                GetConfigFromCache(new GetConfigurationValueParameters(ConfigurationValues.AuthenticationMethod, Config.DefaultConfigurationVersion));

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            return (String) returnValue.getReturnValue();
        }

        return ""; //$NON-NLS-1$
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

    private static HashMap<Version, ArrayList<ServerCpu>> cachedCPUs;

    public static ArrayList<ServerCpu> GetCPUList(Version Version)
    {
        if (Version != null
                && (cachedCPUs == null ? InitCachedCPUsDict() : true)
                && ((!cachedCPUs.containsKey(Version)) || ((cachedCPUs.containsKey(Version)) && cachedCPUs.get(Version) == null)))
        {
            VdcQueryReturnValue returnValue =
                    Frontend.RunQuery(VdcQueryType.GetAllServerCpuList, new GetAllServerCpuListParameters(Version));

            if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
            {
                cachedCPUs.put(Version, (ArrayList<ServerCpu>) returnValue.getReturnValue());
            }
        }

        return Version != null && cachedCPUs.containsKey(Version) ? ((cachedCPUs.get(Version)) != null) ? cachedCPUs.get(Version)
                : new ArrayList<ServerCpu>()
                : new ArrayList<ServerCpu>();
    }

    private static boolean InitCachedCPUsDict()
    {
        cachedCPUs = new HashMap<Version, ArrayList<ServerCpu>>();
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
                GetConfigFromCache(new GetConfigurationValueParameters(ConfigurationValues.ComputerADPaths, Config.DefaultConfigurationVersion));

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            return ((String) returnValue.getReturnValue()).split("[;]", -1); //$NON-NLS-1$
        }

        return new String[] {};
    }

    public static ArrayList<StorageType> GetStoragePoolTypeList() {

        return new ArrayList<StorageType>(Arrays.asList(new StorageType[] {
            StorageType.NFS,
            StorageType.ISCSI,
            StorageType.FCP,
            StorageType.LOCALFS,
            StorageType.POSIXFS
        }));
    }

    public static boolean IsVersionMatchStorageType(Version version, StorageType type)
    {
        return !((type == StorageType.LOCALFS && version.compareTo(new Version(2, 2)) <= 0) || (type == StorageType.POSIXFS && version.compareTo(new Version(3,
                0)) <= 0));
    }

    public static String GetRpmVersionViaPublic()
    {
        VdcQueryReturnValue returnValue =
                Frontend.RunPublicQuery(VdcQueryType.GetConfigurationValue,
                        new GetConfigurationValueParameters(ConfigurationValues.ProductRPMVersion, Config.DefaultConfigurationVersion));

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            return (String) returnValue.getReturnValue();
        }

        return ""; //$NON-NLS-1$
    }

    /**
     * Used to retrieve versions for creating cluster
     *
     * @param storagePoolId
     * @return
     */
    public static ArrayList<Version> GetDataCenterClusterVersions(NGuid storagePoolId)
    {
        GetAvailableClusterVersionsByStoragePoolParameters tempVar =
                new GetAvailableClusterVersionsByStoragePoolParameters();
        tempVar.setStoragePoolId(storagePoolId);
        VdcQueryReturnValue returnValue =
                Frontend.RunQuery(VdcQueryType.GetAvailableClusterVersionsByStoragePool, tempVar);

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            ArrayList<Version> list = (ArrayList<Version>) returnValue.getReturnValue();
            Collections.sort(list);
            return list;
        }

        return new ArrayList<Version>();
    }

    public static ArrayList<Version> GetDataCenterVersions(NGuid storagePoolId)
    {
        GetAvailableStoragePoolVersionsParameters tempVar = new GetAvailableStoragePoolVersionsParameters();
        tempVar.setStoragePoolId(storagePoolId);
        VdcQueryReturnValue returnValue = Frontend.RunQuery(VdcQueryType.GetAvailableStoragePoolVersions, tempVar);

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            return Linq.OrderByDescending((ArrayList<Version>) returnValue.getReturnValue());
        }

        return new ArrayList<Version>();
    }

    public static int GetClusterServerMemoryOverCommit()
    {
        VdcQueryReturnValue returnValue =
                GetConfigFromCache(new GetConfigurationValueParameters(ConfigurationValues.MaxVdsMemOverCommitForServers, Config.DefaultConfigurationVersion));

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            return (Integer) returnValue.getReturnValue();
        }

        return 0;
    }

    public static int GetClusterDesktopMemoryOverCommit()
    {
        VdcQueryReturnValue returnValue =
                GetConfigFromCache(new GetConfigurationValueParameters(ConfigurationValues.MaxVdsMemOverCommit, Config.DefaultConfigurationVersion));

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

    public static ArrayList<storage_pool> GetDataCenterList()
    {
        SearchParameters tempVar = new SearchParameters("DataCenter: sortby name", SearchType.StoragePool); //$NON-NLS-1$
        tempVar.setMaxCount(SearchLimit);
        VdcQueryReturnValue returnValue = Frontend.RunQuery(VdcQueryType.Search, tempVar);

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            return Linq.<storage_pool> Cast((ArrayList<IVdcQueryable>) returnValue.getReturnValue());
            // return ((List<IVdcQueryable>)returnValue.ReturnValue)
            // .Cast<storage_pool>()
            // .ToList();
        }

        return new ArrayList<storage_pool>();
    }

    public static Iterable GetUsbPolicyList()
    {
        return Arrays.asList(UsbPolicy.values());
    }

    public static ArrayList<VDS> GetUpHostList()
    {
        VdcQueryReturnValue returnValue =
                Frontend.RunQuery(VdcQueryType.Search, new SearchParameters("Host: status=up", SearchType.VDS)); //$NON-NLS-1$

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            // return ((List<IVdcQueryable>)returnValue.ReturnValue).Cast<VDS>().ToList();
            return Linq.<VDS> Cast((ArrayList<IVdcQueryable>) returnValue.getReturnValue());
        }

        return new ArrayList<VDS>();
    }

    public static ArrayList<VDS> GetUpSpmsByStorage(Guid storageDomainId)
    {
        ArrayList<storage_domains> storageDomainWithPools = null;

        // get the storage domain instance - each one with a different storage pool:
        VdcQueryReturnValue returnValue =
                Frontend.RunQuery(VdcQueryType.GetStorageDomainListById,
                        new StorageDomainQueryParametersBase(storageDomainId));

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            storageDomainWithPools = (ArrayList<storage_domains>) returnValue.getReturnValue();
        }

        // filter only the storage domain instances that are active:
        // storageDomainWithPools =
        // storageDomainWithPools == null ?
        // null :
        // storageDomainWithPools.Where(a => a.status.HasValue && a.status.Value ==
        // StorageDomainStatus.Active).ToList();
        if (storageDomainWithPools != null)
        {
            ArrayList<storage_domains> list = new ArrayList<storage_domains>();

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
            sbSearch.append(StringFormat.format("Host: status=up and datacenter=%1$s", storageDomainWithPools.get(0) //$NON-NLS-1$
                    .getstorage_pool_name()));

            for (int i = 1; i < storageDomainWithPools.size(); i++)
            {
                sbSearch.append(StringFormat.format(" or datacenter=%1$s", storageDomainWithPools.get(i) //$NON-NLS-1$
                        .getstorage_pool_name()));
            }

            returnValue =
                    Frontend.RunQuery(VdcQueryType.Search, new SearchParameters(sbSearch.toString(), SearchType.VDS));

            if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
            {

                // return ((List<IVdcQueryable>)returnValue.ReturnValue).Cast<VDS>().Where(a => a.spm_status ==
                // VdsSpmStatus.SPM).ToList();
                ArrayList<VDS> list = new ArrayList<VDS>();
                for (IVdcQueryable a : (ArrayList<IVdcQueryable>) returnValue.getReturnValue())
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

        return new ArrayList<VDS>();
    }

    public static ArrayList<VDS> GetUpHostListByCluster(String cluster)
    {
        VdcQueryReturnValue returnValue =
                Frontend.RunQuery(VdcQueryType.Search, new SearchParameters("Host: cluster = " + cluster //$NON-NLS-1$
                        + " and status = up", SearchType.VDS)); //$NON-NLS-1$

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            return Linq.<VDS> Cast((ArrayList<IVdcQueryable>) returnValue.getReturnValue());
        }

        return new ArrayList<VDS>();
    }

    public static ArrayList<VDS> GetHostListByDataCenter(String dataCenterName)
    {
        VdcQueryReturnValue returnValue =
                Frontend.RunQuery(VdcQueryType.Search, new SearchParameters("Host: datacenter = " + dataCenterName //$NON-NLS-1$
                        + " sortby name", SearchType.VDS)); //$NON-NLS-1$

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            return Linq.<VDS> Cast((ArrayList<IVdcQueryable>) returnValue.getReturnValue());
        }

        return new ArrayList<VDS>();
    }

    public static ArrayList<VDS> GetHostListByCluster(String cluster)
    {
        VdcQueryReturnValue returnValue =
                Frontend.RunQuery(VdcQueryType.Search, new SearchParameters("Host: cluster = " + cluster //$NON-NLS-1$
                        + " sortby name", SearchType.VDS)); //$NON-NLS-1$

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            // var list = ((List<IVdcQueryable>)returnValue.ReturnValue)
            // .Cast<VDS>()
            // .ToList();
            ArrayList<VDS> list =
                    Linq.<VDS> Cast((ArrayList<IVdcQueryable>) returnValue.getReturnValue());
            return list;
        }

        return new ArrayList<VDS>();
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

    public static ArrayList<VDS> GetHostList()
    {
        SearchParameters tempVar = new SearchParameters("Host:", SearchType.VDS); //$NON-NLS-1$
        tempVar.setMaxCount(SearchLimit);
        VdcQueryReturnValue returnValue = Frontend.RunQuery(VdcQueryType.Search, tempVar);

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            // return ((List<IVdcQueryable>)returnValue.ReturnValue).Cast<VDS>().ToList();
            return Linq.<VDS> Cast((ArrayList<IVdcQueryable>) returnValue.getReturnValue());
        }

        return new ArrayList<VDS>();
    }

    public static ArrayList<VM> GetServerList()
    {
        VdcQueryReturnValue returnValue =
                Frontend.RunQuery(VdcQueryType.Search, new SearchParameters("Vms: status=up sortby cpu_usage desc", //$NON-NLS-1$
                        SearchType.VM));

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            // return ((List<IVdcQueryable>)returnValue.ReturnValue).Cast<VM>().ToList();
            return Linq.<VM> Cast((ArrayList<IVdcQueryable>) returnValue.getReturnValue());
        }

        return new ArrayList<VM>();
    }

    // Assumption that we have only 1 data/export storage domain per pool otherwise review the code
    public static ArrayList<storage_domains> GetStorageDomainList(Guid storagePoolId)
    {
        VdcQueryReturnValue returnValue =
                Frontend.RunQuery(VdcQueryType.GetStorageDomainsByStoragePoolId,
                        new StoragePoolQueryParametersBase(storagePoolId));

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            return (ArrayList<storage_domains>) returnValue.getReturnValue();
        }

        return new ArrayList<storage_domains>();
    }

    public static ArrayList<storage_pool> GetDataCentersByStorageDomain(Guid storageDomainId)
    {
        VdcQueryReturnValue returnValue =
                Frontend.RunQuery(VdcQueryType.GetStoragePoolsByStorageDomainId,
                        new StorageDomainQueryParametersBase(storageDomainId));

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null
                && ((ArrayList<storage_pool>) returnValue.getReturnValue()).size() > 0)
        {
            return (ArrayList<storage_pool>) (returnValue.getReturnValue());
        }

        return new ArrayList<storage_pool>();
    }

    public static storage_pool GetFirstStoragePoolByStorageDomain(Guid storageDomainId)
    {
        ArrayList<storage_pool> storagePools = GetDataCentersByStorageDomain(storageDomainId);
        return storagePools.size() > 0 ? storagePools.get(0) : null;
    }

    public static ArrayList<storage_domains> GetDataDomainsListByDomain(Guid storageDomainId)
    {
        storage_pool pool = GetFirstStoragePoolByStorageDomain(storageDomainId);
        if (pool != null)
        {
            // return GetStorageDomainList(pool.id).Where(a => a.id != storageDomainId).ToList();
            ArrayList<storage_domains> list = new ArrayList<storage_domains>();
            for (storage_domains domain : GetStorageDomainList(pool.getId()))
            {
                if (!domain.getId().equals(storageDomainId))
                {
                    list.add(domain);
                }
            }
            return list;
        }

        return new ArrayList<storage_domains>();
    }

    public static ArrayList<VDSGroup> GetClusterListByStorageDomain(Guid storage_domain_id)
    {

        storage_pool pool = GetFirstStoragePoolByStorageDomain(storage_domain_id);
        if (pool != null)
        {
            return GetClusterList(pool.getId());
        }

        return new ArrayList<VDSGroup>();
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
        ArrayList<DiskImage> diskImageList =
                disksList == null ? new ArrayList<DiskImage>() : Linq.<DiskImage> Cast(disksList);

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
                    (disk.getstorage_ids() != null && disk.getstorage_ids().size() > 0) ? disk.getstorage_ids().get(0)
                            : NGuid.Empty;
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

    public static ArrayList<storage_domains> GetStorageDomainList()
    {
        SearchParameters searchParameters = new SearchParameters("Storage:", SearchType.StorageDomain); //$NON-NLS-1$
        searchParameters.setMaxCount(SearchLimit);
        VdcQueryReturnValue returnValue = Frontend.RunQuery(VdcQueryType.Search, searchParameters);

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            // return ((List<IVdcQueryable>)returnValue.ReturnValue).Cast<storage_domains>().ToList();
            return Linq.<storage_domains> Cast((ArrayList<IVdcQueryable>) returnValue.getReturnValue());
        }

        return new ArrayList<storage_domains>();
    }

    public static ArrayList<storage_domains> GetISOStorageDomainList()
    {
        SearchParameters searchParams = new SearchParameters("Storage:", SearchType.StorageDomain); //$NON-NLS-1$
        searchParams.setMaxCount(9999);

        VdcQueryReturnValue returnValue = Frontend.RunQuery(VdcQueryType.Search, searchParams);

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            // filter only the ISO storage domains into the return value:
            ArrayList<storage_domains> allStorageDomains =
                    (ArrayList<storage_domains>) returnValue.getReturnValue();
            ArrayList<storage_domains> isoStorageDomains = new ArrayList<storage_domains>();
            for (storage_domains storageDomain : allStorageDomains)
            {
                if (storageDomain.getstorage_domain_type() == StorageDomainType.ISO)
                {
                    isoStorageDomains.add(storageDomain);
                }
            }

            return isoStorageDomains;
        }

        return new ArrayList<storage_domains>();
    }

    public static boolean IsSSLEnabled()
    {
        VdcQueryReturnValue returnValue =
                GetConfigFromCache(new GetConfigurationValueParameters(ConfigurationValues.SSLEnabled, Config.DefaultConfigurationVersion));

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            return (Boolean) returnValue.getReturnValue();
        }

        return false;
    }

    public static boolean EnableSpiceRootCertificateValidation()
    {
        VdcQueryReturnValue returnValue =
                GetConfigFromCache(new GetConfigurationValueParameters(ConfigurationValues.EnableSpiceRootCertificateValidation, Config.DefaultConfigurationVersion));

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            return (Boolean) returnValue.getReturnValue();
        }

        return false;
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
                GetConfigFromCache(new GetConfigurationValueParameters(ConfigurationValues.SpiceSecureChannels, Config.DefaultConfigurationVersion));

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            return (String) returnValue.getReturnValue();
        }

        return ""; //$NON-NLS-1$
    }

    public static String GetCipherSuite()
    {
        VdcQueryReturnValue returnValue =
                GetConfigFromCache(new GetConfigurationValueParameters(ConfigurationValues.CipherSuite, Config.DefaultConfigurationVersion));

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            return (String) returnValue.getReturnValue();
        }

        return ""; //$NON-NLS-1$
    }

    public static boolean IsUSBEnabledByDefault()
    {
        VdcQueryReturnValue returnValue =
                GetConfigFromCache(new GetConfigurationValueParameters(ConfigurationValues.EnableUSBAsDefault, Config.DefaultConfigurationVersion));

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            return (Boolean) returnValue.getReturnValue();
        }

        return false;
    }

    public static String GetSpiceToggleFullScreenKeys()
    {
        VdcQueryReturnValue returnValue =
                GetConfigFromCache(new GetConfigurationValueParameters(ConfigurationValues.SpiceToggleFullScreenKeys, Config.DefaultConfigurationVersion));

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            return (String) returnValue.getReturnValue();
        }

        return "shift+f11"; //$NON-NLS-1$
    }

    public static String GetSpiceReleaseCursorKeys()
    {
        VdcQueryReturnValue returnValue =
                DataProvider.GetConfigFromCache(new GetConfigurationValueParameters(ConfigurationValues.SpiceReleaseCursorKeys, Config.DefaultConfigurationVersion));

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            return (String) returnValue.getReturnValue();
        }

        return "shift+f12"; //$NON-NLS-1$
    }

    public static ArrayList<ActionGroup> GetRoleActionGroupsByRoleId(Guid roleId)
    {
        // TODO getRoleActionGroupsByRoleId instead
        VdcQueryReturnValue returnValue =
                Frontend.RunQuery(VdcQueryType.GetRoleActionGroupsByRoleId,
                        new MultilevelAdministrationByRoleIdParameters(roleId));

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            return (ArrayList<ActionGroup>) returnValue.getReturnValue();
        }
        return new ArrayList<ActionGroup>();
    }

    public static boolean IsLicenseValid(RefObject<List<String>> reasons)
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
                DataProvider.GetConfigFromCache(new GetConfigurationValueParameters(ConfigurationValues.CpuOverCommitDurationMinutes, Config.DefaultConfigurationVersion));

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            return (Integer) returnValue.getReturnValue();
        }

        return 0;
    }

    public static VdsSelectionAlgorithm GetDefaultVdsSelectionAlgorithm()
    {
        VdcQueryReturnValue returnValue =
                GetConfigFromCache(new GetConfigurationValueParameters(ConfigurationValues.VdsSelectionAlgorithm, Config.DefaultConfigurationVersion));

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            return VdsSelectionAlgorithm.valueOf((String) returnValue.getReturnValue());
        }

        return VdsSelectionAlgorithm.None;
    }

    public static int GetHighUtilizationForEvenDistribution()
    {
        VdcQueryReturnValue returnValue =
                GetConfigFromCache(new GetConfigurationValueParameters(ConfigurationValues.HighUtilizationForEvenlyDistribute, Config.DefaultConfigurationVersion));

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            return (Integer) returnValue.getReturnValue();
        }

        return 0;
    }

    public static int GetHighUtilizationForPowerSave()
    {
        VdcQueryReturnValue returnValue =
                GetConfigFromCache(new GetConfigurationValueParameters(ConfigurationValues.HighUtilizationForPowerSave, Config.DefaultConfigurationVersion));

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            return (Integer) returnValue.getReturnValue();
        }

        return 0;
    }

    public static int GetLowUtilizationForPowerSave()
    {
        VdcQueryReturnValue returnValue =
                GetConfigFromCache(new GetConfigurationValueParameters(ConfigurationValues.LowUtilizationForPowerSave, Config.DefaultConfigurationVersion));

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            return (Integer) returnValue.getReturnValue();
        }

        return 0;
    }

    public static int GetVcpuConsumptionPercentage()
    {
        VdcQueryReturnValue returnValue =
                GetConfigFromCache(new GetConfigurationValueParameters(ConfigurationValues.VcpuConsumptionPercentage, Config.DefaultConfigurationVersion));

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            return (Integer) returnValue.getReturnValue();
        }

        return 10;
    }

    public static ArrayList<DiskImageBase> GetDiskPresetList(VmType vmType, StorageType storageType)
    {
        // Get basic preset list from backend:
        VdcQueryReturnValue returnValue =
                Frontend.RunQuery(VdcQueryType.GetDiskConfigurationList, new VdcQueryParametersBase());

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            ArrayList<DiskImageBase> list = new ArrayList<DiskImageBase>();
            for (DiskImageBase disk : (ArrayList<DiskImageBase>) returnValue.getReturnValue())
            {
                disk.setvolume_type(disk.isBoot() && vmType == VmType.Desktop ?
                        VolumeType.Sparse : VolumeType.Preallocated);
                disk.setvolume_format(GetDiskVolumeFormat(disk.getvolume_type(), storageType));

                list.add(disk);
            }
            return list;
        }

        return new ArrayList<DiskImageBase>();
    }

    public static VolumeFormat GetDiskVolumeFormat(VolumeType volumeType, StorageType storageType) {

        switch (storageType) {
            case NFS:
            case LOCALFS:
            case POSIXFS:
                return VolumeFormat.RAW;

            case ISCSI:
            case FCP:
                switch (volumeType) {
                    case Sparse:
                        return VolumeFormat.COW;

                    case Preallocated:
                        return VolumeFormat.RAW;

                    default:
                        return VolumeFormat.Unassigned;
                }

            default:
                return VolumeFormat.Unassigned;
        }
    }

    public static ArrayList<VolumeType> GetVolumeTypeList()
    {
        return new ArrayList<VolumeType>(Arrays.asList(new VolumeType[] {
            VolumeType.Preallocated,
            VolumeType.Sparse
        }));
    }

    public static ArrayList<StorageType> GetStorageTypeList()
    {
        return new ArrayList<StorageType>(Arrays.asList(new StorageType[] {
                StorageType.ISCSI,
                StorageType.FCP
        }));
    }

    public static ArrayList<DiskInterface> GetDiskInterfaceList(VmOsType osType, Version Version)
    {
        return osType == VmOsType.WindowsXP && (Version == null || Version.compareTo(new Version("2.2")) < 0) ? new ArrayList<DiskInterface>(Arrays.asList(new DiskInterface[] { DiskInterface.IDE })) //$NON-NLS-1$
                : new ArrayList<DiskInterface>(Arrays.asList(new DiskInterface[] {
                        DiskInterface.IDE, DiskInterface.VirtIO }));
    }

    public static DiskInterface GetDefaultDiskInterface(VmOsType osType, List<Disk> disks)
    {
        return osType == VmOsType.WindowsXP ? DiskInterface.IDE : disks != null && disks.size() > 0 ? disks.get(0)
                .getDiskInterface() : DiskInterface.VirtIO;
    }

    public static VolumeFormat[] GetVolumeFormatList()
    {
        return new VolumeFormat[] { VolumeFormat.COW, VolumeFormat.RAW };
    }

    public static ArrayList<VdsNetworkInterface> GetFreeBondList(Guid hostId)
    {
        VdcQueryReturnValue returnValue =
                Frontend.RunQuery(VdcQueryType.GetVdsFreeBondsByVdsId, new GetVdsByVdsIdParameters(hostId));

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            return (ArrayList<VdsNetworkInterface>) returnValue.getReturnValue();
        }

        return new ArrayList<VdsNetworkInterface>();
    }

    public static ArrayList<String> GetoVirtISOsList()
    {
        VdcQueryReturnValue returnValue = Frontend.RunQuery(VdcQueryType.GetoVirtISOs, new VdcQueryParametersBase());

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            return (ArrayList<String>) returnValue.getReturnValue();
        }

        return new ArrayList<String>();
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
                        new SearchParameters(StringFormat.format("Storage: name=%1$s", name), SearchType.StorageDomain)); //$NON-NLS-1$

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            ArrayList<storage_domains> list =
                    (ArrayList<storage_domains>) returnValue.getReturnValue();
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
            for (Role role : (ArrayList<Role>) returnValue.getReturnValue())
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

    public static ArrayList<String> GetPmTypeList(Version ClusterVersion)
    {
        ArrayList<String> list = new ArrayList<String>();

        GetConfigurationValueParameters tempVar = new GetConfigurationValueParameters(ConfigurationValues.VdsFenceType);
        tempVar.setVersion(ClusterVersion != null ? ClusterVersion.toString() : null);
        VdcQueryReturnValue returnValue = GetConfigFromCache(tempVar);

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            String[] array = ((String) returnValue.getReturnValue()).split("[,]", -1); //$NON-NLS-1$
            for (String item : array)
            {
                list.add(item);
            }
        }

        return list;
    }

    private static HashMap<String, ArrayList<String>> cachedPmMap;

    public static ArrayList<String> GetPmOptions(String pmType)
    {
        if (cachedPmMap == null)
        {
            VdcQueryReturnValue returnValue =
                    Frontend.RunQuery(VdcQueryType.GetAgentFenceOptions, new VdcQueryParametersBase());

            if (returnValue != null && returnValue.getSucceeded())
            {
                cachedPmMap = new HashMap<String, ArrayList<String>>();

                HashMap<String, HashMap<String, Object>> dict =
                        (HashMap<String, HashMap<String, Object>>) returnValue.getReturnValue();
                for (Map.Entry<String, HashMap<String, Object>> pair : dict.entrySet())
                {
                    ArrayList<String> list = new ArrayList<String>();
                    for (Map.Entry<String, Object> p : pair.getValue().entrySet())
                    {
                        list.add(p.getKey());
                    }

                    cachedPmMap.put(pair.getKey(), list);
                }
            }
        }

        return cachedPmMap.get(pmType);
    }

    public static ArrayList<DiskImage> GetVmDiskList(Guid id)
    {
        VdcQueryReturnValue returnValue =
                Frontend.RunQuery(VdcQueryType.GetAllDisksByVmId, new GetAllDisksByVmIdParameters(id));

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            return (ArrayList<DiskImage>) returnValue.getReturnValue();
        }

        return new ArrayList<DiskImage>();
    }

    public static ArrayList<DiskImage> GetTemplateDiskList(Guid templateId)
    {
        VdcQueryReturnValue returnValue =
                Frontend.RunQuery(VdcQueryType.GetVmTemplatesDisks, new GetVmTemplatesDisksParameters(templateId));

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            return (ArrayList<DiskImage>) returnValue.getReturnValue();
        }

        return new ArrayList<DiskImage>();
    }

    public static ArrayList<VmNetworkInterface> GetVmNicList(Guid id)
    {
        VdcQueryReturnValue returnValue =
                Frontend.RunQuery(VdcQueryType.GetVmInterfacesByVmId, new GetVmByVmIdParameters(id));

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            return (ArrayList<VmNetworkInterface>) returnValue.getReturnValue();
        }

        return new ArrayList<VmNetworkInterface>();
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
                GetConfigFromCache(new GetConfigurationValueParameters(ConfigurationValues.VmPoolLeaseDays, Config.DefaultConfigurationVersion));

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            return (Integer) returnValue.getReturnValue();
        }

        return 0;
    }

    public static Date GetDefaultPoolLeaseStartTime()
    {
        VdcQueryReturnValue returnValue =
                GetConfigFromCache(new GetConfigurationValueParameters(ConfigurationValues.VmPoolLeaseStartTime, Config.DefaultConfigurationVersion));

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            return new Date(Date.parse((String) returnValue.getReturnValue()));
        }

        return new Date();
    }

    public static Date GetDefaultPoolLeaseEndTime()
    {
        VdcQueryReturnValue returnValue =
                GetConfigFromCache(new GetConfigurationValueParameters(ConfigurationValues.VmPoolLeaseEndTime, Config.DefaultConfigurationVersion));

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            return new Date(Date.parse((String) returnValue.getReturnValue()));
        }

        return new Date();
    }

    public static ArrayList<DbUser> GetUserList()
    {
        VdcQueryReturnValue returnValue =
                Frontend.RunQuery(VdcQueryType.Search, new SearchParameters("User:", SearchType.DBUser)); //$NON-NLS-1$

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            return Linq.<DbUser> Cast((ArrayList<IVdcQueryable>) returnValue.getReturnValue());
        }

        return new ArrayList<DbUser>();
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
                        new SearchParameters(StringFormat.format("Cluster: name=%1$s", name), SearchType.Cluster)); //$NON-NLS-1$

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            return Linq.FirstOrDefault(Linq.<VDSGroup> Cast((ArrayList<IVdcQueryable>) returnValue.getReturnValue()));
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
                        new SearchParameters(StringFormat.format("DataCenter: name=%1$s", name), SearchType.StoragePool)); //$NON-NLS-1$

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            return Linq.FirstOrDefault(Linq.<storage_pool> Cast((ArrayList<IVdcQueryable>) returnValue.getReturnValue()));
        }

        return null;
    }

    public static ArrayList<VM> GetVmList(String poolName)
    {
        VdcQueryReturnValue returnValue =
                Frontend.RunQuery(VdcQueryType.Search, new SearchParameters("Vms: pool=" + poolName, SearchType.VM)); //$NON-NLS-1$

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            // return ((List<IVdcQueryable>)returnValue.ReturnValue).Cast<VM>().ToList();
            return Linq.<VM> Cast((ArrayList<IVdcQueryable>) returnValue.getReturnValue());
        }

        return new ArrayList<VM>();
    }

    public static VM GetAnyVm(String poolName)
    {
        ArrayList<VM> vms = GetVmList(poolName);
        return vms.size() > 0 ? vms.get(0) : null;
    }

    private static int _cachedSearchResultsLimit = -1;

    public static int GetSearchResultsLimit()
    {
        if (_cachedSearchResultsLimit == -1)
        {
            VdcQueryReturnValue returnValue =
                    GetConfigFromCache(new GetConfigurationValueParameters(ConfigurationValues.SearchResultsLimit, Config.DefaultConfigurationVersion));

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



    private static ArrayList<VdsNetworkInterface> GetAllHostInterfaces(Guid vdsID)
    {
        ArrayList<VdsNetworkInterface> interfaceList = new ArrayList<VdsNetworkInterface>();

        VdcQueryReturnValue retValue =
                Frontend.RunQuery(VdcQueryType.GetVdsInterfacesByVdsId, new GetVdsByVdsIdParameters(vdsID));

        if (retValue != null && retValue.getSucceeded())
        {
            interfaceList = (ArrayList<VdsNetworkInterface>) retValue.getReturnValue();
        }

        return interfaceList;
    }

    public static HashMap<String, Integer> GetSystemStatistics()
    {
        VdcQueryReturnValue returnValue =
                Frontend.RunQuery(VdcQueryType.GetSystemStatistics, new GetSystemStatisticsQueryParameters(-1));

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            return (HashMap<String, Integer>) returnValue.getReturnValue();
        }

        return new HashMap<String, Integer>();
    }

    public static int GetDiskMaxSize()
    {
        VdcQueryReturnValue returnValue =
                GetConfigFromCache(new GetConfigurationValueParameters(ConfigurationValues.MaxBlockDiskSize, Config.DefaultConfigurationVersion));

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            return (Integer) returnValue.getReturnValue();
        }

        return 2047;
    }

    public static ArrayList<VM> GetUserVmList(Guid userId, String groupNames)
    {
        VdcQueryReturnValue returnValue =
                Frontend.RunQuery(VdcQueryType.GetUserVmsByUserIdAndGroups, new GetUserVmsByUserIdAndGroupsParameters()); // user.UserId,
                                                                                                                          // user.GroupNames

        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
        {
            return (ArrayList<VM>) returnValue.getReturnValue();
        }

        return new ArrayList<VM>();
    }

    public static String GetNewNicName(ArrayList<VmNetworkInterface> existingInterfaces)
    {
        int maxIfaceNumber = 0;
        if (existingInterfaces != null)
        {
            for (VmNetworkInterface iface : existingInterfaces)
            {
                // name of Interface is "eth<n>" (<n>: integer).
                if (iface.getName().length() > 3) {
                    final Integer ifaceNumber = IntegerCompat.tryParse(iface.getName().substring(3));
                    if (ifaceNumber != null && ifaceNumber > maxIfaceNumber) {
                        maxIfaceNumber = ifaceNumber;
                    }
                }
            }
        }

        return "nic" + (maxIfaceNumber + 1); //$NON-NLS-1$
    }

    private static HashMap<String, ResourceManager> _resourcesCache =
            new HashMap<String, ResourceManager>();

    public static String GetValueFromResource(String resourcePath, String key)
    {
        if (StringHelper.isNullOrEmpty(resourcePath) || StringHelper.isNullOrEmpty(key))
        {
            return ""; //$NON-NLS-1$
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
        key = key.replaceAll("-", "_"); //$NON-NLS-1$ //$NON-NLS-2$
        return SpiceConstantsManager.getInstance().getSpiceRedKeys().getString(key);
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
            return ""; //$NON-NLS-1$
        }
        ArrayList<String> values = new ArrayList<String>();

        for (String s : complexValue.split("[+]", -1)) //$NON-NLS-1$
        {
            values.add(GetValueFromSpiceRedKeysResource(s));
        }
        return StringHelper.join("+", values.toArray(new String[] {})); //$NON-NLS-1$
        // return string.Join("+", complexValue.Split(new char[] { '+' }).Select(a =>
        // GetValueFromSpiceRedKeysResource(a)).ToArray());
    }

    public static int GetMaxVmPriority()
    {
        VdcQueryReturnValue returnValue =
                GetConfigFromCache(new GetConfigurationValueParameters(ConfigurationValues.VmPriorityMaxValue, Config.DefaultConfigurationVersion));

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
                    GetConfigFromCache(new GetConfigurationValueParameters(ConfigurationValues.StoragePoolNameSizeLimit, Config.DefaultConfigurationVersion));

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
                    DataProvider.GetConfigFromCache(new GetConfigurationValueParameters(ConfigurationValues.StorageDomainNameSizeLimit, Config.DefaultConfigurationVersion));

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
    private static HashMap<Map.Entry<ConfigurationValues, String>, VdcQueryReturnValue> CachedConfigValues =
            new HashMap<Map.Entry<ConfigurationValues, String>, VdcQueryReturnValue>();

    // helper method to clear the config cache (currently used on each login)
    public static void ClearConfigCache()
    {
        if (CachedConfigValues != null)
        {
            CachedConfigValues.clear();
        }

        //        cacheCustomProperties = null;
        windowsOsTypes = null;
        linuxOsTypes = null;
        x64OsTypes = null;
        hasAdminSystemPermission = null;
    }

    // method to get an item from config while caching it (config is not supposed to change during a session)
    public static VdcQueryReturnValue GetConfigFromCache(GetConfigurationValueParameters parameters)
    {
        Map.Entry<ConfigurationValues, String> config_key =
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
                GetConfigFromCache(new GetConfigurationValueParameters(ConfigurationValues.SANWipeAfterDelete, Config.DefaultConfigurationVersion));

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
            return ((VM) entity).getId();
        }
        else if (entity instanceof storage_pool)
        {
            return ((storage_pool) entity).getId();
        }
        else if (entity instanceof VDSGroup)
        {
            return ((VDSGroup) entity).getId();
        }
        else if (entity instanceof VDS)
        {
            return ((VDS) entity).getId();
        }
        else if (entity instanceof storage_domains)
        {
            return ((storage_domains) entity).getId();
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
        else if (entity instanceof Quota)
        {
            return ((Quota) entity).getId();
        }
        else if (entity instanceof DiskImage)
        {
            return ((DiskImage) entity).getId();
        }
        else if (entity instanceof GlusterVolumeEntity)
        {
            return ((GlusterVolumeEntity) entity).getId();
        }
        else if (entity instanceof Network)
        {
            return ((Network) entity).getId();
        }
        else if (entity instanceof NetworkView)
        {
            return ((NetworkView) entity).getNetwork().getId();
        }
        return new Guid();
    }

    //    private static HashMap<String, String> cacheCustomProperties;
    //
    //    /**
    //     * Gets a dictionary in which the keys are the valid custom property keys allowed and the values are the valid
    //     * RegExp to validate the custom property values with.
    //     *
    //     * @return dictionary of valid keys and valid values' RegExps.
    //     */
    //    public static HashMap<String, String> GetCustomPropertiesList()
    //    {
    //        if (cacheCustomProperties != null)
    //        {
    //            return cacheCustomProperties;
    //        }
    //        VdcQueryReturnValue returnValue =
    //                Frontend.RunQuery(VdcQueryType.GetVmCustomProperties, new VdcQueryParametersBase());
    //        if (returnValue.getSucceeded() && returnValue.getReturnValue() != null
    //                && ((String) returnValue.getReturnValue()).equals("") == false)
    //        {
    //            String temp = (String) returnValue.getReturnValue();
    //            String[] tempArray = temp.split("[;]", -1);
    //            cacheCustomProperties = new HashMap<String, String>();
    //            for (String keyRegexpPair : tempArray)
    //            {
    //                String[] keyAndRegexp = keyRegexpPair.split("[=]", -1);
    //                String key = keyAndRegexp[0];
    //                String regexp = null;
    //                // if there is no "=", it means that there is no RegExp to
    //                // validate with, which means that all strings are valid.
    //                if (keyAndRegexp.length > 1)
    //                {
    //                    regexp = keyAndRegexp[1];
    //                }
    //
    //                if (!cacheCustomProperties.containsKey(key))
    //                {
    //                    cacheCustomProperties.put(key, regexp);
    //                }
    //            }
    //            return cacheCustomProperties;
    //        }
    //
    //        return new HashMap<String, String>();
    //    }

    private static ArrayList<VmOsType> windowsOsTypes;
    private static ArrayList<VmOsType> linuxOsTypes;
    private static ArrayList<VmOsType> x64OsTypes;

    public static ArrayList<VmOsType> GetWindowsOsTypes()
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
        windowsOsTypes = new ArrayList<VmOsType>();

        for (VmOsType type : VmOsType.values()) {
            if (type.isWindows()) {
                windowsOsTypes.add(type);
            }
        }

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

    public static ArrayList<VmOsType> GetLinuxOsTypes()
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
            new ArrayList<VmOsType>(Arrays.asList(new VmOsType[] {
                VmOsType.OtherLinux,
                VmOsType.RHEL3,
                VmOsType.RHEL3x64,
                VmOsType.RHEL4,
                VmOsType.RHEL4x64,
                VmOsType.RHEL5,
                VmOsType.RHEL5x64,
                VmOsType.RHEL6,
                VmOsType.RHEL6x64
            }));

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

    public static ArrayList<VmOsType> Get64bitOsTypes()
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
            new ArrayList<VmOsType>(Arrays.asList(new VmOsType[] {
                VmOsType.RHEL3x64,
                VmOsType.RHEL4x64,
                VmOsType.RHEL5x64,
                VmOsType.RHEL6x64,
                VmOsType.Windows2003x64,
                VmOsType.Windows2008R2x64,
                VmOsType.Windows2008x64,
                VmOsType.Windows7x64
            }));

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
            ArrayList<permissions> permissions =
                    (ArrayList<permissions>) returnValue.getReturnValue();

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
                new SearchParameters(StringFormat.format("hosts: datacenter=%1$s", storagePoolName), SearchType.VDS); //$NON-NLS-1$

        VdcQueryReturnValue result = Frontend.RunQuery(VdcQueryType.Search, sp);
        if ((result != null) && result.getSucceeded() && (result.getReturnValue() != null))
        {
            for (IVdcQueryable res : (ArrayList<IVdcQueryable>) (result.getReturnValue()))
            {
                return (VDS) res;
            }
        }
        return null;
    }

    public static ArrayList<Map.Entry<String, EntityModel>> GetBondingOptionList(RefObject<Map.Entry<String, EntityModel>> defaultItem)
    {
        ArrayList<Map.Entry<String, EntityModel>> list =
                new ArrayList<Map.Entry<String, EntityModel>>();
        EntityModel entityModel = new EntityModel();
        entityModel.setEntity("(Mode 1) Active-Backup"); //$NON-NLS-1$
        list.add(new KeyValuePairCompat<String, EntityModel>("mode=1 miimon=100", entityModel)); //$NON-NLS-1$
        entityModel = new EntityModel();
        entityModel.setEntity("(Mode 2) Load balance (balance-xor)"); //$NON-NLS-1$
        list.add(new KeyValuePairCompat<String, EntityModel>("mode=2", entityModel)); //$NON-NLS-1$
        entityModel = new EntityModel();
        entityModel.setEntity("(Mode 4) Dynamic link aggregation (802.3ad)"); //$NON-NLS-1$
        defaultItem.argvalue = new KeyValuePairCompat<String, EntityModel>("mode=4", entityModel); //$NON-NLS-1$
        list.add(defaultItem.argvalue);
        entityModel = new EntityModel();
        entityModel.setEntity("(Mode 5) Adaptive transmit load balancing (balance-tlb)"); //$NON-NLS-1$
        list.add(new KeyValuePairCompat<String, EntityModel>("mode=5", entityModel)); //$NON-NLS-1$
        entityModel = new EntityModel();
        entityModel.setEntity(""); //$NON-NLS-1$
        list.add(new KeyValuePairCompat<String, EntityModel>("custom", entityModel)); //$NON-NLS-1$
        return list;
    }

    public static String GetDefaultBondingOption()
    {
        return "mode=802.3ad miimon=150"; //$NON-NLS-1$
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
            List<storage_domains> storages =
                    (ArrayList<storage_domains>) returnValue.getReturnValue();
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
