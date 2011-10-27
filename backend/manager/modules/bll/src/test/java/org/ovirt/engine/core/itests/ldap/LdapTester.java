package org.ovirt.engine.core.itests.ldap;

import static junit.framework.Assert.assertTrue;
import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.createNiceMock;
import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.replayAll;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ovirt.engine.core.bll.SearchQuery;
import org.ovirt.engine.core.bll.adbroker.ADRootDSEAttributes;
import org.ovirt.engine.core.bll.adbroker.Domain;
import org.ovirt.engine.core.bll.adbroker.GetRootDSE;
import org.ovirt.engine.core.bll.adbroker.GroupSearchResult;
import org.ovirt.engine.core.bll.adbroker.LDAPSecurityAuthentication;
import org.ovirt.engine.core.bll.adbroker.LdapAuthenticateUserCommand;
import org.ovirt.engine.core.bll.adbroker.LdapBrokerBase;
import org.ovirt.engine.core.bll.adbroker.LdapBrokerImpl;
import org.ovirt.engine.core.bll.adbroker.LdapFactory;
import org.ovirt.engine.core.bll.adbroker.LdapGetAdGroupByGroupIdCommand;
import org.ovirt.engine.core.bll.adbroker.LdapGetAdUserByUserIdCommand;
import org.ovirt.engine.core.bll.adbroker.LdapGetAdUserByUserIdListCommand;
import org.ovirt.engine.core.bll.adbroker.LdapGetAdUserByUserNameCommand;
import org.ovirt.engine.core.bll.adbroker.LdapProviderType;
import org.ovirt.engine.core.bll.adbroker.LdapQueryData;
import org.ovirt.engine.core.bll.adbroker.LdapQueryDataImpl;
import org.ovirt.engine.core.bll.adbroker.LdapQueryMetadata;
import org.ovirt.engine.core.bll.adbroker.LdapQueryMetadataFactory;
import org.ovirt.engine.core.bll.adbroker.LdapQueryMetadataFactoryImpl;
import org.ovirt.engine.core.bll.adbroker.LdapQueryType;
import org.ovirt.engine.core.bll.adbroker.LdapReturnValueBase;
import org.ovirt.engine.core.bll.adbroker.LdapSearchByIdListParameters;
import org.ovirt.engine.core.bll.adbroker.LdapSearchByIdParameters;
import org.ovirt.engine.core.bll.adbroker.LdapSearchByQueryParameters;
import org.ovirt.engine.core.bll.adbroker.LdapSearchByUserNameParameters;
import org.ovirt.engine.core.bll.adbroker.LdapSearchGroupsByQueryCommand;
import org.ovirt.engine.core.bll.adbroker.LdapSearchUserByQueryCommand;
import org.ovirt.engine.core.bll.adbroker.LdapUserPasswordBaseParameters;
import org.ovirt.engine.core.bll.adbroker.RootDSEAttributes;
import org.ovirt.engine.core.bll.adbroker.UserAuthenticationResult;
import org.ovirt.engine.core.bll.adbroker.UsersDomainsCacheManager;
import org.ovirt.engine.core.bll.adbroker.UsersDomainsCacheManagerService;
import org.ovirt.engine.core.common.businessentities.AdUser;
import org.ovirt.engine.core.common.businessentities.ad_groups;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.config.IConfigUtilsInterface;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.SearchParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbEngineDialect;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.AdGroupDAO;
import org.ovirt.engine.core.utils.GuidUtils;
import org.ovirt.engine.core.utils.ejb.BeanProxyType;
import org.ovirt.engine.core.utils.ejb.BeanType;
import org.ovirt.engine.core.utils.ejb.EJBUtilsStrategy;
import org.ovirt.engine.core.utils.ejb.EjbUtils;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ DbFacade.class, LdapBrokerBase.class, LdapFactory.class })
public class LdapTester {

    private static Guid ipaGuidExample;
    private static Guid adGuidExample;
    private static Guid adGuidExample2;
    private static Guid adGroupGuidExample;
    private static Guid ipaGroupGuidExample;
    private final LdapQueryMetadataFactory queryMetadataFactory = LdapQueryMetadataFactoryImpl.getInstance();
    private static LdapTestsSetup ldapTestsSetup;

    @BeforeClass
    public static void setUp() throws Exception {
        // If this will fail then the tests won't run
        ldapTestsSetup = new LdapTestsSetup();
    }

    private static String dnToDomain(String dn) {

        String returnValue = dn.replaceAll("dc=", "").replaceAll(",", ".");

        return returnValue;
    }

    private static String getUserNameFromUserDn(String userDn) {
        return userDn.split(",", 2)[0].split("=")[1];
    }

    private String encodeGuid(Guid guid) {
        byte[] ba = GuidUtils.ToByteArray(guid.getUuid());

        // AD guid is stored in reversed order than MS-SQL guid -
        // Since it is important for us to work with GUIDs which are MS-SQL
        // aligned,
        // for each GUID -before using with AD we will change its byte order to
        // support AD
        Guid adGuid = new Guid(ba, false);
        ba = GuidUtils.ToByteArray(adGuid.getUuid());
        StringBuilder sb = new StringBuilder();
        for (int idx = 0; idx < ba.length; idx++) {
            sb.append("\\" + String.format("%02X", ba[idx]));
        }

        return sb.toString();
    }

    /**
     * setup the ldap schemas
     *
     * @throws Exception
     */
    public void setupData() throws Exception {
        ldapTestsSetup = new LdapTestsSetup();
        ldapTestsSetup.setup();

        ldapTestsSetup.populateUsersAndGroups();
    }

    @Test
    public void testSearchQuery() throws Exception {
        try {
            setupData();
            setMockups(LdapProviderType.activeDirectory);
            // search users
            SearchParameters parameters = new SearchParameters("AdUser: name=gandalf", SearchType.AdUser);
            SearchQuery<SearchParameters> searchCmd = new SearchQuery<SearchParameters>(parameters);
            searchCmd.ExecuteWithReturnValue();
            AdUser gandalf = ((List<AdUser>) searchCmd.getQueryReturnValue().getReturnValue()).get(0);
            Assert.assertTrue(gandalf != null
                    && gandalf.getSurName().equals("the gray"));
            // search groups
            parameters = new SearchParameters("AdGroup: name=others", SearchType.AdGroup);
            searchCmd = new SearchQuery<SearchParameters>(parameters);
            searchCmd.ExecuteWithReturnValue();
            List<ad_groups> groups = (List<ad_groups>) searchCmd.getQueryReturnValue().getReturnValue();
            assertTrue(groups.size() > 0);
            ad_groups groupOthers = groups.get(0);
            assertTrue(groupOthers.getname().contains("others"));
        } catch (Exception e) {
            System.err.println(e);
        } finally {
            zcleanup();
        }
    }

    @Test
    public void testGetRootDSE() throws Exception {
        setupData();
        GetRootDSE getRootDSE = new GetRootDSE(new URI(
                ldapTestsSetup.getAdLdapContext().getUrls()[0]));
        Attributes results =
                getRootDSE.getDomainAttributes(LdapProviderType.activeDirectory,
                        dnToDomain(ldapTestsSetup.getAdLdapContext()
                        .getBaseLdapPathAsString()));

        assertNotNull(results);
        assertNotNull(results.get(ADRootDSEAttributes.domainControllerFunctionality.name()).get(0));
        assertNotNull(results.get(RootDSEAttributes.domainFunctionality.name()).get(0));
        assertNotNull(results.get(RootDSEAttributes.defaultNamingContext.name()).get(0));

        assertEquals("4",
                results.get(RootDSEAttributes.domainControllerFunctionality.name()).get(0));
        assertEquals("2", results.get(RootDSEAttributes.domainFunctionality.name()).get(0));
        assertEquals(ldapTestsSetup.getAdLdapContext().getBaseLdapPath().toString().toLowerCase(),
                results.get(RootDSEAttributes.defaultNamingContext.name()).get(0).toString()
                        .toLowerCase());
    }

    @Before
    public void setMockups() throws URISyntaxException {
        setMockups(LdapProviderType.activeDirectory);
    }

    @Test
    public void testAdAuthenticateUserCommand() throws Exception {
        setMockups();
        String userName = getUserNameFromUserDn(ldapTestsSetup.getAdLdapContext()
                .getAuthenticationSource().getPrincipal());
        LdapUserPasswordBaseParameters adParameters = new LdapUserPasswordBaseParameters(
                dnToDomain(ldapTestsSetup.getAdLdapContext().getBaseLdapPathAsString()), userName,
                ldapTestsSetup.getAdLdapContext().getAuthenticationSource().getCredentials());
        LdapAuthenticateUserCommand command = new LdapAuthenticateUserCommand(
                adParameters);
        LdapReturnValueBase retVal = command.Execute();
        assertNotNull(retVal);
        assertTrue(retVal.getSucceeded());

        UserAuthenticationResult userAuthResult = (UserAuthenticationResult) retVal
                .getReturnValue();
        assertNotNull(userAuthResult);
        AdUser adUser = userAuthResult.getUser();
        assertNotNull(adUser);
        assertEquals(userName, adUser.getUserName());
        assertNotNull(adUser.getGroups().get(
                dnToDomain(ldapTestsSetup.getAdLdapContext().getBaseLdapPathAsString())
                        + "/Builtin/Administrators"));

        // Password problem
        LdapUserPasswordBaseParameters adParametersIllegalPassword = new LdapUserPasswordBaseParameters(
                dnToDomain(ldapTestsSetup.getAdLdapContext().getBaseLdapPathAsString()), userName,
                ldapTestsSetup.getAdLdapContext().getAuthenticationSource().getCredentials()
                        + "aaaa");
        LdapAuthenticateUserCommand commandIllegalPassword = new LdapAuthenticateUserCommand(
                adParametersIllegalPassword);
        LdapReturnValueBase retValIllegalPassword = commandIllegalPassword
                .Execute();
        assertNotNull(retValIllegalPassword);
        assertTrue(!retValIllegalPassword.getSucceeded());

        // username problem
        LdapUserPasswordBaseParameters adParametersIllegalUsername = new LdapUserPasswordBaseParameters(
                dnToDomain(ldapTestsSetup.getAdLdapContext().getBaseLdapPathAsString()), userName
                        + "aa", ldapTestsSetup.getAdLdapContext().getAuthenticationSource()
                        .getCredentials());
        LdapAuthenticateUserCommand commandIllegalUsername = new LdapAuthenticateUserCommand(
                adParametersIllegalUsername);
        LdapReturnValueBase retValIllegalUsername = commandIllegalUsername
                .Execute();
        assertNotNull(retValIllegalUsername);
        assertTrue(!retValIllegalUsername.getSucceeded());

        // domain problem
        LdapUserPasswordBaseParameters adParametersIllegalDomain = new LdapUserPasswordBaseParameters(
                "blabla." + dnToDomain(ldapTestsSetup.getAdLdapContext().getBaseLdapPathAsString()),
                userName, ldapTestsSetup.getAdLdapContext().getAuthenticationSource()
                        .getCredentials());
        LdapAuthenticateUserCommand commandIllegalDomain = new LdapAuthenticateUserCommand(
                adParametersIllegalDomain);
        LdapReturnValueBase retValIllegalDomain = commandIllegalDomain.Execute();
        assertNotNull(retValIllegalDomain);
        assertTrue(!retValIllegalDomain.getSucceeded());
    }

    @Test
    public void testIPAAuthenticateUserCommand() throws Exception {
        setMockups(LdapProviderType.ipa);
        String username =
                getUserNameFromUserDn(ldapTestsSetup.getIpaLdapContext()
                        .getAuthenticationSource()
                        .getPrincipal());
        String password = ldapTestsSetup.getIpaLdapContext().getAuthenticationSource().getCredentials();

        mockCacheManger(dnToDomain(ldapTestsSetup.getIpaLdapContext().getBaseLdapPathAsString()),
                ldapTestsSetup.getIpaLdapContext().getUrls()[0], username, password);
        String userName = getUserNameFromUserDn(ldapTestsSetup.getIpaLdapContext()
                .getAuthenticationSource().getPrincipal());
        LdapUserPasswordBaseParameters ipaParameters = new LdapUserPasswordBaseParameters(
                dnToDomain(ldapTestsSetup.getIpaLdapContext().getBaseLdapPathAsString()), userName,
                ldapTestsSetup.getIpaLdapContext().getAuthenticationSource().getCredentials());
        LdapAuthenticateUserCommand command = new LdapAuthenticateUserCommand(
                ipaParameters);
        LdapReturnValueBase retVal = command.Execute();
        assertNotNull(retVal);
        assertTrue(retVal.getSucceeded());

        UserAuthenticationResult userAuthResult = (UserAuthenticationResult) retVal
                .getReturnValue();
        assertNotNull(userAuthResult);
        AdUser adUser = userAuthResult.getUser();
        assertNotNull(adUser);
        assertEquals(userName, adUser.getUserName());
        assertNotNull(adUser.getGroups().get(
                dnToDomain(ldapTestsSetup.getIpaLdapContext().getBaseLdapPathAsString())
                        + "/accounts/groups/admins"));

        // Password problem
        LdapUserPasswordBaseParameters ipaParametersIllegalPassword = new LdapUserPasswordBaseParameters(
                dnToDomain(ldapTestsSetup.getIpaLdapContext().getBaseLdapPathAsString()), userName,
                ldapTestsSetup.getIpaLdapContext().getAuthenticationSource().getCredentials()
                        + "aaaa");
        LdapAuthenticateUserCommand commandIllegalPassword = new LdapAuthenticateUserCommand(
                ipaParametersIllegalPassword);
        LdapReturnValueBase retValIllegalPassword = commandIllegalPassword
                .Execute();
        assertNotNull(retValIllegalPassword);
        assertTrue(!retValIllegalPassword.getSucceeded());

        // username problem
        LdapUserPasswordBaseParameters ipaParametersIllegalUsername = new LdapUserPasswordBaseParameters(
                dnToDomain(ldapTestsSetup.getIpaLdapContext().getBaseLdapPathAsString()), userName
                        + "aa", ldapTestsSetup.getIpaLdapContext().getAuthenticationSource()
                        .getCredentials());
        LdapAuthenticateUserCommand commandIllegalUsername = new LdapAuthenticateUserCommand(
                ipaParametersIllegalUsername);
        LdapReturnValueBase retValIllegalUsername = commandIllegalUsername
                .Execute();
        assertNotNull(retValIllegalUsername);
        assertTrue(!retValIllegalUsername.getSucceeded());

        // domain problem
        LdapUserPasswordBaseParameters ipaParametersIllegalDomain = new LdapUserPasswordBaseParameters(
                "blabla." + dnToDomain(ldapTestsSetup.getIpaLdapContext().getBaseLdapPathAsString()),
                userName, ldapTestsSetup.getIpaLdapContext().getAuthenticationSource()
                        .getCredentials());
        LdapAuthenticateUserCommand commandIllegalDomain = new LdapAuthenticateUserCommand(
                ipaParametersIllegalDomain);
        LdapReturnValueBase retValIllegalDomain = commandIllegalDomain.Execute();
        assertNotNull(retValIllegalDomain);
        assertTrue(!retValIllegalDomain.getSucceeded());
    }

    public void setMockups(LdapProviderType ldapProviderType) throws URISyntaxException {
        if (ldapTestsSetup != null) {
            String username;
            String password;
            String domain;
            String url;

            if (ldapProviderType.equals(LdapProviderType.activeDirectory)) {
                username =
                        getUserNameFromUserDn(ldapTestsSetup.getAdLdapContext()
                                .getAuthenticationSource()
                                .getPrincipal());
                password = ldapTestsSetup.getAdLdapContext().getAuthenticationSource().getCredentials();
                domain = dnToDomain(ldapTestsSetup.getAdLdapContext().getBaseLdapPathAsString());
                url = ldapTestsSetup.getAdLdapContext().getUrls()[0];
            } else {
                username =
                        getUserNameFromUserDn(ldapTestsSetup.getIpaLdapContext()
                                .getAuthenticationSource()
                                .getPrincipal());
                password = ldapTestsSetup.getIpaLdapContext().getAuthenticationSource().getCredentials();
                domain = dnToDomain(ldapTestsSetup.getIpaLdapContext().getBaseLdapPathAsString());
                url = ldapTestsSetup.getIpaLdapContext().getUrls()[0];
            }

            mockConfig(username, password);
            mockCacheManger(domain, url, username, password);
            mockDbFacade();
            mockStatic(LdapFactory.class);
            LdapBrokerImpl broker = new LdapBrokerImpl();
            expect(LdapFactory.getInstance(domain)).andReturn(broker).anyTimes();
            LdapReturnValueBase mockRetrunVal = createMock(LdapReturnValueBase.class);
            // expect(mockADBrokerBatse.RunAdAction(AdActionType.SearchUserByQuery, (AdSearchByQueryParameters)
            // org.easymock.EasyMock.anyObject())).andReturn(mockRetrunVal).anyTimes();
            replayAll();
            Assert.assertNotNull(LdapFactory.getInstance(domain));
        }
    }

    @Test
    public void testADGetUserByUserNameCommand() throws Exception {
        internalTestGetUserByUserNameCommand(ldapTestsSetup.getAdLdapContext());
    }

    @Test
    public void testIPAGetUserByUserNameCommand() throws Exception {
        setMockups(LdapProviderType.ipa);
        internalTestGetUserByUserNameCommand(ldapTestsSetup.getIpaLdapContext());
    }

    public void internalTestGetUserByUserNameCommand(LdapContextSource ldapCtx) throws Exception {

        Person gandalf = ldapTestsSetup.getUser("userA");
        Group others = ldapTestsSetup.getGroup("groupD");
        String krbPrincipalName = gandalf.getUsername() + "@"
                + gandalf.getDomain();

        LdapSearchByUserNameParameters adParameters = new LdapSearchByUserNameParameters(
                null, dnToDomain(ldapCtx.getBaseLdapPathAsString()),
                gandalf.getUsername());
        LdapGetAdUserByUserNameCommand command = new LdapGetAdUserByUserNameCommand(
                adParameters);
        LdapReturnValueBase retVal = command.Execute();
        assertNotNull(retVal);
        assertTrue(retVal.getSucceeded());
        AdUser adUser = (AdUser) retVal.getReturnValue();
        assertNotNull(adUser);
        assertEquals(krbPrincipalName, adUser.getUserName());
        assertEquals(gandalf.getGivenName(), adUser.getName());
        List<String> memberOf = adUser.getMemberof();
        assertTrue(memberOf.get(0).contains(others.getName()));
        assertEquals(gandalf.getSurName(), adUser.getSurName());

        // no results
        LdapSearchByUserNameParameters adParametersNoResults = new LdapSearchByUserNameParameters(
                null, dnToDomain(ldapCtx.getBaseLdapPathAsString()),
                gandalf.getUsername() + "blabla");
        LdapGetAdUserByUserNameCommand commandNoResults = new LdapGetAdUserByUserNameCommand(
                adParametersNoResults);
        LdapReturnValueBase retValNoResults = commandNoResults.Execute();
        assertNotNull(retValNoResults);
        assertTrue(!retValNoResults.getSucceeded());

        // illegal domain
        LdapSearchByUserNameParameters adParametersIllegalDomain = new LdapSearchByUserNameParameters(
                null, dnToDomain("blabla."
                        + ldapCtx.getBaseLdapPathAsString()),
                gandalf.getUsername());
        LdapGetAdUserByUserNameCommand commandIllegalDomain = new LdapGetAdUserByUserNameCommand(
                adParametersIllegalDomain);
        LdapReturnValueBase retValIllegalDomain = commandIllegalDomain.Execute();
        assertNotNull(retValIllegalDomain);
        assertTrue(!retValIllegalDomain.getSucceeded());
    }

    private static void mockDbFacade() {
        mockStatic(DbFacade.class);
        DbFacade db = createMock(DbFacade.class);
        DbEngineDialect mockDbEngineDialect = createNiceMock(DbEngineDialect.class);
        db.setDbEngineDialect(mockDbEngineDialect);
        expect(mockDbEngineDialect.getPreSearchQueryCommand()).andReturn("");
        expect(DbFacade.getInstance()).andReturn(db).anyTimes();
        expect(db.getDbEngineDialect()).andReturn(mockDbEngineDialect)
                .anyTimes();
        AdGroupDAO dao = createNiceMock(AdGroupDAO.class);
        expect(db.getAdGroupDAO()).andReturn(dao).anyTimes();
        replayAll();
    }

    @Test
    public void testADSearchGroupsByQueryCommand() throws Exception {
        internalTestSearchGroupsByQueryCommand(ldapTestsSetup.getAdLdapContext());
    }

    @Test
    public void testIPASearchGroupsByQueryCommand() throws Exception {
        setMockups(LdapProviderType.ipa);
        internalTestSearchGroupsByQueryCommand(ldapTestsSetup.getIpaLdapContext());
    }

    public void internalTestSearchGroupsByQueryCommand(LdapContextSource ldapCtx) throws Exception {
        Group superstars = ldapTestsSetup.getGroup("groupC");

        LdapQueryData ldapQueryData = new LdapQueryDataImpl();
        ldapQueryData.setFilterParameters(new Object[] { superstars.getName() });
        ldapQueryData.setLdapQueryType(LdapQueryType.getGroupByName);
        ldapQueryData.setDomain(dnToDomain(ldapCtx.getBaseLdapPathAsString()));

        LdapSearchByQueryParameters adParameters = new LdapSearchByQueryParameters(
                dnToDomain(ldapCtx.getBaseLdapPathAsString()), ldapQueryData);
        LdapSearchGroupsByQueryCommand command = new LdapSearchGroupsByQueryCommand(
                adParameters);
        LdapReturnValueBase retVal = command.Execute();
        assertNotNull(retVal);
        assertTrue(retVal.getSucceeded());
        List<ad_groups> groups = (List<ad_groups>) retVal.getReturnValue();

        assertNotNull(groups);
        assertEquals(1, groups.size());

        // illegal domain
        ldapQueryData.setDomain("blabla." + dnToDomain(ldapCtx.getBaseLdapPathAsString()));
        LdapSearchByQueryParameters adParametersIllegalDomain = new LdapSearchByQueryParameters(
                "blabla." + dnToDomain(ldapCtx.getBaseLdapPathAsString()),
                ldapQueryData);
        LdapSearchGroupsByQueryCommand commandIllegalDomain = new LdapSearchGroupsByQueryCommand(
                adParametersIllegalDomain);
        LdapReturnValueBase retValIllegalDomain = commandIllegalDomain.Execute();
        assertNotNull(retValIllegalDomain);
        assertTrue(!retValIllegalDomain.getSucceeded());
        List<ad_groups> groupsIllegalDomain = (List<ad_groups>) retValIllegalDomain
                .getReturnValue();

        assertNull(groupsIllegalDomain);

        // non-existent user
        ldapQueryData.setDomain(dnToDomain(ldapCtx.getBaseLdapPathAsString()));
        ldapQueryData.setFilterParameters(new Object[] { superstars.getName() + "aaaa" });
        LdapSearchByQueryParameters adParametersNonExistent = new LdapSearchByQueryParameters(
                dnToDomain(ldapCtx.getBaseLdapPathAsString()),
                ldapQueryData);
        LdapSearchGroupsByQueryCommand commandNonExistent = new LdapSearchGroupsByQueryCommand(
                adParametersNonExistent);
        LdapReturnValueBase retValNonExistent = commandNonExistent.Execute();
        assertNotNull(retValNonExistent);
        assertTrue(retValNonExistent.getSucceeded());
        List<ad_groups> groupsNonExistent = (List<ad_groups>) retValNonExistent
                .getReturnValue();

        assertNotNull(groupsNonExistent);
        assertEquals(0, groupsNonExistent.size());
    }

    @Test
    public void TestADSearchUserByQueryCommand() throws Exception {
        internalTestSearchUserByQueryCommand(ldapTestsSetup.getAdLdapContext());
    }

    @Test
    public void TestIPASearchUserByQueryCommand() throws Exception {
        setMockups(LdapProviderType.ipa);
        internalTestSearchUserByQueryCommand(ldapTestsSetup.getIpaLdapContext());
    }

    public void internalTestSearchUserByQueryCommand(LdapContextSource ldapCtx) throws Exception {
        Person gandalf = ldapTestsSetup.getUser("userA");
        Group others = ldapTestsSetup.getGroup("groupD");
        String krbPrincipalName = gandalf.getUsername() + "@"
                + gandalf.getDomain();

        LdapQueryData ldapQueryData = new LdapQueryDataImpl();
        ldapQueryData.setFilterParameters(new Object[] { gandalf.getUsername() });
        ldapQueryData.setLdapQueryType(LdapQueryType.getUserByName);
        ldapQueryData.setDomain(dnToDomain(ldapCtx.getBaseLdapPathAsString()));

        LdapSearchByQueryParameters adParameters = new LdapSearchByQueryParameters(
                dnToDomain(ldapCtx.getBaseLdapPathAsString()), ldapQueryData);
        LdapSearchUserByQueryCommand command = new LdapSearchUserByQueryCommand(
                adParameters);
        LdapReturnValueBase retVal = command.Execute();
        assertNotNull(retVal);
        assertTrue(retVal.getSucceeded());
        List<AdUser> users = (List<AdUser>) retVal.getReturnValue();

        assertNotNull(users);
        assertEquals(1, users.size());

        AdUser adUser = users.get(0);
        assertNotNull(adUser);
        assertEquals(krbPrincipalName, adUser.getUserName());
        assertEquals(gandalf.getGivenName(), adUser.getName());
        List<String> memberOf = adUser.getMemberof();
        assertTrue(memberOf.get(0).contains(others.getName()));
        assertEquals(gandalf.getSurName(), adUser.getSurName());

        // Illegal domain
        ldapQueryData.setDomain("blabla." + dnToDomain(ldapCtx.getBaseLdapPathAsString()));
        LdapSearchByQueryParameters adParametersIllegalDomain = new LdapSearchByQueryParameters(
                "blabla." + dnToDomain(ldapCtx.getBaseLdapPathAsString()),
                ldapQueryData);
        LdapSearchUserByQueryCommand commandIllegalDomain = new LdapSearchUserByQueryCommand(
                adParametersIllegalDomain);
        LdapReturnValueBase retValIllegalDomain = commandIllegalDomain.Execute();
        assertNotNull(retValIllegalDomain);
        assertTrue(!retValIllegalDomain.getSucceeded());
        List<AdUser> usersIllegalDomain = (List<AdUser>) retValIllegalDomain
                .getReturnValue();
        assertNull(usersIllegalDomain);

        // Non-Existent user
        ldapQueryData.setDomain(dnToDomain(ldapCtx.getBaseLdapPathAsString()));
        ldapQueryData.setFilterParameters(new Object[] { gandalf.getUsername() + "aaaa" });
        LdapSearchByQueryParameters adParametersNonExistentUser = new LdapSearchByQueryParameters(
                dnToDomain(ldapCtx.getBaseLdapPathAsString()),
                ldapQueryData);
        LdapSearchUserByQueryCommand commandNonExistentUser = new LdapSearchUserByQueryCommand(
                adParametersNonExistentUser);
        LdapReturnValueBase retValNonExistentUser = commandNonExistentUser
                .Execute();
        assertNotNull(retValNonExistentUser);
        assertTrue(retValNonExistentUser.getSucceeded());
        List<AdUser> usersNonExistentUser = (List<AdUser>) retValNonExistentUser
                .getReturnValue();
        assertNotNull(usersNonExistentUser);
        assertEquals(0, usersNonExistentUser.size());
    }

    private static void mockCacheManger(String domainName, String ldapUri, String userName, String password)
            throws URISyntaxException {
        EJBUtilsStrategy ejbStrategy = mock(EJBUtilsStrategy.class);
        UsersDomainsCacheManagerService mock = mock(UsersDomainsCacheManagerService.class);

        Domain domain = new Domain(domainName);
        domain.setLdapProviderType(LdapProviderType.general);
        domain.addLDAPServer(new URI(ldapUri));
        domain.setLdapSecurityAuthentication(LDAPSecurityAuthentication.SIMPLE);
        domain.setUserName(userName);
        domain.setPassword(password);
        when(mock.getDomain(domainName)).thenReturn(domain);
        when(
                ejbStrategy.<UsersDomainsCacheManager> findBean(
                        BeanType.USERS_DOMAINS_CACHE, BeanProxyType.LOCAL))
                .thenReturn(mock);
        EjbUtils.setStrategy(ejbStrategy);
    }

    private static void mockConfig(String username, String password) {
        IConfigUtilsInterface mockConfigUtils = mock(IConfigUtilsInterface.class);
        Config.setConfigUtils(mockConfigUtils);
        when(mockConfigUtils.<String> GetValue(ConfigValues.LDAPSecurityAuthentication,
                Config.DefaultConfigurationVersion)).thenReturn("default:SIMPLE");
        when(mockConfigUtils.<String> GetValue(ConfigValues.AdUserName,
                Config.DefaultConfigurationVersion)).thenReturn(username);
        when(mockConfigUtils.<String> GetValue(ConfigValues.AdUserPassword,
                Config.DefaultConfigurationVersion)).thenReturn(password);
        when(mockConfigUtils.<Integer> GetValue(ConfigValues.LDAPQueryTimeout,
                Config.DefaultConfigurationVersion)).thenReturn(90);
        when(mockConfigUtils.<Integer> GetValue(ConfigValues.LdapQueryPageSize,
                Config.DefaultConfigurationVersion)).thenReturn(10000);
        when(mockConfigUtils.<Integer> GetValue(ConfigValues.SearchResultsLimit, Config.DefaultConfigurationVersion)).thenReturn(100);
        when(mockConfigUtils.<String> GetValue(ConfigValues.AuthenticationMethod,
                 Config.DefaultConfigurationVersion)).thenReturn("LDAP");
        when(mockConfigUtils.<String> GetValue(ConfigValues.DomainName,
                Config.DefaultConfigurationVersion)).thenReturn("example.com");
        when(mockConfigUtils.<String> GetValue(ConfigValues.AdminDomain,
                Config.DefaultConfigurationVersion)).thenReturn("internal");

        Config.setConfigUtils(mockConfigUtils);

        when(
                mockConfigUtils.<String> GetValue(
                        ConfigValues.LDAPSecurityAuthentication,
                        Config.DefaultConfigurationVersion)).thenReturn(
                "default:SIMPLE");
        when(
                mockConfigUtils.<String> GetValue(ConfigValues.AdUserName,
                        Config.DefaultConfigurationVersion)).thenReturn(
                username);
        when(
                mockConfigUtils.<String> GetValue(ConfigValues.AdUserPassword,
                        Config.DefaultConfigurationVersion)).thenReturn(
                password);
        when(
                mockConfigUtils.<Integer> GetValue(
                        ConfigValues.LDAPQueryTimeout,
                        Config.DefaultConfigurationVersion)).thenReturn(20);
        when(
                mockConfigUtils.<Integer> GetValue(
                        ConfigValues.LdapQueryPageSize,
                        Config.DefaultConfigurationVersion)).thenReturn(10000);
        when(
                mockConfigUtils.<Integer> GetValue(
                        ConfigValues.SearchResultsLimit,
                        Config.DefaultConfigurationVersion)).thenReturn(100);
        when(
                mockConfigUtils.<Integer> GetValue(
                        ConfigValues.MaxLDAPQueryPartsNumber,
                        Config.DefaultConfigurationVersion)).thenReturn(100);
        when(
                mockConfigUtils.<String> GetValue(
                        ConfigValues.AuthenticationMethod,
                        Config.DefaultConfigurationVersion)).thenReturn("LDAP");
    }

    @Test
    public void testIPAUserQueries() throws Exception {
        Person gandalf = ldapTestsSetup.getUser("userA");
        Group movies = ldapTestsSetup.getGroup("groupE");
        Group others = ldapTestsSetup.getGroup("groupD");

        String krbPrincipalName = gandalf.getUsername() + "@"
                + gandalf.getDomain();

        LdapQueryData queryData = new LdapQueryDataImpl();
        queryData.setLdapQueryType(LdapQueryType.getUserByPrincipalName);
        queryData.setFilterParameters(new Object[] { krbPrincipalName });
        queryData.setDomain(dnToDomain(ldapTestsSetup.getIpaLdapContext().getBaseLdapPathAsString()));
        LdapQueryMetadata queryMetadata = queryMetadataFactory
                .getLdapQueryMetadata(LdapProviderType.ipa,
                        queryData);

        List<AdUser> resultByKpn = ldapTestsSetup.getIpaPersonDao().runFilter(
                queryMetadata.getBaseDN(),
                String.format(queryMetadata.getFilter(), krbPrincipalName));

        assertNotNull(resultByKpn);
        assertEquals(1, resultByKpn.size());

        AdUser user1 = resultByKpn.get(0);
        assertEquals(krbPrincipalName, user1.getUserName());
        assertEquals(gandalf.getGivenName(), user1.getName());
        List<String> memberOf1 = user1.getMemberof();
        assertNotNull(memberOf1);
        assertEquals(2, memberOf1.size());
        assertTrue(memberOf1.get(0).contains(others.getName()));
        assertTrue(memberOf1.get(1).contains(movies.getName()));
        assertEquals(gandalf.getSurName(), user1.getSurName());

        // Setting up for objectGuidtest
        ipaGuidExample = user1.getUserId();

        List<AdUser> resultNonExistent1 = ldapTestsSetup.getIpaPersonDao().runFilter(
                queryMetadata.getBaseDN(),
                String.format(queryMetadata.getFilter(), "non-existent@"
                        + gandalf.getDomain()));

        assertNotNull(resultNonExistent1);
        assertEquals(0, resultNonExistent1.size());

        List<AdUser> resultNonExistent2 = ldapTestsSetup.getIpaPersonDao().runFilter(
                queryMetadata.getBaseDN(),
                String.format(queryMetadata.getFilter(), gandalf.getUsername()
                        + "@example111.com"));

        assertNotNull(resultNonExistent2);
        assertEquals(0, resultNonExistent2.size());

        LdapQueryData queryDataByName = new LdapQueryDataImpl();
        queryDataByName.setLdapQueryType(LdapQueryType.getUserByName);
        queryDataByName.setFilterParameters(new Object[] { gandalf.getUsername() });
        queryDataByName.setDomain(dnToDomain(ldapTestsSetup.getIpaLdapContext().getBaseLdapPathAsString()));
        LdapQueryMetadata queryMetadataByName = queryMetadataFactory
                .getLdapQueryMetadata(LdapProviderType.ipa,
                        queryDataByName);

        List<AdUser> resultByUid = ldapTestsSetup.getIpaPersonDao().runFilter(
                queryMetadataByName.getBaseDN(),
                String.format(queryMetadataByName.getFilter(),
                        gandalf.getUsername()));

        assertNotNull(resultByUid);
        assertEquals(1, resultByUid.size());

        AdUser user2 = resultByUid.get(0);
        assertEquals(krbPrincipalName, user2.getUserName());
        assertEquals(gandalf.getGivenName(), user2.getName());
        List<String> memberOf2 = user2.getMemberof();
        assertNotNull(memberOf2);
        assertEquals(2, memberOf2.size());

        assertTrue(memberOf2.get(0).contains(others.getName()));
        assertTrue(memberOf2.get(1).contains(movies.getName()));
        assertEquals(gandalf.getSurName(), user2.getSurName());

        List<AdUser> resultByUidNonExistent = ldapTestsSetup.getIpaPersonDao().runFilter(
                queryMetadataByName.getBaseDN(),
                String.format(queryMetadataByName.getFilter(), "ggray2"));
        assertNotNull(resultByUidNonExistent);
        assertEquals(0, resultByUidNonExistent.size());
    }

    @Test
    public void testADUserQueries() throws Exception {
        Group others = ldapTestsSetup.getGroup("groupD");
        Person gandalf = ldapTestsSetup.getUser("userA");
        String krbPrincipalName = gandalf.getUsername() + "@"
                + gandalf.getDomain();

        LdapQueryData queryData = new LdapQueryDataImpl();
        queryData.setLdapQueryType(LdapQueryType.getUserByPrincipalName);
        queryData.setFilterParameters(new Object[] { krbPrincipalName });
        queryData.setDomain(dnToDomain(ldapTestsSetup.getAdLdapContext().getBaseLdapPathAsString()));
        LdapQueryMetadata queryMetadata = queryMetadataFactory
                .getLdapQueryMetadata(LdapProviderType.activeDirectory,
                        queryData);

        List<AdUser> resultByUpn = ldapTestsSetup.getAdPersonDao().runFilter(
                queryMetadata.getBaseDN(),
                String.format(queryMetadata.getFilter(), krbPrincipalName));

        assertNotNull(resultByUpn);
        assertEquals(1, resultByUpn.size());

        AdUser user1 = resultByUpn.get(0);
        assertEquals(krbPrincipalName, user1.getUserName());
        assertEquals(gandalf.getGivenName(), user1.getName());
        List<String> memberOf1 = user1.getMemberof();
        assertNotNull(memberOf1);
        assertEquals(1, memberOf1.size());
        assertTrue(memberOf1.get(0).contains(others.getName()));

        assertEquals(gandalf.getSurName(), user1.getSurName());

        // Setting up for objectGuidtest
        adGuidExample = user1.getUserId();

        List<AdUser> resultNonExistent1 = ldapTestsSetup.getAdPersonDao().runFilter(
                queryMetadata.getBaseDN(),
                String.format(queryMetadata.getFilter(), "non-existent@"
                        + gandalf.getDomain()));

        assertNotNull(resultNonExistent1);
        assertEquals(0, resultNonExistent1.size());

        List<AdUser> resultNonExistent2 = ldapTestsSetup.getAdPersonDao().runFilter(
                queryMetadata.getBaseDN(),
                String.format(queryMetadata.getFilter(), gandalf.getUsername()
                        + "@example111.com"));

        assertNotNull(resultNonExistent2);
        assertEquals(0, resultNonExistent2.size());

        LdapQueryData queryDataByName = new LdapQueryDataImpl();
        queryDataByName.setLdapQueryType(LdapQueryType.getUserByName);
        queryDataByName.setFilterParameters(new Object[] { gandalf.getUsername() });
        queryDataByName.setDomain(dnToDomain(ldapTestsSetup.getAdLdapContext().getBaseLdapPathAsString()));
        LdapQueryMetadata queryMetadataByName = queryMetadataFactory
                .getLdapQueryMetadata(LdapProviderType.activeDirectory,
                        queryDataByName);

        List<AdUser> resultByUid = ldapTestsSetup.getAdPersonDao().runFilter(
                queryMetadataByName.getBaseDN(),
                String.format(queryMetadataByName.getFilter(),
                        gandalf.getUsername()));
        assertNotNull(resultByUid);
        assertEquals(1, resultByUid.size());

        AdUser user2 = resultByUid.get(0);
        assertEquals(krbPrincipalName, user2.getUserName());
        assertEquals(gandalf.getGivenName(), user2.getName());
        List<String> memberOf2 = user2.getMemberof();
        assertNotNull(memberOf2);
        assertEquals(1, memberOf2.size());

        assertTrue(memberOf2.get(0).contains("others"));

        assertEquals(gandalf.getSurName(), user2.getSurName());

        List<AdUser> resultByUidNonExistent = ldapTestsSetup.getAdPersonDao().runFilter(
                queryMetadataByName.getBaseDN(),
                String.format(queryMetadataByName.getFilter(), "ggray2"));
        assertNotNull(resultByUidNonExistent);
        assertEquals(0, resultByUidNonExistent.size());

        Person ksoze = ldapTestsSetup.getUser("userB");
        String ksozeKrbPrincipalName = ksoze.getUsername() + "@"
                + ksoze.getDomain();

        List<AdUser> ksozeResultByUpn = ldapTestsSetup.getAdPersonDao()
                .runFilter(queryMetadata.getBaseDN(), String.format(
                        queryMetadata.getFilter(), ksozeKrbPrincipalName));

        assertNotNull(ksozeResultByUpn);
        assertEquals(1, ksozeResultByUpn.size());

        AdUser user3 = ksozeResultByUpn.get(0);
        assertEquals(ksozeKrbPrincipalName, user3.getUserName());
        assertEquals(ksoze.getGivenName(), user3.getName());
        assertEquals(ksoze.getSurName(), user3.getSurName());

        // Setting up for objectGuidtest
        adGuidExample2 = user3.getUserId();
    }

    public void internalTestGroupQueries(LdapProviderType providerType,
            GroupDao groupDao, String dnTest, String domain) throws Exception {
        Group superstars = ldapTestsSetup.getGroup("groupC");

        LdapQueryData queryData = new LdapQueryDataImpl();
        queryData.setLdapQueryType(LdapQueryType.getGroupByName);
        queryData.setFilterParameters(new Object[] { superstars.getName() });
        queryData.setDomain(domain);

        LdapQueryMetadata queryMetadata = queryMetadataFactory
                .getLdapQueryMetadata(providerType, queryData);

        List<GroupSearchResult> resultByCn = groupDao.runFilter(
                queryMetadata.getBaseDN(),
                String.format(queryMetadata.getFilter(), superstars.getName()));

        assertNotNull(resultByCn);
        assertEquals(1, resultByCn.size());

        GroupSearchResult groupResult = resultByCn.get(0);
        // IPA case
        if (!dnTest.isEmpty()) {
            assertEquals("cn=" + superstars.getName() + "," + dnTest + ","
                    + superstars.getDc(), groupResult.getDistinguishedName());
            ipaGroupGuidExample = groupResult.getGuid();
            // AD case
        } else {
            assertEquals(
                    "cn=" + superstars.getName() + "," + superstars.getDc(),
                    groupResult.getDistinguishedName());
            adGroupGuidExample = groupResult.getGuid();
        }

        assertNotNull(groupResult.getGuid());

        List<GroupSearchResult> resultByCnNonExistent = groupDao.runFilter(
                queryMetadata.getBaseDN(),
                String.format(queryMetadata.getFilter(), "superstars1111"));
        assertNotNull(resultByCnNonExistent);
        assertEquals(0, resultByCnNonExistent.size());
    }

    @Test
    public void testIPAGroupQueries() throws Exception {
        internalTestGroupQueries(LdapProviderType.ipa, ldapTestsSetup.getIpaGroupDao(),
                "cn=groups,cn=accounts", dnToDomain(ldapTestsSetup.getIpaLdapContext().getBaseLdapPathAsString()));
    }

    @Test
    public void testADGroupQueries() throws Exception {
        internalTestGroupQueries(LdapProviderType.activeDirectory, ldapTestsSetup.getAdGroupDao(),
                "", dnToDomain(ldapTestsSetup.getAdLdapContext().getBaseLdapPathAsString()));
    }

    public void internalTestbyDnQueries(LdapProviderType providerType,
            GroupDao groupDao, String dnTest, String domain) throws Exception {
        Group superstars = ldapTestsSetup.getGroup("groupC");

        LdapQueryData queryData = new LdapQueryDataImpl();
        queryData.setLdapQueryType(LdapQueryType.getGroupByDN);
        queryData.setFilterParameters(new Object[] { superstars.getName() });
        queryData.setDomain(domain);
        LdapQueryMetadata queryMetadata = queryMetadataFactory
                .getLdapQueryMetadata(providerType, queryData);

        List<GroupSearchResult> resultByDn = groupDao.runFilter(
                String.format(queryMetadata.getBaseDN(), superstars.getName()),
                String.format(queryMetadata.getFilter(), superstars.getName()));

        assertNotNull(resultByDn);
        assertEquals(1, resultByDn.size());

        GroupSearchResult groupResult = resultByDn.get(0);

        if (!dnTest.isEmpty()) {
            assertEquals("cn=" + superstars.getName() + "," + dnTest + ","
                    + superstars.getDc(), groupResult.getDistinguishedName());
        } else {
            assertEquals(
                    "cn=" + superstars.getName() + "," + superstars.getDc(),
                    groupResult.getDistinguishedName());
        }

        assertNotNull(groupResult.getGuid());
    }

    public void testIPAbyDnQueries() throws Exception {
        internalTestbyDnQueries(LdapProviderType.ipa, ldapTestsSetup.getIpaGroupDao(),
                "cn=groups,cn=accounts", dnToDomain(ldapTestsSetup.getIpaLdapContext().getBaseLdapPathAsString()));
    }

    public void testADbyDnQueries() throws Exception {
        internalTestbyDnQueries(LdapProviderType.activeDirectory, ldapTestsSetup.getAdGroupDao(),
                "", dnToDomain(ldapTestsSetup.getAdLdapContext().getBaseLdapPathAsString()));
    }

    @Test
    public void testIPARootDSEQuery() throws Exception {
        LdapContextSource ipaNewContext = new LdapContextSource();
        LdapContextSource context = ldapTestsSetup.getIpaLdapContext();
        String baseDN = context.getBaseLdapPathAsString();
        // No need for basedn, userdn and password for the RootDSE query
        ipaNewContext.setBase("");
        ipaNewContext.setUserDn("");
        ipaNewContext.setPassword("");
        ipaNewContext.afterPropertiesSet();
        LdapTemplate ipaLdapTemplate = new LdapTemplate(ipaNewContext);

        LdapQueryData queryData = new LdapQueryDataImpl();
        queryData.setLdapQueryType(LdapQueryType.rootDSE);

        LdapQueryMetadata queryMetadata = queryMetadataFactory
                .getLdapQueryMetadata(LdapProviderType.ipa, queryData);
        List<String> namingContext = ipaLdapTemplate.search(
                queryMetadata.getBaseDN(), queryMetadata.getFilter(),
                SearchControls.OBJECT_SCOPE, new IPARootDSEContextMapper());
        assertNotNull(namingContext);
        assertEquals(1, namingContext.size());
        assertEquals(baseDN, namingContext.get(0));
    }

    @Test
    public void testADRootDSEQuery() throws Exception {
        LdapContextSource adNewContext = new LdapContextSource();
        LdapContextSource context = ldapTestsSetup.getAdLdapContext();
        String baseDN = context.getBaseLdapPathAsString();
        // No need for basedn, userdn and password for the RootDSE query
        adNewContext.setBase("");
        adNewContext.setUserDn("");
        adNewContext.setPassword("");
        adNewContext.afterPropertiesSet();
        LdapTemplate adLdapTemplate = new LdapTemplate(adNewContext);

        LdapQueryData queryData = new LdapQueryDataImpl();
        queryData.setLdapQueryType(LdapQueryType.rootDSE);

        LdapQueryMetadata queryMetadata = queryMetadataFactory
                .getLdapQueryMetadata(LdapProviderType.activeDirectory,
                        queryData);
        List<Attributes> attributes = adLdapTemplate.search(
                queryMetadata.getBaseDN(), queryMetadata.getFilter(),
                SearchControls.OBJECT_SCOPE, new ADRootDSEContextMapper());
        assertNotNull(attributes);
        assertEquals(1, attributes.size());
        assertEquals(baseDN.toLowerCase(),
                ((String) attributes.get(0).get("defaultNamingContext").get(0))
                        .toLowerCase());
        assertEquals("4",
                (attributes.get(0).get("domainControllerFunctionality").get(0)));
        assertEquals("2", (attributes.get(0).get("domainFunctionality").get(0)));
    }

    @Test
    public void testIPAbyGuidQueries() throws Exception {
        Person gandalf = ldapTestsSetup.getUser("userA");

        LdapQueryData queryData = new LdapQueryDataImpl();
        queryData.setLdapQueryType(LdapQueryType.getUserByGuid);
        queryData.setFilterParameters(new Object[] { ipaGuidExample });
        queryData.setDomain(dnToDomain(ldapTestsSetup.getIpaLdapContext().getBaseLdapPathAsString()));

        LdapQueryMetadata queryMetadata = queryMetadataFactory
                .getLdapQueryMetadata(LdapProviderType.ipa,
                        queryData);

        List<AdUser> resultByGuid = ldapTestsSetup.getIpaPersonDao().runFilter(
                queryMetadata.getBaseDN(),
                String.format(queryMetadata.getFilter(), ipaGuidExample));

        assertNotNull(resultByGuid);
        assertEquals(1, resultByGuid.size());

        AdUser user = resultByGuid.get(0);

        assertEquals(gandalf.getGivenName(), user.getName());

        List<AdUser> resultByGuidIllegal = ldapTestsSetup.getIpaPersonDao().runFilter(
                queryMetadata.getBaseDN(),
                String.format(queryMetadata.getFilter(), "000"));

        assertNotNull(resultByGuidIllegal);
        assertEquals(0, resultByGuidIllegal.size());

        List<AdUser> resultByGuidEmptyGuid = ldapTestsSetup.getIpaPersonDao().runFilter(
                queryMetadata.getBaseDN(), String.format(
                        queryMetadata.getFilter(),
                        "00000000-0000-0000-0000-000000000000"));

        assertNotNull(resultByGuidEmptyGuid);
        assertEquals(0, resultByGuidEmptyGuid.size());

        List<AdUser> resultByGuidNonExistent = ldapTestsSetup.getIpaPersonDao().runFilter(
                queryMetadata.getBaseDN(), String.format(
                        queryMetadata.getFilter(),
                        "12345678-1234-1234-1234-123456789012"));

        assertNotNull(resultByGuidNonExistent);
        assertEquals(0, resultByGuidNonExistent.size());
    }

    @Test
    public void testADbyGuidQueries() throws Exception {
        Person gandalf = ldapTestsSetup.getUser("userA");

        String gid = encodeGuid(adGuidExample);

        LdapQueryData queryData = new LdapQueryDataImpl();
        queryData.setLdapQueryType(LdapQueryType.getUserByGuid);
        queryData.setFilterParameters(new Object[] { gid });
        queryData.setDomain(dnToDomain(ldapTestsSetup.getAdLdapContext().getBaseLdapPathAsString()));
        LdapQueryMetadata queryMetadata = queryMetadataFactory
                .getLdapQueryMetadata(LdapProviderType.activeDirectory,
                        queryData);

        List<AdUser> resultByGuid = ldapTestsSetup.getAdPersonDao().runFilter(
                queryMetadata.getBaseDN(),
                String.format(queryMetadata.getFilter(), gid));

        assertNotNull(resultByGuid);
        assertEquals(1, resultByGuid.size());

        AdUser user = resultByGuid.get(0);

        assertEquals(gandalf.getGivenName(), user.getName());

        List<AdUser> resultByGuidIllegal = ldapTestsSetup.getAdPersonDao().runFilter(
                queryMetadata.getBaseDN(),
                String.format(queryMetadata.getFilter(), "000"));

        assertNotNull(resultByGuidIllegal);
        assertEquals(0, resultByGuidIllegal.size());

        List<AdUser> resultByGuidEmptyGuid = ldapTestsSetup.getAdPersonDao().runFilter(
                queryMetadata.getBaseDN(), String.format(
                        queryMetadata.getFilter(),
                        "00000000-0000-0000-0000-000000000000"));

        assertNotNull(resultByGuidEmptyGuid);
        assertEquals(0, resultByGuidEmptyGuid.size());

        List<AdUser> resultByGuidNonExistent = ldapTestsSetup.getAdPersonDao().runFilter(
                queryMetadata.getBaseDN(), String.format(
                        queryMetadata.getFilter(),
                        "12345678-1234-1234-1234-123456789012"));

        assertNotNull(resultByGuidNonExistent);
        assertEquals(0, resultByGuidNonExistent.size());
    }

    @Test
    public void testADGetGroupByGroupIdCommand() throws Exception {
        internalTestGetGroupByGroupIdCommand(ldapTestsSetup.getAdLdapContext(), adGroupGuidExample, "");
    }

    @Test
    public void testIPAGetGroupByGroupIdCommand() throws Exception {
        setMockups(LdapProviderType.ipa);
        internalTestGetGroupByGroupIdCommand(ldapTestsSetup.getIpaLdapContext(),
                ipaGroupGuidExample,
                "accounts/groups/");
    }

    public void internalTestGetGroupByGroupIdCommand(LdapContextSource ldapCtx, Guid guidSample, String groupPath)
            throws Exception {

        Group movies = ldapTestsSetup.getGroup("groupE");
        Group superstars = ldapTestsSetup.getGroup("groupC");

        LdapSearchByIdParameters adParameters = new LdapSearchByIdParameters(
                dnToDomain(ldapCtx.getBaseLdapPathAsString()),
                guidSample);
        LdapGetAdGroupByGroupIdCommand command = new LdapGetAdGroupByGroupIdCommand(
                adParameters);
        LdapReturnValueBase retVal = command.Execute();
        assertNotNull(retVal);
        assertTrue(retVal.getSucceeded());
        ad_groups group = (ad_groups) retVal.getReturnValue();

        assertNotNull(group);
        assertEquals(dnToDomain(ldapCtx.getBaseLdapPathAsString()),
                group.getdomain());
        assertEquals(dnToDomain(ldapCtx.getBaseLdapPathAsString()) + "/" + groupPath +
                superstars.getName(), group.getname());
        assertEquals(1, group.getMemberOf().size());
        assertTrue(group.getMemberOf().get(0).contains(movies.getName()));

        // Illegal domain
        LdapSearchByIdParameters adParametersIllegalDomain = new LdapSearchByIdParameters(
                "blabla." + dnToDomain(ldapCtx.getBaseLdapPathAsString()),
                guidSample);
        LdapGetAdGroupByGroupIdCommand commandIllegalDomain = new LdapGetAdGroupByGroupIdCommand(
                adParametersIllegalDomain);
        LdapReturnValueBase retValIllegalDomain = commandIllegalDomain.Execute();
        assertNotNull(retValIllegalDomain);
        assertTrue(!retValIllegalDomain.getSucceeded());
        ad_groups groupIllegalDomain = (ad_groups) retValIllegalDomain
                .getReturnValue();
        assertNull(groupIllegalDomain);

        // No results
        LdapSearchByIdParameters adParametersNoResults = new LdapSearchByIdParameters(
                dnToDomain(ldapCtx.getBaseLdapPathAsString()), new Guid());
        LdapGetAdGroupByGroupIdCommand commandNoResults = new LdapGetAdGroupByGroupIdCommand(
                adParametersNoResults);
        LdapReturnValueBase retValNoResults = commandNoResults.Execute();
        assertNotNull(retValNoResults);
        assertTrue(retValNoResults.getSucceeded());
        ad_groups groupNoResults = (ad_groups) retValNoResults.getReturnValue();
        assertNull(groupNoResults);
    }

    @Test
    public void testADGetUserByUserIdCommand() throws Exception {
        internalTestGetUserByUserIdCommand(ldapTestsSetup.getAdLdapContext());
    }

    public void testIPAGetUserByUserIdCommand() throws Exception {
        setMockups(LdapProviderType.ipa);
        internalTestGetUserByUserIdCommand(ldapTestsSetup.getIpaLdapContext());
    }

    public void internalTestGetUserByUserIdCommand(LdapContextSource ldapCtx) throws Exception {
        Group others = ldapTestsSetup.getGroup("groupD");

        Person gandalf = ldapTestsSetup.getUser("userA");
        String krbPrincipalName = gandalf.getUsername() + "@"
                + gandalf.getDomain();
        LdapSearchByIdParameters adParameters = new LdapSearchByIdParameters(
                dnToDomain(ldapCtx.getBaseLdapPathAsString()),
                adGuidExample);
        LdapGetAdUserByUserIdCommand command = new LdapGetAdUserByUserIdCommand(
                adParameters);
        LdapReturnValueBase retVal = command.Execute();
        assertNotNull(retVal);
        assertTrue(retVal.getSucceeded());
        AdUser adUser = (AdUser) retVal.getReturnValue();
        assertNotNull(adUser);
        assertEquals(krbPrincipalName, adUser.getUserName());
        assertEquals(gandalf.getGivenName(), adUser.getName());
        List<String> memberOf = adUser.getMemberof();
        assertTrue(memberOf.get(0).contains(others.getName()));
        assertEquals(gandalf.getSurName(), adUser.getSurName());

        // Illegal domain
        LdapSearchByIdParameters adParametersIllegalDomain = new LdapSearchByIdParameters(
                "blabla." + dnToDomain(ldapCtx.getBaseLdapPathAsString()),
                adGuidExample);
        LdapGetAdUserByUserIdCommand commandIllegalDomain = new LdapGetAdUserByUserIdCommand(
                adParametersIllegalDomain);
        LdapReturnValueBase retValIllegalDomain = commandIllegalDomain.Execute();
        assertNotNull(retValIllegalDomain);
        assertTrue(!retValIllegalDomain.getSucceeded());
        AdUser adUserIllegalDomain = (AdUser) retValIllegalDomain
                .getReturnValue();
        assertNull(adUserIllegalDomain);

        // No Results
        LdapSearchByIdParameters adParametersNoResults = new LdapSearchByIdParameters(
                dnToDomain(ldapCtx.getBaseLdapPathAsString()), new Guid());
        LdapGetAdUserByUserIdCommand commandNoResults = new LdapGetAdUserByUserIdCommand(
                adParametersNoResults);
        LdapReturnValueBase retValNoResults = commandNoResults.Execute();
        assertNotNull(retValNoResults);
        assertTrue(retValNoResults.getSucceeded());
        AdUser adUserNoResults = (AdUser) retValNoResults.getReturnValue();
        assertNull(adUserNoResults);
    }

    @Test
    public void testADGetAdUserByUserIdListCommand() throws Exception {
        setMockups(LdapProviderType.activeDirectory);
        Person gandalf = ldapTestsSetup.getUser("userA");
        Person ksoze = ldapTestsSetup.getUser("userB");
        Group usual = ldapTestsSetup.getGroup("groupB");
        Group movies = ldapTestsSetup.getGroup("groupE");
        Group others = ldapTestsSetup.getGroup("groupD");

        ArrayList<Guid> userArr = new ArrayList<Guid>();
        userArr.add(adGuidExample);
        userArr.add(adGuidExample2);
        LdapSearchByIdListParameters adParameters = new LdapSearchByIdListParameters(
                dnToDomain(ldapTestsSetup.getAdLdapContext().getBaseLdapPathAsString()), userArr);
        LdapGetAdUserByUserIdListCommand command = new LdapGetAdUserByUserIdListCommand(
                adParameters);
        LdapReturnValueBase retVal = command.Execute();
        assertNotNull(retVal);
        assertTrue(retVal.getSucceeded());
        ArrayList<AdUser> adUsers = (ArrayList<AdUser>) retVal.getReturnValue();
        assertNotNull(adUsers);
        assertEquals(2, adUsers.size());
        boolean foundGandalf = false;
        boolean foundKaizer = false;
        boolean foundUnexpectedUser = false;

        for (AdUser currUser : adUsers) {
            if (currUser.getName().equals(ksoze.getName())) {
                assertEquals(ksoze.getSurName(), currUser.getSurName());
                foundKaizer = true;
                assertNotNull(currUser.getGroups().get(
                        dnToDomain(ldapTestsSetup.getAdLdapContext().getBaseLdapPathAsString())
                                + "/" + usual.getName()));
                assertNotNull(currUser.getGroups().get(
                        dnToDomain(ldapTestsSetup.getAdLdapContext().getBaseLdapPathAsString())
                                + "/" + movies.getName()));
                assertNotNull(currUser.getGroups().get(
                        dnToDomain(ldapTestsSetup.getAdLdapContext().getBaseLdapPathAsString())
                                + "/" + others.getName()));
                assertEquals(3, currUser.getGroups().size());
            } else if (currUser.getName().equals(gandalf.getName())) {
                assertEquals(gandalf.getSurName(), currUser.getSurName());
                foundGandalf = true;
                assertNotNull(currUser.getGroups().get(
                        dnToDomain(ldapTestsSetup.getAdLdapContext().getBaseLdapPathAsString())
                                + "/" + movies.getName()));
                assertNotNull(currUser.getGroups().get(
                        dnToDomain(ldapTestsSetup.getAdLdapContext().getBaseLdapPathAsString())
                                + "/" + others.getName()));
                assertEquals(2, currUser.getGroups().size());
            } else {
                foundUnexpectedUser = true;
            }
        }
        assertTrue(foundGandalf);
        assertTrue(foundKaizer);
        assertTrue(!foundUnexpectedUser);

        // No results
        LdapSearchByIdListParameters adParametersNoResults = new LdapSearchByIdListParameters(
                dnToDomain(ldapTestsSetup.getAdLdapContext().getBaseLdapPathAsString()),
                new ArrayList<Guid>());
        LdapGetAdUserByUserIdListCommand commandNoResults = new LdapGetAdUserByUserIdListCommand(
                adParametersNoResults);
        LdapReturnValueBase retValNoResults = commandNoResults.Execute();
        assertNotNull(retValNoResults);
        assertTrue(retVal.getSucceeded());
        ArrayList<AdUser> adUsersNoResults = (ArrayList<AdUser>) retValNoResults
                .getReturnValue();
        assertNotNull(adUsersNoResults);
        assertEquals(0, adUsersNoResults.size());

        // Illegal domain
        LdapSearchByIdListParameters adParametersIllegalDomain = new LdapSearchByIdListParameters(
                "blabla." + dnToDomain(ldapTestsSetup.getAdLdapContext().getBaseLdapPathAsString()),
                new ArrayList<Guid>());
        LdapGetAdUserByUserIdListCommand commandIllegalDomain = new LdapGetAdUserByUserIdListCommand(
                adParametersIllegalDomain);
        LdapReturnValueBase retValIllegalDomain = commandIllegalDomain.Execute();
        assertNotNull(retValIllegalDomain);
        assertTrue(retVal.getSucceeded());
        ArrayList<AdUser> adUsersIllegalDomain = (ArrayList<AdUser>) retValNoResults
                .getReturnValue();
        assertNotNull(adUsersIllegalDomain);
        assertEquals(0, adUsersIllegalDomain.size());
    }

    /**
     * clean the test data from ldap schemas
     */
    @Test
    public void zcleanup() {
        ldapTestsSetup.cleanup();
    }

}
