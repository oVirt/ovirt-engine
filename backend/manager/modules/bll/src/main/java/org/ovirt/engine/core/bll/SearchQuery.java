package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.api.extensions.ExtMap;
import org.ovirt.engine.core.aaa.AuthenticationProfileRepository;
import org.ovirt.engine.core.aaa.DirectoryGroup;
import org.ovirt.engine.core.aaa.DirectoryUser;
import org.ovirt.engine.core.aaa.QueryData;
import org.ovirt.engine.core.aaa.SsoOAuthServiceUtils;
import org.ovirt.engine.core.bll.aaa.DirectoryUtils;
import org.ovirt.engine.core.bll.aaa.SessionDataContainer;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.bll.quota.QuotaManager;
import org.ovirt.engine.core.bll.storage.pool.DcSingleMacPoolFinder;
import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.EngineSession;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.Queryable;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.ServerCpu;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.UserSession;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmPool;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.aaa.DbGroup;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.businessentities.network.NetworkView;
import org.ovirt.engine.core.common.businessentities.network.VnicProfileView;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.ImageTransfer;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.SearchEngineIllegalCharacterException;
import org.ovirt.engine.core.common.errors.SqlInjectionException;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryParametersBase;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.common.queries.SearchParameters;
import org.ovirt.engine.core.compat.DateTime;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.TimeSpan;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dao.AuditLogDao;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.DbGroupDao;
import org.ovirt.engine.core.dao.DbUserDao;
import org.ovirt.engine.core.dao.DiskDao;
import org.ovirt.engine.core.dao.EngineSessionDao;
import org.ovirt.engine.core.dao.ImageTransferDao;
import org.ovirt.engine.core.dao.JobDao;
import org.ovirt.engine.core.dao.QuotaDao;
import org.ovirt.engine.core.dao.SearchDao;
import org.ovirt.engine.core.dao.StorageDomainDao;
import org.ovirt.engine.core.dao.StoragePoolDao;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.VmPoolDao;
import org.ovirt.engine.core.dao.VmTemplateDao;
import org.ovirt.engine.core.dao.gluster.GlusterVolumeDao;
import org.ovirt.engine.core.dao.network.NetworkViewDao;
import org.ovirt.engine.core.dao.network.VmNetworkInterfaceDao;
import org.ovirt.engine.core.dao.network.VnicProfileViewDao;
import org.ovirt.engine.core.dao.provider.ProviderDao;
import org.ovirt.engine.core.searchbackend.ISyntaxChecker;
import org.ovirt.engine.core.searchbackend.SearchObjects;
import org.ovirt.engine.core.searchbackend.SyntaxCheckerFactory;
import org.ovirt.engine.core.searchbackend.SyntaxContainer;
import org.ovirt.engine.core.searchbackend.SyntaxError;
import org.ovirt.engine.core.utils.lock.EngineLock;
import org.ovirt.engine.core.utils.lock.LockManager;

public class SearchQuery<P extends SearchParameters> extends QueriesCommandBase<P> {
    private static final Map<String, QueryData> queriesCache = new HashMap<>();
    public static final String LDAP = "LDAP";

    @Inject
    private QuotaManager quotaManager;

    @Inject
    private SessionDataContainer sessionDataContainer;

    @Inject
    protected CpuFlagsManagerHandler cpuFlagsManagerHandler;

    @Inject
    private VmHandler vmHandler;

    @Inject
    private VmDao vmDao;

    @Inject
    private VdsDao vdsDao;

    @Inject
    private DbUserDao dbUserDao;

    @Inject
    private DbGroupDao dbGroupDao;

    @Inject
    private VmTemplateDao vmTemplateDao;

    @Inject
    private AuditLogDao auditLogDao;

    @Inject
    private VmPoolDao vmPoolDao;

    @Inject
    private ClusterDao clusterDao;

    @Inject
    private StoragePoolDao storagePoolDao;

    @Inject
    private StorageDomainDao storageDomainDao;

    @Inject
    private QuotaDao quotaDao;

    @Inject
    private DiskDao diskDao;

    @Inject
    private GlusterVolumeDao glusterVolumeDao;

    @Inject
    private NetworkViewDao networkViewDao;

    @Inject
    private VnicProfileViewDao vnicProfileViewDao;

    @Inject
    private ProviderDao providerDao;

    @Inject
    private EngineSessionDao engineSessionDao;

    @Inject
    private ImageTransferDao imageTransferDao;

    @Inject
    private JobDao jobDao;

    @Inject
    private DirectoryUtils directoryUtils;

    @Inject
    private DcSingleMacPoolFinder dcSingleMacPoolFinder;

    @Inject
    private LockManager lockManager;
    @Inject
    private HostLocking hostLocking;
    @Inject
    private VmNetworkInterfaceDao vmNetworkInterfaceDao;

    public SearchQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        List<? extends Queryable> returnValue = new ArrayList<>();
        switch (getParameters().getSearchTypeValue()) {
        case VM:
            returnValue = searchVmsFromDb();
            break;
        case DirectoryGroup:
            returnValue = searchDirectoryGroups();
            break;
        case DirectoryUser:
            returnValue = searchDirectoryUsers();
            break;
        case AuditLog:
            returnValue = searchAuditLogEvents();
            break;
        case DBUser:
            returnValue = searchDbUsers();
            break;
        case DBGroup:
            returnValue = searchDbGroups();
            break;
        case VDS:
            returnValue = searchVDSsByDb();
            break;
        case VmTemplate:
            returnValue = searchVmTemplates();
            break;
        case VmPools:
            returnValue = searchVmPools();
            break;
        case Cluster:
            returnValue = searchClusters();
            break;
        case StoragePool:
            returnValue = searchStoragePool();
            break;
        case StorageDomain:
            returnValue = searchStorageDomain();
            break;
        case Quota:
            returnValue = searchQuota();
            break;
        case Disk:
            returnValue = searchDisk();
            break;
        case GlusterVolume:
            returnValue = searchGlusterVolumes();
            break;
        case Network:
            returnValue = searchNetworks();
            break;
        case Provider:
            returnValue = searchProviders();
            break;
        case InstanceType:
            returnValue = searchInstanceTypes();
            break;
        case ImageType:
            returnValue = searchImageTypes();
            break;
        case Session:
            returnValue = searchSessions();
            break;
        case ImageTransfer:
            returnValue = searchImageTransfer();
            break;
        case Job:
            returnValue = searchJobs();
            break;
        case VnicProfile:
            returnValue = searchVnicProfiles();
            break;
        default:
            log.error("Search object type not handled: {}", getParameters().getSearchTypeValue());
            break;
        }
        getQueryReturnValue().setReturnValue(returnValue);
    }

    private List<VM> searchVmsFromDb() {
        QueryData data = initQueryData(true);
        if (data == null) {
            return Collections.emptyList();
        }

        var javaZoneIdToOffset = vmHandler.getJavaZoneIdToOffsetFuncSupplier();
        List<VM> vms = vmDao.getAllUsingQuery(data.getQuery());
        Map<Guid, VM> vmsById = new HashMap<>();
        for (VM vm : vms) {
            vmHandler.updateVmGuestAgentVersion(vm);
            vmHandler.updateVmLock(vm);
            vmHandler.updateOperationProgress(vm);
            vmHandler.updateVmStatistics(vm);
            vmHandler.updateConfiguredCpuVerb(vm);
            vmHandler.updateIsDifferentTimeZone(vm, javaZoneIdToOffset);
            vmsById.put(vm.getId(), vm);
        }
        var vmIdsWithVnicsOutOfSync = vmNetworkInterfaceDao.getAllWithVnicOutOfSync(vmsById.keySet());
        vmIdsWithVnicsOutOfSync.stream().map(vmsById::get).filter(Objects::nonNull).forEach(vm -> vm.setVnicsOutOfSync(true));
        return vms;
    }

    private List<VDS> searchVDSsByDb() {
        List<VDS> data = genericSearch(vdsDao, true);
        for (VDS vds : data) {
            List<ServerCpu> supportedCpus = cpuFlagsManagerHandler.findServerCpusByFlags(
                    vds.getCpuFlags(),
                    vds.getClusterCompatibilityVersion());

            vds.setSupportedCpus(supportedCpus.stream().map(cpu -> cpu.getCpuName()).collect(Collectors.toList()));
            if (!supportedCpus.isEmpty()) {
                vds.setCpuName(supportedCpus.get(0));
            }

            List<String> missingFlags = cpuFlagsManagerHandler.missingServerCpuFlags(
                    vds.getClusterCpuName(),
                    vds.getCpuFlags(),
                    vds.getClusterCompatibilityVersion());
            if (missingFlags != null) {
                vds.setCpuFlagsMissing(missingFlags.stream().collect(Collectors.toSet()));
            }
            setNetworkOperationInProgressOnVds(vds);
        }
        return data;
    }

    private void setNetworkOperationInProgressOnVds(VDS vds) {
        vds.setNetworkOperationInProgress(lockManager.isExclusiveLockPresent(new EngineLock(hostLocking.getSetupNetworksLock(vds.getId()))));
    }

    private List<DirectoryUser> searchDirectoryUsers() {
        // Parse the query:
        QueryData data = initQueryData(true);
        if (data == null) {
            return Collections.emptyList();
        }

        List<DirectoryUser> results = new ArrayList<>();
        Map<String, Object> response = SsoOAuthServiceUtils.searchUsers(
                sessionDataContainer.getSsoAccessToken(getParameters().getSessionId()),
                getParamsMap(data));
        if (response.containsKey("result")) {
            Collection<ExtMap> users = (Collection<ExtMap>) response.get("result");
            results = users.stream()
                    .map((ExtMap u) -> directoryUtils.mapPrincipalRecordToDirectoryUser(data.getAuthz(), u))
                    .collect(Collectors.toList());
        }
        return results;
    }

    private static Map<String, Object> getParamsMap(QueryData queryData) {
        Map<String, Object> params = new HashMap<>();
        params.put("authz", queryData.getAuthz());
        params.put("namespace", queryData.getNamespace());
        params.put("query", queryData.getQuery());
        return params;
    }

    private List<DirectoryGroup> searchDirectoryGroups() {
        // Parse the query:
        QueryData data = initQueryData(true);
        if (data == null) {
            return Collections.emptyList();
        }

        List<DirectoryGroup> results = new ArrayList<>();
        Map<String, Object> response = SsoOAuthServiceUtils.searchGroups(
                sessionDataContainer.getSsoAccessToken(getParameters().getSessionId()),
                getParamsMap(data));
        if (response.containsKey("result")) {
            Collection<ExtMap> groups = (Collection<ExtMap>) response.get("result");
            results = groups.stream()
                    .map((ExtMap g) -> directoryUtils.mapGroupRecordToDirectoryGroup(data.getAuthz(), g))
                    .collect(Collectors.toList());
        }
        return results;
    }

    private List<DbUser> searchDbUsers() {
        return genericSearch(dbUserDao, true);
    }

    private List<DbGroup> searchDbGroups() {
        return genericSearch(dbGroupDao, true);
    }

    private List<VmTemplate> searchVmTemplates() {
        return genericSearch(vmTemplateDao, true);
    }

    private List<VmTemplate> searchInstanceTypes() {
        return genericSearch(vmTemplateDao, true);
    }

    private List<VmTemplate> searchImageTypes() {
        return genericSearch(vmTemplateDao, true);
    }

    private <T extends Queryable> List<T> genericSearch(final SearchDao<T> dao,
            final boolean useCache) {
        final QueryData data = initQueryData(useCache);
        if (data == null) {
            return new ArrayList<>();
        }

        log.debug("Executing generic query: {}", data.getQuery());
        return dao.getAllWithQuery(data.getQuery());
    }

    private List<AuditLog> searchAuditLogEvents() {
        return genericSearch(auditLogDao, false);
    }

    private List<VmPool> searchVmPools() {
        return genericSearch(vmPoolDao, true);
    }

    private List<Cluster> searchClusters() {
        Optional<Version> retVal = Config.<Set<Version>> getValue(ConfigValues.SupportedClusterLevels)
                .stream()
                .max(Comparator.naturalOrder());
        List<Cluster> clusters = genericSearch(clusterDao, true);
        if (retVal.isPresent()) {
            clusters.forEach(cluster -> cluster.setClusterCompatibilityLevelUpgradeNeeded(
                            retVal.get().compareTo(cluster.getCompatibilityVersion()) > 0)
                    );
        }
        for(Cluster cluster: clusters) {
            if (cluster.isManaged()) {
                List<VDS> hostsWithMissingFlags = backend.runInternalQuery(QueryType.GetHostsWithMissingFlagsForCluster,
                        new IdQueryParameters(cluster.getId())).getReturnValue();
                cluster.setHasHostWithMissingCpuFlags(!hostsWithMissingFlags.isEmpty());
            }

            String verb = cpuFlagsManagerHandler.getCpuId(cluster.getCpuName(), cluster.getCompatibilityVersion());
            cluster.setConfiguredCpuVerb(verb);
        }
        return clusters;
    }

    private List<StoragePool> searchStoragePool() {
        List<StoragePool> dataCenters = genericSearch(storagePoolDao, true);
        dataCenters.forEach(this::setDcSingleMacPoolId);
        setDcCompatibilityLevelUpgradeNeeded(dataCenters);
        return dataCenters;
    }

    private void setDcSingleMacPoolId(StoragePool dataCenter) {
        dataCenter.setMacPoolId(dcSingleMacPoolFinder.find(dataCenter.getId()));
    }

    private void setDcCompatibilityLevelUpgradeNeeded(List<StoragePool> dataCenters) {
        Config.<Set<Version>> getValue(ConfigValues.SupportedClusterLevels)
                .stream()
                .max(Comparator.naturalOrder())
                .ifPresent(maxSupportedClusterLevel ->
                        setDcCompatibilityLevelUpgradeNeeded(dataCenters, maxSupportedClusterLevel));
    }

    private void setDcCompatibilityLevelUpgradeNeeded(List<StoragePool> dataCenters, Version maxSupportedClusterLevel) {
        dataCenters.forEach(
                dataCenter -> dataCenter.setStoragePoolCompatibilityLevelUpgradeNeeded(
                        maxSupportedClusterLevel.compareTo(dataCenter.getCompatibilityVersion()) > 0));
    }

    private List<StorageDomain> searchStorageDomain() {
        return genericSearch(storageDomainDao, true);
    }

    private List<Quota> searchQuota() {
        List<Quota> quotaList = genericSearch(quotaDao, true);
        quotaManager.updateUsage(quotaList);
        return quotaList;
    }

    private List<Disk> searchDisk() {
        return genericSearch(diskDao, true);
    }

    private List<GlusterVolumeEntity> searchGlusterVolumes() {
        return genericSearch(glusterVolumeDao, true);
    }

    private List<NetworkView> searchNetworks() {
        return genericSearch(networkViewDao, true);
    }

    private List<VnicProfileView> searchVnicProfiles() {
        return genericSearch(vnicProfileViewDao, true);
    }

    private List<Provider<?>> searchProviders() {
        return genericSearch(providerDao, true);
    }

    private List<UserSession> searchSessions() {
        return genericSearch(engineSessionDao, false).stream().peek(this::injectSessionInfo)
                .map(UserSession::new)
                .collect(Collectors.toList());
    }

    private List<ImageTransfer> searchImageTransfer() {
        return genericSearch(imageTransferDao, false);
    }

    private List<? extends Queryable> searchJobs() {
        return genericSearch(jobDao, false);
    }

    private void injectSessionInfo(EngineSession engineSession) {
        engineSession.setStartTime(getSessionDataContainer().getSessionStartTime(engineSession.getEngineSessionId()));
        engineSession.setLastActiveTime(
                getSessionDataContainer().getSessionLastActiveTime(engineSession.getEngineSessionId()));
    }

    private static final String[] AD_SEARCH_TYPES = {
            SearchObjects.AD_USER_OBJ_NAME,
            SearchObjects.AD_USER_PLU_OBJ_NAME,
            SearchObjects.AD_GROUP_OBJ_NAME,
            SearchObjects.AD_GROUP_PLU_OBJ_NAME
    };

    private static final Pattern adSearchPattern = Pattern.compile(
            String.format(
                    "^((?<prefix>(%s))@)(?<content>.*)",
                    StringUtils.join(AD_SEARCH_TYPES, "|")));

    private QueryData initQueryData(boolean useCache) {
        final String ASTR = "*";
        QueryData data = null;
        boolean isExistsValue = false;
        boolean IsFromYesterday = false;
        boolean isSafe = false;
        String searchKey = "";
        try {
            if (getParameters().getMaxCount() < 0) {
                throw new RuntimeException(String.format("Illegal max count value for query : %s", getParameters().getMaxCount()));
            }
            String searchText = getParameters().getSearchPattern();
            // do not cache expressions with '*' since it is translated to specific IDs that might be changed
            useCache = useCache && !searchText.contains(ASTR);
            if (useCache) {
                // first lets check the cache of queries.
                searchKey = String.format("%1$s,%2$s,%3$s", searchText, getParameters().getMaxCount(), getParameters().getCaseSensitive());
                data = queriesCache.get(searchKey);
                isExistsValue = data != null;

                if (isExistsValue) {
                    TimeSpan span = DateTime.getNow().subtract(new Date(data.getDate()));
                    if (span.Days >= 1) {
                        IsFromYesterday = true;
                    }
                }
            }
            // query not in cache or the cached entry is too old, process the
            // search text.
            if (!isExistsValue || IsFromYesterday) {
                log.debug("ResourceManager::searchBusinessObjects(''{}'') - entered", searchText);
                final char AT='@';
                String queryAuthz = null;
                String queryNamespace = null;
                ISyntaxChecker curSyntaxChecker;
                Matcher m = adSearchPattern.matcher(searchText);
                // checks if this is a AD query, if it is, verify given profile and namespace and pass the query
                if (m.matches()) {
                    final String COLON = ":";
                    String prefix = m.group("prefix");
                    searchText =  m.group("content");
                    // get profile
                    List<String> profiles = backend.runInternalQuery(QueryType.GetDomainList,
                            new QueryParametersBase(getParameters().getSessionId())).getReturnValue();
                    for (String profile : profiles) {
                        if (searchText.startsWith(profile + COLON)) {
                            queryAuthz = profile;
                            searchText = searchText.replaceFirst(profile + COLON, StringUtils.EMPTY);
                            break;
                        }
                    }
                    if (queryAuthz == null) {
                        queryAuthz = getDefaultAuthz();
                    }
                    // get namespace
                    Map<String, List<String>> namespacesMap =
                            backend.runInternalQuery(QueryType.GetAvailableNamespaces,
                                    new QueryParametersBase(getParameters().getSessionId())).getReturnValue();
                    List<String> namespaces = namespacesMap.get(queryAuthz);
                    for (String namespace : namespaces) {
                        if (searchText.startsWith(namespace + COLON)) {
                            queryNamespace = namespace;
                            searchText = searchText.replace(namespace + COLON, StringUtils.EMPTY);
                            break;
                        }
                    }
                    // Check if query is for all namespaces (REST) i.e.:
                    // ADUSER/ADGROUP<profile>::<query>
                    if (searchText.startsWith(COLON)) {
                        searchText = prefix + searchText;
                    } else {
                        searchText = prefix + COLON + searchText;
                    }
                    curSyntaxChecker = SyntaxCheckerFactory.createADSyntaxChecker(LDAP);
                } else {
                    curSyntaxChecker = SyntaxCheckerFactory.createBackendSyntaxChecker(LDAP);
                }
                SyntaxContainer searchObj = curSyntaxChecker.analyzeSyntaxState(searchText, true);
                // set the case-sensitive flag
                searchObj.setCaseSensitive(getParameters().getCaseSensitive());
                // If a number > maxValue is given then maxValue will be used
                searchObj.setMaxCount(Math.min(Integer.MAX_VALUE, getParameters().getMaxCount()));
                // setting FromSearch value
                searchObj.setSearchFrom(getParameters().getSearchFrom());
                if (searchObj.getError() != SyntaxError.NO_ERROR) {
                    int startPos = searchObj.getErrorStartPos();
                    int endPos = searchObj.getErrorEndPos();
                    int length = endPos - startPos;
                    String error =
                            (length > 0 && ((startPos + 1 + length) < searchText.length())
                            && (endPos + 1 < searchText.length()))
                                    ?
                                    searchText.substring(0, startPos)
                                            + "$"
                                            + searchText.substring(startPos + 1, startPos + 1
                                                    + length) + "$"
                                            + searchText.substring(endPos + 1)
                                    :
                                    searchObj.getError().toString();
                    getQueryReturnValue().setExceptionString(error);
                    if (!queriesCache.containsKey(searchKey)) {
                        // log error only once
                        log.info(
                                "ResourceManager::searchBusinessObjects - erroneous search text - ''{}'' error - ''{}''",
                                searchText,
                                error);
                        // add search to the cache in order not process it again in case that
                        // this query is scheduled to be called repeatedly
                        queriesCache.put(searchKey, null);
                    }
                    return null;
                }
                if (!searchObj.getvalid()) {
                    if (!queriesCache.containsKey(searchKey)) {
                        log.warn("ResourceManager::searchBusinessObjects - Invalid search text - ''{}''", searchText);
                        queriesCache.put(searchKey, null);
                    }
                    return null;
                }
                // find if this is a trivial search expression (like 'Vms:' etc).
                isSafe = SearchObjects.isSafeExpression(searchText);
                // An expression is considered safe if matches a trivial search.
                data =
                        new QueryData(curSyntaxChecker.generateQueryFromSyntaxContainer(searchObj, isSafe),
                                DateTime.getNow().getTime(),
                                queryAuthz, queryNamespace);
                // when looking for tags , the query contains all parent children tag id's
                // statically, therefore , in order to reflect changes in the parent tree
                // we should not rely on the cached query in such case and have to build the
                // query from scratch.
                if (!containsStaticInValues(data.getQuery())) {
                    queriesCache.put(searchKey, data);
                }
            }
        } catch (SearchEngineIllegalCharacterException e) {
            if (!queriesCache.containsKey(searchKey)) {
                log.error("Search expression can not end with ESCAPE character: {}",
                        getParameters().getSearchPattern());
                queriesCache.put(searchKey, null);
            }
            data = null;
        } catch (SqlInjectionException e) {
            if (!queriesCache.containsKey(searchKey)) {
                log.error("Sql Injection in search: {}", getParameters().getSearchPattern());
                queriesCache.put(searchKey, null);
            }
            data = null;
        } catch (RuntimeException ex) {
            if (!queriesCache.containsKey(searchKey)) {
                log.warn("Illegal search: {}: {}", getParameters().getSearchPattern(), ex.getMessage());
                log.debug("Exception", ex);
                queriesCache.put(searchKey, null);
            }
            throw ex;
        }
        return data;
    }

    protected String getDefaultAuthz() {
        return AuthenticationProfileRepository.getInstance().getProfiles().get(0).getName();
    }

    private static boolean containsStaticInValues(String query) {
        final String MATCH_IN_TAG_ID_CLAUSE = "with_tags.tag_id in";
        return query.toLowerCase().contains(MATCH_IN_TAG_ID_CLAUSE);
    }
}
