package org.ovirt.engine.core.itests.ldap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.mockito.ArgumentMatcher;
import org.ovirt.engine.core.bll.AbstractQueryTest;
import org.ovirt.engine.core.bll.AdUsersSearchQuery;
import org.ovirt.engine.core.bll.SearchQuery;
import org.ovirt.engine.core.bll.adbroker.AdActionType;
import org.ovirt.engine.core.bll.adbroker.LdapBroker;
import org.ovirt.engine.core.bll.adbroker.LdapQueryType;
import org.ovirt.engine.core.bll.adbroker.LdapReturnValueBase;
import org.ovirt.engine.core.bll.adbroker.LdapSearchByQueryParameters;
import org.ovirt.engine.core.common.businessentities.AdUser;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.AdUsersSearchParameters;
import org.ovirt.engine.core.common.queries.SearchParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.PostgresDbEngineDialect;
import org.ovirt.engine.core.utils.MockConfigRule;
import org.ovirt.engine.core.utils.RandomUtils;

@RunWith(Parameterized.class)
public class LdapSearchQueryTest extends AbstractQueryTest<SearchParameters, SearchQuery<? extends SearchParameters>> {

    @Rule
    public static final MockConfigRule mcr = new MockConfigRule();

    /** Constants */
    public static final String DOMAIN = RandomUtils.instance().nextString(10);

    private Class<? extends SearchQuery<? extends SearchParameters>> queryType;
    private SearchParameters queryParameters;

    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]
        { { SearchQuery.class, new SearchParameters("AdUser: allnames=gandalf", SearchType.AdUser) },
                { AdUsersSearchQuery.class, new AdUsersSearchParameters("allnames=gandalf") } });
    }

    public LdapSearchQueryTest(Class<? extends SearchQuery<? extends SearchParameters>> queryType,
            SearchParameters queryParamters) {
        this.queryType = queryType;
        this.queryParameters = queryParamters;
    }

    @Override
    protected Class<? extends SearchQuery<? extends SearchParameters>> getQueryType() {
        return queryType;
    }

    @Override
    protected SearchParameters getQueryParameters() {
        return queryParameters;
    }

    @Override
    protected Class<? extends SearchParameters> getParameterType() {
        return queryParameters.getClass();
    }

    private AdUser result;

    @Before
    public void initResult() {
        result = new AdUser("gandalf", "melon!", Guid.NewGuid(), DOMAIN);
    }

    public void initConfig() {
        mcr.<String> mockConfigValue(ConfigValues.LDAPSecurityAuthentication,
                Config.DefaultConfigurationVersion, "SIMPLE");
        mcr.<Integer> mockConfigValue(ConfigValues.SearchResultsLimit, Config.DefaultConfigurationVersion, 100);
        mcr.<String> mockConfigValue(ConfigValues.AuthenticationMethod,
                Config.DefaultConfigurationVersion, "LDAP");
        mcr.<String> mockConfigValue(ConfigValues.DBEngine,
                Config.DefaultConfigurationVersion, "postgres");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSearchQuery() {
        initConfig();
        when(getDbFacadeMockInstance().getDbEngineDialect()).thenReturn(new PostgresDbEngineDialect());

        doReturn(DOMAIN).when(getQuery()).getDefaultDomain();

        LdapBroker ldapFactoryMock = mock(LdapBroker.class);

        doReturn(ldapFactoryMock).when(getQuery()).getLdapFactory(DOMAIN);
        LdapReturnValueBase ldapRerunValue = new LdapReturnValueBase();
        ldapRerunValue.setSucceeded(true);
        ldapRerunValue.setReturnValue(Collections.singletonList(result));
        when(ldapFactoryMock.RunAdAction(eq(AdActionType.SearchUserByQuery),
                argThat(new LdapParametersMatcher("gandalf")))).
                thenReturn(ldapRerunValue);

        getQuery().setInternalExecution(true);
        getQuery().Execute();
        assertTrue("Query should succeed, but failed with: " + getQuery().getQueryReturnValue().getExceptionString(),
                getQuery().getQueryReturnValue().getIsSearchValid());
        assertEquals("Wrong user returned",
                result,
                ((List<AdUser>) getQuery().getQueryReturnValue().getReturnValue()).get(0));
    }

    private class LdapParametersMatcher extends ArgumentMatcher<LdapSearchByQueryParameters> {

        private String name;

        public LdapParametersMatcher(String name) {
            this.name = name;
        }

        @Override
        public boolean matches(Object argument) {
            if (!(argument instanceof LdapSearchByQueryParameters)) {
                return false;
            }
            LdapSearchByQueryParameters ldapParams = (LdapSearchByQueryParameters) argument;
            return ldapParams.getLdapQueryData().getFilterParameters().length == 1 &&
                    ((String) ldapParams.getLdapQueryData().getFilterParameters()[0]).contains(name) &&
                    ldapParams.getLdapQueryData().getLdapQueryType().equals(LdapQueryType.searchUsers) &&
                    ldapParams.getDomain().equals(DOMAIN);

        }
    }
}
