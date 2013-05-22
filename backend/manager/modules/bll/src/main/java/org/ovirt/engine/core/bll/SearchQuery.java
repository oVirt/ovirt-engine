package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.ovirt.engine.core.bll.adbroker.AdActionType;
import org.ovirt.engine.core.bll.adbroker.LdapBroker;
import org.ovirt.engine.core.bll.adbroker.LdapBrokerUtils;
import org.ovirt.engine.core.bll.adbroker.LdapFactory;
import org.ovirt.engine.core.bll.adbroker.LdapQueryData;
import org.ovirt.engine.core.bll.adbroker.LdapQueryDataImpl;
import org.ovirt.engine.core.bll.adbroker.LdapQueryType;
import org.ovirt.engine.core.bll.adbroker.LdapReturnValueBase;
import org.ovirt.engine.core.bll.adbroker.LdapSearchByQueryParameters;
import org.ovirt.engine.core.bll.quota.QuotaManager;
import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.core.common.businessentities.DbUser;
import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.IVdcQueryable;
import org.ovirt.engine.core.common.businessentities.LdapGroup;
import org.ovirt.engine.core.common.businessentities.LdapUser;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VM;
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
import org.ovirt.engine.core.searchbackend.ISyntaxChecker;
import org.ovirt.engine.core.searchbackend.SearchObjects;
import org.ovirt.engine.core.searchbackend.SyntaxCheckerFactory;
import org.ovirt.engine.core.searchbackend.SyntaxContainer;
import org.ovirt.engine.core.searchbackend.SyntaxError;

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
        case AdGroup: {
            returnValue = searchAdGroups();
            break;
        }
        case AdUser: {
            returnValue = searchAdUsers();
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
                VmHandler.UpdateVmGuestAgentVersion(vm);
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

    private List<LdapUser> searchAdUsers() {
        return adSearch(LdapQueryType.searchUsers, AdActionType.SearchUserByQuery);
    }

    private List<LdapGroup> searchAdGroups() {
        return adSearch(LdapQueryType.searchGroups, AdActionType.SearchGroupsByQuery);
    }

    /**
     * Performs an ldap query
     * @param ldapQueryType The type of query to run
     * @param adActionType The action to submit to the LdapBroker
     * @return The result of the query
     */
    private <T extends IVdcQueryable> List<T> adSearch(LdapQueryType ldapQueryType, AdActionType adActionType) {
        QueryData data = initQueryData(true);

        if (data == null) {
            return new ArrayList<T>();
        }

        LdapQueryData ldapQueryData = new LdapQueryDataImpl();
        ldapQueryData.setLdapQueryType(ldapQueryType);
        ldapQueryData.setDomain(data.getDomain());
        ldapQueryData.setFilterParameters(new Object[] { data.getQueryForAdBroker() });

        LdapReturnValueBase returnValue =
                getLdapFactory(data.getDomain()).RunAdAction(adActionType,
                        new LdapSearchByQueryParameters(data.getDomain(), ldapQueryData));
        getQueryReturnValue().setSucceeded(returnValue.getSucceeded());
        getQueryReturnValue().setExceptionString(returnValue.getExceptionString());
        @SuppressWarnings("unchecked")
        List<T> result = (List<T>) returnValue.getReturnValue();

        return (result != null) ? result : new ArrayList<T>();
    }

    private List<DbUser> searchDbUsers() {
        return genericSearch(getDbFacade().getDbUserDao(), true, null);
    }

    private List<VmTemplate> searchVMTemplates() {
        return genericSearch(getDbFacade().getVmTemplateDao(), true, new Filter<VmTemplate>() {
            @Override
            public List<VmTemplate> filter(final List<VmTemplate> data) {
                for (IVdcQueryable vmt_helper : data) {
                    VmTemplate vmt = (VmTemplate) vmt_helper;
                    VmTemplateHandler.UpdateDisksFromDb(vmt);
                }
                return data;
            }
        });
    }

    private final <T extends IVdcQueryable> List<T> genericSearch(final SearchDAO<T> dao,
            final boolean useCache,
            final Filter<T> filter) {
        final QueryData data = initQueryData(useCache);
        if (data == null) {
            return new ArrayList<T>();
        }

        return ListUtils.filter(dao.getAllWithQuery(data.getQuery()), filter);
    }

    private List<AuditLog> searchAuditLogEvents() {
        return genericSearch(getDbFacade().getAuditLogDao(), false, null);
    }

    private List<VmPool> searchVmPools() {
        return genericSearch(getDbFacade().getVmPoolDao(), true, null);
    }

    private List<VDSGroup> searchClusters() {
        return genericSearch(getDbFacade().getVdsGroupDao(), true, null);
    }

    private List<StoragePool> searchStoragePool() {
        return genericSearch(getDbFacade().getStoragePoolDao(), true, null);
    }

    private List<StorageDomain> searchStorageDomain() {
        return genericSearch(getDbFacade().getStorageDomainDao(), true, null);
    }

    private List<Quota> searchQuota() {
        List<Quota> quotaList = genericSearch(getDbFacade().getQuotaDao(), true, null);
        QuotaManager.getInstance().updateUsage(quotaList);
        return quotaList;
    }

    private List<Disk> searchDisk() {
        return genericSearch(getDbFacade().getDiskDao(), true, null);
    }

    private List<GlusterVolumeEntity> searchGlusterVolumes() {
        return genericSearch(getDbFacade().getGlusterVolumeDao(), true, null);
    }

    private List<NetworkView> searchNetworks() {
        return genericSearch(getDbFacade().getNetworkViewDao(), true, null);
    }

    private QueryData initQueryData(boolean useCache) {
        QueryData data = null;
        boolean isExistsValue = false;
        boolean IsFromYesterday = false;
        boolean isSafe = false;
        String searchKey = "";
        try {
            String searchText = getParameters().getSearchPattern();
            // find if this is a trivial search expression (like 'Vms:' etc).
            isSafe = SearchObjects.isSafeExpression(searchText);
            if (useCache) {
                // first lets check the cache of queries.
                searchKey = String.format("%1$s,%2$s,%3$s", searchText, getParameters().getMaxCount(),getParameters().getCaseSensitive());
                data = mQueriesCache.get(searchKey);
                isExistsValue = (data != null);

                if (isExistsValue) {
                    TimeSpan span = DateTime.getNow().Subtract(data.getDate());
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
                    curSyntaxChecker = SyntaxCheckerFactory.CreateADSyntaxChecker(Config
                            .<String> GetValue(ConfigValues.AuthenticationMethod));
                } else {
                    curSyntaxChecker = SyntaxCheckerFactory
                            .CreateBackendSyntaxChecker(Config.<String> GetValue(ConfigValues.AuthenticationMethod));
                }
                SyntaxContainer searchObj = curSyntaxChecker.analyzeSyntaxState(searchText, true);
                // set the case-sensitive flag
                searchObj.setCaseSensitive(getParameters().getCaseSensitive());
                searchObj.setMaxCount(getParameters().getMaxCount() == -1 ? Config
                        .<Integer> GetValue(ConfigValues.SearchResultsLimit) : getParameters().getMaxCount());
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
                if (searchObj.getvalid() != true) {
                    log.warnFormat("ResourceManager::searchBusinessObjects - Invalid search text - ''{0}''", searchText);
                    return null;
                }
                // An expression is considered safe if matches a trivial search.
                data =
                        new QueryData(curSyntaxChecker.generateQueryFromSyntaxContainer(searchObj, isSafe),
                                searchObj.getSearchObjectStr(),
                                new Date(),
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
        return LdapBrokerUtils.getDomainsList().get(0);
    }

    protected LdapBroker getLdapFactory(String domain) {
        return LdapFactory.getInstance(domain);
    }

    private static boolean containsStaticInValues(String query) {
        final String MATCH_IN_TAG_ID_CLAUSE = "with_tags.tag_id in";
        return query.toLowerCase().contains(MATCH_IN_TAG_ID_CLAUSE);
    }
}
