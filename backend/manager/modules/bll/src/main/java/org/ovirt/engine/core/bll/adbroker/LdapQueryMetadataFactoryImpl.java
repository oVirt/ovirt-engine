package org.ovirt.engine.core.bll.adbroker;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import javax.naming.directory.SearchControls;

import org.ovirt.engine.core.ldap.LdapProviderType;

public class LdapQueryMetadataFactoryImpl implements LdapQueryMetadataFactory {

    private static final Map<LdapProviderType, Map<LdapQueryType, LdapQueryMetadata>> queryMetadataMap;
    private static LdapQueryMetadataFactory instance;
    private static EnumMap<SearchLangageLDAPTokens, String> activeDirectorySearchSyntaxMap;
    private static EnumMap<SearchLangageLDAPTokens, String> ipaSearchSyntaxMap;
    private static EnumMap<SearchLangageLDAPTokens, String> dsSearchSyntaxMap;
    private static EnumMap<SearchLangageLDAPTokens, String> itdsSearchSyntaxMap;
    private static EnumMap<SearchLangageLDAPTokens, String> openLdapSearchSyntaxMap;

    @Override
    public LdapQueryMetadata getLdapQueryMetadata(LdapProviderType providerType, LdapQueryData queryData) {
        LdapQueryMetadata ldapQueryMetadata = queryMetadataMap.get(providerType).get(queryData.getLdapQueryType());
        ldapQueryMetadata.setQueryData(queryData);
        return ldapQueryMetadata;
    }

    private LdapQueryMetadataFactoryImpl() {
    }

    public static LdapQueryMetadataFactory getInstance() {
        return instance;
    }

    static {
        prepareQueryFormatters();

        Map<LdapQueryType, LdapQueryMetadata> adHashMap = setADMap();
        Map<LdapQueryType, LdapQueryMetadata> ipaHashMap = setIPAMap();
        Map<LdapQueryType, LdapQueryMetadata> dsHashMap = setDSMap();
        Map<LdapQueryType, LdapQueryMetadata> itdsHashMap = setITDSMap();
        Map<LdapQueryType, LdapQueryMetadata> openLdapHashMap = setOpenLdapMap();
        Map<LdapQueryType, LdapQueryMetadata> generalHashMap = setGeneralProviderMap();

        queryMetadataMap = new HashMap<LdapProviderType, Map<LdapQueryType, LdapQueryMetadata>>();
        queryMetadataMap.put(LdapProviderType.activeDirectory, adHashMap);
        queryMetadataMap.put(LdapProviderType.ipa, ipaHashMap);
        queryMetadataMap.put(LdapProviderType.rhds, dsHashMap);
        queryMetadataMap.put(LdapProviderType.itds, itdsHashMap);
        queryMetadataMap.put(LdapProviderType.openLdap, openLdapHashMap);
        queryMetadataMap.put(LdapProviderType.general, generalHashMap);

        instance = new LdapQueryMetadataFactoryImpl();
    }

    // The following creates the map of query metadata.
    // The arguments are:
    // 1. Filter expression
    // 2. Base DN expression
    // 3. The context mapper
    // 4. The list of attributes we want the query to return from the ldap provider
    // 5. The formatter - it formats the query (we currently have simple one, multiple (for queries like (|(...)(...))) and one for SearchBackend purposes
    // 6. The GUID encoder - sometimes we need to convert the binary data to string (in AD for example), or just toString (in IPA)

    private static Map<LdapQueryType, LdapQueryMetadata> setGeneralProviderMap() {
        Map<LdapQueryType, LdapQueryMetadata> generalHashMap = new HashMap<LdapQueryType, LdapQueryMetadata>();
        generalHashMap.put(LdapQueryType.rootDSE, new LdapQueryMetadataImpl(
                        "(objectClass=*)",
                        "",
                        null,
                        SearchControls.OBJECT_SCOPE,
                        null,
                        new SimpleLdapQueryExecutionFormatter(),
                        null));
        return generalHashMap;
    }

    private static HashMap<LdapQueryType, LdapQueryMetadata> setADMap() {
        HashMap<LdapQueryType, LdapQueryMetadata> adHashMap = new HashMap<LdapQueryType, LdapQueryMetadata>();

        adHashMap.put(LdapQueryType.getGroupByDN, new LdapQueryMetadataImpl(
                        "(cn=*)",
                        "%1$s",
                        new ADGroupContextMapper(),
                        SearchControls.OBJECT_SCOPE,
                        ADGroupContextMapper.GROUP_ATTRIBUTE_FILTER,
                        new SimpleLdapQueryExecutionFormatter(),
                        new ADLdapGuidEncoder()));
        adHashMap.put(LdapQueryType.getUserByGuid, new LdapQueryMetadataImpl(
                        "(objectGUID=%1$s)",
                        "",
                        new ADUserContextMapper(),
                        SearchControls.SUBTREE_SCOPE,
                        ADUserContextMapper.USERS_ATTRIBUTE_FILTER,
                        new SimpleLdapQueryExecutionFormatter(),
                        new ADLdapGuidEncoder()));
        adHashMap.put(LdapQueryType.getGroupByGuid, new LdapQueryMetadataImpl(
                        "(objectGUID=%1$s)",
                        "",
                        new ADGroupContextMapper(),
                        SearchControls.SUBTREE_SCOPE,
                        ADGroupContextMapper.GROUP_ATTRIBUTE_FILTER,
                        new SimpleLdapQueryExecutionFormatter(),
                        new ADLdapGuidEncoder()));
        adHashMap.put(LdapQueryType.getGroupByName, new LdapQueryMetadataImpl(
                        "(&(ObjectCategory=Group)(name=%1$s))",
                        "",
                        new ADGroupContextMapper(),
                        SearchControls.SUBTREE_SCOPE,
                        ADGroupContextMapper.GROUP_ATTRIBUTE_FILTER,
                        new SimpleLdapQueryExecutionFormatter(),
                        new ADLdapGuidEncoder()));
        adHashMap.put(LdapQueryType.getUserByPrincipalName, new LdapQueryMetadataImpl(
                        "(&(sAMAccountType=805306368)(userPrincipalName=%1$s))",
                        "",
                        new ADUserContextMapper(),
                        SearchControls.SUBTREE_SCOPE,
                        ADUserContextMapper.USERS_ATTRIBUTE_FILTER,
                        new SimpleLdapQueryExecutionFormatter(),
                        new ADLdapGuidEncoder()));
        adHashMap.put(LdapQueryType.getUserByName, new LdapQueryMetadataImpl(
                        "(&(sAMAccountType=805306368)(sAMAccountName=%1$s))",
                        "",
                        new ADUserContextMapper(),
                        SearchControls.SUBTREE_SCOPE,
                        ADUserContextMapper.USERS_ATTRIBUTE_FILTER,
                        new SimpleLdapQueryExecutionFormatter(),
                        new ADLdapGuidEncoder()));
        adHashMap.put(LdapQueryType.rootDSE, new LdapQueryMetadataImpl(
                        "(objectClass=*)",
                        "",
                        new ADRootDSEContextMapper(),
                        SearchControls.OBJECT_SCOPE,
                        ADRootDSEContextMapper.ROOTDSE_ATTRIBUTE_FILTER,
                        new SimpleLdapQueryExecutionFormatter(),
                        new ADLdapGuidEncoder()));
        adHashMap.put(LdapQueryType.getGroupsByGroupNames, new LdapQueryMetadataImpl(
                        "(&(ObjectCategory=Group)(name=%1$s))",
                        "",
                        new ADGroupContextMapper(),
                        SearchControls.SUBTREE_SCOPE,
                        ADGroupContextMapper.GROUP_ATTRIBUTE_FILTER,
                        new MultipleLdapQueryExecutionFormatter("(|", ")"),
                        new ADLdapGuidEncoder()));
        adHashMap.put(LdapQueryType.getUsersByUserGuids, new LdapQueryMetadataImpl(
                        "(objectGUID=%1$s)",
                        "",
                        new ADUserContextMapper(),
                        SearchControls.SUBTREE_SCOPE,
                        ADUserContextMapper.USERS_ATTRIBUTE_FILTER,
                        new MultipleLdapQueryExecutionFormatter("(|", ")"),
                        new ADLdapGuidEncoder()));

        LdapQueryMetadataImpl searchUsersMetadata =
                new LdapQueryMetadataImpl("this string is overrided by user input meta-query",
                        "",
                        new ADUserContextMapper(),
                        SearchControls.SUBTREE_SCOPE,
                        ADUserContextMapper.USERS_ATTRIBUTE_FILTER,
                        new SearchQueryFotmatter(activeDirectorySearchSyntaxMap),
                        new ADLdapGuidEncoder());
        adHashMap.put(LdapQueryType.searchUsers, searchUsersMetadata);

        LdapQueryMetadataImpl searchGroupsMetadata =
                new LdapQueryMetadataImpl("this string is overrided by user input meta-query",
                        "",
                        new ADGroupContextMapper(),
                        SearchControls.SUBTREE_SCOPE,
                        ADGroupContextMapper.GROUP_ATTRIBUTE_FILTER,
                        new SearchQueryFotmatter(activeDirectorySearchSyntaxMap),
                        new ADLdapGuidEncoder());
        adHashMap.put(LdapQueryType.searchGroups, searchGroupsMetadata);
        return adHashMap;
    }

    private static HashMap<LdapQueryType, LdapQueryMetadata> setIPAMap() {
        HashMap<LdapQueryType, LdapQueryMetadata> ipaHashMap = new HashMap<LdapQueryType, LdapQueryMetadata>();
        ipaHashMap.put(LdapQueryType.getGroupByDN, new LdapQueryMetadataImpl(
                        "(cn=*)",
                        "%1$s",
                        new IPAGroupContextMapper(),
                        SearchControls.OBJECT_SCOPE,
                        IPAGroupContextMapper.GROUP_ATTRIBUTE_FILTER,
                        new SimpleLdapQueryExecutionFormatter(),
                        new IPALdapGuidEncoder()));
        ipaHashMap.put(LdapQueryType.getGroupByGuid, new LdapQueryMetadataImpl(
                        "(ipaUniqueID=%1$s)",
                        "",
                        new IPAGroupContextMapper(),
                        SearchControls.SUBTREE_SCOPE,
                        IPAGroupContextMapper.GROUP_ATTRIBUTE_FILTER,
                        new SimpleLdapQueryExecutionFormatter(),
                        new IPALdapGuidEncoder()));
        ipaHashMap.put(LdapQueryType.getUserByGuid, new LdapQueryMetadataImpl(
                        "(ipaUniqueID=%1$s)",
                        "",
                        new IPAUserContextMapper(),
                        SearchControls.SUBTREE_SCOPE,
                        IPAUserContextMapper.USERS_ATTRIBUTE_FILTER,
                        new SimpleLdapQueryExecutionFormatter(),
                        new IPALdapGuidEncoder()));
        ipaHashMap.put(LdapQueryType.getGroupByName, new LdapQueryMetadataImpl(
                        "(&(objectClass=ipaUserGroup)(cn=%1$s))",
                        "",
                        new IPAGroupContextMapper(),
                        SearchControls.SUBTREE_SCOPE,
                        IPAGroupContextMapper.GROUP_ATTRIBUTE_FILTER,
                        new SimpleLdapQueryExecutionFormatter(),
                        new IPALdapGuidEncoder()));
        ipaHashMap.put(LdapQueryType.getUserByPrincipalName, new LdapQueryMetadataImpl(
                        "(&(objectClass=krbPrincipalAux)(krbPrincipalName=%1$s))",
                        "",
                        new IPAUserContextMapper(),
                        SearchControls.SUBTREE_SCOPE,
                        IPAUserContextMapper.USERS_ATTRIBUTE_FILTER,
                        new SimpleLdapQueryExecutionFormatter(),
                        new IPALdapGuidEncoder()));
        ipaHashMap.put(LdapQueryType.getUserByName, new LdapQueryMetadataImpl(
                        "(&(objectClass=posixAccount)(objectClass=krbPrincipalAux)(uid=%1$s))",
                        "",
                        new IPAUserContextMapper(),
                        SearchControls.SUBTREE_SCOPE,
                        IPAUserContextMapper.USERS_ATTRIBUTE_FILTER,
                        new SimpleLdapQueryExecutionFormatter(),
                        new IPALdapGuidEncoder()));
        ipaHashMap.put(LdapQueryType.rootDSE, new LdapQueryMetadataImpl(
                        "(objectClass=*)",
                        "",
                        new IPARootDSEContextMapper(),
                        SearchControls.OBJECT_SCOPE,
                        IPARootDSEContextMapper.ROOTDSE_ATTRIBUTE_FILTER,
                        new SimpleLdapQueryExecutionFormatter(),
                        new IPALdapGuidEncoder()));
        ipaHashMap.put(LdapQueryType.getGroupsByGroupNames, new LdapQueryMetadataImpl(
                        "(&(objectClass=ipaUserGroup)(cn=%1$s))",
                        "",
                        new IPAGroupContextMapper(),
                        SearchControls.SUBTREE_SCOPE,
                        IPAGroupContextMapper.GROUP_ATTRIBUTE_FILTER,
                        new MultipleLdapQueryExecutionFormatter("(|", ")"),
                        new IPALdapGuidEncoder()));
        ipaHashMap.put(LdapQueryType.getUsersByUserGuids, new LdapQueryMetadataImpl(
                        "(ipaUniqueID=%1$s)",
                        "",
                        new IPAUserContextMapper(),
                        SearchControls.SUBTREE_SCOPE,
                        IPAUserContextMapper.USERS_ATTRIBUTE_FILTER,
                        new MultipleLdapQueryExecutionFormatter("(|", ")"),
                        new IPALdapGuidEncoder()));
        LdapQueryMetadataImpl ipaSearchUsersMetadata = new LdapQueryMetadataImpl(
                        "this string is replaced by user input meta-query",
                        "",
                        new IPAUserContextMapper(),
                        SearchControls.SUBTREE_SCOPE,
                        IPAUserContextMapper.USERS_ATTRIBUTE_FILTER,
                        new SearchQueryFotmatter(ipaSearchSyntaxMap),
                        new IPALdapGuidEncoder());
        ipaHashMap.put(LdapQueryType.searchUsers, ipaSearchUsersMetadata);

        LdapQueryMetadataImpl ipaSearchGroupsMetadata = new LdapQueryMetadataImpl(
                        "this string is replaced by user input meta-query",
                        "",
                        new IPAGroupContextMapper(),
                        SearchControls.SUBTREE_SCOPE,
                        IPAGroupContextMapper.GROUP_ATTRIBUTE_FILTER,
                        new SearchQueryFotmatter(ipaSearchSyntaxMap),
                        new IPALdapGuidEncoder());
        ipaHashMap.put(LdapQueryType.searchGroups, ipaSearchGroupsMetadata);

        return ipaHashMap;

    }

    private static HashMap<LdapQueryType, LdapQueryMetadata> setDSMap() {
        HashMap<LdapQueryType, LdapQueryMetadata> dsHashMap = new HashMap<LdapQueryType, LdapQueryMetadata>();
        dsHashMap.put(LdapQueryType.getGroupByDN, new LdapQueryMetadataImpl(
                        "(cn=*)",
                        "%1$s",
                        new RHDSGroupContextMapper(),
                        SearchControls.OBJECT_SCOPE,
                        RHDSGroupContextMapper.GROUP_ATTRIBUTE_FILTER,
                        new RHDSSimpleLdapQueryExecutionFormatter(),
                        new RHDSLdapGuidEncoder()));
        dsHashMap.put(LdapQueryType.getGroupByGuid, new LdapQueryMetadataImpl(
                        "(nsuniqueid=%1$s)",
                        "",
                        new RHDSGroupContextMapper(),
                        SearchControls.SUBTREE_SCOPE,
                        RHDSGroupContextMapper.GROUP_ATTRIBUTE_FILTER,
                        new RHDSSimpleLdapQueryExecutionFormatter(),
                        new RHDSLdapGuidEncoder()));
        dsHashMap.put(LdapQueryType.getUserByGuid, new LdapQueryMetadataImpl(
                        "(nsuniqueid=%1$s)",
                        "",
                        new RHDSUserContextMapper(),
                        SearchControls.SUBTREE_SCOPE,
                        RHDSUserContextMapper.USERS_ATTRIBUTE_FILTER,
                        new RHDSSimpleLdapQueryExecutionFormatter(),
                        new RHDSLdapGuidEncoder()));
        dsHashMap.put(LdapQueryType.getGroupByName, new LdapQueryMetadataImpl(
                        "(&(objectClass=groupofuniquenames)(cn=%1$s))",
                        "",
                        new RHDSGroupContextMapper(),
                        SearchControls.SUBTREE_SCOPE,
                        RHDSGroupContextMapper.GROUP_ATTRIBUTE_FILTER,
                        new RHDSSimpleLdapQueryExecutionFormatter(),
                        new RHDSLdapGuidEncoder()));
        dsHashMap.put(LdapQueryType.getUserByName, new LdapQueryMetadataImpl(
                        "(&(objectClass=person)(uid=%1$s))",
                        "",
                        new RHDSUserContextMapper(),
                        SearchControls.SUBTREE_SCOPE,
                        RHDSUserContextMapper.USERS_ATTRIBUTE_FILTER,
                        new RHDSSimpleLdapQueryExecutionFormatter(),
                        new RHDSLdapGuidEncoder()));
        // In RHDS there is no UPN, so we do the same query as getUserByName, using a formatter that will adjust the filter
        // to contain the user name instead of the UPN
        dsHashMap.put(LdapQueryType.getUserByPrincipalName, new LdapQueryMetadataImpl(
                        "(&(objectClass=person)(uid=%1$s))",
                        "",
                        new RHDSUserContextMapper(),
                        SearchControls.SUBTREE_SCOPE,
                        RHDSUserContextMapper.USERS_ATTRIBUTE_FILTER,
                        new RHDSUPNLdapQueryExecutionFormatter(),
                        new RHDSLdapGuidEncoder()));
        dsHashMap.put(LdapQueryType.rootDSE, new LdapQueryMetadataImpl(
                        "(objectClass=*)",
                        "",
                        new RHDSRootDSEContextMapper(),
                        SearchControls.OBJECT_SCOPE,
                        RHDSRootDSEContextMapper.ROOTDSE_ATTRIBUTE_FILTER,
                        new RHDSSimpleLdapQueryExecutionFormatter(),
                        new RHDSLdapGuidEncoder()));
        dsHashMap.put(LdapQueryType.getGroupsByGroupNames, new LdapQueryMetadataImpl(
                        "(&(objectClass=groupofuniquenames)(cn=%1$s))",
                        "",
                        new RHDSGroupContextMapper(),
                        SearchControls.SUBTREE_SCOPE,
                        RHDSGroupContextMapper.GROUP_ATTRIBUTE_FILTER,
                        new RHDSMultipleLdapQueryExecutionFormatter("(|", ")"),
                        new RHDSLdapGuidEncoder()));
        dsHashMap.put(LdapQueryType.getUsersByUserGuids, new LdapQueryMetadataImpl(
                        "(nsuniqueid=%1$s)",
                        "",
                        new RHDSUserContextMapper(),
                        SearchControls.SUBTREE_SCOPE,
                        RHDSUserContextMapper.USERS_ATTRIBUTE_FILTER,
                        new RHDSMultipleLdapQueryExecutionFormatter("(|", ")"),
                        new RHDSLdapGuidEncoder()));
        LdapQueryMetadataImpl rhdsSearchUsersMetadata = new LdapQueryMetadataImpl(
                        "this string is replaced by user input meta-query",
                        "",
                        new RHDSUserContextMapper(),
                        SearchControls.SUBTREE_SCOPE,
                        RHDSUserContextMapper.USERS_ATTRIBUTE_FILTER,
                        new SearchQueryFotmatter(dsSearchSyntaxMap),
                        new RHDSLdapGuidEncoder());
        dsHashMap.put(LdapQueryType.searchUsers, rhdsSearchUsersMetadata);

        LdapQueryMetadataImpl rhdsSearchGroupsMetadata = new LdapQueryMetadataImpl(
                        "this string is replaced by user input meta-query",
                        "",
                        new RHDSGroupContextMapper(),
                        SearchControls.SUBTREE_SCOPE,
                        RHDSGroupContextMapper.GROUP_ATTRIBUTE_FILTER,
                        new SearchQueryFotmatter(dsSearchSyntaxMap),
                        new RHDSLdapGuidEncoder());
        dsHashMap.put(LdapQueryType.searchGroups, rhdsSearchGroupsMetadata);

        return dsHashMap;

    }

    private static HashMap<LdapQueryType, LdapQueryMetadata> setITDSMap() {
        HashMap<LdapQueryType, LdapQueryMetadata> itdsHashMap = new HashMap<LdapQueryType, LdapQueryMetadata>();
        itdsHashMap.put(LdapQueryType.getGroupByDN, new LdapQueryMetadataImpl(
                        "(cn=*)",
                        "%1$s",
                        new ITDSGroupContextMapper(),
                        SearchControls.OBJECT_SCOPE,
                        ITDSGroupContextMapper.GROUP_ATTRIBUTE_FILTER,
                        new SimpleLdapQueryExecutionFormatter(),
                        new ITDSLdapGuidEncoder()));
        itdsHashMap.put(LdapQueryType.getGroupByGuid, new LdapQueryMetadataImpl(
                        "(uniqueIdentifier=%1$s)",
                        "",
                        new ITDSGroupContextMapper(),
                        SearchControls.SUBTREE_SCOPE,
                        ITDSGroupContextMapper.GROUP_ATTRIBUTE_FILTER,
                        new SimpleLdapQueryExecutionFormatter(),
                        new ITDSLdapGuidEncoder()));
        itdsHashMap.put(LdapQueryType.getUserByGuid, new LdapQueryMetadataImpl(
                        "(uniqueIdentifier=%1$s)",
                        "",
                        new ITDSUserContextMapper(),
                        SearchControls.SUBTREE_SCOPE,
                        ITDSUserContextMapper.USERS_ATTRIBUTE_FILTER,
                        new SimpleLdapQueryExecutionFormatter(),
                        new ITDSLdapGuidEncoder()));
        itdsHashMap.put(LdapQueryType.getGroupByName, new LdapQueryMetadataImpl(
                        "(&(objectClass=groupofuniquenames)(cn=%1$s))",
                        "",
                        new ITDSGroupContextMapper(),
                        SearchControls.SUBTREE_SCOPE,
                        ITDSGroupContextMapper.GROUP_ATTRIBUTE_FILTER,
                        new SimpleLdapQueryExecutionFormatter(),
                        new ITDSLdapGuidEncoder()));
        itdsHashMap.put(LdapQueryType.getUserByPrincipalName, new LdapQueryMetadataImpl(
                        "(principalName=%1$s))",
                        "",
                        new ITDSUserContextMapper(),
                        SearchControls.SUBTREE_SCOPE,
                        ITDSUserContextMapper.USERS_ATTRIBUTE_FILTER,
                        new SimpleLdapQueryExecutionFormatter(),
                        new ITDSLdapGuidEncoder()));
        itdsHashMap.put(LdapQueryType.getUserByName, new LdapQueryMetadataImpl(
                        "(uid=%1$s))",
                        "",
                        new ITDSUserContextMapper(),
                        SearchControls.SUBTREE_SCOPE,
                        ITDSUserContextMapper.USERS_ATTRIBUTE_FILTER,
                        new SimpleLdapQueryExecutionFormatter(),
                        new ITDSLdapGuidEncoder()));
        itdsHashMap.put(LdapQueryType.rootDSE, new LdapQueryMetadataImpl(
                        "(objectClass=*)",
                        "",
                        new ITDSRootDSEContextMapper(),
                        SearchControls.OBJECT_SCOPE,
                        ITDSRootDSEContextMapper.ROOTDSE_ATTRIBUTE_FILTER,
                        new SimpleLdapQueryExecutionFormatter(),
                        new ITDSLdapGuidEncoder()));
        itdsHashMap.put(LdapQueryType.getGroupsByGroupNames, new LdapQueryMetadataImpl(
                        "(&(objectClass=groupofuniquenames)(cn=%1$s))",
                        "",
                        new ITDSGroupContextMapper(),
                        SearchControls.SUBTREE_SCOPE,
                        ITDSGroupContextMapper.GROUP_ATTRIBUTE_FILTER,
                        new MultipleLdapQueryExecutionFormatter("(|", ")"),
                        new ITDSLdapGuidEncoder()));
        itdsHashMap.put(LdapQueryType.getUsersByUserGuids, new LdapQueryMetadataImpl(
                        "(uid=%1$s)",
                        "",
                        new ITDSUserContextMapper(),
                        SearchControls.SUBTREE_SCOPE,
                        ITDSUserContextMapper.USERS_ATTRIBUTE_FILTER,
                        new MultipleLdapQueryExecutionFormatter("(|", ")"),
                        new ITDSLdapGuidEncoder()));
        LdapQueryMetadataImpl itdsSearchUsersMetadata = new LdapQueryMetadataImpl(
                        "this string is replaced by user input meta-query",
                        "",
                        new ITDSUserContextMapper(),
                        SearchControls.SUBTREE_SCOPE,
                        ITDSUserContextMapper.USERS_ATTRIBUTE_FILTER,
                        new SearchQueryFotmatter(itdsSearchSyntaxMap),
                        new ITDSLdapGuidEncoder());
        itdsHashMap.put(LdapQueryType.searchUsers, itdsSearchUsersMetadata);

        LdapQueryMetadataImpl itdsSearchGroupsMetadata = new LdapQueryMetadataImpl(
                        "this string is replaced by user input meta-query",
                        "",
                        new ITDSGroupContextMapper(),
                        SearchControls.SUBTREE_SCOPE,
                        ITDSGroupContextMapper.GROUP_ATTRIBUTE_FILTER,
                        new SearchQueryFotmatter(itdsSearchSyntaxMap),
                        new ITDSLdapGuidEncoder());
        itdsHashMap.put(LdapQueryType.searchGroups, itdsSearchGroupsMetadata);

        return itdsHashMap;

    }

    private static HashMap<LdapQueryType, LdapQueryMetadata> setOpenLdapMap() {
        HashMap<LdapQueryType, LdapQueryMetadata> openLdapHashMap = new HashMap<LdapQueryType, LdapQueryMetadata>();
        openLdapHashMap.put(LdapQueryType.getGroupByDN, new LdapQueryMetadataImpl(
                        "(cn=*)",
                        "%1$s",
                        new OpenLdapGroupContextMapper(),
                        SearchControls.OBJECT_SCOPE,
                        OpenLdapGroupContextMapper.GROUP_ATTRIBUTE_FILTER,
                        new SimpleLdapQueryExecutionFormatter(),
                        new DefaultGuidEncoder()));
        openLdapHashMap.put(LdapQueryType.getGroupByGuid, new LdapQueryMetadataImpl(
                        "(entryUUID=%1$s)",
                        "",
                        new OpenLdapGroupContextMapper(),
                        SearchControls.SUBTREE_SCOPE,
                        OpenLdapGroupContextMapper.GROUP_ATTRIBUTE_FILTER,
                        new SimpleLdapQueryExecutionFormatter(),
                        new DefaultGuidEncoder()));
        openLdapHashMap.put(LdapQueryType.getUserByGuid, new LdapQueryMetadataImpl(
                        "(entryUUID=%1$s)",
                        "",
                        new OpenLdapUserContextMapper(),
                        SearchControls.SUBTREE_SCOPE,
                        OpenLdapUserContextMapper.USERS_ATTRIBUTE_FILTER,
                        new SimpleLdapQueryExecutionFormatter(),
                        new DefaultGuidEncoder()));
        openLdapHashMap.put(LdapQueryType.getGroupByName, new LdapQueryMetadataImpl(
                        "(&(objectClass=groupOfNames)(cn=%1$s))",
                        "",
                        new OpenLdapGroupContextMapper(),
                        SearchControls.SUBTREE_SCOPE,
                        OpenLdapGroupContextMapper.GROUP_ATTRIBUTE_FILTER,
                        new SimpleLdapQueryExecutionFormatter(),
                        new DefaultGuidEncoder()));
        openLdapHashMap.put(LdapQueryType.getUserByPrincipalName, new LdapQueryMetadataImpl(
                        "(uid=%1$s)",
                        "",
                        new OpenLdapUserContextMapper(),
                        SearchControls.SUBTREE_SCOPE,
                        OpenLdapUserContextMapper.USERS_ATTRIBUTE_FILTER,
                        new SimpleLdapQueryExecutionFormatter(),
                        new DefaultGuidEncoder()));
        openLdapHashMap.put(LdapQueryType.getUserByName, new LdapQueryMetadataImpl(
                        "(uid=%1$s)",
                        "",
                        new OpenLdapUserContextMapper(),
                        SearchControls.SUBTREE_SCOPE,
                        OpenLdapUserContextMapper.USERS_ATTRIBUTE_FILTER,
                        new SimpleLdapQueryExecutionFormatter(),
                        new DefaultGuidEncoder()));
        openLdapHashMap.put(LdapQueryType.rootDSE, new LdapQueryMetadataImpl(
                        "(objectClass=*)",
                        "",
                        new DefaultRootDSEContextMapper(),
                        SearchControls.OBJECT_SCOPE,
                        DefaultRootDSEContextMapper.ROOTDSE_ATTRIBUTE_FILTER,
                        new SimpleLdapQueryExecutionFormatter(),
                        new DefaultGuidEncoder()));
        openLdapHashMap.put(LdapQueryType.getGroupsByGroupNames, new LdapQueryMetadataImpl(
                        "(&(objectClass=groupOfNames)(cn=%1$s))",
                        "",
                        new OpenLdapGroupContextMapper(),
                        SearchControls.SUBTREE_SCOPE,
                        OpenLdapGroupContextMapper.GROUP_ATTRIBUTE_FILTER,
                        new MultipleLdapQueryExecutionFormatter("(|", ")"),
                        new DefaultGuidEncoder()));
        openLdapHashMap.put(LdapQueryType.getUsersByUserGuids, new LdapQueryMetadataImpl(
                        "(uid=%1$s)",
                        "",
                        new OpenLdapUserContextMapper(),
                        SearchControls.SUBTREE_SCOPE,
                        OpenLdapUserContextMapper.USERS_ATTRIBUTE_FILTER,
                        new MultipleLdapQueryExecutionFormatter("(|", ")"),
                        new DefaultGuidEncoder()));
        LdapQueryMetadataImpl OpenLdapSearchUsersMetadata = new LdapQueryMetadataImpl(
                        "this string is replaced by user input meta-query",
                        "",
                        new OpenLdapUserContextMapper(),
                        SearchControls.SUBTREE_SCOPE,
                        OpenLdapUserContextMapper.USERS_ATTRIBUTE_FILTER,
                        new SearchQueryFotmatter(openLdapSearchSyntaxMap),
                        new DefaultGuidEncoder());
        openLdapHashMap.put(LdapQueryType.searchUsers, OpenLdapSearchUsersMetadata);

        LdapQueryMetadataImpl OpenLdapSearchGroupsMetadata = new LdapQueryMetadataImpl(
                        "this string is replaced by user input meta-query",
                        "",
                        new OpenLdapGroupContextMapper(),
                        SearchControls.SUBTREE_SCOPE,
                        OpenLdapGroupContextMapper.GROUP_ATTRIBUTE_FILTER,
                        new SearchQueryFotmatter(openLdapSearchSyntaxMap),
                        new DefaultGuidEncoder());
        openLdapHashMap.put(LdapQueryType.searchGroups, OpenLdapSearchGroupsMetadata);

        return openLdapHashMap;

    }

    private static void prepareQueryFormatters() {
        activeDirectorySearchSyntaxMap = new EnumMap<SearchLangageLDAPTokens, String>(SearchLangageLDAPTokens.class);
        activeDirectorySearchSyntaxMap.put(SearchLangageLDAPTokens.$GIVENNAME, "givenname");
        activeDirectorySearchSyntaxMap.put(SearchLangageLDAPTokens.$USER_ACCOUNT_TYPE, "sAMAccountType=805306368");
        activeDirectorySearchSyntaxMap.put(SearchLangageLDAPTokens.$PRINCIPAL_NAME, "userPrincipalName");
        activeDirectorySearchSyntaxMap.put(SearchLangageLDAPTokens.$LDAP_GROUP_CATEGORY, "ObjectCategory=Group");
        activeDirectorySearchSyntaxMap.put(SearchLangageLDAPTokens.$CN, "name");
        activeDirectorySearchSyntaxMap.put(SearchLangageLDAPTokens.$USER_ACCOUNT_NAME, "samaccountname");

        ipaSearchSyntaxMap = new EnumMap<SearchLangageLDAPTokens, String>(SearchLangageLDAPTokens.class);
        ipaSearchSyntaxMap.put(SearchLangageLDAPTokens.$GIVENNAME, "givenname");
        ipaSearchSyntaxMap.put(SearchLangageLDAPTokens.$USER_ACCOUNT_TYPE,
                "&(objectClass=posixAccount)(objectClass=krbPrincipalAux)");
        ipaSearchSyntaxMap.put(SearchLangageLDAPTokens.$PRINCIPAL_NAME, "krbPrincipalName");
        ipaSearchSyntaxMap.put(SearchLangageLDAPTokens.$LDAP_GROUP_CATEGORY, "objectClass=ipaUserGroup");
        ipaSearchSyntaxMap.put(SearchLangageLDAPTokens.$CN, "cn");
        ipaSearchSyntaxMap.put(SearchLangageLDAPTokens.$USER_ACCOUNT_NAME, "uid");

        dsSearchSyntaxMap = new EnumMap<SearchLangageLDAPTokens, String>(SearchLangageLDAPTokens.class);
        dsSearchSyntaxMap.put(SearchLangageLDAPTokens.$GIVENNAME, "givenname");
        dsSearchSyntaxMap.put(SearchLangageLDAPTokens.$USER_ACCOUNT_TYPE, "&(objectClass=person)");
        // We put here a duplicate. Need to solve it in another way.
        dsSearchSyntaxMap.put(SearchLangageLDAPTokens.$PRINCIPAL_NAME, "uid");
        dsSearchSyntaxMap.put(SearchLangageLDAPTokens.$LDAP_GROUP_CATEGORY, "objectClass=groupOfUniqueNames");
        dsSearchSyntaxMap.put(SearchLangageLDAPTokens.$CN, "cn");
        dsSearchSyntaxMap.put(SearchLangageLDAPTokens.$USER_ACCOUNT_NAME, "uid");

        itdsSearchSyntaxMap = new EnumMap<SearchLangageLDAPTokens, String>(SearchLangageLDAPTokens.class);
        itdsSearchSyntaxMap.put(SearchLangageLDAPTokens.$GIVENNAME, "givenname");
        itdsSearchSyntaxMap.put(SearchLangageLDAPTokens.$USER_ACCOUNT_TYPE, "&(objectClass=person)");
        itdsSearchSyntaxMap.put(SearchLangageLDAPTokens.$PRINCIPAL_NAME, "sn");
        itdsSearchSyntaxMap.put(SearchLangageLDAPTokens.$LDAP_GROUP_CATEGORY, "objectClass=groupOfUniqueNames");
        itdsSearchSyntaxMap.put(SearchLangageLDAPTokens.$CN, "cn");
        itdsSearchSyntaxMap.put(SearchLangageLDAPTokens.$USER_ACCOUNT_NAME, "uid");

        openLdapSearchSyntaxMap = new EnumMap<SearchLangageLDAPTokens, String>(SearchLangageLDAPTokens.class);
        openLdapSearchSyntaxMap.put(SearchLangageLDAPTokens.$GIVENNAME, "givenname");
        openLdapSearchSyntaxMap.put(SearchLangageLDAPTokens.$USER_ACCOUNT_TYPE, "&(objectClass=person)");
        openLdapSearchSyntaxMap.put(SearchLangageLDAPTokens.$PRINCIPAL_NAME, "uid");
        openLdapSearchSyntaxMap.put(SearchLangageLDAPTokens.$LDAP_GROUP_CATEGORY, "objectClass=groupOfNames");
        openLdapSearchSyntaxMap.put(SearchLangageLDAPTokens.$CN, "cn");
        openLdapSearchSyntaxMap.put(SearchLangageLDAPTokens.$USER_ACCOUNT_NAME, "uid");
    }
}
