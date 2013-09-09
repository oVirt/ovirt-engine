package org.ovirt.engine.ui.uicommonweb.dataprovider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.EventNotificationEntity;
import org.ovirt.engine.core.common.TimeZoneType;
import org.ovirt.engine.core.common.VdcEventNotificationUtils;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.DbUser;
import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.DiskInterface;
import org.ovirt.engine.core.common.businessentities.IVdcQueryable;
import org.ovirt.engine.core.common.businessentities.ImageFileType;
import org.ovirt.engine.core.common.businessentities.LUNs;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.ProviderType;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.QuotaEnforcementTypeEnum;
import org.ovirt.engine.core.common.businessentities.RepoImage;
import org.ovirt.engine.core.common.businessentities.Role;
import org.ovirt.engine.core.common.businessentities.ServerCpu;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.TagsType;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmGuestAgentInterface;
import org.ovirt.engine.core.common.businessentities.VmPool;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.VmTemplateStatus;
import org.ovirt.engine.core.common.businessentities.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.VolumeType;
import org.ovirt.engine.core.common.businessentities.permissions;
import org.ovirt.engine.core.common.businessentities.tags;
import org.ovirt.engine.core.common.businessentities.comparators.NameableComparator;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterClusterService;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterHookEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterServerService;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.businessentities.gluster.ServiceType;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VmInterfaceType;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VnicProfile;
import org.ovirt.engine.core.common.businessentities.network.VnicProfileView;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.core.common.queries.CommandVersionsInfo;
import org.ovirt.engine.core.common.queries.ConfigurationValues;
import org.ovirt.engine.core.common.queries.GetAllAttachableDisks;
import org.ovirt.engine.core.common.queries.GetAllFromExportDomainQueryParameters;
import org.ovirt.engine.core.common.queries.GetAllProvidersParameters;
import org.ovirt.engine.core.common.queries.GetAllServerCpuListParameters;
import org.ovirt.engine.core.common.queries.GetConfigurationValueParameters;
import org.ovirt.engine.core.common.queries.GetDataCentersWithPermittedActionOnClustersParameters;
import org.ovirt.engine.core.common.queries.GetDomainListParameters;
import org.ovirt.engine.core.common.queries.GetEntitiesWithPermittedActionParameters;
import org.ovirt.engine.core.common.queries.GetExistingStorageDomainListParameters;
import org.ovirt.engine.core.common.queries.GetHostListFromExternalProviderParameters;
import org.ovirt.engine.core.common.queries.GetHostsForStorageOperationParameters;
import org.ovirt.engine.core.common.queries.GetImagesListByStoragePoolIdParameters;
import org.ovirt.engine.core.common.queries.GetLunsByVgIdParameters;
import org.ovirt.engine.core.common.queries.GetPermittedStorageDomainsByStoragePoolIdParameters;
import org.ovirt.engine.core.common.queries.GetStorageDomainsByConnectionParameters;
import org.ovirt.engine.core.common.queries.GetStoragePoolsByClusterServiceParameters;
import org.ovirt.engine.core.common.queries.GetTagsByUserGroupIdParameters;
import org.ovirt.engine.core.common.queries.GetTagsByUserIdParameters;
import org.ovirt.engine.core.common.queries.GetTagsByVdsIdParameters;
import org.ovirt.engine.core.common.queries.GetTagsByVmIdParameters;
import org.ovirt.engine.core.common.queries.GetVmTemplateParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.InterfaceAndIdQueryParameters;
import org.ovirt.engine.core.common.queries.MultilevelAdministrationsQueriesParameters;
import org.ovirt.engine.core.common.queries.NameQueryParameters;
import org.ovirt.engine.core.common.queries.OsQueryParameters;
import org.ovirt.engine.core.common.queries.OsQueryParameters.OsRepositoryVerb;
import org.ovirt.engine.core.common.queries.ProviderQueryParameters;
import org.ovirt.engine.core.common.queries.SearchParameters;
import org.ovirt.engine.core.common.queries.ServerParameters;
import org.ovirt.engine.core.common.queries.StorageServerConnectionQueryParametersBase;
import org.ovirt.engine.core.common.queries.TimeZoneQueryParams;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.queries.VdsIdParametersBase;
import org.ovirt.engine.core.common.queries.gluster.AddedGlusterServersParameters;
import org.ovirt.engine.core.common.queries.gluster.GlusterHookContentQueryParameters;
import org.ovirt.engine.core.common.queries.gluster.GlusterHookQueryParameters;
import org.ovirt.engine.core.common.queries.gluster.GlusterParameters;
import org.ovirt.engine.core.common.queries.gluster.GlusterServersQueryParameters;
import org.ovirt.engine.core.common.queries.gluster.GlusterServiceQueryParameters;
import org.ovirt.engine.core.common.queries.gluster.GlusterVolumeAdvancedDetailsParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.IntegerCompat;
import org.ovirt.engine.core.compat.KeyValuePairCompat;
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
import org.ovirt.engine.ui.uicommonweb.models.vms.WANDisableEffects;
import org.ovirt.engine.ui.uicommonweb.models.vms.WanColorDepth;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.EventArgs;
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

    // cached OS names
    private static HashMap<Integer, String> osNames;

    // cached list of os ids
    private static List<Integer> osIds;

    // cached unique OS names
    private static HashMap<Integer, String> uniqueOsNames;

    // cached linux OS
    private static List<Integer> linuxOsIds;
    // cached windows OS
    private static List<Integer> windowsOsIds;

    public static String getDefaultConfigurationVersion() {
        return _defaultConfigurationVersion;
    }

    private static void getDefaultConfigurationVersion(Object target) {
        AsyncQuery callback = new AsyncQuery(target, new INewAsyncCallback() {

            @Override
            public void onSuccess(Object model, Object returnValue) {
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
        AsyncDataProvider.cacheConfigValues(new AsyncQuery(loginModel, new INewAsyncCallback() {
            @Override
            public void onSuccess(Object target, Object returnValue) {
                getDefaultConfigurationVersion(target);
            }
        }));
        initOsNames();
        initUniqueOsNames();
        initLinuxOsTypes();
        initWindowsOsTypes();
    }

    public static void getDomainListViaPublic(AsyncQuery aQuery, boolean filterInternalDomain) {
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

    public static void getIsoDomainByDataCenterId(AsyncQuery aQuery, Guid dataCenterId) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                if (source != null)
                {
                    ArrayList<StorageDomain> storageDomains = (ArrayList<StorageDomain>) source;
                    for (StorageDomain domain : storageDomains)
                    {
                        if (domain.getStorageDomainType() == StorageDomainType.ISO)
                        {
                            return domain;
                        }
                    }
                }

                return null;
            }
        };

        IdQueryParameters getIsoParams = new IdQueryParameters(dataCenterId);
        Frontend.RunQuery(VdcQueryType.GetStorageDomainsByStoragePoolId, getIsoParams, aQuery);
    }

    public static void getExportDomainByDataCenterId(AsyncQuery aQuery, Guid dataCenterId) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                ArrayList<StorageDomain> storageDomains = (ArrayList<StorageDomain>) source;
                for (StorageDomain domain : storageDomains)
                {
                    if (domain.getStorageDomainType() == StorageDomainType.ImportExport)
                    {
                        return domain;
                    }
                }

                return null;
            }
        };

        IdQueryParameters getExportParams = new IdQueryParameters(dataCenterId);
        Frontend.RunQuery(VdcQueryType.GetStorageDomainsByStoragePoolId, getExportParams, aQuery);
    }

    public static void getIrsImageList(AsyncQuery aQuery, Guid storagePoolId) {
        getIrsImageList(aQuery, storagePoolId, false);
    }

    public static void getIrsImageList(AsyncQuery aQuery, Guid storagePoolId, boolean forceRefresh) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                if (source != null)
                {
                    ArrayList<RepoImage> repoList = (ArrayList<RepoImage>) source;
                    ArrayList<String> fileNameList = new ArrayList<String>();
                    for (RepoImage repoImage : repoList)
                    {
                        fileNameList.add(repoImage.getRepoImageId());
                    }

                    Collections.sort(fileNameList, String.CASE_INSENSITIVE_ORDER);
                    return fileNameList;
                }
                return new ArrayList<String>();
            }
        };

        GetImagesListByStoragePoolIdParameters parameters =
                new GetImagesListByStoragePoolIdParameters(storagePoolId, ImageFileType.ISO);
        parameters.setForceRefresh(forceRefresh);
        Frontend.RunQuery(VdcQueryType.GetImagesListByStoragePoolId, parameters, aQuery);
    }

    public static void getFloppyImageList(AsyncQuery aQuery, Guid storagePoolId) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                if (source != null)
                {
                    ArrayList<RepoImage> repoList = (ArrayList<RepoImage>) source;
                    ArrayList<String> fileNameList = new ArrayList<String>();
                    for (RepoImage repoImage : repoList)
                    {
                        fileNameList.add(repoImage.getRepoImageId());
                    }

                    Collections.sort(fileNameList, String.CASE_INSENSITIVE_ORDER);
                    return fileNameList;
                }
                return new ArrayList<String>();
            }
        };

        Frontend.RunQuery(VdcQueryType.GetImagesListByStoragePoolId,
                new GetImagesListByStoragePoolIdParameters(storagePoolId, ImageFileType.Floppy),
                aQuery);
    }

    public static void getClusterById(AsyncQuery aQuery, Guid id) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                return source;
            }
        };
        Frontend.RunQuery(VdcQueryType.GetVdsGroupById, new IdQueryParameters(id), aQuery);
    }

    public static void getClusterListByName(AsyncQuery aQuery, String name) {
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

    public static void getPoolById(AsyncQuery aQuery, Guid poolId) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                return source;
            }
        };
        Frontend.RunQuery(VdcQueryType.GetVmPoolById, new IdQueryParameters(poolId), aQuery);
    }

    public static void getVmById(AsyncQuery aQuery, Guid vmId) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                return source;
            }
        };
        Frontend.RunQuery(VdcQueryType.GetVmByVmId, new IdQueryParameters(vmId), aQuery);
    }

    public static void getTimeZoneList(AsyncQuery aQuery, TimeZoneType timeZoneType) {
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
        params.setTimeZoneType(timeZoneType);
        Frontend.RunQuery(VdcQueryType.GetTimeZones, params, aQuery);
    }

    public static void getDataCenterList(AsyncQuery aQuery) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                if (source == null)
                {
                    return new ArrayList<StoragePool>();
                }
                return source;
            }
        };
        Frontend.RunQuery(VdcQueryType.Search,
                new SearchParameters("DataCenter: sortby name", SearchType.StoragePool), //$NON-NLS-1$
                aQuery);
    }

    public static void getDataCenterByClusterServiceList(AsyncQuery aQuery,
            boolean supportsVirtService,
            boolean supportsGlusterService) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery) {
                if (source == null) {
                    return new ArrayList<StoragePool>();
                }

                // sort data centers
                final ArrayList<StoragePool> storagePoolList = (ArrayList<StoragePool>) source;
                Collections.sort(storagePoolList, new NameableComparator());
                return source;
            }
        };

        final GetStoragePoolsByClusterServiceParameters parameters = new GetStoragePoolsByClusterServiceParameters();
        parameters.setSupportsVirtService(supportsVirtService);
        parameters.setSupportsGlusterService(supportsGlusterService);

        Frontend.RunQuery(VdcQueryType.GetStoragePoolsByClusterService, parameters, aQuery);
    }

    public static void getDataCenterListByName(AsyncQuery aQuery, String name) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                if (source == null)
                {
                    return new ArrayList<StoragePool>();
                }
                return source;
            }
        };
        Frontend.RunQuery(VdcQueryType.Search,
                new SearchParameters("DataCenter: name=" + name + " sortby name", SearchType.StoragePool), //$NON-NLS-1$ //$NON-NLS-2$
                aQuery);
    }

    public static void getMinimalVmMemSize(AsyncQuery aQuery) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                return source != null ? ((Integer) source).intValue() : 1;
            }
        };
        getConfigFromCache(
                new GetConfigurationValueParameters(ConfigurationValues.VMMinMemorySizeInMB,
                        getDefaultConfigurationVersion()),
                aQuery);
    }

    public static void getSpiceUsbAutoShare(AsyncQuery aQuery) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                return source != null ? ((Boolean) source).booleanValue() : true;
            }
        };
        getConfigFromCache(
                new GetConfigurationValueParameters(ConfigurationValues.SpiceUsbAutoShare,
                        getDefaultConfigurationVersion()),
                aQuery);
    }

    public static void getWANColorDepth(AsyncQuery aQuery) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery) {
                return source != null ? WanColorDepth.fromInt(((Integer) source).intValue()) : WanColorDepth.depth16;
            }
        };
        getConfigFromCache(
                new GetConfigurationValueParameters(ConfigurationValues.WANColorDepth, getDefaultConfigurationVersion()),
                aQuery);
    }

    public static void getWANDisableEffects(AsyncQuery aQuery) {
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
        getConfigFromCache(
                new GetConfigurationValueParameters(ConfigurationValues.WANDisableEffects,
                        getDefaultConfigurationVersion()),
                aQuery);
    }

    public static void getMaximalVmMemSize64OS(AsyncQuery aQuery, String version) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                // we should detect missing config values instead of putting in obsolete hardcoded values
                return source != null ? ((Integer) source).intValue() : -1;
            }
        };
        GetConfigurationValueParameters tempVar =
                new GetConfigurationValueParameters(ConfigurationValues.VM64BitMaxMemorySizeInMB);
        tempVar.setVersion(version);
        getConfigFromCache(tempVar, aQuery);
    }

    public static void getMaximalVmMemSize32OS(AsyncQuery aQuery) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                return source != null ? ((Integer) source).intValue() : 20480;
            }
        };
        getConfigFromCache(
                new GetConfigurationValueParameters(ConfigurationValues.VM32BitMaxMemorySizeInMB,
                        getDefaultConfigurationVersion()),
                aQuery);
    }

    public static void getMaxVmsInPool(AsyncQuery aQuery) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                return source != null ? ((Integer) source).intValue() : 1000;
            }
        };
        getConfigFromCache(
                new GetConfigurationValueParameters(ConfigurationValues.MaxVmsInPool, getDefaultConfigurationVersion()),
                aQuery);
    }

    public static void getMaxNumOfVmSockets(AsyncQuery aQuery, String version) {
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
        getConfigFromCache(tempVar, aQuery);
    }

    public static void getMaxNumOfVmCpus(AsyncQuery aQuery, String version) {
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
        getConfigFromCache(tempVar, aQuery);
    }

    public static void getMaxNumOfCPUsPerSocket(AsyncQuery aQuery, String version) {
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
        getConfigFromCache(tempVar, aQuery);
    }

    public static void getClusterList(AsyncQuery aQuery, Guid dataCenterId) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                if (source != null)
                {
                    ArrayList<VDSGroup> list = (ArrayList<VDSGroup>) source;
                    Collections.sort(list, new NameableComparator());
                    return list;
                }
                return new ArrayList<VDSGroup>();
            }
        };
        Frontend.RunQuery(VdcQueryType.GetVdsGroupsByStoragePoolId,
                new IdQueryParameters(dataCenterId),
                aQuery);
    }

    public static void getClusterByServiceList(AsyncQuery aQuery, Guid dataCenterId,
            final boolean supportsVirtService, final boolean supportsGlusterService) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery) {
                if (source == null) {
                    return new ArrayList<VDSGroup>();
                }
                final ArrayList<VDSGroup> list = (ArrayList<VDSGroup>) source;
                return getClusterByServiceList(list, supportsVirtService, supportsGlusterService);
            }
        };
        Frontend.RunQuery(VdcQueryType.GetVdsGroupsByStoragePoolId,
                new IdQueryParameters(dataCenterId),
                aQuery);
    }

    public static void isSoundcardEnabled(AsyncQuery aQuery, Guid vmId) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery) {
                if (source != null) {
                    return ((List<String>) source).size() > 0;
                }

                return false;
            }
        };
        Frontend.RunQuery(VdcQueryType.GetSoundDevices, new IdQueryParameters(vmId), aQuery);
    }

    public static void getClusterListByService(AsyncQuery aQuery, final boolean supportsVirtService,
            final boolean supportsGlusterService) {

        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                if (source != null)
                {
                    ArrayList<VDSGroup> list =
                            getClusterByServiceList((ArrayList<VDSGroup>) source,
                                    supportsVirtService,
                                    supportsGlusterService);
                    Collections.sort(list, new NameableComparator());
                    return list;
                }
                return new ArrayList<VDSGroup>();
            }
        };
        Frontend.RunQuery(VdcQueryType.GetAllVdsGroups, new VdcQueryParametersBase(), aQuery);
    }

    public static void getClusterList(AsyncQuery aQuery) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                if (source != null)
                {
                    ArrayList<VDSGroup> list = (ArrayList<VDSGroup>) source;
                    Collections.sort(list, new NameableComparator());
                    return list;
                }
                return new ArrayList<VDSGroup>();
            }
        };
        Frontend.RunQuery(VdcQueryType.GetAllVdsGroups, new VdcQueryParametersBase(), aQuery);
    }

    public static void getTemplateDiskList(AsyncQuery aQuery, Guid templateId) {
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
        Frontend.RunQuery(VdcQueryType.GetVmTemplatesDisks, new IdQueryParameters(templateId), aQuery);
    }

    /**
     * Round the priority to the closest value from n (3 for now) values
     *
     * i.e.: if priority entered is 30 and the predefined values are 1,50,100
     *
     * then the return value will be 50 (closest to 50).
     *
     * @param priority
     *            - the current priority of the vm
     * @param maxPriority
     *            - the max priority
     * @return the rounded priority
     */
    public static int getRoundedPriority(int priority, int maxPriority) {

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

    public static void getTemplateListByDataCenter(AsyncQuery aQuery, Guid dataCenterId) {
        aQuery.converterCallback = new TemplateConverter();
        Frontend.RunQuery(VdcQueryType.GetVmTemplatesByStoragePoolId,
                new IdQueryParameters(dataCenterId),
                aQuery);
    }

    public static void getTemplateListByStorage(AsyncQuery aQuery, Guid storageId) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                ArrayList<VmTemplate> list = new ArrayList<VmTemplate>();
                if (source != null)
                {
                    for (VmTemplate template : (ArrayList<VmTemplate>) source)
                    {
                        if (template.getStatus() == VmTemplateStatus.OK)
                        {
                            list.add(template);
                        }
                    }

                    Collections.sort(list, new NameableComparator());
                }

                return list;
            }
        };
        Frontend.RunQuery(VdcQueryType.GetVmTemplatesFromStorageDomain,
                new IdQueryParameters(storageId),
                aQuery);
    }

    public static void getNumOfMonitorList(AsyncQuery aQuery) {
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
        getConfigFromCache(
                new GetConfigurationValueParameters(ConfigurationValues.ValidNumOfMonitors,
                        getDefaultConfigurationVersion()),
                aQuery);
    }

    public static void getStorageDomainListByTemplate(AsyncQuery aQuery, Guid templateId) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                if (source == null)
                {
                    return new ArrayList<StorageDomain>();
                }
                return source;
            }
        };
        Frontend.RunQuery(VdcQueryType.GetStorageDomainsByVmTemplateId,
                new IdQueryParameters(templateId),
                aQuery);
    }

    public static void getStorageDomainList(AsyncQuery aQuery, Guid dataCenterId) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                if (source == null)
                {
                    return new ArrayList<StorageDomain>();
                }
                return source;
            }
        };
        Frontend.RunQuery(VdcQueryType.GetStorageDomainsByStoragePoolId,
                new IdQueryParameters(dataCenterId),
                aQuery);
    }

    public static void getMaxVmPriority(AsyncQuery aQuery) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                if (source == null)
                {
                    return 100;
                }
                return source;
            }
        };
        getConfigFromCache(
                new GetConfigurationValueParameters(ConfigurationValues.VmPriorityMaxValue,
                        getDefaultConfigurationVersion()),
                aQuery);
    }

    public static void getDefaultTimeZone(AsyncQuery aQuery, TimeZoneType timeZoneType) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                if (source != null)
                {
                    return source;
                }
                return ""; //$NON-NLS-1$
            }
        };

        TimeZoneQueryParams params = new TimeZoneQueryParams();
        params.setTimeZoneType(timeZoneType);
        Frontend.RunQuery(VdcQueryType.GetDefaultTimeZone, params, aQuery);
    }

    public static void getHostById(AsyncQuery aQuery, Guid id) {

        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery) {
                return source;
            }
        };
        Frontend.RunQuery(VdcQueryType.GetVdsByVdsId, new IdQueryParameters(id), aQuery);
    }

    public static void getHostListByCluster(AsyncQuery aQuery, String clusterName) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                if (source != null)
                {
                    ArrayList<VDS> list = Linq.<VDS> cast((ArrayList<IVdcQueryable>) source);
                    return list;
                }

                return new ArrayList<VDS>();
            }
        };
        Frontend.RunQuery(VdcQueryType.Search, new SearchParameters("Host: cluster = " + clusterName + " sortby name", //$NON-NLS-1$ //$NON-NLS-2$
                SearchType.VDS), aQuery);
    }

    public static void getHostListByDataCenter(AsyncQuery aQuery, Guid spId) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                if (source != null)
                {
                    ArrayList<VDS> list = Linq.<VDS> cast((ArrayList<IVdcQueryable>) source);
                    return list;
                }

                return new ArrayList<VDS>();
            }
        };
        Frontend.RunQuery(VdcQueryType.GetAllVdsByStoragePool, new IdQueryParameters(spId), aQuery);
    }

    public static void getVmDiskList(AsyncQuery aQuery, Guid vmId, boolean isRefresh) {
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
        IdQueryParameters params = new IdQueryParameters(vmId);
        params.setRefresh(isRefresh);
        Frontend.RunQuery(VdcQueryType.GetAllDisksByVmId, params, aQuery);
    }

    public static HashMap<Integer, String> getOsNames() {
        return osNames;
    }

    public static HashMap<Integer, String> getOsUniqueOsNames() {
        return uniqueOsNames;
    }

    public final static class GetSnapshotListQueryResult {
        private Guid privatePreviewingImage = Guid.Empty;

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

        private Guid privateVmId = Guid.Empty;

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

    public static void getMaxVmMemSize(AsyncQuery aQuery, boolean is64) {
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
        getConfigFromCache(
                new GetConfigurationValueParameters(is64 ? ConfigurationValues.VM64BitMaxMemorySizeInMB
                        : ConfigurationValues.VM32BitMaxMemorySizeInMB, getDefaultConfigurationVersion()),
                aQuery);
    }

    public static void getDomainList(AsyncQuery aQuery, boolean filterInternalDomain) {
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

    public static void getRoleList(AsyncQuery aQuery) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                return source != null ? (ArrayList<Role>) source : new ArrayList<Role>();
            }
        };
        Frontend.RunQuery(VdcQueryType.GetAllRoles, new MultilevelAdministrationsQueriesParameters(), aQuery);
    }

    public static void getStorageDomainById(AsyncQuery aQuery, Guid storageDomainId) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                return source != null ? (StorageDomain) source : null;
            }
        };
        Frontend.RunQuery(VdcQueryType.GetStorageDomainById,
                new IdQueryParameters(storageDomainId),
                aQuery);
    }

    public static VolumeFormat getDiskVolumeFormat(VolumeType volumeType, StorageType storageType) {
        if (storageType.isFileDomain()) {
            return VolumeFormat.RAW;
        } else if (storageType.isBlockDomain()) {
            switch (volumeType) {
            case Sparse:
                return VolumeFormat.COW;

            case Preallocated:
                return VolumeFormat.RAW;

            default:
                return VolumeFormat.Unassigned;
            }
        } else {
            return VolumeFormat.Unassigned;
        }
    }

    public static void getClusterNetworkList(AsyncQuery aQuery, Guid clusterId) {
        // do not replace a converter = just add if none provided
        if (aQuery.converterCallback == null) {
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
        }

        Frontend.RunQuery(VdcQueryType.GetAllNetworksByClusterId, new IdQueryParameters(clusterId), aQuery);
    }

    public static void getDataCenterById(AsyncQuery aQuery, Guid dataCenterId) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                return source;
            }
        };
        Frontend.RunQuery(VdcQueryType.GetStoragePoolById, new IdQueryParameters(dataCenterId), aQuery);
    }

    public static void GetWatchdogByVmId(AsyncQuery aQuery, Guid vmId) {
        Frontend.RunQuery(VdcQueryType.GetWatchdog, new IdQueryParameters(vmId), aQuery);
    }

    public static void getTemplateById(AsyncQuery aQuery, Guid templateId) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                return source;
            }
        };
        Frontend.RunQuery(VdcQueryType.GetVmTemplate, new GetVmTemplateParameters(templateId), aQuery);
    }

    public static void getHostList(AsyncQuery aQuery) {
        getHostListByStatus(aQuery, null);
    }

    public static void getHostListByStatus(AsyncQuery aQuery, VDSStatus status) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                if (source != null)
                {
                    ArrayList<VDS> list = Linq.<VDS> cast((Iterable) source);
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

    public static void getHostsForStorageOperation(AsyncQuery aQuery, Guid storagePoolId, boolean localFsOnly) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery) {
                if (source != null) {
                    return source;
                }

                return new ArrayList<VDS>();
            }
        };
        Frontend.RunQuery(VdcQueryType.GetHostsForStorageOperation,
                new GetHostsForStorageOperationParameters(storagePoolId, localFsOnly),
                aQuery);
    }

    public static void getVolumeList(AsyncQuery aQuery, String clusterName) {

        if ((ApplicationModeHelper.getUiMode().getValue() & ApplicationMode.GlusterOnly.getValue()) == 0) {
            aQuery.asyncCallback.onSuccess(aQuery.Model, new ArrayList<GlusterVolumeEntity>());
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

    public static void getGlusterVolumeOptionInfoList(AsyncQuery aQuery, Guid clusterId) {
        Frontend.RunQuery(VdcQueryType.GetGlusterVolumeOptionsInfo, new GlusterParameters(clusterId), aQuery);
    }

    public static void getHostFingerprint(AsyncQuery aQuery, String hostAddress) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                return source != null ? (String) source : ""; //$NON-NLS-1$
            }
        };
        Frontend.RunQuery(VdcQueryType.GetServerSSHKeyFingerprint, new ServerParameters(hostAddress), aQuery);
    }

    public static void getHostPublicKey(AsyncQuery aQuery) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                return source != null ? (String) source : ""; //$NON-NLS-1$
            }
        };
        Frontend.RunQuery(VdcQueryType.GetServerSSHPublicKey, new VdcQueryParametersBase(), aQuery);
    }

    public static void getGlusterHosts(AsyncQuery aQuery, String hostAddress, String rootPassword, String fingerprint) {
        GlusterServersQueryParameters parameters = new GlusterServersQueryParameters(hostAddress, rootPassword);
        parameters.setFingerprint(fingerprint);
        Frontend.RunQuery(VdcQueryType.GetGlusterServersForImport,
                parameters,
                aQuery);
    }

    public static void getClusterGlusterServices(AsyncQuery aQuery, Guid clusterId) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                return source;
            }
        };
        // Passing empty values for Volume and Brick to get the services of all the volumes/hosts in the cluster
        GlusterVolumeAdvancedDetailsParameters parameters =
                new GlusterVolumeAdvancedDetailsParameters(clusterId, null, null, false); //$NON-NLS-1$ //$NON-NLS-2$
        Frontend.RunQuery(VdcQueryType.GetGlusterVolumeAdvancedDetails,
                parameters,
                aQuery);
    }

    public static void getGlusterVolumeBrickDetails(AsyncQuery aQuery, Guid clusterId, Guid volumeId, Guid brickId) {
        GlusterVolumeAdvancedDetailsParameters parameters =
                new GlusterVolumeAdvancedDetailsParameters(clusterId, volumeId, brickId, true);
        Frontend.RunQuery(VdcQueryType.GetGlusterVolumeAdvancedDetails,
                parameters,
                aQuery);
    }

    public static void getGlusterHostsNewlyAdded(AsyncQuery aQuery, Guid clusterId, boolean isFingerprintRequired) {
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

    public static void isAnyHostUpInCluster(AsyncQuery aQuery, String clusterName) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                if (source != null && ((List) source).size() > 0) {
                    return true;
                }
                return false;
            }
        };
        getUpHostListByCluster(aQuery, clusterName, 1);
    }

    public static void getGlusterHooks(AsyncQuery aQuery, Guid clusterId) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                return source != null ? source : new ArrayList<GlusterHookEntity>();
            }
        };
        Frontend.RunQuery(VdcQueryType.GetGlusterHooks, new GlusterParameters(clusterId), aQuery);
    }

    public static void getGlusterHook(AsyncQuery aQuery, Guid hookId, boolean includeServerHooks) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                return source;
            }
        };
        Frontend.RunQuery(VdcQueryType.GetGlusterHookById,
                new GlusterHookQueryParameters(hookId, includeServerHooks),
                aQuery);
    }

    public static void getGlusterHookContent(AsyncQuery aQuery, Guid hookId, Guid serverId) {

        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery) {
                return source != null ? source : ""; //$NON-NLS-1$
            }
        };
        GlusterHookContentQueryParameters parameters = new GlusterHookContentQueryParameters(hookId);
        parameters.setGlusterServerId(serverId);
        Frontend.RunQuery(VdcQueryType.GetGlusterHookContent, parameters, aQuery);
    }

    public static void getGlusterSwiftServices(AsyncQuery aQuery, Guid serverId) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                return source != null ? source : new ArrayList<GlusterServerService>();
            }
        };
        Frontend.RunQuery(VdcQueryType.GetGlusterServerServicesByServerId, new GlusterServiceQueryParameters(serverId,
                ServiceType.GLUSTER_SWIFT), aQuery);
    }

    public static void getClusterGlusterSwiftService(AsyncQuery aQuery, Guid clusterId) {

        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery) {
                if (source != null) {
                    List<GlusterClusterService> serviceList = (List<GlusterClusterService>) source;
                    if (!serviceList.isEmpty()) {
                        return serviceList.get(0);
                    }
                    return null;
                }
                else {
                    return source;
                }
            }
        };
        Frontend.RunQuery(VdcQueryType.GetGlusterClusterServiceByClusterId,
                new GlusterServiceQueryParameters(clusterId,
                        ServiceType.GLUSTER_SWIFT), aQuery);
    }

    public static void getGlusterSwiftServerServices(AsyncQuery aQuery, Guid clusterId) {

        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery) {
                return source != null ? source : new ArrayList<GlusterServerService>();
            }
        };
        Frontend.RunQuery(VdcQueryType.GetGlusterServerServicesByClusterId,
                new GlusterServiceQueryParameters(clusterId,
                        ServiceType.GLUSTER_SWIFT), aQuery);
    }

    public static void getRpmVersionViaPublic(AsyncQuery aQuery) {
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

    public static void getUserMessageOfTheDayViaPublic(AsyncQuery aQuery) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                return source != null ? (String) source : ""; //$NON-NLS-1$
            }
        };
        Frontend.RunPublicQuery(VdcQueryType.GetConfigurationValue,
                new GetConfigurationValueParameters(ConfigurationValues.UserMessageOfTheDay,
                        getDefaultConfigurationVersion()),
                aQuery);
    }

    public static void getSearchResultsLimit(AsyncQuery aQuery) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                return source != null ? ((Integer) source).intValue() : 100;
            }
        };
        getConfigFromCache(
                new GetConfigurationValueParameters(ConfigurationValues.SearchResultsLimit,
                        getDefaultConfigurationVersion()),
                aQuery);
    }

    public static void getCustomPropertiesList(AsyncQuery aQuery) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                Map<Version, String> map =
                        source != null ? (HashMap<Version, String>) source : new HashMap<Version, String>();
                Map<Version, ArrayList<String>> retMap = new HashMap<Version, ArrayList<String>>();

                for (Map.Entry<Version, String> keyValuePair : map.entrySet()) {
                    String[] split = keyValuePair.getValue().split("[;]", -1); //$NON-NLS-1$
                    if (split.length == 1 && (split[0] == null || split[0].isEmpty())) {
                        retMap.put(keyValuePair.getKey(),
                                null);
                    } else {
                        retMap.put(keyValuePair.getKey(),
                                new ArrayList<String>());
                        for (String s : split) {
                            retMap.get(keyValuePair.getKey()).add(s);
                        }
                    }
                }
                return retMap;
            }
        };
        Frontend.RunQuery(VdcQueryType.GetVmCustomProperties, new VdcQueryParametersBase(), aQuery);
    }

    public static void getPermissionsByAdElementId(AsyncQuery aQuery, Guid userId) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                return source != null ? (ArrayList<permissions>) source
                        : new ArrayList<permissions>();
            }
        };
        Frontend.RunQuery(VdcQueryType.GetPermissionsByAdElementId,
                new IdQueryParameters(userId),
                aQuery);
    }

    public static void getRoleActionGroupsByRoleId(AsyncQuery aQuery, Guid roleId) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                return source != null ? (ArrayList<ActionGroup>) source
                        : new ArrayList<ActionGroup>();
            }
        };
        Frontend.RunQuery(VdcQueryType.GetRoleActionGroupsByRoleId,
                new IdQueryParameters(roleId),
                aQuery);
    }

    public static void isTemplateNameUnique(AsyncQuery aQuery, String name) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                return source != null ? !((Boolean) source).booleanValue() : false;
            }
        };
        Frontend.RunQuery(VdcQueryType.IsVmTemlateWithSameNameExist,
                new NameQueryParameters(name),
                aQuery);
    }

    public static void isVmNameUnique(AsyncQuery aQuery, String name) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                return source != null ? !((Boolean) source).booleanValue() : false;
            }
        };
        Frontend.RunQuery(VdcQueryType.IsVmWithSameNameExist, new NameQueryParameters(name), aQuery);
    }

    public static void getDataCentersWithPermittedActionOnClusters(AsyncQuery aQuery, ActionGroup actionGroup,
            final boolean supportsVirtService, final boolean supportsGlusterService) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                if (source == null)
                {
                    return new ArrayList<StoragePool>();
                }
                return source;
            }
        };

        GetDataCentersWithPermittedActionOnClustersParameters getDataCentersWithPermittedActionOnClustersParameters =
                new GetDataCentersWithPermittedActionOnClustersParameters();
        getDataCentersWithPermittedActionOnClustersParameters.setActionGroup(actionGroup);
        getDataCentersWithPermittedActionOnClustersParameters.setSupportsVirtService(supportsVirtService);
        getDataCentersWithPermittedActionOnClustersParameters.setSupportsGlusterService(supportsGlusterService);

        Frontend.RunQuery(VdcQueryType.GetDataCentersWithPermittedActionOnClusters,
                getDataCentersWithPermittedActionOnClustersParameters,
                aQuery);
    }

    public static void getClustersWithPermittedAction(AsyncQuery aQuery, ActionGroup actionGroup,
            final boolean supportsVirtService, final boolean supportsGlusterService) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                if (source != null)
                {
                    ArrayList<VDSGroup> list = (ArrayList<VDSGroup>) source;
                    return getClusterByServiceList(list, supportsVirtService, supportsGlusterService);
                }
                return new ArrayList<VDSGroup>();
            }
        };

        GetEntitiesWithPermittedActionParameters getEntitiesWithPermittedActionParameters =
                new GetEntitiesWithPermittedActionParameters();
        getEntitiesWithPermittedActionParameters.setActionGroup(actionGroup);
        Frontend.RunQuery(VdcQueryType.GetClustersWithPermittedAction, getEntitiesWithPermittedActionParameters, aQuery);
    }

    public static void getVmTemplatesWithPermittedAction(AsyncQuery aQuery, ActionGroup actionGroup) {
        aQuery.converterCallback = new TemplateConverter();

        GetEntitiesWithPermittedActionParameters getEntitiesWithPermittedActionParameters =
                new GetEntitiesWithPermittedActionParameters();
        getEntitiesWithPermittedActionParameters.setActionGroup(actionGroup);
        Frontend.RunQuery(VdcQueryType.GetVmTemplatesWithPermittedAction,
                getEntitiesWithPermittedActionParameters,
                aQuery);
    }

    public static void getAllVmTemplates(AsyncQuery aQuery, final boolean refresh) {
        aQuery.converterCallback = new TemplateConverter();
        VdcQueryParametersBase params = new VdcQueryParametersBase();
        params.setRefresh(refresh);
        Frontend.RunQuery(VdcQueryType.GetAllVmTemplates, params, aQuery);
    }

    public static void isUSBEnabledByDefault(AsyncQuery aQuery) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                return source != null ? ((Boolean) source).booleanValue() : false;
            }
        };
        getConfigFromCache(
                new GetConfigurationValueParameters(ConfigurationValues.EnableUSBAsDefault,
                        getDefaultConfigurationVersion()),
                aQuery);
    }

    public static void getStorageConnectionById(AsyncQuery aQuery, String id, boolean isRefresh) {
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

    public static void getDataCentersByStorageDomain(AsyncQuery aQuery, Guid storageDomainId) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                return source != null ? (ArrayList<StoragePool>) source : null;
            }
        };
        Frontend.RunQuery(VdcQueryType.GetStoragePoolsByStorageDomainId,
                new IdQueryParameters(storageDomainId),
                aQuery);
    }

    public static void getDataCenterVersions(AsyncQuery aQuery, Guid dataCenterId) {
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
        IdQueryParameters tempVar = new IdQueryParameters(dataCenterId);
        Frontend.RunQuery(VdcQueryType.GetAvailableClusterVersionsByStoragePool, tempVar, aQuery);
    }

    public static void getDataCenterMaxNameLength(AsyncQuery aQuery) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                return source != null ? ((Integer) source).intValue() : 1;
            }
        };
        getConfigFromCache(
                new GetConfigurationValueParameters(ConfigurationValues.StoragePoolNameSizeLimit,
                        getDefaultConfigurationVersion()),
                aQuery);
    }

    public static void getClusterServerMemoryOverCommit(AsyncQuery aQuery) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                return source != null ? ((Integer) source).intValue() : 0;
            }
        };
        getConfigFromCache(
                new GetConfigurationValueParameters(ConfigurationValues.MaxVdsMemOverCommitForServers,
                        getDefaultConfigurationVersion()),
                aQuery);
    }

    public static void getClusterDesktopMemoryOverCommit(AsyncQuery aQuery) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                return source != null ? ((Integer) source).intValue() : 0;
            }
        };
        getConfigFromCache(
                new GetConfigurationValueParameters(ConfigurationValues.MaxVdsMemOverCommit,
                        getDefaultConfigurationVersion()),
                aQuery);
    }

    public static void getAllowClusterWithVirtGlusterEnabled(AsyncQuery aQuery) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                return source != null ? source : Boolean.TRUE;
            }
        };
        getConfigFromCache(
                new GetConfigurationValueParameters(ConfigurationValues.AllowClusterWithVirtGlusterEnabled,
                        getDefaultConfigurationVersion()),
                aQuery);
    }

    public static void getCPUList(AsyncQuery aQuery, Version version) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                return source != null ? (ArrayList<ServerCpu>) source : new ArrayList<ServerCpu>();
            }
        };
        Frontend.RunQuery(VdcQueryType.GetAllServerCpuList, new GetAllServerCpuListParameters(version), aQuery);
    }

    public static void getPmTypeList(AsyncQuery aQuery, Version version) {
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
        getConfigFromCache(tempVar, aQuery);
    }

    public static void getPmOptions(AsyncQuery aQuery, String pmType) {
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
        Frontend.RunQuery(VdcQueryType.GetAgentFenceOptions, new VdcQueryParametersBase(), aQuery);
    }

    public static void getNetworkList(AsyncQuery aQuery, Guid dataCenterId) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                return source != null ? (ArrayList<Network>) source : new ArrayList<Network>();
            }
        };

        Frontend.RunQuery(VdcQueryType.GetAllNetworks, new IdQueryParameters(dataCenterId), aQuery);
    }

    public static void getISOStorageDomainList(AsyncQuery aQuery) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                if (source != null)
                {
                    ArrayList<StorageDomain> allStorageDomains =
                            (ArrayList<StorageDomain>) source;
                    ArrayList<StorageDomain> isoStorageDomains = new ArrayList<StorageDomain>();
                    for (StorageDomain storageDomain : allStorageDomains)
                    {
                        if (storageDomain.getStorageDomainType() == StorageDomainType.ISO)
                        {
                            isoStorageDomains.add(storageDomain);
                        }
                    }
                    return isoStorageDomains;
                }
                return new ArrayList<StorageDomain>();
            }
        };

        SearchParameters searchParams = new SearchParameters("Storage:", SearchType.StorageDomain); //$NON-NLS-1$
        searchParams.setMaxCount(9999);

        Frontend.RunQuery(VdcQueryType.Search, searchParams, aQuery);
    }

    public static void getStorageDomainList(AsyncQuery aQuery) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                return source != null ? (ArrayList<StorageDomain>) source
                        : new ArrayList<StorageDomain>();
            }
        };

        SearchParameters searchParams = new SearchParameters("Storage:", SearchType.StorageDomain); //$NON-NLS-1$
        searchParams.setMaxCount(9999);

        Frontend.RunQuery(VdcQueryType.Search, searchParams, aQuery);
    }

    public static void getLocalStorageHost(AsyncQuery aQuery, String dataCenterName) {
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

    public static void getStorageDomainsByConnection(AsyncQuery aQuery, Guid storagePoolId, String connectionPath) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                return source != null ? (ArrayList<StorageDomain>) source : null;
            }
        };

        GetStorageDomainsByConnectionParameters param = new GetStorageDomainsByConnectionParameters();
        param.setConnection(connectionPath);
        if (storagePoolId != null) {
            param.setStoragePoolId(storagePoolId);
        }

        Frontend.RunQuery(VdcQueryType.GetStorageDomainsByConnection, param, aQuery);
    }

    public static void getExistingStorageDomainList(AsyncQuery aQuery,
            Guid hostId,
            StorageDomainType domainType,
            StorageType storageType,
            String path) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                return source != null ? (ArrayList<StorageDomain>) source : null;
            }
        };

        Frontend.RunQuery(VdcQueryType.GetExistingStorageDomainList, new GetExistingStorageDomainListParameters(hostId,
                storageType,
                domainType,
                path), aQuery);
    }

    public static void getStorageDomainMaxNameLength(AsyncQuery aQuery) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                return source != null ? ((Integer) source).intValue() : 1;
            }
        };
        getConfigFromCache(
                new GetConfigurationValueParameters(ConfigurationValues.StorageDomainNameSizeLimit,
                        getDefaultConfigurationVersion()),
                aQuery);
    }

    public static void isStorageDomainNameUnique(AsyncQuery aQuery, String name) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                if (source != null)
                {
                    ArrayList<StorageDomain> storageDomains = (ArrayList<StorageDomain>) source;
                    return storageDomains.isEmpty();
                }

                return source;
            }
        };
        Frontend.RunQuery(VdcQueryType.Search, new SearchParameters("Storage: name=" + name, //$NON-NLS-1$
                SearchType.StorageDomain), aQuery);
    }

    public static void getNetworkConnectivityCheckTimeoutInSeconds(AsyncQuery aQuery) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                return source != null ? ((Integer) source).intValue() : 120;
            }
        };
        getConfigFromCache(
                new GetConfigurationValueParameters(ConfigurationValues.NetworkConnectivityCheckTimeoutInSeconds,
                        getDefaultConfigurationVersion()),
                aQuery);
    }

    public static void getMaxSpmPriority(AsyncQuery aQuery) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery) {
                return source != null ? ((Integer) source).intValue() : 0;
            }
        };

        // GetConfigFromCache(
        // new GetConfigurationValueParameters(ConfigurationValues.HighUtilizationForPowerSave,
        // getDefaultConfigurationVersion()),
        // aQuery);

        aQuery.asyncCallback.onSuccess(aQuery.getModel(), 10);
    }

    public static void getDefaultSpmPriority(AsyncQuery aQuery) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery) {
                return source != null ? ((Integer) source).intValue() : 0;
            }
        };

        // GetConfigFromCache(
        // new GetConfigurationValueParameters(ConfigurationValues.HighUtilizationForPowerSave,
        // getDefaultConfigurationVersion()),
        // aQuery);

        aQuery.asyncCallback.onSuccess(aQuery.getModel(), 5);
    }

    public static void getDefaultPmProxyPreferences(AsyncQuery query) {
        getConfigFromCache(
                new GetConfigurationValueParameters(ConfigurationValues.FenceProxyDefaultPreferences,
                        getDefaultConfigurationVersion()),
                query);
    }

    public static void getRootTag(AsyncQuery aQuery) {
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

    private static void setAttachedTagsConverter(AsyncQuery aQuery) {
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

    public static void getAttachedTagsToVm(AsyncQuery aQuery, Guid id) {
        setAttachedTagsConverter(aQuery);

        Frontend.RunQuery(VdcQueryType.GetTagsByVmId, new GetTagsByVmIdParameters(id.toString()), aQuery);
    }

    public static void getAttachedTagsToUser(AsyncQuery aQuery, Guid id) {
        setAttachedTagsConverter(aQuery);

        Frontend.RunQuery(VdcQueryType.GetTagsByUserId, new GetTagsByUserIdParameters(id.toString()), aQuery);
    }

    public static void getAttachedTagsToUserGroup(AsyncQuery aQuery, Guid id) {
        setAttachedTagsConverter(aQuery);

        Frontend.RunQuery(VdcQueryType.GetTagsByUserGroupId, new GetTagsByUserGroupIdParameters(id.toString()), aQuery);
    }

    public static void getAttachedTagsToHost(AsyncQuery aQuery, Guid id) {
        setAttachedTagsConverter(aQuery);

        Frontend.RunQuery(VdcQueryType.GetTagsByVdsId, new GetTagsByVdsIdParameters(id.toString()), aQuery);
    }

    public static void getoVirtISOsList(AsyncQuery aQuery, Guid id) {
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

    public static void getLunsByVgId(AsyncQuery aQuery, String vgId, Guid vdsId) {
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

    public static void getAllTemplatesFromExportDomain(AsyncQuery aQuery, Guid storagePoolId, Guid storageDomainId) {
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

    public static void getUpHostListByCluster(AsyncQuery aQuery, String clusterName) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                if (source != null)
                {
                    ArrayList<VDS> list = Linq.<VDS> cast((ArrayList<IVdcQueryable>) source);
                    return list;
                }

                return new ArrayList<VDS>();
            }
        };
        getUpHostListByCluster(aQuery, clusterName, null);
    }

    public static void getUpHostListByCluster(AsyncQuery aQuery, String clusterName, Integer maxCount) {
        SearchParameters searchParameters =
                new SearchParameters("Host: cluster = " + clusterName + " and status = up", SearchType.VDS); //$NON-NLS-1$ //$NON-NLS-2$
        if (maxCount != null) {
            searchParameters.setMaxCount(maxCount);
        }
        Frontend.RunQuery(VdcQueryType.Search, searchParameters, aQuery);
    }

    public static void getClusterListByStorageDomain(AsyncQuery _AsyncQuery,
            Guid storageDomainId) {
        Frontend.RunQuery(VdcQueryType.GetStoragePoolsByStorageDomainId,
                new IdQueryParameters(storageDomainId),
                new AsyncQuery(_AsyncQuery, new INewAsyncCallback() {

                    @Override
                    public void onSuccess(Object model, Object returnValue) {
                        ArrayList<StoragePool> pools =
                                (ArrayList<StoragePool>) ((VdcQueryReturnValue) returnValue).getReturnValue();
                        if (pools != null && pools.size() > 0) {
                            StoragePool pool = pools.get(0);
                            getClusterList((AsyncQuery) model, pool.getId());
                        }
                    }
                }));
    }

    public static void getDataDomainsListByDomain(AsyncQuery _asyncQuery,
            Guid storageDomainId) {
        getDataCentersByStorageDomain(new AsyncQuery(_asyncQuery,
                new INewAsyncCallback() {

                    @Override
                    public void onSuccess(Object model, Object returnValue) {
                        ArrayList<StoragePool> pools = (ArrayList<StoragePool>) returnValue;
                        StoragePool pool = pools.get(0);
                        if (pool != null) {
                            getStorageDomainList((AsyncQuery) model,
                                    pool.getId());
                        }

                    }
                }), storageDomainId);
    }

    public static void getVmNicList(AsyncQuery aQuery, Guid id) {
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

    public static void getVmSnapshotList(AsyncQuery aQuery, Guid id) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                return source != null ? (ArrayList<Snapshot>) source : new ArrayList<Snapshot>();
            }
        };

        Frontend.RunQuery(VdcQueryType.GetAllVmSnapshotsByVmId, new IdQueryParameters(id), aQuery);
    }

    public static void getVmsRunningOnOrMigratingToVds(AsyncQuery aQuery, Guid id) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery) {
                if (source == null) {
                    return new ArrayList<VM>();
                }
                return source;
            }
        };

        Frontend.RunQuery(VdcQueryType.GetVmsRunningOnOrMigratingToVds,
                new IdQueryParameters(id),
                aQuery);
    }

    public static void getVmDiskList(AsyncQuery aQuery, Guid id) {
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

        Frontend.RunQuery(VdcQueryType.GetAllDisksByVmId, new IdQueryParameters(id), aQuery);
    }

    public static void getVmList(AsyncQuery aQuery, String poolName) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                ArrayList<VM> vms = Linq.<VM> cast((ArrayList<IVdcQueryable>) source);
                return vms;
            }
        };
        Frontend.RunQuery(VdcQueryType.Search, new SearchParameters("Vms: pool=" + poolName, SearchType.VM), aQuery); //$NON-NLS-1$
    }

    public static void getVmListByClusterName(AsyncQuery aQuery, String clusterName) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                ArrayList<VM> vms = Linq.<VM> cast((ArrayList<IVdcQueryable>) source);
                return vms;
            }
        };
        Frontend.RunQuery(VdcQueryType.Search,
                new SearchParameters("Vms: cluster=" + clusterName, SearchType.VM), aQuery); //$NON-NLS-1$
    }

    public static void getDiskList(AsyncQuery aQuery) {
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

    public static void getNextAvailableDiskAliasNameByVMId(AsyncQuery aQuery, Guid vmId) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                return source;
            }
        };
        Frontend.RunQuery(VdcQueryType.GetNextAvailableDiskAliasNameByVMId,
                new IdQueryParameters(vmId),
                aQuery);
    }

    public static void isPoolNameUnique(AsyncQuery aQuery, String name) {

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
                new NameQueryParameters(name),
                aQuery);
    }

    public static void getVmConfigurationBySnapshot(AsyncQuery aQuery, Guid snapshotSourceId) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                return source != null ? (VM) source : null;
            }
        };

        Frontend.RunQuery(VdcQueryType.GetVmConfigurationBySnapshot,
                new IdQueryParameters(snapshotSourceId),
                aQuery);
    }

    public static void getAllAttachableDisks(AsyncQuery aQuery, Guid storagePoolId, Guid vmId) {
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

    public static void getPermittedStorageDomainsByStoragePoolId(AsyncQuery aQuery,
            Guid dataCenterId,
            ActionGroup actionGroup) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                if (source == null)
                {
                    return new java.util.ArrayList<StorageDomain>();
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

    public static void getRedirectServletReportsPage(AsyncQuery aQuery) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                return source != null ? (String) source : ""; //$NON-NLS-1$
            }
        };
        getConfigFromCache(
                new GetConfigurationValueParameters(ConfigurationValues.RedirectServletReportsPage,
                        getDefaultConfigurationVersion()),
                aQuery);
    }

    private static HashMap<VdcActionType, CommandVersionsInfo> cachedCommandsCompatibilityVersions;

    public static void isCommandCompatible(AsyncQuery aQuery, final VdcActionType vdcActionType,
            final Version cluster, final Version dc) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                cachedCommandsCompatibilityVersions = (HashMap<VdcActionType, CommandVersionsInfo>) source;
                return isCommandCompatible(vdcActionType, cluster, dc);
            }
        };

        if (cachedCommandsCompatibilityVersions != null) {
            aQuery.asyncCallback.onSuccess(aQuery.getModel(), isCommandCompatible(vdcActionType, cluster, dc));
        } else {
            Frontend.RunQuery(VdcQueryType.GetCommandsCompatibilityVersions, new VdcQueryParametersBase(), aQuery);
        }
    }

    private static boolean isCommandCompatible(VdcActionType vdcActionType, Version cluster, Version dc) {
        if (cachedCommandsCompatibilityVersions == null || cluster == null || dc == null) {
            return false;
        }

        CommandVersionsInfo commandVersionsInfo = cachedCommandsCompatibilityVersions.get(vdcActionType);
        if (commandVersionsInfo == null) {
            return false;
        }

        Version clusterCompatibility = commandVersionsInfo.getClusterVersion();
        Version dcCompatibility = commandVersionsInfo.getStoragePoolVersion();

        return (clusterCompatibility.compareTo(cluster) <= 0)
                && (dcCompatibility.compareTo(dc) <= 0);
    }

    public static CommandVersionsInfo getCommandVersionsInfo(VdcActionType vdcActionType) {
        if (cachedCommandsCompatibilityVersions == null) {
            return null;
        }

        return cachedCommandsCompatibilityVersions.get(vdcActionType);
    }

    /**
     * Get the Management Network Name
     *
     * @param aQuery
     *            result callback
     */
    public static void getManagementNetworkName(AsyncQuery aQuery) {
        getConfigFromCache(
                new GetConfigurationValueParameters(ConfigurationValues.ManagementNetwork,
                        getDefaultConfigurationVersion()),
                aQuery);
    }

    /**
     * Cache configuration values [raw (not converted) values from vdc_options table].
     */
    private static void cacheConfigValues(AsyncQuery aQuery) {
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
     * Get configuration value from 'cachedConfigValuesPreConvert' (raw values from vdc_options table).
     *
     * @param version
     */
    public static Object getConfigValuePreConverted(ConfigurationValues configValue, String version) {
        KeyValuePairCompat<ConfigurationValues, String> key =
                new KeyValuePairCompat<ConfigurationValues, String>(configValue, version);

        return cachedConfigValuesPreConvert.get(key);
    }

    /**
     * Get configuration value from 'cachedConfigValuesPreConvert' (raw values from vdc_options table).
     */
    public static Object getConfigValuePreConverted(ConfigurationValues configValue) {
        KeyValuePairCompat<ConfigurationValues, String> key =
                new KeyValuePairCompat<ConfigurationValues, String>(configValue, getDefaultConfigurationVersion());

        return cachedConfigValuesPreConvert.get(key);
    }

    /**
     * Get configuration value from using a specified converter.
     */
    public static Object getConfigValue(ConfigurationValues configValue, String version, IAsyncConverter converter) {
        if (converter == null) {
            return null;
        }

        KeyValuePairCompat<ConfigurationValues, String> key =
                new KeyValuePairCompat<ConfigurationValues, String>(configValue, version);

        return converter.Convert(cachedConfigValuesPreConvert.get(key), null);
    }

    /**
     * method to get an item from config while caching it (config is not supposed to change during a session)
     *
     * @param aQuery
     *            an async query
     * @param parameters
     *            a converter for the async query
     */
    public static void getConfigFromCache(GetConfigurationValueParameters parameters, AsyncQuery aQuery) {
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
        // and call asyncCallback's onSuccess
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
        aQuery.asyncCallback.onSuccess(aQuery.getModel(), returnValue);
    }

    /**
     * method to get an item from config while caching it (config is not supposed to change during a session)
     *
     * @param configValue
     *            the config value to query
     * @param version
     *            the compatibility version to query
     * @param aQuery
     *            an async query
     */
    public static void getConfigFromCache(ConfigurationValues configValue, String version, AsyncQuery aQuery) {
        GetConfigurationValueParameters parameters = new GetConfigurationValueParameters(configValue, version);
        getConfigFromCache(parameters, aQuery);
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
                    } else if (template.getStatus() == VmTemplateStatus.OK) {
                        list.add(template);
                    }
                }

                Collections.sort(list, new NameableComparator());
                if (blankTemplate != null) {
                    list.add(0, blankTemplate);
                }
            }

            return list;
        }
    }

    public static void getInterfaceOptionsForEditNetwork(final AsyncQuery asyncQuery,
            final ArrayList<VdsNetworkInterface> interfaceList,
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
            getAllChildVlanInterfaces(vdsID, ifacesOptions, new IFrontendMultipleQueryAsyncCallback() {

                @Override
                public void executed(FrontendMultipleQueryAsyncResult result) {

                    ArrayList<VdsNetworkInterface> ifacesOptionsTemp = new ArrayList<VdsNetworkInterface>();
                    List<VdcQueryReturnValue> returnValueList = result.getReturnValues();

                    for (int i = 0; i < returnValueList.size(); i++)
                    {
                        VdcQueryReturnValue returnValue = returnValueList.get(i);
                        if (returnValue != null && returnValue.getSucceeded() && returnValue.getReturnValue() != null)
                        {
                            ArrayList<VdsNetworkInterface> childVlanInterfaces =
                                    (ArrayList<VdsNetworkInterface>) (returnValue.getReturnValue());

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

                    asyncQuery.asyncCallback.onSuccess(asyncQuery.Model, ifacesOptions);
                }
            });

        }

        else // vlan:
        {
            getVlanParentInterface(vdsID, originalInterface, new AsyncQuery(asyncQuery, new INewAsyncCallback() {

                @Override
                public void onSuccess(Object model, Object returnValue) {
                    final VdsNetworkInterface vlanParent = (VdsNetworkInterface) returnValue;

                    if (vlanParent != null && vlanParent.getBonded() != null && vlanParent.getBonded()) {
                        interfaceHasSiblingVlanInterfaces(vdsID, originalInterface, new AsyncQuery(asyncQuery,
                                new INewAsyncCallback() {

                                    @Override
                                    public void onSuccess(Object model, Object returnValue) {
                                        Boolean interfaceHasSiblingVlanInterfaces = (Boolean) returnValue;

                                        if (!interfaceHasSiblingVlanInterfaces) {
                                            // eth0 -- \
                                            // |--- bond0 ---> bond0.3 -> <networkToEdit>
                                            // eth1 -- /
                                            // ---------------------------------------------------
                                            // - originalInterface: 'bond0.3'
                                            // - vlanParent: 'bond0'
                                            // - 'bond0.3' has no vlan siblings
                                            // --> We want to add 'eth0' and and 'eth1' as optional Interfaces.
                                            // (note that choosing one of them will break the bond):
                                            // ifacesOptions.AddRange(interfaceList.Where(a => a.bond_name ==
                                            // vlanParent.name).ToList());
                                            for (VdsNetworkInterface i : interfaceList) {
                                                if (StringHelper.stringsEqual(i.getBondName(), vlanParent.getName())) {
                                                    ifacesOptions.add(i);
                                                }
                                            }
                                        }

                                        // the vlanParent should already be in ifacesOptions
                                        // (since it has no network_name or bond_name).
                                        defaultInterfaceName.append(vlanParent.getName());

                                        asyncQuery.asyncCallback.onSuccess(asyncQuery.Model, ifacesOptions);

                                    }
                                }));
                    } else {
                        // the vlanParent should already be in ifacesOptions
                        // (since it has no network_name or bond_name).
                        if (vlanParent != null)
                            defaultInterfaceName.append(vlanParent.getName());
                        asyncQuery.asyncCallback.onSuccess(asyncQuery.Model, ifacesOptions);
                    }
                }
            }));
        }
    }

    private static void getVlanParentInterface(Guid vdsID, VdsNetworkInterface iface, AsyncQuery aQuery)
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

    private static void interfaceHasSiblingVlanInterfaces(Guid vdsID, VdsNetworkInterface iface, AsyncQuery aQuery)
    {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                ArrayList<VdsNetworkInterface> siblingVlanInterfaces = (ArrayList<VdsNetworkInterface>) source;

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

    public static void GetExternalProviderHostList(AsyncQuery aQuery,
            Guid providerId,
            boolean filterOutExistingHosts,
            String searchFilter) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                if (source == null)
                {
                    return new ArrayList<VDS>();
                }
                return source;
            }
        };
        GetHostListFromExternalProviderParameters params = new GetHostListFromExternalProviderParameters();
        params.setFilterOutExistingHosts(filterOutExistingHosts);
        params.setProviderId(providerId);
        params.setSearchFilter(searchFilter);
        Frontend.RunQuery(VdcQueryType.GetHostListFromExternalProvider,
                params,
                aQuery);
    }

    public static void GetAllProviders(AsyncQuery aQuery) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                if (source == null)
                {
                    return new ArrayList<Provider>();
                }
                Collections.sort((List<Provider>) source, new NameableComparator());
                return source;
            }
        };
        Frontend.RunQuery(VdcQueryType.GetAllProviders, new GetAllProvidersParameters(), aQuery);
    }

    public static void GetAllProvidersByProvidedEntity(AsyncQuery query, final VdcObjectType providedEntity) {
        query.converterCallback = new IAsyncConverter<List<Provider>>() {
            @Override
            public List<Provider> Convert(Object returnValue, AsyncQuery asyncQuery) {
                if (returnValue == null) {
                    return new ArrayList<Provider>();
                }
                List<Provider> providers =
                        Linq.toList(Linq.filterProvidersByProvidedType((Iterable<Provider>) returnValue, providedEntity));
                Collections.sort(providers, new NameableComparator());
                return providers;
            }
        };
        Frontend.RunQuery(VdcQueryType.GetAllProviders, new GetAllProvidersParameters(), query);
    }

    public static void GetAllNetworkProviders(AsyncQuery query) {
        GetAllProvidersByProvidedEntity(query, VdcObjectType.Network);
    }

    public static void GetAllProvidersByType(AsyncQuery aQuery, ProviderType providerType) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                if (source == null)
                {
                    return new ArrayList<Provider>();
                }
                return source;
            }
        };
        Frontend.RunQuery(VdcQueryType.GetAllProviders, new GetAllProvidersParameters(providerType), aQuery);
    }

    public static void GetProviderCertificateChain(AsyncQuery aQuery, Provider provider) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                return source;
            }
        };
        Frontend.RunQuery(VdcQueryType.GetProviderCertificateChain, new ProviderQueryParameters(provider), aQuery);
    }

    private static void getAllChildVlanInterfaces(Guid vdsID,
            List<VdsNetworkInterface> ifaces,
            IFrontendMultipleQueryAsyncCallback callback)
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

    public static void isSupportBridgesReportByVDSM(AsyncQuery aQuery, String version) {
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
        getConfigFromCache(tempVar, aQuery);
    }

    public static void isMTUOverrideSupported(AsyncQuery aQuery, String version) {
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
        getConfigFromCache(tempVar, aQuery);
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

    public static ArrayList<EventNotificationEntity> getEventNotificationTypeList()
    {
        ArrayList<EventNotificationEntity> ret = new ArrayList<EventNotificationEntity>();
        // TODO: We can translate it here too
        for (EventNotificationEntity entity : EventNotificationEntity.values()) {
            if (entity != EventNotificationEntity.UNKNOWN) {
                ret.add(entity);
            }
        }
        return ret;
    }

    public static Map<EventNotificationEntity, HashSet<AuditLogType>> getAvailableNotificationEvents() {
        return VdcEventNotificationUtils.GetNotificationEvents();
    }

    public static void getNicTypeList(final int osId, Version version, AsyncQuery asyncQuery) {
        final INewAsyncCallback chainedCallback = asyncQuery.asyncCallback;
        asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object returnValue) {
                ArrayList<String> nics = (ArrayList<String>) ((VdcQueryReturnValue) returnValue).getReturnValue();
                List<VmInterfaceType> interfaceTypes = new ArrayList<VmInterfaceType>();
                for (String nic : nics) {
                    try {
                        interfaceTypes.add(VmInterfaceType.valueOf(nic));
                    } catch (IllegalArgumentException e) {
                        // ignore if we can't find the enum value.
                    }
                }
                chainedCallback.onSuccess(model, interfaceTypes);
            }
        };
        Frontend.RunQuery(VdcQueryType.OsRepository,
                new OsQueryParameters(OsRepositoryVerb.GetNetworkDevices, osId, version),
                asyncQuery);
    }

    public static VmInterfaceType getDefaultNicType()
    {
        return VmInterfaceType.pv;
    }

    public static ArrayList<StorageType> getStoragePoolTypeList() {

        return new ArrayList<StorageType>(Arrays.asList(new StorageType[] {
                StorageType.NFS,
                StorageType.ISCSI,
                StorageType.FCP,
                StorageType.LOCALFS,
                StorageType.POSIXFS,
                StorageType.GLUSTERFS
        }));
    }

    public static boolean isVersionMatchStorageType(Version version, StorageType type) {
        return !((type == StorageType.LOCALFS && version.compareTo(new Version(2, 2)) <= 0)
                || (type == StorageType.POSIXFS && version.compareTo(new Version(3, 0)) <= 0)
                || (type == StorageType.GLUSTERFS && version.compareTo(new Version(3, 2)) <= 0));
    }

    public static int getClusterDefaultMemoryOverCommit() {
        return 100;
    }

    public static boolean getClusterDefaultCountThreadsAsCores() {
        return false;
    }

    public static ArrayList<VolumeType> getVolumeTypeList() {
        return new ArrayList<VolumeType>(Arrays.asList(new VolumeType[] {
                VolumeType.Preallocated,
                VolumeType.Sparse
        }));
    }

    public static ArrayList<StorageType> getStorageTypeList()
    {
        return new ArrayList<StorageType>(Arrays.asList(new StorageType[] {
                StorageType.ISCSI,
                StorageType.FCP
        }));
    }

    public static ArrayList<DiskInterface> getDiskInterfaceList(Version clusterVersion)
    {
        ArrayList<DiskInterface> diskInterfaces = new ArrayList<DiskInterface>(
                Arrays.asList(new DiskInterface[] {
                        DiskInterface.IDE,
                        DiskInterface.VirtIO
                }));

        boolean isVirtIOScsiEnabled = clusterVersion != null ? (Boolean) AsyncDataProvider.getConfigValuePreConverted(
                ConfigurationValues.VirtIoScsiEnabled, clusterVersion.getValue()) : true;

        if (isVirtIOScsiEnabled) {
            diskInterfaces.add(DiskInterface.VirtIO_SCSI);
        }

        return diskInterfaces;
    }

    public static String getNewNicName(ArrayList<VmNetworkInterface> existingInterfaces)
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
    public static String getComplexValueFromSpiceRedKeysResource(String complexValue) {
        if (StringHelper.isNullOrEmpty(complexValue)) {
            return ""; //$NON-NLS-1$
        }
        ArrayList<String> values = new ArrayList<String>();

        for (String s : complexValue.split("[+]", -1)) { //$NON-NLS-1$
            try {
                String value =
                    SpiceConstantsManager.getInstance()
                    .getSpiceRedKeys()
                    .getString(s.replaceAll("-", "_")); //$NON-NLS-1$ //$NON-NLS-2$
                values.add(value);
            } catch (MissingResourceException e) {
                values.add(s);
            }

        }

        return StringHelper.join("+", values.toArray(new String[] {})); //$NON-NLS-1$
    }

    public static Guid getEntityGuid(Object entity)
    {
        if (entity instanceof VM)
        {
            return ((VM) entity).getId();
        }
        else if (entity instanceof StoragePool)
        {
            return ((StoragePool) entity).getId();
        }
        else if (entity instanceof VDSGroup)
        {
            return ((VDSGroup) entity).getId();
        }
        else if (entity instanceof VDS)
        {
            return ((VDS) entity).getId();
        }
        else if (entity instanceof StorageDomain)
        {
            return ((StorageDomain) entity).getId();
        }
        else if (entity instanceof VmTemplate)
        {
            return ((VmTemplate) entity).getId();
        }
        else if (entity instanceof VmPool)
        {
            return ((VmPool) entity).getVmPoolId();
        }
        else if (entity instanceof DbUser)
        {
            return ((DbUser) entity).getId();
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
        else if (entity instanceof VnicProfile)
        {
            return ((VnicProfile) entity).getId();
        }
        return Guid.Empty;
    }

    public static boolean isWindowsOsType(Integer osType) {
        // can be null as a consequence of setItems on ListModel
        if (osType == null) {
            return false;
        }

        return windowsOsIds.contains(osType);
    }

    public static boolean isLinuxOsType(Integer osId) {
        // can be null as a consequence of setItems on ListModel
        if (osId == null) {
            return false;
        }

        return linuxOsIds.contains(osId);
    }

    public static void initWindowsOsTypes() {

        AsyncQuery callback = new AsyncQuery();
        callback.asyncCallback = new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object returnValue) {
                windowsOsIds = (ArrayList<Integer>) ((VdcQueryReturnValue) returnValue).getReturnValue();
            }
        };
        Frontend.RunQuery(VdcQueryType.OsRepository, new OsQueryParameters(OsRepositoryVerb.GetWindowsOss), callback);
    }

    public static void initLinuxOsTypes() {

        AsyncQuery callback = new AsyncQuery();
        callback.asyncCallback = new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object returnValue) {
                linuxOsIds = (ArrayList<Integer>) ((VdcQueryReturnValue) returnValue).getReturnValue();
            }
        };
        Frontend.RunQuery(VdcQueryType.OsRepository, new OsQueryParameters(OsRepositoryVerb.GetLinuxOss), callback);
    }

    public static void initUniqueOsNames() {

        AsyncQuery callback = new AsyncQuery();
        callback.asyncCallback = new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object returnValue) {
                uniqueOsNames = (HashMap<Integer, String>) ((VdcQueryReturnValue) returnValue).getReturnValue();
            }
        };
        Frontend.RunQuery(VdcQueryType.OsRepository, new OsQueryParameters(OsRepositoryVerb.GetUniqueOsNames), callback);
    }

    public static void initOsNames() {

        AsyncQuery callback = new AsyncQuery();
        callback.asyncCallback = new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object returnValue) {
                osNames = (HashMap<Integer, String>) ((VdcQueryReturnValue) returnValue).getReturnValue();
                initOsIds();
            }
        };
        Frontend.RunQuery(VdcQueryType.OsRepository, new OsQueryParameters(OsRepositoryVerb.GetOsNames), callback);
    }

    private static void initOsIds() {
        osIds = new ArrayList<Integer>(osNames.keySet());
        Collections.sort(osIds, new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return osNames.get(o1).compareTo(osNames.get(o2));
            }
        });
    }

    public static String getOsName(Integer osId) {
        // can be null as a consequence of setItems on ListModel
        if (osId == null) {
            return "";
        }

        return osNames.get(osId);
    }

    public static void hasSpiceSupport(int osId, Version version, AsyncQuery callback) {
        Frontend.RunQuery(VdcQueryType.OsRepository,
                new OsQueryParameters(OsRepositoryVerb.HasSpiceSupport, osId, version),
                callback);
    }

    public static List<Integer> getOsIds() {
        return osIds;
    }

    public static void getOsMaxRam(int osId, Version version, AsyncQuery asyncQuery) {
        Frontend.RunQuery(VdcQueryType.OsRepository,
                new OsQueryParameters(OsRepositoryVerb.GetMaxOsRam, osId, version),
                asyncQuery);
    }

    public static ArrayList<Map.Entry<String, EntityModel>> getBondingOptionList(RefObject<Map.Entry<String, EntityModel>> defaultItem)
    {
        ArrayList<Map.Entry<String, EntityModel>> list =
                new ArrayList<Map.Entry<String, EntityModel>>();
        EntityModel entityModel = new EntityModel();
        entityModel.setEntity("(Mode 1) Active-Backup"); //$NON-NLS-1$
        list.add(new KeyValuePairCompat<String, EntityModel>("mode=1 miimon=100", entityModel)); //$NON-NLS-1$
        entityModel = new EntityModel();
        entityModel.setEntity("(Mode 2) Load balance (balance-xor)"); //$NON-NLS-1$
        list.add(new KeyValuePairCompat<String, EntityModel>("mode=2 miimon=100", entityModel)); //$NON-NLS-1$
        entityModel = new EntityModel();
        entityModel.setEntity("(Mode 4) Dynamic link aggregation (802.3ad)"); //$NON-NLS-1$
        defaultItem.argvalue = new KeyValuePairCompat<String, EntityModel>("mode=4 miimon=100", entityModel); //$NON-NLS-1$
        list.add(defaultItem.argvalue);
        entityModel = new EntityModel();
        entityModel.setEntity("(Mode 5) Adaptive transmit load balancing (balance-tlb)"); //$NON-NLS-1$
        list.add(new KeyValuePairCompat<String, EntityModel>("mode=5 miimon=100", entityModel)); //$NON-NLS-1$
        entityModel = new EntityModel();
        entityModel.setEntity(""); //$NON-NLS-1$
        list.add(new KeyValuePairCompat<String, EntityModel>("custom", entityModel)); //$NON-NLS-1$
        return list;
    }

    public static String getDefaultBondingOption()
    {
        return "mode=802.3ad miimon=150"; //$NON-NLS-1$
    }

    public static int getMaxVmPriority()
    {
        return (Integer) getConfigValuePreConverted(ConfigurationValues.VmPriorityMaxValue,
                getDefaultConfigurationVersion());
    }

    public static int roundPriority(int priority)
    {
        int max = getMaxVmPriority();
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

    public static void getAllVnicProfiles(AsyncQuery aQuery) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                if (source == null)
                {
                    return new ArrayList<VnicProfileView>();
                }
                return source;
            }
        };
        Frontend.RunQuery(VdcQueryType.GetAllVnicProfiles, new VdcQueryParametersBase(), aQuery);
    }

    public static void getVnicProfilesByNetworkId(AsyncQuery aQuery, Guid networkId) {
        aQuery.converterCallback = new IAsyncConverter() {
            @Override
            public Object Convert(Object source, AsyncQuery _asyncQuery)
            {
                if (source == null)
                {
                    return new ArrayList<VnicProfileView>();
                }
                return source;
            }
        };
        Frontend.RunQuery(VdcQueryType.GetVnicProfilesByNetworkId, new IdQueryParameters(networkId), aQuery);
    }

    public static void getVnicProfilesByDcId(AsyncQuery aQuery, Guid dcId) {
        // do not replace a converter = just add if none provided
        if (aQuery.converterCallback == null) {
            aQuery.converterCallback = new IAsyncConverter() {
                @Override
                public Object Convert(Object source, AsyncQuery _asyncQuery)
                {
                    if (source == null)
                    {
                        return new ArrayList<VnicProfileView>();
                    }
                    return source;
                }
            };
        }
        Frontend.RunQuery(VdcQueryType.GetVnicProfilesByDataCenterId, new IdQueryParameters(dcId), aQuery);
    }

    private static ArrayList<VDSGroup> getClusterByServiceList(ArrayList<VDSGroup> list,
            boolean supportsVirtService,
            boolean supportsGlusterService) {
        final ArrayList<VDSGroup> filteredList = new ArrayList<VDSGroup>();
        for (VDSGroup cluster : list) {
            if ((supportsVirtService && cluster.supportsVirtService())
                    || (supportsGlusterService && cluster.supportsGlusterService())) {
                filteredList.add(cluster);
            }
        }

        // sort by cluster name
        Collections.sort(filteredList, new NameableComparator());
        return filteredList;
    }

    public static String priorityToString(int value) {
        int roundedPriority = AsyncDataProvider.roundPriority(value);

        if (roundedPriority == 1) {
            return ConstantsManager.getInstance().getConstants().vmLowPriority();
        }
        else if (roundedPriority == AsyncDataProvider.getMaxVmPriority() / 2) {
            return ConstantsManager.getInstance().getConstants().vmMediumPriority();
        }
        else if (roundedPriority == AsyncDataProvider.getMaxVmPriority()) {
            return ConstantsManager.getInstance().getConstants().vmHighPriority();
        }
        else {
            return ConstantsManager.getInstance().getConstants().vmUnknownPriority();
        }
    }

    public static void GetExternalNetworkList(AsyncQuery aQuery, Guid providerId) {
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
        Frontend.RunQuery(VdcQueryType.GetAllExternalNetworksOnProvider,
                new IdQueryParameters(providerId),
                aQuery);
    }

    public static Integer getMaxVmNameLengthWin() {
        Integer maxVmNameLengthWindows = (Integer) AsyncDataProvider.getConfigValuePreConverted(ConfigurationValues.MaxVmNameLengthWindows);
        return maxVmNameLengthWindows == null ? 15 : maxVmNameLengthWindows;
    }

    public static Integer getMaxVmNameLengthNonWin() {
        Integer maxVmNameLengthNonWindows = (Integer) AsyncDataProvider.getConfigValuePreConverted(ConfigurationValues.MaxVmNameLengthNonWindows);
        return maxVmNameLengthNonWindows == null ? 64 : maxVmNameLengthNonWindows;
    }
}
