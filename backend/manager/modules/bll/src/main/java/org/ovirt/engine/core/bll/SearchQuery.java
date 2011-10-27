package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.bll.adbroker.AdActionType;
import org.ovirt.engine.core.bll.adbroker.LdapBrokerUtils;
import org.ovirt.engine.core.bll.adbroker.LdapFactory;
import org.ovirt.engine.core.bll.adbroker.LdapQueryData;
import org.ovirt.engine.core.bll.adbroker.LdapQueryDataImpl;
import org.ovirt.engine.core.bll.adbroker.LdapQueryType;
import org.ovirt.engine.core.bll.adbroker.LdapSearchByQueryParameters;
import org.ovirt.engine.core.common.businessentities.AdUser;
import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.core.common.businessentities.DbUser;
import org.ovirt.engine.core.common.businessentities.IVdcQueryable;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.ad_groups;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.common.businessentities.vm_pools;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.SearchEngineIllegalCharacterException;
import org.ovirt.engine.core.common.errors.SqlInjectionException;
import org.ovirt.engine.core.common.queries.SearchParameters;
import org.ovirt.engine.core.common.queries.SearchReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.compat.DateTime;
import org.ovirt.engine.core.compat.LogCompat;
import org.ovirt.engine.core.compat.LogFactoryCompat;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.compat.TimeSpan;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.searchbackend.ISyntaxChecker;
import org.ovirt.engine.core.searchbackend.SearchObjects;
import org.ovirt.engine.core.searchbackend.SyntaxCheckerFactory;
import org.ovirt.engine.core.searchbackend.SyntaxContainer;
import org.ovirt.engine.core.searchbackend.SyntaxError;

public class SearchQuery<P extends SearchParameters> extends QueriesCommandBase<P> {
    private static java.util.HashMap<String, QueryData2> mQueriesCache = new java.util.HashMap<String, QueryData2>();
    private static ISyntaxChecker _defaultSyntaxChecker;
    static {
        // Dictionary<string, string> dictionary =
        // LicenseManager.LicenseManager.Instance.GetLicenseProperties();
        // if (dictionary.ContainsKey("ProductTypeProperty") &&
        // dictionary["ProductTypeProperty"].Contains("Desktop"))
        // {
        _defaultSyntaxChecker = SyntaxCheckerFactory.CreateBackendSyntaxChecker(Config
                .<String> GetValue(ConfigValues.AuthenticationMethod));
        // }
        // else
        // {
        // _defaultSyntaxChecker =
        // SyntaxCheckerFactory.CreateBackendSyntaxCheckerServersOnly();
        // }
    }

    public SearchQuery(P parameters) {
        super(parameters);

    }

    private SearchReturnValue getSearchReturnValue() {
        VdcQueryReturnValue tempVar = getQueryReturnValue();
        return (SearchReturnValue) ((tempVar instanceof SearchReturnValue) ? tempVar : null);
    }

    @Override
    protected void ProceedOnFail() {
        getSearchReturnValue().setIsSearchValid(false);
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
        default: {
            break;
        }
        }
        getSearchReturnValue().setReturnValue(returnValue);
    }

    protected List<VM> searchVmsFromDb() {
        List<VM> returnValue = null;
        QueryData2 data = InitQueryData(true);
        if (data == null) {
            returnValue = new ArrayList<VM>();
            getQueryReturnValue().setExceptionString(getSearchReturnValue().getExceptionString());
        } else {
            returnValue = DbFacade.getInstance().getVmDAO().getAllUsingQuery(data.getQuery());
            for (VM vm : returnValue) {
                // VmHandler.updateDisksFromDb(vm);
                // vm.Interfaces =
                // DbFacade.Instance.getIterfacesByVmId(vm.vm_guid);
                VmHandler.UpdateVmGuestAgentVersion(vm);
            }
        }
        return returnValue;
    }

    protected List<VDS> searchVDSsByDb() {
        List<VDS> returnValue = null;
        QueryData2 data = InitQueryData(true);
        if (data == null) {
            returnValue = new ArrayList<VDS>();
        } else {
            returnValue = DbFacade.getInstance().getVdsDAO().getAllWithQuery(data.getQuery());
            for (VDS vds : returnValue) {
                vds.setCpuName(CpuFlagsManagerHandler.FindMaxServerCpuByFlags(vds.getcpu_flags(),
                        vds.getvds_group_compatibility_version()));
            }
        }
        return returnValue;
    }

    protected java.util.ArrayList<AdUser> searchAdUsers() {
        QueryData2 data = InitQueryData(true);

        if (data == null) {
            return new java.util.ArrayList<AdUser>();
        } else {
            LdapQueryData ldapQueryData = new LdapQueryDataImpl();
            ldapQueryData.setLdapQueryType(LdapQueryType.searchUsers);
            ldapQueryData.setFilterParameters(new Object[] { data.getQueryForAdBroker() });
            ldapQueryData.setDomain(data.getDomain());
            java.util.ArrayList<AdUser> result = (java.util.ArrayList<AdUser>) LdapFactory
                    .getInstance(data.getDomain())
                    .RunAdAction(AdActionType.SearchUserByQuery,
                            new LdapSearchByQueryParameters(data.getDomain(), ldapQueryData))
                    .getReturnValue();
            return (result != null) ? result : new java.util.ArrayList<AdUser>();
        }
    }

    protected List<DbUser> searchDbUsers() {
        List<DbUser> returnValue = null;
        QueryData2 data = InitQueryData(true);
        if (data == null) {
            returnValue = new ArrayList<DbUser>();
        } else {
            returnValue = DbFacade.getInstance().getDbUserDAO().getAllWithQuery(data.getQuery());
        }

        return returnValue;
    }

    protected java.util.ArrayList<ad_groups> searchAdGroups() {
        QueryData2 data = InitQueryData(true);

        if (data == null) {
            return new java.util.ArrayList<ad_groups>();
        } else {
            LdapQueryData ldapQueryData = new LdapQueryDataImpl();
            ldapQueryData.setLdapQueryType(LdapQueryType.searchGroups);
            ldapQueryData.setDomain(data.getDomain());
            ldapQueryData.setFilterParameters(new Object[] { data.getQueryForAdBroker() });

            java.util.ArrayList<ad_groups> result = (java.util.ArrayList<ad_groups>) LdapFactory
                    .getInstance(data.getDomain())
                    .RunAdAction(AdActionType.SearchGroupsByQuery,
                            new LdapSearchByQueryParameters(data.getDomain(), ldapQueryData))
                    .getReturnValue();
            return (result != null) ? result : new java.util.ArrayList<ad_groups>();
        }
    }

    protected List<VmTemplate> searchVMTemplates() {
        List<VmTemplate> returnValue;
        QueryData2 data = InitQueryData(true);
        if (data == null) {
            returnValue = new ArrayList<VmTemplate>();
        } else {
            List<VmTemplate> tmp = DbFacade.getInstance().getVmTemplateDAO().getAllWithQuery(data.getQuery());
            for (IVdcQueryable vmt_helper : tmp) {
                VmTemplate vmt = (VmTemplate) vmt_helper;
                VmTemplateHandler.UpdateDisksFromDb(vmt);
            }
            returnValue = tmp;
        }
        return returnValue;
    }

    protected List<AuditLog> searchAuditLogEvents() {
        List<AuditLog> returnValue = null;
        QueryData2 data = InitQueryData(false);
        if (data == null) {
            returnValue = new ArrayList<AuditLog>();
        } else {
            returnValue = DbFacade.getInstance().getAuditLogDAO().getAllWithQuery(data.getQuery());
        }
        return returnValue;
    }

    protected List<vm_pools> searchVmPools() {
        List<vm_pools> returnValue = null;
        QueryData2 data = InitQueryData(true);
        if (data == null) {
            returnValue = new ArrayList<vm_pools>();
        } else {
            returnValue = DbFacade.getInstance().getVmPoolDAO().getAllWithQuery(data.getQuery());
        }

        return returnValue;
    }

    protected List<VDSGroup> searchClusters() {
        List<VDSGroup> returnValue = null;
        QueryData2 data = InitQueryData(true);
        if (data == null) {
            returnValue = new ArrayList<VDSGroup>();
        } else {
            returnValue = DbFacade.getInstance().getVdsGroupDAO().getAllWithQuery(data.getQuery());
        }
        return returnValue;
    }

    protected List<storage_pool> searchStoragePool() {
        List<storage_pool> returnValue = null;
        QueryData2 data = InitQueryData(true);
        if (data == null) {
            returnValue = new ArrayList<storage_pool>();
        } else {
            returnValue = DbFacade.getInstance().getStoragePoolDAO().getAllWithQuery(data.getQuery());
        }
        return returnValue;
    }

    protected List<storage_domains> searchStorageDomain() {
        List<storage_domains> returnValue = null;
        QueryData2 data = InitQueryData(true);
        if (data == null) {
            returnValue = new ArrayList<storage_domains>();
        } else {
            returnValue = DbFacade.getInstance().getStorageDomainDAO().getAllWithQuery(data.getQuery());
        }
        return returnValue;
    }

    private QueryData2 InitQueryData(boolean useCache) {
        QueryData2 data = null;
        boolean isExistsValue = false;
        boolean IsFromYesterday = false;
        boolean isSafe = false;
        String searchKey = "";
        try {
            String searchText = getParameters().getSearchPattern();
            // find if this is a trivial search expression (like 'Vms:' etc).
            isSafe = SearchObjects.isSafeExpression(searchText);
            getSearchReturnValue().setIsSearchValid(true);
            if (useCache) {
                // first lets check the cache of queries.
                searchKey = String.format("%1$s,%2$s", searchText, getParameters().getMaxCount());
                isExistsValue = (data = mQueriesCache.get(searchKey)) != null;

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
                data = new QueryData2();
                data.setPreQueryCommand(DbFacade.getInstance().getDbEngineDialect().getPreSearchQueryCommand());
                ISyntaxChecker curSyntaxChecker;
                String[] splitted = searchText.split("[:@ ]");
                if ((StringHelper.EqOp(splitted[0].toUpperCase(), SearchObjects.AD_USER_OBJ_NAME))
                        || (StringHelper.EqOp(splitted[0].toUpperCase(), SearchObjects.AD_USER_PLU_OBJ_NAME))
                        || (StringHelper.EqOp(splitted[0].toUpperCase(), SearchObjects.AD_GROUP_OBJ_NAME))
                        || (StringHelper.EqOp(splitted[0].toUpperCase(), SearchObjects.AD_GROUP_PLU_OBJ_NAME))) {
                    if (searchText.indexOf('@') > 0 && splitted.length > 1) {
                        data.setDomain(splitted[1]);
                        searchText = searchText.substring(0, searchText.indexOf('@'))
                                + searchText.substring(searchText.indexOf(':'));
                    } else {
                        String domain = LdapBrokerUtils.getDomainsList().get(0);
                        data.setDomain(domain);
                    }
                    curSyntaxChecker = SyntaxCheckerFactory.CreateADSyntaxChecker(Config
                            .<String> GetValue(ConfigValues.AuthenticationMethod));
                } else {
                    curSyntaxChecker = _defaultSyntaxChecker;
                }
                SyntaxContainer searchObj = curSyntaxChecker.analyzeSyntaxState(searchText, true);
                // set the case-sensitive flag
                searchObj.setCaseSensitive(getParameters().getCaseSensitive());
                searchObj.setMaxCount(getParameters().getMaxCount() == -1 ? Config
                        .<Integer> GetValue(ConfigValues.SearchResultsLimit) : getParameters().getMaxCount());
                // setting FromSearch value
                searchObj.setSearchFrom(getParameters().getSearchFrom());

                if (searchObj.getError() != SyntaxError.NO_ERROR) {
                    getSearchReturnValue().setIsSearchValid(false);
                    log.warnFormat("ResourceManager::searchBusinessObjects - erroneous search text - ''{0}''",
                            searchText);
                    // throw new
                    // VdcBLLException(VdcBllErrors.SEARCH_ERRORNOUS_SEARCH_TEXT);
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
                    getSearchReturnValue().setExceptionString(error);
                    return null;
                }
                if (searchObj.getvalid() != true) {
                    getSearchReturnValue().setIsSearchValid(false);
                    log.warnFormat("ResourceManager::searchBusinessObjects - Invalid search text - ''{0}''", searchText);
                    // throw new
                    // VdcBLLException(VdcBllErrors.SEARCH_INVALID_SEARCH_TEXT);
                    return null;
                }
                // An expression is considered safe if matches a trivial search.
                data.setQType(searchObj.getSearchObjectStr());
                data.setQuery(curSyntaxChecker.generateQueryFromSyntaxContainer(searchObj, isSafe));
                data.setDate(new java.util.Date());
                // when looking for tags , the query contains all parent children tag id's
                // statically, therefore , in order to reflect changes in the parent tree
                // we should not rely on the cached query in such case and have to build the
                // query from scratch.
                if (!containsStaticInValues(data.getQuery()))
                    mQueriesCache.put(searchKey, data);
            }
        } catch (SearchEngineIllegalCharacterException e){
            log.error("Search expression can not end with ESCAPE character:" + getParameters().getSearchPattern());
            data = null;
            getSearchReturnValue().setIsSearchValid(false);
        } catch (SqlInjectionException e) {
            log.error("Sql Injection in search: " + getParameters().getSearchPattern());
            data = null;
            getSearchReturnValue().setIsSearchValid(false);
        } catch (RuntimeException ex) {
            log.warn("Illegal search: " + getParameters().getSearchPattern(), ex);
            data = null;
            getSearchReturnValue().setIsSearchValid(false);
        }
        return data;
    }

    private boolean containsStaticInValues(String query) {
        final String MATCH_IN_TAG_ID_CLAUSE = "with_tags.tag_id in";
        return query.toLowerCase().contains(MATCH_IN_TAG_ID_CLAUSE);
    }

    private static LogCompat log = LogFactoryCompat.getLog(SearchQuery.class);
}
