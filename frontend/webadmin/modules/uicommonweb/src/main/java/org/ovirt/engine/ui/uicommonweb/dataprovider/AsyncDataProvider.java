package org.ovirt.engine.ui.uicommonweb.dataprovider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.DiskImageBase;
import org.ovirt.engine.core.common.businessentities.IVdcQueryable;
import org.ovirt.engine.core.common.businessentities.LUNs;
import org.ovirt.engine.core.common.businessentities.QuotaEnforcementTypeEnum;
import org.ovirt.engine.core.common.businessentities.RepoFileMetaData;
import org.ovirt.engine.core.common.businessentities.ServerCpu;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.TagsType;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.VmTemplateStatus;
import org.ovirt.engine.core.common.businessentities.VmType;
import org.ovirt.engine.core.common.businessentities.VolumeType;
import org.ovirt.engine.core.common.businessentities.network;
import org.ovirt.engine.core.common.businessentities.permissions;
import org.ovirt.engine.core.common.businessentities.roles;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.common.businessentities.storage_server_connections;
import org.ovirt.engine.core.common.businessentities.tags;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeOptionInfo;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.core.common.queries.CommandVersionsInfo;
import org.ovirt.engine.core.common.queries.ConfigurationValues;
import org.ovirt.engine.core.common.queries.GetAllAttachableDisks;
import org.ovirt.engine.core.common.queries.GetAllDisksByVmIdParameters;
import org.ovirt.engine.core.common.queries.GetAllFromExportDomainQueryParamenters;
import org.ovirt.engine.core.common.queries.GetAllImagesListByStoragePoolIdParameters;
import org.ovirt.engine.core.common.queries.GetAllNetworkQueryParamenters;
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
import org.ovirt.engine.core.common.queries.VdsIdParametersBase;
import org.ovirt.engine.core.common.queries.gluster.GlusterParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.KeyValuePairCompat;
import org.ovirt.engine.core.compat.NGuid;
import org.ovirt.engine.core.compat.RpmVersion;
import org.ovirt.engine.core.compat.StringFormat;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.IAsyncConverter;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.DataProvider;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.models.ApplicationModeHelper;

public final class AsyncDataProvider {

    // dictionary to hold cache of all config values (per version) queried by client, if the request for them succeeded.
    private static HashMap<Map.Entry<ConfigurationValues, String>, Object> cachedConfigValues =
            new HashMap<Map.Entry<ConfigurationValues, String>, Object>();

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

    public static void IsBackendAvailable(AsyncQuery aQuery) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                return source != null;
            }
        };
        GetDomainListParameters tempVar = new GetDomainListParameters();
        tempVar.setFilterInternalDomain(true);
        Frontend.RunPublicQuery(VdcQueryType.GetDomainList, tempVar, aQuery);
    }

    public static void IsCustomPropertiesAvailable(AsyncQuery aQuery, String version) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                return source != null ? ((Boolean) source).booleanValue() : true;
            }
        };
        GetConfigurationValueParameters tempVar =
                new GetConfigurationValueParameters(ConfigurationValues.SupportCustomProperties);
        tempVar.setVersion(version);
        GetConfigFromCache(tempVar, aQuery);
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
        Frontend.RunQuery(VdcQueryType.GetTimeZones, new VdcQueryParametersBase(), aQuery);
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
                new GetConfigurationValueParameters(ConfigurationValues.VMMinMemorySizeInMB),
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
                new GetConfigurationValueParameters(ConfigurationValues.SpiceUsbAutoShare),
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
                new GetConfigurationValueParameters(ConfigurationValues.VM32BitMaxMemorySizeInMB),
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

    public static void GetRoundedPriority(AsyncQuery aQuery, int priority) {
        aQuery.setData(new Object[] { priority });
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                int max = ((Integer) source).intValue();
                int medium = max / 2;

                int[] levels = new int[] { 1, medium, max };

                for (int i = 0; i < levels.length; i++)
                {
                    int lengthToLess = levels[i] - (Integer) _asyncQuery.Data[0];
                    int lengthToMore = levels[i + 1] - (Integer) _asyncQuery.Data[0];

                    if (lengthToMore < 0)
                    {
                        continue;
                    }

                    return Math.abs(lengthToLess) < lengthToMore ? levels[i] : levels[i + 1];
                }

                return 0;
            }
        };
        GetConfigFromCache(
                new GetConfigurationValueParameters(ConfigurationValues.VmPriorityMaxValue),
                aQuery);
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
                new GetConfigurationValueParameters(ConfigurationValues.ValidNumOfMonitors),
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
                new GetConfigurationValueParameters(ConfigurationValues.VmPriorityMaxValue),
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
                        : ConfigurationValues.VM32BitMaxMemorySizeInMB),
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
        Frontend.RunPublicQuery(VdcQueryType.GetDomainList, tempVar, aQuery);
    }

    public static void GetRoleList(AsyncQuery aQuery) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                return source != null ? (ArrayList<roles>) source : new ArrayList<roles>();
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

    public static void GetDiskPresetList(AsyncQuery aQuery, VmType vmType, StorageType storageType) {
        aQuery.setData(new Object[] { vmType, storageType });
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                if (source == null)
                {
                    return null;
                }

                ArrayList<DiskImageBase> list = new ArrayList<DiskImageBase>();
                boolean hasBootDisk = false;
                for (DiskImageBase disk : (ArrayList<DiskImageBase>) source)
                {
                    if (!hasBootDisk) {
                        disk.setBoot(true);
                        hasBootDisk = true;
                    }

                    disk.setvolume_type(disk.isBoot() && (VmType) _asyncQuery.Data[0] == VmType.Desktop ?
                            VolumeType.Sparse : VolumeType.Preallocated);
                    disk.setvolume_format(DataProvider.GetDiskVolumeFormat(disk.getvolume_type(),
                            (StorageType) _asyncQuery.Data[1]));

                    list.add(disk);
                }
                return list;
            }
        };
        Frontend.RunQuery(VdcQueryType.GetDiskConfigurationList, new VdcQueryParametersBase(), aQuery);
    }

    public static void GetClusterNetworkList(AsyncQuery aQuery, Guid clusterId) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                if (source == null)
                {
                    return new ArrayList<network>();
                }
                return source;
            }
        };
        Frontend.RunQuery(VdcQueryType.GetAllNetworksByClusterId, new VdsGroupQueryParamenters(clusterId), aQuery);
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
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                if (source != null)
                {
                    ArrayList<GlusterVolumeOptionInfo> list =
                            new ArrayList<GlusterVolumeOptionInfo>((Set<GlusterVolumeOptionInfo>) source);

                    return list;
                }

                return new ArrayList<GlusterVolumeOptionInfo>();
            }
        };
        Frontend.RunQuery(VdcQueryType.GetGlusterVolumeOptionsInfo, new GlusterParameters(clusterId), aQuery);
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
                new GetConfigurationValueParameters(ConfigurationValues.ProductRPMVersion),
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
                new GetConfigurationValueParameters(ConfigurationValues.SearchResultsLimit),
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
                new GetConfigurationValueParameters(ConfigurationValues.SANWipeAfterDelete),
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

    public static void IsUSBEnabledByDefault(AsyncQuery aQuery) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                return source != null ? ((Boolean) source).booleanValue() : false;
            }
        };
        GetConfigFromCache(
                new GetConfigurationValueParameters(ConfigurationValues.EnableUSBAsDefault),
                aQuery);
    }

    public static void GetStorageConnectionById(AsyncQuery aQuery, String id, boolean isRefresh) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                return source != null ? (storage_server_connections) source : null;
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
                new GetConfigurationValueParameters(ConfigurationValues.StoragePoolNameSizeLimit),
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
                new GetConfigurationValueParameters(ConfigurationValues.MaxVdsMemOverCommitForServers),
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
                new GetConfigurationValueParameters(ConfigurationValues.MaxVdsMemOverCommit),
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
        tempVar.setVersion(version != null ? version.toString() : null);
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
                return source != null ? (ArrayList<network>) source : new ArrayList<network>();
            }
        };

        Frontend.RunQuery(VdcQueryType.GetAllNetworks, new GetAllNetworkQueryParamenters(dataCenterId), aQuery);
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
        SearchParameters sp =
                new SearchParameters(StringFormat.format("hosts: datacenter=%1$s", dataCenterName), SearchType.VDS); //$NON-NLS-1$
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
                new GetConfigurationValueParameters(ConfigurationValues.RhevhLocalFSPath),
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
                new GetConfigurationValueParameters(ConfigurationValues.StorageDomainNameSizeLimit),
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
        Frontend.RunQuery(VdcQueryType.Search, new SearchParameters(StringFormat.format("Storage: name=%1$s", name), //$NON-NLS-1$
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
                new GetConfigurationValueParameters(ConfigurationValues.HighUtilizationForEvenlyDistribute),
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
                new GetConfigurationValueParameters(ConfigurationValues.LowUtilizationForPowerSave),
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
                new GetConfigurationValueParameters(ConfigurationValues.HighUtilizationForPowerSave),
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
        // new GetConfigurationValueParameters(ConfigurationValues.HighUtilizationForPowerSave),
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
        // new GetConfigurationValueParameters(ConfigurationValues.HighUtilizationForPowerSave),
        // aQuery);

        aQuery.asyncCallback.OnSuccess(aQuery.getModel(), 5);
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
                        DataProvider.fillTagsRecursive(root, tag.getChildren());
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

    public static void GetLunsByVgId(AsyncQuery aQuery, String vgId) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                return source != null ? (ArrayList<LUNs>) source : new ArrayList<LUNs>();
            }
        };
        GetLunsByVgIdParameters tempVar = new GetLunsByVgIdParameters();
        tempVar.setVgId(vgId);
        Frontend.RunQuery(VdcQueryType.GetLunsByVgId, tempVar, aQuery);
    }

    public static void GetAllTemplatesFromExportDomain(AsyncQuery aQuery, Guid storagePoolId, Guid storageDomainId) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                return source != null ? source : new HashMap<VmTemplate, ArrayList<DiskImage>>();
            }
        };
        GetAllFromExportDomainQueryParamenters getAllFromExportDomainQueryParamenters =
                new GetAllFromExportDomainQueryParamenters(storagePoolId, storageDomainId);
        getAllFromExportDomainQueryParamenters.setGetAll(true);
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
                return source != null ? ((Integer) source).intValue() : 2047;
            }
        };

        GetConfigFromCache(
                new GetConfigurationValueParameters(ConfigurationValues.MaxDiskSize),
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

        Frontend.RunQuery(VdcQueryType.GetVmInterfacesByVmId, new GetVmByVmIdParameters(id), aQuery);
    }

    public static void GetVmDiskList(AsyncQuery aQuery, Guid id) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                ArrayList<DiskImage> list = new ArrayList<DiskImage>();
                if (source != null)
                {
                    Iterable listEnumerable = (Iterable) source;
                    Iterator listIterator = listEnumerable.iterator();
                    while (listIterator.hasNext())
                    {
                        list.add((DiskImage) listIterator.next());
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
                new GetConfigurationValueParameters(ConfigurationValues.DocsURL),
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

    public static void GetAllAttachableDisks(AsyncQuery aQuery, Guid storagePoolId) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                return source != null ? (ArrayList<Disk>) source : new ArrayList<Disk>();
            }
        };

        Frontend.RunQuery(VdcQueryType.GetAllAttachableDisks, new GetAllAttachableDisks(storagePoolId), aQuery);
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
                new GetConfigurationValueParameters(ConfigurationValues.RedirectServletReportsPage),
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
        GetConfigFromCache(ConfigurationValues.ManagementNetwork, aQuery);
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
        final Map.Entry<ConfigurationValues, String> config_key =
                new KeyValuePairCompat<ConfigurationValues, String>(parameters.getConfigValue(),
                        parameters.getVersion());

        if (cachedConfigValues.containsKey(config_key)) {
            // Cache hit
            Object cached = cachedConfigValues.get(config_key);
            // return result
            if (cached != null) {
                aQuery.asyncCallback.OnSuccess(aQuery.getModel(), cached);
                return;
            }
        }

        // save original converter
        final IAsyncConverter origConverter = aQuery.converterCallback;

        // Cache miss: run the query and replace the converter to cache the results
        aQuery.converterCallback = new IAsyncConverter() {

            @Override
            public Object Convert(Object returnValue, AsyncQuery asyncQuery) {
                // run original converter
                if (origConverter != null) {
                    returnValue = origConverter.Convert(returnValue, asyncQuery);
                }
                if (returnValue != null) {
                    cachedConfigValues.put(config_key, returnValue);
                }
                return returnValue;
            }
        };

        // run query
        Frontend.RunQuery(VdcQueryType.GetConfigurationValue, parameters, aQuery);
    }

    /**
     * method to get an item from config while caching it (config is not supposed to change during a session)
     * @param aQuery
     *            an async query
     * @param configValue
     *            the config value to query
     */
    public static void GetConfigFromCache(ConfigurationValues configValue, AsyncQuery aQuery) {
        GetConfigFromCache(new GetConfigurationValueParameters(configValue), aQuery);
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
}
