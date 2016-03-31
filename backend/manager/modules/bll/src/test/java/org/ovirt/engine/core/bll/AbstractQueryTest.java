package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertNotSame;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.common.utils.MockConfigRule.MockConfigDescriptor;
import static org.ovirt.engine.core.common.utils.MockConfigRule.mockConfig;

import java.lang.reflect.Constructor;
import java.lang.reflect.ParameterizedType;
import java.util.Collections;
import java.util.Set;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.EngineSession;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.utils.MockConfigRule;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.RandomUtils;

public abstract class AbstractQueryTest<P extends VdcQueryParametersBase, Q extends QueriesCommandBase<? extends P>> extends BaseCommandTest {

    @ClassRule
    public static MockConfigRule mcr = new MockConfigRule(
            mockConfig(ConfigValues.UserSessionTimeOutInterval, 60));

    protected P params;
    private Q query;

    /** Sets up a mock user a spy query with it, and the generic query parameters */
    @Before
    public void setUp() throws Exception {
        setUpMockQueryParameters();
        setUpSpyQuery();
        for (MockConfigDescriptor<?> mcd : getExtraConfigDescriptors()) {
            mcr.mockConfigValue(mcd);
        }
    }

    protected <T> Set<MockConfigDescriptor<T>> getExtraConfigDescriptors() {
        return Collections.emptySet();
    }

    /** Sets up a mock for {@link #params} */
    private void setUpMockQueryParameters() {
        params = mock(getParameterType());
        when(params.getSessionId()).thenReturn("test");
        when(params.getRefresh()).thenReturn(true);
    }

    /** Sets up a mock for {@link #query} */
    protected void setUpSpyQuery() throws Exception {
        setUpSpyQuery(getQueryParameters());
    }

    protected Q setUpSpyQuery(P parameters) throws Exception {
        DbFacade dbFacadeMock = mock(DbFacade.class);
        DbUser dbUserMock = mock(DbUser.class);

        when(engineSessionDao.save(any(EngineSession.class))).thenReturn(RandomUtils.instance().nextLong());
        when(engineSessionDao.remove(any(Long.class))).thenReturn(1);

        sessionDataContainer.setUser(parameters.getSessionId(), dbUserMock);

        Constructor<? extends Q> con = getQueryType().getConstructor(getParameterType());
        query = spy(con.newInstance(parameters));
        doReturn(sessionDataContainer).when(query).getSessionDataContainer();
        doReturn(dbFacadeMock).when(query).getDbFacade();
        doReturn(dbUserMock).when(query).initUser();
        initQuery(query);
        query.postConstruct();
        return query;
    }

    /** Hook for initialization */
    protected void initQuery(Q query) {
    }

    /** Extract the {@link Class} object for the P generic parameter */
    @SuppressWarnings("unchecked")
    protected Class<? extends P> getParameterType() {
        ParameterizedType parameterizedType =
                (ParameterizedType) getClass().getGenericSuperclass();
        return (Class<? extends P>) parameterizedType.getActualTypeArguments()[0];
    }

    /** Extract the {@link Class} object for the Q generic parameter */
    @SuppressWarnings("unchecked")
    protected Class<? extends Q> getQueryType() {
        ParameterizedType parameterizedType =
                (ParameterizedType) getClass().getGenericSuperclass();
        ParameterizedType queryParameterizedType = (ParameterizedType) parameterizedType.getActualTypeArguments()[1];
        return (Class<? extends Q>) queryParameterizedType.getRawType();
    }

    /** Power-Mocks {@link DbFacade#getInstance()} and returns a mock for it */
    protected DbFacade getDbFacadeMockInstance() {
        return getQuery().getDbFacade();
    }

    /** @return The spied query to use in the test */
    protected Q getQuery() {
        return query;
    }

    /** @return The mock query parameters to use in the test */
    protected P getQueryParameters() {
        return params;
    }

    @Test
    public void testQueryType() throws IllegalArgumentException, IllegalAccessException {
        assertNotSame("The query can't be found in the enum VdcQueryType",
                VdcQueryType.Unknown,
                TestHelperQueriesCommandType.getQueryTypeFieldValue(query));
    }
}
