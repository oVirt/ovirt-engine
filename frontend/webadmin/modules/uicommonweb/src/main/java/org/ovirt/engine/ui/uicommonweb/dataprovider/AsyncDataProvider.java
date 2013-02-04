package org.ovirt.engine.ui.uicommonweb.dataprovider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.EventNotificationEntity;
import org.ovirt.engine.core.common.VdcEventNotificationUtils;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.DbUser;
import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.DiskImageBase;
import org.ovirt.engine.core.common.businessentities.DiskInterface;
import org.ovirt.engine.core.common.businessentities.IVdcQueryable;
import org.ovirt.engine.core.common.businessentities.LUNs;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.QuotaEnforcementTypeEnum;
import org.ovirt.engine.core.common.businessentities.RepoFileMetaData;
import org.ovirt.engine.core.common.businessentities.Role;
import org.ovirt.engine.core.common.businessentities.ServerCpu;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.TagsType;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmGuestAgentInterface;
import org.ovirt.engine.core.common.businessentities.VmOsType;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.VmTemplateStatus;
import org.ovirt.engine.core.common.businessentities.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.VolumeType;
import org.ovirt.engine.core.common.businessentities.permissions;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.common.businessentities.tags;
import org.ovirt.engine.core.common.businessentities.vm_pools;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VmInterfaceType;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.core.common.queries.CommandVersionsInfo;
import org.ovirt.engine.core.common.queries.ConfigurationValues;
import org.ovirt.engine.core.common.queries.GetAllAttachableDisks;
import org.ovirt.engine.core.common.queries.InterfaceAndIdQueryParameters;
import org.ovirt.engine.core.common.queries.GetAllDisksByVmIdParameters;
import org.ovirt.engine.core.common.queries.GetAllFromExportDomainQueryParameters;
import org.ovirt.engine.core.common.queries.GetAllImagesListByStoragePoolIdParameters;
import org.ovirt.engine.core.common.queries.GetAllServerCpuListParameters;
import org.ovirt.engine.core.common.queries.GetAllVdsByStoragePoolParameters;
import org.ovirt.engine.core.common.queries.GetAvailableClusterVersionsByStoragePoolParameters;
import org.ovirt.engine.core.common.queries.GetConfigurationValueParameters;
import org.ovirt.engine.core.common.queries.GetDomainListParameters;
import org.ovirt.engine.core.common.queries.GetEntitiesWithPermittedActionParameters;
import org.ovirt.engine.core.common.queries.GetExistingStorageDomainListParameters;
import org.ovirt.engine.core.common.queries.GetLunsByVgIdParameters;
import org.ovirt.engine.core.common.queries.GetPermittedStorageDomainsByStoragePoolIdParameters;
import org.ovirt.engine.core.common.queries.GetStorageDomainsByConnectionParameters;
import org.ovirt.engine.core.common.queries.GetStorageDomainsByVmTemplateIdQueryParameters;
import org.ovirt.engine.core.common.queries.GetTagsByUserGroupIdParameters;
import org.ovirt.engine.core.common.queries.GetTagsByUserIdParameters;
import org.ovirt.engine.core.common.queries.GetTagsByVdsIdParameters;
import org.ovirt.engine.core.common.queries.GetTagsByVmIdParameters;
import org.ovirt.engine.core.common.queries.GetVdsByVdsIdParameters;
import org.ovirt.engine.core.common.queries.GetVdsGroupByIdParameters;
import org.ovirt.engine.core.common.queries.GetVmByVmIdParameters;
import org.ovirt.engine.core.common.queries.GetVmConfigurationBySnapshotQueryParams;
import org.ovirt.engine.core.common.queries.GetVmPoolByIdParameters;
import org.ovirt.engine.core.common.queries.GetVmTemplateParameters;
import org.ovirt.engine.core.common.queries.GetVmTemplatesByStoragePoolIdParameters;
import org.ovirt.engine.core.common.queries.GetVmTemplatesDisksParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.IsVmPoolWithSameNameExistsParameters;
import org.ovirt.engine.core.common.queries.IsVmTemlateWithSameNameExistParameters;
import org.ovirt.engine.core.common.queries.IsVmWithSameNameExistParameters;
import org.ovirt.engine.core.common.queries.MultilevelAdministrationByAdElementIdParameters;
import org.ovirt.engine.core.common.queries.MultilevelAdministrationByRoleIdParameters;
import org.ovirt.engine.core.common.queries.MultilevelAdministrationsQueriesParameters;
import org.ovirt.engine.core.common.queries.SearchParameters;
import org.ovirt.engine.core.common.queries.ServerParameters;
import org.ovirt.engine.core.common.queries.StorageDomainQueryParametersBase;
import org.ovirt.engine.core.common.queries.StoragePoolQueryParametersBase;
import org.ovirt.engine.core.common.queries.StorageServerConnectionQueryParametersBase;
import org.ovirt.engine.core.common.queries.TimeZoneQueryParams;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.queries.VdsIdParametersBase;
import org.ovirt.engine.core.common.queries.gluster.AddedGlusterServersParameters;
import org.ovirt.engine.core.common.queries.gluster.GlusterParameters;
import org.ovirt.engine.core.common.queries.gluster.GlusterServersQueryParameters;
import org.ovirt.engine.core.common.queries.gluster.GlusterVolumeAdvancedDetailsParameters;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.IntegerCompat;
import org.ovirt.engine.core.compat.KeyValuePairCompat;
import org.ovirt.engine.core.compat.NGuid;
import org.ovirt.engine.core.compat.RefObject;
import org.ovirt.engine.core.compat.RpmVersion;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.IAsyncConverter;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.models.ApplicationModeHelper;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.LoginModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmModelBehaviorBase;
import org.ovirt.engine.ui.uicommonweb.models.vms.WANDisableEffects;
import org.ovirt.engine.ui.uicommonweb.models.vms.WanColorDepth;
import org.ovirt.engine.ui.uicompat.FrontendMultipleQueryAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendMultipleQueryAsyncCallback;
import org.ovirt.engine.ui.uicompat.SpiceConstantsManager;

public final class AsyncDataProvider {

    private static final String GENERAL = "general"; //$NON-NLS-1$

    // dictionary to hold cache of all config values (per version) queried by client, if the request for them succeeded.
    private static HashMap<KeyValuePairCompat<ConfigurationValues, String>, Object> cachedConfigValues =
            new HashMap<KeyValuePairCompat<ConfigurationValues, String>, Object>();

    private static HashMap<KeyValuePairCompat<ConfigurationValues, String>, Object> cachedConfigValuesPreConvert =
            new HashMap<KeyValuePairCompat<ConfigurationValues, String>, Object>();

    private static String _defaultConfigurationVersion = null;
    public static String getDefaultConfigurationVersion() {
        return _defaultConfigurationVersion;
    }

    private static void getDefaultConfigurationVersion(Object target) {
        AsyncQuery callback = new AsyncQuery(target, new INewAsyncCallback() {

            @Override
            public void OnSuccess(Object model, Object returnValue) {
                if (returnValue != null) {
                    _defaultConfigurationVersion =
                            (String) ((VdcQueryReturnValue) returnValue).getReturnValue();
                } else {
                    _defaultConfigurationVersion = GENERAL;
                }
                LoginModel loginModel = (LoginModel) model;
                loginModel.getLoggedInEvent().raise(loginModel, EventArgs.Empty);
            }
        });
        callback.setHandleFailure(true);
        Frontend.RunQuery(VdcQueryType.GetDefaultConfigurationVersion,
                new VdcQueryParametersBase(),
                callback);
    }

    public static void initCache(LoginModel loginModel) {
        AsyncDataProvider.CacheConfigValues(new AsyncQuery(loginModel, new INewAsyncCallback() {
            @Override
            public void OnSuccess(Object target, Object returnValue) {
                getDefaultConfigurationVersion(target);
            }
        }));
    }

    public static void GetDomainListViaPublic(AsyncQuery aQuery, boolean filterInternalDomain) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                return source != null ? new ArrayList<String>((ArrayList<String>) source)
                        : new ArrayList<String>();
            }
        };
        GetDomainListParameters tempVar = new GetDomainListParameters();
        tempVar.setFilterInternalDomain(filterInternalDomain);
        Frontend.RunPublicQuery(VdcQueryType.GetDomainList, tempVar, aQuery);
    }

    public static void GetIsoDomainByDataCenterId(AsyncQuery aQuery, Guid dataCenterId) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                if (source != null)
                {
                    ArrayList<storage_domains> storageDomains = (ArrayList<storage_domains>) source;
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
        };

        StoragePoolQueryParametersBase getIsoParams = new StoragePoolQueryParametersBase(dataCenterId);
        Frontend.RunQuery(VdcQueryType.GetStorageDomainsByStoragePoolId, getIsoParams, aQuery);
    }

    public static void GetExportDomainByDataCenterId(AsyncQuery aQuery, Guid dataCenterId) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                ArrayList<storage_domains> storageDomains = (ArrayList<storage_domains>) source;
                for (storage_domains domain : storageDomains)
                {
                    if (domain.getstorage_domain_type() == StorageDomainType.ImportExport)
                    {
                        return domain;
                    }
                }

                return null;
            }
        };

        StoragePoolQueryParametersBase getExportParams = new StoragePoolQueryParametersBase(dataCenterId);
        Frontend.RunQuery(VdcQueryType.GetStorageDomainsByStoragePoolId, getExportParams, aQuery);
    }

    public static void GetIrsImageList(AsyncQuery aQuery, Guid storagePoolId) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                if (source != null)
                {
                    ArrayList<RepoFileMetaData> repoList = (ArrayList<RepoFileMetaData>) source;
                    ArrayList<String> fileNameList = new ArrayList<String>();
                    for (RepoFileMetaData RepoFileMetaData : repoList)
                    {
                        fileNameList.add(RepoFileMetaData.getRepoFileName());
                    }

                    Collections.sort(fileNameList, new Linq.CaseInsensitiveComparer());
                    return fileNameList;
                }
                return new ArrayList<String>();
            }
        };

        GetAllImagesListByStoragePoolIdParameters parameters =
                new GetAllImagesListByStoragePoolIdParameters(storagePoolId);
        Frontend.RunQuery(VdcQueryType.GetAllIsoImagesListByStoragePoolId, parameters, aQuery);
    }

    public static void GetFloppyImageList(AsyncQuery aQuery, Guid storagePoolId) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                if (source != null)
                {
                    ArrayList<RepoFileMetaData> repoList = (ArrayList<RepoFileMetaData>) source;
                    ArrayList<String> fileNameList = new ArrayList<String>();
                    for (RepoFileMetaData RepoFileMetaData : repoList)
                    {
                        fileNameList.add(RepoFileMetaData.getRepoFileName());
                    }

                    Collections.sort(fileNameList, new Linq.CaseInsensitiveComparer());
                    return fileNameList;
                }
                return new ArrayList<String>();
            }
        };

        Frontend.RunQuery(VdcQueryType.GetAllFloppyImagesListByStoragePoolId,
                new GetAllImagesListByStoragePoolIdParameters(storagePoolId),
                aQuery);
    }

    public static void GetClusterById(AsyncQuery aQuery, Guid id) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                return source;
            }
        };
        Frontend.RunQuery(VdcQueryType.GetVdsGroupById, new GetVdsGroupByIdParameters(id), aQuery);
    }

    public static void GetClusterListByName(AsyncQuery aQuery, String name) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                if (source == null)
                {
                    return new ArrayList<VDSGroup>();
                }
                return source;
            }
        };
        Frontend.RunQuery(VdcQueryType.Search,
                new SearchParameters("Cluster: name=" + name + " sortby name", SearchType.Cluster), //$NON-NLS-1$ //$NON-NLS-2$
                aQuery);
    }

    public static void GetPoolById(AsyncQuery aQuery, Guid poolId) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                return source;
            }
        };
        Frontend.RunQuery(VdcQueryType.GetVmPoolById, new GetVmPoolByIdParameters(poolId), aQuery);
    }

    public static void GetVmById(AsyncQuery aQuery, Guid vmId) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                return source;
            }
        };
        Frontend.RunQuery(VdcQueryType.GetVmByVmId, new GetVmByVmIdParameters(vmId), aQuery);
    }

    public static void GetTimeZoneList(AsyncQuery aQuery) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                if (source == null)
                {
                    return new HashMap<String, String>();
                }
                return source;
            }
        };
        TimeZoneQueryParams params = new TimeZoneQueryParams();
        params.setWindowsOS(((VmModelBehaviorBase) aQuery.getModel()).getModel().getIsWindowsOS());
        Frontend.RunQuery(VdcQueryType.GetTimeZones, params, aQuery);
    }

    public static void GetDataCenterList(AsyncQuery aQuery) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                if (source == null)
                {
                    return new ArrayList<storage_pool>();
                }
                return source;
            }
        };
        Frontend.RunQuery(VdcQueryType.Search,
                new SearchParameters("DataCenter: sortby name", SearchType.StoragePool), //$NON-NLS-1$
                aQuery);
    }

    public static void GetDataCenterListByName(AsyncQuery aQuery, String name) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                if (source == null)
                {
                    return new ArrayList<storage_pool>();
                }
                return source;
            }
        };
        Frontend.RunQuery(VdcQueryType.Search,
                new SearchParameters("DataCenter: name=" + name + " sortby name", SearchType.StoragePool), //$NON-NLS-1$ //$NON-NLS-2$
                aQuery);
    }

    public static void GetMinimalVmMemSize(AsyncQuery aQuery) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                return source != null ? ((Integer) source).intValue() : 1;
            }
        };
        GetConfigFromCache(
                new GetConfigurationValueParameters(ConfigurationValues.VMMinMemorySizeInMB,
                        getDefaultConfigurationVersion()),
                aQuery);
    }

    public static void GetSpiceUsbAutoShare(AsyncQuery aQuery) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                return source != null ? ((Boolean) source).booleanValue() : true;
            }
        };
        GetConfigFromCache(
                new GetConfigurationValueParameters(ConfigurationValues.SpiceUsbAutoShare,
                        getDefaultConfigurationVersion()),
                aQuery);
    }

    public static void GetWANColorDepth(AsyncQuery aQuery) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery) {
                return source != null ? WanColorDepth.fromInt(((Integer) source).intValue()) : WanColorDepth.depth16;
            }
        };
        GetConfigFromCache(
                new GetConfigurationValueParameters(ConfigurationValues.WANColorDepth, getDefaultConfigurationVersion()),
                aQuery);
    }

    public static void GetWANDisableEffects(AsyncQuery aQuery) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                if (source == null) {
                    return new ArrayList<WANDisableEffects>();
                }

                List<WANDisableEffects> res = new ArrayList<WANDisableEffects>();
                String fromDb = (String) source;
                for (String value : fromDb.split(",")) {//$NON-NLS-1$
                    if (value == null) {
                        continue;
                    }

                    String trimmedValue = value.trim();
                    if ("".equals(trimmedValue)) {
                        continue;
                    }

                    res.add(WANDisableEffects.fromString(trimmedValue));
                }

                return res;

            }
        };
        GetConfigFromCache(
                new GetConfigurationValueParameters(ConfigurationValues.WANDisableEffects,
                        getDefaultConfigurationVersion()),
                aQuery);
    }

    public static void GetMaximalVmMemSize64OS(AsyncQuery aQuery, String version) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                return source != null ? ((Integer) source).intValue() : 262144;
            }
        };
        GetConfigurationValueParameters tempVar =
                new GetConfigurationValueParameters(ConfigurationValues.VM64BitMaxMemorySizeInMB);
        tempVar.setVersion(version);
        GetConfigFromCache(tempVar, aQuery);
    }

    public static void GetMaximalVmMemSize32OS(AsyncQuery aQuery) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                return source != null ? ((Integer) source).intValue() : 20480;
            }
        };
        GetConfigFromCache(
                new GetConfigurationValueParameters(ConfigurationValues.VM32BitMaxMemorySizeInMB,
                        getDefaultConfigurationVersion()),
                aQuery);
    }

    public static void GetMaxVmsInPool(AsyncQuery aQuery) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                return source != null ? ((Integer) source).intValue() : 1000;
            }
        };
        GetConfigFromCache(
                new GetConfigurationValueParameters(ConfigurationValues.MaxVmsInPool, getDefaultConfigurationVersion()),
                aQuery);
    }

    public static void GetMaxNumOfVmSockets(AsyncQuery aQuery, String version) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                return source != null ? ((Integer) source).intValue() : 1;
            }
        };
        GetConfigurationValueParameters tempVar =
                new GetConfigurationValueParameters(ConfigurationValues.MaxNumOfVmSockets);
        tempVar.setVersion(version);
        GetConfigFromCache(tempVar, aQuery);
    }

    public static void GetMaxNumOfVmCpus(AsyncQuery aQuery, String version) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                return source != null ? ((Integer) source).intValue() : 1;
            }
        };
        GetConfigurationValueParameters tempVar =
                new GetConfigurationValueParameters(ConfigurationValues.MaxNumOfVmCpus);
        tempVar.setVersion(version);
        GetConfigFromCache(tempVar, aQuery);
    }

    public static void GetMaxNumOfCPUsPerSocket(AsyncQuery aQuery, String version) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                return source != null ? ((Integer) source).intValue() : 1;
            }
        };
        GetConfigurationValueParameters tempVar =
                new GetConfigurationValueParameters(ConfigurationValues.MaxNumOfCpuPerSocket);
        tempVar.setVersion(version);
        GetConfigFromCache(tempVar, aQuery);
    }

    public static void GetClusterList(AsyncQuery aQuery, Guid dataCenterId) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                if (source != null)
                {
                    ArrayList<VDSGroup> list = (ArrayList<VDSGroup>) source;
                    Collections.sort(list, new Linq.VdsGroupByNameComparer());
                    return list;
                }
                return new ArrayList<VDSGroup>();
            }
        };
        Frontend.RunQuery(VdcQueryType.GetVdsGroupsByStoragePoolId,
                new StoragePoolQueryParametersBase(dataCenterId),
                aQuery);
    }

    public static void GetClusterList(AsyncQuery aQuery) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                if (source != null)
                {
                    ArrayList<VDSGroup> list = (ArrayList<VDSGroup>) source;
                    Collections.sort(list, new Linq.VdsGroupByNameComparer());
                    return list;
                }
                return new ArrayList<VDSGroup>();
            }
        };
        Frontend.RunQuery(VdcQueryType.GetAllVdsGroups, new VdcQueryParametersBase(), aQuery);
    }

    public static void GetTemplateDiskList(AsyncQuery aQuery, Guid templateId) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                if (source == null)
                {
                    return new ArrayList<DiskImage>();
                }
                return source;
            }
        };
        Frontend.RunQuery(VdcQueryType.GetVmTemplatesDisks, new GetVmTemplatesDisksParameters(templateId), aQuery);
    }


    /**
     * Round the priority to the closest value from n (3 for now) values
     *
     * i.e.: if priority entered is 30 and the predefined values are 1,50,100
     *
     * then the return value will be 50 (closest to 50).
     * @param priority - the current priority of the vm
     * @param maxPriority - the max priority
     * @return the rounded priority
     */
    public static int GetRoundedPriority(int priority, int maxPriority) {

        int medium = maxPriority / 2;

        int[] levels = new int[] { 1, medium, maxPriority };

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

    public static void GetTemplateListByDataCenter(AsyncQuery aQuery, Guid dataCenterId) {
        aQuery.converterCallback = new TemplateConverter();
        Frontend.RunQuery(VdcQueryType.GetVmTemplatesByStoragePoolId,
                new GetVmTemplatesByStoragePoolIdParameters(dataCenterId),
                aQuery);
    }

    public static void GetTemplateListByStorage(AsyncQuery aQuery, Guid storageId) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                ArrayList<VmTemplate> list = new ArrayList<VmTemplate>();
                if (source != null)
                {
                    for (VmTemplate template : (ArrayList<VmTemplate>) source)
                    {
                        if (template.getstatus() == VmTemplateStatus.OK)
                        {
                            list.add(template);
                        }
                    }

                    Collections.sort(list, new Linq.VmTemplateByNameComparer());
                }

                return list;
            }
        };
        Frontend.RunQuery(VdcQueryType.GetVmTemplatesFromStorageDomain,
                new StorageDomainQueryParametersBase(storageId),
                aQuery);
    }

    public static void GetNumOfMonitorList(AsyncQuery aQuery) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                ArrayList<Integer> nums = new ArrayList<Integer>();
                if (source != null)
                {
                    Iterable numEnumerable = (Iterable) source;
                    Iterator numIterator = numEnumerable.iterator();
                    while (numIterator.hasNext())
                    {
                        nums.add(Integer.parseInt(numIterator.next().toString()));
                    }
                }
                return nums;
            }
        };
        GetConfigFromCache(
                new GetConfigurationValueParameters(ConfigurationValues.ValidNumOfMonitors,
                        getDefaultConfigurationVersion()),
                aQuery);
    }

    public static void GetStorageDomainListByTemplate(AsyncQuery aQuery, Guid templateId) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                if (source == null)
                {
                    return new ArrayList<storage_domains>();
                }
                return source;
            }
        };
        Frontend.RunQuery(VdcQueryType.GetStorageDomainsByVmTemplateId,
                new GetStorageDomainsByVmTemplateIdQueryParameters(templateId),
                aQuery);
    }

    public static void GetStorageDomainList(AsyncQuery aQuery, Guid dataCenterId) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                if (source == null)
                {
                    return new ArrayList<storage_domains>();
                }
                return source;
            }
        };
        Frontend.RunQuery(VdcQueryType.GetStorageDomainsByStoragePoolId,
                new StoragePoolQueryParametersBase(dataCenterId),
                aQuery);
    }

    public static void GetMaxVmPriority(AsyncQuery aQuery) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                if (source == null)
                {
                    return 100;
                }
                return ((Integer) source).intValue();
            }
        };
        GetConfigFromCache(
                new GetConfigurationValueParameters(ConfigurationValues.VmPriorityMaxValue,
                        getDefaultConfigurationVersion()),
                aQuery);
    }

    public static void GetDefaultTimeZone(AsyncQuery aQuery) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                if (source != null)
                {
                    return ((Map.Entry<String, String>) source).getKey();
                }
                return ""; //$NON-NLS-1$
            }
        };
        Frontend.RunQuery(VdcQueryType.GetDefualtTimeZone, new VdcQueryParametersBase(), aQuery);
    }

    public static void GetHostById(AsyncQuery aQuery, Guid id) {

        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery) {
                return source;
            }
        };
        Frontend.RunQuery(VdcQueryType.GetVdsByVdsId, new GetVdsByVdsIdParameters(id), aQuery);
    }

    public static void GetHostListByCluster(AsyncQuery aQuery, String clusterName) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                if (source != null)
                {
                    ArrayList<VDS> list = Linq.<VDS> Cast((ArrayList<IVdcQueryable>) source);
                    return list;
                }

                return new ArrayList<VDS>();
            }
        };
        Frontend.RunQuery(VdcQueryType.Search, new SearchParameters("Host: cluster = " + clusterName + " sortby name", //$NON-NLS-1$ //$NON-NLS-2$
                SearchType.VDS), aQuery);
    }

    public static void GetHostListByDataCenter(AsyncQuery aQuery, Guid spId) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                if (source != null)
                {
                    ArrayList<VDS> list = Linq.<VDS> Cast((ArrayList<IVdcQueryable>) source);
                    return list;
                }

                return new ArrayList<VDS>();
            }
        };
        Frontend.RunQuery(VdcQueryType.GetAllVdsByStoragePool, new GetAllVdsByStoragePoolParameters(spId), aQuery);
    }

    public static void GetVmDiskList(AsyncQuery aQuery, Guid vmId, boolean isRefresh) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                if (source != null)
                {
                    return source;
                }
                return new ArrayList<DiskImage>();
            }
        };
        GetAllDisksByVmIdParameters params = new GetAllDisksByVmIdParameters(vmId);
        params.setRefresh(isRefresh);
        Frontend.RunQuery(VdcQueryType.GetAllDisksByVmId, params, aQuery);
    }

    public final static class GetSnapshotListQueryResult {
        private Guid privatePreviewingImage = new Guid();

        public Guid getPreviewingImage() {
            return privatePreviewingImage;
        }

        private void setPreviewingImage(Guid value) {
            privatePreviewingImage = value;
        }

        private ArrayList<DiskImage> privateSnapshots;

        public ArrayList<DiskImage> getSnapshots() {
            return privateSnapshots;
        }

        private void setSnapshots(ArrayList<DiskImage> value) {
            privateSnapshots = value;
        }

        private DiskImage privateDisk;

        public DiskImage getDisk() {
            return privateDisk;
        }

        private void setDisk(DiskImage value) {
            privateDisk = value;
        }

        private Guid privateVmId = new Guid();

        public Guid getVmId() {
            return privateVmId;
        }

        public void setVmId(Guid value) {
            privateVmId = value;
        }

        public GetSnapshotListQueryResult(Guid previewingImage, ArrayList<DiskImage> snapshots, DiskImage disk) {
            setPreviewingImage(previewingImage);
            setSnapshots(snapshots);
            setDisk(disk);
        }
    }

    public static void GetMaxVmMemSize(AsyncQuery aQuery, boolean is64) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                if (source != null)
                {
                    return source;
                }
                return 262144;
            }
        };
        GetConfigFromCache(
                new GetConfigurationValueParameters(is64 ? ConfigurationValues.VM64BitMaxMemorySizeInMB
                        : ConfigurationValues.VM32BitMaxMemorySizeInMB, getDefaultConfigurationVersion()),
                aQuery);
    }

    public static void GetDomainList(AsyncQuery aQuery, boolean filterInternalDomain) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                return source != null ? new ArrayList<String>((ArrayList<String>) source)
                        : new ArrayList<String>();
            }
        };
        GetDomainListParameters tempVar = new GetDomainListParameters();
        tempVar.setFilterInternalDomain(filterInternalDomain);
        Frontend.RunQuery(VdcQueryType.GetDomainList, tempVar, aQuery);
    }

    public static void GetRoleList(AsyncQuery aQuery) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                return source != null ? (ArrayList<Role>) source : new ArrayList<Role>();
            }
        };
        Frontend.RunQuery(VdcQueryType.GetAllRoles, new MultilevelAdministrationsQueriesParameters(), aQuery);
    }

    public static void GetStorageDomainById(AsyncQuery aQuery, Guid storageDomainId) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                return source != null ? (storage_domains) source : null;
            }
        };
        Frontend.RunQuery(VdcQueryType.GetStorageDomainById,
                new StorageDomainQueryParametersBase(storageDomainId),
                aQuery);
    }

    public static void GetDiskPresetList(AsyncQuery aQuery, StorageType storageType) {
        aQuery.setData(new Object[] { storageType });
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                if (source == null) {
                    return null;
                }

                ArrayList<DiskImageBase> list = new ArrayList<DiskImageBase>();
                StorageType storageType = (StorageType) _asyncQuery.Data[0];
                boolean hasBootDisk = false;
                for (DiskImageBase disk : (ArrayList<DiskImageBase>) source) {
                    if (!hasBootDisk) {
                        disk.setBoot(true);
                        hasBootDisk = true;
                    }

                    disk.setvolume_type(storageType == StorageType.ISCSI || storageType == StorageType.FCP ?
                            VolumeType.Preallocated : VolumeType.Sparse);

                    disk.setvolume_format(GetDiskVolumeFormat(disk.getvolume_type(), storageType));

                    list.add(disk);
                }
                return list;
            }
        };
        Frontend.RunQuery(VdcQueryType.GetDiskConfigurationList, new VdcQueryParametersBase(), aQuery);
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

    public static void GetClusterNetworkList(AsyncQuery aQuery, Guid clusterId) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                if (source == null)
                {
                    return new ArrayList<Network>();
                }
                return source;
            }
        };
        Frontend.RunQuery(VdcQueryType.GetAllNetworksByClusterId, new IdQueryParameters(clusterId), aQuery);
    }

    public static void GetDataCenterById(AsyncQuery aQuery, Guid dataCenterId) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                return source;
            }
        };
        Frontend.RunQuery(VdcQueryType.GetStoragePoolById, new StoragePoolQueryParametersBase(dataCenterId), aQuery);
    }

    public static void GetTemplateById(AsyncQuery aQuery, Guid templateId) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                return source;
            }
        };
        Frontend.RunQuery(VdcQueryType.GetVmTemplate, new GetVmTemplateParameters(templateId), aQuery);
    }

    public static void GetHostList(AsyncQuery aQuery) {
        GetHostListByStatus(aQuery, null);
    }

    public static void GetHostListByStatus(AsyncQuery aQuery, VDSStatus status) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                if (source != null)
                {
                    ArrayList<VDS> list = Linq.<VDS> Cast((Iterable) source);
                    return list;
                }

                return new ArrayList<VDS>();
            }
        };
        SearchParameters searchParameters =
                new SearchParameters("Host: " + (status == null ? "" : ("status=" + status.name())), SearchType.VDS); //$NON-NLS-1$ //$NON-NLS-2$
        searchParameters.setMaxCount(9999);
        Frontend.RunQuery(VdcQueryType.Search, searchParameters, aQuery);
    }

    public static void GetVolumeList(AsyncQuery aQuery, String clusterName) {

        if ((ApplicationModeHelper.getUiMode().getValue() & ApplicationMode.GlusterOnly.getValue()) == 0) {
            aQuery.asyncCallback.OnSuccess(aQuery.Model, new ArrayList<GlusterVolumeEntity>());
            return;
        }
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                if (source != null)
                {
                    ArrayList<GlusterVolumeEntity> list =
                            (ArrayList<GlusterVolumeEntity>) source;
                    return list;
                }

                return new ArrayList<GlusterVolumeEntity>();
            }
        };
        SearchParameters searchParameters;
        searchParameters =
                clusterName == null ? new SearchParameters("Volumes:", SearchType.GlusterVolume) //$NON-NLS-1$
                        : new SearchParameters("Volumes: cluster.name=" + clusterName, SearchType.GlusterVolume); //$NON-NLS-1$
        searchParameters.setMaxCount(9999);
        Frontend.RunQuery(VdcQueryType.Search, searchParameters, aQuery);
    }

    public static void GetGlusterVolumeOptionInfoList(AsyncQuery aQuery, Guid clusterId) {
        Frontend.RunQuery(VdcQueryType.GetGlusterVolumeOptionsInfo, new GlusterParameters(clusterId), aQuery);
    }

    public static void GetHostFingerprint(AsyncQuery aQuery, String hostAddress) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                return source != null ? (String) source : ""; //$NON-NLS-1$
            }
        };
        Frontend.RunQuery(VdcQueryType.GetServerSSHKeyFingerprint, new ServerParameters(hostAddress), aQuery);
    }

    public static void GetGlusterHosts(AsyncQuery aQuery, String hostAddress, String rootPassword, String fingerprint) {
        GlusterServersQueryParameters parameters = new GlusterServersQueryParameters(hostAddress, rootPassword);
        parameters.setFingerprint(fingerprint);
        Frontend.RunQuery(VdcQueryType.GetGlusterServersForImport,
                parameters,
                aQuery);
    }

    public static void GetClusterGlusterServices(AsyncQuery aQuery, Guid clusterId) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                return source;
            }
        };
        // Passing empty values for Volume and Brick to get the services of all the volumes/hosts in the cluster
        GlusterVolumeAdvancedDetailsParameters parameters =
                new GlusterVolumeAdvancedDetailsParameters(clusterId, "", "", false); //$NON-NLS-1$ //$NON-NLS-2$
        Frontend.RunQuery(VdcQueryType.GetGlusterVolumeAdvancedDetails,
                parameters,
                aQuery);
    }

    public static void GetGlusterVolumeBrickDetails(AsyncQuery aQuery, Guid clusterId, String volume, String brick) {
        GlusterVolumeAdvancedDetailsParameters parameters =
                new GlusterVolumeAdvancedDetailsParameters(clusterId, volume, brick, true);
        Frontend.RunQuery(VdcQueryType.GetGlusterVolumeAdvancedDetails,
                parameters,
                aQuery);
    }

    public static void GetGlusterHostsNewlyAdded(AsyncQuery aQuery, Guid clusterId, boolean isFingerprintRequired) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                return source;
            }
        };
        Frontend.RunQuery(VdcQueryType.GetAddedGlusterServers,
                new AddedGlusterServersParameters(clusterId, isFingerprintRequired),
                aQuery);
    }

    public static void GetRpmVersionViaPublic(AsyncQuery aQuery) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                return source != null ? (String) source : ""; //$NON-NLS-1$
            }
        };
        Frontend.RunPublicQuery(VdcQueryType.GetConfigurationValue,
                new GetConfigurationValueParameters(ConfigurationValues.ProductRPMVersion,
                        getDefaultConfigurationVersion()),
                aQuery);
    }

    public static void GetSearchResultsLimit(AsyncQuery aQuery) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                return source != null ? ((Integer) source).intValue() : 100;
            }
        };
        GetConfigFromCache(
                new GetConfigurationValueParameters(ConfigurationValues.SearchResultsLimit,
                        getDefaultConfigurationVersion()),
                aQuery);
    }

    public static void GetSANWipeAfterDelete(AsyncQuery aQuery) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                return source != null ? ((Boolean) source).booleanValue() : false;
            }
        };
        GetConfigFromCache(
                new GetConfigurationValueParameters(ConfigurationValues.SANWipeAfterDelete,
                        getDefaultConfigurationVersion()),
                aQuery);
    }

    public static void GetCustomPropertiesList(AsyncQuery aQuery) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                return source != null ? (HashMap<Version, String>) source : null;
            }
        };
        Frontend.RunQuery(VdcQueryType.GetVmCustomProperties, new VdcQueryParametersBase(), aQuery);
    }

    public static void GetPermissionsByAdElementId(AsyncQuery aQuery, Guid userId) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                return source != null ? (ArrayList<permissions>) source
                        : new ArrayList<permissions>();
            }
        };
        Frontend.RunQuery(VdcQueryType.GetPermissionsByAdElementId,
                new MultilevelAdministrationByAdElementIdParameters(userId),
                aQuery);
    }

    public static void GetRoleActionGroupsByRoleId(AsyncQuery aQuery, Guid roleId) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                return source != null ? (ArrayList<ActionGroup>) source
                        : new ArrayList<ActionGroup>();
            }
        };
        Frontend.RunQuery(VdcQueryType.GetRoleActionGroupsByRoleId,
                new MultilevelAdministrationByRoleIdParameters(roleId),
                aQuery);
    }

    public static void IsTemplateNameUnique(AsyncQuery aQuery, String name) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                return source != null ? !((Boolean) source).booleanValue() : false;
            }
        };
        Frontend.RunQuery(VdcQueryType.IsVmTemlateWithSameNameExist,
                new IsVmTemlateWithSameNameExistParameters(name),
                aQuery);
    }

    public static void IsVmNameUnique(AsyncQuery aQuery, String name) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                return source != null ? !((Boolean) source).booleanValue() : false;
            }
        };
        Frontend.RunQuery(VdcQueryType.IsVmWithSameNameExist, new IsVmWithSameNameExistParameters(name), aQuery);
    }

    public static void GetDataCentersWithPermittedActionOnClusters(AsyncQuery aQuery, ActionGroup actionGroup) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                if (source == null)
                {
                    return new ArrayList<storage_pool>();
                }
                return source;
            }
        };

        GetEntitiesWithPermittedActionParameters getEntitiesWithPermittedActionParameters =
                new GetEntitiesWithPermittedActionParameters();
        getEntitiesWithPermittedActionParameters.setActionGroup(actionGroup);
        Frontend.RunQuery(VdcQueryType.GetDataCentersWithPermittedActionOnClusters,
                getEntitiesWithPermittedActionParameters,
                aQuery);
    }

    public static void GetClustersWithPermittedAction(AsyncQuery aQuery, ActionGroup actionGroup) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                if (source != null)
                {
                    ArrayList<VDSGroup> list = (ArrayList<VDSGroup>) source;
                    Collections.sort(list, new Linq.VdsGroupByNameComparer());
                    return list;
                }
                return new ArrayList<VDSGroup>();
            }
        };

        GetEntitiesWithPermittedActionParameters getEntitiesWithPermittedActionParameters =
                new GetEntitiesWithPermittedActionParameters();
        getEntitiesWithPermittedActionParameters.setActionGroup(actionGroup);
        Frontend.RunQuery(VdcQueryType.GetClustersWithPermittedAction, getEntitiesWithPermittedActionParameters, aQuery);
    }

    public static void GetVmTemplatesWithPermittedAction(AsyncQuery aQuery, ActionGroup actionGroup) {
        aQuery.converterCallback = new TemplateConverter();

        GetEntitiesWithPermittedActionParameters getEntitiesWithPermittedActionParameters =
                new GetEntitiesWithPermittedActionParameters();
        getEntitiesWithPermittedActionParameters.setActionGroup(actionGroup);
        Frontend.RunQuery(VdcQueryType.GetVmTemplatesWithPermittedAction,
                getEntitiesWithPermittedActionParameters,
                aQuery);
    }

    public static void GetAllVmTemplates(AsyncQuery aQuery) {
        aQuery.converterCallback = new TemplateConverter();
        VdcQueryParametersBase params = new VdcQueryParametersBase();
        Frontend.RunQuery(VdcQueryType.GetAllVmTemplates, params, aQuery);
    }

    public static void IsUSBEnabledByDefault(AsyncQuery aQuery) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                return source != null ? ((Boolean) source).booleanValue() : false;
            }
        };
        GetConfigFromCache(
                new GetConfigurationValueParameters(ConfigurationValues.EnableUSBAsDefault,
                        getDefaultConfigurationVersion()),
                aQuery);
    }

    public static void GetStorageConnectionById(AsyncQuery aQuery, String id, boolean isRefresh) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                return source != null ? (StorageServerConnections) source : null;
            }
        };
        StorageServerConnectionQueryParametersBase params = new StorageServerConnectionQueryParametersBase(id);
        params.setRefresh(isRefresh);
        Frontend.RunQuery(VdcQueryType.GetStorageServerConnectionById, params, aQuery);
    }

    public static void GetDataCentersByStorageDomain(AsyncQuery aQuery, Guid storageDomainId) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                return source != null ? (ArrayList<storage_pool>) source : null;
            }
        };
        Frontend.RunQuery(VdcQueryType.GetStoragePoolsByStorageDomainId,
                new StorageDomainQueryParametersBase(storageDomainId),
                aQuery);
    }

    public static void GetDataCenterVersions(AsyncQuery aQuery, NGuid dataCenterId) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                if (source == null)
                {
                    return new ArrayList<Version>();
                }
                else
                {
                    ArrayList<Version> list = (ArrayList<Version>) source;
                    Collections.sort(list);
                    return list;
                }
            }
        };
        GetAvailableClusterVersionsByStoragePoolParameters tempVar =
                new GetAvailableClusterVersionsByStoragePoolParameters();
        tempVar.setStoragePoolId(dataCenterId);
        Frontend.RunQuery(VdcQueryType.GetAvailableClusterVersionsByStoragePool, tempVar, aQuery);
    }

    public static void GetDataCenterMaxNameLength(AsyncQuery aQuery) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                return source != null ? ((Integer) source).intValue() : 1;
            }
        };
        GetConfigFromCache(
                new GetConfigurationValueParameters(ConfigurationValues.StoragePoolNameSizeLimit,
                        getDefaultConfigurationVersion()),
                aQuery);
    }

    public static void GetClusterServerMemoryOverCommit(AsyncQuery aQuery) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                return source != null ? ((Integer) source).intValue() : 0;
            }
        };
        GetConfigFromCache(
                new GetConfigurationValueParameters(ConfigurationValues.MaxVdsMemOverCommitForServers,
                        getDefaultConfigurationVersion()),
                aQuery);
    }

    public static void GetClusterDesktopMemoryOverCommit(AsyncQuery aQuery) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                return source != null ? ((Integer) source).intValue() : 0;
            }
        };
        GetConfigFromCache(
                new GetConfigurationValueParameters(ConfigurationValues.MaxVdsMemOverCommit,
                        getDefaultConfigurationVersion()),
                aQuery);
    }

    public static void GetAllowClusterWithVirtGlusterEnabled(AsyncQuery aQuery) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                return source != null ? source : Boolean.TRUE;
            }
        };
        GetConfigFromCache(
                new GetConfigurationValueParameters(ConfigurationValues.AllowClusterWithVirtGlusterEnabled,
                        getDefaultConfigurationVersion()),
                aQuery);
    }

    public static void GetCPUList(AsyncQuery aQuery, Version version) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                return source != null ? (ArrayList<ServerCpu>) source : new ArrayList<ServerCpu>();
            }
        };
        Frontend.RunQuery(VdcQueryType.GetAllServerCpuList, new GetAllServerCpuListParameters(version), aQuery);
    }

    public static void GetPmTypeList(AsyncQuery aQuery, Version version) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                ArrayList<String> list = new ArrayList<String>();
                if (source != null)
                {
                    String[] array = ((String) source).split("[,]", -1); //$NON-NLS-1$
                    for (String item : array)
                    {
                        list.add(item);
                    }
                }
                return list;
            }
        };
        GetConfigurationValueParameters tempVar = new GetConfigurationValueParameters(ConfigurationValues.VdsFenceType);
        tempVar.setVersion(version != null ? version.toString() : getDefaultConfigurationVersion());
        GetConfigFromCache(tempVar, aQuery);
    }

    public static void GetPmOptions(AsyncQuery aQuery, String pmType) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                String pmtype = (String) _asyncQuery.Data[0];
                HashMap<String, ArrayList<String>> cachedPmMap =
                        new HashMap<String, ArrayList<String>>();
                HashMap<String, HashMap<String, Object>> dict =
                        (HashMap<String, HashMap<String, Object>>) source;
                for (Map.Entry<String, HashMap<String, Object>> pair : dict.entrySet())
                {
                    ArrayList<String> list = new ArrayList<String>();
                    for (Map.Entry<String, Object> p : pair.getValue().entrySet())
                    {
                        list.add(p.getKey());
                    }

                    cachedPmMap.put(pair.getKey(), list);
                }
                return cachedPmMap.get(pmtype);
            }
        };
        aQuery.setData(new Object[] { pmType });
        Frontend.RunQuery(VdcQueryType.GetAgentFenceOptions2, new VdcQueryParametersBase(), aQuery);
    }

    public static void GetNetworkList(AsyncQuery aQuery, Guid dataCenterId) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                return source != null ? (ArrayList<Network>) source : new ArrayList<Network>();
            }
        };

        Frontend.RunQuery(VdcQueryType.GetAllNetworks, new IdQueryParameters(dataCenterId), aQuery);
    }

    public static void GetISOStorageDomainList(AsyncQuery aQuery) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                if (source != null)
                {
                    ArrayList<storage_domains> allStorageDomains =
                            (ArrayList<storage_domains>) source;
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
        };

        SearchParameters searchParams = new SearchParameters("Storage:", SearchType.StorageDomain); //$NON-NLS-1$
        searchParams.setMaxCount(9999);

        Frontend.RunQuery(VdcQueryType.Search, searchParams, aQuery);
    }

    public static void GetStorageDomainList(AsyncQuery aQuery) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                return source != null ? (ArrayList<storage_domains>) source
                        : new ArrayList<storage_domains>();
            }
        };

        SearchParameters searchParams = new SearchParameters("Storage:", SearchType.StorageDomain); //$NON-NLS-1$
        searchParams.setMaxCount(9999);

        Frontend.RunQuery(VdcQueryType.Search, searchParams, aQuery);
    }

    public static void GetLocalStorageHost(AsyncQuery aQuery, String dataCenterName) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                if (source != null)
                {
                    for (IVdcQueryable item : (ArrayList<IVdcQueryable>) source)
                    {
                        return item;
                    }
                }
                return null;
            }
        };
        SearchParameters sp = new SearchParameters("hosts: datacenter=" + dataCenterName, SearchType.VDS); //$NON-NLS-1$
        Frontend.RunQuery(VdcQueryType.Search, sp, aQuery);
    }

    public static void GetStorageDomainsByConnection(AsyncQuery aQuery, NGuid storagePoolId, String connectionPath) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                return source != null ? (ArrayList<storage_domains>) source : null;
            }
        };

        GetStorageDomainsByConnectionParameters param = new GetStorageDomainsByConnectionParameters();
        param.setConnection(connectionPath);
        if (storagePoolId != null) {
            param.setStoragePoolId(storagePoolId.getValue());
        }

        Frontend.RunQuery(VdcQueryType.GetStorageDomainsByConnection, param, aQuery);
    }

    public static void GetExistingStorageDomainList(AsyncQuery aQuery,
            Guid hostId,
            StorageDomainType domainType,
            String path) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                return source != null ? (ArrayList<storage_domains>) source : null;
            }
        };

        Frontend.RunQuery(VdcQueryType.GetExistingStorageDomainList, new GetExistingStorageDomainListParameters(hostId,
                StorageType.NFS,
                domainType,
                path), aQuery);
    }

    public static void GetLocalFSPath(AsyncQuery aQuery) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                return source != null ? (String) source : ""; //$NON-NLS-1$
            }
        };
        GetConfigFromCache(
                new GetConfigurationValueParameters(ConfigurationValues.RhevhLocalFSPath,
                        getDefaultConfigurationVersion()),
                aQuery);
    }

    public static void GetStorageDomainMaxNameLength(AsyncQuery aQuery) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                return source != null ? ((Integer) source).intValue() : 1;
            }
        };
        GetConfigFromCache(
                new GetConfigurationValueParameters(ConfigurationValues.StorageDomainNameSizeLimit,
                        getDefaultConfigurationVersion()),
                aQuery);
    }

    public static void IsStorageDomainNameUnique(AsyncQuery aQuery, String name) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                if (source != null)
                {
                    ArrayList<storage_domains> storageDomains = (ArrayList<storage_domains>) source;
                    return storageDomains.isEmpty();
                }

                return source;
            }
        };
        Frontend.RunQuery(VdcQueryType.Search, new SearchParameters("Storage: name=" + name, //$NON-NLS-1$
                SearchType.StorageDomain), aQuery);
    }

    public static void GetHighUtilizationForEvenDistribution(AsyncQuery aQuery) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                return source != null ? ((Integer) source).intValue() : 0;
            }
        };
        GetConfigFromCache(
                new GetConfigurationValueParameters(ConfigurationValues.HighUtilizationForEvenlyDistribute,
                        getDefaultConfigurationVersion()),
                aQuery);
    }

    public static void GetLowUtilizationForPowerSave(AsyncQuery aQuery) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                return source != null ? ((Integer) source).intValue() : 0;
            }
        };
        GetConfigFromCache(
                new GetConfigurationValueParameters(ConfigurationValues.LowUtilizationForPowerSave,
                        getDefaultConfigurationVersion()),
                aQuery);
    }

    public static void GetHighUtilizationForPowerSave(AsyncQuery aQuery) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                return source != null ? ((Integer) source).intValue() : 0;
            }
        };
        GetConfigFromCache(
                new GetConfigurationValueParameters(ConfigurationValues.HighUtilizationForPowerSave,
                        getDefaultConfigurationVersion()),
                aQuery);
    }

    public static void GetNetworkConnectivityCheckTimeoutInSeconds(AsyncQuery aQuery) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                return source != null ? ((Integer) source).intValue() : 120;
            }
        };
        GetConfigFromCache(
                new GetConfigurationValueParameters(ConfigurationValues.NetworkConnectivityCheckTimeoutInSeconds,
                        getDefaultConfigurationVersion()),
                aQuery);
    }

    public static void GetMaxSpmPriority(AsyncQuery aQuery) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery) {
                return source != null ? ((Integer) source).intValue() : 0;
            }
        };

        // GetConfigFromCache(
        // new GetConfigurationValueParameters(ConfigurationValues.HighUtilizationForPowerSave, getDefaultConfigurationVersion()),
        // aQuery);

        aQuery.asyncCallback.OnSuccess(aQuery.getModel(), 10);
    }

    public static void GetDefaultSpmPriority(AsyncQuery aQuery) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery) {
                return source != null ? ((Integer) source).intValue() : 0;
            }
        };

        // GetConfigFromCache(
        // new GetConfigurationValueParameters(ConfigurationValues.HighUtilizationForPowerSave, getDefaultConfigurationVersion()),
        // aQuery);

        aQuery.asyncCallback.OnSuccess(aQuery.getModel(), 5);
    }

    public static void GetDefaultPmProxyPreferences(AsyncQuery query) {
        GetConfigFromCache(
            new GetConfigurationValueParameters(ConfigurationValues.FenceProxyDefaultPreferences,
                        getDefaultConfigurationVersion()),
            query);
    }

    public static void GetRootTag(AsyncQuery aQuery) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                if (source != null)
                {
                    tags tag = (tags) source;

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
        };
        Frontend.RunQuery(VdcQueryType.GetRootTag, new VdcQueryParametersBase(), aQuery);
    }

    private static void SetAttachedTagsConverter(AsyncQuery aQuery) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                if (source != null)
                {
                    ArrayList<tags> ret = new ArrayList<tags>();
                    for (tags tags : (ArrayList<tags>) source)
                    {
                        if (tags.gettype() == TagsType.GeneralTag)
                        {
                            ret.add(tags);
                        }
                    }
                    return ret;
                }

                return new tags();
            }
        };
    }

    public static void GetAttachedTagsToVm(AsyncQuery aQuery, Guid id) {
        SetAttachedTagsConverter(aQuery);

        Frontend.RunQuery(VdcQueryType.GetTagsByVmId, new GetTagsByVmIdParameters(id.toString()), aQuery);
    }

    public static void GetAttachedTagsToUser(AsyncQuery aQuery, Guid id) {
        SetAttachedTagsConverter(aQuery);

        Frontend.RunQuery(VdcQueryType.GetTagsByUserId, new GetTagsByUserIdParameters(id.toString()), aQuery);
    }

    public static void GetAttachedTagsToUserGroup(AsyncQuery aQuery, Guid id) {
        SetAttachedTagsConverter(aQuery);

        Frontend.RunQuery(VdcQueryType.GetTagsByUserGroupId, new GetTagsByUserGroupIdParameters(id.toString()), aQuery);
    }

    public static void GetAttachedTagsToHost(AsyncQuery aQuery, Guid id) {
        SetAttachedTagsConverter(aQuery);

        Frontend.RunQuery(VdcQueryType.GetTagsByVdsId, new GetTagsByVdsIdParameters(id.toString()), aQuery);
    }

    public static void GetoVirtISOsList(AsyncQuery aQuery, Guid id) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                return source != null ? new ArrayList<RpmVersion>((ArrayList<RpmVersion>) source)
                        : new ArrayList<RpmVersion>();
            }
        };
        Frontend.RunQuery(VdcQueryType.GetoVirtISOs, new VdsIdParametersBase(id), aQuery);
    }

    public static void GetLunsByVgId(AsyncQuery aQuery, String vgId, Guid vdsId) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                return source != null ? (ArrayList<LUNs>) source : new ArrayList<LUNs>();
            }
        };
        GetLunsByVgIdParameters params = new GetLunsByVgIdParameters(vgId, vdsId);
        Frontend.RunQuery(VdcQueryType.GetLunsByVgId, params, aQuery);
    }

    public static void GetAllTemplatesFromExportDomain(AsyncQuery aQuery, Guid storagePoolId, Guid storageDomainId) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                return source != null ? source : new HashMap<VmTemplate, ArrayList<DiskImage>>();
            }
        };
        GetAllFromExportDomainQueryParameters getAllFromExportDomainQueryParamenters =
                new GetAllFromExportDomainQueryParameters(storagePoolId, storageDomainId);
        Frontend.RunQuery(VdcQueryType.GetTemplatesFromExportDomain, getAllFromExportDomainQueryParamenters, aQuery);
    }

    public static void GetUpHostListByCluster(AsyncQuery aQuery, String clusterName) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                if (source != null)
                {
                    ArrayList<VDS> list = Linq.<VDS> Cast((ArrayList<IVdcQueryable>) source);
                    return list;
                }

                return new ArrayList<VDS>();
            }
        };

        Frontend.RunQuery(VdcQueryType.Search, new SearchParameters("Host: cluster = " + clusterName //$NON-NLS-1$
                + " and status = up", SearchType.VDS), aQuery); //$NON-NLS-1$
    }

    public static void GetClusterListByStorageDomain(AsyncQuery _AsyncQuery,
            Guid storageDomainId) {
        Frontend.RunQuery(VdcQueryType.GetStoragePoolsByStorageDomainId,
                new StorageDomainQueryParametersBase(storageDomainId),
                new AsyncQuery(_AsyncQuery, new INewAsyncCallback() {

                    @Override
                    public void OnSuccess(Object model, Object returnValue) {
                        ArrayList<storage_pool> pools =
                                (ArrayList<storage_pool>) ((VdcQueryReturnValue) returnValue).getReturnValue();
                        if (pools != null && pools.size() > 0) {
                            storage_pool pool = pools.get(0);
                            GetClusterList((AsyncQuery) model, pool.getId());
                        }
                    }
                }));
    }

    public static void GetDataDomainsListByDomain(AsyncQuery _asyncQuery,
            Guid storageDomainId) {
        GetDataCentersByStorageDomain(new AsyncQuery(_asyncQuery,
                new INewAsyncCallback() {

                    @Override
                    public void OnSuccess(Object model, Object returnValue) {
                        ArrayList<storage_pool> pools = (ArrayList<storage_pool>) returnValue;
                        storage_pool pool = pools.get(0);
                        if (pool != null) {
                            GetStorageDomainList((AsyncQuery) model,
                                    pool.getId());
                        }

                    }
                }), storageDomainId);
    }

    public static void GetDiskMaxSize(AsyncQuery aQuery) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                return source != null ? ((Integer) source).intValue() : 0;
            }
        };

        GetConfigFromCache(
                new GetConfigurationValueParameters(ConfigurationValues.MaxBlockDiskSize,
                        getDefaultConfigurationVersion()),
                aQuery);
    }

    public static void GetVmNicList(AsyncQuery aQuery, Guid id) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                return source != null ? new ArrayList<VmNetworkInterface>((ArrayList<VmNetworkInterface>) source)
                        : new ArrayList<VmNetworkInterface>();
            }
        };

        Frontend.RunQuery(VdcQueryType.GetVmInterfacesByVmId, new IdQueryParameters(id), aQuery);
    }

    public static void GetVmDiskList(AsyncQuery aQuery, Guid id) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                ArrayList<Disk> list = new ArrayList<Disk>();
                if (source != null)
                {
                    Iterable listEnumerable = (Iterable) source;
                    Iterator listIterator = listEnumerable.iterator();
                    while (listIterator.hasNext())
                    {
                        list.add((Disk) listIterator.next());
                    }
                }
                return list;
            }
        };

        Frontend.RunQuery(VdcQueryType.GetAllDisksByVmId, new GetAllDisksByVmIdParameters(id), aQuery);
    }

    public static void GetVmList(AsyncQuery aQuery, String poolName) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                ArrayList<VM> vms = Linq.<VM> Cast((ArrayList<IVdcQueryable>) source);
                return vms;
            }
        };
        Frontend.RunQuery(VdcQueryType.Search, new SearchParameters("Vms: pool=" + poolName, SearchType.VM), aQuery); //$NON-NLS-1$
    }

    public static void GetVmListByClusterName(AsyncQuery aQuery, String clusterName) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                ArrayList<VM> vms = Linq.<VM> Cast((ArrayList<IVdcQueryable>) source);
                return vms;
            }
        };
        Frontend.RunQuery(VdcQueryType.Search,
                new SearchParameters("Vms: cluster=" + clusterName, SearchType.VM), aQuery); //$NON-NLS-1$
    }

    public static void GetDiskList(AsyncQuery aQuery) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                return source != null ? (ArrayList<DiskImage>) source : new ArrayList<DiskImage>();
            }
        };

        SearchParameters searchParams = new SearchParameters("Disks:", SearchType.Disk); //$NON-NLS-1$
        searchParams.setMaxCount(9999);

        Frontend.RunQuery(VdcQueryType.Search, searchParams, aQuery);
    }

    public static void GetNextAvailableDiskAliasNameByVMId(AsyncQuery aQuery, Guid vmId) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                return source;
            }
        };
        Frontend.RunQuery(VdcQueryType.GetNextAvailableDiskAliasNameByVMId,
                new GetAllDisksByVmIdParameters(vmId),
                aQuery);
    }

    public static void IsPoolNameUnique(AsyncQuery aQuery, String name) {

        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                if (source != null)
                {
                    return !(Boolean) source;
                }

                return false;
            }
        };
        Frontend.RunQuery(VdcQueryType.IsVmPoolWithSameNameExists,
                new IsVmPoolWithSameNameExistsParameters(name),
                aQuery);
    }

    public static void GetVmConfigurationBySnapshot(AsyncQuery aQuery, Guid snapshotSourceId) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                return source != null ? (VM) source : null;
            }
        };

        Frontend.RunQuery(VdcQueryType.GetVmConfigurationBySnapshot,
                new GetVmConfigurationBySnapshotQueryParams(snapshotSourceId),
                aQuery);
    }

    public static void GetDocumentationBaseURL(AsyncQuery aQuery) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                return source != null ? (String) source : ""; //$NON-NLS-1$
            }
        };
        GetConfigFromCache(
                new GetConfigurationValueParameters(ConfigurationValues.DocsURL, getDefaultConfigurationVersion()),
                aQuery);
    }

    public static void IsHotPlugAvailable(AsyncQuery aQuery, String version) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                return source != null ? ((Boolean) source).booleanValue() : true;
            }
        };
        GetConfigurationValueParameters tempVar =
                new GetConfigurationValueParameters(ConfigurationValues.HotPlugEnabled);
        tempVar.setVersion(version);
        GetConfigFromCache(tempVar, aQuery);
    }

    public static void IsDirectLunDiskEnabled(AsyncQuery aQuery, String version) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                return source != null ? ((Boolean) source).booleanValue() : true;
            }
        };
        GetConfigurationValueParameters params =
                new GetConfigurationValueParameters(ConfigurationValues.DirectLUNDiskEnabled);
        params.setVersion(version);
        GetConfigFromCache(params, aQuery);
    }

    public static void IsShareableDiskEnabled(AsyncQuery aQuery, String version) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                return source != null ? ((Boolean) source).booleanValue() : true;
            }
        };
        GetConfigurationValueParameters params =
                new GetConfigurationValueParameters(ConfigurationValues.ShareableDiskEnabled);
        params.setVersion(version);
        GetConfigFromCache(params, aQuery);
    }

    public static void IsLiveStorageMigrationEnabled(AsyncQuery aQuery, String version) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                return source != null ? ((Boolean) source).booleanValue() : true;
            }
        };
        GetConfigurationValueParameters params =
                new GetConfigurationValueParameters(ConfigurationValues.LiveStorageMigrationEnabled);
        params.setVersion(version);
        GetConfigFromCache(params, aQuery);
    }

    public static void GetAllAttachableDisks(AsyncQuery aQuery, Guid storagePoolId, Guid vmId) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                return source != null ? (ArrayList<Disk>) source : new ArrayList<Disk>();
            }
        };
        GetAllAttachableDisks params = new GetAllAttachableDisks(storagePoolId);
        params.setVmId(vmId);
        Frontend.RunQuery(VdcQueryType.GetAllAttachableDisks, params, aQuery);
    }

    public static void GetPermittedStorageDomainsByStoragePoolId(AsyncQuery aQuery,
            Guid dataCenterId,
            ActionGroup actionGroup) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                if (source == null)
                {
                    return new java.util.ArrayList<storage_domains>();
                }
                return source;
            }
        };
        GetPermittedStorageDomainsByStoragePoolIdParameters params =
                new GetPermittedStorageDomainsByStoragePoolIdParameters();

        params.setStoragePoolId(dataCenterId);
        params.setActionGroup(actionGroup);

        Frontend.RunQuery(VdcQueryType.GetPermittedStorageDomainsByStoragePoolId, params, aQuery);
    }

    public static void GetRedirectServletReportsPage(AsyncQuery aQuery) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                return source != null ? (String) source : ""; //$NON-NLS-1$
            }
        };
        GetConfigFromCache(
                new GetConfigurationValueParameters(ConfigurationValues.RedirectServletReportsPage,
                        getDefaultConfigurationVersion()),
                aQuery);
    }

    private static HashMap<VdcActionType, CommandVersionsInfo> cachedCommandsCompatibilityVersions;

    public static void IsCommandCompatible(AsyncQuery aQuery, final VdcActionType vdcActionType,
            final Version cluster, final Version dc) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                cachedCommandsCompatibilityVersions = (HashMap<VdcActionType, CommandVersionsInfo>) source;
                return IsCommandCompatible(vdcActionType, cluster, dc);
            }
        };

        if (cachedCommandsCompatibilityVersions != null) {
            aQuery.asyncCallback.OnSuccess(aQuery.getModel(), IsCommandCompatible(vdcActionType, cluster, dc));
        } else {
            Frontend.RunQuery(VdcQueryType.GetCommandsCompatibilityVersions, new VdcQueryParametersBase(), aQuery);
        }
    }

    private static boolean IsCommandCompatible(VdcActionType vdcActionType, Version cluster, Version dc) {
        if (cachedCommandsCompatibilityVersions == null || cluster == null || dc == null) {
            return false;
        }

        CommandVersionsInfo commandVersionsInfo = cachedCommandsCompatibilityVersions.get(vdcActionType);
        if (commandVersionsInfo == null) {
            return false;
        }

        Version clusterCompatibility = commandVersionsInfo.getClusterVersion();
        Version dcCompatibility = commandVersionsInfo.getStoragePoolVersion();

        return (cluster != null && clusterCompatibility.compareTo(cluster) <= 0)
                && (dc != null && dcCompatibility.compareTo(dc) <= 0);
    }

    public static CommandVersionsInfo GetCommandVersionsInfo(VdcActionType vdcActionType) {
        if (cachedCommandsCompatibilityVersions == null) {
            return null;
        }

        return cachedCommandsCompatibilityVersions.get(vdcActionType);
    }

    /**
     * Get the Management Network Name
     * @param aQuery
     *            result callback
     */
    public static void GetManagementNetworkName(AsyncQuery aQuery) {
        GetConfigFromCache(
                new GetConfigurationValueParameters(ConfigurationValues.ManagementNetwork,
                        getDefaultConfigurationVersion()),
                aQuery);
    }

    /**
     * Cache configuration values [raw (not converted) values from vdc_options table].
     */
    private static void CacheConfigValues(AsyncQuery aQuery) {
        getDefaultConfigurationVersion();
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object returnValue, AsyncQuery _asyncQuery)
            {
                if (returnValue != null) {
                    cachedConfigValuesPreConvert.putAll((HashMap<KeyValuePairCompat<ConfigurationValues, String>, Object>) returnValue);
                }
                return cachedConfigValuesPreConvert;
            }
        };
        Frontend.RunQuery(VdcQueryType.GetConfigurationValues, new VdcQueryParametersBase(), aQuery);
    }


    /**
     * Get configuration value from 'cachedConfigValuesPreConvert'
     * (raw values from vdc_options table).
     *
     * @param version
     */
    public static Object GetConfigValuePreConverted(ConfigurationValues configValue, String version) {
        KeyValuePairCompat<ConfigurationValues, String> key =
                new KeyValuePairCompat<ConfigurationValues, String>(configValue, version);

        return cachedConfigValuesPreConvert.get(key);
    }

    /**
     * Get configuration value from 'cachedConfigValuesPreConvert'
     * (raw values from vdc_options table).
     */
    public static Object GetConfigValuePreConverted(ConfigurationValues configValue) {
        KeyValuePairCompat<ConfigurationValues, String> key =
                new KeyValuePairCompat<ConfigurationValues, String>(configValue, getDefaultConfigurationVersion());

        return cachedConfigValuesPreConvert.get(key);
    }

    /**
     * Get configuration value from using a specified converter.
     */
    public static Object GetConfigValue(ConfigurationValues configValue, String version, IAsyncConverter converter) {
        if (converter == null) {
            return null;
        }

        KeyValuePairCompat<ConfigurationValues, String> key =
                new KeyValuePairCompat<ConfigurationValues, String>(configValue, version);

        return converter.Convert(cachedConfigValuesPreConvert.get(key), null);
    }

    /**
     * method to get an item from config while caching it (config is not supposed to change during a session)
     * @param aQuery
     *            an async query
     * @param parameters
     *            a converter for the async query
     */
    public static void GetConfigFromCache(GetConfigurationValueParameters parameters, AsyncQuery aQuery) {
        // cache key
        final KeyValuePairCompat<ConfigurationValues, String> config_key =
                new KeyValuePairCompat<ConfigurationValues, String>(parameters.getConfigValue(),
                        parameters.getVersion());

        Object returnValue = null;

        if (cachedConfigValues.containsKey(config_key)) {
            // cache hit
            returnValue = cachedConfigValues.get(config_key);
        }
        // cache miss: convert configuration value using query's converter
        // and call asyncCallback's OnSuccess
        else if (cachedConfigValuesPreConvert.containsKey(config_key)) {
            returnValue = cachedConfigValuesPreConvert.get(config_key);

            // run converter
            if (aQuery.converterCallback != null) {
                returnValue = aQuery.converterCallback.Convert(returnValue, aQuery);
            }
            if (returnValue != null) {
                cachedConfigValues.put(config_key, returnValue);
            }
        }
        aQuery.asyncCallback.OnSuccess(aQuery.getModel(), returnValue);
    }

    /**
     * method to get an item from config while caching it (config is not supposed to change during a session)
     * @param configValue
     *            the config value to query
     * @param version
     *            the compatibility version to query
     * @param aQuery
     *            an async query
     */
    public static void GetConfigFromCache(ConfigurationValues configValue, String version, AsyncQuery aQuery) {
        GetConfigurationValueParameters parameters = new GetConfigurationValueParameters(configValue, version);
        GetConfigFromCache(parameters, aQuery);
    }

    public static ArrayList<QuotaEnforcementTypeEnum> getQuotaEnforcmentTypes() {
        return new ArrayList<QuotaEnforcementTypeEnum>(Arrays.asList(new QuotaEnforcementTypeEnum[] {
                QuotaEnforcementTypeEnum.DISABLED,
                QuotaEnforcementTypeEnum.SOFT_ENFORCEMENT,
                QuotaEnforcementTypeEnum.HARD_ENFORCEMENT }));
    }

    public static void clearCache() {
        cachedConfigValues.clear();
    }

    private static class TemplateConverter implements IAsyncConverter {

        @Override
        public Object Convert(Object source, AsyncQuery asyncQuery) {
            List<VmTemplate> list = new ArrayList<VmTemplate>();
            if (source != null) {
                VmTemplate blankTemplate = null;
                for (VmTemplate template : (List<VmTemplate>) source) {
                    if (template.getId().equals(Guid.Empty)) {
                        blankTemplate = template;
                    } else if (template.getstatus() == VmTemplateStatus.OK) {
                        list.add(template);
                    }
                }

                Collections.sort(list, new Linq.VmTemplateByNameComparer());
                if (blankTemplate != null) {
                    list.add(0, blankTemplate);
                }
            }

            return list;
        }
    }

    public static void GetInterfaceOptionsForEditNetwork(final AsyncQuery asyncQuery, final ArrayList<VdsNetworkInterface> interfaceList,
            final VdsNetworkInterface originalInterface,
            Network networkToEdit,
            final Guid vdsID,
            final StringBuilder defaultInterfaceName)
    {
        final ArrayList<VdsNetworkInterface> ifacesOptions = new ArrayList<VdsNetworkInterface>();
        for (VdsNetworkInterface i : interfaceList)
        {
            if (StringHelper.isNullOrEmpty(i.getNetworkName()) && StringHelper.isNullOrEmpty(i.getBondName()))
            {
                ifacesOptions.add(i);
            }
        }

        if (originalInterface.getVlanId() == null) // no vlan:
        {
            // Filter out the Interfaces that have child vlan Interfaces

            ArrayList<InterfaceAndIdQueryParameters> parametersList =
                    new ArrayList<InterfaceAndIdQueryParameters>();
            ArrayList<VdcQueryType> queryTypeList = new ArrayList<VdcQueryType>();
            GetAllChildVlanInterfaces(vdsID, ifacesOptions, new IFrontendMultipleQueryAsyncCallback() {

                @Override
                public void Executed(FrontendMultipleQueryAsyncResult result) {

                    ArrayList<VdsNetworkInterface> ifacesOptionsTemp = new ArrayList<VdsNetworkInterface>();
                    List<VdcQueryReturnValue> returnValueList = result.getReturnValues();

                    for (int i = 0; i < returnValueList.size(); i++)
                    {
                        VdcQueryReturnValue returnValue = returnValueList.get(i);
                        ArrayList<VdsNetworkInterface> childVlanInterfaces = new ArrayList<VdsNetworkInterface>();
                        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
                        {
                            childVlanInterfaces = (ArrayList<VdsNetworkInterface>) (returnValue.getReturnValue());

                            if (childVlanInterfaces.size() == 0)
                            {
                                ifacesOptionsTemp.add(ifacesOptions.get(i));
                            }
                        }
                    }

                    ifacesOptions.clear();
                    ifacesOptions.addAll(ifacesOptionsTemp);

                    if (originalInterface.getBonded() != null && originalInterface.getBonded())
                    {
                        // eth0 -- \
                        // |---> bond0 -> <networkToEdit>
                        // eth1 -- /
                        // ---------------------------------------
                        // - originalInterface: 'bond0'
                        // --> We want to add 'eth0' and and 'eth1' as optional Interfaces
                        // (note that choosing one of them will break the bond):
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
                    defaultInterfaceName.append(originalInterface.getName());

                    asyncQuery.asyncCallback.OnSuccess(asyncQuery.Model, ifacesOptions);
                }
            });



        }

        else // vlan:
        {
            GetVlanParentInterface(vdsID, originalInterface, new AsyncQuery(asyncQuery, new INewAsyncCallback() {

                @Override
                public void OnSuccess(Object model, Object returnValue) {
                    final VdsNetworkInterface vlanParent = (VdsNetworkInterface) returnValue;

                    if (vlanParent != null && vlanParent.getBonded() != null && vlanParent.getBonded()){
                        InterfaceHasSiblingVlanInterfaces(vdsID, originalInterface, new AsyncQuery(asyncQuery, new INewAsyncCallback() {

                            @Override
                            public void OnSuccess(Object model, Object returnValue) {
                                Boolean interfaceHasSiblingVlanInterfaces = (Boolean) returnValue;

                                if (!interfaceHasSiblingVlanInterfaces){
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
                                defaultInterfaceName.append(vlanParent.getName());

                                asyncQuery.asyncCallback.OnSuccess(asyncQuery.Model, ifacesOptions);

                            }
                        }));
                    }else{
                        // the vlanParent should already be in ifacesOptions
                        // (since it has no network_name or bond_name).
                        if (vlanParent != null)
                            defaultInterfaceName.append(vlanParent.getName());
                        asyncQuery.asyncCallback.OnSuccess(asyncQuery.Model, ifacesOptions);
                    }
                }
           }));
        }
}


    private static void GetVlanParentInterface(Guid vdsID, VdsNetworkInterface iface, AsyncQuery aQuery)
    {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                return source;
            }
        };
        Frontend.RunQuery(VdcQueryType.GetVlanParent, new InterfaceAndIdQueryParameters(vdsID,
                iface), aQuery);
    }

    private static void InterfaceHasSiblingVlanInterfaces(Guid vdsID, VdsNetworkInterface iface, AsyncQuery aQuery)
    {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                ArrayList<VdsNetworkInterface> siblingVlanInterfaces = new ArrayList<VdsNetworkInterface>();

                siblingVlanInterfaces = (ArrayList<VdsNetworkInterface>) source;

                if (siblingVlanInterfaces.size() > 0)
                {
                    return true;
                }

                return false;
            }
        };
        Frontend.RunQuery(VdcQueryType.GetAllSiblingVlanInterfaces,
                new InterfaceAndIdQueryParameters(vdsID, iface), aQuery);

    }

    private static void GetAllChildVlanInterfaces(Guid vdsID, List<VdsNetworkInterface> ifaces, IFrontendMultipleQueryAsyncCallback callback)
    {
        ArrayList<VdcQueryParametersBase> parametersList = new ArrayList<VdcQueryParametersBase>();
        ArrayList<VdcQueryType> queryTypeList = new ArrayList<VdcQueryType>();
        for (final VdsNetworkInterface iface : ifaces)
        {
            queryTypeList.add(VdcQueryType.GetAllChildVlanInterfaces);
            parametersList.add(new InterfaceAndIdQueryParameters(vdsID, iface));
        }
        Frontend.RunMultipleQueries(queryTypeList, parametersList, callback);
    }

    public static void IsSupportBridgesReportByVDSM(AsyncQuery aQuery, String version) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                return source != null ? ((Boolean) source).booleanValue() : true;
            }
        };
        GetConfigurationValueParameters tempVar =
                new GetConfigurationValueParameters(ConfigurationValues.SupportBridgesReportByVDSM);
        tempVar.setVersion(version);
        GetConfigFromCache(tempVar, aQuery);
    }

    public static void IsMTUOverrideSupported(AsyncQuery aQuery, String version) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                return source != null ? ((Boolean) source).booleanValue() : true;
            }
        };
        GetConfigurationValueParameters tempVar =
                new GetConfigurationValueParameters(ConfigurationValues.MTUOverrideSupported);
        tempVar.setVersion(version);
        GetConfigFromCache(tempVar, aQuery);
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

    public static ArrayList<EventNotificationEntity> GetEventNotificationTypeList()
    {
        ArrayList<EventNotificationEntity> ret = new ArrayList<EventNotificationEntity>();
        // TODO: We can translate it here too
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

    public static int GetClusterDefaultMemoryOverCommit()
    {
        return 100;
    }

    public static boolean GetClusterDefaultCountThreadsAsCores()
    {
        return false;
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
            values.add(SpiceConstantsManager.getInstance()
                    .getSpiceRedKeys()
                    .getString(s.replaceAll("-", "_"))); //$NON-NLS-1$ //$NON-NLS-2$
        }
        return StringHelper.join("+", values.toArray(new String[] {})); //$NON-NLS-1$
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
        return new Guid();
    }

    private static ArrayList<VmOsType> windowsOsTypes;

    public static ArrayList<VmOsType> GetWindowsOsTypes()
    {
        if (windowsOsTypes != null)
        {
            return windowsOsTypes;
        }

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

    private static ArrayList<VmOsType> linuxOsTypes;
    private static ArrayList<VmOsType> x64OsTypes;

    public static boolean IsLinuxOsType(VmOsType osType)
    {
        if (GetLinuxOsTypes().contains(osType))
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

    public static boolean Is64bitOsType(VmOsType osType)
    {
        if (Get64bitOsTypes().contains(osType))
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

    public static int GetMaxVmPriority()
    {
        return (Integer) GetConfigValuePreConverted(ConfigurationValues.VmPriorityMaxValue,
                getDefaultConfigurationVersion());
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

    public static void getVmGuestAgentInterfacesByVmId(AsyncQuery aQuery, Guid vmId) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                if (source == null)
                {
                    return new ArrayList<VmGuestAgentInterface>();
                }
                return source;
            }
        };
        Frontend.RunQuery(VdcQueryType.GetVmGuestAgentInterfacesByVmId, new IdQueryParameters(vmId), aQuery);
    }

}
