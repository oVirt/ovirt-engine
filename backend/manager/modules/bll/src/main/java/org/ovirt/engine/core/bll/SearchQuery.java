package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.ovirt.engine.api.extensions.aaa.Authz;
import org.ovirt.engine.core.aaa.AuthenticationProfileRepository;
import org.ovirt.engine.core.aaa.DirectoryGroup;
import org.ovirt.engine.core.aaa.DirectoryUser;
import org.ovirt.engine.core.aaa.DirectoryUtils;
import org.ovirt.engine.core.bll.quota.QuotaManager;
import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.core.common.businessentities.DbGroup;
import org.ovirt.engine.core.common.businessentities.DbUser;
import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.IVdcQueryable;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmEntityType;
import org.ovirt.engine.core.common.businessentities.VmPool;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.businessentities.network.NetworkView;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.SearchEngineIllegalCharacterException;
import org.ovirt.engine.core.common.errors.SqlInjectionException;
import org.ovirt.engine.core.common.queries.SearchParameters;
import org.ovirt.engine.core.common.utils.ListUtils;
import org.ovirt.engine.core.common.utils.ListUtils.Filter;
import org.ovirt.engine.core.compat.DateTime;
import org.ovirt.engine.core.compat.TimeSpan;
import org.ovirt.engine.core.dao.SearchDAO;
import org.ovirt.engine.core.extensions.mgr.ExtensionProxy;
import org.ovirt.engine.core.searchbackend.ISyntaxChecker;
import org.ovirt.engine.core.searchbackend.SearchObjects;
import org.ovirt.engine.core.searchbackend.SyntaxCheckerFactory;
import org.ovirt.engine.core.searchbackend.SyntaxContainer;
import org.ovirt.engine.core.searchbackend.SyntaxError;
import org.ovirt.engine.core.utils.extensionsmgr.EngineExtensionsManager;

public class SearchQuery<P extends SearchParameters> extends QueriesCommandBase<P> {
    private static final HashMap<String, QueryData> mQueriesCache = new HashMap<String, QueryData>();

    public SearchQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        List<? extends IVdcQueryable> returnValue = new ArrayList<IVdcQueryable>();
        switch (getParameters().getSearchTypeValue()) {
        case VM: {
            returnValue = searchVmsFromDb();
            break;
        }
        case DirectoryGroup: {
            returnValue = searchDirectoryGroups();
            break;
        }
        case DirectoryUser: {
            returnValue = searchDirectoryUsers();
            break;
        }
        case AuditLog: {
            returnValue = searchAuditLogEvents();
            break;
        }
        case DBUser: {
            returnValue = searchDbUsers();
            break;
        }
        case DBGroup: {
            returnValue = searchDbGroups();
            break;
        }
        case VDS: {
            returnValue = searchVDSsByDb();
            break;
        }
        case VmTemplate: {
            returnValue = searchVMTemplates();
            break;
        }
        case VmPools: {
            returnValue = searchVmPools();
            break;
        }
        case Cluster: {
            returnValue = searchClusters();
            break;
        }
        case StoragePool: {
            returnValue = searchStoragePool();
            break;
        }
        case StorageDomain: {
            returnValue = searchStorageDomain();
            break;
        }
        case Quota: {
            returnValue = searchQuota();
            break;
        }
        case Disk: {
            returnValue = searchDisk();
            break;
        }
        case GlusterVolume: {
            returnValue = searchGlusterVolumes();
            break;
        }
        case Network: {
            returnValue = searchNetworks();
            break;
        }
        case Provider: {
            returnValue = searchProviders();
            break;
        }
        case InstanceType: {
            returnValue = searchInstanceTypes();
            break;
        }
        case ImageType: {
            returnValue = searchVMTemplates();
            break;
        }
        default: {
            log.errorFormat("Search object type not handled: {0}", getParameters().getSearchTypeValue());
            break;
        }
        }
        getQueryReturnValue().setReturnValue(returnValue);
    }

    private List<VM> searchVmsFromDb() {
        List<VM> returnValue = null;

        QueryData data = initQueryData(true);
        if (data == null) {
            returnValue = new ArrayList<VM>();
            getQueryReturnValue().setExceptionString(getQueryReturnValue().getExceptionString());
        } else {
            returnValue = getDbFacade().getVmDao().getAllUsingQuery(data.getQuery());
            for (VM vm : returnValue) {
                VmHandler.updateVmGuestAgentVersion(vm);
            }
        }
        return returnValue;
    }

    private List<VDS> searchVDSsByDb() {
        return genericSearch(getDbFacade().getVdsDao(), true, new Filter<VDS>() {
            @Override
            public List<VDS> filter(List<VDS> data) {
                for (VDS vds : data) {
                    vds.setCpuName(CpuFlagsManagerHandler.FindMaxServerCpuByFlags(vds.getCpuFlags(),
                            vds.getVdsGroupCompatibilityVersion()));
                }
                return data;
            }
        });
    }

    private List<DirectoryUser> searchDirectoryUsers() {
        // Parse the query:
        QueryData data = initQueryData(true);
        if (data == null) {
            return Collections.emptyList();
        }

        ExtensionProxy authz = EngineExtensionsManager.getInstance().getExtensionByName(data.getDomain());
        List<DirectoryUser> results = new ArrayList<>();
        for (String namespace : authz.getContext().<List<String>> get(Authz.ContextKeys.AVAILABLE_NAMESPACES)) {
            results.addAll(DirectoryUtils.findDirectoryUsersByQuery(authz, namespace, data.getQuery()));
        }

        return results;
    }

    private List<DirectoryGroup> searchDirectoryGroups() {
        // Parse the query:
        QueryData data = initQueryData(true);
        if (data == null) {
            return Collections.emptyList();
        }

        ExtensionProxy authz = EngineExtensionsManager.getInstance().getExtensionByName(data.getDomain());
        List<DirectoryGroup> results = new ArrayList<>();
        for (String namespace : authz.getContext().<List<String>> get(Authz.ContextKeys.AVAILABLE_NAMESPACES)) {
            results.addAll(DirectoryUtils.findDirectoryGroupsByQuery(authz, namespace, data.getQuery()));
        }
        return results;
    }

    private List<DbUser> searchDbUsers() {
        return genericSearch(getDbFacade().getDbUserDao(), true);
    }

    private List<DbGroup> searchDbGroups() {
        return genericSearch(getDbFacade().getDbGroupDao(), true);
    }

    private List<VmTemplate> searchVMTemplates() {

        return genericSearch(getDbFacade().getVmTemplateDao(), true, new Filter<VmTemplate>() {
            @Override
            public List<VmTemplate> filter(final List<VmTemplate> data) {
                List<VmTemplate> filtered = new ArrayList<>();
                for (IVdcQueryable vmt_helper : data) {
                    VmTemplate vmt = (VmTemplate) vmt_helper;
                    if (vmt.getTemplateType() != VmEntityType.TEMPLATE) {
                        continue;
                    }
                    filtered.add(vmt);
                }
                return filtered;
            }
        });
    }

    private List<VmTemplate> searchInstanceTypes() {
        return genericSearch(getDbFacade().getVmTemplateDao(), true);
    }

    private final <T extends IVdcQueryable> List<T> genericSearch(final SearchDAO<T> dao,
            final boolean useCache) {
        return genericSearch(dao, useCache, null);
    }

    private final <T extends IVdcQueryable> List<T> genericSearch(final SearchDAO<T> dao,
            final boolean useCache,
            final Filter<T> filter) {
        final QueryData data = initQueryData(useCache);
        if (data == null) {
            return new ArrayList<T>();
        }

        log.debug("Executing generic query: " + data.getQuery());
        return ListUtils.filter(dao.getAllWithQuery(data.getQuery()), filter);
    }

    private List<AuditLog> searchAuditLogEvents() {
        return genericSearch(getDbFacade().getAuditLogDao(), false);
    }

    private List<VmPool> searchVmPools() {
        return genericSearch(getDbFacade().getVmPoolDao(), true);
    }

    private List<VDSGroup> searchClusters() {
        return genericSearch(getDbFacade().getVdsGroupDao(), true);
    }

    private List<StoragePool> searchStoragePool() {
        return genericSearch(getDbFacade().getStoragePoolDao(), true);
    }

    private List<StorageDomain> searchStorageDomain() {
        return genericSearch(getDbFacade().getStorageDomainDao(), true);
    }

    private List<Quota> searchQuota() {
        List<Quota> quotaList = genericSearch(getDbFacade().getQuotaDao(), true);
        QuotaManager.getInstance().updateUsage(quotaList);
        return quotaList;
    }

    private List<Disk> searchDisk() {
        return genericSearch(getDbFacade().getDiskDao(), true);
    }

    private List<GlusterVolumeEntity> searchGlusterVolumes() {
        return genericSearch(getDbFacade().getGlusterVolumeDao(), true);
    }

    private List<NetworkView> searchNetworks() {
        return genericSearch(getDbFacade().getNetworkViewDao(), true);
    }

    private List<Provider<?>> searchProviders() {
        return genericSearch(getDbFacade().getProviderDao(), true);
    }

    private QueryData initQueryData(boolean useCache) {
        QueryData data = null;
        boolean isExistsValue = false;
        boolean IsFromYesterday = false;
        boolean isSafe = false;
        String searchKey = "";
        try {
            String searchText = getParameters().getSearchPattern();
            if (useCache) {
                // first lets check the cache of queries.
                searchKey = String.format("%1$s,%2$s,%3$s", searchText, getParameters().getMaxCount(), getParameters().getCaseSensitive());
                data = mQueriesCache.get(searchKey);
                isExistsValue = (data != null);

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
                log.debugFormat("ResourceManager::searchBusinessObjects(''{0}'') - entered", searchText);
                String queryDomain = null;
                ISyntaxChecker curSyntaxChecker;
                String[] splitted = searchText.split("[:@ ]");
                final String objectName = splitted[0].toUpperCase();
                if ((SearchObjects.AD_USER_OBJ_NAME.equals(objectName))
                        || (SearchObjects.AD_USER_PLU_OBJ_NAME.equals(objectName))
                        || (SearchObjects.AD_GROUP_OBJ_NAME.equals(objectName))
                        || (SearchObjects.AD_GROUP_PLU_OBJ_NAME.equals(objectName))) {
                    if (searchText.indexOf('@') > 0 && splitted.length > 1) {
                        queryDomain = splitted[1];
                        searchText = searchText.substring(0, searchText.indexOf('@'))
                                + searchText.substring(searchText.indexOf(':'));
                    } else {
                        queryDomain = getDefaultDomain();
                    }
                    curSyntaxChecker = SyntaxCheckerFactory.createADSyntaxChecker(Config
                            .<String>getValue(ConfigValues.AuthenticationMethod));
                } else {
                    curSyntaxChecker = SyntaxCheckerFactory
                            .createBackendSyntaxChecker(Config.<String>getValue(ConfigValues.AuthenticationMethod));
                }
                SyntaxContainer searchObj = curSyntaxChecker.analyzeSyntaxState(searchText, true);
                // set the case-sensitive flag
                searchObj.setCaseSensitive(getParameters().getCaseSensitive());
                // If a number > maxValue is given then maxValue will be used
                searchObj.setMaxCount(getParameters().getMaxCount() == -1 ? Integer.MAX_VALUE : Math.min(Integer.MAX_VALUE, getParameters().getMaxCount()));
                // setting FromSearch value
                searchObj.setSearchFrom(getParameters().getSearchFrom());

                if (searchObj.getError() != SyntaxError.NO_ERROR) {
                    log.warnFormat("ResourceManager::searchBusinessObjects - erroneous search text - ''{0}''",
                            searchText);
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
                    return null;
                }
                if (!searchObj.getvalid()) {
                    log.warnFormat("ResourceManager::searchBusinessObjects - Invalid search text - ''{0}''", searchText);
                    return null;
                }
                // find if this is a trivial search expression (like 'Vms:' etc).
                isSafe = SearchObjects.isSafeExpression(searchText);
                // An expression is considered safe if matches a trivial search.
                data =
                        new QueryData(curSyntaxChecker.generateQueryFromSyntaxContainer(searchObj, isSafe),
                                DateTime.getNow().getTime(),
                                queryDomain);
                // when looking for tags , the query contains all parent children tag id's
                // statically, therefore , in order to reflect changes in the parent tree
                // we should not rely on the cached query in such case and have to build the
                // query from scratch.
                if (!containsStaticInValues(data.getQuery()))
                    mQueriesCache.put(searchKey, data);
            }
        } catch (SearchEngineIllegalCharacterException e) {
            log.error("Search expression can not end with ESCAPE character:" + getParameters().getSearchPattern());
            data = null;
        } catch (SqlInjectionException e) {
            log.error("Sql Injection in search: " + getParameters().getSearchPattern());
            data = null;
        } catch (RuntimeException ex) {
            log.warn("Illegal search: " + getParameters().getSearchPattern(), ex);
            data = null;
        }
        return data;
    }

    protected String getDefaultDomain() {
        return AuthenticationProfileRepository.getInstance().getProfiles().get(0).getName();
    }

    private static boolean containsStaticInValues(String query) {
        final String MATCH_IN_TAG_ID_CLAUSE = "with_tags.tag_id in";
        return query.toLowerCase().contains(MATCH_IN_TAG_ID_CLAUSE);
    }
}
