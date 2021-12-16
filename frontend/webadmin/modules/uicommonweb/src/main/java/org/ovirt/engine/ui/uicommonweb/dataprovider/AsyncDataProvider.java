package org.ovirt.engine.ui.uicommonweb.dataprovider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.ovirt.engine.core.aaa.ProfileEntry;
import org.ovirt.engine.core.common.ActionUtils;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.EventNotificationEntity;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.TimeZoneType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.VmManagementParametersBase;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeRemoveBricksQueriesParameters;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.AdditionalFeature;
import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.BiosType;
import org.ovirt.engine.core.common.businessentities.BusinessEntity;
import org.ovirt.engine.core.common.businessentities.CertificateInfo;
import org.ovirt.engine.core.common.businessentities.ChipsetType;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.ClusterEditWarnings;
import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.GraphicsType;
import org.ovirt.engine.core.common.businessentities.HostDeviceView;
import org.ovirt.engine.core.common.businessentities.Label;
import org.ovirt.engine.core.common.businessentities.Nameable;
import org.ovirt.engine.core.common.businessentities.NumaTuneMode;
import org.ovirt.engine.core.common.businessentities.OriginType;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.ProviderType;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.QuotaEnforcementTypeEnum;
import org.ovirt.engine.core.common.businessentities.Role;
import org.ovirt.engine.core.common.businessentities.SerialNumberPolicy;
import org.ovirt.engine.core.common.businessentities.ServerCpu;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.SupportedAdditionalClusterFeature;
import org.ovirt.engine.core.common.businessentities.Tags;
import org.ovirt.engine.core.common.businessentities.TagsType;
import org.ovirt.engine.core.common.businessentities.TpmSupport;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VdsNumaNode;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.businessentities.VmGuestAgentInterface;
import org.ovirt.engine.core.common.businessentities.VmHostDevice;
import org.ovirt.engine.core.common.businessentities.VmPool;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.VmTemplateStatus;
import org.ovirt.engine.core.common.businessentities.VmWithStatusForExclusiveLock;
import org.ovirt.engine.core.common.businessentities.aaa.AuthzGroup;
import org.ovirt.engine.core.common.businessentities.aaa.DbGroup;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.common.businessentities.comparators.LexoNumericComparator;
import org.ovirt.engine.core.common.businessentities.comparators.NameableComparator;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterClusterService;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterGeoRepNonEligibilityReason;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterGeoRepSession;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterHookEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterServerService;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeAdvancedDetails;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeSnapshotEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeSnapshotSchedule;
import org.ovirt.engine.core.common.businessentities.gluster.ServiceType;
import org.ovirt.engine.core.common.businessentities.gluster.StorageDevice;
import org.ovirt.engine.core.common.businessentities.network.BondMode;
import org.ovirt.engine.core.common.businessentities.network.HostNetworkQos;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkQoS;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VmInterfaceType;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VnicProfileView;
import org.ovirt.engine.core.common.businessentities.pm.FenceAgent;
import org.ovirt.engine.core.common.businessentities.qos.QosType;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskInterface;
import org.ovirt.engine.core.common.businessentities.storage.Image;
import org.ovirt.engine.core.common.businessentities.storage.LUNs;
import org.ovirt.engine.core.common.businessentities.storage.LibvirtSecretUsageType;
import org.ovirt.engine.core.common.businessentities.storage.ManagedBlockStorage;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.storage.VolumeType;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.migration.MigrationPolicy;
import org.ovirt.engine.core.common.migration.NoMigrationPolicy;
import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.core.common.queries.ArchCapabilitiesParameters;
import org.ovirt.engine.core.common.queries.ArchCapabilitiesParameters.ArchCapabilitiesVerb;
import org.ovirt.engine.core.common.queries.ClusterEditParameters;
import org.ovirt.engine.core.common.queries.GetAgentFenceOptionsQueryParameters;
import org.ovirt.engine.core.common.queries.GetAllAttachableDisksForVmQueryParameters;
import org.ovirt.engine.core.common.queries.GetAllFromExportDomainQueryParameters;
import org.ovirt.engine.core.common.queries.GetAllProvidersParameters;
import org.ovirt.engine.core.common.queries.GetAllServerCpuListParameters;
import org.ovirt.engine.core.common.queries.GetClusterFeaturesByVersionAndCategoryParameters;
import org.ovirt.engine.core.common.queries.GetConfigurationValueParameters;
import org.ovirt.engine.core.common.queries.GetConnectionsByDataCenterAndStorageTypeParameters;
import org.ovirt.engine.core.common.queries.GetCpuByFlagsParameters;
import org.ovirt.engine.core.common.queries.GetExistingStorageDomainListParameters;
import org.ovirt.engine.core.common.queries.GetHostsForStorageOperationParameters;
import org.ovirt.engine.core.common.queries.GetLunsByVgIdParameters;
import org.ovirt.engine.core.common.queries.GetManagedBlockStorageDomainsByDriversParameters;
import org.ovirt.engine.core.common.queries.GetPermittedStorageDomainsByStoragePoolIdParameters;
import org.ovirt.engine.core.common.queries.GetStorageDomainDefaultWipeAfterDeleteParameters;
import org.ovirt.engine.core.common.queries.GetStorageDomainsByConnectionParameters;
import org.ovirt.engine.core.common.queries.GetStoragePoolsByClusterServiceParameters;
import org.ovirt.engine.core.common.queries.GetSupportedCpuListParameters;
import org.ovirt.engine.core.common.queries.GetTagsByUserGroupIdParameters;
import org.ovirt.engine.core.common.queries.GetTagsByUserIdParameters;
import org.ovirt.engine.core.common.queries.GetTagsByVdsIdParameters;
import org.ovirt.engine.core.common.queries.GetTagsByVmIdParameters;
import org.ovirt.engine.core.common.queries.GetVmChangedFieldsForNextRunParameters;
import org.ovirt.engine.core.common.queries.GetVmFromOvaQueryParameters;
import org.ovirt.engine.core.common.queries.GetVmTemplateParameters;
import org.ovirt.engine.core.common.queries.GetVmsFromExternalProviderQueryParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.IdsQueryParameters;
import org.ovirt.engine.core.common.queries.IsDefaultRouteRoleNetworkAttachedToHostQueryParameters;
import org.ovirt.engine.core.common.queries.NameQueryParameters;
import org.ovirt.engine.core.common.queries.OsQueryParameters;
import org.ovirt.engine.core.common.queries.OsQueryParameters.OsRepositoryVerb;
import org.ovirt.engine.core.common.queries.ProviderQueryParameters;
import org.ovirt.engine.core.common.queries.QosQueryParameterBase;
import org.ovirt.engine.core.common.queries.QueryParametersBase;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.common.queries.SearchParameters;
import org.ovirt.engine.core.common.queries.ServerParameters;
import org.ovirt.engine.core.common.queries.StorageDomainsAndStoragePoolIdQueryParameters;
import org.ovirt.engine.core.common.queries.StorageServerConnectionQueryParametersBase;
import org.ovirt.engine.core.common.queries.TimeZoneQueryParameters;
import org.ovirt.engine.core.common.queries.TimeZoneQueryParameters.TimeZoneVerb;
import org.ovirt.engine.core.common.queries.ValidateVmMacsParameters;
import org.ovirt.engine.core.common.queries.VmIconIdSizePair;
import org.ovirt.engine.core.common.queries.gluster.AddedGlusterServersParameters;
import org.ovirt.engine.core.common.queries.gluster.GlusterHookContentQueryParameters;
import org.ovirt.engine.core.common.queries.gluster.GlusterHookQueryParameters;
import org.ovirt.engine.core.common.queries.gluster.GlusterParameters;
import org.ovirt.engine.core.common.queries.gluster.GlusterServersQueryParameters;
import org.ovirt.engine.core.common.queries.gluster.GlusterServiceQueryParameters;
import org.ovirt.engine.core.common.queries.gluster.GlusterVolumeAdvancedDetailsParameters;
import org.ovirt.engine.core.common.queries.gluster.GlusterVolumeGeoRepEligibilityParameters;
import org.ovirt.engine.core.common.queries.gluster.GlusterVolumeProfileParameters;
import org.ovirt.engine.core.common.queries.gluster.GlusterVolumeQueriesParameters;
import org.ovirt.engine.core.common.scheduling.AffinityGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.utils.PairQueryable;
import org.ovirt.engine.core.common.utils.SimpleDependencyInjector;
import org.ovirt.engine.core.common.utils.VmCommonUtils;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.IntegerCompat;
import org.ovirt.engine.core.compat.KeyValuePairCompat;
import org.ovirt.engine.core.compat.RefObject;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.searchbackend.OsValueAutoCompleter;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Converter;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.comparators.QuotaComparator;
import org.ovirt.engine.ui.uicommonweb.models.ApplicationModeHelper;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.LoginModel;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.NetworkModel;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.NetworkQoSModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.SetupNetworksBondModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.FcpStorageModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.GlusterStorageModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.IStorageModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.ImportFcpStorageModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.ImportIscsiStorageModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.IscsiStorageModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.LocalStorageModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.ManagedBlockStorageModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.NfsStorageModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.PosixStorageModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.NetworkFilterParameterModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IFrontendMultipleQueryAsyncCallback;

public class AsyncDataProvider {

    private static AsyncDataProvider instance;

    public static AsyncDataProvider getInstance() {
        if (instance == null) {
            instance = new AsyncDataProvider();
        }
        return instance;
    }

    public static void setInstance(AsyncDataProvider provider) {
        instance = provider;
    }

    private static final String GENERAL = "general"; //$NON-NLS-1$

    private static final int DEFAULT_OS_ID = 0;

    // dictionary to hold cache of all config values (per version) queried by client, if the request for them succeeded.
    private final Map<KeyValuePairCompat<ConfigValues, String>, Object> cachedConfigValues = new HashMap<>();

    private final Map<KeyValuePairCompat<ConfigValues, String>, Object> cachedConfigValuesPreConvert = new HashMap<>();

    private String _defaultConfigurationVersion = null;

    // cached OS names
    private Map<Integer, String> osNames;

    // cached general timezones
    private Map<String, String> generalTimezones;

    // cached windows timezones
    private Map<String, String> windowsTimezones;

    // OS default icons
    private Map<Integer, VmIconIdSizePair> osIdToDefaultIconIdMap;

    /**
     * large-icon-id -> small-icon-id; data comes from {@link #osIdToDefaultIconIdMap}
     */
    private Map<Guid, Guid> largeToSmallOsDefaultIconIdMap;

    // all defined migration policies
    private Map<Version, List<MigrationPolicy>> migrationPoliciesByVersion;

    // cached unique OS names
    private Map<Integer, String> uniqueOsNames;

    // cached linux OS
    private List<Integer> linuxOsIds;

    // cached VM init type to OS ids
    private Map<Integer, String> vmInitMap;

    // cached NIC hotplug support map
    private Map<Pair<Integer, Version>, Boolean> nicHotplugSupportMap;

    // cached disk hotpluggable interfaces map
    private Map<Pair<Integer, Version>, Set<String>> diskHotpluggableInterfacesMap;

    // cached sound device enabled by map
    private Map<Integer, Map<Version, Boolean>> soundDeviceSupportMap;

    // cached TPM support for OS map
    private Map<Integer, TpmSupport> tpmSupportForOsMap;

    // cached windows OS
    private List<Integer> windowsOsIds;
    // cached OS Architecture
    private Map<Integer, ArchitectureType> osArchitectures;
    // default OS per architecture
    private Map<ArchitectureType, Integer> defaultOSes;

    // default OS per architecture
    private Set<Integer> oses64bit;

    // cached os's support for graphics and display types (given compatibility version)
    private Map<Integer, Map<Version, List<Pair<GraphicsType, DisplayType>>>> graphicsAndDisplays;

    // cached architecture support for live migration
    private Map<ArchitectureType, Map<Version, Boolean>> migrationSupport;

    // cached architecture support for memory snapshot
    private Map<ArchitectureType, Map<Version, Boolean>> memorySnapshotSupport;

    // cached architecture support for memory hot unplug
    private Map<ArchitectureType, Map<Version, Boolean>> memoryHotUnplugSupport;

    // cached architecture support for tpm device
    private Map<ArchitectureType, Map<Version, Boolean>> tpmDeviceSupport;

    // cached custom properties
    private Map<Version, Map<String, String>> customPropertiesList;

    /** (CPU name, cluster compatibility version) -> {@link ServerCpu} */
    private Map<Pair<String, Version>, ServerCpu> cpuMap;

    // cached ImageIO Proxy URI
    private String imageioProxyUri;

    //cached unsupported os ids
    private Set<Integer> unsupportedOsIds;

    public String getDefaultConfigurationVersion() {
        return _defaultConfigurationVersion;
    }

    private void getDefaultConfigurationVersion(final LoginModel loginModel) {
        AsyncQuery<QueryReturnValue> callback = new AsyncQuery<>(returnValue -> {
            if (returnValue != null) {
                _defaultConfigurationVersion =
                        returnValue.getReturnValue();
            } else {
                _defaultConfigurationVersion = GENERAL;
            }
            loginModel.getLoggedInEvent().raise(loginModel, EventArgs.EMPTY);
        });
        callback.setHandleFailure(true);
        Frontend.getInstance().runQuery(QueryType.GetDefaultConfigurationVersion,
                new QueryParametersBase(),
                callback);
    }

    public void initCache(final LoginModel loginModel) {
        cacheConfigValues(new AsyncQuery<>(returnValue -> getDefaultConfigurationVersion(loginModel)));
        initOsNames();
        initTimezones();
        initOsDefaultIconIds();
        initUniqueOsNames();
        initLinuxOsTypes();
        initVmInitTypes();
        initWindowsOsTypes();
        initDisplayTypes();
        initNicHotplugSupportMap();
        initDiskHotpluggableInterfacesMap();
        initOsArchitecture();
        initDefaultOSes();
        initGet64BitOss();
        initMigrationSupportMap();
        initMemorySnapshotSupportMap();
        initMemoryHotUnplugSupportMap();
        initTpmDeviceSupportMap();
        initCustomPropertiesList();
        initSoundDeviceSupportMap();
        initMigrationPolicies();
        initCpuMap();
        initImageioProxyUri();
        initTpmSupportForOsMap();
        initUnsupportedOsIds();
    }

    private void initMigrationPolicies() {
        AsyncQuery<Map<Version, List<MigrationPolicy>>> aQuery =
                new AsyncQuery<>(returnValue -> migrationPoliciesByVersion = returnValue);

        aQuery.converterCallback = returnValue -> {
            if (returnValue == null) {
                return new HashMap<>();
            }

            Map<Version, List<MigrationPolicy>> policiesByVersion = (Map<Version, List<MigrationPolicy>>) returnValue;

            for (List<MigrationPolicy> policies : policiesByVersion.values()) {
                policies.sort(Comparator.comparing((MigrationPolicy m) -> !NoMigrationPolicy.ID.equals(m.getId()))
                        .thenComparing(MigrationPolicy::getName));
            }

            return policiesByVersion;
        };

        Frontend.getInstance().runQuery(QueryType.GetAllMigrationPolicies,
                new QueryParametersBase(),
                aQuery);
    }

    private void initCpuMap() {
        cpuMap = new HashMap<>();

        final List<QueryType> queryTypes = new ArrayList<>();
        final List<QueryParametersBase> queryParams = new ArrayList<>();
        for (Version version : Version.ALL) {
            queryTypes.add(QueryType.GetAllServerCpuList);
            queryParams.add(new GetAllServerCpuListParameters(version));
        }

        final IFrontendMultipleQueryAsyncCallback callback = result -> {
            for (int i = 0; i < result.getReturnValues().size(); i++) {
                final List<ServerCpu> cpus = result.getReturnValues().get(i).getReturnValue();
                final Version version =
                        ((GetAllServerCpuListParameters) result.getParameters().get(i)).getVersion();
                initCpuMapForVersion(version, cpus);
            }
        };

        Frontend.getInstance().runMultipleQueries(queryTypes, queryParams, callback);
    }

    private void initCpuMapForVersion(Version version, List<ServerCpu> cpus) {
        for (ServerCpu cpu : cpus) {
            cpuMap.put(new Pair<>(cpu.getCpuName(), version), cpu);
        }
    }

    private void initCustomPropertiesList() {
        AsyncQuery<Map<Version, Map<String, String>>> callback =
                new AsyncQuery<>(returnValue -> customPropertiesList = returnValue);

        callback.converterCallback = new MapConverter<>();
        Frontend.getInstance().runQuery(QueryType.GetVmCustomProperties,
                new QueryParametersBase().withoutRefresh(),
                callback);
    }

    public void initDefaultOSes() {
        Frontend.getInstance().runQuery(QueryType.OsRepository,
                new OsQueryParameters(
                        OsRepositoryVerb.GetDefaultOSes),
                new AsyncQuery<QueryReturnValue>(returnValue -> defaultOSes = returnValue.getReturnValue()));
    }

    private void initGet64BitOss() {
        Frontend.getInstance().runQuery(QueryType.OsRepository,
                new OsQueryParameters(OsRepositoryVerb.Get64BitOss),
                new AsyncQuery<QueryReturnValue>(returnValue -> oses64bit = Collections.unmodifiableSet(
                        new HashSet<>(returnValue.<List<Integer>> getReturnValue()))));
    }

    public void getStorageDomainsWithAttachedStoragePoolGuid(
            AsyncQuery<List<StorageDomainStatic>> aQuery, StoragePool storagePool, List<StorageDomain> storageDomains) {
        aQuery.converterCallback = new ListConverter<>();
        StorageDomainsAndStoragePoolIdQueryParameters parameters =
                new StorageDomainsAndStoragePoolIdQueryParameters(storageDomains, storagePool.getId());
        Frontend.getInstance().runQuery(QueryType.GetStorageDomainsWithAttachedStoragePoolGuid,
                parameters,
                aQuery);
    }

    public void getStorageDomainsWithAttachedStoragePoolGuid(
            AsyncQuery<List<StorageDomainStatic>> aQuery,
            StoragePool storagePool,
            List<StorageDomain> storageDomains,
            StorageServerConnections storageServerConnection,
            Guid vdsId) {
        aQuery.converterCallback = new ListConverter<>();

        if (storageDomains != null) {
            // Get file storage domains
            StorageDomainsAndStoragePoolIdQueryParameters parameters =
                    new StorageDomainsAndStoragePoolIdQueryParameters(storageDomains, storagePool.getId(), vdsId);
            Frontend.getInstance().runQuery(QueryType.GetBlockStorageDomainsWithAttachedStoragePoolGuid,
                    parameters,
                    aQuery);
        } else {
            // Get block storage domains
            StorageDomainsAndStoragePoolIdQueryParameters parameters =
                    new StorageDomainsAndStoragePoolIdQueryParameters(storageServerConnection,
                            storagePool.getId(),
                            vdsId);
            Frontend.getInstance().runQuery(QueryType.GetFileStorageDomainsWithAttachedStoragePoolGuid,
                    parameters,
                    aQuery);
        }
    }

    public Boolean isMigrationSupported(ArchitectureType architecture, Version version) {
        return migrationSupport.get(architecture).get(version);
    }

    public Boolean isMemorySnapshotSupportedByArchitecture(ArchitectureType architecture, Version version) {
        return memorySnapshotSupport.get(architecture).get(version);
    }

    public Boolean isMemoryHotUnplugSupportedByArchitecture(ArchitectureType architecture, Version version) {
        return memoryHotUnplugSupport.get(architecture).get(version);
    }

    public Boolean isTpmDeviceSupported(ArchitectureType architecture, Version version) {
        return tpmDeviceSupport.get(architecture).get(version);
    }

    public Boolean isTscSupportedByVersion(Version version) {
        return FeatureSupported.isTscFrequencySupported(version);
    }

    public Boolean isFipsModeSupportedByVersion(Version version) {
        return FeatureSupported.isFipsModeSupported(version);
    }

    private void initMigrationSupportMap() {
        Frontend.getInstance().runQuery(QueryType.GetArchitectureCapabilities,
                new ArchCapabilitiesParameters(ArchCapabilitiesVerb.GetMigrationSupport),
                new AsyncQuery<QueryReturnValue>(returnValue -> migrationSupport = returnValue.getReturnValue()));
    }

    private void initMemorySnapshotSupportMap() {
        Frontend.getInstance().runQuery(QueryType.GetArchitectureCapabilities,
                new ArchCapabilitiesParameters(ArchCapabilitiesVerb.GetMemorySnapshotSupport),
                new AsyncQuery<QueryReturnValue>(returnValue -> memorySnapshotSupport = returnValue.getReturnValue()));
    }

    private void initMemoryHotUnplugSupportMap() {
        Frontend.getInstance().runQuery(QueryType.GetArchitectureCapabilities,
                new ArchCapabilitiesParameters(ArchCapabilitiesVerb.GetMemoryHotUnplugSupport),
                new AsyncQuery<QueryReturnValue>(returnValue -> memoryHotUnplugSupport = returnValue.getReturnValue()));
    }

    private void initTpmDeviceSupportMap() {
        Frontend.getInstance().runQuery(QueryType.GetArchitectureCapabilities,
                new ArchCapabilitiesParameters(ArchCapabilitiesVerb.GetTpmDeviceSupport),
                new AsyncQuery<QueryReturnValue>(returnValue -> tpmDeviceSupport = returnValue.getReturnValue()));
    }

    /**
     * Check if memory snapshot is supported
     */
    public boolean isMemorySnapshotSupported(VM vm) {
        if (vm == null) {
            return false;
        }

        return isMemorySnapshotSupportedByArchitecture(
                vm.getClusterArch(),
                vm.getCompatibilityVersion());
    }

    public void initNicHotplugSupportMap() {
        Frontend.getInstance().runQuery(QueryType.OsRepository,
                new OsQueryParameters(
                        OsRepositoryVerb.GetNicHotplugSupportMap),
                new AsyncQuery<QueryReturnValue>(returnValue -> nicHotplugSupportMap = returnValue.getReturnValue()));
    }

    public Map<Pair<Integer, Version>, Boolean> getNicHotplugSupportMap() {
        return nicHotplugSupportMap;
    }

    public Boolean getNicHotplugSupport(Integer osId, Version version) {
        Pair<Integer, Version> pair = new Pair<>(osId, version);

        if (getNicHotplugSupportMap().containsKey(pair)) {
            return getNicHotplugSupportMap().get(pair);
        }

        return false;
    }

    public void initDiskHotpluggableInterfacesMap() {
        Frontend.getInstance().runQuery(QueryType.OsRepository,
                new OsQueryParameters(
                        OsRepositoryVerb.GetDiskHotpluggableInterfacesMap),
                new AsyncQuery<QueryReturnValue>(
                        returnValue -> diskHotpluggableInterfacesMap = returnValue.getReturnValue()));
    }

    public Boolean isSoundDeviceEnabled(int osId, Version version) {
        return soundDeviceSupportMap.get(osId).get(version);
    }

    public void initSoundDeviceSupportMap() {
        Frontend.getInstance().runQuery(QueryType.OsRepository,
                new OsQueryParameters(
                        OsRepositoryVerb.GetSoundDeviceSupportMap),
                new AsyncQuery<QueryReturnValue>(result -> soundDeviceSupportMap = result.getReturnValue()));
    }

    public Map<Pair<Integer, Version>, Set<String>> getDiskHotpluggableInterfacesMap() {
        return diskHotpluggableInterfacesMap;
    }

    public Collection<DiskInterface> getDiskHotpluggableInterfaces(Integer osId, Version version) {

        Set<String> diskHotpluggableInterfaces = getDiskHotpluggableInterfacesMap().get(new Pair<>(osId, version));
        if (diskHotpluggableInterfaces == null) {
            return Collections.emptySet();
        }

        Collection<DiskInterface> diskInterfaces = new HashSet<>();
        for (String diskHotpluggableInterface : diskHotpluggableInterfaces) {
            diskInterfaces.add(DiskInterface.valueOf(diskHotpluggableInterface));
        }

        return diskInterfaces;
    }

    public void getAAAProfilesListViaPublic(AsyncQuery<List<String>> aQuery, boolean passwordBasedOnly) {
        convertAAAProfilesResult(aQuery, passwordBasedOnly);
        Frontend.getInstance().runPublicQuery(QueryType.GetAAAProfileList, new QueryParametersBase(), aQuery);
    }

    public static void isFloppySupported(AsyncQuery<Boolean> aQuery, Integer osId, Version version) {
        aQuery.converterCallback = new DefaultValueConverter<>(Boolean.FALSE);
        OsQueryParameters params = new OsQueryParameters(OsRepositoryVerb.GetFloppySupport, osId, version);

        Frontend.getInstance().runQuery(QueryType.OsRepository, params, aQuery);
    }

    public void getIsoDomainByDataCenterId(AsyncQuery<StorageDomain> aQuery, Guid dataCenterId) {
        aQuery.converterCallback = source -> {
            if (source != null) {
                ArrayList<StorageDomain> storageDomains = (ArrayList<StorageDomain>) source;
                for (StorageDomain domain : storageDomains) {
                    if (domain.getStorageDomainType() == StorageDomainType.ISO) {
                        return domain;
                    }
                }
            }

            return null;
        };

        IdQueryParameters getIsoParams = new IdQueryParameters(dataCenterId);
        Frontend.getInstance().runQuery(QueryType.GetStorageDomainsByStoragePoolId, getIsoParams, aQuery);
    }

    public void getExportDomainByDataCenterId(AsyncQuery<StorageDomain> aQuery, Guid dataCenterId) {
        aQuery.converterCallback = source -> {
            ArrayList<StorageDomain> storageDomains = (ArrayList<StorageDomain>) source;
            for (StorageDomain domain : storageDomains) {
                if (domain.getStorageDomainType() == StorageDomainType.ImportExport) {
                    return domain;
                }
            }

            return null;
        };

        IdQueryParameters getExportParams = new IdQueryParameters(dataCenterId);
        Frontend.getInstance().runQuery(QueryType.GetStorageDomainsByStoragePoolId, getExportParams, aQuery);
    }

    public void getDefaultManagementNetwork(AsyncQuery<Network> aQuery, Guid dataCenterId) {
        runQueryByIdParameter(QueryType.GetDefaultManagementNetwork, aQuery, dataCenterId);
    }

    public void getManagementNetwork(AsyncQuery<Network> aQuery, Guid clusterId) {
        runQueryByIdParameter(QueryType.GetManagementNetwork, aQuery, clusterId);
    }

    public void isManagementNetwork(AsyncQuery<Boolean> aQuery, Guid networkId) {
        runQueryByIdParameter(QueryType.IsManagementNetwork, aQuery, networkId);
    }

    public void getProviderById(AsyncQuery<Provider> aQuery, Guid providerId) {
        aQuery.converterCallback = new CastingConverter<>();
        Frontend.getInstance().runQuery(QueryType.GetProviderById,
                new IdQueryParameters(providerId),
                aQuery);
    }

    public void isClusterEmpty(AsyncQuery<Boolean> aQuery, Guid clusterId) {
        runQueryByIdParameter(QueryType.IsClusterEmpty, aQuery, clusterId);
    }

    private void runQueryByIdParameter(QueryType queryType, AsyncQuery aQuery, Guid id) {
        aQuery.converterCallback = new CastingConverter();
        Frontend.getInstance().runQuery(queryType, new IdQueryParameters(id), aQuery);
    }

    public void getHostArchitecture(AsyncQuery<ArchitectureType> aQuery, Guid id) {
        aQuery.converterCallback = new CastingConverter<>();
        Frontend.getInstance().runQuery(QueryType.GetHostArchitecture, new IdQueryParameters(id), aQuery);
    }

    public void getClusterById(AsyncQuery<Cluster> aQuery, Guid id) {
        aQuery.converterCallback = new CastingConverter<>();
        Frontend.getInstance().runQuery(QueryType.GetClusterById, new IdQueryParameters(id), aQuery);
    }

    public void getClusterListByName(AsyncQuery<List<Cluster>> aQuery, String name) {
        aQuery.converterCallback = new ListConverter<>();
        Frontend.getInstance().runQuery(QueryType.Search,
                new SearchParameters("Cluster: name=" + name + " sortby name", SearchType.Cluster), //$NON-NLS-1$ //$NON-NLS-2$
                aQuery);
    }

    public void getAuthzGroupsByUserId(AsyncQuery<List<AuthzGroup>> aQuery, Guid userId) {
        aQuery.converterCallback = new CastingConverter<>();
        Frontend.getInstance().runQuery(QueryType.GetAuthzGroupsByUserId, new IdQueryParameters(userId), aQuery);
    }

    public void getVmById(AsyncQuery<VM> aQuery, Guid vmId) {
        aQuery.converterCallback = new CastingConverter<>();
        Frontend.getInstance().runQuery(QueryType.GetVmByVmId, new IdQueryParameters(vmId), aQuery);
    }

    public void getVmNextRunConfiguration(AsyncQuery<VM> aQuery, Guid vmId) {
        aQuery.converterCallback = new CastingConverter<>();
        Frontend.getInstance().runQuery(QueryType.GetVmNextRunConfiguration, new IdQueryParameters(vmId), aQuery);
    }

    public void getVmChangedFieldsForNextRun(VM original,
            VM updated,
            VmManagementParametersBase updateVmParameters,
            AsyncQuery<QueryReturnValue> aQuery) {
        Frontend.getInstance().runQuery(QueryType.GetVmChangedFieldsForNextRun,
                new GetVmChangedFieldsForNextRunParameters(original, updated, updateVmParameters),
                aQuery);
    }

    public void getTemplateList(String searchPattern, AsyncQuery<QueryReturnValue> aQuery) {
        SearchParameters params = new SearchParameters(searchPattern, SearchType.VmTemplate);
        Frontend.getInstance().runQuery(QueryType.Search, params, aQuery);
    }

    public void getDataCenterList(AsyncQuery<List<StoragePool>> aQuery) {
        getDataCenterList(aQuery, true);
    }

    public List<MigrationPolicy> getMigrationPolicies(Version compatibilityVersion) {
        List<MigrationPolicy> migrationPolicies = migrationPoliciesByVersion.get(compatibilityVersion);
        return migrationPolicies != null ? new ArrayList<>(migrationPolicies)
                : Collections.singletonList((MigrationPolicy) new NoMigrationPolicy());
    }

    public void getDataCenterList(AsyncQuery<List<StoragePool>> aQuery, boolean doRefresh) {
        aQuery.converterCallback = new ListConverter<>();
        SearchParameters params = new SearchParameters("DataCenter: sortby name", SearchType.StoragePool); //$NON-NLS-1$
        Frontend.getInstance().runQuery(QueryType.Search, doRefresh ? params : params.withoutRefresh(), aQuery);
    }

    public void getDataCenterByClusterServiceList(AsyncQuery<List<StoragePool>> aQuery,
            boolean supportsVirtService,
            boolean supportsGlusterService) {
        aQuery.converterCallback = new SortListByNameConverter<>();

        final GetStoragePoolsByClusterServiceParameters parameters = new GetStoragePoolsByClusterServiceParameters();
        parameters.setSupportsVirtService(supportsVirtService);
        parameters.setSupportsGlusterService(supportsGlusterService);

        Frontend.getInstance().runQuery(QueryType.GetStoragePoolsByClusterService, parameters, aQuery);
    }

    public void getDataCenterListByName(AsyncQuery<List<StoragePool>> aQuery, String name) {
        aQuery.converterCallback = new ListConverter<>();
        Frontend.getInstance().runQuery(QueryType.Search,
                new SearchParameters("DataCenter: name=" + name + " sortby name", SearchType.StoragePool), //$NON-NLS-1$ //$NON-NLS-2$
                aQuery);
    }

    public void getSpiceUsbAutoShare(AsyncQuery<Boolean> aQuery) {
        aQuery.converterCallback = new DefaultValueConverter<>(Boolean.TRUE);
        getConfigFromCache(
                new GetConfigurationValueParameters(ConfigValues.SpiceUsbAutoShare,
                        getDefaultConfigurationVersion()),
                aQuery);
    }

    public void getConfigurationValueBoolean(AsyncQuery<Boolean> aQuery, ConfigValues configVal) {
        aQuery.converterCallback = new DefaultValueConverter<>(Boolean.TRUE);
        getConfigFromCache(
                new GetConfigurationValueParameters(configVal, getDefaultConfigurationVersion()), aQuery);
    }

    public ServerCpu getCpuByName(String cpuName, Version clusterVersion) {
        return cpuMap.get(new Pair<>(cpuName, clusterVersion));
    }

    public void getMaxVmsInPool(AsyncQuery<Integer> aQuery) {
        aQuery.converterCallback = new DefaultValueConverter<>(1000);
        getConfigFromCache(
                new GetConfigurationValueParameters(ConfigValues.MaxVmsInPool, getDefaultConfigurationVersion()),
                aQuery);
    }

    public void getMaxNumOfVmSockets(AsyncQuery<Integer> aQuery, String version) {
        aQuery.converterCallback = new DefaultValueConverter<>(1);
        GetConfigurationValueParameters tempVar =
                new GetConfigurationValueParameters(ConfigValues.MaxNumOfVmSockets);
        tempVar.setVersion(version);
        getConfigFromCache(tempVar, aQuery);
    }

    public void getMaxNumOfVmCpus(AsyncQuery<Map<String, Integer>> aQuery, String version) {
        aQuery.converterCallback = new MapConverter<>();
        GetConfigurationValueParameters tempVar =
                new GetConfigurationValueParameters(ConfigValues.MaxNumOfVmCpus);
        tempVar.setVersion(version);
        getConfigFromCache(tempVar, aQuery);
    }

    public void getMaxNumOfCPUsPerSocket(AsyncQuery<Integer> aQuery, String version) {
        aQuery.converterCallback = new DefaultValueConverter<>(1);
        GetConfigurationValueParameters tempVar =
                new GetConfigurationValueParameters(ConfigValues.MaxNumOfCpuPerSocket);
        tempVar.setVersion(version);
        getConfigFromCache(tempVar, aQuery);
    }

    public void getMaxNumOfThreadsPerCpu(AsyncQuery<Integer> aQuery, String version) {
        aQuery.converterCallback = new DefaultValueConverter<>(1);
        GetConfigurationValueParameters tempVar =
                new GetConfigurationValueParameters(ConfigValues.MaxNumOfThreadsPerCpu);
        tempVar.setVersion(version);
        getConfigFromCache(tempVar, aQuery);
    }

    public void getClusterList(AsyncQuery<List<Cluster>> aQuery, Guid dataCenterId) {
        aQuery.converterCallback = new SortListByNameConverter<>();
        Frontend.getInstance().runQuery(QueryType.GetClustersByStoragePoolId,
                new IdQueryParameters(dataCenterId),
                aQuery);
    }

    public List<Cluster> filterByArchitecture(List<Cluster> clusters, ArchitectureType targetArchitecture) {
        List<Cluster> filteredClusters = new ArrayList<>();

        for (Cluster cluster : clusters) {
            if (cluster.getArchitecture().equals(targetArchitecture)) {
                filteredClusters.add(cluster);
            }
        }
        return filteredClusters;
    }

    public List<Cluster> filterClustersWithoutArchitecture(List<Cluster> clusters) {
        List<Cluster> filteredClusters = new ArrayList<>();

        for (Cluster cluster : clusters) {
            if (cluster.getArchitecture() != ArchitectureType.undefined) {
                filteredClusters.add(cluster);
            }
        }
        return filteredClusters;
    }

    public void getClusterByServiceList(AsyncQuery<List<Cluster>> aQuery,
            Guid dataCenterId,
            final boolean supportsVirtService,
            final boolean supportsGlusterService) {
        aQuery.converterCallback = source -> {
            if (source == null) {
                return new ArrayList<>();
            }
            final ArrayList<Cluster> list = (ArrayList<Cluster>) source;
            return getClusterByServiceList(list, supportsVirtService, supportsGlusterService);
        };
        Frontend.getInstance().runQuery(QueryType.GetClustersByStoragePoolId,
                new IdQueryParameters(dataCenterId),
                aQuery);
    }

    public void isSoundcardEnabled(AsyncQuery<Boolean> aQuery, Guid vmId) {
        aQuery.converterCallback = new IsNonEmptyCollectionConverter();
        Frontend.getInstance().runQuery(QueryType.GetSoundDevices, new IdQueryParameters(vmId), aQuery);
    }

    public void isVirtioScsiEnabledForVm(AsyncQuery<Boolean> aQuery, Guid vmId) {
        aQuery.converterCallback = new IsNonEmptyCollectionConverter();
        Frontend.getInstance().runQuery(QueryType.GetVirtioScsiControllers, new IdQueryParameters(vmId), aQuery);
    }

    public void getClusterListByService(AsyncQuery<List<Cluster>> aQuery,
            final boolean supportsVirtService,
            final boolean supportsGlusterService) {

        aQuery.converterCallback = source -> {
            if (source != null) {
                ArrayList<Cluster> list =
                        getClusterByServiceList((ArrayList<Cluster>) source,
                                supportsVirtService,
                                supportsGlusterService);
                list.sort(new NameableComparator());
                return list;
            }
            return new ArrayList<>();
        };
        Frontend.getInstance().runQuery(QueryType.GetAllClusters, new QueryParametersBase(), aQuery);
    }

    public void getClusterList(AsyncQuery<List<Cluster>> aQuery) {
        getClusterList(aQuery, true);
    }

    public void getClusterList(AsyncQuery aQuery, boolean doRefresh) {
        aQuery.converterCallback = new SortListByNameConverter<>();
        Frontend.getInstance().runQuery(QueryType.GetAllClusters,
                doRefresh ? new QueryParametersBase() : new QueryParametersBase().withoutRefresh(),
                aQuery);
    }

    public static void isOvirtCockpitSSOStarted(AsyncQuery<Boolean> aQuery) {
        aQuery.converterCallback = new DefaultValueConverter<>(Boolean.FALSE);
        Frontend.getInstance().runQuery(QueryType.IsOvirtCockpitSSOStarted, new QueryParametersBase(), aQuery);
    }

    public void getAffinityGroupsByClusterId(AsyncQuery<List<AffinityGroup>> aQuery, Guid clusterId) {
        aQuery.converterCallback = new SortListByNameConverter<>();
        Frontend.getInstance().runQuery(QueryType.GetAffinityGroupsByClusterId, new IdQueryParameters(clusterId), aQuery);
    }

    public void getLabelList(AsyncQuery<List<Label>> aQuery) {
        aQuery.converterCallback = new SortListByNameConverter<>();
        Frontend.getInstance().runQuery(QueryType.GetAllLabels, new QueryParametersBase(), aQuery);
    }

    public void getEntitiesNameMap(AsyncQuery<Map<Guid, String>> aQuery) {
        aQuery.converterCallback = new MapConverter<>();
        Frontend.getInstance().runQuery(QueryType.GetEntitiesNameMap, new QueryParametersBase(), aQuery);
    }

    public void getTemplateDiskList(AsyncQuery<List<DiskImage>> aQuery, Guid templateId) {
        aQuery.converterCallback = new ListConverter<>();
        Frontend.getInstance().runQuery(QueryType.GetVmTemplatesDisks, new IdQueryParameters(templateId), aQuery);
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
    public int getRoundedPriority(int priority, int maxPriority) {

        int medium = maxPriority / 2;

        int[] levels = new int[] { 1, medium, maxPriority };

        for (int i = 0; i < levels.length; i++) {
            int lengthToLess = levels[i] - priority;
            int lengthToMore = levels[i + 1] - priority;

            if (lengthToMore < 0) {
                continue;
            }

            return Math.abs(lengthToLess) < lengthToMore ? levels[i] : levels[i + 1];
        }

        return 0;
    }

    public void getTemplateListByDataCenter(AsyncQuery<List<VmTemplate>> aQuery, Guid dataCenterId) {
        aQuery.converterCallback = new TemplateConverter();
        Frontend.getInstance().runQuery(QueryType.GetVmTemplatesByStoragePoolId,
                new IdQueryParameters(dataCenterId),
                aQuery);
    }

    public ArrayList<VmTemplate> filterTemplatesByArchitecture(List<VmTemplate> list,
            ArchitectureType architecture) {
        ArrayList<VmTemplate> filteredList = new ArrayList<>();

        for (VmTemplate template : list) {
            if (template.getId().equals(Guid.Empty) ||
                    template.getClusterArch().equals(architecture)) {
                filteredList.add(template);
            }
        }

        return filteredList;
    }

    public void getNumOfMonitorList(AsyncQuery<List<Integer>> aQuery) {
        aQuery.converterCallback = source -> {
            ArrayList<Integer> nums = new ArrayList<>();
            if (source != null) {
                Iterable numEnumerable = (Iterable) source;
                Iterator numIterator = numEnumerable.iterator();
                while (numIterator.hasNext()) {
                    nums.add(Integer.parseInt(numIterator.next().toString()));
                }
            }
            return nums;
        };
        getConfigFromCache(
                new GetConfigurationValueParameters(ConfigValues.ValidNumOfMonitors,
                        getDefaultConfigurationVersion()),
                aQuery);
    }

    public void getStorageDomainList(AsyncQuery<List<StorageDomain>> aQuery, Guid dataCenterId) {
        aQuery.converterCallback = new ListConverter<>();
        Frontend.getInstance().runQuery(QueryType.GetStorageDomainsByStoragePoolId,
                new IdQueryParameters(dataCenterId),
                aQuery);
    }

    public void getMaxVmPriority(AsyncQuery<Integer> aQuery) {
        aQuery.converterCallback = new DefaultValueConverter<>(100);
        getConfigFromCache(
                new GetConfigurationValueParameters(ConfigValues.VmPriorityMaxValue,
                        getDefaultConfigurationVersion()),
                aQuery);
    }

    public void getHostById(AsyncQuery<VDS> aQuery, Guid id) {
        aQuery.converterCallback = new CastingConverter<>();
        Frontend.getInstance().runQuery(QueryType.GetVdsByVdsId, new IdQueryParameters(id).withoutRefresh(), aQuery);
    }

    public void getHostListByCluster(AsyncQuery<List<VDS>> aQuery, String clusterName) {
        aQuery.converterCallback = new ListConverter<>();
        Frontend.getInstance().runQuery(QueryType.Search,
                new SearchParameters("Host: cluster = " + clusterName + " sortby name", //$NON-NLS-1$ //$NON-NLS-2$
                        SearchType.VDS),
                aQuery);
    }

    public void getHostListByClusterId(AsyncQuery<List<VDS>> aQuery, Guid clusterId) {
        aQuery.converterCallback = new ListConverter<>();
        Frontend.getInstance().runQuery(QueryType.GetHostsByClusterId, new IdQueryParameters(clusterId), aQuery);
    }

    public void getHostListByDataCenter(AsyncQuery<List<VDS>> aQuery, Guid spId) {
        aQuery.converterCallback = new ListConverter<>();
        Frontend.getInstance().runQuery(QueryType.GetAllVdsByStoragePool, new IdQueryParameters(spId), aQuery);
    }

    public void getHostDevicesByHostId(AsyncQuery<List<HostDeviceView>> aQuery, Guid hostId) {
        aQuery.converterCallback = new ListConverter<>();
        Frontend.getInstance().runQuery(QueryType.GetExtendedHostDevicesByHostId,
                new IdQueryParameters(hostId),
                aQuery);
    }

    public void getConfiguredVmHostDevices(AsyncQuery<List<VmHostDevice>> aQuery, Guid vmId) {
        aQuery.converterCallback = new ListConverter<>();
        Frontend.getInstance().runQuery(QueryType.GetVmHostDevices, new IdQueryParameters(vmId), aQuery);
    }

    public void getVmDiskList(AsyncQuery<List<Disk>> aQuery, Guid vmId, boolean isRefresh) {
        aQuery.converterCallback = new ListConverter<>();
        IdQueryParameters params = new IdQueryParameters(vmId);
        params.setRefresh(isRefresh);
        Frontend.getInstance().runQuery(QueryType.GetAllDisksByVmId, params, aQuery);
    }

    /**
     * Upper bound of maximum memory size for given OS and compatibilityVersion. If {@code osId} is null then maximum of
     * all configuration values is returned. If {@code compatVersion} is null then last Version is used.
     *
     * <p>
     * Inspired by {@link VmCommonUtils#maxMemorySizeWithHotplugInMb(int, Version)}
     * </p>
     *
     * @param osId
     *            operating system id, may be null
     * @param compatVersion
     *            compatibility version, may be null
     * @return upper bound of maximum memory size for given OS and compatibilityVersion,
     */
    public int getMaxMaxMemorySize(Integer osId, Version compatVersion) {
        String usedVersion = compatVersion != null ? compatVersion.getValue() : Version.getLast().getValue();
        if (osId == null) {
            return getMaxMaxMemoryForAllOss(usedVersion);
        }

        final ConfigValues maxMaxMemoryConfigValue = getMaxMaxMemoryConfigValue(osId);
        return (Integer) getConfigValuePreConverted(maxMaxMemoryConfigValue, usedVersion);
    }

    private int getMaxMaxMemoryForAllOss(String version) {
        final int x86_32MaxMaxMemory =
                (Integer) getConfigValuePreConverted(ConfigValues.VM32BitMaxMemorySizeInMB, version);
        final int x86_64MaxMaxMemory =
                (Integer) getConfigValuePreConverted(ConfigValues.VM64BitMaxMemorySizeInMB, version);
        final int ppc64MaxMaxMemory =
                (Integer) getConfigValuePreConverted(ConfigValues.VMPpc64BitMaxMemorySizeInMB, version);
        return Math.max(Math.max(x86_32MaxMaxMemory, x86_64MaxMaxMemory), ppc64MaxMaxMemory);
    }

    private ConfigValues getMaxMaxMemoryConfigValue(int osId) {
        return oses64bit.contains(osId)
                ? (osArchitectures.get(osId).getFamily() == ArchitectureType.ppc
                        ? ConfigValues.VMPpc64BitMaxMemorySizeInMB
                        : ConfigValues.VM64BitMaxMemorySizeInMB)
                : ConfigValues.VM32BitMaxMemorySizeInMB;
    }

    public void getAuthzExtensionsNames(AsyncQuery<List<String>> aQuery) {
        aQuery.converterCallback = new ListConverter<>();
        Frontend.getInstance().runQuery(QueryType.GetDomainList, new QueryParametersBase(), aQuery);
    }

    public void getAAANamespaces(AsyncQuery<Map<String, List<String>>> aQuery) {
        aQuery.converterCallback = new MapConverter<>();
        Frontend.getInstance().runQuery(QueryType.GetAvailableNamespaces, new QueryParametersBase(), aQuery);
    }

    public void getAAAProfilesEntriesList(AsyncQuery<List<ProfileEntry>> aQuery) {
        aQuery.converterCallback = new ListConverter<>();
        Frontend.getInstance().runQuery(QueryType.GetAAAProfileList, new QueryParametersBase(), aQuery);
    }

    public void getRoleList(AsyncQuery<List<Role>> aQuery) {
        aQuery.converterCallback = new ListConverter<>();
        Frontend.getInstance().runQuery(QueryType.GetAllRoles, new QueryParametersBase(), aQuery);
    }

    public void getStorageDomainById(AsyncQuery<StorageDomain> aQuery, Guid storageDomainId) {
        aQuery.converterCallback = new CastingConverter<>();
        Frontend.getInstance().runQuery(QueryType.GetStorageDomainById,
                new IdQueryParameters(storageDomainId),
                aQuery);
    }

    public void getStorageDomainByName(AsyncQuery<StorageDomainStatic> aQuery, String storageDomainName) {
        aQuery.converterCallback = new CastingConverter<>();
        Frontend.getInstance().runQuery(QueryType.GetStorageDomainByName,
                new NameQueryParameters(storageDomainName),
                aQuery);
    }

    // NOTE: This logic is duplicated in ovirt-web-ui for disk and VM creation.  Any changes here should also be made there.
    public VolumeFormat getDiskVolumeFormat(VolumeType volumeType, StorageType storageType) {
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
        } else if (storageType.isManagedBlockStorage()) {
            return volumeType == VolumeType.Preallocated ? VolumeFormat.RAW : VolumeFormat.Unassigned;
        } else {
            return VolumeFormat.Unassigned;
        }
    }

    // NOTE: This logic is duplicated in ovirt-web-ui for disk and VM creation.  Any changes here should also be made there.
    public VolumeType getVolumeType(VolumeFormat volumeFormat, StorageType storageType, VM vm, Image diskImage) {
        switch (volumeFormat) {
        case COW:
            return VolumeType.Sparse;
        case RAW:
        default:
            boolean copyPreallocatedFileBasedDiskSupported =
                    vm != null ? isCopyPreallocatedFileBasedDiskSupported(vm.getCompatibilityVersion()) : false;
            // if diskImage provided it means that we want to use the source image volume type,
            // otherwise we set the volume type as sparse by default for file-based storage domain
            VolumeType fileBasedVolumeType;
            if (copyPreallocatedFileBasedDiskSupported) {
                fileBasedVolumeType = diskImage != null ? diskImage.getVolumeType() : VolumeType.Sparse;
            } else {
                fileBasedVolumeType = VolumeType.Sparse;
            }
            return storageType.isFileDomain() ? fileBasedVolumeType : VolumeType.Preallocated;
        }
    }

    public void getClusterNetworkList(AsyncQuery<List<Network>> aQuery, Guid clusterId) {
        // do not replace a converter = just add if none provided
        if (aQuery.converterCallback == null) {
            aQuery.converterCallback = new ListConverter<>();
        }

        Frontend.getInstance().runQuery(QueryType.GetAllNetworksByClusterId, new IdQueryParameters(clusterId), aQuery);
    }

    public void getClusterNetworkSyncStatus(AsyncQuery<Boolean> aQuery, Guid clusterId) {
        aQuery.converterCallback = returnValue -> (Boolean) returnValue;
        Frontend.getInstance().runQuery(QueryType.GetClusterNetworkSyncStatus, new IdQueryParameters(clusterId), aQuery);
    }

    public void getAllNetworkQos(Guid dcId, AsyncQuery<List<NetworkQoS>> query) {
        query.converterCallback = new ListConverter<NetworkQoS>() {

            @Override
            public List<NetworkQoS> convert(List<NetworkQoS> returnValue) {
                List<NetworkQoS> qosList = super.convert(returnValue);
                qosList.add(0, NetworkQoSModel.EMPTY_QOS);
                return qosList;
            }
        };
        Frontend.getInstance().runQuery(QueryType.GetAllNetworkQosByStoragePoolId, new IdQueryParameters(dcId), query);
    }

    public void getAllHostNetworkQos(Guid dcId, AsyncQuery<List<HostNetworkQos>> query) {
        query.converterCallback = new ListConverter<HostNetworkQos>() {

            @Override
            public List<HostNetworkQos> convert(List<HostNetworkQos> returnValue) {
                List<HostNetworkQos> qosList = super.convert(returnValue);
                qosList.add(0, NetworkModel.EMPTY_HOST_NETWORK_QOS);
                return qosList;
            }
        };
        Frontend.getInstance().runQuery(QueryType.GetAllQosByStoragePoolIdAndType,
                new QosQueryParameterBase(dcId, QosType.HOSTNETWORK),
                query);
    }

    public void getDataCenterById(AsyncQuery<StoragePool> aQuery, Guid dataCenterId) {
        aQuery.converterCallback = new CastingConverter<>();
        Frontend.getInstance().runQuery(QueryType.GetStoragePoolById,
                new IdQueryParameters(dataCenterId).withoutRefresh(),
                aQuery);
    }

    public void getNetworkLabelsByDataCenterId(Guid dataCenterId, AsyncQuery<SortedSet<String>> query) {
        query.converterCallback = returnValue -> {
            SortedSet<String> sortedSet = new TreeSet<>(new LexoNumericComparator());
            sortedSet.addAll((Collection<String>) returnValue);
            return sortedSet;
        };
        Frontend.getInstance().runQuery(QueryType.GetNetworkLabelsByDataCenterId,
                new IdQueryParameters(dataCenterId),
                query);
    }

    public void getWatchdogByVmId(AsyncQuery aQuery, Guid vmId) {
        Frontend.getInstance().runQuery(QueryType.GetWatchdog, new IdQueryParameters(vmId), aQuery);
    }

    public void getTemplateById(AsyncQuery<VmTemplate> aQuery, Guid templateId) {
        aQuery.converterCallback = new CastingConverter<>();
        Frontend.getInstance().runQuery(QueryType.GetVmTemplate, new GetVmTemplateParameters(templateId), aQuery);
    }

    public void countAllTemplates(AsyncQuery<Integer> aQuery) {
        aQuery.converterCallback = new CastingConverter<>();
        Frontend.getInstance().runQuery(QueryType.GetVmTemplateCount, new QueryParametersBase(), aQuery);
    }

    public void getHostList(AsyncQuery<List<VDS>> aQuery) {
        getHostListByStatus(aQuery, null);
    }

    public void getHostList(AsyncQuery<List<VDS>> aQuery, boolean doRefresh) {
        getHostListByStatus(aQuery, null, doRefresh);
    }

    public void getHostListByStatus(AsyncQuery<List<VDS>> aQuery, VDSStatus status) {
        getHostListByStatus(aQuery, status, true);
    }

    public void getHostListByStatus(AsyncQuery<List<VDS>> aQuery, VDSStatus status, boolean doRefresh) {
        aQuery.converterCallback = new ListConverter<>();
        SearchParameters searchParameters =
                new SearchParameters("Host: " + (status == null ? "" : ("status=" + status.name())), SearchType.VDS); //$NON-NLS-1$ //$NON-NLS-2$
        searchParameters.setMaxCount(9999);
        Frontend.getInstance().runQuery(QueryType.Search,
                doRefresh ? searchParameters : searchParameters.withoutRefresh(),
                aQuery);
    }

    public void getHostsForStorageOperation(AsyncQuery<List<VDS>> aQuery, Guid storagePoolId, boolean localFsOnly) {
        aQuery.converterCallback = new ListConverter<>();
        Frontend.getInstance().runQuery(QueryType.GetHostsForStorageOperation,
                new GetHostsForStorageOperationParameters(storagePoolId, localFsOnly),
                aQuery);
    }

    public void getVolumeList(AsyncQuery<List<GlusterVolumeEntity>> aQuery, String clusterName) {
        getVolumeList(aQuery, clusterName, true);
    }

    public void getVolumeList(AsyncQuery<List<GlusterVolumeEntity>> aQuery, String clusterName, boolean doRefresh) {

        if ((ApplicationModeHelper.getUiMode().getValue() & ApplicationMode.GlusterOnly.getValue()) == 0) {
            aQuery.getAsyncCallback().onSuccess(new ArrayList<>());
            return;
        }
        aQuery.converterCallback = new ListConverter<>();
        SearchParameters searchParameters;
        searchParameters =
                clusterName == null ? new SearchParameters("Volumes:", SearchType.GlusterVolume) //$NON-NLS-1$
                        : new SearchParameters("Volumes: cluster.name=" + clusterName, SearchType.GlusterVolume); //$NON-NLS-1$
        searchParameters.setMaxCount(9999);
        if (!doRefresh) {
            searchParameters.withoutRefresh();
        }
        Frontend.getInstance().runQuery(QueryType.Search, searchParameters, aQuery);
    }

    public void getGlusterVolumeOptionInfoList(AsyncQuery aQuery, Guid clusterId) {
        Frontend.getInstance().runQuery(QueryType.GetGlusterVolumeOptionsInfo,
                new GlusterParameters(clusterId),
                aQuery);
    }

    public void getHostSshPublicKey(AsyncQuery<String> aQuery, String hostAddress, Integer hostPort) {
        aQuery.converterCallback = new StringConverter();
        Frontend.getInstance().runQuery(QueryType.GetServerSSHPublicKey,
                new ServerParameters(hostAddress, hostPort),
                aQuery);
    }

    public void getEngineSshPublicKey(AsyncQuery<String> aQuery) {
        aQuery.converterCallback = new StringConverter();
        Frontend.getInstance().runQuery(QueryType.GetEngineSSHPublicKey, new QueryParametersBase(), aQuery);
    }

    public void getGlusterHosts(AsyncQuery aQuery, String hostAddress, String rootPassword, String sshPublicKey) {
        GlusterServersQueryParameters parameters = new GlusterServersQueryParameters(hostAddress, rootPassword);
        parameters.setSshPublicKey(sshPublicKey);
        Frontend.getInstance().runQuery(QueryType.GetGlusterServersForImport,
                parameters,
                aQuery);
    }

    public void getClusterGlusterServices(AsyncQuery<GlusterVolumeAdvancedDetails> aQuery, Guid clusterId) {
        aQuery.converterCallback = new CastingConverter<>();
        // Passing empty values for Volume and Brick to get the services of all the volumes/hosts in the cluster
        GlusterVolumeAdvancedDetailsParameters parameters =
                new GlusterVolumeAdvancedDetailsParameters(clusterId, null, null, false); // $NON-NLS-1$ //$NON-NLS-2$
        Frontend.getInstance().runQuery(QueryType.GetGlusterVolumeAdvancedDetails,
                parameters,
                aQuery);
    }

    public void getGlusterVolumeBrickDetails(AsyncQuery aQuery, Guid clusterId, Guid volumeId, Guid brickId) {
        GlusterVolumeAdvancedDetailsParameters parameters =
                new GlusterVolumeAdvancedDetailsParameters(clusterId, volumeId, brickId, true);
        Frontend.getInstance().runQuery(QueryType.GetGlusterVolumeAdvancedDetails,
                parameters,
                aQuery);
    }

    public void getGlusterHostsNewlyAdded(AsyncQuery<Map<String, Pair<String, String>>> aQuery,
            Guid clusterId,
            boolean isSshPublicKeyRequired) {
        aQuery.converterCallback = new CastingConverter<>();
        Frontend.getInstance()
                .runQuery(QueryType.GetAddedGlusterServers,
                        new AddedGlusterServersParameters(clusterId, isSshPublicKeyRequired),
                        aQuery);
    }

    public void isAnyHostUpInCluster(AsyncQuery<Boolean> aQuery, String clusterName) {
        aQuery.converterCallback = new IsNonEmptyCollectionConverter();
        getUpHostListByCluster(aQuery, clusterName, 1);
    }

    public void getGlusterHooks(AsyncQuery<List<GlusterHookEntity>> aQuery, Guid clusterId) {
        aQuery.converterCallback = new ListConverter<>();
        Frontend.getInstance().runQuery(QueryType.GetGlusterHooks, new GlusterParameters(clusterId), aQuery);
    }

    public void getGlusterBricksForServer(AsyncQuery<List<GlusterBrickEntity>> aQuery, Guid serverId) {
        aQuery.converterCallback = new ListConverter<>();
        Frontend.getInstance().runQuery(QueryType.GetGlusterVolumeBricksByServerId,
                new IdQueryParameters(serverId),
                aQuery);
    }

    public void getGlusterVolumeGeoRepStatusForMasterVolume(AsyncQuery<List<GlusterGeoRepSession>> aQuery,
            Guid masterVolumeId) {
        aQuery.converterCallback = new ListConverter<>();
        Frontend.getInstance().runQuery(QueryType.GetGlusterVolumeGeoRepSessions,
                new IdQueryParameters(masterVolumeId),
                aQuery);
    }

    public void getGlusterVolumeGeoRepRecommendationViolations(
            AsyncQuery<List<GlusterGeoRepNonEligibilityReason>> aQuery,
            Guid masterVolumeId,
            Guid slaveVolumeId) {
        aQuery.converterCallback = new ListConverter<>();
        Frontend.getInstance().runQuery(QueryType.GetNonEligibilityReasonsOfVolumeForGeoRepSession,
                new GlusterVolumeGeoRepEligibilityParameters(masterVolumeId, slaveVolumeId),
                aQuery);
    }

    public void getGlusterVolumeSnapshotsForVolume(AsyncQuery<List<GlusterVolumeSnapshotEntity>> aQuery,
            Guid volumeId) {
        aQuery.converterCallback = new ListConverter<>();
        Frontend.getInstance().runQuery(QueryType.GetGlusterVolumeSnapshotsByVolumeId,
                new IdQueryParameters(volumeId),
                aQuery);
    }

    public void getVolumeSnapshotSchedule(AsyncQuery<GlusterVolumeSnapshotSchedule> aQuery, Guid volumeId) {
        aQuery.converterCallback = new CastingConverter<>();
        Frontend.getInstance().runQuery(QueryType.GetGlusterVolumeSnapshotScheduleByVolumeId,
                new IdQueryParameters(volumeId),
                aQuery);
    }

    public void getIsGlusterVolumeSnapshotCliScheduleEnabled(AsyncQuery<Boolean> aQuery, Guid clusterId) {
        aQuery.converterCallback = new CastingConverter<>();
        Frontend.getInstance().runQuery(QueryType.GetGlusterVolumeSnapshotCliScheduleFlag,
                new IdQueryParameters(clusterId),
                aQuery);
    }

    public void getGlusterHook(AsyncQuery<GlusterHookEntity> aQuery, Guid hookId, boolean includeServerHooks) {
        aQuery.converterCallback = new CastingConverter<>();
        Frontend.getInstance().runQuery(QueryType.GetGlusterHookById,
                new GlusterHookQueryParameters(hookId, includeServerHooks),
                aQuery);
    }

    public void getGlusterHookContent(AsyncQuery<String> aQuery, Guid hookId, Guid serverId) {
        aQuery.converterCallback = new StringConverter();
        GlusterHookContentQueryParameters parameters = new GlusterHookContentQueryParameters(hookId);
        parameters.setGlusterServerId(serverId);
        Frontend.getInstance().runQuery(QueryType.GetGlusterHookContent, parameters, aQuery);
    }

    public void getGlusterSwiftServices(AsyncQuery<List<GlusterServerService>> aQuery, Guid serverId) {
        aQuery.converterCallback = new ListConverter<>();
        Frontend.getInstance().runQuery(QueryType.GetGlusterServerServicesByServerId,
                new GlusterServiceQueryParameters(serverId,
                        ServiceType.GLUSTER_SWIFT),
                aQuery);
    }

    public void getClusterGlusterSwiftService(AsyncQuery<GlusterClusterService> aQuery, Guid clusterId) {

        aQuery.converterCallback = new GetFirstConverter<>();
        Frontend.getInstance().runQuery(QueryType.GetGlusterClusterServiceByClusterId,
                new GlusterServiceQueryParameters(clusterId,
                        ServiceType.GLUSTER_SWIFT),
                aQuery);
    }

    public void getGlusterSwiftServerServices(AsyncQuery<List<GlusterServerService>> aQuery, Guid clusterId) {

        aQuery.converterCallback = new ListConverter<>();
        Frontend.getInstance().runQuery(QueryType.GetGlusterServerServicesByClusterId,
                new GlusterServiceQueryParameters(clusterId,
                        ServiceType.GLUSTER_SWIFT),
                aQuery);
    }

    public void getGlusterRebalanceStatus(AsyncQuery<QueryReturnValue> aQuery, Guid clusterId, Guid volumeId) {
        aQuery.setHandleFailure(true);
        GlusterVolumeQueriesParameters parameters = new GlusterVolumeQueriesParameters(clusterId, volumeId);
        Frontend.getInstance().runQuery(QueryType.GetGlusterVolumeRebalanceStatus, parameters, aQuery);
    }

    public void getGlusterSnapshotConfig(AsyncQuery<QueryReturnValue> aQuery, Guid clusterId, Guid volumeId) {
        aQuery.setHandleFailure(true);
        Frontend.getInstance().runQuery(QueryType.GetGlusterVolumeSnapshotConfig,
                new GlusterVolumeQueriesParameters(clusterId, volumeId),
                aQuery);
    }

    public void getGlusterVolumeProfilingStatistics(AsyncQuery<QueryReturnValue> aQuery,
            Guid clusterId,
            Guid volumeId,
            boolean nfs) {
        aQuery.setHandleFailure(true);
        GlusterVolumeProfileParameters parameters = new GlusterVolumeProfileParameters(clusterId, volumeId, nfs);
        Frontend.getInstance().runQuery(QueryType.GetGlusterVolumeProfileInfo, parameters, aQuery);
    }

    public void getGlusterRemoveBricksStatus(AsyncQuery<QueryReturnValue> aQuery,
            Guid clusterId,
            Guid volumeId,
            List<GlusterBrickEntity> bricks) {
        aQuery.setHandleFailure(true);
        GlusterVolumeRemoveBricksQueriesParameters parameters =
                new GlusterVolumeRemoveBricksQueriesParameters(clusterId, volumeId, bricks);
        Frontend.getInstance().runQuery(QueryType.GetGlusterVolumeRemoveBricksStatus, parameters, aQuery);
    }

    public void getSearchResultsLimit(AsyncQuery<Integer> aQuery) {
        aQuery.converterCallback = new DefaultValueConverter<>(100);
        getConfigFromCache(
                new GetConfigurationValueParameters(ConfigValues.SearchResultsLimit,
                        getDefaultConfigurationVersion()),
                aQuery);
    }

    public Map<Version, Map<String, String>> getCustomPropertiesList() {
        return customPropertiesList;
    }

    public void isTemplateNameUnique(AsyncQuery<Boolean> aQuery, String templateName, Guid datacenterId) {
        aQuery.converterCallback = source -> source != null && !(Boolean) source;
        NameQueryParameters params = new NameQueryParameters(templateName);
        params.setDatacenterId(datacenterId);
        Frontend.getInstance().runQuery(QueryType.IsVmTemlateWithSameNameExist,
                params,
                aQuery);
    }

    public void isVmNameUnique(AsyncQuery<Boolean> aQuery, String name, Guid datacenterId) {
        aQuery.converterCallback = source -> source != null && !(Boolean) source;
        NameQueryParameters params = new NameQueryParameters(name);
        params.setDatacenterId(datacenterId);
        Frontend.getInstance().runQuery(QueryType.IsVmWithSameNameExist, params, aQuery);
    }

    public void getClustersHavingHosts(AsyncQuery<List<Cluster>> aQuery) {
        aQuery.converterCallback = new SortListByNameConverter<>();
        Frontend.getInstance().runQuery(QueryType.GetAllClustersHavingHosts,
                new QueryParametersBase(),
                aQuery);
    }

    public void getAllVmTemplates(AsyncQuery<List<VmTemplate>> aQuery, final boolean refresh) {
        aQuery.converterCallback = new TemplateConverter();
        QueryParametersBase params = new QueryParametersBase();
        params.setRefresh(refresh);
        Frontend.getInstance().runQuery(QueryType.GetAllVmTemplates, params, aQuery);
    }

    public void getStorageConnectionById(AsyncQuery<StorageServerConnections> aQuery, String id, boolean isRefresh) {
        aQuery.converterCallback = source -> source != null ? (StorageServerConnections) source : null;
        StorageServerConnectionQueryParametersBase params = new StorageServerConnectionQueryParametersBase(id);
        params.setRefresh(isRefresh);
        Frontend.getInstance().runQuery(QueryType.GetStorageServerConnectionById, params, aQuery);
    }

    public void getNumberOfImagesOnStorageDomain(AsyncQuery<Long> aQuery, Guid storageDomainId) {
        aQuery.converterCallback = source -> (Long) source;
        IdQueryParameters params = new IdQueryParameters(storageDomainId);
        Frontend.getInstance().runQuery(QueryType.GetNumberOfImagesByStorageDomainId, params, aQuery);
    }

    public void getDataCentersByStorageDomain(AsyncQuery<List<StoragePool>> aQuery, Guid storageDomainId) {
        aQuery.converterCallback = new CastingConverter<>();
        Frontend.getInstance().runQuery(QueryType.GetStoragePoolsByStorageDomainId,
                new IdQueryParameters(storageDomainId),
                aQuery);
    }

    public void getDataCenterVersions(AsyncQuery<List<Version>> aQuery, Guid dataCenterId) {
        aQuery.converterCallback = new SortListConverter<>();
        IdQueryParameters tempVar = new IdQueryParameters(dataCenterId);
        Frontend.getInstance().runQuery(QueryType.GetAvailableClusterVersionsByStoragePool, tempVar, aQuery);
    }

    public void getDataCenterMaxNameLength(AsyncQuery<Integer> aQuery) {
        aQuery.converterCallback = new DefaultValueConverter<>(1);
        getConfigFromCache(
                new GetConfigurationValueParameters(ConfigValues.StoragePoolNameSizeLimit,
                        getDefaultConfigurationVersion()),
                aQuery);
    }

    public void getClusterServerMemoryOverCommit(AsyncQuery<Integer> aQuery) {
        aQuery.converterCallback = new DefaultValueConverter<>(0);
        getConfigFromCache(
                new GetConfigurationValueParameters(ConfigValues.MaxVdsMemOverCommitForServers,
                        getDefaultConfigurationVersion()),
                aQuery);
    }

    public void getClusterDesktopMemoryOverCommit(AsyncQuery<Integer> aQuery) {
        aQuery.converterCallback = new DefaultValueConverter<>(0);
        getConfigFromCache(
                new GetConfigurationValueParameters(ConfigValues.MaxVdsMemOverCommit,
                        getDefaultConfigurationVersion()),
                aQuery);
    }

    public void getAllowClusterWithVirtGlusterEnabled(AsyncQuery<Boolean> aQuery) {
        aQuery.converterCallback = new DefaultValueConverter<>(Boolean.TRUE);
        getConfigFromCache(
                new GetConfigurationValueParameters(ConfigValues.AllowClusterWithVirtGlusterEnabled,
                        getDefaultConfigurationVersion()),
                aQuery);
    }

    public void getCPUList(AsyncQuery<List<ServerCpu>> aQuery, Version version) {
        aQuery.converterCallback = new ListConverter<>();
        Frontend.getInstance().runQuery(QueryType.GetAllServerCpuList,
                new GetAllServerCpuListParameters(version),
                aQuery);
    }

    public void getCpuByFlags(AsyncQuery<ServerCpu> aQuery, String cpuFlags, Version newVersion) {
        aQuery.converterCallback = new DefaultConverter<>();
        Frontend.getInstance().runQuery(QueryType.GetCpuByFlags,
                new GetCpuByFlagsParameters(cpuFlags, newVersion),
                aQuery);
    }

    public void getPmTypeList(AsyncQuery<List<String>> aQuery, Version version) {
        aQuery.converterCallback = source -> {
            ArrayList<String> list = new ArrayList<>();
            if (source != null) {
                String[] array = ((String) source).split("[,]", -1); //$NON-NLS-1$
                Collections.addAll(list, array);
            }
            return list;
        };
        GetConfigurationValueParameters param = new GetConfigurationValueParameters(ConfigValues.VdsFenceType);
        param.setVersion(version != null ? version.toString() : getDefaultConfigurationVersion());
        Frontend.getInstance().runQuery(QueryType.GetFenceConfigurationValue, param, aQuery);
    }

    public void getPmOptions(AsyncQuery<List<String>> aQuery, final String pmType, String version) {
        aQuery.converterCallback = source -> {
            Map<String, ArrayList<String>> cachedPmMap = new HashMap<>();
            Map<String, Map<String, Object>> dict = (Map<String, Map<String, Object>>) source;
            for (Entry<String, Map<String, Object>> pair : dict.entrySet()) {
                ArrayList<String> list = new ArrayList<>();
                for (Entry<String, Object> p : pair.getValue().entrySet()) {
                    list.add(p.getKey());
                }

                cachedPmMap.put(pair.getKey(), list);
            }
            return cachedPmMap.get(pmType);
        };
        Frontend.getInstance().runQuery(QueryType.GetAgentFenceOptions,
                new GetAgentFenceOptionsQueryParameters(version),
                aQuery);
    }

    public void getNetworkList(AsyncQuery<List<Network>> aQuery, Guid dataCenterId) {
        aQuery.converterCallback = new ListConverter<>();

        Frontend.getInstance().runQuery(QueryType.GetAllNetworks, new IdQueryParameters(dataCenterId), aQuery);
    }

    public void getISOStorageDomainList(AsyncQuery<List<StorageDomain>> aQuery) {
        aQuery.converterCallback = source -> {
            if (source != null) {
                ArrayList<StorageDomain> allStorageDomains =
                        (ArrayList<StorageDomain>) source;
                ArrayList<StorageDomain> isoStorageDomains = new ArrayList<>();
                for (StorageDomain storageDomain : allStorageDomains) {
                    if (storageDomain.getStorageDomainType() == StorageDomainType.ISO) {
                        isoStorageDomains.add(storageDomain);
                    }
                }
                return isoStorageDomains;
            }
            return new ArrayList<>();
        };

        SearchParameters searchParams = new SearchParameters("Storage:", SearchType.StorageDomain); //$NON-NLS-1$
        searchParams.setMaxCount(9999);

        Frontend.getInstance().runQuery(QueryType.Search, searchParams, aQuery);
    }

    public void getStorageDomainList(AsyncQuery<List<StorageDomain>> aQuery) {
        aQuery.converterCallback = new ListConverter<>();

        SearchParameters searchParams = new SearchParameters("Storage:", SearchType.StorageDomain); //$NON-NLS-1$
        searchParams.setMaxCount(9999);

        Frontend.getInstance().runQuery(QueryType.Search, searchParams, aQuery);
    }

    public void getLocalStorageHost(AsyncQuery<VDS> aQuery, String dataCenterName) {
        aQuery.converterCallback = new GetFirstConverter<>();
        SearchParameters sp = new SearchParameters("hosts: datacenter=" + dataCenterName, SearchType.VDS); //$NON-NLS-1$
        Frontend.getInstance().runQuery(QueryType.Search, sp, aQuery);
    }

    public void getStorageDomainsByConnection(AsyncQuery<List<StorageDomain>> aQuery,
            Guid storagePoolId,
            String connectionPath) {
        aQuery.converterCallback = new CastingConverter<>();

        GetStorageDomainsByConnectionParameters param = new GetStorageDomainsByConnectionParameters();
        param.setConnection(connectionPath);
        if (storagePoolId != null) {
            param.setStoragePoolId(storagePoolId);
        }

        Frontend.getInstance().runQuery(QueryType.GetStorageDomainsByConnection, param, aQuery);
    }

    public void getManagedBlockStorageDomainById(AsyncQuery<ManagedBlockStorage> aQuery, Guid cinderStorageId) {
        runQueryByIdParameter(QueryType.GetManagedBlockStorageDomainById, aQuery, cinderStorageId);
    }

    public void getManagedBlockStorageDomainsByDrivers(AsyncQuery<List<ManagedBlockStorage>> aQuery,
            Map<String, Object> driverOption,
            Map<String, Object> driverSensitiveOption) {
        aQuery.converterCallback = new CastingConverter<>();

        Frontend.getInstance()
                .runQuery(QueryType.GetManagedBlockStorageDomainsByDrivers,
                        new GetManagedBlockStorageDomainsByDriversParameters(driverOption, driverSensitiveOption),
                        aQuery);
    }

    public void getExistingStorageDomainList(AsyncQuery<List<StorageDomain>> aQuery,
            Guid hostId,
            StorageDomainType domainType,
            StorageType storageType,
            String path) {
        aQuery.converterCallback = new CastingConverter<>();

        Frontend.getInstance().runQuery(QueryType.GetExistingStorageDomainList,
                new GetExistingStorageDomainListParameters(hostId,
                        storageType,
                        domainType,
                        path),
                aQuery);
    }

    public void getStorageDomainMaxNameLength(AsyncQuery<Integer> aQuery) {
        aQuery.converterCallback = new DefaultValueConverter<>(1);
        getConfigFromCache(
                new GetConfigurationValueParameters(ConfigValues.StorageDomainNameSizeLimit,
                        getDefaultConfigurationVersion()),
                aQuery);
    }

    public void isStorageDomainNameUnique(AsyncQuery<Boolean> aQuery, String name) {
        aQuery.converterCallback = source -> {
            if (source != null) {
                ArrayList<StorageDomain> storageDomains = (ArrayList<StorageDomain>) source;
                return storageDomains.isEmpty();
            }

            return false;
        };
        Frontend.getInstance().runQuery(QueryType.Search, new SearchParameters("Storage: name=" + name, //$NON-NLS-1$
                SearchType.StorageDomain), aQuery);
    }

    public void getNetworkConnectivityCheckTimeoutInSeconds(AsyncQuery<Integer> aQuery) {
        aQuery.converterCallback = new DefaultValueConverter<>(120);
        getConfigFromCache(
                new GetConfigurationValueParameters(ConfigValues.NetworkConnectivityCheckTimeoutInSeconds,
                        getDefaultConfigurationVersion()),
                aQuery);
    }

    public void getMaxSpmPriority(AsyncQuery<Integer> aQuery) {
        aQuery.converterCallback = new DefaultValueConverter<>(0);
        aQuery.getAsyncCallback().onSuccess(10);
    }

    public void getDefaultSpmPriority(AsyncQuery<Integer> aQuery) {
        aQuery.converterCallback = new DefaultValueConverter<>(0);
        aQuery.getAsyncCallback().onSuccess(5);
    }

    public void getDefaultPmProxyPreferences(AsyncQuery<String> query) {
        getConfigFromCache(
                new GetConfigurationValueParameters(ConfigValues.FenceProxyDefaultPreferences,
                        getDefaultConfigurationVersion()),
                query);
    }

    public void getRootTag(AsyncQuery<Tags> aQuery) {
        aQuery.converterCallback = source -> {
            if (source != null) {
                Tags tag = (Tags) source;

                Tags root =
                        new Tags(tag.getDescription(),
                                tag.getParentId(),
                                tag.getIsReadonly(),
                                tag.getTagId(),
                                tag.getTagName());
                if (tag.getChildren() != null) {
                    fillTagsRecursive(root, tag.getChildren());
                }

                return root;
            }

            return new Tags();
        };
        Frontend.getInstance().runQuery(QueryType.GetRootTag, new QueryParametersBase(), aQuery);
    }

    private void setAttachedTagsConverter(AsyncQuery<List<Tags>> aQuery) {
        aQuery.converterCallback = source -> {
            if (source != null) {
                ArrayList<Tags> ret = new ArrayList<>();
                for (Tags tags : (ArrayList<Tags>) source) {
                    if (tags.getType() == TagsType.GeneralTag) {
                        ret.add(tags);
                    }
                }
                return ret;
            }

            return new ArrayList<>();
        };
    }

    public void getAttachedTagsToVm(AsyncQuery<List<Tags>> aQuery, Guid id) {
        setAttachedTagsConverter(aQuery);

        Frontend.getInstance().runQuery(QueryType.GetTagsByVmId, new GetTagsByVmIdParameters(id.toString()), aQuery);
    }

    public void getAttachedTagsToUser(AsyncQuery<List<Tags>> aQuery, Guid id) {
        setAttachedTagsConverter(aQuery);

        Frontend.getInstance().runQuery(QueryType.GetTagsByUserId,
                new GetTagsByUserIdParameters(id.toString()),
                aQuery);
    }

    public void getAttachedTagsToUserGroup(AsyncQuery<List<Tags>> aQuery, Guid id) {
        setAttachedTagsConverter(aQuery);

        Frontend.getInstance().runQuery(QueryType.GetTagsByUserGroupId,
                new GetTagsByUserGroupIdParameters(id.toString()),
                aQuery);
    }

    public void getAttachedTagsToHost(AsyncQuery<List<Tags>> aQuery, Guid id) {
        setAttachedTagsConverter(aQuery);

        Frontend.getInstance().runQuery(QueryType.GetTagsByVdsId, new GetTagsByVdsIdParameters(id.toString()), aQuery);
    }

    public void getLunsByVgId(AsyncQuery<List<LUNs>> aQuery, String vgId, Guid vdsId) {
        aQuery.converterCallback = new ListConverter<>();
        GetLunsByVgIdParameters params = new GetLunsByVgIdParameters(vgId, vdsId);
        Frontend.getInstance().runQuery(QueryType.GetLunsByVgId, params, aQuery);
    }

    public void getAllTemplatesFromExportDomain(AsyncQuery<Map<VmTemplate, ArrayList<DiskImage>>> aQuery,
            Guid storagePoolId,
            Guid storageDomainId) {
        aQuery.converterCallback = new MapConverter<>();
        GetAllFromExportDomainQueryParameters getAllFromExportDomainQueryParamenters =
                new GetAllFromExportDomainQueryParameters(storagePoolId, storageDomainId);
        Frontend.getInstance().runQuery(QueryType.GetTemplatesFromExportDomain,
                getAllFromExportDomainQueryParamenters,
                aQuery);
    }

    public void getUpHostListByCluster(AsyncQuery aQuery, String clusterName, Integer maxCount) {
        SearchParameters searchParameters =
                new SearchParameters("Host: cluster = " + clusterName + " and status = up", SearchType.VDS); //$NON-NLS-1$ //$NON-NLS-2$
        if (maxCount != null) {
            searchParameters.setMaxCount(maxCount);
        }
        Frontend.getInstance().runQuery(QueryType.Search, searchParameters, aQuery);
    }

    public void getVmNicList(AsyncQuery<List<VmNetworkInterface>> aQuery, Guid id) {
        aQuery.converterCallback = new ListConverter<>();

        Frontend.getInstance().runQuery(QueryType.GetVmInterfacesByVmId, new IdQueryParameters(id), aQuery);
    }

    public void getTemplateNicList(AsyncQuery<List<VmNetworkInterface>> aQuery, Guid id) {
        aQuery.converterCallback = new ListConverter<>();

        Frontend.getInstance().runQuery(QueryType.GetTemplateInterfacesByTemplateId, new IdQueryParameters(id), aQuery);
    }

    public void getVmSnapshotList(AsyncQuery<List<Snapshot>> aQuery, Guid id) {
        aQuery.converterCallback = new ListConverter<>();

        Frontend.getInstance().runQuery(QueryType.GetAllVmSnapshotsByVmId, new IdQueryParameters(id), aQuery);
    }

    public void getVmsRunningOnOrMigratingToVds(AsyncQuery<List<VM>> aQuery, Guid id) {
        aQuery.converterCallback = new ListConverter<>();

        Frontend.getInstance().runQuery(QueryType.GetVmsRunningOnOrMigratingToVds,
                new IdQueryParameters(id),
                aQuery);
    }

    public void getVmsPinnedToHost(AsyncQuery<List<VM>> aQuery, Guid id) {
        aQuery.converterCallback = new ListConverter<>();

        Frontend.getInstance().runQuery(QueryType.GetVmsPinnedToHost,
                new IdQueryParameters(id),
                aQuery);
    }

    public void getAllVmsRunningForMultipleVds(AsyncQuery<Map<Guid, List<VM>>> aQuery, List<Guid> vdsIds) {
        aQuery.converterCallback = new CastingConverter<>();

        Frontend.getInstance().runQuery(QueryType.GetAllVmsRunningForMultipleVds,
                new IdsQueryParameters(vdsIds),
                aQuery);
    }

    public void doesStorageDomainContainEntityWithDisksOnMultipleSDs(AsyncQuery<Boolean> aQuery, Guid sdId) {
        aQuery.converterCallback = new DefaultValueConverter<>(Boolean.FALSE);

        Frontend.getInstance().runQuery(QueryType.DoesStorageDomainContainEntityWithDisksOnMultipleSDs,
                new IdQueryParameters(sdId),
                aQuery);
    }

    public void getAllMetadataAndMemoryDisksOfSnapshotsOnDifferentStorageDomains(
            AsyncQuery<List<Guid>> aQuery, Guid sdId) {
        aQuery.converterCallback = new ListConverter<>();

        Frontend.getInstance().runQuery(QueryType.GetAllMetadataAndMemoryDisksOfSnapshotsOnDifferentStorageDomains,
                new IdQueryParameters(sdId),
                aQuery);
    }

    public void getAllEntitiesWithLeaseOnStorageDomain(AsyncQuery<List<VmBase>> aQuery, Guid sdId){
        aQuery.converterCallback = new ListConverter<>();

        Frontend.getInstance().runQuery(QueryType.GetEntitiesWithLeaseByStorageId,
                new IdQueryParameters(sdId),
                aQuery);
    }

    public void getVmsFromExternalServer(AsyncQuery aQuery,
            Guid dataCenterId,
            Guid vdsId,
            String url,
            String username,
            String password,
            OriginType originType,
            List<String> vmsToImport) {

        aQuery.converterCallback = new ListConverter<>();

        Frontend.getInstance().runQuery(QueryType.GetVmsFromExternalProvider,
                new GetVmsFromExternalProviderQueryParameters(url,
                        username,
                        password,
                        originType,
                        vdsId,
                        dataCenterId,
                        vmsToImport),
                aQuery);
    }

    public void getVmFromOva(AsyncQuery<QueryReturnValue> aQuery, Guid vdsId, String path) {
        aQuery.setHandleFailure(true);
        GetVmFromOvaQueryParameters params = new GetVmFromOvaQueryParameters(vdsId, path);
        params.setListDirectory(true);
        Frontend.getInstance().runQuery(
                QueryType.GetVmFromOva,
                params,
                aQuery);
    }

    public void getTemplateFromOva(AsyncQuery<QueryReturnValue> aQuery, Guid vdsId, String path) {
        aQuery.setHandleFailure(true);
        GetVmFromOvaQueryParameters params = new GetVmFromOvaQueryParameters(vdsId, path);
        params.setListDirectory(true);
        Frontend.getInstance().runQuery(
                QueryType.GetVmTemplateFromOva,
                params,
                aQuery);
    }

    public void getVmDiskList(AsyncQuery<List<Disk>> aQuery, Guid id) {
        aQuery.converterCallback = source -> {
            ArrayList<Disk> list = new ArrayList<>();
            if (source != null) {
                Iterable listEnumerable = (Iterable) source;
                for (Object o : listEnumerable) {
                    list.add((Disk) o);
                }
            }
            return list;
        };

        Frontend.getInstance().runQuery(QueryType.GetAllDisksByVmId,
                new IdQueryParameters(id).withoutRefresh(),
                aQuery);
    }

    public void getVmListByClusterName(AsyncQuery<List<VM>> aQuery, String clusterName) {
        aQuery.converterCallback = new CastingConverter<>();
        Frontend.getInstance().runQuery(QueryType.Search,
                new SearchParameters("Vms: cluster=" + clusterName, SearchType.VM), //$NON-NLS-1$
                aQuery);
    }

    public void getDiskList(AsyncQuery<List<DiskImage>> aQuery) {
        aQuery.converterCallback = new ListConverter<>();

        SearchParameters searchParams = new SearchParameters("Disks:", SearchType.Disk); //$NON-NLS-1$
        searchParams.setMaxCount(9999);

        Frontend.getInstance().runQuery(QueryType.Search, searchParams, aQuery);
    }

    public void getNextAvailableDiskAliasNameByVMId(AsyncQuery<String> aQuery, Guid vmId) {
        aQuery.converterCallback = new CastingConverter<>();
        Frontend.getInstance().runQuery(QueryType.GetNextAvailableDiskAliasNameByVMId,
                new IdQueryParameters(vmId),
                aQuery);
    }

    public void isPoolNameUnique(AsyncQuery<Boolean> aQuery, String name) {

        aQuery.converterCallback = source -> {
            if (source != null) {
                return !(Boolean) source;
            }

            return false;
        };
        Frontend.getInstance().runQuery(QueryType.IsVmPoolWithSameNameExists,
                new NameQueryParameters(name),
                aQuery);
    }

    public void getVmConfigurationBySnapshot(AsyncQuery<VM> aQuery, Guid snapshotSourceId) {
        aQuery.converterCallback = new CastingConverter<>();
        Frontend.getInstance().runQuery(QueryType.GetVmConfigurationBySnapshot,
                new IdQueryParameters(snapshotSourceId).withoutRefresh(),
                aQuery);
    }

    public void getAllAttachableDisks(AsyncQuery<List<Disk>> aQuery, Guid storagePoolId, Guid vmId) {
        aQuery.converterCallback = new ListConverter<>();
        GetAllAttachableDisksForVmQueryParameters params = new GetAllAttachableDisksForVmQueryParameters(storagePoolId);
        params.setVmId(vmId);
        Frontend.getInstance().runQuery(QueryType.GetAllAttachableDisksForVm, params, aQuery);
    }

    public void getAncestorImagesByImagesIds(AsyncQuery<Map<Guid, DiskImage>> aQuery, List<Guid> imagesIds) {
        aQuery.converterCallback = new CastingConverter<>();
        IdsQueryParameters params = new IdsQueryParameters(imagesIds);
        Frontend.getInstance().runQuery(QueryType.GetAncestorImagesByImagesIds, params, aQuery);
    }

    public void getPermittedStorageDomainsByStoragePoolId(AsyncQuery<List<StorageDomain>> aQuery,
            Guid dataCenterId,
            ActionGroup actionGroup) {
        aQuery.converterCallback = new ListConverter<>();
        GetPermittedStorageDomainsByStoragePoolIdParameters params =
                new GetPermittedStorageDomainsByStoragePoolIdParameters();

        params.setStoragePoolId(dataCenterId);
        params.setActionGroup(actionGroup);

        Frontend.getInstance().runQuery(QueryType.GetPermittedStorageDomainsByStoragePoolId, params, aQuery);
    }

    public void getStorageDomainDefaultWipeAfterDelete(AsyncQuery<Boolean> aQuery, StorageType storageType) {
        aQuery.converterCallback = new CastingConverter<>();
        GetStorageDomainDefaultWipeAfterDeleteParameters params =
                new GetStorageDomainDefaultWipeAfterDeleteParameters(storageType);
        Frontend.getInstance().runQuery(QueryType.GetStorageDomainDefaultWipeAfterDelete, params, aQuery);
    }

    public void getAllDataCenterNetworks(AsyncQuery<List<Network>> aQuery, Guid storagePoolId) {
        aQuery.converterCallback = new ListConverter<>();
        IdQueryParameters params = new IdQueryParameters(storagePoolId);
        Frontend.getInstance().runQuery(QueryType.GetNetworksByDataCenterId, params, aQuery);
    }

    public void getRequiredNetworksByDataCenterId(AsyncQuery<List<Network>> aQuery, Guid storagePoolId) {
        aQuery.converterCallback = new ListConverter<>();
        IdQueryParameters params = new IdQueryParameters(storagePoolId);
        Frontend.getInstance().runQuery(QueryType.GetRequiredNetworksByDataCenterId, params, aQuery);
    }

    public void getManagementNetworkCandidates(AsyncQuery<List<Network>> aQuery, Guid storagePoolId) {
        aQuery.converterCallback = new ListConverter<>();
        IdQueryParameters params = new IdQueryParameters(storagePoolId);
        Frontend.getInstance().runQuery(QueryType.GetManagementNetworkCandidates, params, aQuery);
    }

    public void getStorageConnectionsByDataCenterIdAndStorageType(AsyncQuery<List<StorageServerConnections>> aQuery,
            Guid storagePoolId,
            StorageType storageType) {
        aQuery.converterCallback = new CastingConverter<>();
        GetConnectionsByDataCenterAndStorageTypeParameters params =
                new GetConnectionsByDataCenterAndStorageTypeParameters(storagePoolId, storageType);
        Frontend.getInstance().runQuery(QueryType.GetConnectionsByDataCenterAndStorageType, params, aQuery);
    }

    /**
     * Cache configuration values [raw (not converted) values from vdc_options table].
     */
    private void cacheConfigValues(AsyncQuery<Map<KeyValuePairCompat<ConfigValues, String>, Object>> aQuery) {
        aQuery.converterCallback = returnValue -> {
            if (returnValue != null) {
                cachedConfigValuesPreConvert
                        .putAll((HashMap<KeyValuePairCompat<ConfigValues, String>, Object>) returnValue);
            }
            return cachedConfigValuesPreConvert;
        };
        Frontend.getInstance().runQuery(QueryType.GetConfigurationValues, new QueryParametersBase(), aQuery);
    }

    /**
     * Get configuration value from 'cachedConfigValuesPreConvert' (raw values from vdc_options table).
     */
    public Object getConfigValuePreConverted(ConfigValues configValue, String version) {
        KeyValuePairCompat<ConfigValues, String> key = new KeyValuePairCompat<>(configValue, version);

        return cachedConfigValuesPreConvert.get(key);
    }

    /**
     * Get configuration value from 'cachedConfigValuesPreConvert' (raw values from vdc_options table).
     */
    public Object getConfigValuePreConverted(ConfigValues configValue) {
        KeyValuePairCompat<ConfigValues, String> key =
                new KeyValuePairCompat<>(configValue, getDefaultConfigurationVersion());

        return cachedConfigValuesPreConvert.get(key);
    }

    /**
     * method to get an item from config while caching it (config is not supposed to change during a session)
     *
     * @param aQuery
     *            an async query
     * @param parameters
     *            a converter for the async query
     */
    public <T> void getConfigFromCache(GetConfigurationValueParameters parameters, AsyncQuery<T> aQuery) {
        // cache key
        final KeyValuePairCompat<ConfigValues, String> config_key =
                new KeyValuePairCompat<>(parameters.getConfigValue(), parameters.getVersion());

        T returnValue = null;

        if (cachedConfigValues.containsKey(config_key)) {
            // cache hit
            returnValue = (T) cachedConfigValues.get(config_key);
        } else if (cachedConfigValuesPreConvert.containsKey(config_key)) {
            // cache miss: convert configuration value using query's converter
            // and call asyncCallback's onSuccess
            returnValue = (T) cachedConfigValuesPreConvert.get(config_key);

            // run converter
            if (aQuery.converterCallback != null) {
                Converter<T, T> converter = (Converter<T, T>) aQuery.converterCallback;
                returnValue = converter.convert(returnValue);
            }
            if (returnValue != null) {
                cachedConfigValues.put(config_key, returnValue);
            }
        }
        aQuery.getAsyncCallback().onSuccess(returnValue);
    }

    public ArrayList<QuotaEnforcementTypeEnum> getQuotaEnforcmentTypes() {
        return new ArrayList<>(Arrays.asList(QuotaEnforcementTypeEnum.DISABLED,
                QuotaEnforcementTypeEnum.SOFT_ENFORCEMENT,
                QuotaEnforcementTypeEnum.HARD_ENFORCEMENT));
    }

    public Version multiFirewallSupportSince() {
        return (Version) getConfigValuePreConverted(ConfigValues.MultiFirewallSupportSince);
    }

    private static class TemplateConverter implements Converter<List<VmTemplate>, List<VmTemplate>> {

        @Override
        public List<VmTemplate> convert(List<VmTemplate> source) {
            List<VmTemplate> list = new ArrayList<>();
            if (source != null) {
                VmTemplate blankTemplate = null;
                for (VmTemplate template : source) {
                    if (template.getId().equals(Guid.Empty)) {
                        blankTemplate = template;
                    } else if (template.getStatus() == VmTemplateStatus.OK) {
                        list.add(template);
                    }
                }

                list.sort(new NameableComparator());
                if (blankTemplate != null) {
                    list.add(0, blankTemplate);
                }
            }

            return list;
        }
    }

    public void getAllProvidersByProvidedEntity(AsyncQuery<List<Provider<?>>> query,
            final VdcObjectType providedEntity) {
        query.converterCallback = returnValue -> {
            if (returnValue == null) {
                return new ArrayList<>();
            }
            return ((Collection<Provider<?>>) returnValue).stream()
                    .filter(p -> p.getType().getProvidedTypes().contains(providedEntity))
                    .sorted(new NameableComparator())
                    .collect(Collectors.toList());
        };
        Frontend.getInstance().runQuery(QueryType.GetAllProviders, new GetAllProvidersParameters(), query);
    }

    public void getAllNetworkProviders(AsyncQuery<List<Provider<?>>> query) {
        getAllProvidersByProvidedEntity(query, VdcObjectType.Network);
    }

    public void getAllProvidersByType(AsyncQuery<List<Provider<?>>> aQuery, ProviderType ... providerTypes) {
        aQuery.converterCallback = new ListConverter<>();
        Frontend.getInstance().runQuery(QueryType.GetAllProviders, new GetAllProvidersParameters(providerTypes), aQuery);
    }

    public void getProviderCertificateChain(AsyncQuery<List<CertificateInfo>> aQuery, Provider provider) {
        aQuery.converterCallback = new ListConverter<>();
        Frontend.getInstance().runQuery(QueryType.GetProviderCertificateChain,
                new ProviderQueryParameters(provider),
                aQuery);
    }

    public void fillTagsRecursive(Tags tagToFill, List<Tags> children) {
        ArrayList<Tags> list = new ArrayList<>();

        for (Tags tag : children) {
            // tags child = new tags(tag.description, tag.parent_id, tag.IsReadonly, tag.tag_id, tag.tag_name);
            if (tag.getType() == TagsType.GeneralTag) {
                list.add(tag);
                if (tag.getChildren() != null) {
                    fillTagsRecursive(tag, tag.getChildren());
                }
            }

        }

        tagToFill.setChildren(list);
    }

    public ArrayList<EventNotificationEntity> getEventNotificationTypeList() {
        ArrayList<EventNotificationEntity> ret = new ArrayList<>();
        // TODO: We can translate it here too
        for (EventNotificationEntity entity : EventNotificationEntity.values()) {
            if (entity != EventNotificationEntity.UNKNOWN) {
                ret.add(entity);
            }
        }
        return ret;
    }

    public Map<EventNotificationEntity, Set<AuditLogType>> getAvailableNotificationEvents() {
        return VdcEventNotificationUtils.getNotificationEvents();
    }

    public void getNicTypeList(final int osId, Version version, AsyncQuery<List<VmInterfaceType>> asyncQuery) {
        asyncQuery.converterCallback = returnValue -> {
            ArrayList<String> nics = (ArrayList<String>) returnValue;
            List<VmInterfaceType> interfaceTypes = new ArrayList<>();
            for (String nic : nics) {
                try {
                    interfaceTypes.add(VmInterfaceType.valueOf(nic));
                } catch (IllegalArgumentException e) {
                    // ignore if we can't find the enum value.
                }
            }
            return interfaceTypes;
        };
        Frontend.getInstance().runQuery(QueryType.OsRepository,
                new OsQueryParameters(OsRepositoryVerb.GetNetworkDevices, osId, version),
                asyncQuery);
    }

    public VmInterfaceType getDefaultNicType(Collection<VmInterfaceType> items) {
        if (items == null || items.isEmpty()) {
            return null;
        } else if (items.contains(VmInterfaceType.pv)) {
            return VmInterfaceType.pv;
        } else {
            return items.iterator().next();
        }
    }

    public int getClusterDefaultMemoryOverCommit() {
        return 100;
    }

    public boolean getClusterDefaultCountThreadsAsCores() {
        return false;
    }

    public boolean getClusterDefaultSmtDisabled() {
        return false;
    }

    public ArrayList<VolumeType> getVolumeTypeList() {
        return new ArrayList<>(Arrays.asList(VolumeType.Preallocated, VolumeType.Sparse));
    }

    public ArrayList<VolumeFormat> getVolumeFormats() {
        return new ArrayList<>(Arrays.asList(VolumeFormat.COW, VolumeFormat.RAW));
    }

    public ArrayList<StorageType> getStorageTypeList() {
        return new ArrayList<>(Arrays.asList(StorageType.ISCSI, StorageType.FCP));
    }

    public void getDiskInterfaceList(
            int osId,
            Version clusterVersion,
            ChipsetType chipset,
            AsyncQuery<List<DiskInterface>> asyncQuery) {
        asyncQuery.converterCallback = returnValue -> {
            ArrayList<String> interfaces = (ArrayList<String>) returnValue;
            List<DiskInterface> interfaceTypes = new ArrayList<>();
            for (String diskIfs : interfaces) {
                try {
                    interfaceTypes.add(DiskInterface.valueOf(diskIfs));
                } catch (IllegalArgumentException e) {
                    // ignore if we can't find the enum value.
                }
            }
            return interfaceTypes;
        };
        Frontend.getInstance().runQuery(QueryType.OsRepository,
                new OsQueryParameters(OsRepositoryVerb.GetDiskInterfaces, osId, clusterVersion, chipset),
                asyncQuery);
    }

    public ArrayList<DiskInterface> getDiskInterfaceList() {
        return new ArrayList<>(Arrays.asList(DiskInterface.values()));
    }

    public String getNewNicName(Collection<VmNetworkInterface> existingInterfaces) {
        int maxIfaceNumber = 0;
        if (existingInterfaces != null) {
            for (VmNetworkInterface iface : existingInterfaces) {
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

    public Guid getEntityGuid(Object entity) {
        if (entity instanceof BusinessEntity) {
            // BusinessEntity can have lot of different ID types, but from this context it cannot be determined.
            Object id = ((BusinessEntity<?>) entity).getId();

            // check whether result can be casted to Guid, otherwise continue with explicit rules.
            if (id instanceof Guid) {
                return (Guid) id;
            }
        }

        if (entity instanceof VmPool) {
            return ((VmPool) entity).getVmPoolId();
        } else if (entity instanceof DbUser) {
            return ((DbUser) entity).getId();
        } else if (entity instanceof DbGroup) {
            return ((DbGroup) entity).getId();
        } else {
            return Guid.Empty;
        }
    }

    public boolean isWindowsOsType(Integer osType) {
        // can be null as a consequence of setItems on ListModel
        if (osType == null) {
            return false;
        }

        return windowsOsIds.contains(osType);
    }

    public boolean isLinuxOsType(Integer osId) {
        // can be null as a consequence of setItems on ListModel
        if (osId == null) {
            return false;
        }

        return linuxOsIds.contains(osId);
    }

    public boolean isIgnition(Integer osId) {
        // can be null as a consequence of setItems on ListModel
        if (osId == null) {
            return false;
        }

        return vmInitMap.containsKey(osId) && vmInitMap.get(osId).startsWith("ignition"); //$NON-NLS-1$
    }

    public String getIgnitionVersion(Integer osId) {
        if (isIgnition(osId)) {
            return vmInitMap.get(osId).replace("ignition_", "");//$NON-NLS-1$
        }

        return "";//$NON-NLS-1$
    }

    public void initWindowsOsTypes() {
        Frontend.getInstance().runQuery(QueryType.OsRepository,
                new OsQueryParameters(OsRepositoryVerb.GetWindowsOss),
                new AsyncQuery<QueryReturnValue>(
                        returnValue -> windowsOsIds = returnValue.getReturnValue()));
    }

    public void initLinuxOsTypes() {
        Frontend.getInstance().runQuery(QueryType.OsRepository,
                new OsQueryParameters(OsRepositoryVerb.GetLinuxOss),
                new AsyncQuery<QueryReturnValue>(
                        returnValue -> linuxOsIds = returnValue.getReturnValue()));
    }

    public void initVmInitTypes() {
        Frontend.getInstance().runQuery(QueryType.OsRepository,
                new OsQueryParameters(OsRepositoryVerb.GetVmInitMap),
                new AsyncQuery<QueryReturnValue>(
                        returnValue -> vmInitMap = returnValue.getReturnValue()));
    }

    public void initUniqueOsNames() {
        Frontend.getInstance().runQuery(QueryType.OsRepository,
                new OsQueryParameters(OsRepositoryVerb.GetUniqueOsNames),
                new AsyncQuery<QueryReturnValue>(returnValue -> {
                    uniqueOsNames = returnValue.getReturnValue();
                    // Initialize specific UI dependencies for search
                    SimpleDependencyInjector.getInstance().bind(new OsValueAutoCompleter(uniqueOsNames));
                }));
    }

    public void initOsNames() {
        Frontend.getInstance().runQuery(QueryType.OsRepository,
                new OsQueryParameters(OsRepositoryVerb.GetOsNames),
                new AsyncQuery<QueryReturnValue>(returnValue -> osNames = returnValue.getReturnValue()));
    }

    private void initTimezones() {
        Frontend.getInstance().runQuery(QueryType.GetTimeZones,
                new TimeZoneQueryParameters(TimeZoneVerb.GetGeneralTimezones),
                new AsyncQuery<QueryReturnValue>(returnValue -> generalTimezones = returnValue.getReturnValue()));

        Frontend.getInstance().runQuery(QueryType.GetTimeZones,
                new TimeZoneQueryParameters(TimeZoneVerb.GetWindowsTimezones),
                new AsyncQuery<QueryReturnValue>(returnValue -> windowsTimezones = returnValue.getReturnValue()));
    }

    public Map<String, String> getTimezones(TimeZoneType timeZoneType) {
        switch (timeZoneType) {
            case GENERAL_TIMEZONE:
                return generalTimezones;
            case WINDOWS_TIMEZONE:
            default:
                return windowsTimezones;
        }
    }

    private void initOsDefaultIconIds() {
        Frontend.getInstance().runQuery(QueryType.GetVmIconDefaults,
                new QueryParametersBase(),
                new AsyncQuery<QueryReturnValue>(returnValue -> {
                    final Map<Integer, VmIconIdSizePair> returnMap = returnValue.getReturnValue();
                    if (returnMap.get(DEFAULT_OS_ID) == null) {
                        throw new RuntimeException("Engine did not provide icon IDs of default OS."); //$NON-NLS-1$
                    }
                    osIdToDefaultIconIdMap = Collections.unmodifiableMap(returnMap);
                    initializeLargeToSmallIconMap();
                }));
    }

    private void initializeLargeToSmallIconMap() {
        largeToSmallOsDefaultIconIdMap = new HashMap<>();
        for (VmIconIdSizePair pair : osIdToDefaultIconIdMap.values()) {
            largeToSmallOsDefaultIconIdMap.put(pair.getLarge(), pair.getSmall());
        }
    }

    public void initOsArchitecture() {
        Frontend.getInstance().runQuery(QueryType.OsRepository,
                new OsQueryParameters(OsRepositoryVerb.GetOsArchitectures),
                new AsyncQuery<QueryReturnValue>(returnValue -> osArchitectures = returnValue.getReturnValue()));
    }

    public boolean osNameExists(Integer osId) {
        return osNames.keySet().contains(osId);
    }

    public String getOsName(Integer osId) {
        // can be null as a consequence of setItems on ListModel
        if (osId == null) {
            return "";
        }

        return osNames.get(osId);
    }

    public Guid getOsDefaultIconId(Integer osId, boolean small) {
        if (osId == null) {
            return getDefaultIconId(small);
        }
        final VmIconIdSizePair pair = osIdToDefaultIconIdMap.get(osId);
        if (pair != null) {
            return pair.get(small);
        }
        return getDefaultIconId(small);
    }

    public boolean isCustomIconId(Guid iconId) {
        return !largeToSmallOsDefaultIconIdMap.containsKey(iconId)
                && !largeToSmallOsDefaultIconIdMap.containsValue(iconId);
    }

    public Guid getDefaultIconId(boolean small) {
        final VmIconIdSizePair pair = osIdToDefaultIconIdMap.get(DEFAULT_OS_ID);
        if (pair != null) {
            return pair.get(small);
        }
        throw new RuntimeException("Icon of default operating system not found."); //$NON-NLS-1$
    }

    public Guid getSmallByLargeOsDefaultIconId(Guid largeIconId) {
        return largeToSmallOsDefaultIconIdMap.get(largeIconId);
    }

    public boolean hasSpiceSupport(int osId, Version version) {
        for (Pair<GraphicsType, DisplayType> graphicsDisplayPair : getGraphicsAndDisplays(osId, version)) {
            if (graphicsDisplayPair.getFirst() == GraphicsType.SPICE) {
                return true;
            }
        }
        return false;
    }

    public List<Pair<GraphicsType, DisplayType>> getGraphicsAndDisplays(int osId, Version version) {
        return graphicsAndDisplays.get(osId).get(version);
    }

    private void initDisplayTypes() {
        Frontend.getInstance().runQuery(QueryType.OsRepository,
                new OsQueryParameters(OsRepositoryVerb.GetDisplayTypes),
                new AsyncQuery<QueryReturnValue>(returnValue -> graphicsAndDisplays = returnValue.getReturnValue()));
    }

    private void initImageioProxyUri() {
        Frontend.getInstance().runQuery(QueryType.GetImageioProxyUri,
                new QueryParametersBase(),
                new AsyncQuery<QueryReturnValue>(returnValue -> imageioProxyUri = returnValue.getReturnValue()));
    }

    public String getImageioProxyUri() {
        return imageioProxyUri;
    }

    public List<Integer> getOsIds(ArchitectureType architectureType) {

        List<Integer> osIds = new ArrayList<>();

        for (Entry<Integer, ArchitectureType> entry : osArchitectures.entrySet()) {
            Integer osId = entry.getKey();

            if (entry.getValue() == architectureType) {
                osIds.add(osId);
            }
        }

        osIds.sort(Comparator.comparing(o -> osNames.get(o)));

        return osIds;
    }

    public List<Integer> getSupportedOsIds(ArchitectureType architectureType, Version version) {
        List<Integer> osIds = getOsIds(architectureType);
        return osIds.stream()
                .filter(osId -> !unsupportedOsIds.contains(osId))
                .filter(osId -> !isTpmRequiredForOs(osId)
                        || isTpmDeviceSupported(architectureType, version))
                .collect(Collectors.toList());
    }

    public void getVmWatchdogTypes(int osId,
            Version version,
            AsyncQuery<QueryReturnValue> asyncQuery) {
        Frontend.getInstance().runQuery(QueryType.OsRepository, new OsQueryParameters(
                OsRepositoryVerb.GetVmWatchdogTypes, osId, version), asyncQuery);
    }

    public ArrayList<Map.Entry<String, EntityModel<String>>> getBondingOptionListDependingOnNetwork(
            RefObject<Map.Entry<String, EntityModel<String>>> defaultItem, boolean hasVmNetworkAttached) {
        ArrayList<Map.Entry<String, EntityModel<String>>> list = new ArrayList<>();

        for (BondMode mode : BondMode.values()) {
            if (!mode.isBondModeValidForVmNetwork() && hasVmNetworkAttached) {
                continue;
            }
            KeyValuePairCompat<String, EntityModel<String>> bondOption = getBondOption(mode);
            list.add(bondOption);
            if (mode.equals(BondMode.BOND4)) {
                defaultItem.argvalue = bondOption;
            }
        }

        EntityModel<String> entityModel = new EntityModel<>();
        entityModel.setEntity(""); //$NON-NLS-1$
        list.add(new KeyValuePairCompat<>(SetupNetworksBondModel.CUSTOM_BONDING_MODE, entityModel));
        return list;
    }

    private KeyValuePairCompat<String, EntityModel<String>> getBondOption(BondMode mode) {
        EntityModel<String> entityModel = new EntityModel<>();
        entityModel.setEntity(mode.getDescription());
        return new KeyValuePairCompat<>(mode.getConfigurationValue(), entityModel);
    }

    public int getMaxVmPriority() {
        return (Integer) getConfigValuePreConverted(ConfigValues.VmPriorityMaxValue,
                getDefaultConfigurationVersion());
    }

    public int roundPriority(int priority) {
        int max = getMaxVmPriority();
        int medium = max / 2;

        int[] levels = new int[] { 1, medium, max };

        for (int i = 0; i < levels.length; i++) {
            int lengthToLess = levels[i] - priority;
            int lengthToMore = levels[i + 1] - priority;

            if (lengthToMore < 0) {
                continue;
            }

            return Math.abs(lengthToLess) < lengthToMore ? levels[i] : levels[i + 1];
        }

        return 0;
    }

    public void getVmGuestAgentInterfacesByVmId(AsyncQuery<List<VmGuestAgentInterface>> aQuery, Guid vmId) {
        aQuery.converterCallback = new ListConverter<>();
        Frontend.getInstance().runQuery(QueryType.GetVmGuestAgentInterfacesByVmId,
                new IdQueryParameters(vmId).withoutRefresh(),
                aQuery);
    }

    public void getVnicProfilesByNetworkId(AsyncQuery<List<VnicProfileView>> aQuery, Guid networkId) {
        aQuery.converterCallback = new ListConverter<>();
        Frontend.getInstance().runQuery(QueryType.GetVnicProfilesByNetworkId, new IdQueryParameters(networkId), aQuery);
    }

    public void getAllVnicProfiles(AsyncQuery<List<VnicProfileView>> aQuery) {
        aQuery.converterCallback = new ListConverter<>();
        Frontend.getInstance().runQuery(QueryType.GetAllVnicProfiles, new IdQueryParameters(), aQuery);
    }

    public void getVnicProfilesByDcId(AsyncQuery<List<VnicProfileView>> aQuery, Guid dcId) {
        // do not replace a converter = just add if none provided
        if (aQuery.converterCallback == null) {
            aQuery.converterCallback = new ListConverter<>();
        }
        Frontend.getInstance().runQuery(QueryType.GetVnicProfilesByDataCenterId, new IdQueryParameters(dcId), aQuery);
    }

    public void getVnicProfilesByClusterId(AsyncQuery<List<VnicProfileView>> aQuery, Guid clusterId) {
        // do not replace a converter = just add if none provided
        if (aQuery.converterCallback == null) {
            aQuery.converterCallback = new ListConverter<>();
        }
        Frontend.getInstance().runQuery(QueryType.GetVnicProfilesByClusterId,
                new IdQueryParameters(clusterId),
                aQuery);
    }

    public void getVnicInteraceNetworkFilterParameters(AsyncQuery<List<NetworkFilterParameterModel>> aQuery, Guid interfaceId) {
        if (aQuery.converterCallback == null) {
            aQuery.converterCallback = new ListConverter<>();
        }
        Frontend.getInstance().runQuery(QueryType.GetVmInterfaceFilterParametersByVmInterfaceId,
                new IdQueryParameters(interfaceId),
                aQuery);
    }

    public void getNumberOfVmsInCluster(AsyncQuery aQuery, Guid clusterId) {
        Frontend.getInstance().runQuery(QueryType.GetNumberOfVmsInClusterByClusterId,
                new IdQueryParameters(clusterId),
                aQuery);
    }

    public Integer getMaxIoThreadsPerVm() {
        return (Integer) getConfigValuePreConverted(ConfigValues.MaxIoThreadsPerVm);
    }

    public ArrayList<Cluster> getClusterByServiceList(List<Cluster> list,
            boolean supportsVirtService,
            boolean supportsGlusterService) {
        final ArrayList<Cluster> filteredList = new ArrayList<>();
        for (Cluster cluster : list) {
            if ((supportsVirtService && cluster.supportsVirtService())
                    || (supportsGlusterService && cluster.supportsGlusterService())) {
                filteredList.add(cluster);
            }
        }

        // sort by cluster name
        filteredList.sort(new NameableComparator());
        return filteredList;
    }

    public String priorityToString(int value) {
        int roundedPriority = roundPriority(value);

        if (roundedPriority == 1) {
            return ConstantsManager.getInstance().getConstants().vmLowPriority();
        } else if (roundedPriority == getMaxVmPriority() / 2) {
            return ConstantsManager.getInstance().getConstants().vmMediumPriority();
        } else if (roundedPriority == getMaxVmPriority()) {
            return ConstantsManager.getInstance().getConstants().vmHighPriority();
        } else {
            return ConstantsManager.getInstance().getConstants().vmUnknownPriority();
        }
    }

    public void getExternalNetworksByProviderId(AsyncQuery<QueryReturnValue> aQuery, Guid providerId) {
        Frontend.getInstance().runQuery(QueryType.GetAllExternalNetworksOnProvider,
                new IdQueryParameters(providerId),
                aQuery);
    }

    public Integer getMaxVmNameLength() {
        Integer maxVmNameLength = (Integer) getConfigValuePreConverted(ConfigValues.MaxVmNameLength);
        if (maxVmNameLength == null) {
            return 64;
        }
        return maxVmNameLength;
    }

    public Integer getMaxVmNameLengthSysprep() {
        Integer maxVmNameLengthSysprep = (Integer) getConfigValuePreConverted(ConfigValues.MaxVmNameLengthSysprep);
        if (maxVmNameLengthSysprep == null) {
            return 64;
        }
        return maxVmNameLengthSysprep;
    }

    public SerialNumberPolicy getSerialNumberPolicy() {
        return (SerialNumberPolicy) getConfigValuePreConverted(ConfigValues.DefaultSerialNumberPolicy,
                getDefaultConfigurationVersion());
    }

    public String getCustomSerialNumber() {
        return (String) getConfigValuePreConverted(ConfigValues.DefaultCustomSerialNumber,
                getDefaultConfigurationVersion());
    }

    public boolean getAutoConverge() {
        return (Boolean) getConfigValuePreConverted(ConfigValues.DefaultAutoConvergence,
                getDefaultConfigurationVersion());
    }

    public boolean getMigrateCompressed() {
        return (Boolean) getConfigValuePreConverted(ConfigValues.DefaultMigrationCompression,
                getDefaultConfigurationVersion());
    }

    public boolean getMigrateEncrypted() {
        return (Boolean) getConfigValuePreConverted(ConfigValues.DefaultMigrationEncryption,
                getDefaultConfigurationVersion());
    }

    public Integer getMigrationDowntime() {
        return (Integer) getConfigValuePreConverted(ConfigValues.DefaultMaximumMigrationDowntime,
                getDefaultConfigurationVersion());
    }

    public int getOptimizeSchedulerForSpeedPendingRequests() {
        return (Integer) getConfigValuePreConverted(ConfigValues.SpeedOptimizationSchedulingThreshold,
                getDefaultConfigurationVersion());
    }

    public boolean getScheudulingAllowOverbookingSupported() {
        return (Boolean) getConfigValuePreConverted(ConfigValues.SchedulerAllowOverBooking,
                getDefaultConfigurationVersion());
    }

    public int getSchedulerAllowOverbookingPendingRequestsThreshold() {
        return (Integer) getConfigValuePreConverted(ConfigValues.SchedulerOverBookingThreshold,
                getDefaultConfigurationVersion());
    }

    public boolean getIsPortIsolationSupported(Version version) {
        return (Boolean) getConfigValuePreConverted(ConfigValues.IsPortIsolationSupported, version.getValue());
    }

    public Integer getDefaultOs(ArchitectureType architectureType) {
        return defaultOSes.get(architectureType);
    }

    public boolean isRebootCommandExecutionAllowed(List<VmWithStatusForExclusiveLock> vms) {
        if (vms.isEmpty()
                || !ActionUtils.canExecutePartially(vms, VmWithStatusForExclusiveLock.class, ActionType.RebootVm)) {
            return false;
        }

        for (VM vm : vms) {
            boolean guestAgentPresent = !StringHelper.isNullOrEmpty(vm.getIp());
            boolean acpiEnabled = Boolean.TRUE.equals(vm.getAcpiEnable());
            if (!(guestAgentPresent || acpiEnabled)) {
                return false;
            }
        }
        return true;
    }

    public List<IStorageModel> getDataStorageModels() {
        ArrayList<IStorageModel> models = new ArrayList<>();
        models.addAll(getFileDataStorageModels());
        models.addAll(getBlockDataStorageModels());
        return models;
    }

    public List<IStorageModel> getFileDataStorageModels() {
        ArrayList<IStorageModel> models = new ArrayList<>();

        NfsStorageModel nfsDataModel = new NfsStorageModel();
        models.add(nfsDataModel);

        PosixStorageModel posixDataModel = new PosixStorageModel();
        models.add(posixDataModel);

        GlusterStorageModel GlusterDataModel = new GlusterStorageModel();
        models.add(GlusterDataModel);

        LocalStorageModel localDataModel = new LocalStorageModel();
        models.add(localDataModel);

        addTypeToStorageModels(StorageDomainType.Data, models);

        return models;
    }

    public List<IStorageModel> getBlockDataStorageModels() {
        ArrayList<IStorageModel> models = new ArrayList<>();

        IscsiStorageModel iscsiDataModel = new IscsiStorageModel();
        iscsiDataModel.setIsGroupedByTarget(true);
        models.add(iscsiDataModel);

        FcpStorageModel fcpDataModel = new FcpStorageModel();
        models.add(fcpDataModel);

        addTypeToStorageModels(StorageDomainType.Data, models);

        return models;
    }

    public List<IStorageModel> getImportBlockDataStorageModels() {
        ArrayList<IStorageModel> models = new ArrayList<>();

        ImportIscsiStorageModel iscsiDataModel = new ImportIscsiStorageModel();
        models.add(iscsiDataModel);

        ImportFcpStorageModel fcpDataModel = new ImportFcpStorageModel();
        models.add(fcpDataModel);

        addTypeToStorageModels(StorageDomainType.Data, models);

        return models;
    }

    public List<IStorageModel> getIsoStorageModels() {
        ArrayList<IStorageModel> models = new ArrayList<>();

        NfsStorageModel nfsIsoModel = new NfsStorageModel();
        models.add(nfsIsoModel);

        PosixStorageModel posixIsoModel = new PosixStorageModel();
        models.add(posixIsoModel);

        GlusterStorageModel glusterStorageModel = new GlusterStorageModel();
        models.add(glusterStorageModel);

        LocalStorageModel localIsoModel = new LocalStorageModel();
        models.add(localIsoModel);

        addTypeToStorageModels(StorageDomainType.ISO, models);

        return models;
    }

    public List<IStorageModel> getExportStorageModels() {
        ArrayList<IStorageModel> models = new ArrayList<>();

        NfsStorageModel nfsExportModel = new NfsStorageModel();
        models.add(nfsExportModel);

        PosixStorageModel posixExportModel = new PosixStorageModel();
        models.add(posixExportModel);

        GlusterStorageModel glusterExportModel = new GlusterStorageModel();
        models.add(glusterExportModel);

        LocalStorageModel localExportStorageModel = new LocalStorageModel();
        models.add(localExportStorageModel);

        addTypeToStorageModels(StorageDomainType.ImportExport, models);

        return models;
    }

    public List<IStorageModel> getManagedBlockStorageModels() {
        ArrayList<IStorageModel> models = new ArrayList<>();

        ManagedBlockStorageModel managedBlockStorageModel = new ManagedBlockStorageModel();
        models.add(managedBlockStorageModel);

        addTypeToStorageModels(StorageDomainType.ManagedBlockStorage, models);

        return models;
    }

    private void addTypeToStorageModels(StorageDomainType storageDomainType, List<IStorageModel> models) {
        for (IStorageModel model : models) {
            model.setRole(storageDomainType);
        }
    }

    private static void convertAAAProfilesResult(AsyncQuery<List<String>> aQuery, final boolean passwordBasedOnly) {
        aQuery.converterCallback = source -> {
            List<String> results = new ArrayList<>();
            for (ProfileEntry profileEntry : (Collection<ProfileEntry>) source) {
                if (!passwordBasedOnly || profileEntry.getSupportsPasswordAuthenication()) {
                    results.add(profileEntry.getProfile());
                }
            }
            return results;
        };
    }

    public void getHostNumaTopologyByHostId(AsyncQuery<List<VdsNumaNode>> asyncQuery, Guid hostId) {
        asyncQuery.converterCallback = new ListConverter<>();
        Frontend.getInstance().runQuery(QueryType.GetVdsNumaNodesByVdsId,
                new IdQueryParameters(hostId),
                asyncQuery);
    }

    public void getVMsWithVNumaNodesByClusterId(AsyncQuery<List<VM>> asyncQuery, Guid clusterId) {
        asyncQuery.converterCallback = new ListConverter<>();
        Frontend.getInstance().runQuery(QueryType.GetAllVmsWithNumaByClusterId,
                new IdQueryParameters(clusterId),
                asyncQuery);
    }

    public ArrayList<NumaTuneMode> getNumaTuneModeList() {
        return new ArrayList<>(Arrays.asList(
                NumaTuneMode.INTERLEAVE,
                NumaTuneMode.PREFERRED,
                NumaTuneMode.STRICT
        ));
    }

    public void getEmulatedMachinesByClusterID(AsyncQuery<Set<String>> aQuery, Guid clusterId) {
        aQuery.converterCallback = source -> {
            if (source != null) {
                List<VDS> vdsList = (List<VDS>) source;
                Set<String> emulatedMachineList = new HashSet<>();
                for (VDS host : vdsList) {
                    String hostSupportedMachines = host.getSupportedEmulatedMachines();
                    if (!StringHelper.isNullOrEmpty(hostSupportedMachines)) {
                        emulatedMachineList.addAll(Arrays.asList(hostSupportedMachines.split(","))); //$NON-NLS-1$
                    }
                }
                return emulatedMachineList;
            }

            return null;
        };

        Frontend.getInstance().runQuery(QueryType.GetHostsByClusterId, new IdQueryParameters(clusterId), aQuery);
    }

    public void getSupportedCpuList(AsyncQuery<List<ServerCpu>> aQuery, String cpuName, Version version) {
        aQuery.converterCallback = new CastingConverter<>();

        Frontend.getInstance().runQuery(QueryType.GetSupportedCpuList,
                new GetSupportedCpuListParameters(cpuName, version),
                aQuery);

    }

    public void getStorageDevices(AsyncQuery<List<StorageDevice>> aQuery, Guid hostId) {
        aQuery.converterCallback = new CastingConverter<>();
        Frontend.getInstance().runQuery(QueryType.GetGlusterStorageDevices,
                new IdQueryParameters(hostId),
                aQuery);
    }

    public void getClusterEditWarnings(AsyncQuery<ClusterEditWarnings> aQuery, Cluster cluster) {
        aQuery.converterCallback = new CastingConverter<>();
        Frontend.getInstance().runQuery(QueryType.GetClusterEditWarnings, new ClusterEditParameters(cluster), aQuery);
    }

    private static class DefaultConverter<T> implements Converter<T, T> {
        @Override
        public T convert(T source) {
            return source;
        }
    }

    private static class CastingConverter<T extends S, S> implements Converter<T, S> {
        @Override
        public T convert(S source) {
            return (T) source;
        }
    }

    private static class DefaultValueConverter<T extends S, S> extends CastingConverter<T, S> {

        private final T defaultValue;

        public DefaultValueConverter(T defaultValue) {
            this.defaultValue = defaultValue;
        }

        @Override
        public T convert(S returnValue) {
            T value = super.convert(returnValue);
            return value != null ? value : defaultValue;
        }
    }

    private static class StringConverter extends DefaultValueConverter<String, String> {
        public StringConverter() {
            super("");
        }
    }

    static class ListConverter<T> implements Converter<List<T>, List<T>> {
        @Override
        public List<T> convert(List<T> source) {
            return source != null ? source : new ArrayList<>();
        }
    }

    private static class MapConverter<K, V> implements Converter<Map<K, V>, Map<K, V>> {
        @Override
        public Map<K, V> convert(Map<K, V> source) {
            return source != null ? source : new HashMap<>();
        }
    }

    private static class SetConverter<T> implements Converter<Set<T>, Set<T>> {
        @Override
        public Set<T> convert(Set<T> source) {
            return source != null ? source : new HashSet<>();
        }
    }

    private static class SortListConverter<T> extends ListConverter<T> {
        private final Comparator<? super T> comparator;

        public SortListConverter() {
            this(null);
        }

        public SortListConverter(Comparator<? super T> comparator) {
            this.comparator = comparator;
        }

        @Override
        public List<T> convert(List<T> source) {
            List<T> list = super.convert(source);
            list.sort(comparator);
            return list;
        }
    }

    private static class SortListByNameConverter<T extends Nameable> extends SortListConverter<T> {
        public SortListByNameConverter() {
            super(new NameableComparator());
        }
    }

    private static class IsNonEmptyCollectionConverter<T> implements Converter<Boolean, Collection<T>> {
        @Override
        public Boolean convert(Collection<T> source) {
            if (source != null) {
                return !source.isEmpty();
            }

            return false;
        }
    }

    private static class GetFirstConverter<T> implements Converter<T, Iterable<T>> {
        @Override
        public T convert(Iterable<T> source) {
            for (T t : source) {
                return t;
            }
            return null;
        }
    }

    public void getUnusedBricksFromServer(AsyncQuery<List<StorageDevice>> asyncQuery, Guid hostId) {
        asyncQuery.converterCallback = new CastingConverter<>();
        IdQueryParameters parameters = new IdQueryParameters(hostId);
        Frontend.getInstance().runQuery(QueryType.GetUnusedGlusterBricks, parameters, asyncQuery);
    }

    public void getClusterFeaturesByVersionAndCategory(AsyncQuery<Set<AdditionalFeature>> aQuery,
            Version version,
            ApplicationMode category) {
        aQuery.converterCallback = new SetConverter<>();
        Frontend.getInstance().runQuery(QueryType.GetClusterFeaturesByVersionAndCategory,
                new GetClusterFeaturesByVersionAndCategoryParameters(version, category),
                aQuery);
    }

    public void getClusterFeaturesByClusterId(AsyncQuery<Set<SupportedAdditionalClusterFeature>> aQuery,
            Guid clusterId) {
        aQuery.converterCallback = new SetConverter<>();
        Frontend.getInstance().runQuery(QueryType.GetClusterFeaturesByClusterId,
                new IdQueryParameters(clusterId),
                aQuery);
    }

    public void getVmTemplatesByBaseTemplateId(AsyncQuery<List<VmTemplate>> asyncQuery, Guid baseTemplate) {
        asyncQuery.converterCallback = new ListConverter<>();
        Frontend.getInstance().runQuery(QueryType.GetVmTemplatesByBaseTemplateId,
                new GetVmTemplateParameters(baseTemplate),
                asyncQuery);
    }

    public ArrayList<LibvirtSecretUsageType> getLibvirtSecretUsageTypeList() {
        return new ArrayList<>(Arrays.asList(LibvirtSecretUsageType.values()));
    }

    public int getUploadImageChunkSizeKB() {
        return (Integer) getConfigValuePreConverted(ConfigValues.UploadImageChunkSizeKB);
    }

    public int getUploadImageXhrTimeoutInSeconds() {
        return (Integer) getConfigValuePreConverted(ConfigValues.UploadImageXhrTimeoutInSeconds);
    }

    public int getUploadImageXhrRetryIntervalInSeconds() {
        return (Integer) getConfigValuePreConverted(ConfigValues.UploadImageXhrRetryIntervalInSeconds);
    }

    public int getUploadImageXhrMaxRetries() {
        return (Integer) getConfigValuePreConverted(ConfigValues.UploadImageXhrMaxRetries);
    }

    private static final class QuotaConverter implements Converter<List<Quota>, List<Quota>> {
        private final Guid topId;

        public QuotaConverter(Guid topId) {
            this.topId = topId;
        }

        @Override
        public List<Quota> convert(List<Quota> quotaList) {
            if (quotaList != null && !quotaList.isEmpty()) {
                Comparator<Quota> comparator =
                        (topId == null) ? QuotaComparator.NAME : QuotaComparator.withTopId(topId, QuotaComparator.NAME);

                quotaList.sort(comparator);
            }
            return quotaList;
        }
    }

    public void getAllRelevantQuotasForStorageSorted(AsyncQuery<List<Quota>> asyncQuery,
            Guid storageId,
            Guid topQuotaId) {
        asyncQuery.converterCallback = new QuotaConverter(topQuotaId);
        Frontend.getInstance().runQuery(QueryType.GetAllRelevantQuotasForStorage,
                new IdQueryParameters(storageId),
                asyncQuery);
    }

    public void getAllRelevantQuotasForClusterSorted(AsyncQuery<List<Quota>> asyncQuery,
            Guid clusterId,
            Guid topQuotaId) {
        asyncQuery.converterCallback = new QuotaConverter(topQuotaId);
        Frontend.getInstance().runQuery(QueryType.GetAllRelevantQuotasForCluster,
                new IdQueryParameters(clusterId),
                asyncQuery);
    }

    public boolean isManagedBlockDomainSupported(Version dataCenterVersion) {
        return (Boolean) getConfigValuePreConverted(
                ConfigValues.ManagedBlockDomainSupported, dataCenterVersion.getValue());
    }

    public Boolean isCopyPreallocatedFileBasedDiskSupported(Version dataCenterVersion) {
        return (Boolean) getConfigValuePreConverted(
                ConfigValues.CopyPreallocatedFileBasedDiskSupported, dataCenterVersion.getValue());
    }

    public void getGlusterVolumesForStorageDomain(AsyncQuery<List<GlusterVolumeEntity>> aQuery) {
        aQuery.converterCallback = new ListConverter<>();
        Frontend.getInstance().runQuery(QueryType.GetAllGlusterVolumesForStorageDomain,
                new QueryParametersBase(),
                aQuery);
    }

    public void validateVmMacs(AsyncQuery<Map<Guid, List<List<String>>>> asyncQuery, Map<Guid, List<VM>> vmsByCluster) {
        asyncQuery.converterCallback = new CastingConverter<>();
        Frontend.getInstance().runQuery(QueryType.ValidateVmMacs,
                new ValidateVmMacsParameters(vmsByCluster),
                asyncQuery);
    }

    public void getGlusterGeoRepSessionsForStorageDomain(AsyncQuery<List<GlusterGeoRepSession>> aQuery,
            Guid storageDomainId) {
        aQuery.converterCallback = new ListConverter<>();
        Frontend.getInstance().runQuery(QueryType.GetGeoRepSessionsForStorageDomain,
                new IdQueryParameters(storageDomainId),
                aQuery);
    }

    public void getGlusterVolumeGeoRepSessionById(AsyncQuery<GlusterGeoRepSession> aQuery, Guid geoRepSessionId) {
        aQuery.converterCallback = new CastingConverter<>();
        Frontend.getInstance().runQuery(QueryType.GetGlusterVolumeGeoRepSessionById,
                new IdQueryParameters(geoRepSessionId),
                aQuery);
    }

    public void getAllFenceAgentsByHostId(AsyncQuery<List<FenceAgent>> aQuery, Guid hostId) {
        aQuery.converterCallback = new ListConverter<>();
        Frontend.getInstance().runQuery(QueryType.GetFenceAgentsByVdsId, new IdQueryParameters(hostId), aQuery);
    }

    public ArrayList<BiosType> getBiosTypeList() {
        return new ArrayList<>(Arrays.asList(BiosType.values()));
    }

    public void updateVDSDefaultRouteRole(List<VDS> vdsList, Runnable callback) {
        if (vdsList != null && !vdsList.isEmpty()) {
            List<QueryType> types = new ArrayList<>();
            List<QueryParametersBase> ids = new ArrayList<>();
            vdsList.forEach(vds -> {
                types.add(QueryType.IsDefaultRouteRoleNetworkAttachedToHost);
                ids.add(new IsDefaultRouteRoleNetworkAttachedToHostQueryParameters(vds.getClusterId(), vds.getId()));
            });
            Frontend.getInstance().runMultipleQueries(types, ids, result -> {
                List<QueryReturnValue> values = result.getReturnValues();
                for (int i = 0; i < vdsList.size(); i++) {
                    QueryReturnValue queryReturnValue = values.get(i);
                    if (queryReturnValue.getReturnValue() != null) {
                        vdsList.get(i).setIsDefaultRouteRoleNetworkAttached(queryReturnValue.getReturnValue());
                    }
                }
                callback.run();
            });
        } else {
            callback.run();
        }
    }

    public void updateVDSDefaultRouteRole(Collection<PairQueryable<VdsNetworkInterface, VDS>> pairCollection,
            Runnable callback) {
        if (pairCollection != null) {
            List<VDS> vdsList = pairCollection.stream().map(Pair::getSecond).collect(Collectors.toList());
            updateVDSDefaultRouteRole(vdsList, callback);
        } else {
            callback.run();
        }
    }

    public void getCustomBondNameSupported(AsyncQuery<Boolean> aQuery, Version version) {
        aQuery.converterCallback = new DefaultValueConverter<>(Boolean.TRUE);
        getConfigFromCache(
                new GetConfigurationValueParameters(ConfigValues.CustomBondNameSupported,
                        version.getValue()),
                aQuery);
    }

    public boolean isVgpuPlacementSupported(Version version) {
        return (Boolean) getConfigValuePreConverted(ConfigValues.VgpuPlacementSupported, version.getValue());
    }

    public Boolean isTpmAllowedForOs(Integer osId) {
        return osId != null && tpmSupportForOsMap.get(osId) != TpmSupport.UNSUPPORTED;
    }

    private void initTpmSupportForOsMap() {
        Frontend.getInstance().runQuery(QueryType.OsRepository,
                new OsQueryParameters(OsRepositoryVerb.GetTpmSupportMap),
                new AsyncQuery<QueryReturnValue>(result -> tpmSupportForOsMap = result.getReturnValue()));
    }

    public Boolean isTpmRequiredForOs(Integer osId) {
        return osId != null && tpmSupportForOsMap.get(osId) == TpmSupport.REQUIRED;
    }

    private void initUnsupportedOsIds() {
        Frontend.getInstance()
                .runQuery(QueryType.OsRepository,
                        new OsQueryParameters(OsRepositoryVerb.GetUnsupportedOsIds),
                        new AsyncQuery<QueryReturnValue>(
                                returnValue -> unsupportedOsIds = returnValue.getReturnValue()));
    }

    public static void getMinMemoryForOs(AsyncQuery<Integer> aQuery, Integer osId, Version version) {
        aQuery.converterCallback = new DefaultValueConverter<>(1024);
        OsQueryParameters params = new OsQueryParameters(OsRepositoryVerb.GetMinOsRam, osId, version);

        Frontend.getInstance().runQuery(QueryType.OsRepository, params, aQuery);
    }

    public static void getMinCpus(AsyncQuery<Integer> aQuery, Integer osId) {
        aQuery.converterCallback = new DefaultValueConverter<>(1);
        OsQueryParameters params = new OsQueryParameters(OsRepositoryVerb.GetMinCpus, osId, null);

        Frontend.getInstance().runQuery(QueryType.OsRepository, params, aQuery);
    }

}
