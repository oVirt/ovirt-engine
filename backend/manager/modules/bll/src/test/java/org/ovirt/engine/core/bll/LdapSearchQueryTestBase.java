package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.utils.MockConfigRule.mockConfig;

import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.ovirt.engine.core.bll.adbroker.AdActionType;
import org.ovirt.engine.core.bll.adbroker.LdapBroker;
import org.ovirt.engine.core.bll.adbroker.LdapQueryType;
import org.ovirt.engine.core.bll.adbroker.LdapReturnValueBase;
import org.ovirt.engine.core.bll.adbroker.LdapSearchByQueryParameters;
import org.ovirt.engine.core.common.businessentities.IVdcQueryable;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.queries.SearchParameters;
import org.ovirt.engine.core.dal.dbbroker.PostgresDbEngineDialect;
import org.ovirt.engine.core.utils.MockConfigRule;
import org.ovirt.engine.core.utils.RandomUtils;

public abstract class LdapSearchQueryTestBase extends AbstractQueryTest<SearchParameters, SearchQuery<? extends SearchParameters>> {

    protected static final String NAME_TO_SEARCH = "gandalf";

    @ClassRule
    public static final MockConfigRule mcr =
            new MockConfigRule(
                    mockConfig(ConfigValues.LDAPSecurityAuthentication, "SIMPLE"),
                    mockConfig(ConfigValues.SearchResultsLimit, 100),
                    mockConfig(ConfigValues.AuthenticationMethod, "LDAP"),
                    mockConfig(ConfigValues.DBEngine, "postgres")
            );

    /** Constants */
    public static final String DOMAIN = RandomUtils.instance().nextString(10);

    private SearchParameters queryParameters;
    private Class<? extends SearchQuery<? extends SearchParameters>> queryType;

    public LdapSearchQueryTestBase(Class<? extends SearchQuery<? extends SearchParameters>> queryType,
            SearchParameters queryParamters) {
        this.queryType = queryType;
        this.queryParameters = queryParamters;
    }

    private IVdcQueryable result;

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

    @Before
    public void initResult() {
        result = getExpectedResult();
    }

    protected abstract IVdcQueryable getExpectedResult();

    @SuppressWarnings("unchecked")
    @Test
    public void testSearchQuery() {
        when(getDbFacadeMockInstance().getDbEngineDialect()).thenReturn(new PostgresDbEngineDialect());

        doReturn(DOMAIN).when(getQuery()).getDefaultDomain();

        LdapBroker ldapFactoryMock = mock(LdapBroker.class);

        doReturn(ldapFactoryMock).when(getQuery()).getLdapFactory(DOMAIN);
        LdapReturnValueBase ldapRerunValue = new LdapReturnValueBase();
        ldapRerunValue.setSucceeded(true);
        ldapRerunValue.setReturnValue(Collections.singletonList(result));
        when(ldapFactoryMock.RunAdAction(eq(getAdActionType()),
                argThat(new LdapParametersMatcher()))).
                thenReturn(ldapRerunValue);

        getQuery().setInternalExecution(true);
        getQuery().Execute();
        assertTrue("Query should succeed, but failed with: " + getQuery().getQueryReturnValue().getExceptionString(),
                getQuery().getQueryReturnValue().getIsSearchValid());
        assertEquals("Wrong ldap result returned",
                result,
                ((List<IVdcQueryable>) getQuery().getQueryReturnValue().getReturnValue()).get(0));
    }

    protected abstract AdActionType getAdActionType();

    protected abstract LdapQueryType getLdapActionType();

    public class LdapParametersMatcher extends ArgumentMatcher<LdapSearchByQueryParameters> {

        @Override
        public boolean matches(Object argument) {
            if (!(argument instanceof LdapSearchByQueryParameters)) {
                return false;
            }
            LdapSearchByQueryParameters ldapParams = (LdapSearchByQueryParameters) argument;
            return ldapParams.getLdapQueryData().getFilterParameters().length == 1 &&
                    ((String) ldapParams.getLdapQueryData().getFilterParameters()[0]).contains(NAME_TO_SEARCH) &&
                    ldapParams.getLdapQueryData().getLdapQueryType().equals(getLdapActionType()) &&
                    ldapParams.getDomain().equals(DOMAIN);

        }
    }
}
